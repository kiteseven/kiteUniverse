-- 用户信息表
CREATE TABLE IF NOT EXISTS `user_info` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户账号ID',
    `real_name` VARCHAR(64) DEFAULT NULL COMMENT '真实姓名',
    `birthday` DATE DEFAULT NULL COMMENT '生日',
    `signature` VARCHAR(255) DEFAULT NULL COMMENT '个性签名',
    `profile` VARCHAR(500) DEFAULT NULL COMMENT '个人简介',
    `country` VARCHAR(64) DEFAULT NULL COMMENT '国家',
    `province` VARCHAR(64) DEFAULT NULL COMMENT '省份',
    `city` VARCHAR(64) DEFAULT NULL COMMENT '城市',
    `website` VARCHAR(255) DEFAULT NULL COMMENT '个人网站',
    `background_image` VARCHAR(255) DEFAULT NULL COMMENT '个人主页背景图',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_info_user_id` (`user_id`),
    CONSTRAINT `fk_user_info_user_id` FOREIGN KEY (`user_id`) REFERENCES `user_account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户信息表';
