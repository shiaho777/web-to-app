package com.webtoapp.core.i18n

import kotlin.random.Random

object RandomAppNameGenerator {

    private val chinesePrefixes = listOf(
        "小", "大", "快", "智", "云", "新", "超", "酷", "妙", "神",
        "精", "万", "美", "乐", "易", "轻", "巧", "火", "飞", "闪",
        "光", "星", "月", "雷", "风", "电", "金", "银", "玉", "宝",
        "梦", "天", "地", "海", "山", "林", "泉", "雪", "雨", "露",
        "红", "蓝", "绿", "紫", "青", "白", "黑", "橙", "粉", "灰",
        "微", "极", "优", "凡", "真", "善", "雅", "简", "纯", "净",
        "悦", "爱", "心", "灵", "思", "意", "念", "忆", "品", "尚",
        "蜂", "鸟", "鱼", "龙", "虎", "鹿", "猫", "鹰", "蝶", "鲸",
        "晨", "暮", "春", "夏", "秋", "冬", "晴", "岚", "溪", "谷",
        "墨", "砚", "琴", "棋", "书", "画", "诗", "茶", "花", "草"
    )

    private val chineseSuffixes = listOf(
        "助手", "工具", "宝", "通", "达", "星", "盒", "管家", "精灵", "魔法",
        "帮手", "大师", "侠", "神", "客", "管", "方", "统", "乐园", "向导",
        "天使", "小精", "小宝", "宝贝", "小伙伴", "助理", "笔记", "记录",
        "空间", "世界", "宇宙", "星球", "银河", "统计", "分析", "探索", "发现", "秘密",
        "路径", "桥梁", "通道", "入口", "窗口", "门户", "平台", "中心", "基地", "站",
        "日历", "天气", "时钟", "清单", "备忘", "日记", "账本", "钱包", "商城", "社区",
        "阅读", "音乐", "视频", "相册", "相机", "录音", "翻译", "地图", "导航", "快递",
        "办公", "学习", "课堂", "题库", "词典", "百科", "论坛", "聊天", "动态", "头条",
        "健身", "运动", "健康", "饮食", "菜谱", "旅行", "酒店", "购票", "打卡", "日程",
        "头条", "周刊", "杂志", "电台", "电视", "影院", "剧场", "画廊", "博物馆", "图书馆",
        "工坊", "实验室", "研究所", "俱乐部", "协会", "联盟", "家族", "团队", "小组", "公社",
        "便利店", "超市", "集市", "广场", "街区", "小镇", "城堡", "宫殿", "庄园", "花园"
    )

    private val chineseCategories = listOf(
        "阅读", "音乐", "视频", "相册", "相机", "录音", "翻译", "地图", "导航", "快递",
        "办公", "学习", "课堂", "题库", "词典", "百科", "论坛", "聊天", "社交", "资讯",
        "健身", "运动", "健康", "饮食", "菜谱", "旅行", "酒店", "购票", "打卡", "日程",
        "记账", "理财", "股票", "银行", "支付", "购物", "比价", "优惠", "外卖", "打车",
        "天气", "日历", "时钟", "备忘", "笔记", "日记", "清单", "待办", "提醒", "邮箱",
        "新闻", "杂志", "电台", "播客", "小说", "漫画", "动画", "直播", "短视频", "摄影",
        "冥想", "睡眠", "瑜伽", "跑步", "骑行", "游泳", "登山", "滑雪", "钓鱼", "棋牌",
        "编程", "设计", "绘画", "剪辑", "作曲", "写作", "翻译", "排版", "建模", "渲染"
    )

    private val chineseBrandWords = listOf(
        "天天", "人人", "一键", "全民", "极速", "完美", "轻松", "快乐", "方便", "简单",
        "专业", "精选", "优选", "良品", "好物", "好帮手", "小能手", "随身", "口袋", "掌上",
        "一刻", "即时", "实时", "全天", "全场景", "全平台", "一站式", "一体化", "智能", "自动化",
        "个性化", "定制", "专属", "私人", "旗舰", "卓越", "极致", "非凡", "创新", "前沿"
    )

    private val englishPrefixes = listOf(
        "Quick", "Smart", "Easy", "Super", "Magic", "Ultra", "Pro", "Neo", "Max", "Prime",
        "Flash", "Turbo", "Swift", "Rapid", "Instant", "Fast", "Speed", "Zoom", "Rush", "Blitz",
        "Power", "Mega", "Giga", "Hyper", "Omni", "Multi", "Poly", "Meta", "Cyber", "Tech",
        "Star", "Nova", "Luna", "Solar", "Cosmic", "Galaxy", "Orbit", "Sky", "Cloud", "Air",
        "Dream", "Vision", "Mind", "Soul", "Spirit", "Heart", "Core", "Pure", "True", "Real",
        "Blue", "Red", "Green", "Gold", "Silver", "Crystal", "Diamond", "Pearl", "Ruby", "Jade",
        "Fire", "Ice", "Thunder", "Storm", "Wave", "Spark", "Flame", "Frost", "Wind", "Rain",
        "Bright", "Bold", "Fresh", "Clear", "Sharp", "Smooth", "Clean", "Crisp", "Vivid", "Lush",
        "Happy", "Lucky", "Mighty", "Noble", "Royal", "Elite", "Apex", "Peak", "Zen", "Vibe",
        "Pixel", "Byte", "Quantum", "Digital", "Virtual", "Global", "Local", "Urban", "Retro", "Modern"
    )

    private val englishSuffixes = listOf(
        "App", "Tool", "Kit", "Box", "Hub", "Lab", "Pro", "Go", "Now", "One",
        "Helper", "Master", "Genius", "Wizard", "Expert", "Guide", "Buddy", "Pal", "Mate", "Friend",
        "Space", "World", "Zone", "Land", "Realm", "Sphere", "Field", "Arena", "Studio", "Works",
        "Link", "Connect", "Bridge", "Path", "Way", "Gate", "Door", "Portal", "Channel", "Stream",
        "Base", "Center", "Core", "Point", "Spot", "Place", "Site", "Desk", "Board", "Pad",
        "Flow", "Sync", "Track", "Pulse", "Beat", "Loop", "Ring", "Spin", "Dash", "Vault",
        "Notes", "Diary", "Journal", "Memo", "List", "Timer", "Clock", "Calendar", "Weather", "Map",
        "Reader", "Player", "Viewer", "Editor", "Maker", "Builder", "Creator", "Scanner", "Finder", "Explorer",
        "Dock", "Nest", "Den", "HQ", "Station", "Camp", "Post", "Nest", "Hive", "Forge",
        "Zone", "Hub", "Deck", "Stage", "Scene", "Plot", "Tale", "Story", "Verse", "Aura"
    )

    private val englishCategories = listOf(
        "Reader", "Player", "Browser", "Editor", "Camera", "Recorder", "Translator", "Mapper", "Tracker", "Scanner",
        "Notes", "Diary", "Journal", "Memo", "Calendar", "Weather", "Clock", "Timer", "Alarm", "Calculator",
        "Wallet", "Budget", "Finance", "Bank", "Pay", "Shop", "Store", "Cart", "Deal", "Coupon",
        "Fitness", "Workout", "Running", "Yoga", "Health", "Diet", "Recipe", "Cook", "Travel", "Flight",
        "News", "Feed", "Blog", "Forum", "Chat", "Mail", "Message", "Call", "Social", "Share",
        "Music", "Video", "Photo", "Gallery", "Album", "Podcast", "Radio", "TV", "Movie", "Game",
        "Books", "Comics", "Anime", "Live", "Vlog", "Photo", "Drawing", "Painting", "Coding", "Writing",
        "Meditation", "Sleep", "Habit", "Mood", "Focus", "Timer", "Reminder", "Task", "Project", "Team"
    )

    private val englishBrandWords = listOf(
        "Daily", "Pocket", "Mini", "Lite", "Plus", "Premium", "Deluxe", "Express", "Direct", "Instant",
        "All", "My", "Your", "Our", "The", "Go", "On", "Up", "In", "Now",
        "Everyday", "Always", "Forever", "Infinity", "Anytime", "Anywhere", "Everywhere", "Lifetime", "Endless", "Boundless",
        "Smart", "Simple", "Clean", "Pure", "Fresh", "Bold", "Quick", "Easy", "Safe", "Secure",
        "Pro", "Max", "Ultra", "Hyper", "Super", "Mega", "Prime", "Elite", "Prime", "Signature"
    )

    private val arabicPrefixes = listOf(
        "السريع", "الذكي", "السهل", "الخارق", "السحري", "الفائق", "المتقدم", "الجديد", "الأقصى", "الأول",
        "البرق", "الصاروخ", "الخفيف", "المباشر", "الفوري", "العاجل", "المنطلق", "المندفع", "الخاطف", "اللامع",
        "القوي", "الضخم", "العملاق", "المفرط", "الشامل", "المتعدد", "المتنوع", "الرقمي", "الإلكتروني", "التقني",
        "النجم", "المضيء", "القمري", "الشمسي", "الكوني", "المجري", "السماوي", "العلوي", "السحابي", "الهوائي",
        "الأزرق", "الأحمر", "الأخضر", "الذهبي", "الفضي", "البلوري", "الماسي", "اللؤلؤي", "الياقوتي", "الزمردي",
        "الجميل", "الأنيق", "البسيط", "النقي", "الحقيقي", "المثالي", "المميز", "الفريد", "الأصيل", "العريق",
        "الصافي", "السلس", "الحاد", "الزاهي", "المشع", "المتوهج", "المشرق", "الباهر", "الرائع", "الجبار",
        "المحمي", "الآمن", "الموثوق", "الدقيق", "الثابت", "المستقر", "المرن", "النشط", "الحيوي", "الديناميكي"
    )

    private val arabicSuffixes = listOf(
        "التطبيق", "الأداة", "المجموعة", "الصندوق", "المركز", "المختبر", "المحترف", "المنطلق", "الآن", "الواحد",
        "المساعد", "الخبير", "العبقري", "الساحر", "المتخصص", "المرشد", "الرفيق", "الصديق", "الزميل", "الشريك",
        "الفضاء", "العالم", "المنطقة", "الأرض", "المملكة", "الكرة", "الميدان", "الساحة", "الاستوديو", "الورشة",
        "الرابط", "الموصل", "الجسر", "الطريق", "المسار", "البوابة", "الباب", "البورتال", "القناة", "التدفق",
        "القاعدة", "النقطة", "القلب", "الموقع", "المكان", "الموضع", "الموقف", "المكتب", "اللوحة", "الوسادة",
        "المذكرات", "اليوميات", "الميزانية", "المحفظة", "المتجر", "السوق", "المنتدى", "الدردشة", "الأخبار", "البودكاست",
        "القراء", "المشغل", "العارض", "المحرر", "الآلة الحاسبة", "المؤقت", "المنبه", "الطقس", "الخرائط", "الملاحة",
        "المستودع", "الأرشيف", "المكتبة", "المعرض", "المتحف", "النادي", "الرابطة", "التحالف", "العائلة", "الفريق",
        "المتجر", "البقالة", "السوق", "الساحة", "الحارة", "البلدة", "القلعة", "القصر", "الضيعة", "الحديقة"
    )

    private val arabicCategories = listOf(
        "القراءة", "الموسيقى", "الفيديو", "الصور", "الكاميرا", "التسجيل", "الترجمة", "الخرائط", "الملاحة", "التتبع",
        "المذكرات", "اليوميات", "الميزانية", "المحفظة", "المتجر", "السوق", "المنتدى", "الدردشة", "الأخبار", "البودكاست",
        "اللياقة", "التمارين", "الجري", "اليوغا", "الصحة", "الحمية", "الوصفات", "الطبخ", "السفر", "الطيران",
        "المستعرض", "المحرر", "الآلة الحاسبة", "المؤقت", "المنبه", "الطقس", "التقويم", "الساعة", "البريد", "المكالمات",
        "الكتب", "القصص المصورة", "الأنمي", "البث المباشر", "المدونة المرئية", "الرسم", "التصوير", "البرمجة", "الكتابة", "التأمل",
        "النوم", "العادات", "المزاج", "التركيز", "المهام", "المشاريع", "الفريق", "التذكير", "التخطيط", "التنظيم"
    )

    private val arabicBrandWords = listOf(
        "يومي", "جيبي", "مصغر", "خفيف", "بلاس", "بريميوم", "فاخر", "فائق", "سريع", "مباشر",
        "الكل", "خاصتي", "خاصك", "لنا", "الـ", "انطلق", "دائمًا", "للأبد", "في كل مكان", "في أي وقت",
        "ذكي", "بسيط", "نظيف", "نقي", "طازج", "جريء", "آمن", "موثوق", "احترافي", "حصري",
        "متميز", "إبداعي", "مرن", "نشط", "حيوي", "شامل", "متكامل", "فوري", "لحظي", "دقيق"
    )

    fun generate(): String {
        return when (Strings.currentLanguage.value) {
            AppLanguage.CHINESE -> generateChinese()
            AppLanguage.ENGLISH -> generateEnglish()
            AppLanguage.ARABIC -> generateArabic()
        }
    }

    fun generate(language: AppLanguage): String {
        return when (language) {
            AppLanguage.CHINESE -> generateChinese()
            AppLanguage.ENGLISH -> generateEnglish()
            AppLanguage.ARABIC -> generateArabic()
        }
    }

    private fun generateChinese(): String {
        return when (Random.nextInt(4)) {
            0 -> chinesePrefixes.random() + chineseSuffixes.random()
            1 -> chineseBrandWords.random() + chineseCategories.random()
            2 -> chinesePrefixes.random() + chineseCategories.random()
            else -> chineseBrandWords.random() + chineseSuffixes.random()
        }
    }

    private fun generateEnglish(): String {
        return when (Random.nextInt(4)) {
            0 -> englishPrefixes.random() + englishSuffixes.random()
            1 -> englishBrandWords.random() + englishCategories.random()
            2 -> englishPrefixes.random() + englishCategories.random()
            else -> englishBrandWords.random() + englishSuffixes.random()
        }
    }

    private fun generateArabic(): String {
        return when (Random.nextInt(4)) {
            0 -> "${arabicPrefixes.random()} ${arabicSuffixes.random()}"
            1 -> "${arabicBrandWords.random()} ${arabicCategories.random()}"
            2 -> "${arabicPrefixes.random()} ${arabicCategories.random()}"
            else -> "${arabicBrandWords.random()} ${arabicSuffixes.random()}"
        }
    }

    private fun <T> List<T>.random(): T = this[Random.nextInt(this.size)]
}
