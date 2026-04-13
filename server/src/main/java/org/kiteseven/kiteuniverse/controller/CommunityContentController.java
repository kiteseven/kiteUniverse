package org.kiteseven.kiteuniverse.controller;

import org.kiteseven.kiteuniverse.common.result.Result;
import org.kiteseven.kiteuniverse.pojo.vo.content.BoardsPageVO;
import org.kiteseven.kiteuniverse.pojo.vo.content.HomePageVO;
import org.kiteseven.kiteuniverse.service.CommunityContentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes cached community page payloads used by the frontend home and boards views.
 */
@RestController
@RequestMapping("/api/content")
public class CommunityContentController {

    private final CommunityContentService communityContentService;

    public CommunityContentController(CommunityContentService communityContentService) {
        this.communityContentService = communityContentService;
    }

    /**
     * Loads the homepage content payload.
     *
     * @return homepage content
     */
    @GetMapping("/home")
    public Result<HomePageVO> getHomePage() {
        return Result.success(communityContentService.getHomePage());
    }

    /**
     * Loads the boards-page content payload.
     *
     * @return boards content
     */
    @GetMapping("/boards")
    public Result<BoardsPageVO> getBoardsPage() {
        return Result.success(communityContentService.getBoardsPage());
    }
}
