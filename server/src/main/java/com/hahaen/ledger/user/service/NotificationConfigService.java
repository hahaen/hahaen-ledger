package com.hahaen.ledger.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hahaen.ledger.auth.service.PasswordCryptoService;
import com.hahaen.ledger.common.exception.BusinessException;
import com.hahaen.ledger.common.security.CurrentUser;
import com.hahaen.ledger.user.dto.NotificationConfigRequest;
import com.hahaen.ledger.user.entity.UserNotificationConfig;
import com.hahaen.ledger.user.mapper.UserNotificationConfigMapper;
import com.hahaen.ledger.user.vo.NotificationConfigVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class NotificationConfigService {
    private static final Pattern TYPE = Pattern.compile("[A-Z][A-Z0-9_]{1,31}");
    private static final Pattern KEY = Pattern.compile("[A-Za-z0-9_-]{8,64}");
    // RSA-2048 OAEP-SHA256 accepts at most 190 plaintext bytes.
    private static final int MAX_KEY_BYTES = 190;

    private final UserNotificationConfigMapper mapper;
    private final PasswordCryptoService transportCipher;
    private final NotificationKeyCipher storageCipher;

    public List<NotificationConfigVO> list() {
        long userId = CurrentUser.id();
        if (mapper.activeUser(userId) == null) throw new BusinessException("USER_NOT_FOUND", "用户不存在或已停用");
        return mapper.selectList(new LambdaQueryWrapper<UserNotificationConfig>()
                .eq(UserNotificationConfig::getUserId, userId)
                .eq(UserNotificationConfig::getDeleted, 0))
                .stream().map(row -> new NotificationConfigVO(row.getNotificationType(), true,
                        storageCipher.decrypt(row.getNotificationKey()))).toList();
    }

    @Transactional
    public NotificationConfigVO save(String type, NotificationConfigRequest request) {
        if (!TYPE.matcher(type).matches()) throw new BusinessException("NOTIFICATION_TYPE_INVALID", "通知类型无效");
        if (!KEY.matcher(request.idempotencyKey()).matches()) throw new BusinessException("IDEMPOTENCY_KEY_INVALID", "幂等键无效");
        if ((request.remove() && request.encryptedKey() != null)
                || (!request.remove() && (request.encryptedKey() == null || request.encryptedKey().isBlank()))) {
            throw new BusinessException("NOTIFICATION_REQUEST_INVALID", "请提供通知Key或选择移除");
        }
        long userId = CurrentUser.id();
        if (mapper.lockActiveUser(userId) == null) throw new BusinessException("USER_NOT_FOUND", "用户不存在或已停用");
        String hash = hash(type + "\n" + request.remove() + "\n" + request.encryptedKey());
        String previousHash = mapper.requestHash(userId, request.idempotencyKey());
        if (previousHash != null) {
            if (!previousHash.equals(hash)) throw new BusinessException("IDEMPOTENCY_CONFLICT", "幂等键已用于其他请求");
            UserNotificationConfig current = mapper.findIncludingDeleted(userId, type);
            return new NotificationConfigVO(type, current != null && Integer.valueOf(0).equals(current.getDeleted()));
        }
        UserNotificationConfig current = mapper.findIncludingDeleted(userId, type);
        if (request.remove()) {
            if (current != null && Integer.valueOf(0).equals(current.getDeleted())) {
                if (mapper.softDelete(current.getId(), userId, CurrentUser.optionalName()) != 1) {
                    throw new BusinessException("NOTIFICATION_SAVE_FAILED", "通知配置保存失败");
                }
            }
        } else {
            String plain = transportCipher.decrypt(request.encryptedKey());
            if (plain.isBlank() || plain.getBytes(StandardCharsets.UTF_8).length > MAX_KEY_BYTES) {
                throw new BusinessException("NOTIFICATION_KEY_INVALID", "通知Key不能为空且不得超过190字节");
            }
            String encrypted = storageCipher.encrypt(plain);
            if (current == null) {
                current = new UserNotificationConfig();
                current.setUserId(userId);
                current.setNotificationType(type);
                current.setNotificationKey(encrypted);
                if (mapper.insert(current) != 1) throw new BusinessException("NOTIFICATION_SAVE_FAILED", "通知配置保存失败");
            } else {
                if (mapper.restoreOrUpdate(current.getId(), userId, CurrentUser.optionalName(), encrypted) != 1) {
                    throw new BusinessException("NOTIFICATION_SAVE_FAILED", "通知配置保存失败");
                }
            }
        }
        mapper.insertRequest(userId, request.idempotencyKey(), hash);
        return new NotificationConfigVO(type, !request.remove());
    }

    private static String hash(String input) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256不可用", ex);
        }
    }
}
