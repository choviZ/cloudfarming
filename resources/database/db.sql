CREATE DATABASE IF NOT EXISTS `cloud_farming`;

create table t_users
(
    id          bigint                              not null primary key,
    username    varchar(50)                         not null,
    password    varchar(255)                        not null,
    phone       varchar(20)                         null,
    email       varchar(100)                        null,
    avatar      varchar(255)                        null,
    user_type   tinyint   default 0                 null comment '0:普通用户 1:农户 2:系统管理员',
    status      tinyint   default 0                 null comment '0:正常 1:禁用',
    create_time timestamp default CURRENT_TIMESTAMP null,
    update_time timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    del_flag    tinyint   default 0                 null comment '0:正常 1:删除',
    constraint phone unique (phone),
    constraint username unique (username)
) COMMENT '用户表' CHARSET = utf8mb4;

create table t_farmer
(
    id                   bigint(18)           not null comment '主键' primary key,
    user_id              bigint(18)           not null comment '用户id',
    real_name            varchar(255)         not null comment '姓名',
    id_card              varchar(255)         not null comment '身份证号',
    house_address        varchar(255)         null comment '户籍地',
    farm_name            varchar(255)         null comment '养殖场名称',
    farm_address         varchar(255)         null comment '养殖场地址',
    farm_area            double               null comment '养殖场面积',
    breeding_type        varchar(255)         null comment '养殖品类',
    business_license_no  bigint(18)           null comment '营业执照编号',
    business_license_pic varchar(255)         null comment '营业执照图片',
    environment_images   text                 null comment '农场环境图片，多个地址使用英文逗号分隔',
    featured_flag        tinyint(1) default 0 not null comment '是否精选：0-否 1-是',
    remark               varchar(255)         null comment '申请审核备注',
    review_status        tinyint(1)           null comment '审核状态 0-待审核 1-已通过 2-未通过',
    review_user_id       varchar(255)         null comment '审核人id',
    review_remark        varchar(255)         null comment '管理员审核备注',
    create_time          datetime             not null on update CURRENT_TIMESTAMP comment '创建时间',
    update_time          datetime             not null comment '修改时间',
    del_flag             tinyint(1)           not null comment '逻辑删除'
) COMMENT '农户表' CHARSET = utf8mb4;

create table t_receive_address
(
    id             bigint                               not null comment '主键ID' primary key,
    user_id        bigint                               not null comment '用户ID',
    receiver_name  varchar(50)                          not null comment '收货人姓名',
    receiver_phone varchar(20)                          not null comment '收货人手机号',
    province_name  varchar(50)                          not null comment '省份名称（冗余存储，减少关联查询）',
    city_name      varchar(50)                          not null comment '城市名称',
    district_name  varchar(50)                          not null comment '区县名称',
    detail_address varchar(500)                         not null comment '详细地址（如XX小区XX栋XX单元XX室）',
    is_default     tinyint(1) default 0                 not null comment '是否默认地址：0=否，1=是',
    remark         varchar(200)                         null comment '地址备注（如“公司地址”“家里地址”）',
    create_time    datetime   default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time    datetime   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    del_flag       tinyint(1) default 0                 not null comment '是否删除',
    INDEX idx_user_id (user_id)
) COMMENT '收货地址表' charset = utf8mb4;

create table t_shops
(
    id           bigint auto_increment primary key,
    farmer_id    bigint                              not null comment '农户（用户）ID',
    shop_name    varchar(100)                        not null comment '店铺名称',
    shop_avatar  varchar(255)                        null comment '店铺头像',
    shop_banner  varchar(255)                        null comment '店铺横幅',
    description  text                                null comment '店铺描述',
    announcement text                                null comment '店铺公告',
    status       tinyint   default 1                 null comment '状态: 1-正常, 2-禁用',
    create_time  timestamp default CURRENT_TIMESTAMP null,
    update_time  timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    del_flag     tinyint   default 0                 not null comment '是否删除',
    constraint uk_farmer_id unique (farmer_id)
) COMMENT '店铺表' charset = utf8mb4;

create table t_cart_item
(
    id          bigint auto_increment primary key,
    user_id     bigint                               not null comment '用户ID',
    sku_id      bigint                               not null comment 'SKU ID',
    quantity    int                                  not null comment '购买数量',
    selected    tinyint(1) default 1                 not null comment '是否选中',
    create_time datetime   default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    del_flag    tinyint(1) default 0                 not null comment '逻辑删除',
    unique key uk_user_sku (user_id, sku_id),
    key idx_user_update_time (user_id, update_time),
    key idx_user_selected (user_id, selected)
) COMMENT '购物车表' charset = utf8mb4;

create table t_feedback
(
    id             bigint                               not null comment '主键ID' primary key,
    user_id        bigint                               not null comment '提交用户ID',
    submitter_type tinyint(1)                           not null comment '提交人类型：0-普通用户 1-农户',
    feedback_type  varchar(32)                          not null comment '反馈类型编码',
    content        varchar(1000)                        not null comment '反馈内容',
    contact_phone  varchar(20)                          not null comment '联系电话',
    status         tinyint(1) default 0                 not null comment '处理状态：0-待处理 1-已处理',
    reply_content  varchar(500)                         null comment '处理回复',
    handler_id     bigint                               null comment '处理人ID',
    handle_time    datetime                             null comment '处理时间',
    create_time    datetime   default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time    datetime   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    del_flag       tinyint(1) default 0                 not null comment '逻辑删除',
    key idx_user_create_time (user_id, create_time),
    key idx_status_create_time (status, create_time),
    key idx_type_create_time (feedback_type, create_time)
) COMMENT '意见反馈表' charset = utf8mb4;

create table t_adopt_item
(
    id              bigint auto_increment
        primary key,
    shop_id         bigint                             not null comment '店铺id',
    title           varchar(100)                       not null comment '认养标题',
    animal_category varchar(20)                        not null comment '动物分类（代码）',
    adopt_days      int                                not null comment '认养周期（天）',
    price           decimal(10, 2)                     not null comment '认养价格',
    expected_yield  varchar(100)                       null comment '预计收益（用户自填，如10kg肉）',
    description     text                               null comment '认养说明（喂养方式、发货方式等）',
    cover_image     varchar(255)                       null,
    total_count     int                                null comment '本批次总数',
    lock_count      int      default 0                 null comment '锁定的库存数量',
    available_count int                                null comment '剩余可认养数量',
    audit_status    tinyint  default 0                 null comment '0=待审核 1=通过 2=拒绝',
    status          tinyint  default 1                 null comment '1=上架 0=下架',
    create_time     datetime default CURRENT_TIMESTAMP null,
    update_time     datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    del_flag        tinyint  default 0                 null comment '是否删除'
);

create table t_adopt_instance
(
    id             int auto_increment comment '主键id'
        primary key,
    item_id        bigint                             not null comment '认养项目id',
    ear_tag_no     bigint                             null comment '耳标号 / 动物的唯一标识',
    image          varchar(128)                       null comment '图片',
    status         int                                null comment '养殖中 / 已出栏 / 配送中 / 已完成 /死亡终止',
    farmer_id      bigint                             null comment '农户id',
    owner_user_id  bigint                             null comment '所属用户id',
    owner_order_id bigint                             null comment '绑定订单id',
    death_time     date                               null comment '死亡时间',
    death_reason   varchar(255)                       null comment '死亡原因',
    create_time    datetime default CURRENT_TIMESTAMP null,
    update_time    datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    del_flag       tinyint  default 0                 null
)
    comment '认养牲畜表';

create table t_adopt_log
(
    id          bigint auto_increment comment '主键id'
        primary key,
    instance_id bigint                             not null comment '认养实例id',
    log_type    int                                not null comment '日志类型：1-喂食 2-体重记录 3-防疫 4-日常记录 5-异常事件',
    content     varchar(512)                       null comment '文字描述',
    image_urls  varchar(1024)                      null comment '图片地址，多个用逗号分隔',
    video_url   varchar(255)                       null comment '视频地址',
    weight      decimal(8, 2)                      null comment '本次记录体重（kg）',
    operator_id bigint                             not null comment '操作人id（农户）',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime                           null comment '修改时间',
    del_flag    tinyint  default 0                 null comment '逻辑删除',
    index idx_instance_id (instance_id)
) comment '认养实例生长日志表';

create table t_advert
(
    id            int auto_increment
        primary key,
    image_url     varchar(255)                         not null comment '图片的URL地址',
    link_url      varchar(255)                         null comment '点击图片跳转的链接地址',
    alt_text      varchar(255)                         null comment '图片的替代文本，用于SEO和无障碍访问',
    display_order int                                  not null comment '显示顺序，决定轮播图播放顺序',
    start_date    datetime                             null comment '开始显示日期时间',
    end_date      datetime                             null comment '结束显示日期时间',
    is_active     tinyint(1) default 1                 null comment '是否激活状态，若为FALSE则不显示',
    create_time   datetime   default CURRENT_TIMESTAMP null comment '记录创建时间',
    update_time   datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '记录最后更新时间',
    del_flag      tinyint    default 0                 null
)
    comment '广告表（首页轮播图）';

create table t_audit_record
(
    id            bigint auto_increment comment '主键ID'
        primary key,
    biz_type      int                                  not null comment '业务类型：0-云养殖项目，1-农产品',
    biz_id        bigint                               not null comment '业务ID（商品ID）',
    audit_status  int        default 0                 not null comment '审核状态：0-待审核，1-通过，2-拒绝',
    submitter_id  bigint                               null comment '提交人ID',
    auditor_id    bigint                               null comment '审核人ID',
    audit_time    datetime                             null comment '审核时间',
    reject_reason varchar(500)                         null comment '拒绝原因',
    create_time   datetime   default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   datetime   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '修改时间',
    del_flag      tinyint(1) default 0                 not null comment '删除标识 0：未删除 1：已删除'
)
    comment '审核记录表' charset = utf8mb4;


create table t_category
(
    id          bigint auto_increment
        primary key,
    name        varchar(255)                        not null comment '分类名称',
    parent_id   bigint                              null comment '父级分类ID，顶级分类为NULL',
    sort_order  int       default 0                 null comment '排序权重',
    create_time timestamp default CURRENT_TIMESTAMP null,
    update_time timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    del_flag    tinyint                             null
)
    comment '商品分类表' charset = utf8mb4;

create table t_attribute
(
    id          bigint auto_increment
        primary key,
    category_id bigint                              not null comment '关联的分类ID',
    name        varchar(255)                        not null comment '属性名',
    attr_type   int                                 not null comment '属性类型：0-基本/1-销售',
    sort        int       default 0                 null comment '排序权重',
    create_time timestamp default CURRENT_TIMESTAMP null,
    update_time timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    del_flag    tinyint   default 0                 null
)
    comment '属性表' charset = utf8mb4;

create table t_spu
(
    id           bigint unsigned auto_increment comment 'SPU主键'
        primary key,
    shop_id      bigint                             not null comment '所属店铺ID',
    title        varchar(150)                       not null comment '商品标题',
    category_id  bigint                             not null comment '商品分类ID',
    images       varchar(256)                       null comment '商品图片列表',
    audit_status tinyint  default 2                 not null comment '审核状态',
    status       tinyint                            null comment '上下架状态',
    create_time  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    del_flag     tinyint  default 0                 not null comment '逻辑删除'
);
create table t_sku_attr_value
(
    id         bigint auto_increment comment 'id'
        primary key,
    sku_id     bigint       null comment '标准库存商品id',
    attr_id    bigint       null comment '属性id',
    attr_value varchar(255) null comment '属性值',
    price      decimal      null,
    stock      int          null comment '库存'
);

create table t_sku
(
    id          bigint unsigned auto_increment comment 'SKU唯一主键'
        primary key,
    spu_id      bigint unsigned                                        not null comment '关联SPU ID',
    sku_image   varchar(255)                                           null comment '商品图片',
    price       decimal(10, 2)                                         null comment '商品价格',
    lock_stock  int                          default 0                 null comment '锁定库存',
    stock       int                                                    null comment '商品库存',
    status      tinyint(1)                   default 1                 not null comment 'SKU状态：0-下架，1-上架',
    create_time datetime                     default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime                     default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    del_flag    tinyint(1) unsigned zerofill default 0                 not null comment '逻辑删除：0-未删除，1-已删除',
    index idx_spu_id (spu_id)
)
    comment '农产品商品SKU表';

create table t_sku_attr_value
(
    id         bigint auto_increment comment 'id'
        primary key,
    sku_id     bigint       null comment '标准库存商品id',
    attr_id    bigint       null comment '属性id',
    attr_value varchar(255) null comment '属性值',
    price      decimal      null,
    stock      int          null comment '库存'
);

create table t_pay
(
    id           bigint                             not null comment '主键ID'
        primary key,
    pay_order_no varchar(64)                        not null comment '支付聚合单号',
    buyer_id     bigint                             not null comment '买家ID',
    total_amount decimal(10, 2)                     not null comment '支付总金额',
    pay_channel  int                                null comment '支付渠道',
    trade_no     varchar(64)                        null comment '三方流水号',
    pay_status   int      default 0                 not null comment '支付状态: 0-待支付 1-已支付 2-失败',
    biz_status   int                                null comment '业务状态 0-待支付 1-已完成 2-已关闭（超时/主动取消）',
    pay_time     datetime                           null comment '支付时间',
    expire_time  datetime                           null comment '过期时间',
    create_time  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    del_flag     int      default 0                 null comment '是否删除',
    constraint uk_pay_order_no
        unique (pay_order_no)
)
    comment '支付单表' charset = utf8mb4;

CREATE TABLE t_order_0
(
    id                BIGINT                                   NOT NULL COMMENT '主键ID' PRIMARY KEY,
    order_no          VARCHAR(128)                             NOT NULL COMMENT '业务订单号',
    pay_order_no      VARCHAR(64)                              NULL COMMENT '支付单号',
    user_id           BIGINT                                   NOT NULL COMMENT '用户ID',
    shop_id           BIGINT                                   NULL COMMENT '店铺ID',
    order_type        INT                                      NOT NULL COMMENT '订单类型:  0-认养 1-普通电商',
    total_amount      DECIMAL(10, 2)                           NOT NULL COMMENT '订单总额',
    actual_pay_amount DECIMAL(10, 2)                           NOT NULL COMMENT '实付金额',
    freight_amount    DECIMAL(10, 2) DEFAULT 0.00              NULL COMMENT '运费',
    discount_amount   DECIMAL(10, 2) DEFAULT 0.00              NULL COMMENT '优惠金额',
    order_status      INT                                      NOT NULL COMMENT '订单状态',
    receive_name      VARCHAR(32)                              NULL COMMENT '收货人',
    receive_phone     VARCHAR(20)                              NULL COMMENT '收货电话',
    province_name     VARCHAR(50)                              NULL COMMENT '省份名称',
    city_name         VARCHAR(50)                              NULL COMMENT '城市名称',
    district_name     VARCHAR(50)                              NULL COMMENT '区县名称',
    detail_address    VARCHAR(255)                             NULL COMMENT '收货地址',
    logistics_no      VARCHAR(64)                              NULL COMMENT '物流单号',
    logistics_company VARCHAR(64)                              NULL COMMENT '物流公司',
    delivery_time     DATETIME                                 NULL COMMENT '发货时间',
    receive_time      DATETIME                                 NULL COMMENT '收货时间',
    create_time       DATETIME       DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    update_time       DATETIME       DEFAULT CURRENT_TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag          INT            DEFAULT 0                 NULL COMMENT '是否删除',
    CONSTRAINT uk_order_no UNIQUE (order_no),
    INDEX idx_pay_order_no (pay_order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) COMMENT '订单表' CHARSET = utf8mb4;

CREATE TABLE t_order_1
(
    id                BIGINT                                   NOT NULL COMMENT '主键ID' PRIMARY KEY,
    order_no          VARCHAR(128)                             NOT NULL COMMENT '业务订单号',
    pay_order_no      VARCHAR(64)                              NULL COMMENT '支付单号',
    user_id           BIGINT                                   NOT NULL COMMENT '用户ID',
    shop_id           BIGINT                                   NULL COMMENT '店铺ID',
    order_type        INT                                      NOT NULL COMMENT '订单类型:  0-认养 1-普通电商',
    total_amount      DECIMAL(10, 2)                           NOT NULL COMMENT '订单总额',
    actual_pay_amount DECIMAL(10, 2)                           NOT NULL COMMENT '实付金额',
    freight_amount    DECIMAL(10, 2) DEFAULT 0.00              NULL COMMENT '运费',
    discount_amount   DECIMAL(10, 2) DEFAULT 0.00              NULL COMMENT '优惠金额',
    order_status      INT                                      NOT NULL COMMENT '订单状态',
    receive_name      VARCHAR(32)                              NULL COMMENT '收货人',
    receive_phone     VARCHAR(20)                              NULL COMMENT '收货电话',
    province_name     VARCHAR(50)                              NULL COMMENT '省份名称',
    city_name         VARCHAR(50)                              NULL COMMENT '城市名称',
    district_name     VARCHAR(50)                              NULL COMMENT '区县名称',
    detail_address    VARCHAR(255)                             NULL COMMENT '收货地址',
    logistics_no      VARCHAR(64)                              NULL COMMENT '物流单号',
    logistics_company VARCHAR(64)                              NULL COMMENT '物流公司',
    delivery_time     DATETIME                                 NULL COMMENT '发货时间',
    receive_time      DATETIME                                 NULL COMMENT '收货时间',
    create_time       DATETIME       DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    update_time       DATETIME       DEFAULT CURRENT_TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag          INT            DEFAULT 0                 NULL COMMENT '是否删除',
    CONSTRAINT uk_order_no UNIQUE (order_no),
    INDEX idx_pay_order_no (pay_order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) COMMENT '订单表' CHARSET = utf8mb4;

CREATE TABLE t_order_2
(
    id                BIGINT                                   NOT NULL COMMENT '主键ID' PRIMARY KEY,
    order_no          VARCHAR(128)                             NOT NULL COMMENT '业务订单号',
    pay_order_no      VARCHAR(64)                              NULL COMMENT '支付单号',
    user_id           BIGINT                                   NOT NULL COMMENT '用户ID',
    shop_id           BIGINT                                   NULL COMMENT '店铺ID',
    order_type        INT                                      NOT NULL COMMENT '订单类型:  0-认养 1-普通电商',
    total_amount      DECIMAL(10, 2)                           NOT NULL COMMENT '订单总额',
    actual_pay_amount DECIMAL(10, 2)                           NOT NULL COMMENT '实付金额',
    freight_amount    DECIMAL(10, 2) DEFAULT 0.00              NULL COMMENT '运费',
    discount_amount   DECIMAL(10, 2) DEFAULT 0.00              NULL COMMENT '优惠金额',
    order_status      INT                                      NOT NULL COMMENT '订单状态',
    receive_name      VARCHAR(32)                              NULL COMMENT '收货人',
    receive_phone     VARCHAR(20)                              NULL COMMENT '收货电话',
    province_name     VARCHAR(50)                              NULL COMMENT '省份名称',
    city_name         VARCHAR(50)                              NULL COMMENT '城市名称',
    district_name     VARCHAR(50)                              NULL COMMENT '区县名称',
    detail_address    VARCHAR(255)                             NULL COMMENT '收货地址',
    logistics_no      VARCHAR(64)                              NULL COMMENT '物流单号',
    logistics_company VARCHAR(64)                              NULL COMMENT '物流公司',
    delivery_time     DATETIME                                 NULL COMMENT '发货时间',
    receive_time      DATETIME                                 NULL COMMENT '收货时间',
    create_time       DATETIME       DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    update_time       DATETIME       DEFAULT CURRENT_TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag          INT            DEFAULT 0                 NULL COMMENT '是否删除',
    CONSTRAINT uk_order_no UNIQUE (order_no),
    INDEX idx_pay_order_no (pay_order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) COMMENT '订单表' CHARSET = utf8mb4;

CREATE TABLE t_order_3
(
    id                BIGINT                                   NOT NULL COMMENT '主键ID' PRIMARY KEY,
    order_no          VARCHAR(128)                             NOT NULL COMMENT '业务订单号',
    pay_order_no      VARCHAR(64)                              NULL COMMENT '支付单号',
    user_id           BIGINT                                   NOT NULL COMMENT '用户ID',
    shop_id           BIGINT                                   NULL COMMENT '店铺ID',
    order_type        INT                                      NOT NULL COMMENT '订单类型:  0-认养 1-普通电商',
    total_amount      DECIMAL(10, 2)                           NOT NULL COMMENT '订单总额',
    actual_pay_amount DECIMAL(10, 2)                           NOT NULL COMMENT '实付金额',
    freight_amount    DECIMAL(10, 2) DEFAULT 0.00              NULL COMMENT '运费',
    discount_amount   DECIMAL(10, 2) DEFAULT 0.00              NULL COMMENT '优惠金额',
    order_status      INT                                      NOT NULL COMMENT '订单状态',
    receive_name      VARCHAR(32)                              NULL COMMENT '收货人',
    receive_phone     VARCHAR(20)                              NULL COMMENT '收货电话',
    province_name     VARCHAR(50)                              NULL COMMENT '省份名称',
    city_name         VARCHAR(50)                              NULL COMMENT '城市名称',
    district_name     VARCHAR(50)                              NULL COMMENT '区县名称',
    detail_address    VARCHAR(255)                             NULL COMMENT '收货地址',
    logistics_no      VARCHAR(64)                              NULL COMMENT '物流单号',
    logistics_company VARCHAR(64)                              NULL COMMENT '物流公司',
    delivery_time     DATETIME                                 NULL COMMENT '发货时间',
    receive_time      DATETIME                                 NULL COMMENT '收货时间',
    create_time       DATETIME       DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    update_time       DATETIME       DEFAULT CURRENT_TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag          INT            DEFAULT 0                 NULL COMMENT '是否删除',
    CONSTRAINT uk_order_no UNIQUE (order_no),
    INDEX idx_pay_order_no (pay_order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) COMMENT '订单表' CHARSET = utf8mb4;

CREATE TABLE t_order_4
(
    id                BIGINT                                   NOT NULL COMMENT '主键ID' PRIMARY KEY,
    order_no          VARCHAR(128)                             NOT NULL COMMENT '业务订单号',
    pay_order_no      VARCHAR(64)                              NULL COMMENT '支付单号',
    user_id           BIGINT                                   NOT NULL COMMENT '用户ID',
    shop_id           BIGINT                                   NULL COMMENT '店铺ID',
    order_type        INT                                      NOT NULL COMMENT '订单类型:  0-认养 1-普通电商',
    total_amount      DECIMAL(10, 2)                           NOT NULL COMMENT '订单总额',
    actual_pay_amount DECIMAL(10, 2)                           NOT NULL COMMENT '实付金额',
    freight_amount    DECIMAL(10, 2) DEFAULT 0.00              NULL COMMENT '运费',
    discount_amount   DECIMAL(10, 2) DEFAULT 0.00              NULL COMMENT '优惠金额',
    order_status      INT                                      NOT NULL COMMENT '订单状态',
    receive_name      VARCHAR(32)                              NULL COMMENT '收货人',
    receive_phone     VARCHAR(20)                              NULL COMMENT '收货电话',
    province_name     VARCHAR(50)                              NULL COMMENT '省份名称',
    city_name         VARCHAR(50)                              NULL COMMENT '城市名称',
    district_name     VARCHAR(50)                              NULL COMMENT '区县名称',
    detail_address    VARCHAR(255)                             NULL COMMENT '收货地址',
    logistics_no      VARCHAR(64)                              NULL COMMENT '物流单号',
    logistics_company VARCHAR(64)                              NULL COMMENT '物流公司',
    delivery_time     DATETIME                                 NULL COMMENT '发货时间',
    receive_time      DATETIME                                 NULL COMMENT '收货时间',
    create_time       DATETIME       DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    update_time       DATETIME       DEFAULT CURRENT_TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag          INT            DEFAULT 0                 NULL COMMENT '是否删除',
    CONSTRAINT uk_order_no UNIQUE (order_no),
    INDEX idx_pay_order_no (pay_order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) COMMENT '订单表' CHARSET = utf8mb4;

CREATE TABLE t_order_5
(
    id                BIGINT                                   NOT NULL COMMENT '主键ID' PRIMARY KEY,
    order_no          VARCHAR(128)                             NOT NULL COMMENT '业务订单号',
    pay_order_no      VARCHAR(64)                              NULL COMMENT '支付单号',
    user_id           BIGINT                                   NOT NULL COMMENT '用户ID',
    shop_id           BIGINT                                   NULL COMMENT '店铺ID',
    order_type        INT                                      NOT NULL COMMENT '订单类型:  0-认养 1-普通电商',
    total_amount      DECIMAL(10, 2)                           NOT NULL COMMENT '订单总额',
    actual_pay_amount DECIMAL(10, 2)                           NOT NULL COMMENT '实付金额',
    freight_amount    DECIMAL(10, 2) DEFAULT 0.00              NULL COMMENT '运费',
    discount_amount   DECIMAL(10, 2) DEFAULT 0.00              NULL COMMENT '优惠金额',
    order_status      INT                                      NOT NULL COMMENT '订单状态',
    receive_name      VARCHAR(32)                              NULL COMMENT '收货人',
    receive_phone     VARCHAR(20)                              NULL COMMENT '收货电话',
    province_name     VARCHAR(50)                              NULL COMMENT '省份名称',
    city_name         VARCHAR(50)                              NULL COMMENT '城市名称',
    district_name     VARCHAR(50)                              NULL COMMENT '区县名称',
    detail_address    VARCHAR(255)                             NULL COMMENT '收货地址',
    logistics_no      VARCHAR(64)                              NULL COMMENT '物流单号',
    logistics_company VARCHAR(64)                              NULL COMMENT '物流公司',
    delivery_time     DATETIME                                 NULL COMMENT '发货时间',
    receive_time      DATETIME                                 NULL COMMENT '收货时间',
    create_time       DATETIME       DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    update_time       DATETIME       DEFAULT CURRENT_TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag          INT            DEFAULT 0                 NULL COMMENT '是否删除',
    CONSTRAINT uk_order_no UNIQUE (order_no),
    INDEX idx_pay_order_no (pay_order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) COMMENT '订单表' CHARSET = utf8mb4;

CREATE TABLE t_order_6
(
    id                BIGINT                                   NOT NULL COMMENT '主键ID' PRIMARY KEY,
    order_no          VARCHAR(128)                             NOT NULL COMMENT '业务订单号',
    pay_order_no      VARCHAR(64)                              NULL COMMENT '支付单号',
    user_id           BIGINT                                   NOT NULL COMMENT '用户ID',
    shop_id           BIGINT                                   NULL COMMENT '店铺ID',
    order_type        INT                                      NOT NULL COMMENT '订单类型:  0-认养 1-普通电商',
    total_amount      DECIMAL(10, 2)                           NOT NULL COMMENT '订单总额',
    actual_pay_amount DECIMAL(10, 2)                           NOT NULL COMMENT '实付金额',
    freight_amount    DECIMAL(10, 2) DEFAULT 0.00              NULL COMMENT '运费',
    discount_amount   DECIMAL(10, 2) DEFAULT 0.00              NULL COMMENT '优惠金额',
    order_status      INT                                      NOT NULL COMMENT '订单状态',
    receive_name      VARCHAR(32)                              NULL COMMENT '收货人',
    receive_phone     VARCHAR(20)                              NULL COMMENT '收货电话',
    province_name     VARCHAR(50)                              NULL COMMENT '省份名称',
    city_name         VARCHAR(50)                              NULL COMMENT '城市名称',
    district_name     VARCHAR(50)                              NULL COMMENT '区县名称',
    detail_address    VARCHAR(255)                             NULL COMMENT '收货地址',
    logistics_no      VARCHAR(64)                              NULL COMMENT '物流单号',
    logistics_company VARCHAR(64)                              NULL COMMENT '物流公司',
    delivery_time     DATETIME                                 NULL COMMENT '发货时间',
    receive_time      DATETIME                                 NULL COMMENT '收货时间',
    create_time       DATETIME       DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    update_time       DATETIME       DEFAULT CURRENT_TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag          INT            DEFAULT 0                 NULL COMMENT '是否删除',
    CONSTRAINT uk_order_no UNIQUE (order_no),
    INDEX idx_pay_order_no (pay_order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) COMMENT '订单表' CHARSET = utf8mb4;

CREATE TABLE t_order_7
(
    id                BIGINT                                   NOT NULL COMMENT '主键ID' PRIMARY KEY,
    order_no          VARCHAR(128)                             NOT NULL COMMENT '业务订单号',
    pay_order_no      VARCHAR(64)                              NULL COMMENT '支付单号',
    user_id           BIGINT                                   NOT NULL COMMENT '用户ID',
    shop_id           BIGINT                                   NULL COMMENT '店铺ID',
    order_type        INT                                      NOT NULL COMMENT '订单类型:  0-认养 1-普通电商',
    total_amount      DECIMAL(10, 2)                           NOT NULL COMMENT '订单总额',
    actual_pay_amount DECIMAL(10, 2)                           NOT NULL COMMENT '实付金额',
    freight_amount    DECIMAL(10, 2) DEFAULT 0.00              NULL COMMENT '运费',
    discount_amount   DECIMAL(10, 2) DEFAULT 0.00              NULL COMMENT '优惠金额',
    order_status      INT                                      NOT NULL COMMENT '订单状态',
    receive_name      VARCHAR(32)                              NULL COMMENT '收货人',
    receive_phone     VARCHAR(20)                              NULL COMMENT '收货电话',
    province_name     VARCHAR(50)                              NULL COMMENT '省份名称',
    city_name         VARCHAR(50)                              NULL COMMENT '城市名称',
    district_name     VARCHAR(50)                              NULL COMMENT '区县名称',
    detail_address    VARCHAR(255)                             NULL COMMENT '收货地址',
    logistics_no      VARCHAR(64)                              NULL COMMENT '物流单号',
    logistics_company VARCHAR(64)                              NULL COMMENT '物流公司',
    delivery_time     DATETIME                                 NULL COMMENT '发货时间',
    receive_time      DATETIME                                 NULL COMMENT '收货时间',
    create_time       DATETIME       DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    update_time       DATETIME       DEFAULT CURRENT_TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag          INT            DEFAULT 0                 NULL COMMENT '是否删除',
    CONSTRAINT uk_order_no UNIQUE (order_no),
    INDEX idx_pay_order_no (pay_order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) COMMENT '订单表' CHARSET = utf8mb4;

create table t_order_detail_adopt
(
    id            bigint                             not null comment '主键ID'
        primary key,
    order_no      varchar(128)                       not null comment '订单号',
    adopt_item_id bigint(128)                        not null comment '认养项目ID',
    item_name     varchar(128)                       not null comment '项目名称',
    item_image    varchar(255)                       null comment '项目图片',
    price         decimal(10, 2)                     not null comment '认养单价',
    quantity      int                                not null comment '认养数量',
    total_amount  decimal(10, 2)                     not null comment '总金额',
    start_date    datetime                           null comment '开始时间',
    end_date      datetime                           null comment '结束时间',
    create_time   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    del_flag      int      default 0                 null comment '是否删除',
    INDEX idx_order_no (order_no)
)
    comment '认养订单明细表' charset = utf8mb4;

create table t_order_detail_sku_0
(
    id           bigint                             not null comment '主键ID' primary key,
    order_no     varchar(64)                        null comment '订单号',
    sku_id       bigint                             not null comment 'SKU ID',
    spu_id       bigint                             null comment 'SPU ID',
    sku_name     varchar(128)                       not null comment '商品名称',
    sku_image    varchar(255)                       null comment '商品图片',
    sku_specs    varchar(512)                       null comment '规格JSON',
    price        decimal(10, 2)                     not null comment '单价',
    quantity     int                                not null comment '数量',
    total_amount decimal(10, 2)                     not null comment '总金额',
    create_time  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    del_flag     int      default 0                 null comment '是否删除',
    index idx_order_no (order_no)
)
    comment '普通商品明细表' charset = utf8mb4;

create table t_order_detail_sku_1
(
    id           bigint                             not null comment '主键ID' primary key,
    order_no     varchar(64)                        null comment '订单号',
    sku_id       bigint                             not null comment 'SKU ID',
    spu_id       bigint                             null comment 'SPU ID',
    sku_name     varchar(128)                       not null comment '商品名称',
    sku_image    varchar(255)                       null comment '商品图片',
    sku_specs    varchar(512)                       null comment '规格JSON',
    price        decimal(10, 2)                     not null comment '单价',
    quantity     int                                not null comment '数量',
    total_amount decimal(10, 2)                     not null comment '总金额',
    create_time  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    del_flag     int      default 0                 null comment '是否删除',
    index idx_order_no (order_no)
)
    comment '普通商品明细表' charset = utf8mb4;

create table t_order_detail_sku_2
(
    id           bigint                             not null comment '主键ID' primary key,
    order_no     varchar(64)                        null comment '订单号',
    sku_id       bigint                             not null comment 'SKU ID',
    spu_id       bigint                             null comment 'SPU ID',
    sku_name     varchar(128)                       not null comment '商品名称',
    sku_image    varchar(255)                       null comment '商品图片',
    sku_specs    varchar(512)                       null comment '规格JSON',
    price        decimal(10, 2)                     not null comment '单价',
    quantity     int                                not null comment '数量',
    total_amount decimal(10, 2)                     not null comment '总金额',
    create_time  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    del_flag     int      default 0                 null comment '是否删除',
    index idx_order_no (order_no)
)
    comment '普通商品明细表' charset = utf8mb4;

create table t_order_detail_sku_3
(
    id           bigint                             not null comment '主键ID' primary key,
    order_no     varchar(64)                        null comment '订单号',
    sku_id       bigint                             not null comment 'SKU ID',
    spu_id       bigint                             null comment 'SPU ID',
    sku_name     varchar(128)                       not null comment '商品名称',
    sku_image    varchar(255)                       null comment '商品图片',
    sku_specs    varchar(512)                       null comment '规格JSON',
    price        decimal(10, 2)                     not null comment '单价',
    quantity     int                                not null comment '数量',
    total_amount decimal(10, 2)                     not null comment '总金额',
    create_time  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    del_flag     int      default 0                 null comment '是否删除',
    index idx_order_no (order_no)
)
    comment '普通商品明细表' charset = utf8mb4;

create table t_order_detail_sku_4
(
    id           bigint                             not null comment '主键ID' primary key,
    order_no     varchar(64)                        null comment '订单号',
    sku_id       bigint                             not null comment 'SKU ID',
    spu_id       bigint                             null comment 'SPU ID',
    sku_name     varchar(128)                       not null comment '商品名称',
    sku_image    varchar(255)                       null comment '商品图片',
    sku_specs    varchar(512)                       null comment '规格JSON',
    price        decimal(10, 2)                     not null comment '单价',
    quantity     int                                not null comment '数量',
    total_amount decimal(10, 2)                     not null comment '总金额',
    create_time  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    del_flag     int      default 0                 null comment '是否删除',
    index idx_order_no (order_no)
)
    comment '普通商品明细表' charset = utf8mb4;

create table t_order_detail_sku_5
(
    id           bigint                             not null comment '主键ID' primary key,
    order_no     varchar(64)                        null comment '订单号',
    sku_id       bigint                             not null comment 'SKU ID',
    spu_id       bigint                             null comment 'SPU ID',
    sku_name     varchar(128)                       not null comment '商品名称',
    sku_image    varchar(255)                       null comment '商品图片',
    sku_specs    varchar(512)                       null comment '规格JSON',
    price        decimal(10, 2)                     not null comment '单价',
    quantity     int                                not null comment '数量',
    total_amount decimal(10, 2)                     not null comment '总金额',
    create_time  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    del_flag     int      default 0                 null comment '是否删除',
    index idx_order_no (order_no)
)
    comment '普通商品明细表' charset = utf8mb4;

create table t_order_detail_sku_6
(
    id           bigint                             not null comment '主键ID' primary key,
    order_no     varchar(64)                        null comment '订单号',
    sku_id       bigint                             not null comment 'SKU ID',
    spu_id       bigint                             null comment 'SPU ID',
    sku_name     varchar(128)                       not null comment '商品名称',
    sku_image    varchar(255)                       null comment '商品图片',
    sku_specs    varchar(512)                       null comment '规格JSON',
    price        decimal(10, 2)                     not null comment '单价',
    quantity     int                                not null comment '数量',
    total_amount decimal(10, 2)                     not null comment '总金额',
    create_time  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    del_flag     int      default 0                 null comment '是否删除',
    index idx_order_no (order_no)
)
    comment '普通商品明细表' charset = utf8mb4;

create table t_order_detail_sku_7
(
    id           bigint                             not null comment '主键ID' primary key,
    order_no     varchar(64)                        null comment '订单号',
    sku_id       bigint                             not null comment 'SKU ID',
    spu_id       bigint                             null comment 'SPU ID',
    sku_name     varchar(128)                       not null comment '商品名称',
    sku_image    varchar(255)                       null comment '商品图片',
    sku_specs    varchar(512)                       null comment '规格JSON',
    price        decimal(10, 2)                     not null comment '单价',
    quantity     int                                not null comment '数量',
    total_amount decimal(10, 2)                     not null comment '总金额',
    create_time  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    del_flag     int      default 0                 null comment '是否删除',
    index idx_order_no (order_no)
)
    comment '普通商品明细表' charset = utf8mb4;

CREATE TABLE t_order_sku_review
(
    id                   BIGINT                             NOT NULL PRIMARY KEY,
    order_no             VARCHAR(64)                        NOT NULL,
    order_detail_sku_id  BIGINT                             NOT NULL,
    spu_id               BIGINT                             NOT NULL,
    sku_id               BIGINT                             NOT NULL,
    shop_id              BIGINT                             NOT NULL,
    user_id              BIGINT                             NOT NULL,
    score                TINYINT                            NOT NULL,
    content              VARCHAR(1000)                      NULL,
    image_urls           VARCHAR(2000)                      NULL,
    user_name_snapshot   VARCHAR(128)                       NULL,
    user_avatar_snapshot VARCHAR(255)                       NULL,
    create_time          DATETIME DEFAULT CURRENT_TIMESTAMP NULL,
    update_time          DATETIME DEFAULT CURRENT_TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    del_flag             INT      DEFAULT 0                 NULL,
    UNIQUE INDEX uk_order_detail_sku_id (order_detail_sku_id),
    INDEX idx_order_no (order_no),
    INDEX idx_spu_create_time (spu_id, create_time),
    INDEX idx_user_create_time (user_id, create_time)
)
    CHARSET = utf8mb4;
