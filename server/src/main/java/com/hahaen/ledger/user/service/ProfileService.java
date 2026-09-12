package com.hahaen.ledger.user.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hahaen.ledger.auth.service.PasswordCryptoService;
import com.hahaen.ledger.common.exception.BusinessException;
import com.hahaen.ledger.common.security.CurrentUser;
import com.hahaen.ledger.file.entity.AppFile;
import com.hahaen.ledger.file.mapper.AppFileMapper;
import com.hahaen.ledger.transaction.mapper.TransactionDetailMapper;
import com.hahaen.ledger.user.dto.ProfilePasswordUpdateRequest;
import com.hahaen.ledger.user.dto.ProfileUpdateRequest;
import com.hahaen.ledger.user.entity.AppUser;
import com.hahaen.ledger.user.mapper.AppUserMapper;
import com.hahaen.ledger.user.vo.ProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private static final Pattern ACCOUNT_PATTERN = Pattern.compile("[a-z0-9]{2,64}");
    private static final int MAX_PASSWORD_BYTES = 72;

    private final AppUserMapper userMapper;
    private final AppFileMapper fileMapper;
    private final TransactionDetailMapper transactionMapper;
    private final PasswordEncoder passwordEncoder;
    private final PasswordCryptoService passwordCryptoService;

    public ProfileVO currentProfile() {
        AppUser user = currentActiveUser();
        return toProfileVO(user);
    }

    @Transactional
    public ProfileVO updateProfile(ProfileUpdateRequest request) {
        AppUser user = currentActiveUser();
        String nickname = normalizedNickname(request.nickname());
        String account = normalizeAccount(request.loginAccount());
        if (hasText(user.getLoginAccount())) {
            if (!user.getLoginAccount().equals(account)) {
                throw new BusinessException("ACCOUNT_IMMUTABLE", "账号已设置，不能修改");
            }
        } else {
            assertAccountAvailable(account, user.getId());
            user.setLoginAccount(account);
        }
        if (!hasText(user.getPasswordHash())) {
            if (!hasText(request.encryptedPassword())) {
                throw new BusinessException("SET_PASSWORD_FIRST", "请先设置密码");
            }
            String password = validPassword(passwordCryptoService.decrypt(request.encryptedPassword()));
            user.setPasswordHash(passwordEncoder.encode(password));
        }
        if (hasText(request.avatarFileId())) {
            user.setAvatarFileUrl(readyOwnedAvatar(request.avatarFileId(), user.getId()).getObjectKey());
        }
        user.setNickname(nickname);
        saveUser(user);
        if (StpUtil.isLogin()) StpUtil.getSession().set("auditName", nickname);
        return toProfileVO(user);
    }

    @Transactional
    public void updatePassword(ProfilePasswordUpdateRequest request) {
        AppUser user = currentActiveUser();
        if (!hasText(user.getLoginAccount())) {
            String account = normalizeAccount(request.loginAccount());
            assertAccountAvailable(account, user.getId());
            user.setLoginAccount(account);
        } else if (hasText(request.loginAccount()) && !user.getLoginAccount().equals(normalizeAccount(request.loginAccount()))) {
            throw new BusinessException("ACCOUNT_IMMUTABLE", "账号已设置，不能修改");
        }
        String password = validPassword(passwordCryptoService.decrypt(request.encryptedPassword()));
        user.setPasswordHash(passwordEncoder.encode(password));
        saveUser(user);
    }

    private AppUser currentActiveUser() {
        long userId = CurrentUser.id();
        AppUser user = userMapper.selectOne(new LambdaQueryWrapper<AppUser>()
                .eq(AppUser::getId, userId)
                .eq(AppUser::getStatus, "ACTIVE")
                .eq(AppUser::getDeleted, 0));
        if (user == null) {
            throw new BusinessException("USER_NOT_FOUND", "用户不存在或已停用");
        }
        return user;
    }

    private AppFile readyOwnedAvatar(String fileId, long userId) {
        final long id;
        try {
            id = Long.parseLong(fileId);
        } catch (NumberFormatException ex) {
            throw new BusinessException("AVATAR_FILE_INVALID", "头像文件参数无效");
        }
        AppFile file = fileMapper.selectOne(new LambdaQueryWrapper<AppFile>()
                .eq(AppFile::getId, id)
                .eq(AppFile::getUserId, userId)
                .eq(AppFile::getBusinessType, "AVATAR")
                .eq(AppFile::getStatus, "READY")
                .eq(AppFile::getDeleted, 0));
        if (file == null) throw new BusinessException("AVATAR_FILE_INVALID", "头像文件不存在或尚未上传完成");
        return file;
    }

    private ProfileVO toProfileVO(AppUser user) {
        LocalDateTime createdAt = user.getCreatedAt();
        LocalDateTime earliestOccurredAt = transactionMapper.selectEarliestActiveOccurredAt(user.getId());
        return new ProfileVO(
                String.valueOf(user.getId()),
                user.getNickname(),
                user.getLoginAccount(),
                hasText(user.getPasswordHash()),
                createdAt,
                calculateCumulativeDays(earliestOccurredAt == null ? null : earliestOccurredAt.toLocalDate(), LocalDate.now()),
                hasText(user.getAvatarFileUrl()),
                user.getAvatarFileUrl());
    }

    private void assertAccountAvailable(String account, long currentUserId) {
        if (userMapper.selectCount(new LambdaQueryWrapper<AppUser>()
                .eq(AppUser::getLoginAccount, account)
                .ne(AppUser::getId, currentUserId)) > 0) {
            throw new BusinessException("ACCOUNT_EXISTS", "账号已存在，请更换后重试");
        }
    }

    private void saveUser(AppUser user) {
        try {
            if (userMapper.updateById(user) != 1) {
                throw new BusinessException("USER_UPDATE_FAILED", "资料保存失败，请重试");
            }
        } catch (DuplicateKeyException ex) {
            throw new BusinessException("ACCOUNT_EXISTS", "账号已存在，请更换后重试");
        }
    }

    private static String normalizedNickname(String value) {
        String nickname = value == null ? "" : value.trim();
        if (nickname.isEmpty() || nickname.length() > 40) {
            throw new BusinessException("NICKNAME_INVALID", "昵称需为 1-40 个字符");
        }
        return nickname;
    }

    private static String normalizeAccount(String value) {
        String account = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        if (!ACCOUNT_PATTERN.matcher(account).matches()) {
            throw new BusinessException("ACCOUNT_INVALID", "账号需为 2-64 位英文字母或数字");
        }
        return account;
    }

    private static String validPassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 64
                || password.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            throw new BusinessException("PASSWORD_INVALID", "密码需为 8-64 位字符");
        }
        return password;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    static long calculateCumulativeDays(LocalDate firstRecordDate, LocalDate today) {
        if (firstRecordDate == null || today == null || firstRecordDate.isAfter(today)) return 0;
        return ChronoUnit.DAYS.between(firstRecordDate, today) + 1;
    }
}
