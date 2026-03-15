package com.vv.cloudfarming.product.controller;

import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.framework.storage.core.FileUploadService;
import com.vv.cloudfarming.framework.storage.enums.UploadTypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @PostMapping("/api/spu/upload")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ClientException("文件不能为空");
        }
        String extension = getExtension(file);
        List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp");
        if (!allowedExtensions.contains(extension)) {
            throw new ClientException("不支持该格式：" + extension);
        }
        String url = fileUploadService.fileUpload(file, UploadTypeEnum.PRODUCT_SPU_COVER.getCode());
        return Results.success(url);
    }

    private String getExtension(MultipartFile file) {
        long maxSize = 20 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new ClientException("文件大小不能超过 20MB");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new ClientException("文件名不能为空");
        }
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex + 1).toLowerCase();
        }
        return extension;
    }
}
