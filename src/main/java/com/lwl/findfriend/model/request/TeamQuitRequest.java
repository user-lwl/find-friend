package com.lwl.findfriend.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户退出队伍请求体
 */
@Data
public class TeamQuitRequest implements Serializable {

    private static final long serialVersionUID = 6222422475638573851L;

    /**
     * teamId
     */
    private Long teamId;

}
