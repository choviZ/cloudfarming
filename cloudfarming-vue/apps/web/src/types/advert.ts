/**
 * 广告信息响应参数
 */
export interface AdvertRespDTO {
  /**
   * 轮播图记录的唯一标识符
   */
  id: number;

  /**
   * 图片的URL地址
   */
  imageUrl: string;

  /**
   * 点击图片跳转的链接地址（可选）
   */
  linkUrl?: string | null;

  /**
   * 图片的替代文本，用于SEO和无障碍访问（可选）
   */
  altText?: string | null;

  /**
   * 显示顺序，决定轮播图播放顺序，数值越小优先级越高
   */
  displayOrder: number;

  /**
   * 开始显示日期时间（可选），格式：ISO 8601 字符串，例如 "2025-12-17T10:20:00"
   */
  startDate?: string | null;

  /**
   * 结束显示日期时间（可选），格式：ISO 8601 字符串
   */
  endDate?: string | null;

  /**
   * 是否激活状态
   */
  isActive: boolean;
}