package org.kiteseven.kiteuniverse.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Handles storage for files uploaded by the frontend.
 */
public interface FileStorageService {

    /**
     * Stores the avatar image for the specified user and returns its public path.
     *
     * @param file uploaded image
     * @param userId current authenticated user id
     * @return public avatar path
     */
    String storeAvatar(MultipartFile file, Long userId);

    /**
     * Stores a post image uploaded by the specified user and returns its public path.
     *
     * @param file uploaded image
     * @param userId current authenticated user id
     * @return public image path
     */
    String storePostImage(MultipartFile file, Long userId);
}
