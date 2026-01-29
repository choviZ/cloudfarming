package com.vv.cloudfarming.framework.storage.core;

import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.framework.storage.config.CosProperties;
import com.vv.cloudfarming.framework.storage.enums.UploadTypeEnum;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Setter
public class FileUploadService {

    private final CosManager cosManager;
    private final CosProperties cosProperties;

    public FileUploadService(CosManager cosManager, CosProperties cosProperties) {
        this.cosManager = cosManager;
        this.cosProperties = cosProperties;
    }

    /**
     * 文件上传
     * @param multipartFile 文件
     * @param bizCode 业务代码
     * @return 可访问的url
     */
    public String fileUpload(MultipartFile multipartFile,String bizCode){
        UploadTypeEnum typeEnum = UploadTypeEnum.getEnumByCode(bizCode);
        if (typeEnum == null) {
            throw new ClientException("业务码不存在");
        }
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
            return result;
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
