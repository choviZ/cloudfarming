package com.vv.cloudfarming.shop.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 动物分类枚举
 */
@Getter
@AllArgsConstructor
public enum AnimalCategoryEnum {

    /* ===== 家畜类 ===== */
    PIG("pig", "生猪"),
    CATTLE("cattle", "牛"),
    SHEEP("sheep", "羊"),
    GOAT("goat", "山羊"),
    RABBIT("rabbit", "兔"),

    /* ===== 家禽类 ===== */
    CHICKEN("chicken", "鸡"),
    DUCK("duck", "鸭"),
    GOOSE("goose", "鹅"),
    TURKEY("turkey", "火鸡"),
    QUAIL("quail", "鹌鹑"),

    /* ===== 水产类 ===== */
    FISH("fish", "鱼类"),
    SHRIMP("shrimp", "虾"),
    CRAB("crab", "蟹"),
    LOBSTER("lobster", "龙虾"),
    EEL("eel", "鳗鱼"),

    /* ===== 特色 / 经济养殖 ===== */
    DEER("deer", "梅花鹿"),
    DONKEY("donkey", "驴"),
    OSTRICH("ostrich", "鸵鸟"),
    PIGEON("pigeon", "鸽子"),
    BEE("bee", "蜜蜂"),

    /* ===== 备用兜底 ===== */
    OTHER("other", "其他");

    private final String code;
    private final String name;
}
