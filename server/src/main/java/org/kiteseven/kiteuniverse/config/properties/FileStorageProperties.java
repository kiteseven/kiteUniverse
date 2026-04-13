package org.kiteseven.kiteuniverse.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Binds local file storage settings used by avatar uploads.
 */
@ConfigurationProperties(prefix = "kite-universe.file")
public class FileStorageProperties {

    /**
     * Root directory used to save uploaded files.
     */
    private String uploadDir = "./uploads";

    /**
     * Public URL prefix exposed by the backend.
     */
    private String publicBasePath = "/uploads";

    /**
     * Sub-directory used for user avatar files.
     */
    private String avatarDir = "avatars";

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String getPublicBasePath() {
        return publicBasePath;
    }

    public void setPublicBasePath(String publicBasePath) {
        this.publicBasePath = publicBasePath;
    }

    public String getAvatarDir() {
        return avatarDir;
    }

    public void setAvatarDir(String avatarDir) {
        this.avatarDir = avatarDir;
    }

    /**
     * Resolves the upload directory to a normalized absolute path.
     *
     * @return absolute upload root
     */
    public Path resolveUploadRoot() {
        return Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    /**
     * Returns the normalized public URL prefix without a trailing slash.
     *
     * @return normalized public path
     */
    public String resolvePublicBasePath() {
        if (!StringUtils.hasText(publicBasePath)) {
            return "/uploads";
        }

        String normalizedPath = publicBasePath.trim().replace('\\', '/');
        if (!normalizedPath.startsWith("/")) {
            normalizedPath = "/" + normalizedPath;
        }
        if (normalizedPath.endsWith("/")) {
            normalizedPath = normalizedPath.substring(0, normalizedPath.length() - 1);
        }
        return normalizedPath;
    }

    /**
     * Builds a public URL for a stored file.
     *
     * @param relativePath file path relative to the upload root
     * @return public URL path
     */
    public String resolvePublicUrl(String relativePath) {
        String normalizedRelativePath = relativePath.replace('\\', '/');
        if (normalizedRelativePath.startsWith("/")) {
            normalizedRelativePath = normalizedRelativePath.substring(1);
        }
        return resolvePublicBasePath() + "/" + normalizedRelativePath;
    }
}
