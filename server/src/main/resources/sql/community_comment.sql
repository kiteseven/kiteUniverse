CREATE TABLE IF NOT EXISTS community_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论主键',
    post_id BIGINT NOT NULL COMMENT '所属帖子编号',
    author_id BIGINT NULL COMMENT '评论用户编号',
    content VARCHAR(1000) NOT NULL COMMENT '评论内容',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态，1 启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_community_comment_post (post_id),
    KEY idx_community_comment_author (author_id)
) COMMENT='社区评论表';
