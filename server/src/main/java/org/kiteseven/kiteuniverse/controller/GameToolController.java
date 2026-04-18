package org.kiteseven.kiteuniverse.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.kiteseven.kiteuniverse.common.result.Result;
import org.kiteseven.kiteuniverse.common.enums.ResultCode;
import org.kiteseven.kiteuniverse.pojo.dto.game.GameAccountBindDTO;
import org.kiteseven.kiteuniverse.pojo.dto.game.GameCharacterAddDTO;
import org.kiteseven.kiteuniverse.pojo.dto.game.GameStatsUpdateDTO;
import org.kiteseven.kiteuniverse.pojo.vo.game.GameAccountVO;
import org.kiteseven.kiteuniverse.pojo.vo.game.GameCharacterVO;
import org.kiteseven.kiteuniverse.pojo.vo.game.GameStatsVO;
import org.kiteseven.kiteuniverse.service.GameToolService;
import org.kiteseven.kiteuniverse.support.auth.UserTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 游戏工具接口：账号绑定、角色名片、游戏数据。
 */
@RestController
@RequestMapping("/api/game")
public class GameToolController {

    @Autowired
    private GameToolService gameToolService;

    @Autowired
    private UserTokenService userTokenService;

    // ── 游戏账号 ──────────────────────────────────────────────────────────────

    @GetMapping("/accounts")
    public Result<List<GameAccountVO>> listAccounts(HttpServletRequest request) {
        String token = userTokenService.resolveToken(request);
        if (token == null) return Result.fail(ResultCode.UNAUTHORIZED, "请先登录");
        Long userId = userTokenService.parseUserId(token);
        return Result.success(gameToolService.listAccounts(userId));
    }

    @PostMapping("/accounts")
    public Result<GameAccountVO> bindAccount(@RequestBody GameAccountBindDTO dto, HttpServletRequest request) {
        String token = userTokenService.resolveToken(request);
        if (token == null) return Result.fail(ResultCode.UNAUTHORIZED, "请先登录");
        if (dto.getGameUid() == null || dto.getGameUid().isBlank())
            return Result.fail(ResultCode.BAD_REQUEST, "游戏UID不能为空");
        Long userId = userTokenService.parseUserId(token);
        return Result.success(gameToolService.bindAccount(userId, dto));
    }

    @PutMapping("/accounts/{id}")
    public Result<GameAccountVO> updateAccount(@PathVariable Long id,
                                               @RequestBody GameAccountBindDTO dto,
                                               HttpServletRequest request) {
        String token = userTokenService.resolveToken(request);
        if (token == null) return Result.fail(ResultCode.UNAUTHORIZED, "请先登录");
        Long userId = userTokenService.parseUserId(token);
        GameAccountVO vo = gameToolService.updateAccount(userId, id, dto);
        if (vo == null) return Result.fail(ResultCode.NOT_FOUND, "账号不存在");
        return Result.success(vo);
    }

    @DeleteMapping("/accounts/{id}")
    public Result<Void> unbindAccount(@PathVariable Long id, HttpServletRequest request) {
        String token = userTokenService.resolveToken(request);
        if (token == null) return Result.fail(ResultCode.UNAUTHORIZED, "请先登录");
        Long userId = userTokenService.parseUserId(token);
        gameToolService.unbindAccount(userId, id);
        return Result.success(null);
    }

    // ── 角色名片 ──────────────────────────────────────────────────────────────

    @GetMapping("/characters")
    public Result<List<GameCharacterVO>> listCharacters(HttpServletRequest request) {
        String token = userTokenService.resolveToken(request);
        if (token == null) return Result.fail(ResultCode.UNAUTHORIZED, "请先登录");
        Long userId = userTokenService.parseUserId(token);
        return Result.success(gameToolService.listCharacters(userId));
    }

    @PostMapping("/characters")
    public Result<GameCharacterVO> addCharacter(@RequestBody GameCharacterAddDTO dto,
                                                HttpServletRequest request) {
        String token = userTokenService.resolveToken(request);
        if (token == null) return Result.fail(ResultCode.UNAUTHORIZED, "请先登录");
        if (dto.getClassName() == null || dto.getClassName().isBlank())
            return Result.fail(ResultCode.BAD_REQUEST, "职业名称不能为空");
        Long userId = userTokenService.parseUserId(token);
        return Result.success(gameToolService.addCharacter(userId, dto));
    }

    @PutMapping("/characters/{id}")
    public Result<GameCharacterVO> updateCharacter(@PathVariable Long id,
                                                   @RequestBody GameCharacterAddDTO dto,
                                                   HttpServletRequest request) {
        String token = userTokenService.resolveToken(request);
        if (token == null) return Result.fail(ResultCode.UNAUTHORIZED, "请先登录");
        Long userId = userTokenService.parseUserId(token);
        GameCharacterVO vo = gameToolService.updateCharacter(userId, id, dto);
        if (vo == null) return Result.fail(ResultCode.NOT_FOUND, "角色不存在");
        return Result.success(vo);
    }

    @DeleteMapping("/characters/{id}")
    public Result<Void> deleteCharacter(@PathVariable Long id, HttpServletRequest request) {
        String token = userTokenService.resolveToken(request);
        if (token == null) return Result.fail(ResultCode.UNAUTHORIZED, "请先登录");
        Long userId = userTokenService.parseUserId(token);
        gameToolService.deleteCharacter(userId, id);
        return Result.success(null);
    }

    // ── 游戏数据 ──────────────────────────────────────────────────────────────

    @GetMapping("/stats")
    public Result<GameStatsVO> getStats(HttpServletRequest request) {
        String token = userTokenService.resolveToken(request);
        if (token == null) return Result.fail(ResultCode.UNAUTHORIZED, "请先登录");
        Long userId = userTokenService.parseUserId(token);
        return Result.success(gameToolService.getStats(userId));
    }

    @PutMapping("/stats")
    public Result<GameStatsVO> updateStats(@RequestBody GameStatsUpdateDTO dto,
                                           HttpServletRequest request) {
        String token = userTokenService.resolveToken(request);
        if (token == null) return Result.fail(ResultCode.UNAUTHORIZED, "请先登录");
        Long userId = userTokenService.parseUserId(token);
        return Result.success(gameToolService.updateStats(userId, dto));
    }
}
