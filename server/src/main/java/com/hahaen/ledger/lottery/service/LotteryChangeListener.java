package com.hahaen.ledger.lottery.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.slf4j.*;

/** 购买事务提交后立即结算已有本地开奖；失败由持久化记录及周期任务继续处理。 */
@Component
@RequiredArgsConstructor
public class LotteryChangeListener {
    private static final Logger log=LoggerFactory.getLogger(LotteryChangeListener.class);
    private final LotteryProcessor processor;
    @TransactionalEventListener
    public void changed(LotteryService.Changed event) {
        try {processor.process(event.userId());}
        catch(RuntimeException ex){log.error("彩票保存后结算待后台重试，用户ID={}",event.userId(),ex);}
    }
}
