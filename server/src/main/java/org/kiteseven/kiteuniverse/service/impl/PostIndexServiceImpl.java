package org.kiteseven.kiteuniverse.service.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MoreLikeThisQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Like;
import org.kiteseven.kiteuniverse.document.PostDocument;
import org.kiteseven.kiteuniverse.mapper.CommunityPostMapper;
import org.kiteseven.kiteuniverse.pojo.dto.community.PostIndexDTO;
import org.kiteseven.kiteuniverse.pojo.vo.community.PostSummaryVO;
import org.kiteseven.kiteuniverse.pojo.vo.community.SearchStatsVO;
import org.kiteseven.kiteuniverse.repository.PostSearchRepository;
import org.kiteseven.kiteuniverse.service.PostIndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightFieldParameters;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightParameters;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 帖子全文搜索索引服务实现。
 *
 * <p>搜索策略（bool should）：
 * <ol>
 *   <li>multi_match + fuzziness=AUTO — 分词模糊匹配，提高召回</li>
 *   <li>match_phrase_prefix — 前缀匹配（"赛博"→"赛博朋克"）</li>
 * </ol>
 * 搜索词及结果数量写入 Redis ZSet 用于统计看板。
 */
@Service
public class PostIndexServiceImpl implements PostIndexService {

    private static final Logger log = LoggerFactory.getLogger(PostIndexServiceImpl.class);

    private static final String PRE_TAG = "<em class=\"search-highlight\">";
    private static final String POST_TAG = "</em>";

    /** Redis key：搜索词频率 ZSet（member=词, score=搜索次数） */
    private static final String REDIS_KEY_HOT = "search:hot_keywords";
    /** Redis key：无结果搜索词 ZSet */
    private static final String REDIS_KEY_ZERO = "search:zero_result_keywords";
    /** Redis key：总搜索次数 */
    private static final String REDIS_KEY_TOTAL = "search:total_count";

    private final CommunityPostMapper communityPostMapper;
    private final PostSearchRepository postSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final StringRedisTemplate stringRedisTemplate;

    public PostIndexServiceImpl(CommunityPostMapper communityPostMapper,
                                PostSearchRepository postSearchRepository,
                                ElasticsearchOperations elasticsearchOperations,
                                StringRedisTemplate stringRedisTemplate) {
        this.communityPostMapper = communityPostMapper;
        this.postSearchRepository = postSearchRepository;
        this.elasticsearchOperations = elasticsearchOperations;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void indexPost(Long postId) {
        PostIndexDTO dto = communityPostMapper.selectForIndexById(postId);
        if (dto == null) {
            postSearchRepository.deleteById(postId);
            return;
        }
        postSearchRepository.save(toDocument(dto));
    }

    @Override
    public void removePost(Long postId) {
        postSearchRepository.deleteById(postId);
    }

    @Override
    public void rebuildAll() {
        postSearchRepository.deleteAll();
        List<PostIndexDTO> all = communityPostMapper.selectAllForIndex();
        List<PostDocument> docs = new ArrayList<>(all.size());
        for (PostIndexDTO dto : all) {
            docs.add(toDocument(dto));
        }
        postSearchRepository.saveAll(docs);
        log.info("[ES] Rebuilt index with {} documents.", docs.size());
    }

    @Override
    public List<PostSummaryVO> search(String keyword, int limit) {
        // ① multi_match: 分词模糊匹配（title^3 badge^2 summary^2 content）
        Query multiMatch = Query.of(q -> q.multiMatch(MultiMatchQuery.of(mm -> mm
                .query(keyword)
                .fields("title^3", "badge^2", "summary^2", "content")
                .fuzziness("AUTO")
        )));

        // ② match_phrase_prefix: 前缀匹配（"赛博" → "赛博朋克"/"赛博坦"）
        Query phrasePrefix = Query.of(q -> q.matchPhrasePrefix(mp -> mp
                .field("title")
                .query(keyword)
                .boost(2.0f)
        ));

        // ③ pinyin multi_match: 拼音搜索（"sbpk" / "saibo" → "赛博朋克"）
        Query pinyinMatch = Query.of(q -> q.multiMatch(MultiMatchQuery.of(mm -> mm
                .query(keyword)
                .fields("title.pinyin^2", "badge.pinyin")
        )));

        // filter: 只搜已发布帖子
        Query statusFilter = Query.of(q -> q.term(TermQuery.of(t -> t
                .field("status")
                .value(1)
        )));

        Query boolQuery = Query.of(q -> q.bool(BoolQuery.of(b -> b
                .should(multiMatch)
                .should(phrasePrefix)
                .should(pinyinMatch)
                .filter(statusFilter)
                .minimumShouldMatch("1")
        )));

        // 高亮：title 不分片（0=返回整段），summary/content 取150字片段
        HighlightFieldParameters titleFieldParams = HighlightFieldParameters.builder()
                .withNumberOfFragments(0)
                .build();
        HighlightFieldParameters snippetFieldParams = HighlightFieldParameters.builder()
                .withNumberOfFragments(1)
                .withFragmentSize(150)
                .build();
        List<HighlightField> highlightFields = new ArrayList<>();
        highlightFields.add(new HighlightField("title", titleFieldParams));
        highlightFields.add(new HighlightField("summary", snippetFieldParams));
        highlightFields.add(new HighlightField("content", snippetFieldParams));

        HighlightParameters globalParams = HighlightParameters.builder()
                .withPreTags(PRE_TAG)
                .withPostTags(POST_TAG)
                .build();
        Highlight highlight = new Highlight(globalParams, highlightFields);

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(boolQuery)
                .withHighlightQuery(new org.springframework.data.elasticsearch.core.query.HighlightQuery(highlight, PostDocument.class))
                .withMaxResults(limit)
                .build();

        SearchHits<PostDocument> hits = elasticsearchOperations.search(nativeQuery, PostDocument.class);

        List<PostSummaryVO> results = new ArrayList<>();
        for (SearchHit<PostDocument> hit : hits) {
            PostDocument doc = hit.getContent();
            Map<String, List<String>> hlFields = hit.getHighlightFields();

            PostSummaryVO vo = toSummaryVO(doc);

            List<String> titleHL = hlFields.get("title");
            if (titleHL != null && !titleHL.isEmpty()) {
                vo.setHighlightTitle(titleHL.get(0));
            }
            List<String> summaryHL = hlFields.get("summary");
            List<String> contentHL = hlFields.get("content");
            if (summaryHL != null && !summaryHL.isEmpty()) {
                vo.setHighlightSnippet(summaryHL.get(0));
            } else if (contentHL != null && !contentHL.isEmpty()) {
                vo.setHighlightSnippet(contentHL.get(0));
            }

            results.add(vo);
        }

        // 记录搜索统计到 Redis（失败不影响主流程）
        recordSearchStats(keyword, results.isEmpty());

        return results;
    }

    @Override
    public List<String> suggest(String prefix, int limit) {
        // match_phrase_prefix on title for fast autocomplete
        Query query = Query.of(q -> q.bool(BoolQuery.of(b -> b
                .must(Query.of(mq -> mq.matchPhrasePrefix(mp -> mp
                        .field("title")
                        .query(prefix)
                )))
                .filter(Query.of(fq -> fq.term(TermQuery.of(t -> t
                        .field("status")
                        .value(1)
                ))))
        )));

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(query)
                .withMaxResults(limit)
                .build();

        SearchHits<PostDocument> hits = elasticsearchOperations.search(nativeQuery, PostDocument.class);
        List<String> suggestions = new ArrayList<>();
        for (SearchHit<PostDocument> hit : hits) {
            String title = hit.getContent().getTitle();
            if (title != null && !suggestions.contains(title)) {
                suggestions.add(title);
            }
        }
        return suggestions;
    }

    @Override
    public List<PostSummaryVO> findRelated(Long postId, int limit) {
        // more_like_this based on title + summary + content of the given post
        Like likeDoc = Like.of(l -> l
                .document(d -> d
                        .index("community_posts")
                        .id(String.valueOf(postId))
                )
        );

        Query moreLikeThis = Query.of(q -> q.moreLikeThis(MoreLikeThisQuery.of(mlt -> mlt
                .like(likeDoc)
                .fields("title", "summary", "content", "badge")
                .minTermFreq(1)
                .minDocFreq(1)
                .maxQueryTerms(12)
        )));

        // exclude the post itself and only published posts
        Query excludeSelf = Query.of(q -> q.term(TermQuery.of(t -> t
                .field("_id")
                .value(String.valueOf(postId))
        )));
        Query statusFilter = Query.of(q -> q.term(TermQuery.of(t -> t
                .field("status")
                .value(1)
        )));

        Query boolQuery = Query.of(q -> q.bool(BoolQuery.of(b -> b
                .must(moreLikeThis)
                .filter(statusFilter)
                .mustNot(excludeSelf)
        )));

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(boolQuery)
                .withMaxResults(limit)
                .build();

        SearchHits<PostDocument> hits = elasticsearchOperations.search(nativeQuery, PostDocument.class);
        List<PostSummaryVO> results = new ArrayList<>();
        for (SearchHit<PostDocument> hit : hits) {
            results.add(toSummaryVO(hit.getContent()));
        }
        return results;
    }

    @Override
    public SearchStatsVO getSearchStats(int topN) {
        SearchStatsVO vo = new SearchStatsVO();
        try {
            // 热门搜索词（ZSet 按 score 降序）
            Set<org.springframework.data.redis.core.ZSetOperations.TypedTuple<String>> tuples =
                    stringRedisTemplate.opsForZSet().reverseRangeWithScores(REDIS_KEY_HOT, 0, topN - 1);
            List<SearchStatsVO.TermCount> hotKeywords = new ArrayList<>();
            if (tuples != null) {
                for (org.springframework.data.redis.core.ZSetOperations.TypedTuple<String> t : tuples) {
                    if (t.getValue() != null && t.getScore() != null) {
                        hotKeywords.add(new SearchStatsVO.TermCount(t.getValue(), t.getScore().longValue()));
                    }
                }
            }
            vo.setHotKeywords(hotKeywords);

            // 总搜索次数
            String totalStr = stringRedisTemplate.opsForValue().get(REDIS_KEY_TOTAL);
            long total = totalStr != null ? Long.parseLong(totalStr) : 0L;
            vo.setTotalSearches(total);

            // 无结果搜索次数（ZSet 所有成员的 score 之和）
            Long zeroCard = stringRedisTemplate.opsForZSet().zCard(REDIS_KEY_ZERO);
            long zeroCount = 0L;
            if (zeroCard != null && zeroCard > 0) {
                Set<org.springframework.data.redis.core.ZSetOperations.TypedTuple<String>> zeroTuples =
                        stringRedisTemplate.opsForZSet().rangeWithScores(REDIS_KEY_ZERO, 0, -1);
                if (zeroTuples != null) {
                    for (org.springframework.data.redis.core.ZSetOperations.TypedTuple<String> t : zeroTuples) {
                        if (t.getScore() != null) zeroCount += t.getScore().longValue();
                    }
                }
            }
            vo.setZeroResultSearches(zeroCount);
            vo.setZeroResultRate(total > 0 ? Math.round(zeroCount * 1000.0 / total) / 10.0 : 0.0);
        } catch (Exception e) {
            log.warn("[Search] Failed to read search stats from Redis: {}", e.getMessage());
            vo.setHotKeywords(new ArrayList<>());
        }
        return vo;
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    /**
     * 将搜索词和结果情况写入 Redis 用于统计。
     */
    private void recordSearchStats(String keyword, boolean zeroResult) {
        try {
            stringRedisTemplate.opsForZSet().incrementScore(REDIS_KEY_HOT, keyword, 1);
            stringRedisTemplate.opsForValue().increment(REDIS_KEY_TOTAL);
            if (zeroResult) {
                stringRedisTemplate.opsForZSet().incrementScore(REDIS_KEY_ZERO, keyword, 1);
            }
        } catch (Exception e) {
            log.debug("[Search] Stats write skipped: {}", e.getMessage());
        }
    }

    private PostDocument toDocument(PostIndexDTO dto) {
        PostDocument doc = new PostDocument();
        doc.setId(dto.getId());
        doc.setTitle(dto.getTitle());
        doc.setSummary(dto.getSummary());
        doc.setContent(dto.getContent());
        doc.setBadge(dto.getBadge());
        doc.setBoardId(dto.getBoardId());
        doc.setBoardName(dto.getBoardName());
        doc.setBoardSlug(dto.getBoardSlug());
        doc.setBoardTagName(dto.getBoardTagName());
        doc.setAuthorId(dto.getAuthorId());
        doc.setAuthorName(dto.getAuthorName());
        doc.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        doc.setViewCount(dto.getViewCount());
        doc.setCommentCount(dto.getCommentCount());
        doc.setFavoriteCount(dto.getFavoriteCount());
        doc.setLikeCount(dto.getLikeCount());
        doc.setPinned(Boolean.TRUE.equals(dto.getPinned()));
        doc.setFeatured(Boolean.TRUE.equals(dto.getFeatured()));
        doc.setAiGenerated(Boolean.TRUE.equals(dto.getAiGenerated()));
        doc.setPublishedAt(dto.getPublishedAt());
        return doc;
    }

    private PostSummaryVO toSummaryVO(PostDocument doc) {
        PostSummaryVO vo = new PostSummaryVO();
        vo.setId(doc.getId());
        vo.setBoardId(doc.getBoardId());
        vo.setBoardName(doc.getBoardName());
        vo.setBoardSlug(doc.getBoardSlug());
        vo.setBoardTagName(doc.getBoardTagName());
        vo.setAuthorId(doc.getAuthorId());
        vo.setAuthorName(doc.getAuthorName());
        vo.setBadge(doc.getBadge());
        vo.setTitle(doc.getTitle());
        vo.setSummary(doc.getSummary());
        vo.setViewCount(doc.getViewCount());
        vo.setCommentCount(doc.getCommentCount());
        vo.setFavoriteCount(doc.getFavoriteCount());
        vo.setLikeCount(doc.getLikeCount());
        vo.setPinned(Boolean.TRUE.equals(doc.getPinned()));
        vo.setFeatured(Boolean.TRUE.equals(doc.getFeatured()));
        vo.setPublishedAt(doc.getPublishedAt());
        return vo;
    }
}
