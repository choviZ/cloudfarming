package com.vv.cloudfarming.framework.storage.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@Getter
@AllArgsConstructor
public enum UploadTypeEnum {

    // spu封面
    PRODUCT_SPU_COVER(
            "PRODUCT_SPU_COVER",
            "/product/spu/cover/",
            Set.of(1, 2)
    ),

    // spu详情图片
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

    // 认养项目详情图片
    ADOPT_ITEM_DETAIL(
            "ADOPT_ITEM_DETAIL",
            "/adopt/item/detail/",
            Set.of(1, 2)
    ),

    // 认养动物图片
    ANIMAL_PROFILE(
            "ANIMAL_PROFILE",
            "/adopt/animal/profile/",
            Set.of(1, 2)
    ),

    // 认养动物日记记录图片
    ANIMAL_DIARY(
            "ANIMAL_DIARY",
            "/adopt/animal/diary/",
            Set.of(1, 2)
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

    // 文章中的图片
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
     * 前端传入的类型
     */
    private final String code;

    /**
     * 存储路径前缀
     */
    private final String pathPrefix;

    /**
     * 需要的角色
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
