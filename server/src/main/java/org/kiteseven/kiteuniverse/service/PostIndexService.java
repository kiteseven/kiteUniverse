package org.kiteseven.kiteuniverse.service;

import org.kiteseven.kiteuniverse.pojo.vo.community.PostSummaryVO;
import org.kiteseven.kiteuniverse.pojo.vo.community.SearchStatsVO;

import java.util.List;

/**
 * 帖子全文搜索索引服务。
 */
public interface PostIndexService {

    /**
     * 将帖子写入（新增或更新）Elasticsearch 索引。
     *
     * @param postId 帖子编号
     */
    void indexPost(Long postId);

    /**
     * 从 Elasticsearch 索引删除指定帖子。
     *
     * @param postId 帖子编号
     */
    void removePost(Long postId);

    /**
     * 全量重建索引：删除现有索引中所有文档并重新从数据库导入。
     */
    void rebuildAll();

    /**
     * 全文检索帖子，支持 IK 分词、前缀匹配、高亮和相关度排序。
     * 同时记录搜索词和是否有结果用于统计。
     *
     * @param keyword 搜索关键词
     * @param limit   最大返回数
     * @return 带高亮字段的帖子概要列表
     */
    List<PostSummaryVO> search(String keyword, int limit);

    /**
     * 搜索建议（自动补全）：按帖子标题前缀快速返回候选词。
     *
     * @param prefix 输入前缀
     * @param limit  最多返回条数
     * @return 候选标题列表
     */
    List<String> suggest(String prefix, int limit);

    /**
     * 基于 more_like_this 推荐与指定帖子相关的帖子。
     *
     * @param postId 当前帖子编号
     * @param limit  最多返回条数
     * @return 相关帖子概要列表
     */
    List<PostSummaryVO> findRelated(Long postId, int limit);

    /**
     * 获取搜索统计数据：热门搜索词与无结果率。
     *
     * @param topN 热门词条数
     * @return 搜索统计视图对象
     */
    SearchStatsVO getSearchStats(int topN);
}
