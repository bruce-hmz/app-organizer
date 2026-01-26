package com.apporganizer

/**
 * 应用分类器 - 根据包名自动分类
 */
object AppClassifier {
    
    // 分类规则：包名关键词映射
    private val categoryRules = mapOf(
        AppCategory.SOCIAL to listOf(
            "wechat", "weixin", "tencent.mm", "tencent.mobileqq",
            "sina.weibo", "zhihu", "douban", "momo", "tantan",
            "xingin.xhs", "kuaishou", "ss.android.ugc", // 小红书、快手、抖音
            "facebook", "twitter", "instagram", "whatsapp", "telegram",
            "line", "kakao", "snapchat", "tiktok", "discord"
        ),
        
        AppCategory.WORK to listOf(
            "alibaba.android.rimet", "tencent.wework", "feishu", "lark",
            "dingtalk", "office", "microsoft", "zoom", "slack",
            "wps", "evernote", "notion", "trello", "asana",
            "teams", "webex", "citrix", "teamviewer"
        ),
        
        AppCategory.ENTERTAINMENT to listOf(
            "qiyi.video", "tencent.qqlive", "youku", "bilibili",
            "netease.cloudmusic", "tencent.qqmusic", "kugou", "kuwo",
            "spotify", "youtube", "netflix", "hulu", "disney",
            "iqiyi", "mgtv", "pptv", "sohu.sohuvideo"
        ),
        
        AppCategory.SHOPPING to listOf(
            "taobao", "tmall", "jd", "jingdong", "pinduoduo",
            "vipshop", "suning", "gome", "dangdang", "amazon",
            "sankuai.meituan", "dianping", "ele.me", "eleme",
            "dewu", "xiaohongshu", "alibaba", "shopee"
        ),
        
        AppCategory.FINANCE to listOf(
            "alipay", "eg.android.AlipayGphone", "tencent.mm.plugin.wallet",
            "unionpay", "icbc", "ccb", "abc", "boc", "bank",
            "cmb", "cmbchina", "spdb", "cib", "ceb",
            "hsbc", "paypal", "venmo", "cashapp", "chase"
        ),
        
        AppCategory.TOOLS to listOf(
            "autonavi.minimap", "baidu.BaiduMap", "compass",
            "baidu.netdisk", "tencent.mtt", "uc.browser",
            "android.camera", "gallery", "miui.notes", "calculator",
            "cleanmaster", "security", "flashlight", "weather",
            "vpn", "password", "file.manager", "scanner"
        ),
        
        AppCategory.GAMES to listOf(
            "tencent.tmgp", "game", "play", "王者", "pubg",
            "mihoyo", "supercell", "mojang", "rockstar",
            "unity", "unreal", "steam", "epicgames"
        ),
        
        AppCategory.SYSTEM to listOf(
            "android.systemui", "android.settings", "android.launcher",
            "android.providers", "miui.home", "huawei.android.launcher",
            "oppo.launcher", "vivo.launcher", "samsung.android.app.launcher",
            "google.android", "android.vending", "packageinstaller"
        )
    )
    
    /**
     * 根据包名自动分类应用
     */
    fun classify(packageName: String, appName: String): List<AppCategory> {
        val categories = mutableListOf<AppCategory>()
        val lowerPackage = packageName.toLowerCase()
        val lowerName = appName.toLowerCase()
        
        // 检查每个分类规则
        for ((category, keywords) in categoryRules) {
            if (keywords.any { lowerPackage.contains(it) || lowerName.contains(it) }) {
                categories.add(category)
            }
        }
        
        // 如果没有匹配到任何分类，归为"其他"
        if (categories.isEmpty()) {
            categories.add(AppCategory.OTHER)
        }
        
        return categories
    }
    
    /**
     * 判断是否为系统应用（需要隐藏的）
     */
    fun isSystemApp(packageName: String): Boolean {
        val systemPackages = listOf(
            "com.android.systemui",
            "com.android.settings",
            "com.android.providers",
            "com.android.packageinstaller",
            "com.google.android.gms",
            "com.google.android.gsf"
        )
        return systemPackages.any { packageName.startsWith(it) }
    }
}
