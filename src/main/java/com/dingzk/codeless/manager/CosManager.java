package com.dingzk.codeless.manager;

import cn.hutool.core.util.StrUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Slf4j
@Data
public class CosManager {

    @Value("${cos.client.host}")
    private String host;

    @Value("${cos.client.bucket}")
    private String bucket;

    @Resource
    private COSClient cosClient;

    /**
     * 上传文件到 COS
     * @param key 文件在 COS 中的路径
     * @param file 要上传的文件
     * @return 文件在 COS 中的 URL
     */
    public String uploadFile(String key, File file) {
        if (StrUtil.isBlank(key) || file == null) {
            log.error("键或文件为空");
            return null;
        }
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, key, file);
        PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
        if (putObjectResult != null) {
            String fileUrl = String.format("%s/%s", host, key);
            log.info("文件上传至 COS 成功，文件URL为：{}", fileUrl);
            return fileUrl;
        } else {
            log.error("文件上传至 COS 失败");
            return null;
        }
    }
}
