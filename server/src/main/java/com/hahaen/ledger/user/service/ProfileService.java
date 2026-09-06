package com.hahaen.ledger.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hahaen.ledger.common.exception.BusinessException;
import com.hahaen.ledger.common.security.CurrentUser;
import com.hahaen.ledger.user.entity.AppUser;
import com.hahaen.ledger.user.mapper.AppUserMapper;
import com.hahaen.ledger.user.vo.ProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final AppUserMapper userMapper;

    public ProfileVO currentProfile() {
        AppUser user = userMapper.selectOne(new LambdaQueryWrapper<AppUser>()
                .eq(AppUser::getId, CurrentUser.id())
                .eq(AppUser::getStatus, "ACTIVE")
                .eq(AppUser::getDeleted, 0));
        if (user == null) {
            throw new BusinessException("USER_NOT_FOUND", "用户不存在或已停用");
        }

        LocalDateTime createdAt = user.getCreatedAt();
        LocalDate createdDate = createdAt == null ? LocalDate.now() : createdAt.toLocalDate();
        return new ProfileVO(
                user.getId(),
                user.getNickname(),
                createdAt,
                calculateCumulativeDays(createdDate, LocalDate.now()),
                user.getAvatarFileId() != null,
                user.getAvatarFileId());
    }

    static long calculateCumulativeDays(LocalDate createdDate, LocalDate today) {
        if (createdDate == null || today == null) return 1;
        return Math.max(1, ChronoUnit.DAYS.between(createdDate, today) + 1);
    }
}
