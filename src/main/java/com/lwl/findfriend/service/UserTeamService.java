package com.lwl.findfriend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lwl.findfriend.model.domain.UserTeam;

import java.util.List;

/**
* @author HP
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service
* @createDate 2022-11-12 14:21:19
*/
public interface UserTeamService extends IService<UserTeam> {

    /**
     * 根据队伍id获取成员id
     * @param teamId 队伍id
     * @return 成员id列表
     */
    List<Long> getUserIdByTeamId(Integer teamId);
}
