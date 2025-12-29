/**
 * 农户相关类型
 */

// 申请入住农户请求参数
export interface FarmerApplyReqDO {

  /**
   * 姓名
   */
  realName: string;

  /**
   * 身份证号
   */
  idCard: string;

  /**
   * 户籍地
   */
  houseAddress: string;

  /**
   * 养殖场名称
   */
  farmName: string;

  /**
   * 养殖场地址
   */
  farmAddress: string;

  /**
   * 养殖场面积
   */
  farmArea: number | null;

  /**
   * 养殖品类
   */
  breedingType: string;

  /**
   * 营业执照编号
   */
  businessLicenseNo: number | null;

  /**
   * 营业执照图片
   */
  businessLicensePic: string;

  /**
   * 申请审核备注
   */
  remark: string | null;
}

// 审核状态
export interface FarmerReviewRespDTO{
  status: string;
  reviewRemark: string;
}