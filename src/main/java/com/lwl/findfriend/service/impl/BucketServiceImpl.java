package com.lwl.findfriend.service.impl;

import com.lwl.findfriend.common.ErrorCode;
import com.lwl.findfriend.config.TencentCOSConfig;
import com.lwl.findfriend.exception.BusinessException;
import com.lwl.findfriend.service.BucketService;
import com.lwl.findfriend.utils.SerialUtils;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * 存取Bucket的服务实现
 * @author SpartaEN
 */
@Service
public class BucketServiceImpl implements BucketService {
    @Resource
    private TencentCOSConfig tencentCOSConfig;

    private COSClient cosClient;


    @PostConstruct
    public void init() {
        BasicCOSCredentials credentials = new BasicCOSCredentials(tencentCOSConfig.getSecretId(), tencentCOSConfig.getSecretKey());
        Region region = new Region(tencentCOSConfig.getBucketRegion());
        ClientConfig clientConfig = new ClientConfig(region);
        clientConfig.setHttpProtocol(HttpProtocol.https);
        cosClient = new COSClient(credentials, clientConfig);
    }


    @Override
    public String upload(MultipartFile file) throws IOException {
        if (file.getSize() > tencentCOSConfig.getMaxFileSize()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求文件过大");
        }
        // 上传文件
        File localFile = File.createTempFile("temp", null);
        String fileKey = tencentCOSConfig.getPath() + getRandomFileName(file.getOriginalFilename());
        file.transferTo(localFile);
        PutObjectRequest putObjectRequest = new PutObjectRequest(tencentCOSConfig.getBucketName(), fileKey, localFile);
        PutObjectResult result = cosClient.putObject(putObjectRequest);
        // 释放临时文件
        localFile.delete();
        // 获取链接
        URL objectUrl = cosClient.getObjectUrl(tencentCOSConfig.getBucketName(), fileKey);
        return objectUrl.toString();
    }

    /**
     * 获取随机文件名
     * @param fileName 原始文件名
     * @return 随机文件名
     */
    private String getRandomFileName(String fileName) {
        return SerialUtils.getUUID() + "." + getExtension(fileName);
    }

    /**
     * 获取文件后缀
     * @param fileName 文件名
     * @return 后缀
     */
    private String getExtension(String fileName)
    {
        String extension = FilenameUtils.getExtension(fileName);
        if (fileName.length() == 0)
        {
            extension = "";
        }
        return extension;
    }
}
