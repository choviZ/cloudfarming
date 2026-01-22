package com.vv.cloudfarming.controller.user;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.lang.UUID;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.enums.UserRoleEnum;
import com.vv.cloudfarming.framework.storage.config.CosProperties;
import com.vv.cloudfarming.framework.storage.core.CosManager;
import com.vv.cloudfarming.framework.storage.dto.UploadFileRequest;
import com.vv.cloudfarming.framework.storage.enums.UploadTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/**
 * 文件接口
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
@Slf4j
public class FileController {

    private final CosManager cosManager;
    private final CosProperties cosProperties;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile multipartFile, UploadFileRequest request) {
        String bizCode = request.getBizCode();
        UploadTypeEnum typeEnum = UploadTypeEnum.getEnumByCode(bizCode);
        if (typeEnum == null) {
            throw new ClientException("业务码不存在");
        }
        // 检查权限
        Set<Integer> roleCodes = typeEnum.getNeedRoles();
        String[] roleKeys = roleCodes.stream()
                .map(UserRoleEnum::fromCode)
                .map(UserRoleEnum::getDescription)
                .toArray(String[]::new);
        StpUtil.checkRoleOr(roleKeys);

        // 根据不同业务划分
        String pathPrefix = typeEnum.getPathPrefix();
        String fileName = UUID.randomUUID() + multipartFile.getOriginalFilename();
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String uploadPath = pathPrefix + "/" + datePath + "/" + fileName;

        File file = null;
        try {
            file = File.createTempFile(uploadPath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(uploadPath, file);
            String result = cosProperties.getHost() + uploadPath;
            log.info("file upload success，biz：{}，path：{}", bizCode, uploadPath);
            return Results.success(result);
        } catch (IOException e) {
            log.error("file upload error, filepath = " + uploadPath, e);
            throw new ServiceException("图片上传失败:" + e.getMessage());
        } finally {
            if (file != null) {
                // 删除临时文件
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error, filepath = {}", uploadPath);
                }
            }
        }
    }
}
