export interface IPage<T> {
  // 当前页数据
  records: T[];
  // 总条目数
  total: number;
  // 每页显示条数
  size: number;
  // 当前页码
  current: number;
  // 总页数
  pages: number;
}


/**
 * 通用响应结果
 */
export interface Result<T> {

  /**
   * 响应状态码
   */
  code: string

  /**
   * 响应消息
   */
  message: string

  /**
   * 响应数据
   */
  data: T
}

/**
 * 上传文件请求
 */
export interface UploadFileRequest{

  /**
   * 业务代码
   */
  bizCode:string
}

// 上传业务枚举
export const UploadType = {
  PRODUCT_SPU_COVER: 'PRODUCT_SPU_COVER',
  PRODUCT_SPU_DETAIL: 'PRODUCT_SPU_DETAIL',
  ADOPT_ITEM_COVER: 'ADOPT_ITEM_COVER',
  ADOPT_ITEM_DETAIL: 'ADOPT_ITEM_DETAIL',
  ANIMAL_PROFILE: 'ANIMAL_PROFILE',
  ANIMAL_DIARY: 'ANIMAL_DIARY',
  BANNER: 'BANNER',
  USER_AVATAR: 'USER_AVATAR',
  ARTICLE_IMAGE: 'ARTICLE_IMAGE',
  TEMP: 'TEMP',
} as const;

export type UploadType = typeof UploadType[keyof typeof UploadType];
