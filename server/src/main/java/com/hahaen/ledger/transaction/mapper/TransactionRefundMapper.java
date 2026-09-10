package com.hahaen.ledger.transaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hahaen.ledger.transaction.entity.TransactionRefund;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TransactionRefundMapper extends BaseMapper<TransactionRefund> {
    @Select("""
            SELECT * FROM transaction_refund
             WHERE transaction_id = #{transactionId} AND deleted = 0
             ORDER BY created_at ASC, id ASC
            """)
    List<TransactionRefund> selectActiveByTransaction(@Param("transactionId") long transactionId);

    @Select("""
            SELECT COALESCE(SUM(refund_amount), 0) FROM transaction_refund
             WHERE transaction_id = #{transactionId} AND deleted = 0
            """)
    long sumActiveAmount(@Param("transactionId") long transactionId);

    @Select("""
            SELECT * FROM transaction_refund
             WHERE transaction_id = #{transactionId}
               AND idempotency_key = #{key}
             LIMIT 1
            """)
    TransactionRefund selectByIdempotency(@Param("transactionId") long transactionId,
                                          @Param("key") String key);

    @Select("""
            SELECT * FROM transaction_refund
             WHERE id = #{id} AND deleted = 0
            """)
    TransactionRefund selectActiveById(@Param("id") long id);

    @Select("SELECT * FROM transaction_refund WHERE id = #{id} AND deleted = 0 FOR UPDATE")
    TransactionRefund selectActiveByIdForUpdate(@Param("id") long id);
}
