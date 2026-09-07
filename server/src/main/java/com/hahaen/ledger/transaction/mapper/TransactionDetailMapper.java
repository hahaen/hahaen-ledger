package com.hahaen.ledger.transaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hahaen.ledger.transaction.entity.TransactionDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface TransactionDetailMapper extends BaseMapper<TransactionDetail> {
    @Select("""
            SELECT * FROM transaction_detail
             WHERE id = #{id} AND user_id = #{userId} AND deleted = 0
             FOR UPDATE
            """)
    TransactionDetail selectOwnedForUpdate(@Param("id") long id, @Param("userId") long userId);

    @Select("""
            <script>
            SELECT * FROM transaction_detail
             WHERE user_id = #{userId} AND deleted = 0
            <if test="start != null"> AND occurred_at <![CDATA[>=]]> #{start}</if>
            <if test="end != null"> AND occurred_at <![CDATA[<]]> #{end}</if>
            <if test="date != null"> AND DATE(occurred_at) = #{date}</if>
            <if test="accountId != null">
              AND (account_id = #{accountId} OR from_account_id = #{accountId} OR to_account_id = #{accountId})
            </if>
            <if test="type != null and type != ''"> AND transaction_type = #{type}</if>
             ORDER BY occurred_at DESC, id DESC
             LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<TransactionDetail> selectPageByScope(@Param("userId") long userId,
                                               @Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end,
                                               @Param("date") String date,
                                               @Param("accountId") Long accountId,
                                               @Param("type") String type,
                                               @Param("limit") int limit,
                                               @Param("offset") int offset);

    @Select("""
            <script>
            SELECT COUNT(*) FROM transaction_detail
             WHERE user_id = #{userId} AND deleted = 0
            <if test="start != null"> AND occurred_at <![CDATA[>=]]> #{start}</if>
            <if test="end != null"> AND occurred_at <![CDATA[<]]> #{end}</if>
            <if test="date != null"> AND DATE(occurred_at) = #{date}</if>
            <if test="accountId != null">
              AND (account_id = #{accountId} OR from_account_id = #{accountId} OR to_account_id = #{accountId})
            </if>
            <if test="type != null and type != ''"> AND transaction_type = #{type}</if>
            </script>
            """)
    long countByScope(@Param("userId") long userId,
                      @Param("start") LocalDateTime start,
                      @Param("end") LocalDateTime end,
                      @Param("date") String date,
                      @Param("accountId") Long accountId,
                      @Param("type") String type);

    @Select("""
            SELECT * FROM transaction_detail
             WHERE user_id = #{userId} AND deleted = 0
               AND occurred_at >= #{start} AND occurred_at < #{end}
             ORDER BY occurred_at DESC, id DESC
            """)
    List<TransactionDetail> selectByPeriod(@Param("userId") long userId,
                                           @Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);

    @Select("""
            SELECT * FROM transaction_detail
             WHERE user_id = #{userId} AND idempotency_key = #{key}
             LIMIT 1
            """)
    TransactionDetail selectByIdempotency(@Param("userId") long userId, @Param("key") String key);
}
