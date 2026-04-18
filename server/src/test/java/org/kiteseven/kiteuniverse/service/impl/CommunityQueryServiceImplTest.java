package org.kiteseven.kiteuniverse.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiteseven.kiteuniverse.common.exception.BusinessException;
import org.kiteseven.kiteuniverse.mapper.CommunityBoardMapper;
import org.kiteseven.kiteuniverse.mapper.CommunityCommentMapper;
import org.kiteseven.kiteuniverse.mapper.CommunityPostMapper;
import org.kiteseven.kiteuniverse.pojo.vo.community.PostDetailVO;
import org.kiteseven.kiteuniverse.service.PostIndexService;
import org.kiteseven.kiteuniverse.support.redis.CachePenetrationGuardService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunityQueryServiceImplTest {

    @Mock
    private CommunityBoardMapper communityBoardMapper;

    @Mock
    private CommunityPostMapper communityPostMapper;

    @Mock
    private CommunityCommentMapper communityCommentMapper;

    @Mock
    private PostIndexService postIndexService;

    @Mock
    private CachePenetrationGuardService cachePenetrationGuardService;

    @InjectMocks
    private CommunityQueryServiceImpl communityQueryService;

    @Test
    void getPostDetailShouldShortCircuitWhenBloomFilterRejectsId() {
        when(cachePenetrationGuardService.mightContainPostId(404L)).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> communityQueryService.getPostDetail(404L));

        assertEquals(404, exception.getCode());
        verify(communityPostMapper, never()).incrementViewCount(404L);
        verify(communityPostMapper, never()).selectDetailById(404L);
    }

    @Test
    void getPostDetailShouldQueryMapperWhenBloomFilterAllowsId() {
        PostDetailVO postDetailVO = new PostDetailVO();
        postDetailVO.setId(1L);

        when(cachePenetrationGuardService.mightContainPostId(1L)).thenReturn(true);
        when(communityPostMapper.selectDetailById(1L)).thenReturn(postDetailVO);

        PostDetailVO result = communityQueryService.getPostDetail(1L);

        assertEquals(1L, result.getId());
        verify(communityPostMapper).incrementViewCount(1L);
        verify(communityPostMapper).selectDetailById(1L);
    }
}
