package org.kiteseven.kiteuniverse.repository;

import org.kiteseven.kiteuniverse.document.PostDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * 帖子 Elasticsearch 仓库，用于保存和删除文档。
 * 复杂搜索逻辑通过 {@code ElasticsearchOperations} 在服务层实现。
 */
@Repository
public interface PostSearchRepository extends ElasticsearchRepository<PostDocument, Long> {
}
