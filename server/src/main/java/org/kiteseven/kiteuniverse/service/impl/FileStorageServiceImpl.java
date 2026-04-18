package org.kiteseven.kiteuniverse.service.impl;

import org.kiteseven.kiteuniverse.common.enums.ResultCode;
import org.kiteseven.kiteuniverse.common.exception.BusinessException;
import org.kiteseven.kiteuniverse.config.properties.FileStorageProperties;
import org.kiteseven.kiteuniverse.service.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Stores uploaded avatar files in a local directory exposed by Spring MVC.
 */
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    private final FileStorageProperties fileStorageProperties;

    public FileStorageServiceImpl(FileStorageProperties fileStorageProperties) {
        this.fileStorageProperties = fileStorageProperties;
    }

    /**
     * Persists the uploaded avatar image and returns the public access path.
     *
     * @param file uploaded image
     * @param userId current authenticated user id
     * @return public avatar path
     */
    @Override
    public String storeAvatar(MultipartFile file, Long userId) {
        if (userId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户编号不能为空");
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请先选择头像图片");
        }

        String fileExtension = resolveAllowedExtension(file.getOriginalFilename());
        Path uploadRoot = fileStorageProperties.resolveUploadRoot();
        Path avatarDirectory = uploadRoot.resolve(fileStorageProperties.getAvatarDir()).normalize();

        if (!avatarDirectory.startsWith(uploadRoot)) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "头像目录配置无效");
        }

        String fileName = "user-" + userId + "-" + System.currentTimeMillis()
                + "-" + UUID.randomUUID().toString().substring(0, 8) + fileExtension;
        Path targetPath = avatarDirectory.resolve(fileName).normalize();

        if (!targetPath.startsWith(uploadRoot)) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "头像保存路径无效");
        }

        try {
            Files.createDirectories(avatarDirectory);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "头像上传失败，请稍后重试");
        }

        return fileStorageProperties.resolvePublicUrl(fileStorageProperties.getAvatarDir() + "/" + fileName);
    }

    /**
     * Persists the uploaded post image and returns the public access path.
     *
     * @param file uploaded image
     * @param userId current authenticated user id
     * @return public image path
     */
    @Override
    public String storePostImage(MultipartFile file, Long userId) {
        if (userId == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户编号不能为空");
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请先选择图片");
        }

        String fileExtension = resolveAllowedExtension(file.getOriginalFilename());
        Path uploadRoot = fileStorageProperties.resolveUploadRoot();
        Path imageDirectory = uploadRoot.resolve("images").normalize();

        if (!imageDirectory.startsWith(uploadRoot)) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "图片目录配置无效");
        }

        String fileName = "post-" + userId + "-" + System.currentTimeMillis()
                + "-" + UUID.randomUUID().toString().substring(0, 8) + fileExtension;
        Path targetPath = imageDirectory.resolve(fileName).normalize();

        if (!targetPath.startsWith(uploadRoot)) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "图片保存路径无效");
        }

        try {
            Files.createDirectories(imageDirectory);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "图片上传失败，请稍后重试");
        }

        return fileStorageProperties.resolvePublicUrl("images/" + fileName);
    }

    /**
     * Validates the uploaded filename and returns its file extension.
     *
     * @param originalFilename raw client filename
     * @return normalized allowed extension
     */
    private String resolveAllowedExtension(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "头像文件名不能为空");
        }

        String normalizedName = originalFilename.trim().replace('\\', '/');
        int extensionIndex = normalizedName.lastIndexOf('.');
        if (extensionIndex < 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "头像文件格式不受支持");
        }

        String fileExtension = normalizedName.substring(extensionIndex).toLowerCase(Locale.ROOT);
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(fileExtension)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "头像仅支持 jpg、png、gif 或 webp 格式");
        }
        return fileExtension;
    }
}
