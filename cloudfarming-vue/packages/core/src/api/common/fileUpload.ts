import request from "../request";
import type {Result, UploadFileRequest} from "./types";

export function upload(
    file: File,
    data: UploadFileRequest
): Promise<Result<string>> {
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
