/**
 * 新增收货地址请求 DTO
 */
export interface ReceiveAddressAddReq {
  /**
   * 收货人姓名
   */
  receiverName: string;

  /**
   * 收货人手机号
   */
  receiverPhone: string;

  /**
   * 省份名称
   */
  provinceName: string;

  /**
   * 城市名称
   */
  cityName: string;

  /**
   * 区县名称
   */
  districtName: string;

  /**
   * 详细地址
   */
  detailAddress: string;

  /**
   * 是否默认地址：0=否，1=是
   */
  isDefault: number;

  /**
   * 地址备注
   */
  remark?: string;
}

/**
 * 设置默认地址参数对象
 */
export interface ReceiveAddressSetDefaultReq {
    id:string
}

/**
 * 修改收货地址参数对象
 */
export interface ReceiveAddressUpdateReq {
  /**
   * 主键ID
   */
  id: string;

  /**
   * 收货人姓名
   */
  receiverName: string;

  /**
   * 收货人手机号
   */
  receiverPhone: string;

  /**
   * 省份名称
   */
  provinceName: string;

  /**
   * 城市名称
   */
  cityName: string;

  /**
   * 区县名称
   */
  districtName: string;

  /**
   * 详细地址
   */
  detailAddress: string;

  /**
   * 是否默认地址：0=否，1=是
   */
  isDefault: number;

  /**
   * 地址备注
   */
  remark?: string;
}

/**
 * 用户收货地址响应对象
 */
export interface ReceiveAddressResp {
  /**
   * 主键ID
   */
  id: string;

  /**
   * 用户ID
   */
  userId: string;

  /**
   * 收货人姓名
   */
  receiverName: string;

  /**
   * 收货人手机号
   */
  receiverPhone: string;

  /**
   * 省份名称
   */
  provinceName: string;

  /**
   * 城市名称
   */
  cityName: string;

  /**
   * 区县名称
   */
  districtName: string;

  /**
   * 详细地址（如XX小区XX栋XX单元XX室）
   */
  detailAddress: string;

  /**
   * 是否默认地址：0=否，1=是
   */
  isDefault: number;

  /**
   * 地址备注（如"公司地址""家里地址"）
   */
  remark: string;
}

