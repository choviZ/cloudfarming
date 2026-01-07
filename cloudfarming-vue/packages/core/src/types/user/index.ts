/**
 * 用户相关类型定义
 */

/**
 * 用户信息
 */
export interface UserRespDTO {
  /** 主键 */
  id: string;

  /** 用户名 */
  username: string;

  /** 密码 */
  password: string;

  /** 手机号 */
  phone: string;

  /** 头像 */
  avatar: string;

  /** 用户类型： 0-普通用户 1-农户 2-系统管理员 */
  userType: number;

  /** 账号状态：0-正常 1-禁用 */
  status: number;

  /** 创建时间 */
  createTime: Date;

  /** 更新时间 */
  updateTime: Date;
}


/**
 * 用户注册请求参数
 */
export interface UserRegisterReqDTO {
  /**
   * 用户名，需为6-18位，仅允许中文、英文、数字、下划线
   */
  username: string

  /**
   * 密码，必须为6-20位，且包含字母和数字
   */
  password: string

  /**
   * 确认密码，必须为6-20位，且包含字母和数字
   */
  checkPassword: string
}

/**
 * 用户登录请求参数
 */
export interface UserLoginReqDTO {
  /**
   * 用户名
   */
  username: string

  /**
   * 密码
   */
  password: string
}

/**
 * 创建用户请求参数
 */
export interface UserCreateReqDTO {
  /**
   * 用户名
   */
  username: string

  /**
   * 密码
   */
  password: string

  /**
   * 手机号
   */
  phone: string

  /**
   * 头像
   */
  avatar: string

  /**
   * 用户类型： 0-普通用户 1-农户 2-系统管理员
   */
  userType: number

  /**
   * 账号状态：0-正常 1-禁用
   */
  status: number
}

/**
 * 修改用户信息请求参数
 */
export interface UserUpdateReqDTO {
  /**
   * 主键
   */
  id: string

  /**
   * 用户名
   */
  username?: string

  /**
   * 密码
   */
  password?: string

  /**
   * 手机号
   */
  phone?: string

  /**
   * 头像
   */
  avatar?: string

  /**
   * 用户类型： 0-普通用户 1-农户 2-系统管理员
   */
  userType?: number

  /**
   * 账号状态： 0-正常 1-禁用
   */
  status?: number
}

/**
 * 用户分页查询请求参数
 */
export interface UserPageQueryReqDTO {
  /**
   * 当前页码
   */
  current?: number

  /**
   * 每页显示条数
   */
  size?: number

  /**
   * 用户名（模糊查询）
   */
  username?: string

  /**
   * 手机号（模糊查询）
   */
  phone?: string

  /**
   * 用户类型
   */
  userType?: number

  /**
   * 账号状态
   */
  status?: number
}
