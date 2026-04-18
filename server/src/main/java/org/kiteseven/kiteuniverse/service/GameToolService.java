package org.kiteseven.kiteuniverse.service;

import org.kiteseven.kiteuniverse.pojo.dto.game.GameAccountBindDTO;
import org.kiteseven.kiteuniverse.pojo.dto.game.GameCharacterAddDTO;
import org.kiteseven.kiteuniverse.pojo.dto.game.GameStatsUpdateDTO;
import org.kiteseven.kiteuniverse.pojo.vo.game.GameAccountVO;
import org.kiteseven.kiteuniverse.pojo.vo.game.GameCharacterVO;
import org.kiteseven.kiteuniverse.pojo.vo.game.GameStatsVO;

import java.util.List;

/**
 * 游戏工具服务接口。
 */
public interface GameToolService {

    // 游戏账号绑定

    List<GameAccountVO> listAccounts(Long userId);

    GameAccountVO bindAccount(Long userId, GameAccountBindDTO dto);

    GameAccountVO updateAccount(Long userId, Long accountId, GameAccountBindDTO dto);

    void unbindAccount(Long userId, Long accountId);

    // 角色名片

    List<GameCharacterVO> listCharacters(Long userId);

    GameCharacterVO addCharacter(Long userId, GameCharacterAddDTO dto);

    GameCharacterVO updateCharacter(Long userId, Long characterId, GameCharacterAddDTO dto);

    void deleteCharacter(Long userId, Long characterId);

    // 游戏数据

    GameStatsVO getStats(Long userId);

    GameStatsVO updateStats(Long userId, GameStatsUpdateDTO dto);
}
