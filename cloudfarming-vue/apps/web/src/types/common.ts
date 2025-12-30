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