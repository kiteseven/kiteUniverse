package org.kiteseven.kiteuniverse.service.impl;

import org.kiteseven.kiteuniverse.mapper.GameAccountMapper;
import org.kiteseven.kiteuniverse.mapper.GameCharacterMapper;
import org.kiteseven.kiteuniverse.mapper.GameStatsMapper;
import org.kiteseven.kiteuniverse.pojo.dto.game.GameAccountBindDTO;
import org.kiteseven.kiteuniverse.pojo.dto.game.GameCharacterAddDTO;
import org.kiteseven.kiteuniverse.pojo.dto.game.GameStatsUpdateDTO;
import org.kiteseven.kiteuniverse.pojo.entity.GameAccount;
import org.kiteseven.kiteuniverse.pojo.entity.GameCharacterRecord;
import org.kiteseven.kiteuniverse.pojo.entity.GameStats;
import org.kiteseven.kiteuniverse.pojo.vo.game.GameAccountVO;
import org.kiteseven.kiteuniverse.pojo.vo.game.GameCharacterVO;
import org.kiteseven.kiteuniverse.pojo.vo.game.GameStatsVO;
import org.kiteseven.kiteuniverse.service.GameToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 游戏工具服务实现。
 */
@Service
public class GameToolServiceImpl implements GameToolService {

    @Autowired
    private GameAccountMapper gameAccountMapper;

    @Autowired
    private GameCharacterMapper gameCharacterMapper;

    @Autowired
    private GameStatsMapper gameStatsMapper;

    // ── 游戏账号 ──────────────────────────────────────────────────────────────

    @Override
    public List<GameAccountVO> listAccounts(Long userId) {
        return gameAccountMapper.selectByUserId(userId)
                .stream().map(this::toAccountVO).collect(Collectors.toList());
    }

    @Override
    public GameAccountVO bindAccount(Long userId, GameAccountBindDTO dto) {
        GameAccount account = new GameAccount();
        account.setUserId(userId);
        account.setGameUid(dto.getGameUid());
        account.setServerName(dto.getServerName());
        account.setInGameName(dto.getInGameName());
        account.setAccountLevel(dto.getAccountLevel());
        gameAccountMapper.insert(account);
        return toAccountVO(account);
    }

    @Override
    public GameAccountVO updateAccount(Long userId, Long accountId, GameAccountBindDTO dto) {
        GameAccount account = new GameAccount();
        account.setId(accountId);
        account.setUserId(userId);
        account.setServerName(dto.getServerName());
        account.setInGameName(dto.getInGameName());
        account.setAccountLevel(dto.getAccountLevel());
        gameAccountMapper.updateByIdAndUserId(account);
        GameAccount updated = gameAccountMapper.selectByIdAndUserId(accountId, userId);
        return updated != null ? toAccountVO(updated) : null;
    }

    @Override
    public void unbindAccount(Long userId, Long accountId) {
        gameAccountMapper.deleteByIdAndUserId(accountId, userId);
    }

    // ── 跑图记录 ──────────────────────────────────────────────────────────────

    @Override
    public List<GameCharacterVO> listCharacters(Long userId) {
        return gameCharacterMapper.selectByUserId(userId)
                .stream().map(this::toRunRecordVO).collect(Collectors.toList());
    }

    @Override
    public GameCharacterVO addCharacter(Long userId, GameCharacterAddDTO dto) {
        GameCharacterRecord record = new GameCharacterRecord();
        record.setUserId(userId);
        record.setClassId(dto.getClassId());
        record.setClassName(dto.getClassName());
        record.setAscensionLevel(dto.getAscensionLevel());
        record.setActReached(dto.getActReached());
        record.setFloorReached(dto.getFloorReached());
        record.setScore(dto.getScore());
        record.setKeyRelic(dto.getKeyRelic());
        gameCharacterMapper.insert(record);
        return toRunRecordVO(record);
    }

    @Override
    public GameCharacterVO updateCharacter(Long userId, Long characterId, GameCharacterAddDTO dto) {
        GameCharacterRecord record = new GameCharacterRecord();
        record.setId(characterId);
        record.setUserId(userId);
        record.setClassName(dto.getClassName());
        record.setAscensionLevel(dto.getAscensionLevel());
        record.setActReached(dto.getActReached());
        record.setFloorReached(dto.getFloorReached());
        record.setScore(dto.getScore());
        record.setKeyRelic(dto.getKeyRelic());
        gameCharacterMapper.updateByIdAndUserId(record);
        GameCharacterRecord updated = gameCharacterMapper.selectByIdAndUserId(characterId, userId);
        return updated != null ? toRunRecordVO(updated) : null;
    }

    @Override
    public void deleteCharacter(Long userId, Long characterId) {
        gameCharacterMapper.deleteByIdAndUserId(characterId, userId);
    }

    // ── 游戏数据 ──────────────────────────────────────────────────────────────

    @Override
    public GameStatsVO getStats(Long userId) {
        GameStats stats = gameStatsMapper.selectByUserId(userId);
        return stats != null ? toStatsVO(stats) : null;
    }

    @Override
    public GameStatsVO updateStats(Long userId, GameStatsUpdateDTO dto) {
        GameStats existing = gameStatsMapper.selectByUserId(userId);
        GameStats stats = new GameStats();
        stats.setUserId(userId);
        stats.setActionPoint(dto.getActionPoint());
        stats.setMaxActionPoint(dto.getMaxActionPoint());
        stats.setVoidShards(dto.getVoidShards());
        stats.setAccountLevel(dto.getAccountLevel());
        stats.setTotalRuns(dto.getTotalRuns());
        if (existing == null) {
            gameStatsMapper.insert(stats);
        } else {
            stats.setId(existing.getId());
            gameStatsMapper.updateByUserId(stats);
        }
        GameStats saved = gameStatsMapper.selectByUserId(userId);
        return saved != null ? toStatsVO(saved) : toStatsVO(stats);
    }

    // ── 转换方法 ──────────────────────────────────────────────────────────────

    private GameAccountVO toAccountVO(GameAccount a) {
        GameAccountVO vo = new GameAccountVO();
        vo.setId(a.getId());
        vo.setGameUid(a.getGameUid());
        vo.setServerName(a.getServerName());
        vo.setInGameName(a.getInGameName());
        vo.setAccountLevel(a.getAccountLevel());
        vo.setBindTime(a.getBindTime());
        vo.setUpdateTime(a.getUpdateTime());
        return vo;
    }

    private GameCharacterVO toRunRecordVO(GameCharacterRecord r) {
        GameCharacterVO vo = new GameCharacterVO();
        vo.setId(r.getId());
        vo.setClassId(r.getClassId());
        vo.setClassName(r.getClassName());
        vo.setAscensionLevel(r.getAscensionLevel());
        vo.setActReached(r.getActReached());
        vo.setFloorReached(r.getFloorReached());
        vo.setScore(r.getScore());
        vo.setKeyRelic(r.getKeyRelic());
        vo.setUpdateTime(r.getUpdateTime());
        return vo;
    }

    private GameStatsVO toStatsVO(GameStats s) {
        GameStatsVO vo = new GameStatsVO();
        vo.setId(s.getId());
        vo.setActionPoint(s.getActionPoint());
        vo.setMaxActionPoint(s.getMaxActionPoint());
        vo.setVoidShards(s.getVoidShards());
        vo.setAccountLevel(s.getAccountLevel());
        vo.setTotalRuns(s.getTotalRuns());
        vo.setUpdateTime(s.getUpdateTime());
        return vo;
    }
}
