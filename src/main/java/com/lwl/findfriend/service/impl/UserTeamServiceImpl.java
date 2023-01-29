package com.lwl.findfriend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lwl.findfriend.common.ErrorCode;
import com.lwl.findfriend.exception.BusinessException;
import com.lwl.findfriend.mapper.UserTeamMapper;
import com.lwl.findfriend.model.domain.UserTeam;
import com.lwl.findfriend.service.UserTeamService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
* @author HP
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2022-11-12 14:21:19
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService {
    @Resource
    private UserTeamMapper userTeamMapper;

    /**
     * 根据队伍id获取成员id
     * @param teamId 队伍id
     * @return 成员id列表
     */
    @Override
    public List<Long> getUserIdByTeamId(Integer teamId) {
        List<Long> userId = new ArrayList<>();
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId", teamId);
        List<UserTeam> userTeams = userTeamMapper.selectList(queryWrapper);
        if (userTeams == null) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR);
        }
        for (UserTeam userTeam : userTeams) {
            userId.add(userTeam.getUserId());
        }
        return userId;
    }
}




