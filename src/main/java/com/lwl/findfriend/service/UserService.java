package com.lwl.findfriend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lwl.findfriend.model.domain.User;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户服务
* @author lwl
* @description 针对表【user】的数据库操作Service
* @createDate 2022-10-13 19:54:10
*/
public interface UserService extends IService<User> {


    /**
     * 用户注册
     * @param userAccount 用户名
     * @param userPassword 密码
     * @param checkPassword 密码校验
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode);

    /**
     * 用户登录
     *
     * @param userAccount  用户名
     * @param userPassword 密码
     * @param request 请求
     * @return 用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     * @param originUser 用户信息
     * @return 脱敏用户信息
     */
    User getSafetyUser(User originUser);

    /**
     * 用户注销
     * @param request 请求
     */
    int userLogout(HttpServletRequest request);

    /**
     * 根据标签搜索用户
     *
     * @param tagNameList 用户要拥有的标签
     * @return 用户信息
     */
    List<User> searchUsersByTags(List<String> tagNameList);

    /**
     * 更新用户信息
     * @param user 用户
     * @return 是否更新成功
     */
    int updateUser(User user, User loginUser);

    /**
     * 获取当前登录用户信息
     * @return 用户信息
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 是否为管理员
     * @param request 请求
     * @return 是否为管理员
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     *
     * @param loginUser 登录用户
     * @return 是否为管理员
     */
    boolean isAdmin(User loginUser);

    /**
     * 匹配用户
     * @param num 数量
     * @param loginUser 当前用户
     * @return 用户信息List
     */
    List<User> matchUsers(long num, User loginUser);
}
