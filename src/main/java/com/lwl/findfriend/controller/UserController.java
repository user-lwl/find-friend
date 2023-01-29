package com.lwl.findfriend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lwl.findfriend.common.BaseResponse;
import com.lwl.findfriend.common.ErrorCode;
import com.lwl.findfriend.common.ResultUtils;
import com.lwl.findfriend.exception.BusinessException;
import com.lwl.findfriend.model.domain.User;
import com.lwl.findfriend.model.request.UserLoginRequest;
import com.lwl.findfriend.model.request.UserRegisterRequest;
import com.lwl.findfriend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.lwl.findfriend.contant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"http://localhost:3000"})
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        User currentUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long userId = currentUser.getId();
        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        //仅管理员可用
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list);
    }

    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = userService.searchUsersByTags(tagNameList);
        return ResultUtils.success(userList);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUsers(@RequestBody long id, HttpServletRequest request) {
        //仅管理员可用
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.removeById(id);
        return ResultUtils.success(result);
    }

    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        //校验参数是否为空
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        //更新
        int result = userService.updateUser(user, loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/update/password")
    public BaseResponse<Integer> updatePassword(@RequestBody User user, HttpServletRequest request, String oldPassword) {
        //校验参数是否为空
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        String userAccount = user.getUserAccount();
        //更新
        int result = userService.updatePassword(user, loginUser, userAccount, oldPassword);
        return ResultUtils.success(result);
    }

    @GetMapping("/recommend")
    public BaseResponse<List<User>> recommendUsers(long num, HttpServletRequest request) {
        if (num <= 0 || num > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        String redisKey = String.format("findfriend:user:recommend:%s", loginUser.getId());
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        //先查缓存
        List<User> users = (List<User>) valueOperations.get(redisKey);
        if (users != null) {
            return ResultUtils.success(users);
        }
        //查库
        users = userService.matchUsers(num, loginUser);
        //写缓存
        valueOperations.set(redisKey, users, 60000, TimeUnit.MILLISECONDS);
        return ResultUtils.success(users);
    }

    /**
     * 获取最匹配的用户
     *
     * @param num     数量
     * @param request 请求
     * @return top n匹配的用户
     */
    @GetMapping("/match")
    public BaseResponse<List<User>> matchUsers(long num, HttpServletRequest request) {
        if (num <= 0 || num > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.matchUsers(num, loginUser));
    }
}