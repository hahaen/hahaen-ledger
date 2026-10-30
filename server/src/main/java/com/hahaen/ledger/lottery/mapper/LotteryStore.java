package com.hahaen.ledger.lottery.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hahaen.ledger.common.entity.BaseAuditEntity;
import com.hahaen.ledger.lottery.entity.*;
import com.hahaen.ledger.user.entity.AppUser;
import com.hahaen.ledger.user.mapper.AppUserMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.*;
import java.util.function.Consumer;

/** 数据访问集中在此处；类型注册表限制动态表名，业务值全部绑定参数。 */
@Repository
public class LotteryStore {
    private final Map<Class<?>, BaseMapper<?>> mappers;
    private final JdbcTemplate jdbc;
    private final AppUserMapper users;
    public LotteryStore(LotteryDrawMapper draw, LotteryDrawRevisionMapper revision, LotterySyncStateMapper sync,
                        LotteryPlanMapper plan, LotteryBatchMapper batch, LotteryEntryMapper entry,
                        LotteryEventMapper event, LotteryDeliveryMapper delivery, LotteryAttemptMapper attempt,
                        AppUserMapper users, JdbcTemplate jdbc) {
        this.mappers = Map.of(LotteryDraw.class,draw,LotteryDrawRevision.class,revision,LotterySyncState.class,sync,
                LotteryPlan.class,plan,LotteryBatch.class,batch,LotteryEntry.class,entry,LotteryEvent.class,event,
                LotteryDelivery.class,delivery,LotteryAttempt.class,attempt);
        this.jdbc=jdbc; this.users=users;
    }
    @SuppressWarnings("unchecked")
    private <T extends BaseAuditEntity> BaseMapper<T> mapper(Class<T> type) {
        BaseMapper<?> mapper=mappers.get(type);
        if(mapper==null) throw new IllegalArgumentException("Unknown lottery entity");
        return (BaseMapper<T>)mapper;
    }
    public <T extends BaseAuditEntity> List<T> list(Class<T> type, Consumer<QueryWrapper<T>> criteria) {
        QueryWrapper<T> q=new QueryWrapper<>(); criteria.accept(q); return mapper(type).selectList(q);
    }
    public <T extends BaseAuditEntity> T one(Class<T> type, Consumer<QueryWrapper<T>> criteria) {
        QueryWrapper<T> q=new QueryWrapper<>(); criteria.accept(q); q.last("LIMIT 1"); return mapper(type).selectOne(q);
    }
    public <T extends BaseAuditEntity> long count(Class<T> type, Consumer<QueryWrapper<T>> criteria) {
        QueryWrapper<T> q=new QueryWrapper<>(); criteria.accept(q); return mapper(type).selectCount(q);
    }
    public <T extends BaseAuditEntity> T get(Class<T> type,long id) { return mapper(type).selectById(id); }
    @SuppressWarnings("unchecked")
    public <T extends BaseAuditEntity> void insert(T value) { mapper((Class<T>)value.getClass()).insert(value); }
    @SuppressWarnings("unchecked")
    public <T extends BaseAuditEntity> void update(T value) {
        Long actor=com.hahaen.ledger.common.security.CurrentUser.optionalId();
        value.setUpdatedAt(com.hahaen.ledger.lottery.service.LotteryRules.now());
        value.setUpdatedBy(actor==null?com.hahaen.ledger.common.security.AuditIdentity.SYSTEM_USER_ID:actor);
        value.setUpdateName(actor==null?"系统":com.hahaen.ledger.common.security.CurrentUser.optionalName());
        mapper((Class<T>)value.getClass()).updateById(value);
    }
    @SuppressWarnings("unchecked")
    public <T extends BaseAuditEntity> void updateSystem(T value) {
        value.setUpdatedAt(com.hahaen.ledger.lottery.service.LotteryRules.now());
        value.setUpdatedBy(com.hahaen.ledger.common.security.AuditIdentity.SYSTEM_USER_ID);value.setUpdateName("系统");
        mapper((Class<T>)value.getClass()).updateById(value);
    }
    public AppUser user(long id, boolean lock) {
        if(lock) jdbc.queryForList("SELECT id FROM app_user WHERE id=? FOR UPDATE",id);
        return users.selectById(id);
    }
    public List<Long> owners() {
        return jdbc.queryForList("SELECT DISTINCT u.id FROM app_user u JOIN lottery_entry e ON e.user_id=u.id WHERE u.deleted=0 AND u.status='ACTIVE' AND u.lottery_enabled=1 AND e.deleted=0 ORDER BY u.id",Long.class);
    }
    public String requestHash(long user,String key) {
        return jdbc.query("SELECT request_hash FROM lottery_request WHERE user_id=? AND idempotency_key=?",r->r.next()?r.getString(1):null,user,key);
    }
    public Long requestResult(long user,String key) {
        return jdbc.queryForObject("SELECT result_id FROM lottery_request WHERE user_id=? AND idempotency_key=?",Long.class,user,key);
    }
    public void request(long user,String key,String hash,long result) {
        jdbc.update("INSERT INTO lottery_request(user_id,idempotency_key,request_hash,result_id) VALUES(?,?,?,?)",user,key,hash,result);
    }
    public void softDelete(Class<? extends BaseAuditEntity> type,long id,long actor,String name) {
        if(!mappers.containsKey(type)) throw new IllegalArgumentException("Unknown lottery entity");
        String table=type.getAnnotation(TableName.class).value();
        jdbc.update("UPDATE "+table+" SET deleted=1,deleted_at=CURRENT_TIMESTAMP(3),deleted_by=?,deleted_name=?,updated_at=CURRENT_TIMESTAMP(3),updated_by=?,update_name=? WHERE id=? AND deleted=0",actor,name,actor,name,id);
    }
    public Map<String,Object> summary(long user,String game,Long plan) {
        String suffix=plan==null?"":" AND plan_id=?";
        Object[] args=plan==null?new Object[]{user,game}:new Object[]{user,game,plan};
        return jdbc.queryForMap("SELECT COALESCE(SUM(cost_cents),0) AS costCents,COALESCE(SUM(IF(result='SETTLED',cost_cents,0)),0) AS settledCostCents,COALESCE(SUM(IF(result='WAITING',cost_cents,0)),0) AS waitingCostCents,COALESCE(SUM(IF(result='SETTLED',prize_cents,0)),0) AS prizeCents,COALESCE(SUM(IF(result='SETTLED',prize_cents-cost_cents,0)),0) AS profitCents,COALESCE(SUM(result<>'SETTLED'),0) AS pendingCount,COUNT(*) AS entryCount FROM lottery_entry WHERE user_id=? AND game=? AND deleted=0"+suffix,args);
    }
    public record DrawSummary(String costCents, String prizeCents, long entryCount, long pendingCount) {}

    /** 当前页个人购买汇总；未核实或官方修订尚未重算时不输出不完整奖金。 */
    public Map<String,DrawSummary> drawSummaries(long user,String game,List<String> issues) {
        if(issues.isEmpty()) return Map.of();
        String settled="e.result='SETTLED' AND e.prize_cents IS NOT NULL AND e.draw_id=d.id AND e.draw_version=d.version";
        String placeholders=String.join(",",Collections.nCopies(issues.size(),"?"));
        List<Object> args=new ArrayList<>();args.add(user);args.add(game);args.addAll(issues);
        return jdbc.query("SELECT e.issue,SUM(e.cost_cents) AS cost_cents,SUM(IF("+settled+",e.prize_cents,0)) AS prize_cents,COUNT(*) AS entry_count,SUM(IF("+settled+",0,1)) AS pending_count FROM lottery_entry e LEFT JOIN lottery_draw d ON d.game=e.game AND d.issue=e.issue AND d.deleted=0 WHERE e.user_id=? AND e.game=? AND e.deleted=0 AND e.issue IN ("+placeholders+") GROUP BY e.issue",rs->{
            Map<String,DrawSummary> summaries=new HashMap<>();
            while(rs.next()) {
                long pending=rs.getLong("pending_count");
                summaries.put(rs.getString("issue"),new DrawSummary(rs.getString("cost_cents"),pending==0?rs.getString("prize_cents"):null,rs.getLong("entry_count"),pending));
            }
            return summaries;
        },args.toArray());
    }
    public List<LotteryEntry> unsettled(long user) {
        return jdbc.query("SELECT e.* FROM lottery_entry e JOIN lottery_draw d ON d.game=e.game AND d.issue=e.issue AND d.deleted=0 WHERE e.user_id=? AND e.deleted=0 AND (e.draw_version IS NULL OR e.draw_version<>d.version) ORDER BY e.id LIMIT 500",new org.springframework.jdbc.core.BeanPropertyRowMapper<>(LotteryEntry.class),user);
    }
    public long acceptedBatch(long batch) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM lottery_delivery d JOIN lottery_event e ON e.id=d.event_id WHERE e.batch_id=? AND e.kind='EXPIRY' AND d.status='ACCEPTED' AND d.deleted=0",Long.class,batch);
    }
    public long unsettledDeliveries(long entry) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM lottery_delivery d JOIN lottery_event e ON e.id=d.event_id WHERE e.entry_id=? AND d.deleted=0 AND d.status IN ('SENDING','UNKNOWN')",Long.class,entry);
    }
    public long blockingDeliveries(long entry) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM lottery_delivery d JOIN lottery_event e ON e.id=d.event_id WHERE e.entry_id=? AND d.deleted=0 AND (d.status IN ('SENDING','UNKNOWN') OR EXISTS(SELECT 1 FROM lottery_attempt a WHERE a.delivery_id=d.id AND a.result='UNKNOWN' AND a.deleted=0))",Long.class,entry);
    }
    public void clearSettlement(long entry) {
        jdbc.update("UPDATE lottery_entry SET prize_cents=NULL,grade=NULL,draw_id=NULL,draw_version=NULL,result_hash=NULL WHERE id=? AND deleted=0",entry);
    }
    /** 同步租约有持有者令牌，过期持有者不能释放新租约。 */
    public boolean claimSync(String game,String token,int seconds) {
        return jdbc.update("UPDATE lottery_sync_state SET lease_token=?,locked_until=DATE_ADD(CURRENT_TIMESTAMP(3),INTERVAL ? SECOND),updated_at=CURRENT_TIMESTAMP(3) WHERE game=? AND deleted=0 AND (locked_until IS NULL OR locked_until<CURRENT_TIMESTAMP(3))",token,seconds,game)==1;
    }
    public boolean lockSyncPage(String game,String token) {
        return !jdbc.queryForList("SELECT id FROM lottery_sync_state WHERE game=? AND lease_token=? AND locked_until>CURRENT_TIMESTAMP(3) AND deleted=0 FOR UPDATE",Long.class,game,token).isEmpty();
    }
    /** CAS只写进度，不把旧实体中的租约写回，过期持有者不得覆盖新持有者。 */
    public boolean saveSyncProgress(LotterySyncState state,String token) {
        return jdbc.update("UPDATE lottery_sync_state SET catchup_page=?,catchup_issue=?,history_page=?,history_done=?,last_success_at=?,last_error=?,updated_at=CURRENT_TIMESTAMP(3),updated_by=?,update_name='系统' WHERE game=? AND lease_token=? AND locked_until>CURRENT_TIMESTAMP(3) AND deleted=0",
                state.getCatchupPage(),state.getCatchupIssue(),state.getHistoryPage(),state.getHistoryDone(),state.getLastSuccessAt(),state.getLastError(),com.hahaen.ledger.common.security.AuditIdentity.SYSTEM_USER_ID,state.getGame(),token)==1;
    }
    public void releaseSync(String game,String token) {
        jdbc.update("UPDATE lottery_sync_state SET locked_until=NULL,lease_token=NULL WHERE game=? AND lease_token=?",game,token);
    }
    public boolean hasSyncLease(String game,String token) {
        return Boolean.TRUE.equals(jdbc.queryForObject("SELECT COUNT(*)=1 FROM lottery_sync_state WHERE game=? AND lease_token=? AND locked_until>CURRENT_TIMESTAMP(3)",Boolean.class,game,token));
    }
}
