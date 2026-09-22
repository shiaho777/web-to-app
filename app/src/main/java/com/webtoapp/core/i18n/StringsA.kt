package com.webtoapp.core.i18n

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

object StringsA {
    val appTitle: String get() = "WebToApp"

    val translationInProgressBadge: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "翻译进行中"
        AppLanguage.ENGLISH -> "Translation in progress"
        AppLanguage.ARABIC -> "الترجمة قيد التقدم"
        AppLanguage.PORTUGUESE -> "Tradução em andamento"
        AppLanguage.SPANISH -> "Traducción en progreso"
        AppLanguage.FRENCH -> "Traduction en cours"
        AppLanguage.GERMAN -> "Übersetzung läuft"
        AppLanguage.RUSSIAN -> "Перевод в процессе"
        AppLanguage.JAPANESE -> "翻訳中"
        AppLanguage.KOREAN -> "번역 중"
    }

    val typewriterText1: String get() = "WebToApp"
    val typewriterText2: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "探索网页与APK的边界"
        AppLanguage.ENGLISH -> "Explore the boundary of Web & APK"
        AppLanguage.ARABIC -> "استكشف حدود الويب و APK"
        AppLanguage.PORTUGUESE -> "Explore a fronteira entre Web e APK"
        AppLanguage.SPANISH -> "Explora la frontera entre Web y APK"
        AppLanguage.FRENCH -> "Explorez la frontière entre le Web et l'APK"
        AppLanguage.GERMAN -> "Erkunde die Grenze zwischen Web und APK"
        AppLanguage.RUSSIAN -> "Исследуйте границу между вебом и APK"
        AppLanguage.JAPANESE -> "WebとAPKの境界を探る"
        AppLanguage.KOREAN -> "웹과 APK의 경계를 탐색하세요"
    }
    val typewriterText3: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "代码的无限可能"
        AppLanguage.ENGLISH -> "Infinite possibilities of code"
        AppLanguage.ARABIC -> "إمكانيات لا حدود لها للبرمجة"
        AppLanguage.PORTUGUESE -> "Possibilidades infinitas do código"
        AppLanguage.SPANISH -> "Posibilidades infinitas del código"
        AppLanguage.FRENCH -> "Possibilités infinies du code"
        AppLanguage.GERMAN -> "Unendliche Möglichkeiten des Codes"
        AppLanguage.RUSSIAN -> "Бесконечные возможности кода"
        AppLanguage.JAPANESE -> "コードの無限の可能性"
        AppLanguage.KOREAN -> "코드의 무한한 가능성"
    }

    val myApps: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "我的应用"
        AppLanguage.ENGLISH -> "My Apps"
        AppLanguage.ARABIC -> "تطبيقاتي"
        AppLanguage.PORTUGUESE -> "Meus Aplicativos"
        AppLanguage.SPANISH -> "Mis Aplicaciones"
        AppLanguage.FRENCH -> "Mes Applications"
        AppLanguage.GERMAN -> "Meine Apps"
        AppLanguage.RUSSIAN -> "Мои приложения"
        AppLanguage.JAPANESE -> "マイアプリ"
        AppLanguage.KOREAN -> "내 앱"
    }

    val createApp: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "创建应用"
        AppLanguage.ENGLISH -> "Create App"
        AppLanguage.ARABIC -> "إنشاء تطبيق"
        AppLanguage.PORTUGUESE -> "Criar Aplicativo"
        AppLanguage.SPANISH -> "Crear Aplicación"
        AppLanguage.FRENCH -> "Créer une Application"
        AppLanguage.GERMAN -> "App erstellen"
        AppLanguage.RUSSIAN -> "Создать приложение"
        AppLanguage.JAPANESE -> "アプリ作成"
        AppLanguage.KOREAN -> "앱 만들기"
    }

    val search: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "搜索..."
        AppLanguage.ENGLISH -> "Search..."
        AppLanguage.ARABIC -> "بحث..."
        AppLanguage.PORTUGUESE -> "Pesquisar..."
        AppLanguage.SPANISH -> "Buscar..."
        AppLanguage.FRENCH -> "Rechercher..."
        AppLanguage.GERMAN -> "Suchen..."
        AppLanguage.RUSSIAN -> "Поиск..."
        AppLanguage.JAPANESE -> "検索..."
        AppLanguage.KOREAN -> "검색..."
    }

    val more: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "更多"
        AppLanguage.ENGLISH -> "More"
        AppLanguage.ARABIC -> "المزيد"
        AppLanguage.PORTUGUESE -> "Mais"
        AppLanguage.SPANISH -> "Más"
        AppLanguage.FRENCH -> "Plus"
        AppLanguage.GERMAN -> "Mehr"
        AppLanguage.RUSSIAN -> "Ещё"
        AppLanguage.JAPANESE -> "その他"
        AppLanguage.KOREAN -> "더보기"
    }

    val back: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "返回"
        AppLanguage.ENGLISH -> "Back"
        AppLanguage.ARABIC -> "رجوع"
        AppLanguage.PORTUGUESE -> "Voltar"
        AppLanguage.SPANISH -> "Atrás"
        AppLanguage.FRENCH -> "Retour"
        AppLanguage.GERMAN -> "Zurück"
        AppLanguage.RUSSIAN -> "Назад"
        AppLanguage.JAPANESE -> "戻る"
        AppLanguage.KOREAN -> "뒤로"
    }

    val menuAgent: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Agent"
        AppLanguage.ENGLISH -> "Agent"
        AppLanguage.ARABIC -> "Agent"
        AppLanguage.PORTUGUESE -> "Agent"
        AppLanguage.SPANISH -> "Agent"
        AppLanguage.FRENCH -> "Agent"
        AppLanguage.GERMAN -> "Agent"
        AppLanguage.RUSSIAN -> "Agent"
        AppLanguage.JAPANESE -> "Agent"
        AppLanguage.KOREAN -> "Agent"
    }

    val menuAiSettings: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "AI 设置"
        AppLanguage.ENGLISH -> "AI Settings"
        AppLanguage.ARABIC -> "إعدادات AI"
        AppLanguage.PORTUGUESE -> "Configurações de IA"
        AppLanguage.SPANISH -> "Ajustes de IA"
        AppLanguage.FRENCH -> "Paramètres IA"
        AppLanguage.GERMAN -> "KI-Einstellungen"
        AppLanguage.RUSSIAN -> "Настройки ИИ"
        AppLanguage.JAPANESE -> "AI設定"
        AppLanguage.KOREAN -> "AI 설정"
    }

    val menuAppModifier: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "应用修改器"
        AppLanguage.ENGLISH -> "App Modifier"
        AppLanguage.ARABIC -> "معدل التطبيق"
        AppLanguage.PORTUGUESE -> "Modificador de Aplicativos"
        AppLanguage.SPANISH -> "Modificador de Aplicaciones"
        AppLanguage.FRENCH -> "Modificateur d'Applications"
        AppLanguage.GERMAN -> "App-Modifikator"
        AppLanguage.RUSSIAN -> "Модификатор приложений"
        AppLanguage.JAPANESE -> "アプリ変更ツール"
        AppLanguage.KOREAN -> "앱 수정기"
    }

    val menuExtensionModules: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "扩展模块"
        AppLanguage.ENGLISH -> "Extension Modules"
        AppLanguage.ARABIC -> "الوحدات الإضافية"
        AppLanguage.PORTUGUESE -> "Módulos de Extensão"
        AppLanguage.SPANISH -> "Módulos de Extensión"
        AppLanguage.FRENCH -> "Modules d'Extension"
        AppLanguage.GERMAN -> "Erweiterungsmodule"
        AppLanguage.RUSSIAN -> "Модули расширений"
        AppLanguage.JAPANESE -> "拡張モジュール"
        AppLanguage.KOREAN -> "확장 모듈"
    }

    val menuAbout: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "关于"
        AppLanguage.ENGLISH -> "About"
        AppLanguage.ARABIC -> "حول"
        AppLanguage.PORTUGUESE -> "Sobre"
        AppLanguage.SPANISH -> "Acerca de"
        AppLanguage.FRENCH -> "À propos"
        AppLanguage.GERMAN -> "Über"
        AppLanguage.RUSSIAN -> "О приложении"
        AppLanguage.JAPANESE -> "について"
        AppLanguage.KOREAN -> "정보"
    }

    val tabMore: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "更多"
        AppLanguage.ENGLISH -> "More"
        AppLanguage.ARABIC -> "المزيد"
        AppLanguage.PORTUGUESE -> "Mais"
        AppLanguage.SPANISH -> "Más"
        AppLanguage.FRENCH -> "Plus"
        AppLanguage.GERMAN -> "Mehr"
        AppLanguage.RUSSIAN -> "Ещё"
        AppLanguage.JAPANESE -> "その他"
        AppLanguage.KOREAN -> "더보기"
    }

    val moreSectionAiTools: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "AI 工具"
        AppLanguage.ENGLISH -> "AI Tools"
        AppLanguage.ARABIC -> "أدوات AI"
        AppLanguage.PORTUGUESE -> "Ferramentas de IA"
        AppLanguage.SPANISH -> "Herramientas de IA"
        AppLanguage.FRENCH -> "Outils IA"
        AppLanguage.GERMAN -> "KI-Werkzeuge"
        AppLanguage.RUSSIAN -> "Инструменты ИИ"
        AppLanguage.JAPANESE -> "AIツール"
        AppLanguage.KOREAN -> "AI 도구"
    }
    val moreSectionDevTools: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "开发工具"
        AppLanguage.ENGLISH -> "Dev Tools"
        AppLanguage.ARABIC -> "أدوات التطوير"
        AppLanguage.PORTUGUESE -> "Ferramentas de Desenvolvimento"
        AppLanguage.SPANISH -> "Herramientas de Desarrollo"
        AppLanguage.FRENCH -> "Outils de développement"
        AppLanguage.GERMAN -> "Entwicklertools"
        AppLanguage.RUSSIAN -> "Инструменты разработчика"
        AppLanguage.JAPANESE -> "開発ツール"
        AppLanguage.KOREAN -> "개발 도구"
    }
    val moreSectionBrowser: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "浏览器 & 网络"
        AppLanguage.ENGLISH -> "Browser & Network"
        AppLanguage.ARABIC -> "المتصفح والشبكة"
        AppLanguage.PORTUGUESE -> "Navegador e Rede"
        AppLanguage.SPANISH -> "Navegador y Red"
        AppLanguage.FRENCH -> "Navigateur et Réseau"
        AppLanguage.GERMAN -> "Browser und Netzwerk"
        AppLanguage.RUSSIAN -> "Браузер и сеть"
        AppLanguage.JAPANESE -> "ブラウザとネットワーク"
        AppLanguage.KOREAN -> "브라우저 및 네트워크"
    }
    val moreSectionAppearance: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "外观 & 数据"
        AppLanguage.ENGLISH -> "Appearance & Data"
        AppLanguage.ARABIC -> "المظهر والبيانات"
        AppLanguage.PORTUGUESE -> "Aparência e Dados"
        AppLanguage.SPANISH -> "Apariencia y Datos"
        AppLanguage.FRENCH -> "Apparence et Données"
        AppLanguage.GERMAN -> "Erscheinungsbild und Daten"
        AppLanguage.RUSSIAN -> "Внешний вид и данные"
        AppLanguage.JAPANESE -> "外観とデータ"
        AppLanguage.KOREAN -> "외형 및 데이터"
    }
    val moreSectionGeneral: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "通用"
        AppLanguage.ENGLISH -> "General"
        AppLanguage.ARABIC -> "عام"
        AppLanguage.PORTUGUESE -> "Geral"
        AppLanguage.SPANISH -> "General"
        AppLanguage.FRENCH -> "Général"
        AppLanguage.GERMAN -> "Allgemein"
        AppLanguage.RUSSIAN -> "Общие"
        AppLanguage.JAPANESE -> "一般"
        AppLanguage.KOREAN -> "일반"
    }
    val rememberCategoryFilter: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "记住上次分类"
        AppLanguage.ENGLISH -> "Remember last category"
        AppLanguage.ARABIC -> "تذكر آخر تصنيف"
        AppLanguage.PORTUGUESE -> "Lembrar última categoria"
        AppLanguage.SPANISH -> "Recordar última categoría"
        AppLanguage.FRENCH -> "Mémoriser la dernière catégorie"
        AppLanguage.GERMAN -> "Letzte Kategorie merken"
        AppLanguage.RUSSIAN -> "Запомнить последнюю категорию"
        AppLanguage.JAPANESE -> "前回のカテゴリを記憶"
        AppLanguage.KOREAN -> "마지막 카테고리 기억"
    }
    val rememberCategoryFilterDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "启动时回到上次选择的分类标签；关闭则总是进入「全部」"
        AppLanguage.ENGLISH -> "Reopen the last selected category on launch; off always starts on \"All\""
        AppLanguage.ARABIC -> "العودة إلى آخر تصنيف محدد عند التشغيل؛ عند الإيقاف يبدأ دائمًا على \"الكل\""
        AppLanguage.PORTUGUESE -> "Reabrir a última categoria selecionada ao iniciar; desligado sempre inicia em \"Todos\""
        AppLanguage.SPANISH -> "Volver a la última categoría seleccionada al iniciar; desactivado siempre inicia en \"Todos\""
        AppLanguage.FRENCH -> "Rouvrir la dernière catégorie sélectionnée au lancement ; désactivé, démarre toujours sur « Tous »"
        AppLanguage.GERMAN -> "Beim Start die zuletzt gewählte Kategorie öffnen; aus startet immer auf „Alle“"
        AppLanguage.RUSSIAN -> "Открывать последнюю выбранную категорию при запуске; выкл. — всегда «Все»"
        AppLanguage.JAPANESE -> "起動時に前回選択したカテゴリを開きます。オフでは常に「すべて」から開始"
        AppLanguage.KOREAN -> "시작 시 마지막으로 선택한 카테고리를 엽니다. 끄면 항상 \"전체\"로 시작합니다"
    }
    val menuStats: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "使用统计"
        AppLanguage.ENGLISH -> "Usage Stats"
        AppLanguage.ARABIC -> "إحصائيات الاستخدام"
        AppLanguage.PORTUGUESE -> "Estatísticas de Uso"
        AppLanguage.SPANISH -> "Estadísticas de Uso"
        AppLanguage.FRENCH -> "Statistiques d'utilisation"
        AppLanguage.GERMAN -> "Nutzungsstatistiken"
        AppLanguage.RUSSIAN -> "Статистика использования"
        AppLanguage.JAPANESE -> "使用統計"
        AppLanguage.KOREAN -> "사용 통계"
    }
    val statsTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "使用统计"
        AppLanguage.ENGLISH -> "Usage Statistics"
        AppLanguage.ARABIC -> "إحصائيات الاستخدام"
        AppLanguage.PORTUGUESE -> "Estatísticas de Uso"
        AppLanguage.SPANISH -> "Estadísticas de Uso"
        AppLanguage.FRENCH -> "Statistiques d'utilisation"
        AppLanguage.GERMAN -> "Nutzungsstatistiken"
        AppLanguage.RUSSIAN -> "Статистика использования"
        AppLanguage.JAPANESE -> "使用統計"
        AppLanguage.KOREAN -> "사용 통계"
    }
    val statsTotalLaunches: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "总启动次数"
        AppLanguage.ENGLISH -> "Total Launches"
        AppLanguage.ARABIC -> "إجمالي عمليات الإطلاق"
        AppLanguage.PORTUGUESE -> "Total de Inicializações"
        AppLanguage.SPANISH -> "Total de Inicios"
        AppLanguage.FRENCH -> "Total des lancements"
        AppLanguage.GERMAN -> "Gesamtstartvorgänge"
        AppLanguage.RUSSIAN -> "Всего запусков"
        AppLanguage.JAPANESE -> "合計起動回数"
        AppLanguage.KOREAN -> "총 실행 횟수"
    }
    val statsTotalUsage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "总使用时长"
        AppLanguage.ENGLISH -> "Total Usage"
        AppLanguage.ARABIC -> "إجمالي الاستخدام"
        AppLanguage.PORTUGUESE -> "Uso Total"
        AppLanguage.SPANISH -> "Uso Total"
        AppLanguage.FRENCH -> "Usage total"
        AppLanguage.GERMAN -> "Gesamtnutzung"
        AppLanguage.RUSSIAN -> "Общее время использования"
        AppLanguage.JAPANESE -> "合計使用時間"
        AppLanguage.KOREAN -> "총 사용 시간"
    }
    val statsActiveApps: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "活跃应用"
        AppLanguage.ENGLISH -> "Active Apps"
        AppLanguage.ARABIC -> "التطبيقات النشطة"
        AppLanguage.PORTUGUESE -> "Apps Ativas"
        AppLanguage.SPANISH -> "Apps Activas"
        AppLanguage.FRENCH -> "Applications actives"
        AppLanguage.GERMAN -> "Aktive Apps"
        AppLanguage.RUSSIAN -> "Активные приложения"
        AppLanguage.JAPANESE -> "アクティブなアプリ"
        AppLanguage.KOREAN -> "활성 앱"
    }
    val statsMostUsed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "最常使用"
        AppLanguage.ENGLISH -> "Most Used"
        AppLanguage.ARABIC -> "الأكثر استخدامًا"
        AppLanguage.PORTUGUESE -> "Mais Usadas"
        AppLanguage.SPANISH -> "Más Usadas"
        AppLanguage.FRENCH -> "Plus utilisées"
        AppLanguage.GERMAN -> "Am häufigsten genutzt"
        AppLanguage.RUSSIAN -> "Самые используемые"
        AppLanguage.JAPANESE -> "最も使用頻度が高い"
        AppLanguage.KOREAN -> "가장 많이 사용됨"
    }
    val statsMostTime: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "使用最久"
        AppLanguage.ENGLISH -> "Most Time Spent"
        AppLanguage.ARABIC -> "الأطول استخدامًا"
        AppLanguage.PORTUGUESE -> "Mais Tempo de Uso"
        AppLanguage.SPANISH -> "Más Tiempo de Uso"
        AppLanguage.FRENCH -> "Plus de temps passé"
        AppLanguage.GERMAN -> "Meiste Zeit verbracht"
        AppLanguage.RUSSIAN -> "Больше всего времени"
        AppLanguage.JAPANESE -> "最も使用時間が長い"
        AppLanguage.KOREAN -> "가장 긴 사용 시간"
    }
    val statsLaunches: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "次"
        AppLanguage.ENGLISH -> "launches"
        AppLanguage.ARABIC -> "مرة"
        AppLanguage.PORTUGUESE -> "inicializações"
        AppLanguage.SPANISH -> "inicios"
        AppLanguage.FRENCH -> "lancements"
        AppLanguage.GERMAN -> "Startvorgänge"
        AppLanguage.RUSSIAN -> "запусков"
        AppLanguage.JAPANESE -> "回起動"
        AppLanguage.KOREAN -> "회 실행"
    }
    val statsNoData: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "暂无使用数据"
        AppLanguage.ENGLISH -> "No usage data yet"
        AppLanguage.ARABIC -> "لا توجد بيانات استخدام بعد"
        AppLanguage.PORTUGUESE -> "Ainda não há dados de uso"
        AppLanguage.SPANISH -> "Aún no hay datos de uso"
        AppLanguage.FRENCH -> "Pas encore de données d'utilisation"
        AppLanguage.GERMAN -> "Noch keine Nutzungsdaten"
        AppLanguage.RUSSIAN -> "Данных об использовании пока нет"
        AppLanguage.JAPANESE -> "使用データはまだありません"
        AppLanguage.KOREAN -> "아직 사용 데이터가 없습니다"
    }
    val statsSubtitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "查看应用打开次数、使用时长与网站健康状态"
        AppLanguage.ENGLISH -> "Review launches, time spent, and website health"
        AppLanguage.ARABIC -> "راجع عمليات الفتح ومدة الاستخدام وصحة المواقع"
        AppLanguage.PORTUGUESE -> "Veja aberturas, tempo de uso e saúde dos sites"
        AppLanguage.SPANISH -> "Revisa aperturas, tiempo de uso y salud de sitios"
        AppLanguage.FRENCH -> "Consultez les lancements, le temps passé et la santé des sites"
        AppLanguage.GERMAN -> "Starts, Nutzungsdauer und Website-Status ansehen"
        AppLanguage.RUSSIAN -> "Просматривайте запуски, время использования и состояние сайтов"
        AppLanguage.JAPANESE -> "起動回数、使用時間、サイトの健全性を確認"
        AppLanguage.KOREAN -> "실행 횟수, 사용 시간, 사이트 상태 확인"
    }
    val statsSearchHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "搜索应用…"
        AppLanguage.ENGLISH -> "Search apps…"
        AppLanguage.ARABIC -> "ابحث عن التطبيقات…"
        AppLanguage.PORTUGUESE -> "Pesquisar apps…"
        AppLanguage.SPANISH -> "Buscar apps…"
        AppLanguage.FRENCH -> "Rechercher des apps…"
        AppLanguage.GERMAN -> "Apps suchen…"
        AppLanguage.RUSSIAN -> "Поиск приложений…"
        AppLanguage.JAPANESE -> "アプリを検索…"
        AppLanguage.KOREAN -> "앱 검색…"
    }
    val statsSortLaunches: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "按启动"
        AppLanguage.ENGLISH -> "Launches"
        AppLanguage.ARABIC -> "حسب الفتح"
        AppLanguage.PORTUGUESE -> "Aberturas"
        AppLanguage.SPANISH -> "Aperturas"
        AppLanguage.FRENCH -> "Lancements"
        AppLanguage.GERMAN -> "Starts"
        AppLanguage.RUSSIAN -> "Запуски"
        AppLanguage.JAPANESE -> "起動順"
        AppLanguage.KOREAN -> "실행순"
    }
    val statsSortTime: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "按时长"
        AppLanguage.ENGLISH -> "Time"
        AppLanguage.ARABIC -> "حسب المدة"
        AppLanguage.PORTUGUESE -> "Tempo"
        AppLanguage.SPANISH -> "Tiempo"
        AppLanguage.FRENCH -> "Temps"
        AppLanguage.GERMAN -> "Dauer"
        AppLanguage.RUSSIAN -> "Время"
        AppLanguage.JAPANESE -> "時間順"
        AppLanguage.KOREAN -> "시간순"
    }
    val statsSortRecent: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "最近使用"
        AppLanguage.ENGLISH -> "Recent"
        AppLanguage.ARABIC -> "الأحدث"
        AppLanguage.PORTUGUESE -> "Recentes"
        AppLanguage.SPANISH -> "Recientes"
        AppLanguage.FRENCH -> "Récents"
        AppLanguage.GERMAN -> "Zuletzt"
        AppLanguage.RUSSIAN -> "Недавние"
        AppLanguage.JAPANESE -> "最近"
        AppLanguage.KOREAN -> "최근"
    }
    val statsRankings: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "应用排行"
        AppLanguage.ENGLISH -> "App rankings"
        AppLanguage.ARABIC -> "ترتيب التطبيقات"
        AppLanguage.PORTUGUESE -> "Ranking de apps"
        AppLanguage.SPANISH -> "Ranking de apps"
        AppLanguage.FRENCH -> "Classement des apps"
        AppLanguage.GERMAN -> "App-Ranking"
        AppLanguage.RUSSIAN -> "Рейтинг приложений"
        AppLanguage.JAPANESE -> "アプリランキング"
        AppLanguage.KOREAN -> "앱 순위"
    }
    val statsAvgSession: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "平均每次"
        AppLanguage.ENGLISH -> "Avg session"
        AppLanguage.ARABIC -> "متوسط الجلسة"
        AppLanguage.PORTUGUESE -> "Sessão média"
        AppLanguage.SPANISH -> "Sesión media"
        AppLanguage.FRENCH -> "Session moy."
        AppLanguage.GERMAN -> "Ø Sitzung"
        AppLanguage.RUSSIAN -> "Средняя сессия"
        AppLanguage.JAPANESE -> "平均セッション"
        AppLanguage.KOREAN -> "평균 세션"
    }
    val statsLastSession: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "上次会话"
        AppLanguage.ENGLISH -> "Last session"
        AppLanguage.ARABIC -> "آخر جلسة"
        AppLanguage.PORTUGUESE -> "Última sessão"
        AppLanguage.SPANISH -> "Última sesión"
        AppLanguage.FRENCH -> "Dernière session"
        AppLanguage.GERMAN -> "Letzte Sitzung"
        AppLanguage.RUSSIAN -> "Последняя сессия"
        AppLanguage.JAPANESE -> "前回のセッション"
        AppLanguage.KOREAN -> "마지막 세션"
    }
    val statsClearAll: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "清空统计"
        AppLanguage.ENGLISH -> "Clear stats"
        AppLanguage.ARABIC -> "مسح الإحصائيات"
        AppLanguage.PORTUGUESE -> "Limpar estatísticas"
        AppLanguage.SPANISH -> "Borrar estadísticas"
        AppLanguage.FRENCH -> "Effacer les stats"
        AppLanguage.GERMAN -> "Statistiken leeren"
        AppLanguage.RUSSIAN -> "Очистить статистику"
        AppLanguage.JAPANESE -> "統計をクリア"
        AppLanguage.KOREAN -> "통계 지우기"
    }
    val statsClearConfirm: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "确定清空全部使用统计？此操作不可撤销。"
        AppLanguage.ENGLISH -> "Clear all usage statistics? This cannot be undone."
        AppLanguage.ARABIC -> "مسح جميع إحصائيات الاستخدام؟ لا يمكن التراجع."
        AppLanguage.PORTUGUESE -> "Limpar todas as estatísticas de uso? Isso não pode ser desfeito."
        AppLanguage.SPANISH -> "¿Borrar todas las estadísticas de uso? Esto no se puede deshacer."
        AppLanguage.FRENCH -> "Effacer toutes les statistiques d'utilisation ? Action irréversible."
        AppLanguage.GERMAN -> "Alle Nutzungsstatistiken löschen? Dies kann nicht rückgängig gemacht werden."
        AppLanguage.RUSSIAN -> "Очистить всю статистику использования? Это нельзя отменить."
        AppLanguage.JAPANESE -> "すべての使用統計をクリアしますか？この操作は取り消せません。"
        AppLanguage.KOREAN -> "모든 사용 통계를 지울까요? 되돌릴 수 없습니다."
    }
    val statsCleared: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "统计已清空"
        AppLanguage.ENGLISH -> "Statistics cleared"
        AppLanguage.ARABIC -> "تم مسح الإحصائيات"
        AppLanguage.PORTUGUESE -> "Estatísticas limpas"
        AppLanguage.SPANISH -> "Estadísticas borradas"
        AppLanguage.FRENCH -> "Statistiques effacées"
        AppLanguage.GERMAN -> "Statistiken gelöscht"
        AppLanguage.RUSSIAN -> "Статистика очищена"
        AppLanguage.JAPANESE -> "統計をクリアしました"
        AppLanguage.KOREAN -> "통계를 지웠습니다"
    }
    val statsNoMatch: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "没有匹配的应用"
        AppLanguage.ENGLISH -> "No matching apps"
        AppLanguage.ARABIC -> "لا توجد تطبيقات مطابقة"
        AppLanguage.PORTUGUESE -> "Nenhum app correspondente"
        AppLanguage.SPANISH -> "No hay apps coincidentes"
        AppLanguage.FRENCH -> "Aucune app correspondante"
        AppLanguage.GERMAN -> "Keine passenden Apps"
        AppLanguage.RUSSIAN -> "Нет подходящих приложений"
        AppLanguage.JAPANESE -> "一致するアプリがありません"
        AppLanguage.KOREAN -> "일치하는 앱이 없습니다"
    }
    val statsNeverUsed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "从未使用"
        AppLanguage.ENGLISH -> "Never used"
        AppLanguage.ARABIC -> "لم يُستخدم أبدًا"
        AppLanguage.PORTUGUESE -> "Nunca usado"
        AppLanguage.SPANISH -> "Nunca usado"
        AppLanguage.FRENCH -> "Jamais utilisé"
        AppLanguage.GERMAN -> "Nie verwendet"
        AppLanguage.RUSSIAN -> "Никогда не использовалось"
        AppLanguage.JAPANESE -> "未使用"
        AppLanguage.KOREAN -> "사용 기록 없음"
    }
    val statsJustNow: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "刚刚"
        AppLanguage.ENGLISH -> "Just now"
        AppLanguage.ARABIC -> "الآن"
        AppLanguage.PORTUGUESE -> "Agora mesmo"
        AppLanguage.SPANISH -> "Justo ahora"
        AppLanguage.FRENCH -> "À l'instant"
        AppLanguage.GERMAN -> "Gerade eben"
        AppLanguage.RUSSIAN -> "Только что"
        AppLanguage.JAPANESE -> "たった今"
        AppLanguage.KOREAN -> "방금"
    }
    val statsMinutesAgo: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "%d 分钟前"
        AppLanguage.ENGLISH -> "%d min ago"
        AppLanguage.ARABIC -> "قبل %d دقيقة"
        AppLanguage.PORTUGUESE -> "há %d min"
        AppLanguage.SPANISH -> "hace %d min"
        AppLanguage.FRENCH -> "il y a %d min"
        AppLanguage.GERMAN -> "vor %d Min."
        AppLanguage.RUSSIAN -> "%d мин назад"
        AppLanguage.JAPANESE -> "%d 分前"
        AppLanguage.KOREAN -> "%d분 전"
    }
    val statsHoursAgo: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "%d 小时前"
        AppLanguage.ENGLISH -> "%d h ago"
        AppLanguage.ARABIC -> "قبل %d ساعة"
        AppLanguage.PORTUGUESE -> "há %d h"
        AppLanguage.SPANISH -> "hace %d h"
        AppLanguage.FRENCH -> "il y a %d h"
        AppLanguage.GERMAN -> "vor %d Std."
        AppLanguage.RUSSIAN -> "%d ч назад"
        AppLanguage.JAPANESE -> "%d 時間前"
        AppLanguage.KOREAN -> "%d시간 전"
    }
    val statsDaysAgo: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "%d 天前"
        AppLanguage.ENGLISH -> "%d d ago"
        AppLanguage.ARABIC -> "قبل %d يوم"
        AppLanguage.PORTUGUESE -> "há %d d"
        AppLanguage.SPANISH -> "hace %d d"
        AppLanguage.FRENCH -> "il y a %d j"
        AppLanguage.GERMAN -> "vor %d T."
        AppLanguage.RUSSIAN -> "%d дн. назад"
        AppLanguage.JAPANESE -> "%d 日前"
        AppLanguage.KOREAN -> "%d일 전"
    }
    val statsMonthsAgo: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "%d 个月前"
        AppLanguage.ENGLISH -> "%d mo ago"
        AppLanguage.ARABIC -> "قبل %d شهر"
        AppLanguage.PORTUGUESE -> "há %d mês(es)"
        AppLanguage.SPANISH -> "hace %d mes(es)"
        AppLanguage.FRENCH -> "il y a %d mois"
        AppLanguage.GERMAN -> "vor %d Mon."
        AppLanguage.RUSSIAN -> "%d мес. назад"
        AppLanguage.JAPANESE -> "%d か月前"
        AppLanguage.KOREAN -> "%d개월 전"
    }
    val statsDurationHoursMinutes: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "%dh %dm"
        AppLanguage.ENGLISH -> "%dh %dm"
        AppLanguage.ARABIC -> "%dس %dد"
        AppLanguage.PORTUGUESE -> "%dh %dm"
        AppLanguage.SPANISH -> "%dh %dm"
        AppLanguage.FRENCH -> "%dh %dm"
        AppLanguage.GERMAN -> "%d Std. %d Min."
        AppLanguage.RUSSIAN -> "%dч %dм"
        AppLanguage.JAPANESE -> "%d時間 %d分"
        AppLanguage.KOREAN -> "%d시간 %d분"
    }
    val statsDurationMinutes: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "%dm"
        AppLanguage.ENGLISH -> "%dm"
        AppLanguage.ARABIC -> "%dد"
        AppLanguage.PORTUGUESE -> "%dm"
        AppLanguage.SPANISH -> "%dm"
        AppLanguage.FRENCH -> "%dm"
        AppLanguage.GERMAN -> "%d Min."
        AppLanguage.RUSSIAN -> "%dм"
        AppLanguage.JAPANESE -> "%d分"
        AppLanguage.KOREAN -> "%d분"
    }
    val statsDurationUnderOneMinute: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "<1 分钟"
        AppLanguage.ENGLISH -> "<1m"
        AppLanguage.ARABIC -> "<1د"
        AppLanguage.PORTUGUESE -> "<1m"
        AppLanguage.SPANISH -> "<1m"
        AppLanguage.FRENCH -> "<1m"
        AppLanguage.GERMAN -> "<1 Min."
        AppLanguage.RUSSIAN -> "<1м"
        AppLanguage.JAPANESE -> "1分未満"
        AppLanguage.KOREAN -> "1분 미만"
    }
    val statsHealthCheckHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "仅检测以 http/https 开头的网页应用。点击右上角可全部检测。"
        AppLanguage.ENGLISH -> "Only checks web apps with http/https URLs. Use the top-right action to check all."
        AppLanguage.ARABIC -> "يتحقق فقط من تطبيقات الويب ذات روابط http/https. استخدم الإجراء أعلى اليمين للتحقق من الكل."
        AppLanguage.PORTUGUESE -> "Verifica apenas apps web com URLs http/https. Use a ação no canto superior direito para checar todos."
        AppLanguage.SPANISH -> "Solo comprueba apps web con URLs http/https. Usa la acción superior derecha para comprobar todas."
        AppLanguage.FRENCH -> "Vérifie uniquement les apps web avec des URL http/https. Utilisez l'action en haut à droite pour tout vérifier."
        AppLanguage.GERMAN -> "Prüft nur Web-Apps mit http/https-URLs. Oben rechts alle prüfen."
        AppLanguage.RUSSIAN -> "Проверяет только веб-приложения с URL http/https. Кнопка справа вверху проверяет все."
        AppLanguage.JAPANESE -> "http/https の Web アプリのみ検査します。右上の操作ですべて検査できます。"
        AppLanguage.KOREAN -> "http/https 웹 앱만 검사합니다. 오른쪽 위 작업으로 전체 검사할 수 있습니다."
    }
    val healthLastChecked: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "上次检测"
        AppLanguage.ENGLISH -> "Last checked"
        AppLanguage.ARABIC -> "آخر فحص"
        AppLanguage.PORTUGUESE -> "Última verificação"
        AppLanguage.SPANISH -> "Última comprobación"
        AppLanguage.FRENCH -> "Dernière vérif."
        AppLanguage.GERMAN -> "Zuletzt geprüft"
        AppLanguage.RUSSIAN -> "Последняя проверка"
        AppLanguage.JAPANESE -> "最終検査"
        AppLanguage.KOREAN -> "마지막 검사"
    }
    val healthTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "网站健康"
        AppLanguage.ENGLISH -> "Site Health"
        AppLanguage.ARABIC -> "صحة الموقع"
        AppLanguage.PORTUGUESE -> "Saúde do Site"
        AppLanguage.SPANISH -> "Salud del Sitio"
        AppLanguage.FRENCH -> "Santé du site"
        AppLanguage.GERMAN -> "Website-Status"
        AppLanguage.RUSSIAN -> "Состояние сайта"
        AppLanguage.JAPANESE -> "サイトの健全性"
        AppLanguage.KOREAN -> "사이트 상태"
    }
    val healthOnline: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "在线"
        AppLanguage.ENGLISH -> "Online"
        AppLanguage.ARABIC -> "متصل"
        AppLanguage.PORTUGUESE -> "Em linha"
        AppLanguage.SPANISH -> "En línea"
        AppLanguage.FRENCH -> "En ligne"
        AppLanguage.GERMAN -> "Erreichbar"
        AppLanguage.RUSSIAN -> "В сети"
        AppLanguage.JAPANESE -> "オンライン"
        AppLanguage.KOREAN -> "온라인"
    }
    val healthSlow: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "慢速"
        AppLanguage.ENGLISH -> "Slow"
        AppLanguage.ARABIC -> "بطيء"
        AppLanguage.PORTUGUESE -> "Lento"
        AppLanguage.SPANISH -> "Lento"
        AppLanguage.FRENCH -> "Lent"
        AppLanguage.GERMAN -> "Langsam"
        AppLanguage.RUSSIAN -> "Медленно"
        AppLanguage.JAPANESE -> "低速"
        AppLanguage.KOREAN -> "느림"
    }
    val healthOffline: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "离线"
        AppLanguage.ENGLISH -> "Offline"
        AppLanguage.ARABIC -> "غير متصل"
        AppLanguage.PORTUGUESE -> "Offline"
        AppLanguage.SPANISH -> "Sin conexión"
        AppLanguage.FRENCH -> "Hors ligne"
        AppLanguage.GERMAN -> "Nicht erreichbar"
        AppLanguage.RUSSIAN -> "Не в сети"
        AppLanguage.JAPANESE -> "オフライン"
        AppLanguage.KOREAN -> "오프라인"
    }
    val healthUnknown: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "未检测"
        AppLanguage.ENGLISH -> "Unknown"
        AppLanguage.ARABIC -> "غير معروف"
        AppLanguage.PORTUGUESE -> "Desconhecido"
        AppLanguage.SPANISH -> "Desconocido"
        AppLanguage.FRENCH -> "Inconnu"
        AppLanguage.GERMAN -> "Unbekannt"
        AppLanguage.RUSSIAN -> "Неизвестно"
        AppLanguage.JAPANESE -> "不明"
        AppLanguage.KOREAN -> "알 수 없음"
    }
    val healthCheckNow: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "立即检测"
        AppLanguage.ENGLISH -> "Check Now"
        AppLanguage.ARABIC -> "تحقق الآن"
        AppLanguage.PORTUGUESE -> "Verificar Agora"
        AppLanguage.SPANISH -> "Comprobar Ahora"
        AppLanguage.FRENCH -> "Vérifier maintenant"
        AppLanguage.GERMAN -> "Jetzt prüfen"
        AppLanguage.RUSSIAN -> "Проверить сейчас"
        AppLanguage.JAPANESE -> "今すぐ確認"
        AppLanguage.KOREAN -> "지금 확인"
    }
    val healthResponseTime: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "响应时间"
        AppLanguage.ENGLISH -> "Response Time"
        AppLanguage.ARABIC -> "وقت الاستجابة"
        AppLanguage.PORTUGUESE -> "Tempo de Resposta"
        AppLanguage.SPANISH -> "Tiempo de Respuesta"
        AppLanguage.FRENCH -> "Temps de réponse"
        AppLanguage.GERMAN -> "Antwortzeit"
        AppLanguage.RUSSIAN -> "Время отклика"
        AppLanguage.JAPANESE -> "応答時間"
        AppLanguage.KOREAN -> "응답 시간"
    }
    val healthUptime24h: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "24h 在线率"
        AppLanguage.ENGLISH -> "24h Uptime"
        AppLanguage.ARABIC -> "وقت التشغيل 24 ساعة"
        AppLanguage.PORTUGUESE -> "Disponibilidade 24h"
        AppLanguage.SPANISH -> "Disponibilidad 24h"
        AppLanguage.FRENCH -> "Disponibilité 24h"
        AppLanguage.GERMAN -> "24h Verfügbarkeit"
        AppLanguage.RUSSIAN -> "Аптайм 24ч"
        AppLanguage.JAPANESE -> "24時間稼働率"
        AppLanguage.KOREAN -> "24시간 가동률"
    }
    val menuBatchImport: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "批量导入"
        AppLanguage.ENGLISH -> "Batch Import"
        AppLanguage.ARABIC -> "استيراد مجمع"
        AppLanguage.PORTUGUESE -> "Importação em Lote"
        AppLanguage.SPANISH -> "Importación por Lotes"
        AppLanguage.FRENCH -> "Importation par lot"
        AppLanguage.GERMAN -> "Stapelimport"
        AppLanguage.RUSSIAN -> "Пакетный импорт"
        AppLanguage.JAPANESE -> "一括インポート"
        AppLanguage.KOREAN -> "일괄 가져오기"
    }
    val batchImportTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "批量导入"
        AppLanguage.ENGLISH -> "Batch Import"
        AppLanguage.ARABIC -> "استيراد مجمع"
        AppLanguage.PORTUGUESE -> "Importação em Lote"
        AppLanguage.SPANISH -> "Importación por Lotes"
        AppLanguage.FRENCH -> "Importation par lot"
        AppLanguage.GERMAN -> "Stapelimport"
        AppLanguage.RUSSIAN -> "Пакетный импорт"
        AppLanguage.JAPANESE -> "一括インポート"
        AppLanguage.KOREAN -> "일괄 가져오기"
    }
    val batchImportFromText: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "从文本导入"
        AppLanguage.ENGLISH -> "Import from Text"
        AppLanguage.ARABIC -> "استيراد من نص"
        AppLanguage.PORTUGUESE -> "Importar de Texto"
        AppLanguage.SPANISH -> "Importar desde Texto"
        AppLanguage.FRENCH -> "Importer depuis du texte"
        AppLanguage.GERMAN -> "Aus Text importieren"
        AppLanguage.RUSSIAN -> "Импорт из текста"
        AppLanguage.JAPANESE -> "テキストからインポート"
        AppLanguage.KOREAN -> "텍스트에서 가져오기"
    }
    val batchImportFromBookmarks: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "从书签文件导入"
        AppLanguage.ENGLISH -> "Import from Bookmarks"
        AppLanguage.ARABIC -> "استيراد من الإشارات المرجعية"
        AppLanguage.PORTUGUESE -> "Importar de Favoritos"
        AppLanguage.SPANISH -> "Importar desde Marcadores"
        AppLanguage.FRENCH -> "Importer depuis les favoris"
        AppLanguage.GERMAN -> "Aus Lesezeichen importieren"
        AppLanguage.RUSSIAN -> "Импорт из закладок"
        AppLanguage.JAPANESE -> "ブックマークからインポート"
        AppLanguage.KOREAN -> "북마크에서 가져오기"
    }
    val batchImportHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "每行一个，支持：URL、名称|URL、名称 URL、Markdown 链接"
        AppLanguage.ENGLISH -> "One per line: URL, Name|URL, Name URL, or Markdown links"
        AppLanguage.ARABIC -> "واحد لكل سطر: URL أو الاسم|URL أو روابط Markdown"
        AppLanguage.PORTUGUESE -> "Um por linha: URL, Nome|URL, Nome URL ou links Markdown"
        AppLanguage.SPANISH -> "Uno por línea: URL, Nombre|URL, Nombre URL o enlaces Markdown"
        AppLanguage.FRENCH -> "Un par ligne : URL, Nom|URL, Nom URL ou liens Markdown"
        AppLanguage.GERMAN -> "Eine pro Zeile: URL, Name|URL, Name URL oder Markdown-Links"
        AppLanguage.RUSSIAN -> "По одной в строке: URL, Имя|URL, Имя URL или Markdown"
        AppLanguage.JAPANESE -> "1行1件: URL、名前|URL、名前 URL、Markdownリンク"
        AppLanguage.KOREAN -> "한 줄에 하나: URL, 이름|URL, 이름 URL, Markdown 링크"
    }
    val batchImportParsed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已解析 %d 个URL"
        AppLanguage.ENGLISH -> "%d URLs parsed"
        AppLanguage.ARABIC -> "تم تحليل %d روابط"
        AppLanguage.PORTUGUESE -> "%d URLs analisados"
        AppLanguage.SPANISH -> "%d URLs analizados"
        AppLanguage.FRENCH -> "%d URLs analysées"
        AppLanguage.GERMAN -> "%d URLs analysiert"
        AppLanguage.RUSSIAN -> "Разобрано URL: %d"
        AppLanguage.JAPANESE -> "%d 件のURLを解析済み"
        AppLanguage.KOREAN -> "%d개 URL 파싱됨"
    }
    val batchImportSuccess: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "成功导入 %d 个应用"
        AppLanguage.ENGLISH -> "%d apps imported successfully"
        AppLanguage.ARABIC -> "تم استيراد %d تطبيقات بنجاح"
        AppLanguage.PORTUGUESE -> "%d apps importadas com sucesso"
        AppLanguage.SPANISH -> "%d apps importadas con éxito"
        AppLanguage.FRENCH -> "%d applications importées avec succès"
        AppLanguage.GERMAN -> "%d Apps erfolgreich importiert"
        AppLanguage.RUSSIAN -> "Успешно импортировано приложений: %d"
        AppLanguage.JAPANESE -> "%d個のアプリをインポートしました"
        AppLanguage.KOREAN -> "%d개 앱 가져오기 성공"
    }
    val batchImportBtn: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "导入"
        AppLanguage.ENGLISH -> "Import"
        AppLanguage.ARABIC -> "استيراد"
        AppLanguage.PORTUGUESE -> "Importar"
        AppLanguage.SPANISH -> "Importar"
        AppLanguage.FRENCH -> "Importer"
        AppLanguage.GERMAN -> "Importieren"
        AppLanguage.RUSSIAN -> "Импорт"
        AppLanguage.JAPANESE -> "インポート"
        AppLanguage.KOREAN -> "가져오기"
    }
    val batchImportPaste: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "粘贴"
        AppLanguage.ENGLISH -> "Paste"
        AppLanguage.ARABIC -> "لصق"
        AppLanguage.PORTUGUESE -> "Colar"
        AppLanguage.SPANISH -> "Pegar"
        AppLanguage.FRENCH -> "Coller"
        AppLanguage.GERMAN -> "Einfügen"
        AppLanguage.RUSSIAN -> "Вставить"
        AppLanguage.JAPANESE -> "貼り付け"
        AppLanguage.KOREAN -> "붙여넣기"
    }
    val batchImportClear: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "清空"
        AppLanguage.ENGLISH -> "Clear"
        AppLanguage.ARABIC -> "مسح"
        AppLanguage.PORTUGUESE -> "Limpar"
        AppLanguage.SPANISH -> "Borrar"
        AppLanguage.FRENCH -> "Effacer"
        AppLanguage.GERMAN -> "Leeren"
        AppLanguage.RUSSIAN -> "Очистить"
        AppLanguage.JAPANESE -> "クリア"
        AppLanguage.KOREAN -> "지우기"
    }
    val batchImportPickBookmarks: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择书签 HTML 文件"
        AppLanguage.ENGLISH -> "Choose bookmarks HTML file"
        AppLanguage.ARABIC -> "اختر ملف إشارات HTML"
        AppLanguage.PORTUGUESE -> "Escolher arquivo HTML de favoritos"
        AppLanguage.SPANISH -> "Elegir archivo HTML de marcadores"
        AppLanguage.FRENCH -> "Choisir un fichier HTML de favoris"
        AppLanguage.GERMAN -> "Lesezeichen-HTML-Datei wählen"
        AppLanguage.RUSSIAN -> "Выбрать HTML-файл закладок"
        AppLanguage.JAPANESE -> "ブックマークHTMLを選択"
        AppLanguage.KOREAN -> "북마크 HTML 파일 선택"
    }
    val batchImportBookmarksHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "支持 Chrome / Edge / Firefox 导出的 bookmarks.html"
        AppLanguage.ENGLISH -> "Supports bookmarks.html exported from Chrome, Edge, or Firefox"
        AppLanguage.ARABIC -> "يدعم bookmarks.html من Chrome أو Edge أو Firefox"
        AppLanguage.PORTUGUESE -> "Compatível com bookmarks.html do Chrome, Edge ou Firefox"
        AppLanguage.SPANISH -> "Compatible con bookmarks.html de Chrome, Edge o Firefox"
        AppLanguage.FRENCH -> "Compatible avec bookmarks.html de Chrome, Edge ou Firefox"
        AppLanguage.GERMAN -> "Unterstützt bookmarks.html von Chrome, Edge oder Firefox"
        AppLanguage.RUSSIAN -> "Поддерживается bookmarks.html из Chrome, Edge или Firefox"
        AppLanguage.JAPANESE -> "Chrome / Edge / Firefox の bookmarks.html に対応"
        AppLanguage.KOREAN -> "Chrome / Edge / Firefox bookmarks.html 지원"
    }
    val batchImportNoValid: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "未找到有效的 URL"
        AppLanguage.ENGLISH -> "No valid URLs found"
        AppLanguage.ARABIC -> "لم يتم العثور على روابط صالحة"
        AppLanguage.PORTUGUESE -> "Nenhum URL válido encontrado"
        AppLanguage.SPANISH -> "No se encontraron URLs válidos"
        AppLanguage.FRENCH -> "Aucune URL valide trouvée"
        AppLanguage.GERMAN -> "Keine gültigen URLs gefunden"
        AppLanguage.RUSSIAN -> "Действительные URL не найдены"
        AppLanguage.JAPANESE -> "有効なURLが見つかりません"
        AppLanguage.KOREAN -> "유효한 URL을 찾지 못했습니다"
    }
    fun batchImportParseStats(invalid: Int, duplicates: Int): String = when (Strings.lang) {
        AppLanguage.CHINESE -> "无效 %d 行，输入内重复 %d 条"
        AppLanguage.ENGLISH -> "%d invalid lines, %d duplicates in input"
        AppLanguage.ARABIC -> "%d أسطر غير صالحة، %d مكررات في الإدخال"
        AppLanguage.PORTUGUESE -> "%d linhas inválidas, %d duplicatas na entrada"
        AppLanguage.SPANISH -> "%d líneas inválidas, %d duplicados en la entrada"
        AppLanguage.FRENCH -> "%d lignes invalides, %d doublons dans la saisie"
        AppLanguage.GERMAN -> "%d ungültige Zeilen, %d Duplikate in der Eingabe"
        AppLanguage.RUSSIAN -> "Недействительных строк: %d, дубликатов во вводе: %d"
        AppLanguage.JAPANESE -> "無効 %d 行、入力内の重複 %d 件"
        AppLanguage.KOREAN -> "무효 %d줄, 입력 중복 %d개"
    }.let { String.format(it, invalid, duplicates) }
    fun batchImportSkipped(count: Int): String = when (Strings.lang) {
        AppLanguage.CHINESE -> "已跳过重复 %d 条"
        AppLanguage.ENGLISH -> "Skipped %d duplicates"
        AppLanguage.ARABIC -> "تم تخطي %d مكررات"
        AppLanguage.PORTUGUESE -> "%d duplicatas ignoradas"
        AppLanguage.SPANISH -> "Se omitieron %d duplicados"
        AppLanguage.FRENCH -> "%d doublons ignorés"
        AppLanguage.GERMAN -> "%d Duplikate übersprungen"
        AppLanguage.RUSSIAN -> "Пропущено дубликатов: %d"
        AppLanguage.JAPANESE -> "重複 %d 件をスキップ"
        AppLanguage.KOREAN -> "중복 %d개 건너뜀"
    }.let { String.format(it, count) }
    fun batchImportMore(count: Int): String = when (Strings.lang) {
        AppLanguage.CHINESE -> "还有 %d 条…"
        AppLanguage.ENGLISH -> "%d more…"
        AppLanguage.ARABIC -> "%d إضافية…"
        AppLanguage.PORTUGUESE -> "mais %d…"
        AppLanguage.SPANISH -> "%d más…"
        AppLanguage.FRENCH -> "%d de plus…"
        AppLanguage.GERMAN -> "%d weitere…"
        AppLanguage.RUSSIAN -> "ещё %d…"
        AppLanguage.JAPANESE -> "他 %d 件…"
        AppLanguage.KOREAN -> "%d개 더…"
    }.let { String.format(it, count) }
    val menuLinuxEnvironment: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "本地构建环境"
        AppLanguage.ENGLISH -> "Local Build Environment"
        AppLanguage.ARABIC -> "بيئة البناء المحلية"
        AppLanguage.PORTUGUESE -> "Ambiente de Build Local"
        AppLanguage.SPANISH -> "Entorno de Compilación Local"
        AppLanguage.FRENCH -> "Environnement de build local"
        AppLanguage.GERMAN -> "Lokale Build-Umgebung"
        AppLanguage.RUSSIAN -> "Локальная среда сборки"
        AppLanguage.JAPANESE -> "ローカルビルド環境"
        AppLanguage.KOREAN -> "로컬 빌드 환경"
    }

    val createMediaApp: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "媒体应用"
        AppLanguage.ENGLISH -> "Media App"
        AppLanguage.ARABIC -> "تطبيق وسائط"
        AppLanguage.PORTUGUESE -> "App de Mídia"
        AppLanguage.SPANISH -> "App de Medios"
        AppLanguage.FRENCH -> "App Multimédia"
        AppLanguage.GERMAN -> "Medien-App"
        AppLanguage.RUSSIAN -> "Медиа-приложение"
        AppLanguage.JAPANESE -> "メディアアプリ"
        AppLanguage.KOREAN -> "미디어 앱"
    }

    val createHtmlApp: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "HTML App"
        AppLanguage.ENGLISH -> "HTML App"
        AppLanguage.ARABIC -> "تطبيق HTML"
        AppLanguage.PORTUGUESE -> "App HTML"
        AppLanguage.SPANISH -> "App HTML"
        AppLanguage.FRENCH -> "App HTML"
        AppLanguage.GERMAN -> "HTML-App"
        AppLanguage.RUSSIAN -> "HTML-приложение"
        AppLanguage.JAPANESE -> "HTMLアプリ"
        AppLanguage.KOREAN -> "HTML 앱"
    }

    val createFrontendApp: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "前端项目"
        AppLanguage.ENGLISH -> "Frontend Project"
        AppLanguage.ARABIC -> "مشروع الواجهة الأمامية"
        AppLanguage.PORTUGUESE -> "Projeto Frontend"
        AppLanguage.SPANISH -> "Proyecto Frontend"
        AppLanguage.FRENCH -> "Projet Frontend"
        AppLanguage.GERMAN -> "Frontend-Projekt"
        AppLanguage.RUSSIAN -> "Фронтенд-проект"
        AppLanguage.JAPANESE -> "フロントエンドプロジェクト"
        AppLanguage.KOREAN -> "프런트엔드 프로젝트"
    }

    val editFrontendApp: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "编辑前端项目"
        AppLanguage.ENGLISH -> "Edit Frontend Project"
        AppLanguage.ARABIC -> "تعديل مشروع الواجهة الأمامية"
        AppLanguage.PORTUGUESE -> "Editar Projeto Frontend"
        AppLanguage.SPANISH -> "Editar Proyecto Frontend"
        AppLanguage.FRENCH -> "Modifier le projet Frontend"
        AppLanguage.GERMAN -> "Frontend-Projekt bearbeiten"
        AppLanguage.RUSSIAN -> "Изменить фронтенд-проект"
        AppLanguage.JAPANESE -> "フロントエンドプロジェクトを編集"
        AppLanguage.KOREAN -> "프런트엔드 프로젝트 편집"
    }

    val createNodeJsApp: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Node.js 应用"
        AppLanguage.ENGLISH -> "Node.js App"
        AppLanguage.ARABIC -> "تطبيق Node.js"
        AppLanguage.PORTUGUESE -> "App Node.js"
        AppLanguage.SPANISH -> "App Node.js"
        AppLanguage.FRENCH -> "App Node.js"
        AppLanguage.GERMAN -> "Node.js-App"
        AppLanguage.RUSSIAN -> "Приложение Node.js"
        AppLanguage.JAPANESE -> "Node.jsアプリ"
        AppLanguage.KOREAN -> "Node.js 앱"
    }

    val createPhpApp: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "PHP 应用"
        AppLanguage.ENGLISH -> "PHP App"
        AppLanguage.ARABIC -> "تطبيق PHP"
        AppLanguage.PORTUGUESE -> "App PHP"
        AppLanguage.SPANISH -> "App PHP"
        AppLanguage.FRENCH -> "App PHP"
        AppLanguage.GERMAN -> "PHP-App"
        AppLanguage.RUSSIAN -> "Приложение PHP"
        AppLanguage.JAPANESE -> "PHPアプリ"
        AppLanguage.KOREAN -> "PHP 앱"
    }

    val createPythonApp: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Python 应用"
        AppLanguage.ENGLISH -> "Python App"
        AppLanguage.ARABIC -> "تطبيق Python"
        AppLanguage.PORTUGUESE -> "App Python"
        AppLanguage.SPANISH -> "App Python"
        AppLanguage.FRENCH -> "App Python"
        AppLanguage.GERMAN -> "Python-App"
        AppLanguage.RUSSIAN -> "Приложение Python"
        AppLanguage.JAPANESE -> "Pythonアプリ"
        AppLanguage.KOREAN -> "Python 앱"
    }

    val createGoApp: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Go 服务"
        AppLanguage.ENGLISH -> "Go Service"
        AppLanguage.ARABIC -> "خدمة Go"
        AppLanguage.PORTUGUESE -> "Serviço Go"
        AppLanguage.SPANISH -> "Servicio Go"
        AppLanguage.FRENCH -> "Service Go"
        AppLanguage.GERMAN -> "Go-Dienst"
        AppLanguage.RUSSIAN -> "Служба Go"
        AppLanguage.JAPANESE -> "Goサービス"
        AppLanguage.KOREAN -> "Go 서비스"
    }

    val appTypeMultiWeb: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "多站点"
        AppLanguage.ENGLISH -> "Multi-Site"
        AppLanguage.ARABIC -> "متعدد المواقع"
        AppLanguage.PORTUGUESE -> "Multissite"
        AppLanguage.SPANISH -> "Multisitio"
        AppLanguage.FRENCH -> "Multi-site"
        AppLanguage.GERMAN -> "Multi-Site"
        AppLanguage.RUSSIAN -> "Мульти-сайт"
        AppLanguage.JAPANESE -> "マルチサイト"
        AppLanguage.KOREAN -> "멀티사이트"
    }

    val createMultiWebApp: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "多站点聚合"
        AppLanguage.ENGLISH -> "Multi-Site App"
        AppLanguage.ARABIC -> "تطبيق متعدد المواقع"
        AppLanguage.PORTUGUESE -> "App Multi-Site"
        AppLanguage.SPANISH -> "App Multi-Site"
        AppLanguage.FRENCH -> "App Multi-Site"
        AppLanguage.GERMAN -> "Multi-Site-App"
        AppLanguage.RUSSIAN -> "Мульти-сайт приложение"
        AppLanguage.JAPANESE -> "マルチサイトアプリ"
        AppLanguage.KOREAN -> "멀티사이트 앱"
    }

    val multiWebModeFeed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "信息流"
        AppLanguage.ENGLISH -> "Feed"
        AppLanguage.ARABIC -> "خلاصات"
        AppLanguage.PORTUGUESE -> "Feed"
        AppLanguage.SPANISH -> "Feed"
        AppLanguage.FRENCH -> "Flux"
        AppLanguage.GERMAN -> "Feed"
        AppLanguage.RUSSIAN -> "Лента"
        AppLanguage.JAPANESE -> "フィード"
        AppLanguage.KOREAN -> "피드"
    }

    val multiWebDisplayMode: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "界面样式"
        AppLanguage.ENGLISH -> "Layout Style"
        AppLanguage.ARABIC -> "نمط العرض"
        AppLanguage.PORTUGUESE -> "Estilo de Layout"
        AppLanguage.SPANISH -> "Estilo de Diseño"
        AppLanguage.FRENCH -> "Style de mise en page"
        AppLanguage.GERMAN -> "Layout-Stil"
        AppLanguage.RUSSIAN -> "Стиль оформления"
        AppLanguage.JAPANESE -> "レイアウトスタイル"
        AppLanguage.KOREAN -> "레이아웃 스타일"
    }

    val multiWebModeTabs: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "底部标签"
        AppLanguage.ENGLISH -> "Bottom Tabs"
        AppLanguage.ARABIC -> "ألسنة سفلية"
        AppLanguage.PORTUGUESE -> "Abas Inferiores"
        AppLanguage.SPANISH -> "Pestañas Inferiores"
        AppLanguage.FRENCH -> "Onglets en bas"
        AppLanguage.GERMAN -> "Untere Tabs"
        AppLanguage.RUSSIAN -> "Нижние вкладки"
        AppLanguage.JAPANESE -> "下部タブ"
        AppLanguage.KOREAN -> "하단 탭"
    }

    val multiWebModeCards: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "卡片主页"
        AppLanguage.ENGLISH -> "Card Home"
        AppLanguage.ARABIC -> "بطاقات رئيسية"
        AppLanguage.PORTUGUESE -> "Página de Cartões"
        AppLanguage.SPANISH -> "Inicio de Tarjetas"
        AppLanguage.FRENCH -> "Accueil en cartes"
        AppLanguage.GERMAN -> "Karten-Startseite"
        AppLanguage.RUSSIAN -> "Карточки"
        AppLanguage.JAPANESE -> "カードホーム"
        AppLanguage.KOREAN -> "카드 홈"
    }

    val multiWebModeDrawer: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "侧边抽屉"
        AppLanguage.ENGLISH -> "Side Drawer"
        AppLanguage.ARABIC -> "درج جانبي"
        AppLanguage.PORTUGUESE -> "Gaveta Lateral"
        AppLanguage.SPANISH -> "Cajón Lateral"
        AppLanguage.FRENCH -> "Tiroir latéral"
        AppLanguage.GERMAN -> "Seitenmenü"
        AppLanguage.RUSSIAN -> "Боковое меню"
        AppLanguage.JAPANESE -> "サイドドロワー"
        AppLanguage.KOREAN -> "사이드 드로어"
    }

    val multiWebModeTabsDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "底部标签栏常驻，点击切换站点"
        AppLanguage.ENGLISH -> "Persistent bottom bar, tap to switch sites"
        AppLanguage.ARABIC -> "شريط سفلي دائم، انقر للتبديل بين المواقع"
        AppLanguage.PORTUGUESE -> "Barra inferior persistente, toque para alternar sites"
        AppLanguage.SPANISH -> "Barra inferior persistente, toca para cambiar de sitio"
        AppLanguage.FRENCH -> "Barre inférieure persistante, touchez pour changer de site"
        AppLanguage.GERMAN -> "Feste Leiste unten, zum Wechseln tippen"
        AppLanguage.RUSSIAN -> "Постоянная нижняя панель для переключения сайтов"
        AppLanguage.JAPANESE -> "常設の下部バーでサイトを切り替え"
        AppLanguage.KOREAN -> "하단 바 상시 표시, 탭하여 사이트 전환"
    }

    val multiWebModeCardsDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "主页展示站点卡片，点进卡片浏览"
        AppLanguage.ENGLISH -> "Home grid of site cards, tap to browse"
        AppLanguage.ARABIC -> "شبكة بطاقات رئيسية للمواقع، انقر للتصفح"
        AppLanguage.PORTUGUESE -> "Grade inicial de cartões, toque para navegar"
        AppLanguage.SPANISH -> "Cuadrícula de tarjetas de sitios, toca para navegar"
        AppLanguage.FRENCH -> "Grille d'accueil de cartes, touchez pour naviguer"
        AppLanguage.GERMAN -> "Startseiten-Raster mit Karten, zum Öffnen tippen"
        AppLanguage.RUSSIAN -> "Главная с карточками сайтов, нажмите для просмотра"
        AppLanguage.JAPANESE -> "ホームにサイトカードを並べ、タップで閲覧"
        AppLanguage.KOREAN -> "홈에 사이트 카드 표시, 탭하여 열기"
    }

    val multiWebModeDrawerDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "左上角菜单或侧滑打开站点列表"
        AppLanguage.ENGLISH -> "Site list in a side menu, top-left button or edge swipe"
        AppLanguage.ARABIC -> "قائمة المواقع في قائمة جانبية، زر أعلى اليسار أو سحب من الحافة"
        AppLanguage.PORTUGUESE -> "Lista de sites em menu lateral, botão superior ou deslize da borda"
        AppLanguage.SPANISH -> "Lista de sitios en menú lateral, botón superior o deslizamiento"
        AppLanguage.FRENCH -> "Liste des sites dans un menu latéral, bouton ou balayage"
        AppLanguage.GERMAN -> "Seitenliste im Seitenmenü, oben links oder Wischgeste"
        AppLanguage.RUSSIAN -> "Список сайтов в боковом меню, кнопка или свайп от края"
        AppLanguage.JAPANESE -> "サイドメニューでサイト一覧、左上ボタンか端スワイプ"
        AppLanguage.KOREAN -> "사이드 메뉴의 사이트 목록, 좌상단 버튼 또는 가장자리 스와이프"
    }

    val multiWebModeFeedDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "聚合各站点文章列表，适合资讯类站点"
        AppLanguage.ENGLISH -> "Aggregates article lists from all sites, best for news/blog sites"
        AppLanguage.ARABIC -> "يجمع قوائم المقالات من كل المواقع، مناسب لمواقع الأخبار"
        AppLanguage.PORTUGUESE -> "Agrega listas de artigos de todos os sites, ideal para notícias/blogs"
        AppLanguage.SPANISH -> "Agrega listas de artículos de todos los sitios, ideal para noticias/blogs"
        AppLanguage.FRENCH -> "Agrège les articles de tous les sites, idéal pour l'actualité/blogs"
        AppLanguage.GERMAN -> "Aggregiert Artikellisten aller Seiten, ideal für News/Blogs"
        AppLanguage.RUSSIAN -> "Собирает статьи со всех сайтов в ленту, для новостей/блогов"
        AppLanguage.JAPANESE -> "全サイトの記事を集約、ニュース/ブログ向け"
        AppLanguage.KOREAN -> "모든 사이트의 글을 모아 보여줌, 뉴스/블로그에 적합"
    }

    val multiWebShowSiteIcons: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "卡片上显示站点图标"
        AppLanguage.ENGLISH -> "Show site icons on cards"
        AppLanguage.ARABIC -> "إظهار أيقونات المواقع على البطاقات"
        AppLanguage.PORTUGUESE -> "Mostrar ícones dos sites nos cartões"
        AppLanguage.SPANISH -> "Mostrar iconos de sitios en tarjetas"
        AppLanguage.FRENCH -> "Afficher les icônes sur les cartes"
        AppLanguage.GERMAN -> "Seiten-Icons auf Karten anzeigen"
        AppLanguage.RUSSIAN -> "Показывать значки сайтов на карточках"
        AppLanguage.JAPANESE -> "カードにサイトアイコンを表示"
        AppLanguage.KOREAN -> "카드에 사이트 아이콘 표시"
    }

    val multiWebSitesInheritConfig: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "子站点跟随主应用配置"
        AppLanguage.ENGLISH -> "Sites inherit the main app's config"
        AppLanguage.ARABIC -> "المواقع تتبع إعدادات التطبيق الرئيسي"
        AppLanguage.PORTUGUESE -> "Sites herdam a configuração do app principal"
        AppLanguage.SPANISH -> "Los sitios heredan la configuración de la app principal"
        AppLanguage.FRENCH -> "Les sites héritent de la config de l'app principale"
        AppLanguage.GERMAN -> "Seiten übernehmen die Haupt-App-Konfiguration"
        AppLanguage.RUSSIAN -> "Сайты наследуют настройки основного приложения"
        AppLanguage.JAPANESE -> "サイトはメインアプリの設定を継承"
        AppLanguage.KOREAN -> "사이트가 메인 앱 설정을 상속"
    }

    val multiWebSitesInheritConfigHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "仅加载子站点的内容与资源，通用配置以主应用为准"
        AppLanguage.ENGLISH -> "Only site content and resources load; common settings follow the main app"
        AppLanguage.ARABIC -> "يتم تحميل محتوى المواقع ومواردها فقط؛ الإعدادات العامة تتبع التطبيق الرئيسي"
        AppLanguage.PORTUGUESE -> "Carrega apenas conteúdo e recursos dos sites; as configurações seguem o app principal"
        AppLanguage.SPANISH -> "Solo se cargan contenido y recursos de los sitios; la configuración sigue a la app principal"
        AppLanguage.FRENCH -> "Seuls le contenu et les ressources des sites sont chargés ; les réglages suivent l'app principale"
        AppLanguage.GERMAN -> "Nur Inhalte und Ressourcen der Seiten werden geladen; allgemeine Einstellungen folgen der Haupt-App"
        AppLanguage.RUSSIAN -> "Загружаются только контент и ресурсы сайтов; общие настройки берутся из основного приложения"
        AppLanguage.JAPANESE -> "サイトのコンテンツとリソースのみ読み込み、共通設定はメインアプリに従います"
        AppLanguage.KOREAN -> "사이트의 콘텐츠와 리소스만 로드하고 공통 설정은 메인 앱을 따릅니다"
    }

    val multiWebFeedEmpty: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "暂无文章"
        AppLanguage.ENGLISH -> "No articles found"
        AppLanguage.ARABIC -> "لا توجد مقالات"
        AppLanguage.PORTUGUESE -> "Nenhum artigo encontrado"
        AppLanguage.SPANISH -> "No se encontraron artículos"
        AppLanguage.FRENCH -> "Aucun article trouvé"
        AppLanguage.GERMAN -> "Keine Artikel gefunden"
        AppLanguage.RUSSIAN -> "Статей не найдено"
        AppLanguage.JAPANESE -> "記事が見つかりません"
        AppLanguage.KOREAN -> "글이 없습니다"
    }

    val multiWebFeedEmptyHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "站点暂无可聚合的内容，点右上角刷新重试"
        AppLanguage.ENGLISH -> "Nothing to aggregate yet — tap refresh to retry"
        AppLanguage.ARABIC -> "لا يوجد محتوى للتجميع بعد — انقر على تحديث لإعادة المحاولة"
        AppLanguage.PORTUGUESE -> "Nada para agregar ainda — toque em atualizar para tentar de novo"
        AppLanguage.SPANISH -> "Aún no hay contenido para agregar — toca actualizar para reintentar"
        AppLanguage.FRENCH -> "Rien à agréger pour l'instant — touchez actualiser pour réessayer"
        AppLanguage.GERMAN -> "Noch nichts zum Aggregieren — zum Wiederholen aktualisieren tippen"
        AppLanguage.RUSSIAN -> "Пока нечего собирать — нажмите «Обновить» для повтора"
        AppLanguage.JAPANESE -> "集約するコンテンツがありません — 更新をタップして再試行"
        AppLanguage.KOREAN -> "아직 집계할 콘텐츠가 없습니다 — 새로고침을 눌러 다시 시도하세요"
    }

    val multiWebFeedStats: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "来自 %2\$d 个站点的 %1\$d 篇文章"
        AppLanguage.ENGLISH -> "%1\$d articles from %2\$d sites"
        AppLanguage.ARABIC -> "%1\$d مقالات من %2\$d مواقع"
        AppLanguage.PORTUGUESE -> "%1\$d artigos de %2\$d sites"
        AppLanguage.SPANISH -> "%1\$d artículos de %2\$d sitios"
        AppLanguage.FRENCH -> "%1\$d articles de %2\$d sites"
        AppLanguage.GERMAN -> "%1\$d Artikel aus %2\$d Seiten"
        AppLanguage.RUSSIAN -> "%1\$d статей с %2\$d сайтов"
        AppLanguage.JAPANESE -> "%2\$d サイトから %1\$d 件の記事"
        AppLanguage.KOREAN -> "%2\$d개 사이트의 글 %1\$d개"
    }

    val multiWebNoSiteSelected: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "未选择站点"
        AppLanguage.ENGLISH -> "No site selected"
        AppLanguage.ARABIC -> "لم يتم اختيار موقع"
        AppLanguage.PORTUGUESE -> "Nenhum site selecionado"
        AppLanguage.SPANISH -> "Ningún sitio seleccionado"
        AppLanguage.FRENCH -> "Aucun site sélectionné"
        AppLanguage.GERMAN -> "Keine Seite ausgewählt"
        AppLanguage.RUSSIAN -> "Сайт не выбран"
        AppLanguage.JAPANESE -> "サイトが選択されていません"
        AppLanguage.KOREAN -> "선택된 사이트 없음"
    }

    val multiWebAddSite: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "添加站点"
        AppLanguage.ENGLISH -> "Add Site"
        AppLanguage.ARABIC -> "إضافة موقع"
        AppLanguage.PORTUGUESE -> "Adicionar Site"
        AppLanguage.SPANISH -> "Añadir Sitio"
        AppLanguage.FRENCH -> "Ajouter un site"
        AppLanguage.GERMAN -> "Seite hinzufügen"
        AppLanguage.RUSSIAN -> "Добавить сайт"
        AppLanguage.JAPANESE -> "サイトを追加"
        AppLanguage.KOREAN -> "사이트 추가"
    }

    val multiWebTypeExisting: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已有应用"
        AppLanguage.ENGLISH -> "Existing App"
        AppLanguage.ARABIC -> "تطبيق موجود"
        AppLanguage.PORTUGUESE -> "App Existente"
        AppLanguage.SPANISH -> "App Existente"
        AppLanguage.FRENCH -> "App existante"
        AppLanguage.GERMAN -> "Bestehende App"
        AppLanguage.RUSSIAN -> "Существующее приложение"
        AppLanguage.JAPANESE -> "既存アプリ"
        AppLanguage.KOREAN -> "기존 앱"
    }

    val multiWebCustomCodeSection: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自定义代码注入"
        AppLanguage.ENGLISH -> "Custom Code Injection"
        AppLanguage.ARABIC -> "حقن كود مخصص"
        AppLanguage.PORTUGUESE -> "Injeção de Código Personalizado"
        AppLanguage.SPANISH -> "Inyección de Código Personalizado"
        AppLanguage.FRENCH -> "Injection de code personnalisé"
        AppLanguage.GERMAN -> "Benutzerdefinierte Code-Injektion"
        AppLanguage.RUSSIAN -> "Внедрение пользовательского кода"
        AppLanguage.JAPANESE -> "カスタムコード注入"
        AppLanguage.KOREAN -> "사용자 정의 코드 주입"
    }

    val multiWebCustomCodeDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "为所有站点注入自定义 JS / CSS，在每个页面加载时运行。可用 location.host 判断作用于哪个站点。"
        AppLanguage.ENGLISH -> "Inject custom JS / CSS into every site, run on each page load. Use location.host to target a specific site."
        AppLanguage.ARABIC -> "حقن JS / CSS مخصص في كل المواقع، يُشغّل عند تحميل كل صفحة. استخدم location.host لاستهداف موقع معيّن."
        AppLanguage.PORTUGUESE -> "Injeta JS / CSS personalizado em todos os sites, executado a cada carregamento de página. Use location.host para visar um site específico."
        AppLanguage.SPANISH -> "Inyecta JS / CSS personalizado en todos los sitios, se ejecuta en cada carga de página. Usa location.host para targeting un sitio específico."
        AppLanguage.FRENCH -> "Injecte du JS / CSS personnalisé dans tous les sites, exécuté à chaque chargement de page. Utilisez location.host pour cibler un site spécifique."
        AppLanguage.GERMAN -> "Injiziert benutzerdefiniertes JS / CSS in jede Seite, ausgeführt bei jedem Seitenladen. Verwende location.host, um eine bestimmte Seite anzusprechen."
        AppLanguage.RUSSIAN -> "Внедряет пользовательский JS / CSS во все сайты, выполняется при каждой загрузке страницы. Используйте location.host для конкретного сайта."
        AppLanguage.JAPANESE -> "全サイトにカスタムJS / CSSを注入し、各ページ読み込み時に実行します。location.hostで特定サイトを指定できます。"
        AppLanguage.KOREAN -> "모든 사이트에 사용자 정의 JS / CSS를 주입하여 각 페이지 로드 시 실행합니다. location.host로 특정 사이트를 지정할 수 있습니다."
    }

    val multiWebNoApps: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "暂无已创建的应用"
        AppLanguage.ENGLISH -> "No apps created yet"
        AppLanguage.ARABIC -> "لا توجد تطبيقات بعد"
        AppLanguage.PORTUGUESE -> "Nenhum app criado ainda"
        AppLanguage.SPANISH -> "Aún no se han creado apps"
        AppLanguage.FRENCH -> "Aucune application créée pour le moment"
        AppLanguage.GERMAN -> "Noch keine Apps erstellt"
        AppLanguage.RUSSIAN -> "Приложений пока не создано"
        AppLanguage.JAPANESE -> "まだアプリが作成されていません"
        AppLanguage.KOREAN -> "아직 생성된 앱이 없습니다"
    }

    val multiWebSiteList: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "站点列表"
        AppLanguage.ENGLISH -> "Sites"
        AppLanguage.ARABIC -> "المواقع"
        AppLanguage.PORTUGUESE -> "Sites"
        AppLanguage.SPANISH -> "Sitios"
        AppLanguage.FRENCH -> "Sites web"
        AppLanguage.GERMAN -> "Seiten"
        AppLanguage.RUSSIAN -> "Сайты"
        AppLanguage.JAPANESE -> "サイト"
        AppLanguage.KOREAN -> "사이트"
    }

    val multiWebNoSites: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "尚未添加任何站点"
        AppLanguage.ENGLISH -> "No sites added yet"
        AppLanguage.ARABIC -> "لم تتم إضافة مواقع بعد"
        AppLanguage.PORTUGUESE -> "Nenhum site adicionado ainda"
        AppLanguage.SPANISH -> "Aún no se han añadido sitios"
        AppLanguage.FRENCH -> "Aucun site ajouté pour le moment"
        AppLanguage.GERMAN -> "Noch keine Seiten hinzugefügt"
        AppLanguage.RUSSIAN -> "Сайты пока не добавлены"
        AppLanguage.JAPANESE -> "まだサイトが追加されていません"
        AppLanguage.KOREAN -> "아직 추가된 사이트가 없습니다"
    }

    val multiWebSiteCount: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "%d 个站点"
        AppLanguage.ENGLISH -> "%d sites"
        AppLanguage.ARABIC -> "%d مواقع"
        AppLanguage.PORTUGUESE -> "%d sites"
        AppLanguage.SPANISH -> "%d sitios"
        AppLanguage.FRENCH -> "%d sites web"
        AppLanguage.GERMAN -> "%d Seiten"
        AppLanguage.RUSSIAN -> "%d сайтов"
        AppLanguage.JAPANESE -> "%d サイト"
        AppLanguage.KOREAN -> "%d개 사이트"
    }

    val multiWebDeleteSite: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "删除站点"
        AppLanguage.ENGLISH -> "Delete Site"
        AppLanguage.ARABIC -> "حذف الموقع"
        AppLanguage.PORTUGUESE -> "Excluir Site"
        AppLanguage.SPANISH -> "Eliminar Sitio"
        AppLanguage.FRENCH -> "Supprimer le site"
        AppLanguage.GERMAN -> "Seite löschen"
        AppLanguage.RUSSIAN -> "Удалить сайт"
        AppLanguage.JAPANESE -> "サイトを削除"
        AppLanguage.KOREAN -> "사이트 삭제"
    }

    val multiWebDisableSite: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "禁用站点"
        AppLanguage.ENGLISH -> "Disable Site"
        AppLanguage.ARABIC -> "تعطيل الموقع"
        AppLanguage.PORTUGUESE -> "Desativar Site"
        AppLanguage.SPANISH -> "Desactivar Sitio"
        AppLanguage.FRENCH -> "Désactiver le site"
        AppLanguage.GERMAN -> "Seite deaktivieren"
        AppLanguage.RUSSIAN -> "Отключить сайт"
        AppLanguage.JAPANESE -> "サイトを無効化"
        AppLanguage.KOREAN -> "사이트 비활성화"
    }

    val multiWebEnableSite: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "启用站点"
        AppLanguage.ENGLISH -> "Enable Site"
        AppLanguage.ARABIC -> "تمكين الموقع"
        AppLanguage.PORTUGUESE -> "Ativar Site"
        AppLanguage.SPANISH -> "Activar Sitio"
        AppLanguage.FRENCH -> "Activer le site"
        AppLanguage.GERMAN -> "Seite aktivieren"
        AppLanguage.RUSSIAN -> "Включить сайт"
        AppLanguage.JAPANESE -> "サイトを有効化"
        AppLanguage.KOREAN -> "사이트 활성화"
    }

    val multiWebMoveUp: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "上移"
        AppLanguage.ENGLISH -> "Move Up"
        AppLanguage.ARABIC -> "نقل للأعلى"
        AppLanguage.PORTUGUESE -> "Mover para Cima"
        AppLanguage.SPANISH -> "Mover Arriba"
        AppLanguage.FRENCH -> "Monter"
        AppLanguage.GERMAN -> "Nach oben"
        AppLanguage.RUSSIAN -> "Вверх"
        AppLanguage.JAPANESE -> "上に移動"
        AppLanguage.KOREAN -> "위로 이동"
    }

    val multiWebMoveDown: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下移"
        AppLanguage.ENGLISH -> "Move Down"
        AppLanguage.ARABIC -> "نقل للأسفل"
        AppLanguage.PORTUGUESE -> "Mover para Baixo"
        AppLanguage.SPANISH -> "Mover Abajo"
        AppLanguage.FRENCH -> "Descendre"
        AppLanguage.GERMAN -> "Nach unten"
        AppLanguage.RUSSIAN -> "Вниз"
        AppLanguage.JAPANESE -> "下に移動"
        AppLanguage.KOREAN -> "아래로 이동"
    }

    val multiWebAddCustomSite: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自定义网页"
        AppLanguage.ENGLISH -> "Custom Site"
        AppLanguage.ARABIC -> "موقع مخصص"
        AppLanguage.PORTUGUESE -> "Site Personalizado"
        AppLanguage.SPANISH -> "Sitio Personalizado"
        AppLanguage.FRENCH -> "Site personnalisé"
        AppLanguage.GERMAN -> "Benutzerdefinierte Seite"
        AppLanguage.RUSSIAN -> "Свой сайт"
        AppLanguage.JAPANESE -> "カスタムサイト"
        AppLanguage.KOREAN -> "사용자 지정 사이트"
    }

    val multiWebEditSite: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "编辑站点"
        AppLanguage.ENGLISH -> "Edit Site"
        AppLanguage.ARABIC -> "تحرير الموقع"
        AppLanguage.PORTUGUESE -> "Editar Site"
        AppLanguage.SPANISH -> "Editar Sitio"
        AppLanguage.FRENCH -> "Modifier le site"
        AppLanguage.GERMAN -> "Seite bearbeiten"
        AppLanguage.RUSSIAN -> "Изменить сайт"
        AppLanguage.JAPANESE -> "サイトを編集"
        AppLanguage.KOREAN -> "사이트 편집"
    }

    val appTypeWeb: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "网页"
        AppLanguage.ENGLISH -> "Web"
        AppLanguage.ARABIC -> "ويب"
        AppLanguage.PORTUGUESE -> "Web"
        AppLanguage.SPANISH -> "Web"
        AppLanguage.FRENCH -> "Web"
        AppLanguage.GERMAN -> "Web"
        AppLanguage.RUSSIAN -> "Веб"
        AppLanguage.JAPANESE -> "ウェブ"
        AppLanguage.KOREAN -> "웹"
    }

    val appTypeImage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "图片"
        AppLanguage.ENGLISH -> "Image"
        AppLanguage.ARABIC -> "صورة"
        AppLanguage.PORTUGUESE -> "Imagem"
        AppLanguage.SPANISH -> "Imagen"
        AppLanguage.FRENCH -> "Image"
        AppLanguage.GERMAN -> "Bild"
        AppLanguage.RUSSIAN -> "Изображение"
        AppLanguage.JAPANESE -> "画像"
        AppLanguage.KOREAN -> "이미지"
    }

    val appTypeVideo: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "视频"
        AppLanguage.ENGLISH -> "Video"
        AppLanguage.ARABIC -> "فيديو"
        AppLanguage.PORTUGUESE -> "Vídeo"
        AppLanguage.SPANISH -> "Vídeo"
        AppLanguage.FRENCH -> "Vidéo"
        AppLanguage.GERMAN -> "Video"
        AppLanguage.RUSSIAN -> "Видео"
        AppLanguage.JAPANESE -> "動画"
        AppLanguage.KOREAN -> "동영상"
    }

    val appTypeHtml: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "HTML"
        AppLanguage.ENGLISH -> "HTML"
        AppLanguage.ARABIC -> "HTML"
        AppLanguage.PORTUGUESE -> "HTML"
        AppLanguage.SPANISH -> "HTML"
        AppLanguage.FRENCH -> "HTML"
        AppLanguage.GERMAN -> "HTML"
        AppLanguage.RUSSIAN -> "HTML"
        AppLanguage.JAPANESE -> "HTML"
        AppLanguage.KOREAN -> "HTML"
    }

    val appTypeGallery: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "画廊"
        AppLanguage.ENGLISH -> "Gallery"
        AppLanguage.ARABIC -> "معرض"
        AppLanguage.PORTUGUESE -> "Galeria"
        AppLanguage.SPANISH -> "Galería"
        AppLanguage.FRENCH -> "Galerie"
        AppLanguage.GERMAN -> "Galerie"
        AppLanguage.RUSSIAN -> "Галерея"
        AppLanguage.JAPANESE -> "ギャラリー"
        AppLanguage.KOREAN -> "갤러리"
    }

    val appTypeFrontend: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "前端"
        AppLanguage.ENGLISH -> "Frontend"
        AppLanguage.ARABIC -> "الواجهة الأمامية"
        AppLanguage.PORTUGUESE -> "Frontend"
        AppLanguage.SPANISH -> "Frontend"
        AppLanguage.FRENCH -> "Frontend"
        AppLanguage.GERMAN -> "Frontend"
        AppLanguage.RUSSIAN -> "Фронтенд"
        AppLanguage.JAPANESE -> "フロントエンド"
        AppLanguage.KOREAN -> "프런트엔드"
    }

    val appTypeWordPress: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "WordPress"
        AppLanguage.ENGLISH -> "WordPress"
        AppLanguage.ARABIC -> "WordPress"
        AppLanguage.PORTUGUESE -> "WordPress"
        AppLanguage.SPANISH -> "WordPress"
        AppLanguage.FRENCH -> "WordPress"
        AppLanguage.GERMAN -> "WordPress"
        AppLanguage.RUSSIAN -> "WordPress"
        AppLanguage.JAPANESE -> "WordPress"
        AppLanguage.KOREAN -> "WordPress"
    }

    val appTypeNodeJs: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Node.js"
        AppLanguage.ENGLISH -> "Node.js"
        AppLanguage.ARABIC -> "Node.js"
        AppLanguage.PORTUGUESE -> "Node.js"
        AppLanguage.SPANISH -> "Node.js"
        AppLanguage.FRENCH -> "Node.js"
        AppLanguage.GERMAN -> "Node.js"
        AppLanguage.RUSSIAN -> "Node.js"
        AppLanguage.JAPANESE -> "Node.js"
        AppLanguage.KOREAN -> "Node.js"
    }

    val appTypePhp: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "PHP"
        AppLanguage.ENGLISH -> "PHP"
        AppLanguage.ARABIC -> "PHP"
        AppLanguage.PORTUGUESE -> "PHP"
        AppLanguage.SPANISH -> "PHP"
        AppLanguage.FRENCH -> "PHP"
        AppLanguage.GERMAN -> "PHP"
        AppLanguage.RUSSIAN -> "PHP"
        AppLanguage.JAPANESE -> "PHP"
        AppLanguage.KOREAN -> "PHP"
    }

    val appTypePython: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Python"
        AppLanguage.ENGLISH -> "Python"
        AppLanguage.ARABIC -> "Python"
        AppLanguage.PORTUGUESE -> "Python"
        AppLanguage.SPANISH -> "Python"
        AppLanguage.FRENCH -> "Python"
        AppLanguage.GERMAN -> "Python"
        AppLanguage.RUSSIAN -> "Python"
        AppLanguage.JAPANESE -> "Python"
        AppLanguage.KOREAN -> "Python"
    }

    val appTypeGo: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Go"
        AppLanguage.ENGLISH -> "Go"
        AppLanguage.ARABIC -> "Go"
        AppLanguage.PORTUGUESE -> "Go"
        AppLanguage.SPANISH -> "Go"
        AppLanguage.FRENCH -> "Go"
        AppLanguage.GERMAN -> "Go"
        AppLanguage.RUSSIAN -> "Go"
        AppLanguage.JAPANESE -> "Go"
        AppLanguage.KOREAN -> "Go"
    }

    val dirNotExists: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "目录不存在"
        AppLanguage.ENGLISH -> "Directory does not exist"
        AppLanguage.ARABIC -> "المجلد غير موجود"
        AppLanguage.PORTUGUESE -> "O diretório não existe"
        AppLanguage.SPANISH -> "El directorio no existe"
        AppLanguage.FRENCH -> "Le répertoire n'existe pas"
        AppLanguage.GERMAN -> "Verzeichnis existiert nicht"
        AppLanguage.RUSSIAN -> "Каталог не существует"
        AppLanguage.JAPANESE -> "ディレクトリが存在しません"
        AppLanguage.KOREAN -> "디렉터리가 존재하지 않습니다"
    }

    val projectImportFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "项目导入失败"
        AppLanguage.ENGLISH -> "Project import failed"
        AppLanguage.ARABIC -> "فشل استيراد المشروع"
        AppLanguage.PORTUGUESE -> "Falha na importação do projeto"
        AppLanguage.SPANISH -> "Error al importar el proyecto"
        AppLanguage.FRENCH -> "Échec de l'importation du projet"
        AppLanguage.GERMAN -> "Projektimport fehlgeschlagen"
        AppLanguage.RUSSIAN -> "Не удалось импортировать проект"
        AppLanguage.JAPANESE -> "プロジェクトのインポートに失敗しました"
        AppLanguage.KOREAN -> "프로젝트 가져오기 실패"
    }

    val frameworkDetected: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "检测到框架"
        AppLanguage.ENGLISH -> "Framework Detected"
        AppLanguage.ARABIC -> "تم اكتشاف الإطار"
        AppLanguage.PORTUGUESE -> "Framework Detectado"
        AppLanguage.SPANISH -> "Framework Detectado"
        AppLanguage.FRENCH -> "Framework détecté"
        AppLanguage.GERMAN -> "Framework erkannt"
        AppLanguage.RUSSIAN -> "Фреймворк обнаружен"
        AppLanguage.JAPANESE -> "フレームワークを検出"
        AppLanguage.KOREAN -> "프레임워크 감지됨"
    }

    val preparingEnv: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "正在准备环境..."
        AppLanguage.ENGLISH -> "Preparing environment..."
        AppLanguage.ARABIC -> "جارٍ تحضير البيئة..."
        AppLanguage.PORTUGUESE -> "Preparando ambiente..."
        AppLanguage.SPANISH -> "Preparando entorno..."
        AppLanguage.FRENCH -> "Préparation de l'environnement..."
        AppLanguage.GERMAN -> "Umgebung wird vorbereitet..."
        AppLanguage.RUSSIAN -> "Подготовка среды..."
        AppLanguage.JAPANESE -> "環境を準備中..."
        AppLanguage.KOREAN -> "환경 준비 중..."
    }

    val startingServer: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "正在准备应用..."
        AppLanguage.ENGLISH -> "Preparing your app..."
        AppLanguage.ARABIC -> "جارٍ تجهيز التطبيق..."
        AppLanguage.PORTUGUESE -> "Preparando seu aplicativo..."
        AppLanguage.SPANISH -> "Preparando tu aplicación..."
        AppLanguage.FRENCH -> "Préparation de votre application..."
        AppLanguage.GERMAN -> "Ihre App wird vorbereitet..."
        AppLanguage.RUSSIAN -> "Подготовка приложения..."
        AppLanguage.JAPANESE -> "アプリを準備中..."
        AppLanguage.KOREAN -> "앱을 준비하는 중..."
    }

    val serverStartFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "服务启动失败"
        AppLanguage.ENGLISH -> "Server failed to start"
        AppLanguage.ARABIC -> "فشل تشغيل الخادم"
        AppLanguage.PORTUGUESE -> "Falha ao iniciar o servidor"
        AppLanguage.SPANISH -> "Error al iniciar el servidor"
        AppLanguage.FRENCH -> "Échec du démarrage du serveur"
        AppLanguage.GERMAN -> "Serverstart fehlgeschlagen"
        AppLanguage.RUSSIAN -> "Не удалось запустить сервер"
        AppLanguage.JAPANESE -> "サーバーの起動に失敗しました"
        AppLanguage.KOREAN -> "서버 시작 실패"
    }

    val errorScreenTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "应用启动遇到问题"
        AppLanguage.ENGLISH -> "Something went wrong while starting"
        AppLanguage.ARABIC -> "حدثت مشكلة أثناء بدء التشغيل"
        AppLanguage.PORTUGUESE -> "Algo deu errado ao iniciar"
        AppLanguage.SPANISH -> "Algo salió mal al iniciar"
        AppLanguage.FRENCH -> "Une erreur est survenue au démarrage"
        AppLanguage.GERMAN -> "Beim Start ist ein Fehler aufgetreten"
        AppLanguage.RUSSIAN -> "При запуске возникла проблема"
        AppLanguage.JAPANESE -> "起動中に問題が発生しました"
        AppLanguage.KOREAN -> "시작 중 문제가 발생했습니다"
    }
    val errorShowDetails: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "显示详细信息"
        AppLanguage.ENGLISH -> "Show details"
        AppLanguage.ARABIC -> "عرض التفاصيل"
        AppLanguage.PORTUGUESE -> "Mostrar detalhes"
        AppLanguage.SPANISH -> "Mostrar detalles"
        AppLanguage.FRENCH -> "Afficher les détails"
        AppLanguage.GERMAN -> "Details anzeigen"
        AppLanguage.RUSSIAN -> "Показать подробности"
        AppLanguage.JAPANESE -> "詳細を表示"
        AppLanguage.KOREAN -> "세부정보 표시"
    }
    val errorHideDetails: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "隐藏详细信息"
        AppLanguage.ENGLISH -> "Hide details"
        AppLanguage.ARABIC -> "إخفاء التفاصيل"
        AppLanguage.PORTUGUESE -> "Ocultar detalhes"
        AppLanguage.SPANISH -> "Ocultar detalles"
        AppLanguage.FRENCH -> "Masquer les détails"
        AppLanguage.GERMAN -> "Details ausblenden"
        AppLanguage.RUSSIAN -> "Скрыть подробности"
        AppLanguage.JAPANESE -> "詳細を非表示"
        AppLanguage.KOREAN -> "세부정보 숨기기"
    }
    val errorCopyDetails: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "复制详细信息"
        AppLanguage.ENGLISH -> "Copy details"
        AppLanguage.ARABIC -> "نسخ التفاصيل"
        AppLanguage.PORTUGUESE -> "Copiar detalhes"
        AppLanguage.SPANISH -> "Copiar detalles"
        AppLanguage.FRENCH -> "Copier les détails"
        AppLanguage.GERMAN -> "Details kopieren"
        AppLanguage.RUSSIAN -> "Копировать подробности"
        AppLanguage.JAPANESE -> "詳細をコピー"
        AppLanguage.KOREAN -> "세부정보 복사"
    }
    val errorCopied: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已复制详细信息"
        AppLanguage.ENGLISH -> "Details copied"
        AppLanguage.ARABIC -> "تم نسخ التفاصيل"
        AppLanguage.PORTUGUESE -> "Detalhes copiados"
        AppLanguage.SPANISH -> "Detalles copiados"
        AppLanguage.FRENCH -> "Détails copiés"
        AppLanguage.GERMAN -> "Details kopiert"
        AppLanguage.RUSSIAN -> "Подробности скопированы"
        AppLanguage.JAPANESE -> "詳細をコピーしました"
        AppLanguage.KOREAN -> "세부정보가 복사되었습니다"
    }
    val errorRetry: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "重试"
        AppLanguage.ENGLISH -> "Retry"
        AppLanguage.ARABIC -> "إعادة المحاولة"
        AppLanguage.PORTUGUESE -> "Tentar novamente"
        AppLanguage.SPANISH -> "Reintentar"
        AppLanguage.FRENCH -> "Réessayer"
        AppLanguage.GERMAN -> "Erneut versuchen"
        AppLanguage.RUSSIAN -> "Повторить"
        AppLanguage.JAPANESE -> "再試行"
        AppLanguage.KOREAN -> "재시도"
    }

    val phpSupportedFrameworks: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "支持 Laravel、ThinkPHP、CodeIgniter、Slim。"
        AppLanguage.ENGLISH -> "Supports Laravel, ThinkPHP, CodeIgniter, Slim and plain PHP projects"
        AppLanguage.ARABIC -> "يدعم Laravel و ThinkPHP و CodeIgniter و Slim ومشاريع PHP الأصلية"
        AppLanguage.PORTUGUESE -> "Suporta Laravel, ThinkPHP, CodeIgniter, Slim e projetos PHP puros"
        AppLanguage.SPANISH -> "Soporta Laravel, ThinkPHP, CodeIgniter, Slim y proyectos PHP puros"
        AppLanguage.FRENCH -> "Prend en charge Laravel, ThinkPHP, CodeIgniter, Slim et les projets PHP purs"
        AppLanguage.GERMAN -> "Unterstützt Laravel, ThinkPHP, CodeIgniter, Slim und reine PHP-Projekte"
        AppLanguage.RUSSIAN -> "Поддерживает Laravel, ThinkPHP, CodeIgniter, Slim и чистые PHP-проекты"
        AppLanguage.JAPANESE -> "Laravel、ThinkPHP、CodeIgniter、Slim、および純粋なPHPプロジェクトに対応"
        AppLanguage.KOREAN -> "Laravel, ThinkPHP, CodeIgniter, Slim 및 순수 PHP 프로젝트 지원"
    }

    val pySupportedFrameworks: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "支持 Flask、Django、FastAPI、Tornado。"
        AppLanguage.ENGLISH -> "Supports Flask, Django, FastAPI, Tornado and plain Python Web projects"
        AppLanguage.ARABIC -> "يدعم Flask و Django و FastAPI و Tornado ومشاريع Python Web الأصلية"
        AppLanguage.PORTUGUESE -> "Suporta Flask, Django, FastAPI, Tornado e projetos Python Web puros"
        AppLanguage.SPANISH -> "Soporta Flask, Django, FastAPI, Tornado y proyectos Python Web puros"
        AppLanguage.FRENCH -> "Prend en charge Flask, Django, FastAPI, Tornado et les projets Python Web purs"
        AppLanguage.GERMAN -> "Unterstützt Flask, Django, FastAPI, Tornado und reine Python-Web-Projekte"
        AppLanguage.RUSSIAN -> "Поддерживает Flask, Django, FastAPI, Tornado и чистые Python Web-проекты"
        AppLanguage.JAPANESE -> "Flask、Django、FastAPI、Tornado、および純粋なPython Webプロジェクトに対応"
        AppLanguage.KOREAN -> "Flask, Django, FastAPI, Tornado 및 순수 Python Web 프로젝트 지원"
    }

    val goSupportedFrameworks: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "支持 Gin、Fiber、Echo、Chi、net/http，需 ARM64 版。"
        AppLanguage.ENGLISH -> "Supports Gin, Fiber, Echo, Chi, net/http. Requires ARM64 binary."
        AppLanguage.ARABIC -> "يدعم Gin و Fiber و Echo و Chi و net/http. يتطلب ملف ثنائي ARM64 مسبق التجميع."
        AppLanguage.PORTUGUESE -> "Suporta Gin, Fiber, Echo, Chi, net/http. Requer binário ARM64."
        AppLanguage.SPANISH -> "Soporta Gin, Fiber, Echo, Chi, net/http. Requiere binario ARM64."
        AppLanguage.FRENCH -> "Prend en charge Gin, Fiber, Echo, Chi, net/http. Nécessite un binaire ARM64."
        AppLanguage.GERMAN -> "Unterstützt Gin, Fiber, Echo, Chi, net/http. Erfordert ARM64-Binary."
        AppLanguage.RUSSIAN -> "Поддерживает Gin, Fiber, Echo, Chi, net/http. Требуется ARM64-бинарник."
        AppLanguage.JAPANESE -> "Gin、Fiber、Echo、Chi、net/http に対応。ARM64バイナリが必要です。"
        AppLanguage.KOREAN -> "Gin, Fiber, Echo, Chi, net/http 지원. ARM64 바이너리 필요."
    }

    val phpFrameworkDetected: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "检测到框架"
        AppLanguage.ENGLISH -> "Framework Detected"
        AppLanguage.ARABIC -> "تم اكتشاف الإطار"
        AppLanguage.PORTUGUESE -> "Framework Detectado"
        AppLanguage.SPANISH -> "Framework Detectado"
        AppLanguage.FRENCH -> "Framework détecté"
        AppLanguage.GERMAN -> "Framework erkannt"
        AppLanguage.RUSSIAN -> "Фреймворк обнаружен"
        AppLanguage.JAPANESE -> "フレームワークを検出"
        AppLanguage.KOREAN -> "프레임워크 감지됨"
    }

    val phpDocumentRoot: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Web 根目录"
        AppLanguage.ENGLISH -> "Document Root"
        AppLanguage.ARABIC -> "جذر المستند"
        AppLanguage.PORTUGUESE -> "Raiz do Documento"
        AppLanguage.SPANISH -> "Raíz del Documento"
        AppLanguage.FRENCH -> "Racine des documents"
        AppLanguage.GERMAN -> "Dokumentwurzel"
        AppLanguage.RUSSIAN -> "Корневой каталог документов"
        AppLanguage.JAPANESE -> "ドキュメントルート"
        AppLanguage.KOREAN -> "문서 루트"
    }

    val phpEntryFile: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "入口文件"
        AppLanguage.ENGLISH -> "Entry File"
        AppLanguage.ARABIC -> "ملف الدخول"
        AppLanguage.PORTUGUESE -> "Arquivo de Entrada"
        AppLanguage.SPANISH -> "Archivo de Entrada"
        AppLanguage.FRENCH -> "Fichier d'entrée"
        AppLanguage.GERMAN -> "Einstiegsdatei"
        AppLanguage.RUSSIAN -> "Точка входа"
        AppLanguage.JAPANESE -> "エントリファイル"
        AppLanguage.KOREAN -> "진입 파일"
    }

    val phpProjectReady: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "PHP 项目已就绪"
        AppLanguage.ENGLISH -> "PHP project ready"
        AppLanguage.ARABIC -> "مشروع PHP جاهز"
        AppLanguage.PORTUGUESE -> "Projeto PHP pronto"
        AppLanguage.SPANISH -> "Proyecto PHP listo"
        AppLanguage.FRENCH -> "Projet PHP prêt"
        AppLanguage.GERMAN -> "PHP-Projekt bereit"
        AppLanguage.RUSSIAN -> "Проект PHP готов"
        AppLanguage.JAPANESE -> "PHPプロジェクトの準備が完了しました"
        AppLanguage.KOREAN -> "PHP 프로젝트 준비 완료"
    }

    val phpSelectProject: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择 PHP 项目目录"
        AppLanguage.ENGLISH -> "Select PHP Project Directory"
        AppLanguage.ARABIC -> "اختر مجلد مشروع PHP"
        AppLanguage.PORTUGUESE -> "Selecionar Diretório do Projeto PHP"
        AppLanguage.SPANISH -> "Seleccionar Directorio del Proyecto PHP"
        AppLanguage.FRENCH -> "Sélectionner le répertoire du projet PHP"
        AppLanguage.GERMAN -> "PHP-Projektverzeichnis auswählen"
        AppLanguage.RUSSIAN -> "Выберите каталог проекта PHP"
        AppLanguage.JAPANESE -> "PHPプロジェクトディレクトリを選択"
        AppLanguage.KOREAN -> "PHP 프로젝트 디렉터리 선택"
    }

    val phpAppCheckingDeps: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "检查 PHP 环境..."
        AppLanguage.ENGLISH -> "Checking PHP environment..."
        AppLanguage.ARABIC -> "جارٍ فحص بيئة PHP..."
        AppLanguage.PORTUGUESE -> "Verificando ambiente PHP..."
        AppLanguage.SPANISH -> "Comprobando entorno PHP..."
        AppLanguage.FRENCH -> "Vérification de l'environnement PHP..."
        AppLanguage.GERMAN -> "PHP-Umgebung wird geprüft..."
        AppLanguage.RUSSIAN -> "Проверка среды PHP..."
        AppLanguage.JAPANESE -> "PHP環境を確認中..."
        AppLanguage.KOREAN -> "PHP 환경 확인 중..."
    }

    val phpAppDownloading: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "正在下载 PHP 运行时"
        AppLanguage.ENGLISH -> "Downloading PHP runtime"
        AppLanguage.ARABIC -> "جارٍ تنزيل بيئة تشغيل PHP"
        AppLanguage.PORTUGUESE -> "Baixando runtime PHP"
        AppLanguage.SPANISH -> "Descargando runtime PHP"
        AppLanguage.FRENCH -> "Téléchargement du runtime PHP"
        AppLanguage.GERMAN -> "PHP-Runtime wird heruntergeladen"
        AppLanguage.RUSSIAN -> "Загрузка среды выполнения PHP"
        AppLanguage.JAPANESE -> "PHPランタイムをダウンロード中"
        AppLanguage.KOREAN -> "PHP 런타임 다운로드 중"
    }

    val phpAppStartingServer: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "正在启动 PHP 服务器..."
        AppLanguage.ENGLISH -> "Starting PHP server..."
        AppLanguage.ARABIC -> "جارٍ تشغيل خادم PHP..."
        AppLanguage.PORTUGUESE -> "Iniciando servidor PHP..."
        AppLanguage.SPANISH -> "Iniciando servidor PHP..."
        AppLanguage.FRENCH -> "Démarrage du serveur PHP..."
        AppLanguage.GERMAN -> "PHP-Server wird gestartet..."
        AppLanguage.RUSSIAN -> "Запуск сервера PHP..."
        AppLanguage.JAPANESE -> "PHPサーバーを起動中..."
        AppLanguage.KOREAN -> "PHP 서버 시작 중..."
    }

    val phpAppServerError: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "PHP 服务器启动失败"
        AppLanguage.ENGLISH -> "PHP server failed to start"
        AppLanguage.ARABIC -> "فشل تشغيل خادم PHP"
        AppLanguage.PORTUGUESE -> "Falha ao iniciar o servidor PHP"
        AppLanguage.SPANISH -> "Error al iniciar el servidor PHP"
        AppLanguage.FRENCH -> "Échec du démarrage du serveur PHP"
        AppLanguage.GERMAN -> "PHP-Serverstart fehlgeschlagen"
        AppLanguage.RUSSIAN -> "Не удалось запустить сервер PHP"
        AppLanguage.JAPANESE -> "PHPサーバーの起動に失敗しました"
        AppLanguage.KOREAN -> "PHP 서버 시작 실패"
    }

    val phpAppDownloadFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "PHP 运行时下载失败，请检查网络后重试"
        AppLanguage.ENGLISH -> "PHP runtime download failed, please check network and retry"
        AppLanguage.ARABIC -> "فشل تنزيل بيئة تشغيل PHP، يرجى التحقق من الشبكة والمحاولة مرة أخرى"
        AppLanguage.PORTUGUESE -> "Falha no download do runtime PHP, verifique a rede e tente novamente"
        AppLanguage.SPANISH -> "Error al descargar el runtime PHP, comprueba la red e inténtalo de nuevo"
        AppLanguage.FRENCH -> "Échec du téléchargement du runtime PHP, vérifiez le réseau et réessayez"
        AppLanguage.GERMAN -> "Download der PHP-Runtime fehlgeschlagen, bitte Netzwerk prüfen und erneut versuchen"
        AppLanguage.RUSSIAN -> "Не удалось загрузить среду выполнения PHP, проверьте сеть и повторите"
        AppLanguage.JAPANESE -> "PHPランタイムのダウンロードに失敗しました。ネットワークを確認して再試行してください"
        AppLanguage.KOREAN -> "PHP 런타임 다운로드 실패, 네트워크를 확인하고 재시도하세요"
    }

    val phpAppProjectNotFound: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "PHP 项目文件不存在"
        AppLanguage.ENGLISH -> "PHP project files not found"
        AppLanguage.ARABIC -> "ملفات مشروع PHP غير موجودة"
        AppLanguage.PORTUGUESE -> "Arquivos do projeto PHP não encontrados"
        AppLanguage.SPANISH -> "Archivos del proyecto PHP no encontrados"
        AppLanguage.FRENCH -> "Fichiers du projet PHP introuvables"
        AppLanguage.GERMAN -> "PHP-Projektdateien nicht gefunden"
        AppLanguage.RUSSIAN -> "Файлы проекта PHP не найдены"
        AppLanguage.JAPANESE -> "PHPプロジェクトファイルが見つかりません"
        AppLanguage.KOREAN -> "PHP 프로젝트 파일을 찾을 수 없습니다"
    }

    val phpImportZip: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "导入 ZIP 压缩包"
        AppLanguage.ENGLISH -> "Import ZIP Archive"
        AppLanguage.ARABIC -> "استيراد أرشيف ZIP"
        AppLanguage.PORTUGUESE -> "Importar Arquivo ZIP"
        AppLanguage.SPANISH -> "Importar Archivo ZIP"
        AppLanguage.FRENCH -> "Importer l'archive ZIP"
        AppLanguage.GERMAN -> "ZIP-Archiv importieren"
        AppLanguage.RUSSIAN -> "Импорт ZIP-архива"
        AppLanguage.JAPANESE -> "ZIPアーカイブをインポート"
        AppLanguage.KOREAN -> "ZIP 아카이브 가져오기"
    }

    val phpExtractingZip: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "正在解压 ZIP 文件..."
        AppLanguage.ENGLISH -> "Extracting ZIP file..."
        AppLanguage.ARABIC -> "جارٍ استخراج ملف ZIP..."
        AppLanguage.PORTUGUESE -> "Extraindo arquivo ZIP..."
        AppLanguage.SPANISH -> "Extrayendo archivo ZIP..."
        AppLanguage.FRENCH -> "Extraction de l'archive ZIP..."
        AppLanguage.GERMAN -> "ZIP-Datei wird entpackt..."
        AppLanguage.RUSSIAN -> "Распаковка ZIP-файла..."
        AppLanguage.JAPANESE -> "ZIPファイルを展開中..."
        AppLanguage.KOREAN -> "ZIP 파일 압축 푸는 중..."
    }

    val phpZipExtractFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "ZIP 解压失败"
        AppLanguage.ENGLISH -> "ZIP extraction failed"
        AppLanguage.ARABIC -> "فشل استخراج ZIP"
        AppLanguage.PORTUGUESE -> "Falha na extração do ZIP"
        AppLanguage.SPANISH -> "Error al extraer ZIP"
        AppLanguage.FRENCH -> "Échec de l'extraction ZIP"
        AppLanguage.GERMAN -> "ZIP-Entpacken fehlgeschlagen"
        AppLanguage.RUSSIAN -> "Не удалось распаковать ZIP"
        AppLanguage.JAPANESE -> "ZIPの展開に失敗しました"
        AppLanguage.KOREAN -> "ZIP 압축 해제 실패"
    }

    val phpZipNoPhpFiles: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "ZIP 中未找到 PHP 项目文件"
        AppLanguage.ENGLISH -> "No PHP project files found in ZIP"
        AppLanguage.ARABIC -> "لم يتم العثور على ملفات مشروع PHP في ZIP"
        AppLanguage.PORTUGUESE -> "Nenhum arquivo de projeto PHP encontrado no ZIP"
        AppLanguage.SPANISH -> "No se encontraron archivos de proyecto PHP en el ZIP"
        AppLanguage.FRENCH -> "Aucun fichier de projet PHP trouvé dans le ZIP"
        AppLanguage.GERMAN -> "Keine PHP-Projektdateien im ZIP gefunden"
        AppLanguage.RUSSIAN -> "В ZIP нет файлов проекта PHP"
        AppLanguage.JAPANESE -> "ZIP内にPHPプロジェクトファイルが見つかりません"
        AppLanguage.KOREAN -> "ZIP에서 PHP 프로젝트 파일을 찾을 수 없습니다"
    }

    val pySelectProject: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择 Python 项目目录"
        AppLanguage.ENGLISH -> "Select Python Project Directory"
        AppLanguage.ARABIC -> "اختر مجلد مشروع Python"
        AppLanguage.PORTUGUESE -> "Selecionar Diretório do Projeto Python"
        AppLanguage.SPANISH -> "Seleccionar Directorio del Proyecto Python"
        AppLanguage.FRENCH -> "Sélectionner le répertoire du projet Python"
        AppLanguage.GERMAN -> "Python-Projektverzeichnis auswählen"
        AppLanguage.RUSSIAN -> "Выберите каталог проекта Python"
        AppLanguage.JAPANESE -> "Pythonプロジェクトディレクトリを選択"
        AppLanguage.KOREAN -> "Python 프로젝트 디렉터리 선택"
    }

    val pyServerType: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "服务器类型"
        AppLanguage.ENGLISH -> "Server Type"
        AppLanguage.ARABIC -> "نوع الخادم"
        AppLanguage.PORTUGUESE -> "Tipo de Servidor"
        AppLanguage.SPANISH -> "Tipo de Servidor"
        AppLanguage.FRENCH -> "Type de serveur"
        AppLanguage.GERMAN -> "Server-Typ"
        AppLanguage.RUSSIAN -> "Тип сервера"
        AppLanguage.JAPANESE -> "サーバータイプ"
        AppLanguage.KOREAN -> "서버 유형"
    }

    val pyRuntimeModeAuto: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自动配置（推荐）"
        AppLanguage.ENGLISH -> "Auto-configured (recommended)"
        AppLanguage.ARABIC -> "إعداد تلقائي (موصى به)"
        AppLanguage.PORTUGUESE -> "Configuração automática (recomendado)"
        AppLanguage.SPANISH -> "Configuración automática (recomendada)"
        AppLanguage.FRENCH -> "Configuration automatique (recommandée)"
        AppLanguage.GERMAN -> "Automatisch konfiguriert (empfohlen)"
        AppLanguage.RUSSIAN -> "Автоматическая настройка (рекомендуется)"
        AppLanguage.JAPANESE -> "自動設定（推奨）"
        AppLanguage.KOREAN -> "자동 구성(권장)"
    }

    val pyRuntimeModeAutoDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已根据检测到的项目框架自动选择最兼容的运行方式，无需手动设置"
        AppLanguage.ENGLISH -> "The most compatible runtime mode is selected automatically from the detected framework — no manual setup needed"
        AppLanguage.ARABIC -> "يتم اختيار وضع التشغيل الأكثر توافقًا تلقائيًا حسب إطار العمل المكتشف — لا حاجة لإعداد يدوي"
        AppLanguage.PORTUGUESE -> "O modo de execução mais compatível é escolhido automaticamente a partir do framework detectado — sem configuração manual"
        AppLanguage.SPANISH -> "El modo de ejecución más compatible se selecciona automáticamente según el framework detectado; no requiere configuración manual"
        AppLanguage.FRENCH -> "Le mode d'exécution le plus compatible est choisi automatiquement selon le framework détecté — aucune configuration manuelle"
        AppLanguage.GERMAN -> "Der kompatibelste Laufzeitmodus wird automatisch anhand des erkannten Frameworks gewählt — keine manuelle Einrichtung nötig"
        AppLanguage.RUSSIAN -> "Наиболее совместимый режим выполнения выбирается автоматически по обнаруженному фреймворку — ручная настройка не требуется"
        AppLanguage.JAPANESE -> "検出されたフレームワークに基づいて最も互換性の高い実行モードが自動的に選択されます。手動設定は不要です"
        AppLanguage.KOREAN -> "감지된 프레임워크에 따라 가장 호환되는 실행 모드가 자동으로 선택됩니다 — 수동 설정이 필요 없습니다"
    }

    val pythonStaticFallbackBanner: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Python 后端未运行：当前为静态预览，接口请求不可用"
        AppLanguage.ENGLISH -> "Python backend is not running — static preview only, API calls will fail"
        AppLanguage.ARABIC -> "خادم Python لا يعمل — معاينة ثابتة فقط، ولن تعمل طلبات API"
        AppLanguage.PORTUGUESE -> "O backend Python não está em execução — apenas visualização estática, chamadas de API falharão"
        AppLanguage.SPANISH -> "El backend de Python no se está ejecutando: solo vista previa estática, las llamadas a la API fallarán"
        AppLanguage.FRENCH -> "Le backend Python ne fonctionne pas — aperçu statique uniquement, les appels d'API échoueront"
        AppLanguage.GERMAN -> "Python-Backend läuft nicht — nur statische Vorschau, API-Aufrufe schlagen fehl"
        AppLanguage.RUSSIAN -> "Бэкенд Python не запущен — только статический предпросмотр, запросы к API не сработают"
        AppLanguage.JAPANESE -> "Pythonバックエンドが実行されていません — 静的プレビューのみで、APIリクエストは失敗します"
        AppLanguage.KOREAN -> "Python 백엔드가 실행 중이 아닙니다 — 정적 미리보기만 제공되며 API 요청은 실패합니다"
    }

    val siteAnalyzeLanTimeoutHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "连接局域网地址超时：请确认与目标设备在同一网络，并在系统设置中允许本应用访问本地网络/局域网"
        AppLanguage.ENGLISH -> "Timed out connecting to a LAN address: make sure you are on the same network as the target device, and allow local network access for this app in system settings"
        AppLanguage.ARABIC -> "انتهت مهلة الاتصال بعنوان شبكة محلية: تأكد من أنك على نفس شبكة الجهاز الهدف، واسمح لهذا التطبيق بالوصول إلى الشبكة المحلية في إعدادات النظام"
        AppLanguage.PORTUGUESE -> "Tempo esgotado ao conectar a um endereço de rede local: verifique se você está na mesma rede do dispositivo de destino e permita o acesso à rede local para este app nas configurações do sistema"
        AppLanguage.SPANISH -> "Se agotó el tiempo de conexión a una dirección de red local: compruebe que está en la misma red que el dispositivo de destino y permita el acceso a la red local para esta app en los ajustes del sistema"
        AppLanguage.FRENCH -> "Délai dépassé lors de la connexion à une adresse réseau locale : vérifiez que vous êtes sur le même réseau que l'appareil cible et autorisez l'accès au réseau local pour cette application dans les paramètres système"
        AppLanguage.GERMAN -> "Zeitüberschreitung bei der Verbindung zu einer lokalen Netzwerkadresse: Stellen Sie sicher, dass Sie sich im selben Netzwerk wie das Zielgerät befinden, und erlauben Sie dieser App in den Systemeinstellungen den Zugriff auf das lokale Netzwerk"
        AppLanguage.RUSSIAN -> "Истекло время ожидания при подключении к локальному сетевому адресу: убедитесь, что вы в одной сети с целевым устройством, и разрешите этому приложению доступ к локальной сети в настройках системы"
        AppLanguage.JAPANESE -> "ローカルネットワークアドレスへの接続がタイムアウトしました：対象デバイスと同じネットワークにいることを確認し、システム設定でこのアプリのローカルネットワークへのアクセスを許可してください"
        AppLanguage.KOREAN -> "로컬 네트워크 주소 연결 시간이 초과되었습니다: 대상 기기와 같은 네트워크에 있는지 확인하고, 시스템 설정에서 이 앱의 로컬 네트워크 접근을 허용해 주세요"
    }

    val pyProjectReady: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Python 项目已就绪"
        AppLanguage.ENGLISH -> "Python project ready"
        AppLanguage.ARABIC -> "مشروع Python جاهز"
        AppLanguage.PORTUGUESE -> "Projeto Python pronto"
        AppLanguage.SPANISH -> "Proyecto Python listo"
        AppLanguage.FRENCH -> "Projet Python prêt"
        AppLanguage.GERMAN -> "Python-Projekt bereit"
        AppLanguage.RUSSIAN -> "Проект Python готов"
        AppLanguage.JAPANESE -> "Pythonプロジェクトの準備が完了しました"
        AppLanguage.KOREAN -> "Python 프로젝트 준비 완료"
    }

    val pyProjectNotFound: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Python 项目文件不存在"
        AppLanguage.ENGLISH -> "Python project files not found"
        AppLanguage.ARABIC -> "ملفات مشروع Python غير موجودة"
        AppLanguage.PORTUGUESE -> "Arquivos do projeto Python não encontrados"
        AppLanguage.SPANISH -> "Archivos del proyecto Python no encontrados"
        AppLanguage.FRENCH -> "Fichiers du projet Python introuvables"
        AppLanguage.GERMAN -> "Python-Projektdateien nicht gefunden"
        AppLanguage.RUSSIAN -> "Файлы проекта Python не найдены"
        AppLanguage.JAPANESE -> "Pythonプロジェクトファイルが見つかりません"
        AppLanguage.KOREAN -> "Python 프로젝트 파일을 찾을 수 없습니다"
    }

    val pyStartingPreview: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "正在启动 Python 应用预览..."
        AppLanguage.ENGLISH -> "Starting Python app preview..."
        AppLanguage.ARABIC -> "جارٍ تشغيل معاينة تطبيق Python..."
        AppLanguage.PORTUGUESE -> "Iniciando pré-visualização do app Python..."
        AppLanguage.SPANISH -> "Iniciando vista previa de la app Python..."
        AppLanguage.FRENCH -> "Démarrage de l'aperçu de l'app Python..."
        AppLanguage.GERMAN -> "Python-App-Vorschau wird gestartet..."
        AppLanguage.RUSSIAN -> "Запуск предпросмотра приложения Python..."
        AppLanguage.JAPANESE -> "Pythonアプリのプレビューを起動中..."
        AppLanguage.KOREAN -> "Python 앱 미리보기 시작 중..."
    }

    val pyPreviewFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Python 应用预览启动失败"
        AppLanguage.ENGLISH -> "Python app preview failed to start"
        AppLanguage.ARABIC -> "فشل تشغيل معاينة تطبيق Python"
        AppLanguage.PORTUGUESE -> "Falha ao iniciar a pré-visualização do app Python"
        AppLanguage.SPANISH -> "Error al iniciar la vista previa de la app Python"
        AppLanguage.FRENCH -> "Échec du démarrage de l'aperçu de l'app Python"
        AppLanguage.GERMAN -> "Start der Python-App-Vorschau fehlgeschlagen"
        AppLanguage.RUSSIAN -> "Не удалось запустить предпросмотр приложения Python"
        AppLanguage.JAPANESE -> "Pythonアプリのプレビュー起動に失敗しました"
        AppLanguage.KOREAN -> "Python 앱 미리보기 시작 실패"
    }

    val goSelectBinary: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择 Go 二进制文件"
        AppLanguage.ENGLISH -> "Select Go Binary"
        AppLanguage.ARABIC -> "اختر ملف Go الثنائي"
        AppLanguage.PORTUGUESE -> "Selecionar Binário Go"
        AppLanguage.SPANISH -> "Seleccionar Binario Go"
        AppLanguage.FRENCH -> "Sélectionner le binaire Go"
        AppLanguage.GERMAN -> "Go-Binary auswählen"
        AppLanguage.RUSSIAN -> "Выберите бинарник Go"
        AppLanguage.JAPANESE -> "Goバイナリを選択"
        AppLanguage.KOREAN -> "Go 바이너리 선택"
    }

    val goSelectProject: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择 Go 项目目录"
        AppLanguage.ENGLISH -> "Select Go Project Directory"
        AppLanguage.ARABIC -> "اختر مجلد مشروع Go"
        AppLanguage.PORTUGUESE -> "Selecionar Diretório do Projeto Go"
        AppLanguage.SPANISH -> "Seleccionar Directorio del Proyecto Go"
        AppLanguage.FRENCH -> "Sélectionner le répertoire du projet Go"
        AppLanguage.GERMAN -> "Go-Projektverzeichnis auswählen"
        AppLanguage.RUSSIAN -> "Выберите каталог проекта Go"
        AppLanguage.JAPANESE -> "Goプロジェクトディレクトリを選択"
        AppLanguage.KOREAN -> "Go 프로젝트 디렉터리 선택"
    }

    val goProjectReady: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Go 服务已就绪"
        AppLanguage.ENGLISH -> "Go service ready"
        AppLanguage.ARABIC -> "خدمة Go جاهزة"
        AppLanguage.PORTUGUESE -> "Serviço Go pronto"
        AppLanguage.SPANISH -> "Servicio Go listo"
        AppLanguage.FRENCH -> "Service Go prêt"
        AppLanguage.GERMAN -> "Go-Dienst bereit"
        AppLanguage.RUSSIAN -> "Служба Go готова"
        AppLanguage.JAPANESE -> "Goサービスの準備が完了しました"
        AppLanguage.KOREAN -> "Go 서비스 준비 완료"
    }

    val goProjectNotFound: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Go 项目文件不存在"
        AppLanguage.ENGLISH -> "Go project files not found"
        AppLanguage.ARABIC -> "ملفات مشروع Go غير موجودة"
        AppLanguage.PORTUGUESE -> "Arquivos do projeto Go não encontrados"
        AppLanguage.SPANISH -> "Archivos del proyecto Go no encontrados"
        AppLanguage.FRENCH -> "Fichiers du projet Go introuvables"
        AppLanguage.GERMAN -> "Go-Projektdateien nicht gefunden"
        AppLanguage.RUSSIAN -> "Файлы проекта Go не найдены"
        AppLanguage.JAPANESE -> "Goプロジェクトファイルが見つかりません"
        AppLanguage.KOREAN -> "Go 프로젝트 파일을 찾을 수 없습니다"
    }

    val goStartingPreview: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "正在启动 Go 应用预览..."
        AppLanguage.ENGLISH -> "Starting Go app preview..."
        AppLanguage.ARABIC -> "جارٍ تشغيل معاينة تطبيق Go..."
        AppLanguage.PORTUGUESE -> "Iniciando pré-visualização do app Go..."
        AppLanguage.SPANISH -> "Iniciando vista previa de la app Go..."
        AppLanguage.FRENCH -> "Démarrage de l'aperçu de l'app Go..."
        AppLanguage.GERMAN -> "Go-App-Vorschau wird gestartet..."
        AppLanguage.RUSSIAN -> "Запуск предпросмотра приложения Go..."
        AppLanguage.JAPANESE -> "Goアプリのプレビューを起動中..."
        AppLanguage.KOREAN -> "Go 앱 미리보기 시작 중..."
    }

    val goPreviewFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Go 应用预览启动失败"
        AppLanguage.ENGLISH -> "Go app preview failed to start"
        AppLanguage.ARABIC -> "فشل تشغيل معاينة تطبيق Go"
        AppLanguage.PORTUGUESE -> "Falha ao iniciar a pré-visualização do app Go"
        AppLanguage.SPANISH -> "Error al iniciar la vista previa de la app Go"
        AppLanguage.FRENCH -> "Échec du démarrage de l'aperçu de l'app Go"
        AppLanguage.GERMAN -> "Start der Go-App-Vorschau fehlgeschlagen"
        AppLanguage.RUSSIAN -> "Не удалось запустить предпросмотр приложения Go"
        AppLanguage.JAPANESE -> "Goアプリのプレビュー起動に失敗しました"
        AppLanguage.KOREAN -> "Go 앱 미리보기 시작 실패"
    }

    val nodeProjectNotFound: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Node.js 项目文件不存在"
        AppLanguage.ENGLISH -> "Node.js project files not found"
        AppLanguage.ARABIC -> "ملفات مشروع Node.js غير موجودة"
        AppLanguage.PORTUGUESE -> "Arquivos do projeto Node.js não encontrados"
        AppLanguage.SPANISH -> "Archivos del proyecto Node.js no encontrados"
        AppLanguage.FRENCH -> "Fichiers du projet Node.js introuvables"
        AppLanguage.GERMAN -> "Node.js-Projektdateien nicht gefunden"
        AppLanguage.RUSSIAN -> "Файлы проекта Node.js не найдены"
        AppLanguage.JAPANESE -> "Node.jsプロジェクトファイルが見つかりません"
        AppLanguage.KOREAN -> "Node.js 프로젝트 파일을 찾을 수 없습니다"
    }

    val nodePreviewFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Node.js 应用预览启动失败"
        AppLanguage.ENGLISH -> "Node.js app preview failed to start"
        AppLanguage.ARABIC -> "فشل تشغيل معاينة تطبيق Node.js"
        AppLanguage.PORTUGUESE -> "Falha ao iniciar a pré-visualização do app Node.js"
        AppLanguage.SPANISH -> "Error al iniciar la vista previa de la app Node.js"
        AppLanguage.FRENCH -> "Échec du démarrage de l'aperçu de l'app Node.js"
        AppLanguage.GERMAN -> "Start der Node.js-App-Vorschau fehlgeschlagen"
        AppLanguage.RUSSIAN -> "Не удалось запустить предпросмотр приложения Node.js"
        AppLanguage.JAPANESE -> "Node.jsアプリのプレビュー起動に失敗しました"
        AppLanguage.KOREAN -> "Node.js 앱 미리보기 시작 실패"
    }

    val previewNotRunningBadge: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "预览（未运行）"
        AppLanguage.ENGLISH -> "Preview (not running)"
        AppLanguage.ARABIC -> "معاينة (غير قيد التشغيل)"
        AppLanguage.PORTUGUESE -> "Pré-visualização (não executando)"
        AppLanguage.SPANISH -> "Vista previa (no ejecutándose)"
        AppLanguage.FRENCH -> "Aperçu (non démarré)"
        AppLanguage.GERMAN -> "Vorschau (wird nicht ausgeführt)"
        AppLanguage.RUSSIAN -> "Предпросмотр (не запущен)"
        AppLanguage.JAPANESE -> "プレビュー（未実行）"
        AppLanguage.KOREAN -> "미리보기 (실행 중 아님)"
    }

    val previewNotRunningNote: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "这是未运行状态的预览，点击进入后会真正启动服务器并加载实际页面。"
        AppLanguage.ENGLISH -> "This is a non-running preview. Opening the app starts the server and loads the real page."
        AppLanguage.ARABIC -> "هذه معاينة في حالة عدم التشغيل. عند فتح التطبيق سيبدأ الخادم ويُحمّل الصفحة الفعلية."
        AppLanguage.PORTUGUESE -> "Esta é uma pré-visualização sem execução. Abrir o app inicia o servidor e carrega a página real."
        AppLanguage.SPANISH -> "Esta es una vista previa sin ejecución. Abrir la app inicia el servidor y carga la página real."
        AppLanguage.FRENCH -> "Ceci est un aperçu non démarré. Ouvrir l'app démarre le serveur et charge la page réelle."
        AppLanguage.GERMAN -> "Dies ist eine Vorschau ohne Ausführung. Beim Öffnen der App wird der Server gestartet und die echte Seite geladen."
        AppLanguage.RUSSIAN -> "Это предпросмотр без запуска. При открытии приложения запускается сервер и загружается реальная страница."
        AppLanguage.JAPANESE -> "これは非実行状態のプレビューです。アプリを開くとサーバーが起動し、実際のページが読み込まれます。"
        AppLanguage.KOREAN -> "실행 중이 아닌 미리보기입니다. 앱을 열면 서버가 시작되고 실제 페이지가 로드됩니다."
    }

    val previewLabelTheme: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "主题"
        AppLanguage.ENGLISH -> "Theme"
        AppLanguage.ARABIC -> "السمة"
        AppLanguage.PORTUGUESE -> "Tema"
        AppLanguage.SPANISH -> "Tema"
        AppLanguage.FRENCH -> "Thème"
        AppLanguage.GERMAN -> "Theme"
        AppLanguage.RUSSIAN -> "Тема"
        AppLanguage.JAPANESE -> "テーマ"
        AppLanguage.KOREAN -> "테마"
    }

    val previewLabelPlugins: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "插件"
        AppLanguage.ENGLISH -> "Plugins"
        AppLanguage.ARABIC -> "الإضافات"
        AppLanguage.PORTUGUESE -> "Plugins"
        AppLanguage.SPANISH -> "Plugins"
        AppLanguage.FRENCH -> "Plugins"
        AppLanguage.GERMAN -> "Plugins"
        AppLanguage.RUSSIAN -> "Плагины"
        AppLanguage.JAPANESE -> "プラグイン"
        AppLanguage.KOREAN -> "플러그인"
    }

    val previewLabelAdmin: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "管理员"
        AppLanguage.ENGLISH -> "Admin"
        AppLanguage.ARABIC -> "المسؤول"
        AppLanguage.PORTUGUESE -> "Administrador"
        AppLanguage.SPANISH -> "Administrador"
        AppLanguage.FRENCH -> "Admin"
        AppLanguage.GERMAN -> "Admin"
        AppLanguage.RUSSIAN -> "Админ"
        AppLanguage.JAPANESE -> "管理者"
        AppLanguage.KOREAN -> "관리자"
    }

    val previewLabelFramework: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "框架"
        AppLanguage.ENGLISH -> "Framework"
        AppLanguage.ARABIC -> "الإطار"
        AppLanguage.PORTUGUESE -> "Framework"
        AppLanguage.SPANISH -> "Framework"
        AppLanguage.FRENCH -> "Framework"
        AppLanguage.GERMAN -> "Framework"
        AppLanguage.RUSSIAN -> "Фреймворк"
        AppLanguage.JAPANESE -> "フレームワーク"
        AppLanguage.KOREAN -> "프레임워크"
    }

    val previewLabelEntry: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "入口文件"
        AppLanguage.ENGLISH -> "Entry"
        AppLanguage.ARABIC -> "ملف الدخول"
        AppLanguage.PORTUGUESE -> "Arquivo de Entrada"
        AppLanguage.SPANISH -> "Archivo de Entrada"
        AppLanguage.FRENCH -> "Fichier d'Entrée"
        AppLanguage.GERMAN -> "Einstiegspunkt"
        AppLanguage.RUSSIAN -> "Точка входа"
        AppLanguage.JAPANESE -> "エントリファイル"
        AppLanguage.KOREAN -> "진입 파일"
    }

    val previewLabelDocumentRoot: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Web 根目录"
        AppLanguage.ENGLISH -> "Document Root"
        AppLanguage.ARABIC -> "جذر المستند"
        AppLanguage.PORTUGUESE -> "Raiz do Documento"
        AppLanguage.SPANISH -> "Raíz del Documento"
        AppLanguage.FRENCH -> "Racine du Document"
        AppLanguage.GERMAN -> "Document Root"
        AppLanguage.RUSSIAN -> "Корневой каталог"
        AppLanguage.JAPANESE -> "ドキュメントルート"
        AppLanguage.KOREAN -> "문서 루트"
    }

    val previewLabelBinary: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "二进制文件"
        AppLanguage.ENGLISH -> "Binary"
        AppLanguage.ARABIC -> "الملف الثنائي"
        AppLanguage.PORTUGUESE -> "Binário"
        AppLanguage.SPANISH -> "Binario"
        AppLanguage.FRENCH -> "Binaire"
        AppLanguage.GERMAN -> "Binärdatei"
        AppLanguage.RUSSIAN -> "Бинарный файл"
        AppLanguage.JAPANESE -> "バイナリファイル"
        AppLanguage.KOREAN -> "바이너리 파일"
    }

    val previewValueDefault: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "默认"
        AppLanguage.ENGLISH -> "Default"
        AppLanguage.ARABIC -> "افتراضي"
        AppLanguage.PORTUGUESE -> "Padrão"
        AppLanguage.SPANISH -> "Predeterminado"
        AppLanguage.FRENCH -> "Par défaut"
        AppLanguage.GERMAN -> "Standard"
        AppLanguage.RUSSIAN -> "По умолчанию"
        AppLanguage.JAPANESE -> "デフォルト"
        AppLanguage.KOREAN -> "기본값"
    }

    val previewValueUnknown: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "未知"
        AppLanguage.ENGLISH -> "Unknown"
        AppLanguage.ARABIC -> "غير معروف"
        AppLanguage.PORTUGUESE -> "Desconhecido"
        AppLanguage.SPANISH -> "Desconocido"
        AppLanguage.FRENCH -> "Inconnu"
        AppLanguage.GERMAN -> "Unbekannt"
        AppLanguage.RUSSIAN -> "Неизвестно"
        AppLanguage.JAPANESE -> "不明"
        AppLanguage.KOREAN -> "알 수 없음"
    }

    val wpDownloadDeps: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下载 WordPress 依赖"
        AppLanguage.ENGLISH -> "Download WordPress Dependencies"
        AppLanguage.ARABIC -> "تنزيل متطلبات WordPress"
        AppLanguage.PORTUGUESE -> "Baixar Dependências do WordPress"
        AppLanguage.SPANISH -> "Descargar Dependencias de WordPress"
        AppLanguage.FRENCH -> "Télécharger les dépendances WordPress"
        AppLanguage.GERMAN -> "WordPress-Abhängigkeiten herunterladen"
        AppLanguage.RUSSIAN -> "Загрузить зависимости WordPress"
        AppLanguage.JAPANESE -> "WordPressの依存関係をダウンロード"
        AppLanguage.KOREAN -> "WordPress 종속성 다운로드"
    }

    val wpDownloadDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "首次使用需要下载 PHP 解释器和 WordPress 核心文件（级 25MB）"
        AppLanguage.ENGLISH -> "First use needs PHP and WordPress core (~25MB)."
        AppLanguage.ARABIC -> "يتطلب الاستخدام الأول تنزيل مترجم PHP ونواة WordPress (~25MB)"
        AppLanguage.PORTUGUESE -> "O primeiro uso precisa do PHP e do núcleo do WordPress (~25MB)."
        AppLanguage.SPANISH -> "El primer uso requiere PHP y el núcleo de WordPress (~25MB)."
        AppLanguage.FRENCH -> "La première utilisation nécessite PHP et le cœur de WordPress (~25MB)."
        AppLanguage.GERMAN -> "Erstverwendung benötigt PHP und den WordPress-Core (~25MB)."
        AppLanguage.RUSSIAN -> "При первом использовании нужен PHP и ядро WordPress (~25МБ)."
        AppLanguage.JAPANESE -> "初回利用時にはPHPとWordPressコアが必要です（約25MB）。"
        AppLanguage.KOREAN -> "첫 사용 시 PHP와 WordPress 코어가 필요합니다 (~25MB)."
    }

    val wpMirrorCN: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "国内镜像"
        AppLanguage.ENGLISH -> "China Mirror"
        AppLanguage.ARABIC -> "مرآة الصين"
        AppLanguage.PORTUGUESE -> "Mirror da China"
        AppLanguage.SPANISH -> "Mirror de China"
        AppLanguage.FRENCH -> "Miroir de Chine"
        AppLanguage.GERMAN -> "China-Mirror"
        AppLanguage.RUSSIAN -> "Китайское зеркало"
        AppLanguage.JAPANESE -> "中国ミラー"
        AppLanguage.KOREAN -> "중국 미러"
    }

    val wpMirrorGlobal: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "国际源"
        AppLanguage.ENGLISH -> "Global Source"
        AppLanguage.ARABIC -> "مصدر عالمي"
        AppLanguage.PORTUGUESE -> "Fonte Global"
        AppLanguage.SPANISH -> "Fuente Global"
        AppLanguage.FRENCH -> "Source globale"
        AppLanguage.GERMAN -> "Globale Quelle"
        AppLanguage.RUSSIAN -> "Глобальный источник"
        AppLanguage.JAPANESE -> "グローバルソース"
        AppLanguage.KOREAN -> "글로벌 소스"
    }

    val wpDownloading: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "正在下载"
        AppLanguage.ENGLISH -> "Downloading"
        AppLanguage.ARABIC -> "جارٍ التنزيل"
        AppLanguage.PORTUGUESE -> "Baixando"
        AppLanguage.SPANISH -> "Descargando"
        AppLanguage.FRENCH -> "Téléchargement"
        AppLanguage.GERMAN -> "Wird heruntergeladen"
        AppLanguage.RUSSIAN -> "Загрузка"
        AppLanguage.JAPANESE -> "ダウンロード中"
        AppLanguage.KOREAN -> "다운로드 중"
    }

    val wpExtracting: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "正在解压"
        AppLanguage.ENGLISH -> "Extracting"
        AppLanguage.ARABIC -> "جارٍ الاستخراج"
        AppLanguage.PORTUGUESE -> "Extraindo"
        AppLanguage.SPANISH -> "Extrayendo"
        AppLanguage.FRENCH -> "Extraction"
        AppLanguage.GERMAN -> "Wird entpackt"
        AppLanguage.RUSSIAN -> "Распаковка"
        AppLanguage.JAPANESE -> "展開中"
        AppLanguage.KOREAN -> "압축 푸는 중"
    }

    val wpDepsReady: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "依赖已就绪"
        AppLanguage.ENGLISH -> "Dependencies Ready"
        AppLanguage.ARABIC -> "المتطلبات جاهزة"
        AppLanguage.PORTUGUESE -> "Dependências Prontas"
        AppLanguage.SPANISH -> "Dependencias Listas"
        AppLanguage.FRENCH -> "Dépendances prêtes"
        AppLanguage.GERMAN -> "Abhängigkeiten bereit"
        AppLanguage.RUSSIAN -> "Зависимости готовы"
        AppLanguage.JAPANESE -> "依存関係の準備が完了しました"
        AppLanguage.KOREAN -> "종속성 준비 완료"
    }

    val wpSiteTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "站点标题"
        AppLanguage.ENGLISH -> "Site Title"
        AppLanguage.ARABIC -> "عنوان الموقع"
        AppLanguage.PORTUGUESE -> "Título do Site"
        AppLanguage.SPANISH -> "Título del Sitio"
        AppLanguage.FRENCH -> "Titre du site"
        AppLanguage.GERMAN -> "Website-Titel"
        AppLanguage.RUSSIAN -> "Заголовок сайта"
        AppLanguage.JAPANESE -> "サイトタイトル"
        AppLanguage.KOREAN -> "사이트 제목"
    }

    val wpAdminUser: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "管理员用户名"
        AppLanguage.ENGLISH -> "Admin Username"
        AppLanguage.ARABIC -> "اسم المستخدم المسؤول"
        AppLanguage.PORTUGUESE -> "Nome de Usuário Admin"
        AppLanguage.SPANISH -> "Nombre de Usuario Admin"
        AppLanguage.FRENCH -> "Nom d'utilisateur admin"
        AppLanguage.GERMAN -> "Admin-Benutzername"
        AppLanguage.RUSSIAN -> "Имя администратора"
        AppLanguage.JAPANESE -> "管理者ユーザー名"
        AppLanguage.KOREAN -> "관리자 사용자 이름"
    }

    val wpStartingServer: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "正在准备应用..."
        AppLanguage.ENGLISH -> "Preparing your app..."
        AppLanguage.ARABIC -> "جارٍ تجهيز التطبيق..."
        AppLanguage.PORTUGUESE -> "Preparando seu aplicativo..."
        AppLanguage.SPANISH -> "Preparando tu aplicación..."
        AppLanguage.FRENCH -> "Préparation de votre application..."
        AppLanguage.GERMAN -> "Ihre App wird vorbereitet..."
        AppLanguage.RUSSIAN -> "Подготовка приложения..."
        AppLanguage.JAPANESE -> "アプリを準備中..."
        AppLanguage.KOREAN -> "앱을 준비하는 중..."
    }

    val wpServerError: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "PHP 服务器启动失败"
        AppLanguage.ENGLISH -> "PHP server failed to start"
        AppLanguage.ARABIC -> "فشل تشغيل خادم PHP"
        AppLanguage.PORTUGUESE -> "Falha ao iniciar o servidor PHP"
        AppLanguage.SPANISH -> "Error al iniciar el servidor PHP"
        AppLanguage.FRENCH -> "Échec du démarrage du serveur PHP"
        AppLanguage.GERMAN -> "PHP-Serverstart fehlgeschlagen"
        AppLanguage.RUSSIAN -> "Не удалось запустить сервер PHP"
        AppLanguage.JAPANESE -> "PHPサーバーの起動に失敗しました"
        AppLanguage.KOREAN -> "PHP 서버 시작 실패"
    }

    val wpClearCache: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "清理 WordPress 缓存"
        AppLanguage.ENGLISH -> "Clear WordPress Cache"
        AppLanguage.ARABIC -> "مسح ذاكرة التخزين المؤقت WordPress"
        AppLanguage.PORTUGUESE -> "Limpar Cache do WordPress"
        AppLanguage.SPANISH -> "Borrar Caché de WordPress"
        AppLanguage.FRENCH -> "Vider le cache WordPress"
        AppLanguage.GERMAN -> "WordPress-Cache leeren"
        AppLanguage.RUSSIAN -> "Очистить кэш WordPress"
        AppLanguage.JAPANESE -> "WordPressキャッシュをクリア"
        AppLanguage.KOREAN -> "WordPress 캐시 지우기"
    }

    val wpMirrorSource: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "镜像源"
        AppLanguage.ENGLISH -> "Mirror Source"
        AppLanguage.ARABIC -> "مصدر المرآة"
        AppLanguage.PORTUGUESE -> "Fonte do Mirror"
        AppLanguage.SPANISH -> "Fuente del Mirror"
        AppLanguage.FRENCH -> "Source du miroir"
        AppLanguage.GERMAN -> "Mirror-Quelle"
        AppLanguage.RUSSIAN -> "Источник зеркала"
        AppLanguage.JAPANESE -> "ミラーソース"
        AppLanguage.KOREAN -> "미러 소스"
    }

    val wpAutoDetect: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自动识别"
        AppLanguage.ENGLISH -> "Auto Detect"
        AppLanguage.ARABIC -> "كشف تلقائي"
        AppLanguage.PORTUGUESE -> "Detecção Automática"
        AppLanguage.SPANISH -> "Detección Automática"
        AppLanguage.FRENCH -> "Détection automatique"
        AppLanguage.GERMAN -> "Automatische Erkennung"
        AppLanguage.RUSSIAN -> "Автоопределение"
        AppLanguage.JAPANESE -> "自動検出"
        AppLanguage.KOREAN -> "자동 감지"
    }

    val wpCheckingDeps: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "检查依赖..."
        AppLanguage.ENGLISH -> "Checking dependencies..."
        AppLanguage.ARABIC -> "جارٍ فحص المتطلبات..."
        AppLanguage.PORTUGUESE -> "Verificando dependências..."
        AppLanguage.SPANISH -> "Comprobando dependencias..."
        AppLanguage.FRENCH -> "Vérification des dépendances..."
        AppLanguage.GERMAN -> "Abhängigkeiten werden geprüft..."
        AppLanguage.RUSSIAN -> "Проверка зависимостей..."
        AppLanguage.JAPANESE -> "依存関係を確認中..."
        AppLanguage.KOREAN -> "종속성 확인 중..."
    }

    val wpCreatingProject: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "创建 WordPress 项目..."
        AppLanguage.ENGLISH -> "Creating WordPress project..."
        AppLanguage.ARABIC -> "جارٍ إنشاء مشروع WordPress..."
        AppLanguage.PORTUGUESE -> "Criando projeto WordPress..."
        AppLanguage.SPANISH -> "Creando proyecto WordPress..."
        AppLanguage.FRENCH -> "Création du projet WordPress..."
        AppLanguage.GERMAN -> "WordPress-Projekt wird erstellt..."
        AppLanguage.RUSSIAN -> "Создание проекта WordPress..."
        AppLanguage.JAPANESE -> "WordPressプロジェクトを作成中..."
        AppLanguage.KOREAN -> "WordPress 프로젝트 생성 중..."
    }

    val wpDownloadFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "依赖下载失败，请检查网络后重试"
        AppLanguage.ENGLISH -> "Dependency download failed, please check network and retry"
        AppLanguage.ARABIC -> "فشل تنزيل المتطلبات، يرجى التحقق من الشبكة والمحاولة مرة أخرى"
        AppLanguage.PORTUGUESE -> "Falha no download de dependências, verifique a rede e tente novamente"
        AppLanguage.SPANISH -> "Error al descargar dependencias, comprueba la red e inténtalo de nuevo"
        AppLanguage.FRENCH -> "Échec du téléchargement des dépendances, vérifiez le réseau et réessayez"
        AppLanguage.GERMAN -> "Download der Abhängigkeiten fehlgeschlagen, bitte Netzwerk prüfen und erneut versuchen"
        AppLanguage.RUSSIAN -> "Не удалось загрузить зависимости, проверьте сеть и повторите"
        AppLanguage.JAPANESE -> "依存関係のダウンロードに失敗しました。ネットワークを確認して再試行してください"
        AppLanguage.KOREAN -> "종속성 다운로드 실패, 네트워크를 확인하고 재시도하세요"
    }

    val wpProjectCreateFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "WordPress 项目创建失败"
        AppLanguage.ENGLISH -> "WordPress project creation failed"
        AppLanguage.ARABIC -> "فشل إنشاء مشروع WordPress"
        AppLanguage.PORTUGUESE -> "Falha na criação do projeto WordPress"
        AppLanguage.SPANISH -> "Error al crear el proyecto WordPress"
        AppLanguage.FRENCH -> "Échec de la création du projet WordPress"
        AppLanguage.GERMAN -> "Erstellung des WordPress-Projekts fehlgeschlagen"
        AppLanguage.RUSSIAN -> "Не удалось создать проект WordPress"
        AppLanguage.JAPANESE -> "WordPressプロジェクトの作成に失敗しました"
        AppLanguage.KOREAN -> "WordPress 프로젝트 생성 실패"
    }

    val wpCreateTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "创建 WordPress 应用"
        AppLanguage.ENGLISH -> "Create WordPress App"
        AppLanguage.ARABIC -> "إنشاء تطبيق WordPress"
        AppLanguage.PORTUGUESE -> "Criar App WordPress"
        AppLanguage.SPANISH -> "Crear App WordPress"
        AppLanguage.FRENCH -> "Créer une app WordPress"
        AppLanguage.GERMAN -> "WordPress-App erstellen"
        AppLanguage.RUSSIAN -> "Создать приложение WordPress"
        AppLanguage.JAPANESE -> "WordPressアプリを作成"
        AppLanguage.KOREAN -> "WordPress 앱 만들기"
    }

    val wpSiteTitleHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "输入站点标题"
        AppLanguage.ENGLISH -> "Enter site title"
        AppLanguage.ARABIC -> "أدخل عنوان الموقع"
        AppLanguage.PORTUGUESE -> "Digite o título do site"
        AppLanguage.SPANISH -> "Introduce el título del sitio"
        AppLanguage.FRENCH -> "Saisir le titre du site"
        AppLanguage.GERMAN -> "Website-Titel eingeben"
        AppLanguage.RUSSIAN -> "Введите заголовок сайта"
        AppLanguage.JAPANESE -> "サイトタイトルを入力"
        AppLanguage.KOREAN -> "사이트 제목 입력"
    }

    val wpDefaultSiteTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "我的站点"
        AppLanguage.ENGLISH -> "My Site"
        AppLanguage.ARABIC -> "موقعي"
        AppLanguage.PORTUGUESE -> "Meu Site"
        AppLanguage.SPANISH -> "Mi Sitio"
        AppLanguage.FRENCH -> "Mon site"
        AppLanguage.GERMAN -> "Meine Website"
        AppLanguage.RUSSIAN -> "Мой сайт"
        AppLanguage.JAPANESE -> "マイサイト"
        AppLanguage.KOREAN -> "내 사이트"
    }

    val wpDefaultSiteLanguageCode: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "zh_CN"
        AppLanguage.ENGLISH -> "en_US"
        AppLanguage.ARABIC -> "ar"
        AppLanguage.PORTUGUESE -> "pt_BR"
        AppLanguage.SPANISH -> "es_ES"
        AppLanguage.FRENCH -> "fr_FR"
        AppLanguage.GERMAN -> "de_DE"
        AppLanguage.RUSSIAN -> "ru_RU"
        AppLanguage.JAPANESE -> "ja_JP"
        AppLanguage.KOREAN -> "ko_KR"
    }

    val wpAdminUserHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "管理员用户名（默认 admin）"
        AppLanguage.ENGLISH -> "Admin username (default: admin)"
        AppLanguage.ARABIC -> "اسم المستخدم المسؤول (افتراضي: admin)"
        AppLanguage.PORTUGUESE -> "Nome de usuário admin (padrão: admin)"
        AppLanguage.SPANISH -> "Nombre de usuario admin (predeterminado: admin)"
        AppLanguage.FRENCH -> "Nom d'utilisateur admin (par défaut : admin)"
        AppLanguage.GERMAN -> "Admin-Benutzername (Standard: admin)"
        AppLanguage.RUSSIAN -> "Имя администратора (по умолчанию: admin)"
        AppLanguage.JAPANESE -> "管理者ユーザー名（デフォルト: admin）"
        AppLanguage.KOREAN -> "관리자 사용자 이름 (기본값: admin)"
    }

    val wpBasicConfig: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "基本配置"
        AppLanguage.ENGLISH -> "Basic Configuration"
        AppLanguage.ARABIC -> "الإعدادات الأساسية"
        AppLanguage.PORTUGUESE -> "Configuração Básica"
        AppLanguage.SPANISH -> "Configuración Básica"
        AppLanguage.FRENCH -> "Configuration de base"
        AppLanguage.GERMAN -> "Grundkonfiguration"
        AppLanguage.RUSSIAN -> "Базовая конфигурация"
        AppLanguage.JAPANESE -> "基本設定"
        AppLanguage.KOREAN -> "기본 구성"
    }

    val wpImportProject: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "导入 WordPress 项目"
        AppLanguage.ENGLISH -> "Import WordPress Project"
        AppLanguage.ARABIC -> "استيراد مشروع WordPress"
        AppLanguage.PORTUGUESE -> "Importar Projeto WordPress"
        AppLanguage.SPANISH -> "Importar Proyecto WordPress"
        AppLanguage.FRENCH -> "Importer le projet WordPress"
        AppLanguage.GERMAN -> "WordPress-Projekt importieren"
        AppLanguage.RUSSIAN -> "Импорт проекта WordPress"
        AppLanguage.JAPANESE -> "WordPressプロジェクトをインポート"
        AppLanguage.KOREAN -> "WordPress 프로젝트 가져오기"
    }

    val wpImportProjectDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择完整的 WordPress 压缩包（.zip），包含主题和插件"
        AppLanguage.ENGLISH -> "Select a complete WordPress archive (.zip) with themes and plugins"
        AppLanguage.ARABIC -> "حدد أرشيف WordPress كامل (.zip) مع السمات والإضافات"
        AppLanguage.PORTUGUESE -> "Selecione um arquivo WordPress completo (.zip) com temas e plugins"
        AppLanguage.SPANISH -> "Selecciona un archivo WordPress completo (.zip) con temas y plugins"
        AppLanguage.FRENCH -> "Sélectionnez une archive WordPress complète (.zip) avec thèmes et plugins"
        AppLanguage.GERMAN -> "Vollständiges WordPress-Archiv (.zip) mit Themes und Plugins auswählen"
        AppLanguage.RUSSIAN -> "Выберите полный архив WordPress (.zip) с темами и плагинами"
        AppLanguage.JAPANESE -> "テーマとプラグインを含む完全なWordPressアーカイブ(.zip)を選択"
        AppLanguage.KOREAN -> "테마와 플러그인이 포함된 완전한 WordPress 아카이브(.zip)를 선택하세요"
    }

    val wpOrCreateNew: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "或者创建全新站点"
        AppLanguage.ENGLISH -> "Or create a new site"
        AppLanguage.ARABIC -> "أو أنشئ موقعًا جديدًا"
        AppLanguage.PORTUGUESE -> "Ou crie um novo site"
        AppLanguage.SPANISH -> "O crea un nuevo sitio"
        AppLanguage.FRENCH -> "Ou créez un nouveau site"
        AppLanguage.GERMAN -> "Oder neue Website erstellen"
        AppLanguage.RUSSIAN -> "Или создайте новый сайт"
        AppLanguage.JAPANESE -> "または新規サイトを作成"
        AppLanguage.KOREAN -> "또는 새 사이트 만들기"
    }

    val wpCreateNewSite: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "创建新站点"
        AppLanguage.ENGLISH -> "Create New Site"
        AppLanguage.ARABIC -> "إنشاء موقع جديد"
        AppLanguage.PORTUGUESE -> "Criar Novo Site"
        AppLanguage.SPANISH -> "Crear Nuevo Sitio"
        AppLanguage.FRENCH -> "Créer un nouveau site"
        AppLanguage.GERMAN -> "Neue Website erstellen"
        AppLanguage.RUSSIAN -> "Создать новый сайт"
        AppLanguage.JAPANESE -> "新規サイトを作成"
        AppLanguage.KOREAN -> "새 사이트 만들기"
    }

    val wpCreateNewSiteDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "使用默认 WordPress 核心创建空白站点"
        AppLanguage.ENGLISH -> "Create blank site with default WordPress core"
        AppLanguage.ARABIC -> "إنشاء موقع فارغ باستخدام نواة WordPress الافتراضية"
        AppLanguage.PORTUGUESE -> "Criar site vazio com o núcleo padrão do WordPress"
        AppLanguage.SPANISH -> "Crear sitio vacío con el núcleo predeterminado de WordPress"
        AppLanguage.FRENCH -> "Créer un site vide avec le cœur WordPress par défaut"
        AppLanguage.GERMAN -> "Leere Website mit Standard-WordPress-Core erstellen"
        AppLanguage.RUSSIAN -> "Создать пустой сайт с ядром WordPress по умолчанию"
        AppLanguage.JAPANESE -> "デフォルトのWordPressコアで空のサイトを作成"
        AppLanguage.KOREAN -> "기본 WordPress 코어로 빈 사이트 만들기"
    }

    val wpSettings: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "WordPress 设置"
        AppLanguage.ENGLISH -> "WordPress Settings"
        AppLanguage.ARABIC -> "إعدادات WordPress"
        AppLanguage.PORTUGUESE -> "Configurações do WordPress"
        AppLanguage.SPANISH -> "Ajustes de WordPress"
        AppLanguage.FRENCH -> "Réglages WordPress"
        AppLanguage.GERMAN -> "WordPress-Einstellungen"
        AppLanguage.RUSSIAN -> "Настройки WordPress"
        AppLanguage.JAPANESE -> "WordPress設定"
        AppLanguage.KOREAN -> "WordPress 설정"
    }

    val wpCacheSize: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "缓存占用"
        AppLanguage.ENGLISH -> "Cache Size"
        AppLanguage.ARABIC -> "حجم ذاكرة التخزين المؤقت"
        AppLanguage.PORTUGUESE -> "Tamanho do Cache"
        AppLanguage.SPANISH -> "Tamaño de Caché"
        AppLanguage.FRENCH -> "Taille du cache"
        AppLanguage.GERMAN -> "Cache-Größe"
        AppLanguage.RUSSIAN -> "Размер кэша"
        AppLanguage.JAPANESE -> "キャッシュサイズ"
        AppLanguage.KOREAN -> "캐시 크기"
    }

    val wpProjectReady: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "WordPress 项目已就绪"
        AppLanguage.ENGLISH -> "WordPress project ready"
        AppLanguage.ARABIC -> "مشروع WordPress جاهز"
        AppLanguage.PORTUGUESE -> "Projeto WordPress pronto"
        AppLanguage.SPANISH -> "Proyecto WordPress listo"
        AppLanguage.FRENCH -> "Projet WordPress prêt"
        AppLanguage.GERMAN -> "WordPress-Projekt bereit"
        AppLanguage.RUSSIAN -> "Проект WordPress готов"
        AppLanguage.JAPANESE -> "WordPressプロジェクトの準備が完了しました"
        AppLanguage.KOREAN -> "WordPress 프로젝트 준비 완료"
    }

    val njsCreateTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "创建 Node.js 应用"
        AppLanguage.ENGLISH -> "Create Node.js App"
        AppLanguage.ARABIC -> "إنشاء تطبيق Node.js"
        AppLanguage.PORTUGUESE -> "Criar App Node.js"
        AppLanguage.SPANISH -> "Crear App Node.js"
        AppLanguage.FRENCH -> "Créer une app Node.js"
        AppLanguage.GERMAN -> "Node.js-App erstellen"
        AppLanguage.RUSSIAN -> "Создать приложение Node.js"
        AppLanguage.JAPANESE -> "Node.jsアプリを作成"
        AppLanguage.KOREAN -> "Node.js 앱 만들기"
    }

    val njsBasicConfig: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "基本配置"
        AppLanguage.ENGLISH -> "Basic Configuration"
        AppLanguage.ARABIC -> "الإعدادات الأساسية"
        AppLanguage.PORTUGUESE -> "Configuração Básica"
        AppLanguage.SPANISH -> "Configuración Básica"
        AppLanguage.FRENCH -> "Configuration de base"
        AppLanguage.GERMAN -> "Grundkonfiguration"
        AppLanguage.RUSSIAN -> "Базовая конфигурация"
        AppLanguage.JAPANESE -> "基本設定"
        AppLanguage.KOREAN -> "기본 구성"
    }

    val njsSelectProjectFolder: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择项目文件夹"
        AppLanguage.ENGLISH -> "Select Project Folder"
        AppLanguage.ARABIC -> "اختر مجلد المشروع"
        AppLanguage.PORTUGUESE -> "Selecionar Pasta do Projeto"
        AppLanguage.SPANISH -> "Seleccionar Carpeta del Proyecto"
        AppLanguage.FRENCH -> "Sélectionner le dossier du projet"
        AppLanguage.GERMAN -> "Projektordner auswählen"
        AppLanguage.RUSSIAN -> "Выберите папку проекта"
        AppLanguage.JAPANESE -> "プロジェクトフォルダを選択"
        AppLanguage.KOREAN -> "프로젝트 폴더 선택"
    }

    val njsSelectProjectDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择包含 package.json 的 Node.js 项目目录"
        AppLanguage.ENGLISH -> "Select a Node.js project directory containing package.json"
        AppLanguage.ARABIC -> "حدد مجلد مشروع Node.js يحتوي على package.json"
        AppLanguage.PORTUGUESE -> "Selecione um diretório de projeto Node.js contendo package.json"
        AppLanguage.SPANISH -> "Selecciona un directorio de proyecto Node.js que contenga package.json"
        AppLanguage.FRENCH -> "Sélectionnez un répertoire de projet Node.js contenant package.json"
        AppLanguage.GERMAN -> "Node.js-Projektverzeichnis mit package.json auswählen"
        AppLanguage.RUSSIAN -> "Выберите каталог проекта Node.js, содержащий package.json"
        AppLanguage.JAPANESE -> "package.jsonを含むNode.jsプロジェクトディレクトリを選択"
        AppLanguage.KOREAN -> "package.json을 포함한 Node.js 프로젝트 디렉터리를 선택하세요"
    }

    val njsBuildMode: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "构建模式"
        AppLanguage.ENGLISH -> "Build Mode"
        AppLanguage.ARABIC -> "وضع البناء"
        AppLanguage.PORTUGUESE -> "Modo de Build"
        AppLanguage.SPANISH -> "Modo de Compilación"
        AppLanguage.FRENCH -> "Mode de build"
        AppLanguage.GERMAN -> "Build-Modus"
        AppLanguage.RUSSIAN -> "Режим сборки"
        AppLanguage.JAPANESE -> "ビルドモード"
        AppLanguage.KOREAN -> "빌드 모드"
    }

    val njsModeStatic: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "静态前端"
        AppLanguage.ENGLISH -> "Static Frontend"
        AppLanguage.ARABIC -> "الواجهة الأمامية الثابتة"
        AppLanguage.PORTUGUESE -> "Frontend Estático"
        AppLanguage.SPANISH -> "Frontend Estático"
        AppLanguage.FRENCH -> "Frontend statique"
        AppLanguage.GERMAN -> "Statisches Frontend"
        AppLanguage.RUSSIAN -> "Статический фронтенд"
        AppLanguage.JAPANESE -> "静的フロントエンド"
        AppLanguage.KOREAN -> "정적 프런트엔드"
    }

    val njsModeStaticDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "打包构建产物（dist 目录）为本地应用"
        AppLanguage.ENGLISH -> "Package build output (dist) as local app"
        AppLanguage.ARABIC -> "تغليف مخرجات البناء (dist) كتطبيق محلي"
        AppLanguage.PORTUGUESE -> "Empacotar saída de build (dist) como app local"
        AppLanguage.SPANISH -> "Empaquetar salida de build (dist) como app local"
        AppLanguage.FRENCH -> "Empaqueter la sortie de build (dist) comme app locale"
        AppLanguage.GERMAN -> "Build-Ausgabe (dist) als lokale App verpacken"
        AppLanguage.RUSSIAN -> "Упаковать вывод сборки (dist) как локальное приложение"
        AppLanguage.JAPANESE -> "ビルド出力（dist）をローカルアプリとしてパッケージ化"
        AppLanguage.KOREAN -> "빌드 출력(dist)을 로컬 앱으로 패키징"
    }

    val njsModeBackend: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "API 后端"
        AppLanguage.ENGLISH -> "API Backend"
        AppLanguage.ARABIC -> "الخلفية API"
        AppLanguage.PORTUGUESE -> "Backend de API"
        AppLanguage.SPANISH -> "Backend de API"
        AppLanguage.FRENCH -> "Backend API"
        AppLanguage.GERMAN -> "API-Backend"
        AppLanguage.RUSSIAN -> "API-бэкенд"
        AppLanguage.JAPANESE -> "APIバックエンド"
        AppLanguage.KOREAN -> "API 백엔드"
    }

    val njsModeBackendDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "运行 Node.js 服务器（Express/Fastify/Koa 等）"
        AppLanguage.ENGLISH -> "Run Node.js server (Express/Fastify/Koa etc.)"
        AppLanguage.ARABIC -> "تشغيل خادم Node.js (Express/Fastify/Koa إلخ)"
        AppLanguage.PORTUGUESE -> "Executar servidor Node.js (Express/Fastify/Koa etc.)"
        AppLanguage.SPANISH -> "Ejecutar servidor Node.js (Express/Fastify/Koa etc.)"
        AppLanguage.FRENCH -> "Exécuter un serveur Node.js (Express/Fastify/Koa etc.)"
        AppLanguage.GERMAN -> "Node.js-Server ausführen (Express/Fastify/Koa etc.)"
        AppLanguage.RUSSIAN -> "Запуск сервера Node.js (Express/Fastify/Koa и т.д.)"
        AppLanguage.JAPANESE -> "Node.jsサーバーを実行（Express/Fastify/Koa など）"
        AppLanguage.KOREAN -> "Node.js 서버 실행 (Express/Fastify/Koa 등)"
    }

    val njsModeFullstack: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "全栈应用"
        AppLanguage.ENGLISH -> "Fullstack App"
        AppLanguage.ARABIC -> "تطبيق كامل"
        AppLanguage.PORTUGUESE -> "App Fullstack"
        AppLanguage.SPANISH -> "App Fullstack"
        AppLanguage.FRENCH -> "App fullstack"
        AppLanguage.GERMAN -> "Fullstack-App"
        AppLanguage.RUSSIAN -> "Fullstack-приложение"
        AppLanguage.JAPANESE -> "フルスタックアプリ"
        AppLanguage.KOREAN -> "풀스택 앱"
    }

    val njsModeFullstackDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "前端 + API 服务器（Next.js/Nuxt.js 等）"
        AppLanguage.ENGLISH -> "Frontend + API server (Next.js/Nuxt.js etc.)"
        AppLanguage.ARABIC -> "الواجهة + خادم API (Next.js/Nuxt.js إلخ)"
        AppLanguage.PORTUGUESE -> "Frontend + servidor API (Next.js/Nuxt.js etc.)"
        AppLanguage.SPANISH -> "Frontend + servidor API (Next.js/Nuxt.js etc.)"
        AppLanguage.FRENCH -> "Frontend + serveur API (Next.js/Nuxt.js etc.)"
        AppLanguage.GERMAN -> "Frontend + API-Server (Next.js/Nuxt.js etc.)"
        AppLanguage.RUSSIAN -> "Фронтенд + API-сервер (Next.js/Nuxt.js и т.д.)"
        AppLanguage.JAPANESE -> "フロントエンド + APIサーバー（Next.js/Nuxt.js など）"
        AppLanguage.KOREAN -> "프런트엔드 + API 서버 (Next.js/Nuxt.js 등)"
    }

    val njsEntryFile: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "入口文件"
        AppLanguage.ENGLISH -> "Entry File"
        AppLanguage.ARABIC -> "ملف الإدخال"
        AppLanguage.PORTUGUESE -> "Arquivo de Entrada"
        AppLanguage.SPANISH -> "Archivo de Entrada"
        AppLanguage.FRENCH -> "Fichier d'entrée"
        AppLanguage.GERMAN -> "Einstiegsdatei"
        AppLanguage.RUSSIAN -> "Точка входа"
        AppLanguage.JAPANESE -> "エントリファイル"
        AppLanguage.KOREAN -> "진입 파일"
    }

    val njsEntryFileHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "如 server.js, index.js, app.js"
        AppLanguage.ENGLISH -> "e.g. server.js, index.js, app.js"
        AppLanguage.ARABIC -> "مثل server.js, index.js, app.js"
        AppLanguage.PORTUGUESE -> "ex. server.js, index.js, app.js"
        AppLanguage.SPANISH -> "ej. server.js, index.js, app.js"
        AppLanguage.FRENCH -> "ex. server.js, index.js, app.js"
        AppLanguage.GERMAN -> "z.B. server.js, index.js, app.js"
        AppLanguage.RUSSIAN -> "напр. server.js, index.js, app.js"
        AppLanguage.JAPANESE -> "例: server.js, index.js, app.js"
        AppLanguage.KOREAN -> "예: server.js, index.js, app.js"
    }

    val njsEnvVars: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "环境变量"
        AppLanguage.ENGLISH -> "Environment Variables"
        AppLanguage.ARABIC -> "متغيرات البيئة"
        AppLanguage.PORTUGUESE -> "Variáveis de Ambiente"
        AppLanguage.SPANISH -> "Variables de Entorno"
        AppLanguage.FRENCH -> "Variables d'environnement"
        AppLanguage.GERMAN -> "Umgebungsvariablen"
        AppLanguage.RUSSIAN -> "Переменные окружения"
        AppLanguage.JAPANESE -> "環境変数"
        AppLanguage.KOREAN -> "환경 변수"
    }

    val njsAddEnvVar: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "添加环境变量"
        AppLanguage.ENGLISH -> "Add Env Variable"
        AppLanguage.ARABIC -> "إضافة متغير بيئة"
        AppLanguage.PORTUGUESE -> "Adicionar Variável de Ambiente"
        AppLanguage.SPANISH -> "Añadir Variable de Entorno"
        AppLanguage.FRENCH -> "Ajouter une variable d'environnement"
        AppLanguage.GERMAN -> "Umgebungsvariable hinzufügen"
        AppLanguage.RUSSIAN -> "Добавить переменную окружения"
        AppLanguage.JAPANESE -> "環境変数を追加"
        AppLanguage.KOREAN -> "환경 변수 추가"
    }

    val njsEnvKey: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "键"
        AppLanguage.ENGLISH -> "Key"
        AppLanguage.ARABIC -> "المفتاح"
        AppLanguage.PORTUGUESE -> "Chave"
        AppLanguage.SPANISH -> "Clave"
        AppLanguage.FRENCH -> "Clé"
        AppLanguage.GERMAN -> "Schlüssel"
        AppLanguage.RUSSIAN -> "Ключ"
        AppLanguage.JAPANESE -> "キー"
        AppLanguage.KOREAN -> "키"
    }

    val njsEnvValue: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "值"
        AppLanguage.ENGLISH -> "Value"
        AppLanguage.ARABIC -> "القيمة"
        AppLanguage.PORTUGUESE -> "Valor"
        AppLanguage.SPANISH -> "Valor"
        AppLanguage.FRENCH -> "Valeur"
        AppLanguage.GERMAN -> "Wert"
        AppLanguage.RUSSIAN -> "Значение"
        AppLanguage.JAPANESE -> "値"
        AppLanguage.KOREAN -> "값"
    }

    val frameworkTips: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "框架提示"
        AppLanguage.ENGLISH -> "Framework Tips"
        AppLanguage.ARABIC -> "نصائح الإطار"
        AppLanguage.PORTUGUESE -> "Dicas de Framework"
        AppLanguage.SPANISH -> "Consejos de Framework"
        AppLanguage.FRENCH -> "Astuces Framework"
        AppLanguage.GERMAN -> "Framework-Tipps"
        AppLanguage.RUSSIAN -> "Подсказки по фреймворкам"
        AppLanguage.JAPANESE -> "フレームワークのヒント"
        AppLanguage.KOREAN -> "프레임워크 팁"
    }

    fun njsFrameworkTips(framework: String): List<String> = when (Strings.lang) {
        AppLanguage.CHINESE -> when (framework) {
            "Express" -> listOf(
                "确保 app.listen() 绑定到 0.0.0.0 而非 localhost",
                "静态文件使用 express.static() 中间件",
                "生产模式设置 NODE_ENV=production 以优化性能"
            )
            "Fastify" -> listOf(
                "使用 host: '0.0.0.0' 配置监听地址",
                "Fastify 原生支持 JSON Schema 验证",
                "推荐使用 @fastify/static 插件服务静态文件"
            )
            "Koa" -> listOf(
                "Koa 需要显式安装路由中间件 (koa-router)",
                "使用 koa-static 服务静态资源",
                "Koa 的洋葱模型适合复杂中间件编排"
            )
            "NestJS" -> listOf(
                "NestJS 入口文件通常为 dist/main.js",
                "确保先运行 nest build 生成 dist 目录",
                "在 main.ts 中设置 app.listen(port, '0.0.0.0')"
            )
            "Next.js" -> listOf(
                "Next.js 使用 next start 启动生产服务器",
                "确保已运行 next build 生成 .next 目录",
                "API 路由位于 pages/api 或 app/api 目录"
            )
            "Nuxt.js" -> listOf(
                "Nuxt 3 使用 .output 目录作为生产输出",
                "运行 nuxi build 生成服务端产物",
                "Nitro 服务引擎自动处理 API 路由"
            )
            "Hapi" -> listOf(
                "Hapi 使用 server.start() 启动服务",
                "配置 host: '0.0.0.0' 以允许外部访问",
                "使用 @hapi/inert 插件服务静态文件"
            )
            else -> listOf(
                "确保服务器监听 0.0.0.0 而非 127.0.0.1",
                "使用 PORT 环境变量配置端口号",
                "生产模式请设置 NODE_ENV=production"
            )
        }

        AppLanguage.ENGLISH -> when (framework) {
            "Express" -> listOf(
                "Ensure app.listen() binds to 0.0.0.0 instead of localhost",
                "Use express.static() to serve static files",
                "Set NODE_ENV=production in production mode for better performance"
            )
            "Fastify" -> listOf(
                "Use host: '0.0.0.0' for the listen address",
                "Fastify includes native JSON Schema validation",
                "Use @fastify/static to serve static files"
            )
            "Koa" -> listOf(
                "Koa usually needs an explicit router middleware such as koa-router",
                "Use koa-static for static assets",
                "Koa's onion middleware model works well for complex pipelines"
            )
            "NestJS" -> listOf(
                "NestJS entry output is usually dist/main.js",
                "Run nest build before packaging to generate the dist directory",
                "Set app.listen(port, '0.0.0.0') in main.ts"
            )
            "Next.js" -> listOf(
                "Use next start to run the production server",
                "Run next build first so the .next directory exists",
                "API routes live under pages/api or app/api"
            )
            "Nuxt.js" -> listOf(
                "Nuxt 3 uses the .output directory for production output",
                "Run nuxi build to generate the server bundle",
                "Nitro handles API routes automatically"
            )
            "Hapi" -> listOf(
                "Start the server with server.start()",
                "Set host: '0.0.0.0' to allow external access",
                "Use @hapi/inert to serve static files"
            )
            else -> listOf(
                "Ensure the server listens on 0.0.0.0 instead of 127.0.0.1",
                "Use the PORT environment variable for the server port",
                "Set NODE_ENV=production for production builds"
            )
        }

        AppLanguage.ARABIC -> when (framework) {
            "Express" -> listOf(
                "تأكد من أن app.listen() يرتبط بـ 0.0.0.0 بدلاً من localhost",
                "استخدم express.static() لخدمة الملفات الثابتة",
                "اضبط NODE_ENV=production في وضع الإنتاج لتحسين الأداء"
            )
            "Fastify" -> listOf(
                "استخدم host: '0.0.0.0' لعنوان الاستماع",
                "يوفر Fastify تحقق JSON Schema بشكل أصلي",
                "استخدم @fastify/static لخدمة الملفات الثابتة"
            )
            "Koa" -> listOf(
                "يحتاج Koa عادةً إلى وسيط توجيه صريح مثل koa-router",
                "استخدم koa-static لخدمة الموارد الثابتة",
                "نموذج الوسطاء المتداخل في Koa مناسب للتجميعات المعقدة"
            )
            "NestJS" -> listOf(
                "ملف الإخراج الرئيسي في NestJS يكون غالباً dist/main.js",
                "شغّل nest build أولاً لإنشاء مجلد dist",
                "اضبط app.listen(port, '0.0.0.0') في main.ts"
            )
            "Next.js" -> listOf(
                "استخدم next start لتشغيل خادم الإنتاج",
                "شغّل next build أولاً حتى يتم إنشاء مجلد .next",
                "توجد مسارات API داخل pages/api أو app/api"
            )
            "Nuxt.js" -> listOf(
                "يستخدم Nuxt 3 مجلد .output كمخرج إنتاج",
                "شغّل nuxi build لإنشاء حزمة الخادم",
                "يتولى Nitro معالجة مسارات API تلقائياً"
            )
            "Hapi" -> listOf(
                "ابدأ الخادم باستخدام server.start()",
                "اضبط host: '0.0.0.0' للسماح بالوصول الخارجي",
                "استخدم @hapi/inert لخدمة الملفات الثابتة"
            )
            else -> listOf(
                "تأكد من أن الخادم يستمع على 0.0.0.0 بدلاً من 127.0.0.1",
                "استخدم متغير البيئة PORT لضبط منفذ الخادم",
                "اضبط NODE_ENV=production في بيئات الإنتاج"
            )
        }

        AppLanguage.PORTUGUESE -> when (framework) {
            "Express" -> listOf(
                "Garanta que app.listen() escute em 0.0.0.0 em vez de localhost",
                "Use express.static() para servir arquivos estáticos",
                "Defina NODE_ENV=production em produção para melhor desempenho"
            )
            "Fastify" -> listOf(
                "Use host: '0.0.0.0' como endereço de escuta",
                "O Fastify inclui validação nativa com JSON Schema",
                "Use @fastify/static para servir arquivos estáticos"
            )
            "Koa" -> listOf(
                "O Koa geralmente precisa de um middleware de rotas explícito como koa-router",
                "Use koa-static para recursos estáticos",
                "O modelo de middleware em cebola do Koa funciona bem em pipelines complexos"
            )
            "NestJS" -> listOf(
                "A saída de entrada do NestJS costuma ser dist/main.js",
                "Execute nest build antes de empacotar para gerar o diretório dist",
                "Defina app.listen(port, '0.0.0.0') em main.ts"
            )
            "Next.js" -> listOf(
                "Use next start para o servidor de produção",
                "Execute next build primeiro para gerar o diretório .next",
                "As rotas de API ficam em pages/api ou app/api"
            )
            "Nuxt.js" -> listOf(
                "O Nuxt 3 usa o diretório .output para a saída de produção",
                "Execute nuxi build para gerar o bundle do servidor",
                "O Nitro trata as rotas de API automaticamente"
            )
            "Hapi" -> listOf(
                "Inicie o servidor com server.start()",
                "Defina host: '0.0.0.0' para permitir acesso externo",
                "Use @hapi/inert para servir arquivos estáticos"
            )
            else -> listOf(
                "Garanta que o servidor escute em 0.0.0.0 em vez de 127.0.0.1",
                "Use a variável de ambiente PORT para a porta do servidor",
                "Defina NODE_ENV=production em builds de produção"
            )
        }

        AppLanguage.SPANISH -> when (framework) {
            "Express" -> listOf(
                "Asegúrate de que app.listen() escuche en 0.0.0.0 en lugar de localhost",
                "Usa express.static() para servir archivos estáticos",
                "Define NODE_ENV=production en producción para mejor rendimiento"
            )
            "Fastify" -> listOf(
                "Usa host: '0.0.0.0' como dirección de escucha",
                "Fastify incluye validación nativa con JSON Schema",
                "Usa @fastify/static para servir archivos estáticos"
            )
            "Koa" -> listOf(
                "Koa suele necesitar un middleware de rutas explícito como koa-router",
                "Usa koa-static para recursos estáticos",
                "El modelo de middleware en cebolla de Koa funciona bien en pipelines complejos"
            )
            "NestJS" -> listOf(
                "La salida de entrada de NestJS suele ser dist/main.js",
                "Ejecuta nest build antes de empaquetar para generar el directorio dist",
                "Define app.listen(port, '0.0.0.0') en main.ts"
            )
            "Next.js" -> listOf(
                "Usa next start para el servidor de producción",
                "Ejecuta next build primero para generar el directorio .next",
                "Las rutas API están en pages/api o app/api"
            )
            "Nuxt.js" -> listOf(
                "Nuxt 3 usa el directorio .output para la salida de producción",
                "Ejecuta nuxi build para generar el bundle del servidor",
                "Nitro gestiona las rutas API automáticamente"
            )
            "Hapi" -> listOf(
                "Inicia el servidor con server.start()",
                "Define host: '0.0.0.0' para permitir acceso externo",
                "Usa @hapi/inert para servir archivos estáticos"
            )
            else -> listOf(
                "Asegúrate de que el servidor escuche en 0.0.0.0 en lugar de 127.0.0.1",
                "Usa la variable de entorno PORT para el puerto del servidor",
                "Define NODE_ENV=production en builds de producción"
            )
        }

        AppLanguage.FRENCH -> when (framework) {
            "Express" -> listOf(
                "Assurez-vous que app.listen() écoute sur 0.0.0.0 plutôt que localhost",
                "Utilisez express.static() pour servir les fichiers statiques",
                "Définissez NODE_ENV=production en production pour de meilleures performances"
            )
            "Fastify" -> listOf(
                "Utilisez host: '0.0.0.0' comme adresse d'écoute",
                "Fastify inclut une validation JSON Schema native",
                "Utilisez @fastify/static pour servir les fichiers statiques"
            )
            "Koa" -> listOf(
                "Koa a généralement besoin d'un middleware de routes explicite comme koa-router",
                "Utilisez koa-static pour les ressources statiques",
                "Le modèle de middleware en oignon de Koa convient aux pipelines complexes"
            )
            "NestJS" -> listOf(
                "La sortie d'entrée NestJS est généralement dist/main.js",
                "Exécutez nest build avant le packaging pour générer le dossier dist",
                "Définissez app.listen(port, '0.0.0.0') dans main.ts"
            )
            "Next.js" -> listOf(
                "Utilisez next start pour le serveur de production",
                "Exécutez d'abord next build pour générer le dossier .next",
                "Les routes API se trouvent dans pages/api ou app/api"
            )
            "Nuxt.js" -> listOf(
                "Nuxt 3 utilise le dossier .output pour la sortie de production",
                "Exécutez nuxi build pour générer le bundle serveur",
                "Nitro gère automatiquement les routes API"
            )
            "Hapi" -> listOf(
                "Démarrez le serveur avec server.start()",
                "Définissez host: '0.0.0.0' pour autoriser l'accès externe",
                "Utilisez @hapi/inert pour servir les fichiers statiques"
            )
            else -> listOf(
                "Assurez-vous que le serveur écoute sur 0.0.0.0 plutôt que 127.0.0.1",
                "Utilisez la variable d'environnement PORT pour le port du serveur",
                "Définissez NODE_ENV=production pour les builds de production"
            )
        }

        AppLanguage.GERMAN -> when (framework) {
            "Express" -> listOf(
                "Stellen Sie sicher, dass app.listen() an 0.0.0.0 statt localhost gebunden ist",
                "Verwenden Sie express.static() für statische Dateien",
                "Setzen Sie NODE_ENV=production in Produktion für bessere Leistung"
            )
            "Fastify" -> listOf(
                "Verwenden Sie host: '0.0.0.0' als Listen-Adresse",
                "Fastify enthält native JSON-Schema-Validierung",
                "Verwenden Sie @fastify/static für statische Dateien"
            )
            "Koa" -> listOf(
                "Koa benötigt in der Regel ein explizites Router-Middleware wie koa-router",
                "Verwenden Sie koa-static für statische Assets",
                "Das Zwiebel-Middleware-Modell von Koa eignet sich gut für komplexe Pipelines"
            )
            "NestJS" -> listOf(
                "Die NestJS-Eingabeausgabe ist meist dist/main.js",
                "Führen Sie nest build vor dem Packen aus, um das dist-Verzeichnis zu erzeugen",
                "Setzen Sie app.listen(port, '0.0.0.0') in main.ts"
            )
            "Next.js" -> listOf(
                "Verwenden Sie next start für den Produktionsserver",
                "Führen Sie zuerst next build aus, damit das .next-Verzeichnis existiert",
                "API-Routen liegen unter pages/api oder app/api"
            )
            "Nuxt.js" -> listOf(
                "Nuxt 3 verwendet das .output-Verzeichnis für die Produktionsausgabe",
                "Führen Sie nuxi build aus, um das Server-Bundle zu erzeugen",
                "Nitro verarbeitet API-Routen automatisch"
            )
            "Hapi" -> listOf(
                "Starten Sie den Server mit server.start()",
                "Setzen Sie host: '0.0.0.0', um externen Zugriff zu erlauben",
                "Verwenden Sie @hapi/inert für statische Dateien"
            )
            else -> listOf(
                "Stellen Sie sicher, dass der Server auf 0.0.0.0 statt 127.0.0.1 lauscht",
                "Verwenden Sie die Umgebungsvariable PORT für den Serverport",
                "Setzen Sie NODE_ENV=production für Produktionsbuilds"
            )
        }

        AppLanguage.RUSSIAN -> when (framework) {
            "Express" -> listOf(
                "Убедитесь, что app.listen() слушает 0.0.0.0, а не localhost",
                "Используйте express.static() для раздачи статики",
                "Задайте NODE_ENV=production в продакшене для лучшей производительности"
            )
            "Fastify" -> listOf(
                "Используйте host: '0.0.0.0' как адрес прослушивания",
                "В Fastify есть встроенная проверка JSON Schema",
                "Используйте @fastify/static для раздачи статики"
            )
            "Koa" -> listOf(
                "Koa обычно нужен явный middleware маршрутизации, например koa-router",
                "Используйте koa-static для статических ресурсов",
                "Луковичная модель middleware Koa удобна для сложных конвейеров"
            )
            "NestJS" -> listOf(
                "Точка входа NestJS обычно dist/main.js",
                "Перед упаковкой выполните nest build, чтобы создать каталог dist",
                "Задайте app.listen(port, '0.0.0.0') в main.ts"
            )
            "Next.js" -> listOf(
                "Используйте next start для production-сервера",
                "Сначала выполните next build, чтобы появился каталог .next",
                "API-маршруты находятся в pages/api или app/api"
            )
            "Nuxt.js" -> listOf(
                "Nuxt 3 использует каталог .output для production-сборки",
                "Выполните nuxi build, чтобы собрать серверный бандл",
                "Nitro автоматически обрабатывает API-маршруты"
            )
            "Hapi" -> listOf(
                "Запускайте сервер через server.start()",
                "Задайте host: '0.0.0.0', чтобы разрешить внешний доступ",
                "Используйте @hapi/inert для раздачи статики"
            )
            else -> listOf(
                "Убедитесь, что сервер слушает 0.0.0.0, а не 127.0.0.1",
                "Используйте переменную окружения PORT для порта сервера",
                "Задайте NODE_ENV=production для production-сборок"
            )
        }

        AppLanguage.JAPANESE -> when (framework) {
            "Express" -> listOf(
                "app.listen() は localhost ではなく 0.0.0.0 にバインドしてください",
                "静的ファイルは express.static() で配信します",
                "本番では NODE_ENV=production を設定して性能を最適化します"
            )
            "Fastify" -> listOf(
                "リッスンアドレスに host: '0.0.0.0' を使います",
                "Fastify は JSON Schema 検証を標準搭載しています",
                "静的ファイルは @fastify/static で配信します"
            )
            "Koa" -> listOf(
                "Koa は通常 koa-router などの明示的なルーティング middleware が必要です",
                "静的アセットには koa-static を使います",
                "Koa のオニオン型 middleware モデルは複雑なパイプラインに向きます"
            )
            "NestJS" -> listOf(
                "NestJS のエントリ出力は通常 dist/main.js です",
                "パッケージ前に nest build を実行して dist を生成してください",
                "main.ts で app.listen(port, '0.0.0.0') を設定します"
            )
            "Next.js" -> listOf(
                "本番サーバーは next start で起動します",
                "先に next build を実行して .next を生成してください",
                "API ルートは pages/api または app/api にあります"
            )
            "Nuxt.js" -> listOf(
                "Nuxt 3 の本番出力は .output ディレクトリです",
                "nuxi build でサーバーバンドルを生成します",
                "Nitro が API ルートを自動処理します"
            )
            "Hapi" -> listOf(
                "server.start() でサーバーを起動します",
                "外部アクセスを許可するには host: '0.0.0.0' を設定します",
                "静的ファイルは @hapi/inert で配信します"
            )
            else -> listOf(
                "サーバーは 127.0.0.1 ではなく 0.0.0.0 で待ち受けてください",
                "ポートは PORT 環境変数で設定します",
                "本番ビルドでは NODE_ENV=production を設定します"
            )
        }

        AppLanguage.KOREAN -> when (framework) {
            "Express" -> listOf(
                "app.listen()이 localhost가 아닌 0.0.0.0에 바인딩되도록 하세요",
                "정적 파일은 express.static()으로 제공하세요",
                "프로덕션에서는 NODE_ENV=production을 설정해 성능을 높이세요"
            )
            "Fastify" -> listOf(
                "수신 주소로 host: '0.0.0.0'을 사용하세요",
                "Fastify는 JSON Schema 검증을 기본 제공합니다",
                "정적 파일은 @fastify/static으로 제공하세요"
            )
            "Koa" -> listOf(
                "Koa는 보통 koa-router 같은 명시적 라우터 미들웨어가 필요합니다",
                "정적 자산에는 koa-static을 사용하세요",
                "Koa의 양파형 미들웨어 모델은 복잡한 파이프라인에 적합합니다"
            )
            "NestJS" -> listOf(
                "NestJS 진입 출력은 보통 dist/main.js입니다",
                "패키징 전에 nest build로 dist를 생성하세요",
                "main.ts에서 app.listen(port, '0.0.0.0')을 설정하세요"
            )
            "Next.js" -> listOf(
                "프로덕션 서버는 next start로 실행하세요",
                "먼저 next build로 .next 디렉터리를 만드세요",
                "API 라우트는 pages/api 또는 app/api에 있습니다"
            )
            "Nuxt.js" -> listOf(
                "Nuxt 3 프로덕션 출력은 .output 디렉터리입니다",
                "nuxi build로 서버 번들을 생성하세요",
                "Nitro가 API 라우트를 자동 처리합니다"
            )
            "Hapi" -> listOf(
                "server.start()로 서버를 시작하세요",
                "외부 접근을 허용하려면 host: '0.0.0.0'을 설정하세요",
                "정적 파일은 @hapi/inert로 제공하세요"
            )
            else -> listOf(
                "서버가 127.0.0.1이 아닌 0.0.0.0에서 수신하도록 하세요",
                "포트는 PORT 환경 변수로 설정하세요",
                "프로덕션 빌드에서는 NODE_ENV=production을 설정하세요"
            )
        }
    }

    val njsDownloadDeps: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下载 Node.js 依赖"
        AppLanguage.ENGLISH -> "Download Node.js Dependencies"
        AppLanguage.ARABIC -> "تنزيل متطلبات Node.js"
        AppLanguage.PORTUGUESE -> "Baixar Dependências do Node.js"
        AppLanguage.SPANISH -> "Descargar Dependencias de Node.js"
        AppLanguage.FRENCH -> "Télécharger les dépendances Node.js"
        AppLanguage.GERMAN -> "Node.js-Abhängigkeiten herunterladen"
        AppLanguage.RUSSIAN -> "Загрузить зависимости Node.js"
        AppLanguage.JAPANESE -> "Node.jsの依存関係をダウンロード"
        AppLanguage.KOREAN -> "Node.js 종속성 다운로드"
    }

    val njsDownloading: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "正在下载 Node.js 运行时..."
        AppLanguage.ENGLISH -> "Downloading Node.js runtime..."
        AppLanguage.ARABIC -> "جاري تنزيل Node.js..."
        AppLanguage.PORTUGUESE -> "Baixando runtime Node.js..."
        AppLanguage.SPANISH -> "Descargando runtime Node.js..."
        AppLanguage.FRENCH -> "Téléchargement du runtime Node.js..."
        AppLanguage.GERMAN -> "Node.js-Runtime wird heruntergeladen..."
        AppLanguage.RUSSIAN -> "Загрузка среды выполнения Node.js..."
        AppLanguage.JAPANESE -> "Node.jsランタイムをダウンロード中..."
        AppLanguage.KOREAN -> "Node.js 런타임 다운로드 중..."
    }

    val njsDownloadComplete: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Node.js 运行时已就绪"
        AppLanguage.ENGLISH -> "Node.js runtime ready"
        AppLanguage.ARABIC -> "Node.js جاهز"
        AppLanguage.PORTUGUESE -> "Runtime Node.js pronto"
        AppLanguage.SPANISH -> "Runtime Node.js listo"
        AppLanguage.FRENCH -> "Runtime Node.js prêt"
        AppLanguage.GERMAN -> "Node.js-Runtime bereit"
        AppLanguage.RUSSIAN -> "Среда выполнения Node.js готова"
        AppLanguage.JAPANESE -> "Node.jsランタイムの準備が完了しました"
        AppLanguage.KOREAN -> "Node.js 런타임 준비 완료"
    }

    val njsDownloadFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Node.js 下载失败"
        AppLanguage.ENGLISH -> "Node.js download failed"
        AppLanguage.ARABIC -> "فشل تنزيل Node.js"
        AppLanguage.PORTUGUESE -> "Falha no download do Node.js"
        AppLanguage.SPANISH -> "Error al descargar Node.js"
        AppLanguage.FRENCH -> "Échec du téléchargement de Node.js"
        AppLanguage.GERMAN -> "Node.js-Download fehlgeschlagen"
        AppLanguage.RUSSIAN -> "Не удалось загрузить Node.js"
        AppLanguage.JAPANESE -> "Node.jsのダウンロードに失敗しました"
        AppLanguage.KOREAN -> "Node.js 다운로드 실패"
    }

    val njsProjectDetected: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "检测到 Node.js 项目"
        AppLanguage.ENGLISH -> "Node.js project detected"
        AppLanguage.ARABIC -> "تم اكتشاف مشروع Node.js"
        AppLanguage.PORTUGUESE -> "Projeto Node.js detectado"
        AppLanguage.SPANISH -> "Proyecto Node.js detectado"
        AppLanguage.FRENCH -> "Projet Node.js détecté"
        AppLanguage.GERMAN -> "Node.js-Projekt erkannt"
        AppLanguage.RUSSIAN -> "Обнаружен проект Node.js"
        AppLanguage.JAPANESE -> "Node.jsプロジェクトを検出しました"
        AppLanguage.KOREAN -> "Node.js 프로젝트 감지됨"
    }

    val njsFramework: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "后端框架"
        AppLanguage.ENGLISH -> "Backend Framework"
        AppLanguage.ARABIC -> "إطار الخلفية"
        AppLanguage.PORTUGUESE -> "Framework de Backend"
        AppLanguage.SPANISH -> "Framework de Backend"
        AppLanguage.FRENCH -> "Framework Backend"
        AppLanguage.GERMAN -> "Backend-Framework"
        AppLanguage.RUSSIAN -> "Бэкенд-фреймворк"
        AppLanguage.JAPANESE -> "バックエンドフレームワーク"
        AppLanguage.KOREAN -> "백엔드 프레임워크"
    }

    val njsProjectReady: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Node.js 项目已就绪"
        AppLanguage.ENGLISH -> "Node.js project ready"
        AppLanguage.ARABIC -> "مشروع Node.js جاهز"
        AppLanguage.PORTUGUESE -> "Projeto Node.js pronto"
        AppLanguage.SPANISH -> "Proyecto Node.js listo"
        AppLanguage.FRENCH -> "Projet Node.js prêt"
        AppLanguage.GERMAN -> "Node.js-Projekt bereit"
        AppLanguage.RUSSIAN -> "Проект Node.js готов"
        AppLanguage.JAPANESE -> "Node.jsプロジェクトの準備が完了しました"
        AppLanguage.KOREAN -> "Node.js 프로젝트 준비 완료"
    }

    val btnCreate: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "创建应用"
        AppLanguage.ENGLISH -> "Create App"
        AppLanguage.ARABIC -> "إنشاء تطبيق"
        AppLanguage.PORTUGUESE -> "Criar App"
        AppLanguage.SPANISH -> "Crear App"
        AppLanguage.FRENCH -> "Créer une app"
        AppLanguage.GERMAN -> "App erstellen"
        AppLanguage.RUSSIAN -> "Создать приложение"
        AppLanguage.JAPANESE -> "アプリを作成"
        AppLanguage.KOREAN -> "앱 만들기"
    }

    val btnPreview: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "预览"
        AppLanguage.ENGLISH -> "Preview"
        AppLanguage.ARABIC -> "معاينة"
        AppLanguage.PORTUGUESE -> "Pré-visualizar"
        AppLanguage.SPANISH -> "Vista previa"
        AppLanguage.FRENCH -> "Aperçu"
        AppLanguage.GERMAN -> "Vorschau"
        AppLanguage.RUSSIAN -> "Предпросмотр"
        AppLanguage.JAPANESE -> "プレビュー"
        AppLanguage.KOREAN -> "미리보기"
    }

    val btnExport: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "导出APK"
        AppLanguage.ENGLISH -> "Export APK"
        AppLanguage.ARABIC -> "تصدير APK"
        AppLanguage.PORTUGUESE -> "Exportar APK"
        AppLanguage.SPANISH -> "Exportar APK"
        AppLanguage.FRENCH -> "Exporter l'APK"
        AppLanguage.GERMAN -> "APK exportieren"
        AppLanguage.RUSSIAN -> "Экспорт APK"
        AppLanguage.JAPANESE -> "APKをエクスポート"
        AppLanguage.KOREAN -> "APK 내보내기"
    }

    val btnSave: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "保存"
        AppLanguage.ENGLISH -> "Save"
        AppLanguage.ARABIC -> "حفظ"
        AppLanguage.PORTUGUESE -> "Salvar"
        AppLanguage.SPANISH -> "Guardar"
        AppLanguage.FRENCH -> "Enregistrer"
        AppLanguage.GERMAN -> "Speichern"
        AppLanguage.RUSSIAN -> "Сохранить"
        AppLanguage.JAPANESE -> "保存"
        AppLanguage.KOREAN -> "저장"
    }

    val btnCancel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "取消"
        AppLanguage.ENGLISH -> "Cancel"
        AppLanguage.ARABIC -> "إلغاء"
        AppLanguage.PORTUGUESE -> "Cancelar"
        AppLanguage.SPANISH -> "Cancelar"
        AppLanguage.FRENCH -> "Annuler"
        AppLanguage.GERMAN -> "Abbrechen"
        AppLanguage.RUSSIAN -> "Отмена"
        AppLanguage.JAPANESE -> "キャンセル"
        AppLanguage.KOREAN -> "취소"
    }

    val btnImport: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "导入"
        AppLanguage.ENGLISH -> "Import"
        AppLanguage.ARABIC -> "استيراد"
        AppLanguage.PORTUGUESE -> "Importar"
        AppLanguage.SPANISH -> "Importar"
        AppLanguage.FRENCH -> "Importer"
        AppLanguage.GERMAN -> "Importieren"
        AppLanguage.RUSSIAN -> "Импорт"
        AppLanguage.JAPANESE -> "インポート"
        AppLanguage.KOREAN -> "가져오기"
    }

    val btnDelete: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "删除"
        AppLanguage.ENGLISH -> "Delete"
        AppLanguage.ARABIC -> "حذف"
        AppLanguage.PORTUGUESE -> "Excluir"
        AppLanguage.SPANISH -> "Eliminar"
        AppLanguage.FRENCH -> "Supprimer"
        AppLanguage.GERMAN -> "Löschen"
        AppLanguage.RUSSIAN -> "Удалить"
        AppLanguage.JAPANESE -> "削除"
        AppLanguage.KOREAN -> "삭제"
    }

    val btnEdit: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "编辑"
        AppLanguage.ENGLISH -> "Edit"
        AppLanguage.ARABIC -> "تعديل"
        AppLanguage.PORTUGUESE -> "Editar"
        AppLanguage.SPANISH -> "Editar"
        AppLanguage.FRENCH -> "Modifier"
        AppLanguage.GERMAN -> "Bearbeiten"
        AppLanguage.RUSSIAN -> "Изменить"
        AppLanguage.JAPANESE -> "編集"
        AppLanguage.KOREAN -> "편집"
    }

    val editCoreConfig: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "编辑核心配置"
        AppLanguage.ENGLISH -> "Edit Core Config"
        AppLanguage.ARABIC -> "تعديل الإعدادات الأساسية"
        AppLanguage.PORTUGUESE -> "Editar Configuração Principal"
        AppLanguage.SPANISH -> "Editar Configuración Principal"
        AppLanguage.FRENCH -> "Modifier la configuration principale"
        AppLanguage.GERMAN -> "Hauptkonfiguration bearbeiten"
        AppLanguage.RUSSIAN -> "Изменить основную конфигурацию"
        AppLanguage.JAPANESE -> "コア設定を編集"
        AppLanguage.KOREAN -> "핵심 구성 편집"
    }

    val editCommonConfig: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "编辑通用配置"
        AppLanguage.ENGLISH -> "Edit Common Config"
        AppLanguage.ARABIC -> "تعديل الإعدادات العامة"
        AppLanguage.PORTUGUESE -> "Editar Configuração Comum"
        AppLanguage.SPANISH -> "Editar Configuración Común"
        AppLanguage.FRENCH -> "Modifier la configuration commune"
        AppLanguage.GERMAN -> "Allgemeine Konfiguration bearbeiten"
        AppLanguage.RUSSIAN -> "Изменить общую конфигурацию"
        AppLanguage.JAPANESE -> "共通設定を編集"
        AppLanguage.KOREAN -> "공통 구성 편집"
    }

    val btnShortcut: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "创建快捷方式"
        AppLanguage.ENGLISH -> "Create Shortcut"
        AppLanguage.ARABIC -> "إنشاء اختصار"
        AppLanguage.PORTUGUESE -> "Criar Atalho"
        AppLanguage.SPANISH -> "Crear Acceso Directo"
        AppLanguage.FRENCH -> "Créer un raccourci"
        AppLanguage.GERMAN -> "Verknüpfung erstellen"
        AppLanguage.RUSSIAN -> "Создать ярлык"
        AppLanguage.JAPANESE -> "ショートカットを作成"
        AppLanguage.KOREAN -> "바로가기 만들기"
    }

    val btnConfirm: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "确认"
        AppLanguage.ENGLISH -> "Confirm"
        AppLanguage.ARABIC -> "تأكيد"
        AppLanguage.PORTUGUESE -> "Confirmar"
        AppLanguage.SPANISH -> "Confirmar"
        AppLanguage.FRENCH -> "Confirmer"
        AppLanguage.GERMAN -> "Bestätigen"
        AppLanguage.RUSSIAN -> "Подтвердить"
        AppLanguage.JAPANESE -> "確認"
        AppLanguage.KOREAN -> "확인"
    }

    val btnOk: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "确定"
        AppLanguage.ENGLISH -> "OK"
        AppLanguage.ARABIC -> "موافق"
        AppLanguage.PORTUGUESE -> "OK"
        AppLanguage.SPANISH -> "Aceptar"
        AppLanguage.FRENCH -> "OK"
        AppLanguage.GERMAN -> "OK"
        AppLanguage.RUSSIAN -> "ОК"
        AppLanguage.JAPANESE -> "OK"
        AppLanguage.KOREAN -> "확인"
    }

    val btnRetry: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "重试"
        AppLanguage.ENGLISH -> "Retry"
        AppLanguage.ARABIC -> "إعادة المحاولة"
        AppLanguage.PORTUGUESE -> "Tentar novamente"
        AppLanguage.SPANISH -> "Reintentar"
        AppLanguage.FRENCH -> "Réessayer"
        AppLanguage.GERMAN -> "Erneut versuchen"
        AppLanguage.RUSSIAN -> "Повторить"
        AppLanguage.JAPANESE -> "再試行"
        AppLanguage.KOREAN -> "재시도"
    }

    val btnStartBuild: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "构建"
        AppLanguage.ENGLISH -> "Build"
        AppLanguage.ARABIC -> "بناء"
        AppLanguage.PORTUGUESE -> "Compilar"
        AppLanguage.SPANISH -> "Compilar"
        AppLanguage.FRENCH -> "Compiler"
        AppLanguage.GERMAN -> "Erstellen"
        AppLanguage.RUSSIAN -> "Сборка"
        AppLanguage.JAPANESE -> "ビルド"
        AppLanguage.KOREAN -> "빌드"
    }

    val btnReset: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "重置"
        AppLanguage.ENGLISH -> "Reset"
        AppLanguage.ARABIC -> "إعادة تعيين"
        AppLanguage.PORTUGUESE -> "Redefinir"
        AppLanguage.SPANISH -> "Restablecer"
        AppLanguage.FRENCH -> "Réinitialiser"
        AppLanguage.GERMAN -> "Zurücksetzen"
        AppLanguage.RUSSIAN -> "Сбросить"
        AppLanguage.JAPANESE -> "リセット"
        AppLanguage.KOREAN -> "재설정"
    }

    val btnClearCache: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "清理缓存"
        AppLanguage.ENGLISH -> "Clear Cache"
        AppLanguage.ARABIC -> "مسح ذاكرة التخزين المؤقت"
        AppLanguage.PORTUGUESE -> "Limpar Cache"
        AppLanguage.SPANISH -> "Borrar Caché"
        AppLanguage.FRENCH -> "Vider le cache"
        AppLanguage.GERMAN -> "Cache leeren"
        AppLanguage.RUSSIAN -> "Очистить кэш"
        AppLanguage.JAPANESE -> "キャッシュをクリア"
        AppLanguage.KOREAN -> "캐시 지우기"
    }

    val labelAppName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "应用名称"
        AppLanguage.ENGLISH -> "App Name"
        AppLanguage.ARABIC -> "اسم التطبيق"
        AppLanguage.PORTUGUESE -> "Nome do App"
        AppLanguage.SPANISH -> "Nombre de la App"
        AppLanguage.FRENCH -> "Nom de l'app"
        AppLanguage.GERMAN -> "App-Name"
        AppLanguage.RUSSIAN -> "Имя приложения"
        AppLanguage.JAPANESE -> "アプリ名"
        AppLanguage.KOREAN -> "앱 이름"
    }

    val labelUrl: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "网站地址"
        AppLanguage.ENGLISH -> "Website URL"
        AppLanguage.ARABIC -> "عنوان الموقع"
        AppLanguage.PORTUGUESE -> "URL do Site"
        AppLanguage.SPANISH -> "URL del Sitio"
        AppLanguage.FRENCH -> "URL du site"
        AppLanguage.GERMAN -> "Website-URL"
        AppLanguage.RUSSIAN -> "URL сайта"
        AppLanguage.JAPANESE -> "ウェブサイトURL"
        AppLanguage.KOREAN -> "웹사이트 URL"
    }

    val labelIcon: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "应用图标"
        AppLanguage.ENGLISH -> "App Icon"
        AppLanguage.ARABIC -> "أيقونة التطبيق"
        AppLanguage.PORTUGUESE -> "Ícone do App"
        AppLanguage.SPANISH -> "Icono de la App"
        AppLanguage.FRENCH -> "Icône de l'app"
        AppLanguage.GERMAN -> "App-Icon"
        AppLanguage.RUSSIAN -> "Иконка приложения"
        AppLanguage.JAPANESE -> "アプリアイコン"
        AppLanguage.KOREAN -> "앱 아이콘"
    }

    val labelBasicInfo: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "基本信息"
        AppLanguage.ENGLISH -> "Basic Info"
        AppLanguage.ARABIC -> "المعلومات الأساسية"
        AppLanguage.PORTUGUESE -> "Informações Básicas"
        AppLanguage.SPANISH -> "Información Básica"
        AppLanguage.FRENCH -> "Informations de base"
        AppLanguage.GERMAN -> "Grundinformationen"
        AppLanguage.RUSSIAN -> "Основная информация"
        AppLanguage.JAPANESE -> "基本情報"
        AppLanguage.KOREAN -> "기본 정보"
    }

    val labelAdvancedConfig: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "高级配置"
        AppLanguage.ENGLISH -> "Advanced Config"
        AppLanguage.ARABIC -> "الإعدادات المتقدمة"
        AppLanguage.PORTUGUESE -> "Configuração Avançada"
        AppLanguage.SPANISH -> "Configuración Avanzada"
        AppLanguage.FRENCH -> "Configuration avancée"
        AppLanguage.GERMAN -> "Erweiterte Konfiguration"
        AppLanguage.RUSSIAN -> "Расширенная конфигурация"
        AppLanguage.JAPANESE -> "詳細設定"
        AppLanguage.KOREAN -> "고급 구성"
    }

    val labelDisplaySettings: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "显示设置"
        AppLanguage.ENGLISH -> "Display Settings"
        AppLanguage.ARABIC -> "إعدادات العرض"
        AppLanguage.PORTUGUESE -> "Configurações de Exibição"
        AppLanguage.SPANISH -> "Ajustes de Pantalla"
        AppLanguage.FRENCH -> "Paramètres d'affichage"
        AppLanguage.GERMAN -> "Anzeigeeinstellungen"
        AppLanguage.RUSSIAN -> "Настройки отображения"
        AppLanguage.JAPANESE -> "表示設定"
        AppLanguage.KOREAN -> "디스플레이 설정"
    }

    val labelAppInfo: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "应用信息"
        AppLanguage.ENGLISH -> "App Info"
        AppLanguage.ARABIC -> "معلومات التطبيق"
        AppLanguage.PORTUGUESE -> "Informações do App"
        AppLanguage.SPANISH -> "Información de la App"
        AppLanguage.FRENCH -> "Infos de l'app"
        AppLanguage.GERMAN -> "App-Info"
        AppLanguage.RUSSIAN -> "Информация о приложении"
        AppLanguage.JAPANESE -> "アプリ情報"
        AppLanguage.KOREAN -> "앱 정보"
    }

    val msgNoApps: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "暂无应用"
        AppLanguage.ENGLISH -> "No apps yet"
        AppLanguage.ARABIC -> "لا توجد تطبيقات بعد"
        AppLanguage.PORTUGUESE -> "Nenhum app ainda"
        AppLanguage.SPANISH -> "Aún no hay apps"
        AppLanguage.FRENCH -> "Aucune application pour le moment"
        AppLanguage.GERMAN -> "Noch keine Apps"
        AppLanguage.RUSSIAN -> "Приложений пока нет"
        AppLanguage.JAPANESE -> "まだアプリがありません"
        AppLanguage.KOREAN -> "아직 앱이 없습니다"
    }

    val msgLanguageChanged: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "语言已更改"
        AppLanguage.ENGLISH -> "Language changed"
        AppLanguage.ARABIC -> "تم تغيير اللغة"
        AppLanguage.PORTUGUESE -> "Idioma alterado"
        AppLanguage.SPANISH -> "Idioma cambiado"
        AppLanguage.FRENCH -> "Langue modifiée"
        AppLanguage.GERMAN -> "Sprache geändert"
        AppLanguage.RUSSIAN -> "Язык изменён"
        AppLanguage.JAPANESE -> "言語が変更されました"
        AppLanguage.KOREAN -> "언어가 변경되었습니다"
    }

    val msgImportSuccess: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "导入成功"
        AppLanguage.ENGLISH -> "Import successful"
        AppLanguage.ARABIC -> "تم الاستيراد بنجاح"
        AppLanguage.PORTUGUESE -> "Importação bem-sucedida"
        AppLanguage.SPANISH -> "Importación exitosa"
        AppLanguage.FRENCH -> "Importation réussie"
        AppLanguage.GERMAN -> "Import erfolgreich"
        AppLanguage.RUSSIAN -> "Импорт выполнен"
        AppLanguage.JAPANESE -> "インポートに成功しました"
        AppLanguage.KOREAN -> "가져오기 성공"
    }

    fun moduleImportSuccess(name: String): String = when (Strings.lang) {
        AppLanguage.CHINESE -> "模块 $name 导入成功"
        AppLanguage.ENGLISH -> "Module $name imported successfully"
        AppLanguage.ARABIC -> "تم استيراد الوحدة $name بنجاح"
        AppLanguage.PORTUGUESE -> "Módulo $name importado com sucesso"
        AppLanguage.SPANISH -> "Módulo $name importado con éxito"
        AppLanguage.FRENCH -> "Module $name importé avec succès"
        AppLanguage.GERMAN -> "Modul $name erfolgreich importiert"
        AppLanguage.RUSSIAN -> "Модуль $name успешно импортирован"
        AppLanguage.JAPANESE -> "モジュール $name のインポートに成功しました"
        AppLanguage.KOREAN -> "모듈 $name 가져오기 성공"
    }

    fun moduleImportFailed(message: String): String = when (Strings.lang) {
        AppLanguage.CHINESE -> "模块导入失败: $message"
        AppLanguage.ENGLISH -> "Module import failed: $message"
        AppLanguage.ARABIC -> "فشل استيراد الوحدة: $message"
        AppLanguage.PORTUGUESE -> "Falha ao importar o módulo: $message"
        AppLanguage.SPANISH -> "Error al importar el módulo: $message"
        AppLanguage.FRENCH -> "Échec de l'importation du module : $message"
        AppLanguage.GERMAN -> "Modulimport fehlgeschlagen: $message"
        AppLanguage.RUSSIAN -> "Ошибка импорта модуля: $message"
        AppLanguage.JAPANESE -> "モジュールのインポートに失敗: $message"
        AppLanguage.KOREAN -> "모듈 가져오기 실패: $message"
    }

    val languageSettings: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "语言"
        AppLanguage.ENGLISH -> "Language"
        AppLanguage.ARABIC -> "اللغة"
        AppLanguage.PORTUGUESE -> "Idioma"
        AppLanguage.SPANISH -> "Idioma"
        AppLanguage.FRENCH -> "Langue"
        AppLanguage.GERMAN -> "Sprache"
        AppLanguage.RUSSIAN -> "Язык"
        AppLanguage.JAPANESE -> "言語"
        AppLanguage.KOREAN -> "언어"
    }

    val sslError: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "SSL 安全错误"
        AppLanguage.ENGLISH -> "SSL Security Error"
        AppLanguage.ARABIC -> "خطأ أمان SSL"
        AppLanguage.PORTUGUESE -> "Erro de segurança SSL"
        AppLanguage.SPANISH -> "Error de seguridad SSL"
        AppLanguage.FRENCH -> "Erreur de sécurité SSL"
        AppLanguage.GERMAN -> "SSL-Sicherheitsfehler"
        AppLanguage.RUSSIAN -> "Ошибка безопасности SSL"
        AppLanguage.JAPANESE -> "SSLセキュリティエラー"
        AppLanguage.KOREAN -> "SSL 보안 오류"
    }

    val msgCopied: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已复制"
        AppLanguage.ENGLISH -> "Copied"
        AppLanguage.ARABIC -> "تم النسخ"
        AppLanguage.PORTUGUESE -> "Copiado"
        AppLanguage.SPANISH -> "Copiado"
        AppLanguage.FRENCH -> "Copié"
        AppLanguage.GERMAN -> "Kopiert"
        AppLanguage.RUSSIAN -> "Скопировано"
        AppLanguage.JAPANESE -> "コピーしました"
        AppLanguage.KOREAN -> "복사됨"
    }

    val menuRuntimeDeps: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "运行时管理"
        AppLanguage.ENGLISH -> "Runtime Manager"
        AppLanguage.ARABIC -> "مدير وقت التشغيل"
        AppLanguage.PORTUGUESE -> "Gerenciador de Runtime"
        AppLanguage.SPANISH -> "Gestor de Runtime"
        AppLanguage.FRENCH -> "Gestionnaire de runtime"
        AppLanguage.GERMAN -> "Runtime-Manager"
        AppLanguage.RUSSIAN -> "Менеджер среды выполнения"
        AppLanguage.JAPANESE -> "ランタイムマネージャー"
        AppLanguage.KOREAN -> "런타임 관리자"
    }

    val menuPortManager: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "端口管理"
        AppLanguage.ENGLISH -> "Port Manager"
        AppLanguage.ARABIC -> "مدير المنافذ"
        AppLanguage.PORTUGUESE -> "Gerenciador de Portas"
        AppLanguage.SPANISH -> "Gestor de Puertos"
        AppLanguage.FRENCH -> "Gestionnaire de ports"
        AppLanguage.GERMAN -> "Port-Manager"
        AppLanguage.RUSSIAN -> "Менеджер портов"
        AppLanguage.JAPANESE -> "ポートマネージャー"
        AppLanguage.KOREAN -> "포트 관리자"
    }

    val portManagerTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "端口管理"
        AppLanguage.ENGLISH -> "Port Manager"
        AppLanguage.ARABIC -> "مدير المنافذ"
        AppLanguage.PORTUGUESE -> "Gerenciador de Portas"
        AppLanguage.SPANISH -> "Gestor de Puertos"
        AppLanguage.FRENCH -> "Gestionnaire de ports"
        AppLanguage.GERMAN -> "Port-Manager"
        AppLanguage.RUSSIAN -> "Менеджер портов"
        AppLanguage.JAPANESE -> "ポートマネージャー"
        AppLanguage.KOREAN -> "포트 관리자"
    }

    val portManagerRunningServices: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "运行中的服务"
        AppLanguage.ENGLISH -> "Running Services"
        AppLanguage.ARABIC -> "الخدمات قيد التشغيل"
        AppLanguage.PORTUGUESE -> "Serviços em Execução"
        AppLanguage.SPANISH -> "Servicios en Ejecución"
        AppLanguage.FRENCH -> "Services en cours d'exécution"
        AppLanguage.GERMAN -> "Laufende Dienste"
        AppLanguage.RUSSIAN -> "Запущенные службы"
        AppLanguage.JAPANESE -> "実行中のサービス"
        AppLanguage.KOREAN -> "실행 중인 서비스"
    }

    val portManagerNoServices: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "没有运行中的服务"
        AppLanguage.ENGLISH -> "No running services"
        AppLanguage.ARABIC -> "لا توجد خدمات قيد التشغيل"
        AppLanguage.PORTUGUESE -> "Nenhum serviço em execução"
        AppLanguage.SPANISH -> "No hay servicios en ejecución"
        AppLanguage.FRENCH -> "Aucun service en cours d'exécution"
        AppLanguage.GERMAN -> "Keine laufenden Dienste"
        AppLanguage.RUSSIAN -> "Нет запущенных служб"
        AppLanguage.JAPANESE -> "実行中のサービスはありません"
        AppLanguage.KOREAN -> "실행 중인 서비스가 없습니다"
    }

    val portManagerAllReleased: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "所有端口均已释放"
        AppLanguage.ENGLISH -> "All ports released"
        AppLanguage.ARABIC -> "تم تحرير جميع المنافذ"
        AppLanguage.PORTUGUESE -> "Todos os portas liberados"
        AppLanguage.SPANISH -> "Todos los puertos liberados"
        AppLanguage.FRENCH -> "Tous les ports libérés"
        AppLanguage.GERMAN -> "Alle Ports freigegeben"
        AppLanguage.RUSSIAN -> "Все порты освобождены"
        AppLanguage.JAPANESE -> "すべてのポートを解放しました"
        AppLanguage.KOREAN -> "모든 포트가 해제되었습니다"
    }

    val portManagerKillAll: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "终止所有"
        AppLanguage.ENGLISH -> "Kill All"
        AppLanguage.ARABIC -> "إنهاء الكل"
        AppLanguage.PORTUGUESE -> "Encerrar Tudo"
        AppLanguage.SPANISH -> "Terminar Todo"
        AppLanguage.FRENCH -> "Tout arrêter"
        AppLanguage.GERMAN -> "Alle beenden"
        AppLanguage.RUSSIAN -> "Завершить все"
        AppLanguage.JAPANESE -> "すべて終了"
        AppLanguage.KOREAN -> "모두 종료"
    }

    val portManagerKillAllConfirm: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "确定要终止所有运行中的服务吗？"
        AppLanguage.ENGLISH -> "Kill all running services?"
        AppLanguage.ARABIC -> "هل تريد إنهاء جميع الخدمات قيد التشغيل؟"
        AppLanguage.PORTUGUESE -> "Encerrar todos os serviços em execução?"
        AppLanguage.SPANISH -> "¿Terminar todos los servicios en ejecución?"
        AppLanguage.FRENCH -> "Arrêter tous les services en cours d'exécution ?"
        AppLanguage.GERMAN -> "Alle laufenden Dienste beenden?"
        AppLanguage.RUSSIAN -> "Завершить все запущенные службы?"
        AppLanguage.JAPANESE -> "実行中のすべてのサービスを終了しますか？"
        AppLanguage.KOREAN -> "실행 중인 모든 서비스를 종료하시겠습니까?"
    }

    val portManagerKillService: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "终止服务"
        AppLanguage.ENGLISH -> "Kill Service"
        AppLanguage.ARABIC -> "إنهاء الخدمة"
        AppLanguage.PORTUGUESE -> "Encerrar Serviço"
        AppLanguage.SPANISH -> "Terminar Servicio"
        AppLanguage.FRENCH -> "Arrêter le service"
        AppLanguage.GERMAN -> "Dienst beenden"
        AppLanguage.RUSSIAN -> "Завершить службу"
        AppLanguage.JAPANESE -> "サービスを終了"
        AppLanguage.KOREAN -> "서비스 종료"
    }

    val portManagerOpen: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "打开"
        AppLanguage.ENGLISH -> "Open"
        AppLanguage.ARABIC -> "فتح"
        AppLanguage.PORTUGUESE -> "Abrir"
        AppLanguage.SPANISH -> "Abrir"
        AppLanguage.FRENCH -> "Ouvrir"
        AppLanguage.GERMAN -> "Öffnen"
        AppLanguage.RUSSIAN -> "Открыть"
        AppLanguage.JAPANESE -> "開く"
        AppLanguage.KOREAN -> "열기"
    }

    val portManagerKill: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "终止"
        AppLanguage.ENGLISH -> "Kill"
        AppLanguage.ARABIC -> "إنهاء"
        AppLanguage.PORTUGUESE -> "Encerrar"
        AppLanguage.SPANISH -> "Terminar"
        AppLanguage.FRENCH -> "Arrêter"
        AppLanguage.GERMAN -> "Beenden"
        AppLanguage.RUSSIAN -> "Завершить"
        AppLanguage.JAPANESE -> "終了"
        AppLanguage.KOREAN -> "종료"
    }

    val portManagerPort: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "端口"
        AppLanguage.ENGLISH -> "Port"
        AppLanguage.ARABIC -> "المنفذ"
        AppLanguage.PORTUGUESE -> "Porta"
        AppLanguage.SPANISH -> "Puerto"
        AppLanguage.FRENCH -> "Port"
        AppLanguage.GERMAN -> "Port"
        AppLanguage.RUSSIAN -> "Порт"
        AppLanguage.JAPANESE -> "ポート"
        AppLanguage.KOREAN -> "포트"
    }

    val portManagerType: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "类型"
        AppLanguage.ENGLISH -> "Type"
        AppLanguage.ARABIC -> "النوع"
        AppLanguage.PORTUGUESE -> "Tipo"
        AppLanguage.SPANISH -> "Tipo"
        AppLanguage.FRENCH -> "Type"
        AppLanguage.GERMAN -> "Typ"
        AppLanguage.RUSSIAN -> "Тип"
        AppLanguage.JAPANESE -> "タイプ"
        AppLanguage.KOREAN -> "유형"
    }

    val portManagerProject: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "项目"
        AppLanguage.ENGLISH -> "Project"
        AppLanguage.ARABIC -> "المشروع"
        AppLanguage.PORTUGUESE -> "Projeto"
        AppLanguage.SPANISH -> "Proyecto"
        AppLanguage.FRENCH -> "Projet"
        AppLanguage.GERMAN -> "Projekt"
        AppLanguage.RUSSIAN -> "Проект"
        AppLanguage.JAPANESE -> "プロジェクト"
        AppLanguage.KOREAN -> "프로젝트"
    }

    val portManagerStatus: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "状态"
        AppLanguage.ENGLISH -> "Status"
        AppLanguage.ARABIC -> "الحالة"
        AppLanguage.PORTUGUESE -> "Estado"
        AppLanguage.SPANISH -> "Estado"
        AppLanguage.FRENCH -> "Statut"
        AppLanguage.GERMAN -> "Status"
        AppLanguage.RUSSIAN -> "Статус"
        AppLanguage.JAPANESE -> "ステータス"
        AppLanguage.KOREAN -> "상태"
    }

    val portManagerResponding: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "响应中"
        AppLanguage.ENGLISH -> "Responding"
        AppLanguage.ARABIC -> "يستجيب"
        AppLanguage.PORTUGUESE -> "Respondendo"
        AppLanguage.SPANISH -> "Respondiendo"
        AppLanguage.FRENCH -> "Répond"
        AppLanguage.GERMAN -> "Antwortet"
        AppLanguage.RUSSIAN -> "Отвечает"
        AppLanguage.JAPANESE -> "応答中"
        AppLanguage.KOREAN -> "응답 중"
    }

    val portManagerNotResponding: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "无响应"
        AppLanguage.ENGLISH -> "Not Responding"
        AppLanguage.ARABIC -> "لا يستجيب"
        AppLanguage.PORTUGUESE -> "Sem Resposta"
        AppLanguage.SPANISH -> "No Responde"
        AppLanguage.FRENCH -> "Ne répond pas"
        AppLanguage.GERMAN -> "Antwortet nicht"
        AppLanguage.RUSSIAN -> "Не отвечает"
        AppLanguage.JAPANESE -> "応答なし"
        AppLanguage.KOREAN -> "응답 없음"
    }

    val portManagerUnknown: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "未知"
        AppLanguage.ENGLISH -> "Unknown"
        AppLanguage.ARABIC -> "غير معروف"
        AppLanguage.PORTUGUESE -> "Desconhecido"
        AppLanguage.SPANISH -> "Desconocido"
        AppLanguage.FRENCH -> "Inconnu"
        AppLanguage.GERMAN -> "Unbekannt"
        AppLanguage.RUSSIAN -> "Неизвестно"
        AppLanguage.JAPANESE -> "不明"
        AppLanguage.KOREAN -> "알 수 없음"
    }

    val portManagerServiceKilled: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已终止端口 %d 的服务"
        AppLanguage.ENGLISH -> "Killed service on port %d"
        AppLanguage.ARABIC -> "تم إنهاء الخدمة على المنفذ %d"
        AppLanguage.PORTUGUESE -> "Serviço na porta %d encerrado"
        AppLanguage.SPANISH -> "Servicio terminado en el puerto %d"
        AppLanguage.FRENCH -> "Service arrêté sur le port %d"
        AppLanguage.GERMAN -> "Dienst auf Port %d beendet"
        AppLanguage.RUSSIAN -> "Служба на порту %d завершена"
        AppLanguage.JAPANESE -> "ポート %d のサービスを終了しました"
        AppLanguage.KOREAN -> "포트 %d의 서비스가 종료되었습니다"
    }

    val portManagerAllKilled: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已终止 %d 个服务"
        AppLanguage.ENGLISH -> "Killed %d services"
        AppLanguage.ARABIC -> "تم إنهاء %d خدمات"
        AppLanguage.PORTUGUESE -> "%d serviços encerrados"
        AppLanguage.SPANISH -> "%d servicios terminados"
        AppLanguage.FRENCH -> "%d services arrêtés"
        AppLanguage.GERMAN -> "%d Dienste beendet"
        AppLanguage.RUSSIAN -> "Завершено служб: %d"
        AppLanguage.JAPANESE -> "%d個のサービスを終了しました"
        AppLanguage.KOREAN -> "%d개 서비스 종료됨"
    }

    val portManagerKillFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "终止失败，请重试"
        AppLanguage.ENGLISH -> "Kill failed, please retry"
        AppLanguage.ARABIC -> "فشل الإنهاء، يرجى المحاولة مرة أخرى"
        AppLanguage.PORTUGUESE -> "Falha ao encerrar, tente novamente"
        AppLanguage.SPANISH -> "Error al terminar, inténtalo de nuevo"
        AppLanguage.FRENCH -> "Échec de l'arrêt, veuillez réessayer"
        AppLanguage.GERMAN -> "Beenden fehlgeschlagen, bitte erneut versuchen"
        AppLanguage.RUSSIAN -> "Не удалось завершить, повторите"
        AppLanguage.JAPANESE -> "終了に失敗しました。再試行してください"
        AppLanguage.KOREAN -> "종료 실패, 재시도하세요"
    }

    val portManagerUptime: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "运行时长"
        AppLanguage.ENGLISH -> "Uptime"
        AppLanguage.ARABIC -> "وقت التشغيل"
        AppLanguage.PORTUGUESE -> "Tempo de Atividade"
        AppLanguage.SPANISH -> "Tiempo de Actividad"
        AppLanguage.FRENCH -> "Disponibilité"
        AppLanguage.GERMAN -> "Betriebszeit"
        AppLanguage.RUSSIAN -> "Время работы"
        AppLanguage.JAPANESE -> "稼働時間"
        AppLanguage.KOREAN -> "가동 시간"
    }

    val portManagerLatency: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "延迟"
        AppLanguage.ENGLISH -> "Latency"
        AppLanguage.ARABIC -> "زمن الاستجابة"
        AppLanguage.PORTUGUESE -> "Latência"
        AppLanguage.SPANISH -> "Latencia"
        AppLanguage.FRENCH -> "Latence"
        AppLanguage.GERMAN -> "Latenz"
        AppLanguage.RUSSIAN -> "Задержка"
        AppLanguage.JAPANESE -> "レイテンシ"
        AppLanguage.KOREAN -> "지연 시간"
    }

    val portManagerPortRanges: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "端口范围"
        AppLanguage.ENGLISH -> "Port Ranges"
        AppLanguage.ARABIC -> "نطاقات المنافذ"
        AppLanguage.PORTUGUESE -> "Faixas de Portas"
        AppLanguage.SPANISH -> "Rangos de Puertos"
        AppLanguage.FRENCH -> "Plages de ports"
        AppLanguage.GERMAN -> "Port-Bereiche"
        AppLanguage.RUSSIAN -> "Диапазоны портов"
        AppLanguage.JAPANESE -> "ポート範囲"
        AppLanguage.KOREAN -> "포트 범위"
    }

    val portManagerAutoRefresh: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自动刷新"
        AppLanguage.ENGLISH -> "Auto Refresh"
        AppLanguage.ARABIC -> "تحديث تلقائي"
        AppLanguage.PORTUGUESE -> "Atualização Automática"
        AppLanguage.SPANISH -> "Actualización Automática"
        AppLanguage.FRENCH -> "Actualisation automatique"
        AppLanguage.GERMAN -> "Automatische Aktualisierung"
        AppLanguage.RUSSIAN -> "Автообновление"
        AppLanguage.JAPANESE -> "自動更新"
        AppLanguage.KOREAN -> "자동 새로고침"
    }

    val portManagerKillConfirmSingle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "确定要终止此服务吗？"
        AppLanguage.ENGLISH -> "Kill this service?"
        AppLanguage.ARABIC -> "هل تريد إنهاء هذه الخدمة؟"
        AppLanguage.PORTUGUESE -> "Encerrar este serviço?"
        AppLanguage.SPANISH -> "¿Terminar este servicio?"
        AppLanguage.FRENCH -> "Arrêter ce service ?"
        AppLanguage.GERMAN -> "Diesen Dienst beenden?"
        AppLanguage.RUSSIAN -> "Завершить эту службу?"
        AppLanguage.JAPANESE -> "このサービスを終了しますか？"
        AppLanguage.KOREAN -> "이 서비스를 종료하시겠습니까?"
    }

    val portManagerProcess: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "进程"
        AppLanguage.ENGLISH -> "Process"
        AppLanguage.ARABIC -> "العملية"
        AppLanguage.PORTUGUESE -> "Processo"
        AppLanguage.SPANISH -> "Proceso"
        AppLanguage.FRENCH -> "Processus"
        AppLanguage.GERMAN -> "Prozess"
        AppLanguage.RUSSIAN -> "Процесс"
        AppLanguage.JAPANESE -> "プロセス"
        AppLanguage.KOREAN -> "프로세스"
    }

    val portManagerTabThisApp: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "本应用"
        AppLanguage.ENGLISH -> "This App"
        AppLanguage.ARABIC -> "هذا التطبيق"
        AppLanguage.PORTUGUESE -> "Este App"
        AppLanguage.SPANISH -> "Esta App"
        AppLanguage.FRENCH -> "Cette app"
        AppLanguage.GERMAN -> "Diese App"
        AppLanguage.RUSSIAN -> "Это приложение"
        AppLanguage.JAPANESE -> "このアプリ"
        AppLanguage.KOREAN -> "이 앱"
    }

    val portManagerTabAllApps: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "全部 Web2App 应用"
        AppLanguage.ENGLISH -> "All Web2App Apps"
        AppLanguage.ARABIC -> "كل تطبيقات Web2App"
        AppLanguage.PORTUGUESE -> "Todos os Apps Web2App"
        AppLanguage.SPANISH -> "Todas las Apps Web2App"
        AppLanguage.FRENCH -> "Toutes les apps Web2App"
        AppLanguage.GERMAN -> "Alle Web2App-Apps"
        AppLanguage.RUSSIAN -> "Все приложения Web2App"
        AppLanguage.JAPANESE -> "すべてのWeb2Appアプリ"
        AppLanguage.KOREAN -> "모든 Web2App 앱"
    }

    val portManagerNoWtaApps: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "未发现其他 Web2App 应用"
        AppLanguage.ENGLISH -> "No other Web2App apps found"
        AppLanguage.ARABIC -> "لم يتم العثور على تطبيقات Web2App أخرى"
        AppLanguage.PORTUGUESE -> "Nenhum outro app Web2App encontrado"
        AppLanguage.SPANISH -> "No se encontraron otras apps Web2App"
        AppLanguage.FRENCH -> "Aucune autre app Web2App trouvée"
        AppLanguage.GERMAN -> "Keine weiteren Web2App-Apps gefunden"
        AppLanguage.RUSSIAN -> "Другие приложения Web2App не найдены"
        AppLanguage.JAPANESE -> "他のWeb2Appアプリが見つかりません"
        AppLanguage.KOREAN -> "다른 Web2App 앱을 찾을 수 없습니다"
    }

    val portManagerNoWtaAppsHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "由本工具构建并安装的应用会自动出现在这里"
        AppLanguage.ENGLISH -> "Apps built and installed via this tool appear here"
        AppLanguage.ARABIC -> "التطبيقات المبنية والمثبتة عبر هذه الأداة تظهر هنا"
        AppLanguage.PORTUGUESE -> "Apps construídos e instalados via esta ferramenta aparecem aqui"
        AppLanguage.SPANISH -> "Las apps construidas e instaladas con esta herramienta aparecen aquí"
        AppLanguage.FRENCH -> "Les apps construites et installées via cet outil apparaissent ici"
        AppLanguage.GERMAN -> "Mit diesem Tool erstellte und installierte Apps erscheinen hier"
        AppLanguage.RUSSIAN -> "Приложения, созданные и установленные этим инструментом, появляются здесь"
        AppLanguage.JAPANESE -> "このツールでビルド・インストールしたアプリがここに表示されます"
        AppLanguage.KOREAN -> "이 도구로 빌드 및 설치된 앱이 여기에 표시됩니다"
    }

    val portManagerWtaAppNoPorts: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "当前未占用任何端口"
        AppLanguage.ENGLISH -> "Not holding any ports"
        AppLanguage.ARABIC -> "لا يحتجز أي منفذ"
        AppLanguage.PORTUGUESE -> "Não ocupa nenhuma porta"
        AppLanguage.SPANISH -> "No ocupa ningún puerto"
        AppLanguage.FRENCH -> "N'occupe aucun port"
        AppLanguage.GERMAN -> "Belegt keine Ports"
        AppLanguage.RUSSIAN -> "Не занимает портов"
        AppLanguage.JAPANESE -> "ポートを占有していません"
        AppLanguage.KOREAN -> "포트를 점유하지 않습니다"
    }

    val portManagerWtaAppOffline: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "未响应（应用未运行或版本过旧）"
        AppLanguage.ENGLISH -> "Not responding (app not running or older build)"
        AppLanguage.ARABIC -> "لا يستجيب (التطبيق غير قيد التشغيل أو إصدار قديم)"
        AppLanguage.PORTUGUESE -> "Sem resposta (app não em execução ou build antigo)"
        AppLanguage.SPANISH -> "No responde (app no en ejecución o build antiguo)"
        AppLanguage.FRENCH -> "Ne répond pas (app non lancée ou build ancien)"
        AppLanguage.GERMAN -> "Antwortet nicht (App läuft nicht oder älterer Build)"
        AppLanguage.RUSSIAN -> "Не отвечает (приложение не запущено или старая сборка)"
        AppLanguage.JAPANESE -> "応答なし（アプリが実行されていないか古いビルド）"
        AppLanguage.KOREAN -> "응답 없음 (앱이 실행 중이 아니거나 이전 빌드)"
    }

    val portManagerWtaAppPortsCount: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "%d 个端口"
        AppLanguage.ENGLISH -> "%d ports"
        AppLanguage.ARABIC -> "%d منافذ"
        AppLanguage.PORTUGUESE -> "%d portas"
        AppLanguage.SPANISH -> "%d puertos"
        AppLanguage.FRENCH -> "%d ports"
        AppLanguage.GERMAN -> "%d Ports"
        AppLanguage.RUSSIAN -> "%d портов"
        AppLanguage.JAPANESE -> "%d ポート"
        AppLanguage.KOREAN -> "%d개 포트"
    }

    val portManagerReleaseRemoteSuccess: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已请求 %s 释放端口 %d"
        AppLanguage.ENGLISH -> "Requested %s to release port %d"
        AppLanguage.ARABIC -> "طلب من %s تحرير المنفذ %d"
        AppLanguage.PORTUGUESE -> "Solicitado a %s liberar a porta %d"
        AppLanguage.SPANISH -> "Solicitado a %s liberar el puerto %d"
        AppLanguage.FRENCH -> "Demandé à %s de libérer le port %d"
        AppLanguage.GERMAN -> "%s aufgefordert, Port %d freizugeben"
        AppLanguage.RUSSIAN -> "Запрошено у %s освобождение порта %d"
        AppLanguage.JAPANESE -> "%sにポート%dの解放を要求しました"
        AppLanguage.KOREAN -> "%s에게 포트 %d 해제를 요청했습니다"
    }

    val portManagerReleaseRemoteFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "释放失败：目标应用未响应"
        AppLanguage.ENGLISH -> "Release failed: target app did not respond"
        AppLanguage.ARABIC -> "فشل التحرير: لم يستجب التطبيق المستهدف"
        AppLanguage.PORTUGUESE -> "Falha na liberação: app alvo não respondeu"
        AppLanguage.SPANISH -> "Error al liberar: la app objetivo no respondió"
        AppLanguage.FRENCH -> "Échec de la libération : l'app cible n'a pas répondu"
        AppLanguage.GERMAN -> "Freigabe fehlgeschlagen: Ziel-App hat nicht geantwortet"
        AppLanguage.RUSSIAN -> "Не удалось освободить: целевое приложение не ответило"
        AppLanguage.JAPANESE -> "解放失敗：対象アプリが応答しませんでした"
        AppLanguage.KOREAN -> "해제 실패: 대상 앱이 응답하지 않았습니다"
    }

    val portManagerScanFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "扫描失败"
        AppLanguage.ENGLISH -> "Scan failed"
        AppLanguage.ARABIC -> "فشل الفحص"
        AppLanguage.PORTUGUESE -> "Falha na verificação"
        AppLanguage.SPANISH -> "Error al escanear"
        AppLanguage.FRENCH -> "Échec du scan"
        AppLanguage.GERMAN -> "Scan fehlgeschlagen"
        AppLanguage.RUSSIAN -> "Ошибка сканирования"
        AppLanguage.JAPANESE -> "スキャン失敗"
        AppLanguage.KOREAN -> "스캔 실패"
    }

    val portManagerSubtitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "查看、打开或释放本机与其它 WebToApp 实例的端口"
        AppLanguage.ENGLISH -> "Inspect, open, or free ports used by this and other WebToApp instances"
        AppLanguage.ARABIC -> "اعرض وافتح أو حرّر المنافذ المستخدمة بواسطة هذا التطبيق ونسخ WebToApp الأخرى"
        AppLanguage.PORTUGUESE -> "Veja, abra ou libere portas usadas por esta e outras instâncias do WebToApp"
        AppLanguage.SPANISH -> "Consulta, abre o libera puertos usados por esta y otras instancias de WebToApp"
        AppLanguage.FRENCH -> "Consultez, ouvrez ou libérez les ports de cette instance et d'autres WebToApp"
        AppLanguage.GERMAN -> "Ports dieser und anderer WebToApp-Instanzen ansehen, öffnen oder freigeben"
        AppLanguage.RUSSIAN -> "Просматривайте, открывайте или освобождайте порты этого и других экземпляров WebToApp"
        AppLanguage.JAPANESE -> "このアプリや他の WebToApp インスタンスが使うポートを確認・開放・解放"
        AppLanguage.KOREAN -> "이 앱과 다른 WebToApp 인스턴스가 사용하는 포트 확인·열기·해제"
    }
    val portManagerSearchHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "搜索端口、项目或类型…"
        AppLanguage.ENGLISH -> "Search port, project, or type…"
        AppLanguage.ARABIC -> "ابحث عن المنفذ أو المشروع أو النوع…"
        AppLanguage.PORTUGUESE -> "Pesquisar porta, projeto ou tipo…"
        AppLanguage.SPANISH -> "Buscar puerto, proyecto o tipo…"
        AppLanguage.FRENCH -> "Rechercher port, projet ou type…"
        AppLanguage.GERMAN -> "Port, Projekt oder Typ suchen…"
        AppLanguage.RUSSIAN -> "Поиск порта, проекта или типа…"
        AppLanguage.JAPANESE -> "ポート・プロジェクト・種類を検索…"
        AppLanguage.KOREAN -> "포트, 프로젝트 또는 유형 검색…"
    }
    val portManagerFilterAll: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "全部类型"
        AppLanguage.ENGLISH -> "All types"
        AppLanguage.ARABIC -> "كل الأنواع"
        AppLanguage.PORTUGUESE -> "Todos os tipos"
        AppLanguage.SPANISH -> "Todos los tipos"
        AppLanguage.FRENCH -> "Tous les types"
        AppLanguage.GERMAN -> "Alle Typen"
        AppLanguage.RUSSIAN -> "Все типы"
        AppLanguage.JAPANESE -> "すべての種類"
        AppLanguage.KOREAN -> "모든 유형"
    }
    val portManagerNoMatch: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "没有匹配的端口服务"
        AppLanguage.ENGLISH -> "No matching port services"
        AppLanguage.ARABIC -> "لا توجد خدمات منافذ مطابقة"
        AppLanguage.PORTUGUESE -> "Nenhum serviço de porta correspondente"
        AppLanguage.SPANISH -> "No hay servicios de puerto coincidentes"
        AppLanguage.FRENCH -> "Aucun service de port correspondant"
        AppLanguage.GERMAN -> "Keine passenden Port-Dienste"
        AppLanguage.RUSSIAN -> "Нет подходящих портовых служб"
        AppLanguage.JAPANESE -> "一致するポートサービスがありません"
        AppLanguage.KOREAN -> "일치하는 포트 서비스가 없습니다"
    }
    val portManagerRespondingCount: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "%d 个响应中"
        AppLanguage.ENGLISH -> "%d responding"
        AppLanguage.ARABIC -> "%d يستجيب"
        AppLanguage.PORTUGUESE -> "%d respondendo"
        AppLanguage.SPANISH -> "%d respondiendo"
        AppLanguage.FRENCH -> "%d qui répondent"
        AppLanguage.GERMAN -> "%d antworten"
        AppLanguage.RUSSIAN -> "%d отвечают"
        AppLanguage.JAPANESE -> "%d 件が応答中"
        AppLanguage.KOREAN -> "%d개 응답 중"
    }
    val portManagerTypeLocalHttp: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "静态服务"
        AppLanguage.ENGLISH -> "Static server"
        AppLanguage.ARABIC -> "خادم ثابت"
        AppLanguage.PORTUGUESE -> "Servidor estático"
        AppLanguage.SPANISH -> "Servidor estático"
        AppLanguage.FRENCH -> "Serveur statique"
        AppLanguage.GERMAN -> "Statischer Server"
        AppLanguage.RUSSIAN -> "Статический сервер"
        AppLanguage.JAPANESE -> "静的サーバー"
        AppLanguage.KOREAN -> "정적 서버"
    }
    val portManagerTypeNodeJs: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Node.js"
        AppLanguage.ENGLISH -> "Node.js"
        AppLanguage.ARABIC -> "Node.js"
        AppLanguage.PORTUGUESE -> "Node.js"
        AppLanguage.SPANISH -> "Node.js"
        AppLanguage.FRENCH -> "Node.js"
        AppLanguage.GERMAN -> "Node.js"
        AppLanguage.RUSSIAN -> "Node.js"
        AppLanguage.JAPANESE -> "Node.js"
        AppLanguage.KOREAN -> "Node.js"
    }
    val portManagerTypePhp: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "PHP"
        AppLanguage.ENGLISH -> "PHP"
        AppLanguage.ARABIC -> "PHP"
        AppLanguage.PORTUGUESE -> "PHP"
        AppLanguage.SPANISH -> "PHP"
        AppLanguage.FRENCH -> "PHP"
        AppLanguage.GERMAN -> "PHP"
        AppLanguage.RUSSIAN -> "PHP"
        AppLanguage.JAPANESE -> "PHP"
        AppLanguage.KOREAN -> "PHP"
    }
    val portManagerTypePython: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Python"
        AppLanguage.ENGLISH -> "Python"
        AppLanguage.ARABIC -> "Python"
        AppLanguage.PORTUGUESE -> "Python"
        AppLanguage.SPANISH -> "Python"
        AppLanguage.FRENCH -> "Python"
        AppLanguage.GERMAN -> "Python"
        AppLanguage.RUSSIAN -> "Python"
        AppLanguage.JAPANESE -> "Python"
        AppLanguage.KOREAN -> "Python"
    }
    val portManagerTypeGo: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Go"
        AppLanguage.ENGLISH -> "Go"
        AppLanguage.ARABIC -> "Go"
        AppLanguage.PORTUGUESE -> "Go"
        AppLanguage.SPANISH -> "Go"
        AppLanguage.FRENCH -> "Go"
        AppLanguage.GERMAN -> "Go"
        AppLanguage.RUSSIAN -> "Go"
        AppLanguage.JAPANESE -> "Go"
        AppLanguage.KOREAN -> "Go"
    }
    val portManagerTypeUnknown: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "未知"
        AppLanguage.ENGLISH -> "Unknown"
        AppLanguage.ARABIC -> "غير معروف"
        AppLanguage.PORTUGUESE -> "Desconhecido"
        AppLanguage.SPANISH -> "Desconocido"
        AppLanguage.FRENCH -> "Inconnu"
        AppLanguage.GERMAN -> "Unbekannt"
        AppLanguage.RUSSIAN -> "Неизвестно"
        AppLanguage.JAPANESE -> "不明"
        AppLanguage.KOREAN -> "알 수 없음"
    }
    val portManagerCopyPort: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "复制端口"
        AppLanguage.ENGLISH -> "Copy port"
        AppLanguage.ARABIC -> "نسخ المنفذ"
        AppLanguage.PORTUGUESE -> "Copiar porta"
        AppLanguage.SPANISH -> "Copiar puerto"
        AppLanguage.FRENCH -> "Copier le port"
        AppLanguage.GERMAN -> "Port kopieren"
        AppLanguage.RUSSIAN -> "Копировать порт"
        AppLanguage.JAPANESE -> "ポートをコピー"
        AppLanguage.KOREAN -> "포트 복사"
    }
    val portManagerPortCopied: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "端口已复制"
        AppLanguage.ENGLISH -> "Port copied"
        AppLanguage.ARABIC -> "تم نسخ المنفذ"
        AppLanguage.PORTUGUESE -> "Porta copiada"
        AppLanguage.SPANISH -> "Puerto copiado"
        AppLanguage.FRENCH -> "Port copié"
        AppLanguage.GERMAN -> "Port kopiert"
        AppLanguage.RUSSIAN -> "Порт скопирован"
        AppLanguage.JAPANESE -> "ポートをコピーしました"
        AppLanguage.KOREAN -> "포트를 복사했습니다"
    }

    val runtimeDepsTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "运行时 & 依赖管理"
        AppLanguage.ENGLISH -> "Runtime & Dependencies"
        AppLanguage.ARABIC -> "وقت التشغيل والتبعيات"
        AppLanguage.PORTUGUESE -> "Runtime & Dependências"
        AppLanguage.SPANISH -> "Runtime & Dependencias"
        AppLanguage.FRENCH -> "Runtime & Dépendances"
        AppLanguage.GERMAN -> "Runtime & Abhängigkeiten"
        AppLanguage.RUSSIAN -> "Среда выполнения и зависимости"
        AppLanguage.JAPANESE -> "ランタイム & 依存関係"
        AppLanguage.KOREAN -> "런타임 & 종속성"
    }

    val depSectionRuntimes: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "运行时环境"
        AppLanguage.ENGLISH -> "Runtimes"
        AppLanguage.ARABIC -> "بيئات التشغيل"
        AppLanguage.PORTUGUESE -> "Ambientes de execução"
        AppLanguage.SPANISH -> "Entornos de ejecución"
        AppLanguage.FRENCH -> "Environnements d'exécution"
        AppLanguage.GERMAN -> "Laufzeitumgebungen"
        AppLanguage.RUSSIAN -> "Среды выполнения"
        AppLanguage.JAPANESE -> "ランタイム"
        AppLanguage.KOREAN -> "런타임"
    }

    val depSectionRuntimePlugins: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "运行时插件"
        AppLanguage.ENGLISH -> "Runtime Plugins"
        AppLanguage.ARABIC -> "إضافات التشغيل"
        AppLanguage.PORTUGUESE -> "Plugins de Runtime"
        AppLanguage.SPANISH -> "Plugins de Runtime"
        AppLanguage.FRENCH -> "Plugins de runtime"
        AppLanguage.GERMAN -> "Runtime-Plugins"
        AppLanguage.RUSSIAN -> "Плагины среды выполнения"
        AppLanguage.JAPANESE -> "ランタイムプラグイン"
        AppLanguage.KOREAN -> "런타임 플러그인"
    }

    val depSectionProjects: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "项目文件"
        AppLanguage.ENGLISH -> "Project Files"
        AppLanguage.ARABIC -> "ملفات المشاريع"
        AppLanguage.PORTUGUESE -> "Arquivos de Projeto"
        AppLanguage.SPANISH -> "Archivos de Proyecto"
        AppLanguage.FRENCH -> "Fichiers de projet"
        AppLanguage.GERMAN -> "Projektdateien"
        AppLanguage.RUSSIAN -> "Файлы проектов"
        AppLanguage.JAPANESE -> "プロジェクトファイル"
        AppLanguage.KOREAN -> "프로젝트 파일"
    }

    val depSectionDownload: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "镜像源 & 下载"
        AppLanguage.ENGLISH -> "Mirror & Download"
        AppLanguage.ARABIC -> "المرآة والتنزيل"
        AppLanguage.PORTUGUESE -> "Espelho & Download"
        AppLanguage.SPANISH -> "Mirror & Descarga"
        AppLanguage.FRENCH -> "Miroir & Téléchargement"
        AppLanguage.GERMAN -> "Spiegel & Download"
        AppLanguage.RUSSIAN -> "Зеркало и загрузка"
        AppLanguage.JAPANESE -> "ミラー & ダウンロード"
        AppLanguage.KOREAN -> "미러 & 다운로드"
    }

    val depSectionStorage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "存储空间"
        AppLanguage.ENGLISH -> "Storage"
        AppLanguage.ARABIC -> "التخزين"
        AppLanguage.PORTUGUESE -> "Armazenamento"
        AppLanguage.SPANISH -> "Almacenamiento"
        AppLanguage.FRENCH -> "Stockage"
        AppLanguage.GERMAN -> "Speicher"
        AppLanguage.RUSSIAN -> "Хранилище"
        AppLanguage.JAPANESE -> "ストレージ"
        AppLanguage.KOREAN -> "저장공간"
    }

    val depStatusReady: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已就绪"
        AppLanguage.ENGLISH -> "Ready"
        AppLanguage.ARABIC -> "جاهز"
        AppLanguage.PORTUGUESE -> "Pronto"
        AppLanguage.SPANISH -> "Listo"
        AppLanguage.FRENCH -> "Prêt"
        AppLanguage.GERMAN -> "Bereit"
        AppLanguage.RUSSIAN -> "Готово"
        AppLanguage.JAPANESE -> "準備完了"
        AppLanguage.KOREAN -> "준비됨"
    }

    val depStatusNotInstalled: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "未安装"
        AppLanguage.ENGLISH -> "Not Installed"
        AppLanguage.ARABIC -> "غير مثبت"
        AppLanguage.PORTUGUESE -> "Não Instalado"
        AppLanguage.SPANISH -> "No Instalado"
        AppLanguage.FRENCH -> "Non installé"
        AppLanguage.GERMAN -> "Nicht installiert"
        AppLanguage.RUSSIAN -> "Не установлено"
        AppLanguage.JAPANESE -> "未インストール"
        AppLanguage.KOREAN -> "설치되지 않음"
    }

    val depStatusDownloading: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下载中..."
        AppLanguage.ENGLISH -> "Downloading..."
        AppLanguage.ARABIC -> "جارٍ التنزيل..."
        AppLanguage.PORTUGUESE -> "Baixando..."
        AppLanguage.SPANISH -> "Descargando..."
        AppLanguage.FRENCH -> "Téléchargement..."
        AppLanguage.GERMAN -> "Wird heruntergeladen..."
        AppLanguage.RUSSIAN -> "Загрузка..."
        AppLanguage.JAPANESE -> "ダウンロード中..."
        AppLanguage.KOREAN -> "다운로드 중..."
    }

    val depPhpRuntime: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "PHP 运行时"
        AppLanguage.ENGLISH -> "PHP Runtime"
        AppLanguage.ARABIC -> "وقت تشغيل PHP"
        AppLanguage.PORTUGUESE -> "Runtime PHP"
        AppLanguage.SPANISH -> "Runtime PHP"
        AppLanguage.FRENCH -> "Runtime PHP"
        AppLanguage.GERMAN -> "PHP-Runtime"
        AppLanguage.RUSSIAN -> "Среда выполнения PHP"
        AppLanguage.JAPANESE -> "PHPランタイム"
        AppLanguage.KOREAN -> "PHP 런타임"
    }

    val depPhpDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "PHP ${com.webtoapp.core.wordpress.WordPressDependencyManager.PHP_VERSION} · WordPress / PHP 应用"
        AppLanguage.ENGLISH -> "PHP ${com.webtoapp.core.wordpress.WordPressDependencyManager.PHP_VERSION} · WordPress / PHP Apps"
        AppLanguage.ARABIC -> "PHP ${com.webtoapp.core.wordpress.WordPressDependencyManager.PHP_VERSION} · تطبيقات WordPress / PHP"
        AppLanguage.PORTUGUESE -> "PHP ${com.webtoapp.core.wordpress.WordPressDependencyManager.PHP_VERSION} · Apps WordPress / PHP"
        AppLanguage.SPANISH -> "PHP ${com.webtoapp.core.wordpress.WordPressDependencyManager.PHP_VERSION} · Apps WordPress / PHP"
        AppLanguage.FRENCH -> "PHP ${com.webtoapp.core.wordpress.WordPressDependencyManager.PHP_VERSION} · Apps WordPress / PHP"
        AppLanguage.GERMAN -> "PHP ${com.webtoapp.core.wordpress.WordPressDependencyManager.PHP_VERSION} · WordPress / PHP-Apps"
        AppLanguage.RUSSIAN -> "PHP ${com.webtoapp.core.wordpress.WordPressDependencyManager.PHP_VERSION} · Приложения WordPress / PHP"
        AppLanguage.JAPANESE -> "PHP ${com.webtoapp.core.wordpress.WordPressDependencyManager.PHP_VERSION} · WordPress / PHPアプリ"
        AppLanguage.KOREAN -> "PHP ${com.webtoapp.core.wordpress.WordPressDependencyManager.PHP_VERSION} · WordPress / PHP 앱"
    }

    val depWpCore: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "WordPress 核心"
        AppLanguage.ENGLISH -> "WordPress Core"
        AppLanguage.ARABIC -> "نواة WordPress"
        AppLanguage.PORTUGUESE -> "Núcleo WordPress"
        AppLanguage.SPANISH -> "Núcleo WordPress"
        AppLanguage.FRENCH -> "Cœur WordPress"
        AppLanguage.GERMAN -> "WordPress-Core"
        AppLanguage.RUSSIAN -> "Ядро WordPress"
        AppLanguage.JAPANESE -> "WordPressコア"
        AppLanguage.KOREAN -> "WordPress 코어"
    }

    val depWpCoreDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "WordPress ${com.webtoapp.core.wordpress.WordPressDependencyManager.WORDPRESS_VERSION} 核心文件"
        AppLanguage.ENGLISH -> "WordPress ${com.webtoapp.core.wordpress.WordPressDependencyManager.WORDPRESS_VERSION} core files"
        AppLanguage.ARABIC -> "ملفات نواة WordPress ${com.webtoapp.core.wordpress.WordPressDependencyManager.WORDPRESS_VERSION}"
        AppLanguage.PORTUGUESE -> "Arquivos do núcleo WordPress ${com.webtoapp.core.wordpress.WordPressDependencyManager.WORDPRESS_VERSION}"
        AppLanguage.SPANISH -> "Archivos del núcleo WordPress ${com.webtoapp.core.wordpress.WordPressDependencyManager.WORDPRESS_VERSION}"
        AppLanguage.FRENCH -> "Fichiers du cœur WordPress ${com.webtoapp.core.wordpress.WordPressDependencyManager.WORDPRESS_VERSION}"
        AppLanguage.GERMAN -> "WordPress ${com.webtoapp.core.wordpress.WordPressDependencyManager.WORDPRESS_VERSION} -Core-Dateien"
        AppLanguage.RUSSIAN -> "Файлы ядра WordPress ${com.webtoapp.core.wordpress.WordPressDependencyManager.WORDPRESS_VERSION}"
        AppLanguage.JAPANESE -> "WordPress ${com.webtoapp.core.wordpress.WordPressDependencyManager.WORDPRESS_VERSION} コアファイル"
        AppLanguage.KOREAN -> "WordPress ${com.webtoapp.core.wordpress.WordPressDependencyManager.WORDPRESS_VERSION} 코어 파일"
    }

    val depSqlitePlugin: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "SQLite 数据库插件"
        AppLanguage.ENGLISH -> "SQLite Database Plugin"
        AppLanguage.ARABIC -> "إضافة قاعدة بيانات SQLite"
        AppLanguage.PORTUGUESE -> "Plugin de Banco de Dados SQLite"
        AppLanguage.SPANISH -> "Plugin de Base de Datos SQLite"
        AppLanguage.FRENCH -> "Plugin de Base de Données SQLite"
        AppLanguage.GERMAN -> "SQLite-Datenbank-Plugin"
        AppLanguage.RUSSIAN -> "Плагин базы данных SQLite"
        AppLanguage.JAPANESE -> "SQLite データベースプラグイン"
        AppLanguage.KOREAN -> "SQLite 데이터베이스 플러그인"
    }

    val depSqliteDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "WordPress SQLite 集成 v${com.webtoapp.core.wordpress.WordPressDependencyManager.SQLITE_PLUGIN_VERSION}"
        AppLanguage.ENGLISH -> "WordPress SQLite Integration v${com.webtoapp.core.wordpress.WordPressDependencyManager.SQLITE_PLUGIN_VERSION}"
        AppLanguage.ARABIC -> "تكامل WordPress SQLite v${com.webtoapp.core.wordpress.WordPressDependencyManager.SQLITE_PLUGIN_VERSION}"
        AppLanguage.PORTUGUESE -> "Integração WordPress SQLite v${com.webtoapp.core.wordpress.WordPressDependencyManager.SQLITE_PLUGIN_VERSION}"
        AppLanguage.SPANISH -> "Integración WordPress SQLite v${com.webtoapp.core.wordpress.WordPressDependencyManager.SQLITE_PLUGIN_VERSION}"
        AppLanguage.FRENCH -> "Intégration WordPress SQLite v${com.webtoapp.core.wordpress.WordPressDependencyManager.SQLITE_PLUGIN_VERSION}"
        AppLanguage.GERMAN -> "WordPress-SQLite-Integration v${com.webtoapp.core.wordpress.WordPressDependencyManager.SQLITE_PLUGIN_VERSION}"
        AppLanguage.RUSSIAN -> "Интеграция WordPress SQLite v${com.webtoapp.core.wordpress.WordPressDependencyManager.SQLITE_PLUGIN_VERSION}"
        AppLanguage.JAPANESE -> "WordPress SQLite 統合 v${com.webtoapp.core.wordpress.WordPressDependencyManager.SQLITE_PLUGIN_VERSION}"
        AppLanguage.KOREAN -> "WordPress SQLite 통합 v${com.webtoapp.core.wordpress.WordPressDependencyManager.SQLITE_PLUGIN_VERSION}"
    }

    val depNodeRuntime: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Node.js 运行时"
        AppLanguage.ENGLISH -> "Node.js Runtime"
        AppLanguage.ARABIC -> "وقت تشغيل Node.js"
        AppLanguage.PORTUGUESE -> "Runtime Node.js"
        AppLanguage.SPANISH -> "Runtime de Node.js"
        AppLanguage.FRENCH -> "Runtime Node.js"
        AppLanguage.GERMAN -> "Node.js-Laufzeit"
        AppLanguage.RUSSIAN -> "Среда выполнения Node.js"
        AppLanguage.JAPANESE -> "Node.js ランタイム"
        AppLanguage.KOREAN -> "Node.js 런타임"
    }

    val depNodeDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Node.js ${com.webtoapp.core.nodejs.NodeDependencyManager.NODE_VERSION} · 全栈 / API 后端"
        AppLanguage.ENGLISH -> "Node.js ${com.webtoapp.core.nodejs.NodeDependencyManager.NODE_VERSION} · Fullstack / API Backend"
        AppLanguage.ARABIC -> "Node.js ${com.webtoapp.core.nodejs.NodeDependencyManager.NODE_VERSION} · كامل / خلفية API"
        AppLanguage.PORTUGUESE -> "Node.js ${com.webtoapp.core.nodejs.NodeDependencyManager.NODE_VERSION} · Fullstack / Backend de API"
        AppLanguage.SPANISH -> "Node.js ${com.webtoapp.core.nodejs.NodeDependencyManager.NODE_VERSION} · Fullstack / Backend de API"
        AppLanguage.FRENCH -> "Node.js ${com.webtoapp.core.nodejs.NodeDependencyManager.NODE_VERSION} · Fullstack / Backend API"
        AppLanguage.GERMAN -> "Node.js ${com.webtoapp.core.nodejs.NodeDependencyManager.NODE_VERSION} · Fullstack / API-Backend"
        AppLanguage.RUSSIAN -> "Node.js ${com.webtoapp.core.nodejs.NodeDependencyManager.NODE_VERSION} · Fullstack / API-бэкенд"
        AppLanguage.JAPANESE -> "Node.js ${com.webtoapp.core.nodejs.NodeDependencyManager.NODE_VERSION} · フルスタック / API バックエンド"
        AppLanguage.KOREAN -> "Node.js ${com.webtoapp.core.nodejs.NodeDependencyManager.NODE_VERSION} · 풀스택 / API 백엔드"
    }

    val depPythonRuntime: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Python 运行时"
        AppLanguage.ENGLISH -> "Python Runtime"
        AppLanguage.ARABIC -> "وقت تشغيل Python"
        AppLanguage.PORTUGUESE -> "Runtime Python"
        AppLanguage.SPANISH -> "Runtime de Python"
        AppLanguage.FRENCH -> "Runtime Python"
        AppLanguage.GERMAN -> "Python-Laufzeit"
        AppLanguage.RUSSIAN -> "Среда выполнения Python"
        AppLanguage.JAPANESE -> "Python ランタイム"
        AppLanguage.KOREAN -> "Python 런타임"
    }

    val depPythonDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Python ${com.webtoapp.core.python.PythonDependencyManager.PYTHON_FULL_VERSION} · 脚本 / Web 后端"
        AppLanguage.ENGLISH -> "Python ${com.webtoapp.core.python.PythonDependencyManager.PYTHON_FULL_VERSION} · Scripting / Web Backend"
        AppLanguage.ARABIC -> "Python ${com.webtoapp.core.python.PythonDependencyManager.PYTHON_FULL_VERSION} · برمجة / خلفية الويب"
        AppLanguage.PORTUGUESE -> "Python ${com.webtoapp.core.python.PythonDependencyManager.PYTHON_FULL_VERSION} · Scripts / Backend Web"
        AppLanguage.SPANISH -> "Python ${com.webtoapp.core.python.PythonDependencyManager.PYTHON_FULL_VERSION} · Scripts / Backend Web"
        AppLanguage.FRENCH -> "Python ${com.webtoapp.core.python.PythonDependencyManager.PYTHON_FULL_VERSION} · Scripts / Backend Web"
        AppLanguage.GERMAN -> "Python ${com.webtoapp.core.python.PythonDependencyManager.PYTHON_FULL_VERSION} · Skripte / Web-Backend"
        AppLanguage.RUSSIAN -> "Python ${com.webtoapp.core.python.PythonDependencyManager.PYTHON_FULL_VERSION} · Скрипты / Веб-бэкенд"
        AppLanguage.JAPANESE -> "Python ${com.webtoapp.core.python.PythonDependencyManager.PYTHON_FULL_VERSION} · スクリプト / Web バックエンド"
        AppLanguage.KOREAN -> "Python ${com.webtoapp.core.python.PythonDependencyManager.PYTHON_FULL_VERSION} · 스크립팅 / 웹 백엔드"
    }

    val depGoRuntime: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Go 二进制运行支持"
        AppLanguage.ENGLISH -> "Go Binary Runtime Support"
        AppLanguage.ARABIC -> "دعم تشغيل ملفات Go الثنائية"
        AppLanguage.PORTUGUESE -> "Suporte a Runtime Binário Go"
        AppLanguage.SPANISH -> "Soporte de Runtime Binario Go"
        AppLanguage.FRENCH -> "Support du Runtime Binaire Go"
        AppLanguage.GERMAN -> "Go-Binary-Runtime-Unterstützung"
        AppLanguage.RUSSIAN -> "Поддержка бинарного runtime Go"
        AppLanguage.JAPANESE -> "Go バイナリランタイムサポート"
        AppLanguage.KOREAN -> "Go 바이너리 런타임 지원"
    }

    val depGoDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Go 1.26 官方工具链 · 支持 go build / go mod / go run · arm64-v8a"
        AppLanguage.ENGLISH -> "Go 1.26 official toolchain · Supports go build / go mod / go run · arm64-v8a"
        AppLanguage.ARABIC -> "سلسلة أدوات Go 1.26 الرسمية · تدعم go build / go mod / go run · arm64-v8a"
        AppLanguage.PORTUGUESE -> "Toolchain oficial Go 1.26 · Suporta go build / go mod / go run · arm64-v8a"
        AppLanguage.SPANISH -> "Toolchain oficial Go 1.26 · Soporta go build / go mod / go run · arm64-v8a"
        AppLanguage.FRENCH -> "Toolchain officielle Go 1.26 · Prend en charge go build / go mod / go run · arm64-v8a"
        AppLanguage.GERMAN -> "Offizielle Go 1.26-Toolchain · Unterstützt go build / go mod / go run · arm64-v8a"
        AppLanguage.RUSSIAN -> "Официальная toolchain Go 1.26 · Поддерживает go build / go mod / go run · arm64-v8a"
        AppLanguage.JAPANESE -> "Go 1.26 公式ツールチェーン · go build / go mod / go run 対応 · arm64-v8a"
        AppLanguage.KOREAN -> "Go 1.26 공식 툴체인 · go build / go mod / go run 지원 · arm64-v8a"
    }

    val depWpProjects: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "WordPress 项目"
        AppLanguage.ENGLISH -> "WordPress Projects"
        AppLanguage.ARABIC -> "مشاريع WordPress"
        AppLanguage.PORTUGUESE -> "Projetos WordPress"
        AppLanguage.SPANISH -> "Proyectos WordPress"
        AppLanguage.FRENCH -> "Projets WordPress"
        AppLanguage.GERMAN -> "WordPress-Projekte"
        AppLanguage.RUSSIAN -> "Проекты WordPress"
        AppLanguage.JAPANESE -> "WordPress プロジェクト"
        AppLanguage.KOREAN -> "WordPress 프로젝트"
    }

    val depNodeProjects: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Node.js 项目"
        AppLanguage.ENGLISH -> "Node.js Projects"
        AppLanguage.ARABIC -> "مشاريع Node.js"
        AppLanguage.PORTUGUESE -> "Projetos Node.js"
        AppLanguage.SPANISH -> "Proyectos Node.js"
        AppLanguage.FRENCH -> "Projets Node.js"
        AppLanguage.GERMAN -> "Node.js-Projekte"
        AppLanguage.RUSSIAN -> "Проекты Node.js"
        AppLanguage.JAPANESE -> "Node.js プロジェクト"
        AppLanguage.KOREAN -> "Node.js 프로젝트"
    }

    val depPythonProjects: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Python 项目"
        AppLanguage.ENGLISH -> "Python Projects"
        AppLanguage.ARABIC -> "مشاريع Python"
        AppLanguage.PORTUGUESE -> "Projetos Python"
        AppLanguage.SPANISH -> "Proyectos Python"
        AppLanguage.FRENCH -> "Projets Python"
        AppLanguage.GERMAN -> "Python-Projekte"
        AppLanguage.RUSSIAN -> "Проекты Python"
        AppLanguage.JAPANESE -> "Python プロジェクト"
        AppLanguage.KOREAN -> "Python 프로젝트"
    }

    val depGoProjects: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Go 项目"
        AppLanguage.ENGLISH -> "Go Projects"
        AppLanguage.ARABIC -> "مشاريع Go"
        AppLanguage.PORTUGUESE -> "Projetos Go"
        AppLanguage.SPANISH -> "Proyectos Go"
        AppLanguage.FRENCH -> "Projets Go"
        AppLanguage.GERMAN -> "Go-Projekte"
        AppLanguage.RUSSIAN -> "Проекты Go"
        AppLanguage.JAPANESE -> "Go プロジェクト"
        AppLanguage.KOREAN -> "Go 프로젝트"
    }

    val depDocsProjects: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "文档站点"
        AppLanguage.ENGLISH -> "Docs Sites"
        AppLanguage.ARABIC -> "مواقع التوثيق"
        AppLanguage.PORTUGUESE -> "Sites de Documentação"
        AppLanguage.SPANISH -> "Sitios de Documentación"
        AppLanguage.FRENCH -> "Sites de Documentation"
        AppLanguage.GERMAN -> "Dokumentationsseiten"
        AppLanguage.RUSSIAN -> "Сайты документации"
        AppLanguage.JAPANESE -> "ドキュメントサイト"
        AppLanguage.KOREAN -> "문서 사이트"
    }

    fun depProjectCount(count: Int): String = when (Strings.lang) {
        AppLanguage.CHINESE -> "$count 个项目"
        AppLanguage.ENGLISH -> "$count project${if (count != 1) "s" else ""}"
        AppLanguage.ARABIC -> "$count مشروع"
        AppLanguage.PORTUGUESE -> "$count projetos"
        AppLanguage.SPANISH -> "$count proyectos"
        AppLanguage.FRENCH -> "$count projets"
        AppLanguage.GERMAN -> "$count Projekte"
        AppLanguage.RUSSIAN -> "$count проектов"
        AppLanguage.JAPANESE -> "$count プロジェクト"
        AppLanguage.KOREAN -> "$count 프로젝트"
    }

    val depMirrorSource: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下载镜像源"
        AppLanguage.ENGLISH -> "Download Mirror"
        AppLanguage.ARABIC -> "مرآة التنزيل"
        AppLanguage.PORTUGUESE -> "Espelho de Download"
        AppLanguage.SPANISH -> "Espejo de Descarga"
        AppLanguage.FRENCH -> "Miroir de Téléchargement"
        AppLanguage.GERMAN -> "Download-Spiegel"
        AppLanguage.RUSSIAN -> "Зеркало загрузки"
        AppLanguage.JAPANESE -> "ダウンロードミラー"
        AppLanguage.KOREAN -> "다운로드 미러"
    }

    val depMirrorDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "影响 PHP、WordPress、Node.js 和 Go 的下载速度"
        AppLanguage.ENGLISH -> "Affects download speed for PHP, WordPress, Node.js & Go"
        AppLanguage.ARABIC -> "يؤثر على سرعة تنزيل PHP و WordPress و Node.js و Go"
        AppLanguage.PORTUGUESE -> "Afeta a velocidade de download de PHP, WordPress, Node.js e Go"
        AppLanguage.SPANISH -> "Afecta la velocidad de descarga de PHP, WordPress, Node.js y Go"
        AppLanguage.FRENCH -> "Affecte la vitesse de téléchargement de PHP, WordPress, Node.js et Go"
        AppLanguage.GERMAN -> "Beeinflusst die Download-Geschwindigkeit für PHP, WordPress, Node.js & Go"
        AppLanguage.RUSSIAN -> "Влияет на скорость загрузки PHP, WordPress, Node.js и Go"
        AppLanguage.JAPANESE -> "PHP、WordPress、Node.js、Go のダウンロード速度に影響します"
        AppLanguage.KOREAN -> "PHP, WordPress, Node.js, Go의 다운로드 속도에 영향을 줍니다"
    }

    val depMirrorCN: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "国内镜像"
        AppLanguage.ENGLISH -> "China Mirror"
        AppLanguage.ARABIC -> "مرآة الصين"
        AppLanguage.PORTUGUESE -> "Espelho da China"
        AppLanguage.SPANISH -> "Espejo de China"
        AppLanguage.FRENCH -> "Miroir Chine"
        AppLanguage.GERMAN -> "China-Spiegel"
        AppLanguage.RUSSIAN -> "Китайское зеркало"
        AppLanguage.JAPANESE -> "中国ミラー"
        AppLanguage.KOREAN -> "중국 미러"
    }

    val depMirrorGlobal: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "国际源"
        AppLanguage.ENGLISH -> "Global Source"
        AppLanguage.ARABIC -> "مصدر عالمي"
        AppLanguage.PORTUGUESE -> "Fonte Global"
        AppLanguage.SPANISH -> "Fuente Global"
        AppLanguage.FRENCH -> "Source Globale"
        AppLanguage.GERMAN -> "Globale Quelle"
        AppLanguage.RUSSIAN -> "Глобальный источник"
        AppLanguage.JAPANESE -> "グローバルソース"
        AppLanguage.KOREAN -> "글로벌 소스"
    }

    val depMirrorAuto: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自动"
        AppLanguage.ENGLISH -> "Auto"
        AppLanguage.ARABIC -> "تلقائي"
        AppLanguage.PORTUGUESE -> "Automático"
        AppLanguage.SPANISH -> "Automático"
        AppLanguage.FRENCH -> "Automatique"
        AppLanguage.GERMAN -> "Automatisch"
        AppLanguage.RUSSIAN -> "Авто"
        AppLanguage.JAPANESE -> "自動"
        AppLanguage.KOREAN -> "자동"
    }

    val depDownloadAll: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下载全部运行时"
        AppLanguage.ENGLISH -> "Download All Runtimes"
        AppLanguage.ARABIC -> "تنزيل جميع بيئات التشغيل"
        AppLanguage.PORTUGUESE -> "Baixar Todos os Runtimes"
        AppLanguage.SPANISH -> "Descargar Todos los Runtimes"
        AppLanguage.FRENCH -> "Télécharger Tous les Runtimes"
        AppLanguage.GERMAN -> "Alle Runtimes herunterladen"
        AppLanguage.RUSSIAN -> "Загрузить все среды выполнения"
        AppLanguage.JAPANESE -> "すべてのランタイムをダウンロード"
        AppLanguage.KOREAN -> "모든 런타임 다운로드"
    }

    val depTotalStorage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "总占用空间"
        AppLanguage.ENGLISH -> "Total Storage"
        AppLanguage.ARABIC -> "إجمالي التخزين"
        AppLanguage.PORTUGUESE -> "Armazenamento Total"
        AppLanguage.SPANISH -> "Almacenamiento Total"
        AppLanguage.FRENCH -> "Stockage Total"
        AppLanguage.GERMAN -> "Gesamtspeicher"
        AppLanguage.RUSSIAN -> "Общий объём хранилища"
        AppLanguage.JAPANESE -> "合計ストレージ"
        AppLanguage.KOREAN -> "전체 저장 공간"
    }

    val depClearAll: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "清理全部缓存"
        AppLanguage.ENGLISH -> "Clear All Cache"
        AppLanguage.ARABIC -> "مسح كل ذاكرة التخزين المؤقت"
        AppLanguage.PORTUGUESE -> "Limpar Todo o Cache"
        AppLanguage.SPANISH -> "Borrar Toda la Caché"
        AppLanguage.FRENCH -> "Vider Tout le Cache"
        AppLanguage.GERMAN -> "Gesamten Cache löschen"
        AppLanguage.RUSSIAN -> "Очистить весь кэш"
        AppLanguage.JAPANESE -> "すべてのキャッシュをクリア"
        AppLanguage.KOREAN -> "모든 캐시 지우기"
    }

    val depClearConfirm: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "将删除所有已下载的运行时和缓存文件，下次使用时需要重新下载。确定继续？"
        AppLanguage.ENGLISH -> "This will delete all downloaded runtimes and cache. They will need to be re-downloaded. Continue?"
        AppLanguage.ARABIC -> "سيؤدي هذا إلى حذف جميع بيئات التشغيل وذاكرة التخزين المؤقت المحملة. ستحتاج إلى إعادة التنزيل. هل تريد المتابعة؟"
        AppLanguage.PORTUGUESE -> "Isso excluirá todos os runtimes e o cache baixados. Eles precisarão ser baixados novamente. Continuar?"
        AppLanguage.SPANISH -> "Esto eliminará todos los runtimes y la caché descargados. Tendrán que ser descargados nuevamente. ¿Continuar?"
        AppLanguage.FRENCH -> "Cela supprimera tous les runtimes et le cache téléchargés. Ils devront être re-téléchargés. Continuer ?"
        AppLanguage.GERMAN -> "Dies löscht alle heruntergeladenen Runtimes und den Cache. Sie müssen neu heruntergeladen werden. Fortfahren?"
        AppLanguage.RUSSIAN -> "Это удалит все загруженные среды выполнения и кэш. Их потребуется загрузить заново. Продолжить?"
        AppLanguage.JAPANESE -> "ダウンロードしたすべてのランタイムとキャッシュが削除されます。再ダウンロードが必要になります。続行しますか？"
        AppLanguage.KOREAN -> "이 작업은 다운로드된 모든 런타임과 캐시를 삭제합니다. 다시 다운로드해야 합니다. 계속하시겠습니까?"
    }

    val depAllReady: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "所有运行时已就绪"
        AppLanguage.ENGLISH -> "All runtimes ready"
        AppLanguage.ARABIC -> "جميع بيئات التشغيل جاهزة"
        AppLanguage.PORTUGUESE -> "Todos os runtimes prontos"
        AppLanguage.SPANISH -> "Todos los runtimes listos"
        AppLanguage.FRENCH -> "Tous les runtimes prêts"
        AppLanguage.GERMAN -> "Alle Runtimes bereit"
        AppLanguage.RUSSIAN -> "Все среды выполнения готовы"
        AppLanguage.JAPANESE -> "すべてのランタイムの準備が完了"
        AppLanguage.KOREAN -> "모든 런타임 준비 완료"
    }

    val depSomeNotReady: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "部分运行时未安装"
        AppLanguage.ENGLISH -> "Some runtimes not installed"
        AppLanguage.ARABIC -> "بعض بيئات التشغيل غير مثبتة"
        AppLanguage.PORTUGUESE -> "Alguns runtimes não estão instalados"
        AppLanguage.SPANISH -> "Algunos runtimes no están instalados"
        AppLanguage.FRENCH -> "Certains runtimes ne sont pas installés"
        AppLanguage.GERMAN -> "Einige Runtimes sind nicht installiert"
        AppLanguage.RUSSIAN -> "Некоторые среды выполнения не установлены"
        AppLanguage.JAPANESE -> "一部のランタイムがインストールされていません"
        AppLanguage.KOREAN -> "일부 런타임이 설치되지 않았습니다"
    }

    val depInstall: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "安装"
        AppLanguage.ENGLISH -> "Install"
        AppLanguage.ARABIC -> "تثبيت"
        AppLanguage.PORTUGUESE -> "Instalar"
        AppLanguage.SPANISH -> "Instalar"
        AppLanguage.FRENCH -> "Installer"
        AppLanguage.GERMAN -> "Installieren"
        AppLanguage.RUSSIAN -> "Установить"
        AppLanguage.JAPANESE -> "インストール"
        AppLanguage.KOREAN -> "설치"
    }

    val depDlPause: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "暂停"
        AppLanguage.ENGLISH -> "Pause"
        AppLanguage.ARABIC -> "إيقاف مؤقت"
        AppLanguage.PORTUGUESE -> "Pausar"
        AppLanguage.SPANISH -> "Pausar"
        AppLanguage.FRENCH -> "Mettre en pause"
        AppLanguage.GERMAN -> "Pausieren"
        AppLanguage.RUSSIAN -> "Пауза"
        AppLanguage.JAPANESE -> "一時停止"
        AppLanguage.KOREAN -> "일시정지"
    }

    val depDlResume: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "继续"
        AppLanguage.ENGLISH -> "Resume"
        AppLanguage.ARABIC -> "استئناف"
        AppLanguage.PORTUGUESE -> "Retomar"
        AppLanguage.SPANISH -> "Reanudar"
        AppLanguage.FRENCH -> "Reprendre"
        AppLanguage.GERMAN -> "Fortsetzen"
        AppLanguage.RUSSIAN -> "Продолжить"
        AppLanguage.JAPANESE -> "再開"
        AppLanguage.KOREAN -> "재개"
    }

    val depDlEta: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "剩余时间"
        AppLanguage.ENGLISH -> "Time Left"
        AppLanguage.ARABIC -> "الوقت المتبقي"
        AppLanguage.PORTUGUESE -> "Tempo Restante"
        AppLanguage.SPANISH -> "Tiempo Restante"
        AppLanguage.FRENCH -> "Temps Restant"
        AppLanguage.GERMAN -> "Verbleibende Zeit"
        AppLanguage.RUSSIAN -> "Оставшееся время"
        AppLanguage.JAPANESE -> "残り時間"
        AppLanguage.KOREAN -> "남은 시간"
    }

    val depDlPaused: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已暂停"
        AppLanguage.ENGLISH -> "Paused"
        AppLanguage.ARABIC -> "متوقف مؤقتًا"
        AppLanguage.PORTUGUESE -> "Pausado"
        AppLanguage.SPANISH -> "Pausado"
        AppLanguage.FRENCH -> "En pause"
        AppLanguage.GERMAN -> "Pausiert"
        AppLanguage.RUSSIAN -> "Приостановлено"
        AppLanguage.JAPANESE -> "一時停止中"
        AppLanguage.KOREAN -> "일시정지됨"
    }

    val depDlCancel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "取消下载"
        AppLanguage.ENGLISH -> "Cancel download"
        AppLanguage.ARABIC -> "إلغاء التنزيل"
        AppLanguage.PORTUGUESE -> "Cancelar download"
        AppLanguage.SPANISH -> "Cancelar descarga"
        AppLanguage.FRENCH -> "Annuler le téléchargement"
        AppLanguage.GERMAN -> "Download abbrechen"
        AppLanguage.RUSSIAN -> "Отменить загрузку"
        AppLanguage.JAPANESE -> "ダウンロードをキャンセル"
        AppLanguage.KOREAN -> "다운로드 취소"
    }

    val deleteConfirmTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "确认删除"
        AppLanguage.ENGLISH -> "Confirm Delete"
        AppLanguage.ARABIC -> "تأكيد الحذف"
        AppLanguage.PORTUGUESE -> "Confirmar Exclusão"
        AppLanguage.SPANISH -> "Confirmar Eliminación"
        AppLanguage.FRENCH -> "Confirmer la Suppression"
        AppLanguage.GERMAN -> "Löschen bestätigen"
        AppLanguage.RUSSIAN -> "Подтвердить удаление"
        AppLanguage.JAPANESE -> "削除の確認"
        AppLanguage.KOREAN -> "삭제 확인"
    }

    val deleteConfirmMessage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "确定要删除这个应用吗？"
        AppLanguage.ENGLISH -> "Are you sure you want to delete this app?"
        AppLanguage.ARABIC -> "هل أنت متأكد أنك تريد حذف هذا التطبيق؟"
        AppLanguage.PORTUGUESE -> "Tem certeza de que deseja excluir este app?"
        AppLanguage.SPANISH -> "¿Estás seguro de que quieres eliminar esta app?"
        AppLanguage.FRENCH -> "Voulez-vous vraiment supprimer cette application ?"
        AppLanguage.GERMAN -> "Möchtest du diese App wirklich löschen?"
        AppLanguage.RUSSIAN -> "Вы уверены, что хотите удалить это приложение?"
        AppLanguage.JAPANESE -> "このアプリを削除してもよろしいですか？"
        AppLanguage.KOREAN -> "이 앱을 삭제하시겠습니까?"
    }

    val buildDialogTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "构建 APK"
        AppLanguage.ENGLISH -> "Build APK"
        AppLanguage.ARABIC -> "بناء APK"
        AppLanguage.PORTUGUESE -> "Compilar APK"
        AppLanguage.SPANISH -> "Compilar APK"
        AppLanguage.FRENCH -> "Compiler l'APK"
        AppLanguage.GERMAN -> "APK erstellen"
        AppLanguage.RUSSIAN -> "Собрать APK"
        AppLanguage.JAPANESE -> "APK をビルド"
        AppLanguage.KOREAN -> "APK 빌드"
    }

    val buildEnvironment: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "构建环境"
        AppLanguage.ENGLISH -> "Build Environment"
        AppLanguage.ARABIC -> "بيئة البناء"
        AppLanguage.PORTUGUESE -> "Ambiente de Build"
        AppLanguage.SPANISH -> "Entorno de Compilación"
        AppLanguage.FRENCH -> "Environnement de Compilation"
        AppLanguage.GERMAN -> "Build-Umgebung"
        AppLanguage.RUSSIAN -> "Среда сборки"
        AppLanguage.JAPANESE -> "ビルド環境"
        AppLanguage.KOREAN -> "빌드 환경"
    }

    val envReady: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "环境就绪"
        AppLanguage.ENGLISH -> "Environment Ready"
        AppLanguage.ARABIC -> "البيئة جاهزة"
        AppLanguage.PORTUGUESE -> "Ambiente Pronto"
        AppLanguage.SPANISH -> "Entorno Listo"
        AppLanguage.FRENCH -> "Environnement Prêt"
        AppLanguage.GERMAN -> "Umgebung bereit"
        AppLanguage.RUSSIAN -> "Среда готова"
        AppLanguage.JAPANESE -> "環境の準備が完了"
        AppLanguage.KOREAN -> "환경 준비 완료"
    }

    val envNotInstalled: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "未安装"
        AppLanguage.ENGLISH -> "Not Installed"
        AppLanguage.ARABIC -> "غير مثبت"
        AppLanguage.PORTUGUESE -> "Não Instalado"
        AppLanguage.SPANISH -> "No Instalado"
        AppLanguage.FRENCH -> "Non Installé"
        AppLanguage.GERMAN -> "Nicht installiert"
        AppLanguage.RUSSIAN -> "Не установлено"
        AppLanguage.JAPANESE -> "未インストール"
        AppLanguage.KOREAN -> "설치되지 않음"
    }

    val envDownloading: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下载中"
        AppLanguage.ENGLISH -> "Downloading"
        AppLanguage.ARABIC -> "جاري التحميل"
        AppLanguage.PORTUGUESE -> "Baixando"
        AppLanguage.SPANISH -> "Descargando"
        AppLanguage.FRENCH -> "Téléchargement"
        AppLanguage.GERMAN -> "Wird heruntergeladen"
        AppLanguage.RUSSIAN -> "Загрузка"
        AppLanguage.JAPANESE -> "ダウンロード中"
        AppLanguage.KOREAN -> "다운로드 중"
    }

    val envInstalling: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "安装中"
        AppLanguage.ENGLISH -> "Installing"
        AppLanguage.ARABIC -> "جاري التثبيت"
        AppLanguage.PORTUGUESE -> "Instalando"
        AppLanguage.SPANISH -> "Instalando"
        AppLanguage.FRENCH -> "Installation"
        AppLanguage.GERMAN -> "Wird installiert"
        AppLanguage.RUSSIAN -> "Установка"
        AppLanguage.JAPANESE -> "インストール中"
        AppLanguage.KOREAN -> "설치 중"
    }

    val canBuildFrontend: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "可在本机安装依赖并构建前端项目"
        AppLanguage.ENGLISH -> "Can install deps and build frontend projects locally"
        AppLanguage.ARABIC -> "يمكن تثبيت التبعيات وبناء مشاريع الواجهة محليًا"
        AppLanguage.PORTUGUESE -> "Pode instalar dependências e compilar projetos frontend localmente"
        AppLanguage.SPANISH -> "Puede instalar dependencias y compilar proyectos frontend localmente"
        AppLanguage.FRENCH -> "Peut installer les dépendances et compiler les projets frontend localement"
        AppLanguage.GERMAN -> "Kann Abhängigkeiten installieren und Frontend-Projekte lokal bauen"
        AppLanguage.RUSSIAN -> "Может устанавливать зависимости и собирать фронтенд-проекты локально"
        AppLanguage.JAPANESE -> "依存関係をインストールし、フロントエンドプロジェクトをローカルでビルドできます"
        AppLanguage.KOREAN -> "로컬에서 종속성을 설치하고 프론트엔드 프로젝트를 빌드할 수 있습니다"
    }

    val builtInPackagerReady: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "请先安装本地 Node.js 构建工具链"
        AppLanguage.ENGLISH -> "Install the local Node.js build toolchain first"
        AppLanguage.ARABIC -> "ثبّت سلسلة أدوات بناء Node.js المحلية أولاً"
        AppLanguage.PORTUGUESE -> "Instale a toolchain de build Node.js local primeiro"
        AppLanguage.SPANISH -> "Instala la toolchain de compilación Node.js local primero"
        AppLanguage.FRENCH -> "Installez d'abord la toolchain de compilation Node.js locale"
        AppLanguage.GERMAN -> "Installiere zuerst die lokale Node.js-Build-Toolchain"
        AppLanguage.RUSSIAN -> "Сначала установите локальную toolchain сборки Node.js"
        AppLanguage.JAPANESE -> "まずローカルの Node.js ビルドツールチェーンをインストールしてください"
        AppLanguage.KOREAN -> "먼저 로컬 Node.js 빌드 툴체인을 설치하세요"
    }

    val installAdvancedBuildTool: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "安装本地构建环境"
        AppLanguage.ENGLISH -> "Install Local Build Environment"
        AppLanguage.ARABIC -> "تثبيت بيئة البناء المحلية"
        AppLanguage.PORTUGUESE -> "Instalar Ambiente de Build Local"
        AppLanguage.SPANISH -> "Instalar Entorno de Compilación Local"
        AppLanguage.FRENCH -> "Installer l'Environnement de Compilation Local"
        AppLanguage.GERMAN -> "Lokale Build-Umgebung installieren"
        AppLanguage.RUSSIAN -> "Установить локальную среду сборки"
        AppLanguage.JAPANESE -> "ローカルビルド環境をインストール"
        AppLanguage.KOREAN -> "로컬 빌드 환경 설치"
    }

    val nodeInstalledNpmMissing: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Node 已就绪，npm 未安装"
        AppLanguage.ENGLISH -> "Node ready, npm not installed"
        AppLanguage.ARABIC -> "Node جاهز، لم يتم تثبيت npm"
        AppLanguage.PORTUGUESE -> "Node pronto, npm não instalado"
        AppLanguage.SPANISH -> "Node listo, npm no instalado"
        AppLanguage.FRENCH -> "Node prêt, npm non installé"
        AppLanguage.GERMAN -> "Node bereit, npm nicht installiert"
        AppLanguage.RUSSIAN -> "Node готов, npm не установлен"
        AppLanguage.JAPANESE -> "Node の準備が完了、npm は未インストール"
        AppLanguage.KOREAN -> "Node 준비 완료, npm 미설치"
    }

    val nodeInstalledNpmMissingHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "继续安装 npm / pnpm / yarn 后即可进行本地构建"
        AppLanguage.ENGLISH -> "Install npm / pnpm / yarn to enable local builds"
        AppLanguage.ARABIC -> "ثبّت npm / pnpm / yarn لتفعيل البناء المحلي"
        AppLanguage.PORTUGUESE -> "Instale npm / pnpm / yarn para habilitar builds locais"
        AppLanguage.SPANISH -> "Instala npm / pnpm / yarn para habilitar compilaciones locales"
        AppLanguage.FRENCH -> "Installez npm / pnpm / yarn pour activer les compilations locales"
        AppLanguage.GERMAN -> "Installiere npm / pnpm / yarn, um lokale Builds zu aktivieren"
        AppLanguage.RUSSIAN -> "Установите npm / pnpm / yarn для локальной сборки"
        AppLanguage.JAPANESE -> "npm / pnpm / yarn をインストールするとローカルビルドが有効になります"
        AppLanguage.KOREAN -> "npm / pnpm / yarn을 설치하면 로컬 빌드를 사용할 수 있습니다"
    }

    val preparingBuildEnv: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "准备本地构建环境"
        AppLanguage.ENGLISH -> "Preparing local build environment"
        AppLanguage.ARABIC -> "تجهيز بيئة البناء المحلية"
        AppLanguage.PORTUGUESE -> "Preparando ambiente de build local"
        AppLanguage.SPANISH -> "Preparando entorno de compilación local"
        AppLanguage.FRENCH -> "Préparation de l'environnement de compilation local"
        AppLanguage.GERMAN -> "Lokale Build-Umgebung wird vorbereitet"
        AppLanguage.RUSSIAN -> "Подготовка локальной среды сборки"
        AppLanguage.JAPANESE -> "ローカルビルド環境を準備中"
        AppLanguage.KOREAN -> "로컬 빌드 환경 준비 중"
    }

    val buildEnvNotFullyReady: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "构建环境未完全就绪"
        AppLanguage.ENGLISH -> "Build environment is not fully ready"
        AppLanguage.ARABIC -> "بيئة البناء ليست جاهزة بالكامل"
        AppLanguage.PORTUGUESE -> "Ambiente de build não está totalmente pronto"
        AppLanguage.SPANISH -> "El entorno de compilación no está completamente listo"
        AppLanguage.FRENCH -> "L'environnement de compilation n'est pas entièrement prêt"
        AppLanguage.GERMAN -> "Build-Umgebung ist nicht vollständig bereit"
        AppLanguage.RUSSIAN -> "Среда сборки не полностью готова"
        AppLanguage.JAPANESE -> "ビルド環境が完全ではありません"
        AppLanguage.KOREAN -> "빌드 환경이 완전히 준비되지 않았습니다"
    }

    val nodeLauncherUnavailable: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Node 启动器不可用"
        AppLanguage.ENGLISH -> "Node launcher unavailable"
        AppLanguage.ARABIC -> "مشغّل Node غير متاح"
        AppLanguage.PORTUGUESE -> "Iniciador Node indisponível"
        AppLanguage.SPANISH -> "Lanzador Node no disponible"
        AppLanguage.FRENCH -> "Lanceur Node indisponible"
        AppLanguage.GERMAN -> "Node-Starter nicht verfügbar"
        AppLanguage.RUSSIAN -> "Запуск Node недоступен"
        AppLanguage.JAPANESE -> "Node ランチャーは利用できません"
        AppLanguage.KOREAN -> "Node 런처를 사용할 수 없습니다"
    }

    val npmUnavailable: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "npm 不可用"
        AppLanguage.ENGLISH -> "npm unavailable"
        AppLanguage.ARABIC -> "npm غير متاح"
        AppLanguage.PORTUGUESE -> "npm indisponível"
        AppLanguage.SPANISH -> "npm no disponible"
        AppLanguage.FRENCH -> "npm indisponible"
        AppLanguage.GERMAN -> "npm nicht verfügbar"
        AppLanguage.RUSSIAN -> "npm недоступен"
        AppLanguage.JAPANESE -> "npm は利用できません"
        AppLanguage.KOREAN -> "npm을 사용할 수 없습니다"
    }

    val localBuildEnvNotReady: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "本地构建环境未就绪"
        AppLanguage.ENGLISH -> "Local build environment is not ready"
        AppLanguage.ARABIC -> "بيئة البناء المحلية ليست جاهزة"
        AppLanguage.PORTUGUESE -> "Ambiente de build local não está pronto"
        AppLanguage.SPANISH -> "El entorno de compilación local no está listo"
        AppLanguage.FRENCH -> "L'environnement de compilation local n'est pas prêt"
        AppLanguage.GERMAN -> "Lokale Build-Umgebung ist nicht bereit"
        AppLanguage.RUSSIAN -> "Локальная среда сборки не готова"
        AppLanguage.JAPANESE -> "ローカルビルド環境の準備ができていません"
        AppLanguage.KOREAN -> "로컬 빌드 환경이 준비되지 않았습니다"
    }

    val buildTools: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "构建工具"
        AppLanguage.ENGLISH -> "Build Tools"
        AppLanguage.ARABIC -> "أدوات البناء"
        AppLanguage.PORTUGUESE -> "Ferramentas de Build"
        AppLanguage.SPANISH -> "Herramientas de Compilación"
        AppLanguage.FRENCH -> "Outils de Compilation"
        AppLanguage.GERMAN -> "Build-Tools"
        AppLanguage.RUSSIAN -> "Инструменты сборки"
        AppLanguage.JAPANESE -> "ビルドツール"
        AppLanguage.KOREAN -> "빌드 도구"
    }

    val installed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已安装"
        AppLanguage.ENGLISH -> "Installed"
        AppLanguage.ARABIC -> "مثبت"
        AppLanguage.PORTUGUESE -> "Instalado"
        AppLanguage.SPANISH -> "Instalado"
        AppLanguage.FRENCH -> "Installé"
        AppLanguage.GERMAN -> "Installiert"
        AppLanguage.RUSSIAN -> "Установлено"
        AppLanguage.JAPANESE -> "インストール済み"
        AppLanguage.KOREAN -> "설치됨"
    }

    val notInstalled: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "未安装"
        AppLanguage.ENGLISH -> "Not Installed"
        AppLanguage.ARABIC -> "غير مثبت"
        AppLanguage.PORTUGUESE -> "Não Instalado"
        AppLanguage.SPANISH -> "No Instalado"
        AppLanguage.FRENCH -> "Non Installé"
        AppLanguage.GERMAN -> "Nicht installiert"
        AppLanguage.RUSSIAN -> "Не установлено"
        AppLanguage.JAPANESE -> "未インストール"
        AppLanguage.KOREAN -> "설치되지 않음"
    }

    val ready: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已就绪"
        AppLanguage.ENGLISH -> "Ready"
        AppLanguage.ARABIC -> "جاهز"
        AppLanguage.PORTUGUESE -> "Pronto"
        AppLanguage.SPANISH -> "Listo"
        AppLanguage.FRENCH -> "Prêt"
        AppLanguage.GERMAN -> "Bereit"
        AppLanguage.RUSSIAN -> "Готово"
        AppLanguage.JAPANESE -> "準備完了"
        AppLanguage.KOREAN -> "준비 완료"
    }

    val cache: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "缓存"
        AppLanguage.ENGLISH -> "Cache"
        AppLanguage.ARABIC -> "ذاكرة التخزين المؤقت"
        AppLanguage.PORTUGUESE -> "Cache"
        AppLanguage.SPANISH -> "Caché"
        AppLanguage.FRENCH -> "Cache"
        AppLanguage.GERMAN -> "Zwischenspeicher"
        AppLanguage.RUSSIAN -> "Кэш"
        AppLanguage.JAPANESE -> "キャッシュ"
        AppLanguage.KOREAN -> "캐시"
    }

    val supportedFeatures: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "支持的功能"
        AppLanguage.ENGLISH -> "Supported Features"
        AppLanguage.ARABIC -> "الميزات المدعومة"
        AppLanguage.PORTUGUESE -> "Recursos Suportados"
        AppLanguage.SPANISH -> "Características Soportadas"
        AppLanguage.FRENCH -> "Fonctionnalités Prises en Charge"
        AppLanguage.GERMAN -> "Unterstützte Funktionen"
        AppLanguage.RUSSIAN -> "Поддерживаемые функции"
        AppLanguage.JAPANESE -> "対応機能"
        AppLanguage.KOREAN -> "지원 기능"
    }

    val resetEnvironment: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "重置环境"
        AppLanguage.ENGLISH -> "Reset Environment"
        AppLanguage.ARABIC -> "إعادة تعيين البيئة"
        AppLanguage.PORTUGUESE -> "Redefinir Ambiente"
        AppLanguage.SPANISH -> "Restablecer Entorno"
        AppLanguage.FRENCH -> "Réinitialiser l'Environnement"
        AppLanguage.GERMAN -> "Umgebung zurücksetzen"
        AppLanguage.RUSSIAN -> "Сбросить среду"
        AppLanguage.JAPANESE -> "環境をリセット"
        AppLanguage.KOREAN -> "환경 재설정"
    }

    val resetEnvConfirm: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "这将删除已下载的构建工具。确定要继续吗？"
        AppLanguage.ENGLISH -> "This will delete downloaded build tools. Are you sure?"
        AppLanguage.ARABIC -> "سيؤدي هذا إلى حذف أدوات البناء المحملة. هل أنت متأكد؟"
        AppLanguage.PORTUGUESE -> "Isso excluirá as ferramentas de build baixadas. Tem certeza?"
        AppLanguage.SPANISH -> "Esto eliminará las herramientas de compilación descargadas. ¿Estás seguro?"
        AppLanguage.FRENCH -> "Cela supprimera les outils de compilation téléchargés. Êtes-vous sûr ?"
        AppLanguage.GERMAN -> "Dies löscht heruntergeladene Build-Tools. Bist du sicher?"
        AppLanguage.RUSSIAN -> "Это удалит загруженные инструменты сборки. Вы уверены?"
        AppLanguage.JAPANESE -> "ダウンロードしたビルドツールが削除されます。よろしいですか？"
        AppLanguage.KOREAN -> "다운로드한 빌드 도구가 삭제됩니다. 확실하시겠습니까?"
    }

    val clearCacheTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "清理缓存"
        AppLanguage.ENGLISH -> "Clear Cache"
        AppLanguage.ARABIC -> "مسح ذاكرة التخزين المؤقت"
        AppLanguage.PORTUGUESE -> "Limpar Cache"
        AppLanguage.SPANISH -> "Borrar Caché"
        AppLanguage.FRENCH -> "Vider le Cache"
        AppLanguage.GERMAN -> "Cache löschen"
        AppLanguage.RUSSIAN -> "Очистить кэш"
        AppLanguage.JAPANESE -> "キャッシュをクリア"
        AppLanguage.KOREAN -> "캐시 지우기"
    }

    val clearCacheConfirm: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "这将清理构建缓存和临时文件。"
        AppLanguage.ENGLISH -> "This will clear build cache and temporary files."
        AppLanguage.ARABIC -> "سيؤدي هذا إلى مسح ذاكرة التخزين المؤقت للبناء والملفات المؤقتة."
        AppLanguage.PORTUGUESE -> "Isso limpará o cache de build e os arquivos temporários."
        AppLanguage.SPANISH -> "Esto borrará la caché de compilación y los archivos temporales."
        AppLanguage.FRENCH -> "Cela videra le cache de compilation et les fichiers temporaires."
        AppLanguage.GERMAN -> "Dies löscht den Build-Cache und temporäre Dateien."
        AppLanguage.RUSSIAN -> "Это очистит кэш сборки и временные файлы."
        AppLanguage.JAPANESE -> "ビルドキャッシュと一時ファイルをクリアします。"
        AppLanguage.KOREAN -> "빌드 캐시와 임시 파일을 지웁니다."
    }

    val clean: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "清理"
        AppLanguage.ENGLISH -> "Clean"
        AppLanguage.ARABIC -> "تنظيف"
        AppLanguage.PORTUGUESE -> "Limpar"
        AppLanguage.SPANISH -> "Limpiar"
        AppLanguage.FRENCH -> "Nettoyer"
        AppLanguage.GERMAN -> "Bereinigen"
        AppLanguage.RUSSIAN -> "Очистить"
        AppLanguage.JAPANESE -> "クリーン"
        AppLanguage.KOREAN -> "정리"
    }

    val selectProject: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择项目"
        AppLanguage.ENGLISH -> "Select Project"
        AppLanguage.ARABIC -> "اختيار المشروع"
        AppLanguage.PORTUGUESE -> "Selecionar Projeto"
        AppLanguage.SPANISH -> "Seleccionar Proyecto"
        AppLanguage.FRENCH -> "Sélectionner le Projet"
        AppLanguage.GERMAN -> "Projekt auswählen"
        AppLanguage.RUSSIAN -> "Выбрать проект"
        AppLanguage.JAPANESE -> "プロジェクトを選択"
        AppLanguage.KOREAN -> "프로젝트 선택"
    }

    val selectProjectFolder: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择项目文件夹"
        AppLanguage.ENGLISH -> "Select Project Folder"
        AppLanguage.ARABIC -> "اختيار مجلد المشروع"
        AppLanguage.PORTUGUESE -> "Selecionar Pasta do Projeto"
        AppLanguage.SPANISH -> "Seleccionar Carpeta del Proyecto"
        AppLanguage.FRENCH -> "Sélectionner le Dossier du Projet"
        AppLanguage.GERMAN -> "Projektordner auswählen"
        AppLanguage.RUSSIAN -> "Выбрать папку проекта"
        AppLanguage.JAPANESE -> "プロジェクトフォルダを選択"
        AppLanguage.KOREAN -> "프로젝트 폴더 선택"
    }

    val selectProjectHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择项目根目录或构建输出目录（dist/build）"
        AppLanguage.ENGLISH -> "Select project root or build output directory (dist/build)"
        AppLanguage.ARABIC -> "اختر جذر المشروع أو دليل إخراج البناء (dist/build)"
        AppLanguage.PORTUGUESE -> "Selecione a raiz do projeto ou o diretório de saída de build (dist/build)"
        AppLanguage.SPANISH -> "Selecciona la raíz del proyecto o el directorio de salida de compilación (dist/build)"
        AppLanguage.FRENCH -> "Sélectionnez la racine du projet ou le répertoire de sortie de compilation (dist/build)"
        AppLanguage.GERMAN -> "Projekt-Root oder Build-Output-Verzeichnis auswählen (dist/build)"
        AppLanguage.RUSSIAN -> "Выберите корень проекта или каталог вывода сборки (dist/build)"
        AppLanguage.JAPANESE -> "プロジェクトのルートまたはビルド出力ディレクトリ (dist/build) を選択"
        AppLanguage.KOREAN -> "프로젝트 루트 또는 빌드 출력 디렉터리(dist/build)를 선택하세요"
    }

    val projectAnalysis: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "项目检查"
        AppLanguage.ENGLISH -> "Project Check"
        AppLanguage.ARABIC -> "فحص المشروع"
        AppLanguage.PORTUGUESE -> "Verificação do Projeto"
        AppLanguage.SPANISH -> "Verificación del Proyecto"
        AppLanguage.FRENCH -> "Vérification du Projet"
        AppLanguage.GERMAN -> "Projektprüfung"
        AppLanguage.RUSSIAN -> "Проверка проекта"
        AppLanguage.JAPANESE -> "プロジェクトチェック"
        AppLanguage.KOREAN -> "프로젝트 확인"
    }

    val appConfig: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "应用配置"
        AppLanguage.ENGLISH -> "App Config"
        AppLanguage.ARABIC -> "إعدادات التطبيق"
        AppLanguage.PORTUGUESE -> "Configuração do App"
        AppLanguage.SPANISH -> "Configuración de la App"
        AppLanguage.FRENCH -> "Configuration de l'App"
        AppLanguage.GERMAN -> "App-Konfiguration"
        AppLanguage.RUSSIAN -> "Конфигурация приложения"
        AppLanguage.JAPANESE -> "アプリ設定"
        AppLanguage.KOREAN -> "앱 설정"
    }

    val importProject: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "导入项目"
        AppLanguage.ENGLISH -> "Import Project"
        AppLanguage.ARABIC -> "استيراد المشروع"
        AppLanguage.PORTUGUESE -> "Importar Projeto"
        AppLanguage.SPANISH -> "Importar Proyecto"
        AppLanguage.FRENCH -> "Importer le Projet"
        AppLanguage.GERMAN -> "Projekt importieren"
        AppLanguage.RUSSIAN -> "Импортировать проект"
        AppLanguage.JAPANESE -> "プロジェクトをインポート"
        AppLanguage.KOREAN -> "프로젝트 가져오기"
    }

    val reimportProject: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "重新导入项目"
        AppLanguage.ENGLISH -> "Re-import Project"
        AppLanguage.ARABIC -> "إعادة استيراد المشروع"
        AppLanguage.PORTUGUESE -> "Reimportar Projeto"
        AppLanguage.SPANISH -> "Reimportar Proyecto"
        AppLanguage.FRENCH -> "Réimporter le Projet"
        AppLanguage.GERMAN -> "Projekt neu importieren"
        AppLanguage.RUSSIAN -> "Повторно импортировать проект"
        AppLanguage.JAPANESE -> "プロジェクトを再インポート"
        AppLanguage.KOREAN -> "프로젝트 다시 가져오기"
    }

    val buildProject: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "构建项目"
        AppLanguage.ENGLISH -> "Build Project"
        AppLanguage.ARABIC -> "بناء المشروع"
        AppLanguage.PORTUGUESE -> "Compilar Projeto"
        AppLanguage.SPANISH -> "Compilar Proyecto"
        AppLanguage.FRENCH -> "Compiler le Projet"
        AppLanguage.GERMAN -> "Projekt bauen"
        AppLanguage.RUSSIAN -> "Собрать проект"
        AppLanguage.JAPANESE -> "プロジェクトをビルド"
        AppLanguage.KOREAN -> "프로젝트 빌드"
    }

    val rebuildProject: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "重新构建项目"
        AppLanguage.ENGLISH -> "Rebuild Project"
        AppLanguage.ARABIC -> "إعادة بناء المشروع"
        AppLanguage.PORTUGUESE -> "Recompilar Projeto"
        AppLanguage.SPANISH -> "Recompilar Proyecto"
        AppLanguage.FRENCH -> "Recompiler le Projet"
        AppLanguage.GERMAN -> "Projekt neu bauen"
        AppLanguage.RUSSIAN -> "Пересобрать проект"
        AppLanguage.JAPANESE -> "プロジェクトを再ビルド"
        AppLanguage.KOREAN -> "프로젝트 다시 빌드"
    }

    val scanningProject: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "扫描项目中..."
        AppLanguage.ENGLISH -> "Scanning project..."
        AppLanguage.ARABIC -> "جاري فحص المشروع..."
        AppLanguage.PORTUGUESE -> "Escaneando projeto..."
        AppLanguage.SPANISH -> "Escaneando proyecto..."
        AppLanguage.FRENCH -> "Analyse du projet..."
        AppLanguage.GERMAN -> "Projekt wird gescannt..."
        AppLanguage.RUSSIAN -> "Сканирование проекта..."
        AppLanguage.JAPANESE -> "プロジェクトをスキャン中..."
        AppLanguage.KOREAN -> "프로젝트 스캔 중..."
    }

    val importing: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "导入中"
        AppLanguage.ENGLISH -> "Importing"
        AppLanguage.ARABIC -> "جاري الاستيراد"
        AppLanguage.PORTUGUESE -> "Importando"
        AppLanguage.SPANISH -> "Importando"
        AppLanguage.FRENCH -> "Importation"
        AppLanguage.GERMAN -> "Wird importiert"
        AppLanguage.RUSSIAN -> "Импорт"
        AppLanguage.JAPANESE -> "インポート中"
        AppLanguage.KOREAN -> "가져오는 중"
    }

    val checkingEnv: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "检查环境..."
        AppLanguage.ENGLISH -> "Checking environment..."
        AppLanguage.ARABIC -> "جاري فحص البيئة..."
        AppLanguage.PORTUGUESE -> "Verificando ambiente..."
        AppLanguage.SPANISH -> "Verificando entorno..."
        AppLanguage.FRENCH -> "Vérification de l'environnement..."
        AppLanguage.GERMAN -> "Umgebung wird geprüft..."
        AppLanguage.RUSSIAN -> "Проверка среды..."
        AppLanguage.JAPANESE -> "環境を確認中..."
        AppLanguage.KOREAN -> "환경 확인 중..."
    }

    val copyingProjectFiles: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "复制项目文件"
        AppLanguage.ENGLISH -> "Copying project files"
        AppLanguage.ARABIC -> "نسخ ملفات المشروع"
        AppLanguage.PORTUGUESE -> "Copiando arquivos do projeto"
        AppLanguage.SPANISH -> "Copiando archivos del proyecto"
        AppLanguage.FRENCH -> "Copie des fichiers du projet"
        AppLanguage.GERMAN -> "Projektdateien werden kopiert"
        AppLanguage.RUSSIAN -> "Копирование файлов проекта"
        AppLanguage.JAPANESE -> "プロジェクトファイルをコピー中"
        AppLanguage.KOREAN -> "프로젝트 파일 복사 중"
    }

    val installingDeps: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "安装依赖"
        AppLanguage.ENGLISH -> "Installing dependencies"
        AppLanguage.ARABIC -> "تثبيت التبعيات"
        AppLanguage.PORTUGUESE -> "Instalando dependências"
        AppLanguage.SPANISH -> "Instalando dependencias"
        AppLanguage.FRENCH -> "Installation des dépendances"
        AppLanguage.GERMAN -> "Abhängigkeiten werden installiert"
        AppLanguage.RUSSIAN -> "Установка зависимостей"
        AppLanguage.JAPANESE -> "依存関係をインストール中"
        AppLanguage.KOREAN -> "종속성 설치 중"
    }

    val building: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "构建中"
        AppLanguage.ENGLISH -> "Building"
        AppLanguage.ARABIC -> "جاري البناء"
        AppLanguage.PORTUGUESE -> "Compilando"
        AppLanguage.SPANISH -> "Compilando"
        AppLanguage.FRENCH -> "Compilation"
        AppLanguage.GERMAN -> "Wird gebaut"
        AppLanguage.RUSSIAN -> "Сборка"
        AppLanguage.JAPANESE -> "ビルド中"
        AppLanguage.KOREAN -> "빌드 중"
    }

    val processingOutput: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "处理构建产物..."
        AppLanguage.ENGLISH -> "Processing build output..."
        AppLanguage.ARABIC -> "معالجة مخرجات البناء..."
        AppLanguage.PORTUGUESE -> "Processando saída de build..."
        AppLanguage.SPANISH -> "Procesando salida de compilación..."
        AppLanguage.FRENCH -> "Traitement de la sortie de compilation..."
        AppLanguage.GERMAN -> "Build-Output wird verarbeitet..."
        AppLanguage.RUSSIAN -> "Обработка вывода сборки..."
        AppLanguage.JAPANESE -> "ビルド出力を処理中..."
        AppLanguage.KOREAN -> "빌드 출력 처리 중..."
    }

    val completed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "完成"
        AppLanguage.ENGLISH -> "Completed"
        AppLanguage.ARABIC -> "مكتمل"
        AppLanguage.PORTUGUESE -> "Concluído"
        AppLanguage.SPANISH -> "Completado"
        AppLanguage.FRENCH -> "Terminé"
        AppLanguage.GERMAN -> "Abgeschlossen"
        AppLanguage.RUSSIAN -> "Завершено"
        AppLanguage.JAPANESE -> "完了"
        AppLanguage.KOREAN -> "완료"
    }

    val failed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "失败"
        AppLanguage.ENGLISH -> "Failed"
        AppLanguage.ARABIC -> "فشل"
        AppLanguage.PORTUGUESE -> "Falhou"
        AppLanguage.SPANISH -> "Falló"
        AppLanguage.FRENCH -> "Échoué"
        AppLanguage.GERMAN -> "Fehlgeschlagen"
        AppLanguage.RUSSIAN -> "Ошибка"
        AppLanguage.JAPANESE -> "失敗"
        AppLanguage.KOREAN -> "실패"
    }

    val totalFiles: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "共 %d 个文件"
        AppLanguage.ENGLISH -> "%d files total"
        AppLanguage.ARABIC -> "إجمالي %d ملفات"
        AppLanguage.PORTUGUESE -> "%d arquivos no total"
        AppLanguage.SPANISH -> "%d archivos en total"
        AppLanguage.FRENCH -> "%d fichiers au total"
        AppLanguage.GERMAN -> "%d Dateien insgesamt"
        AppLanguage.RUSSIAN -> "Всего %d файлов"
        AppLanguage.JAPANESE -> "合計 %d ファイル"
        AppLanguage.KOREAN -> "총 %d개 파일"
    }

    val logs: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "日志"
        AppLanguage.ENGLISH -> "Logs"
        AppLanguage.ARABIC -> "السجلات"
        AppLanguage.PORTUGUESE -> "Registros"
        AppLanguage.SPANISH -> "Registros"
        AppLanguage.FRENCH -> "Journaux"
        AppLanguage.GERMAN -> "Protokolle"
        AppLanguage.RUSSIAN -> "Журналы"
        AppLanguage.JAPANESE -> "ログ"
        AppLanguage.KOREAN -> "로그"
    }

    val importLogs: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "导入日志"
        AppLanguage.ENGLISH -> "Import Logs"
        AppLanguage.ARABIC -> "سجلات الاستيراد"
        AppLanguage.PORTUGUESE -> "Logs de Importação"
        AppLanguage.SPANISH -> "Registros de Importación"
        AppLanguage.FRENCH -> "Journaux d'Importation"
        AppLanguage.GERMAN -> "Import-Protokolle"
        AppLanguage.RUSSIAN -> "Журналы импорта"
        AppLanguage.JAPANESE -> "インポートログ"
        AppLanguage.KOREAN -> "가져오기 로그"
    }

    val importFrontendProject: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "导入前端项目"
        AppLanguage.ENGLISH -> "Import Frontend Project"
        AppLanguage.ARABIC -> "استيراد مشروع الواجهة الأمامية"
        AppLanguage.PORTUGUESE -> "Importar Projeto Frontend"
        AppLanguage.SPANISH -> "Importar Proyecto Frontend"
        AppLanguage.FRENCH -> "Importer un Projet Frontend"
        AppLanguage.GERMAN -> "Frontend-Projekt importieren"
        AppLanguage.RUSSIAN -> "Импортировать фронтенд-проект"
        AppLanguage.JAPANESE -> "フロントエンドプロジェクトをインポート"
        AppLanguage.KOREAN -> "프론트엔드 프로젝트 가져오기"
    }

    val supportVueReactVite: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "支持 Vue、React、Vite 等已构建的项目"
        AppLanguage.ENGLISH -> "Supports built Vue, React, Vite projects"
        AppLanguage.ARABIC -> "يدعم مشاريع Vue و React و Vite المبنية"
        AppLanguage.PORTUGUESE -> "Suporta projetos Vue, React, Vite já compilados"
        AppLanguage.SPANISH -> "Soporta proyectos Vue, React, Vite ya compilados"
        AppLanguage.FRENCH -> "Prend en charge les projets Vue, React, Vite déjà compilés"
        AppLanguage.GERMAN -> "Unterstützt gebaute Vue-, React-, Vite-Projekte"
        AppLanguage.RUSSIAN -> "Поддерживает собранные проекты Vue, React, Vite"
        AppLanguage.JAPANESE -> "ビルド済みの Vue、React、Vite プロジェクトに対応"
        AppLanguage.KOREAN -> "빌드된 Vue, React, Vite 프로젝트를 지원합니다"
    }

    val usageSteps: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "使用步骤"
        AppLanguage.ENGLISH -> "Usage Steps"
        AppLanguage.ARABIC -> "خطوات الاستخدام"
        AppLanguage.PORTUGUESE -> "Passos de Uso"
        AppLanguage.SPANISH -> "Pasos de Uso"
        AppLanguage.FRENCH -> "Étapes d'Utilisation"
        AppLanguage.GERMAN -> "Verwendungsschritte"
        AppLanguage.RUSSIAN -> "Шаги использования"
        AppLanguage.JAPANESE -> "使用手順"
        AppLanguage.KOREAN -> "사용 단계"
    }

    val usageStepsContent: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "1. 选择项目目录\n2. 安装本地构建环境\n3. 在手机上直接安装依赖并执行 build"
        AppLanguage.ENGLISH -> "1. Select the project folder\n2. Install the local build environment\n3. Install deps and run build on-device"
        AppLanguage.ARABIC -> "1. اختر مجلد المشروع\n2. ثبّت بيئة البناء المحلية\n3. ثبّت التبعيات وشغّل build على الجهاز"
        AppLanguage.PORTUGUESE -> "1. Selecione a pasta do projeto\n2. Instale o ambiente de build local\n3. Instale as dependências e execute o build no dispositivo"
        AppLanguage.SPANISH -> "1. Selecciona la carpeta del proyecto\n2. Instala el entorno de compilación local\n3. Instala las dependencias y ejecuta la compilación en el dispositivo"
        AppLanguage.FRENCH -> "1. Sélectionnez le dossier du projet\n2. Installez l'environnement de compilation local\n3. Installez les dépendances et lancez la compilation sur l'appareil"
        AppLanguage.GERMAN -> "1. Projektordner auswählen\n2. Lokale Build-Umgebung installieren\n3. Abhängigkeiten installieren und Build auf dem Gerät ausführen"
        AppLanguage.RUSSIAN -> "1. Выберите папку проекта\n2. Установите локальную среду сборки\n3. Установите зависимости и запустите сборку на устройстве"
        AppLanguage.JAPANESE -> "1. プロジェクトフォルダを選択\n2. ローカルビルド環境をインストール\n3. デバイスで依存関係をインストールしてビルドを実行"
        AppLanguage.KOREAN -> "1. 프로젝트 폴더 선택\n2. 로컬 빌드 환경 설치\n3. 기기에서 종속성을 설치하고 빌드 실행"
    }

    val builtInEngineReady: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "支持在本地安装依赖并执行 package.json 的构建脚本，也支持直接导入已构建产物。"
        AppLanguage.ENGLISH -> "Supports local dependency install and package.json build scripts, and can also import prebuilt output."
        AppLanguage.ARABIC -> "يدعم تثبيت التبعيات محليًا وتشغيل أوامر البناء من package.json، كما يدعم استيراد المخرجات المبنية."
        AppLanguage.PORTUGUESE -> "Suporta instalação local de dependências e scripts de build do package.json, e também pode importar saídas pré-compiladas."
        AppLanguage.SPANISH -> "Soporta instalación local de dependencias y scripts de compilación de package.json, y también puede importar salida precompilada."
        AppLanguage.FRENCH -> "Prend en charge l'installation locale des dépendances et les scripts de compilation de package.json, et peut aussi importer une sortie précompilée."
        AppLanguage.GERMAN -> "Unterstützt lokale Abhängigkeitsinstallation und package.json-Build-Skripte und kann auch vorgebaute Ausgaben importieren."
        AppLanguage.RUSSIAN -> "Поддерживает локальную установку зависимостей и скрипты сборки package.json, а также импорт готовых сборок."
        AppLanguage.JAPANESE -> "ローカルでの依存関係のインストールと package.json のビルドスクリプトに対応し、ビルド済み出力のインポートも可能です。"
        AppLanguage.KOREAN -> "로컬 종속성 설치와 package.json 빌드 스크립트를 지원하며, 미리 빌드된 출력을 가져올 수도 있습니다."
    }

    val createMediaAppTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "创建媒体应用"
        AppLanguage.ENGLISH -> "Create Media App"
        AppLanguage.ARABIC -> "إنشاء تطبيق وسائط"
        AppLanguage.PORTUGUESE -> "Criar App de Mídia"
        AppLanguage.SPANISH -> "Crear App de Medios"
        AppLanguage.FRENCH -> "Créer une App Média"
        AppLanguage.GERMAN -> "Medien-App erstellen"
        AppLanguage.RUSSIAN -> "Создать медиа-приложение"
        AppLanguage.JAPANESE -> "メディアアプリを作成"
        AppLanguage.KOREAN -> "미디어 앱 만들기"
    }

    val selectMediaType: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择媒体类型"
        AppLanguage.ENGLISH -> "Select Media Type"
        AppLanguage.ARABIC -> "اختيار نوع الوسائط"
        AppLanguage.PORTUGUESE -> "Selecionar Tipo de Mídia"
        AppLanguage.SPANISH -> "Seleccionar Tipo de Medio"
        AppLanguage.FRENCH -> "Sélectionner le Type de Média"
        AppLanguage.GERMAN -> "Medientyp auswählen"
        AppLanguage.RUSSIAN -> "Выбрать тип медиа"
        AppLanguage.JAPANESE -> "メディアタイプを選択"
        AppLanguage.KOREAN -> "미디어 유형 선택"
    }

    val image: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "图片"
        AppLanguage.ENGLISH -> "Image"
        AppLanguage.ARABIC -> "صورة"
        AppLanguage.PORTUGUESE -> "Imagem"
        AppLanguage.SPANISH -> "Imagen"
        AppLanguage.FRENCH -> "Image"
        AppLanguage.GERMAN -> "Bild"
        AppLanguage.RUSSIAN -> "Изображение"
        AppLanguage.JAPANESE -> "画像"
        AppLanguage.KOREAN -> "이미지"
    }

    val video: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "视频"
        AppLanguage.ENGLISH -> "Video"
        AppLanguage.ARABIC -> "فيديو"
        AppLanguage.PORTUGUESE -> "Vídeo"
        AppLanguage.SPANISH -> "Vídeo"
        AppLanguage.FRENCH -> "Vidéo"
        AppLanguage.GERMAN -> "Video"
        AppLanguage.RUSSIAN -> "Видео"
        AppLanguage.JAPANESE -> "動画"
        AppLanguage.KOREAN -> "동영상"
    }

    val selectImage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择图片"
        AppLanguage.ENGLISH -> "Select Image"
        AppLanguage.ARABIC -> "اختيار صورة"
        AppLanguage.PORTUGUESE -> "Selecionar Imagem"
        AppLanguage.SPANISH -> "Seleccionar Imagen"
        AppLanguage.FRENCH -> "Sélectionner une Image"
        AppLanguage.GERMAN -> "Bild auswählen"
        AppLanguage.RUSSIAN -> "Выбрать изображение"
        AppLanguage.JAPANESE -> "画像を選択"
        AppLanguage.KOREAN -> "이미지 선택"
    }

    val selectVideo: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择视频"
        AppLanguage.ENGLISH -> "Select Video"
        AppLanguage.ARABIC -> "اختيار فيديو"
        AppLanguage.PORTUGUESE -> "Selecionar Vídeo"
        AppLanguage.SPANISH -> "Seleccionar Video"
        AppLanguage.FRENCH -> "Sélectionner une Vidéo"
        AppLanguage.GERMAN -> "Video auswählen"
        AppLanguage.RUSSIAN -> "Выбрать видео"
        AppLanguage.JAPANESE -> "動画を選択"
        AppLanguage.KOREAN -> "동영상 선택"
    }

    val clickToSelectImage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "点击选择图片"
        AppLanguage.ENGLISH -> "Click to select image"
        AppLanguage.ARABIC -> "انقر لاختيار صورة"
        AppLanguage.PORTUGUESE -> "Toque para selecionar imagem"
        AppLanguage.SPANISH -> "Toca para seleccionar imagen"
        AppLanguage.FRENCH -> "Toucher pour sélectionner une image"
        AppLanguage.GERMAN -> "Tippen, um Bild auszuwählen"
        AppLanguage.RUSSIAN -> "Нажмите, чтобы выбрать изображение"
        AppLanguage.JAPANESE -> "タップして画像を選択"
        AppLanguage.KOREAN -> "탭하여 이미지 선택"
    }

    val clickToSelectVideo: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "点击选择视频"
        AppLanguage.ENGLISH -> "Click to select video"
        AppLanguage.ARABIC -> "انقر لاختيار فيديو"
        AppLanguage.PORTUGUESE -> "Toque para selecionar vídeo"
        AppLanguage.SPANISH -> "Toca para seleccionar video"
        AppLanguage.FRENCH -> "Toucher pour sélectionner une vidéo"
        AppLanguage.GERMAN -> "Tippen, um Video auszuwählen"
        AppLanguage.RUSSIAN -> "Нажмите, чтобы выбрать видео"
        AppLanguage.JAPANESE -> "タップして動画を選択"
        AppLanguage.KOREAN -> "탭하여 동영상 선택"
    }

    val videoSelected: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "视频已选择"
        AppLanguage.ENGLISH -> "Video selected"
        AppLanguage.ARABIC -> "تم اختيار الفيديو"
        AppLanguage.PORTUGUESE -> "Vídeo selecionado"
        AppLanguage.SPANISH -> "Video seleccionado"
        AppLanguage.FRENCH -> "Vidéo sélectionnée"
        AppLanguage.GERMAN -> "Video ausgewählt"
        AppLanguage.RUSSIAN -> "Видео выбрано"
        AppLanguage.JAPANESE -> "動画が選択されました"
        AppLanguage.KOREAN -> "동영상이 선택되었습니다"
    }

    val fillScreen: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "铺满屏幕"
        AppLanguage.ENGLISH -> "Fill Screen"
        AppLanguage.ARABIC -> "ملء الشاشة"
        AppLanguage.PORTUGUESE -> "Preencher Tela"
        AppLanguage.SPANISH -> "Llenar Pantalla"
        AppLanguage.FRENCH -> "Remplir l'Écran"
        AppLanguage.GERMAN -> "Bildschirm füllen"
        AppLanguage.RUSSIAN -> "Заполнить экран"
        AppLanguage.JAPANESE -> "画面に合わせて表示"
        AppLanguage.KOREAN -> "화면 채우기"
    }

    val fillScreenHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自动裁剪以填满整个屏幕"
        AppLanguage.ENGLISH -> "Auto crop to fill entire screen"
        AppLanguage.ARABIC -> "قص تلقائي لملء الشاشة بالكامل"
        AppLanguage.PORTUGUESE -> "Recortar automaticamente para preencher toda a tela"
        AppLanguage.SPANISH -> "Recortar automáticamente para llenar toda la pantalla"
        AppLanguage.FRENCH -> "Recadrer automatiquement pour remplir tout l'écran"
        AppLanguage.GERMAN -> "Automatisch zuschneiden, um den gesamten Bildschirm zu füllen"
        AppLanguage.RUSSIAN -> "Автокадрирование для заполнения всего экрана"
        AppLanguage.JAPANESE -> "画面全体を埋めるために自動クロップ"
        AppLanguage.KOREAN -> "전체 화면을 채우기 위해 자동 크롭"
    }

    val landscapeMode: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "横屏显示"
        AppLanguage.ENGLISH -> "Landscape Mode"
        AppLanguage.ARABIC -> "الوضع الأفقي"
        AppLanguage.PORTUGUESE -> "Modo Paisagem"
        AppLanguage.SPANISH -> "Modo Horizontal"
        AppLanguage.FRENCH -> "Mode Paysage"
        AppLanguage.GERMAN -> "Querformat-Modus"
        AppLanguage.RUSSIAN -> "Альбомный режим"
        AppLanguage.JAPANESE -> "横向きモード"
        AppLanguage.KOREAN -> "가로 모드"
    }

    val landscapeModeHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "以横屏模式显示内容"
        AppLanguage.ENGLISH -> "Display content in landscape mode"
        AppLanguage.ARABIC -> "عرض المحتوى في الوضع الأفقي"
        AppLanguage.PORTUGUESE -> "Exibir conteúdo no modo paisagem"
        AppLanguage.SPANISH -> "Mostrar contenido en modo horizontal"
        AppLanguage.FRENCH -> "Afficher le contenu en mode paysage"
        AppLanguage.GERMAN -> "Inhalte im Querformat anzeigen"
        AppLanguage.RUSSIAN -> "Отображать контент в альбомном режиме"
        AppLanguage.JAPANESE -> "コンテンツを横向きモードで表示"
        AppLanguage.KOREAN -> "콘텐츠를 가로 모드로 표시"
    }

    val enableAudio: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "启用音频"
        AppLanguage.ENGLISH -> "Enable Audio"
        AppLanguage.ARABIC -> "تفعيل الصوت"
        AppLanguage.PORTUGUESE -> "Ativar Áudio"
        AppLanguage.SPANISH -> "Activar Audio"
        AppLanguage.FRENCH -> "Activer l'Audio"
        AppLanguage.GERMAN -> "Audio aktivieren"
        AppLanguage.RUSSIAN -> "Включить звук"
        AppLanguage.JAPANESE -> "オーディオを有効化"
        AppLanguage.KOREAN -> "오디오 활성화"
    }

    val enableAudioHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "播放视频时包含声音"
        AppLanguage.ENGLISH -> "Include sound when playing video"
        AppLanguage.ARABIC -> "تضمين الصوت عند تشغيل الفيديو"
        AppLanguage.PORTUGUESE -> "Incluir som ao reproduzir vídeo"
        AppLanguage.SPANISH -> "Incluir sonido al reproducir video"
        AppLanguage.FRENCH -> "Inclure le son lors de la lecture vidéo"
        AppLanguage.GERMAN -> "Ton bei der Videowiedergabe einschließen"
        AppLanguage.RUSSIAN -> "Воспроизводить звук при проигрывании видео"
        AppLanguage.JAPANESE -> "動画再生時に音声を含める"
        AppLanguage.KOREAN -> "동영상 재생 시 소리 포함"
    }

    val loopPlay: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "循环播放"
        AppLanguage.ENGLISH -> "Loop Play"
        AppLanguage.ARABIC -> "تشغيل متكرر"
        AppLanguage.PORTUGUESE -> "Reprodução em Loop"
        AppLanguage.SPANISH -> "Reproducción en Bucle"
        AppLanguage.FRENCH -> "Lecture en Boucle"
        AppLanguage.GERMAN -> "Endlosschleife"
        AppLanguage.RUSSIAN -> "Повтор воспроизведения"
        AppLanguage.JAPANESE -> "ループ再生"
        AppLanguage.KOREAN -> "반복 재생"
    }

    val loopPlayHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "视频结束后自动重新播放"
        AppLanguage.ENGLISH -> "Auto replay when video ends"
        AppLanguage.ARABIC -> "إعادة التشغيل تلقائيًا عند انتهاء الفيديو"
        AppLanguage.PORTUGUESE -> "Reproduzir novamente automaticamente ao terminar"
        AppLanguage.SPANISH -> "Reproducir automáticamente al terminar"
        AppLanguage.FRENCH -> "Rejouer automatiquement à la fin de la vidéo"
        AppLanguage.GERMAN -> "Automatisch erneut abspielen, wenn das Video endet"
        AppLanguage.RUSSIAN -> "Автоповтор при окончании видео"
        AppLanguage.JAPANESE -> "動画終了時に自動的に再再生"
        AppLanguage.KOREAN -> "동영상 종료 시 자동 재생"
    }

    val autoPlay: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自动播放"
        AppLanguage.ENGLISH -> "Auto Play"
        AppLanguage.ARABIC -> "تشغيل تلقائي"
        AppLanguage.PORTUGUESE -> "Reprodução Automática"
        AppLanguage.SPANISH -> "Reproducción Automática"
        AppLanguage.FRENCH -> "Lecture Automatique"
        AppLanguage.GERMAN -> "Automatische Wiedergabe"
        AppLanguage.RUSSIAN -> "Автовоспроизведение"
        AppLanguage.JAPANESE -> "自動再生"
        AppLanguage.KOREAN -> "자동 재생"
    }

    val autoPlayHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "打开应用时自动开始播放"
        AppLanguage.ENGLISH -> "Auto start playing when app opens"
        AppLanguage.ARABIC -> "بدء التشغيل تلقائيًا عند فتح التطبيق"
        AppLanguage.PORTUGUESE -> "Iniciar reprodução automaticamente ao abrir o app"
        AppLanguage.SPANISH -> "Iniciar reproducción automáticamente al abrir la app"
        AppLanguage.FRENCH -> "Lancer automatiquement la lecture à l'ouverture de l'app"
        AppLanguage.GERMAN -> "Wiedergabe beim Öffnen der App automatisch starten"
        AppLanguage.RUSSIAN -> "Автозапуск воспроизведения при открытии приложения"
        AppLanguage.JAPANESE -> "アプリを開くと自動的に再生を開始"
        AppLanguage.KOREAN -> "앱을 열면 자동으로 재생 시작"
    }

    val mediaAppHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "创建的应用将%s，可用作相框、展示或视频壁纸。"
        AppLanguage.ENGLISH -> "The app will %s. Good for frames, signage, or video wallpaper."
        AppLanguage.ARABIC -> "سيقوم التطبيق المُنشأ بـ %s، مناسب للإطارات الرقمية أو عروض الإعلانات أو خلفيات الفيديو."
        AppLanguage.PORTUGUESE -> "O app vai %s. Útil para molduras, displays ou papéis de parede em vídeo."
        AppLanguage.SPANISH -> "La app %s. Útil para marcos, cartelería o fondos de pantalla de video."
        AppLanguage.FRENCH -> "L'app %s. Utile pour cadres, affichages ou fonds d'écran vidéo."
        AppLanguage.GERMAN -> "Die App wird %s. Gut für Rahmen, Beschilderung oder Video-Hintergründe."
        AppLanguage.RUSSIAN -> "Приложение будет %s. Подходит для рамок, вывесок или видеообоев."
        AppLanguage.JAPANESE -> "アプリは%sします。フォトフレーム、サイネージ、動画の壁紙に適しています。"
        AppLanguage.KOREAN -> "앱은 %s합니다. 액자, 사이니지 또는 비디오 배경화면에 적합합니다."
    }

    val fullscreenDisplayImage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "全屏显示所选图片"
        AppLanguage.ENGLISH -> "Display the selected image fullscreen"
        AppLanguage.ARABIC -> "عرض الصورة المختارة بملء الشاشة"
        AppLanguage.PORTUGUESE -> "Exibir a imagem selecionada em tela cheia"
        AppLanguage.SPANISH -> "Mostrar la imagen seleccionada a pantalla completa"
        AppLanguage.FRENCH -> "Afficher l'image sélectionnée en plein écran"
        AppLanguage.GERMAN -> "Ausgewähltes Bild im Vollbild anzeigen"
        AppLanguage.RUSSIAN -> "Показать выбранное изображение на весь экран"
        AppLanguage.JAPANESE -> "選択した画像を全画面表示"
        AppLanguage.KOREAN -> "선택한 이미지 전체 화면 표시"
    }

    val fullscreenPlayVideo: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "全屏播放所选视频"
        AppLanguage.ENGLISH -> "Play the selected video fullscreen"
        AppLanguage.ARABIC -> "تشغيل الفيديو المختار بملء الشاشة"
        AppLanguage.PORTUGUESE -> "Reproduzir o vídeo selecionado em tela cheia"
        AppLanguage.SPANISH -> "Reproducir el video seleccionado a pantalla completa"
        AppLanguage.FRENCH -> "Lire la vidéo sélectionnée en plein écran"
        AppLanguage.GERMAN -> "Ausgewähltes Video im Vollbild abspielen"
        AppLanguage.RUSSIAN -> "Воспроизвести выбранное видео на весь экран"
        AppLanguage.JAPANESE -> "選択した動画を全画面再生"
        AppLanguage.KOREAN -> "선택한 동영상 전체 화면 재생"
    }

    val createHtmlAppTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "创建HTML应用"
        AppLanguage.ENGLISH -> "Create HTML App"
        AppLanguage.ARABIC -> "إنشاء تطبيق HTML"
        AppLanguage.PORTUGUESE -> "Criar App HTML"
        AppLanguage.SPANISH -> "Crear App HTML"
        AppLanguage.FRENCH -> "Créer une App HTML"
        AppLanguage.GERMAN -> "HTML-App erstellen"
        AppLanguage.RUSSIAN -> "Создать HTML-приложение"
        AppLanguage.JAPANESE -> "HTML アプリを作成"
        AppLanguage.KOREAN -> "HTML 앱 만들기"
    }

    val selectFiles: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择文件"
        AppLanguage.ENGLISH -> "Select Files"
        AppLanguage.ARABIC -> "اختيار الملفات"
        AppLanguage.PORTUGUESE -> "Selecionar Arquivos"
        AppLanguage.SPANISH -> "Seleccionar Archivos"
        AppLanguage.FRENCH -> "Sélectionner les Fichiers"
        AppLanguage.GERMAN -> "Dateien auswählen"
        AppLanguage.RUSSIAN -> "Выбрать файлы"
        AppLanguage.JAPANESE -> "ファイルを選択"
        AppLanguage.KOREAN -> "파일 선택"
    }

    val selectFilesHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "分别选择HTML、CSS、JS文件（CSS和JS为可选）"
        AppLanguage.ENGLISH -> "Select HTML, CSS, JS files separately (CSS and JS are optional)"
        AppLanguage.ARABIC -> "اختر ملفات HTML و CSS و JS بشكل منفصل (CSS و JS اختياريان)"
        AppLanguage.PORTUGUESE -> "Selecione arquivos HTML, CSS, JS separadamente (CSS e JS são opcionais)"
        AppLanguage.SPANISH -> "Selecciona archivos HTML, CSS, JS por separado (CSS y JS son opcionales)"
        AppLanguage.FRENCH -> "Sélectionnez les fichiers HTML, CSS, JS séparément (CSS et JS sont optionnels)"
        AppLanguage.GERMAN -> "HTML-, CSS-, JS-Dateien separat auswählen (CSS und JS sind optional)"
        AppLanguage.RUSSIAN -> "Выберите файлы HTML, CSS, JS по отдельности (CSS и JS необязательны)"
        AppLanguage.JAPANESE -> "HTML、CSS、JS ファイルを個別に選択(CSS と JS は任意)"
        AppLanguage.KOREAN -> "HTML, CSS, JS 파일을 개별적으로 선택(CSS와 JS는 선택 사항)"
    }

    val htmlFile: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "HTML 文件"
        AppLanguage.ENGLISH -> "HTML File"
        AppLanguage.ARABIC -> "ملف HTML"
        AppLanguage.PORTUGUESE -> "Arquivo HTML"
        AppLanguage.SPANISH -> "Archivo HTML"
        AppLanguage.FRENCH -> "Fichier HTML"
        AppLanguage.GERMAN -> "HTML-Datei"
        AppLanguage.RUSSIAN -> "Файл HTML"
        AppLanguage.JAPANESE -> "HTML ファイル"
        AppLanguage.KOREAN -> "HTML 파일"
    }

    val cssFile: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "CSS 样式文件"
        AppLanguage.ENGLISH -> "CSS Style File"
        AppLanguage.ARABIC -> "ملف أنماط CSS"
        AppLanguage.PORTUGUESE -> "Arquivo de Estilo CSS"
        AppLanguage.SPANISH -> "Archivo de Estilo CSS"
        AppLanguage.FRENCH -> "Fichier de Style CSS"
        AppLanguage.GERMAN -> "CSS-Style-Datei"
        AppLanguage.RUSSIAN -> "Файл стилей CSS"
        AppLanguage.JAPANESE -> "CSS スタイルファイル"
        AppLanguage.KOREAN -> "CSS 스타일 파일"
    }

    val jsFile: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "JavaScript 脚本"
        AppLanguage.ENGLISH -> "JavaScript Script"
        AppLanguage.ARABIC -> "سكريبت JavaScript"
        AppLanguage.PORTUGUESE -> "Script JavaScript"
        AppLanguage.SPANISH -> "Script JavaScript"
        AppLanguage.FRENCH -> "Script JavaScript"
        AppLanguage.GERMAN -> "JavaScript-Skript"
        AppLanguage.RUSSIAN -> "Скрипт JavaScript"
        AppLanguage.JAPANESE -> "JavaScript スクリプト"
        AppLanguage.KOREAN -> "JavaScript 스크립트"
    }

    val enableJavaScript: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "启用 JavaScript"
        AppLanguage.ENGLISH -> "Enable JavaScript"
        AppLanguage.ARABIC -> "تفعيل JavaScript"
        AppLanguage.PORTUGUESE -> "Ativar JavaScript"
        AppLanguage.SPANISH -> "Activar JavaScript"
        AppLanguage.FRENCH -> "Activer JavaScript"
        AppLanguage.GERMAN -> "JavaScript aktivieren"
        AppLanguage.RUSSIAN -> "Включить JavaScript"
        AppLanguage.JAPANESE -> "JavaScript を有効化"
        AppLanguage.KOREAN -> "JavaScript 활성화"
    }

    val enableJsHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "允许HTML中的JavaScript代码执行"
        AppLanguage.ENGLISH -> "Allow JavaScript code execution in HTML"
        AppLanguage.ARABIC -> "السماح بتنفيذ كود JavaScript في HTML"
        AppLanguage.PORTUGUESE -> "Permitir execução de código JavaScript no HTML"
        AppLanguage.SPANISH -> "Permitir ejecución de código JavaScript en HTML"
        AppLanguage.FRENCH -> "Autoriser l'exécution du code JavaScript dans le HTML"
        AppLanguage.GERMAN -> "Ausführung von JavaScript-Code in HTML erlauben"
        AppLanguage.RUSSIAN -> "Разрешить выполнение кода JavaScript в HTML"
        AppLanguage.JAPANESE -> "HTML 内の JavaScript コードの実行を許可"
        AppLanguage.KOREAN -> "HTML 내 JavaScript 코드 실행 허용"
    }

    val enableLocalStorage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "启用本地存储"
        AppLanguage.ENGLISH -> "Enable Local Storage"
        AppLanguage.ARABIC -> "تفعيل التخزين المحلي"
        AppLanguage.PORTUGUESE -> "Ativar Armazenamento Local"
        AppLanguage.SPANISH -> "Activar Almacenamiento Local"
        AppLanguage.FRENCH -> "Activer le Stockage Local"
        AppLanguage.GERMAN -> "Lokalen Speicher aktivieren"
        AppLanguage.RUSSIAN -> "Включить локальное хранилище"
        AppLanguage.JAPANESE -> "ローカルストレージを有効化"
        AppLanguage.KOREAN -> "로컬 스토리지 활성화"
    }

    val enableLocalStorageHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "允许使用 localStorage 保存数据"
        AppLanguage.ENGLISH -> "Allow using localStorage to save data"
        AppLanguage.ARABIC -> "السماح باستخدام localStorage لحفظ البيانات"
        AppLanguage.PORTUGUESE -> "Permitir uso de localStorage para salvar dados"
        AppLanguage.SPANISH -> "Permitir uso de localStorage para guardar datos"
        AppLanguage.FRENCH -> "Autoriser l'utilisation de localStorage pour sauvegarder des données"
        AppLanguage.GERMAN -> "Verwendung von localStorage zum Speichern von Daten erlauben"
        AppLanguage.RUSSIAN -> "Разрешить использовать localStorage для сохранения данных"
        AppLanguage.JAPANESE -> "localStorage を使用したデータ保存を許可"
        AppLanguage.KOREAN -> "localStorage를 사용한 데이터 저장 허용"
    }

    val localServerToggle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "本地服务"
        AppLanguage.ENGLISH -> "Local Server"
        AppLanguage.ARABIC -> "خادم محلي"
        AppLanguage.PORTUGUESE -> "Servidor Local"
        AppLanguage.SPANISH -> "Servidor Local"
        AppLanguage.FRENCH -> "Serveur Local"
        AppLanguage.GERMAN -> "Lokaler Server"
        AppLanguage.RUSSIAN -> "Локальный сервер"
        AppLanguage.JAPANESE -> "ローカルサーバー"
        AppLanguage.KOREAN -> "로컬 서버"
    }

    val localServerFileDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "使用本地文件直接加载，离线可用"
        AppLanguage.ENGLISH -> "Load local files directly, available offline"
        AppLanguage.ARABIC -> "تحميل الملفات المحلية مباشرة، متاح دون اتصال"
        AppLanguage.PORTUGUESE -> "Carregar arquivos locais diretamente, disponível offline"
        AppLanguage.SPANISH -> "Cargar archivos locales directamente, disponible sin conexión"
        AppLanguage.FRENCH -> "Charger les fichiers locaux directement, disponible hors ligne"
        AppLanguage.GERMAN -> "Lokale Dateien direkt laden, offline verfügbar"
        AppLanguage.RUSSIAN -> "Загружать локальные файлы напрямую, доступно офлайн"
        AppLanguage.JAPANESE -> "ローカルファイルを直接読み込み、オフラインで利用可能"
        AppLanguage.KOREAN -> "로컬 파일을 직접 로드, 오프라인 사용 가능"
    }

    val localServerOnDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "通过本地 HTTP 服务承载，兼容 fetch、模块、WASM 和 PWA"
        AppLanguage.ENGLISH -> "Serve via local HTTP, compatible with fetch, modules, WASM and PWA"
        AppLanguage.ARABIC -> "خدمة عبر HTTP المحلي، متوافق مع fetch والوحدات و WASM و PWA"
        AppLanguage.PORTUGUESE -> "Servir via HTTP local, compatível com fetch, módulos, WASM e PWA"
        AppLanguage.SPANISH -> "Servir vía HTTP local, compatible con fetch, módulos, WASM y PWA"
        AppLanguage.FRENCH -> "Servir via HTTP local, compatible avec fetch, modules, WASM et PWA"
        AppLanguage.GERMAN -> "Über lokales HTTP bereitstellen, kompatibel mit fetch, Modulen, WASM und PWA"
        AppLanguage.RUSSIAN -> "Обслуживание через локальный HTTP, совместимо с fetch, модулями, WASM и PWA"
        AppLanguage.JAPANESE -> "ローカル HTTP で提供、fetch、モジュール、WASM、PWA に対応"
        AppLanguage.KOREAN -> "로컬 HTTP로 제공, fetch, 모듈, WASM, PWA와 호환"
    }

    val portConfigTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "端口配置"
        AppLanguage.ENGLISH -> "Port Configuration"
        AppLanguage.ARABIC -> "إعدادات المنفذ"
        AppLanguage.PORTUGUESE -> "Configuração de Porta"
        AppLanguage.SPANISH -> "Configuración de Puerto"
        AppLanguage.FRENCH -> "Configuration du Port"
        AppLanguage.GERMAN -> "Port-Konfiguration"
        AppLanguage.RUSSIAN -> "Настройка порта"
        AppLanguage.JAPANESE -> "ポート設定"
        AppLanguage.KOREAN -> "포트 설정"
    }

    val portAutoAssign: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自动分配端口"
        AppLanguage.ENGLISH -> "Auto Assign Port"
        AppLanguage.ARABIC -> "تعيين المنفذ تلقائيا"
        AppLanguage.PORTUGUESE -> "Atribuir Porta Automaticamente"
        AppLanguage.SPANISH -> "Asignar Puerto Automáticamente"
        AppLanguage.FRENCH -> "Attribuer le Port Automatiquement"
        AppLanguage.GERMAN -> "Port automatisch zuweisen"
        AppLanguage.RUSSIAN -> "Автовыбор порта"
        AppLanguage.JAPANESE -> "ポートを自動割り当て"
        AppLanguage.KOREAN -> "포트 자동 할당"
    }

    val portAutoAssignHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "系统自动选择可用端口"
        AppLanguage.ENGLISH -> "System automatically selects an available port"
        AppLanguage.ARABIC -> "يختار النظام منفذا متاحا تلقائيا"
        AppLanguage.PORTUGUESE -> "O sistema seleciona automaticamente uma porta disponível"
        AppLanguage.SPANISH -> "El sistema selecciona automáticamente un puerto disponible"
        AppLanguage.FRENCH -> "Le système sélectionne automatiquement un port disponible"
        AppLanguage.GERMAN -> "Das System wählt automatisch einen verfügbaren Port"
        AppLanguage.RUSSIAN -> "Система автоматически выбирает доступный порт"
        AppLanguage.JAPANESE -> "システムが利用可能なポートを自動選択"
        AppLanguage.KOREAN -> "시스템이 사용 가능한 포트를 자동 선택"
    }

    val portConflictTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "端口冲突处理"
        AppLanguage.ENGLISH -> "Port Conflict Handling"
        AppLanguage.ARABIC -> "معالجة تعارض المنفذ"
        AppLanguage.PORTUGUESE -> "Tratamento de Conflito de Porta"
        AppLanguage.SPANISH -> "Manejo de Conflicto de Puerto"
        AppLanguage.FRENCH -> "Gestion des Conflits de Port"
        AppLanguage.GERMAN -> "Port-Konfliktbehandlung"
        AppLanguage.RUSSIAN -> "Обработка конфликтов портов"
        AppLanguage.JAPANESE -> "ポート競合処理"
        AppLanguage.KOREAN -> "포트 충돌 처리"
    }

    val portConflictAutoKill: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自动终止占用进程"
        AppLanguage.ENGLISH -> "Auto Kill Occupying Process"
        AppLanguage.ARABIC -> "إنهاء العملية المحتلة تلقائيا"
        AppLanguage.PORTUGUESE -> "Encerrar Processo Ocupante Automaticamente"
        AppLanguage.SPANISH -> "Terminar Proceso Ocupante Automáticamente"
        AppLanguage.FRENCH -> "Terminer Automatiquement le Processus Occupant"
        AppLanguage.GERMAN -> "Belegenden Prozess automatisch beenden"
        AppLanguage.RUSSIAN -> "Автозавершение занимающего процесса"
        AppLanguage.JAPANESE -> "占有プロセスを自動終了"
        AppLanguage.KOREAN -> "점유 프로세스 자동 종료"
    }

    val portConflictAutoKillHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "检测到端口被占用时自动终止占用进程并启动服务"
        AppLanguage.ENGLISH -> "Automatically kill the occupying process and start the service when a port conflict is detected"
        AppLanguage.ARABIC -> "إنهاء العملية المحتلة تلقائيا وبدء الخدمة عند اكتشاف تعارض في المنفذ"
        AppLanguage.PORTUGUESE -> "Encerrar automaticamente o processo ocupante e iniciar o serviço quando um conflito de porta é detectado"
        AppLanguage.SPANISH -> "Terminar automáticamente el proceso ocupante e iniciar el servicio cuando se detecta un conflicto de puerto"
        AppLanguage.FRENCH -> "Terminer automatiquement le processus occupant et démarrer le service lorsqu'un conflit de port est détecté"
        AppLanguage.GERMAN -> "Belegenden Prozess automatisch beenden und den Dienst starten, wenn ein Port-Konflikt erkannt wird"
        AppLanguage.RUSSIAN -> "Автоматически завершать занимающий процесс и запускать службу при обнаружении конфликта портов"
        AppLanguage.JAPANESE -> "ポート競合を検出した際、占有プロセスを自動終了してサービスを開始"
        AppLanguage.KOREAN -> "포트 충돌 감지 시 점유 프로세스를 자동 종료하고 서비스 시작"
    }

    val portConflictAlert: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "提示端口被占用"
        AppLanguage.ENGLISH -> "Alert Port In Use"
        AppLanguage.ARABIC -> "تنبيه المنفذ مستخدم"
        AppLanguage.PORTUGUESE -> "Alertar Porta em Uso"
        AppLanguage.SPANISH -> "Alertar Puerto en Uso"
        AppLanguage.FRENCH -> "Alerter Port Utilisé"
        AppLanguage.GERMAN -> "Bei belegtem Port warnen"
        AppLanguage.RUSSIAN -> "Оповещать о занятом порте"
        AppLanguage.JAPANESE -> "ポート使用中を警告"
        AppLanguage.KOREAN -> "포트 사용 중 알림"
    }

    val portConflictAlertHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "检测到端口被占用时不加载页面并提示用户"
        AppLanguage.ENGLISH -> "Do not load the page and alert the user when a port conflict is detected"
        AppLanguage.ARABIC -> "عدم تحميل الصفحة وتنبيه المستخدم عند اكتشاف تعارض في المنفذ"
        AppLanguage.PORTUGUESE -> "Não carregar a página e alertar o usuário quando um conflito de porta é detectado"
        AppLanguage.SPANISH -> "No cargar la página y alertar al usuario cuando se detecta un conflicto de puerto"
        AppLanguage.FRENCH -> "Ne pas charger la page et alerter l'utilisateur lorsqu'un conflit de port est détecté"
        AppLanguage.GERMAN -> "Seite nicht laden und Benutzer warnen, wenn ein Port-Konflikt erkannt wird"
        AppLanguage.RUSSIAN -> "Не загружать страницу и оповещать пользователя при обнаружении конфликта портов"
        AppLanguage.JAPANESE -> "ポート競合検出時にページを読み込まず、ユーザーに警告"
        AppLanguage.KOREAN -> "포트 충돌 감지 시 페이지를 로드하지 않고 사용자에게 알림"
    }

    val portDefaultLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "默认端口"
        AppLanguage.ENGLISH -> "Default Port"
        AppLanguage.ARABIC -> "المنفذ الافتراضي"
        AppLanguage.PORTUGUESE -> "Porta Padrão"
        AppLanguage.SPANISH -> "Puerto Predeterminado"
        AppLanguage.FRENCH -> "Port par Défaut"
        AppLanguage.GERMAN -> "Standard-Port"
        AppLanguage.RUSSIAN -> "Порт по умолчанию"
        AppLanguage.JAPANESE -> "デフォルトポート"
        AppLanguage.KOREAN -> "기본 포트"
    }

    val orientationModeLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "屏幕方向"
        AppLanguage.ENGLISH -> "Screen Orientation"
        AppLanguage.ARABIC -> "اتجاه الشاشة"
        AppLanguage.PORTUGUESE -> "Orientação da Tela"
        AppLanguage.SPANISH -> "Orientación de Pantalla"
        AppLanguage.FRENCH -> "Orientation de l'Écran"
        AppLanguage.GERMAN -> "Bildschirmausrichtung"
        AppLanguage.RUSSIAN -> "Ориентация экрана"
        AppLanguage.JAPANESE -> "画面の向き"
        AppLanguage.KOREAN -> "화면 방향"
    }

    val orientationModeHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "设置应用的屏幕旋转方式"
        AppLanguage.ENGLISH -> "Control how the app handles screen rotation"
        AppLanguage.ARABIC -> "التحكم في كيفية تعامل التطبيق مع دوران الشاشة"
        AppLanguage.PORTUGUESE -> "Controlar como o app gerencia a rotação da tela"
        AppLanguage.SPANISH -> "Controlar cómo la app maneja la rotación de pantalla"
        AppLanguage.FRENCH -> "Contrôler la gestion de la rotation d'écran par l'app"
        AppLanguage.GERMAN -> "Steuern, wie die App die Bildschirmdrehung behandelt"
        AppLanguage.RUSSIAN -> "Управление поворотом экрана в приложении"
        AppLanguage.JAPANESE -> "アプリの画面回転の扱いを制御"
        AppLanguage.KOREAN -> "앱이 화면 회전을 처리하는 방식 제어"
    }

    val orientationLandscape: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "锁定横屏"
        AppLanguage.ENGLISH -> "Landscape"
        AppLanguage.ARABIC -> "أفقي"
        AppLanguage.PORTUGUESE -> "Paisagem"
        AppLanguage.SPANISH -> "Horizontal"
        AppLanguage.FRENCH -> "Paysage"
        AppLanguage.GERMAN -> "Querformat"
        AppLanguage.RUSSIAN -> "Альбомная"
        AppLanguage.JAPANESE -> "横向き"
        AppLanguage.KOREAN -> "가로"
    }

    val orientationAuto: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自动旋转"
        AppLanguage.ENGLISH -> "Auto-rotate"
        AppLanguage.ARABIC -> "تدوير تلقائي"
        AppLanguage.PORTUGUESE -> "Rotação Automática"
        AppLanguage.SPANISH -> "Rotación Automática"
        AppLanguage.FRENCH -> "Rotation Automatique"
        AppLanguage.GERMAN -> "Automatische Drehung"
        AppLanguage.RUSSIAN -> "Автоповорот"
        AppLanguage.JAPANESE -> "自動回転"
        AppLanguage.KOREAN -> "자동 회전"
    }

    val orientationAutoHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "跟随设备重力感应自动旋转方向，推荐平板设备使用"
        AppLanguage.ENGLISH -> "Automatically rotate based on device orientation sensor, recommended for tablets"
        AppLanguage.ARABIC -> "التدوير تلقائيًا بناءً على مستشعر اتجاه الجهاز، موصى به للأجهزة اللوحية"
        AppLanguage.PORTUGUESE -> "Rotacionar automaticamente com base no sensor de orientação do dispositivo, recomendado para tablets"
        AppLanguage.SPANISH -> "Rotar automáticamente según el sensor de orientación del dispositivo, recomendado para tablets"
        AppLanguage.FRENCH -> "Rotation automatique selon le capteur d'orientation de l'appareil, recommandé pour les tablettes"
        AppLanguage.GERMAN -> "Automatische Drehung basierend auf dem Ausrichtungssensor des Geräts, empfohlen für Tablets"
        AppLanguage.RUSSIAN -> "Автоповорот по датчику ориентации устройства, рекомендуется для планшетов"
        AppLanguage.JAPANESE -> "デバイスの方向センサーに基づき自動回転、タブレットに推奨"
        AppLanguage.KOREAN -> "기기 방향 센서를 기반으로 자동 회전, 태블릿에 권장"
    }

    val orientationBasicLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "基础模式"
        AppLanguage.ENGLISH -> "Basic"
        AppLanguage.ARABIC -> "أساسي"
        AppLanguage.PORTUGUESE -> "Básico"
        AppLanguage.SPANISH -> "Básico"
        AppLanguage.FRENCH -> "Basique"
        AppLanguage.GERMAN -> "Einfach"
        AppLanguage.RUSSIAN -> "Базовый"
        AppLanguage.JAPANESE -> "基本"
        AppLanguage.KOREAN -> "기본"
    }

    val orientationAdvancedLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "高级方向选项"
        AppLanguage.ENGLISH -> "Advanced Orientation"
        AppLanguage.ARABIC -> "اتجاه متقدم"
        AppLanguage.PORTUGUESE -> "Orientação Avançada"
        AppLanguage.SPANISH -> "Orientación Avanzada"
        AppLanguage.FRENCH -> "Orientation Avancée"
        AppLanguage.GERMAN -> "Erweiterte Ausrichtung"
        AppLanguage.RUSSIAN -> "Расширенная ориентация"
        AppLanguage.JAPANESE -> "高度な向き設定"
        AppLanguage.KOREAN -> "고급 방향 설정"
    }

    val orientationReversedLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "反向锁定"
        AppLanguage.ENGLISH -> "Reversed Lock"
        AppLanguage.ARABIC -> "قفل معكوس"
        AppLanguage.PORTUGUESE -> "Bloqueio Invertido"
        AppLanguage.SPANISH -> "Bloqueo Invertido"
        AppLanguage.FRENCH -> "Verrouillage Inversé"
        AppLanguage.GERMAN -> "Umgekehrte Sperre"
        AppLanguage.RUSSIAN -> "Обратная блокировка"
        AppLanguage.JAPANESE -> "反転ロック"
        AppLanguage.KOREAN -> "반전 잠금"
    }

    val orientationSensorLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "感应旋转"
        AppLanguage.ENGLISH -> "Sensor Rotation"
        AppLanguage.ARABIC -> "دوران بالمستشعر"
        AppLanguage.PORTUGUESE -> "Rotação por sensor"
        AppLanguage.SPANISH -> "Rotación por sensor"
        AppLanguage.FRENCH -> "Rotation par capteur"
        AppLanguage.GERMAN -> "Sensor-Rotation"
        AppLanguage.RUSSIAN -> "Поворот по датчику"
        AppLanguage.JAPANESE -> "センサー回転"
        AppLanguage.KOREAN -> "센서 회전"
    }

    val orientationLandscapeDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "锁定正向横屏，适合视频和游戏"
        AppLanguage.ENGLISH -> "Lock in landscape, ideal for video & games"
        AppLanguage.ARABIC -> "قفل أفقي، مثالي للفيديو والألعاب"
        AppLanguage.PORTUGUESE -> "Travar em paisagem, ideal para vídeos e jogos"
        AppLanguage.SPANISH -> "Bloquear en horizontal, ideal para vídeo y juegos"
        AppLanguage.FRENCH -> "Verrouiller en paysage, idéal pour vidéo et jeux"
        AppLanguage.GERMAN -> "Im Querformat sperren, ideal für Video & Spiele"
        AppLanguage.RUSSIAN -> "Заблокировать в альбомной ориентации, идеально для видео и игр"
        AppLanguage.JAPANESE -> "横画面で固定、動画やゲームに最適"
        AppLanguage.KOREAN -> "가로 모드 고정, 동영상 및 게임에 적합"
    }

    val orientationAutoDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "全方向自由旋转，跟随设备重力感应"
        AppLanguage.ENGLISH -> "Free rotation in all directions, follows device sensor"
        AppLanguage.ARABIC -> "دوران حر في جميع الاتجاهات، يتبع مستشعر الجهاز"
        AppLanguage.PORTUGUESE -> "Rotação livre em todas as direções, segue o sensor do dispositivo"
        AppLanguage.SPANISH -> "Rotación libre en todas las direcciones, sigue el sensor del dispositivo"
        AppLanguage.FRENCH -> "Rotation libre dans toutes les directions, suit le capteur de l'appareil"
        AppLanguage.GERMAN -> "Freie Rotation in alle Richtungen, folgt dem Gerätesensor"
        AppLanguage.RUSSIAN -> "Свободное вращение во всех направлениях, по датчику устройства"
        AppLanguage.JAPANESE -> "全方向に自由回転、デバイスのセンサーに追従"
        AppLanguage.KOREAN -> "모든 방향으로 자유 회전, 기기 센서를 따름"
    }

    val orientationReversePortrait: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "反向竖屏"
        AppLanguage.ENGLISH -> "Reverse Portrait"
        AppLanguage.ARABIC -> "عمودي معكوس"
        AppLanguage.PORTUGUESE -> "Retrato invertido"
        AppLanguage.SPANISH -> "Retrato invertido"
        AppLanguage.FRENCH -> "Portrait inversé"
        AppLanguage.GERMAN -> "Umgekehrtes Hochformat"
        AppLanguage.RUSSIAN -> "Обратный портрет"
        AppLanguage.JAPANESE -> "逆向き縦画面"
        AppLanguage.KOREAN -> "역방향 세로 모드"
    }

    val orientationReversePortraitDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "锁定倒置竖屏，底部朝上"
        AppLanguage.ENGLISH -> "Lock upside-down portrait, bottom facing up"
        AppLanguage.ARABIC -> "قفل عمودي مقلوب، الجزء السفلي لأعلى"
        AppLanguage.PORTUGUESE -> "Travar retrato de cabeça para baixo, fundo voltado para cima"
        AppLanguage.SPANISH -> "Bloquear retrato invertido, parte inferior hacia arriba"
        AppLanguage.FRENCH -> "Verrouiller portrait inversé, bas vers le haut"
        AppLanguage.GERMAN -> "Kopfstehendes Hochformat sperren, unten nach oben"
        AppLanguage.RUSSIAN -> "Заблокировать перевернутый портрет, низом вверх"
        AppLanguage.JAPANESE -> "逆さま縦画面で固定、下を上に"
        AppLanguage.KOREAN -> "거꾸로 세로 모드 고정, 아래가 위로"
    }

    val orientationReverseLandscape: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "反向横屏"
        AppLanguage.ENGLISH -> "Reverse Landscape"
        AppLanguage.ARABIC -> "أفقي معكوس"
        AppLanguage.PORTUGUESE -> "Paisagem invertida"
        AppLanguage.SPANISH -> "Horizontal invertido"
        AppLanguage.FRENCH -> "Paysage inversé"
        AppLanguage.GERMAN -> "Umgekehrtes Querformat"
        AppLanguage.RUSSIAN -> "Обратный альбом"
        AppLanguage.JAPANESE -> "逆向き横画面"
        AppLanguage.KOREAN -> "역방향 가로 모드"
    }

    val orientationReverseLandscapeDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "锁定反向横屏，与正向横屏镜像"
        AppLanguage.ENGLISH -> "Lock reverse landscape, mirrored from standard"
        AppLanguage.ARABIC -> "قفل أفقي معكوس، انعكاس من الوضع القياسي"
        AppLanguage.PORTUGUESE -> "Travar paisagem invertida, espelhada do padrão"
        AppLanguage.SPANISH -> "Bloquear horizontal invertido, reflejado del estándar"
        AppLanguage.FRENCH -> "Verrouiller paysage inversé, miroir du standard"
        AppLanguage.GERMAN -> "Umgekehrtes Querformat sperren, gespiegelt vom Standard"
        AppLanguage.RUSSIAN -> "Заблокировать обратный альбом, зеркально от стандартного"
        AppLanguage.JAPANESE -> "逆向き横画面で固定、標準の鏡像"
        AppLanguage.KOREAN -> "역방향 가로 모드 고정, 표준의 거울"
    }

    val orientationSensorPortrait: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "感应竖屏"
        AppLanguage.ENGLISH -> "Sensor Portrait"
        AppLanguage.ARABIC -> "عمودي بالمستشعر"
        AppLanguage.PORTUGUESE -> "Retrato por sensor"
        AppLanguage.SPANISH -> "Retrato por sensor"
        AppLanguage.FRENCH -> "Portrait par capteur"
        AppLanguage.GERMAN -> "Sensor-Hochformat"
        AppLanguage.RUSSIAN -> "Портрет по датчику"
        AppLanguage.JAPANESE -> "センサー縦画面"
        AppLanguage.KOREAN -> "센서 세로 모드"
    }

    val orientationSensorPortraitDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "竖屏范围内自动切换正向/反向"
        AppLanguage.ENGLISH -> "Auto-switch between portrait & reverse portrait"
        AppLanguage.ARABIC -> "التبديل تلقائيًا بين العمودي والعمودي المعكوس"
        AppLanguage.PORTUGUESE -> "Alternar entre retrato e retrato invertido"
        AppLanguage.SPANISH -> "Alternar entre retrato y retrato invertido"
        AppLanguage.FRENCH -> "Alterner entre portrait et portrait inversé"
        AppLanguage.GERMAN -> "Zwischen Hochformat und umgekehrtem Hochformat wechseln"
        AppLanguage.RUSSIAN -> "Автопереключение между портретом и обратным портретом"
        AppLanguage.JAPANESE -> "縦画面と逆向き縦画面を自動切替"
        AppLanguage.KOREAN -> "세로 모드와 역방향 세로 모드 자동 전환"
    }

    val orientationSensorPortraitHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "在正向竖屏和反向竖屏之间根据重力感应自动切换，适合需要倒置使用的场景"
        AppLanguage.ENGLISH -> "Automatically switch between upright and upside-down portrait based on sensor, useful for inverted setups"
        AppLanguage.ARABIC -> "التبديل تلقائيًا بين الوضع العمودي والمقلوب بناءً على المستشعر"
        AppLanguage.PORTUGUESE -> "Alternar automaticamente entre retrato normal e invertido por sensor, útil para montagens invertidas"
        AppLanguage.SPANISH -> "Alternar automáticamente entre retrato normal e invertido según el sensor, útil para configuraciones invertidas"
        AppLanguage.FRENCH -> "Basculer automatiquement entre portrait normal et inversé selon le capteur, utile pour configurations inversées"
        AppLanguage.GERMAN -> "Automatisch zwischen normalem und umgekehrtem Hochformat wechseln, nützlich für invertierte Setups"
        AppLanguage.RUSSIAN -> "Автоматическое переключение между обычным и перевёрнутым портретом по датчику, удобно для перевёрнутых установок"
        AppLanguage.JAPANESE -> "センサーに基づき縦画面と逆さま縦画面を自動切替、逆向き設置に便利"
        AppLanguage.KOREAN -> "센서 기반으로 세로 모드와 거꾸로 세로 모드 자동 전환, 역방향 설정에 유용"
    }

    val orientationSensorLandscape: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "感应横屏"
        AppLanguage.ENGLISH -> "Sensor Landscape"
        AppLanguage.ARABIC -> "أفقي بالمستشعر"
        AppLanguage.PORTUGUESE -> "Paisagem por sensor"
        AppLanguage.SPANISH -> "Horizontal por sensor"
        AppLanguage.FRENCH -> "Paysage par capteur"
        AppLanguage.GERMAN -> "Sensor-Querformat"
        AppLanguage.RUSSIAN -> "Альбом по датчику"
        AppLanguage.JAPANESE -> "センサー横画面"
        AppLanguage.KOREAN -> "센서 가로 모드"
    }

    val orientationSensorLandscapeDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "横屏范围内自动切换正向/反向"
        AppLanguage.ENGLISH -> "Auto-switch between landscape & reverse landscape"
        AppLanguage.ARABIC -> "التبديل تلقائيًا بين الأفقي والأفقي المعكوس"
        AppLanguage.PORTUGUESE -> "Alternar entre paisagem e paisagem invertida"
        AppLanguage.SPANISH -> "Alternar entre horizontal y horizontal invertido"
        AppLanguage.FRENCH -> "Alterner entre paysage et paysage inversé"
        AppLanguage.GERMAN -> "Zwischen Querformat und umgekehrtem Querformat wechseln"
        AppLanguage.RUSSIAN -> "Автопереключение между альбомом и обратным альбомом"
        AppLanguage.JAPANESE -> "横画面と逆向き横画面を自動切替"
        AppLanguage.KOREAN -> "가로 모드와 역방향 가로 모드 자동 전환"
    }

    val orientationSensorLandscapeHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "在正向横屏和反向横屏之间根据重力感应自动切换，适合平板游戏等横屏场景"
        AppLanguage.ENGLISH -> "Automatically switch between standard and reverse landscape based on sensor, ideal for tablet gaming"
        AppLanguage.ARABIC -> "التبديل تلقائيًا بين الأفقي القياسي والمعكوس بناءً على المستشعر، مثالي لألعاب الأجهزة اللوحية"
        AppLanguage.PORTUGUESE -> "Alternar automaticamente entre paisagem padrão e invertida por sensor, ideal para jogos em tablet"
        AppLanguage.SPANISH -> "Alternar automáticamente entre horizontal estándar e invertido según el sensor, ideal para juegos en tablet"
        AppLanguage.FRENCH -> "Basculer automatiquement entre paysage standard et inversé selon le capteur, idéal pour jeux sur tablette"
        AppLanguage.GERMAN -> "Automatisch zwischen Standard- und umgekehrtem Querformat wechseln, ideal für Tablet-Gaming"
        AppLanguage.RUSSIAN -> "Автопереключение между стандартным и обратным альбомом по датчику, идеально для игр на планшете"
        AppLanguage.JAPANESE -> "センサーに基づき標準横画面と逆向き横画面を自動切替、タブレットゲームに最適"
        AppLanguage.KOREAN -> "센서 기반으로 표준 가로 모드와 역방향 가로 모드 자동 전환, 태블릿 게임에 적합"
    }

    val keepScreenOnLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "屏幕常亮"
        AppLanguage.ENGLISH -> "Screen Always On"
        AppLanguage.ARABIC -> "الشاشة مضاءة دائمًا"
        AppLanguage.PORTUGUESE -> "Tela sempre acesa"
        AppLanguage.SPANISH -> "Pantalla siempre encendida"
        AppLanguage.FRENCH -> "Écran toujours allumé"
        AppLanguage.GERMAN -> "Bildschirm immer an"
        AppLanguage.RUSSIAN -> "Экран всегда включён"
        AppLanguage.JAPANESE -> "画面常時点灯"
        AppLanguage.KOREAN -> "화면 상시 켜짐"
    }

    val screenAwakeModeLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "常亮模式"
        AppLanguage.ENGLISH -> "Awake Mode"
        AppLanguage.ARABIC -> "وضع الإيقاظ"
        AppLanguage.PORTUGUESE -> "Modo acordado"
        AppLanguage.SPANISH -> "Modo despierto"
        AppLanguage.FRENCH -> "Mode éveillé"
        AppLanguage.GERMAN -> "Wach-Modus"
        AppLanguage.RUSSIAN -> "Режим пробуждения"
        AppLanguage.JAPANESE -> "起動モード"
        AppLanguage.KOREAN -> "깨어 있기 모드"
    }

    val screenAwakeOff: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "关闭"
        AppLanguage.ENGLISH -> "Off"
        AppLanguage.ARABIC -> "إيقاف"
        AppLanguage.PORTUGUESE -> "Desligado"
        AppLanguage.SPANISH -> "Apagado"
        AppLanguage.FRENCH -> "Désactivé"
        AppLanguage.GERMAN -> "Aus"
        AppLanguage.RUSSIAN -> "Выкл"
        AppLanguage.JAPANESE -> "オフ"
        AppLanguage.KOREAN -> "꺼짐"
    }

    val screenAwakeOffDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "跟随系统自动息屏"
        AppLanguage.ENGLISH -> "Follow system screen timeout"
        AppLanguage.ARABIC -> "اتبع مهلة شاشة النظام"
        AppLanguage.PORTUGUESE -> "Seguir tempo limite da tela do sistema"
        AppLanguage.SPANISH -> "Seguir tiempo de espera de pantalla del sistema"
        AppLanguage.FRENCH -> "Suivre le délai d'extinction système"
        AppLanguage.GERMAN -> "System-Bildschirm-Timeout folgen"
        AppLanguage.RUSSIAN -> "Следовать системному тайм-ауту экрана"
        AppLanguage.JAPANESE -> "システムの画面タイムアウトに従う"
        AppLanguage.KOREAN -> "시스템 화면 시간제한 따르기"
    }

    val screenAwakeAlways: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "始终常亮"
        AppLanguage.ENGLISH -> "Always On"
        AppLanguage.ARABIC -> "تشغيل دائم"
        AppLanguage.PORTUGUESE -> "Sempre ligado"
        AppLanguage.SPANISH -> "Siempre encendido"
        AppLanguage.FRENCH -> "Toujours allumé"
        AppLanguage.GERMAN -> "Immer an"
        AppLanguage.RUSSIAN -> "Всегда вкл"
        AppLanguage.JAPANESE -> "常時オン"
        AppLanguage.KOREAN -> "항상 켜짐"
    }

    val screenAwakeAlwaysDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "屏幕保持常亮，适合 code-server、数字相框"
        AppLanguage.ENGLISH -> "Screen stays on, ideal for code-server & digital frames"
        AppLanguage.ARABIC -> "تبقى الشاشة مضاءة، مثالي للخوادم والإطارات الرقمية"
        AppLanguage.PORTUGUESE -> "Tela permanece acesa, ideal para code-server e molduras digitais"
        AppLanguage.SPANISH -> "Pantalla permanece encendida, ideal para code-server y marcos digitales"
        AppLanguage.FRENCH -> "L'écran reste allumé, idéal pour code-server et cadres numériques"
        AppLanguage.GERMAN -> "Bildschirm bleibt an, ideal für code-server und digitale Rahmen"
        AppLanguage.RUSSIAN -> "Экран остаётся включённым, идеально для code-server и цифровых рамок"
        AppLanguage.JAPANESE -> "画面を点灯し続ける、code-serverやデジタルフォトフレームに最適"
        AppLanguage.KOREAN -> "화면이 켜진 채로 유지, code-server 및 디지털 액자에 적합"
    }

    val screenAwakeTimed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "定时常亮"
        AppLanguage.ENGLISH -> "Timed"
        AppLanguage.ARABIC -> "مؤقت"
        AppLanguage.PORTUGUESE -> "Temporizado"
        AppLanguage.SPANISH -> "Temporizado"
        AppLanguage.FRENCH -> "Minuté"
        AppLanguage.GERMAN -> "Zeitgesteuert"
        AppLanguage.RUSSIAN -> "По таймеру"
        AppLanguage.JAPANESE -> "タイマー"
        AppLanguage.KOREAN -> "타이머"
    }

    val screenAwakeTimedDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "在指定时间后恢复系统息屏，节省电量"
        AppLanguage.ENGLISH -> "Revert to system timeout after set duration, saves battery"
        AppLanguage.ARABIC -> "العودة إلى مهلة النظام بعد المدة المحددة، يوفر البطارية"
        AppLanguage.PORTUGUESE -> "Reverter para tempo limite do sistema após duração definida, economiza bateria"
        AppLanguage.SPANISH -> "Revertir al tiempo de espera del sistema tras la duración establecida, ahorra batería"
        AppLanguage.FRENCH -> "Revenir au délai système après la durée définie, économise la batterie"
        AppLanguage.GERMAN -> "Nach festgelegter Dauer zum System-Timeout zurückkehren, spart Akku"
        AppLanguage.RUSSIAN -> "Вернуться к системному тайм-ауту после заданной длительности, экономит батарею"
        AppLanguage.JAPANESE -> "設定時間後にシステムタイムアウトに戻り、バッテリーを節約"
        AppLanguage.KOREAN -> "설정 시간 후 시스템 시간제한으로 되돌림, 배터리 절약"
    }

    fun screenAwakeTimedStatusDesc(minutes: Int): String = when (Strings.lang) {
        AppLanguage.CHINESE -> "定时常亮 ${minutes} 分钟后自动息屏"
        AppLanguage.ENGLISH -> "Screen on for $minutes min, then auto-sleep"
        AppLanguage.ARABIC -> "الشاشة مضاءة لمدة $minutes دقيقة، ثم السكون التلقائي"
        AppLanguage.PORTUGUESE -> "Tela acesa por $minutes min, depois dorme automaticamente"
        AppLanguage.SPANISH -> "Pantalla encendida por $minutes min, luego duerme automáticamente"
        AppLanguage.FRENCH -> "Écran allumé pendant $minutes min, puis mise en veille auto"
        AppLanguage.GERMAN -> "Bildschirm $minutes Min an, dann auto-Schlaf"
        AppLanguage.RUSSIAN -> "Экран вкл $minutes мин, затем авто-сон"
        AppLanguage.JAPANESE -> "${minutes}分間点灯後、自動スリープ"
        AppLanguage.KOREAN -> "${minutes}분 화면 켜짐 후 자동 대기"
    }

    val screenAwakeTimeoutLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "常亮时长"
        AppLanguage.ENGLISH -> "Duration"
        AppLanguage.ARABIC -> "المدة"
        AppLanguage.PORTUGUESE -> "Duração"
        AppLanguage.SPANISH -> "Duración"
        AppLanguage.FRENCH -> "Durée"
        AppLanguage.GERMAN -> "Dauer"
        AppLanguage.RUSSIAN -> "Длительность"
        AppLanguage.JAPANESE -> "継続時間"
        AppLanguage.KOREAN -> "지속 시간"
    }

    fun screenAwakeTimeoutValue(minutes: Int): String = when {
        minutes >= 60 -> when (Strings.lang) {
            AppLanguage.CHINESE -> "${minutes / 60} 小时${if (minutes % 60 > 0) " ${minutes % 60} 分" else ""}"
            AppLanguage.ENGLISH -> "${minutes / 60}h${if (minutes % 60 > 0) " ${minutes % 60}m" else ""}"
            AppLanguage.ARABIC -> "${minutes / 60} ساعة${if (minutes % 60 > 0) " ${minutes % 60} دقيقة" else ""}"
            AppLanguage.PORTUGUESE -> "${minutes / 60}h${if (minutes % 60 > 0) " ${minutes % 60}min" else ""}"
            AppLanguage.SPANISH -> "${minutes / 60}h${if (minutes % 60 > 0) " ${minutes % 60}min" else ""}"
            AppLanguage.FRENCH -> "${minutes / 60}h${if (minutes % 60 > 0) " ${minutes % 60}min" else ""}"
            AppLanguage.GERMAN -> "${minutes / 60} Std${if (minutes % 60 > 0) " ${minutes % 60} Min" else ""}"
            AppLanguage.RUSSIAN -> "${minutes / 60} ч${if (minutes % 60 > 0) " ${minutes % 60} мин" else ""}"
            AppLanguage.JAPANESE -> "${minutes / 60}時間${if (minutes % 60 > 0) " ${minutes % 60}分" else ""}"
            AppLanguage.KOREAN -> "${minutes / 60}시간${if (minutes % 60 > 0) " ${minutes % 60}분" else ""}"
        }
        else -> when (Strings.lang) {
            AppLanguage.CHINESE -> "${minutes} 分钟"
            AppLanguage.ENGLISH -> "${minutes} min"
            AppLanguage.ARABIC -> "$minutes دقيقة"
            AppLanguage.PORTUGUESE -> "${minutes} min"
            AppLanguage.SPANISH -> "${minutes} min"
            AppLanguage.FRENCH -> "${minutes} min"
            AppLanguage.GERMAN -> "${minutes} Min"
            AppLanguage.RUSSIAN -> "${minutes} мин"
            AppLanguage.JAPANESE -> "${minutes}分"
            AppLanguage.KOREAN -> "${minutes}분"
        }
    }

    val screenBrightnessLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "屏幕亮度"
        AppLanguage.ENGLISH -> "Screen Brightness"
        AppLanguage.ARABIC -> "سطوع الشاشة"
        AppLanguage.PORTUGUESE -> "Brilho da tela"
        AppLanguage.SPANISH -> "Brillo de pantalla"
        AppLanguage.FRENCH -> "Luminosité de l'écran"
        AppLanguage.GERMAN -> "Bildschirmhelligkeit"
        AppLanguage.RUSSIAN -> "Яркость экрана"
        AppLanguage.JAPANESE -> "画面の明るさ"
        AppLanguage.KOREAN -> "화면 밝기"
    }

    val screenBrightnessAuto: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "跟随系统"
        AppLanguage.ENGLISH -> "System Default"
        AppLanguage.ARABIC -> "افتراضي النظام"
        AppLanguage.PORTUGUESE -> "Padrão do sistema"
        AppLanguage.SPANISH -> "Predeterminado del sistema"
        AppLanguage.FRENCH -> "Par défaut du système"
        AppLanguage.GERMAN -> "Systemstandard"
        AppLanguage.RUSSIAN -> "Системный по умолчанию"
        AppLanguage.JAPANESE -> "システム標準"
        AppLanguage.KOREAN -> "시스템 기본값"
    }

    val screenBrightnessManual: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自定义亮度"
        AppLanguage.ENGLISH -> "Custom"
        AppLanguage.ARABIC -> "مخصص"
        AppLanguage.PORTUGUESE -> "Personalizado"
        AppLanguage.SPANISH -> "Personalizado"
        AppLanguage.FRENCH -> "Personnalisé"
        AppLanguage.GERMAN -> "Benutzerdefiniert"
        AppLanguage.RUSSIAN -> "Пользовательский"
        AppLanguage.JAPANESE -> "カスタム"
        AppLanguage.KOREAN -> "사용자 지정"
    }

    val screenAwakeBatteryWarning: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "始终常亮模式会增加电量消耗，建议仅在插电或对接使用场景下开启"
        AppLanguage.ENGLISH -> "Always-on mode increases battery drain. Recommended only when plugged in or for kiosk use."
        AppLanguage.ARABIC -> "وضع التشغيل الدائم يزيد استهلاك البطارية. يُوصى به فقط عند التوصيل بالشاحن."
        AppLanguage.PORTUGUESE -> "Modo sempre ligado aumenta o consumo de bateria. Recomendado apenas quando plugado ou para uso em quiosque."
        AppLanguage.SPANISH -> "El modo siempre encendido aumenta el consumo de batería. Recomendado solo cuando está enchufado o para uso en quiosco."
        AppLanguage.FRENCH -> "Le mode toujours allumé augmente la décharge de la batterie. Recommandé uniquement branché ou pour usage en kiosque."
        AppLanguage.GERMAN -> "Always-on-Modus erhöht Akkuverbrauch. Nur bei Netzbetrieb oder Kiosk-Use empfohlen."
        AppLanguage.RUSSIAN -> "Режим «всегда вкл» ускоряет разряд батареи. Рекомендуется только при подключении к сети или для режима киоска."
        AppLanguage.JAPANESE -> "常時オンモードはバッテリー消費を増加させます。充電中またはキオスク使用時のみ推奨。"
        AppLanguage.KOREAN -> "항상 켜짐 모드는 배터리 소모를 증가시킵니다. 충전 중이거나 키오스크 용도로만 권장됩니다."
    }

    val screenAwakeTimedHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "定时常亮兼顾使用体验与电量节省，超时后将自动恢复系统息屏策略"
        AppLanguage.ENGLISH -> "Timed mode balances usability and battery life. Screen will auto-sleep after the timeout."
        AppLanguage.ARABIC -> "الوضع المؤقت يوازن بين سهولة الاستخدام وعمر البطارية. ستنام الشاشة تلقائيًا بعد المهلة."
        AppLanguage.PORTUGUESE -> "Modo temporizado equilibra usabilidade e bateria. Tela dormirá automaticamente após o tempo."
        AppLanguage.SPANISH -> "Modo temporizado equilibra usabilidad y batería. Pantalla dormirá automáticamente tras el tiempo."
        AppLanguage.FRENCH -> "Le mode minuté équilibre utilisabilité et batterie. L'écran se mettra en veille après le délai."
        AppLanguage.GERMAN -> "Zeitgesteuerter Modus balanciert Nutzbarkeit und Akku. Bildschirm schläft nach Timeout automatisch."
        AppLanguage.RUSSIAN -> "Режим по таймеру балансирует удобство и батарею. Экран уснёт автоматически после тайм-аута."
        AppLanguage.JAPANESE -> "タイマーモードは使いやすさとバッテリーのバランスを取ります。タイムアウト後に自動スリープします。"
        AppLanguage.KOREAN -> "타이머 모드는 사용성과 배터리 수명의 균형을 맞춥니다. 시간제한 후 화면이 자동 대기 모드로 전환됩니다."
    }

    val keyboardAdjustModeLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "键盘调整模式"
        AppLanguage.ENGLISH -> "Keyboard Adjust Mode"
        AppLanguage.ARABIC -> "وضع ضبط لوحة المفاتيح"
        AppLanguage.PORTUGUESE -> "Modo de ajuste do teclado"
        AppLanguage.SPANISH -> "Modo de ajuste de teclado"
        AppLanguage.FRENCH -> "Mode d'ajustement du clavier"
        AppLanguage.GERMAN -> "Tastatur-Anpassungsmodus"
        AppLanguage.RUSSIAN -> "Режим регулировки клавиатуры"
        AppLanguage.JAPANESE -> "キーボード調整モード"
        AppLanguage.KOREAN -> "키보드 조정 모드"
    }

    val keyboardAdjustModeHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "控制软键盘弹出时的页面行为。如遇到键盘遮挡输入框或动画卡顿，可尝试切换此选项"
        AppLanguage.ENGLISH -> "Control page behavior when soft keyboard appears. Switch if input fields are hidden or animation is laggy"
        AppLanguage.ARABIC -> "التحكم في سلوك الصفحة عند ظهور لوحة المفاتيح الناعمة. قم بالتبديل إذا كانت حقول الإدخال مخفية أو الرسوم متحركة بطيئة"
        AppLanguage.PORTUGUESE -> "Controlar comportamento da página quando o teclado virtual aparece. Alterne se campos de entrada ficarem ocultos ou animação travar"
        AppLanguage.SPANISH -> "Controlar comportamiento de la página cuando aparece el teclado virtual. Cambia si los campos de entrada están ocultos o la animación es lenta"
        AppLanguage.FRENCH -> "Contrôler le comportement de la page quand le clavier virtuel apparaît. Changez si les champs sont masqués ou l'animation saccade"
        AppLanguage.GERMAN -> "Seitenverhalten steuern, wenn Soft-Tastatur erscheint. Wechseln, wenn Eingabefelder verdeckt oder Animation ruckelt"
        AppLanguage.RUSSIAN -> "Управление поведением страницы при появлении soft-клавиатуры. Переключите, если поля ввода скрыты или анимация тормозит"
        AppLanguage.JAPANESE -> "ソフトキーボード表示時のページ動作を制御。入力欄が隠れるまたはアニメがカクつく場合は切替"
        AppLanguage.KOREAN -> "소프트 키보드 표시 시 페이지 동작 제어. 입력 필드가 숨겨지거나 애니메이션이 끊기면 전환"
    }

    val keyboardAdjustResize: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "推起页面"
        AppLanguage.ENGLISH -> "Resize Content"
        AppLanguage.ARABIC -> "تغيير حجم المحتوى"
        AppLanguage.PORTUGUESE -> "Redimensionar conteúdo"
        AppLanguage.SPANISH -> "Redimensionar contenido"
        AppLanguage.FRENCH -> "Redimensionner le contenu"
        AppLanguage.GERMAN -> "Inhalt anpassen"
        AppLanguage.RUSSIAN -> "Изменить размер содержимого"
        AppLanguage.JAPANESE -> "コンテンツをリサイズ"
        AppLanguage.KOREAN -> "콘텐츠 크기 조정"
    }

    val keyboardAdjustResizeHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "键盘会向上推页面，确保输入框始终可见（在某些设备上可能有轻微卡顿）"
        AppLanguage.ENGLISH -> "Keyboard pushes page up, ensuring input fields remain visible (may be slightly laggy on some devices)"
        AppLanguage.ARABIC -> "تدفع لوحة المفاتيح الصفحة لأعلى، مما يضمن بقاء حقول الإدخال مرئية (قد تكون بطيئة قليلاً على بعض الأجهزة)"
        AppLanguage.PORTUGUESE -> "Teclado empurra página para cima, garantindo campos visíveis (pode ser levemente lento em alguns dispositivos)"
        AppLanguage.SPANISH -> "Teclado empuja página hacia arriba, garantizando campos visibles (puede ser ligeramente lento en algunos dispositivos)"
        AppLanguage.FRENCH -> "Le clavier pousse la page vers le haut, garantissant des champs visibles (peut être légèrement lent sur certains appareils)"
        AppLanguage.GERMAN -> "Tastatur schiebt Seite nach oben, stellt sichtbare Eingabefelder sicher (kann auf einigen Geräten leicht ruckeln)"
        AppLanguage.RUSSIAN -> "Клавиатура сдвигает страницу вверх, обеспечивая видимость полей ввода (может слегка тормозить на некоторых устройствах)"
        AppLanguage.JAPANESE -> "キーボードがページを上に押し上げ、入力欄の可視性を確保（一部デバイスでわずかに遅延する場合あり）"
        AppLanguage.KOREAN -> "키보드가 페이지를 위로 밀어 올려 입력 필드 가시성을 보장 (일부 기기에서 약간 지연될 수 있음)"
    }

    val keyboardAdjustNothing: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "覆盖页面"
        AppLanguage.ENGLISH -> "Overlay Content"
        AppLanguage.ARABIC -> "تراكب المحتوى"
        AppLanguage.PORTUGUESE -> "Sobrepor conteúdo"
        AppLanguage.SPANISH -> "Superponer contenido"
        AppLanguage.FRENCH -> "Superposer le contenu"
        AppLanguage.GERMAN -> "Inhalt überlagern"
        AppLanguage.RUSSIAN -> "Наложить содержимое"
        AppLanguage.JAPANESE -> "コンテンツをオーバーレイ"
        AppLanguage.KOREAN -> "콘텐츠 오버레이"
    }

    val keyboardAdjustNothingHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "键盘直接覆盖页面，无布局调整动画（更流畅，但可能遮挡输入框）"
        AppLanguage.ENGLISH -> "Keyboard overlays page directly without layout animation (smoother, but may hide input fields)"
        AppLanguage.ARABIC -> "تتراكب لوحة المفاتيح على الصفحة مباشرة بدون رسوم متحركة للتخطيط (أكثر سلاسة، لكن قد تخفي حقول الإدخال)"
        AppLanguage.PORTUGUESE -> "Teclado sobrepõe a página diretamente sem animação de layout (mais fluido, mas pode ocultar campos)"
        AppLanguage.SPANISH -> "Teclado superpone la página directamente sin animación de layout (más fluido, pero puede ocultar campos)"
        AppLanguage.FRENCH -> "Le clavier superpose la page directement sans animation de layout (plus fluide, mais peut masquer les champs)"
        AppLanguage.GERMAN -> "Tastatur überlagert Seite direkt ohne Layout-Animation (flüssiger, kann aber Felder verdecken)"
        AppLanguage.RUSSIAN -> "Клавиатура накладывается на страницу без анимации лейаута (плавнее, но может скрыть поля ввода)"
        AppLanguage.JAPANESE -> "キーボードがレイアウトアニメーションなしでページに直接重なる（より滑らかだが入力欄を隠す可能性）"
        AppLanguage.KOREAN -> "키보드가 레이아웃 애니메이션 없이 페이지를 직접 덮음 (더 부드럽지만 입력 필드를 가릴 수 있음)"
    }

    val showFloatingBackButtonLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "悬浮返回按钮"
        AppLanguage.ENGLISH -> "Floating Back Button"
        AppLanguage.ARABIC -> "زر العودة العائم"
        AppLanguage.PORTUGUESE -> "Botão voltar flutuante"
        AppLanguage.SPANISH -> "Botón atrás flotante"
        AppLanguage.FRENCH -> "Bouton retour flottant"
        AppLanguage.GERMAN -> "Schwebende Zurück-Taste"
        AppLanguage.RUSSIAN -> "Плавающая кнопка назад"
        AppLanguage.JAPANESE -> "フローティング戻るボタン"
        AppLanguage.KOREAN -> "플로팅 뒤로 버튼"
    }

    val showFloatingBackButtonHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "全屏模式下在左上角显示悬浮返回按钮。如果与网页 UI 冲突，可关闭此选项"
        AppLanguage.ENGLISH -> "Show a floating back button at top-left in fullscreen mode. Disable if it conflicts with web page UI"
        AppLanguage.ARABIC -> "عرض زر عودة عائم في الزاوية العلوية اليسرى في وضع ملء الشاشة. قم بتعطيله إذا تعارض مع واجهة صفحة الويب"
        AppLanguage.PORTUGUESE -> "Mostrar botão voltar flutuante no canto superior esquerdo em tela cheia. Desative se conflitar com a UI da página"
        AppLanguage.SPANISH -> "Mostrar botón atrás flotante en esquina superior izquierda en pantalla completa. Desactivar si conflictúa con la UI de la página"
        AppLanguage.FRENCH -> "Afficher un bouton retour flottant en haut à gauche en plein écran. Désactiver s'il entre en conflit avec l'UI de la page"
        AppLanguage.GERMAN -> "Schwebende Zurück-Taste oben links im Vollbild anzeigen. Deaktivieren bei Konflikt mit Web-UI"
        AppLanguage.RUSSIAN -> "Показывать плавающую кнопку «назад» вверху слева в полноэкранном режиме. Отключите при конфликте с UI страницы"
        AppLanguage.JAPANESE -> "全画面モードで左上にフローティング戻るボタンを表示。ページUIと競合する場合は無効化"
        AppLanguage.KOREAN -> "전체 화면 모드에서 왼쪽 상단에 플로팅 뒤로 버튼 표시. 페이지 UI와 충돌 시 비활성화"
    }

    val projectIssuesDetected: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "项目需要确认"
        AppLanguage.ENGLISH -> "Project needs review"
        AppLanguage.ARABIC -> "المشروع يحتاج إلى مراجعة"
        AppLanguage.PORTUGUESE -> "Projeto precisa de revisão"
        AppLanguage.SPANISH -> "El proyecto necesita revisión"
        AppLanguage.FRENCH -> "Le projet nécessite une révision"
        AppLanguage.GERMAN -> "Projekt braucht Überprüfung"
        AppLanguage.RUSSIAN -> "Проект требует проверки"
        AppLanguage.JAPANESE -> "プロジェクト要確認"
        AppLanguage.KOREAN -> "프로젝트 검토 필요"
    }

    val errorsCount: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "%d 个错误"
        AppLanguage.ENGLISH -> "%d errors"
        AppLanguage.ARABIC -> "%d أخطاء"
        AppLanguage.PORTUGUESE -> "%d erros"
        AppLanguage.SPANISH -> "%d errores"
        AppLanguage.FRENCH -> "%d erreurs"
        AppLanguage.GERMAN -> "%d Fehler"
        AppLanguage.RUSSIAN -> "%d ошибок"
        AppLanguage.JAPANESE -> "%d 件のエラー"
        AppLanguage.KOREAN -> "%d개 오류"
    }

    val warningsCount: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "%d 个警告"
        AppLanguage.ENGLISH -> "%d warnings"
        AppLanguage.ARABIC -> "%d تحذيرات"
        AppLanguage.PORTUGUESE -> "%d avisos"
        AppLanguage.SPANISH -> "%d advertencias"
        AppLanguage.FRENCH -> "%d avertissements"
        AppLanguage.GERMAN -> "%d Warnungen"
        AppLanguage.RUSSIAN -> "%d предупреждений"
        AppLanguage.JAPANESE -> "%d 件の警告"
        AppLanguage.KOREAN -> "%d개 경고"
    }

    val autoFixHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "请确认这些文件引用是否需要一并导入"
        AppLanguage.ENGLISH -> "Review these file references before creating the app."
        AppLanguage.ARABIC -> "راجع مراجع الملفات هذه قبل إنشاء التطبيق."
        AppLanguage.PORTUGUESE -> "Revise estas referências de arquivos antes de criar o app."
        AppLanguage.SPANISH -> "Revisa estas referencias de archivos antes de crear la app."
        AppLanguage.FRENCH -> "Vérifiez ces références de fichiers avant de créer l'app."
        AppLanguage.GERMAN -> "Prüfen Sie diese Dateireferenzen vor der App-Erstellung."
        AppLanguage.RUSSIAN -> "Проверьте эти ссылки на файлы перед созданием приложения."
        AppLanguage.JAPANESE -> "アプリ作成前にこれらのファイル参照を確認してください。"
        AppLanguage.KOREAN -> "앱 생성 전 이 파일 참조를 확인하세요."
    }

    val viewAnalysisResult: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "查看详情"
        AppLanguage.ENGLISH -> "View Details"
        AppLanguage.ARABIC -> "عرض التفاصيل"
        AppLanguage.PORTUGUESE -> "Ver detalhes"
        AppLanguage.SPANISH -> "Ver detalles"
        AppLanguage.FRENCH -> "Voir détails"
        AppLanguage.GERMAN -> "Details anzeigen"
        AppLanguage.RUSSIAN -> "Подробнее"
        AppLanguage.JAPANESE -> "詳細を表示"
        AppLanguage.KOREAN -> "상세 보기"
    }

    val htmlAppTip: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "HTML 必选，CSS/JS 可选；有引用就一并选择。"
        AppLanguage.ENGLISH -> "HTML is required. Add CSS/JS if the page references them."
        AppLanguage.ARABIC -> "تلميح: ملف HTML مطلوب، ملفات CSS و JS اختيارية. إذا كان HTML يشير إلى CSS أو JS، يرجى اختيار الملفات المقابلة."
        AppLanguage.PORTUGUESE -> "HTML é obrigatório. Adicione CSS/JS se a página os referenciar."
        AppLanguage.SPANISH -> "HTML es obligatorio. Añade CSS/JS si la página los referencia."
        AppLanguage.FRENCH -> "HTML est obligatoire. Ajoutez CSS/JS si la page les référence."
        AppLanguage.GERMAN -> "HTML ist erforderlich. CSS/JS hinzufügen, wenn die Seite sie referenziert."
        AppLanguage.RUSSIAN -> "HTML обязателен. Добавьте CSS/JS, если страница их ссылается."
        AppLanguage.JAPANESE -> "HTMLは必須。ページが参照する場合はCSS/JSを追加。"
        AppLanguage.KOREAN -> "HTML은 필수. 페이지가 참조하면 CSS/JS를 추가하세요."
    }

    val featureTip: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "激活码、BGM 等功能可在创建后编辑。"
        AppLanguage.ENGLISH -> "Add activation codes, BGM, and more after creation."
        AppLanguage.ARABIC -> "يمكن إضافة ميزات مثل رمز التفعيل والموسيقى الخلفية عبر 'تعديل' في إدارة المشروع بعد الإنشاء."
        AppLanguage.PORTUGUESE -> "Adicione códigos de ativação, BGM e mais após a criação."
        AppLanguage.SPANISH -> "Añade códigos de activación, BGM y más tras la creación."
        AppLanguage.FRENCH -> "Ajoutez codes d'activation, BGM et plus après création."
        AppLanguage.GERMAN -> "Füge Aktivierungscodes, BGM u. v. m. nach der Erstellung hinzu."
        AppLanguage.RUSSIAN -> "Добавьте коды активации, фоновую музыку и др. после создания."
        AppLanguage.JAPANESE -> "作成後にアクティベーションコード、BGMなどを追加できます。"
        AppLanguage.KOREAN -> "생성 후 활성화 코드, BGM 등을 추가할 수 있습니다."
    }

    val aboutFileReference: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "关于文件引用"
        AppLanguage.ENGLISH -> "About File References"
        AppLanguage.ARABIC -> "حول مراجع الملفات"
        AppLanguage.PORTUGUESE -> "Sobre referências de arquivos"
        AppLanguage.SPANISH -> "Acerca de referencias de archivos"
        AppLanguage.FRENCH -> "À propos des références de fichiers"
        AppLanguage.GERMAN -> "Über Dateireferenzen"
        AppLanguage.RUSSIAN -> "О ссылках на файлы"
        AppLanguage.JAPANESE -> "ファイル参照について"
        AppLanguage.KOREAN -> "파일 참조 정보"
    }

    val fileReferenceHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "• CSS/JS 会自动内联\n• 绝对路径会转换\n• 尽量用相对路径"
        AppLanguage.ENGLISH -> "• CSS/JS are inlined automatically\n• Absolute paths are converted\n• Prefer relative paths"
        AppLanguage.ARABIC -> "• سيقوم التطبيق بدمج CSS و JS تلقائيًا في HTML\n• سيتم تحويل المسارات المطلقة (مثل /css/style.css) تلقائيًا\n• يُنصح باستخدام المسارات النسبية (مثل ./style.css)"
        AppLanguage.PORTUGUESE -> "• CSS/JS são inline automaticamente\n• Caminhos absolutos são convertidos\n• Prefira caminhos relativos"
        AppLanguage.SPANISH -> "• CSS/JS se inlinean automáticamente\n• Rutas absolutas se convierten\n• Prefiere rutas relativas"
        AppLanguage.FRENCH -> "• CSS/JS sont intégrés automatiquement\n• Chemins absolus convertis\n• Préférez les chemins relatifs"
        AppLanguage.GERMAN -> "• CSS/JS werden automatisch inline eingebettet\n• Absolute Pfade werden konvertiert\n• Relative Pfade bevorzugen"
        AppLanguage.RUSSIAN -> "• CSS/JS встраиваются автоматически\n• Абсолютные пути конвертируются\n• Предпочитайте относительные пути"
        AppLanguage.JAPANESE -> "• CSS/JSは自動でインライン化\n• 絶対パスは変換されます\n• 相対パスを推奨"
        AppLanguage.KOREAN -> "• CSS/JS는 자동 인라인됨\n• 절대 경로는 변환됨\n• 상대 경로를 선호하세요"
    }

    val projectAnalysisResult: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "项目检查"
        AppLanguage.ENGLISH -> "Project Check"
        AppLanguage.ARABIC -> "فحص المشروع"
        AppLanguage.PORTUGUESE -> "Verificação de projeto"
        AppLanguage.SPANISH -> "Verificación de proyecto"
        AppLanguage.FRENCH -> "Vérification du projet"
        AppLanguage.GERMAN -> "Projektprüfung"
        AppLanguage.RUSSIAN -> "Проверка проекта"
        AppLanguage.JAPANESE -> "プロジェクト確認"
        AppLanguage.KOREAN -> "프로젝트 확인"
    }

    val detectedIssues: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "检测到的问题"
        AppLanguage.ENGLISH -> "Detected Issues"
        AppLanguage.ARABIC -> "المشاكل المكتشفة"
        AppLanguage.PORTUGUESE -> "Problemas detectados"
        AppLanguage.SPANISH -> "Problemas detectados"
        AppLanguage.FRENCH -> "Problèmes détectés"
        AppLanguage.GERMAN -> "Erkannte Probleme"
        AppLanguage.RUSSIAN -> "Обнаруженные проблемы"
        AppLanguage.JAPANESE -> "検出された問題"
        AppLanguage.KOREAN -> "감지된 문제"
    }

    val gotIt: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "知道了"
        AppLanguage.ENGLISH -> "Got it"
        AppLanguage.ARABIC -> "فهمت"
        AppLanguage.PORTUGUESE -> "Entendi"
        AppLanguage.SPANISH -> "Entendido"
        AppLanguage.FRENCH -> "Compris"
        AppLanguage.GERMAN -> "Verstanden"
        AppLanguage.RUSSIAN -> "Понятно"
        AppLanguage.JAPANESE -> "了解"
        AppLanguage.KOREAN -> "확인"
    }

    val zipImportMode: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "ZIP 导入"
        AppLanguage.ENGLISH -> "ZIP Import"
        AppLanguage.ARABIC -> "استيراد ZIP"
        AppLanguage.PORTUGUESE -> "Importar ZIP"
        AppLanguage.SPANISH -> "Importar ZIP"
        AppLanguage.FRENCH -> "Import ZIP"
        AppLanguage.GERMAN -> "ZIP-Import"
        AppLanguage.RUSSIAN -> "Импорт ZIP"
        AppLanguage.JAPANESE -> "ZIPインポート"
        AppLanguage.KOREAN -> "ZIP 가져오기"
    }

    val manualSelectMode: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "手动选择"
        AppLanguage.ENGLISH -> "Manual Select"
        AppLanguage.ARABIC -> "اختيار يدوي"
        AppLanguage.PORTUGUESE -> "Seleção manual"
        AppLanguage.SPANISH -> "Selección manual"
        AppLanguage.FRENCH -> "Sélection manuelle"
        AppLanguage.GERMAN -> "Manuelle Auswahl"
        AppLanguage.RUSSIAN -> "Ручной выбор"
        AppLanguage.JAPANESE -> "手動選択"
        AppLanguage.KOREAN -> "수동 선택"
    }

    val selectZipFile: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择 ZIP 文件"
        AppLanguage.ENGLISH -> "Select ZIP File"
        AppLanguage.ARABIC -> "اختيار ملف ZIP"
        AppLanguage.PORTUGUESE -> "Selecionar arquivo ZIP"
        AppLanguage.SPANISH -> "Seleccionar archivo ZIP"
        AppLanguage.FRENCH -> "Sélectionner un fichier ZIP"
        AppLanguage.GERMAN -> "ZIP-Datei auswählen"
        AppLanguage.RUSSIAN -> "Выбрать ZIP-файл"
        AppLanguage.JAPANESE -> "ZIPファイルを選択"
        AppLanguage.KOREAN -> "ZIP 파일 선택"
    }

    val selectZipHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择包含 HTML 项目的 ZIP 文件，自动解压并配置所有资源"
        AppLanguage.ENGLISH -> "Select a ZIP file containing your HTML project, auto-extract and configure all resources"
        AppLanguage.ARABIC -> "اختر ملف ZIP يحتوي على مشروع HTML، سيتم استخراج وتكوين جميع الموارد تلقائيًا"
        AppLanguage.PORTUGUESE -> "Selecione um ZIP contendo seu projeto HTML, extração e configuração automáticas de todos os recursos"
        AppLanguage.SPANISH -> "Selecciona un ZIP que contenga tu proyecto HTML, extracción y configuración automáticas de todos los recursos"
        AppLanguage.FRENCH -> "Sélectionnez un ZIP contenant votre projet HTML, extraction et configuration automatiques de toutes les ressources"
        AppLanguage.GERMAN -> "Wählen Sie ein ZIP mit Ihrem HTML-Projekt, Auto-Extraktion und Konfiguration aller Ressourcen"
        AppLanguage.RUSSIAN -> "Выберите ZIP с HTML-проектом, авто-распаковка и настройка всех ресурсов"
        AppLanguage.JAPANESE -> "HTMLプロジェクトを含むZIPを選択、すべてのリソースを自動展開・設定"
        AppLanguage.KOREAN -> "HTML 프로젝트가 포함된 ZIP 선택, 모든 리소스 자동 추출 및 구성"
    }

    val zipImporting: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "正在解压并分析项目..."
        AppLanguage.ENGLISH -> "Extracting and analyzing project..."
        AppLanguage.ARABIC -> "جاري استخراج وتحليل المشروع..."
        AppLanguage.PORTUGUESE -> "Extraindo e analisando projeto..."
        AppLanguage.SPANISH -> "Extrayendo y analizando proyecto..."
        AppLanguage.FRENCH -> "Extraction et analyse du projet..."
        AppLanguage.GERMAN -> "Projekt wird extrahiert und analysiert..."
        AppLanguage.RUSSIAN -> "Распаковка и анализ проекта..."
        AppLanguage.JAPANESE -> "プロジェクトを展開・分析中..."
        AppLanguage.KOREAN -> "프로젝트 추출 및 분석 중..."
    }

    val zipImportFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "ZIP 导入失败: %s"
        AppLanguage.ENGLISH -> "ZIP import failed: %s"
        AppLanguage.ARABIC -> "فشل استيراد ZIP: %s"
        AppLanguage.PORTUGUESE -> "Falha na importação ZIP: %s"
        AppLanguage.SPANISH -> "Falló la importación ZIP: %s"
        AppLanguage.FRENCH -> "Échec de l'import ZIP : %s"
        AppLanguage.GERMAN -> "ZIP-Import fehlgeschlagen: %s"
        AppLanguage.RUSSIAN -> "Ошибка импорта ZIP: %s"
        AppLanguage.JAPANESE -> "ZIPインポート失敗: %s"
        AppLanguage.KOREAN -> "ZIP 가져오기 실패: %s"
    }

    val zipProjectAnalysis: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "项目检查"
        AppLanguage.ENGLISH -> "Project Check"
        AppLanguage.ARABIC -> "فحص المشروع"
        AppLanguage.PORTUGUESE -> "Verificação de projeto"
        AppLanguage.SPANISH -> "Verificación de proyecto"
        AppLanguage.FRENCH -> "Vérification du projet"
        AppLanguage.GERMAN -> "Projektprüfung"
        AppLanguage.RUSSIAN -> "Проверка проекта"
        AppLanguage.JAPANESE -> "プロジェクト確認"
        AppLanguage.KOREAN -> "프로젝트 확인"
    }

    val zipEntryFile: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "入口文件"
        AppLanguage.ENGLISH -> "Entry File"
        AppLanguage.ARABIC -> "ملف الدخول"
        AppLanguage.PORTUGUESE -> "Arquivo de entrada"
        AppLanguage.SPANISH -> "Archivo de entrada"
        AppLanguage.FRENCH -> "Fichier d'entrée"
        AppLanguage.GERMAN -> "Einstiegsdatei"
        AppLanguage.RUSSIAN -> "Стартовый файл"
        AppLanguage.JAPANESE -> "エントリファイル"
        AppLanguage.KOREAN -> "진입 파일"
    }

    val zipResourceStats: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "资源统计"
        AppLanguage.ENGLISH -> "Resource Stats"
        AppLanguage.ARABIC -> "إحصائيات الموارد"
        AppLanguage.PORTUGUESE -> "Estatísticas de recursos"
        AppLanguage.SPANISH -> "Estadísticas de recursos"
        AppLanguage.FRENCH -> "Statistiques des ressources"
        AppLanguage.GERMAN -> "Ressourcenstatistik"
        AppLanguage.RUSSIAN -> "Статистика ресурсов"
        AppLanguage.JAPANESE -> "リソース統計"
        AppLanguage.KOREAN -> "리소스 통계"
    }

    val zipTotalFiles: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "%d 个文件"
        AppLanguage.ENGLISH -> "%d files"
        AppLanguage.ARABIC -> "%d ملفات"
        AppLanguage.PORTUGUESE -> "%d arquivos"
        AppLanguage.SPANISH -> "%d archivos"
        AppLanguage.FRENCH -> "%d fichiers"
        AppLanguage.GERMAN -> "%d Dateien"
        AppLanguage.RUSSIAN -> "%d файлов"
        AppLanguage.JAPANESE -> "%d ファイル"
        AppLanguage.KOREAN -> "%d개 파일"
    }

    val zipTotalSize: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "总大小: %s"
        AppLanguage.ENGLISH -> "Total size: %s"
        AppLanguage.ARABIC -> "الحجم الإجمالي: %s"
        AppLanguage.PORTUGUESE -> "Tamanho total: %s"
        AppLanguage.SPANISH -> "Tamaño total: %s"
        AppLanguage.FRENCH -> "Taille totale : %s"
        AppLanguage.GERMAN -> "Gesamtgröße: %s"
        AppLanguage.RUSSIAN -> "Общий размер: %s"
        AppLanguage.JAPANESE -> "合計サイズ: %s"
        AppLanguage.KOREAN -> "전체 크기: %s"
    }

    val zipChangeEntry: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "更换入口文件"
        AppLanguage.ENGLISH -> "Change Entry File"
        AppLanguage.ARABIC -> "تغيير ملف الدخول"
        AppLanguage.PORTUGUESE -> "Alterar arquivo de entrada"
        AppLanguage.SPANISH -> "Cambiar archivo de entrada"
        AppLanguage.FRENCH -> "Changer le fichier d'entrée"
        AppLanguage.GERMAN -> "Einstiegsdatei ändern"
        AppLanguage.RUSSIAN -> "Сменить стартовый файл"
        AppLanguage.JAPANESE -> "エントリファイルを変更"
        AppLanguage.KOREAN -> "진입 파일 변경"
    }

    val zipReimport: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "重新导入"
        AppLanguage.ENGLISH -> "Re-import"
        AppLanguage.ARABIC -> "إعادة الاستيراد"
        AppLanguage.PORTUGUESE -> "Reimportar"
        AppLanguage.SPANISH -> "Reimportar"
        AppLanguage.FRENCH -> "Réimporter"
        AppLanguage.GERMAN -> "Erneut importieren"
        AppLanguage.RUSSIAN -> "Переимпорт"
        AppLanguage.JAPANESE -> "再インポート"
        AppLanguage.KOREAN -> "다시 가져오기"
    }

    val zipTip: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "提示：ZIP 项目会保留完整目录结构，所有相对路径引用（CSS/JS/图片/音视频/字体等）自动生效，无需手动配置。"
        AppLanguage.ENGLISH -> "Tip: ZIP projects preserve the full directory structure. All relative path references (CSS/JS/images/media/fonts etc.) work automatically."
        AppLanguage.ARABIC -> "تلميح: تحتفظ مشاريع ZIP ببنية المجلدات الكاملة. تعمل جميع المراجع ذات المسارات النسبية (CSS/JS/صور/وسائط/خطوط إلخ) تلقائيًا."
        AppLanguage.PORTUGUESE -> "Dica: projetos ZIP preservam a estrutura completa de diretórios. Todas as referências de caminhos relativos (CSS/JS/imagens/mídia/fontes etc.) funcionam automaticamente."
        AppLanguage.SPANISH -> "Consejo: los proyectos ZIP preservan la estructura completa de directorios. Todas las referencias de rutas relativas (CSS/JS/imágenes/multimedia/fuentes etc.) funcionan automáticamente."
        AppLanguage.FRENCH -> "Astuce : les projets ZIP préservent la structure complète des répertoires. Toutes les références de chemins relatifs (CSS/JS/images/médias/polices etc.) fonctionnent automatiquement."
        AppLanguage.GERMAN -> "Tipp: ZIP-Projekte bewahren die volle Verzeichnisstruktur. Alle relativen Pfadreferenzen (CSS/JS/Bilder/Medien/Fonts usw.) funktionieren automatisch."
        AppLanguage.RUSSIAN -> "Подсказка: ZIP-проекты сохраняют полную структуру каталогов. Все относительные ссылки (CSS/JS/изображения/медиа/шрифты и т. д.) работают автоматически."
        AppLanguage.JAPANESE -> "ヒント: ZIPプロジェクトは完全なディレクトリ構造を保持します。すべての相対パス参照（CSS/JS/画像/メディア/フォントなど）が自動的に機能します。"
        AppLanguage.KOREAN -> "팁: ZIP 프로젝트는 전체 디렉터리 구조를 보존합니다. 모든 상대 경로 참조(CSS/JS/이미지/미디어/글꼴 등)가 자동으로 작동합니다."
    }

    val zipNoHtmlWarning: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "ZIP 中未找到 HTML 文件，请确认文件内容"
        AppLanguage.ENGLISH -> "No HTML files found in ZIP, please verify contents"
        AppLanguage.ARABIC -> "لم يتم العثور على ملفات HTML في ZIP، يرجى التحقق من المحتويات"
        AppLanguage.PORTUGUESE -> "Nenhum arquivo HTML encontrado no ZIP, verifique o conteúdo"
        AppLanguage.SPANISH -> "No se encontraron archivos HTML en el ZIP, verifica el contenido"
        AppLanguage.FRENCH -> "Aucun fichier HTML trouvé dans le ZIP, vérifiez le contenu"
        AppLanguage.GERMAN -> "Keine HTML-Dateien im ZIP gefunden, bitte Inhalt prüfen"
        AppLanguage.RUSSIAN -> "В ZIP не найдено HTML-файлов, проверьте содержимое"
        AppLanguage.JAPANESE -> "ZIPにHTMLファイルが見つかりません、内容を確認してください"
        AppLanguage.KOREAN -> "ZIP에서 HTML 파일을 찾을 수 없습니다. 내용을 확인하세요"
    }

    val zipSelectEntryTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择入口文件"
        AppLanguage.ENGLISH -> "Select Entry File"
        AppLanguage.ARABIC -> "اختيار ملف الدخول"
        AppLanguage.PORTUGUESE -> "Selecionar arquivo de entrada"
        AppLanguage.SPANISH -> "Seleccionar archivo de entrada"
        AppLanguage.FRENCH -> "Sélectionner le fichier d'entrée"
        AppLanguage.GERMAN -> "Einstiegsdatei auswählen"
        AppLanguage.RUSSIAN -> "Выбрать стартовый файл"
        AppLanguage.JAPANESE -> "エントリファイルを選択"
        AppLanguage.KOREAN -> "진입 파일 선택"
    }

    val zipFileTreeTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "文件列表"
        AppLanguage.ENGLISH -> "File List"
        AppLanguage.ARABIC -> "قائمة الملفات"
        AppLanguage.PORTUGUESE -> "Lista de arquivos"
        AppLanguage.SPANISH -> "Lista de archivos"
        AppLanguage.FRENCH -> "Liste des fichiers"
        AppLanguage.GERMAN -> "Dateiliste"
        AppLanguage.RUSSIAN -> "Список файлов"
        AppLanguage.JAPANESE -> "ファイルリスト"
        AppLanguage.KOREAN -> "파일 목록"
    }

    val folderImportMode: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "文件夹导入"
        AppLanguage.ENGLISH -> "Folder Import"
        AppLanguage.ARABIC -> "استيراد مجلد"
        AppLanguage.PORTUGUESE -> "Importar pasta"
        AppLanguage.SPANISH -> "Importar carpeta"
        AppLanguage.FRENCH -> "Import de dossier"
        AppLanguage.GERMAN -> "Ordner-Import"
        AppLanguage.RUSSIAN -> "Импорт папки"
        AppLanguage.JAPANESE -> "フォルダインポート"
        AppLanguage.KOREAN -> "폴더 가져오기"
    }

    val folderSelectFolder: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择项目文件夹"
        AppLanguage.ENGLISH -> "Select Project Folder"
        AppLanguage.ARABIC -> "اختيار مجلد المشروع"
        AppLanguage.PORTUGUESE -> "Selecionar pasta do projeto"
        AppLanguage.SPANISH -> "Seleccionar carpeta del proyecto"
        AppLanguage.FRENCH -> "Sélectionner le dossier du projet"
        AppLanguage.GERMAN -> "Projektordner auswählen"
        AppLanguage.RUSSIAN -> "Выбрать папку проекта"
        AppLanguage.JAPANESE -> "プロジェクトフォルダを選択"
        AppLanguage.KOREAN -> "프로젝트 폴더 선택"
    }

    val folderSelectHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择包含 HTML 项目的文件夹，自动扫描并导入所有资源文件"
        AppLanguage.ENGLISH -> "Select a folder containing your HTML project, auto-scan and import all resource files"
        AppLanguage.ARABIC -> "اختر مجلدًا يحتوي على مشروع HTML، سيتم فحص واستيراد جميع ملفات الموارد تلقائيًا"
        AppLanguage.PORTUGUESE -> "Selecione uma pasta contendo seu projeto HTML, escaneamento e importação automáticos de todos os arquivos de recursos"
        AppLanguage.SPANISH -> "Selecciona una carpeta que contenga tu proyecto HTML, escaneo e importación automáticos de todos los archivos de recursos"
        AppLanguage.FRENCH -> "Sélectionnez un dossier contenant votre projet HTML, scan automatique et import de tous les fichiers de ressources"
        AppLanguage.GERMAN -> "Wählen Sie einen Ordner mit Ihrem HTML-Projekt, Auto-Scan und Import aller Ressourcendateien"
        AppLanguage.RUSSIAN -> "Выберите папку с HTML-проектом, авто-сканирование и импорт всех файлов ресурсов"
        AppLanguage.JAPANESE -> "HTMLプロジェクトを含むフォルダを選択、すべてのリソースファイルを自動スキャン・インポート"
        AppLanguage.KOREAN -> "HTML 프로젝트가 포함된 폴더 선택, 모든 리소스 파일 자동 스캔 및 가져오기"
    }

    val folderImporting: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "正在扫描并导入文件夹..."
        AppLanguage.ENGLISH -> "Scanning and importing folder..."
        AppLanguage.ARABIC -> "جاري فحص واستيراد المجلد..."
        AppLanguage.PORTUGUESE -> "Escaneando e importando pasta..."
        AppLanguage.SPANISH -> "Escaneando e importando carpeta..."
        AppLanguage.FRENCH -> "Scan et import du dossier..."
        AppLanguage.GERMAN -> "Ordner wird gescannt und importiert..."
        AppLanguage.RUSSIAN -> "Сканирование и импорт папки..."
        AppLanguage.JAPANESE -> "フォルダをスキャン・インポート中..."
        AppLanguage.KOREAN -> "폴더 스캔 및 가져오는 중..."
    }

    val folderNoHtmlWarning: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "文件夹中未找到 HTML 文件，请确认选择的目录"
        AppLanguage.ENGLISH -> "No HTML files found in folder, please verify the selected directory"
        AppLanguage.ARABIC -> "لم يتم العثور على ملفات HTML في المجلد، يرجى التحقق من الدليل المحدد"
        AppLanguage.PORTUGUESE -> "Nenhum arquivo HTML na pasta, verifique o diretório selecionado"
        AppLanguage.SPANISH -> "No se encontraron archivos HTML en la carpeta, verifica el directorio seleccionado"
        AppLanguage.FRENCH -> "Aucun fichier HTML dans le dossier, vérifiez le répertoire sélectionné"
        AppLanguage.GERMAN -> "Keine HTML-Dateien im Ordner, bitte Verzeichnis prüfen"
        AppLanguage.RUSSIAN -> "В папке нет HTML-файлов, проверьте выбранный каталог"
        AppLanguage.JAPANESE -> "フォルダにHTMLファイルが見つかりません、選択ディレクトリを確認してください"
        AppLanguage.KOREAN -> "폴더에서 HTML 파일을 찾을 수 없습니다. 선택한 디렉터리를 확인하세요"
    }

    val folderTip: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "提示：文件夹导入会保留完整目录结构，所有相对路径引用（CSS/JS/图片/音视频/字体等）自动生效，适合多文件 HTML 项目。"
        AppLanguage.ENGLISH -> "Tip: Folder import preserves the full directory structure. All relative path references (CSS/JS/images/media/fonts etc.) work automatically, ideal for multi-file HTML projects."
        AppLanguage.ARABIC -> "تلميح: يحتفظ استيراد المجلد ببنية المجلدات الكاملة. تعمل جميع المراجع ذات المسارات النسبية تلقائيًا، مثالي لمشاريع HTML متعددة الملفات."
        AppLanguage.PORTUGUESE -> "Dica: importar pasta preserva a estrutura completa de diretórios. Todas as referências relativas (CSS/JS/imagens/mídia/fontes etc.) funcionam automaticamente, ideal para projetos HTML multi-arquivo."
        AppLanguage.SPANISH -> "Consejo: importar carpeta preserva la estructura completa de directorios. Todas las referencias relativas (CSS/JS/imágenes/multimedia/fuentes etc.) funcionan automáticamente, ideal para proyectos HTML multi-archivo."
        AppLanguage.FRENCH -> "Astuce : l'import de dossier préserve la structure complète des répertoires. Toutes les références relatives (CSS/JS/images/médias/polices etc.) fonctionnent automatiquement, idéal pour les projets HTML multi-fichiers."
        AppLanguage.GERMAN -> "Tipp: Ordner-Import bewahrt die volle Verzeichnisstruktur. Alle relativen Pfadreferenzen (CSS/JS/Bilder/Medien/Fonts usw.) funktionieren automatisch, ideal für Multi-File-HTML-Projekte."
        AppLanguage.RUSSIAN -> "Подсказка: импорт папки сохраняет полную структуру каталогов. Все относительные ссылки (CSS/JS/изображения/медиа/шрифты и т. д.) работают автоматически, идеально для multi-file HTML-проектов."
        AppLanguage.JAPANESE -> "ヒント: フォルダインポートは完全なディレクトリ構造を保持します。すべての相対パス参照（CSS/JS/画像/メディア/フォントなど）が自動的に機能し、複数ファイルHTMLプロジェクトに最適です。"
        AppLanguage.KOREAN -> "팁: 폴더 가져오기는 전체 디렉터리 구조를 보존합니다. 모든 상대 경로 참조(CSS/JS/이미지/미디어/글꼴 등)가 자동으로 작동하며, 다중 파일 HTML 프로젝트에 적합합니다."
    }

    val folderImportFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "文件夹导入失败: %s"
        AppLanguage.ENGLISH -> "Folder import failed: %s"
        AppLanguage.ARABIC -> "فشل استيراد المجلد: %s"
        AppLanguage.PORTUGUESE -> "Falha na importação da pasta: %s"
        AppLanguage.SPANISH -> "Falló la importación de la carpeta: %s"
        AppLanguage.FRENCH -> "Échec de l'import du dossier : %s"
        AppLanguage.GERMAN -> "Ordner-Import fehlgeschlagen: %s"
        AppLanguage.RUSSIAN -> "Ошибка импорта папки: %s"
        AppLanguage.JAPANESE -> "フォルダインポート失敗: %s"
        AppLanguage.KOREAN -> "폴더 가져오기 실패: %s"
    }

    val htmlAddFiles: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "添加文件"
        AppLanguage.ENGLISH -> "Add Files"
        AppLanguage.ARABIC -> "إضافة ملفات"
        AppLanguage.PORTUGUESE -> "Adicionar arquivos"
        AppLanguage.SPANISH -> "Añadir archivos"
        AppLanguage.FRENCH -> "Ajouter fichiers"
        AppLanguage.GERMAN -> "Dateien hinzufügen"
        AppLanguage.RUSSIAN -> "Добавить файлы"
        AppLanguage.JAPANESE -> "ファイルを追加"
        AppLanguage.KOREAN -> "파일 추가"
    }

    val editApp: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "编辑应用"
        AppLanguage.ENGLISH -> "Edit App"
        AppLanguage.ARABIC -> "تعديل التطبيق"
        AppLanguage.PORTUGUESE -> "Editar app"
        AppLanguage.SPANISH -> "Editar app"
        AppLanguage.FRENCH -> "Modifier l'app"
        AppLanguage.GERMAN -> "App bearbeiten"
        AppLanguage.RUSSIAN -> "Изменить приложение"
        AppLanguage.JAPANESE -> "アプリを編集"
        AppLanguage.KOREAN -> "앱 편집"
    }

    val inputAppName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "输入应用显示名称"
        AppLanguage.ENGLISH -> "Enter app display name"
        AppLanguage.ARABIC -> "أدخل اسم عرض التطبيق"
        AppLanguage.PORTUGUESE -> "Digite o nome de exibição do app"
        AppLanguage.SPANISH -> "Introduce el nombre de visualización de la app"
        AppLanguage.FRENCH -> "Saisir le nom d'affichage de l'app"
        AppLanguage.GERMAN -> "App-Anzeigenamen eingeben"
        AppLanguage.RUSSIAN -> "Введите отображаемое имя приложения"
        AppLanguage.JAPANESE -> "アプリ表示名を入力"
        AppLanguage.KOREAN -> "앱 표시 이름 입력"
    }

    val activationCodeVerify: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "激活码"
        AppLanguage.ENGLISH -> "Activation Code"
        AppLanguage.ARABIC -> "رمز التفعيل"
        AppLanguage.PORTUGUESE -> "Código de ativação"
        AppLanguage.SPANISH -> "Código de activación"
        AppLanguage.FRENCH -> "Code d'activation"
        AppLanguage.GERMAN -> "Aktivierungscode"
        AppLanguage.RUSSIAN -> "Код активации"
        AppLanguage.JAPANESE -> "アクティベーションコード"
        AppLanguage.KOREAN -> "활성화 코드"
    }

    val activationModeLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "验证方式"
        AppLanguage.ENGLISH -> "Verification mode"
        AppLanguage.ARABIC -> "طريقة التحقق"
        AppLanguage.PORTUGUESE -> "Modo de verificação"
        AppLanguage.SPANISH -> "Modo de verificación"
        AppLanguage.FRENCH -> "Mode de vérification"
        AppLanguage.GERMAN -> "Verifizierungsmodus"
        AppLanguage.RUSSIAN -> "Режим проверки"
        AppLanguage.JAPANESE -> "認証方式"
        AppLanguage.KOREAN -> "인증 방식"
    }

    val activationModeLocal: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "本地激活码"
        AppLanguage.ENGLISH -> "Local activation codes"
        AppLanguage.ARABIC -> "رموز تفعيل محلية"
        AppLanguage.PORTUGUESE -> "Códigos de ativação locais"
        AppLanguage.SPANISH -> "Códigos de activación locales"
        AppLanguage.FRENCH -> "Codes d'activation locaux"
        AppLanguage.GERMAN -> "Lokale Aktivierungscodes"
        AppLanguage.RUSSIAN -> "Локальные коды активации"
        AppLanguage.JAPANESE -> "ローカル認証コード"
        AppLanguage.KOREAN -> "로컬 인증 코드"
    }

    val activationModeLocalDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "激活码打包进应用，离线可用，适合分发给少量用户"
        AppLanguage.ENGLISH -> "Codes ship inside the app and work offline — good for a small distribution list"
        AppLanguage.ARABIC -> "تُحزم الرموز داخل التطبيق وتعمل دون اتصال — مناسبة لقائمة توزيع صغيرة"
        AppLanguage.PORTUGUESE -> "Os códigos vão dentro do app e funcionam offline — bons para uma lista pequena de usuários"
        AppLanguage.SPANISH -> "Los códigos van dentro de la app y funcionan sin conexión — aptos para una lista pequeña"
        AppLanguage.FRENCH -> "Les codes sont intégrés à l'app et fonctionnent hors ligne — adaptés à une petite liste"
        AppLanguage.GERMAN -> "Codes liegen in der App und funktionieren offline — gut für eine kleine Verteilerliste"
        AppLanguage.RUSSIAN -> "Коды встроены в приложение и работают офлайн — подходят для небольшого списка пользователей"
        AppLanguage.JAPANESE -> "コードをアプリ内に同梱し、オフラインで動作します。少数の配布向け"
        AppLanguage.KOREAN -> "코드가 앱에 포함되어 오프라인으로 동작합니다 — 소규모 배포에 적합"
    }

    val activationModeRemote: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "在线验证"
        AppLanguage.ENGLISH -> "Online verification"
        AppLanguage.ARABIC -> "تحقق عبر الإنترنت"
        AppLanguage.PORTUGUESE -> "Verificação online"
        AppLanguage.SPANISH -> "Verificación en línea"
        AppLanguage.FRENCH -> "Vérification en ligne"
        AppLanguage.GERMAN -> "Online-Verifizierung"
        AppLanguage.RUSSIAN -> "Онлайн-проверка"
        AppLanguage.JAPANESE -> "オンライン認証"
        AppLanguage.KOREAN -> "온라인 인증"
    }

    val activationModeRemoteDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "由你的服务器实时验证，支持有效期与动态下发网址"
        AppLanguage.ENGLISH -> "Verified live by your server, with expiry and dynamic URL delivery"
        AppLanguage.ARABIC -> "يتحقق خادمك في الوقت الفعلي، مع انتهاء الصلاحية وتسليم عنوان URL ديناميكي"
        AppLanguage.PORTUGUESE -> "Verificado em tempo real pelo seu servidor, com expiração e entrega dinâmica de URL"
        AppLanguage.SPANISH -> "Verificado en vivo por tu servidor, con caducidad y entrega dinámica de URL"
        AppLanguage.FRENCH -> "Vérifié en direct par votre serveur, avec expiration et livraison dynamique d'URL"
        AppLanguage.GERMAN -> "Live von Ihrem Server geprüft, mit Ablauf und dynamischer URL-Auslieferung"
        AppLanguage.RUSSIAN -> "Проверяется вашим сервером в реальном времени, со сроком действия и динамической выдачей URL"
        AppLanguage.JAPANESE -> "独自サーバーがリアルタイムに検証。有効期限と動的 URL 配信に対応"
        AppLanguage.KOREAN -> "사용자의 서버가 실시간으로 검증합니다. 만료 및 동적 URL 전달 지원"
    }

    val remoteGuideCopyFull: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "复制全文"
        AppLanguage.ENGLISH -> "Copy all"
        AppLanguage.ARABIC -> "نسخ الكل"
        AppLanguage.PORTUGUESE -> "Copiar tudo"
        AppLanguage.SPANISH -> "Copiar todo"
        AppLanguage.FRENCH -> "Tout copier"
        AppLanguage.GERMAN -> "Alles kopieren"
        AppLanguage.RUSSIAN -> "Копировать всё"
        AppLanguage.JAPANESE -> "全文をコピー"
        AppLanguage.KOREAN -> "전체 복사"
    }

    val remoteGuideSaveDoc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "保存为文档"
        AppLanguage.ENGLISH -> "Save as document"
        AppLanguage.ARABIC -> "حفظ كمستند"
        AppLanguage.PORTUGUESE -> "Salvar como documento"
        AppLanguage.SPANISH -> "Guardar como documento"
        AppLanguage.FRENCH -> "Enregistrer en document"
        AppLanguage.GERMAN -> "Als Dokument speichern"
        AppLanguage.RUSSIAN -> "Сохранить как документ"
        AppLanguage.JAPANESE -> "ドキュメントとして保存"
        AppLanguage.KOREAN -> "문서로 저장"
    }

    val remoteGuideSaved: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "接口文档已保存"
        AppLanguage.ENGLISH -> "Guide saved"
        AppLanguage.ARABIC -> "تم حفظ الدليل"
        AppLanguage.PORTUGUESE -> "Guia salvo"
        AppLanguage.SPANISH -> "Guía guardada"
        AppLanguage.FRENCH -> "Guide enregistré"
        AppLanguage.GERMAN -> "Anleitung gespeichert"
        AppLanguage.RUSSIAN -> "Руководство сохранено"
        AppLanguage.JAPANESE -> "ドキュメントを保存しました"
        AppLanguage.KOREAN -> "문서를 저장했습니다"
    }

    val activationCodeHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "启用后，用户需要输入正确的激活码才能使用应用"
        AppLanguage.ENGLISH -> "When enabled, users need to enter correct activation code to use the app"
        AppLanguage.ARABIC -> "عند التفعيل، يحتاج المستخدمون إلى إدخال رمز التفعيل الصحيح لاستخدام التطبيق"
        AppLanguage.PORTUGUESE -> "Quando ativado, usuários precisam digitar o código correto para usar o app"
        AppLanguage.SPANISH -> "Cuando está activado, los usuarios deben introducir el código correcto para usar la app"
        AppLanguage.FRENCH -> "Lorsqu'activé, les utilisateurs doivent saisir le code correct pour utiliser l'app"
        AppLanguage.GERMAN -> "Wenn aktiviert, müssen Nutzer den korrekten Code eingeben, um die App zu nutzen"
        AppLanguage.RUSSIAN -> "Если включено, пользователи должны ввести правильный код активации для использования приложения"
        AppLanguage.JAPANESE -> "有効化すると、ユーザーは正しいアクティベーションコードを入力しないとアプリを使用できません"
        AppLanguage.KOREAN -> "활성화 시 사용자는 올바른 활성화 코드를 입력해야 앱을 사용할 수 있습니다"
    }

    val inputActivationCode: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "输入激活码"
        AppLanguage.ENGLISH -> "Enter activation code"
        AppLanguage.ARABIC -> "أدخل رمز التفعيل"
        AppLanguage.PORTUGUESE -> "Digite o código de ativação"
        AppLanguage.SPANISH -> "Introduce el código de activación"
        AppLanguage.FRENCH -> "Saisir le code d'activation"
        AppLanguage.GERMAN -> "Aktivierungscode eingeben"
        AppLanguage.RUSSIAN -> "Введите код активации"
        AppLanguage.JAPANESE -> "アクティベーションコードを入力"
        AppLanguage.KOREAN -> "활성화 코드 입력"
    }

    val popupAnnouncement: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "弹窗公告"
        AppLanguage.ENGLISH -> "Popup Announcement"
        AppLanguage.ARABIC -> "إعلان منبثق"
        AppLanguage.PORTUGUESE -> "Anúncio popup"
        AppLanguage.SPANISH -> "Anuncio emergente"
        AppLanguage.FRENCH -> "Annonce popup"
        AppLanguage.GERMAN -> "Popup-Ankündigung"
        AppLanguage.RUSSIAN -> "Всплывающее объявление"
        AppLanguage.JAPANESE -> "ポップアップお知らせ"
        AppLanguage.KOREAN -> "팝업 공지"
    }

    val announcementTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "公告标题"
        AppLanguage.ENGLISH -> "Announcement Title"
        AppLanguage.ARABIC -> "عنوان الإعلان"
        AppLanguage.PORTUGUESE -> "Título do anúncio"
        AppLanguage.SPANISH -> "Título del anuncio"
        AppLanguage.FRENCH -> "Titre de l'annonce"
        AppLanguage.GERMAN -> "Ankündigungstitel"
        AppLanguage.RUSSIAN -> "Заголовок объявления"
        AppLanguage.JAPANESE -> "お知らせタイトル"
        AppLanguage.KOREAN -> "공지 제목"
    }

    val announcementContent: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "公告内容"
        AppLanguage.ENGLISH -> "Announcement Content"
        AppLanguage.ARABIC -> "محتوى الإعلان"
        AppLanguage.PORTUGUESE -> "Conteúdo do anúncio"
        AppLanguage.SPANISH -> "Contenido del anuncio"
        AppLanguage.FRENCH -> "Contenu de l'annonce"
        AppLanguage.GERMAN -> "Ankündigungsinhalt"
        AppLanguage.RUSSIAN -> "Содержание объявления"
        AppLanguage.JAPANESE -> "お知らせ内容"
        AppLanguage.KOREAN -> "공지 내용"
    }

    val announcementContentHtml: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "内容使用 HTML"
        AppLanguage.ENGLISH -> "Content as HTML"
        AppLanguage.ARABIC -> "المحتوى بصيغة HTML"
        AppLanguage.PORTUGUESE -> "Conteúdo como HTML"
        AppLanguage.SPANISH -> "Contenido como HTML"
        AppLanguage.FRENCH -> "Contenu au format HTML"
        AppLanguage.GERMAN -> "Inhalt als HTML"
        AppLanguage.RUSSIAN -> "Содержимое в формате HTML"
        AppLanguage.JAPANESE -> "内容をHTMLで"
        AppLanguage.KOREAN -> "내용을 HTML로"
    }

    val announcementContentHtmlDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "将公告内容作为 HTML 渲染，获得更高的排版自由度。弹窗外框与关闭按钮不变。"
        AppLanguage.ENGLISH -> "Render the announcement content as HTML for richer layout. The popup frame and close button stay the same."
        AppLanguage.ARABIC -> "عرض محتوى الإعلان بصيغة HTML لمزيد من حرية التنسيق. يبقى إطار النافذة وزر الإغلاق كما هو."
        AppLanguage.PORTUGUESE -> "Renderizar o conteúdo do anúncio como HTML para layout mais rico. O quadro popup e o botão fechar permanecem os mesmos."
        AppLanguage.SPANISH -> "Renderizar el contenido del anuncio como HTML para un layout más rico. El marco emergente y el botón de cierre permanecen igual."
        AppLanguage.FRENCH -> "Afficher le contenu de l'annonce en HTML pour un layout plus riche. Le cadre popup et le bouton fermer restent identiques."
        AppLanguage.GERMAN -> "Ankündigungsinhalt als HTML rendern für reichhaltigeres Layout. Popup-Rahmen und Schließen-Button bleiben gleich."
        AppLanguage.RUSSIAN -> "Отображать содержимое объявления как HTML для более богатого layout. Рамка popup и кнопка закрытия остаются прежними."
        AppLanguage.JAPANESE -> "お知らせ内容をHTMLでレンダリングし、より豊かなレイアウトを可能に。ポップアップ枠と閉じるボタンは同じまま。"
        AppLanguage.KOREAN -> "공지 내용을 HTML로 렌더링하여 더 풍부한 레이아웃 제공. 팝업 프레임과 닫기 버튼은 동일하게 유지."
    }

    val linkUrl: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "链接地址（可选）"
        AppLanguage.ENGLISH -> "Link URL (optional)"
        AppLanguage.ARABIC -> "رابط URL (اختياري)"
        AppLanguage.PORTUGUESE -> "URL do link (opcional)"
        AppLanguage.SPANISH -> "URL del enlace (opcional)"
        AppLanguage.FRENCH -> "URL du lien (optionnel)"
        AppLanguage.GERMAN -> "Link-URL (optional)"
        AppLanguage.RUSSIAN -> "URL ссылки (необязательно)"
        AppLanguage.JAPANESE -> "リンクURL（任意）"
        AppLanguage.KOREAN -> "링크 URL (선택)"
    }

    val linkButtonText: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "链接按钮文字"
        AppLanguage.ENGLISH -> "Link Button Text"
        AppLanguage.ARABIC -> "نص زر الرابط"
        AppLanguage.PORTUGUESE -> "Texto do botão de link"
        AppLanguage.SPANISH -> "Texto del botón de enlace"
        AppLanguage.FRENCH -> "Texte du bouton de lien"
        AppLanguage.GERMAN -> "Link-Button-Text"
        AppLanguage.RUSSIAN -> "Текст кнопки ссылки"
        AppLanguage.JAPANESE -> "リンクボタンのテキスト"
        AppLanguage.KOREAN -> "링크 버튼 텍스트"
    }

    val viewDetails: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "查看详情"
        AppLanguage.ENGLISH -> "View Details"
        AppLanguage.ARABIC -> "عرض التفاصيل"
        AppLanguage.PORTUGUESE -> "Ver detalhes"
        AppLanguage.SPANISH -> "Ver detalles"
        AppLanguage.FRENCH -> "Voir détails"
        AppLanguage.GERMAN -> "Details anzeigen"
        AppLanguage.RUSSIAN -> "Подробнее"
        AppLanguage.JAPANESE -> "詳細を表示"
        AppLanguage.KOREAN -> "상세 보기"
    }

    val displayFrequency: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "显示频率"
        AppLanguage.ENGLISH -> "Display Frequency"
        AppLanguage.ARABIC -> "تكرار العرض"
        AppLanguage.PORTUGUESE -> "Frequência de exibição"
        AppLanguage.SPANISH -> "Frecuencia de visualización"
        AppLanguage.FRENCH -> "Fréquence d'affichage"
        AppLanguage.GERMAN -> "Anzeigehäufigkeit"
        AppLanguage.RUSSIAN -> "Частота показа"
        AppLanguage.JAPANESE -> "表示頻度"
        AppLanguage.KOREAN -> "표시 빈도"
    }

    val showOnce: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "仅显示一次"
        AppLanguage.ENGLISH -> "Show Once Only"
        AppLanguage.ARABIC -> "عرض مرة واحدة فقط"
        AppLanguage.PORTUGUESE -> "Mostrar apenas uma vez"
        AppLanguage.SPANISH -> "Mostrar solo una vez"
        AppLanguage.FRENCH -> "Afficher une seule fois"
        AppLanguage.GERMAN -> "Nur einmal anzeigen"
        AppLanguage.RUSSIAN -> "Показать один раз"
        AppLanguage.JAPANESE -> "一度だけ表示"
        AppLanguage.KOREAN -> "한 번만 표시"
    }

    val everyLaunch: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "每次启动"
        AppLanguage.ENGLISH -> "Every Launch"
        AppLanguage.ARABIC -> "كل تشغيل"
        AppLanguage.PORTUGUESE -> "A cada inicialização"
        AppLanguage.SPANISH -> "Cada inicio"
        AppLanguage.FRENCH -> "À chaque lancement"
        AppLanguage.GERMAN -> "Bei jedem Start"
        AppLanguage.RUSSIAN -> "При каждом запуске"
        AppLanguage.JAPANESE -> "起動のたび"
        AppLanguage.KOREAN -> "실행할 때마다"
    }

    val announcementTriggerSettings: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "触发机制"
        AppLanguage.ENGLISH -> "Trigger Settings"
        AppLanguage.ARABIC -> "إعدادات التشغيل"
        AppLanguage.PORTUGUESE -> "Configurações de gatilho"
        AppLanguage.SPANISH -> "Configuración de disparador"
        AppLanguage.FRENCH -> "Paramètres de déclenchement"
        AppLanguage.GERMAN -> "Auslöser-Einstellungen"
        AppLanguage.RUSSIAN -> "Настройки триггера"
        AppLanguage.JAPANESE -> "トリガー設定"
        AppLanguage.KOREAN -> "트리거 설정"
    }

    val announcementTriggerOnLaunch: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "启动时显示"
        AppLanguage.ENGLISH -> "Show on Launch"
        AppLanguage.ARABIC -> "عرض عند التشغيل"
        AppLanguage.PORTUGUESE -> "Mostrar na inicialização"
        AppLanguage.SPANISH -> "Mostrar al iniciar"
        AppLanguage.FRENCH -> "Afficher au lancement"
        AppLanguage.GERMAN -> "Beim Start anzeigen"
        AppLanguage.RUSSIAN -> "Показать при запуске"
        AppLanguage.JAPANESE -> "起動時に表示"
        AppLanguage.KOREAN -> "실행 시 표시"
    }

    val announcementTriggerOnLaunchHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "应用启动时显示公告"
        AppLanguage.ENGLISH -> "Show announcement when app launches"
        AppLanguage.ARABIC -> "عرض الإعلان عند تشغيل التطبيق"
        AppLanguage.PORTUGUESE -> "Mostrar anúncio quando o app inicia"
        AppLanguage.SPANISH -> "Mostrar anuncio cuando la app inicia"
        AppLanguage.FRENCH -> "Afficher l'annonce au lancement de l'app"
        AppLanguage.GERMAN -> "Ankündigung beim App-Start anzeigen"
        AppLanguage.RUSSIAN -> "Показывать объявление при запуске приложения"
        AppLanguage.JAPANESE -> "アプリ起動時にお知らせを表示"
        AppLanguage.KOREAN -> "앱 실행 시 공지 표시"
    }

    val announcementTriggerOnNoNetwork: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "无网络时显示"
        AppLanguage.ENGLISH -> "Show When Offline"
        AppLanguage.ARABIC -> "عرض عند انقطاع الاتصال"
        AppLanguage.PORTUGUESE -> "Mostrar quando offline"
        AppLanguage.SPANISH -> "Mostrar sin conexión"
        AppLanguage.FRENCH -> "Afficher hors connexion"
        AppLanguage.GERMAN -> "Offline anzeigen"
        AppLanguage.RUSSIAN -> "Показать без сети"
        AppLanguage.JAPANESE -> "オフライン時に表示"
        AppLanguage.KOREAN -> "오프라인 시 표시"
    }

    val announcementTriggerOnNoNetworkHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "检测到网络连接断开时显示公告"
        AppLanguage.ENGLISH -> "Show announcement when network connection is lost"
        AppLanguage.ARABIC -> "عرض الإعلان عند فقدان الاتصال بالشبكة"
        AppLanguage.PORTUGUESE -> "Mostrar anúncio quando a conexão de rede é perdida"
        AppLanguage.SPANISH -> "Mostrar anuncio cuando se pierde la conexión de red"
        AppLanguage.FRENCH -> "Afficher l'annonce quand la connexion réseau est perdue"
        AppLanguage.GERMAN -> "Ankündigung anzeigen, wenn Netzwerkverbindung verloren geht"
        AppLanguage.RUSSIAN -> "Показывать объявление при потере сетевого соединения"
        AppLanguage.JAPANESE -> "ネットワーク接続が失われた時にお知らせを表示"
        AppLanguage.KOREAN -> "네트워크 연결이 끊기면 공지 표시"
    }

    val announcementTriggerInterval: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "定时弹窗"
        AppLanguage.ENGLISH -> "Timed Popup"
        AppLanguage.ARABIC -> "نافذة منبثقة مؤقتة"
        AppLanguage.PORTUGUESE -> "Popup temporizado"
        AppLanguage.SPANISH -> "Popup temporizado"
        AppLanguage.FRENCH -> "Popup minuté"
        AppLanguage.GERMAN -> "Zeitgesteuertes Popup"
        AppLanguage.RUSSIAN -> "Всплывающее по таймеру"
        AppLanguage.JAPANESE -> "タイマーポップアップ"
        AppLanguage.KOREAN -> "타이머 팝업"
    }

    val announcementTriggerIntervalHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "每隔指定时间自动显示公告"
        AppLanguage.ENGLISH -> "Auto show announcement at specified intervals"
        AppLanguage.ARABIC -> "عرض الإعلان تلقائيًا على فترات محددة"
        AppLanguage.PORTUGUESE -> "Mostrar anúncio automaticamente em intervalos especificados"
        AppLanguage.SPANISH -> "Mostrar anuncio automáticamente en intervalos especificados"
        AppLanguage.FRENCH -> "Afficher automatiquement l'annonce à intervalles spécifiés"
        AppLanguage.GERMAN -> "Ankündigung automatisch in festgelegten Intervallen anzeigen"
        AppLanguage.RUSSIAN -> "Автопоказ объявления с заданными интервалами"
        AppLanguage.JAPANESE -> "指定間隔で自動的にお知らせを表示"
        AppLanguage.KOREAN -> "지정된 간격으로 자동 공지 표시"
    }

    val announcementIntervalDisabled: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "禁用"
        AppLanguage.ENGLISH -> "Disabled"
        AppLanguage.ARABIC -> "معطل"
        AppLanguage.PORTUGUESE -> "Desativado"
        AppLanguage.SPANISH -> "Desactivado"
        AppLanguage.FRENCH -> "Désactivé"
        AppLanguage.GERMAN -> "Deaktiviert"
        AppLanguage.RUSSIAN -> "Отключено"
        AppLanguage.JAPANESE -> "無効"
        AppLanguage.KOREAN -> "비활성화됨"
    }

    val announcementTriggerIntervalIncludeLaunch: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "启动时也立即弹窗一次"
        AppLanguage.ENGLISH -> "Also show immediately on launch"
        AppLanguage.ARABIC -> "عرض فورًا عند التشغيل أيضًا"
        AppLanguage.PORTUGUESE -> "Também mostrar imediatamente na inicialização"
        AppLanguage.SPANISH -> "También mostrar inmediatamente al iniciar"
        AppLanguage.FRENCH -> "Afficher aussi immédiatement au lancement"
        AppLanguage.GERMAN -> "Beim Start auch sofort anzeigen"
        AppLanguage.RUSSIAN -> "Также показать сразу при запуске"
        AppLanguage.JAPANESE -> "起動時にも即座に表示"
        AppLanguage.KOREAN -> "실행 시에도 즉시 표시"
    }

    val announcementAdvancedOptions: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "高级选项"
        AppLanguage.ENGLISH -> "Advanced Options"
        AppLanguage.ARABIC -> "خيارات متقدمة"
        AppLanguage.PORTUGUESE -> "Opções avançadas"
        AppLanguage.SPANISH -> "Opciones avanzadas"
        AppLanguage.FRENCH -> "Options avancées"
        AppLanguage.GERMAN -> "Erweiterte Optionen"
        AppLanguage.RUSSIAN -> "Расширенные настройки"
        AppLanguage.JAPANESE -> "詳細オプション"
        AppLanguage.KOREAN -> "고급 옵션"
    }

    val announcementRequireConfirmLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "需确认"
        AppLanguage.ENGLISH -> "Require Confirm"
        AppLanguage.ARABIC -> "تأكيد مطلوب"
        AppLanguage.PORTUGUESE -> "Exigir confirmação"
        AppLanguage.SPANISH -> "Requerir confirmación"
        AppLanguage.FRENCH -> "Confirmation requise"
        AppLanguage.GERMAN -> "Bestätigung erforderlich"
        AppLanguage.RUSSIAN -> "Требуется подтверждение"
        AppLanguage.JAPANESE -> "確認が必要"
        AppLanguage.KOREAN -> "확인 필요"
    }

    val announcementRequireConfirmHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "用户必须点击确认按钮才能关闭公告"
        AppLanguage.ENGLISH -> "Users must click confirm to dismiss the announcement"
        AppLanguage.ARABIC -> "يجب على المستخدمين النقر على تأكيد لإغلاق الإعلان"
        AppLanguage.PORTUGUESE -> "Usuários devem clicar em confirmar para dispensar o anúncio"
        AppLanguage.SPANISH -> "Los usuarios deben hacer clic en confirmar para cerrar el anuncio"
        AppLanguage.FRENCH -> "Les utilisateurs doivent cliquer sur confirmer pour fermer l'annonce"
        AppLanguage.GERMAN -> "Nutzer müssen auf Bestätigen klicken, um die Ankündigung zu schließen"
        AppLanguage.RUSSIAN -> "Пользователи должны нажать «подтвердить», чтобы закрыть объявление"
        AppLanguage.JAPANESE -> "ユーザーは確認ボタンを押さないとお知らせを閉じられません"
        AppLanguage.KOREAN -> "사용자가 확인을 클릭해야 공지를 닫을 수 있습니다"
    }

    val announcementAllowNeverShowLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "可关闭"
        AppLanguage.ENGLISH -> "Allow Dismiss"
        AppLanguage.ARABIC -> "السماح بالإغلاق"
        AppLanguage.PORTUGUESE -> "Permitir dispensar"
        AppLanguage.SPANISH -> "Permitir cerrar"
        AppLanguage.FRENCH -> "Autoriser fermeture"
        AppLanguage.GERMAN -> "Schließen erlauben"
        AppLanguage.RUSSIAN -> "Разрешить закрытие"
        AppLanguage.JAPANESE -> "閉じるを許可"
        AppLanguage.KOREAN -> "닫기 허용"
    }

    val announcementAllowNeverShowHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "允许用户选择不再显示此公告"
        AppLanguage.ENGLISH -> "Allow users to permanently dismiss this announcement"
        AppLanguage.ARABIC -> "السماح للمستخدمين بإغلاق هذا الإعلان نهائيًا"
        AppLanguage.PORTUGUESE -> "Permitir que usuários dispensem permanentemente este anúncio"
        AppLanguage.SPANISH -> "Permitir a los usuarios cerrar permanentemente este anuncio"
        AppLanguage.FRENCH -> "Autoriser les utilisateurs à fermer définitivement cette annonce"
        AppLanguage.GERMAN -> "Nutzern erlauben, diese Ankündigung dauerhaft zu schließen"
        AppLanguage.RUSSIAN -> "Разрешить пользователям навсегда закрыть это объявление"
        AppLanguage.JAPANESE -> "ユーザーがこのお知らせを完全に閉じることを許可"
        AppLanguage.KOREAN -> "사용자가 이 공지를 영구적으로 닫을 수 있도록 허용"
    }

    val adBlocking: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "广告拦截"
        AppLanguage.ENGLISH -> "Ad Blocking"
        AppLanguage.ARABIC -> "حظر الإعلانات"
        AppLanguage.PORTUGUESE -> "Bloqueio de anúncios"
        AppLanguage.SPANISH -> "Bloqueo de anuncios"
        AppLanguage.FRENCH -> "Blocage des pubs"
        AppLanguage.GERMAN -> "Werbung blockieren"
        AppLanguage.RUSSIAN -> "Блокировка рекламы"
        AppLanguage.JAPANESE -> "広告ブロック"
        AppLanguage.KOREAN -> "광고 차단"
    }

    val fullscreenMode: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "全屏模式"
        AppLanguage.ENGLISH -> "Fullscreen Mode"
        AppLanguage.ARABIC -> "وضع ملء الشاشة"
        AppLanguage.PORTUGUESE -> "Modo Tela Cheia"
        AppLanguage.SPANISH -> "Modo Pantalla Completa"
        AppLanguage.FRENCH -> "Mode Plein Écran"
        AppLanguage.GERMAN -> "Vollbildmodus"
        AppLanguage.RUSSIAN -> "Полноэкранный режим"
        AppLanguage.JAPANESE -> "全画面モード"
        AppLanguage.KOREAN -> "전체 화면 모드"
    }

    val splashScreen: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "启动画面"
        AppLanguage.ENGLISH -> "Splash Screen"
        AppLanguage.ARABIC -> "شاشة البداية"
        AppLanguage.PORTUGUESE -> "Tela de Abertura"
        AppLanguage.SPANISH -> "Pantalla de Inicio"
        AppLanguage.FRENCH -> "Écran de Démarrage"
        AppLanguage.GERMAN -> "Startbildschirm"
        AppLanguage.RUSSIAN -> "Заставка"
        AppLanguage.JAPANESE -> "スプラッシュ画面"
        AppLanguage.KOREAN -> "스플래시 화면"
    }

    val autoTranslate: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自动翻译"
        AppLanguage.ENGLISH -> "Auto Translate"
        AppLanguage.ARABIC -> "الترجمة التلقائية"
        AppLanguage.PORTUGUESE -> "Tradução Automática"
        AppLanguage.SPANISH -> "Traducción Automática"
        AppLanguage.FRENCH -> "Traduction Automatique"
        AppLanguage.GERMAN -> "Automatische Übersetzung"
        AppLanguage.RUSSIAN -> "Автоматический перевод"
        AppLanguage.JAPANESE -> "自動翻訳"
        AppLanguage.KOREAN -> "자동 번역"
    }

    val htmlApp: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "HTML 应用"
        AppLanguage.ENGLISH -> "HTML App"
        AppLanguage.ARABIC -> "تطبيق HTML"
        AppLanguage.PORTUGUESE -> "App HTML"
        AppLanguage.SPANISH -> "App HTML"
        AppLanguage.FRENCH -> "App HTML"
        AppLanguage.GERMAN -> "HTML-App"
        AppLanguage.RUSSIAN -> "HTML-приложение"
        AppLanguage.JAPANESE -> "HTMLアプリ"
        AppLanguage.KOREAN -> "HTML 앱"
    }

    val frontendApp: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "前端应用"
        AppLanguage.ENGLISH -> "Frontend App"
        AppLanguage.ARABIC -> "تطبيق الواجهة الأمامية"
        AppLanguage.PORTUGUESE -> "App Frontend"
        AppLanguage.SPANISH -> "App Frontend"
        AppLanguage.FRENCH -> "App Frontend"
        AppLanguage.GERMAN -> "Frontend-App"
        AppLanguage.RUSSIAN -> "Фронтенд-приложение"
        AppLanguage.JAPANESE -> "フロントエンドアプリ"
        AppLanguage.KOREAN -> "프론트엔드 앱"
    }

    val entryFile: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "入口文件"
        AppLanguage.ENGLISH -> "Entry File"
        AppLanguage.ARABIC -> "ملف الدخول"
        AppLanguage.PORTUGUESE -> "Arquivo de Entrada"
        AppLanguage.SPANISH -> "Archivo de Entrada"
        AppLanguage.FRENCH -> "Fichier d'Entrée"
        AppLanguage.GERMAN -> "Einstiegsdatei"
        AppLanguage.RUSSIAN -> "Точка входа"
        AppLanguage.JAPANESE -> "エントリファイル"
        AppLanguage.KOREAN -> "진입 파일"
    }

    val totalFilesCount: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "共 %d 个文件"
        AppLanguage.ENGLISH -> "%d files total"
        AppLanguage.ARABIC -> "إجمالي %d ملفات"
        AppLanguage.PORTUGUESE -> "%d arquivos no total"
        AppLanguage.SPANISH -> "%d archivos en total"
        AppLanguage.FRENCH -> "%d fichiers au total"
        AppLanguage.GERMAN -> "%d Dateien gesamt"
        AppLanguage.RUSSIAN -> "Всего %d файлов"
        AppLanguage.JAPANESE -> "合計 %d ファイル"
        AppLanguage.KOREAN -> "총 %d개 파일"
    }

    val imageApp: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "图片应用"
        AppLanguage.ENGLISH -> "Image App"
        AppLanguage.ARABIC -> "تطبيق صور"
        AppLanguage.PORTUGUESE -> "App de Imagens"
        AppLanguage.SPANISH -> "App de Imágenes"
        AppLanguage.FRENCH -> "App d'Images"
        AppLanguage.GERMAN -> "Bild-App"
        AppLanguage.RUSSIAN -> "Фото-приложение"
        AppLanguage.JAPANESE -> "画像アプリ"
        AppLanguage.KOREAN -> "이미지 앱"
    }

    val videoApp: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "视频应用"
        AppLanguage.ENGLISH -> "Video App"
        AppLanguage.ARABIC -> "تطبيق فيديو"
        AppLanguage.PORTUGUESE -> "App de Vídeos"
        AppLanguage.SPANISH -> "App de Vídeos"
        AppLanguage.FRENCH -> "App de Vidéos"
        AppLanguage.GERMAN -> "Video-App"
        AppLanguage.RUSSIAN -> "Видео-приложение"
        AppLanguage.JAPANESE -> "動画アプリ"
        AppLanguage.KOREAN -> "동영상 앱"
    }

    val unknownFile: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "未知文件"
        AppLanguage.ENGLISH -> "Unknown File"
        AppLanguage.ARABIC -> "ملف غير معروف"
        AppLanguage.PORTUGUESE -> "Arquivo Desconhecido"
        AppLanguage.SPANISH -> "Archivo Desconocido"
        AppLanguage.FRENCH -> "Fichier Inconnu"
        AppLanguage.GERMAN -> "Unbekannte Datei"
        AppLanguage.RUSSIAN -> "Неизвестный файл"
        AppLanguage.JAPANESE -> "不明なファイル"
        AppLanguage.KOREAN -> "알 수 없는 파일"
    }

    val runtimePhpSqlite: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "PHP + SQLite"
        AppLanguage.ENGLISH -> "PHP + SQLite"
        AppLanguage.ARABIC -> "PHP + SQLite"
        AppLanguage.PORTUGUESE -> "PHP + SQLite"
        AppLanguage.SPANISH -> "PHP + SQLite"
        AppLanguage.FRENCH -> "PHP + SQLite"
        AppLanguage.GERMAN -> "PHP + SQLite"
        AppLanguage.RUSSIAN -> "PHP + SQLite"
        AppLanguage.JAPANESE -> "PHP + SQLite"
        AppLanguage.KOREAN -> "PHP + SQLite"
    }

    val runtimeNodeJs: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Node.js Runtime"
        AppLanguage.ENGLISH -> "Node.js Runtime"
        AppLanguage.ARABIC -> "بيئة تشغيل Node.js"
        AppLanguage.PORTUGUESE -> "Runtime Node.js"
        AppLanguage.SPANISH -> "Runtime Node.js"
        AppLanguage.FRENCH -> "Runtime Node.js"
        AppLanguage.GERMAN -> "Node.js-Laufzeit"
        AppLanguage.RUSSIAN -> "Среда Node.js"
        AppLanguage.JAPANESE -> "Node.jsランタイム"
        AppLanguage.KOREAN -> "Node.js 런타임"
    }

    val runtimePhp: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "PHP Runtime"
        AppLanguage.ENGLISH -> "PHP Runtime"
        AppLanguage.ARABIC -> "بيئة تشغيل PHP"
        AppLanguage.PORTUGUESE -> "Runtime PHP"
        AppLanguage.SPANISH -> "Runtime PHP"
        AppLanguage.FRENCH -> "Runtime PHP"
        AppLanguage.GERMAN -> "PHP-Laufzeit"
        AppLanguage.RUSSIAN -> "Среда PHP"
        AppLanguage.JAPANESE -> "PHPランタイム"
        AppLanguage.KOREAN -> "PHP 런타임"
    }

    val runtimePython: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Python Runtime"
        AppLanguage.ENGLISH -> "Python Runtime"
        AppLanguage.ARABIC -> "بيئة تشغيل Python"
        AppLanguage.PORTUGUESE -> "Runtime Python"
        AppLanguage.SPANISH -> "Runtime Python"
        AppLanguage.FRENCH -> "Runtime Python"
        AppLanguage.GERMAN -> "Python-Laufzeit"
        AppLanguage.RUSSIAN -> "Среда Python"
        AppLanguage.JAPANESE -> "Pythonランタイム"
        AppLanguage.KOREAN -> "Python 런타임"
    }

    val runtimeGoBinary: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Go 二进制 / 构建产物"
        AppLanguage.ENGLISH -> "Go Binary / Build Output"
        AppLanguage.ARABIC -> "ثنائي Go / ناتج البناء"
        AppLanguage.PORTUGUESE -> "Binário Go / Saída de Build"
        AppLanguage.SPANISH -> "Binario Go / Salida de Build"
        AppLanguage.FRENCH -> "Binaire Go / Sortie de Build"
        AppLanguage.GERMAN -> "Go-Binärdatei / Build-Ausgabe"
        AppLanguage.RUSSIAN -> "Бинарник Go / Результат сборки"
        AppLanguage.JAPANESE -> "Goバイナリ / ビルド成果物"
        AppLanguage.KOREAN -> "Go 바이너리 / 빌드 산출물"
    }


    val fabIconFromGallery: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "相册"
        AppLanguage.ENGLISH -> "Gallery"
        AppLanguage.ARABIC -> "المعرض"
        AppLanguage.PORTUGUESE -> "Galeria"
        AppLanguage.SPANISH -> "Galería"
        AppLanguage.FRENCH -> "Galerie"
        AppLanguage.GERMAN -> "Galerie"
        AppLanguage.RUSSIAN -> "Галерея"
        AppLanguage.JAPANESE -> "ギャラリー"
        AppLanguage.KOREAN -> "갤러리"
    }

    val fabIconSelected: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已选择自定义图标"
        AppLanguage.ENGLISH -> "Custom icon selected"
        AppLanguage.ARABIC -> "تم تحديد أيقونة مخصصة"
        AppLanguage.PORTUGUESE -> "Ícone personalizado selecionado"
        AppLanguage.SPANISH -> "Icono personalizado seleccionado"
        AppLanguage.FRENCH -> "Icône personnalisée sélectionnée"
        AppLanguage.GERMAN -> "Benutzerdefiniertes Symbol ausgewählt"
        AppLanguage.RUSSIAN -> "Выбрана пользовательская иконка"
        AppLanguage.JAPANESE -> "カスタムアイコンを選択しました"
        AppLanguage.KOREAN -> "커스텀 아이콘 선택됨"
    }

    val fabIconPreviewTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "预览悬浮按钮图标"
        AppLanguage.ENGLISH -> "Preview FAB Icon"
        AppLanguage.ARABIC -> "معاينة أيقونة الزر العائم"
        AppLanguage.PORTUGUESE -> "Pré-visualizar Ícone do FAB"
        AppLanguage.SPANISH -> "Vista Previa del Icono del FAB"
        AppLanguage.FRENCH -> "Prévisualiser l'Icône du FAB"
        AppLanguage.GERMAN -> "FAB-Symbol Vorschau"
        AppLanguage.RUSSIAN -> "Предпросмотр иконки FAB"
        AppLanguage.JAPANESE -> "FABアイコンをプレビュー"
        AppLanguage.KOREAN -> "FAB 아이콘 미리보기"
    }

    val fabIconPreviewDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "这是你选择的图片在悬浮按钮中的效果，确认使用吗？"
        AppLanguage.ENGLISH -> "This is how your image will appear in the floating action button. Use this icon?"
        AppLanguage.ARABIC -> "هذا هو شكل صورتك في الزر العائم. هل تريد استخدام هذه الأيقونة؟"
        AppLanguage.PORTUGUESE -> "É assim que sua imagem aparecerá no botão flutuante. Usar este ícone?"
        AppLanguage.SPANISH -> "Así es como aparecerá tu imagen en el botón flotante. ¿Usar este icono?"
        AppLanguage.FRENCH -> "Voici comment votre image apparaîtra dans le bouton d'action flottant. Utiliser cette icône ?"
        AppLanguage.GERMAN -> "So wird Ihr Bild im schwebenden Aktionsbutton erscheinen. Dieses Symbol verwenden?"
        AppLanguage.RUSSIAN -> "Так ваше изображение будет выглядеть в плавающей кнопке. Использовать эту иконку?"
        AppLanguage.JAPANESE -> "選択した画像がフローティングボタンに表示されるイメージです。このアイコンを使いますか？"
        AppLanguage.KOREAN -> "선택한 이미지가 플로팅 버튼에 표시되는 모습입니다. 이 아이콘을 사용하시겠습니까?"
    }

    val fabIconCustom: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自定义"
        AppLanguage.ENGLISH -> "Custom"
        AppLanguage.ARABIC -> "مخصص"
        AppLanguage.PORTUGUESE -> "Personalizado"
        AppLanguage.SPANISH -> "Personalizado"
        AppLanguage.FRENCH -> "Personnalisé"
        AppLanguage.GERMAN -> "Benutzerdefiniert"
        AppLanguage.RUSSIAN -> "Пользовательский"
        AppLanguage.JAPANESE -> "カスタム"
        AppLanguage.KOREAN -> "커스텀"
    }

    val fabIconChangeImage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "更换"
        AppLanguage.ENGLISH -> "Change"
        AppLanguage.ARABIC -> "تغيير"
        AppLanguage.PORTUGUESE -> "Alterar"
        AppLanguage.SPANISH -> "Cambiar"
        AppLanguage.FRENCH -> "Changer"
        AppLanguage.GERMAN -> "Ändern"
        AppLanguage.RUSSIAN -> "Изменить"
        AppLanguage.JAPANESE -> "変更"
        AppLanguage.KOREAN -> "변경"
    }

    val reselect: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "重新选择"
        AppLanguage.ENGLISH -> "Reselect"
        AppLanguage.ARABIC -> "إعادة الاختيار"
        AppLanguage.PORTUGUESE -> "Selecionar novamente"
        AppLanguage.SPANISH -> "Seleccionar de nuevo"
        AppLanguage.FRENCH -> "Resélectionner"
        AppLanguage.GERMAN -> "Neu auswählen"
        AppLanguage.RUSSIAN -> "Выбрать заново"
        AppLanguage.JAPANESE -> "再選択"
        AppLanguage.KOREAN -> "다시 선택"
    }

    val extensionModule: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "扩展模块"
        AppLanguage.ENGLISH -> "Extension Module"
        AppLanguage.ARABIC -> "وحدة إضافية"
        AppLanguage.PORTUGUESE -> "Módulo de Extensão"
        AppLanguage.SPANISH -> "Módulo de Extensión"
        AppLanguage.FRENCH -> "Module d'Extension"
        AppLanguage.GERMAN -> "Erweiterungsmodul"
        AppLanguage.RUSSIAN -> "Модуль расширения"
        AppLanguage.JAPANESE -> "拡張モジュール"
        AppLanguage.KOREAN -> "확장 모듈"
    }

    val searchModules: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "搜索模块..."
        AppLanguage.ENGLISH -> "Search modules..."
        AppLanguage.ARABIC -> "البحث عن الوحدات..."
        AppLanguage.PORTUGUESE -> "Buscar módulos..."
        AppLanguage.SPANISH -> "Buscar módulos..."
        AppLanguage.FRENCH -> "Rechercher des modules..."
        AppLanguage.GERMAN -> "Module suchen..."
        AppLanguage.RUSSIAN -> "Поиск модулей..."
        AppLanguage.JAPANESE -> "モジュールを検索..."
        AppLanguage.KOREAN -> "모듈 검색..."
    }

    val all: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "全部"
        AppLanguage.ENGLISH -> "All"
        AppLanguage.ARABIC -> "الكل"
        AppLanguage.PORTUGUESE -> "Todos"
        AppLanguage.SPANISH -> "Todos"
        AppLanguage.FRENCH -> "Tous"
        AppLanguage.GERMAN -> "Alle"
        AppLanguage.RUSSIAN -> "Все"
        AppLanguage.JAPANESE -> "すべて"
        AppLanguage.KOREAN -> "전체"
    }

    val builtIn: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "内置"
        AppLanguage.ENGLISH -> "Built-in"
        AppLanguage.ARABIC -> "مدمج"
        AppLanguage.PORTUGUESE -> "Integrado"
        AppLanguage.SPANISH -> "Integrado"
        AppLanguage.FRENCH -> "Intégré"
        AppLanguage.GERMAN -> "Eingebaut"
        AppLanguage.RUSSIAN -> "Встроенный"
        AppLanguage.JAPANESE -> "組み込み"
        AppLanguage.KOREAN -> "내장"
    }

    val noModulesFound: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "没有找到匹配的模块"
        AppLanguage.ENGLISH -> "No matching modules found"
        AppLanguage.ARABIC -> "لم يتم العثور على وحدات مطابقة"
        AppLanguage.PORTUGUESE -> "Nenhum módulo correspondente encontrado"
        AppLanguage.SPANISH -> "No se encontraron módulos coincidentes"
        AppLanguage.FRENCH -> "Aucun module correspondant trouvé"
        AppLanguage.GERMAN -> "Keine passenden Module gefunden"
        AppLanguage.RUSSIAN -> "Подходящие модули не найдены"
        AppLanguage.JAPANESE -> "一致するモジュールが見つかりません"
        AppLanguage.KOREAN -> "일치하는 모듈이 없습니다"
    }

    val noModulesYet: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "暂无模块"
        AppLanguage.ENGLISH -> "No modules yet"
        AppLanguage.ARABIC -> "لا توجد وحدات بعد"
        AppLanguage.PORTUGUESE -> "Ainda não há módulos"
        AppLanguage.SPANISH -> "Aún no hay módulos"
        AppLanguage.FRENCH -> "Pas encore de modules"
        AppLanguage.GERMAN -> "Noch keine Module"
        AppLanguage.RUSSIAN -> "Модулей пока нет"
        AppLanguage.JAPANESE -> "まだモジュールがありません"
        AppLanguage.KOREAN -> "아직 모듈이 없습니다"
    }

    val createFirstModule: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "创建第一个模块"
        AppLanguage.ENGLISH -> "Create first module"
        AppLanguage.ARABIC -> "إنشاء أول وحدة"
        AppLanguage.PORTUGUESE -> "Criar primeiro módulo"
        AppLanguage.SPANISH -> "Crear primer módulo"
        AppLanguage.FRENCH -> "Créer le premier module"
        AppLanguage.GERMAN -> "Erstes Modul erstellen"
        AppLanguage.RUSSIAN -> "Создать первый модуль"
        AppLanguage.JAPANESE -> "最初のモジュールを作成"
        AppLanguage.KOREAN -> "첫 모듈 만들기"
    }

    val totalModulesLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "总模块"
        AppLanguage.ENGLISH -> "Total"
        AppLanguage.ARABIC -> "الإجمالي"
        AppLanguage.PORTUGUESE -> "Total"
        AppLanguage.SPANISH -> "Total"
        AppLanguage.FRENCH -> "Total"
        AppLanguage.GERMAN -> "Gesamt"
        AppLanguage.RUSSIAN -> "Всего"
        AppLanguage.JAPANESE -> "合計"
        AppLanguage.KOREAN -> "전체"
    }

    val builtInLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "内置"
        AppLanguage.ENGLISH -> "Built-in"
        AppLanguage.ARABIC -> "مدمج"
        AppLanguage.PORTUGUESE -> "Integrado"
        AppLanguage.SPANISH -> "Integrado"
        AppLanguage.FRENCH -> "Intégré"
        AppLanguage.GERMAN -> "Eingebaut"
        AppLanguage.RUSSIAN -> "Встроенный"
        AppLanguage.JAPANESE -> "組み込み"
        AppLanguage.KOREAN -> "내장"
    }

    val customLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自定义"
        AppLanguage.ENGLISH -> "Custom"
        AppLanguage.ARABIC -> "مخصص"
        AppLanguage.PORTUGUESE -> "Personalizado"
        AppLanguage.SPANISH -> "Personalizado"
        AppLanguage.FRENCH -> "Personnalisé"
        AppLanguage.GERMAN -> "Benutzerdefiniert"
        AppLanguage.RUSSIAN -> "Пользовательский"
        AppLanguage.JAPANESE -> "カスタム"
        AppLanguage.KOREAN -> "커스텀"
    }

    val tryDifferentSearch: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "请尝试其他搜索关键词"
        AppLanguage.ENGLISH -> "Try a different search term"
        AppLanguage.ARABIC -> "جرب مصطلح بحث مختلف"
        AppLanguage.PORTUGUESE -> "Tente um termo de busca diferente"
        AppLanguage.SPANISH -> "Prueba un término de búsqueda diferente"
        AppLanguage.FRENCH -> "Essayez un autre terme de recherche"
        AppLanguage.GERMAN -> "Versuchen Sie einen anderen Suchbegriff"
        AppLanguage.RUSSIAN -> "Попробуйте другой поисковый запрос"
        AppLanguage.JAPANESE -> "別の検索キーワードを試してください"
        AppLanguage.KOREAN -> "다른 검색어를 시도해 보세요"
    }

    val createModuleHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "创建你的第一个扩展模块\n为你的应用添加自定义功能"
        AppLanguage.ENGLISH -> "Create your first extension module\nAdd custom features to your apps"
        AppLanguage.ARABIC -> "أنشئ أول وحدة إضافية\nأضف ميزات مخصصة لتطبيقاتك"
        AppLanguage.PORTUGUESE -> "Crie seu primeiro módulo de extensão\nAdicione recursos personalizados aos seus apps"
        AppLanguage.SPANISH -> "Crea tu primer módulo de extensión\nAñade funciones personalizadas a tus apps"
        AppLanguage.FRENCH -> "Créez votre premier module d'extension\nAjoutez des fonctionnalités personnalisées à vos apps"
        AppLanguage.GERMAN -> "Erstellen Sie Ihr erstes Erweiterungsmodul\nFügen Sie Ihren Apps benutzerdefinierte Funktionen hinzu"
        AppLanguage.RUSSIAN -> "Создайте свой первый модуль расширения\nДобавьте кастомные функции в ваши приложения"
        AppLanguage.JAPANESE -> "最初の拡張モジュールを作成\nアプリにカスタム機能を追加"
        AppLanguage.KOREAN -> "첫 확장 모듈을 만들어 보세요\n앱에 커스텀 기능을 추가하세요"
    }

    val clearSearch: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "清除搜索"
        AppLanguage.ENGLISH -> "Clear search"
        AppLanguage.ARABIC -> "مسح البحث"
        AppLanguage.PORTUGUESE -> "Limpar busca"
        AppLanguage.SPANISH -> "Borrar búsqueda"
        AppLanguage.FRENCH -> "Effacer la recherche"
        AppLanguage.GERMAN -> "Suche löschen"
        AppLanguage.RUSSIAN -> "Очистить поиск"
        AppLanguage.JAPANESE -> "検索をクリア"
        AppLanguage.KOREAN -> "검색 지우기"
    }

    val importFromFile: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "从文件导入"
        AppLanguage.ENGLISH -> "Import from File"
        AppLanguage.ARABIC -> "استيراد من ملف"
        AppLanguage.PORTUGUESE -> "Importar de Arquivo"
        AppLanguage.SPANISH -> "Importar desde Archivo"
        AppLanguage.FRENCH -> "Importer depuis un Fichier"
        AppLanguage.GERMAN -> "Aus Datei importieren"
        AppLanguage.RUSSIAN -> "Импортировать из файла"
        AppLanguage.JAPANESE -> "ファイルからインポート"
        AppLanguage.KOREAN -> "파일에서 가져오기"
    }

    val selectWtamodFile: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择 .wtamod 或 .wtapkg 文件"
        AppLanguage.ENGLISH -> "Select .wtamod or .wtapkg file"
        AppLanguage.ARABIC -> "اختر ملف .wtamod أو .wtapkg"
        AppLanguage.PORTUGUESE -> "Selecionar arquivo .wtamod ou .wtapkg"
        AppLanguage.SPANISH -> "Seleccionar archivo .wtamod o .wtapkg"
        AppLanguage.FRENCH -> "Sélectionner un fichier .wtamod ou .wtapkg"
        AppLanguage.GERMAN -> ".wtamod- oder .wtapkg-Datei auswählen"
        AppLanguage.RUSSIAN -> "Выбрать файл .wtamod или .wtapkg"
        AppLanguage.JAPANESE -> ".wtamod または .wtapkg ファイルを選択"
        AppLanguage.KOREAN -> ".wtamod 또는 .wtapkg 파일 선택"
    }

    val importFromQrImage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "从二维码图片导入"
        AppLanguage.ENGLISH -> "Import from QR Code Image"
        AppLanguage.ARABIC -> "استيراد من صورة رمز QR"
        AppLanguage.PORTUGUESE -> "Importar de Imagem QR Code"
        AppLanguage.SPANISH -> "Importar de Imagen de Código QR"
        AppLanguage.FRENCH -> "Importer depuis une Image QR Code"
        AppLanguage.GERMAN -> "Aus QR-Code-Bild importieren"
        AppLanguage.RUSSIAN -> "Импорт из изображения QR-кода"
        AppLanguage.JAPANESE -> "QRコード画像からインポート"
        AppLanguage.KOREAN -> "QR 코드 이미지에서 가져오기"
    }

    val selectQrImageHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择包含模块二维码的图片"
        AppLanguage.ENGLISH -> "Select image containing module QR code"
        AppLanguage.ARABIC -> "اختر صورة تحتوي على رمز QR للوحدة"
        AppLanguage.PORTUGUESE -> "Selecione imagem contendo QR code do módulo"
        AppLanguage.SPANISH -> "Selecciona imagen que contenga el código QR del módulo"
        AppLanguage.FRENCH -> "Sélectionnez une image contenant le QR code du module"
        AppLanguage.GERMAN -> "Bild mit QR-Code des Moduls auswählen"
        AppLanguage.RUSSIAN -> "Выберите изображение с QR-кодом модуля"
        AppLanguage.JAPANESE -> "モジュールのQRコードを含む画像を選択"
        AppLanguage.KOREAN -> "모듈 QR 코드가 포함된 이미지 선택"
    }

    val qrCodeNotFound: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "未检测到二维码"
        AppLanguage.ENGLISH -> "QR code not detected"
        AppLanguage.ARABIC -> "لم يتم اكتشاف رمز QR"
        AppLanguage.PORTUGUESE -> "QR code não detectado"
        AppLanguage.SPANISH -> "Código QR no detectado"
        AppLanguage.FRENCH -> "QR code non détecté"
        AppLanguage.GERMAN -> "QR-Code nicht erkannt"
        AppLanguage.RUSSIAN -> "QR-код не обнаружен"
        AppLanguage.JAPANESE -> "QRコードが検出されませんでした"
        AppLanguage.KOREAN -> "QR 코드가 감지되지 않았습니다"
    }

    val imageLoadFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "图片加载失败"
        AppLanguage.ENGLISH -> "Failed to load image"
        AppLanguage.ARABIC -> "فشل في تحميل الصورة"
        AppLanguage.PORTUGUESE -> "Falha ao carregar imagem"
        AppLanguage.SPANISH -> "Error al cargar imagen"
        AppLanguage.FRENCH -> "Échec du chargement de l'image"
        AppLanguage.GERMAN -> "Bild laden fehlgeschlagen"
        AppLanguage.RUSSIAN -> "Не удалось загрузить изображение"
        AppLanguage.JAPANESE -> "画像の読み込みに失敗しました"
        AppLanguage.KOREAN -> "이미지 로드 실패"
    }

    val requiresSensitivePermissions: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "需要敏感权限"
        AppLanguage.ENGLISH -> "Requires sensitive permissions"
        AppLanguage.ARABIC -> "يتطلب أذونات حساسة"
        AppLanguage.PORTUGUESE -> "Requer permissões sensíveis"
        AppLanguage.SPANISH -> "Requiere permisos sensibles"
        AppLanguage.FRENCH -> "Requiert des permissions sensibles"
        AppLanguage.GERMAN -> "Erfordert sensible Berechtigungen"
        AppLanguage.RUSSIAN -> "Требуются чувствительные разрешения"
        AppLanguage.JAPANESE -> "機密権限が必要です"
        AppLanguage.KOREAN -> "민감한 권한이 필요합니다"
    }

    val aiDevelop: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "AI 开发"
        AppLanguage.ENGLISH -> "AI Develop"
        AppLanguage.ARABIC -> "تطوير AI"
        AppLanguage.PORTUGUESE -> "Desenvolvimento com IA"
        AppLanguage.SPANISH -> "Desarrollo con IA"
        AppLanguage.FRENCH -> "Développement IA"
        AppLanguage.GERMAN -> "KI-Entwicklung"
        AppLanguage.RUSSIAN -> "Разработка с ИИ"
        AppLanguage.JAPANESE -> "AI開発"
        AppLanguage.KOREAN -> "AI 개발"
    }

    val manualCreate: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "手动创建"
        AppLanguage.ENGLISH -> "Manual Create"
        AppLanguage.ARABIC -> "إنشاء يدوي"
        AppLanguage.PORTUGUESE -> "Criação Manual"
        AppLanguage.SPANISH -> "Creación Manual"
        AppLanguage.FRENCH -> "Création Manuelle"
        AppLanguage.GERMAN -> "Manuell erstellen"
        AppLanguage.RUSSIAN -> "Ручное создание"
        AppLanguage.JAPANESE -> "手動作成"
        AppLanguage.KOREAN -> "수동 만들기"
    }

    val createModule: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "创建模块"
        AppLanguage.ENGLISH -> "Create Module"
        AppLanguage.ARABIC -> "إنشاء وحدة"
        AppLanguage.PORTUGUESE -> "Criar Módulo"
        AppLanguage.SPANISH -> "Crear Módulo"
        AppLanguage.FRENCH -> "Créer un Module"
        AppLanguage.GERMAN -> "Modul erstellen"
        AppLanguage.RUSSIAN -> "Создать модуль"
        AppLanguage.JAPANESE -> "モジュールを作成"
        AppLanguage.KOREAN -> "모듈 만들기"
    }

    val followSystem: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "跟随系统"
        AppLanguage.ENGLISH -> "Follow System"
        AppLanguage.ARABIC -> "اتباع النظام"
        AppLanguage.PORTUGUESE -> "Seguir Sistema"
        AppLanguage.SPANISH -> "Seguir Sistema"
        AppLanguage.FRENCH -> "Suivre le Système"
        AppLanguage.GERMAN -> "System folgen"
        AppLanguage.RUSSIAN -> "Системная"
        AppLanguage.JAPANESE -> "システムに従う"
        AppLanguage.KOREAN -> "시스템 따르기"
    }

    val alwaysLight: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "始终浅色"
        AppLanguage.ENGLISH -> "Always Light"
        AppLanguage.ARABIC -> "فاتح دائمًا"
        AppLanguage.PORTUGUESE -> "Sempre Claro"
        AppLanguage.SPANISH -> "Siempre Claro"
        AppLanguage.FRENCH -> "Toujours Clair"
        AppLanguage.GERMAN -> "Immer Hell"
        AppLanguage.RUSSIAN -> "Всегда светлая"
        AppLanguage.JAPANESE -> "常にライト"
        AppLanguage.KOREAN -> "항상 라이트"
    }

    val alwaysDark: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "始终深色"
        AppLanguage.ENGLISH -> "Always Dark"
        AppLanguage.ARABIC -> "داكن دائمًا"
        AppLanguage.PORTUGUESE -> "Sempre Escuro"
        AppLanguage.SPANISH -> "Siempre Oscuro"
        AppLanguage.FRENCH -> "Toujours Sombre"
        AppLanguage.GERMAN -> "Immer Dunkel"
        AppLanguage.RUSSIAN -> "Всегда тёмная"
        AppLanguage.JAPANESE -> "常にダーク"
        AppLanguage.KOREAN -> "항상 다크"
    }

    val speedSlow: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "慢速"
        AppLanguage.ENGLISH -> "Slow"
        AppLanguage.ARABIC -> "بطيء"
        AppLanguage.PORTUGUESE -> "Lento"
        AppLanguage.SPANISH -> "Lento"
        AppLanguage.FRENCH -> "Lent"
        AppLanguage.GERMAN -> "Langsam"
        AppLanguage.RUSSIAN -> "Медленно"
        AppLanguage.JAPANESE -> "低速"
        AppLanguage.KOREAN -> "느림"
    }

    val speedNormal: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "正常"
        AppLanguage.ENGLISH -> "Normal"
        AppLanguage.ARABIC -> "عادي"
        AppLanguage.PORTUGUESE -> "Normal"
        AppLanguage.SPANISH -> "Normal"
        AppLanguage.FRENCH -> "Normal"
        AppLanguage.GERMAN -> "Normal"
        AppLanguage.RUSSIAN -> "Обычно"
        AppLanguage.JAPANESE -> "通常"
        AppLanguage.KOREAN -> "보통"
    }

    val speedFast: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "快速"
        AppLanguage.ENGLISH -> "Fast"
        AppLanguage.ARABIC -> "سريع"
        AppLanguage.PORTUGUESE -> "Rápido"
        AppLanguage.SPANISH -> "Rápido"
        AppLanguage.FRENCH -> "Rapide"
        AppLanguage.GERMAN -> "Schnell"
        AppLanguage.RUSSIAN -> "Быстро"
        AppLanguage.JAPANESE -> "高速"
        AppLanguage.KOREAN -> "빠름"
    }

    val speedInstant: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "即时"
        AppLanguage.ENGLISH -> "Instant"
        AppLanguage.ARABIC -> "فوري"
        AppLanguage.PORTUGUESE -> "Instantâneo"
        AppLanguage.SPANISH -> "Instantáneo"
        AppLanguage.FRENCH -> "Instantané"
        AppLanguage.GERMAN -> "Sofort"
        AppLanguage.RUSSIAN -> "Мгновенно"
        AppLanguage.JAPANESE -> "即時"
        AppLanguage.KOREAN -> "즉시"
    }

    val about: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "关于"
        AppLanguage.ENGLISH -> "About"
        AppLanguage.ARABIC -> "حول"
        AppLanguage.PORTUGUESE -> "Sobre"
        AppLanguage.SPANISH -> "Acerca de"
        AppLanguage.FRENCH -> "À propos"
        AppLanguage.GERMAN -> "Über"
        AppLanguage.RUSSIAN -> "О приложении"
        AppLanguage.JAPANESE -> "について"
        AppLanguage.KOREAN -> "정보"
    }

    val aboutAppDescription: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "安卓上功能最完善的WEB转APK工具型应用。"
        AppLanguage.ENGLISH -> "The most full-featured Web-to-APK utility app on Android."
        AppLanguage.ARABIC -> "تطبيق أدوات Web-to-APK الأكثر اكتمالاً على Android."
        AppLanguage.PORTUGUESE -> "O app utilitário de Web-para-APK mais completo no Android."
        AppLanguage.SPANISH -> "La app utilitaria de Web-a-APK más completa en Android."
        AppLanguage.FRENCH -> "L'app utilitaire Web-vers-APK la plus complète sur Android."
        AppLanguage.GERMAN -> "Die funktionsreichste Web-zu-APK-Utility-App auf Android."
        AppLanguage.RUSSIAN -> "Самое многофункциональное приложение Web-to-APK на Android."
        AppLanguage.JAPANESE -> "Androidで最も多機能なWeb-to-APKユーティリティアプリ。"
        AppLanguage.KOREAN -> "Android에서 가장 기능이 풍부한 Web-to-APK 유틸리티 앱."
    }

    val updateCheckTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "检查更新"
        AppLanguage.ENGLISH -> "Check for Updates"
        AppLanguage.ARABIC -> "التحقق من التحديثات"
        AppLanguage.PORTUGUESE -> "Verificar Atualizações"
        AppLanguage.SPANISH -> "Buscar Actualizaciones"
        AppLanguage.FRENCH -> "Rechercher des Mises à Jour"
        AppLanguage.GERMAN -> "Nach Updates suchen"
        AppLanguage.RUSSIAN -> "Проверить обновления"
        AppLanguage.JAPANESE -> "更新を確認"
        AppLanguage.KOREAN -> "업데이트 확인"
    }
    val updateChecking: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "正在检查更新…"
        AppLanguage.ENGLISH -> "Checking for updates…"
        AppLanguage.ARABIC -> "جارٍ التحقق من التحديثات…"
        AppLanguage.PORTUGUESE -> "Verificando atualizações…"
        AppLanguage.SPANISH -> "Buscando actualizaciones…"
        AppLanguage.FRENCH -> "Recherche de mises à jour…"
        AppLanguage.GERMAN -> "Suche nach Updates…"
        AppLanguage.RUSSIAN -> "Проверка обновлений…"
        AppLanguage.JAPANESE -> "更新を確認中…"
        AppLanguage.KOREAN -> "업데이트 확인 중…"
    }
    val updateUpToDate: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已是最新版本"
        AppLanguage.ENGLISH -> "You're on the latest version"
        AppLanguage.ARABIC -> "أنت تستخدم أحدث إصدار"
        AppLanguage.PORTUGUESE -> "Você está na versão mais recente"
        AppLanguage.SPANISH -> "Estás en la versión más reciente"
        AppLanguage.FRENCH -> "Vous êtes sur la dernière version"
        AppLanguage.GERMAN -> "Sie sind auf der neuesten Version"
        AppLanguage.RUSSIAN -> "У вас последняя версия"
        AppLanguage.JAPANESE -> "最新バージョンを使用中です"
        AppLanguage.KOREAN -> "최신 버전을 사용 중입니다"
    }
    val updateAvailableTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "发现新版本"
        AppLanguage.ENGLISH -> "Update available"
        AppLanguage.ARABIC -> "يتوفّر تحديث"
        AppLanguage.PORTUGUESE -> "Atualização disponível"
        AppLanguage.SPANISH -> "Actualización disponible"
        AppLanguage.FRENCH -> "Mise à jour disponible"
        AppLanguage.GERMAN -> "Update verfügbar"
        AppLanguage.RUSSIAN -> "Доступно обновление"
        AppLanguage.JAPANESE -> "更新が利用可能です"
        AppLanguage.KOREAN -> "업데이트가 있습니다"
    }
    val updateNewVersionLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "最新版本"
        AppLanguage.ENGLISH -> "Latest version"
        AppLanguage.ARABIC -> "أحدث إصدار"
        AppLanguage.PORTUGUESE -> "Versão mais recente"
        AppLanguage.SPANISH -> "Versión más reciente"
        AppLanguage.FRENCH -> "Dernière version"
        AppLanguage.GERMAN -> "Neueste Version"
        AppLanguage.RUSSIAN -> "Последняя версия"
        AppLanguage.JAPANESE -> "最新バージョン"
        AppLanguage.KOREAN -> "최신 버전"
    }
    val updateCurrentVersionLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "当前版本"
        AppLanguage.ENGLISH -> "Current version"
        AppLanguage.ARABIC -> "الإصدار الحالي"
        AppLanguage.PORTUGUESE -> "Versão atual"
        AppLanguage.SPANISH -> "Versión actual"
        AppLanguage.FRENCH -> "Version actuelle"
        AppLanguage.GERMAN -> "Aktuelle Version"
        AppLanguage.RUSSIAN -> "Текущая версия"
        AppLanguage.JAPANESE -> "現在のバージョン"
        AppLanguage.KOREAN -> "현재 버전"
    }
    val updateDownloadButton: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下载"
        AppLanguage.ENGLISH -> "Download"
        AppLanguage.ARABIC -> "تنزيل"
        AppLanguage.PORTUGUESE -> "Baixar"
        AppLanguage.SPANISH -> "Descargar"
        AppLanguage.FRENCH -> "Télécharger"
        AppLanguage.GERMAN -> "Herunterladen"
        AppLanguage.RUSSIAN -> "Скачать"
        AppLanguage.JAPANESE -> "ダウンロード"
        AppLanguage.KOREAN -> "다운로드"
    }
    val updateDownloadStarted: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已开始下载，完成后将提示安装"
        AppLanguage.ENGLISH -> "Download started; you'll be prompted to install when it finishes"
        AppLanguage.ARABIC -> "بدأ التنزيل؛ ستتم مطالبتك بالتثبيت عند الانتهاء"
        AppLanguage.PORTUGUESE -> "Download iniciado; você será solicitado a instalar ao concluir"
        AppLanguage.SPANISH -> "Descarga iniciada; se te pedirá instalar al finalizar"
        AppLanguage.FRENCH -> "Téléchargement démarré ; vous serez invité à installer à la fin"
        AppLanguage.GERMAN -> "Download gestartet; Sie werden bei Fertigstellung zur Installation aufgefordert"
        AppLanguage.RUSSIAN -> "Загрузка начата; по завершении вам будет предложено установить"
        AppLanguage.JAPANESE -> "ダウンロードを開始しました。完了時にインストールを促されます"
        AppLanguage.KOREAN -> "다운로드가 시작되었습니다. 완료되면 설치하라는 메시지가 표시됩니다"
    }
    val updateInstallReady: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "安装包已就绪"
        AppLanguage.ENGLISH -> "Update package ready"
        AppLanguage.ARABIC -> "حزمة التحديث جاهزة"
        AppLanguage.PORTUGUESE -> "Pacote de atualização pronto"
        AppLanguage.SPANISH -> "Paquete de actualización listo"
        AppLanguage.FRENCH -> "Mise à jour prête"
        AppLanguage.GERMAN -> "Update-Paket bereit"
        AppLanguage.RUSSIAN -> "Пакет обновления готов"
        AppLanguage.JAPANESE -> "更新パッケージの準備ができました"
        AppLanguage.KOREAN -> "업데이트 패키지 준비됨"
    }
    val updateDownloading: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "正在下载…"
        AppLanguage.ENGLISH -> "Downloading…"
        AppLanguage.ARABIC -> "جاري التنزيل…"
        AppLanguage.PORTUGUESE -> "Baixando…"
        AppLanguage.SPANISH -> "Descargando…"
        AppLanguage.FRENCH -> "Téléchargement…"
        AppLanguage.GERMAN -> "Wird heruntergeladen…"
        AppLanguage.RUSSIAN -> "Загрузка…"
        AppLanguage.JAPANESE -> "ダウンロード中…"
        AppLanguage.KOREAN -> "다운로드 중…"
    }
    val updateVerifying: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "正在校验…"
        AppLanguage.ENGLISH -> "Verifying…"
        AppLanguage.ARABIC -> "جاري التحقق…"
        AppLanguage.PORTUGUESE -> "Verificando…"
        AppLanguage.SPANISH -> "Verificando…"
        AppLanguage.FRENCH -> "Vérification…"
        AppLanguage.GERMAN -> "Wird verifiziert…"
        AppLanguage.RUSSIAN -> "Проверка…"
        AppLanguage.JAPANESE -> "検証中…"
        AppLanguage.KOREAN -> "검증 중…"
    }
    val updateCancelDownload: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "取消下载"
        AppLanguage.ENGLISH -> "Cancel download"
        AppLanguage.ARABIC -> "إلغاء التنزيل"
        AppLanguage.PORTUGUESE -> "Cancelar download"
        AppLanguage.SPANISH -> "Cancelar descarga"
        AppLanguage.FRENCH -> "Annuler le téléchargement"
        AppLanguage.GERMAN -> "Download abbrechen"
        AppLanguage.RUSSIAN -> "Отменить загрузку"
        AppLanguage.JAPANESE -> "ダウンロードをキャンセル"
        AppLanguage.KOREAN -> "다운로드 취소"
    }
    val updateRetry: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "重试"
        AppLanguage.ENGLISH -> "Retry"
        AppLanguage.ARABIC -> "إعادة المحاولة"
        AppLanguage.PORTUGUESE -> "Tentar novamente"
        AppLanguage.SPANISH -> "Reintentar"
        AppLanguage.FRENCH -> "Réessayer"
        AppLanguage.GERMAN -> "Erneut versuchen"
        AppLanguage.RUSSIAN -> "Повторить"
        AppLanguage.JAPANESE -> "再試行"
        AppLanguage.KOREAN -> "재시도"
    }
    val updateDownloadFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下载失败"
        AppLanguage.ENGLISH -> "Download failed"
        AppLanguage.ARABIC -> "فشل التنزيل"
        AppLanguage.PORTUGUESE -> "Falha no download"
        AppLanguage.SPANISH -> "Error en la descarga"
        AppLanguage.FRENCH -> "Échec du téléchargement"
        AppLanguage.GERMAN -> "Download fehlgeschlagen"
        AppLanguage.RUSSIAN -> "Ошибка загрузки"
        AppLanguage.JAPANESE -> "ダウンロードに失敗しました"
        AppLanguage.KOREAN -> "다운로드 실패"
    }
    val updateReadyHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下载完成，点击安装"
        AppLanguage.ENGLISH -> "Downloaded. Tap install to continue."
        AppLanguage.ARABIC -> "تم التنزيل. اضغط تثبيت للمتابعة."
        AppLanguage.PORTUGUESE -> "Baixado. Toque em instalar para continuar."
        AppLanguage.SPANISH -> "Descargado. Toca instalar para continuar."
        AppLanguage.FRENCH -> "Téléchargé. Appuyez sur installer pour continuer."
        AppLanguage.GERMAN -> "Heruntergeladen. Zum Fortfahren auf Installieren tippen."
        AppLanguage.RUSSIAN -> "Загружено. Нажмите «Установить», чтобы продолжить."
        AppLanguage.JAPANESE -> "ダウンロード完了。インストールをタップして続行してください。"
        AppLanguage.KOREAN -> "다운로드 완료. 계속하려면 설치를 탭하세요."
    }
    val updatePerSec: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "/秒"
        AppLanguage.ENGLISH -> "/s"
        AppLanguage.ARABIC -> "/ث"
        AppLanguage.PORTUGUESE -> "/s"
        AppLanguage.SPANISH -> "/s"
        AppLanguage.FRENCH -> "/s"
        AppLanguage.GERMAN -> "/s"
        AppLanguage.RUSSIAN -> "/с"
        AppLanguage.JAPANESE -> "/秒"
        AppLanguage.KOREAN -> "/초"
    }
    val updateReleaseNotesLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "更新说明"
        AppLanguage.ENGLISH -> "Release notes"
        AppLanguage.ARABIC -> "ملاحظات الإصدار"
        AppLanguage.PORTUGUESE -> "Notas da versão"
        AppLanguage.SPANISH -> "Notas de la versión"
        AppLanguage.FRENCH -> "Notes de version"
        AppLanguage.GERMAN -> "Versionshinweise"
        AppLanguage.RUSSIAN -> "Примечания к версии"
        AppLanguage.JAPANESE -> "リリースノート"
        AppLanguage.KOREAN -> "릴리스 노트"
    }
    val updateSizeLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "安装包大小"
        AppLanguage.ENGLISH -> "Download size"
        AppLanguage.ARABIC -> "حجم التنزيل"
        AppLanguage.PORTUGUESE -> "Tamanho do download"
        AppLanguage.SPANISH -> "Tamaño de descarga"
        AppLanguage.FRENCH -> "Taille du téléchargement"
        AppLanguage.GERMAN -> "Downloadgröße"
        AppLanguage.RUSSIAN -> "Размер загрузки"
        AppLanguage.JAPANESE -> "ダウンロードサイズ"
        AppLanguage.KOREAN -> "다운로드 크기"
    }
    val updateVerificationFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下载文件校验失败（SHA-256 不匹配），已删除以保护安全"
        AppLanguage.ENGLISH -> "Downloaded file failed integrity check (SHA-256 mismatch); it was deleted for your safety"
        AppLanguage.ARABIC -> "فشل التحقق من سلامة الملف الذي تم تنزيله (عدم تطابق SHA-256)؛ تم حذفه لحمايتك"
        AppLanguage.PORTUGUESE -> "O arquivo baixado falhou na verificação de integridade (SHA-256 incompatível); foi excluído para sua segurança"
        AppLanguage.SPANISH -> "El archivo descargado falló la verificación de integridad (SHA-256 no coincide); fue eliminado por tu seguridad"
        AppLanguage.FRENCH -> "Le fichier téléchargé a échoué la vérification d'intégrité (SHA-256 non correspondant) ; il a été supprimé pour votre sécurité"
        AppLanguage.GERMAN -> "Die heruntergeladene Datei hat die Integritätsprüfung nicht bestanden (SHA-256 stimmt nicht überein); sie wurde zu Ihrer Sicherheit gelöscht"
        AppLanguage.RUSSIAN -> "Скачанный файл не прошёл проверку целостности (несоответствие SHA-256); он был удалён для вашей безопасности"
        AppLanguage.JAPANESE -> "ダウンロードしたファイルの整合性チェックに失敗しました（SHA-256不一致）。安全のため削除されました"
        AppLanguage.KOREAN -> "다운로드한 파일의 무결성 검사에 실패했습니다 (SHA-256 불일치). 안전을 위해 삭제되었습니다"
    }

    val versionHistoryTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "版本历史"
        AppLanguage.ENGLISH -> "Version history"
        AppLanguage.ARABIC -> "سجل الإصدارات"
        AppLanguage.PORTUGUESE -> "Histórico de versões"
        AppLanguage.SPANISH -> "Historial de versiones"
        AppLanguage.FRENCH -> "Historique des versions"
        AppLanguage.GERMAN -> "Versionsverlauf"
        AppLanguage.RUSSIAN -> "История версий"
        AppLanguage.JAPANESE -> "バージョン履歴"
        AppLanguage.KOREAN -> "버전 기록"
    }

    val versionHistoryButtonHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "版本历史"
        AppLanguage.ENGLISH -> "Version history"
        AppLanguage.ARABIC -> "سجل الإصدارات"
        AppLanguage.PORTUGUESE -> "Histórico de versões"
        AppLanguage.SPANISH -> "Historial de versiones"
        AppLanguage.FRENCH -> "Historique des versions"
        AppLanguage.GERMAN -> "Versionsverlauf"
        AppLanguage.RUSSIAN -> "История версий"
        AppLanguage.JAPANESE -> "バージョン履歴"
        AppLanguage.KOREAN -> "버전 기록"
    }

    val versionHistoryLoading: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "正在加载版本历史…"
        AppLanguage.ENGLISH -> "Loading version history…"
        AppLanguage.ARABIC -> "جارٍ تحميل سجل الإصدارات…"
        AppLanguage.PORTUGUESE -> "Carregando histórico de versões…"
        AppLanguage.SPANISH -> "Cargando historial de versiones…"
        AppLanguage.FRENCH -> "Chargement de l'historique des versions…"
        AppLanguage.GERMAN -> "Versionsverlauf wird geladen…"
        AppLanguage.RUSSIAN -> "Загрузка истории версий…"
        AppLanguage.JAPANESE -> "バージョン履歴を読み込み中…"
        AppLanguage.KOREAN -> "버전 기록을 불러오는 중…"
    }

    val versionHistoryLoadFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "加载版本历史失败"
        AppLanguage.ENGLISH -> "Failed to load version history"
        AppLanguage.ARABIC -> "فشل تحميل سجل الإصدارات"
        AppLanguage.PORTUGUESE -> "Falha ao carregar o histórico de versões"
        AppLanguage.SPANISH -> "Error al cargar el historial de versiones"
        AppLanguage.FRENCH -> "Échec du chargement de l'historique des versions"
        AppLanguage.GERMAN -> "Versionsverlauf konnte nicht geladen werden"
        AppLanguage.RUSSIAN -> "Не удалось загрузить историю версий"
        AppLanguage.JAPANESE -> "バージョン履歴の読み込みに失敗しました"
        AppLanguage.KOREAN -> "버전 기록을 불러오지 못했습니다"
    }

    val versionHistoryEmpty: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "暂无历史版本"
        AppLanguage.ENGLISH -> "No releases yet"
        AppLanguage.ARABIC -> "لا توجد إصدارات بعد"
        AppLanguage.PORTUGUESE -> "Nenhuma versão ainda"
        AppLanguage.SPANISH -> "Aún no hay versiones"
        AppLanguage.FRENCH -> "Aucune version pour le moment"
        AppLanguage.GERMAN -> "Noch keine Versionen"
        AppLanguage.RUSSIAN -> "Версий пока нет"
        AppLanguage.JAPANESE -> "まだリリースがありません"
        AppLanguage.KOREAN -> "아직 릴리스가 없습니다"
    }

    val versionHistoryCurrentVersion: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "当前版本"
        AppLanguage.ENGLISH -> "Current"
        AppLanguage.ARABIC -> "الحالي"
        AppLanguage.PORTUGUESE -> "Atual"
        AppLanguage.SPANISH -> "Actual"
        AppLanguage.FRENCH -> "Actuelle"
        AppLanguage.GERMAN -> "Aktuell"
        AppLanguage.RUSSIAN -> "Текущая"
        AppLanguage.JAPANESE -> "現行"
        AppLanguage.KOREAN -> "현재"
    }

    val versionHistoryDownload: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下载此版本"
        AppLanguage.ENGLISH -> "Download this version"
        AppLanguage.ARABIC -> "تنزيل هذا الإصدار"
        AppLanguage.PORTUGUESE -> "Baixar esta versão"
        AppLanguage.SPANISH -> "Descargar esta versión"
        AppLanguage.FRENCH -> "Télécharger cette version"
        AppLanguage.GERMAN -> "Diese Version herunterladen"
        AppLanguage.RUSSIAN -> "Скачать эту версию"
        AppLanguage.JAPANESE -> "このバージョンをダウンロード"
        AppLanguage.KOREAN -> "이 버전 다운로드"
    }

    val versionHistoryInstall: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "安装"
        AppLanguage.ENGLISH -> "Install"
        AppLanguage.ARABIC -> "تثبيت"
        AppLanguage.PORTUGUESE -> "Instalar"
        AppLanguage.SPANISH -> "Instalar"
        AppLanguage.FRENCH -> "Installer"
        AppLanguage.GERMAN -> "Installieren"
        AppLanguage.RUSSIAN -> "Установить"
        AppLanguage.JAPANESE -> "インストール"
        AppLanguage.KOREAN -> "설치"
    }

    val versionHistoryNoApk: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "此版本无安装包"
        AppLanguage.ENGLISH -> "No APK for this release"
        AppLanguage.ARABIC -> "لا يوجد APK لهذا الإصدار"
        AppLanguage.PORTUGUESE -> "Sem APK para esta versão"
        AppLanguage.SPANISH -> "Sin APK para esta versión"
        AppLanguage.FRENCH -> "Aucun APK pour cette version"
        AppLanguage.GERMAN -> "Kein APK für diese Version"
        AppLanguage.RUSSIAN -> "Нет APK для этой версии"
        AppLanguage.JAPANESE -> "このリリースのAPKはありません"
        AppLanguage.KOREAN -> "이 릴리스에 대한 APK가 없습니다"
    }

    val downloadComplete: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下载完成，正在安装..."
        AppLanguage.ENGLISH -> "Download complete, installing..."
        AppLanguage.ARABIC -> "اكتمل التحميل، جاري التثبيت..."
        AppLanguage.PORTUGUESE -> "Download concluído, instalando..."
        AppLanguage.SPANISH -> "Descarga completa, instalando..."
        AppLanguage.FRENCH -> "Téléchargement terminé, installation..."
        AppLanguage.GERMAN -> "Download abgeschlossen, wird installiert..."
        AppLanguage.RUSSIAN -> "Загрузка завершена, установка..."
        AppLanguage.JAPANESE -> "ダウンロード完了、インストール中..."
        AppLanguage.KOREAN -> "다운로드 완료, 설치 중..."
    }

    val close: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "关闭"
        AppLanguage.ENGLISH -> "Close"
        AppLanguage.ARABIC -> "إغلاق"
        AppLanguage.PORTUGUESE -> "Fechar"
        AppLanguage.SPANISH -> "Cerrar"
        AppLanguage.FRENCH -> "Fermer"
        AppLanguage.GERMAN -> "Schließen"
        AppLanguage.RUSSIAN -> "Закрыть"
        AppLanguage.JAPANESE -> "閉じる"
        AppLanguage.KOREAN -> "닫기"
    }

    val cancel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "取消"
        AppLanguage.ENGLISH -> "Cancel"
        AppLanguage.ARABIC -> "إلغاء"
        AppLanguage.PORTUGUESE -> "Cancelar"
        AppLanguage.SPANISH -> "Cancelar"
        AppLanguage.FRENCH -> "Annuler"
        AppLanguage.GERMAN -> "Abbrechen"
        AppLanguage.RUSSIAN -> "Отмена"
        AppLanguage.JAPANESE -> "キャンセル"
        AppLanguage.KOREAN -> "취소"
    }

    val copy: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "复制"
        AppLanguage.ENGLISH -> "Copy"
        AppLanguage.ARABIC -> "نسخ"
        AppLanguage.PORTUGUESE -> "Copiar"
        AppLanguage.SPANISH -> "Copiar"
        AppLanguage.FRENCH -> "Copier"
        AppLanguage.GERMAN -> "Kopieren"
        AppLanguage.RUSSIAN -> "Копировать"
        AppLanguage.JAPANESE -> "コピー"
        AppLanguage.KOREAN -> "복사"
    }

    val share: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Share"
        AppLanguage.ENGLISH -> "Share"
        AppLanguage.ARABIC -> "مشاركة"
        AppLanguage.PORTUGUESE -> "Compartilhar"
        AppLanguage.SPANISH -> "Compartir"
        AppLanguage.FRENCH -> "Partager"
        AppLanguage.GERMAN -> "Teilen"
        AppLanguage.RUSSIAN -> "Поделиться"
        AppLanguage.JAPANESE -> "共有"
        AppLanguage.KOREAN -> "공유"
    }

    val download: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下载"
        AppLanguage.ENGLISH -> "Download"
        AppLanguage.ARABIC -> "تحميل"
        AppLanguage.PORTUGUESE -> "Baixar"
        AppLanguage.SPANISH -> "Descargar"
        AppLanguage.FRENCH -> "Télécharger"
        AppLanguage.GERMAN -> "Herunterladen"
        AppLanguage.RUSSIAN -> "Скачать"
        AppLanguage.JAPANESE -> "ダウンロード"
        AppLanguage.KOREAN -> "다운로드"
    }

    val remove: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Remove"
        AppLanguage.ENGLISH -> "Remove"
        AppLanguage.ARABIC -> "إزالة"
        AppLanguage.PORTUGUESE -> "Remover"
        AppLanguage.SPANISH -> "Eliminar"
        AppLanguage.FRENCH -> "Supprimer"
        AppLanguage.GERMAN -> "Entfernen"
        AppLanguage.RUSSIAN -> "Удалить"
        AppLanguage.JAPANESE -> "削除"
        AppLanguage.KOREAN -> "제거"
    }

    val clear: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "清除"
        AppLanguage.ENGLISH -> "Clear"
        AppLanguage.ARABIC -> "مسح"
        AppLanguage.PORTUGUESE -> "Limpar"
        AppLanguage.SPANISH -> "Borrar"
        AppLanguage.FRENCH -> "Effacer"
        AppLanguage.GERMAN -> "Löschen"
        AppLanguage.RUSSIAN -> "Очистить"
        AppLanguage.JAPANESE -> "クリア"
        AppLanguage.KOREAN -> "지우기"
    }

    val add: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Add"
        AppLanguage.ENGLISH -> "Add"
        AppLanguage.ARABIC -> "إضافة"
        AppLanguage.PORTUGUESE -> "Adicionar"
        AppLanguage.SPANISH -> "Añadir"
        AppLanguage.FRENCH -> "Ajouter"
        AppLanguage.GERMAN -> "Hinzufügen"
        AppLanguage.RUSSIAN -> "Добавить"
        AppLanguage.JAPANESE -> "追加"
        AppLanguage.KOREAN -> "추가"
    }

    val enabled: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已启用"
        AppLanguage.ENGLISH -> "Enabled"
        AppLanguage.ARABIC -> "مفعل"
        AppLanguage.PORTUGUESE -> "Ativado"
        AppLanguage.SPANISH -> "Activado"
        AppLanguage.FRENCH -> "Activé"
        AppLanguage.GERMAN -> "Aktiviert"
        AppLanguage.RUSSIAN -> "Включено"
        AppLanguage.JAPANESE -> "有効"
        AppLanguage.KOREAN -> "활성화됨"
    }

    val enable: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "启用"
        AppLanguage.ENGLISH -> "Enable"
        AppLanguage.ARABIC -> "تمكين"
        AppLanguage.PORTUGUESE -> "Ativar"
        AppLanguage.SPANISH -> "Activar"
        AppLanguage.FRENCH -> "Activer"
        AppLanguage.GERMAN -> "Aktivieren"
        AppLanguage.RUSSIAN -> "Включить"
        AppLanguage.JAPANESE -> "有効化"
        AppLanguage.KOREAN -> "활성화"
    }

    val disable: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "禁用"
        AppLanguage.ENGLISH -> "Disable"
        AppLanguage.ARABIC -> "تعطيل"
        AppLanguage.PORTUGUESE -> "Desativar"
        AppLanguage.SPANISH -> "Desactivar"
        AppLanguage.FRENCH -> "Désactiver"
        AppLanguage.GERMAN -> "Deaktivieren"
        AppLanguage.RUSSIAN -> "Отключить"
        AppLanguage.JAPANESE -> "無効化"
        AppLanguage.KOREAN -> "비활성화"
    }

    val emptyStateHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "点击下方按钮创建您的第一个应用"
        AppLanguage.ENGLISH -> "Tap the button below to create your first app"
        AppLanguage.ARABIC -> "اضغط على الزر أدناه لإنشاء تطبيقك الأول"
        AppLanguage.PORTUGUESE -> "Toque no botão abaixo para criar seu primeiro app"
        AppLanguage.SPANISH -> "Toca el botón de abajo para crear tu primera app"
        AppLanguage.FRENCH -> "Touchez le bouton ci-dessous pour créer votre première app"
        AppLanguage.GERMAN -> "Tippen Sie auf den Button unten, um Ihre erste App zu erstellen"
        AppLanguage.RUSSIAN -> "Нажмите кнопку ниже, чтобы создать своё первое приложение"
        AppLanguage.JAPANESE -> "下のボタンをタップして最初のアプリを作成"
        AppLanguage.KOREAN -> "아래 버튼을 탭하여 첫 앱을 만드세요"
    }

    val appIconModifier: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "应用修改器"
        AppLanguage.ENGLISH -> "App Modifier"
        AppLanguage.ARABIC -> "معدل التطبيق"
        AppLanguage.PORTUGUESE -> "Modificador de Apps"
        AppLanguage.SPANISH -> "Modificador de Apps"
        AppLanguage.FRENCH -> "Modificateur d'Apps"
        AppLanguage.GERMAN -> "App-Modifikator"
        AppLanguage.RUSSIAN -> "Модификатор приложений"
        AppLanguage.JAPANESE -> "アプリモディファイア"
        AppLanguage.KOREAN -> "앱 수정기"
    }

    val searchApps: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "搜索应用..."
        AppLanguage.ENGLISH -> "Search apps..."
        AppLanguage.ARABIC -> "البحث عن التطبيقات..."
        AppLanguage.PORTUGUESE -> "Buscar apps..."
        AppLanguage.SPANISH -> "Buscar apps..."
        AppLanguage.FRENCH -> "Rechercher des apps..."
        AppLanguage.GERMAN -> "Apps suchen..."
        AppLanguage.RUSSIAN -> "Поиск приложений..."
        AppLanguage.JAPANESE -> "アプリを検索..."
        AppLanguage.KOREAN -> "앱 검색..."
    }

    val userApps: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "用户应用"
        AppLanguage.ENGLISH -> "User Apps"
        AppLanguage.ARABIC -> "تطبيقات المستخدم"
        AppLanguage.PORTUGUESE -> "Apps do Usuário"
        AppLanguage.SPANISH -> "Apps de Usuario"
        AppLanguage.FRENCH -> "Apps Utilisateur"
        AppLanguage.GERMAN -> "Benutzer-Apps"
        AppLanguage.RUSSIAN -> "Пользовательские приложения"
        AppLanguage.JAPANESE -> "ユーザーアプリ"
        AppLanguage.KOREAN -> "사용자 앱"
    }

    val systemApps: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "系统应用"
        AppLanguage.ENGLISH -> "System Apps"
        AppLanguage.ARABIC -> "تطبيقات النظام"
        AppLanguage.PORTUGUESE -> "Apps do Sistema"
        AppLanguage.SPANISH -> "Apps del Sistema"
        AppLanguage.FRENCH -> "Apps Système"
        AppLanguage.GERMAN -> "System-Apps"
        AppLanguage.RUSSIAN -> "Системные приложения"
        AppLanguage.JAPANESE -> "システムアプリ"
        AppLanguage.KOREAN -> "시스템 앱"
    }

    val modifyApp: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "修改应用"
        AppLanguage.ENGLISH -> "Modify App"
        AppLanguage.ARABIC -> "تعديل التطبيق"
        AppLanguage.PORTUGUESE -> "Modificar App"
        AppLanguage.SPANISH -> "Modificar App"
        AppLanguage.FRENCH -> "Modifier l'App"
        AppLanguage.GERMAN -> "App ändern"
        AppLanguage.RUSSIAN -> "Изменить приложение"
        AppLanguage.JAPANESE -> "アプリを変更"
        AppLanguage.KOREAN -> "앱 수정"
    }

    val cloneInstall: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "克隆安装"
        AppLanguage.ENGLISH -> "Clone Install"
        AppLanguage.ARABIC -> "تثبيت نسخة"
        AppLanguage.PORTUGUESE -> "Instalar Clone"
        AppLanguage.SPANISH -> "Instalar Clon"
        AppLanguage.FRENCH -> "Installer un Clone"
        AppLanguage.GERMAN -> "Klon installieren"
        AppLanguage.RUSSIAN -> "Установить клон"
        AppLanguage.JAPANESE -> "クローンをインストール"
        AppLanguage.KOREAN -> "복제 설치"
    }

    val originalApp: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "原应用"
        AppLanguage.ENGLISH -> "Original App"
        AppLanguage.ARABIC -> "التطبيق الأصلي"
        AppLanguage.PORTUGUESE -> "App Original"
        AppLanguage.SPANISH -> "App Original"
        AppLanguage.FRENCH -> "App Originale"
        AppLanguage.GERMAN -> "Original-App"
        AppLanguage.RUSSIAN -> "Оригинальное приложение"
        AppLanguage.JAPANESE -> "オリジナルアプリ"
        AppLanguage.KOREAN -> "원본 앱"
    }

    val useOriginalIcon: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "使用原图标"
        AppLanguage.ENGLISH -> "Use Original Icon"
        AppLanguage.ARABIC -> "استخدام الأيقونة الأصلية"
        AppLanguage.PORTUGUESE -> "Usar Ícone Original"
        AppLanguage.SPANISH -> "Usar Icono Original"
        AppLanguage.FRENCH -> "Utiliser l'Icône Originale"
        AppLanguage.GERMAN -> "Original-Symbol verwenden"
        AppLanguage.RUSSIAN -> "Использовать оригинальную иконку"
        AppLanguage.JAPANESE -> "オリジナルアイコンを使用"
        AppLanguage.KOREAN -> "원본 아이콘 사용"
    }

    val shortcutCreated: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "快捷方式创建成功"
        AppLanguage.ENGLISH -> "Shortcut created successfully"
        AppLanguage.ARABIC -> "تم إنشاء الاختصار بنجاح"
        AppLanguage.PORTUGUESE -> "Atalho criado com sucesso"
        AppLanguage.SPANISH -> "Acceso directo creado con éxito"
        AppLanguage.FRENCH -> "Raccourci créé avec succès"
        AppLanguage.GERMAN -> "Verknüpfung erfolgreich erstellt"
        AppLanguage.RUSSIAN -> "Ярлык успешно создан"
        AppLanguage.JAPANESE -> "ショートカットを作成しました"
        AppLanguage.KOREAN -> "바로가기가 생성되었습니다"
    }

    val cloneSuccess: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "克隆成功，请确认安装"
        AppLanguage.ENGLISH -> "Clone successful, please confirm installation"
        AppLanguage.ARABIC -> "تم النسخ بنجاح، يرجى تأكيد التثبيت"
        AppLanguage.PORTUGUESE -> "Clone bem-sucedido, confirme a instalação"
        AppLanguage.SPANISH -> "Clon exitoso, confirma la instalación"
        AppLanguage.FRENCH -> "Clonage réussi, veuillez confirmer l'installation"
        AppLanguage.GERMAN -> "Klonen erfolgreich, bitte Installation bestätigen"
        AppLanguage.RUSSIAN -> "Клонирование успешно, подтвердите установку"
        AppLanguage.JAPANESE -> "クローン成功、インストールを確認してください"
        AppLanguage.KOREAN -> "복제 성공, 설치를 확인해 주세요"
    }

    val appNotFound: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "未找到该应用"
        AppLanguage.ENGLISH -> "App not found"
        AppLanguage.ARABIC -> "لم يتم العثور على التطبيق"
        AppLanguage.PORTUGUESE -> "App não encontrado"
        AppLanguage.SPANISH -> "App no encontrada"
        AppLanguage.FRENCH -> "App non trouvée"
        AppLanguage.GERMAN -> "App nicht gefunden"
        AppLanguage.RUSSIAN -> "Приложение не найдено"
        AppLanguage.JAPANESE -> "アプリが見つかりません"
        AppLanguage.KOREAN -> "앱을 찾을 수 없습니다"
    }

    val appModifierEmptyMessage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "尝试切换筛选或修改搜索关键字"
        AppLanguage.ENGLISH -> "Try a different filter or search term"
        AppLanguage.ARABIC -> "حاول تصفية مختلفة أو كلمة بحث أخرى"
        AppLanguage.PORTUGUESE -> "Tente um filtro ou termo de busca diferente"
        AppLanguage.SPANISH -> "Prueba un filtro o término de búsqueda diferente"
        AppLanguage.FRENCH -> "Essayez un autre filtre ou terme de recherche"
        AppLanguage.GERMAN -> "Versuchen Sie einen anderen Filter oder Suchbegriff"
        AppLanguage.RUSSIAN -> "Попробуйте другой фильтр или поисковый запрос"
        AppLanguage.JAPANESE -> "別のフィルターや検索キーワードを試してください"
        AppLanguage.KOREAN -> "다른 필터나 검색어를 시도해 보세요"
    }

    val resourceEncryption: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "资源加密"
        AppLanguage.ENGLISH -> "Resource Encryption"
        AppLanguage.ARABIC -> "تشفير الموارد"
        AppLanguage.PORTUGUESE -> "Criptografia de Recursos"
        AppLanguage.SPANISH -> "Cifrado de Recursos"
        AppLanguage.FRENCH -> "Chiffrement des Ressources"
        AppLanguage.GERMAN -> "Ressourcenverschlüsselung"
        AppLanguage.RUSSIAN -> "Шифрование ресурсов"
        AppLanguage.JAPANESE -> "リソース暗号化"
        AppLanguage.KOREAN -> "리소스 암호화"
    }

    val encryptionEnabled: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已启用加密保护"
        AppLanguage.ENGLISH -> "Encryption protection enabled"
        AppLanguage.ARABIC -> "تم تفعيل حماية التشفير"
        AppLanguage.PORTUGUESE -> "Proteção por criptografia ativada"
        AppLanguage.SPANISH -> "Protección por cifrado activada"
        AppLanguage.FRENCH -> "Protection par chiffrement activée"
        AppLanguage.GERMAN -> "Verschlüsselungsschutz aktiviert"
        AppLanguage.RUSSIAN -> "Защита шифрованием включена"
        AppLanguage.JAPANESE -> "暗号化保護が有効です"
        AppLanguage.KOREAN -> "암호화 보호가 활성화됨"
    }

    val encryptionKeyMode: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "密钥来源"
        AppLanguage.ENGLISH -> "Key Source"
        AppLanguage.ARABIC -> "مصدر المفتاح"
        AppLanguage.PORTUGUESE -> "Origem da Chave"
        AppLanguage.SPANISH -> "Origen de la Clave"
        AppLanguage.FRENCH -> "Source de la clé"
        AppLanguage.GERMAN -> "Schlüsselquelle"
        AppLanguage.RUSSIAN -> "Источник ключа"
        AppLanguage.JAPANESE -> "キーの生成元"
        AppLanguage.KOREAN -> "키 소스"
    }

    val encryptionKeyModeSignature: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "签名绑定"
        AppLanguage.ENGLISH -> "Signature-bound"
        AppLanguage.ARABIC -> "مرتبط بالتوقيع"
        AppLanguage.PORTUGUESE -> "Vinculado à assinatura"
        AppLanguage.SPANISH -> "Vinculado a la firma"
        AppLanguage.FRENCH -> "Lié à la signature"
        AppLanguage.GERMAN -> "Signaturgebunden"
        AppLanguage.RUSSIAN -> "Привязка к подписи"
        AppLanguage.JAPANESE -> "署名に紐付け"
        AppLanguage.KOREAN -> "서명 바인딩"
    }

    val encryptionKeyModeEmbedded: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "内置密钥（商店安全）"
        AppLanguage.ENGLISH -> "Embedded key (store-safe)"
        AppLanguage.ARABIC -> "مفتاح مضمّن (آمن للمتجر)"
        AppLanguage.PORTUGUESE -> "Chave embutida (segura p/ loja)"
        AppLanguage.SPANISH -> "Clave integrada (apta para tienda)"
        AppLanguage.FRENCH -> "Clé intégrée (compatible store)"
        AppLanguage.GERMAN -> "Eingebetteter Schlüssel (store-sicher)"
        AppLanguage.RUSSIAN -> "Встроенный ключ (для магазина)"
        AppLanguage.JAPANESE -> "埋め込みキー（ストア対応）"
        AppLanguage.KOREAN -> "내장 키 (스토어 안전)"
    }

    val encryptionKeyModeHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "签名绑定模式在 Play 商店重签或手动重签名后会解密失败；发布到商店请选择内置密钥"
        AppLanguage.ENGLISH -> "Signature-bound mode fails to decrypt after Play Store or manual re-signing; choose embedded key for store releases"
        AppLanguage.ARABIC -> "الوضع المرتبط بالتوقيع يفشل في فك التشفير بعد إعادة التوقيع من المتجر أو يدويًا؛ اختر المفتاح المضمّن لإصدارات المتجر"
        AppLanguage.PORTUGUESE -> "O modo vinculado à assinatura falha ao descriptografar após reassinatura pela Play Store ou manual; use a chave embutida para publicações em loja"
        AppLanguage.SPANISH -> "El modo vinculado a la firma falla al descifrar tras refirmar en Play Store o manualmente; elige la clave integrada para publicar en tiendas"
        AppLanguage.FRENCH -> "Le mode lié à la signature échoue après une resignature Play Store ou manuelle ; choisissez la clé intégrée pour les publications en store"
        AppLanguage.GERMAN -> "Der signaturgebundene Modus schlägt nach Play-Store- oder manueller Neusignierung fehl; für Store-Releases den eingebetteten Schlüssel wählen"
        AppLanguage.RUSSIAN -> "Режим привязки к подписи ломается после переподписи в Play Store или вручную; для публикации в магазине выберите встроенный ключ"
        AppLanguage.JAPANESE -> "署名紐付けモードは Play ストアや手動での再署名後に復号に失敗します。ストア公開では埋め込みキーを選んでください"
        AppLanguage.KOREAN -> "서명 바인딩 모드는 Play 스토어 또는 수동 재서명 후 복호화에 실패합니다. 스토어 배포에는 내장 키를 선택하세요"
    }

    val basic: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "基础"
        AppLanguage.ENGLISH -> "Basic"
        AppLanguage.ARABIC -> "أساسي"
        AppLanguage.PORTUGUESE -> "Básico"
        AppLanguage.SPANISH -> "Básico"
        AppLanguage.FRENCH -> "Basique"
        AppLanguage.GERMAN -> "Basis"
        AppLanguage.RUSSIAN -> "Базовый"
        AppLanguage.JAPANESE -> "基本"
        AppLanguage.KOREAN -> "기본"
    }

    val standard: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "标准"
        AppLanguage.ENGLISH -> "Standard"
        AppLanguage.ARABIC -> "قياسي"
        AppLanguage.PORTUGUESE -> "Padrão"
        AppLanguage.SPANISH -> "Estándar"
        AppLanguage.FRENCH -> "Standard"
        AppLanguage.GERMAN -> "Standard"
        AppLanguage.RUSSIAN -> "Стандартный"
        AppLanguage.JAPANESE -> "標準"
        AppLanguage.KOREAN -> "표준"
    }

    val isolatedEnvironment: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "独立环境"
        AppLanguage.ENGLISH -> "Isolated Environment"
        AppLanguage.ARABIC -> "بيئة معزولة"
        AppLanguage.PORTUGUESE -> "Ambiente Isolado"
        AppLanguage.SPANISH -> "Entorno Aislado"
        AppLanguage.FRENCH -> "Environnement Isolé"
        AppLanguage.GERMAN -> "Isolierte Umgebung"
        AppLanguage.RUSSIAN -> "Изолированная среда"
        AppLanguage.JAPANESE -> "隔離環境"
        AppLanguage.KOREAN -> "격리 환경"
    }

    val antiDetectionEnabled: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已启用防检测保护"
        AppLanguage.ENGLISH -> "Anti-detection protection enabled"
        AppLanguage.ARABIC -> "تم تفعيل حماية مكافحة الكشف"
        AppLanguage.PORTUGUESE -> "Proteção anti-detecção ativada"
        AppLanguage.SPANISH -> "Protección anti-detección activada"
        AppLanguage.FRENCH -> "Protection anti-détection activée"
        AppLanguage.GERMAN -> "Anti-Erkennungsschutz aktiviert"
        AppLanguage.RUSSIAN -> "Защита от обнаружения включена"
        AppLanguage.JAPANESE -> "検出回避保護が有効です"
        AppLanguage.KOREAN -> "탐지 방지 보호가 활성화됨"
    }

    val isolationLevel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "隔离级别"
        AppLanguage.ENGLISH -> "Isolation Level"
        AppLanguage.ARABIC -> "مستوى العزل"
        AppLanguage.PORTUGUESE -> "Nível de Isolamento"
        AppLanguage.SPANISH -> "Nivel de Aislamiento"
        AppLanguage.FRENCH -> "Niveau d'Isolement"
        AppLanguage.GERMAN -> "Isolationsstufe"
        AppLanguage.RUSSIAN -> "Уровень изоляции"
        AppLanguage.JAPANESE -> "隔離レベル"
        AppLanguage.KOREAN -> "격리 수준"
    }

    val activateApp: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "激活应用"
        AppLanguage.ENGLISH -> "Activate App"
        AppLanguage.ARABIC -> "تفعيل التطبيق"
        AppLanguage.PORTUGUESE -> "Ativar App"
        AppLanguage.SPANISH -> "Activar App"
        AppLanguage.FRENCH -> "Activer l'App"
        AppLanguage.GERMAN -> "App aktivieren"
        AppLanguage.RUSSIAN -> "Активировать приложение"
        AppLanguage.JAPANESE -> "アプリを有効化"
        AppLanguage.KOREAN -> "앱 활성화"
    }

    val enterActivationCodeToContinue: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "请输入激活码以继续使用"
        AppLanguage.ENGLISH -> "Please enter activation code to continue"
        AppLanguage.ARABIC -> "يرجى إدخال رمز التفعيل للمتابعة"
        AppLanguage.PORTUGUESE -> "Insira o código de ativação para continuar"
        AppLanguage.SPANISH -> "Introduce el código de activación para continuar"
        AppLanguage.FRENCH -> "Veuillez saisir le code d'activation pour continuer"
        AppLanguage.GERMAN -> "Bitte Aktivierungscode eingeben, um fortzufahren"
        AppLanguage.RUSSIAN -> "Введите код активации, чтобы продолжить"
        AppLanguage.JAPANESE -> "続行するにはアクティベーションコードを入力してください"
        AppLanguage.KOREAN -> "계속하려면 활성화 코드를 입력하세요"
    }

    val activationCodeExample: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "支持 4-16 位字母和数字"
        AppLanguage.ENGLISH -> "4-16 alphanumeric characters"
        AppLanguage.ARABIC -> "4-16 حرفًا أو رقمًا"
        AppLanguage.PORTUGUESE -> "4-16 caracteres alfanuméricos"
        AppLanguage.SPANISH -> "4-16 caracteres alfanuméricos"
        AppLanguage.FRENCH -> "4-16 caractères alphanumériques"
        AppLanguage.GERMAN -> "4-16 alphanumerische Zeichen"
        AppLanguage.RUSSIAN -> "4-16 буквенно-цифровых символов"
        AppLanguage.JAPANESE -> "4-16文字の英数字"
        AppLanguage.KOREAN -> "4-16자 영숫자"
    }

    val activate: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "激活"
        AppLanguage.ENGLISH -> "Activate"
        AppLanguage.ARABIC -> "تفعيل"
        AppLanguage.PORTUGUESE -> "Ativar"
        AppLanguage.SPANISH -> "Activar"
        AppLanguage.FRENCH -> "Activer"
        AppLanguage.GERMAN -> "Aktivieren"
        AppLanguage.RUSSIAN -> "Активировать"
        AppLanguage.JAPANESE -> "有効化"
        AppLanguage.KOREAN -> "활성화"
    }

    val addActivationCode: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "添加激活码"
        AppLanguage.ENGLISH -> "Add Activation Code"
        AppLanguage.ARABIC -> "إضافة رمز التفعيل"
        AppLanguage.PORTUGUESE -> "Adicionar Código de Ativação"
        AppLanguage.SPANISH -> "Añadir Código de Activación"
        AppLanguage.FRENCH -> "Ajouter un Code d'Activation"
        AppLanguage.GERMAN -> "Aktivierungscode hinzufügen"
        AppLanguage.RUSSIAN -> "Добавить код активации"
        AppLanguage.JAPANESE -> "アクティベーションコードを追加"
        AppLanguage.KOREAN -> "활성화 코드 추가"
    }

    val useCustomCode: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "使用自定义激活码"
        AppLanguage.ENGLISH -> "Use Custom Code"
        AppLanguage.ARABIC -> "استخدام رمز مخصص"
        AppLanguage.PORTUGUESE -> "Usar Código Personalizado"
        AppLanguage.SPANISH -> "Usar Código Personalizado"
        AppLanguage.FRENCH -> "Utiliser un Code Personnalisé"
        AppLanguage.GERMAN -> "Benutzerdefinierten Code verwenden"
        AppLanguage.RUSSIAN -> "Использовать кастомный код"
        AppLanguage.JAPANESE -> "カスタムコードを使用"
        AppLanguage.KOREAN -> "커스텀 코드 사용"
    }

    val codeLength: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "激活码长度"
        AppLanguage.ENGLISH -> "Code Length"
        AppLanguage.ARABIC -> "طول الرمز"
        AppLanguage.PORTUGUESE -> "Comprimento do Código"
        AppLanguage.SPANISH -> "Longitud del Código"
        AppLanguage.FRENCH -> "Longueur du Code"
        AppLanguage.GERMAN -> "Codelänge"
        AppLanguage.RUSSIAN -> "Длина кода"
        AppLanguage.JAPANESE -> "コード長"
        AppLanguage.KOREAN -> "코드 길이"
    }

    val chars: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "位"
        AppLanguage.ENGLISH -> "chars"
        AppLanguage.ARABIC -> "حرف"
        AppLanguage.PORTUGUESE -> "caracteres"
        AppLanguage.SPANISH -> "caracteres"
        AppLanguage.FRENCH -> "caractères"
        AppLanguage.GERMAN -> "Zeichen"
        AppLanguage.RUSSIAN -> "символов"
        AppLanguage.JAPANESE -> "文字"
        AppLanguage.KOREAN -> "자"
    }

    val codeTooShort: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "激活码至少需要 4 个字符"
        AppLanguage.ENGLISH -> "Activation code must be at least 4 characters"
        AppLanguage.ARABIC -> "يجب أن يكون رمز التفعيل 4 أحرف على الأقل"
        AppLanguage.PORTUGUESE -> "O código de ativação deve ter pelo menos 4 caracteres"
        AppLanguage.SPANISH -> "El código de activación debe tener al menos 4 caracteres"
        AppLanguage.FRENCH -> "Le code d'activation doit comporter au moins 4 caractères"
        AppLanguage.GERMAN -> "Der Aktivierungscode muss mindestens 4 Zeichen lang sein"
        AppLanguage.RUSSIAN -> "Код активации должен содержать минимум 4 символа"
        AppLanguage.JAPANESE -> "アクティベーションコードは4文字以上必要です"
        AppLanguage.KOREAN -> "활성화 코드는 최소 4자 이상이어야 합니다"
    }

    val batchGeneratedNote: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "批量生成"
        AppLanguage.ENGLISH -> "Batch generated"
        AppLanguage.ARABIC -> "إنشاء دفعة"
        AppLanguage.PORTUGUESE -> "Gerado em lote"
        AppLanguage.SPANISH -> "Generado en lote"
        AppLanguage.FRENCH -> "Généré par lot"
        AppLanguage.GERMAN -> "Stapelweise generiert"
        AppLanguage.RUSSIAN -> "Сгенерировано пакетно"
        AppLanguage.JAPANESE -> "一括生成"
        AppLanguage.KOREAN -> "일괄 생성됨"
    }

    fun tooManyAttemptsWithCountdown(remaining: String): String = when (Strings.lang) {
        AppLanguage.CHINESE -> "尝试次数过多，请在 $remaining 后重试"
        AppLanguage.ENGLISH -> "Too many attempts. Try again in $remaining"
        AppLanguage.ARABIC -> "محاولات كثيرة جدًا. حاول مرة أخرى بعد $remaining"
        AppLanguage.PORTUGUESE -> "Muitas tentativas. Tente novamente em $remaining"
        AppLanguage.SPANISH -> "Demasiados intentos. Inténtalo de nuevo en $remaining"
        AppLanguage.FRENCH -> "Trop de tentatives. Réessayez dans $remaining"
        AppLanguage.GERMAN -> "Zu viele Versuche. Versuchen Sie es in $remaining erneut"
        AppLanguage.RUSSIAN -> "Слишком много попыток. Попробуйте снова через $remaining"
        AppLanguage.JAPANESE -> "試行回数が多すぎます。$remaining 後に再試行してください"
        AppLanguage.KOREAN -> "시도 횟수가 너무 많습니다. $remaining 후에 다시 시도하세요"
    }

    val invalidTimeLimitConfig: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "无效的时间限制配置"
        AppLanguage.ENGLISH -> "Invalid time limit config"
        AppLanguage.ARABIC -> "تكوين حد زمني غير صالح"
        AppLanguage.PORTUGUESE -> "Configuração de limite de tempo inválida"
        AppLanguage.SPANISH -> "Configuración de límite de tiempo no válida"
        AppLanguage.FRENCH -> "Configuration de limite de temps invalide"
        AppLanguage.GERMAN -> "Ungültige Zeitlimit-Konfiguration"
        AppLanguage.RUSSIAN -> "Неверная конфигурация ограничения времени"
        AppLanguage.JAPANESE -> "無効な時間制限設定"
        AppLanguage.KOREAN -> "잘못된 시간 제한 설정"
    }

    val invalidUsageLimitConfig: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "无效的使用次数配置"
        AppLanguage.ENGLISH -> "Invalid usage limit config"
        AppLanguage.ARABIC -> "تكوين حد الاستخدام غير صالح"
        AppLanguage.PORTUGUESE -> "Configuração de limite de uso inválida"
        AppLanguage.SPANISH -> "Configuración de límite de uso no válida"
        AppLanguage.FRENCH -> "Configuration de limite d'utilisation invalide"
        AppLanguage.GERMAN -> "Ungültige Nutzungslimit-Konfiguration"
        AppLanguage.RUSSIAN -> "Неверная конфигурация ограничения использования"
        AppLanguage.JAPANESE -> "無効な使用回数制限設定"
        AppLanguage.KOREAN -> "잘못된 사용 횟수 제한 설정"
    }

    val validityDays: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "有效期（天）"
        AppLanguage.ENGLISH -> "Validity (days)"
        AppLanguage.ARABIC -> "الصلاحية (أيام)"
        AppLanguage.PORTUGUESE -> "Validade (dias)"
        AppLanguage.SPANISH -> "Validez (días)"
        AppLanguage.FRENCH -> "Validité (jours)"
        AppLanguage.GERMAN -> "Gültigkeit (Tage)"
        AppLanguage.RUSSIAN -> "Срок действия (дни)"
        AppLanguage.JAPANESE -> "有効期間（日）"
        AppLanguage.KOREAN -> "유효기간 (일)"
    }
    val activationCodeExpiryDays: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "截止天数（自生成日起，留空永久）"
        AppLanguage.ENGLISH -> "Expires in (days from creation, blank = never)"
        AppLanguage.ARABIC -> "ينتهي بعد (أيام من الإنشاء، فارغ = أبدًا)"
        AppLanguage.PORTUGUESE -> "Expira em (dias desde a criação, vazio = nunca)"
        AppLanguage.SPANISH -> "Caduca en (días desde la creación, vacío = nunca)"
        AppLanguage.FRENCH -> "Expire dans (jours depuis la création, vide = jamais)"
        AppLanguage.GERMAN -> "Läuft ab in (Tage ab Erstellung, leer = nie)"
        AppLanguage.RUSSIAN -> "Истекает через (дней с создания, пусто = никогда)"
        AppLanguage.JAPANESE -> "有効期限（生成日からの日数、空欄は無期限）"
        AppLanguage.KOREAN -> "만료 기간(생성일부터 일수, 비우면 무제한)"
    }
    val activationCodeValidUntil: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "截止 %s"
        AppLanguage.ENGLISH -> "Until %s"
        AppLanguage.ARABIC -> "حتى %s"
        AppLanguage.PORTUGUESE -> "Até %s"
        AppLanguage.SPANISH -> "Hasta %s"
        AppLanguage.FRENCH -> "Jusqu'au %s"
        AppLanguage.GERMAN -> "Bis %s"
        AppLanguage.RUSSIAN -> "До %s"
        AppLanguage.JAPANESE -> "期限 %s"
        AppLanguage.KOREAN -> "만료 %s"
    }

    val usageCount: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "使用次数"
        AppLanguage.ENGLISH -> "Usage Count"
        AppLanguage.ARABIC -> "عدد الاستخدامات"
        AppLanguage.PORTUGUESE -> "Contagem de Uso"
        AppLanguage.SPANISH -> "Conteo de Uso"
        AppLanguage.FRENCH -> "Nombre d'Utilisations"
        AppLanguage.GERMAN -> "Nutzungsanzahl"
        AppLanguage.RUSSIAN -> "Количество использований"
        AppLanguage.JAPANESE -> "使用回数"
        AppLanguage.KOREAN -> "사용 횟수"
    }

    val noteOptional: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "备注（可选）"
        AppLanguage.ENGLISH -> "Note (optional)"
        AppLanguage.ARABIC -> "ملاحظة (اختياري)"
        AppLanguage.PORTUGUESE -> "Nota (opcional)"
        AppLanguage.SPANISH -> "Nota (opcional)"
        AppLanguage.FRENCH -> "Note (facultatif)"
        AppLanguage.GERMAN -> "Notiz (optional)"
        AppLanguage.RUSSIAN -> "Заметка (необязательно)"
        AppLanguage.JAPANESE -> "メモ（任意）"
        AppLanguage.KOREAN -> "메모 (선택)"
    }

    val vipUserOnly: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "例如：VIP用户专用"
        AppLanguage.ENGLISH -> "e.g.: VIP users only"
        AppLanguage.ARABIC -> "مثال: لمستخدمي VIP فقط"
        AppLanguage.PORTUGUESE -> "ex.: apenas usuários VIP"
        AppLanguage.SPANISH -> "ej.: solo usuarios VIP"
        AppLanguage.FRENCH -> "ex. : utilisateurs VIP uniquement"
        AppLanguage.GERMAN -> "z. B.: nur VIP-Nutzer"
        AppLanguage.RUSSIAN -> "напр.: только для VIP-пользователей"
        AppLanguage.JAPANESE -> "例：VIPユーザー限定"
        AppLanguage.KOREAN -> "예: VIP 사용자 전용"
    }

    val requireEveryLaunch: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "每次启动都需要验证"
        AppLanguage.ENGLISH -> "Require verification every launch"
        AppLanguage.ARABIC -> "يتطلب التحقق في كل تشغيل"
        AppLanguage.PORTUGUESE -> "Exigir verificação a cada inicialização"
        AppLanguage.SPANISH -> "Requerir verificación en cada inicio"
        AppLanguage.FRENCH -> "Exiger une vérification à chaque lancement"
        AppLanguage.GERMAN -> "Bei jedem Start Verifizierung verlangen"
        AppLanguage.RUSSIAN -> "Требовать проверку при каждом запуске"
        AppLanguage.JAPANESE -> "起動のたびに認証を要求"
        AppLanguage.KOREAN -> "실행 시마다 인증 요구"
    }

    val customDialogText: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自定义对话框文本"
        AppLanguage.ENGLISH -> "Custom Dialog Text"
        AppLanguage.ARABIC -> "نص الحوار المخصص"
        AppLanguage.PORTUGUESE -> "Texto de Diálogo Personalizado"
        AppLanguage.SPANISH -> "Texto de Diálogo Personalizado"
        AppLanguage.FRENCH -> "Texte de Dialogue Personnalisé"
        AppLanguage.GERMAN -> "Benutzerdefinierter Dialogtext"
        AppLanguage.RUSSIAN -> "Пользовательский текст диалога"
        AppLanguage.JAPANESE -> "カスタムダイアログテキスト"
        AppLanguage.KOREAN -> "커스텀 대화상자 텍스트"
    }

    val customDialogTextHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自定义激活对话框中显示的文本，留空使用默认文本"
        AppLanguage.ENGLISH -> "Customize text shown in activation dialog, leave empty for default"
        AppLanguage.ARABIC -> "تخصيص النص المعروض في حوار التفعيل، اتركه فارغًا للافتراضي"
        AppLanguage.PORTUGUESE -> "Personalize o texto exibido no diálogo de ativação, deixe vazio para o padrão"
        AppLanguage.SPANISH -> "Personaliza el texto mostrado en el diálogo de activación, déjalo vacío para el predeterminado"
        AppLanguage.FRENCH -> "Personnalisez le texte affiché dans le dialogue d'activation, laissez vide pour le défaut"
        AppLanguage.GERMAN -> "Passen Sie den im Aktivierungsdialog angezeigten Text an, leer für Standard"
        AppLanguage.RUSSIAN -> "Настройте текст в диалоге активации, оставьте пустым для значения по умолчанию"
        AppLanguage.JAPANESE -> "アクティベーションダイアログのテキストをカスタマイズ。空欄でデフォルト"
        AppLanguage.KOREAN -> "활성화 대화상자에 표시될 텍스트를 커스터마이즈. 비워두면 기본값"
    }

    val dialogTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "标题"
        AppLanguage.ENGLISH -> "Title"
        AppLanguage.ARABIC -> "العنوان"
        AppLanguage.PORTUGUESE -> "Título"
        AppLanguage.SPANISH -> "Título"
        AppLanguage.FRENCH -> "Titre"
        AppLanguage.GERMAN -> "Titel"
        AppLanguage.RUSSIAN -> "Заголовок"
        AppLanguage.JAPANESE -> "タイトル"
        AppLanguage.KOREAN -> "제목"
    }

    val dialogTitleHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "默认：激活应用"
        AppLanguage.ENGLISH -> "Default: Activate App"
        AppLanguage.ARABIC -> "الافتراضي: تفعيل التطبيق"
        AppLanguage.PORTUGUESE -> "Padrão: Ativar App"
        AppLanguage.SPANISH -> "Predeterminado: Activar App"
        AppLanguage.FRENCH -> "Défaut : Activer l'App"
        AppLanguage.GERMAN -> "Standard: App aktivieren"
        AppLanguage.RUSSIAN -> "По умолчанию: Активировать приложение"
        AppLanguage.JAPANESE -> "デフォルト：アプリを有効化"
        AppLanguage.KOREAN -> "기본값: 앱 활성화"
    }

    val dialogSubtitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "副标题"
        AppLanguage.ENGLISH -> "Subtitle"
        AppLanguage.ARABIC -> "العنوان الفرعي"
        AppLanguage.PORTUGUESE -> "Subtítulo"
        AppLanguage.SPANISH -> "Subtítulo"
        AppLanguage.FRENCH -> "Sous-titre"
        AppLanguage.GERMAN -> "Untertitel"
        AppLanguage.RUSSIAN -> "Подзаголовок"
        AppLanguage.JAPANESE -> "サブタイトル"
        AppLanguage.KOREAN -> "부제목"
    }

    val dialogSubtitleHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "默认：请输入激活码以继续使用"
        AppLanguage.ENGLISH -> "Default: Please enter activation code to continue"
        AppLanguage.ARABIC -> "الافتراضي: يرجى إدخال رمز التفعيل للمتابعة"
        AppLanguage.PORTUGUESE -> "Padrão: Insira o código de ativação para continuar"
        AppLanguage.SPANISH -> "Predeterminado: Introduce el código de activación para continuar"
        AppLanguage.FRENCH -> "Défaut : Veuillez saisir le code d'activation pour continuer"
        AppLanguage.GERMAN -> "Standard: Bitte Aktivierungscode eingeben, um fortzufahren"
        AppLanguage.RUSSIAN -> "По умолчанию: Введите код активации, чтобы продолжить"
        AppLanguage.JAPANESE -> "デフォルト：続行するにはアクティベーションコードを入力してください"
        AppLanguage.KOREAN -> "기본값: 계속하려면 활성화 코드를 입력하세요"
    }

    val dialogInputLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "输入框标签"
        AppLanguage.ENGLISH -> "Input Label"
        AppLanguage.ARABIC -> "علامة الإدخال"
        AppLanguage.PORTUGUESE -> "Rótulo de Entrada"
        AppLanguage.SPANISH -> "Etiqueta de Entrada"
        AppLanguage.FRENCH -> "Étiquette de Saisie"
        AppLanguage.GERMAN -> "Eingabebezeichnung"
        AppLanguage.RUSSIAN -> "Метка ввода"
        AppLanguage.JAPANESE -> "入力ラベル"
        AppLanguage.KOREAN -> "입력 라벨"
    }

    val dialogInputLabelHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "默认：激活码"
        AppLanguage.ENGLISH -> "Default: Activation Code"
        AppLanguage.ARABIC -> "الافتراضي: رمز التفعيل"
        AppLanguage.PORTUGUESE -> "Padrão: Código de Ativação"
        AppLanguage.SPANISH -> "Predeterminado: Código de Activación"
        AppLanguage.FRENCH -> "Défaut : Code d'Activation"
        AppLanguage.GERMAN -> "Standard: Aktivierungscode"
        AppLanguage.RUSSIAN -> "По умолчанию: Код активации"
        AppLanguage.JAPANESE -> "デフォルト：アクティベーションコード"
        AppLanguage.KOREAN -> "기본값: 활성화 코드"
    }

    val dialogButtonText: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "按钮文字"
        AppLanguage.ENGLISH -> "Button Text"
        AppLanguage.ARABIC -> "نص الزر"
        AppLanguage.PORTUGUESE -> "Texto do Botão"
        AppLanguage.SPANISH -> "Texto del Botón"
        AppLanguage.FRENCH -> "Texte du Bouton"
        AppLanguage.GERMAN -> "Buttontext"
        AppLanguage.RUSSIAN -> "Текст кнопки"
        AppLanguage.JAPANESE -> "ボタンテキスト"
        AppLanguage.KOREAN -> "버튼 텍스트"
    }

    val dialogButtonTextHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "默认：激活"
        AppLanguage.ENGLISH -> "Default: Activate"
        AppLanguage.ARABIC -> "الافتراضي: تفعيل"
        AppLanguage.PORTUGUESE -> "Padrão: Ativar"
        AppLanguage.SPANISH -> "Predeterminado: Activar"
        AppLanguage.FRENCH -> "Défaut : Activer"
        AppLanguage.GERMAN -> "Standard: Aktivieren"
        AppLanguage.RUSSIAN -> "По умолчанию: Активировать"
        AppLanguage.JAPANESE -> "デフォルト：有効化"
        AppLanguage.KOREAN -> "기본값: 활성화"
    }

    val requireEveryLaunchHintOn: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "每次启动重新验证上次的激活卡，失效或被移除才需重新输入"
        AppLanguage.ENGLISH -> "Re-verify the last card on every launch; re-entry only if it lapsed or was revoked"
        AppLanguage.ARABIC -> "إعادة التحقق من آخر بطاقة عند كل تشغيل؛ لا يُطلب الإدخال إلا إذا انتهت أو أُلغيت"
        AppLanguage.PORTUGUESE -> "Reverificar o último cartão a cada abertura; só pede o código se expirar ou for revogado"
        AppLanguage.SPANISH -> "Reverificar la última tarjeta en cada inicio; solo pide el código si caducó o fue revocada"
        AppLanguage.FRENCH -> "Revérifie la dernière carte à chaque lancement ; ne redemande le code que si elle a expiré ou été révoquée"
        AppLanguage.GERMAN -> "Letzte Karte bei jedem Start erneut prüfen; Eingabe nur nötig, wenn sie abgelaufen oder widerrufen ist"
        AppLanguage.RUSSIAN -> "Перепроверять последнюю карту при каждом запуске; код запрашивается, только если она истекла или отозвана"
        AppLanguage.JAPANESE -> "起動ごとに前回のカードを再検証。失効・削除時のみ再入力が必要"
        AppLanguage.KOREAN -> "매번 실행 시 이전 카드를 재검증. 만료·폐기된 경우에만 다시 입력"
    }

    val requireEveryLaunchHintOff: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "激活一次后永久有效"
        AppLanguage.ENGLISH -> "Valid permanently after one activation"
        AppLanguage.ARABIC -> "صالح بشكل دائم بعد تفعيل واحد"
        AppLanguage.PORTUGUESE -> "Válido permanentemente após uma ativação"
        AppLanguage.SPANISH -> "Válido permanentemente tras una activación"
        AppLanguage.FRENCH -> "Valide permanently après une activation"
        AppLanguage.GERMAN -> "Nach einmaliger Aktivierung dauerhaft gültig"
        AppLanguage.RUSSIAN -> "Действителен навсегда после одной активации"
        AppLanguage.JAPANESE -> "一度の有効化で永続的に有効"
        AppLanguage.KOREAN -> "한 번 활성화하면 영구 유효"
    }

    val selectColor: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择颜色"
        AppLanguage.ENGLISH -> "Select Color"
        AppLanguage.ARABIC -> "اختيار اللون"
        AppLanguage.PORTUGUESE -> "Selecionar Cor"
        AppLanguage.SPANISH -> "Seleccionar Color"
        AppLanguage.FRENCH -> "Sélectionner Couleur"
        AppLanguage.GERMAN -> "Farbe auswählen"
        AppLanguage.RUSSIAN -> "Выбрать цвет"
        AppLanguage.JAPANESE -> "色を選択"
        AppLanguage.KOREAN -> "색상 선택"
    }

    val hexColor: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "十六进制颜色"
        AppLanguage.ENGLISH -> "Hex Color"
        AppLanguage.ARABIC -> "لون سداسي عشري"
        AppLanguage.PORTUGUESE -> "Cor Hex"
        AppLanguage.SPANISH -> "Color Hex"
        AppLanguage.FRENCH -> "Couleur Hex"
        AppLanguage.GERMAN -> "Hex-Farbe"
        AppLanguage.RUSSIAN -> "Hex-цвет"
        AppLanguage.JAPANESE -> "Hexカラー"
        AppLanguage.KOREAN -> "Hex 색상"
    }

    val hexColorHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "如: FF5722 或 80FF5722"
        AppLanguage.ENGLISH -> "e.g.: FF5722 or 80FF5722"
        AppLanguage.ARABIC -> "مثال: FF5722 أو 80FF5722"
        AppLanguage.PORTUGUESE -> "ex.: FF5722 ou 80FF5722"
        AppLanguage.SPANISH -> "ej.: FF5722 o 80FF5722"
        AppLanguage.FRENCH -> "ex. : FF5722 ou 80FF5722"
        AppLanguage.GERMAN -> "z. B.: FF5722 oder 80FF5722"
        AppLanguage.RUSSIAN -> "напр.: FF5722 или 80FF5722"
        AppLanguage.JAPANESE -> "例: FF5722 または 80FF5722"
        AppLanguage.KOREAN -> "예: FF5722 또는 80FF5722"
    }

    val onlineMusic: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "在线音乐"
        AppLanguage.ENGLISH -> "Online Music"
        AppLanguage.ARABIC -> "موسيقى عبر الإنترنت"
        AppLanguage.PORTUGUESE -> "Música Online"
        AppLanguage.SPANISH -> "Música Online"
        AppLanguage.FRENCH -> "Musique en Ligne"
        AppLanguage.GERMAN -> "Online-Musik"
        AppLanguage.RUSSIAN -> "Музыка онлайн"
        AppLanguage.JAPANESE -> "オンライン音楽"
        AppLanguage.KOREAN -> "온라인 음악"
    }

    val searchSongName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "搜索歌曲名称"
        AppLanguage.ENGLISH -> "Search song name"
        AppLanguage.ARABIC -> "البحث عن اسم الأغنية"
        AppLanguage.PORTUGUESE -> "Buscar nome da música"
        AppLanguage.SPANISH -> "Buscar nombre de canción"
        AppLanguage.FRENCH -> "Rechercher nom de chanson"
        AppLanguage.GERMAN -> "Songnamen suchen"
        AppLanguage.RUSSIAN -> "Поиск названия песни"
        AppLanguage.JAPANESE -> "曲名で検索"
        AppLanguage.KOREAN -> "곡명 검색"
    }

    val musicChannel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "音乐渠道"
        AppLanguage.ENGLISH -> "Music Channel"
        AppLanguage.ARABIC -> "قناة الموسيقى"
        AppLanguage.PORTUGUESE -> "Canal de Música"
        AppLanguage.SPANISH -> "Canal de Música"
        AppLanguage.FRENCH -> "Canal Musical"
        AppLanguage.GERMAN -> "Musik-Kanal"
        AppLanguage.RUSSIAN -> "Музыкальный канал"
        AppLanguage.JAPANESE -> "音楽チャンネル"
        AppLanguage.KOREAN -> "음악 채널"
    }

    val testAllChannels: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "测试全部"
        AppLanguage.ENGLISH -> "Test All"
        AppLanguage.ARABIC -> "اختبار الكل"
        AppLanguage.PORTUGUESE -> "Testar Todos"
        AppLanguage.SPANISH -> "Probar Todos"
        AppLanguage.FRENCH -> "Tout Tester"
        AppLanguage.GERMAN -> "Alle testen"
        AppLanguage.RUSSIAN -> "Проверить все"
        AppLanguage.JAPANESE -> "すべてテスト"
        AppLanguage.KOREAN -> "전체 테스트"
    }

    val channelAvailable: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "可用"
        AppLanguage.ENGLISH -> "Available"
        AppLanguage.ARABIC -> "متاح"
        AppLanguage.PORTUGUESE -> "Disponível"
        AppLanguage.SPANISH -> "Disponible"
        AppLanguage.FRENCH -> "Disponible"
        AppLanguage.GERMAN -> "Verfügbar"
        AppLanguage.RUSSIAN -> "Доступен"
        AppLanguage.JAPANESE -> "利用可能"
        AppLanguage.KOREAN -> "사용 가능"
    }

    val channelUnavailable: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "不可用"
        AppLanguage.ENGLISH -> "Unavailable"
        AppLanguage.ARABIC -> "غير متاح"
        AppLanguage.PORTUGUESE -> "Indisponível"
        AppLanguage.SPANISH -> "No disponible"
        AppLanguage.FRENCH -> "Indisponible"
        AppLanguage.GERMAN -> "Nicht verfügbar"
        AppLanguage.RUSSIAN -> "Недоступен"
        AppLanguage.JAPANESE -> "利用不可"
        AppLanguage.KOREAN -> "사용 불가"
    }

    val recommendedLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "推荐"
        AppLanguage.ENGLISH -> "REC"
        AppLanguage.ARABIC -> "موصى به"
        AppLanguage.PORTUGUESE -> "RECOM."
        AppLanguage.SPANISH -> "RECOM."
        AppLanguage.FRENCH -> "REC."
        AppLanguage.GERMAN -> "EMP."
        AppLanguage.RUSSIAN -> "РЕК"
        AppLanguage.JAPANESE -> "推奨"
        AppLanguage.KOREAN -> "추천"
    }

    val channelTesting: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "测试中..."
        AppLanguage.ENGLISH -> "Testing..."
        AppLanguage.ARABIC -> "جاري الاختبار..."
        AppLanguage.PORTUGUESE -> "Testando..."
        AppLanguage.SPANISH -> "Probando..."
        AppLanguage.FRENCH -> "Test en cours..."
        AppLanguage.GERMAN -> "Teste..."
        AppLanguage.RUSSIAN -> "Проверка..."
        AppLanguage.JAPANESE -> "テスト中..."
        AppLanguage.KOREAN -> "테스트 중..."
    }

    val channelUntested: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "未测试"
        AppLanguage.ENGLISH -> "Not tested"
        AppLanguage.ARABIC -> "لم يتم الاختبار"
        AppLanguage.PORTUGUESE -> "Não testado"
        AppLanguage.SPANISH -> "No probado"
        AppLanguage.FRENCH -> "Non testé"
        AppLanguage.GERMAN -> "Nicht getestet"
        AppLanguage.RUSSIAN -> "Не проверен"
        AppLanguage.JAPANESE -> "未テスト"
        AppLanguage.KOREAN -> "미테스트"
    }

    val searchOnlineMusic: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "搜索在线音乐"
        AppLanguage.ENGLISH -> "Search online music"
        AppLanguage.ARABIC -> "البحث عن الموسيقى عبر الإنترنت"
        AppLanguage.PORTUGUESE -> "Buscar música online"
        AppLanguage.SPANISH -> "Buscar música online"
        AppLanguage.FRENCH -> "Rechercher musique en ligne"
        AppLanguage.GERMAN -> "Online-Musik suchen"
        AppLanguage.RUSSIAN -> "Поиск музыки онлайн"
        AppLanguage.JAPANESE -> "オンライン音楽を検索"
        AppLanguage.KOREAN -> "온라인 음악 검색"
    }

    val noMusicResults: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "没有搜索结果"
        AppLanguage.ENGLISH -> "No results found"
        AppLanguage.ARABIC -> "لا توجد نتائج"
        AppLanguage.PORTUGUESE -> "Nenhum resultado encontrado"
        AppLanguage.SPANISH -> "Sin resultados"
        AppLanguage.FRENCH -> "Aucun résultat trouvé"
        AppLanguage.GERMAN -> "Keine Ergebnisse gefunden"
        AppLanguage.RUSSIAN -> "Результатов не найдено"
        AppLanguage.JAPANESE -> "結果が見つかりません"
        AppLanguage.KOREAN -> "검색 결과 없음"
    }

    val previewListen: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "试听"
        AppLanguage.ENGLISH -> "Preview"
        AppLanguage.ARABIC -> "معاينة"
        AppLanguage.PORTUGUESE -> "Pré-ouvir"
        AppLanguage.SPANISH -> "Pre-escuchar"
        AppLanguage.FRENCH -> "Pré-écouter"
        AppLanguage.GERMAN -> "Vorhören"
        AppLanguage.RUSSIAN -> "Прослушать"
        AppLanguage.JAPANESE -> "試聴"
        AppLanguage.KOREAN -> "미리듣기"
    }

    val downloadToBgm: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下载为BGM"
        AppLanguage.ENGLISH -> "Download as BGM"
        AppLanguage.ARABIC -> "تنزيل كـ BGM"
        AppLanguage.PORTUGUESE -> "Baixar como BGM"
        AppLanguage.SPANISH -> "Descargar como BGM"
        AppLanguage.FRENCH -> "Télécharger comme BGM"
        AppLanguage.GERMAN -> "Als BGM herunterladen"
        AppLanguage.RUSSIAN -> "Скачать как BGM"
        AppLanguage.JAPANESE -> "BGMとしてダウンロード"
        AppLanguage.KOREAN -> "BGM으로 다운로드"
    }

    val downloadSuccess: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下载成功"
        AppLanguage.ENGLISH -> "Download successful"
        AppLanguage.ARABIC -> "تم التنزيل بنجاح"
        AppLanguage.PORTUGUESE -> "Download bem-sucedido"
        AppLanguage.SPANISH -> "Descarga exitosa"
        AppLanguage.FRENCH -> "Téléchargement réussi"
        AppLanguage.GERMAN -> "Download erfolgreich"
        AppLanguage.RUSSIAN -> "Загрузка успешна"
        AppLanguage.JAPANESE -> "ダウンロード成功"
        AppLanguage.KOREAN -> "다운로드 성공"
    }

    val searchFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "搜索失败"
        AppLanguage.ENGLISH -> "Search failed"
        AppLanguage.ARABIC -> "فشل البحث"
        AppLanguage.PORTUGUESE -> "Falha na busca"
        AppLanguage.SPANISH -> "Búsqueda fallida"
        AppLanguage.FRENCH -> "Recherche échouée"
        AppLanguage.GERMAN -> "Suche fehlgeschlagen"
        AppLanguage.RUSSIAN -> "Поиск не удался"
        AppLanguage.JAPANESE -> "検索失敗"
        AppLanguage.KOREAN -> "검색 실패"
    }

    val results: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "个结果"
        AppLanguage.ENGLISH -> "results"
        AppLanguage.ARABIC -> "نتائج"
        AppLanguage.PORTUGUESE -> "resultados"
        AppLanguage.SPANISH -> "resultados"
        AppLanguage.FRENCH -> "résultats"
        AppLanguage.GERMAN -> "Ergebnisse"
        AppLanguage.RUSSIAN -> "результатов"
        AppLanguage.JAPANESE -> "件"
        AppLanguage.KOREAN -> "개 결과"
    }

    val selectModel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择模型"
        AppLanguage.ENGLISH -> "Select Model"
        AppLanguage.ARABIC -> "اختيار النموذج"
        AppLanguage.PORTUGUESE -> "Selecionar Modelo"
        AppLanguage.SPANISH -> "Seleccionar Modelo"
        AppLanguage.FRENCH -> "Sélectionner Modèle"
        AppLanguage.GERMAN -> "Modell auswählen"
        AppLanguage.RUSSIAN -> "Выбрать модель"
        AppLanguage.JAPANESE -> "モデルを選択"
        AppLanguage.KOREAN -> "모델 선택"
    }

    val describeIcon: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "描述你想要的图标"
        AppLanguage.ENGLISH -> "Describe the icon you want"
        AppLanguage.ARABIC -> "صف الأيقونة التي تريدها"
        AppLanguage.PORTUGUESE -> "Descreva o ícone que você quer"
        AppLanguage.SPANISH -> "Describe el icono que quieres"
        AppLanguage.FRENCH -> "Décrivez l'icône que vous voulez"
        AppLanguage.GERMAN -> "Beschreiben Sie das Symbol, das Sie möchten"
        AppLanguage.RUSSIAN -> "Опишите иконку, которую хотите"
        AppLanguage.JAPANESE -> "欲しいアイコンを説明してください"
        AppLanguage.KOREAN -> "원하는 아이콘을 설명하세요"
    }

    val iconDescriptionExample: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "例如：一个蓝色渐变的音乐播放器图标"
        AppLanguage.ENGLISH -> "e.g.: A blue gradient music player icon"
        AppLanguage.ARABIC -> "مثال: أيقونة مشغل موسيقى بتدرج أزرق"
        AppLanguage.PORTUGUESE -> "ex.: Um ícone de player de música com gradiente azul"
        AppLanguage.SPANISH -> "ej.: Un icono de reproductor de música con degradado azul"
        AppLanguage.FRENCH -> "ex. : Une icône de lecteur de musique avec dégradé bleu"
        AppLanguage.GERMAN -> "z. B.: Ein Musikplayer-Symbol mit blauem Verlauf"
        AppLanguage.RUSSIAN -> "напр.: Иконка музыкального плеера с синим градиентом"
        AppLanguage.JAPANESE -> "例: 青いグラデーションの音楽プレーヤーアイコン"
        AppLanguage.KOREAN -> "예: 파란색 그라데이션 음악 플레이어 아이콘"
    }

    val generationResult: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "生成结果"
        AppLanguage.ENGLISH -> "Generation Result"
        AppLanguage.ARABIC -> "نتيجة التوليد"
        AppLanguage.PORTUGUESE -> "Resultado da Geração"
        AppLanguage.SPANISH -> "Resultado de Generación"
        AppLanguage.FRENCH -> "Résultat de Génération"
        AppLanguage.GERMAN -> "Generierungsergebnis"
        AppLanguage.RUSSIAN -> "Результат генерации"
        AppLanguage.JAPANESE -> "生成結果"
        AppLanguage.KOREAN -> "생성 결과"
    }

    val useThisIcon: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "使用此图标"
        AppLanguage.ENGLISH -> "Use This Icon"
        AppLanguage.ARABIC -> "استخدام هذه الأيقونة"
        AppLanguage.PORTUGUESE -> "Usar Este Ícone"
        AppLanguage.SPANISH -> "Usar Este Icono"
        AppLanguage.FRENCH -> "Utiliser cet icône"
        AppLanguage.GERMAN -> "Dieses Symbol verwenden"
        AppLanguage.RUSSIAN -> "Использовать эту иконку"
        AppLanguage.JAPANESE -> "このアイコンを使用"
        AppLanguage.KOREAN -> "이 아이콘 사용"
    }

    val saving: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "保存中..."
        AppLanguage.ENGLISH -> "Saving..."
        AppLanguage.ARABIC -> "جاري الحفظ..."
        AppLanguage.PORTUGUESE -> "Salvando..."
        AppLanguage.SPANISH -> "Guardando..."
        AppLanguage.FRENCH -> "Enregistrement..."
        AppLanguage.GERMAN -> "Speichern..."
        AppLanguage.RUSSIAN -> "Сохранение..."
        AppLanguage.JAPANESE -> "保存中..."
        AppLanguage.KOREAN -> "저장 중..."
    }

    val generateIcon: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "生成图标"
        AppLanguage.ENGLISH -> "Generate Icon"
        AppLanguage.ARABIC -> "توليد الأيقونة"
        AppLanguage.PORTUGUESE -> "Gerar Ícone"
        AppLanguage.SPANISH -> "Generar Icono"
        AppLanguage.FRENCH -> "Générer l'icône"
        AppLanguage.GERMAN -> "Symbol generieren"
        AppLanguage.RUSSIAN -> "Сгенерировать иконку"
        AppLanguage.JAPANESE -> "アイコンを生成"
        AppLanguage.KOREAN -> "아이콘 생성"
    }

    val regenerate: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "重新生成"
        AppLanguage.ENGLISH -> "Regenerate"
        AppLanguage.ARABIC -> "إعادة التوليد"
        AppLanguage.PORTUGUESE -> "Regenerar"
        AppLanguage.SPANISH -> "Regenerar"
        AppLanguage.FRENCH -> "Régénérer"
        AppLanguage.GERMAN -> "Neu generieren"
        AppLanguage.RUSSIAN -> "Перегенерировать"
        AppLanguage.JAPANESE -> "再生成"
        AppLanguage.KOREAN -> "다시 생성"
    }

    val backgroundType: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "背景类型"
        AppLanguage.ENGLISH -> "Background Type"
        AppLanguage.ARABIC -> "نوع الخلفية"
        AppLanguage.PORTUGUESE -> "Tipo de Fundo"
        AppLanguage.SPANISH -> "Tipo de Fondo"
        AppLanguage.FRENCH -> "Type d'arrière-plan"
        AppLanguage.GERMAN -> "Hintergrundtyp"
        AppLanguage.RUSSIAN -> "Тип фона"
        AppLanguage.JAPANESE -> "背景タイプ"
        AppLanguage.KOREAN -> "배경 유형"
    }

    val solidColor: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "纯色"
        AppLanguage.ENGLISH -> "Solid Color"
        AppLanguage.ARABIC -> "لون صلب"
        AppLanguage.PORTUGUESE -> "Cor Sólida"
        AppLanguage.SPANISH -> "Color Sólido"
        AppLanguage.FRENCH -> "Couleur unie"
        AppLanguage.GERMAN -> "Volltonfarbe"
        AppLanguage.RUSSIAN -> "Сплошной цвет"
        AppLanguage.JAPANESE -> "単色"
        AppLanguage.KOREAN -> "단색"
    }

    val cropStatusBarBg: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "裁剪状态栏背景"
        AppLanguage.ENGLISH -> "Crop Status Bar Background"
        AppLanguage.ARABIC -> "قص خلفية شريط الحالة"
        AppLanguage.PORTUGUESE -> "Cortar Fundo da Barra de Status"
        AppLanguage.SPANISH -> "Recortar Fondo de Barra de Estado"
        AppLanguage.FRENCH -> "Rogner l'arrière-plan de la barre d'état"
        AppLanguage.GERMAN -> "Statusleisten-Hintergrund zuschneiden"
        AppLanguage.RUSSIAN -> "Обрезать фон строки состояния"
        AppLanguage.JAPANESE -> "ステータスバー背景をクロップ"
        AppLanguage.KOREAN -> "상태 표시줄 배경 자르기"
    }

    val confirmCrop: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "确认裁剪"
        AppLanguage.ENGLISH -> "Confirm Crop"
        AppLanguage.ARABIC -> "تأكيد القص"
        AppLanguage.PORTUGUESE -> "Confirmar Corte"
        AppLanguage.SPANISH -> "Confirmar Recorte"
        AppLanguage.FRENCH -> "Confirmer le rognage"
        AppLanguage.GERMAN -> "Zuschnitt bestätigen"
        AppLanguage.RUSSIAN -> "Подтвердить обрезку"
        AppLanguage.JAPANESE -> "クロップを確認"
        AppLanguage.KOREAN -> "자르기 확인"
    }

    val nextStepTimeAlign: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下一步：时间对齐"
        AppLanguage.ENGLISH -> "Next: Time Alignment"
        AppLanguage.ARABIC -> "التالي: محاذاة الوقت"
        AppLanguage.PORTUGUESE -> "Próximo: Alinhamento de Tempo"
        AppLanguage.SPANISH -> "Siguiente: Alineación de Tiempo"
        AppLanguage.FRENCH -> "Suivant : Alignement du temps"
        AppLanguage.GERMAN -> "Weiter: Zeitausrichtung"
        AppLanguage.RUSSIAN -> "Далее: Выравнивание времени"
        AppLanguage.JAPANESE -> "次: タイムアライメント"
        AppLanguage.KOREAN -> "다음: 시간 정렬"
    }

    val tap: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "打点"
        AppLanguage.ENGLISH -> "Tap"
        AppLanguage.ARABIC -> "نقر"
        AppLanguage.PORTUGUESE -> "Tocar"
        AppLanguage.SPANISH -> "Tocar"
        AppLanguage.FRENCH -> "Taper"
        AppLanguage.GERMAN -> "Tippen"
        AppLanguage.RUSSIAN -> "Нажать"
        AppLanguage.JAPANESE -> "タップ"
        AppLanguage.KOREAN -> "탭"
    }

    val previousStep: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Previous"
        AppLanguage.ENGLISH -> "Previous"
        AppLanguage.ARABIC -> "السابق"
        AppLanguage.PORTUGUESE -> "Anterior"
        AppLanguage.SPANISH -> "Anterior"
        AppLanguage.FRENCH -> "Précédent"
        AppLanguage.GERMAN -> "Zurück"
        AppLanguage.RUSSIAN -> "Предыдущий"
        AppLanguage.JAPANESE -> "前へ"
        AppLanguage.KOREAN -> "이전"
    }

    val nextStep: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Next"
        AppLanguage.ENGLISH -> "Next"
        AppLanguage.ARABIC -> "التالي"
        AppLanguage.PORTUGUESE -> "Próximo"
        AppLanguage.SPANISH -> "Siguiente"
        AppLanguage.FRENCH -> "Suivant"
        AppLanguage.GERMAN -> "Weiter"
        AppLanguage.RUSSIAN -> "Далее"
        AppLanguage.JAPANESE -> "次へ"
        AppLanguage.KOREAN -> "다음"
    }

    val saveLrc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "保存 LRC"
        AppLanguage.ENGLISH -> "Save LRC"
        AppLanguage.ARABIC -> "حفظ LRC"
        AppLanguage.PORTUGUESE -> "Salvar LRC"
        AppLanguage.SPANISH -> "Guardar LRC"
        AppLanguage.FRENCH -> "Enregistrer LRC"
        AppLanguage.GERMAN -> "LRC speichern"
        AppLanguage.RUSSIAN -> "Сохранить LRC"
        AppLanguage.JAPANESE -> "LRCを保存"
        AppLanguage.KOREAN -> "LRC 저장"
    }

    val seconds: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "秒"
        AppLanguage.ENGLISH -> "seconds"
        AppLanguage.ARABIC -> "ثواني"
        AppLanguage.PORTUGUESE -> "segundos"
        AppLanguage.SPANISH -> "segundos"
        AppLanguage.FRENCH -> "secondes"
        AppLanguage.GERMAN -> "Sekunden"
        AppLanguage.RUSSIAN -> "секунд"
        AppLanguage.JAPANESE -> "秒"
        AppLanguage.KOREAN -> "초"
    }

    val downloadFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下载失败，请重试"
        AppLanguage.ENGLISH -> "Download failed, please retry"
        AppLanguage.ARABIC -> "فشل التحميل، يرجى المحاولة مرة أخرى"
        AppLanguage.PORTUGUESE -> "Falha no download, tente novamente"
        AppLanguage.SPANISH -> "Error de descarga, inténtelo de nuevo"
        AppLanguage.FRENCH -> "Échec du téléchargement, veuillez réessayer"
        AppLanguage.GERMAN -> "Download fehlgeschlagen, bitte erneut versuchen"
        AppLanguage.RUSSIAN -> "Ошибка загрузки, попробуйте снова"
        AppLanguage.JAPANESE -> "ダウンロード失敗、再試行してください"
        AppLanguage.KOREAN -> "다운로드 실패, 다시 시도하세요"
    }

    val aiGenerateIcon: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "AI 生成图标"
        AppLanguage.ENGLISH -> "AI Generate Icon"
        AppLanguage.ARABIC -> "توليد أيقونة AI"
        AppLanguage.PORTUGUESE -> "Gerar Ícone por IA"
        AppLanguage.SPANISH -> "Generar Icono con IA"
        AppLanguage.FRENCH -> "Générer l'icône par IA"
        AppLanguage.GERMAN -> "KI-Symbol generieren"
        AppLanguage.RUSSIAN -> "Сгенерировать иконку ИИ"
        AppLanguage.JAPANESE -> "AIでアイコン生成"
        AppLanguage.KOREAN -> "AI 아이콘 생성"
    }

    val noImageGenModel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "未找到支持图像生成的模型"
        AppLanguage.ENGLISH -> "No image generation model found"
        AppLanguage.ARABIC -> "لم يتم العثور على نموذج توليد الصور"
        AppLanguage.PORTUGUESE -> "Nenhum modelo de geração de imagem encontrado"
        AppLanguage.SPANISH -> "No se encontró modelo de generación de imágenes"
        AppLanguage.FRENCH -> "Aucun modèle de génération d'images trouvé"
        AppLanguage.GERMAN -> "Kein Bildgenerierungsmodell gefunden"
        AppLanguage.RUSSIAN -> "Модель генерации изображений не найдена"
        AppLanguage.JAPANESE -> "画像生成モデルが見つかりません"
        AppLanguage.KOREAN -> "이미지 생성 모델을 찾을 수 없습니다"
    }

    val addImageGenModelHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "请在「AI设置」中添加模型并标记「图像生成」能力"
        AppLanguage.ENGLISH -> "Please add a model in 'AI Settings' and mark 'Image Generation' capability"
        AppLanguage.ARABIC -> "يرجى إضافة نموذج في 'إعدادات AI' وتحديد قدرة 'توليد الصور'"
        AppLanguage.PORTUGUESE -> "Adicione um modelo em 'Configurações de IA' e marque a capacidade 'Geração de Imagem'"
        AppLanguage.SPANISH -> "Agregue un modelo en 'Ajustes de IA' y marque la capacidad 'Generación de Imágenes'"
        AppLanguage.FRENCH -> "Ajoutez un modèle dans « Paramètres IA » et marquez la capacité « Génération d'images »"
        AppLanguage.GERMAN -> "Bitte Modell in 'KI-Einstellungen' hinzufügen und 'Bildgenerierung'-Fähigkeit markieren"
        AppLanguage.RUSSIAN -> "Добавьте модель в «Настройки ИИ» и отметьте способность «Генерация изображений»"
        AppLanguage.JAPANESE -> "「AI設定」でモデルを追加し、「画像生成」機能をマークしてください"
        AppLanguage.KOREAN -> "'AI 설정'에서 모델을 추가하고 '이미지 생성' 기능을 표시하세요"
    }

    val referenceImages: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "参考图片（可选，最多3张）"
        AppLanguage.ENGLISH -> "Reference images (optional, max 3)"
        AppLanguage.ARABIC -> "صور مرجعية (اختياري، بحد أقصى 3)"
        AppLanguage.PORTUGUESE -> "Imagens de referência (opcional, máx. 3)"
        AppLanguage.SPANISH -> "Imágenes de referencia (opcional, máx. 3)"
        AppLanguage.FRENCH -> "Images de référence (facultatif, max 3)"
        AppLanguage.GERMAN -> "Referenzbilder (optional, max. 3)"
        AppLanguage.RUSSIAN -> "Референсные изображения (необязательно, макс. 3)"
        AppLanguage.JAPANESE -> "参照画像(任意、最大3枚)"
        AppLanguage.KOREAN -> "참조 이미지(선택, 최대 3장)"
    }

    val addImage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Add image"
        AppLanguage.ENGLISH -> "Add Image"
        AppLanguage.ARABIC -> "إضافة صورة"
        AppLanguage.PORTUGUESE -> "Adicionar Imagem"
        AppLanguage.SPANISH -> "Añadir Imagen"
        AppLanguage.FRENCH -> "Ajouter une image"
        AppLanguage.GERMAN -> "Bild hinzufügen"
        AppLanguage.RUSSIAN -> "Добавить изображение"
        AppLanguage.JAPANESE -> "画像を追加"
        AppLanguage.KOREAN -> "이미지 추가"
    }

    val generatedIcon: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "生成的图标"
        AppLanguage.ENGLISH -> "Generated Icon"
        AppLanguage.ARABIC -> "الأيقونة المولدة"
        AppLanguage.PORTUGUESE -> "Ícone Gerado"
        AppLanguage.SPANISH -> "Icono Generado"
        AppLanguage.FRENCH -> "Icône générée"
        AppLanguage.GERMAN -> "Generiertes Symbol"
        AppLanguage.RUSSIAN -> "Сгенерированная иконка"
        AppLanguage.JAPANESE -> "生成されたアイコン"
        AppLanguage.KOREAN -> "생성된 아이콘"
    }

    val presetColors: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "预设颜色"
        AppLanguage.ENGLISH -> "Preset Colors"
        AppLanguage.ARABIC -> "الألوان المسبقة"
        AppLanguage.PORTUGUESE -> "Cores Predefinidas"
        AppLanguage.SPANISH -> "Colores Predefinidos"
        AppLanguage.FRENCH -> "Couleurs prédéfinies"
        AppLanguage.GERMAN -> "Voreingestellte Farben"
        AppLanguage.RUSSIAN -> "Предустановленные цвета"
        AppLanguage.JAPANESE -> "プリセットカラー"
        AppLanguage.KOREAN -> "사전 설정 색상"
    }

    val customColor: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自定义颜色"
        AppLanguage.ENGLISH -> "Custom Color"
        AppLanguage.ARABIC -> "لون مخصص"
        AppLanguage.PORTUGUESE -> "Cor Personalizada"
        AppLanguage.SPANISH -> "Color Personalizado"
        AppLanguage.FRENCH -> "Couleur personnalisée"
        AppLanguage.GERMAN -> "Benutzerdefinierte Farbe"
        AppLanguage.RUSSIAN -> "Пользовательский цвет"
        AppLanguage.JAPANESE -> "カスタムカラー"
        AppLanguage.KOREAN -> "사용자 지정 색상"
    }

    val currentSelection: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "当前选择"
        AppLanguage.ENGLISH -> "Current Selection"
        AppLanguage.ARABIC -> "الاختيار الحالي"
        AppLanguage.PORTUGUESE -> "Seleção Atual"
        AppLanguage.SPANISH -> "Selección Actual"
        AppLanguage.FRENCH -> "Sélection actuelle"
        AppLanguage.GERMAN -> "Aktuelle Auswahl"
        AppLanguage.RUSSIAN -> "Текущий выбор"
        AppLanguage.JAPANESE -> "現在の選択"
        AppLanguage.KOREAN -> "현재 선택"
    }

    val hexColorFormat: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "6位(RGB)或8位(ARGB)十六进制"
        AppLanguage.ENGLISH -> "6-digit (RGB) or 8-digit (ARGB) hex"
        AppLanguage.ARABIC -> "سداسي عشري 6 أرقام (RGB) أو 8 أرقام (ARGB)"
        AppLanguage.PORTUGUESE -> "Hex 6 dígitos (RGB) ou 8 dígitos (ARGB)"
        AppLanguage.SPANISH -> "Hex 6 dígitos (RGB) u 8 dígitos (ARGB)"
        AppLanguage.FRENCH -> "Hex 6 chiffres (RGB) ou 8 chiffres (ARGB)"
        AppLanguage.GERMAN -> "6-stellig (RGB) oder 8-stellig (ARGB) Hex"
        AppLanguage.RUSSIAN -> "6-значный (RGB) или 8-значный (ARGB) hex"
        AppLanguage.JAPANESE -> "6桁(RGB)または8桁(ARGB)の16進数"
        AppLanguage.KOREAN -> "6자리(RGB) 또는 8자리(ARGB) 16진수"
    }

    val dragToSelectArea: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "上下拖动选择要截取的区域"
        AppLanguage.ENGLISH -> "Drag up/down to select crop area"
        AppLanguage.ARABIC -> "اسحب لأعلى/لأسفل لتحديد منطقة القص"
        AppLanguage.PORTUGUESE -> "Arraste para cima/baixo para selecionar a área de corte"
        AppLanguage.SPANISH -> "Arrastre arriba/abajo para seleccionar el área de recorte"
        AppLanguage.FRENCH -> "Glisser haut/bas pour sélectionner la zone de rognage"
        AppLanguage.GERMAN -> "Zum Auswählen des Zuschnittsbereichs nach oben/unten ziehen"
        AppLanguage.RUSSIAN -> "Тяните вверх/вниз для выбора области обрезки"
        AppLanguage.JAPANESE -> "上下にドラッグしてクロップ範囲を選択"
        AppLanguage.KOREAN -> "위/아래로 드래그하여 자르기 영역 선택"
    }

    val loadingImage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "加载图片中..."
        AppLanguage.ENGLISH -> "Loading image..."
        AppLanguage.ARABIC -> "جاري تحميل الصورة..."
        AppLanguage.PORTUGUESE -> "Carregando imagem..."
        AppLanguage.SPANISH -> "Cargando imagen..."
        AppLanguage.FRENCH -> "Chargement de l'image..."
        AppLanguage.GERMAN -> "Bild wird geladen..."
        AppLanguage.RUSSIAN -> "Загрузка изображения..."
        AppLanguage.JAPANESE -> "画像を読み込み中..."
        AppLanguage.KOREAN -> "이미지 로딩 중..."
    }

    val cropSize: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "裁剪尺寸"
        AppLanguage.ENGLISH -> "Crop Size"
        AppLanguage.ARABIC -> "حجم القص"
        AppLanguage.PORTUGUESE -> "Tamanho do Corte"
        AppLanguage.SPANISH -> "Tamaño de Recorte"
        AppLanguage.FRENCH -> "Taille de rognage"
        AppLanguage.GERMAN -> "Zuschnittsgröße"
        AppLanguage.RUSSIAN -> "Размер обрезки"
        AppLanguage.JAPANESE -> "クロップサイズ"
        AppLanguage.KOREAN -> "자르기 크기"
    }

    val originalSize: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "原图尺寸"
        AppLanguage.ENGLISH -> "Original Size"
        AppLanguage.ARABIC -> "الحجم الأصلي"
        AppLanguage.PORTUGUESE -> "Tamanho Original"
        AppLanguage.SPANISH -> "Tamaño Original"
        AppLanguage.FRENCH -> "Taille d'origine"
        AppLanguage.GERMAN -> "Originalgröße"
        AppLanguage.RUSSIAN -> "Оригинальный размер"
        AppLanguage.JAPANESE -> "元のサイズ"
        AppLanguage.KOREAN -> "원본 크기"
    }

    val statusBarHeight: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "状态栏高度"
        AppLanguage.ENGLISH -> "Status Bar Height"
        AppLanguage.ARABIC -> "ارتفاع شريط الحالة"
        AppLanguage.PORTUGUESE -> "Altura da Barra de Status"
        AppLanguage.SPANISH -> "Altura de la Barra de Estado"
        AppLanguage.FRENCH -> "Hauteur de la barre d'état"
        AppLanguage.GERMAN -> "Statusleistenhöhe"
        AppLanguage.RUSSIAN -> "Высота строки состояния"
        AppLanguage.JAPANESE -> "ステータスバーの高さ"
        AppLanguage.KOREAN -> "상태 표시줄 높이"
    }

    val restoreDefault: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "恢复默认"
        AppLanguage.ENGLISH -> "Restore Default"
        AppLanguage.ARABIC -> "استعادة الافتراضي"
        AppLanguage.PORTUGUESE -> "Restaurar Padrão"
        AppLanguage.SPANISH -> "Restaurar Predeterminado"
        AppLanguage.FRENCH -> "Restaurer par défaut"
        AppLanguage.GERMAN -> "Standard wiederherstellen"
        AppLanguage.RUSSIAN -> "Восстановить по умолчанию"
        AppLanguage.JAPANESE -> "デフォルトに戻す"
        AppLanguage.KOREAN -> "기본값 복원"
    }

    val statusBarPreview: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "状态栏预览"
        AppLanguage.ENGLISH -> "Status Bar Preview"
        AppLanguage.ARABIC -> "معاينة شريط الحالة"
        AppLanguage.PORTUGUESE -> "Pré-visualização da Barra de Status"
        AppLanguage.SPANISH -> "Vista Previa de Barra de Estado"
        AppLanguage.FRENCH -> "Aperçu de la barre d'état"
        AppLanguage.GERMAN -> "Statusleisten-Vorschau"
        AppLanguage.RUSSIAN -> "Предпросмотр строки состояния"
        AppLanguage.JAPANESE -> "ステータスバープレビュー"
        AppLanguage.KOREAN -> "상태 표시줄 미리보기"
    }

    val noImageSelected: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "未选择图片"
        AppLanguage.ENGLISH -> "No image selected"
        AppLanguage.ARABIC -> "لم يتم اختيار صورة"
        AppLanguage.PORTUGUESE -> "Nenhuma imagem selecionada"
        AppLanguage.SPANISH -> "Ninguna imagen seleccionada"
        AppLanguage.FRENCH -> "Aucune image sélectionnée"
        AppLanguage.GERMAN -> "Kein Bild ausgewählt"
        AppLanguage.RUSSIAN -> "Изображение не выбрано"
        AppLanguage.JAPANESE -> "画像が選択されていません"
        AppLanguage.KOREAN -> "선택된 이미지가 없습니다"
    }

    val backgroundColor: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "背景颜色"
        AppLanguage.ENGLISH -> "Background Color"
        AppLanguage.ARABIC -> "لون الخلفية"
        AppLanguage.PORTUGUESE -> "Cor de Fundo"
        AppLanguage.SPANISH -> "Color de Fondo"
        AppLanguage.FRENCH -> "Couleur d'arrière-plan"
        AppLanguage.GERMAN -> "Hintergrundfarbe"
        AppLanguage.RUSSIAN -> "Цвет фона"
        AppLanguage.JAPANESE -> "背景色"
        AppLanguage.KOREAN -> "배경색"
    }

    val followPageTop: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "跟随网页顶部"
        AppLanguage.ENGLISH -> "Follow Page Top"
        AppLanguage.ARABIC -> "اتباع أعلى الصفحة"
        AppLanguage.PORTUGUESE -> "Seguir Topo da Página"
        AppLanguage.SPANISH -> "Seguir Parte Superior de la Página"
        AppLanguage.FRENCH -> "Suivre le haut de la page"
        AppLanguage.GERMAN -> "Seitenoberseite folgen"
        AppLanguage.RUSSIAN -> "Следовать за верхом страницы"
        AppLanguage.JAPANESE -> "ページ上部に従う"
        AppLanguage.KOREAN -> "페이지 상단 따르기"
    }

    val selectBackgroundImage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择背景图片"
        AppLanguage.ENGLISH -> "Select Background Image"
        AppLanguage.ARABIC -> "اختيار صورة الخلفية"
        AppLanguage.PORTUGUESE -> "Selecionar Imagem de Fundo"
        AppLanguage.SPANISH -> "Seleccionar Imagen de Fondo"
        AppLanguage.FRENCH -> "Sélectionner l'image d'arrière-plan"
        AppLanguage.GERMAN -> "Hintergrundbild auswählen"
        AppLanguage.RUSSIAN -> "Выбрать фоновое изображение"
        AppLanguage.JAPANESE -> "背景画像を選択"
        AppLanguage.KOREAN -> "배경 이미지 선택"
    }

    val imageSelected: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已选择图片"
        AppLanguage.ENGLISH -> "Image Selected"
        AppLanguage.ARABIC -> "تم اختيار الصورة"
        AppLanguage.PORTUGUESE -> "Imagem Selecionada"
        AppLanguage.SPANISH -> "Imagen Seleccionada"
        AppLanguage.FRENCH -> "Image sélectionnée"
        AppLanguage.GERMAN -> "Bild ausgewählt"
        AppLanguage.RUSSIAN -> "Изображение выбрано"
        AppLanguage.JAPANESE -> "画像が選択されました"
        AppLanguage.KOREAN -> "이미지가 선택됨"
    }

    val clickToChangeOrClear: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "点击更换或清除"
        AppLanguage.ENGLISH -> "Click to change or clear"
        AppLanguage.ARABIC -> "انقر للتغيير أو المسح"
        AppLanguage.PORTUGUESE -> "Clique para alterar ou limpar"
        AppLanguage.SPANISH -> "Haga clic para cambiar o limpiar"
        AppLanguage.FRENCH -> "Cliquez pour changer ou effacer"
        AppLanguage.GERMAN -> "Klicken zum Ändern oder Löschen"
        AppLanguage.RUSSIAN -> "Нажмите для изменения или очистки"
        AppLanguage.JAPANESE -> "クリックして変更またはクリア"
        AppLanguage.KOREAN -> "클릭하여 변경 또는 지우기"
    }

    val changeImage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "更换图片"
        AppLanguage.ENGLISH -> "Change Image"
        AppLanguage.ARABIC -> "تغيير الصورة"
        AppLanguage.PORTUGUESE -> "Alterar Imagem"
        AppLanguage.SPANISH -> "Cambiar Imagen"
        AppLanguage.FRENCH -> "Changer l'image"
        AppLanguage.GERMAN -> "Bild ändern"
        AppLanguage.RUSSIAN -> "Изменить изображение"
        AppLanguage.JAPANESE -> "画像を変更"
        AppLanguage.KOREAN -> "이미지 변경"
    }

    val clearImage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "清除图片"
        AppLanguage.ENGLISH -> "Clear Image"
        AppLanguage.ARABIC -> "مسح الصورة"
        AppLanguage.PORTUGUESE -> "Limpar Imagem"
        AppLanguage.SPANISH -> "Limpiar Imagen"
        AppLanguage.FRENCH -> "Effacer l'image"
        AppLanguage.GERMAN -> "Bild löschen"
        AppLanguage.RUSSIAN -> "Очистить изображение"
        AppLanguage.JAPANESE -> "画像をクリア"
        AppLanguage.KOREAN -> "이미지 지우기"
    }

    val backgroundAlpha: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "背景透明度"
        AppLanguage.ENGLISH -> "Background Alpha"
        AppLanguage.ARABIC -> "شفافية الخلفية"
        AppLanguage.PORTUGUESE -> "Alfa de Fundo"
        AppLanguage.SPANISH -> "Alfa de Fondo"
        AppLanguage.FRENCH -> "Alpha d'arrière-plan"
        AppLanguage.GERMAN -> "Hintergrund-Alpha"
        AppLanguage.RUSSIAN -> "Альфа фона"
        AppLanguage.JAPANESE -> "背景アルファ"
        AppLanguage.KOREAN -> "배경 알파"
    }

    val transparent: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "透明"
        AppLanguage.ENGLISH -> "Transparent"
        AppLanguage.ARABIC -> "شفاف"
        AppLanguage.PORTUGUESE -> "Transparente"
        AppLanguage.SPANISH -> "Transparente"
        AppLanguage.FRENCH -> "Transparent"
        AppLanguage.GERMAN -> "Transparent"
        AppLanguage.RUSSIAN -> "Прозрачный"
        AppLanguage.JAPANESE -> "透明"
        AppLanguage.KOREAN -> "투명"
    }

    val opaque: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "不透明"
        AppLanguage.ENGLISH -> "Opaque"
        AppLanguage.ARABIC -> "معتم"
        AppLanguage.PORTUGUESE -> "Opaco"
        AppLanguage.SPANISH -> "Opaco"
        AppLanguage.FRENCH -> "Opaque"
        AppLanguage.GERMAN -> "Deckend"
        AppLanguage.RUSSIAN -> "Непрозрачный"
        AppLanguage.JAPANESE -> "不透明"
        AppLanguage.KOREAN -> "불투명"
    }

    val inputLyrics: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "输入歌词"
        AppLanguage.ENGLISH -> "Input Lyrics"
        AppLanguage.ARABIC -> "إدخال كلمات الأغنية"
        AppLanguage.PORTUGUESE -> "Inserir Letras"
        AppLanguage.SPANISH -> "Ingresar Letras"
        AppLanguage.FRENCH -> "Saisir les paroles"
        AppLanguage.GERMAN -> "Liedtext eingeben"
        AppLanguage.RUSSIAN -> "Ввести текст песни"
        AppLanguage.JAPANESE -> "歌詞を入力"
        AppLanguage.KOREAN -> "가사 입력"
    }

    val timeAlignment: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "时间对齐"
        AppLanguage.ENGLISH -> "Time Alignment"
        AppLanguage.ARABIC -> "محاذاة الوقت"
        AppLanguage.PORTUGUESE -> "Alinhamento de Tempo"
        AppLanguage.SPANISH -> "Alineación de Tiempo"
        AppLanguage.FRENCH -> "Alignement du temps"
        AppLanguage.GERMAN -> "Zeitausrichtung"
        AppLanguage.RUSSIAN -> "Выравнивание времени"
        AppLanguage.JAPANESE -> "タイムアライメント"
        AppLanguage.KOREAN -> "시간 정렬"
    }

    val previewConfirm: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "预览确认"
        AppLanguage.ENGLISH -> "Preview Confirm"
        AppLanguage.ARABIC -> "تأكيد المعاينة"
        AppLanguage.PORTUGUESE -> "Confirmar Pré-visualização"
        AppLanguage.SPANISH -> "Confirmar Vista Previa"
        AppLanguage.FRENCH -> "Confirmer l'aperçu"
        AppLanguage.GERMAN -> "Vorschau bestätigen"
        AppLanguage.RUSSIAN -> "Подтвердить предпросмотр"
        AppLanguage.JAPANESE -> "プレビュー確認"
        AppLanguage.KOREAN -> "미리보기 확인"
    }

    val duration: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "时长"
        AppLanguage.ENGLISH -> "Duration"
        AppLanguage.ARABIC -> "المدة"
        AppLanguage.PORTUGUESE -> "Duração"
        AppLanguage.SPANISH -> "Duración"
        AppLanguage.FRENCH -> "Durée"
        AppLanguage.GERMAN -> "Dauer"
        AppLanguage.RUSSIAN -> "Длительность"
        AppLanguage.JAPANESE -> "再生時間"
        AppLanguage.KOREAN -> "재생 시간"
    }

    val inputLyricsHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "请输入歌词文本，每行一句："
        AppLanguage.ENGLISH -> "Please enter lyrics text, one line per sentence:"
        AppLanguage.ARABIC -> "يرجى إدخال نص كلمات الأغنية، سطر واحد لكل جملة:"
        AppLanguage.PORTUGUESE -> "Insira o texto das letras, uma linha por frase:"
        AppLanguage.SPANISH -> "Ingrese el texto de las letras, una línea por frase:"
        AppLanguage.FRENCH -> "Veuillez saisir le texte des paroles, une ligne par phrase :"
        AppLanguage.GERMAN -> "Bitte Liedtext eingeben, eine Zeile pro Satz:"
        AppLanguage.RUSSIAN -> "Введите текст песни, по одной строке на предложение:"
        AppLanguage.JAPANESE -> "歌詞テキストを入力してください、1文1行:"
        AppLanguage.KOREAN -> "가사 텍스트를 입력하세요, 한 문장당 한 줄:"
    }

    val lyricsPlaceholder: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "在这里粘贴或输入歌词...\n\n示例：\n前奏\n第一句歌词\n第二句歌词\n间奏\n继续歌词..."
        AppLanguage.ENGLISH -> "Paste or enter lyrics here...\n\nExample:\nIntro\nFirst line\nSecond line\nInterlude\nContinue lyrics..."
        AppLanguage.ARABIC -> "الصق أو أدخل كلمات الأغنية هنا...\n\nمثال:\nمقدمة\nالسطر الأول\nالسطر الثاني\nفاصل\nمتابعة الكلمات..."
        AppLanguage.PORTUGUESE -> "Cole ou insira letras aqui...\n\nExemplo:\nIntro\nPrimeira linha\nSegunda linha\nInterlúdio\nContinuar letras..."
        AppLanguage.SPANISH -> "Pegue o ingrese letras aquí...\n\nEjemplo:\nIntro\nPrimera línea\nSegunda línea\nInterludio\nContinuar letras..."
        AppLanguage.FRENCH -> "Collez ou saisissez les paroles ici...\n\nExemple :\nIntro\nPremière ligne\nDeuxième ligne\nInterlude\nContinuer les paroles..."
        AppLanguage.GERMAN -> "Hier Liedtext einfügen oder eingeben...\n\nBeispiel:\nIntro\nErste Zeile\nZweite Zeile\nZwischenspiel\nLiedtext fortsetzen..."
        AppLanguage.RUSSIAN -> "Вставьте или введите текст песни здесь...\n\nПример:\nВступление\nПервая строка\nВторая строка\nИнтерлюдия\nПродолжить текст..."
        AppLanguage.JAPANESE -> "ここに歌詞を貼り付けまたは入力...\n\n例:\nイントロ\n一行目\n二行目\n間奏\n歌詞を続ける..."
        AppLanguage.KOREAN -> "여기에 가사를 붙여넣거나 입력하세요...\n\n예:\n인트로\n첫 번째 줄\n두 번째 줄\n간주\n가사 계속..."
    }

    val alignmentHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "播放音频，在听到每句歌词开始时点击「打点」按钮"
        AppLanguage.ENGLISH -> "Play audio, click 'Tap' button when you hear each line start"
        AppLanguage.ARABIC -> "شغل الصوت، انقر على زر 'نقر' عند سماع بداية كل سطر"
        AppLanguage.PORTUGUESE -> "Reproduza o áudio, clique no botão 'Tocar' ao ouvir o início de cada linha"
        AppLanguage.SPANISH -> "Reproduzca el audio, haga clic en el botón 'Tocar' al escuchar el inicio de cada línea"
        AppLanguage.FRENCH -> "Lisez l'audio, cliquez sur le bouton « Taper » lorsque vous entendez le début de chaque ligne"
        AppLanguage.GERMAN -> "Audio abspielen, auf 'Tippen'-Button klicken, wenn jede Zeile beginnt"
        AppLanguage.RUSSIAN -> "Воспроизведите аудио, нажимайте кнопку «Нажать» при начале каждой строки"
        AppLanguage.JAPANESE -> "音声を再生し、各行の開始を聞いたら「タップ」ボタンをクリック"
        AppLanguage.KOREAN -> "오디오를 재생하고, 각 줄의 시작을 들으면 '탭' 버튼을 클릭하세요"
    }

    val rewind3s: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "后退3秒"
        AppLanguage.ENGLISH -> "Rewind 3s"
        AppLanguage.ARABIC -> "إرجاع 3 ثواني"
        AppLanguage.PORTUGUESE -> "Retroceder 3s"
        AppLanguage.SPANISH -> "Retroceder 3s"
        AppLanguage.FRENCH -> "Reculer de 3s"
        AppLanguage.GERMAN -> "3s zurückspulen"
        AppLanguage.RUSSIAN -> "Перемотать на 3с назад"
        AppLanguage.JAPANESE -> "3秒巻き戻し"
        AppLanguage.KOREAN -> "3초 뒤로"
    }

    val play: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "播放"
        AppLanguage.ENGLISH -> "Play"
        AppLanguage.ARABIC -> "تشغيل"
        AppLanguage.PORTUGUESE -> "Reproduzir"
        AppLanguage.SPANISH -> "Reproducir"
        AppLanguage.FRENCH -> "Lire"
        AppLanguage.GERMAN -> "Abspielen"
        AppLanguage.RUSSIAN -> "Воспроизвести"
        AppLanguage.JAPANESE -> "再生"
        AppLanguage.KOREAN -> "재생"
    }

    val pause: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "暂停"
        AppLanguage.ENGLISH -> "Pause"
        AppLanguage.ARABIC -> "إيقاف مؤقت"
        AppLanguage.PORTUGUESE -> "Pausar"
        AppLanguage.SPANISH -> "Pausar"
        AppLanguage.FRENCH -> "Pause"
        AppLanguage.GERMAN -> "Pause"
        AppLanguage.RUSSIAN -> "Пауза"
        AppLanguage.JAPANESE -> "一時停止"
        AppLanguage.KOREAN -> "일시정지"
    }

    val reTap: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "重新打点"
        AppLanguage.ENGLISH -> "Re-tap"
        AppLanguage.ARABIC -> "إعادة النقر"
        AppLanguage.PORTUGUESE -> "Retocar"
        AppLanguage.SPANISH -> "Volver a tocar"
        AppLanguage.FRENCH -> "Retaper"
        AppLanguage.GERMAN -> "Erneut tippen"
        AppLanguage.RUSSIAN -> "Нажать заново"
        AppLanguage.JAPANESE -> "再タップ"
        AppLanguage.KOREAN -> "다시 탭"
    }

    val undo: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "撤销"
        AppLanguage.ENGLISH -> "Undo"
        AppLanguage.ARABIC -> "تراجع"
        AppLanguage.PORTUGUESE -> "Desfazer"
        AppLanguage.SPANISH -> "Deshacer"
        AppLanguage.FRENCH -> "Annuler"
        AppLanguage.GERMAN -> "Rückgängig"
        AppLanguage.RUSSIAN -> "Отменить"
        AppLanguage.JAPANESE -> "元に戻す"
        AppLanguage.KOREAN -> "실행 취소"
    }

    val redo: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "重做"
        AppLanguage.ENGLISH -> "Redo"
        AppLanguage.ARABIC -> "إعادة"
        AppLanguage.PORTUGUESE -> "Refazer"
        AppLanguage.SPANISH -> "Rehacer"
        AppLanguage.FRENCH -> "Rétablir"
        AppLanguage.GERMAN -> "Wiederholen"
        AppLanguage.RUSSIAN -> "Повторить"
        AppLanguage.JAPANESE -> "やり直し"
        AppLanguage.KOREAN -> "다시 실행"
    }

    val progress: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "进度"
        AppLanguage.ENGLISH -> "Progress"
        AppLanguage.ARABIC -> "التقدم"
        AppLanguage.PORTUGUESE -> "Progresso"
        AppLanguage.SPANISH -> "Progreso"
        AppLanguage.FRENCH -> "Progression"
        AppLanguage.GERMAN -> "Fortschritt"
        AppLanguage.RUSSIAN -> "Прогресс"
        AppLanguage.JAPANESE -> "進行状況"
        AppLanguage.KOREAN -> "진행률"
    }

    val activationSuccess: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "激活成功！"
        AppLanguage.ENGLISH -> "Activation successful!"
        AppLanguage.ARABIC -> "تم التفعيل بنجاح!"
        AppLanguage.PORTUGUESE -> "Ativação bem-sucedida!"
        AppLanguage.SPANISH -> "¡Activación exitosa!"
        AppLanguage.FRENCH -> "Activation réussie !"
        AppLanguage.GERMAN -> "Aktivierung erfolgreich!"
        AppLanguage.RUSSIAN -> "Активация успешна!"
        AppLanguage.JAPANESE -> "アクティベーション成功！"
        AppLanguage.KOREAN -> "활성화 성공!"
    }

    val activationCodeCopied: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "激活码已复制"
        AppLanguage.ENGLISH -> "Activation code copied"
        AppLanguage.ARABIC -> "تم نسخ رمز التفعيل"
        AppLanguage.PORTUGUESE -> "Código de ativação copiado"
        AppLanguage.SPANISH -> "Código de activación copiado"
        AppLanguage.FRENCH -> "Code d'activation copié"
        AppLanguage.GERMAN -> "Aktivierungscode kopiert"
        AppLanguage.RUSSIAN -> "Код активации скопирован"
        AppLanguage.JAPANESE -> "アクティベーションコードをコピーしました"
        AppLanguage.KOREAN -> "활성화 코드가 복사되었습니다"
    }

    val copyActivationCode: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "复制激活码"
        AppLanguage.ENGLISH -> "Copy Activation Code"
        AppLanguage.ARABIC -> "نسخ رمز التفعيل"
        AppLanguage.PORTUGUESE -> "Copiar Código de Ativação"
        AppLanguage.SPANISH -> "Copiar Código de Activación"
        AppLanguage.FRENCH -> "Copier le code d'activation"
        AppLanguage.GERMAN -> "Aktivierungscode kopieren"
        AppLanguage.RUSSIAN -> "Копировать код активации"
        AppLanguage.JAPANESE -> "アクティベーションコードをコピー"
        AppLanguage.KOREAN -> "활성화 코드 복사"
    }

    val noActivationCodes: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "暂无激活码，点击上方按钮添加"
        AppLanguage.ENGLISH -> "No activation codes, click button above to add"
        AppLanguage.ARABIC -> "لا توجد رموز تفعيل، انقر على الزر أعلاه للإضافة"
        AppLanguage.PORTUGUESE -> "Sem códigos de ativação, clique no botão acima para adicionar"
        AppLanguage.SPANISH -> "Sin códigos de activación, haga clic en el botón superior para añadir"
        AppLanguage.FRENCH -> "Aucun code d'activation, cliquez sur le bouton ci-dessus pour ajouter"
        AppLanguage.GERMAN -> "Keine Aktivierungscodes, klicken Sie auf den oberen Button zum Hinzufügen"
        AppLanguage.RUSSIAN -> "Нет кодов активации, нажмите кнопку выше, чтобы добавить"
        AppLanguage.JAPANESE -> "アクティベーションコードがありません、上のボタンをクリックして追加"
        AppLanguage.KOREAN -> "활성화 코드가 없습니다, 위 버튼을 클릭하여 추가"
    }

    val activationCodeType: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "激活码类型"
        AppLanguage.ENGLISH -> "Activation Code Type"
        AppLanguage.ARABIC -> "نوع رمز التفعيل"
        AppLanguage.PORTUGUESE -> "Tipo de Código de Ativação"
        AppLanguage.SPANISH -> "Tipo de Código de Activación"
        AppLanguage.FRENCH -> "Type de code d'activation"
        AppLanguage.GERMAN -> "Aktivierungscode-Typ"
        AppLanguage.RUSSIAN -> "Тип кода активации"
        AppLanguage.JAPANESE -> "アクティベーションコードタイプ"
        AppLanguage.KOREAN -> "활성화 코드 유형"
    }

    val activationTypePermanent: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "永久激活"
        AppLanguage.ENGLISH -> "Permanent"
        AppLanguage.ARABIC -> "دائم"
        AppLanguage.PORTUGUESE -> "Permanente"
        AppLanguage.SPANISH -> "Permanente"
        AppLanguage.FRENCH -> "Permanent"
        AppLanguage.GERMAN -> "Dauerhaft"
        AppLanguage.RUSSIAN -> "Постоянный"
        AppLanguage.JAPANESE -> "永久"
        AppLanguage.KOREAN -> "영구"
    }
    val activationTypePermanentDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "激活后永久有效，无任何限制"
        AppLanguage.ENGLISH -> "Valid forever after activation, no restrictions"
        AppLanguage.ARABIC -> "صالح إلى الأبد بعد التفعيل، بدون قيود"
        AppLanguage.PORTUGUESE -> "Válido para sempre após ativação, sem restrições"
        AppLanguage.SPANISH -> "Válido para siempre tras la activación, sin restricciones"
        AppLanguage.FRENCH -> "Valide pour toujours après activation, sans restrictions"
        AppLanguage.GERMAN -> "Nach Aktivierung dauerhaft gültig, keine Einschränkungen"
        AppLanguage.RUSSIAN -> "Действителен навсегда после активации, без ограничений"
        AppLanguage.JAPANESE -> "アクティベーション後永久に有効、制限なし"
        AppLanguage.KOREAN -> "활성화 후 영구적으로 유효, 제한 없음"
    }
    val activationTypeTimeLimited: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "时间限制"
        AppLanguage.ENGLISH -> "Time Limited"
        AppLanguage.ARABIC -> "محدود بالوقت"
        AppLanguage.PORTUGUESE -> "Tempo Limitado"
        AppLanguage.SPANISH -> "Tiempo Limitado"
        AppLanguage.FRENCH -> "Durée limitée"
        AppLanguage.GERMAN -> "Zeitbegrenzt"
        AppLanguage.RUSSIAN -> "Ограничено по времени"
        AppLanguage.JAPANESE -> "時間制限"
        AppLanguage.KOREAN -> "시간 제한"
    }
    val activationTypeTimeLimitedDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "激活后在指定时间内有效"
        AppLanguage.ENGLISH -> "Valid within specified time after activation"
        AppLanguage.ARABIC -> "صالح خلال الوقت المحدد بعد التفعيل"
        AppLanguage.PORTUGUESE -> "Válido dentro do tempo especificado após ativação"
        AppLanguage.SPANISH -> "Válido dentro del tiempo especificado tras la activación"
        AppLanguage.FRENCH -> "Valide dans le délai spécifié après activation"
        AppLanguage.GERMAN -> "Innerhalb der angegebenen Zeit nach Aktivierung gültig"
        AppLanguage.RUSSIAN -> "Действителен в течение указанного времени после активации"
        AppLanguage.JAPANESE -> "アクティベーション後、指定時間内有効"
        AppLanguage.KOREAN -> "활성화 후 지정된 시간 내 유효"
    }
    val activationTypeUsageLimited: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "次数限制"
        AppLanguage.ENGLISH -> "Usage Limited"
        AppLanguage.ARABIC -> "محدود بالاستخدام"
        AppLanguage.PORTUGUESE -> "Uso Limitado"
        AppLanguage.SPANISH -> "Uso Limitado"
        AppLanguage.FRENCH -> "Usage limité"
        AppLanguage.GERMAN -> "Nutzungsbegrenzt"
        AppLanguage.RUSSIAN -> "Ограничено по использованию"
        AppLanguage.JAPANESE -> "回数制限"
        AppLanguage.KOREAN -> "사용 횟수 제한"
    }
    val activationTypeUsageLimitedDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "激活后可使用指定次数"
        AppLanguage.ENGLISH -> "Can be used specified number of times after activation"
        AppLanguage.ARABIC -> "يمكن استخدامه عدد محدد من المرات بعد التفعيل"
        AppLanguage.PORTUGUESE -> "Pode ser usado um número especificado de vezes após ativação"
        AppLanguage.SPANISH -> "Se puede usar un número especificado de veces tras la activación"
        AppLanguage.FRENCH -> "Utilisable un nombre spécifié de fois après activation"
        AppLanguage.GERMAN -> "Nach Aktivierung eine bestimmte Anzahl nutzbar"
        AppLanguage.RUSSIAN -> "Можно использовать указанное количество раз после активации"
        AppLanguage.JAPANESE -> "アクティベーション後、指定回数使用可能"
        AppLanguage.KOREAN -> "활성화 후 지정된 횟수만큼 사용 가능"
    }
    val activationTypeDeviceBound: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "设备绑定"
        AppLanguage.ENGLISH -> "Device Bound"
        AppLanguage.ARABIC -> "مرتبط بالجهاز"
        AppLanguage.PORTUGUESE -> "Vinculado ao Dispositivo"
        AppLanguage.SPANISH -> "Vinculado al Dispositivo"
        AppLanguage.FRENCH -> "Lié à l'appareil"
        AppLanguage.GERMAN -> "Gerätegebunden"
        AppLanguage.RUSSIAN -> "Привязан к устройству"
        AppLanguage.JAPANESE -> "デバイス紐付け"
        AppLanguage.KOREAN -> "기기 연결"
    }
    val activationTypeDeviceBoundDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "激活后绑定到当前设备"
        AppLanguage.ENGLISH -> "Bound to current device after activation"
        AppLanguage.ARABIC -> "مرتبط بالجهاز الحالي بعد التفعيل"
        AppLanguage.PORTUGUESE -> "Vinculado ao dispositivo atual após ativação"
        AppLanguage.SPANISH -> "Vinculado al dispositivo actual tras la activación"
        AppLanguage.FRENCH -> "Lié à l'appareil actuel après activation"
        AppLanguage.GERMAN -> "Nach Aktivierung an aktuelles Gerät gebunden"
        AppLanguage.RUSSIAN -> "Привязан к текущему устройству после активации"
        AppLanguage.JAPANESE -> "アクティベーション後、現在のデバイスに紐付け"
        AppLanguage.KOREAN -> "활성화 후 현재 기기에 연결"
    }
    val activationTypeCombined: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "组合限制"
        AppLanguage.ENGLISH -> "Combined"
        AppLanguage.ARABIC -> "مجمع"
        AppLanguage.PORTUGUESE -> "Combinado"
        AppLanguage.SPANISH -> "Combinado"
        AppLanguage.FRENCH -> "Combiné"
        AppLanguage.GERMAN -> "Kombiniert"
        AppLanguage.RUSSIAN -> "Комбинированный"
        AppLanguage.JAPANESE -> "組み合わせ"
        AppLanguage.KOREAN -> "조합"
    }
    val activationTypeCombinedDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "同时支持时间和次数限制"
        AppLanguage.ENGLISH -> "Supports both time and usage limits"
        AppLanguage.ARABIC -> "يدعم قيود الوقت والاستخدام معًا"
        AppLanguage.PORTUGUESE -> "Suporta tanto limites de tempo quanto de uso"
        AppLanguage.SPANISH -> "Soporta límites de tiempo y de uso"
        AppLanguage.FRENCH -> "Prend en charge les limites de durée et d'usage"
        AppLanguage.GERMAN -> "Unterstützt Zeit- und Nutzungslimits"
        AppLanguage.RUSSIAN -> "Поддерживает ограничения по времени и использованию"
        AppLanguage.JAPANESE -> "時間と回数の制限を両方サポート"
        AppLanguage.KOREAN -> "시간 및 사용 횟수 제한을 모두 지원"
    }

    val activated: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已激活"
        AppLanguage.ENGLISH -> "Activated"
        AppLanguage.ARABIC -> "مفعل"
        AppLanguage.PORTUGUESE -> "Ativado"
        AppLanguage.SPANISH -> "Activado"
        AppLanguage.FRENCH -> "Activé"
        AppLanguage.GERMAN -> "Aktiviert"
        AppLanguage.RUSSIAN -> "Активирован"
        AppLanguage.JAPANESE -> "アクティベート済み"
        AppLanguage.KOREAN -> "활성화됨"
    }

    // Shown on the activation record inside the code prompt when a valid grant
    // exists but this launch still requires verification. Never claims "已激活"
    // while a code is being demanded.
    val activationNeedsReverify: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "需重新验证"
        AppLanguage.ENGLISH -> "Re-verification required"
        AppLanguage.ARABIC -> "يلزم إعادة التحقق"
        AppLanguage.PORTUGUESE -> "Requer nova verificação"
        AppLanguage.SPANISH -> "Se requiere reverificación"
        AppLanguage.FRENCH -> "Revérification requise"
        AppLanguage.GERMAN -> "Erneute Prüfung erforderlich"
        AppLanguage.RUSSIAN -> "Требуется повторная проверка"
        AppLanguage.JAPANESE -> "再検証が必要です"
        AppLanguage.KOREAN -> "재검증 필요"
    }

    val activationExpired: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "激活已失效"
        AppLanguage.ENGLISH -> "Activation expired"
        AppLanguage.ARABIC -> "انتهت صلاحية التفعيل"
        AppLanguage.PORTUGUESE -> "Ativação expirada"
        AppLanguage.SPANISH -> "Activación caducada"
        AppLanguage.FRENCH -> "Activation expirée"
        AppLanguage.GERMAN -> "Aktivierung abgelaufen"
        AppLanguage.RUSSIAN -> "Активация истекла"
        AppLanguage.JAPANESE -> "アクティベーション期限切れ"
        AppLanguage.KOREAN -> "활성화 만료됨"
    }

    val activationTime: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "激活时间"
        AppLanguage.ENGLISH -> "Activation Time"
        AppLanguage.ARABIC -> "وقت التفعيل"
        AppLanguage.PORTUGUESE -> "Hora de Ativação"
        AppLanguage.SPANISH -> "Hora de Activación"
        AppLanguage.FRENCH -> "Heure d'activation"
        AppLanguage.GERMAN -> "Aktivierungszeit"
        AppLanguage.RUSSIAN -> "Время активации"
        AppLanguage.JAPANESE -> "アクティベーション時刻"
        AppLanguage.KOREAN -> "활성화 시간"
    }

    val remainingTime: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "剩余时间"
        AppLanguage.ENGLISH -> "Remaining Time"
        AppLanguage.ARABIC -> "الوقت المتبقي"
        AppLanguage.PORTUGUESE -> "Tempo Restante"
        AppLanguage.SPANISH -> "Tiempo Restante"
        AppLanguage.FRENCH -> "Temps restant"
        AppLanguage.GERMAN -> "Verbleibende Zeit"
        AppLanguage.RUSSIAN -> "Оставшееся время"
        AppLanguage.JAPANESE -> "残り時間"
        AppLanguage.KOREAN -> "남은 시간"
    }

    val expireTime: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "过期时间"
        AppLanguage.ENGLISH -> "Expire Time"
        AppLanguage.ARABIC -> "وقت الانتهاء"
        AppLanguage.PORTUGUESE -> "Hora de Expiração"
        AppLanguage.SPANISH -> "Hora de Expiración"
        AppLanguage.FRENCH -> "Heure d'expiration"
        AppLanguage.GERMAN -> "Ablaufzeit"
        AppLanguage.RUSSIAN -> "Время истечения"
        AppLanguage.JAPANESE -> "有効期限"
        AppLanguage.KOREAN -> "만료 시간"
    }

    val remainingUsage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "剩余次数"
        AppLanguage.ENGLISH -> "Remaining Usage"
        AppLanguage.ARABIC -> "الاستخدامات المتبقية"
        AppLanguage.PORTUGUESE -> "Usos Restantes"
        AppLanguage.SPANISH -> "Usos Restantes"
        AppLanguage.FRENCH -> "Utilisations restantes"
        AppLanguage.GERMAN -> "Verbleibende Nutzungen"
        AppLanguage.RUSSIAN -> "Оставшиеся использования"
        AppLanguage.JAPANESE -> "残り使用回数"
        AppLanguage.KOREAN -> "남은 사용 횟수"
    }

    val deviceBound: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "设备绑定：已启用"
        AppLanguage.ENGLISH -> "Device Bound: Enabled"
        AppLanguage.ARABIC -> "ربط الجهاز: مفعل"
        AppLanguage.PORTUGUESE -> "Vinculado ao Dispositivo: Ativado"
        AppLanguage.SPANISH -> "Vinculado al Dispositivo: Activado"
        AppLanguage.FRENCH -> "Lié à l'appareil : Activé"
        AppLanguage.GERMAN -> "Gerätegebunden: Aktiviert"
        AppLanguage.RUSSIAN -> "Привязан к устройству: Включено"
        AppLanguage.JAPANESE -> "デバイス紐付け: 有効"
        AppLanguage.KOREAN -> "기기 연결: 활성화됨"
    }
    val deviceBoundRemoteTip: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "提示：设备绑定在本机生效（防止本机重激活/硬件变更后旧码复用）。跨设备限制需要在应用中配置远程激活验证服务，由服务器按设备 ID 拒绝其他设备。"
        AppLanguage.ENGLISH -> "Note: device binding is enforced on this device only (prevents re-activation after a local reset or hardware change). Cross-device restriction requires configuring the remote activation verifier in the app, so the server can reject other devices by device ID."
        AppLanguage.ARABIC -> "ملاحظة: ربط الجهاز يُطبَّق على هذا الجهاز فقط (يمنع إعادة التفعيل بعد إعادة الضبط المحلية أو تغيير العتاد). التقييد عبر الأجهزة يتطلب إعداد خدمة التحقق عن بُعد في التطبيق، بحيث يرفض الخادم الأجهزة الأخرى حسب معرّف الجهاز."
        AppLanguage.PORTUGUESE -> "Observação: a vinculação ao dispositivo vale apenas neste aparelho (impede reativação após redefinição local ou troca de hardware). A restrição entre dispositivos exige configurar o verificador remoto no aplicativo, para o servidor rejeitar outros dispositivos pelo ID."
        AppLanguage.SPANISH -> "Nota: la vinculación al dispositivo solo se aplica en este equipo (evita reactivar tras un restablecimiento local o cambio de hardware). La restricción entre dispositivos requiere configurar el verificador remoto en la app, para que el servidor rechace otros dispositivos por ID."
        AppLanguage.FRENCH -> "Remarque : la liaison à l'appareil ne s'applique qu'à cet appareil (empêche la réactivation après une réinitialisation locale ou un changement de matériel). La restriction multi-appareils nécessite de configurer le vérificateur distant dans l'application, pour que le serveur rejette les autres appareils par ID."
        AppLanguage.GERMAN -> "Hinweis: Die Gerätebindung gilt nur für dieses Gerät (verhindert Reaktivierung nach lokalem Reset oder Hardwarewechsel). Geräteübergreifende Einschränkung erfordert die Konfiguration des Remote-Verifizierers in der App, damit der Server andere Geräte per ID ablehnt."
        AppLanguage.RUSSIAN -> "Примечание: привязка к устройству действует только на этом устройстве (предотвращает повторную активацию после локального сброса или замены железа). Ограничение между устройствами требует настройки удалённого проверяющего в приложении, чтобы сервер отклонял другие устройства по ID."
        AppLanguage.JAPANESE -> "注意: デバイス紐付けはこの端末のみで有効です（ローカルリセットやハードウェア変更後の再アクティベーションを防ぎます）。端末間の制限には、アプリでリモート検証サービスを設定し、サーバーがデバイス ID で他の端末を拒否する必要があります。"
        AppLanguage.KOREAN -> "참고: 기기 연결은 이 기기에서만 적용됩니다(로컬 초기화 또는 하드웨어 변경 후 재활성화 방지). 기기 간 제한은 앱에서 원격 인증 서비스를 구성하여 서버가 기기 ID로 다른 기기를 거부하도록 해야 합니다."
    }
    val invalidActivationCode: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "无效的激活码"
        AppLanguage.ENGLISH -> "Invalid activation code"
        AppLanguage.ARABIC -> "رمز تفعيل غير صالح"
        AppLanguage.PORTUGUESE -> "Código de ativação inválido"
        AppLanguage.SPANISH -> "Código de activación inválido"
        AppLanguage.FRENCH -> "Code d'activation invalide"
        AppLanguage.GERMAN -> "Ungültiger Aktivierungscode"
        AppLanguage.RUSSIAN -> "Недействительный код активации"
        AppLanguage.JAPANESE -> "無効なアクティベーションコード"
        AppLanguage.KOREAN -> "잘못된 활성화 코드"
    }

    val remoteActivationTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "在线验证"
        AppLanguage.ENGLISH -> "Online Verification"
        AppLanguage.ARABIC -> "التحقق عبر الإنترنت"
        AppLanguage.PORTUGUESE -> "Verificação Online"
        AppLanguage.SPANISH -> "Verificación en Línea"
        AppLanguage.FRENCH -> "Vérification en ligne"
        AppLanguage.GERMAN -> "Online-Verifizierung"
        AppLanguage.RUSSIAN -> "Онлайн-проверка"
        AppLanguage.JAPANESE -> "オンライン認証"
        AppLanguage.KOREAN -> "온라인 인증"
    }

    val activationSectionRemoteEnabled: (String) -> String get() = { url ->
        when (Strings.lang) {
            AppLanguage.CHINESE -> if (url.isNotEmpty()) String.format(java.util.Locale.getDefault(), "已启用 · %s", url) else "已启用"
            AppLanguage.ENGLISH -> if (url.isNotEmpty()) String.format(java.util.Locale.getDefault(), "Enabled · %s", url) else "Enabled"
            AppLanguage.ARABIC -> if (url.isNotEmpty()) String.format(java.util.Locale.getDefault(), "مفعّل · %s", url) else "مفعّل"
            AppLanguage.PORTUGUESE -> if (url.isNotEmpty()) String.format(java.util.Locale.getDefault(), "Ativado · %s", url) else "Ativado"
            AppLanguage.SPANISH -> if (url.isNotEmpty()) String.format(java.util.Locale.getDefault(), "Activado · %s", url) else "Activado"
            AppLanguage.FRENCH -> if (url.isNotEmpty()) String.format(java.util.Locale.getDefault(), "Activé · %s", url) else "Activé"
            AppLanguage.GERMAN -> if (url.isNotEmpty()) String.format(java.util.Locale.getDefault(), "Aktiviert · %s", url) else "Aktiviert"
            AppLanguage.RUSSIAN -> if (url.isNotEmpty()) String.format(java.util.Locale.getDefault(), "Включено · %s", url) else "Включено"
            AppLanguage.JAPANESE -> if (url.isNotEmpty()) String.format(java.util.Locale.getDefault(), "有効 · %s", url) else "有効"
            AppLanguage.KOREAN -> if (url.isNotEmpty()) String.format(java.util.Locale.getDefault(), "활성화됨 · %s", url) else "활성화됨"
        }
    }

    val activationSectionRemoteDisabled: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "未启用"
        AppLanguage.ENGLISH -> "Not enabled"
        AppLanguage.ARABIC -> "غير مفعّل"
        AppLanguage.PORTUGUESE -> "Não ativado"
        AppLanguage.SPANISH -> "No activado"
        AppLanguage.FRENCH -> "Non activé"
        AppLanguage.GERMAN -> "Nicht aktiviert"
        AppLanguage.RUSSIAN -> "Не включено"
        AppLanguage.JAPANESE -> "無効"
        AppLanguage.KOREAN -> "비활성화됨"
    }

    val activationSectionCodes: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "管理激活码"
        AppLanguage.ENGLISH -> "Manage Codes"
        AppLanguage.ARABIC -> "إدارة الرموز"
        AppLanguage.PORTUGUESE -> "Gerenciar Códigos"
        AppLanguage.SPANISH -> "Gestionar Códigos"
        AppLanguage.FRENCH -> "Gérer les codes"
        AppLanguage.GERMAN -> "Codes verwalten"
        AppLanguage.RUSSIAN -> "Управление кодами"
        AppLanguage.JAPANESE -> "コードを管理"
        AppLanguage.KOREAN -> "코드 관리"
    }

    val activationSectionCodesCount: (Int) -> String get() = { count ->
        when (Strings.lang) {
            AppLanguage.CHINESE -> String.format(java.util.Locale.getDefault(), "共 %d 个", count)
            AppLanguage.ENGLISH -> String.format(java.util.Locale.getDefault(), "%d total", count)
            AppLanguage.ARABIC -> String.format(java.util.Locale.getDefault(), "%d الإجمالي", count)
            AppLanguage.PORTUGUESE -> String.format(java.util.Locale.getDefault(), "%d no total", count)
            AppLanguage.SPANISH -> String.format(java.util.Locale.getDefault(), "%d en total", count)
            AppLanguage.FRENCH -> String.format(java.util.Locale.getDefault(), "%d au total", count)
            AppLanguage.GERMAN -> String.format(java.util.Locale.getDefault(), "%d gesamt", count)
            AppLanguage.RUSSIAN -> String.format(java.util.Locale.getDefault(), "%d всего", count)
            AppLanguage.JAPANESE -> String.format(java.util.Locale.getDefault(), "合計 %d 個", count)
            AppLanguage.KOREAN -> String.format(java.util.Locale.getDefault(), "총 %d개", count)
        }
    }

    val activationSectionCodesEmpty: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "暂无激活码"
        AppLanguage.ENGLISH -> "No codes yet"
        AppLanguage.ARABIC -> "لا رموز بعد"
        AppLanguage.PORTUGUESE -> "Sem códigos ainda"
        AppLanguage.SPANISH -> "Sin códigos aún"
        AppLanguage.FRENCH -> "Aucun code pour l'instant"
        AppLanguage.GERMAN -> "Noch keine Codes"
        AppLanguage.RUSSIAN -> "Кодов пока нет"
        AppLanguage.JAPANESE -> "コードがまだありません"
        AppLanguage.KOREAN -> "아직 코드가 없습니다"
    }

    val remoteActivationHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "让你自己的 HTTPS 接口验证激活码，用于吊销、动态发码、设备识别和次数控制。"
        AppLanguage.ENGLISH -> "Verify activation codes with your own HTTPS endpoint for revocation, dynamic issuing, device checks, and usage control."
        AppLanguage.ARABIC -> "تحقق من رموز التفعيل عبر نقطة HTTPS الخاصة بك للإبطال والإصدار الديناميكي وفحص الجهاز والتحكم في الاستخدام."
        AppLanguage.PORTUGUESE -> "Verifique códigos de ativação com seu próprio endpoint HTTPS para revogação, emissão dinâmica, verificação de dispositivo e controle de uso."
        AppLanguage.SPANISH -> "Verifique códigos de activación con su propio endpoint HTTPS para revocación, emisión dinámica, verificación de dispositivo y control de uso."
        AppLanguage.FRENCH -> "Vérifiez les codes d'activation avec votre propre endpoint HTTPS pour la révocation, l'émission dynamique, la vérification de l'appareil et le contrôle d'usage."
        AppLanguage.GERMAN -> "Verifizieren Sie Aktivierungscodes mit Ihrem eigenen HTTPS-Endpoint für Widerruf, dynamische Ausstellung, Geräteprüfung und Nutzungssteuerung."
        AppLanguage.RUSSIAN -> "Проверяйте коды активации через свой HTTPS-endpoint для отзыва, динамической выдачи, проверки устройств и контроля использования."
        AppLanguage.JAPANESE -> "独自のHTTPSエンドポイントでアクティベーションコードを検証し、取り消し、動的発行、デバイス確認、使用回数制御に使用します。"
        AppLanguage.KOREAN -> "자체 HTTPS 엔드포인트로 활성화 코드를 검증하여 취소, 동적 발급, 기기 확인 및 사용 제어에 사용합니다."
    }

    val remoteActivationUrlLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "验证接口地址（必须 HTTPS）"
        AppLanguage.ENGLISH -> "Verification endpoint (must be HTTPS)"
        AppLanguage.ARABIC -> "نقطة نهاية التحقق (يجب أن تكون HTTPS)"
        AppLanguage.PORTUGUESE -> "Endpoint de verificação (deve ser HTTPS)"
        AppLanguage.SPANISH -> "Endpoint de verificación (debe ser HTTPS)"
        AppLanguage.FRENCH -> "Endpoint de vérification (doit être HTTPS)"
        AppLanguage.GERMAN -> "Verifizierungs-Endpoint (muss HTTPS sein)"
        AppLanguage.RUSSIAN -> "Endpoint проверки (должен быть HTTPS)"
        AppLanguage.JAPANESE -> "検証エンドポイント（HTTPS必須）"
        AppLanguage.KOREAN -> "검증 엔드포인트 (HTTPS 필수)"
    }

    val remoteActivationPublicKeyLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "验签公钥（EC P-256，Base64）"
        AppLanguage.ENGLISH -> "Signature public key (EC P-256, Base64)"
        AppLanguage.ARABIC -> "مفتاح التوقيع العام (EC P-256، Base64)"
        AppLanguage.PORTUGUESE -> "Chave pública de assinatura (EC P-256, Base64)"
        AppLanguage.SPANISH -> "Clave pública de firma (EC P-256, Base64)"
        AppLanguage.FRENCH -> "Clé publique de signature (EC P-256, Base64)"
        AppLanguage.GERMAN -> "Signatur-Öffentlichschlüssel (EC P-256, Base64)"
        AppLanguage.RUSSIAN -> "Публичный ключ подписи (EC P-256, Base64)"
        AppLanguage.JAPANESE -> "署名公開鍵（EC P-256、Base64）"
        AppLanguage.KOREAN -> "서명 공개 키 (EC P-256, Base64)"
    }

    val remoteActivationGuideButton: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "接口教程与示例代码"
        AppLanguage.ENGLISH -> "Endpoint guide and sample code"
        AppLanguage.ARABIC -> "دليل نقطة النهاية وكود مثال"
        AppLanguage.PORTUGUESE -> "Guia de endpoint e código de exemplo"
        AppLanguage.SPANISH -> "Guía de endpoint y código de ejemplo"
        AppLanguage.FRENCH -> "Guide de l'endpoint et exemple de code"
        AppLanguage.GERMAN -> "Endpoint-Leitfaden und Beispielcode"
        AppLanguage.RUSSIAN -> "Руководство по endpoint и пример кода"
        AppLanguage.JAPANESE -> "エンドポイントガイドとサンプルコード"
        AppLanguage.KOREAN -> "엔드포인트 가이드 및 샘플 코드"
    }

    val remoteActivationProtocolSummary: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "客户端会 POST JSON 到你的接口，服务端返回带签名的 JSON。字段名和签名载荷必须与教程一致。"
        AppLanguage.ENGLISH -> "The client POSTs JSON to your endpoint, then expects signed JSON back. Field names and the signed payload must match the guide."
        AppLanguage.ARABIC -> "يرسل العميل JSON عبر POST إلى نقطة النهاية الخاصة بك ويتوقع JSON موقعًا. يجب أن تطابق أسماء الحقول وحمولة التوقيع الدليل."
        AppLanguage.PORTUGUESE -> "O cliente envia JSON via POST ao seu endpoint, esperando JSON assinado de volta. Nomes de campos e payload assinado devem corresponder ao guia."
        AppLanguage.SPANISH -> "El cliente envía JSON vía POST a su endpoint, esperando JSON firmado de vuelta. Los nombres de campos y el payload firmado deben coincidir con la guía."
        AppLanguage.FRENCH -> "Le client envoie du JSON via POST à votre endpoint, puis attend du JSON signé en retour. Les noms de champs et le payload signé doivent correspondre au guide."
        AppLanguage.GERMAN -> "Der Client sendet JSON per POST an Ihren Endpoint und erwartet signiertes JSON zurück. Feldnamen und signierte Payload müssen mit dem Leitfaden übereinstimmen."
        AppLanguage.RUSSIAN -> "Клиент отправляет JSON через POST на ваш endpoint и ожидает подписанный JSON обратно. Имена полей и подписанная payload должны совпадать с руководством."
        AppLanguage.JAPANESE -> "クライアントはJSONをPOSTでエンドポイントに送信し、署名付きJSONを受信します。フィールド名と署名ペイロードはガイドと一致する必要があります。"
        AppLanguage.KOREAN -> "클라이언트는 JSON을 POST로 엔드포인트에 보내고 서명된 JSON을 받습니다. 필드 이름과 서명된 페이로드는 가이드와 일치해야 합니다."
    }

    val remoteActivationUrlSupporting: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "填写你的服务端验证地址，例如 https://example.com/activation/verify"
        AppLanguage.ENGLISH -> "Enter your server verification URL, for example https://example.com/activation/verify"
        AppLanguage.ARABIC -> "أدخل رابط تحقق الخادم، مثل https://example.com/activation/verify"
        AppLanguage.PORTUGUESE -> "Insira a URL de verificação do servidor, por exemplo https://example.com/activation/verify"
        AppLanguage.SPANISH -> "Ingrese la URL de verificación del servidor, por ejemplo https://example.com/activation/verify"
        AppLanguage.FRENCH -> "Saisissez l'URL de vérification du serveur, par exemple https://example.com/activation/verify"
        AppLanguage.GERMAN -> "Geben Sie die Server-Verifizierungs-URL ein, z. B. https://example.com/activation/verify"
        AppLanguage.RUSSIAN -> "Введите URL проверки сервера, например https://example.com/activation/verify"
        AppLanguage.JAPANESE -> "サーバー検証URLを入力、例: https://example.com/activation/verify"
        AppLanguage.KOREAN -> "서버 검증 URL을 입력하세요, 예: https://example.com/activation/verify"
    }

    val remoteActivationPublicKeySupporting: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "这里填公钥；私钥只放在你的服务器上，用来签名响应。留空或签名不匹配会验证失败。"
        AppLanguage.ENGLISH -> "Paste the public key here. Keep the private key only on your server to sign responses. Empty or mismatched keys fail verification."
        AppLanguage.ARABIC -> "الصق المفتاح العام هنا. احتفظ بالمفتاح الخاص على الخادم فقط لتوقيع الاستجابات. المفتاح الفارغ أو غير المطابق يفشل التحقق."
        AppLanguage.PORTUGUESE -> "Cole a chave pública aqui. Mantenha a chave privada apenas no servidor para assinar respostas. Chaves vazias ou incompatíveis falham na verificação."
        AppLanguage.SPANISH -> "Pegue la clave pública aquí. Mantenga la clave privada solo en su servidor para firmar respuestas. Claves vacías o no coincidentes fallan la verificación."
        AppLanguage.FRENCH -> "Collez la clé publique ici. Gardez la clé privée uniquement sur votre serveur pour signer les réponses. Les clés vides ou non correspondantes échouent la vérification."
        AppLanguage.GERMAN -> "Fügen Sie den öffentlichen Schlüssel hier ein. Behalten Sie den privaten Schlüssel nur auf Ihrem Server zum Signieren von Antworten. Leere oder nicht übereinstimmende Schlüssel scheitern an der Verifizierung."
        AppLanguage.RUSSIAN -> "Вставьте публичный ключ сюда. Держите приватный ключ только на вашем сервере для подписи ответов. Пустые или несоответствующие ключи не проходят проверку."
        AppLanguage.JAPANESE -> "ここに公開鍵を貼り付けます。秘密鍵はサーバーにのみ保持し、レスポンスの署名に使用します。空または不一致の鍵は検証に失敗します。"
        AppLanguage.KOREAN -> "여기에 공개 키를 붙여넣으세요. 개인 키는 서버에만 보관하여 응답 서명에 사용합니다. 비어 있거나 일치하지 않는 키는 검증에 실패합니다."
    }

    val remoteActivationGuideTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "在线验证接口教程"
        AppLanguage.ENGLISH -> "Online Verification Guide"
        AppLanguage.ARABIC -> "دليل التحقق عبر الإنترنت"
        AppLanguage.PORTUGUESE -> "Guia de Verificação Online"
        AppLanguage.SPANISH -> "Guía de Verificación en Línea"
        AppLanguage.FRENCH -> "Guide de vérification en ligne"
        AppLanguage.GERMAN -> "Online-Verifizierungs-Leitfaden"
        AppLanguage.RUSSIAN -> "Руководство по онлайн-проверке"
        AppLanguage.JAPANESE -> "オンライン認証ガイド"
        AppLanguage.KOREAN -> "온라인 인증 가이드"
    }

    val remoteActivationGuideIntro: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "这个功能需要你部署一个 HTTPS 接口。应用负责收集激活码和设备信息，服务器负责判断是否允许，最后用你的私钥签名结果；应用只保存公钥并验签。"
        AppLanguage.ENGLISH -> "This feature needs an HTTPS endpoint. The app sends the code and device info, your server decides whether it is allowed, signs the result with your private key, and the app verifies it with the public key."
        AppLanguage.ARABIC -> "تحتاج هذه الميزة إلى نقطة HTTPS. يرسل التطبيق الرمز ومعلومات الجهاز، ويقرر خادمك السماح، ثم يوقع النتيجة بالمفتاح الخاص ويتحقق التطبيق منها بالمفتاح العام."
        AppLanguage.PORTUGUESE -> "Este recurso precisa de um endpoint HTTPS. O app envia o código e as informações do dispositivo, seu servidor decide se é permitido, assina o resultado com sua chave privada, e o app verifica com a chave pública."
        AppLanguage.SPANISH -> "Esta función necesita un endpoint HTTPS. La app envía el código y la información del dispositivo, su servidor decide si está permitido, firma el resultado con su clave privada, y la app lo verifica con la clave pública."
        AppLanguage.FRENCH -> "Cette fonctionnalité nécessite un endpoint HTTPS. L'app envoie le code et les infos de l'appareil, votre serveur décide si c'est autorisé, signe le résultat avec votre clé privée, et l'app vérifie avec la clé publique."
        AppLanguage.GERMAN -> "Diese Funktion benötigt einen HTTPS-Endpoint. Die App sendet Code und Geräteinfo, Ihr Server entscheidet ob erlaubt, signiert das Ergebnis mit Ihrem privaten Schlüssel, und die App verifiziert mit dem öffentlichen Schlüssel."
        AppLanguage.RUSSIAN -> "Эта функция требует HTTPS-endpoint. Приложение отправляет код и информацию об устройстве, ваш сервер решает, разрешено ли это, подписывает результат вашим приватным ключом, и приложение проверяет публичным ключом."
        AppLanguage.JAPANESE -> "この機能にはHTTPSエンドポイントが必要です。アプリがコードとデバイス情報を送信し、サーバーが許可可否を判断し、秘密鍵で結果に署名し、アプリが公開鍵で検証します。"
        AppLanguage.KOREAN -> "이 기능은 HTTPS 엔드포인트가 필요합니다. 앱이 코드와 기기 정보를 보내고, 서버가 허용 여부를 결정하여 개인 키로 결과에 서명하고, 앱이 공개 키로 검증합니다."
    }

    val remoteActivationGuideRequestTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "1. 请求格式"
        AppLanguage.ENGLISH -> "1. Request"
        AppLanguage.ARABIC -> "1. الطلب"
        AppLanguage.PORTUGUESE -> "1. Requisição"
        AppLanguage.SPANISH -> "1. Solicitud"
        AppLanguage.FRENCH -> "1. Requête"
        AppLanguage.GERMAN -> "1. Anfrage"
        AppLanguage.RUSSIAN -> "1. Запрос"
        AppLanguage.JAPANESE -> "1. リクエスト"
        AppLanguage.KOREAN -> "1. 요청"
    }

    val remoteActivationGuideRequestBody: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "应用使用 POST application/json。必须读取 code、deviceId、packageName、nonce、ts。nonce 是本次请求随机值，响应里必须原样返回。"
        AppLanguage.ENGLISH -> "The app uses POST application/json. Read code, deviceId, packageName, nonce, and ts. nonce is a per-request random value and must be returned unchanged."
        AppLanguage.ARABIC -> "يستخدم التطبيق POST application/json. اقرأ code و deviceId و packageName و nonce و ts. قيمة nonce عشوائية لكل طلب ويجب إرجاعها كما هي."
        AppLanguage.PORTUGUESE -> "O app usa POST application/json. Leia code, deviceId, packageName, nonce e ts. nonce é um valor aleatório por requisição e deve ser retornado inalterado."
        AppLanguage.SPANISH -> "La app usa POST application/json. Lea code, deviceId, packageName, nonce y ts. nonce es un valor aleatorio por solicitud y debe devolverse sin cambios."
        AppLanguage.FRENCH -> "L'app utilise POST application/json. Lisez code, deviceId, packageName, nonce et ts. nonce est une valeur aléatoire par requête et doit être renvoyée inchangée."
        AppLanguage.GERMAN -> "Die App verwendet POST application/json. Lesen Sie code, deviceId, packageName, nonce und ts. nonce ist ein zufälliger Wert pro Anfrage und muss unverändert zurückgegeben werden."
        AppLanguage.RUSSIAN -> "Приложение использует POST application/json. Читайте code, deviceId, packageName, nonce и ts. nonce — случайное значение для каждого запроса, должно возвращаться без изменений."
        AppLanguage.JAPANESE -> "アプリはPOST application/jsonを使用します。code、deviceId、packageName、nonce、tsを読み取ります。nonceはリクエストごとのランダム値であり、変更せずに返す必要があります。"
        AppLanguage.KOREAN -> "앱은 POST application/json을 사용합니다. code, deviceId, packageName, nonce, ts를 읽으세요. nonce는 요청마다 임의의 값이며 변경하지 않고 반환해야 합니다."
    }

    val remoteActivationGuideRequestExampleTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "请求示例"
        AppLanguage.ENGLISH -> "Request example"
        AppLanguage.ARABIC -> "مثال الطلب"
        AppLanguage.PORTUGUESE -> "Exemplo de requisição"
        AppLanguage.SPANISH -> "Ejemplo de solicitud"
        AppLanguage.FRENCH -> "Exemple de requête"
        AppLanguage.GERMAN -> "Anfrage-Beispiel"
        AppLanguage.RUSSIAN -> "Пример запроса"
        AppLanguage.JAPANESE -> "リクエスト例"
        AppLanguage.KOREAN -> "요청 예시"
    }

    val remoteActivationGuideResponseTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "2. 返回格式"
        AppLanguage.ENGLISH -> "2. Response"
        AppLanguage.ARABIC -> "2. الاستجابة"
        AppLanguage.PORTUGUESE -> "2. Resposta"
        AppLanguage.SPANISH -> "2. Respuesta"
        AppLanguage.FRENCH -> "2. Réponse"
        AppLanguage.GERMAN -> "2. Antwort"
        AppLanguage.RUSSIAN -> "2. Ответ"
        AppLanguage.JAPANESE -> "2. レスポンス"
        AppLanguage.KOREAN -> "2. 응답"
    }

    val remoteActivationGuideResponseBody: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "返回 JSON。ok 表示是否通过；message 会展示给用户；expiresAt 是毫秒时间戳，null 表示不过期；remainingUses 可为 null；sig 是 Base64 ECDSA 签名。"
        AppLanguage.ENGLISH -> "Return JSON. ok means allowed; message can be shown to users; expiresAt is a millisecond timestamp or null; remainingUses can be null; sig is a Base64 ECDSA signature."
        AppLanguage.ARABIC -> "أرجع JSON. تعني ok السماح؛ يمكن عرض message للمستخدم؛ expiresAt طابع زمني بالميلي ثانية أو null؛ يمكن أن تكون remainingUses قيمة null؛ و sig توقيع ECDSA بصيغة Base64."
        AppLanguage.PORTUGUESE -> "Retorne JSON. ok significa permitido; message pode ser exibido aos usuários; expiresAt é um timestamp em milissegundos ou null; remainingUses pode ser null; sig é uma assinatura ECDSA em Base64."
        AppLanguage.SPANISH -> "Devuelva JSON. ok significa permitido; message puede mostrarse a los usuarios; expiresAt es un timestamp en milisegundos o null; remainingUses puede ser null; sig es una firma ECDSA en Base64."
        AppLanguage.FRENCH -> "Renvoyez du JSON. ok signifie autorisé ; message peut être affiché aux utilisateurs ; expiresAt est un timestamp en millisecondes ou null ; remainingUses peut être null ; sig est une signature ECDSA en Base64."
        AppLanguage.GERMAN -> "Geben Sie JSON zurück. ok bedeutet erlaubt; message kann Nutzern angezeigt werden; expiresAt ist ein Millisekunden-Zeitstempel oder null; remainingUses kann null sein; sig ist eine Base64-ECDSA-Signatur."
        AppLanguage.RUSSIAN -> "Верните JSON. ok означает разрешено; message можно показать пользователю; expiresAt — временная метка в миллисекундах или null; remainingUses может быть null; sig — подпись ECDSA в Base64."
        AppLanguage.JAPANESE -> "JSONを返します。okは許可を意味し、messageはユーザーに表示でき、expiresAtはミリ秒タイムスタンプまたはnull、remainingUsesはnull可能、sigはBase64 ECDSA署名です。"
        AppLanguage.KOREAN -> "JSON을 반환합니다. ok는 허용됨을 의미하고, message는 사용자에게 표시할 수 있으며, expiresAt은 밀리초 타임스탬프 또는 null, remainingUses는 null 가능, sig는 Base64 ECDSA 서명입니다."
    }

    val remoteActivationGuideResponseExampleTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "成功返回示例"
        AppLanguage.ENGLISH -> "Success response"
        AppLanguage.ARABIC -> "استجابة نجاح"
        AppLanguage.PORTUGUESE -> "Resposta de sucesso"
        AppLanguage.SPANISH -> "Respuesta exitosa"
        AppLanguage.FRENCH -> "Réponse de succès"
        AppLanguage.GERMAN -> "Erfolgsantwort"
        AppLanguage.RUSSIAN -> "Успешный ответ"
        AppLanguage.JAPANESE -> "成功レスポンス"
        AppLanguage.KOREAN -> "성공 응답"
    }

    val remoteActivationGuideSignatureTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "3. 签名规则"
        AppLanguage.ENGLISH -> "3. Signature"
        AppLanguage.ARABIC -> "3. التوقيع"
        AppLanguage.PORTUGUESE -> "3. Assinatura"
        AppLanguage.SPANISH -> "3. Firma"
        AppLanguage.FRENCH -> "3. Règles de signature"
        AppLanguage.GERMAN -> "3. Signatur"
        AppLanguage.RUSSIAN -> "3. Подпись"
        AppLanguage.JAPANESE -> "3. 署名"
        AppLanguage.KOREAN -> "3. 서명"
    }

    val remoteActivationGuideSignatureBody: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "签名算法是 SHA256withECDSA，推荐 EC P-256。签名内容必须是下面这个 JSON 结构：ok、expiresAt、remainingUses、nonce。null 过期时间用 0，null 次数用 -1。"
        AppLanguage.ENGLISH -> "Use SHA256withECDSA, preferably EC P-256. Sign exactly this JSON structure: ok, expiresAt, remainingUses, nonce. Use 0 for null expiry and -1 for null remaining uses."
        AppLanguage.ARABIC -> "استخدم SHA256withECDSA ويفضل EC P-256. وقّع بنية JSON هذه تحديدًا: ok و expiresAt و remainingUses و nonce. استخدم 0 لانتهاء الصلاحية null و -1 للاستخدامات null."
        AppLanguage.PORTUGUESE -> "Use SHA256withECDSA, preferencialmente EC P-256. Assine exatamente esta estrutura JSON: ok, expiresAt, remainingUses, nonce. Use 0 para expiração null e -1 para usos restantes null."
        AppLanguage.SPANISH -> "Use SHA256withECDSA, preferiblemente EC P-256. Firme exactamente esta estructura JSON: ok, expiresAt, remainingUses, nonce. Use 0 para expiración null y -1 para usos restantes null."
        AppLanguage.FRENCH -> "Utilisez SHA256withECDSA, de préférence EC P-256. Signez exactement cette structure JSON : ok, expiresAt, remainingUses, nonce. Utilisez 0 pour expiration null et -1 pour usages restants null."
        AppLanguage.GERMAN -> "Verwenden Sie SHA256withECDSA, vorzugsweise EC P-256. Signieren Sie genau diese JSON-Struktur: ok, expiresAt, remainingUses, nonce. Verwenden Sie 0 für null Ablauf und -1 für null verbleibende Nutzungen."
        AppLanguage.RUSSIAN -> "Используйте SHA256withECDSA, предпочтительно EC P-256. Подписывайте ровно эту JSON-структуру: ok, expiresAt, remainingUses, nonce. Используйте 0 для null истечения и -1 для null оставшихся использований."
        AppLanguage.JAPANESE -> "SHA256withECDSAを使用、推奨はEC P-256。このJSON構造を正確に署名: ok、expiresAt、remainingUses、nonce。null有効期限には0、null残回数には-1を使用。"
        AppLanguage.KOREAN -> "SHA256withECDSA 사용, 권장 EC P-256. 이 JSON 구조를 정확히 서명: ok, expiresAt, remainingUses, nonce. null 만료에는 0, null 남은 사용에는 -1 사용."
    }

    val remoteActivationGuideSignatureExampleTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "签名载荷"
        AppLanguage.ENGLISH -> "Signed payload"
        AppLanguage.ARABIC -> "حمولة التوقيع"
        AppLanguage.PORTUGUESE -> "Payload assinado"
        AppLanguage.SPANISH -> "Payload firmado"
        AppLanguage.FRENCH -> "Payload signé"
        AppLanguage.GERMAN -> "Signierte Payload"
        AppLanguage.RUSSIAN -> "Подписанная payload"
        AppLanguage.JAPANESE -> "署名ペイロード"
        AppLanguage.KOREAN -> "서명된 페이로드"
    }

    val remoteActivationGuideKeysTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "4. 密钥生成"
        AppLanguage.ENGLISH -> "4. Keys"
        AppLanguage.ARABIC -> "4. المفاتيح"
        AppLanguage.PORTUGUESE -> "4. Chaves"
        AppLanguage.SPANISH -> "4. Claves"
        AppLanguage.FRENCH -> "4. Clés"
        AppLanguage.GERMAN -> "4. Schlüssel"
        AppLanguage.RUSSIAN -> "4. Ключи"
        AppLanguage.JAPANESE -> "4. 鍵"
        AppLanguage.KOREAN -> "4. 키"
    }

    val remoteActivationGuideKeysBody: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "可以用 openssl ecparam -name prime256v1 -genkey -noout -out private.pem 生成私钥，再用 openssl ec -in private.pem -pubout -out public.pem 导出公钥。把 public.pem 内容或去掉头尾后的 Base64 填到应用里。"
        AppLanguage.ENGLISH -> "Generate a private key with openssl ecparam -name prime256v1 -genkey -noout -out private.pem, then export the public key with openssl ec -in private.pem -pubout -out public.pem. Paste public.pem or its Base64 body into the app."
        AppLanguage.ARABIC -> "أنشئ مفتاحًا خاصًا بالأمر openssl ecparam -name prime256v1 -genkey -noout -out private.pem، ثم صدّر المفتاح العام بالأمر openssl ec -in private.pem -pubout -out public.pem. الصق public.pem أو جسم Base64 في التطبيق."
        AppLanguage.PORTUGUESE -> "Gere uma chave privada com openssl ecparam -name prime256v1 -genkey -noout -out private.pem, depois exporte a chave pública com openssl ec -in private.pem -pubout -out public.pem. Cole public.pem ou seu conteúdo Base64 no app."
        AppLanguage.SPANISH -> "Genere una clave privada con openssl ecparam -name prime256v1 -genkey -noout -out private.pem, luego exporte la clave pública con openssl ec -in private.pem -pubout -out public.pem. Pegue public.pem o su contenido Base64 en la app."
        AppLanguage.FRENCH -> "Générez une clé privée avec openssl ecparam -name prime256v1 -genkey -noout -out private.pem, puis exportez la clé publique avec openssl ec -in private.pem -pubout -out public.pem. Collez public.pem ou son contenu Base64 dans l'app."
        AppLanguage.GERMAN -> "Erzeugen Sie einen privaten Schlüssel mit openssl ecparam -name prime256v1 -genkey -noout -out private.pem, dann exportieren Sie den öffentlichen Schlüssel mit openssl ec -in private.pem -pubout -out public.pem. Fügen Sie public.pem oder dessen Base64-Inhalt in die App ein."
        AppLanguage.RUSSIAN -> "Сгенерируйте приватный ключ командой openssl ecparam -name prime256v1 -genkey -noout -out private.pem, затем экспортируйте публичный ключ командой openssl ec -in private.pem -pubout -out public.pem. Вставьте public.pem или его Base64-содержимое в приложение."
        AppLanguage.JAPANESE -> "openssl ecparam -name prime256v1 -genkey -noout -out private.pemで秘密鍵を生成し、openssl ec -in private.pem -pubout -out public.pemで公開鍵をエクスポートします。public.pemまたはそのBase64内容をアプリに貼り付けます。"
        AppLanguage.KOREAN -> "openssl ecparam -name prime256v1 -genkey -noout -out private.pem으로 개인 키를 생성하고, openssl ec -in private.pem -pubout -out public.pem으로 공개 키를 내보냅니다. public.pem 또는 그 Base64 내용을 앱에 붙여넣으세요."
    }

    val remoteActivationGuidePhpExampleTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "最小 PHP 示例"
        AppLanguage.ENGLISH -> "Minimal PHP example"
        AppLanguage.ARABIC -> "مثال PHP بسيط"
        AppLanguage.PORTUGUESE -> "Exemplo mínimo de PHP"
        AppLanguage.SPANISH -> "Ejemplo mínimo de PHP"
        AppLanguage.FRENCH -> "Exemple PHP minimal"
        AppLanguage.GERMAN -> "Minimales PHP-Beispiel"
        AppLanguage.RUSSIAN -> "Минимальный пример PHP"
        AppLanguage.JAPANESE -> "最小限のPHP例"
        AppLanguage.KOREAN -> "최소 PHP 예제"
    }

    val remoteActivationGuideDeployTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "5. 部署要点"
        AppLanguage.ENGLISH -> "5. Deployment"
        AppLanguage.ARABIC -> "5. النشر"
        AppLanguage.PORTUGUESE -> "5. Implantação"
        AppLanguage.SPANISH -> "5. Despliegue"
        AppLanguage.FRENCH -> "5. Déploiement"
        AppLanguage.GERMAN -> "5. Bereitstellung"
        AppLanguage.RUSSIAN -> "5. Развертывание"
        AppLanguage.JAPANESE -> "5. デプロイ"
        AppLanguage.KOREAN -> "5. 배포"
    }

    val remoteActivationGuideDeployBody: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "接口必须能被公网访问并使用 HTTPS。不要把 private.pem 放到可下载目录；按 code、deviceId、packageName 建表即可扩展吊销、设备绑定、次数扣减和到期时间。"
        AppLanguage.ENGLISH -> "The endpoint must be public HTTPS. Never place private.pem in a downloadable directory. Store code, deviceId, and packageName in a table to support revocation, device binding, usage deduction, and expiry."
        AppLanguage.ARABIC -> "يجب أن تكون نقطة النهاية HTTPS عامة. لا تضع private.pem في مجلد قابل للتنزيل. خزّن code و deviceId و packageName في جدول لدعم الإبطال وربط الجهاز وخصم الاستخدام والانتهاء."
        AppLanguage.PORTUGUESE -> "O endpoint deve ser HTTPS público. Nunca coloque private.pem em um diretório baixável. Armazene code, deviceId e packageName em uma tabela para suportar revogação, vinculação de dispositivo, dedução de uso e expiração."
        AppLanguage.SPANISH -> "El endpoint debe ser HTTPS público. Nunca coloque private.pem en un directorio descargable. Almacene code, deviceId y packageName en una tabla para soportar revocación, vinculación de dispositivo, deducción de uso y expiración."
        AppLanguage.FRENCH -> "L'endpoint doit être HTTPS public. Ne placez jamais private.pem dans un répertoire téléchargeable. Stockez code, deviceId et packageName dans une table pour supporter la révocation, le binding d'appareil, la déduction d'usage et l'expiration."
        AppLanguage.GERMAN -> "Der Endpoint muss öffentliches HTTPS sein. Platzieren Sie private.pem nie in einem herunterladbaren Verzeichnis. Speichern Sie code, deviceId und packageName in einer Tabelle, um Widerruf, Gerätebindung, Nutzungsabzug und Ablauf zu unterstützen."
        AppLanguage.RUSSIAN -> "Endpoint должен быть публичным HTTPS. Никогда не помещайте private.pem в загружаемый каталог. Храните code, deviceId и packageName в таблице для поддержки отзыва, привязки устройств, списания использований и истечения."
        AppLanguage.JAPANESE -> "エンドポイントは公開HTTPSでなければなりません。private.pemをダウンロード可能なディレクトリに置かないでください。code、deviceId、packageNameをテーブルに保存して、取り消し、デバイスバインディング、使用回数控除、有効期限をサポートします。"
        AppLanguage.KOREAN -> "엔드포인트는 공개 HTTPS여야 합니다. private.pem을 다운로드 가능한 디렉토리에 두지 마세요. code, deviceId, packageName을 테이블에 저장하여 취소, 기기 바인딩, 사용 차감, 만료를 지원합니다."
    }

    val remoteActivationOfflineLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "离线策略"
        AppLanguage.ENGLISH -> "Offline policy"
        AppLanguage.ARABIC -> "سياسة عدم الاتصال"
        AppLanguage.PORTUGUESE -> "Política offline"
        AppLanguage.SPANISH -> "Política sin conexión"
        AppLanguage.FRENCH -> "Politique hors ligne"
        AppLanguage.GERMAN -> "Offline-Richtlinie"
        AppLanguage.RUSSIAN -> "Политика офлайн"
        AppLanguage.JAPANESE -> "オフラインポリシー"
        AppLanguage.KOREAN -> "오프라인 정책"
    }

    val remoteActivationOfflineAllowCached: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "允许使用上次成功结果（推荐）"
        AppLanguage.ENGLISH -> "Allow last successful result (recommended)"
        AppLanguage.ARABIC -> "السماح بآخر نتيجة ناجحة (موصى به)"
        AppLanguage.PORTUGUESE -> "Permitir último resultado bem-sucedido (recomendado)"
        AppLanguage.SPANISH -> "Permitir último resultado exitoso (recomendado)"
        AppLanguage.FRENCH -> "Autoriser le dernier résultat réussi (recommandé)"
        AppLanguage.GERMAN -> "Letztes erfolgreiches Ergebnis zulassen (empfohlen)"
        AppLanguage.RUSSIAN -> "Разрешить последний успешный результат (рекомендуется)"
        AppLanguage.JAPANESE -> "最後の成功結果を許可（推奨）"
        AppLanguage.KOREAN -> "마지막 성공 결과 허용 (권장)"
    }

    val remoteActivationOfflineDeny: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "无网络时拒绝进入"
        AppLanguage.ENGLISH -> "Deny when offline"
        AppLanguage.ARABIC -> "الرفض عند عدم الاتصال"
        AppLanguage.PORTUGUESE -> "Negar quando offline"
        AppLanguage.SPANISH -> "Denegar sin conexión"
        AppLanguage.FRENCH -> "Refuser hors ligne"
        AppLanguage.GERMAN -> "Offline ablehnen"
        AppLanguage.RUSSIAN -> "Отклонять офлайн"
        AppLanguage.JAPANESE -> "オフライン時に拒否"
        AppLanguage.KOREAN -> "오프라인 시 거부"
    }

    val remoteActivationOfflineAllow: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "无网络时直接放行（不安全）"
        AppLanguage.ENGLISH -> "Allow when offline (insecure)"
        AppLanguage.ARABIC -> "السماح عند عدم الاتصال (غير آمن)"
        AppLanguage.PORTUGUESE -> "Permitir quando offline (inseguro)"
        AppLanguage.SPANISH -> "Permitir sin conexión (inseguro)"
        AppLanguage.FRENCH -> "Autoriser hors ligne (non sécurisé)"
        AppLanguage.GERMAN -> "Offline zulassen (unsicher)"
        AppLanguage.RUSSIAN -> "Разрешать офлайн (небезопасно)"
        AppLanguage.JAPANESE -> "オフライン時に許可（安全ではない）"
        AppLanguage.KOREAN -> "오프라인 시 허용 (안전하지 않음)"
    }

    val remoteActivationPrivacyNote: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "启用后，应用会把激活码与设备标识发送到你填写的服务器。请在隐私说明中告知用户。"
        AppLanguage.ENGLISH -> "When enabled, the app sends the activation code and a device identifier to the server you configure. Disclose this to your users."
        AppLanguage.ARABIC -> "عند التمكين، يرسل التطبيق رمز التفعيل ومعرّف الجهاز إلى الخادم الذي تقوم بتكوينه. أبلغ مستخدميك بذلك."
        AppLanguage.PORTUGUESE -> "Quando ativado, o aplicativo envia o código de ativação e um identificador do dispositivo para o servidor que você configurar. Divulgue isso aos seus usuários."
        AppLanguage.SPANISH -> "Cuando está activado, la aplicación envía el código de activación y un identificador del dispositivo al servidor que configure. Informe de esto a sus usuarios."
        AppLanguage.FRENCH -> "Lorsqu'activé, l'application envoie le code d'activation et un identifiant d'appareil au serveur que vous configurez. Informez vos utilisateurs."
        AppLanguage.GERMAN -> "Wenn aktiviert, sendet die App den Aktivierungscode und eine Gerätekennung an den von Ihnen konfigurierten Server. Weisen Sie Ihre Nutzer darauf hin."
        AppLanguage.RUSSIAN -> "Если включено, приложение отправляет код активации и идентификатор устройства на настроенный вами сервер. Сообщите об этом пользователям."
        AppLanguage.JAPANESE -> "有効にすると、アプリはアクティベーションコードとデバイス識別子を設定したサーバーに送信します。ユーザーにこれを明示してください。"
        AppLanguage.KOREAN -> "활성화 시, 앱이 활성화 코드와 기기 식별자를 구성한 서버로 전송합니다. 사용자에게 이를 알리세요."
    }

    val remoteActivationDeliverUrlTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "由服务器下发目标网址"
        AppLanguage.ENGLISH -> "Deliver target URL from server"
        AppLanguage.ARABIC -> "تسليم عنوان URL المستهدف من الخادم"
        AppLanguage.PORTUGUESE -> "Entregar URL de destino do servidor"
        AppLanguage.SPANISH -> "Entregar URL de destino desde el servidor"
        AppLanguage.FRENCH -> "Fournir l'URL cible depuis le serveur"
        AppLanguage.GERMAN -> "Ziel-URL vom Server bereitstellen"
        AppLanguage.RUSSIAN -> "Доставлять целевой URL с сервера"
        AppLanguage.JAPANESE -> "サーバーからターゲットURLを配信"
        AppLanguage.KOREAN -> "서버에서 대상 URL 전달"
    }
    val remoteActivationDeliverUrlHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "开启后，目标网址在激活成功后由验证服务器下发，无需打包进应用（网址可留空，即空壳）。服务器需在签名响应中加入 url 字段。"
        AppLanguage.ENGLISH -> "When enabled, the target URL is delivered by the verification server after successful activation instead of being packaged (the URL can be left empty). The server must include a signed url field in its response."
        AppLanguage.ARABIC -> "عند التمكين، يتم تسليم عنوان URL المستهدف من خادم التحقق بعد نجاح التفعيل بدلاً من تعبئته (يمكن ترك عنوان URL فارغاً). يجب أن يتضمن الخادم حقل url موقّعاً في استجابته."
        AppLanguage.PORTUGUESE -> "Quando ativado, o URL de destino é entregue pelo servidor de verificação após ativação bem-sucedida, em vez de ser empacotado (o URL pode ser deixado vazio). O servidor deve incluir um campo url assinado na resposta."
        AppLanguage.SPANISH -> "Cuando está activado, el servidor de verificación entrega la URL de destino tras una activación correcta, en lugar de empaquetarla (la URL puede dejarse vacía). El servidor debe incluir un campo url firmado en su respuesta."
        AppLanguage.FRENCH -> "Lorsqu'activé, l'URL cible est fournie par le serveur de vérification après une activation réussie, au lieu d'être empaquetée (l'URL peut être laissée vide). Le serveur doit inclure un champ url signé dans sa réponse."
        AppLanguage.GERMAN -> "Wenn aktiviert, wird die Ziel-URL nach erfolgreicher Aktivierung vom Verifizierungsserver bereitgestellt, statt verpackt zu werden (die URL kann leer bleiben). Der Server muss ein signiertes url-Feld in seiner Antwort enthalten."
        AppLanguage.RUSSIAN -> "Если включено, целевой URL доставляется сервером проверки после успешной активации вместо упаковки (URL можно оставить пустым). Сервер должен включать подписанное поле url в ответ."
        AppLanguage.JAPANESE -> "有効にすると、ターゲットURLはパッケージ化される代わりに、アクティベーション成功後に検証サーバーから配信されます（URLは空欄可）。サーバーは応答に署名付きのurlフィールドを含める必要があります。"
        AppLanguage.KOREAN -> "활성화 시, 대상 URL이 패키징되는 대신 활성화 성공 후 검증 서버에서 전달됩니다(URL은 비워둘 수 있음). 서버는 응답에 서명된 url 필드를 포함해야 합니다."
    }

    val remoteActivationDeviceBoundTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "设备绑定（一码一次）"
        AppLanguage.ENGLISH -> "Device binding (one-time codes)"
        AppLanguage.ARABIC -> "ربط الجهاز (رموز لمرة واحدة)"
        AppLanguage.PORTUGUESE -> "Vínculo de dispositivo (códigos únicos)"
        AppLanguage.SPANISH -> "Vinculación de dispositivo (códigos de un solo uso)"
        AppLanguage.FRENCH -> "Liaison d'appareil (codes à usage unique)"
        AppLanguage.GERMAN -> "Gerätebindung (Einmalcodes)"
        AppLanguage.RUSSIAN -> "Привязка к устройству (одноразовые коды)"
        AppLanguage.JAPANESE -> "デバイスバインディング（使い切りコード）"
        AppLanguage.KOREAN -> "기기 바인딩(일회용 코드)"
    }
    val remoteActivationDeviceBoundHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "需要远程验证。开启后，激活请求携带设备标识，由服务器执行绑定：每个码默认限 1 台设备（首个激活的设备占座），其他设备激活将被拒绝。卸载重装不换设备不受影响；清除数据/换机后需服务器释放座位。本地（离线）验证无法实现此能力。"
        AppLanguage.ENGLISH -> "Requires remote verification. The activation request carries a device identifier and the server enforces binding: each code is limited to 1 device by default (the first device to activate claims the seat); other devices are rejected. Reinstalling on the same device is unaffected; after clearing data or switching devices the server must release the seat. Not possible with local (offline) verification."
        AppLanguage.ARABIC -> "يتطلب التحقق عن بُعد. يحمل طلب التفعيل معرّف الجهاز ويقوم الخادم بالربط: كل رمز محدود بجهاز واحد افتراضياً (أول جهاز يفعّل يحجز المقعد)؛ تُرفض الأجهزة الأخرى. إعادة التثبيت على نفس الجهاز لا تتأثر؛ بعد مسح البيانات أو تغيير الجهاز يجب أن يفرغ الخادم المقعد. غير ممكن مع التحقق المحلي (دون اتصال)."
        AppLanguage.PORTUGUESE -> "Requer verificação remota. A solicitação de ativação carrega um identificador de dispositivo e o servidor impõe o vínculo: cada código é limitado a 1 dispositivo por padrão (o primeiro dispositivo a ativar reivindica a vaga); outros dispositivos são rejeitados. Reinstalar no mesmo dispositivo não é afetado; após limpar dados ou trocar de dispositivo o servidor precisa liberar a vaga. Não é possível com verificação local (offline)."
        AppLanguage.SPANISH -> "Requiere verificación remota. La solicitud de activación lleva un identificador de dispositivo y el servidor aplica la vinculación: cada código está limitado a 1 dispositivo por defecto (el primer dispositivo que activa reclama el cupo); otros dispositivos son rechazados. Reinstalar en el mismo dispositivo no se ve afectado; tras borrar datos o cambiar de dispositivo el servidor debe liberar el cupo. No es posible con verificación local (sin conexión)."
        AppLanguage.FRENCH -> "Nécessite la vérification distante. La requête d'activation transporte un identifiant d'appareil et le serveur applique la liaison : chaque code est limité à 1 appareil par défaut (le premier appareil qui active réserve la place) ; les autres appareils sont refusés. Réinstaller sur le même appareil n'a aucun effet ; après effacement des données ou changement d'appareil, le serveur doit libérer la place. Impossible avec la vérification locale (hors ligne)."
        AppLanguage.GERMAN -> "Erfordert Remote-Verifizierung. Die Aktivierungsanfrage überträgt eine Gerätekennung, der Server erzwingt die Bindung: Jeder Code ist standardmäßig auf 1 Gerät begrenzt (das erste Gerät, das aktiviert, belegt den Platz); andere Geräte werden abgelehnt. Neuinstallation auf demselben Gerät bleibt unberührt; nach Datenlöschung oder Gerätewechsel muss der Server den Platz freigeben. Mit lokaler (Offline-)Verifizierung nicht möglich."
        AppLanguage.RUSSIAN -> "Требуется удалённая проверка. Запрос активации несёт идентификатор устройства, сервер выполняет привязку: каждый код по умолчанию ограничен 1 устройством (первое активировавшее устройство занимает место); другие устройства отклоняются. Переустановка на том же устройстве не влияет; после очистки данных или смены устройства сервер должен освободить место. При локальной (офлайн) проверке невозможно."
        AppLanguage.JAPANESE -> "リモート検証が必要です。アクティベーション要求にデバイス識別子を含め、サーバー側でバインディングを強制します：各コードは既定で1台のデバイスに制限され（最初にアクティベートしたデバイスが席を確保）、他のデバイスは拒否されます。同じデバイスへの再インストールは影響しません。データ消去や機種変更後はサーバーで席を解放する必要があります。ローカル（オフライン）検証では実現できません。"
        AppLanguage.KOREAN -> "원격 검증이 필요합니다. 활성화 요청에 기기 식별자를 담아 서버가 바인딩을 강제합니다: 각 코드는 기본적으로 1대의 기기로 제한되며(최초 활성화한 기기가 자리를 차지), 다른 기기는 거부됩니다. 같은 기기에 재설치해도 영향이 없으며, 데이터 삭제나 기기 변경 후에는 서버에서 자리를 해제해야 합니다. 로컬(오프라인) 검증으로는 불가능합니다."
    }

    val remoteActivationEncryptUrlTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "加密目标网址 (AES-256-GCM)"
        AppLanguage.ENGLISH -> "Encrypt target URL (AES-256-GCM)"
        AppLanguage.ARABIC -> "تشفير عنوان URL المستهدف (AES-256-GCM)"
        AppLanguage.PORTUGUESE -> "Criptografar URL de destino (AES-256-GCM)"
        AppLanguage.SPANISH -> "Cifrar URL de destino (AES-256-GCM)"
        AppLanguage.FRENCH -> "Chiffrer l'URL cible (AES-256-GCM)"
        AppLanguage.GERMAN -> "Ziel-URL verschlüsseln (AES-256-GCM)"
        AppLanguage.RUSSIAN -> "Шифровать целевой URL (AES-256-GCM)"
        AppLanguage.JAPANESE -> "ターゲットURLを暗号化 (AES-256-GCM)"
        AppLanguage.KOREAN -> "대상 URL 암호화 (AES-256-GCM)"
    }
    val remoteActivationEncryptUrlHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "开启后，服务器须用下方 AES-256 密钥加密网址。响应中的 url 字段为 Base64(IV[12B] || 密文 || GCM_Tag[16B])。"
        AppLanguage.ENGLISH -> "When enabled, the server must encrypt the URL with the AES-256 key below. The url field in the response is Base64(IV[12B] || ciphertext || GCM tag[16B])."
        AppLanguage.ARABIC -> "عند التمكين، يجب على الخادم تشفير عنوان URL باستخدام مفتاح AES-256 أدناه. حقل url في الاستجابة هو Base64(IV[12B] || النص المشفر || علامة GCM[16B])."
        AppLanguage.PORTUGUESE -> "Quando ativado, o servidor deve criptografar a URL com a chave AES-256 abaixo. O campo url na resposta é Base64(IV[12B] || texto cifrado || tag GCM[16B])."
        AppLanguage.SPANISH -> "Cuando está activado, el servidor debe cifrar la URL con la clave AES-256 a continuación. El campo url en la respuesta es Base64(IV[12B] || texto cifrado || etiqueta GCM[16B])."
        AppLanguage.FRENCH -> "Lorsqu'activé, le serveur doit chiffrer l'URL avec la clé AES-256 ci-dessous. Le champ url dans la réponse est Base64(IV[12B] || texte chiffré || étiquette GCM[16B])."
        AppLanguage.GERMAN -> "Wenn aktiviert, muss der Server die URL mit dem AES-256-Schlüssel unten verschlüsseln. Das url-Feld in der Antwort ist Base64(IV[12B] || Chiffrat || GCM-Tag[16B])."
        AppLanguage.RUSSIAN -> "Если включено, сервер должен шифровать URL с помощью ключа AES-256 ниже. Поле url в ответе — Base64(IV[12B] || шифротекст || тег GCM[16B])."
        AppLanguage.JAPANESE -> "有効にすると、サーバーは以下のAES-256キーでURLを暗号化する必要があります。応答のurlフィールドはBase64(IV[12B] || 暗号文 || GCMタグ[16B])です。"
        AppLanguage.KOREAN -> "활성화 시, 서버는 아래 AES-256 키로 URL을 암호화해야 합니다. 응답의 url 필드는 Base64(IV[12B] || 암호문 || GCM 태그[16B])입니다."
    }
    val remoteActivationAesKeyLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "AES-256 密钥 (Base64)"
        AppLanguage.ENGLISH -> "AES-256 key (Base64)"
        AppLanguage.ARABIC -> "مفتاح AES-256 (Base64)"
        AppLanguage.PORTUGUESE -> "Chave AES-256 (Base64)"
        AppLanguage.SPANISH -> "Clave AES-256 (Base64)"
        AppLanguage.FRENCH -> "Clé AES-256 (Base64)"
        AppLanguage.GERMAN -> "AES-256-Schlüssel (Base64)"
        AppLanguage.RUSSIAN -> "Ключ AES-256 (Base64)"
        AppLanguage.JAPANESE -> "AES-256キー (Base64)"
        AppLanguage.KOREAN -> "AES-256 키 (Base64)"
    }
    val remoteActivationAesKeyHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "32 字节随机密钥的 Base64 编码。生成：openssl rand -base64 32"
        AppLanguage.ENGLISH -> "Base64 encoding of a 32-byte random key. Generate: openssl rand -base64 32"
        AppLanguage.ARABIC -> "ترميز Base64 لمفتاح عشوائي 32 بايت. للتوليد: openssl rand -base64 32"
        AppLanguage.PORTUGUESE -> "Codificação Base64 de uma chave aleatória de 32 bytes. Gerar: openssl rand -base64 32"
        AppLanguage.SPANISH -> "Codificación Base64 de una clave aleatoria de 32 bytes. Generar: openssl rand -base64 32"
        AppLanguage.FRENCH -> "Encodage Base64 d'une clé aléatoire de 32 octets. Générer : openssl rand -base64 32"
        AppLanguage.GERMAN -> "Base64-Kodierung eines 32-Byte-Zufallsschlüssels. Erzeugen: openssl rand -base64 32"
        AppLanguage.RUSSIAN -> "Base64-кодировка 32-байтового случайного ключа. Генерация: openssl rand -base64 32"
        AppLanguage.JAPANESE -> "32バイトのランダムキーのBase64エンコード。生成：openssl rand -base64 32"
        AppLanguage.KOREAN -> "32바이트 랜덤 키의 Base64 인코딩. 생성: openssl rand -base64 32"
    }
    val remoteActivationEncryptUrlNeedsKey: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "开启网址加密后必须提供 AES-256 密钥"
        AppLanguage.ENGLISH -> "AES-256 key is required when URL encryption is enabled"
        AppLanguage.ARABIC -> "مفتاح AES-256 مطلوب عند تمكين تشفير عنوان URL"
        AppLanguage.PORTUGUESE -> "A chave AES-256 é obrigatória quando a criptografia de URL está ativada"
        AppLanguage.SPANISH -> "Se requiere clave AES-256 cuando el cifrado de URL está activado"
        AppLanguage.FRENCH -> "La clé AES-256 est requise lorsque le chiffrement de l'URL est activé"
        AppLanguage.GERMAN -> "AES-256-Schlüssel ist erforderlich, wenn URL-Verschlüsselung aktiviert ist"
        AppLanguage.RUSSIAN -> "Требуется ключ AES-256 при включенном шифровании URL"
        AppLanguage.JAPANESE -> "URL暗号化が有効な場合、AES-256キーが必要です"
        AppLanguage.KOREAN -> "URL 암호화가 활성화된 경우 AES-256 키가 필요합니다"
    }
    val remoteActivationDecryptFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "服务器返回的网址解密失败，请检查 AES 密钥配置"
        AppLanguage.ENGLISH -> "Failed to decrypt the URL from the server, please check the AES key configuration"
        AppLanguage.ARABIC -> "فشل فك تشفير عنوان URL من الخادم، يرجى التحقق من تكوين مفتاح AES"
        AppLanguage.PORTUGUESE -> "Falha ao descriptografar a URL do servidor, verifique a configuração da chave AES"
        AppLanguage.SPANISH -> "Error al descifrar la URL del servidor, verifique la configuración de la clave AES"
        AppLanguage.FRENCH -> "Échec du déchiffrement de l'URL du serveur, vérifiez la configuration de la clé AES"
        AppLanguage.GERMAN -> "Fehler beim Entschlüsseln der URL vom Server, bitte die AES-Schlüsselkonfiguration prüfen"
        AppLanguage.RUSSIAN -> "Не удалось расшифровать URL с сервера, проверьте конфигурацию ключа AES"
        AppLanguage.JAPANESE -> "サーバーからのURLの復号に失敗しました。AESキー設定を確認してください"
        AppLanguage.KOREAN -> "서버에서 URL을 복호화하지 못했습니다. AES 키 구성을 확인하세요"
    }

    val remoteActivationMisconfigured: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "在线验证配置不完整"
        AppLanguage.ENGLISH -> "Online verification is not configured correctly"
        AppLanguage.ARABIC -> "لم يتم تكوين التحقق عبر الإنترنت بشكل صحيح"
        AppLanguage.PORTUGUESE -> "A verificação online não está configurada corretamente"
        AppLanguage.SPANISH -> "La verificación en línea no está configurada correctamente"
        AppLanguage.FRENCH -> "La vérification en ligne n'est pas configurée correctement"
        AppLanguage.GERMAN -> "Die Online-Verifizierung ist nicht korrekt konfiguriert"
        AppLanguage.RUSSIAN -> "Онлайн-проверка настроена некорректно"
        AppLanguage.JAPANESE -> "オンライン認証が正しく構成されていません"
        AppLanguage.KOREAN -> "온라인 인증이 올바르게 구성되지 않았습니다"
    }

    val remoteActivationInsecureUrl: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "验证接口必须使用 HTTPS"
        AppLanguage.ENGLISH -> "Verification endpoint must use HTTPS"
        AppLanguage.ARABIC -> "يجب أن تستخدم نقطة نهاية التحقق HTTPS"
        AppLanguage.PORTUGUESE -> "O endpoint de verificação deve usar HTTPS"
        AppLanguage.SPANISH -> "El endpoint de verificación debe usar HTTPS"
        AppLanguage.FRENCH -> "Le point de terminaison de vérification doit utiliser HTTPS"
        AppLanguage.GERMAN -> "Der Verifizierungs-Endpoint muss HTTPS verwenden"
        AppLanguage.RUSSIAN -> "Endpoint проверки должен использовать HTTPS"
        AppLanguage.JAPANESE -> "検証エンドポイントはHTTPSを使用する必要があります"
        AppLanguage.KOREAN -> "검증 엔드포인트는 HTTPS를 사용해야 합니다"
    }

    val remoteActivationRejected: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "服务器拒绝了此激活码"
        AppLanguage.ENGLISH -> "The server rejected this activation code"
        AppLanguage.ARABIC -> "رفض الخادم رمز التفعيل هذا"
        AppLanguage.PORTUGUESE -> "O servidor rejeitou este código de ativação"
        AppLanguage.SPANISH -> "El servidor rechazó este código de activación"
        AppLanguage.FRENCH -> "Le serveur a rejeté ce code d'activation"
        AppLanguage.GERMAN -> "Der Server hat diesen Aktivierungscode abgelehnt"
        AppLanguage.RUSSIAN -> "Сервер отклонил этот код активации"
        AppLanguage.JAPANESE -> "サーバーがこのアクティベーションコードを拒否しました"
        AppLanguage.KOREAN -> "서버가 이 활성화 코드를 거부했습니다"
    }

    val remoteActivationSignatureFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "服务器响应验签失败"
        AppLanguage.ENGLISH -> "Server response signature verification failed"
        AppLanguage.ARABIC -> "فشل التحقق من توقيع استجابة الخادم"
        AppLanguage.PORTUGUESE -> "Falha na verificação da assinatura da resposta do servidor"
        AppLanguage.SPANISH -> "Falló la verificación de la firma de la respuesta del servidor"
        AppLanguage.FRENCH -> "Échec de la vérification de la signature de la réponse du serveur"
        AppLanguage.GERMAN -> "Verifizierung der Signatur der Serverantwort fehlgeschlagen"
        AppLanguage.RUSSIAN -> "Сбой проверки подписи ответа сервера"
        AppLanguage.JAPANESE -> "サーバー応答の署名検証に失敗しました"
        AppLanguage.KOREAN -> "서버 응답 서명 검증에 실패했습니다"
    }

    val remoteActivationOfflineDenied: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "无法连接验证服务器，请检查网络"
        AppLanguage.ENGLISH -> "Cannot reach the verification server, please check your network"
        AppLanguage.ARABIC -> "تعذّر الوصول إلى خادم التحقق، يرجى التحقق من اتصالك"
        AppLanguage.PORTUGUESE -> "Não é possível acessar o servidor de verificação, verifique sua rede"
        AppLanguage.SPANISH -> "No se puede acceder al servidor de verificación, compruebe su red"
        AppLanguage.FRENCH -> "Impossible de joindre le serveur de vérification, veuillez vérifier votre réseau"
        AppLanguage.GERMAN -> "Der Verifizierungsserver ist nicht erreichbar, bitte überprüfen Sie Ihr Netzwerk"
        AppLanguage.RUSSIAN -> "Не удается связаться с сервером проверки, проверьте сеть"
        AppLanguage.JAPANESE -> "検証サーバーに接続できません。ネットワークを確認してください"
        AppLanguage.KOREAN -> "검증 서버에 연결할 수 없습니다. 네트워크를 확인하세요"
    }

    val remoteActivationOfflineNoCache: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "无法连接验证服务器，且没有可用的离线凭据"
        AppLanguage.ENGLISH -> "Cannot reach the verification server and no offline credential is available"
        AppLanguage.ARABIC -> "تعذّر الوصول إلى خادم التحقق ولا يتوفر اعتماد دون اتصال"
        AppLanguage.PORTUGUESE -> "Não é possível acessar o servidor de verificação e nenhuma credencial offline está disponível"
        AppLanguage.SPANISH -> "No se puede acceder al servidor de verificación y no hay credencial sin conexión disponible"
        AppLanguage.FRENCH -> "Impossible de joindre le serveur de vérification et aucune crédentielle hors ligne n'est disponible"
        AppLanguage.GERMAN -> "Der Verifizierungsserver ist nicht erreichbar und keine Offline-Referenz ist verfügbar"
        AppLanguage.RUSSIAN -> "Не удается связаться с сервером проверки, и нет доступных офлайн-учетных данных"
        AppLanguage.JAPANESE -> "検証サーバーに接続できず、オフライン資格情報も利用できません"
        AppLanguage.KOREAN -> "검증 서버에 연결할 수 없으며 오프라인 자격 증명도 없습니다"
    }


    val activationCodeBoundToOtherDevice: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "此激活码已绑定到其他设备"
        AppLanguage.ENGLISH -> "This activation code is bound to another device"
        AppLanguage.ARABIC -> "رمز التفعيل هذا مرتبط بجهاز آخر"
        AppLanguage.PORTUGUESE -> "Este código de ativação está vinculado a outro dispositivo"
        AppLanguage.SPANISH -> "Este código de activación está vinculado a otro dispositivo"
        AppLanguage.FRENCH -> "Ce code d'activation est lié à un autre appareil"
        AppLanguage.GERMAN -> "Dieser Aktivierungscode ist an ein anderes Gerät gebunden"
        AppLanguage.RUSSIAN -> "Этот код активации привязан к другому устройству"
        AppLanguage.JAPANESE -> "このアクティベーションコードは別のデバイスにバインドされています"
        AppLanguage.KOREAN -> "이 활성화 코드는 다른 기기에 바인딩되어 있습니다"
    }

    val activationCodeExpired: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Activation code expired"
        AppLanguage.ENGLISH -> "Activation code expired"
        AppLanguage.ARABIC -> "انتهت صلاحية رمز التفعيل"
        AppLanguage.PORTUGUESE -> "Código de ativação expirado"
        AppLanguage.SPANISH -> "Código de activación caducado"
        AppLanguage.FRENCH -> "Code d'activation expiré"
        AppLanguage.GERMAN -> "Aktivierungscode abgelaufen"
        AppLanguage.RUSSIAN -> "Срок действия кода активации истёк"
        AppLanguage.JAPANESE -> "アクティベーションコードの有効期限が切れました"
        AppLanguage.KOREAN -> "활성화 코드가 만료되었습니다"
    }

    val activationCodeUsageExceeded: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "激活码使用次数已用完"
        AppLanguage.ENGLISH -> "Activation code usage exceeded"
        AppLanguage.ARABIC -> "تم تجاوز استخدام رمز التفعيل"
        AppLanguage.PORTUGUESE -> "Uso do código de ativação excedido"
        AppLanguage.SPANISH -> "Uso del código de activación excedido"
        AppLanguage.FRENCH -> "Utilisation du code d'activation dépassée"
        AppLanguage.GERMAN -> "Nutzung des Aktivierungscodes überschritten"
        AppLanguage.RUSSIAN -> "Превышено использование кода активации"
        AppLanguage.JAPANESE -> "アクティベーションコードの使用回数を超過しました"
        AppLanguage.KOREAN -> "활성화 코드 사용 한도를 초과했습니다"
    }

    val appAlreadyActivated: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "应用已激活"
        AppLanguage.ENGLISH -> "App already activated"
        AppLanguage.ARABIC -> "التطبيق مفعل بالفعل"
        AppLanguage.PORTUGUESE -> "Aplicativo já ativado"
        AppLanguage.SPANISH -> "Aplicación ya activada"
        AppLanguage.FRENCH -> "Application déjà activée"
        AppLanguage.GERMAN -> "App bereits aktiviert"
        AppLanguage.RUSSIAN -> "Приложение уже активировано"
        AppLanguage.JAPANESE -> "アプリは既にアクティベート済みです"
        AppLanguage.KOREAN -> "앱이 이미 활성화되었습니다"
    }

    val activationSuccessHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "激活码验证通过，即将进入应用"
        AppLanguage.ENGLISH -> "Activation code verified, entering app shortly"
        AppLanguage.ARABIC -> "تم التحقق من رمز التفعيل، جاري الدخول إلى التطبيق"
        AppLanguage.PORTUGUESE -> "Código de ativação verificado, entrando no aplicativo em instantes"
        AppLanguage.SPANISH -> "Código de activación verificado, entrando en la aplicación en breve"
        AppLanguage.FRENCH -> "Code d'activation vérifié, entrée dans l'application dans un instant"
        AppLanguage.GERMAN -> "Aktivierungscode verifiziert, App wird in Kürze gestartet"
        AppLanguage.RUSSIAN -> "Код активации проверен, вход в приложение через мгновение"
        AppLanguage.JAPANESE -> "アクティベーションコードが確認されました。まもなくアプリに入ります"
        AppLanguage.KOREAN -> "활성화 코드가 확인되었습니다. 곧 앱에 진입합니다"
    }

    val activationSuccessDetail: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "您的激活码已成功验证，应用功能已解锁"
        AppLanguage.ENGLISH -> "Your activation code has been verified successfully. App features unlocked."
        AppLanguage.ARABIC -> "تم التحقق من رمز التفعيل بنجاح. تم فتح ميزات التطبيق."
        AppLanguage.PORTUGUESE -> "Seu código de ativação foi verificado com sucesso. Recursos do aplicativo desbloqueados."
        AppLanguage.SPANISH -> "Su código de activación se ha verificado con éxito. Funciones de la aplicación desbloqueadas."
        AppLanguage.FRENCH -> "Votre code d'activation a été vérifié avec succès. Fonctionnalités de l'application déverrouillées."
        AppLanguage.GERMAN -> "Ihr Aktivierungscode wurde erfolgreich verifiziert. App-Funktionen freigeschaltet."
        AppLanguage.RUSSIAN -> "Ваш код активации успешно проверен. Функции приложения разблокированы."
        AppLanguage.JAPANESE -> "アクティベーションコードが正常に確認されました。アプリの機能がアンロックされました。"
        AppLanguage.KOREAN -> "활성화 코드가 성공적으로 확인되었습니다. 앱 기능이 잠금 해제되었습니다."
    }

    val appAlreadyActivatedHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "此应用已使用有效激活码激活"
        AppLanguage.ENGLISH -> "This app has already been activated with a valid code"
        AppLanguage.ARABIC -> "تم تفعيل هذا التطبيق بالفعل برمز صالح"
        AppLanguage.PORTUGUESE -> "Este aplicativo já foi ativado com um código válido"
        AppLanguage.SPANISH -> "Esta aplicación ya ha sido activada con un código válido"
        AppLanguage.FRENCH -> "Cette application a déjà été activée avec un code valide"
        AppLanguage.GERMAN -> "Diese App wurde bereits mit einem gültigen Code aktiviert"
        AppLanguage.RUSSIAN -> "Это приложение уже активировано действительным кодом"
        AppLanguage.JAPANESE -> "このアプリは有効なコードで既にアクティベートされています"
        AppLanguage.KOREAN -> "이 앱은 유효한 코드로 이미 활성화되었습니다"
    }

    val alreadyActivatedDetail: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "无需重复激活，您可以直接使用应用的全部功能"
        AppLanguage.ENGLISH -> "No need to activate again, you can use all app features directly"
        AppLanguage.ARABIC -> "لا حاجة للتفعيل مرة أخرى، يمكنك استخدام جميع ميزات التطبيق مباشرة"
        AppLanguage.PORTUGUESE -> "Não é necessário ativar novamente, você pode usar todos os recursos do aplicativo diretamente"
        AppLanguage.SPANISH -> "No es necesario activar de nuevo, puede usar todas las funciones de la aplicación directamente"
        AppLanguage.FRENCH -> "Pas besoin d'activer à nouveau, vous pouvez utiliser toutes les fonctionnalités de l'application directement"
        AppLanguage.GERMAN -> "Erneute Aktivierung nicht nötig, Sie können alle App-Funktionen direkt nutzen"
        AppLanguage.RUSSIAN -> "Повторная активация не требуется, вы можете использовать все функции приложения напрямую"
        AppLanguage.JAPANESE -> "再アクティベートの必要はありません。すべてのアプリ機能を直接使用できます"
        AppLanguage.KOREAN -> "다시 활성화할 필요 없이 모든 앱 기능을 직접 사용할 수 있습니다"
    }

    val invalidCodeSuggestion: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "请检查激活码是否正确，注意区分大小写。如多次失败将暂时锁定输入。"
        AppLanguage.ENGLISH -> "Please check if the activation code is correct. Note it is case-insensitive. Too many failures will temporarily lock input."
        AppLanguage.ARABIC -> "يرجى التحقق مما إذا كان رمز التفعيل صحيحًا. الفشل المتكرر سيقفل الإدخال مؤقتًا."
        AppLanguage.PORTUGUESE -> "Verifique se o código de ativação está correto. Observe que não diferencia maiúsculas de minúsculas. Muitas falhas bloquearão temporariamente a entrada."
        AppLanguage.SPANISH -> "Compruebe si el código de activación es correcto. Tenga en cuenta que no distingue mayúsculas de minúsculas. Demasiados fallos bloquearán temporalmente la entrada."
        AppLanguage.FRENCH -> "Veuillez vérifier si le code d'activation est correct. Notez qu'il est insensible à la casse. Trop d'échecs verrouilleront temporairement la saisie."
        AppLanguage.GERMAN -> "Bitte prüfen Sie, ob der Aktivierungscode korrekt ist. Beachten Sie, dass er Groß-/Kleinschreibung nicht unterscheidet. Zu viele Fehlversuche sperren die Eingabe vorübergehend."
        AppLanguage.RUSSIAN -> "Проверьте правильность кода активации. Он нечувствителен к регистру. Слишком много неудач временно заблокирует ввод."
        AppLanguage.JAPANESE -> "アクティベーションコードが正しいか確認してください。大文字小文字は区別しません。失敗が多すぎると入力が一時的にロックされます。"
        AppLanguage.KOREAN -> "활성화 코드가 올바른지 확인하세요. 대소문자를 구분하지 않습니다. 실패가 너무 많으면 입력이 일시적으로 잠깁니다."
    }

    val deviceMismatchDetail: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "此激活码已绑定到其他设备，无法在当前设备上使用"
        AppLanguage.ENGLISH -> "This activation code is bound to another device and cannot be used on this device"
        AppLanguage.ARABIC -> "رمز التفعيل هذا مرتبط بجهاز آخر ولا يمكن استخدامه على هذا الجهاز"
        AppLanguage.PORTUGUESE -> "Este código de ativação está vinculado a outro dispositivo e não pode ser usado neste dispositivo"
        AppLanguage.SPANISH -> "Este código de activación está vinculado a otro dispositivo y no puede usarse en este dispositivo"
        AppLanguage.FRENCH -> "Ce code d'activation est lié à un autre appareil et ne peut être utilisé sur cet appareil"
        AppLanguage.GERMAN -> "Dieser Aktivierungscode ist an ein anderes Gerät gebunden und kann auf diesem Gerät nicht verwendet werden"
        AppLanguage.RUSSIAN -> "Этот код активации привязан к другому устройству и не может быть использован на этом устройстве"
        AppLanguage.JAPANESE -> "このアクティベーションコードは別のデバイスにバインドされており、このデバイスでは使用できません"
        AppLanguage.KOREAN -> "이 활성화 코드는 다른 기기에 바인딩되어 이 기기에서 사용할 수 없습니다"
    }

    val deviceMismatchSuggestion: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "请使用绑定设备登录，或联系开发者获取新的设备绑定激活码。"
        AppLanguage.ENGLISH -> "Please use the bound device, or contact the developer for a new device-bound activation code."
        AppLanguage.ARABIC -> "يرجى استخدام الجهاز المرتبط، أو الاتصال بالمطور للحصول على رمز تفعيل جديد مرتبط بالجهاز."
        AppLanguage.PORTUGUESE -> "Use o dispositivo vinculado ou entre em contato com o desenvolvedor para obter um novo código de ativação vinculado ao dispositivo."
        AppLanguage.SPANISH -> "Use el dispositivo vinculado o contacte con el desarrollador para obtener un nuevo código de activación vinculado al dispositivo."
        AppLanguage.FRENCH -> "Veuillez utiliser l'appareil lié, ou contacter le développeur pour obtenir un nouveau code d'activation lié à l'appareil."
        AppLanguage.GERMAN -> "Bitte verwenden Sie das gebundene Gerät, oder kontaktieren Sie den Entwickler für einen neuen gerätegebundenen Aktivierungscode."
        AppLanguage.RUSSIAN -> "Используйте привязанное устройство или свяжитесь с разработчиком для получения нового кода активации, привязанного к устройству."
        AppLanguage.JAPANESE -> "バインドされたデバイスを使用するか、デバイスバインドされた新しいアクティベーションコードについて開発者にお問い合わせください。"
        AppLanguage.KOREAN -> "바인딩된 기기를 사용하거나, 기기 바인딩된 새 활성화 코드를 위해 개발자에게 문의하세요."
    }

    val expiredDetail: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "此激活码的有效期已过，无法继续使用"
        AppLanguage.ENGLISH -> "This activation code has expired and can no longer be used"
        AppLanguage.ARABIC -> "انتهت صلاحية رمز التفعيل هذا ولا يمكن استخدامه بعد الآن"
        AppLanguage.PORTUGUESE -> "Este código de ativação expirou e não pode mais ser usado"
        AppLanguage.SPANISH -> "Este código de activación ha caducado y ya no puede usarse"
        AppLanguage.FRENCH -> "Ce code d'activation a expiré et ne peut plus être utilisé"
        AppLanguage.GERMAN -> "Dieser Aktivierungscode ist abgelaufen und kann nicht mehr verwendet werden"
        AppLanguage.RUSSIAN -> "Срок действия этого кода активации истёк, и он больше не может быть использован"
        AppLanguage.JAPANESE -> "このアクティベーションコードの有効期限が切れ、使用できなくなりました"
        AppLanguage.KOREAN -> "이 활성화 코드가 만료되어 더 이상 사용할 수 없습니다"
    }

    val expiredSuggestion: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "请联系开发者获取新的激活码，或续期当前激活码。"
        AppLanguage.ENGLISH -> "Please contact the developer for a new activation code or to renew the current one."
        AppLanguage.ARABIC -> "يرجى الاتصال بالمطور للحصول على رمز تفعيل جديد أو لتجديد الرمز الحالي."
        AppLanguage.PORTUGUESE -> "Entre em contato com o desenvolvedor para obter um novo código de ativação ou renovar o atual."
        AppLanguage.SPANISH -> "Contacte con el desarrollador para obtener un nuevo código de activación o renovar el actual."
        AppLanguage.FRENCH -> "Veuillez contacter le développeur pour obtenir un nouveau code d'activation ou renouveler celui actuel."
        AppLanguage.GERMAN -> "Bitte kontaktieren Sie den Entwickler für einen neuen Aktivierungscode oder zur Verlängerung des aktuellen."
        AppLanguage.RUSSIAN -> "Свяжитесь с разработчиком для получения нового кода активации или продления текущего."
        AppLanguage.JAPANESE -> "新しいアクティベーションコードの取得、または現在のコードの更新について開発者にお問い合わせください。"
        AppLanguage.KOREAN -> "새 활성화 코드를 받거나 현재 코드를 갱신하려면 개발자에게 문의하세요."
    }

    val usageExceededDetail: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "此激活码的可用次数已全部用完"
        AppLanguage.ENGLISH -> "This activation code has used all its available uses"
        AppLanguage.ARABIC -> "استنفد رمز التفعيل هذا جميع استخداماته المتاحة"
        AppLanguage.PORTUGUESE -> "Este código de ativação esgotou todos os seus usos disponíveis"
        AppLanguage.SPANISH -> "Este código de activación ha agotado todos sus usos disponibles"
        AppLanguage.FRENCH -> "Ce code d'activation a épuisé toutes ses utilisations disponibles"
        AppLanguage.GERMAN -> "Dieser Aktivierungscode hat alle verfügbaren Nutzungen aufgebraucht"
        AppLanguage.RUSSIAN -> "Этот код активации исчерпал все доступные использования"
        AppLanguage.JAPANESE -> "このアクティベーションコードは利用可能な使用回数を使い切りました"
        AppLanguage.KOREAN -> "이 활성화 코드는 사용 가능한 횟수를 모두 소진했습니다"
    }

    val usageExceededSuggestion: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "请联系开发者获取新的激活码以继续使用。"
        AppLanguage.ENGLISH -> "Please contact the developer for a new activation code to continue using."
        AppLanguage.ARABIC -> "يرجى الاتصال بالمطور للحصول على رمز تفعيل جديد للمتابعة في الاستخدام."
        AppLanguage.PORTUGUESE -> "Entre em contato com o desenvolvedor para obter um novo código de ativação e continuar usando."
        AppLanguage.SPANISH -> "Contacte con el desarrollador para obtener un nuevo código de activación y continuar usando."
        AppLanguage.FRENCH -> "Veuillez contacter le développeur pour obtenir un nouveau code d'activation afin de continuer à utiliser."
        AppLanguage.GERMAN -> "Bitte kontaktieren Sie den Entwickler für einen neuen Aktivierungscode, um die Nutzung fortzusetzen."
        AppLanguage.RUSSIAN -> "Свяжитесь с разработчиком для получения нового кода активации, чтобы продолжить использование."
        AppLanguage.JAPANESE -> "引き続き使用するには、新しいアクティベーションコードについて開発者にお問い合わせください。"
        AppLanguage.KOREAN -> "계속 사용하려면 개발자에게 새 활성화 코드를 문의하세요."
    }

    val batchGenerate: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "批量生成"
        AppLanguage.ENGLISH -> "Batch Generate"
        AppLanguage.ARABIC -> "إنشاء دفعي"
        AppLanguage.PORTUGUESE -> "Geração em Lote"
        AppLanguage.SPANISH -> "Generación por Lotes"
        AppLanguage.FRENCH -> "Génération par Lot"
        AppLanguage.GERMAN -> "Stapelgenerierung"
        AppLanguage.RUSSIAN -> "Пакетная генерация"
        AppLanguage.JAPANESE -> "一括生成"
        AppLanguage.KOREAN -> "일괄 생성"
    }

    val batchCount: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "生成数量"
        AppLanguage.ENGLISH -> "Count"
        AppLanguage.ARABIC -> "العدد"
        AppLanguage.PORTUGUESE -> "Quantidade"
        AppLanguage.SPANISH -> "Cantidad"
        AppLanguage.FRENCH -> "Nombre"
        AppLanguage.GERMAN -> "Anzahl"
        AppLanguage.RUSSIAN -> "Количество"
        AppLanguage.JAPANESE -> "数量"
        AppLanguage.KOREAN -> "수량"
    }

    val batchImport: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "批量导入"
        AppLanguage.ENGLISH -> "Batch Import"
        AppLanguage.ARABIC -> "استيراد دفعي"
        AppLanguage.PORTUGUESE -> "Importação em Lote"
        AppLanguage.SPANISH -> "Importación por Lotes"
        AppLanguage.FRENCH -> "Importation par Lot"
        AppLanguage.GERMAN -> "Stapelimport"
        AppLanguage.RUSSIAN -> "Пакетный импорт"
        AppLanguage.JAPANESE -> "一括インポート"
        AppLanguage.KOREAN -> "일괄 가져오기"
    }

    val batchImportCodes: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "批量导入激活码"
        AppLanguage.ENGLISH -> "Batch Import Codes"
        AppLanguage.ARABIC -> "استيراد رموز التفعيل دفعةً"
        AppLanguage.PORTUGUESE -> "Importar Códigos em Lote"
        AppLanguage.SPANISH -> "Importar Códigos por Lotes"
        AppLanguage.FRENCH -> "Importer les Codes par Lot"
        AppLanguage.GERMAN -> "Codes stapelweise importieren"
        AppLanguage.RUSSIAN -> "Пакетный импорт кодов"
        AppLanguage.JAPANESE -> "コードを一括インポート"
        AppLanguage.KOREAN -> "코드 일괄 가져오기"
    }

    val batchImportCodesHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "每行粘贴一个激活码，一行一个。空行、重复和已存在的激活码会自动忽略。"
        AppLanguage.ENGLISH -> "Paste one activation code per line. Blank lines, duplicates, and codes that already exist are skipped automatically."
        AppLanguage.ARABIC -> "الصق رمز تفعيل واحد في كل سطر. يتم تجاهل الأسطر الفارغة والمكررة والرموز الموجودة تلقائيًا."
        AppLanguage.PORTUGUESE -> "Cole um código de ativação por linha. Linhas em branco, duplicatas e códigos já existentes são ignorados automaticamente."
        AppLanguage.SPANISH -> "Pegue un código de activación por línea. Las líneas en blanco, los duplicados y los códigos ya existentes se omiten automáticamente."
        AppLanguage.FRENCH -> "Collez un code d'activation par ligne. Les lignes vides, les doublons et les codes déjà existants sont ignorés automatiquement."
        AppLanguage.GERMAN -> "Fügen Sie einen Aktivierungscode pro Zeile ein. Leerzeilen, Duplikate und bereits vorhandene Codes werden automatisch übersprungen."
        AppLanguage.RUSSIAN -> "Вставляйте по одному коду активации на строку. Пустые строки, дубликаты и уже существующие коды пропускаются автоматически."
        AppLanguage.JAPANESE -> "1行に1つのアクティベーションコードを貼り付けてください。空行、重複、既存のコードは自動的にスキップされます。"
        AppLanguage.KOREAN -> "한 줄에 하나의 활성화 코드를 붙여넣으세요. 빈 줄, 중복 및 이미 존재하는 코드는 자동으로 건너뜁니다."
    }

    val batchImportInputLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "激活码列表（每行一个）"
        AppLanguage.ENGLISH -> "Activation codes (one per line)"
        AppLanguage.ARABIC -> "رموز التفعيل (واحد في كل سطر)"
        AppLanguage.PORTUGUESE -> "Códigos de ativação (um por linha)"
        AppLanguage.SPANISH -> "Códigos de activación (uno por línea)"
        AppLanguage.FRENCH -> "Codes d'activation (un par ligne)"
        AppLanguage.GERMAN -> "Aktivierungscodes (einer pro Zeile)"
        AppLanguage.RUSSIAN -> "Коды активации (по одному на строку)"
        AppLanguage.JAPANESE -> "アクティベーションコード（1行に1つ）"
        AppLanguage.KOREAN -> "활성화 코드 (한 줄에 하나)"
    }

    val batchImportEmpty: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "未识别到有效的激活码"
        AppLanguage.ENGLISH -> "No valid activation codes found"
        AppLanguage.ARABIC -> "لم يتم العثور على رموز تفعيل صالحة"
        AppLanguage.PORTUGUESE -> "Nenhum código de ativação válido encontrado"
        AppLanguage.SPANISH -> "No se encontraron códigos de activación válidos"
        AppLanguage.FRENCH -> "Aucun code d'activation valide trouvé"
        AppLanguage.GERMAN -> "Keine gültigen Aktivierungscodes gefunden"
        AppLanguage.RUSSIAN -> "Действительные коды активации не найдены"
        AppLanguage.JAPANESE -> "有効なアクティベーションコードが見つかりません"
        AppLanguage.KOREAN -> "유효한 활성화 코드를 찾을 수 없습니다"
    }

    fun batchImportResult(added: Int, skipped: Int): String = when (Strings.lang) {
        AppLanguage.CHINESE -> "已导入 $added 个，跳过 $skipped 个"
        AppLanguage.ENGLISH -> "Imported $added, skipped $skipped"
        AppLanguage.ARABIC -> "تم استيراد $added، وتم تخطي $skipped"
        AppLanguage.PORTUGUESE -> "Importados $added, ignorados $skipped"
        AppLanguage.SPANISH -> "Importados $added, omitidos $skipped"
        AppLanguage.FRENCH -> "Importés $added, ignorés $skipped"
        AppLanguage.GERMAN -> "Importiert $added, übersprungen $skipped"
        AppLanguage.RUSSIAN -> "Импортировано $added, пропущено $skipped"
        AppLanguage.JAPANESE -> "${added}個インポート、${skipped}個スキップ"
        AppLanguage.KOREAN -> "${added}개 가져옴, ${skipped}개 건너뜀"
    }

    val batchImportNote: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "批量导入"
        AppLanguage.ENGLISH -> "Batch import"
        AppLanguage.ARABIC -> "استيراد دفعي"
        AppLanguage.PORTUGUESE -> "Importação em lote"
        AppLanguage.SPANISH -> "Importación por lotes"
        AppLanguage.FRENCH -> "Importation par lot"
        AppLanguage.GERMAN -> "Stapelimport"
        AppLanguage.RUSSIAN -> "Пакетный импорт"
        AppLanguage.JAPANESE -> "一括インポート"
        AppLanguage.KOREAN -> "일괄 가져오기"
    }

    val deleteAllCodes: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "清空全部"
        AppLanguage.ENGLISH -> "Delete All"
        AppLanguage.ARABIC -> "حذف الكل"
        AppLanguage.PORTUGUESE -> "Excluir Tudo"
        AppLanguage.SPANISH -> "Eliminar Todo"
        AppLanguage.FRENCH -> "Tout supprimer"
        AppLanguage.GERMAN -> "Alle löschen"
        AppLanguage.RUSSIAN -> "Удалить все"
        AppLanguage.JAPANESE -> "すべて削除"
        AppLanguage.KOREAN -> "모두 삭제"
    }

    val deleteAllCodesConfirm: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "确定清空所有激活码吗？此操作不可撤销。"
        AppLanguage.ENGLISH -> "Delete all activation codes? This action cannot be undone."
        AppLanguage.ARABIC -> "هل تريد حذف جميع رموز التفعيل؟ لا يمكن التراجع عن هذا الإجراء."
        AppLanguage.PORTUGUESE -> "Excluir todos os códigos de ativação? Esta ação não pode ser desfeita."
        AppLanguage.SPANISH -> "¿Eliminar todos los códigos de activación? Esta acción no se puede deshacer."
        AppLanguage.FRENCH -> "Supprimer tous les codes d'activation ? Cette action est irréversible."
        AppLanguage.GERMAN -> "Alle Aktivierungscodes löschen? Diese Aktion kann nicht rückgängig gemacht werden."
        AppLanguage.RUSSIAN -> "Удалить все коды активации? Это действие нельзя отменить."
        AppLanguage.JAPANESE -> "すべてのアクティベーションコードを削除しますか？この操作は元に戻せません。"
        AppLanguage.KOREAN -> "모든 활성화 코드를 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다."
    }

    val copyAllCodes: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "复制全部"
        AppLanguage.ENGLISH -> "Copy All"
        AppLanguage.ARABIC -> "نسخ الكل"
        AppLanguage.PORTUGUESE -> "Copiar Tudo"
        AppLanguage.SPANISH -> "Copiar Todo"
        AppLanguage.FRENCH -> "Tout copier"
        AppLanguage.GERMAN -> "Alle kopieren"
        AppLanguage.RUSSIAN -> "Скопировать все"
        AppLanguage.JAPANESE -> "すべてコピー"
        AppLanguage.KOREAN -> "모두 복사"
    }

    val pleaseEnterActivationCode: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "请输入激活码"
        AppLanguage.ENGLISH -> "Please enter activation code"
        AppLanguage.ARABIC -> "يرجى إدخال رمز التفعيل"
        AppLanguage.PORTUGUESE -> "Por favor, insira o código de ativação"
        AppLanguage.SPANISH -> "Por favor, introduzca el código de activación"
        AppLanguage.FRENCH -> "Veuillez saisir le code d'activation"
        AppLanguage.GERMAN -> "Bitte Aktivierungscode eingeben"
        AppLanguage.RUSSIAN -> "Пожалуйста, введите код активации"
        AppLanguage.JAPANESE -> "アクティベーションコードを入力してください"
        AppLanguage.KOREAN -> "활성화 코드를 입력하세요"
    }

    val permanentValid: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "永久有效"
        AppLanguage.ENGLISH -> "Permanently valid"
        AppLanguage.ARABIC -> "صالح بشكل دائم"
        AppLanguage.PORTUGUESE -> "Válido permanentemente"
        AppLanguage.SPANISH -> "Válido permanentemente"
        AppLanguage.FRENCH -> "Valide en permanence"
        AppLanguage.GERMAN -> "Dauerhaft gültig"
        AppLanguage.RUSSIAN -> "Действует бессрочно"
        AppLanguage.JAPANESE -> "永久有効"
        AppLanguage.KOREAN -> "영구 유효"
    }

    val validityPeriod: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "有效期"
        AppLanguage.ENGLISH -> "Validity Period"
        AppLanguage.ARABIC -> "فترة الصلاحية"
        AppLanguage.PORTUGUESE -> "Período de validade"
        AppLanguage.SPANISH -> "Período de validez"
        AppLanguage.FRENCH -> "Période de validité"
        AppLanguage.GERMAN -> "Gültigkeitszeitraum"
        AppLanguage.RUSSIAN -> "Срок действия"
        AppLanguage.JAPANESE -> "有効期間"
        AppLanguage.KOREAN -> "유효 기간"
    }

    val days: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "天"
        AppLanguage.ENGLISH -> "days"
        AppLanguage.ARABIC -> "أيام"
        AppLanguage.PORTUGUESE -> "dias"
        AppLanguage.SPANISH -> "días"
        AppLanguage.FRENCH -> "jours"
        AppLanguage.GERMAN -> "Tage"
        AppLanguage.RUSSIAN -> "дн."
        AppLanguage.JAPANESE -> "日"
        AppLanguage.KOREAN -> "일"
    }

    val hours: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "小时"
        AppLanguage.ENGLISH -> "hours"
        AppLanguage.ARABIC -> "ساعات"
        AppLanguage.PORTUGUESE -> "horas"
        AppLanguage.SPANISH -> "horas"
        AppLanguage.FRENCH -> "heures"
        AppLanguage.GERMAN -> "Stunden"
        AppLanguage.RUSSIAN -> "ч."
        AppLanguage.JAPANESE -> "時間"
        AppLanguage.KOREAN -> "시간"
    }

    val times: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "次"
        AppLanguage.ENGLISH -> "times"
        AppLanguage.ARABIC -> "مرات"
        AppLanguage.PORTUGUESE -> "vezes"
        AppLanguage.SPANISH -> "veces"
        AppLanguage.FRENCH -> "fois"
        AppLanguage.GERMAN -> "Mal"
        AppLanguage.RUSSIAN -> "раз"
        AppLanguage.JAPANESE -> "回"
        AppLanguage.KOREAN -> "회"
    }

    val cloneInstallWarning: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "克隆安装仅适用于无签名校验的应用，兼容性较差。建议优先使用「快捷方式」功能。"
        AppLanguage.ENGLISH -> "Clone install only works for apps without signature verification, with limited compatibility. It's recommended to use 'Shortcut' feature instead."
        AppLanguage.ARABIC -> "التثبيت المستنسخ يعمل فقط للتطبيقات بدون التحقق من التوقيع، مع توافق محدود. يُنصح باستخدام ميزة 'الاختصار' بدلاً من ذلك."
        AppLanguage.PORTUGUESE -> "A instalação de clone só funciona para apps sem verificação de assinatura, com compatibilidade limitada. Recomenda-se usar o recurso 'Atalho'."
        AppLanguage.SPANISH -> "La instalación clonada solo funciona para aplicaciones sin verificación de firma, con compatibilidad limitada. Se recomienda usar la función 'Acceso directo'."
        AppLanguage.FRENCH -> "L'installation clonée ne fonctionne que pour les applications sans vérification de signature, avec une compatibilité limitée. Il est recommandé d'utiliser la fonctionnalité 'Raccourci'."
        AppLanguage.GERMAN -> "Die Klon-Installation funktioniert nur bei Apps ohne Signaturverifikation, mit eingeschränkter Kompatibilität. Es wird empfohlen, stattdessen die Funktion 'Verknüpfung' zu verwenden."
        AppLanguage.RUSSIAN -> "Установка клона работает только для приложений без проверки подписи, с ограниченной совместимостью. Рекомендуется использовать функцию «Ярлык»."
        AppLanguage.JAPANESE -> "クローンインストールは署名検証のないアプリのみで動作し、互換性が限定的です。代わりに「ショートカット」機能の使用をお勧めします。"
        AppLanguage.KOREAN -> "클론 설치는 서명 검증이 없는 앱에서만 작동하며 호환성이 제한적입니다. 대신 '바로가기' 기능을 사용하는 것을 권장합니다."
    }

    val iconLibrary: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "图标库"
        AppLanguage.ENGLISH -> "Icon Library"
        AppLanguage.ARABIC -> "مكتبة الأيقونات"
        AppLanguage.PORTUGUESE -> "Biblioteca de Ícones"
        AppLanguage.SPANISH -> "Biblioteca de Iconos"
        AppLanguage.FRENCH -> "Bibliothèque d'Icônes"
        AppLanguage.GERMAN -> "Symbolbibliothek"
        AppLanguage.RUSSIAN -> "Библиотека иконок"
        AppLanguage.JAPANESE -> "アイコンライブラリ"
        AppLanguage.KOREAN -> "아이콘 라이브러리"
    }

    val selectIconOrGenerate: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择图标或使用AI生成新图标"
        AppLanguage.ENGLISH -> "Select icon or generate new one with AI"
        AppLanguage.ARABIC -> "اختر أيقونة أو أنشئ واحدة جديدة بالذكاء الاصطناعي"
        AppLanguage.PORTUGUESE -> "Selecione um ícone ou gere um novo com IA"
        AppLanguage.SPANISH -> "Seleccione un icono o genere uno nuevo con IA"
        AppLanguage.FRENCH -> "Sélectionnez une icône ou générez-en une nouvelle avec l'IA"
        AppLanguage.GERMAN -> "Symbol auswählen oder neues mit KI generieren"
        AppLanguage.RUSSIAN -> "Выберите иконку или создайте новую с помощью ИИ"
        AppLanguage.JAPANESE -> "アイコンを選択またはAIで新規生成"
        AppLanguage.KOREAN -> "아이콘을 선택하거나 AI로 새로 생성"
    }

    val useAiToGenerateIcon: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "使用AI模型生成自定义图标"
        AppLanguage.ENGLISH -> "Use AI model to generate custom icon"
        AppLanguage.ARABIC -> "استخدم نموذج الذكاء الاصطناعي لإنشاء أيقونة مخصصة"
        AppLanguage.PORTUGUESE -> "Use modelo de IA para gerar ícone personalizado"
        AppLanguage.SPANISH -> "Use modelo de IA para generar icono personalizado"
        AppLanguage.FRENCH -> "Utilisez un modèle d'IA pour générer une icône personnalisée"
        AppLanguage.GERMAN -> "KI-Modell verwenden, um benutzerdefiniertes Symbol zu generieren"
        AppLanguage.RUSSIAN -> "Используйте модель ИИ для создания пользовательской иконки"
        AppLanguage.JAPANESE -> "AIモデルを使用してカスタムアイコンを生成"
        AppLanguage.KOREAN -> "AI 모델을 사용하여 맞춤 아이콘 생성"
    }

    val iconLibraryEmpty: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "图标库为空"
        AppLanguage.ENGLISH -> "Icon library is empty"
        AppLanguage.ARABIC -> "مكتبة الأيقونات فارغة"
        AppLanguage.PORTUGUESE -> "A biblioteca de ícones está vazia"
        AppLanguage.SPANISH -> "La biblioteca de iconos está vacía"
        AppLanguage.FRENCH -> "La bibliothèque d'icônes est vide"
        AppLanguage.GERMAN -> "Die Symbolbibliothek ist leer"
        AppLanguage.RUSSIAN -> "Библиотека иконок пуста"
        AppLanguage.JAPANESE -> "アイコンライブラリが空です"
        AppLanguage.KOREAN -> "아이콘 라이브러리가 비어 있습니다"
    }

    val iconLibraryEmptyHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "使用AI生成图标后会自动保存到这里"
        AppLanguage.ENGLISH -> "Icons generated by AI will be saved here automatically"
        AppLanguage.ARABIC -> "سيتم حفظ الأيقونات المُنشأة بالذكاء الاصطناعي هنا تلقائيًا"
        AppLanguage.PORTUGUESE -> "Ícones gerados por IA serão salvos aqui automaticamente"
        AppLanguage.SPANISH -> "Los iconos generados por IA se guardarán aquí automáticamente"
        AppLanguage.FRENCH -> "Les icônes générées par l'IA seront enregistrées ici automatiquement"
        AppLanguage.GERMAN -> "Von der KI generierte Symbole werden automatisch hier gespeichert"
        AppLanguage.RUSSIAN -> "Иконки, созданные ИИ, будут автоматически сохранены здесь"
        AppLanguage.JAPANESE -> "AIで生成されたアイコンは自動的にここに保存されます"
        AppLanguage.KOREAN -> "AI로 생성된 아이콘은 자동으로 여기에 저장됩니다"
    }

    val savedIcons: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已保存的图标"
        AppLanguage.ENGLISH -> "Saved Icons"
        AppLanguage.ARABIC -> "الأيقونات المحفوظة"
        AppLanguage.PORTUGUESE -> "Ícones Salvos"
        AppLanguage.SPANISH -> "Iconos Guardados"
        AppLanguage.FRENCH -> "Icônes Enregistrées"
        AppLanguage.GERMAN -> "Gespeicherte Symbole"
        AppLanguage.RUSSIAN -> "Сохраненные иконки"
        AppLanguage.JAPANESE -> "保存済みアイコン"
        AppLanguage.KOREAN -> "저장된 아이콘"
    }

    val uploadToLibrary: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "上传图片"
        AppLanguage.ENGLISH -> "Upload Image"
        AppLanguage.ARABIC -> "تحميل صورة"
        AppLanguage.PORTUGUESE -> "Enviar Imagem"
        AppLanguage.SPANISH -> "Subir Imagen"
        AppLanguage.FRENCH -> "Téléverser une image"
        AppLanguage.GERMAN -> "Bild hochladen"
        AppLanguage.RUSSIAN -> "Загрузить изображение"
        AppLanguage.JAPANESE -> "画像をアップロード"
        AppLanguage.KOREAN -> "이미지 업로드"
    }

    val uploadToLibraryDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "从相册选择图片，存入图标库"
        AppLanguage.ENGLISH -> "Pick an image from the gallery and save it to the library"
        AppLanguage.ARABIC -> "اختر صورة من المعرض واحفظها في المكتبة"
        AppLanguage.PORTUGUESE -> "Escolha uma imagem da galeria e salve-a na biblioteca"
        AppLanguage.SPANISH -> "Elige una imagen de la galería y guárdala en la biblioteca"
        AppLanguage.FRENCH -> "Choisissez une image dans la galerie et enregistrez-la dans la bibliothèque"
        AppLanguage.GERMAN -> "Bild aus der Galerie wählen und in der Bibliothek speichern"
        AppLanguage.RUSSIAN -> "Выберите изображение из галереи и сохраните в библиотеку"
        AppLanguage.JAPANESE -> "ギャラリーから画像を選びライブラリに保存"
        AppLanguage.KOREAN -> "갤러리에서 이미지를 선택해 라이브러리에 저장"
    }

    val deleteIcon: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "删除图标"
        AppLanguage.ENGLISH -> "Delete Icon"
        AppLanguage.ARABIC -> "حذف الأيقونة"
        AppLanguage.PORTUGUESE -> "Excluir Ícone"
        AppLanguage.SPANISH -> "Eliminar Icono"
        AppLanguage.FRENCH -> "Supprimer l'Icône"
        AppLanguage.GERMAN -> "Symbol löschen"
        AppLanguage.RUSSIAN -> "Удалить иконку"
        AppLanguage.JAPANESE -> "アイコン을 삭제"
        AppLanguage.KOREAN -> "아이콘 삭제"
    }

    val deleteIconConfirm: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "确定要从图标库中删除此图标吗？"
        AppLanguage.ENGLISH -> "Are you sure you want to delete this icon from the library?"
        AppLanguage.ARABIC -> "هل أنت متأكد أنك تريد حذف هذه الأيقونة من المكتبة؟"
        AppLanguage.PORTUGUESE -> "Tem certeza de que deseja excluir este ícone da biblioteca?"
        AppLanguage.SPANISH -> "¿Está seguro de que desea eliminar este icono de la biblioteca?"
        AppLanguage.FRENCH -> "Êtes-vous sûr de vouloir supprimer cette icône de la bibliothèque ?"
        AppLanguage.GERMAN -> "Möchten Sie dieses Symbol wirklich aus der Bibliothek löschen?"
        AppLanguage.RUSSIAN -> "Вы уверены, что хотите удалить эту иконку из библиотеки?"
        AppLanguage.JAPANESE -> "このアイコン을ライブラリから削除してもよろしいですか？"
        AppLanguage.KOREAN -> "이 아이콘을 라이브러리에서 삭제하시겠습니까?"
    }

    val saveFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "保存失败"
        AppLanguage.ENGLISH -> "Save failed"
        AppLanguage.ARABIC -> "فشل الحفظ"
        AppLanguage.PORTUGUESE -> "Falha ao salvar"
        AppLanguage.SPANISH -> "Error al guardar"
        AppLanguage.FRENCH -> "Échec de l'enregistrement"
        AppLanguage.GERMAN -> "Speichern fehlgeschlagen"
        AppLanguage.RUSSIAN -> "Не удалось сохранить"
        AppLanguage.JAPANESE -> "保存に失敗しました"
        AppLanguage.KOREAN -> "저장 실패"
    }

    val saveFailedWithReason: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "保存失败: %s"
        AppLanguage.ENGLISH -> "Save failed: %s"
        AppLanguage.ARABIC -> "فشل الحفظ: %s"
        AppLanguage.PORTUGUESE -> "Falha ao salvar: %s"
        AppLanguage.SPANISH -> "Error al guardar: %s"
        AppLanguage.FRENCH -> "Échec de l'enregistrement : %s"
        AppLanguage.GERMAN -> "Speichern fehlgeschlagen: %s"
        AppLanguage.RUSSIAN -> "Не удалось сохранить: %s"
        AppLanguage.JAPANESE -> "保存に失敗しました: %s"
        AppLanguage.KOREAN -> "저장 실패: %s"
    }

    val savedTo: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已保存到: %s"
        AppLanguage.ENGLISH -> "Saved to: %s"
        AppLanguage.ARABIC -> "تم الحفظ إلى: %s"
        AppLanguage.PORTUGUESE -> "Salvo em: %s"
        AppLanguage.SPANISH -> "Guardado en: %s"
        AppLanguage.FRENCH -> "Enregistré dans : %s"
        AppLanguage.GERMAN -> "Gespeichert unter: %s"
        AppLanguage.RUSSIAN -> "Сохранено в: %s"
        AppLanguage.JAPANESE -> "保存先: %s"
        AppLanguage.KOREAN -> "저장됨: %s"
    }

    val copiedToClipboard: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已复制到剪贴板"
        AppLanguage.ENGLISH -> "Copied to clipboard"
        AppLanguage.ARABIC -> "تم النسخ إلى الحافظة"
        AppLanguage.PORTUGUESE -> "Copiado para a área de transferência"
        AppLanguage.SPANISH -> "Copiado al portapapeles"
        AppLanguage.FRENCH -> "Copié dans le presse-papiers"
        AppLanguage.GERMAN -> "In die Zwischenablage kopiert"
        AppLanguage.RUSSIAN -> "Скопировано в буфер обмена"
        AppLanguage.JAPANESE -> "クリップボードにコピーしました"
        AppLanguage.KOREAN -> "클립보드에 복사되었습니다"
    }

    val downloadingVideo: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "正在下载视频..."
        AppLanguage.ENGLISH -> "Downloading video..."
        AppLanguage.ARABIC -> "جاري تحميل الفيديو..."
        AppLanguage.PORTUGUESE -> "Baixando vídeo..."
        AppLanguage.SPANISH -> "Descargando vídeo..."
        AppLanguage.FRENCH -> "Téléchargement de la vidéo..."
        AppLanguage.GERMAN -> "Video wird heruntergeladen..."
        AppLanguage.RUSSIAN -> "Загрузка видео..."
        AppLanguage.JAPANESE -> "動画をダウンロード中..."
        AppLanguage.KOREAN -> "동영상 다운로드 중..."
    }

    val shareFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Share failed"
        AppLanguage.ENGLISH -> "Share failed"
        AppLanguage.ARABIC -> "فشلت المشاركة"
        AppLanguage.PORTUGUESE -> "Falha ao compartilhar"
        AppLanguage.SPANISH -> "Error al compartir"
        AppLanguage.FRENCH -> "Échec du partage"
        AppLanguage.GERMAN -> "Teilen fehlgeschlagen"
        AppLanguage.RUSSIAN -> "Не удалось поделиться"
        AppLanguage.JAPANESE -> "共有に失敗しました"
        AppLanguage.KOREAN -> "공유 실패"
    }

    val preparingShare: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "正在准备分享..."
        AppLanguage.ENGLISH -> "Preparing to share..."
        AppLanguage.ARABIC -> "جاري التحضير للمشاركة..."
        AppLanguage.PORTUGUESE -> "Preparando para compartilhar..."
        AppLanguage.SPANISH -> "Preparando para compartir..."
        AppLanguage.FRENCH -> "Préparation du partage..."
        AppLanguage.GERMAN -> "Teilen wird vorbereitet..."
        AppLanguage.RUSSIAN -> "Подготовка к отправке..."
        AppLanguage.JAPANESE -> "共有を準備中..."
        AppLanguage.KOREAN -> "공유 준비 중..."
    }

    val cannotOpenLink: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "无法打开链接"
        AppLanguage.ENGLISH -> "Cannot open link"
        AppLanguage.ARABIC -> "لا يمكن فتح الرابط"
        AppLanguage.PORTUGUESE -> "Não é possível abrir o link"
        AppLanguage.SPANISH -> "No se puede abrir el enlace"
        AppLanguage.FRENCH -> "Impossible d'ouvrir le lien"
        AppLanguage.GERMAN -> "Link kann nicht geöffnet werden"
        AppLanguage.RUSSIAN -> "Не удается открыть ссылку"
        AppLanguage.JAPANESE -> "リンクを開けません"
        AppLanguage.KOREAN -> "링크를 열 수 없습니다"
    }

    val testingModules: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "测试 {0} 个模块"
        AppLanguage.ENGLISH -> "Testing {0} modules"
        AppLanguage.ARABIC -> "اختبار {0} وحدات"
        AppLanguage.PORTUGUESE -> "Testando {0} módulos"
        AppLanguage.SPANISH -> "Probando {0} módulos"
        AppLanguage.FRENCH -> "Test de {0} modules"
        AppLanguage.GERMAN -> "{0} Module werden getestet"
        AppLanguage.RUSSIAN -> "Тестирование {0} модулей"
        AppLanguage.JAPANESE -> "{0}個のモジュールをテスト中"
        AppLanguage.KOREAN -> "{0}개 모듈 테스트 중"
    }

    val savingImage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "正在保存图片..."
        AppLanguage.ENGLISH -> "Saving image..."
        AppLanguage.ARABIC -> "جاري حفظ الصورة..."
        AppLanguage.PORTUGUESE -> "Salvando imagem..."
        AppLanguage.SPANISH -> "Guardando imagen..."
        AppLanguage.FRENCH -> "Enregistrement de l'image..."
        AppLanguage.GERMAN -> "Bild wird gespeichert..."
        AppLanguage.RUSSIAN -> "Сохранение изображения..."
        AppLanguage.JAPANESE -> "画像を保存中..."
        AppLanguage.KOREAN -> "이미지 저장 중..."
    }

    val imageSavedToGallery: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "图片已保存到相册"
        AppLanguage.ENGLISH -> "Image saved to gallery"
        AppLanguage.ARABIC -> "تم حفظ الصورة في المعرض"
        AppLanguage.PORTUGUESE -> "Imagem salva na galeria"
        AppLanguage.SPANISH -> "Imagen guardada en la galería"
        AppLanguage.FRENCH -> "Image enregistrée dans la galerie"
        AppLanguage.GERMAN -> "Bild in Galerie gespeichert"
        AppLanguage.RUSSIAN -> "Изображение сохранено в галерею"
        AppLanguage.JAPANESE -> "画像をギャラリーに保存しました"
        AppLanguage.KOREAN -> "이미지가 갤러리에 저장되었습니다"
    }

    val savingVideo: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "正在保存视频..."
        AppLanguage.ENGLISH -> "Saving video..."
        AppLanguage.ARABIC -> "جاري حفظ الفيديو..."
        AppLanguage.PORTUGUESE -> "Salvando vídeo..."
        AppLanguage.SPANISH -> "Guardando vídeo..."
        AppLanguage.FRENCH -> "Enregistrement de la vidéo..."
        AppLanguage.GERMAN -> "Video wird gespeichert..."
        AppLanguage.RUSSIAN -> "Сохранение видео..."
        AppLanguage.JAPANESE -> "動画を保存中..."
        AppLanguage.KOREAN -> "동영상 저장 중..."
    }

    val videoSavedToGallery: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "视频已保存到相册"
        AppLanguage.ENGLISH -> "Video saved to gallery"
        AppLanguage.ARABIC -> "تم حفظ الفيديو في المعرض"
        AppLanguage.PORTUGUESE -> "Vídeo salvo na galeria"
        AppLanguage.SPANISH -> "Vídeo guardado en la galería"
        AppLanguage.FRENCH -> "Vidéo enregistrée dans la galerie"
        AppLanguage.GERMAN -> "Video in Galerie gespeichert"
        AppLanguage.RUSSIAN -> "Видео сохранено в галерею"
        AppLanguage.JAPANESE -> "動画をギャラリーに保存しました"
        AppLanguage.KOREAN -> "동영상이 갤러리에 저장되었습니다"
    }

    val startDownload: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "开始下载: %s"
        AppLanguage.ENGLISH -> "Start download: %s"
        AppLanguage.ARABIC -> "بدء التحميل: %s"
        AppLanguage.PORTUGUESE -> "Iniciar download: %s"
        AppLanguage.SPANISH -> "Iniciar descarga: %s"
        AppLanguage.FRENCH -> "Démarrer le téléchargement : %s"
        AppLanguage.GERMAN -> "Download starten: %s"
        AppLanguage.RUSSIAN -> "Начать загрузку: %s"
        AppLanguage.JAPANESE -> "ダウンロードを開始: %s"
        AppLanguage.KOREAN -> "다운로드 시작: %s"
    }

    val downloadFailedWithReason: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下载失败: %s"
        AppLanguage.ENGLISH -> "Download failed: %s"
        AppLanguage.ARABIC -> "فشل التحميل: %s"
        AppLanguage.PORTUGUESE -> "Falha no download: %s"
        AppLanguage.SPANISH -> "Error de descarga: %s"
        AppLanguage.FRENCH -> "Échec du téléchargement : %s"
        AppLanguage.GERMAN -> "Download fehlgeschlagen: %s"
        AppLanguage.RUSSIAN -> "Ошибка загрузки: %s"
        AppLanguage.JAPANESE -> "ダウンロードに失敗しました: %s"
        AppLanguage.KOREAN -> "다운로드 실패: %s"
    }

    val previewAnnouncementEffect: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "预览公告效果"
        AppLanguage.ENGLISH -> "Preview announcement style"
        AppLanguage.ARABIC -> "معاينة تأثير الإعلان"
        AppLanguage.PORTUGUESE -> "Visualizar estilo do anúncio"
        AppLanguage.SPANISH -> "Vista previa del estilo del anuncio"
        AppLanguage.FRENCH -> "Aperçu du style d'annonce"
        AppLanguage.GERMAN -> "Ankündigungsstilvorschau"
        AppLanguage.RUSSIAN -> "Предпросмотр стиля объявления"
        AppLanguage.JAPANESE -> "お知らせスタイルをプレビュー"
        AppLanguage.KOREAN -> "공지 스타일 미리보기"
    }

    val textGeneration: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "文本生成"
        AppLanguage.ENGLISH -> "Text Generation"
        AppLanguage.ARABIC -> "توليد النص"
        AppLanguage.PORTUGUESE -> "Geração de Texto"
        AppLanguage.SPANISH -> "Generación de Texto"
        AppLanguage.FRENCH -> "Génération de Texte"
        AppLanguage.GERMAN -> "Textgenerierung"
        AppLanguage.RUSSIAN -> "Генерация текста"
        AppLanguage.JAPANESE -> "テキスト生成"
        AppLanguage.KOREAN -> "텍스트 생성"
    }

    val basicTextDialogue: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "基础文本对话和生成"
        AppLanguage.ENGLISH -> "Basic text dialogue and generation"
        AppLanguage.ARABIC -> "حوار النص الأساسي والتوليد"
        AppLanguage.PORTUGUESE -> "Diálogo e geração de texto básica"
        AppLanguage.SPANISH -> "Diálogo y generación de texto básica"
        AppLanguage.FRENCH -> "Dialogue et génération de texte de base"
        AppLanguage.GERMAN -> "Grundlegende Textdialoge und -generierung"
        AppLanguage.RUSSIAN -> "Базовый текстовый диалог и генерация"
        AppLanguage.JAPANESE -> "基本的なテキスト対話と生成"
        AppLanguage.KOREAN -> "기본 텍스트 대화 및 생성"
    }

    val imageGeneration: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "图像生成"
        AppLanguage.ENGLISH -> "Image Generation"
        AppLanguage.ARABIC -> "توليد الصور"
        AppLanguage.PORTUGUESE -> "Geração de Imagem"
        AppLanguage.SPANISH -> "Generación de Imágenes"
        AppLanguage.FRENCH -> "Génération d'Images"
        AppLanguage.GERMAN -> "Bildgenerierung"
        AppLanguage.RUSSIAN -> "Генерация изображений"
        AppLanguage.JAPANESE -> "画像生成"
        AppLanguage.KOREAN -> "이미지 생성"
    }

    val generateImages: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "生成图片"
        AppLanguage.ENGLISH -> "Generate images"
        AppLanguage.ARABIC -> "إنشاء الصور"
        AppLanguage.PORTUGUESE -> "Gerar imagens"
        AppLanguage.SPANISH -> "Generar imágenes"
        AppLanguage.FRENCH -> "Générer des images"
        AppLanguage.GERMAN -> "Bilder generieren"
        AppLanguage.RUSSIAN -> "Создать изображения"
        AppLanguage.JAPANESE -> "画像を生成"
        AppLanguage.KOREAN -> "이미지 생성"
    }

    val retry: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Retry"
        AppLanguage.ENGLISH -> "Retry"
        AppLanguage.ARABIC -> "إعادة المحاولة"
        AppLanguage.PORTUGUESE -> "Tentar novamente"
        AppLanguage.SPANISH -> "Reintentar"
        AppLanguage.FRENCH -> "Réessayer"
        AppLanguage.GERMAN -> "Erneut versuchen"
        AppLanguage.RUSSIAN -> "Повторить"
        AppLanguage.JAPANESE -> "再試行"
        AppLanguage.KOREAN -> "재시도"
    }

    val closeDialog: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "关闭"
        AppLanguage.ENGLISH -> "Close"
        AppLanguage.ARABIC -> "إغلاق"
        AppLanguage.PORTUGUESE -> "Fechar"
        AppLanguage.SPANISH -> "Cerrar"
        AppLanguage.FRENCH -> "Fermer"
        AppLanguage.GERMAN -> "Schließen"
        AppLanguage.RUSSIAN -> "Закрыть"
        AppLanguage.JAPANESE -> "閉じる"
        AppLanguage.KOREAN -> "닫기"
    }

    val deleteAction: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Delete"
        AppLanguage.ENGLISH -> "Delete"
        AppLanguage.ARABIC -> "حذف"
        AppLanguage.PORTUGUESE -> "Excluir"
        AppLanguage.SPANISH -> "Eliminar"
        AppLanguage.FRENCH -> "Supprimer"
        AppLanguage.GERMAN -> "Löschen"
        AppLanguage.RUSSIAN -> "Удалить"
        AppLanguage.JAPANESE -> "削除"
        AppLanguage.KOREAN -> "삭제"
    }

    val savingToGallery: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "正在保存到相册..."
        AppLanguage.ENGLISH -> "Saving to gallery..."
        AppLanguage.ARABIC -> "جاري الحفظ في المعرض..."
        AppLanguage.PORTUGUESE -> "Salvando na galeria..."
        AppLanguage.SPANISH -> "Guardando en la galería..."
        AppLanguage.FRENCH -> "Enregistrement dans la galerie..."
        AppLanguage.GERMAN -> "Wird in Galerie gespeichert..."
        AppLanguage.RUSSIAN -> "Сохранение в галерею..."
        AppLanguage.JAPANESE -> "ギャラリーに保存中..."
        AppLanguage.KOREAN -> "갤러리에 저장 중..."
    }

    val savingImageToGallery: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "正在保存图片到相册..."
        AppLanguage.ENGLISH -> "Saving image to gallery..."
        AppLanguage.ARABIC -> "جاري حفظ الصورة في المعرض..."
        AppLanguage.PORTUGUESE -> "Salvando imagem na galeria..."
        AppLanguage.SPANISH -> "Guardando imagen en la galería..."
        AppLanguage.FRENCH -> "Enregistrement de l'image dans la galerie..."
        AppLanguage.GERMAN -> "Bild wird in Galerie gespeichert..."
        AppLanguage.RUSSIAN -> "Сохранение изображения в галерею..."
        AppLanguage.JAPANESE -> "画像をギャラリーに保存中..."
        AppLanguage.KOREAN -> "이미지를 갤러리에 저장 중..."
    }

    val savingVideoToGallery: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "正在保存视频到相册..."
        AppLanguage.ENGLISH -> "Saving video to gallery..."
        AppLanguage.ARABIC -> "جاري حفظ الفيديو في المعرض..."
        AppLanguage.PORTUGUESE -> "Salvando vídeo na galeria..."
        AppLanguage.SPANISH -> "Guardando vídeo en la galería..."
        AppLanguage.FRENCH -> "Enregistrement de la vidéo dans la galerie..."
        AppLanguage.GERMAN -> "Video wird in Galerie gespeichert..."
        AppLanguage.RUSSIAN -> "Сохранение видео в галерею..."
        AppLanguage.JAPANESE -> "動画をギャラリーに保存中..."
        AppLanguage.KOREAN -> "동영상을 갤러리에 저장 중..."
    }

    val blobDownloadProcessing: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "正在处理下载..."
        AppLanguage.ENGLISH -> "Processing download..."
        AppLanguage.ARABIC -> "جاري معالجة التحميل..."
        AppLanguage.PORTUGUESE -> "Processando download..."
        AppLanguage.SPANISH -> "Procesando descarga..."
        AppLanguage.FRENCH -> "Traitement du téléchargement..."
        AppLanguage.GERMAN -> "Download wird verarbeitet..."
        AppLanguage.RUSSIAN -> "Обработка загрузки..."
        AppLanguage.JAPANESE -> "ダウンロードを処理中..."
        AppLanguage.KOREAN -> "다운로드 처리 중..."
    }

    val blobDownloadFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "无法获取文件数据，请重试"
        AppLanguage.ENGLISH -> "Cannot get file data, please try again"
        AppLanguage.ARABIC -> "لا يمكن الحصول على بيانات الملف، حاول مرة أخرى"
        AppLanguage.PORTUGUESE -> "Não é possível obter os dados do arquivo, tente novamente"
        AppLanguage.SPANISH -> "No se pueden obtener los datos del archivo, inténtelo de nuevo"
        AppLanguage.FRENCH -> "Impossible d'obtenir les données du fichier, veuillez réessayer"
        AppLanguage.GERMAN -> "Dateidaten können nicht abgerufen werden, bitte erneut versuchen"
        AppLanguage.RUSSIAN -> "Не удается получить данные файла, попробуйте еще раз"
        AppLanguage.JAPANESE -> "ファイルデータを取得できません。再試行してください"
        AppLanguage.KOREAN -> "파일 데이터를 가져올 수 없습니다. 다시 시도하세요"
    }

    val cannotOpenBrowser: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "无法打开浏览器"
        AppLanguage.ENGLISH -> "Cannot open browser"
        AppLanguage.ARABIC -> "لا يمكن فتح المتصفح"
        AppLanguage.PORTUGUESE -> "Não é possível abrir o navegador"
        AppLanguage.SPANISH -> "No se puede abrir el navegador"
        AppLanguage.FRENCH -> "Impossible d'ouvrir le navigateur"
        AppLanguage.GERMAN -> "Browser kann nicht geöffnet werden"
        AppLanguage.RUSSIAN -> "Не удается открыть браузер"
        AppLanguage.JAPANESE -> "ブラウザを開けません"
        AppLanguage.KOREAN -> "브라우저를 열 수 없습니다"
    }

    val presetSaved: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "方案已保存"
        AppLanguage.ENGLISH -> "Preset saved"
        AppLanguage.ARABIC -> "تم حفظ الإعداد المسبق"
        AppLanguage.PORTUGUESE -> "Predefinição salva"
        AppLanguage.SPANISH -> "Preset guardado"
        AppLanguage.FRENCH -> "Préréglage enregistré"
        AppLanguage.GERMAN -> "Voreinstellung gespeichert"
        AppLanguage.RUSSIAN -> "Пресет сохранен"
        AppLanguage.JAPANESE -> "プリセットを保存しました"
        AppLanguage.KOREAN -> "사전 설정이 저장되었습니다"
    }

    val copied: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已复制"
        AppLanguage.ENGLISH -> "Copied"
        AppLanguage.ARABIC -> "تم النسخ"
        AppLanguage.PORTUGUESE -> "Copiado"
        AppLanguage.SPANISH -> "Copiado"
        AppLanguage.FRENCH -> "Copié"
        AppLanguage.GERMAN -> "Kopiert"
        AppLanguage.RUSSIAN -> "Скопировано"
        AppLanguage.JAPANESE -> "コピーしました"
        AppLanguage.KOREAN -> "복사됨"
    }

    val deleted: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已删除"
        AppLanguage.ENGLISH -> "Deleted"
        AppLanguage.ARABIC -> "تم الحذف"
        AppLanguage.PORTUGUESE -> "Excluído"
        AppLanguage.SPANISH -> "Eliminado"
        AppLanguage.FRENCH -> "Supprimé"
        AppLanguage.GERMAN -> "Gelöscht"
        AppLanguage.RUSSIAN -> "Удалено"
        AppLanguage.JAPANESE -> "削除しました"
        AppLanguage.KOREAN -> "삭제됨"
    }

    val cannotOpenInBrowser: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "无法在外部浏览器中打开"
        AppLanguage.ENGLISH -> "Cannot open in external browser"
        AppLanguage.ARABIC -> "لا يمكن الفتح في المتصفح الخارجي"
        AppLanguage.PORTUGUESE -> "Não é possível abrir no navegador externo"
        AppLanguage.SPANISH -> "No se puede abrir en el navegador externo"
        AppLanguage.FRENCH -> "Impossible d'ouvrir dans le navigateur externe"
        AppLanguage.GERMAN -> "Öffnen im externen Browser nicht möglich"
        AppLanguage.RUSSIAN -> "Не удается открыть во внешнем браузере"
        AppLanguage.JAPANESE -> "外部ブラウザで開けません"
        AppLanguage.KOREAN -> "외부 브라우저에서 열 수 없습니다"
    }

    val noFilePathAvailable: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "没有可用的文件路径"
        AppLanguage.ENGLISH -> "No file path available"
        AppLanguage.ARABIC -> "لا يوجد مسار ملف متاح"
        AppLanguage.PORTUGUESE -> "Nenhum caminho de arquivo disponível"
        AppLanguage.SPANISH -> "No hay ruta de archivo disponible"
        AppLanguage.FRENCH -> "Aucun chemin de fichier disponible"
        AppLanguage.GERMAN -> "Kein Dateipfad verfügbar"
        AppLanguage.RUSSIAN -> "Нет доступного пути к файлу"
        AppLanguage.JAPANESE -> "利用可能なファイルパスがありません"
        AppLanguage.KOREAN -> "사용 가능한 파일 경로가 없습니다"
    }

    val copiedAllLogs: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已复制全部日志"
        AppLanguage.ENGLISH -> "All logs copied"
        AppLanguage.ARABIC -> "تم نسخ جميع السجلات"
        AppLanguage.PORTUGUESE -> "Todos os logs copiados"
        AppLanguage.SPANISH -> "Todos los registros copiados"
        AppLanguage.FRENCH -> "Tous les journaux copiés"
        AppLanguage.GERMAN -> "Alle Protokolle kopiert"
        AppLanguage.RUSSIAN -> "Все журналы скопированы"
        AppLanguage.JAPANESE -> "すべてのログをコピーしました"
        AppLanguage.KOREAN -> "모든 로그가 복사되었습니다"
    }

    val console: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "控制台"
        AppLanguage.ENGLISH -> "Console"
        AppLanguage.ARABIC -> "وحدة التحكم"
        AppLanguage.PORTUGUESE -> "Consola"
        AppLanguage.SPANISH -> "Consola"
        AppLanguage.FRENCH -> "Console"
        AppLanguage.GERMAN -> "Konsole"
        AppLanguage.RUSSIAN -> "Консоль"
        AppLanguage.JAPANESE -> "コンソール"
        AppLanguage.KOREAN -> "콘솔"
    }

    val pageZoomLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "页面缩放"
        AppLanguage.ENGLISH -> "Page Zoom"
        AppLanguage.ARABIC -> "تكبير الصفحة"
        AppLanguage.PORTUGUESE -> "Zoom da Página"
        AppLanguage.SPANISH -> "Zoom de Página"
        AppLanguage.FRENCH -> "Zoom de Page"
        AppLanguage.GERMAN -> "Seitenzoom"
        AppLanguage.RUSSIAN -> "Масштаб страницы"
        AppLanguage.JAPANESE -> "ページズーム"
        AppLanguage.KOREAN -> "페이지 확대"
    }

    val pageZoomReset: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "重置"
        AppLanguage.ENGLISH -> "Reset"
        AppLanguage.ARABIC -> "إعادة تعيين"
        AppLanguage.PORTUGUESE -> "Redefinir"
        AppLanguage.SPANISH -> "Restablecer"
        AppLanguage.FRENCH -> "Réinitialiser"
        AppLanguage.GERMAN -> "Zurücksetzen"
        AppLanguage.RUSSIAN -> "Сбросить"
        AppLanguage.JAPANESE -> "リセット"
        AppLanguage.KOREAN -> "재설정"
    }

    val pageZoomSettingLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "初始页面缩放"
        AppLanguage.ENGLISH -> "Initial Page Zoom"
        AppLanguage.ARABIC -> "تكبير الصفحة الابتدائي"
        AppLanguage.PORTUGUESE -> "Zoom Inicial da Página"
        AppLanguage.SPANISH -> "Zoom inicial de página"
        AppLanguage.FRENCH -> "Zoom de page initial"
        AppLanguage.GERMAN -> "Anfänglicher Seitenzoom"
        AppLanguage.RUSSIAN -> "Начальный масштаб страницы"
        AppLanguage.JAPANESE -> "初期ページズーム"
        AppLanguage.KOREAN -> "초기 페이지 확대"
    }

    val pageZoomSettingHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "应用启动时应用的页面缩放比例，无需退出全屏使用工具栏"
        AppLanguage.ENGLISH -> "Page zoom applied at app start — no need to leave fullscreen for the toolbar"
        AppLanguage.ARABIC -> "تكبير الصفحة المطبق عند بدء التطبيق — لا حاجة لمغادرة ملء الشاشة لاستخدام شريط الأدوات"
        AppLanguage.PORTUGUESE -> "Zoom de página aplicado ao iniciar o aplicativo — sem precisar sair da tela cheia para usar a barra de ferramentas"
        AppLanguage.SPANISH -> "Zoom de página aplicado al iniciar la aplicación; no hace falta salir de pantalla completa para usar la barra de herramientas"
        AppLanguage.FRENCH -> "Zoom de page appliqué au démarrage de l'application — inutile de quitter le plein écran pour utiliser la barre d'outils"
        AppLanguage.GERMAN -> "Beim App-Start angewandter Seitenzoom — ohne die Symbolleiste im Vollbildmodus verlassen zu müssen"
        AppLanguage.RUSSIAN -> "Масштаб страницы при запуске приложения — не нужно выходить из полноэкранного режима ради панели инструментов"
        AppLanguage.JAPANESE -> "アプリ起動時に適用されるページズーム。フルスクリーンを解除してツールバーを使う必要はありません"
        AppLanguage.KOREAN -> "앱 시작 시 적용되는 페이지 확대/축소 — 전체 화면을 나가 도구 모음을 사용할 필요가 없습니다"
    }

    val noConsoleMessages: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "暂无控制台消息"
        AppLanguage.ENGLISH -> "No console messages"
        AppLanguage.ARABIC -> "لا توجد رسائل في وحدة التحكم"
        AppLanguage.PORTUGUESE -> "Nenhuma mensagem no console"
        AppLanguage.SPANISH -> "Sin mensajes en la consola"
        AppLanguage.FRENCH -> "Aucun message dans la console"
        AppLanguage.GERMAN -> "Keine Konsolenmeldungen"
        AppLanguage.RUSSIAN -> "Нет сообщений консоли"
        AppLanguage.JAPANESE -> "コンソールメッセージはありません"
        AppLanguage.KOREAN -> "콘솔 메시지가 없습니다"
    }

    val inputJavaScript: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "输入 JavaScript..."
        AppLanguage.ENGLISH -> "Enter JavaScript..."
        AppLanguage.ARABIC -> "أدخل JavaScript..."
        AppLanguage.PORTUGUESE -> "Digite JavaScript..."
        AppLanguage.SPANISH -> "Introduzca JavaScript..."
        AppLanguage.FRENCH -> "Saisir JavaScript..."
        AppLanguage.GERMAN -> "JavaScript eingeben..."
        AppLanguage.RUSSIAN -> "Введите JavaScript..."
        AppLanguage.JAPANESE -> "JavaScriptを入力..."
        AppLanguage.KOREAN -> "JavaScript 입력..."
    }

    val preparingDownload: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "正在准备下载: "
        AppLanguage.ENGLISH -> "Preparing download: "
        AppLanguage.ARABIC -> "جاري تحضير التحميل: "
        AppLanguage.PORTUGUESE -> "Preparando download: "
        AppLanguage.SPANISH -> "Preparando descarga: "
        AppLanguage.FRENCH -> "Préparation du téléchargement : "
        AppLanguage.GERMAN -> "Download wird vorbereitet: "
        AppLanguage.RUSSIAN -> "Подготовка загрузки: "
        AppLanguage.JAPANESE -> "ダウンロードを準備中: "
        AppLanguage.KOREAN -> "다운로드 준비 중: "
    }

    val cannotGetFileData: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "无法获取文件数据，请重试"
        AppLanguage.ENGLISH -> "Cannot get file data, please try again"
        AppLanguage.ARABIC -> "لا يمكن الحصول على بيانات الملف، حاول مرة أخرى"
        AppLanguage.PORTUGUESE -> "Não é possível obter os dados do arquivo, tente novamente"
        AppLanguage.SPANISH -> "No se pueden obtener los datos del archivo, inténtelo de nuevo"
        AppLanguage.FRENCH -> "Impossible d'obtenir les données du fichier, veuillez réessayer"
        AppLanguage.GERMAN -> "Dateidaten können nicht abgerufen werden, bitte erneut versuchen"
        AppLanguage.RUSSIAN -> "Не удается получить данные файла, попробуйте еще раз"
        AppLanguage.JAPANESE -> "ファイルデータを取得できません。再試行してください"
        AppLanguage.KOREAN -> "파일 데이터를 가져올 수 없습니다. 다시 시도하세요"
    }

    val downloadUnavailable: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下载功能不可用，请确保应用已正确配置"
        AppLanguage.ENGLISH -> "Download unavailable, please ensure the app is configured correctly"
        AppLanguage.ARABIC -> "التحميل غير متاح، يرجى التأكد من تكوين التطبيق بشكل صحيح"
        AppLanguage.PORTUGUESE -> "Download indisponível, verifique se o aplicativo está configurado corretamente"
        AppLanguage.SPANISH -> "Descarga no disponible, asegúrese de que la aplicación esté configurada correctamente"
        AppLanguage.FRENCH -> "Téléchargement indisponible, veuillez vous assurer que l'application est correctement configurée"
        AppLanguage.GERMAN -> "Download nicht verfügbar, bitte stellen Sie sicher, dass die App korrekt konfiguriert ist"
        AppLanguage.RUSSIAN -> "Загрузка недоступна, убедитесь, что приложение настроено правильно"
        AppLanguage.JAPANESE -> "ダウンロードを利用できません。アプリが正しく構成されていることを確認してください"
        AppLanguage.KOREAN -> "다운로드를 사용할 수 없습니다. 앱이 올바르게 구성되어 있는지 확인하세요"
    }

    val processFileFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "处理文件失败: "
        AppLanguage.ENGLISH -> "Failed to process file: "
        AppLanguage.ARABIC -> "فشل في معالجة الملف: "
        AppLanguage.PORTUGUESE -> "Falha ao processar arquivo: "
        AppLanguage.SPANISH -> "Error al procesar archivo: "
        AppLanguage.FRENCH -> "Échec du traitement du fichier : "
        AppLanguage.GERMAN -> "Dateiverarbeitung fehlgeschlagen: "
        AppLanguage.RUSSIAN -> "Ошибка обработки файла: "
        AppLanguage.JAPANESE -> "ファイルの処理に失敗しました: "
        AppLanguage.KOREAN -> "파일 처리 실패: "
    }

    val readFileFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Failed to read file"
        AppLanguage.ENGLISH -> "Failed to read file"
        AppLanguage.ARABIC -> "فشل في قراءة الملف"
        AppLanguage.PORTUGUESE -> "Falha ao ler arquivo"
        AppLanguage.SPANISH -> "Error al leer archivo"
        AppLanguage.FRENCH -> "Échec de la lecture du fichier"
        AppLanguage.GERMAN -> "Datei lesen fehlgeschlagen"
        AppLanguage.RUSSIAN -> "Ошибка чтения файла"
        AppLanguage.JAPANESE -> "ファイルの読み込みに失敗しました"
        AppLanguage.KOREAN -> "파일 읽기 실패"
    }

    val downloadFailedPrefix: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下载失败: "
        AppLanguage.ENGLISH -> "Download failed: "
        AppLanguage.ARABIC -> "فشل التحميل: "
        AppLanguage.PORTUGUESE -> "Falha no download: "
        AppLanguage.SPANISH -> "Error de descarga: "
        AppLanguage.FRENCH -> "Échec du téléchargement : "
        AppLanguage.GERMAN -> "Download fehlgeschlagen: "
        AppLanguage.RUSSIAN -> "Ошибка загрузки: "
        AppLanguage.JAPANESE -> "ダウンロードに失敗しました: "
        AppLanguage.KOREAN -> "다운로드 실패: "
    }

    val copiedFullLog: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已复制完整日志"
        AppLanguage.ENGLISH -> "Full log copied"
        AppLanguage.ARABIC -> "تم نسخ السجل الكامل"
        AppLanguage.PORTUGUESE -> "Registro completo copiado"
        AppLanguage.SPANISH -> "Registro completo copiado"
        AppLanguage.FRENCH -> "Journal complet copié"
        AppLanguage.GERMAN -> "Vollständiges Protokoll kopiert"
        AppLanguage.RUSSIAN -> "Полный журнал скопирован"
        AppLanguage.JAPANESE -> "完全なログをコピーしました"
        AppLanguage.KOREAN -> "전체 로그 복사됨"
    }

    val copiedSourceCode: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已复制源代码"
        AppLanguage.ENGLISH -> "Source code copied"
        AppLanguage.ARABIC -> "تم نسخ الكود المصدري"
        AppLanguage.PORTUGUESE -> "Código-fonte copiado"
        AppLanguage.SPANISH -> "Código fuente copiado"
        AppLanguage.FRENCH -> "Code source copié"
        AppLanguage.GERMAN -> "Quellcode kopiert"
        AppLanguage.RUSSIAN -> "Исходный код скопирован"
        AppLanguage.JAPANESE -> "ソースコードをコピーしました"
        AppLanguage.KOREAN -> "소스 코드 복사됨"
    }

    val copyAll: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "复制全部"
        AppLanguage.ENGLISH -> "Copy all"
        AppLanguage.ARABIC -> "نسخ الكل"
        AppLanguage.PORTUGUESE -> "Copiar tudo"
        AppLanguage.SPANISH -> "Copiar todo"
        AppLanguage.FRENCH -> "Tout copier"
        AppLanguage.GERMAN -> "Alles kopieren"
        AppLanguage.RUSSIAN -> "Копировать всё"
        AppLanguage.JAPANESE -> "すべてコピー"
        AppLanguage.KOREAN -> "모두 복사"
    }

    val logDetails: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "日志详情"
        AppLanguage.ENGLISH -> "Log Details"
        AppLanguage.ARABIC -> "تفاصيل السجل"
        AppLanguage.PORTUGUESE -> "Detalhes do Registro"
        AppLanguage.SPANISH -> "Detalles del Registro"
        AppLanguage.FRENCH -> "Détails du Journal"
        AppLanguage.GERMAN -> "Protokolldetails"
        AppLanguage.RUSSIAN -> "Подробности журнала"
        AppLanguage.JAPANESE -> "ログの詳細"
        AppLanguage.KOREAN -> "로그 상세"
    }

    val level: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "级别"
        AppLanguage.ENGLISH -> "Level"
        AppLanguage.ARABIC -> "المستوى"
        AppLanguage.PORTUGUESE -> "Nível"
        AppLanguage.SPANISH -> "Nivel"
        AppLanguage.FRENCH -> "Niveau"
        AppLanguage.GERMAN -> "Stufe"
        AppLanguage.RUSSIAN -> "Уровень"
        AppLanguage.JAPANESE -> "レベル"
        AppLanguage.KOREAN -> "레벨"
    }

    val time: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "时间"
        AppLanguage.ENGLISH -> "Time"
        AppLanguage.ARABIC -> "الوقت"
        AppLanguage.PORTUGUESE -> "Hora"
        AppLanguage.SPANISH -> "Hora"
        AppLanguage.FRENCH -> "Heure"
        AppLanguage.GERMAN -> "Zeit"
        AppLanguage.RUSSIAN -> "Время"
        AppLanguage.JAPANESE -> "時刻"
        AppLanguage.KOREAN -> "시간"
    }

    val source: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "来源"
        AppLanguage.ENGLISH -> "Source"
        AppLanguage.ARABIC -> "المصدر"
        AppLanguage.PORTUGUESE -> "Origem"
        AppLanguage.SPANISH -> "Origen"
        AppLanguage.FRENCH -> "Source"
        AppLanguage.GERMAN -> "Quelle"
        AppLanguage.RUSSIAN -> "Источник"
        AppLanguage.JAPANESE -> "ソース"
        AppLanguage.KOREAN -> "소스"
    }

    val messageContent: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "消息内容"
        AppLanguage.ENGLISH -> "Message Content"
        AppLanguage.ARABIC -> "محتوى الرسالة"
        AppLanguage.PORTUGUESE -> "Conteúdo da Mensagem"
        AppLanguage.SPANISH -> "Contenido del Mensaje"
        AppLanguage.FRENCH -> "Contenu du Message"
        AppLanguage.GERMAN -> "Nachrichteninhalt"
        AppLanguage.RUSSIAN -> "Содержимое сообщения"
        AppLanguage.JAPANESE -> "メッセージ内容"
        AppLanguage.KOREAN -> "메시지 내용"
    }

    val sourceCode: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "源代码"
        AppLanguage.ENGLISH -> "Source Code"
        AppLanguage.ARABIC -> "الكود المصدري"
        AppLanguage.PORTUGUESE -> "Código-fonte"
        AppLanguage.SPANISH -> "Código fuente"
        AppLanguage.FRENCH -> "Code source"
        AppLanguage.GERMAN -> "Quellcode"
        AppLanguage.RUSSIAN -> "Исходный код"
        AppLanguage.JAPANESE -> "ソースコード"
        AppLanguage.KOREAN -> "소스 코드"
    }

    val inputJavaScriptExpression: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "输入 JavaScript 表达式..."
        AppLanguage.ENGLISH -> "Enter JavaScript expression..."
        AppLanguage.ARABIC -> "أدخل تعبير JavaScript..."
        AppLanguage.PORTUGUESE -> "Digite a expressão JavaScript..."
        AppLanguage.SPANISH -> "Ingrese la expresión JavaScript..."
        AppLanguage.FRENCH -> "Saisir l'expression JavaScript..."
        AppLanguage.GERMAN -> "JavaScript-Ausdruck eingeben..."
        AppLanguage.RUSSIAN -> "Введите выражение JavaScript..."
        AppLanguage.JAPANESE -> "JavaScript 式を入力..."
        AppLanguage.KOREAN -> "JavaScript 식 입력..."
    }

    val preview: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "预览"
        AppLanguage.ENGLISH -> "Preview"
        AppLanguage.ARABIC -> "معاينة"
        AppLanguage.PORTUGUESE -> "Pré-visualização"
        AppLanguage.SPANISH -> "Vista previa"
        AppLanguage.FRENCH -> "Aperçu"
        AppLanguage.GERMAN -> "Vorschau"
        AppLanguage.RUSSIAN -> "Предпросмотр"
        AppLanguage.JAPANESE -> "プレビュー"
        AppLanguage.KOREAN -> "미리보기"
    }

    val exportFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Export failed"
        AppLanguage.ENGLISH -> "Export failed"
        AppLanguage.ARABIC -> "فشل التصدير"
        AppLanguage.PORTUGUESE -> "Falha na exportação"
        AppLanguage.SPANISH -> "Error de exportación"
        AppLanguage.FRENCH -> "Échec de l'exportation"
        AppLanguage.GERMAN -> "Export fehlgeschlagen"
        AppLanguage.RUSSIAN -> "Ошибка экспорта"
        AppLanguage.JAPANESE -> "エクスポート失敗"
        AppLanguage.KOREAN -> "내보내기 실패"
    }

    val selected: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已选择"
        AppLanguage.ENGLISH -> "Selected"
        AppLanguage.ARABIC -> "محدد"
        AppLanguage.PORTUGUESE -> "Selecionado"
        AppLanguage.SPANISH -> "Seleccionado"
        AppLanguage.FRENCH -> "Sélectionné"
        AppLanguage.GERMAN -> "Ausgewählt"
        AppLanguage.RUSSIAN -> "Выбрано"
        AppLanguage.JAPANESE -> "選択済み"
        AppLanguage.KOREAN -> "선택됨"
    }

    val export: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "导出"
        AppLanguage.ENGLISH -> "Export"
        AppLanguage.ARABIC -> "تصدير"
        AppLanguage.PORTUGUESE -> "Exportar"
        AppLanguage.SPANISH -> "Exportar"
        AppLanguage.FRENCH -> "Exporter"
        AppLanguage.GERMAN -> "Exportieren"
        AppLanguage.RUSSIAN -> "Экспорт"
        AppLanguage.JAPANESE -> "エクスポート"
        AppLanguage.KOREAN -> "내보내기"
    }

    val exportModule: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "导出模块"
        AppLanguage.ENGLISH -> "Export Module"
        AppLanguage.ARABIC -> "تصدير الوحدة"
        AppLanguage.PORTUGUESE -> "Exportar Módulo"
        AppLanguage.SPANISH -> "Exportar Módulo"
        AppLanguage.FRENCH -> "Exporter le Module"
        AppLanguage.GERMAN -> "Modul exportieren"
        AppLanguage.RUSSIAN -> "Экспортировать модуль"
        AppLanguage.JAPANESE -> "モジュールをエクスポート"
        AppLanguage.KOREAN -> "모듈 내보내기"
    }

    val exportToDownloads: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "保存到 Downloads"
        AppLanguage.ENGLISH -> "Save to Downloads"
        AppLanguage.ARABIC -> "حفظ في Downloads"
        AppLanguage.PORTUGUESE -> "Salvar em Downloads"
        AppLanguage.SPANISH -> "Guardar en Downloads"
        AppLanguage.FRENCH -> "Enregistrer dans Downloads"
        AppLanguage.GERMAN -> "In Downloads speichern"
        AppLanguage.RUSSIAN -> "Сохранить в Downloads"
        AppLanguage.JAPANESE -> "Downloads に保存"
        AppLanguage.KOREAN -> "Downloads에 저장"
    }

    val exportToDownloadsHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "保存到默认的下载文件夹"
        AppLanguage.ENGLISH -> "Save to default Downloads folder"
        AppLanguage.ARABIC -> "حفظ في مجلد التنزيلات الافتراضي"
        AppLanguage.PORTUGUESE -> "Salvar na pasta Downloads padrão"
        AppLanguage.SPANISH -> "Guardar en la carpeta Downloads predeterminada"
        AppLanguage.FRENCH -> "Enregistrer dans le dossier Downloads par défaut"
        AppLanguage.GERMAN -> "Im Standard-Downloads-Ordner speichern"
        AppLanguage.RUSSIAN -> "Сохранить в папку Downloads по умолчанию"
        AppLanguage.JAPANESE -> "デフォルトの Downloads フォルダに保存"
        AppLanguage.KOREAN -> "기본 Downloads 폴더에 저장"
    }

    val exportToCustomPath: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自定义存储路径"
        AppLanguage.ENGLISH -> "Custom Storage Path"
        AppLanguage.ARABIC -> "مسار تخزين مخصص"
        AppLanguage.PORTUGUESE -> "Caminho de Armazenamento Personalizado"
        AppLanguage.SPANISH -> "Ruta de Almacenamiento Personalizada"
        AppLanguage.FRENCH -> "Chemin de Stockage Personnalisé"
        AppLanguage.GERMAN -> "Benutzerdefinierter Speicherpfad"
        AppLanguage.RUSSIAN -> "Пользовательский путь хранения"
        AppLanguage.JAPANESE -> "カスタム保存パス"
        AppLanguage.KOREAN -> "사용자 지정 저장 경로"
    }

    val exportToCustomPathHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择存储位置"
        AppLanguage.ENGLISH -> "Choose storage location"
        AppLanguage.ARABIC -> "اختر موقع التخزين"
        AppLanguage.PORTUGUESE -> "Escolha o local de armazenamento"
        AppLanguage.SPANISH -> "Elija la ubicación de almacenamiento"
        AppLanguage.FRENCH -> "Choisir l'emplacement de stockage"
        AppLanguage.GERMAN -> "Speicherort wählen"
        AppLanguage.RUSSIAN -> "Выберите место хранения"
        AppLanguage.JAPANESE -> "保存場所を選択"
        AppLanguage.KOREAN -> "저장 위치 선택"
    }

    val exportSuccess: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Export successful"
        AppLanguage.ENGLISH -> "Export successful"
        AppLanguage.ARABIC -> "تم التصدير بنجاح"
        AppLanguage.PORTUGUESE -> "Exportação bem-sucedida"
        AppLanguage.SPANISH -> "Exportación exitosa"
        AppLanguage.FRENCH -> "Exportation réussie"
        AppLanguage.GERMAN -> "Export erfolgreich"
        AppLanguage.RUSSIAN -> "Экспорт завершён"
        AppLanguage.JAPANESE -> "エクスポート成功"
        AppLanguage.KOREAN -> "내보내기 성공"
    }

    val save: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "保存"
        AppLanguage.ENGLISH -> "Save"
        AppLanguage.ARABIC -> "حفظ"
        AppLanguage.PORTUGUESE -> "Salvar"
        AppLanguage.SPANISH -> "Guardar"
        AppLanguage.FRENCH -> "Enregistrer"
        AppLanguage.GERMAN -> "Speichern"
        AppLanguage.RUSSIAN -> "Сохранить"
        AppLanguage.JAPANESE -> "保存"
        AppLanguage.KOREAN -> "저장"
    }

    val delete: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Delete"
        AppLanguage.ENGLISH -> "Delete"
        AppLanguage.ARABIC -> "حذف"
        AppLanguage.PORTUGUESE -> "Excluir"
        AppLanguage.SPANISH -> "Eliminar"
        AppLanguage.FRENCH -> "Supprimer"
        AppLanguage.GERMAN -> "Löschen"
        AppLanguage.RUSSIAN -> "Удалить"
        AppLanguage.JAPANESE -> "削除"
        AppLanguage.KOREAN -> "삭제"
    }

    val pleaseEnterModuleName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "请输入模块名称"
        AppLanguage.ENGLISH -> "Please enter module name"
        AppLanguage.ARABIC -> "يرجى إدخال اسم الوحدة"
        AppLanguage.PORTUGUESE -> "Por favor, insira o nome do módulo"
        AppLanguage.SPANISH -> "Por favor, ingrese el nombre del módulo"
        AppLanguage.FRENCH -> "Veuillez saisir le nom du module"
        AppLanguage.GERMAN -> "Bitte Modulnamen eingeben"
        AppLanguage.RUSSIAN -> "Введите имя модуля"
        AppLanguage.JAPANESE -> "モジュール名を入力してください"
        AppLanguage.KOREAN -> "모듈 이름을 입력하세요"
    }

    val pleaseEnterCodeContent: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "请输入代码内容"
        AppLanguage.ENGLISH -> "Please enter code content"
        AppLanguage.ARABIC -> "يرجى إدخال محتوى الكود"
        AppLanguage.PORTUGUESE -> "Por favor, insira o conteúdo do código"
        AppLanguage.SPANISH -> "Por favor, ingrese el contenido del código"
        AppLanguage.FRENCH -> "Veuillez saisir le contenu du code"
        AppLanguage.GERMAN -> "Bitte Codeinhalt eingeben"
        AppLanguage.RUSSIAN -> "Введите содержимое кода"
        AppLanguage.JAPANESE -> "コード内容を入力してください"
        AppLanguage.KOREAN -> "코드 내용을 입력하세요"
    }

    val saveSuccess: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Save successful"
        AppLanguage.ENGLISH -> "Save successful"
        AppLanguage.ARABIC -> "تم الحفظ بنجاح"
        AppLanguage.PORTUGUESE -> "Salvo com sucesso"
        AppLanguage.SPANISH -> "Guardado con éxito"
        AppLanguage.FRENCH -> "Enregistré avec succès"
        AppLanguage.GERMAN -> "Erfolgreich gespeichert"
        AppLanguage.RUSSIAN -> "Сохранение завершено"
        AppLanguage.JAPANESE -> "保存成功"
        AppLanguage.KOREAN -> "저장 성공"
    }

    val storagePermissionRequired: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "需要存储权限才能下载文件"
        AppLanguage.ENGLISH -> "Storage permission required to download files"
        AppLanguage.ARABIC -> "يلزم إذن التخزين لتحميل الملفات"
        AppLanguage.PORTUGUESE -> "Permissão de armazenamento necessária para baixar arquivos"
        AppLanguage.SPANISH -> "Se requiere permiso de almacenamiento para descargar archivos"
        AppLanguage.FRENCH -> "Permission de stockage requise pour télécharger des fichiers"
        AppLanguage.GERMAN -> "Speicherberechtigung zum Herunterladen von Dateien erforderlich"
        AppLanguage.RUSSIAN -> "Для загрузки файлов требуется разрешение на хранение"
        AppLanguage.JAPANESE -> "ファイルをダウンロードするにはストレージ権限が必要です"
        AppLanguage.KOREAN -> "파일을 다운로드하려면 저장소 권한이 필요합니다"
    }

    val appConfigLoadFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "应用配置加载失败，请重新安装"
        AppLanguage.ENGLISH -> "App configuration load failed, please reinstall"
        AppLanguage.ARABIC -> "فشل تحميل تكوين التطبيق، يرجى إعادة التثبيت"
        AppLanguage.PORTUGUESE -> "Falha ao carregar a configuração do app, reinstale"
        AppLanguage.SPANISH -> "Error al cargar la configuración de la app, reinstale"
        AppLanguage.FRENCH -> "Échec du chargement de la configuration, veuillez réinstaller"
        AppLanguage.GERMAN -> "App-Konfiguration konnte nicht geladen werden, bitte neu installieren"
        AppLanguage.RUSSIAN -> "Не удалось загрузить конфигурацию приложения, переустановите"
        AppLanguage.JAPANESE -> "アプリ設定の読み込みに失敗しました。再インストールしてください"
        AppLanguage.KOREAN -> "앱 설정 로드 실패, 재설치해주세요"
    }

    val pressAgainToExit: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "再按一次退出"
        AppLanguage.ENGLISH -> "Press back again to exit"
        AppLanguage.ARABIC -> "اضغط رجوع مرة أخرى للخروج"
        AppLanguage.PORTUGUESE -> "Pressione voltar novamente para sair"
        AppLanguage.SPANISH -> "Pulsa atrás de nuevo para salir"
        AppLanguage.FRENCH -> "Appuyez à nouveau sur retour pour quitter"
        AppLanguage.GERMAN -> "Zum Beenden erneut zurück drücken"
        AppLanguage.RUSSIAN -> "Нажмите «назад» ещё раз для выхода"
        AppLanguage.JAPANESE -> "もう一度戻るを押すと終了します"
        AppLanguage.KOREAN -> "한 번 더 누르면 종료됩니다"
    }

    val shortcutCreatedSuccess: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "快捷方式创建成功"
        AppLanguage.ENGLISH -> "Shortcut created successfully"
        AppLanguage.ARABIC -> "تم إنشاء الاختصار بنجاح"
        AppLanguage.PORTUGUESE -> "Atalho criado com sucesso"
        AppLanguage.SPANISH -> "Acceso directo creado con éxito"
        AppLanguage.FRENCH -> "Raccourci créé avec succès"
        AppLanguage.GERMAN -> "Verknüpfung erfolgreich erstellt"
        AppLanguage.RUSSIAN -> "Ярлык успешно создан"
        AppLanguage.JAPANESE -> "ショートカットを作成しました"
        AppLanguage.KOREAN -> "바로가기 생성 성공"
    }

    val projectExportedTo: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "项目已导出到: %s"
        AppLanguage.ENGLISH -> "Project exported to: %s"
        AppLanguage.ARABIC -> "تم تصدير المشروع إلى: %s"
        AppLanguage.PORTUGUESE -> "Projeto exportado para: %s"
        AppLanguage.SPANISH -> "Proyecto exportado a: %s"
        AppLanguage.FRENCH -> "Projet exporté vers : %s"
        AppLanguage.GERMAN -> "Projekt exportiert nach: %s"
        AppLanguage.RUSSIAN -> "Проект экспортирован в: %s"
        AppLanguage.JAPANESE -> "プロジェクトをエクスポートしました: %s"
        AppLanguage.KOREAN -> "프로젝트 내보냄: %s"
    }

    val preparing: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "准备中..."
        AppLanguage.ENGLISH -> "Preparing..."
        AppLanguage.ARABIC -> "جاري التحضير..."
        AppLanguage.PORTUGUESE -> "Preparando..."
        AppLanguage.SPANISH -> "Preparando..."
        AppLanguage.FRENCH -> "Préparation..."
        AppLanguage.GERMAN -> "Wird vorbereitet..."
        AppLanguage.RUSSIAN -> "Подготовка..."
        AppLanguage.JAPANESE -> "準備中..."
        AppLanguage.KOREAN -> "준비 중..."
    }

    val buildApkForApp: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "将为「%s」构建独立的 APK 安装包。"
        AppLanguage.ENGLISH -> "Will build standalone APK for \"%s\"."
        AppLanguage.ARABIC -> "سيتم بناء APK مستقل لـ \"%s\"."
        AppLanguage.PORTUGUESE -> "Será construído APK autônomo para \"%s\"."
        AppLanguage.SPANISH -> "Se construirá APK independiente para \"%s\"."
        AppLanguage.FRENCH -> "Construction d'un APK autonome pour \"%s\"."
        AppLanguage.GERMAN -> "Standalone-APK für \"%s\" wird erstellt."
        AppLanguage.RUSSIAN -> "Будет создан автономный APK для \"%s\"."
        AppLanguage.JAPANESE -> "「%s」のスタンドアロン APK をビルドします。"
        AppLanguage.KOREAN -> "\"%s\"용 독립 APK를 빌드합니다."
    }

    val buildCompleteInstallHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "构建完成后可直接安装到设备上，无需创建快捷方式。"
        AppLanguage.ENGLISH -> "After build, can be installed directly without creating shortcut."
        AppLanguage.ARABIC -> "بعد البناء، يمكن التثبيت مباشرة دون إنشاء اختصار."
        AppLanguage.PORTUGUESE -> "Após build, pode ser instalado diretamente sem criar atalho."
        AppLanguage.SPANISH -> "Tras build, se puede instalar directamente sin crear acceso directo."
        AppLanguage.FRENCH -> "Après build, installation directe sans créer de raccourci."
        AppLanguage.GERMAN -> "Nach dem Build direkt installierbar, ohne Verknüpfung zu erstellen."
        AppLanguage.RUSSIAN -> "После сборки можно установить напрямую без ярлыка."
        AppLanguage.JAPANESE -> "ビルド後、ショートカットを作成せず直接インストールできます。"
        AppLanguage.KOREAN -> "빌드 후 바로가기 없이 직접 설치할 수 있습니다."
    }

    val buildSummaryTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "构建摘要"
        AppLanguage.ENGLISH -> "Build Summary"
        AppLanguage.ARABIC -> "ملخص البناء"
        AppLanguage.PORTUGUESE -> "Resumo do Build"
        AppLanguage.SPANISH -> "Resumen de Build"
        AppLanguage.FRENCH -> "Résumé du Build"
        AppLanguage.GERMAN -> "Build-Zusammenfassung"
        AppLanguage.RUSSIAN -> "Сводка сборки"
        AppLanguage.JAPANESE -> "ビルド概要"
        AppLanguage.KOREAN -> "빌드 요약"
    }

}

