package com.vv.cloudfarming.framework.storage.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@Getter
@AllArgsConstructor
public enum UploadTypeEnum {

    // 商品SPU封面
    PRODUCT_SPU_COVER(
            "PRODUCT_SPU_COVER",
            "/product/spu/cover/",
            Set.of(1, 2)
    ),

    // 商品SPU详情图
    PRODUCT_SPU_DETAIL(
            "PRODUCT_SPU_DETAIL",
            "/product/spu/detail/",
            Set.of(1, 2)
    ),

    // 认养项目封面
    ADOPT_ITEM_COVER(
            "ADOPT_ITEM_COVER",
            "/adopt/item/cover/",
            Set.of(1, 2)
    ),

    // 认养项目详情图
    ADOPT_ITEM_DETAIL(
            "ADOPT_ITEM_DETAIL",
            "/adopt/item/detail/",
            Set.of(1, 2)
    ),

    // 认养动物档案图
    ANIMAL_PROFILE(
            "ANIMAL_PROFILE",
            "/adopt/animal/profile/",
            Set.of(1, 2)
    ),

    // 生长日记图片
    ANIMAL_DIARY(
            "ANIMAL_DIARY",
            "/adopt/animal/diary/",
            Set.of(1, 2)
    ),

    // 生长日记视频
    ANIMAL_DIARY_VIDEO(
            "ANIMAL_DIARY_VIDEO",
            "/adopt/animal/diary/video/",
            Set.of(1, 2)
    ),

    // 商品评价图片
    PRODUCT_REVIEW(
            "PRODUCT_REVIEW",
            "/order/review/",
            Set.of(0, 1, 2)
    ),

    // 广告
    BANNER(
            "BANNER",
            "/operation/banner/",
            Set.of(2)
    ),

    // 用户头像
    USER_AVATAR(
            "USER_AVATAR",
            "/user/avatar/",
            Set.of(0, 1, 2)
    ),

    // 文章图片
    ARTICLE_IMAGE(
            "ARTICLE_IMAGE",
            "/article/",
            Set.of(2)
    ),

    // 临时内容
    TEMP(
            "TEMP",
            "/temp/",
            Set.of(0, 1, 2)
    );

    /**
     * 前端传入的业务码
     */
    private final String code;

    /**
     * 存储路径前缀
     */
    private final String pathPrefix;

    /**
     * 允许上传的角色
     */
    private Set<Integer> needRoles;

    public static UploadTypeEnum getEnumByCode(String code) {
        for (UploadTypeEnum uploadTypeEnum : UploadTypeEnum.values()) {
            if (uploadTypeEnum.getCode().equals(code)) {
                return uploadTypeEnum;
            }
        }
        return null;
    }
}
