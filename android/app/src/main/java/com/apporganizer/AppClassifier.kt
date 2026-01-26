package com.apporganizer

/**
 * 应用分类器 - 根据包名和应用名自动分类
 */
object AppClassifier {
    
    // 分类规则：包名和应用名关键词映射
    private val categoryRules = mapOf(
        AppCategory.SOCIAL to listOf(
            "wechat", "weixin", "tencent.mm", "tencent.mobileqq", "qq",
            "sina.weibo", "zhihu", "douban", "momo", "tantan", "soul",
            "xingin.xhs", "kuaishou", "ss.android.ugc", // 小红书、快手、抖音
            "facebook", "twitter", "instagram", "whatsapp", "telegram",
            "line", "kakao", "snapchat", "tiktok", "discord", "skype",
            "微信", "QQ", "微博", "知乎", "豆瓣", "陌陌", "探探", "Soul"
        ),
        
        AppCategory.OFFICE to listOf(
            "alibaba.android.rimet", "tencent.wework", "feishu", "lark",
            "dingtalk", "office", "microsoft", "zoom", "slack",
            "wps", "evernote", "notion", "trello", "asana",
            "teams", "webex", "citrix", "teamviewer",
            "钉钉", "企业微信", "飞书", "印象笔记", "有道云", "石墨文档"
        ),
        
        AppCategory.ENTERTAINMENT to listOf(
            "qiyi.video", "tencent.qqlive", "youku", "bilibili",
            "spotify", "youtube", "netflix", "hulu", "disney",
            "iqiyi", "mgtv", "pptv", "sohu.sohuvideo", "xigua",
            "爱奇艺", "腾讯视频", "优酷", "哔哩哔哩", "芒果TV", "西瓜视频"
        ),
        
        AppCategory.MUSIC to listOf(
            "netease.cloudmusic", "tencent.qqmusic", "kugou", "kuwo",
            "spotify", "music", "radio", "podcast",
            "网易云音乐", "QQ音乐", "酷狗音乐", "酷我音乐", "喜马拉雅", "荔枝FM"
        ),
        
        AppCategory.SHOPPING to listOf(
            "taobao", "tmall", "jd", "jingdong", "pinduoduo",
            "vipshop", "suning", "gome", "dangdang", "amazon",
            "sankuai.meituan", "dianping", "ele.me", "eleme",
            "dewu", "xiaohongshu", "alibaba", "shopee",
            "淘宝", "天猫", "京东", "拼多多", "美团", "大众点评", "饿了么", "得物"
        ),
        
        AppCategory.FINANCE to listOf(
            "alipay", "eg.android.AlipayGphone", "tencent.mm.plugin.wallet",
            "unionpay", "icbc", "ccb", "abc", "boc", "bank", "banking",
            "cmb", "cmbchina", "spdb", "cib", "ceb",
            "hsbc", "paypal", "venmo", "cashapp", "chase",
            "支付宝", "银行", "理财", "证券", "股票", "基金"
        ),
        
        AppCategory.TRAVEL to listOf(
            "autonavi.minimap", "baidu.BaiduMap", "didi", "uber",
            "ctrip", "qunar", "tuniu", "mafengwo", "airbnb",
            "12306", "railway", "flight", "hotel",
            "高德地图", "百度地图", "滴滴", "携程", "去哪儿", "飞猪", "12306"
        ),
        
        AppCategory.HEALTH to listOf(
            "sport", "fitness", "health", "medical", "hospital",
            "keep", "nike.training", "step", "pedometer", "workout",
            "运动", "健康", "健身", "医疗", "医院", "Keep", "步数"
        ),
        
        AppCategory.NEWS to listOf(
            "toutiao", "news", "reader", "rss", "feed",
            "jinri.toutiao", "tencent.news", "netease.newsreader",
            "今日头条", "新闻", "资讯", "阅读器"
        ),
        
        AppCategory.PHOTO to listOf(
            "camera", "photo", "gallery", "picture", "image",
            "meitu", "meituxiuxiu", "vsco", "snapseed", "lightroom",
            "相机", "相册", "照片", "美图", "图片"
        ),
        
        AppCategory.EDUCATION to listOf(
            "education", "learn", "study", "school", "course",
            "yuanfudao", "zuoyebang", "duolingo", "khan",
            "教育", "学习", "课程", "作业", "猿辅导", "作业帮", "学而思"
        ),
        
        AppCategory.TOOLS to listOf(
            "baidu.netdisk", "tencent.mtt", "uc.browser", "chrome",
            "calculator", "cleanmaster", "security", "flashlight",
            "weather", "vpn", "password", "file.manager", "scanner",
            "compress", "translate", "input",
            "百度网盘", "浏览器", "文件", "扫描", "天气", "计算器", "输入法", "翻译"
        ),
        
        AppCategory.GAME to listOf(
            "tencent.tmgp", "game", "play", "王者", "pubg", "吃鸡",
            "mihoyo", "supercell", "mojang", "rockstar",
            "unity", "unreal", "steam", "epicgames",
            "游戏", "原神", "王者荣耀", "和平精英"
        ),
        
        AppCategory.SYSTEM to listOf(
            "android.systemui", "android.settings", "android.launcher",
            "android.providers", "miui.home", "huawei.android.launcher",
            "oppo.launcher", "vivo.launcher", "samsung.android.app.launcher",
            "google.android", "android.vending", "packageinstaller"
        )
    )
    
    /**
     * 根据包名和应用名自动分类
     */
    fun classify(packageName: String, appName: String): List<AppCategory> {
        val categories = mutableListOf<AppCategory>()
        val lowerPackage = packageName.lowercase()
        val lowerName = appName.lowercase()
        
        // 检查每个分类规则
        for ((category, keywords) in categoryRules) {
            if (keywords.any { keyword ->
                lowerPackage.contains(keyword) || lowerName.contains(keyword)
            }) {
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
     * 判断是否为系统核心应用（需要隐藏的）
     */
    fun isSystemApp(packageName: String): Boolean {
        // 只过滤最核心的系统应用，其他都保留
        val systemPackages = listOf(
            "com.android.systemui",
            "com.android.settings",
            "com.android.providers.settings",
            "com.android.providers.calendar",
            "com.android.providers.contacts",
            "com.android.providers.media",
            "com.android.providers.telephony",
            "com.android.packageinstaller",
            "com.google.android.gms.policy",
            "com.google.android.gsf"
        )
        // 完全匹配才过滤
        return systemPackages.contains(packageName)
    }
}
