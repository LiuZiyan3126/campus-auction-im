package com.ca.common.config;

import com.ca.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class LocalFileStorageService implements FileStorageService {

    private final Path uploadPath;
    private final Set<String> allowedTypes;
    private final long maxSize;

    public LocalFileStorageService(
            @Value("${file.upload-path}") String uploadPath,
            @Value("${file.allowed-types}") String allowedTypes,
            @Value("${file.max-size}") long maxSize) {
        this.uploadPath = Paths.get(uploadPath).toAbsolutePath().normalize();
        this.allowedTypes = Set.of(allowedTypes.split(","));
        this.maxSize = maxSize;
        try {
            Files.createDirectories(this.uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("无法创建上传目录: " + this.uploadPath, e);
        }
    }

    @Override
    public String upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw BusinessException.badRequest("文件不能为空");
        }
        if (file.getSize() > maxSize) {
            throw BusinessException.badRequest("文件大小超过限制（最大5MB）");
        }
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        }
        if (!allowedTypes.contains(extension)) {
            throw BusinessException.badRequest("不支持的文件类型: " + extension + "，仅允许 " + allowedTypes);
        }
        String newFilename = UUID.randomUUID() + "." + extension;
        try {
            Path targetPath = this.uploadPath.resolve(newFilename);
            file.transferTo(targetPath.toFile());
            log.info("文件上传成功: {}", newFilename);
            return "/uploads/images/" + newFilename;
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(500, "文件上传失败");
        }
    }

    @Override
    public void delete(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith("/uploads/images/")) return;
        String filename = fileUrl.substring("/uploads/images/".length());
        try {
            Files.deleteIfExists(this.uploadPath.resolve(filename));
        } catch (IOException e) {
            log.warn("删除文件失败: {}", filename, e);
        }
    }
}
