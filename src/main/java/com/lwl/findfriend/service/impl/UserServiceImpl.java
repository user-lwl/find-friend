package com.lwl.findfriend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lwl.findfriend.common.ErrorCode;
import com.lwl.findfriend.exception.BusinessException;
import com.lwl.findfriend.mapper.UserMapper;
import com.lwl.findfriend.model.domain.User;
import com.lwl.findfriend.service.UserService;
import com.lwl.findfriend.utils.AlgorithmUtils;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.lwl.findfriend.contant.UserConstant.ADMIN_ROLE;
import static com.lwl.findfriend.contant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现类
* @author HP
* @description 针对表【user】的数据库操作Service实现
* @createDate 2022-10-13 19:54:10
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Resource
    private UserMapper userMapper;

    /**
     * 盐，混淆密码
     */
    private static final String SALT = "lwl";


    /**
     * 用户注册
     * @param userAccount 用户名
     * @param userPassword 密码
     * @param checkPassword 密码校验
     * @return 状态
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        //1.校验用户
        //非空
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)){
           throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        //账户不小于4位
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        //密码不小于8位
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        //编号长度不大于5
        if (planetCode.length() > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编号过长");
        }
        //账户不包含特殊字符
        //用户名正则，4到30位（字母，数字，下划线，减号）
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号包含特殊字符");
        }
        //密码和校验密码相同
        if (!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码输入不同");
        }
        //账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        //编号不能重复
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode", planetCode);
        count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编号重复");
        }
        //2.加密
        String newPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //3.插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(newPassword);
        user.setPlanetCode(planetCode);
        boolean result = this.save(user);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户信息保存失败");
        }
        return user.getId();
    }

    /**
     * 用户登录
     *
     * @param userAccount  用户名
     * @param userPassword 密码
     * @param request 请求
     * @return 用户信息
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1.校验用户
        //非空
        if (StringUtils.isAnyBlank(userAccount, userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名或密码为空");
        }
        //账户不小于4位
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号小于4位");
        }
        //密码不小于8位
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码小于8位");
        }
        //账户不包含特殊字符
        //用户名正则，4到30位（字母，数字，下划线，减号）
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号包含特殊字符");
        }
        //2.加密
        String newPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", newPassword);
        User user = userMapper.selectOne(queryWrapper);
        //用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        //3.用户脱敏
        User safetyUser = getSafetyUser(user);
        //4.记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);

        return safetyUser;
    }

    /**
     * 用户脱敏
     * @param originUser 用户信息
     * @return 脱敏信息
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        //3.用户脱敏
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setTags(originUser.getTags());
        return safetyUser;
    }

    @Override
    public int userLogout(HttpServletRequest request) {
        //移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 根据标签搜索用户
     * 内存过滤
     * @param tagNameList 用户要拥有的标签
     * @return 用户列表
     */
    @Override
    public List<User> searchUsersByTags(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //内存查询
        //1.查询所有用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        //2.在内存中判断标签
        Gson gson = new Gson();
        return userList.stream().filter(user -> {
            String tagsStr = user.getTags();
            Set<String> tempTagNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>(){}.getType());
            // Optional可选类
            // ofNullable,封装可能为空的对象，orElse给对象默认值，如果为空就把orElse赋给对象
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
            for (String tagName : tagNameList) {
                if (!tempTagNameSet.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }

    /**
     * 更新用户信息
     * @param user 用户
     * @return 是否更新成功
     */
    @Override
    public int updateUser(User user, User loginUser) {
        // 仅管理员和自己
        long userId = user.getId();
        if (userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //管理员可更新其他用户
        //其他用户只能更新自己
        if (!isAdmin(loginUser) && userId != loginUser.getId()) {
            //不是自己抛异常
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR);
        }
        // 过滤字段
        if (user.getUserStatus() != null && !Objects.equals(user.getUserStatus(), oldUser.getUserStatus())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (user.getCreateTime() != null && !Objects.equals(user.getCreateTime(), oldUser.getCreateTime())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (user.getUpdateTime() != null && !Objects.equals(user.getUpdateTime(), oldUser.getUpdateTime())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (user.getUserRole() != null && !Objects.equals(user.getUserRole(), oldUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (user.getUserAccount() != null && !Objects.equals(user.getUserAccount(), oldUser.getUserAccount())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        // 加密
        user.setUserPassword(DigestUtils.md5DigestAsHex((SALT + user.getUserPassword()).getBytes()));
        //更新
        return userMapper.updateById(user);
    }

    /**
     * 获取登录用户
     * @return 用户信息
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        return (User) userObj;
    }

    /**
     * 根据标签搜索用户
     * SQL查询
     * @param tagNameList 用户要拥有的标签
     * @return 用户列表
     */
    @Deprecated
    private List<User> searchUsersByTagsBySQL(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 数据库查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        //拼接and查询
        for (String tagName : tagNameList) {
            queryWrapper = queryWrapper.like("tags", tagName);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }

    /**
     * 是否为管理员
     * @param request 请求
     * @return 是否为管理员
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        //仅管理员可用
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 是否为管理员
     * @param loginUser 登录用户
     * @return 是否为管理员
     */
    @Override
    public boolean isAdmin(User loginUser) {
        //仅管理员可用
        return loginUser != null && loginUser.getUserRole() == ADMIN_ROLE;
    }

    @Override
    public List<User> matchUsers(long num, User loginUser) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "tags");
        queryWrapper.isNotNull("tags");
        List<User> userList = this.list(queryWrapper);
        if (userList == null) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR);
        }
        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
        //<下标，相似度>
        List<Pair<User, Long>> list = new ArrayList<>();
        for (User user : userList) {
            String userTags = user.getTags();
            //无标签 或 用户自己
            if (StringUtils.isBlank(userTags) || user.getId() == loginUser.getId()) {
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            long distance = AlgorithmUtils.minDistance(tagList, userTagList);
            list.add(new Pair<>(user, distance));
        }
        // 编辑距离排序
        List<Pair<User, Long>> topUserPairList = list.stream()
                .sorted((a, b) -> (int) (a.getValue() - b.getValue()))
                .limit(num)
                .collect(Collectors.toList());
        // userId列表
        List<Long> userIdList = topUserPairList.stream().map(pair -> pair.getKey().getId()).collect(Collectors.toList());
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", userIdList);
        Map<Long, List<User>> userIdUserListMap = this.list(userQueryWrapper)
                .stream()
                .map(this::getSafetyUser)
                .collect(Collectors.groupingBy(User::getId));
        List<User> users = new ArrayList<>();
        for (Long userId : userIdList) {
            users.add(userIdUserListMap.get(userId).get(0));
        }
        return users;
    }

    /**
     * 更新密码
     * @param user 用户信息
     * @param loginUser 当前用户
     * @param userAccount 账号
     * @param oldPassword 旧密码
     * @return 是否更新成功
     */
    @Override
    public int updatePassword(User user, User loginUser, String userAccount, String oldPassword) {
        // 仅管理员和自己
        long userId = user.getId();
        if (userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //管理员可更新其他用户
        //其他用户只能更新自己
        if (!isAdmin(loginUser) && userId != loginUser.getId()) {
            //不是自己抛异常
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_NULL_ERROR);
        }
        if (!Objects.equals(oldUser.getUserPassword(), oldPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 加密
        String newPassword = DigestUtils.md5DigestAsHex((SALT + user.getUserPassword()).getBytes());
        user.setUserPassword(newPassword);
        // 更新
        return userMapper.updateById(user);
    }
}




