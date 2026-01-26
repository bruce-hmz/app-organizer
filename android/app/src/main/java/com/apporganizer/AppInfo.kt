package com.apporganizer

import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable

/**
 * 应用信息数据类
 */
data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val categories: MutableList<AppCategory> = mutableListOf()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        null, // icon需要重新加载
        ArrayList<AppCategory>().apply {
            val categoryNames = parcel.createStringArrayList() ?: emptyList()
            addAll(categoryNames.map { AppCategory.fromString(it) })
        }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(packageName)
        parcel.writeString(appName)
        parcel.writeStringList(categories.map { it.name })
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<AppInfo> {
        override fun createFromParcel(parcel: Parcel): AppInfo = AppInfo(parcel)
        override fun newArray(size: Int): Array<AppInfo?> = arrayOfNulls(size)
    }
}

/**
 * 应用分类枚举
 */
enum class AppCategory(val displayName: String) {
    ALL("全部"),
    SOCIAL("社交通讯"),
    WORK("办公商务"),
    OFFICE("办公学习"),
    ENTERTAINMENT("影音娱乐"),
    TOOLS("实用工具"),
    SHOPPING("购物支付"),
    FINANCE("金融理财"),
    EDUCATION("教育学习"),
    TRAVEL("出行旅游"),
    HEALTH("运动健康"),
    NEWS("新闻资讯"),
    PHOTO("拍照摄影"),
    MUSIC("音乐视频"),
    GAME("游戏娱乐"),
    SYSTEM("系统工具"),
    OTHER("其他应用");

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

/**
 * 整理偏好方案
 */
enum class OrganizePreference(val displayName: String, val description: String) {
    GENERAL("常规方案", "按功能分类，适合大多数用户"),
    WORK("工作优先", "突出办公、商务类应用"),
    ENTERTAINMENT("娱乐优先", "突出影音、游戏、社交类应用"),
    SIMPLE("极简方案", "只创建少量核心文件夹");
    
    /**
     * 根据偏好过滤分类
     */
    fun getPreferredCategories(): List<AppCategory> {
        return when (this) {
            GENERAL -> listOf(
                AppCategory.SOCIAL, AppCategory.OFFICE, AppCategory.ENTERTAINMENT,
                AppCategory.MUSIC, AppCategory.SHOPPING, AppCategory.FINANCE,
                AppCategory.TRAVEL, AppCategory.TOOLS, AppCategory.PHOTO,
                AppCategory.GAME, AppCategory.EDUCATION, AppCategory.HEALTH, AppCategory.NEWS
            )
            WORK -> listOf(
                AppCategory.OFFICE, AppCategory.FINANCE, AppCategory.TOOLS,
                AppCategory.SOCIAL, AppCategory.NEWS, AppCategory.EDUCATION
            )
            ENTERTAINMENT -> listOf(
                AppCategory.ENTERTAINMENT, AppCategory.MUSIC, AppCategory.GAME,
                AppCategory.SOCIAL, AppCategory.PHOTO, AppCategory.SHOPPING
            )
            SIMPLE -> listOf(
                AppCategory.SOCIAL, AppCategory.TOOLS, AppCategory.ENTERTAINMENT,
                AppCategory.SHOPPING
            )
        }
    }
}
