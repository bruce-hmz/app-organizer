package com.apporganizer

import android.graphics.drawable.Drawable

/**
 * 应用信息数据类
 */
data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable,
    val categories: MutableList<AppCategory> = mutableListOf()
)

/**
 * 应用分类枚举
 */
enum class AppCategory(val displayName: String) {
    ALL("全部"),
    SOCIAL("社交"),
    WORK("办公"),
    ENTERTAINMENT("娱乐"),
    TOOLS("工具"),
    SHOPPING("购物"),
    FINANCE("金融"),
    GAMES("游戏"),
    SYSTEM("系统"),
    OTHER("其他");

    companion object {
        fun fromString(value: String): AppCategory {
            return values().find { it.name == value } ?: OTHER
        }
    }
}

/**
 * 品牌UI风格
 */
enum class BrandStyle(val displayName: String, val spanCount: Int, val useListLayout: Boolean) {
    XIAOMI("小米", 3, false),
    HUAWEI("华为", 2, false),
    OPPO("OPPO", 1, true),
    VIVO("vivo", 4, false),
    IPHONE("iPhone", 3, false)
}
