package com.hahaen.ledger.file.service;

import com.hahaen.ledger.common.security.CurrentUser;
import com.hahaen.ledger.file.dto.FileUploadUrlRequest;
import com.hahaen.ledger.file.entity.AppFile;
import com.hahaen.ledger.file.mapper.AppFileMapper;
import com.hahaen.ledger.file.vo.FileUploadUrlVO;
import com.hahaen.ledger.user.entity.AppUser;
import com.hahaen.ledger.user.mapper.AppUserMapper;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppFileServiceTest {
    @Test
    void reusesReadyAvatarWithTheSameHashInsteadOfCreatingAnotherObject() {
        AppFileMapper files = mock(AppFileMapper.class);
        AppUserMapper users = mock(AppUserMapper.class);
        MinioStorageService storage = mock(MinioStorageService.class);
        AppFile existing = readyAvatar();
        AppUser user = new AppUser();
        user.setId(7L);
        user.setDeleted(0);
        when(files.selectOne(any())).thenReturn(null, existing);
        when(users.selectById(7L)).thenReturn(user);

        try (MockedStatic<CurrentUser> currentUser = mockStatic(CurrentUser.class)) {
            currentUser.when(CurrentUser::id).thenReturn(7L);
            FileUploadUrlVO response = new AppFileService(files, users, storage).createUploadUrl(request());

            assertEquals("18", response.fileId());
            assertEquals("READY", response.status());
            assertEquals("", response.uploadUrl());
        }

        verify(users, never()).updateById(any(AppUser.class));
        verify(files, never()).insert(any(AppFile.class));
    }

    @Test
    void backfillsAndReusesTheCurrentLegacyAvatarWhoseHashIsMissing() throws Exception {
        AppFileMapper files = mock(AppFileMapper.class);
        AppUserMapper users = mock(AppUserMapper.class);
        MinioStorageService storage = mock(MinioStorageService.class);
        byte[] content = "legacy-avatar".getBytes(StandardCharsets.UTF_8);
        String hash = sha256(content);
        AppFile legacy = readyAvatar();
        legacy.setFileHash(null);
        AppUser user = new AppUser();
        user.setId(7L);
        user.setDeleted(0);
        user.setAvatarFileUrl(legacy.getObjectKey());
        when(files.selectOne(any())).thenReturn(null, null, legacy);
        when(users.selectById(7L)).thenReturn(user);
        when(files.updateById(any(AppFile.class))).thenReturn(1);
        when(storage.getObject(legacy.getObjectKey())).thenReturn(new ByteArrayInputStream(content));

        try (MockedStatic<CurrentUser> currentUser = mockStatic(CurrentUser.class)) {
            currentUser.when(CurrentUser::id).thenReturn(7L);
            FileUploadUrlVO response = new AppFileService(files, users, storage).createUploadUrl(
                    new FileUploadUrlRequest("AVATAR", "avatar.png", "image/png", (long) content.length, hash, "legacy-request"));

            assertEquals("18", response.fileId());
            assertEquals("READY", response.status());
        }

        assertEquals(hash, legacy.getFileHash());
        verify(files).updateById(legacy);
        verify(users, never()).updateById(any(AppUser.class));
        verify(files, never()).insert(any(AppFile.class));
    }

    private static FileUploadUrlRequest request() {
        return new FileUploadUrlRequest("AVATAR", "avatar.png", "image/png", 8L,
                "4c4b6a3be1314ab8617cf4bfe12b0c9f4d93fc2f56c1cbd27b262490fc04e3b7", "request-key");
    }

    private static AppFile readyAvatar() {
        AppFile file = new AppFile();
        file.setId(18L);
        file.setUserId(7L);
        file.setBusinessType("AVATAR");
        file.setObjectKey("avatars/7/existing.png");
        file.setContentType("image/png");
        file.setFileSize(8L);
        file.setFileHash("4c4b6a3be1314ab8617cf4bfe12b0c9f4d93fc2f56c1cbd27b262490fc04e3b7");
        file.setStatus("READY");
        file.setDeleted(0);
        return file;
    }

    private static String sha256(byte[] content) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(content);
        StringBuilder result = new StringBuilder(digest.length * 2);
        for (byte value : digest) result.append(String.format("%02x", value));
        return result.toString();
    }
}
