import request from "./request";

/**
 * 文件上传
 * @param {File} file
 * @param {Object} data - UploadFileRequest
 * @returns {Promise<Object>} Result<string>
 */
export function upload(file, data) {
    const formData = new FormData();
    // 名字必须叫 file
    formData.append('file', file);
    // 其他参数
    Object.entries(data).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
            formData.append(key, String(value));
        }
    });

    return request.post(
        '/file/upload',
        formData,
        {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        }
    );
}
