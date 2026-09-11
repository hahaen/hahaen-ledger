package com.hahaen.ledger.transaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hahaen.ledger.transaction.entity.TransactionRefund;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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

    /**
     * 显式执行退款逻辑删除。
     *
     * <p>不能使用 {@link BaseMapper#updateById(Object)} 修改 {@code @TableLogic} 字段：
     * MyBatis-Plus 会将该字段从普通更新集合中排除，导致审计字段已写入而
     * {@code deleted} 仍为 0。条件更新同时防止并发请求重复恢复账户余额。</p>
     */
    @Update("""
            UPDATE transaction_refund
               SET deleted = 1,
                   deleted_at = #{refund.deletedAt},
                   deleted_by = #{refund.deletedBy},
                   deleted_name = #{refund.deletedName},
                   updated_at = #{refund.deletedAt},
                   updated_by = #{refund.deletedBy},
                   update_name = #{refund.deletedName}
             WHERE id = #{refund.id} AND deleted = 0
            """)
    int softDeleteById(@Param("refund") TransactionRefund refund);
}
