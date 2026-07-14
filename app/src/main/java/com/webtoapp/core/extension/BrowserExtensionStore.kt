package com.webtoapp.core.extension

object BrowserExtensionStore {

    enum class Category {
        FEATURED,
        AD_BLOCKING,
        PRIVACY,
        YOUTUBE,
        PRODUCTIVITY,
        DEVELOPER,
        STYLING
    }

    data class StoreEntry(
        val storeId: String,
        val name: String,
        val author: String,
        val description: String,
        val homepage: String,
        val iconUrl: String,
        val categories: Set<Category> = setOf(Category.FEATURED),
        val ratingValue: Double = 0.0,
        val ratingCount: Int = 0,
        val userCountValue: Long = 0L,
        val ratingLabel: String = "",
        val ratingCountLabel: String = "",
        val userCountLabel: String = ""
    )

    fun storePageUrl(storeId: String): String =
        "https://chromewebstore.google.com/detail/$storeId"

    val catalog: List<StoreEntry> = listOf(
        StoreEntry(
            storeId = "eimadpbcbfnmbkopoojfekhnkhdbieeh",
            name = "Dark Reader",
            author = "Alexander Shutau",
            description = "Dark mode for every website; adjustable brightness, contrast and color.",
            homepage = "https://darkreader.org",
            iconUrl = "https://lh3.googleusercontent.com/T66wTLk-gpBBGsMm0SDJJ3VaI8YM0Utr8NaGCSANmXOfb84K-9GmyXORLKoslfxtasKtQ4spDCdq_zlp_t3QQ6SI0A=s128-rj-sc0x00ffffff",
            categories = setOf(Category.FEATURED),
            ratingValue = 4.7,
            ratingCount = 13128,
            userCountValue = 6000000L,
            ratingLabel = "4.7",
            ratingCountLabel = "13.1K",
            userCountLabel = "6M+"
        ),
        StoreEntry(
            storeId = "ddkjiahejlhfcafbddmgiahcphecmpfh",
            name = "uBlock Origin Lite",
            author = "Raymond Hill",
            description = "Permission-less MV3 content blocker for ads and trackers.",
            homepage = "https://github.com/uBlockOrigin/uBOL-home",
            iconUrl = "https://lh3.googleusercontent.com/lsanoOfx5N_t-7gh5Qg9FGIirVEjdCqalZXyLZYRd5d7Fydm83FQhu4Oq0JmlRyMtyF_LfwuQQZyKRTHs6emnFirsA=s128-rj-sc0x00ffffff",
            categories = setOf(Category.FEATURED),
            ratingValue = 4.5,
            ratingCount = 3286,
            userCountValue = 15000000L,
            ratingLabel = "4.5",
            ratingCountLabel = "3.3K",
            userCountLabel = "15M+"
        ),
        StoreEntry(
            storeId = "bgnkhhnnamicmpeenaelnjfhikgbkllg",
            name = "AdGuard AdBlocker",
            author = "AdGuard",
            description = "Blocks ads and trackers across websites, including video ads.",
            homepage = "https://adguard.com",
            iconUrl = "https://lh3.googleusercontent.com/mOms1zOrTTa6cSy0IlGTsl5EUUff7E9C6O3h-g1ScmaBK6H3WdKXnXQI8oIRTAuf5Uh7Zc3IEFlgfusDIo08khi9=s128-rj-sc0x00ffffff",
            categories = setOf(Category.FEATURED),
            ratingValue = 4.7,
            ratingCount = 68272,
            userCountValue = 16000000L,
            ratingLabel = "4.7",
            ratingCountLabel = "68.3K",
            userCountLabel = "16M+"
        ),
        StoreEntry(
            storeId = "cfhdojbkjhnklbpkdaibdccddilifddb",
            name = "Adblock Plus",
            author = "eyeo GmbH",
            description = "Popular ad blocker; blocks ads, pop-ups and trackers.",
            homepage = "https://adblockplus.org",
            iconUrl = "https://lh3.googleusercontent.com/nnMASpwJY4U5ukhKl4PfIdaOpuKXNrVvfIc9n8-NJOJIY7m3RLgsazN6ATmDkXyaMll8zADOXuBR574MwC7T71kJcQ=s128-rj-sc0x00ffffff",
            categories = setOf(Category.FEATURED),
            ratingValue = 4.4,
            ratingCount = 187970,
            userCountValue = 37000000L,
            ratingLabel = "4.4",
            ratingCountLabel = "187K",
            userCountLabel = "37M+"
        ),
        StoreEntry(
            storeId = "gighmmpiobklfepjocnamgkkbiglidom",
            name = "AdBlock",
            author = "BetaFish",
            description = "Block ads and pop-ups on websites and video.",
            homepage = "https://getadblock.com",
            iconUrl = "https://lh3.googleusercontent.com/mgNKV-3VMXD556WVUiWSbcukQQN-il4Zlqq03efTjG2B5j9YP7Fxr3idTQ_G0JFD7E6o4TMwvTQTleDn_8UdFLf5VQ=s128-rj-sc0x00ffffff",
            categories = setOf(Category.FEATURED),
            ratingValue = 4.5,
            ratingCount = 290335,
            userCountValue = 59000000L,
            ratingLabel = "4.5",
            ratingCountLabel = "290K",
            userCountLabel = "59M+"
        ),
        StoreEntry(
            storeId = "pkehgijcmpdhfbdbbnkijodmdjhbjlgp",
            name = "Privacy Badger",
            author = "EFF Technologists",
            description = "Automatically learns to block invisible trackers.",
            homepage = "https://privacybadger.org",
            iconUrl = "https://lh3.googleusercontent.com/LzhiFbFNvTwkOM7XX4K3wkwSlTkMj6NLyixs1yLPaYGIzXaiFGsfi6qcwzKzQcLR-6jtIVnFPRlQWUZC-OlnHpROcg=s128-rj-sc0x00ffffff",
            categories = setOf(Category.FEATURED),
            ratingValue = 4.5,
            ratingCount = 1989,
            userCountValue = 1000000L,
            ratingLabel = "4.5",
            ratingCountLabel = "2K",
            userCountLabel = "1M+"
        ),
        StoreEntry(
            storeId = "pncfbmialoiaghdehhbnbhkkgmjanfhe",
            name = "uBlacklist",
            author = "iorate",
            description = "Block specific sites from appearing in search results.",
            homepage = "https://github.com/iorate/uBlacklist",
            iconUrl = "https://lh3.googleusercontent.com/7BYkR_68-QVujiN-3VFq9UODmQDYZ25dsjNzjmDkYwEAKe9EVMSo0BJGYtXFG5Y2gfWUb2iaM8myB3td8q8-ytZEiQ=s128-rj-sc0x00ffffff",
            categories = setOf(Category.FEATURED),
            ratingValue = 4.4,
            ratingCount = 934,
            userCountValue = 200000L,
            ratingLabel = "4.4",
            ratingCountLabel = "934",
            userCountLabel = "200K+"
        ),
        StoreEntry(
            storeId = "gebbhagfogifgggkldgodflihgfeippi",
            name = "Return YouTube Dislike",
            author = "Dmitrii Selivanov",
            description = "Brings back the YouTube dislike count.",
            homepage = "https://returnyoutubedislike.com",
            iconUrl = "https://lh3.googleusercontent.com/bVYgRXHiKIDU1EqkGv58alRhXu-SjSqi-I_yZHak8ZvZo_kYxMePoqa3pyIX931tzFkQ3b-EbxT7gSk8M1eOSdpDCQ8=s128-rj-sc0x00ffffff",
            categories = setOf(Category.FEATURED),
            ratingValue = 4.5,
            ratingCount = 19789,
            userCountValue = 5000000L,
            ratingLabel = "4.5",
            ratingCountLabel = "19.8K",
            userCountLabel = "5M+"
        ),
        StoreEntry(
            storeId = "mnjggcdmjocbbbhaepdhchncahnbgone",
            name = "SponsorBlock",
            author = "Ajay Ramachandran",
            description = "Skip sponsor segments, intros and more in YouTube videos.",
            homepage = "https://sponsor.ajay.app",
            iconUrl = "https://lh3.googleusercontent.com/oSoXDpjLX_iytl11_ROa1thmFI0xPk9pL8ttEtnFkBI8Cie0Ge8KxVFaokgBRscvUR1cXH4bVeG_C_Fl6kBw3A3_=s128-rj-sc0x00ffffff",
            categories = setOf(Category.FEATURED),
            ratingValue = 4.6,
            ratingCount = 3434,
            userCountValue = 2000000L,
            ratingLabel = "4.6",
            ratingCountLabel = "3.4K",
            userCountLabel = "2M+"
        ),
        StoreEntry(
            storeId = "ponfpcnoihfmfllpaingbgckeeldkhle",
            name = "Enhancer for YouTube",
            author = "Maxime RF",
            description = "Customize YouTube: speed, volume, themes, ad blocking and more.",
            homepage = "https://www.mrfdev.com/enhancer-for-youtube",
            iconUrl = "https://lh3.googleusercontent.com/6PBcKpsoS15e2SUqMi6_KGBHsnvUdaRrRYXkHM3zkn5Zzj8TAEJp1_RtykaCfn1DCmyH9PJOKHrMbmtAOnQqtAU8aLs=s128-rj-sc0x00ffffff",
            categories = setOf(Category.FEATURED),
            ratingValue = 4.7,
            ratingCount = 17366,
            userCountValue = 1000000L,
            ratingLabel = "4.7",
            ratingCountLabel = "17.4K",
            userCountLabel = "1M+"
        ),
        StoreEntry(
            storeId = "nffaoalbilbmmfgbnbgppjihopabppdk",
            name = "Video Speed Controller",
            author = "igrigorik",
            description = "Speed up, slow down and control HTML5 video playback.",
            homepage = "https://github.com/igrigorik/videospeed",
            iconUrl = "https://lh3.googleusercontent.com/5yY1QQ__b0c2ZLmmi6mcdjX-9V3LAgXhgUaZtsICFTjeQV0S6xkVncmV99oEtU-H8WN8dZpWh_cycXSXoyffADsoYg=s128-rj-sc0x00ffffff",
            categories = setOf(Category.FEATURED),
            ratingValue = 4.5,
            ratingCount = 4453,
            userCountValue = 3000000L,
            ratingLabel = "4.5",
            ratingCountLabel = "4.5K",
            userCountLabel = "3M+"
        ),
        StoreEntry(
            storeId = "aapbdbdomjkkjkaonfhkkikfgjllcleb",
            name = "Google Translate",
            author = "Google",
            description = "Translate selected text and whole web pages.",
            homepage = "https://translate.google.com",
            iconUrl = "https://lh3.googleusercontent.com/3ZU5aHnsnQUl9ySPrGBqe5LXz_z9DK05DEfk10tpKHv5cvG19elbOr0BdW_k8GjLMFDexT2QHlDwAmW62iLVdek--Q=s128-rj-sc0x00ffffff",
            categories = setOf(Category.FEATURED),
            ratingValue = 4.2,
            ratingCount = 44808,
            userCountValue = 30000000L,
            ratingLabel = "4.2",
            ratingCountLabel = "44.8K",
            userCountLabel = "30M+"
        ),
        StoreEntry(
            storeId = "oldceeleldhonbafppcapldpdifcinji",
            name = "LanguageTool",
            author = "LanguageTooler GmbH",
            description = "Grammar and spelling checker for text fields on the web.",
            homepage = "https://languagetool.org",
            iconUrl = "https://lh3.googleusercontent.com/aWtP0rIATRVsZjZHBoicvGMeXImFkM4PV4LVJ3SWyJ6LFYDFjHjp2oJYZAND3HW-3Fz0Sgx9liEq2G6Ftq4OIEOO0zE=s128-rj-sc0x00ffffff",
            categories = setOf(Category.FEATURED),
            ratingValue = 4.7,
            ratingCount = 12521,
            userCountValue = 2000000L,
            ratingLabel = "4.7",
            ratingCountLabel = "12.5K",
            userCountLabel = "2M+"
        ),
        StoreEntry(
            storeId = "dgmanlpmmkibanfdgjocnabmcaclkmod",
            name = "Just Read",
            author = "ZackDeRose",
            description = "Clean, distraction-free reader view for articles.",
            homepage = "https://github.com/ZachSaucier/Just-Read",
            iconUrl = "https://lh3.googleusercontent.com/NPdAH77xk1xO-J90MzWakzPyGMmGft8oBo0OFTGGPdfB-V7fcBUiAr1Vo5S1JaS1lSgl9S92gucJBH1as7y7erghDSQ=s128-rj-sc0x00ffffff",
            categories = setOf(Category.FEATURED),
            ratingValue = 4.6,
            ratingCount = 907,
            userCountValue = 200000L,
            ratingLabel = "4.6",
            ratingCountLabel = "907",
            userCountLabel = "200K+"
        ),
        StoreEntry(
            storeId = "bcjindcccaagfpapjjmafapmmgkkhgoa",
            name = "JSON Formatter",
            author = "Callum Locke",
            description = "Makes JSON responses readable with syntax highlighting.",
            homepage = "https://github.com/callumlocke/json-formatter",
            iconUrl = "https://lh3.googleusercontent.com/kAyuA6Uo8kCc8EQJNkvj0GHetWNpbwU9ssHPf0tKbDaVddJD3FebnaZM5boUoU4_WLOOlJEVxnLoQFhPg_WuGPSI=s128-rj-sc0x00ffffff",
            categories = setOf(Category.FEATURED),
            ratingValue = 4.3,
            ratingCount = 2084,
            userCountValue = 2000000L,
            ratingLabel = "4.3",
            ratingCountLabel = "2.1K",
            userCountLabel = "2M+"
        ),
        StoreEntry(
            storeId = "gppongmhjkpfnbhagpmjfkannfbllamg",
            name = "Wappalyzer",
            author = "Wappalyzer",
            description = "Identifies the technologies used on websites.",
            homepage = "https://www.wappalyzer.com",
            iconUrl = "https://lh3.googleusercontent.com/YssvlGeiyyXJhYYV8Fh4aeW_kuQ7zC-ap7kygvyLT59TZwnFWYy-9aHCfXpyKgBEwJhFajvSUFqpjiG_42yfQXIw2Q=s128-rj-sc0x00ffffff",
            categories = setOf(Category.FEATURED),
            ratingValue = 4.6,
            ratingCount = 1969,
            userCountValue = 3000000L,
            ratingLabel = "4.6",
            ratingCountLabel = "2K",
            userCountLabel = "3M+"
        ),
        StoreEntry(
            storeId = "bhlhnicpbhignbdhedgjhgdocnmhomnp",
            name = "ColorZilla",
            author = "ColorZilla",
            description = "Eyedropper, color picker and gradient generator for the web.",
            homepage = "https://www.colorzilla.com",
            iconUrl = "https://lh3.googleusercontent.com/dqKrPk33LRO7ZFvKvSJv0q-rofymwBRJDiA3fuAZEXydo_tTS-959G0ZZKjLet9xVoVEWmD0FTYTZMEFx1mzbPzuOQ=s128-rj-sc0x00ffffff",
            categories = setOf(Category.FEATURED),
            ratingValue = 4.6,
            ratingCount = 3939,
            userCountValue = 4000000L,
            ratingLabel = "4.6",
            ratingCountLabel = "3.9K",
            userCountLabel = "4M+"
        ),
        StoreEntry(
            storeId = "clngdbkpkpeebahjckkjfobafhncgmne",
            name = "Stylus",
            author = "Stylus Team",
            description = "Restyle the web with user CSS styles.",
            homepage = "https://github.com/openstyles/stylus",
            iconUrl = "https://lh3.googleusercontent.com/2K8pc_5-2DkPam9b3oAWoITZ7IuIz68A5a8Ssg2_MNNHTPWPOPSBVTFdTmeVu9hi8GJxpKbvTekgwpeyGV6vXyBKH80=s128-rj-sc0x00ffffff",
            categories = setOf(Category.FEATURED),
            ratingValue = 4.5,
            ratingCount = 1225,
            userCountValue = 1000000L,
            ratingLabel = "4.5",
            ratingCountLabel = "1.2K",
            userCountLabel = "1M+"
        ),
        StoreEntry(
            storeId = "dhdgffkkebhmkfjojejmpbldmpobfkfo",
            name = "Tampermonkey",
            author = "Jan Biniok",
            description = "The popular userscript manager.",
            homepage = "https://www.tampermonkey.net",
            iconUrl = "https://lh3.googleusercontent.com/zoY8FwoOqPlBgFxcmFdNSK2Q4CcLmv-gw7vTjF2KMR9cEabwBsGNrHBTEMitn0Ba6OmCVJ0NcLnFGu3N97BP8Phu0g=s128-rj-sc0x00ffffff",
            categories = setOf(Category.FEATURED),
            ratingValue = 4.7,
            ratingCount = 72942,
            userCountValue = 11000000L,
            ratingLabel = "4.7",
            ratingCountLabel = "72.9K",
            userCountLabel = "11M+"
        ),
        StoreEntry(
            storeId = "kbfnbcaeplbcioakkpcpgfkobkghlhen",
            name = "Grammarly",
            author = "Grammarly",
            description = "Writing assistant: grammar, spelling and clarity suggestions.",
            homepage = "https://www.grammarly.com",
            iconUrl = "https://lh3.googleusercontent.com/Ywdz5mn9q2Mx76DU45LSH-Pv5OGpqk8QAOY3lT1AWScMTZYQtAhqhVjtY5I2JZK530QIycLZooe2a0k3quGqYUaZ=s128-rj-sc0x00ffffff",
            categories = setOf(Category.FEATURED),
            ratingValue = 4.5,
            ratingCount = 43099,
            userCountValue = 35000000L,
            ratingLabel = "4.5",
            ratingCountLabel = "43.1K",
            userCountLabel = "35M+"
        ),
    )

    fun featured(): List<StoreEntry> =
        catalog.filter { Category.FEATURED in it.categories }

    fun byCategory(category: Category): List<StoreEntry> =
        if (category == Category.FEATURED) {
            featured()
        } else {
            catalog.filter { category in it.categories }
        }

    fun browseCategories(): List<Category> = listOf(
        Category.FEATURED,
        Category.AD_BLOCKING,
        Category.PRIVACY,
        Category.YOUTUBE,
        Category.PRODUCTIVITY,
        Category.DEVELOPER,
        Category.STYLING
    )

    private val STORE_URL_ID_REGEX = Regex("/detail/(?:[^/]+/)?([a-p]{32})")
    private val RAW_ID_REGEX = Regex("^[a-p]{32}$")

    fun extractStoreId(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null
        RAW_ID_REGEX.find(trimmed)?.let { return trimmed }
        STORE_URL_ID_REGEX.find(trimmed)?.let { return it.groupValues[1] }
        return trimmed.takeIf { it.length == 32 && it.all { ch -> ch in 'a'..'p' } }
    }
}
