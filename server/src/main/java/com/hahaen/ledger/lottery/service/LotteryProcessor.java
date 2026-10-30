package com.hahaen.ledger.lottery.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hahaen.ledger.common.security.AuditIdentity;
import com.hahaen.ledger.lottery.entity.*;
import com.hahaen.ledger.lottery.mapper.LotteryStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import static com.hahaen.ledger.lottery.service.LotteryRules.*;

@Service
@RequiredArgsConstructor
public class LotteryProcessor {
    private final LotteryStore db;
    private final LotteryService service;
    private final ObjectMapper json;
    @Transactional(propagation=org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void process(long user) {
        if(!service.enabled(user))return;service.require(user,true);
        for(var batch:db.list(LotteryBatch.class,q->q.eq("user_id",user).orderByAsc("id")))bind(batch);
        for(var e:db.unsettled(user)) {
            LotteryDraw d=db.one(LotteryDraw.class,q->q.eq("game",e.getGame()).eq("issue",e.getIssue()));
            if(d==null||db.unsettledDeliveries(e.getId())>0)continue;
            Result result;
            try {result=settle(e,d,json.readTree(d.getPrizes()));}catch(com.fasterxml.jackson.core.JsonProcessingException ex){throw new IllegalStateException("Stored prize JSON invalid",ex);}
            String resultHash=hash(result.status()+"|"+result.grade()+"|"+result.cents()+"|"+d.getRed()+"|"+d.getBlue());
            String previous=e.getResultHash();
            // MyBatis默认不更新NULL；先显式清理旧奖金，防止更正为待核实时继续展示旧金额。
            db.clearSettlement(e.getId());e.setResult(result.status());e.setGrade(result.grade());e.setPrizeCents(result.cents());e.setDrawId(d.getId());e.setDrawVersion(d.getVersion());e.setResultHash(resultHash);e.setUpdatedBy(AuditIdentity.SYSTEM_USER_ID);db.updateSystem(e);
            if(e.getPlanId()==null) {
                if(e.getAcceptedAt()!=null&&previous!=null&&!previous.equals(resultHash)) {
                    event(user,e.getId(),null,"entry:"+e.getId()+":correction:v"+d.getVersion()+":"+resultHash,"CORRECTION","彩票结果更正",entryBody(e));
                }else if(e.getAcceptedAt()==null&&"SETTLED".equals(result.status())) {
                    event(user,e.getId(),null,"entry:"+e.getId()+":v"+e.getRevision(),"RESULT","彩票开奖提醒",entryBody(e));
                }else if(e.getAcceptedAt()==null)service.cancelEntrySystem(e);
            }else if(previous!=null&&!previous.equals(resultHash)&&expiryAccepted(e.getBatchId())) {
                event(user,e.getId(),e.getBatchId(),"batch:"+e.getBatchId()+":entry:"+e.getId()+":v"+d.getVersion()+":"+resultHash,"CORRECTION","彩票批次结果更正",entryBody(e));
            }
        }
        for(var batch:db.list(LotteryBatch.class,q->q.eq("user_id",user))) {
            LotteryPlan p=db.get(LotteryPlan.class,batch.getPlanId());if(p==null||!"ACTIVE".equals(p.getStatus()))continue;
            List<LotteryEntry> entries=db.list(LotteryEntry.class,q->q.eq("batch_id",batch.getId()).orderByAsc("position"));
            if(entries.size()!=batch.getPeriods()||entries.getLast().getDrawId()==null)continue;
            long cost=entries.stream().mapToLong(LotteryEntry::getCostCents).sum();
            long prize=entries.stream().filter(e->e.getPrizeCents()!=null).mapToLong(LotteryEntry::getPrizeCents).sum();
            long pending=entries.stream().filter(e->e.getPrizeCents()==null).count();
            LotteryDraw last=db.get(LotteryDraw.class,entries.getLast().getDrawId());
            LotteryDraw next=last==null||last.getNextIssue()==null?null:db.one(LotteryDraw.class,
                    q->q.eq("game",batch.getGame()).eq("issue",last.getNextIssue()));
            String body="哈记账："+p.getName()+"\n号码："+entries.getFirst().getRed()+" + "+entries.getFirst().getBlue()+"\n本批"+batch.getPeriods()+"期已到期\n成本："+yuan(cost)+"元\n已确认税前奖金："+yuan(prize)+"元"+(pending>0?"\n另有"+pending+"期奖金待确认":"")
                    +"\n"+LotteryRenewalDeadline.describe(batch.getGame(),last,next,now())+"\n如已实际续买，请进入彩票登记新批次。";
            event(user,null,batch.getId(),"batch:"+batch.getId()+":expiry","EXPIRY","彩票续买提醒",body);
        }
        for(var event:db.list(LotteryEvent.class,q->q.eq("user_id",user).eq("cancelled",0))) {
            for(var config:service.channels(user))if(db.count(LotteryDelivery.class,q->q.eq("event_id",event.getId()).eq("channel",config.getNotificationType()))==0) {
                LotteryDelivery d=new LotteryDelivery();d.setEventId(event.getId());d.setUserId(user);d.setChannel(config.getNotificationType());d.setStatus("PENDING");d.setAttempts(0);d.setNextAttemptAt(now());d.setCreatedBy(AuditIdentity.SYSTEM_USER_ID);db.insert(d);
            }
        }
    }
    private void bind(LotteryBatch batch) {
        String expected=batch.getStartIssue();
        for(var e:db.list(LotteryEntry.class,q->q.eq("batch_id",batch.getId()).orderByAsc("position"))) {
            if(expected==null)break;
            if(e.getIssue()==null) {
                String boundIssue=expected;
                if(db.count(LotteryEntry.class,q->q.eq("plan_id",e.getPlanId()).eq("issue",boundIssue))>0)throw error("批次期号与既有购买记录重叠，请核对");
                e.setIssue(expected);e.setUpdatedBy(AuditIdentity.SYSTEM_USER_ID);db.updateSystem(e);
            }else if(!e.getIssue().equals(expected))throw error("批次期号顺序与官方记录不符");
            LotteryDraw d=db.one(LotteryDraw.class,q->q.eq("game",batch.getGame()).eq("issue",e.getIssue()));expected=d==null?null:d.getNextIssue();
        }
    }
    private boolean expiryAccepted(long batch) {return db.acceptedBatch(batch)>0;}
    private void event(long user,Long entry,Long batch,String key,String kind,String title,String body) {
        LotteryEvent old=db.one(LotteryEvent.class,q->q.eq("event_key",key));
        if(old!=null) {
            // 已受理消息快照不改写；未投递或失败的消息跟随当前完整结算更新。
            if(db.count(LotteryDelivery.class,q->q.eq("event_id",old.getId()).in("status","ACCEPTED","SENDING","UNKNOWN"))==0) {
                old.setCancelled(0);old.setBody(body);old.setUpdatedBy(AuditIdentity.SYSTEM_USER_ID);db.updateSystem(old);
                for(var delivery:db.list(LotteryDelivery.class,q->q.eq("event_id",old.getId()).eq("status","CANCELLED"))) {delivery.setStatus("PENDING");delivery.setNextAttemptAt(now());delivery.setUpdatedBy(AuditIdentity.SYSTEM_USER_ID);db.updateSystem(delivery);}
            }return;
        }
        LotteryEvent event=new LotteryEvent();event.setUserId(user);event.setEntryId(entry);event.setBatchId(batch);event.setEventKey(key);event.setKind(kind);event.setTitle(title);event.setBody(body);event.setCancelled(0);event.setCreatedBy(AuditIdentity.SYSTEM_USER_ID);db.insert(event);
    }
    private String entryBody(LotteryEntry e) {return "哈记账："+("DLT".equals(e.getGame())?"大乐透":"双色球")+" 第"+e.getIssue()+"期\n号码："+e.getRed()+" + "+e.getBlue()+"\n结果："+e.getGrade()+"\n成本："+yuan(e.getCostCents())+"元\n税前奖金："+(e.getPrizeCents()==null?"待确认":yuan(e.getPrizeCents())+"元");}
    public static String yuan(long cents) {return java.math.BigDecimal.valueOf(cents,2).stripTrailingZeros().toPlainString();}
}
