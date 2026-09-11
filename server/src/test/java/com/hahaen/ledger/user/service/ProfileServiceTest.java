package com.hahaen.ledger.user.service;

import com.hahaen.ledger.auth.service.PasswordCryptoService;
import com.hahaen.ledger.common.exception.BusinessException;
import com.hahaen.ledger.common.security.CurrentUser;
import cn.dev33.satoken.stp.StpUtil;
import com.hahaen.ledger.file.entity.AppFile;
import com.hahaen.ledger.file.mapper.AppFileMapper;
import com.hahaen.ledger.transaction.mapper.TransactionDetailMapper;
import com.hahaen.ledger.user.dto.ProfilePasswordUpdateRequest;
import com.hahaen.ledger.user.dto.ProfileUpdateRequest;
import com.hahaen.ledger.user.entity.AppUser;
import com.hahaen.ledger.user.mapper.AppUserMapper;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

class ProfileServiceTest {
    @Test
    void countsFirstRecordDayAsTheFirstCumulativeDay() {
        LocalDate today = LocalDate.of(2026, 9, 6);

        assertEquals(1, ProfileService.calculateCumulativeDays(today, today));
        assertEquals(2, ProfileService.calculateCumulativeDays(today.minusDays(1), today));
        assertEquals(0, ProfileService.calculateCumulativeDays(today.plusDays(1), today));
        assertEquals(0, ProfileService.calculateCumulativeDays(null, today));
    }

    @Test
    void currentProfileUsesTheEarliestActiveTransactionInsteadOfUserCreationTime() {
        AppUserMapper users = mock(AppUserMapper.class);
        TransactionDetailMapper transactions = mock(TransactionDetailMapper.class);
        AppUser user = activeUser();
        when(users.selectOne(any())).thenReturn(user);
        LocalDate firstRecordDate = LocalDate.now().minusDays(3);
        when(transactions.selectEarliestActiveOccurredAt(7L)).thenReturn(firstRecordDate.atTime(8, 30));

        try (MockedStatic<CurrentUser> current = mockStatic(CurrentUser.class)) {
            current.when(CurrentUser::id).thenReturn(7L);
            assertEquals(4, service(users, transactions).currentProfile().cumulativeDays());
        }

        verify(transactions).selectEarliestActiveOccurredAt(7L);
    }

    @Test
    void currentProfileReturnsZeroCumulativeDaysWhenThereAreNoActiveTransactions() {
        AppUserMapper users = mock(AppUserMapper.class);
        TransactionDetailMapper transactions = mock(TransactionDetailMapper.class);
        when(users.selectOne(any())).thenReturn(activeUser());
        when(transactions.selectEarliestActiveOccurredAt(7L)).thenReturn(null);

        try (MockedStatic<CurrentUser> current = mockStatic(CurrentUser.class)) {
            current.when(CurrentUser::id).thenReturn(7L);
            assertEquals(0, service(users, transactions).currentProfile().cumulativeDays());
        }
    }

    @Test
    void rejectsChangingAnExistingLoginAccount() {
        AppUserMapper users = mock(AppUserMapper.class);
        TransactionDetailMapper transactions = mock(TransactionDetailMapper.class);
        AppUser user = activeUser();
        user.setLoginAccount("fixed.account");
        when(users.selectOne(any())).thenReturn(user);

        try (MockedStatic<CurrentUser> current = mockStatic(CurrentUser.class)) {
            current.when(CurrentUser::id).thenReturn(7L);
            BusinessException error = assertThrows(BusinessException.class,
                    () -> service(users, transactions).updateProfile(new ProfileUpdateRequest("新昵称", "other.account", null, null)));
            assertEquals("ACCOUNT_IMMUTABLE", error.getErrorCode());
        }
        verify(users, never()).updateById(any(AppUser.class));
    }

    @Test
    void updatesNicknameForTheCurrentUserWithoutChangingExistingAccount() {
        AppUserMapper users = mock(AppUserMapper.class);
        TransactionDetailMapper transactions = mock(TransactionDetailMapper.class);
        AppUser user = activeUser();
        user.setLoginAccount("fixed.account");
        user.setPasswordHash("existing-hash");
        when(users.selectOne(any())).thenReturn(user);
        when(users.updateById(any(AppUser.class))).thenReturn(1);
        when(transactions.selectEarliestActiveOccurredAt(7L)).thenReturn(null);

        try (MockedStatic<CurrentUser> current = mockStatic(CurrentUser.class);
             MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            current.when(CurrentUser::id).thenReturn(7L);
            stp.when(StpUtil::isLogin).thenReturn(false);
            assertEquals("新昵称", service(users, transactions)
                    .updateProfile(new ProfileUpdateRequest(" 新昵称 ", "fixed.account", null, null)).nickname());
        }

        ArgumentCaptor<AppUser> saved = ArgumentCaptor.forClass(AppUser.class);
        verify(users).updateById(saved.capture());
        assertEquals("新昵称", saved.getValue().getNickname());
        assertEquals("fixed.account", saved.getValue().getLoginAccount());
    }

    @Test
    void firstPasswordUpdateSetsNormalizedUniqueAccountAndHashTogether() {
        AppUserMapper users = mock(AppUserMapper.class);
        TransactionDetailMapper transactions = mock(TransactionDetailMapper.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        PasswordCryptoService crypto = mock(PasswordCryptoService.class);
        AppUser user = activeUser();
        when(users.selectOne(any())).thenReturn(user);
        when(users.selectCount(any())).thenReturn(0L);
        when(users.updateById(any(AppUser.class))).thenReturn(1);
        when(crypto.decrypt("encrypted")).thenReturn("password-8");
        when(encoder.encode("password-8")).thenReturn("hashed-password");

        try (MockedStatic<CurrentUser> current = mockStatic(CurrentUser.class)) {
            current.when(CurrentUser::id).thenReturn(7L);
            new ProfileService(users, mock(AppFileMapper.class), transactions, encoder, crypto)
                    .updatePassword(new ProfilePasswordUpdateRequest("encrypted", " First.User "));
        }

        ArgumentCaptor<AppUser> saved = ArgumentCaptor.forClass(AppUser.class);
        verify(users).updateById(saved.capture());
        assertEquals("first.user", saved.getValue().getLoginAccount());
        assertEquals("hashed-password", saved.getValue().getPasswordHash());
    }

    @Test
    void firstProfileSaveSetsAccountAndPasswordTogether() {
        AppUserMapper users = mock(AppUserMapper.class);
        TransactionDetailMapper transactions = mock(TransactionDetailMapper.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        PasswordCryptoService crypto = mock(PasswordCryptoService.class);
        AppUser user = activeUser();
        when(users.selectOne(any())).thenReturn(user);
        when(users.selectCount(any())).thenReturn(0L);
        when(users.updateById(any(AppUser.class))).thenReturn(1);
        when(crypto.decrypt("encrypted")).thenReturn("password-8");
        when(encoder.encode("password-8")).thenReturn("hashed-password");
        when(transactions.selectEarliestActiveOccurredAt(7L)).thenReturn(null);

        try (MockedStatic<CurrentUser> current = mockStatic(CurrentUser.class);
             MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            current.when(CurrentUser::id).thenReturn(7L);
            stp.when(StpUtil::isLogin).thenReturn(false);
            assertEquals(true, new ProfileService(users, mock(AppFileMapper.class), transactions, encoder, crypto)
                    .updateProfile(new ProfileUpdateRequest(" 新昵称 ", " First.User ", "encrypted", null))
                    .passwordConfigured());
        }

        ArgumentCaptor<AppUser> saved = ArgumentCaptor.forClass(AppUser.class);
        verify(users).updateById(saved.capture());
        assertEquals("first.user", saved.getValue().getLoginAccount());
        assertEquals("hashed-password", saved.getValue().getPasswordHash());
    }

    @Test
    void rejectsDuplicateFirstAccountBeforePasswordUpdate() {
        AppUserMapper users = mock(AppUserMapper.class);
        TransactionDetailMapper transactions = mock(TransactionDetailMapper.class);
        AppUser user = activeUser();
        when(users.selectOne(any())).thenReturn(user);
        when(users.selectCount(any())).thenReturn(1L);

        try (MockedStatic<CurrentUser> current = mockStatic(CurrentUser.class)) {
            current.when(CurrentUser::id).thenReturn(7L);
            BusinessException error = assertThrows(BusinessException.class,
                    () -> service(users, transactions).updatePassword(new ProfilePasswordUpdateRequest("encrypted", "taken.user")));
            assertEquals("ACCOUNT_EXISTS", error.getErrorCode());
        }
        verify(users, never()).updateById(any(AppUser.class));
    }

    @Test
    void savesTheSelectedReadyAvatarOnlyWithProfileSave() {
        AppUserMapper users = mock(AppUserMapper.class);
        AppFileMapper files = mock(AppFileMapper.class);
        TransactionDetailMapper transactions = mock(TransactionDetailMapper.class);
        AppUser user = activeUser();
        user.setLoginAccount("fixed.account");
        user.setPasswordHash("existing-hash");
        AppFile avatar = new AppFile();
        avatar.setId(18L);
        avatar.setUserId(7L);
        avatar.setBusinessType("AVATAR");
        avatar.setStatus("READY");
        avatar.setDeleted(0);
        avatar.setObjectKey("avatars/7/selected.jpg");
        when(users.selectOne(any())).thenReturn(user);
        when(files.selectOne(any())).thenReturn(avatar);
        when(users.updateById(any(AppUser.class))).thenReturn(1);
        when(transactions.selectEarliestActiveOccurredAt(7L)).thenReturn(null);

        try (MockedStatic<CurrentUser> current = mockStatic(CurrentUser.class);
             MockedStatic<StpUtil> stp = mockStatic(StpUtil.class)) {
            current.when(CurrentUser::id).thenReturn(7L);
            stp.when(StpUtil::isLogin).thenReturn(false);
            service(users, files, transactions).updateProfile(
                    new ProfileUpdateRequest("新昵称", "fixed.account", null, "18"));
        }

        ArgumentCaptor<AppUser> saved = ArgumentCaptor.forClass(AppUser.class);
        verify(users).updateById(saved.capture());
        assertEquals("avatars/7/selected.jpg", saved.getValue().getAvatarFileUrl());
    }

    private static ProfileService service(AppUserMapper users, TransactionDetailMapper transactions) {
        return service(users, mock(AppFileMapper.class), transactions);
    }

    private static ProfileService service(AppUserMapper users, AppFileMapper files, TransactionDetailMapper transactions) {
        return new ProfileService(users, files, transactions, mock(PasswordEncoder.class), mock(PasswordCryptoService.class));
    }

    private static AppUser activeUser() {
        AppUser user = new AppUser();
        user.setId(7L);
        user.setNickname("测试用户");
        user.setStatus("ACTIVE");
        user.setDeleted(0);
        user.setCreatedAt(LocalDateTime.of(2026, 1, 1, 0, 0));
        return user;
    }
}
