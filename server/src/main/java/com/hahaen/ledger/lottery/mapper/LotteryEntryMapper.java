package com.hahaen.ledger.lottery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hahaen.ledger.lottery.entity.LotteryEntry;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LotteryEntryMapper extends BaseMapper<LotteryEntry> {}
