package com.hahaen.ledger.lottery.service;

import com.hahaen.ledger.lottery.mapper.LotteryStore;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.*;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name="lottery.worker.enabled",havingValue="true",matchIfMissing=true)
public class LotteryWorker {
    private static final Logger log=LoggerFactory.getLogger(LotteryWorker.class);
    private final LotteryStore db;
    private final LotterySyncService sync;
    private final LotteryProcessor processor;
    private final LotteryDeliveryService deliveries;
    @Scheduled(fixedDelayString="${lottery.sync.delay-ms:300000}",initialDelayString="${lottery.sync.initial-delay-ms:20000}")
    public void synchronize() {
        for(String game:new String[]{"DLT","SSQ"})try{sync.sync(game);}catch(RuntimeException ex){log.error("彩票官方同步失败，彩种={}，类别={}",game,ex.getClass().getSimpleName());}
    }
    @Scheduled(fixedDelay=60000,initialDelay=30000)
    public void process() {
        for(long user:db.owners())try{processor.process(user);}catch(RuntimeException ex){log.error("彩票结算失败，用户ID={}",user,ex);}
        try{deliveries.tick();}catch(RuntimeException ex){log.error("彩票投递处理失败，类别={}",ex.getClass().getSimpleName());}
    }
}
