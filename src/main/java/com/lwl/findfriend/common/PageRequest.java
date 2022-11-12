package com.lwl.findfriend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用分页请求
 * @author user-lwl
 * @date 2022/11/12 15:07
 */
@Data
public class PageRequest implements Serializable {

    private static final long serialVersionUID = -2178928735916303659L;

    /**
     * 页面大小
     */
    protected int pageSize = 10;

    /**
     * 当前是第几页
     */
    protected int pageNum = 1;
}
