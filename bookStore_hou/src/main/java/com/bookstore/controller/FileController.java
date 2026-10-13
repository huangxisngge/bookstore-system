package com.bookstore.controller;

import com.bookstore.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
public class FileController {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.access-prefix:http://localhost:8080/uploads}")
    private String accessPrefix;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }

        String original = file.getOriginalFilename();
        String ext = "";
        int dot = original == null ? -1 : original.lastIndexOf('.');
        if (dot >= 0) {
            ext = original.substring(dot).toLowerCase();
        }
        // 仅允许常见图片格式
        String allowed = ".jpg .jpeg .png .gif .webp .bmp";
        if (!allowed.contains(ext)) {
            return Result.error("仅支持图片格式 (jpg/png/gif/webp/bmp)");
        }

        // 生成唯一文件名，防止覆盖/路径注入
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;

        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            Path target = dir.resolve(filename);
            file.transferTo(target.toAbsolutePath().toFile());
        } catch (IOException e) {
            return Result.error("文件保存失败: " + e.getMessage());
        }

        String url = accessPrefix + "/" + filename;
        // 若配置了前缀主机为空，则用相对路径（由后端同源提供）
        return Result.success("上传成功", url);
    }
}
