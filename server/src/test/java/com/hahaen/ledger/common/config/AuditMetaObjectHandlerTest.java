package com.hahaen.ledger.common.config;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.hahaen.ledger.common.security.AuditIdentity;
import com.hahaen.ledger.common.security.CurrentUser;
import com.hahaen.ledger.user.entity.AppUser;
import com.hahaen.ledger.user.entity.UserIdentity;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

class AuditMetaObjectHandlerTest {
    private final AuditMetaObjectHandler handler = new AuditMetaObjectHandler();

    @BeforeAll
    static void initTableMetadata() {
        var assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "audit-test");
        assistant.setCurrentNamespace("audit-test");
        TableInfoHelper.initTableInfo(assistant, AppUser.class);
        TableInfoHelper.initTableInfo(assistant, UserIdentity.class);
    }

    @Test
    void anonymousInsertKeepsCreatorNullEvenWhenRecordHasId() {
        try (var user = mockStatic(CurrentUser.class)) {
            user.when(CurrentUser::optionalId).thenReturn(null);
            AppUser entity = new AppUser();
            entity.setId(123L);
            LocalDateTime before = LocalDateTime.now();
            handler.insertFill(SystemMetaObject.forObject(entity));
            assertNull(entity.getCreatedBy());
            assertNull(entity.getCreatedName());
            assertNull(entity.getUpdatedAt());
            assertNotNull(entity.getCreatedAt());
            assertFalse(entity.getCreatedAt().isBefore(before));
            assertFalse(entity.getCreatedAt().isAfter(LocalDateTime.now()));
            assertEquals(0, entity.getDeleted());
        }
    }

    @Test
    void anonymousInsertWithoutIdDoesNotInventSystemCreator() {
        try (var user = mockStatic(CurrentUser.class)) {
            user.when(CurrentUser::optionalId).thenReturn(null);
            AppUser entity = new AppUser();
            handler.insertFill(SystemMetaObject.forObject(entity));
            assertNull(entity.getCreatedBy());
            assertNotNull(entity.getCreatedAt());
            assertEquals(0, entity.getDeleted());
        }
    }

    @Test
    void authenticatedInsertUsesCurrentIdentity() {
        try (var user = mockStatic(CurrentUser.class)) {
            user.when(CurrentUser::optionalId).thenReturn(42L);
            user.when(CurrentUser::optionalName).thenReturn("当前用户");
            AppUser entity = new AppUser();
            entity.setId(123L);
            handler.insertFill(SystemMetaObject.forObject(entity));
            assertEquals(42L, entity.getCreatedBy());
            assertEquals("当前用户", entity.getCreatedName());
        }
    }

    @Test
    void explicitAuditValuesArePreservedIncludingSystemIdentity() {
        try (var user = mockStatic(CurrentUser.class)) {
            user.when(CurrentUser::optionalId).thenReturn(42L);
            user.when(CurrentUser::optionalName).thenReturn("当前用户");
            AppUser entity = new AppUser();
            LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);
            entity.setCreatedAt(createdAt);
            entity.setCreatedBy(AuditIdentity.SYSTEM_USER_ID);
            entity.setCreatedName("系统任务");
            entity.setDeleted(1);
            handler.insertFill(SystemMetaObject.forObject(entity));
            assertEquals(createdAt, entity.getCreatedAt());
            assertEquals(AuditIdentity.SYSTEM_USER_ID, entity.getCreatedBy());
            assertEquals("系统任务", entity.getCreatedName());
            assertEquals(1, entity.getDeleted());
        }
    }

    @Test
    void updateDoesNotFillOrOverwriteCreationFields() {
        try (var user = mockStatic(CurrentUser.class)) {
            user.when(CurrentUser::optionalId).thenReturn(42L);
            user.when(CurrentUser::optionalName).thenReturn("当前用户");
            AppUser entity = new AppUser();
            LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);
            entity.setCreatedAt(createdAt);
            entity.setCreatedName("历史快照");
            entity.setDeleted(0);
            handler.updateFill(SystemMetaObject.forObject(entity));
            assertEquals(createdAt, entity.getCreatedAt());
            assertNull(entity.getCreatedBy());
            assertEquals("历史快照", entity.getCreatedName());
            assertNotNull(entity.getUpdatedAt());
            assertEquals(42L, entity.getUpdatedBy());
            assertEquals("当前用户", entity.getUpdateName());
            assertEquals(0, entity.getDeleted());
        }
    }

    @Test
    void relationInsertFillsOnlyAvailableFields() {
        UserIdentity entity = new UserIdentity();
        handler.insertFill(SystemMetaObject.forObject(entity));
        assertNotNull(entity.getCreatedAt());
        assertEquals(0, entity.getDeleted());
        assertDoesNotThrow(() -> handler.updateFill(SystemMetaObject.forObject(entity)));
    }
}
