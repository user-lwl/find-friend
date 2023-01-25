package com.lwl.findfriend.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 存取Bucket的服务
 * @author SpartaEN
 */
public interface BucketService {
    /**
     * 上传文件到bucket，将其进行重命名并返回文件URL
     * @param file 文件
     * @return 文件URL
     * @throws IOException
     */
    String upload(MultipartFile file) throws IOException;
}
