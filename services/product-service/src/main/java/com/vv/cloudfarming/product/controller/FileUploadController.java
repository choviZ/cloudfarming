package com.vv.cloudfarming.product.controller;

import cn.hutool.core.util.StrUtil;
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

import java.util.Set;

@RestController
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @PostMapping("/api/spu/upload")
    public Result<String> uploadFile(
        @RequestParam("file") MultipartFile file,
        @RequestParam(value = "bizCode", required = false) String bizCode
    ) {
        if (file == null || file.isEmpty()) {
            throw new ClientException("文件不能为空");
        }
        String resolvedBizCode = StrUtil.blankToDefault(bizCode, UploadTypeEnum.PRODUCT_SPU_COVER.getCode());
        UploadTypeEnum uploadTypeEnum = UploadTypeEnum.getEnumByCode(resolvedBizCode);
        if (uploadTypeEnum == null) {
            throw new ClientException("业务码不存在");
        }
        String extension = getExtension(file, resolvedBizCode);
        Set<String> allowedExtensions = resolveAllowedExtensions(resolvedBizCode);
        if (!allowedExtensions.contains(extension)) {
            throw new ClientException("不支持该格式：" + extension);
        }
        String url = fileUploadService.fileUpload(file, uploadTypeEnum.getCode());
        return Results.success(url);
    }

    private String getExtension(MultipartFile file, String bizCode) {
        long maxSize = UploadTypeEnum.ANIMAL_DIARY_VIDEO.getCode().equals(bizCode)
            ? 50L * 1024 * 1024
            : 20L * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new ClientException("文件大小不能超过 " + (maxSize / 1024 / 1024) + "MB");
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

    private Set<String> resolveAllowedExtensions(String bizCode) {
        if (UploadTypeEnum.ANIMAL_DIARY_VIDEO.getCode().equals(bizCode)) {
            return Set.of("mp4", "webm", "mov");
        }
        return Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp");
    }
}
