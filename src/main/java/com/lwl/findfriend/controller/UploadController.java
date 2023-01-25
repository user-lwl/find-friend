package com.lwl.findfriend.controller;

import com.lwl.findfriend.common.BaseResponse;
import com.lwl.findfriend.common.ResultUtils;
import com.lwl.findfriend.model.vo.FileUploadVo;
import com.lwl.findfriend.service.BucketService;
import com.lwl.findfriend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 文件上传接口
 */
@RestController
@RequestMapping("/upload")
@CrossOrigin(origins = {"http://localhost:3000"})
@Slf4j
public class UploadController {
    @Resource
    private BucketService bucketService;

    @Resource
    private UserService userService;

    @PostMapping("")
    public BaseResponse<FileUploadVo> uploadFile(MultipartFile file, HttpServletRequest httpServletRequest) throws IOException {
        // 检查用户是否登入
        userService.getLoginUser(httpServletRequest);
        return ResultUtils.success(new FileUploadVo(bucketService.upload(file)));
    }
}
