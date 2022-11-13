package com.lwl.findfriend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lwl.findfriend.model.domain.Team;
import com.lwl.findfriend.model.domain.User;
import com.lwl.findfriend.model.dto.TeamQuery;
import com.lwl.findfriend.model.request.TeamJoinRequest;
import com.lwl.findfriend.model.request.TeamQuitRequest;
import com.lwl.findfriend.model.request.TeamUpdateRequest;
import com.lwl.findfriend.model.vo.TeamUserVO;

import java.util.List;

/**
* @author user-lwl
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2022-11-12 14:19:59
*/
public interface TeamService extends IService<Team> {

    /**
     * 创建队伍
     * @param team 队伍信息
     * @param loginUser 当前用户
     * @return id
     */
    long addTeam(Team team, User loginUser);

    /**
     * 搜索队伍
     * @param teamQuery 队伍查询
     * @param isAdmin 是否是管理员
     * @return 队伍和用户信息
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin);

    /**
     * 更新队伍
     * @param teamUpdateRequest 更新信息
     * @param longinUser 登录的用户
     * @return 是否成功
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User longinUser);

    /**
     * 加入队伍
     * @param teamJoinRequest 队伍加入请求
     * @param loginUser 当前登录的用户
     * @return 是否成功
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    /**
     * 退出队伍
     * @param teamQuitRequest 用户退出队伍请求
     * @param loginUser 当前登录用户
     * @return 是否成功
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * 解散（删除）队伍
     * @param id 队伍id
     * @param loginUser 当前登录用户
     * @return 是否成功
     */
    boolean deleteTeam(long id, User loginUser);
}
