CREATE TABLE IF NOT EXISTS community_board (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '版块主键',
    name VARCHAR(64) NOT NULL COMMENT '版块名称',
    slug VARCHAR(64) NOT NULL COMMENT '版块别名',
    tag_name VARCHAR(32) NOT NULL COMMENT '版块标签名称',
    description VARCHAR(255) NOT NULL COMMENT '版块简介',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序值',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态，1 启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_community_board_slug (slug)
) COMMENT='社区版块表';
