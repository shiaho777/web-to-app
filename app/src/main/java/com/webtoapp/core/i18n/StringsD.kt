package com.webtoapp.core.i18n

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

object StringsD {
    val browserDisguiseBatteryDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "返回固定电池信息 (100%, 充电中)"
        AppLanguage.ENGLISH -> "Return fixed battery info (100%, charging)"
        AppLanguage.ARABIC -> "إرجاع معلومات بطارية ثابتة"
        AppLanguage.PORTUGUESE -> "Retornar informações fixas de bateria (100%, carregando)"
        AppLanguage.SPANISH -> "Devolver información fija de batería (100%, cargando)"
        AppLanguage.FRENCH -> "Renvoyer des informations de batterie fixes (100%, en charge)"
        AppLanguage.GERMAN -> "Feste Akku-Info zurückgeben (100%, lädt)"
        AppLanguage.RUSSIAN -> "Возврат фиксированных данных батареи (100%, зарядка)"
        AppLanguage.JAPANESE -> "固定のバッテリー情報を返す (100%, 充電中)"
        AppLanguage.KOREAN -> "고정된 배터리 정보 반환 (100%, 충전 중)"
    }

    val browserDisguiseL5Title: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Level 5 · 原型链保护"
        AppLanguage.ENGLISH -> "Level 5 · Prototype Protection"
        AppLanguage.ARABIC -> "المستوى 5 · حماية سلسلة النماذج"
        AppLanguage.PORTUGUESE -> "Level 5 · Proteção de Protótipo"
        AppLanguage.SPANISH -> "Level 5 · Protección de Prototipo"
        AppLanguage.FRENCH -> "Level 5 · Protection de Prototype"
        AppLanguage.GERMAN -> "Level 5 · Prototyp-Schutz"
        AppLanguage.RUSSIAN -> "Level 5 · Защита прототипа"
        AppLanguage.JAPANESE -> "Level 5 · プロトタイプ保護"
        AppLanguage.KOREAN -> "Level 5 · 프로토타입 보호"
    }

    val browserDisguisePrototype: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "toString 保护"
        AppLanguage.ENGLISH -> "toString Protection"
        AppLanguage.ARABIC -> "حماية toString"
        AppLanguage.PORTUGUESE -> "Proteção de toString"
        AppLanguage.SPANISH -> "Protección de toString"
        AppLanguage.FRENCH -> "Protection de toString"
        AppLanguage.GERMAN -> "toString-Schutz"
        AppLanguage.RUSSIAN -> "Защита toString"
        AppLanguage.JAPANESE -> "toString 保護"
        AppLanguage.KOREAN -> "toString 보호"
    }
    val browserDisguisePrototypeDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "所有 hook 函数的 toString 返回 [native code]"
        AppLanguage.ENGLISH -> "All hooked functions return [native code] via toString"
        AppLanguage.ARABIC -> "جميع الوظائف المعدلة تعيد [native code]"
        AppLanguage.PORTUGUESE -> "Todas as funções hookadas retornam [native code] via toString"
        AppLanguage.SPANISH -> "Todas las funciones hookeadas devuelven [native code] vía toString"
        AppLanguage.FRENCH -> "Toutes les fonctions hookées renvoient [native code] via toString"
        AppLanguage.GERMAN -> "Alle gehookten Funktionen geben [native code] über toString zurück"
        AppLanguage.RUSSIAN -> "Все перехваченные функции возвращают [native code] через toString"
        AppLanguage.JAPANESE -> "すべてのフックされた関数は toString で [native code] を返す"
        AppLanguage.KOREAN -> "모든 훅된 함수는 toString을 통해 [native code] 반환"
    }

    val browserDisguiseIframe: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "iframe 穿透传播"
        AppLanguage.ENGLISH -> "iframe Propagation"
        AppLanguage.ARABIC -> "انتشار iframe"
        AppLanguage.PORTUGUESE -> "Propagação de iframe"
        AppLanguage.SPANISH -> "Propagación de iframe"
        AppLanguage.FRENCH -> "Propagation d'iframe"
        AppLanguage.GERMAN -> "iframe-Weitergabe"
        AppLanguage.RUSSIAN -> "Распространение iframe"
        AppLanguage.JAPANESE -> "iframe 伝播"
        AppLanguage.KOREAN -> "iframe 전파"
    }
    val browserDisguiseIframeDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自动将伪装传播到新创建的 iframe 中"
        AppLanguage.ENGLISH -> "Auto-propagate disguise into newly created iframes"
        AppLanguage.ARABIC -> "نشر التمويه تلقائياً في إطارات iframe الجديدة"
        AppLanguage.PORTUGUESE -> "Propagar automaticamente disfarce em iframes recém-criados"
        AppLanguage.SPANISH -> "Propagar automáticamente disfraz en iframes recién creados"
        AppLanguage.FRENCH -> "Propager automatiquement le déguisement dans les iframes nouvellement créés"
        AppLanguage.GERMAN -> "Tarnung automatisch in neu erstellte iframes weitergeben"
        AppLanguage.RUSSIAN -> "Автоматическое распространение маскировки во вновь созданные iframe"
        AppLanguage.JAPANESE -> "新しく作成された iframe に偽装を自動伝播"
        AppLanguage.KOREAN -> "새로 생성된 iframe에 위장 자동 전파"
    }

    val browserDisguiseTip: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Level 1 默认启用。级别越高，伪装越强，也越可能影响网页功能。"
        AppLanguage.ENGLISH -> "Level 1 is on by default. Higher levels add stronger disguise but may affect site behavior."
        AppLanguage.ARABIC -> "يعدل محرك تمويه المتصفح قيم إرجاع واجهات برمجة البصمات عبر حقن JS. المستوى 1 نشط دائماً. المستويات الأعلى تضيف تدريجياً ضوضاء Canvas/WebGL والتزييف البيئي وحماية سلسلة النماذج."
        AppLanguage.PORTUGUESE -> "Level 1 vem ativado por padrão. Níveis mais altos adicionam disfarce mais forte, mas podem afetar o comportamento do site."
        AppLanguage.SPANISH -> "Level 1 está activado por defecto. Los niveles superiores añaden un disfraz más fuerte, pero pueden afectar el comportamiento del sitio."
        AppLanguage.FRENCH -> "Level 1 est activé par défaut. Les niveaux supérieurs ajoutent un déguisement plus fort, mais peuvent affecter le comportement du site."
        AppLanguage.GERMAN -> "Level 1 ist standardmäßig aktiviert. Höhere Level fügen stärkere Tarnung hinzu, können aber das Seitenverhalten beeinflussen."
        AppLanguage.RUSSIAN -> "Level 1 включён по умолчанию. Более высокие уровни усиливают маскировку, но могут влиять на поведение сайта."
        AppLanguage.JAPANESE -> "Level 1 はデフォルトでオンです。より高いレベルはより強力な偽装を追加しますが、サイトの動作に影響する可能性があります。"
        AppLanguage.KOREAN -> "Level 1은 기본적으로 켜져 있습니다. 더 높은 레벨은 더 강력한 위장을 추가하지만 사이트 동작에 영향을 줄 수 있습니다."
    }

    val userScripts: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "User script"
        AppLanguage.ENGLISH -> "User Scripts"
        AppLanguage.ARABIC -> "سكريبتات المستخدم"
        AppLanguage.PORTUGUESE -> "Scripts de Usuário"
        AppLanguage.SPANISH -> "Scripts de Usuario"
        AppLanguage.FRENCH -> "Scripts Utilisateur"
        AppLanguage.GERMAN -> "Benutzer-Skripte"
        AppLanguage.RUSSIAN -> "Пользовательские скрипты"
        AppLanguage.JAPANESE -> "ユーザースクリプト"
        AppLanguage.KOREAN -> "사용자 스크립트"
    }

    val userScriptsDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "类似油猴脚本，注入自定义 JavaScript 代码"
        AppLanguage.ENGLISH -> "Tampermonkey-like custom JavaScript injection"
        AppLanguage.ARABIC -> "حقن كود JavaScript مخصص مثل Tampermonkey"
        AppLanguage.PORTUGUESE -> "Injeção de JavaScript personalizada estilo Tampermonkey"
        AppLanguage.SPANISH -> "Inyección de JavaScript personalizada estilo Tampermonkey"
        AppLanguage.FRENCH -> "Injection de JavaScript personnalisée façon Tampermonkey"
        AppLanguage.GERMAN -> "Tampermonkey-ähnliche benutzerdefinierte JavaScript-Injection"
        AppLanguage.RUSSIAN -> "Внедрение пользовательского JavaScript в стиле Tampermonkey"
        AppLanguage.JAPANESE -> "Tampermonkey 風のカスタム JavaScript 注入"
        AppLanguage.KOREAN -> "Tampermonkey 스타일의 사용자 정의 JavaScript 주입"
    }

    val addScript: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "添加脚本"
        AppLanguage.ENGLISH -> "Add Script"
        AppLanguage.ARABIC -> "إضافة سكريبت"
        AppLanguage.PORTUGUESE -> "Adicionar Script"
        AppLanguage.SPANISH -> "Añadir Script"
        AppLanguage.FRENCH -> "Ajouter un Script"
        AppLanguage.GERMAN -> "Skript hinzufügen"
        AppLanguage.RUSSIAN -> "Добавить скрипт"
        AppLanguage.JAPANESE -> "スクリプトを追加"
        AppLanguage.KOREAN -> "스크립트 추가"
    }

    val editScript: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "编辑脚本"
        AppLanguage.ENGLISH -> "Edit Script"
        AppLanguage.ARABIC -> "تعديل السكريبت"
        AppLanguage.PORTUGUESE -> "Editar Script"
        AppLanguage.SPANISH -> "Editar Script"
        AppLanguage.FRENCH -> "Modifier le Script"
        AppLanguage.GERMAN -> "Skript bearbeiten"
        AppLanguage.RUSSIAN -> "Изменить скрипт"
        AppLanguage.JAPANESE -> "スクリプトを編集"
        AppLanguage.KOREAN -> "스크립트 편집"
    }

    val scriptName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "脚本名称"
        AppLanguage.ENGLISH -> "Script Name"
        AppLanguage.ARABIC -> "اسم السكريبت"
        AppLanguage.PORTUGUESE -> "Nome do Script"
        AppLanguage.SPANISH -> "Nombre del Script"
        AppLanguage.FRENCH -> "Nom du Script"
        AppLanguage.GERMAN -> "Skriptname"
        AppLanguage.RUSSIAN -> "Имя скрипта"
        AppLanguage.JAPANESE -> "スクリプト名"
        AppLanguage.KOREAN -> "스크립트 이름"
    }

    val scriptNamePlaceholder: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "输入脚本名称"
        AppLanguage.ENGLISH -> "Enter script name"
        AppLanguage.ARABIC -> "أدخل اسم السكريبت"
        AppLanguage.PORTUGUESE -> "Digite o nome do script"
        AppLanguage.SPANISH -> "Introducir el nombre del script"
        AppLanguage.FRENCH -> "Saisir le nom du script"
        AppLanguage.GERMAN -> "Skriptname eingeben"
        AppLanguage.RUSSIAN -> "Введите имя скрипта"
        AppLanguage.JAPANESE -> "スクリプト名を入力"
        AppLanguage.KOREAN -> "스크립트 이름 입력"
    }

    val scriptCode: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "脚本代码"
        AppLanguage.ENGLISH -> "Script Code"
        AppLanguage.ARABIC -> "كود السكريبت"
        AppLanguage.PORTUGUESE -> "Código do Script"
        AppLanguage.SPANISH -> "Código del Script"
        AppLanguage.FRENCH -> "Code du Script"
        AppLanguage.GERMAN -> "Skriptcode"
        AppLanguage.RUSSIAN -> "Код скрипта"
        AppLanguage.JAPANESE -> "スクリプトコード"
        AppLanguage.KOREAN -> "스크립트 코드"
    }

    val scriptCodePlaceholder: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "输入 JavaScript 代码"
        AppLanguage.ENGLISH -> "Enter JavaScript code"
        AppLanguage.ARABIC -> "أدخل كود JavaScript"
        AppLanguage.PORTUGUESE -> "Digite o código JavaScript"
        AppLanguage.SPANISH -> "Introducir el código JavaScript"
        AppLanguage.FRENCH -> "Saisir le code JavaScript"
        AppLanguage.GERMAN -> "JavaScript-Code eingeben"
        AppLanguage.RUSSIAN -> "Введите код JavaScript"
        AppLanguage.JAPANESE -> "JavaScript コードを入力"
        AppLanguage.KOREAN -> "JavaScript 코드 입력"
    }

    val scriptRunAt: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Run at"
        AppLanguage.ENGLISH -> "Run At"
        AppLanguage.ARABIC -> "وقت التشغيل"
        AppLanguage.PORTUGUESE -> "Executar Em"
        AppLanguage.SPANISH -> "Ejecutar En"
        AppLanguage.FRENCH -> "Exécuter À"
        AppLanguage.GERMAN -> "Ausführen Bei"
        AppLanguage.RUSSIAN -> "Запуск При"
        AppLanguage.JAPANESE -> "実行タイミング"
        AppLanguage.KOREAN -> "실행 시점"
    }

    val scriptEnabled: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "启用脚本"
        AppLanguage.ENGLISH -> "Enable Script"
        AppLanguage.ARABIC -> "تفعيل السكريبت"
        AppLanguage.PORTUGUESE -> "Ativar Script"
        AppLanguage.SPANISH -> "Activar Script"
        AppLanguage.FRENCH -> "Activer le Script"
        AppLanguage.GERMAN -> "Skript aktivieren"
        AppLanguage.RUSSIAN -> "Включить скрипт"
        AppLanguage.JAPANESE -> "スクリプトを有効化"
        AppLanguage.KOREAN -> "스크립트 활성화"
    }

    val scriptNameRequired: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "请输入脚本名称"
        AppLanguage.ENGLISH -> "Please enter script name"
        AppLanguage.ARABIC -> "الرجاء إدخال اسم السكريبت"
        AppLanguage.PORTUGUESE -> "Por favor, digite o nome do script"
        AppLanguage.SPANISH -> "Por favor, introducir el nombre del script"
        AppLanguage.FRENCH -> "Veuillez saisir le nom du script"
        AppLanguage.GERMAN -> "Bitte Skriptname eingeben"
        AppLanguage.RUSSIAN -> "Пожалуйста, введите имя скрипта"
        AppLanguage.JAPANESE -> "スクリプト名を入力してください"
        AppLanguage.KOREAN -> "스크립트 이름을 입력해 주세요"
    }

    val scriptCodeRequired: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "请输入脚本代码"
        AppLanguage.ENGLISH -> "Please enter script code"
        AppLanguage.ARABIC -> "الرجاء إدخال كود السكريبت"
        AppLanguage.PORTUGUESE -> "Por favor, digite o código do script"
        AppLanguage.SPANISH -> "Por favor, introducir el código del script"
        AppLanguage.FRENCH -> "Veuillez saisir le code du script"
        AppLanguage.GERMAN -> "Bitte Skriptcode eingeben"
        AppLanguage.RUSSIAN -> "Пожалуйста, введите код скрипта"
        AppLanguage.JAPANESE -> "スクリプトコードを入力してください"
        AppLanguage.KOREAN -> "스크립트 코드를 입력해 주세요"
    }

    val scriptImportFile: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "导入 JS 文件"
        AppLanguage.ENGLISH -> "Import JS File"
        AppLanguage.ARABIC -> "استيراد ملف JS"
        AppLanguage.PORTUGUESE -> "Importar Arquivo JS"
        AppLanguage.SPANISH -> "Importar Archivo JS"
        AppLanguage.FRENCH -> "Importer un Fichier JS"
        AppLanguage.GERMAN -> "JS-Datei importieren"
        AppLanguage.RUSSIAN -> "Импортировать JS-файл"
        AppLanguage.JAPANESE -> "JS ファイルをインポート"
        AppLanguage.KOREAN -> "JS 파일 가져오기"
    }

    val scriptFileLoaded: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已导入文件 · %d 行 · %s"
        AppLanguage.ENGLISH -> "File imported · %d lines · %s"
        AppLanguage.ARABIC -> "تم استيراد الملف · %d سطر · %s"
        AppLanguage.PORTUGUESE -> "Arquivo importado · %d linhas · %s"
        AppLanguage.SPANISH -> "Archivo importado · %d líneas · %s"
        AppLanguage.FRENCH -> "Fichier importé · %d lignes · %s"
        AppLanguage.GERMAN -> "Datei importiert · %d Zeilen · %s"
        AppLanguage.RUSSIAN -> "Файл импортирован · %d строк · %s"
        AppLanguage.JAPANESE -> "ファイルをインポート · %d 行 · %s"
        AppLanguage.KOREAN -> "파일 가져옴 · %d 줄 · %s"
    }

    val scriptClearCode: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "清除代码"
        AppLanguage.ENGLISH -> "Clear Code"
        AppLanguage.ARABIC -> "مسح الكود"
        AppLanguage.PORTUGUESE -> "Limpar Código"
        AppLanguage.SPANISH -> "Limpiar Código"
        AppLanguage.FRENCH -> "Effacer le Code"
        AppLanguage.GERMAN -> "Code löschen"
        AppLanguage.RUSSIAN -> "Очистить код"
        AppLanguage.JAPANESE -> "コードをクリア"
        AppLanguage.KOREAN -> "코드 지우기"
    }

    val allApps: String get() = when (Strings.lang) {
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

    val uncategorized: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "未分类"
        AppLanguage.ENGLISH -> "Uncategorized"
        AppLanguage.ARABIC -> "غير مصنف"
        AppLanguage.PORTUGUESE -> "Sem categoria"
        AppLanguage.SPANISH -> "Sin categorizar"
        AppLanguage.FRENCH -> "Non classé"
        AppLanguage.GERMAN -> "Nicht kategorisiert"
        AppLanguage.RUSSIAN -> "Без категории"
        AppLanguage.JAPANESE -> "未分類"
        AppLanguage.KOREAN -> "미분류"
    }

    val addCategory: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "添加分类"
        AppLanguage.ENGLISH -> "Add Category"
        AppLanguage.ARABIC -> "إضافة تصنيف"
        AppLanguage.PORTUGUESE -> "Adicionar Categoria"
        AppLanguage.SPANISH -> "Añadir Categoría"
        AppLanguage.FRENCH -> "Ajouter une Catégorie"
        AppLanguage.GERMAN -> "Kategorie hinzufügen"
        AppLanguage.RUSSIAN -> "Добавить категорию"
        AppLanguage.JAPANESE -> "カテゴリを追加"
        AppLanguage.KOREAN -> "카테고리 추가"
    }

    val editCategory: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "编辑分类"
        AppLanguage.ENGLISH -> "Edit Category"
        AppLanguage.ARABIC -> "تعديل التصنيف"
        AppLanguage.PORTUGUESE -> "Editar Categoria"
        AppLanguage.SPANISH -> "Editar Categoría"
        AppLanguage.FRENCH -> "Modifier la Catégorie"
        AppLanguage.GERMAN -> "Kategorie bearbeiten"
        AppLanguage.RUSSIAN -> "Изменить категорию"
        AppLanguage.JAPANESE -> "カテゴリを編集"
        AppLanguage.KOREAN -> "카테고리 편집"
    }

    val deleteCategory: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "删除分类"
        AppLanguage.ENGLISH -> "Delete Category"
        AppLanguage.ARABIC -> "حذف التصنيف"
        AppLanguage.PORTUGUESE -> "Excluir Categoria"
        AppLanguage.SPANISH -> "Eliminar Categoría"
        AppLanguage.FRENCH -> "Supprimer la Catégorie"
        AppLanguage.GERMAN -> "Kategorie löschen"
        AppLanguage.RUSSIAN -> "Удалить категорию"
        AppLanguage.JAPANESE -> "カテゴリを削除"
        AppLanguage.KOREAN -> "카테고리 삭제"
    }

    val categoryName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "分类名称"
        AppLanguage.ENGLISH -> "Category Name"
        AppLanguage.ARABIC -> "اسم التصنيف"
        AppLanguage.PORTUGUESE -> "Nome da Categoria"
        AppLanguage.SPANISH -> "Nombre de Categoría"
        AppLanguage.FRENCH -> "Nom de la Catégorie"
        AppLanguage.GERMAN -> "Kategoriename"
        AppLanguage.RUSSIAN -> "Имя категории"
        AppLanguage.JAPANESE -> "カテゴリ名"
        AppLanguage.KOREAN -> "카테고리 이름"
    }

    val categoryNamePlaceholder: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "输入分类名称"
        AppLanguage.ENGLISH -> "Enter category name"
        AppLanguage.ARABIC -> "أدخل اسم التصنيف"
        AppLanguage.PORTUGUESE -> "Digite o nome da categoria"
        AppLanguage.SPANISH -> "Introducir el nombre de la categoría"
        AppLanguage.FRENCH -> "Saisir le nom de la catégorie"
        AppLanguage.GERMAN -> "Kategoriename eingeben"
        AppLanguage.RUSSIAN -> "Введите имя категории"
        AppLanguage.JAPANESE -> "カテゴリ名を入力"
        AppLanguage.KOREAN -> "카테고리 이름 입력"
    }

    val categoryIcon: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "分类图标"
        AppLanguage.ENGLISH -> "Category Icon"
        AppLanguage.ARABIC -> "أيقونة التصنيف"
        AppLanguage.PORTUGUESE -> "Ícone da Categoria"
        AppLanguage.SPANISH -> "Icono de Categoría"
        AppLanguage.FRENCH -> "Icône de la Catégorie"
        AppLanguage.GERMAN -> "Kategorie-Icon"
        AppLanguage.RUSSIAN -> "Иконка категории"
        AppLanguage.JAPANESE -> "カテゴリアイコン"
        AppLanguage.KOREAN -> "카테고리 아이콘"
    }

    val moveToCategory: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "移动到分类"
        AppLanguage.ENGLISH -> "Move to Category"
        AppLanguage.ARABIC -> "نقل إلى تصنيف"
        AppLanguage.PORTUGUESE -> "Mover para Categoria"
        AppLanguage.SPANISH -> "Mover a Categoría"
        AppLanguage.FRENCH -> "Déplacer vers la Catégorie"
        AppLanguage.GERMAN -> "In Kategorie verschieben"
        AppLanguage.RUSSIAN -> "Переместить в категорию"
        AppLanguage.JAPANESE -> "カテゴリに移動"
        AppLanguage.KOREAN -> "카테고리로 이동"
    }

    val clearAppCacheMenu: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "清理缓存"
        AppLanguage.ENGLISH -> "Clear Cache"
        AppLanguage.ARABIC -> "مسح ذاكرة التخزين المؤقت"
        AppLanguage.PORTUGUESE -> "Limpar Cache"
        AppLanguage.SPANISH -> "Borrar Caché"
        AppLanguage.FRENCH -> "Vider le Cache"
        AppLanguage.GERMAN -> "Cache leeren"
        AppLanguage.RUSSIAN -> "Очистить кэш"
        AppLanguage.JAPANESE -> "キャッシュを消去"
        AppLanguage.KOREAN -> "캐시 지우기"
    }

    val clearAppCacheTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "清理应用缓存"
        AppLanguage.ENGLISH -> "Clear App Cache"
        AppLanguage.ARABIC -> "مسح ذاكرة التخزين المؤقت للتطبيق"
        AppLanguage.PORTUGUESE -> "Limpar Cache do Aplicativo"
        AppLanguage.SPANISH -> "Borrar Caché de la Aplicación"
        AppLanguage.FRENCH -> "Vider le Cache de l'Application"
        AppLanguage.GERMAN -> "App-Cache leeren"
        AppLanguage.RUSSIAN -> "Очистить кэш приложения"
        AppLanguage.JAPANESE -> "アプリのキャッシュを消去"
        AppLanguage.KOREAN -> "앱 캐시 지우기"
    }

    val clearAppCacheConfirm: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "将删除该应用的 APK 增量构建缓存和站点本地数据（保留 Cookie 与应用配置）。被清理的构建缓存会在下次构建时重新生成。"
        AppLanguage.ENGLISH -> "Deletes this app's incremental APK build cache and site local storage. Cookies and app settings are kept; the build cache is regenerated on the next build."
        AppLanguage.ARABIC -> "سيتم حذف ذاكرة التخزين المؤقت لبناء APK والبيانات المحلية للموقع لهذا التطبيق. تُحفظ ملفات تعريف الارتباط وإعدادات التطبيق، ويُعاد توليد ذاكرة البناء في البناء التالي."
        AppLanguage.PORTUGUESE -> "Exclui o cache incremental de build do APK e os dados locais do site deste aplicativo. Cookies e configurações são mantidos; o cache de build é recriado no próximo build."
        AppLanguage.SPANISH -> "Elimina la caché de compilación incremental del APK y los datos locales del sitio de esta aplicación. Se conservan las cookies y la configuración; la caché se regenera en la próxima compilación."
        AppLanguage.FRENCH -> "Supprime le cache de build APK incrémental et les données locales du site de cette application. Les cookies et les réglages sont conservés ; le cache est régénéré au prochain build."
        AppLanguage.GERMAN -> "Löscht den inkrementellen APK-Build-Cache und die lokalen Websitedaten dieser App. Cookies und Einstellungen bleiben erhalten; der Build-Cache wird beim nächsten Build neu erzeugt."
        AppLanguage.RUSSIAN -> "Удалит инкрементальный кэш сборки APK и локальные данные сайта этого приложения. Cookie и настройки сохраняются; кэш сборки создастся заново при следующей сборке."
        AppLanguage.JAPANESE -> "このアプリのAPK増分ビルドキャッシュとサイトのローカルデータを削除します。Cookieとアプリ設定は保持され、ビルドキャッシュは次回ビルド時に再生成されます。"
        AppLanguage.KOREAN -> "이 앱의 APK 증분 빌드 캐시와 사이트 로컬 데이터를 삭제합니다. 쿠키와 앱 설정은 유지되며, 빌드 캐시는 다음 빌드 때 다시 생성됩니다."
    }

    val clearAppCacheDone: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已清理，释放 %s"
        AppLanguage.ENGLISH -> "Cleared — freed %s"
        AppLanguage.ARABIC -> "تم المسح — تم تحرير %s"
        AppLanguage.PORTUGUESE -> "Limpo — liberado %s"
        AppLanguage.SPANISH -> "Borrado — liberado %s"
        AppLanguage.FRENCH -> "Vidé — %s libérés"
        AppLanguage.GERMAN -> "Geleert — %s freigegeben"
        AppLanguage.RUSSIAN -> "Очищено — освобождено %s"
        AppLanguage.JAPANESE -> "消去しました — %s 解放"
        AppLanguage.KOREAN -> "지웠습니다 — %s 확보"
    }

    val deleteCategoryConfirm: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "确定删除此分类吗？该分类下的应用将变为未分类。"
        AppLanguage.ENGLISH -> "Delete this category? Apps in this category will become uncategorized."
        AppLanguage.ARABIC -> "حذف هذا التصنيف؟ ستصبح التطبيقات في هذا التصنيف غير مصنفة."
        AppLanguage.PORTUGUESE -> "Excluir esta categoria? Os apps nesta categoria ficarão sem categoria."
        AppLanguage.SPANISH -> "¿Eliminar esta categoría? Las apps en esta categoría quedarán sin categorizar."
        AppLanguage.FRENCH -> "Supprimer cette catégorie ? Les applications de cette catégorie deviendront non classées."
        AppLanguage.GERMAN -> "Diese Kategorie löschen? Apps in dieser Kategorie werden nicht kategorisiert."
        AppLanguage.RUSSIAN -> "Удалить эту категорию? Приложения в ней станут без категории."
        AppLanguage.JAPANESE -> "このカテゴリを削除しますか？このカテゴリのアプリは未分類になります。"
        AppLanguage.KOREAN -> "이 카테고리를 삭제하시겠습니까? 이 카테고리의 앱은 미분류로 변경됩니다."
    }

    val manageCategories: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "管理分类"
        AppLanguage.ENGLISH -> "Manage Categories"
        AppLanguage.ARABIC -> "إدارة التصنيفات"
        AppLanguage.PORTUGUESE -> "Gerenciar Categorias"
        AppLanguage.SPANISH -> "Gestionar Categorías"
        AppLanguage.FRENCH -> "Gérer les Catégories"
        AppLanguage.GERMAN -> "Kategorien verwalten"
        AppLanguage.RUSSIAN -> "Управление категориями"
        AppLanguage.JAPANESE -> "カテゴリを管理"
        AppLanguage.KOREAN -> "카테고리 관리"
    }

    val categoriesEmptyHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "还没有分类，点「添加分类」创建一个"
        AppLanguage.ENGLISH -> "No categories yet — tap \"Add Category\" to create one"
        AppLanguage.ARABIC -> "لا توجد تصنيفات بعد — اضغط \"إضافة تصنيف\" لإنشاء واحد"
        AppLanguage.PORTUGUESE -> "Ainda não há categorias — toque em \"Adicionar Categoria\" para criar uma"
        AppLanguage.SPANISH -> "Aún no hay categorías — toca \"Añadir Categoría\" para crear una"
        AppLanguage.FRENCH -> "Aucune catégorie pour l'instant — touchez « Ajouter une Catégorie » pour en créer une"
        AppLanguage.GERMAN -> "Noch keine Kategorien — tippe auf „Kategorie hinzufügen“, um eine zu erstellen"
        AppLanguage.RUSSIAN -> "Категорий пока нет — нажмите «Добавить категорию», чтобы создать"
        AppLanguage.JAPANESE -> "カテゴリはまだありません —「カテゴリを追加」で作成できます"
        AppLanguage.KOREAN -> "아직 카테고리가 없습니다 — \"카테고리 추가\"를 눌러 만드세요"
    }

    val iconGroupCommon: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "常用"
        AppLanguage.ENGLISH -> "Common"
        AppLanguage.ARABIC -> "شائعة"
        AppLanguage.PORTUGUESE -> "Comuns"
        AppLanguage.SPANISH -> "Comunes"
        AppLanguage.FRENCH -> "Courantes"
        AppLanguage.GERMAN -> "Häufig"
        AppLanguage.RUSSIAN -> "Частые"
        AppLanguage.JAPANESE -> "よく使う"
        AppLanguage.KOREAN -> "자주 사용"
    }

    val iconGroupMedia: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "媒体"
        AppLanguage.ENGLISH -> "Media"
        AppLanguage.ARABIC -> "وسائط"
        AppLanguage.PORTUGUESE -> "Mídia"
        AppLanguage.SPANISH -> "Medios"
        AppLanguage.FRENCH -> "Médias"
        AppLanguage.GERMAN -> "Medien"
        AppLanguage.RUSSIAN -> "Медиа"
        AppLanguage.JAPANESE -> "メディア"
        AppLanguage.KOREAN -> "미디어"
    }

    val iconGroupWorkStudy: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "工作学习"
        AppLanguage.ENGLISH -> "Work & Study"
        AppLanguage.ARABIC -> "عمل ودراسة"
        AppLanguage.PORTUGUESE -> "Trabalho e Estudo"
        AppLanguage.SPANISH -> "Trabajo y Estudio"
        AppLanguage.FRENCH -> "Travail et Études"
        AppLanguage.GERMAN -> "Arbeit & Studium"
        AppLanguage.RUSSIAN -> "Работа и учёба"
        AppLanguage.JAPANESE -> "仕事と学び"
        AppLanguage.KOREAN -> "작업·학습"
    }

    val iconGroupLifeTravel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "生活出行"
        AppLanguage.ENGLISH -> "Life & Travel"
        AppLanguage.ARABIC -> "حياة وسفر"
        AppLanguage.PORTUGUESE -> "Vida e Viagem"
        AppLanguage.SPANISH -> "Vida y Viajes"
        AppLanguage.FRENCH -> "Vie et Voyages"
        AppLanguage.GERMAN -> "Leben & Reisen"
        AppLanguage.RUSSIAN -> "Жизнь и поездки"
        AppLanguage.JAPANESE -> "生活と旅行"
        AppLanguage.KOREAN -> "생활·여행"
    }

    val iconGroupTools: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "工具"
        AppLanguage.ENGLISH -> "Tools"
        AppLanguage.ARABIC -> "أدوات"
        AppLanguage.PORTUGUESE -> "Ferramentas"
        AppLanguage.SPANISH -> "Herramientas"
        AppLanguage.FRENCH -> "Outils"
        AppLanguage.GERMAN -> "Werkzeuge"
        AppLanguage.RUSSIAN -> "Инструменты"
        AppLanguage.JAPANESE -> "ツール"
        AppLanguage.KOREAN -> "도구"
    }

    val iconGroupFun: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "趣味"
        AppLanguage.ENGLISH -> "Fun"
        AppLanguage.ARABIC -> "ترفيه"
        AppLanguage.PORTUGUESE -> "Diversão"
        AppLanguage.SPANISH -> "Diversión"
        AppLanguage.FRENCH -> "Loisirs"
        AppLanguage.GERMAN -> "Spaß"
        AppLanguage.RUSSIAN -> "Развлечения"
        AppLanguage.JAPANESE -> "趣味"
        AppLanguage.KOREAN -> "재미"
    }

    val randomNameTooltip: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "点击生成随机应用名称"
        AppLanguage.ENGLISH -> "Click to generate a random app name"
        AppLanguage.ARABIC -> "انقر لإنشاء اسم تطبيق عشوائي"
        AppLanguage.PORTUGUESE -> "Clique para gerar um nome aleatório de app"
        AppLanguage.SPANISH -> "Haz clic para generar un nombre aleatorio de app"
        AppLanguage.FRENCH -> "Cliquez pour générer un nom d'application aléatoire"
        AppLanguage.GERMAN -> "Klicken, um einen zufälligen App-Namen zu generieren"
        AppLanguage.RUSSIAN -> "Нажмите для генерации случайного имени приложения"
        AppLanguage.JAPANESE -> "クリックしてランダムなアプリ名を生成"
        AppLanguage.KOREAN -> "클릭하여 임의의 앱 이름 생성"
    }

    val sampleVueCounterName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Vue 计数器"
        AppLanguage.ENGLISH -> "Vue Counter"
        AppLanguage.ARABIC -> "عداد Vue"
        AppLanguage.PORTUGUESE -> "Contador Vue"
        AppLanguage.SPANISH -> "Contador Vue"
        AppLanguage.FRENCH -> "Compteur Vue"
        AppLanguage.GERMAN -> "Vue-Zähler"
        AppLanguage.RUSSIAN -> "Счётчик Vue"
        AppLanguage.JAPANESE -> "Vue カウンター"
        AppLanguage.KOREAN -> "Vue 카운터"
    }

    val sampleVueCounterDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Vue 3 响应式计数器示例，展示 Composition API"
        AppLanguage.ENGLISH -> "Vue 3 reactive counter demo, showcasing Composition API"
        AppLanguage.ARABIC -> "عرض عداد Vue 3 التفاعلي، يعرض Composition API"
        AppLanguage.PORTUGUESE -> "Demo de contador reativo Vue 3, mostrando Composition API"
        AppLanguage.SPANISH -> "Demo de contador reactivo Vue 3, mostrando Composition API"
        AppLanguage.FRENCH -> "Démo de compteur réactif Vue 3, présentant Composition API"
        AppLanguage.GERMAN -> "Vue 3 reaktiver Zähler-Demo, zeigt Composition API"
        AppLanguage.RUSSIAN -> "Демо реактивного счётчика Vue 3, демонстрирует Composition API"
        AppLanguage.JAPANESE -> "Vue 3 リアクティブカウンターデモ、Composition API を紹介"
        AppLanguage.KOREAN -> "Vue 3 반응형 카운터 데모, Composition API 선보임"
    }

    val sampleVueCounterTagReactive: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "响应式"
        AppLanguage.ENGLISH -> "Reactive"
        AppLanguage.ARABIC -> "تفاعلي"
        AppLanguage.PORTUGUESE -> "Reativo"
        AppLanguage.SPANISH -> "Reactivo"
        AppLanguage.FRENCH -> "Réactif"
        AppLanguage.GERMAN -> "Reaktiv"
        AppLanguage.RUSSIAN -> "Реактивный"
        AppLanguage.JAPANESE -> "リアクティブ"
        AppLanguage.KOREAN -> "반응형"
    }

    val sampleReactTodoName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "React 待办"
        AppLanguage.ENGLISH -> "React Todo"
        AppLanguage.ARABIC -> "React Todo"
        AppLanguage.PORTUGUESE -> "React Tarefas"
        AppLanguage.SPANISH -> "React Tareas"
        AppLanguage.FRENCH -> "React Tâches"
        AppLanguage.GERMAN -> "React Aufgaben"
        AppLanguage.RUSSIAN -> "React Задачи"
        AppLanguage.JAPANESE -> "React タスク"
        AppLanguage.KOREAN -> "React 할 일"
    }

    val sampleReactTodoDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "React 18 待办事项应用，展示 Hooks 用法"
        AppLanguage.ENGLISH -> "React 18 todo app, showcasing Hooks usage"
        AppLanguage.ARABIC -> "تطبيق مهام React 18، يعرض استخدام Hooks"
        AppLanguage.PORTUGUESE -> "App de tarefas React 18, demonstrando uso de Hooks"
        AppLanguage.SPANISH -> "App de tareas React 18, demostrando el uso de Hooks"
        AppLanguage.FRENCH -> "App de tâches React 18, démontrant l'utilisation de Hooks"
        AppLanguage.GERMAN -> "React 18 Aufgaben-App, demonstriert Hooks-Verwendung"
        AppLanguage.RUSSIAN -> "Приложение задач React 18, демонстрирует использование Hooks"
        AppLanguage.JAPANESE -> "React 18 タスクアプリ、Hooks の使用法を紹介"
        AppLanguage.KOREAN -> "React 18 할 일 앱, Hooks 사용법 시연"
    }

    val sampleWeatherAppName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "天气应用"
        AppLanguage.ENGLISH -> "Weather App"
        AppLanguage.ARABIC -> "تطبيق الطقس"
        AppLanguage.PORTUGUESE -> "App de Clima"
        AppLanguage.SPANISH -> "App de Clima"
        AppLanguage.FRENCH -> "App Météo"
        AppLanguage.GERMAN -> "Wetter-App"
        AppLanguage.RUSSIAN -> "Приложение Погоды"
        AppLanguage.JAPANESE -> "天気アプリ"
        AppLanguage.KOREAN -> "날씨 앱"
    }

    val sampleWeatherAppDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Vite + 原生 JS 天气查询应用，无框架依赖"
        AppLanguage.ENGLISH -> "Vite + Vanilla JS weather app, no framework dependency"
        AppLanguage.ARABIC -> "تطبيق طقس Vite + JS أصلي، بدون إطار عمل"
        AppLanguage.PORTUGUESE -> "App de clima Vite + Vanilla JS, sem dependência de framework"
        AppLanguage.SPANISH -> "App de clima Vite + Vanilla JS, sin dependencia de framework"
        AppLanguage.FRENCH -> "App météo Vite + Vanilla JS, sans dépendance de framework"
        AppLanguage.GERMAN -> "Wetter-App mit Vite + Vanilla JS, ohne Framework-Abhängigkeit"
        AppLanguage.RUSSIAN -> "Приложение погоды Vite + Vanilla JS, без зависимости от фреймворка"
        AppLanguage.JAPANESE -> "Vite + Vanilla JS 天気アプリ、フレームワーク非依存"
        AppLanguage.KOREAN -> "Vite + Vanilla JS 날씨 앱, 프레임워크 의존성 없음"
    }

    val fetchWebsiteIcon: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "获取图标"
        AppLanguage.ENGLISH -> "Fetch Icon"
        AppLanguage.ARABIC -> "جلب الأيقونة"
        AppLanguage.PORTUGUESE -> "Obter Ícone"
        AppLanguage.SPANISH -> "Obtener Ícono"
        AppLanguage.FRENCH -> "Récupérer l'Icône"
        AppLanguage.GERMAN -> "Icon abrufen"
        AppLanguage.RUSSIAN -> "Получить Значок"
        AppLanguage.JAPANESE -> "アイコンを取得"
        AppLanguage.KOREAN -> "아이콘 가져오기"
    }

    val faviconFetchSuccess: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已获取网站图标"
        AppLanguage.ENGLISH -> "Website icon fetched"
        AppLanguage.ARABIC -> "تم جلب أيقونة الموقع"
        AppLanguage.PORTUGUESE -> "Ícone do site obtido"
        AppLanguage.SPANISH -> "Ícono del sitio obtenido"
        AppLanguage.FRENCH -> "Icône du site récupérée"
        AppLanguage.GERMAN -> "Website-Icon abgerufen"
        AppLanguage.RUSSIAN -> "Значок сайта получен"
        AppLanguage.JAPANESE -> "サイトアイコンを取得しました"
        AppLanguage.KOREAN -> "사이트 아이콘을 가져왔습니다"
    }

    val faviconFetchFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "无法获取网站图标"
        AppLanguage.ENGLISH -> "Failed to fetch website icon"
        AppLanguage.ARABIC -> "فشل في جلب أيقونة الموقع"
        AppLanguage.PORTUGUESE -> "Falha ao obter ícone do site"
        AppLanguage.SPANISH -> "Error al obtener el ícono del sitio"
        AppLanguage.FRENCH -> "Échec de la récupération de l'icône du site"
        AppLanguage.GERMAN -> "Abruf des Website-Icons fehlgeschlagen"
        AppLanguage.RUSSIAN -> "Не удалось получить значок сайта"
        AppLanguage.JAPANESE -> "サイトアイコンの取得に失敗しました"
        AppLanguage.KOREAN -> "사이트 아이콘 가져오기 실패"
    }

    val longPressMenuImage: String get() = when (Strings.lang) {
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

    val longPressMenuVideo: String get() = when (Strings.lang) {
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

    val longPressMenuLink: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "链接"
        AppLanguage.ENGLISH -> "Link"
        AppLanguage.ARABIC -> "رابط"
        AppLanguage.PORTUGUESE -> "Link"
        AppLanguage.SPANISH -> "Enlace"
        AppLanguage.FRENCH -> "Lien"
        AppLanguage.GERMAN -> "Link"
        AppLanguage.RUSSIAN -> "Ссылка"
        AppLanguage.JAPANESE -> "リンク"
        AppLanguage.KOREAN -> "링크"
    }

    val longPressMenuSaveImage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "保存图片"
        AppLanguage.ENGLISH -> "Save Image"
        AppLanguage.ARABIC -> "حفظ الصورة"
        AppLanguage.PORTUGUESE -> "Salvar Imagem"
        AppLanguage.SPANISH -> "Guardar Imagen"
        AppLanguage.FRENCH -> "Enregistrer l'Image"
        AppLanguage.GERMAN -> "Bild speichern"
        AppLanguage.RUSSIAN -> "Сохранить Изображение"
        AppLanguage.JAPANESE -> "画像を保存"
        AppLanguage.KOREAN -> "이미지 저장"
    }

    val longPressMenuCopyImageLink: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "复制图片链接"
        AppLanguage.ENGLISH -> "Copy Image Link"
        AppLanguage.ARABIC -> "نسخ رابط الصورة"
        AppLanguage.PORTUGUESE -> "Copiar Link da Imagem"
        AppLanguage.SPANISH -> "Copiar Enlace de Imagen"
        AppLanguage.FRENCH -> "Copier le Lien de l'Image"
        AppLanguage.GERMAN -> "Bild-Link kopieren"
        AppLanguage.RUSSIAN -> "Копировать Ссылку на Изображение"
        AppLanguage.JAPANESE -> "画像リンクをコピー"
        AppLanguage.KOREAN -> "이미지 링크 복사"
    }

    val longPressMenuDownloadVideo: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下载视频"
        AppLanguage.ENGLISH -> "Download Video"
        AppLanguage.ARABIC -> "تنزيل الفيديو"
        AppLanguage.PORTUGUESE -> "Baixar Vídeo"
        AppLanguage.SPANISH -> "Descargar Video"
        AppLanguage.FRENCH -> "Télécharger la Vidéo"
        AppLanguage.GERMAN -> "Video herunterladen"
        AppLanguage.RUSSIAN -> "Скачать Видео"
        AppLanguage.JAPANESE -> "動画をダウンロード"
        AppLanguage.KOREAN -> "동영상 다운로드"
    }

    val longPressMenuCopyVideoLink: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "复制视频链接"
        AppLanguage.ENGLISH -> "Copy Video Link"
        AppLanguage.ARABIC -> "نسخ رابط الفيديو"
        AppLanguage.PORTUGUESE -> "Copiar Link do Vídeo"
        AppLanguage.SPANISH -> "Copiar Enlace de Video"
        AppLanguage.FRENCH -> "Copier le Lien de la Vidéo"
        AppLanguage.GERMAN -> "Video-Link kopieren"
        AppLanguage.RUSSIAN -> "Копировать Ссылку на Видео"
        AppLanguage.JAPANESE -> "動画リンクをコピー"
        AppLanguage.KOREAN -> "동영상 링크 복사"
    }

    val longPressMenuCopyLink: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "复制链接"
        AppLanguage.ENGLISH -> "Copy Link"
        AppLanguage.ARABIC -> "نسخ الرابط"
        AppLanguage.PORTUGUESE -> "Copiar Link"
        AppLanguage.SPANISH -> "Copiar Enlace"
        AppLanguage.FRENCH -> "Copier le Lien"
        AppLanguage.GERMAN -> "Link kopieren"
        AppLanguage.RUSSIAN -> "Копировать Ссылку"
        AppLanguage.JAPANESE -> "リンクをコピー"
        AppLanguage.KOREAN -> "링크 복사"
    }

    val longPressMenuCopyLinkAddress: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "复制链接地址"
        AppLanguage.ENGLISH -> "Copy Link Address"
        AppLanguage.ARABIC -> "نسخ عنوان الرابط"
        AppLanguage.PORTUGUESE -> "Copiar Endereço do Link"
        AppLanguage.SPANISH -> "Copiar Dirección del Enlace"
        AppLanguage.FRENCH -> "Copier l'Adresse du Lien"
        AppLanguage.GERMAN -> "Link-Adresse kopieren"
        AppLanguage.RUSSIAN -> "Копировать Адрес Ссылки"
        AppLanguage.JAPANESE -> "リンクアドレスをコピー"
        AppLanguage.KOREAN -> "링크 주소 복사"
    }

    val longPressMenuOpenInBrowser: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "在浏览器中打开"
        AppLanguage.ENGLISH -> "Open in Browser"
        AppLanguage.ARABIC -> "فتح في المتصفح"
        AppLanguage.PORTUGUESE -> "Abrir no Navegador"
        AppLanguage.SPANISH -> "Abrir en Navegador"
        AppLanguage.FRENCH -> "Ouvrir dans le Navigateur"
        AppLanguage.GERMAN -> "Im Browser öffnen"
        AppLanguage.RUSSIAN -> "Открыть в Браузере"
        AppLanguage.JAPANESE -> "ブラウザで開く"
        AppLanguage.KOREAN -> "브라우저에서 열기"
    }

    val longPressMenuImagePreview: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "图片预览"
        AppLanguage.ENGLISH -> "Image Preview"
        AppLanguage.ARABIC -> "معاينة الصورة"
        AppLanguage.PORTUGUESE -> "Pré-visualizar Imagem"
        AppLanguage.SPANISH -> "Vista Previa de Imagen"
        AppLanguage.FRENCH -> "Aperçu de l'Image"
        AppLanguage.GERMAN -> "Bildvorschau"
        AppLanguage.RUSSIAN -> "Предпросмотр Изображения"
        AppLanguage.JAPANESE -> "画像プレビュー"
        AppLanguage.KOREAN -> "이미지 미리보기"
    }

    val longPressMenuSettings: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "长按菜单"
        AppLanguage.ENGLISH -> "Long Press Menu"
        AppLanguage.ARABIC -> "قائمة الضغط المطول"
        AppLanguage.PORTUGUESE -> "Menu de Pressão Longa"
        AppLanguage.SPANISH -> "Menú de Pulsación Larga"
        AppLanguage.FRENCH -> "Menu de Pression Longue"
        AppLanguage.GERMAN -> "Langes Drücken Menü"
        AppLanguage.RUSSIAN -> "Меню Долгого Нажатия"
        AppLanguage.JAPANESE -> "長押しメニュー"
        AppLanguage.KOREAN -> "길게 누름 메뉴"
    }

    val longPressMenuStyleSimple: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "简洁"
        AppLanguage.ENGLISH -> "Simple"
        AppLanguage.ARABIC -> "بسيط"
        AppLanguage.PORTUGUESE -> "Simples"
        AppLanguage.SPANISH -> "Simple"
        AppLanguage.FRENCH -> "Basique"
        AppLanguage.GERMAN -> "Einfach"
        AppLanguage.RUSSIAN -> "Простой"
        AppLanguage.JAPANESE -> "シンプル"
        AppLanguage.KOREAN -> "간단"
    }

    val longPressMenuStyleSimpleDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "仅显示保存图片和复制链接"
        AppLanguage.ENGLISH -> "Only save image and copy link"
        AppLanguage.ARABIC -> "حفظ الصورة ونسخ الرابط فقط"
        AppLanguage.PORTUGUESE -> "Apenas salvar imagem e copiar link"
        AppLanguage.SPANISH -> "Solo guardar imagen y copiar enlace"
        AppLanguage.FRENCH -> "Enregistrer l'image et copier le lien uniquement"
        AppLanguage.GERMAN -> "Nur Bild speichern und Link kopieren"
        AppLanguage.RUSSIAN -> "Только сохранить изображение и копировать ссылку"
        AppLanguage.JAPANESE -> "画像の保存とリンクのコピーのみ"
        AppLanguage.KOREAN -> "이미지 저장 및 링크 복사만"
    }

    val longPressMenuStyleFull: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "完整"
        AppLanguage.ENGLISH -> "Full"
        AppLanguage.ARABIC -> "كامل"
        AppLanguage.PORTUGUESE -> "Completo"
        AppLanguage.SPANISH -> "Completo"
        AppLanguage.FRENCH -> "Complet"
        AppLanguage.GERMAN -> "Vollständig"
        AppLanguage.RUSSIAN -> "Полный"
        AppLanguage.JAPANESE -> "フル"
        AppLanguage.KOREAN -> "전체"
    }

    val longPressMenuStyleFullDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "显示所有可用操作"
        AppLanguage.ENGLISH -> "Show all available actions"
        AppLanguage.ARABIC -> "عرض جميع الإجراءات المتاحة"
        AppLanguage.PORTUGUESE -> "Mostrar todas as ações disponíveis"
        AppLanguage.SPANISH -> "Mostrar todas las acciones disponibles"
        AppLanguage.FRENCH -> "Afficher toutes les actions disponibles"
        AppLanguage.GERMAN -> "Alle verfügbaren Aktionen anzeigen"
        AppLanguage.RUSSIAN -> "Показать все доступные действия"
        AppLanguage.JAPANESE -> "利用可能なすべてのアクションを表示"
        AppLanguage.KOREAN -> "사용 가능한 모든 작업 표시"
    }

    val longPressMenuStyleIos: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "iOS 风格"
        AppLanguage.ENGLISH -> "iOS Style"
        AppLanguage.ARABIC -> "نمط iOS"
        AppLanguage.PORTUGUESE -> "Estilo iOS"
        AppLanguage.SPANISH -> "Estilo iOS"
        AppLanguage.FRENCH -> "Style iOS"
        AppLanguage.GERMAN -> "iOS-Stil"
        AppLanguage.RUSSIAN -> "Стиль iOS"
        AppLanguage.JAPANESE -> "iOS スタイル"
        AppLanguage.KOREAN -> "iOS 스타일"
    }

    val longPressMenuStyleIosDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "毛玻璃背景，类似 iPhone 体验"
        AppLanguage.ENGLISH -> "Frosted glass background, iPhone-like"
        AppLanguage.ARABIC -> "خلفية زجاجية مصقولة، مثل iPhone"
        AppLanguage.PORTUGUESE -> "Fundo de vidro fosco, estilo iPhone"
        AppLanguage.SPANISH -> "Fondo de vidrio esmerilado, estilo iPhone"
        AppLanguage.FRENCH -> "Fond en verre dépoli, style iPhone"
        AppLanguage.GERMAN -> "Milchglashintergrund, iPhone-ähnlich"
        AppLanguage.RUSSIAN -> "Матовый стеклянный фон, в стиле iPhone"
        AppLanguage.JAPANESE -> "すりガラス背景、iPhone風"
        AppLanguage.KOREAN -> "프로스티드 글라스 배경, iPhone 스타일"
    }

    val longPressMenuStyleFloating: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "悬浮气泡"
        AppLanguage.ENGLISH -> "Floating Bubble"
        AppLanguage.ARABIC -> "فقاعة عائمة"
        AppLanguage.PORTUGUESE -> "Bolha Flutuante"
        AppLanguage.SPANISH -> "Burbuja Flotante"
        AppLanguage.FRENCH -> "Bulle Flottante"
        AppLanguage.GERMAN -> "Schwebende Blase"
        AppLanguage.RUSSIAN -> "Плавающий Пузырь"
        AppLanguage.JAPANESE -> "フローティングバブル"
        AppLanguage.KOREAN -> "플로팅 버블"
    }

    val longPressMenuStyleFloatingDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "在点击位置显示圆形气泡菜单"
        AppLanguage.ENGLISH -> "Circular bubble menu at tap position"
        AppLanguage.ARABIC -> "قائمة فقاعات دائرية في موضع النقر"
        AppLanguage.PORTUGUESE -> "Menu de bolha circular na posição do toque"
        AppLanguage.SPANISH -> "Menú de burbuja circular en la posición del toque"
        AppLanguage.FRENCH -> "Menu circulaire en bulle à la position du toucher"
        AppLanguage.GERMAN -> "Kreisförmiges Blasenmenü an der Tapposition"
        AppLanguage.RUSSIAN -> "Круговое пузырьковое меню в месте касания"
        AppLanguage.JAPANESE -> "タップ位置に円形バブルメニューを表示"
        AppLanguage.KOREAN -> "탭 위치에 원형 버블 메뉴"
    }

    val longPressMenuStyleContext: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "右键菜单"
        AppLanguage.ENGLISH -> "Context Menu"
        AppLanguage.ARABIC -> "قائمة السياق"
        AppLanguage.PORTUGUESE -> "Menu de Contexto"
        AppLanguage.SPANISH -> "Menú Contextual"
        AppLanguage.FRENCH -> "Menu Contextuel"
        AppLanguage.GERMAN -> "Kontextmenü"
        AppLanguage.RUSSIAN -> "Контекстное Меню"
        AppLanguage.JAPANESE -> "コンテキストメニュー"
        AppLanguage.KOREAN -> "컨텍스트 메뉴"
    }

    val longPressMenuStyleContextDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "类似桌面端右键菜单，紧凑高效"
        AppLanguage.ENGLISH -> "Desktop-like right-click menu, compact"
        AppLanguage.ARABIC -> "قائمة نقر يمين مثل سطح المكتب، مضغوطة"
        AppLanguage.PORTUGUESE -> "Menu de clique direito estilo desktop, compacto"
        AppLanguage.SPANISH -> "Menú de clic derecho estilo escritorio, compacto"
        AppLanguage.FRENCH -> "Menu clic droit style bureau, compact"
        AppLanguage.GERMAN -> "Desktop-ähnliches Rechtsklickmenü, kompakt"
        AppLanguage.RUSSIAN -> "Контекстное меню как на рабочем столе, компактное"
        AppLanguage.JAPANESE -> "デスクトップ風の右クリックメニュー、コンパクト"
        AppLanguage.KOREAN -> "데스크톱 스타일의 오른쪽 클릭 메뉴, 간결"
    }

    val menuBrowserKernel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "浏览器内核"
        AppLanguage.ENGLISH -> "Browser Kernel"
        AppLanguage.ARABIC -> "نواة المتصفح"
        AppLanguage.PORTUGUESE -> "Núcleo do Navegador"
        AppLanguage.SPANISH -> "Núcleo del Navegador"
        AppLanguage.FRENCH -> "Noyau du Navigateur"
        AppLanguage.GERMAN -> "Browser-Kernel"
        AppLanguage.RUSSIAN -> "Ядро Браузера"
        AppLanguage.JAPANESE -> "ブラウザカーネル"
        AppLanguage.KOREAN -> "브라우저 커널"
    }

    val browserKernelTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "浏览器内核设置"
        AppLanguage.ENGLISH -> "Browser Kernel Settings"
        AppLanguage.ARABIC -> "إعدادات نواة المتصفح"
        AppLanguage.PORTUGUESE -> "Configurações do Núcleo do Navegador"
        AppLanguage.SPANISH -> "Configuración del Núcleo del Navegador"
        AppLanguage.FRENCH -> "Paramètres du Noyau du Navigateur"
        AppLanguage.GERMAN -> "Browser-Kernel-Einstellungen"
        AppLanguage.RUSSIAN -> "Настройки Ядра Браузера"
        AppLanguage.JAPANESE -> "ブラウザカーネル設定"
        AppLanguage.KOREAN -> "브라우저 커널 설정"
    }

    val currentWebViewInfo: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "当前 WebView 信息"
        AppLanguage.ENGLISH -> "Current WebView Info"
        AppLanguage.ARABIC -> "معلومات WebView الحالي"
        AppLanguage.PORTUGUESE -> "Informações do WebView atual"
        AppLanguage.SPANISH -> "Información del WebView actual"
        AppLanguage.FRENCH -> "Infos du WebView actuel"
        AppLanguage.GERMAN -> "Aktuelle WebView-Info"
        AppLanguage.RUSSIAN -> "Информация о текущем WebView"
        AppLanguage.JAPANESE -> "現在の WebView 情報"
        AppLanguage.KOREAN -> "현재 WebView 정보"
    }

    val webViewProvider: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "WebView 提供者"
        AppLanguage.ENGLISH -> "WebView Provider"
        AppLanguage.ARABIC -> "مزود WebView"
        AppLanguage.PORTUGUESE -> "Provedor WebView"
        AppLanguage.SPANISH -> "Proveedor WebView"
        AppLanguage.FRENCH -> "Fournisseur WebView"
        AppLanguage.GERMAN -> "WebView-Anbieter"
        AppLanguage.RUSSIAN -> "Поставщик WebView"
        AppLanguage.JAPANESE -> "WebView プロバイダー"
        AppLanguage.KOREAN -> "WebView 제공자"
    }

    val webViewVersion: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "版本"
        AppLanguage.ENGLISH -> "Version"
        AppLanguage.ARABIC -> "الإصدار"
        AppLanguage.PORTUGUESE -> "Versão"
        AppLanguage.SPANISH -> "Versión"
        AppLanguage.FRENCH -> "Version"
        AppLanguage.GERMAN -> "Version"
        AppLanguage.RUSSIAN -> "Версия"
        AppLanguage.JAPANESE -> "バージョン"
        AppLanguage.KOREAN -> "버전"
    }

    val webViewPackage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "包名"
        AppLanguage.ENGLISH -> "Package"
        AppLanguage.ARABIC -> "الحزمة"
        AppLanguage.PORTUGUESE -> "Pacote"
        AppLanguage.SPANISH -> "Paquete"
        AppLanguage.FRENCH -> "Paquet"
        AppLanguage.GERMAN -> "Paket"
        AppLanguage.RUSSIAN -> "Пакет"
        AppLanguage.JAPANESE -> "パッケージ"
        AppLanguage.KOREAN -> "패키지"
    }

    val changeWebViewProvider: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "更改 WebView 提供者"
        AppLanguage.ENGLISH -> "Change WebView Provider"
        AppLanguage.ARABIC -> "تغيير مزود WebView"
        AppLanguage.PORTUGUESE -> "Alterar Provedor WebView"
        AppLanguage.SPANISH -> "Cambiar Proveedor WebView"
        AppLanguage.FRENCH -> "Changer le Fournisseur WebView"
        AppLanguage.GERMAN -> "WebView-Anbieter ändern"
        AppLanguage.RUSSIAN -> "Изменить Поставщика WebView"
        AppLanguage.JAPANESE -> "WebView プロバイダーを変更"
        AppLanguage.KOREAN -> "WebView 제공자 변경"
    }

    val changeWebViewProviderDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "打开系统设置更改默认 WebView 实现（需要开发者选项）"
        AppLanguage.ENGLISH -> "Open system settings to change default WebView (Developer Options required)"
        AppLanguage.ARABIC -> "فتح إعدادات النظام لتغيير WebView الافتراضي (يتطلب خيارات المطور)"
        AppLanguage.PORTUGUESE -> "Abrir configurações do sistema para alterar o WebView padrão (Opções de Desenvolvedor necessárias)"
        AppLanguage.SPANISH -> "Abrir ajustes del sistema para cambiar el WebView predeterminado (Opciones de Desarrollador requeridas)"
        AppLanguage.FRENCH -> "Ouvrir les paramètres système pour changer le WebView par défaut (Options de Développeur requises)"
        AppLanguage.GERMAN -> "Systemeinstellungen öffnen, um Standard-WebView zu ändern (Entwickleroptionen erforderlich)"
        AppLanguage.RUSSIAN -> "Открыть системные настройки для смены WebView по умолчанию (требуются Опции Разработчика)"
        AppLanguage.JAPANESE -> "システム設定を開いてデフォルトの WebView を変更（開発者オプションが必要）"
        AppLanguage.KOREAN -> "시스템 설정을 열어 기본 WebView 변경 (개발자 옵션 필요)"
    }

    val webViewProvidersTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "可作为 WebView 提供者的应用"
        AppLanguage.ENGLISH -> "Apps that can provide WebView"
        AppLanguage.ARABIC -> "التطبيقات التي يمكنها توفير WebView"
        AppLanguage.PORTUGUESE -> "Apps que podem ser provedores WebView"
        AppLanguage.SPANISH -> "Apps que pueden ser proveedores WebView"
        AppLanguage.FRENCH -> "Apps pouvant fournir WebView"
        AppLanguage.GERMAN -> "Apps als WebView-Anbieter"
        AppLanguage.RUSSIAN -> "Приложения-поставщики WebView"
        AppLanguage.JAPANESE -> "WebView プロバイダーになれるアプリ"
        AppLanguage.KOREAN -> "WebView 제공자가 될 수 있는 앱"
    }

    val webViewProvidersDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "仅列出系统白名单中允许作为 WebView 实现的已安装应用"
        AppLanguage.ENGLISH -> "Only installed apps whitelisted by the system as WebView implementations"
        AppLanguage.ARABIC -> "يعرض فقط التطبيقات المثبتة الموجودة في القائمة البيضاء للنظام كتطبيقات WebView"
        AppLanguage.PORTUGUESE -> "Lista apenas apps instalados permitidos pelo sistema como implementações WebView"
        AppLanguage.SPANISH -> "Solo se listan las apps instaladas permitidas por el sistema como WebView"
        AppLanguage.FRENCH -> "Seules les apps installées autorisées par le système comme implémentations WebView"
        AppLanguage.GERMAN -> "Nur installierte Apps aus der System-Whitelist für WebView-Implementierungen"
        AppLanguage.RUSSIAN -> "Только установленные приложения из системного белого списка WebView"
        AppLanguage.JAPANESE -> "システムのホワイトリストにある WebView 実装として許可されたインストール済みアプリのみ"
        AppLanguage.KOREAN -> "시스템 화이트리스트에서 WebView 구현으로 허용된 설치 앱만 표시"
    }

    val noOtherWebViewProviders: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "此设备上没有其他可作为 WebView 提供者的应用"
        AppLanguage.ENGLISH -> "No other WebView-capable providers installed"
        AppLanguage.ARABIC -> "لا توجد موفرات WebView أخرى مثبتة"
        AppLanguage.PORTUGUESE -> "Nenhum outro provedor WebView instalado"
        AppLanguage.SPANISH -> "No hay otros proveedores WebView instalados"
        AppLanguage.FRENCH -> "Aucun autre fournisseur WebView installé"
        AppLanguage.GERMAN -> "Keine weiteren WebView-Anbieter installiert"
        AppLanguage.RUSSIAN -> "Других поставщиков WebView не установлено"
        AppLanguage.JAPANESE -> "他の WebView プロバイダーはインストールされていません"
        AppLanguage.KOREAN -> "설치된 다른 WebView 제공자가 없습니다"
    }

    val singleWebViewProviderNote: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "此设备只有一个 WebView 实现，系统不支持切换"
        AppLanguage.ENGLISH -> "This device has a single WebView implementation; switching is not supported"
        AppLanguage.ARABIC -> "يحتوي هذا الجهاز على تطبيق WebView واحد فقط؛ التبديل غير مدعوم"
        AppLanguage.PORTUGUESE -> "Este dispositivo tem uma única implementação WebView; a troca não é suportada"
        AppLanguage.SPANISH -> "Este dispositivo solo tiene una implementación WebView; no se puede cambiar"
        AppLanguage.FRENCH -> "Cet appareil n'a qu'une seule implémentation WebView ; le changement n'est pas possible"
        AppLanguage.GERMAN -> "Dieses Gerät hat nur eine WebView-Implementierung; ein Wechsel wird nicht unterstützt"
        AppLanguage.RUSSIAN -> "На этом устройстве только одна реализация WebView; переключение не поддерживается"
        AppLanguage.JAPANESE -> "この端末には WebView 実装が 1 つしかなく、切り替えはサポートされていません"
        AppLanguage.KOREAN -> "이 기기에는 WebView 구현이 하나뿐이며 전환을 지원하지 않습니다"
    }

    val recommendedBrowsers: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "推荐浏览器下载"
        AppLanguage.ENGLISH -> "Recommended Browsers"
        AppLanguage.ARABIC -> "المتصفحات الموصى بها"
        AppLanguage.PORTUGUESE -> "Navegadores Recomendados"
        AppLanguage.SPANISH -> "Navegadores Recomendados"
        AppLanguage.FRENCH -> "Navigateurs Recommandés"
        AppLanguage.GERMAN -> "Empfohlene Browser"
        AppLanguage.RUSSIAN -> "Рекомендуемые Браузеры"
        AppLanguage.JAPANESE -> "推奨ブラウザ"
        AppLanguage.KOREAN -> "권장 브라우저"
    }

    val recommendedBrowsersDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "安装这些浏览器后可在开发者选项中选择作为 WebView 提供者"
        AppLanguage.ENGLISH -> "After installing, select as WebView provider in Developer Options"
        AppLanguage.ARABIC -> "بعد التثبيت، حدد كمزود WebView في خيارات المطور"
        AppLanguage.PORTUGUESE -> "Após instalar, selecione como provedor WebView nas Opções de Desenvolvedor"
        AppLanguage.SPANISH -> "Después de instalar, selecciona como proveedor WebView en Opciones de Desarrollador"
        AppLanguage.FRENCH -> "Après installation, sélectionner comme fournisseur WebView dans les Options de Développeur"
        AppLanguage.GERMAN -> "Nach der Installation als WebView-Anbieter in den Entwickleroptionen auswählen"
        AppLanguage.RUSSIAN -> "После установки выберите как поставщика WebView в Опциях Разработчика"
        AppLanguage.JAPANESE -> "インストール後、開発者オプションで WebView プロバイダーとして選択"
        AppLanguage.KOREAN -> "설치 후 개발자 옵션에서 WebView 제공자로 선택"
    }

    val browserChromeDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Google 官方浏览器，性能优秀"
        AppLanguage.ENGLISH -> "Google's official browser, excellent performance"
        AppLanguage.ARABIC -> "متصفح Google الرسمي، أداء ممتاز"
        AppLanguage.PORTUGUESE -> "Navegador oficial do Google, excelente desempenho"
        AppLanguage.SPANISH -> "Navegador oficial de Google, excelente rendimiento"
        AppLanguage.FRENCH -> "Navigateur officiel de Google, performances excellentes"
        AppLanguage.GERMAN -> "Offizieller Browser von Google, hervorragende Leistung"
        AppLanguage.RUSSIAN -> "Официальный браузер Google, отличная производительность"
        AppLanguage.JAPANESE -> "Google の公式ブラウザ、優れたパフォーマンス"
        AppLanguage.KOREAN -> "Google 공식 브라우저, 뛰어난 성능"
    }

    val browserEdgeDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "微软官方浏览器，基于 Chromium"
        AppLanguage.ENGLISH -> "Microsoft's browser, Chromium-based"
        AppLanguage.ARABIC -> "متصفح Microsoft، مبني على Chromium"
        AppLanguage.PORTUGUESE -> "Navegador da Microsoft, baseado em Chromium"
        AppLanguage.SPANISH -> "Navegador de Microsoft, basado en Chromium"
        AppLanguage.FRENCH -> "Navigateur de Microsoft, basé sur Chromium"
        AppLanguage.GERMAN -> "Browser von Microsoft, Chromium-basiert"
        AppLanguage.RUSSIAN -> "Браузер Microsoft, на базе Chromium"
        AppLanguage.JAPANESE -> "Microsoft のブラウザ、Chromium ベース"
        AppLanguage.KOREAN -> "Microsoft 브라우저, Chromium 기반"
    }

    val browserFirefoxDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "开源浏览器，注重隐私保护"
        AppLanguage.ENGLISH -> "Open source, privacy-focused"
        AppLanguage.ARABIC -> "مفتوح المصدر، يركز على الخصوصية"
        AppLanguage.PORTUGUESE -> "Código aberto, focado em privacidade"
        AppLanguage.SPANISH -> "Código abierto, enfocado en la privacidad"
        AppLanguage.FRENCH -> "Open source, axé sur la confidentialité"
        AppLanguage.GERMAN -> "Open Source, datenschutzorientiert"
        AppLanguage.RUSSIAN -> "С открытым исходным кодом, с акцентом на конфиденциальность"
        AppLanguage.JAPANESE -> "オープンソース、プライバシー重視"
        AppLanguage.KOREAN -> "오픈 소스, 개인정보 보호 중심"
    }

    val browserBraveDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "内置广告拦截，隐私优先"
        AppLanguage.ENGLISH -> "Built-in ad blocking, privacy first"
        AppLanguage.ARABIC -> "حظر الإعلانات المدمج، الخصوصية أولاً"
        AppLanguage.PORTUGUESE -> "Bloqueio de anúncios integrado, privacidade em primeiro lugar"
        AppLanguage.SPANISH -> "Bloqueo de anuncios integrado, privacidad primero"
        AppLanguage.FRENCH -> "Blocage des publicités intégré, confidentialité d'abord"
        AppLanguage.GERMAN -> "Eingebauter Werbeblocker, Datenschutz zuerst"
        AppLanguage.RUSSIAN -> "Встроенная блокировка рекламы, конфиденциальность прежде всего"
        AppLanguage.JAPANESE -> "内蔵広告ブロック、プライバシー優先"
        AppLanguage.KOREAN -> "내장 광고 차단, 개인정보 보호 우선"
    }

    val browserViaDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "轻量级浏览器，体积小速度快"
        AppLanguage.ENGLISH -> "Lightweight browser, small and fast"
        AppLanguage.ARABIC -> "متصفح خفيف، صغير وسريع"
        AppLanguage.PORTUGUESE -> "Navegador leve, pequeno e rápido"
        AppLanguage.SPANISH -> "Navegador ligero, pequeño y rápido"
        AppLanguage.FRENCH -> "Navigateur léger, petit et rapide"
        AppLanguage.GERMAN -> "Leichter Browser, klein und schnell"
        AppLanguage.RUSSIAN -> "Лёгкий браузер, компактный и быстрый"
        AppLanguage.JAPANESE -> "軽量ブラウザ、小さくて速い"
        AppLanguage.KOREAN -> "가벼운 브라우저, 작고 빠름"
    }

    val browserX5: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "腾讯 X5 内核"
        AppLanguage.ENGLISH -> "Tencent X5 Kernel"
        AppLanguage.ARABIC -> "نواة Tencent X5"
        AppLanguage.PORTUGUESE -> "Núcleo Tencent X5"
        AppLanguage.SPANISH -> "Núcleo Tencent X5"
        AppLanguage.FRENCH -> "Noyau Tencent X5"
        AppLanguage.GERMAN -> "Tencent X5-Kernel"
        AppLanguage.RUSSIAN -> "Ядро Tencent X5"
        AppLanguage.JAPANESE -> "Tencent X5 カーネル"
        AppLanguage.KOREAN -> "Tencent X5 커널"
    }

    val browserX5Desc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "腾讯 WebView 解决方案，兼容性好"
        AppLanguage.ENGLISH -> "Tencent WebView solution, good compatibility"
        AppLanguage.ARABIC -> "حل Tencent WebView، توافق جيد"
        AppLanguage.PORTUGUESE -> "Solução WebView da Tencent, boa compatibilidade"
        AppLanguage.SPANISH -> "Solución WebView de Tencent, buena compatibilidad"
        AppLanguage.FRENCH -> "Solution WebView de Tencent, bonne compatibilité"
        AppLanguage.GERMAN -> "Tencent WebView-Lösung, gute Kompatibilität"
        AppLanguage.RUSSIAN -> "Решение WebView от Tencent, хорошая совместимость"
        AppLanguage.JAPANESE -> "Tencent WebView ソリューション、互換性良好"
        AppLanguage.KOREAN -> "Tencent WebView 솔루션, 우수한 호환성"
    }

    val webViewNote: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "注意：更改 WebView 提供者需要启用开发者选项"
        AppLanguage.ENGLISH -> "Note: Changing WebView provider requires Developer Options enabled"
        AppLanguage.ARABIC -> "ملاحظة: تغيير مزود WebView يتطلب تمكين خيارات المطور"
        AppLanguage.PORTUGUESE -> "Nota: Alterar o provedor WebView requer Opções de Desenvolvedor ativadas"
        AppLanguage.SPANISH -> "Nota: Cambiar el proveedor WebView requiere Opciones de Desarrollador habilitadas"
        AppLanguage.FRENCH -> "Note : Changer le fournisseur WebView nécessite les Options de Développeur activées"
        AppLanguage.GERMAN -> "Hinweis: Ändern des WebView-Anbieters erfordert aktivierte Entwickleroptionen"
        AppLanguage.RUSSIAN -> "Примечание: Для смены поставщика WebView требуются включённые Опции Разработчика"
        AppLanguage.JAPANESE -> "注意: WebView プロバイダーの変更には開発者オプションの有効化が必要です"
        AppLanguage.KOREAN -> "참고: WebView 제공자 변경 시 개발자 옵션 활성화 필요"
    }

    val howToEnableDeveloperOptions: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "如何启用开发者选项？"
        AppLanguage.ENGLISH -> "How to enable Developer Options?"
        AppLanguage.ARABIC -> "كيفية تمكين خيارات المطور؟"
        AppLanguage.PORTUGUESE -> "Como ativar as Opções de Desenvolvedor?"
        AppLanguage.SPANISH -> "¿Cómo habilitar las Opciones de Desarrollador?"
        AppLanguage.FRENCH -> "Comment activer les Options de Développeur ?"
        AppLanguage.GERMAN -> "Wie werden die Entwickleroptionen aktiviert?"
        AppLanguage.RUSSIAN -> "Как включить Опции Разработчика?"
        AppLanguage.JAPANESE -> "開発者オプションを有効にする方法は？"
        AppLanguage.KOREAN -> "개발자 옵션을 활성화하는 방법?"
    }

    val developerOptionsSteps: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "设置 → 关于手机 → 连续点击\"版本号\"7次"
        AppLanguage.ENGLISH -> "Settings → About Phone → Tap \"Build Number\" 7 times"
        AppLanguage.ARABIC -> "الإعدادات ← حول الهاتف ← انقر على \"رقم البناء\" 7 مرات"
        AppLanguage.PORTUGUESE -> "Configurações → Sobre o Telefone → Tocar em \"Número da Compilação\" 7 vezes"
        AppLanguage.SPANISH -> "Ajustes → Sobre el Teléfono → Tocar \"Número de Compilación\" 7 veces"
        AppLanguage.FRENCH -> "Paramètres → À propos du Téléphone → Appuyer sur \"Numéro de Build\" 7 fois"
        AppLanguage.GERMAN -> "Einstellungen → Über das Telefon → \"Build-Nummer\" 7 Mal antippen"
        AppLanguage.RUSSIAN -> "Настройки → О телефоне → Нажать \"Номер сборки\" 7 раз"
        AppLanguage.JAPANESE -> "設定 → 電話情報 → \"ビルド番号\" を7回タップ"
        AppLanguage.KOREAN -> "설정 → 휴대전화 정보 → \"빌드 번호\"를 7번 탭"
    }

    val currentlyUsing: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "当前使用中"
        AppLanguage.ENGLISH -> "Currently in use"
        AppLanguage.ARABIC -> "قيد الاستخدام حالياً"
        AppLanguage.PORTUGUESE -> "Em uso atualmente"
        AppLanguage.SPANISH -> "En uso actualmente"
        AppLanguage.FRENCH -> "Actuellement utilisé"
        AppLanguage.GERMAN -> "Aktuell in Verwendung"
        AppLanguage.RUSSIAN -> "Сейчас используется"
        AppLanguage.JAPANESE -> "現在使用中"
        AppLanguage.KOREAN -> "현재 사용 중"
    }

    val canBeWebViewProvider: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "可设为 WebView 提供者"
        AppLanguage.ENGLISH -> "Can be WebView provider"
        AppLanguage.ARABIC -> "يمكن أن يكون مزود WebView"
        AppLanguage.PORTUGUESE -> "Pode ser provedor WebView"
        AppLanguage.SPANISH -> "Puede ser proveedor WebView"
        AppLanguage.FRENCH -> "Peut être fournisseur WebView"
        AppLanguage.GERMAN -> "Kann WebView-Anbieter sein"
        AppLanguage.RUSSIAN -> "Может быть поставщиком WebView"
        AppLanguage.JAPANESE -> "WebView プロバイダーになることができます"
        AppLanguage.KOREAN -> "WebView 제공자가 될 수 있습니다"
    }

    val openInBrowser: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Open in browser"
        AppLanguage.ENGLISH -> "Open in browser"
        AppLanguage.ARABIC -> "فتح في المتصفح"
        AppLanguage.PORTUGUESE -> "Abrir no navegador"
        AppLanguage.SPANISH -> "Abrir en navegador"
        AppLanguage.FRENCH -> "Ouvrir dans le navigateur"
        AppLanguage.GERMAN -> "Im Browser öffnen"
        AppLanguage.RUSSIAN -> "Открыть в браузере"
        AppLanguage.JAPANESE -> "ブラウザで開く"
        AppLanguage.KOREAN -> "브라우저에서 열기"
    }

    val scriptAuthor: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "作者"
        AppLanguage.ENGLISH -> "Author"
        AppLanguage.ARABIC -> "المؤلف"
        AppLanguage.PORTUGUESE -> "Autor"
        AppLanguage.SPANISH -> "Autor"
        AppLanguage.FRENCH -> "Auteur"
        AppLanguage.GERMAN -> "Autor"
        AppLanguage.RUSSIAN -> "Автор"
        AppLanguage.JAPANESE -> "作者"
        AppLanguage.KOREAN -> "작성자"
    }

    val loadFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Load failed"
        AppLanguage.ENGLISH -> "Load failed"
        AppLanguage.ARABIC -> "فشل التحميل"
        AppLanguage.PORTUGUESE -> "Falha ao carregar"
        AppLanguage.SPANISH -> "Error al cargar"
        AppLanguage.FRENCH -> "Échec du chargement"
        AppLanguage.GERMAN -> "Laden fehlgeschlagen"
        AppLanguage.RUSSIAN -> "Ошибка загрузки"
        AppLanguage.JAPANESE -> "読み込みに失敗しました"
        AppLanguage.KOREAN -> "로드 실패"
    }

    val noMatchingScripts: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "没有匹配的脚本"
        AppLanguage.ENGLISH -> "No matching scripts"
        AppLanguage.ARABIC -> "لا توجد سكريبتات مطابقة"
        AppLanguage.PORTUGUESE -> "Nenhum script correspondente"
        AppLanguage.SPANISH -> "Sin scripts coincidentes"
        AppLanguage.FRENCH -> "Aucun script correspondant"
        AppLanguage.GERMAN -> "Keine passenden Skripte"
        AppLanguage.RUSSIAN -> "Нет совпадающих скриптов"
        AppLanguage.JAPANESE -> "一致するスクリプトがありません"
        AppLanguage.KOREAN -> "일치하는 스크립트가 없습니다"
    }

    val matchRules: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "匹配规则"
        AppLanguage.ENGLISH -> "Match rules"
        AppLanguage.ARABIC -> "قواعد المطابقة"
        AppLanguage.PORTUGUESE -> "Regras de correspondência"
        AppLanguage.SPANISH -> "Reglas de coincidencia"
        AppLanguage.FRENCH -> "Règles de correspondance"
        AppLanguage.GERMAN -> "Übereinstimmungsregeln"
        AppLanguage.RUSSIAN -> "Правила совпадения"
        AppLanguage.JAPANESE -> "一致ルール"
        AppLanguage.KOREAN -> "일치 규칙"
    }

    val extensionModulesTab: String get() = when (Strings.lang) {
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

    val moduleDeleteConfirm: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "确定要删除这个模块吗？"
        AppLanguage.ENGLISH -> "Delete this module?"
        AppLanguage.ARABIC -> "هل تريد حذف هذه الوحدة؟"
        AppLanguage.PORTUGUESE -> "Excluir este módulo?"
        AppLanguage.SPANISH -> "¿Eliminar este módulo?"
        AppLanguage.FRENCH -> "Supprimer ce module ?"
        AppLanguage.GERMAN -> "Dieses Modul löschen?"
        AppLanguage.RUSSIAN -> "Удалить этот модуль?"
        AppLanguage.JAPANESE -> "このモジュールを削除しますか？"
        AppLanguage.KOREAN -> "이 모듈을 삭제할까요?"
    }

    val userScriptsTab: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "浏览器扩展"
        AppLanguage.ENGLISH -> "Browser Extensions"
        AppLanguage.ARABIC -> "إضافات المتصفح"
        AppLanguage.PORTUGUESE -> "Extensões de Navegador"
        AppLanguage.SPANISH -> "Extensiones de Navegador"
        AppLanguage.FRENCH -> "Extensions de Navigateur"
        AppLanguage.GERMAN -> "Browser-Erweiterungen"
        AppLanguage.RUSSIAN -> "Расширения Браузера"
        AppLanguage.JAPANESE -> "ブラウザ拡張機能"
        AppLanguage.KOREAN -> "브라우저 확장 프로그램"
    }

    val noUserScripts: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "暂无浏览器扩展"
        AppLanguage.ENGLISH -> "No browser extensions"
        AppLanguage.ARABIC -> "لا توجد إضافات متصفح"
        AppLanguage.PORTUGUESE -> "Nenhuma extensão de navegador"
        AppLanguage.SPANISH -> "Sin extensiones de navegador"
        AppLanguage.FRENCH -> "Aucune extension de navigateur"
        AppLanguage.GERMAN -> "Keine Browser-Erweiterungen"
        AppLanguage.RUSSIAN -> "Нет расширений браузера"
        AppLanguage.JAPANESE -> "ブラウザ拡張機能がありません"
        AppLanguage.KOREAN -> "브라우저 확장 프로그램이 없습니다"
    }

    val noUserScriptsHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "导入油猴脚本 (.user.js) 或带 content_scripts 的 Chrome 扩展 (.crx/.zip)"
        AppLanguage.ENGLISH -> "Import userscripts (.user.js) or Chrome extensions with content_scripts (.crx/.zip)"
        AppLanguage.ARABIC -> "استيراد سكريبتات (.user.js) أو إضافات كروم التي تحتوي على content_scripts (.crx/.zip)"
        AppLanguage.PORTUGUESE -> "Importar userscripts (.user.js) ou extensões do Chrome com content_scripts (.crx/.zip)"
        AppLanguage.SPANISH -> "Importar userscripts (.user.js) o extensiones de Chrome con content_scripts (.crx/.zip)"
        AppLanguage.FRENCH -> "Importer des userscripts (.user.js) ou des extensions Chrome avec content_scripts (.crx/.zip)"
        AppLanguage.GERMAN -> "Userscripts (.user.js) oder Chrome-Erweiterungen mit content_scripts (.crx/.zip) importieren"
        AppLanguage.RUSSIAN -> "Импортировать userscripts (.user.js) или расширения Chrome с content_scripts (.crx/.zip)"
        AppLanguage.JAPANESE -> "userscripts (.user.js) または content_scripts を含む Chrome 拡張機能 (.crx/.zip) をインポート"
        AppLanguage.KOREAN -> "userscripts (.user.js) 또는 content_scripts가 포함된 Chrome 확장 프로그램 (.crx/.zip) 가져오기"
    }

    val viewSourceCode: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "查看源码"
        AppLanguage.ENGLISH -> "View Source"
        AppLanguage.ARABIC -> "عرض المصدر"
        AppLanguage.PORTUGUESE -> "Ver Fonte"
        AppLanguage.SPANISH -> "Ver Código Fuente"
        AppLanguage.FRENCH -> "Voir la Source"
        AppLanguage.GERMAN -> "Quelltext anzeigen"
        AppLanguage.RUSSIAN -> "Просмотр Исходного Кода"
        AppLanguage.JAPANESE -> "ソースを表示"
        AppLanguage.KOREAN -> "소스 보기"
    }

    val cannotReadFile: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "无法读取文件内容"
        AppLanguage.ENGLISH -> "Cannot read file content"
        AppLanguage.ARABIC -> "لا يمكن قراءة محتوى الملف"
        AppLanguage.PORTUGUESE -> "Não foi possível ler o conteúdo do arquivo"
        AppLanguage.SPANISH -> "No se puede leer el contenido del archivo"
        AppLanguage.FRENCH -> "Impossible de lire le contenu du fichier"
        AppLanguage.GERMAN -> "Dateiinhalt kann nicht gelesen werden"
        AppLanguage.RUSSIAN -> "Не удалось прочитать содержимое файла"
        AppLanguage.JAPANESE -> "ファイルの内容を読み込めません"
        AppLanguage.KOREAN -> "파일 내용을 읽을 수 없습니다"
    }

    val addEntryFromMarketDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "从社区模块市场一键安装"
        AppLanguage.ENGLISH -> "Install with one tap from the community market"
        AppLanguage.ARABIC -> "تثبيت بنقرة واحدة من سوق المجتمع"
        AppLanguage.PORTUGUESE -> "Instalar com um toque do mercado comunitário"
        AppLanguage.SPANISH -> "Instalar con un toque desde el mercado comunitario"
        AppLanguage.FRENCH -> "Installer en un geste depuis le marché communautaire"
        AppLanguage.GERMAN -> "Mit einem Tipp aus dem Community-Markt installieren"
        AppLanguage.RUSSIAN -> "Установить одним нажатием из сообщества"
        AppLanguage.JAPANESE -> "コミュニティマーケットからワンタップでインストール"
        AppLanguage.KOREAN -> "커뮤니티 마켓에서 원탭으로 설치"
    }
    val addEntryAiDevelopDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "用一句话描述，AI 帮你写"
        AppLanguage.ENGLISH -> "Describe in one sentence, AI builds it"
        AppLanguage.ARABIC -> "صفه بجملة، والذكاء الاصطناعي ينشئه"
        AppLanguage.PORTUGUESE -> "Descreva em uma frase, a IA constrói para você"
        AppLanguage.SPANISH -> "Describe en una frase, la IA lo crea"
        AppLanguage.FRENCH -> "Décrivez en une phrase, l'IA le construit"
        AppLanguage.GERMAN -> "In einem Satz beschreiben, die KI erstellt es"
        AppLanguage.RUSSIAN -> "Опишите одним предложением, ИИ создаст это"
        AppLanguage.JAPANESE -> "一言で説明すると、AI が作成します"
        AppLanguage.KOREAN -> "한 문장으로 설명하면 AI가 만들어 줍니다"
    }
    val addEntryManualDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "在内置编辑器里手写 JS / CSS"
        AppLanguage.ENGLISH -> "Hand-write JS / CSS in the built-in editor"
        AppLanguage.ARABIC -> "اكتب JS / CSS يدويًا في المحرر المدمج"
        AppLanguage.PORTUGUESE -> "Escrever JS / CSS manualmente no editor integrado"
        AppLanguage.SPANISH -> "Escribir JS / CSS a mano en el editor integrado"
        AppLanguage.FRENCH -> "Écrire JS / CSS à la main dans l'éditeur intégré"
        AppLanguage.GERMAN -> "JS / CSS im integrierten Editor von Hand schreiben"
        AppLanguage.RUSSIAN -> "Писать JS / CSS вручную во встроенном редакторе"
        AppLanguage.JAPANESE -> "内蔵エディタで JS / CSS を手動で記述"
        AppLanguage.KOREAN -> "내장 에디터에서 JS / CSS 직접 작성"
    }
    val addEntrySectionImport: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "从文件导入"
        AppLanguage.ENGLISH -> "Import from file"
        AppLanguage.ARABIC -> "استيراد من ملف"
        AppLanguage.PORTUGUESE -> "Importar de arquivo"
        AppLanguage.SPANISH -> "Importar desde archivo"
        AppLanguage.FRENCH -> "Importer depuis un fichier"
        AppLanguage.GERMAN -> "Aus Datei importieren"
        AppLanguage.RUSSIAN -> "Импортировать из файла"
        AppLanguage.JAPANESE -> "ファイルからインポート"
        AppLanguage.KOREAN -> "파일에서 가져오기"
    }

    val moduleMarketSearchHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "搜索市场模块"
        AppLanguage.ENGLISH -> "Search the market"
        AppLanguage.ARABIC -> "ابحث في السوق"
        AppLanguage.PORTUGUESE -> "Pesquisar no mercado"
        AppLanguage.SPANISH -> "Buscar en el mercado"
        AppLanguage.FRENCH -> "Rechercher dans le marché"
        AppLanguage.GERMAN -> "Im Markt suchen"
        AppLanguage.RUSSIAN -> "Искать на рынке"
        AppLanguage.JAPANESE -> "マーケットを検索"
        AppLanguage.KOREAN -> "마켓 검색"
    }

    val moduleMarketAll: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "全部"
        AppLanguage.ENGLISH -> "All"
        AppLanguage.ARABIC -> "الكل"
        AppLanguage.PORTUGUESE -> "Tudo"
        AppLanguage.SPANISH -> "Todos"
        AppLanguage.FRENCH -> "Tous"
        AppLanguage.GERMAN -> "Alle"
        AppLanguage.RUSSIAN -> "Все"
        AppLanguage.JAPANESE -> "すべて"
        AppLanguage.KOREAN -> "전체"
    }

    val moduleMarketInstall: String get() = when (Strings.lang) {
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

    val moduleMarketUpdate: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "更新"
        AppLanguage.ENGLISH -> "Update"
        AppLanguage.ARABIC -> "تحديث"
        AppLanguage.PORTUGUESE -> "Atualizar"
        AppLanguage.SPANISH -> "Actualizar"
        AppLanguage.FRENCH -> "Mettre à jour"
        AppLanguage.GERMAN -> "Aktualisieren"
        AppLanguage.RUSSIAN -> "Обновить"
        AppLanguage.JAPANESE -> "更新"
        AppLanguage.KOREAN -> "업데이트"
    }

    val moduleMarketDlManifest: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下载清单…"
        AppLanguage.ENGLISH -> "Downloading manifest…"
        AppLanguage.ARABIC -> "Downloading manifest…"
        AppLanguage.PORTUGUESE -> "Baixando manifesto…"
        AppLanguage.SPANISH -> "Descargando manifiesto…"
        AppLanguage.FRENCH -> "Téléchargement du manifeste…"
        AppLanguage.GERMAN -> "Manifest wird heruntergeladen…"
        AppLanguage.RUSSIAN -> "Загрузка манифеста…"
        AppLanguage.JAPANESE -> "マニフェストをダウンロード中…"
        AppLanguage.KOREAN -> "매니페스트 다운로드 중…"
    }

    val moduleMarketDlCode: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下载代码…"
        AppLanguage.ENGLISH -> "Downloading code…"
        AppLanguage.ARABIC -> "Downloading code…"
        AppLanguage.PORTUGUESE -> "Baixando código…"
        AppLanguage.SPANISH -> "Descargando código…"
        AppLanguage.FRENCH -> "Téléchargement du code…"
        AppLanguage.GERMAN -> "Code wird heruntergeladen…"
        AppLanguage.RUSSIAN -> "Загрузка кода…"
        AppLanguage.JAPANESE -> "コードをダウンロード中…"
        AppLanguage.KOREAN -> "코드 다운로드 중…"
    }

    val moduleMarketDlStyle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下载样式…"
        AppLanguage.ENGLISH -> "Downloading styles…"
        AppLanguage.ARABIC -> "Downloading styles…"
        AppLanguage.PORTUGUESE -> "Baixando estilos…"
        AppLanguage.SPANISH -> "Descargando estilos…"
        AppLanguage.FRENCH -> "Téléchargement des styles…"
        AppLanguage.GERMAN -> "Stile werden heruntergeladen…"
        AppLanguage.RUSSIAN -> "Загрузка стилей…"
        AppLanguage.JAPANESE -> "スタイルをダウンロード中…"
        AppLanguage.KOREAN -> "스타일 다운로드 중…"
    }

    val moduleMarketInstalling: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "正在安装"
        AppLanguage.ENGLISH -> "Installing"
        AppLanguage.ARABIC -> "جارٍ التثبيت"
        AppLanguage.PORTUGUESE -> "Instalando"
        AppLanguage.SPANISH -> "Instalando"
        AppLanguage.FRENCH -> "Installation"
        AppLanguage.GERMAN -> "Wird installiert"
        AppLanguage.RUSSIAN -> "Установка"
        AppLanguage.JAPANESE -> "インストール中"
        AppLanguage.KOREAN -> "설치 중"
    }

    val moduleMarketInstalled: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已安装 %s"
        AppLanguage.ENGLISH -> "Installed %s"
        AppLanguage.ARABIC -> "تم تثبيت %s"
        AppLanguage.PORTUGUESE -> "Instalado %s"
        AppLanguage.SPANISH -> "Instalado %s"
        AppLanguage.FRENCH -> "Installé %s"
        AppLanguage.GERMAN -> "Installiert %s"
        AppLanguage.RUSSIAN -> "Установлено %s"
        AppLanguage.JAPANESE -> "インストール済み %s"
        AppLanguage.KOREAN -> "설치됨 %s"
    }

    val moduleMarketInstallFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "安装失败：%s"
        AppLanguage.ENGLISH -> "Install failed: %s"
        AppLanguage.ARABIC -> "فشل التثبيت: %s"
        AppLanguage.PORTUGUESE -> "Falha na instalação: %s"
        AppLanguage.SPANISH -> "Error de instalación: %s"
        AppLanguage.FRENCH -> "Échec de l'installation : %s"
        AppLanguage.GERMAN -> "Installation fehlgeschlagen: %s"
        AppLanguage.RUSSIAN -> "Ошибка установки: %s"
        AppLanguage.JAPANESE -> "インストールに失敗しました: %s"
        AppLanguage.KOREAN -> "설치 실패: %s"
    }

    val moduleMarketViewSource: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "在 GitHub 上查看源码"
        AppLanguage.ENGLISH -> "View source on GitHub"
        AppLanguage.ARABIC -> "عرض المصدر على GitHub"
        AppLanguage.PORTUGUESE -> "Ver código-fonte no GitHub"
        AppLanguage.SPANISH -> "Ver código fuente en GitHub"
        AppLanguage.FRENCH -> "Voir le code source sur GitHub"
        AppLanguage.GERMAN -> "Quellcode auf GitHub ansehen"
        AppLanguage.RUSSIAN -> "Исходный код на GitHub"
        AppLanguage.JAPANESE -> "GitHubでソースコードを表示"
        AppLanguage.KOREAN -> "GitHub에서 소스 보기"
    }

    val moduleMarketContribute: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "贡献模块"
        AppLanguage.ENGLISH -> "Contribute a module"
        AppLanguage.ARABIC -> "ساهم بوحدة"
        AppLanguage.PORTUGUESE -> "Contribuir com um módulo"
        AppLanguage.SPANISH -> "Contribuir con un módulo"
        AppLanguage.FRENCH -> "Contribuer un module"
        AppLanguage.GERMAN -> "Ein Modul beitragen"
        AppLanguage.RUSSIAN -> "Внести модуль"
        AppLanguage.JAPANESE -> "モジュールを貢献する"
        AppLanguage.KOREAN -> "모듈 기여하기"
    }

    val moduleMarketLoading: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "正在从 GitHub 加载模块"
        AppLanguage.ENGLISH -> "Loading modules from GitHub"
        AppLanguage.ARABIC -> "جارٍ تحميل الوحدات من GitHub"
        AppLanguage.PORTUGUESE -> "Carregando módulos do GitHub"
        AppLanguage.SPANISH -> "Cargando módulos desde GitHub"
        AppLanguage.FRENCH -> "Chargement des modules depuis GitHub"
        AppLanguage.GERMAN -> "Module werden von GitHub geladen"
        AppLanguage.RUSSIAN -> "Загрузка модулей с GitHub"
        AppLanguage.JAPANESE -> "GitHubからモジュールを読み込み中"
        AppLanguage.KOREAN -> "GitHub에서 모듈 로드 중"
    }

    val moduleMarketNoResults: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "没有匹配的模块"
        AppLanguage.ENGLISH -> "No matching modules"
        AppLanguage.ARABIC -> "لا توجد وحدات مطابقة"
        AppLanguage.PORTUGUESE -> "Nenhum módulo correspondente"
        AppLanguage.SPANISH -> "Sin módulos coincidentes"
        AppLanguage.FRENCH -> "Aucun module correspondant"
        AppLanguage.GERMAN -> "Keine passenden Module"
        AppLanguage.RUSSIAN -> "Нет совпадающих модулей"
        AppLanguage.JAPANESE -> "一致するモジュールはありません"
        AppLanguage.KOREAN -> "일치하는 모듈 없음"
    }

    val moduleMarketSubmittedBy: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "由 @%s 提交"
        AppLanguage.ENGLISH -> "Submitted by @%s"
        AppLanguage.ARABIC -> "قدّم بواسطة @%s"
        AppLanguage.PORTUGUESE -> "Enviado por @%s"
        AppLanguage.SPANISH -> "Enviado por @%s"
        AppLanguage.FRENCH -> "Soumis par @%s"
        AppLanguage.GERMAN -> "Eingereicht von @%s"
        AppLanguage.RUSSIAN -> "Отправил @%s"
        AppLanguage.JAPANESE -> "送信者: @%s"
        AppLanguage.KOREAN -> "제출자: @%s"
    }

    val moduleMarketWithContributors: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "及 %d 位贡献者"
        AppLanguage.ENGLISH -> "+ %d contributor(s)"
        AppLanguage.ARABIC -> "+ %d مساهم"
        AppLanguage.PORTUGUESE -> "+ %d contribuidor(es)"
        AppLanguage.SPANISH -> "+ %d colaborador(es)"
        AppLanguage.FRENCH -> "+ %d contributeur(s)"
        AppLanguage.GERMAN -> "+ %d Mitwirkende(r)"
        AppLanguage.RUSSIAN -> "+ %d участник(ов)"
        AppLanguage.JAPANESE -> "+ %d 人の貢献者"
        AppLanguage.KOREAN -> "+ %d명 기여자"
    }

    val moduleMarketContributorsTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "贡献者"
        AppLanguage.ENGLISH -> "Contributors"
        AppLanguage.ARABIC -> "المساهمون"
        AppLanguage.PORTUGUESE -> "Contribuidores"
        AppLanguage.SPANISH -> "Colaboradores"
        AppLanguage.FRENCH -> "Contributeurs"
        AppLanguage.GERMAN -> "Mitwirkende"
        AppLanguage.RUSSIAN -> "Участники"
        AppLanguage.JAPANESE -> "貢献者"
        AppLanguage.KOREAN -> "기여자"
    }

    val moduleMarketContributorsOverview: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "%1\$d 位贡献者 · 共 %2\$d 个模块"
        AppLanguage.ENGLISH -> "%1\$d contributors · %2\$d modules"
        AppLanguage.ARABIC -> "%1\$d مساهم · %2\$d وحدة"
        AppLanguage.PORTUGUESE -> "%1\$d contribuidores · %2\$d módulos"
        AppLanguage.SPANISH -> "%1\$d colaboradores · %2\$d módulos"
        AppLanguage.FRENCH -> "%1\$d contributeurs · %2\$d modules"
        AppLanguage.GERMAN -> "%1\$d Mitwirkende · %2\$d Module"
        AppLanguage.RUSSIAN -> "%1\$d участников · %2\$d модулей"
        AppLanguage.JAPANESE -> "%1\$d 人の貢献者 · %2\$d モジュール"
        AppLanguage.KOREAN -> "%1\$d명 기여자 · %2\$d개 모듈"
    }

    val moduleMarketTopContributor: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "首席贡献者"
        AppLanguage.ENGLISH -> "Top contributor"
        AppLanguage.ARABIC -> "أبرز مساهم"
        AppLanguage.PORTUGUESE -> "Principal contribuidor"
        AppLanguage.SPANISH -> "Principal colaborador"
        AppLanguage.FRENCH -> "Meilleur contributeur"
        AppLanguage.GERMAN -> "Top-Mitwirkender"
        AppLanguage.RUSSIAN -> "Лучший участник"
        AppLanguage.JAPANESE -> "トップ貢献者"
        AppLanguage.KOREAN -> "최고 기여자"
    }

    val moduleMarketModulesContributed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "贡献的模块"
        AppLanguage.ENGLISH -> "Modules contributed"
        AppLanguage.ARABIC -> "الوحدات المساهَم بها"
        AppLanguage.PORTUGUESE -> "Módulos contribuídos"
        AppLanguage.SPANISH -> "Módulos aportados"
        AppLanguage.FRENCH -> "Modules contribués"
        AppLanguage.GERMAN -> "Beigetragene Module"
        AppLanguage.RUSSIAN -> "Внесённые модули"
        AppLanguage.JAPANESE -> "貢献したモジュール"
        AppLanguage.KOREAN -> "기여한 모듈"
    }

    val moduleMarketViewProfile: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "查看 GitHub 主页"
        AppLanguage.ENGLISH -> "View GitHub profile"
        AppLanguage.ARABIC -> "عرض ملف GitHub"
        AppLanguage.PORTUGUESE -> "Ver perfil no GitHub"
        AppLanguage.SPANISH -> "Ver perfil de GitHub"
        AppLanguage.FRENCH -> "Voir le profil GitHub"
        AppLanguage.GERMAN -> "GitHub-Profil ansehen"
        AppLanguage.RUSSIAN -> "Профиль на GitHub"
        AppLanguage.JAPANESE -> "GitHubプロフィールを表示"
        AppLanguage.KOREAN -> "GitHub 프로필 보기"
    }

    val moduleMarketDetails: String get() = when (Strings.lang) {
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

    val moduleMarketCategory: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "分类"
        AppLanguage.ENGLISH -> "Category"
        AppLanguage.ARABIC -> "الفئة"
        AppLanguage.PORTUGUESE -> "Categoria"
        AppLanguage.SPANISH -> "Categoría"
        AppLanguage.FRENCH -> "Catégorie"
        AppLanguage.GERMAN -> "Kategorie"
        AppLanguage.RUSSIAN -> "Категория"
        AppLanguage.JAPANESE -> "カテゴリ"
        AppLanguage.KOREAN -> "카테고리"
    }

    val moduleMarketTags: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "标签"
        AppLanguage.ENGLISH -> "Tags"
        AppLanguage.ARABIC -> "الوسوم"
        AppLanguage.PORTUGUESE -> "Etiquetas"
        AppLanguage.SPANISH -> "Etiquetas"
        AppLanguage.FRENCH -> "Étiquettes"
        AppLanguage.GERMAN -> "Schlagwörter"
        AppLanguage.RUSSIAN -> "Теги"
        AppLanguage.JAPANESE -> "タグ"
        AppLanguage.KOREAN -> "태그"
    }

    val moduleMarketPermissions: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "权限"
        AppLanguage.ENGLISH -> "Permissions"
        AppLanguage.ARABIC -> "الأذونات"
        AppLanguage.PORTUGUESE -> "Permissões"
        AppLanguage.SPANISH -> "Permisos"
        AppLanguage.FRENCH -> "Autorisations"
        AppLanguage.GERMAN -> "Berechtigungen"
        AppLanguage.RUSSIAN -> "Разрешения"
        AppLanguage.JAPANESE -> "権限"
        AppLanguage.KOREAN -> "권한"
    }

    val moduleMarketUrlMatches: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "生效地址"
        AppLanguage.ENGLISH -> "URL matches"
        AppLanguage.ARABIC -> "مطابقة الروابط"
        AppLanguage.PORTUGUESE -> "URL correspondentes"
        AppLanguage.SPANISH -> "URL coincidentes"
        AppLanguage.FRENCH -> "URL correspondantes"
        AppLanguage.GERMAN -> "URL-Treffer"
        AppLanguage.RUSSIAN -> "Совпадения URL"
        AppLanguage.JAPANESE -> "URL一致"
        AppLanguage.KOREAN -> "URL 일치"
    }

    val moduleMarketRunAt: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "注入时机"
        AppLanguage.ENGLISH -> "Inject at"
        AppLanguage.ARABIC -> "وقت الحقن"
        AppLanguage.PORTUGUESE -> "Injetar em"
        AppLanguage.SPANISH -> "Inyectar en"
        AppLanguage.FRENCH -> "Injecter à"
        AppLanguage.GERMAN -> "Injizieren bei"
        AppLanguage.RUSSIAN -> "Внедрение при"
        AppLanguage.JAPANESE -> "注入タイミング"
        AppLanguage.KOREAN -> "주입 시점"
    }

    val moduleMarketMergedAt: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "合并于"
        AppLanguage.ENGLISH -> "Merged"
        AppLanguage.ARABIC -> "مدمج"
        AppLanguage.PORTUGUESE -> "Mesclado"
        AppLanguage.SPANISH -> "Fusionado"
        AppLanguage.FRENCH -> "Fusionné"
        AppLanguage.GERMAN -> "Zusammengeführt"
        AppLanguage.RUSSIAN -> "Слито"
        AppLanguage.JAPANESE -> "マージ済み"
        AppLanguage.KOREAN -> "병합됨"
    }

    val moduleMarketViewPullRequest: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "查看合并的 PR"
        AppLanguage.ENGLISH -> "View merged PR"
        AppLanguage.ARABIC -> "عرض طلب السحب المدمج"
        AppLanguage.PORTUGUESE -> "Ver PR mesclado"
        AppLanguage.SPANISH -> "Ver PR fusionado"
        AppLanguage.FRENCH -> "Voir la PR fusionnée"
        AppLanguage.GERMAN -> "Zusammengeführte PR ansehen"
        AppLanguage.RUSSIAN -> "Открыть слитый PR"
        AppLanguage.JAPANESE -> "マージされたPRを表示"
        AppLanguage.KOREAN -> "병합된 PR 보기"
    }

    val moduleMarketDirectPush: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "维护者直接推送"
        AppLanguage.ENGLISH -> "Maintainer direct push"
        AppLanguage.ARABIC -> "دفع مباشر من المسؤول"
        AppLanguage.PORTUGUESE -> "Push direto do mantenedor"
        AppLanguage.SPANISH -> "Push directo del mantenedor"
        AppLanguage.FRENCH -> "Push direct du mainteneur"
        AppLanguage.GERMAN -> "Direkter Push des Maintainers"
        AppLanguage.RUSSIAN -> "Прямой push мейнтейнера"
        AppLanguage.JAPANESE -> "メンテナーの直接プッシュ"
        AppLanguage.KOREAN -> "유지관리자 직접 푸시"
    }

    val moduleMarketPrNumber: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "PR #%d"
        AppLanguage.ENGLISH -> "PR #%d"
        AppLanguage.ARABIC -> "PR رقم %d"
        AppLanguage.PORTUGUESE -> "PR #%d"
        AppLanguage.SPANISH -> "PR #%d"
        AppLanguage.FRENCH -> "PR #%d"
        AppLanguage.GERMAN -> "PR #%d"
        AppLanguage.RUSSIAN -> "PR #%d"
        AppLanguage.JAPANESE -> "PR #%d"
        AppLanguage.KOREAN -> "PR #%d"
    }

    val moduleMarketTimeJustNow: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "刚刚"
        AppLanguage.ENGLISH -> "just now"
        AppLanguage.ARABIC -> "الآن"
        AppLanguage.PORTUGUESE -> "agora mesmo"
        AppLanguage.SPANISH -> "justo ahora"
        AppLanguage.FRENCH -> "à l'instant"
        AppLanguage.GERMAN -> "gerade eben"
        AppLanguage.RUSSIAN -> "только что"
        AppLanguage.JAPANESE -> "たった今"
        AppLanguage.KOREAN -> "방금"
    }

    val moduleMarketTimeMinutesAgo: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "%d 分钟前"
        AppLanguage.ENGLISH -> "%d min ago"
        AppLanguage.ARABIC -> "قبل %d دقيقة"
        AppLanguage.PORTUGUESE -> "%d min atrás"
        AppLanguage.SPANISH -> "hace %d min"
        AppLanguage.FRENCH -> "il y a %d min"
        AppLanguage.GERMAN -> "vor %d Min."
        AppLanguage.RUSSIAN -> "%d мин назад"
        AppLanguage.JAPANESE -> "%d分前"
        AppLanguage.KOREAN -> "%d분 전"
    }

    val moduleMarketTimeHoursAgo: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "%d 小时前"
        AppLanguage.ENGLISH -> "%d h ago"
        AppLanguage.ARABIC -> "قبل %d ساعة"
        AppLanguage.PORTUGUESE -> "%d h atrás"
        AppLanguage.SPANISH -> "hace %d h"
        AppLanguage.FRENCH -> "il y a %d h"
        AppLanguage.GERMAN -> "vor %d Std."
        AppLanguage.RUSSIAN -> "%d ч назад"
        AppLanguage.JAPANESE -> "%d時間前"
        AppLanguage.KOREAN -> "%d시간 전"
    }

    val moduleMarketTimeDaysAgo: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "%d 天前"
        AppLanguage.ENGLISH -> "%d d ago"
        AppLanguage.ARABIC -> "قبل %d يوم"
        AppLanguage.PORTUGUESE -> "%d d atrás"
        AppLanguage.SPANISH -> "hace %d d"
        AppLanguage.FRENCH -> "il y a %d j"
        AppLanguage.GERMAN -> "vor %d Tg."
        AppLanguage.RUSSIAN -> "%d дн назад"
        AppLanguage.JAPANESE -> "%d日前"
        AppLanguage.KOREAN -> "%d일 전"
    }

    val moduleMarketTimeMonthsAgo: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "%d 个月前"
        AppLanguage.ENGLISH -> "%d mo ago"
        AppLanguage.ARABIC -> "قبل %d شهر"
        AppLanguage.PORTUGUESE -> "%d meses atrás"
        AppLanguage.SPANISH -> "hace %d meses"
        AppLanguage.FRENCH -> "il y a %d mois"
        AppLanguage.GERMAN -> "vor %d Mon."
        AppLanguage.RUSSIAN -> "%d мес назад"
        AppLanguage.JAPANESE -> "%dヶ月前"
        AppLanguage.KOREAN -> "%d개월 전"
    }

    val moduleMarketTimeYearsAgo: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "%d 年前"
        AppLanguage.ENGLISH -> "%d y ago"
        AppLanguage.ARABIC -> "قبل %d سنة"
        AppLanguage.PORTUGUESE -> "%d anos atrás"
        AppLanguage.SPANISH -> "hace %d años"
        AppLanguage.FRENCH -> "il y a %d ans"
        AppLanguage.GERMAN -> "vor %d J."
        AppLanguage.RUSSIAN -> "%d г назад"
        AppLanguage.JAPANESE -> "%d年前"
        AppLanguage.KOREAN -> "%d년 전"
    }

    val moduleMarketGuideOpenRepo: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "打开仓库与完整指南"
        AppLanguage.ENGLISH -> "Open repo & full guide"
        AppLanguage.ARABIC -> "افتح المستودع والدليل الكامل"
        AppLanguage.PORTUGUESE -> "Abrir repositório e guia completo"
        AppLanguage.SPANISH -> "Abrir repositorio y guía completa"
        AppLanguage.FRENCH -> "Ouvrir le dépôt et le guide complet"
        AppLanguage.GERMAN -> "Repo und vollständigen Leitfaden öffnen"
        AppLanguage.RUSSIAN -> "Открыть репозиторий и полное руководство"
        AppLanguage.JAPANESE -> "リポジトリと完全なガイドを開く"
        AppLanguage.KOREAN -> "저장소 및 전체 가이드 열기"
    }

    val moduleMarketGuideEmptyCta: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "还没有社区模块——成为第一个贡献者吧。"
        AppLanguage.ENGLISH -> "No community modules yet — be the first to contribute."
        AppLanguage.ARABIC -> "لا توجد وحدات مجتمعية بعد — كن أول مساهم."
        AppLanguage.PORTUGUESE -> "Ainda não há módulos da comunidade — seja o primeiro a contribuir."
        AppLanguage.SPANISH -> "Aún no hay módulos de la comunidad — sé el primero en contribuir."
        AppLanguage.FRENCH -> "Pas encore de modules communautaires — soyez le premier à contribuer."
        AppLanguage.GERMAN -> "Noch keine Community-Module — seien Sie der erste Mitwirkende."
        AppLanguage.RUSSIAN -> "Сообщественных модулей пока нет — станьте первым участником."
        AppLanguage.JAPANESE -> "まだコミュニティモジュールはありません — 最初の貢献者になりましょう。"
        AppLanguage.KOREAN -> "아직 커뮤니티 모듈이 없습니다 — 첫 번째 기여자가 되세요."
    }

    val imageFile: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "图片文件（不支持预览）"
        AppLanguage.ENGLISH -> "Image file (preview not supported)"
        AppLanguage.ARABIC -> "ملف صورة (المعاينة غير مدعومة)"
        AppLanguage.PORTUGUESE -> "Arquivo de imagem (pré-visualização não suportada)"
        AppLanguage.SPANISH -> "Archivo de imagen (vista previa no soportada)"
        AppLanguage.FRENCH -> "Fichier image (aperçu non pris en charge)"
        AppLanguage.GERMAN -> "Bilddatei (Vorschau nicht unterstützt)"
        AppLanguage.RUSSIAN -> "Файл изображения (предпросмотр не поддерживается)"
        AppLanguage.JAPANESE -> "画像ファイル(プレビュー非対応)"
        AppLanguage.KOREAN -> "이미지 파일(미리보기 미지원)"
    }

    val binaryFile: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "二进制文件（不支持预览）"
        AppLanguage.ENGLISH -> "Binary file (preview not supported)"
        AppLanguage.ARABIC -> "ملف ثنائي (المعاينة غير مدعومة)"
        AppLanguage.PORTUGUESE -> "Arquivo binário (pré-visualização não suportada)"
        AppLanguage.SPANISH -> "Archivo binario (vista previa no soportada)"
        AppLanguage.FRENCH -> "Fichier binaire (aperçu non pris en charge)"
        AppLanguage.GERMAN -> "Binärdatei (Vorschau nicht unterstützt)"
        AppLanguage.RUSSIAN -> "Бинарный файл (предпросмотр не поддерживается)"
        AppLanguage.JAPANESE -> "バイナリファイル(プレビュー非対応)"
        AppLanguage.KOREAN -> "바이너리 파일(미리보기 미지원)"
    }

    val hostsAdBlock: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Hosts 广告拦截"
        AppLanguage.ENGLISH -> "Hosts Ad Blocking"
        AppLanguage.ARABIC -> "حظر الإعلانات عبر Hosts"
        AppLanguage.PORTUGUESE -> "Bloqueio de anúncios via Hosts"
        AppLanguage.SPANISH -> "Bloqueo de anuncios por Hosts"
        AppLanguage.FRENCH -> "Blocage de publicités via Hosts"
        AppLanguage.GERMAN -> "Werbeblockung per Hosts"
        AppLanguage.RUSSIAN -> "Блокировка рекламы через Hosts"
        AppLanguage.JAPANESE -> "Hosts広告ブロック"
        AppLanguage.KOREAN -> "Hosts 광고 차단"
    }

    val hostsAdBlockSubtitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "使用 hosts 文件拦截广告域名"
        AppLanguage.ENGLISH -> "Block ad domains using hosts files"
        AppLanguage.ARABIC -> "حظر نطاقات الإعلانات باستخدام ملفات hosts"
        AppLanguage.PORTUGUESE -> "Bloquear domínios de anúncios usando arquivos hosts"
        AppLanguage.SPANISH -> "Bloquear dominios de anuncios usando archivos hosts"
        AppLanguage.FRENCH -> "Bloquer les domaines publicitaires avec des fichiers hosts"
        AppLanguage.GERMAN -> "Werbedomänen mittels hosts-Dateien blockieren"
        AppLanguage.RUSSIAN -> "Блокировка рекламных доменов через файлы hosts"
        AppLanguage.JAPANESE -> "hostsファイルで広告ドメインをブロック"
        AppLanguage.KOREAN -> "hosts 파일로 광고 도메인 차단"
    }

    val menuHostsAdBlock: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Hosts 拦截"
        AppLanguage.ENGLISH -> "Hosts Blocking"
        AppLanguage.ARABIC -> "حظر Hosts"
        AppLanguage.PORTUGUESE -> "Bloqueio Hosts"
        AppLanguage.SPANISH -> "Bloqueo Hosts"
        AppLanguage.FRENCH -> "Blocage Hosts"
        AppLanguage.GERMAN -> "Hosts-Blockung"
        AppLanguage.RUSSIAN -> "Блокировка Hosts"
        AppLanguage.JAPANESE -> "Hostsブロック"
        AppLanguage.KOREAN -> "Hosts 차단"
    }

    val hostsRulesCount: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "%d 条规则"
        AppLanguage.ENGLISH -> "%d rules"
        AppLanguage.ARABIC -> "%d قواعد"
        AppLanguage.PORTUGUESE -> "%d regras"
        AppLanguage.SPANISH -> "%d reglas"
        AppLanguage.FRENCH -> "%d règles"
        AppLanguage.GERMAN -> "%d Regeln"
        AppLanguage.RUSSIAN -> "%d правил"
        AppLanguage.JAPANESE -> "%d件のルール"
        AppLanguage.KOREAN -> "%d개 규칙"
    }

    val importFromUrl: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "从 URL 导入"
        AppLanguage.ENGLISH -> "Import from URL"
        AppLanguage.ARABIC -> "استيراد من URL"
        AppLanguage.PORTUGUESE -> "Importar de URL"
        AppLanguage.SPANISH -> "Importar desde URL"
        AppLanguage.FRENCH -> "Importer depuis une URL"
        AppLanguage.GERMAN -> "Aus URL importieren"
        AppLanguage.RUSSIAN -> "Импорт из URL"
        AppLanguage.JAPANESE -> "URLからインポート"
        AppLanguage.KOREAN -> "URL에서 가져오기"
    }

    val popularHostsSources: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "常用 Hosts 源"
        AppLanguage.ENGLISH -> "Popular Hosts Sources"
        AppLanguage.ARABIC -> "مصادر Hosts الشائعة"
        AppLanguage.PORTUGUESE -> "Fontes Hosts populares"
        AppLanguage.SPANISH -> "Fuentes Hosts populares"
        AppLanguage.FRENCH -> "Sources Hosts populaires"
        AppLanguage.GERMAN -> "Beliebte Hosts-Quellen"
        AppLanguage.RUSSIAN -> "Популярные источники Hosts"
        AppLanguage.JAPANESE -> "人気のHostsソース"
        AppLanguage.KOREAN -> "인기 Hosts 소스"
    }

    val customHostsSources: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自定义源"
        AppLanguage.ENGLISH -> "Custom Sources"
        AppLanguage.ARABIC -> "مصادر مخصصة"
        AppLanguage.PORTUGUESE -> "Fontes Personalizadas"
        AppLanguage.SPANISH -> "Fuentes Personalizadas"
        AppLanguage.FRENCH -> "Sources Personnalisées"
        AppLanguage.GERMAN -> "Eigene Quellen"
        AppLanguage.RUSSIAN -> "Пользовательские источники"
        AppLanguage.JAPANESE -> "カスタムソース"
        AppLanguage.KOREAN -> "사용자 지정 소스"
    }

    val importHostsUrl: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Hosts 文件 URL"
        AppLanguage.ENGLISH -> "Hosts File URL"
        AppLanguage.ARABIC -> "رابط ملف Hosts"
        AppLanguage.PORTUGUESE -> "URL do arquivo Hosts"
        AppLanguage.SPANISH -> "URL del archivo Hosts"
        AppLanguage.FRENCH -> "URL du fichier Hosts"
        AppLanguage.GERMAN -> "Hosts-Datei-URL"
        AppLanguage.RUSSIAN -> "URL файла Hosts"
        AppLanguage.JAPANESE -> "HostsファイルのURL"
        AppLanguage.KOREAN -> "Hosts 파일 URL"
    }

    val importHostsUrlHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "输入 hosts 文件的 URL 地址"
        AppLanguage.ENGLISH -> "Enter URL of hosts file"
        AppLanguage.ARABIC -> "أدخل رابط ملف hosts"
        AppLanguage.PORTUGUESE -> "Insira a URL do arquivo hosts"
        AppLanguage.SPANISH -> "Introduce la URL del archivo hosts"
        AppLanguage.FRENCH -> "Saisissez l'URL du fichier hosts"
        AppLanguage.GERMAN -> "URL der hosts-Datei eingeben"
        AppLanguage.RUSSIAN -> "Введите URL файла hosts"
        AppLanguage.JAPANESE -> "hostsファイルのURLを入力"
        AppLanguage.KOREAN -> "hosts 파일의 URL을 입력하세요"
    }

    val importHostsSuccess: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "成功导入 %d 条规则"
        AppLanguage.ENGLISH -> "Successfully imported %d rules"
        AppLanguage.ARABIC -> "تم استيراد %d قاعدة بنجاح"
        AppLanguage.PORTUGUESE -> "%d regras importadas com sucesso"
        AppLanguage.SPANISH -> "%d reglas importadas con éxito"
        AppLanguage.FRENCH -> "%d règles importées avec succès"
        AppLanguage.GERMAN -> "%d Regeln erfolgreich importiert"
        AppLanguage.RUSSIAN -> "Успешно импортировано %d правил"
        AppLanguage.JAPANESE -> "%d件のルールをインポートしました"
        AppLanguage.KOREAN -> "%d개 규칙을 가져왔습니다"
    }

    val importHostsFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "导入失败"
        AppLanguage.ENGLISH -> "Import failed"
        AppLanguage.ARABIC -> "فشل الاستيراد"
        AppLanguage.PORTUGUESE -> "Falha na importação"
        AppLanguage.SPANISH -> "Importación fallida"
        AppLanguage.FRENCH -> "Échec de l'importation"
        AppLanguage.GERMAN -> "Import fehlgeschlagen"
        AppLanguage.RUSSIAN -> "Ошибка импорта"
        AppLanguage.JAPANESE -> "インポート失敗"
        AppLanguage.KOREAN -> "가져오기 실패"
    }

    val clearHostsRules: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "清空所有 Hosts 规则"
        AppLanguage.ENGLISH -> "Clear All Hosts Rules"
        AppLanguage.ARABIC -> "مسح جميع قواعد Hosts"
        AppLanguage.PORTUGUESE -> "Limpar todas as regras Hosts"
        AppLanguage.SPANISH -> "Borrar todas las reglas Hosts"
        AppLanguage.FRENCH -> "Effacer toutes les règles Hosts"
        AppLanguage.GERMAN -> "Alle Hosts-Regeln löschen"
        AppLanguage.RUSSIAN -> "Очистить все правила Hosts"
        AppLanguage.JAPANESE -> "すべてのHostsルールを消去"
        AppLanguage.KOREAN -> "모든 Hosts 규칙 삭제"
    }

    val clearHostsConfirm: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "确定要清空所有 hosts 规则吗？"
        AppLanguage.ENGLISH -> "Are you sure you want to clear all hosts rules?"
        AppLanguage.ARABIC -> "هل أنت متأكد من مسح جميع قواعد hosts؟"
        AppLanguage.PORTUGUESE -> "Tem certeza de que deseja limpar todas as regras hosts?"
        AppLanguage.SPANISH -> "¿Seguro que quieres borrar todas las reglas hosts?"
        AppLanguage.FRENCH -> "Voulez-vous vraiment effacer toutes les règles hosts ?"
        AppLanguage.GERMAN -> "Möchten Sie wirklich alle hosts-Regeln löschen?"
        AppLanguage.RUSSIAN -> "Вы уверены, что хотите очистить все правила hosts?"
        AppLanguage.JAPANESE -> "すべてのhostsルールを消去してもよろしいですか？"
        AppLanguage.KOREAN -> "모든 hosts 규칙을 삭제하시겠습니까?"
    }

    val hostsCleared: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Hosts 规则已清空"
        AppLanguage.ENGLISH -> "Hosts rules cleared"
        AppLanguage.ARABIC -> "تم مسح قواعد Hosts"
        AppLanguage.PORTUGUESE -> "Regras Hosts limpas"
        AppLanguage.SPANISH -> "Reglas Hosts borradas"
        AppLanguage.FRENCH -> "Règles Hosts effacées"
        AppLanguage.GERMAN -> "Hosts-Regeln gelöscht"
        AppLanguage.RUSSIAN -> "Правила Hosts очищены"
        AppLanguage.JAPANESE -> "Hostsルールを消去しました"
        AppLanguage.KOREAN -> "Hosts 규칙이 삭제되었습니다"
    }

    val hostsBlockingDescription: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "通过导入 hosts 文件来拦截广告域名\n支持标准 hosts 格式和 AdBlock 格式"
        AppLanguage.ENGLISH -> "Block ad domains by importing hosts files\nSupports standard hosts format and AdBlock format"
        AppLanguage.ARABIC -> "حظر نطاقات الإعلانات عن طريق استيراد ملفات hosts\nيدعم تنسيق hosts القياسي وتنسيق AdBlock"
        AppLanguage.PORTUGUESE -> "Bloqueie domínios de anúncios importando arquivos hosts\nSuporta formato hosts padrão e formato AdBlock"
        AppLanguage.SPANISH -> "Bloquea dominios de anuncios importando archivos hosts\nSoporta formato hosts estándar y formato AdBlock"
        AppLanguage.FRENCH -> "Bloquez les domaines publicitaires en important des fichiers hosts\nPrend en charge le format hosts standard et le format AdBlock"
        AppLanguage.GERMAN -> "Werbedomänen durch Import von hosts-Dateien blockieren\nUnterstützt Standard-hosts-Format und AdBlock-Format"
        AppLanguage.RUSSIAN -> "Блокировка рекламных доменов путём импорта файлов hosts\nПоддерживает стандартный формат hosts и формат AdBlock"
        AppLanguage.JAPANESE -> "hostsファイルをインポートして広告ドメインをブロック\n標準hosts形式とAdBlock形式に対応"
        AppLanguage.KOREAN -> "hosts 파일을 가져와 광고 도메인 차단\n표준 hosts 형식과 AdBlock 형식 지원"
    }

    val downloadAndImport: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下载并导入"
        AppLanguage.ENGLISH -> "Download & Import"
        AppLanguage.ARABIC -> "تنزيل واستيراد"
        AppLanguage.PORTUGUESE -> "Baixar e importar"
        AppLanguage.SPANISH -> "Descargar e importar"
        AppLanguage.FRENCH -> "Télécharger et importer"
        AppLanguage.GERMAN -> "Herunterladen und importieren"
        AppLanguage.RUSSIAN -> "Скачать и импортировать"
        AppLanguage.JAPANESE -> "ダウンロードしてインポート"
        AppLanguage.KOREAN -> "다운로드 후 가져오기"
    }

    val hostsSourceDownloaded: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已下载"
        AppLanguage.ENGLISH -> "Downloaded"
        AppLanguage.ARABIC -> "تم التنزيل"
        AppLanguage.PORTUGUESE -> "Baixado"
        AppLanguage.SPANISH -> "Descargado"
        AppLanguage.FRENCH -> "Téléchargé"
        AppLanguage.GERMAN -> "Heruntergeladen"
        AppLanguage.RUSSIAN -> "Загружено"
        AppLanguage.JAPANESE -> "ダウンロード済み"
        AppLanguage.KOREAN -> "다운로드됨"
    }

    val deleteHostsSource: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "删除 Hosts 源"
        AppLanguage.ENGLISH -> "Delete Hosts Source"
        AppLanguage.ARABIC -> "حذف مصدر Hosts"
        AppLanguage.PORTUGUESE -> "Excluir fonte Hosts"
        AppLanguage.SPANISH -> "Eliminar fuente Hosts"
        AppLanguage.FRENCH -> "Supprimer la source Hosts"
        AppLanguage.GERMAN -> "Hosts-Quelle löschen"
        AppLanguage.RUSSIAN -> "Удалить источник Hosts"
        AppLanguage.JAPANESE -> "Hostsソースを削除"
        AppLanguage.KOREAN -> "Hosts 소스 삭제"
    }

    val deleteHostsSourceConfirm: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "确定要删除 \"%s\" 及其规则吗？"
        AppLanguage.ENGLISH -> "Are you sure you want to delete \"%s\" and its rules?"
        AppLanguage.ARABIC -> "هل أنت متأكد من حذف \"%s\" وقواعده؟"
        AppLanguage.PORTUGUESE -> "Tem certeza de que deseja excluir \"%s\" e suas regras?"
        AppLanguage.SPANISH -> "¿Seguro que quieres eliminar \"%s\" y sus reglas?"
        AppLanguage.FRENCH -> "Voulez-vous vraiment supprimer \"%s\" et ses règles ?"
        AppLanguage.GERMAN -> "Möchten Sie wirklich \"%s\" und seine Regeln löschen?"
        AppLanguage.RUSSIAN -> "Вы уверены, что хотите удалить \"%s\" и его правила?"
        AppLanguage.JAPANESE -> "\"%s\"とそのルールを削除してもよろしいですか？"
        AppLanguage.KOREAN -> "\"%s\"와(과) 그 규칙을 삭제하시겠습니까?"
    }

    val hostsSourceDeleted: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Hosts 源已删除"
        AppLanguage.ENGLISH -> "Hosts source deleted"
        AppLanguage.ARABIC -> "تم حذف مصدر Hosts"
        AppLanguage.PORTUGUESE -> "Fonte Hosts excluída"
        AppLanguage.SPANISH -> "Fuente Hosts eliminada"
        AppLanguage.FRENCH -> "Source Hosts supprimée"
        AppLanguage.GERMAN -> "Hosts-Quelle gelöscht"
        AppLanguage.RUSSIAN -> "Источник Hosts удалён"
        AppLanguage.JAPANESE -> "Hostsソースを削除しました"
        AppLanguage.KOREAN -> "Hosts 소스가 삭제되었습니다"
    }

    val downloading: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下载中"
        AppLanguage.ENGLISH -> "Downloading"
        AppLanguage.ARABIC -> "جاري التنزيل"
        AppLanguage.PORTUGUESE -> "Baixando"
        AppLanguage.SPANISH -> "Descargando"
        AppLanguage.FRENCH -> "Téléchargement"
        AppLanguage.GERMAN -> "Wird heruntergeladen"
        AppLanguage.RUSSIAN -> "Скачивание"
        AppLanguage.JAPANESE -> "ダウンロード中"
        AppLanguage.KOREAN -> "다운로드 중"
    }

    val downloadCanceled: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下载已取消"
        AppLanguage.ENGLISH -> "Download canceled"
        AppLanguage.ARABIC -> "تم إلغاء التنزيل"
        AppLanguage.PORTUGUESE -> "Download cancelado"
        AppLanguage.SPANISH -> "Descarga cancelada"
        AppLanguage.FRENCH -> "Téléchargement annulé"
        AppLanguage.GERMAN -> "Download abgebrochen"
        AppLanguage.RUSSIAN -> "Скачивание отменено"
        AppLanguage.JAPANESE -> "ダウンロードがキャンセルされました"
        AppLanguage.KOREAN -> "다운로드 취소됨"
    }

    val hostsSourcesSummary: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已下载 %d 个规则源"
        AppLanguage.ENGLISH -> "%d filter lists downloaded"
        AppLanguage.ARABIC -> "تم تنزيل %d قائمة تصفية"
        AppLanguage.PORTUGUESE -> "%d listas de filtro baixadas"
        AppLanguage.SPANISH -> "%d listas de filtros descargadas"
        AppLanguage.FRENCH -> "%d listes de filtres téléchargées"
        AppLanguage.GERMAN -> "%d Filterlisten heruntergeladen"
        AppLanguage.RUSSIAN -> "Скачано %d списков фильтров"
        AppLanguage.JAPANESE -> "%d件のフィルターリストをダウンロード済み"
        AppLanguage.KOREAN -> "%d개 필터 목록 다운로드됨"
    }

    val hostsSearchHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "搜索规则源…"
        AppLanguage.ENGLISH -> "Search filter lists…"
        AppLanguage.ARABIC -> "ابحث في قوائم التصفية…"
        AppLanguage.PORTUGUESE -> "Pesquisar listas de filtro…"
        AppLanguage.SPANISH -> "Buscar listas de filtros…"
        AppLanguage.FRENCH -> "Rechercher des listes de filtres…"
        AppLanguage.GERMAN -> "Filterlisten suchen…"
        AppLanguage.RUSSIAN -> "Поиск списков фильтров…"
        AppLanguage.JAPANESE -> "フィルターリストを検索…"
        AppLanguage.KOREAN -> "필터 목록 검색…"
    }
    val hostsFilterDownloaded: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已下载"
        AppLanguage.ENGLISH -> "Downloaded"
        AppLanguage.ARABIC -> "تم التنزيل"
        AppLanguage.PORTUGUESE -> "Baixadas"
        AppLanguage.SPANISH -> "Descargadas"
        AppLanguage.FRENCH -> "Téléchargées"
        AppLanguage.GERMAN -> "Heruntergeladen"
        AppLanguage.RUSSIAN -> "Скачанные"
        AppLanguage.JAPANESE -> "ダウンロード済み"
        AppLanguage.KOREAN -> "다운로드됨"
    }
    val hostsFilterNotDownloaded: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "未下载"
        AppLanguage.ENGLISH -> "Not downloaded"
        AppLanguage.ARABIC -> "غير مُنزّل"
        AppLanguage.PORTUGUESE -> "Não baixadas"
        AppLanguage.SPANISH -> "No descargadas"
        AppLanguage.FRENCH -> "Non téléchargées"
        AppLanguage.GERMAN -> "Nicht heruntergeladen"
        AppLanguage.RUSSIAN -> "Не скачаны"
        AppLanguage.JAPANESE -> "未ダウンロード"
        AppLanguage.KOREAN -> "미다운로드"
    }
    val hostsNoMatch: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "没有匹配的规则源"
        AppLanguage.ENGLISH -> "No matching filter lists"
        AppLanguage.ARABIC -> "لا توجد قوائم تصفية مطابقة"
        AppLanguage.PORTUGUESE -> "Nenhuma lista de filtro correspondente"
        AppLanguage.SPANISH -> "No hay listas de filtros coincidentes"
        AppLanguage.FRENCH -> "Aucune liste de filtres correspondante"
        AppLanguage.GERMAN -> "Keine passenden Filterlisten"
        AppLanguage.RUSSIAN -> "Нет подходящих списков фильтров"
        AppLanguage.JAPANESE -> "一致するフィルターリストがありません"
        AppLanguage.KOREAN -> "일치하는 필터 목록이 없습니다"
    }
    val hostsNoMatchHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "试试换个关键词，或切换筛选条件。"
        AppLanguage.ENGLISH -> "Try another keyword or change the filter."
        AppLanguage.ARABIC -> "جرّب كلمة أخرى أو غيّر عامل التصفية."
        AppLanguage.PORTUGUESE -> "Tente outra palavra-chave ou mude o filtro."
        AppLanguage.SPANISH -> "Prueba otra palabra clave o cambia el filtro."
        AppLanguage.FRENCH -> "Essayez un autre mot-clé ou changez le filtre."
        AppLanguage.GERMAN -> "Versuchen Sie ein anderes Stichwort oder ändern Sie den Filter."
        AppLanguage.RUSSIAN -> "Попробуйте другое ключевое слово или смените фильтр."
        AppLanguage.JAPANESE -> "別のキーワードを試すか、フィルタを切り替えてください。"
        AppLanguage.KOREAN -> "다른 키워드를 사용하거나 필터를 바꿔 보세요."
    }
    val hostsSourceEnabled: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已启用"
        AppLanguage.ENGLISH -> "Enabled"
        AppLanguage.ARABIC -> "مُفعّل"
        AppLanguage.PORTUGUESE -> "Ativado"
        AppLanguage.SPANISH -> "Activado"
        AppLanguage.FRENCH -> "Activé"
        AppLanguage.GERMAN -> "Aktiviert"
        AppLanguage.RUSSIAN -> "Включено"
        AppLanguage.JAPANESE -> "有効"
        AppLanguage.KOREAN -> "사용 중"
    }
    val hostsSourceDisabled: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已停用"
        AppLanguage.ENGLISH -> "Disabled"
        AppLanguage.ARABIC -> "مُعطّل"
        AppLanguage.PORTUGUESE -> "Desativado"
        AppLanguage.SPANISH -> "Desactivado"
        AppLanguage.FRENCH -> "Désactivé"
        AppLanguage.GERMAN -> "Deaktiviert"
        AppLanguage.RUSSIAN -> "Отключено"
        AppLanguage.JAPANESE -> "無効"
        AppLanguage.KOREAN -> "사용 안 함"
    }
    val hostsSourceRuleCount: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "%d 条规则"
        AppLanguage.ENGLISH -> "%d rules"
        AppLanguage.ARABIC -> "%d قواعد"
        AppLanguage.PORTUGUESE -> "%d regras"
        AppLanguage.SPANISH -> "%d reglas"
        AppLanguage.FRENCH -> "%d règles"
        AppLanguage.GERMAN -> "%d Regeln"
        AppLanguage.RUSSIAN -> "%d правил"
        AppLanguage.JAPANESE -> "%d件のルール"
        AppLanguage.KOREAN -> "%d개 규칙"
    }
    val hostsActiveRulesCount: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "当前生效 %d 条"
        AppLanguage.ENGLISH -> "%d rules active now"
        AppLanguage.ARABIC -> "%d قاعدة مفعّلة الآن"
        AppLanguage.PORTUGUESE -> "%d regras ativas agora"
        AppLanguage.SPANISH -> "%d reglas activas ahora"
        AppLanguage.FRENCH -> "%d règles actives maintenant"
        AppLanguage.GERMAN -> "%d Regeln derzeit aktiv"
        AppLanguage.RUSSIAN -> "Сейчас активно %d правил"
        AppLanguage.JAPANESE -> "現在有効 %d 件"
        AppLanguage.KOREAN -> "현재 활성 %d개"
    }
    val hostsSourcesEnabledSummary: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已启用 %d / %d 个规则源"
        AppLanguage.ENGLISH -> "%d / %d filter lists enabled"
        AppLanguage.ARABIC -> "%d / %d قوائم تصفية مفعّلة"
        AppLanguage.PORTUGUESE -> "%d / %d listas de filtro ativadas"
        AppLanguage.SPANISH -> "%d / %d listas de filtros activadas"
        AppLanguage.FRENCH -> "%d / %d listes de filtres activées"
        AppLanguage.GERMAN -> "%d / %d Filterlisten aktiviert"
        AppLanguage.RUSSIAN -> "Включено %d / %d списков фильтров"
        AppLanguage.JAPANESE -> "%d / %d 件のフィルターリストが有効"
        AppLanguage.KOREAN -> "필터 목록 %d / %d개 사용 중"
    }

    val galleryApp: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "媒体画廊"
        AppLanguage.ENGLISH -> "Media Gallery"
        AppLanguage.ARABIC -> "معرض الوسائط"
        AppLanguage.PORTUGUESE -> "Galeria de Mídia"
        AppLanguage.SPANISH -> "Galería multimedia"
        AppLanguage.FRENCH -> "Galerie multimédia"
        AppLanguage.GERMAN -> "Mediengalerie"
        AppLanguage.RUSSIAN -> "Галерея медиа"
        AppLanguage.JAPANESE -> "メディアギャラリー"
        AppLanguage.KOREAN -> "미디어 갤러리"
    }

    val galleryCreateTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "创建媒体画廊"
        AppLanguage.ENGLISH -> "Create Media Gallery"
        AppLanguage.ARABIC -> "إنشاء معرض الوسائط"
        AppLanguage.PORTUGUESE -> "Criar galeria de mídia"
        AppLanguage.SPANISH -> "Crear galería multimedia"
        AppLanguage.FRENCH -> "Créer une galerie multimédia"
        AppLanguage.GERMAN -> "Mediengalerie erstellen"
        AppLanguage.RUSSIAN -> "Создать галерею медиа"
        AppLanguage.JAPANESE -> "メディアギャラリーを作成"
        AppLanguage.KOREAN -> "미디어 갤러리 만들기"
    }

    val galleryTabMedia: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "媒体"
        AppLanguage.ENGLISH -> "Media"
        AppLanguage.ARABIC -> "الوسائط"
        AppLanguage.PORTUGUESE -> "Mídia"
        AppLanguage.SPANISH -> "Multimedia"
        AppLanguage.FRENCH -> "Médias"
        AppLanguage.GERMAN -> "Medien"
        AppLanguage.RUSSIAN -> "Медиа"
        AppLanguage.JAPANESE -> "メディア"
        AppLanguage.KOREAN -> "미디어"
    }

    val galleryTabPlayback: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "播放"
        AppLanguage.ENGLISH -> "Playback"
        AppLanguage.ARABIC -> "التشغيل"
        AppLanguage.PORTUGUESE -> "Reprodução"
        AppLanguage.SPANISH -> "Reproducción"
        AppLanguage.FRENCH -> "Lecture"
        AppLanguage.GERMAN -> "Wiedergabe"
        AppLanguage.RUSSIAN -> "Воспроизведение"
        AppLanguage.JAPANESE -> "再生"
        AppLanguage.KOREAN -> "재생"
    }

    val galleryTabDisplay: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "显示"
        AppLanguage.ENGLISH -> "Display"
        AppLanguage.ARABIC -> "العرض"
        AppLanguage.PORTUGUESE -> "Exibição"
        AppLanguage.SPANISH -> "Visualización"
        AppLanguage.FRENCH -> "Affichage"
        AppLanguage.GERMAN -> "Anzeige"
        AppLanguage.RUSSIAN -> "Отображение"
        AppLanguage.JAPANESE -> "表示"
        AppLanguage.KOREAN -> "표시"
    }

    val galleryCategories: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "分类管理"
        AppLanguage.ENGLISH -> "Categories"
        AppLanguage.ARABIC -> "الفئات"
        AppLanguage.PORTUGUESE -> "Categorias"
        AppLanguage.SPANISH -> "Categorías"
        AppLanguage.FRENCH -> "Catégories"
        AppLanguage.GERMAN -> "Kategorien"
        AppLanguage.RUSSIAN -> "Категории"
        AppLanguage.JAPANESE -> "カテゴリ"
        AppLanguage.KOREAN -> "카테고리"
    }

    val galleryMediaList: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "媒体列表"
        AppLanguage.ENGLISH -> "Media List"
        AppLanguage.ARABIC -> "قائمة الوسائط"
        AppLanguage.PORTUGUESE -> "Lista de mídia"
        AppLanguage.SPANISH -> "Lista multimedia"
        AppLanguage.FRENCH -> "Liste des médias"
        AppLanguage.GERMAN -> "Medienliste"
        AppLanguage.RUSSIAN -> "Список медиа"
        AppLanguage.JAPANESE -> "メディアリスト"
        AppLanguage.KOREAN -> "미디어 목록"
    }

    val galleryItemCount: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "项"
        AppLanguage.ENGLISH -> "items"
        AppLanguage.ARABIC -> "عناصر"
        AppLanguage.PORTUGUESE -> "itens"
        AppLanguage.SPANISH -> "elementos"
        AppLanguage.FRENCH -> "éléments"
        AppLanguage.GERMAN -> "Elemente"
        AppLanguage.RUSSIAN -> "эл."
        AppLanguage.JAPANESE -> "件"
        AppLanguage.KOREAN -> "개"
    }

    val galleryAddMedia: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "添加媒体"
        AppLanguage.ENGLISH -> "Add Media"
        AppLanguage.ARABIC -> "إضافة وسائط"
        AppLanguage.PORTUGUESE -> "Adicionar mídia"
        AppLanguage.SPANISH -> "Añadir multimedia"
        AppLanguage.FRENCH -> "Ajouter des médias"
        AppLanguage.GERMAN -> "Medien hinzufügen"
        AppLanguage.RUSSIAN -> "Добавить медиа"
        AppLanguage.JAPANESE -> "メディアを追加"
        AppLanguage.KOREAN -> "미디어 추가"
    }

    val galleryClickToAdd: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "点击添加图片或视频"
        AppLanguage.ENGLISH -> "Click to add images or videos"
        AppLanguage.ARABIC -> "انقر لإضافة صور أو فيديوهات"
        AppLanguage.PORTUGUESE -> "Clique para adicionar imagens ou vídeos"
        AppLanguage.SPANISH -> "Haz clic para añadir imágenes o vídeos"
        AppLanguage.FRENCH -> "Cliquez pour ajouter des images ou des vidéos"
        AppLanguage.GERMAN -> "Klicken Sie, um Bilder oder Videos hinzuzufügen"
        AppLanguage.RUSSIAN -> "Нажмите, чтобы добавить изображения или видео"
        AppLanguage.JAPANESE -> "クリックして画像または動画を追加"
        AppLanguage.KOREAN -> "클릭하여 이미지나 동영상 추가"
    }

    val gallerySupportTypes: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "支持 JPG, PNG, GIF, MP4, WebM 等格式"
        AppLanguage.ENGLISH -> "Supports JPG, PNG, GIF, MP4, WebM, etc."
        AppLanguage.ARABIC -> "يدعم JPG, PNG, GIF, MP4, WebM وغيرها"
        AppLanguage.PORTUGUESE -> "Suporta JPG, PNG, GIF, MP4, WebM, etc."
        AppLanguage.SPANISH -> "Soporta JPG, PNG, GIF, MP4, WebM, etc."
        AppLanguage.FRENCH -> "Prend en charge JPG, PNG, GIF, MP4, WebM, etc."
        AppLanguage.GERMAN -> "Unterstützt JPG, PNG, GIF, MP4, WebM usw."
        AppLanguage.RUSSIAN -> "Поддерживает JPG, PNG, GIF, MP4, WebM и т. д."
        AppLanguage.JAPANESE -> "JPG, PNG, GIF, MP4, WebMなどに対応"
        AppLanguage.KOREAN -> "JPG, PNG, GIF, MP4, WebM 등 지원"
    }

    val galleryImages: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "图片"
        AppLanguage.ENGLISH -> "Images"
        AppLanguage.ARABIC -> "صور"
        AppLanguage.PORTUGUESE -> "Imagens"
        AppLanguage.SPANISH -> "Imágenes"
        AppLanguage.FRENCH -> "Images"
        AppLanguage.GERMAN -> "Bilder"
        AppLanguage.RUSSIAN -> "Изображения"
        AppLanguage.JAPANESE -> "画像"
        AppLanguage.KOREAN -> "이미지"
    }

    val galleryVideos: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "视频"
        AppLanguage.ENGLISH -> "Videos"
        AppLanguage.ARABIC -> "فيديوهات"
        AppLanguage.PORTUGUESE -> "Vídeos"
        AppLanguage.SPANISH -> "Vídeos"
        AppLanguage.FRENCH -> "Vidéos"
        AppLanguage.GERMAN -> "Videos"
        AppLanguage.RUSSIAN -> "Видео"
        AppLanguage.JAPANESE -> "動画"
        AppLanguage.KOREAN -> "동영상"
    }

    val galleryEmpty: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "无媒体文件"
        AppLanguage.ENGLISH -> "No media files"
        AppLanguage.ARABIC -> "لا توجد ملفات وسائط"
        AppLanguage.PORTUGUESE -> "Nenhum arquivo de mídia"
        AppLanguage.SPANISH -> "Sin archivos multimedia"
        AppLanguage.FRENCH -> "Aucun fichier média"
        AppLanguage.GERMAN -> "Keine Mediendateien"
        AppLanguage.RUSSIAN -> "Нет медиафайлов"
        AppLanguage.JAPANESE -> "メディアファイルがありません"
        AppLanguage.KOREAN -> "미디어 파일 없음"
    }

    val galleryTotalSize: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "总大小"
        AppLanguage.ENGLISH -> "Total Size"
        AppLanguage.ARABIC -> "الحجم الكلي"
        AppLanguage.PORTUGUESE -> "Tamanho total"
        AppLanguage.SPANISH -> "Tamaño total"
        AppLanguage.FRENCH -> "Taille totale"
        AppLanguage.GERMAN -> "Gesamtgröße"
        AppLanguage.RUSSIAN -> "Общий размер"
        AppLanguage.JAPANESE -> "合計サイズ"
        AppLanguage.KOREAN -> "전체 크기"
    }

    val galleryPlayMode: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "播放模式"
        AppLanguage.ENGLISH -> "Play Mode"
        AppLanguage.ARABIC -> "وضع التشغيل"
        AppLanguage.PORTUGUESE -> "Modo de reprodução"
        AppLanguage.SPANISH -> "Modo de reproducción"
        AppLanguage.FRENCH -> "Mode de lecture"
        AppLanguage.GERMAN -> "Wiedergabemodus"
        AppLanguage.RUSSIAN -> "Режим воспроизведения"
        AppLanguage.JAPANESE -> "再生モード"
        AppLanguage.KOREAN -> "재생 모드"
    }

    val galleryModeSequential: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "顺序"
        AppLanguage.ENGLISH -> "Sequential"
        AppLanguage.ARABIC -> "تسلسلي"
        AppLanguage.PORTUGUESE -> "Sequencial"
        AppLanguage.SPANISH -> "Secuencial"
        AppLanguage.FRENCH -> "Séquentiel"
        AppLanguage.GERMAN -> "Sequentiell"
        AppLanguage.RUSSIAN -> "Последовательно"
        AppLanguage.JAPANESE -> "シーケンシャル"
        AppLanguage.KOREAN -> "순차"
    }

    val galleryModeShuffle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "随机"
        AppLanguage.ENGLISH -> "Shuffle"
        AppLanguage.ARABIC -> "عشوائي"
        AppLanguage.PORTUGUESE -> "Aleatório"
        AppLanguage.SPANISH -> "Aleatorio"
        AppLanguage.FRENCH -> "Aléatoire"
        AppLanguage.GERMAN -> "Zufällig"
        AppLanguage.RUSSIAN -> "Случайно"
        AppLanguage.JAPANESE -> "シャッフル"
        AppLanguage.KOREAN -> "셔플"
    }

    val galleryModeSingleLoop: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Single loop"
        AppLanguage.ENGLISH -> "Single Loop"
        AppLanguage.ARABIC -> "تكرار واحد"
        AppLanguage.PORTUGUESE -> "Repetição única"
        AppLanguage.SPANISH -> "Bucle único"
        AppLanguage.FRENCH -> "Boucle unique"
        AppLanguage.GERMAN -> "Einzelwiederholung"
        AppLanguage.RUSSIAN -> "Один цикл"
        AppLanguage.JAPANESE -> "シングルループ"
        AppLanguage.KOREAN -> "단일 반복"
    }

    val galleryImageSettings: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "图片播放设置"
        AppLanguage.ENGLISH -> "Image Playback Settings"
        AppLanguage.ARABIC -> "إعدادات تشغيل الصور"
        AppLanguage.PORTUGUESE -> "Configurações de reprodução de imagem"
        AppLanguage.SPANISH -> "Ajustes de reproducción de imágenes"
        AppLanguage.FRENCH -> "Paramètres de lecture des images"
        AppLanguage.GERMAN -> "Bildwiedergabeeinstellungen"
        AppLanguage.RUSSIAN -> "Настройки воспроизведения изображений"
        AppLanguage.JAPANESE -> "画像再生設定"
        AppLanguage.KOREAN -> "이미지 재생 설정"
    }

    val galleryImageInterval: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "图片播放间隔"
        AppLanguage.ENGLISH -> "Image Interval"
        AppLanguage.ARABIC -> "فترة الصورة"
        AppLanguage.PORTUGUESE -> "Intervalo de imagem"
        AppLanguage.SPANISH -> "Intervalo de imagen"
        AppLanguage.FRENCH -> "Intervalle d'image"
        AppLanguage.GERMAN -> "Bildintervall"
        AppLanguage.RUSSIAN -> "Интервал изображений"
        AppLanguage.JAPANESE -> "画像間隔"
        AppLanguage.KOREAN -> "이미지 간격"
    }

    val galleryVideoSettings: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "视频播放设置"
        AppLanguage.ENGLISH -> "Video Playback Settings"
        AppLanguage.ARABIC -> "إعدادات تشغيل الفيديو"
        AppLanguage.PORTUGUESE -> "Configurações de reprodução de vídeo"
        AppLanguage.SPANISH -> "Ajustes de reproducción de vídeo"
        AppLanguage.FRENCH -> "Paramètres de lecture vidéo"
        AppLanguage.GERMAN -> "Videowiedergabeeinstellungen"
        AppLanguage.RUSSIAN -> "Настройки воспроизведения видео"
        AppLanguage.JAPANESE -> "動画再生設定"
        AppLanguage.KOREAN -> "동영상 재생 설정"
    }

    val galleryEnableAudioHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "视频播放时启用声音"
        AppLanguage.ENGLISH -> "Enable sound during video playback"
        AppLanguage.ARABIC -> "تفعيل الصوت أثناء تشغيل الفيديو"
        AppLanguage.PORTUGUESE -> "Ativar som durante a reprodução de vídeo"
        AppLanguage.SPANISH -> "Activar sonido durante la reproducción de vídeo"
        AppLanguage.FRENCH -> "Activer le son pendant la lecture vidéo"
        AppLanguage.GERMAN -> "Ton bei Videowiedergabe aktivieren"
        AppLanguage.RUSSIAN -> "Включить звук при воспроизведении видео"
        AppLanguage.JAPANESE -> "動画再生時に音声を有効化"
        AppLanguage.KOREAN -> "동영상 재생 중 소리 활성화"
    }

    val galleryVideoAutoNext: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自动播放下一个"
        AppLanguage.ENGLISH -> "Auto Play Next"
        AppLanguage.ARABIC -> "تشغيل التالي تلقائيًا"
        AppLanguage.PORTUGUESE -> "Reproduzir próximo automaticamente"
        AppLanguage.SPANISH -> "Reproducir siguiente automáticamente"
        AppLanguage.FRENCH -> "Lecture suivante auto"
        AppLanguage.GERMAN -> "Nächste automatisch abspielen"
        AppLanguage.RUSSIAN -> "Автопереключение далее"
        AppLanguage.JAPANESE -> "次を自動再生"
        AppLanguage.KOREAN -> "다음 자동 재생"
    }

    val galleryVideoAutoNextHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "视频播放完毕后自动播放下一个"
        AppLanguage.ENGLISH -> "Automatically play next after video ends"
        AppLanguage.ARABIC -> "تشغيل التالي تلقائيًا بعد انتهاء الفيديو"
        AppLanguage.PORTUGUESE -> "Reproduzir próximo automaticamente após o vídeo terminar"
        AppLanguage.SPANISH -> "Reproducir siguiente automáticamente tras terminar el vídeo"
        AppLanguage.FRENCH -> "Lire automatiquement la suite après la fin de la vidéo"
        AppLanguage.GERMAN -> "Nächste automatisch abspielen, nachdem das Video endet"
        AppLanguage.RUSSIAN -> "Автоматически переключать далее после окончания видео"
        AppLanguage.JAPANESE -> "動画終了後に次を自動再生"
        AppLanguage.KOREAN -> "동영상 종료 후 다음 자동 재생"
    }

    val galleryGeneralSettings: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "通用设置"
        AppLanguage.ENGLISH -> "General Settings"
        AppLanguage.ARABIC -> "الإعدادات العامة"
        AppLanguage.PORTUGUESE -> "Configurações gerais"
        AppLanguage.SPANISH -> "Ajustes generales"
        AppLanguage.FRENCH -> "Paramètres généraux"
        AppLanguage.GERMAN -> "Allgemeine Einstellungen"
        AppLanguage.RUSSIAN -> "Общие настройки"
        AppLanguage.JAPANESE -> "一般設定"
        AppLanguage.KOREAN -> "일반 설정"
    }

    val galleryAutoPlay: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自动播放"
        AppLanguage.ENGLISH -> "Auto Play"
        AppLanguage.ARABIC -> "تشغيل تلقائي"
        AppLanguage.PORTUGUESE -> "Reprodução automática"
        AppLanguage.SPANISH -> "Reproducción automática"
        AppLanguage.FRENCH -> "Lecture auto"
        AppLanguage.GERMAN -> "Automatische Wiedergabe"
        AppLanguage.RUSSIAN -> "Автовоспроизведение"
        AppLanguage.JAPANESE -> "自動再生"
        AppLanguage.KOREAN -> "자동 재생"
    }

    val galleryAutoPlayHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "进入画廊后自动开始播放"
        AppLanguage.ENGLISH -> "Start playing automatically when entering gallery"
        AppLanguage.ARABIC -> "بدء التشغيل تلقائيًا عند الدخول إلى المعرض"
        AppLanguage.PORTUGUESE -> "Iniciar reprodução automaticamente ao entrar na galeria"
        AppLanguage.SPANISH -> "Iniciar reproducción automáticamente al entrar en la galería"
        AppLanguage.FRENCH -> "Lancer automatiquement la lecture en entrant dans la galerie"
        AppLanguage.GERMAN -> "Wiedergabe beim Öffnen der Galerie automatisch starten"
        AppLanguage.RUSSIAN -> "Автоматически начинать воспроизведение при входе в галерею"
        AppLanguage.JAPANESE -> "ギャラリーに入ると自動的に再生を開始"
        AppLanguage.KOREAN -> "갤러리 진입 시 자동 재생 시작"
    }

    val galleryLoopHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "播放完所有媒体后重新开始"
        AppLanguage.ENGLISH -> "Restart from beginning after playing all media"
        AppLanguage.ARABIC -> "إعادة التشغيل من البداية بعد تشغيل كل الوسائط"
        AppLanguage.PORTUGUESE -> "Reiniciar do início após reproduzir toda a mídia"
        AppLanguage.SPANISH -> "Reiniciar desde el principio tras reproducir todo"
        AppLanguage.FRENCH -> "Recommencer depuis le début après avoir tout lu"
        AppLanguage.GERMAN -> "Nach Wiedergabe aller Medien von vorne beginnen"
        AppLanguage.RUSSIAN -> "Начать сначала после воспроизведения всех медиа"
        AppLanguage.JAPANESE -> "すべてのメディア再生後に最初から再開"
        AppLanguage.KOREAN -> "모든 미디어 재생 후 처음부터 다시 시작"
    }

    val galleryShuffleOnLoop: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "循环时打乱顺序"
        AppLanguage.ENGLISH -> "Shuffle on Loop"
        AppLanguage.ARABIC -> "خلط عند التكرار"
        AppLanguage.PORTUGUESE -> "Aleatorizar no loop"
        AppLanguage.SPANISH -> "Aleatorizar en bucle"
        AppLanguage.FRENCH -> "Aléatoire en boucle"
        AppLanguage.GERMAN -> "Zufällig bei Schleife"
        AppLanguage.RUSSIAN -> "Перемешивать в цикле"
        AppLanguage.JAPANESE -> "ループ時にシャッフル"
        AppLanguage.KOREAN -> "반복 시 셔플"
    }

    val galleryShuffleOnLoopHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "每次循环时重新打乱播放顺序"
        AppLanguage.ENGLISH -> "Shuffle playback order on each loop"
        AppLanguage.ARABIC -> "خلط ترتيب التشغيل في كل تكرار"
        AppLanguage.PORTUGUESE -> "Aleatorizar ordem de reprodução a cada loop"
        AppLanguage.SPANISH -> "Aleatorizar orden de reproducción en cada bucle"
        AppLanguage.FRENCH -> "Mélanger l'ordre de lecture à chaque boucle"
        AppLanguage.GERMAN -> "Wiedergabereihenfolge bei jeder Schleife mischen"
        AppLanguage.RUSSIAN -> "Перемешивать порядок воспроизведения при каждом цикле"
        AppLanguage.JAPANESE -> "各ループで再生順序をシャッフル"
        AppLanguage.KOREAN -> "각 반복 시 재생 순서 셔플"
    }

    val galleryRememberPosition: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "记住播放位置"
        AppLanguage.ENGLISH -> "Remember Position"
        AppLanguage.ARABIC -> "تذكر الموضع"
        AppLanguage.PORTUGUESE -> "Lembrar posição"
        AppLanguage.SPANISH -> "Recordar posición"
        AppLanguage.FRENCH -> "Mémoriser la position"
        AppLanguage.GERMAN -> "Position merken"
        AppLanguage.RUSSIAN -> "Запомнить позицию"
        AppLanguage.JAPANESE -> "再生位置を記憶"
        AppLanguage.KOREAN -> "재생 위치 기억"
    }

    val galleryRememberPositionHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下次打开时从上次位置继续"
        AppLanguage.ENGLISH -> "Continue from last position when reopening"
        AppLanguage.ARABIC -> "المتابعة من الموضع الأخير عند إعادة الفتح"
        AppLanguage.PORTUGUESE -> "Continuar da última posição ao reabrir"
        AppLanguage.SPANISH -> "Continuar desde la última posición al reabrir"
        AppLanguage.FRENCH -> "Reprendre à la dernière position lors de la réouverture"
        AppLanguage.GERMAN -> "Beim erneuten Öffnen an letzter Position fortfahren"
        AppLanguage.RUSSIAN -> "Продолжить с последней позиции при повторном открытии"
        AppLanguage.JAPANESE -> "再開時に前回の位置から続行"
        AppLanguage.KOREAN -> "다시 열 때 마지막 위치에서 계속"
    }

    val galleryViewMode: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "视图模式"
        AppLanguage.ENGLISH -> "View Mode"
        AppLanguage.ARABIC -> "وضع العرض"
        AppLanguage.PORTUGUESE -> "Modo de exibição"
        AppLanguage.SPANISH -> "Modo de vista"
        AppLanguage.FRENCH -> "Mode d'affichage"
        AppLanguage.GERMAN -> "Ansichtsmodus"
        AppLanguage.RUSSIAN -> "Режим просмотра"
        AppLanguage.JAPANESE -> "表示モード"
        AppLanguage.KOREAN -> "보기 모드"
    }

    val galleryViewGrid: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "网格"
        AppLanguage.ENGLISH -> "Grid"
        AppLanguage.ARABIC -> "شبكة"
        AppLanguage.PORTUGUESE -> "Grade"
        AppLanguage.SPANISH -> "Cuadrícula"
        AppLanguage.FRENCH -> "Grille"
        AppLanguage.GERMAN -> "Raster"
        AppLanguage.RUSSIAN -> "Сетка"
        AppLanguage.JAPANESE -> "グリッド"
        AppLanguage.KOREAN -> "그리드"
    }

    val galleryViewList: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "列表"
        AppLanguage.ENGLISH -> "List"
        AppLanguage.ARABIC -> "قائمة"
        AppLanguage.PORTUGUESE -> "Lista"
        AppLanguage.SPANISH -> "Lista"
        AppLanguage.FRENCH -> "Liste"
        AppLanguage.GERMAN -> "Liste"
        AppLanguage.RUSSIAN -> "Список"
        AppLanguage.JAPANESE -> "リスト"
        AppLanguage.KOREAN -> "목록"
    }

    val galleryViewTimeline: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "时间线"
        AppLanguage.ENGLISH -> "Timeline"
        AppLanguage.ARABIC -> "الجدول الزمني"
        AppLanguage.PORTUGUESE -> "Linha do tempo"
        AppLanguage.SPANISH -> "Línea de tiempo"
        AppLanguage.FRENCH -> "Chronologie"
        AppLanguage.GERMAN -> "Zeitleiste"
        AppLanguage.RUSSIAN -> "Хронология"
        AppLanguage.JAPANESE -> "タイムライン"
        AppLanguage.KOREAN -> "타임라인"
    }

    val galleryGridColumns: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "网格列数"
        AppLanguage.ENGLISH -> "Grid Columns"
        AppLanguage.ARABIC -> "أعمدة الشبكة"
        AppLanguage.PORTUGUESE -> "Colunas da grade"
        AppLanguage.SPANISH -> "Columnas de cuadrícula"
        AppLanguage.FRENCH -> "Colonnes de la grille"
        AppLanguage.GERMAN -> "Rasterspalten"
        AppLanguage.RUSSIAN -> "Столбцы сетки"
        AppLanguage.JAPANESE -> "グリッド列数"
        AppLanguage.KOREAN -> "그리드 열"
    }

    val gallerySortOrder: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "排序方式"
        AppLanguage.ENGLISH -> "Sort Order"
        AppLanguage.ARABIC -> "ترتيب الفرز"
        AppLanguage.PORTUGUESE -> "Ordem de classificação"
        AppLanguage.SPANISH -> "Orden de clasificación"
        AppLanguage.FRENCH -> "Ordre de tri"
        AppLanguage.GERMAN -> "Sortierreihenfolge"
        AppLanguage.RUSSIAN -> "Порядок сортировки"
        AppLanguage.JAPANESE -> "並び順"
        AppLanguage.KOREAN -> "정렬 순서"
    }

    val gallerySortCustom: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自定义排序"
        AppLanguage.ENGLISH -> "Custom Order"
        AppLanguage.ARABIC -> "ترتيب مخصص"
        AppLanguage.PORTUGUESE -> "Ordem personalizada"
        AppLanguage.SPANISH -> "Orden personalizado"
        AppLanguage.FRENCH -> "Ordre personnalisé"
        AppLanguage.GERMAN -> "Benutzerdefinierte Reihenfolge"
        AppLanguage.RUSSIAN -> "Свой порядок"
        AppLanguage.JAPANESE -> "カスタム順"
        AppLanguage.KOREAN -> "사용자 지정 순서"
    }

    val gallerySortNameAsc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "名称升序"
        AppLanguage.ENGLISH -> "Name (A-Z)"
        AppLanguage.ARABIC -> "الاسم (أ-ي)"
        AppLanguage.PORTUGUESE -> "Nome (A-Z)"
        AppLanguage.SPANISH -> "Nombre (A-Z)"
        AppLanguage.FRENCH -> "Nom (A-Z)"
        AppLanguage.GERMAN -> "Name (A–Z)"
        AppLanguage.RUSSIAN -> "Имя (A-Z)"
        AppLanguage.JAPANESE -> "名前 (A-Z)"
        AppLanguage.KOREAN -> "이름 (A-Z)"
    }

    val gallerySortNameDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "名称降序"
        AppLanguage.ENGLISH -> "Name (Z-A)"
        AppLanguage.ARABIC -> "الاسم (ي-أ)"
        AppLanguage.PORTUGUESE -> "Nome (Z-A)"
        AppLanguage.SPANISH -> "Nombre (Z-A)"
        AppLanguage.FRENCH -> "Nom (Z-A)"
        AppLanguage.GERMAN -> "Name (Z–A)"
        AppLanguage.RUSSIAN -> "Имя (Z-A)"
        AppLanguage.JAPANESE -> "名前 (Z-A)"
        AppLanguage.KOREAN -> "이름 (Z-A)"
    }

    val gallerySortDateAsc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "日期升序"
        AppLanguage.ENGLISH -> "Date (Oldest)"
        AppLanguage.ARABIC -> "التاريخ (الأقدم)"
        AppLanguage.PORTUGUESE -> "Data (mais antiga)"
        AppLanguage.SPANISH -> "Fecha (más antigua)"
        AppLanguage.FRENCH -> "Date (plus ancienne)"
        AppLanguage.GERMAN -> "Datum (älteste)"
        AppLanguage.RUSSIAN -> "Дата (старые)"
        AppLanguage.JAPANESE -> "日付(古い順)"
        AppLanguage.KOREAN -> "날짜(오래된 순)"
    }

    val gallerySortDateDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "日期降序"
        AppLanguage.ENGLISH -> "Date (Newest)"
        AppLanguage.ARABIC -> "التاريخ (الأحدث)"
        AppLanguage.PORTUGUESE -> "Data (mais recente)"
        AppLanguage.SPANISH -> "Fecha (más reciente)"
        AppLanguage.FRENCH -> "Date (plus récente)"
        AppLanguage.GERMAN -> "Datum (neueste)"
        AppLanguage.RUSSIAN -> "Дата (новые)"
        AppLanguage.JAPANESE -> "日付(新しい順)"
        AppLanguage.KOREAN -> "날짜(최신 순)"
    }

    val gallerySortType: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "按类型分组"
        AppLanguage.ENGLISH -> "By Type"
        AppLanguage.ARABIC -> "حسب النوع"
        AppLanguage.PORTUGUESE -> "Por tipo"
        AppLanguage.SPANISH -> "Por tipo"
        AppLanguage.FRENCH -> "Par type"
        AppLanguage.GERMAN -> "Nach Typ"
        AppLanguage.RUSSIAN -> "По типу"
        AppLanguage.JAPANESE -> "種類別"
        AppLanguage.KOREAN -> "유형별"
    }

    val galleryPlayerSettings: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "播放器设置"
        AppLanguage.ENGLISH -> "Player Settings"
        AppLanguage.ARABIC -> "إعدادات المشغل"
        AppLanguage.PORTUGUESE -> "Configurações do player"
        AppLanguage.SPANISH -> "Ajustes del reproductor"
        AppLanguage.FRENCH -> "Paramètres du lecteur"
        AppLanguage.GERMAN -> "Player-Einstellungen"
        AppLanguage.RUSSIAN -> "Настройки плеера"
        AppLanguage.JAPANESE -> "プレーヤー設定"
        AppLanguage.KOREAN -> "플레이어 설정"
    }

    val galleryShowThumbnailBar: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "显示缩略图栏"
        AppLanguage.ENGLISH -> "Show Thumbnail Bar"
        AppLanguage.ARABIC -> "إظهار شريط الصور المصغرة"
        AppLanguage.PORTUGUESE -> "Mostrar barra de miniaturas"
        AppLanguage.SPANISH -> "Mostrar barra de miniaturas"
        AppLanguage.FRENCH -> "Afficher la barre de vignettes"
        AppLanguage.GERMAN -> "Miniaturleiste anzeigen"
        AppLanguage.RUSSIAN -> "Показывать панель миниатюр"
        AppLanguage.JAPANESE -> "サムネイルバーを表示"
        AppLanguage.KOREAN -> "썸네일 막대 표시"
    }

    val galleryShowThumbnailBarHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "在播放器底部显示可点击的缩略图导航栏"
        AppLanguage.ENGLISH -> "Show clickable thumbnail navigation bar at the bottom"
        AppLanguage.ARABIC -> "إظهار شريط التنقل بالصور المصغرة القابلة للنقر في الأسفل"
        AppLanguage.PORTUGUESE -> "Mostrar barra de navegação por miniaturas clicáveis na parte inferior"
        AppLanguage.SPANISH -> "Mostrar barra de navegación de miniaturas clicables en la parte inferior"
        AppLanguage.FRENCH -> "Afficher la barre de navigation par vignettes cliquables en bas"
        AppLanguage.GERMAN -> "Klickbare Miniatur-Navigationsleiste unten anzeigen"
        AppLanguage.RUSSIAN -> "Показывать кликабельную панель навигации по миниатюрам внизу"
        AppLanguage.JAPANESE -> "下部にクリック可能なサムネイルナビゲーションバーを表示"
        AppLanguage.KOREAN -> "하단에 클릭 가능한 썸네일 내비게이션 바 표시"
    }

    val galleryShowMediaInfo: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "显示媒体信息"
        AppLanguage.ENGLISH -> "Show Media Info"
        AppLanguage.ARABIC -> "إظهار معلومات الوسائط"
        AppLanguage.PORTUGUESE -> "Mostrar informações de mídia"
        AppLanguage.SPANISH -> "Mostrar información multimedia"
        AppLanguage.FRENCH -> "Afficher les infos du média"
        AppLanguage.GERMAN -> "Medieninfo anzeigen"
        AppLanguage.RUSSIAN -> "Показывать информацию о медиа"
        AppLanguage.JAPANESE -> "メディア情報を表示"
        AppLanguage.KOREAN -> "미디어 정보 표시"
    }

    val galleryShowMediaInfoHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "播放时显示媒体名称、索引等信息"
        AppLanguage.ENGLISH -> "Show media name, index, etc. during playback"
        AppLanguage.ARABIC -> "إظهار اسم الوسائط والفهرس وغيرها أثناء التشغيل"
        AppLanguage.PORTUGUESE -> "Mostrar nome, índice etc. da mídia durante a reprodução"
        AppLanguage.SPANISH -> "Mostrar nombre, índice etc. durante la reproducción"
        AppLanguage.FRENCH -> "Afficher le nom, l'index etc. du média pendant la lecture"
        AppLanguage.GERMAN -> "Medienname, Index usw. während der Wiedergabe anzeigen"
        AppLanguage.RUSSIAN -> "Показывать название, индекс и т. д. медиа при воспроизведении"
        AppLanguage.JAPANESE -> "再生中にメディア名、インデックスなどを表示"
        AppLanguage.KOREAN -> "재생 중 미디어 이름, 인덱스 등 표시"
    }

    val galleryBackgroundColor: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "背景颜色"
        AppLanguage.ENGLISH -> "Background Color"
        AppLanguage.ARABIC -> "لون الخلفية"
        AppLanguage.PORTUGUESE -> "Cor de fundo"
        AppLanguage.SPANISH -> "Color de fondo"
        AppLanguage.FRENCH -> "Couleur de fond"
        AppLanguage.GERMAN -> "Hintergrundfarbe"
        AppLanguage.RUSSIAN -> "Цвет фона"
        AppLanguage.JAPANESE -> "背景色"
        AppLanguage.KOREAN -> "배경색"
    }

    val galleryAddCategory: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "添加分类"
        AppLanguage.ENGLISH -> "Add Category"
        AppLanguage.ARABIC -> "إضافة فئة"
        AppLanguage.PORTUGUESE -> "Adicionar categoria"
        AppLanguage.SPANISH -> "Añadir categoría"
        AppLanguage.FRENCH -> "Ajouter une catégorie"
        AppLanguage.GERMAN -> "Kategorie hinzufügen"
        AppLanguage.RUSSIAN -> "Добавить категорию"
        AppLanguage.JAPANESE -> "カテゴリを追加"
        AppLanguage.KOREAN -> "카테고리 추가"
    }

    val galleryEditCategory: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "编辑分类"
        AppLanguage.ENGLISH -> "Edit Category"
        AppLanguage.ARABIC -> "تعديل الفئة"
        AppLanguage.PORTUGUESE -> "Editar categoria"
        AppLanguage.SPANISH -> "Editar categoría"
        AppLanguage.FRENCH -> "Modifier la catégorie"
        AppLanguage.GERMAN -> "Kategorie bearbeiten"
        AppLanguage.RUSSIAN -> "Изменить категорию"
        AppLanguage.JAPANESE -> "カテゴリを編集"
        AppLanguage.KOREAN -> "카테고리 편집"
    }

    val galleryCategoryName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "分类名称"
        AppLanguage.ENGLISH -> "Category Name"
        AppLanguage.ARABIC -> "اسم الفئة"
        AppLanguage.PORTUGUESE -> "Nome da categoria"
        AppLanguage.SPANISH -> "Nombre de categoría"
        AppLanguage.FRENCH -> "Nom de la catégorie"
        AppLanguage.GERMAN -> "Kategoriename"
        AppLanguage.RUSSIAN -> "Название категории"
        AppLanguage.JAPANESE -> "カテゴリ名"
        AppLanguage.KOREAN -> "카테고리 이름"
    }

    val galleryCategoryIcon: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "分类图标"
        AppLanguage.ENGLISH -> "Category Icon"
        AppLanguage.ARABIC -> "رمز الفئة"
        AppLanguage.PORTUGUESE -> "Ícone da categoria"
        AppLanguage.SPANISH -> "Icono de categoría"
        AppLanguage.FRENCH -> "Icône de la catégorie"
        AppLanguage.GERMAN -> "Kategorie-Icon"
        AppLanguage.RUSSIAN -> "Иконка категории"
        AppLanguage.JAPANESE -> "カテゴリアイコン"
        AppLanguage.KOREAN -> "카테고리 아이콘"
    }

    val galleryCategoryColor: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "分类颜色"
        AppLanguage.ENGLISH -> "Category Color"
        AppLanguage.ARABIC -> "لون الفئة"
        AppLanguage.PORTUGUESE -> "Cor da categoria"
        AppLanguage.SPANISH -> "Color de categoría"
        AppLanguage.FRENCH -> "Couleur de la catégorie"
        AppLanguage.GERMAN -> "Kategoriefarbe"
        AppLanguage.RUSSIAN -> "Цвет категории"
        AppLanguage.JAPANESE -> "カテゴリ色"
        AppLanguage.KOREAN -> "카테고리 색상"
    }

    val galleryMediaDetail: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "媒体详情"
        AppLanguage.ENGLISH -> "Media Details"
        AppLanguage.ARABIC -> "تفاصيل الوسائط"
        AppLanguage.PORTUGUESE -> "Detalhes da mídia"
        AppLanguage.SPANISH -> "Detalles multimedia"
        AppLanguage.FRENCH -> "Détails du média"
        AppLanguage.GERMAN -> "Mediendetails"
        AppLanguage.RUSSIAN -> "Детали медиа"
        AppLanguage.JAPANESE -> "メディア詳細"
        AppLanguage.KOREAN -> "미디어 세부 정보"
    }

    val galleryCategory: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "分类"
        AppLanguage.ENGLISH -> "Category"
        AppLanguage.ARABIC -> "الفئة"
        AppLanguage.PORTUGUESE -> "Categoria"
        AppLanguage.SPANISH -> "Categoría"
        AppLanguage.FRENCH -> "Catégorie"
        AppLanguage.GERMAN -> "Kategorie"
        AppLanguage.RUSSIAN -> "Категория"
        AppLanguage.JAPANESE -> "カテゴリ"
        AppLanguage.KOREAN -> "카테고리"
    }

    val galleryNoCategory: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "未分类"
        AppLanguage.ENGLISH -> "Uncategorized"
        AppLanguage.ARABIC -> "غير مصنف"
        AppLanguage.PORTUGUESE -> "Sem categoria"
        AppLanguage.SPANISH -> "Sin categoría"
        AppLanguage.FRENCH -> "Non classé"
        AppLanguage.GERMAN -> "Ohne Kategorie"
        AppLanguage.RUSSIAN -> "Без категории"
        AppLanguage.JAPANESE -> "未分類"
        AppLanguage.KOREAN -> "분류 안 됨"
    }

    val galleryType: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "类型"
        AppLanguage.ENGLISH -> "Type"
        AppLanguage.ARABIC -> "النوع"
        AppLanguage.PORTUGUESE -> "Tipo"
        AppLanguage.SPANISH -> "Tipo"
        AppLanguage.FRENCH -> "Type"
        AppLanguage.GERMAN -> "Typ"
        AppLanguage.RUSSIAN -> "Тип"
        AppLanguage.JAPANESE -> "種類"
        AppLanguage.KOREAN -> "유형"
    }

    val galleryDuration: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "时长"
        AppLanguage.ENGLISH -> "Duration"
        AppLanguage.ARABIC -> "المدة"
        AppLanguage.PORTUGUESE -> "Duração"
        AppLanguage.SPANISH -> "Duración"
        AppLanguage.FRENCH -> "Durée"
        AppLanguage.GERMAN -> "Dauer"
        AppLanguage.RUSSIAN -> "Длительность"
        AppLanguage.JAPANESE -> "長さ"
        AppLanguage.KOREAN -> "길이"
    }

    val gallerySize: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "大小"
        AppLanguage.ENGLISH -> "Size"
        AppLanguage.ARABIC -> "الحجم"
        AppLanguage.PORTUGUESE -> "Tamanho"
        AppLanguage.SPANISH -> "Tamaño"
        AppLanguage.FRENCH -> "Taille"
        AppLanguage.GERMAN -> "Größe"
        AppLanguage.RUSSIAN -> "Размер"
        AppLanguage.JAPANESE -> "サイズ"
        AppLanguage.KOREAN -> "크기"
    }

    val galleryDimensions: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "尺寸"
        AppLanguage.ENGLISH -> "Dimensions"
        AppLanguage.ARABIC -> "الأبعاد"
        AppLanguage.PORTUGUESE -> "Dimensões"
        AppLanguage.SPANISH -> "Dimensiones"
        AppLanguage.FRENCH -> "Dimensions"
        AppLanguage.GERMAN -> "Abmessungen"
        AppLanguage.RUSSIAN -> "Размеры"
        AppLanguage.JAPANESE -> "寸法"
        AppLanguage.KOREAN -> "해상도"
    }

    val name: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "名称"
        AppLanguage.ENGLISH -> "Name"
        AppLanguage.ARABIC -> "الاسم"
        AppLanguage.PORTUGUESE -> "Nome"
        AppLanguage.SPANISH -> "Nombre"
        AppLanguage.FRENCH -> "Nom"
        AppLanguage.GERMAN -> "Name"
        AppLanguage.RUSSIAN -> "Имя"
        AppLanguage.JAPANESE -> "名前"
        AppLanguage.KOREAN -> "이름"
    }

    val galleryPlayerPrevious: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "上一个"
        AppLanguage.ENGLISH -> "Previous"
        AppLanguage.ARABIC -> "السابق"
        AppLanguage.PORTUGUESE -> "Anterior"
        AppLanguage.SPANISH -> "Anterior"
        AppLanguage.FRENCH -> "Précédent"
        AppLanguage.GERMAN -> "Zurück"
        AppLanguage.RUSSIAN -> "Предыдущее"
        AppLanguage.JAPANESE -> "前へ"
        AppLanguage.KOREAN -> "이전"
    }

    val galleryPlayerNext: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下一个"
        AppLanguage.ENGLISH -> "Next"
        AppLanguage.ARABIC -> "التالي"
        AppLanguage.PORTUGUESE -> "Próximo"
        AppLanguage.SPANISH -> "Siguiente"
        AppLanguage.FRENCH -> "Suivant"
        AppLanguage.GERMAN -> "Weiter"
        AppLanguage.RUSSIAN -> "Следующее"
        AppLanguage.JAPANESE -> "次へ"
        AppLanguage.KOREAN -> "다음"
    }

    val galleryPlayerPause: String get() = when (Strings.lang) {
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

    val galleryPlayerPlay: String get() = when (Strings.lang) {
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

    val galleryPlayerSeekForward: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "快进 10 秒"
        AppLanguage.ENGLISH -> "Forward 10s"
        AppLanguage.ARABIC -> "تقديم 10 ثواني"
        AppLanguage.PORTUGUESE -> "Avançar 10s"
        AppLanguage.SPANISH -> "Adelantar 10s"
        AppLanguage.FRENCH -> "Avancer 10 s"
        AppLanguage.GERMAN -> "10 s vor"
        AppLanguage.RUSSIAN -> "Вперёд 10 с"
        AppLanguage.JAPANESE -> "10秒進む"
        AppLanguage.KOREAN -> "10초 앞으로"
    }

    val galleryPlayerSeekBack: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "后退 10 秒"
        AppLanguage.ENGLISH -> "Back 10s"
        AppLanguage.ARABIC -> "رجوع 10 ثواني"
        AppLanguage.PORTUGUESE -> "Retroceder 10s"
        AppLanguage.SPANISH -> "Retroceder 10s"
        AppLanguage.FRENCH -> "Reculer 10 s"
        AppLanguage.GERMAN -> "10 s zurück"
        AppLanguage.RUSSIAN -> "Назад 10 с"
        AppLanguage.JAPANESE -> "10秒戻る"
        AppLanguage.KOREAN -> "10초 뒤로"
    }

    val tagVideo: String get() = when (Strings.lang) {
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
    val tagDownload: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下载"
        AppLanguage.ENGLISH -> "Download"
        AppLanguage.ARABIC -> "تحميل"
        AppLanguage.PORTUGUESE -> "Download"
        AppLanguage.SPANISH -> "Descarga"
        AppLanguage.FRENCH -> "Téléchargement"
        AppLanguage.GERMAN -> "Download"
        AppLanguage.RUSSIAN -> "Загрузка"
        AppLanguage.JAPANESE -> "ダウンロード"
        AppLanguage.KOREAN -> "다운로드"
    }
    val tagImage: String get() = when (Strings.lang) {
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
    val tagSpeed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "倍速"
        AppLanguage.ENGLISH -> "Speed"
        AppLanguage.ARABIC -> "السرعة"
        AppLanguage.PORTUGUESE -> "Velocidade"
        AppLanguage.SPANISH -> "Velocidad"
        AppLanguage.FRENCH -> "Vitesse"
        AppLanguage.GERMAN -> "Geschwindigkeit"
        AppLanguage.RUSSIAN -> "Скорость"
        AppLanguage.JAPANESE -> "速度"
        AppLanguage.KOREAN -> "속도"
    }
    val tagPiP: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "画中画"
        AppLanguage.ENGLISH -> "PiP"
        AppLanguage.ARABIC -> "صورة داخل صورة"
        AppLanguage.PORTUGUESE -> "PiP"
        AppLanguage.SPANISH -> "PiP"
        AppLanguage.FRENCH -> "PiP"
        AppLanguage.GERMAN -> "PiP"
        AppLanguage.RUSSIAN -> "PiP"
        AppLanguage.JAPANESE -> "PiP"
        AppLanguage.KOREAN -> "PiP"
    }
    val tagDebug: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "调试"
        AppLanguage.ENGLISH -> "Debug"
        AppLanguage.ARABIC -> "تصحيح"
        AppLanguage.PORTUGUESE -> "Depurar"
        AppLanguage.SPANISH -> "Depurar"
        AppLanguage.FRENCH -> "Débogage"
        AppLanguage.GERMAN -> "Debuggen"
        AppLanguage.RUSSIAN -> "Отладка"
        AppLanguage.JAPANESE -> "デバッグ"
        AppLanguage.KOREAN -> "디버그"
    }
    val tagAnalyze: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "分析"
        AppLanguage.ENGLISH -> "Analyze"
        AppLanguage.ARABIC -> "تحليل"
        AppLanguage.PORTUGUESE -> "Analisar"
        AppLanguage.SPANISH -> "Analizar"
        AppLanguage.FRENCH -> "Analyser"
        AppLanguage.GERMAN -> "Analysieren"
        AppLanguage.RUSSIAN -> "Анализ"
        AppLanguage.JAPANESE -> "分析"
        AppLanguage.KOREAN -> "분석"
    }
    val tagDevelop: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "开发"
        AppLanguage.ENGLISH -> "Develop"
        AppLanguage.ARABIC -> "تطوير"
        AppLanguage.PORTUGUESE -> "Desenvolver"
        AppLanguage.SPANISH -> "Desarrollar"
        AppLanguage.FRENCH -> "Développer"
        AppLanguage.GERMAN -> "Entwickeln"
        AppLanguage.RUSSIAN -> "Разработка"
        AppLanguage.JAPANESE -> "開発"
        AppLanguage.KOREAN -> "개발"
    }
    val tagDark: String get() = when (Strings.lang) {
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
    val tagEyeCare: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "护眼"
        AppLanguage.ENGLISH -> "Eye Care"
        AppLanguage.ARABIC -> "حماية العين"
        AppLanguage.PORTUGUESE -> "Proteção Ocular"
        AppLanguage.SPANISH -> "Cuidado Ocular"
        AppLanguage.FRENCH -> "Protection des Yeux"
        AppLanguage.GERMAN -> "Augenschutz"
        AppLanguage.RUSSIAN -> "Защита глаз"
        AppLanguage.JAPANESE -> "アイケア"
        AppLanguage.KOREAN -> "아이케어"
    }
    val tagTheme: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "主题"
        AppLanguage.ENGLISH -> "Theme"
        AppLanguage.ARABIC -> "المظهر"
        AppLanguage.PORTUGUESE -> "Tema"
        AppLanguage.SPANISH -> "Tema"
        AppLanguage.FRENCH -> "Thème"
        AppLanguage.GERMAN -> "Design"
        AppLanguage.RUSSIAN -> "Тема"
        AppLanguage.JAPANESE -> "テーマ"
        AppLanguage.KOREAN -> "테마"
    }
    val tagPrivacy: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "隐私"
        AppLanguage.ENGLISH -> "Privacy"
        AppLanguage.ARABIC -> "الخصوصية"
        AppLanguage.PORTUGUESE -> "Privacidade"
        AppLanguage.SPANISH -> "Privacidad"
        AppLanguage.FRENCH -> "Confidentialité"
        AppLanguage.GERMAN -> "Datenschutz"
        AppLanguage.RUSSIAN -> "Конфиденциальность"
        AppLanguage.JAPANESE -> "プライバシー"
        AppLanguage.KOREAN -> "개인정보"
    }
    val tagSecurity: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "安全"
        AppLanguage.ENGLISH -> "Security"
        AppLanguage.ARABIC -> "الأمان"
        AppLanguage.PORTUGUESE -> "Segurança"
        AppLanguage.SPANISH -> "Seguridad"
        AppLanguage.FRENCH -> "Sécurité"
        AppLanguage.GERMAN -> "Sicherheit"
        AppLanguage.RUSSIAN -> "Безопасность"
        AppLanguage.JAPANESE -> "セキュリティ"
        AppLanguage.KOREAN -> "보안"
    }
    val tagAntiTrack: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "反追踪"
        AppLanguage.ENGLISH -> "Anti-Track"
        AppLanguage.ARABIC -> "مكافحة التتبع"
        AppLanguage.PORTUGUESE -> "Antirrastreio"
        AppLanguage.SPANISH -> "Antirrastreo"
        AppLanguage.FRENCH -> "Anti-Pistage"
        AppLanguage.GERMAN -> "Anti-Tracking"
        AppLanguage.RUSSIAN -> "Анти-трекинг"
        AppLanguage.JAPANESE -> "トラッキング防止"
        AppLanguage.KOREAN -> "추적 방지"
    }
    val tagAd: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "广告"
        AppLanguage.ENGLISH -> "Ad"
        AppLanguage.ARABIC -> "إعلان"
        AppLanguage.PORTUGUESE -> "Anúncio"
        AppLanguage.SPANISH -> "Anuncio"
        AppLanguage.FRENCH -> "Pub"
        AppLanguage.GERMAN -> "Werbung"
        AppLanguage.RUSSIAN -> "Реклама"
        AppLanguage.JAPANESE -> "広告"
        AppLanguage.KOREAN -> "광고"
    }
    val tagElement: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "元素"
        AppLanguage.ENGLISH -> "Element"
        AppLanguage.ARABIC -> "عنصر"
        AppLanguage.PORTUGUESE -> "Elemento"
        AppLanguage.SPANISH -> "Elemento"
        AppLanguage.FRENCH -> "Élément"
        AppLanguage.GERMAN -> "Element"
        AppLanguage.RUSSIAN -> "Элемент"
        AppLanguage.JAPANESE -> "要素"
        AppLanguage.KOREAN -> "요소"
    }
    val tagCopy: String get() = when (Strings.lang) {
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
    val tagTranslate: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "翻译"
        AppLanguage.ENGLISH -> "Translate"
        AppLanguage.ARABIC -> "ترجمة"
        AppLanguage.PORTUGUESE -> "Traduzir"
        AppLanguage.SPANISH -> "Traducir"
        AppLanguage.FRENCH -> "Traduire"
        AppLanguage.GERMAN -> "Übersetzen"
        AppLanguage.RUSSIAN -> "Перевести"
        AppLanguage.JAPANESE -> "翻訳"
        AppLanguage.KOREAN -> "번역"
    }
    val tagScreenshot: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "截图"
        AppLanguage.ENGLISH -> "Screenshot"
        AppLanguage.ARABIC -> "لقطة شاشة"
        AppLanguage.PORTUGUESE -> "Captura de Tela"
        AppLanguage.SPANISH -> "Captura de Pantalla"
        AppLanguage.FRENCH -> "Capture d'Écran"
        AppLanguage.GERMAN -> "Bildschirmfoto"
        AppLanguage.RUSSIAN -> "Снимок экрана"
        AppLanguage.JAPANESE -> "スクリーンショット"
        AppLanguage.KOREAN -> "스크린샷"
    }

    val tagToast: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Notice"
        AppLanguage.ENGLISH -> "Toast"
        AppLanguage.ARABIC -> "تنبيه"
        AppLanguage.PORTUGUESE -> "Aviso"
        AppLanguage.SPANISH -> "Aviso"
        AppLanguage.FRENCH -> "Avis"
        AppLanguage.GERMAN -> "Hinweis"
        AppLanguage.RUSSIAN -> "Уведомление"
        AppLanguage.JAPANESE -> "通知"
        AppLanguage.KOREAN -> "알림"
    }
    val tagMessage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "消息"
        AppLanguage.ENGLISH -> "Message"
        AppLanguage.ARABIC -> "رسالة"
        AppLanguage.PORTUGUESE -> "Mensagem"
        AppLanguage.SPANISH -> "Mensaje"
        AppLanguage.FRENCH -> "Message"
        AppLanguage.GERMAN -> "Nachricht"
        AppLanguage.RUSSIAN -> "Сообщение"
        AppLanguage.JAPANESE -> "メッセージ"
        AppLanguage.KOREAN -> "메시지"
    }
    val tagVibrate: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "震动"
        AppLanguage.ENGLISH -> "Vibrate"
        AppLanguage.ARABIC -> "اهتزاز"
        AppLanguage.PORTUGUESE -> "Vibrar"
        AppLanguage.SPANISH -> "Vibrar"
        AppLanguage.FRENCH -> "Vibrer"
        AppLanguage.GERMAN -> "Vibrieren"
        AppLanguage.RUSSIAN -> "Вибрация"
        AppLanguage.JAPANESE -> "振動"
        AppLanguage.KOREAN -> "진동"
    }
    val tagFeedback: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "反馈"
        AppLanguage.ENGLISH -> "Feedback"
        AppLanguage.ARABIC -> "ردود الفعل"
        AppLanguage.PORTUGUESE -> "Comentários"
        AppLanguage.SPANISH -> "Comentarios"
        AppLanguage.FRENCH -> "Retour"
        AppLanguage.GERMAN -> "Feedback"
        AppLanguage.RUSSIAN -> "Отзыв"
        AppLanguage.JAPANESE -> "フィードバック"
        AppLanguage.KOREAN -> "피드백"
    }
    val tagHaptic: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "触感"
        AppLanguage.ENGLISH -> "Haptic"
        AppLanguage.ARABIC -> "لمسي"
        AppLanguage.PORTUGUESE -> "Tátil"
        AppLanguage.SPANISH -> "Táctil"
        AppLanguage.FRENCH -> "Haptique"
        AppLanguage.GERMAN -> "Haptisch"
        AppLanguage.RUSSIAN -> "Тактильный"
        AppLanguage.JAPANESE -> "ハプティック"
        AppLanguage.KOREAN -> "햅틱"
    }
    val tagClipboard: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "剪贴板"
        AppLanguage.ENGLISH -> "Clipboard"
        AppLanguage.ARABIC -> "الحافظة"
        AppLanguage.PORTUGUESE -> "Área de Transferência"
        AppLanguage.SPANISH -> "Portapapeles"
        AppLanguage.FRENCH -> "Presse-papiers"
        AppLanguage.GERMAN -> "Zwischenablage"
        AppLanguage.RUSSIAN -> "Буфер обмена"
        AppLanguage.JAPANESE -> "クリップボード"
        AppLanguage.KOREAN -> "클립보드"
    }
    val tagShare: String get() = when (Strings.lang) {
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
    val tagSocial: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "社交"
        AppLanguage.ENGLISH -> "Social"
        AppLanguage.ARABIC -> "اجتماعي"
        AppLanguage.PORTUGUESE -> "Social"
        AppLanguage.SPANISH -> "Social"
        AppLanguage.FRENCH -> "Social"
        AppLanguage.GERMAN -> "Sozial"
        AppLanguage.RUSSIAN -> "Соцсети"
        AppLanguage.JAPANESE -> "ソーシャル"
        AppLanguage.KOREAN -> "소셜"
    }
    val tagSave: String get() = when (Strings.lang) {
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
    val tagGallery: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "相册"
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
    val tagBrowser: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "浏览器"
        AppLanguage.ENGLISH -> "Browser"
        AppLanguage.ARABIC -> "متصفح"
        AppLanguage.PORTUGUESE -> "Navegador"
        AppLanguage.SPANISH -> "Navegador"
        AppLanguage.FRENCH -> "Navigateur"
        AppLanguage.GERMAN -> "Browser"
        AppLanguage.RUSSIAN -> "Браузер"
        AppLanguage.JAPANESE -> "ブラウザ"
        AppLanguage.KOREAN -> "브라우저"
    }
    val tagLink: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "链接"
        AppLanguage.ENGLISH -> "Link"
        AppLanguage.ARABIC -> "رابط"
        AppLanguage.PORTUGUESE -> "Link"
        AppLanguage.SPANISH -> "Enlace"
        AppLanguage.FRENCH -> "Lien"
        AppLanguage.GERMAN -> "Link"
        AppLanguage.RUSSIAN -> "Ссылка"
        AppLanguage.JAPANESE -> "リンク"
        AppLanguage.KOREAN -> "링크"
    }
    val tagExternal: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "外部"
        AppLanguage.ENGLISH -> "External"
        AppLanguage.ARABIC -> "خارجي"
        AppLanguage.PORTUGUESE -> "Externo"
        AppLanguage.SPANISH -> "Externo"
        AppLanguage.FRENCH -> "Externe"
        AppLanguage.GERMAN -> "Extern"
        AppLanguage.RUSSIAN -> "Внешний"
        AppLanguage.JAPANESE -> "外部"
        AppLanguage.KOREAN -> "외부"
    }
    val tagDevice: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "设备"
        AppLanguage.ENGLISH -> "Device"
        AppLanguage.ARABIC -> "جهاز"
        AppLanguage.PORTUGUESE -> "Dispositivo"
        AppLanguage.SPANISH -> "Dispositivo"
        AppLanguage.FRENCH -> "Appareil"
        AppLanguage.GERMAN -> "Gerät"
        AppLanguage.RUSSIAN -> "Устройство"
        AppLanguage.JAPANESE -> "デバイス"
        AppLanguage.KOREAN -> "기기"
    }
    val tagInfo: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "信息"
        AppLanguage.ENGLISH -> "Info"
        AppLanguage.ARABIC -> "معلومات"
        AppLanguage.PORTUGUESE -> "Info"
        AppLanguage.SPANISH -> "Info"
        AppLanguage.FRENCH -> "Infos"
        AppLanguage.GERMAN -> "Info"
        AppLanguage.RUSSIAN -> "Инфо"
        AppLanguage.JAPANESE -> "情報"
        AppLanguage.KOREAN -> "정보"
    }
    val tagScreen: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "屏幕"
        AppLanguage.ENGLISH -> "Screen"
        AppLanguage.ARABIC -> "شاشة"
        AppLanguage.PORTUGUESE -> "Tela"
        AppLanguage.SPANISH -> "Pantalla"
        AppLanguage.FRENCH -> "Écran"
        AppLanguage.GERMAN -> "Bildschirm"
        AppLanguage.RUSSIAN -> "Экран"
        AppLanguage.JAPANESE -> "画面"
        AppLanguage.KOREAN -> "화면"
    }
    val tagNetwork: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "网络"
        AppLanguage.ENGLISH -> "Network"
        AppLanguage.ARABIC -> "شبكة"
        AppLanguage.PORTUGUESE -> "Rede"
        AppLanguage.SPANISH -> "Red"
        AppLanguage.FRENCH -> "Réseau"
        AppLanguage.GERMAN -> "Netzwerk"
        AppLanguage.RUSSIAN -> "Сеть"
        AppLanguage.JAPANESE -> "ネットワーク"
        AppLanguage.KOREAN -> "네트워크"
    }
    val tagWiFi: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "WiFi"
        AppLanguage.ENGLISH -> "WiFi"
        AppLanguage.ARABIC -> "واي فاي"
        AppLanguage.PORTUGUESE -> "WiFi"
        AppLanguage.SPANISH -> "WiFi"
        AppLanguage.FRENCH -> "WiFi"
        AppLanguage.GERMAN -> "WiFi"
        AppLanguage.RUSSIAN -> "WiFi"
        AppLanguage.JAPANESE -> "WiFi"
        AppLanguage.KOREAN -> "WiFi"
    }
    val tagData: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "流量"
        AppLanguage.ENGLISH -> "Data"
        AppLanguage.ARABIC -> "بيانات"
        AppLanguage.PORTUGUESE -> "Dados"
        AppLanguage.SPANISH -> "Datos"
        AppLanguage.FRENCH -> "Données"
        AppLanguage.GERMAN -> "Daten"
        AppLanguage.RUSSIAN -> "Данные"
        AppLanguage.JAPANESE -> "データ"
        AppLanguage.KOREAN -> "데이터"
    }
    val tagFile: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "文件"
        AppLanguage.ENGLISH -> "File"
        AppLanguage.ARABIC -> "ملف"
        AppLanguage.PORTUGUESE -> "Arquivo"
        AppLanguage.SPANISH -> "Archivo"
        AppLanguage.FRENCH -> "Fichier"
        AppLanguage.GERMAN -> "Datei"
        AppLanguage.RUSSIAN -> "Файл"
        AppLanguage.JAPANESE -> "ファイル"
        AppLanguage.KOREAN -> "파일"
    }
    val tagExport: String get() = when (Strings.lang) {
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
    val tagFloating: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "悬浮"
        AppLanguage.ENGLISH -> "Floating"
        AppLanguage.ARABIC -> "عائم"
        AppLanguage.PORTUGUESE -> "Flutuante"
        AppLanguage.SPANISH -> "Flotante"
        AppLanguage.FRENCH -> "Flottant"
        AppLanguage.GERMAN -> "Schwebend"
        AppLanguage.RUSSIAN -> "Плавающий"
        AppLanguage.JAPANESE -> "フローティング"
        AppLanguage.KOREAN -> "플로팅"
    }
    val tagQuery: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "查询"
        AppLanguage.ENGLISH -> "Query"
        AppLanguage.ARABIC -> "استعلام"
        AppLanguage.PORTUGUESE -> "Consulta"
        AppLanguage.SPANISH -> "Consulta"
        AppLanguage.FRENCH -> "Requête"
        AppLanguage.GERMAN -> "Abfrage"
        AppLanguage.RUSSIAN -> "Запрос"
        AppLanguage.JAPANESE -> "クエリ"
        AppLanguage.KOREAN -> "쿼리"
    }
    val tagSelector: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择器"
        AppLanguage.ENGLISH -> "Selector"
        AppLanguage.ARABIC -> "محدد"
        AppLanguage.PORTUGUESE -> "Seletor"
        AppLanguage.SPANISH -> "Selector"
        AppLanguage.FRENCH -> "Sélecteur"
        AppLanguage.GERMAN -> "Selektor"
        AppLanguage.RUSSIAN -> "Селектор"
        AppLanguage.JAPANESE -> "セレクタ"
        AppLanguage.KOREAN -> "셀렉터"
    }
    val tagIterate: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "遍历"
        AppLanguage.ENGLISH -> "Iterate"
        AppLanguage.ARABIC -> "تكرار"
        AppLanguage.PORTUGUESE -> "Iterar"
        AppLanguage.SPANISH -> "Iterar"
        AppLanguage.FRENCH -> "Itérer"
        AppLanguage.GERMAN -> "Iterieren"
        AppLanguage.RUSSIAN -> "Итерация"
        AppLanguage.JAPANESE -> "反復"
        AppLanguage.KOREAN -> "반복"
    }
    val tagHide: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "隐藏"
        AppLanguage.ENGLISH -> "Hide"
        AppLanguage.ARABIC -> "إخفاء"
        AppLanguage.PORTUGUESE -> "Ocultar"
        AppLanguage.SPANISH -> "Ocultar"
        AppLanguage.FRENCH -> "Masquer"
        AppLanguage.GERMAN -> "Ausblenden"
        AppLanguage.RUSSIAN -> "Скрыть"
        AppLanguage.JAPANESE -> "非表示"
        AppLanguage.KOREAN -> "숨기기"
    }
    val tagStyle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "样式"
        AppLanguage.ENGLISH -> "Style"
        AppLanguage.ARABIC -> "نمط"
        AppLanguage.PORTUGUESE -> "Estilo"
        AppLanguage.SPANISH -> "Estilo"
        AppLanguage.FRENCH -> "Style"
        AppLanguage.GERMAN -> "Stil"
        AppLanguage.RUSSIAN -> "Стиль"
        AppLanguage.JAPANESE -> "スタイル"
        AppLanguage.KOREAN -> "스타일"
    }
    val tagDelete: String get() = when (Strings.lang) {
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
    val tagRemove: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Remove"
        AppLanguage.ENGLISH -> "Remove"
        AppLanguage.ARABIC -> "إزالة"
        AppLanguage.PORTUGUESE -> "Remover"
        AppLanguage.SPANISH -> "Quitar"
        AppLanguage.FRENCH -> "Supprimer"
        AppLanguage.GERMAN -> "Entfernen"
        AppLanguage.RUSSIAN -> "Убрать"
        AppLanguage.JAPANESE -> "削除"
        AppLanguage.KOREAN -> "제거"
    }
    val tagCreate: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "创建"
        AppLanguage.ENGLISH -> "Create"
        AppLanguage.ARABIC -> "إنشاء"
        AppLanguage.PORTUGUESE -> "Criar"
        AppLanguage.SPANISH -> "Crear"
        AppLanguage.FRENCH -> "Créer"
        AppLanguage.GERMAN -> "Erstellen"
        AppLanguage.RUSSIAN -> "Создать"
        AppLanguage.JAPANESE -> "作成"
        AppLanguage.KOREAN -> "생성"
    }
    val tagAdd: String get() = when (Strings.lang) {
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
    val tagText: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "文本"
        AppLanguage.ENGLISH -> "Text"
        AppLanguage.ARABIC -> "نص"
        AppLanguage.PORTUGUESE -> "Texto"
        AppLanguage.SPANISH -> "Texto"
        AppLanguage.FRENCH -> "Texte"
        AppLanguage.GERMAN -> "Text"
        AppLanguage.RUSSIAN -> "Текст"
        AppLanguage.JAPANESE -> "テキスト"
        AppLanguage.KOREAN -> "텍스트"
    }
    val tagModify: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "修改"
        AppLanguage.ENGLISH -> "Modify"
        AppLanguage.ARABIC -> "تعديل"
        AppLanguage.PORTUGUESE -> "Modificar"
        AppLanguage.SPANISH -> "Modificar"
        AppLanguage.FRENCH -> "Modifier"
        AppLanguage.GERMAN -> "Ändern"
        AppLanguage.RUSSIAN -> "Изменить"
        AppLanguage.JAPANESE -> "変更"
        AppLanguage.KOREAN -> "수정"
    }
    val tagAttribute: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "属性"
        AppLanguage.ENGLISH -> "Attribute"
        AppLanguage.ARABIC -> "سمة"
        AppLanguage.PORTUGUESE -> "Atributo"
        AppLanguage.SPANISH -> "Atributo"
        AppLanguage.FRENCH -> "Attribut"
        AppLanguage.GERMAN -> "Attribut"
        AppLanguage.RUSSIAN -> "Атрибут"
        AppLanguage.JAPANESE -> "属性"
        AppLanguage.KOREAN -> "속성"
    }
    val tagInsert: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "插入"
        AppLanguage.ENGLISH -> "Insert"
        AppLanguage.ARABIC -> "إدراج"
        AppLanguage.PORTUGUESE -> "Inserir"
        AppLanguage.SPANISH -> "Insertar"
        AppLanguage.FRENCH -> "Insérer"
        AppLanguage.GERMAN -> "Einfügen"
        AppLanguage.RUSSIAN -> "Вставить"
        AppLanguage.JAPANESE -> "挿入"
        AppLanguage.KOREAN -> "삽입"
    }
    val tagPosition: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "位置"
        AppLanguage.ENGLISH -> "Position"
        AppLanguage.ARABIC -> "موقع"
        AppLanguage.PORTUGUESE -> "Posição"
        AppLanguage.SPANISH -> "Posición"
        AppLanguage.FRENCH -> "Position"
        AppLanguage.GERMAN -> "Position"
        AppLanguage.RUSSIAN -> "Позиция"
        AppLanguage.JAPANESE -> "位置"
        AppLanguage.KOREAN -> "위치"
    }
    val tagClone: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "克隆"
        AppLanguage.ENGLISH -> "Clone"
        AppLanguage.ARABIC -> "استنساخ"
        AppLanguage.PORTUGUESE -> "Clonar"
        AppLanguage.SPANISH -> "Clonar"
        AppLanguage.FRENCH -> "Cloner"
        AppLanguage.GERMAN -> "Klonen"
        AppLanguage.RUSSIAN -> "Клонировать"
        AppLanguage.JAPANESE -> "複製"
        AppLanguage.KOREAN -> "복제"
    }
    val tagWrap: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "包裹"
        AppLanguage.ENGLISH -> "Wrap"
        AppLanguage.ARABIC -> "تغليف"
        AppLanguage.PORTUGUESE -> "Envolver"
        AppLanguage.SPANISH -> "Envolver"
        AppLanguage.FRENCH -> "Envelopper"
        AppLanguage.GERMAN -> "Umschließen"
        AppLanguage.RUSSIAN -> "Обёртка"
        AppLanguage.JAPANESE -> "ラップ"
        AppLanguage.KOREAN -> "래핑"
    }
    val tagStructure: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "结构"
        AppLanguage.ENGLISH -> "Structure"
        AppLanguage.ARABIC -> "هيكل"
        AppLanguage.PORTUGUESE -> "Estrutura"
        AppLanguage.SPANISH -> "Estructura"
        AppLanguage.FRENCH -> "Structure"
        AppLanguage.GERMAN -> "Struktur"
        AppLanguage.RUSSIAN -> "Структура"
        AppLanguage.JAPANESE -> "構造"
        AppLanguage.KOREAN -> "구조"
    }
    val tagReplace: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "替换"
        AppLanguage.ENGLISH -> "Replace"
        AppLanguage.ARABIC -> "استبدال"
        AppLanguage.PORTUGUESE -> "Substituir"
        AppLanguage.SPANISH -> "Reemplazar"
        AppLanguage.FRENCH -> "Remplacer"
        AppLanguage.GERMAN -> "Ersetzen"
        AppLanguage.RUSSIAN -> "Заменить"
        AppLanguage.JAPANESE -> "置換"
        AppLanguage.KOREAN -> "교체"
    }
    val tagCSS: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "CSS"
        AppLanguage.ENGLISH -> "CSS"
        AppLanguage.ARABIC -> "CSS"
        AppLanguage.PORTUGUESE -> "CSS"
        AppLanguage.SPANISH -> "CSS"
        AppLanguage.FRENCH -> "CSS"
        AppLanguage.GERMAN -> "CSS"
        AppLanguage.RUSSIAN -> "CSS"
        AppLanguage.JAPANESE -> "CSS"
        AppLanguage.KOREAN -> "CSS"
    }
    val tagInject: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "注入"
        AppLanguage.ENGLISH -> "Inject"
        AppLanguage.ARABIC -> "حقن"
        AppLanguage.PORTUGUESE -> "Injetar"
        AppLanguage.SPANISH -> "Inyectar"
        AppLanguage.FRENCH -> "Injecter"
        AppLanguage.GERMAN -> "Injizieren"
        AppLanguage.RUSSIAN -> "Внедрить"
        AppLanguage.JAPANESE -> "注入"
        AppLanguage.KOREAN -> "주입"
    }
    val tagInline: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "内联"
        AppLanguage.ENGLISH -> "Inline"
        AppLanguage.ARABIC -> "مضمن"
        AppLanguage.PORTUGUESE -> "Em Linha"
        AppLanguage.SPANISH -> "En Línea"
        AppLanguage.FRENCH -> "En Ligne"
        AppLanguage.GERMAN -> "Inline"
        AppLanguage.RUSSIAN -> "Встроенный"
        AppLanguage.JAPANESE -> "インライン"
        AppLanguage.KOREAN -> "인라인"
    }
    val tagClassName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "类名"
        AppLanguage.ENGLISH -> "Class"
        AppLanguage.ARABIC -> "فئة"
        AppLanguage.PORTUGUESE -> "Classe"
        AppLanguage.SPANISH -> "Clase"
        AppLanguage.FRENCH -> "Classe"
        AppLanguage.GERMAN -> "Klasse"
        AppLanguage.RUSSIAN -> "Класс"
        AppLanguage.JAPANESE -> "クラス"
        AppLanguage.KOREAN -> "클래스"
    }
    val tagWarm: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "暖色"
        AppLanguage.ENGLISH -> "Warm"
        AppLanguage.ARABIC -> "دافئ"
        AppLanguage.PORTUGUESE -> "Quente"
        AppLanguage.SPANISH -> "Cálido"
        AppLanguage.FRENCH -> "Chaud"
        AppLanguage.GERMAN -> "Warm"
        AppLanguage.RUSSIAN -> "Тёплый"
        AppLanguage.JAPANESE -> "ウォーム"
        AppLanguage.KOREAN -> "웜"
    }
    val tagGrayscale: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "灰度"
        AppLanguage.ENGLISH -> "Grayscale"
        AppLanguage.ARABIC -> "تدرج رمادي"
        AppLanguage.PORTUGUESE -> "Tons de Cinza"
        AppLanguage.SPANISH -> "Escala de Grises"
        AppLanguage.FRENCH -> "Niveaux de Gris"
        AppLanguage.GERMAN -> "Graustufen"
        AppLanguage.RUSSIAN -> "Оттенки серого"
        AppLanguage.JAPANESE -> "グレースケール"
        AppLanguage.KOREAN -> "그레이스케일"
    }
    val tagFilter: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "滤镜"
        AppLanguage.ENGLISH -> "Filter"
        AppLanguage.ARABIC -> "فلتر"
        AppLanguage.PORTUGUESE -> "Filtro"
        AppLanguage.SPANISH -> "Filtro"
        AppLanguage.FRENCH -> "Filtre"
        AppLanguage.GERMAN -> "Filter"
        AppLanguage.RUSSIAN -> "Фильтр"
        AppLanguage.JAPANESE -> "フィルター"
        AppLanguage.KOREAN -> "필터"
    }
    val tagFont: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "字体"
        AppLanguage.ENGLISH -> "Font"
        AppLanguage.ARABIC -> "خط"
        AppLanguage.PORTUGUESE -> "Fonte"
        AppLanguage.SPANISH -> "Fuente"
        AppLanguage.FRENCH -> "Police"
        AppLanguage.GERMAN -> "Schriftart"
        AppLanguage.RUSSIAN -> "Шрифт"
        AppLanguage.JAPANESE -> "フォント"
        AppLanguage.KOREAN -> "글꼴"
    }
    val tagSize: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "大小"
        AppLanguage.ENGLISH -> "Size"
        AppLanguage.ARABIC -> "حجم"
        AppLanguage.PORTUGUESE -> "Tamanho"
        AppLanguage.SPANISH -> "Tamaño"
        AppLanguage.FRENCH -> "Taille"
        AppLanguage.GERMAN -> "Größe"
        AppLanguage.RUSSIAN -> "Размер"
        AppLanguage.JAPANESE -> "サイズ"
        AppLanguage.KOREAN -> "크기"
    }
    val tagScrollbar: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "滚动条"
        AppLanguage.ENGLISH -> "Scrollbar"
        AppLanguage.ARABIC -> "شريط التمرير"
        AppLanguage.PORTUGUESE -> "Barra de Rolagem"
        AppLanguage.SPANISH -> "Barra de Desplazamiento"
        AppLanguage.FRENCH -> "Barre de Défilement"
        AppLanguage.GERMAN -> "Scrollleiste"
        AppLanguage.RUSSIAN -> "Полоса прокрутки"
        AppLanguage.JAPANESE -> "スクロールバー"
        AppLanguage.KOREAN -> "스크롤바"
    }
    val tagHighlight: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "高亮"
        AppLanguage.ENGLISH -> "Highlight"
        AppLanguage.ARABIC -> "تمييز"
        AppLanguage.PORTUGUESE -> "Destaque"
        AppLanguage.SPANISH -> "Resaltar"
        AppLanguage.FRENCH -> "Surligner"
        AppLanguage.GERMAN -> "Hervorheben"
        AppLanguage.RUSSIAN -> "Подсветка"
        AppLanguage.JAPANESE -> "ハイライト"
        AppLanguage.KOREAN -> "하이라이트"
    }
    val tagWidth: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "宽度"
        AppLanguage.ENGLISH -> "Width"
        AppLanguage.ARABIC -> "عرض"
        AppLanguage.PORTUGUESE -> "Largura"
        AppLanguage.SPANISH -> "Ancho"
        AppLanguage.FRENCH -> "Largeur"
        AppLanguage.GERMAN -> "Breite"
        AppLanguage.RUSSIAN -> "Ширина"
        AppLanguage.JAPANESE -> "幅"
        AppLanguage.KOREAN -> "너비"
    }
    val tagReading: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "阅读"
        AppLanguage.ENGLISH -> "Reading"
        AppLanguage.ARABIC -> "قراءة"
        AppLanguage.PORTUGUESE -> "Leitura"
        AppLanguage.SPANISH -> "Lectura"
        AppLanguage.FRENCH -> "Lecture"
        AppLanguage.GERMAN -> "Lesen"
        AppLanguage.RUSSIAN -> "Чтение"
        AppLanguage.JAPANESE -> "読書"
        AppLanguage.KOREAN -> "읽기"
    }
    val tagLineHeight: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "行高"
        AppLanguage.ENGLISH -> "Line Height"
        AppLanguage.ARABIC -> "ارتفاع السطر"
        AppLanguage.PORTUGUESE -> "Altura da Linha"
        AppLanguage.SPANISH -> "Altura de Línea"
        AppLanguage.FRENCH -> "Hauteur de Ligne"
        AppLanguage.GERMAN -> "Zeilenhöhe"
        AppLanguage.RUSSIAN -> "Высота строки"
        AppLanguage.JAPANESE -> "行の高さ"
        AppLanguage.KOREAN -> "줄 높이"
    }
    val tagClick: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "点击"
        AppLanguage.ENGLISH -> "Click"
        AppLanguage.ARABIC -> "نقر"
        AppLanguage.PORTUGUESE -> "Clique"
        AppLanguage.SPANISH -> "Clic"
        AppLanguage.FRENCH -> "Clic"
        AppLanguage.GERMAN -> "Klick"
        AppLanguage.RUSSIAN -> "Клик"
        AppLanguage.JAPANESE -> "クリック"
        AppLanguage.KOREAN -> "클릭"
    }
    val tagEvent: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "事件"
        AppLanguage.ENGLISH -> "Event"
        AppLanguage.ARABIC -> "حدث"
        AppLanguage.PORTUGUESE -> "Evento"
        AppLanguage.SPANISH -> "Evento"
        AppLanguage.FRENCH -> "Événement"
        AppLanguage.GERMAN -> "Ereignis"
        AppLanguage.RUSSIAN -> "Событие"
        AppLanguage.JAPANESE -> "イベント"
        AppLanguage.KOREAN -> "이벤트"
    }
    val tagKeyboard: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "键盘"
        AppLanguage.ENGLISH -> "Keyboard"
        AppLanguage.ARABIC -> "لوحة المفاتيح"
        AppLanguage.PORTUGUESE -> "Teclado"
        AppLanguage.SPANISH -> "Teclado"
        AppLanguage.FRENCH -> "Clavier"
        AppLanguage.GERMAN -> "Tastatur"
        AppLanguage.RUSSIAN -> "Клавиатура"
        AppLanguage.JAPANESE -> "キーボード"
        AppLanguage.KOREAN -> "키보드"
    }
    val tagShortcut: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "快捷键"
        AppLanguage.ENGLISH -> "Shortcut"
        AppLanguage.ARABIC -> "اختصار"
        AppLanguage.PORTUGUESE -> "Atalho"
        AppLanguage.SPANISH -> "Atajo"
        AppLanguage.FRENCH -> "Raccourci"
        AppLanguage.GERMAN -> "Tastenkürzel"
        AppLanguage.RUSSIAN -> "Горячая клавиша"
        AppLanguage.JAPANESE -> "ショートカット"
        AppLanguage.KOREAN -> "단축키"
    }
    val tagScroll: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "滚动"
        AppLanguage.ENGLISH -> "Scroll"
        AppLanguage.ARABIC -> "تمرير"
        AppLanguage.PORTUGUESE -> "Rolagem"
        AppLanguage.SPANISH -> "Desplazamiento"
        AppLanguage.FRENCH -> "Défilement"
        AppLanguage.GERMAN -> "Scrollen"
        AppLanguage.RUSSIAN -> "Прокрутка"
        AppLanguage.JAPANESE -> "スクロール"
        AppLanguage.KOREAN -> "스크롤"
    }
    val tagListen: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "监听"
        AppLanguage.ENGLISH -> "Listen"
        AppLanguage.ARABIC -> "استماع"
        AppLanguage.PORTUGUESE -> "Escutar"
        AppLanguage.SPANISH -> "Escuchar"
        AppLanguage.FRENCH -> "Écouter"
        AppLanguage.GERMAN -> "Lauschen"
        AppLanguage.RUSSIAN -> "Слушать"
        AppLanguage.JAPANESE -> "リッスン"
        AppLanguage.KOREAN -> "수신"
    }
    val tagDomChange: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "DOM变化"
        AppLanguage.ENGLISH -> "DOM Change"
        AppLanguage.ARABIC -> "تغيير DOM"
        AppLanguage.PORTUGUESE -> "Mudança DOM"
        AppLanguage.SPANISH -> "Cambio DOM"
        AppLanguage.FRENCH -> "Changement DOM"
        AppLanguage.GERMAN -> "DOM-Änderung"
        AppLanguage.RUSSIAN -> "Изменение DOM"
        AppLanguage.JAPANESE -> "DOM変更"
        AppLanguage.KOREAN -> "DOM 변경"
    }
    val tagDynamic: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "动态"
        AppLanguage.ENGLISH -> "Dynamic"
        AppLanguage.ARABIC -> "ديناميكي"
        AppLanguage.PORTUGUESE -> "Dinâmico"
        AppLanguage.SPANISH -> "Dinámico"
        AppLanguage.FRENCH -> "Dynamique"
        AppLanguage.GERMAN -> "Dynamisch"
        AppLanguage.RUSSIAN -> "Динамический"
        AppLanguage.JAPANESE -> "動的"
        AppLanguage.KOREAN -> "동적"
    }
    val tagWindow: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "窗口"
        AppLanguage.ENGLISH -> "Window"
        AppLanguage.ARABIC -> "نافذة"
        AppLanguage.PORTUGUESE -> "Janela"
        AppLanguage.SPANISH -> "Ventana"
        AppLanguage.FRENCH -> "Fenêtre"
        AppLanguage.GERMAN -> "Fenster"
        AppLanguage.RUSSIAN -> "Окно"
        AppLanguage.JAPANESE -> "ウィンドウ"
        AppLanguage.KOREAN -> "창"
    }
    val tagRightClick: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "右键"
        AppLanguage.ENGLISH -> "Right Click"
        AppLanguage.ARABIC -> "نقر يمين"
        AppLanguage.PORTUGUESE -> "Clique Direito"
        AppLanguage.SPANISH -> "Clic Derecho"
        AppLanguage.FRENCH -> "Clic Droit"
        AppLanguage.GERMAN -> "Rechtsklick"
        AppLanguage.RUSSIAN -> "Правый клик"
        AppLanguage.JAPANESE -> "右クリック"
        AppLanguage.KOREAN -> "오른쪽 클릭"
    }
    val tagMenu: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "菜单"
        AppLanguage.ENGLISH -> "Menu"
        AppLanguage.ARABIC -> "قائمة"
        AppLanguage.PORTUGUESE -> "Menu"
        AppLanguage.SPANISH -> "Menú"
        AppLanguage.FRENCH -> "Menu"
        AppLanguage.GERMAN -> "Menü"
        AppLanguage.RUSSIAN -> "Меню"
        AppLanguage.JAPANESE -> "メニュー"
        AppLanguage.KOREAN -> "메뉴"
    }
    val tagVisibility: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "可见性"
        AppLanguage.ENGLISH -> "Visibility"
        AppLanguage.ARABIC -> "الرؤية"
        AppLanguage.PORTUGUESE -> "Visibilidade"
        AppLanguage.SPANISH -> "Visibilidad"
        AppLanguage.FRENCH -> "Visibilité"
        AppLanguage.GERMAN -> "Sichtbarkeit"
        AppLanguage.RUSSIAN -> "Видимость"
        AppLanguage.JAPANESE -> "可視性"
        AppLanguage.KOREAN -> "표시 여부"
    }
    val tagBackground: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "后台"
        AppLanguage.ENGLISH -> "Background"
        AppLanguage.ARABIC -> "خلفية"
        AppLanguage.PORTUGUESE -> "Segundo Plano"
        AppLanguage.SPANISH -> "Segundo Plano"
        AppLanguage.FRENCH -> "Arrière-plan"
        AppLanguage.GERMAN -> "Hintergrund"
        AppLanguage.RUSSIAN -> "Фон"
        AppLanguage.JAPANESE -> "バックグラウンド"
        AppLanguage.KOREAN -> "백그라운드"
    }
    val tagClose: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Close"
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
    val tagTouch: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "触摸"
        AppLanguage.ENGLISH -> "Touch"
        AppLanguage.ARABIC -> "لمس"
        AppLanguage.PORTUGUESE -> "Toque"
        AppLanguage.SPANISH -> "Toque"
        AppLanguage.FRENCH -> "Toucher"
        AppLanguage.GERMAN -> "Berühren"
        AppLanguage.RUSSIAN -> "Касание"
        AppLanguage.JAPANESE -> "タッチ"
        AppLanguage.KOREAN -> "터치"
    }
    val tagGesture: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "手势"
        AppLanguage.ENGLISH -> "Gesture"
        AppLanguage.ARABIC -> "إيماءة"
        AppLanguage.PORTUGUESE -> "Gesto"
        AppLanguage.SPANISH -> "Gesto"
        AppLanguage.FRENCH -> "Geste"
        AppLanguage.GERMAN -> "Geste"
        AppLanguage.RUSSIAN -> "Жест"
        AppLanguage.JAPANESE -> "ジェスチャー"
        AppLanguage.KOREAN -> "제스처"
    }
    val tagLongPress: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "长按"
        AppLanguage.ENGLISH -> "Long Press"
        AppLanguage.ARABIC -> "ضغط مطول"
        AppLanguage.PORTUGUESE -> "Pressão Longa"
        AppLanguage.SPANISH -> "Pulsación Larga"
        AppLanguage.FRENCH -> "Appui Long"
        AppLanguage.GERMAN -> "Langer Druck"
        AppLanguage.RUSSIAN -> "Долгое нажатие"
        AppLanguage.JAPANESE -> "長押し"
        AppLanguage.KOREAN -> "길게 누르기"
    }
    val tagStorage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "存储"
        AppLanguage.ENGLISH -> "Storage"
        AppLanguage.ARABIC -> "تخزين"
        AppLanguage.PORTUGUESE -> "Armazenamento"
        AppLanguage.SPANISH -> "Almacenamiento"
        AppLanguage.FRENCH -> "Stockage"
        AppLanguage.GERMAN -> "Speicher"
        AppLanguage.RUSSIAN -> "Хранилище"
        AppLanguage.JAPANESE -> "ストレージ"
        AppLanguage.KOREAN -> "저장소"
    }
    val tagRead: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "读取"
        AppLanguage.ENGLISH -> "Read"
        AppLanguage.ARABIC -> "قراءة"
        AppLanguage.PORTUGUESE -> "Ler"
        AppLanguage.SPANISH -> "Leer"
        AppLanguage.FRENCH -> "Lire"
        AppLanguage.GERMAN -> "Lesen"
        AppLanguage.RUSSIAN -> "Чтение"
        AppLanguage.JAPANESE -> "読み取り"
        AppLanguage.KOREAN -> "읽기"
    }
    val tagSession: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "会话"
        AppLanguage.ENGLISH -> "Session"
        AppLanguage.ARABIC -> "جلسة"
        AppLanguage.PORTUGUESE -> "Sessão"
        AppLanguage.SPANISH -> "Sesión"
        AppLanguage.FRENCH -> "Session"
        AppLanguage.GERMAN -> "Sitzung"
        AppLanguage.RUSSIAN -> "Сессия"
        AppLanguage.JAPANESE -> "セッション"
        AppLanguage.KOREAN -> "세션"
    }
    val tagTemporary: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "临时"
        AppLanguage.ENGLISH -> "Temporary"
        AppLanguage.ARABIC -> "مؤقت"
        AppLanguage.PORTUGUESE -> "Temporário"
        AppLanguage.SPANISH -> "Temporal"
        AppLanguage.FRENCH -> "Temporaire"
        AppLanguage.GERMAN -> "Temporär"
        AppLanguage.RUSSIAN -> "Временный"
        AppLanguage.JAPANESE -> "一時的"
        AppLanguage.KOREAN -> "임시"
    }
    val tagCookie: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Cookie"
        AppLanguage.ENGLISH -> "Cookie"
        AppLanguage.ARABIC -> "كوكي"
        AppLanguage.PORTUGUESE -> "Cookie"
        AppLanguage.SPANISH -> "Cookie"
        AppLanguage.FRENCH -> "Cookie"
        AppLanguage.GERMAN -> "Cookie"
        AppLanguage.RUSSIAN -> "Cookie"
        AppLanguage.JAPANESE -> "Cookie"
        AppLanguage.KOREAN -> "Cookie"
    }
    val tagSetting: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "设置"
        AppLanguage.ENGLISH -> "Setting"
        AppLanguage.ARABIC -> "إعداد"
        AppLanguage.PORTUGUESE -> "Configuração"
        AppLanguage.SPANISH -> "Configuración"
        AppLanguage.FRENCH -> "Paramètre"
        AppLanguage.GERMAN -> "Einstellung"
        AppLanguage.RUSSIAN -> "Настройка"
        AppLanguage.JAPANESE -> "設定"
        AppLanguage.KOREAN -> "설정"
    }
    val tagIndexedDB: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "IndexedDB"
        AppLanguage.ENGLISH -> "IndexedDB"
        AppLanguage.ARABIC -> "IndexedDB"
        AppLanguage.PORTUGUESE -> "IndexedDB"
        AppLanguage.SPANISH -> "IndexedDB"
        AppLanguage.FRENCH -> "IndexedDB"
        AppLanguage.GERMAN -> "IndexedDB"
        AppLanguage.RUSSIAN -> "IndexedDB"
        AppLanguage.JAPANESE -> "IndexedDB"
        AppLanguage.KOREAN -> "IndexedDB"
    }
    val tagBigData: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "大数据"
        AppLanguage.ENGLISH -> "Big Data"
        AppLanguage.ARABIC -> "بيانات كبيرة"
        AppLanguage.PORTUGUESE -> "Big Data"
        AppLanguage.SPANISH -> "Big Data"
        AppLanguage.FRENCH -> "Big Data"
        AppLanguage.GERMAN -> "Big Data"
        AppLanguage.RUSSIAN -> "Большие данные"
        AppLanguage.JAPANESE -> "ビッグデータ"
        AppLanguage.KOREAN -> "빅데이터"
    }
    val tagGET: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "GET"
        AppLanguage.ENGLISH -> "GET"
        AppLanguage.ARABIC -> "GET"
        AppLanguage.PORTUGUESE -> "GET"
        AppLanguage.SPANISH -> "GET"
        AppLanguage.FRENCH -> "GET"
        AppLanguage.GERMAN -> "GET"
        AppLanguage.RUSSIAN -> "GET"
        AppLanguage.JAPANESE -> "GET"
        AppLanguage.KOREAN -> "GET"
    }
    val tagRequest: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "请求"
        AppLanguage.ENGLISH -> "Request"
        AppLanguage.ARABIC -> "طلب"
        AppLanguage.PORTUGUESE -> "Requisição"
        AppLanguage.SPANISH -> "Solicitud"
        AppLanguage.FRENCH -> "Requête"
        AppLanguage.GERMAN -> "Anfrage"
        AppLanguage.RUSSIAN -> "Запрос"
        AppLanguage.JAPANESE -> "リクエスト"
        AppLanguage.KOREAN -> "요청"
    }
    val tagPOST: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "POST"
        AppLanguage.ENGLISH -> "POST"
        AppLanguage.ARABIC -> "POST"
        AppLanguage.PORTUGUESE -> "POST"
        AppLanguage.SPANISH -> "POST"
        AppLanguage.FRENCH -> "POST"
        AppLanguage.GERMAN -> "POST"
        AppLanguage.RUSSIAN -> "POST"
        AppLanguage.JAPANESE -> "POST"
        AppLanguage.KOREAN -> "POST"
    }
    val tagSubmit: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "提交"
        AppLanguage.ENGLISH -> "Submit"
        AppLanguage.ARABIC -> "إرسال"
        AppLanguage.PORTUGUESE -> "Enviar"
        AppLanguage.SPANISH -> "Enviar"
        AppLanguage.FRENCH -> "Soumettre"
        AppLanguage.GERMAN -> "Absenden"
        AppLanguage.RUSSIAN -> "Отправить"
        AppLanguage.JAPANESE -> "送信"
        AppLanguage.KOREAN -> "제출"
    }
    val tagTimeout: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "超时"
        AppLanguage.ENGLISH -> "Timeout"
        AppLanguage.ARABIC -> "مهلة"
        AppLanguage.PORTUGUESE -> "Tempo Limite"
        AppLanguage.SPANISH -> "Tiempo de Espera"
        AppLanguage.FRENCH -> "Délai d'Attente"
        AppLanguage.GERMAN -> "Zeitüberschreitung"
        AppLanguage.RUSSIAN -> "Тайм-аут"
        AppLanguage.JAPANESE -> "タイムアウト"
        AppLanguage.KOREAN -> "시간 초과"
    }
    val tagRetry: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Retry"
        AppLanguage.ENGLISH -> "Retry"
        AppLanguage.ARABIC -> "إعادة المحاولة"
        AppLanguage.PORTUGUESE -> "Repetir"
        AppLanguage.SPANISH -> "Reintentar"
        AppLanguage.FRENCH -> "Réessayer"
        AppLanguage.GERMAN -> "Erneut versuchen"
        AppLanguage.RUSSIAN -> "Повторить"
        AppLanguage.JAPANESE -> "リトライ"
        AppLanguage.KOREAN -> "재시도"
    }
    val tagJSONP: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "JSONP"
        AppLanguage.ENGLISH -> "JSONP"
        AppLanguage.ARABIC -> "JSONP"
        AppLanguage.PORTUGUESE -> "JSONP"
        AppLanguage.SPANISH -> "JSONP"
        AppLanguage.FRENCH -> "JSONP"
        AppLanguage.GERMAN -> "JSONP"
        AppLanguage.RUSSIAN -> "JSONP"
        AppLanguage.JAPANESE -> "JSONP"
        AppLanguage.KOREAN -> "JSONP"
    }
    val tagCrossDomain: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "跨域"
        AppLanguage.ENGLISH -> "Cross Domain"
        AppLanguage.ARABIC -> "عبر النطاق"
        AppLanguage.PORTUGUESE -> "Cross-Domain"
        AppLanguage.SPANISH -> "Cross-Domain"
        AppLanguage.FRENCH -> "Cross-Domain"
        AppLanguage.GERMAN -> "Cross-Domain"
        AppLanguage.RUSSIAN -> "Кросс-домен"
        AppLanguage.JAPANESE -> "クロスドメイン"
        AppLanguage.KOREAN -> "크로스 도메인"
    }
    val tagTable: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "表格"
        AppLanguage.ENGLISH -> "Table"
        AppLanguage.ARABIC -> "جدول"
        AppLanguage.PORTUGUESE -> "Tabela"
        AppLanguage.SPANISH -> "Tabla"
        AppLanguage.FRENCH -> "Tableau"
        AppLanguage.GERMAN -> "Tabelle"
        AppLanguage.RUSSIAN -> "Таблица"
        AppLanguage.JAPANESE -> "テーブル"
        AppLanguage.KOREAN -> "테이블"
    }
    val tagExtract: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "提取"
        AppLanguage.ENGLISH -> "Extract"
        AppLanguage.ARABIC -> "استخراج"
        AppLanguage.PORTUGUESE -> "Extrair"
        AppLanguage.SPANISH -> "Extraer"
        AppLanguage.FRENCH -> "Extraire"
        AppLanguage.GERMAN -> "Extrahieren"
        AppLanguage.RUSSIAN -> "Извлечь"
        AppLanguage.JAPANESE -> "抽出"
        AppLanguage.KOREAN -> "추출"
    }
    val tagJSON: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "JSON"
        AppLanguage.ENGLISH -> "JSON"
        AppLanguage.ARABIC -> "JSON"
        AppLanguage.PORTUGUESE -> "JSON"
        AppLanguage.SPANISH -> "JSON"
        AppLanguage.FRENCH -> "JSON"
        AppLanguage.GERMAN -> "JSON"
        AppLanguage.RUSSIAN -> "JSON"
        AppLanguage.JAPANESE -> "JSON"
        AppLanguage.KOREAN -> "JSON"
    }
    val tagCSV: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "CSV"
        AppLanguage.ENGLISH -> "CSV"
        AppLanguage.ARABIC -> "CSV"
        AppLanguage.PORTUGUESE -> "CSV"
        AppLanguage.SPANISH -> "CSV"
        AppLanguage.FRENCH -> "CSV"
        AppLanguage.GERMAN -> "CSV"
        AppLanguage.RUSSIAN -> "CSV"
        AppLanguage.JAPANESE -> "CSV"
        AppLanguage.KOREAN -> "CSV"
    }
    val tagURL: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "URL"
        AppLanguage.ENGLISH -> "URL"
        AppLanguage.ARABIC -> "URL"
        AppLanguage.PORTUGUESE -> "URL"
        AppLanguage.SPANISH -> "URL"
        AppLanguage.FRENCH -> "URL"
        AppLanguage.GERMAN -> "URL"
        AppLanguage.RUSSIAN -> "URL"
        AppLanguage.JAPANESE -> "URL"
        AppLanguage.KOREAN -> "URL"
    }
    val tagParse: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "解析"
        AppLanguage.ENGLISH -> "Parse"
        AppLanguage.ARABIC -> "تحليل"
        AppLanguage.PORTUGUESE -> "Analisar"
        AppLanguage.SPANISH -> "Analizar"
        AppLanguage.FRENCH -> "Analyser"
        AppLanguage.GERMAN -> "Analysieren"
        AppLanguage.RUSSIAN -> "Разбор"
        AppLanguage.JAPANESE -> "解析"
        AppLanguage.KOREAN -> "파싱"
    }
    val tagBuild: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "构建"
        AppLanguage.ENGLISH -> "Build"
        AppLanguage.ARABIC -> "بناء"
        AppLanguage.PORTUGUESE -> "Construir"
        AppLanguage.SPANISH -> "Construir"
        AppLanguage.FRENCH -> "Construire"
        AppLanguage.GERMAN -> "Erstellen"
        AppLanguage.RUSSIAN -> "Сборка"
        AppLanguage.JAPANESE -> "ビルド"
        AppLanguage.KOREAN -> "빌드"
    }
    val tagPopup: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "弹窗"
        AppLanguage.ENGLISH -> "Popup"
        AppLanguage.ARABIC -> "نافذة منبثقة"
        AppLanguage.PORTUGUESE -> "Pop-up"
        AppLanguage.SPANISH -> "Emergente"
        AppLanguage.FRENCH -> "Pop-up"
        AppLanguage.GERMAN -> "Popup"
        AppLanguage.RUSSIAN -> "Всплывающее окно"
        AppLanguage.JAPANESE -> "ポップアップ"
        AppLanguage.KOREAN -> "팝업"
    }
    val tagDialog: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "对话框"
        AppLanguage.ENGLISH -> "Dialog"
        AppLanguage.ARABIC -> "حوار"
        AppLanguage.PORTUGUESE -> "Diálogo"
        AppLanguage.SPANISH -> "Diálogo"
        AppLanguage.FRENCH -> "Dialogue"
        AppLanguage.GERMAN -> "Dialog"
        AppLanguage.RUSSIAN -> "Диалог"
        AppLanguage.JAPANESE -> "ダイアログ"
        AppLanguage.KOREAN -> "대화상자"
    }
    val tagProgress: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "进度"
        AppLanguage.ENGLISH -> "Progress"
        AppLanguage.ARABIC -> "تقدم"
        AppLanguage.PORTUGUESE -> "Progresso"
        AppLanguage.SPANISH -> "Progreso"
        AppLanguage.FRENCH -> "Progression"
        AppLanguage.GERMAN -> "Fortschritt"
        AppLanguage.RUSSIAN -> "Прогресс"
        AppLanguage.JAPANESE -> "進捗"
        AppLanguage.KOREAN -> "진행률"
    }
    val tagLoading: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "加载"
        AppLanguage.ENGLISH -> "Loading"
        AppLanguage.ARABIC -> "تحميل"
        AppLanguage.PORTUGUESE -> "Carregando"
        AppLanguage.SPANISH -> "Cargando"
        AppLanguage.FRENCH -> "Chargement"
        AppLanguage.GERMAN -> "Laden"
        AppLanguage.RUSSIAN -> "Загрузка"
        AppLanguage.JAPANESE -> "読み込み中"
        AppLanguage.KOREAN -> "로딩"
    }
    val tagAnimation: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "动画"
        AppLanguage.ENGLISH -> "Animation"
        AppLanguage.ARABIC -> "رسوم متحركة"
        AppLanguage.PORTUGUESE -> "Animação"
        AppLanguage.SPANISH -> "Animación"
        AppLanguage.FRENCH -> "Animation"
        AppLanguage.GERMAN -> "Animation"
        AppLanguage.RUSSIAN -> "Анимация"
        AppLanguage.JAPANESE -> "アニメーション"
        AppLanguage.KOREAN -> "애니메이션"
    }
    val tagNotification: String get() = when (Strings.lang) {
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
    val tagSnackbar: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Snackbar"
        AppLanguage.ENGLISH -> "Snackbar"
        AppLanguage.ARABIC -> "Snackbar"
        AppLanguage.PORTUGUESE -> "Snackbar"
        AppLanguage.SPANISH -> "Snackbar"
        AppLanguage.FRENCH -> "Snackbar"
        AppLanguage.GERMAN -> "Snackbar"
        AppLanguage.RUSSIAN -> "Snackbar"
        AppLanguage.JAPANESE -> "Snackbar"
        AppLanguage.KOREAN -> "Snackbar"
    }
    val tagToolbar: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "工具栏"
        AppLanguage.ENGLISH -> "Toolbar"
        AppLanguage.ARABIC -> "شريط الأدوات"
        AppLanguage.PORTUGUESE -> "Barra de Ferramentas"
        AppLanguage.SPANISH -> "Barra de Herramientas"
        AppLanguage.FRENCH -> "Barre d'Outils"
        AppLanguage.GERMAN -> "Symbolleiste"
        AppLanguage.RUSSIAN -> "Панель инструментов"
        AppLanguage.JAPANESE -> "ツールバー"
        AppLanguage.KOREAN -> "툴바"
    }
    val tagSidebar: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "侧边栏"
        AppLanguage.ENGLISH -> "Sidebar"
        AppLanguage.ARABIC -> "الشريط الجانبي"
        AppLanguage.PORTUGUESE -> "Barra Lateral"
        AppLanguage.SPANISH -> "Barra Lateral"
        AppLanguage.FRENCH -> "Barre Latérale"
        AppLanguage.GERMAN -> "Seitenleiste"
        AppLanguage.RUSSIAN -> "Боковая панель"
        AppLanguage.JAPANESE -> "サイドバー"
        AppLanguage.KOREAN -> "사이드바"
    }
    val tagPanel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "面板"
        AppLanguage.ENGLISH -> "Panel"
        AppLanguage.ARABIC -> "لوحة"
        AppLanguage.PORTUGUESE -> "Painel"
        AppLanguage.SPANISH -> "Painel"
        AppLanguage.FRENCH -> "Panneau"
        AppLanguage.GERMAN -> "Panel"
        AppLanguage.RUSSIAN -> "Панель"
        AppLanguage.JAPANESE -> "パネル"
        AppLanguage.KOREAN -> "패널"
    }
    val tagDrag: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "拖动"
        AppLanguage.ENGLISH -> "Drag"
        AppLanguage.ARABIC -> "سحب"
        AppLanguage.PORTUGUESE -> "Arrastar"
        AppLanguage.SPANISH -> "Arrastrar"
        AppLanguage.FRENCH -> "Glisser"
        AppLanguage.GERMAN -> "Ziehen"
        AppLanguage.RUSSIAN -> "Перетаскивание"
        AppLanguage.JAPANESE -> "ドラッグ"
        AppLanguage.KOREAN -> "드래그"
    }
    val tagInteraction: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "交互"
        AppLanguage.ENGLISH -> "Interaction"
        AppLanguage.ARABIC -> "تفاعل"
        AppLanguage.PORTUGUESE -> "Interação"
        AppLanguage.SPANISH -> "Interacción"
        AppLanguage.FRENCH -> "Interaction"
        AppLanguage.GERMAN -> "Interaktion"
        AppLanguage.RUSSIAN -> "Взаимодействие"
        AppLanguage.JAPANESE -> "インタラクション"
        AppLanguage.KOREAN -> "인터랙션"
    }
    val tagPlayer: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "播放器"
        AppLanguage.ENGLISH -> "Player"
        AppLanguage.ARABIC -> "مشغل"
        AppLanguage.PORTUGUESE -> "Reprodutor"
        AppLanguage.SPANISH -> "Reproductor"
        AppLanguage.FRENCH -> "Lecteur"
        AppLanguage.GERMAN -> "Player"
        AppLanguage.RUSSIAN -> "Плеер"
        AppLanguage.JAPANESE -> "プレーヤー"
        AppLanguage.KOREAN -> "플레이어"
    }
    val tagBadge: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "角标"
        AppLanguage.ENGLISH -> "Badge"
        AppLanguage.ARABIC -> "شارة"
        AppLanguage.PORTUGUESE -> "Emblema"
        AppLanguage.SPANISH -> "Insignia"
        AppLanguage.FRENCH -> "Badge"
        AppLanguage.GERMAN -> "Abzeichen"
        AppLanguage.RUSSIAN -> "Значок"
        AppLanguage.JAPANESE -> "バッジ"
        AppLanguage.KOREAN -> "배지"
    }
    val tagNumber: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "数字"
        AppLanguage.ENGLISH -> "Number"
        AppLanguage.ARABIC -> "رقم"
        AppLanguage.PORTUGUESE -> "Número"
        AppLanguage.SPANISH -> "Número"
        AppLanguage.FRENCH -> "Nombre"
        AppLanguage.GERMAN -> "Zahl"
        AppLanguage.RUSSIAN -> "Число"
        AppLanguage.JAPANESE -> "数値"
        AppLanguage.KOREAN -> "숫자"
    }
    val tagBanner: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "横幅"
        AppLanguage.ENGLISH -> "Banner"
        AppLanguage.ARABIC -> "لافتة"
        AppLanguage.PORTUGUESE -> "Banner"
        AppLanguage.SPANISH -> "Banner"
        AppLanguage.FRENCH -> "Bannière"
        AppLanguage.GERMAN -> "Banner"
        AppLanguage.RUSSIAN -> "Баннер"
        AppLanguage.JAPANESE -> "バナー"
        AppLanguage.KOREAN -> "배너"
    }
    val tagReminder: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "提醒"
        AppLanguage.ENGLISH -> "Reminder"
        AppLanguage.ARABIC -> "تذكير"
        AppLanguage.PORTUGUESE -> "Lembrete"
        AppLanguage.SPANISH -> "Recordatorio"
        AppLanguage.FRENCH -> "Rappel"
        AppLanguage.GERMAN -> "Erinnerung"
        AppLanguage.RUSSIAN -> "Напоминание"
        AppLanguage.JAPANESE -> "リマインダー"
        AppLanguage.KOREAN -> "알림"
    }
    val tagTop: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "顶部"
        AppLanguage.ENGLISH -> "Top"
        AppLanguage.ARABIC -> "أعلى"
        AppLanguage.PORTUGUESE -> "Topo"
        AppLanguage.SPANISH -> "Arriba"
        AppLanguage.FRENCH -> "Haut"
        AppLanguage.GERMAN -> "Oben"
        AppLanguage.RUSSIAN -> "Наверх"
        AppLanguage.JAPANESE -> "上部"
        AppLanguage.KOREAN -> "맨 위"
    }
    val tagBottom: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "底部"
        AppLanguage.ENGLISH -> "Bottom"
        AppLanguage.ARABIC -> "أسفل"
        AppLanguage.PORTUGUESE -> "Inferior"
        AppLanguage.SPANISH -> "Inferior"
        AppLanguage.FRENCH -> "Bas"
        AppLanguage.GERMAN -> "Unten"
        AppLanguage.RUSSIAN -> "Вниз"
        AppLanguage.JAPANESE -> "下部"
        AppLanguage.KOREAN -> "맨 아래"
    }
    val tagBackToTop: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "返回顶部"
        AppLanguage.ENGLISH -> "Back to Top"
        AppLanguage.ARABIC -> "العودة للأعلى"
        AppLanguage.PORTUGUESE -> "Voltar ao Topo"
        AppLanguage.SPANISH -> "Volver Arriba"
        AppLanguage.FRENCH -> "Retour en Haut"
        AppLanguage.GERMAN -> "Nach Oben"
        AppLanguage.RUSSIAN -> "Наверх"
        AppLanguage.JAPANESE -> "トップへ戻る"
        AppLanguage.KOREAN -> "맨 위로"
    }
    val tagNavigation: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "导航"
        AppLanguage.ENGLISH -> "Navigation"
        AppLanguage.ARABIC -> "تنقل"
        AppLanguage.PORTUGUESE -> "Navegação"
        AppLanguage.SPANISH -> "Navegación"
        AppLanguage.FRENCH -> "Navigation"
        AppLanguage.GERMAN -> "Navigation"
        AppLanguage.RUSSIAN -> "Навигация"
        AppLanguage.JAPANESE -> "ナビゲーション"
        AppLanguage.KOREAN -> "내비게이션"
    }
    val tagForm: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "表单"
        AppLanguage.ENGLISH -> "Form"
        AppLanguage.ARABIC -> "نموذج"
        AppLanguage.PORTUGUESE -> "Formulário"
        AppLanguage.SPANISH -> "Formulario"
        AppLanguage.FRENCH -> "Formulaire"
        AppLanguage.GERMAN -> "Formular"
        AppLanguage.RUSSIAN -> "Форма"
        AppLanguage.JAPANESE -> "フォーム"
        AppLanguage.KOREAN -> "폼"
    }
    val tagFill: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "填充"
        AppLanguage.ENGLISH -> "Fill"
        AppLanguage.ARABIC -> "تعبئة"
        AppLanguage.PORTUGUESE -> "Preencher"
        AppLanguage.SPANISH -> "Rellenar"
        AppLanguage.FRENCH -> "Remplir"
        AppLanguage.GERMAN -> "Ausfüllen"
        AppLanguage.RUSSIAN -> "Заполнить"
        AppLanguage.JAPANESE -> "入力"
        AppLanguage.KOREAN -> "채우기"
    }
    val tagGet: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "获取"
        AppLanguage.ENGLISH -> "Get"
        AppLanguage.ARABIC -> "الحصول على"
        AppLanguage.PORTUGUESE -> "Obter"
        AppLanguage.SPANISH -> "Obtener"
        AppLanguage.FRENCH -> "Obtenir"
        AppLanguage.GERMAN -> "Abrufen"
        AppLanguage.RUSSIAN -> "Получить"
        AppLanguage.JAPANESE -> "取得"
        AppLanguage.KOREAN -> "가져오기"
    }
    val tagValidate: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "验证"
        AppLanguage.ENGLISH -> "Validate"
        AppLanguage.ARABIC -> "تحقق"
        AppLanguage.PORTUGUESE -> "Validar"
        AppLanguage.SPANISH -> "Validar"
        AppLanguage.FRENCH -> "Valider"
        AppLanguage.GERMAN -> "Validieren"
        AppLanguage.RUSSIAN -> "Проверить"
        AppLanguage.JAPANESE -> "検証"
        AppLanguage.KOREAN -> "검증"
    }
    val tagIntercept: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "拦截"
        AppLanguage.ENGLISH -> "Intercept"
        AppLanguage.ARABIC -> "اعتراض"
        AppLanguage.PORTUGUESE -> "Interceptar"
        AppLanguage.SPANISH -> "Interceptar"
        AppLanguage.FRENCH -> "Intercepter"
        AppLanguage.GERMAN -> "Abfangen"
        AppLanguage.RUSSIAN -> "Перехватить"
        AppLanguage.JAPANESE -> "インターセプト"
        AppLanguage.KOREAN -> "가로채기"
    }
    val tagClear: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "清空"
        AppLanguage.ENGLISH -> "Clear"
        AppLanguage.ARABIC -> "مسح"
        AppLanguage.PORTUGUESE -> "Limpar"
        AppLanguage.SPANISH -> "Limpiar"
        AppLanguage.FRENCH -> "Effacer"
        AppLanguage.GERMAN -> "Leeren"
        AppLanguage.RUSSIAN -> "Очистить"
        AppLanguage.JAPANESE -> "クリア"
        AppLanguage.KOREAN -> "지우기"
    }
    val tagPassword: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "密码"
        AppLanguage.ENGLISH -> "Password"
        AppLanguage.ARABIC -> "كلمة مرور"
        AppLanguage.PORTUGUESE -> "Senha"
        AppLanguage.SPANISH -> "Contraseña"
        AppLanguage.FRENCH -> "Mot de passe"
        AppLanguage.GERMAN -> "Passwort"
        AppLanguage.RUSSIAN -> "Пароль"
        AppLanguage.JAPANESE -> "パスワード"
        AppLanguage.KOREAN -> "비밀번호"
    }
    val tagToggle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "切换"
        AppLanguage.ENGLISH -> "Toggle"
        AppLanguage.ARABIC -> "تبديل"
        AppLanguage.PORTUGUESE -> "Alternar"
        AppLanguage.SPANISH -> "Alternar"
        AppLanguage.FRENCH -> "Basculer"
        AppLanguage.GERMAN -> "Umschalten"
        AppLanguage.RUSSIAN -> "Переключить"
        AppLanguage.JAPANESE -> "切り替え"
        AppLanguage.KOREAN -> "토글"
    }
    val tagZoom: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "放大"
        AppLanguage.ENGLISH -> "Zoom"
        AppLanguage.ARABIC -> "تكبير"
        AppLanguage.PORTUGUESE -> "Zoom"
        AppLanguage.SPANISH -> "Zoom"
        AppLanguage.FRENCH -> "Zoom"
        AppLanguage.GERMAN -> "Zoom"
        AppLanguage.RUSSIAN -> "Масштаб"
        AppLanguage.JAPANESE -> "ズーム"
        AppLanguage.KOREAN -> "확대"
    }
    val tagAudio: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "音频"
        AppLanguage.ENGLISH -> "Audio"
        AppLanguage.ARABIC -> "صوت"
        AppLanguage.PORTUGUESE -> "Áudio"
        AppLanguage.SPANISH -> "Audio"
        AppLanguage.FRENCH -> "Audio"
        AppLanguage.GERMAN -> "Audio"
        AppLanguage.RUSSIAN -> "Аудио"
        AppLanguage.JAPANESE -> "オーディオ"
        AppLanguage.KOREAN -> "오디오"
    }
    val tagControl: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "控制"
        AppLanguage.ENGLISH -> "Control"
        AppLanguage.ARABIC -> "تحكم"
        AppLanguage.PORTUGUESE -> "Controle"
        AppLanguage.SPANISH -> "Controle"
        AppLanguage.FRENCH -> "Contrôle"
        AppLanguage.GERMAN -> "Steuerung"
        AppLanguage.RUSSIAN -> "Управление"
        AppLanguage.JAPANESE -> "コントロール"
        AppLanguage.KOREAN -> "컨트롤"
    }
    val tagLazyLoad: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "懒加载"
        AppLanguage.ENGLISH -> "Lazy Load"
        AppLanguage.ARABIC -> "تحميل كسول"
        AppLanguage.PORTUGUESE -> "Carregamento Tardio"
        AppLanguage.SPANISH -> "Carga Diferida"
        AppLanguage.FRENCH -> "Chargement Différé"
        AppLanguage.GERMAN -> "Verzögertes Laden"
        AppLanguage.RUSSIAN -> "Ленивая Загрузка"
        AppLanguage.JAPANESE -> "遅延読み込み"
        AppLanguage.KOREAN -> "지연 로딩"
    }
    val tagFullscreen: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "全屏"
        AppLanguage.ENGLISH -> "Fullscreen"
        AppLanguage.ARABIC -> "ملء الشاشة"
        AppLanguage.PORTUGUESE -> "Tela Cheia"
        AppLanguage.SPANISH -> "Pantalla Completa"
        AppLanguage.FRENCH -> "Plein Écran"
        AppLanguage.GERMAN -> "Vollbild"
        AppLanguage.RUSSIAN -> "Полный Экран"
        AppLanguage.JAPANESE -> "全画面"
        AppLanguage.KOREAN -> "전체 화면"
    }
    val tagSimplify: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "简化"
        AppLanguage.ENGLISH -> "Simplify"
        AppLanguage.ARABIC -> "تبسيط"
        AppLanguage.PORTUGUESE -> "Simplificar"
        AppLanguage.SPANISH -> "Simplificar"
        AppLanguage.FRENCH -> "Simplifier"
        AppLanguage.GERMAN -> "Vereinfachen"
        AppLanguage.RUSSIAN -> "Упростить"
        AppLanguage.JAPANESE -> "簡素化"
        AppLanguage.KOREAN -> "단순화"
    }
    val tagUnlock: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "解锁"
        AppLanguage.ENGLISH -> "Unlock"
        AppLanguage.ARABIC -> "فتح"
        AppLanguage.PORTUGUESE -> "Desbloquear"
        AppLanguage.SPANISH -> "Desbloquear"
        AppLanguage.FRENCH -> "Déverrouiller"
        AppLanguage.GERMAN -> "Entsperren"
        AppLanguage.RUSSIAN -> "Разблокировать"
        AppLanguage.JAPANESE -> "ロック解除"
        AppLanguage.KOREAN -> "잠금 해제"
    }
    val tagPrint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "打印"
        AppLanguage.ENGLISH -> "Print"
        AppLanguage.ARABIC -> "طباعة"
        AppLanguage.PORTUGUESE -> "Imprimir"
        AppLanguage.SPANISH -> "Imprimir"
        AppLanguage.FRENCH -> "Imprimer"
        AppLanguage.GERMAN -> "Drucken"
        AppLanguage.RUSSIAN -> "Печать"
        AppLanguage.JAPANESE -> "印刷"
        AppLanguage.KOREAN -> "인쇄"
    }
    val tagOptimize: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "优化"
        AppLanguage.ENGLISH -> "Optimize"
        AppLanguage.ARABIC -> "تحسين"
        AppLanguage.PORTUGUESE -> "Otimizar"
        AppLanguage.SPANISH -> "Optimizar"
        AppLanguage.FRENCH -> "Optimiser"
        AppLanguage.GERMAN -> "Optimieren"
        AppLanguage.RUSSIAN -> "Оптимизировать"
        AppLanguage.JAPANESE -> "最適化"
        AppLanguage.KOREAN -> "최적화"
    }
    val tagVoice: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "语音"
        AppLanguage.ENGLISH -> "Voice"
        AppLanguage.ARABIC -> "صوت"
        AppLanguage.PORTUGUESE -> "Voz"
        AppLanguage.SPANISH -> "Voz"
        AppLanguage.FRENCH -> "Voix"
        AppLanguage.GERMAN -> "Stimme"
        AppLanguage.RUSSIAN -> "Голос"
        AppLanguage.JAPANESE -> "音声"
        AppLanguage.KOREAN -> "음성"
    }
    val tagReadAloud: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "朗读"
        AppLanguage.ENGLISH -> "Read Aloud"
        AppLanguage.ARABIC -> "قراءة بصوت عالٍ"
        AppLanguage.PORTUGUESE -> "Ler em Voz Alta"
        AppLanguage.SPANISH -> "Leer en Voz Alta"
        AppLanguage.FRENCH -> "Lire à Voix Haute"
        AppLanguage.GERMAN -> "Vorlesen"
        AppLanguage.RUSSIAN -> "Чтение вслух"
        AppLanguage.JAPANESE -> "読み上げ"
        AppLanguage.KOREAN -> "소리 내어 읽기"
    }
    val tagStats: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "统计"
        AppLanguage.ENGLISH -> "Stats"
        AppLanguage.ARABIC -> "إحصائيات"
        AppLanguage.PORTUGUESE -> "Estatísticas"
        AppLanguage.SPANISH -> "Estadísticas"
        AppLanguage.FRENCH -> "Statistiques"
        AppLanguage.GERMAN -> "Statistiken"
        AppLanguage.RUSSIAN -> "Статистика"
        AppLanguage.JAPANESE -> "統計"
        AppLanguage.KOREAN -> "통계"
    }
    val tagWordCount: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "字数"
        AppLanguage.ENGLISH -> "Word Count"
        AppLanguage.ARABIC -> "عدد الكلمات"
        AppLanguage.PORTUGUESE -> "Contagem de Palavras"
        AppLanguage.SPANISH -> "Conteo de Palabras"
        AppLanguage.FRENCH -> "Nombre de Mots"
        AppLanguage.GERMAN -> "Wortanzahl"
        AppLanguage.RUSSIAN -> "Количество Слов"
        AppLanguage.JAPANESE -> "単語数"
        AppLanguage.KOREAN -> "단어 수"
    }
    val tagSearch: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "搜索"
        AppLanguage.ENGLISH -> "Search"
        AppLanguage.ARABIC -> "بحث"
        AppLanguage.PORTUGUESE -> "Pesquisar"
        AppLanguage.SPANISH -> "Buscar"
        AppLanguage.FRENCH -> "Rechercher"
        AppLanguage.GERMAN -> "Suchen"
        AppLanguage.RUSSIAN -> "Поиск"
        AppLanguage.JAPANESE -> "検索"
        AppLanguage.KOREAN -> "검색"
    }
    val tagKeyword: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "关键词"
        AppLanguage.ENGLISH -> "Keyword"
        AppLanguage.ARABIC -> "كلمة مفتاحية"
        AppLanguage.PORTUGUESE -> "Palavra-chave"
        AppLanguage.SPANISH -> "Palabra Clave"
        AppLanguage.FRENCH -> "Mot-clé"
        AppLanguage.GERMAN -> "Schlüsselwort"
        AppLanguage.RUSSIAN -> "Ключевое слово"
        AppLanguage.JAPANESE -> "キーワード"
        AppLanguage.KOREAN -> "키워드"
    }
    val tagEmptyElement: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "空元素"
        AppLanguage.ENGLISH -> "Empty Element"
        AppLanguage.ARABIC -> "عنصر فارغ"
        AppLanguage.PORTUGUESE -> "Elemento Vazio"
        AppLanguage.SPANISH -> "Elemento Vacío"
        AppLanguage.FRENCH -> "Élément Vide"
        AppLanguage.GERMAN -> "Leeres Element"
        AppLanguage.RUSSIAN -> "Пустой Элемент"
        AppLanguage.JAPANESE -> "空要素"
        AppLanguage.KOREAN -> "빈 요소"
    }
    val tagClean: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "清理"
        AppLanguage.ENGLISH -> "Clean"
        AppLanguage.ARABIC -> "تنظيف"
        AppLanguage.PORTUGUESE -> "Limpar"
        AppLanguage.SPANISH -> "Limpiar"
        AppLanguage.FRENCH -> "Nettoyer"
        AppLanguage.GERMAN -> "Bereinigen"
        AppLanguage.RUSSIAN -> "Очистить"
        AppLanguage.JAPANESE -> "クリーンアップ"
        AppLanguage.KOREAN -> "정리"
    }
    val tagComment: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "评论"
        AppLanguage.ENGLISH -> "Comment"
        AppLanguage.ARABIC -> "تعليق"
        AppLanguage.PORTUGUESE -> "Comentário"
        AppLanguage.SPANISH -> "Comentario"
        AppLanguage.FRENCH -> "Commentaire"
        AppLanguage.GERMAN -> "Kommentar"
        AppLanguage.RUSSIAN -> "Комментарий"
        AppLanguage.JAPANESE -> "コメント"
        AppLanguage.KOREAN -> "댓글"
    }
    val tagPrevent: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "阻止"
        AppLanguage.ENGLISH -> "Prevent"
        AppLanguage.ARABIC -> "منع"
        AppLanguage.PORTUGUESE -> "Impedir"
        AppLanguage.SPANISH -> "Prevenir"
        AppLanguage.FRENCH -> "Empêcher"
        AppLanguage.GERMAN -> "Verhindern"
        AppLanguage.RUSSIAN -> "Предотвратить"
        AppLanguage.JAPANESE -> "防止"
        AppLanguage.KOREAN -> "차단"
    }
    val tagMask: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "遮罩"
        AppLanguage.ENGLISH -> "Mask"
        AppLanguage.ARABIC -> "قناع"
        AppLanguage.PORTUGUESE -> "Máscara"
        AppLanguage.SPANISH -> "Máscara"
        AppLanguage.FRENCH -> "Masque"
        AppLanguage.GERMAN -> "Maske"
        AppLanguage.RUSSIAN -> "Маска"
        AppLanguage.JAPANESE -> "マスク"
        AppLanguage.KOREAN -> "마스크"
    }
    val tagAntiDetect: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "反检测"
        AppLanguage.ENGLISH -> "Anti-Detect"
        AppLanguage.ARABIC -> "مكافحة الكشف"
        AppLanguage.PORTUGUESE -> "Anti-Detecção"
        AppLanguage.SPANISH -> "Antidetección"
        AppLanguage.FRENCH -> "Anti-Détection"
        AppLanguage.GERMAN -> "Anti-Erkennung"
        AppLanguage.RUSSIAN -> "Антидетект"
        AppLanguage.JAPANESE -> "検出回避"
        AppLanguage.KOREAN -> "탐지 방지"
    }
    val tagDebounce: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "防抖"
        AppLanguage.ENGLISH -> "Debounce"
        AppLanguage.ARABIC -> "منع الارتداد"
        AppLanguage.PORTUGUESE -> "Debounce"
        AppLanguage.SPANISH -> "Debounce"
        AppLanguage.FRENCH -> "Debounce"
        AppLanguage.GERMAN -> "Entprellung"
        AppLanguage.RUSSIAN -> "Дебаунс"
        AppLanguage.JAPANESE -> "デバウンス"
        AppLanguage.KOREAN -> "디바운스"
    }
    val tagPerformance: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "性能"
        AppLanguage.ENGLISH -> "Performance"
        AppLanguage.ARABIC -> "أداء"
        AppLanguage.PORTUGUESE -> "Desempenho"
        AppLanguage.SPANISH -> "Rendimiento"
        AppLanguage.FRENCH -> "Performances"
        AppLanguage.GERMAN -> "Leistung"
        AppLanguage.RUSSIAN -> "Производительность"
        AppLanguage.JAPANESE -> "パフォーマンス"
        AppLanguage.KOREAN -> "성능"
    }
    val tagThrottle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "节流"
        AppLanguage.ENGLISH -> "Throttle"
        AppLanguage.ARABIC -> "تقييد"
        AppLanguage.PORTUGUESE -> "Limitação"
        AppLanguage.SPANISH -> "Limitación"
        AppLanguage.FRENCH -> "Limitation"
        AppLanguage.GERMAN -> "Drosselung"
        AppLanguage.RUSSIAN -> "Дросселирование"
        AppLanguage.JAPANESE -> "スロットル"
        AppLanguage.KOREAN -> "스로틀"
    }
    val tagWait: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "等待"
        AppLanguage.ENGLISH -> "Wait"
        AppLanguage.ARABIC -> "انتظار"
        AppLanguage.PORTUGUESE -> "Aguardar"
        AppLanguage.SPANISH -> "Esperar"
        AppLanguage.FRENCH -> "Attendre"
        AppLanguage.GERMAN -> "Warten"
        AppLanguage.RUSSIAN -> "Ждать"
        AppLanguage.JAPANESE -> "待機"
        AppLanguage.KOREAN -> "대기"
    }
    val tagAsync: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "异步"
        AppLanguage.ENGLISH -> "Async"
        AppLanguage.ARABIC -> "غير متزامن"
        AppLanguage.PORTUGUESE -> "Assíncrono"
        AppLanguage.SPANISH -> "Asíncrono"
        AppLanguage.FRENCH -> "Asynchrone"
        AppLanguage.GERMAN -> "Asynchron"
        AppLanguage.RUSSIAN -> "Асинхронный"
        AppLanguage.JAPANESE -> "非同期"
        AppLanguage.KOREAN -> "비동기"
    }
    val tagDate: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "日期"
        AppLanguage.ENGLISH -> "Date"
        AppLanguage.ARABIC -> "تاريخ"
        AppLanguage.PORTUGUESE -> "Data"
        AppLanguage.SPANISH -> "Fecha"
        AppLanguage.FRENCH -> "Date"
        AppLanguage.GERMAN -> "Datum"
        AppLanguage.RUSSIAN -> "Дата"
        AppLanguage.JAPANESE -> "日付"
        AppLanguage.KOREAN -> "날짜"
    }
    val tagFormat: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "格式化"
        AppLanguage.ENGLISH -> "Format"
        AppLanguage.ARABIC -> "تنسيق"
        AppLanguage.PORTUGUESE -> "Formatar"
        AppLanguage.SPANISH -> "Formatear"
        AppLanguage.FRENCH -> "Formater"
        AppLanguage.GERMAN -> "Formatieren"
        AppLanguage.RUSSIAN -> "Форматировать"
        AppLanguage.JAPANESE -> "フォーマット"
        AppLanguage.KOREAN -> "포맷"
    }
    val tagRandom: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "随机"
        AppLanguage.ENGLISH -> "Random"
        AppLanguage.ARABIC -> "عشوائي"
        AppLanguage.PORTUGUESE -> "Aleatório"
        AppLanguage.SPANISH -> "Aleatorio"
        AppLanguage.FRENCH -> "Aléatoire"
        AppLanguage.GERMAN -> "Zufällig"
        AppLanguage.RUSSIAN -> "Случайный"
        AppLanguage.JAPANESE -> "ランダム"
        AppLanguage.KOREAN -> "무작위"
    }
    val tagString: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "字符串"
        AppLanguage.ENGLISH -> "String"
        AppLanguage.ARABIC -> "سلسلة"
        AppLanguage.PORTUGUESE -> "Cadeia"
        AppLanguage.SPANISH -> "Cadena"
        AppLanguage.FRENCH -> "Chaîne"
        AppLanguage.GERMAN -> "Zeichenkette"
        AppLanguage.RUSSIAN -> "Строка"
        AppLanguage.JAPANESE -> "文字列"
        AppLanguage.KOREAN -> "문자열"
    }
    val tagDelay: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "延迟"
        AppLanguage.ENGLISH -> "Delay"
        AppLanguage.ARABIC -> "تأخير"
        AppLanguage.PORTUGUESE -> "Atraso"
        AppLanguage.SPANISH -> "Retraso"
        AppLanguage.FRENCH -> "Délai"
        AppLanguage.GERMAN -> "Verzögerung"
        AppLanguage.RUSSIAN -> "Задержка"
        AppLanguage.JAPANESE -> "遅延"
        AppLanguage.KOREAN -> "지연"
    }
    val tagErrorHandle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "错误处理"
        AppLanguage.ENGLISH -> "Error Handling"
        AppLanguage.ARABIC -> "معالجة الأخطاء"
        AppLanguage.PORTUGUESE -> "Tratamento de Erros"
        AppLanguage.SPANISH -> "Manejo de Errores"
        AppLanguage.FRENCH -> "Gestion des Erreurs"
        AppLanguage.GERMAN -> "Fehlerbehandlung"
        AppLanguage.RUSSIAN -> "Обработка Ошибок"
        AppLanguage.JAPANESE -> "エラー処理"
        AppLanguage.KOREAN -> "오류 처리"
    }
    val tagArticle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "文章"
        AppLanguage.ENGLISH -> "Article"
        AppLanguage.ARABIC -> "مقال"
        AppLanguage.PORTUGUESE -> "Artigo"
        AppLanguage.SPANISH -> "Artículo"
        AppLanguage.FRENCH -> "Article"
        AppLanguage.GERMAN -> "Artikel"
        AppLanguage.RUSSIAN -> "Статья"
        AppLanguage.JAPANESE -> "記事"
        AppLanguage.KOREAN -> "기사"
    }
    val tagMarkdown: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Markdown"
        AppLanguage.ENGLISH -> "Markdown"
        AppLanguage.ARABIC -> "Markdown"
        AppLanguage.PORTUGUESE -> "Markdown"
        AppLanguage.SPANISH -> "Markdown"
        AppLanguage.FRENCH -> "Markdown"
        AppLanguage.GERMAN -> "Markdown"
        AppLanguage.RUSSIAN -> "Markdown"
        AppLanguage.JAPANESE -> "Markdown"
        AppLanguage.KOREAN -> "Markdown"
    }
    val tagConvert: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "转换"
        AppLanguage.ENGLISH -> "Convert"
        AppLanguage.ARABIC -> "تحويل"
        AppLanguage.PORTUGUESE -> "Converter"
        AppLanguage.SPANISH -> "Convertir"
        AppLanguage.FRENCH -> "Convertir"
        AppLanguage.GERMAN -> "Konvertieren"
        AppLanguage.RUSSIAN -> "Конвертировать"
        AppLanguage.JAPANESE -> "変換"
        AppLanguage.KOREAN -> "변환"
    }
    val tagFetch: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "fetch"
        AppLanguage.ENGLISH -> "Fetch"
        AppLanguage.ARABIC -> "Fetch"
        AppLanguage.PORTUGUESE -> "Fetch"
        AppLanguage.SPANISH -> "Fetch"
        AppLanguage.FRENCH -> "Fetch"
        AppLanguage.GERMAN -> "Fetch"
        AppLanguage.RUSSIAN -> "Fetch"
        AppLanguage.JAPANESE -> "Fetch"
        AppLanguage.KOREAN -> "Fetch"
    }
    val tagXHR: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "XHR"
        AppLanguage.ENGLISH -> "XHR"
        AppLanguage.ARABIC -> "XHR"
        AppLanguage.PORTUGUESE -> "XHR"
        AppLanguage.SPANISH -> "XHR"
        AppLanguage.FRENCH -> "XHR"
        AppLanguage.GERMAN -> "XHR"
        AppLanguage.RUSSIAN -> "XHR"
        AppLanguage.JAPANESE -> "XHR"
        AppLanguage.KOREAN -> "XHR"
    }
    val tagWebSocket: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "WebSocket"
        AppLanguage.ENGLISH -> "WebSocket"
        AppLanguage.ARABIC -> "WebSocket"
        AppLanguage.PORTUGUESE -> "WebSocket"
        AppLanguage.SPANISH -> "WebSocket"
        AppLanguage.FRENCH -> "WebSocket"
        AppLanguage.GERMAN -> "WebSocket"
        AppLanguage.RUSSIAN -> "WebSocket"
        AppLanguage.JAPANESE -> "WebSocket"
        AppLanguage.KOREAN -> "WebSocket"
    }
    val tagTimer: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "定时"
        AppLanguage.ENGLISH -> "Timer"
        AppLanguage.ARABIC -> "مؤقت"
        AppLanguage.PORTUGUESE -> "Temporizador"
        AppLanguage.SPANISH -> "Temporizador"
        AppLanguage.FRENCH -> "Minuteur"
        AppLanguage.GERMAN -> "Timer"
        AppLanguage.RUSSIAN -> "Таймер"
        AppLanguage.JAPANESE -> "タイマー"
        AppLanguage.KOREAN -> "타이머"
    }
    val tagRefresh: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Refresh"
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
    val tagLogin: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "登录"
        AppLanguage.ENGLISH -> "Login"
        AppLanguage.ARABIC -> "تسجيل الدخول"
        AppLanguage.PORTUGUESE -> "Entrar"
        AppLanguage.SPANISH -> "Iniciar Sesión"
        AppLanguage.FRENCH -> "Connexion"
        AppLanguage.GERMAN -> "Anmelden"
        AppLanguage.RUSSIAN -> "Вход"
        AppLanguage.JAPANESE -> "ログイン"
        AppLanguage.KOREAN -> "로그인"
    }
    val tagDetect: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "检测"
        AppLanguage.ENGLISH -> "Detect"
        AppLanguage.ARABIC -> "كشف"
        AppLanguage.PORTUGUESE -> "Detectar"
        AppLanguage.SPANISH -> "Detectar"
        AppLanguage.FRENCH -> "Détecter"
        AppLanguage.GERMAN -> "Erkennen"
        AppLanguage.RUSSIAN -> "Обнаружить"
        AppLanguage.JAPANESE -> "検出"
        AppLanguage.KOREAN -> "감지"
    }
    val tagConsole: String get() = when (Strings.lang) {
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
    val tagLog: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "日志"
        AppLanguage.ENGLISH -> "Log"
        AppLanguage.ARABIC -> "سجل"
        AppLanguage.PORTUGUESE -> "Registro"
        AppLanguage.SPANISH -> "Registro"
        AppLanguage.FRENCH -> "Journal"
        AppLanguage.GERMAN -> "Protokoll"
        AppLanguage.RUSSIAN -> "Журнал"
        AppLanguage.JAPANESE -> "ログ"
        AppLanguage.KOREAN -> "로그"
    }
    val tagInspect: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "检查"
        AppLanguage.ENGLISH -> "Inspect"
        AppLanguage.ARABIC -> "فحص"
        AppLanguage.PORTUGUESE -> "Inspecionar"
        AppLanguage.SPANISH -> "Inspeccionar"
        AppLanguage.FRENCH -> "Inspecter"
        AppLanguage.GERMAN -> "Untersuchen"
        AppLanguage.RUSSIAN -> "Инспектировать"
        AppLanguage.JAPANESE -> "検査"
        AppLanguage.KOREAN -> "검사"
    }
    val tagMonitor: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "监控"
        AppLanguage.ENGLISH -> "Monitor"
        AppLanguage.ARABIC -> "مراقبة"
        AppLanguage.PORTUGUESE -> "Monitorizar"
        AppLanguage.SPANISH -> "Monitorear"
        AppLanguage.FRENCH -> "Surveiller"
        AppLanguage.GERMAN -> "Überwachen"
        AppLanguage.RUSSIAN -> "Мониторинг"
        AppLanguage.JAPANESE -> "モニター"
        AppLanguage.KOREAN -> "모니터링"
    }

    val quickPrompt1: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "创建一个现代简约的个人主页，使用TailwindCSS，包含导航、英雄区、作品展示、联系方式。"
        AppLanguage.ENGLISH -> "Create a modern minimalist personal homepage using TailwindCSS, including navigation, hero section, portfolio showcase, and contact info."
        AppLanguage.ARABIC -> "إنشاء صفحة رئيسية شخصية حديثة وبسيطة باستخدام TailwindCSS، تتضمن التنقل، قسم البطل، عرض الأعمال، ومعلومات الاتصال."
        AppLanguage.PORTUGUESE -> "Crie uma página inicial pessoal moderna e minimalista usando TailwindCSS, incluindo navegação, seção hero, portfólio e informações de contato."
        AppLanguage.SPANISH -> "Crea una página de inicio personal moderna y minimalista usando TailwindCSS, incluyendo navegación, sección hero, portafolio e información de contacto."
        AppLanguage.FRENCH -> "Créez une page d'accueil personnelle moderne et minimaliste avec TailwindCSS, incluant la navigation, une section hero, un portfolio et des informations de contact."
        AppLanguage.GERMAN -> "Erstelle eine moderne, minimalistische persönliche Homepage mit TailwindCSS, inklusive Navigation, Hero-Bereich, Portfolio-Showcase und Kontaktinfo."
        AppLanguage.RUSSIAN -> "Создайте современную минималистичную личную домашнюю страницу с использованием TailwindCSS, включая навигацию, hero-секцию, портфолио и контактную информацию."
        AppLanguage.JAPANESE -> "TailwindCSSを使用したモダンでミニマルな個人ホームページを作成。ナビゲーション、ヒーローセクション、ポートフォリオ、連絡先を含む。"
        AppLanguage.KOREAN -> "TailwindCSS를 사용해 현대적이고 미니멀한 개인 홈페이지를 만들어주세요. 내비게이션, 히어로 섹션, 포트폴리오, 연락처를 포함."
    }
    val quickPrompt2: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "做一个玻璃拟态风格的登录页面，深色渐变背景，有邮箱密码输入和社交登录按钮。"
        AppLanguage.ENGLISH -> "Create a glassmorphism style login page with dark gradient background, email/password inputs and social login buttons."
        AppLanguage.ARABIC -> "إنشاء صفحة تسجيل دخول بنمط الزجاج المطفأ مع خلفية متدرجة داكنة، حقول البريد الإلكتروني وكلمة المرور وأزرار تسجيل الدخول الاجتماعي."
        AppLanguage.PORTUGUESE -> "Crie uma página de login estilo glassmorphism com fundo gradiente escuro, campos de email/senha e botões de login social."
        AppLanguage.SPANISH -> "Crea una página de inicio de sesión estilo glassmorphism con fondo de gradiente oscuro, campos de email/contraseña y botones de inicio de sesión social."
        AppLanguage.FRENCH -> "Créez une page de connexion de style glassmorphism avec un fond dégradé sombre, des champs email/mot de passe et des boutons de connexion sociale."
        AppLanguage.GERMAN -> "Erstelle eine Login-Seite im Glassmorphism-Stil mit dunklem Gradient-Hintergrund, E-Mail-/Passwort-Eingaben und Social-Login-Buttons."
        AppLanguage.RUSSIAN -> "Создайте страницу входа в стиле glassmorphism с тёмным градиентным фоном, полями email/пароль и кнопками социального входа."
        AppLanguage.JAPANESE -> "グラスモーフィズムスタイルのログインページを作成。暗いグラデーション背景、メール/パスワード入力、ソーシャルログインボタン付き。"
        AppLanguage.KOREAN -> "글래스모피즘 스타일의 로그인 페이지를 만들어주세요. 어두운 그라데이션 배경, 이메일/비밀번호 입력란, 소셜 로그인 버튼 포함."
    }
    val quickPrompt3: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "创建一个赛博朋克风格的404错误页面，有霓虹效果和故障艺术动画。"
        AppLanguage.ENGLISH -> "Create a cyberpunk style 404 error page with neon effects and glitch art animations."
        AppLanguage.ARABIC -> "إنشاء صفحة خطأ 404 بنمط السايبربانك مع تأثيرات النيون ورسوم متحركة للخلل الفني."
        AppLanguage.PORTUGUESE -> "Crie uma página de erro 404 estilo cyberpunk com efeitos neon e animações de glitch art."
        AppLanguage.SPANISH -> "Crea una página de error 404 estilo cyberpunk con efectos neón y animaciones de glitch art."
        AppLanguage.FRENCH -> "Créez une page d'erreur 404 de style cyberpunk avec des effets néon et des animations de glitch art."
        AppLanguage.GERMAN -> "Erstelle eine 404-Fehlerseite im Cyberpunk-Stil mit Neon-Effekten und Glitch-Art-Animationen."
        AppLanguage.RUSSIAN -> "Создайте страницу ошибки 404 в стиле киберпанк с неоновыми эффектами и анимациями глитч-арта."
        AppLanguage.JAPANESE -> "サイバーパンクスタイルの404エラーページを作成。ネオン効果とグリッチアートアニメーション付き。"
        AppLanguage.KOREAN -> "사이버펑크 스타일의 404 에러 페이지를 만들어주세요. 네온 효과와 글리치 아트 애니메이션 포함."
    }
    val quickPrompt4: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "做一个音乐播放器界面，暗黑主题，有唱片旋转动画、进度条、播放控制按钮。"
        AppLanguage.ENGLISH -> "Create a music player interface with dark theme, vinyl rotation animation, progress bar, and playback control buttons."
        AppLanguage.ARABIC -> "إنشاء واجهة مشغل موسيقى بسمة داكنة، مع رسوم متحركة لدوران الفينيل، شريط التقدم، وأزرار التحكم في التشغيل."
        AppLanguage.PORTUGUESE -> "Crie uma interface de player de música com tema escuro, animação de rotação de vinil, barra de progresso e botões de controle de reprodução."
        AppLanguage.SPANISH -> "Crea una interfaz de reproductor de música con tema oscuro, animación de rotación de vinilo, barra de progreso y botones de control de reproducción."
        AppLanguage.FRENCH -> "Créez une interface de lecteur de musique avec thème sombre, animation de rotation de vinyle, barre de progression et boutons de contrôle de lecture."
        AppLanguage.GERMAN -> "Erstelle eine Musikplayer-Oberfläche mit dunklem Theme, Vinyl-Rotationsanimation, Fortschrittsbalken und Wiedergabesteuerung-Buttons."
        AppLanguage.RUSSIAN -> "Создайте интерфейс музыкального плеера с тёмной темой, анимацией вращения винила, прогресс-баром и кнопками управления воспроизведением."
        AppLanguage.JAPANESE -> "ダークテーマの音楽プレーヤーインターフェースを作成。レコード回転アニメーション、プログレスバー、再生コントロールボタン付き。"
        AppLanguage.KOREAN -> "다크 테마의 음악 플레이어 인터페이스를 만들어주세요. 바이닐 회전 애니메이션, 진행률 표시줄, 재생 컨트롤 버튼 포함."
    }
    val quickPrompt5: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "创建一个天气卡片组件，根据天气类型显示不同的图标和背景渐变。"
        AppLanguage.ENGLISH -> "Create a weather card component that displays different icons and background gradients based on weather type."
        AppLanguage.ARABIC -> "إنشاء مكون بطاقة الطقس يعرض أيقونات وتدرجات خلفية مختلفة بناءً على نوع الطقس."
        AppLanguage.PORTUGUESE -> "Crie um componente de cartão de clima que exibe diferentes ícones e gradientes de fundo com base no tipo de clima."
        AppLanguage.SPANISH -> "Crea un componente de tarjeta de clima que muestre diferentes iconos y gradientes de fondo según el tipo de clima."
        AppLanguage.FRENCH -> "Créez un composant de carte météo qui affiche différentes icônes et dégradés de fond selon le type de météo."
        AppLanguage.GERMAN -> "Erstelle eine Wetterkarten-Komponente, die je nach Wettertyp unterschiedliche Icons und Hintergrund-Gradienten anzeigt."
        AppLanguage.RUSSIAN -> "Создайте компонент карточки погоды, который отображает разные иконки и градиенты фона в зависимости от типа погоды."
        AppLanguage.JAPANESE -> "天気タイプに応じて異なるアイコンと背景グラデーションを表示する天気カードコンポーネントを作成。"
        AppLanguage.KOREAN -> "날씨 유형에 따라 다른 아이콘과 배경 그라데이션을 표시하는 날씨 카드 컴포넌트를 만들어주세요."
    }
    val quickPrompt6: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "做一个响应式的图片画廊，瀑布流布局，点击图片有灯箱效果。"
        AppLanguage.ENGLISH -> "Create a responsive image gallery with masonry layout and lightbox effect on click."
        AppLanguage.ARABIC -> "إنشاء معرض صور متجاوب بتخطيط الشلال وتأثير صندوق الضوء عند النقر."
        AppLanguage.PORTUGUESE -> "Crie uma galeria de imagens responsiva com layout masonry e efeito lightbox ao clicar."
        AppLanguage.SPANISH -> "Crea una galería de imágenes responsive con diseño masonry y efecto lightbox al hacer clic."
        AppLanguage.FRENCH -> "Créez une galerie d'images responsive avec une disposition masonry et un effet lightbox au clic."
        AppLanguage.GERMAN -> "Erstelle eine responsive Bildergalerie mit Masonry-Layout und Lightbox-Effekt beim Klick."
        AppLanguage.RUSSIAN -> "Создайте адаптивную галерею изображений с masonry-раскладкой и эффектом lightbox по клику."
        AppLanguage.JAPANESE -> "レスポンシブな画像ギャラリーを作成。マソンリーレイアウトとクリック時のライトボックス効果付き。"
        AppLanguage.KOREAN -> "반응형 이미지 갤러리를 만들어주세요. 메이슨리 레이아웃과 클릭 시 라이트박스 효과 포함."
    }
    val quickPrompt7: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "创建一个待办事项应用，有添加、完成、删除功能，数据存储在localStorage。"
        AppLanguage.ENGLISH -> "Create a todo app with add, complete, delete functions, storing data in localStorage."
        AppLanguage.ARABIC -> "إنشاء تطبيق قائمة المهام مع وظائف الإضافة والإكمال والحذف، وتخزين البيانات في localStorage."
        AppLanguage.PORTUGUESE -> "Crie um app de tarefas com funções de adicionar, concluir e excluir, armazenando dados no localStorage."
        AppLanguage.SPANISH -> "Crea una app de tareas con funciones de añadir, completar y eliminar, almacenando datos en localStorage."
        AppLanguage.FRENCH -> "Créez une application de tâches avec fonctions d'ajout, de completion et de suppression, stockant les données dans localStorage."
        AppLanguage.GERMAN -> "Erstelle eine Todo-App mit Hinzufügen-, Erledigen- und Löschen-Funktionen, die Daten in localStorage speichert."
        AppLanguage.RUSSIAN -> "Создайте приложение задач с функциями добавления, выполнения и удаления, сохраняя данные в localStorage."
        AppLanguage.JAPANESE -> "追加、完了、削除機能を持つTodoアプリを作成。データはlocalStorageに保存。"
        AppLanguage.KOREAN -> "추가, 완료, 삭제 기능이 있는 할 일 앱을 만들어주세요. 데이터는 localStorage에 저장."
    }
    val quickPrompt8: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "做一个倒计时页面，显示距离某个日期的天时分秒，有翻牌动画效果。"
        AppLanguage.ENGLISH -> "Create a countdown page showing days, hours, minutes, seconds to a date, with flip card animation."
        AppLanguage.ARABIC -> "إنشاء صفحة عد تنازلي تعرض الأيام والساعات والدقائق والثواني حتى تاريخ معين، مع تأثير رسوم متحركة للبطاقات المقلوبة."
        AppLanguage.PORTUGUESE -> "Crie uma página de contagem regressiva mostrando dias, horas, minutos e segundos até uma data, com animação de flip card."
        AppLanguage.SPANISH -> "Crea una página de cuenta regresiva que muestre días, horas, minutos y segundos hasta una fecha, con animación de flip card."
        AppLanguage.FRENCH -> "Créez une page de compte à rebours affichant les jours, heures, minutes et secondes jusqu'à une date, avec animation de flip card."
        AppLanguage.GERMAN -> "Erstelle eine Countdown-Seite, die Tage, Stunden, Minuten und Sekunden bis zu einem Datum anzeigt, mit Flip-Card-Animation."
        AppLanguage.RUSSIAN -> "Создайте страницу обратного отсчёта, показывающую дни, часы, минуты и секунды до даты, с анимацией flip card."
        AppLanguage.JAPANESE -> "ある日付までの日、時間、分、秒を表示するカウントダウンページを作成。フリップカードアニメーション付き。"
        AppLanguage.KOREAN -> "특정 날짜까지의 일, 시, 분, 초를 표시하는 카운트다운 페이지를 만들어주세요. 플립 카드 애니메이션 포함."
    }

    val providerOpenAI: String get() = "OpenAI"
    val providerOpenAIDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "文本表现出色，推理能力强，支持文本、视觉和图像生成"
        AppLanguage.ENGLISH -> "Excellent text performance, strong reasoning, supports text, vision and image generation"
        AppLanguage.ARABIC -> "أداء نصي ممتاز، قدرة استدلال قوية، يدعم النص والرؤية وتوليد الصور"
        AppLanguage.PORTUGUESE -> "Excelente desempenho de texto, raciocínio forte, suporta texto, visão e geração de imagens"
        AppLanguage.SPANISH -> "Excelente rendimiento de texto, razonamiento fuerte, soporta texto, visión y generación de imágenes"
        AppLanguage.FRENCH -> "Excellente performance textuelle, raisonnement fort, prend en charge texte, vision et génération d'images"
        AppLanguage.GERMAN -> "Ausgezeichnete Textleistung, starkes Reasoning, unterstützt Text, Vision und Bildgenerierung"
        AppLanguage.RUSSIAN -> "Отличная работа с текстом, сильное рассуждение, поддержка текста, зрения и генерации изображений"
        AppLanguage.JAPANESE -> "テキスト性能に優れ、推論能力が高く、テキスト、ビジョン、画像生成をサポート"
        AppLanguage.KOREAN -> "텍스트 성능이 뛰어나고 추론 능력이 강하며, 텍스트, 비전, 이미지 생성 지원"
    }
    val providerOpenAIPricing: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "GPT 5.1 系列约 \$10/ 百万token"
        AppLanguage.ENGLISH -> "GPT 5.1 series ~\$10/million tokens"
        AppLanguage.ARABIC -> "سلسلة GPT 5.1 حوالي \$10/مليون رمز"
        AppLanguage.PORTUGUESE -> "Série GPT 5.1 ~\$10/milhão de tokens"
        AppLanguage.SPANISH -> "Serie GPT 5.1 ~\$10/millón de tokens"
        AppLanguage.FRENCH -> "Série GPT 5.1 ~\$10/million de tokens"
        AppLanguage.GERMAN -> "GPT 5.1 Serie ~\$10/Million Token"
        AppLanguage.RUSSIAN -> "Серия GPT 5.1 ~\$10/млн токенов"
        AppLanguage.JAPANESE -> "GPT 5.1 シリーズ 約\$10/100万トークン"
        AppLanguage.KOREAN -> "GPT 5.1 시리즈 ~\$10/100만 토큰"
    }

    val providerOpenRouter: String get() = "OpenRouter"
    val providerOpenRouterDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "聚合多家 AI 供应商，统一接口调用。可用同一 API Key 调用 OpenAI、Claude、Gemini 等多种模型"
        AppLanguage.ENGLISH -> "Aggregates multiple AI providers with unified API. Use one API Key to access OpenAI, Claude, Gemini and more"
        AppLanguage.ARABIC -> "يجمع مزودي الذكاء الاصطناعي المتعددين بواجهة موحدة. استخدم مفتاح API واحد للوصول إلى OpenAI وClaude وGemini والمزيد"
        AppLanguage.PORTUGUESE -> "Agrega múltiplos provedores de IA com API unificada. Use uma API Key para acessar OpenAI, Claude, Gemini e mais"
        AppLanguage.SPANISH -> "Agrega múltiples proveedores de IA con API unificada. Usa una API Key para acceder a OpenAI, Claude, Gemini y más"
        AppLanguage.FRENCH -> "Agrège plusieurs fournisseurs d'IA avec une API unifiée. Utilisez une API Key pour accéder à OpenAI, Claude, Gemini et plus"
        AppLanguage.GERMAN -> "Aggregiert mehrere KI-Anbieter mit einheitlicher API. Nutze einen API Key für OpenAI, Claude, Gemini und mehr"
        AppLanguage.RUSSIAN -> "Агрегирует нескольких поставщиков ИИ через единый API. Используйте один API Key для доступа к OpenAI, Claude, Gemini и др."
        AppLanguage.JAPANESE -> "統合APIで複数のAIプロバイダーを集約。1つのAPIキーでOpenAI、Claude、Geminiなどにアクセス"
        AppLanguage.KOREAN -> "통합 API로 여러 AI 공급자를 집계. 하나의 API 키로 OpenAI, Claude, Gemini 등에 접근"
    }
    val providerOpenRouterPricing: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "按模型不同计费，价格透明，有免费模型，强烈推荐"
        AppLanguage.ENGLISH -> "Pay per model, transparent pricing, free models available, highly recommended"
        AppLanguage.ARABIC -> "الدفع لكل نموذج، تسعير شفاف، نماذج مجانية متاحة، موصى به بشدة"
        AppLanguage.PORTUGUESE -> "Pague por modelo, preços transparentes, modelos gratuitos disponíveis, altamente recomendado"
        AppLanguage.SPANISH -> "Paga por modelo, precios transparentes, modelos gratuitos disponibles, muy recomendado"
        AppLanguage.FRENCH -> "Paiement par modèle, tarification transparente, modèles gratuits disponibles, fortement recommandé"
        AppLanguage.GERMAN -> "Zahlung pro Modell, transparente Preise, kostenlose Modelle verfügbar, sehr empfohlen"
        AppLanguage.RUSSIAN -> "Оплата за модель, прозрачные цены, есть бесплатные модели, настоятельно рекомендуется"
        AppLanguage.JAPANESE -> "モデルごとの課金、透明な価格設定、無料モデルあり、強く推奨"
        AppLanguage.KOREAN -> "모델별 과금, 투명한 가격, 무료 모델 제공, 강력 추천"
    }

    val providerAnthropic: String get() = "Anthropic/Claude"
    val providerAnthropicDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Claude 系列模型，擅长文本理解和代码生成且有视觉支持，编程能力强。"
        AppLanguage.ENGLISH -> "Claude models, excels at text understanding and code generation with vision support, strong programming ability."
        AppLanguage.ARABIC -> "نماذج Claude، متميزة في فهم النص وتوليد الكود مع دعم الرؤية، قدرة برمجة قوية."
        AppLanguage.PORTUGUESE -> "Modelos Claude, destaca-se em compreensão de texto e geração de código com suporte a visão, forte habilidade de programação."
        AppLanguage.SPANISH -> "Modelos Claude, destaca en comprensión de texto y generación de código con soporte de visión, fuerte capacidad de programación."
        AppLanguage.FRENCH -> "Modèles Claude, excelle en compréhension de texte et génération de code avec support vision, forte capacité de programmation."
        AppLanguage.GERMAN -> "Claude-Modelle, überzeugt bei Textverständnis und Codegenerierung mit Vision-Support, starke Programmierfähigkeit."
        AppLanguage.RUSSIAN -> "Модели Claude, превосходят в понимании текста и генерации кода с поддержкой зрения, сильные навыки программирования."
        AppLanguage.JAPANESE -> "Claudeモデル、テキスト理解とコード生成に優れ、ビジョンサポート付き、プログラミング能力が強い。"
        AppLanguage.KOREAN -> "Claude 모델, 텍스트 이해와 코드 생성에 뛰어나며 비전 지원, 강력한 프로그래밍 능력."
    }
    val providerAnthropicPricing: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Claude 4.5 Sonnet 约 \$15/百万 token"
        AppLanguage.ENGLISH -> "Claude 4.5 Sonnet ~\$15/million tokens"
        AppLanguage.ARABIC -> "Claude 4.5 Sonnet حوالي \$15/مليون رمز"
        AppLanguage.PORTUGUESE -> "Claude 4.5 Sonnet ~\$15/milhão de tokens"
        AppLanguage.SPANISH -> "Claude 4.5 Sonnet ~\$15/millón de tokens"
        AppLanguage.FRENCH -> "Claude 4.5 Sonnet ~\$15/million de tokens"
        AppLanguage.GERMAN -> "Claude 4.5 Sonnet ~\$15/Million Token"
        AppLanguage.RUSSIAN -> "Claude 4.5 Sonnet ~\$15/млн токенов"
        AppLanguage.JAPANESE -> "Claude 4.5 Sonnet 約\$15/100万トークン"
        AppLanguage.KOREAN -> "Claude 4.5 Sonnet ~\$15/100만 토큰"
    }

    val providerGoogle: String get() = "Google/Gemini"
    val providerGoogleDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "推荐：Gemini 3.0 Pro 前端表现出色，原生多模态支持，全面顶配支持。"
        AppLanguage.ENGLISH -> "Recommended: Gemini 3.0 Pro excels at frontend, native multimodal support, fully featured."
        AppLanguage.ARABIC -> "موصى به: Gemini 3.0 Pro متميز في الواجهة الأمامية، دعم متعدد الوسائط أصلي، ميزات كاملة."
        AppLanguage.PORTUGUESE -> "Recomendado: Gemini 3.0 Pro destaca-se em frontend, suporte multimodal nativo, totalmente completo."
        AppLanguage.SPANISH -> "Recomendado: Gemini 3.0 Pro destaca en frontend, soporte multimodal nativo, con todas las funciones."
        AppLanguage.FRENCH -> "Recommandé : Gemini 3.0 Pro excelle en frontend, support multimodal natif, toutes fonctionnalités."
        AppLanguage.GERMAN -> "Empfohlen: Gemini 3.0 Pro überzeugt im Frontend, native Multimodal-Unterstützung, voll ausgestattet."
        AppLanguage.RUSSIAN -> "Рекомендуется: Gemini 3.0 Pro превосходен во фронтенде, нативная мультимодальная поддержка, все функции."
        AppLanguage.JAPANESE -> "推奨：Gemini 3.0 Proはフロントエンドに優れ、ネイティブマルチモーダル対応、フル機能。"
        AppLanguage.KOREAN -> "추천: Gemini 3.0 Pro는 프론트엔드에 뛰어나고, 네이티브 멀티모달 지원, 모든 기능 탑재."
    }
    val providerGooglePricing: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "有免费额度，超出后按 token 计费"
        AppLanguage.ENGLISH -> "Free tier available, pay per token after limit"
        AppLanguage.ARABIC -> "مستوى مجاني متاح، الدفع لكل رمز بعد الحد"
        AppLanguage.PORTUGUESE -> "Nível gratuito disponível, pague por token após o limite"
        AppLanguage.SPANISH -> "Nivel gratuito disponible, paga por token después del límite"
        AppLanguage.FRENCH -> "Niveau gratuit disponible, paiement par token après la limite"
        AppLanguage.GERMAN -> "Kostenloser Tarif verfügbar, Zahlung pro Token nach Limit"
        AppLanguage.RUSSIAN -> "Есть бесплатный уровень, оплата за токен после лимита"
        AppLanguage.JAPANESE -> "無料枠あり、上限超過後はトークンごとに課金"
        AppLanguage.KOREAN -> "무료 한도 제공, 한도 초과 후 토큰당 과금"
    }

    val providerDeepSeek: String get() = "DeepSeek"
    val providerDeepSeekDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "国家队，性价比高。目前仅支持文本和图像文本生成"
        AppLanguage.ENGLISH -> "Chinese national team, cost-effective. Currently supports text and image-text generation only"
        AppLanguage.ARABIC -> "الفريق الوطني الصيني، فعال من حيث التكلفة. يدعم حاليًا توليد النص والصور-النص فقط"
        AppLanguage.PORTUGUESE -> "Equipe nacional chinesa, custo-benefício. Atualmente suporta apenas texto e geração de texto-imagem"
        AppLanguage.SPANISH -> "Equipo nacional chino, rentable. Actualmente solo soporta texto y generación de texto-imagen"
        AppLanguage.FRENCH -> "Équipe nationale chinoise, bon rapport qualité-prix. Actuellement prend en charge uniquement texte et génération texte-image"
        AppLanguage.GERMAN -> "Chinesisches Nationalteam, kosteneffizient. Aktuell nur Text- und Bild-Text-Generierung unterstützt"
        AppLanguage.RUSSIAN -> "Китайская национальная команда, выгодное соотношение цены и качества. Сейчас поддерживает только текст и генерацию текст-изображение"
        AppLanguage.JAPANESE -> "中国ナショナルチーム、コストパフォーマンス良好。現在はテキストと画像テキスト生成のみサポート"
        AppLanguage.KOREAN -> "중국 국가대표팀, 가성비 좋음. 현재 텍스트 및 이미지-텍스트 생성만 지원"
    }
    val providerDeepSeekPricing: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "极低价格，约 ¥0.4/百万 token"
        AppLanguage.ENGLISH -> "Very low price, ~¥0.4/million tokens"
        AppLanguage.ARABIC -> "سعر منخفض جدًا، حوالي ¥0.4/مليون رمز"
        AppLanguage.PORTUGUESE -> "Preço muito baixo, ~¥0.4/milhão de tokens"
        AppLanguage.SPANISH -> "Precio muy bajo, ~¥0.4/millón de tokens"
        AppLanguage.FRENCH -> "Prix très bas, ~¥0.4/million de tokens"
        AppLanguage.GERMAN -> "Sehr niedriger Preis, ~¥0.4/Million Token"
        AppLanguage.RUSSIAN -> "Очень низкая цена, ~¥0.4/млн токенов"
        AppLanguage.JAPANESE -> "非常に低価格、約¥0.4/100万トークン"
        AppLanguage.KOREAN -> "매우 낮은 가격, ~¥0.4/100만 토큰"
    }


    val providerGLM: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "智谱GLM"
        AppLanguage.ENGLISH -> "Zhipu GLM"
        AppLanguage.ARABIC -> "Zhipu GLM"
        AppLanguage.PORTUGUESE -> "Zhipu GLM"
        AppLanguage.SPANISH -> "Zhipu GLM"
        AppLanguage.FRENCH -> "Zhipu GLM"
        AppLanguage.GERMAN -> "Zhipu GLM"
        AppLanguage.RUSSIAN -> "Zhipu GLM"
        AppLanguage.JAPANESE -> "智譜 GLM"
        AppLanguage.KOREAN -> "Zhipu GLM"
    }
    val providerGLMDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "国产，GLM-4.6 系列性能优秀，编码能力强，支持多模态"
        AppLanguage.ENGLISH -> "Chinese, GLM-4.6 series performs well, strong coding ability, supports multimodal"
        AppLanguage.ARABIC -> "صيني، سلسلة GLM-4.6 أداء جيد، قدرة برمجة قوية، يدعم متعدد الوسائط"
        AppLanguage.PORTUGUESE -> "Chinês, série GLM-4.6 tem bom desempenho, forte capacidade de codificação, suporta multimodal"
        AppLanguage.SPANISH -> "Chino, serie GLM-4.6 buen rendimiento, fuerte capacidad de codificación, soporta multimodal"
        AppLanguage.FRENCH -> "Chinois, série GLM-4.6 bonnes performances, forte capacité de codage, prend en charge le multimodal"
        AppLanguage.GERMAN -> "Chinesisch, GLM-4.6 Serie leistet gut, starke Codierungsfähigkeit, unterstützt Multimodal"
        AppLanguage.RUSSIAN -> "Китайский, серия GLM-4.6 хорошо работает, сильные навыки кодирования, поддержка мультимодальности"
        AppLanguage.JAPANESE -> "中国製、GLM-4.6 シリーズ性能良好、コーディング能力強、マルチモーダル対応"
        AppLanguage.KOREAN -> "중국산, GLM-4.6 시리즈 성능 우수, 코딩 능력 강함, 멀티모달 지원"
    }
    val providerGLMPricing: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "价格较低，约 \$2/百万 token"
        AppLanguage.ENGLISH -> "Low price, ~\$2/million tokens"
        AppLanguage.ARABIC -> "سعر منخفض، حوالي \$2/مليون رمز"
        AppLanguage.PORTUGUESE -> "Preço baixo, ~\$2/milhão de tokens"
        AppLanguage.SPANISH -> "Precio bajo, ~\$2/millón de tokens"
        AppLanguage.FRENCH -> "Prix bas, ~\$2/million de tokens"
        AppLanguage.GERMAN -> "Niedriger Preis, ~\$2/Million Token"
        AppLanguage.RUSSIAN -> "Низкая цена, ~\$2/млн токенов"
        AppLanguage.JAPANESE -> "低価格、約\$2/100万トークン"
        AppLanguage.KOREAN -> "저렴한 가격, ~\$2/100만 토큰"
    }

    val providerGrok: String get() = "xAI/Grok"
    val providerGrokDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "马斯克旗下 xAI 的 Grok 系列，支持文本和视觉"
        AppLanguage.ENGLISH -> "Elon Musk's xAI Grok series, supports text and vision"
        AppLanguage.ARABIC -> "سلسلة Grok من xAI التابعة لإيلون ماسك، تدعم النص والرؤية"
        AppLanguage.PORTUGUESE -> "Série Grok da xAI de Elon Musk, suporta texto e visão"
        AppLanguage.SPANISH -> "Serie Grok de xAI de Elon Musk, soporta texto y visión"
        AppLanguage.FRENCH -> "Série Grok d'xAI d'Elon Musk, prend en charge texte et vision"
        AppLanguage.GERMAN -> "Elon Musks xAI Grok-Serie, unterstützt Text und Vision"
        AppLanguage.RUSSIAN -> "Серия Grok от xAI Илона Маска, поддерживает текст и зрение"
        AppLanguage.JAPANESE -> "イーロン・マスクのxAI Grokシリーズ、テキストとビジョンをサポート"
        AppLanguage.KOREAN -> "일론 머스크의 xAI Grok 시리즈, 텍스트와 비전 지원"
    }
    val providerGrokPricing: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "价格便宜，Grok-4.1-fast 约 \$0.5/百万 token"
        AppLanguage.ENGLISH -> "Cheap, Grok-4.1-fast ~\$0.5/million tokens"
        AppLanguage.ARABIC -> "رخيص، Grok-4.1-fast حوالي \$0.5/مليون رمز"
        AppLanguage.PORTUGUESE -> "Barato, Grok-4.1-fast ~\$0.5/milhão de tokens"
        AppLanguage.SPANISH -> "Barato, Grok-4.1-fast ~\$0.5/millón de tokens"
        AppLanguage.FRENCH -> "Bon marché, Grok-4.1-fast ~\$0.5/million de tokens"
        AppLanguage.GERMAN -> "Günstig, Grok-4.1-fast ~\$0.5/Million Token"
        AppLanguage.RUSSIAN -> "Дёшево, Grok-4.1-fast ~\$0.5/млн токенов"
        AppLanguage.JAPANESE -> "安価、Grok-4.1-fast 約 \$0.5/100万トークン"
        AppLanguage.KOREAN -> "저렴함, Grok-4.1-fast 약 \$0.5/백만 토큰"
    }



    val providerQwen: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "通义千问"
        AppLanguage.ENGLISH -> "Tongyi Qwen"
        AppLanguage.ARABIC -> "Tongyi Qwen"
        AppLanguage.PORTUGUESE -> "Tongyi Qwen"
        AppLanguage.SPANISH -> "Tongyi Qwen"
        AppLanguage.FRENCH -> "Tongyi Qwen"
        AppLanguage.GERMAN -> "Tongyi Qwen"
        AppLanguage.RUSSIAN -> "Tongyi Qwen"
        AppLanguage.JAPANESE -> "通義千問"
        AppLanguage.KOREAN -> "통의 첸원"
    }
    val providerQwenDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "阿里云通义千问，支持文本、视觉、音频等多模态。Qwen3 系列推理能力强"
        AppLanguage.ENGLISH -> "Alibaba Cloud Tongyi Qwen, supports text, vision, audio multimodal. Qwen3 series has strong reasoning"
        AppLanguage.ARABIC -> "Alibaba Cloud Tongyi Qwen، يدعم النص والرؤية والصوت متعدد الوسائط. سلسلة Qwen3 لديها استدلال قوي"
        AppLanguage.PORTUGUESE -> "Tongyi Qwen da Alibaba Cloud, suporta multimodal de texto, visão e áudio. A série Qwen3 tem forte raciocínio"
        AppLanguage.SPANISH -> "Tongyi Qwen de Alibaba Cloud, soporta multimodal de texto, visión y audio. La serie Qwen3 tiene fuerte razonamiento"
        AppLanguage.FRENCH -> "Tongyi Qwen d'Alibaba Cloud, prend en charge le multimodal texte, vision et audio. La série Qwen3 a un raisonnement puissant"
        AppLanguage.GERMAN -> "Tongyi Qwen von Alibaba Cloud, unterstützt multimodal Text, Vision und Audio. Die Qwen3-Serie hat starke Schlussfolgerungsfähigkeit"
        AppLanguage.RUSSIAN -> "Tongyi Qwen от Alibaba Cloud, поддерживает мультимодальность текста, зрения и аудио. Серия Qwen3 обладает сильным рассуждением"
        AppLanguage.JAPANESE -> "Alibaba CloudのTongyi Qwen、テキスト・ビジョン・音声のマルチモーダル対応。Qwen3シリーズは推論能力が強い"
        AppLanguage.KOREAN -> "Alibaba Cloud의 Tongyi Qwen, 텍스트·비전·오디오 멀티모달 지원. Qwen3 시리즈는 강력한 추론 능력을 갖춤"
    }
    val providerQwenPricing: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "有免费额度，价格便宜，约 ¥0.5/百万 token"
        AppLanguage.ENGLISH -> "Free tier available, cheap, ~¥0.5/million tokens"
        AppLanguage.ARABIC -> "مستوى مجاني متاح، رخيص، حوالي ¥0.5/مليون رمز"
        AppLanguage.PORTUGUESE -> "Camada gratuita disponível, barato, ~¥0.5/milhão de tokens"
        AppLanguage.SPANISH -> "Nivel gratuito disponible, barato, ~¥0.5/millón de tokens"
        AppLanguage.FRENCH -> "Niveau gratuit disponible, bon marché, ~¥0.5/million de tokens"
        AppLanguage.GERMAN -> "Kostenlose Stufe verfügbar, günstig, ~¥0.5/Million Token"
        AppLanguage.RUSSIAN -> "Есть бесплатный уровень, дёшево, ~¥0.5/млн токенов"
        AppLanguage.JAPANESE -> "無料枠あり、安価、約¥0.5/100万トークン"
        AppLanguage.KOREAN -> "무료 tier 제공, 저렴함, ~¥0.5/100만 토큰"
    }

    val providerCustom: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Custom"
        AppLanguage.ENGLISH -> "Custom"
        AppLanguage.ARABIC -> "مخصص"
        AppLanguage.PORTUGUESE -> "Custom"
        AppLanguage.SPANISH -> "Custom"
        AppLanguage.FRENCH -> "Custom"
        AppLanguage.GERMAN -> "Custom"
        AppLanguage.RUSSIAN -> "Custom"
        AppLanguage.JAPANESE -> "Custom"
        AppLanguage.KOREAN -> "Custom"
    }
    val providerCustomDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "兼容 OpenAI API 格式的自定义服务。需要填写完整的 Base URL"
        AppLanguage.ENGLISH -> "Custom service compatible with OpenAI API format. Requires full Base URL"
        AppLanguage.ARABIC -> "خدمة مخصصة متوافقة مع تنسيق OpenAI API. يتطلب عنوان URL الأساسي الكامل"
        AppLanguage.PORTUGUESE -> "Serviço personalizado compatível com o formato da OpenAI API. Requer o Base URL completo"
        AppLanguage.SPANISH -> "Servicio personalizado compatible con el formato de OpenAI API. Requiere el Base URL completo"
        AppLanguage.FRENCH -> "Service personnalisé compatible avec le format OpenAI API. Nécessite le Base URL complet"
        AppLanguage.GERMAN -> "Benutzerdefinierter Dienst kompatibel mit dem OpenAI-API-Format. Erfordert die vollständige Base URL"
        AppLanguage.RUSSIAN -> "Пользовательский сервис, совместимый с форматом OpenAI API. Требует полный Base URL"
        AppLanguage.JAPANESE -> "OpenAI API 形式に対応したカスタムサービス。完全な Base URL が必要"
        AppLanguage.KOREAN -> "OpenAI API 형식과 호환되는 사용자 지정 서비스. 전체 Base URL 필요"
    }
    val providerCustomPricing: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "取决于服务商"
        AppLanguage.ENGLISH -> "Depends on provider"
        AppLanguage.ARABIC -> "يعتمد على المزود"
        AppLanguage.PORTUGUESE -> "Depende do provedor"
        AppLanguage.SPANISH -> "Depende del proveedor"
        AppLanguage.FRENCH -> "Dépend du fournisseur"
        AppLanguage.GERMAN -> "Hängt vom Anbieter ab"
        AppLanguage.RUSSIAN -> "Зависит от провайдера"
        AppLanguage.JAPANESE -> "プロバイダーによる"
        AppLanguage.KOREAN -> "제공업체에 따라 다름"
    }

    val providerCategoryRecommended: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "推荐"
        AppLanguage.ENGLISH -> "Recommended"
        AppLanguage.ARABIC -> "موصى به"
        AppLanguage.PORTUGUESE -> "Recomendado"
        AppLanguage.SPANISH -> "Recomendado"
        AppLanguage.FRENCH -> "Recommandé"
        AppLanguage.GERMAN -> "Empfohlen"
        AppLanguage.RUSSIAN -> "Рекомендуемые"
        AppLanguage.JAPANESE -> "おすすめ"
        AppLanguage.KOREAN -> "추천"
    }
    val providerCategoryInternational: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "国际供应商"
        AppLanguage.ENGLISH -> "International"
        AppLanguage.ARABIC -> "دولي"
        AppLanguage.PORTUGUESE -> "Internacional"
        AppLanguage.SPANISH -> "Internacional"
        AppLanguage.FRENCH -> "International"
        AppLanguage.GERMAN -> "International"
        AppLanguage.RUSSIAN -> "Международные"
        AppLanguage.JAPANESE -> "海外プロバイダー"
        AppLanguage.KOREAN -> "국제 제공업체"
    }
    val providerCategoryChinese: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "国内供应商"
        AppLanguage.ENGLISH -> "Chinese"
        AppLanguage.ARABIC -> "صيني"
        AppLanguage.PORTUGUESE -> "Provedores chineses"
        AppLanguage.SPANISH -> "Proveedores chinos"
        AppLanguage.FRENCH -> "Fournisseurs chinois"
        AppLanguage.GERMAN -> "Chinesische Anbieter"
        AppLanguage.RUSSIAN -> "Китайские провайдеры"
        AppLanguage.JAPANESE -> "中国プロバイダー"
        AppLanguage.KOREAN -> "중국 제공업체"
    }
    val providerCategoryAggregator: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "聚合平台"
        AppLanguage.ENGLISH -> "Aggregator"
        AppLanguage.ARABIC -> "منصة تجميع"
        AppLanguage.PORTUGUESE -> "Agregador"
        AppLanguage.SPANISH -> "Agregador"
        AppLanguage.FRENCH -> "Agrégateur"
        AppLanguage.GERMAN -> "Aggregator-Plattform"
        AppLanguage.RUSSIAN -> "Агрегатор"
        AppLanguage.JAPANESE -> "アグリゲーター"
        AppLanguage.KOREAN -> "애그리게이터"
    }
    val providerCategorySelfHosted: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "本地/自托管"
        AppLanguage.ENGLISH -> "Self-Hosted"
        AppLanguage.ARABIC -> "استضافة ذاتية"
        AppLanguage.PORTUGUESE -> "Auto-hospedado"
        AppLanguage.SPANISH -> "Autoalojado"
        AppLanguage.FRENCH -> "Auto-hébergé"
        AppLanguage.GERMAN -> "Selbst gehostet"
        AppLanguage.RUSSIAN -> "Самостоятельный хостинг"
        AppLanguage.JAPANESE -> "セルフホスト"
        AppLanguage.KOREAN -> "셀프 호스팅"
    }
    val providerCategoryCustom: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自定义"
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







    val providerTogether: String get() = "Together AI"
    val providerTogetherDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "开源模型聚合平台，支持数百种开源模型。OpenAI 兼容接口，支持微调"
        AppLanguage.ENGLISH -> "Open-source model aggregator, supports hundreds of models. OpenAI-compatible API with fine-tuning support"
        AppLanguage.ARABIC -> "منصة تجميع نماذج مفتوحة المصدر، تدعم مئات النماذج. واجهة متوافقة مع OpenAI مع دعم الضبط الدقيق"
        AppLanguage.PORTUGUESE -> "Agregador de modelos de código aberto, suporta centenas de modelos. API compatível com OpenAI com suporte a fine-tuning"
        AppLanguage.SPANISH -> "Agregador de modelos de código abierto, soporta cientos de modelos. API compatible con OpenAI con soporte de fine-tuning"
        AppLanguage.FRENCH -> "Agrégateur de modèles open source, prend en charge des centaines de modèles. API compatible OpenAI avec support du fine-tuning"
        AppLanguage.GERMAN -> "Open-Source-Modell-Aggregator, unterstützt Hunderte von Modellen. OpenAI-kompatible API mit Fine-Tuning-Unterstützung"
        AppLanguage.RUSSIAN -> "Агрегатор моделей с открытым исходным кодом, поддерживает сотни моделей. API, совместимый с OpenAI, с поддержкой дообучения"
        AppLanguage.JAPANESE -> "オープンソースモデル集約プラットフォーム、数百のモデルをサポート。OpenAI 互換 API、ファインチューニング対応"
        AppLanguage.KOREAN -> "오픈소스 모델 애그리게이터, 수백 개 모델 지원. OpenAI 호환 API, 파인튜닝 지원"
    }
    val providerTogetherPricing: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "按模型计费，开源模型价格低"
        AppLanguage.ENGLISH -> "Pay per model, low prices for open-source models"
        AppLanguage.ARABIC -> "الدفع لكل نموذج، أسعار منخفضة للنماذج مفتوحة المصدر"
        AppLanguage.PORTUGUESE -> "Pagamento por modelo, preços baixos para modelos de código aberto"
        AppLanguage.SPANISH -> "Pago por modelo, precios bajos para modelos de código abierto"
        AppLanguage.FRENCH -> "Paiement par modèle, prix bas pour les modèles open source"
        AppLanguage.GERMAN -> "Zahlung pro Modell, niedrige Preise für Open-Source-Modelle"
        AppLanguage.RUSSIAN -> "Оплата за модель, низкие цены на модели с открытым исходным кодом"
        AppLanguage.JAPANESE -> "モデルごと課金、オープンソースモデルは低価格"
        AppLanguage.KOREAN -> "모델별 과금, 오픈소스 모델은 저렴"
    }

    val providerPerplexity: String get() = "Perplexity"
    val providerPerplexityDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "AI 搜索引擎，Sonar 系列模型自带在线搜索能力，适合需要实时信息的场景"
        AppLanguage.ENGLISH -> "AI search engine. Sonar series models with built-in web search, ideal for real-time information needs"
        AppLanguage.ARABIC -> "محرك بحث ذكاء اصطناعي. سلسلة Sonar مع بحث ويب مدمج، مثالي لاحتياجات المعلومات في الوقت الفعلي"
        AppLanguage.PORTUGUESE -> "Motor de busca por IA. Modelos da série Sonar com busca na web integrada, ideal para necessidades de informações em tempo real"
        AppLanguage.SPANISH -> "Motor de búsqueda por IA. Modelos de la serie Sonar con búsqueda web integrada, ideal para necesidades de información en tiempo real"
        AppLanguage.FRENCH -> "Moteur de recherche IA. Modèles de la série Sonar avec recherche web intégrée, idéal pour les besoins d'information en temps réel"
        AppLanguage.GERMAN -> "KI-Suchmaschine. Sonar-Serien-Modelle mit integrierter Websuche, ideal für Echtzeit-Informationsbedürfnisse"
        AppLanguage.RUSSIAN -> "Поисковая система на ИИ. Модели серии Sonar со встроенным веб-поиском, идеально для потребностей в информации в реальном времени"
        AppLanguage.JAPANESE -> "AI検索エンジン。Web検索内蔵の Sonar シリーズモデル、リアルタイム情報ニーズに最適"
        AppLanguage.KOREAN -> "AI 검색 엔진. 웹 검색 내장 Sonar 시리즈 모델, 실시간 정보 필요에 적합"
    }
    val providerPerplexityPricing: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Sonar Pro 约 \$3/百万 token + 搜索费用"
        AppLanguage.ENGLISH -> "Sonar Pro ~\$3/M tokens + search costs"
        AppLanguage.ARABIC -> "Sonar Pro حوالي \$3/مليون رمز + تكاليف البحث"
        AppLanguage.PORTUGUESE -> "Sonar Pro ~\$3/M tokens + custos de busca"
        AppLanguage.SPANISH -> "Sonar Pro ~\$3/M tokens + costos de búsqueda"
        AppLanguage.FRENCH -> "Sonar Pro ~\$3/M tokens + coûts de recherche"
        AppLanguage.GERMAN -> "Sonar Pro ~\$3/M Tokens + Suchkosten"
        AppLanguage.RUSSIAN -> "Sonar Pro ~\$3/M токенов + расходы на поиск"
        AppLanguage.JAPANESE -> "Sonar Pro ~\$3/M tokens + 検索費用"
        AppLanguage.KOREAN -> "Sonar Pro ~\$3/M tokens + 검색 비용"
    }

    val providerFireworks: String get() = "Fireworks AI"
    val providerFireworksDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "高性能推理平台，支持多种开源模型。专注低延迟推理优化"
        AppLanguage.ENGLISH -> "High-performance inference platform with multiple open-source models. Focuses on low-latency inference optimization"
        AppLanguage.ARABIC -> "منصة استدلال عالية الأداء مع نماذج مفتوحة المصدر متعددة. تركز على تحسين الاستدلال منخفض الكمون"
        AppLanguage.PORTUGUESE -> "Plataforma de inferência de alto desempenho com vários modelos de código aberto. Focada em otimização de inferência de baixa latência"
        AppLanguage.SPANISH -> "Plataforma de inferencia de alto rendimiento con múltiples modelos de código abierto. Enfocada en optimización de inferencia de baja latencia"
        AppLanguage.FRENCH -> "Plateforme d'inférence haute performance avec plusieurs modèles open source. Axée sur l'optimisation de l'inférence à faible latence"
        AppLanguage.GERMAN -> "Leistungsstarke Inferenzplattform mit mehreren Open-Source-Modellen. Fokus auf Inferenzoptimierung mit niedriger Latenz"
        AppLanguage.RUSSIAN -> "Высокопроизводительная платформа вывода с несколькими моделями с открытым исходным кодом. Сосредоточена на оптимизации вывода с низкой задержкой"
        AppLanguage.JAPANESE -> "高性能推論プラットフォーム、複数のオープンソースモデルをサポート。低レイテンシ推論最適化に注力"
        AppLanguage.KOREAN -> "고성능 추론 플랫폼, 여러 오픈소스 모델 지원. 저지연 추론 최적화에 집중"
    }
    val providerFireworksPricing: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "按模型计费，价格有竞争力"
        AppLanguage.ENGLISH -> "Pay per model, competitive pricing"
        AppLanguage.ARABIC -> "الدفع لكل نموذج، تسعير تنافسي"
        AppLanguage.PORTUGUESE -> "Pagamento por modelo, preço competitivo"
        AppLanguage.SPANISH -> "Pago por modelo, precio competitivo"
        AppLanguage.FRENCH -> "Paiement par modèle, prix compétitif"
        AppLanguage.GERMAN -> "Zahlung pro Modell, wettbewerbsfähiger Preis"
        AppLanguage.RUSSIAN -> "Оплата за модель, конкурентные цены"
        AppLanguage.JAPANESE -> "モデルごと課金、競争力のある価格"
        AppLanguage.KOREAN -> "모델별 과금, 경쟁력 있는 가격"
    }









    val providerOllama: String get() = "Ollama"
    val providerOllamaDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "本地运行开源模型，完全免费。需在电脑上安装 Ollama 并确保手机可访问"
        AppLanguage.ENGLISH -> "Run open-source models locally, completely free. Install Ollama on PC and ensure mobile access"
        AppLanguage.ARABIC -> "تشغيل نماذج مفتوحة المصدر محلياً، مجاني تماماً. ثبت Ollama على الكمبيوتر وتأكد من الوصول عبر الجوال"
        AppLanguage.PORTUGUESE -> "Roda modelos de código aberto localmente, totalmente gratuito. Instale Ollama no PC e garanta acesso pelo celular"
        AppLanguage.SPANISH -> "Ejecuta modelos de código abierto localmente, totalmente gratis. Instala Ollama en el PC y asegura el acceso desde el móvil"
        AppLanguage.FRENCH -> "Exécute des modèles open source localement, entièrement gratuit. Installez Ollama sur PC et assurez l'accès depuis le mobile"
        AppLanguage.GERMAN -> "Open-Source-Modelle lokal ausführen, völlig kostenlos. Ollama auf dem PC installieren und Mobilzugriff sicherstellen"
        AppLanguage.RUSSIAN -> "Локальный запуск моделей с открытым исходным кодом, полностью бесплатно. Установите Ollama на ПК и обеспечьте доступ с телефона"
        AppLanguage.JAPANESE -> "オープンソースモデルをローカル実行、完全無料。PC に Ollama をインストールし、モバイルからのアクセスを確保"
        AppLanguage.KOREAN -> "오픈소스 모델을 로컬에서 실행, 완전 무료. PC에 Ollama 설치 후 모바일 접근 확인"
    }
    val providerOllamaPricing: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "完全免费（本地运行）"
        AppLanguage.ENGLISH -> "Completely free (local)"
        AppLanguage.ARABIC -> "مجاني تماماً (محلي)"
        AppLanguage.PORTUGUESE -> "Totalmente gratuito (local)"
        AppLanguage.SPANISH -> "Totalmente gratis (local)"
        AppLanguage.FRENCH -> "Entièrement gratuit (local)"
        AppLanguage.GERMAN -> "Vollständig kostenlos (lokal)"
        AppLanguage.RUSSIAN -> "Полностью бесплатно (локально)"
        AppLanguage.JAPANESE -> "完全無料(ローカル)"
        AppLanguage.KOREAN -> "완전 무료(로컬)"
    }

    val providerLmStudio: String get() = "LM Studio"
    val providerLmStudioDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "桌面端本地模型运行工具，提供 OpenAI 兼容 API。图形化界面管理模型"
        AppLanguage.ENGLISH -> "Desktop local model runner with OpenAI-compatible API. GUI-based model management"
        AppLanguage.ARABIC -> "أداة تشغيل نماذج محلية لسطح المكتب مع واجهة متوافقة مع OpenAI. إدارة نماذج بواجهة رسومية"
        AppLanguage.PORTUGUESE -> "Executor de modelos locais para desktop com API compatível com OpenAI. Gerenciamento de modelos via interface gráfica"
        AppLanguage.SPANISH -> "Ejecutor de modelos locales para escritorio con API compatible con OpenAI. Gestión de modelos mediante interfaz gráfica"
        AppLanguage.FRENCH -> "Outil d'exécution de modèles locaux pour desktop avec API compatible OpenAI. Gestion des modèles via interface graphique"
        AppLanguage.GERMAN -> "Desktop-Tool für lokale Modelle mit OpenAI-kompatibler API. Modellverwaltung per grafischer Oberfläche"
        AppLanguage.RUSSIAN -> "Инструмент запуска локальных моделей для ПК с API, совместимым с OpenAI. Управление моделями через графический интерфейс"
        AppLanguage.JAPANESE -> "デスクトップ向けローカルモデル実行ツール、OpenAI 互換 API 提供。GUI でモデル管理"
        AppLanguage.KOREAN -> "데스크톱 로컬 모델 실행 도구, OpenAI 호환 API 제공. GUI 기반 모델 관리"
    }
    val providerLmStudioPricing: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "完全免费（本地运行）"
        AppLanguage.ENGLISH -> "Completely free (local)"
        AppLanguage.ARABIC -> "مجاني تماماً (محلي)"
        AppLanguage.PORTUGUESE -> "Totalmente gratuito (local)"
        AppLanguage.SPANISH -> "Totalmente gratis (local)"
        AppLanguage.FRENCH -> "Entièrement gratuit (local)"
        AppLanguage.GERMAN -> "Vollständig kostenlos (lokal)"
        AppLanguage.RUSSIAN -> "Полностью бесплатно (локально)"
        AppLanguage.JAPANESE -> "完全無料(ローカル)"
        AppLanguage.KOREAN -> "완전 무료(로컬)"
    }

    val providerVllm: String get() = "vLLM"
    val providerVllmDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "高性能 LLM 推理引擎，提供 OpenAI 兼容 API。适合自部署生产环境"
        AppLanguage.ENGLISH -> "High-performance LLM inference engine with OpenAI-compatible API. Suitable for self-hosted production"
        AppLanguage.ARABIC -> "محرك استدلال LLM عالي الأداء مع واجهة متوافقة مع OpenAI. مناسب للاستضافة الذاتية في الإنتاج"
        AppLanguage.PORTUGUESE -> "Motor de inferência LLM de alto desempenho com API compatível com OpenAI. Adequado para produção autogerenciada"
        AppLanguage.SPANISH -> "Motor de inferencia LLM de alto rendimiento con API compatible con OpenAI. Adecuado para producción autoalojada"
        AppLanguage.FRENCH -> "Moteur d'inférence LLM haute performance avec API compatible OpenAI. Adapté à la production auto-hébergée"
        AppLanguage.GERMAN -> "Hochleistungs-LLM-Inferenz-Engine mit OpenAI-kompatibler API. Geeignet für selbstgehostete Produktion"
        AppLanguage.RUSSIAN -> "Высокопроизводительный движок вывода LLM с API, совместимым с OpenAI. Подходит для самостоятельного продакшена"
        AppLanguage.JAPANESE -> "高性能LLM推論エンジン、OpenAI 互換 API 提供。セルフホスト本番環境に適合"
        AppLanguage.KOREAN -> "고성능 LLM 추론 엔진, OpenAI 호환 API 제공. 셀프 호스팅 프로덕션에 적합"
    }
    val providerVllmPricing: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "完全免费（自部署）"
        AppLanguage.ENGLISH -> "Completely free (self-hosted)"
        AppLanguage.ARABIC -> "مجاني تماماً (استضافة ذاتية)"
        AppLanguage.PORTUGUESE -> "Totalmente gratuito (autogerenciado)"
        AppLanguage.SPANISH -> "Totalmente gratis (autoalojado)"
        AppLanguage.FRENCH -> "Entièrement gratuit (auto-hébergé)"
        AppLanguage.GERMAN -> "Vollständig kostenlos (selbstgehostet)"
        AppLanguage.RUSSIAN -> "Полностью бесплатно (самохостинг)"
        AppLanguage.JAPANESE -> "完全無料(セルフホスト)"
        AppLanguage.KOREAN -> "완전 무료(셀프 호스팅)"
    }

    val archUniversal: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "通用"
        AppLanguage.ENGLISH -> "Universal"
        AppLanguage.ARABIC -> "عالمي"
        AppLanguage.PORTUGUESE -> "Universal"
        AppLanguage.SPANISH -> "Universal"
        AppLanguage.FRENCH -> "Universel"
        AppLanguage.GERMAN -> "Universell"
        AppLanguage.RUSSIAN -> "Универсальная"
        AppLanguage.JAPANESE -> "ユニバーサル"
        AppLanguage.KOREAN -> "범용"
    }
    val archUniversalDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "支持所有设备，APK体积较大"
        AppLanguage.ENGLISH -> "Supports all devices, larger APK size"
        AppLanguage.ARABIC -> "يدعم جميع الأجهزة، حجم APK أكبر"
        AppLanguage.PORTUGUESE -> "Suporta todos os dispositivos, APK maior"
        AppLanguage.SPANISH -> "Soporta todos los dispositivos, APK más grande"
        AppLanguage.FRENCH -> "Prend en charge tous les appareils, APK plus volumineux"
        AppLanguage.GERMAN -> "Unterstützt alle Geräte, größere APK-Größe"
        AppLanguage.RUSSIAN -> "Поддерживает все устройства, больший размер APK"
        AppLanguage.JAPANESE -> "全デバイス対応、APKサイズ大"
        AppLanguage.KOREAN -> "모든 기기 지원, APK 크기 큼"
    }
    val archArm64: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "仅 64 位"
        AppLanguage.ENGLISH -> "64-bit only"
        AppLanguage.ARABIC -> "64 بت فقط"
        AppLanguage.PORTUGUESE -> "Apenas 64 bits"
        AppLanguage.SPANISH -> "Solo 64 bits"
        AppLanguage.FRENCH -> "64 bits uniquement"
        AppLanguage.GERMAN -> "Nur 64-Bit"
        AppLanguage.RUSSIAN -> "Только 64-бит"
        AppLanguage.JAPANESE -> "64ビットのみ"
        AppLanguage.KOREAN -> "64비트 전용"
    }
    val archArm64Desc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "适合现代设备，APK体积较小"
        AppLanguage.ENGLISH -> "For modern devices, smaller APK size"
        AppLanguage.ARABIC -> "للأجهزة الحديثة، حجم APK أصغر"
        AppLanguage.PORTUGUESE -> "Para dispositivos modernos, APK menor"
        AppLanguage.SPANISH -> "Para dispositivos modernos, APK más pequeño"
        AppLanguage.FRENCH -> "Pour appareils modernes, APK plus petit"
        AppLanguage.GERMAN -> "Für moderne Geräte, kleinere APK-Größe"
        AppLanguage.RUSSIAN -> "Для современных устройств, меньший размер APK"
        AppLanguage.JAPANESE -> "モダンデバイス向け、APKサイズ小"
        AppLanguage.KOREAN -> "최신 기기용, APK 크기 작음"
    }
    val archArm32: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "仅 32 位"
        AppLanguage.ENGLISH -> "32-bit only"
        AppLanguage.ARABIC -> "32 بت فقط"
        AppLanguage.PORTUGUESE -> "Apenas 32 bits"
        AppLanguage.SPANISH -> "Solo 32 bits"
        AppLanguage.FRENCH -> "32 bits uniquement"
        AppLanguage.GERMAN -> "Nur 32-Bit"
        AppLanguage.RUSSIAN -> "Только 32-бит"
        AppLanguage.JAPANESE -> "32ビットのみ"
        AppLanguage.KOREAN -> "32비트 전용"
    }
    val archArm32Desc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "兼容老旧设备，APK体积较小"
        AppLanguage.ENGLISH -> "Compatible with older devices, smaller APK size"
        AppLanguage.ARABIC -> "متوافق مع الأجهزة القديمة، حجم APK أصغر"
        AppLanguage.PORTUGUESE -> "Compatível com dispositivos antigos, APK menor"
        AppLanguage.SPANISH -> "Compatible con dispositivos antiguos, APK más pequeño"
        AppLanguage.FRENCH -> "Compatible avec les anciens appareils, APK plus petit"
        AppLanguage.GERMAN -> "Kompatibel mit älteren Geräten, kleinere APK-Größe"
        AppLanguage.RUSSIAN -> "Совместим со старыми устройствами, меньший размер APK"
        AppLanguage.JAPANESE -> "旧デバイス互換、APKサイズ小"
        AppLanguage.KOREAN -> "구형 기기 호환, APK 크기 작음"
    }

    val hostsAdGuardDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "AdGuard DNS 过滤规则"
        AppLanguage.ENGLISH -> "AdGuard DNS filter rules"
        AppLanguage.ARABIC -> "قواعد تصفية AdGuard DNS"
        AppLanguage.PORTUGUESE -> "Regras de filtro DNS do AdGuard"
        AppLanguage.SPANISH -> "Reglas de filtro DNS de AdGuard"
        AppLanguage.FRENCH -> "Règles de filtrage DNS AdGuard"
        AppLanguage.GERMAN -> "AdGuard DNS-Filterregeln"
        AppLanguage.RUSSIAN -> "Правила фильтрации DNS AdGuard"
        AppLanguage.JAPANESE -> "AdGuard DNS フィルタールール"
        AppLanguage.KOREAN -> "AdGuard DNS 필터 규칙"
    }
    val hostsStevenBlackDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "统一的 hosts 文件，拦截广告和恶意软件"
        AppLanguage.ENGLISH -> "Unified hosts file, blocks ads and malware"
        AppLanguage.ARABIC -> "ملف hosts موحد، يحظر الإعلانات والبرامج الضارة"
        AppLanguage.PORTUGUESE -> "Arquivo hosts unificado, bloqueia anúncios e malware"
        AppLanguage.SPANISH -> "Archivo hosts unificado, bloquea anuncios y malware"
        AppLanguage.FRENCH -> "Fichier hosts unifié, bloque les pubs et les malwares"
        AppLanguage.GERMAN -> "Vereinigte hosts-Datei, blockiert Werbung und Malware"
        AppLanguage.RUSSIAN -> "Единый файл hosts, блокирует рекламу и вредоносное ПО"
        AppLanguage.JAPANESE -> "統合 hosts ファイル、広告とマルウェアをブロック"
        AppLanguage.KOREAN -> "통합 hosts 파일, 광고 및 악성코드 차단"
    }
    val hostsAdAwayDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "AdAway 默认广告拦截列表"
        AppLanguage.ENGLISH -> "AdAway default ad blocking list"
        AppLanguage.ARABIC -> "قائمة حظر الإعلانات الافتراضية لـ AdAway"
        AppLanguage.PORTUGUESE -> "Lista padrão de bloqueio de anúncios do AdAway"
        AppLanguage.SPANISH -> "Lista predeterminada de bloqueo de anuncios de AdAway"
        AppLanguage.FRENCH -> "Liste de blocage de pubs par défaut d'AdAway"
        AppLanguage.GERMAN -> "AdAway Standard-Werbeblockliste"
        AppLanguage.RUSSIAN -> "Список блокировки рекламы по умолчанию AdAway"
        AppLanguage.JAPANESE -> "AdAway デフォルト広告ブロックリスト"
        AppLanguage.KOREAN -> "AdAway 기본 광고 차단 목록"
    }
    val hosts1HostsLiteDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "轻量级广告和跟踪拦截"
        AppLanguage.ENGLISH -> "Lightweight ad and tracking blocking"
        AppLanguage.ARABIC -> "حظر خفيف للإعلانات والتتبع"
        AppLanguage.PORTUGUESE -> "Bloqueio leve de anúncios e rastreamento"
        AppLanguage.SPANISH -> "Bloqueo ligero de anuncios y rastreo"
        AppLanguage.FRENCH -> "Blocage léger des pubs et du suivi"
        AppLanguage.GERMAN -> "Leichtes Blockieren von Werbung und Tracking"
        AppLanguage.RUSSIAN -> "Легкая блокировка рекламы и отслеживания"
        AppLanguage.JAPANESE -> "軽量な広告・トラッキングブロック"
        AppLanguage.KOREAN -> "가벼운 광고 및 추적 차단"
    }




    val embeddedEngineTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "内嵌浏览器引擎"
        AppLanguage.ENGLISH -> "Embedded Browser Engine"
        AppLanguage.ARABIC -> "محرك المتصفح المضمن"
        AppLanguage.PORTUGUESE -> "Motor de Navegador Integrado"
        AppLanguage.SPANISH -> "Motor de Navegador Integrado"
        AppLanguage.FRENCH -> "Moteur de Navigateur Intégré"
        AppLanguage.GERMAN -> "Eingebettete Browser-Engine"
        AppLanguage.RUSSIAN -> "Встроенный движок браузера"
        AppLanguage.JAPANESE -> "組み込みブラウザエンジン"
        AppLanguage.KOREAN -> "내장 브라우저 엔진"
    }
    val embeddedEngineDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下载独立浏览器引擎，可嵌入导出的 APK 中"
        AppLanguage.ENGLISH -> "Download standalone engine to embed in exported APKs"
        AppLanguage.ARABIC -> "تنزيل محرك مستقل لتضمينه في ملفات APK المصدرة"
        AppLanguage.PORTUGUESE -> "Baixar motor independente para incorporar nos APKs exportados"
        AppLanguage.SPANISH -> "Descargar motor independiente para incrustar en los APKs exportados"
        AppLanguage.FRENCH -> "Télécharger un moteur autonome à intégrer dans les APKs exportés"
        AppLanguage.GERMAN -> "Eigenständige Engine herunterladen, um sie in exportierte APKs einzubetten"
        AppLanguage.RUSSIAN -> "Скачать автономный движок для встраивания в экспортируемые APK"
        AppLanguage.JAPANESE -> "独立エンジンをダウンロードしてエクスポートAPKに組み込み"
        AppLanguage.KOREAN -> "독립 엔진을 다운로드하여 내보낸 APK에 포함"
    }
    val engineSystemWebView: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "系统 WebView"
        AppLanguage.ENGLISH -> "System WebView"
        AppLanguage.ARABIC -> "WebView النظام"
        AppLanguage.PORTUGUESE -> "WebView do Sistema"
        AppLanguage.SPANISH -> "WebView del Sistema"
        AppLanguage.FRENCH -> "WebView Système"
        AppLanguage.GERMAN -> "System-WebView"
        AppLanguage.RUSSIAN -> "Системный WebView"
        AppLanguage.JAPANESE -> "システム WebView"
        AppLanguage.KOREAN -> "시스템 WebView"
    }
    val engineSystemWebViewDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "使用设备内置的 Android WebView，零额外体积"
        AppLanguage.ENGLISH -> "Uses built-in Android WebView, zero extra size"
        AppLanguage.ARABIC -> "يستخدم WebView المدمج في Android، بدون حجم إضافي"
        AppLanguage.PORTUGUESE -> "Usa o WebView do Android integrado, sem tamanho extra"
        AppLanguage.SPANISH -> "Usa el WebView de Android integrado, sin tamaño extra"
        AppLanguage.FRENCH -> "Utilise le WebView Android intégré, taille supplémentaire nulle"
        AppLanguage.GERMAN -> "Verwendet das integrierte Android-WebView, keine zusätzliche Größe"
        AppLanguage.RUSSIAN -> "Использует встроенный Android WebView, нулевой дополнительный размер"
        AppLanguage.JAPANESE -> "組み込みの Android WebView を使用、追加サイズなし"
        AppLanguage.KOREAN -> "내장 Android WebView 사용, 추가 용량 없음"
    }
    val engineGeckoView: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "GeckoView (Firefox)"
        AppLanguage.ENGLISH -> "GeckoView (Firefox)"
        AppLanguage.ARABIC -> "GeckoView (Firefox)"
        AppLanguage.PORTUGUESE -> "GeckoView (Firefox)"
        AppLanguage.SPANISH -> "GeckoView (Firefox)"
        AppLanguage.FRENCH -> "GeckoView (Firefox)"
        AppLanguage.GERMAN -> "GeckoView (Firefox)"
        AppLanguage.RUSSIAN -> "GeckoView (Firefox)"
        AppLanguage.JAPANESE -> "GeckoView (Firefox)"
        AppLanguage.KOREAN -> "GeckoView (Firefox)"
    }
    val engineGeckoViewDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "内嵌 Firefox 引擎，内置隐私保护与广告拦截"
        AppLanguage.ENGLISH -> "Embedded Firefox engine with privacy protection & ad blocking"
        AppLanguage.ARABIC -> "محرك Firefox مضمن مع حماية الخصوصية وحظر الإعلانات"
        AppLanguage.PORTUGUESE -> "Motor Firefox integrado com proteção de privacidade e bloqueio de anúncios"
        AppLanguage.SPANISH -> "Motor Firefox integrado con protección de privacidad y bloqueo de anuncios"
        AppLanguage.FRENCH -> "Moteur Firefox intégré avec protection de la vie privée et blocage des pubs"
        AppLanguage.GERMAN -> "Integrierte Firefox-Engine mit Datenschutz und Werbeblocker"
        AppLanguage.RUSSIAN -> "Встроенный движок Firefox с защитой приватности и блокировкой рекламы"
        AppLanguage.JAPANESE -> "組み込み Firefox エンジン、プライバシー保護と広告ブロック内蔵"
        AppLanguage.KOREAN -> "내장 Firefox 엔진, 개인정보 보호 및 광고 차단 포함"
    }
    val engineReady: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "就绪"
        AppLanguage.ENGLISH -> "Ready"
        AppLanguage.ARABIC -> "جاهز"
        AppLanguage.PORTUGUESE -> "Pronto"
        AppLanguage.SPANISH -> "Listo"
        AppLanguage.FRENCH -> "Prêt"
        AppLanguage.GERMAN -> "Bereit"
        AppLanguage.RUSSIAN -> "Готов"
        AppLanguage.JAPANESE -> "準備完了"
        AppLanguage.KOREAN -> "준비됨"
    }
    val engineNotDownloaded: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "未下载"
        AppLanguage.ENGLISH -> "Not Downloaded"
        AppLanguage.ARABIC -> "لم يتم التنزيل"
        AppLanguage.PORTUGUESE -> "Não baixado"
        AppLanguage.SPANISH -> "No descargado"
        AppLanguage.FRENCH -> "Non téléchargé"
        AppLanguage.GERMAN -> "Nicht heruntergeladen"
        AppLanguage.RUSSIAN -> "Не скачано"
        AppLanguage.JAPANESE -> "未ダウンロード"
        AppLanguage.KOREAN -> "다운로드 안 됨"
    }
    val engineDownloaded: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已下载"
        AppLanguage.ENGLISH -> "Downloaded"
        AppLanguage.ARABIC -> "تم التنزيل"
        AppLanguage.PORTUGUESE -> "Baixado"
        AppLanguage.SPANISH -> "Descargado"
        AppLanguage.FRENCH -> "Téléchargé"
        AppLanguage.GERMAN -> "Heruntergeladen"
        AppLanguage.RUSSIAN -> "Скачано"
        AppLanguage.JAPANESE -> "ダウンロード済み"
        AppLanguage.KOREAN -> "다운로드됨"
    }
    val engineDownloadBtn: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下载引擎"
        AppLanguage.ENGLISH -> "Download Engine"
        AppLanguage.ARABIC -> "تنزيل المحرك"
        AppLanguage.PORTUGUESE -> "Baixar Motor"
        AppLanguage.SPANISH -> "Descargar Motor"
        AppLanguage.FRENCH -> "Télécharger le Moteur"
        AppLanguage.GERMAN -> "Engine herunterladen"
        AppLanguage.RUSSIAN -> "Скачать движок"
        AppLanguage.JAPANESE -> "エンジンをダウンロード"
        AppLanguage.KOREAN -> "엔진 다운로드"
    }
    val engineDeleteBtn: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "删除引擎"
        AppLanguage.ENGLISH -> "Delete Engine"
        AppLanguage.ARABIC -> "حذف المحرك"
        AppLanguage.PORTUGUESE -> "Excluir Motor"
        AppLanguage.SPANISH -> "Eliminar Motor"
        AppLanguage.FRENCH -> "Supprimer le Moteur"
        AppLanguage.GERMAN -> "Engine löschen"
        AppLanguage.RUSSIAN -> "Удалить движок"
        AppLanguage.JAPANESE -> "エンジンを削除"
        AppLanguage.KOREAN -> "엔진 삭제"
    }
    val engineDownloading: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "正在下载..."
        AppLanguage.ENGLISH -> "Downloading..."
        AppLanguage.ARABIC -> "جاري التنزيل..."
        AppLanguage.PORTUGUESE -> "Baixando..."
        AppLanguage.SPANISH -> "Descargando..."
        AppLanguage.FRENCH -> "Téléchargement..."
        AppLanguage.GERMAN -> "Wird heruntergeladen..."
        AppLanguage.RUSSIAN -> "Скачивание..."
        AppLanguage.JAPANESE -> "ダウンロード中..."
        AppLanguage.KOREAN -> "다운로드 중..."
    }
    val engineCancelDownload: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "取消下载"
        AppLanguage.ENGLISH -> "Cancel Download"
        AppLanguage.ARABIC -> "إلغاء التنزيل"
        AppLanguage.PORTUGUESE -> "Cancelar Download"
        AppLanguage.SPANISH -> "Cancelar Descarga"
        AppLanguage.FRENCH -> "Annuler le Téléchargement"
        AppLanguage.GERMAN -> "Download abbrechen"
        AppLanguage.RUSSIAN -> "Отменить скачивание"
        AppLanguage.JAPANESE -> "ダウンロードをキャンセル"
        AppLanguage.KOREAN -> "다운로드 취소"
    }
    val engineRetry: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "重试"
        AppLanguage.ENGLISH -> "Retry"
        AppLanguage.ARABIC -> "إعادة المحاولة"
        AppLanguage.PORTUGUESE -> "Repetir"
        AppLanguage.SPANISH -> "Reintentar"
        AppLanguage.FRENCH -> "Réessayer"
        AppLanguage.GERMAN -> "Erneut versuchen"
        AppLanguage.RUSSIAN -> "Повторить"
        AppLanguage.JAPANESE -> "再試行"
        AppLanguage.KOREAN -> "재시도"
    }
    val engineEstimatedSize: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "预估大小"
        AppLanguage.ENGLISH -> "Est. size"
        AppLanguage.ARABIC -> "الحجم المقدر"
        AppLanguage.PORTUGUESE -> "Tam. est."
        AppLanguage.SPANISH -> "Tamaño est."
        AppLanguage.FRENCH -> "Taille est."
        AppLanguage.GERMAN -> "Geschätzte Größe"
        AppLanguage.RUSSIAN -> "Оцен. размер"
        AppLanguage.JAPANESE -> "推定サイズ"
        AppLanguage.KOREAN -> "예상 용량"
    }
    val engineCurrentSize: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "占用空间"
        AppLanguage.ENGLISH -> "Disk usage"
        AppLanguage.ARABIC -> "استخدام القرص"
        AppLanguage.PORTUGUESE -> "Uso de disco"
        AppLanguage.SPANISH -> "Uso de disco"
        AppLanguage.FRENCH -> "Utilisation du disque"
        AppLanguage.GERMAN -> "Speichernutzung"
        AppLanguage.RUSSIAN -> "Использование диска"
        AppLanguage.JAPANESE -> "ディスク使用量"
        AppLanguage.KOREAN -> "디스크 사용량"
    }
    val engineDeleteConfirm: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "确定要删除已下载的引擎文件吗？"
        AppLanguage.ENGLISH -> "Are you sure you want to delete the engine files?"
        AppLanguage.ARABIC -> "هل أنت متأكد من حذف ملفات المحرك؟"
        AppLanguage.PORTUGUESE -> "Tem certeza de que deseja excluir os arquivos do motor?"
        AppLanguage.SPANISH -> "¿Estás seguro de que quieres eliminar los archivos del motor?"
        AppLanguage.FRENCH -> "Êtes-vous sûr de vouloir supprimer les fichiers du moteur ?"
        AppLanguage.GERMAN -> "Möchtest du die Engine-Dateien wirklich löschen?"
        AppLanguage.RUSSIAN -> "Вы уверены, что хотите удалить файлы движка?"
        AppLanguage.JAPANESE -> "エンジンファイルを削除してもよろしいですか？"
        AppLanguage.KOREAN -> "엔진 파일을 삭제하시겠습니까?"
    }
    val engineVersionLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "版本"
        AppLanguage.ENGLISH -> "Version"
        AppLanguage.ARABIC -> "الإصدار"
        AppLanguage.PORTUGUESE -> "Versão"
        AppLanguage.SPANISH -> "Versión"
        AppLanguage.FRENCH -> "Version"
        AppLanguage.GERMAN -> "Version"
        AppLanguage.RUSSIAN -> "Версия"
        AppLanguage.JAPANESE -> "バージョン"
        AppLanguage.KOREAN -> "버전"
    }
    val engineDefault: String get() = when (Strings.lang) {
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
    val engineSelectTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "APK 内置浏览器引擎"
        AppLanguage.ENGLISH -> "APK Embedded Browser Engine"
        AppLanguage.ARABIC -> "محرك متصفح مضمن في APK"
        AppLanguage.PORTUGUESE -> "Motor de Navegador Integrado no APK"
        AppLanguage.SPANISH -> "Motor de Navegador Integrado en APK"
        AppLanguage.FRENCH -> "Moteur de Navigateur Intégré au APK"
        AppLanguage.GERMAN -> "Im APK integrierte Browser-Engine"
        AppLanguage.RUSSIAN -> "Движок браузера, встроенный в APK"
        AppLanguage.JAPANESE -> "APK 組み込みブラウザエンジン"
        AppLanguage.KOREAN -> "APK 내장 브라우저 엔진"
    }
    val engineSelectDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择随导出 APK 一起打包的浏览器引擎，而不是跳转到外部浏览器"
        AppLanguage.ENGLISH -> "Select the browser engine packaged into the exported APK, not an external browser launch"
        AppLanguage.ARABIC -> "اختر محرك المتصفح المضمن داخل ملف APK المصدر، وليس فتح متصفح خارجي"
        AppLanguage.PORTUGUESE -> "Selecione o motor de navegador embalado no APK exportado, em vez de abrir um navegador externo"
        AppLanguage.SPANISH -> "Selecciona el motor de navegador empaquetado en el APK exportado, en lugar de abrir un navegador externo"
        AppLanguage.FRENCH -> "Sélectionnez le moteur de navigateur embarqué dans l'APK exporté, au lieu de lancer un navigateur externe"
        AppLanguage.GERMAN -> "Wähle die Browser-Engine, die ins exportierte APK gepackt wird, anstatt einen externen Browser zu öffnen"
        AppLanguage.RUSSIAN -> "Выберите движок браузера, упакованный в экспортируемый APK, а не запуск внешнего браузера"
        AppLanguage.JAPANESE -> "エクスポートAPKにパッケージするブラウザエンジンを選択(外部ブラウザ起動ではなく)"
        AppLanguage.KOREAN -> "내보낸 APK에 패키징할 브라우저 엔진 선택(외부 브라우저 실행 아님)"
    }
    val engineGeckoNotDownloaded: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "GeckoView 引擎未下载，请先在“浏览器内核”页面下载"
        AppLanguage.ENGLISH -> "GeckoView engine not downloaded. Please download it from Browser Kernel page first."
        AppLanguage.ARABIC -> "لم يتم تنزيل محرك GeckoView. يرجى تنزيله من صفحة نواة المتصفح أولاً."
        AppLanguage.PORTUGUESE -> "Motor GeckoView não baixado. Baixe-o na página Núcleo do Navegador primeiro."
        AppLanguage.SPANISH -> "Motor GeckoView no descargado. Descárgalo desde la página Núcleo del Navegador primero."
        AppLanguage.FRENCH -> "Moteur GeckoView non téléchargé. Veuillez le télécharger depuis la page Noyau du Navigateur d'abord."
        AppLanguage.GERMAN -> "GeckoView-Engine nicht heruntergeladen. Bitte zuerst auf der Browser-Kernel-Seite herunterladen."
        AppLanguage.RUSSIAN -> "Движок GeckoView не скачан. Сначала скачайте его на странице Ядра Браузера."
        AppLanguage.JAPANESE -> "GeckoView エンジンが未ダウンロードです。先にブラウザカーネルページからダウンロードしてください。"
        AppLanguage.KOREAN -> "GeckoView 엔진이 다운로드되지 않았습니다. 브라우저 커널 페이지에서 먼저 다운로드하세요."
    }
    val engineApkSizeWarning: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "GeckoView 会被打进 APK，体积约增加 %s MB"
        AppLanguage.ENGLISH -> "GeckoView is packaged into the APK and adds ~%s MB"
        AppLanguage.ARABIC -> "سيتم تضمين GeckoView داخل APK وسيضيف نحو %s MB"
        AppLanguage.PORTUGUESE -> "GeckoView é embalado no APK e adiciona ~%s MB"
        AppLanguage.SPANISH -> "GeckoView se empaqueta en el APK y añade ~%s MB"
        AppLanguage.FRENCH -> "GeckoView est embarqué dans l'APK et ajoute ~%s MB"
        AppLanguage.GERMAN -> "GeckoView wird ins APK gepackt und fügt ~%s MB hinzu"
        AppLanguage.RUSSIAN -> "GeckoView упаковывается в APK и добавляет ~%s МБ"
        AppLanguage.JAPANESE -> "GeckoView はAPKに同梱され、約 %s MB 追加されます"
        AppLanguage.KOREAN -> "GeckoView가 APK에 패키징되어 ~%s MB 추가됩니다"
    }

    val deepLinkSetting: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "链接打开"
        AppLanguage.ENGLISH -> "Deep Link"
        AppLanguage.ARABIC -> "الرابط العميق"
        AppLanguage.PORTUGUESE -> "Enlace Profundo"
        AppLanguage.SPANISH -> "Enlace Profundo"
        AppLanguage.FRENCH -> "Lien Profond"
        AppLanguage.GERMAN -> "Deep-Link"
        AppLanguage.RUSSIAN -> "Диплинк"
        AppLanguage.JAPANESE -> "ディープリンク"
        AppLanguage.KOREAN -> "딥 링크"
    }

    val deepLinkSettingHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "允许从外部打开匹配的链接"
        AppLanguage.ENGLISH -> "Allow opening matching links from external apps"
        AppLanguage.ARABIC -> "السماح بفتح الروابط المطابقة من التطبيقات الخارجية"
        AppLanguage.PORTUGUESE -> "Permitir abrir links correspondentes de apps externos"
        AppLanguage.SPANISH -> "Permitir abrir enlaces coincidentes desde apps externas"
        AppLanguage.FRENCH -> "Autoriser l'ouverture des liens correspondants depuis les apps externes"
        AppLanguage.GERMAN -> "Öffnen passender Links aus externen Apps erlauben"
        AppLanguage.RUSSIAN -> "Разрешать открывать совпадающие ссылки из внешних приложений"
        AppLanguage.JAPANESE -> "外部アプリから一致するリンクを開くことを許可"
        AppLanguage.KOREAN -> "외부 앱에서 일치하는 링크 열기 허용"
    }

    val deepLinkCustomHostsLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "额外匹配域名"
        AppLanguage.ENGLISH -> "Additional Domains"
        AppLanguage.ARABIC -> "نطاقات إضافية"
        AppLanguage.PORTUGUESE -> "Domínios Adicionais"
        AppLanguage.SPANISH -> "Dominios Adicionales"
        AppLanguage.FRENCH -> "Domaines Supplémentaires"
        AppLanguage.GERMAN -> "Zusätzliche Domains"
        AppLanguage.RUSSIAN -> "Дополнительные домены"
        AppLanguage.JAPANESE -> "追加ドメイン"
        AppLanguage.KOREAN -> "추가 도메인"
    }

    val deepLinkCustomHostsHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "主域名和 www 子域名会自动匹配，此处可添加其他需要拦截的域名（每行一个）"
        AppLanguage.ENGLISH -> "Main domain and www subdomain are auto-matched. Add extra domains here (one per line)"
        AppLanguage.ARABIC -> "يتم مطابقة النطاق الرئيسي والنطاق الفرعي www تلقائيًا. أضف نطاقات إضافية هنا (واحد لكل سطر)"
        AppLanguage.PORTUGUESE -> "Domínio principal e subdomínio www são correspondidos automaticamente. Adicione domínios extras aqui (um por linha)"
        AppLanguage.SPANISH -> "El dominio principal y el subdominio www se coinciden automáticamente. Añade dominios extra aquí (uno por línea)"
        AppLanguage.FRENCH -> "Le domaine principal et le sous-domaine www sont correspondus automatiquement. Ajoutez des domaines supplémentaires ici (un par ligne)"
        AppLanguage.GERMAN -> "Hauptdomain und www-Subdomain werden automatisch gematcht. Hier weitere Domains hinzufügen (eine pro Zeile)"
        AppLanguage.RUSSIAN -> "Основной домен и поддомен www сопоставляются автоматически. Добавьте дополнительные домены здесь (по одному на строку)"
        AppLanguage.JAPANESE -> "メインドメインと www サブドメインは自動マッチングされます。ここに追加ドメインを入力(1行1件)"
        AppLanguage.KOREAN -> "메인 도메인과 www 서브도메인은 자동 매칭됩니다. 여기에 추가 도메인을 입력(한 줄에 하나씩)"
    }

    val phpExtensions: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "PHP 扩展"
        AppLanguage.ENGLISH -> "PHP Extensions"
        AppLanguage.ARABIC -> "إضافات PHP"
        AppLanguage.PORTUGUESE -> "Extensões PHP"
        AppLanguage.SPANISH -> "Extensiones PHP"
        AppLanguage.FRENCH -> "Extensions PHP"
        AppLanguage.GERMAN -> "PHP-Erweiterungen"
        AppLanguage.RUSSIAN -> "Расширения PHP"
        AppLanguage.JAPANESE -> "PHP 拡張機能"
        AppLanguage.KOREAN -> "PHP 확장"
    }
    val phpExtensionsHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "启用项目所需的 PHP 扩展模块"
        AppLanguage.ENGLISH -> "Enable PHP extensions required by your project"
        AppLanguage.ARABIC -> "تمكين إضافات PHP المطلوبة لمشروعك"
        AppLanguage.PORTUGUESE -> "Ative as extensões PHP necessárias para o seu projeto"
        AppLanguage.SPANISH -> "Habilita las extensiones PHP requeridas por tu proyecto"
        AppLanguage.FRENCH -> "Activez les extensions PHP requises par votre projet"
        AppLanguage.GERMAN -> "Aktiviere die von deinem Projekt benötigten PHP-Erweiterungen"
        AppLanguage.RUSSIAN -> "Включите расширения PHP, необходимые для вашего проекта"
        AppLanguage.JAPANESE -> "プロジェクトに必要な PHP 拡張機能を有効化"
        AppLanguage.KOREAN -> "프로젝트에 필요한 PHP 확장을 활성화하세요"
    }
    val phpCustomExtensions: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自定义扩展"
        AppLanguage.ENGLISH -> "Custom Extensions"
        AppLanguage.ARABIC -> "الإضافات المخصصة"
        AppLanguage.PORTUGUESE -> "Extensões Personalizadas"
        AppLanguage.SPANISH -> "Extensiones Personalizadas"
        AppLanguage.FRENCH -> "Extensions Personnalisées"
        AppLanguage.GERMAN -> "Benutzerdefinierte Erweiterungen"
        AppLanguage.RUSSIAN -> "Пользовательские расширения"
        AppLanguage.JAPANESE -> "カスタム拡張機能"
        AppLanguage.KOREAN -> "사용자 정의 확장"
    }
    val phpAddCustomExtension: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "添加自定义扩展"
        AppLanguage.ENGLISH -> "Add Custom Extension"
        AppLanguage.ARABIC -> "إضافة إضافة مخصصة"
        AppLanguage.PORTUGUESE -> "Adicionar Extensão Personalizada"
        AppLanguage.SPANISH -> "Añadir Extensión Personalizada"
        AppLanguage.FRENCH -> "Ajouter une Extension Personnalisée"
        AppLanguage.GERMAN -> "Benutzerdefinierte Erweiterung hinzufügen"
        AppLanguage.RUSSIAN -> "Добавить пользовательское расширение"
        AppLanguage.JAPANESE -> "カスタム拡張機能を追加"
        AppLanguage.KOREAN -> "사용자 정의 확장 추가"
    }
    val phpAddCustomExtensionButton: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "添加扩展"
        AppLanguage.ENGLISH -> "Add Extension"
        AppLanguage.ARABIC -> "إضافة الإضافة"
        AppLanguage.PORTUGUESE -> "Adicionar Extensão"
        AppLanguage.SPANISH -> "Añadir Extensión"
        AppLanguage.FRENCH -> "Ajouter une Extension"
        AppLanguage.GERMAN -> "Erweiterung hinzufügen"
        AppLanguage.RUSSIAN -> "Добавить расширение"
        AppLanguage.JAPANESE -> "拡張機能を追加"
        AppLanguage.KOREAN -> "확장 추가"
    }
    val phpCustomExtensionHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "将 .so 文件放入项目 php_exts/ 目录。扩展名用于加载指令，.so 文件名留空则自动补全。"
        AppLanguage.ENGLISH -> "Place .so files in project php_exts/. Extension name is used for the load directive; leave .so filename blank to auto-complete."
        AppLanguage.ARABIC -> "ضع ملفات .so في دليل المشروع php_exts/. يستخدم اسم الإضافة لتوجيه التحميل؛ اترك اسم ملف .so فارغاً للإكمال التلقائي."
        AppLanguage.PORTUGUESE -> "Coloque arquivos .so no diretório php_exts/ do projeto. O nome da extensão é usado para a diretiva de carregamento; deixe o nome do arquivo .so em branco para auto-completar."
        AppLanguage.SPANISH -> "Coloca archivos .so en el directorio php_exts/ del proyecto. El nombre de la extensión se usa para la directiva de carga; deja el nombre del archivo .so en blanco para autocompletar."
        AppLanguage.FRENCH -> "Placez les fichiers .so dans le répertoire php_exts/ du projet. Le nom de l'extension est utilisé pour la directive de chargement ; laissez le nom du fichier .so vide pour auto-compléter."
        AppLanguage.GERMAN -> "Lege .so-Dateien im Projektverzeichnis php_exts/ ab. Der Erweiterungsname wird für die Load-Directive verwendet; .so-Dateinamen leer lassen für Auto-Vervollständigung."
        AppLanguage.RUSSIAN -> "Поместите файлы .so в каталог php_exts/ проекта. Имя расширения используется для директивы загрузки; оставьте имя файла .so пустым для автозаполнения."
        AppLanguage.JAPANESE -> ".so ファイルをプロジェクトの php_exts/ ディレクトリに配置。拡張名はロードディレクティブに使用、.so ファイル名を空欄で自動補完。"
        AppLanguage.KOREAN -> ".so 파일을 프로젝트 php_exts/ 디렉토리에 배치. 확장명은 로드 지시문에 사용되며, .so 파일명을 비우면 자동 완성됩니다."
    }
    val phpCustomExtensionName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "扩展名"
        AppLanguage.ENGLISH -> "Extension Name"
        AppLanguage.ARABIC -> "اسم الإضافة"
        AppLanguage.PORTUGUESE -> "Nome da Extensão"
        AppLanguage.SPANISH -> "Nombre de Extensión"
        AppLanguage.FRENCH -> "Nom de l'Extension"
        AppLanguage.GERMAN -> "Erweiterungsname"
        AppLanguage.RUSSIAN -> "Имя расширения"
        AppLanguage.JAPANESE -> "拡張機能名"
        AppLanguage.KOREAN -> "확장 이름"
    }
    val phpCustomExtensionSoName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> ".so 文件名（可选）"
        AppLanguage.ENGLISH -> ".so Filename (Optional)"
        AppLanguage.ARABIC -> "اسم ملف .so (اختياري)"
        AppLanguage.PORTUGUESE -> "Nome do arquivo .so (Opcional)"
        AppLanguage.SPANISH -> "Nombre del archivo .so (Opcional)"
        AppLanguage.FRENCH -> "Nom du fichier .so (Facultatif)"
        AppLanguage.GERMAN -> ".so-Dateiname (Optional)"
        AppLanguage.RUSSIAN -> "Имя файла .so (необязательно)"
        AppLanguage.JAPANESE -> ".so ファイル名(任意)"
        AppLanguage.KOREAN -> ".so 파일명(선택)"
    }
    val phpCustomExtensionOrder: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "加载顺序"
        AppLanguage.ENGLISH -> "Load Order"
        AppLanguage.ARABIC -> "ترتيب التحميل"
        AppLanguage.PORTUGUESE -> "Ordem de Carregamento"
        AppLanguage.SPANISH -> "Orden de Carga"
        AppLanguage.FRENCH -> "Ordre de Chargement"
        AppLanguage.GERMAN -> "Ladereihenfolge"
        AppLanguage.RUSSIAN -> "Порядок загрузки"
        AppLanguage.JAPANESE -> "ロード順序"
        AppLanguage.KOREAN -> "로드 순서"
    }
    val nodeExtensions: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Node.js 扩展"
        AppLanguage.ENGLISH -> "Node.js Extensions"
        AppLanguage.ARABIC -> "إضافات Node.js"
        AppLanguage.PORTUGUESE -> "Extensões Node.js"
        AppLanguage.SPANISH -> "Extensiones Node.js"
        AppLanguage.FRENCH -> "Extensions Node.js"
        AppLanguage.GERMAN -> "Node.js-Erweiterungen"
        AppLanguage.RUSSIAN -> "Расширения Node.js"
        AppLanguage.JAPANESE -> "Node.js 拡張機能"
        AppLanguage.KOREAN -> "Node.js 확장"
    }
    val nodeExtensionsHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "将 .node 文件放入项目 nodejs_exts/ 目录。扩展名用于排序，.node 文件名留空则自动补全。"
        AppLanguage.ENGLISH -> "Place .node files in project nodejs_exts/. Extension name is used for ordering; leave .node filename blank to auto-complete."
        AppLanguage.ARABIC -> "ضع ملفات .node في دليل المشروع nodejs_exts/. يستخدم اسم الإضافة للترتيب؛ اترك اسم ملف .node فارغاً للإكمال التلقائي."
        AppLanguage.PORTUGUESE -> "Coloque arquivos .node no diretório nodejs_exts/ do projeto. O nome da extensão é usado para ordenação; deixe o nome do arquivo .node em branco para auto-completar."
        AppLanguage.SPANISH -> "Coloca archivos .node en el directorio nodejs_exts/ del proyecto. El nombre de la extensión se usa para ordenar; deja el nombre del archivo .node en blanco para autocompletar."
        AppLanguage.FRENCH -> "Placez les fichiers .node dans le répertoire nodejs_exts/ du projet. Le nom de l'extension est utilisé pour le tri ; laissez le nom du fichier .node vide pour auto-compléter."
        AppLanguage.GERMAN -> "Lege .node-Dateien im Projektverzeichnis nodejs_exts/ ab. Der Erweiterungsname wird für die Sortierung verwendet; .node-Dateinamen leer lassen für Auto-Vervollständigung."
        AppLanguage.RUSSIAN -> "Поместите файлы .node в каталог nodejs_exts/ проекта. Имя расширения используется для сортировки; оставьте имя файла .node пустым для автозаполнения."
        AppLanguage.JAPANESE -> ".node ファイルをプロジェクトの nodejs_exts/ ディレクトリに配置。拡張名はソートに使用、.node ファイル名を空欄で自動補完。"
        AppLanguage.KOREAN -> ".node 파일을 프로젝트 nodejs_exts/ 디렉토리에 배치. 확장명은 정렬에 사용되며, .node 파일명을 비우면 자동 완성됩니다."
    }
    val nodeCustomExtensions: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自定义扩展"
        AppLanguage.ENGLISH -> "Custom Extensions"
        AppLanguage.ARABIC -> "الإضافات المخصصة"
        AppLanguage.PORTUGUESE -> "Extensões Personalizadas"
        AppLanguage.SPANISH -> "Extensiones Personalizadas"
        AppLanguage.FRENCH -> "Extensions Personnalisées"
        AppLanguage.GERMAN -> "Benutzerdefinierte Erweiterungen"
        AppLanguage.RUSSIAN -> "Пользовательские расширения"
        AppLanguage.JAPANESE -> "カスタム拡張機能"
        AppLanguage.KOREAN -> "사용자 정의 확장"
    }
    val nodeAddCustomExtension: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "添加自定义扩展"
        AppLanguage.ENGLISH -> "Add Custom Extension"
        AppLanguage.ARABIC -> "إضافة إضافة مخصصة"
        AppLanguage.PORTUGUESE -> "Adicionar Extensão Personalizada"
        AppLanguage.SPANISH -> "Añadir Extensión Personalizada"
        AppLanguage.FRENCH -> "Ajouter une Extension Personnalisée"
        AppLanguage.GERMAN -> "Benutzerdefinierte Erweiterung hinzufügen"
        AppLanguage.RUSSIAN -> "Добавить пользовательское расширение"
        AppLanguage.JAPANESE -> "カスタム拡張機能を追加"
        AppLanguage.KOREAN -> "사용자 정의 확장 추가"
    }
    val nodeAddCustomExtensionButton: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "添加扩展"
        AppLanguage.ENGLISH -> "Add Extension"
        AppLanguage.ARABIC -> "إضافة الإضافة"
        AppLanguage.PORTUGUESE -> "Adicionar Extensão"
        AppLanguage.SPANISH -> "Añadir Extensión"
        AppLanguage.FRENCH -> "Ajouter une Extension"
        AppLanguage.GERMAN -> "Erweiterung hinzufügen"
        AppLanguage.RUSSIAN -> "Добавить расширение"
        AppLanguage.JAPANESE -> "拡張機能を追加"
        AppLanguage.KOREAN -> "확장 추가"
    }
    val nodeCustomExtensionHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "将 .node 文件放入项目 nodejs_exts/ 目录"
        AppLanguage.ENGLISH -> "Place .node files in project nodejs_exts/"
        AppLanguage.ARABIC -> "ضع ملفات .node في دليل المشروع nodejs_exts/"
        AppLanguage.PORTUGUESE -> "Coloque arquivos .node no diretório nodejs_exts/ do projeto"
        AppLanguage.SPANISH -> "Coloca archivos .node en el directorio nodejs_exts/ del proyecto"
        AppLanguage.FRENCH -> "Placez les fichiers .node dans le répertoire nodejs_exts/ du projet"
        AppLanguage.GERMAN -> "Lege .node-Dateien im Projektverzeichnis nodejs_exts/ ab"
        AppLanguage.RUSSIAN -> "Поместите файлы .node в каталог nodejs_exts/ проекта"
        AppLanguage.JAPANESE -> ".node ファイルをプロジェクトの nodejs_exts/ ディレクトリに配置"
        AppLanguage.KOREAN -> ".node 파일을 프로젝트 nodejs_exts/ 디렉토리에 배치"
    }
    val nodeCustomExtensionName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "扩展名"
        AppLanguage.ENGLISH -> "Extension Name"
        AppLanguage.ARABIC -> "اسم الإضافة"
        AppLanguage.PORTUGUESE -> "Nome da Extensão"
        AppLanguage.SPANISH -> "Nombre de Extensión"
        AppLanguage.FRENCH -> "Nom de l'Extension"
        AppLanguage.GERMAN -> "Erweiterungsname"
        AppLanguage.RUSSIAN -> "Имя расширения"
        AppLanguage.JAPANESE -> "拡張機能名"
        AppLanguage.KOREAN -> "확장 이름"
    }
    val nodeCustomExtensionNodeName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> ".node 文件名（可选）"
        AppLanguage.ENGLISH -> ".node Filename (Optional)"
        AppLanguage.ARABIC -> "اسم ملف .node (اختياري)"
        AppLanguage.PORTUGUESE -> "Nome do arquivo .node (Opcional)"
        AppLanguage.SPANISH -> "Nombre del archivo .node (Opcional)"
        AppLanguage.FRENCH -> "Nom du fichier .node (Facultatif)"
        AppLanguage.GERMAN -> ".node-Dateiname (Optional)"
        AppLanguage.RUSSIAN -> "Имя файла .node (необязательно)"
        AppLanguage.JAPANESE -> ".node ファイル名(任意)"
        AppLanguage.KOREAN -> ".node 파일명(선택)"
    }
    val nodeCustomExtensionOrder: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "加载顺序"
        AppLanguage.ENGLISH -> "Load Order"
        AppLanguage.ARABIC -> "ترتيب التحميل"
        AppLanguage.PORTUGUESE -> "Ordem de Carregamento"
        AppLanguage.SPANISH -> "Orden de Carga"
        AppLanguage.FRENCH -> "Ordre de Chargement"
        AppLanguage.GERMAN -> "Ladereihenfolge"
        AppLanguage.RUSSIAN -> "Порядок загрузки"
        AppLanguage.JAPANESE -> "ロード順序"
        AppLanguage.KOREAN -> "로드 순서"
    }
    val workingDirNotFound: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "工作目录不存在: %s"
        AppLanguage.ENGLISH -> "Working directory doesn't exist: %s"
        AppLanguage.ARABIC -> "دليل العمل غير موجود: %s"
        AppLanguage.PORTUGUESE -> "Diretório de trabalho não existe: %s"
        AppLanguage.SPANISH -> "El directorio de trabajo no existe: %s"
        AppLanguage.FRENCH -> "Le répertoire de travail n'existe pas : %s"
        AppLanguage.GERMAN -> "Arbeitsverzeichnis existiert nicht: %s"
        AppLanguage.RUSSIAN -> "Рабочий каталог не существует: %s"
        AppLanguage.JAPANESE -> "作業ディレクトリが存在しません: %s"
        AppLanguage.KOREAN -> "작업 디렉토리가 존재하지 않습니다: %s"
    }

    val buildLogFrameworkLine: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "框架: %s"
        AppLanguage.ENGLISH -> "Framework: %s"
        AppLanguage.ARABIC -> "إطار العمل: %s"
        AppLanguage.PORTUGUESE -> "Framework: %s"
        AppLanguage.SPANISH -> "Framework: %s"
        AppLanguage.FRENCH -> "Framework : %s"
        AppLanguage.GERMAN -> "Framework: %s"
        AppLanguage.RUSSIAN -> "Фреймворк: %s"
        AppLanguage.JAPANESE -> "フレームワーク: %s"
        AppLanguage.KOREAN -> "프레임워크: %s"
    }
    val buildLogRunningScript: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "执行构建脚本: %s"
        AppLanguage.ENGLISH -> "Running build script: %s"
        AppLanguage.ARABIC -> "تشغيل سكربت البناء: %s"
        AppLanguage.PORTUGUESE -> "Executando script de build: %s"
        AppLanguage.SPANISH -> "Ejecutando script de build: %s"
        AppLanguage.FRENCH -> "Exécution du script de build : %s"
        AppLanguage.GERMAN -> "Build-Skript wird ausgeführt: %s"
        AppLanguage.RUSSIAN -> "Запуск скрипта сборки: %s"
        AppLanguage.JAPANESE -> "ビルドスクリプトを実行中: %s"
        AppLanguage.KOREAN -> "빌드 스크립트 실행 중: %s"
    }

    val importLogStartScan: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "开始扫描项目..."
        AppLanguage.ENGLISH -> "Starting project scan..."
        AppLanguage.ARABIC -> "بدء فحص المشروع..."
        AppLanguage.PORTUGUESE -> "Iniciando scan do projeto..."
        AppLanguage.SPANISH -> "Iniciando escaneo del proyecto..."
        AppLanguage.FRENCH -> "Démarrage du scan du projet..."
        AppLanguage.GERMAN -> "Projekt-Scan wird gestartet..."
        AppLanguage.RUSSIAN -> "Запуск сканирования проекта..."
        AppLanguage.JAPANESE -> "プロジェクトのスキャンを開始..."
        AppLanguage.KOREAN -> "프로젝트 스캔 시작..."
    }
    val importLogDetectingType: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "检测项目类型..."
        AppLanguage.ENGLISH -> "Detecting project type..."
        AppLanguage.ARABIC -> "اكتشاف نوع المشروع..."
        AppLanguage.PORTUGUESE -> "Detectando tipo de projeto..."
        AppLanguage.SPANISH -> "Detectando tipo de proyecto..."
        AppLanguage.FRENCH -> "Détection du type de projet..."
        AppLanguage.GERMAN -> "Projekttyp wird erkannt..."
        AppLanguage.RUSSIAN -> "Определение типа проекта..."
        AppLanguage.JAPANESE -> "プロジェクトタイプを検出中..."
        AppLanguage.KOREAN -> "프로젝트 유형 감지 중..."
    }
    val importLogVersionLine: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "版本: %s"
        AppLanguage.ENGLISH -> "Version: %s"
        AppLanguage.ARABIC -> "الإصدار: %s"
        AppLanguage.PORTUGUESE -> "Versão: %s"
        AppLanguage.SPANISH -> "Versión: %s"
        AppLanguage.FRENCH -> "Version : %s"
        AppLanguage.GERMAN -> "Version: %s"
        AppLanguage.RUSSIAN -> "Версия: %s"
        AppLanguage.JAPANESE -> "バージョン: %s"
        AppLanguage.KOREAN -> "버전: %s"
    }
    val importLogFoundOutputDir: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "找到输出目录: %s"
        AppLanguage.ENGLISH -> "Found output directory: %s"
        AppLanguage.ARABIC -> "تم العثور على دليل الإخراج: %s"
        AppLanguage.PORTUGUESE -> "Diretório de saída encontrado: %s"
        AppLanguage.SPANISH -> "Directorio de salida encontrado: %s"
        AppLanguage.FRENCH -> "Répertoire de sortie trouvé : %s"
        AppLanguage.GERMAN -> "Ausgabeverzeichnis gefunden: %s"
        AppLanguage.RUSSIAN -> "Найден каталог вывода: %s"
        AppLanguage.JAPANESE -> "出力ディレクトリが見つかりました: %s"
        AppLanguage.KOREAN -> "출력 디렉토리를 찾았습니다: %s"
    }
    val importLogFileCount: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "共 %d 个文件"
        AppLanguage.ENGLISH -> "%d file(s) total"
        AppLanguage.ARABIC -> "إجمالي %d ملف"
        AppLanguage.PORTUGUESE -> "%d arquivo(s) no total"
        AppLanguage.SPANISH -> "%d archivo(s) en total"
        AppLanguage.FRENCH -> "%d fichier(s) au total"
        AppLanguage.GERMAN -> "%d Datei(en) gesamt"
        AppLanguage.RUSSIAN -> "Всего %d файл(ов)"
        AppLanguage.JAPANESE -> "合計 %d ファイル"
        AppLanguage.KOREAN -> "총 %d 파일"
    }
    val importLogPreparing: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "准备导入..."
        AppLanguage.ENGLISH -> "Preparing import..."
        AppLanguage.ARABIC -> "تجهيز الاستيراد..."
        AppLanguage.PORTUGUESE -> "Preparando importação..."
        AppLanguage.SPANISH -> "Preparando importación..."
        AppLanguage.FRENCH -> "Préparation de l'import..."
        AppLanguage.GERMAN -> "Import wird vorbereitet..."
        AppLanguage.RUSSIAN -> "Подготовка импорта..."
        AppLanguage.JAPANESE -> "インポートを準備中..."
        AppLanguage.KOREAN -> "가져오기 준비 중..."
    }
    val importLogScanComplete: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "扫描完成，准备导入"
        AppLanguage.ENGLISH -> "Scan complete, ready to import"
        AppLanguage.ARABIC -> "اكتمل الفحص، جاهز للاستيراد"
        AppLanguage.PORTUGUESE -> "Scan concluído, pronto para importar"
        AppLanguage.SPANISH -> "Escaneo completado, listo para importar"
        AppLanguage.FRENCH -> "Scan terminé, prêt à importer"
        AppLanguage.GERMAN -> "Scan abgeschlossen, bereit zum Importieren"
        AppLanguage.RUSSIAN -> "Сканирование завершено, готово к импорту"
        AppLanguage.JAPANESE -> "スキャン完了、インポート準備完了"
        AppLanguage.KOREAN -> "스캔 완료, 가져올 준비됨"
    }
    val importLogFailedWithMsg: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "导入失败: %s"
        AppLanguage.ENGLISH -> "Import failed: %s"
        AppLanguage.ARABIC -> "فشل الاستيراد: %s"
        AppLanguage.PORTUGUESE -> "Importação falhou: %s"
        AppLanguage.SPANISH -> "Importación fallida: %s"
        AppLanguage.FRENCH -> "Import échoué : %s"
        AppLanguage.GERMAN -> "Import fehlgeschlagen: %s"
        AppLanguage.RUSSIAN -> "Импорт не удался: %s"
        AppLanguage.JAPANESE -> "インポート失敗: %s"
        AppLanguage.KOREAN -> "가져오기 실패: %s"
    }

    val pyDownloadSourceLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "%s [源%d/%d]"
        AppLanguage.ENGLISH -> "%s [Source %d/%d]"
        AppLanguage.ARABIC -> "%s [المصدر %d/%d]"
        AppLanguage.PORTUGUESE -> "%s [Fonte %d/%d]"
        AppLanguage.SPANISH -> "%s [Fuente %d/%d]"
        AppLanguage.FRENCH -> "%s [source %d/%d]"
        AppLanguage.GERMAN -> "%s [Quelle %d/%d]"
        AppLanguage.RUSSIAN -> "%s [Источник %d/%d]"
        AppLanguage.JAPANESE -> "%s [ソース %d/%d]"
        AppLanguage.KOREAN -> "%s [소스 %d/%d]"
    }
    val njsHeroTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Node.js 应用"
        AppLanguage.ENGLISH -> "Node.js Application"
        AppLanguage.ARABIC -> "تطبيق Node.js"
        AppLanguage.PORTUGUESE -> "Aplicação Node.js"
        AppLanguage.SPANISH -> "Aplicación Node.js"
        AppLanguage.FRENCH -> "Application Node.js"
        AppLanguage.GERMAN -> "Node.js-Anwendung"
        AppLanguage.RUSSIAN -> "Приложение Node.js"
        AppLanguage.JAPANESE -> "Node.js アプリケーション"
        AppLanguage.KOREAN -> "Node.js 애플리케이션"
    }
    val njsHeroDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "将 Node.js 项目打包为独立 Android 应用"
        AppLanguage.ENGLISH -> "Package Node.js project as standalone Android app"
        AppLanguage.ARABIC -> "تغليف مشروع Node.js كتطبيق Android مستقل"
        AppLanguage.PORTUGUESE -> "Embalar projeto Node.js como app Android autônoma"
        AppLanguage.SPANISH -> "Empaquetar proyecto Node.js como app Android independiente"
        AppLanguage.FRENCH -> "Emballer un projet Node.js comme application Android autonome"
        AppLanguage.GERMAN -> "Node.js-Projekt als eigenständige Android-App verpacken"
        AppLanguage.RUSSIAN -> "Упаковать проект Node.js как самостоятельное приложение Android"
        AppLanguage.JAPANESE -> "Node.js プロジェクトを独立した Android アプリとしてパッケージ化"
        AppLanguage.KOREAN -> "Node.js 프로젝트를 독립 Android 앱으로 패키징"
    }
    val njsScripts: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "NPM 脚本"
        AppLanguage.ENGLISH -> "NPM Scripts"
        AppLanguage.ARABIC -> "نصوص NPM"
        AppLanguage.PORTUGUESE -> "Scripts NPM"
        AppLanguage.SPANISH -> "Scripts NPM"
        AppLanguage.FRENCH -> "Scripts NPM"
        AppLanguage.GERMAN -> "NPM-Skripte"
        AppLanguage.RUSSIAN -> "Скрипты NPM"
        AppLanguage.JAPANESE -> "NPM スクリプト"
        AppLanguage.KOREAN -> "NPM 스크립트"
    }
    val njsStartupScript: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "启动脚本"
        AppLanguage.ENGLISH -> "Startup Script"
        AppLanguage.ARABIC -> "نص بدء التشغيل"
        AppLanguage.PORTUGUESE -> "Script de Inicialização"
        AppLanguage.SPANISH -> "Script de Inicio"
        AppLanguage.FRENCH -> "Script de Démarrage"
        AppLanguage.GERMAN -> "Start-Skript"
        AppLanguage.RUSSIAN -> "Скрипт запуска"
        AppLanguage.JAPANESE -> "スタートアップスクリプト"
        AppLanguage.KOREAN -> "시작 스크립트"
    }
    val njsPackageManager: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "包管理器"
        AppLanguage.ENGLISH -> "Package Manager"
        AppLanguage.ARABIC -> "مدير الحزم"
        AppLanguage.PORTUGUESE -> "Gerenciador de Pacotes"
        AppLanguage.SPANISH -> "Gestor de Paquetes"
        AppLanguage.FRENCH -> "Gestionnaire de Paquets"
        AppLanguage.GERMAN -> "Paketmanager"
        AppLanguage.RUSSIAN -> "Менеджер пакетов"
        AppLanguage.JAPANESE -> "パッケージマネージャー"
        AppLanguage.KOREAN -> "패키지 관리자"
    }
    val njsDetectedPort: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "检测到端口"
        AppLanguage.ENGLISH -> "Detected Port"
        AppLanguage.ARABIC -> "المنفذ المكتشف"
        AppLanguage.PORTUGUESE -> "Porta Detectada"
        AppLanguage.SPANISH -> "Puerto Detectado"
        AppLanguage.FRENCH -> "Port Détecté"
        AppLanguage.GERMAN -> "Erkannter Port"
        AppLanguage.RUSSIAN -> "Обнаруженный порт"
        AppLanguage.JAPANESE -> "検出されたポート"
        AppLanguage.KOREAN -> "감지된 포트"
    }
    val njsPortOverride: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自定义端口"
        AppLanguage.ENGLISH -> "Custom Port"
        AppLanguage.ARABIC -> "منفذ مخصص"
        AppLanguage.PORTUGUESE -> "Porta Personalizada"
        AppLanguage.SPANISH -> "Puerto Personalizado"
        AppLanguage.FRENCH -> "Port Personnalisé"
        AppLanguage.GERMAN -> "Benutzerdefinierter Port"
        AppLanguage.RUSSIAN -> "Пользовательский порт"
        AppLanguage.JAPANESE -> "カスタムポート"
        AppLanguage.KOREAN -> "사용자 정의 포트"
    }
    val njsProjectInfo: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "项目信息"
        AppLanguage.ENGLISH -> "Project Info"
        AppLanguage.ARABIC -> "معلومات المشروع"
        AppLanguage.PORTUGUESE -> "Informações do Projeto"
        AppLanguage.SPANISH -> "Información del Proyecto"
        AppLanguage.FRENCH -> "Infos du Projet"
        AppLanguage.GERMAN -> "Projektinfo"
        AppLanguage.RUSSIAN -> "Информация о проекте"
        AppLanguage.JAPANESE -> "プロジェクト情報"
        AppLanguage.KOREAN -> "프로젝트 정보"
    }

    val mediaImageInfo: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "图片信息"
        AppLanguage.ENGLISH -> "Image Info"
        AppLanguage.ARABIC -> "معلومات الصورة"
        AppLanguage.PORTUGUESE -> "Informações da Imagem"
        AppLanguage.SPANISH -> "Información de la Imagen"
        AppLanguage.FRENCH -> "Infos de l'Image"
        AppLanguage.GERMAN -> "Bildinfo"
        AppLanguage.RUSSIAN -> "Информация об изображении"
        AppLanguage.JAPANESE -> "画像情報"
        AppLanguage.KOREAN -> "이미지 정보"
    }
    val mediaVideoInfo: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "视频信息"
        AppLanguage.ENGLISH -> "Video Info"
        AppLanguage.ARABIC -> "معلومات الفيديو"
        AppLanguage.PORTUGUESE -> "Informações do Vídeo"
        AppLanguage.SPANISH -> "Información del Vídeo"
        AppLanguage.FRENCH -> "Infos de la Vidéo"
        AppLanguage.GERMAN -> "Video-Info"
        AppLanguage.RUSSIAN -> "Информация о видео"
        AppLanguage.JAPANESE -> "動画情報"
        AppLanguage.KOREAN -> "동영상 정보"
    }
    val mediaFileSize: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "文件大小"
        AppLanguage.ENGLISH -> "File Size"
        AppLanguage.ARABIC -> "حجم الملف"
        AppLanguage.PORTUGUESE -> "Tamanho do Arquivo"
        AppLanguage.SPANISH -> "Tamaño del Archivo"
        AppLanguage.FRENCH -> "Taille du Fichier"
        AppLanguage.GERMAN -> "Dateigröße"
        AppLanguage.RUSSIAN -> "Размер файла"
        AppLanguage.JAPANESE -> "ファイルサイズ"
        AppLanguage.KOREAN -> "파일 크기"
    }
    val mediaFormat: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "格式"
        AppLanguage.ENGLISH -> "Format"
        AppLanguage.ARABIC -> "التنسيق"
        AppLanguage.PORTUGUESE -> "Formato"
        AppLanguage.SPANISH -> "Formato"
        AppLanguage.FRENCH -> "Format"
        AppLanguage.GERMAN -> "Format"
        AppLanguage.RUSSIAN -> "Формат"
        AppLanguage.JAPANESE -> "形式"
        AppLanguage.KOREAN -> "형식"
    }
    val mediaPlaybackSpeed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "播放速度"
        AppLanguage.ENGLISH -> "Playback Speed"
        AppLanguage.ARABIC -> "سرعة التشغيل"
        AppLanguage.PORTUGUESE -> "Velocidade de Reprodução"
        AppLanguage.SPANISH -> "Velocidad de Reproducción"
        AppLanguage.FRENCH -> "Vitesse de Lecture"
        AppLanguage.GERMAN -> "Wiedergabegeschwindigkeit"
        AppLanguage.RUSSIAN -> "Скорость воспроизведения"
        AppLanguage.JAPANESE -> "再生速度"
        AppLanguage.KOREAN -> "재생 속도"
    }
    val mediaBackgroundColor: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "背景颜色"
        AppLanguage.ENGLISH -> "Background Color"
        AppLanguage.ARABIC -> "لون الخلفية"
        AppLanguage.PORTUGUESE -> "Cor de Fundo"
        AppLanguage.SPANISH -> "Color de Fondo"
        AppLanguage.FRENCH -> "Couleur de Fond"
        AppLanguage.GERMAN -> "Hintergrundfarbe"
        AppLanguage.RUSSIAN -> "Цвет фона"
        AppLanguage.JAPANESE -> "背景色"
        AppLanguage.KOREAN -> "배경색"
    }
    val mediaScreenLock: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "屏幕常亮"
        AppLanguage.ENGLISH -> "Keep Screen On"
        AppLanguage.ARABIC -> "إبقاء الشاشة مضاءة"
        AppLanguage.PORTUGUESE -> "Manter Tela Ligada"
        AppLanguage.SPANISH -> "Mantener Pantalla Encendida"
        AppLanguage.FRENCH -> "Garder l'Écran Allumé"
        AppLanguage.GERMAN -> "Bildschirm an lassen"
        AppLanguage.RUSSIAN -> "Держать экран включенным"
        AppLanguage.JAPANESE -> "画面を常時オン"
        AppLanguage.KOREAN -> "화면 켜짐 유지"
    }
    val mediaScreenLockHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "显示媒体时防止屏幕自动关闭"
        AppLanguage.ENGLISH -> "Prevent screen from turning off during media display"
        AppLanguage.ARABIC -> "منع إيقاف الشاشة أثناء عرض الوسائط"
        AppLanguage.PORTUGUESE -> "Evitar desligamento da tela durante a exibição de mídia"
        AppLanguage.SPANISH -> "Evitar que la pantalla se apague durante la reproducción de medios"
        AppLanguage.FRENCH -> "Empêcher l'extinction de l'écran pendant l'affichage des médias"
        AppLanguage.GERMAN -> "Bildschirm während der Medienwiedergabe eingeschaltet lassen"
        AppLanguage.RUSSIAN -> "Предотвращать выключение экрана во время показа медиа"
        AppLanguage.JAPANESE -> "メディア表示中の画面自動オフを防止"
        AppLanguage.KOREAN -> "미디어 표시 중 화면 자동 꺼짐 방지"
    }
    val mediaGestureConfig: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "手势设置"
        AppLanguage.ENGLISH -> "Gesture Settings"
        AppLanguage.ARABIC -> "إعدادات الإيماءات"
        AppLanguage.PORTUGUESE -> "Configurações de Gesto"
        AppLanguage.SPANISH -> "Ajustes de Gestos"
        AppLanguage.FRENCH -> "Paramètres de Geste"
        AppLanguage.GERMAN -> "Gesten-Einstellungen"
        AppLanguage.RUSSIAN -> "Настройки жестов"
        AppLanguage.JAPANESE -> "ジェスチャー設定"
        AppLanguage.KOREAN -> "제스처 설정"
    }
    val mediaSwipeDismiss: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "滑动退出"
        AppLanguage.ENGLISH -> "Swipe to Dismiss"
        AppLanguage.ARABIC -> "اسحب للإغلاق"
        AppLanguage.PORTUGUESE -> "Deslizar para Fechar"
        AppLanguage.SPANISH -> "Deslizar para Cerrar"
        AppLanguage.FRENCH -> "Glisser pour Fermer"
        AppLanguage.GERMAN -> "Wischen zum Schließen"
        AppLanguage.RUSSIAN -> "Смахнуть для закрытия"
        AppLanguage.JAPANESE -> "スワイプで閉じる"
        AppLanguage.KOREAN -> "스와이프하여 닫기"
    }
    val mediaSwipeDismissHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "上下滑动关闭媒体"
        AppLanguage.ENGLISH -> "Swipe up/down to close media"
        AppLanguage.ARABIC -> "اسحب لأعلى/لأسفل لإغلاق الوسائط"
        AppLanguage.PORTUGUESE -> "Deslizar para cima/baixo para fechar a mídia"
        AppLanguage.SPANISH -> "Desliza hacia arriba/abajo para cerrar los medios"
        AppLanguage.FRENCH -> "Glisser vers le haut/bas pour fermer les médias"
        AppLanguage.GERMAN -> "Nach oben/unten wischen, um Medien zu schließen"
        AppLanguage.RUSSIAN -> "Смахните вверх/вниз, чтобы закрыть медиа"
        AppLanguage.JAPANESE -> "上/下にスワイプしてメディアを閉じる"
        AppLanguage.KOREAN -> "위/아래로 스와이프하여 미디어 닫기"
    }
    val mediaDoubleTapZoom: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "双击缩放"
        AppLanguage.ENGLISH -> "Double-tap to Zoom"
        AppLanguage.ARABIC -> "انقر مرتين للتكبير"
        AppLanguage.PORTUGUESE -> "Toque duplo para Zoom"
        AppLanguage.SPANISH -> "Doble toque para Zoom"
        AppLanguage.FRENCH -> "Double-tap pour Zoomer"
        AppLanguage.GERMAN -> "Doppeltippen zum Zoomen"
        AppLanguage.RUSSIAN -> "Двойное нажатие для масштабирования"
        AppLanguage.JAPANESE -> "ダブルタップでズーム"
        AppLanguage.KOREAN -> "더블탭으로 확대"
    }
    val mediaDoubleTapZoomHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "双击放大/缩小图片"
        AppLanguage.ENGLISH -> "Double-tap to zoom in/out"
        AppLanguage.ARABIC -> "انقر مرتين للتكبير/التصغير"
        AppLanguage.PORTUGUESE -> "Toque duplo para ampliar/reduzir"
        AppLanguage.SPANISH -> "Doble toque para acercar/alejar"
        AppLanguage.FRENCH -> "Double-tap pour zoomer/dézoomer"
        AppLanguage.GERMAN -> "Doppeltippen zum Vergrößern/Verkleinern"
        AppLanguage.RUSSIAN -> "Двойное нажатие для увеличения/уменьшения"
        AppLanguage.JAPANESE -> "ダブルタップで拡大/縮小"
        AppLanguage.KOREAN -> "더블탭으로 확대/축소"
    }
    val mediaBrightness: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "亮度"
        AppLanguage.ENGLISH -> "Brightness"
        AppLanguage.ARABIC -> "السطوع"
        AppLanguage.PORTUGUESE -> "Brilho"
        AppLanguage.SPANISH -> "Brillo"
        AppLanguage.FRENCH -> "Luminosité"
        AppLanguage.GERMAN -> "Helligkeit"
        AppLanguage.RUSSIAN -> "Яркость"
        AppLanguage.JAPANESE -> "明るさ"
        AppLanguage.KOREAN -> "밝기"
    }
    val mediaContrast: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "对比度"
        AppLanguage.ENGLISH -> "Contrast"
        AppLanguage.ARABIC -> "التباين"
        AppLanguage.PORTUGUESE -> "Contraste"
        AppLanguage.SPANISH -> "Contraste"
        AppLanguage.FRENCH -> "Contraste"
        AppLanguage.GERMAN -> "Kontrast"
        AppLanguage.RUSSIAN -> "Контрастность"
        AppLanguage.JAPANESE -> "コントラスト"
        AppLanguage.KOREAN -> "대비"
    }
    val mediaSaturation: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "饱和度"
        AppLanguage.ENGLISH -> "Saturation"
        AppLanguage.ARABIC -> "التشبع"
        AppLanguage.PORTUGUESE -> "Saturação"
        AppLanguage.SPANISH -> "Saturación"
        AppLanguage.FRENCH -> "Saturation"
        AppLanguage.GERMAN -> "Sättigung"
        AppLanguage.RUSSIAN -> "Насыщенность"
        AppLanguage.JAPANESE -> "彩度"
        AppLanguage.KOREAN -> "채도"
    }
    val mediaImageAdjust: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "图片调整"
        AppLanguage.ENGLISH -> "Image Adjustments"
        AppLanguage.ARABIC -> "تعديلات الصورة"
        AppLanguage.PORTUGUESE -> "Ajustes de Imagem"
        AppLanguage.SPANISH -> "Ajustes de Imagen"
        AppLanguage.FRENCH -> "Ajustements d'Image"
        AppLanguage.GERMAN -> "Bildanpassungen"
        AppLanguage.RUSSIAN -> "Коррекция изображения"
        AppLanguage.JAPANESE -> "画像調整"
        AppLanguage.KOREAN -> "이미지 조정"
    }
    val mediaImageAdjustHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "调整图片显示效果"
        AppLanguage.ENGLISH -> "Adjust image display effects"
        AppLanguage.ARABIC -> "ضبط تأثيرات عرض الصورة"
        AppLanguage.PORTUGUESE -> "Ajustar efeitos de exibição da imagem"
        AppLanguage.SPANISH -> "Ajustar efectos de visualización de la imagen"
        AppLanguage.FRENCH -> "Ajuster les effets d'affichage de l'image"
        AppLanguage.GERMAN -> "Bildanzeigeeffekte anpassen"
        AppLanguage.RUSSIAN -> "Настроить эффекты отображения изображения"
        AppLanguage.JAPANESE -> "画像表示効果を調整"
        AppLanguage.KOREAN -> "이미지 표시 효과 조정"
    }
    val mediaReset: String get() = when (Strings.lang) {
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

    val importUserScript: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "导入油猴脚本"
        AppLanguage.ENGLISH -> "Import Userscript"
        AppLanguage.ARABIC -> "استيراد سكريبت المستخدم"
        AppLanguage.PORTUGUESE -> "Importar Userscript"
        AppLanguage.SPANISH -> "Importar Userscript"
        AppLanguage.FRENCH -> "Importer un Userscript"
        AppLanguage.GERMAN -> "Userscript importieren"
        AppLanguage.RUSSIAN -> "Импортировать пользовательский скрипт"
        AppLanguage.JAPANESE -> "ユーザースクリプトをインポート"
        AppLanguage.KOREAN -> "유저스크립트 가져오기"
    }
    val importUserScriptHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "支持 .user.js 及普通 .js 文件"
        AppLanguage.ENGLISH -> "Supports .user.js and plain .js files"
        AppLanguage.ARABIC -> "يدعم ملفات .user.js و .js العادية"
        AppLanguage.PORTUGUESE -> "Suporta arquivos .user.js e .js comuns"
        AppLanguage.SPANISH -> "Soporta archivos .user.js y .js normales"
        AppLanguage.FRENCH -> "Prend en charge les fichiers .user.js et .js simples"
        AppLanguage.GERMAN -> "Unterstützt .user.js- und einfache .js-Dateien"
        AppLanguage.RUSSIAN -> "Поддерживает файлы .user.js и обычные .js"
        AppLanguage.JAPANESE -> ".user.js および通常の .js ファイルに対応"
        AppLanguage.KOREAN -> ".user.js 및 일반 .js 파일 지원"
    }
    val importChromeExtension: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "导入 Chrome 扩展"
        AppLanguage.ENGLISH -> "Import Chrome Extension"
        AppLanguage.ARABIC -> "استيراد إضافة Chrome"
        AppLanguage.PORTUGUESE -> "Importar Extensão do Chrome"
        AppLanguage.SPANISH -> "Importar Extensión de Chrome"
        AppLanguage.FRENCH -> "Importer une Extension Chrome"
        AppLanguage.GERMAN -> "Chrome-Erweiterung importieren"
        AppLanguage.RUSSIAN -> "Импортировать расширение Chrome"
        AppLanguage.JAPANESE -> "Chrome 拡張機能をインポート"
        AppLanguage.KOREAN -> "Chrome 확장 프로그램 가져오기"
    }
}

