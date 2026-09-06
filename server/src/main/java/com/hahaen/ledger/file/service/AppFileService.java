package com.hahaen.ledger.file.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hahaen.ledger.common.exception.BusinessException;
import com.hahaen.ledger.common.security.AuditSupport;
import com.hahaen.ledger.common.security.CurrentUser;
import com.hahaen.ledger.file.dto.FileUploadUrlRequest;
import com.hahaen.ledger.file.entity.AppFile;
import com.hahaen.ledger.file.mapper.AppFileMapper;
import com.hahaen.ledger.file.vo.FileCompleteVO;
import com.hahaen.ledger.file.vo.FileUploadUrlVO;
import com.hahaen.ledger.file.vo.FileViewUrlVO;
import com.hahaen.ledger.user.entity.AppUser;
import com.hahaen.ledger.user.mapper.AppUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppFileService {
    private static final int PRESIGNED_EXPIRES_SECONDS = 600;
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private final AppFileMapper fileMapper;
    private final AppUserMapper userMapper;
    private final MinioStorageService storage;

    public FileUploadUrlVO createUploadUrl(FileUploadUrlRequest request) {
        long userId = CurrentUser.id();
        validateUpload(request);
        String idempotencyKey = request.idempotencyKey().trim();
        AppFile existing = fileMapper.selectOne(new LambdaQueryWrapper<AppFile>()
                .eq(AppFile::getUserId, userId)
                .eq(AppFile::getIdempotencyKey, idempotencyKey)
                .eq(AppFile::getDeleted, 0));
        if (existing != null) {
            if (!sameUpload(existing, request)) throw new BusinessException("FILE_IDEMPOTENCY_CONFLICT", "文件幂等键已用于其他文件");
            return uploadResponse(existing);
        }

        AppFile file = new AppFile();
        file.setUserId(userId);
        file.setBusinessType("AVATAR");
        file.setStorageProvider("MINIO");
        file.setBucketName(storage.bucketName());
        file.setObjectKey("avatars/" + userId + "/" + UUID.randomUUID() + extension(request.contentType()));
        file.setOriginalName(trimmedName(request.originalName()));
        file.setContentType(request.contentType().trim().toLowerCase(Locale.ROOT));
        file.setFileSize(request.fileSize());
        file.setFileHash(normalizeHash(request.fileHash()));
        file.setStatus("UPLOADING");
        file.setIdempotencyKey(idempotencyKey);
        try {
            fileMapper.insert(file);
            return uploadResponse(file);
        } catch (DuplicateKeyException ex) {
            AppFile retry = fileMapper.selectOne(new LambdaQueryWrapper<AppFile>()
                    .eq(AppFile::getUserId, userId)
                    .eq(AppFile::getIdempotencyKey, idempotencyKey)
                    .eq(AppFile::getDeleted, 0));
            if (retry != null) return uploadResponse(retry);
            throw new BusinessException("FILE_CREATE_FAILED", "文件记录创建失败，请重试");
        }
    }

    public FileCompleteVO complete(long fileId) {
        AppFile file = ownedFile(fileId);
        if ("READY".equals(file.getStatus())) return completeResponse(file);
        if (!"UPLOADING".equals(file.getStatus())) throw new BusinessException("FILE_STATUS_INVALID", "文件当前状态不可确认");
        try {
            var object = storage.statObject(file.getObjectKey());
            if (object.size() != file.getFileSize()) {
                markFailed(file, "FILE_SIZE_MISMATCH");
                throw new BusinessException("FILE_SIZE_MISMATCH", "文件大小校验失败");
            }
            if (object.contentType() != null && !file.getContentType().equalsIgnoreCase(object.contentType())) {
                markFailed(file, "FILE_CONTENT_TYPE_MISMATCH");
                throw new BusinessException("FILE_CONTENT_TYPE_MISMATCH", "文件类型校验失败");
            }
            file.setStorageEtag(object.etag());
            file.setUploadedAt(LocalDateTime.now());
            file.setStatus("READY");
            file.setFailureCode(null);
            fileMapper.updateById(file);
            AppUser user = userMapper.selectById(CurrentUser.id());
            if (user != null) {
                user.setAvatarFileId(file.getId());
                userMapper.updateById(user);
            }
            return completeResponse(file);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            markFailed(file, "MINIO_OBJECT_NOT_FOUND");
            throw new BusinessException("MINIO_OBJECT_NOT_FOUND", "未找到已上传的文件，请重新上传");
        }
    }

    public FileViewUrlVO viewUrl(long fileId) {
        AppFile file = ownedFile(fileId);
        if (!"READY".equals(file.getStatus())) throw new BusinessException("FILE_NOT_READY", "文件尚未完成上传");
        try {
            return new FileViewUrlVO(file.getId(), storage.presignedViewUrl(file.getObjectKey()), PRESIGNED_EXPIRES_SECONDS);
        } catch (Exception ex) {
            throw new BusinessException("MINIO_UNAVAILABLE", "文件服务暂时不可用，请稍后重试");
        }
    }

    public FileViewUrlVO currentAvatarViewUrl() {
        AppUser user = userMapper.selectById(CurrentUser.id());
        if (user == null || user.getAvatarFileId() == null) return null;
        return viewUrl(user.getAvatarFileId());
    }

    public void delete(long fileId) {
        AppFile file = ownedFile(fileId);
        if (file.getDeleted() != null && file.getDeleted() == 1) return;
        file.setStatus("DELETING");
        fileMapper.updateById(file);
        try {
            storage.removeObject(file.getObjectKey());
            file.setStatus("DELETED");
            file.setFailureCode(null);
            AuditSupport.markDeleted(file);
            fileMapper.markDeleted(file, CurrentUser.id());
            AppUser user = userMapper.selectById(CurrentUser.id());
            if (user != null && file.getId().equals(user.getAvatarFileId())) {
                user.setAvatarFileId(null);
                userMapper.updateById(user);
            }
        } catch (Exception ex) {
            file.setStatus("FAILED");
            file.setFailureCode("MINIO_DELETE_FAILED");
            fileMapper.updateById(file);
            throw new BusinessException("MINIO_DELETE_FAILED", "文件删除失败，请稍后重试");
        }
    }

    private AppFile ownedFile(long fileId) {
        AppFile file = fileMapper.selectOne(new LambdaQueryWrapper<AppFile>()
                .eq(AppFile::getId, fileId)
                .eq(AppFile::getUserId, CurrentUser.id())
                .eq(AppFile::getDeleted, 0));
        if (file == null) throw new BusinessException("FILE_NOT_FOUND", "文件不存在");
        return file;
    }

    private FileUploadUrlVO uploadResponse(AppFile file) {
        if (!"UPLOADING".equals(file.getStatus())) {
            return new FileUploadUrlVO(file.getId(), "", PRESIGNED_EXPIRES_SECONDS, file.getStatus());
        }
        try {
            return new FileUploadUrlVO(file.getId(), storage.presignedUploadUrl(file.getObjectKey()), PRESIGNED_EXPIRES_SECONDS, file.getStatus());
        } catch (Exception ex) {
            throw new BusinessException("MINIO_UNAVAILABLE", "文件服务暂时不可用，请稍后重试");
        }
    }

    private static void validateUpload(FileUploadUrlRequest request) {
        String type = request.businessType().trim().toUpperCase(Locale.ROOT);
        if (!"AVATAR".equals(type)) throw new BusinessException("FILE_BUSINESS_NOT_OPEN", "当前仅开放头像文件上传");
        String contentType = request.contentType().trim().toLowerCase(Locale.ROOT);
        if (!SetOfTypes.ALLOWED.contains(contentType)) throw new BusinessException("FILE_TYPE_NOT_ALLOWED", "头像仅支持 JPG、PNG、WEBP 或 GIF");
        if (request.fileSize() <= 0 || request.fileSize() > MAX_FILE_SIZE) throw new BusinessException("FILE_SIZE_NOT_ALLOWED", "头像大小需大于 0 且不超过 10MB");
        String hash = request.fileHash();
        if (hash != null && !hash.isBlank() && !hash.matches("[0-9a-fA-F]{64}")) throw new BusinessException("FILE_HASH_INVALID", "文件摘要格式不正确");
    }

    private static boolean sameUpload(AppFile file, FileUploadUrlRequest request) {
        return file.getFileSize().equals(request.fileSize())
                && file.getContentType().equalsIgnoreCase(request.contentType().trim())
                && "AVATAR".equalsIgnoreCase(request.businessType());
    }

    private static String extension(String contentType) {
        return switch (contentType.trim().toLowerCase(Locale.ROOT)) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> ".jpg";
        };
    }

    private static String trimmedName(String name) {
        String value = name == null ? "file" : name.trim().replaceAll("[\\r\\n\\\\/]", "_");
        return value.substring(0, Math.min(255, value.length()));
    }

    private static String normalizeHash(String hash) {
        return hash == null || hash.isBlank() ? null : hash.trim().toLowerCase(Locale.ROOT);
    }

    private static FileCompleteVO completeResponse(AppFile file) {
        return new FileCompleteVO(file.getId(), file.getStatus(), file.getFileSize(), file.getContentType());
    }

    private void markFailed(AppFile file, String failureCode) {
        file.setStatus("FAILED");
        file.setFailureCode(failureCode);
        fileMapper.updateById(file);
    }

    private static final class SetOfTypes {
        private static final java.util.Set<String> ALLOWED = java.util.Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    }
}
