-- User phone verification code table
CREATE TABLE IF NOT EXISTS `user_sms_code` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key ID',
    `phone` VARCHAR(20) NOT NULL COMMENT 'Target mobile phone number',
    `biz_type` VARCHAR(32) NOT NULL COMMENT 'Business scene: login or register',
    `code` VARCHAR(6) NOT NULL COMMENT 'Verification code',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT 'Code status: 0 unused, 1 used, 2 expired',
    `expires_at` DATETIME NOT NULL COMMENT 'Expiration time',
    `used_at` DATETIME DEFAULT NULL COMMENT 'Consume time',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    PRIMARY KEY (`id`),
    KEY `idx_user_sms_code_phone_biz_status` (`phone`, `biz_type`, `status`),
    KEY `idx_user_sms_code_expires_at` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='User phone verification code table';
