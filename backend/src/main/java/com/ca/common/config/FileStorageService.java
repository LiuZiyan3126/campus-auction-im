package com.ca.common.config;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /** 上传文件，返回访问URL */
    String upload(MultipartFile file);

    /** 删除文件 */
    void delete(String fileUrl);
}
