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

    val scriptViewCode: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "查看代码"
        AppLanguage.ENGLISH -> "View Code"
        AppLanguage.ARABIC -> "عرض الكود"
        AppLanguage.PORTUGUESE -> "Ver Código"
        AppLanguage.SPANISH -> "Ver Código"
        AppLanguage.FRENCH -> "Voir le Code"
        AppLanguage.GERMAN -> "Code ansehen"
        AppLanguage.RUSSIAN -> "Просмотреть код"
        AppLanguage.JAPANESE -> "コードを表示"
        AppLanguage.KOREAN -> "코드 보기"
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

    val longPressHapticOnActionOnly: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "空长按时不震动"
        AppLanguage.ENGLISH -> "No haptic on an empty long-press"
        AppLanguage.ARABIC -> "بدون اهتزاز للضغط المطول الفارغ"
        AppLanguage.PORTUGUESE -> "Sem vibração num toque longo vazio"
        AppLanguage.SPANISH -> "Sin vibración en una pulsación larga vacía"
        AppLanguage.FRENCH -> "Pas de vibration pour un appui long vide"
        AppLanguage.GERMAN -> "Keine Vibration bei leerem langem Druck"
        AppLanguage.RUSSIAN -> "Без вибрации при пустом долгом нажатии"
        AppLanguage.JAPANESE -> "何も起きない長押しは振動しない"
        AppLanguage.KOREAN -> "빈 길게 누르기는 진동 없음"
    }

    val longPressHapticOnActionOnlyDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "菜单弹出，或网页自己处理时，仍然震动。"
        AppLanguage.ENGLISH -> "A menu, or a press the page handles, still vibrates."
        AppLanguage.ARABIC -> "القائمة، أو الضغط الذي تتعامل معه الصفحة، يهتز."
        AppLanguage.PORTUGUESE -> "Um menu, ou um toque que a página trata, vibra."
        AppLanguage.SPANISH -> "Un menú, o una pulsación que la página gestiona, vibra."
        AppLanguage.FRENCH -> "Un menu, ou un appui géré par la page, vibre."
        AppLanguage.GERMAN -> "Ein Menü oder ein Druck, den die Seite verarbeitet, vibriert."
        AppLanguage.RUSSIAN -> "Меню или нажатие, обработанное страницей, вибрирует."
        AppLanguage.JAPANESE -> "メニューが出るとき、ページが処理するときは振動します。"
        AppLanguage.KOREAN -> "메뉴가 열리거나 페이지가 처리하면 진동합니다."
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

    val openInBrowser: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "在浏览器打开"
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

    val loadFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "加载失败"
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

    val webAppSeparateTasks: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "WebApp 独立任务"
        AppLanguage.ENGLISH -> "Separate WebApp tasks"
        AppLanguage.ARABIC -> "مهام WebApp منفصلة"
        AppLanguage.PORTUGUESE -> "Tarefas WebApp separadas"
        AppLanguage.SPANISH -> "Tareas WebApp separadas"
        AppLanguage.FRENCH -> "Tâches WebApp séparées"
        AppLanguage.GERMAN -> "Getrennte WebApp-Aufgaben"
        AppLanguage.RUSSIAN -> "Отдельные задачи WebApp"
        AppLanguage.JAPANESE -> "WebAppを個別タスクで開く"
        AppLanguage.KOREAN -> "WebApp 개별 작업"
    }

    val webAppSeparateTasksDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "开启后，首页预览会为每个 WebApp 单独占用最近任务，可同时打开多个；更占内存。默认关闭。桌面快捷方式始终单独占用一个任务，返回时回到桌面。"
        AppLanguage.ENGLISH -> "When on, home preview opens each WebApp in its own recents task so several can run at once. Uses more memory. Off by default. Desktop shortcuts always open in their own task, so Back returns to the launcher."
        AppLanguage.ARABIC -> "عند التفعيل، تفتح معاينة الصفحة الرئيسية كل WebApp في مهمة منفصلة. يستهلك ذاكرة أكثر. متوقف افتراضياً. اختصارات الشاشة الرئيسية تفتح دائماً في مهمة مستقلة، والرجوع يعود إلى المشغّل."
        AppLanguage.PORTUGUESE -> "Quando ativo, a prévia inicial abre cada WebApp em uma tarefa própria. Usa mais memória. Desligado por padrão. Atalhos da área de trabalho sempre abrem em uma tarefa própria, e Voltar retorna ao launcher."
        AppLanguage.SPANISH -> "Si está activo, la vista previa abre cada WebApp en una tarea propia. Usa más memoria. Desactivado por defecto. Los accesos directos siempre abren en una tarea propia, y Atrás vuelve al lanzador."
        AppLanguage.FRENCH -> "Une fois activé, l'aperçu d'accueil ouvre chaque WebApp dans une tâche distincte. Plus de mémoire. Désactivé par défaut. Les raccourcis ouvrent toujours dans une tâche propre, et Retour ramène au lanceur."
        AppLanguage.GERMAN -> "Wenn aktiv, öffnet die Startvorschau jedes WebApp in einer eigenen Aufgabe. Mehr Speicher. Standard: aus. Desktop-Verknüpfungen öffnen immer in einer eigenen Aufgabe, Zurück kehrt zum Launcher zurück."
        AppLanguage.RUSSIAN -> "При включении превью с главного экрана открывает каждый WebApp в отдельной задаче. Больше памяти. По умолчанию выкл. Ярлыки всегда открываются в отдельной задаче, «Назад» возвращает на рабочий стол."
        AppLanguage.JAPANESE -> "オンにすると、ホームのプレビューが各WebAppを別タスクで開きます。メモリ使用量が増えます。初期値はオフ。デスクトップのショートカットは常に別タスクで開き、戻るとランチャーに戻ります。"
        AppLanguage.KOREAN -> "켜면 홈 미리보기가 각 WebApp을 별도 최근 작업으로 엽니다. 메모리를 더 씁니다. 기본값은 끔. 바탕화면 바로가기는 항상 별도 작업으로 열리며, 뒤로 가면 런처로 돌아갑니다."
    }

    val localMcp: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "本地 MCP"
        AppLanguage.ENGLISH -> "Local MCP"
        AppLanguage.ARABIC -> "MCP المحلي"
        AppLanguage.PORTUGUESE -> "MCP local"
        AppLanguage.SPANISH -> "MCP local"
        AppLanguage.FRENCH -> "MCP local"
        AppLanguage.GERMAN -> "Lokales MCP"
        AppLanguage.RUSSIAN -> "Локальный MCP"
        AppLanguage.JAPANESE -> "ローカル MCP"
        AppLanguage.KOREAN -> "로컬 MCP"
    }

    val localMcpDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "开启后在 127.0.0.1 提供与应用内 Agent 相同的工具，供外部 Agent 连接。默认关闭。写操作会在手机上确认。"
        AppLanguage.ENGLISH -> "When on, serves the same tools as the in-app agent on 127.0.0.1 for an external agent. Off by default. Writes ask for confirmation on the phone."
        AppLanguage.ARABIC -> "عند التفعيل يقدّم نفس أدوات الوكيل الداخلي على 127.0.0.1. متوقف افتراضياً. عمليات الكتابة تطلب تأكيداً على الهاتف."
        AppLanguage.PORTUGUESE -> "Quando ativo, oferece as mesmas ferramentas do agente interno em 127.0.0.1. Desligado por padrão. Escrita pede confirmação no telefone."
        AppLanguage.SPANISH -> "Si está activo, ofrece las mismas herramientas del agente interno en 127.0.0.1. Desactivado por defecto. Escribir pide confirmación en el teléfono."
        AppLanguage.FRENCH -> "Une fois activé, sert les mêmes outils que l'agent interne sur 127.0.0.1. Désactivé par défaut. Les écritures demandent une confirmation sur le téléphone."
        AppLanguage.GERMAN -> "Wenn aktiv, stellt dieselben Werkzeuge wie der interne Agent auf 127.0.0.1 bereit. Standard: aus. Schreiben fragt auf dem Telefon nach."
        AppLanguage.RUSSIAN -> "Когда включено, отдаёт те же инструменты, что и встроенный агент, на 127.0.0.1. По умолчанию выкл. Запись спрашивает подтверждение на телефоне."
        AppLanguage.JAPANESE -> "オンにすると、アプリ内エージェントと同じツールを 127.0.0.1 で提供します。初期値はオフ。書き込みはスマホで確認します。"
        AppLanguage.KOREAN -> "켜면 앱 안 에이전트와 같은 도구를 127.0.0.1에서 제공합니다. 기본값은 끔. 쓰기 작업은 휴대폰에서 확인합니다."
    }

    val localMcpCopyConfig: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "复制配置"
        AppLanguage.ENGLISH -> "Copy config"
        AppLanguage.ARABIC -> "نسخ الإعداد"
        AppLanguage.PORTUGUESE -> "Copiar config"
        AppLanguage.SPANISH -> "Copiar config"
        AppLanguage.FRENCH -> "Copier la config"
        AppLanguage.GERMAN -> "Konfig kopieren"
        AppLanguage.RUSSIAN -> "Копировать конфиг"
        AppLanguage.JAPANESE -> "設定をコピー"
        AppLanguage.KOREAN -> "설정 복사"
    }

    val localMcpCopied: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已复制 MCP 配置"
        AppLanguage.ENGLISH -> "MCP config copied"
        AppLanguage.ARABIC -> "تم نسخ إعداد MCP"
        AppLanguage.PORTUGUESE -> "Config MCP copiada"
        AppLanguage.SPANISH -> "Config MCP copiada"
        AppLanguage.FRENCH -> "Config MCP copiée"
        AppLanguage.GERMAN -> "MCP-Konfig kopiert"
        AppLanguage.RUSSIAN -> "Конфиг MCP скопирован"
        AppLanguage.JAPANESE -> "MCP設定をコピーしました"
        AppLanguage.KOREAN -> "MCP 설정을 복사했습니다"
    }

    val localMcpAddressCopied: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已复制地址"
        AppLanguage.ENGLISH -> "Address copied"
        AppLanguage.ARABIC -> "تم نسخ العنوان"
        AppLanguage.PORTUGUESE -> "Endereço copiado"
        AppLanguage.SPANISH -> "Dirección copiada"
        AppLanguage.FRENCH -> "Adresse copiée"
        AppLanguage.GERMAN -> "Adresse kopiert"
        AppLanguage.RUSSIAN -> "Адрес скопирован"
        AppLanguage.JAPANESE -> "アドレスをコピーしました"
        AppLanguage.KOREAN -> "주소를 복사했습니다"
    }

    val localMcpTokenCopied: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已复制令牌"
        AppLanguage.ENGLISH -> "Token copied"
        AppLanguage.ARABIC -> "تم نسخ الرمز"
        AppLanguage.PORTUGUESE -> "Token copiado"
        AppLanguage.SPANISH -> "Token copiado"
        AppLanguage.FRENCH -> "Jeton copié"
        AppLanguage.GERMAN -> "Token kopiert"
        AppLanguage.RUSSIAN -> "Токен скопирован"
        AppLanguage.JAPANESE -> "トークンをコピーしました"
        AppLanguage.KOREAN -> "토큰을 복사했습니다"
    }

    val localMcpRotateToken: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "更换令牌"
        AppLanguage.ENGLISH -> "Rotate token"
        AppLanguage.ARABIC -> "تدوير الرمز"
        AppLanguage.PORTUGUESE -> "Trocar token"
        AppLanguage.SPANISH -> "Rotar token"
        AppLanguage.FRENCH -> "Renouveler le jeton"
        AppLanguage.GERMAN -> "Token erneuern"
        AppLanguage.RUSSIAN -> "Сменить токен"
        AppLanguage.JAPANESE -> "トークンを更新"
        AppLanguage.KOREAN -> "토큰 교체"
    }

    fun localMcpAdb(port: Int): String = when (Strings.lang) {
        AppLanguage.CHINESE -> "电脑上的 Agent 先执行 adb reverse tcp:$port tcp:$port，再用上面的地址。"
        AppLanguage.ENGLISH -> "From a computer, run adb reverse tcp:$port tcp:$port, then use the URL above."
        AppLanguage.ARABIC -> "من الحاسوب نفّذ adb reverse tcp:$port tcp:$port ثم استخدم العنوان أعلاه."
        AppLanguage.PORTUGUESE -> "No computador, execute adb reverse tcp:$port tcp:$port e use o endereço acima."
        AppLanguage.SPANISH -> "En el ordenador, ejecuta adb reverse tcp:$port tcp:$port y usa la URL de arriba."
        AppLanguage.FRENCH -> "Depuis un ordinateur, lancez adb reverse tcp:$port tcp:$port puis utilisez l'adresse ci-dessus."
        AppLanguage.GERMAN -> "Am Computer adb reverse tcp:$port tcp:$port ausführen, dann die Adresse oben nutzen."
        AppLanguage.RUSSIAN -> "На компьютере выполните adb reverse tcp:$port tcp:$port и используйте адрес выше."
        AppLanguage.JAPANESE -> "パソコンでは adb reverse tcp:$port tcp:$port を実行してから、上の URL を使います。"
        AppLanguage.KOREAN -> "컴퓨터에서는 adb reverse tcp:$port tcp:$port 를 실행한 뒤 위 주소를 사용하세요."
    }

    fun localMcpBindFailed(detail: String): String = when (Strings.lang) {
        AppLanguage.CHINESE -> "本地 MCP 没有打开：$detail"
        AppLanguage.ENGLISH -> "Local MCP did not start: $detail"
        AppLanguage.ARABIC -> "لم يبدأ MCP المحلي: $detail"
        AppLanguage.PORTUGUESE -> "O MCP local não iniciou: $detail"
        AppLanguage.SPANISH -> "El MCP local no arrancó: $detail"
        AppLanguage.FRENCH -> "Le MCP local n'a pas démarré : $detail"
        AppLanguage.GERMAN -> "Lokales MCP ist nicht gestartet: $detail"
        AppLanguage.RUSSIAN -> "Локальный MCP не запустился: $detail"
        AppLanguage.JAPANESE -> "ローカル MCP を開始できませんでした: $detail"
        AppLanguage.KOREAN -> "로컬 MCP를 시작하지 못했습니다: $detail"
    }

    val advancedFeatures: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "高级功能"
        AppLanguage.ENGLISH -> "Advanced features"
        AppLanguage.ARABIC -> "ميزات متقدمة"
        AppLanguage.PORTUGUESE -> "Recursos avançados"
        AppLanguage.SPANISH -> "Funciones avanzadas"
        AppLanguage.FRENCH -> "Fonctions avancées"
        AppLanguage.GERMAN -> "Erweiterte Funktionen"
        AppLanguage.RUSSIAN -> "Расширенные функции"
        AppLanguage.JAPANESE -> "高度な機能"
        AppLanguage.KOREAN -> "고급 기능"
    }

    val advancedFeaturesDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "开启后可以创建、预览并导出 Node.js、PHP、Python、Go、WordPress、图片和视频。默认关闭。代码一直在应用里，开关只控制是否使用。"
        AppLanguage.ENGLISH -> "When on, you can create, preview, and export Node.js, PHP, Python, Go, WordPress, image, and video apps. Off by default. The code stays in the app; the switch only controls use."
        AppLanguage.ARABIC -> "عند التفعيل يمكنك إنشاء ومعاينة وتصدير تطبيقات Node.js وPHP وPython وGo وWordPress والصور والفيديو. متوقف افتراضياً. الكود موجود دائماً، والمفتاح يتحكم بالاستخدام فقط."
        AppLanguage.PORTUGUESE -> "Quando ativo, dá para criar, pré-visualizar e exportar apps Node.js, PHP, Python, Go, WordPress, imagem e vídeo. Desligado por padrão. O código fica no app; o interruptor só controla o uso."
        AppLanguage.SPANISH -> "Si está activo, puedes crear, previsualizar y exportar apps Node.js, PHP, Python, Go, WordPress, imagen y vídeo. Desactivado por defecto. El código sigue en la app; el interruptor solo controla el uso."
        AppLanguage.FRENCH -> "Une fois activé, vous pouvez créer, prévisualiser et exporter des apps Node.js, PHP, Python, Go, WordPress, image et vidéo. Désactivé par défaut. Le code reste dans l'app ; l'interrupteur ne fait que contrôler l'usage."
        AppLanguage.GERMAN -> "Wenn aktiv, lassen sich Node.js-, PHP-, Python-, Go-, WordPress-, Bild- und Video-Apps erstellen, ansehen und exportieren. Standard: aus. Der Code bleibt in der App; der Schalter steuert nur die Nutzung."
        AppLanguage.RUSSIAN -> "Когда включено, можно создавать, смотреть и экспортировать приложения Node.js, PHP, Python, Go, WordPress, изображения и видео. По умолчанию выкл. Код остаётся в приложении, переключатель только разрешает использование."
        AppLanguage.JAPANESE -> "オンにすると Node.js、PHP、Python、Go、WordPress、画像、動画を作成・プレビュー・書き出しできます。初期値はオフ。コードは常にアプリ内にあり、スイッチは使うかどうかだけを決めます。"
        AppLanguage.KOREAN -> "켜면 Node.js, PHP, Python, Go, WordPress, 이미지, 비디오 앱을 만들고 미리 보고 내보낼 수 있습니다. 기본값은 끔. 코드는 앱에 그대로 있고, 스위치는 사용 여부만 바꿉니다."
    }

    val advancedFeaturesOff: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "高级功能已关闭。到关于页打开后再预览或导出。"
        AppLanguage.ENGLISH -> "Advanced features are off. Turn them on from About before preview or export."
        AppLanguage.ARABIC -> "الميزات المتقدمة متوقفة. فعّلها من صفحة حول قبل المعاينة أو التصدير."
        AppLanguage.PORTUGUESE -> "Recursos avançados estão desligados. Ative em Sobre antes de pré-visualizar ou exportar."
        AppLanguage.SPANISH -> "Las funciones avanzadas están desactivadas. Actívalas en Acerca de antes de previsualizar o exportar."
        AppLanguage.FRENCH -> "Les fonctions avancées sont désactivées. Activez-les dans À propos avant l'aperçu ou l'export."
        AppLanguage.GERMAN -> "Erweiterte Funktionen sind aus. Schalte sie unter Über ein, bevor du eine Vorschau oder einen Export machst."
        AppLanguage.RUSSIAN -> "Расширенные функции выключены. Включите их на странице «О программе» перед просмотром или экспортом."
        AppLanguage.JAPANESE -> "高度な機能はオフです。プレビューや書き出しの前に「このアプリについて」でオンにしてください。"
        AppLanguage.KOREAN -> "고급 기능이 꺼져 있습니다. 미리보기나 내보내기 전에 정보 페이지에서 켜세요."
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

    val galleryEmbeddedMedia: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "内置媒体"
        AppLanguage.ENGLISH -> "Bundled media"
        AppLanguage.ARABIC -> "وسائط مضمّنة"
        AppLanguage.PORTUGUESE -> "Mídia incluída"
        AppLanguage.SPANISH -> "Contenido incluido"
        AppLanguage.FRENCH -> "Médias intégrés"
        AppLanguage.GERMAN -> "Mitgelieferte Medien"
        AppLanguage.RUSSIAN -> "Встроенные медиа"
        AppLanguage.JAPANESE -> "同梱メディア"
        AppLanguage.KOREAN -> "내장 미디어"
    }

    val galleryDeviceFolder: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "本机视频目录"
        AppLanguage.ENGLISH -> "Device video folder"
        AppLanguage.ARABIC -> "مجلد فيديوهات الجهاز"
        AppLanguage.PORTUGUESE -> "Pasta de vídeos do dispositivo"
        AppLanguage.SPANISH -> "Carpeta de vídeos del dispositivo"
        AppLanguage.FRENCH -> "Dossier vidéo de l’appareil"
        AppLanguage.GERMAN -> "Videoordner auf dem Gerät"
        AppLanguage.RUSSIAN -> "Папка видео на устройстве"
        AppLanguage.JAPANESE -> "端末の動画フォルダ"
        AppLanguage.KOREAN -> "기기 동영상 폴더"
    }

    val galleryDeviceFolderHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "安装后在应用里选择视频目录，直接播放其中的视频。"
        AppLanguage.ENGLISH -> "Choose a video folder in the installed app to play its files directly."
        AppLanguage.ARABIC -> "اختر مجلد فيديوهات في التطبيق المثبّت لتشغيل ملفاته مباشرةً."
        AppLanguage.PORTUGUESE -> "Escolha uma pasta de vídeos no app instalado para reproduzir os arquivos diretamente."
        AppLanguage.SPANISH -> "Elige una carpeta de vídeos en la app instalada para reproducir sus archivos directamente."
        AppLanguage.FRENCH -> "Choisissez un dossier vidéo dans l’application installée pour lire ses fichiers directement."
        AppLanguage.GERMAN -> "Wähle in der installierten App einen Videoordner, um dessen Dateien direkt abzuspielen."
        AppLanguage.RUSSIAN -> "Выберите папку видео в установленном приложении, чтобы воспроизводить файлы напрямую."
        AppLanguage.JAPANESE -> "インストール後のアプリで動画フォルダを選ぶと、その中の動画を直接再生できます。"
        AppLanguage.KOREAN -> "설치한 앱에서 동영상 폴더를 선택하면 파일을 바로 재생할 수 있습니다."
    }

    val galleryChooseFolder: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择文件夹"
        AppLanguage.ENGLISH -> "Choose folder"
        AppLanguage.ARABIC -> "اختر مجلدًا"
        AppLanguage.PORTUGUESE -> "Escolher pasta"
        AppLanguage.SPANISH -> "Elegir carpeta"
        AppLanguage.FRENCH -> "Choisir un dossier"
        AppLanguage.GERMAN -> "Ordner auswählen"
        AppLanguage.RUSSIAN -> "Выбрать папку"
        AppLanguage.JAPANESE -> "フォルダを選択"
        AppLanguage.KOREAN -> "폴더 선택"
    }

    val galleryNewFolder: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "新建文件夹"
        AppLanguage.ENGLISH -> "New folder"
        AppLanguage.ARABIC -> "مجلد جديد"
        AppLanguage.PORTUGUESE -> "Nova pasta"
        AppLanguage.SPANISH -> "Nueva carpeta"
        AppLanguage.FRENCH -> "Nouveau dossier"
        AppLanguage.GERMAN -> "Neuer Ordner"
        AppLanguage.RUSSIAN -> "Новая папка"
        AppLanguage.JAPANESE -> "新しいフォルダ"
        AppLanguage.KOREAN -> "새 폴더"
    }

    val galleryFolderName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "文件夹名称"
        AppLanguage.ENGLISH -> "Folder name"
        AppLanguage.ARABIC -> "اسم المجلد"
        AppLanguage.PORTUGUESE -> "Nome da pasta"
        AppLanguage.SPANISH -> "Nombre de la carpeta"
        AppLanguage.FRENCH -> "Nom du dossier"
        AppLanguage.GERMAN -> "Ordnername"
        AppLanguage.RUSSIAN -> "Название папки"
        AppLanguage.JAPANESE -> "フォルダ名"
        AppLanguage.KOREAN -> "폴더 이름"
    }

    val galleryMoveToFolder: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "移到文件夹"
        AppLanguage.ENGLISH -> "Move to folder"
        AppLanguage.ARABIC -> "نقل إلى مجلد"
        AppLanguage.PORTUGUESE -> "Mover para a pasta"
        AppLanguage.SPANISH -> "Mover a la carpeta"
        AppLanguage.FRENCH -> "Déplacer vers un dossier"
        AppLanguage.GERMAN -> "In Ordner verschieben"
        AppLanguage.RUSSIAN -> "Переместить в папку"
        AppLanguage.JAPANESE -> "フォルダに移動"
        AppLanguage.KOREAN -> "폴더로 이동"
    }

    val galleryUnfiled: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "未归类"
        AppLanguage.ENGLISH -> "Unfiled"
        AppLanguage.ARABIC -> "بلا تصنيف"
        AppLanguage.PORTUGUESE -> "Sem pasta"
        AppLanguage.SPANISH -> "Sin carpeta"
        AppLanguage.FRENCH -> "Non classés"
        AppLanguage.GERMAN -> "Ohne Ordner"
        AppLanguage.RUSSIAN -> "Без папки"
        AppLanguage.JAPANESE -> "未分類"
        AppLanguage.KOREAN -> "미분류"
    }

    val galleryFolderUnavailable: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "无法读取此目录，请重新选择。"
        AppLanguage.ENGLISH -> "This folder can’t be read. Choose it again."
        AppLanguage.ARABIC -> "يتعذّر قراءة هذا المجلد. اختره مجددًا."
        AppLanguage.PORTUGUESE -> "Não foi possível ler esta pasta. Selecione-a novamente."
        AppLanguage.SPANISH -> "No se puede leer esta carpeta. Selecciónala de nuevo."
        AppLanguage.FRENCH -> "Impossible de lire ce dossier. Sélectionnez-le à nouveau."
        AppLanguage.GERMAN -> "Dieser Ordner kann nicht gelesen werden. Wähle ihn erneut aus."
        AppLanguage.RUSSIAN -> "Не удалось прочитать папку. Выберите её снова."
        AppLanguage.JAPANESE -> "このフォルダを読み込めません。選び直してください。"
        AppLanguage.KOREAN -> "이 폴더를 읽을 수 없습니다. 다시 선택하세요."
    }

    val galleryFolderEmpty: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "此目录没有视频或子文件夹。"
        AppLanguage.ENGLISH -> "This folder has no videos or subfolders."
        AppLanguage.ARABIC -> "لا يحتوي هذا المجلد على فيديوهات أو مجلدات فرعية."
        AppLanguage.PORTUGUESE -> "Esta pasta não tem vídeos nem subpastas."
        AppLanguage.SPANISH -> "Esta carpeta no tiene vídeos ni subcarpetas."
        AppLanguage.FRENCH -> "Ce dossier ne contient ni vidéos ni sous-dossiers."
        AppLanguage.GERMAN -> "Dieser Ordner enthält keine Videos oder Unterordner."
        AppLanguage.RUSSIAN -> "В папке нет видео или подпапок."
        AppLanguage.JAPANESE -> "このフォルダには動画やサブフォルダがありません。"
        AppLanguage.KOREAN -> "이 폴더에는 동영상이나 하위 폴더가 없습니다."
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

    val lrcAnimNone: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "无动画"
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
    val lrcAnimFade: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "淡入淡出"
        AppLanguage.ENGLISH -> "Fade"
        AppLanguage.ARABIC -> "تلاشي"
        AppLanguage.PORTUGUESE -> "Esmaecer"
        AppLanguage.SPANISH -> "Desvanecer"
        AppLanguage.FRENCH -> "Fondu"
        AppLanguage.GERMAN -> "Überblenden"
        AppLanguage.RUSSIAN -> "Затухание"
        AppLanguage.JAPANESE -> "フェード"
        AppLanguage.KOREAN -> "페이드"
    }
    val lrcAnimSlideUp: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "向上滑动"
        AppLanguage.ENGLISH -> "Slide Up"
        AppLanguage.ARABIC -> "انزلاق لأعلى"
        AppLanguage.PORTUGUESE -> "Deslizar para cima"
        AppLanguage.SPANISH -> "Deslizar hacia arriba"
        AppLanguage.FRENCH -> "Glisser vers le haut"
        AppLanguage.GERMAN -> "Nach oben schieben"
        AppLanguage.RUSSIAN -> "Сдвиг вверх"
        AppLanguage.JAPANESE -> "上にスライド"
        AppLanguage.KOREAN -> "위로 슬라이드"
    }
    val lrcAnimSlideLeft: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "向左滑动"
        AppLanguage.ENGLISH -> "Slide Left"
        AppLanguage.ARABIC -> "انزلاق لليسار"
        AppLanguage.PORTUGUESE -> "Deslizar para a esquerda"
        AppLanguage.SPANISH -> "Deslizar hacia la izquierda"
        AppLanguage.FRENCH -> "Glisser vers la gauche"
        AppLanguage.GERMAN -> "Nach links schieben"
        AppLanguage.RUSSIAN -> "Сдвиг влево"
        AppLanguage.JAPANESE -> "左にスライド"
        AppLanguage.KOREAN -> "왼쪽으로 슬라이드"
    }
    val lrcAnimScale: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "缩放"
        AppLanguage.ENGLISH -> "Scale"
        AppLanguage.ARABIC -> "تكبير"
        AppLanguage.PORTUGUESE -> "Escala"
        AppLanguage.SPANISH -> "Escala"
        AppLanguage.FRENCH -> "Échelle"
        AppLanguage.GERMAN -> "Skalieren"
        AppLanguage.RUSSIAN -> "Масштаб"
        AppLanguage.JAPANESE -> "スケール"
        AppLanguage.KOREAN -> "스케일"
    }
    val lrcAnimTypewriter: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "打字机"
        AppLanguage.ENGLISH -> "Typewriter"
        AppLanguage.ARABIC -> "آلة كاتبة"
        AppLanguage.PORTUGUESE -> "Máquina de escrever"
        AppLanguage.SPANISH -> "Máquina de escribir"
        AppLanguage.FRENCH -> "Machine à écrire"
        AppLanguage.GERMAN -> "Schreibmaschine"
        AppLanguage.RUSSIAN -> "Печатная машинка"
        AppLanguage.JAPANESE -> "タイプライター"
        AppLanguage.KOREAN -> "타자기"
    }
    val lrcAnimKaraoke: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "卡拉OK高亮"
        AppLanguage.ENGLISH -> "Karaoke Highlight"
        AppLanguage.ARABIC -> "تمييز كاريوكي"
        AppLanguage.PORTUGUESE -> "Realce de karaokê"
        AppLanguage.SPANISH -> "Resaltado de karaoke"
        AppLanguage.FRENCH -> "Surlignage karaoké"
        AppLanguage.GERMAN -> "Karaoke-Hervorhebung"
        AppLanguage.RUSSIAN -> "Подсветка караоке"
        AppLanguage.JAPANESE -> "カラオケハイライト"
        AppLanguage.KOREAN -> "노래방 강조"
    }

    val lrcPosTop: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "顶部"
        AppLanguage.ENGLISH -> "Top"
        AppLanguage.ARABIC -> "أعلى"
        AppLanguage.PORTUGUESE -> "Topo"
        AppLanguage.SPANISH -> "Arriba"
        AppLanguage.FRENCH -> "Haut"
        AppLanguage.GERMAN -> "Oben"
        AppLanguage.RUSSIAN -> "Верх"
        AppLanguage.JAPANESE -> "上"
        AppLanguage.KOREAN -> "상단"
    }
    val lrcPosCenter: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "居中"
        AppLanguage.ENGLISH -> "Center"
        AppLanguage.ARABIC -> "وسط"
        AppLanguage.PORTUGUESE -> "Centro"
        AppLanguage.SPANISH -> "Centro"
        AppLanguage.FRENCH -> "Centre"
        AppLanguage.GERMAN -> "Mitte"
        AppLanguage.RUSSIAN -> "Центр"
        AppLanguage.JAPANESE -> "中央"
        AppLanguage.KOREAN -> "중앙"
    }
    val lrcPosBottom: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "底部"
        AppLanguage.ENGLISH -> "Bottom"
        AppLanguage.ARABIC -> "أسفل"
        AppLanguage.PORTUGUESE -> "Inferior"
        AppLanguage.SPANISH -> "Abajo"
        AppLanguage.FRENCH -> "Bas"
        AppLanguage.GERMAN -> "Unten"
        AppLanguage.RUSSIAN -> "Низ"
        AppLanguage.JAPANESE -> "下"
        AppLanguage.KOREAN -> "하단"
    }

    val bgmTagPureMusic: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "纯音乐"
        AppLanguage.ENGLISH -> "Instrumental"
        AppLanguage.ARABIC -> "موسيقى آلية"
        AppLanguage.PORTUGUESE -> "Instrumental"
        AppLanguage.SPANISH -> "Instrumental"
        AppLanguage.FRENCH -> "Instrumental"
        AppLanguage.GERMAN -> "Instrumental"
        AppLanguage.RUSSIAN -> "Инструментал"
        AppLanguage.JAPANESE -> "インストゥルメンタル"
        AppLanguage.KOREAN -> "기악"
    }
    val bgmTagPop: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "流行"
        AppLanguage.ENGLISH -> "Pop"
        AppLanguage.ARABIC -> "بوب"
        AppLanguage.PORTUGUESE -> "Pop"
        AppLanguage.SPANISH -> "Pop"
        AppLanguage.FRENCH -> "Pop"
        AppLanguage.GERMAN -> "Pop"
        AppLanguage.RUSSIAN -> "Поп"
        AppLanguage.JAPANESE -> "ポップ"
        AppLanguage.KOREAN -> "팝"
    }
    val bgmTagRock: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "摇滚"
        AppLanguage.ENGLISH -> "Rock"
        AppLanguage.ARABIC -> "روك"
        AppLanguage.PORTUGUESE -> "Rock"
        AppLanguage.SPANISH -> "Rock"
        AppLanguage.FRENCH -> "Rock"
        AppLanguage.GERMAN -> "Rock"
        AppLanguage.RUSSIAN -> "Рок"
        AppLanguage.JAPANESE -> "ロック"
        AppLanguage.KOREAN -> "록"
    }
    val bgmTagClassical: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "古典"
        AppLanguage.ENGLISH -> "Classical"
        AppLanguage.ARABIC -> "كلاسيكي"
        AppLanguage.PORTUGUESE -> "Clássica"
        AppLanguage.SPANISH -> "Clásica"
        AppLanguage.FRENCH -> "Classique"
        AppLanguage.GERMAN -> "Klassik"
        AppLanguage.RUSSIAN -> "Классика"
        AppLanguage.JAPANESE -> "クラシック"
        AppLanguage.KOREAN -> "클래식"
    }
    val bgmTagJazz: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "爵士"
        AppLanguage.ENGLISH -> "Jazz"
        AppLanguage.ARABIC -> "جاز"
        AppLanguage.PORTUGUESE -> "Jazz"
        AppLanguage.SPANISH -> "Jazz"
        AppLanguage.FRENCH -> "Jazz"
        AppLanguage.GERMAN -> "Jazz"
        AppLanguage.RUSSIAN -> "Джаз"
        AppLanguage.JAPANESE -> "ジャズ"
        AppLanguage.KOREAN -> "재즈"
    }
    val bgmTagElectronic: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "电子"
        AppLanguage.ENGLISH -> "Electronic"
        AppLanguage.ARABIC -> "إلكتروني"
        AppLanguage.PORTUGUESE -> "Eletrônica"
        AppLanguage.SPANISH -> "Electrónica"
        AppLanguage.FRENCH -> "Électronique"
        AppLanguage.GERMAN -> "Elektronisch"
        AppLanguage.RUSSIAN -> "Электронная"
        AppLanguage.JAPANESE -> "エレクトロニック"
        AppLanguage.KOREAN -> "일렉트로닉"
    }
    val bgmTagFolk: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "民谣"
        AppLanguage.ENGLISH -> "Folk"
        AppLanguage.ARABIC -> "شعبي"
        AppLanguage.PORTUGUESE -> "Folk"
        AppLanguage.SPANISH -> "Folk"
        AppLanguage.FRENCH -> "Folk"
        AppLanguage.GERMAN -> "Folk"
        AppLanguage.RUSSIAN -> "Фолк"
        AppLanguage.JAPANESE -> "フォーク"
        AppLanguage.KOREAN -> "포크"
    }
    val bgmTagChineseStyle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "古风"
        AppLanguage.ENGLISH -> "Chinese Style"
        AppLanguage.ARABIC -> "صيني تقليدي"
        AppLanguage.PORTUGUESE -> "Estilo Chinês"
        AppLanguage.SPANISH -> "Estilo Chino"
        AppLanguage.FRENCH -> "Style Chinois"
        AppLanguage.GERMAN -> "Chinesischer Stil"
        AppLanguage.RUSSIAN -> "Китайский стиль"
        AppLanguage.JAPANESE -> "中国風"
        AppLanguage.KOREAN -> "중국풍"
    }
    val bgmTagAnime: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "动漫"
        AppLanguage.ENGLISH -> "Anime"
        AppLanguage.ARABIC -> "أنيمي"
        AppLanguage.PORTUGUESE -> "Anime"
        AppLanguage.SPANISH -> "Anime"
        AppLanguage.FRENCH -> "Anime"
        AppLanguage.GERMAN -> "Anime"
        AppLanguage.RUSSIAN -> "Аниме"
        AppLanguage.JAPANESE -> "アニメ"
        AppLanguage.KOREAN -> "애니메이션"
    }
    val bgmTagGame: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "游戏"
        AppLanguage.ENGLISH -> "Game"
        AppLanguage.ARABIC -> "ألعاب"
        AppLanguage.PORTUGUESE -> "Jogo"
        AppLanguage.SPANISH -> "Juego"
        AppLanguage.FRENCH -> "Jeu"
        AppLanguage.GERMAN -> "Spiel"
        AppLanguage.RUSSIAN -> "Игра"
        AppLanguage.JAPANESE -> "ゲーム"
        AppLanguage.KOREAN -> "게임"
    }
    val bgmTagMovie: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "影视"
        AppLanguage.ENGLISH -> "Movie"
        AppLanguage.ARABIC -> "أفلام"
        AppLanguage.PORTUGUESE -> "Filme"
        AppLanguage.SPANISH -> "Película"
        AppLanguage.FRENCH -> "Film"
        AppLanguage.GERMAN -> "Film"
        AppLanguage.RUSSIAN -> "Фильм"
        AppLanguage.JAPANESE -> "映画"
        AppLanguage.KOREAN -> "영화"
    }
    val bgmTagHealing: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "治愈"
        AppLanguage.ENGLISH -> "Healing"
        AppLanguage.ARABIC -> "شفاء"
        AppLanguage.PORTUGUESE -> "Curação"
        AppLanguage.SPANISH -> "Sanador"
        AppLanguage.FRENCH -> "Apaisant"
        AppLanguage.GERMAN -> "Heilend"
        AppLanguage.RUSSIAN -> "Исцеляющая"
        AppLanguage.JAPANESE -> "ヒーリング"
        AppLanguage.KOREAN -> "힐링"
    }
    val bgmTagExciting: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "激昂"
        AppLanguage.ENGLISH -> "Exciting"
        AppLanguage.ARABIC -> "مثير"
        AppLanguage.PORTUGUESE -> "Animado"
        AppLanguage.SPANISH -> "Emocionante"
        AppLanguage.FRENCH -> "Enthousiasmant"
        AppLanguage.GERMAN -> "Mitreißend"
        AppLanguage.RUSSIAN -> "Вдохновляющая"
        AppLanguage.JAPANESE -> "エキサイティング"
        AppLanguage.KOREAN -> "신나는"
    }
    val bgmTagSad: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "伤感"
        AppLanguage.ENGLISH -> "Sad"
        AppLanguage.ARABIC -> "حزين"
        AppLanguage.PORTUGUESE -> "Triste"
        AppLanguage.SPANISH -> "Triste"
        AppLanguage.FRENCH -> "Triste"
        AppLanguage.GERMAN -> "Traurig"
        AppLanguage.RUSSIAN -> "Грустная"
        AppLanguage.JAPANESE -> "切ない"
        AppLanguage.KOREAN -> "슬픈"
    }
    val bgmTagRomantic: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "浪漫"
        AppLanguage.ENGLISH -> "Romantic"
        AppLanguage.ARABIC -> "رومانسي"
        AppLanguage.PORTUGUESE -> "Romântico"
        AppLanguage.SPANISH -> "Romántico"
        AppLanguage.FRENCH -> "Romantique"
        AppLanguage.GERMAN -> "Romantisch"
        AppLanguage.RUSSIAN -> "Романтичная"
        AppLanguage.JAPANESE -> "ロマンチック"
        AppLanguage.KOREAN -> "로맨틱"
    }
    val bgmTagRelaxing: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "轻松"
        AppLanguage.ENGLISH -> "Relaxing"
        AppLanguage.ARABIC -> "مريح"
        AppLanguage.PORTUGUESE -> "Relaxante"
        AppLanguage.SPANISH -> "Relajante"
        AppLanguage.FRENCH -> "Relaxant"
        AppLanguage.GERMAN -> "Entspannend"
        AppLanguage.RUSSIAN -> "Расслабляющая"
        AppLanguage.JAPANESE -> "リラックス"
        AppLanguage.KOREAN -> "릴렉싱"
    }
    val bgmTagWorkout: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "运动"
        AppLanguage.ENGLISH -> "Workout"
        AppLanguage.ARABIC -> "تمرين"
        AppLanguage.PORTUGUESE -> "Treino"
        AppLanguage.SPANISH -> "Entrenamiento"
        AppLanguage.FRENCH -> "Entraînement"
        AppLanguage.GERMAN -> "Training"
        AppLanguage.RUSSIAN -> "Тренировка"
        AppLanguage.JAPANESE -> "ワークアウト"
        AppLanguage.KOREAN -> "운동"
    }
    val bgmTagSleep: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "助眠"
        AppLanguage.ENGLISH -> "Sleep"
        AppLanguage.ARABIC -> "نوم"
        AppLanguage.PORTUGUESE -> "Sono"
        AppLanguage.SPANISH -> "Dormir"
        AppLanguage.FRENCH -> "Sommeil"
        AppLanguage.GERMAN -> "Schlaf"
        AppLanguage.RUSSIAN -> "Сон"
        AppLanguage.JAPANESE -> "睡眠"
        AppLanguage.KOREAN -> "수면"
    }
    val bgmTagStudy: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "学习"
        AppLanguage.ENGLISH -> "Study"
        AppLanguage.ARABIC -> "دراسة"
        AppLanguage.PORTUGUESE -> "Estudo"
        AppLanguage.SPANISH -> "Estudio"
        AppLanguage.FRENCH -> "Étude"
        AppLanguage.GERMAN -> "Lernen"
        AppLanguage.RUSSIAN -> "Учёба"
        AppLanguage.JAPANESE -> "学習"
        AppLanguage.KOREAN -> "학습"
    }
    val bgmTagOther: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "其他"
        AppLanguage.ENGLISH -> "Other"
        AppLanguage.ARABIC -> "أخرى"
        AppLanguage.PORTUGUESE -> "Outro"
        AppLanguage.SPANISH -> "Otro"
        AppLanguage.FRENCH -> "Autre"
        AppLanguage.GERMAN -> "Andere"
        AppLanguage.RUSSIAN -> "Другое"
        AppLanguage.JAPANESE -> "その他"
        AppLanguage.KOREAN -> "기타"
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

    val phpHeroTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "PHP 应用"
        AppLanguage.ENGLISH -> "PHP Application"
        AppLanguage.ARABIC -> "تطبيق PHP"
        AppLanguage.PORTUGUESE -> "Aplicação PHP"
        AppLanguage.SPANISH -> "Aplicación PHP"
        AppLanguage.FRENCH -> "Application PHP"
        AppLanguage.GERMAN -> "PHP-Anwendung"
        AppLanguage.RUSSIAN -> "Приложение PHP"
        AppLanguage.JAPANESE -> "PHP アプリケーション"
        AppLanguage.KOREAN -> "PHP 애플리케이션"
    }
    val phpHeroDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "将 PHP 项目打包为独立 Android 应用"
        AppLanguage.ENGLISH -> "Package PHP project as standalone Android app"
        AppLanguage.ARABIC -> "تغليف مشروع PHP كتطبيق Android مستقل"
        AppLanguage.PORTUGUESE -> "Embalar projeto PHP como app Android autônoma"
        AppLanguage.SPANISH -> "Empaquetar proyecto PHP como app Android independiente"
        AppLanguage.FRENCH -> "Emballer un projet PHP comme application Android autonome"
        AppLanguage.GERMAN -> "PHP-Projekt als eigenständige Android-App verpacken"
        AppLanguage.RUSSIAN -> "Упаковать проект PHP как самостоятельное приложение Android"
        AppLanguage.JAPANESE -> "PHP プロジェクトを独立した Android アプリとしてパッケージ化"
        AppLanguage.KOREAN -> "PHP 프로젝트를 독립 Android 앱으로 패키징"
    }
    val phpComposerDeps: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Composer 依赖"
        AppLanguage.ENGLISH -> "Composer Dependencies"
        AppLanguage.ARABIC -> "متطلبات Composer"
        AppLanguage.PORTUGUESE -> "Dependências do Composer"
        AppLanguage.SPANISH -> "Dependencias de Composer"
        AppLanguage.FRENCH -> "Dépendances Composer"
        AppLanguage.GERMAN -> "Composer-Abhängigkeiten"
        AppLanguage.RUSSIAN -> "Зависимости Composer"
        AppLanguage.JAPANESE -> "Composer 依存関係"
        AppLanguage.KOREAN -> "Composer 의존성"
    }
    val phpRequireDeps: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "运行依赖"
        AppLanguage.ENGLISH -> "Dependencies"
        AppLanguage.ARABIC -> "التبعيات"
        AppLanguage.PORTUGUESE -> "Dependências"
        AppLanguage.SPANISH -> "Dependencias"
        AppLanguage.FRENCH -> "Dépendances"
        AppLanguage.GERMAN -> "Abhängigkeiten"
        AppLanguage.RUSSIAN -> "Зависимости"
        AppLanguage.JAPANESE -> "依存関係"
        AppLanguage.KOREAN -> "의존성"
    }
    val phpRequireDevDeps: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "开发依赖"
        AppLanguage.ENGLISH -> "Dev Dependencies"
        AppLanguage.ARABIC -> "تبعيات التطوير"
        AppLanguage.PORTUGUESE -> "Dependências de Desenvolvimento"
        AppLanguage.SPANISH -> "Dependencias de Desarrollo"
        AppLanguage.FRENCH -> "Dépendances de Dev"
        AppLanguage.GERMAN -> "Dev-Abhängigkeiten"
        AppLanguage.RUSSIAN -> "Зависимости разработки"
        AppLanguage.JAPANESE -> "開発依存関係"
        AppLanguage.KOREAN -> "개발 의존성"
    }
    val phpDocRootSelect: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Web 根目录选择"
        AppLanguage.ENGLISH -> "Document Root Selection"
        AppLanguage.ARABIC -> "اختيار جذر المستند"
        AppLanguage.PORTUGUESE -> "Seleção de Document Root"
        AppLanguage.SPANISH -> "Selección de Document Root"
        AppLanguage.FRENCH -> "Sélection de la racine des documents"
        AppLanguage.GERMAN -> "Document Root Auswahl"
        AppLanguage.RUSSIAN -> "Выбор корня документов"
        AppLanguage.JAPANESE -> "ドキュメントルート選択"
        AppLanguage.KOREAN -> "Document Root 선택"
    }
    val phpDocRootHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Web 服务器的根目录，通常为 public/ 或项目根目录"
        AppLanguage.ENGLISH -> "Web server root directory, usually public/ or project root"
        AppLanguage.ARABIC -> "دليل جذر خادم الويب، عادة public/ أو جذر المشروع"
        AppLanguage.PORTUGUESE -> "Diretório raiz do servidor web, geralmente public/ ou raiz do projeto"
        AppLanguage.SPANISH -> "Directorio raíz del servidor web, generalmente public/ o raíz del proyecto"
        AppLanguage.FRENCH -> "Répertoire racine du serveur web, généralement public/ ou racine du projet"
        AppLanguage.GERMAN -> "Root-Verzeichnis des Webservers, meist public/ oder Projekt-Root"
        AppLanguage.RUSSIAN -> "Корневой каталог веб-сервера, обычно public/ или корень проекта"
        AppLanguage.JAPANESE -> "Webサーバーのルートディレクトリ、通常 public/ またはプロジェクトルート"
        AppLanguage.KOREAN -> "웹 서버 루트 디렉토리, 보통 public/ 또는 프로젝트 루트"
    }
    val phpDocRootExample: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "public"
        AppLanguage.ENGLISH -> "public"
        AppLanguage.ARABIC -> "public"
        AppLanguage.PORTUGUESE -> "public"
        AppLanguage.SPANISH -> "public"
        AppLanguage.FRENCH -> "public"
        AppLanguage.GERMAN -> "public"
        AppLanguage.RUSSIAN -> "public"
        AppLanguage.JAPANESE -> "public"
        AppLanguage.KOREAN -> "public"
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
    val pythonExtensions: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Python 扩展"
        AppLanguage.ENGLISH -> "Python Extensions"
        AppLanguage.ARABIC -> "إضافات Python"
        AppLanguage.PORTUGUESE -> "Extensões Python"
        AppLanguage.SPANISH -> "Extensiones Python"
        AppLanguage.FRENCH -> "Extensions Python"
        AppLanguage.GERMAN -> "Python-Erweiterungen"
        AppLanguage.RUSSIAN -> "Расширения Python"
        AppLanguage.JAPANESE -> "Python 拡張機能"
        AppLanguage.KOREAN -> "Python 확장"
    }
    val pythonExtensionsHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "将 .so / .pyd 文件放入项目 python_exts/ 目录。扩展名用于加载顺序，.so 文件名留空则自动补全。"
        AppLanguage.ENGLISH -> "Place .so / .pyd files in project python_exts/. Extension name is used for load order; leave .so filename blank to auto-complete."
        AppLanguage.ARABIC -> "ضع ملفات .so / .pyd في دليل المشروع python_exts/. يستخدم اسم الإضافة لترتيب التحميل؛ اترك اسم ملف .so فارغاً للإكمال التلقائي."
        AppLanguage.PORTUGUESE -> "Coloque arquivos .so / .pyd no diretório python_exts/ do projeto. O nome da extensão é usado para a ordem de carregamento; deixe o nome do arquivo .so em branco para auto-completar."
        AppLanguage.SPANISH -> "Coloca archivos .so / .pyd en el directorio python_exts/ del proyecto. El nombre de la extensión se usa para el orden de carga; deja el nombre del archivo .so en blanco para autocompletar."
        AppLanguage.FRENCH -> "Placez les fichiers .so / .pyd dans le répertoire python_exts/ du projet. Le nom de l'extension est utilisé pour l'ordre de chargement ; laissez le nom du fichier .so vide pour auto-compléter."
        AppLanguage.GERMAN -> "Lege .so / .pyd-Dateien im Projektverzeichnis python_exts/ ab. Der Erweiterungsname wird für die Ladereihenfolge verwendet; .so-Dateinamen leer lassen für Auto-Vervollständigung."
        AppLanguage.RUSSIAN -> "Поместите файлы .so / .pyd в каталог python_exts/ проекта. Имя расширения используется для порядка загрузки; оставьте имя файла .so пустым для автозаполнения."
        AppLanguage.JAPANESE -> ".so / .pyd ファイルをプロジェクトの python_exts/ ディレクトリに配置。拡張名はロード順序に使用、.so ファイル名を空欄で自動補完。"
        AppLanguage.KOREAN -> ".so / .pyd 파일을 프로젝트 python_exts/ 디렉토리에 배치. 확장명은 로드 순서에 사용되며, .so 파일명을 비우면 자동 완성됩니다."
    }
    val pythonCustomExtensions: String get() = when (Strings.lang) {
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
    val pythonAddCustomExtension: String get() = when (Strings.lang) {
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
    val pythonAddCustomExtensionButton: String get() = when (Strings.lang) {
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
    val pythonCustomExtensionHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "将 .so / .pyd 文件放入项目 python_exts/ 目录"
        AppLanguage.ENGLISH -> "Place .so / .pyd files in project python_exts/"
        AppLanguage.ARABIC -> "ضع ملفات .so / .pyd في دليل المشروع python_exts/"
        AppLanguage.PORTUGUESE -> "Coloque arquivos .so / .pyd no diretório python_exts/ do projeto"
        AppLanguage.SPANISH -> "Coloca archivos .so / .pyd en el directorio python_exts/ del proyecto"
        AppLanguage.FRENCH -> "Placez les fichiers .so / .pyd dans le répertoire python_exts/ du projet"
        AppLanguage.GERMAN -> "Lege .so / .pyd-Dateien im Projektverzeichnis python_exts/ ab"
        AppLanguage.RUSSIAN -> "Поместите файлы .so / .pyd в каталог python_exts/ проекта"
        AppLanguage.JAPANESE -> ".so / .pyd ファイルをプロジェクトの python_exts/ ディレクトリに配置"
        AppLanguage.KOREAN -> ".so / .pyd 파일을 프로젝트 python_exts/ 디렉토리에 배치"
    }
    val pythonCustomExtensionName: String get() = when (Strings.lang) {
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
    val pythonCustomExtensionSoName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> ".so / .pyd 文件名（可选）"
        AppLanguage.ENGLISH -> ".so / .pyd Filename (Optional)"
        AppLanguage.ARABIC -> "اسم ملف .so / .pyd (اختياري)"
        AppLanguage.PORTUGUESE -> "Nome do arquivo .so / .pyd (Opcional)"
        AppLanguage.SPANISH -> "Nombre del archivo .so / .pyd (Opcional)"
        AppLanguage.FRENCH -> "Nom du fichier .so / .pyd (Facultatif)"
        AppLanguage.GERMAN -> ".so / .pyd-Dateiname (Optional)"
        AppLanguage.RUSSIAN -> "Имя файла .so / .pyd (необязательно)"
        AppLanguage.JAPANESE -> ".so / .pyd ファイル名(任意)"
        AppLanguage.KOREAN -> ".so / .pyd 파일명(선택)"
    }
    val pythonCustomExtensionOrder: String get() = when (Strings.lang) {
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
    val phpDatabaseConfig: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "数据库配置"
        AppLanguage.ENGLISH -> "Database Configuration"
        AppLanguage.ARABIC -> "إعدادات قاعدة البيانات"
        AppLanguage.PORTUGUESE -> "Configuração de Banco de Dados"
        AppLanguage.SPANISH -> "Configuración de Base de Datos"
        AppLanguage.FRENCH -> "Configuration de Base de Données"
        AppLanguage.GERMAN -> "Datenbankkonfiguration"
        AppLanguage.RUSSIAN -> "Конфигурация базы данных"
        AppLanguage.JAPANESE -> "データベース設定"
        AppLanguage.KOREAN -> "데이터베이스 설정"
    }
    val phpSqlitePath: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "SQLite 数据库路径"
        AppLanguage.ENGLISH -> "SQLite Database Path"
        AppLanguage.ARABIC -> "مسار قاعدة بيانات SQLite"
        AppLanguage.PORTUGUESE -> "Caminho do Banco de Dados SQLite"
        AppLanguage.SPANISH -> "Ruta de Base de Datos SQLite"
        AppLanguage.FRENCH -> "Chemin de Base de Données SQLite"
        AppLanguage.GERMAN -> "SQLite-Datenbankpfad"
        AppLanguage.RUSSIAN -> "Путь к базе данных SQLite"
        AppLanguage.JAPANESE -> "SQLite データベースパス"
        AppLanguage.KOREAN -> "SQLite 데이터베이스 경로"
    }
    val phpDbDetected: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "检测到数据库文件"
        AppLanguage.ENGLISH -> "Database file detected"
        AppLanguage.ARABIC -> "تم اكتشاف ملف قاعدة البيانات"
        AppLanguage.PORTUGUESE -> "Arquivo de banco de dados detectado"
        AppLanguage.SPANISH -> "Archivo de base de datos detectado"
        AppLanguage.FRENCH -> "Fichier de base de données détecté"
        AppLanguage.GERMAN -> "Datenbankdatei erkannt"
        AppLanguage.RUSSIAN -> "Обнаружен файл базы данных"
        AppLanguage.JAPANESE -> "データベースファイルを検出"
        AppLanguage.KOREAN -> "데이터베이스 파일 감지됨"
    }
    val phpFrameworkTip: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "框架提示"
        AppLanguage.ENGLISH -> "Framework Tips"
        AppLanguage.ARABIC -> "نصائح الإطار"
        AppLanguage.PORTUGUESE -> "Dicas de Framework"
        AppLanguage.SPANISH -> "Consejos de Framework"
        AppLanguage.FRENCH -> "Astuces de Framework"
        AppLanguage.GERMAN -> "Framework-Tipps"
        AppLanguage.RUSSIAN -> "Советы по фреймворку"
        AppLanguage.JAPANESE -> "フレームワークのヒント"
        AppLanguage.KOREAN -> "프레임워크 팁"
    }
    val phpLaravelTip: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Laravel 项目：已自动配置 storage/、bootstrap/cache/ 目录权限，APP_KEY 将自动生成"
        AppLanguage.ENGLISH -> "Laravel: storage/ and bootstrap/cache/ permissions auto-configured. APP_KEY will be auto-generated."
        AppLanguage.ARABIC -> "Laravel: تم تكوين أذونات storage/ و bootstrap/cache/ تلقائياً. سيتم إنشاء APP_KEY تلقائياً."
        AppLanguage.PORTUGUESE -> "Laravel: permissões de storage/ e bootstrap/cache/ auto-configuradas. APP_KEY será gerada automaticamente."
        AppLanguage.SPANISH -> "Laravel: permisos de storage/ y bootstrap/cache/ auto-configurados. APP_KEY se generará automáticamente."
        AppLanguage.FRENCH -> "Laravel : permissions storage/ et bootstrap/cache/ auto-configurées. APP_KEY sera générée automatiquement."
        AppLanguage.GERMAN -> "Laravel: storage/ und bootstrap/cache/-Berechtigungen automatisch konfiguriert. APP_KEY wird automatisch generiert."
        AppLanguage.RUSSIAN -> "Laravel: разрешения storage/ и bootstrap/cache/ настроены автоматически. APP_KEY будет сгенерирован автоматически."
        AppLanguage.JAPANESE -> "Laravel: storage/ と bootstrap/cache/ の権限を自動設定。APP_KEY は自動生成されます。"
        AppLanguage.KOREAN -> "Laravel: storage/ 및 bootstrap/cache/ 권한이 자동 설정됩니다. APP_KEY는 자동 생성됩니다."
    }
    val phpThinkPhpTip: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "ThinkPHP 项目：runtime/ 目录已自动配置，支持多应用模式"
        AppLanguage.ENGLISH -> "ThinkPHP: runtime/ directory auto-configured. Multi-app mode supported."
        AppLanguage.ARABIC -> "ThinkPHP: تم تكوين دليل runtime/ تلقائياً. وضع التطبيقات المتعددة مدعوم."
        AppLanguage.PORTUGUESE -> "ThinkPHP: diretório runtime/ auto-configurado. Modo multi-app suportado."
        AppLanguage.SPANISH -> "ThinkPHP: directorio runtime/ auto-configurado. Modo multi-app soportado."
        AppLanguage.FRENCH -> "ThinkPHP : répertoire runtime/ auto-configuré. Mode multi-app pris en charge."
        AppLanguage.GERMAN -> "ThinkPHP: runtime/-Verzeichnis automatisch konfiguriert. Multi-App-Modus unterstützt."
        AppLanguage.RUSSIAN -> "ThinkPHP: каталог runtime/ настроен автоматически. Поддерживается режим multi-app."
        AppLanguage.JAPANESE -> "ThinkPHP: runtime/ ディレクトリを自動設定。マルチアプリモード対応。"
        AppLanguage.KOREAN -> "ThinkPHP: runtime/ 디렉토리 자동 설정. 멀티앱 모드 지원."
    }
    val phpCodeIgniterTip: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "CodeIgniter 项目：writable/ 目录已自动配置，环境已设置为 production"
        AppLanguage.ENGLISH -> "CodeIgniter: writable/ directory auto-configured. Environment set to production."
        AppLanguage.ARABIC -> "CodeIgniter: تم تكوين دليل writable/ تلقائياً. تم ضبط البيئة على production."
        AppLanguage.PORTUGUESE -> "CodeIgniter: diretório writable/ auto-configurado. Ambiente definido como production."
        AppLanguage.SPANISH -> "CodeIgniter: directorio writable/ auto-configurado. Entorno establecido en production."
        AppLanguage.FRENCH -> "CodeIgniter : répertoire writable/ auto-configuré. Environnement défini sur production."
        AppLanguage.GERMAN -> "CodeIgniter: writable/-Verzeichnis automatisch konfiguriert. Umgebung auf production gesetzt."
        AppLanguage.RUSSIAN -> "CodeIgniter: каталог writable/ настроен автоматически. Окружение установлено на production."
        AppLanguage.JAPANESE -> "CodeIgniter: writable/ ディレクトリを自動設定。環境は production に設定。"
        AppLanguage.KOREAN -> "CodeIgniter: writable/ 디렉토리 자동 설정. 환경이 production으로 설정됩니다."
    }
    val phpSlimTip: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Slim 项目：已检测到路由配置，支持 PSR-7 中间件"
        AppLanguage.ENGLISH -> "Slim: Routes detected. PSR-7 middleware supported."
        AppLanguage.ARABIC -> "Slim: تم اكتشاف المسارات. وسيط PSR-7 مدعوم."
        AppLanguage.PORTUGUESE -> "Slim: Rotas detectadas. Middleware PSR-7 suportado."
        AppLanguage.SPANISH -> "Slim: Rutas detectadas. Middleware PSR-7 soportado."
        AppLanguage.FRENCH -> "Slim : Routes détectées. Middleware PSR-7 pris en charge."
        AppLanguage.GERMAN -> "Slim: Routen erkannt. PSR-7-Middleware unterstützt."
        AppLanguage.RUSSIAN -> "Slim: Маршруты обнаружены. Middleware PSR-7 поддерживается."
        AppLanguage.JAPANESE -> "Slim: ルートを検出。PSR-7 ミドルウェア対応。"
        AppLanguage.KOREAN -> "Slim: 라우트 감지됨. PSR-7 미들웨어 지원."
    }
    val phpAdvancedConfig: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "高级配置"
        AppLanguage.ENGLISH -> "Advanced Configuration"
        AppLanguage.ARABIC -> "إعدادات متقدمة"
        AppLanguage.PORTUGUESE -> "Configuração Avançada"
        AppLanguage.SPANISH -> "Configuración Avanzada"
        AppLanguage.FRENCH -> "Configuration Avancée"
        AppLanguage.GERMAN -> "Erweiterte Konfiguration"
        AppLanguage.RUSSIAN -> "Расширенная конфигурация"
        AppLanguage.JAPANESE -> "詳細設定"
        AppLanguage.KOREAN -> "고급 설정"
    }
    val phpProjectRoot: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "项目根目录"
        AppLanguage.ENGLISH -> "Project Root"
        AppLanguage.ARABIC -> "جذر المشروع"
        AppLanguage.PORTUGUESE -> "Raiz do Projeto"
        AppLanguage.SPANISH -> "Raíz del Proyecto"
        AppLanguage.FRENCH -> "Racine du Projet"
        AppLanguage.GERMAN -> "Projekt-Root"
        AppLanguage.RUSSIAN -> "Корень проекта"
        AppLanguage.JAPANESE -> "プロジェクトルート"
        AppLanguage.KOREAN -> "프로젝트 루트"
    }
    val phpVersion: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "PHP 版本"
        AppLanguage.ENGLISH -> "PHP Version"
        AppLanguage.ARABIC -> "إصدار PHP"
        AppLanguage.PORTUGUESE -> "Versão do PHP"
        AppLanguage.SPANISH -> "Versión de PHP"
        AppLanguage.FRENCH -> "Version de PHP"
        AppLanguage.GERMAN -> "PHP-Version"
        AppLanguage.RUSSIAN -> "Версия PHP"
        AppLanguage.JAPANESE -> "PHP バージョン"
        AppLanguage.KOREAN -> "PHP 버전"
    }
    val phpDetectedDirs: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "检测到的 Web 目录"
        AppLanguage.ENGLISH -> "Detected Web Directories"
        AppLanguage.ARABIC -> "أدلة الويب المكتشفة"
        AppLanguage.PORTUGUESE -> "Diretórios Web Detectados"
        AppLanguage.SPANISH -> "Directorios Web Detectados"
        AppLanguage.FRENCH -> "Répertoires Web Détectés"
        AppLanguage.GERMAN -> "Erkannte Web-Verzeichnisse"
        AppLanguage.RUSSIAN -> "Обнаруженные веб-каталоги"
        AppLanguage.JAPANESE -> "検出された Web ディレクトリ"
        AppLanguage.KOREAN -> "감지된 웹 디렉토리"
    }
    val phpCustomPath: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自定义路径"
        AppLanguage.ENGLISH -> "Custom Path"
        AppLanguage.ARABIC -> "مسار مخصص"
        AppLanguage.PORTUGUESE -> "Caminho Personalizado"
        AppLanguage.SPANISH -> "Ruta Personalizada"
        AppLanguage.FRENCH -> "Chemin Personnalisé"
        AppLanguage.GERMAN -> "Benutzerdefinierter Pfad"
        AppLanguage.RUSSIAN -> "Пользовательский путь"
        AppLanguage.JAPANESE -> "カスタムパス"
        AppLanguage.KOREAN -> "사용자 정의 경로"
    }

    val pyHeroTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Python 应用"
        AppLanguage.ENGLISH -> "Python Application"
        AppLanguage.ARABIC -> "تطبيق Python"
        AppLanguage.PORTUGUESE -> "Aplicação Python"
        AppLanguage.SPANISH -> "Aplicación Python"
        AppLanguage.FRENCH -> "Application Python"
        AppLanguage.GERMAN -> "Python-Anwendung"
        AppLanguage.RUSSIAN -> "Приложение Python"
        AppLanguage.JAPANESE -> "Python アプリケーション"
        AppLanguage.KOREAN -> "Python 애플리케이션"
    }
    val pyHeroDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "将 Python Web 项目打包为独立 Android 应用"
        AppLanguage.ENGLISH -> "Package Python Web project as standalone Android app"
        AppLanguage.ARABIC -> "تغليف مشروع Python Web كتطبيق Android مستقل"
        AppLanguage.PORTUGUESE -> "Embalar projeto Python Web como app Android autônoma"
        AppLanguage.SPANISH -> "Empaquetar proyecto Python Web como app Android independiente"
        AppLanguage.FRENCH -> "Emballer un projet Python Web comme application Android autonome"
        AppLanguage.GERMAN -> "Python-Webprojekt als eigenständige Android-App verpacken"
        AppLanguage.RUSSIAN -> "Упаковать веб-проект Python как самостоятельное приложение Android"
        AppLanguage.JAPANESE -> "Python Web プロジェクトを独立した Android アプリとしてパッケージ化"
        AppLanguage.KOREAN -> "Python 웹 프로젝트를 독립 Android 앱으로 패키징"
    }
    fun pyFrameworkTip(framework: String?): String? = when (Strings.lang) {
        AppLanguage.CHINESE -> when (framework?.lowercase()) {
            "flask" -> "Flask：已为生产环境自动关闭调试模式，静态文件默认从 static/ 目录提供。"
            "tornado" -> "Tornado：已自动配置 IOLoop，可用 WebSocket，默认启用非阻塞模式。"
            else -> null
        }
        AppLanguage.ENGLISH -> when (framework?.lowercase()) {
            "flask" -> "Flask: Debug mode is auto-disabled for production. Static files are served from the static/ folder."
            "tornado" -> "Tornado: IOLoop is auto-configured, WebSocket support is available, and non-blocking mode is enabled."
            else -> null
        }
        AppLanguage.ARABIC -> when (framework?.lowercase()) {
            "flask" -> "Flask: يتم تعطيل وضع التصحيح تلقائياً للإنتاج، وتُخدَّم الملفات الثابتة من مجلد static/."
            "tornado" -> "Tornado: تتم تهيئة IOLoop تلقائياً، ودعم WebSocket متاح، وتم تفعيل الوضع غير المتزامن."
            else -> null
        }
        AppLanguage.PORTUGUESE -> when (framework?.lowercase()) {
            "flask" -> "Flask: o modo debug é desativado automaticamente em produção. Arquivos estáticos saem da pasta static/."
            "tornado" -> "Tornado: o IOLoop é configurado automaticamente, com suporte a WebSocket e modo não bloqueante."
            else -> null
        }
        AppLanguage.SPANISH -> when (framework?.lowercase()) {
            "flask" -> "Flask: el modo debug se desactiva automáticamente en producción. Los archivos estáticos se sirven desde static/."
            "tornado" -> "Tornado: IOLoop se configura automáticamente, con soporte WebSocket y modo no bloqueante."
            else -> null
        }
        AppLanguage.FRENCH -> when (framework?.lowercase()) {
            "flask" -> "Flask : le mode debug est désactivé automatiquement en production. Les fichiers statiques sont servis depuis static/."
            "tornado" -> "Tornado : l'IOLoop est configuré automatiquement, avec support WebSocket et mode non bloquant."
            else -> null
        }
        AppLanguage.GERMAN -> when (framework?.lowercase()) {
            "flask" -> "Flask: Debug-Modus wird für Produktion automatisch deaktiviert. Statische Dateien kommen aus static/."
            "tornado" -> "Tornado: IOLoop wird automatisch konfiguriert, WebSocket ist verfügbar, Non-Blocking ist aktiv."
            else -> null
        }
        AppLanguage.RUSSIAN -> when (framework?.lowercase()) {
            "flask" -> "Flask: режим отладки автоматически отключается в продакшене. Статика раздаётся из static/."
            "tornado" -> "Tornado: IOLoop настраивается автоматически, доступны WebSocket и неблокирующий режим."
            else -> null
        }
        AppLanguage.JAPANESE -> when (framework?.lowercase()) {
            "flask" -> "Flask: 本番ではデバッグモードを自動無効化。静的ファイルは static/ から配信します。"
            "tornado" -> "Tornado: IOLoop を自動設定。WebSocket 対応、ノンブロッキングを有効化。"
            else -> null
        }
        AppLanguage.KOREAN -> when (framework?.lowercase()) {
            "flask" -> "Flask: 프로덕션에서는 디버그 모드가 자동 비활성화됩니다. 정적 파일은 static/에서 제공됩니다."
            "tornado" -> "Tornado: IOLoop이 자동 구성되며 WebSocket과 논블로킹 모드를 사용합니다."
            else -> null
        }
    }
    val pyRequirements: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "项目依赖"
        AppLanguage.ENGLISH -> "Project Dependencies"
        AppLanguage.ARABIC -> "تبعيات المشروع"
        AppLanguage.PORTUGUESE -> "Dependências do Projeto"
        AppLanguage.SPANISH -> "Dependencias del Proyecto"
        AppLanguage.FRENCH -> "Dépendances du Projet"
        AppLanguage.GERMAN -> "Projekt-Abhängigkeiten"
        AppLanguage.RUSSIAN -> "Зависимости проекта"
        AppLanguage.JAPANESE -> "プロジェクトの依存関係"
        AppLanguage.KOREAN -> "프로젝트 의존성"
    }
    val pyRequirementsFile: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "依赖来源"
        AppLanguage.ENGLISH -> "Dependency Source"
        AppLanguage.ARABIC -> "مصدر التبعيات"
        AppLanguage.PORTUGUESE -> "Fonte de Dependência"
        AppLanguage.SPANISH -> "Fuente de Dependencias"
        AppLanguage.FRENCH -> "Source des Dépendances"
        AppLanguage.GERMAN -> "Abhängigkeitsquelle"
        AppLanguage.RUSSIAN -> "Источник зависимостей"
        AppLanguage.JAPANESE -> "依存関係のソース"
        AppLanguage.KOREAN -> "의존성 소스"
    }
    val pyServerBuiltin: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "内置服务器"
        AppLanguage.ENGLISH -> "Built-in Server"
        AppLanguage.ARABIC -> "الخادم المدمج"
        AppLanguage.PORTUGUESE -> "Servidor Integrado"
        AppLanguage.SPANISH -> "Servidor Integrado"
        AppLanguage.FRENCH -> "Serveur Intégré"
        AppLanguage.GERMAN -> "Eingebauter Server"
        AppLanguage.RUSSIAN -> "Встроенный сервер"
        AppLanguage.JAPANESE -> "組み込みサーバー"
        AppLanguage.KOREAN -> "내장 서버"
    }

    val pyServerUvicorn: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Uvicorn (ASGI)"
        AppLanguage.ENGLISH -> "Uvicorn (ASGI)"
        AppLanguage.ARABIC -> "Uvicorn (ASGI)"
        AppLanguage.PORTUGUESE -> "Uvicorn (ASGI)"
        AppLanguage.SPANISH -> "Uvicorn (ASGI)"
        AppLanguage.FRENCH -> "Uvicorn (ASGI)"
        AppLanguage.GERMAN -> "Uvicorn (ASGI)"
        AppLanguage.RUSSIAN -> "Uvicorn (ASGI)"
        AppLanguage.JAPANESE -> "Uvicorn (ASGI)"
        AppLanguage.KOREAN -> "Uvicorn (ASGI)"
    }

    val pyWsgiModule: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "WSGI/ASGI 模块"
        AppLanguage.ENGLISH -> "WSGI/ASGI Module"
        AppLanguage.ARABIC -> "وحدة WSGI/ASGI"
        AppLanguage.PORTUGUESE -> "Módulo WSGI/ASGI"
        AppLanguage.SPANISH -> "Módulo WSGI/ASGI"
        AppLanguage.FRENCH -> "Module WSGI/ASGI"
        AppLanguage.GERMAN -> "WSGI/ASGI-Modul"
        AppLanguage.RUSSIAN -> "Модуль WSGI/ASGI"
        AppLanguage.JAPANESE -> "WSGI/ASGI モジュール"
        AppLanguage.KOREAN -> "WSGI/ASGI 모듈"
    }
    val pyWsgiModuleHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "如 myapp.wsgi:application 或 main:app"
        AppLanguage.ENGLISH -> "e.g. myapp.wsgi:application or main:app"
        AppLanguage.ARABIC -> "مثل myapp.wsgi:application أو main:app"
        AppLanguage.PORTUGUESE -> "ex. myapp.wsgi:application ou main:app"
        AppLanguage.SPANISH -> "ej. myapp.wsgi:application o main:app"
        AppLanguage.FRENCH -> "ex. myapp.wsgi:application ou main:app"
        AppLanguage.GERMAN -> "z. B. myapp.wsgi:application oder main:app"
        AppLanguage.RUSSIAN -> "напр. myapp.wsgi:application или main:app"
        AppLanguage.JAPANESE -> "例: myapp.wsgi:application または main:app"
        AppLanguage.KOREAN -> "예: myapp.wsgi:application 또는 main:app"
    }
    val pyVenvDetected: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "虚拟环境"
        AppLanguage.ENGLISH -> "Virtual Environment"
        AppLanguage.ARABIC -> "البيئة الافتراضية"
        AppLanguage.PORTUGUESE -> "Ambiente Virtual"
        AppLanguage.SPANISH -> "Entorno Virtual"
        AppLanguage.FRENCH -> "Environnement Virtuel"
        AppLanguage.GERMAN -> "Virtuelle Umgebung"
        AppLanguage.RUSSIAN -> "Виртуальное окружение"
        AppLanguage.JAPANESE -> "仮想環境"
        AppLanguage.KOREAN -> "가상 환경"
    }
    val pyVenvFound: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已检测到虚拟环境"
        AppLanguage.ENGLISH -> "Virtual environment detected"
        AppLanguage.ARABIC -> "تم اكتشاف البيئة الافتراضية"
        AppLanguage.PORTUGUESE -> "Ambiente virtual detectado"
        AppLanguage.SPANISH -> "Entorno virtual detectado"
        AppLanguage.FRENCH -> "Environnement virtuel détecté"
        AppLanguage.GERMAN -> "Virtuelle Umgebung erkannt"
        AppLanguage.RUSSIAN -> "Виртуальное окружение обнаружено"
        AppLanguage.JAPANESE -> "仮想環境を検出"
        AppLanguage.KOREAN -> "가상 환경 감지됨"
    }
    val pyVenvNotFound: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "未检测到虚拟环境"
        AppLanguage.ENGLISH -> "No virtual environment detected"
        AppLanguage.ARABIC -> "لم يتم اكتشاف بيئة افتراضية"
        AppLanguage.PORTUGUESE -> "Nenhum ambiente virtual detectado"
        AppLanguage.SPANISH -> "Ningún entorno virtual detectado"
        AppLanguage.FRENCH -> "Aucun environnement virtuel détecté"
        AppLanguage.GERMAN -> "Keine virtuelle Umgebung erkannt"
        AppLanguage.RUSSIAN -> "Виртуальное окружение не обнаружено"
        AppLanguage.JAPANESE -> "仮想環境が検出されませんでした"
        AppLanguage.KOREAN -> "가상 환경이 감지되지 않음"
    }
    val pyDjangoSettings: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Django 配置"
        AppLanguage.ENGLISH -> "Django Settings"
        AppLanguage.ARABIC -> "إعدادات Django"
        AppLanguage.PORTUGUESE -> "Configurações do Django"
        AppLanguage.SPANISH -> "Configuración de Django"
        AppLanguage.FRENCH -> "Paramètres Django"
        AppLanguage.GERMAN -> "Django-Einstellungen"
        AppLanguage.RUSSIAN -> "Настройки Django"
        AppLanguage.JAPANESE -> "Django 設定"
        AppLanguage.KOREAN -> "Django 설정"
    }
    val pyDjangoSettingsModule: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Settings 模块"
        AppLanguage.ENGLISH -> "Settings Module"
        AppLanguage.ARABIC -> "وحدة الإعدادات"
        AppLanguage.PORTUGUESE -> "Módulo de Configurações"
        AppLanguage.SPANISH -> "Módulo de Configuración"
        AppLanguage.FRENCH -> "Module de Paramètres"
        AppLanguage.GERMAN -> "Einstellungsmodul"
        AppLanguage.RUSSIAN -> "Модуль настроек"
        AppLanguage.JAPANESE -> "設定モジュール"
        AppLanguage.KOREAN -> "설정 모듈"
    }
    val pyDjangoSettingsModuleExample: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "myproject.settings"
        AppLanguage.ENGLISH -> "myproject.settings"
        AppLanguage.ARABIC -> "myproject.settings"
        AppLanguage.PORTUGUESE -> "myproject.settings"
        AppLanguage.SPANISH -> "myproject.settings"
        AppLanguage.FRENCH -> "myproject.settings"
        AppLanguage.GERMAN -> "myproject.settings"
        AppLanguage.RUSSIAN -> "myproject.settings"
        AppLanguage.JAPANESE -> "myproject.settings"
        AppLanguage.KOREAN -> "myproject.settings"
    }
    val pyDjangoStaticDir: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "静态文件目录"
        AppLanguage.ENGLISH -> "Static Files Directory"
        AppLanguage.ARABIC -> "دليل الملفات الثابتة"
        AppLanguage.PORTUGUESE -> "Diretório de Arquivos Estáticos"
        AppLanguage.SPANISH -> "Directorio de Archivos Estáticos"
        AppLanguage.FRENCH -> "Répertoire des Fichiers Statiques"
        AppLanguage.GERMAN -> "Verzeichnis für statische Dateien"
        AppLanguage.RUSSIAN -> "Каталог статических файлов"
        AppLanguage.JAPANESE -> "静的ファイルディレクトリ"
        AppLanguage.KOREAN -> "정적 파일 디렉토리"
    }
    val pyDjangoStaticDirExample: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "static"
        AppLanguage.ENGLISH -> "static"
        AppLanguage.ARABIC -> "static"
        AppLanguage.PORTUGUESE -> "static"
        AppLanguage.SPANISH -> "static"
        AppLanguage.FRENCH -> "static"
        AppLanguage.GERMAN -> "static"
        AppLanguage.RUSSIAN -> "static"
        AppLanguage.JAPANESE -> "static"
        AppLanguage.KOREAN -> "static"
    }
    val pyDjangoAllowedHosts: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "提示：ALLOWED_HOSTS 已自动设置为 ['*']"
        AppLanguage.ENGLISH -> "Tip: ALLOWED_HOSTS is auto-set to ['*']"
        AppLanguage.ARABIC -> "تلميح: تم ضبط ALLOWED_HOSTS تلقائياً على ['*']"
        AppLanguage.PORTUGUESE -> "Dica: ALLOWED_HOSTS foi definido automaticamente para ['*']"
        AppLanguage.SPANISH -> "Consejo: ALLOWED_HOSTS se configuró automáticamente a ['*']"
        AppLanguage.FRENCH -> "Astuce : ALLOWED_HOSTS est automatiquement défini sur ['*']"
        AppLanguage.GERMAN -> "Tipp: ALLOWED_HOSTS wurde automatisch auf ['*'] gesetzt"
        AppLanguage.RUSSIAN -> "Подсказка: ALLOWED_HOSTS автоматически установлен на ['*']"
        AppLanguage.JAPANESE -> "ヒント: ALLOWED_HOSTS は自動で ['*'] に設定されます"
        AppLanguage.KOREAN -> "팁: ALLOWED_HOSTS가 자동으로 ['*']로 설정됩니다"
    }
    val pyFastapiConfig: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "FastAPI 配置"
        AppLanguage.ENGLISH -> "FastAPI Settings"
        AppLanguage.ARABIC -> "إعدادات FastAPI"
        AppLanguage.PORTUGUESE -> "Configurações do FastAPI"
        AppLanguage.SPANISH -> "Configuración de FastAPI"
        AppLanguage.FRENCH -> "Paramètres FastAPI"
        AppLanguage.GERMAN -> "FastAPI-Einstellungen"
        AppLanguage.RUSSIAN -> "Настройки FastAPI"
        AppLanguage.JAPANESE -> "FastAPI 設定"
        AppLanguage.KOREAN -> "FastAPI 설정"
    }
    val pyFastapiDocsEndpoint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "API 文档端点：/docs (Swagger) · /redoc (ReDoc)"
        AppLanguage.ENGLISH -> "API docs endpoints: /docs (Swagger) · /redoc (ReDoc)"
        AppLanguage.ARABIC -> "نقاط نهاية وثائق API: /docs (Swagger) · /redoc (ReDoc)"
        AppLanguage.PORTUGUESE -> "Endpoints de docs da API: /docs (Swagger) · /redoc (ReDoc)"
        AppLanguage.SPANISH -> "Endpoints de docs de la API: /docs (Swagger) · /redoc (ReDoc)"
        AppLanguage.FRENCH -> "Endpoints de docs API : /docs (Swagger) · /redoc (ReDoc)"
        AppLanguage.GERMAN -> "API-Docs-Endpunkte: /docs (Swagger) · /redoc (ReDoc)"
        AppLanguage.RUSSIAN -> "Эндпоинты документации API: /docs (Swagger) · /redoc (ReDoc)"
        AppLanguage.JAPANESE -> "APIドキュメントエンドポイント: /docs (Swagger) · /redoc (ReDoc)"
        AppLanguage.KOREAN -> "API 문서 엔드포인트: /docs (Swagger) · /redoc (ReDoc)"
    }
    val pyFastapiAsgiHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "推荐使用 Uvicorn 作为 ASGI 服务器"
        AppLanguage.ENGLISH -> "Uvicorn recommended as ASGI server"
        AppLanguage.ARABIC -> "يوصى باستخدام Uvicorn كخادم ASGI"
        AppLanguage.PORTUGUESE -> "Uvicorn recomendado como servidor ASGI"
        AppLanguage.SPANISH -> "Uvicorn recomendado como servidor ASGI"
        AppLanguage.FRENCH -> "Uvicorn recommandé comme serveur ASGI"
        AppLanguage.GERMAN -> "Uvicorn als ASGI-Server empfohlen"
        AppLanguage.RUSSIAN -> "Рекомендуется Uvicorn как ASGI-сервер"
        AppLanguage.JAPANESE -> "ASGI サーバーとして Uvicorn を推奨"
        AppLanguage.KOREAN -> "ASGI 서버로 Uvicorn 권장"
    }

    val goHeroTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Go 服务"
        AppLanguage.ENGLISH -> "Go Service"
        AppLanguage.ARABIC -> "خدمة Go"
        AppLanguage.PORTUGUESE -> "Serviço Go"
        AppLanguage.SPANISH -> "Servicio Go"
        AppLanguage.FRENCH -> "Service Go"
        AppLanguage.GERMAN -> "Go-Service"
        AppLanguage.RUSSIAN -> "Сервис Go"
        AppLanguage.JAPANESE -> "Go サービス"
        AppLanguage.KOREAN -> "Go 서비스"
    }
    val goHeroDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "将 Go Web 服务打包为独立 Android 应用"
        AppLanguage.ENGLISH -> "Package Go Web service as standalone Android app"
        AppLanguage.ARABIC -> "تغليف خدمة Go Web كتطبيق Android مستقل"
        AppLanguage.PORTUGUESE -> "Embalar serviço Go Web como app Android autônoma"
        AppLanguage.SPANISH -> "Empaquetar servicio Go Web como app Android independiente"
        AppLanguage.FRENCH -> "Emballer un service Go Web comme application Android autonome"
        AppLanguage.GERMAN -> "Go-Webdienst als eigenständige Android-App verpacken"
        AppLanguage.RUSSIAN -> "Упаковать веб-сервис Go как самостоятельное приложение Android"
        AppLanguage.JAPANESE -> "Go Web サービスを独立した Android アプリとしてパッケージ化"
        AppLanguage.KOREAN -> "Go 웹 서비스를 독립 Android 앱으로 패키징"
    }
    fun goFrameworkTip(framework: String?): String? = when (Strings.lang) {
        AppLanguage.CHINESE -> when (framework?.lowercase()) {
            "gin" -> "Gin：已自动设置 GIN_MODE=release，路由配置适合生产环境。"
            "fiber" -> "Fiber：Android 环境下默认关闭 Prefork，启用 Fasthttp 引擎。"
            "echo" -> "Echo：已自动配置 Logger 与 Recover 中间件。"
            "chi" -> "Chi：轻量级且兼容标准库的路由器，中间件栈已就绪。"
            "net_http" -> "net/http：使用标准库服务器，已启用优雅关闭。"
            else -> null
        }
        AppLanguage.ENGLISH -> when (framework?.lowercase()) {
            "gin" -> "Gin: GIN_MODE is auto-set to release, and the router is tuned for production."
            "fiber" -> "Fiber: Prefork is disabled on Android, and the Fasthttp engine stays active."
            "echo" -> "Echo: Logger and Recover middleware are auto-configured."
            "chi" -> "Chi: Lightweight stdlib-compatible router with middleware stack ready."
            "net_http" -> "net/http: Standard library server with graceful shutdown enabled."
            else -> null
        }
        AppLanguage.ARABIC -> when (framework?.lowercase()) {
            "gin" -> "Gin: يتم ضبط GIN_MODE تلقائياً على release، ويُهيأ الموجّه للإنتاج."
            "fiber" -> "Fiber: Prefork معطّل على Android، ومحرك Fasthttp يبقى نشطاً."
            "echo" -> "Echo: تتم تهيئة وسائط Logger و Recover تلقائياً."
            "chi" -> "Chi: موجّه خفيف متوافق مع المكتبة القياسية مع مكدس وسائط جاهز."
            "net_http" -> "net/http: خادم المكتبة القياسية مع إيقاف أنيق مفعّل."
            else -> null
        }
        AppLanguage.PORTUGUESE -> when (framework?.lowercase()) {
            "gin" -> "Gin: GIN_MODE é definido automaticamente como release e o router é ajustado para produção."
            "fiber" -> "Fiber: Prefork fica desativado no Android e o motor Fasthttp permanece ativo."
            "echo" -> "Echo: middlewares Logger e Recover são configurados automaticamente."
            "chi" -> "Chi: router leve compatível com stdlib, com pilha de middleware pronta."
            "net_http" -> "net/http: servidor da biblioteca padrão com desligamento gracioso habilitado."
            else -> null
        }
        AppLanguage.SPANISH -> when (framework?.lowercase()) {
            "gin" -> "Gin: GIN_MODE se establece automáticamente en release y el router se ajusta para producción."
            "fiber" -> "Fiber: Prefork se desactiva en Android y el motor Fasthttp sigue activo."
            "echo" -> "Echo: los middlewares Logger y Recover se configuran automáticamente."
            "chi" -> "Chi: router ligero compatible con stdlib, con pila de middleware lista."
            "net_http" -> "net/http: servidor de la biblioteca estándar con apagado elegante habilitado."
            else -> null
        }
        AppLanguage.FRENCH -> when (framework?.lowercase()) {
            "gin" -> "Gin : GIN_MODE est défini automatiquement sur release et le routeur est réglé pour la production."
            "fiber" -> "Fiber : Prefork est désactivé sur Android et le moteur Fasthttp reste actif."
            "echo" -> "Echo : les middlewares Logger et Recover sont configurés automatiquement."
            "chi" -> "Chi : routeur léger compatible stdlib avec pile de middlewares prête."
            "net_http" -> "net/http : serveur de la bibliothèque standard avec arrêt gracieux activé."
            else -> null
        }
        AppLanguage.GERMAN -> when (framework?.lowercase()) {
            "gin" -> "Gin: GIN_MODE wird automatisch auf release gesetzt und der Router für Produktion optimiert."
            "fiber" -> "Fiber: Prefork ist unter Android deaktiviert, die Fasthttp-Engine bleibt aktiv."
            "echo" -> "Echo: Logger- und Recover-Middleware werden automatisch konfiguriert."
            "chi" -> "Chi: leichter stdlib-kompatibler Router mit vorbereiteter Middleware-Stack."
            "net_http" -> "net/http: Standardbibliothek-Server mit graceful shutdown."
            else -> null
        }
        AppLanguage.RUSSIAN -> when (framework?.lowercase()) {
            "gin" -> "Gin: GIN_MODE автоматически ставится в release, роутер настроен для продакшена."
            "fiber" -> "Fiber: Prefork отключён на Android, движок Fasthttp остаётся активным."
            "echo" -> "Echo: middleware Logger и Recover настраиваются автоматически."
            "chi" -> "Chi: лёгкий роутер, совместимый со stdlib, со стеком middleware."
            "net_http" -> "net/http: сервер стандартной библиотеки с graceful shutdown."
            else -> null
        }
        AppLanguage.JAPANESE -> when (framework?.lowercase()) {
            "gin" -> "Gin: GIN_MODE を自動で release に設定し、本番向けにルータを調整します。"
            "fiber" -> "Fiber: Android では Prefork を無効化し、Fasthttp エンジンを維持します。"
            "echo" -> "Echo: Logger と Recover middleware を自動設定します。"
            "chi" -> "Chi: stdlib 互換の軽量ルータ。middleware スタックを用意済みです。"
            "net_http" -> "net/http: 標準ライブラリサーバー。graceful shutdown 対応。"
            else -> null
        }
        AppLanguage.KOREAN -> when (framework?.lowercase()) {
            "gin" -> "Gin: GIN_MODE가 자동으로 release로 설정되고 라우터가 프로덕션에 맞게 조정됩니다."
            "fiber" -> "Fiber: Android에서는 Prefork가 비활성화되고 Fasthttp 엔진이 유지됩니다."
            "echo" -> "Echo: Logger와 Recover 미들웨어가 자동 구성됩니다."
            "chi" -> "Chi: stdlib 호환 경량 라우터와 준비된 미들웨어 스택."
            "net_http" -> "net/http: 표준 라이브러리 서버, graceful shutdown 지원."
            else -> null
        }
    }
    val goModuleInfo: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Go 模块信息"
        AppLanguage.ENGLISH -> "Go Module Info"
        AppLanguage.ARABIC -> "معلومات وحدة Go"
        AppLanguage.PORTUGUESE -> "Informações do Módulo Go"
        AppLanguage.SPANISH -> "Información del Módulo Go"
        AppLanguage.FRENCH -> "Infos du Module Go"
        AppLanguage.GERMAN -> "Go-Modulinfo"
        AppLanguage.RUSSIAN -> "Информация о модуле Go"
        AppLanguage.JAPANESE -> "Go モジュール情報"
        AppLanguage.KOREAN -> "Go 모듈 정보"
    }
    val goModulePath: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "模块路径"
        AppLanguage.ENGLISH -> "Module Path"
        AppLanguage.ARABIC -> "مسار الوحدة"
        AppLanguage.PORTUGUESE -> "Caminho do Módulo"
        AppLanguage.SPANISH -> "Ruta del Módulo"
        AppLanguage.FRENCH -> "Chemin du Module"
        AppLanguage.GERMAN -> "Modulpfad"
        AppLanguage.RUSSIAN -> "Путь модуля"
        AppLanguage.JAPANESE -> "モジュールパス"
        AppLanguage.KOREAN -> "모듈 경로"
    }
    val goVersion: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Go 版本"
        AppLanguage.ENGLISH -> "Go Version"
        AppLanguage.ARABIC -> "إصدار Go"
        AppLanguage.PORTUGUESE -> "Versão do Go"
        AppLanguage.SPANISH -> "Versión de Go"
        AppLanguage.FRENCH -> "Version de Go"
        AppLanguage.GERMAN -> "Go-Version"
        AppLanguage.RUSSIAN -> "Версия Go"
        AppLanguage.JAPANESE -> "Go バージョン"
        AppLanguage.KOREAN -> "Go 버전"
    }
    val goDependencyCount: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "依赖数量"
        AppLanguage.ENGLISH -> "Dependency Count"
        AppLanguage.ARABIC -> "عدد التبعيات"
        AppLanguage.PORTUGUESE -> "Contagem de Dependências"
        AppLanguage.SPANISH -> "Conteo de Dependencias"
        AppLanguage.FRENCH -> "Nombre de Dépendances"
        AppLanguage.GERMAN -> "Anzahl der Abhängigkeiten"
        AppLanguage.RUSSIAN -> "Количество зависимостей"
        AppLanguage.JAPANESE -> "依存関係数"
        AppLanguage.KOREAN -> "의존성 수"
    }
    val goBinaryDetection: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "二进制文件"
        AppLanguage.ENGLISH -> "Binary Detection"
        AppLanguage.ARABIC -> "اكتشاف الملف الثنائي"
        AppLanguage.PORTUGUESE -> "Detecção de Binário"
        AppLanguage.SPANISH -> "Detección de Binario"
        AppLanguage.FRENCH -> "Détection de Binaire"
        AppLanguage.GERMAN -> "Binärerkennung"
        AppLanguage.RUSSIAN -> "Обнаружение бинарника"
        AppLanguage.JAPANESE -> "バイナリ検出"
        AppLanguage.KOREAN -> "바이너리 감지"
    }
    val goBinaryFound: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "检测到预编译二进制文件"
        AppLanguage.ENGLISH -> "Pre-compiled binary detected"
        AppLanguage.ARABIC -> "تم اكتشاف ملف ثنائي مسبق التجميع"
        AppLanguage.PORTUGUESE -> "Binário pré-compilado detectado"
        AppLanguage.SPANISH -> "Binario precompilado detectado"
        AppLanguage.FRENCH -> "Binaire précompilé détecté"
        AppLanguage.GERMAN -> "Vorkompilierte Binärdatei erkannt"
        AppLanguage.RUSSIAN -> "Обнаружен предкомпилированный бинарник"
        AppLanguage.JAPANESE -> "プリコンパイル済みバイナリを検出"
        AppLanguage.KOREAN -> "사전 컴파일된 바이너리 감지됨"
    }
    val goBuildInAppTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "应用内构建"
        AppLanguage.ENGLISH -> "Build In App"
        AppLanguage.ARABIC -> "البناء في التطبيق"
        AppLanguage.PORTUGUESE -> "Compilar no App"
        AppLanguage.SPANISH -> "Compilar en la App"
        AppLanguage.FRENCH -> "Compiler dans l'App"
        AppLanguage.GERMAN -> "In der App kompilieren"
        AppLanguage.RUSSIAN -> "Сборка в приложении"
        AppLanguage.JAPANESE -> "アプリ内でビルド"
        AppLanguage.KOREAN -> "앱 내 빌드"
    }
    val goBuildInAppDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "用应用内置的 Go 工具链直接在手机上跑 go build。需要先在「运行时管理」里安装 Go 工具链。"
        AppLanguage.ENGLISH -> "Run go build directly on the phone using the bundled Go toolchain. Install the Go toolchain from Runtime Manager first."
        AppLanguage.ARABIC -> "شغّل go build مباشرة على الهاتف باستخدام سلسلة أدوات Go المُثبتة. ثبّت سلسلة أدوات Go من إدارة وقت التشغيل أولاً."
        AppLanguage.PORTUGUESE -> "Execute go build diretamente no celular usando a toolchain Go embutida. Instale a toolchain Go pelo Gerenciador de Runtime primeiro."
        AppLanguage.SPANISH -> "Ejecuta go build directamente en el teléfono usando la toolchain Go integrada. Instala la toolchain Go desde el Gestor de Runtime primero."
        AppLanguage.FRENCH -> "Exécutez go build directement sur le téléphone avec la toolchain Go intégrée. Installez d'abord la toolchain Go depuis le Gestionnaire de Runtime."
        AppLanguage.GERMAN -> "Führe go build direkt auf dem Telefon mit der integrierten Go-Toolchain aus. Installiere die Go-Toolchain zuerst über den Runtime-Manager."
        AppLanguage.RUSSIAN -> "Запустите go build прямо на телефоне с помощью встроенной Go-тулчейна. Сначала установите Go-тулчейн из Менеджера Runtime."
        AppLanguage.JAPANESE -> "組み込みの Go ツールチェーンでスマホ上で直接 go build を実行。先にランタイムマネージャーから Go ツールチェーンをインストールしてください。"
        AppLanguage.KOREAN -> "내장 Go 툴체인으로 폰에서 직접 go build를 실행하세요. 먼저 런타임 관리자에서 Go 툴체인을 설치하세요."
    }
    val goBuildToolchainNotReady: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Go 工具链未安装。请前往「运行时管理」安装 Go 后重试。"
        AppLanguage.ENGLISH -> "Go toolchain not installed. Install Go from Runtime Manager and retry."
        AppLanguage.ARABIC -> "سلسلة أدوات Go غير مثبتة. ثبّتها من إدارة وقت التشغيل ثم أعد المحاولة."
        AppLanguage.PORTUGUESE -> "Toolchain Go não instalada. Instale o Go pelo Gerenciador de Runtime e tente novamente."
        AppLanguage.SPANISH -> "Toolchain de Go no instalada. Instala Go desde el Gestor de Runtime e inténtalo de nuevo."
        AppLanguage.FRENCH -> "Toolchain Go non installée. Installez Go depuis le Gestionnaire de Runtime et réessayez."
        AppLanguage.GERMAN -> "Go-Toolchain nicht installiert. Installiere Go über den Runtime-Manager und versuche es erneut."
        AppLanguage.RUSSIAN -> "Go-тулчейн не установлен. Установите Go из Менеджера Runtime и повторите."
        AppLanguage.JAPANESE -> "Go ツールチェーンが未インストール。ランタイムマネージャーから Go をインストールして再試行してください。"
        AppLanguage.KOREAN -> "Go 툴체인이 설치되지 않았습니다. 런타임 관리자에서 Go를 설치하고 재시도하세요."
    }
    val goBuildButtonLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "在应用内构建"
        AppLanguage.ENGLISH -> "Build in app"
        AppLanguage.ARABIC -> "ابنِ في التطبيق"
        AppLanguage.PORTUGUESE -> "Compilar no app"
        AppLanguage.SPANISH -> "Compilar en la app"
        AppLanguage.FRENCH -> "Compiler dans l'app"
        AppLanguage.GERMAN -> "In der App kompilieren"
        AppLanguage.RUSSIAN -> "Собрать в приложении"
        AppLanguage.JAPANESE -> "アプリ内でビルド"
        AppLanguage.KOREAN -> "앱 내에서 빌드"
    }
    val goBuildInProgress: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "构建中…"
        AppLanguage.ENGLISH -> "Building…"
        AppLanguage.ARABIC -> "جارٍ البناء…"
        AppLanguage.PORTUGUESE -> "Compilando…"
        AppLanguage.SPANISH -> "Compilando…"
        AppLanguage.FRENCH -> "Compilation…"
        AppLanguage.GERMAN -> "Kompilieren…"
        AppLanguage.RUSSIAN -> "Сборка…"
        AppLanguage.JAPANESE -> "ビルド中…"
        AppLanguage.KOREAN -> "빌드 중…"
    }
    val goBuildSuccess: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "构建成功"
        AppLanguage.ENGLISH -> "Build successful"
        AppLanguage.ARABIC -> "نجح البناء"
        AppLanguage.PORTUGUESE -> "Build bem-sucedido"
        AppLanguage.SPANISH -> "Build exitosa"
        AppLanguage.FRENCH -> "Build réussi"
        AppLanguage.GERMAN -> "Build erfolgreich"
        AppLanguage.RUSSIAN -> "Сборка успешна"
        AppLanguage.JAPANESE -> "ビルド成功"
        AppLanguage.KOREAN -> "빌드 성공"
    }
    val goBuildFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "构建失败"
        AppLanguage.ENGLISH -> "Build failed"
        AppLanguage.ARABIC -> "فشل البناء"
        AppLanguage.PORTUGUESE -> "Build falhou"
        AppLanguage.SPANISH -> "Build fallida"
        AppLanguage.FRENCH -> "Build échoué"
        AppLanguage.GERMAN -> "Build fehlgeschlagen"
        AppLanguage.RUSSIAN -> "Сборка не удалась"
        AppLanguage.JAPANESE -> "ビルド失敗"
        AppLanguage.KOREAN -> "빌드 실패"
    }

    val goBuildNoSpace: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "设备存储空间不足，无法完成 Go 应用内构建。已尝试清理构建缓存；请释放存储空间后重试。"
        AppLanguage.ENGLISH -> "Not enough storage for in-app Go build. Build caches were cleaned; free up space and try again."
        AppLanguage.ARABIC -> "لا توجد مساحة تخزين كافية لبناء Go داخل التطبيق. تم تنظيف ذاكرة البناء؛ حرّر مساحة ثم أعد المحاولة."
        AppLanguage.PORTUGUESE -> "Armazenamento insuficiente para o build Go no app. Caches de build foram limpos; libere espaço e tente de novo."
        AppLanguage.SPANISH -> "Almacenamiento insuficiente para el build de Go en la app. Se limpiaron caches; libera espacio e inténtalo de nuevo."
        AppLanguage.FRENCH -> "Espace de stockage insuffisant pour le build Go intégré. Caches nettoyés ; libérez de l'espace puis réessayez."
        AppLanguage.GERMAN -> "Nicht genug Speicher für den In-App-Go-Build. Build-Caches wurden geleert; Speicher freigeben und erneut versuchen."
        AppLanguage.RUSSIAN -> "Недостаточно места для in-app сборки Go. Кэш сборки очищен; освободите место и повторите."
        AppLanguage.JAPANESE -> "アプリ内 Go ビルドに十分な空き容量がありません。ビルドキャッシュを削除しました。容量を確保して再試行してください。"
        AppLanguage.KOREAN -> "인앱 Go 빌드에 저장 공간이 부족합니다. 빌드 캐시를 정리했습니다. 공간을 확보한 뒤 다시 시도하세요."
    }

    val goBuildStreamCleaningCache: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已清理约 %d MB 的 Go 构建缓存"
        AppLanguage.ENGLISH -> "Cleared about %d MB of Go build cache"
        AppLanguage.ARABIC -> "تم مسح حوالي %d ميغابايت من ذاكرة بناء Go"
        AppLanguage.PORTUGUESE -> "Limpou cerca de %d MB do cache de build Go"
        AppLanguage.SPANISH -> "Se limpiaron unos %d MB de caché de build de Go"
        AppLanguage.FRENCH -> "Environ %d Mo de cache de build Go nettoyés"
        AppLanguage.GERMAN -> "Etwa %d MB Go-Build-Cache freigegeben"
        AppLanguage.RUSSIAN -> "Очищено около %d МБ кэша сборки Go"
        AppLanguage.JAPANESE -> "Go ビルドキャッシュを約 %d MB 削除しました"
        AppLanguage.KOREAN -> "Go 빌드 캐시 약 %d MB를 정리했습니다"
    }

    val goBuildStreamRetryAfterClean: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "清理后自动重试构建…"
        AppLanguage.ENGLISH -> "Retrying build after cache cleanup…"
        AppLanguage.ARABIC -> "إعادة محاولة البناء بعد تنظيف الذاكرة…"
        AppLanguage.PORTUGUESE -> "Tentando o build novamente após limpar o cache…"
        AppLanguage.SPANISH -> "Reintentando el build tras limpiar la caché…"
        AppLanguage.FRENCH -> "Nouvelle tentative de build après nettoyage du cache…"
        AppLanguage.GERMAN -> "Build wird nach Cache-Bereinigung erneut versucht…"
        AppLanguage.RUSSIAN -> "Повторная сборка после очистки кэша…"
        AppLanguage.JAPANESE -> "キャッシュ削除後にビルドを再試行します…"
        AppLanguage.KOREAN -> "캐시 정리 후 빌드를 다시 시도합니다…"
    }

    val goBuildStreamNoGoMod: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "未检测到 go.mod，跳过 module 下载"
        AppLanguage.ENGLISH -> "go.mod not found, skipping module download"
        AppLanguage.ARABIC -> "لم يتم العثور على go.mod، يتم تخطي تنزيل الوحدات"
        AppLanguage.PORTUGUESE -> "go.mod não encontrado, pulando download de módulos"
        AppLanguage.SPANISH -> "go.mod no encontrado, omitiendo descarga de módulos"
        AppLanguage.FRENCH -> "go.mod introuvable, téléchargement des modules ignoré"
        AppLanguage.GERMAN -> "go.mod nicht gefunden, Modul-Download übersprungen"
        AppLanguage.RUSSIAN -> "go.mod не найден, загрузка модулей пропущена"
        AppLanguage.JAPANESE -> "go.mod が見つかりません、モジュールダウンロードをスキップ"
        AppLanguage.KOREAN -> "go.mod를 찾을 수 없음, 모듈 다운로드 건너뜀"
    }
    val goBuildStreamFailedExit: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "构建失败 exit=%d"
        AppLanguage.ENGLISH -> "build failed exit=%d"
        AppLanguage.ARABIC -> "فشل البناء بالحالة %d"
        AppLanguage.PORTUGUESE -> "build falhou exit=%d"
        AppLanguage.SPANISH -> "build fallida exit=%d"
        AppLanguage.FRENCH -> "build échoué exit=%d"
        AppLanguage.GERMAN -> "build fehlgeschlagen exit=%d"
        AppLanguage.RUSSIAN -> "сборка не удалась exit=%d"
        AppLanguage.JAPANESE -> "ビルド失敗 exit=%d"
        AppLanguage.KOREAN -> "빌드 실패 exit=%d"
    }
    val goBuildStreamToolchainNotReady: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Go 工具链未就绪，请先在运行时管理界面安装"
        AppLanguage.ENGLISH -> "Go toolchain isn't ready; install it from Runtime Manager first"
        AppLanguage.ARABIC -> "سلسلة أدوات Go غير جاهزة، ثبّتها من إدارة وقت التشغيل أولاً"
        AppLanguage.PORTUGUESE -> "Toolchain Go não está pronta; instale-a pelo Gerenciador de Runtime primeiro"
        AppLanguage.SPANISH -> "La toolchain de Go no está lista; instálala desde el Gestor de Runtime primero"
        AppLanguage.FRENCH -> "La toolchain Go n'est pas prête ; installez-la depuis le Gestionnaire de Runtime d'abord"
        AppLanguage.GERMAN -> "Go-Toolchain nicht bereit; zuerst über den Runtime-Manager installieren"
        AppLanguage.RUSSIAN -> "Go-тулчейн не готов; сначала установите его из Менеджера Runtime"
        AppLanguage.JAPANESE -> "Go ツールチェーンの準備ができていません。先にランタイムマネージャーからインストールしてください"
        AppLanguage.KOREAN -> "Go 툴체인이 준비되지 않았습니다. 먼저 런타임 관리자에서 설치하세요"
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

    val buildLogAnalyzingProject: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "分析项目..."
        AppLanguage.ENGLISH -> "Analysing project..."
        AppLanguage.ARABIC -> "تحليل المشروع..."
        AppLanguage.PORTUGUESE -> "Analisando projeto..."
        AppLanguage.SPANISH -> "Analizando proyecto..."
        AppLanguage.FRENCH -> "Analyse du projet..."
        AppLanguage.GERMAN -> "Projekt wird analysiert..."
        AppLanguage.RUSSIAN -> "Анализ проекта..."
        AppLanguage.JAPANESE -> "プロジェクトを分析中..."
        AppLanguage.KOREAN -> "프로젝트 분석 중..."
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
    val buildLogPackageManagerLine: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "包管理器: %s"
        AppLanguage.ENGLISH -> "Package manager: %s"
        AppLanguage.ARABIC -> "مدير الحزم: %s"
        AppLanguage.PORTUGUESE -> "Gerenciador de pacotes: %s"
        AppLanguage.SPANISH -> "Gestor de paquetes: %s"
        AppLanguage.FRENCH -> "Gestionnaire de paquets : %s"
        AppLanguage.GERMAN -> "Paketmanager: %s"
        AppLanguage.RUSSIAN -> "Менеджер пакетов: %s"
        AppLanguage.JAPANESE -> "パッケージマネージャー: %s"
        AppLanguage.KOREAN -> "패키지 관리자: %s"
    }
    val buildLogPreparingNodeEnv: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "准备本地 Node.js 构建环境..."
        AppLanguage.ENGLISH -> "Preparing the local Node.js build environment..."
        AppLanguage.ARABIC -> "تجهيز بيئة بناء Node.js المحلية..."
        AppLanguage.PORTUGUESE -> "Preparando o ambiente de build local do Node.js..."
        AppLanguage.SPANISH -> "Preparando el entorno de build local de Node.js..."
        AppLanguage.FRENCH -> "Préparation de l'environnement de build Node.js local..."
        AppLanguage.GERMAN -> "Lokale Node.js-Build-Umgebung wird vorbereitet..."
        AppLanguage.RUSSIAN -> "Подготовка локальной среды сборки Node.js..."
        AppLanguage.JAPANESE -> "ローカル Node.js ビルド環境を準備中..."
        AppLanguage.KOREAN -> "로컬 Node.js 빌드 환경 준비 중..."
    }
    val buildLogPreparingBuildEnv: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "准备构建环境"
        AppLanguage.ENGLISH -> "Preparing build environment"
        AppLanguage.ARABIC -> "تجهيز بيئة البناء"
        AppLanguage.PORTUGUESE -> "Preparando ambiente de build"
        AppLanguage.SPANISH -> "Preparando entorno de build"
        AppLanguage.FRENCH -> "Préparation de l'environnement de build"
        AppLanguage.GERMAN -> "Build-Umgebung wird vorbereitet"
        AppLanguage.RUSSIAN -> "Подготовка среды сборки"
        AppLanguage.JAPANESE -> "ビルド環境を準備中"
        AppLanguage.KOREAN -> "빌드 환경 준비 중"
    }
    val buildLogInstallingProjectDeps: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "安装项目依赖..."
        AppLanguage.ENGLISH -> "Installing project dependencies..."
        AppLanguage.ARABIC -> "تثبيت تبعيات المشروع..."
        AppLanguage.PORTUGUESE -> "Instalando dependências do projeto..."
        AppLanguage.SPANISH -> "Instalando dependencias del proyecto..."
        AppLanguage.FRENCH -> "Installation des dépendances du projet..."
        AppLanguage.GERMAN -> "Projekt-Abhängigkeiten werden installiert..."
        AppLanguage.RUSSIAN -> "Установка зависимостей проекта..."
        AppLanguage.JAPANESE -> "プロジェクトの依存関係をインストール中..."
        AppLanguage.KOREAN -> "프로젝트 의존성 설치 중..."
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
    val buildLogRunningScriptShort: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "执行 %s"
        AppLanguage.ENGLISH -> "Running %s"
        AppLanguage.ARABIC -> "تشغيل %s"
        AppLanguage.PORTUGUESE -> "Executando %s"
        AppLanguage.SPANISH -> "Ejecutando %s"
        AppLanguage.FRENCH -> "Exécution de %s"
        AppLanguage.GERMAN -> "%s wird ausgeführt"
        AppLanguage.RUSSIAN -> "Запуск %s"
        AppLanguage.JAPANESE -> "%s を実行中"
        AppLanguage.KOREAN -> "%s 실행 중"
    }
    val buildLogBuildCompleteSummary: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "构建完成: %d 个文件, %s"
        AppLanguage.ENGLISH -> "Build complete: %d file(s), %s"
        AppLanguage.ARABIC -> "اكتمل البناء: %d ملف، %s"
        AppLanguage.PORTUGUESE -> "Build concluído: %d arquivo(s), %s"
        AppLanguage.SPANISH -> "Build completado: %d archivo(s), %s"
        AppLanguage.FRENCH -> "Build terminé : %d fichier(s), %s"
        AppLanguage.GERMAN -> "Build abgeschlossen: %d Datei(en), %s"
        AppLanguage.RUSSIAN -> "Сборка завершена: %d файл(ов), %s"
        AppLanguage.JAPANESE -> "ビルド完了: %d ファイル, %s"
        AppLanguage.KOREAN -> "빌드 완료: %d 파일, %s"
    }
    val buildLogOutputDirectory: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "输出目录: %s"
        AppLanguage.ENGLISH -> "Output directory: %s"
        AppLanguage.ARABIC -> "دليل الإخراج: %s"
        AppLanguage.PORTUGUESE -> "Diretório de saída: %s"
        AppLanguage.SPANISH -> "Directorio de salida: %s"
        AppLanguage.FRENCH -> "Répertoire de sortie : %s"
        AppLanguage.GERMAN -> "Ausgabeverzeichnis: %s"
        AppLanguage.RUSSIAN -> "Каталог вывода: %s"
        AppLanguage.JAPANESE -> "出力ディレクトリ: %s"
        AppLanguage.KOREAN -> "출력 디렉토리: %s"
    }
    val buildLogBuildFailedWithMsg: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "构建失败: %s"
        AppLanguage.ENGLISH -> "Build failed: %s"
        AppLanguage.ARABIC -> "فشل البناء: %s"
        AppLanguage.PORTUGUESE -> "Build falhou: %s"
        AppLanguage.SPANISH -> "Build fallida: %s"
        AppLanguage.FRENCH -> "Build échoué : %s"
        AppLanguage.GERMAN -> "Build fehlgeschlagen: %s"
        AppLanguage.RUSSIAN -> "Сборка не удалась: %s"
        AppLanguage.JAPANESE -> "ビルド失敗: %s"
        AppLanguage.KOREAN -> "빌드 실패: %s"
    }
    val buildLogCopyingProject: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "复制项目到本地构建工作区..."
        AppLanguage.ENGLISH -> "Copying project to the local build workspace..."
        AppLanguage.ARABIC -> "نسخ المشروع إلى مساحة عمل البناء المحلية..."
        AppLanguage.PORTUGUESE -> "Copiando projeto para o workspace de build local..."
        AppLanguage.SPANISH -> "Copiando proyecto al workspace de build local..."
        AppLanguage.FRENCH -> "Copie du projet vers l'espace de build local..."
        AppLanguage.GERMAN -> "Projekt wird in den lokalen Build-Workspace kopiert..."
        AppLanguage.RUSSIAN -> "Копирование проекта в локальную рабочую область сборки..."
        AppLanguage.JAPANESE -> "プロジェクトをローカルビルドワークスペースにコピー中..."
        AppLanguage.KOREAN -> "프로젝트를 로컬 빌드 워크스페이스로 복사 중..."
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

    val goBuildStreamGoModTidyFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "go mod tidy 失败 exit=%d"
        AppLanguage.ENGLISH -> "go mod tidy failed exit=%d"
        AppLanguage.ARABIC -> "فشل go mod tidy بالحالة %d"
        AppLanguage.PORTUGUESE -> "go mod tidy falhou exit=%d"
        AppLanguage.SPANISH -> "go mod tidy falló exit=%d"
        AppLanguage.FRENCH -> "go mod tidy échoué exit=%d"
        AppLanguage.GERMAN -> "go mod tidy fehlgeschlagen exit=%d"
        AppLanguage.RUSSIAN -> "go mod tidy не удалось exit=%d"
        AppLanguage.JAPANESE -> "go mod tidy 失敗 exit=%d"
        AppLanguage.KOREAN -> "go mod tidy 실패 exit=%d"
    }

    val localBuildComposerSourceOfficial: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "官方源"
        AppLanguage.ENGLISH -> "official source"
        AppLanguage.ARABIC -> "المصدر الرسمي"
        AppLanguage.PORTUGUESE -> "fonte oficial"
        AppLanguage.SPANISH -> "fuente oficial"
        AppLanguage.FRENCH -> "source officielle"
        AppLanguage.GERMAN -> "offizielle Quelle"
        AppLanguage.RUSSIAN -> "официальный источник"
        AppLanguage.JAPANESE -> "公式ソース"
        AppLanguage.KOREAN -> "공식 소스"
    }
    val localBuildComposerSourceMirror: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "GitHub 镜像"
        AppLanguage.ENGLISH -> "GitHub mirror"
        AppLanguage.ARABIC -> "مرآة GitHub"
        AppLanguage.PORTUGUESE -> "mirror do GitHub"
        AppLanguage.SPANISH -> "mirror de GitHub"
        AppLanguage.FRENCH -> "miroir GitHub"
        AppLanguage.GERMAN -> "GitHub-Mirror"
        AppLanguage.RUSSIAN -> "зеркало GitHub"
        AppLanguage.JAPANESE -> "GitHub ミラー"
        AppLanguage.KOREAN -> "GitHub 미러"
    }
    val localBuildDownloadComposer: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下载 composer.phar %s（%s）"
        AppLanguage.ENGLISH -> "Downloading composer.phar %s (%s)"
        AppLanguage.ARABIC -> "تنزيل composer.phar %s (%s)"
        AppLanguage.PORTUGUESE -> "Baixando composer.phar %s (%s)"
        AppLanguage.SPANISH -> "Descargando composer.phar %s (%s)"
        AppLanguage.FRENCH -> "Téléchargement de composer.phar %s (%s)"
        AppLanguage.GERMAN -> "composer.phar %s (%s) wird heruntergeladen"
        AppLanguage.RUSSIAN -> "Скачивание composer.phar %s (%s)"
        AppLanguage.JAPANESE -> "composer.phar %s (%s) をダウンロード中"
        AppLanguage.KOREAN -> "composer.phar %s (%s) 다운로드 중"
    }
    val localBuildDownloadPhp: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下载 PHP 运行时"
        AppLanguage.ENGLISH -> "Downloading PHP runtime"
        AppLanguage.ARABIC -> "تنزيل وقت تشغيل PHP"
        AppLanguage.PORTUGUESE -> "Baixando runtime do PHP"
        AppLanguage.SPANISH -> "Descargando runtime de PHP"
        AppLanguage.FRENCH -> "Téléchargement du runtime PHP"
        AppLanguage.GERMAN -> "PHP-Runtime wird heruntergeladen"
        AppLanguage.RUSSIAN -> "Скачивание runtime PHP"
        AppLanguage.JAPANESE -> "PHP ランタイムをダウンロード中"
        AppLanguage.KOREAN -> "PHP 런타임 다운로드 중"
    }
    val localBuildDownloadPython: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下载 Python 运行时"
        AppLanguage.ENGLISH -> "Downloading Python runtime"
        AppLanguage.ARABIC -> "تنزيل وقت تشغيل Python"
        AppLanguage.PORTUGUESE -> "Baixando runtime do Python"
        AppLanguage.SPANISH -> "Descargando runtime de Python"
        AppLanguage.FRENCH -> "Téléchargement du runtime Python"
        AppLanguage.GERMAN -> "Python-Runtime wird heruntergeladen"
        AppLanguage.RUSSIAN -> "Скачивание runtime Python"
        AppLanguage.JAPANESE -> "Python ランタイムをダウンロード中"
        AppLanguage.KOREAN -> "Python 런타임 다운로드 중"
    }
    val localBuildDownloadNode: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下载 Node.js 运行时"
        AppLanguage.ENGLISH -> "Downloading Node.js runtime"
        AppLanguage.ARABIC -> "تنزيل وقت تشغيل Node.js"
        AppLanguage.PORTUGUESE -> "Baixando runtime do Node.js"
        AppLanguage.SPANISH -> "Descargando runtime de Node.js"
        AppLanguage.FRENCH -> "Téléchargement du runtime Node.js"
        AppLanguage.GERMAN -> "Node.js-Runtime wird heruntergeladen"
        AppLanguage.RUSSIAN -> "Скачивание runtime Node.js"
        AppLanguage.JAPANESE -> "Node.js ランタイムをダウンロード中"
        AppLanguage.KOREAN -> "Node.js 런타임 다운로드 중"
    }
    val localBuildPreparingNodeLauncher: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "准备 node 启动器"
        AppLanguage.ENGLISH -> "Preparing the node launcher"
        AppLanguage.ARABIC -> "تجهيز مُشغّل node"
        AppLanguage.PORTUGUESE -> "Preparando o node launcher"
        AppLanguage.SPANISH -> "Preparando el node launcher"
        AppLanguage.FRENCH -> "Préparation du node launcher"
        AppLanguage.GERMAN -> "Node-Launcher wird vorbereitet"
        AppLanguage.RUSSIAN -> "Подготовка node launcher"
        AppLanguage.JAPANESE -> "node ランチャーを準備中"
        AppLanguage.KOREAN -> "node 런처 준비 중"
    }
    val localBuildInstallingNpm: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "安装 npm"
        AppLanguage.ENGLISH -> "Installing npm"
        AppLanguage.ARABIC -> "تثبيت npm"
        AppLanguage.PORTUGUESE -> "Instalando npm"
        AppLanguage.SPANISH -> "Instalando npm"
        AppLanguage.FRENCH -> "Installation de npm"
        AppLanguage.GERMAN -> "npm wird installiert"
        AppLanguage.RUSSIAN -> "Установка npm"
        AppLanguage.JAPANESE -> "npm をインストール中"
        AppLanguage.KOREAN -> "npm 설치 중"
    }
    val localBuildInstallingPnpm: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "安装 pnpm"
        AppLanguage.ENGLISH -> "Installing pnpm"
        AppLanguage.ARABIC -> "تثبيت pnpm"
        AppLanguage.PORTUGUESE -> "Instalando pnpm"
        AppLanguage.SPANISH -> "Instalando pnpm"
        AppLanguage.FRENCH -> "Installation de pnpm"
        AppLanguage.GERMAN -> "pnpm wird installiert"
        AppLanguage.RUSSIAN -> "Установка pnpm"
        AppLanguage.JAPANESE -> "pnpm をインストール中"
        AppLanguage.KOREAN -> "pnpm 설치 중"
    }
    val localBuildInstallingYarn: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "安装 yarn"
        AppLanguage.ENGLISH -> "Installing yarn"
        AppLanguage.ARABIC -> "تثبيت yarn"
        AppLanguage.PORTUGUESE -> "Instalando yarn"
        AppLanguage.SPANISH -> "Instalando yarn"
        AppLanguage.FRENCH -> "Installation de yarn"
        AppLanguage.GERMAN -> "yarn wird installiert"
        AppLanguage.RUSSIAN -> "Установка yarn"
        AppLanguage.JAPANESE -> "yarn をインストール中"
        AppLanguage.KOREAN -> "yarn 설치 중"
    }
    val composerStartInstall: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "[composer] 启动 install 流程（构建标记 %s）"
        AppLanguage.ENGLISH -> "[composer] Starting install flow (build tag %s)"
        AppLanguage.ARABIC -> "[composer] بدء تثبيت (علامة البناء %s)"
        AppLanguage.PORTUGUESE -> "[composer] Iniciando fluxo de install (build tag %s)"
        AppLanguage.SPANISH -> "[composer] Iniciando flujo de install (build tag %s)"
        AppLanguage.FRENCH -> "[composer] Démarrage du flux d'install (build tag %s)"
        AppLanguage.GERMAN -> "[composer] Install-Ablauf wird gestartet (Build-Tag %s)"
        AppLanguage.RUSSIAN -> "[composer] Запуск потока install (тег сборки %s)"
        AppLanguage.JAPANESE -> "[composer] install フローを開始(ビルドタグ %s)"
        AppLanguage.KOREAN -> "[composer] install 흐름 시작(빌드 태그 %s)"
    }
    val composerNoComposerJson: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "composer.json 不存在，跳过 composer 依赖安装"
        AppLanguage.ENGLISH -> "composer.json not found, skipping composer install"
        AppLanguage.ARABIC -> "لم يتم العثور على composer.json، يتم تخطي تثبيت composer"
        AppLanguage.PORTUGUESE -> "composer.json não encontrado, pulando composer install"
        AppLanguage.SPANISH -> "composer.json no encontrado, omitiendo composer install"
        AppLanguage.FRENCH -> "composer.json introuvable, installation composer ignorée"
        AppLanguage.GERMAN -> "composer.json nicht gefunden, composer install übersprungen"
        AppLanguage.RUSSIAN -> "composer.json не найден, composer install пропущен"
        AppLanguage.JAPANESE -> "composer.json が見つかりません、composer install をスキップ"
        AppLanguage.KOREAN -> "composer.json을 찾을 수 없음, composer install 건너뜀"
    }
    val composerCurrentPharVersion: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "[composer] 当前 phar 自报版本：%s"
        AppLanguage.ENGLISH -> "[composer] Current phar self-reported version: %s"
        AppLanguage.ARABIC -> "[composer] إصدار phar الحالي: %s"
        AppLanguage.PORTUGUESE -> "[composer] Versão atual do phar: %s"
        AppLanguage.SPANISH -> "[composer] Versión actual del phar: %s"
        AppLanguage.FRENCH -> "[composer] Version actuelle du phar : %s"
        AppLanguage.GERMAN -> "[composer] Aktuelle phar-Version: %s"
        AppLanguage.RUSSIAN -> "[composer] Текущая версия phar: %s"
        AppLanguage.JAPANESE -> "[composer] 現在の phar バージョン: %s"
        AppLanguage.KOREAN -> "[composer] 현재 phar 버전: %s"
    }
    val composerVersionUnknown: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "(无法识别)"
        AppLanguage.ENGLISH -> "(unknown)"
        AppLanguage.ARABIC -> "(غير معروف)"
        AppLanguage.PORTUGUESE -> "(desconhecido)"
        AppLanguage.SPANISH -> "(desconocido)"
        AppLanguage.FRENCH -> "(inconnu)"
        AppLanguage.GERMAN -> "(unbekannt)"
        AppLanguage.RUSSIAN -> "(неизвестно)"
        AppLanguage.JAPANESE -> "(不明)"
        AppLanguage.KOREAN -> "(알 수 없음)"
    }
    val composerUpgradingPhar: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "[composer] 升级到 %s（当前不匹配，修复已知的 256 层递归 bug）..."
        AppLanguage.ENGLISH -> "[composer] Upgrading to %s (current mismatch; fixes the known 256-level recursion bug)..."
        AppLanguage.ARABIC -> "[composer] الترقية إلى %s (إصلاح خطأ التكرار 256 المعروف)..."
        AppLanguage.PORTUGUESE -> "[composer] Atualizando para %s (incompatibilidade atual; corrige o bug de recursão de 256 níveis)..."
        AppLanguage.SPANISH -> "[composer] Actualizando a %s (incompatibilidad actual; corrige el bug de recursión de 256 niveles)..."
        AppLanguage.FRENCH -> "[composer] Mise à niveau vers %s (incompatibilité actuelle ; corrige le bug de récursion à 256 niveaux)..."
        AppLanguage.GERMAN -> "[composer] Upgrade auf %s (aktuelle Inkompatibilität; behebt den bekannten 256-Ebenen-Rekursionsbug)..."
        AppLanguage.RUSSIAN -> "[composer] Обновление до %s (текущее несоответствие; исправляет известный баг рекурсии на 256 уровней)..."
        AppLanguage.JAPANESE -> "[composer] %s にアップグレード中(現在不一致、既知の 256 レベル再帰バグを修正)..."
        AppLanguage.KOREAN -> "[composer] %s(으)로 업그레이드 중(현재 불일치, 알려진 256레벨 재귀 버그 수정)..."
    }
    val composerAutoUpgradeFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "[composer] 自动升级失败：%s（继续用现有版本尝试）"
        AppLanguage.ENGLISH -> "[composer] Auto-upgrade failed: %s (continuing with the existing version)"
        AppLanguage.ARABIC -> "[composer] فشل الترقية التلقائية: %s (المتابعة بالإصدار الحالي)"
        AppLanguage.PORTUGUESE -> "[composer] Auto-upgrade falhou: %s (continuando com a versão existente)"
        AppLanguage.SPANISH -> "[composer] Auto-upgrade fallido: %s (continuando con la versión existente)"
        AppLanguage.FRENCH -> "[composer] Auto-upgrade échoué : %s (continue avec la version existante)"
        AppLanguage.GERMAN -> "[composer] Auto-Upgrade fehlgeschlagen: %s (wird mit der bestehenden Version fortgesetzt)"
        AppLanguage.RUSSIAN -> "[composer] Автообновление не удалось: %s (продолжаем с существующей версией)"
        AppLanguage.JAPANESE -> "[composer] 自動アップグレード失敗: %s(既存バージョンで継続)"
        AppLanguage.KOREAN -> "[composer] 자동 업그레이드 실패: %s(기존 버전으로 계속)"
    }
    val composerUpgradeStatus: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "[composer] phar 是否已升级到 %s：%s"
        AppLanguage.ENGLISH -> "[composer] phar upgraded to %s: %s"
        AppLanguage.ARABIC -> "[composer] هل تمت ترقية phar إلى %s: %s"
        AppLanguage.PORTUGUESE -> "[composer] phar atualizado para %s: %s"
        AppLanguage.SPANISH -> "[composer] phar actualizado a %s: %s"
        AppLanguage.FRENCH -> "[composer] phar mis à niveau vers %s : %s"
        AppLanguage.GERMAN -> "[composer] phar auf %s aktualisiert: %s"
        AppLanguage.RUSSIAN -> "[composer] phar обновлён до %s: %s"
        AppLanguage.JAPANESE -> "[composer] phar を %s にアップグレード: %s"
        AppLanguage.KOREAN -> "[composer] phar을 %s(으)로 업그레이드: %s"
    }
    val pipNoRequirementsTxt: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "requirements.txt 不存在，跳过 pip 依赖安装"
        AppLanguage.ENGLISH -> "requirements.txt not found, skipping pip install"
        AppLanguage.ARABIC -> "لم يتم العثور على requirements.txt، يتم تخطي تثبيت pip"
        AppLanguage.PORTUGUESE -> "requirements.txt não encontrado, pulando pip install"
        AppLanguage.SPANISH -> "requirements.txt no encontrado, omitiendo pip install"
        AppLanguage.FRENCH -> "requirements.txt introuvable, installation pip ignorée"
        AppLanguage.GERMAN -> "requirements.txt nicht gefunden, pip install übersprungen"
        AppLanguage.RUSSIAN -> "requirements.txt не найден, pip install пропущен"
        AppLanguage.JAPANESE -> "requirements.txt が見つかりません、pip install をスキップ"
        AppLanguage.KOREAN -> "requirements.txt를 찾을 수 없음, pip install 건너뜀"
    }

    val nodeBuildNoExecutableScript: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "未检测到可执行的构建脚本"
        AppLanguage.ENGLISH -> "No executable build script detected"
        AppLanguage.ARABIC -> "لم يتم اكتشاف أي سكربت بناء قابل للتنفيذ"
        AppLanguage.PORTUGUESE -> "Nenhum script de build executável detectado"
        AppLanguage.SPANISH -> "Ningún script de build ejecutable detectado"
        AppLanguage.FRENCH -> "Aucun script de build exécutable détecté"
        AppLanguage.GERMAN -> "Kein ausführbares Build-Skript erkannt"
        AppLanguage.RUSSIAN -> "Исполняемый скрипт сборки не обнаружен"
        AppLanguage.JAPANESE -> "実行可能なビルドスクリプトが検出されませんでした"
        AppLanguage.KOREAN -> "실행 가능한 빌드 스크립트가 감지되지 않음"
    }
    val nodeBuildDependencyInstallFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "依赖安装失败"
        AppLanguage.ENGLISH -> "Dependency install failed"
        AppLanguage.ARABIC -> "فشل تثبيت التبعيات"
        AppLanguage.PORTUGUESE -> "Instalação de dependências falhou"
        AppLanguage.SPANISH -> "Instalación de dependencias fallida"
        AppLanguage.FRENCH -> "Installation des dépendances échouée"
        AppLanguage.GERMAN -> "Abhängigkeitsinstallation fehlgeschlagen"
        AppLanguage.RUSSIAN -> "Установка зависимостей не удалась"
        AppLanguage.JAPANESE -> "依存関係のインストール失敗"
        AppLanguage.KOREAN -> "의존성 설치 실패"
    }
    val nodeBuildScriptExecutionFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "构建脚本执行失败"
        AppLanguage.ENGLISH -> "Build script execution failed"
        AppLanguage.ARABIC -> "فشل تنفيذ سكربت البناء"
        AppLanguage.PORTUGUESE -> "Execução do script de build falhou"
        AppLanguage.SPANISH -> "Ejecución del script de build fallida"
        AppLanguage.FRENCH -> "Exécution du script de build échouée"
        AppLanguage.GERMAN -> "Ausführung des Build-Skripts fehlgeschlagen"
        AppLanguage.RUSSIAN -> "Выполнение скрипта сборки не удалось"
        AppLanguage.JAPANESE -> "ビルドスクリプトの実行失敗"
        AppLanguage.KOREAN -> "빌드 스크립트 실행 실패"
    }
    val nodeBuildCompletedNoOutput: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "构建完成，但未找到输出目录: %s"
        AppLanguage.ENGLISH -> "Build completed but the output directory wasn't found: %s"
        AppLanguage.ARABIC -> "اكتمل البناء لكن لم يتم العثور على دليل الإخراج: %s"
        AppLanguage.PORTUGUESE -> "Build concluído mas o diretório de saída não foi encontrado: %s"
        AppLanguage.SPANISH -> "Build completado pero el directorio de salida no fue encontrado: %s"
        AppLanguage.FRENCH -> "Build terminé mais le répertoire de sortie est introuvable : %s"
        AppLanguage.GERMAN -> "Build abgeschlossen, aber das Ausgabeverzeichnis wurde nicht gefunden: %s"
        AppLanguage.RUSSIAN -> "Сборка завершена, но каталог вывода не найден: %s"
        AppLanguage.JAPANESE -> "ビルド完了しましたが出力ディレクトリが見つかりません: %s"
        AppLanguage.KOREAN -> "빌드는 완료되었으나 출력 디렉토리를 찾을 수 없습니다: %s"
    }

    val nodeRuntimeDownloadFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Node.js 运行时下载失败"
        AppLanguage.ENGLISH -> "Node.js runtime download failed"
        AppLanguage.ARABIC -> "فشل تنزيل وقت تشغيل Node.js"
        AppLanguage.PORTUGUESE -> "Download do runtime do Node.js falhou"
        AppLanguage.SPANISH -> "Descarga del runtime de Node.js fallida"
        AppLanguage.FRENCH -> "Téléchargement du runtime Node.js échoué"
        AppLanguage.GERMAN -> "Download der Node.js-Runtime fehlgeschlagen"
        AppLanguage.RUSSIAN -> "Скачивание runtime Node.js не удалось"
        AppLanguage.JAPANESE -> "Node.js ランタイムのダウンロード失敗"
        AppLanguage.KOREAN -> "Node.js 런타임 다운로드 실패"
    }
    val phpRuntimeDownloadFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "PHP 运行时下载失败。请检查网络连接后重试。"
        AppLanguage.ENGLISH -> "PHP runtime download failed. Check your network connection and retry."
        AppLanguage.ARABIC -> "فشل تنزيل وقت تشغيل PHP. تحقق من اتصال الشبكة وحاول مجددًا."
        AppLanguage.PORTUGUESE -> "Download do runtime do PHP falhou. Verifique a conexão de rede e tente novamente."
        AppLanguage.SPANISH -> "Descarga del runtime de PHP fallida. Verifica la conexión de red e inténtalo de nuevo."
        AppLanguage.FRENCH -> "Téléchargement du runtime PHP échoué. Vérifiez la connexion réseau et réessayez."
        AppLanguage.GERMAN -> "Download der PHP-Runtime fehlgeschlagen. Netzwerkverbindung prüfen und erneut versuchen."
        AppLanguage.RUSSIAN -> "Скачивание runtime PHP не удалось. Проверьте сетевое подключение и повторите."
        AppLanguage.JAPANESE -> "PHP ランタイムのダウンロード失敗。ネットワーク接続を確認して再試行してください。"
        AppLanguage.KOREAN -> "PHP 런타임 다운로드 실패. 네트워크 연결을 확인하고 재시도하세요."
    }
    val pythonRuntimeDownloadFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Python 运行时下载失败"
        AppLanguage.ENGLISH -> "Python runtime download failed"
        AppLanguage.ARABIC -> "فشل تنزيل وقت تشغيل Python"
        AppLanguage.PORTUGUESE -> "Download do runtime do Python falhou"
        AppLanguage.SPANISH -> "Descarga del runtime de Python fallida"
        AppLanguage.FRENCH -> "Téléchargement du runtime Python échoué"
        AppLanguage.GERMAN -> "Download der Python-Runtime fehlgeschlagen"
        AppLanguage.RUSSIAN -> "Скачивание runtime Python не удалось"
        AppLanguage.JAPANESE -> "Python ランタイムのダウンロード失敗"
        AppLanguage.KOREAN -> "Python 런타임 다운로드 실패"
    }
    val pyRuntimeNoMuslForAbi: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "当前 ABI (%s) 暂无可用的 musl linker"
        AppLanguage.ENGLISH -> "No musl linker available for the current ABI (%s)"
        AppLanguage.ARABIC -> "لا يتوفر رابط musl للبنية الحالية (%s)"
        AppLanguage.PORTUGUESE -> "Nenhum musl linker disponível para a ABI atual (%s)"
        AppLanguage.SPANISH -> "Ningún musl linker disponible para la ABI actual (%s)"
        AppLanguage.FRENCH -> "Aucun musl linker disponible pour l'ABI actuelle (%s)"
        AppLanguage.GERMAN -> "Kein musl-Linker für die aktuelle ABI verfügbar (%s)"
        AppLanguage.RUSSIAN -> "Нет musl-линкера для текущего ABI (%s)"
        AppLanguage.JAPANESE -> "現在の ABI (%s) で利用可能な musl リンカーがありません"
        AppLanguage.KOREAN -> "현재 ABI (%s)에 사용 가능한 musl 링커가 없습니다"
    }
    val pyRuntimeDownloadIncomplete: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Python 运行时下载不完整：缺少 Python 二进制或 musl linker"
        AppLanguage.ENGLISH -> "Python runtime download incomplete: missing Python binary or musl linker"
        AppLanguage.ARABIC -> "تنزيل بيئة تشغيل Python غير مكتمل: ملف Python التنفيذي أو رابط musl مفقود"
        AppLanguage.PORTUGUESE -> "Download do runtime do Python incompleto: faltando binário do Python ou musl linker"
        AppLanguage.SPANISH -> "Descarga del runtime de Python incompleta: falta el binario de Python o el musl linker"
        AppLanguage.FRENCH -> "Téléchargement du runtime Python incomplet : binaire Python ou musl linker manquant"
        AppLanguage.GERMAN -> "Python-Runtime-Download unvollständig: Python-Binary oder musl-Linker fehlt"
        AppLanguage.RUSSIAN -> "Скачивание runtime Python неполное: отсутствует бинарник Python или musl-линкер"
        AppLanguage.JAPANESE -> "Python ランタイムのダウンロードが不完全: Python バイナリまたは musl リンカーが不足"
        AppLanguage.KOREAN -> "Python 런타임 다운로드 불완전: Python 바이너리 또는 musl 링커 누락"
    }
    val pyDepsAlreadyReady: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "依赖已就绪，跳过安装"
        AppLanguage.ENGLISH -> "Dependencies ready, skipping install"
        AppLanguage.ARABIC -> "التبعيات جاهزة، يتم تخطي التثبيت"
        AppLanguage.PORTUGUESE -> "Dependências prontas, pulando instalação"
        AppLanguage.SPANISH -> "Dependencias listas, omitiendo instalación"
        AppLanguage.FRENCH -> "Dépendances prêtes, installation ignorée"
        AppLanguage.GERMAN -> "Abhängigkeiten bereit, Installation übersprungen"
        AppLanguage.RUSSIAN -> "Зависимости готовы, установка пропущена"
        AppLanguage.JAPANESE -> "依存関係の準備完了、インストールをスキップ"
        AppLanguage.KOREAN -> "의존성 준비 완료, 설치 건너뜀"
    }
    val pyDepsInstalling: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "正在安装 Python 依赖..."
        AppLanguage.ENGLISH -> "Installing Python dependencies..."
        AppLanguage.ARABIC -> "جارٍ تثبيت تبعيات Python..."
        AppLanguage.PORTUGUESE -> "Instalando dependências do Python..."
        AppLanguage.SPANISH -> "Instalando dependencias de Python..."
        AppLanguage.FRENCH -> "Installation des dépendances Python..."
        AppLanguage.GERMAN -> "Python-Abhängigkeiten werden installiert..."
        AppLanguage.RUSSIAN -> "Установка зависимостей Python..."
        AppLanguage.JAPANESE -> "Python の依存関係をインストール中..."
        AppLanguage.KOREAN -> "Python 의존성 설치 중..."
    }
    val pyDepsBuilderMissingMusl: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "本机构建器缺少可执行 musl linker，无法预安装依赖"
        AppLanguage.ENGLISH -> "The builder is missing an executable musl linker; cannot pre-install dependencies"
        AppLanguage.ARABIC -> "أداة البناء تفتقر إلى رابط musl قابل للتنفيذ؛ يتعذّر تثبيت التبعيات مسبقًا"
        AppLanguage.PORTUGUESE -> "O builder não tem um musl linker executável; não é possível pré-instalar dependências"
        AppLanguage.SPANISH -> "El builder no tiene un musl linker ejecutable; no se pueden preinstalar dependencias"
        AppLanguage.FRENCH -> "Le builder n'a pas de musl linker exécutable ; impossible de pré-installer les dépendances"
        AppLanguage.GERMAN -> "Dem Builder fehlt ein ausführbarer musl-Linker; Abhängigkeiten können nicht vorinstalliert werden"
        AppLanguage.RUSSIAN -> "У сборщика нет исполняемого musl-линкера; нельзя предустановить зависимости"
        AppLanguage.JAPANESE -> "ビルダーに実行可能な musl リンカーがありません。依存関係を事前インストールできません"
        AppLanguage.KOREAN -> "빌더에 실행 가능한 musl 링커가 없습니다. 의존성을 사전 설치할 수 없습니다"
    }
    val pyDepsInstallComplete: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "依赖安装完成"
        AppLanguage.ENGLISH -> "Dependencies installed"
        AppLanguage.ARABIC -> "اكتمل تثبيت التبعيات"
        AppLanguage.PORTUGUESE -> "Dependências instaladas"
        AppLanguage.SPANISH -> "Dependencias instaladas"
        AppLanguage.FRENCH -> "Dépendances installées"
        AppLanguage.GERMAN -> "Abhängigkeiten installiert"
        AppLanguage.RUSSIAN -> "Зависимости установлены"
        AppLanguage.JAPANESE -> "依存関係をインストール済み"
        AppLanguage.KOREAN -> "의존성 설치됨"
    }
    val pyDepsInstallFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "依赖安装失败"
        AppLanguage.ENGLISH -> "Dependency install failed"
        AppLanguage.ARABIC -> "فشل تثبيت التبعيات"
        AppLanguage.PORTUGUESE -> "Instalação de dependências falhou"
        AppLanguage.SPANISH -> "Instalación de dependencias fallida"
        AppLanguage.FRENCH -> "Installation des dépendances échouée"
        AppLanguage.GERMAN -> "Abhängigkeitsinstallation fehlgeschlagen"
        AppLanguage.RUSSIAN -> "Установка зависимостей не удалась"
        AppLanguage.JAPANESE -> "依存関係のインストール失敗"
        AppLanguage.KOREAN -> "의존성 설치 실패"
    }
    val pyDepsInstallException: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "安装异常: %s"
        AppLanguage.ENGLISH -> "Install error: %s"
        AppLanguage.ARABIC -> "خطأ في التثبيت: %s"
        AppLanguage.PORTUGUESE -> "Erro de instalação: %s"
        AppLanguage.SPANISH -> "Error de instalación: %s"
        AppLanguage.FRENCH -> "Erreur d'installation : %s"
        AppLanguage.GERMAN -> "Installationsfehler: %s"
        AppLanguage.RUSSIAN -> "Ошибка установки: %s"
        AppLanguage.JAPANESE -> "インストールエラー: %s"
        AppLanguage.KOREAN -> "설치 오류: %s"
    }
    val pyDepsAdjustedIncompatible: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已自动调整 Android 不兼容依赖"
        AppLanguage.ENGLISH -> "Automatically adjusted Android-incompatible dependencies"
        AppLanguage.ARABIC -> "تم تعديل التبعيات غير المتوافقة مع Android تلقائيًا"
        AppLanguage.PORTUGUESE -> "Dependências incompatíveis com Android ajustadas automaticamente"
        AppLanguage.SPANISH -> "Dependencias incompatibles con Android ajustadas automáticamente"
        AppLanguage.FRENCH -> "Dépendances incompatibles avec Android ajustées automatiquement"
        AppLanguage.GERMAN -> "Android-inkompatible Abhängigkeiten automatisch angepasst"
        AppLanguage.RUSSIAN -> "Android-несовместимые зависимости автоматически скорректированы"
        AppLanguage.JAPANESE -> "Android 非互換の依存関係を自動調整しました"
        AppLanguage.KOREAN -> "Android 비호환 의존성을 자동 조정했습니다"
    }
    val pyDepsRetrying: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "正在重试依赖安装..."
        AppLanguage.ENGLISH -> "Retrying dependency install..."
        AppLanguage.ARABIC -> "جارٍ إعادة محاولة تثبيت التبعيات..."
        AppLanguage.PORTUGUESE -> "Tentando novamente a instalação de dependências..."
        AppLanguage.SPANISH -> "Reintentando la instalación de dependencias..."
        AppLanguage.FRENCH -> "Nouvelle tentative d'installation des dépendances..."
        AppLanguage.GERMAN -> "Abhängigkeitsinstallation wird erneut versucht..."
        AppLanguage.RUSSIAN -> "Повторная установка зависимостей..."
        AppLanguage.JAPANESE -> "依存関係のインストールを再試行中..."
        AppLanguage.KOREAN -> "의존성 설치 재시도 중..."
    }
    val pyDepsInstallTimeout: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "依赖安装超时 (%d秒)"
        AppLanguage.ENGLISH -> "Dependency install timed out (%ds)"
        AppLanguage.ARABIC -> "انتهت مهلة تثبيت التبعيات (%d ثانية)"
        AppLanguage.PORTUGUESE -> "Instalação de dependências expirou (%ds)"
        AppLanguage.SPANISH -> "Instalación de dependencias agotó el tiempo (%ds)"
        AppLanguage.FRENCH -> "L'installation des dépendances a expiré (%ds)"
        AppLanguage.GERMAN -> "Abhängigkeitsinstallation abgelaufen (%ds)"
        AppLanguage.RUSSIAN -> "Время установки зависимостей истекло (%ds)"
        AppLanguage.JAPANESE -> "依存関係のインストールがタイムアウト (%ds)"
        AppLanguage.KOREAN -> "의존성 설치 시간 초과 (%ds)"
    }
    val pyDepsInstallFailedCode: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "依赖安装失败 (exitCode=%d)"
        AppLanguage.ENGLISH -> "Dependency install failed (exitCode=%d)"
        AppLanguage.ARABIC -> "فشل تثبيت التبعيات (exitCode=%d)"
        AppLanguage.PORTUGUESE -> "Instalação de dependências falhou (exitCode=%d)"
        AppLanguage.SPANISH -> "Instalación de dependencias fallida (exitCode=%d)"
        AppLanguage.FRENCH -> "Installation des dépendances échouée (exitCode=%d)"
        AppLanguage.GERMAN -> "Abhängigkeitsinstallation fehlgeschlagen (exitCode=%d)"
        AppLanguage.RUSSIAN -> "Установка зависимостей не удалась (exitCode=%d)"
        AppLanguage.JAPANESE -> "依存関係のインストール失敗 (exitCode=%d)"
        AppLanguage.KOREAN -> "의존성 설치 실패 (exitCode=%d)"
    }
    val pyDepsNoWheelHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "软件包 \"%s\" 没有适用于本机 Python %s 的预编译 wheel，且设备端 Python 无法从源码编译。请尝试升级该软件包或移除版本锁定。"
        AppLanguage.ENGLISH -> "Package \"%s\" has no prebuilt wheel for Python %s on this device, and the on-device Python cannot compile packages from source. Try upgrading the package or removing its version pin."
        AppLanguage.ARABIC -> "لا توجد حزمة wheel جاهزة للحزمة \"%s\" لإصدار Python %s على هذا الجهاز، ولا يمكن لبيئة Python على الجهاز تجميع الحزم من المصدر. جرّب ترقية الحزمة أو إزالة تثبيت الإصدار."
        AppLanguage.PORTUGUESE -> "O pacote \"%s\" não tem wheel pré-compilada para Python %s neste dispositivo, e o Python no dispositivo não consegue compilar pacotes a partir do código-fonte. Tente atualizar o pacote ou remover a fixação de versão."
        AppLanguage.SPANISH -> "El paquete \"%s\" no tiene wheel precompilada para Python %s en este dispositivo, y el Python del dispositivo no puede compilar paquetes desde el código fuente. Intenta actualizar el paquete o eliminar la fijación de versión."
        AppLanguage.FRENCH -> "Le paquet « %s » n'a pas de wheel précompilée pour Python %s sur cet appareil, et le Python de l'appareil ne peut pas compiler les paquets depuis le code source. Essayez de mettre à jour le paquet ou de retirer l'épinglage de version."
        AppLanguage.GERMAN -> "Für Paket \"%s\" gibt es kein vorgefertigtes Wheel für Python %s auf diesem Gerät, und das Python auf dem Gerät kann Pakete nicht aus dem Quellcode kompilieren. Versuchen Sie, das Paket zu aktualisieren oder die Versionsfestlegung zu entfernen."
        AppLanguage.RUSSIAN -> "Для пакета «%s» нет готового wheel для Python %s на этом устройстве, а Python на устройстве не может компилировать пакеты из исходного кода. Попробуйте обновить пакет или убрать фиксацию версии."
        AppLanguage.JAPANESE -> "このデバイスの Python %s 向けにパッケージ「%s」のプリビルド wheel がなく、デバイス上の Python はソースからのコンパイルができません。パッケージをアップグレードするか、バージョン固定を解除してみてください。"
        AppLanguage.KOREAN -> "이 기기의 Python %s용 패키지 \"%s\" 사전 빌드 wheel이 없으며, 기기 내 Python은 소스에서 패키지를 컴파일할 수 없습니다. 패키지를 업그레이드하거나 버전 고정을 제거해 보세요."
    }
    val pyDepsSourceBuildHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "本机 Python %s 没有可用的预编译 wheel，源码构建需要编译器，而设备端没有编译器。请尝试升级软件包或移除版本锁定。"
        AppLanguage.ENGLISH -> "No prebuilt wheel is available for Python %s on this device, and source builds need a compiler that is not available on-device. Try upgrading the package or removing the version pin."
        AppLanguage.ARABIC -> "لا توجد حزمة wheel جاهزة لإصدار Python %s على هذا الجهاز، ويتطلب البناء من المصدر مترجمًا غير متوفر على الجهاز. جرّب ترقية الحزمة أو إزالة تثبيت الإصدار."
        AppLanguage.PORTUGUESE -> "Não há wheel pré-compilada disponível para Python %s neste dispositivo, e a compilação a partir do código-fonte exige um compilador indisponível no dispositivo. Tente atualizar o pacote ou remover a fixação de versão."
        AppLanguage.SPANISH -> "No hay wheel precompilada disponible para Python %s en este dispositivo, y compilar desde el código fuente requiere un compilador no disponible en el dispositivo. Intenta actualizar el paquete o eliminar la fijación de versión."
        AppLanguage.FRENCH -> "Aucune wheel précompilée n'est disponible pour Python %s sur cet appareil, et la compilation depuis le code source nécessite un compilateur indisponible sur l'appareil. Essayez de mettre à jour le paquet ou de retirer l'épinglage de version."
        AppLanguage.GERMAN -> "Für Python %s ist auf diesem Gerät kein vorgefertigtes Wheel verfügbar, und Quellcode-Builds benötigen einen Compiler, der auf dem Gerät nicht vorhanden ist. Versuchen Sie, das Paket zu aktualisieren oder die Versionsfestlegung zu entfernen."
        AppLanguage.RUSSIAN -> "Нет готового wheel для Python %s на этом устройстве, а сборка из исходного кода требует компилятор, который недоступен на устройстве. Попробуйте обновить пакет или убрать фиксацию версии."
        AppLanguage.JAPANESE -> "このデバイスの Python %s 向けにプリビルド wheel がなく、ソースビルドにはデバイス上にないコンパイラが必要です。パッケージをアップグレードするか、バージョン固定を解除してみてください。"
        AppLanguage.KOREAN -> "이 기기의 Python %s용 사전 빌드 wheel이 없으며, 소스 빌드에는 기기에 없는 컴파일러가 필요합니다. 패키지를 업그레이드하거나 버전 고정을 제거해 보세요."
    }
    val pyDepsKilledBySignal: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "依赖安装进程被系统终止(信号 %d / %s)"
        AppLanguage.ENGLISH -> "The dependency install process was killed by the system (signal %d / %s)"
        AppLanguage.ARABIC -> "تم إنهاء عملية تثبيت التبعيات بواسطة النظام (الإشارة %d / %s)"
        AppLanguage.PORTUGUESE -> "O processo de instalação de dependências foi encerrado pelo sistema (sinal %d / %s)"
        AppLanguage.SPANISH -> "El proceso de instalación de dependencias fue terminado por el sistema (señal %d / %s)"
        AppLanguage.FRENCH -> "Le processus d'installation des dépendances a été tué par le système (signal %d / %s)"
        AppLanguage.GERMAN -> "Der Abhängigkeitsinstallationsprozess wurde vom System beendet (Signal %d / %s)"
        AppLanguage.RUSSIAN -> "Процесс установки зависимостей был завершён системой (сигнал %d / %s)"
        AppLanguage.JAPANESE -> "依存関係のインストールプロセスがシステムによって強制終了されました (シグナル %d / %s)"
        AppLanguage.KOREAN -> "의존성 설치 프로세스가 시스템에 의해 종료되었습니다 (시그널 %d / %s)"
    }
    val pyDepsSeccompHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "内置 Python %s 运行时被本 Android 版本的系统调用过滤器(SIGSYS)终止，这是系统层面的不兼容：Python 功能需要 Android 9.0 或更高版本。"
        AppLanguage.ENGLISH -> "The bundled Python %s runtime was killed by this Android version's system-call filter (SIGSYS). This is an OS-level incompatibility: Python features require Android 9.0 or newer."
        AppLanguage.ARABIC -> "تم إنهاء بيئة Python %s المدمجة بواسطة مرشح استدعاءات النظام في إصدار Android هذا (SIGSYS). هذا تعارض على مستوى النظام: تتطلب ميزات Python نظام Android 9.0 أو أحدث."
        AppLanguage.PORTUGUESE -> "O runtime Python %s integrado foi encerrado pelo filtro de chamadas de sistema desta versão do Android (SIGSYS). Esta é uma incompatibilidade no nível do sistema: os recursos de Python exigem o Android 9.0 ou mais recente."
        AppLanguage.SPANISH -> "El runtime de Python %s incluido fue terminado por el filtro de llamadas al sistema de esta versión de Android (SIGSYS). Es una incompatibilidad a nivel del sistema operativo: las funciones de Python requieren Android 9.0 o superior."
        AppLanguage.FRENCH -> "L'environnement Python %s intégré a été tué par le filtre d'appels système de cette version d'Android (SIGSYS). Il s'agit d'une incompatibilité au niveau du système : les fonctionnalités Python nécessitent Android 9.0 ou plus récent."
        AppLanguage.GERMAN -> "Die integrierte Python-%s-Laufzeit wurde vom Systemaufruf-Filter dieser Android-Version beendet (SIGSYS). Dies ist eine Inkompatibilität auf Betriebssystemebene: Python-Funktionen erfordern Android 9.0 oder neuer."
        AppLanguage.RUSSIAN -> "Встроенная среда Python %s была завершена фильтром системных вызовов этой версии Android (SIGSYS). Это несовместимость на уровне ОС: функции Python требуют Android 9.0 или новее."
        AppLanguage.JAPANESE -> "内蔵の Python %s ランタイムが、この Android バージョンのシステムコールフィルター (SIGSYS) によって強制終了されました。これは OS レベルの非互換性であり、Python 機能には Android 9.0 以降が必要です。"
        AppLanguage.KOREAN -> "내장된 Python %s 런타임이 이 Android 버전의 시스템 콜 필터(SIGSYS)에 의해 종료되었습니다. 이는 OS 수준의 비호환성이며, Python 기능에는 Android 9.0 이상이 필요합니다."
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
    val pyExtractNoBinary: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "解压后未找到 Python 二进制"
        AppLanguage.ENGLISH -> "No Python binary found after extraction"
        AppLanguage.ARABIC -> "لم يتم العثور على ملف Python التنفيذي بعد فك الضغط"
        AppLanguage.PORTUGUESE -> "Nenhum binário do Python encontrado após extração"
        AppLanguage.SPANISH -> "Ningún binario de Python encontrado tras la extracción"
        AppLanguage.FRENCH -> "Aucun binaire Python trouvé après extraction"
        AppLanguage.GERMAN -> "Kein Python-Binary nach dem Entpacken gefunden"
        AppLanguage.RUSSIAN -> "Бинарник Python не найден после распаковки"
        AppLanguage.JAPANESE -> "展開後に Python バイナリが見つかりません"
        AppLanguage.KOREAN -> "압축 해제 후 Python 바이너리를 찾을 수 없음"
    }
    val pyExtractFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "解压 Python 失败: %s"
        AppLanguage.ENGLISH -> "Failed to extract Python: %s"
        AppLanguage.ARABIC -> "فشل فك ضغط Python: %s"
        AppLanguage.PORTUGUESE -> "Falha ao extrair Python: %s"
        AppLanguage.SPANISH -> "Error al extraer Python: %s"
        AppLanguage.FRENCH -> "Échec de l'extraction de Python : %s"
        AppLanguage.GERMAN -> "Python entpacken fehlgeschlagen: %s"
        AppLanguage.RUSSIAN -> "Не удалось распаковать Python: %s"
        AppLanguage.JAPANESE -> "Python の展開に失敗: %s"
        AppLanguage.KOREAN -> "Python 압축 해제 실패: %s"
    }
    val pyMuslNotInAlpineApk: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Alpine APK 中未找到 %s"
        AppLanguage.ENGLISH -> "%s not found in the Alpine APK"
        AppLanguage.ARABIC -> "لم يتم العثور على %s في حزمة Alpine"
        AppLanguage.PORTUGUESE -> "%s não encontrado no APK do Alpine"
        AppLanguage.SPANISH -> "%s no encontrado en el APK de Alpine"
        AppLanguage.FRENCH -> "%s introuvable dans l'APK Alpine"
        AppLanguage.GERMAN -> "%s nicht im Alpine-APK gefunden"
        AppLanguage.RUSSIAN -> "%s не найден в Alpine APK"
        AppLanguage.JAPANESE -> "Alpine APK に %s が見つかりません"
        AppLanguage.KOREAN -> "Alpine APK에서 %s을(를) 찾을 수 없음"
    }
    val pyMuslDownloadFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下载 musl linker 失败: %s"
        AppLanguage.ENGLISH -> "Failed to download musl linker: %s"
        AppLanguage.ARABIC -> "فشل تنزيل رابط musl: %s"
        AppLanguage.PORTUGUESE -> "Falha ao baixar musl linker: %s"
        AppLanguage.SPANISH -> "Error al descargar el musl linker: %s"
        AppLanguage.FRENCH -> "Échec du téléchargement du musl linker : %s"
        AppLanguage.GERMAN -> "musl-Linker-Download fehlgeschlagen: %s"
        AppLanguage.RUSSIAN -> "Не удалось скачать musl-линкер: %s"
        AppLanguage.JAPANESE -> "musl リンカーのダウンロード失敗: %s"
        AppLanguage.KOREAN -> "musl 링커 다운로드 실패: %s"
    }
    val nodeLauncherNotPackaged: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "node 启动器未随 APK 打包: %s"
        AppLanguage.ENGLISH -> "Node launcher wasn't packaged with the APK: %s"
        AppLanguage.ARABIC -> "مُشغّل node لم يُحزَّم مع APK: %s"
        AppLanguage.PORTUGUESE -> "Node launcher não foi empacotado com o APK: %s"
        AppLanguage.SPANISH -> "Node launcher no fue empaquetado con el APK: %s"
        AppLanguage.FRENCH -> "Node launcher n'a pas été empaqueté avec l'APK : %s"
        AppLanguage.GERMAN -> "Node-Launcher wurde nicht mit dem APK gepackt: %s"
        AppLanguage.RUSSIAN -> "Node launcher не упакован с APK: %s"
        AppLanguage.JAPANESE -> "node ランチャーが APK に同梱されていません: %s"
        AppLanguage.KOREAN -> "node 런처가 APK에 패키징되지 않음: %s"
    }
    val composerPhpRuntimeNotReady: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "PHP 运行时未就绪"
        AppLanguage.ENGLISH -> "PHP runtime isn't ready"
        AppLanguage.ARABIC -> "وقت تشغيل PHP غير جاهز"
        AppLanguage.PORTUGUESE -> "Runtime do PHP não está pronto"
        AppLanguage.SPANISH -> "El runtime de PHP no está listo"
        AppLanguage.FRENCH -> "Le runtime PHP n'est pas prêt"
        AppLanguage.GERMAN -> "PHP-Runtime ist nicht bereit"
        AppLanguage.RUSSIAN -> "Runtime PHP не готов"
        AppLanguage.JAPANESE -> "PHP ランタイムの準備ができていません"
        AppLanguage.KOREAN -> "PHP 런타임이 준비되지 않았습니다"
    }
    val composerPharNotInstalled: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "composer.phar 未安装，请先在「本地构建环境」里安装 Composer"
        AppLanguage.ENGLISH -> "composer.phar isn't installed; install Composer from \"Local Build Environment\" first"
        AppLanguage.ARABIC -> "composer.phar غير مثبّت؛ ثبّت Composer من «بيئة البناء المحلية» أولاً"
        AppLanguage.PORTUGUESE -> "composer.phar não instalado; instale o Composer pelo \"Ambiente de Build Local\" primeiro"
        AppLanguage.SPANISH -> "composer.phar no instalado; instala Composer desde el \"Entorno de Build Local\" primero"
        AppLanguage.FRENCH -> "composer.phar n'est pas installé ; installez Composer depuis « Environnement de Build Local » d'abord"
        AppLanguage.GERMAN -> "composer.phar nicht installiert; installiere Composer zuerst über „Lokale Build-Umgebung“"
        AppLanguage.RUSSIAN -> "composer.phar не установлен; сначала установите Composer из «Локальной среды сборки»"
        AppLanguage.JAPANESE -> "composer.phar がインストールされていません。先に「ローカルビルド環境」から Composer をインストールしてください"
        AppLanguage.KOREAN -> "composer.phar가 설치되지 않았습니다. 먼저 \"로컬 빌드 환경\"에서 Composer를 설치하세요"
    }
    val goTargetArch: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "目标架构"
        AppLanguage.ENGLISH -> "Target Architecture"
        AppLanguage.ARABIC -> "البنية المستهدفة"
        AppLanguage.PORTUGUESE -> "Arquitetura de Destino"
        AppLanguage.SPANISH -> "Arquitectura de Destino"
        AppLanguage.FRENCH -> "Architecture Cible"
        AppLanguage.GERMAN -> "Zielarchitektur"
        AppLanguage.RUSSIAN -> "Целевая архитектура"
        AppLanguage.JAPANESE -> "ターゲットアーキテクチャ"
        AppLanguage.KOREAN -> "대상 아키텍처"
    }
    val goStaticFiles: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "静态文件目录"
        AppLanguage.ENGLISH -> "Static Files Directory"
        AppLanguage.ARABIC -> "دليل الملفات الثابتة"
        AppLanguage.PORTUGUESE -> "Diretório de Arquivos Estáticos"
        AppLanguage.SPANISH -> "Directorio de Archivos Estáticos"
        AppLanguage.FRENCH -> "Répertoire des Fichiers Statiques"
        AppLanguage.GERMAN -> "Verzeichnis für statische Dateien"
        AppLanguage.RUSSIAN -> "Каталог статических файлов"
        AppLanguage.JAPANESE -> "静的ファイルディレクトリ"
        AppLanguage.KOREAN -> "정적 파일 디렉토리"
    }
    val goStaticFilesHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Web 静态文件服务路径"
        AppLanguage.ENGLISH -> "Web static file serving path"
        AppLanguage.ARABIC -> "مسار خدمة الملفات الثابتة على الويب"
        AppLanguage.PORTUGUESE -> "Caminho de serviço de arquivos estáticos da web"
        AppLanguage.SPANISH -> "Ruta de servicio de archivos estáticos web"
        AppLanguage.FRENCH -> "Chemin de service des fichiers statiques web"
        AppLanguage.GERMAN -> "Web-Pfad für statische Dateien"
        AppLanguage.RUSSIAN -> "Путь обслуживания статических веб-файлов"
        AppLanguage.JAPANESE -> "Web 静的ファイル配信パス"
        AppLanguage.KOREAN -> "웹 정적 파일 서비스 경로"
    }
    val goHealthCheck: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "健康检查"
        AppLanguage.ENGLISH -> "Health Check"
        AppLanguage.ARABIC -> "فحص السلامة"
        AppLanguage.PORTUGUESE -> "Verificação de Saúde"
        AppLanguage.SPANISH -> "Comprobación de Salud"
        AppLanguage.FRENCH -> "Vérification de Santé"
        AppLanguage.GERMAN -> "Gesundheitsprüfung"
        AppLanguage.RUSSIAN -> "Проверка работоспособности"
        AppLanguage.JAPANESE -> "ヘルスチェック"
        AppLanguage.KOREAN -> "헬스 체크"
    }
    val goHealthCheckEndpoint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "健康检查端点"
        AppLanguage.ENGLISH -> "Health Check Endpoint"
        AppLanguage.ARABIC -> "نقطة نهاية فحص السلامة"
        AppLanguage.PORTUGUESE -> "Endpoint de Verificação de Saúde"
        AppLanguage.SPANISH -> "Endpoint de Comprobación de Salud"
        AppLanguage.FRENCH -> "Endpoint de Vérification de Santé"
        AppLanguage.GERMAN -> "Gesundheitsprüfungs-Endpunkt"
        AppLanguage.RUSSIAN -> "Эндпоинт проверки работоспособности"
        AppLanguage.JAPANESE -> "ヘルスチェックエンドポイント"
        AppLanguage.KOREAN -> "헬스 체크 엔드포인트"
    }
    val goStaticFilesExample: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "static/"
        AppLanguage.ENGLISH -> "static/"
        AppLanguage.ARABIC -> "static/"
        AppLanguage.PORTUGUESE -> "static/"
        AppLanguage.SPANISH -> "static/"
        AppLanguage.FRENCH -> "static/"
        AppLanguage.GERMAN -> "static/"
        AppLanguage.RUSSIAN -> "static/"
        AppLanguage.JAPANESE -> "static/"
        AppLanguage.KOREAN -> "static/"
    }
    val goHealthCheckEndpointExample: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "/health"
        AppLanguage.ENGLISH -> "/health"
        AppLanguage.ARABIC -> "/health"
        AppLanguage.PORTUGUESE -> "/health"
        AppLanguage.SPANISH -> "/health"
        AppLanguage.FRENCH -> "/health"
        AppLanguage.GERMAN -> "/health"
        AppLanguage.RUSSIAN -> "/health"
        AppLanguage.JAPANESE -> "/health"
        AppLanguage.KOREAN -> "/health"
    }
    val goDirectDeps: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "直接依赖"
        AppLanguage.ENGLISH -> "Direct Dependencies"
        AppLanguage.ARABIC -> "التبعيات المباشرة"
        AppLanguage.PORTUGUESE -> "Dependências Diretas"
        AppLanguage.SPANISH -> "Dependencias Directas"
        AppLanguage.FRENCH -> "Dépendances Directes"
        AppLanguage.GERMAN -> "Direkte Abhängigkeiten"
        AppLanguage.RUSSIAN -> "Прямые зависимости"
        AppLanguage.JAPANESE -> "直接依存関係"
        AppLanguage.KOREAN -> "직접 의존성"
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
    val njsDependencies: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "项目依赖"
        AppLanguage.ENGLISH -> "Project Dependencies"
        AppLanguage.ARABIC -> "تبعيات المشروع"
        AppLanguage.PORTUGUESE -> "Dependências do Projeto"
        AppLanguage.SPANISH -> "Dependencias del Proyecto"
        AppLanguage.FRENCH -> "Dépendances du Projet"
        AppLanguage.GERMAN -> "Projekt-Abhängigkeiten"
        AppLanguage.RUSSIAN -> "Зависимости проекта"
        AppLanguage.JAPANESE -> "プロジェクトの依存関係"
        AppLanguage.KOREAN -> "프로젝트 의존성"
    }
    val njsDevDependencies: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "开发依赖"
        AppLanguage.ENGLISH -> "Dev Dependencies"
        AppLanguage.ARABIC -> "تبعيات التطوير"
        AppLanguage.PORTUGUESE -> "Dependências de Desenvolvimento"
        AppLanguage.SPANISH -> "Dependencias de Desarrollo"
        AppLanguage.FRENCH -> "Dépendances de Dev"
        AppLanguage.GERMAN -> "Dev-Abhängigkeiten"
        AppLanguage.RUSSIAN -> "Зависимости разработки"
        AppLanguage.JAPANESE -> "開発依存関係"
        AppLanguage.KOREAN -> "개발 의존성"
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

    val wpHeroTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "WordPress 应用"
        AppLanguage.ENGLISH -> "WordPress Application"
        AppLanguage.ARABIC -> "تطبيق WordPress"
        AppLanguage.PORTUGUESE -> "Aplicação WordPress"
        AppLanguage.SPANISH -> "Aplicación WordPress"
        AppLanguage.FRENCH -> "Application WordPress"
        AppLanguage.GERMAN -> "WordPress-Anwendung"
        AppLanguage.RUSSIAN -> "Приложение WordPress"
        AppLanguage.JAPANESE -> "WordPress アプリケーション"
        AppLanguage.KOREAN -> "WordPress 애플리케이션"
    }
    val wpHeroDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "运行离线 WordPress 站点，无需服务器"
        AppLanguage.ENGLISH -> "Run offline WordPress site, no server needed"
        AppLanguage.ARABIC -> "تشغيل موقع WordPress دون اتصال، بدون خادم"
        AppLanguage.PORTUGUESE -> "Executar site WordPress offline, sem servidor"
        AppLanguage.SPANISH -> "Ejecutar sitio WordPress sin conexión, sin servidor"
        AppLanguage.FRENCH -> "Exécuter un site WordPress hors ligne, sans serveur"
        AppLanguage.GERMAN -> "Offline-WordPress-Site ausführen, kein Server nötig"
        AppLanguage.RUSSIAN -> "Запуск офлайн-сайта WordPress, без сервера"
        AppLanguage.JAPANESE -> "オフラインで WordPress サイトを実行、サーバー不要"
        AppLanguage.KOREAN -> "오프라인 WordPress 사이트 실행, 서버 불필요"
    }
    val wpThemePanel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "主题管理"
        AppLanguage.ENGLISH -> "Theme Management"
        AppLanguage.ARABIC -> "إدارة السمات"
        AppLanguage.PORTUGUESE -> "Gestão de Temas"
        AppLanguage.SPANISH -> "Gestión de Temas"
        AppLanguage.FRENCH -> "Gestion des Thèmes"
        AppLanguage.GERMAN -> "Theme-Verwaltung"
        AppLanguage.RUSSIAN -> "Управление темами"
        AppLanguage.JAPANESE -> "テーマ管理"
        AppLanguage.KOREAN -> "테마 관리"
    }
    val wpActiveTheme: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "当前主题"
        AppLanguage.ENGLISH -> "Active Theme"
        AppLanguage.ARABIC -> "السمة النشطة"
        AppLanguage.PORTUGUESE -> "Tema Ativo"
        AppLanguage.SPANISH -> "Tema Activo"
        AppLanguage.FRENCH -> "Thème Actif"
        AppLanguage.GERMAN -> "Aktives Theme"
        AppLanguage.RUSSIAN -> "Активная тема"
        AppLanguage.JAPANESE -> "アクティブテーマ"
        AppLanguage.KOREAN -> "활성 테마"
    }
    val wpInstalledThemes: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已安装主题"
        AppLanguage.ENGLISH -> "Installed Themes"
        AppLanguage.ARABIC -> "السمات المثبتة"
        AppLanguage.PORTUGUESE -> "Temas Instalados"
        AppLanguage.SPANISH -> "Temas Instalados"
        AppLanguage.FRENCH -> "Thèmes Installés"
        AppLanguage.GERMAN -> "Installierte Themes"
        AppLanguage.RUSSIAN -> "Установленные темы"
        AppLanguage.JAPANESE -> "インストール済みテーマ"
        AppLanguage.KOREAN -> "설치된 테마"
    }
    val wpPluginPanel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "插件管理"
        AppLanguage.ENGLISH -> "Plugin Management"
        AppLanguage.ARABIC -> "إدارة الإضافات"
        AppLanguage.PORTUGUESE -> "Gestão de Plugins"
        AppLanguage.SPANISH -> "Gestión de Plugins"
        AppLanguage.FRENCH -> "Gestion des Plugins"
        AppLanguage.GERMAN -> "Plugin-Verwaltung"
        AppLanguage.RUSSIAN -> "Управление плагинами"
        AppLanguage.JAPANESE -> "プラグイン管理"
        AppLanguage.KOREAN -> "플러그인 관리"
    }
    val wpAdminConfig: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "管理员配置"
        AppLanguage.ENGLISH -> "Admin Configuration"
        AppLanguage.ARABIC -> "إعدادات المسؤول"
        AppLanguage.PORTUGUESE -> "Configuração de Admin"
        AppLanguage.SPANISH -> "Configuración de Admin"
        AppLanguage.FRENCH -> "Configuration Admin"
        AppLanguage.GERMAN -> "Admin-Konfiguration"
        AppLanguage.RUSSIAN -> "Конфигурация администратора"
        AppLanguage.JAPANESE -> "管理者設定"
        AppLanguage.KOREAN -> "관리자 설정"
    }
    val wpAdminEmail: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "管理员邮箱"
        AppLanguage.ENGLISH -> "Admin Email"
        AppLanguage.ARABIC -> "بريد المسؤول"
        AppLanguage.PORTUGUESE -> "Email do Admin"
        AppLanguage.SPANISH -> "Correo del Admin"
        AppLanguage.FRENCH -> "E-mail Admin"
        AppLanguage.GERMAN -> "Admin-E-Mail"
        AppLanguage.RUSSIAN -> "Email администратора"
        AppLanguage.JAPANESE -> "管理者メール"
        AppLanguage.KOREAN -> "관리자 이메일"
    }
    val wpAdminEmailHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "例如：admin@example.com"
        AppLanguage.ENGLISH -> "For example: admin@example.com"
        AppLanguage.ARABIC -> "مثال: admin@example.com"
        AppLanguage.PORTUGUESE -> "Exemplo: admin@example.com"
        AppLanguage.SPANISH -> "Ejemplo: admin@example.com"
        AppLanguage.FRENCH -> "Exemple : admin@example.com"
        AppLanguage.GERMAN -> "Beispiel: admin@example.com"
        AppLanguage.RUSSIAN -> "Например: admin@example.com"
        AppLanguage.JAPANESE -> "例: admin@example.com"
        AppLanguage.KOREAN -> "예: admin@example.com"
    }
    val wpAdminPassword: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "管理员密码"
        AppLanguage.ENGLISH -> "Admin Password"
        AppLanguage.ARABIC -> "كلمة مرور المسؤول"
        AppLanguage.PORTUGUESE -> "Senha do Admin"
        AppLanguage.SPANISH -> "Contraseña del Admin"
        AppLanguage.FRENCH -> "Mot de passe Admin"
        AppLanguage.GERMAN -> "Admin-Passwort"
        AppLanguage.RUSSIAN -> "Пароль администратора"
        AppLanguage.JAPANESE -> "管理者パスワード"
        AppLanguage.KOREAN -> "관리자 비밀번호"
    }
    val wpAdminPasswordHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "管理员密码（默认 admin）"
        AppLanguage.ENGLISH -> "Admin password (default: admin)"
        AppLanguage.ARABIC -> "كلمة مرور المسؤول (الافتراضية: admin)"
        AppLanguage.PORTUGUESE -> "Senha do admin (padrão: admin)"
        AppLanguage.SPANISH -> "Contraseña del admin (predeterminada: admin)"
        AppLanguage.FRENCH -> "Mot de passe admin (par défaut : admin)"
        AppLanguage.GERMAN -> "Admin-Passwort (Standard: admin)"
        AppLanguage.RUSSIAN -> "Пароль администратора (по умолчанию: admin)"
        AppLanguage.JAPANESE -> "管理者パスワード(デフォルト: admin)"
        AppLanguage.KOREAN -> "관리자 비밀번호(기본값: admin)"
    }
    val wpPermalink: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "固定链接结构"
        AppLanguage.ENGLISH -> "Permalink Structure"
        AppLanguage.ARABIC -> "هيكل الروابط الدائمة"
        AppLanguage.PORTUGUESE -> "Estrutura de Link Permanente"
        AppLanguage.SPANISH -> "Estructura de Enlace Permanente"
        AppLanguage.FRENCH -> "Structure de Permalien"
        AppLanguage.GERMAN -> "Permalink-Struktur"
        AppLanguage.RUSSIAN -> "Структура постоянных ссылок"
        AppLanguage.JAPANESE -> "パーマリンク構造"
        AppLanguage.KOREAN -> "고정링크 구조"
    }
    val wpPermalinkPlain: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "朴素 (?p=123)"
        AppLanguage.ENGLISH -> "Plain (?p=123)"
        AppLanguage.ARABIC -> "عادي (?p=123)"
        AppLanguage.PORTUGUESE -> "Simples (?p=123)"
        AppLanguage.SPANISH -> "Simple (?p=123)"
        AppLanguage.FRENCH -> "Simple (?p=123)"
        AppLanguage.GERMAN -> "Einfach (?p=123)"
        AppLanguage.RUSSIAN -> "Простой (?p=123)"
        AppLanguage.JAPANESE -> "プレーン (?p=123)"
        AppLanguage.KOREAN -> "단순 (?p=123)"
    }
    val wpPermalinkPostName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "文章名 (/post-name/)"
        AppLanguage.ENGLISH -> "Post name (/post-name/)"
        AppLanguage.ARABIC -> "اسم المنشور (/post-name/)"
        AppLanguage.PORTUGUESE -> "Nome do post (/post-name/)"
        AppLanguage.SPANISH -> "Nombre del post (/post-name/)"
        AppLanguage.FRENCH -> "Nom du post (/post-name/)"
        AppLanguage.GERMAN -> "Post-Name (/post-name/)"
        AppLanguage.RUSSIAN -> "Имя поста (/post-name/)"
        AppLanguage.JAPANESE -> "投稿名 (/post-name/)"
        AppLanguage.KOREAN -> "글 이름 (/post-name/)"
    }
    val wpPermalinkNumeric: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "数字型 (/archives/123)"
        AppLanguage.ENGLISH -> "Numeric (/archives/123)"
        AppLanguage.ARABIC -> "رقمي (/archives/123)"
        AppLanguage.PORTUGUESE -> "Numérico (/archives/123)"
        AppLanguage.SPANISH -> "Numérico (/archives/123)"
        AppLanguage.FRENCH -> "Numérique (/archives/123)"
        AppLanguage.GERMAN -> "Numerisch (/archives/123)"
        AppLanguage.RUSSIAN -> "Числовой (/archives/123)"
        AppLanguage.JAPANESE -> "数字型 (/archives/123)"
        AppLanguage.KOREAN -> "숫자형 (/archives/123)"
    }
    val wpDbInfo: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "数据库信息"
        AppLanguage.ENGLISH -> "Database Info"
        AppLanguage.ARABIC -> "معلومات قاعدة البيانات"
        AppLanguage.PORTUGUESE -> "Informações do Banco de Dados"
        AppLanguage.SPANISH -> "Información de la Base de Datos"
        AppLanguage.FRENCH -> "Infos de la Base de Données"
        AppLanguage.GERMAN -> "Datenbankinfo"
        AppLanguage.RUSSIAN -> "Информация о базе данных"
        AppLanguage.JAPANESE -> "データベース情報"
        AppLanguage.KOREAN -> "데이터베이스 정보"
    }
    val wpDbType: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "数据库类型：SQLite（离线）"
        AppLanguage.ENGLISH -> "Database type: SQLite (offline)"
        AppLanguage.ARABIC -> "نوع قاعدة البيانات: SQLite (غير متصل)"
        AppLanguage.PORTUGUESE -> "Tipo de banco de dados: SQLite (offline)"
        AppLanguage.SPANISH -> "Tipo de base de datos: SQLite (sin conexión)"
        AppLanguage.FRENCH -> "Type de base de données : SQLite (hors ligne)"
        AppLanguage.GERMAN -> "Datenbanktyp: SQLite (offline)"
        AppLanguage.RUSSIAN -> "Тип базы данных: SQLite (офлайн)"
        AppLanguage.JAPANESE -> "データベースタイプ: SQLite(オフライン)"
        AppLanguage.KOREAN -> "데이터베이스 타입: SQLite(오프라인)"
    }
    val wpSiteLanguage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "站点语言"
        AppLanguage.ENGLISH -> "Site Language"
        AppLanguage.ARABIC -> "لغة الموقع"
        AppLanguage.PORTUGUESE -> "Idioma do Site"
        AppLanguage.SPANISH -> "Idioma del Sitio"
        AppLanguage.FRENCH -> "Langue du Site"
        AppLanguage.GERMAN -> "Site-Sprache"
        AppLanguage.RUSSIAN -> "Язык сайта"
        AppLanguage.JAPANESE -> "サイト言語"
        AppLanguage.KOREAN -> "사이트 언어"
    }
    val wpPluginActive: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已启用"
        AppLanguage.ENGLISH -> "Active"
        AppLanguage.ARABIC -> "مفعل"
        AppLanguage.PORTUGUESE -> "Ativo"
        AppLanguage.SPANISH -> "Activo"
        AppLanguage.FRENCH -> "Actif"
        AppLanguage.GERMAN -> "Aktiv"
        AppLanguage.RUSSIAN -> "Активен"
        AppLanguage.JAPANESE -> "有効"
        AppLanguage.KOREAN -> "활성"
    }
    val wpPluginInactive: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "未启用"
        AppLanguage.ENGLISH -> "Inactive"
        AppLanguage.ARABIC -> "غير مفعل"
        AppLanguage.PORTUGUESE -> "Inativo"
        AppLanguage.SPANISH -> "Inactivo"
        AppLanguage.FRENCH -> "Inactif"
        AppLanguage.GERMAN -> "Inaktiv"
        AppLanguage.RUSSIAN -> "Неактивен"
        AppLanguage.JAPANESE -> "無効"
        AppLanguage.KOREAN -> "비활성"
    }
    val wpDbEngine: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "引擎"
        AppLanguage.ENGLISH -> "Engine"
        AppLanguage.ARABIC -> "المحرك"
        AppLanguage.PORTUGUESE -> "Motor"
        AppLanguage.SPANISH -> "Motor"
        AppLanguage.FRENCH -> "Moteur"
        AppLanguage.GERMAN -> "Engine"
        AppLanguage.RUSSIAN -> "Движок"
        AppLanguage.JAPANESE -> "エンジン"
        AppLanguage.KOREAN -> "엔진"
    }
    val wpDbMode: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "模式"
        AppLanguage.ENGLISH -> "Mode"
        AppLanguage.ARABIC -> "الوضع"
        AppLanguage.PORTUGUESE -> "Modo"
        AppLanguage.SPANISH -> "Modo"
        AppLanguage.FRENCH -> "Mode"
        AppLanguage.GERMAN -> "Modus"
        AppLanguage.RUSSIAN -> "Режим"
        AppLanguage.JAPANESE -> "モード"
        AppLanguage.KOREAN -> "모드"
    }
    val wpDbPlugin: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "插件"
        AppLanguage.ENGLISH -> "Plugin"
        AppLanguage.ARABIC -> "الإضافة"
        AppLanguage.PORTUGUESE -> "Plugin"
        AppLanguage.SPANISH -> "Plugin"
        AppLanguage.FRENCH -> "Plugin"
        AppLanguage.GERMAN -> "Plugin"
        AppLanguage.RUSSIAN -> "Плагин"
        AppLanguage.JAPANESE -> "プラグイン"
        AppLanguage.KOREAN -> "플러그인"
    }
    val wpDbOfflineMode: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "离线 / 本地设备"
        AppLanguage.ENGLISH -> "Offline / On-device"
        AppLanguage.ARABIC -> "غير متصل / على الجهاز"
        AppLanguage.PORTUGUESE -> "Offline / No dispositivo"
        AppLanguage.SPANISH -> "Sin conexión / En el dispositivo"
        AppLanguage.FRENCH -> "Hors ligne / Sur l'appareil"
        AppLanguage.GERMAN -> "Offline / Auf dem Gerät"
        AppLanguage.RUSSIAN -> "Офлайн / На устройстве"
        AppLanguage.JAPANESE -> "オフライン / デバイス上"
        AppLanguage.KOREAN -> "오프라인 / 기기 내"
    }
    val wpNoPlugins: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "无已安装插件"
        AppLanguage.ENGLISH -> "No plugins installed"
        AppLanguage.ARABIC -> "لا توجد إضافات مثبتة"
        AppLanguage.PORTUGUESE -> "Nenhum plugin instalado"
        AppLanguage.SPANISH -> "Ningún plugin instalado"
        AppLanguage.FRENCH -> "Aucun plugin installé"
        AppLanguage.GERMAN -> "Keine Plugins installiert"
        AppLanguage.RUSSIAN -> "Нет установленных плагинов"
        AppLanguage.JAPANESE -> "インストールされたプラグインはありません"
        AppLanguage.KOREAN -> "설치된 플러그인 없음"
    }
    val wpNoThemes: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "使用默认主题"
        AppLanguage.ENGLISH -> "Using default theme"
        AppLanguage.ARABIC -> "استخدام السمة الافتراضية"
        AppLanguage.PORTUGUESE -> "Usando tema padrão"
        AppLanguage.SPANISH -> "Usando tema predeterminado"
        AppLanguage.FRENCH -> "Utilisation du thème par défaut"
        AppLanguage.GERMAN -> "Verwendet Standard-Theme"
        AppLanguage.RUSSIAN -> "Используется тема по умолчанию"
        AppLanguage.JAPANESE -> "デフォルトテーマを使用中"
        AppLanguage.KOREAN -> "기본 테마 사용 중"
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

    val samplePhpSubtitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "一键导入 PHP 示例"
        AppLanguage.ENGLISH -> "Import a PHP sample to try"
        AppLanguage.ARABIC -> "استيراد مثال PHP"
        AppLanguage.PORTUGUESE -> "Importe um exemplo PHP"
        AppLanguage.SPANISH -> "Importa un ejemplo PHP"
        AppLanguage.FRENCH -> "Importez un exemple PHP"
        AppLanguage.GERMAN -> "PHP-Beispiel importieren"
        AppLanguage.RUSSIAN -> "Импорт примера PHP"
        AppLanguage.JAPANESE -> "PHP サンプルをインポート"
        AppLanguage.KOREAN -> "PHP 샘플 가져오기"
    }
    val samplePhpLaravelName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Laravel 博客"
        AppLanguage.ENGLISH -> "Laravel Blog"
        AppLanguage.ARABIC -> "مدونة Laravel"
        AppLanguage.PORTUGUESE -> "Blog Laravel"
        AppLanguage.SPANISH -> "Blog Laravel"
        AppLanguage.FRENCH -> "Blog Laravel"
        AppLanguage.GERMAN -> "Laravel-Blog"
        AppLanguage.RUSSIAN -> "Блог Laravel"
        AppLanguage.JAPANESE -> "Laravel ブログ"
        AppLanguage.KOREAN -> "Laravel 블로그"
    }
    val samplePhpLaravelDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Laravel 框架博客示例，展示路由与 Blade 模板引擎"
        AppLanguage.ENGLISH -> "Laravel blog demo with routing and Blade template engine"
        AppLanguage.ARABIC -> "عرض مدونة Laravel مع التوجيه ومحرك قوالب Blade"
        AppLanguage.PORTUGUESE -> "Demo de blog Laravel com roteamento e motor de templates Blade"
        AppLanguage.SPANISH -> "Demo de blog Laravel con enrutamiento y motor de plantillas Blade"
        AppLanguage.FRENCH -> "Démo de blog Laravel avec routage et moteur de templates Blade"
        AppLanguage.GERMAN -> "Laravel-Blog-Demo mit Routing und Blade-Template-Engine"
        AppLanguage.RUSSIAN -> "Демо блога Laravel с маршрутизацией и шаблонизатором Blade"
        AppLanguage.JAPANESE -> "Laravel ブログデモ、ルーティングと Blade テンプレートエンジンを表示"
        AppLanguage.KOREAN -> "Laravel 블로그 데모, 라우팅과 Blade 템플릿 엔진 포함"
    }
    val samplePhpSlimName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Slim REST API"
        AppLanguage.ENGLISH -> "Slim REST API"
        AppLanguage.ARABIC -> "Slim REST API"
        AppLanguage.PORTUGUESE -> "Slim REST API"
        AppLanguage.SPANISH -> "Slim REST API"
        AppLanguage.FRENCH -> "Slim REST API"
        AppLanguage.GERMAN -> "Slim REST API"
        AppLanguage.RUSSIAN -> "Slim REST API"
        AppLanguage.JAPANESE -> "Slim REST API"
        AppLanguage.KOREAN -> "Slim REST API"
    }
    val samplePhpSlimDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Slim 微框架 RESTful 接口示例，轻量高效"
        AppLanguage.ENGLISH -> "Slim micro-framework RESTful API demo, lightweight and fast"
        AppLanguage.ARABIC -> "عرض API RESTful بإطار Slim الخفيف والسريع"
        AppLanguage.PORTUGUESE -> "Demo de API RESTful com micro-framework Slim, leve e rápido"
        AppLanguage.SPANISH -> "Demo de API RESTful con micro-framework Slim, ligero y rápido"
        AppLanguage.FRENCH -> "Démo d'API RESTful avec micro-framework Slim, léger et rapide"
        AppLanguage.GERMAN -> "Slim-Microframework RESTful-API-Demo, leicht und schnell"
        AppLanguage.RUSSIAN -> "Демо RESTful API на микро-фреймворке Slim, лёгкий и быстрый"
        AppLanguage.JAPANESE -> "Slim マイクロフレームワークの RESTful API デモ、軽量・高速"
        AppLanguage.KOREAN -> "Slim 마이크로 프레임워크 RESTful API 데모, 가볍고 빠름"
    }
    val samplePhpVanillaName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "原生 PHP 应用"
        AppLanguage.ENGLISH -> "Vanilla PHP App"
        AppLanguage.ARABIC -> "تطبيق PHP أصلي"
        AppLanguage.PORTUGUESE -> "App PHP Vanilla"
        AppLanguage.SPANISH -> "App PHP Vanilla"
        AppLanguage.FRENCH -> "App PHP Vanilla"
        AppLanguage.GERMAN -> "Vanilla-PHP-App"
        AppLanguage.RUSSIAN -> "Приложение на чистом PHP"
        AppLanguage.JAPANESE -> "素の PHP アプリ"
        AppLanguage.KOREAN -> "순수 PHP 앱"
    }
    val samplePhpVanillaDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "无框架原生 PHP 开发示例，展示基本 Web 功能"
        AppLanguage.ENGLISH -> "No-framework PHP demo showcasing basic web features"
        AppLanguage.ARABIC -> "عرض PHP بدون إطار عمل يعرض ميزات الويب الأساسية"
        AppLanguage.PORTUGUESE -> "Demo de PHP sem framework mostrando recursos web básicos"
        AppLanguage.SPANISH -> "Demo de PHP sin framework que muestra funciones web básicas"
        AppLanguage.FRENCH -> "Démo PHP sans framework présentant les fonctionnalités web de base"
        AppLanguage.GERMAN -> "PHP-Demo ohne Framework, zeigt grundlegende Web-Funktionen"
        AppLanguage.RUSSIAN -> "Демо PHP без фреймворка, демонстрирует базовые веб-функции"
        AppLanguage.JAPANESE -> "フレームワークなしの PHP デモ、基本的な Web 機能を表示"
        AppLanguage.KOREAN -> "프레임워크 없는 PHP 데모, 기본 웹 기능 소개"
    }
    val sampleTagMvc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "MVC"
        AppLanguage.ENGLISH -> "MVC"
        AppLanguage.ARABIC -> "MVC"
        AppLanguage.PORTUGUESE -> "MVC"
        AppLanguage.SPANISH -> "MVC"
        AppLanguage.FRENCH -> "MVC"
        AppLanguage.GERMAN -> "MVC"
        AppLanguage.RUSSIAN -> "MVC"
        AppLanguage.JAPANESE -> "MVC"
        AppLanguage.KOREAN -> "MVC"
    }
    val sampleTagRest: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "REST"
        AppLanguage.ENGLISH -> "REST"
        AppLanguage.ARABIC -> "REST"
        AppLanguage.PORTUGUESE -> "REST"
        AppLanguage.SPANISH -> "REST"
        AppLanguage.FRENCH -> "REST"
        AppLanguage.GERMAN -> "REST"
        AppLanguage.RUSSIAN -> "REST"
        AppLanguage.JAPANESE -> "REST"
        AppLanguage.KOREAN -> "REST"
    }
    val sampleTagLightweight: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "轻量级"
        AppLanguage.ENGLISH -> "Lightweight"
        AppLanguage.ARABIC -> "خفيف الوزن"
        AppLanguage.PORTUGUESE -> "Leve"
        AppLanguage.SPANISH -> "Ligero"
        AppLanguage.FRENCH -> "Léger"
        AppLanguage.GERMAN -> "Leicht"
        AppLanguage.RUSSIAN -> "Лёгкий"
        AppLanguage.JAPANESE -> "軽量"
        AppLanguage.KOREAN -> "가벼움"
    }
    val sampleTagNoFramework: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "无框架"
        AppLanguage.ENGLISH -> "No Framework"
        AppLanguage.ARABIC -> "بدون إطار"
        AppLanguage.PORTUGUESE -> "Sem Framework"
        AppLanguage.SPANISH -> "Sin Framework"
        AppLanguage.FRENCH -> "Sans Framework"
        AppLanguage.GERMAN -> "Ohne Framework"
        AppLanguage.RUSSIAN -> "Без фреймворка"
        AppLanguage.JAPANESE -> "フレームワークなし"
        AppLanguage.KOREAN -> "프레임워크 없음"
    }

    val samplePythonSubtitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "一键导入 Python 示例"
        AppLanguage.ENGLISH -> "Import a Python sample to try"
        AppLanguage.ARABIC -> "استيراد مثال Python"
        AppLanguage.PORTUGUESE -> "Importe um exemplo Python"
        AppLanguage.SPANISH -> "Importa un ejemplo Python"
        AppLanguage.FRENCH -> "Importez un exemple Python"
        AppLanguage.GERMAN -> "Python-Beispiel importieren"
        AppLanguage.RUSSIAN -> "Импорт примера Python"
        AppLanguage.JAPANESE -> "Python サンプルをインポート"
        AppLanguage.KOREAN -> "Python 샘플 가져오기"
    }
    val samplePythonFlaskName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Flask 仪表盘"
        AppLanguage.ENGLISH -> "Flask Dashboard"
        AppLanguage.ARABIC -> "لوحة تحكم Flask"
        AppLanguage.PORTUGUESE -> "Painel Flask"
        AppLanguage.SPANISH -> "Panel Flask"
        AppLanguage.FRENCH -> "Tableau de bord Flask"
        AppLanguage.GERMAN -> "Flask-Dashboard"
        AppLanguage.RUSSIAN -> "Панель Flask"
        AppLanguage.JAPANESE -> "Flask ダッシュボード"
        AppLanguage.KOREAN -> "Flask 대시보드"
    }
    val samplePythonFlaskDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Flask 框架数据仪表盘示例，展示 Jinja2 模板渲染"
        AppLanguage.ENGLISH -> "Flask dashboard demo with Jinja2 template rendering"
        AppLanguage.ARABIC -> "عرض لوحة تحكم Flask مع عرض قوالب Jinja2"
        AppLanguage.PORTUGUESE -> "Demo de painel Flask com renderização de templates Jinja2"
        AppLanguage.SPANISH -> "Demo de panel Flask con renderizado de plantillas Jinja2"
        AppLanguage.FRENCH -> "Démo de tableau de bord Flask avec rendu de templates Jinja2"
        AppLanguage.GERMAN -> "Flask-Dashboard-Demo mit Jinja2-Template-Rendering"
        AppLanguage.RUSSIAN -> "Демо панели Flask с рендерингом шаблонов Jinja2"
        AppLanguage.JAPANESE -> "Flask ダッシュボードデモ、Jinja2 テンプレートレンダリングを表示"
        AppLanguage.KOREAN -> "Flask 대시보드 데모, Jinja2 템플릿 렌더링 포함"
    }
    val samplePythonFastapiName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "FastAPI 书店"
        AppLanguage.ENGLISH -> "FastAPI Bookstore"
        AppLanguage.ARABIC -> "مكتبة FastAPI"
        AppLanguage.PORTUGUESE -> "Livraria FastAPI"
        AppLanguage.SPANISH -> "Librería FastAPI"
        AppLanguage.FRENCH -> "Librairie FastAPI"
        AppLanguage.GERMAN -> "FastAPI-Buchhandlung"
        AppLanguage.RUSSIAN -> "Книжный магазин FastAPI"
        AppLanguage.JAPANESE -> "FastAPI ブックストア"
        AppLanguage.KOREAN -> "FastAPI 서점"
    }
    val samplePythonFastapiDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "FastAPI 书店 REST API 示例，自动生成 OpenAPI 文档"
        AppLanguage.ENGLISH -> "FastAPI bookstore REST API with auto-generated OpenAPI docs"
        AppLanguage.ARABIC -> "API مكتبة FastAPI مع توثيق OpenAPI تلقائي"
        AppLanguage.PORTUGUESE -> "API REST de livraria FastAPI com docs OpenAPI gerados automaticamente"
        AppLanguage.SPANISH -> "API REST de librería FastAPI con docs OpenAPI autogenerados"
        AppLanguage.FRENCH -> "API REST de librairie FastAPI avec docs OpenAPI auto-générés"
        AppLanguage.GERMAN -> "FastAPI-Buchhandlungs-REST-API mit auto-generierter OpenAPI-Doku"
        AppLanguage.RUSSIAN -> "REST API книжного магазина FastAPI с автогенерацией OpenAPI-документации"
        AppLanguage.JAPANESE -> "FastAPI 書店の REST API、OpenAPI ドキュメントを自動生成"
        AppLanguage.KOREAN -> "FastAPI 서점 REST API, OpenAPI 문서 자동 생성"
    }
    val samplePythonDjangoName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Django 站点"
        AppLanguage.ENGLISH -> "Django Site"
        AppLanguage.ARABIC -> "موقع Django"
        AppLanguage.PORTUGUESE -> "Site Django"
        AppLanguage.SPANISH -> "Sitio Django"
        AppLanguage.FRENCH -> "Site Django"
        AppLanguage.GERMAN -> "Django-Site"
        AppLanguage.RUSSIAN -> "Сайт Django"
        AppLanguage.JAPANESE -> "Django サイト"
        AppLanguage.KOREAN -> "Django 사이트"
    }
    val samplePythonDjangoDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Django 全栈站点示例，展示 ORM 与管理后台"
        AppLanguage.ENGLISH -> "Django full-stack site demo with ORM and admin panel"
        AppLanguage.ARABIC -> "عرض موقع Django كامل مع ORM ولوحة الإدارة"
        AppLanguage.PORTUGUESE -> "Demo de site Django full-stack com ORM e painel admin"
        AppLanguage.SPANISH -> "Demo de sitio Django full-stack con ORM y panel admin"
        AppLanguage.FRENCH -> "Démo de site Django full-stack avec ORM et panneau admin"
        AppLanguage.GERMAN -> "Django-Fullstack-Site-Demo mit ORM und Admin-Panel"
        AppLanguage.RUSSIAN -> "Демо fullstack-сайта Django с ORM и админ-панелью"
        AppLanguage.JAPANESE -> "Django フルスタックサイトデモ、ORMと管理画面を表示"
        AppLanguage.KOREAN -> "Django 풀스택 사이트 데모, ORM과 관리 패널 포함"
    }
    val sampleTagWsgi: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "WSGI"
        AppLanguage.ENGLISH -> "WSGI"
        AppLanguage.ARABIC -> "WSGI"
        AppLanguage.PORTUGUESE -> "WSGI"
        AppLanguage.SPANISH -> "WSGI"
        AppLanguage.FRENCH -> "WSGI"
        AppLanguage.GERMAN -> "WSGI"
        AppLanguage.RUSSIAN -> "WSGI"
        AppLanguage.JAPANESE -> "WSGI"
        AppLanguage.KOREAN -> "WSGI"
    }
    val sampleTagAsgi: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "ASGI"
        AppLanguage.ENGLISH -> "ASGI"
        AppLanguage.ARABIC -> "ASGI"
        AppLanguage.PORTUGUESE -> "ASGI"
        AppLanguage.SPANISH -> "ASGI"
        AppLanguage.FRENCH -> "ASGI"
        AppLanguage.GERMAN -> "ASGI"
        AppLanguage.RUSSIAN -> "ASGI"
        AppLanguage.JAPANESE -> "ASGI"
        AppLanguage.KOREAN -> "ASGI"
    }
    val sampleTagOpenapi: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "OpenAPI"
        AppLanguage.ENGLISH -> "OpenAPI"
        AppLanguage.ARABIC -> "OpenAPI"
        AppLanguage.PORTUGUESE -> "OpenAPI"
        AppLanguage.SPANISH -> "OpenAPI"
        AppLanguage.FRENCH -> "OpenAPI"
        AppLanguage.GERMAN -> "OpenAPI"
        AppLanguage.RUSSIAN -> "OpenAPI"
        AppLanguage.JAPANESE -> "OpenAPI"
        AppLanguage.KOREAN -> "OpenAPI"
    }
    val sampleTagOrm: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "ORM"
        AppLanguage.ENGLISH -> "ORM"
        AppLanguage.ARABIC -> "ORM"
        AppLanguage.PORTUGUESE -> "ORM"
        AppLanguage.SPANISH -> "ORM"
        AppLanguage.FRENCH -> "ORM"
        AppLanguage.GERMAN -> "ORM"
        AppLanguage.RUSSIAN -> "ORM"
        AppLanguage.JAPANESE -> "ORM"
        AppLanguage.KOREAN -> "ORM"
    }
    val sampleTagAdmin: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "管理后台"
        AppLanguage.ENGLISH -> "Admin"
        AppLanguage.ARABIC -> "الإدارة"
        AppLanguage.PORTUGUESE -> "Admin"
        AppLanguage.SPANISH -> "Admin"
        AppLanguage.FRENCH -> "Admin"
        AppLanguage.GERMAN -> "Admin"
        AppLanguage.RUSSIAN -> "Админ"
        AppLanguage.JAPANESE -> "管理画面"
        AppLanguage.KOREAN -> "관리"
    }

    val sampleGoSubtitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "一键导入 Go 示例"
        AppLanguage.ENGLISH -> "Import a Go sample to try"
        AppLanguage.ARABIC -> "استيراد مثال Go"
        AppLanguage.PORTUGUESE -> "Importe um exemplo Go"
        AppLanguage.SPANISH -> "Importa un ejemplo Go"
        AppLanguage.FRENCH -> "Importez un exemple Go"
        AppLanguage.GERMAN -> "Go-Beispiel importieren"
        AppLanguage.RUSSIAN -> "Импорт примера Go"
        AppLanguage.JAPANESE -> "Go サンプルをインポート"
        AppLanguage.KOREAN -> "Go 샘플 가져오기"
    }
    val sampleGoGinName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Gin REST API"
        AppLanguage.ENGLISH -> "Gin REST API"
        AppLanguage.ARABIC -> "Gin REST API"
        AppLanguage.PORTUGUESE -> "Gin REST API"
        AppLanguage.SPANISH -> "Gin REST API"
        AppLanguage.FRENCH -> "Gin REST API"
        AppLanguage.GERMAN -> "Gin REST API"
        AppLanguage.RUSSIAN -> "Gin REST API"
        AppLanguage.JAPANESE -> "Gin REST API"
        AppLanguage.KOREAN -> "Gin REST API"
    }
    val sampleGoGinDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Gin 框架 RESTful 接口示例，中间件与路由组"
        AppLanguage.ENGLISH -> "Gin RESTful API demo with middleware and route groups"
        AppLanguage.ARABIC -> "عرض Gin REST API مع الوسيط ومجموعات التوجيه"
        AppLanguage.PORTUGUESE -> "Demo de API RESTful Gin com middleware e grupos de rotas"
        AppLanguage.SPANISH -> "Demo de API RESTful Gin con middleware y grupos de rutas"
        AppLanguage.FRENCH -> "Démo d'API RESTful Gin avec middleware et groupes de routes"
        AppLanguage.GERMAN -> "Gin RESTful-API-Demo mit Middleware und Routengruppen"
        AppLanguage.RUSSIAN -> "Демо RESTful API Gin с middleware и группами маршрутов"
        AppLanguage.JAPANESE -> "Gin RESTful API デモ、ミドルウェアとルートグループ含む"
        AppLanguage.KOREAN -> "Gin RESTful API 데모, 미들웨어와 라우트 그룹 포함"
    }
    val sampleGoFiberName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Fiber Web 应用"
        AppLanguage.ENGLISH -> "Fiber Web App"
        AppLanguage.ARABIC -> "تطبيق Fiber Web"
        AppLanguage.PORTUGUESE -> "App Web Fiber"
        AppLanguage.SPANISH -> "App Web Fiber"
        AppLanguage.FRENCH -> "App Web Fiber"
        AppLanguage.GERMAN -> "Fiber-Web-App"
        AppLanguage.RUSSIAN -> "Веб-приложение Fiber"
        AppLanguage.JAPANESE -> "Fiber Web アプリ"
        AppLanguage.KOREAN -> "Fiber 웹 앱"
    }
    val sampleGoFiberDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Fiber 框架 Web 应用示例，Express 风格高性能路由"
        AppLanguage.ENGLISH -> "Fiber web app demo with Express-style high-performance routing"
        AppLanguage.ARABIC -> "عرض Fiber بأسلوب Express مع توجيه عالي الأداء"
        AppLanguage.PORTUGUESE -> "Demo de app web Fiber com roteamento de alta performance estilo Express"
        AppLanguage.SPANISH -> "Demo de app web Fiber con enrutamiento de alto rendimiento estilo Express"
        AppLanguage.FRENCH -> "Démo d'app web Fiber avec routage haute performance style Express"
        AppLanguage.GERMAN -> "Fiber-Web-App-Demo mit Express-artigem High-Performance-Routing"
        AppLanguage.RUSSIAN -> "Демо веб-приложения Fiber с высокопроизводительной маршрутизацией в стиле Express"
        AppLanguage.JAPANESE -> "Fiber Web アプリデモ、Express 風の高性能ルーティング"
        AppLanguage.KOREAN -> "Fiber 웹 앱 데모, Express 스타일 고성능 라우팅"
    }
    val sampleGoEchoName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Echo 服务器"
        AppLanguage.ENGLISH -> "Echo Server"
        AppLanguage.ARABIC -> "خادم Echo"
        AppLanguage.PORTUGUESE -> "Servidor Echo"
        AppLanguage.SPANISH -> "Servidor Echo"
        AppLanguage.FRENCH -> "Serveur Echo"
        AppLanguage.GERMAN -> "Echo-Server"
        AppLanguage.RUSSIAN -> "Сервер Echo"
        AppLanguage.JAPANESE -> "Echo サーバー"
        AppLanguage.KOREAN -> "Echo 서버"
    }
    val sampleGoEchoDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Echo 框架服务器示例，极简 API 与自动绑定"
        AppLanguage.ENGLISH -> "Echo server demo with minimal API and auto-binding"
        AppLanguage.ARABIC -> "عرض خادم Echo مع API بسيط وربط تلقائي"
        AppLanguage.PORTUGUESE -> "Demo de servidor Echo com API minimalista e auto-binding"
        AppLanguage.SPANISH -> "Demo de servidor Echo con API minimalista y auto-binding"
        AppLanguage.FRENCH -> "Démo de serveur Echo avec API minimale et auto-binding"
        AppLanguage.GERMAN -> "Echo-Server-Demo mit minimaler API und Auto-Binding"
        AppLanguage.RUSSIAN -> "Демо сервера Echo с минималистичным API и авто-биндингом"
        AppLanguage.JAPANESE -> "Echo サーバーデモ、最小 API と自動バインディング"
        AppLanguage.KOREAN -> "Echo 서버 데모, 최소 API와 자동 바인딩"
    }
    val sampleTagMiddleware: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "中间件"
        AppLanguage.ENGLISH -> "Middleware"
        AppLanguage.ARABIC -> "وسيط"
        AppLanguage.PORTUGUESE -> "Middleware"
        AppLanguage.SPANISH -> "Middleware"
        AppLanguage.FRENCH -> "Middleware"
        AppLanguage.GERMAN -> "Middleware"
        AppLanguage.RUSSIAN -> "Middleware"
        AppLanguage.JAPANESE -> "ミドルウェア"
        AppLanguage.KOREAN -> "미들웨어"
    }
    val sampleTagHighPerf: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "高性能"
        AppLanguage.ENGLISH -> "High Perf"
        AppLanguage.ARABIC -> "أداء عالي"
        AppLanguage.PORTUGUESE -> "Alta Perf"
        AppLanguage.SPANISH -> "Alto Rend."
        AppLanguage.FRENCH -> "Haute Perf"
        AppLanguage.GERMAN -> "Hohe Perf"
        AppLanguage.RUSSIAN -> "Высокая произв."
        AppLanguage.JAPANESE -> "高性能"
        AppLanguage.KOREAN -> "고성능"
    }
    val sampleTagMinimalApi: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "极简 API"
        AppLanguage.ENGLISH -> "Minimal API"
        AppLanguage.ARABIC -> "API بسيط"
        AppLanguage.PORTUGUESE -> "API Mínima"
        AppLanguage.SPANISH -> "API Mínima"
        AppLanguage.FRENCH -> "API Minimale"
        AppLanguage.GERMAN -> "Minimale API"
        AppLanguage.RUSSIAN -> "Мин. API"
        AppLanguage.JAPANESE -> "最小 API"
        AppLanguage.KOREAN -> "최소 API"
    }

    val sampleNodeSubtitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "一键导入 Node.js 示例"
        AppLanguage.ENGLISH -> "Import a Node.js sample to try"
        AppLanguage.ARABIC -> "استيراد مثال Node.js"
        AppLanguage.PORTUGUESE -> "Importe um exemplo Node.js"
        AppLanguage.SPANISH -> "Importa un ejemplo Node.js"
        AppLanguage.FRENCH -> "Importez un exemple Node.js"
        AppLanguage.GERMAN -> "Node.js-Beispiel importieren"
        AppLanguage.RUSSIAN -> "Импорт примера Node.js"
        AppLanguage.JAPANESE -> "Node.js サンプルをインポート"
        AppLanguage.KOREAN -> "Node.js 샘플 가져오기"
    }
    val sampleNodeExpressName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "待办事项应用"
        AppLanguage.ENGLISH -> "Todo App"
        AppLanguage.ARABIC -> "تطبيق المهام"
        AppLanguage.PORTUGUESE -> "App de Tarefas"
        AppLanguage.SPANISH -> "App de Tareas"
        AppLanguage.FRENCH -> "App de Tâches"
        AppLanguage.GERMAN -> "Todo-App"
        AppLanguage.RUSSIAN -> "Приложение задач"
        AppLanguage.JAPANESE -> "Todo アプリ"
        AppLanguage.KOREAN -> "할 일 앱"
    }
    val sampleNodeExpressDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "完整的待办事项应用，支持添加/完成/删除任务，Material Design 风格 UI"
        AppLanguage.ENGLISH -> "Full-featured Todo app with add/complete/delete tasks, Material Design UI"
        AppLanguage.ARABIC -> "تطبيق مهام كامل مع إضافة/إكمال/حذف المهام، واجهة Material Design"
        AppLanguage.PORTUGUESE -> "App de tarefas completo com adicionar/concluir/excluir, UI Material Design"
        AppLanguage.SPANISH -> "App de tareas completa con añadir/completar/eliminar, UI Material Design"
        AppLanguage.FRENCH -> "App de tâches complète avec ajouter/terminer/supprimer, UI Material Design"
        AppLanguage.GERMAN -> "Vollwertige Todo-App mit Hinzufügen/Erledigen/Löschen, Material Design UI"
        AppLanguage.RUSSIAN -> "Полноценное приложение задач с добавлением/завершением/удалением, UI Material Design"
        AppLanguage.JAPANESE -> "フル機能の Todo アプリ、追加/完了/削除対応、Material Design UI"
        AppLanguage.KOREAN -> "모든 기능의 할 일 앱, 추가/완료/삭제 지원, Material Design UI"
    }
    val sampleNodeFastifyName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "系统监控仪表盘"
        AppLanguage.ENGLISH -> "System Monitor Dashboard"
        AppLanguage.ARABIC -> "لوحة مراقبة النظام"
        AppLanguage.PORTUGUESE -> "Painel de Monitoramento do Sistema"
        AppLanguage.SPANISH -> "Panel de Monitorización del Sistema"
        AppLanguage.FRENCH -> "Tableau de Bord de Surveillance Système"
        AppLanguage.GERMAN -> "System-Monitor-Dashboard"
        AppLanguage.RUSSIAN -> "Панель мониторинга системы"
        AppLanguage.JAPANESE -> "システムモニターダッシュボード"
        AppLanguage.KOREAN -> "시스템 모니터 대시보드"
    }
    val sampleNodeFastifyDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "实时系统监控仪表盘，显示 CPU/内存/请求统计，深色主题"
        AppLanguage.ENGLISH -> "Real-time system monitor showing CPU/memory/request stats, dark theme"
        AppLanguage.ARABIC -> "مراقب نظام في الوقت الفعلي يعرض إحصائيات CPU/الذاكرة/الطلبات"
        AppLanguage.PORTUGUESE -> "Monitor de sistema em tempo real com estatísticas de CPU/memória/requisições, tema escuro"
        AppLanguage.SPANISH -> "Monitor de sistema en tiempo real con estadísticas de CPU/memoria/solicitudes, tema oscuro"
        AppLanguage.FRENCH -> "Surveillance système en temps réel avec stats CPU/mémoire/requêtes, thème sombre"
        AppLanguage.GERMAN -> "Echtzeit-Systemmonitor mit CPU-/Speicher-/Request-Statistiken, Dark Theme"
        AppLanguage.RUSSIAN -> "Мониторинг системы в реальном времени со статистикой CPU/памяти/запросов, тёмная тема"
        AppLanguage.JAPANESE -> "リアルタイムシステムモニター、CPU/メモリ/リクエスト統計を表示、ダークテーマ"
        AppLanguage.KOREAN -> "실시간 시스템 모니터, CPU/메모리/요청 통계 표시, 다크 테마"
    }
    val sampleNodeKoaName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Markdown 笔记"
        AppLanguage.ENGLISH -> "Markdown Notes"
        AppLanguage.ARABIC -> "ملاحظات Markdown"
        AppLanguage.PORTUGUESE -> "Notas Markdown"
        AppLanguage.SPANISH -> "Notas Markdown"
        AppLanguage.FRENCH -> "Notes Markdown"
        AppLanguage.GERMAN -> "Markdown-Notizen"
        AppLanguage.RUSSIAN -> "Заметки Markdown"
        AppLanguage.JAPANESE -> "Markdown メモ"
        AppLanguage.KOREAN -> "Markdown 메모"
    }
    val sampleNodeKoaDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Markdown 笔记应用，支持创建/编辑/搜索笔记，实时预览"
        AppLanguage.ENGLISH -> "Markdown notes app with create/edit/search, live preview"
        AppLanguage.ARABIC -> "تطبيق ملاحظات Markdown مع إنشاء/تحرير/بحث، معاينة مباشرة"
        AppLanguage.PORTUGUESE -> "App de notas Markdown com criar/editar/buscar, preview ao vivo"
        AppLanguage.SPANISH -> "App de notas Markdown con crear/editar/buscar, vista previa en vivo"
        AppLanguage.FRENCH -> "App de notes Markdown avec créer/éditer/rechercher, aperçu en direct"
        AppLanguage.GERMAN -> "Markdown-Notizen-App mit Erstellen/Bearbeiten/Suchen, Live-Vorschau"
        AppLanguage.RUSSIAN -> "Приложение заметок Markdown с созданием/редактированием/поиском, живой предпросмотр"
        AppLanguage.JAPANESE -> "Markdown メモアプリ、作成/編集/検索対応、ライブプレビュー"
        AppLanguage.KOREAN -> "Markdown 메모 앱, 생성/편집/검색 지원, 라이브 미리보기"
    }

    val sampleWpSubtitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "一键导入 WordPress 站点"
        AppLanguage.ENGLISH -> "Import a WordPress site to try"
        AppLanguage.ARABIC -> "استيراد موقع WordPress"
        AppLanguage.PORTUGUESE -> "Importe um site WordPress"
        AppLanguage.SPANISH -> "Importa un sitio WordPress"
        AppLanguage.FRENCH -> "Importez un site WordPress"
        AppLanguage.GERMAN -> "WordPress-Website importieren"
        AppLanguage.RUSSIAN -> "Импорт сайта WordPress"
        AppLanguage.JAPANESE -> "WordPress サイトをインポート"
        AppLanguage.KOREAN -> "WordPress 사이트 가져오기"
    }
    val sampleWpBlogName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "WordPress 博客"
        AppLanguage.ENGLISH -> "WordPress Blog"
        AppLanguage.ARABIC -> "مدونة WordPress"
        AppLanguage.PORTUGUESE -> "Blog WordPress"
        AppLanguage.SPANISH -> "Blog WordPress"
        AppLanguage.FRENCH -> "Blog WordPress"
        AppLanguage.GERMAN -> "WordPress-Blog"
        AppLanguage.RUSSIAN -> "Блог WordPress"
        AppLanguage.JAPANESE -> "WordPress ブログ"
        AppLanguage.KOREAN -> "WordPress 블로그"
    }
    val sampleWpBlogDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "经典 WordPress 博客站点，含默认主题与文章示例"
        AppLanguage.ENGLISH -> "Classic WordPress blog site with default theme and sample posts"
        AppLanguage.ARABIC -> "مدونة WordPress كلاسيكية مع السمة الافتراضية ومقالات نموذجية"
        AppLanguage.PORTUGUESE -> "Site blog WordPress clássico com tema padrão e posts de exemplo"
        AppLanguage.SPANISH -> "Sitio blog WordPress clásico con tema predeterminado y publicaciones de ejemplo"
        AppLanguage.FRENCH -> "Site blog WordPress classique avec thème par défaut et articles d'exemple"
        AppLanguage.GERMAN -> "Klassische WordPress-Blog-Site mit Standard-Theme und Beispielbeiträgen"
        AppLanguage.RUSSIAN -> "Классический сайт-блог WordPress с темой по умолчанию и примерами постов"
        AppLanguage.JAPANESE -> "クラシックな WordPress ブログサイト、デフォルトテーマとサンプル記事付き"
        AppLanguage.KOREAN -> "클래식 WordPress 블로그 사이트, 기본 테마와 샘플 게시물 포함"
    }
    val sampleWpWooName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "WooCommerce 商店"
        AppLanguage.ENGLISH -> "WooCommerce Store"
        AppLanguage.ARABIC -> "متجر WooCommerce"
        AppLanguage.PORTUGUESE -> "Loja WooCommerce"
        AppLanguage.SPANISH -> "Tienda WooCommerce"
        AppLanguage.FRENCH -> "Boutique WooCommerce"
        AppLanguage.GERMAN -> "WooCommerce-Shop"
        AppLanguage.RUSSIAN -> "Магазин WooCommerce"
        AppLanguage.JAPANESE -> "WooCommerce ストア"
        AppLanguage.KOREAN -> "WooCommerce 스토어"
    }
    val sampleWpWooDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "WooCommerce 电商站点示例，展示商品展示与购物车"
        AppLanguage.ENGLISH -> "WooCommerce e-commerce demo with product display and cart"
        AppLanguage.ARABIC -> "عرض WooCommerce للتجارة الإلكترونية مع عرض المنتجات وسلة التسوق"
        AppLanguage.PORTUGUESE -> "Demo de e-commerce WooCommerce com exibição de produtos e carrinho"
        AppLanguage.SPANISH -> "Demo de e-commerce WooCommerce con exhibición de productos y carrito"
        AppLanguage.FRENCH -> "Démo e-commerce WooCommerce avec affichage des produits et panier"
        AppLanguage.GERMAN -> "WooCommerce-E-Commerce-Demo mit Produktanzeige und Warenkorb"
        AppLanguage.RUSSIAN -> "Демо интернет-магазина WooCommerce с витриной товаров и корзиной"
        AppLanguage.JAPANESE -> "WooCommerce ECサイトデモ、商品表示とカート機能"
        AppLanguage.KOREAN -> "WooCommerce 이커머스 데모, 상품 표시와 장바구니"
    }
    val sampleWpPortfolioName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "WordPress 作品集"
        AppLanguage.ENGLISH -> "WordPress Portfolio"
        AppLanguage.ARABIC -> "معرض أعمال WordPress"
        AppLanguage.PORTUGUESE -> "Portfólio WordPress"
        AppLanguage.SPANISH -> "Portafolio WordPress"
        AppLanguage.FRENCH -> "Portfolio WordPress"
        AppLanguage.GERMAN -> "WordPress-Portfolio"
        AppLanguage.RUSSIAN -> "Портфолио WordPress"
        AppLanguage.JAPANESE -> "WordPress ポートフォリオ"
        AppLanguage.KOREAN -> "WordPress 포트폴리오"
    }
    val sampleWpPortfolioDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "WordPress 作品集/企业站点，响应式布局与自定义页面"
        AppLanguage.ENGLISH -> "WordPress portfolio/business site with responsive layout"
        AppLanguage.ARABIC -> "موقع معرض أعمال WordPress بتصميم متجاوب"
        AppLanguage.PORTUGUESE -> "Site portfólio/empresarial WordPress com layout responsivo"
        AppLanguage.SPANISH -> "Sitio portafolio/empresarial WordPress con diseño responsivo"
        AppLanguage.FRENCH -> "Site portfolio/entreprise WordPress avec mise en page responsive"
        AppLanguage.GERMAN -> "WordPress-Portfolio-/Business-Site mit responsivem Layout"
        AppLanguage.RUSSIAN -> "Сайт-портфолио/корпоративный сайт WordPress с адаптивной вёрсткой"
        AppLanguage.JAPANESE -> "WordPress ポートフォリオ/ビジネスサイト、レスポンシブレイアウト"
        AppLanguage.KOREAN -> "WordPress 포트폴리오/비즈니스 사이트, 반응형 레이아웃"
    }
    val sampleTagBlog: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "博客"
        AppLanguage.ENGLISH -> "Blog"
        AppLanguage.ARABIC -> "مدونة"
        AppLanguage.PORTUGUESE -> "Blog"
        AppLanguage.SPANISH -> "Blog"
        AppLanguage.FRENCH -> "Blog"
        AppLanguage.GERMAN -> "Blog"
        AppLanguage.RUSSIAN -> "Блог"
        AppLanguage.JAPANESE -> "ブログ"
        AppLanguage.KOREAN -> "블로그"
    }
    val sampleTagEcommerce: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "电商"
        AppLanguage.ENGLISH -> "E-Commerce"
        AppLanguage.ARABIC -> "متجر"
        AppLanguage.PORTUGUESE -> "E-commerce"
        AppLanguage.SPANISH -> "E-commerce"
        AppLanguage.FRENCH -> "E-commerce"
        AppLanguage.GERMAN -> "E-Commerce"
        AppLanguage.RUSSIAN -> "E-commerce"
        AppLanguage.JAPANESE -> "EC"
        AppLanguage.KOREAN -> "이커머스"
    }
    val sampleTagPortfolio: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "作品集"
        AppLanguage.ENGLISH -> "Portfolio"
        AppLanguage.ARABIC -> "معرض أعمال"
        AppLanguage.PORTUGUESE -> "Portfólio"
        AppLanguage.SPANISH -> "Portafolio"
        AppLanguage.FRENCH -> "Portfolio"
        AppLanguage.GERMAN -> "Portfolio"
        AppLanguage.RUSSIAN -> "Портфолио"
        AppLanguage.JAPANESE -> "ポートフォリオ"
        AppLanguage.KOREAN -> "포트폴리오"
    }
    val sampleTagResponsive: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "响应式"
        AppLanguage.ENGLISH -> "Responsive"
        AppLanguage.ARABIC -> "متجاوب"
        AppLanguage.PORTUGUESE -> "Responsivo"
        AppLanguage.SPANISH -> "Responsivo"
        AppLanguage.FRENCH -> "Adaptatif"
        AppLanguage.GERMAN -> "Responsiv"
        AppLanguage.RUSSIAN -> "Адаптивный"
        AppLanguage.JAPANESE -> "レスポンシブ"
        AppLanguage.KOREAN -> "반응형"
    }
    val sampleTagSqlite: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "SQLite"
        AppLanguage.ENGLISH -> "SQLite"
        AppLanguage.ARABIC -> "SQLite"
        AppLanguage.PORTUGUESE -> "SQLite"
        AppLanguage.SPANISH -> "SQLite"
        AppLanguage.FRENCH -> "SQLite"
        AppLanguage.GERMAN -> "SQLite"
        AppLanguage.RUSSIAN -> "SQLite"
        AppLanguage.JAPANESE -> "SQLite"
        AppLanguage.KOREAN -> "SQLite"
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

