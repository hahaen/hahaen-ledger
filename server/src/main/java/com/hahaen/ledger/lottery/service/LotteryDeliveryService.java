package com.hahaen.ledger.lottery.service;

import com.hahaen.ledger.lottery.entity.*;
import com.hahaen.ledger.lottery.mapper.LotteryStore;
import com.hahaen.ledger.common.security.AuditIdentity;
import com.hahaen.ledger.user.service.NotificationKeyCipher;
import com.hahaen.ledger.todo.service.TodoNotificationSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import static com.hahaen.ledger.lottery.service.LotteryRules.*;

@Service
@RequiredArgsConstructor
public class LotteryDeliveryService {
    private final LotteryStore db;
    private final LotteryService service;
    private final TransactionTemplate tx;
    private final NotificationKeyCipher cipher;
    private final TodoNotificationSender sender;
    private record Claim(LotteryDelivery delivery,LotteryEvent event,long attemptId,String encryptedKey) {}
    public void tick() {
        // 进程崩溃后的发送结果未知，不当作明确失败，不自动解锁购买信息。
        for(var stale:db.list(LotteryDelivery.class,q->q.eq("status","SENDING").lt("locked_until",now()).last("LIMIT 100"))) {
            tx.executeWithoutResult(s->{db.user(stale.getUserId(),true);LotteryDelivery d=db.get(LotteryDelivery.class,stale.getId());
                if(d!=null&&"SENDING".equals(d.getStatus())&&d.getLockedUntil().isBefore(now())){d.setStatus("UNKNOWN");system(d);db.updateSystem(d);
                    for(var a:db.list(LotteryAttempt.class,q->q.eq("delivery_id",d.getId()).eq("result","SENDING"))){a.setResult("UNKNOWN");system(a);db.updateSystem(a);}}});
        }
        for(var candidate:db.list(LotteryDelivery.class,q->q.eq("status","PENDING").le("next_attempt_at",now()).orderByAsc("next_attempt_at").last("LIMIT 100"))) {
            Claim claim=tx.execute(s->claim(candidate.getId()));if(claim==null)continue;
            String result;
            String key;
            try {key=cipher.decrypt(claim.encryptedKey());}
            catch(RuntimeException ex){tx.executeWithoutResult(s->finish(claim,"FAILED"));continue;}
            try {result=sender.send(claim.delivery().getChannel(),key,claim.event().getTitle(),claim.event().getBody(),"彩票")?"ACCEPTED":"FAILED";}
            catch(org.springframework.web.client.ResourceAccessException ex){result="UNKNOWN";}
            catch(RuntimeException ex){result="UNKNOWN";}
            final String outcome=result;
            tx.executeWithoutResult(s->finish(claim,outcome));
        }
    }
    private Claim claim(long id) {
        LotteryDelivery candidate=db.get(LotteryDelivery.class,id);if(candidate==null)return null;
        db.user(candidate.getUserId(),true);LotteryDelivery d=db.get(LotteryDelivery.class,id);
        if(d==null||!"PENDING".equals(d.getStatus())||d.getNextAttemptAt().isAfter(now()))return null;
        LotteryEvent e=db.get(LotteryEvent.class,d.getEventId());
        if(e==null||e.getCancelled()==1){d.setStatus("CANCELLED");system(d);db.updateSystem(d);return null;}
        if(!service.enabled(d.getUserId())) {d.setNextAttemptAt(now().plusMinutes(10));system(d);db.updateSystem(d);return null;}
        if(e.getBatchId()!=null) {
            LotteryBatch b=db.get(LotteryBatch.class,e.getBatchId());LotteryPlan p=b==null?null:db.get(LotteryPlan.class,b.getPlanId());
            if(p==null){d.setStatus("CANCELLED");db.updateSystem(d);return null;}
            if("EXPIRY".equals(e.getKind())&&!"ACTIVE".equals(p.getStatus())){d.setNextAttemptAt(now().plusMinutes(10));system(d);db.updateSystem(d);return null;}
        }
        if(e.getEntryId()!=null&&db.get(LotteryEntry.class,e.getEntryId())==null){d.setStatus("CANCELLED");db.updateSystem(d);return null;}
        var config=service.channels(d.getUserId()).stream().filter(c->c.getNotificationType().equals(d.getChannel())).findFirst().orElse(null);
        if(config==null){d.setNextAttemptAt(now().plusMinutes(10));system(d);db.updateSystem(d);return null;}
        d.setStatus("SENDING");d.setAttempts(d.getAttempts()+1);d.setLockedUntil(now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS).plusMinutes(2));system(d);db.updateSystem(d);
        LotteryAttempt a=new LotteryAttempt();a.setDeliveryId(id);a.setUserId(d.getUserId());a.setResult("SENDING");a.setAttemptedAt(now());a.setCreatedBy(AuditIdentity.SYSTEM_USER_ID);db.insert(a);
        return new Claim(d,e,a.getId(),config.getNotificationKey());
    }
    private void finish(Claim claim,String outcome) {
        db.user(claim.delivery().getUserId(),true);LotteryDelivery d=db.get(LotteryDelivery.class,claim.delivery().getId());
        LotteryAttempt a=db.get(LotteryAttempt.class,claim.attemptId());a.setResult(outcome);system(a);db.updateSystem(a);
        // 迟到的明确受理仍冻结原购买信息，但旧发送者不得覆盖新租约的投递结果。
        if("ACCEPTED".equals(outcome)&&claim.event().getEntryId()!=null) {
            LotteryEntry e=db.get(LotteryEntry.class,claim.event().getEntryId());
            if(e!=null&&e.getAcceptedAt()==null){e.setAcceptedAt(now());system(e);db.updateSystem(e);}
        }
        if(d==null||!"SENDING".equals(d.getStatus())||!java.util.Objects.equals(d.getLockedUntil(),claim.delivery().getLockedUntil()))return;
        d.setStatus(outcome);
        if("ACCEPTED".equals(outcome)) {
            d.setAcceptedAt(now());
        }else if("FAILED".equals(outcome)&&d.getAttempts()<5){d.setStatus("PENDING");d.setNextAttemptAt(now().plusMinutes(1L<<Math.min(d.getAttempts(),5)));}
        system(d);db.updateSystem(d);
    }
    private void system(com.hahaen.ledger.common.entity.BaseAuditEntity entity){entity.setUpdatedBy(AuditIdentity.SYSTEM_USER_ID);entity.setUpdateName("系统");}
}
