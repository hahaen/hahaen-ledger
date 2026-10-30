package com.hahaen.ledger.lottery.service;

import com.hahaen.ledger.common.security.AuditIdentity;
import com.hahaen.ledger.lottery.entity.*;
import com.hahaen.ledger.lottery.mapper.LotteryStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.dao.DuplicateKeyException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class LotterySyncService {
    private final LotteryStore db;
    private final LotteryOfficialClient client;
    private final TransactionTemplate tx;

    public void sync(String game) {
        LotterySyncState s=db.one(LotterySyncState.class,q->q.eq("game",game));
        if(s==null) {
            s=new LotterySyncState();s.setGame(game);s.setHistoryPage(1);s.setHistoryDone(0);s.setCatchupPage(1);s.setCreatedBy(AuditIdentity.SYSTEM_USER_ID);
            try {db.insert(s);}catch(DuplicateKeyException ignored) { /* 另一实例已初始化。 */ }
        }
        String token=UUID.randomUUID().toString();
        if(!db.claimSync(game,token,120))return;
        try {
            s=db.one(LotterySyncState.class,q->q.eq("game",game));
            // 每次先刷新最新页；补漏游标保留上一轮旧头部，不能被最新页覆盖。
            var latest=db.one(LotteryDraw.class,q->q.eq("game",game).orderByDesc("issue"));
            if(s.getCatchupIssue()==null) {s.setCatchupIssue(latest==null?"":latest.getIssue());s.setCatchupPage(1);}
            String boundary=s.getCatchupIssue();
            var head=client.fetch(game,1);savePage(game,token,head);
            if(s.getCatchupPage()==1) {
                boolean overlap=head.items().stream().anyMatch(i->i.draw().getIssue().compareTo(boundary)<=0);
                if(boundary.isEmpty()||overlap||head.pages()<=1)s.setCatchupIssue(null);
                else s.setCatchupPage(2);
            }else {
                var page=client.fetch(game,s.getCatchupPage());savePage(game,token,page);
                boolean overlap=page.items().stream().anyMatch(i->i.draw().getIssue().compareTo(boundary)<=0);
                if(overlap||page.items().isEmpty()||s.getCatchupPage()>=page.pages()) {s.setCatchupIssue(null);s.setCatchupPage(1);}
                else s.setCatchupPage(s.getCatchupPage()+1);
            }
            { // 回填完成后循环低频复查历史，发现较早期奖金后补与官方更正。
                var page=client.fetch(game,s.getHistoryPage());savePage(game,token,page);
                if(page.items().isEmpty()||s.getHistoryPage()>=page.pages()){s.setHistoryDone(1);s.setHistoryPage(1);}
                else s.setHistoryPage(s.getHistoryPage()+1);
            }
            s.setLastSuccessAt(LotteryRules.now());s.setLastError("");s.setUpdatedBy(AuditIdentity.SYSTEM_USER_ID);
            db.saveSyncProgress(s,token);
        }catch(RuntimeException ex) {
            LotterySyncState failed=db.one(LotterySyncState.class,q->q.eq("game",game));
            failed.setLastError(ex.getClass().getSimpleName());failed.setUpdatedBy(AuditIdentity.SYSTEM_USER_ID);db.saveSyncProgress(failed,token);
            throw ex;
        }finally {db.releaseSync(game,token);}
    }
    private void savePage(String game,String token,LotteryOfficialClient.Page page) {
        tx.executeWithoutResult(status->{
            if(!db.lockSyncPage(game,token))throw new IllegalStateException("Sync lease expired");
            for(var item:page.items()) {
                LotteryDraw d=item.draw();LotteryDraw old=db.one(LotteryDraw.class,q->q.eq("game",game).eq("issue",d.getIssue()));
                if(old!=null) {
                    d.setId(old.getId());d.setVersion(old.getVersion());
                    if(!old.getSourceHash().equals(d.getSourceHash()))d.setVersion(old.getVersion()+1);
                    if(d.getNextIssue()==null)d.setNextIssue(old.getNextIssue());
                    if(old.getSourceHash().equals(d.getSourceHash())&&"UNKNOWN".equals(d.getSpecialState())&&!"UNKNOWN".equals(old.getSpecialState()))d.setSpecialState(old.getSpecialState());
                    d.setUpdatedBy(AuditIdentity.SYSTEM_USER_ID);db.updateSystem(d);
                }else {d.setCreatedBy(AuditIdentity.SYSTEM_USER_ID);d.setCreatedName("系统");db.insert(d);}
                if(db.count(LotteryDrawRevision.class,q->q.eq("draw_id",d.getId()).eq("source_hash",d.getSourceHash()))==0) {
                    LotteryDrawRevision r=new LotteryDrawRevision();r.setDrawId(d.getId());r.setSourceHash(d.getSourceHash());r.setRawJson(item.raw());r.setCreatedBy(AuditIdentity.SYSTEM_USER_ID);db.insert(r);
                }
            }
            if("SSQ".equals(game))resolveSpecial();
        });
    }
    private void resolveSpecial() {
        // 只沿官方连续列表连接传播状态；当前奖池不足以独立判断当期是否执行特别规定。
        for(LotteryDraw current:db.list(LotteryDraw.class,q->q.eq("game","SSQ").ge("issue","2026014").orderByAsc("issue"))) {
            LotteryDraw previous=db.one(LotteryDraw.class,q->q.eq("game","SSQ").eq("next_issue",current.getIssue()));
            if(previous==null||previous.getPoolCents()==null)continue;
            String state=previous.getPoolCents()<30_000_000_000L?"OFF":previous.getPoolCents()>=150_000_000_000L?"ON":previous.getSpecialState();
            if(!"UNKNOWN".equals(state)&&!state.equals(current.getSpecialState())) {
                current.setSpecialState(state);current.setVersion(current.getVersion()+1);current.setUpdatedBy(AuditIdentity.SYSTEM_USER_ID);db.updateSystem(current);
            }
        }
    }
}
