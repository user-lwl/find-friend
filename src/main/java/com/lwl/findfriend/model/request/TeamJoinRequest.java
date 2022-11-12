package com.lwl.findfriend.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 */
@Data
public class TeamJoinRequest implements Serializable {

    private static final long serialVersionUID = 6222422475638573851L;

    /**
     * teamId
     */
    private Long teamId;

    /**
     * 密码
     */
    private String password;

}
