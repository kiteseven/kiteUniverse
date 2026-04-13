package org.kiteseven.kiteuniverse.service;

import org.kiteseven.kiteuniverse.pojo.vo.content.BoardsPageVO;
import org.kiteseven.kiteuniverse.pojo.vo.content.HomePageVO;

/**
 * Provides cached content payloads used by the community homepage and boards page.
 */
public interface CommunityContentService {

    /**
     * Loads the homepage payload, preferring Redis when possible.
     *
     * @return homepage content
     */
    HomePageVO getHomePage();

    /**
     * Loads the boards-page payload, preferring Redis when possible.
     *
     * @return boards content
     */
    BoardsPageVO getBoardsPage();
}
