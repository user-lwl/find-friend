package com.lwl.findfriend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用删除请求
 * @author user-lwl
 * @date 2022/11/12 15:07
 */
@Data
public class DeleteRequest implements Serializable {

    private static final long serialVersionUID = -2178928735916303659L;

    /**
     * id
     */
    private long id;
}
