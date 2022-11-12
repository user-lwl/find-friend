package com.lwl.findfriend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lwl.findfriend.mapper.UserTeamMapper;
import com.lwl.findfriend.model.domain.UserTeam;
import com.lwl.findfriend.service.UserTeamService;
import org.springframework.stereotype.Service;

/**
* @author HP
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2022-11-12 14:21:19
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService {

}




