package com.webtoapp.core.i18n

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

object StringsC {

    val tagAuto: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自动"
        AppLanguage.ENGLISH -> "Auto"
        AppLanguage.ARABIC -> "تلقائي"
        AppLanguage.PORTUGUESE -> "Automático"
        AppLanguage.SPANISH -> "Automático"
        AppLanguage.FRENCH -> "Automatique"
        AppLanguage.GERMAN -> "Auto"
        AppLanguage.RUSSIAN -> "Авто"
        AppLanguage.JAPANESE -> "自動"
        AppLanguage.KOREAN -> "자동"
    }

    val builtinMediaDownloader: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "媒体下载"
        AppLanguage.ENGLISH -> "Media Downloader"
        AppLanguage.ARABIC -> "تحميل الوسائط"
        AppLanguage.PORTUGUESE -> "Download de Mídia"
        AppLanguage.SPANISH -> "Descarga de Medios"
        AppLanguage.FRENCH -> "Téléchargement Multimédia"
        AppLanguage.GERMAN -> "Medien-Download"
        AppLanguage.RUSSIAN -> "Загрузка медиа"
        AppLanguage.JAPANESE -> "メディアダウンロード"
        AppLanguage.KOREAN -> "미디어 다운로드"
    }
    val builtinMediaDownloaderDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自动检测并下载网页视频、图片。支持 YouTube、B站、抖音、小红书、Instagram、Facebook、TikTok，以及通用网页视频检测"
        AppLanguage.ENGLISH -> "Auto-detect and download web videos and images. Supports YouTube, Bilibili, Douyin, Xiaohongshu, Instagram, Facebook, TikTok, and generic web video detection"
        AppLanguage.ARABIC -> "الكشف التلقائي وتحميل فيديوهات وصور الويب. يدعم يوتيوب وبيليبيلي ودوين وشياوهونغشو وإنستغرام وفيسبوك وتيك توك والكشف العام عن الفيديو"
        AppLanguage.PORTUGUESE -> "Detectar e baixar automaticamente vídeos e imagens da web. Suporta YouTube, Bilibili, Douyin, Xiaohongshu, Instagram, Facebook, TikTok, e detecção genérica de vídeos web"
        AppLanguage.SPANISH -> "Detectar y descargar automáticamente vídeos e imágenes web. Soporta YouTube, Bilibili, Douyin, Xiaohongshu, Instagram, Facebook, TikTok, y detección genérica de vídeos web"
        AppLanguage.FRENCH -> "Détecter et télécharger automatiquement les vidéos et images web. Prend en charge YouTube, Bilibili, Douyin, Xiaohongshu, Instagram, Facebook, TikTok, et la détection générique des vidéos web"
        AppLanguage.GERMAN -> "Web-Videos und -Bilder automatisch erkennen und herunterladen. Unterstützt YouTube, Bilibili, Douyin, Xiaohongshu, Instagram, Facebook, TikTok und generische Web-Video-Erkennung"
        AppLanguage.RUSSIAN -> "Автоматически обнаруживать и загружать веб-видео и изображения. Поддерживает YouTube, Bilibili, Douyin, Xiaohongshu, Instagram, Facebook, TikTok, а также общую детекцию веб-видео"
        AppLanguage.JAPANESE -> "ウェブ動画や画像を自動検出してダウンロード。YouTube、Bilibili、Douyin、Xiaohongshu、Instagram、Facebook、TikTok、および一般的なウェブ動画検出をサポート"
        AppLanguage.KOREAN -> "웹 동영상과 이미지를 자동 감지하여 다운로드. YouTube, Bilibili, Douyin, Xiaohongshu, Instagram, Facebook, TikTok 및 일반적인 웹 동영상 감지 지원"
    }
    val builtinVideoEnhancer: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "视频增强"
        AppLanguage.ENGLISH -> "Video Enhancer"
        AppLanguage.ARABIC -> "تحسين الفيديو"
        AppLanguage.PORTUGUESE -> "Aprimoramento de Vídeo"
        AppLanguage.SPANISH -> "Mejora de Vídeo"
        AppLanguage.FRENCH -> "Amélioration Vidéo"
        AppLanguage.GERMAN -> "Video-Optimierung"
        AppLanguage.RUSSIAN -> "Улучшение видео"
        AppLanguage.JAPANESE -> "動画強化"
        AppLanguage.KOREAN -> "동영상 향상"
    }
    val builtinVideoEnhancerDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "倍速播放、画中画、循环、跳转，以及 YouTube 净化"
        AppLanguage.ENGLISH -> "Playback speed, picture-in-picture, loop, seek, plus YouTube cleanup"
        AppLanguage.ARABIC -> "سرعة التشغيل، صورة داخل صورة، تكرار، انتقال، بالإضافة إلى تنظيف يوتيوب"
        AppLanguage.PORTUGUESE -> "Velocidade de reprodução, picture-in-picture, loop, busca, além da limpeza do YouTube"
        AppLanguage.SPANISH -> "Velocidad de reproducción, imagen en imagen, bucle, salto, más limpieza de YouTube"
        AppLanguage.FRENCH -> "Vitesse de lecture, image dans l'image, boucle, déplacement, plus le nettoyage YouTube"
        AppLanguage.GERMAN -> "Wiedergabegeschwindigkeit, Bild-in-Bild, Schleife, Springen, plus YouTube-Bereinigung"
        AppLanguage.RUSSIAN -> "Скорость воспроизведения, картинка в картинке, повтор, перемотка, плюс очистка YouTube"
        AppLanguage.JAPANESE -> "再生速度、ピクチャーインピクチャー、ループ、シーク、さらに YouTube のクリーンアップ"
        AppLanguage.KOREAN -> "재생 속도, PIP, 반복, 탐색, 그리고 YouTube 정리"
    }
    val builtinWebAnalyzer: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "网页分析"
        AppLanguage.ENGLISH -> "Web Analyzer"
        AppLanguage.ARABIC -> "محلل الويب"
        AppLanguage.PORTUGUESE -> "Analisador Web"
        AppLanguage.SPANISH -> "Analizador Web"
        AppLanguage.FRENCH -> "Analyseur Web"
        AppLanguage.GERMAN -> "Web-Analysator"
        AppLanguage.RUSSIAN -> "Веб-анализатор"
        AppLanguage.JAPANESE -> "ウェブアナライザー"
        AppLanguage.KOREAN -> "웹 분석기"
    }
    val builtinWebAnalyzerDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "查看页面信息、加载性能和元素统计"
        AppLanguage.ENGLISH -> "Inspect page info, load performance and element statistics"
        AppLanguage.ARABIC -> "فحص معلومات الصفحة وأداء التحميل وإحصاءات العناصر"
        AppLanguage.PORTUGUESE -> "Inspecionar informações da página, desempenho de carregamento e estatísticas de elementos"
        AppLanguage.SPANISH -> "Inspeccionar información de la página, rendimiento de carga y estadísticas de elementos"
        AppLanguage.FRENCH -> "Inspecter les infos de la page, les performances de chargement et les statistiques d'éléments"
        AppLanguage.GERMAN -> "Seiteninfos, Ladeleistung und Elementstatistiken prüfen"
        AppLanguage.RUSSIAN -> "Просмотр сведений о странице, производительности загрузки и статистики элементов"
        AppLanguage.JAPANESE -> "ページ情報、読み込み性能、要素の統計を確認"
        AppLanguage.KOREAN -> "페이지 정보, 로드 성능, 요소 통계 확인"
    }
    val builtinFindInPage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "页内查找"
        AppLanguage.ENGLISH -> "Find in Page"
        AppLanguage.ARABIC -> "بحث في الصفحة"
        AppLanguage.PORTUGUESE -> "Encontrar na Página"
        AppLanguage.SPANISH -> "Buscar en la Página"
        AppLanguage.FRENCH -> "Rechercher dans la Page"
        AppLanguage.GERMAN -> "In Seite Suchen"
        AppLanguage.RUSSIAN -> "Найти на странице"
        AppLanguage.JAPANESE -> "ページ内検索"
        AppLanguage.KOREAN -> "페이지에서 찾기"
    }
    val builtinFindInPageDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "像浏览器一样在当前页面搜索关键词，支持高亮、上一个和下一个"
        AppLanguage.ENGLISH -> "Search the current page like a browser, with highlight, previous and next"
        AppLanguage.ARABIC -> "ابحث في الصفحة الحالية مثل المتصفح، مع التمييز والتنقل بين النتائج"
        AppLanguage.PORTUGUESE -> "Pesquisar a página atual como um navegador, com destaque, anterior e próximo"
        AppLanguage.SPANISH -> "Buscar en la página actual como un navegador, con resaltado, anterior y siguiente"
        AppLanguage.FRENCH -> "Rechercher dans la page actuelle comme un navigateur, avec surlignage, précédent et suivant"
        AppLanguage.GERMAN -> "Aktuelle Seite wie einen Browser durchsuchen, mit Hervorhebung, Zurück und Weiter"
        AppLanguage.RUSSIAN -> "Искать на текущей странице как в браузере, с подсветкой, предыдущей и следующей"
        AppLanguage.JAPANESE -> "ブラウザのように現在のページを検索、ハイライト、前へ/次へ付き"
        AppLanguage.KOREAN -> "브라우저처럼 현재 페이지 검색, 강조 표시, 이전/다음 지원"
    }
    val builtinDarkMode: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "深色模式"
        AppLanguage.ENGLISH -> "Dark Mode"
        AppLanguage.ARABIC -> "الوضع الداكن"
        AppLanguage.PORTUGUESE -> "Modo Escuro"
        AppLanguage.SPANISH -> "Modo Oscuro"
        AppLanguage.FRENCH -> "Mode Sombre"
        AppLanguage.GERMAN -> "Dunkelmodus"
        AppLanguage.RUSSIAN -> "Тёмный режим"
        AppLanguage.JAPANESE -> "ダークモード"
        AppLanguage.KOREAN -> "다크 모드"
    }
    val builtinDarkModeDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "智能反色，适用于任意页面，可从工具栏或面板开关"
        AppLanguage.ENGLISH -> "Smart inversion for any page. Toggle from the toolbar or the panel"
        AppLanguage.ARABIC -> "عكس ذكي لأي صفحة. التبديل من شريط الأدوات أو اللوحة"
        AppLanguage.PORTUGUESE -> "Inversão inteligente para qualquer página. Alterne pela barra de ferramentas ou pelo painel"
        AppLanguage.SPANISH -> "Inversión inteligente para cualquier página. Actívalo desde la barra o el panel"
        AppLanguage.FRENCH -> "Inversion intelligente pour n'importe quelle page. Activez-la depuis la barre d'outils ou le panneau"
        AppLanguage.GERMAN -> "Intelligente Invertierung für jede Seite. Umschalten über die Symbolleiste oder das Panel"
        AppLanguage.RUSSIAN -> "Умная инверсия для любой страницы. Переключение с панели инструментов или из панели"
        AppLanguage.JAPANESE -> "どのページでも使えるスマート反転。ツールバーまたはパネルから切り替え"
        AppLanguage.KOREAN -> "모든 페이지에 적용되는 스마트 반전. 툴바 또는 패널에서 전환"
    }
    val builtinPrivacyProtection: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "隐私保护"
        AppLanguage.ENGLISH -> "Privacy Protection"
        AppLanguage.ARABIC -> "حماية الخصوصية"
        AppLanguage.PORTUGUESE -> "Proteção de Privacidade"
        AppLanguage.SPANISH -> "Protección de Privacidad"
        AppLanguage.FRENCH -> "Protection de la Vie Privée"
        AppLanguage.GERMAN -> "Datenschutz"
        AppLanguage.RUSSIAN -> "Защита конфиденциальности"
        AppLanguage.JAPANESE -> "プライバシー保護"
        AppLanguage.KOREAN -> "개인정보 보호"
    }
    val builtinPrivacyProtectionDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "阻止追踪、模糊指纹信号，退出时清除 Cookie"
        AppLanguage.ENGLISH -> "Block trackers, blur fingerprint signals, clear cookies on exit"
        AppLanguage.ARABIC -> "حظر المتعقبات، طمس إشارات البصمة، مسح ملفات تعريف الارتباط عند الخروج"
        AppLanguage.PORTUGUESE -> "Bloquear rastreadores, ofuscar sinais de impressão digital, limpar cookies ao sair"
        AppLanguage.SPANISH -> "Bloquear rastreadores, difuminar señales de huella digital, borrar cookies al salir"
        AppLanguage.FRENCH -> "Bloquer les traqueurs, flouter les signaux d'empreinte, effacer les cookies à la sortie"
        AppLanguage.GERMAN -> "Tracker blockieren, Fingerabdrucksignale unkenntlich machen, Cookies beim Beenden löschen"
        AppLanguage.RUSSIAN -> "Блокировать трекеры, размывать сигналы отпечатка, очищать cookie при выходе"
        AppLanguage.JAPANESE -> "トラッカーをブロック、フィンガープリント信号をぼかし、終了時に Cookie を消去"
        AppLanguage.KOREAN -> "추적기 차단, 지문 신호 흐리기, 종료 시 쿠키 삭제"
    }
    val builtinContentEnhancer: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "内容增强"
        AppLanguage.ENGLISH -> "Content Enhancer"
        AppLanguage.ARABIC -> "تحسين المحتوى"
        AppLanguage.PORTUGUESE -> "Aprimoramento de Conteúdo"
        AppLanguage.SPANISH -> "Mejora de Contenido"
        AppLanguage.FRENCH -> "Amélioration du Contenu"
        AppLanguage.GERMAN -> "Inhalt-Optimierung"
        AppLanguage.RUSSIAN -> "Улучшение контента"
        AppLanguage.JAPANESE -> "コンテンツ強化"
        AppLanguage.KOREAN -> "콘텐츠 향상"
    }
    val builtinContentEnhancerDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "解除复制限制，复制页面文本或 HTML，快速滚到顶部或底部"
        AppLanguage.ENGLISH -> "Unlock copy, copy page text or HTML, jump to the top or bottom"
        AppLanguage.ARABIC -> "إلغاء قيود النسخ، نسخ نص الصفحة أو HTML، الانتقال إلى الأعلى أو الأسفل"
        AppLanguage.PORTUGUESE -> "Liberar cópia, copiar texto ou HTML da página, ir ao topo ou ao fim"
        AppLanguage.SPANISH -> "Desbloquear copia, copiar texto o HTML de la página, ir al inicio o al final"
        AppLanguage.FRENCH -> "Débloquer la copie, copier le texte ou le HTML de la page, aller en haut ou en bas"
        AppLanguage.GERMAN -> "Kopiersperre aufheben, Seitentext oder HTML kopieren, nach oben oder unten springen"
        AppLanguage.RUSSIAN -> "Снять запрет копирования, копировать текст или HTML страницы, перейти вверх или вниз"
        AppLanguage.JAPANESE -> "コピー制限を解除し、ページのテキストや HTML をコピー、先頭や末尾へ移動"
        AppLanguage.KOREAN -> "복사 제한 해제, 페이지 텍스트 또는 HTML 복사, 맨 위 또는 맨 아래로 이동"
    }
    val builtinElementBlocker: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "元素屏蔽器"
        AppLanguage.ENGLISH -> "Element Blocker"
        AppLanguage.ARABIC -> "مانع العناصر"
        AppLanguage.PORTUGUESE -> "Bloqueador de Elementos"
        AppLanguage.SPANISH -> "Bloqueador de Elementos"
        AppLanguage.FRENCH -> "Bloqueur d'Éléments"
        AppLanguage.GERMAN -> "Element-Blocker"
        AppLanguage.RUSSIAN -> "Блокировщик элементов"
        AppLanguage.JAPANESE -> "要素ブロッカー"
        AppLanguage.KOREAN -> "요소 차단기"
    }
    val builtinElementBlockerDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "单击选择、双击屏蔽烦人元素，规则按网站保存"
        AppLanguage.ENGLISH -> "Click to select, double-click to hide elements. Rules persist per site"
        AppLanguage.ARABIC -> "انقر للتحديد، انقر مرتين لإخفاء العناصر. تُحفظ القواعد لكل موقع"
        AppLanguage.PORTUGUESE -> "Clique para selecionar, clique duplo para ocultar. As regras persistem por site"
        AppLanguage.SPANISH -> "Clic para seleccionar, doble clic para ocultar. Las reglas se conservan por sitio"
        AppLanguage.FRENCH -> "Cliquer pour sélectionner, double-cliquer pour masquer. Les règles persistent par site"
        AppLanguage.GERMAN -> "Klicken zum Auswählen, Doppelklick zum Ausblenden. Regeln gelten pro Website"
        AppLanguage.RUSSIAN -> "Клик для выбора, двойной клик для скрытия. Правила сохраняются для сайта"
        AppLanguage.JAPANESE -> "クリックで選択、ダブルクリックで非表示。ルールはサイトごとに保存"
        AppLanguage.KOREAN -> "클릭하여 선택, 더블클릭으로 숨김. 규칙은 사이트별로 유지"
    }

    val builtinPageMenu: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "页面菜单"
        AppLanguage.ENGLISH -> "Page Menu"
        AppLanguage.ARABIC -> "قائمة الصفحة"
        AppLanguage.PORTUGUESE -> "Menu da Página"
        AppLanguage.SPANISH -> "Menú de la Página"
        AppLanguage.FRENCH -> "Menu de la Page"
        AppLanguage.GERMAN -> "Seitenmenü"
        AppLanguage.RUSSIAN -> "Меню страницы"
        AppLanguage.JAPANESE -> "ページメニュー"
        AppLanguage.KOREAN -> "페이지 메뉴"
    }
    val builtinPageMenuDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "浮动页面工具：刷新、前进后退、跳转网址、分享链接、外部打开、清除数据、隐藏菜单、退出"
        AppLanguage.ENGLISH -> "Floating page tools: refresh, back/forward, jump to a URL, share, open externally, clear data, hide menu, exit"
        AppLanguage.ARABIC -> "أدوات صفحة عائمة: تحديث، رجوع/تقدم، الانتقال إلى رابط، مشاركة، فتح خارجيًا، مسح البيانات، إخفاء القائمة، خروج"
        AppLanguage.PORTUGUESE -> "Ferramentas flutuantes: atualizar, voltar/avançar, ir a um URL, compartilhar, abrir externamente, limpar dados, ocultar menu, sair"
        AppLanguage.SPANISH -> "Herramientas flotantes: actualizar, atrás/adelante, ir a una URL, compartir, abrir externamente, borrar datos, ocultar menú, salir"
        AppLanguage.FRENCH -> "Outils flottants : actualiser, précédent/suivant, aller à une URL, partager, ouvrir à l'extérieur, effacer les données, masquer le menu, quitter"
        AppLanguage.GERMAN -> "Schwebende Seitenwerkzeuge: Aktualisieren, Zurück/Vor, URL öffnen, teilen, extern öffnen, Daten löschen, Menü ausblenden, beenden"
        AppLanguage.RUSSIAN -> "Плавающие инструменты: обновить, назад/вперёд, перейти по URL, поделиться, открыть внешне, очистить данные, скрыть меню, выход"
        AppLanguage.JAPANESE -> "フローティングのページツール：更新、戻る/進む、URL へ移動、共有、外部で開く、データ消去、メニュー非表示、終了"
        AppLanguage.KOREAN -> "플로팅 페이지 도구: 새로고침, 뒤로/앞으로, URL 이동, 공유, 외부에서 열기, 데이터 지우기, 메뉴 숨기기, 종료"
    }

    val builtinUrlSwitcher: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "网址切换"
        AppLanguage.ENGLISH -> "URL Switcher"
        AppLanguage.ARABIC -> "تبديل الروابط"
        AppLanguage.PORTUGUESE -> "Alternar URL"
        AppLanguage.SPANISH -> "Cambiar URL"
        AppLanguage.FRENCH -> "Changer d’URL"
        AppLanguage.GERMAN -> "URL wechseln"
        AppLanguage.RUSSIAN -> "Смена URL"
        AppLanguage.JAPANESE -> "URL切り替え"
        AppLanguage.KOREAN -> "URL 전환"
    }
    val builtinUrlSwitcherDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "保存网址并在任意应用里切换，不占用页面高度"
        AppLanguage.ENGLISH -> "Save URLs and switch between them in any app without taking page space"
        AppLanguage.ARABIC -> "احفظ الروابط وبدّل بينها في أي تطبيق دون شغل الصفحة"
        AppLanguage.PORTUGUESE -> "Salve URLs e alterne entre eles em qualquer app sem ocupar a página"
        AppLanguage.SPANISH -> "Guarda URLs y cámbiate entre ellas en cualquier app sin ocupar la página"
        AppLanguage.FRENCH -> "Enregistrez des URL et basculez depuis n’importe quelle app sans occuper la page"
        AppLanguage.GERMAN -> "URLs speichern und in jeder App wechseln, ohne Seitenplatz zu belegen"
        AppLanguage.RUSSIAN -> "Сохраняйте адреса и переключайтесь в любом приложении, не занимая место"
        AppLanguage.JAPANESE -> "URLを保存し、どのアプリからでも切り替え。ページ領域を占有しない"
        AppLanguage.KOREAN -> "URL을 저장하고 어떤 앱에서든 전환, 페이지 영역을 차지하지 않음"
    }

    // Qualify through the facade so generateShellStrings follows the closure
    // into each name/desc property (bare identifiers in this object are not scanned).
    fun builtinPluginName(id: String, fallback: String): String = when (id) {
        "builtin-media-downloader" -> Strings.builtinMediaDownloader
        "builtin-video-enhancer" -> Strings.builtinVideoEnhancer
        "builtin-web-analyzer" -> Strings.builtinWebAnalyzer
        "builtin-find-in-page" -> Strings.builtinFindInPage
        "builtin-dark-mode" -> Strings.builtinDarkMode
        "builtin-privacy-protection" -> Strings.builtinPrivacyProtection
        "builtin-content-enhancer" -> Strings.builtinContentEnhancer
        "builtin-element-blocker" -> Strings.builtinElementBlocker
        "builtin-page-menu" -> Strings.builtinPageMenu
        "builtin-url-switcher" -> Strings.builtinUrlSwitcher
        else -> fallback
    }

    fun builtinPluginDescription(id: String, fallback: String): String = when (id) {
        "builtin-media-downloader" -> Strings.builtinMediaDownloaderDesc
        "builtin-video-enhancer" -> Strings.builtinVideoEnhancerDesc
        "builtin-web-analyzer" -> Strings.builtinWebAnalyzerDesc
        "builtin-find-in-page" -> Strings.builtinFindInPageDesc
        "builtin-dark-mode" -> Strings.builtinDarkModeDesc
        "builtin-privacy-protection" -> Strings.builtinPrivacyProtectionDesc
        "builtin-content-enhancer" -> Strings.builtinContentEnhancerDesc
        "builtin-element-blocker" -> Strings.builtinElementBlockerDesc
        "builtin-page-menu" -> Strings.builtinPageMenuDesc
        "builtin-url-switcher" -> Strings.builtinUrlSwitcherDesc
        else -> fallback
    }

    val lrcThemeDefault: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "默认"
        AppLanguage.ENGLISH -> "Default"
        AppLanguage.ARABIC -> "افتراضي"
        AppLanguage.PORTUGUESE -> "Padrão"
        AppLanguage.SPANISH -> "Predeterminado"
        AppLanguage.FRENCH -> "Par défaut"
        AppLanguage.GERMAN -> "Standard"
        AppLanguage.RUSSIAN -> "По умолчанию"
        AppLanguage.JAPANESE -> "デフォルト"
        AppLanguage.KOREAN -> "기본"
    }
    val lrcThemeKaraoke: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "卡拉OK"
        AppLanguage.ENGLISH -> "Karaoke"
        AppLanguage.ARABIC -> "كاريوكي"
        AppLanguage.PORTUGUESE -> "Karaokê"
        AppLanguage.SPANISH -> "Karaoke"
        AppLanguage.FRENCH -> "Karaoké"
        AppLanguage.GERMAN -> "Karaoke"
        AppLanguage.RUSSIAN -> "Караоке"
        AppLanguage.JAPANESE -> "カラオケ"
        AppLanguage.KOREAN -> "노래방"
    }
    val lrcThemeNeon: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "霓虹"
        AppLanguage.ENGLISH -> "Neon"
        AppLanguage.ARABIC -> "نيون"
        AppLanguage.PORTUGUESE -> "Neon"
        AppLanguage.SPANISH -> "Neón"
        AppLanguage.FRENCH -> "Néon"
        AppLanguage.GERMAN -> "Neon"
        AppLanguage.RUSSIAN -> "Неон"
        AppLanguage.JAPANESE -> "ネオン"
        AppLanguage.KOREAN -> "네온"
    }
    val lrcThemeMinimal: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "极简"
        AppLanguage.ENGLISH -> "Minimal"
        AppLanguage.ARABIC -> "بسيط"
        AppLanguage.PORTUGUESE -> "Minimalista"
        AppLanguage.SPANISH -> "Minimalista"
        AppLanguage.FRENCH -> "Minimaliste"
        AppLanguage.GERMAN -> "Minimalistisch"
        AppLanguage.RUSSIAN -> "Минимализм"
        AppLanguage.JAPANESE -> "ミニマル"
        AppLanguage.KOREAN -> "미니멀"
    }
    val lrcThemeClassic: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "经典"
        AppLanguage.ENGLISH -> "Classic"
        AppLanguage.ARABIC -> "كلاسيكي"
        AppLanguage.PORTUGUESE -> "Clássico"
        AppLanguage.SPANISH -> "Clásico"
        AppLanguage.FRENCH -> "Classique"
        AppLanguage.GERMAN -> "Klassisch"
        AppLanguage.RUSSIAN -> "Классический"
        AppLanguage.JAPANESE -> "クラシック"
        AppLanguage.KOREAN -> "클래식"
    }
    val lrcThemeDark: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "暗夜"
        AppLanguage.ENGLISH -> "Dark"
        AppLanguage.ARABIC -> "داكن"
        AppLanguage.PORTUGUESE -> "Escuro"
        AppLanguage.SPANISH -> "Oscuro"
        AppLanguage.FRENCH -> "Sombre"
        AppLanguage.GERMAN -> "Dunkel"
        AppLanguage.RUSSIAN -> "Тёмный"
        AppLanguage.JAPANESE -> "ダーク"
        AppLanguage.KOREAN -> "다크"
    }
    val lrcThemeRomantic: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "浪漫"
        AppLanguage.ENGLISH -> "Romantic"
        AppLanguage.ARABIC -> "رومانسي"
        AppLanguage.PORTUGUESE -> "Romântico"
        AppLanguage.SPANISH -> "Romántico"
        AppLanguage.FRENCH -> "Romantique"
        AppLanguage.GERMAN -> "Romantisch"
        AppLanguage.RUSSIAN -> "Романтичный"
        AppLanguage.JAPANESE -> "ロマンティック"
        AppLanguage.KOREAN -> "로맨틱"
    }
    val lrcThemeEnergetic: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "活力"
        AppLanguage.ENGLISH -> "Energetic"
        AppLanguage.ARABIC -> "نشط"
        AppLanguage.PORTUGUESE -> "Energético"
        AppLanguage.SPANISH -> "Enérgico"
        AppLanguage.FRENCH -> "Énergique"
        AppLanguage.GERMAN -> "Energetisch"
        AppLanguage.RUSSIAN -> "Энергичный"
        AppLanguage.JAPANESE -> "エネルギッシュ"
        AppLanguage.KOREAN -> "에너제틱"
    }

    val featureAgent: String get() = when (Strings.lang) {
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
    val featureAgentDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "用 AI 直接构建网页、模块、应用 — 在手机上"
        AppLanguage.ENGLISH -> "Build pages, modules, and apps with AI — directly on your phone"
        AppLanguage.ARABIC -> "بناء الصفحات والوحدات والتطبيقات باستخدام الذكاء الاصطناعي — مباشرة على هاتفك"
        AppLanguage.PORTUGUESE -> "Crie páginas, módulos e apps com IA — diretamente no seu telefone"
        AppLanguage.SPANISH -> "Crea páginas, módulos y apps con IA — directamente en tu teléfono"
        AppLanguage.FRENCH -> "Créez des pages, des modules et des apps avec l'IA — directement sur votre téléphone"
        AppLanguage.GERMAN -> "Erstellen Sie Seiten, Module und Apps mit KI — direkt auf Ihrem Telefon"
        AppLanguage.RUSSIAN -> "Создавайте страницы, модули и приложения с ИИ — прямо на телефоне"
        AppLanguage.JAPANESE -> "AIでページ、モジュール、アプリを構築 — スマホで直接"
        AppLanguage.KOREAN -> "AI로 페이지, 모듈, 앱 빌드 — 휴대폰에서 직접"
    }
    val featureAgentImage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Agent（图像）"
        AppLanguage.ENGLISH -> "Agent (Image)"
        AppLanguage.ARABIC -> "Agent (صورة)"
        AppLanguage.PORTUGUESE -> "Agent (Imagem)"
        AppLanguage.SPANISH -> "Agent (Imagen)"
        AppLanguage.FRENCH -> "Agent (Image)"
        AppLanguage.GERMAN -> "Agent (Bild)"
        AppLanguage.RUSSIAN -> "Agent (Изображение)"
        AppLanguage.JAPANESE -> "Agent（画像）"
        AppLanguage.KOREAN -> "Agent (이미지)"
    }
    val featureAgentImageDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "在 Agent 会话中生成、查看、迭代图像"
        AppLanguage.ENGLISH -> "Generate, view, and iterate on images during Agent sessions"
        AppLanguage.ARABIC -> "إنشاء الصور وعرضها وتحسينها أثناء جلسات Agent"
        AppLanguage.PORTUGUESE -> "Gerar, visualizar e iterar imagens durante sessões do Agent"
        AppLanguage.SPANISH -> "Generar, ver e iterar imágenes durante sesiones de Agent"
        AppLanguage.FRENCH -> "Générer, visualiser et itérer des images pendant les sessions Agent"
        AppLanguage.GERMAN -> "Bilder während Agent-Sitzungen generieren, ansehen und iterieren"
        AppLanguage.RUSSIAN -> "Создание, просмотр и итерация изображений во время сессий Agent"
        AppLanguage.JAPANESE -> "Agentセッション中に画像を生成、表示、反復"
        AppLanguage.KOREAN -> "Agent 세션 중 이미지 생성, 보기 및 반복"
    }
    val featureIconGen: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "图标生成"
        AppLanguage.ENGLISH -> "Icon Generation"
        AppLanguage.ARABIC -> "إنشاء الأيقونات"
        AppLanguage.PORTUGUESE -> "Geração de Ícones"
        AppLanguage.SPANISH -> "Generación de Iconos"
        AppLanguage.FRENCH -> "Génération d'Icônes"
        AppLanguage.GERMAN -> "Symbolgenerierung"
        AppLanguage.RUSSIAN -> "Генерация иконок"
        AppLanguage.JAPANESE -> "アイコン生成"
        AppLanguage.KOREAN -> "아이콘 생성"
    }
    val featureIconGenDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "使用 AI 生成应用图标"
        AppLanguage.ENGLISH -> "Generate app icons using AI"
        AppLanguage.ARABIC -> "إنشاء أيقونات التطبيق باستخدام الذكاء الاصطناعي"
        AppLanguage.PORTUGUESE -> "Gerar ícones de app usando IA"
        AppLanguage.SPANISH -> "Generar iconos de app usando IA"
        AppLanguage.FRENCH -> "Générer des icônes d'application avec l'IA"
        AppLanguage.GERMAN -> "App-Symbole mit KI generieren"
        AppLanguage.RUSSIAN -> "Создавать иконки приложения с помощью ИИ"
        AppLanguage.JAPANESE -> "AIを使用してアプリアイコンを生成"
        AppLanguage.KOREAN -> "AI를 사용하여 앱 아이콘 생성"
    }
    val featureModuleDev: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "模块开发"
        AppLanguage.ENGLISH -> "Module Development"
        AppLanguage.ARABIC -> "تطوير الوحدات"
        AppLanguage.PORTUGUESE -> "Desenvolvimento de Módulos"
        AppLanguage.SPANISH -> "Desarrollo de Módulos"
        AppLanguage.FRENCH -> "Développement de Modules"
        AppLanguage.GERMAN -> "Modulentwicklung"
        AppLanguage.RUSSIAN -> "Разработка модулей"
        AppLanguage.JAPANESE -> "モジュール開発"
        AppLanguage.KOREAN -> "모듈 개발"
    }
    val featureModuleDevDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "AI Agent 辅助开发扩展模块"
        AppLanguage.ENGLISH -> "AI Agent-assisted extension module development"
        AppLanguage.ARABIC -> "تطوير وحدات الامتداد بمساعدة وكيل الذكاء الاصطناعي"
        AppLanguage.PORTUGUESE -> "Desenvolvimento de módulos de extensão assistido por Agente de IA"
        AppLanguage.SPANISH -> "Desarrollo de módulos de extensión asistido por Agente de IA"
        AppLanguage.FRENCH -> "Développement de modules d'extension assisté par Agent IA"
        AppLanguage.GERMAN -> "KI-Agent-gestützte Erweiterungsmodulentwicklung"
        AppLanguage.RUSSIAN -> "Разработка модулей расширения с помощью ИИ-агента"
        AppLanguage.JAPANESE -> "AIエージェント支援による拡張モジュール開発"
        AppLanguage.KOREAN -> "AI 에이전트 지원 확장 모듈 개발"
    }
    val featureLrcGen: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "歌词生成"
        AppLanguage.ENGLISH -> "LRC Generation"
        AppLanguage.ARABIC -> "إنشاء كلمات الأغاني"
        AppLanguage.PORTUGUESE -> "Geração de LRC"
        AppLanguage.SPANISH -> "Generación de LRC"
        AppLanguage.FRENCH -> "Génération LRC"
        AppLanguage.GERMAN -> "LRC-Generierung"
        AppLanguage.RUSSIAN -> "Генерация LRC"
        AppLanguage.JAPANESE -> "LRC生成"
        AppLanguage.KOREAN -> "LRC 생성"
    }
    val featureLrcGenDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "AI 生成 LRC 歌词文件"
        AppLanguage.ENGLISH -> "AI-generated LRC lyrics files"
        AppLanguage.ARABIC -> "ملفات كلمات LRC بالذكاء الاصطناعي"
        AppLanguage.PORTUGUESE -> "Arquivos de letras LRC gerados por IA"
        AppLanguage.SPANISH -> "Archivos de letras LRC generados por IA"
        AppLanguage.FRENCH -> "Fichiers de paroles LRC générés par IA"
        AppLanguage.GERMAN -> "KI-generierte LRC-Liedtextdateien"
        AppLanguage.RUSSIAN -> "LRC-файлы текстов песен, сгенерированные ИИ"
        AppLanguage.JAPANESE -> "AI生成LRC歌詞ファイル"
        AppLanguage.KOREAN -> "AI 생성 LRC 가사 파일"
    }
    val featureTranslate: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "翻译"
        AppLanguage.ENGLISH -> "Translation"
        AppLanguage.ARABIC -> "ترجمة"
        AppLanguage.PORTUGUESE -> "Tradução"
        AppLanguage.SPANISH -> "Traducción"
        AppLanguage.FRENCH -> "Traduction"
        AppLanguage.GERMAN -> "Übersetzung"
        AppLanguage.RUSSIAN -> "Перевод"
        AppLanguage.JAPANESE -> "翻訳"
        AppLanguage.KOREAN -> "번역"
    }
    val featureTranslateDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "网页内容翻译"
        AppLanguage.ENGLISH -> "Web content translation"
        AppLanguage.ARABIC -> "ترجمة محتوى الويب"
        AppLanguage.PORTUGUESE -> "Tradução de conteúdo web"
        AppLanguage.SPANISH -> "Traducción de contenido web"
        AppLanguage.FRENCH -> "Traduction de contenu web"
        AppLanguage.GERMAN -> "Webinhaltsübersetzung"
        AppLanguage.RUSSIAN -> "Перевод веб-содержимого"
        AppLanguage.JAPANESE -> "ウェブコンテンツ翻訳"
        AppLanguage.KOREAN -> "웹 콘텐츠 번역"
    }
    val featureGeneral: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "通用对话"
        AppLanguage.ENGLISH -> "General Chat"
        AppLanguage.ARABIC -> "محادثة عامة"
        AppLanguage.PORTUGUESE -> "Chat Geral"
        AppLanguage.SPANISH -> "Chat General"
        AppLanguage.FRENCH -> "Chat Général"
        AppLanguage.GERMAN -> "Allgemeiner Chat"
        AppLanguage.RUSSIAN -> "Общий чат"
        AppLanguage.JAPANESE -> "一般チャット"
        AppLanguage.KOREAN -> "일반 채팅"
    }
    val featureGeneralDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "通用 AI 对话功能"
        AppLanguage.ENGLISH -> "General AI chat functionality"
        AppLanguage.ARABIC -> "وظيفة محادثة الذكاء الاصطناعي العامة"
        AppLanguage.PORTUGUESE -> "Funcionalidade geral de chat com IA"
        AppLanguage.SPANISH -> "Funcionalidad general de chat con IA"
        AppLanguage.FRENCH -> "Fonctionnalité générale de chat IA"
        AppLanguage.GERMAN -> "Allgemeine KI-Chat-Funktionalität"
        AppLanguage.RUSSIAN -> "Общая функция ИИ-чата"
        AppLanguage.JAPANESE -> "一般的なAIチャット機能"
        AppLanguage.KOREAN -> "일반 AI 채팅 기능"
    }

    val capabilityText: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "文本生成"
        AppLanguage.ENGLISH -> "Text Generation"
        AppLanguage.ARABIC -> "إنشاء النص"
        AppLanguage.PORTUGUESE -> "Geração de Texto"
        AppLanguage.SPANISH -> "Generación de Texto"
        AppLanguage.FRENCH -> "Génération de Texte"
        AppLanguage.GERMAN -> "Textgenerierung"
        AppLanguage.RUSSIAN -> "Генерация текста"
        AppLanguage.JAPANESE -> "テキスト生成"
        AppLanguage.KOREAN -> "텍스트 생성"
    }
    val capabilityTextDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "基础文本对话和生成"
        AppLanguage.ENGLISH -> "Basic text dialogue and generation"
        AppLanguage.ARABIC -> "حوار وإنشاء النص الأساسي"
        AppLanguage.PORTUGUESE -> "Diálogo e geração de texto básicos"
        AppLanguage.SPANISH -> "Diálogo y generación de texto básicos"
        AppLanguage.FRENCH -> "Dialogue et génération de texte de base"
        AppLanguage.GERMAN -> "Grundlegende Textdialoge und -generierung"
        AppLanguage.RUSSIAN -> "Базовый текстовый диалог и генерация"
        AppLanguage.JAPANESE -> "基本的なテキスト対話と生成"
        AppLanguage.KOREAN -> "기본 텍스트 대화 및 생성"
    }
    val capabilityMultimodal: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "多模态"
        AppLanguage.ENGLISH -> "Multimodal"
        AppLanguage.ARABIC -> "متعدد الوسائط"
        AppLanguage.PORTUGUESE -> "Multimodal"
        AppLanguage.SPANISH -> "Multimodal"
        AppLanguage.FRENCH -> "Multimodal"
        AppLanguage.GERMAN -> "Multimodal"
        AppLanguage.RUSSIAN -> "Мультимодальный"
        AppLanguage.JAPANESE -> "マルチモーダル"
        AppLanguage.KOREAN -> "멀티모달"
    }
    val capabilityMultimodalDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "支持文本、图像、音频等多种输入"
        AppLanguage.ENGLISH -> "Supports text, image, audio and other input modalities"
        AppLanguage.ARABIC -> "يدعم النص والصور والصوت وغيرها من أنواع الإدخال"
        AppLanguage.PORTUGUESE -> "Suporta texto, imagem, áudio e outras modalidades de entrada"
        AppLanguage.SPANISH -> "Soporta texto, imagen, audio y otras modalidades de entrada"
        AppLanguage.FRENCH -> "Prend en charge texte, image, audio et autres modalités d'entrée"
        AppLanguage.GERMAN -> "Unterstützt Text, Bild, Audio und andere Eingabemodalitäten"
        AppLanguage.RUSSIAN -> "Поддерживает текст, изображения, аудио и другие виды ввода"
        AppLanguage.JAPANESE -> "テキスト、画像、オーディオなどその他の入力モダリティをサポート"
        AppLanguage.KOREAN -> "텍스트, 이미지, 오디오 등 다양한 입력 모달리티 지원"
    }
    val modelCategory: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "模型分类"
        AppLanguage.ENGLISH -> "Model Category"
        AppLanguage.ARABIC -> "فئة النموذج"
        AppLanguage.PORTUGUESE -> "Categoria de Modelo"
        AppLanguage.SPANISH -> "Categoría de Modelo"
        AppLanguage.FRENCH -> "Catégorie de Modèle"
        AppLanguage.GERMAN -> "Modellkategorie"
        AppLanguage.RUSSIAN -> "Категория модели"
        AppLanguage.JAPANESE -> "モデルカテゴリー"
        AppLanguage.KOREAN -> "모델 카테고리"
    }
    val multimodalModel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "多模态模型"
        AppLanguage.ENGLISH -> "Multimodal Model"
        AppLanguage.ARABIC -> "نموذج متعدد الوسائط"
        AppLanguage.PORTUGUESE -> "Modelo Multimodal"
        AppLanguage.SPANISH -> "Modelo Multimodal"
        AppLanguage.FRENCH -> "Modèle Multimodal"
        AppLanguage.GERMAN -> "Multimodales Modell"
        AppLanguage.RUSSIAN -> "Мультимодальная модель"
        AppLanguage.JAPANESE -> "マルチモーダルモデル"
        AppLanguage.KOREAN -> "멀티모달 모델"
    }
    val multimodalModelDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "支持文本、图像、音频等多种输入"
        AppLanguage.ENGLISH -> "Supports text, image, audio and other input modalities"
        AppLanguage.ARABIC -> "يدعم النص والصور والصوت وغيرها من أنواع الإدخال"
        AppLanguage.PORTUGUESE -> "Suporta texto, imagem, áudio e outras modalidades de entrada"
        AppLanguage.SPANISH -> "Soporta texto, imagen, audio y otras modalidades de entrada"
        AppLanguage.FRENCH -> "Prend en charge texte, image, audio et autres modalités d'entrée"
        AppLanguage.GERMAN -> "Unterstützt Text, Bild, Audio und andere Eingabemodalitäten"
        AppLanguage.RUSSIAN -> "Поддерживает текст, изображения, аудио и другие виды ввода"
        AppLanguage.JAPANESE -> "テキスト、画像、オーディオなどその他の入力モダリティをサポート"
        AppLanguage.KOREAN -> "텍스트, 이미지, 오디오 등 다양한 입력 모달리티 지원"
    }
    val capabilityImageGen: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "图像生成"
        AppLanguage.ENGLISH -> "Image Generation"
        AppLanguage.ARABIC -> "إنشاء الصور"
        AppLanguage.PORTUGUESE -> "Geração de Imagem"
        AppLanguage.SPANISH -> "Generación de Imágenes"
        AppLanguage.FRENCH -> "Génération d'Images"
        AppLanguage.GERMAN -> "Bildgenerierung"
        AppLanguage.RUSSIAN -> "Генерация изображений"
        AppLanguage.JAPANESE -> "画像生成"
        AppLanguage.KOREAN -> "이미지 생성"
    }
    val capabilityImageGenDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "生成图像和图标"
        AppLanguage.ENGLISH -> "Generate images and icons"
        AppLanguage.ARABIC -> "إنشاء الصور والأيقونات"
        AppLanguage.PORTUGUESE -> "Gerar imagens e ícones"
        AppLanguage.SPANISH -> "Generar imágenes e iconos"
        AppLanguage.FRENCH -> "Générer des images et des icônes"
        AppLanguage.GERMAN -> "Bilder und Symbole generieren"
        AppLanguage.RUSSIAN -> "Создавать изображения и иконки"
        AppLanguage.JAPANESE -> "画像とアイコンを生成"
        AppLanguage.KOREAN -> "이미지 및 아이콘 생성"
    }

    val allowSkip: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "允许点击跳过"
        AppLanguage.ENGLISH -> "Allow Skip"
        AppLanguage.ARABIC -> "السماح بالتخطي"
        AppLanguage.PORTUGUESE -> "Permitir Pular"
        AppLanguage.SPANISH -> "Permitir Saltar"
        AppLanguage.FRENCH -> "Autoriser Ignorer"
        AppLanguage.GERMAN -> "Überspringen erlauben"
        AppLanguage.RUSSIAN -> "Разрешить пропуск"
        AppLanguage.JAPANESE -> "スキップを許可"
        AppLanguage.KOREAN -> "건너뛰기 허용"
    }

    val splashShowCountdownLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "显示倒计时"
        AppLanguage.ENGLISH -> "Show countdown"
        AppLanguage.ARABIC -> "إظهار العد التنازلي"
        AppLanguage.PORTUGUESE -> "Mostrar contagem regressiva"
        AppLanguage.SPANISH -> "Mostrar cuenta atrás"
        AppLanguage.FRENCH -> "Afficher le compte à rebours"
        AppLanguage.GERMAN -> "Countdown anzeigen"
        AppLanguage.RUSSIAN -> "Показывать обратный отсчёт"
        AppLanguage.JAPANESE -> "カウントダウンを表示"
        AppLanguage.KOREAN -> "카운트다운 표시"
    }

    val splashShowCountdownHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "在启动画面右上角显示剩余秒数"
        AppLanguage.ENGLISH -> "Show remaining seconds in the top-right corner of the splash screen"
        AppLanguage.ARABIC -> "عرض الثواني المتبقية في الزاوية العلوية اليمنى لشاشة البداية"
        AppLanguage.PORTUGUESE -> "Mostrar segundos restantes no canto superior direito da tela inicial"
        AppLanguage.SPANISH -> "Mostrar los segundos restantes en la esquina superior derecha de la pantalla de inicio"
        AppLanguage.FRENCH -> "Afficher les secondes restantes en haut à droite de l'écran de démarrage"
        AppLanguage.GERMAN -> "Verbleibende Sekunden oben rechts auf dem Startbildschirm anzeigen"
        AppLanguage.RUSSIAN -> "Показывать оставшиеся секунды в правом верхнем углу заставки"
        AppLanguage.JAPANESE -> "スプラッシュ画面の右上に残り秒数を表示"
        AppLanguage.KOREAN -> "시작 화면 오른쪽 상단에 남은 초 표시"
    }

    val allowSkipHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "用户可点击屏幕跳过启动画面"
        AppLanguage.ENGLISH -> "User can tap screen to skip splash"
        AppLanguage.ARABIC -> "يمكن للمستخدم النقر على الشاشة لتخطي شاشة البداية"
        AppLanguage.PORTUGUESE -> "O usuário pode tocar na tela para pular a splash"
        AppLanguage.SPANISH -> "El usuario puede tocar la pantalla para saltar la splash"
        AppLanguage.FRENCH -> "L'utilisateur peut taper sur l'écran pour ignorer le splash"
        AppLanguage.GERMAN -> "Der Nutzer kann auf den Bildschirm tippen, um den Splash zu überspringen"
        AppLanguage.RUSSIAN -> "Пользователь может коснуться экрана, чтобы пропустить заставку"
        AppLanguage.JAPANESE -> "ユーザーは画面をタップしてスプラッシュをスキップできます"
        AppLanguage.KOREAN -> "사용자가 화면을 탭하여 스플래시를 건너뛸 수 있습니다"
    }

    val showTranslateButton: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "显示翻译按钮"
        AppLanguage.ENGLISH -> "Show Translate Button"
        AppLanguage.ARABIC -> "إظهار زر الترجمة"
        AppLanguage.PORTUGUESE -> "Mostrar Botão de Tradução"
        AppLanguage.SPANISH -> "Mostrar Botón de Traducción"
        AppLanguage.FRENCH -> "Afficher le Bouton de Traduction"
        AppLanguage.GERMAN -> "Übersetzungs-Button anzeigen"
        AppLanguage.RUSSIAN -> "Показывать кнопку перевода"
        AppLanguage.JAPANESE -> "翻訳ボタンを表示"
        AppLanguage.KOREAN -> "번역 버튼 표시"
    }

    val showTranslateButtonHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "在页面右下角显示可拖拽的翻译悬浮按钮"
        AppLanguage.ENGLISH -> "Show a draggable translate FAB at bottom right"
        AppLanguage.ARABIC -> "إظهار زر ترجمة عائم قابل للسحب في أسفل اليمين"
        AppLanguage.PORTUGUESE -> "Mostrar um FAB de tradução arrastável no canto inferior direito"
        AppLanguage.SPANISH -> "Mostrar un FAB de traducción arrastrable en la esquina inferior derecha"
        AppLanguage.FRENCH -> "Afficher un FAB de traduction déplaçable en bas à droite"
        AppLanguage.GERMAN -> "Einen ziehbaren Übersetzungs-FAB unten rechts anzeigen"
        AppLanguage.RUSSIAN -> "Показывать перетаскиваемый FAB перевода в правом нижнем углу"
        AppLanguage.JAPANESE -> "右下にドラッグ可能な翻訳FABを表示"
        AppLanguage.KOREAN -> "오른쪽 하단에 드래그 가능한 번역 FAB 표시"
    }

    val translateEngine: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "翻译引擎"
        AppLanguage.ENGLISH -> "Translation Engine"
        AppLanguage.ARABIC -> "محرك الترجمة"
        AppLanguage.PORTUGUESE -> "Motor de Tradução"
        AppLanguage.SPANISH -> "Motor de Traducción"
        AppLanguage.FRENCH -> "Moteur de Traduction"
        AppLanguage.GERMAN -> "Übersetzungs-Engine"
        AppLanguage.RUSSIAN -> "Движок перевода"
        AppLanguage.JAPANESE -> "翻訳エンジン"
        AppLanguage.KOREAN -> "번역 엔진"
    }

    val autoTranslateOnLoad: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "页面加载后自动翻译"
        AppLanguage.ENGLISH -> "Auto Translate on Load"
        AppLanguage.ARABIC -> "ترجمة تلقائية عند التحميل"
        AppLanguage.PORTUGUESE -> "Tradução Automática ao Carregar"
        AppLanguage.SPANISH -> "Traducción Automática al Cargar"
        AppLanguage.FRENCH -> "Traduction Automatique au Chargement"
        AppLanguage.GERMAN -> "Automatische Übersetzung beim Laden"
        AppLanguage.RUSSIAN -> "Автоперевод при загрузке"
        AppLanguage.JAPANESE -> "読み込み時に自動翻訳"
        AppLanguage.KOREAN -> "로드 시 자동 번역"
    }

    val autoTranslateOnLoadHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "页面加载完成后自动执行翻译，无需手动点击按钮"
        AppLanguage.ENGLISH -> "Automatically translate after page loads without manual click"
        AppLanguage.ARABIC -> "ترجمة تلقائية بعد تحميل الصفحة بدون نقر يدوي"
        AppLanguage.PORTUGUESE -> "Traduzir automaticamente após o carregamento da página sem clique manual"
        AppLanguage.SPANISH -> "Traducir automáticamente después de que la página se cargue sin clic manual"
        AppLanguage.FRENCH -> "Traduire automatiquement après le chargement de la page sans clic manuel"
        AppLanguage.GERMAN -> "Nach dem Laden der Seite automatisch übersetzen ohne manuellen Klick"
        AppLanguage.RUSSIAN -> "Автоматически переводить после загрузки страницы без ручного нажатия"
        AppLanguage.JAPANESE -> "ページ読み込み後に手動クリックなしで自動翻訳"
        AppLanguage.KOREAN -> "페이지 로드 후 수동 클릭 없이 자동으로 번역"
    }

    val displayCutoutModeLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "刘海 / 挖孔区域"
        AppLanguage.ENGLISH -> "Display cutout"
        AppLanguage.ARABIC -> "تجنب منطقة القطع في الشاشة"
        AppLanguage.PORTUGUESE -> "Área do entalhe da tela"
        AppLanguage.SPANISH -> "Área del notch de pantalla"
        AppLanguage.FRENCH -> "Zone d'encoche de l'écran"
        AppLanguage.GERMAN -> "Bildschirm-Ausschnitt"
        AppLanguage.RUSSIAN -> "Область выреза экрана"
        AppLanguage.JAPANESE -> "画面の切り欠き領域"
        AppLanguage.KOREAN -> "화면 컷아웃 영역"
    }

    val displayCutoutModeShortEdges: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "延伸到刘海区"
        AppLanguage.ENGLISH -> "Extend into cutout"
        AppLanguage.ARABIC -> "التمدد إلى منطقة القطع"
        AppLanguage.PORTUGUESE -> "Estender até o entalhe"
        AppLanguage.SPANISH -> "Extender hasta el notch"
        AppLanguage.FRENCH -> "Étendre dans l'encoche"
        AppLanguage.GERMAN -> "In den Ausschnitt erweitern"
        AppLanguage.RUSSIAN -> "Заходить в область выреза"
        AppLanguage.JAPANESE -> "切り欠き領域まで広げる"
        AppLanguage.KOREAN -> "컷아웃까지 확장"
    }

    val displayCutoutModeDefault: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "跟随系统默认"
        AppLanguage.ENGLISH -> "Use system default"
        AppLanguage.ARABIC -> "استخدام إعداد النظام الافتراضي"
        AppLanguage.PORTUGUESE -> "Usar padrão do sistema"
        AppLanguage.SPANISH -> "Usar valor predeterminado del sistema"
        AppLanguage.FRENCH -> "Valeur par défaut du système"
        AppLanguage.GERMAN -> "Systemstandard verwenden"
        AppLanguage.RUSSIAN -> "Как в системе по умолчанию"
        AppLanguage.JAPANESE -> "システムの既定に従う"
        AppLanguage.KOREAN -> "시스템 기본값 사용"
    }

    val displayCutoutModeNever: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "避开刘海区"
        AppLanguage.ENGLISH -> "Avoid cutout area"
        AppLanguage.ARABIC -> "تجنب منطقة القطع"
        AppLanguage.PORTUGUESE -> "Evitar área do entalhe"
        AppLanguage.SPANISH -> "Evitar área del notch"
        AppLanguage.FRENCH -> "Éviter la zone d'encoche"
        AppLanguage.GERMAN -> "Ausschnittbereich meiden"
        AppLanguage.RUSSIAN -> "Обходить область выреза"
        AppLanguage.JAPANESE -> "切り欠き領域を避ける"
        AppLanguage.KOREAN -> "컷아웃 영역 피하기"
    }

    val displayCutoutModeShortEdgesDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "画面铺满整屏，沉浸感最强"
        AppLanguage.ENGLISH -> "Fills the whole screen for full immersion"
        AppLanguage.ARABIC -> "يملأ الشاشة بالكامل لأقصى اندماج"
        AppLanguage.PORTUGUESE -> "Preenche toda a tela para imersão total"
        AppLanguage.SPANISH -> "Llena toda la pantalla para máxima inmersión"
        AppLanguage.FRENCH -> "Remplit tout l'écran pour une immersion totale"
        AppLanguage.GERMAN -> "Füllt den ganzen Bildschirm für volle Immersion"
        AppLanguage.RUSSIAN -> "Заполняет весь экран для полного погружения"
        AppLanguage.JAPANESE -> "画面全体に表示し没入感を最大化"
        AppLanguage.KOREAN -> "전체 화면을 채워 몰입감 극대화"
    }

    val displayCutoutModeDefaultDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "按系统默认规则处理刘海区"
        AppLanguage.ENGLISH -> "Let the system handle the cutout area"
        AppLanguage.ARABIC -> "دع النظام يتعامل مع منطقة القطع"
        AppLanguage.PORTUGUESE -> "Deixe o sistema tratar a área do entalhe"
        AppLanguage.SPANISH -> "Deja que el sistema gestione el notch"
        AppLanguage.FRENCH -> "Laisse le système gérer l'encoche"
        AppLanguage.GERMAN -> "System verwaltet den Ausschnittbereich"
        AppLanguage.RUSSIAN -> "Система сама обрабатывает область выреза"
        AppLanguage.JAPANESE -> "切り欠き領域はシステムの既定に従う"
        AppLanguage.KOREAN -> "컷아웃 영역은 시스템 기본값을 따름"
    }

    val displayCutoutModeNeverDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "避开刘海区，顶部按钮不被遮挡"
        AppLanguage.ENGLISH -> "Avoids the cutout so top buttons stay clear"
        AppLanguage.ARABIC -> "يتجنب منطقة القطع لتبقى الأزرار العلوية ظاهرة"
        AppLanguage.PORTUGUESE -> "Evita o entalhe para manter os botões superiores livres"
        AppLanguage.SPANISH -> "Evita el notch para que los botones superiores queden libres"
        AppLanguage.FRENCH -> "Évite l'encoche pour garder les boutons du haut dégagés"
        AppLanguage.GERMAN -> "Meidet den Ausschnitt, obere Schaltflächen bleiben frei"
        AppLanguage.RUSSIAN -> "Обходит вырез, верхние кнопки остаются открытыми"
        AppLanguage.JAPANESE -> "切り欠きを避け上部ボタンを隠さない"
        AppLanguage.KOREAN -> "컷아웃을 피해 상단 버튼을 가리지 않음"
    }

    val showStatusBar: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "状态栏常驻"
        AppLanguage.ENGLISH -> "Always show status bar"
        AppLanguage.ARABIC -> "إبقاء شريط الحالة ظاهرًا دائمًا"
        AppLanguage.PORTUGUESE -> "Manter barra de status sempre visível"
        AppLanguage.SPANISH -> "Mantener siempre visible la barra de estado"
        AppLanguage.FRENCH -> "Toujours afficher la barre d'état"
        AppLanguage.GERMAN -> "Statusleiste immer anzeigen"
        AppLanguage.RUSSIAN -> "Всегда показывать строку состояния"
        AppLanguage.JAPANESE -> "ステータスバーを常に表示"
        AppLanguage.KOREAN -> "상태 표시줄 항상 표시"
    }

    val showStatusBarHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "全屏时状态栏始终显示，不会自动隐藏"
        AppLanguage.ENGLISH -> "Status bar stays visible in fullscreen and never auto-hides"
        AppLanguage.ARABIC -> "يبقى شريط الحالة ظاهرًا في ملء الشاشة ولا يختفي تلقائيًا"
        AppLanguage.PORTUGUESE -> "A barra de status permanece visível em tela cheia e nunca se oculta sozinha"
        AppLanguage.SPANISH -> "La barra de estado permanece visible en pantalla completa y nunca se oculta sola"
        AppLanguage.FRENCH -> "La barre d'état reste visible en plein écran et ne se masque jamais toute seule"
        AppLanguage.GERMAN -> "Die Statusleiste bleibt im Vollbildmodus sichtbar und blendet sich nie automatisch aus"
        AppLanguage.RUSSIAN -> "Строка состояния остаётся видимой в полноэкранном режиме и никогда не скрывается сама"
        AppLanguage.JAPANESE -> "全画面でもステータスバーは表示されたままになり、自動では隠れません"
        AppLanguage.KOREAN -> "전체 화면에서도 상태 표시줄이 계속 표시되며 자동으로 숨겨지지 않습니다"
    }

    val showNavigationBar: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "显示导航栏"
        AppLanguage.ENGLISH -> "Show Navigation Bar"
        AppLanguage.ARABIC -> "إظهار شريط التنقل"
        AppLanguage.PORTUGUESE -> "Mostrar Barra de Navegação"
        AppLanguage.SPANISH -> "Mostrar Barra de Navegación"
        AppLanguage.FRENCH -> "Afficher la Barre de Navigation"
        AppLanguage.GERMAN -> "Navigationsleiste anzeigen"
        AppLanguage.RUSSIAN -> "Показывать панель навигации"
        AppLanguage.JAPANESE -> "ナビゲーションバーを表示"
        AppLanguage.KOREAN -> "내비게이션 바 표시"
    }

    val showNavigationBarHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "全屏模式下仍显示底部导航栏（返回、主页、最近任务）"
        AppLanguage.ENGLISH -> "Keep bottom navigation bar visible in fullscreen mode (Back, Home, Recents)"
        AppLanguage.ARABIC -> "إبقاء شريط التنقل السفلي مرئيًا في وضع ملء الشاشة (رجوع، الرئيسية، الأخيرة)"
        AppLanguage.PORTUGUESE -> "Manter a barra de navegação inferior visível no modo tela cheia (Voltar, Início, Recentes)"
        AppLanguage.SPANISH -> "Mantener la barra de navegación inferior visible en modo pantalla completa (Atrás, Inicio, Recientes)"
        AppLanguage.FRENCH -> "Garder la barre de navigation inférieure visible en mode plein écran (Retour, Accueil, Récents)"
        AppLanguage.GERMAN -> "Untere Navigationsleiste im Vollbildmodus sichtbar lassen (Zurück, Start, Letzte)"
        AppLanguage.RUSSIAN -> "Держать нижнюю панель навигации видимой в полноэкранном режиме (Назад, Главная, Недавние)"
        AppLanguage.JAPANESE -> "全画面モードで下部ナビゲーションバーを表示し続ける（戻る、ホーム、最近）"
        AppLanguage.KOREAN -> "전체 화면 모드에서 하단 내비게이션 바를 계속 표시（뒤로, 홈, 최근）"
    }

    val fullscreenContentPadding: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "内容内边距"
        AppLanguage.ENGLISH -> "Content Padding"
        AppLanguage.ARABIC -> "هامش المحتوى"
        AppLanguage.PORTUGUESE -> "Preenchimento de Conteúdo"
        AppLanguage.SPANISH -> "Relleno de Contenido"
        AppLanguage.FRENCH -> "Marge de Contenu"
        AppLanguage.GERMAN -> "Innenabstand des Inhalts"
        AppLanguage.RUSSIAN -> "Внутренний отступ контента"
        AppLanguage.JAPANESE -> "コンテンツの余白"
        AppLanguage.KOREAN -> "콘텐츠 안쪽 여백"
    }

    val fullscreenContentPaddingHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "全屏时给内容留边，角落按钮更好点，也能缓解返回手势冲突"
        AppLanguage.ENGLISH -> "Pad content in fullscreen: easier corner taps, fewer edge-gesture conflicts"
        AppLanguage.ARABIC -> "هامش حول المحتوى في ملء الشاشة: نقر أسهل على أزرار الزوايا وتعاوض أقل مع إيماءات الحافة"
        AppLanguage.PORTUGUESE -> "Margem no conteúdo em tela cheia: cantos mais fáceis de tocar, menos conflito com gestos de borda"
        AppLanguage.SPANISH -> "Margen en pantalla completa: esquinas más fáciles y menos conflicto con gestos de borde"
        AppLanguage.FRENCH -> "Marge en plein écran : boutons d'angle plus faciles, moins de conflits de gestes"
        AppLanguage.GERMAN -> "Abstand im Vollbild: leichtere Eckbuttons, weniger Kantengesten-Konflikte"
        AppLanguage.RUSSIAN -> "Отступ в полноэкранном режиме: кнопки в углах удобнее, меньше конфликтов с жестами"
        AppLanguage.JAPANESE -> "全画面で余白を設け、隅のボタンを押しやすくし、エッジジェスチャーとの競合を軽減"
        AppLanguage.KOREAN -> "전체 화면 여백: 모서리 버튼이 쉬워지고 가장자리 제스처 충돌 감소"
    }

    val fullscreenPaddingPerSide: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "分别设置四边"
        AppLanguage.ENGLISH -> "Set each side separately"
        AppLanguage.ARABIC -> "ضبط كل جانب على حدة"
        AppLanguage.PORTUGUESE -> "Definir cada lado separadamente"
        AppLanguage.SPANISH -> "Ajustar cada lado por separado"
        AppLanguage.FRENCH -> "Régler chaque côté séparément"
        AppLanguage.GERMAN -> "Jede Seite einzeln einstellen"
        AppLanguage.RUSSIAN -> "Задать каждую сторону отдельно"
        AppLanguage.JAPANESE -> "辺ごとに個別設定"
        AppLanguage.KOREAN -> "변별로 개별 설정"
    }

    val paddingSideTop: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "上"
        AppLanguage.ENGLISH -> "Top"
        AppLanguage.ARABIC -> "أعلى"
        AppLanguage.PORTUGUESE -> "Superior"
        AppLanguage.SPANISH -> "Superior"
        AppLanguage.FRENCH -> "Haut"
        AppLanguage.GERMAN -> "Oben"
        AppLanguage.RUSSIAN -> "Сверху"
        AppLanguage.JAPANESE -> "上"
        AppLanguage.KOREAN -> "위"
    }

    val paddingSideBottom: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下"
        AppLanguage.ENGLISH -> "Bottom"
        AppLanguage.ARABIC -> "أسفل"
        AppLanguage.PORTUGUESE -> "Inferior"
        AppLanguage.SPANISH -> "Inferior"
        AppLanguage.FRENCH -> "Bas"
        AppLanguage.GERMAN -> "Unten"
        AppLanguage.RUSSIAN -> "Снизу"
        AppLanguage.JAPANESE -> "下"
        AppLanguage.KOREAN -> "아래"
    }

    val paddingSideLeft: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "左"
        AppLanguage.ENGLISH -> "Left"
        AppLanguage.ARABIC -> "يسار"
        AppLanguage.PORTUGUESE -> "Esquerda"
        AppLanguage.SPANISH -> "Izquierda"
        AppLanguage.FRENCH -> "Gauche"
        AppLanguage.GERMAN -> "Links"
        AppLanguage.RUSSIAN -> "Слева"
        AppLanguage.JAPANESE -> "左"
        AppLanguage.KOREAN -> "왼쪽"
    }

    val paddingSideRight: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "右"
        AppLanguage.ENGLISH -> "Right"
        AppLanguage.ARABIC -> "يمين"
        AppLanguage.PORTUGUESE -> "Direita"
        AppLanguage.SPANISH -> "Derecha"
        AppLanguage.FRENCH -> "Droite"
        AppLanguage.GERMAN -> "Rechts"
        AppLanguage.RUSSIAN -> "Справа"
        AppLanguage.JAPANESE -> "右"
        AppLanguage.KOREAN -> "오른쪽"
    }

    val statusBarCustomizeLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "状态栏自定义"
        AppLanguage.ENGLISH -> "Customize status bar"
        AppLanguage.ARABIC -> "تخصيص شريط الحالة"
        AppLanguage.PORTUGUESE -> "Personalizar barra de status"
        AppLanguage.SPANISH -> "Personalizar barra de estado"
        AppLanguage.FRENCH -> "Personnaliser la barre d'état"
        AppLanguage.GERMAN -> "Statusleiste anpassen"
        AppLanguage.RUSSIAN -> "Настроить строку состояния"
        AppLanguage.JAPANESE -> "ステータスバーをカスタマイズ"
        AppLanguage.KOREAN -> "상태 표시줄 사용자 설정"
    }

    val statusBarLightModeLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "浅色模式"
        AppLanguage.ENGLISH -> "Light Mode"
        AppLanguage.ARABIC -> "الوضع الفاتح"
        AppLanguage.PORTUGUESE -> "Modo Claro"
        AppLanguage.SPANISH -> "Modo Claro"
        AppLanguage.FRENCH -> "Mode Clair"
        AppLanguage.GERMAN -> "Heller Modus"
        AppLanguage.RUSSIAN -> "Светлый режим"
        AppLanguage.JAPANESE -> "ライトモード"
        AppLanguage.KOREAN -> "라이트 모드"
    }

    val statusBarDarkModeLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "深色模式"
        AppLanguage.ENGLISH -> "Dark Mode"
        AppLanguage.ARABIC -> "الوضع الداكن"
        AppLanguage.PORTUGUESE -> "Modo Escuro"
        AppLanguage.SPANISH -> "Modo Oscuro"
        AppLanguage.FRENCH -> "Mode Sombre"
        AppLanguage.GERMAN -> "Dunkler Modus"
        AppLanguage.RUSSIAN -> "Тёмный режим"
        AppLanguage.JAPANESE -> "ダークモード"
        AppLanguage.KOREAN -> "다크 모드"
    }

    val statusBarIconsLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "状态栏图标"
        AppLanguage.ENGLISH -> "Status bar icons"
        AppLanguage.ARABIC -> "أيقونات شريط الحالة"
        AppLanguage.PORTUGUESE -> "Ícones da barra de status"
        AppLanguage.SPANISH -> "Iconos de la barra de estado"
        AppLanguage.FRENCH -> "Icônes de la barre d'état"
        AppLanguage.GERMAN -> "Symbole der Statusleiste"
        AppLanguage.RUSSIAN -> "Значки строки состояния"
        AppLanguage.JAPANESE -> "ステータスバーのアイコン"
        AppLanguage.KOREAN -> "상태 표시줄 아이콘"
    }

    val statusBarIconsAuto: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自动"
        AppLanguage.ENGLISH -> "Auto"
        AppLanguage.ARABIC -> "تلقائي"
        AppLanguage.PORTUGUESE -> "Automático"
        AppLanguage.SPANISH -> "Automático"
        AppLanguage.FRENCH -> "Auto"
        AppLanguage.GERMAN -> "Auto"
        AppLanguage.RUSSIAN -> "Авто"
        AppLanguage.JAPANESE -> "自動"
        AppLanguage.KOREAN -> "자동"
    }

    val statusBarIconsDark: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "深色"
        AppLanguage.ENGLISH -> "Dark"
        AppLanguage.ARABIC -> "داكن"
        AppLanguage.PORTUGUESE -> "Escuros"
        AppLanguage.SPANISH -> "Oscuros"
        AppLanguage.FRENCH -> "Sombres"
        AppLanguage.GERMAN -> "Dunkel"
        AppLanguage.RUSSIAN -> "Тёмные"
        AppLanguage.JAPANESE -> "ダーク"
        AppLanguage.KOREAN -> "어둡게"
    }

    val statusBarIconsLight: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "浅色"
        AppLanguage.ENGLISH -> "Light"
        AppLanguage.ARABIC -> "فاتح"
        AppLanguage.PORTUGUESE -> "Claros"
        AppLanguage.SPANISH -> "Claros"
        AppLanguage.FRENCH -> "Claires"
        AppLanguage.GERMAN -> "Hell"
        AppLanguage.RUSSIAN -> "Светлые"
        AppLanguage.JAPANESE -> "ライト"
        AppLanguage.KOREAN -> "밝게"
    }

    val browserToolbarLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "工具栏"
        AppLanguage.ENGLISH -> "Toolbar"
        AppLanguage.ARABIC -> "شريط الأدوات"
        AppLanguage.PORTUGUESE -> "Barra de Ferramentas"
        AppLanguage.SPANISH -> "Barra de Herramientas"
        AppLanguage.FRENCH -> "Barre d'Outils"
        AppLanguage.GERMAN -> "Symbolleiste"
        AppLanguage.RUSSIAN -> "Панель инструментов"
        AppLanguage.JAPANESE -> "ツールバー"
        AppLanguage.KOREAN -> "도구 모음"
    }

    val toolbarShowTitleLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "显示标题"
        AppLanguage.ENGLISH -> "Show Title"
        AppLanguage.ARABIC -> "إظهار العنوان"
        AppLanguage.PORTUGUESE -> "Mostrar Título"
        AppLanguage.SPANISH -> "Mostrar Título"
        AppLanguage.FRENCH -> "Afficher le Titre"
        AppLanguage.GERMAN -> "Titel anzeigen"
        AppLanguage.RUSSIAN -> "Показывать заголовок"
        AppLanguage.JAPANESE -> "タイトルを表示"
        AppLanguage.KOREAN -> "제목 표시"
    }

    val toolbarShowTitleHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "在工具栏显示页面标题或应用名称"
        AppLanguage.ENGLISH -> "Show the page title or app name in the toolbar"
        AppLanguage.ARABIC -> "إظهار عنوان الصفحة أو اسم التطبيق في شريط الأدوات"
        AppLanguage.PORTUGUESE -> "Mostrar o título da página ou nome do app na barra de ferramentas"
        AppLanguage.SPANISH -> "Mostrar el título de la página o nombre de la app en la barra de herramientas"
        AppLanguage.FRENCH -> "Afficher le titre de la page ou le nom de l'application dans la barre d'outils"
        AppLanguage.GERMAN -> "Seitentitel oder App-Namen in der Symbolleiste anzeigen"
        AppLanguage.RUSSIAN -> "Показывать заголовок страницы или имя приложения на панели инструментов"
        AppLanguage.JAPANESE -> "ツールバーにページタイトルまたはアプリ名を表示"
        AppLanguage.KOREAN -> "도구 모음에 페이지 제목 또는 앱 이름 표시"
    }

    val toolbarShowUrlLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "显示网址"
        AppLanguage.ENGLISH -> "Show URL"
        AppLanguage.ARABIC -> "إظهار العنوان URL"
        AppLanguage.PORTUGUESE -> "Mostrar URL"
        AppLanguage.SPANISH -> "Mostrar URL"
        AppLanguage.FRENCH -> "Afficher l'URL"
        AppLanguage.GERMAN -> "URL anzeigen"
        AppLanguage.RUSSIAN -> "Показывать URL"
        AppLanguage.JAPANESE -> "URLを表示"
        AppLanguage.KOREAN -> "URL 표시"
    }

    val toolbarShowUrlHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "在工具栏标题下方显示当前网址"
        AppLanguage.ENGLISH -> "Show the current URL below the toolbar title"
        AppLanguage.ARABIC -> "إظهار العنوان URL الحالي أسفل عنوان شريط الأدوات"
        AppLanguage.PORTUGUESE -> "Mostrar o URL atual abaixo do título da barra de ferramentas"
        AppLanguage.SPANISH -> "Mostrar el URL actual debajo del título de la barra de herramientas"
        AppLanguage.FRENCH -> "Afficher l'URL actuelle sous le titre de la barre d'outils"
        AppLanguage.GERMAN -> "Aktuelle URL unter dem Symbolleistentitel anzeigen"
        AppLanguage.RUSSIAN -> "Показывать текущий URL под заголовком панели инструментов"
        AppLanguage.JAPANESE -> "ツールバータイトルの下に現在のURLを表示"
        AppLanguage.KOREAN -> "도구 모음 제목 아래에 현재 URL 표시"
    }

    val toolbarShowBackLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "显示后退按钮"
        AppLanguage.ENGLISH -> "Show Back Button"
        AppLanguage.ARABIC -> "إظهار زر الرجوع"
        AppLanguage.PORTUGUESE -> "Mostrar Botão Voltar"
        AppLanguage.SPANISH -> "Mostrar Botón Atrás"
        AppLanguage.FRENCH -> "Afficher le Bouton Retour"
        AppLanguage.GERMAN -> "Zurück-Button anzeigen"
        AppLanguage.RUSSIAN -> "Показывать кнопку «Назад»"
        AppLanguage.JAPANESE -> "戻るボタンを表示"
        AppLanguage.KOREAN -> "뒤로 버튼 표시"
    }

    val toolbarShowForwardLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "显示前进按钮"
        AppLanguage.ENGLISH -> "Show Forward Button"
        AppLanguage.ARABIC -> "إظهار زر التقدم"
        AppLanguage.PORTUGUESE -> "Mostrar Botão Avançar"
        AppLanguage.SPANISH -> "Mostrar Botón Adelante"
        AppLanguage.FRENCH -> "Afficher le Bouton Suivant"
        AppLanguage.GERMAN -> "Vorwärts-Button anzeigen"
        AppLanguage.RUSSIAN -> "Показывать кнопку «Вперёд»"
        AppLanguage.JAPANESE -> "進むボタンを表示"
        AppLanguage.KOREAN -> "앞으로 버튼 표시"
    }

    val toolbarShowRefreshLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "显示刷新按钮"
        AppLanguage.ENGLISH -> "Show Refresh Button"
        AppLanguage.ARABIC -> "إظهار زر التحديث"
        AppLanguage.PORTUGUESE -> "Mostrar Botão Atualizar"
        AppLanguage.SPANISH -> "Mostrar Botón Actualizar"
        AppLanguage.FRENCH -> "Afficher le Bouton Actualiser"
        AppLanguage.GERMAN -> "Aktualisieren-Button anzeigen"
        AppLanguage.RUSSIAN -> "Показывать кнопку «Обновить»"
        AppLanguage.JAPANESE -> "更新ボタンを表示"
        AppLanguage.KOREAN -> "새로고침 버튼 표시"
    }

    val toolbarShowConsoleLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "显示控制台按钮"
        AppLanguage.ENGLISH -> "Show Console Button"
        AppLanguage.ARABIC -> "إظهار زر وحدة التحكم"
        AppLanguage.PORTUGUESE -> "Mostrar Botão do Console"
        AppLanguage.SPANISH -> "Mostrar Botón de Consola"
        AppLanguage.FRENCH -> "Afficher le Bouton de la Console"
        AppLanguage.GERMAN -> "Konsolen-Button anzeigen"
        AppLanguage.RUSSIAN -> "Показывать кнопку консоли"
        AppLanguage.JAPANESE -> "コンソールボタンを表示"
        AppLanguage.KOREAN -> "콘솔 버튼 표시"
    }

    val toolbarShowFindLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "显示页内查找按钮"
        AppLanguage.ENGLISH -> "Show Find in Page Button"
        AppLanguage.ARABIC -> "إظهار زر البحث في الصفحة"
        AppLanguage.PORTUGUESE -> "Mostrar Botão de Localizar na Página"
        AppLanguage.SPANISH -> "Mostrar Botón de Buscar en Página"
        AppLanguage.FRENCH -> "Afficher le Bouton Rechercher dans la Page"
        AppLanguage.GERMAN -> "Seiten-Such-Button anzeigen"
        AppLanguage.RUSSIAN -> "Показывать кнопку поиска по странице"
        AppLanguage.JAPANESE -> "ページ内検索ボタンを表示"
        AppLanguage.KOREAN -> "페이지 내 찾기 버튼 표시"
    }

    val clickToSelectImageOrVideo: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "点击下方按钮选择图片或视频"
        AppLanguage.ENGLISH -> "Click button below to select image or video"
        AppLanguage.ARABIC -> "انقر على الزر أدناه لاختيار صورة أو فيديو"
        AppLanguage.PORTUGUESE -> "Clique no botão abaixo para selecionar imagem ou vídeo"
        AppLanguage.SPANISH -> "Haga clic en el botón de abajo para seleccionar imagen o video"
        AppLanguage.FRENCH -> "Cliquez sur le bouton ci-dessous pour sélectionner une image ou une vidéo"
        AppLanguage.GERMAN -> "Klicken Sie auf die Schaltfläche unten, um Bild oder Video auszuwählen"
        AppLanguage.RUSSIAN -> "Нажмите кнопку ниже, чтобы выбрать изображение или видео"
        AppLanguage.JAPANESE -> "下のボタンをクリックして画像または動画を選択"
        AppLanguage.KOREAN -> "아래 버튼을 클릭하여 이미지 또는 동영상 선택"
    }

    val displayDuration: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "显示时长"
        AppLanguage.ENGLISH -> "Display Duration"
        AppLanguage.ARABIC -> "مدة العرض"
        AppLanguage.PORTUGUESE -> "Duração de Exibição"
        AppLanguage.SPANISH -> "Duración de Visualización"
        AppLanguage.FRENCH -> "Durée d'Affichage"
        AppLanguage.GERMAN -> "Anzeigedauer"
        AppLanguage.RUSSIAN -> "Длительность показа"
        AppLanguage.JAPANESE -> "表示時間"
        AppLanguage.KOREAN -> "표시 시간"
    }

    val displayDurationSeconds: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "显示时长：%d 秒"
        AppLanguage.ENGLISH -> "Display duration: %d seconds"
        AppLanguage.ARABIC -> "مدة العرض: %d ثانية"
        AppLanguage.PORTUGUESE -> "Duração de exibição: %d segundos"
        AppLanguage.SPANISH -> "Duración de visualización: %d segundos"
        AppLanguage.FRENCH -> "Durée d'affichage : %d secondes"
        AppLanguage.GERMAN -> "Anzeigedauer: %d Sekunden"
        AppLanguage.RUSSIAN -> "Длительность показа: %d сек."
        AppLanguage.JAPANESE -> "表示時間：%d 秒"
        AppLanguage.KOREAN -> "표시 시간: %d초"
    }

    val autoTranslateHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "页面加载完成后自动翻译为指定语言，支持多引擎自动降级（Google / MyMemory / LibreTranslate / Lingva）"
        AppLanguage.ENGLISH -> "Auto translate to specified language after page loads with multi-engine fallback (Google / MyMemory / LibreTranslate / Lingva)"
        AppLanguage.ARABIC -> "ترجمة تلقائية إلى اللغة المحددة بعد تحميل الصفحة مع دعم محركات متعددة (Google / MyMemory / LibreTranslate / Lingva)"
        AppLanguage.PORTUGUESE -> "Traduzir automaticamente para o idioma especificado após o carregamento da página com fallback de vários motores (Google / MyMemory / LibreTranslate / Lingva)"
        AppLanguage.SPANISH -> "Traducir automáticamente al idioma especificado después de que la página se cargue con fallback de múltiples motores (Google / MyMemory / LibreTranslate / Lingva)"
        AppLanguage.FRENCH -> "Traduire automatiquement vers la langue spécifiée après le chargement de la page avec repli multi-moteurs (Google / MyMemory / LibreTranslate / Lingva)"
        AppLanguage.GERMAN -> "Automatische Übersetzung in die angegebene Sprache nach dem Laden der Seite mit Multi-Engine-Fallback (Google / MyMemory / LibreTranslate / Lingva)"
        AppLanguage.RUSSIAN -> "Автоматический перевод на указанный язык после загрузки страницы с многоядерным резервом (Google / MyMemory / LibreTranslate / Lingva)"
        AppLanguage.JAPANESE -> "ページ読み込み後に複数エンジンのフォールバックで指定言語に自動翻訳（Google / MyMemory / LibreTranslate / Lingva）"
        AppLanguage.KOREAN -> "페이지 로드 후 다중 엔진 폴백으로 지정된 언어에 자동 번역（Google / MyMemory / LibreTranslate / Lingva）"
    }

    val videoCrop: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "视频裁剪"
        AppLanguage.ENGLISH -> "Video Crop"
        AppLanguage.ARABIC -> "قص الفيديو"
        AppLanguage.PORTUGUESE -> "Recortar Vídeo"
        AppLanguage.SPANISH -> "Recortar Video"
        AppLanguage.FRENCH -> "Rogner la Vidéo"
        AppLanguage.GERMAN -> "Video zuschneiden"
        AppLanguage.RUSSIAN -> "Обрезка видео"
        AppLanguage.JAPANESE -> "動画のクロップ"
        AppLanguage.KOREAN -> "동영상 자르기"
    }

    val splashPreview: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "启动画面预览"
        AppLanguage.ENGLISH -> "Splash Screen Preview"
        AppLanguage.ARABIC -> "معاينة شاشة البداية"
        AppLanguage.PORTUGUESE -> "Pré-visualização da Tela Inicial"
        AppLanguage.SPANISH -> "Vista Previa de Pantalla de Inicio"
        AppLanguage.FRENCH -> "Aperçu de l'Écran de Démarrage"
        AppLanguage.GERMAN -> "Startbildschirm-Vorschau"
        AppLanguage.RUSSIAN -> "Предпросмотр заставки"
        AppLanguage.JAPANESE -> "スプラッシュ画面のプレビュー"
        AppLanguage.KOREAN -> "시작 화면 미리보기"
    }

    val landscapeDisplay: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "横屏显示"
        AppLanguage.ENGLISH -> "Landscape Display"
        AppLanguage.ARABIC -> "عرض أفقي"
        AppLanguage.PORTUGUESE -> "Exibição em Paisagem"
        AppLanguage.SPANISH -> "Visualización en Paisaje"
        AppLanguage.FRENCH -> "Affichage en Paysage"
        AppLanguage.GERMAN -> "Querformat-Anzeige"
        AppLanguage.RUSSIAN -> "Альбомная ориентация"
        AppLanguage.JAPANESE -> "横向き表示"
        AppLanguage.KOREAN -> "가로 모드 표시"
    }

    val landscapeDisplayHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "启动画面以横屏方式展示"
        AppLanguage.ENGLISH -> "Display splash screen in landscape orientation"
        AppLanguage.ARABIC -> "عرض شاشة البداية بالاتجاه الأفقي"
        AppLanguage.PORTUGUESE -> "Exibir tela inicial em orientação paisagem"
        AppLanguage.SPANISH -> "Mostrar pantalla de inicio en orientación paisaje"
        AppLanguage.FRENCH -> "Afficher l'écran de démarrage en orientation paysage"
        AppLanguage.GERMAN -> "Startbildschirm im Querformat anzeigen"
        AppLanguage.RUSSIAN -> "Показывать заставку в альбомной ориентации"
        AppLanguage.JAPANESE -> "スプラッシュ画面を横向きで表示"
        AppLanguage.KOREAN -> "시작 화면을 가로 모드로 표시"
    }

    val autoStartSettings: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自启动设置"
        AppLanguage.ENGLISH -> "Auto Start Settings"
        AppLanguage.ARABIC -> "إعدادات التشغيل التلقائي"
        AppLanguage.PORTUGUESE -> "Configurações de Início Automático"
        AppLanguage.SPANISH -> "Configuración de Inicio Automático"
        AppLanguage.FRENCH -> "Paramètres de Démarrage Automatique"
        AppLanguage.GERMAN -> "Autostart-Einstellungen"
        AppLanguage.RUSSIAN -> "Настройки автозапуска"
        AppLanguage.JAPANESE -> "自動起動設定"
        AppLanguage.KOREAN -> "자동 시작 설정"
    }

    val configured: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已配置"
        AppLanguage.ENGLISH -> "Configured"
        AppLanguage.ARABIC -> "تم التكوين"
        AppLanguage.PORTUGUESE -> "Configurado"
        AppLanguage.SPANISH -> "Configurado"
        AppLanguage.FRENCH -> "Configuré"
        AppLanguage.GERMAN -> "Konfiguriert"
        AppLanguage.RUSSIAN -> "Настроено"
        AppLanguage.JAPANESE -> "設定済み"
        AppLanguage.KOREAN -> "구성됨"
    }

    val common: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "常用"
        AppLanguage.ENGLISH -> "Common"
        AppLanguage.ARABIC -> "شائع"
        AppLanguage.PORTUGUESE -> "Comum"
        AppLanguage.SPANISH -> "Común"
        AppLanguage.FRENCH -> "Courant"
        AppLanguage.GERMAN -> "Allgemein"
        AppLanguage.RUSSIAN -> "Общие"
        AppLanguage.JAPANESE -> "共通"
        AppLanguage.KOREAN -> "공통"
    }

    val lab: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Lab"
        AppLanguage.ENGLISH -> "Lab"
        AppLanguage.ARABIC -> "مختبر"
        AppLanguage.PORTUGUESE -> "Lab"
        AppLanguage.SPANISH -> "Lab"
        AppLanguage.FRENCH -> "Lab"
        AppLanguage.GERMAN -> "Lab"
        AppLanguage.RUSSIAN -> "Lab"
        AppLanguage.JAPANESE -> "Lab"
        AppLanguage.KOREAN -> "Lab"
    }

    val capabilityBasicInfo: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "基础信息"
        AppLanguage.ENGLISH -> "Basic Info"
        AppLanguage.ARABIC -> "المعلومات الأساسية"
        AppLanguage.PORTUGUESE -> "Informações Básicas"
        AppLanguage.SPANISH -> "Información Básica"
        AppLanguage.FRENCH -> "Informations de Base"
        AppLanguage.GERMAN -> "Grundinformationen"
        AppLanguage.RUSSIAN -> "Основная информация"
        AppLanguage.JAPANESE -> "基本情報"
        AppLanguage.KOREAN -> "기본 정보"
    }

    val capabilityBasicInfoHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "名称、URL、图标和应用类型"
        AppLanguage.ENGLISH -> "Name, URL, icon, and app type"
        AppLanguage.ARABIC -> "الاسم والرابط والأيقونة ونوع التطبيق"
        AppLanguage.PORTUGUESE -> "Nome, URL, ícone e tipo de app"
        AppLanguage.SPANISH -> "Nombre, URL, icono y tipo de app"
        AppLanguage.FRENCH -> "Nom, URL, icône et type d'application"
        AppLanguage.GERMAN -> "Name, URL, Symbol und App-Typ"
        AppLanguage.RUSSIAN -> "Имя, URL, значок и тип приложения"
        AppLanguage.JAPANESE -> "名前、URL、アイコン、アプリタイプ"
        AppLanguage.KOREAN -> "이름, URL, 아이콘, 앱 유형"
    }

    val capabilityAdBlock: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "广告拦截"
        AppLanguage.ENGLISH -> "Ad Block"
        AppLanguage.ARABIC -> "حظر الإعلانات"
        AppLanguage.PORTUGUESE -> "Bloqueio de Anúncios"
        AppLanguage.SPANISH -> "Bloqueo de Anuncios"
        AppLanguage.FRENCH -> "Blocage des Publicités"
        AppLanguage.GERMAN -> "Werbeblockierung"
        AppLanguage.RUSSIAN -> "Блокировка рекламы"
        AppLanguage.JAPANESE -> "広告ブロック"
        AppLanguage.KOREAN -> "광고 차단"
    }

    val capabilityAdBlockHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "拦截规则和运行时开关"
        AppLanguage.ENGLISH -> "Blocking rules and runtime toggles"
        AppLanguage.ARABIC -> "قواعد الحظر ومفاتيح التشغيل أثناء运行"
        AppLanguage.PORTUGUESE -> "Regras de bloqueio e alternadores de tempo de execução"
        AppLanguage.SPANISH -> "Reglas de bloqueo y interruptores en tiempo de ejecución"
        AppLanguage.FRENCH -> "Règles de blocage et bascules d'exécution"
        AppLanguage.GERMAN -> "Blockierungsregeln und Laufzeit-Umschalter"
        AppLanguage.RUSSIAN -> "Правила блокировки и переключатели времени выполнения"
        AppLanguage.JAPANESE -> "ブロックルールとランタイム切り替え"
        AppLanguage.KOREAN -> "차단 규칙 및 런타임 토글"
    }

    val capabilityBrowserBehavior: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "浏览器行为"
        AppLanguage.ENGLISH -> "Browser Behavior"
        AppLanguage.ARABIC -> "سلوك المتصفح"
        AppLanguage.PORTUGUESE -> "Comportamento do Navegador"
        AppLanguage.SPANISH -> "Comportamiento del Navegador"
        AppLanguage.FRENCH -> "Comportement du Navigateur"
        AppLanguage.GERMAN -> "Browser-Verhalten"
        AppLanguage.RUSSIAN -> "Поведение браузера"
        AppLanguage.JAPANESE -> "ブラウザの動作"
        AppLanguage.KOREAN -> "브라우저 동작"
    }

    val capabilityBrowserBehaviorHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "工具栏、全屏、横屏、长按菜单、UA"
        AppLanguage.ENGLISH -> "Toolbar, fullscreen, landscape, long-press menu, UA"
        AppLanguage.ARABIC -> "شريط الأدوات وملء الشاشة والوضع الأفقي وقائمة الضغط المطول ووكيل المستخدم"
        AppLanguage.PORTUGUESE -> "Barra de ferramentas, tela cheia, paisagem, menu de toque longo, UA"
        AppLanguage.SPANISH -> "Barra de herramientas, pantalla completa, paisaje, menú de pulsación larga, UA"
        AppLanguage.FRENCH -> "Barre d'outils, plein écran, paysage, menu de pression longue, UA"
        AppLanguage.GERMAN -> "Symbolleiste, Vollbild, Querformat, Long-Press-Menü, UA"
        AppLanguage.RUSSIAN -> "Панель инструментов, полный экран, альбомный, меню долгого нажатия, UA"
        AppLanguage.JAPANESE -> "ツールバー、全画面、横向き、長押しメニュー、UA"
        AppLanguage.KOREAN -> "도구 모음, 전체 화면, 가로 모드, 길게 누름 메뉴, UA"
    }

    val capabilitySplash: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "启动页"
        AppLanguage.ENGLISH -> "Splash"
        AppLanguage.ARABIC -> "شاشة البداية"
        AppLanguage.PORTUGUESE -> "Tela Inicial"
        AppLanguage.SPANISH -> "Pantalla de Inicio"
        AppLanguage.FRENCH -> "Écran de Démarrage"
        AppLanguage.GERMAN -> "Startbildschirm"
        AppLanguage.RUSSIAN -> "Заставка"
        AppLanguage.JAPANESE -> "スプラッシュ"
        AppLanguage.KOREAN -> "시작 화면"
    }

    val capabilitySplashHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "启动图片、视频、时长和跳过行为"
        AppLanguage.ENGLISH -> "Launch image, video, duration, and skip behavior"
        AppLanguage.ARABIC -> "صورة وفيديو البدء والمدة وطريقة التخطي"
        AppLanguage.PORTUGUESE -> "Imagem de inicialização, vídeo, duração e comportamento de skip"
        AppLanguage.SPANISH -> "Imagen de inicio, video, duración y comportamiento de omisión"
        AppLanguage.FRENCH -> "Image de lancement, vidéo, durée et comportement d'omission"
        AppLanguage.GERMAN -> "Startbild, Video, Dauer und Überspringen-Verhalten"
        AppLanguage.RUSSIAN -> "Изображение запуска, видео, длительность и пропуск"
        AppLanguage.JAPANESE -> "起動画像、動画、再生時間、スキップ動作"
        AppLanguage.KOREAN -> "시작 이미지, 동영상, 재생 시간, 건너뛰기 동작"
    }

    val capabilityAnnouncement: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "弹窗公告"
        AppLanguage.ENGLISH -> "Announcements"
        AppLanguage.ARABIC -> "إعلانات منبثقة"
        AppLanguage.PORTUGUESE -> "Anúncios"
        AppLanguage.SPANISH -> "Anuncios"
        AppLanguage.FRENCH -> "Annonces"
        AppLanguage.GERMAN -> "Ankündigungen"
        AppLanguage.RUSSIAN -> "Объявления"
        AppLanguage.JAPANESE -> "お知らせ"
        AppLanguage.KOREAN -> "공지"
    }

    val capabilityAnnouncementHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "启动后的提示弹窗、公告内容和展示频率"
        AppLanguage.ENGLISH -> "Launch prompts, announcement content, and display frequency"
        AppLanguage.ARABIC -> "رسائل البدء ومحتوى الإعلانات وتكرار العرض"
        AppLanguage.PORTUGUESE -> "Prompts de inicialização, conteúdo de anúncios e frequência de exibição"
        AppLanguage.SPANISH -> "Mensajes de inicio, contenido de anuncios y frecuencia de visualización"
        AppLanguage.FRENCH -> "Invites de lancement, contenu d'annonces et fréquence d'affichage"
        AppLanguage.GERMAN -> "Start-Prompts, Ankündigungsinhalte und Anzeigehäufigkeit"
        AppLanguage.RUSSIAN -> "Подсказки запуска, содержимое объявлений и частота показа"
        AppLanguage.JAPANESE -> "起動プロンプト、お知らせ内容、表示頻度"
        AppLanguage.KOREAN -> "시작 프롬프트, 공지 내용, 표시 빈도"
    }

    val capabilityRuntimeControl: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "运行控制"
        AppLanguage.ENGLISH -> "Runtime Control"
        AppLanguage.ARABIC -> "التحكم أثناء التشغيل"
        AppLanguage.PORTUGUESE -> "Controle de Tempo de Execução"
        AppLanguage.SPANISH -> "Control en Tiempo de Ejecución"
        AppLanguage.FRENCH -> "Contrôle d'Exécution"
        AppLanguage.GERMAN -> "Laufzeitsteuerung"
        AppLanguage.RUSSIAN -> "Управление временем выполнения"
        AppLanguage.JAPANESE -> "ランタイム制御"
        AppLanguage.KOREAN -> "런타임 제어"
    }

    val capabilityRuntimeControlHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "常亮、亮度、悬浮窗、自启动"
        AppLanguage.ENGLISH -> "Keep-awake, brightness, floating window, auto start"
        AppLanguage.ARABIC -> "إبقاء الشاشة مضاءة والسطوع والنافذة العائمة والتشغيل التلقائي"
        AppLanguage.PORTUGUESE -> "Manter acordado, brilho, janela flutuante, início automático"
        AppLanguage.SPANISH -> "Mantener despierto, brillo, ventana flotante, inicio automático"
        AppLanguage.FRENCH -> "Garder éveillé, luminosité, fenêtre flottante, démarrage automatique"
        AppLanguage.GERMAN -> "Wach halten, Helligkeit, schwebendes Fenster, Autostart"
        AppLanguage.RUSSIAN -> "Не засыпать, яркость, плавающее окно, автозапуск"
        AppLanguage.JAPANESE -> "スリープ防止、明るさ、フローティングウィンドウ、自動起動"
        AppLanguage.KOREAN -> "절전 방지, 밝기, 플로팅 창, 자동 시작"
    }

    val capabilityActivation: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "激活码"
        AppLanguage.ENGLISH -> "Activation"
        AppLanguage.ARABIC -> "رمز التفعيل"
        AppLanguage.PORTUGUESE -> "Ativação"
        AppLanguage.SPANISH -> "Activación"
        AppLanguage.FRENCH -> "Code d'activation"
        AppLanguage.GERMAN -> "Aktivierung"
        AppLanguage.RUSSIAN -> "Активация"
        AppLanguage.JAPANESE -> "アクティベーション"
        AppLanguage.KOREAN -> "활성화"
    }

    val capabilityActivationHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "访问验证、授权码和验证弹窗"
        AppLanguage.ENGLISH -> "Access validation, license codes, and verification dialogs"
        AppLanguage.ARABIC -> "التحقق من الوصول ورموز الترخيص ونوافذ التحقق"
        AppLanguage.PORTUGUESE -> "Validação de acesso, códigos de licença e diálogos de verificação"
        AppLanguage.SPANISH -> "Validación de acceso, códigos de licencia y diálogos de verificación"
        AppLanguage.FRENCH -> "Validation d'accès, codes de licence et dialogues de vérification"
        AppLanguage.GERMAN -> "Zugriffsvalidierung, Lizenzcodes und Verifizierungsdialoge"
        AppLanguage.RUSSIAN -> "Проверка доступа, лицензионные коды и диалоги подтверждения"
        AppLanguage.JAPANESE -> "アクセス検証、ライセンスコード、確認ダイアログ"
        AppLanguage.KOREAN -> "접근 검증, 라이선스 코드, 확인 대화상자"
    }

    val capabilityPermissions: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "权限配置"
        AppLanguage.ENGLISH -> "Permissions"
        AppLanguage.ARABIC -> "إعدادات الأذونات"
        AppLanguage.PORTUGUESE -> "Permissões"
        AppLanguage.SPANISH -> "Permisos"
        AppLanguage.FRENCH -> "Autorisations"
        AppLanguage.GERMAN -> "Berechtigungen"
        AppLanguage.RUSSIAN -> "Разрешения"
        AppLanguage.JAPANESE -> "権限"
        AppLanguage.KOREAN -> "권한"
    }

    val capabilityPermissionsHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "运行时权限声明和权限方案"
        AppLanguage.ENGLISH -> "Runtime permissions and permission presets"
        AppLanguage.ARABIC -> "أذونات وقت التشغيل وخطط الأذونات"
        AppLanguage.PORTUGUESE -> "Permissões de tempo de execução e predefinições de permissão"
        AppLanguage.SPANISH -> "Permisos en tiempo de ejecución y preajustes de permisos"
        AppLanguage.FRENCH -> "Permissions d'exécution et préréglages de permissions"
        AppLanguage.GERMAN -> "Laufzeitberechtigungen und Berechtigungsvoreinstellungen"
        AppLanguage.RUSSIAN -> "Разрешения времени выполнения и предустановки разрешений"
        AppLanguage.JAPANESE -> "ランタイム権限と権限プリセット"
        AppLanguage.KOREAN -> "런타임 권한 및 권한 사전 설정"
    }

    val capabilityNetworkTrust: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "网络信任 / CA"
        AppLanguage.ENGLISH -> "Network Trust / CA"
        AppLanguage.ARABIC -> "ثقة الشبكة / CA"
        AppLanguage.PORTUGUESE -> "Confiança de Rede / CA"
        AppLanguage.SPANISH -> "Confianza de Red / CA"
        AppLanguage.FRENCH -> "Confiance Réseau / CA"
        AppLanguage.GERMAN -> "Netzwerkvertrauen / CA"
        AppLanguage.RUSSIAN -> "Доверие сети / CA"
        AppLanguage.JAPANESE -> "ネットワーク信頼 / CA"
        AppLanguage.KOREAN -> "네트워크 신뢰 / CA"
    }

    val capabilityNetworkTrustHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "用户 CA、自定义证书、明文流量"
        AppLanguage.ENGLISH -> "User CA, custom certificates, and cleartext traffic"
        AppLanguage.ARABIC -> "شهادات CA للمستخدم والشهادات المخصصة وحركة المرور النصية"
        AppLanguage.PORTUGUESE -> "CA do usuário, certificados personalizados e tráfego de texto claro"
        AppLanguage.SPANISH -> "CA de usuario, certificados personalizados y tráfico de texto claro"
        AppLanguage.FRENCH -> "CA utilisateur, certificats personnalisés et trafic en clair"
        AppLanguage.GERMAN -> "Benutzer-CA, benutzerdefinierte Zertifikate und Klartextverkehr"
        AppLanguage.RUSSIAN -> "Пользовательский CA, пользовательские сертификаты и открытый трафик"
        AppLanguage.JAPANESE -> "ユーザーCA、カスタム証明書、クリアテキストトラフィック"
        AppLanguage.KOREAN -> "사용자 CA, 사용자 지정 인증서, 평문 트래픽"
    }

    val capabilityApkExport: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "APK 导出"
        AppLanguage.ENGLISH -> "APK Export"
        AppLanguage.ARABIC -> "تصدير APK"
        AppLanguage.PORTUGUESE -> "Exportação de APK"
        AppLanguage.SPANISH -> "Exportación de APK"
        AppLanguage.FRENCH -> "Exportation d'APK"
        AppLanguage.GERMAN -> "APK-Export"
        AppLanguage.RUSSIAN -> "Экспорт APK"
        AppLanguage.JAPANESE -> "APKエクスポート"
        AppLanguage.KOREAN -> "APK 내보내기"
    }

    val capabilityApkExportHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "包名、版本、架构、签名和构建优化"
        AppLanguage.ENGLISH -> "Package name, version, ABI, signing, and build optimization"
        AppLanguage.ARABIC -> "اسم الحزمة والإصدار والبنية والتوقيع وتحسينات البناء"
        AppLanguage.PORTUGUESE -> "Nome do pacote, versão, ABI, assinatura e otimização de build"
        AppLanguage.SPANISH -> "Nombre del paquete, versión, ABI, firma y optimización de build"
        AppLanguage.FRENCH -> "Nom du paquet, version, ABI, signature et optimisation de build"
        AppLanguage.GERMAN -> "Paketname, Version, ABI, Signierung und Build-Optimierung"
        AppLanguage.RUSSIAN -> "Имя пакета, версия, ABI, подпись и оптимизация сборки"
        AppLanguage.JAPANESE -> "パッケージ名、バージョン、ABI、署名、ビルド最適化"
        AppLanguage.KOREAN -> "패키지 이름, 버전, ABI, 서명, 빌드 최적화"
    }

    val capabilityLab: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Lab 能力"
        AppLanguage.ENGLISH -> "Lab Features"
        AppLanguage.ARABIC -> "ميزات المختبر"
        AppLanguage.PORTUGUESE -> "Recursos Lab"
        AppLanguage.SPANISH -> "Funciones Lab"
        AppLanguage.FRENCH -> "Fonctionnalités Lab"
        AppLanguage.GERMAN -> "Lab-Funktionen"
        AppLanguage.RUSSIAN -> "Функции Lab"
        AppLanguage.JAPANESE -> "Lab機能"
        AppLanguage.KOREAN -> "Lab 기능"
    }

    val capabilityLabHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "伪装和设备特征"
        AppLanguage.ENGLISH -> "Disguise, advanced controls, and device traits"
        AppLanguage.ARABIC -> "التخفي والميزات المتقدمة وخصائص الجهاز"
        AppLanguage.PORTUGUESE -> "Disfarce, controles avançados e características do dispositivo"
        AppLanguage.SPANISH -> "Disfraz, controles avanzados y rasgos del dispositivo"
        AppLanguage.FRENCH -> "Déguisement, contrôles avancés et caractéristiques de l'appareil"
        AppLanguage.GERMAN -> "Tarnung, erweiterte Steuerelemente und Gerätemerkmale"
        AppLanguage.RUSSIAN -> "Маскировка, расширенные элементы управления и характеристики устройства"
        AppLanguage.JAPANESE -> "偽装、高度なコントロール、デバイス特性"
        AppLanguage.KOREAN -> "위장, 고급 컨트롤, 장치 특성"
    }

    val capabilityExtension: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "扩展模块"
        AppLanguage.ENGLISH -> "Extensions"
        AppLanguage.ARABIC -> "الوحدات الإضافية"
        AppLanguage.PORTUGUESE -> "Extensões"
        AppLanguage.SPANISH -> "Extensiones"
        AppLanguage.FRENCH -> "Modules d'extension"
        AppLanguage.GERMAN -> "Erweiterungen"
        AppLanguage.RUSSIAN -> "Расширения"
        AppLanguage.JAPANESE -> "拡張機能"
        AppLanguage.KOREAN -> "확장 프로그램"
    }

    val capabilityExtensionHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "脚本、模块和浏览器增强能力"
        AppLanguage.ENGLISH -> "Scripts, modules, and browser enhancements"
        AppLanguage.ARABIC -> "البرامج النصية والوحدات وتحسينات المتصفح"
        AppLanguage.PORTUGUESE -> "Scripts, módulos e aprimoramentos do navegador"
        AppLanguage.SPANISH -> "Scripts, módulos y mejoras del navegador"
        AppLanguage.FRENCH -> "Scripts, modules et améliorations du navigateur"
        AppLanguage.GERMAN -> "Skripte, Module und Browser-Erweiterungen"
        AppLanguage.RUSSIAN -> "Скрипты, модули и улучшения браузера"
        AppLanguage.JAPANESE -> "スクリプト、モジュール、ブラウザ拡張"
        AppLanguage.KOREAN -> "스크립트, 모듈, 브라우저 향상"
    }

    val capabilityAppearanceMedia: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "外观与媒体"
        AppLanguage.ENGLISH -> "Appearance & Media"
        AppLanguage.ARABIC -> "المظهر والوسائط"
        AppLanguage.PORTUGUESE -> "Aparência e Mídia"
        AppLanguage.SPANISH -> "Apariencia y Medios"
        AppLanguage.FRENCH -> "Apparence et Médias"
        AppLanguage.GERMAN -> "Erscheinungsbild und Medien"
        AppLanguage.RUSSIAN -> "Внешний вид и медиа"
        AppLanguage.JAPANESE -> "外観とメディア"
        AppLanguage.KOREAN -> "외관 및 미디어"
    }

    val capabilityAppearanceMediaHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "背景音乐、翻译按钮和视觉配置"
        AppLanguage.ENGLISH -> "BGM, translate button, and visual settings"
        AppLanguage.ARABIC -> "الموسيقى الخلفية وزر الترجمة والإعدادات المرئية"
        AppLanguage.PORTUGUESE -> "BGM, botão de tradução e configurações visuais"
        AppLanguage.SPANISH -> "BGM, botón de traducción y configuraciones visuales"
        AppLanguage.FRENCH -> "BGM, bouton de traduction et paramètres visuels"
        AppLanguage.GERMAN -> "BGM, Übersetzen-Button und visuelle Einstellungen"
        AppLanguage.RUSSIAN -> "BGM, кнопка перевода и визуальные настройки"
        AppLanguage.JAPANESE -> "BGM、翻訳ボタン、視覚設定"
        AppLanguage.KOREAN -> "BGM, 번역 버튼, 시각 설정"
    }

    val networkTrustTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "网络信任"
        AppLanguage.ENGLISH -> "Network Trust"
        AppLanguage.ARABIC -> "ثقة الشبكة"
        AppLanguage.PORTUGUESE -> "Confiança de Rede"
        AppLanguage.SPANISH -> "Confianza de Red"
        AppLanguage.FRENCH -> "Confiance Réseau"
        AppLanguage.GERMAN -> "Netzwerkvertrauen"
        AppLanguage.RUSSIAN -> "Доверие сети"
        AppLanguage.JAPANESE -> "ネットワーク信頼"
        AppLanguage.KOREAN -> "네트워크 신뢰"
    }

    val trustSystemCa: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "信任系统 CA"
        AppLanguage.ENGLISH -> "Trust System CA"
        AppLanguage.ARABIC -> "الثقة في CA النظام"
        AppLanguage.PORTUGUESE -> "Confiar em CA do Sistema"
        AppLanguage.SPANISH -> "Confiar en CA del Sistema"
        AppLanguage.FRENCH -> "Faire confiance aux CA Système"
        AppLanguage.GERMAN -> "System-CA vertrauen"
        AppLanguage.RUSSIAN -> "Доверять системному CA"
        AppLanguage.JAPANESE -> "システムCAを信頼"
        AppLanguage.KOREAN -> "시스템 CA 신뢰"
    }

    val trustSystemCaHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "保留 Android 系统内置证书信任链"
        AppLanguage.ENGLISH -> "Keep Android system certificate trust chain"
        AppLanguage.ARABIC -> "الاحتفاظ بسلسلة الثقة لشهادات نظام Android"
        AppLanguage.PORTUGUESE -> "Manter a cadeia de confiança de certificados do sistema Android"
        AppLanguage.SPANISH -> "Mantener la cadena de confianza de certificados del sistema Android"
        AppLanguage.FRENCH -> "Conserver la chaîne de confiance des certificats système Android"
        AppLanguage.GERMAN -> "Vertrauenskette der Android-Systemzertifikate beibehalten"
        AppLanguage.RUSSIAN -> "Сохранять цепочку доверия системных сертификатов Android"
        AppLanguage.JAPANESE -> "Androidシステム証明書信頼チェーンを維持"
        AppLanguage.KOREAN -> "Android 시스템 인증서 신뢰 체인 유지"
    }

    val trustUserCa: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "信任用户安装 CA"
        AppLanguage.ENGLISH -> "Trust User-installed CA"
        AppLanguage.ARABIC -> "الثقة في CA المثبتة من المستخدم"
        AppLanguage.PORTUGUESE -> "Confiar em CA Instalada pelo Usuário"
        AppLanguage.SPANISH -> "Confiar en CA Instalada por el Usuario"
        AppLanguage.FRENCH -> "Faire confiance aux CA Installées par l'Utilisateur"
        AppLanguage.GERMAN -> "Benutzerinstallierten CA vertrauen"
        AppLanguage.RUSSIAN -> "Доверять CA, установленным пользователем"
        AppLanguage.JAPANESE -> "ユーザーインストールCAを信頼"
        AppLanguage.KOREAN -> "사용자 설치 CA 신뢰"
    }

    val trustUserCaHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "适用于已在手机系统中安装内网或开发 CA 的环境"
        AppLanguage.ENGLISH -> "For environments with intranet or development CA installed in system settings"
        AppLanguage.ARABIC -> "مناسب للبيئات التي ثُبِّتت فيها شهادات CA داخلية أو تطويرية في إعدادات النظام"
        AppLanguage.PORTUGUESE -> "Para ambientes com CA de intranet ou desenvolvimento instalada nas configurações do sistema"
        AppLanguage.SPANISH -> "Para entornos con CA de intranet o desarrollo instalada en la configuración del sistema"
        AppLanguage.FRENCH -> "Pour les environnements avec CA intranet ou de développement installés dans les paramètres système"
        AppLanguage.GERMAN -> "Für Umgebungen mit Intranet- oder Entwicklungs-CA in den Systemeinstellungen"
        AppLanguage.RUSSIAN -> "Для сред с CA интрасети или разработки, установленными в настройках системы"
        AppLanguage.JAPANESE -> "システム設定にインストールされたイントラネットまたは開発CA環境用"
        AppLanguage.KOREAN -> "시스템 설정에 설치된 인트라넷 또는 개발 CA 환경용"
    }

    val clientCertificateAuthTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "客户端证书认证 (mTLS)"
        AppLanguage.ENGLISH -> "Client Certificate Authentication (mTLS)"
        AppLanguage.ARABIC -> "مصادقة شهادة العميل (mTLS)"
        AppLanguage.PORTUGUESE -> "Autenticação por certificado do cliente (mTLS)"
        AppLanguage.SPANISH -> "Autenticación con certificado de cliente (mTLS)"
        AppLanguage.FRENCH -> "Authentification par certificat client (mTLS)"
        AppLanguage.GERMAN -> "Client-Zertifikatsauthentifizierung (mTLS)"
        AppLanguage.RUSSIAN -> "Аутентификация по клиентскому сертификату (mTLS)"
        AppLanguage.JAPANESE -> "クライアント証明書認証 (mTLS)"
        AppLanguage.KOREAN -> "클라이언트 인증서 인증 (mTLS)"
    }

    val clientCertificateAuthDescription: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "服务器请求客户端证书时，打开 Android 系统证书选择器并使用设备上安装的身份凭据。这不同于信任服务器 CA。选定的身份会在后续连接同一服务器时自动复用。"
        AppLanguage.ENGLISH -> "When a server requests a client certificate, open Android's system certificate picker and use the selected device-installed identity. This is different from trusting a server CA. The selected identity is reused for later connections to the same server."
        AppLanguage.ARABIC -> "عندما يطلب الخادم شهادة عميل، يفتح منتقي شهادات Android ويستخدم هوية مثبتة على الجهاز. يختلف هذا عن الوثوق بشهادة CA للخادم. تُعاد استخدام الهوية المختارة للاتصالات اللاحقة بنفس الخادم."
        AppLanguage.PORTUGUESE -> "Quando um servidor solicita um certificado de cliente, abre o seletor do Android e usa uma identidade instalada no dispositivo. Isso é diferente de confiar na CA do servidor. A identidade selecionada é reutilizada para conexões posteriores ao mesmo servidor."
        AppLanguage.SPANISH -> "Cuando un servidor solicita un certificado de cliente, abre el selector de Android y usa una identidad instalada en el dispositivo. Esto es distinto de confiar en la CA del servidor. La identidad seleccionada se reutiliza para conexiones posteriores al mismo servidor."
        AppLanguage.FRENCH -> "Lorsqu'un serveur demande un certificat client, ouvre le sélecteur Android et utilise une identité installée sur l'appareil. Cela diffère de la confiance accordée à l'AC du serveur. L'identité sélectionnée est réutilisée pour les connexions ultérieures au même serveur."
        AppLanguage.GERMAN -> "Wenn ein Server ein Client-Zertifikat anfordert, wird die Android-Auswahl geöffnet und eine auf dem Gerät installierte Identität verwendet. Dies unterscheidet sich vom Vertrauen in die Server-CA. Die gewählte Identität wird für spätere Verbindungen zum selben Server wiederverwendet."
        AppLanguage.RUSSIAN -> "Когда сервер запрашивает клиентский сертификат, открывает системный выбор сертификата Android и использует выбранное удостоверение, установленное на устройстве. Это не то же самое, что доверие сертификату сервера. Выбранное удостоверение используется повторно для последующих подключений к тому же серверу."
        AppLanguage.JAPANESE -> "サーバーがクライアント証明書を要求したとき、Android の証明書選択画面を開き、端末にインストール済みのIDを使用します。サーバーCAの信頼とは異なります。選択したIDは、同じサーバーへの以降の接続で再利用されます。"
        AppLanguage.KOREAN -> "서버가 클라이언트 인증서를 요청하면 Android 인증서 선택기를 열고 기기에 설치된 ID를 사용합니다. 서버 CA 신뢰와는 다릅니다. 선택한 ID는 같은 서버에 대한 이후 연결에 재사용됩니다."
    }

    val cleartextTrafficAllowed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "允许明文流量"
        AppLanguage.ENGLISH -> "Allow Cleartext Traffic"
        AppLanguage.ARABIC -> "السماح بحركة مرور غير مشفرة"
        AppLanguage.PORTUGUESE -> "Permitir Tráfego de Texto Claro"
        AppLanguage.SPANISH -> "Permitir Tráfico de Texto Claro"
        AppLanguage.FRENCH -> "Autoriser le Trafic en Clair"
        AppLanguage.GERMAN -> "Klartextverkehr zulassen"
        AppLanguage.RUSSIAN -> "Разрешить открытый трафик"
        AppLanguage.JAPANESE -> "クリアテキストトラフィックを許可"
        AppLanguage.KOREAN -> "평문 트래픽 허용"
    }

    val cleartextTrafficAllowedHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "保留 localhost、内网调试与旧站点兼容性"
        AppLanguage.ENGLISH -> "Keep compatibility for localhost, intranet debugging, and legacy sites"
        AppLanguage.ARABIC -> "الحفاظ على التوافق مع localhost وتصحيح الشبكات الداخلية والمواقع القديمة"
        AppLanguage.PORTUGUESE -> "Manter compatibilidade para localhost, depuração de intranet e sites legados"
        AppLanguage.SPANISH -> "Mantener compatibilidad para localhost, depuración de intranet y sitios heredados"
        AppLanguage.FRENCH -> "Conserver la compatibilité pour localhost, le débogage intranet et les sites hérités"
        AppLanguage.GERMAN -> "Kompatibilität für localhost, Intranet-Debugging und Legacy-Sites beibehalten"
        AppLanguage.RUSSIAN -> "Сохранять совместимость с localhost, отладкой интрасети и устаревшими сайтами"
        AppLanguage.JAPANESE -> "localhost、イントラネットデバッグ、レガシーサイトの互換性を維持"
        AppLanguage.KOREAN -> "localhost, 인트라넷 디버깅, 레거시 사이트 호환성 유지"
    }

    val importCustomCa: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "导入自定义 CA"
        AppLanguage.ENGLISH -> "Import Custom CA"
        AppLanguage.ARABIC -> "استيراد CA مخصصة"
        AppLanguage.PORTUGUESE -> "Importar CA Personalizada"
        AppLanguage.SPANISH -> "Importar CA Personalizada"
        AppLanguage.FRENCH -> "Importer un CA Personnalisé"
        AppLanguage.GERMAN -> "Benutzerdefinierte CA importieren"
        AppLanguage.RUSSIAN -> "Импортировать пользовательский CA"
        AppLanguage.JAPANESE -> "カスタムCAをインポート"
        AppLanguage.KOREAN -> "사용자 지정 CA 가져오기"
    }

    val importCustomCaHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "支持 PEM、CRT、CER 格式的 X.509 证书"
        AppLanguage.ENGLISH -> "Supports X.509 certificates in PEM, CRT, and CER formats"
        AppLanguage.ARABIC -> "يدعم شهادات X.509 بصيغ PEM وCRT وCER"
        AppLanguage.PORTUGUESE -> "Suporta certificados X.509 nos formatos PEM, CRT e CER"
        AppLanguage.SPANISH -> "Soporta certificados X.509 en formatos PEM, CRT y CER"
        AppLanguage.FRENCH -> "Prend en charge les certificats X.509 aux formats PEM, CRT et CER"
        AppLanguage.GERMAN -> "Unterstützt X.509-Zertifikate in PEM-, CRT- und CER-Formaten"
        AppLanguage.RUSSIAN -> "Поддерживает сертификаты X.509 в форматах PEM, CRT и CER"
        AppLanguage.JAPANESE -> "PEM、CRT、CER形式のX.509証明書をサポート"
        AppLanguage.KOREAN -> "PEM, CRT, CER 형식의 X.509 인증서 지원"
    }

    val importedCertificatesCount: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已导入 %d 个证书"
        AppLanguage.ENGLISH -> "%d certificates imported"
        AppLanguage.ARABIC -> "تم استيراد %d شهادات"
        AppLanguage.PORTUGUESE -> "%d certificados importados"
        AppLanguage.SPANISH -> "%d certificados importados"
        AppLanguage.FRENCH -> "%d certificats importés"
        AppLanguage.GERMAN -> "%d Zertifikate importiert"
        AppLanguage.RUSSIAN -> "%d сертификатов импортировано"
        AppLanguage.JAPANESE -> "%d 個の証明書をインポート済み"
        AppLanguage.KOREAN -> "%d개 인증서 가져옴"
    }

    val saveNetworkPreset: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "保存为网络方案"
        AppLanguage.ENGLISH -> "Save as Network Preset"
        AppLanguage.ARABIC -> "حفظ كإعداد شبكة"
        AppLanguage.PORTUGUESE -> "Salvar como Predefinição de Rede"
        AppLanguage.SPANISH -> "Guardar como Preajuste de Red"
        AppLanguage.FRENCH -> "Enregistrer comme Préréglage Réseau"
        AppLanguage.GERMAN -> "Als Netzwerkvoreinstellung speichern"
        AppLanguage.RUSSIAN -> "Сохранить как предустановку сети"
        AppLanguage.JAPANESE -> "ネットワークプリセットとして保存"
        AppLanguage.KOREAN -> "네트워크 사전 설정으로 저장"
    }

    val saveNetworkPresetHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "将当前 CA、用户证书和明文流量策略保存为可复用方案"
        AppLanguage.ENGLISH -> "Save current CA, user certificate, and cleartext strategy as a reusable preset"
        AppLanguage.ARABIC -> "حفظ إعدادات CA الحالية وشهادات المستخدم وسياسة النص الصريح كإعداد قابل لإعادة الاستخدام"
        AppLanguage.PORTUGUESE -> "Salvar CA atual, certificado de usuário e estratégia de texto claro como uma predefinição reutilizável"
        AppLanguage.SPANISH -> "Guardar CA actual, certificado de usuario y estrategia de texto claro como un preajuste reutilizable"
        AppLanguage.FRENCH -> "Enregistrer le CA actuel, le certificat utilisateur et la stratégie en clair comme préréglage réutilisable"
        AppLanguage.GERMAN -> "Aktuelle CA, Benutzerzertifikat und Klartextstrategie als wiederverwendbare Voreinstellung speichern"
        AppLanguage.RUSSIAN -> "Сохранить текущий CA, пользовательский сертификат и стратегию открытого трафика как предустановку"
        AppLanguage.JAPANESE -> "現在のCA、ユーザー証明書、クリアテキスト戦略を再利用可能なプリセットとして保存"
        AppLanguage.KOREAN -> "현재 CA, 사용자 인증서, 평문 전략을 재사용 가능한 사전 설정으로 저장"
    }

    val sha256Prefix: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "SHA-256"
        AppLanguage.ENGLISH -> "SHA-256"
        AppLanguage.ARABIC -> "SHA-256"
        AppLanguage.PORTUGUESE -> "SHA-256"
        AppLanguage.SPANISH -> "SHA-256"
        AppLanguage.FRENCH -> "SHA-256"
        AppLanguage.GERMAN -> "SHA-256"
        AppLanguage.RUSSIAN -> "SHA-256"
        AppLanguage.JAPANESE -> "SHA-256"
        AppLanguage.KOREAN -> "SHA-256"
    }

    val applySavedNetworkPreset: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "套用已保存的网络信任方案"
        AppLanguage.ENGLISH -> "Apply saved network trust preset"
        AppLanguage.ARABIC -> "تطبيق إعداد ثقة الشبكة المحفوظ"
        AppLanguage.PORTUGUESE -> "Aplicar predefinição de confiança de rede salva"
        AppLanguage.SPANISH -> "Aplicar preajuste de confianza de red guardado"
        AppLanguage.FRENCH -> "Appliquer le préréglage de confiance réseau enregistré"
        AppLanguage.GERMAN -> "Gespeicherte Netzwerkvertrauens-Voreinstellung anwenden"
        AppLanguage.RUSSIAN -> "Применить сохранённую предустановку доверия сети"
        AppLanguage.JAPANESE -> "保存済みネットワーク信頼プリセットを適用"
        AppLanguage.KOREAN -> "저장된 네트워크 신뢰 사전 설정 적용"
    }

    val saveNetworkPresetTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "保存网络方案"
        AppLanguage.ENGLISH -> "Save Network Preset"
        AppLanguage.ARABIC -> "حفظ إعداد الشبكة"
        AppLanguage.PORTUGUESE -> "Salvar Predefinição de Rede"
        AppLanguage.SPANISH -> "Guardar Preajuste de Red"
        AppLanguage.FRENCH -> "Enregistrer le Préréglage Réseau"
        AppLanguage.GERMAN -> "Netzwerkvoreinstellung speichern"
        AppLanguage.RUSSIAN -> "Сохранить предустановку сети"
        AppLanguage.JAPANESE -> "ネットワークプリセットを保存"
        AppLanguage.KOREAN -> "네트워크 사전 설정 저장"
    }

    val presetName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "方案名称"
        AppLanguage.ENGLISH -> "Preset Name"
        AppLanguage.ARABIC -> "اسم الإعداد"
        AppLanguage.PORTUGUESE -> "Nome da Predefinição"
        AppLanguage.SPANISH -> "Nombre del Preajuste"
        AppLanguage.FRENCH -> "Nom du Préréglage"
        AppLanguage.GERMAN -> "Voreinstellungsname"
        AppLanguage.RUSSIAN -> "Имя предустановки"
        AppLanguage.JAPANESE -> "プリセット名"
        AppLanguage.KOREAN -> "사전 설정 이름"
    }

    val invalidCertificate: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "证书无效"
        AppLanguage.ENGLISH -> "Invalid certificate"
        AppLanguage.ARABIC -> "شهادة غير صالحة"
        AppLanguage.PORTUGUESE -> "Certificado inválido"
        AppLanguage.SPANISH -> "Certificado inválido"
        AppLanguage.FRENCH -> "Certificat invalide"
        AppLanguage.GERMAN -> "Ungültiges Zertifikat"
        AppLanguage.RUSSIAN -> "Недействительный сертификат"
        AppLanguage.JAPANESE -> "無効な証明書"
        AppLanguage.KOREAN -> "잘못된 인증서"
    }

    val invalidCertificatePrivateKey: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "这是私钥,不是证书。请只导出证书部分(.crt/.cer/.pem)"
        AppLanguage.ENGLISH -> "This is a private key, not a certificate. Export only the certificate (.crt/.cer/.pem)"
        AppLanguage.ARABIC -> "هذا مفتاح خاص وليس شهادة. صدّر الشهادة فقط (.crt/.cer/.pem)"
        AppLanguage.PORTUGUESE -> "Esta é uma chave privada, não um certificado. Exporte apenas o certificado (.crt/.cer/.pem)"
        AppLanguage.SPANISH -> "Esta es una clave privada, no un certificado. Exporta solo el certificado (.crt/.cer/.pem)"
        AppLanguage.FRENCH -> "Ceci est une clé privée, pas un certificat. Exportez uniquement le certificat (.crt/.cer/.pem)"
        AppLanguage.GERMAN -> "Dies ist ein privater Schlüssel, kein Zertifikat. Exportieren Sie nur das Zertifikat (.crt/.cer/.pem)"
        AppLanguage.RUSSIAN -> "Это закрытый ключ, а не сертификат. Экспортируйте только сертификат (.crt/.cer/.pem)"
        AppLanguage.JAPANESE -> "これは証明書ではなく秘密鍵です。証明書のみをエクスポートしてください(.crt/.cer/.pem)"
        AppLanguage.KOREAN -> "인증서가 아니라 개인 키입니다. 인증서만 내보내세요(.crt/.cer/.pem)"
    }

    val invalidCertificateUnrecognized: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "无法识别该证书格式。常见原因:PKCS#12/.pfx 密钥库、证书签名请求(CSR)、缺少 PEM 头的 Base64、或编码异常。请导出为 X.509 证书(PEM 或 DER)"
        AppLanguage.ENGLISH -> "Unrecognized certificate format. Common causes: a PKCS#12/.pfx keystore, a signing request (CSR), Base64 without PEM headers, or an odd encoding. Export it as an X.509 certificate (PEM or DER)"
        AppLanguage.ARABIC -> "تنسيق شهادة غير معروف. الأسباب الشائعة: مستودع مفاتيح PKCS#12/.pfx، أو طلب توقيع (CSR)، أو Base64 بدون ترويسات PEM، أو ترميز غير معتاد. صدّرها كشهادة X.509 (PEM أو DER)"
        AppLanguage.PORTUGUESE -> "Formato de certificado não reconhecido. Causas comuns: keystore PKCS#12/.pfx, solicitação de assinatura (CSR), Base64 sem cabeçalhos PEM ou codificação incomum. Exporte como certificado X.509 (PEM ou DER)"
        AppLanguage.SPANISH -> "Formato de certificado no reconocido. Causas comunes: almacén PKCS#12/.pfx, solicitud de firma (CSR), Base64 sin cabeceras PEM o codificación inusual. Expórtalo como certificado X.509 (PEM o DER)"
        AppLanguage.FRENCH -> "Format de certificat non reconnu. Causes courantes : keystore PKCS#12/.pfx, requête de signature (CSR), Base64 sans en-têtes PEM ou encodage inhabituel. Exportez-le comme certificat X.509 (PEM ou DER)"
        AppLanguage.GERMAN -> "Unbekanntes Zertifikatsformat. Häufige Ursachen: PKCS#12/.pfx-Keystore, Signing-Request (CSR), Base64 ohne PEM-Header oder unübliche Codierung. Exportieren Sie es als X.509-Zertifikat (PEM oder DER)"
        AppLanguage.RUSSIAN -> "Формат сертификата не распознан. Частые причины: хранилище ключей PKCS#12/.pfx, запрос на подпись (CSR), Base64 без PEM-заголовков или необычная кодировка. Экспортируйте как сертификат X.509 (PEM или DER)"
        AppLanguage.JAPANESE -> "証明書フォーマットを認識できません。よくある原因: PKCS#12/.pfx キーストア、署名要求(CSR)、PEMヘッダーのないBase64、特異なエンコーディング。X.509証明書(PEMまたはDER)としてエクスポートしてください"
        AppLanguage.KOREAN -> "인증서 형식을 인식할 수 없습니다. 흔한 원인: PKCS#12/.pfx 키스토어, 서명 요청(CSR), PEM 헤더가 없는 Base64, 잘못된 인코딩. X.509 인증서(PEM 또는 DER)로 내보내세요"
    }

    val preflightPackageAutoTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "包名将自动生成"
        AppLanguage.ENGLISH -> "Package Name Will Be Auto-generated"
        AppLanguage.ARABIC -> "سيتم إنشاء اسم الحزمة تلقائيًا"
        AppLanguage.PORTUGUESE -> "O Nome do Pacote Será Gerado Automaticamente"
        AppLanguage.SPANISH -> "El Nombre del Paquete Se Generará Automáticamente"
        AppLanguage.FRENCH -> "Le Nom du Paquet Sera Généré Automatiquement"
        AppLanguage.GERMAN -> "Paketname wird automatisch generiert"
        AppLanguage.RUSSIAN -> "Имя пакета будет сгенерировано автоматически"
        AppLanguage.JAPANESE -> "パッケージ名は自動生成されます"
        AppLanguage.KOREAN -> "패키지 이름이 자동 생성됩니다"
    }

    val preflightPackageAutoMessage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "导出时会根据应用名称生成包名。正式分发前建议设置稳定包名。"
        AppLanguage.ENGLISH -> "The package name will be generated from the app name during export. Set a stable package name before distribution."
        AppLanguage.ARABIC -> "سيتم إنشاء اسم الحزمة من اسم التطبيق أثناء التصدير. يوصى بتحديد اسم حزمة ثابت قبل التوزيع."
        AppLanguage.PORTUGUESE -> "O nome do pacote será gerado a partir do nome do app durante a exportação. Defina um nome de pacote estável antes da distribuição."
        AppLanguage.SPANISH -> "El nombre del paquete se generará a partir del nombre de la app durante la exportación. Establezca un nombre de paquete estable antes de la distribución."
        AppLanguage.FRENCH -> "Le nom du paquet sera généré à partir du nom de l'application lors de l'export. Définissez un nom de paquet stable avant la distribution."
        AppLanguage.GERMAN -> "Der Paketname wird beim Export aus dem App-Namen generiert. Legen Sie vor der Verteilung einen stabilen Paketnamen fest."
        AppLanguage.RUSSIAN -> "Имя пакета будет сгенерировано из имени приложения при экспорте. Установите стабильное имя пакета перед распространением."
        AppLanguage.JAPANESE -> "エクスポート時にアプリ名からパッケージ名が生成されます。配布前に安定したパッケージ名を設定してください。"
        AppLanguage.KOREAN -> "내보낼 때 앱 이름에서 패키지 이름이 생성됩니다. 배포 전에 안정적인 패키지 이름을 설정하세요."
    }

    val preflightIconMissingTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "未设置图标"
        AppLanguage.ENGLISH -> "Icon Missing"
        AppLanguage.ARABIC -> "لا توجد أيقونة"
        AppLanguage.PORTUGUESE -> "Ícone Ausente"
        AppLanguage.SPANISH -> "Icono Faltante"
        AppLanguage.FRENCH -> "Icône Manquante"
        AppLanguage.GERMAN -> "Symbol fehlt"
        AppLanguage.RUSSIAN -> "Значок отсутствует"
        AppLanguage.JAPANESE -> "アイコン未設定"
        AppLanguage.KOREAN -> "아이콘 누락"
    }

    val preflightIconMissingMessage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "导出会使用默认图标。正式分发前建议配置应用图标。"
        AppLanguage.ENGLISH -> "Export will use the default icon. Configure an app icon before distribution."
        AppLanguage.ARABIC -> "سيستخدم التصدير الأيقونة الافتراضية. يوصى بتعيين أيقونة للتطبيق قبل التوزيع."
        AppLanguage.PORTUGUESE -> "A exportação usará o ícone padrão. Configure um ícone de app antes da distribuição."
        AppLanguage.SPANISH -> "La exportación usará el icono predeterminado. Configure un icono de app antes de la distribución."
        AppLanguage.FRENCH -> "L'export utilisera l'icône par défaut. Configurez une icône d'application avant la distribution."
        AppLanguage.GERMAN -> "Der Export verwendet das Standardsymbol. Konfigurieren Sie vor der Verteilung ein App-Symbol."
        AppLanguage.RUSSIAN -> "При экспорте будет использоваться значок по умолчанию. Настройте значок приложения перед распространением."
        AppLanguage.JAPANESE -> "エクスポートはデフォルトアイコンを使用します。配布前にアプリアイコンを設定してください。"
        AppLanguage.KOREAN -> "내보내기 시 기본 아이콘을 사용합니다. 배포 전에 앱 아이콘을 구성하세요."
    }

    val preflightOverlayPermissionTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "包含悬浮窗权限"
        AppLanguage.ENGLISH -> "Overlay Permission Included"
        AppLanguage.ARABIC -> "يتضمن إذن النافذة العائمة"
        AppLanguage.PORTUGUESE -> "Permissão de Sobreposição Incluída"
        AppLanguage.SPANISH -> "Permiso de Superposición Incluido"
        AppLanguage.FRENCH -> "Permission de Superposition Incluse"
        AppLanguage.GERMAN -> "Overlay-Berechtigung enthalten"
        AppLanguage.RUSSIAN -> "Включено разрешение на наложение"
        AppLanguage.JAPANESE -> "オーバーレイ権限を含む"
        AppLanguage.KOREAN -> "오버레이 권한 포함됨"
    }

    val preflightOverlayPermissionMessage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "部分设备会要求用户手动授权，导出前请确认这是必要能力。"
        AppLanguage.ENGLISH -> "Some devices require users to grant this manually. Confirm that this capability is necessary."
        AppLanguage.ARABIC -> "تتطلب بعض الأجهزة منح هذا الإذن يدويًا. تأكد من أن هذه القدرة ضرورية قبل التصدير."
        AppLanguage.PORTUGUESE -> "Alguns dispositivos exigem que os usuários concedam isso manualmente. Confirme que essa capacidade é necessária."
        AppLanguage.SPANISH -> "Algunos dispositivos requieren que los usuarios concedan esto manualmente. Confirme que esta capacidad es necesaria."
        AppLanguage.FRENCH -> "Certains appareils exigent que les utilisateurs accordent cela manuellement. Confirmez que cette capacité est nécessaire."
        AppLanguage.GERMAN -> "Einige Geräte erfordern, dass Benutzer dies manuell gewähren. Bestätigen Sie, dass diese Fähigkeit erforderlich ist."
        AppLanguage.RUSSIAN -> "Некоторые устройства требуют ручного предоставления разрешения. Подтвердите, что эта возможность необходима."
        AppLanguage.JAPANESE -> "一部のデバイスではユーザーが手動で付与する必要があります。この機能が必要か確認してください。"
        AppLanguage.KOREAN -> "일부 기기에서는 사용자가 수동으로 권한을 부여해야 합니다. 이 기능이 필요한지 확인하세요."
    }

    val preflightNoCaAnchorTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "没有可用 CA 信任锚"
        AppLanguage.ENGLISH -> "No Available CA Trust Anchor"
        AppLanguage.ARABIC -> "لا يوجد مرساة ثقة CA متاحة"
        AppLanguage.PORTUGUESE -> "Nenhuma Âncora de Confiança CA Disponível"
        AppLanguage.SPANISH -> "Sin Ancla de Confianza CA Disponible"
        AppLanguage.FRENCH -> "Aucune Ancre de Confiance CA Disponible"
        AppLanguage.GERMAN -> "Kein verfügbarer CA-Vertrauensanker"
        AppLanguage.RUSSIAN -> "Нет доступного якоря доверия CA"
        AppLanguage.JAPANESE -> "利用可能なCA信頼アンカーなし"
        AppLanguage.KOREAN -> "사용 가능한 CA 신뢰 앵커 없음"
    }

    val preflightNoCaAnchorMessage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "至少需要信任系统 CA、用户安装 CA 或导入一个自定义 CA。"
        AppLanguage.ENGLISH -> "At least one trust anchor is required: system CA, user-installed CA, or an imported custom CA."
        AppLanguage.ARABIC -> "يلزم وجود مرساة ثقة واحدة على الأقل: CA النظام أو CA المثبتة من المستخدم أو CA مخصصة مستوردة."
        AppLanguage.PORTUGUESE -> "Pelo menos uma âncora de confiança é necessária: CA do sistema, CA instalada pelo usuário ou CA personalizada importada."
        AppLanguage.SPANISH -> "Se requiere al menos un ancla de confianza: CA del sistema, CA instalada por el usuario o CA personalizada importada."
        AppLanguage.FRENCH -> "Au moins une ancre de confiance est requise : CA système, CA installé par l'utilisateur ou CA personnalisé importé."
        AppLanguage.GERMAN -> "Mindestens ein Vertrauensanker ist erforderlich: System-CA, benutzerinstallierte CA oder importierte benutzerdefinierte CA."
        AppLanguage.RUSSIAN -> "Требуется хотя бы один якорь доверия: системный CA, пользовательский CA или импортированный пользовательский CA."
        AppLanguage.JAPANESE -> "少なくとも1つの信頼アンカーが必要です：システムCA、ユーザーインストールCA、またはインポートされたカスタムCA。"
        AppLanguage.KOREAN -> "하나 이상의 신뢰 앵커가 필요합니다: 시스템 CA, 사용자 설치 CA 또는 가져온 사용자 지정 CA."
    }

    val preflightCleartextTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "允许明文流量"
        AppLanguage.ENGLISH -> "Cleartext Traffic Enabled"
        AppLanguage.ARABIC -> "تمكين حركة المرور غير المشفرة"
        AppLanguage.PORTUGUESE -> "Tráfego de Texto Claro Ativado"
        AppLanguage.SPANISH -> "Tráfico de Texto Claro Habilitado"
        AppLanguage.FRENCH -> "Trafic en Clair Activé"
        AppLanguage.GERMAN -> "Klartextverkehr aktiviert"
        AppLanguage.RUSSIAN -> "Открытый трафик включён"
        AppLanguage.JAPANESE -> "クリアテキストトラフィック有効"
        AppLanguage.KOREAN -> "평문 트래픽 활성화됨"
    }

    val preflightCleartextMessage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "该设置兼容内网、localhost 和旧站点，但正式发布时建议关闭。"
        AppLanguage.ENGLISH -> "This keeps compatibility with intranet, localhost, and legacy sites, but should be disabled for production releases."
        AppLanguage.ARABIC -> "يوفر هذا الإعداد التوافق مع الشبكات الداخلية وlocalhost والمواقع القديمة، لكن يُنصح بتعطيله في الإصدارات الرسمية."
        AppLanguage.PORTUGUESE -> "Isso mantém compatibilidade com intranet, localhost e sites legados, mas deve ser desativado para releases de produção."
        AppLanguage.SPANISH -> "Esto mantiene compatibilidad con intranet, localhost y sitios heredados, pero debe desactivarse para releases de producción."
        AppLanguage.FRENCH -> "Cela conserve la compatibilité avec intranet, localhost et les sites hérités, mais doit être désactivé pour les versions de production."
        AppLanguage.GERMAN -> "Dies bewahrt Kompatibilität mit Intranet, localhost und Legacy-Sites, sollte aber für Produktions- Releases deaktiviert werden."
        AppLanguage.RUSSIAN -> "Это сохраняет совместимость с интрасетью, localhost и устаревшими сайтами, но должно быть отключено для production-релизов."
        AppLanguage.JAPANESE -> "これはイントラネット、localhost、レガシーサイトとの互換性を維持しますが、本番リリースでは無効にする必要があります。"
        AppLanguage.KOREAN -> "이는 인트라넷, localhost, 레거시 사이트와의 호환성을 유지하지만, 프로덕션 릴리스에서는 비활성화해야 합니다."
    }

    val preflightCustomCaUnavailable: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自定义 CA 不可用"
        AppLanguage.ENGLISH -> "Custom CA Unavailable"
        AppLanguage.ARABIC -> "CA المخصصة غير متاحة"
        AppLanguage.PORTUGUESE -> "CA Personalizada Indisponível"
        AppLanguage.SPANISH -> "CA Personalizada No Disponible"
        AppLanguage.FRENCH -> "CA Personnalisé Indisponible"
        AppLanguage.GERMAN -> "Benutzerdefinierte CA nicht verfügbar"
        AppLanguage.RUSSIAN -> "Пользовательский CA недоступен"
        AppLanguage.JAPANESE -> "カスタムCA利用不可"
        AppLanguage.KOREAN -> "사용자 지정 CA 사용 불가"
    }

    val preflightEntryFileIssue: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "入口文件异常"
        AppLanguage.ENGLISH -> "Entry File Issue"
        AppLanguage.ARABIC -> "مشكلة في ملف الدخول"
        AppLanguage.PORTUGUESE -> "Problema no Arquivo de Entrada"
        AppLanguage.SPANISH -> "Problema en Archivo de Entrada"
        AppLanguage.FRENCH -> "Problème de Fichier d'Entrée"
        AppLanguage.GERMAN -> "Problem mit Einstiegsdatei"
        AppLanguage.RUSSIAN -> "Проблема с файлом точки входа"
        AppLanguage.JAPANESE -> "エントリファイルの問題"
        AppLanguage.KOREAN -> "진입 파일 문제"
    }

    val preflightHtmlFileIssue: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "HTML 文件异常"
        AppLanguage.ENGLISH -> "HTML File Issue"
        AppLanguage.ARABIC -> "مشكلة في ملف HTML"
        AppLanguage.PORTUGUESE -> "Problema no Arquivo HTML"
        AppLanguage.SPANISH -> "Problema en Archivo HTML"
        AppLanguage.FRENCH -> "Problème de Fichier HTML"
        AppLanguage.GERMAN -> "Problem mit HTML-Datei"
        AppLanguage.RUSSIAN -> "Проблема с файлом HTML"
        AppLanguage.JAPANESE -> "HTMLファイルの問題"
        AppLanguage.KOREAN -> "HTML 파일 문제"
    }

    val preflightGalleryIssue: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "图库资源异常"
        AppLanguage.ENGLISH -> "Gallery Resource Issue"
        AppLanguage.ARABIC -> "مشكلة في موارد المعرض"
        AppLanguage.PORTUGUESE -> "Problema em Recursos da Galeria"
        AppLanguage.SPANISH -> "Problema en Recursos de Galería"
        AppLanguage.FRENCH -> "Problème de Ressources de Galerie"
        AppLanguage.GERMAN -> "Problem mit Galerie-Ressourcen"
        AppLanguage.RUSSIAN -> "Проблема с ресурсами галереи"
        AppLanguage.JAPANESE -> "ギャラリーリソースの問題"
        AppLanguage.KOREAN -> "갤러리 리소스 문제"
    }

    val preflightRuntimeProjectIssue: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "运行时项目目录异常"
        AppLanguage.ENGLISH -> "Runtime Project Directory Issue"
        AppLanguage.ARABIC -> "مشكلة في مجلد مشروع وقت التشغيل"
        AppLanguage.PORTUGUESE -> "Problema no Diretório do Projeto de Runtime"
        AppLanguage.SPANISH -> "Problema en Directorio de Proyecto de Runtime"
        AppLanguage.FRENCH -> "Problème de Répertoire de Projet Runtime"
        AppLanguage.GERMAN -> "Problem mit Runtime-Projektverzeichnis"
        AppLanguage.RUSSIAN -> "Проблема с каталогом runtime-проекта"
        AppLanguage.JAPANESE -> "ランタイムプロジェクトディレクトリの問題"
        AppLanguage.KOREAN -> "런타임 프로젝트 디렉터리 문제"
    }

    val preflightMediaFileIssue: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "媒体文件异常"
        AppLanguage.ENGLISH -> "Media File Issue"
        AppLanguage.ARABIC -> "مشكلة في ملف الوسائط"
        AppLanguage.PORTUGUESE -> "Problema no Arquivo de Mídia"
        AppLanguage.SPANISH -> "Problema en Archivo de Medios"
        AppLanguage.FRENCH -> "Problème de Fichier Multimédia"
        AppLanguage.GERMAN -> "Problem mit Mediendatei"
        AppLanguage.RUSSIAN -> "Проблема с медиафайлом"
        AppLanguage.JAPANESE -> "メディアファイルの問題"
        AppLanguage.KOREAN -> "미디어 파일 문제"
    }

    val preflightInputIssue: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "导出输入异常"
        AppLanguage.ENGLISH -> "Export Input Issue"
        AppLanguage.ARABIC -> "مشكلة في مدخلات التصدير"
        AppLanguage.PORTUGUESE -> "Problema na Entrada de Exportação"
        AppLanguage.SPANISH -> "Problema en Entrada de Exportación"
        AppLanguage.FRENCH -> "Problème d'Entrée d'Export"
        AppLanguage.GERMAN -> "Problem mit Export-Eingabe"
        AppLanguage.RUSSIAN -> "Проблема с входными данными экспорта"
        AppLanguage.JAPANESE -> "エクスポート入力の問題"
        AppLanguage.KOREAN -> "내보내기 입력 문제"
    }

    val bootAutoStart: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "开机自启动"
        AppLanguage.ENGLISH -> "Boot Auto Start"
        AppLanguage.ARABIC -> "التشغيل التلقائي عند الإقلاع"
        AppLanguage.PORTUGUESE -> "Início Automático no Boot"
        AppLanguage.SPANISH -> "Inicio Automático en Arranque"
        AppLanguage.FRENCH -> "Démarrage Automatique au Boot"
        AppLanguage.GERMAN -> "Autostart beim Booten"
        AppLanguage.RUSSIAN -> "Автозапуск при загрузке"
        AppLanguage.JAPANESE -> "起動時自動開始"
        AppLanguage.KOREAN -> "부팅 시 자동 시작"
    }

    val scheduledAutoStart: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "定时自启动"
        AppLanguage.ENGLISH -> "Scheduled Auto Start"
        AppLanguage.ARABIC -> "التشغيل التلقائي المجدول"
        AppLanguage.PORTUGUESE -> "Início Automático Programado"
        AppLanguage.SPANISH -> "Inicio Automático Programado"
        AppLanguage.FRENCH -> "Démarrage Automatique Planifié"
        AppLanguage.GERMAN -> "Geplanter Autostart"
        AppLanguage.RUSSIAN -> "Запланированный автозапуск"
        AppLanguage.JAPANESE -> "スケジュール自動起動"
        AppLanguage.KOREAN -> "예약된 자동 시작"
    }

    val scheduledRepeat: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "重复启动"
        AppLanguage.ENGLISH -> "Repeat"
        AppLanguage.ARABIC -> "تكرار"
        AppLanguage.PORTUGUESE -> "Repetir"
        AppLanguage.SPANISH -> "Repetir"
        AppLanguage.FRENCH -> "Répéter"
        AppLanguage.GERMAN -> "Wiederholen"
        AppLanguage.RUSSIAN -> "Повторять"
        AppLanguage.JAPANESE -> "繰り返し"
        AppLanguage.KOREAN -> "반복"
    }

    val launchDate: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "启动日期"
        AppLanguage.ENGLISH -> "Launch Date"
        AppLanguage.ARABIC -> "تاريخ التشغيل"
        AppLanguage.PORTUGUESE -> "Data de Início"
        AppLanguage.SPANISH -> "Fecha de Inicio"
        AppLanguage.FRENCH -> "Date de Lancement"
        AppLanguage.GERMAN -> "Startdatum"
        AppLanguage.RUSSIAN -> "Дата запуска"
        AppLanguage.JAPANESE -> "起動日"
        AppLanguage.KOREAN -> "시작 날짜"
    }

    val today: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "今天"
        AppLanguage.ENGLISH -> "Today"
        AppLanguage.ARABIC -> "اليوم"
        AppLanguage.PORTUGUESE -> "Hoje"
        AppLanguage.SPANISH -> "Hoy"
        AppLanguage.FRENCH -> "Aujourd'hui"
        AppLanguage.GERMAN -> "Heute"
        AppLanguage.RUSSIAN -> "Сегодня"
        AppLanguage.JAPANESE -> "今日"
        AppLanguage.KOREAN -> "오늘"
    }

    val tomorrow: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "明天"
        AppLanguage.ENGLISH -> "Tomorrow"
        AppLanguage.ARABIC -> "غداً"
        AppLanguage.PORTUGUESE -> "Amanhã"
        AppLanguage.SPANISH -> "Mañana"
        AppLanguage.FRENCH -> "Demain"
        AppLanguage.GERMAN -> "Morgen"
        AppLanguage.RUSSIAN -> "Завтра"
        AppLanguage.JAPANESE -> "明日"
        AppLanguage.KOREAN -> "내일"
    }

    val nextLaunchTime: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下次启动"
        AppLanguage.ENGLISH -> "Next Launch"
        AppLanguage.ARABIC -> "التشغيل التالي"
        AppLanguage.PORTUGUESE -> "Próximo Início"
        AppLanguage.SPANISH -> "Próximo Inicio"
        AppLanguage.FRENCH -> "Prochain Lancement"
        AppLanguage.GERMAN -> "Nächster Start"
        AppLanguage.RUSSIAN -> "Следующий запуск"
        AppLanguage.JAPANESE -> "次回起動"
        AppLanguage.KOREAN -> "다음 시작"
    }

    val exactAlarmPermissionHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "需要精确闹钟权限才能准时启动，点击前往设置"
        AppLanguage.ENGLISH -> "Exact alarm permission required for on-time launch, tap to open settings"
        AppLanguage.ARABIC -> "يلزم إذن المنبه الدقيق للتشغيل في الوقت المحدد، انقر لفتح الإعدادات"
        AppLanguage.PORTUGUESE -> "Permissão de alarme exato necessária para início pontual, toque para abrir as configurações"
        AppLanguage.SPANISH -> "Se requiere permiso de alarma exacta para inicio puntual, toque para abrir configuración"
        AppLanguage.FRENCH -> "Permission d'alarme exacte requise pour un lancement à l'heure, appuyez pour ouvrir les paramètres"
        AppLanguage.GERMAN -> "Berechtigung für exakten Alarm für pünktlichen Start erforderlich, tippen, um Einstellungen zu öffnen"
        AppLanguage.RUSSIAN -> "Для запуска вовремя требуется разрешение на точный будильник, нажмите, чтобы открыть настройки"
        AppLanguage.JAPANESE -> "時刻通りの起動には正確なアラーム権限が必要です、タップして設定を開く"
        AppLanguage.KOREAN -> "정시 시작을 위해 정확한 알람 권한이 필요합니다, 탭하여 설정 열기"
    }

    val batteryOptimizationHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "建议关闭电池优化以保证自启动可靠触发，点击前往设置"
        AppLanguage.ENGLISH -> "Disable battery optimization for reliable auto start, tap to open settings"
        AppLanguage.ARABIC -> "قم بتعطيل تحسين البطارية لضمان التشغيل التلقائي الموثوق، انقر لفتح الإعدادات"
        AppLanguage.PORTUGUESE -> "Desative a otimização de bateria para início automático confiável, toque para abrir as configurações"
        AppLanguage.SPANISH -> "Desactive la optimización de batería para inicio automático confiable, toque para abrir configuración"
        AppLanguage.FRENCH -> "Désactivez l'optimisation de batterie pour un démarrage automatique fiable, appuyez pour ouvrir les paramètres"
        AppLanguage.GERMAN -> "Batterieoptimierung für zuverlässigen Autostart deaktivieren, tippen, um Einstellungen zu öffnen"
        AppLanguage.RUSSIAN -> "Отключите оптимизацию батареи для надёжного автозапуска, нажмите, чтобы открыть настройки"
        AppLanguage.JAPANESE -> "確実な自動起動のためバッテリー最適化を無効にしてください、タップして設定を開く"
        AppLanguage.KOREAN -> "신뢰할 수 있는 자동 시작을 위해 배터리 최적화를 비활성화하세요, 탭하여 설정 열기"
    }

    val bootDelay: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "开机延迟"
        AppLanguage.ENGLISH -> "Boot Delay"
        AppLanguage.ARABIC -> "تأخير التشغيل"
        AppLanguage.PORTUGUESE -> "Atraso de Boot"
        AppLanguage.SPANISH -> "Retraso de Arranque"
        AppLanguage.FRENCH -> "Délai de Boot"
        AppLanguage.GERMAN -> "Boot-Verzögerung"
        AppLanguage.RUSSIAN -> "Задержка загрузки"
        AppLanguage.JAPANESE -> "起動遅延"
        AppLanguage.KOREAN -> "부팅 지연"
    }

    val oemAutoStartHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "检测到 %s 系统，请在手机管家中将本应用加入自启动白名单，否则系统可能阻止自启动"
        AppLanguage.ENGLISH -> "%s system detected. Please add this app to auto-start whitelist in phone manager to prevent system from blocking auto start"
        AppLanguage.ARABIC -> "تم اكتشاف نظام %s. يرجى إضافة هذا التطبيق إلى القائمة البيضاء للتشغيل التلقائي في مدير الهاتف"
        AppLanguage.PORTUGUESE -> "Sistema %s detectado. Adicione este app à lista branca de início automático no gerenciador do telefone para evitar que o sistema bloqueie o início automático"
        AppLanguage.SPANISH -> "Sistema %s detectado. Agregue esta app a la lista blanca de inicio automático en el administrador del teléfono para evitar que el sistema bloquee el inicio automático"
        AppLanguage.FRENCH -> "Système %s détecté. Ajoutez cette application à la liste blanche de démarrage automatique dans le gestionnaire du téléphone pour empêcher le système de bloquer le démarrage automatique"
        AppLanguage.GERMAN -> "%s-System erkannt. Bitte diese App zur Autostart-Whitelist im Telefon-Manager hinzufügen, damit das System den Autostart nicht blockiert"
        AppLanguage.RUSSIAN -> "Обнаружена система %s. Добавьте это приложение в белый список автозапуска в диспетчере телефона, чтобы система не блокировала автозапуск"
        AppLanguage.JAPANESE -> "%sシステムを検出しました。システムが自動起動をブロックしないよう、電話マネージャーでこのアプリを自動起動ホワイトリストに追加してください"
        AppLanguage.KOREAN -> "%s 시스템이 감지되었습니다. 시스템이 자동 시작을 차단하지 않도록 전화 관리자에서 이 앱을 자동 시작 화이트리스트에 추가하세요"
    }

    val selectAnnouncementStyle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择公告风格"
        AppLanguage.ENGLISH -> "Select announcement style"
        AppLanguage.ARABIC -> "اختيار نمط الإعلان"
        AppLanguage.PORTUGUESE -> "Selecionar estilo de anúncio"
        AppLanguage.SPANISH -> "Seleccionar estilo de anuncio"
        AppLanguage.FRENCH -> "Sélectionner le style d'annonce"
        AppLanguage.GERMAN -> "Ankündigungsstil auswählen"
        AppLanguage.RUSSIAN -> "Выберите стиль объявления"
        AppLanguage.JAPANESE -> "お知らせスタイルを選択"
        AppLanguage.KOREAN -> "공지 스타일 선택"
    }

    val announcementStyleClean: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "浅色"
        AppLanguage.ENGLISH -> "Light"
        AppLanguage.ARABIC -> "فاتح"
        AppLanguage.PORTUGUESE -> "Claro"
        AppLanguage.SPANISH -> "Claro"
        AppLanguage.FRENCH -> "Clair"
        AppLanguage.GERMAN -> "Hell"
        AppLanguage.RUSSIAN -> "Светлый"
        AppLanguage.JAPANESE -> "ライト"
        AppLanguage.KOREAN -> "라이트"
    }

    val announcementStyleCleanDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "清爽明亮，适合常规提示"
        AppLanguage.ENGLISH -> "Bright and clean for routine notices"
        AppLanguage.ARABIC -> "مشرق وواضح للإشعارات العادية"
        AppLanguage.PORTUGUESE -> "Brilhante e limpo para avisos rotineiros"
        AppLanguage.SPANISH -> "Brillante y limpio para avisos rutinarios"
        AppLanguage.FRENCH -> "Lumineux et net pour les avis de routine"
        AppLanguage.GERMAN -> "Hell und klar für routinemäßige Hinweise"
        AppLanguage.RUSSIAN -> "Яркий и чистый для обычных уведомлений"
        AppLanguage.JAPANESE -> "明るくクリーン、定例通知に最適"
        AppLanguage.KOREAN -> "밝고 깔끔한 일반 공지 스타일"
    }

    val announcementStyleDark: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "深色"
        AppLanguage.ENGLISH -> "Dark"
        AppLanguage.ARABIC -> "داكن"
        AppLanguage.PORTUGUESE -> "Escuro"
        AppLanguage.SPANISH -> "Oscuro"
        AppLanguage.FRENCH -> "Sombre"
        AppLanguage.GERMAN -> "Dunkel"
        AppLanguage.RUSSIAN -> "Тёмный"
        AppLanguage.JAPANESE -> "ダーク"
        AppLanguage.KOREAN -> "다크"
    }

    val announcementStyleDarkDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "高对比，适合重要提醒"
        AppLanguage.ENGLISH -> "High contrast for important alerts"
        AppLanguage.ARABIC -> "تباين عالٍ للتنبيهات المهمة"
        AppLanguage.PORTUGUESE -> "Alto contraste para alertas importantes"
        AppLanguage.SPANISH -> "Alto contraste para alertas importantes"
        AppLanguage.FRENCH -> "Contraste élevé pour alertes importantes"
        AppLanguage.GERMAN -> "Hoher Kontrast für wichtige Warnungen"
        AppLanguage.RUSSIAN -> "Высокий контраст для важных оповещений"
        AppLanguage.JAPANESE -> "重要な通知に適した高コントラスト"
        AppLanguage.KOREAN -> "중요한 알림에 적합한 고대비"
    }

    val announcementShowIcon: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "显示图标"
        AppLanguage.ENGLISH -> "Show Icon"
        AppLanguage.ARABIC -> "إظهار الأيقونة"
        AppLanguage.PORTUGUESE -> "Mostrar Ícone"
        AppLanguage.SPANISH -> "Mostrar Icono"
        AppLanguage.FRENCH -> "Afficher l'icône"
        AppLanguage.GERMAN -> "Symbol anzeigen"
        AppLanguage.RUSSIAN -> "Показать значок"
        AppLanguage.JAPANESE -> "アイコンを表示"
        AppLanguage.KOREAN -> "아이콘 표시"
    }

    val announcementShowIconHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "在弹窗左上角显示图标"
        AppLanguage.ENGLISH -> "Show icon in the top-left of the popup"
        AppLanguage.ARABIC -> "إظهار الأيقونة في أعلى يسار النافذة"
        AppLanguage.PORTUGUESE -> "Mostrar ícone no canto superior esquerdo"
        AppLanguage.SPANISH -> "Mostrar icono en la esquina superior izquierda"
        AppLanguage.FRENCH -> "Afficher l'icône en haut à gauche"
        AppLanguage.GERMAN -> "Symbol oben links im Popup anzeigen"
        AppLanguage.RUSSIAN -> "Показать значок в левом верхнем углу"
        AppLanguage.JAPANESE -> "ポップアップの左上にアイコンを表示"
        AppLanguage.KOREAN -> "팝업 왼쪽 상단에 아이콘 표시"
    }

    val announcementCustomIcon: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自定义图标"
        AppLanguage.ENGLISH -> "Custom Icon"
        AppLanguage.ARABIC -> "أيقونة مخصصة"
        AppLanguage.PORTUGUESE -> "Ícone Personalizado"
        AppLanguage.SPANISH -> "Icono Personalizado"
        AppLanguage.FRENCH -> "Icône personnalisée"
        AppLanguage.GERMAN -> "Eigenes Symbol"
        AppLanguage.RUSSIAN -> "Свой значок"
        AppLanguage.JAPANESE -> "カスタムアイコン"
        AppLanguage.KOREAN -> "커스텀 아이콘"
    }

    val announcementRemoveCustomIcon: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "移除自定义图标"
        AppLanguage.ENGLISH -> "Remove Custom Icon"
        AppLanguage.ARABIC -> "إزالة الأيقونة المخصصة"
        AppLanguage.PORTUGUESE -> "Remover Ícone Personalizado"
        AppLanguage.SPANISH -> "Eliminar Icono Personalizado"
        AppLanguage.FRENCH -> "Supprimer l'icône personnalisée"
        AppLanguage.GERMAN -> "Eigenes Symbol entfernen"
        AppLanguage.RUSSIAN -> "Удалить свой значок"
        AppLanguage.JAPANESE -> "カスタムアイコンを削除"
        AppLanguage.KOREAN -> "커스텀 아이콘 제거"
    }

    val langEnglish: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "英文"
        AppLanguage.ENGLISH -> "English"
        AppLanguage.ARABIC -> "الإنجليزية"
        AppLanguage.PORTUGUESE -> "Inglês"
        AppLanguage.SPANISH -> "Inglés"
        AppLanguage.FRENCH -> "Anglais"
        AppLanguage.GERMAN -> "Englisch"
        AppLanguage.RUSSIAN -> "Английский"
        AppLanguage.JAPANESE -> "英語"
        AppLanguage.KOREAN -> "영어"
    }

    val langJapanese: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "日文"
        AppLanguage.ENGLISH -> "Japanese"
        AppLanguage.ARABIC -> "اليابانية"
        AppLanguage.PORTUGUESE -> "Japonês"
        AppLanguage.SPANISH -> "Japonés"
        AppLanguage.FRENCH -> "Japonais"
        AppLanguage.GERMAN -> "Japanisch"
        AppLanguage.RUSSIAN -> "Японский"
        AppLanguage.JAPANESE -> "日本語"
        AppLanguage.KOREAN -> "일본어"
    }

    val langArabic: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "阿拉伯语"
        AppLanguage.ENGLISH -> "Arabic"
        AppLanguage.ARABIC -> "العربية"
        AppLanguage.PORTUGUESE -> "Árabe"
        AppLanguage.SPANISH -> "Árabe"
        AppLanguage.FRENCH -> "Arabe"
        AppLanguage.GERMAN -> "Arabisch"
        AppLanguage.RUSSIAN -> "Арабский"
        AppLanguage.JAPANESE -> "アラビア語"
        AppLanguage.KOREAN -> "아랍어"
    }

    val details: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "详情"
        AppLanguage.ENGLISH -> "Details"
        AppLanguage.ARABIC -> "التفاصيل"
        AppLanguage.PORTUGUESE -> "Detalhes"
        AppLanguage.SPANISH -> "Detalles"
        AppLanguage.FRENCH -> "Détails"
        AppLanguage.GERMAN -> "Einzelheiten"
        AppLanguage.RUSSIAN -> "Подробности"
        AppLanguage.JAPANESE -> "詳細"
        AppLanguage.KOREAN -> "세부 정보"
    }

    val clickToSelectOrUseButton: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "点击选择或使用下方按钮"
        AppLanguage.ENGLISH -> "Click to select or use button below"
        AppLanguage.ARABIC -> "انقر للاختيار أو استخدم الزر أدناه"
        AppLanguage.PORTUGUESE -> "Clique para selecionar ou use o botão abaixo"
        AppLanguage.SPANISH -> "Haga clic para seleccionar o use el botón de abajo"
        AppLanguage.FRENCH -> "Cliquez pour sélectionner ou utilisez le bouton ci-dessous"
        AppLanguage.GERMAN -> "Klicken Sie zum Auswählen oder verwenden Sie die Schaltfläche unten"
        AppLanguage.RUSSIAN -> "Нажмите для выбора или используйте кнопку ниже"
        AppLanguage.JAPANESE -> "クリックして選択または下のボタンを使用"
        AppLanguage.KOREAN -> "클릭하여 선택하거나 아래 버튼 사용"
    }

    val aiSettings: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "AI 设置"
        AppLanguage.ENGLISH -> "AI Settings"
        AppLanguage.ARABIC -> "إعدادات AI"
        AppLanguage.PORTUGUESE -> "Configurações de AI"
        AppLanguage.SPANISH -> "Configuración de AI"
        AppLanguage.FRENCH -> "Paramètres AI"
        AppLanguage.GERMAN -> "AI-Einstellungen"
        AppLanguage.RUSSIAN -> "Настройки AI"
        AppLanguage.JAPANESE -> "AI設定"
        AppLanguage.KOREAN -> "AI 설정"
    }

    val apiKeys: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "API 密钥"
        AppLanguage.ENGLISH -> "API Keys"
        AppLanguage.ARABIC -> "مفاتيح API"
        AppLanguage.PORTUGUESE -> "Chaves de API"
        AppLanguage.SPANISH -> "Claves de API"
        AppLanguage.FRENCH -> "Clés API"
        AppLanguage.GERMAN -> "API-Schlüssel"
        AppLanguage.RUSSIAN -> "Ключи API"
        AppLanguage.JAPANESE -> "APIキー"
        AppLanguage.KOREAN -> "API 키"
    }

    val noApiKeysHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "暂无 API 密钥，点击右上角添加"
        AppLanguage.ENGLISH -> "No API keys yet, click top right to add"
        AppLanguage.ARABIC -> "لا توجد مفاتيح API بعد، انقر في الأعلى للإضافة"
        AppLanguage.PORTUGUESE -> "Nenhuma chave de API ainda, clique no canto superior direito para adicionar"
        AppLanguage.SPANISH -> "Aún no hay claves de API, haga clic arriba a la derecha para agregar"
        AppLanguage.FRENCH -> "Aucune clé API pour le moment, cliquez en haut à droite pour ajouter"
        AppLanguage.GERMAN -> "Noch keine API-Schlüssel, oben rechts klicken zum Hinzufügen"
        AppLanguage.RUSSIAN -> "Ключей API пока нет, нажмите вверху справа, чтобы добавить"
        AppLanguage.JAPANESE -> "APIキーがまだありません、右上をクリックして追加"
        AppLanguage.KOREAN -> "아직 API 키가 없습니다, 우측 상단을 클릭하여 추가"
    }

    val testing: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "测试中..."
        AppLanguage.ENGLISH -> "Testing..."
        AppLanguage.ARABIC -> "جاري الاختبار..."
        AppLanguage.PORTUGUESE -> "Testando..."
        AppLanguage.SPANISH -> "Probando..."
        AppLanguage.FRENCH -> "Test en cours..."
        AppLanguage.GERMAN -> "Testen..."
        AppLanguage.RUSSIAN -> "Тестирование..."
        AppLanguage.JAPANESE -> "テスト中..."
        AppLanguage.KOREAN -> "테스트 중..."
    }

    val connectionSuccess: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "[OK] 连接成功"
        AppLanguage.ENGLISH -> "[OK] Connection successful"
        AppLanguage.ARABIC -> "[OK] الاتصال ناجح"
        AppLanguage.PORTUGUESE -> "[OK] Conexão bem-sucedida"
        AppLanguage.SPANISH -> "[OK] Conexión exitosa"
        AppLanguage.FRENCH -> "[OK] Connexion réussie"
        AppLanguage.GERMAN -> "[OK] Verbindung erfolgreich"
        AppLanguage.RUSSIAN -> "[OK] Подключение успешно"
        AppLanguage.JAPANESE -> "[OK] 接続成功"
        AppLanguage.KOREAN -> "[OK] 연결 성공"
    }
    val connectionFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "[FAIL] 连接失败: %s"
        AppLanguage.ENGLISH -> "[FAIL] Connection failed: %s"
        AppLanguage.ARABIC -> "[FAIL] فشل الاتصال: %s"
        AppLanguage.PORTUGUESE -> "[FAIL] Falha na conexão: %s"
        AppLanguage.SPANISH -> "[FAIL] Error de conexión: %s"
        AppLanguage.FRENCH -> "[FAIL] Échec de connexion : %s"
        AppLanguage.GERMAN -> "[FAIL] Verbindung fehlgeschlagen: %s"
        AppLanguage.RUSSIAN -> "[FAIL] Ошибка подключения: %s"
        AppLanguage.JAPANESE -> "[FAIL] 接続失敗: %s"
        AppLanguage.KOREAN -> "[FAIL] 연결 실패: %s"
    }
    val chineseSeparator: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "、"
        AppLanguage.ENGLISH -> ", "
        AppLanguage.ARABIC -> "، "
        AppLanguage.PORTUGUESE -> ", "
        AppLanguage.SPANISH -> ", "
        AppLanguage.FRENCH -> ", "
        AppLanguage.GERMAN -> ", "
        AppLanguage.RUSSIAN -> ", "
        AppLanguage.JAPANESE -> "、"
        AppLanguage.KOREAN -> ", "
    }

    val test: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "测试"
        AppLanguage.ENGLISH -> "Test"
        AppLanguage.ARABIC -> "اختبار"
        AppLanguage.PORTUGUESE -> "Testar"
        AppLanguage.SPANISH -> "Probar"
        AppLanguage.FRENCH -> "Tester"
        AppLanguage.GERMAN -> "Testen"
        AppLanguage.RUSSIAN -> "Тест"
        AppLanguage.JAPANESE -> "テスト"
        AppLanguage.KOREAN -> "테스트"
    }

    val savedModels: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已保存的模型"
        AppLanguage.ENGLISH -> "Saved Models"
        AppLanguage.ARABIC -> "النماذج المحفوظة"
        AppLanguage.PORTUGUESE -> "Modelos Salvos"
        AppLanguage.SPANISH -> "Modelos Guardados"
        AppLanguage.FRENCH -> "Modèles Enregistrés"
        AppLanguage.GERMAN -> "Gespeicherte Modelle"
        AppLanguage.RUSSIAN -> "Сохранённые модели"
        AppLanguage.JAPANESE -> "保存済みモデル"
        AppLanguage.KOREAN -> "저장된 모델"
    }

    val aiModelCatalog: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "模型目录"
        AppLanguage.ENGLISH -> "Model Catalog"
        AppLanguage.ARABIC -> "كتالوج النماذج"
        AppLanguage.PORTUGUESE -> "Catálogo de Modelos"
        AppLanguage.SPANISH -> "Catálogo de Modelos"
        AppLanguage.FRENCH -> "Catalogue de Modèles"
        AppLanguage.GERMAN -> "Modellkatalog"
        AppLanguage.RUSSIAN -> "Каталог моделей"
        AppLanguage.JAPANESE -> "モデルカタログ"
        AppLanguage.KOREAN -> "모델 카탈로그"
    }
    val aiCatalogSource: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "数据来源：models.dev"
        AppLanguage.ENGLISH -> "Source: models.dev"
        AppLanguage.ARABIC -> "المصدر: models.dev"
        AppLanguage.PORTUGUESE -> "Fonte: models.dev"
        AppLanguage.SPANISH -> "Fuente: models.dev"
        AppLanguage.FRENCH -> "Source : models.dev"
        AppLanguage.GERMAN -> "Quelle: models.dev"
        AppLanguage.RUSSIAN -> "Источник: models.dev"
        AppLanguage.JAPANESE -> "出典：models.dev"
        AppLanguage.KOREAN -> "출처: models.dev"
    }
    val aiCatalogCount: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "%d 个模型"
        AppLanguage.ENGLISH -> "%d models"
        AppLanguage.ARABIC -> "%d نموذج"
        AppLanguage.PORTUGUESE -> "%d modelos"
        AppLanguage.SPANISH -> "%d modelos"
        AppLanguage.FRENCH -> "%d modèles"
        AppLanguage.GERMAN -> "%d Modelle"
        AppLanguage.RUSSIAN -> "%d моделей"
        AppLanguage.JAPANESE -> "%d モデル"
        AppLanguage.KOREAN -> "%d개 모델"
    }
    val aiCatalogRefresh: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "刷新"
        AppLanguage.ENGLISH -> "Refresh"
        AppLanguage.ARABIC -> "تحديث"
        AppLanguage.PORTUGUESE -> "Atualizar"
        AppLanguage.SPANISH -> "Actualizar"
        AppLanguage.FRENCH -> "Actualiser"
        AppLanguage.GERMAN -> "Aktualisieren"
        AppLanguage.RUSSIAN -> "Обновить"
        AppLanguage.JAPANESE -> "更新"
        AppLanguage.KOREAN -> "새로고침"
    }
    val aiCatalogSearchHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "搜索模型…"
        AppLanguage.ENGLISH -> "Search models…"
        AppLanguage.ARABIC -> "البحث عن نماذج…"
        AppLanguage.PORTUGUESE -> "Buscar modelos…"
        AppLanguage.SPANISH -> "Buscar modelos…"
        AppLanguage.FRENCH -> "Rechercher des modèles…"
        AppLanguage.GERMAN -> "Modelle suchen…"
        AppLanguage.RUSSIAN -> "Поиск моделей…"
        AppLanguage.JAPANESE -> "モデルを検索…"
        AppLanguage.KOREAN -> "모델 검색…"
    }
    val aiCatalogAllProviders: String get() = when (Strings.lang) {
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
    val aiCatalogLoading: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "加载目录…"
        AppLanguage.ENGLISH -> "Loading catalog…"
        AppLanguage.ARABIC -> "جارٍ تحميل الكتالوج…"
        AppLanguage.PORTUGUESE -> "Carregando catálogo…"
        AppLanguage.SPANISH -> "Cargando catálogo…"
        AppLanguage.FRENCH -> "Chargement du catalogue…"
        AppLanguage.GERMAN -> "Katalog wird geladen…"
        AppLanguage.RUSSIAN -> "Загрузка каталога…"
        AppLanguage.JAPANESE -> "カタログを読み込み中…"
        AppLanguage.KOREAN -> "카탈로그 로드 중…"
    }
    val aiCatalogEmpty: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "没有匹配的模型。"
        AppLanguage.ENGLISH -> "No models match."
        AppLanguage.ARABIC -> "لا توجد نماذج مطابقة."
        AppLanguage.PORTUGUESE -> "Nenhum modelo corresponde."
        AppLanguage.SPANISH -> "No hay modelos que coincidan."
        AppLanguage.FRENCH -> "Aucun modèle correspondant."
        AppLanguage.GERMAN -> "Keine passenden Modelle."
        AppLanguage.RUSSIAN -> "Нет подходящих моделей."
        AppLanguage.JAPANESE -> "一致するモデルがありません。"
        AppLanguage.KOREAN -> "일치하는 모델이 없습니다."
    }
    val aiCatalogAdd: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "添加"
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
    val aiCatalogAdded: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已添加 %s"
        AppLanguage.ENGLISH -> "Added %s"
        AppLanguage.ARABIC -> "تمت إضافة %s"
        AppLanguage.PORTUGUESE -> "Adicionado %s"
        AppLanguage.SPANISH -> "Añadido %s"
        AppLanguage.FRENCH -> "Ajouté %s"
        AppLanguage.GERMAN -> "Hinzugefügt %s"
        AppLanguage.RUSSIAN -> "Добавлено %s"
        AppLanguage.JAPANESE -> "追加しました %s"
        AppLanguage.KOREAN -> "추가됨 %s"
    }
    val aiCatalogBadgeVision: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "视觉"
        AppLanguage.ENGLISH -> "Vision"
        AppLanguage.ARABIC -> "رؤية"
        AppLanguage.PORTUGUESE -> "Visão"
        AppLanguage.SPANISH -> "Visión"
        AppLanguage.FRENCH -> "Vision"
        AppLanguage.GERMAN -> "Vision"
        AppLanguage.RUSSIAN -> "Зрение"
        AppLanguage.JAPANESE -> "画像認識"
        AppLanguage.KOREAN -> "비전"
    }
    val aiCatalogBadgeReasoning: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "推理"
        AppLanguage.ENGLISH -> "Reasoning"
        AppLanguage.ARABIC -> "استدلال"
        AppLanguage.PORTUGUESE -> "Raciocínio"
        AppLanguage.SPANISH -> "Razonamiento"
        AppLanguage.FRENCH -> "Raisonnement"
        AppLanguage.GERMAN -> "Reasoning"
        AppLanguage.RUSSIAN -> "Рассуждение"
        AppLanguage.JAPANESE -> "推論"
        AppLanguage.KOREAN -> "추론"
    }
    val aiCatalogBadgeToolCall: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "工具"
        AppLanguage.ENGLISH -> "Tools"
        AppLanguage.ARABIC -> "أدوات"
        AppLanguage.PORTUGUESE -> "Ferramentas"
        AppLanguage.SPANISH -> "Herramientas"
        AppLanguage.FRENCH -> "Outils"
        AppLanguage.GERMAN -> "Tools"
        AppLanguage.RUSSIAN -> "Инструменты"
        AppLanguage.JAPANESE -> "ツール"
        AppLanguage.KOREAN -> "도구"
    }
    val aiCatalogBadgeImageGen: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "图像"
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
    val aiCatalogContext: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "%d 上下文"
        AppLanguage.ENGLISH -> "%d ctx"
        AppLanguage.ARABIC -> "%d سياق"
        AppLanguage.PORTUGUESE -> "%d ctx"
        AppLanguage.SPANISH -> "%d ctx"
        AppLanguage.FRENCH -> "%d ctx"
        AppLanguage.GERMAN -> "%d ctx"
        AppLanguage.RUSSIAN -> "%d ctx"
        AppLanguage.JAPANESE -> "%d ctx"
        AppLanguage.KOREAN -> "%d ctx"
    }
    val aiCatalogPrice: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "$%.2f/$%.2f 每百万"
        AppLanguage.ENGLISH -> "$%.2f/$%.2f per M"
        AppLanguage.ARABIC -> "$%.2f/$%.2f لكل مليون"
        AppLanguage.PORTUGUESE -> "$%.2f/$%.2f por M"
        AppLanguage.SPANISH -> "$%.2f/$%.2f por M"
        AppLanguage.FRENCH -> "$%.2f/$%.2f par M"
        AppLanguage.GERMAN -> "$%.2f/$%.2f pro M"
        AppLanguage.RUSSIAN -> "$%.2f/$%.2f за M"
        AppLanguage.JAPANESE -> "$%.2f/$%.2f /M"
        AppLanguage.KOREAN -> "$%.2f/$%.2f /M"
    }
    val aiCatalogAddTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "添加模型"
        AppLanguage.ENGLISH -> "Add model"
        AppLanguage.ARABIC -> "إضافة نموذج"
        AppLanguage.PORTUGUESE -> "Adicionar modelo"
        AppLanguage.SPANISH -> "Añadir modelo"
        AppLanguage.FRENCH -> "Ajouter un modèle"
        AppLanguage.GERMAN -> "Modell hinzufügen"
        AppLanguage.RUSSIAN -> "Добавить модель"
        AppLanguage.JAPANESE -> "モデルを追加"
        AppLanguage.KOREAN -> "모델 추가"
    }
    val aiCatalogPickKey: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择 API 密钥"
        AppLanguage.ENGLISH -> "Choose an API key"
        AppLanguage.ARABIC -> "اختر مفتاح API"
        AppLanguage.PORTUGUESE -> "Escolha uma chave de API"
        AppLanguage.SPANISH -> "Elige una clave de API"
        AppLanguage.FRENCH -> "Choisissez une clé API"
        AppLanguage.GERMAN -> "API-Schlüssel wählen"
        AppLanguage.RUSSIAN -> "Выберите API-ключ"
        AppLanguage.JAPANESE -> "APIキーを選択"
        AppLanguage.KOREAN -> "API 키 선택"
    }
    val aiCatalogAliasHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "别名（可选）"
        AppLanguage.ENGLISH -> "Alias (optional)"
        AppLanguage.ARABIC -> "الاسم المستعار (اختياري)"
        AppLanguage.PORTUGUESE -> "Apelido (opcional)"
        AppLanguage.SPANISH -> "Alias (opcional)"
        AppLanguage.FRENCH -> "Alias (facultatif)"
        AppLanguage.GERMAN -> "Alias (optional)"
        AppLanguage.RUSSIAN -> "Псевдоним (необязательно)"
        AppLanguage.JAPANESE -> "別名（任意）"
        AppLanguage.KOREAN -> "별칭 (선택)"
    }

    val configModelCapabilities: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "配置模型能力标签，用于不同场景"
        AppLanguage.ENGLISH -> "Configure model capability tags for different scenarios"
        AppLanguage.ARABIC -> "تكوين علامات قدرات النموذج لسيناريوهات مختلفة"
        AppLanguage.PORTUGUESE -> "Configurar tags de capacidade do modelo para diferentes cenários"
        AppLanguage.SPANISH -> "Configurar etiquetas de capacidad del modelo para diferentes escenarios"
        AppLanguage.FRENCH -> "Configurer les étiquettes de capacité du modèle pour différents scénarios"
        AppLanguage.GERMAN -> "Modellfähigkeits-Tags für verschiedene Szenarien konfigurieren"
        AppLanguage.RUSSIAN -> "Настройте теги возможностей модели для разных сценариев"
        AppLanguage.JAPANESE -> "異なるシナリオ用のモデル機能タグを設定"
        AppLanguage.KOREAN -> "다양한 시나리오를 위한 모델 기능 태그 구성"
    }

    val pleaseAddApiKeyFirst: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "请先添加 API 密钥"
        AppLanguage.ENGLISH -> "Please add API key first"
        AppLanguage.ARABIC -> "يرجى إضافة مفتاح API أولاً"
        AppLanguage.PORTUGUESE -> "Adicione a chave de API primeiro"
        AppLanguage.SPANISH -> "Agregue la clave de API primero"
        AppLanguage.FRENCH -> "Veuillez d'abord ajouter une clé API"
        AppLanguage.GERMAN -> "Bitte zuerst API-Schlüssel hinzufügen"
        AppLanguage.RUSSIAN -> "Сначала добавьте ключ API"
        AppLanguage.JAPANESE -> "最初にAPIキーを追加してください"
        AppLanguage.KOREAN -> "먼저 API 키를 추가하세요"
    }

    val noSavedModelsHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "暂无已保存的模型，点击右上角添加"
        AppLanguage.ENGLISH -> "No saved models yet, click top right to add"
        AppLanguage.ARABIC -> "لا توجد نماذج محفوظة بعد، انقر في الأعلى للإضافة"
        AppLanguage.PORTUGUESE -> "Nenhum modelo salvo ainda, clique no canto superior direito para adicionar"
        AppLanguage.SPANISH -> "Aún no hay modelos guardados, haga clic arriba a la derecha para agregar"
        AppLanguage.FRENCH -> "Aucun modèle enregistré pour le moment, cliquez en haut à droite pour ajouter"
        AppLanguage.GERMAN -> "Noch keine gespeicherten Modelle, oben rechts klicken zum Hinzufügen"
        AppLanguage.RUSSIAN -> "Сохранённых моделей пока нет, нажмите вверху справа, чтобы добавить"
        AppLanguage.JAPANESE -> "保存済みモデルがまだありません、右上をクリックして追加"
        AppLanguage.KOREAN -> "저장된 모델이 아직 없습니다, 우측 상단을 클릭하여 추가"
    }

    val defaultLabel: String get() = when (Strings.lang) {
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

    val setAsDefault: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "设为默认"
        AppLanguage.ENGLISH -> "Set as Default"
        AppLanguage.ARABIC -> "تعيين كافتراضي"
        AppLanguage.PORTUGUESE -> "Definir como Padrão"
        AppLanguage.SPANISH -> "Establecer como Predeterminado"
        AppLanguage.FRENCH -> "Définir par Défaut"
        AppLanguage.GERMAN -> "Als Standard festlegen"
        AppLanguage.RUSSIAN -> "Установить по умолчанию"
        AppLanguage.JAPANESE -> "デフォルトに設定"
        AppLanguage.KOREAN -> "기본값으로 설정"
    }

    val editApiKey: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "编辑 API 密钥"
        AppLanguage.ENGLISH -> "Edit API Key"
        AppLanguage.ARABIC -> "تعديل مفتاح API"
        AppLanguage.PORTUGUESE -> "Editar Chave de API"
        AppLanguage.SPANISH -> "Editar Clave de API"
        AppLanguage.FRENCH -> "Modifier la Clé API"
        AppLanguage.GERMAN -> "API-Schlüssel bearbeiten"
        AppLanguage.RUSSIAN -> "Изменить ключ API"
        AppLanguage.JAPANESE -> "APIキーを編集"
        AppLanguage.KOREAN -> "API 키 편집"
    }

    val addApiKey: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "添加 API 密钥"
        AppLanguage.ENGLISH -> "Add API Key"
        AppLanguage.ARABIC -> "إضافة مفتاح API"
        AppLanguage.PORTUGUESE -> "Adicionar Chave de API"
        AppLanguage.SPANISH -> "Agregar Clave de API"
        AppLanguage.FRENCH -> "Ajouter une Clé API"
        AppLanguage.GERMAN -> "API-Schlüssel hinzufügen"
        AppLanguage.RUSSIAN -> "Добавить ключ API"
        AppLanguage.JAPANESE -> "APIキーを追加"
        AppLanguage.KOREAN -> "API 키 추가"
    }

    val getApiKey: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "获取 API Key"
        AppLanguage.ENGLISH -> "Get API Key"
        AppLanguage.ARABIC -> "الحصول على مفتاح API"
        AppLanguage.PORTUGUESE -> "Obter Chave de API"
        AppLanguage.SPANISH -> "Obtener Clave de API"
        AppLanguage.FRENCH -> "Obtenir une Clé API"
        AppLanguage.GERMAN -> "API-Schlüssel erhalten"
        AppLanguage.RUSSIAN -> "Получить ключ API"
        AppLanguage.JAPANESE -> "APIキーを取得"
        AppLanguage.KOREAN -> "API 키 받기"
    }

    val customBaseUrlHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "API 根地址，不含 /v1 等路径"
        AppLanguage.ENGLISH -> "API base URL, without the /v1 path"
        AppLanguage.ARABIC -> "عنوان URL الأساسي لواجهة برمجة التطبيقات، بدون المسار /v1"
        AppLanguage.PORTUGUESE -> "URL base da API, sem o caminho /v1"
        AppLanguage.SPANISH -> "URL base de la API, sin la ruta /v1"
        AppLanguage.FRENCH -> "URL de base de l'API, sans le chemin /v1"
        AppLanguage.GERMAN -> "API-Basis-URL ohne den /v1-Pfad"
        AppLanguage.RUSSIAN -> "Базовый URL API без пути /v1"
        AppLanguage.JAPANESE -> "APIのベースURL（/v1 などのパスは含めない）"
        AppLanguage.KOREAN -> "API 기본 URL, /v1 경로 제외"
    }

    fun chatEndpointDefaultHint(defaultEndpoint: String): String = when (Strings.lang) {
        AppLanguage.CHINESE -> "默认：$defaultEndpoint"
        AppLanguage.ENGLISH -> "Default: $defaultEndpoint"
        AppLanguage.ARABIC -> "الافتراضي: $defaultEndpoint"
        AppLanguage.PORTUGUESE -> "Padrão: $defaultEndpoint"
        AppLanguage.SPANISH -> "Predeterminado: $defaultEndpoint"
        AppLanguage.FRENCH -> "Par défaut : $defaultEndpoint"
        AppLanguage.GERMAN -> "Standard: $defaultEndpoint"
        AppLanguage.RUSSIAN -> "По умолчанию: $defaultEndpoint"
        AppLanguage.JAPANESE -> "デフォルト: $defaultEndpoint"
        AppLanguage.KOREAN -> "기본값: $defaultEndpoint"
    }

    val apiFormat: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "API 格式"
        AppLanguage.ENGLISH -> "API Format"
        AppLanguage.ARABIC -> "تنسيق API"
        AppLanguage.PORTUGUESE -> "Formato de API"
        AppLanguage.SPANISH -> "Formato de API"
        AppLanguage.FRENCH -> "Format d'API"
        AppLanguage.GERMAN -> "API-Format"
        AppLanguage.RUSSIAN -> "Формат API"
        AppLanguage.JAPANESE -> "APIフォーマット"
        AppLanguage.KOREAN -> "API 형식"
    }

    val apiKeyAliasPlaceholder: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "例如：我的 GPT-4 Key"
        AppLanguage.ENGLISH -> "e.g. My GPT-4 Key"
        AppLanguage.ARABIC -> "مثال: مفتاح GPT-4 الخاص بي"
        AppLanguage.PORTUGUESE -> "ex. Minha Chave GPT-4"
        AppLanguage.SPANISH -> "ej. Mi Clave GPT-4"
        AppLanguage.FRENCH -> "ex. Ma Clé GPT-4"
        AppLanguage.GERMAN -> "z.B. Mein GPT-4-Schlüssel"
        AppLanguage.RUSSIAN -> "напр. Мой ключ GPT-4"
        AppLanguage.JAPANESE -> "例: 私のGPT-4キー"
        AppLanguage.KOREAN -> "예: 내 GPT-4 키"
    }

    val apiKeyMultiHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "多把密钥用英文逗号或换行隔开。某一把被限额后会自动换下一把，冷却结束再回来。同一个 Google 项目里的密钥共用额度。"
        AppLanguage.ENGLISH -> "Separate extra keys with a comma or a new line. A rate-limited key steps aside until its cooldown ends. Keys from the same Google project share one quota."
        AppLanguage.ARABIC -> "افصل المفاتيح الإضافية بفاصلة أو سطر جديد. المفتاح الذي بلغ الحد يتنحى حتى تنتهي فترة التبريد. مفاتيح مشروع Google الواحد تشترك في الحصة."
        AppLanguage.PORTUGUESE -> "Separe chaves extras com vírgula ou uma nova linha. A chave limitada sai de cena até o cooldown acabar. Chaves do mesmo projeto Google compartilham a cota."
        AppLanguage.SPANISH -> "Separa las claves extra con una coma o un salto de línea. La clave limitada se aparta hasta que termina la pausa. Las claves del mismo proyecto de Google comparten la cuota."
        AppLanguage.FRENCH -> "Séparez les clés supplémentaires par une virgule ou un retour à la ligne. Une clé limitée s'efface jusqu'à la fin du délai. Les clés d'un même projet Google partagent le quota."
        AppLanguage.GERMAN -> "Trenne weitere Schlüssel mit Komma oder Zeilenumbruch. Ein begrenzter Schlüssel pausiert bis zum Ende der Sperre. Schlüssel desselben Google-Projekts teilen sich das Kontingent."
        AppLanguage.RUSSIAN -> "Дополнительные ключи разделяйте запятой или новой строкой. Ключ на лимите отходит в сторону, пока не кончится пауза. Ключи одного проекта Google делят одну квоту."
        AppLanguage.JAPANESE -> "追加のキーはカンマか改行で区切ります。制限に達したキーは冷却が終わるまで休み、その後戻ります。同じ Google プロジェクトのキーは枠を共有します。"
        AppLanguage.KOREAN -> "키를 더 넣을 때는 쉼표나 줄바꿈으로 구분하세요. 한도에 걸린 키는 냉각이 끝날 때까지 빠졌다가 돌아옵니다. 같은 Google 프로젝트의 키는 할당량을 같이 씁니다."
    }

    val modelsEndpoint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "模型列表端点"
        AppLanguage.ENGLISH -> "Models Endpoint"
        AppLanguage.ARABIC -> "نقطة نهاية النماذج"
        AppLanguage.PORTUGUESE -> "Endpoint de Modelos"
        AppLanguage.SPANISH -> "Endpoint de Modelos"
        AppLanguage.FRENCH -> "Endpoint des Modèles"
        AppLanguage.GERMAN -> "Modelle-Endpoint"
        AppLanguage.RUSSIAN -> "Endpoint моделей"
        AppLanguage.JAPANESE -> "モデルエンドポイント"
        AppLanguage.KOREAN -> "모델 엔드포인트"
    }

    val modelsEndpointHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "默认: /v1/models"
        AppLanguage.ENGLISH -> "Default: /v1/models"
        AppLanguage.ARABIC -> "الافتراضي: /v1/models"
        AppLanguage.PORTUGUESE -> "Padrão: /v1/models"
        AppLanguage.SPANISH -> "Predeterminado: /v1/models"
        AppLanguage.FRENCH -> "Par défaut : /v1/models"
        AppLanguage.GERMAN -> "Standard: /v1/models"
        AppLanguage.RUSSIAN -> "По умолчанию: /v1/models"
        AppLanguage.JAPANESE -> "デフォルト: /v1/models"
        AppLanguage.KOREAN -> "기본값: /v1/models"
    }

    val chatEndpoint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "聊天端点"
        AppLanguage.ENGLISH -> "Chat Endpoint"
        AppLanguage.ARABIC -> "نقطة نهاية الدردشة"
        AppLanguage.PORTUGUESE -> "Endpoint de Chat"
        AppLanguage.SPANISH -> "Endpoint de Chat"
        AppLanguage.FRENCH -> "Endpoint de Chat"
        AppLanguage.GERMAN -> "Chat-Endpoint"
        AppLanguage.RUSSIAN -> "Endpoint чата"
        AppLanguage.JAPANESE -> "チャットエンドポイント"
        AppLanguage.KOREAN -> "채팅 엔드포인트"
    }

    val selectApiKey: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择 API Key"
        AppLanguage.ENGLISH -> "Select API Key"
        AppLanguage.ARABIC -> "اختر مفتاح API"
        AppLanguage.PORTUGUESE -> "Selecionar Chave de API"
        AppLanguage.SPANISH -> "Seleccionar Clave de API"
        AppLanguage.FRENCH -> "Sélectionner la Clé API"
        AppLanguage.GERMAN -> "API-Schlüssel auswählen"
        AppLanguage.RUSSIAN -> "Выбрать ключ API"
        AppLanguage.JAPANESE -> "APIキーを選択"
        AppLanguage.KOREAN -> "API 키 선택"
    }

    val batchSelectModels: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "批量选择模型"
        AppLanguage.ENGLISH -> "Batch Select Models"
        AppLanguage.ARABIC -> "تحديد النماذج دفعة واحدة"
        AppLanguage.PORTUGUESE -> "Selecionar Modelos em Lote"
        AppLanguage.SPANISH -> "Seleccionar Modelos en Lote"
        AppLanguage.FRENCH -> "Sélectionner des Modèles en Lot"
        AppLanguage.GERMAN -> "Modelle stapelweise auswählen"
        AppLanguage.RUSSIAN -> "Пакетный выбор моделей"
        AppLanguage.JAPANESE -> "モデルを一括選択"
        AppLanguage.KOREAN -> "모델 일괄 선택"
    }

    val selectedModelsCount: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已选择 %d 个模型"
        AppLanguage.ENGLISH -> "%d models selected"
        AppLanguage.ARABIC -> "تم تحديد %d نماذج"
        AppLanguage.PORTUGUESE -> "%d modelos selecionados"
        AppLanguage.SPANISH -> "%d modelos seleccionados"
        AppLanguage.FRENCH -> "%d modèles sélectionnés"
        AppLanguage.GERMAN -> "%d Modelle ausgewählt"
        AppLanguage.RUSSIAN -> "%d моделей выбрано"
        AppLanguage.JAPANESE -> "%d 個のモデルを選択済み"
        AppLanguage.KOREAN -> "%d개 모델 선택됨"
    }

    val addSelectedModels: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "添加所选模型"
        AppLanguage.ENGLISH -> "Add Selected Models"
        AppLanguage.ARABIC -> "إضافة النماذج المحددة"
        AppLanguage.PORTUGUESE -> "Adicionar Modelos Selecionados"
        AppLanguage.SPANISH -> "Añadir Modelos Seleccionados"
        AppLanguage.FRENCH -> "Ajouter les Modèles Sélectionnés"
        AppLanguage.GERMAN -> "Ausgewählte Modelle hinzufügen"
        AppLanguage.RUSSIAN -> "Добавить выбранные модели"
        AppLanguage.JAPANESE -> "選択したモデルを追加"
        AppLanguage.KOREAN -> "선택한 모델 추가"
    }

    val searchModels: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "搜索模型名称或 ID..."
        AppLanguage.ENGLISH -> "Search model name or ID..."
        AppLanguage.ARABIC -> "ابحث عن اسم النموذج أو المعرف..."
        AppLanguage.PORTUGUESE -> "Pesquisar nome ou ID do modelo..."
        AppLanguage.SPANISH -> "Buscar nombre o ID del modelo..."
        AppLanguage.FRENCH -> "Rechercher le nom ou l'ID du modèle..."
        AppLanguage.GERMAN -> "Modellname oder -ID suchen..."
        AppLanguage.RUSSIAN -> "Поиск по имени или ID модели..."
        AppLanguage.JAPANESE -> "モデル名またはIDを検索..."
        AppLanguage.KOREAN -> "모델 이름 또는 ID 검색..."
    }

    val noSearchResults: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "没有找到匹配的模型"
        AppLanguage.ENGLISH -> "No matching models found"
        AppLanguage.ARABIC -> "لم يتم العثور على نماذج مطابقة"
        AppLanguage.PORTUGUESE -> "Nenhum modelo correspondente encontrado"
        AppLanguage.SPANISH -> "No se encontraron modelos coincidentes"
        AppLanguage.FRENCH -> "Aucun modèle correspondant trouvé"
        AppLanguage.GERMAN -> "Keine passenden Modelle gefunden"
        AppLanguage.RUSSIAN -> "Подходящие модели не найдены"
        AppLanguage.JAPANESE -> "一致するモデルが見つかりません"
        AppLanguage.KOREAN -> "일치하는 모델을 찾을 수 없습니다"
    }

    val addModel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "添加模型"
        AppLanguage.ENGLISH -> "Add Model"
        AppLanguage.ARABIC -> "إضافة نموذج"
        AppLanguage.PORTUGUESE -> "Adicionar Modelo"
        AppLanguage.SPANISH -> "Añadir Modelo"
        AppLanguage.FRENCH -> "Ajouter un Modèle"
        AppLanguage.GERMAN -> "Modell hinzufügen"
        AppLanguage.RUSSIAN -> "Добавить модель"
        AppLanguage.JAPANESE -> "モデルを追加"
        AppLanguage.KOREAN -> "모델 추가"
    }

    val orManualInputModelId: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "或手动输入模型 ID"
        AppLanguage.ENGLISH -> "Or manually input model ID"
        AppLanguage.ARABIC -> "أو أدخل معرف النموذج يدويًا"
        AppLanguage.PORTUGUESE -> "Ou insira o ID do modelo manualmente"
        AppLanguage.SPANISH -> "O introduzca el ID del modelo manualmente"
        AppLanguage.FRENCH -> "Ou saisir l'ID du modèle manuellement"
        AppLanguage.GERMAN -> "Oder Modell-ID manuell eingeben"
        AppLanguage.RUSSIAN -> "Или введите ID модели вручную"
        AppLanguage.JAPANESE -> "またはモデルIDを手動入力"
        AppLanguage.KOREAN -> "또는 모델 ID를 수동으로 입력"
    }

    val modelIdPlaceholder: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "例如: gpt-4o-mini"
        AppLanguage.ENGLISH -> "e.g. gpt-4o-mini"
        AppLanguage.ARABIC -> "مثال: gpt-4o-mini"
        AppLanguage.PORTUGUESE -> "ex.: gpt-4o-mini"
        AppLanguage.SPANISH -> "ej.: gpt-4o-mini"
        AppLanguage.FRENCH -> "ex. : gpt-4o-mini"
        AppLanguage.GERMAN -> "z. B. gpt-4o-mini"
        AppLanguage.RUSSIAN -> "напр.: gpt-4o-mini"
        AppLanguage.JAPANESE -> "例: gpt-4o-mini"
        AppLanguage.KOREAN -> "예: gpt-4o-mini"
    }

    val editModel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "编辑模型"
        AppLanguage.ENGLISH -> "Edit Model"
        AppLanguage.ARABIC -> "تعديل النموذج"
        AppLanguage.PORTUGUESE -> "Editar Modelo"
        AppLanguage.SPANISH -> "Editar Modelo"
        AppLanguage.FRENCH -> "Modifier le Modèle"
        AppLanguage.GERMAN -> "Modell bearbeiten"
        AppLanguage.RUSSIAN -> "Изменить модель"
        AppLanguage.JAPANESE -> "モデルを編集"
        AppLanguage.KOREAN -> "모델 편집"
    }

    val bgmTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "背景音乐"
        AppLanguage.ENGLISH -> "Background Music"
        AppLanguage.ARABIC -> "موسيقى الخلفية"
        AppLanguage.PORTUGUESE -> "Música de Fundo"
        AppLanguage.SPANISH -> "Música de Fondo"
        AppLanguage.FRENCH -> "Musique de Fond"
        AppLanguage.GERMAN -> "Hintergrundmusik"
        AppLanguage.RUSSIAN -> "Фоновая музыка"
        AppLanguage.JAPANESE -> "背景音楽"
        AppLanguage.KOREAN -> "배경 음악"
    }

    val andMoreTracks: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "还有 %d 首..."
        AppLanguage.ENGLISH -> "and %d more..."
        AppLanguage.ARABIC -> "و %d أخرى..."
        AppLanguage.PORTUGUESE -> "e mais %d..."
        AppLanguage.SPANISH -> "y %d más..."
        AppLanguage.FRENCH -> "et %d de plus..."
        AppLanguage.GERMAN -> "und %d weitere..."
        AppLanguage.RUSSIAN -> "и ещё %d..."
        AppLanguage.JAPANESE -> "あと %d 曲..."
        AppLanguage.KOREAN -> "외 %d곡..."
    }

    val bgmLibraryTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "音乐库"
        AppLanguage.ENGLISH -> "Music Library"
        AppLanguage.ARABIC -> "مكتبة الموسيقى"
        AppLanguage.PORTUGUESE -> "Biblioteca de Música"
        AppLanguage.SPANISH -> "Biblioteca de Música"
        AppLanguage.FRENCH -> "Bibliothèque Musicale"
        AppLanguage.GERMAN -> "Musikbibliothek"
        AppLanguage.RUSSIAN -> "Медиатека"
        AppLanguage.JAPANESE -> "ミュージックライブラリ"
        AppLanguage.KOREAN -> "음악 라이브러리"
    }

    val bgmTrackCount: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "%d 首"
        AppLanguage.ENGLISH -> "%d tracks"
        AppLanguage.ARABIC -> "%d مقاطع"
        AppLanguage.PORTUGUESE -> "%d faixas"
        AppLanguage.SPANISH -> "%d pistas"
        AppLanguage.FRENCH -> "%d morceaux"
        AppLanguage.GERMAN -> "%d Titel"
        AppLanguage.RUSSIAN -> "%d треков"
        AppLanguage.JAPANESE -> "%d 曲"
        AppLanguage.KOREAN -> "%d곡"
    }

    val moreOptions: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "更多选项"
        AppLanguage.ENGLISH -> "More options"
        AppLanguage.ARABIC -> "خيارات أكثر"
        AppLanguage.PORTUGUESE -> "Mais opções"
        AppLanguage.SPANISH -> "Más opciones"
        AppLanguage.FRENCH -> "Plus d'options"
        AppLanguage.GERMAN -> "Mehr Optionen"
        AppLanguage.RUSSIAN -> "Ещё"
        AppLanguage.JAPANESE -> "その他のオプション"
        AppLanguage.KOREAN -> "더 많은 옵션"
    }

    val sampleProjects: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "示例项目"
        AppLanguage.ENGLISH -> "Sample Projects"
        AppLanguage.ARABIC -> "مشاريع نموذجية"
        AppLanguage.PORTUGUESE -> "Projetos de Exemplo"
        AppLanguage.SPANISH -> "Proyectos de Ejemplo"
        AppLanguage.FRENCH -> "Projets d'Exemple"
        AppLanguage.GERMAN -> "Beispielprojekte"
        AppLanguage.RUSSIAN -> "Примеры проектов"
        AppLanguage.JAPANESE -> "サンプルプロジェクト"
        AppLanguage.KOREAN -> "샘플 프로젝트"
    }

    val quickExperienceFrontend: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "一键导入前端示例"
        AppLanguage.ENGLISH -> "Import a frontend sample to try"
        AppLanguage.ARABIC -> "استيراد مثال واجهة أمامية"
        AppLanguage.PORTUGUESE -> "Importe um exemplo frontend"
        AppLanguage.SPANISH -> "Importa un ejemplo frontend"
        AppLanguage.FRENCH -> "Importez un exemple frontend"
        AppLanguage.GERMAN -> "Frontend-Beispiel importieren"
        AppLanguage.RUSSIAN -> "Импорт фронтенд-примера"
        AppLanguage.JAPANESE -> "フロントエンドサンプルをインポート"
        AppLanguage.KOREAN -> "프론트엔드 샘플 가져오기"
    }

    val quickExperience: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "快速体验"
        AppLanguage.ENGLISH -> "Try Now"
        AppLanguage.ARABIC -> "تجربة"
        AppLanguage.PORTUGUESE -> "Testar"
        AppLanguage.SPANISH -> "Probar"
        AppLanguage.FRENCH -> "Essayer"
        AppLanguage.GERMAN -> "Testen"
        AppLanguage.RUSSIAN -> "Демо"
        AppLanguage.JAPANESE -> "体験"
        AppLanguage.KOREAN -> "체험"
    }

    val run: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "运行"
        AppLanguage.ENGLISH -> "Run"
        AppLanguage.ARABIC -> "تشغيل"
        AppLanguage.PORTUGUESE -> "Executar"
        AppLanguage.SPANISH -> "Ejecutar"
        AppLanguage.FRENCH -> "Exécuter"
        AppLanguage.GERMAN -> "Ausführen"
        AppLanguage.RUSSIAN -> "Запустить"
        AppLanguage.JAPANESE -> "実行"
        AppLanguage.KOREAN -> "실행"
    }

    val cannotParseImage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "无法解析图片"
        AppLanguage.ENGLISH -> "Cannot parse image"
        AppLanguage.ARABIC -> "لا يمكن تحليل الصورة"
        AppLanguage.PORTUGUESE -> "Não foi possível analisar a imagem"
        AppLanguage.SPANISH -> "No se puede analizar la imagen"
        AppLanguage.FRENCH -> "Impossible d'analyser l'image"
        AppLanguage.GERMAN -> "Bild kann nicht analysiert werden"
        AppLanguage.RUSSIAN -> "Не удалось разобрать изображение"
        AppLanguage.JAPANESE -> "画像を解析できません"
        AppLanguage.KOREAN -> "이미지를 분석할 수 없습니다"
    }

    val cannotOpenImage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "无法打开图片"
        AppLanguage.ENGLISH -> "Cannot open image"
        AppLanguage.ARABIC -> "لا يمكن فتح الصورة"
        AppLanguage.PORTUGUESE -> "Não foi possível abrir a imagem"
        AppLanguage.SPANISH -> "No se puede abrir la imagen"
        AppLanguage.FRENCH -> "Impossible d'ouvrir l'image"
        AppLanguage.GERMAN -> "Bild kann nicht geöffnet werden"
        AppLanguage.RUSSIAN -> "Не удалось открыть изображение"
        AppLanguage.JAPANESE -> "画像を開けません"
        AppLanguage.KOREAN -> "이미지를 열 수 없습니다"
    }

    val loadImageFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "加载图片失败: %s"
        AppLanguage.ENGLISH -> "Failed to load image: %s"
        AppLanguage.ARABIC -> "فشل تحميل الصورة: %s"
        AppLanguage.PORTUGUESE -> "Falha ao carregar imagem: %s"
        AppLanguage.SPANISH -> "Error al cargar imagen: %s"
        AppLanguage.FRENCH -> "Échec du chargement de l'image : %s"
        AppLanguage.GERMAN -> "Bild laden fehlgeschlagen: %s"
        AppLanguage.RUSSIAN -> "Не удалось загрузить изображение: %s"
        AppLanguage.JAPANESE -> "画像の読み込みに失敗: %s"
        AppLanguage.KOREAN -> "이미지 로드 실패: %s"
    }

    val originalImage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "原始图片"
        AppLanguage.ENGLISH -> "Original Image"
        AppLanguage.ARABIC -> "الصورة الأصلية"
        AppLanguage.PORTUGUESE -> "Imagem Original"
        AppLanguage.SPANISH -> "Imagen Original"
        AppLanguage.FRENCH -> "Image Originale"
        AppLanguage.GERMAN -> "Originalbild"
        AppLanguage.RUSSIAN -> "Оригинальное изображение"
        AppLanguage.JAPANESE -> "元の画像"
        AppLanguage.KOREAN -> "원본 이미지"
    }

    val videoFileNotExist: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "视频文件不存在"
        AppLanguage.ENGLISH -> "Video file does not exist"
        AppLanguage.ARABIC -> "ملف الفيديو غير موجود"
        AppLanguage.PORTUGUESE -> "Arquivo de vídeo não existe"
        AppLanguage.SPANISH -> "El archivo de vídeo no existe"
        AppLanguage.FRENCH -> "Le fichier vidéo n'existe pas"
        AppLanguage.GERMAN -> "Videodatei existiert nicht"
        AppLanguage.RUSSIAN -> "Видеофайл не существует"
        AppLanguage.JAPANESE -> "動画ファイルが存在しません"
        AppLanguage.KOREAN -> "동영상 파일이 존재하지 않습니다"
    }

    val videoPreview: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "视频预览"
        AppLanguage.ENGLISH -> "Video Preview"
        AppLanguage.ARABIC -> "معاينة الفيديو"
        AppLanguage.PORTUGUESE -> "Pré-visualização do Vídeo"
        AppLanguage.SPANISH -> "Vista Previa del Vídeo"
        AppLanguage.FRENCH -> "Aperçu de la Vidéo"
        AppLanguage.GERMAN -> "Videovorschau"
        AppLanguage.RUSSIAN -> "Предпросмотр видео"
        AppLanguage.JAPANESE -> "動画プレビュー"
        AppLanguage.KOREAN -> "동영상 미리보기"
    }

    val selectedDuration: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已选：%.1f 秒"
        AppLanguage.ENGLISH -> "Selected: %.1f seconds"
        AppLanguage.ARABIC -> "المحدد: %.1f ثانية"
        AppLanguage.PORTUGUESE -> "Selecionado: %.1f segundos"
        AppLanguage.SPANISH -> "Seleccionado: %.1f segundos"
        AppLanguage.FRENCH -> "Sélectionné : %.1f secondes"
        AppLanguage.GERMAN -> "Ausgewählt: %.1f Sekunden"
        AppLanguage.RUSSIAN -> "Выбрано: %.1f сек."
        AppLanguage.JAPANESE -> "選択中: %.1f 秒"
        AppLanguage.KOREAN -> "선택됨: %.1f초"
    }

    val totalDuration: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "总时长：%.1f 秒"
        AppLanguage.ENGLISH -> "Total duration: %.1f seconds"
        AppLanguage.ARABIC -> "المدة الإجمالية: %.1f ثانية"
        AppLanguage.PORTUGUESE -> "Duração total: %.1f segundos"
        AppLanguage.SPANISH -> "Duración total: %.1f segundos"
        AppLanguage.FRENCH -> "Durée totale : %.1f secondes"
        AppLanguage.GERMAN -> "Gesamtdauer: %.1f Sekunden"
        AppLanguage.RUSSIAN -> "Общая длительность: %.1f сек."
        AppLanguage.JAPANESE -> "合計時間: %.1f 秒"
        AppLanguage.KOREAN -> "총 시간: %.1f초"
    }

    val trimRangeHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "拖动选择裁剪范围"
        AppLanguage.ENGLISH -> "Trim range (drag to select playback segment)"
        AppLanguage.ARABIC -> "نطاق القص (اسحب لتحديد مقطع التشغيل)"
        AppLanguage.PORTUGUESE -> "Faixa de corte (arraste para selecionar o segmento de reprodução)"
        AppLanguage.SPANISH -> "Rango de recorte (arrastra para seleccionar el segmento de reproducción)"
        AppLanguage.FRENCH -> "Plage de découpe (glisser pour sélectionner le segment de lecture)"
        AppLanguage.GERMAN -> "Zuschneidebereich (ziehen, um Wiedergabeabschnitt auszuwählen)"
        AppLanguage.RUSSIAN -> "Диапазон обрезки (перетащите, чтобы выбрать сегмент воспроизведения)"
        AppLanguage.JAPANESE -> "トリム範囲（ドラッグして再生区間を選択）"
        AppLanguage.KOREAN -> "트림 범위 (드래그하여 재생 구간 선택)"
    }

    val apkExportConfig: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "APK 导出配置"
        AppLanguage.ENGLISH -> "APK Export Config"
        AppLanguage.ARABIC -> "إعدادات تصدير APK"
        AppLanguage.PORTUGUESE -> "Configuração de Exportação APK"
        AppLanguage.SPANISH -> "Configuración de Exportación APK"
        AppLanguage.FRENCH -> "Configuration d'Exportation APK"
        AppLanguage.GERMAN -> "APK-Exportkonfiguration"
        AppLanguage.RUSSIAN -> "Конфигурация экспорта APK"
        AppLanguage.JAPANESE -> "APKエクスポート設定"
        AppLanguage.KOREAN -> "APK 내보내기 구성"
    }

    val apkIdentityBuildSection: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "应用标识"
        AppLanguage.ENGLISH -> "App Identity"
        AppLanguage.ARABIC -> "هوية التطبيق"
        AppLanguage.PORTUGUESE -> "Identidade do App"
        AppLanguage.SPANISH -> "Identidad de la app"
        AppLanguage.FRENCH -> "Identité de l'app"
        AppLanguage.GERMAN -> "App-Identität"
        AppLanguage.RUSSIAN -> "Идентификатор приложения"
        AppLanguage.JAPANESE -> "アプリ識別"
        AppLanguage.KOREAN -> "앱 식별"
    }

    val apkBuildOptionsSection: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "构建选项"
        AppLanguage.ENGLISH -> "Build Options"
        AppLanguage.ARABIC -> "خيارات البناء"
        AppLanguage.PORTUGUESE -> "Opções de Build"
        AppLanguage.SPANISH -> "Opciones de compilación"
        AppLanguage.FRENCH -> "Options de compilation"
        AppLanguage.GERMAN -> "Build-Optionen"
        AppLanguage.RUSSIAN -> "Параметры сборки"
        AppLanguage.JAPANESE -> "ビルド設定"
        AppLanguage.KOREAN -> "빌드 옵션"
    }

    val signingSectionTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "签名"
        AppLanguage.ENGLISH -> "Signing"
        AppLanguage.ARABIC -> "التوقيع"
        AppLanguage.PORTUGUESE -> "Assinatura"
        AppLanguage.SPANISH -> "Firma"
        AppLanguage.FRENCH -> "Signature"
        AppLanguage.GERMAN -> "Signierung"
        AppLanguage.RUSSIAN -> "Подпись"
        AppLanguage.JAPANESE -> "署名"
        AppLanguage.KOREAN -> "서명"
    }

    val apkArchitecture: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "APK 架构"
        AppLanguage.ENGLISH -> "APK Architecture"
        AppLanguage.ARABIC -> "بنية APK"
        AppLanguage.PORTUGUESE -> "Arquitetura do APK"
        AppLanguage.SPANISH -> "Arquitectura del APK"
        AppLanguage.FRENCH -> "Architecture de l'APK"
        AppLanguage.GERMAN -> "APK-Architektur"
        AppLanguage.RUSSIAN -> "Архитектура APK"
        AppLanguage.JAPANESE -> "APKアーキテクチャ"
        AppLanguage.KOREAN -> "APK 아키텍처"
    }

    val apkLoggingTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "诊断日志"
        AppLanguage.ENGLISH -> "Diagnostic Logging"
        AppLanguage.ARABIC -> "سجل التشخيص"
        AppLanguage.PORTUGUESE -> "Registro de Diagnóstico"
        AppLanguage.SPANISH -> "Registro de Diagnóstico"
        AppLanguage.FRENCH -> "Journalisation de Diagnostic"
        AppLanguage.GERMAN -> "Diagnoseprotokollierung"
        AppLanguage.RUSSIAN -> "Журнал диагностики"
        AppLanguage.JAPANESE -> "診断ログ"
        AppLanguage.KOREAN -> "진단 로깅"
    }

    val apkLoggingHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "开启后，生成的应用会把运行日志写入其数据目录，便于排查问题；关闭则不产生任何日志文件。默认关闭。"
        AppLanguage.ENGLISH -> "When enabled, the generated app writes runtime logs to its data directory for troubleshooting; when off, no log files are created. Off by default."
        AppLanguage.ARABIC -> "عند التفعيل، يكتب التطبيق المُنشأ سجلات التشغيل في دليل بياناته لتسهيل تتبع المشكلات؛ وعند الإيقاف لا يتم إنشاء أي ملفات سجل. مُعطّل افتراضيًا."
        AppLanguage.PORTUGUESE -> "Quando ativado, o app gerado grava logs de execução em seu diretório de dados para solução de problemas; quando desativado, nenhum arquivo de log é criado. Desativado por padrão."
        AppLanguage.SPANISH -> "Cuando está activado, la app generada escribe registros de ejecución en su directorio de datos para resolución de problemas; cuando está desactivado, no se crea ningún archivo de registro. Desactivado por defecto."
        AppLanguage.FRENCH -> "Lorsqu'activé, l'application générée écrit les journaux d'exécution dans son répertoire de données pour le dépannage ; lorsqu'inactif, aucun fichier journal n'est créé. Inactif par défaut."
        AppLanguage.GERMAN -> "Wenn aktiviert, schreibt die generierte App Laufzeitprotokolle in ihr Datenverzeichnis zur Fehlerbehebung; wenn deaktiviert, werden keine Protokolldateien erstellt. Standardmäßig deaktiviert."
        AppLanguage.RUSSIAN -> "Если включено, сгенерированное приложение записывает журналы выполнения в свой каталог данных для устранения неполадок; если выключено, файлы журнала не создаются. По умолчанию выключено."
        AppLanguage.JAPANESE -> "有効にすると、生成されたアプリはトラブルシューティングのために実行ログを自身のデータディレクトリに書き込みます。無効時はログファイルは作成されません。デフォルトは無効です。"
        AppLanguage.KOREAN -> "활성화하면 생성된 앱이 문제 해결을 위해 런타임 로그를 자신의 데이터 디렉터리에 기록합니다. 비활성화 시 로그 파일이 생성되지 않습니다. 기본값은 비활성화입니다."
    }

    val targetSdkOverrideTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自定义 targetSdk"
        AppLanguage.ENGLISH -> "Override targetSdk"
        AppLanguage.ARABIC -> "تجاوز targetSdk"
        AppLanguage.PORTUGUESE -> "Substituir targetSdk"
        AppLanguage.SPANISH -> "Sobrescribir targetSdk"
        AppLanguage.FRENCH -> "Remplacer targetSdk"
        AppLanguage.GERMAN -> "targetSdk überschreiben"
        AppLanguage.RUSSIAN -> "Изменить targetSdk"
        AppLanguage.JAPANESE -> "targetSdk を上書き"
        AppLanguage.KOREAN -> "targetSdk 재정의"
    }

    val targetSdkOverrideOffHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "默认 targetSdk 35（Android 15）。开启后可为生成的 APK 固定其他 API 级别，用于兼容旧设备行为或满足渠道要求。上架 Google Play 请改用「更多 → Google Play」的 AAB 导出。"
        AppLanguage.ENGLISH -> "Defaults to targetSdk 35 (Android 15). Turn on to pin a different API level for the generated APK — useful for legacy behavior or channel requirements. For Google Play use the AAB export under More → Google Play."
        AppLanguage.ARABIC -> "الافتراضي targetSdk 35 (Android 15). فعّل هذا لتثبيت مستوى API مختلف للتطبيق المُنشأ — مفيد للسلوك القديم أو متطلبات القنوات. للنشر على Google Play استخدم تصدير AAB ضمن المزيد → Google Play."
        AppLanguage.PORTUGUESE -> "O padrão é targetSdk 35 (Android 15). Ative para fixar outro nível de API no APK gerado — útil para comportamento legado ou requisitos de canais. Para a Google Play, use a exportação AAB em Mais → Google Play."
        AppLanguage.SPANISH -> "El valor predeterminado es targetSdk 35 (Android 15). Actívalo para fijar otro nivel de API en el APK generado: útil para comportamiento heredado o requisitos de canales. Para Google Play usa la exportación AAB en Más → Google Play."
        AppLanguage.FRENCH -> "Par défaut targetSdk 35 (Android 15). Activez pour fixer un autre niveau d'API pour l'APK généré — utile pour un comportement hérité ou des exigences de canal. Pour Google Play, utilisez l'export AAB dans Plus → Google Play."
        AppLanguage.GERMAN -> "Standard ist targetSdk 35 (Android 15). Aktivieren, um ein anderes API-Level für das generierte APK festzulegen — nützlich für Legacy-Verhalten oder Kanalanforderungen. Für Google Play den AAB-Export unter Mehr → Google Play verwenden."
        AppLanguage.RUSSIAN -> "По умолчанию targetSdk 35 (Android 15). Включите, чтобы зафиксировать другой уровень API для генерируемого APK — полезно для старого поведения или требований каналов. Для Google Play используйте экспорт AAB в разделе Ещё → Google Play."
        AppLanguage.JAPANESE -> "デフォルトは targetSdk 35（Android 15）です。オンにすると生成 APK に別の API レベルを固定できます。旧動作との互換や配信チャンネルの要件に便利です。Google Play への公開は「その他 → Google Play」の AAB エクスポートをご利用ください。"
        AppLanguage.KOREAN -> "기본값은 targetSdk 35(Android 15)입니다. 켜면 생성된 APK에 다른 API 레벨을 고정할 수 있습니다. 레거시 동작이나 채널 요구사항에 유용합니다. Google Play 배포는 '더보기 → Google Play'의 AAB보내기를 사용하세요."
    }

    val targetSdkOverrideOnHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "生成的 APK 将使用所选 targetSdk。运行时已适配现代 Android 行为（通知权限、前台服务类型、精确闹钟兜底等）。上架 Google Play 请用「更多 → Google Play」的 AAB 导出；此处用于侧载或第三方分发渠道。"
        AppLanguage.ENGLISH -> "The generated APK will use the selected targetSdk. The runtime already handles modern Android behavior (notification permission, foreground-service types, exact-alarm fallback). Use AAB export for Google Play; this is for sideload or third-party distribution."
        AppLanguage.ARABIC -> "سيستخدم التطبيق المُنشأ targetSdk المحدد. وقت التشغيل يتعامل مع سلوك Android الحديث (إذن الإشعارات، أنواع الخدمات الأمامية، تراجع المنبه الدقيق). استخدم تصدير AAB لـ Google Play؛ هذا مخصص للتثبيت الجانبي أو قنوات الطرف الثالث."
        AppLanguage.PORTUGUESE -> "O APK gerado usará o targetSdk selecionado. O runtime já lida com o comportamento moderno do Android (permissão de notificação, tipos de serviço em primeiro plano, fallback de alarme exato). Use a exportação AAB para a Google Play; isto é para sideload ou canais de terceiros."
        AppLanguage.SPANISH -> "El APK generado usará el targetSdk seleccionado. El runtime ya maneja el comportamiento moderno de Android (permiso de notificaciones, tipos de servicios en primer plano, fallback de alarma exacta). Usa la exportación AAB para Google Play; esto es para sideload o canales de terceros."
        AppLanguage.FRENCH -> "L'APK généré utilisera le targetSdk sélectionné. Le runtime gère déjà le comportement Android moderne (autorisation de notification, types de services de premier plan, repli d'alarme exacte). Utilisez l'export AAB pour Google Play ; ceci est pour le sideload ou la distribution tierce."
        AppLanguage.GERMAN -> "Das generierte APK verwendet das gewählte targetSdk. Die Laufzeit behandelt bereits modernes Android-Verhalten (Benachrichtigungsberechtigung, Vordergrunddienst-Typen, Fallback für exakte Alarme). Für Google Play den AAB-Export verwenden; dies ist für Sideload oder Drittanbieter-Vertrieb."
        AppLanguage.RUSSIAN -> "Сгенерированный APK будет использовать выбранный targetSdk. Среда выполнения уже учитывает современное поведение Android (разрешение на уведомления, типы фоновых служб, резерв для точных будильников). Для Google Play используйте экспорт AAB; это — для sideload или сторонних каналов."
        AppLanguage.JAPANESE -> "生成される APK は選択した targetSdk を使用します。ランタイムは最新の Android 動作に対応済みです（通知権限、フォアグラウンドサービスの種別、正確なアラームのフォールバック）。Google Play 公開には AAB エクスポートを使用してください。こちらはサイドロードやサードパーティ配布向けです。"
        AppLanguage.KOREAN -> "생성된 APK가 선택한 targetSdk를 사용합니다. 런타임은 최신 Android 동작을 이미 처리합니다(알림 권한, 포그라운드 서비스 유형, 정확한 알람 폴백). Google Play 배포에는 AAB보내기를 사용하세요. 이 옵션은 사이드로드 또는 타사 배포용입니다."
    }

    val minSdkOverrideTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自定义 minSdk"
        AppLanguage.ENGLISH -> "Override minSdk"
        AppLanguage.ARABIC -> "تجاوز minSdk"
        AppLanguage.PORTUGUESE -> "Substituir minSdk"
        AppLanguage.SPANISH -> "Sobrescribir minSdk"
        AppLanguage.FRENCH -> "Remplacer minSdk"
        AppLanguage.GERMAN -> "minSdk überschreiben"
        AppLanguage.RUSSIAN -> "Изменить minSdk"
        AppLanguage.JAPANESE -> "minSdk を上書き"
        AppLanguage.KOREAN -> "minSdk 재정의"
    }

    val minSdkOverrideOffHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "默认 minSdk 23（Android 6）。开启后可提高最低系统版本，用于排除旧设备或满足渠道要求。不能低于 23，也不能高于当前 targetSdk。"
        AppLanguage.ENGLISH -> "Defaults to minSdk 23 (Android 6). Turn on to raise the floor so older devices are excluded or a store requirement is met. Cannot go below 23 or above the current targetSdk."
        AppLanguage.ARABIC -> "الافتراضي minSdk 23 (أندرويد 6). فعّل لرفع الحد الأدنى لاستبعاد الأجهزة القديمة أو تلبية متطلبات المتجر. لا يمكن أن يقل عن 23 أو يتجاوز targetSdk الحالي."
        AppLanguage.PORTUGUESE -> "O padrão é minSdk 23 (Android 6). Ative para elevar o piso, excluindo dispositivos antigos ou atendendo a um requisito de loja. Não pode ficar abaixo de 23 nem acima do targetSdk atual."
        AppLanguage.SPANISH -> "El valor predeterminado es minSdk 23 (Android 6). Actívalo para subir el mínimo, excluir dispositivos antiguos o cumplir un requisito de tienda. No puede bajar de 23 ni superar el targetSdk actual."
        AppLanguage.FRENCH -> "Par défaut minSdk 23 (Android 6). Activez pour relever le plancher, exclure les anciens appareils ou satisfaire une exigence de magasin. Impossible de descendre sous 23 ou de dépasser le targetSdk actuel."
        AppLanguage.GERMAN -> "Standard ist minSdk 23 (Android 6). Aktivieren, um die Untergrenze anzuheben, ältere Geräte auszuschließen oder eine Store-Anforderung zu erfüllen. Nicht unter 23 und nicht über dem aktuellen targetSdk."
        AppLanguage.RUSSIAN -> "По умолчанию minSdk 23 (Android 6). Включите, чтобы поднять нижнюю границу, исключить старые устройства или выполнить требование магазина. Нельзя ниже 23 и выше текущего targetSdk."
        AppLanguage.JAPANESE -> "デフォルトは minSdk 23（Android 6）です。オンにすると下限を上げ、古い端末を除外したりストア要件を満たしたりできます。23 未満や現在の targetSdk 超えはできません。"
        AppLanguage.KOREAN -> "기본값은 minSdk 23(Android 6)입니다. 켜면 하한을 올려 오래된 기기를 제외하거나 스토어 요구를 맞출 수 있습니다. 23 미만이거나 현재 targetSdk보다 높을 수 없습니다."
    }

    val minSdkOverrideOnHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "生成的 APK 将使用所选 minSdk。运行时按 API 23 编译，无法安装到更低系统。选择不能超过当前 targetSdk。"
        AppLanguage.ENGLISH -> "The generated APK will use the selected minSdk. The runtime is compiled for API 23, so it cannot install below that. The choice cannot exceed the current targetSdk."
        AppLanguage.ARABIC -> "سيستخدم التطبيق المُنشأ minSdk المحدد. وقت التشغيل مُجمَّع لـ API 23 لذا لا يمكن التثبيت أدنى من ذلك. لا يمكن أن يتجاوز الاختيار targetSdk الحالي."
        AppLanguage.PORTUGUESE -> "O APK gerado usará o minSdk selecionado. O runtime é compilado para API 23, então não instala abaixo disso. A escolha não pode exceder o targetSdk atual."
        AppLanguage.SPANISH -> "El APK generado usará el minSdk seleccionado. El runtime está compilado para API 23, así que no se instala por debajo. La elección no puede superar el targetSdk actual."
        AppLanguage.FRENCH -> "L'APK généré utilisera le minSdk sélectionné. Le runtime est compilé pour l'API 23, donc il ne s'installe pas en dessous. Le choix ne peut pas dépasser le targetSdk actuel."
        AppLanguage.GERMAN -> "Das generierte APK verwendet das gewählte minSdk. Die Laufzeit ist für API 23 kompiliert und installiert nicht darunter. Die Wahl darf das aktuelle targetSdk nicht überschreiten."
        AppLanguage.RUSSIAN -> "Сгенерированный APK будет использовать выбранный minSdk. Среда выполнения собрана под API 23 и не устанавливается ниже. Выбор не может превышать текущий targetSdk."
        AppLanguage.JAPANESE -> "生成される APK は選択した minSdk を使用します。ランタイムは API 23 向けにコンパイルされているため、それ未満にはインストールできません。現在の targetSdk を超える選択はできません。"
        AppLanguage.KOREAN -> "생성된 APK가 선택한 minSdk를 사용합니다. 런타임은 API 23용으로 컴파일되어 그 아래에는 설치되지 않습니다. 현재 targetSdk를 넘길 수 없습니다."
    }

    val signingTypeAutoGenerated: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自动生成的证书"
        AppLanguage.ENGLISH -> "Auto-generated certificate"
        AppLanguage.ARABIC -> "شهادة مولدة تلقائيًا"
        AppLanguage.PORTUGUESE -> "Certificado gerado automaticamente"
        AppLanguage.SPANISH -> "Certificado generado automáticamente"
        AppLanguage.FRENCH -> "Certificat généré automatiquement"
        AppLanguage.GERMAN -> "Automatisch generiertes Zertifikat"
        AppLanguage.RUSSIAN -> "Автоматически созданный сертификат"
        AppLanguage.JAPANESE -> "自動生成された証明書"
        AppLanguage.KOREAN -> "자동 생성된 인증서"
    }

    val signingTypeCustom: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自定义证书"
        AppLanguage.ENGLISH -> "Custom certificate"
        AppLanguage.ARABIC -> "شهادة مخصصة"
        AppLanguage.PORTUGUESE -> "Certificado personalizado"
        AppLanguage.SPANISH -> "Certificado personalizado"
        AppLanguage.FRENCH -> "Certificat personnalisé"
        AppLanguage.GERMAN -> "Benutzerdefiniertes Zertifikat"
        AppLanguage.RUSSIAN -> "Пользовательский сертификат"
        AppLanguage.JAPANESE -> "カスタム証明書"
        AppLanguage.KOREAN -> "커스텀 인증서"
    }

    val signingTypeAndroidKeyStore: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Android 系统密钥库"
        AppLanguage.ENGLISH -> "Android KeyStore"
        AppLanguage.ARABIC -> "مخزن مفاتيح أندرويد"
        AppLanguage.PORTUGUESE -> "Armazenamento de Chaves do Android"
        AppLanguage.SPANISH -> "Almacén de Claves de Android"
        AppLanguage.FRENCH -> "Magasin de Clés Android"
        AppLanguage.GERMAN -> "Android-Schlüsselspeicher"
        AppLanguage.RUSSIAN -> "Хранилище ключей Android"
        AppLanguage.JAPANESE -> "Androidキーストア"
        AppLanguage.KOREAN -> "Android 키 저장소"
    }

    val importKeystore: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "导入签名文件"
        AppLanguage.ENGLISH -> "Import Keystore"
        AppLanguage.ARABIC -> "استيراد ملف التوقيع"
        AppLanguage.PORTUGUESE -> "Importar Keystore"
        AppLanguage.SPANISH -> "Importar Keystore"
        AppLanguage.FRENCH -> "Importer le Keystore"
        AppLanguage.GERMAN -> "Keystore importieren"
        AppLanguage.RUSSIAN -> "Импортировать Keystore"
        AppLanguage.JAPANESE -> "Keystoreをインポート"
        AppLanguage.KOREAN -> "Keystore 가져오기"
    }

    val exportKeystore: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "导出签名文件"
        AppLanguage.ENGLISH -> "Export Keystore"
        AppLanguage.ARABIC -> "تصدير ملف التوقيع"
        AppLanguage.PORTUGUESE -> "Exportar Keystore"
        AppLanguage.SPANISH -> "Exportar Keystore"
        AppLanguage.FRENCH -> "Exporter le Keystore"
        AppLanguage.GERMAN -> "Keystore exportieren"
        AppLanguage.RUSSIAN -> "Экспортировать Keystore"
        AppLanguage.JAPANESE -> "Keystoreをエクスポート"
        AppLanguage.KOREAN -> "Keystore 내보내기"
    }

    val removeCustomKeystore: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "删除自定义证书"
        AppLanguage.ENGLISH -> "Remove Custom Certificate"
        AppLanguage.ARABIC -> "إزالة الشهادة المخصصة"
        AppLanguage.PORTUGUESE -> "Remover Certificado Personalizado"
        AppLanguage.SPANISH -> "Eliminar Certificado Personalizado"
        AppLanguage.FRENCH -> "Supprimer le Certificat Personnalisé"
        AppLanguage.GERMAN -> "Benutzerdefiniertes Zertifikat entfernen"
        AppLanguage.RUSSIAN -> "Удалить пользовательский сертификат"
        AppLanguage.JAPANESE -> "カスタム証明書を削除"
        AppLanguage.KOREAN -> "커스텀 인증서 제거"
    }

    val keystorePassword: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "密钥库密码"
        AppLanguage.ENGLISH -> "Keystore Password"
        AppLanguage.ARABIC -> "كلمة مرور مخزن المفاتيح"
        AppLanguage.PORTUGUESE -> "Senha do Keystore"
        AppLanguage.SPANISH -> "Contraseña del Keystore"
        AppLanguage.FRENCH -> "Mot de passe du Keystore"
        AppLanguage.GERMAN -> "Keystore-Passwort"
        AppLanguage.RUSSIAN -> "Пароль Keystore"
        AppLanguage.JAPANESE -> "Keystoreパスワード"
        AppLanguage.KOREAN -> "Keystore 비밀번호"
    }

    val keystorePasswordHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "请输入签名文件的密码"
        AppLanguage.ENGLISH -> "Enter keystore password"
        AppLanguage.ARABIC -> "أدخل كلمة مرور مخزن المفاتيح"
        AppLanguage.PORTUGUESE -> "Digite a senha do keystore"
        AppLanguage.SPANISH -> "Introduce la contraseña del keystore"
        AppLanguage.FRENCH -> "Saisir le mot de passe du keystore"
        AppLanguage.GERMAN -> "Keystore-Passwort eingeben"
        AppLanguage.RUSSIAN -> "Введите пароль keystore"
        AppLanguage.JAPANESE -> "keystoreパスワードを入力"
        AppLanguage.KOREAN -> "keystore 비밀번호 입력"
    }

    val keyPasswordOptional: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "密钥密码（可选）"
        AppLanguage.ENGLISH -> "Key Password (optional)"
        AppLanguage.ARABIC -> "كلمة مرور المفتاح (اختياري)"
        AppLanguage.PORTUGUESE -> "Senha da Chave (opcional)"
        AppLanguage.SPANISH -> "Contraseña de Clave (opcional)"
        AppLanguage.FRENCH -> "Mot de passe de la Clé (facultatif)"
        AppLanguage.GERMAN -> "Schlüsselpasswort (optional)"
        AppLanguage.RUSSIAN -> "Пароль ключа (необязательно)"
        AppLanguage.JAPANESE -> "キーパスワード（任意）"
        AppLanguage.KOREAN -> "키 비밀번호 (선택)"
    }

    val keyPasswordHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "若 key 密码与 keystore 密码不同时填写；留空表示一致"
        AppLanguage.ENGLISH -> "Fill in only if the key password differs from the keystore password; leave blank if they are the same"
        AppLanguage.ARABIC -> "املأ هذا الحقل فقط إذا كانت كلمة مرور المفتاح تختلف عن كلمة مرور مخزن المفاتيح؛ اتركه فارغًا إذا كانا متطابقين"
        AppLanguage.PORTUGUESE -> "Preencha apenas se a senha da chave for diferente da senha do keystore; deixe em branco se forem iguais"
        AppLanguage.SPANISH -> "Rellena solo si la contraseña de la clave difiere de la del keystore; déjalo en blanco si son iguales"
        AppLanguage.FRENCH -> "Remplir uniquement si le mot de passe de la clé diffère de celui du keystore ; laisser vide s'ils sont identiques"
        AppLanguage.GERMAN -> "Nur ausfüllen, wenn das Schlüsselpasswort vom Keystore-Passwort abweicht; leer lassen, wenn identisch"
        AppLanguage.RUSSIAN -> "Заполняйте только если пароль ключа отличается от пароля keystore; оставьте пустым, если они совпадают"
        AppLanguage.JAPANESE -> "キーパスワードがkeystoreパスワードと異なる場合のみ入力。同じ場合は空欄のまま"
        AppLanguage.KOREAN -> "키 비밀번호가 keystore 비밀번호와 다를 때만 입력. 같으면 비워두세요"
    }

    val exportPassword: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "导出密码"
        AppLanguage.ENGLISH -> "Export Password"
        AppLanguage.ARABIC -> "كلمة مرور التصدير"
        AppLanguage.PORTUGUESE -> "Senha de Exportação"
        AppLanguage.SPANISH -> "Contraseña de Exportación"
        AppLanguage.FRENCH -> "Mot de passe d'Exportation"
        AppLanguage.GERMAN -> "Exportpasswort"
        AppLanguage.RUSSIAN -> "Пароль экспорта"
        AppLanguage.JAPANESE -> "エクスポートパスワード"
        AppLanguage.KOREAN -> "내보내기 비밀번호"
    }

    val exportPasswordHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "设置导出文件的密码"
        AppLanguage.ENGLISH -> "Set password for exported file"
        AppLanguage.ARABIC -> "تعيين كلمة مرور للملف المصدر"
        AppLanguage.PORTUGUESE -> "Definir senha para o arquivo exportado"
        AppLanguage.SPANISH -> "Establecer contraseña para el archivo exportado"
        AppLanguage.FRENCH -> "Définir le mot de passe du fichier exporté"
        AppLanguage.GERMAN -> "Passwort für exportierte Datei festlegen"
        AppLanguage.RUSSIAN -> "Установить пароль для экспортируемого файла"
        AppLanguage.JAPANESE -> "エクスポートファイルのパスワードを設定"
        AppLanguage.KOREAN -> "내보낸 파일의 비밀번호 설정"
    }

    val keystoreImportSuccess: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "签名证书导入成功"
        AppLanguage.ENGLISH -> "Keystore imported successfully"
        AppLanguage.ARABIC -> "تم استيراد مخزن المفاتيح بنجاح"
        AppLanguage.PORTUGUESE -> "Keystore importado com sucesso"
        AppLanguage.SPANISH -> "Keystore importado correctamente"
        AppLanguage.FRENCH -> "Keystore importé avec succès"
        AppLanguage.GERMAN -> "Keystore erfolgreich importiert"
        AppLanguage.RUSSIAN -> "Keystore успешно импортирован"
        AppLanguage.JAPANESE -> "Keystoreをインポートしました"
        AppLanguage.KOREAN -> "Keystore를 성공적으로 가져왔습니다"
    }

    val keystoreImportFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "签名证书导入失败。请检查 keystore 密码；若你的 keystore 是 JKS 且 key 密码与 store 密码不同，请同时填写「密钥密码」字段"
        AppLanguage.ENGLISH -> "Keystore import failed. Check the keystore password; if your keystore is JKS with a separate key password, also fill in the Key Password field"
        AppLanguage.ARABIC -> "فشل استيراد مخزن المفاتيح. تحقق من كلمة مرور مخزن المفاتيح؛ إذا كان مخزن المفاتيح من نوع JKS بكلمة مرور مفتاح منفصلة، املأ أيضًا حقل كلمة مرور المفتاح"
        AppLanguage.PORTUGUESE -> "Falha na importação do keystore. Verifique a senha do keystore; se o seu keystore for JKS com senha de chave separada, preencha também o campo Senha da Chave"
        AppLanguage.SPANISH -> "Error al importar el keystore. Verifica la contraseña del keystore; si tu keystore es JKS con contraseña de clave independiente, rellena también el campo Contraseña de Clave"
        AppLanguage.FRENCH -> "Échec de l'importation du keystore. Vérifiez le mot de passe du keystore ; si votre keystore est JKS avec un mot de passe de clé distinct, remplissez aussi le champ Mot de passe de la Clé"
        AppLanguage.GERMAN -> "Keystore-Import fehlgeschlagen. Keystore-Passwort prüfen; falls der Keystore JKS mit separatem Schlüsselpasswort ist, auch das Feld Schlüsselpasswort ausfüllen"
        AppLanguage.RUSSIAN -> "Ошибка импорта keystore. Проверьте пароль keystore; если ваш keystore — JKS с отдельным паролем ключа, также заполните поле «Пароль ключа»"
        AppLanguage.JAPANESE -> "Keystoreのインポートに失敗しました。keystoreパスワードを確認してください。JKSでキーパスワードが別の場合は、「キーパスワード」欄も入力してください"
        AppLanguage.KOREAN -> "Keystore 가져오기 실패. keystore 비밀번호를 확인하세요. JKS이고 키 비밀번호가 다른 경우, \"키 비밀번호\" 필드도 입력하세요"
    }

    val keystoreStorePasswordRejected: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Keystore 密码被拒绝。文件格式已识别，请仔细核对「Keystore 密码」是否输入正确"
        AppLanguage.ENGLISH -> "The keystore password was rejected. The file format was recognized — double-check the Keystore Password field for typos"
        AppLanguage.ARABIC -> "تم رفض كلمة مرور مخزن المفاتيح. تم التعرّف على تنسيق الملف — تحقق من حقل كلمة مرور مخزن المفاتيح بحثًا عن أخطاء كتابية"
        AppLanguage.PORTUGUESE -> "A senha do keystore foi rejeitada. O formato do arquivo foi reconhecido — verifique erros de digitação no campo Senha do Keystore"
        AppLanguage.SPANISH -> "La contraseña del keystore fue rechazada. El formato del archivo se reconoció correctamente — revisa si hay errores en el campo Contraseña del Keystore"
        AppLanguage.FRENCH -> "Le mot de passe du keystore a été rejeté. Le format du fichier a été reconnu — vérifiez les fautes de frappe dans le champ Mot de passe du Keystore"
        AppLanguage.GERMAN -> "Das Keystore-Passwort wurde abgelehnt. Das Dateiformat wurde erkannt — prüfe das Feld Keystore-Passwort auf Tippfehler"
        AppLanguage.RUSSIAN -> "Пароль keystore отклонён. Формат файла распознан — проверьте опечатки в поле «Пароль keystore»"
        AppLanguage.JAPANESE -> "keystoreパスワードが拒否されました。ファイル形式は認識できています。「keystoreパスワード」欄の入力ミスをご確認ください"
        AppLanguage.KOREAN -> "keystore 비밀번호가 거부되었습니다. 파일 형식은 정상적으로 인식되었습니다. \"keystore 비밀번호\" 필드에 오타가 없는지 확인하세요"
    }

    val keystoreKeyPasswordRejected: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "密钥密码被拒绝。该 keystore 的 key 密码与 store 密码不同——请正确填写或修正「密钥密码」字段"
        AppLanguage.ENGLISH -> "The key password was rejected. This keystore's key password differs from the store password — correct the Key Password field"
        AppLanguage.ARABIC -> "تم رفض كلمة مرور المفتاح. كلمة مرور المفتاح في هذا المخزن تختلف عن كلمة مرور المخزن — صحّح حقل كلمة مرور المفتاح"
        AppLanguage.PORTUGUESE -> "A senha da chave foi rejeitada. A senha de chave deste keystore é diferente da senha do keystore — corrija o campo Senha da Chave"
        AppLanguage.SPANISH -> "La contraseña de la clave fue rechazada. La contraseña de clave de este keystore difiere de la del keystore — corrige el campo Contraseña de Clave"
        AppLanguage.FRENCH -> "Le mot de passe de la clé a été rejeté. Le mot de passe de clé de ce keystore diffère de celui du magasin — corrigez le champ Mot de passe de la Clé"
        AppLanguage.GERMAN -> "Das Schlüsselpasswort wurde abgelehnt. Das Schlüsselpasswort dieses Keystores weicht vom Keystore-Passwort ab — korrigiere das Feld Schlüsselpasswort"
        AppLanguage.RUSSIAN -> "Пароль ключа отклонён. Пароль ключа в этом keystore отличается от пароля хранилища — исправьте поле «Пароль ключа»"
        AppLanguage.JAPANESE -> "キーパスワードが拒否されました。このkeystoreのキーパスワードはstoreパスワードと異なります。「キーパスワード」欄を修正してください"
        AppLanguage.KOREAN -> "키 비밀번호가 거부되었습니다. 이 keystore의 키 비밀번호는 store 비밀번호와 다릅니다. \"키 비밀번호\" 필드를 수정하세요"
    }

    val keystoreNoKeyEntry: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "keystore 中没有找到私钥条目。请导入包含密钥对的 keystore（例如通过 Android Studio 签名向导创建），而不是仅含证书的文件"
        AppLanguage.ENGLISH -> "No private key entry was found in this keystore. Import a keystore that contains a key pair (e.g. one created via Android Studio's signed-APK wizard), not a certificate-only file"
        AppLanguage.ARABIC -> "لم يتم العثور على إدخال مفتاح خاص في مخزن المفاتيح هذا. استورد مخزن مفاتيح يحتوي على زوج مفاتيح (مثل الذي ينشئه معالج التوقيع في Android Studio)، وليس ملف شهادات فقط"
        AppLanguage.PORTUGUESE -> "Nenhuma entrada de chave privada foi encontrada neste keystore. Importe um keystore que contenha um par de chaves (ex.: criado pelo assistente de APK assinado do Android Studio), não um arquivo só de certificados"
        AppLanguage.SPANISH -> "No se encontró ninguna entrada de clave privada en este keystore. Importa un keystore que contenga un par de claves (p. ej., uno creado con el asistente de APK firmado de Android Studio), no un archivo de solo certificados"
        AppLanguage.FRENCH -> "Aucune entrÉe de clé privée n'a été trouvée dans ce keystore. Importez un keystore contenant une paire de clés (p. ex. créée via l'assistant APK signé d'Android Studio), et non un fichier de certificats seuls"
        AppLanguage.GERMAN -> "In diesem Keystore wurde kein privater Schlüssel-Eintrag gefunden. Importiere einen Keystore mit einem Schlüsselpaar (z. B. über den Signierungs-Assistenten von Android Studio erstellt), keine reine Zertifikatsdatei"
        AppLanguage.RUSSIAN -> "В этом keystore не найдена запись с приватным ключом. Импортируйте keystore с парой ключей (например, созданный мастером подписи Android Studio), а не файл только с сертификатами"
        AppLanguage.JAPANESE -> "このkeystoreに秘密鍵エントリが見つかりません。証明書のみのファイルではなく、鍵ペアを含むkeystore（例：Android Studioの署名ウィザードで作成）をインポートしてください"
        AppLanguage.KOREAN -> "이 keystore에서 개인 키 항목을 찾을 수 없습니다. 인증서만 포함된 파일이 아니라 키 쌍이 포함된 keystore(예: Android Studio 서명 마법사로 생성)를 가져오세요"
    }

    val keystoreUnsupportedFormat: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "无法识别的 keystore 格式。支持 PKCS12、JKS、JCEKS 和 BKS；若问题持续，请用原工具重新导出"
        AppLanguage.ENGLISH -> "Unrecognized keystore format. Supported types: PKCS12, JKS, JCEKS and BKS. If the problem persists, re-export the keystore from your original tool"
        AppLanguage.ARABIC -> "تنسيق مخزن مفاتيح غير معروف. الأنواع المدعومة: PKCS12 و JKS و JCEKS و BKS. إذا استمرت المشكلة، أعد تصدير مخزن المفاتيح من الأداة الأصلية"
        AppLanguage.PORTUGUESE -> "Formato de keystore não reconhecido. Tipos suportados: PKCS12, JKS, JCEKS e BKS. Se o problema persistir, exporte novamente o keystore a partir da ferramenta original"
        AppLanguage.SPANISH -> "Formato de keystore no reconocido. Tipos admitidos: PKCS12, JKS, JCEKS y BKS. Si el problema continúa, vuelve a exportar el keystore desde tu herramienta original"
        AppLanguage.FRENCH -> "Format de keystore non reconnu. Types pris en charge : PKCS12, JKS, JCEKS et BKS. Si le problème persiste, réexportez le keystore depuis votre outil d'origine"
        AppLanguage.GERMAN -> "Unbekanntes Keystore-Format. Unterstützte Typen: PKCS12, JKS, JCEKS und BKS. Falls das Problem weiterhin besteht, exportiere den Keystore mit dem ursprünglichen Tool neu"
        AppLanguage.RUSSIAN -> "Неизвестный формат keystore. Поддерживаются: PKCS12, JKS, JCEKS и BKS. Если проблема не исчезнет, повторно экспортируйте keystore из исходного инструмента"
        AppLanguage.JAPANESE -> "認識できないkeystore形式です。対応形式：PKCS12、JKS、JCEKS、BKS。問題が続く場合は、元のツールでkeystoreを再エクスポートしてください"
        AppLanguage.KOREAN -> "인식할 수 없는 keystore 형식입니다. 지원 형식: PKCS12, JKS, JCEKS, BKS. 문제가 계속되면 원래 도구에서 keystore를 다시 내보내세요"
    }

    val keystoreExportSuccess: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "签名证书导出成功"
        AppLanguage.ENGLISH -> "Keystore exported successfully"
        AppLanguage.ARABIC -> "تم تصدير مخزن المفاتيح بنجاح"
        AppLanguage.PORTUGUESE -> "Keystore exportado com sucesso"
        AppLanguage.SPANISH -> "Keystore exportado correctamente"
        AppLanguage.FRENCH -> "Keystore exporté avec succès"
        AppLanguage.GERMAN -> "Keystore erfolgreich exportiert"
        AppLanguage.RUSSIAN -> "Keystore успешно экспортирован"
        AppLanguage.JAPANESE -> "Keystoreをエクスポートしました"
        AppLanguage.KOREAN -> "Keystore를 성공적으로 내보냈습니다"
    }

    val keystoreExportFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "签名证书导出失败"
        AppLanguage.ENGLISH -> "Keystore export failed"
        AppLanguage.ARABIC -> "فشل تصدير مخزن المفاتيح"
        AppLanguage.PORTUGUESE -> "Falha na exportação do keystore"
        AppLanguage.SPANISH -> "Error al exportar el keystore"
        AppLanguage.FRENCH -> "Échec de l'exportation du keystore"
        AppLanguage.GERMAN -> "Keystore-Export fehlgeschlagen"
        AppLanguage.RUSSIAN -> "Ошибка экспорта keystore"
        AppLanguage.JAPANESE -> "Keystoreのエクスポートに失敗しました"
        AppLanguage.KOREAN -> "Keystore 내보내기 실패"
    }

    val keystoreRemoveSuccess: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已删除自定义证书，将使用自动生成的证书"
        AppLanguage.ENGLISH -> "Custom certificate removed, will use auto-generated certificate"
        AppLanguage.ARABIC -> "تمت إزالة الشهادة المخصصة، سيتم استخدام الشهادة المولدة تلقائيًا"
        AppLanguage.PORTUGUESE -> "Certificado personalizado removido, será usado o certificado gerado automaticamente"
        AppLanguage.SPANISH -> "Certificado personalizado eliminado, se usará el certificado generado automáticamente"
        AppLanguage.FRENCH -> "Certificat personnalisé supprimé, le certificat généré automatiquement sera utilisé"
        AppLanguage.GERMAN -> "Benutzerdefiniertes Zertifikat entfernt, automatisch generiertes Zertifikat wird verwendet"
        AppLanguage.RUSSIAN -> "Пользовательский сертификат удалён, будет использован автоматически созданный сертификат"
        AppLanguage.JAPANESE -> "カスタム証明書を削除しました。自動生成された証明書を使用します"
        AppLanguage.KOREAN -> "커스텀 인증서가 제거되었습니다. 자동 생성된 인증서를 사용합니다"
    }

    val keystoreRemoveConfirm: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "确定要删除自定义签名证书吗？删除后将使用自动生成的证书进行签名。"
        AppLanguage.ENGLISH -> "Are you sure you want to remove the custom certificate? Auto-generated certificate will be used for signing."
        AppLanguage.ARABIC -> "هل أنت متأكد من إزالة الشهادة المخصصة؟ سيتم استخدام الشهادة المولدة تلقائيًا للتوقيع."
        AppLanguage.PORTUGUESE -> "Tem certeza de que deseja remover o certificado personalizado? O certificado gerado automaticamente será usado para assinatura."
        AppLanguage.SPANISH -> "¿Estás seguro de que quieres eliminar el certificado personalizado? Se usará el certificado generado automáticamente para firmar."
        AppLanguage.FRENCH -> "Êtes-vous sûr de vouloir supprimer le certificat personnalisé ? Le certificat généré automatiquement sera utilisé pour la signature."
        AppLanguage.GERMAN -> "Benutzerdefiniertes Zertifikat wirklich entfernen? Automatisch generiertes Zertifikat wird zum Signieren verwendet."
        AppLanguage.RUSSIAN -> "Уверены, что хотите удалить пользовательский сертификат? Для подписи будет использован автоматически созданный сертификат."
        AppLanguage.JAPANESE -> "カスタム証明書を削除してもよろしいですか？署名には自動生成された証明書が使用されます。"
        AppLanguage.KOREAN -> "커스텀 인증서를 제거하시겠습니까? 서명에 자동 생성된 인증서가 사용됩니다."
    }

    val supportedKeystoreFormats: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "支持 .p12 / .pfx / .jks / .keystore 格式"
        AppLanguage.ENGLISH -> "Supports .p12 / .pfx / .jks / .keystore formats"
        AppLanguage.ARABIC -> "يدعم صيغ .p12 / .pfx / .jks / .keystore"
        AppLanguage.PORTUGUESE -> "Suporta formatos .p12 / .pfx / .jks / .keystore"
        AppLanguage.SPANISH -> "Soporta formatos .p12 / .pfx / .jks / .keystore"
        AppLanguage.FRENCH -> "Prend en charge les formats .p12 / .pfx / .jks / .keystore"
        AppLanguage.GERMAN -> "Unterstützt .p12 / .pfx / .jks / .keystore Formate"
        AppLanguage.RUSSIAN -> "Поддерживает форматы .p12 / .pfx / .jks / .keystore"
        AppLanguage.JAPANESE -> ".p12 / .pfx / .jks / .keystore 形式に対応"
        AppLanguage.KOREAN -> ".p12 / .pfx / .jks / .keystore 형식 지원"
    }

    val customSigningNote: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自定义签名为全局设置，导入后所有新打包的 APK 都将使用此证书签名"
        AppLanguage.ENGLISH -> "Custom signing is a global setting. All newly built APKs will use this certificate"
        AppLanguage.ARABIC -> "التوقيع المخصص إعداد عام. جميع ملفات APK المبنية حديثًا ستستخدم هذه الشهادة"
        AppLanguage.PORTUGUESE -> "A assinatura personalizada é uma configuração global. Todos os APKs recém-construídos usarão este certificado"
        AppLanguage.SPANISH -> "La firma personalizada es una configuración global. Todos los APK recién construidos usarán este certificado"
        AppLanguage.FRENCH -> "La signature personnalisée est un paramètre global. Tous les APK nouvellement construits utiliseront ce certificat"
        AppLanguage.GERMAN -> "Benutzerdefinierte Signatur ist eine globale Einstellung. Alle neu erstellten APKs verwenden dieses Zertifikat"
        AppLanguage.RUSSIAN -> "Пользовательская подпись — глобальная настройка. Все новые APK будут использовать этот сертификат"
        AppLanguage.JAPANESE -> "カスタム署名はグローバル設定です。新しくビルドされたすべてのAPKがこの証明書を使用します"
        AppLanguage.KOREAN -> "커스텀 서명은 전역 설정입니다. 새로 빌드된 모든 APK가 이 인증서를 사용합니다"
    }

    val signingSchemeTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "签名方案"
        AppLanguage.ENGLISH -> "Signing Schemes"
        AppLanguage.ARABIC -> "مخططات التوقيع"
        AppLanguage.PORTUGUESE -> "Esquemas de Assinatura"
        AppLanguage.SPANISH -> "Esquemas de Firma"
        AppLanguage.FRENCH -> "Schémas de Signature"
        AppLanguage.GERMAN -> "Signaturschemata"
        AppLanguage.RUSSIAN -> "Схемы подписи"
        AppLanguage.JAPANESE -> "署名スキーム"
        AppLanguage.KOREAN -> "서명 스킴"
    }

    val signingSchemeNote: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择要使用的 APK 签名方案。方案为全局设置，作用于所有新打包的 APK"
        AppLanguage.ENGLISH -> "Choose which APK signature schemes to apply. This is a global setting affecting all newly built APKs"
        AppLanguage.ARABIC -> "اختر مخططات توقيع APK المراد تطبيقها. هذا إعداد عام يؤثر على جميع ملفات APK المبنية حديثًا"
        AppLanguage.PORTUGUESE -> "Escolha quais esquemas de assinatura do APK aplicar. Esta é uma configuração global que afeta todos os APKs recém-construídos"
        AppLanguage.SPANISH -> "Elige qué esquemas de firma del APK aplicar. Esta es una configuración global que afecta a todos los APK recién construidos"
        AppLanguage.FRENCH -> "Choisissez les schémas de signature de l'APK à appliquer. Il s'agit d'un paramètre global affectant tous les APK nouvellement construits"
        AppLanguage.GERMAN -> "Wählen Sie, welche APK-Signaturschemata angewendet werden sollen. Dies ist eine globale Einstellung, die alle neu erstellten APKs betrifft"
        AppLanguage.RUSSIAN -> "Выберите схемы подписи APK для применения. Это глобальная настройка, влияющая на все новые APK"
        AppLanguage.JAPANESE -> "適用するAPK署名スキームを選択してください。これは新しくビルドされるすべてのAPKに影響するグローバル設定です"
        AppLanguage.KOREAN -> "적용할 APK 서명 스킴을 선택하세요. 이것은 새로 빌드되는 모든 APK에 영향을 미치는 전역 설정입니다"
    }

    val signingSchemeV1Title: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "V1（JAR 签名）"
        AppLanguage.ENGLISH -> "V1 (JAR Signature)"
        AppLanguage.ARABIC -> "V1 (توقيع JAR)"
        AppLanguage.PORTUGUESE -> "V1 (Assinatura JAR)"
        AppLanguage.SPANISH -> "V1 (Firma JAR)"
        AppLanguage.FRENCH -> "V1 (Signature JAR)"
        AppLanguage.GERMAN -> "V1 (JAR-Signatur)"
        AppLanguage.RUSSIAN -> "V1 (подпись JAR)"
        AppLanguage.JAPANESE -> "V1（JAR署名）"
        AppLanguage.KOREAN -> "V1 (JAR 서명)"
    }

    val signingSchemeV1Desc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "兼容所有 Android 版本（API 7+）。关闭后低版本设备可能无法验证签名"
        AppLanguage.ENGLISH -> "Compatible with all Android versions (API 7+). Disabling may break signature verification on older devices"
        AppLanguage.ARABIC -> "متوافق مع جميع إصدارات أندرويد (API 7+). تعطيله قد يعطل التحقق من التوقيع على الأجهزة القديمة"
        AppLanguage.PORTUGUESE -> "Compatível com todas as versões do Android (API 7+). Desativar pode quebrar a verificação de assinatura em dispositivos antigos"
        AppLanguage.SPANISH -> "Compatible con todas las versiones de Android (API 7+). Desactivarlo puede romper la verificación de firma en dispositivos antiguos"
        AppLanguage.FRENCH -> "Compatible avec toutes les versions d'Android (API 7+). La désactivation peut rompre la vérification de signature sur les anciens appareils"
        AppLanguage.GERMAN -> "Kompatibel mit allen Android-Versionen (API 7+). Deaktivieren kann die Signaturverifikation auf älteren Geräten beeinträchtigen"
        AppLanguage.RUSSIAN -> "Совместим со всеми версиями Android (API 7+). Отключение может нарушить проверку подписи на старых устройствах"
        AppLanguage.JAPANESE -> "すべてのAndroidバージョン（API 7+）と互換。無効にすると古いデバイスで署名検証が失敗する可能性があります"
        AppLanguage.KOREAN -> "모든 Android 버전(API 7+)과 호환. 비활성화하면 이전 기기에서 서명 검증이 실패할 수 있습니다"
    }

    val signingSchemeV2Title: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "V2（完整 APK 签名）"
        AppLanguage.ENGLISH -> "V2 (Full APK Signature)"
        AppLanguage.ARABIC -> "V2 (توقيع APK الكامل)"
        AppLanguage.PORTUGUESE -> "V2 (Assinatura APK Completa)"
        AppLanguage.SPANISH -> "V2 (Firma APK Completa)"
        AppLanguage.FRENCH -> "V2 (Signature APK Complète)"
        AppLanguage.GERMAN -> "V2 (Vollständige APK-Signatur)"
        AppLanguage.RUSSIAN -> "V2 (полная подпись APK)"
        AppLanguage.JAPANESE -> "V2（完全APK署名）"
        AppLanguage.KOREAN -> "V2 (전체 APK 서명)"
    }

    val signingSchemeV2Desc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Android 7.0+ 引入，校验更快更安全"
        AppLanguage.ENGLISH -> "Introduced in Android 7.0+, faster and more secure verification"
        AppLanguage.ARABIC -> "ظهر في أندرويد 7.0+، تحقق أسرع وأكثر أمانًا"
        AppLanguage.PORTUGUESE -> "Introduzido no Android 7.0+, verificação mais rápida e segura"
        AppLanguage.SPANISH -> "Introducido en Android 7.0+, verificación más rápida y segura"
        AppLanguage.FRENCH -> "Introduit dans Android 7.0+, vérification plus rapide et plus sécurisée"
        AppLanguage.GERMAN -> "Eingeführt in Android 7.0+, schnellere und sicherere Verifikation"
        AppLanguage.RUSSIAN -> "Появился в Android 7.0+, более быстрая и безопасная проверка"
        AppLanguage.JAPANESE -> "Android 7.0+で導入、より速く安全な検証"
        AppLanguage.KOREAN -> "Android 7.0+에서 도입, 더 빠르고 안전한 검증"
    }

    val signingSchemeV3Title: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "V3（密钥轮换）"
        AppLanguage.ENGLISH -> "V3 (Key Rotation)"
        AppLanguage.ARABIC -> "V3 (تدوير المفاتيح)"
        AppLanguage.PORTUGUESE -> "V3 (Rotação de Chaves)"
        AppLanguage.SPANISH -> "V3 (Rotación de Claves)"
        AppLanguage.FRENCH -> "V3 (Rotation de Clés)"
        AppLanguage.GERMAN -> "V3 (Schlüsselrotation)"
        AppLanguage.RUSSIAN -> "V3 (ротация ключей)"
        AppLanguage.JAPANESE -> "V3（キーローテーション）"
        AppLanguage.KOREAN -> "V3 (키 로테이션)"
    }

    val signingSchemeV3Desc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Android 9.0+ 引入，支持密钥轮换。部分旧证书算法与 V3 不兼容"
        AppLanguage.ENGLISH -> "Introduced in Android 9.0+, supports key rotation. Some legacy certificate algorithms are incompatible with V3"
        AppLanguage.ARABIC -> "ظهر في أندرويد 9.0+، يدعم تدوير المفاتيح. بعض خوارزميات الشهادات القديمة غير متوافقة مع V3"
        AppLanguage.PORTUGUESE -> "Introduzido no Android 9.0+, suporta rotação de chaves. Alguns algoritmos de certificado legados são incompatíveis com o V3"
        AppLanguage.SPANISH -> "Introducido en Android 9.0+, soporta rotación de claves. Algunos algoritmos de certificado heredados son incompatibles con V3"
        AppLanguage.FRENCH -> "Introduit dans Android 9.0+, prend en charge la rotation de clés. Certains algorithmes de certificat hérités sont incompatibles avec V3"
        AppLanguage.GERMAN -> "Eingeführt in Android 9.0+, unterstützt Schlüsselrotation. Einige veraltete Zertifikatsalgorithmen sind mit V3 inkompatibel"
        AppLanguage.RUSSIAN -> "Появился в Android 9.0+, поддерживает ротацию ключей. Некоторые устаревшие алгоритмы сертификатов несовместимы с V3"
        AppLanguage.JAPANESE -> "Android 9.0+で導入、キーローテーションをサポート。一部のレガシー証明書アルゴリズムはV3と互換性がありません"
        AppLanguage.KOREAN -> "Android 9.0+에서 도입, 키 로테이션을 지원. 일부 레거시 인증서 알고리즘은 V3와 호환되지 않습니다"
    }

    val signingSchemeAutoFallbackTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自动回退"
        AppLanguage.ENGLISH -> "Auto Fallback"
        AppLanguage.ARABIC -> "التراجع التلقائي"
        AppLanguage.PORTUGUESE -> "Fallback Automático"
        AppLanguage.SPANISH -> "Alternativa Automática"
        AppLanguage.FRENCH -> "Repli Automatique"
        AppLanguage.GERMAN -> "Automatischer Fallback"
        AppLanguage.RUSSIAN -> "Автоматический откат"
        AppLanguage.JAPANESE -> "自動フォールバック"
        AppLanguage.KOREAN -> "자동 폴백"
    }

    val signingSchemeAutoFallbackDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "若所选方案签名后校验失败，自动降级到更保守的方案以最大化兼容性。关闭则严格只用所选方案"
        AppLanguage.ENGLISH -> "If the selected schemes fail verification, automatically fall back to more conservative ones for maximum compatibility. When off, strictly uses only the selected schemes"
        AppLanguage.ARABIC -> "إذا فشل التحقق من المخططات المحددة، يتم التراجع تلقائيًا إلى مخططات أكثر تحفظًا لأقصى توافق. عند الإيقاف، يستخدم المخططات المحددة فقط بصرامة"
        AppLanguage.PORTUGUESE -> "Se os esquemas selecionados falharem na verificação, recua automaticamente para esquemas mais conservadores para máxima compatibilidade. Quando desativado, usa estritamente apenas os esquemas selecionados"
        AppLanguage.SPANISH -> "Si los esquemas seleccionados fallan la verificación, retrocede automáticamente a esquemas más conservadores para máxima compatibilidad. Cuando está desactivado, usa estrictamente solo los esquemas seleccionados"
        AppLanguage.FRENCH -> "Si les schémas sélectionnés échouent à la vérification, retour automatique à des schémas plus conservateurs pour une compatibilité maximale. Lorsqu'inactif, utilise strictement uniquement les schémas sélectionnés"
        AppLanguage.GERMAN -> "Wenn die ausgewählten Schemata die Verifikation nicht bestehen, automatischer Rückgriff auf konservativere Schemata für maximale Kompatibilität. Bei Deaktivierung werden streng nur die ausgewählten Schemata verwendet"
        AppLanguage.RUSSIAN -> "Если выбранные схемы не проходят проверку, автоматически откатывается к более консервативным для максимальной совместимости. Если выключено, строго использует только выбранные схемы"
        AppLanguage.JAPANESE -> "選択したスキームが検証に失敗した場合、最大の互換性のために自動的により保守的なスキームにフォールバックします。オフの場合、選択したスキームのみを厳格に使用します"
        AppLanguage.KOREAN -> "선택한 스킴이 검증에 실패하면 최대 호환성을 위해 자동으로 더 보수적인 스킴으로 폴백합니다. 비활성화 시 선택한 스킴만 엄격하게 사용합니다"
    }

    val signingSchemeAtLeastOne: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "至少需要启用一个签名方案"
        AppLanguage.ENGLISH -> "At least one signing scheme must be enabled"
        AppLanguage.ARABIC -> "يجب تمكين مخطط توقيع واحد على الأقل"
        AppLanguage.PORTUGUESE -> "Pelo menos um esquema de assinatura deve ser ativado"
        AppLanguage.SPANISH -> "Al menos un esquema de firma debe estar activado"
        AppLanguage.FRENCH -> "Au moins un schéma de signature doit être activé"
        AppLanguage.GERMAN -> "Mindestens ein Signaturschema muss aktiviert sein"
        AppLanguage.RUSSIAN -> "Должна быть включена хотя бы одна схема подписи"
        AppLanguage.JAPANESE -> "少なくとも1つの署名スキームを有効にする必要があります"
        AppLanguage.KOREAN -> "최소 하나의 서명 스킴을 활성화해야 합니다"
    }

    val v1SignerNameTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "V1 签名文件名"
        AppLanguage.ENGLISH -> "V1 Signature Filename"
        AppLanguage.ARABIC -> "اسم ملف توقيع V1"
        AppLanguage.PORTUGUESE -> "Nome do Arquivo de Assinatura V1"
        AppLanguage.SPANISH -> "Nombre del Archivo de Firma V1"
        AppLanguage.FRENCH -> "Nom du Fichier de Signature V1"
        AppLanguage.GERMAN -> "V1-Signaturdateiname"
        AppLanguage.RUSSIAN -> "Имя файла подписи V1"
        AppLanguage.JAPANESE -> "V1署名ファイル名"
        AppLanguage.KOREAN -> "V1 서명 파일명"
    }

    val v1SignerNameLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "签名文件基名"
        AppLanguage.ENGLISH -> "Signature basename"
        AppLanguage.ARABIC -> "الاسم الأساسي للتوقيع"
        AppLanguage.PORTUGUESE -> "Nome base da assinatura"
        AppLanguage.SPANISH -> "Nombre base de la firma"
        AppLanguage.FRENCH -> "Nom de base de la signature"
        AppLanguage.GERMAN -> "Signatur-Basisname"
        AppLanguage.RUSSIAN -> "Базовое имя подписи"
        AppLanguage.JAPANESE -> "署名ベース名"
        AppLanguage.KOREAN -> "서명 기본 이름"
    }

    val v1SignerNameHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "即 META-INF/<名称>.SF 与 .RSA 中的名称。仅含 A-Z 0-9 _ -，最长 8 位。留空则自动从签名密钥派生"
        AppLanguage.ENGLISH -> "The <name> in META-INF/<name>.SF and .RSA. Only A-Z 0-9 _ -, max 8 chars. Leave blank to auto-derive from the signing key"
        AppLanguage.ARABIC -> "الاسم في META-INF/<name>.SF و .RSA. فقط A-Z 0-9 _ -، بحد أقصى 8 أحرف. اتركه فارغًا للاشتقاق التلقائي من مفتاح التوقيع"
        AppLanguage.PORTUGUESE -> "O <name> em META-INF/<name>.SF e .RSA. Apenas A-Z 0-9 _ -, máx. 8 caracteres. Deixe em branco para derivar automaticamente da chave de assinatura"
        AppLanguage.SPANISH -> "El <name> en META-INF/<name>.SF y .RSA. Solo A-Z 0-9 _ -, máx. 8 caracteres. Déjalo en blanco para derivar automáticamente de la clave de firma"
        AppLanguage.FRENCH -> "Le <name> dans META-INF/<name>.SF et .RSA. Uniquement A-Z 0-9 _ -, max. 8 caractères. Laisser vide pour dériver automatiquement de la clé de signature"
        AppLanguage.GERMAN -> "Das <name> in META-INF/<name>.SF und .RSA. Nur A-Z 0-9 _ -, max. 8 Zeichen. Leer lassen, um automatisch vom Signaturschlüssel abzuleiten"
        AppLanguage.RUSSIAN -> "<name> в META-INF/<name>.SF и .RSA. Только A-Z 0-9 _ -, макс. 8 символов. Оставьте пустым для автоматического вывода из ключа подписи"
        AppLanguage.JAPANESE -> "META-INF/<name>.SFおよび.RSA内の<name>。A-Z 0-9 _ - のみ、最大8文字。空白で署名鍵から自動派生"
        AppLanguage.KOREAN -> "META-INF/<name>.SF 및 .RSA의 <name>. A-Z 0-9 _ -만, 최대 8자. 서명 키에서 자동 파생하려면 비워두세요"
    }

    val v1SignerNameAutoPreview: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "当前生效名称"
        AppLanguage.ENGLISH -> "Effective name"
        AppLanguage.ARABIC -> "الاسم الفعلي"
        AppLanguage.PORTUGUESE -> "Nome efetivo"
        AppLanguage.SPANISH -> "Nombre efectivo"
        AppLanguage.FRENCH -> "Nom effectif"
        AppLanguage.GERMAN -> "Effektiver Name"
        AppLanguage.RUSSIAN -> "Эффективное имя"
        AppLanguage.JAPANESE -> "有効な名前"
        AppLanguage.KOREAN -> "유효한 이름"
    }

    val signingSchemeSaved: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "签名方案已保存"
        AppLanguage.ENGLISH -> "Signing schemes saved"
        AppLanguage.ARABIC -> "تم حفظ مخططات التوقيع"
        AppLanguage.PORTUGUESE -> "Esquemas de assinatura salvos"
        AppLanguage.SPANISH -> "Esquemas de firma guardados"
        AppLanguage.FRENCH -> "Schémas de signature enregistrés"
        AppLanguage.GERMAN -> "Signaturschemata gespeichert"
        AppLanguage.RUSSIAN -> "Схемы подписи сохранены"
        AppLanguage.JAPANESE -> "署名スキームを保存しました"
        AppLanguage.KOREAN -> "서명 스킴이 저장됨"
    }

    val createKeystore: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "新建签名"
        AppLanguage.ENGLISH -> "Create Keystore"
        AppLanguage.ARABIC -> "إنشاء مخزن مفاتيح"
        AppLanguage.PORTUGUESE -> "Criar Keystore"
        AppLanguage.SPANISH -> "Crear Keystore"
        AppLanguage.FRENCH -> "Créer un Keystore"
        AppLanguage.GERMAN -> "Keystore erstellen"
        AppLanguage.RUSSIAN -> "Создать Keystore"
        AppLanguage.JAPANESE -> "Keystoreを作成"
        AppLanguage.KOREAN -> "Keystore 생성"
    }

    val createKeystoreNote: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "在本机生成一个全新的签名证书，无需电脑或 keytool"
        AppLanguage.ENGLISH -> "Generate a brand-new signing certificate on-device, no PC or keytool needed"
        AppLanguage.ARABIC -> "أنشئ شهادة توقيع جديدة تمامًا على الجهاز، دون الحاجة إلى كمبيوتر أو keytool"
        AppLanguage.PORTUGUESE -> "Gere um certificado de assinatura novo no dispositivo, sem PC ou keytool"
        AppLanguage.SPANISH -> "Genera un certificado de firma nuevo en el dispositivo, sin PC ni keytool"
        AppLanguage.FRENCH -> "Générez un tout nouveau certificat de signature sur l'appareil, sans PC ni keytool"
        AppLanguage.GERMAN -> "Generiere ein brandneues Signaturzertifikat auf dem Gerät, kein PC oder keytool nötig"
        AppLanguage.RUSSIAN -> "Создайте новый сертификат подписи на устройстве, без ПК или keytool"
        AppLanguage.JAPANESE -> "デバイス上で新しい署名証明書を生成。PCやkeytoolは不要"
        AppLanguage.KOREAN -> "기기에서 완전히 새로운 서명 인증서를 생성. PC나 keytool 불필요"
    }

    val certAlias: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "密钥别名"
        AppLanguage.ENGLISH -> "Key Alias"
        AppLanguage.ARABIC -> "اسم المفتاح المستعار"
        AppLanguage.PORTUGUESE -> "Alias da Chave"
        AppLanguage.SPANISH -> "Alias de la Clave"
        AppLanguage.FRENCH -> "Alias de la Clé"
        AppLanguage.GERMAN -> "Schlüssel-Alias"
        AppLanguage.RUSSIAN -> "Псевдоним ключа"
        AppLanguage.JAPANESE -> "キーエイリアス"
        AppLanguage.KOREAN -> "키 별칭"
    }

    val certCommonName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "通用名 (CN)"
        AppLanguage.ENGLISH -> "Common Name (CN)"
        AppLanguage.ARABIC -> "الاسم الشائع (CN)"
        AppLanguage.PORTUGUESE -> "Nome Comum (CN)"
        AppLanguage.SPANISH -> "Nombre Común (CN)"
        AppLanguage.FRENCH -> "Nom Commun (CN)"
        AppLanguage.GERMAN -> "Allgemeiner Name (CN)"
        AppLanguage.RUSSIAN -> "Общее имя (CN)"
        AppLanguage.JAPANESE -> "コモンネーム (CN)"
        AppLanguage.KOREAN -> "공통 이름 (CN)"
    }

    val certOrganization: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "组织 (O)"
        AppLanguage.ENGLISH -> "Organization (O)"
        AppLanguage.ARABIC -> "المنظمة (O)"
        AppLanguage.PORTUGUESE -> "Organização (O)"
        AppLanguage.SPANISH -> "Organización (O)"
        AppLanguage.FRENCH -> "Organisation (O)"
        AppLanguage.GERMAN -> "Organisation (O)"
        AppLanguage.RUSSIAN -> "Организация (O)"
        AppLanguage.JAPANESE -> "組織 (O)"
        AppLanguage.KOREAN -> "조직 (O)"
    }

    val certOrganizationUnit: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "组织单元 (OU)"
        AppLanguage.ENGLISH -> "Organizational Unit (OU)"
        AppLanguage.ARABIC -> "الوحدة التنظيمية (OU)"
        AppLanguage.PORTUGUESE -> "Unidade Organizacional (OU)"
        AppLanguage.SPANISH -> "Unidad Organizativa (OU)"
        AppLanguage.FRENCH -> "Unité d'Organisation (OU)"
        AppLanguage.GERMAN -> "Organisationseinheit (OU)"
        AppLanguage.RUSSIAN -> "Подразделение (OU)"
        AppLanguage.JAPANESE -> "組織単位 (OU)"
        AppLanguage.KOREAN -> "조직 단위 (OU)"
    }

    val certLocality: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "城市 (L)"
        AppLanguage.ENGLISH -> "City / Locality (L)"
        AppLanguage.ARABIC -> "المدينة (L)"
        AppLanguage.PORTUGUESE -> "Cidade / Localidade (L)"
        AppLanguage.SPANISH -> "Ciudad / Localidad (L)"
        AppLanguage.FRENCH -> "Ville / Localité (L)"
        AppLanguage.GERMAN -> "Stadt / Ort (L)"
        AppLanguage.RUSSIAN -> "Город / Населённый пункт (L)"
        AppLanguage.JAPANESE -> "市 / 地域 (L)"
        AppLanguage.KOREAN -> "시/지역 (L)"
    }

    val certState: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "省/州 (ST)"
        AppLanguage.ENGLISH -> "State / Province (ST)"
        AppLanguage.ARABIC -> "الولاية / المقاطعة (ST)"
        AppLanguage.PORTUGUESE -> "Estado / Província (ST)"
        AppLanguage.SPANISH -> "Estado / Provincia (ST)"
        AppLanguage.FRENCH -> "État / Province (ST)"
        AppLanguage.GERMAN -> "Bundesland / Provinz (ST)"
        AppLanguage.RUSSIAN -> "Штат / Провинция (ST)"
        AppLanguage.JAPANESE -> "州 / 都道府県 (ST)"
        AppLanguage.KOREAN -> "주/도 (ST)"
    }

    val certCountry: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "国家码 (C)"
        AppLanguage.ENGLISH -> "Country Code (C)"
        AppLanguage.ARABIC -> "رمز الدولة (C)"
        AppLanguage.PORTUGUESE -> "Código do País (C)"
        AppLanguage.SPANISH -> "Código de País (C)"
        AppLanguage.FRENCH -> "Code Pays (C)"
        AppLanguage.GERMAN -> "Ländercode (C)"
        AppLanguage.RUSSIAN -> "Код страны (C)"
        AppLanguage.JAPANESE -> "国コード (C)"
        AppLanguage.KOREAN -> "국가 코드 (C)"
    }

    val certValidityYears: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "有效期（年）"
        AppLanguage.ENGLISH -> "Validity (years)"
        AppLanguage.ARABIC -> "مدة الصلاحية (سنوات)"
        AppLanguage.PORTUGUESE -> "Validade (anos)"
        AppLanguage.SPANISH -> "Validez (años)"
        AppLanguage.FRENCH -> "Validité (années)"
        AppLanguage.GERMAN -> "Gültigkeit (Jahre)"
        AppLanguage.RUSSIAN -> "Срок действия (лет)"
        AppLanguage.JAPANESE -> "有効期限（年）"
        AppLanguage.KOREAN -> "유효 기간 (년)"
    }

    val certKeySize: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "密钥长度"
        AppLanguage.ENGLISH -> "Key Size"
        AppLanguage.ARABIC -> "حجم المفتاح"
        AppLanguage.PORTUGUESE -> "Tamanho da Chave"
        AppLanguage.SPANISH -> "Tamaño de Clave"
        AppLanguage.FRENCH -> "Taille de Clé"
        AppLanguage.GERMAN -> "Schlüsselgröße"
        AppLanguage.RUSSIAN -> "Размер ключа"
        AppLanguage.JAPANESE -> "キーサイズ"
        AppLanguage.KOREAN -> "키 크기"
    }

    val certCreateSuccess: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "签名证书已生成并启用"
        AppLanguage.ENGLISH -> "Certificate created and activated"
        AppLanguage.ARABIC -> "تم إنشاء الشهادة وتفعيلها"
        AppLanguage.PORTUGUESE -> "Certificado criado e ativado"
        AppLanguage.SPANISH -> "Certificado creado y activado"
        AppLanguage.FRENCH -> "Certificat créé et activé"
        AppLanguage.GERMAN -> "Zertifikat erstellt und aktiviert"
        AppLanguage.RUSSIAN -> "Сертификат создан и активирован"
        AppLanguage.JAPANESE -> "証明書を作成して有効化しました"
        AppLanguage.KOREAN -> "인증서가 생성되고 활성화되었습니다"
    }

    val certCreateFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "签名证书生成失败，请检查别名与密码"
        AppLanguage.ENGLISH -> "Certificate creation failed. Check the alias and password"
        AppLanguage.ARABIC -> "فشل إنشاء الشهادة. تحقق من الاسم المستعار وكلمة المرور"
        AppLanguage.PORTUGUESE -> "Falha na criação do certificado. Verifique o alias e a senha"
        AppLanguage.SPANISH -> "Error al crear el certificado. Verifica el alias y la contraseña"
        AppLanguage.FRENCH -> "Échec de la création du certificat. Vérifiez l'alias et le mot de passe"
        AppLanguage.GERMAN -> "Zertifikatserstellung fehlgeschlagen. Alias und Passwort prüfen"
        AppLanguage.RUSSIAN -> "Ошибка создания сертификата. Проверьте псевдоним и пароль"
        AppLanguage.JAPANESE -> "証明書の作成に失敗しました。エイリアスとパスワードを確認してください"
        AppLanguage.KOREAN -> "인증서 생성 실패. 별칭과 비밀번호를 확인하세요"
    }

    val certAliasPasswordRequired: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "别名和密码不能为空"
        AppLanguage.ENGLISH -> "Alias and password are required"
        AppLanguage.ARABIC -> "الاسم المستعار وكلمة المرور مطلوبان"
        AppLanguage.PORTUGUESE -> "Alias e senha são obrigatórios"
        AppLanguage.SPANISH -> "El alias y la contraseña son obligatorios"
        AppLanguage.FRENCH -> "L'alias et le mot de passe sont obligatoires"
        AppLanguage.GERMAN -> "Alias und Passwort sind erforderlich"
        AppLanguage.RUSSIAN -> "Псевдоним и пароль обязательны"
        AppLanguage.JAPANESE -> "エイリアスとパスワードは必須です"
        AppLanguage.KOREAN -> "별칭과 비밀번호는 필수입니다"
    }

    val viewFingerprints: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "查看签名指纹"
        AppLanguage.ENGLISH -> "View Fingerprints"
        AppLanguage.ARABIC -> "عرض البصمات"
        AppLanguage.PORTUGUESE -> "Ver Impressões Digitais"
        AppLanguage.SPANISH -> "Ver Huellas"
        AppLanguage.FRENCH -> "Voir les Empreintes"
        AppLanguage.GERMAN -> "Fingerabdrücke anzeigen"
        AppLanguage.RUSSIAN -> "Просмотреть отпечатки"
        AppLanguage.JAPANESE -> "フィンガープリントを表示"
        AppLanguage.KOREAN -> "핑거프린트 보기"
    }

    val fingerprintCopied: String get() = when (Strings.lang) {
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

    val userAgentMode: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "浏览器伪装"
        AppLanguage.ENGLISH -> "Browser Disguise"
        AppLanguage.ARABIC -> "تمويه المتصفح"
        AppLanguage.PORTUGUESE -> "Disfarce de Navegador"
        AppLanguage.SPANISH -> "Disfraz de Navegador"
        AppLanguage.FRENCH -> "Déguisement de Navigateur"
        AppLanguage.GERMAN -> "Browser-Tarnung"
        AppLanguage.RUSSIAN -> "Маскировка браузера"
        AppLanguage.JAPANESE -> "ブラウザ偽装"
        AppLanguage.KOREAN -> "브라우저 위장"
    }

    val userAgentDefault: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "系统默认"
        AppLanguage.ENGLISH -> "System Default"
        AppLanguage.ARABIC -> "افتراضي النظام"
        AppLanguage.PORTUGUESE -> "Padrão do Sistema"
        AppLanguage.SPANISH -> "Predeterminado del Sistema"
        AppLanguage.FRENCH -> "Par Défaut du Système"
        AppLanguage.GERMAN -> "Systemstandard"
        AppLanguage.RUSSIAN -> "Системный по умолчанию"
        AppLanguage.JAPANESE -> "システムデフォルト"
        AppLanguage.KOREAN -> "시스템 기본값"
    }

    val userAgentCustom: String get() = when (Strings.lang) {
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

    val userAgentCustomHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "输入自定义 User-Agent 字符串"
        AppLanguage.ENGLISH -> "Enter custom User-Agent string"
        AppLanguage.ARABIC -> "أدخل سلسلة User-Agent مخصصة"
        AppLanguage.PORTUGUESE -> "Digite a string personalizada de User-Agent"
        AppLanguage.SPANISH -> "Introduce la cadena personalizada de User-Agent"
        AppLanguage.FRENCH -> "Saisir la chaîne User-Agent personnalisée"
        AppLanguage.GERMAN -> "Benutzerdefinierte User-Agent-Zeichenkette eingeben"
        AppLanguage.RUSSIAN -> "Введите пользовательскую строку User-Agent"
        AppLanguage.JAPANESE -> "カスタムUser-Agent文字列を入力"
        AppLanguage.KOREAN -> "커스텀 User-Agent 문자열 입력"
    }

    val mobileVersion: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "移动版"
        AppLanguage.ENGLISH -> "Mobile"
        AppLanguage.ARABIC -> "للجوال"
        AppLanguage.PORTUGUESE -> "Móvel"
        AppLanguage.SPANISH -> "Móvil"
        AppLanguage.FRENCH -> "Mobile"
        AppLanguage.GERMAN -> "Mobil"
        AppLanguage.RUSSIAN -> "Мобильная"
        AppLanguage.JAPANESE -> "モバイル"
        AppLanguage.KOREAN -> "모바일"
    }

    val desktopVersion: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "桌面版"
        AppLanguage.ENGLISH -> "Desktop"
        AppLanguage.ARABIC -> "للكمبيوتر"
        AppLanguage.PORTUGUESE -> "Área de trabalho"
        AppLanguage.SPANISH -> "Escritorio"
        AppLanguage.FRENCH -> "Bureau"
        AppLanguage.GERMAN -> "Desktop"
        AppLanguage.RUSSIAN -> "Десктоп"
        AppLanguage.JAPANESE -> "デスクトップ"
        AppLanguage.KOREAN -> "데스크톱"
    }

    val desktopModeLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "桌面模式"
        AppLanguage.ENGLISH -> "Desktop mode"
        AppLanguage.ARABIC -> "وضع سطح المكتب"
        AppLanguage.PORTUGUESE -> "Modo área de trabalho"
        AppLanguage.SPANISH -> "Modo de escritorio"
        AppLanguage.FRENCH -> "Mode bureau"
        AppLanguage.GERMAN -> "Desktop-Modus"
        AppLanguage.RUSSIAN -> "Режим рабочего стола"
        AppLanguage.JAPANESE -> "デスクトップモード"
        AppLanguage.KOREAN -> "데스크톱 모드"
    }

    val currentUserAgent: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "当前 User-Agent"
        AppLanguage.ENGLISH -> "Current User-Agent"
        AppLanguage.ARABIC -> "User-Agent الحالي"
        AppLanguage.PORTUGUESE -> "User-Agent Atual"
        AppLanguage.SPANISH -> "User-Agent Actual"
        AppLanguage.FRENCH -> "User-Agent Actuel"
        AppLanguage.GERMAN -> "Aktueller User-Agent"
        AppLanguage.RUSSIAN -> "Текущий User-Agent"
        AppLanguage.JAPANESE -> "現在のUser-Agent"
        AppLanguage.KOREAN -> "현재 User-Agent"
    }

    val bypassWebViewDetection: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "部分网站会检测 WebView 并阻止访问，选择浏览器伪装可绕过检测"
        AppLanguage.ENGLISH -> "Some sites detect WebView and block access. Browser disguise can bypass detection"
        AppLanguage.ARABIC -> "تكتشف بعض المواقع WebView وتحظر الوصول. يمكن لتمويه المتصفح تجاوز الاكتشاف"
        AppLanguage.PORTUGUESE -> "Alguns sites detectam o WebView e bloqueiam o acesso. O disfarce de navegador pode contornar a detecção"
        AppLanguage.SPANISH -> "Algunos sitios detectan el WebView y bloquean el acceso. El disfraz de navegador puede evitar la detección"
        AppLanguage.FRENCH -> "Certains sites détectent le WebView et bloquent l'accès. Le déguisement de navigateur peut contourner la détection"
        AppLanguage.GERMAN -> "Einige Seiten erkennen WebView und blockieren den Zugriff. Browser-Tarnung kann die Erkennung umgehen"
        AppLanguage.RUSSIAN -> "Некоторые сайты обнаруживают WebView и блокируют доступ. Маскировка браузера может обойти обнаружение"
        AppLanguage.JAPANESE -> "一部のサイトはWebViewを検出してアクセスをブロックします。ブラウザ偽装で検出を回避できます"
        AppLanguage.KOREAN -> "일부 사이트는 WebView를 감지하고 접근을 차단합니다. 브라우저 위장으로 감지를 우회할 수 있습니다"
    }

    val fileLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "文件：%s"
        AppLanguage.ENGLISH -> "File: %s"
        AppLanguage.ARABIC -> "الملف: %s"
        AppLanguage.PORTUGUESE -> "Arquivo: %s"
        AppLanguage.SPANISH -> "Archivo: %s"
        AppLanguage.FRENCH -> "Fichier : %s"
        AppLanguage.GERMAN -> "Datei: %s"
        AppLanguage.RUSSIAN -> "Файл: %s"
        AppLanguage.JAPANESE -> "ファイル: %s"
        AppLanguage.KOREAN -> "파일: %s"
    }

    val clickToSelectFile: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "点击选择文件"
        AppLanguage.ENGLISH -> "Click to select file"
        AppLanguage.ARABIC -> "انقر لاختيار الملف"
        AppLanguage.PORTUGUESE -> "Clique para selecionar arquivo"
        AppLanguage.SPANISH -> "Haz clic para seleccionar archivo"
        AppLanguage.FRENCH -> "Cliquez pour sélectionner le fichier"
        AppLanguage.GERMAN -> "Klicken, um Datei auszuwählen"
        AppLanguage.RUSSIAN -> "Нажмите, чтобы выбрать файл"
        AppLanguage.JAPANESE -> "クリックしてファイルを選択"
        AppLanguage.KOREAN -> "클릭하여 파일 선택"
    }

    val clearFile: String get() = when (Strings.lang) {
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

    val dayMon: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "一"
        AppLanguage.ENGLISH -> "Mon"
        AppLanguage.ARABIC -> "الإثنين"
        AppLanguage.PORTUGUESE -> "Seg"
        AppLanguage.SPANISH -> "Lun"
        AppLanguage.FRENCH -> "Lun"
        AppLanguage.GERMAN -> "Mo"
        AppLanguage.RUSSIAN -> "Пн"
        AppLanguage.JAPANESE -> "月"
        AppLanguage.KOREAN -> "월"
    }

    val dayTue: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "二"
        AppLanguage.ENGLISH -> "Tue"
        AppLanguage.ARABIC -> "الثلاثاء"
        AppLanguage.PORTUGUESE -> "Ter"
        AppLanguage.SPANISH -> "Mar"
        AppLanguage.FRENCH -> "Mar"
        AppLanguage.GERMAN -> "Di"
        AppLanguage.RUSSIAN -> "Вт"
        AppLanguage.JAPANESE -> "火"
        AppLanguage.KOREAN -> "화"
    }

    val dayWed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "三"
        AppLanguage.ENGLISH -> "Wed"
        AppLanguage.ARABIC -> "الأربعاء"
        AppLanguage.PORTUGUESE -> "Qua"
        AppLanguage.SPANISH -> "Mié"
        AppLanguage.FRENCH -> "Mer"
        AppLanguage.GERMAN -> "Mi"
        AppLanguage.RUSSIAN -> "Ср"
        AppLanguage.JAPANESE -> "水"
        AppLanguage.KOREAN -> "수"
    }

    val dayThu: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "四"
        AppLanguage.ENGLISH -> "Thu"
        AppLanguage.ARABIC -> "الخميس"
        AppLanguage.PORTUGUESE -> "Qui"
        AppLanguage.SPANISH -> "Jue"
        AppLanguage.FRENCH -> "Jeu"
        AppLanguage.GERMAN -> "Do"
        AppLanguage.RUSSIAN -> "Чт"
        AppLanguage.JAPANESE -> "木"
        AppLanguage.KOREAN -> "목"
    }

    val dayFri: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "五"
        AppLanguage.ENGLISH -> "Fri"
        AppLanguage.ARABIC -> "الجمعة"
        AppLanguage.PORTUGUESE -> "Sex"
        AppLanguage.SPANISH -> "Vie"
        AppLanguage.FRENCH -> "Ven"
        AppLanguage.GERMAN -> "Fr"
        AppLanguage.RUSSIAN -> "Пт"
        AppLanguage.JAPANESE -> "金"
        AppLanguage.KOREAN -> "금"
    }

    val daySat: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "六"
        AppLanguage.ENGLISH -> "Sat"
        AppLanguage.ARABIC -> "السبت"
        AppLanguage.PORTUGUESE -> "Sáb"
        AppLanguage.SPANISH -> "Sáb"
        AppLanguage.FRENCH -> "Sam"
        AppLanguage.GERMAN -> "Sa"
        AppLanguage.RUSSIAN -> "Сб"
        AppLanguage.JAPANESE -> "土"
        AppLanguage.KOREAN -> "토"
    }

    val daySun: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "日"
        AppLanguage.ENGLISH -> "Sun"
        AppLanguage.ARABIC -> "الأحد"
        AppLanguage.PORTUGUESE -> "Dom"
        AppLanguage.SPANISH -> "Dom"
        AppLanguage.FRENCH -> "Dim"
        AppLanguage.GERMAN -> "So"
        AppLanguage.RUSSIAN -> "Вс"
        AppLanguage.JAPANESE -> "日"
        AppLanguage.KOREAN -> "일"
    }

    val shareApk: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "分享 APK"
        AppLanguage.ENGLISH -> "Share APK"
        AppLanguage.ARABIC -> "مشاركة APK"
        AppLanguage.PORTUGUESE -> "Compartilhar APK"
        AppLanguage.SPANISH -> "Compartir APK"
        AppLanguage.FRENCH -> "Partager APK"
        AppLanguage.GERMAN -> "APK teilen"
        AppLanguage.RUSSIAN -> "Поделиться APK"
        AppLanguage.JAPANESE -> "APKを共有"
        AppLanguage.KOREAN -> "APK 공유"
    }

    val shareApkBuilding: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "正在构建 APK..."
        AppLanguage.ENGLISH -> "Building APK..."
        AppLanguage.ARABIC -> "جاري بناء APK..."
        AppLanguage.PORTUGUESE -> "Construindo APK..."
        AppLanguage.SPANISH -> "Construyendo APK..."
        AppLanguage.FRENCH -> "Construction de l'APK..."
        AppLanguage.GERMAN -> "APK wird erstellt..."
        AppLanguage.RUSSIAN -> "Сборка APK..."
        AppLanguage.JAPANESE -> "APKを構築中..."
        AppLanguage.KOREAN -> "APK 빌드 중..."
    }

    val shareApkFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "构建 APK 失败: %s"
        AppLanguage.ENGLISH -> "Failed to build APK: %s"
        AppLanguage.ARABIC -> "فشل بناء APK: %s"
        AppLanguage.PORTUGUESE -> "Falha ao construir APK: %s"
        AppLanguage.SPANISH -> "Error al construir APK: %s"
        AppLanguage.FRENCH -> "Échec de la construction de l'APK : %s"
        AppLanguage.GERMAN -> "APK-Erstellung fehlgeschlagen: %s"
        AppLanguage.RUSSIAN -> "Ошибка сборки APK: %s"
        AppLanguage.JAPANESE -> "APKの構築に失敗: %s"
        AppLanguage.KOREAN -> "APK 빌드 실패: %s"
    }

    val shareApkTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "分享 %s 应用"
        AppLanguage.ENGLISH -> "Share %s app"
        AppLanguage.ARABIC -> "مشاركة تطبيق %s"
        AppLanguage.PORTUGUESE -> "Compartilhar app %s"
        AppLanguage.SPANISH -> "Compartir app %s"
        AppLanguage.FRENCH -> "Partager l'app %s"
        AppLanguage.GERMAN -> "App %s teilen"
        AppLanguage.RUSSIAN -> "Поделиться приложением %s"
        AppLanguage.JAPANESE -> "%sアプリを共有"
        AppLanguage.KOREAN -> "%s 앱 공유"
    }

    val minutesShort: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "分"
        AppLanguage.ENGLISH -> "min"
        AppLanguage.ARABIC -> "د"
        AppLanguage.PORTUGUESE -> "min"
        AppLanguage.SPANISH -> "min"
        AppLanguage.FRENCH -> "min"
        AppLanguage.GERMAN -> "Min."
        AppLanguage.RUSSIAN -> "мин"
        AppLanguage.JAPANESE -> "分"
        AppLanguage.KOREAN -> "분"
    }

    val floatingWindowTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "悬浮小窗"
        AppLanguage.ENGLISH -> "Floating Window"
        AppLanguage.ARABIC -> "النافذة العائمة"
        AppLanguage.PORTUGUESE -> "Janela Flutuante"
        AppLanguage.SPANISH -> "Ventana Flotante"
        AppLanguage.FRENCH -> "Fenêtre Flottante"
        AppLanguage.GERMAN -> "Schwebendes Fenster"
        AppLanguage.RUSSIAN -> "Плавающее окно"
        AppLanguage.JAPANESE -> "フローティングウィンドウ"
        AppLanguage.KOREAN -> "플로팅 창"
    }

    val floatingWindowOpacity: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "透明度"
        AppLanguage.ENGLISH -> "Opacity"
        AppLanguage.ARABIC -> "الشفافية"
        AppLanguage.PORTUGUESE -> "Opacidade"
        AppLanguage.SPANISH -> "Opacidad"
        AppLanguage.FRENCH -> "Opacité"
        AppLanguage.GERMAN -> "Deckkraft"
        AppLanguage.RUSSIAN -> "Непрозрачность"
        AppLanguage.JAPANESE -> "不透明度"
        AppLanguage.KOREAN -> "불투명도"
    }

    val floatingWindowShowTitleBar: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "显示标题栏"
        AppLanguage.ENGLISH -> "Show Title Bar"
        AppLanguage.ARABIC -> "عرض شريط العنوان"
        AppLanguage.PORTUGUESE -> "Mostrar Barra de Título"
        AppLanguage.SPANISH -> "Mostrar Barra de Título"
        AppLanguage.FRENCH -> "Afficher la Barre de Titre"
        AppLanguage.GERMAN -> "Titelleiste anzeigen"
        AppLanguage.RUSSIAN -> "Показывать строку заголовка"
        AppLanguage.JAPANESE -> "タイトルバーを表示"
        AppLanguage.KOREAN -> "제목 표시줄 표시"
    }

    val floatingWindowShowTitleBarDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "显示标题栏，并提供后退、前进、全屏、最小化和关闭按钮"
        AppLanguage.ENGLISH -> "Show the title bar with Back, Forward, Fullscreen, Minimize, and Close controls"
        AppLanguage.ARABIC -> "عرض شريط العنوان مع أزرار الرجوع والتقدم وملء الشاشة والتصغير والإغلاق"
        AppLanguage.PORTUGUESE -> "Mostrar barra de título com controles Voltar, Avançar, Tela cheia, Minimizar e Fechar"
        AppLanguage.SPANISH -> "Mostrar barra de título con controles Atrás, Adelante, Pantalla completa, Minimizar y Cerrar"
        AppLanguage.FRENCH -> "Afficher la barre de titre avec les contrôles Précédent, Suivant, Plein écran, Minimiser et Fermer"
        AppLanguage.GERMAN -> "Titelleiste mit Zurück, Vorwärts, Vollbild, Minimieren und Schließen anzeigen"
        AppLanguage.RUSSIAN -> "Показывать строку заголовка с кнопками «Назад», «Вперёд», «Полный экран», «Свернуть» и «Закрыть»"
        AppLanguage.JAPANESE -> "タイトルバーに戻る、進む、全画面、最小化、閉じるボタンを表示"
        AppLanguage.KOREAN -> "제목 표시줄에 뒤로, 앞으로, 전체화면, 최소화, 닫기 컨트롤 표시"
    }

    val floatingWindowStartMinimized: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "启动时最小化"
        AppLanguage.ENGLISH -> "Start Minimized"
        AppLanguage.ARABIC -> "البدء مصغرًا"
        AppLanguage.PORTUGUESE -> "Iniciar Minimizado"
        AppLanguage.SPANISH -> "Iniciar Minimizado"
        AppLanguage.FRENCH -> "Démarrer Minimisé"
        AppLanguage.GERMAN -> "Minimiert starten"
        AppLanguage.RUSSIAN -> "Запускать свёрнутым"
        AppLanguage.JAPANESE -> "最小化で開始"
        AppLanguage.KOREAN -> "최소화하여 시작"
    }

    val floatingWindowStartMinimizedDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "启动时自动最小化为悬浮按钮，点击展开"
        AppLanguage.ENGLISH -> "Auto minimize to floating button on start, tap to expand"
        AppLanguage.ARABIC -> "تصغير تلقائي إلى زر عائم عند البدء، انقر للتوسيع"
        AppLanguage.PORTUGUESE -> "Minimizar automaticamente para botão flutuante ao iniciar, toque para expandir"
        AppLanguage.SPANISH -> "Minimizar automáticamente a botón flotante al iniciar, toca para expandir"
        AppLanguage.FRENCH -> "Minimiser automatiquement en bouton flottant au démarrage, toucher pour agrandir"
        AppLanguage.GERMAN -> "Beim Start automatisch zu schwebendem Button minimieren, tippen zum Erweitern"
        AppLanguage.RUSSIAN -> "Автоматически сворачивать в плавающую кнопку при запуске, нажмите для разворачивания"
        AppLanguage.JAPANESE -> "起動時に自動的にフローティングボタンに最小化、タップで展開"
        AppLanguage.KOREAN -> "시작 시 자동으로 플로팅 버튼으로 최소화, 탭하여 확장"
    }

    val floatingWindowRememberPosition: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "记住位置"
        AppLanguage.ENGLISH -> "Remember Position"
        AppLanguage.ARABIC -> "تذكر الموضع"
        AppLanguage.PORTUGUESE -> "Lembrar Posição"
        AppLanguage.SPANISH -> "Recordar Posición"
        AppLanguage.FRENCH -> "Mémoriser la Position"
        AppLanguage.GERMAN -> "Position merken"
        AppLanguage.RUSSIAN -> "Запомнить позицию"
        AppLanguage.JAPANESE -> "位置を記憶"
        AppLanguage.KOREAN -> "위치 기억"
    }

    val floatingWindowRememberPositionDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下次打开时恢复到上次窗口位置"
        AppLanguage.ENGLISH -> "Restore to last window position on next open"
        AppLanguage.ARABIC -> "الاستعادة إلى آخر موضع للنافذة عند الفتح التالي"
        AppLanguage.PORTUGUESE -> "Restaurar para a última posição da janela na próxima abertura"
        AppLanguage.SPANISH -> "Restaurar a la última posición de la ventana en la próxima apertura"
        AppLanguage.FRENCH -> "Restaurer à la dernière position de la fenêtre à la prochaine ouverture"
        AppLanguage.GERMAN -> "Beim nächsten Öffnen an letzte Fensterposition wiederherstellen"
        AppLanguage.RUSSIAN -> "Восстановить последнюю позицию окна при следующем открытии"
        AppLanguage.JAPANESE -> "次回オープン時に最後のウィンドウ位置を復元"
        AppLanguage.KOREAN -> "다음 열 때 마지막 창 위치로 복원"
    }

    val floatingWindowNotificationChannel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "悬浮窗服务"
        AppLanguage.ENGLISH -> "Floating Window Service"
        AppLanguage.ARABIC -> "خدمة النافذة العائمة"
        AppLanguage.PORTUGUESE -> "Serviço de Janela Flutuante"
        AppLanguage.SPANISH -> "Servicio de Ventana Flotante"
        AppLanguage.FRENCH -> "Service de Fenêtre Flottante"
        AppLanguage.GERMAN -> "Schwebendes-Fenster-Dienst"
        AppLanguage.RUSSIAN -> "Служба плавающего окна"
        AppLanguage.JAPANESE -> "フローティングウィンドウサービス"
        AppLanguage.KOREAN -> "플로팅 창 서비스"
    }

    val floatingWindowNotificationChannelDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "悬浮小窗运行时的通知"
        AppLanguage.ENGLISH -> "Notifications for floating window service"
        AppLanguage.ARABIC -> "إشعارات خدمة النافذة العائمة"
        AppLanguage.PORTUGUESE -> "Notificações do serviço de janela flutuante"
        AppLanguage.SPANISH -> "Notificaciones del servicio de ventana flotante"
        AppLanguage.FRENCH -> "Notifications du service de fenêtre flottante"
        AppLanguage.GERMAN -> "Benachrichtigungen für den schwebendes-Fenster-Dienst"
        AppLanguage.RUSSIAN -> "Уведомления службы плавающего окна"
        AppLanguage.JAPANESE -> "フローティングウィンドウサービスの通知"
        AppLanguage.KOREAN -> "플로팅 창 서비스 알림"
    }

    val floatingWindowNotificationTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "悬浮窗运行中"
        AppLanguage.ENGLISH -> "Floating Window Active"
        AppLanguage.ARABIC -> "النافذة العائمة نشطة"
        AppLanguage.PORTUGUESE -> "Janela Flutuante Ativa"
        AppLanguage.SPANISH -> "Ventana Flotante Activa"
        AppLanguage.FRENCH -> "Fenêtre Flottante Active"
        AppLanguage.GERMAN -> "Schwebendes Fenster aktiv"
        AppLanguage.RUSSIAN -> "Плавающее окно активно"
        AppLanguage.JAPANESE -> "フローティングウィンドウ動作中"
        AppLanguage.KOREAN -> "플로팅 창 활성"
    }

    val floatingWindowNotificationContent: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "%s 正在悬浮窗中运行"
        AppLanguage.ENGLISH -> "%s is running in floating window"
        AppLanguage.ARABIC -> "%s يعمل في النافذة العائمة"
        AppLanguage.PORTUGUESE -> "%s está rodando em janela flutuante"
        AppLanguage.SPANISH -> "%s se está ejecutando en ventana flotante"
        AppLanguage.FRENCH -> "%s s'exécute dans la fenêtre flottante"
        AppLanguage.GERMAN -> "%s läuft im schwebenden Fenster"
        AppLanguage.RUSSIAN -> "%s работает в плавающем окне"
        AppLanguage.JAPANESE -> "%sはフローティングウィンドウで実行中"
        AppLanguage.KOREAN -> "%s이(가) 플로팅 창에서 실행 중"
    }

    val floatingWindowNotificationContentDefault: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "应用正在悬浮窗中运行"
        AppLanguage.ENGLISH -> "App is running in floating window"
        AppLanguage.ARABIC -> "التطبيق يعمل في النافذة العائمة"
        AppLanguage.PORTUGUESE -> "App está rodando em janela flutuante"
        AppLanguage.SPANISH -> "La app se está ejecutando en ventana flotante"
        AppLanguage.FRENCH -> "L'app s'exécute dans la fenêtre flottante"
        AppLanguage.GERMAN -> "App läuft im schwebenden Fenster"
        AppLanguage.RUSSIAN -> "Приложение работает в плавающем окне"
        AppLanguage.JAPANESE -> "アプリはフローティングウィンドウで実行中"
        AppLanguage.KOREAN -> "앱이 플로팅 창에서 실행 중"
    }

    val floatingWindowClose: String get() = when (Strings.lang) {
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

    val floatingWindowMinimize: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "最小化"
        AppLanguage.ENGLISH -> "Minimize"
        AppLanguage.ARABIC -> "تصغير"
        AppLanguage.PORTUGUESE -> "Minimizar"
        AppLanguage.SPANISH -> "Minimizar"
        AppLanguage.FRENCH -> "Minimiser"
        AppLanguage.GERMAN -> "Minimieren"
        AppLanguage.RUSSIAN -> "Свернуть"
        AppLanguage.JAPANESE -> "最小化"
        AppLanguage.KOREAN -> "최소화"
    }

    val floatingWindowRestoreWindow: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "恢复窗口"
        AppLanguage.ENGLISH -> "Restore Window"
        AppLanguage.ARABIC -> "استعادة النافذة"
        AppLanguage.PORTUGUESE -> "Restaurar Janela"
        AppLanguage.SPANISH -> "Restaurar Ventana"
        AppLanguage.FRENCH -> "Restaurer la Fenêtre"
        AppLanguage.GERMAN -> "Fenster wiederherstellen"
        AppLanguage.RUSSIAN -> "Восстановить окно"
        AppLanguage.JAPANESE -> "ウィンドウを復元"
        AppLanguage.KOREAN -> "창 복원"
    }

    val floatingWindowEnterFullscreen: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "进入全屏"
        AppLanguage.ENGLISH -> "Enter Fullscreen"
        AppLanguage.ARABIC -> "دخول ملء الشاشة"
        AppLanguage.PORTUGUESE -> "Entrar em Tela Cheia"
        AppLanguage.SPANISH -> "Entrar en Pantalla Completa"
        AppLanguage.FRENCH -> "Entrer en Plein Écran"
        AppLanguage.GERMAN -> "Vollbild aktivieren"
        AppLanguage.RUSSIAN -> "Войти в полноэкранный режим"
        AppLanguage.JAPANESE -> "全画面へ"
        AppLanguage.KOREAN -> "전체화면 진입"
    }

    val floatingWindowExitFullscreen: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "退出全屏"
        AppLanguage.ENGLISH -> "Exit Fullscreen"
        AppLanguage.ARABIC -> "الخروج من ملء الشاشة"
        AppLanguage.PORTUGUESE -> "Sair de Tela Cheia"
        AppLanguage.SPANISH -> "Salir de Pantalla Completa"
        AppLanguage.FRENCH -> "Quitter le Plein Écran"
        AppLanguage.GERMAN -> "Vollbild verlassen"
        AppLanguage.RUSSIAN -> "Выйти из полноэкранного режима"
        AppLanguage.JAPANESE -> "全画面を終了"
        AppLanguage.KOREAN -> "전체화면 종료"
    }

    val floatingWindowPermissionRequired: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "悬浮窗需要\"显示在其他应用上层\"权限"
        AppLanguage.ENGLISH -> "Floating window requires \"Display over other apps\" permission"
        AppLanguage.ARABIC -> "تتطلب النافذة العائمة إذن \"العرض فوق التطبيقات الأخرى\""
        AppLanguage.PORTUGUESE -> "Janela flutuante requer permissão \"Exibir sobre outros apps\""
        AppLanguage.SPANISH -> "La ventana flotante requiere permiso \"Mostrar sobre otras apps\""
        AppLanguage.FRENCH -> "La fenêtre flottante requiert l'autorisation « Afficher au-dessus des autres apps »"
        AppLanguage.GERMAN -> "Schwebendes Fenster erfordert Berechtigung \"Anzeige über anderen Apps\""
        AppLanguage.RUSSIAN -> "Плавающее окно требует разрешение «Показывать поверх других приложений»"
        AppLanguage.JAPANESE -> "フローティングウィンドウには「他のアプリの上に重ねて表示」権限が必要"
        AppLanguage.KOREAN -> "플로팅 창은 \"다른 앱 위에 표시\" 권한이 필요"
    }

    val httpAuthTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "需要身份验证"
        AppLanguage.ENGLISH -> "Authentication Required"
        AppLanguage.ARABIC -> "المصادقة مطلوبة"
        AppLanguage.PORTUGUESE -> "Autenticação Necessária"
        AppLanguage.SPANISH -> "Autenticación Requerida"
        AppLanguage.FRENCH -> "Authentification Requise"
        AppLanguage.GERMAN -> "Authentifizierung erforderlich"
        AppLanguage.RUSSIAN -> "Требуется аутентификация"
        AppLanguage.JAPANESE -> "認証が必要"
        AppLanguage.KOREAN -> "인증 필요"
    }

    val httpAuthMessage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "服务器 %s 要求输入用户名和密码"
        AppLanguage.ENGLISH -> "The server %s requires a username and password"
        AppLanguage.ARABIC -> "يتطلب الخادم %s اسم مستخدم وكلمة مرور"
        AppLanguage.PORTUGUESE -> "O servidor %s requer usuário e senha"
        AppLanguage.SPANISH -> "El servidor %s requiere usuario y contraseña"
        AppLanguage.FRENCH -> "Le serveur %s requiert un nom d'utilisateur et un mot de passe"
        AppLanguage.GERMAN -> "Der Server %s erfordert Benutzername und Passwort"
        AppLanguage.RUSSIAN -> "Сервер %s требует имя пользователя и пароль"
        AppLanguage.JAPANESE -> "サーバー %s はユーザー名とパスワードを要求"
        AppLanguage.KOREAN -> "서버 %s은(는) 사용자 이름과 비밀번호를 요구"
    }

    val httpAuthUsername: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "用户名"
        AppLanguage.ENGLISH -> "Username"
        AppLanguage.ARABIC -> "اسم المستخدم"
        AppLanguage.PORTUGUESE -> "Usuário"
        AppLanguage.SPANISH -> "Usuario"
        AppLanguage.FRENCH -> "Nom d'utilisateur"
        AppLanguage.GERMAN -> "Benutzername"
        AppLanguage.RUSSIAN -> "Имя пользователя"
        AppLanguage.JAPANESE -> "ユーザー名"
        AppLanguage.KOREAN -> "사용자 이름"
    }

    val httpAuthPassword: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "密码"
        AppLanguage.ENGLISH -> "Password"
        AppLanguage.ARABIC -> "كلمة المرور"
        AppLanguage.PORTUGUESE -> "Senha"
        AppLanguage.SPANISH -> "Contraseña"
        AppLanguage.FRENCH -> "Mot de passe"
        AppLanguage.GERMAN -> "Passwort"
        AppLanguage.RUSSIAN -> "Пароль"
        AppLanguage.JAPANESE -> "パスワード"
        AppLanguage.KOREAN -> "비밀번호"
    }

    val httpAuthLogin: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "登录"
        AppLanguage.ENGLISH -> "Sign In"
        AppLanguage.ARABIC -> "تسجيل الدخول"
        AppLanguage.PORTUGUESE -> "Entrar"
        AppLanguage.SPANISH -> "Iniciar Sesión"
        AppLanguage.FRENCH -> "Se Connecter"
        AppLanguage.GERMAN -> "Anmelden"
        AppLanguage.RUSSIAN -> "Войти"
        AppLanguage.JAPANESE -> "サインイン"
        AppLanguage.KOREAN -> "로그인"
    }

    val httpAuthShowPassword: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "显示密码"
        AppLanguage.ENGLISH -> "Show password"
        AppLanguage.ARABIC -> "إظهار كلمة المرور"
        AppLanguage.PORTUGUESE -> "Mostrar senha"
        AppLanguage.SPANISH -> "Mostrar contraseña"
        AppLanguage.FRENCH -> "Afficher le mot de passe"
        AppLanguage.GERMAN -> "Passwort anzeigen"
        AppLanguage.RUSSIAN -> "Показать пароль"
        AppLanguage.JAPANESE -> "パスワードを表示"
        AppLanguage.KOREAN -> "비밀번호 표시"
    }

    val geckoPromptLeaveTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "离开此页面？"
        AppLanguage.ENGLISH -> "Leave this page?"
        AppLanguage.ARABIC -> "مغادرة هذه الصفحة؟"
        AppLanguage.PORTUGUESE -> "Sair desta página?"
        AppLanguage.SPANISH -> "¿Salir de esta página?"
        AppLanguage.FRENCH -> "Quitter cette page ?"
        AppLanguage.GERMAN -> "Diese Seite verlassen?"
        AppLanguage.RUSSIAN -> "Покинуть эту страницу?"
        AppLanguage.JAPANESE -> "このページを離れますか？"
        AppLanguage.KOREAN -> "이 페이지를 나가시겠습니까?"
    }

    val geckoPromptLeaveMessage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "您所做的更改可能不会被保存。"
        AppLanguage.ENGLISH -> "Changes you made may not be saved."
        AppLanguage.ARABIC -> "قد لا يتم حفظ التغييرات التي أجريتها."
        AppLanguage.PORTUGUESE -> "As alterações feitas podem não ser salvas."
        AppLanguage.SPANISH -> "Es posible que los cambios realizados no se guarden."
        AppLanguage.FRENCH -> "Les modifications apportées pourraient ne pas être enregistrées."
        AppLanguage.GERMAN -> "Vorgenommene Änderungen werden möglicherweise nicht gespeichert."
        AppLanguage.RUSSIAN -> "Внесённые изменения могут не сохраниться."
        AppLanguage.JAPANESE -> "行った変更は保存されない場合があります。"
        AppLanguage.KOREAN -> "변경사항이 저장되지 않을 수 있습니다."
    }

    val geckoPromptBtnLeave: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "离开"
        AppLanguage.ENGLISH -> "Leave"
        AppLanguage.ARABIC -> "مغادرة"
        AppLanguage.PORTUGUESE -> "Sair"
        AppLanguage.SPANISH -> "Salir"
        AppLanguage.FRENCH -> "Quitter"
        AppLanguage.GERMAN -> "Verlassen"
        AppLanguage.RUSSIAN -> "Покинуть"
        AppLanguage.JAPANESE -> "離れる"
        AppLanguage.KOREAN -> "나가기"
    }

    val geckoPromptRepostTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "重新提交表单？"
        AppLanguage.ENGLISH -> "Resubmit the form?"
        AppLanguage.ARABIC -> "إعادة إرسال النموذج؟"
        AppLanguage.PORTUGUESE -> "Reenviar o formulário?"
        AppLanguage.SPANISH -> "¿Reenviar el formulario?"
        AppLanguage.FRENCH -> "Renvoyer le formulaire ?"
        AppLanguage.GERMAN -> "Formular erneut senden?"
        AppLanguage.RUSSIAN -> "Отправить форму повторно?"
        AppLanguage.JAPANESE -> "フォームを再送信しますか？"
        AppLanguage.KOREAN -> "양식을 다시 제출하시겠습니까?"
    }

    val geckoPromptRepostMessage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "页面将重新发送您提交的数据。"
        AppLanguage.ENGLISH -> "The page will resend the data you submitted."
        AppLanguage.ARABIC -> "سيتم إعادة إرسال البيانات التي أدخلتها."
        AppLanguage.PORTUGUESE -> "A página reenviará os dados que você enviou."
        AppLanguage.SPANISH -> "La página volverá a enviar los datos que enviaste."
        AppLanguage.FRENCH -> "La page renverra les données que vous avez envoyées."
        AppLanguage.GERMAN -> "Die Seite sendet die von Ihnen übermittelten Daten erneut."
        AppLanguage.RUSSIAN -> "Страница повторно отправит введённые данные."
        AppLanguage.JAPANESE -> "ページが送信したデータを再送信します。"
        AppLanguage.KOREAN -> "페이지에서 제출한 데이터를 다시 보냅니다."
    }

    val geckoPromptPickColor: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择颜色"
        AppLanguage.ENGLISH -> "Choose a color"
        AppLanguage.ARABIC -> "اختر لونًا"
        AppLanguage.PORTUGUESE -> "Escolha uma cor"
        AppLanguage.SPANISH -> "Elige un color"
        AppLanguage.FRENCH -> "Choisir une couleur"
        AppLanguage.GERMAN -> "Farbe wählen"
        AppLanguage.RUSSIAN -> "Выберите цвет"
        AppLanguage.JAPANESE -> "色を選択"
        AppLanguage.KOREAN -> "색상 선택"
    }

    val geckoPromptInvalidColor: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "颜色格式无效（应为 #RRGGBB）"
        AppLanguage.ENGLISH -> "Invalid color format (expected #RRGGBB)"
        AppLanguage.ARABIC -> "تنسيق اللون غير صالح (المتوقع #RRGGBB)"
        AppLanguage.PORTUGUESE -> "Formato de cor inválido (esperado #RRGGBB)"
        AppLanguage.SPANISH -> "Formato de color no válido (se esperaba #RRGGBB)"
        AppLanguage.FRENCH -> "Format de couleur invalide (#RRGGBB attendu)"
        AppLanguage.GERMAN -> "Ungültiges Farbformat (#RRGGBB erwartet)"
        AppLanguage.RUSSIAN -> "Неверный формат цвета (ожидается #RRGGBB)"
        AppLanguage.JAPANESE -> "色の形式が無効です（#RRGGBB）"
        AppLanguage.KOREAN -> "색상 형식이 잘못되었습니다 (#RRGGBB)"
    }

    val geckoPromptPickValue: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "请选择"
        AppLanguage.ENGLISH -> "Select a value"
        AppLanguage.ARABIC -> "اختر قيمة"
        AppLanguage.PORTUGUESE -> "Selecione um valor"
        AppLanguage.SPANISH -> "Selecciona un valor"
        AppLanguage.FRENCH -> "Sélectionnez une valeur"
        AppLanguage.GERMAN -> "Wert auswählen"
        AppLanguage.RUSSIAN -> "Выберите значение"
        AppLanguage.JAPANESE -> "値を選択"
        AppLanguage.KOREAN -> "값 선택"
    }

    val fwSectionSize: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "窗口尺寸"
        AppLanguage.ENGLISH -> "Window Size"
        AppLanguage.ARABIC -> "حجم النافذة"
        AppLanguage.PORTUGUESE -> "Tamanho da Janela"
        AppLanguage.SPANISH -> "Tamaño de Ventana"
        AppLanguage.FRENCH -> "Taille de la Fenêtre"
        AppLanguage.GERMAN -> "Fenstergröße"
        AppLanguage.RUSSIAN -> "Размер окна"
        AppLanguage.JAPANESE -> "ウィンドウサイズ"
        AppLanguage.KOREAN -> "창 크기"
    }

    val fwWidthLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "宽度"
        AppLanguage.ENGLISH -> "Width"
        AppLanguage.ARABIC -> "العرض"
        AppLanguage.PORTUGUESE -> "Largura"
        AppLanguage.SPANISH -> "Ancho"
        AppLanguage.FRENCH -> "Largeur"
        AppLanguage.GERMAN -> "Breite"
        AppLanguage.RUSSIAN -> "Ширина"
        AppLanguage.JAPANESE -> "幅"
        AppLanguage.KOREAN -> "너비"
    }

    val fwHeightLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "高度"
        AppLanguage.ENGLISH -> "Height"
        AppLanguage.ARABIC -> "الارتفاع"
        AppLanguage.PORTUGUESE -> "Altura"
        AppLanguage.SPANISH -> "Alto"
        AppLanguage.FRENCH -> "Hauteur"
        AppLanguage.GERMAN -> "Höhe"
        AppLanguage.RUSSIAN -> "Высота"
        AppLanguage.JAPANESE -> "高さ"
        AppLanguage.KOREAN -> "높이"
    }

    val fwAspectRatio: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "默认比例"
        AppLanguage.ENGLISH -> "Default Ratio"
        AppLanguage.ARABIC -> "النسبة الافتراضية"
        AppLanguage.PORTUGUESE -> "Proporção Padrão"
        AppLanguage.SPANISH -> "Proporción Predeterminada"
        AppLanguage.FRENCH -> "Ratio par Défaut"
        AppLanguage.GERMAN -> "Standardverhältnis"
        AppLanguage.RUSSIAN -> "Пропорция по умолчанию"
        AppLanguage.JAPANESE -> "デフォルト比率"
        AppLanguage.KOREAN -> "기본 비율"
    }

    val fwAspectScreen: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "跟随屏幕"
        AppLanguage.ENGLISH -> "Screen"
        AppLanguage.ARABIC -> "الشاشة"
        AppLanguage.PORTUGUESE -> "Tela"
        AppLanguage.SPANISH -> "Pantalla"
        AppLanguage.FRENCH -> "Écran"
        AppLanguage.GERMAN -> "Bildschirm"
        AppLanguage.RUSSIAN -> "Экран"
        AppLanguage.JAPANESE -> "画面"
        AppLanguage.KOREAN -> "화면"
    }

    val fwAspectFree: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自由"
        AppLanguage.ENGLISH -> "Free"
        AppLanguage.ARABIC -> "حر"
        AppLanguage.PORTUGUESE -> "Livre"
        AppLanguage.SPANISH -> "Libre"
        AppLanguage.FRENCH -> "Libre"
        AppLanguage.GERMAN -> "Frei"
        AppLanguage.RUSSIAN -> "Свободно"
        AppLanguage.JAPANESE -> "自由"
        AppLanguage.KOREAN -> "자유"
    }

    val fwAspectCustom: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自定义"
        AppLanguage.ENGLISH -> "Custom"
        AppLanguage.ARABIC -> "مخصص"
        AppLanguage.PORTUGUESE -> "Personalizado"
        AppLanguage.SPANISH -> "Personalizado"
        AppLanguage.FRENCH -> "Personnalisé"
        AppLanguage.GERMAN -> "Benutzerdefiniert"
        AppLanguage.RUSSIAN -> "Пользовательский"
        AppLanguage.JAPANESE -> "カスタム"
        AppLanguage.KOREAN -> "사용자 정의"
    }

    val fwAspectWidth: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "比例宽"
        AppLanguage.ENGLISH -> "Ratio Width"
        AppLanguage.ARABIC -> "عرض النسبة"
        AppLanguage.PORTUGUESE -> "Largura da Proporção"
        AppLanguage.SPANISH -> "Ancho de Proporción"
        AppLanguage.FRENCH -> "Largeur du Ratio"
        AppLanguage.GERMAN -> "Verhältnisbreite"
        AppLanguage.RUSSIAN -> "Ширина пропорции"
        AppLanguage.JAPANESE -> "比率幅"
        AppLanguage.KOREAN -> "비율 너비"
    }

    val fwAspectHeight: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "比例高"
        AppLanguage.ENGLISH -> "Ratio Height"
        AppLanguage.ARABIC -> "ارتفاع النسبة"
        AppLanguage.PORTUGUESE -> "Altura da Proporção"
        AppLanguage.SPANISH -> "Alto de Proporción"
        AppLanguage.FRENCH -> "Hauteur du Ratio"
        AppLanguage.GERMAN -> "Verhältnishöhe"
        AppLanguage.RUSSIAN -> "Высота пропорции"
        AppLanguage.JAPANESE -> "比率高"
        AppLanguage.KOREAN -> "비율 높이"
    }

    val fwSectionAppearance: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "外观样式"
        AppLanguage.ENGLISH -> "Appearance"
        AppLanguage.ARABIC -> "المظهر"
        AppLanguage.PORTUGUESE -> "Aparência"
        AppLanguage.SPANISH -> "Apariencia"
        AppLanguage.FRENCH -> "Apparence"
        AppLanguage.GERMAN -> "Erscheinungsbild"
        AppLanguage.RUSSIAN -> "Внешний вид"
        AppLanguage.JAPANESE -> "外観"
        AppLanguage.KOREAN -> "외형"
    }

    val fwCornerRadius: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "圆角半径"
        AppLanguage.ENGLISH -> "Corner Radius"
        AppLanguage.ARABIC -> "نصف قطر الزاوية"
        AppLanguage.PORTUGUESE -> "Raio do canto"
        AppLanguage.SPANISH -> "Radio de esquina"
        AppLanguage.FRENCH -> "Rayon d'angle"
        AppLanguage.GERMAN -> "Eckradius"
        AppLanguage.RUSSIAN -> "Радиус угла"
        AppLanguage.JAPANESE -> "角の半径"
        AppLanguage.KOREAN -> "모서리 반경"
    }

    val fwBorderStyle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "边框样式"
        AppLanguage.ENGLISH -> "Border Style"
        AppLanguage.ARABIC -> "نمط الحدود"
        AppLanguage.PORTUGUESE -> "Estilo da borda"
        AppLanguage.SPANISH -> "Estilo de borde"
        AppLanguage.FRENCH -> "Style de bordure"
        AppLanguage.GERMAN -> "Rahmenstil"
        AppLanguage.RUSSIAN -> "Стиль рамки"
        AppLanguage.JAPANESE -> "枠線スタイル"
        AppLanguage.KOREAN -> "테두리 스타일"
    }

    val fwBorderNone: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "无"
        AppLanguage.ENGLISH -> "None"
        AppLanguage.ARABIC -> "بدون"
        AppLanguage.PORTUGUESE -> "Nenhum"
        AppLanguage.SPANISH -> "Ninguno"
        AppLanguage.FRENCH -> "Aucun"
        AppLanguage.GERMAN -> "Keine"
        AppLanguage.RUSSIAN -> "Нет"
        AppLanguage.JAPANESE -> "なし"
        AppLanguage.KOREAN -> "없음"
    }

    val fwBorderSubtle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "细微"
        AppLanguage.ENGLISH -> "Subtle"
        AppLanguage.ARABIC -> "خفيف"
        AppLanguage.PORTUGUESE -> "Sutil"
        AppLanguage.SPANISH -> "Sutil"
        AppLanguage.FRENCH -> "Subtil"
        AppLanguage.GERMAN -> "Dezent"
        AppLanguage.RUSSIAN -> "Тонкий"
        AppLanguage.JAPANESE -> "控えめ"
        AppLanguage.KOREAN -> "미세"
    }

    val fwBorderGlow: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "发光"
        AppLanguage.ENGLISH -> "Glow"
        AppLanguage.ARABIC -> "توهج"
        AppLanguage.PORTUGUESE -> "Brilho"
        AppLanguage.SPANISH -> "Resplandor"
        AppLanguage.FRENCH -> "Lueur"
        AppLanguage.GERMAN -> "Glühen"
        AppLanguage.RUSSIAN -> "Свечение"
        AppLanguage.JAPANESE -> "発光"
        AppLanguage.KOREAN -> "빛남"
    }

    val fwBorderAccent: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "主题色"
        AppLanguage.ENGLISH -> "Accent"
        AppLanguage.ARABIC -> "اللون المميز"
        AppLanguage.PORTUGUESE -> "Destaque"
        AppLanguage.SPANISH -> "Acento"
        AppLanguage.FRENCH -> "Accent"
        AppLanguage.GERMAN -> "Akzent"
        AppLanguage.RUSSIAN -> "Акцент"
        AppLanguage.JAPANESE -> "アクセント"
        AppLanguage.KOREAN -> "강조"
    }

    val fwMinimizedIcon: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "最小化图标"
        AppLanguage.ENGLISH -> "Minimized Icon"
        AppLanguage.ARABIC -> "أيقونة التصغير"
        AppLanguage.PORTUGUESE -> "Ícone minimizado"
        AppLanguage.SPANISH -> "Icono minimizado"
        AppLanguage.FRENCH -> "Icône minimisée"
        AppLanguage.GERMAN -> "Minimiertes Symbol"
        AppLanguage.RUSSIAN -> "Свернутая иконка"
        AppLanguage.JAPANESE -> "最小化アイコン"
        AppLanguage.KOREAN -> "최소화 아이콘"
    }

    val fwDefaultMinimizedIcon: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "使用默认悬浮图标"
        AppLanguage.ENGLISH -> "Use the default floating icon"
        AppLanguage.ARABIC -> "استخدام الأيقونة العائمة الافتراضية"
        AppLanguage.PORTUGUESE -> "Usar ícone flutuante padrão"
        AppLanguage.SPANISH -> "Usar icono flotante predeterminado"
        AppLanguage.FRENCH -> "Utiliser l'icône flottante par défaut"
        AppLanguage.GERMAN -> "Standard-Schwebendes Symbol verwenden"
        AppLanguage.RUSSIAN -> "Использовать плавающую иконку по умолчанию"
        AppLanguage.JAPANESE -> "デフォルトのフローティングアイコンを使用"
        AppLanguage.KOREAN -> "기본 플로팅 아이콘 사용"
    }

    val fwCustomMinimizedIcon: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已使用自定义图片"
        AppLanguage.ENGLISH -> "Custom image selected"
        AppLanguage.ARABIC -> "تم اختيار صورة مخصصة"
        AppLanguage.PORTUGUESE -> "Imagem personalizada selecionada"
        AppLanguage.SPANISH -> "Imagen personalizada seleccionada"
        AppLanguage.FRENCH -> "Image personnalisée sélectionnée"
        AppLanguage.GERMAN -> "Benutzerdefiniertes Bild ausgewählt"
        AppLanguage.RUSSIAN -> "Выбрано пользовательское изображение"
        AppLanguage.JAPANESE -> "カスタム画像を選択済み"
        AppLanguage.KOREAN -> "맞춤 이미지 선택됨"
    }

    val fwSelectMinimizedIcon: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择图片"
        AppLanguage.ENGLISH -> "Choose Image"
        AppLanguage.ARABIC -> "اختيار صورة"
        AppLanguage.PORTUGUESE -> "Escolher imagem"
        AppLanguage.SPANISH -> "Elegir imagen"
        AppLanguage.FRENCH -> "Choisir une image"
        AppLanguage.GERMAN -> "Bild auswählen"
        AppLanguage.RUSSIAN -> "Выбрать изображение"
        AppLanguage.JAPANESE -> "画像を選択"
        AppLanguage.KOREAN -> "이미지 선택"
    }

    val fwClearMinimizedIcon: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "恢复默认"
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

    val fwMinimizedIconSize: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "图标大小"
        AppLanguage.ENGLISH -> "Icon Size"
        AppLanguage.ARABIC -> "حجم الأيقونة"
        AppLanguage.PORTUGUESE -> "Tamanho do ícone"
        AppLanguage.SPANISH -> "Tamaño del icono"
        AppLanguage.FRENCH -> "Taille de l'icône"
        AppLanguage.GERMAN -> "Symbolgröße"
        AppLanguage.RUSSIAN -> "Размер иконки"
        AppLanguage.JAPANESE -> "アイコンサイズ"
        AppLanguage.KOREAN -> "아이콘 크기"
    }

    val fwMinimizedIconEdgeDocking: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "收起后半隐藏"
        AppLanguage.ENGLISH -> "Half-hide When Minimized"
        AppLanguage.ARABIC -> "إخفاء نصف الأيقونة عند التصغير"
        AppLanguage.PORTUGUESE -> "Meio-ocultar ao minimizar"
        AppLanguage.SPANISH -> "Ocultar media al minimizar"
        AppLanguage.FRENCH -> "Masquer à moitié en réduction"
        AppLanguage.GERMAN -> "Bei Minimierung halb verbergen"
        AppLanguage.RUSSIAN -> "Скрывать наполовину при сворачивании"
        AppLanguage.JAPANESE -> "最小化時に半分隠す"
        AppLanguage.KOREAN -> "최소화 시 절반 숨기기"
    }

    val fwMinimizedIconEdgeDockingDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "拖动收起图标后自动吸附到屏幕边缘，并露出一半方便再次展开"
        AppLanguage.ENGLISH -> "After dragging, the minimized icon docks to the screen edge and leaves half visible for restore"
        AppLanguage.ARABIC -> "بعد السحب، تلتصق الأيقونة المصغرة بحافة الشاشة وتبقي نصفها ظاهرا للاستعادة"
        AppLanguage.PORTUGUESE -> "Após arrastar, o ícone minimizado se ancora à borda da tela e deixa metade visível para restauração"
        AppLanguage.SPANISH -> "Tras arrastrar, el icono minimizado se acopla al borde de la pantalla y deja la mitad visible para restaurar"
        AppLanguage.FRENCH -> "Après glissement, l'icône minimisée s'amarre au bord de l'écran et laisse la moitié visible pour la restauration"
        AppLanguage.GERMAN -> "Nach dem Ziehen dockt das minimierte Symbol am Bildschirmrand an und lässt die Hälfte sichtbar zum Wiederherstellen"
        AppLanguage.RUSSIAN -> "После перетаскивания свернутая иконка пристыковывается к краю экрана, оставляя половину видимой для восстановления"
        AppLanguage.JAPANESE -> "ドラッグ後、最小化アイコンは画面の端にドッキングし、復元のため半分を表示したままにします"
        AppLanguage.KOREAN -> "드래그 후 최소화된 아이콘이 화면 가장자리에 도킹되어 복원을 위해 절반을 표시합니다"
    }

    val fwSectionBehavior: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "行为控制"
        AppLanguage.ENGLISH -> "Behavior"
        AppLanguage.ARABIC -> "السلوك"
        AppLanguage.PORTUGUESE -> "Comportamento"
        AppLanguage.SPANISH -> "Comportamiento"
        AppLanguage.FRENCH -> "Comportement"
        AppLanguage.GERMAN -> "Verhalten"
        AppLanguage.RUSSIAN -> "Поведение"
        AppLanguage.JAPANESE -> "動作"
        AppLanguage.KOREAN -> "동작"
    }

    val fwAutoHideTitleBar: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自动隐藏标题栏"
        AppLanguage.ENGLISH -> "Auto-hide Title Bar"
        AppLanguage.ARABIC -> "إخفاء شريط العنوان تلقائياً"
        AppLanguage.PORTUGUESE -> "Ocultar automaticamente a barra de título"
        AppLanguage.SPANISH -> "Ocultar automáticamente la barra de título"
        AppLanguage.FRENCH -> "Masquer automatiquement la barre de titre"
        AppLanguage.GERMAN -> "Titelleiste automatisch ausblenden"
        AppLanguage.RUSSIAN -> "Автоскрытие панели заголовка"
        AppLanguage.JAPANESE -> "タイトルバーを自動的に隠す"
        AppLanguage.KOREAN -> "제목 표시줄 자동 숨기기"
    }

    val fwAutoHideTitleBarDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "3秒无操作后自动隐藏，触摸时恢复"
        AppLanguage.ENGLISH -> "Auto-hides after 3s of inactivity, restores on touch"
        AppLanguage.ARABIC -> "يختفي بعد 3 ثوانٍ من عدم النشاط، ويعود عند اللمس"
        AppLanguage.PORTUGUESE -> "Oculta automaticamente após 3s de inatividade, restaura ao toque"
        AppLanguage.SPANISH -> "Se oculta automáticamente tras 3s de inactividad, se restaura al tocar"
        AppLanguage.FRENCH -> "Se masque automatiquement après 3s d'inactivité, se restaure au toucher"
        AppLanguage.GERMAN -> "Blendet nach 3s Inaktivität automatisch aus, wird bei Berührung wiederhergestellt"
        AppLanguage.RUSSIAN -> "Автоматически скрывается через 3с бездействия, восстанавливается при касании"
        AppLanguage.JAPANESE -> "3秒間操作がないと自動的に非表示になり、タッチで復元します"
        AppLanguage.KOREAN -> "3초 동안 활동이 없으면 자동으로 숨겨지며, 터치 시 복원됩니다"
    }

    val floatingWindowDragHandle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "拖动以移动窗口"
        AppLanguage.ENGLISH -> "Drag to move window"
        AppLanguage.ARABIC -> "اسحب لتحريك النافذة"
        AppLanguage.PORTUGUESE -> "Arraste para mover a janela"
        AppLanguage.SPANISH -> "Arrastra para mover la ventana"
        AppLanguage.FRENCH -> "Glisser pour déplacer la fenêtre"
        AppLanguage.GERMAN -> "Ziehen, um Fenster zu verschieben"
        AppLanguage.RUSSIAN -> "Перетащите, чтобы переместить окно"
        AppLanguage.JAPANESE -> "ドラッグしてウィンドウを移動"
        AppLanguage.KOREAN -> "드래그하여 창 이동"
    }

    val fwEdgeSnapping: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "边缘吸附"
        AppLanguage.ENGLISH -> "Edge Snapping"
        AppLanguage.ARABIC -> "الالتصاق بالحافة"
        AppLanguage.PORTUGUESE -> "Ajuste de borda"
        AppLanguage.SPANISH -> "Ajuste de borde"
        AppLanguage.FRENCH -> "Aimantation aux bords"
        AppLanguage.GERMAN -> "Kanten-Einrasten"
        AppLanguage.RUSSIAN -> "Прилипание к краю"
        AppLanguage.JAPANESE -> "エッジスナップ"
        AppLanguage.KOREAN -> "가장자리 스냅"
    }

    val fwEdgeSnappingDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "拖拽到屏幕边缘时自动贴合"
        AppLanguage.ENGLISH -> "Snaps to screen edges when dragged nearby"
        AppLanguage.ARABIC -> "ينجذب تلقائياً إلى حواف الشاشة عند السحب بالقرب منها"
        AppLanguage.PORTUGUESE -> "Ajusta-se às bordas da tela quando arrastado perto"
        AppLanguage.SPANISH -> "Se acopla a los bordes de la pantalla al arrastrar cerca"
        AppLanguage.FRENCH -> "S'aimante aux bords de l'écran lorsqu'il est glissé à proximité"
        AppLanguage.GERMAN -> "Richtet sich an Bildschirmkanten aus, wenn in die Nähe gezogen"
        AppLanguage.RUSSIAN -> "Прилипает к краям экрана при перетаскивании рядом"
        AppLanguage.JAPANESE -> "近くにドラッグすると画面の端にスナップします"
        AppLanguage.KOREAN -> "가까이 드래그하면 화면 가장자리에 스냅됩니다"
    }

    val fwResizeHandle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "缩放手柄"
        AppLanguage.ENGLISH -> "Resize Handle"
        AppLanguage.ARABIC -> "مقبض تغيير الحجم"
        AppLanguage.PORTUGUESE -> "Alça de redimensionamento"
        AppLanguage.SPANISH -> "Asa de redimensionamiento"
        AppLanguage.FRENCH -> "Poignée de redimensionnement"
        AppLanguage.GERMAN -> "Größenänderungsgriff"
        AppLanguage.RUSSIAN -> "Маркер изменения размера"
        AppLanguage.JAPANESE -> "リサイズハンドル"
        AppLanguage.KOREAN -> "크기 조정 핸들"
    }

    val fwResizeHandleDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "在窗口右下角显示拖拽缩放手柄"
        AppLanguage.ENGLISH -> "Shows a drag handle at the bottom-right corner"
        AppLanguage.ARABIC -> "يعرض مقبض سحب في الزاوية السفلية اليمنى"
        AppLanguage.PORTUGUESE -> "Mostra uma alça de arrasto no canto inferior direito"
        AppLanguage.SPANISH -> "Muestra un asa de arrastre en la esquina inferior derecha"
        AppLanguage.FRENCH -> "Affiche une poignée de glissement dans le coin inférieur droit"
        AppLanguage.GERMAN -> "Zeigt einen Ziehgriff in der unteren rechten Ecke"
        AppLanguage.RUSSIAN -> "Показывает маркер перетаскивания в правом нижнем углу"
        AppLanguage.JAPANESE -> "右下隅にドラッグハンドルを表示"
        AppLanguage.KOREAN -> "오른쪽 하단 모서리에 드래그 핸들을 표시"
    }

    val fwLockPosition: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "锁定位置"
        AppLanguage.ENGLISH -> "Lock Position"
        AppLanguage.ARABIC -> "قفل الموضع"
        AppLanguage.PORTUGUESE -> "Bloquear posição"
        AppLanguage.SPANISH -> "Bloquear posición"
        AppLanguage.FRENCH -> "Verrouiller la position"
        AppLanguage.GERMAN -> "Position sperren"
        AppLanguage.RUSSIAN -> "Заблокировать позицию"
        AppLanguage.JAPANESE -> "位置をロック"
        AppLanguage.KOREAN -> "위치 잠금"
    }

    val fwLockPositionDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "禁止拖拽移动窗口，防止误触"
        AppLanguage.ENGLISH -> "Prevents window from being dragged"
        AppLanguage.ARABIC -> "يمنع سحب النافذة لتجنب اللمس العرضي"
        AppLanguage.PORTUGUESE -> "Impede que a janela seja arrastada"
        AppLanguage.SPANISH -> "Evita que la ventana sea arrastrada"
        AppLanguage.FRENCH -> "Empêche la fenêtre d'être glissée"
        AppLanguage.GERMAN -> "Verhindert das Ziehen des Fensters"
        AppLanguage.RUSSIAN -> "Предотвращает перетаскивание окна"
        AppLanguage.JAPANESE -> "ウィンドウのドラッグを防止します"
        AppLanguage.KOREAN -> "창이 드래그되지 않도록 방지"
    }

    val backgroundRunTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "后台运行"
        AppLanguage.ENGLISH -> "Background Run"
        AppLanguage.ARABIC -> "التشغيل في الخلفية"
        AppLanguage.PORTUGUESE -> "Execução em segundo plano"
        AppLanguage.SPANISH -> "Ejecución en segundo plano"
        AppLanguage.FRENCH -> "Exécution en arrière-plan"
        AppLanguage.GERMAN -> "Hintergrundausführung"
        AppLanguage.RUSSIAN -> "Фоновый запуск"
        AppLanguage.JAPANESE -> "バックグラウンド実行"
        AppLanguage.KOREAN -> "백그라운드 실행"
    }

    val backgroundRunChannelName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "后台运行"
        AppLanguage.ENGLISH -> "Background Run"
        AppLanguage.ARABIC -> "التشغيل في الخلفية"
        AppLanguage.PORTUGUESE -> "Execução em segundo plano"
        AppLanguage.SPANISH -> "Ejecución en segundo plano"
        AppLanguage.FRENCH -> "Exécution en arrière-plan"
        AppLanguage.GERMAN -> "Hintergrundausführung"
        AppLanguage.RUSSIAN -> "Фоновый запуск"
        AppLanguage.JAPANESE -> "バックグラウンド実行"
        AppLanguage.KOREAN -> "백그라운드 실행"
    }

    val backgroundRunChannelDescription: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "让应用在后台持续运行"
        AppLanguage.ENGLISH -> "Keep app running in background"
        AppLanguage.ARABIC -> "الحفاظ على تشغيل التطبيق في الخلفية"
        AppLanguage.PORTUGUESE -> "Manter o aplicativo em execução em segundo plano"
        AppLanguage.SPANISH -> "Mantener la aplicación ejecutándose en segundo plano"
        AppLanguage.FRENCH -> "Maintenir l'application en cours d'exécution en arrière-plan"
        AppLanguage.GERMAN -> "App im Hintergrund weiterlaufen lassen"
        AppLanguage.RUSSIAN -> "Держать приложение работающим в фоновом режиме"
        AppLanguage.JAPANESE -> "アプリをバックグラウンドで実行し続ける"
        AppLanguage.KOREAN -> "앱이 백그라운드에서 계속 실행되도록 유지"
    }

    val backgroundRunKeepCpuAwake: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "保持CPU唤醒"
        AppLanguage.ENGLISH -> "Keep CPU Awake"
        AppLanguage.ARABIC -> "إبقاء المعالج نشطًا"
        AppLanguage.PORTUGUESE -> "Manter CPU ativo"
        AppLanguage.SPANISH -> "Mantener CPU activo"
        AppLanguage.FRENCH -> "Maintenir le CPU éveillé"
        AppLanguage.GERMAN -> "CPU wach halten"
        AppLanguage.RUSSIAN -> "Держать CPU активным"
        AppLanguage.JAPANESE -> "CPUをウェイク状態に維持"
        AppLanguage.KOREAN -> "CPU를 깨어있게 유지"
    }

    val backgroundRunKeepCpuAwakeDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "屏幕关闭后继续运行，耗电增加"
        AppLanguage.ENGLISH -> "Keeps running while the screen is off; higher battery drain"
        AppLanguage.ARABIC -> "يستمر التشغيل عند إطفاء الشاشة؛ استهلاك أعلى للبطارية"
        AppLanguage.PORTUGUESE -> "Continua executando com a tela desligada; maior consumo de bateria"
        AppLanguage.SPANISH -> "Sigue ejecutándose con la pantalla apagada; mayor consumo de batería"
        AppLanguage.FRENCH -> "Continue de fonctionner écran éteint ; consommation accrue"
        AppLanguage.GERMAN -> "Läuft bei ausgeschaltetem Bildschirm weiter; höherer Akkuverbrauch"
        AppLanguage.RUSSIAN -> "Продолжает работу при выключенном экране; выше расход батареи"
        AppLanguage.JAPANESE -> "画面オフ時も実行を継続（バッテリー消費増）"
        AppLanguage.KOREAN -> "화면이 꺼져도 계속 실행됩니다 (배터리 소모 증가)"
    }

    val backgroundRunNotificationTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "通知标题"
        AppLanguage.ENGLISH -> "Notification Title"
        AppLanguage.ARABIC -> "عنوان الإشعار"
        AppLanguage.PORTUGUESE -> "Título da notificação"
        AppLanguage.SPANISH -> "Título de la notificación"
        AppLanguage.FRENCH -> "Titre de la notification"
        AppLanguage.GERMAN -> "Benachrichtigungstitel"
        AppLanguage.RUSSIAN -> "Заголовок уведомления"
        AppLanguage.JAPANESE -> "通知タイトル"
        AppLanguage.KOREAN -> "알림 제목"
    }

    val backgroundRunNotificationTitlePlaceholder: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "留空使用默认标题"
        AppLanguage.ENGLISH -> "Leave empty for default title"
        AppLanguage.ARABIC -> "اتركه فارغًا للعنوان الافتراضي"
        AppLanguage.PORTUGUESE -> "Deixe vazio para o título padrão"
        AppLanguage.SPANISH -> "Dejar vacío para el título predeterminado"
        AppLanguage.FRENCH -> "Laisser vide pour le titre par défaut"
        AppLanguage.GERMAN -> "Leer lassen für Standardtitel"
        AppLanguage.RUSSIAN -> "Оставьте пустым для заголовка по умолчанию"
        AppLanguage.JAPANESE -> "デフォルトのタイトルを使用する場合は空のまま"
        AppLanguage.KOREAN -> "기본 제목을 사용하려면 비워두세요"
    }

    val backgroundRunNotificationContent: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "通知内容"
        AppLanguage.ENGLISH -> "Notification Content"
        AppLanguage.ARABIC -> "محتوى الإشعار"
        AppLanguage.PORTUGUESE -> "Conteúdo da notificação"
        AppLanguage.SPANISH -> "Contenido de la notificación"
        AppLanguage.FRENCH -> "Contenu de la notification"
        AppLanguage.GERMAN -> "Benachrichtigungsinhalt"
        AppLanguage.RUSSIAN -> "Содержание уведомления"
        AppLanguage.JAPANESE -> "通知内容"
        AppLanguage.KOREAN -> "알림 내용"
    }

    val backgroundRunNotificationContentPlaceholder: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "留空使用默认内容"
        AppLanguage.ENGLISH -> "Leave empty for default content"
        AppLanguage.ARABIC -> "اتركه فارغًا للمحتوى الافتراضي"
        AppLanguage.PORTUGUESE -> "Deixe vazio para o conteúdo padrão"
        AppLanguage.SPANISH -> "Dejar vacío para el contenido predeterminado"
        AppLanguage.FRENCH -> "Laisser vide pour le contenu par défaut"
        AppLanguage.GERMAN -> "Leer lassen für Standardinhalt"
        AppLanguage.RUSSIAN -> "Оставьте пустым для содержания по умолчанию"
        AppLanguage.JAPANESE -> "デフォルトの内容を使用する場合は空のまま"
        AppLanguage.KOREAN -> "기본 내용을 사용하려면 비워두세요"
    }

    val backgroundRunStop: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "停止运行"
        AppLanguage.ENGLISH -> "Stop"
        AppLanguage.ARABIC -> "إيقاف"
        AppLanguage.PORTUGUESE -> "Parar"
        AppLanguage.SPANISH -> "Detener"
        AppLanguage.FRENCH -> "Arrêter"
        AppLanguage.GERMAN -> "Stopp"
        AppLanguage.RUSSIAN -> "Остановить"
        AppLanguage.JAPANESE -> "停止"
        AppLanguage.KOREAN -> "중지"
    }

    val backgroundRunBatteryAutoHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "生成的应用首次启动时会请求一次电池优化豁免，防止后台服务被系统回收"
        AppLanguage.ENGLISH -> "The generated app asks once on first launch to be exempted from battery optimization, preventing the system from reclaiming it"
        AppLanguage.ARABIC -> "يطلب التطبيق المُنشأ مرة واحدة عند أول تشغيل إعفاءه من تحسين البطارية لمنع النظام من إنهائه"
        AppLanguage.PORTUGUESE -> "O app gerado solicita uma vez, no primeiro arranque, isenção da otimização de bateria para evitar que o sistema o encerre"
        AppLanguage.SPANISH -> "La app generada solicita una vez, en el primer inicio, la exención de optimización de batería para evitar que el sistema la cierre"
        AppLanguage.FRENCH -> "L'app générée demande une fois, au premier lancement, l'exemption d'optimisation de batterie pour éviter que le système ne la tue"
        AppLanguage.GERMAN -> "Die generierte App fragt beim ersten Start einmalig nach einer Akkuoptimierungs-Ausnahme, damit das System den Dienst nicht beendet"
        AppLanguage.RUSSIAN -> "Сгенерированное приложение при первом запуске один раз запросит исключение из оптимизации батареи, чтобы система не выгружала службу"
        AppLanguage.JAPANESE -> "生成されたアプリは初回起動時に一度だけバッテリー最適化の除外を求め、システムによる終了を防ぎます"
        AppLanguage.KOREAN -> "생성된 앱은 첫 실행 시 한 번만 배터리 최적화 제외를 요청하여 시스템이 서비스를 종료하지 않도록 합니다"
    }

    val backgroundRunActiveSummary: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "常驻服务 · 划卡自动重启"
        AppLanguage.ENGLISH -> "Persistent service · auto-restarts"
        AppLanguage.ARABIC -> "خدمة دائمة · إعادة تشغيل تلقائية"
        AppLanguage.PORTUGUESE -> "Serviço persistente · reinício automático"
        AppLanguage.SPANISH -> "Servicio persistente · reinicio automático"
        AppLanguage.FRENCH -> "Service persistant · redémarrage auto"
        AppLanguage.GERMAN -> "Persistenter Dienst · Auto-Neustart"
        AppLanguage.RUSSIAN -> "Постоянная служба · автоперезапуск"
        AppLanguage.JAPANESE -> "常駐サービス · 自動再起動"
        AppLanguage.KOREAN -> "상주 서비스 · 자동 재시작"
    }

    val backgroundRunNotificationSection: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "常驻通知"
        AppLanguage.ENGLISH -> "Persistent notification"
        AppLanguage.ARABIC -> "الإشعار الدائم"
        AppLanguage.PORTUGUESE -> "Notificação persistente"
        AppLanguage.SPANISH -> "Notificación persistente"
        AppLanguage.FRENCH -> "Notification persistante"
        AppLanguage.GERMAN -> "Permanente Benachrichtigung"
        AppLanguage.RUSSIAN -> "Постоянное уведомление"
        AppLanguage.JAPANESE -> "常駐通知"
        AppLanguage.KOREAN -> "상주 알림"
    }

    val notificationConfigTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "通知"
        AppLanguage.ENGLISH -> "Notifications"
        AppLanguage.ARABIC -> "الإشعارات"
        AppLanguage.PORTUGUESE -> "Notificações"
        AppLanguage.SPANISH -> "Notificaciones"
        AppLanguage.FRENCH -> "Notifications"
        AppLanguage.GERMAN -> "Benachrichtigungen"
        AppLanguage.RUSSIAN -> "Уведомления"
        AppLanguage.JAPANESE -> "通知"
        AppLanguage.KOREAN -> "알림"
    }

    val notificationTypeLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "通知类型"
        AppLanguage.ENGLISH -> "Notification Type"
        AppLanguage.ARABIC -> "نوع الإشعار"
        AppLanguage.PORTUGUESE -> "Tipo de notificação"
        AppLanguage.SPANISH -> "Tipo de notificación"
        AppLanguage.FRENCH -> "Type de notification"
        AppLanguage.GERMAN -> "Benachrichtigungstyp"
        AppLanguage.RUSSIAN -> "Тип уведомления"
        AppLanguage.JAPANESE -> "通知タイプ"
        AppLanguage.KOREAN -> "알림 유형"
    }

    val notificationSelectChannel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "请选择推送通道"
        AppLanguage.ENGLISH -> "Choose a push channel"
        AppLanguage.ARABIC -> "اختر قناة الدفع"
        AppLanguage.PORTUGUESE -> "Escolha um canal de push"
        AppLanguage.SPANISH -> "Elige un canal de push"
        AppLanguage.FRENCH -> "Choisir un canal de push"
        AppLanguage.GERMAN -> "Push-Kanal wählen"
        AppLanguage.RUSSIAN -> "Выберите канал push"
        AppLanguage.JAPANESE -> "プッシュチャネルを選択"
        AppLanguage.KOREAN -> "푸시 채널 선택"
    }

    val notificationTypeWebApi: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "网页通知"
        AppLanguage.ENGLISH -> "Web Notification"
        AppLanguage.ARABIC -> "إشعار الويب"
        AppLanguage.PORTUGUESE -> "Notificação Web"
        AppLanguage.SPANISH -> "Notificación Web"
        AppLanguage.FRENCH -> "Notification Web"
        AppLanguage.GERMAN -> "Web-Benachrichtigung"
        AppLanguage.RUSSIAN -> "Веб-уведомление"
        AppLanguage.JAPANESE -> "Web通知"
        AppLanguage.KOREAN -> "웹 알림"
    }

    val notificationTypePolling: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "轮询通知"
        AppLanguage.ENGLISH -> "Polling Notification"
        AppLanguage.ARABIC -> "إشعار الاستطلاع"
        AppLanguage.PORTUGUESE -> "Notificação por sondagem"
        AppLanguage.SPANISH -> "Notificación por sondeo"
        AppLanguage.FRENCH -> "Notification par interrogation"
        AppLanguage.GERMAN -> "Polling-Benachrichtigung"
        AppLanguage.RUSSIAN -> "Уведомление опроса"
        AppLanguage.JAPANESE -> "ポーリング通知"
        AppLanguage.KOREAN -> "폴링 알림"
    }

    val notificationTypeWebsocket: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "WebSocket 推送"
        AppLanguage.ENGLISH -> "WebSocket Push"
        AppLanguage.ARABIC -> "دفع WebSocket"
        AppLanguage.PORTUGUESE -> "Push WebSocket"
        AppLanguage.SPANISH -> "Push WebSocket"
        AppLanguage.FRENCH -> "Push WebSocket"
        AppLanguage.GERMAN -> "WebSocket-Push"
        AppLanguage.RUSSIAN -> "WebSocket Push"
        AppLanguage.JAPANESE -> "WebSocket プッシュ"
        AppLanguage.KOREAN -> "WebSocket 푸시"
    }
    val notificationWebApiDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "网页里的 new Notification() 直接映射为系统通知"
        AppLanguage.ENGLISH -> "new Notification() in pages maps directly to system notifications"
        AppLanguage.ARABIC -> "يتم تعيين new Notification() في الصفحات مباشرة إلى إشعارات النظام"
        AppLanguage.PORTUGUESE -> "new Notification() nas páginas mapeia diretamente para notificações do sistema"
        AppLanguage.SPANISH -> "new Notification() en páginas se mapea directamente a notificaciones del sistema"
        AppLanguage.FRENCH -> "new Notification() dans les pages correspond directement aux notifications système"
        AppLanguage.GERMAN -> "new Notification() in Seiten wird direkt auf Systembenachrichtigungen abgebildet"
        AppLanguage.RUSSIAN -> "new Notification() на страницах напрямую превращается в системные уведомления"
        AppLanguage.JAPANESE -> "ページ内の new Notification() がシステム通知に直接マッピングされます"
        AppLanguage.KOREAN -> "페이지의 new Notification()이 시스템 알림으로 직접 매핑됩니다"
    }

    val notificationTypeFcm: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "FCM 推送"
        AppLanguage.ENGLISH -> "FCM Push"
        AppLanguage.ARABIC -> "دفع FCM"
        AppLanguage.PORTUGUESE -> "Push FCM"
        AppLanguage.SPANISH -> "Push FCM"
        AppLanguage.FRENCH -> "Push FCM"
        AppLanguage.GERMAN -> "FCM-Push"
        AppLanguage.RUSSIAN -> "FCM Push"
        AppLanguage.JAPANESE -> "FCM プッシュ"
        AppLanguage.KOREAN -> "FCM 푸시"
    }
    val notificationPollUrl: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "轮询 URL"
        AppLanguage.ENGLISH -> "Poll URL"
        AppLanguage.ARABIC -> "رابط الاستطلاع"
        AppLanguage.PORTUGUESE -> "URL de sondagem"
        AppLanguage.SPANISH -> "URL de sondeo"
        AppLanguage.FRENCH -> "URL d'interrogation"
        AppLanguage.GERMAN -> "Poll-URL"
        AppLanguage.RUSSIAN -> "URL опроса"
        AppLanguage.JAPANESE -> "ポーリング URL"
        AppLanguage.KOREAN -> "폴링 URL"
    }

    val notificationPollUrlPlaceholder: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "https://example.com/api/notifications"
        AppLanguage.ENGLISH -> "https://example.com/api/notifications"
        AppLanguage.ARABIC -> "https://example.com/api/notifications"
        AppLanguage.PORTUGUESE -> "https://example.com/api/notifications"
        AppLanguage.SPANISH -> "https://example.com/api/notifications"
        AppLanguage.FRENCH -> "https://example.com/api/notifications"
        AppLanguage.GERMAN -> "https://example.com/api/notifications"
        AppLanguage.RUSSIAN -> "https://example.com/api/notifications"
        AppLanguage.JAPANESE -> "https://example.com/api/notifications"
        AppLanguage.KOREAN -> "https://example.com/api/notifications"
    }

    val notificationWebsocketDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "连接自定义 WebSocket 服务实时收推送，可选 Token 注册接口"
        AppLanguage.ENGLISH -> "Real-time push from your own WebSocket server, optional token register endpoint"
        AppLanguage.ARABIC -> "دفع فوري من خادم WebSocket الخاص بك، مع نقطة تسجيل رمز اختيارية"
        AppLanguage.PORTUGUESE -> "Push em tempo real do seu próprio servidor WebSocket, endpoint de registro de token opcional"
        AppLanguage.SPANISH -> "Push en tiempo real desde tu propio servidor WebSocket, con endpoint de registro de token opcional"
        AppLanguage.FRENCH -> "Push en temps réel depuis votre serveur WebSocket, endpoint d'enregistrement de jeton optionnel"
        AppLanguage.GERMAN -> "Echtzeit-Push von Ihrem eigenen WebSocket-Server, optionaler Token-Registrierungsendpunkt"
        AppLanguage.RUSSIAN -> "Push в реальном времени с вашего WebSocket-сервера, опциональный endpoint регистрации токена"
        AppLanguage.JAPANESE -> "独自の WebSocket サーバーからリアルタイムにプッシュを受信。トークン登録エンドポイントは任意"
        AppLanguage.KOREAN -> "자체 WebSocket 서버에서 실시간 푸시 수신, 선택적 토큰 등록 엔드포인트"
    }
    val notificationWsUrl: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "WebSocket URL"
        AppLanguage.ENGLISH -> "WebSocket URL"
        AppLanguage.ARABIC -> "رابط WebSocket"
        AppLanguage.PORTUGUESE -> "URL do WebSocket"
        AppLanguage.SPANISH -> "URL de WebSocket"
        AppLanguage.FRENCH -> "URL WebSocket"
        AppLanguage.GERMAN -> "WebSocket-URL"
        AppLanguage.RUSSIAN -> "URL WebSocket"
        AppLanguage.JAPANESE -> "WebSocket URL"
        AppLanguage.KOREAN -> "WebSocket URL"
    }
    val notificationWsUrlPlaceholder: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "wss://example.com/ws/notifications"
        AppLanguage.ENGLISH -> "wss://example.com/ws/notifications"
        AppLanguage.ARABIC -> "wss://example.com/ws/notifications"
        AppLanguage.PORTUGUESE -> "wss://example.com/ws/notifications"
        AppLanguage.SPANISH -> "wss://example.com/ws/notifications"
        AppLanguage.FRENCH -> "wss://example.com/ws/notifications"
        AppLanguage.GERMAN -> "wss://example.com/ws/notifications"
        AppLanguage.RUSSIAN -> "wss://example.com/ws/notifications"
        AppLanguage.JAPANESE -> "wss://example.com/ws/notifications"
        AppLanguage.KOREAN -> "wss://example.com/ws/notifications"
    }
    val notificationAuthToken: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "认证 Token（可选）"
        AppLanguage.ENGLISH -> "Auth Token (optional)"
        AppLanguage.ARABIC -> "رمز المصادقة (اختياري)"
        AppLanguage.PORTUGUESE -> "Token de autenticação (opcional)"
        AppLanguage.SPANISH -> "Token de autenticación (opcional)"
        AppLanguage.FRENCH -> "Jeton d’auth (optionnel)"
        AppLanguage.GERMAN -> "Auth-Token (optional)"
        AppLanguage.RUSSIAN -> "Токен (необязательно)"
        AppLanguage.JAPANESE -> "認証トークン（任意）"
        AppLanguage.KOREAN -> "인증 토큰(선택)"
    }
    val notificationAuthTokenPlaceholder: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Bearer Token 或自定义密钥"
        AppLanguage.ENGLISH -> "Bearer token or custom secret"
        AppLanguage.ARABIC -> "رمز Bearer أو مفتاح مخصص"
        AppLanguage.PORTUGUESE -> "Token Bearer ou segredo personalizado"
        AppLanguage.SPANISH -> "Token Bearer o secreto personalizado"
        AppLanguage.FRENCH -> "Jeton Bearer ou secret personnalisé"
        AppLanguage.GERMAN -> "Bearer-Token oder eigenes Geheimnis"
        AppLanguage.RUSSIAN -> "Bearer-токен или свой секрет"
        AppLanguage.JAPANESE -> "Bearer トークンまたはカスタムシークレット"
        AppLanguage.KOREAN -> "Bearer 토큰 또는 사용자 지정 비밀"
    }
    val notificationRegisterUrl: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Token 注册接口（可选）"
        AppLanguage.ENGLISH -> "Token Register URL (optional)"
        AppLanguage.ARABIC -> "رابط تسجيل الرمز (اختياري)"
        AppLanguage.PORTUGUESE -> "URL de registro do token (opcional)"
        AppLanguage.SPANISH -> "URL de registro de token (opcional)"
        AppLanguage.FRENCH -> "URL d’enregistrement du jeton (optionnel)"
        AppLanguage.GERMAN -> "Token-Registrierungs-URL (optional)"
        AppLanguage.RUSSIAN -> "URL регистрации токена (необязательно)"
        AppLanguage.JAPANESE -> "トークン登録 URL（任意）"
        AppLanguage.KOREAN -> "토큰 등록 URL(선택)"
    }
    val notificationRegisterUrlPlaceholder: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "https://example.com/api/devices/register"
        AppLanguage.ENGLISH -> "https://example.com/api/devices/register"
        AppLanguage.ARABIC -> "https://example.com/api/devices/register"
        AppLanguage.PORTUGUESE -> "https://example.com/api/devices/register"
        AppLanguage.SPANISH -> "https://example.com/api/devices/register"
        AppLanguage.FRENCH -> "https://example.com/api/devices/register"
        AppLanguage.GERMAN -> "https://example.com/api/devices/register"
        AppLanguage.RUSSIAN -> "https://example.com/api/devices/register"
        AppLanguage.JAPANESE -> "https://example.com/api/devices/register"
        AppLanguage.KOREAN -> "https://example.com/api/devices/register"
    }
    val notificationWsHeaders: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "WebSocket 请求头"
        AppLanguage.ENGLISH -> "WebSocket Headers"
        AppLanguage.ARABIC -> "رؤوس WebSocket"
        AppLanguage.PORTUGUESE -> "Cabeçalhos WebSocket"
        AppLanguage.SPANISH -> "Encabezados WebSocket"
        AppLanguage.FRENCH -> "En-têtes WebSocket"
        AppLanguage.GERMAN -> "WebSocket-Header"
        AppLanguage.RUSSIAN -> "Заголовки WebSocket"
        AppLanguage.JAPANESE -> "WebSocket ヘッダー"
        AppLanguage.KOREAN -> "WebSocket 헤더"
    }
    val notificationWsHeadersPlaceholder: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Authorization: Bearer xxx\\nX-Custom: value"
        AppLanguage.ENGLISH -> "Authorization: Bearer xxx\\nX-Custom: value"
        AppLanguage.ARABIC -> "Authorization: Bearer xxx\\nX-Custom: value"
        AppLanguage.PORTUGUESE -> "Authorization: Bearer xxx\\nX-Custom: value"
        AppLanguage.SPANISH -> "Authorization: Bearer xxx\\nX-Custom: value"
        AppLanguage.FRENCH -> "Authorization: Bearer xxx\\nX-Custom: value"
        AppLanguage.GERMAN -> "Authorization: Bearer xxx\\nX-Custom: value"
        AppLanguage.RUSSIAN -> "Authorization: Bearer xxx\\nX-Custom: value"
        AppLanguage.JAPANESE -> "Authorization: Bearer xxx\\nX-Custom: value"
        AppLanguage.KOREAN -> "Authorization: Bearer xxx\\nX-Custom: value"
    }
    val notificationRegisterHeaders: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "注册接口请求头"
        AppLanguage.ENGLISH -> "Register Headers"
        AppLanguage.ARABIC -> "رؤوس التسجيل"
        AppLanguage.PORTUGUESE -> "Cabeçalhos de registro"
        AppLanguage.SPANISH -> "Encabezados de registro"
        AppLanguage.FRENCH -> "En-têtes d’enregistrement"
        AppLanguage.GERMAN -> "Registrierungs-Header"
        AppLanguage.RUSSIAN -> "Заголовки регистрации"
        AppLanguage.JAPANESE -> "登録ヘッダー"
        AppLanguage.KOREAN -> "등록 헤더"
    }
    val notificationRegisterHeadersPlaceholder: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Content-Type: application/json\\nAuthorization: Bearer xxx"
        AppLanguage.ENGLISH -> "Content-Type: application/json\\nAuthorization: Bearer xxx"
        AppLanguage.ARABIC -> "Content-Type: application/json\\nAuthorization: Bearer xxx"
        AppLanguage.PORTUGUESE -> "Content-Type: application/json\\nAuthorization: Bearer xxx"
        AppLanguage.SPANISH -> "Content-Type: application/json\\nAuthorization: Bearer xxx"
        AppLanguage.FRENCH -> "Content-Type: application/json\\nAuthorization: Bearer xxx"
        AppLanguage.GERMAN -> "Content-Type: application/json\\nAuthorization: Bearer xxx"
        AppLanguage.RUSSIAN -> "Content-Type: application/json\\nAuthorization: Bearer xxx"
        AppLanguage.JAPANESE -> "Content-Type: application/json\\nAuthorization: Bearer xxx"
        AppLanguage.KOREAN -> "Content-Type: application/json\\nAuthorization: Bearer xxx"
    }
    val notificationPollInterval: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "轮询间隔（分钟）"
        AppLanguage.ENGLISH -> "Poll Interval (min)"
        AppLanguage.ARABIC -> "فترة الاستطلاع (دقيقة)"
        AppLanguage.PORTUGUESE -> "Intervalo de sondagem (min)"
        AppLanguage.SPANISH -> "Intervalo de sondeo (min)"
        AppLanguage.FRENCH -> "Intervalle d'interrogation (min)"
        AppLanguage.GERMAN -> "Poll-Intervall (min)"
        AppLanguage.RUSSIAN -> "Интервал опроса (min)"
        AppLanguage.JAPANESE -> "ポーリング間隔 (min)"
        AppLanguage.KOREAN -> "폴링 간격 (min)"
    }

    val notificationFcmDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "使用你自己的 Firebase 项目，粘贴 google-services.json 自动填充凭据"
        AppLanguage.ENGLISH -> "Use your own Firebase project — paste google-services.json to auto-fill credentials"
        AppLanguage.ARABIC -> "استخدم مشروع Firebase الخاص بك — الصق google-services.json للتعبئة التلقائية"
        AppLanguage.PORTUGUESE -> "Use seu próprio projeto Firebase — cole o google-services.json para preencher as credenciais"
        AppLanguage.SPANISH -> "Usa tu propio proyecto Firebase — pega google-services.json para autocompletar las credenciales"
        AppLanguage.FRENCH -> "Utilisez votre projet Firebase — collez google-services.json pour remplir les identifiants"
        AppLanguage.GERMAN -> "Eigenes Firebase-Projekt verwenden — google-services.json einfügen zum Autofill der Zugangsdaten"
        AppLanguage.RUSSIAN -> "Используйте свой проект Firebase — вставьте google-services.json для автозаполнения данных"
        AppLanguage.JAPANESE -> "独自の Firebase プロジェクトを使用 — google-services.json を貼ると自動入力されます"
        AppLanguage.KOREAN -> "자체 Firebase 프로젝트 사용 — google-services.json을 붙여 자격 증명을 자동 채움"
    }
    val notificationFcmGoogleServicesJson: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "google-services.json（可选粘贴）"
        AppLanguage.ENGLISH -> "google-services.json (optional paste)"
        AppLanguage.ARABIC -> "google-services.json (لصق اختياري)"
        AppLanguage.PORTUGUESE -> "google-services.json (colar opcional)"
        AppLanguage.SPANISH -> "google-services.json (pegar opcional)"
        AppLanguage.FRENCH -> "google-services.json (collage optionnel)"
        AppLanguage.GERMAN -> "google-services.json (optional einfügen)"
        AppLanguage.RUSSIAN -> "google-services.json (опционально)"
        AppLanguage.JAPANESE -> "google-services.json（任意貼り付け）"
        AppLanguage.KOREAN -> "google-services.json(선택 붙여넣기)"
    }
    val notificationFcmGoogleServicesJsonPlaceholder: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "粘贴 Firebase google-services.json 内容"
        AppLanguage.ENGLISH -> "Paste Firebase google-services.json content"
        AppLanguage.ARABIC -> "الصق محتوى google-services.json"
        AppLanguage.PORTUGUESE -> "Cole o conteúdo do google-services.json"
        AppLanguage.SPANISH -> "Pega el contenido de google-services.json"
        AppLanguage.FRENCH -> "Collez le contenu de google-services.json"
        AppLanguage.GERMAN -> "Inhalt von google-services.json einfügen"
        AppLanguage.RUSSIAN -> "Вставьте содержимое google-services.json"
        AppLanguage.JAPANESE -> "google-services.json の内容を貼り付け"
        AppLanguage.KOREAN -> "google-services.json 내용을 붙여넣기"
    }
    val notificationFcmProjectId: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Firebase Project ID"
        AppLanguage.ENGLISH -> "Firebase Project ID"
        AppLanguage.ARABIC -> "معرف مشروع Firebase"
        AppLanguage.PORTUGUESE -> "ID do projeto Firebase"
        AppLanguage.SPANISH -> "ID del proyecto Firebase"
        AppLanguage.FRENCH -> "ID du projet Firebase"
        AppLanguage.GERMAN -> "Firebase Project ID"
        AppLanguage.RUSSIAN -> "Firebase Project ID"
        AppLanguage.JAPANESE -> "Firebase Project ID"
        AppLanguage.KOREAN -> "Firebase Project ID"
    }
    val notificationFcmProjectIdPlaceholder: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "my-firebase-project"
        AppLanguage.ENGLISH -> "my-firebase-project"
        AppLanguage.ARABIC -> "my-firebase-project"
        AppLanguage.PORTUGUESE -> "my-firebase-project"
        AppLanguage.SPANISH -> "my-firebase-project"
        AppLanguage.FRENCH -> "my-firebase-project"
        AppLanguage.GERMAN -> "my-firebase-project"
        AppLanguage.RUSSIAN -> "my-firebase-project"
        AppLanguage.JAPANESE -> "my-firebase-project"
        AppLanguage.KOREAN -> "my-firebase-project"
    }
    val notificationFcmApplicationId: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Mobile SDK App ID"
        AppLanguage.ENGLISH -> "Mobile SDK App ID"
        AppLanguage.ARABIC -> "معرف تطبيق Mobile SDK"
        AppLanguage.PORTUGUESE -> "ID do app Mobile SDK"
        AppLanguage.SPANISH -> "ID de app Mobile SDK"
        AppLanguage.FRENCH -> "ID d’app Mobile SDK"
        AppLanguage.GERMAN -> "Mobile SDK App-ID"
        AppLanguage.RUSSIAN -> "Mobile SDK App ID"
        AppLanguage.JAPANESE -> "Mobile SDK App ID"
        AppLanguage.KOREAN -> "Mobile SDK App ID"
    }
    val notificationFcmApplicationIdPlaceholder: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "1:1234567890:android:abcdef"
        AppLanguage.ENGLISH -> "1:1234567890:android:abcdef"
        AppLanguage.ARABIC -> "1:1234567890:android:abcdef"
        AppLanguage.PORTUGUESE -> "1:1234567890:android:abcdef"
        AppLanguage.SPANISH -> "1:1234567890:android:abcdef"
        AppLanguage.FRENCH -> "1:1234567890:android:abcdef"
        AppLanguage.GERMAN -> "1:1234567890:android:abcdef"
        AppLanguage.RUSSIAN -> "1:1234567890:android:abcdef"
        AppLanguage.JAPANESE -> "1:1234567890:android:abcdef"
        AppLanguage.KOREAN -> "1:1234567890:android:abcdef"
    }
    val notificationFcmApiKey: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Firebase API Key"
        AppLanguage.ENGLISH -> "Firebase API Key"
        AppLanguage.ARABIC -> "مفتاح API لـ Firebase"
        AppLanguage.PORTUGUESE -> "Chave de API do Firebase"
        AppLanguage.SPANISH -> "Clave API de Firebase"
        AppLanguage.FRENCH -> "Clé API Firebase"
        AppLanguage.GERMAN -> "Firebase API-Schlüssel"
        AppLanguage.RUSSIAN -> "Firebase API Key"
        AppLanguage.JAPANESE -> "Firebase API Key"
        AppLanguage.KOREAN -> "Firebase API Key"
    }
    val notificationFcmApiKeyPlaceholder: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "AIza..."
        AppLanguage.ENGLISH -> "AIza..."
        AppLanguage.ARABIC -> "AIza..."
        AppLanguage.PORTUGUESE -> "AIza..."
        AppLanguage.SPANISH -> "AIza..."
        AppLanguage.FRENCH -> "AIza..."
        AppLanguage.GERMAN -> "AIza..."
        AppLanguage.RUSSIAN -> "AIza..."
        AppLanguage.JAPANESE -> "AIza..."
        AppLanguage.KOREAN -> "AIza..."
    }
    val notificationFcmSenderId: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Sender ID (project_number)"
        AppLanguage.ENGLISH -> "Sender ID (project_number)"
        AppLanguage.ARABIC -> "معرف المرسل (project_number)"
        AppLanguage.PORTUGUESE -> "Sender ID (project_number)"
        AppLanguage.SPANISH -> "Sender ID (project_number)"
        AppLanguage.FRENCH -> "Sender ID (project_number)"
        AppLanguage.GERMAN -> "Sender-ID (project_number)"
        AppLanguage.RUSSIAN -> "Sender ID (project_number)"
        AppLanguage.JAPANESE -> "Sender ID (project_number)"
        AppLanguage.KOREAN -> "Sender ID (project_number)"
    }
    val notificationFcmSenderIdPlaceholder: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "1234567890"
        AppLanguage.ENGLISH -> "1234567890"
        AppLanguage.ARABIC -> "1234567890"
        AppLanguage.PORTUGUESE -> "1234567890"
        AppLanguage.SPANISH -> "1234567890"
        AppLanguage.FRENCH -> "1234567890"
        AppLanguage.GERMAN -> "1234567890"
        AppLanguage.RUSSIAN -> "1234567890"
        AppLanguage.JAPANESE -> "1234567890"
        AppLanguage.KOREAN -> "1234567890"
    }
    val notificationPollIntervalHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "最小 5 分钟，建议 15 分钟以上以减少耗电"
        AppLanguage.ENGLISH -> "Minimum 5 minutes. 15+ minutes recommended to reduce battery usage."
        AppLanguage.ARABIC -> "الحد الأدنى 5 دقائق. يُوصى بـ 15 دقيقة أو أكثر لتقليل استهلاك البطارية."
        AppLanguage.PORTUGUESE -> "Mínimo de 5 minutos. 15+ minutos recomendado para reduzir o uso da bateria."
        AppLanguage.SPANISH -> "Mínimo 5 minutos. Se recomiendan 15+ minutos para reducir el uso de batería."
        AppLanguage.FRENCH -> "Minimum 5 minutes. 15+ minutes recommandées pour réduire l'utilisation de la batterie."
        AppLanguage.GERMAN -> "Minimum 5 Minuten. 15+ Minuten empfohlen, um den Akkuverbrauch zu reduzieren."
        AppLanguage.RUSSIAN -> "Минимум 5 минут. Рекомендуется 15+ минут для снижения расхода батареи."
        AppLanguage.JAPANESE -> "最小5分。バッテリー消費を抑えるため15分以上を推奨。"
        AppLanguage.KOREAN -> "최소 5분. 배터리 사용을 줄이려면 15분 이상을 권장합니다."
    }

    val notificationPollMethod: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "请求方法"
        AppLanguage.ENGLISH -> "Request Method"
        AppLanguage.ARABIC -> "طريقة الطلب"
        AppLanguage.PORTUGUESE -> "Método de solicitação"
        AppLanguage.SPANISH -> "Método de solicitud"
        AppLanguage.FRENCH -> "Méthode de requête"
        AppLanguage.GERMAN -> "Anfragemethode"
        AppLanguage.RUSSIAN -> "Метод запроса"
        AppLanguage.JAPANESE -> "リクエストメソッド"
        AppLanguage.KOREAN -> "요청 메서드"
    }

    val notificationPollHeaders: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自定义 Headers"
        AppLanguage.ENGLISH -> "Custom Headers"
        AppLanguage.ARABIC -> "رؤوس مخصصة"
        AppLanguage.PORTUGUESE -> "Headers personalizados"
        AppLanguage.SPANISH -> "Headers personalizados"
        AppLanguage.FRENCH -> "Headers personnalisés"
        AppLanguage.GERMAN -> "Benutzerdefinierte Headers"
        AppLanguage.RUSSIAN -> "Пользовательские Headers"
        AppLanguage.JAPANESE -> "カスタム Headers"
        AppLanguage.KOREAN -> "사용자 지정 Headers"
    }

    val notificationPollHeadersPlaceholder: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> """{"Authorization": "Bearer xxx"}"""
        AppLanguage.ENGLISH -> """{"Authorization": "Bearer xxx"}"""
        AppLanguage.ARABIC -> """{"Authorization": "Bearer xxx"}"""
        AppLanguage.PORTUGUESE -> """{"Authorization": "Bearer xxx"}"""
        AppLanguage.SPANISH -> """{"Authorization": "Bearer xxx"}"""
        AppLanguage.FRENCH -> """{"Authorization": "Bearer xxx"}"""
        AppLanguage.GERMAN -> """{"Authorization": "Bearer xxx"}"""
        AppLanguage.RUSSIAN -> """{"Authorization": "Bearer xxx"}"""
        AppLanguage.JAPANESE -> """{"Authorization": "Bearer xxx"}"""
        AppLanguage.KOREAN -> """{"Authorization": "Bearer xxx"}"""
    }

    val notificationClickUrl: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "点击跳转路径"
        AppLanguage.ENGLISH -> "Click URL Path"
        AppLanguage.ARABIC -> "مسار النقر"
        AppLanguage.PORTUGUESE -> "Caminho de URL de clique"
        AppLanguage.SPANISH -> "Ruta de URL de clic"
        AppLanguage.FRENCH -> "Chemin d'URL de clic"
        AppLanguage.GERMAN -> "Klick-URL-Pfad"
        AppLanguage.RUSSIAN -> "Путь URL клика"
        AppLanguage.JAPANESE -> "クリック URL パス"
        AppLanguage.KOREAN -> "클릭 URL 경로"
    }

    val notificationClickUrlPlaceholder: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "/chat（留空仅打开 app）"
        AppLanguage.ENGLISH -> "/chat (leave empty to just open app)"
        AppLanguage.ARABIC -> "/chat (اتركه فارغًا لفتح التطبيق فقط)"
        AppLanguage.PORTUGUESE -> "/chat (deixe vazio para apenas abrir o app)"
        AppLanguage.SPANISH -> "/chat (dejar vacío para solo abrir la app)"
        AppLanguage.FRENCH -> "/chat (laisser vide pour juste ouvrir l'app)"
        AppLanguage.GERMAN -> "/chat (leer lassen, um nur die App zu öffnen)"
        AppLanguage.RUSSIAN -> "/chat (оставьте пустым, чтобы только открыть приложение)"
        AppLanguage.JAPANESE -> "/chat (アプリを開くだけの場合は空のまま)"
        AppLanguage.KOREAN -> "/chat (앱만 열려면 비워두세요)"
    }

    val genericAppLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "应用"
        AppLanguage.ENGLISH -> "App"
        AppLanguage.ARABIC -> "التطبيق"
        AppLanguage.PORTUGUESE -> "Aplicativo"
        AppLanguage.SPANISH -> "Aplicación"
        AppLanguage.FRENCH -> "Application"
        AppLanguage.GERMAN -> "App"
        AppLanguage.RUSSIAN -> "Приложение"
        AppLanguage.JAPANESE -> "アプリ"
        AppLanguage.KOREAN -> "앱"
    }

    val genericNotificationLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "通知"
        AppLanguage.ENGLISH -> "Notification"
        AppLanguage.ARABIC -> "إشعار"
        AppLanguage.PORTUGUESE -> "Notificação"
        AppLanguage.SPANISH -> "Notificación"
        AppLanguage.FRENCH -> "Notification"
        AppLanguage.GERMAN -> "Benachrichtigung"
        AppLanguage.RUSSIAN -> "Уведомление"
        AppLanguage.JAPANESE -> "通知"
        AppLanguage.KOREAN -> "알림"
    }

    val pollingNotificationChannelName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "轮询通知"
        AppLanguage.ENGLISH -> "Polling Notifications"
        AppLanguage.ARABIC -> "إشعارات الاستطلاع"
        AppLanguage.PORTUGUESE -> "Notificações por sondagem"
        AppLanguage.SPANISH -> "Notificaciones por sondeo"
        AppLanguage.FRENCH -> "Notifications par interrogation"
        AppLanguage.GERMAN -> "Polling-Benachrichtigungen"
        AppLanguage.RUSSIAN -> "Уведомления опроса"
        AppLanguage.JAPANESE -> "ポーリング通知"
        AppLanguage.KOREAN -> "폴링 알림"
    }

    val pollingNotificationChannelDescription: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "来自轮询服务的通知"
        AppLanguage.ENGLISH -> "Notifications from polling service"
        AppLanguage.ARABIC -> "إشعارات واردة من خدمة الاستطلاع"
        AppLanguage.PORTUGUESE -> "Notificações do serviço de sondagem"
        AppLanguage.SPANISH -> "Notificaciones del servicio de sondeo"
        AppLanguage.FRENCH -> "Notifications du service d'interrogation"
        AppLanguage.GERMAN -> "Benachrichtigungen vom Polling-Dienst"
        AppLanguage.RUSSIAN -> "Уведомления от службы опроса"
        AppLanguage.JAPANESE -> "ポーリングサービスからの通知"
        AppLanguage.KOREAN -> "폴링 서비스의 알림"
    }

    val pollingNotificationServiceTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "通知服务"
        AppLanguage.ENGLISH -> "Notification Service"
        AppLanguage.ARABIC -> "خدمة الإشعارات"
        AppLanguage.PORTUGUESE -> "Serviço de notificação"
        AppLanguage.SPANISH -> "Servicio de notificación"
        AppLanguage.FRENCH -> "Service de notification"
        AppLanguage.GERMAN -> "Benachrichtigungsdienst"
        AppLanguage.RUSSIAN -> "Служба уведомлений"
        AppLanguage.JAPANESE -> "通知サービス"
        AppLanguage.KOREAN -> "알림 서비스"
    }

    val pollingNotificationForegroundText: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "轮询：%s，每 %d 分钟一次"
        AppLanguage.ENGLISH -> "Polling: %s every %d min"
        AppLanguage.ARABIC -> "استطلاع: %s كل %d دقيقة"
        AppLanguage.PORTUGUESE -> "Sondagem: %s a cada %d min"
        AppLanguage.SPANISH -> "Sondeo: %s cada %d min"
        AppLanguage.FRENCH -> "Interrogation: %s toutes les %d min"
        AppLanguage.GERMAN -> "Polling: %s alle %d min"
        AppLanguage.RUSSIAN -> "Опрос: %s каждые %d min"
        AppLanguage.JAPANESE -> "ポーリング: %s、%d分ごと"
        AppLanguage.KOREAN -> "폴링: %s, %d분마다"
    }

    val websocketNotificationChannelName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "WebSocket 推送通知"
        AppLanguage.ENGLISH -> "WebSocket Push Notifications"
        AppLanguage.ARABIC -> "إشعارات دفع WebSocket"
        AppLanguage.PORTUGUESE -> "Notificações Push WebSocket"
        AppLanguage.SPANISH -> "Notificaciones Push WebSocket"
        AppLanguage.FRENCH -> "Notifications Push WebSocket"
        AppLanguage.GERMAN -> "WebSocket-Push-Benachrichtigungen"
        AppLanguage.RUSSIAN -> "WebSocket push-уведомления"
        AppLanguage.JAPANESE -> "WebSocket プッシュ通知"
        AppLanguage.KOREAN -> "WebSocket 푸시 알림"
    }
    val websocketNotificationChannelDescription: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "来自 WebSocket 推送通道的通知"
        AppLanguage.ENGLISH -> "Notifications from the WebSocket push channel"
        AppLanguage.ARABIC -> "إشعارات من قناة دفع WebSocket"
        AppLanguage.PORTUGUESE -> "Notificações do canal push WebSocket"
        AppLanguage.SPANISH -> "Notificaciones del canal push WebSocket"
        AppLanguage.FRENCH -> "Notifications du canal push WebSocket"
        AppLanguage.GERMAN -> "Benachrichtigungen vom WebSocket-Push-Kanal"
        AppLanguage.RUSSIAN -> "Уведомления из WebSocket push-канала"
        AppLanguage.JAPANESE -> "WebSocket プッシュチャネルからの通知"
        AppLanguage.KOREAN -> "WebSocket 푸시 채널의 알림"
    }
    val websocketNotificationServiceTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "推送服务"
        AppLanguage.ENGLISH -> "Push Service"
        AppLanguage.ARABIC -> "خدمة الدفع"
        AppLanguage.PORTUGUESE -> "Serviço de push"
        AppLanguage.SPANISH -> "Servicio de push"
        AppLanguage.FRENCH -> "Service de push"
        AppLanguage.GERMAN -> "Push-Dienst"
        AppLanguage.RUSSIAN -> "Служба push"
        AppLanguage.JAPANESE -> "プッシュサービス"
        AppLanguage.KOREAN -> "푸시 서비스"
    }
    val websocketNotificationForegroundText: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "WebSocket 连接中：%s"
        AppLanguage.ENGLISH -> "Connecting WebSocket: %s"
        AppLanguage.ARABIC -> "جارٍ الاتصال بـ WebSocket: %s"
        AppLanguage.PORTUGUESE -> "Conectando WebSocket: %s"
        AppLanguage.SPANISH -> "Conectando WebSocket: %s"
        AppLanguage.FRENCH -> "Connexion WebSocket : %s"
        AppLanguage.GERMAN -> "WebSocket wird verbunden: %s"
        AppLanguage.RUSSIAN -> "Подключение WebSocket: %s"
        AppLanguage.JAPANESE -> "WebSocket 接続中: %s"
        AppLanguage.KOREAN -> "WebSocket 연결 중: %s"
    }
    val websocketNotificationForegroundConnected: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "WebSocket 已连接：%s"
        AppLanguage.ENGLISH -> "WebSocket connected: %s"
        AppLanguage.ARABIC -> "تم الاتصال بـ WebSocket: %s"
        AppLanguage.PORTUGUESE -> "WebSocket conectado: %s"
        AppLanguage.SPANISH -> "WebSocket conectado: %s"
        AppLanguage.FRENCH -> "WebSocket connecté : %s"
        AppLanguage.GERMAN -> "WebSocket verbunden: %s"
        AppLanguage.RUSSIAN -> "WebSocket подключен: %s"
        AppLanguage.JAPANESE -> "WebSocket 接続済み: %s"
        AppLanguage.KOREAN -> "WebSocket 연결됨: %s"
    }
    val webAppNotificationChannelName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "网页通知"
        AppLanguage.ENGLISH -> "WebApp Notifications"
        AppLanguage.ARABIC -> "إشعارات الويب"
        AppLanguage.PORTUGUESE -> "Notificações WebApp"
        AppLanguage.SPANISH -> "Notificaciones WebApp"
        AppLanguage.FRENCH -> "Notifications WebApp"
        AppLanguage.GERMAN -> "WebApp-Benachrichtigungen"
        AppLanguage.RUSSIAN -> "Уведомления WebApp"
        AppLanguage.JAPANESE -> "WebApp通知"
        AppLanguage.KOREAN -> "WebApp 알림"
    }

    val fcmNotificationChannelName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "FCM 推送通知"
        AppLanguage.ENGLISH -> "FCM Push Notifications"
        AppLanguage.ARABIC -> "إشعارات دفع FCM"
        AppLanguage.PORTUGUESE -> "Notificações Push FCM"
        AppLanguage.SPANISH -> "Notificaciones Push FCM"
        AppLanguage.FRENCH -> "Notifications Push FCM"
        AppLanguage.GERMAN -> "FCM-Push-Benachrichtigungen"
        AppLanguage.RUSSIAN -> "FCM push-уведомления"
        AppLanguage.JAPANESE -> "FCM プッシュ通知"
        AppLanguage.KOREAN -> "FCM 푸시 알림"
    }
    val fcmNotificationChannelDescription: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "来自 Firebase Cloud Messaging 的通知"
        AppLanguage.ENGLISH -> "Notifications from Firebase Cloud Messaging"
        AppLanguage.ARABIC -> "إشعارات من Firebase Cloud Messaging"
        AppLanguage.PORTUGUESE -> "Notificações do Firebase Cloud Messaging"
        AppLanguage.SPANISH -> "Notificaciones de Firebase Cloud Messaging"
        AppLanguage.FRENCH -> "Notifications de Firebase Cloud Messaging"
        AppLanguage.GERMAN -> "Benachrichtigungen von Firebase Cloud Messaging"
        AppLanguage.RUSSIAN -> "Уведомления из Firebase Cloud Messaging"
        AppLanguage.JAPANESE -> "Firebase Cloud Messaging からの通知"
        AppLanguage.KOREAN -> "Firebase Cloud Messaging 알림"
    }
    val showAdvanced: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "显示高级设置"
        AppLanguage.ENGLISH -> "Show Advanced"
        AppLanguage.ARABIC -> "عرض الإعدادات المتقدمة"
        AppLanguage.PORTUGUESE -> "Mostrar avançadas"
        AppLanguage.SPANISH -> "Mostrar avanzadas"
        AppLanguage.FRENCH -> "Afficher avancées"
        AppLanguage.GERMAN -> "Erweiterte anzeigen"
        AppLanguage.RUSSIAN -> "Показать дополнительные"
        AppLanguage.JAPANESE -> "詳細を表示"
        AppLanguage.KOREAN -> "고급 표시"
    }

    val hideAdvanced: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "隐藏高级设置"
        AppLanguage.ENGLISH -> "Hide Advanced"
        AppLanguage.ARABIC -> "إخفاء الإعدادات المتقدمة"
        AppLanguage.PORTUGUESE -> "Ocultar avançadas"
        AppLanguage.SPANISH -> "Ocultar avanzadas"
        AppLanguage.FRENCH -> "Masquer avancées"
        AppLanguage.GERMAN -> "Erweiterte ausblenden"
        AppLanguage.RUSSIAN -> "Скрыть дополнительные"
        AppLanguage.JAPANESE -> "詳細を非表示"
        AppLanguage.KOREAN -> "고급 숨기기"
    }

    val dnsConfigTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自定义 DNS"
        AppLanguage.ENGLISH -> "Custom DNS"
        AppLanguage.ARABIC -> "DNS مخصص"
        AppLanguage.PORTUGUESE -> "DNS personalizado"
        AppLanguage.SPANISH -> "DNS personalizado"
        AppLanguage.FRENCH -> "DNS personnalisé"
        AppLanguage.GERMAN -> "Benutzerdefiniertes DNS"
        AppLanguage.RUSSIAN -> "Пользовательский DNS"
        AppLanguage.JAPANESE -> "カスタム DNS"
        AppLanguage.KOREAN -> "사용자 지정 DNS"
    }

    val dnsProviderLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "DNS 提供商"
        AppLanguage.ENGLISH -> "DNS Provider"
        AppLanguage.ARABIC -> "مزود DNS"
        AppLanguage.PORTUGUESE -> "Provedor de DNS"
        AppLanguage.SPANISH -> "Proveedor de DNS"
        AppLanguage.FRENCH -> "Fournisseur DNS"
        AppLanguage.GERMAN -> "DNS-Anbieter"
        AppLanguage.RUSSIAN -> "Провайдер DNS"
        AppLanguage.JAPANESE -> "DNS プロバイダー"
        AppLanguage.KOREAN -> "DNS 제공자"
    }

    val dnsCustomDohUrl: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自定义 DoH URL"
        AppLanguage.ENGLISH -> "Custom DoH URL"
        AppLanguage.ARABIC -> "رابط DoH مخصص"
        AppLanguage.PORTUGUESE -> "URL DoH personalizado"
        AppLanguage.SPANISH -> "URL DoH personalizada"
        AppLanguage.FRENCH -> "URL DoH personnalisée"
        AppLanguage.GERMAN -> "Benutzerdefinierte DoH-URL"
        AppLanguage.RUSSIAN -> "Пользовательский DoH URL"
        AppLanguage.JAPANESE -> "カスタム DoH URL"
        AppLanguage.KOREAN -> "사용자 지정 DoH URL"
    }

    val dnsCustomDohUrlPlaceholder: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "https://dns.example.com/dns-query"
        AppLanguage.ENGLISH -> "https://dns.example.com/dns-query"
        AppLanguage.ARABIC -> "https://dns.example.com/dns-query"
        AppLanguage.PORTUGUESE -> "https://dns.example.com/dns-query"
        AppLanguage.SPANISH -> "https://dns.example.com/dns-query"
        AppLanguage.FRENCH -> "https://dns.example.com/dns-query"
        AppLanguage.GERMAN -> "https://dns.example.com/dns-query"
        AppLanguage.RUSSIAN -> "https://dns.example.com/dns-query"
        AppLanguage.JAPANESE -> "https://dns.example.com/dns-query"
        AppLanguage.KOREAN -> "https://dns.example.com/dns-query"
    }

    val dohModeLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "DoH 模式"
        AppLanguage.ENGLISH -> "DoH Mode"
        AppLanguage.ARABIC -> "وضع DoH"
        AppLanguage.PORTUGUESE -> "Modo DoH"
        AppLanguage.SPANISH -> "Modo DoH"
        AppLanguage.FRENCH -> "Mode DoH"
        AppLanguage.GERMAN -> "DoH-Modus"
        AppLanguage.RUSSIAN -> "Режим DoH"
        AppLanguage.JAPANESE -> "DoHモード"
        AppLanguage.KOREAN -> "DoH 모드"
    }

    val dohModeAutomatic: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自动"
        AppLanguage.ENGLISH -> "Automatic"
        AppLanguage.ARABIC -> "تلقائي"
        AppLanguage.PORTUGUESE -> "Automático"
        AppLanguage.SPANISH -> "Automático"
        AppLanguage.FRENCH -> "Automatique"
        AppLanguage.GERMAN -> "Automatisch"
        AppLanguage.RUSSIAN -> "Автоматический"
        AppLanguage.JAPANESE -> "自動"
        AppLanguage.KOREAN -> "자동"
    }

    val dohModeStrict: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "严格"
        AppLanguage.ENGLISH -> "Strict"
        AppLanguage.ARABIC -> "صارم"
        AppLanguage.PORTUGUESE -> "Estrito"
        AppLanguage.SPANISH -> "Estricto"
        AppLanguage.FRENCH -> "Strict"
        AppLanguage.GERMAN -> "Streng"
        AppLanguage.RUSSIAN -> "Строгий"
        AppLanguage.JAPANESE -> "厳格"
        AppLanguage.KOREAN -> "엄격"
    }

    val dohModeAutomaticDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "当系统 DNS 响应缓慢或失败时，自动回退到 DoH。兼容性最佳。"
        AppLanguage.ENGLISH -> "Automatically falls back to DoH when system DNS is slow or fails. Best compatibility."
        AppLanguage.ARABIC -> "العودة تلقائيًا إلى DoH عندما يكون DNS بطيئًا أو يفشل. أفضل توافق."
        AppLanguage.PORTUGUESE -> "Recorre automaticamente ao DoH quando o DNS do sistema está lento ou falha. Melhor compatibilidade."
        AppLanguage.SPANISH -> "Recurre automáticamente a DoH cuando el DNS del sistema es lento o falla. Mejor compatibilidad."
        AppLanguage.FRENCH -> "Bascule automatiquement vers DoH lorsque le DNS système est lent ou échoue. Meilleure compatibilité."
        AppLanguage.GERMAN -> "Fällt automatisch auf DoH zurück, wenn das System-DNS langsam ist oder ausfällt. Beste Kompatibilität."
        AppLanguage.RUSSIAN -> "Автоматически откатывается на DoH, когда системный DNS медленный или не работает. Лучшая совместимость."
        AppLanguage.JAPANESE -> "システム DNS が遅いまたは失敗した場合、自動的に DoH にフォールバックします。最も互換性が高い設定。"
        AppLanguage.KOREAN -> "시스템 DNS가 느리거나 실패하면 자동으로 DoH로 폴백합니다. 최상의 호환성."
    }

    val dohModeStrictDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "强制所有 DNS 请求通过 DoH，如果 DoH 不可用则解析失败。隐私性最强。"
        AppLanguage.ENGLISH -> "Force all DNS queries through DoH. Fails if DoH is unavailable. Strongest privacy."
        AppLanguage.ARABIC -> "فرض جميع استعلامات DNS عبر DoH. يفشل إذا كان DoH غير متاح. أقوى خصوصية."
        AppLanguage.PORTUGUESE -> "Força todas as consultas DNS através do DoH. Falha se o DoH não estiver disponível. Privacidade mais forte."
        AppLanguage.SPANISH -> "Fuerza todas las consultas DNS a través de DoH. Falla si DoH no está disponible. Privacidad más fuerte."
        AppLanguage.FRENCH -> "Force toutes les requêtes DNS via DoH. Échoue si DoH n'est pas disponible. Confidentialité la plus forte."
        AppLanguage.GERMAN -> "Erzwingt alle DNS-Anfragen über DoH. Schlägt fehl, wenn DoH nicht verfügbar ist. Stärkster Datenschutz."
        AppLanguage.RUSSIAN -> "Принудительно направляет все DNS-запросы через DoH. Завершается ошибкой, если DoH недоступен. Самая сильная конфиденциальность."
        AppLanguage.JAPANESE -> "すべての DNS クエリを DoH 経由に強制します。DoH が利用不可の場合は失敗します。最も強力なプライバシー。"
        AppLanguage.KOREAN -> "모든 DNS 쿼리를 DoH를 통해 강제합니다. DoH를 사용할 수 없으면 실패합니다. 가장 강력한 개인정보 보호."
    }

    val dnsBypassSystemDns: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "绕过系统 DNS"
        AppLanguage.ENGLISH -> "Bypass System DNS"
        AppLanguage.ARABIC -> "تجاوز DNS النظام"
        AppLanguage.PORTUGUESE -> "Ignorar DNS do sistema"
        AppLanguage.SPANISH -> "Omitir DNS del sistema"
        AppLanguage.FRENCH -> "Contourner le DNS système"
        AppLanguage.GERMAN -> "System-DNS umgehen"
        AppLanguage.RUSSIAN -> "Обойти системный DNS"
        AppLanguage.JAPANESE -> "システム DNS をバイパス"
        AppLanguage.KOREAN -> "시스템 DNS 우회"
    }

    val dnsBypassSystemDnsDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "忽略系统 DNS 设置，仅使用 DoH 解析。可绕过 ISP DNS 污染/劫持。"
        AppLanguage.ENGLISH -> "Ignore system DNS settings, use DoH only. Bypasses ISP DNS pollution/hijacking."
        AppLanguage.ARABIC -> "تجاهل إعدادات DNS للنظام، استخدم DoH فقط. يتجاوز تلوث/اختطاف DNS لمزود الخدمة."
        AppLanguage.PORTUGUESE -> "Ignorar configurações de DNS do sistema, usar apenas DoH. Contorna poluição/sequestro de DNS do ISP."
        AppLanguage.SPANISH -> "Ignorar la configuración de DNS del sistema, usar solo DoH. Evita la contaminación/secuestro de DNS del ISP."
        AppLanguage.FRENCH -> "Ignorer les paramètres DNS du système, utiliser DoH uniquement. Contourne la pollution/le détournement DNS du ISP."
        AppLanguage.GERMAN -> "System-DNS-Einstellungen ignorieren, nur DoH verwenden. Umgeht ISP-DNS-Vergiftung/Entführung."
        AppLanguage.RUSSIAN -> "Игнорировать системные настройки DNS, использовать только DoH. Обходит загрязнение/перехват DNS провайдера."
        AppLanguage.JAPANESE -> "システム DNS 設定を無視し、DoH のみを使用します。ISP の DNS 汚染/ハイジャックを回避します。"
        AppLanguage.KOREAN -> "시스템 DNS 설정을 무시하고 DoH만 사용합니다. ISP DNS 오염/하이재킹을 우회합니다."
    }

    val dnsEchLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "加密客户端问候 (ECH)"
        AppLanguage.ENGLISH -> "Encrypted Client Hello (ECH)"
        AppLanguage.ARABIC -> "تشفير ترحيب العميل (ECH)"
        AppLanguage.PORTUGUESE -> "Hello de Cliente Criptografado (ECH)"
        AppLanguage.SPANISH -> "Client Hello Cifrado (ECH)"
        AppLanguage.FRENCH -> "Client Hello Chiffré (ECH)"
        AppLanguage.GERMAN -> "Verschlüsselter Client Hello (ECH)"
        AppLanguage.RUSSIAN -> "Зашифрованный Client Hello (ECH)"
        AppLanguage.JAPANESE -> "暗号化 Client Hello (ECH)"
        AppLanguage.KOREAN -> "암호화된 Client Hello (ECH)"
    }

    val dnsEchDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "加密 TLS 握手，隐藏你访问的网站域名，防止网络中间人看到你连了哪个站。两种引擎均支持；仅对支持 ECH 的网站生效。"
        AppLanguage.ENGLISH -> "Encrypts the TLS handshake to hide which website domain you visit from on-path observers. Works on both engines; only effective for sites that support ECH."
        AppLanguage.ARABIC -> "يشفّر مصافحة TLS لإخفاء اسم نطاق الموقع الذي تزوره عن المراقبين على المسار. يعمل على كلا المحركين؛ فعال فقط مع المواقع التي تدعم ECH."
        AppLanguage.PORTUGUESE -> "Criptografa o handshake TLS para ocultar o domínio do site que você visita de observadores no caminho. Funciona em ambos os motores; efetivo apenas para sites que suportam ECH."
        AppLanguage.SPANISH -> "Cifra el handshake TLS para ocultar el dominio del sitio web que visitas a observadores en ruta. Funciona en ambos motores; solo efectivo para sitios que soportan ECH."
        AppLanguage.FRENCH -> "Chiffre le handshake TLS pour cacher le domaine du site web que vous visitez aux observateurs sur le chemin. Fonctionne sur les deux moteurs ; efficace uniquement pour les sites qui supportent ECH."
        AppLanguage.GERMAN -> "Verschlüsselt den TLS-Handshake, um die besuchte Website-Domain vor Pfad-Beobachtern zu verbergen. Funktioniert auf beiden Engines; nur wirksam für Sites, die ECH unterstützen."
        AppLanguage.RUSSIAN -> "Шифрует TLS-рукопожатие, чтобы скрыть домен посещаемого сайта от наблюдателей на пути. Работает на обоих движках; эффективно только для сайтов, поддерживающих ECH."
        AppLanguage.JAPANESE -> "TLS ハンドシェイクを暗号化し、経路上の観察者から訪問先のウェブサイトドメインを隠します。両方のエンジンで動作し、ECH をサポートするサイトにのみ有効です。"
        AppLanguage.KOREAN -> "TLS 핸드셰이크를 암호화하여 경로상 관찰자로부터 방문하는 웹사이트 도메인을 숨깁니다. 두 엔진 모두에서 작동하며 ECH를 지원하는 사이트에만 유효합니다."
    }

    val dnsEchGeckoBadge: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "SNI 加密"
        AppLanguage.ENGLISH -> "Encrypted SNI"
        AppLanguage.ARABIC -> "SNI مشفّر"
        AppLanguage.PORTUGUESE -> "SNI criptografado"
        AppLanguage.SPANISH -> "SNI cifrado"
        AppLanguage.FRENCH -> "SNI chiffré"
        AppLanguage.GERMAN -> "Verschlüsseltes SNI"
        AppLanguage.RUSSIAN -> "Шифрование SNI"
        AppLanguage.JAPANESE -> "SNI 暗号化"
        AppLanguage.KOREAN -> "SNI 암호화"
    }
    val dnsEchEngineWarn: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "系统内核下 ECH 经本地桥接的 Chromium 网络组件实现：首次使用会自动下载组件（约 14MB），导出的 APK 将内置；与 SOCKS 上游代理互斥。若本地桥未启用，连接将回退为普通加密。"
        AppLanguage.ENGLISH -> "On the system engine ECH rides the locally-bridged Chromium network component: first use auto-downloads it (~14 MB) and exported APKs embed it; mutually exclusive with a SOCKS upstream proxy. Without the component the connection falls back to ordinary encryption."
        AppLanguage.ARABIC -> "على محرك النظام يعمل ECH عبر مكوّن شبكة Chromium المتصل محليًا: الاستخدام الأول ينزّله تلقائيًا (حوالي 14 ميغابايت) وتُدمجه ملفات APK المصدَّرة؛ وهو حصري مع وكيل SOCKS. بدونه تعود الاتصالات إلى التشفير العادي."
        AppLanguage.PORTUGUESE -> "No motor do sistema, o ECH usa o componente de rede Chromium conectado localmente: o primeiro uso o baixa automaticamente (~14 MB) e os APKs exportados o incorporam; é mutuamente exclusivo com um proxy SOCKS upstream. Sem ele, a conexão volta à criptografia comum."
        AppLanguage.SPANISH -> "En el motor del sistema, ECH funciona mediante el componente de red Chromium conectado localmente: el primer uso lo descarga automáticamente (~14 MB) y los APK exportados lo incorporan; es mutuamente excluyente con un proxy SOCKS. Sin él, la conexión vuelve al cifrado ordinario."
        AppLanguage.FRENCH -> "Sur le moteur système, ECH passe par le composant réseau Chromium relié localement : le premier usage le télécharge automatiquement (~14 Mo) et les APK exportés l'intègrent ; il est mutuellement exclusif avec un proxy SOCKS. Sans lui, la connexion revient au chiffrement ordinaire."
        AppLanguage.GERMAN -> "Auf der System-Engine läuft ECH über die lokal angebundene Chromium-Netzkomponente: Die erste Nutzung lädt sie automatisch herunter (~14 MB), exportierte APKs binden sie ein; gegenseitig ausgeschlossen mit einem SOCKS-Upstream. Ohne sie fällt die Verbindung auf gewöhnliche Verschlüsselung zurück."
        AppLanguage.RUSSIAN -> "На системном движке ECH работает через локально подключённый сетевой компонент Chromium: первое использование скачивает его автоматически (~14 МБ), экспортируемые APK встраивают его; взаимоисключается с прокси SOCKS. Без него соединение откатывается к обычному шифрованию."
        AppLanguage.JAPANESE -> "システムエンジンでは ECH はローカルにブリッジされた Chromium ネットワークコンポーネント経由で動作します。初回使用時に自動ダウンロード（約 14MB）され、エクスポートされた APK には組み込まれます。SOCKS 上流プロキシとは排他です。コンポーネントがない場合は通常の暗号化にフォールバックします。"
        AppLanguage.KOREAN -> "시스템 엔진에서 ECH는 로컬로 브리지된 Chromium 네트워크 컴포넌트를 통해 작동합니다. 최초 사용 시 자동 다운로드(약 14MB)되며 내보낸 APK에는 포함됩니다. SOCKS 업스트림 프록시와는 상호 배타적입니다. 컴포넌트가 없으면 일반 암호화로 폴백합니다."
    }

    val browserDisguiseTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "浏览器伪装"
        AppLanguage.ENGLISH -> "Browser Disguise"
        AppLanguage.ARABIC -> "تمويه المتصفح"
        AppLanguage.PORTUGUESE -> "Disfarce do navegador"
        AppLanguage.SPANISH -> "Disfraz del navegador"
        AppLanguage.FRENCH -> "Déguisement du navigateur"
        AppLanguage.GERMAN -> "Browser-Tarnung"
        AppLanguage.RUSSIAN -> "Маскировка браузера"
        AppLanguage.JAPANESE -> "ブラウザ偽装"
        AppLanguage.KOREAN -> "브라우저 위장"
    }

    val browserDisguiseEnable: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "启用浏览器伪装"
        AppLanguage.ENGLISH -> "Enable Browser Disguise"
        AppLanguage.ARABIC -> "تفعيل تمويه المتصفح"
        AppLanguage.PORTUGUESE -> "Ativar disfarce do navegador"
        AppLanguage.SPANISH -> "Habilitar disfraz del navegador"
        AppLanguage.FRENCH -> "Activer le déguisement du navigateur"
        AppLanguage.GERMAN -> "Browser-Tarnung aktivieren"
        AppLanguage.RUSSIAN -> "Включить маскировку браузера"
        AppLanguage.JAPANESE -> "ブラウザ偽装を有効化"
        AppLanguage.KOREAN -> "브라우저 위장 활성화"
    }

    val browserDisguiseEnableDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "多层次反指纹技术，使 WebView 无法被网站检测"
        AppLanguage.ENGLISH -> "Multi-layer anti-fingerprinting to make WebView undetectable"
        AppLanguage.ARABIC -> "تقنية متعددة الطبقات لمكافحة البصمات لجعل WebView غير قابل للكشف"
        AppLanguage.PORTUGUESE -> "Anti-impressão digital de múltiplas camadas para tornar o WebView indetectável"
        AppLanguage.SPANISH -> "Anti-huella digital de múltiples capas para hacer que WebView sea indetectable"
        AppLanguage.FRENCH -> "Anti-empreinte multi-couches pour rendre WebView indétectable"
        AppLanguage.GERMAN -> "Mehrschichtiger Anti-Fingerprinting, um WebView unentdeckbar zu machen"
        AppLanguage.RUSSIAN -> "Многоуровневая анти-фингерпринтинг защита, делающая WebView необнаружимым"
        AppLanguage.JAPANESE -> "WebViewを検出不能にする多層アンチフィンガープリント"
        AppLanguage.KOREAN -> "WebView를 감지할 수 없게 만드는 다중 계층 안티 핑거프린팅"
    }

    val browserDisguisePreset: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "伪装预设"
        AppLanguage.ENGLISH -> "Disguise Preset"
        AppLanguage.ARABIC -> "إعداد مسبق للتمويه"
        AppLanguage.PORTUGUESE -> "Predefinição de disfarce"
        AppLanguage.SPANISH -> "Preset de disfraz"
        AppLanguage.FRENCH -> "Préréglage de déguisement"
        AppLanguage.GERMAN -> "Tarnungs-Voreinstellung"
        AppLanguage.RUSSIAN -> "Предустановка маскировки"
        AppLanguage.JAPANESE -> "偽装プリセット"
        AppLanguage.KOREAN -> "위장 사전 설정"
    }

    val browserDisguiseCoverage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "覆盖率"
        AppLanguage.ENGLISH -> "Coverage"
        AppLanguage.ARABIC -> "التغطية"
        AppLanguage.PORTUGUESE -> "Cobertura"
        AppLanguage.SPANISH -> "Cobertura"
        AppLanguage.FRENCH -> "Couverture"
        AppLanguage.GERMAN -> "Abdeckung"
        AppLanguage.RUSSIAN -> "Покрытие"
        AppLanguage.JAPANESE -> "カバレッジ"
        AppLanguage.KOREAN -> "적용 범위"
    }

    val browserDisguiseCoverageTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "指纹覆盖率"
        AppLanguage.ENGLISH -> "Fingerprint Coverage"
        AppLanguage.ARABIC -> "تغطية البصمة"
        AppLanguage.PORTUGUESE -> "Cobertura de impressão digital"
        AppLanguage.SPANISH -> "Cobertura de huella digital"
        AppLanguage.FRENCH -> "Couverture d'empreinte"
        AppLanguage.GERMAN -> "Fingerprint-Abdeckung"
        AppLanguage.RUSSIAN -> "Покрытие фингерпринтинга"
        AppLanguage.JAPANESE -> "フィンガープリントカバレッジ"
        AppLanguage.KOREAN -> "핑거프린트 적용 범위"
    }

    val browserDisguiseActiveVectors: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "活跃向量"
        AppLanguage.ENGLISH -> "Active Vectors"
        AppLanguage.ARABIC -> "المتجهات النشطة"
        AppLanguage.PORTUGUESE -> "Vetores ativos"
        AppLanguage.SPANISH -> "Vectores activos"
        AppLanguage.FRENCH -> "Vecteurs actifs"
        AppLanguage.GERMAN -> "Aktive Vektoren"
        AppLanguage.RUSSIAN -> "Активные векторы"
        AppLanguage.JAPANESE -> "アクティブベクトル"
        AppLanguage.KOREAN -> "활성 벡터"
    }

    val browserDisguiseAdvanced: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "高级向量控制"
        AppLanguage.ENGLISH -> "Advanced Vector Controls"
        AppLanguage.ARABIC -> "عناصر التحكم المتقدمة"
        AppLanguage.PORTUGUESE -> "Controles avançados de vetor"
        AppLanguage.SPANISH -> "Controles avanzados de vector"
        AppLanguage.FRENCH -> "Contrôles vectoriels avancés"
        AppLanguage.GERMAN -> "Erweiterte Vektor-Kontrollen"
        AppLanguage.RUSSIAN -> "Расширенные элементы управления векторами"
        AppLanguage.JAPANESE -> "高度なベクトルコントロール"
        AppLanguage.KOREAN -> "고급 벡터 컨트롤"
    }

    val browserDisguiseL2Title: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Level 2 · 指纹向量伪装"
        AppLanguage.ENGLISH -> "Level 2 · Fingerprint Spoofing"
        AppLanguage.ARABIC -> "المستوى 2 · تزييف البصمات"
        AppLanguage.PORTUGUESE -> "Level 2 · Falsificação de impressão digital"
        AppLanguage.SPANISH -> "Level 2 · Falsificación de huella digital"
        AppLanguage.FRENCH -> "Level 2 · Falsification d'empreinte"
        AppLanguage.GERMAN -> "Level 2 · Fingerprint-Spoofing"
        AppLanguage.RUSSIAN -> "Level 2 · Подмена фингерпринта"
        AppLanguage.JAPANESE -> "Level 2 · フィンガープリント偽装"
        AppLanguage.KOREAN -> "Level 2 · 핑거프린트 스푸핑"
    }

    val browserDisguiseCanvasNoise: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Canvas 指纹噪声"
        AppLanguage.ENGLISH -> "Canvas Fingerprint Noise"
        AppLanguage.ARABIC -> "ضوضاء بصمة Canvas"
        AppLanguage.PORTUGUESE -> "Ruído de impressão digital Canvas"
        AppLanguage.SPANISH -> "Ruido de huella digital Canvas"
        AppLanguage.FRENCH -> "Bruit d'empreinte Canvas"
        AppLanguage.GERMAN -> "Canvas-Fingerprint-Rauschen"
        AppLanguage.RUSSIAN -> "Шум фингерпринта Canvas"
        AppLanguage.JAPANESE -> "Canvas フィンガープリントノイズ"
        AppLanguage.KOREAN -> "Canvas 핑거프린트 노이즈"
    }
    val browserDisguiseCanvasNoiseDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "在 toDataURL/getImageData 中注入亚像素噪声"
        AppLanguage.ENGLISH -> "Inject sub-pixel noise into toDataURL/getImageData"
        AppLanguage.ARABIC -> "حقن ضوضاء دون البكسل في toDataURL/getImageData"
        AppLanguage.PORTUGUESE -> "Injetar ruído sub-pixel em toDataURL/getImageData"
        AppLanguage.SPANISH -> "Inyectar ruido sub-píxel en toDataURL/getImageData"
        AppLanguage.FRENCH -> "Injecter du bruit sous-pixel dans toDataURL/getImageData"
        AppLanguage.GERMAN -> "Sub-Pixel-Rauschen in toDataURL/getImageData injizieren"
        AppLanguage.RUSSIAN -> "Внедрять шум суб-пикселя в toDataURL/getImageData"
        AppLanguage.JAPANESE -> "toDataURL/getImageData にサブピクセルノイズを注入"
        AppLanguage.KOREAN -> "toDataURL/getImageData에 서브픽셀 노이즈 주입"
    }

    val browserDisguiseWebGL: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "WebGL 渲染器伪装"
        AppLanguage.ENGLISH -> "WebGL Renderer Spoof"
        AppLanguage.ARABIC -> "تزييف عارض WebGL"
        AppLanguage.PORTUGUESE -> "Falsificação de renderizador WebGL"
        AppLanguage.SPANISH -> "Falsificación de renderizador WebGL"
        AppLanguage.FRENCH -> "Falsification du rendu WebGL"
        AppLanguage.GERMAN -> "WebGL-Renderer-Spoofing"
        AppLanguage.RUSSIAN -> "Подмена рендерера WebGL"
        AppLanguage.JAPANESE -> "WebGL レンダラー偽装"
        AppLanguage.KOREAN -> "WebGL 렌더러 스푸핑"
    }
    val browserDisguiseWebGLDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "替换 GPU 渲染器/供应商信息为标准 PC 值"
        AppLanguage.ENGLISH -> "Replace GPU renderer/vendor info with standard PC values"
        AppLanguage.ARABIC -> "استبدال معلومات GPU بقيم PC قياسية"
        AppLanguage.PORTUGUESE -> "Substituir informações de renderizador/fabricante da GPU por valores padrão de PC"
        AppLanguage.SPANISH -> "Reemplazar información de renderizador/fabricante de GPU por valores estándar de PC"
        AppLanguage.FRENCH -> "Remplacer les infos de rendu/fabricant GPU par des valeurs PC standard"
        AppLanguage.GERMAN -> "GPU-Renderer/Anbieter-Info durch Standard-PC-Werte ersetzen"
        AppLanguage.RUSSIAN -> "Заменять информацию рендерера/производителя GPU на стандартные значения PC"
        AppLanguage.JAPANESE -> "GPU レンダラー/ベンダー情報を標準的な PC の値に置き換え"
        AppLanguage.KOREAN -> "GPU 렌더러/공급업체 정보를 표준 PC 값으로 교체"
    }

    val browserDisguiseAudio: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "AudioContext 噪声"
        AppLanguage.ENGLISH -> "AudioContext Noise"
        AppLanguage.ARABIC -> "ضوضاء AudioContext"
        AppLanguage.PORTUGUESE -> "Ruído de AudioContext"
        AppLanguage.SPANISH -> "Ruido de AudioContext"
        AppLanguage.FRENCH -> "Bruit d'AudioContext"
        AppLanguage.GERMAN -> "AudioContext-Rauschen"
        AppLanguage.RUSSIAN -> "Шум AudioContext"
        AppLanguage.JAPANESE -> "AudioContext ノイズ"
        AppLanguage.KOREAN -> "AudioContext 노이즈"
    }
    val browserDisguiseAudioDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "在音频分析数据中注入微小频率偏移"
        AppLanguage.ENGLISH -> "Inject micro-frequency offsets into audio analysis data"
        AppLanguage.ARABIC -> "حقن إزاحات تردد دقيقة في بيانات تحليل الصوت"
        AppLanguage.PORTUGUESE -> "Injetar deslocamentos de microfrequência nos dados de análise de áudio"
        AppLanguage.SPANISH -> "Inyectar desplazamientos de microfrecuencia en los datos de análisis de audio"
        AppLanguage.FRENCH -> "Injecter des décalages de micro-fréquence dans les données d'analyse audio"
        AppLanguage.GERMAN -> "Mikrofrequenz-Offsets in Audio-Analysedaten injizieren"
        AppLanguage.RUSSIAN -> "Внедрять микрочастотные смещения в данные аудиоанализа"
        AppLanguage.JAPANESE -> "音声分析データに微小周波数オフセットを注入"
        AppLanguage.KOREAN -> "오디오 분석 데이터에 미세 주파수 오프셋 주입"
    }

    val browserDisguiseScreen: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "屏幕分辨率伪装"
        AppLanguage.ENGLISH -> "Screen Resolution Spoof"
        AppLanguage.ARABIC -> "تزييف دقة الشاشة"
        AppLanguage.PORTUGUESE -> "Falsificação de resolução de tela"
        AppLanguage.SPANISH -> "Falsificación de resolución de pantalla"
        AppLanguage.FRENCH -> "Falsification de résolution d'écran"
        AppLanguage.GERMAN -> "Bildschirmauflösungs-Spoofing"
        AppLanguage.RUSSIAN -> "Подмена разрешения экрана"
        AppLanguage.JAPANESE -> "画面解像度偽装"
        AppLanguage.KOREAN -> "화면 해상도 스푸핑"
    }
    val browserDisguiseScreenDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "伪装 screen.width/height 和 devicePixelRatio"
        AppLanguage.ENGLISH -> "Spoof screen.width/height and devicePixelRatio"
        AppLanguage.ARABIC -> "تزييف أبعاد الشاشة ونسبة البكسل"
        AppLanguage.PORTUGUESE -> "Falsificar screen.width/height e devicePixelRatio"
        AppLanguage.SPANISH -> "Falsificar screen.width/height y devicePixelRatio"
        AppLanguage.FRENCH -> "Falsifier screen.width/height et devicePixelRatio"
        AppLanguage.GERMAN -> "screen.width/height und devicePixelRatio fälschen"
        AppLanguage.RUSSIAN -> "Подмена screen.width/height и devicePixelRatio"
        AppLanguage.JAPANESE -> "screen.width/height と devicePixelRatio を偽装"
        AppLanguage.KOREAN -> "screen.width/height 및 devicePixelRatio 위조"
    }

    val browserDisguiseClientRects: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "ClientRects 微偏移"
        AppLanguage.ENGLISH -> "ClientRects Micro-offset"
        AppLanguage.ARABIC -> "إزاحة دقيقة لـ ClientRects"
        AppLanguage.PORTUGUESE -> "Microdeslocamento de ClientRects"
        AppLanguage.SPANISH -> "Microdesplazamiento de ClientRects"
        AppLanguage.FRENCH -> "Micro-décalage des ClientRects"
        AppLanguage.GERMAN -> "ClientRects-Mikroversatz"
        AppLanguage.RUSSIAN -> "Микросмещение ClientRects"
        AppLanguage.JAPANESE -> "ClientRects マイクロオフセット"
        AppLanguage.KOREAN -> "ClientRects 마이크로 오프셋"
    }
    val browserDisguiseClientRectsDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "防止 DOMRect 枚举指纹识别"
        AppLanguage.ENGLISH -> "Prevent DOMRect enumeration fingerprinting"
        AppLanguage.ARABIC -> "منع بصمات تعداد DOMRect"
        AppLanguage.PORTUGUESE -> "Evitar fingerprinting por enumeração de DOMRect"
        AppLanguage.SPANISH -> "Evitar fingerprinting por enumeración de DOMRect"
        AppLanguage.FRENCH -> "Empêcher l'empreinte par énumération de DOMRect"
        AppLanguage.GERMAN -> "DOMRect-Enumerations-Fingerprinting verhindern"
        AppLanguage.RUSSIAN -> "Предотвращение фингерпринтинга через перечисление DOMRect"
        AppLanguage.JAPANESE -> "DOMRect 列挙によるフィンガープリンティングを防止"
        AppLanguage.KOREAN -> "DOMRect 열거 지문 인식 방지"
    }

    val browserDisguiseL3Title: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Level 3 · 环境伪装"
        AppLanguage.ENGLISH -> "Level 3 · Environment Spoofing"
        AppLanguage.ARABIC -> "المستوى 3 · تزييف البيئة"
        AppLanguage.PORTUGUESE -> "Level 3 · Falsificação de Ambiente"
        AppLanguage.SPANISH -> "Level 3 · Falsificación de Entorno"
        AppLanguage.FRENCH -> "Level 3 · Falsification de l'Environnement"
        AppLanguage.GERMAN -> "Level 3 · Umgebungsfälschung"
        AppLanguage.RUSSIAN -> "Level 3 · Подмена среды"
        AppLanguage.JAPANESE -> "Level 3 · 環境偽装"
        AppLanguage.KOREAN -> "Level 3 · 환경 위조"
    }

    val browserDisguiseTimezone: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "时区伪装"
        AppLanguage.ENGLISH -> "Timezone Spoof"
        AppLanguage.ARABIC -> "تزييف المنطقة الزمنية"
        AppLanguage.PORTUGUESE -> "Falsificação de Fuso Horário"
        AppLanguage.SPANISH -> "Falsificación de Zona Horaria"
        AppLanguage.FRENCH -> "Falsification de Fuseau Horaire"
        AppLanguage.GERMAN -> "Zeitzonen-Fälschung"
        AppLanguage.RUSSIAN -> "Подмена часового пояса"
        AppLanguage.JAPANESE -> "タイムゾーン偽装"
        AppLanguage.KOREAN -> "시간대 위조"
    }
    val browserDisguiseTimezoneDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "覆写 Intl.DateTimeFormat 和 Date.getTimezoneOffset"
        AppLanguage.ENGLISH -> "Override Intl.DateTimeFormat and Date.getTimezoneOffset"
        AppLanguage.ARABIC -> "تجاوز DateTimeFormat و getTimezoneOffset"
        AppLanguage.PORTUGUESE -> "Substituir Intl.DateTimeFormat e Date.getTimezoneOffset"
        AppLanguage.SPANISH -> "Sobrescribir Intl.DateTimeFormat y Date.getTimezoneOffset"
        AppLanguage.FRENCH -> "Remplacer Intl.DateTimeFormat et Date.getTimezoneOffset"
        AppLanguage.GERMAN -> "Intl.DateTimeFormat und Date.getTimezoneOffset überschreiben"
        AppLanguage.RUSSIAN -> "Переопределение Intl.DateTimeFormat и Date.getTimezoneOffset"
        AppLanguage.JAPANESE -> "Intl.DateTimeFormat と Date.getTimezoneOffset を上書き"
        AppLanguage.KOREAN -> "Intl.DateTimeFormat 및 Date.getTimezoneOffset 재정의"
    }

    val browserDisguiseLanguage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "语言伪装"
        AppLanguage.ENGLISH -> "Language Spoof"
        AppLanguage.ARABIC -> "تزييف اللغة"
        AppLanguage.PORTUGUESE -> "Falsificação de Idioma"
        AppLanguage.SPANISH -> "Falsificación de Idioma"
        AppLanguage.FRENCH -> "Falsification de Langue"
        AppLanguage.GERMAN -> "Sprachfälschung"
        AppLanguage.RUSSIAN -> "Подмена языка"
        AppLanguage.JAPANESE -> "言語偽装"
        AppLanguage.KOREAN -> "언어 위조"
    }
    val browserDisguiseLanguageDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "覆写 navigator.language 和 navigator.languages"
        AppLanguage.ENGLISH -> "Override navigator.language and navigator.languages"
        AppLanguage.ARABIC -> "تجاوز لغات المتصفح"
        AppLanguage.PORTUGUESE -> "Substituir navigator.language e navigator.languages"
        AppLanguage.SPANISH -> "Sobrescribir navigator.language y navigator.languages"
        AppLanguage.FRENCH -> "Remplacer navigator.language et navigator.languages"
        AppLanguage.GERMAN -> "navigator.language und navigator.languages überschreiben"
        AppLanguage.RUSSIAN -> "Переопределение navigator.language и navigator.languages"
        AppLanguage.JAPANESE -> "navigator.language と navigator.languages を上書き"
        AppLanguage.KOREAN -> "navigator.language 및 navigator.languages 재정의"
    }

    val browserDisguisePlatform: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "平台伪装"
        AppLanguage.ENGLISH -> "Platform Spoof"
        AppLanguage.ARABIC -> "تزييف المنصة"
        AppLanguage.PORTUGUESE -> "Falsificação de Plataforma"
        AppLanguage.SPANISH -> "Falsificación de Plataforma"
        AppLanguage.FRENCH -> "Falsification de Plateforme"
        AppLanguage.GERMAN -> "Plattformfälschung"
        AppLanguage.RUSSIAN -> "Подмена платформы"
        AppLanguage.JAPANESE -> "プラットフォーム偽装"
        AppLanguage.KOREAN -> "플랫폼 위조"
    }
    val browserDisguisePlatformDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "伪装 navigator.platform 为 Win32/MacIntel 等"
        AppLanguage.ENGLISH -> "Spoof navigator.platform to Win32/MacIntel etc."
        AppLanguage.ARABIC -> "تزييف منصة المتصفح إلى Win32 أو MacIntel"
        AppLanguage.PORTUGUESE -> "Falsificar navigator.platform para Win32/MacIntel etc."
        AppLanguage.SPANISH -> "Falsificar navigator.platform a Win32/MacIntel etc."
        AppLanguage.FRENCH -> "Falsifier navigator.platform vers Win32/MacIntel etc."
        AppLanguage.GERMAN -> "navigator.platform auf Win32/MacIntel usw. fälschen"
        AppLanguage.RUSSIAN -> "Подмена navigator.platform на Win32/MacIntel и т.д."
        AppLanguage.JAPANESE -> "navigator.platform を Win32/MacIntel などに偽装"
        AppLanguage.KOREAN -> "navigator.platform을 Win32/MacIntel 등으로 위조"
    }

    val browserDisguiseHardware: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "CPU 核心数伪装"
        AppLanguage.ENGLISH -> "CPU Cores Spoof"
        AppLanguage.ARABIC -> "تزييف عدد أنوية المعالج"
        AppLanguage.PORTUGUESE -> "Falsificação de Núcleos de CPU"
        AppLanguage.SPANISH -> "Falsificación de Núcleos de CPU"
        AppLanguage.FRENCH -> "Falsification des Cœurs CPU"
        AppLanguage.GERMAN -> "CPU-Kern-Fälschung"
        AppLanguage.RUSSIAN -> "Подмена числа ядер CPU"
        AppLanguage.JAPANESE -> "CPU コア数偽装"
        AppLanguage.KOREAN -> "CPU 코어 수 위조"
    }
    val browserDisguiseHardwareDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "覆写 navigator.hardwareConcurrency"
        AppLanguage.ENGLISH -> "Override navigator.hardwareConcurrency"
        AppLanguage.ARABIC -> "تجاوز عدد الأنوية المتاحة"
        AppLanguage.PORTUGUESE -> "Substituir navigator.hardwareConcurrency"
        AppLanguage.SPANISH -> "Sobrescribir navigator.hardwareConcurrency"
        AppLanguage.FRENCH -> "Remplacer navigator.hardwareConcurrency"
        AppLanguage.GERMAN -> "navigator.hardwareConcurrency überschreiben"
        AppLanguage.RUSSIAN -> "Переопределение navigator.hardwareConcurrency"
        AppLanguage.JAPANESE -> "navigator.hardwareConcurrency を上書き"
        AppLanguage.KOREAN -> "navigator.hardwareConcurrency 재정의"
    }

    val browserDisguiseMemory: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "设备内存伪装"
        AppLanguage.ENGLISH -> "Device Memory Spoof"
        AppLanguage.ARABIC -> "تزييف ذاكرة الجهاز"
        AppLanguage.PORTUGUESE -> "Falsificação de Memória do Dispositivo"
        AppLanguage.SPANISH -> "Falsificación de Memoria del Dispositivo"
        AppLanguage.FRENCH -> "Falsification de Mémoire du Dispositif"
        AppLanguage.GERMAN -> "Gerätespeicher-Fälschung"
        AppLanguage.RUSSIAN -> "Подмена памяти устройства"
        AppLanguage.JAPANESE -> "デバイスメモリ偽装"
        AppLanguage.KOREAN -> "기기 메모리 위조"
    }
    val browserDisguiseMemoryDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "覆写 navigator.deviceMemory"
        AppLanguage.ENGLISH -> "Override navigator.deviceMemory"
        AppLanguage.ARABIC -> "تجاوز ذاكرة الجهاز المعلنة"
        AppLanguage.PORTUGUESE -> "Substituir navigator.deviceMemory"
        AppLanguage.SPANISH -> "Sobrescribir navigator.deviceMemory"
        AppLanguage.FRENCH -> "Remplacer navigator.deviceMemory"
        AppLanguage.GERMAN -> "navigator.deviceMemory überschreiben"
        AppLanguage.RUSSIAN -> "Переопределение navigator.deviceMemory"
        AppLanguage.JAPANESE -> "navigator.deviceMemory を上書き"
        AppLanguage.KOREAN -> "navigator.deviceMemory 재정의"
    }

    val browserDisguiseL4Title: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Level 4 · 深度伪装"
        AppLanguage.ENGLISH -> "Level 4 · Deep Disguise"
        AppLanguage.ARABIC -> "المستوى 4 · التمويه العميق"
        AppLanguage.PORTUGUESE -> "Level 4 · Disfarce Profundo"
        AppLanguage.SPANISH -> "Level 4 · Disfraz Profundo"
        AppLanguage.FRENCH -> "Level 4 · Déguisement Profond"
        AppLanguage.GERMAN -> "Level 4 · Tiefe Tarnung"
        AppLanguage.RUSSIAN -> "Level 4 · Глубокая маскировка"
        AppLanguage.JAPANESE -> "Level 4 · 深度偽装"
        AppLanguage.KOREAN -> "Level 4 · 심층 위장"
    }

    val browserDisguiseMediaDevices: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "媒体设备伪装"
        AppLanguage.ENGLISH -> "Media Devices Spoof"
        AppLanguage.ARABIC -> "تزييف أجهزة الوسائط"
        AppLanguage.PORTUGUESE -> "Falsificação de Dispositivos de Mídia"
        AppLanguage.SPANISH -> "Falsificación de Dispositivos de Medios"
        AppLanguage.FRENCH -> "Falsification des Périphériques Multimédia"
        AppLanguage.GERMAN -> "Mediengeräte-Fälschung"
        AppLanguage.RUSSIAN -> "Подмена медиаустройств"
        AppLanguage.JAPANESE -> "メディアデバイス偽装"
        AppLanguage.KOREAN -> "미디어 장치 위조"
    }
    val browserDisguiseMediaDevicesDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "匿名化 enumerateDevices 返回结果"
        AppLanguage.ENGLISH -> "Anonymize enumerateDevices results"
        AppLanguage.ARABIC -> "إخفاء هوية نتائج الأجهزة"
        AppLanguage.PORTUGUESE -> "Anonimizar resultados de enumerateDevices"
        AppLanguage.SPANISH -> "Anonimizar resultados de enumerateDevices"
        AppLanguage.FRENCH -> "Anonymiser les résultats d'enumerateDevices"
        AppLanguage.GERMAN -> "enumerateDevices-Ergebnisse anonymisieren"
        AppLanguage.RUSSIAN -> "Анонимизация результатов enumerateDevices"
        AppLanguage.JAPANESE -> "enumerateDevices の結果を匿名化"
        AppLanguage.KOREAN -> "enumerateDevices 결과 익명화"
    }

    val browserDisguiseWebRTC: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "WebRTC IP 屏蔽"
        AppLanguage.ENGLISH -> "WebRTC IP Shield"
        AppLanguage.ARABIC -> "حجب IP عبر WebRTC"
        AppLanguage.PORTUGUESE -> "Blindagem de IP via WebRTC"
        AppLanguage.SPANISH -> "Blindaje de IP vía WebRTC"
        AppLanguage.FRENCH -> "Bouclier d'IP WebRTC"
        AppLanguage.GERMAN -> "WebRTC-IP-Schutz"
        AppLanguage.RUSSIAN -> "Экранирование IP в WebRTC"
        AppLanguage.JAPANESE -> "WebRTC IP シールド"
        AppLanguage.KOREAN -> "WebRTC IP 차단"
    }
    val browserDisguiseWebRTCDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "阻止 RTCPeerConnection 泄露本地 IP 地址"
        AppLanguage.ENGLISH -> "Block RTCPeerConnection from leaking local IP addresses"
        AppLanguage.ARABIC -> "منع تسريب عناوين IP المحلية عبر WebRTC"
        AppLanguage.PORTUGUESE -> "Bloquear RTCPeerConnection de vazar endereços IP locais"
        AppLanguage.SPANISH -> "Bloquear que RTCPeerConnection filtre direcciones IP locales"
        AppLanguage.FRENCH -> "Empêcher RTCPeerConnection de fuiter les adresses IP locales"
        AppLanguage.GERMAN -> "RTCPeerConnection am Leaken lokaler IP-Adressen hindern"
        AppLanguage.RUSSIAN -> "Блокировка утечки локальных IP-адресов через RTCPeerConnection"
        AppLanguage.JAPANESE -> "RTCPeerConnection によるローカル IP アドレスの漏洩をブロック"
        AppLanguage.KOREAN -> "RTCPeerConnection이 로컬 IP 주소를 유출하지 못하도록 차단"
    }

    val browserDisguiseFonts: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "字体枚举拦截"
        AppLanguage.ENGLISH -> "Font Enumeration Block"
        AppLanguage.ARABIC -> "حظر تعداد الخطوط"
        AppLanguage.PORTUGUESE -> "Bloqueio de Enumeração de Fontes"
        AppLanguage.SPANISH -> "Bloqueo de Enumeración de Fuentes"
        AppLanguage.FRENCH -> "Blocage d'Énumération de Polices"
        AppLanguage.GERMAN -> "Schriftarten-Enumerationsblock"
        AppLanguage.RUSSIAN -> "Блокировка перечисления шрифтов"
        AppLanguage.JAPANESE -> "フォント列挙ブロック"
        AppLanguage.KOREAN -> "글꼴 열거 차단"
    }
    val browserDisguiseFontsDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "在 measureText 中注入噪声防止字体指纹"
        AppLanguage.ENGLISH -> "Inject noise into measureText to prevent font fingerprinting"
        AppLanguage.ARABIC -> "حقن ضوضاء في قياس النص لمنع bصمات الخطوط"
        AppLanguage.PORTUGUESE -> "Injetar ruído em measureText para evitar fingerprinting de fontes"
        AppLanguage.SPANISH -> "Inyectar ruido en measureText para prevenir fingerprinting de fuentes"
        AppLanguage.FRENCH -> "Injecter du bruit dans measureText pour empêcher l'empreinte de polices"
        AppLanguage.GERMAN -> "Rauschen in measureText injizieren, um Schriftarten-Fingerprinting zu verhindern"
        AppLanguage.RUSSIAN -> "Внедрение шума в measureText для защиты от фингерпринтинга шрифтов"
        AppLanguage.JAPANESE -> "measureText にノイズを注入してフォントのフィンガープリンティングを防止"
        AppLanguage.KOREAN -> "measureText에 노이즈를 주입하여 글꼴 지문 인식 방지"
    }

    val browserDisguiseBattery: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Battery API 屏蔽"
        AppLanguage.ENGLISH -> "Battery API Shield"
        AppLanguage.ARABIC -> "حجب واجهة البطارية"
        AppLanguage.PORTUGUESE -> "Blindagem de API de Bateria"
        AppLanguage.SPANISH -> "Blindaje de API de Batería"
        AppLanguage.FRENCH -> "Bouclier d'API Batterie"
        AppLanguage.GERMAN -> "Akku-API-Schutz"
        AppLanguage.RUSSIAN -> "Экранирование Battery API"
        AppLanguage.JAPANESE -> "Battery API シールド"
        AppLanguage.KOREAN -> "Battery API 차단"
    }
}

