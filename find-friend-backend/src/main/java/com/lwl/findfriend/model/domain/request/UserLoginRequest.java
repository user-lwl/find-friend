package com.lwl.findfriend.model.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 6222422475638573851L;

    private String userAccount;

    private String userPassword;

}
