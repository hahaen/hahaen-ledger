package com.hahaen.ledger.lottery.service;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import com.hahaen.ledger.common.entity.BaseAuditEntity;
import com.hahaen.ledger.common.security.*;
import com.hahaen.ledger.lottery.dto.*;
import com.hahaen.ledger.lottery.entity.*;
import com.hahaen.ledger.lottery.mapper.LotteryStore;
import com.hahaen.ledger.user.entity.AppUser;
import com.hahaen.ledger.user.mapper.UserNotificationConfigMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hahaen.ledger.user.entity.UserNotificationConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;
import java.util.function.Supplier;
import static com.hahaen.ledger.lottery.service.LotteryRules.*;

@Service
@RequiredArgsConstructor
public class LotteryService {
    private final LotteryStore db;
    private final ObjectMapper json;
    private final UserNotificationConfigMapper configs;
    private final LotteryPlanProgressService progress;
    private final org.springframework.context.ApplicationEventPublisher publisher;
    public record Changed(long userId) {}

    public AppUser require(long user,boolean lock) {
        AppUser u=db.user(user,lock);
        if(u==null||!"ACTIVE".equals(u.getStatus())||!Integer.valueOf(1).equals(u.getLotteryEnabled()))throw error("未开通彩票功能或账号已停用");
        return u;
    }
    public boolean enabled(long user) {
        AppUser u=db.user(user,false);return u!=null&&"ACTIVE".equals(u.getStatus())&&Integer.valueOf(1).equals(u.getLotteryEnabled());
    }
    public List<UserNotificationConfig> channels(long user) {
        return configs.selectList(new LambdaQueryWrapper<UserNotificationConfig>().eq(UserNotificationConfig::getUserId,user)
                .eq(UserNotificationConfig::getDeleted,0).in(UserNotificationConfig::getNotificationType,"BARK","PUSHPLUS"));
    }
    private long owner(){long user=CurrentUser.id();require(user,false);return user;}
    public ObjectNode overview(String game,String kind,int page) {
        long user=owner();game(game);page(page);if(!Set.of("single","plan").contains(kind))throw error("清单类型无效");
        ObjectNode result=json.createObjectNode();
        result.set("summary",view(db.summary(user,game,null)));
        boolean notificationConfigured = !channels(user).isEmpty();
        result.put("notificationConfigured",notificationConfigured);
        List<?> values=kind.equals("single")?db.list(LotteryEntry.class,q->q.eq("user_id",user).eq("game",game).isNull("plan_id").orderByDesc("created_at","id").last(paging(page))):
                db.list(LotteryPlan.class,q->q.eq("user_id",user).eq("game",game).orderByDesc("created_at","id").last(paging(page)));
        ArrayNode items=json.createArrayNode();
        values.stream().limit(20).forEach(value->{ObjectNode item=(ObjectNode)view(value);if(value instanceof LotteryPlan p){item.set("summary",view(db.summary(user,game,p.getId())));item.put("awaitingRenewal",awaiting(p));item.set("progress",view(progress.describe(p,notificationConfigured)));}items.add(item);});
        result.set("items",items);result.put("hasMore",values.size()>20);result.set("sync",syncInfo(game));return result;
    }
    public ObjectNode draws(String game,int page) {
        owner();game(game);page(page);List<LotteryDraw> values=db.list(LotteryDraw.class,q->q.eq("game",game).orderByDesc("issue").last(paging(page)));
        ObjectNode result=json.createObjectNode();result.set("items",view(values.stream().limit(20).toList()));result.put("hasMore",values.size()>20);result.set("sync",syncInfo(game));return result;
    }
    private JsonNode syncInfo(String game) {
        LotterySyncState s=db.one(LotterySyncState.class,q->q.eq("game",game));
        ObjectNode result=json.createObjectNode();if(s!=null){result.put("lastSuccessAt",s.getLastSuccessAt()==null?null:s.getLastSuccessAt().toString());result.put("lastError",s.getLastError());result.put("historyComplete",s.getHistoryDone()==1);result.put("historyPage",s.getHistoryPage());}return result;
    }
    public ObjectNode detail(String kind,long id,int page) {
        long user=owner();page(page);ObjectNode result;
        switch(kind) {
            case "entry"->{LotteryEntry e=entry(user,id);result=(ObjectNode)view(e);result.put("editable",editable(e));
                if(e.getDrawId()!=null)result.set("draw",view(db.get(LotteryDraw.class,e.getDrawId())));
                List<LotteryEvent> events=db.list(LotteryEvent.class,q->q.eq("user_id",user).eq("entry_id",id).eq("cancelled",0).orderByDesc("id").last("LIMIT 20"));result.set("notifications",eventViews(events));}
            case "plan"->{LotteryPlan p=plan(user,id);result=(ObjectNode)view(p);result.set("summary",view(db.summary(user,p.getGame(),id)));result.put("awaitingRenewal",awaiting(p));result.set("progress",view(progress.describe(p,!channels(user).isEmpty())));
                List<LotteryBatch> values=db.list(LotteryBatch.class,q->q.eq("user_id",user).eq("plan_id",id).orderByDesc("created_at","id").last(paging(page)));
                result.set("items",view(values.stream().limit(20).toList()));result.put("hasMore",values.size()>20);}
            case "batch"->{LotteryBatch b=batch(user,id);result=(ObjectNode)view(b);List<LotteryEntry> entries=db.list(LotteryEntry.class,q->q.eq("user_id",user).eq("batch_id",id).orderByAsc("position"));
                ArrayNode items=json.createArrayNode();for(var e:entries){ObjectNode v=(ObjectNode)view(e);v.put("editable",editable(e));items.add(v);}result.set("items",items);
                ObjectNode totals=json.createObjectNode();long cost=0,settledCost=0,prize=0,waitingCost=0,pending=0,drawn=0;
                for(var e:entries){cost+=e.getCostCents();if(e.getDrawId()!=null)drawn++;if(e.getPrizeCents()==null)pending++;else {settledCost+=e.getCostCents();prize+=e.getPrizeCents();}if("WAITING".equals(e.getResult()))waitingCost+=e.getCostCents();}
                totals.put("costCents",String.valueOf(cost));totals.put("settledCostCents",String.valueOf(settledCost));totals.put("prizeCents",String.valueOf(prize));totals.put("profitCents",String.valueOf(prize-settledCost));totals.put("waitingCostCents",String.valueOf(waitingCost));totals.put("pendingCount",pending);totals.put("entryCount",entries.size());totals.put("drawnCount",drawn);totals.put("remainingCount",entries.size()-drawn);result.set("summary",totals);
                result.set("notifications",eventViews(db.list(LotteryEvent.class,q->q.eq("user_id",user).eq("batch_id",id).eq("cancelled",0).orderByDesc("id").last("LIMIT 20"))));}
            case "draw"->{LotteryDraw d=db.get(LotteryDraw.class,id);if(d==null)throw error("开奖不存在");result=(ObjectNode)view(d);}
            default->throw error("详情类型无效");
        }
        result.put("notificationConfigured",!channels(user).isEmpty());return result;
    }
    private ArrayNode eventViews(List<LotteryEvent> events) {
        ArrayNode nodes=json.createArrayNode();
        for(var event:events){ObjectNode v=(ObjectNode)view(event);v.set("deliveries",view(db.list(LotteryDelivery.class,q->q.eq("event_id",event.getId()))));nodes.add(v);}return nodes;
    }
    private boolean awaiting(LotteryPlan p) {
        LotteryBatch b=db.one(LotteryBatch.class,q->q.eq("plan_id",p.getId()).eq("user_id",p.getUserId()).orderByDesc("created_at","id"));
        return b!=null&&db.count(LotteryEntry.class,q->q.eq("batch_id",b.getId()).isNull("draw_id"))==0;
    }
    public LotteryEntry entry(long user,long id) {LotteryEntry e=db.get(LotteryEntry.class,id);if(e==null||e.getUserId()!=user)throw error("购买记录不存在");return e;}
    public LotteryPlan plan(long user,long id) {LotteryPlan p=db.get(LotteryPlan.class,id);if(p==null||p.getUserId()!=user)throw error("计划不存在");return p;}
    public LotteryBatch batch(long user,long id) {LotteryBatch b=db.get(LotteryBatch.class,id);if(b==null||b.getUserId()!=user)throw error("批次不存在");return b;}
    private void validate(LotterySaveRequest r,boolean issueNeeded) {
        game(r.game());red(r.game(),r.red());blue(r.game(),r.blue());cost(r.game(),r.multiple(),r.additional());
        if(r.purchasedOn()==null||r.purchasedOn().isAfter(LocalDate.now(ZONE))||r.purchasedOn().isBefore(LocalDate.of(2000,1,1)))throw error("购买日期无效");
        if(issueNeeded)issue(r.game(),r.issue());
        if(r.periods()<1||r.periods()>20)throw error("购买期数须为1至20");
        if(r.note()!=null&&r.note().length()>500||r.name()!=null&&r.name().length()>60)throw error("名称或备注过长");
    }
    @Transactional
    public String create(String kind,LotterySaveRequest r) {
        long user=CurrentUser.id();require(user,true);validate(r,!"plan".equals(kind));
        return write(user,r.idempotencyKey(),kind+":"+r,()->{
            if("plan".equals(kind)) {
                if(r.name()==null||r.name().isBlank())throw error("请填写计划名称");
                LotteryPlan p=new LotteryPlan();p.setUserId(user);p.setGame(r.game());p.setName(r.name().trim());p.setRed(red(r.game(),r.red()));p.setBlue(blue(r.game(),r.blue()));p.setMultiple(r.multiple());p.setAdditional(r.additional()?1:0);p.setStatus("ACTIVE");p.setNote(r.note());db.insert(p);return p.getId();
            }
            if(!"single".equals(kind))throw error("创建类型无效");
            if(!r.confirmedPurchase())throw error("请确认已经实际购买");
            LotteryEntry e=from(user,r);e.setIssue(issue(r.game(),r.issue()));db.insert(e);return e.getId();
        });
    }
    @Transactional
    public String purchase(long planId,LotterySaveRequest r) {
        long user=CurrentUser.id();require(user,true);validate(r,true);
        return write(user,r.idempotencyKey(),"purchase:"+planId+":"+r,()->{
            LotteryPlan p=plan(user,planId);if(!"ACTIVE".equals(p.getStatus()))throw error("请先恢复进行中的计划");
            if(!r.confirmedPurchase())throw error("请确认已经实际购买");
            if(!p.getGame().equals(r.game()))throw error("购买彩种与计划不一致");
            String start=issue(r.game(),r.issue());
            LotteryBatch previous=db.one(LotteryBatch.class,q->q.eq("plan_id",planId).orderByDesc("start_issue"));
            if(previous!=null) {
                if(db.count(LotteryEntry.class,q->q.eq("batch_id",previous.getId()).isNull("issue"))>0)throw error("上一批未来期号尚未全部核对，暂不能确认新的不重叠批次");
                LotteryEntry last=db.one(LotteryEntry.class,q->q.eq("batch_id",previous.getId()).orderByDesc("issue"));
                if(last!=null&&start.compareTo(last.getIssue())<=0)throw error("续买起始期必须晚于上一批最后一期");
            }
            LotteryBatch b=new LotteryBatch();b.setUserId(user);b.setPlanId(planId);b.setGame(r.game());b.setStartIssue(start);b.setPeriods(r.periods());b.setPurchasedOn(r.purchasedOn());b.setNote(r.note());db.insert(b);
            for(int i=1;i<=r.periods();i++) {LotteryEntry e=from(user,r);e.setPlanId(planId);e.setBatchId(b.getId());e.setPosition(i);if(i==1)e.setIssue(start);db.insert(e);}
            return b.getId();
        });
    }
    private LotteryEntry from(long user,LotterySaveRequest r) {
        LotteryEntry e=new LotteryEntry();e.setUserId(user);e.setGame(r.game());e.setRed(red(r.game(),r.red()));e.setBlue(blue(r.game(),r.blue()));e.setMultiple(r.multiple());e.setAdditional(r.additional()?1:0);e.setPurchasedOn(r.purchasedOn());e.setNote(r.note());e.setCostCents(cost(r.game(),r.multiple(),r.additional()));e.setResult("WAITING");e.setPosition(1);e.setRevision(1);return e;
    }
    @Transactional
    public String edit(String kind,long id,LotterySaveRequest r) {
        long user=CurrentUser.id();require(user,true);validate(r,false);
        return write(user,r.idempotencyKey(),"edit:"+kind+":"+id+":"+r,()->{
            if("plan".equals(kind)) {
                LotteryPlan p=plan(user,id);if(!p.getGame().equals(r.game())||r.name()==null||r.name().isBlank())throw error("彩种不可修改，计划名称必填");
                p.setName(r.name().trim());p.setRed(red(r.game(),r.red()));p.setBlue(blue(r.game(),r.blue()));p.setMultiple(r.multiple());p.setAdditional(r.additional()?1:0);p.setNote(r.note()==null?"":r.note());db.update(p);
            }else if("entry".equals(kind)) {
                LotteryEntry e=entry(user,id);if(!editable(e))throw error("记录已锁定，不能修改；仍可删除");
                if(!e.getGame().equals(r.game())||e.getPlanId()!=null&&!Objects.equals(e.getIssue(),r.issue()==null||r.issue().isBlank()?null:r.issue()))throw error("彩种及定投已绑定期号不可修改");
                cancelEntry(e);if(e.getPlanId()==null)e.setIssue(issue(r.game(),r.issue()));e.setRed(red(r.game(),r.red()));e.setBlue(blue(r.game(),r.blue()));e.setMultiple(r.multiple());e.setAdditional(r.additional()?1:0);e.setPurchasedOn(r.purchasedOn());e.setNote(r.note()==null?"":r.note());e.setCostCents(cost(r.game(),r.multiple(),r.additional()));e.setRevision(e.getRevision()+1);e.setResult("WAITING");
                db.clearSettlement(e.getId());e.setPrizeCents(null);e.setGrade(null);e.setDrawId(null);e.setDrawVersion(null);e.setResultHash(null);db.update(e);
            }else throw error("编辑类型无效");
            return id;
        });
    }
    public boolean editable(LotteryEntry e) {
        return e.getAcceptedAt()==null&&!(e.getPlanId()!=null&&"SETTLED".equals(e.getResult()))&&
                db.blockingDeliveries(e.getId())==0;
    }
    @Transactional
    public String action(String kind,long id,String action,LotteryActionRequest r) {
        long user=CurrentUser.id();require(user,true);
        return write(user,r.idempotencyKey(),"action:"+kind+":"+id+":"+action+":"+r,()->{
            if("plan".equals(kind)) {
                LotteryPlan p=plan(user,id);
                if("status".equals(action)) {
                    if(r.status()==null||!Set.of("ACTIVE","PAUSED","ENDED").contains(r.status()))throw error("计划状态无效");
                    if("ENDED".equals(p.getStatus())&&!"ENDED".equals(r.status()))throw error("已结束计划不可恢复，请创建新计划");
                    p.setStatus(r.status());db.update(p);
                }else if("delete".equals(action)) {
                    for(LotteryEntry e:db.list(LotteryEntry.class,q->q.eq("user_id",user).eq("plan_id",id))) {cancelEntry(e);db.softDelete(LotteryEntry.class,e.getId(),user,CurrentUser.optionalName());}
                    for(LotteryBatch b:db.list(LotteryBatch.class,q->q.eq("user_id",user).eq("plan_id",id))) {cancelBatch(b.getId());db.softDelete(LotteryBatch.class,b.getId(),user,CurrentUser.optionalName());}
                    db.softDelete(LotteryPlan.class,id,user,CurrentUser.optionalName());
                }else throw error("操作无效");
            }else if("entry".equals(kind)&&"delete".equals(action)) {
                LotteryEntry e=entry(user,id);if(e.getPlanId()!=null)throw error("定投逐期快照请通过删除整个计划移除，以保持已购期数完整");
                cancelEntry(e);db.softDelete(LotteryEntry.class,id,user,CurrentUser.optionalName());
            }else if("delivery".equals(kind)&&"retry".equals(action)) {
                LotteryDelivery d=db.get(LotteryDelivery.class,id);if(d==null||d.getUserId()!=user)throw error("投递记录不存在");
                LotteryEvent event=db.get(LotteryEvent.class,d.getEventId());
                if(event==null||event.getCancelled()==1||!Set.of("FAILED","UNKNOWN").contains(d.getStatus()))throw error("此通知不能重试");
                d.setStatus("PENDING");d.setAttempts(0);d.setNextAttemptAt(now());db.update(d);
            }else throw error("操作无效");
            return id;
        });
    }
    private String write(long user,String key,String body,Supplier<Long> operation) {
        if(key==null||!key.matches("[A-Za-z0-9_-]{8,64}"))throw error("幂等键无效");
        String hash=hash(body),old=db.requestHash(user,key);
        if(old!=null){if(!old.equals(hash))throw error("幂等键已用于不同请求");publisher.publishEvent(new Changed(user));return String.valueOf(db.requestResult(user,key));}
        long id=operation.get();db.request(user,key,hash,id);publisher.publishEvent(new Changed(user));return String.valueOf(id);
    }
    void cancelEntrySystem(LotteryEntry e) {for(LotteryEvent event:db.list(LotteryEvent.class,q->q.eq("entry_id",e.getId()).eq("cancelled",0)))cancel(event,true);}
    void cancelEntry(LotteryEntry e) {for(LotteryEvent event:db.list(LotteryEvent.class,q->q.eq("entry_id",e.getId()).eq("cancelled",0)))cancel(event);}
    void cancelBatch(long id) {for(LotteryEvent event:db.list(LotteryEvent.class,q->q.eq("batch_id",id).eq("cancelled",0)))cancel(event);}
    void cancel(LotteryEvent event) {cancel(event,false);}
    private void cancel(LotteryEvent event,boolean system) {event.setCancelled(1);if(system)db.updateSystem(event);else db.update(event);for(LotteryDelivery d:db.list(LotteryDelivery.class,q->q.eq("event_id",event.getId()).in("status","PENDING","FAILED","UNKNOWN"))){d.setStatus("CANCELLED");if(system)db.updateSystem(d);else db.update(d);}}
    public JsonNode view(Object value) {
        JsonNode node=json.valueToTree(value);sanitize(node);return node;
    }
    private void sanitize(JsonNode node) {
        if(node instanceof ObjectNode obj) {
            List<String> fields=new ArrayList<>();obj.fieldNames().forEachRemaining(fields::add);
            for(String key:fields) {
                JsonNode v=obj.get(key);
                if(Set.of("createdBy","createdName","updatedBy","updateName","deletedAt","deletedBy","deletedName","deleted","userId","sourceHash","leaseToken").contains(key)){obj.remove(key);continue;}
                if(v.isIntegralNumber()&&(key.equals("id")||key.endsWith("Id")||key.endsWith("Cents")))obj.put(key,v.asText());else sanitize(v);
            }
        }else if(node.isArray())node.forEach(this::sanitize);
    }
    private void page(int page) {if(page<1||page>5000)throw error("页码无效");}
    private String paging(int page) {return "LIMIT 21 OFFSET "+((page-1)*20);}
}
