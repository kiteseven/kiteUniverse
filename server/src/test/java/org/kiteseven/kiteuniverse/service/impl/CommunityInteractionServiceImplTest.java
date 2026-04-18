package org.kiteseven.kiteuniverse.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiteseven.kiteuniverse.mapper.CommunityBoardMapper;
import org.kiteseven.kiteuniverse.mapper.CommunityCommentLikeMapper;
import org.kiteseven.kiteuniverse.mapper.CommunityCommentMapper;
import org.kiteseven.kiteuniverse.mapper.CommunityPostFavoriteMapper;
import org.kiteseven.kiteuniverse.mapper.CommunityPostLikeMapper;
import org.kiteseven.kiteuniverse.mapper.CommunityPostMapper;
import org.kiteseven.kiteuniverse.mapper.UserMapper;
import org.kiteseven.kiteuniverse.pojo.dto.community.PostCreateDTO;
import org.kiteseven.kiteuniverse.pojo.entity.CommunityBoard;
import org.kiteseven.kiteuniverse.pojo.entity.CommunityPost;
import org.kiteseven.kiteuniverse.pojo.entity.User;
import org.kiteseven.kiteuniverse.pojo.vo.community.PostDetailVO;
import org.kiteseven.kiteuniverse.pojo.vo.community.PostLikeStateVO;
import org.kiteseven.kiteuniverse.service.CheckInService;
import org.kiteseven.kiteuniverse.service.NotificationService;
import org.kiteseven.kiteuniverse.service.PostIndexService;
import org.kiteseven.kiteuniverse.support.redis.CachePenetrationGuardService;
import org.kiteseven.kiteuniverse.support.redis.DistributedLockService;
import org.kiteseven.kiteuniverse.support.redis.RedisKeyManager;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunityInteractionServiceImplTest {

    @Mock
    private CommunityBoardMapper communityBoardMapper;

    @Mock
    private CommunityPostMapper communityPostMapper;

    @Mock
    private CommunityPostFavoriteMapper communityPostFavoriteMapper;

    @Mock
    private CommunityPostLikeMapper communityPostLikeMapper;

    @Mock
    private CommunityCommentMapper communityCommentMapper;

    @Mock
    private CommunityCommentLikeMapper communityCommentLikeMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private RedisKeyManager redisKeyManager;

    @Mock
    private NotificationService notificationService;

    @Mock
    private CheckInService checkInService;

    @Mock
    private PostIndexService postIndexService;

    @Mock
    private CachePenetrationGuardService cachePenetrationGuardService;

    @Mock
    private DistributedLockService distributedLockService;

    @InjectMocks
    private CommunityInteractionServiceImpl communityInteractionService;

    @Test
    void createPostShouldAddCreatedPostIdToBloomFilter() {
        User user = new User();
        user.setId(11L);

        CommunityBoard board = new CommunityBoard();
        board.setId(3L);
        board.setStatus(1);

        PostDetailVO detailVO = new PostDetailVO();
        detailVO.setId(501L);

        PostCreateDTO dto = new PostCreateDTO();
        dto.setBoardId(3L);
        dto.setTitle("Bloom Filter");
        dto.setSummary("Protect cache");
        dto.setContent("Protect cache and db");

        when(cachePenetrationGuardService.mightContainUserId(11L)).thenReturn(true);
        when(userMapper.selectById(11L)).thenReturn(user);
        when(communityBoardMapper.selectById(3L)).thenReturn(board);
        when(communityPostMapper.selectDetailById(501L)).thenReturn(detailVO);

        doAnswer(invocation -> {
            CommunityPost post = invocation.getArgument(0);
            post.setId(501L);
            return 1;
        }).when(communityPostMapper).insert(any(CommunityPost.class));

        PostDetailVO result = communityInteractionService.createPost(11L, dto);

        assertEquals(501L, result.getId());
        verify(cachePenetrationGuardService).addPostId(501L);
    }

    @Test
    void likePostShouldUseScopedDistributedLock() {
        User user = new User();
        user.setId(11L);

        CommunityPost post = new CommunityPost();
        post.setId(7L);
        post.setAuthorId(20L);
        post.setStatus(1);
        post.setLikeCount(1);

        CommunityPost refreshedPost = new CommunityPost();
        refreshedPost.setId(7L);
        refreshedPost.setAuthorId(20L);
        refreshedPost.setStatus(1);
        refreshedPost.setLikeCount(2);

        when(cachePenetrationGuardService.mightContainUserId(11L)).thenReturn(true);
        when(cachePenetrationGuardService.mightContainPostId(7L)).thenReturn(true);
        when(userMapper.selectById(11L)).thenReturn(user);
        when(communityPostMapper.selectById(7L)).thenReturn(post, refreshedPost);
        when(communityPostLikeMapper.selectStatus(7L, 11L)).thenReturn(null);
        when(distributedLockService.executeWithLock(
                eq("community:post-like:post:7:user:11"),
                anySupplier()
        )).thenAnswer(invocation -> ((Supplier<PostLikeStateVO>) invocation.getArgument(1)).get());

        PostLikeStateVO result = communityInteractionService.likePost(11L, 7L);

        assertEquals(2, result.getLikeCount());
        verify(communityPostLikeMapper, times(1)).insert(any());
        verify(communityPostMapper, times(1)).incrementLikeCount(7L);
        verify(notificationService).createPostLikeNotification(11L, 7L, 20L);
        verify(checkInService).addPoints(20L, 3);
    }

    @SuppressWarnings("unchecked")
    private Supplier<PostLikeStateVO> anySupplier() {
        return any(Supplier.class);
    }
}
