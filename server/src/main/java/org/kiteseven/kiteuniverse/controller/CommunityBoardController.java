package org.kiteseven.kiteuniverse.controller;

import org.kiteseven.kiteuniverse.common.result.Result;
import org.kiteseven.kiteuniverse.pojo.vo.community.BoardSummaryVO;
import org.kiteseven.kiteuniverse.service.CommunityQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 提供社区版块查询接口。
 */
@RestController
@RequestMapping("/api/boards")
public class CommunityBoardController {

    private final CommunityQueryService communityQueryService;

    public CommunityBoardController(CommunityQueryService communityQueryService) {
        this.communityQueryService = communityQueryService;
    }

    /**
     * 查询启用版块概要列表。
     *
     * @return 版块概要列表
     */
    @GetMapping
    public Result<List<BoardSummaryVO>> listBoards() {
        return Result.success(communityQueryService.listBoardSummaries());
    }

    /**
     * 查询指定版块概要。
     *
     * @param boardId 版块编号
     * @return 版块概要
     */
    @GetMapping("/{boardId}")
    public Result<BoardSummaryVO> getBoard(@org.springframework.web.bind.annotation.PathVariable Long boardId) {
        return Result.success(communityQueryService.getBoardSummary(boardId));
    }
}
