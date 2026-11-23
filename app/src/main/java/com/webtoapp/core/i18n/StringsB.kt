package com.webtoapp.core.i18n

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

object StringsB {
    val apkAnalysisTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "APK 组成分析"
        AppLanguage.ENGLISH -> "APK Analysis"
        AppLanguage.ARABIC -> "تحليل حزمة APK"
        AppLanguage.PORTUGUESE -> "Análise do APK"
        AppLanguage.SPANISH -> "Análisis del APK"
        AppLanguage.FRENCH -> "Analyse de l'APK"
        AppLanguage.GERMAN -> "APK-Analyse"
        AppLanguage.RUSSIAN -> "Анализ APK"
        AppLanguage.JAPANESE -> "APK 構成分析"
        AppLanguage.KOREAN -> "APK 구성 분석"
    }

    val buildSummaryAppSize: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "体积"
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

    val buildSummaryVersion: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "版本号"
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

    val buildSummaryJdk: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "JDK"
        AppLanguage.ENGLISH -> "JDK"
        AppLanguage.ARABIC -> "JDK"
        AppLanguage.PORTUGUESE -> "JDK"
        AppLanguage.SPANISH -> "JDK"
        AppLanguage.FRENCH -> "JDK"
        AppLanguage.GERMAN -> "JDK"
        AppLanguage.RUSSIAN -> "JDK"
        AppLanguage.JAPANESE -> "JDK"
        AppLanguage.KOREAN -> "JDK"
    }

    val buildSummarySignature: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "签名"
        AppLanguage.ENGLISH -> "Signature"
        AppLanguage.ARABIC -> "التوقيع"
        AppLanguage.PORTUGUESE -> "Assinatura"
        AppLanguage.SPANISH -> "Firma"
        AppLanguage.FRENCH -> "Signature"
        AppLanguage.GERMAN -> "Signatur"
        AppLanguage.RUSSIAN -> "Подпись"
        AppLanguage.JAPANESE -> "署名"
        AppLanguage.KOREAN -> "서명"
    }

    val buildSummaryLocation: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "安装包目录"
        AppLanguage.ENGLISH -> "APK Location"
        AppLanguage.ARABIC -> "موقع APK"
        AppLanguage.PORTUGUESE -> "Local do APK"
        AppLanguage.SPANISH -> "Ubicación del APK"
        AppLanguage.FRENCH -> "Emplacement de l'APK"
        AppLanguage.GERMAN -> "APK-Speicherort"
        AppLanguage.RUSSIAN -> "Расположение APK"
        AppLanguage.JAPANESE -> "APK の場所"
        AppLanguage.KOREAN -> "APK 위치"
    }

    val buildSummaryOpenWith: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "分享"
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

    val buildSummaryCopyPath: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "复制路径"
        AppLanguage.ENGLISH -> "Copy path"
        AppLanguage.ARABIC -> "نسخ المسار"
        AppLanguage.PORTUGUESE -> "Copiar caminho"
        AppLanguage.SPANISH -> "Copiar ruta"
        AppLanguage.FRENCH -> "Copier le chemin"
        AppLanguage.GERMAN -> "Pfad kopieren"
        AppLanguage.RUSSIAN -> "Копировать путь"
        AppLanguage.JAPANESE -> "パスをコピー"
        AppLanguage.KOREAN -> "경로 복사"
    }

    val clearAll: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "清空"
        AppLanguage.ENGLISH -> "Clear"
        AppLanguage.ARABIC -> "مسح"
        AppLanguage.PORTUGUESE -> "Limpar"
        AppLanguage.SPANISH -> "Limpiar"
        AppLanguage.FRENCH -> "Effacer"
        AppLanguage.GERMAN -> "Leeren"
        AppLanguage.RUSSIAN -> "Очистить"
        AppLanguage.JAPANESE -> "クリア"
        AppLanguage.KOREAN -> "모두 지우기"
    }

    val doneWithCount: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "完成 (%d)"
        AppLanguage.ENGLISH -> "Done (%d)"
        AppLanguage.ARABIC -> "تم (%d)"
        AppLanguage.PORTUGUESE -> "Concluído (%d)"
        AppLanguage.SPANISH -> "Listo (%d)"
        AppLanguage.FRENCH -> "Terminé (%d)"
        AppLanguage.GERMAN -> "Fertig (%d)"
        AppLanguage.RUSSIAN -> "Готово (%d)"
        AppLanguage.JAPANESE -> "完了 (%d)"
        AppLanguage.KOREAN -> "완료 (%d)"
    }

    val searchModulesHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "搜索名称或描述..."
        AppLanguage.ENGLISH -> "Search name or description..."
        AppLanguage.ARABIC -> "البحث عن الاسم أو الوصف..."
        AppLanguage.PORTUGUESE -> "Pesquisar nome ou descrição..."
        AppLanguage.SPANISH -> "Buscar nombre o descripción..."
        AppLanguage.FRENCH -> "Rechercher nom ou description..."
        AppLanguage.GERMAN -> "Name oder Beschreibung suchen..."
        AppLanguage.RUSSIAN -> "Поиск по имени или описанию..."
        AppLanguage.JAPANESE -> "名前または説明を検索..."
        AppLanguage.KOREAN -> "이름 또는 설명 검색..."
    }

    val saveAsSchemeTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "保存为方案"
        AppLanguage.ENGLISH -> "Save as Scheme"
        AppLanguage.ARABIC -> "حفظ كمخطط"
        AppLanguage.PORTUGUESE -> "Salvar como Esquema"
        AppLanguage.SPANISH -> "Guardar como Esquema"
        AppLanguage.FRENCH -> "Enregistrer comme Schéma"
        AppLanguage.GERMAN -> "Als Schema speichern"
        AppLanguage.RUSSIAN -> "Сохранить как схему"
        AppLanguage.JAPANESE -> "スキーマとして保存"
        AppLanguage.KOREAN -> "스키마로 저장"
    }

    val schemeName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "方案名称"
        AppLanguage.ENGLISH -> "Scheme Name"
        AppLanguage.ARABIC -> "اسم المخطط"
        AppLanguage.PORTUGUESE -> "Nome do Esquema"
        AppLanguage.SPANISH -> "Nombre del Esquema"
        AppLanguage.FRENCH -> "Nom du Schéma"
        AppLanguage.GERMAN -> "Schema-Name"
        AppLanguage.RUSSIAN -> "Имя схемы"
        AppLanguage.JAPANESE -> "スキーマ名"
        AppLanguage.KOREAN -> "스키마 이름"
    }

    val inputSchemeName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "输入方案名称"
        AppLanguage.ENGLISH -> "Enter scheme name"
        AppLanguage.ARABIC -> "أدخل اسم المخطط"
        AppLanguage.PORTUGUESE -> "Digite o nome do esquema"
        AppLanguage.SPANISH -> "Ingrese el nombre del esquema"
        AppLanguage.FRENCH -> "Saisir le nom du schéma"
        AppLanguage.GERMAN -> "Schemanamen eingeben"
        AppLanguage.RUSSIAN -> "Введите имя схемы"
        AppLanguage.JAPANESE -> "スキーマ名を入力"
        AppLanguage.KOREAN -> "스키마 이름 입력"
    }

    val selectIcon: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Select icon"
        AppLanguage.ENGLISH -> "Select Icon"
        AppLanguage.ARABIC -> "اختيار أيقونة"
        AppLanguage.PORTUGUESE -> "Selecionar Ícone"
        AppLanguage.SPANISH -> "Seleccionar Icono"
        AppLanguage.FRENCH -> "Sélectionner l'Icône"
        AppLanguage.GERMAN -> "Symbol auswählen"
        AppLanguage.RUSSIAN -> "Выбрать значок"
        AppLanguage.JAPANESE -> "アイコンを選択"
        AppLanguage.KOREAN -> "아이콘 선택"
    }

    val exportData: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "导出数据"
        AppLanguage.ENGLISH -> "Export Data"
        AppLanguage.ARABIC -> "تصدير البيانات"
        AppLanguage.PORTUGUESE -> "Exportar Dados"
        AppLanguage.SPANISH -> "Exportar Datos"
        AppLanguage.FRENCH -> "Exporter les Données"
        AppLanguage.GERMAN -> "Daten exportieren"
        AppLanguage.RUSSIAN -> "Экспортировать данные"
        AppLanguage.JAPANESE -> "データをエクスポート"
        AppLanguage.KOREAN -> "데이터 내보내기"
    }

    val importData: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "导入数据"
        AppLanguage.ENGLISH -> "Import Data"
        AppLanguage.ARABIC -> "استيراد البيانات"
        AppLanguage.PORTUGUESE -> "Importar Dados"
        AppLanguage.SPANISH -> "Importar Datos"
        AppLanguage.FRENCH -> "Importer les Données"
        AppLanguage.GERMAN -> "Daten importieren"
        AppLanguage.RUSSIAN -> "Импортировать данные"
        AppLanguage.JAPANESE -> "データをインポート"
        AppLanguage.KOREAN -> "데이터 가져오기"
    }

    val launchTime: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "启动时间"
        AppLanguage.ENGLISH -> "Launch Time"
        AppLanguage.ARABIC -> "وقت التشغيل"
        AppLanguage.PORTUGUESE -> "Hora de Inicialização"
        AppLanguage.SPANISH -> "Hora de Inicio"
        AppLanguage.FRENCH -> "Heure de Lancement"
        AppLanguage.GERMAN -> "Startzeit"
        AppLanguage.RUSSIAN -> "Время запуска"
        AppLanguage.JAPANESE -> "起動時間"
        AppLanguage.KOREAN -> "실행 시간"
    }

    val selectLaunchTime: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择启动时间"
        AppLanguage.ENGLISH -> "Select Launch Time"
        AppLanguage.ARABIC -> "اختيار وقت التشغيل"
        AppLanguage.PORTUGUESE -> "Selecionar Hora de Inicialização"
        AppLanguage.SPANISH -> "Seleccionar Hora de Inicio"
        AppLanguage.FRENCH -> "Sélectionner l'Heure de Lancement"
        AppLanguage.GERMAN -> "Startzeit auswählen"
        AppLanguage.RUSSIAN -> "Выбрать время запуска"
        AppLanguage.JAPANESE -> "起動時間を選択"
        AppLanguage.KOREAN -> "실행 시간 선택"
    }

    val refresh: String get() = when (Strings.lang) {
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

    val goBack: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "后退"
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

    val goForward: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "前进"
        AppLanguage.ENGLISH -> "Forward"
        AppLanguage.ARABIC -> "إلى الأمام"
        AppLanguage.PORTUGUESE -> "Avançar"
        AppLanguage.SPANISH -> "Adelante"
        AppLanguage.FRENCH -> "Avancer"
        AppLanguage.GERMAN -> "Vorwärts"
        AppLanguage.RUSSIAN -> "Вперёд"
        AppLanguage.JAPANESE -> "進む"
        AppLanguage.KOREAN -> "앞으로"
    }

    val code: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "代码"
        AppLanguage.ENGLISH -> "Code"
        AppLanguage.ARABIC -> "الكود"
        AppLanguage.PORTUGUESE -> "Código"
        AppLanguage.SPANISH -> "Código"
        AppLanguage.FRENCH -> "Code"
        AppLanguage.GERMAN -> "Code"
        AppLanguage.RUSSIAN -> "Код"
        AppLanguage.JAPANESE -> "コード"
        AppLanguage.KOREAN -> "코드"
    }

    val advancedSettings: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "高级设置"
        AppLanguage.ENGLISH -> "Advanced Settings"
        AppLanguage.ARABIC -> "الإعدادات المتقدمة"
        AppLanguage.PORTUGUESE -> "Configurações Avançadas"
        AppLanguage.SPANISH -> "Configuración Avanzada"
        AppLanguage.FRENCH -> "Paramètres Avancés"
        AppLanguage.GERMAN -> "Erweiterte Einstellungen"
        AppLanguage.RUSSIAN -> "Расширенные настройки"
        AppLanguage.JAPANESE -> "詳細設定"
        AppLanguage.KOREAN -> "고급 설정"
    }

    val configTemplates: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "配置模板"
        AppLanguage.ENGLISH -> "Config Templates"
        AppLanguage.ARABIC -> "قوالب الإعداد"
        AppLanguage.PORTUGUESE -> "Modelos de Configuração"
        AppLanguage.SPANISH -> "Plantillas de Configuración"
        AppLanguage.FRENCH -> "Modèles de Configuration"
        AppLanguage.GERMAN -> "Konfigurationsvorlagen"
        AppLanguage.RUSSIAN -> "Шаблоны конфигурации"
        AppLanguage.JAPANESE -> "設定テンプレート"
        AppLanguage.KOREAN -> "구성 템플릿"
    }

    val configTemplatesDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "把当前通用配置保存为模板，创建或编辑任何应用时一键套用"
        AppLanguage.ENGLISH -> "Save the current common config as a template and apply it to any app in one tap"
        AppLanguage.ARABIC -> "احفظ الإعداد العام الحالي كقالب وطبّقه على أي تطبيق بلمسة واحدة"
        AppLanguage.PORTUGUESE -> "Salve a configuração comum atual como modelo e aplique-a a qualquer app com um toque"
        AppLanguage.SPANISH -> "Guarde la configuración común actual como plantilla y aplíquela a cualquier app con un toque"
        AppLanguage.FRENCH -> "Enregistrez la configuration commune actuelle comme modèle et appliquez-le à n'importe quelle app en un geste"
        AppLanguage.GERMAN -> "Speichere die aktuelle gemeinsame Konfiguration als Vorlage und wende sie mit einem Tipp auf jede App an"
        AppLanguage.RUSSIAN -> "Сохраните текущую общую конфигурацию как шаблон и применяйте её к любому приложению одним нажатием"
        AppLanguage.JAPANESE -> "現在の共通設定をテンプレートとして保存し、任意のアプリにワンタップで適用できます"
        AppLanguage.KOREAN -> "현재 공통 구성을 템플릿으로 저장하고 원하는 앱에 한 번에 적용하세요"
    }

    val templateSaveAs: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "保存为模板"
        AppLanguage.ENGLISH -> "Save as template"
        AppLanguage.ARABIC -> "حفظ كقالب"
        AppLanguage.PORTUGUESE -> "Salvar como modelo"
        AppLanguage.SPANISH -> "Guardar como plantilla"
        AppLanguage.FRENCH -> "Enregistrer comme modèle"
        AppLanguage.GERMAN -> "Als Vorlage speichern"
        AppLanguage.RUSSIAN -> "Сохранить как шаблон"
        AppLanguage.JAPANESE -> "テンプレートとして保存"
        AppLanguage.KOREAN -> "템플릿으로 저장"
    }

    val templateNameLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "模板名称"
        AppLanguage.ENGLISH -> "Template name"
        AppLanguage.ARABIC -> "اسم القالب"
        AppLanguage.PORTUGUESE -> "Nome do modelo"
        AppLanguage.SPANISH -> "Nombre de la plantilla"
        AppLanguage.FRENCH -> "Nom du modèle"
        AppLanguage.GERMAN -> "Vorlagenname"
        AppLanguage.RUSSIAN -> "Название шаблона"
        AppLanguage.JAPANESE -> "テンプレート名"
        AppLanguage.KOREAN -> "템플릿 이름"
    }

    val templateApplyHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "点击模板名称，将其套用到当前应用（整体替换通用配置）："
        AppLanguage.ENGLISH -> "Tap a template name to apply it to the current app (replaces the whole common config):"
        AppLanguage.ARABIC -> "اضغط على اسم القالب لتطبيقه على التطبيق الحالي (يستبدل الإعداد العام بالكامل):"
        AppLanguage.PORTUGUESE -> "Toque no nome de um modelo para aplicá-lo ao app atual (substitui toda a configuração comum):"
        AppLanguage.SPANISH -> "Toque el nombre de una plantilla para aplicarla a la app actual (reemplaza toda la configuración común):"
        AppLanguage.FRENCH -> "Touchez le nom d'un modèle pour l'appliquer à l'app actuelle (remplace toute la configuration commune) :"
        AppLanguage.GERMAN -> "Tippen Sie auf einen Vorlagennamen, um ihn auf die aktuelle App anzuwenden (ersetzt die gesamte gemeinsame Konfiguration):"
        AppLanguage.RUSSIAN -> "Нажмите на название шаблона, чтобы применить его к текущему приложению (заменяет всю общую конфигурацию):"
        AppLanguage.JAPANESE -> "テンプレート名をタップすると現在のアプリに適用されます（共通設定をすべて置き換えます）："
        AppLanguage.KOREAN -> "템플릿 이름을 탭하면 현재 앱에 적용됩니다(공통 구성 전체를 교체):"
    }

    val templateManage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "管理模板"
        AppLanguage.ENGLISH -> "Manage templates"
        AppLanguage.ARABIC -> "إدارة القوالب"
        AppLanguage.PORTUGUESE -> "Gerenciar modelos"
        AppLanguage.SPANISH -> "Gestionar plantillas"
        AppLanguage.FRENCH -> "Gérer les modèles"
        AppLanguage.GERMAN -> "Vorlagen verwalten"
        AppLanguage.RUSSIAN -> "Управление шаблонами"
        AppLanguage.JAPANESE -> "テンプレートを管理"
        AppLanguage.KOREAN -> "템플릿 관리"
    }

    val templateRename: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "重命名模板"
        AppLanguage.ENGLISH -> "Rename template"
        AppLanguage.ARABIC -> "إعادة تسمية القالب"
        AppLanguage.PORTUGUESE -> "Renomear modelo"
        AppLanguage.SPANISH -> "Renombrar plantilla"
        AppLanguage.FRENCH -> "Renommer le modèle"
        AppLanguage.GERMAN -> "Vorlage umbenennen"
        AppLanguage.RUSSIAN -> "Переименовать шаблон"
        AppLanguage.JAPANESE -> "テンプレートの名前を変更"
        AppLanguage.KOREAN -> "템플릿 이름 바꾸기"
    }

    val templateEmpty: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "暂无模板"
        AppLanguage.ENGLISH -> "No templates yet"
        AppLanguage.ARABIC -> "لا توجد قوالب بعد"
        AppLanguage.PORTUGUESE -> "Ainda não há modelos"
        AppLanguage.SPANISH -> "Aún no hay plantillas"
        AppLanguage.FRENCH -> "Aucun modèle pour le moment"
        AppLanguage.GERMAN -> "Noch keine Vorlagen"
        AppLanguage.RUSSIAN -> "Шаблонов пока нет"
        AppLanguage.JAPANESE -> "テンプレートはまだありません"
        AppLanguage.KOREAN -> "아직 템플릿이 없습니다"
    }

    val templateOverwriteHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "同名模板已存在，保存将覆盖它"
        AppLanguage.ENGLISH -> "A template with this name exists; saving will overwrite it"
        AppLanguage.ARABIC -> "يوجد قالب بهذا الاسم؛ الحفظ سيستبدله"
        AppLanguage.PORTUGUESE -> "Já existe um modelo com este nome; salvar o substituirá"
        AppLanguage.SPANISH -> "Ya existe una plantilla con este nombre; guardar la sobrescribirá"
        AppLanguage.FRENCH -> "Un modèle portant ce nom existe déjà ; l'enregistrer le remplacera"
        AppLanguage.GERMAN -> "Eine Vorlage mit diesem Namen existiert bereits; Speichern überschreibt sie"
        AppLanguage.RUSSIAN -> "Шаблон с таким названием уже существует; сохранение перезапишет его"
        AppLanguage.JAPANESE -> "同じ名前のテンプレートが存在します。保存すると上書きされます"
        AppLanguage.KOREAN -> "같은 이름의 템플릿이 있습니다. 저장하면 덮어씁니다"
    }

    fun templateApplied(name: String): String = when (Strings.lang) {
        AppLanguage.CHINESE -> "已套用模板「$name」"
        AppLanguage.ENGLISH -> "Applied template \"$name\""
        AppLanguage.ARABIC -> "تم تطبيق القالب \"$name\""
        AppLanguage.PORTUGUESE -> "Modelo \"$name\" aplicado"
        AppLanguage.SPANISH -> "Plantilla \"$name\" aplicada"
        AppLanguage.FRENCH -> "Modèle \"$name\" appliqué"
        AppLanguage.GERMAN -> "Vorlage \"$name\" angewendet"
        AppLanguage.RUSSIAN -> "Шаблон \"$name\" применён"
        AppLanguage.JAPANESE -> "テンプレート「$name」を適用しました"
        AppLanguage.KOREAN -> "템플릿 \"$name\"을(를) 적용했습니다"
    }

    fun templateSaved(name: String): String = when (Strings.lang) {
        AppLanguage.CHINESE -> "模板「$name」已保存"
        AppLanguage.ENGLISH -> "Template \"$name\" saved"
        AppLanguage.ARABIC -> "تم حفظ القالب \"$name\""
        AppLanguage.PORTUGUESE -> "Modelo \"$name\" salvo"
        AppLanguage.SPANISH -> "Plantilla \"$name\" guardada"
        AppLanguage.FRENCH -> "Modèle \"$name\" enregistré"
        AppLanguage.GERMAN -> "Vorlage \"$name\" gespeichert"
        AppLanguage.RUSSIAN -> "Шаблон \"$name\" сохранён"
        AppLanguage.JAPANESE -> "テンプレート「$name」を保存しました"
        AppLanguage.KOREAN -> "템플릿 \"$name\"을(를) 저장했습니다"
    }

    val templateExportCurrent: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "导出当前配置"
        AppLanguage.ENGLISH -> "Export current config"
        AppLanguage.ARABIC -> "تصدير الإعداد الحالي"
        AppLanguage.PORTUGUESE -> "Exportar configuração atual"
        AppLanguage.SPANISH -> "Exportar configuración actual"
        AppLanguage.FRENCH -> "Exporter la configuration actuelle"
        AppLanguage.GERMAN -> "Aktuelle Konfiguration exportieren"
        AppLanguage.RUSSIAN -> "Экспорт текущей конфигурации"
        AppLanguage.JAPANESE -> "現在の設定を書き出す"
        AppLanguage.KOREAN -> "현재 구성 내보내기"
    }

    val templateImport: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "导入配置"
        AppLanguage.ENGLISH -> "Import config"
        AppLanguage.ARABIC -> "استيراد إعداد"
        AppLanguage.PORTUGUESE -> "Importar configuração"
        AppLanguage.SPANISH -> "Importar configuración"
        AppLanguage.FRENCH -> "Importer une configuration"
        AppLanguage.GERMAN -> "Konfiguration importieren"
        AppLanguage.RUSSIAN -> "Импорт конфигурации"
        AppLanguage.JAPANESE -> "設定を読み込む"
        AppLanguage.KOREAN -> "구성 가져오기"
    }

    val templateExport: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "导出"
        AppLanguage.ENGLISH -> "Export"
        AppLanguage.ARABIC -> "تصدير"
        AppLanguage.PORTUGUESE -> "Exportar"
        AppLanguage.SPANISH -> "Exportar"
        AppLanguage.FRENCH -> "Exporter"
        AppLanguage.GERMAN -> "Exportieren"
        AppLanguage.RUSSIAN -> "Экспорт"
        AppLanguage.JAPANESE -> "書き出す"
        AppLanguage.KOREAN -> "내보내기"
    }

    val templateExported: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "配置已导出"
        AppLanguage.ENGLISH -> "Config exported"
        AppLanguage.ARABIC -> "تم تصدير الإعداد"
        AppLanguage.PORTUGUESE -> "Configuração exportada"
        AppLanguage.SPANISH -> "Configuración exportada"
        AppLanguage.FRENCH -> "Configuration exportée"
        AppLanguage.GERMAN -> "Konfiguration exportiert"
        AppLanguage.RUSSIAN -> "Конфигурация экспортирована"
        AppLanguage.JAPANESE -> "設定を書き出しました"
        AppLanguage.KOREAN -> "구성을 내보냈습니다"
    }

    val templateExportFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "导出配置失败"
        AppLanguage.ENGLISH -> "Could not export the config"
        AppLanguage.ARABIC -> "تعذر تصدير الإعداد"
        AppLanguage.PORTUGUESE -> "Não foi possível exportar a configuração"
        AppLanguage.SPANISH -> "No se pudo exportar la configuración"
        AppLanguage.FRENCH -> "Impossible d'exporter la configuration"
        AppLanguage.GERMAN -> "Konfiguration konnte nicht exportiert werden"
        AppLanguage.RUSSIAN -> "Не удалось экспортировать конфигурацию"
        AppLanguage.JAPANESE -> "設定を書き出せませんでした"
        AppLanguage.KOREAN -> "구성을 내보내지 못했습니다"
    }

    val templateImportFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "无法读取这份配置"
        AppLanguage.ENGLISH -> "Could not read this config"
        AppLanguage.ARABIC -> "تعذر قراءة هذا الإعداد"
        AppLanguage.PORTUGUESE -> "Não foi possível ler esta configuração"
        AppLanguage.SPANISH -> "No se pudo leer esta configuración"
        AppLanguage.FRENCH -> "Impossible de lire cette configuration"
        AppLanguage.GERMAN -> "Diese Konfiguration konnte nicht gelesen werden"
        AppLanguage.RUSSIAN -> "Не удалось прочитать эту конфигурацию"
        AppLanguage.JAPANESE -> "この設定を読み取れませんでした"
        AppLanguage.KOREAN -> "이 구성을 읽지 못했습니다"
    }

    fun templateImported(name: String): String = when (Strings.lang) {
        AppLanguage.CHINESE -> "已导入并套用「$name」"
        AppLanguage.ENGLISH -> "Imported and applied \"$name\""
        AppLanguage.ARABIC -> "تم استيراد \"$name\" وتطبيقه"
        AppLanguage.PORTUGUESE -> "\"$name\" importado e aplicado"
        AppLanguage.SPANISH -> "\"$name\" importada y aplicada"
        AppLanguage.FRENCH -> "\"$name\" importé et appliqué"
        AppLanguage.GERMAN -> "\"$name\" importiert und angewendet"
        AppLanguage.RUSSIAN -> "\"$name\" импортирован и применён"
        AppLanguage.JAPANESE -> "「$name」を読み込んで適用しました"
        AppLanguage.KOREAN -> "\"$name\"을(를) 가져와 적용했습니다"
    }

    val sectionWebEngine: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "引擎与显示"
        AppLanguage.ENGLISH -> "Engine & Display"
        AppLanguage.ARABIC -> "المحرك والعرض"
        AppLanguage.PORTUGUESE -> "Motor & Exibição"
        AppLanguage.SPANISH -> "Motor & Visualización"
        AppLanguage.FRENCH -> "Moteur & Affichage"
        AppLanguage.GERMAN -> "Engine & Anzeige"
        AppLanguage.RUSSIAN -> "Движок и отображение"
        AppLanguage.JAPANESE -> "エンジンと表示"
        AppLanguage.KOREAN -> "엔진 및 표시"
    }

    val sectionNavigation: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "导航与交互"
        AppLanguage.ENGLISH -> "Navigation & Interaction"
        AppLanguage.ARABIC -> "التنقل والتفاعل"
        AppLanguage.PORTUGUESE -> "Navegação & Interação"
        AppLanguage.SPANISH -> "Navegación & Interacción"
        AppLanguage.FRENCH -> "Navigation et interaction"
        AppLanguage.GERMAN -> "Navigation & Interaktion"
        AppLanguage.RUSSIAN -> "Навигация и взаимодействие"
        AppLanguage.JAPANESE -> "ナビゲーションと操作"
        AppLanguage.KOREAN -> "내비게이션 및 상호작용"
    }

    val sectionOfflinePerformance: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "网络与离线"
        AppLanguage.ENGLISH -> "Network & Offline"
        AppLanguage.ARABIC -> "الشبكة وعدم الاتصال"
        AppLanguage.PORTUGUESE -> "Rede & Offline"
        AppLanguage.SPANISH -> "Red & Sin Conexión"
        AppLanguage.FRENCH -> "Réseau & Hors Ligne"
        AppLanguage.GERMAN -> "Netzwerk & Offline"
        AppLanguage.RUSSIAN -> "Сеть и офлайн"
        AppLanguage.JAPANESE -> "ネットワークとオフライン"
        AppLanguage.KOREAN -> "네트워크 및 오프라인"
    }

    val sectionDeveloper: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "开发者工具"
        AppLanguage.ENGLISH -> "Developer"
        AppLanguage.ARABIC -> "المطور"
        AppLanguage.PORTUGUESE -> "Desenvolvedor"
        AppLanguage.SPANISH -> "Desarrollador"
        AppLanguage.FRENCH -> "Développeur"
        AppLanguage.GERMAN -> "Entwickler"
        AppLanguage.RUSSIAN -> "Разработчик"
        AppLanguage.JAPANESE -> "開発者"
        AppLanguage.KOREAN -> "개발자"
    }

    val sectionBrowserIdentity: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "浏览器身份"
        AppLanguage.ENGLISH -> "Browser Identity"
        AppLanguage.ARABIC -> "هوية المتصفح"
        AppLanguage.PORTUGUESE -> "Identidade do Navegador"
        AppLanguage.SPANISH -> "Identidad del navegador"
        AppLanguage.FRENCH -> "Identité du navigateur"
        AppLanguage.GERMAN -> "Browser-Identität"
        AppLanguage.RUSSIAN -> "Идентификация браузера"
        AppLanguage.JAPANESE -> "ブラウザ識別"
        AppLanguage.KOREAN -> "브라우저 신원"
    }

    val sectionOfflineCache: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "离线与缓存"
        AppLanguage.ENGLISH -> "Offline & Cache"
        AppLanguage.ARABIC -> "عدم الاتصال والتخزين المؤقت"
        AppLanguage.PORTUGUESE -> "Offline e Cache"
        AppLanguage.SPANISH -> "Sin conexión y caché"
        AppLanguage.FRENCH -> "Hors ligne et cache"
        AppLanguage.GERMAN -> "Offline & Cache"
        AppLanguage.RUSSIAN -> "Офлайн и кэш"
        AppLanguage.JAPANESE -> "オフラインとキャッシュ"
        AppLanguage.KOREAN -> "오프라인 및 캐시"
    }

    val sectionNetworkAccess: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "网络接入"
        AppLanguage.ENGLISH -> "Network Access"
        AppLanguage.ARABIC -> "الوصول إلى الشبكة"
        AppLanguage.PORTUGUESE -> "Acesso à Rede"
        AppLanguage.SPANISH -> "Acceso a la red"
        AppLanguage.FRENCH -> "Accès réseau"
        AppLanguage.GERMAN -> "Netzwerkzugang"
        AppLanguage.RUSSIAN -> "Доступ к сети"
        AppLanguage.JAPANESE -> "ネットワーク接続"
        AppLanguage.KOREAN -> "네트워크 접속"
    }

    val selectCategory: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择分类"
        AppLanguage.ENGLISH -> "Select Category"
        AppLanguage.ARABIC -> "اختيار الفئة"
        AppLanguage.PORTUGUESE -> "Selecionar Categoria"
        AppLanguage.SPANISH -> "Seleccionar Categoría"
        AppLanguage.FRENCH -> "Sélectionner la Catégorie"
        AppLanguage.GERMAN -> "Kategorie auswählen"
        AppLanguage.RUSSIAN -> "Выбрать категорию"
        AppLanguage.JAPANESE -> "カテゴリを選択"
        AppLanguage.KOREAN -> "카테고리 선택"
    }

    val sensitive: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "敏感"
        AppLanguage.ENGLISH -> "Sensitive"
        AppLanguage.ARABIC -> "حساس"
        AppLanguage.PORTUGUESE -> "Sensível"
        AppLanguage.SPANISH -> "Sensible"
        AppLanguage.FRENCH -> "Sensible"
        AppLanguage.GERMAN -> "Sensitiv"
        AppLanguage.RUSSIAN -> "Чувствительный"
        AppLanguage.JAPANESE -> "センシティブ"
        AppLanguage.KOREAN -> "민감"
    }

    val confirm: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "OK"
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

    val category: String get() = when (Strings.lang) {
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

    val urlMatchRules: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "URL match rules"
        AppLanguage.ENGLISH -> "URL Match Rules"
        AppLanguage.ARABIC -> "قواعد مطابقة URL"
        AppLanguage.PORTUGUESE -> "Regras de Correspondência de URL"
        AppLanguage.SPANISH -> "Reglas de Coincidencia de URL"
        AppLanguage.FRENCH -> "Règles de Correspondance d'URL"
        AppLanguage.GERMAN -> "URL-Übereinstimmungsregeln"
        AppLanguage.RUSSIAN -> "Правила совпадения URL"
        AppLanguage.JAPANESE -> "URL 一致ルール"
        AppLanguage.KOREAN -> "URL 일치 규칙"
    }

    val rulesCount: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "%d 条规则"
        AppLanguage.ENGLISH -> "%d rules"
        AppLanguage.ARABIC -> "%d قواعد"
        AppLanguage.PORTUGUESE -> "%d regras"
        AppLanguage.SPANISH -> "%d reglas"
        AppLanguage.FRENCH -> "%d règles"
        AppLanguage.GERMAN -> "%d Regeln"
        AppLanguage.RUSSIAN -> "%d правил"
        AppLanguage.JAPANESE -> "%d 件のルール"
        AppLanguage.KOREAN -> "%d개 규칙"
    }

    val regex: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "正则"
        AppLanguage.ENGLISH -> "Regex"
        AppLanguage.ARABIC -> "تعبير نمطي"
        AppLanguage.PORTUGUESE -> "Regex"
        AppLanguage.SPANISH -> "Regex"
        AppLanguage.FRENCH -> "Regex"
        AppLanguage.GERMAN -> "Regex"
        AppLanguage.RUSSIAN -> "Регекс"
        AppLanguage.JAPANESE -> "正規表現"
        AppLanguage.KOREAN -> "정규식"
    }

    val exclude: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "排除"
        AppLanguage.ENGLISH -> "Exclude"
        AppLanguage.ARABIC -> "استبعاد"
        AppLanguage.PORTUGUESE -> "Excluir"
        AppLanguage.SPANISH -> "Excluir"
        AppLanguage.FRENCH -> "Exclure"
        AppLanguage.GERMAN -> "Ausschließen"
        AppLanguage.RUSSIAN -> "Исключить"
        AppLanguage.JAPANESE -> "除外"
        AppLanguage.KOREAN -> "제외"
    }

    val include: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "包含"
        AppLanguage.ENGLISH -> "Include"
        AppLanguage.ARABIC -> "تضمين"
        AppLanguage.PORTUGUESE -> "Incluir"
        AppLanguage.SPANISH -> "Incluir"
        AppLanguage.FRENCH -> "Inclure"
        AppLanguage.GERMAN -> "Einschließen"
        AppLanguage.RUSSIAN -> "Включить"
        AppLanguage.JAPANESE -> "含める"
        AppLanguage.KOREAN -> "포함"
    }

    val description: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "描述"
        AppLanguage.ENGLISH -> "Description"
        AppLanguage.ARABIC -> "الوصف"
        AppLanguage.PORTUGUESE -> "Descrição"
        AppLanguage.SPANISH -> "Descripción"
        AppLanguage.FRENCH -> "Description"
        AppLanguage.GERMAN -> "Beschreibung"
        AppLanguage.RUSSIAN -> "Описание"
        AppLanguage.JAPANESE -> "説明"
        AppLanguage.KOREAN -> "설명"
    }

    val tags: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "标签"
        AppLanguage.ENGLISH -> "Tags"
        AppLanguage.ARABIC -> "العلامات"
        AppLanguage.PORTUGUESE -> "Etiquetas"
        AppLanguage.SPANISH -> "Etiquetas"
        AppLanguage.FRENCH -> "Étiquettes"
        AppLanguage.GERMAN -> "Schlagwörter"
        AppLanguage.RUSSIAN -> "Теги"
        AppLanguage.JAPANESE -> "タグ"
        AppLanguage.KOREAN -> "태그"
    }

    val author: String get() = when (Strings.lang) {
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

    val provider: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "供应商"
        AppLanguage.ENGLISH -> "Provider"
        AppLanguage.ARABIC -> "المزود"
        AppLanguage.PORTUGUESE -> "Fornecedor"
        AppLanguage.SPANISH -> "Proveedor"
        AppLanguage.FRENCH -> "Fournisseur"
        AppLanguage.GERMAN -> "Anbieter"
        AppLanguage.RUSSIAN -> "Поставщик"
        AppLanguage.JAPANESE -> "プロバイダー"
        AppLanguage.KOREAN -> "공급자"
    }

    val modelId: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "模型 ID"
        AppLanguage.ENGLISH -> "Model ID"
        AppLanguage.ARABIC -> "معرف النموذج"
        AppLanguage.PORTUGUESE -> "ID do Modelo"
        AppLanguage.SPANISH -> "ID del Modelo"
        AppLanguage.FRENCH -> "ID du Modèle"
        AppLanguage.GERMAN -> "Modell-ID"
        AppLanguage.RUSSIAN -> "ID модели"
        AppLanguage.JAPANESE -> "モデル ID"
        AppLanguage.KOREAN -> "모델 ID"
    }

    val aliasOptional: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "别名（可选）"
        AppLanguage.ENGLISH -> "Alias (optional)"
        AppLanguage.ARABIC -> "الاسم المستعار (اختياري)"
        AppLanguage.PORTUGUESE -> "Alias (opcional)"
        AppLanguage.SPANISH -> "Alias (opcional)"
        AppLanguage.FRENCH -> "Alias (facultatif)"
        AppLanguage.GERMAN -> "Optionaler Alias"
        AppLanguage.RUSSIAN -> "Псевдоним (необязательно)"
        AppLanguage.JAPANESE -> "エイリアス（任意）"
        AppLanguage.KOREAN -> "별칭 (선택)"
    }

    val alias: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "别名"
        AppLanguage.ENGLISH -> "Alias"
        AppLanguage.ARABIC -> "الاسم المستعار"
        AppLanguage.PORTUGUESE -> "Apelido"
        AppLanguage.SPANISH -> "Alias"
        AppLanguage.FRENCH -> "Alias"
        AppLanguage.GERMAN -> "Alias"
        AppLanguage.RUSSIAN -> "Псевдоним"
        AppLanguage.JAPANESE -> "エイリアス"
        AppLanguage.KOREAN -> "별칭"
    }

    val activationCode: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "激活码"
        AppLanguage.ENGLISH -> "Activation Code"
        AppLanguage.ARABIC -> "رمز التفعيل"
        AppLanguage.PORTUGUESE -> "Código de Ativação"
        AppLanguage.SPANISH -> "Código de Activación"
        AppLanguage.FRENCH -> "Code d'Activation"
        AppLanguage.GERMAN -> "Aktivierungscode"
        AppLanguage.RUSSIAN -> "Код активации"
        AppLanguage.JAPANESE -> "アクティベーションコード"
        AppLanguage.KOREAN -> "활성화 코드"
    }

    val customPackageName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自定义包名"
        AppLanguage.ENGLISH -> "Custom Package Name"
        AppLanguage.ARABIC -> "اسم الحزمة المخصص"
        AppLanguage.PORTUGUESE -> "Nome de Pacote Personalizado"
        AppLanguage.SPANISH -> "Nombre de Paquete Personalizado"
        AppLanguage.FRENCH -> "Nom de Package Personnalisé"
        AppLanguage.GERMAN -> "Benutzerdefinierter Paketname"
        AppLanguage.RUSSIAN -> "Пользовательское имя пакета"
        AppLanguage.JAPANESE -> "カスタムパッケージ名"
        AppLanguage.KOREAN -> "사용자 지정 패키지 이름"
    }

    val packageNameInvalidFormat: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "格式错误，应为小写字母开头，如：com.w2a.app"
        AppLanguage.ENGLISH -> "Invalid format, should start with lowercase letter, e.g.: com.w2a.app"
        AppLanguage.ARABIC -> "تنسيق غير صالح، يجب أن يبدأ بحرف صغير، مثال: com.w2a.app"
        AppLanguage.PORTUGUESE -> "Formato inválido, deve começar com letra minúscula, ex.: com.w2a.app"
        AppLanguage.SPANISH -> "Formato inválido, debe comenzar con letra minúscula, ej.: com.w2a.app"
        AppLanguage.FRENCH -> "Format invalide, doit commencer par une minuscule, ex. : com.w2a.app"
        AppLanguage.GERMAN -> "Ungültiges Format, muss mit Kleinbuchstabe beginnen, z. B.: com.w2a.app"
        AppLanguage.RUSSIAN -> "Неверный формат, должен начинаться со строчной буквы, напр.: com.w2a.app"
        AppLanguage.JAPANESE -> "無効な形式、小文字で始まる必要があります、例: com.w2a.app"
        AppLanguage.KOREAN -> "잘못된 형식, 소문자로 시작해야 합니다, 예: com.w2a.app"
    }

    val packageNameHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "留空自动生成，如：com.example.myapp"
        AppLanguage.ENGLISH -> "Leave empty for auto-generation, e.g.: com.example.myapp"
        AppLanguage.ARABIC -> "اتركه فارغًا للإنشاء التلقائي، مثال: com.example.myapp"
        AppLanguage.PORTUGUESE -> "Deixe vazio para geração automática, ex.: com.example.myapp"
        AppLanguage.SPANISH -> "Dejar vacío para autogenerar, ej.: com.example.myapp"
        AppLanguage.FRENCH -> "Laisser vide pour auto-génération, ex. : com.example.myapp"
        AppLanguage.GERMAN -> "Leer lassen für automatische Generierung, z. B.: com.example.myapp"
        AppLanguage.RUSSIAN -> "Оставьте пустым для автогенерации, напр.: com.example.myapp"
        AppLanguage.JAPANESE -> "空欄で自動生成、例: com.example.myapp"
        AppLanguage.KOREAN -> "비워두면 자동 생성, 예: com.example.myapp"
    }

    val apkPackageNamePlaceholder: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "com.example.myapp"
        AppLanguage.ENGLISH -> "com.example.myapp"
        AppLanguage.ARABIC -> "com.example.myapp"
        AppLanguage.PORTUGUESE -> "com.example.myapp"
        AppLanguage.SPANISH -> "com.example.myapp"
        AppLanguage.FRENCH -> "com.example.myapp"
        AppLanguage.GERMAN -> "com.example.myapp"
        AppLanguage.RUSSIAN -> "com.example.myapp"
        AppLanguage.JAPANESE -> "com.example.myapp"
        AppLanguage.KOREAN -> "com.example.myapp"
    }

    val versionName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "版本名"
        AppLanguage.ENGLISH -> "Version Name"
        AppLanguage.ARABIC -> "اسم الإصدار"
        AppLanguage.PORTUGUESE -> "Nome da Versão"
        AppLanguage.SPANISH -> "Nombre de Versión"
        AppLanguage.FRENCH -> "Nom de Version"
        AppLanguage.GERMAN -> "Versionsname"
        AppLanguage.RUSSIAN -> "Имя версии"
        AppLanguage.JAPANESE -> "バージョン名"
        AppLanguage.KOREAN -> "버전 이름"
    }

    val apkVersionNamePlaceholder: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "1.0.0"
        AppLanguage.ENGLISH -> "1.0.0"
        AppLanguage.ARABIC -> "1.0.0"
        AppLanguage.PORTUGUESE -> "1.0.0"
        AppLanguage.SPANISH -> "1.0.0"
        AppLanguage.FRENCH -> "1.0.0"
        AppLanguage.GERMAN -> "1.0.0"
        AppLanguage.RUSSIAN -> "1.0.0"
        AppLanguage.JAPANESE -> "1.0.0"
        AppLanguage.KOREAN -> "1.0.0"
    }

    val versionCode: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "版本号"
        AppLanguage.ENGLISH -> "Version Code"
        AppLanguage.ARABIC -> "رقم الإصدار"
        AppLanguage.PORTUGUESE -> "Código da Versão"
        AppLanguage.SPANISH -> "Código de Versión"
        AppLanguage.FRENCH -> "Code de Version"
        AppLanguage.GERMAN -> "Versionscode"
        AppLanguage.RUSSIAN -> "Код версии"
        AppLanguage.JAPANESE -> "バージョンコード"
        AppLanguage.KOREAN -> "버전 코드"
    }

    val apkVersionCodePlaceholder: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "1"
        AppLanguage.ENGLISH -> "1"
        AppLanguage.ARABIC -> "1"
        AppLanguage.PORTUGUESE -> "1"
        AppLanguage.SPANISH -> "1"
        AppLanguage.FRENCH -> "1"
        AppLanguage.GERMAN -> "1"
        AppLanguage.RUSSIAN -> "1"
        AppLanguage.JAPANESE -> "1"
        AppLanguage.KOREAN -> "1"
    }
    val saepPolicyTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "静态 SAEP 策略"
        AppLanguage.ENGLISH -> "Static SAEP policy"
        AppLanguage.ARABIC -> "سياسة SAEP ثابتة"
        AppLanguage.PORTUGUESE -> "Política SAEP estática"
        AppLanguage.SPANISH -> "Política SAEP estática"
        AppLanguage.FRENCH -> "Politique SAEP statique"
        AppLanguage.GERMAN -> "Statische SAEP-Richtlinie"
        AppLanguage.RUSSIAN -> "Статическая политика SAEP"
        AppLanguage.JAPANESE -> "静的 SAEP ポリシー"
        AppLanguage.KOREAN -> "정적 SAEP 정책"
    }

    val saepPolicyHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "可选：声明无额外限制。不授予权限，也不保证智能体支持。默认关闭：省略元数据，并非拒绝访问。"
        AppLanguage.ENGLISH -> "Optional: declares no additional restrictions. Does not grant permissions or guarantee agent support. Off by default: omits metadata, not a denial of access."
        AppLanguage.ARABIC -> "اختياري: يعلن عدم وجود قيود إضافية. لا يمنح أذونات ولا يضمن دعم الوكلاء. معطل افتراضيًا: تُحذف البيانات الوصفية، وليس رفضًا للوصول."
        AppLanguage.PORTUGUESE -> "Opcional: declara que não há restrições adicionais. Não concede permissões nem garante suporte de agentes. Desativado por padrão: omite metadados, não nega acesso."
        AppLanguage.SPANISH -> "Opcional: declara que no hay restricciones adicionales. No otorga permisos ni garantiza compatibilidad con agentes. Desactivado por defecto: omite metadatos, no deniega el acceso."
        AppLanguage.FRENCH -> "Facultatif : déclare aucune restriction supplémentaire. N'accorde aucune permission et ne garantit pas la prise en charge par les agents. Désactivé par défaut : omet les métadonnées, sans refuser l'accès."
        AppLanguage.GERMAN -> "Optional: erklärt, dass keine zusätzlichen Einschränkungen gelten. Erteilt keine Berechtigungen und garantiert keine Agentenunterstützung. Standardmäßig aus: Metadaten entfallen, kein Zugriffsverbot."
        AppLanguage.RUSSIAN -> "Необязательно: заявляет об отсутствии дополнительных ограничений. Не предоставляет разрешений и не гарантирует поддержку агентами. По умолчанию выключено: метаданные отсутствуют, это не запрет доступа."
        AppLanguage.JAPANESE -> "任意：追加の制限がないことを宣言します。権限を付与せず、エージェントの対応も保証しません。既定はオフ：メタデータを省略するだけで、アクセス拒否ではありません。"
        AppLanguage.KOREAN -> "선택 사항: 추가 제한이 없음을 선언합니다. 권한을 부여하거나 에이전트 지원을 보장하지 않습니다. 기본값은 꺼짐: 메타데이터를 생략하며 접근 거부를 뜻하지 않습니다."
    }

    val autoVersionBump: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自动提升版本号"
        AppLanguage.ENGLISH -> "Auto-bump version"
        AppLanguage.ARABIC -> "رفع الإصدار تلقائيًا"
        AppLanguage.PORTUGUESE -> "Elevar versão automaticamente"
        AppLanguage.SPANISH -> "Elevar versión automáticamente"
        AppLanguage.FRENCH -> "Élever la version automatiquement"
        AppLanguage.GERMAN -> "Version automatisch erhöhen"
        AppLanguage.RUSSIAN -> "Автоповышение версии"
        AppLanguage.JAPANESE -> "バージョンを自動で引き上げ"
        AppLanguage.KOREAN -> "버전 자동 올림"
    }

    val autoVersionBumpHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "设备已安装同包名且版本号更高的应用时，自动提升构建版本号以便覆盖安装。关闭后始终使用上方填写的版本。"
        AppLanguage.ENGLISH -> "When this package is already installed with a higher versionCode, raises the build's version so it can install. Turn off to always keep the version above."
        AppLanguage.ARABIC -> "عندما تكون هذه الحزمة مثبتة برقم إصدار أعلى، يتم رفع إصدار البناء ليتمكن من التثبيت. أوقفه لاستخدام الإصدار المحدد أعلاه دائمًا."
        AppLanguage.PORTUGUESE -> "Quando este pacote já está instalado com um versionCode maior, eleva a versão do build para poder instalar. Desative para sempre manter a versão acima."
        AppLanguage.SPANISH -> "Cuando este paquete ya está instalado con un versionCode mayor, eleva la versión del build para poder instalarla. Desactívalo para mantener siempre la versión de arriba."
        AppLanguage.FRENCH -> "Quand ce package est déjà installé avec un versionCode supérieur, augmente la version du build pour permettre l'installation. Désactivez pour toujours garder la version ci-dessus."
        AppLanguage.GERMAN -> "Ist dieses Paket bereits mit höherem versionCode installiert, erhöht sich die Build-Version, damit die Installation klappt. Deaktivieren, um immer die obige Version zu verwenden."
        AppLanguage.RUSSIAN -> "Если этот пакет уже установлен с более высоким versionCode, версия сборки повышается, чтобы обновление установилось. Отключите, чтобы всегда использовать указанную выше версию."
        AppLanguage.JAPANESE -> "このパッケージがより高い versionCode で既にインストールされている場合、インストールできるようビルドのバージョンを引き上げます。オフにすると常に上記のバージョンを使用します。"
        AppLanguage.KOREAN -> "이 패키지가 더 높은 versionCode로 이미 설치된 경우 설치할 수 있도록 빌드 버전을 올립니다. 끄면 항상 위에 지정한 버전을 사용합니다."
    }

    val translateTargetLanguage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "翻译目标语言"
        AppLanguage.ENGLISH -> "Translation Target Language"
        AppLanguage.ARABIC -> "لغة الترجمة المستهدفة"
        AppLanguage.PORTUGUESE -> "Idioma Alvo da Tradução"
        AppLanguage.SPANISH -> "Idioma Objetivo de Traducción"
        AppLanguage.FRENCH -> "Langue Cible de Traduction"
        AppLanguage.GERMAN -> "Übersetzungs-Zielsprache"
        AppLanguage.RUSSIAN -> "Целевой язык перевода"
        AppLanguage.JAPANESE -> "翻訳先言語"
        AppLanguage.KOREAN -> "번역 대상 언어"
    }

    val adBlockRuleHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "如：ads.example.com"
        AppLanguage.ENGLISH -> "e.g.: ads.example.com"
        AppLanguage.ARABIC -> "مثال: ads.example.com"
        AppLanguage.PORTUGUESE -> "ex.: ads.example.com"
        AppLanguage.SPANISH -> "ej.: ads.example.com"
        AppLanguage.FRENCH -> "ex. : ads.example.com"
        AppLanguage.GERMAN -> "z. B.: ads.example.com"
        AppLanguage.RUSSIAN -> "напр.: ads.example.com"
        AppLanguage.JAPANESE -> "例: ads.example.com"
        AppLanguage.KOREAN -> "예: ads.example.com"
    }

    val customBlockRules: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自定义拦截规则（可选）"
        AppLanguage.ENGLISH -> "Custom Block Rules (optional)"
        AppLanguage.ARABIC -> "قواعد الحظر المخصصة (اختياري)"
        AppLanguage.PORTUGUESE -> "Regras de Bloqueio Personalizadas (opcional)"
        AppLanguage.SPANISH -> "Reglas de Bloqueo Personalizadas (opcional)"
        AppLanguage.FRENCH -> "Règles de Blocage Personnalisées (facultatif)"
        AppLanguage.GERMAN -> "Benutzerdefinierte Blockierregeln (optional)"
        AppLanguage.RUSSIAN -> "Пользовательские правила блокировки (необязательно)"
        AppLanguage.JAPANESE -> "カスタムブロックルール（任意）"
        AppLanguage.KOREAN -> "사용자 지정 차단 규칙 (선택)"
    }

    val adBlockSelectSubscriptions: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择过滤规则源"
        AppLanguage.ENGLISH -> "Select Filter Lists"
        AppLanguage.ARABIC -> "اختر قوائم التصفية"
        AppLanguage.PORTUGUESE -> "Selecionar Listas de Filtro"
        AppLanguage.SPANISH -> "Seleccionar Listas de Filtros"
        AppLanguage.FRENCH -> "Sélectionner les Listes de Filtres"
        AppLanguage.GERMAN -> "Filterlisten auswählen"
        AppLanguage.RUSSIAN -> "Выбрать списки фильтров"
        AppLanguage.JAPANESE -> "フィルターリストを選択"
        AppLanguage.KOREAN -> "필터 목록 선택"
    }

    val adBlockSubscriptions: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "过滤规则源"
        AppLanguage.ENGLISH -> "Filter Lists"
        AppLanguage.ARABIC -> "قوائم التصفية"
        AppLanguage.PORTUGUESE -> "Listas de Filtro"
        AppLanguage.SPANISH -> "Listas de Filtros"
        AppLanguage.FRENCH -> "Listes de Filtres"
        AppLanguage.GERMAN -> "Filterlisten"
        AppLanguage.RUSSIAN -> "Списки фильтров"
        AppLanguage.JAPANESE -> "フィルターリスト"
        AppLanguage.KOREAN -> "필터 목록"
    }

    val adBlockNoDownloadedSourcesShort: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "未下载"
        AppLanguage.ENGLISH -> "Not downloaded"
        AppLanguage.ARABIC -> "غير محمّل"
        AppLanguage.PORTUGUESE -> "Não baixado"
        AppLanguage.SPANISH -> "No descargado"
        AppLanguage.FRENCH -> "Non téléchargé"
        AppLanguage.GERMAN -> "Nicht heruntergeladen"
        AppLanguage.RUSSIAN -> "Не загружено"
        AppLanguage.JAPANESE -> "未ダウンロード"
        AppLanguage.KOREAN -> "다운로드 안 됨"
    }

    val adBlockNoSubscriptionSelected: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "未选择规则源"
        AppLanguage.ENGLISH -> "No filter list selected"
        AppLanguage.ARABIC -> "لم يتم اختيار قائمة تصفية"
        AppLanguage.PORTUGUESE -> "Nenhuma lista de filtro selecionada"
        AppLanguage.SPANISH -> "Ninguna lista de filtros seleccionada"
        AppLanguage.FRENCH -> "Aucune liste de filtres sélectionnée"
        AppLanguage.GERMAN -> "Keine Filterliste ausgewählt"
        AppLanguage.RUSSIAN -> "Список фильтров не выбран"
        AppLanguage.JAPANESE -> "フィルターリストが選択されていません"
        AppLanguage.KOREAN -> "선택된 필터 목록 없음"
    }

    val adBlockEnabledSourcesLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已启用规则源"
        AppLanguage.ENGLISH -> "Enabled filter lists"
        AppLanguage.ARABIC -> "قوائم التصفية المفعّلة"
        AppLanguage.PORTUGUESE -> "Listas de filtro ativadas"
        AppLanguage.SPANISH -> "Listas de filtros activadas"
        AppLanguage.FRENCH -> "Listes de filtres activées"
        AppLanguage.GERMAN -> "Aktivierte Filterlisten"
        AppLanguage.RUSSIAN -> "Активные списки фильтров"
        AppLanguage.JAPANESE -> "有効なフィルターリスト"
        AppLanguage.KOREAN -> "활성화된 필터 목록"
    }

    val adBlockAddMoreSources: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "添加更多规则源"
        AppLanguage.ENGLISH -> "Add more filter lists"
        AppLanguage.ARABIC -> "إضافة المزيد من قوائم التصفية"
        AppLanguage.PORTUGUESE -> "Adicionar mais listas de filtro"
        AppLanguage.SPANISH -> "Añadir más listas de filtros"
        AppLanguage.FRENCH -> "Ajouter plus de listes de filtres"
        AppLanguage.GERMAN -> "Weitere Filterlisten hinzufügen"
        AppLanguage.RUSSIAN -> "Добавить ещё списки фильтров"
        AppLanguage.JAPANESE -> "フィルターリストを追加"
        AppLanguage.KOREAN -> "필터 목록 추가"
    }

    val adBlockSubscriptionsCount: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "%d 个订阅源"
        AppLanguage.ENGLISH -> "%d subscriptions"
        AppLanguage.ARABIC -> "%d اشتراكات"
        AppLanguage.PORTUGUESE -> "%d assinaturas"
        AppLanguage.SPANISH -> "%d suscripciones"
        AppLanguage.FRENCH -> "%d abonnements"
        AppLanguage.GERMAN -> "%d Abonnements"
        AppLanguage.RUSSIAN -> "%d подписок"
        AppLanguage.JAPANESE -> "%d 件のサブスクリプション"
        AppLanguage.KOREAN -> "%d개 구독"
    }

    val done: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "完成"
        AppLanguage.ENGLISH -> "Done"
        AppLanguage.ARABIC -> "تم"
        AppLanguage.PORTUGUESE -> "Concluído"
        AppLanguage.SPANISH -> "Listo"
        AppLanguage.FRENCH -> "Terminé"
        AppLanguage.GERMAN -> "Fertig"
        AppLanguage.RUSSIAN -> "Готово"
        AppLanguage.JAPANESE -> "完了"
        AppLanguage.KOREAN -> "완료"
    }

    val edit: String get() = when (Strings.lang) {
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

    val loading: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "加载中"
        AppLanguage.ENGLISH -> "Loading"
        AppLanguage.ARABIC -> "جاري التحميل..."
        AppLanguage.PORTUGUESE -> "Carregando"
        AppLanguage.SPANISH -> "Cargando"
        AppLanguage.FRENCH -> "Chargement"
        AppLanguage.GERMAN -> "Laden"
        AppLanguage.RUSSIAN -> "Загрузка"
        AppLanguage.JAPANESE -> "読み込み中"
        AppLanguage.KOREAN -> "로딩 중"
    }

    val unknownError: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "未知错误"
        AppLanguage.ENGLISH -> "Unknown error"
        AppLanguage.ARABIC -> "خطأ غير معروف"
        AppLanguage.PORTUGUESE -> "Erro desconhecido"
        AppLanguage.SPANISH -> "Error desconocido"
        AppLanguage.FRENCH -> "Erreur inconnue"
        AppLanguage.GERMAN -> "Unbekannter Fehler"
        AppLanguage.RUSSIAN -> "Неизвестная ошибка"
        AppLanguage.JAPANESE -> "不明なエラー"
        AppLanguage.KOREAN -> "알 수 없는 오류"
    }

    val domStorageSetting: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "DOM存储"
        AppLanguage.ENGLISH -> "DOM Storage"
        AppLanguage.ARABIC -> "تخزين DOM"
        AppLanguage.PORTUGUESE -> "Armazenamento DOM"
        AppLanguage.SPANISH -> "Almacenamiento DOM"
        AppLanguage.FRENCH -> "Stockage DOM"
        AppLanguage.GERMAN -> "DOM-Speicher"
        AppLanguage.RUSSIAN -> "DOM-хранилище"
        AppLanguage.JAPANESE -> "DOM ストレージ"
        AppLanguage.KOREAN -> "DOM 저장소"
    }

    val domStorageSettingHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "允许网页使用 localStorage / sessionStorage 保存页面状态"
        AppLanguage.ENGLISH -> "Allow pages to use localStorage / sessionStorage for page state"
        AppLanguage.ARABIC -> "السماح للصفحات باستخدام localStorage / sessionStorage لحفظ حالة الصفحة"
        AppLanguage.PORTUGUESE -> "Permitir páginas usar localStorage / sessionStorage para estado da página"
        AppLanguage.SPANISH -> "Permitir a las páginas usar localStorage / sessionStorage para el estado de la página"
        AppLanguage.FRENCH -> "Autoriser les pages à utiliser localStorage / sessionStorage pour l'état de la page"
        AppLanguage.GERMAN -> "Seiten erlauben, localStorage / sessionStorage für Seitenstatus zu verwenden"
        AppLanguage.RUSSIAN -> "Разрешить страницам использовать localStorage / sessionStorage для состояния"
        AppLanguage.JAPANESE -> "ページが localStorage / sessionStorage を使用してページ状態を保存することを許可"
        AppLanguage.KOREAN -> "페이지가 localStorage / sessionStorage를 사용하여 페이지 상태를 저장하도록 허용"
    }

    val zoomSetting: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "缩放功能"
        AppLanguage.ENGLISH -> "Zoom"
        AppLanguage.ARABIC -> "التكبير/التصغير"
        AppLanguage.PORTUGUESE -> "Zoom"
        AppLanguage.SPANISH -> "Zoom"
        AppLanguage.FRENCH -> "Zoom"
        AppLanguage.GERMAN -> "Zoom"
        AppLanguage.RUSSIAN -> "Масштаб"
        AppLanguage.JAPANESE -> "ズーム"
        AppLanguage.KOREAN -> "줌"
    }

    val swipeRefreshSetting: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下拉刷新"
        AppLanguage.ENGLISH -> "Swipe Refresh"
        AppLanguage.ARABIC -> "السحب للتحديث"
        AppLanguage.PORTUGUESE -> "Atualizar por Deslizar"
        AppLanguage.SPANISH -> "Deslizar para Actualizar"
        AppLanguage.FRENCH -> "Glisser pour Actualiser"
        AppLanguage.GERMAN -> "Wisch-Aktualisierung"
        AppLanguage.RUSSIAN -> "Обновление свайпом"
        AppLanguage.JAPANESE -> "スワイプで更新"
        AppLanguage.KOREAN -> "스와이프 새로고침"
    }

    val overscrollEffectSetting: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "越界回弹"
        AppLanguage.ENGLISH -> "Edge overscroll"
        AppLanguage.ARABIC -> "التمرير خارج الحافة"
        AppLanguage.PORTUGUESE -> "Rolagem além da borda"
        AppLanguage.SPANISH -> "Desplazamiento más allá del borde"
        AppLanguage.FRENCH -> "Défilement au-delà du bord"
        AppLanguage.GERMAN -> "Überrollen am Rand"
        AppLanguage.RUSSIAN -> "Прокрутка за край"
        AppLanguage.JAPANESE -> "端でのオーバースクロール"
        AppLanguage.KOREAN -> "가장자리 오버스크롤"
    }

    val overscrollEffectSettingHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "滚到页面边缘时显示系统的拉伸回弹。关闭后停在边缘，固定顶栏不会被拉开。"
        AppLanguage.ENGLISH -> "Shows the system stretch when the page scrolls past its edge. Turn off to stop at the edge so fixed headers are not stretched."
        AppLanguage.ARABIC -> "يعرض تمدد النظام عند التمرير خارج حافة الصفحة. أوقفه ليتوقف المحتوى عند الحافة دون تمديد الشريط الثابت."
        AppLanguage.PORTUGUESE -> "Mostra o esticamento do sistema quando a página passa da borda. Desative para parar na borda e não esticar cabeçalhos fixos."
        AppLanguage.SPANISH -> "Muestra el estiramiento del sistema cuando la página pasa del borde. Desactívalo para detenerse en el borde y no estirar las cabeceras fijas."
        AppLanguage.FRENCH -> "Affiche l'étirement du système lorsque la page dépasse le bord. Désactivez-le pour s'arrêter au bord sans étirer les en-têtes fixes."
        AppLanguage.GERMAN -> "Zeigt die Systemdehnung, wenn die Seite über den Rand hinaus scrollt. Ausschalten, damit sie am Rand stoppt und feste Leisten nicht gedehnt werden."
        AppLanguage.RUSSIAN -> "Показывает системное растяжение, когда страница прокручивается за край. Выключите, чтобы останавливаться на краю и не растягивать закреплённые панели."
        AppLanguage.JAPANESE -> "ページ端を超えてスクロールしたとき、システムの伸び縮みを表示します。オフにすると端で止まり、固定ヘッダーは伸びません。"
        AppLanguage.KOREAN -> "페이지가 가장자리를 넘어 스크롤될 때 시스템 늘림 효과를 표시합니다. 끄면 가장자리에서 멈추고 고정 헤더는 늘어나지 않습니다."
    }

    val swipeRefreshZoneLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "触发区域"
        AppLanguage.ENGLISH -> "Trigger area"
        AppLanguage.ARABIC -> "منطقة التشغيل"
        AppLanguage.PORTUGUESE -> "Área de disparo"
        AppLanguage.SPANISH -> "Área de activación"
        AppLanguage.FRENCH -> "Zone de déclenchement"
        AppLanguage.GERMAN -> "Auslösebereich"
        AppLanguage.RUSSIAN -> "Зона срабатывания"
        AppLanguage.JAPANESE -> "発生エリア"
        AppLanguage.KOREAN -> "트리거 영역"
    }

    val swipeRefreshZoneTopEdge: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "仅顶部边缘（经典）"
        AppLanguage.ENGLISH -> "Top edge only (classic)"
        AppLanguage.ARABIC -> "الحافة العلوية فقط (كلاسيكي)"
        AppLanguage.PORTUGUESE -> "Somente a borda superior (clássico)"
        AppLanguage.SPANISH -> "Solo el borde superior (clásico)"
        AppLanguage.FRENCH -> "Bord supérieur uniquement (classique)"
        AppLanguage.GERMAN -> "Nur oberer Rand (klassisch)"
        AppLanguage.RUSSIAN -> "Только верхний край (классика)"
        AppLanguage.JAPANESE -> "上部端のみ（クラシック）"
        AppLanguage.KOREAN -> "상단 가장자리만 (클래식)"
    }

    val swipeRefreshZoneAnywhere: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "页面顶部时任意位置"
        AppLanguage.ENGLISH -> "Anywhere while at page top"
        AppLanguage.ARABIC -> "أي مكان عند أعلى الصفحة"
        AppLanguage.PORTUGUESE -> "Qualquer lugar no topo da página"
        AppLanguage.SPANISH -> "Cualquier lugar en la parte superior de la página"
        AppLanguage.FRENCH -> "N'importe où en haut de la page"
        AppLanguage.GERMAN -> "Überall am Seitenanfang"
        AppLanguage.RUSSIAN -> "В любом месте вверху страницы"
        AppLanguage.JAPANESE -> "ページ最上部ではどこでも"
        AppLanguage.KOREAN -> "페이지 상단이라면 어디든"
    }

    val autoRefreshSettingLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自动刷新"
        AppLanguage.ENGLISH -> "Auto Refresh"
        AppLanguage.ARABIC -> "التحديث التلقائي"
        AppLanguage.PORTUGUESE -> "Atualização Automática"
        AppLanguage.SPANISH -> "Actualización Automática"
        AppLanguage.FRENCH -> "Actualisation Auto"
        AppLanguage.GERMAN -> "Auto-Aktualisierung"
        AppLanguage.RUSSIAN -> "Автообновление"
        AppLanguage.JAPANESE -> "自動更新"
        AppLanguage.KOREAN -> "자동 새로고침"
    }

    val autoRefreshIntervalLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "刷新间隔"
        AppLanguage.ENGLISH -> "Refresh Interval"
        AppLanguage.ARABIC -> "فترة التحديث"
        AppLanguage.PORTUGUESE -> "Intervalo de Atualização"
        AppLanguage.SPANISH -> "Intervalo de Actualización"
        AppLanguage.FRENCH -> "Intervalle d'Actualisation"
        AppLanguage.GERMAN -> "Aktualisierungsintervall"
        AppLanguage.RUSSIAN -> "Интервал обновления"
        AppLanguage.JAPANESE -> "更新間隔"
        AppLanguage.KOREAN -> "새로고침 간격"
    }

    val autoRefreshIntervalValue: (Int) -> String get() = { seconds ->
        when (Strings.lang) {
            AppLanguage.CHINESE -> String.format(java.util.Locale.getDefault(), "%d 秒", seconds)
            AppLanguage.ENGLISH -> String.format(java.util.Locale.getDefault(), "%d s", seconds)
            AppLanguage.ARABIC -> String.format(java.util.Locale.getDefault(), "%d ث", seconds)
            AppLanguage.PORTUGUESE -> String.format(java.util.Locale.getDefault(), "%d s", seconds)
            AppLanguage.SPANISH -> String.format(java.util.Locale.getDefault(), "%d s", seconds)
            AppLanguage.FRENCH -> String.format(java.util.Locale.getDefault(), "%d s", seconds)
            AppLanguage.GERMAN -> String.format(java.util.Locale.getDefault(), "%d s", seconds)
            AppLanguage.RUSSIAN -> String.format(java.util.Locale.getDefault(), "%d с", seconds)
            AppLanguage.JAPANESE -> String.format(java.util.Locale.getDefault(), "%d 秒", seconds)
            AppLanguage.KOREAN -> String.format(java.util.Locale.getDefault(), "%d초", seconds)
        }
    }

    val autoRefreshShowCountdownLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "显示倒计时"
        AppLanguage.ENGLISH -> "Show Countdown"
        AppLanguage.ARABIC -> "إظهار العد التنازلي"
        AppLanguage.PORTUGUESE -> "Mostrar Contagem Regressiva"
        AppLanguage.SPANISH -> "Mostrar Cuenta Regresiva"
        AppLanguage.FRENCH -> "Afficher le Compte à Rebours"
        AppLanguage.GERMAN -> "Countdown anzeigen"
        AppLanguage.RUSSIAN -> "Показать обратный отсчёт"
        AppLanguage.JAPANESE -> "カウントダウンを表示"
        AppLanguage.KOREAN -> "카운트다운 표시"
    }

    val autoRefreshShowCountdownDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "右上角显示剩余秒数，点击可暂停本轮"
        AppLanguage.ENGLISH -> "Show remaining seconds in the corner, tap to skip this round"
        AppLanguage.ARABIC -> "إظهار الثواني المتبقية في الزاوية، انقر لتخطي هذه الجولة"
        AppLanguage.PORTUGUESE -> "Mostrar segundos restantes no canto, toque para pular esta rodada"
        AppLanguage.SPANISH -> "Mostrar segundos restantes en la esquina, toca para omitir esta ronda"
        AppLanguage.FRENCH -> "Afficher les secondes restantes dans le coin, appuyez pour passer ce tour"
        AppLanguage.GERMAN -> "Verbleibende Sekunden in der Ecke zeigen, tippen um diese Runde zu überspringen"
        AppLanguage.RUSSIAN -> "Показывать оставшиеся секунды в углу, нажмите, чтобы пропустить этот раунд"
        AppLanguage.JAPANESE -> "隅に残り秒数を表示、タップでこのラウンドをスキップ"
        AppLanguage.KOREAN -> "구석에 남은 초 표시, 탭하여 이번 라운드 건너뛰기"
    }

    val fullscreenVideoSetting: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "全屏视频"
        AppLanguage.ENGLISH -> "Fullscreen Video"
        AppLanguage.ARABIC -> "فيديو ملء الشاشة"
        AppLanguage.PORTUGUESE -> "Vídeo em Tela Cheia"
        AppLanguage.SPANISH -> "Video en Pantalla Completa"
        AppLanguage.FRENCH -> "Vidéo Plein Écran"
        AppLanguage.GERMAN -> "Vollbild-Video"
        AppLanguage.RUSSIAN -> "Полноэкранное видео"
        AppLanguage.JAPANESE -> "全画面動画"
        AppLanguage.KOREAN -> "전체 화면 동영상"
    }

    val externalLinksSetting: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "外部链接"
        AppLanguage.ENGLISH -> "External Links"
        AppLanguage.ARABIC -> "الروابط الخارجية"
        AppLanguage.PORTUGUESE -> "Links Externos"
        AppLanguage.SPANISH -> "Enlaces Externos"
        AppLanguage.FRENCH -> "Liens Externes"
        AppLanguage.GERMAN -> "Externe Links"
        AppLanguage.RUSSIAN -> "Внешние ссылки"
        AppLanguage.JAPANESE -> "外部リンク"
        AppLanguage.KOREAN -> "외부 링크"
    }

    val externalLinksSettingHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "在浏览器中打开外部链接"
        AppLanguage.ENGLISH -> "Open external links in browser"
        AppLanguage.ARABIC -> "فتح الروابط الخارجية في المتصفح"
        AppLanguage.PORTUGUESE -> "Abrir links externos no navegador"
        AppLanguage.SPANISH -> "Abrir enlaces externos en el navegador"
        AppLanguage.FRENCH -> "Ouvrir les liens externes dans le navigateur"
        AppLanguage.GERMAN -> "Externe Links im Browser öffnen"
        AppLanguage.RUSSIAN -> "Открывать внешние ссылки в браузере"
        AppLanguage.JAPANESE -> "外部リンクをブラウザで開く"
        AppLanguage.KOREAN -> "외부 링크를 브라우저에서 열기"
    }

    val crossOriginIsolationSetting: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "SharedArrayBuffer / FFmpeg"
        AppLanguage.ENGLISH -> "SharedArrayBuffer / FFmpeg"
        AppLanguage.ARABIC -> "SharedArrayBuffer / FFmpeg"
        AppLanguage.PORTUGUESE -> "SharedArrayBuffer / FFmpeg"
        AppLanguage.SPANISH -> "SharedArrayBuffer / FFmpeg"
        AppLanguage.FRENCH -> "SharedArrayBuffer / FFmpeg"
        AppLanguage.GERMAN -> "SharedArrayBuffer / FFmpeg"
        AppLanguage.RUSSIAN -> "SharedArrayBuffer / FFmpeg"
        AppLanguage.JAPANESE -> "SharedArrayBuffer / FFmpeg"
        AppLanguage.KOREAN -> "SharedArrayBuffer / FFmpeg"
    }

    val crossOriginIsolationSettingHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "启用跨域隔离 (COOP/COEP)，支持 FFmpeg.wasm 等库"
        AppLanguage.ENGLISH -> "Enable cross-origin isolation (COOP/COEP), supports FFmpeg.wasm and similar libraries"
        AppLanguage.ARABIC -> "تمكين العزل عبر الأصل (COOP/COEP)، يدعم FFmpeg.wasm والمكتبات المماثلة"
        AppLanguage.PORTUGUESE -> "Ativar isolamento entre origens (COOP/COEP), suporta FFmpeg.wasm e bibliotecas similares"
        AppLanguage.SPANISH -> "Habilitar aislamiento entre orígenes (COOP/COEP), soporta FFmpeg.wasm y bibliotecas similares"
        AppLanguage.FRENCH -> "Activer l'isolation entre origines (COOP/COEP), prend en charge FFmpeg.wasm et bibliothèques similaires"
        AppLanguage.GERMAN -> "Cross-Origin-Isolierung aktivieren (COOP/COEP), unterstützt FFmpeg.wasm und ähnliche Bibliotheken"
        AppLanguage.RUSSIAN -> "Включить изоляцию между источниками (COOP/COEP), поддерживает FFmpeg.wasm и подобные библиотеки"
        AppLanguage.JAPANESE -> "クロスオリジン分離を有効化（COOP/COEP）、FFmpeg.wasm などのライブラリに対応"
        AppLanguage.KOREAN -> "교차 출처 격리 활성화(COOP/COEP), FFmpeg.wasm 및 유사 라이브러리 지원"
    }

    val viewportModeTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "视口适配模式"
        AppLanguage.ENGLISH -> "Viewport Mode"
        AppLanguage.ARABIC -> "وضع منفذ العرض"
        AppLanguage.PORTUGUESE -> "Modo de Viewport"
        AppLanguage.SPANISH -> "Modo de Viewport"
        AppLanguage.FRENCH -> "Mode de Viewport"
        AppLanguage.GERMAN -> "Viewport-Modus"
        AppLanguage.RUSSIAN -> "Режим Viewport"
        AppLanguage.JAPANESE -> "Viewport モード"
        AppLanguage.KOREAN -> "Viewport 모드"
    }

    val viewportModeDescription: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "解决 Unity/Canvas 游戏显示放大、UI 被裁切的问题"
        AppLanguage.ENGLISH -> "Fix Unity/Canvas games displaying zoomed in with UI clipped"
        AppLanguage.ARABIC -> "إصلاح عرض ألعاب Unity/Canvas المكبرة مع قص واجهة المستخدم"
        AppLanguage.PORTUGUESE -> "Corrige jogos Unity/Canvas exibidos ampliados com UI recortada"
        AppLanguage.SPANISH -> "Corrige juegos Unity/Canvas mostrados ampliados con UI recortada"
        AppLanguage.FRENCH -> "Corrige l'affichage zoomé des jeux Unity/Canvas avec l'UI rognée"
        AppLanguage.GERMAN -> "Behebt Unity/Canvas-Spiele, die vergrößert mit abgeschnittener UI angezeigt werden"
        AppLanguage.RUSSIAN -> "Исправляет отображение игр Unity/Canvas увеличенными с обрезанным UI"
        AppLanguage.JAPANESE -> "Unity/Canvas ゲームの拡大表示・UI 切り抜き問題を修正"
        AppLanguage.KOREAN -> "Unity/Canvas 게임의 확대 표시 및 UI 잘림 문제 수정"
    }

    val viewportModeDefault: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "标准模式（适合大多数网页）"
        AppLanguage.ENGLISH -> "Standard (for most websites)"
        AppLanguage.ARABIC -> "قياسي (لمعظم المواقع)"
        AppLanguage.PORTUGUESE -> "Padrão (para a maioria dos sites)"
        AppLanguage.SPANISH -> "Estándar (para la mayoría de los sitios)"
        AppLanguage.FRENCH -> "Standard (pour la plupart des sites)"
        AppLanguage.GERMAN -> "Standard (für die meisten Websites)"
        AppLanguage.RUSSIAN -> "Стандартный (для большинства сайтов)"
        AppLanguage.JAPANESE -> "標準（ほとんどのウェブサイト向け）"
        AppLanguage.KOREAN -> "표준(대부분의 웹사이트용)"
    }

    val viewportModeFitScreen: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "适配屏幕（Unity/Canvas 游戏推荐）"
        AppLanguage.ENGLISH -> "Fit Screen (recommended for Unity/Canvas games)"
        AppLanguage.ARABIC -> "ملاءمة الشاشة (لألعاب Unity/Canvas)"
        AppLanguage.PORTUGUESE -> "Ajustar à Tela (recomendado para jogos Unity/Canvas)"
        AppLanguage.SPANISH -> "Ajustar a la Pantalla (recomendado para juegos Unity/Canvas)"
        AppLanguage.FRENCH -> "Ajuster à l'Écran (recommandé pour les jeux Unity/Canvas)"
        AppLanguage.GERMAN -> "An Bildschirm anpassen (empfohlen für Unity/Canvas-Spiele)"
        AppLanguage.RUSSIAN -> "Вписать в экран (рекомендуется для игр Unity/Canvas)"
        AppLanguage.JAPANESE -> "画面に合わせる（Unity/Canvas ゲームに推奨）"
        AppLanguage.KOREAN -> "화면에 맞춤(Unity/Canvas 게임 권장)"
    }

    val viewportModeDesktop: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "桌面视口（980px 宽度）"
        AppLanguage.ENGLISH -> "Desktop Viewport (980px width)"
        AppLanguage.ARABIC -> "منفذ سطح المكتب (عرض 980 بكسل)"
        AppLanguage.PORTUGUESE -> "Viewport de Desktop (largura 980px)"
        AppLanguage.SPANISH -> "Viewport de Escritorio (ancho 980px)"
        AppLanguage.FRENCH -> "Viewport Bureau (largeur 980px)"
        AppLanguage.GERMAN -> "Desktop-Viewport (980px Breite)"
        AppLanguage.RUSSIAN -> "Viewport рабочего стола (ширина 980px)"
        AppLanguage.JAPANESE -> "デスクトップ Viewport（幅 980px）"
        AppLanguage.KOREAN -> "데스크톱 Viewport(폭 980px)"
    }

    val viewportModeCustom: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自定义视口"
        AppLanguage.ENGLISH -> "Custom Viewport"
        AppLanguage.ARABIC -> "منفذ العرض المخصص"
        AppLanguage.PORTUGUESE -> "Viewport Personalizado"
        AppLanguage.SPANISH -> "Viewport Personalizado"
        AppLanguage.FRENCH -> "Viewport Personnalisé"
        AppLanguage.GERMAN -> "Benutzerdefinierter Viewport"
        AppLanguage.RUSSIAN -> "Пользовательский Viewport"
        AppLanguage.JAPANESE -> "カスタム Viewport"
        AppLanguage.KOREAN -> "사용자 지정 Viewport"
    }

    val viewportCustomWidth: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "视口宽度"
        AppLanguage.ENGLISH -> "Viewport Width"
        AppLanguage.ARABIC -> "عرض منفذ العرض"
        AppLanguage.PORTUGUESE -> "Largura do Viewport"
        AppLanguage.SPANISH -> "Ancho del Viewport"
        AppLanguage.FRENCH -> "Largeur du Viewport"
        AppLanguage.GERMAN -> "Viewport-Breite"
        AppLanguage.RUSSIAN -> "Ширина Viewport"
        AppLanguage.JAPANESE -> "Viewport 幅"
        AppLanguage.KOREAN -> "Viewport 너비"
    }

    val viewportCustomWidthHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "输入视口宽度（320-3840 像素）"
        AppLanguage.ENGLISH -> "Enter viewport width (320-3840 px)"
        AppLanguage.ARABIC -> "أدخل عرض منفذ العرض (320-3840 بكسل)"
        AppLanguage.PORTUGUESE -> "Insira a largura do viewport (320-3840 px)"
        AppLanguage.SPANISH -> "Ingrese el ancho del viewport (320-3840 px)"
        AppLanguage.FRENCH -> "Saisir la largeur du viewport (320-3840 px)"
        AppLanguage.GERMAN -> "Viewport-Breite eingeben (320-3840 px)"
        AppLanguage.RUSSIAN -> "Введите ширину viewport (320-3840 px)"
        AppLanguage.JAPANESE -> "Viewport 幅を入力（320-3840 px）"
        AppLanguage.KOREAN -> "Viewport 너비 입력(320-3840 px)"
    }

    val viewportCustomWidthPresets: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "预设宽度"
        AppLanguage.ENGLISH -> "Preset Widths"
        AppLanguage.ARABIC -> "عرض محدد مسبقاً"
        AppLanguage.PORTUGUESE -> "Larguras Predefinidas"
        AppLanguage.SPANISH -> "Anchos Predefinidos"
        AppLanguage.FRENCH -> "Largeurs Prédéfinies"
        AppLanguage.GERMAN -> "Voreingestellte Breiten"
        AppLanguage.RUSSIAN -> "Предустановленные ширины"
        AppLanguage.JAPANESE -> "プリセット幅"
        AppLanguage.KOREAN -> "사전 설정 너비"
    }

    val advancedOptions: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "高级选项"
        AppLanguage.ENGLISH -> "Advanced Options"
        AppLanguage.ARABIC -> "خيارات متقدمة"
        AppLanguage.PORTUGUESE -> "Opções Avançadas"
        AppLanguage.SPANISH -> "Opciones Avanzadas"
        AppLanguage.FRENCH -> "Options Avancées"
        AppLanguage.GERMAN -> "Erweiterte Optionen"
        AppLanguage.RUSSIAN -> "Расширенные параметры"
        AppLanguage.JAPANESE -> "詳細オプション"
        AppLanguage.KOREAN -> "고급 옵션"
    }

    val expand: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "展开"
        AppLanguage.ENGLISH -> "Expand"
        AppLanguage.ARABIC -> "توسيع"
        AppLanguage.PORTUGUESE -> "Expandir"
        AppLanguage.SPANISH -> "Expandir"
        AppLanguage.FRENCH -> "Déplier"
        AppLanguage.GERMAN -> "Ausklappen"
        AppLanguage.RUSSIAN -> "Развернуть"
        AppLanguage.JAPANESE -> "展開"
        AppLanguage.KOREAN -> "펼치기"
    }

    val collapse: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "收起"
        AppLanguage.ENGLISH -> "Collapse"
        AppLanguage.ARABIC -> "طي"
        AppLanguage.PORTUGUESE -> "Recolher"
        AppLanguage.SPANISH -> "Contraer"
        AppLanguage.FRENCH -> "Replier"
        AppLanguage.GERMAN -> "Einklappen"
        AppLanguage.RUSSIAN -> "Свернуть"
        AppLanguage.JAPANESE -> "折りたたむ"
        AppLanguage.KOREAN -> "접기"
    }

    val expandAll: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "展开全部"
        AppLanguage.ENGLISH -> "Expand All"
        AppLanguage.ARABIC -> "توسيع الكل"
        AppLanguage.PORTUGUESE -> "Expandir Tudo"
        AppLanguage.SPANISH -> "Expandir Todo"
        AppLanguage.FRENCH -> "Tout Déplier"
        AppLanguage.GERMAN -> "Alle Ausklappen"
        AppLanguage.RUSSIAN -> "Развернуть всё"
        AppLanguage.JAPANESE -> "すべて展開"
        AppLanguage.KOREAN -> "모두 펼치기"
    }

    val depClearDone: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已清除"
        AppLanguage.ENGLISH -> "Cleared"
        AppLanguage.ARABIC -> "تم المسح"
        AppLanguage.PORTUGUESE -> "Limpo"
        AppLanguage.SPANISH -> "Borrado"
        AppLanguage.FRENCH -> "Effacé"
        AppLanguage.GERMAN -> "Geleert"
        AppLanguage.RUSSIAN -> "Очищено"
        AppLanguage.JAPANESE -> "クリア済み"
        AppLanguage.KOREAN -> "삭제됨"
    }

    val maximum: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "最高"
        AppLanguage.ENGLISH -> "Maximum"
        AppLanguage.ARABIC -> "الأقصى"
        AppLanguage.PORTUGUESE -> "Máximo"
        AppLanguage.SPANISH -> "Máximo"
        AppLanguage.FRENCH -> "Maximum"
        AppLanguage.GERMAN -> "Maximum"
        AppLanguage.RUSSIAN -> "Максимум"
        AppLanguage.JAPANESE -> "最大"
        AppLanguage.KOREAN -> "최대"
    }

    val canvasProtection: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Canvas 防护"
        AppLanguage.ENGLISH -> "Canvas Protection"
        AppLanguage.ARABIC -> "حماية Canvas"
        AppLanguage.PORTUGUESE -> "Proteção de Canvas"
        AppLanguage.SPANISH -> "Protección de Canvas"
        AppLanguage.FRENCH -> "Protection Canvas"
        AppLanguage.GERMAN -> "Canvas-Schutz"
        AppLanguage.RUSSIAN -> "Защита Canvas"
        AppLanguage.JAPANESE -> "Canvas 保護"
        AppLanguage.KOREAN -> "Canvas 보호"
    }

    val webglProtection: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "WebGL 防护"
        AppLanguage.ENGLISH -> "WebGL Protection"
        AppLanguage.ARABIC -> "حماية WebGL"
        AppLanguage.PORTUGUESE -> "Proteção de WebGL"
        AppLanguage.SPANISH -> "Protección de WebGL"
        AppLanguage.FRENCH -> "Protection WebGL"
        AppLanguage.GERMAN -> "WebGL-Schutz"
        AppLanguage.RUSSIAN -> "Защита WebGL"
        AppLanguage.JAPANESE -> "WebGL 保護"
        AppLanguage.KOREAN -> "WebGL 보호"
    }

    val audioProtection: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Audio 防护"
        AppLanguage.ENGLISH -> "Audio Protection"
        AppLanguage.ARABIC -> "حماية الصوت"
        AppLanguage.PORTUGUESE -> "Proteção de Áudio"
        AppLanguage.SPANISH -> "Protección de Audio"
        AppLanguage.FRENCH -> "Protection Audio"
        AppLanguage.GERMAN -> "Audio-Schutz"
        AppLanguage.RUSSIAN -> "Защита звука"
        AppLanguage.JAPANESE -> "Audio 保護"
        AppLanguage.KOREAN -> "Audio 보호"
    }

    val webrtcProtection: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "WebRTC 防泄漏"
        AppLanguage.ENGLISH -> "WebRTC Leak Protection"
        AppLanguage.ARABIC -> "حماية تسرب WebRTC"
        AppLanguage.PORTUGUESE -> "Proteção contra Vazamento WebRTC"
        AppLanguage.SPANISH -> "Protección contra Fuga WebRTC"
        AppLanguage.FRENCH -> "Protection contre les Fuites WebRTC"
        AppLanguage.GERMAN -> "WebRTC-Leckschutz"
        AppLanguage.RUSSIAN -> "Защита от утечки WebRTC"
        AppLanguage.JAPANESE -> "WebRTC リーク保護"
        AppLanguage.KOREAN -> "WebRTC 누출 보호"
    }

    val randomFingerprint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Random fingerprint"
        AppLanguage.ENGLISH -> "Random Fingerprint"
        AppLanguage.ARABIC -> "بصمة عشوائية"
        AppLanguage.PORTUGUESE -> "Impressão Digital Aleatória"
        AppLanguage.SPANISH -> "Huella Digital Aleatoria"
        AppLanguage.FRENCH -> "Empreinte Numérique Aléatoire"
        AppLanguage.GERMAN -> "Zufälliger Fingerabdruck"
        AppLanguage.RUSSIAN -> "Случайный отпечаток"
        AppLanguage.JAPANESE -> "ランダムフィンガープリント"
        AppLanguage.KOREAN -> "무작위 지문"
    }

    val fontProtection: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "字体防护"
        AppLanguage.ENGLISH -> "Font Protection"
        AppLanguage.ARABIC -> "حماية الخطوط"
        AppLanguage.PORTUGUESE -> "Proteção de Fontes"
        AppLanguage.SPANISH -> "Protección de Fuentes"
        AppLanguage.FRENCH -> "Protection des Polices"
        AppLanguage.GERMAN -> "Font-Schutz"
        AppLanguage.RUSSIAN -> "Защита шрифтов"
        AppLanguage.JAPANESE -> "フォント保護"
        AppLanguage.KOREAN -> "폰트 보호"
    }

    val timezoneSpoofing: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "时区伪装"
        AppLanguage.ENGLISH -> "Timezone Spoofing"
        AppLanguage.ARABIC -> "تزييف المنطقة الزمنية"
        AppLanguage.PORTUGUESE -> "Falsificação de Fuso Horário"
        AppLanguage.SPANISH -> "Falsificación de Zona Horaria"
        AppLanguage.FRENCH -> "Falsification du Fuseau Horaire"
        AppLanguage.GERMAN -> "Zeitzonen-Spoofing"
        AppLanguage.RUSSIAN -> "Подделка часового пояса"
        AppLanguage.JAPANESE -> "タイムゾーン偽装"
        AppLanguage.KOREAN -> "시간대 위조"
    }

    val languageSpoofing: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "语言伪装"
        AppLanguage.ENGLISH -> "Language Spoofing"
        AppLanguage.ARABIC -> "تزييف اللغة"
        AppLanguage.PORTUGUESE -> "Falsificação de Idioma"
        AppLanguage.SPANISH -> "Falsificación de Idioma"
        AppLanguage.FRENCH -> "Falsification de Langue"
        AppLanguage.GERMAN -> "Sprach-Spoofing"
        AppLanguage.RUSSIAN -> "Подделка языка"
        AppLanguage.JAPANESE -> "言語偽装"
        AppLanguage.KOREAN -> "언어 위조"
    }

    val resolutionSpoofing: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "分辨率伪装"
        AppLanguage.ENGLISH -> "Resolution Spoofing"
        AppLanguage.ARABIC -> "تزييف الدقة"
        AppLanguage.PORTUGUESE -> "Falsificação de Resolução"
        AppLanguage.SPANISH -> "Falsificación de Resolución"
        AppLanguage.FRENCH -> "Falsification de Résolution"
        AppLanguage.GERMAN -> "Auflösungs-Spoofing"
        AppLanguage.RUSSIAN -> "Подделка разрешения"
        AppLanguage.JAPANESE -> "解像度偽装"
        AppLanguage.KOREAN -> "해상도 위조"
    }

    val regenerateOnLaunch: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "每次启动重新生成"
        AppLanguage.ENGLISH -> "Regenerate on Launch"
        AppLanguage.ARABIC -> "إعادة الإنشاء عند التشغيل"
        AppLanguage.PORTUGUESE -> "Regenerar ao Iniciar"
        AppLanguage.SPANISH -> "Regenerar al Iniciar"
        AppLanguage.FRENCH -> "Régénérer au Lancement"
        AppLanguage.GERMAN -> "Beim Start neu erzeugen"
        AppLanguage.RUSSIAN -> "Регенерировать при запуске"
        AppLanguage.JAPANESE -> "起動時に再生成"
        AppLanguage.KOREAN -> "실행 시 재생성"
    }

    val environmentSpoofing: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "环境伪装"
        AppLanguage.ENGLISH -> "Environment Spoofing"
        AppLanguage.ARABIC -> "انتحال البيئة"
        AppLanguage.PORTUGUESE -> "Falsificação de Ambiente"
        AppLanguage.SPANISH -> "Falsificación de Entorno"
        AppLanguage.FRENCH -> "Falsification d'Environnement"
        AppLanguage.GERMAN -> "Umgebungs-Spoofing"
        AppLanguage.RUSSIAN -> "Подмена окружения"
        AppLanguage.JAPANESE -> "環境偽装"
        AppLanguage.KOREAN -> "환경 위조"
    }

    val followFingerprint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "跟随指纹"
        AppLanguage.ENGLISH -> "Follow fingerprint"
        AppLanguage.ARABIC -> "اتباع البصمة"
        AppLanguage.PORTUGUESE -> "Seguir impressão digital"
        AppLanguage.SPANISH -> "Seguir huella"
        AppLanguage.FRENCH -> "Suivre l'empreinte"
        AppLanguage.GERMAN -> "Fingerabdruck folgen"
        AppLanguage.RUSSIAN -> "Как у отпечатка"
        AppLanguage.JAPANESE -> "フィンガープリントに従う"
        AppLanguage.KOREAN -> "지문 따르기"
    }

    val isolationOff: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "关闭"
        AppLanguage.ENGLISH -> "Off"
        AppLanguage.ARABIC -> "إيقاف"
        AppLanguage.PORTUGUESE -> "Desativado"
        AppLanguage.SPANISH -> "Desactivado"
        AppLanguage.FRENCH -> "Désactivé"
        AppLanguage.GERMAN -> "Aus"
        AppLanguage.RUSSIAN -> "Выкл."
        AppLanguage.JAPANESE -> "オフ"
        AppLanguage.KOREAN -> "끄기"
    }

    val customOption: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自定义"
        AppLanguage.ENGLISH -> "Custom"
        AppLanguage.ARABIC -> "مخصص"
        AppLanguage.PORTUGUESE -> "Personalizado"
        AppLanguage.SPANISH -> "Personalizado"
        AppLanguage.FRENCH -> "Personnalisé"
        AppLanguage.GERMAN -> "Benutzerdefiniert"
        AppLanguage.RUSSIAN -> "Свой вариант"
        AppLanguage.JAPANESE -> "カスタム"
        AppLanguage.KOREAN -> "사용자 정의"
    }

    val customCombination: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自定义组合"
        AppLanguage.ENGLISH -> "Custom mix"
        AppLanguage.ARABIC -> "مزيج مخصص"
        AppLanguage.PORTUGUESE -> "Combinação personalizada"
        AppLanguage.SPANISH -> "Combinación personalizada"
        AppLanguage.FRENCH -> "Combinaison personnalisée"
        AppLanguage.GERMAN -> "Eigene Kombination"
        AppLanguage.RUSSIAN -> "Своя комбинация"
        AppLanguage.JAPANESE -> "カスタム組み合わせ"
        AppLanguage.KOREAN -> "사용자 조합"
    }

    val regenerateFingerprint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "换一组"
        AppLanguage.ENGLISH -> "Shuffle"
        AppLanguage.ARABIC -> "تبديل"
        AppLanguage.PORTUGUESE -> "Trocar"
        AppLanguage.SPANISH -> "Cambiar"
        AppLanguage.FRENCH -> "Régénérer"
        AppLanguage.GERMAN -> "Neu würfeln"
        AppLanguage.RUSSIAN -> "Сменить"
        AppLanguage.JAPANESE -> "変更"
        AppLanguage.KOREAN -> "다시 생성"
    }

    val languageTagLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "语言标签"
        AppLanguage.ENGLISH -> "Language tag"
        AppLanguage.ARABIC -> "وسم اللغة"
        AppLanguage.PORTUGUESE -> "Tag de idioma"
        AppLanguage.SPANISH -> "Etiqueta de idioma"
        AppLanguage.FRENCH -> "Tag de langue"
        AppLanguage.GERMAN -> "Sprach-Tag"
        AppLanguage.RUSSIAN -> "Тег языка"
        AppLanguage.JAPANESE -> "言語タグ"
        AppLanguage.KOREAN -> "언어 태그"
    }

    val timezoneIdLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "时区 ID（IANA）"
        AppLanguage.ENGLISH -> "Timezone ID (IANA)"
        AppLanguage.ARABIC -> "معرّف المنطقة الزمنية (IANA)"
        AppLanguage.PORTUGUESE -> "ID de fuso horário (IANA)"
        AppLanguage.SPANISH -> "ID de zona horaria (IANA)"
        AppLanguage.FRENCH -> "ID de fuseau horaire (IANA)"
        AppLanguage.GERMAN -> "Zeitzonen-ID (IANA)"
        AppLanguage.RUSSIAN -> "ID часового пояса (IANA)"
        AppLanguage.JAPANESE -> "タイムゾーン ID（IANA）"
        AppLanguage.KOREAN -> "시간대 ID (IANA)"
    }

    val widthLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "宽"
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

    val heightLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "高"
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

    val invalidValue: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "无效值"
        AppLanguage.ENGLISH -> "Invalid value"
        AppLanguage.ARABIC -> "قيمة غير صالحة"
        AppLanguage.PORTUGUESE -> "Valor inválido"
        AppLanguage.SPANISH -> "Valor no válido"
        AppLanguage.FRENCH -> "Valeur non valide"
        AppLanguage.GERMAN -> "Ungültiger Wert"
        AppLanguage.RUSSIAN -> "Недопустимое значение"
        AppLanguage.JAPANESE -> "無効な値"
        AppLanguage.KOREAN -> "유효하지 않은 값"
    }

    val isoLevelBasicSummary: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "指纹随机化 · Canvas 噪音 · WebRTC 屏蔽"
        AppLanguage.ENGLISH -> "Random fingerprint · Canvas noise · WebRTC shielding"
        AppLanguage.ARABIC -> "بصمة عشوائية · ضجيج Canvas · حجب WebRTC"
        AppLanguage.PORTUGUESE -> "Impressão aleatória · Ruído de Canvas · Bloqueio de WebRTC"
        AppLanguage.SPANISH -> "Huella aleatoria · Ruido de Canvas · Bloqueo de WebRTC"
        AppLanguage.FRENCH -> "Empreinte aléatoire · Bruit Canvas · Blocage WebRTC"
        AppLanguage.GERMAN -> "Zufalls-Fingerprint · Canvas-Rauschen · WebRTC-Block"
        AppLanguage.RUSSIAN -> "Случайный отпечаток · Шум Canvas · Блокировка WebRTC"
        AppLanguage.JAPANESE -> "ランダムフィンガープリント · Canvas ノイズ · WebRTC 遮断"
        AppLanguage.KOREAN -> "무작위 지문 · Canvas 노이즈 · WebRTC 차단"
    }

    val isoLevelStandardSummary: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "完整指纹防护 · 不改变语言/时区/分辨率"
        AppLanguage.ENGLISH -> "Full fingerprint protection · keeps your locale/timezone/screen"
        AppLanguage.ARABIC -> "حماية كاملة للبصمة · دون تغيير اللغة/المنطقة/الدقة"
        AppLanguage.PORTUGUESE -> "Proteção completa · mantém idioma/fuso/resolução"
        AppLanguage.SPANISH -> "Protección completa · conserva idioma/zona/resolución"
        AppLanguage.FRENCH -> "Protection complète · conserve langue/fuseau/résolution"
        AppLanguage.GERMAN -> "Vollschutz · behält Sprache/Zeitzone/Auflösung"
        AppLanguage.RUSSIAN -> "Полная защита отпечатка · язык/пояс/экран не меняются"
        AppLanguage.JAPANESE -> "完全なフィンガープリント保護 · 言語/タイムゾーン/解像度は維持"
        AppLanguage.KOREAN -> "완전한 지문 보호 · 언어/시간대/해상도 유지"
    }

    val isoLevelMaximumSummary: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "全部防护 + 语言/时区/分辨率伪装 + 每次启动换指纹"
        AppLanguage.ENGLISH -> "All protections + locale/timezone/screen spoofing + new identity every launch"
        AppLanguage.ARABIC -> "كل الحمايات + انتحال اللغة/المنطقة/الدقة + هوية جديدة كل تشغيل"
        AppLanguage.PORTUGUESE -> "Todas as proteções + falsificação de idioma/fuso/resolução + nova identidade a cada início"
        AppLanguage.SPANISH -> "Todas las protecciones + falsificación de idioma/zona/resolución + nueva identidad cada inicio"
        AppLanguage.FRENCH -> "Toutes les protections + falsification langue/fuseau/résolution + nouvelle identité à chaque lancement"
        AppLanguage.GERMAN -> "Alle Schutzmaßnahmen + Sprach-/Zeitzonen-/Auflösungs-Spoofing + neue Identität bei jedem Start"
        AppLanguage.RUSSIAN -> "Все защиты + подмена языка/пояса/экрана + новый отпечаток при каждом запуске"
        AppLanguage.JAPANESE -> "全保護 + 言語/タイムゾーン/解像度偽装 + 起動ごとに新しいフィンガープリント"
        AppLanguage.KOREAN -> "모든 보호 + 언어/시간대/해상도 위조 + 매번 새 지문"
    }

    val frameworkLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "框架"
        AppLanguage.ENGLISH -> "Framework"
        AppLanguage.ARABIC -> "إطار العمل"
        AppLanguage.PORTUGUESE -> "Framework"
        AppLanguage.SPANISH -> "Framework"
        AppLanguage.FRENCH -> "Framework"
        AppLanguage.GERMAN -> "Framework"
        AppLanguage.RUSSIAN -> "Фреймворк"
        AppLanguage.JAPANESE -> "フレームワーク"
        AppLanguage.KOREAN -> "프레임워크"
    }

    val versionLabel: String get() = when (Strings.lang) {
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

    val packageManagerLabel: String get() = when (Strings.lang) {
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

    val dependencyCountLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "依赖数量"
        AppLanguage.ENGLISH -> "Dependency Count"
        AppLanguage.ARABIC -> "عدد التبعيات"
        AppLanguage.PORTUGUESE -> "Contagem de Dependências"
        AppLanguage.SPANISH -> "Conteo de Dependencias"
        AppLanguage.FRENCH -> "Nombre de Dépendances"
        AppLanguage.GERMAN -> "Anzahl der Abhängigkeiten"
        AppLanguage.RUSSIAN -> "Количество зависимостей"
        AppLanguage.JAPANESE -> "依存関係数"
        AppLanguage.KOREAN -> "종속성 수"
    }

    val outputDirLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "输出目录"
        AppLanguage.ENGLISH -> "Output Directory"
        AppLanguage.ARABIC -> "دليل الإخراج"
        AppLanguage.PORTUGUESE -> "Diretório de Saída"
        AppLanguage.SPANISH -> "Directorio de Salida"
        AppLanguage.FRENCH -> "Répertoire de Sortie"
        AppLanguage.GERMAN -> "Ausgabeverzeichnis"
        AppLanguage.RUSSIAN -> "Каталог вывода"
        AppLanguage.JAPANESE -> "出力ディレクトリ"
        AppLanguage.KOREAN -> "출력 디렉토리"
    }

    val dependencyCountValue: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "%d 个"
        AppLanguage.ENGLISH -> "%d"
        AppLanguage.ARABIC -> "%d"
        AppLanguage.PORTUGUESE -> "%d"
        AppLanguage.SPANISH -> "%d"
        AppLanguage.FRENCH -> "%d"
        AppLanguage.GERMAN -> "%d"
        AppLanguage.RUSSIAN -> "%d"
        AppLanguage.JAPANESE -> "%d"
        AppLanguage.KOREAN -> "%d"
    }

    val urlPattern: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "URL 模式"
        AppLanguage.ENGLISH -> "URL Pattern"
        AppLanguage.ARABIC -> "نمط URL"
        AppLanguage.PORTUGUESE -> "Padrão de URL"
        AppLanguage.SPANISH -> "Patrón de URL"
        AppLanguage.FRENCH -> "Motif d'URL"
        AppLanguage.GERMAN -> "URL-Muster"
        AppLanguage.RUSSIAN -> "Шаблон URL"
        AppLanguage.JAPANESE -> "URL パターン"
        AppLanguage.KOREAN -> "URL 패턴"
    }

    val typeLabel: String get() = when (Strings.lang) {
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

    val jsCodePlaceholder: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "// 在这里编写 JavaScript 代码\nconsole.log('Hello from module!');"
        AppLanguage.ENGLISH -> "// Write JavaScript code here\nconsole.log('Hello from module!');"
        AppLanguage.ARABIC -> "// اكتب كود JavaScript هنا\nconsole.log('Hello from module!');"
        AppLanguage.PORTUGUESE -> "// Escreva código JavaScript aqui\nconsole.log('Hello from module!');"
        AppLanguage.SPANISH -> "// Escriba código JavaScript aquí\nconsole.log('Hello from module!');"
        AppLanguage.FRENCH -> "// Écrivez le code JavaScript ici\nconsole.log('Hello from module!');"
        AppLanguage.GERMAN -> "// JavaScript-Code hier schreiben\nconsole.log('Hello from module!');"
        AppLanguage.RUSSIAN -> "// Пишите код JavaScript здесь\nconsole.log('Hello from module!');"
        AppLanguage.JAPANESE -> "// ここに JavaScript コードを記述\nconsole.log('Hello from module!');"
        AppLanguage.KOREAN -> "// 여기에 JavaScript 코드 작성\nconsole.log('Hello from module!');"
    }

    val cssCodePlaceholder: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "/* 在这里编写 CSS 样式 */\n.ad-banner {\n   display: none !important;\n}"
        AppLanguage.ENGLISH -> "/* Write CSS styles here */\n.ad-banner {\n   display: none !important;\n}"
        AppLanguage.ARABIC -> "/* اكتب أنماط CSS هنا */\n.ad-banner {\n   display: none !important;\n}"
        AppLanguage.PORTUGUESE -> "/* Escreva estilos CSS aqui */\n.ad-banner {\n   display: none !important;\n}"
        AppLanguage.SPANISH -> "/* Escriba estilos CSS aquí */\n.ad-banner {\n   display: none !important;\n}"
        AppLanguage.FRENCH -> "/* Écrivez les styles CSS ici */\n.ad-banner {\n   display: none !important;\n}"
        AppLanguage.GERMAN -> "/* CSS-Stile hier schreiben */\n.ad-banner {\n   display: none !important;\n}"
        AppLanguage.RUSSIAN -> "/* Пишите стили CSS здесь */\n.ad-banner {\n   display: none !important;\n}"
        AppLanguage.JAPANESE -> "/* ここに CSS スタイルを記述 */\n.ad-banner {\n   display: none !important;\n}"
        AppLanguage.KOREAN -> "/* 여기에 CSS 스타일 작성 */\n.ad-banner {\n   display: none !important;\n}"
    }

    val authorAvatar: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "作者头像"
        AppLanguage.ENGLISH -> "Author Avatar"
        AppLanguage.ARABIC -> "صورة المؤلف"
        AppLanguage.PORTUGUESE -> "Avatar do Autor"
        AppLanguage.SPANISH -> "Avatar del Autor"
        AppLanguage.FRENCH -> "Avatar de l'Auteur"
        AppLanguage.GERMAN -> "Autor-Avatar"
        AppLanguage.RUSSIAN -> "Аватар автора"
        AppLanguage.JAPANESE -> "作者アバター"
        AppLanguage.KOREAN -> "작성자 아바타"
    }

    val selectBgm: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择背景音乐"
        AppLanguage.ENGLISH -> "Select Background Music"
        AppLanguage.ARABIC -> "اختيار موسيقى الخلفية"
        AppLanguage.PORTUGUESE -> "Selecionar Música de Fundo"
        AppLanguage.SPANISH -> "Seleccionar Música de Fondo"
        AppLanguage.FRENCH -> "Sélectionner la Musique de Fond"
        AppLanguage.GERMAN -> "Hintergrundmusik Auswählen"
        AppLanguage.RUSSIAN -> "Выбрать фоновую музыку"
        AppLanguage.JAPANESE -> "BGM を選択"
        AppLanguage.KOREAN -> "배경 음악 선택"
    }

    val selectedMusic: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已选音乐"
        AppLanguage.ENGLISH -> "Selected Music"
        AppLanguage.ARABIC -> "الموسيقى المحددة"
        AppLanguage.PORTUGUESE -> "Música Selecionada"
        AppLanguage.SPANISH -> "Música Seleccionada"
        AppLanguage.FRENCH -> "Musique Sélectionnée"
        AppLanguage.GERMAN -> "Ausgewählte Musik"
        AppLanguage.RUSSIAN -> "Выбранная музыка"
        AppLanguage.JAPANESE -> "選択済み音楽"
        AppLanguage.KOREAN -> "선택된 음악"
    }

    val availableMusic: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "可用音乐"
        AppLanguage.ENGLISH -> "Available Music"
        AppLanguage.ARABIC -> "الموسيقى المتاحة"
        AppLanguage.PORTUGUESE -> "Música Disponível"
        AppLanguage.SPANISH -> "Música Disponible"
        AppLanguage.FRENCH -> "Musique Disponible"
        AppLanguage.GERMAN -> "Verfügbare Musik"
        AppLanguage.RUSSIAN -> "Доступная музыка"
        AppLanguage.JAPANESE -> "利用可能な音楽"
        AppLanguage.KOREAN -> "사용 가능한 음악"
    }

    val uploadMusic: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "上传音乐"
        AppLanguage.ENGLISH -> "Upload Music"
        AppLanguage.ARABIC -> "رفع موسيقى"
        AppLanguage.PORTUGUESE -> "Carregar Música"
        AppLanguage.SPANISH -> "Subir Música"
        AppLanguage.FRENCH -> "Téléverser de la Musique"
        AppLanguage.GERMAN -> "Musik Hochladen"
        AppLanguage.RUSSIAN -> "Загрузить музыку"
        AppLanguage.JAPANESE -> "音楽をアップロード"
        AppLanguage.KOREAN -> "음악 업로드"
    }

    val clickArrowToReorder: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "点击箭头调整顺序"
        AppLanguage.ENGLISH -> "Click arrows to reorder"
        AppLanguage.ARABIC -> "انقر على الأسهم لإعادة الترتيب"
        AppLanguage.PORTUGUESE -> "Clique nas setas para reordenar"
        AppLanguage.SPANISH -> "Haga clic en las flechas para reordenar"
        AppLanguage.FRENCH -> "Cliquez sur les flèches pour réorganiser"
        AppLanguage.GERMAN -> "Auf Pfeile tippen zum Neuordnen"
        AppLanguage.RUSSIAN -> "Нажимайте на стрелки для изменения порядка"
        AppLanguage.JAPANESE -> "矢印をクリックして並び替え"
        AppLanguage.KOREAN -> "화살표를 클릭하여 순서 변경"
    }

    val noMusicAvailable: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "暂无音乐"
        AppLanguage.ENGLISH -> "No music available"
        AppLanguage.ARABIC -> "لا توجد موسيقى متاحة"
        AppLanguage.PORTUGUESE -> "Sem música disponível"
        AppLanguage.SPANISH -> "Sin música disponible"
        AppLanguage.FRENCH -> "Aucune musique disponible"
        AppLanguage.GERMAN -> "Keine Musik verfügbar"
        AppLanguage.RUSSIAN -> "Нет доступной музыки"
        AppLanguage.JAPANESE -> "利用可能な音楽がありません"
        AppLanguage.KOREAN -> "사용 가능한 음악이 없습니다"
    }

    val bgmEmptyHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "上传本地音频，或在线搜索下载"
        AppLanguage.ENGLISH -> "Upload local audio, or search online"
        AppLanguage.ARABIC -> "ارفع ملفًا صوتيًا محليًا أو ابحث عبر الإنترنت"
        AppLanguage.PORTUGUESE -> "Envie áudio local ou pesquise online"
        AppLanguage.SPANISH -> "Sube audio local o busca en línea"
        AppLanguage.FRENCH -> "Téléversez un audio local ou cherchez en ligne"
        AppLanguage.GERMAN -> "Lokale Audiodatei hochladen oder online suchen"
        AppLanguage.RUSSIAN -> "Загрузите локальное аудио или найдите онлайн"
        AppLanguage.JAPANESE -> "ローカル音声をアップロード、またはオンライン検索"
        AppLanguage.KOREAN -> "로컬 오디오 업로드 또는 온라인 검색"
    }

    val noMusicWithTag: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "没有此标签的音乐"
        AppLanguage.ENGLISH -> "No music with this tag"
        AppLanguage.ARABIC -> "لا توجد موسيقى بهذه العلامة"
        AppLanguage.PORTUGUESE -> "Sem música com esta tag"
        AppLanguage.SPANISH -> "Sin música con esta etiqueta"
        AppLanguage.FRENCH -> "Aucune musique avec ce tag"
        AppLanguage.GERMAN -> "Keine Musik mit diesem Tag"
        AppLanguage.RUSSIAN -> "Нет музыки с этим тегом"
        AppLanguage.JAPANESE -> "このタグの音楽がありません"
        AppLanguage.KOREAN -> "이 태그의 음악이 없습니다"
    }

    val playMode: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "播放模式"
        AppLanguage.ENGLISH -> "Play Mode"
        AppLanguage.ARABIC -> "وضع التشغيل"
        AppLanguage.PORTUGUESE -> "Modo de Reprodução"
        AppLanguage.SPANISH -> "Modo de Reproducción"
        AppLanguage.FRENCH -> "Mode de Lecture"
        AppLanguage.GERMAN -> "Wiedergabemodus"
        AppLanguage.RUSSIAN -> "Режим воспроизведения"
        AppLanguage.JAPANESE -> "再生モード"
        AppLanguage.KOREAN -> "재생 모드"
    }

    val loopMode: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "循环"
        AppLanguage.ENGLISH -> "Loop"
        AppLanguage.ARABIC -> "تكرار"
        AppLanguage.PORTUGUESE -> "Repetir"
        AppLanguage.SPANISH -> "Bucle"
        AppLanguage.FRENCH -> "Boucle"
        AppLanguage.GERMAN -> "Schleife"
        AppLanguage.RUSSIAN -> "Повтор"
        AppLanguage.JAPANESE -> "ループ"
        AppLanguage.KOREAN -> "반복"
    }

    val sequentialMode: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "顺序"
        AppLanguage.ENGLISH -> "Sequential"
        AppLanguage.ARABIC -> "تسلسلي"
        AppLanguage.PORTUGUESE -> "Sequencial"
        AppLanguage.SPANISH -> "Secuencial"
        AppLanguage.FRENCH -> "Séquentiel"
        AppLanguage.GERMAN -> "Sequenziell"
        AppLanguage.RUSSIAN -> "Последовательно"
        AppLanguage.JAPANESE -> "シーケンシャル"
        AppLanguage.KOREAN -> "순차"
    }

    val shuffleMode: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "随机"
        AppLanguage.ENGLISH -> "Shuffle"
        AppLanguage.ARABIC -> "عشوائي"
        AppLanguage.PORTUGUESE -> "Aleatório"
        AppLanguage.SPANISH -> "Aleatorio"
        AppLanguage.FRENCH -> "Aléatoire"
        AppLanguage.GERMAN -> "Zufall"
        AppLanguage.RUSSIAN -> "Случайно"
        AppLanguage.JAPANESE -> "シャッフル"
        AppLanguage.KOREAN -> "셔플"
    }

    val volume: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "音量"
        AppLanguage.ENGLISH -> "Volume"
        AppLanguage.ARABIC -> "مستوى الصوت"
        AppLanguage.PORTUGUESE -> "Volume"
        AppLanguage.SPANISH -> "Volumen"
        AppLanguage.FRENCH -> "Volume"
        AppLanguage.GERMAN -> "Lautstärke"
        AppLanguage.RUSSIAN -> "Громкость"
        AppLanguage.JAPANESE -> "音量"
        AppLanguage.KOREAN -> "볼륨"
    }

    val showLyrics: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "显示歌词"
        AppLanguage.ENGLISH -> "Show Lyrics"
        AppLanguage.ARABIC -> "عرض كلمات الأغنية"
        AppLanguage.PORTUGUESE -> "Mostrar Letras"
        AppLanguage.SPANISH -> "Mostrar Letras"
        AppLanguage.FRENCH -> "Afficher les Paroles"
        AppLanguage.GERMAN -> "Liedtexte Anzeigen"
        AppLanguage.RUSSIAN -> "Показать текст песни"
        AppLanguage.JAPANESE -> "歌詞を表示"
        AppLanguage.KOREAN -> "가사 표시"
    }

    val bgmFloatingPlayer: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "悬浮播放器"
        AppLanguage.ENGLISH -> "Floating player"
        AppLanguage.ARABIC -> "مشغل عائم"
        AppLanguage.PORTUGUESE -> "Reprodutor flutuante"
        AppLanguage.SPANISH -> "Reproductor flotante"
        AppLanguage.FRENCH -> "Lecteur flottant"
        AppLanguage.GERMAN -> "Schwebender Player"
        AppLanguage.RUSSIAN -> "Плавающий плеер"
        AppLanguage.JAPANESE -> "フローティングプレーヤー"
        AppLanguage.KOREAN -> "플로팅 플레이어"
    }

    val bgmFloatingPlayerHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "平时是一颗浮在网页上的小球，点开才出现切歌和关闭。拖动会吸到左右边缘，几秒不用会自己收回去。关闭只停音乐，小球还在。"
        AppLanguage.ENGLISH -> "A ball above the page. Tap it for previous, next, and close. Drag snaps it to either edge, and it folds away after a few seconds. Close stops the music and leaves the ball."
        AppLanguage.ARABIC -> "كرة فوق الصفحة. انقرها للسابق والتالي والإغلاق. اسحبها فتلتصق بأحد الجانبين، ثم تنطوي بعد ثوانٍ. الإغلاق يوقف الموسيقى ويبقي الكرة."
        AppLanguage.PORTUGUESE -> "Uma bola sobre a página. Toque para anterior, próxima e fechar. Arrastar gruda na borda e ela se recolhe sozinha. Fechar para a música e deixa a bola."
        AppLanguage.SPANISH -> "Una bola sobre la página. Tócala para anterior, siguiente y cerrar. Al arrastrarla se pega al borde y se pliega sola. Cerrar detiene la música y deja la bola."
        AppLanguage.FRENCH -> "Une bille au-dessus de la page. Touchez-la pour précédent, suivant et fermer. Le glisser la colle au bord, puis elle se replie. Fermer arrête la musique et garde la bille."
        AppLanguage.GERMAN -> "Eine Kugel über der Seite. Tippen öffnet Zurück, Weiter und Schließen. Ziehen lässt sie einrasten, danach faltet sie sich ein. Schließen stoppt die Musik, die Kugel bleibt."
        AppLanguage.RUSSIAN -> "Шарик поверх страницы. Нажатие открывает предыдущий, следующий и закрытие. Перетаскивание прижимает его к краю, затем он складывается. Закрытие останавливает музыку и оставляет шарик."
        AppLanguage.JAPANESE -> "普段はページ上の小球です。タップすると曲送りと閉じるが出ます。ドラッグで端に吸着し、数秒で戻ります。閉じるは音楽を止めるだけで、球は残ります。"
        AppLanguage.KOREAN -> "평소에는 페이지 위의 작은 공입니다. 누르면 곡 이동과 닫기가 나옵니다. 끌면 가장자리에 붙고, 몇 초 뒤 접힙니다. 닫기는 음악만 멈추고 공은 남습니다."
    }

    val bgmNotificationPlayer: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "通知栏播放器"
        AppLanguage.ENGLISH -> "Notification player"
        AppLanguage.ARABIC -> "مشغل الإشعار"
        AppLanguage.PORTUGUESE -> "Reprodutor na notificação"
        AppLanguage.SPANISH -> "Reproductor en la notificación"
        AppLanguage.FRENCH -> "Lecteur dans la notification"
        AppLanguage.GERMAN -> "Player in der Benachrichtigung"
        AppLanguage.RUSSIAN -> "Плеер в уведомлении"
        AppLanguage.JAPANESE -> "通知プレーヤー"
        AppLanguage.KOREAN -> "알림 플레이어"
    }

    val bgmNotificationPlayerHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "在通知栏和锁屏上使用系统媒体控件，可以暂停、切歌和拖动进度。"
        AppLanguage.ENGLISH -> "System media controls in the notification shade and on the lock screen: pause, skip, and scrub."
        AppLanguage.ARABIC -> "عناصر تحكم الوسائط في لوحة الإشعارات وشاشة القفل: إيقاف مؤقت وتخطٍ وسحب التقدم."
        AppLanguage.PORTUGUESE -> "Controles de mídia do sistema na barra de notificações e na tela de bloqueio: pausar, trocar e arrastar o progresso."
        AppLanguage.SPANISH -> "Controles de medios del sistema en las notificaciones y en la pantalla de bloqueo: pausa, cambio de pista y progreso."
        AppLanguage.FRENCH -> "Contrôles média du système dans les notifications et sur l'écran de verrouillage : pause, piste et progression."
        AppLanguage.GERMAN -> "System-Mediensteuerung in der Benachrichtigung und auf dem Sperrbildschirm: Pause, Titelwechsel und Spulen."
        AppLanguage.RUSSIAN -> "Системные кнопки в шторке и на экране блокировки: пауза, переключение трека и перемотка."
        AppLanguage.JAPANESE -> "通知とロック画面のシステムメディア操作で、一時停止、曲送り、シークができます。"
        AppLanguage.KOREAN -> "알림창과 잠금 화면의 시스템 미디어 컨트롤로 일시정지, 곡 이동, 탐색을 할 수 있습니다."
    }

    val bgmPrevious: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "上一首"
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

    val bgmNext: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下一首"
        AppLanguage.ENGLISH -> "Next"
        AppLanguage.ARABIC -> "التالي"
        AppLanguage.PORTUGUESE -> "Próxima"
        AppLanguage.SPANISH -> "Siguiente"
        AppLanguage.FRENCH -> "Suivant"
        AppLanguage.GERMAN -> "Weiter"
        AppLanguage.RUSSIAN -> "Следующий"
        AppLanguage.JAPANESE -> "次へ"
        AppLanguage.KOREAN -> "다음"
    }

    val bgmNotificationChannel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "背景音乐"
        AppLanguage.ENGLISH -> "Background music"
        AppLanguage.ARABIC -> "موسيقى الخلفية"
        AppLanguage.PORTUGUESE -> "Música de fundo"
        AppLanguage.SPANISH -> "Música de fondo"
        AppLanguage.FRENCH -> "Musique de fond"
        AppLanguage.GERMAN -> "Hintergrundmusik"
        AppLanguage.RUSSIAN -> "Фоновая музыка"
        AppLanguage.JAPANESE -> "バックグラウンド音楽"
        AppLanguage.KOREAN -> "배경 음악"
    }

    val allTag: String get() = when (Strings.lang) {
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

    val lyricsSaved: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "[OK] 歌词已保存"
        AppLanguage.ENGLISH -> "[OK] Lyrics saved"
        AppLanguage.ARABIC -> "[OK] تم حفظ كلمات الأغنية"
        AppLanguage.PORTUGUESE -> "[OK] Letras salvas"
        AppLanguage.SPANISH -> "[OK] Letras guardadas"
        AppLanguage.FRENCH -> "[OK] Paroles enregistrées"
        AppLanguage.GERMAN -> "[OK] Liedtexte gespeichert"
        AppLanguage.RUSSIAN -> "[OK] Текст песни сохранён"
        AppLanguage.JAPANESE -> "[OK] 歌詞を保存しました"
        AppLanguage.KOREAN -> "[OK] 가사가 저장되었습니다"
    }

    val lines: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "行"
        AppLanguage.ENGLISH -> "lines"
        AppLanguage.ARABIC -> "سطر"
        AppLanguage.PORTUGUESE -> "linhas"
        AppLanguage.SPANISH -> "líneas"
        AppLanguage.FRENCH -> "lignes"
        AppLanguage.GERMAN -> "Zeilen"
        AppLanguage.RUSSIAN -> "строки"
        AppLanguage.JAPANESE -> "行"
        AppLanguage.KOREAN -> "줄"
    }

    val previewLyrics: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "预览歌词"
        AppLanguage.ENGLISH -> "Preview Lyrics"
        AppLanguage.ARABIC -> "معاينة كلمات الأغنية"
        AppLanguage.PORTUGUESE -> "Pré-visualizar Letras"
        AppLanguage.SPANISH -> "Vista Previa de Letras"
        AppLanguage.FRENCH -> "Aperçu des Paroles"
        AppLanguage.GERMAN -> "Liedtexte Vorschau"
        AppLanguage.RUSSIAN -> "Предпросмотр текста"
        AppLanguage.JAPANESE -> "歌詞をプレビュー"
        AppLanguage.KOREAN -> "가사 미리보기"
    }

    val aiGenerateLyrics: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "AI生成歌词"
        AppLanguage.ENGLISH -> "AI Generate Lyrics"
        AppLanguage.ARABIC -> "إنشاء كلمات بالذكاء الاصطناعي"
        AppLanguage.PORTUGUESE -> "Gerar Letras com IA"
        AppLanguage.SPANISH -> "Generar Letras con IA"
        AppLanguage.FRENCH -> "Générer des Paroles par IA"
        AppLanguage.GERMAN -> "KI-Liedtexte Generieren"
        AppLanguage.RUSSIAN -> "Сгенерировать текст песни ИИ"
        AppLanguage.JAPANESE -> "AIで歌詞を生成"
        AppLanguage.KOREAN -> "AI로 가사 생성"
    }

    val editTags: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "编辑标签"
        AppLanguage.ENGLISH -> "Edit Tags"
        AppLanguage.ARABIC -> "تعديل العلامات"
        AppLanguage.PORTUGUESE -> "Editar Tags"
        AppLanguage.SPANISH -> "Editar Etiquetas"
        AppLanguage.FRENCH -> "Éditer les Tags"
        AppLanguage.GERMAN -> "Tags Bearbeiten"
        AppLanguage.RUSSIAN -> "Изменить теги"
        AppLanguage.JAPANESE -> "タグを編集"
        AppLanguage.KOREAN -> "태그 편집"
    }

    val stop: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "停止"
        AppLanguage.ENGLISH -> "Stop"
        AppLanguage.ARABIC -> "إيقاف"
        AppLanguage.PORTUGUESE -> "Parar"
        AppLanguage.SPANISH -> "Detener"
        AppLanguage.FRENCH -> "Arrêter"
        AppLanguage.GERMAN -> "Stopp"
        AppLanguage.RUSSIAN -> "Стоп"
        AppLanguage.JAPANESE -> "停止"
        AppLanguage.KOREAN -> "정지"
    }

    val moveUp: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "上移"
        AppLanguage.ENGLISH -> "Move Up"
        AppLanguage.ARABIC -> "نقل لأعلى"
        AppLanguage.PORTUGUESE -> "Mover para Cima"
        AppLanguage.SPANISH -> "Mover Arriba"
        AppLanguage.FRENCH -> "Monter"
        AppLanguage.GERMAN -> "Nach Oben"
        AppLanguage.RUSSIAN -> "Вверх"
        AppLanguage.JAPANESE -> "上に移動"
        AppLanguage.KOREAN -> "위로 이동"
    }

    val moveDown: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "下移"
        AppLanguage.ENGLISH -> "Move Down"
        AppLanguage.ARABIC -> "نقل لأسفل"
        AppLanguage.PORTUGUESE -> "Mover para Baixo"
        AppLanguage.SPANISH -> "Mover Abajo"
        AppLanguage.FRENCH -> "Descendre"
        AppLanguage.GERMAN -> "Nach Unten"
        AppLanguage.RUSSIAN -> "Вниз"
        AppLanguage.JAPANESE -> "下に移動"
        AppLanguage.KOREAN -> "아래로 이동"
    }

    val presetMusic: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "预置音乐"
        AppLanguage.ENGLISH -> "Preset Music"
        AppLanguage.ARABIC -> "موسيقى مسبقة"
        AppLanguage.PORTUGUESE -> "Música Predefinida"
        AppLanguage.SPANISH -> "Música Predefinida"
        AppLanguage.FRENCH -> "Musique Prédéfinie"
        AppLanguage.GERMAN -> "Voreingestellte Musik"
        AppLanguage.RUSSIAN -> "Предустановленная музыка"
        AppLanguage.JAPANESE -> "プリセット音楽"
        AppLanguage.KOREAN -> "사전 설정 음악"
    }

    val userUploaded: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "用户上传"
        AppLanguage.ENGLISH -> "User Uploaded"
        AppLanguage.ARABIC -> "رفع المستخدم"
        AppLanguage.PORTUGUESE -> "Carregado pelo Usuário"
        AppLanguage.SPANISH -> "Subido por el Usuario"
        AppLanguage.FRENCH -> "Téléversé par l'Utilisateur"
        AppLanguage.GERMAN -> "Vom Nutzer Hochgeladen"
        AppLanguage.RUSSIAN -> "Загружено пользователем"
        AppLanguage.JAPANESE -> "ユーザーアップロード"
        AppLanguage.KOREAN -> "사용자 업로드"
    }

    val uploadMusicTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "上传音乐"
        AppLanguage.ENGLISH -> "Upload Music"
        AppLanguage.ARABIC -> "رفع موسيقى"
        AppLanguage.PORTUGUESE -> "Carregar Música"
        AppLanguage.SPANISH -> "Subir Música"
        AppLanguage.FRENCH -> "Téléverser de la Musique"
        AppLanguage.GERMAN -> "Musik Hochladen"
        AppLanguage.RUSSIAN -> "Загрузить музыку"
        AppLanguage.JAPANESE -> "音楽をアップロード"
        AppLanguage.KOREAN -> "음악 업로드"
    }

    val musicName: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "音乐名称"
        AppLanguage.ENGLISH -> "Music Name"
        AppLanguage.ARABIC -> "اسم الموسيقى"
        AppLanguage.PORTUGUESE -> "Nome da Música"
        AppLanguage.SPANISH -> "Nombre de la Música"
        AppLanguage.FRENCH -> "Nom de la Musique"
        AppLanguage.GERMAN -> "Musikname"
        AppLanguage.RUSSIAN -> "Название музыки"
        AppLanguage.JAPANESE -> "音楽名"
        AppLanguage.KOREAN -> "음악 이름"
    }

    val selectMusic: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择音乐"
        AppLanguage.ENGLISH -> "Select Music"
        AppLanguage.ARABIC -> "اختيار موسيقى"
        AppLanguage.PORTUGUESE -> "Selecionar Música"
        AppLanguage.SPANISH -> "Seleccionar Música"
        AppLanguage.FRENCH -> "Sélectionner la Musique"
        AppLanguage.GERMAN -> "Musik Auswählen"
        AppLanguage.RUSSIAN -> "Выбрать музыку"
        AppLanguage.JAPANESE -> "音楽を選択"
        AppLanguage.KOREAN -> "음악 선택"
    }

    val selectCoverOptional: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择封面(可选)"
        AppLanguage.ENGLISH -> "Select Cover (Optional)"
        AppLanguage.ARABIC -> "اختيار غلاف (اختياري)"
        AppLanguage.PORTUGUESE -> "Selecionar Capa (Opcional)"
        AppLanguage.SPANISH -> "Seleccionar Portada (Opcional)"
        AppLanguage.FRENCH -> "Sélectionner la Pochette (Optionnel)"
        AppLanguage.GERMAN -> "Cover Auswählen (Optional)"
        AppLanguage.RUSSIAN -> "Выбрать обложку (опционально)"
        AppLanguage.JAPANESE -> "カバーを選択（任意）"
        AppLanguage.KOREAN -> "커버 선택(선택 사항)"
    }

    val coverTip: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "提示: 封面图片用于在选择界面展示"
        AppLanguage.ENGLISH -> "Tip: Cover image is displayed in the selection interface"
        AppLanguage.ARABIC -> "تلميح: يتم عرض صورة الغلاف في واجهة الاختيار"
        AppLanguage.PORTUGUESE -> "Dica: A imagem de capa é exibida na interface de seleção"
        AppLanguage.SPANISH -> "Consejo: La imagen de portada se muestra en la interfaz de selección"
        AppLanguage.FRENCH -> "Astuce : L'image de pochette est affichée dans l'interface de sélection"
        AppLanguage.GERMAN -> "Tipp: Cover-Bild wird in der Auswahlansicht angezeigt"
        AppLanguage.RUSSIAN -> "Подсказка: Изображение обложки отображается в интерфейсе выбора"
        AppLanguage.JAPANESE -> "ヒント: カバー画像は選択画面に表示されます"
        AppLanguage.KOREAN -> "팁: 커버 이미지는 선택 인터페이스에 표시됩니다"
    }

    val upload: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "上传"
        AppLanguage.ENGLISH -> "Upload"
        AppLanguage.ARABIC -> "رفع"
        AppLanguage.PORTUGUESE -> "Carregar"
        AppLanguage.SPANISH -> "Subir"
        AppLanguage.FRENCH -> "Téléverser"
        AppLanguage.GERMAN -> "Hochladen"
        AppLanguage.RUSSIAN -> "Загрузить"
        AppLanguage.JAPANESE -> "アップロード"
        AppLanguage.KOREAN -> "업로드"
    }

    val editTagsTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "编辑标签"
        AppLanguage.ENGLISH -> "Edit Tags"
        AppLanguage.ARABIC -> "تعديل العلامات"
        AppLanguage.PORTUGUESE -> "Editar Tags"
        AppLanguage.SPANISH -> "Editar Etiquetas"
        AppLanguage.FRENCH -> "Éditer les Tags"
        AppLanguage.GERMAN -> "Tags Bearbeiten"
        AppLanguage.RUSSIAN -> "Изменить теги"
        AppLanguage.JAPANESE -> "タグを編集"
        AppLanguage.KOREAN -> "태그 편집"
    }

    val selectTagsHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择适合的标签(可多选)"
        AppLanguage.ENGLISH -> "Select suitable tags (multiple selection)"
        AppLanguage.ARABIC -> "اختر العلامات المناسبة (اختيار متعدد)"
        AppLanguage.PORTUGUESE -> "Selecione tags adequadas (seleção múltipla)"
        AppLanguage.SPANISH -> "Seleccione etiquetas adecuadas (selección múltiple)"
        AppLanguage.FRENCH -> "Sélectionnez des tags appropriés (sélection multiple)"
        AppLanguage.GERMAN -> "Geeignete Tags auswählen (Mehrfachauswahl)"
        AppLanguage.RUSSIAN -> "Выберите подходящие теги (множественный выбор)"
        AppLanguage.JAPANESE -> "適切なタグを選択（複数選択可）"
        AppLanguage.KOREAN -> "적절한 태그 선택(다중 선택)"
    }

    val selectLyricsTheme: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择字幕主题"
        AppLanguage.ENGLISH -> "Select Lyrics Theme"
        AppLanguage.ARABIC -> "اختيار سمة كلمات الأغنية"
        AppLanguage.PORTUGUESE -> "Selecionar Tema de Letras"
        AppLanguage.SPANISH -> "Seleccionar Tema de Letras"
        AppLanguage.FRENCH -> "Sélectionner le Thème des Paroles"
        AppLanguage.GERMAN -> "Liedtext-Theme Auswählen"
        AppLanguage.RUSSIAN -> "Выбрать тему текста песни"
        AppLanguage.JAPANESE -> "歌詞テーマを選択"
        AppLanguage.KOREAN -> "가사 테마 선택"
    }

    val selectLyricsThemeHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择歌词显示的视觉风格"
        AppLanguage.ENGLISH -> "Select visual style for lyrics display"
        AppLanguage.ARABIC -> "اختر النمط المرئي لعرض كلمات الأغنية"
        AppLanguage.PORTUGUESE -> "Selecione o estilo visual para exibição das letras"
        AppLanguage.SPANISH -> "Seleccione el estilo visual para la visualización de letras"
        AppLanguage.FRENCH -> "Sélectionnez le style visuel pour l'affichage des paroles"
        AppLanguage.GERMAN -> "Visuellen Stil für die Liedtextanzeige auswählen"
        AppLanguage.RUSSIAN -> "Выберите визуальный стиль отображения текста песни"
        AppLanguage.JAPANESE -> "歌詞表示のビジュアルスタイルを選択"
        AppLanguage.KOREAN -> "가사 표시의 시각 스타일 선택"
    }

    val sampleLyricsText: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "示例歌词文本"
        AppLanguage.ENGLISH -> "Sample Lyrics Text"
        AppLanguage.ARABIC -> "نص كلمات نموذجي"
        AppLanguage.PORTUGUESE -> "Texto de Letras de Exemplo"
        AppLanguage.SPANISH -> "Texto de Letras de Ejemplo"
        AppLanguage.FRENCH -> "Texte de Paroles d'Exemple"
        AppLanguage.GERMAN -> "Beispiel-Liedtext"
        AppLanguage.RUSSIAN -> "Образец текста песни"
        AppLanguage.JAPANESE -> "サンプル歌詞テキスト"
        AppLanguage.KOREAN -> "샘플 가사 텍스트"
    }

    val lyricsPreview: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "歌词预览"
        AppLanguage.ENGLISH -> "Lyrics Preview"
        AppLanguage.ARABIC -> "معاينة كلمات الأغنية"
        AppLanguage.PORTUGUESE -> "Pré-visualização de Letras"
        AppLanguage.SPANISH -> "Vista Previa de Letras"
        AppLanguage.FRENCH -> "Aperçu des Paroles"
        AppLanguage.GERMAN -> "Liedtext-Vorschau"
        AppLanguage.RUSSIAN -> "Предпросмотр текста песни"
        AppLanguage.JAPANESE -> "歌詞プレビュー"
        AppLanguage.KOREAN -> "가사 미리보기"
    }

    val lyricsUpdated: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "[OK] 歌词已更新"
        AppLanguage.ENGLISH -> "[OK] Lyrics updated"
        AppLanguage.ARABIC -> "[OK] تم تحديث كلمات الأغنية"
        AppLanguage.PORTUGUESE -> "[OK] Letras atualizadas"
        AppLanguage.SPANISH -> "[OK] Letras actualizadas"
        AppLanguage.FRENCH -> "[OK] Paroles mises à jour"
        AppLanguage.GERMAN -> "[OK] Liedtexte aktualisiert"
        AppLanguage.RUSSIAN -> "[OK] Текст песни обновлён"
        AppLanguage.JAPANESE -> "[OK] 歌詞を更新しました"
        AppLanguage.KOREAN -> "[OK] 가사가 업데이트되었습니다"
    }

    val backward10s: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "后退10秒"
        AppLanguage.ENGLISH -> "Rewind 10s"
        AppLanguage.ARABIC -> "ترجيع 10 ثوانٍ"
        AppLanguage.PORTUGUESE -> "Retroceder 10s"
        AppLanguage.SPANISH -> "Retroceder 10s"
        AppLanguage.FRENCH -> "Reculer de 10s"
        AppLanguage.GERMAN -> "10s zurückspulen"
        AppLanguage.RUSSIAN -> "Перемотать назад на 10с"
        AppLanguage.JAPANESE -> "10秒巻き戻し"
        AppLanguage.KOREAN -> "10초 뒤로"
    }

    val forward10s: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "前进10秒"
        AppLanguage.ENGLISH -> "Forward 10s"
        AppLanguage.ARABIC -> "تقديم 10 ثوانٍ"
        AppLanguage.PORTUGUESE -> "Avançar 10s"
        AppLanguage.SPANISH -> "Avanzar 10s"
        AppLanguage.FRENCH -> "Avancer de 10s"
        AppLanguage.GERMAN -> "10s vorspulen"
        AppLanguage.RUSSIAN -> "Перемотать вперёд на 10с"
        AppLanguage.JAPANESE -> "10秒早送り"
        AppLanguage.KOREAN -> "10초 앞으로"
    }

    val selectAll: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "全选"
        AppLanguage.ENGLISH -> "Select All"
        AppLanguage.ARABIC -> "تحديد الكل"
        AppLanguage.PORTUGUESE -> "Selecionar Tudo"
        AppLanguage.SPANISH -> "Seleccionar Todo"
        AppLanguage.FRENCH -> "Tout Sélectionner"
        AppLanguage.GERMAN -> "Alle Auswählen"
        AppLanguage.RUSSIAN -> "Выбрать всё"
        AppLanguage.JAPANESE -> "すべて選択"
        AppLanguage.KOREAN -> "모두 선택"
    }

    val deselectAll: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "取消全选"
        AppLanguage.ENGLISH -> "Deselect All"
        AppLanguage.ARABIC -> "إلغاء تحديد الكل"
        AppLanguage.PORTUGUESE -> "Desselecionar Tudo"
        AppLanguage.SPANISH -> "Deseleccionar Todo"
        AppLanguage.FRENCH -> "Tout Désélectionner"
        AppLanguage.GERMAN -> "Alle Abwählen"
        AppLanguage.RUSSIAN -> "Снять выделение со всего"
        AppLanguage.JAPANESE -> "すべて選択解除"
        AppLanguage.KOREAN -> "모두 선택 해제"
    }

    val noSearchResult: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "没有匹配的结果"
        AppLanguage.ENGLISH -> "No matching results"
        AppLanguage.ARABIC -> "لا توجد نتائج مطابقة"
        AppLanguage.PORTUGUESE -> "Sem resultados correspondentes"
        AppLanguage.SPANISH -> "Sin resultados coincidentes"
        AppLanguage.FRENCH -> "Aucun résultat correspondant"
        AppLanguage.GERMAN -> "Keine passenden Ergebnisse"
        AppLanguage.RUSSIAN -> "Нет совпадающих результатов"
        AppLanguage.JAPANESE -> "一致する結果がありません"
        AppLanguage.KOREAN -> "일치하는 결과가 없습니다"
    }

    val popupBlockerSetting: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "弹窗拦截"
        AppLanguage.ENGLISH -> "Popup Blocker"
        AppLanguage.ARABIC -> "حظر النوافذ المنبثقة"
        AppLanguage.PORTUGUESE -> "Bloqueador de Pop-up"
        AppLanguage.SPANISH -> "Bloqueador de Pop-up"
        AppLanguage.FRENCH -> "Bloqueur de Pop-up"
        AppLanguage.GERMAN -> "Popup-Blocker"
        AppLanguage.RUSSIAN -> "Блокировщик всплывающих окон"
        AppLanguage.JAPANESE -> "ポップアップブロッカー"
        AppLanguage.KOREAN -> "팝업 차단기"
    }

    val popupBlockerSettingHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "拦截广告弹窗。如果网站按钮/菜单/搜索无法点击，请关闭此选项"
        AppLanguage.ENGLISH -> "Block ad popups. If website buttons/menus/search don't work, disable this option"
        AppLanguage.ARABIC -> "حظر النوافذ المنبثقة الإعلانية. إذا لم تعمل أزرار/قوائم/بحث الموقع، قم بتعطيل هذا الخيار"
        AppLanguage.PORTUGUESE -> "Bloqueia pop-ups de anúncios. Se botões/menus/pesquisa do site não funcionarem, desative esta opção"
        AppLanguage.SPANISH -> "Bloquea pop-ups de anuncios. Si los botones/menús/búsqueda del sitio no funcionan, desactive esta opción"
        AppLanguage.FRENCH -> "Bloque les pop-ups publicitaires. Si les boutons/menus/recherche du site ne fonctionnent pas, désactivez cette option"
        AppLanguage.GERMAN -> "Blockiert Werbe-Popups. Wenn Website-Buttons/Menüs/Suche nicht funktionieren, diese Option deaktivieren"
        AppLanguage.RUSSIAN -> "Блокирует рекламные всплывающие окна. Если кнопки/меню/поиск сайта не работают, отключите эту опцию"
        AppLanguage.JAPANESE -> "広告ポップアップをブロック。サイトのボタン/メニュー/検索が機能しない場合は、このオプションを無効化してください"
        AppLanguage.KOREAN -> "광고 팝업을 차단합니다. 사이트 버튼/메뉴/검색이 작동하지 않으면 이 옵션을 비활성화하세요"
    }

    val aiIcon: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "AI图标"
        AppLanguage.ENGLISH -> "AI Icon"
        AppLanguage.ARABIC -> "أيقونة AI"
        AppLanguage.PORTUGUESE -> "Ícone de IA"
        AppLanguage.SPANISH -> "Icono de IA"
        AppLanguage.FRENCH -> "Icône IA"
        AppLanguage.GERMAN -> "KI-Icon"
        AppLanguage.RUSSIAN -> "Иконка ИИ"
        AppLanguage.JAPANESE -> "AIアイコン"
        AppLanguage.KOREAN -> "AI 아이콘"
    }

    val icon: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "图标"
        AppLanguage.ENGLISH -> "Icon"
        AppLanguage.ARABIC -> "أيقونة"
        AppLanguage.PORTUGUESE -> "Ícone"
        AppLanguage.SPANISH -> "Icono"
        AppLanguage.FRENCH -> "Icône"
        AppLanguage.GERMAN -> "Symbol"
        AppLanguage.RUSSIAN -> "Иконка"
        AppLanguage.JAPANESE -> "アイコン"
        AppLanguage.KOREAN -> "아이콘"
    }

    val availableFor: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "可用于"
        AppLanguage.ENGLISH -> "Available for"
        AppLanguage.ARABIC -> "متاح لـ"
        AppLanguage.PORTUGUESE -> "Disponível para"
        AppLanguage.SPANISH -> "Disponible para"
        AppLanguage.FRENCH -> "Disponible pour"
        AppLanguage.GERMAN -> "Verfügbar für"
        AppLanguage.RUSSIAN -> "Доступно для"
        AppLanguage.JAPANESE -> "利用可能"
        AppLanguage.KOREAN -> "사용 가능"
    }

    val savedToGallery: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "%s已保存到相册"
        AppLanguage.ENGLISH -> "%s saved to gallery"
        AppLanguage.ARABIC -> "تم حفظ %s في المعرض"
        AppLanguage.PORTUGUESE -> "%s salvo na galeria"
        AppLanguage.SPANISH -> "%s guardado en la galería"
        AppLanguage.FRENCH -> "%s enregistré dans la galerie"
        AppLanguage.GERMAN -> "%s in Galerie gespeichert"
        AppLanguage.RUSSIAN -> "%s сохранено в галерею"
        AppLanguage.JAPANESE -> "%sをギャラリーに保存しました"
        AppLanguage.KOREAN -> "%s이(가) 갤러리에 저장되었습니다"
    }

    val pleaseActivateApp: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "请先激活应用"
        AppLanguage.ENGLISH -> "Please activate the app first"
        AppLanguage.ARABIC -> "يرجى تفعيل التطبيق أولاً"
        AppLanguage.PORTUGUESE -> "Por favor, ative o app primeiro"
        AppLanguage.SPANISH -> "Por favor, active el app primero"
        AppLanguage.FRENCH -> "Veuillez d'abord activer l'app"
        AppLanguage.GERMAN -> "Bitte zuerst die App aktivieren"
        AppLanguage.RUSSIAN -> "Сначала активируйте приложение"
        AppLanguage.JAPANESE -> "まずアプリを有効化してください"
        AppLanguage.KOREAN -> "먼저 앱을 활성화해 주세요"
    }

    val enterActivationCode: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "输入激活码"
        AppLanguage.ENGLISH -> "Enter Activation Code"
        AppLanguage.ARABIC -> "أدخل رمز التفعيل"
        AppLanguage.PORTUGUESE -> "Digite o Código de Ativação"
        AppLanguage.SPANISH -> "Ingrese el Código de Activación"
        AppLanguage.FRENCH -> "Saisir le Code d'Activation"
        AppLanguage.GERMAN -> "Aktivierungscode Eingeben"
        AppLanguage.RUSSIAN -> "Введите код активации"
        AppLanguage.JAPANESE -> "アクティベーションコードを入力"
        AppLanguage.KOREAN -> "활성화 코드 입력"
    }

    val enterCodeToContinue: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "请输入激活码以继续使用"
        AppLanguage.ENGLISH -> "Please enter activation code to continue"
        AppLanguage.ARABIC -> "يرجى إدخال رمز التفعيل للمتابعة"
        AppLanguage.PORTUGUESE -> "Por favor, digite o código de ativação para continuar"
        AppLanguage.SPANISH -> "Por favor, ingrese el código de activación para continuar"
        AppLanguage.FRENCH -> "Veuillez saisir le code d'activation pour continuer"
        AppLanguage.GERMAN -> "Bitte Aktivierungscode eingeben, um fortzufahren"
        AppLanguage.RUSSIAN -> "Пожалуйста, введите код активации для продолжения"
        AppLanguage.JAPANESE -> "続行するにはアクティベーションコードを入力してください"
        AppLanguage.KOREAN -> "계속하려면 활성화 코드를 입력해 주세요"
    }

    val themeKimiNoNawa: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "你的名字"
        AppLanguage.ENGLISH -> "Your Name"
        AppLanguage.ARABIC -> "اسمك"
        AppLanguage.PORTUGUESE -> "Seu Nome"
        AppLanguage.SPANISH -> "Tu Nombre"
        AppLanguage.FRENCH -> "Votre Nom"
        AppLanguage.GERMAN -> "Dein Name"
        AppLanguage.RUSSIAN -> "Твоё имя"
        AppLanguage.JAPANESE -> "君の名は"
        AppLanguage.KOREAN -> "너의 이름은"
    }

    val themeKimiNoNawaDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "黄昏天空的彗星轨迹，新海诚式的浪漫黄昏"
        AppLanguage.ENGLISH -> "Comet trails across twilight sky, Shinkai-style romantic dusk"
        AppLanguage.ARABIC -> "آثار المذنب عبر سماء الشفق، غسق رومانسي بأسلوب شينكاي"
        AppLanguage.PORTUGUESE -> "Trilhas de cometa pelo céu do crepúsculo, entardecer romântico estilo Shinkai"
        AppLanguage.SPANISH -> "Estelas de cometa a través del cielo del crepúsculo, anochecer romántico estilo Shinkai"
        AppLanguage.FRENCH -> "Traînées de comète à travers le ciel crépusculaire, crépuscule romantique style Shinkai"
        AppLanguage.GERMAN -> "Kometenschweife am Dämmerungshimmel, romantische Abenddämmerung im Shinkai-Stil"
        AppLanguage.RUSSIAN -> "Следы кометы по сумеречному небу, романтические сумерки в стиле Синкая"
        AppLanguage.JAPANESE -> "黄昏の空の彗星の軌跡、新海誠風のロマンチックな夕暮れ"
        AppLanguage.KOREAN -> "황혼 하늘의 혜성 궤적, 신카이 스타일의 로맨틱한 황혼"
    }

    val uiConfig: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "UI 配置"
        AppLanguage.ENGLISH -> "UI Config"
        AppLanguage.ARABIC -> "تكوين الواجهة"
        AppLanguage.PORTUGUESE -> "Configuração de UI"
        AppLanguage.SPANISH -> "Configuración de IU"
        AppLanguage.FRENCH -> "Configuration de l'IU"
        AppLanguage.GERMAN -> "UI-Konfiguration"
        AppLanguage.RUSSIAN -> "Настройка интерфейса"
        AppLanguage.JAPANESE -> "UI 設定"
        AppLanguage.KOREAN -> "UI 설정"
    }

    val settingsThemeColor: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "主题色"
        AppLanguage.ENGLISH -> "Theme color"
        AppLanguage.ARABIC -> "لون السمة"
        AppLanguage.PORTUGUESE -> "Cor do tema"
        AppLanguage.SPANISH -> "Color del tema"
        AppLanguage.FRENCH -> "Couleur du thème"
        AppLanguage.GERMAN -> "Themenfarbe"
        AppLanguage.RUSSIAN -> "Цвет темы"
        AppLanguage.JAPANESE -> "テーマカラー"
        AppLanguage.KOREAN -> "테마 색상"
    }

    val settingsThemeColorDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "为界面选择一组强调色"
        AppLanguage.ENGLISH -> "Pick an accent palette for the interface"
        AppLanguage.ARABIC -> "اختر لوحة ألوان مميزة للواجهة"
        AppLanguage.PORTUGUESE -> "Escolha uma paleta de destaque para a interface"
        AppLanguage.SPANISH -> "Elige una paleta de acento para la interfaz"
        AppLanguage.FRENCH -> "Choisissez une palette d'accent pour l'interface"
        AppLanguage.GERMAN -> "Akzentpalette für die Oberfläche wählen"
        AppLanguage.RUSSIAN -> "Выберите акцентную палитру интерфейса"
        AppLanguage.JAPANESE -> "インターフェースのアクセントカラーを選択"
        AppLanguage.KOREAN -> "인터페이스 강조 색상을 선택하세요"
    }

    val settingsDarkMode: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "深色模式"
        AppLanguage.ENGLISH -> "Dark mode"
        AppLanguage.ARABIC -> "الوضع الداكن"
        AppLanguage.PORTUGUESE -> "Modo escuro"
        AppLanguage.SPANISH -> "Modo oscuro"
        AppLanguage.FRENCH -> "Mode sombre"
        AppLanguage.GERMAN -> "Dunkelmodus"
        AppLanguage.RUSSIAN -> "Тёмный режим"
        AppLanguage.JAPANESE -> "ダークモード"
        AppLanguage.KOREAN -> "다크 모드"
    }

    val settingsAnimation: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "动画"
        AppLanguage.ENGLISH -> "Animations"
        AppLanguage.ARABIC -> "الحركات"
        AppLanguage.PORTUGUESE -> "Animações"
        AppLanguage.SPANISH -> "Animaciones"
        AppLanguage.FRENCH -> "Animations"
        AppLanguage.GERMAN -> "Animationen"
        AppLanguage.RUSSIAN -> "Анимации"
        AppLanguage.JAPANESE -> "アニメーション"
        AppLanguage.KOREAN -> "애니메이션"
    }

    val animEnableAll: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "启用动画"
        AppLanguage.ENGLISH -> "Enable animations"
        AppLanguage.ARABIC -> "تفعيل الحركات"
        AppLanguage.PORTUGUESE -> "Ativar animações"
        AppLanguage.SPANISH -> "Activar animaciones"
        AppLanguage.FRENCH -> "Activer les animations"
        AppLanguage.GERMAN -> "Animationen aktivieren"
        AppLanguage.RUSSIAN -> "Включить анимации"
        AppLanguage.JAPANESE -> "アニメーションを有効化"
        AppLanguage.KOREAN -> "애니메이션 사용"
    }

    val animHaptics: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "触觉反馈"
        AppLanguage.ENGLISH -> "Haptic feedback"
        AppLanguage.ARABIC -> "الاستجابة اللمسية"
        AppLanguage.PORTUGUESE -> "Resposta tátil"
        AppLanguage.SPANISH -> "Respuesta háptica"
        AppLanguage.FRENCH -> "Retour haptique"
        AppLanguage.GERMAN -> "Haptisches Feedback"
        AppLanguage.RUSSIAN -> "Тактильный отклик"
        AppLanguage.JAPANESE -> "触覚フィードバック"
        AppLanguage.KOREAN -> "햅틱 피드백"
    }

    val animSpeedTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "动画速度"
        AppLanguage.ENGLISH -> "Animation speed"
        AppLanguage.ARABIC -> "سرعة الحركة"
        AppLanguage.PORTUGUESE -> "Velocidade da animação"
        AppLanguage.SPANISH -> "Velocidad de animación"
        AppLanguage.FRENCH -> "Vitesse d'animation"
        AppLanguage.GERMAN -> "Animationsgeschwindigkeit"
        AppLanguage.RUSSIAN -> "Скорость анимации"
        AppLanguage.JAPANESE -> "アニメーション速度"
        AppLanguage.KOREAN -> "애니메이션 속도"
    }

    val accentPeriwinkleMint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "雾蓝薄荷"
        AppLanguage.ENGLISH -> "Periwinkle Mint"
        AppLanguage.ARABIC -> "أزرق ونعناعي"
        AppLanguage.PORTUGUESE -> "Azul e Menta"
        AppLanguage.SPANISH -> "Azul y Menta"
        AppLanguage.FRENCH -> "Pervenche et menthe"
        AppLanguage.GERMAN -> "Blau und Minze"
        AppLanguage.RUSSIAN -> "Сине-мятный"
        AppLanguage.JAPANESE -> "ブルーミント"
        AppLanguage.KOREAN -> "블루 민트"
    }

    val accentDynamic: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "跟随壁纸"
        AppLanguage.ENGLISH -> "Wallpaper"
        AppLanguage.ARABIC -> "من الخلفية"
        AppLanguage.PORTUGUESE -> "Do papel de parede"
        AppLanguage.SPANISH -> "Del fondo"
        AppLanguage.FRENCH -> "Du fond d'écran"
        AppLanguage.GERMAN -> "Vom Hintergrund"
        AppLanguage.RUSSIAN -> "Из обоев"
        AppLanguage.JAPANESE -> "壁紙から"
        AppLanguage.KOREAN -> "배경 화면"
    }

    val accentCharcoalPink: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "甜酷粉"
        AppLanguage.ENGLISH -> "Sweet pink"
        AppLanguage.ARABIC -> "وردي جريء"
        AppLanguage.PORTUGUESE -> "Rosa ousado"
        AppLanguage.SPANISH -> "Rosa audaz"
        AppLanguage.FRENCH -> "Rose audacieux"
        AppLanguage.GERMAN -> "Kräftiges Pink"
        AppLanguage.RUSSIAN -> "Дерзкий розовый"
        AppLanguage.JAPANESE -> "スイートピンク"
        AppLanguage.KOREAN -> "스위트 핑크"
    }

    val accentRoyalPurple: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "帝王紫"
        AppLanguage.ENGLISH -> "Royal purple"
        AppLanguage.ARABIC -> "بنفسجي ملكي"
        AppLanguage.PORTUGUESE -> "Roxo real"
        AppLanguage.SPANISH -> "Púrpura real"
        AppLanguage.FRENCH -> "Violet royal"
        AppLanguage.GERMAN -> "Königspurpur"
        AppLanguage.RUSSIAN -> "Королевский пурпур"
        AppLanguage.JAPANESE -> "ロイヤルパープル"
        AppLanguage.KOREAN -> "로열 퍼플"
    }

    val accentDeepSea: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "深海蓝"
        AppLanguage.ENGLISH -> "Deep sea"
        AppLanguage.ARABIC -> "أزرق أعماق البحر"
        AppLanguage.PORTUGUESE -> "Azul profundo"
        AppLanguage.SPANISH -> "Azul marino"
        AppLanguage.FRENCH -> "Bleu profond"
        AppLanguage.GERMAN -> "Tiefseeblau"
        AppLanguage.RUSSIAN -> "Глубокий синий"
        AppLanguage.JAPANESE -> "深海ブルー"
        AppLanguage.KOREAN -> "딥시 블루"
    }

    val accentIvyViolet: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "藤紫青柠"
        AppLanguage.ENGLISH -> "Ivy violet"
        AppLanguage.ARABIC -> "لبلاب بنفسجي"
        AppLanguage.PORTUGUESE -> "Violeta hera"
        AppLanguage.SPANISH -> "Violeta hiedra"
        AppLanguage.FRENCH -> "Violet lierre"
        AppLanguage.GERMAN -> "Efeuviolett"
        AppLanguage.RUSSIAN -> "Плющ и фиалка"
        AppLanguage.JAPANESE -> "アイビーバイオレット"
        AppLanguage.KOREAN -> "아이비 바이올렛"
    }

    val accentSakuraForest: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "樱花墨绿"
        AppLanguage.ENGLISH -> "Sakura ink"
        AppLanguage.ARABIC -> "ساكورا داكن"
        AppLanguage.PORTUGUESE -> "Sakura escura"
        AppLanguage.SPANISH -> "Sakura oscura"
        AppLanguage.FRENCH -> "Sakura encre"
        AppLanguage.GERMAN -> "Sakura-Tinte"
        AppLanguage.RUSSIAN -> "Сакура и хвоя"
        AppLanguage.JAPANESE -> "桜と深緑"
        AppLanguage.KOREAN -> "사쿠라 잉크"
    }

    val accentInkAqua: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "捣蓝清水"
        AppLanguage.ENGLISH -> "Ink aqua"
        AppLanguage.ARABIC -> "أزرق حبري"
        AppLanguage.PORTUGUESE -> "Azul tinta"
        AppLanguage.SPANISH -> "Azul tinta"
        AppLanguage.FRENCH -> "Bleu encre"
        AppLanguage.GERMAN -> "Tintenblau"
        AppLanguage.RUSSIAN -> "Чернила и вода"
        AppLanguage.JAPANESE -> "藍と水"
        AppLanguage.KOREAN -> "잉크 아쿠아"
    }

    val accentSunsetHaze: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "落日橙"
        AppLanguage.ENGLISH -> "Sunset haze"
        AppLanguage.ARABIC -> "برتقالي الغروب"
        AppLanguage.PORTUGUESE -> "Laranja poente"
        AppLanguage.SPANISH -> "Naranja atardecer"
        AppLanguage.FRENCH -> "Orange couchant"
        AppLanguage.GERMAN -> "Abendrot"
        AppLanguage.RUSSIAN -> "Закатный оранж"
        AppLanguage.JAPANESE -> "夕焼けオレンジ"
        AppLanguage.KOREAN -> "선셋 헤이즈"
    }

    val accentCamellia: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "茶花红"
        AppLanguage.ENGLISH -> "Camellia"
        AppLanguage.ARABIC -> "أحمر الكاميليا"
        AppLanguage.PORTUGUESE -> "Vermelho camélia"
        AppLanguage.SPANISH -> "Rojo camelia"
        AppLanguage.FRENCH -> "Rouge camélia"
        AppLanguage.GERMAN -> "Kamelienrot"
        AppLanguage.RUSSIAN -> "Красная камелия"
        AppLanguage.JAPANESE -> "椿レッド"
        AppLanguage.KOREAN -> "카멜리아 레드"
    }

    val accentFrostCyan: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "冰晶蓝青"
        AppLanguage.ENGLISH -> "Frost cyan"
        AppLanguage.ARABIC -> "أزرق جليدي"
        AppLanguage.PORTUGUESE -> "Ciano glacial"
        AppLanguage.SPANISH -> "Cian glacial"
        AppLanguage.FRENCH -> "Cyan givré"
        AppLanguage.GERMAN -> "Frostblau"
        AppLanguage.RUSSIAN -> "Ледяной циан"
        AppLanguage.JAPANESE -> "氷晶シアン"
        AppLanguage.KOREAN -> "프로스트 시안"
    }

    val accentMochaLatte: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "摩卡拿铁"
        AppLanguage.ENGLISH -> "Mocha latte"
        AppLanguage.ARABIC -> "موكا لاتيه"
        AppLanguage.PORTUGUESE -> "Mocha latte"
        AppLanguage.SPANISH -> "Moka latte"
        AppLanguage.FRENCH -> "Moka latte"
        AppLanguage.GERMAN -> "Mokka"
        AppLanguage.RUSSIAN -> "Мокко латте"
        AppLanguage.JAPANESE -> "モカラテ"
        AppLanguage.KOREAN -> "모카 라떼"
    }

    val settingsInterface: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "界面"
        AppLanguage.ENGLISH -> "Interface"
        AppLanguage.ARABIC -> "الواجهة"
        AppLanguage.PORTUGUESE -> "Interface"
        AppLanguage.SPANISH -> "Interfaz"
        AppLanguage.FRENCH -> "Interface"
        AppLanguage.GERMAN -> "Oberfläche"
        AppLanguage.RUSSIAN -> "Интерфейс"
        AppLanguage.JAPANESE -> "インターフェース"
        AppLanguage.KOREAN -> "인터페이스"
    }

    val fontSizeTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "字体大小"
        AppLanguage.ENGLISH -> "Font size"
        AppLanguage.ARABIC -> "حجم الخط"
        AppLanguage.PORTUGUESE -> "Tamanho da fonte"
        AppLanguage.SPANISH -> "Tamaño de fuente"
        AppLanguage.FRENCH -> "Taille de police"
        AppLanguage.GERMAN -> "Schriftgröße"
        AppLanguage.RUSSIAN -> "Размер шрифта"
        AppLanguage.JAPANESE -> "フォントサイズ"
        AppLanguage.KOREAN -> "글꼴 크기"
    }

    val fontSizeSmall: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "偏小"
        AppLanguage.ENGLISH -> "Small"
        AppLanguage.ARABIC -> "صغير"
        AppLanguage.PORTUGUESE -> "Pequena"
        AppLanguage.SPANISH -> "Pequeña"
        AppLanguage.FRENCH -> "Petite"
        AppLanguage.GERMAN -> "Klein"
        AppLanguage.RUSSIAN -> "Мелкий"
        AppLanguage.JAPANESE -> "小"
        AppLanguage.KOREAN -> "작게"
    }

    val fontSizeStandard: String get() = when (Strings.lang) {
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

    val fontSizeLarge: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "偏大"
        AppLanguage.ENGLISH -> "Large"
        AppLanguage.ARABIC -> "كبير"
        AppLanguage.PORTUGUESE -> "Grande"
        AppLanguage.SPANISH -> "Grande"
        AppLanguage.FRENCH -> "Grande"
        AppLanguage.GERMAN -> "Groß"
        AppLanguage.RUSSIAN -> "Крупный"
        AppLanguage.JAPANESE -> "大"
        AppLanguage.KOREAN -> "크게"
    }

    val fontSizeXLarge: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "特大"
        AppLanguage.ENGLISH -> "Extra large"
        AppLanguage.ARABIC -> "كبير جداً"
        AppLanguage.PORTUGUESE -> "Extra grande"
        AppLanguage.SPANISH -> "Extra grande"
        AppLanguage.FRENCH -> "Très grande"
        AppLanguage.GERMAN -> "Sehr groß"
        AppLanguage.RUSSIAN -> "Очень крупный"
        AppLanguage.JAPANESE -> "特大"
        AppLanguage.KOREAN -> "매우 크게"
    }

    val cornerStyleTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "圆角风格"
        AppLanguage.ENGLISH -> "Corner style"
        AppLanguage.ARABIC -> "نمط الزوايا"
        AppLanguage.PORTUGUESE -> "Estilo dos cantos"
        AppLanguage.SPANISH -> "Estilo de esquinas"
        AppLanguage.FRENCH -> "Style des coins"
        AppLanguage.GERMAN -> "Eckenstil"
        AppLanguage.RUSSIAN -> "Стиль углов"
        AppLanguage.JAPANESE -> "角丸スタイル"
        AppLanguage.KOREAN -> "모서리 스타일"
    }

    val cornerSharp: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "硬朗"
        AppLanguage.ENGLISH -> "Sharp"
        AppLanguage.ARABIC -> "حاد"
        AppLanguage.PORTUGUESE -> "Reto"
        AppLanguage.SPANISH -> "Definido"
        AppLanguage.FRENCH -> "Anguleux"
        AppLanguage.GERMAN -> "Kantig"
        AppLanguage.RUSSIAN -> "Резкий"
        AppLanguage.JAPANESE -> "シャープ"
        AppLanguage.KOREAN -> "각진"
    }

    val cornerStandard: String get() = when (Strings.lang) {
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

    val cornerRounded: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "圆润"
        AppLanguage.ENGLISH -> "Rounded"
        AppLanguage.ARABIC -> "مستدير"
        AppLanguage.PORTUGUESE -> "Arredondado"
        AppLanguage.SPANISH -> "Redondeado"
        AppLanguage.FRENCH -> "Arrondi"
        AppLanguage.GERMAN -> "Rund"
        AppLanguage.RUSSIAN -> "Скруглённый"
        AppLanguage.JAPANESE -> "丸み"
        AppLanguage.KOREAN -> "둥근"
    }

    val catContentFilter: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "内容过滤"
        AppLanguage.ENGLISH -> "Content Filter"
        AppLanguage.ARABIC -> "تصفية المحتوى"
        AppLanguage.PORTUGUESE -> "Filtro de Conteúdo"
        AppLanguage.SPANISH -> "Filtro de Contenido"
        AppLanguage.FRENCH -> "Filtre de Contenu"
        AppLanguage.GERMAN -> "Inhaltsfilter"
        AppLanguage.RUSSIAN -> "Фильтр контента"
        AppLanguage.JAPANESE -> "コンテンツフィルター"
        AppLanguage.KOREAN -> "콘텐츠 필터"
    }

    val catContentEnhance: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "内容增强"
        AppLanguage.ENGLISH -> "Content Enhance"
        AppLanguage.ARABIC -> "تحسين المحتوى"
        AppLanguage.PORTUGUESE -> "Aprimoramento de Conteúdo"
        AppLanguage.SPANISH -> "Mejora de Contenido"
        AppLanguage.FRENCH -> "Amélioration du Contenu"
        AppLanguage.GERMAN -> "Inhaltserweiterung"
        AppLanguage.RUSSIAN -> "Улучшение контента"
        AppLanguage.JAPANESE -> "コンテンツ強化"
        AppLanguage.KOREAN -> "콘텐츠 향상"
    }

    val catStyleModifier: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "样式修改"
        AppLanguage.ENGLISH -> "Style Modifier"
        AppLanguage.ARABIC -> "معدل الأنماط"
        AppLanguage.PORTUGUESE -> "Modificador de Estilo"
        AppLanguage.SPANISH -> "Modificador de Estilo"
        AppLanguage.FRENCH -> "Modificateur de Style"
        AppLanguage.GERMAN -> "Stilmodifikator"
        AppLanguage.RUSSIAN -> "Модификатор стиля"
        AppLanguage.JAPANESE -> "スタイル修飾子"
        AppLanguage.KOREAN -> "스타일 수정자"
    }

    val catTheme: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "主题美化"
        AppLanguage.ENGLISH -> "Theme"
        AppLanguage.ARABIC -> "السمة"
        AppLanguage.PORTUGUESE -> "Tema"
        AppLanguage.SPANISH -> "Tema"
        AppLanguage.FRENCH -> "Thème"
        AppLanguage.GERMAN -> "Thema"
        AppLanguage.RUSSIAN -> "Тема"
        AppLanguage.JAPANESE -> "テーマ"
        AppLanguage.KOREAN -> "테마"
    }

    val catFunctionEnhance: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "功能增强"
        AppLanguage.ENGLISH -> "Function Enhance"
        AppLanguage.ARABIC -> "تحسين الوظائف"
        AppLanguage.PORTUGUESE -> "Aprimoramento de Funções"
        AppLanguage.SPANISH -> "Mejora de Funciones"
        AppLanguage.FRENCH -> "Amélioration des Fonctions"
        AppLanguage.GERMAN -> "Funktionsverbesserung"
        AppLanguage.RUSSIAN -> "Улучшение функций"
        AppLanguage.JAPANESE -> "機能強化"
        AppLanguage.KOREAN -> "기능 향상"
    }

    val catAutomation: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "自动化"
        AppLanguage.ENGLISH -> "Automation"
        AppLanguage.ARABIC -> "الأتمتة"
        AppLanguage.PORTUGUESE -> "Automação"
        AppLanguage.SPANISH -> "Automatización"
        AppLanguage.FRENCH -> "Automatisation"
        AppLanguage.GERMAN -> "Automatisierung"
        AppLanguage.RUSSIAN -> "Автоматизация"
        AppLanguage.JAPANESE -> "自動化"
        AppLanguage.KOREAN -> "자동화"
    }

    val catNavigation: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "导航辅助"
        AppLanguage.ENGLISH -> "Navigation"
        AppLanguage.ARABIC -> "المساعدة في التنقل"
        AppLanguage.PORTUGUESE -> "Navegação"
        AppLanguage.SPANISH -> "Navegación"
        AppLanguage.FRENCH -> "Navigation"
        AppLanguage.GERMAN -> "Navigation"
        AppLanguage.RUSSIAN -> "Навигация"
        AppLanguage.JAPANESE -> "ナビゲーション"
        AppLanguage.KOREAN -> "내비게이션"
    }

    val catDataExtract: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "数据提取"
        AppLanguage.ENGLISH -> "Data Extract"
        AppLanguage.ARABIC -> "استخراج البيانات"
        AppLanguage.PORTUGUESE -> "Extração de Dados"
        AppLanguage.SPANISH -> "Extracción de Datos"
        AppLanguage.FRENCH -> "Extraction de Données"
        AppLanguage.GERMAN -> "Datenextraktion"
        AppLanguage.RUSSIAN -> "Извлечение данных"
        AppLanguage.JAPANESE -> "データ抽出"
        AppLanguage.KOREAN -> "데이터 추출"
    }

    val catDataSave: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "数据保存"
        AppLanguage.ENGLISH -> "Data Save"
        AppLanguage.ARABIC -> "حفظ البيانات"
        AppLanguage.PORTUGUESE -> "Salvamento de Dados"
        AppLanguage.SPANISH -> "Guardado de Datos"
        AppLanguage.FRENCH -> "Sauvegarde de Données"
        AppLanguage.GERMAN -> "Datenspeicherung"
        AppLanguage.RUSSIAN -> "Сохранение данных"
        AppLanguage.JAPANESE -> "データ保存"
        AppLanguage.KOREAN -> "데이터 저장"
    }

    val catInteraction: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "交互增强"
        AppLanguage.ENGLISH -> "Interaction"
        AppLanguage.ARABIC -> "تحسين التفاعل"
        AppLanguage.PORTUGUESE -> "Interação"
        AppLanguage.SPANISH -> "Interacción"
        AppLanguage.FRENCH -> "Interaction"
        AppLanguage.GERMAN -> "Interaktion"
        AppLanguage.RUSSIAN -> "Взаимодействие"
        AppLanguage.JAPANESE -> "インタラクション"
        AppLanguage.KOREAN -> "인터랙션"
    }

    val catAccessibility: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "无障碍"
        AppLanguage.ENGLISH -> "Accessibility"
        AppLanguage.ARABIC -> "إمكانية الوصول"
        AppLanguage.PORTUGUESE -> "Acessibilidade"
        AppLanguage.SPANISH -> "Accesibilidad"
        AppLanguage.FRENCH -> "Accessibilité"
        AppLanguage.GERMAN -> "Barrierefreiheit"
        AppLanguage.RUSSIAN -> "Спец. возможности"
        AppLanguage.JAPANESE -> "アクセシビリティ"
        AppLanguage.KOREAN -> "접근성"
    }

    val catMedia: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "媒体处理"
        AppLanguage.ENGLISH -> "Media"
        AppLanguage.ARABIC -> "معالجة الوسائط"
        AppLanguage.PORTUGUESE -> "Mídia"
        AppLanguage.SPANISH -> "Multimedia"
        AppLanguage.FRENCH -> "Média"
        AppLanguage.GERMAN -> "Medien"
        AppLanguage.RUSSIAN -> "Медиа"
        AppLanguage.JAPANESE -> "メディア"
        AppLanguage.KOREAN -> "미디어"
    }

    val catVideo: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "视频增强"
        AppLanguage.ENGLISH -> "Video"
        AppLanguage.ARABIC -> "تحسين الفيديو"
        AppLanguage.PORTUGUESE -> "Vídeo"
        AppLanguage.SPANISH -> "Vídeo"
        AppLanguage.FRENCH -> "Vidéo"
        AppLanguage.GERMAN -> "Video"
        AppLanguage.RUSSIAN -> "Видео"
        AppLanguage.JAPANESE -> "動画"
        AppLanguage.KOREAN -> "비디오"
    }

    val catImage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "图片处理"
        AppLanguage.ENGLISH -> "Image"
        AppLanguage.ARABIC -> "معالجة الصور"
        AppLanguage.PORTUGUESE -> "Imagem"
        AppLanguage.SPANISH -> "Imagen"
        AppLanguage.FRENCH -> "Image"
        AppLanguage.GERMAN -> "Bild"
        AppLanguage.RUSSIAN -> "Изображение"
        AppLanguage.JAPANESE -> "画像"
        AppLanguage.KOREAN -> "이미지"
    }

    val catAudio: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "音频控制"
        AppLanguage.ENGLISH -> "Audio"
        AppLanguage.ARABIC -> "التحكم في الصوت"
        AppLanguage.PORTUGUESE -> "Áudio"
        AppLanguage.SPANISH -> "Audio"
        AppLanguage.FRENCH -> "Audio"
        AppLanguage.GERMAN -> "Audio"
        AppLanguage.RUSSIAN -> "Аудио"
        AppLanguage.JAPANESE -> "オーディオ"
        AppLanguage.KOREAN -> "오디오"
    }

    val catSecurity: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "安全隐私"
        AppLanguage.ENGLISH -> "Security"
        AppLanguage.ARABIC -> "الأمان والخصوصية"
        AppLanguage.PORTUGUESE -> "Segurança"
        AppLanguage.SPANISH -> "Seguridad"
        AppLanguage.FRENCH -> "Sécurité"
        AppLanguage.GERMAN -> "Sicherheit"
        AppLanguage.RUSSIAN -> "Безопасность"
        AppLanguage.JAPANESE -> "セキュリティ"
        AppLanguage.KOREAN -> "보안"
    }

    val catAntiTracking: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "反追踪"
        AppLanguage.ENGLISH -> "Anti-Tracking"
        AppLanguage.ARABIC -> "مكافحة التتبع"
        AppLanguage.PORTUGUESE -> "Antirrastreamento"
        AppLanguage.SPANISH -> "Antirastreo"
        AppLanguage.FRENCH -> "Anti-Pistage"
        AppLanguage.GERMAN -> "Tracking-Schutz"
        AppLanguage.RUSSIAN -> "Анти-трекинг"
        AppLanguage.JAPANESE -> "アンチトラッキング"
        AppLanguage.KOREAN -> "추적 방지"
    }

    val catSocial: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "社交增强"
        AppLanguage.ENGLISH -> "Social"
        AppLanguage.ARABIC -> "تحسين التواصل الاجتماعي"
        AppLanguage.PORTUGUESE -> "Social"
        AppLanguage.SPANISH -> "Social"
        AppLanguage.FRENCH -> "Social"
        AppLanguage.GERMAN -> "Sozial"
        AppLanguage.RUSSIAN -> "Соцсети"
        AppLanguage.JAPANESE -> "ソーシャル"
        AppLanguage.KOREAN -> "소셜"
    }

    val catShopping: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "购物助手"
        AppLanguage.ENGLISH -> "Shopping"
        AppLanguage.ARABIC -> "مساعد التسوق"
        AppLanguage.PORTUGUESE -> "Compras"
        AppLanguage.SPANISH -> "Compras"
        AppLanguage.FRENCH -> "Achats"
        AppLanguage.GERMAN -> "Einkaufen"
        AppLanguage.RUSSIAN -> "Покупки"
        AppLanguage.JAPANESE -> "ショッピング"
        AppLanguage.KOREAN -> "쇼핑"
    }

    val catReading: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "阅读模式"
        AppLanguage.ENGLISH -> "Reading"
        AppLanguage.ARABIC -> "وضع القراءة"
        AppLanguage.PORTUGUESE -> "Leitura"
        AppLanguage.SPANISH -> "Lectura"
        AppLanguage.FRENCH -> "Lecture"
        AppLanguage.GERMAN -> "Lesen"
        AppLanguage.RUSSIAN -> "Чтение"
        AppLanguage.JAPANESE -> "読書"
        AppLanguage.KOREAN -> "읽기"
    }

    val catTranslate: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "翻译工具"
        AppLanguage.ENGLISH -> "Translate"
        AppLanguage.ARABIC -> "أدوات الترجمة"
        AppLanguage.PORTUGUESE -> "Tradução"
        AppLanguage.SPANISH -> "Traducción"
        AppLanguage.FRENCH -> "Traduction"
        AppLanguage.GERMAN -> "Übersetzung"
        AppLanguage.RUSSIAN -> "Перевод"
        AppLanguage.JAPANESE -> "翻訳"
        AppLanguage.KOREAN -> "번역"
    }

    val catDeveloper: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "开发调试"
        AppLanguage.ENGLISH -> "Developer"
        AppLanguage.ARABIC -> "أدوات المطور"
        AppLanguage.PORTUGUESE -> "Desenvolvedor"
        AppLanguage.SPANISH -> "Desarrollador"
        AppLanguage.FRENCH -> "Développeur"
        AppLanguage.GERMAN -> "Entwickler"
        AppLanguage.RUSSIAN -> "Разработчик"
        AppLanguage.JAPANESE -> "開発者"
        AppLanguage.KOREAN -> "개발자"
    }

    val catOther: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "其他"
        AppLanguage.ENGLISH -> "Other"
        AppLanguage.ARABIC -> "أخرى"
        AppLanguage.PORTUGUESE -> "Outros"
        AppLanguage.SPANISH -> "Otros"
        AppLanguage.FRENCH -> "Autres"
        AppLanguage.GERMAN -> "Andere"
        AppLanguage.RUSSIAN -> "Другое"
        AppLanguage.JAPANESE -> "その他"
        AppLanguage.KOREAN -> "기타"
    }

    val runTimeDocStart: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "页面开始"
        AppLanguage.ENGLISH -> "Document Start"
        AppLanguage.ARABIC -> "بداية المستند"
        AppLanguage.PORTUGUESE -> "Início do Documento"
        AppLanguage.SPANISH -> "Inicio del Documento"
        AppLanguage.FRENCH -> "Début du Document"
        AppLanguage.GERMAN -> "Dokument-Anfang"
        AppLanguage.RUSSIAN -> "Начало документа"
        AppLanguage.JAPANESE -> "ドキュメント開始"
        AppLanguage.KOREAN -> "문서 시작"
    }
    val runTimeDocStartDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "DOM 未就绪时执行，适合拦截请求和早期修改"
        AppLanguage.ENGLISH -> "Execute before DOM ready, suitable for request interception and early modifications"
        AppLanguage.ARABIC -> "التنفيذ قبل جاهزية DOM، مناسب لاعتراض الطلبات والتعديلات المبكرة"
        AppLanguage.PORTUGUESE -> "Executar antes do DOM pronto, adequado para interceptação de requisições e modificações precoces"
        AppLanguage.SPANISH -> "Ejecutar antes del DOM listo, adecuado para intercepción de solicitudes y modificaciones tempranas"
        AppLanguage.FRENCH -> "Exécuter avant DOM prêt, adapté à l'interception de requêtes et modifications précoces"
        AppLanguage.GERMAN -> "Vor DOM-bereit ausführen, für Anfrage-Abfangung und frühe Änderungen"
        AppLanguage.RUSSIAN -> "Выполнять до готовности DOM, подходит для перехвата запросов и ранних изменений"
        AppLanguage.JAPANESE -> "DOM準備前実行、リクエスト傍受と早期変更に適合"
        AppLanguage.KOREAN -> "DOM 준비 전 실행, 요청 가로채기 및 초기 수정에 적합"
    }
    val runTimeDocEnd: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "DOM 就绪"
        AppLanguage.ENGLISH -> "DOM Ready"
        AppLanguage.ARABIC -> "جاهزية DOM"
        AppLanguage.PORTUGUESE -> "DOM Pronto"
        AppLanguage.SPANISH -> "DOM Listo"
        AppLanguage.FRENCH -> "DOM Prêt"
        AppLanguage.GERMAN -> "DOM Bereit"
        AppLanguage.RUSSIAN -> "DOM Готов"
        AppLanguage.JAPANESE -> "DOM準備完了"
        AppLanguage.KOREAN -> "DOM 준비됨"
    }
    val runTimeDocEndDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "DOM 加载完成后执行（推荐），适合大多数场景"
        AppLanguage.ENGLISH -> "Execute after DOM loaded (recommended), suitable for most scenarios"
        AppLanguage.ARABIC -> "التنفيذ بعد تحميل DOM (موصى به)، مناسب لمعظم السيناريوهات"
        AppLanguage.PORTUGUESE -> "Executar após DOM carregado (recomendado), adequado para a maioria dos cenários"
        AppLanguage.SPANISH -> "Ejecutar tras DOM cargado (recomendado), adecuado para la mayoría de escenarios"
        AppLanguage.FRENCH -> "Exécuter après DOM chargé (recommandé), adapté à la plupart des scénarios"
        AppLanguage.GERMAN -> "Nach DOM geladen ausführen (empfohlen), für die meisten Szenarien"
        AppLanguage.RUSSIAN -> "Выполнять после загрузки DOM (рекомендуется), подходит для большинства сценариев"
        AppLanguage.JAPANESE -> "DOM読み込み後に実行（推奨）、ほとんどのシナリオに適合"
        AppLanguage.KOREAN -> "DOM 로드 후 실행(권장), 대부분의 시나리오에 적합"
    }
    val runTimeDocIdle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "页面空闲"
        AppLanguage.ENGLISH -> "Page Idle"
        AppLanguage.ARABIC -> "صفحة خاملة"
        AppLanguage.PORTUGUESE -> "Página Ociosa"
        AppLanguage.SPANISH -> "Página Inactiva"
        AppLanguage.FRENCH -> "Page Inactive"
        AppLanguage.GERMAN -> "Seite Leerlauf"
        AppLanguage.RUSSIAN -> "Страница простаивает"
        AppLanguage.JAPANESE -> "ページアイドル"
        AppLanguage.KOREAN -> "페이지 유휴"
    }
    val runTimeDocIdleDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "页面完全加载后执行，适合后处理和性能优化"
        AppLanguage.ENGLISH -> "Execute after page fully loaded, suitable for post-processing and performance optimization"
        AppLanguage.ARABIC -> "التنفيذ بعد تحميل الصفحة بالكامل، مناسب للمعالجة اللاحقة وتحسين الأداء"
        AppLanguage.PORTUGUESE -> "Executar após página totalmente carregada, adequado para pós-processamento e otimização de desempenho"
        AppLanguage.SPANISH -> "Ejecutar tras página completamente cargada, adecuado para posprocesamiento y optimización de rendimiento"
        AppLanguage.FRENCH -> "Exécuter après chargement complet, adapté au post-traitement et optimisation"
        AppLanguage.GERMAN -> "Nach vollständigem Laden ausführen, für Nachverarbeitung und Leistungsoptimierung"
        AppLanguage.RUSSIAN -> "Выполнять после полной загрузки страницы, подходит для постобработки и оптимизации"
        AppLanguage.JAPANESE -> "ページ完全読み込み後に実行、後処理とパフォーマンス最適化に適合"
        AppLanguage.KOREAN -> "페이지 완전 로드 후 실행, 후처리 및 성능 최적화에 적합"
    }

    val colorRed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "红色"
        AppLanguage.ENGLISH -> "Red"
        AppLanguage.ARABIC -> "أحمر"
        AppLanguage.PORTUGUESE -> "Vermelho"
        AppLanguage.SPANISH -> "Rojo"
        AppLanguage.FRENCH -> "Rouge"
        AppLanguage.GERMAN -> "Rot"
        AppLanguage.RUSSIAN -> "Красный"
        AppLanguage.JAPANESE -> "赤"
        AppLanguage.KOREAN -> "빨강"
    }

    val colorPink: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "粉色"
        AppLanguage.ENGLISH -> "Pink"
        AppLanguage.ARABIC -> "وردي"
        AppLanguage.PORTUGUESE -> "Rosa"
        AppLanguage.SPANISH -> "Rosa"
        AppLanguage.FRENCH -> "Rose"
        AppLanguage.GERMAN -> "Rosa"
        AppLanguage.RUSSIAN -> "Розовый"
        AppLanguage.JAPANESE -> "ピンク"
        AppLanguage.KOREAN -> "분홍"
    }

    val colorPurple: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "紫色"
        AppLanguage.ENGLISH -> "Purple"
        AppLanguage.ARABIC -> "بنفسجي"
        AppLanguage.PORTUGUESE -> "Roxo"
        AppLanguage.SPANISH -> "Púrpura"
        AppLanguage.FRENCH -> "Violet"
        AppLanguage.GERMAN -> "Lila"
        AppLanguage.RUSSIAN -> "Фиолетовый"
        AppLanguage.JAPANESE -> "紫"
        AppLanguage.KOREAN -> "보라"
    }

    val colorDeepPurple: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "深紫"
        AppLanguage.ENGLISH -> "Deep Purple"
        AppLanguage.ARABIC -> "بنفسجي داكن"
        AppLanguage.PORTUGUESE -> "Roxo Escuro"
        AppLanguage.SPANISH -> "Púrpura Oscuro"
        AppLanguage.FRENCH -> "Violet Foncé"
        AppLanguage.GERMAN -> "Dunkellila"
        AppLanguage.RUSSIAN -> "Тёмно-фиолетовый"
        AppLanguage.JAPANESE -> "ディープパープル"
        AppLanguage.KOREAN -> "진한 보라"
    }

    val colorIndigo: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "靛蓝"
        AppLanguage.ENGLISH -> "Indigo"
        AppLanguage.ARABIC -> "نيلي"
        AppLanguage.PORTUGUESE -> "Índigo"
        AppLanguage.SPANISH -> "Índigo"
        AppLanguage.FRENCH -> "Indigo"
        AppLanguage.GERMAN -> "Indigo"
        AppLanguage.RUSSIAN -> "Индиго"
        AppLanguage.JAPANESE -> "インディゴ"
        AppLanguage.KOREAN -> "남색"
    }

    val colorBlue: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "蓝色"
        AppLanguage.ENGLISH -> "Blue"
        AppLanguage.ARABIC -> "أزرق"
        AppLanguage.PORTUGUESE -> "Azul"
        AppLanguage.SPANISH -> "Azul"
        AppLanguage.FRENCH -> "Bleu"
        AppLanguage.GERMAN -> "Blau"
        AppLanguage.RUSSIAN -> "Синий"
        AppLanguage.JAPANESE -> "青"
        AppLanguage.KOREAN -> "파랑"
    }

    val colorLightBlue: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "浅蓝"
        AppLanguage.ENGLISH -> "Light Blue"
        AppLanguage.ARABIC -> "أزرق فاتح"
        AppLanguage.PORTUGUESE -> "Azul Claro"
        AppLanguage.SPANISH -> "Azul Claro"
        AppLanguage.FRENCH -> "Bleu Clair"
        AppLanguage.GERMAN -> "Hellblau"
        AppLanguage.RUSSIAN -> "Светло-синий"
        AppLanguage.JAPANESE -> "ライトブルー"
        AppLanguage.KOREAN -> "연한 파랑"
    }

    val colorCyan: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "青色"
        AppLanguage.ENGLISH -> "Cyan"
        AppLanguage.ARABIC -> "سماوي"
        AppLanguage.PORTUGUESE -> "Ciano"
        AppLanguage.SPANISH -> "Cian"
        AppLanguage.FRENCH -> "Cyan"
        AppLanguage.GERMAN -> "Cyan"
        AppLanguage.RUSSIAN -> "Голубой"
        AppLanguage.JAPANESE -> "シアン"
        AppLanguage.KOREAN -> "시안"
    }

    val colorTeal: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "蓝绿"
        AppLanguage.ENGLISH -> "Teal"
        AppLanguage.ARABIC -> "أزرق مخضر"
        AppLanguage.PORTUGUESE -> "Verde-Azulado"
        AppLanguage.SPANISH -> "Verde Azulado"
        AppLanguage.FRENCH -> "Sarcelle"
        AppLanguage.GERMAN -> "Blaugrün"
        AppLanguage.RUSSIAN -> "Бирюзовый"
        AppLanguage.JAPANESE -> "ティール"
        AppLanguage.KOREAN -> "청록"
    }

    val colorGreen: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "绿色"
        AppLanguage.ENGLISH -> "Green"
        AppLanguage.ARABIC -> "أخضر"
        AppLanguage.PORTUGUESE -> "Verde"
        AppLanguage.SPANISH -> "Verde"
        AppLanguage.FRENCH -> "Vert"
        AppLanguage.GERMAN -> "Grün"
        AppLanguage.RUSSIAN -> "Зелёный"
        AppLanguage.JAPANESE -> "緑"
        AppLanguage.KOREAN -> "초록"
    }

    val colorLightGreen: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "浅绿"
        AppLanguage.ENGLISH -> "Light Green"
        AppLanguage.ARABIC -> "أخضر فاتح"
        AppLanguage.PORTUGUESE -> "Verde Claro"
        AppLanguage.SPANISH -> "Verde Claro"
        AppLanguage.FRENCH -> "Vert Clair"
        AppLanguage.GERMAN -> "Hellgrün"
        AppLanguage.RUSSIAN -> "Светло-зелёный"
        AppLanguage.JAPANESE -> "ライトグリーン"
        AppLanguage.KOREAN -> "연한 초록"
    }

    val colorLime: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "黄绿"
        AppLanguage.ENGLISH -> "Lime"
        AppLanguage.ARABIC -> "ليموني"
        AppLanguage.PORTUGUESE -> "Limão"
        AppLanguage.SPANISH -> "Lima"
        AppLanguage.FRENCH -> "Citron Vert"
        AppLanguage.GERMAN -> "Limette"
        AppLanguage.RUSSIAN -> "Лайм"
        AppLanguage.JAPANESE -> "ライム"
        AppLanguage.KOREAN -> "라임"
    }

    val colorYellow: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "黄色"
        AppLanguage.ENGLISH -> "Yellow"
        AppLanguage.ARABIC -> "أصفر"
        AppLanguage.PORTUGUESE -> "Amarelo"
        AppLanguage.SPANISH -> "Amarillo"
        AppLanguage.FRENCH -> "Jaune"
        AppLanguage.GERMAN -> "Gelb"
        AppLanguage.RUSSIAN -> "Жёлтый"
        AppLanguage.JAPANESE -> "黄"
        AppLanguage.KOREAN -> "노랑"
    }

    val colorAmber: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "琥珀"
        AppLanguage.ENGLISH -> "Amber"
        AppLanguage.ARABIC -> "كهرماني"
        AppLanguage.PORTUGUESE -> "Âmbar"
        AppLanguage.SPANISH -> "Ámbar"
        AppLanguage.FRENCH -> "Ambre"
        AppLanguage.GERMAN -> "Bernstein"
        AppLanguage.RUSSIAN -> "Янтарный"
        AppLanguage.JAPANESE -> "アンバー"
        AppLanguage.KOREAN -> "앰버"
    }

    val colorOrange: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "橙色"
        AppLanguage.ENGLISH -> "Orange"
        AppLanguage.ARABIC -> "برتقالي"
        AppLanguage.PORTUGUESE -> "Laranja"
        AppLanguage.SPANISH -> "Naranja"
        AppLanguage.FRENCH -> "Orange"
        AppLanguage.GERMAN -> "Orange"
        AppLanguage.RUSSIAN -> "Оранжевый"
        AppLanguage.JAPANESE -> "オレンジ"
        AppLanguage.KOREAN -> "주황"
    }

    val colorDeepOrange: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "深橙"
        AppLanguage.ENGLISH -> "Deep Orange"
        AppLanguage.ARABIC -> "برتقالي داكن"
        AppLanguage.PORTUGUESE -> "Laranja Escuro"
        AppLanguage.SPANISH -> "Naranja Oscuro"
        AppLanguage.FRENCH -> "Orange Foncé"
        AppLanguage.GERMAN -> "Dunkelorange"
        AppLanguage.RUSSIAN -> "Тёмно-оранжевый"
        AppLanguage.JAPANESE -> "ディープオレンジ"
        AppLanguage.KOREAN -> "진한 주황"
    }

    val colorBrown: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "棕色"
        AppLanguage.ENGLISH -> "Brown"
        AppLanguage.ARABIC -> "بني"
        AppLanguage.PORTUGUESE -> "Marrom"
        AppLanguage.SPANISH -> "Marrón"
        AppLanguage.FRENCH -> "Marron"
        AppLanguage.GERMAN -> "Braun"
        AppLanguage.RUSSIAN -> "Коричневый"
        AppLanguage.JAPANESE -> "ブラウン"
        AppLanguage.KOREAN -> "갈색"
    }

    val colorGrey: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "灰色"
        AppLanguage.ENGLISH -> "Grey"
        AppLanguage.ARABIC -> "رمادي"
        AppLanguage.PORTUGUESE -> "Cinza"
        AppLanguage.SPANISH -> "Gris"
        AppLanguage.FRENCH -> "Gris"
        AppLanguage.GERMAN -> "Grau"
        AppLanguage.RUSSIAN -> "Серый"
        AppLanguage.JAPANESE -> "グレー"
        AppLanguage.KOREAN -> "회색"
    }

    val colorBlueGrey: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "蓝灰"
        AppLanguage.ENGLISH -> "Blue Grey"
        AppLanguage.ARABIC -> "رمادي مزرق"
        AppLanguage.PORTUGUESE -> "Cinza-Azulado"
        AppLanguage.SPANISH -> "Gris Azulado"
        AppLanguage.FRENCH -> "Gris Bleu"
        AppLanguage.GERMAN -> "Blaugrau"
        AppLanguage.RUSSIAN -> "Сине-серый"
        AppLanguage.JAPANESE -> "ブルーグレー"
        AppLanguage.KOREAN -> "청회색"
    }

    val colorBlack: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "黑色"
        AppLanguage.ENGLISH -> "Black"
        AppLanguage.ARABIC -> "أسود"
        AppLanguage.PORTUGUESE -> "Preto"
        AppLanguage.SPANISH -> "Negro"
        AppLanguage.FRENCH -> "Noir"
        AppLanguage.GERMAN -> "Schwarz"
        AppLanguage.RUSSIAN -> "Чёрный"
        AppLanguage.JAPANESE -> "黒"
        AppLanguage.KOREAN -> "검정"
    }

    val colorWhite: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "白色"
        AppLanguage.ENGLISH -> "White"
        AppLanguage.ARABIC -> "أبيض"
        AppLanguage.PORTUGUESE -> "Branco"
        AppLanguage.SPANISH -> "Blanco"
        AppLanguage.FRENCH -> "Blanc"
        AppLanguage.GERMAN -> "Weiß"
        AppLanguage.RUSSIAN -> "Белый"
        AppLanguage.JAPANESE -> "白"
        AppLanguage.KOREAN -> "흰색"
    }

    val colorDarkTheme: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "深色主题"
        AppLanguage.ENGLISH -> "Dark Theme"
        AppLanguage.ARABIC -> "سمة داكنة"
        AppLanguage.PORTUGUESE -> "Tema Escuro"
        AppLanguage.SPANISH -> "Tema Oscuro"
        AppLanguage.FRENCH -> "Thème Sombre"
        AppLanguage.GERMAN -> "Dunkles Theme"
        AppLanguage.RUSSIAN -> "Тёмная тема"
        AppLanguage.JAPANESE -> "ダークテーマ"
        AppLanguage.KOREAN -> "다크 테마"
    }

    val colorLightTheme: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "浅色主题"
        AppLanguage.ENGLISH -> "Light Theme"
        AppLanguage.ARABIC -> "سمة فاتحة"
        AppLanguage.PORTUGUESE -> "Tema Claro"
        AppLanguage.SPANISH -> "Tema Claro"
        AppLanguage.FRENCH -> "Thème Clair"
        AppLanguage.GERMAN -> "Helles Theme"
        AppLanguage.RUSSIAN -> "Светлая тема"
        AppLanguage.JAPANESE -> "ライトテーマ"
        AppLanguage.KOREAN -> "라이트 테마"
    }

    val colorTransparent: String get() = when (Strings.lang) {
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

    val colorSelected: String get() = when (Strings.lang) {
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

    val selectedCount2: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已选 %d 个"
        AppLanguage.ENGLISH -> "%d selected"
        AppLanguage.ARABIC -> "تم اختيار %d"
        AppLanguage.PORTUGUESE -> "%d selecionado(s)"
        AppLanguage.SPANISH -> "%d seleccionado(s)"
        AppLanguage.FRENCH -> "%d sélectionné(s)"
        AppLanguage.GERMAN -> "%d ausgewählt"
        AppLanguage.RUSSIAN -> "%d выбрано"
        AppLanguage.JAPANESE -> "%d 件選択中"
        AppLanguage.KOREAN -> "%d개 선택됨"
    }

    val noMatchingModules: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "没有找到匹配的模块"
        AppLanguage.ENGLISH -> "No matching modules found"
        AppLanguage.ARABIC -> "لم يتم العثور على وحدات مطابقة"
        AppLanguage.PORTUGUESE -> "Nenhum módulo correspondente encontrado"
        AppLanguage.SPANISH -> "No se encontraron módulos coincidentes"
        AppLanguage.FRENCH -> "Aucun module correspondant trouvé"
        AppLanguage.GERMAN -> "Keine passenden Module gefunden"
        AppLanguage.RUSSIAN -> "Подходящие модули не найдены"
        AppLanguage.JAPANESE -> "一致するモジュールが見つかりません"
        AppLanguage.KOREAN -> "일치하는 모듈을 찾을 수 없습니다"
    }

    val mediaContent: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "媒体内容"
        AppLanguage.ENGLISH -> "Media Content"
        AppLanguage.ARABIC -> "محتوى الوسائط"
        AppLanguage.PORTUGUESE -> "Conteúdo de Mídia"
        AppLanguage.SPANISH -> "Contenido Multimedia"
        AppLanguage.FRENCH -> "Contenu Média"
        AppLanguage.GERMAN -> "Medieninhalt"
        AppLanguage.RUSSIAN -> "Медиаконтент"
        AppLanguage.JAPANESE -> "メディアコンテンツ"
        AppLanguage.KOREAN -> "미디어 콘텐츠"
    }

    val statusBarBackground: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "状态栏背景"
        AppLanguage.ENGLISH -> "Status Bar Background"
        AppLanguage.ARABIC -> "خلفية شريط الحالة"
        AppLanguage.PORTUGUESE -> "Fundo da Barra de Status"
        AppLanguage.SPANISH -> "Fondo de Barra de Estado"
        AppLanguage.FRENCH -> "Fond de Barre d'État"
        AppLanguage.GERMAN -> "Statusleisten-Hintergrund"
        AppLanguage.RUSSIAN -> "Фон строки состояния"
        AppLanguage.JAPANESE -> "ステータスバー背景"
        AppLanguage.KOREAN -> "상태 표시줄 배경"
    }

    val appNeedsActivation: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "应用需要激活"
        AppLanguage.ENGLISH -> "App needs activation"
        AppLanguage.ARABIC -> "التطبيق يحتاج إلى تفعيل"
        AppLanguage.PORTUGUESE -> "App precisa de ativação"
        AppLanguage.SPANISH -> "La app necesita activación"
        AppLanguage.FRENCH -> "L'app nécessite une activation"
        AppLanguage.GERMAN -> "App benötigt Aktivierung"
        AppLanguage.RUSSIAN -> "Приложение требует активации"
        AppLanguage.JAPANESE -> "アプリのアクティベーションが必要です"
        AppLanguage.KOREAN -> "앱 활성화가 필요합니다"
    }

    val skip: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "跳过"
        AppLanguage.ENGLISH -> "Skip"
        AppLanguage.ARABIC -> "تخطي"
        AppLanguage.PORTUGUESE -> "Pular"
        AppLanguage.SPANISH -> "Saltar"
        AppLanguage.FRENCH -> "Passer"
        AppLanguage.GERMAN -> "Überspringen"
        AppLanguage.RUSSIAN -> "Пропустить"
        AppLanguage.JAPANESE -> "スキップ"
        AppLanguage.KOREAN -> "건너뛰기"
    }

    val insert: String get() = when (Strings.lang) {
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

    val totalLinesCount: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "共 %d 行歌词"
        AppLanguage.ENGLISH -> "%d lines of lyrics"
        AppLanguage.ARABIC -> "%d سطر من الكلمات"
        AppLanguage.PORTUGUESE -> "%d linhas de letras"
        AppLanguage.SPANISH -> "%d líneas de letras"
        AppLanguage.FRENCH -> "%d lignes de paroles"
        AppLanguage.GERMAN -> "%d Textzeilen"
        AppLanguage.RUSSIAN -> "%d строк текста"
        AppLanguage.JAPANESE -> "%d 行の歌詞"
        AppLanguage.KOREAN -> "%d줄 가사"
    }

    val applied: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已应用"
        AppLanguage.ENGLISH -> "Applied"
        AppLanguage.ARABIC -> "مطبق"
        AppLanguage.PORTUGUESE -> "Aplicado"
        AppLanguage.SPANISH -> "Aplicado"
        AppLanguage.FRENCH -> "Appliqué"
        AppLanguage.GERMAN -> "Angewendet"
        AppLanguage.RUSSIAN -> "Применено"
        AppLanguage.JAPANESE -> "適用済み"
        AppLanguage.KOREAN -> "적용됨"
    }

    val optionalLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "可选"
        AppLanguage.ENGLISH -> "Optional"
        AppLanguage.ARABIC -> "اختياري"
        AppLanguage.PORTUGUESE -> "Opcional"
        AppLanguage.SPANISH -> "Opcional"
        AppLanguage.FRENCH -> "Facultatif"
        AppLanguage.GERMAN -> "Wahlfrei"
        AppLanguage.RUSSIAN -> "Необязательно"
        AppLanguage.JAPANESE -> "任意"
        AppLanguage.KOREAN -> "선택 사항"
    }

    val previewLrcHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "预览生成的 LRC 效果，确认无误后保存"
        AppLanguage.ENGLISH -> "Preview generated LRC effect, save after confirmation"
        AppLanguage.ARABIC -> "معاينة تأثير LRC المُنشأ، احفظ بعد التأكيد"
        AppLanguage.PORTUGUESE -> "Pré-visualize o efeito LRC gerado, salve após confirmar"
        AppLanguage.SPANISH -> "Previsualiza el efecto LRC generado, guarda tras confirmar"
        AppLanguage.FRENCH -> "Prévisualisez l'effet LRC généré, enregistrez après confirmation"
        AppLanguage.GERMAN -> "LRC-Effekt vorschauen, nach Bestätigung speichern"
        AppLanguage.RUSSIAN -> "Предпросмотр LRC, сохраните после подтверждения"
        AppLanguage.JAPANESE -> "生成された LRC 効果をプレビューし、確認後に保存"
        AppLanguage.KOREAN -> "생성된 LRC 효과를 미리보고 확인 후 저장"
    }

    val featureIconGeneration: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "图标生成"
        AppLanguage.ENGLISH -> "Icon Generation"
        AppLanguage.ARABIC -> "إنشاء الأيقونات"
        AppLanguage.PORTUGUESE -> "Geração de Ícones"
        AppLanguage.SPANISH -> "Generación de Iconos"
        AppLanguage.FRENCH -> "Génération d'Icônes"
        AppLanguage.GERMAN -> "Icon-Generierung"
        AppLanguage.RUSSIAN -> "Генерация иконок"
        AppLanguage.JAPANESE -> "アイコン生成"
        AppLanguage.KOREAN -> "아이콘 생성"
    }

    val featureModuleDevelopment: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "模块开发"
        AppLanguage.ENGLISH -> "Module Development"
        AppLanguage.ARABIC -> "تطوير الوحدات"
        AppLanguage.PORTUGUESE -> "Desenvolvimento de Módulos"
        AppLanguage.SPANISH -> "Desarrollo de Módulos"
        AppLanguage.FRENCH -> "Développement de Modules"
        AppLanguage.GERMAN -> "Modul-Entwicklung"
        AppLanguage.RUSSIAN -> "Разработка модулей"
        AppLanguage.JAPANESE -> "モジュール開発"
        AppLanguage.KOREAN -> "모듈 개발"
    }

    val featureLrcGeneration: String get() = when (Strings.lang) {
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

    val featureTranslation: String get() = when (Strings.lang) {
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

    val featureGeneralChat: String get() = when (Strings.lang) {
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

    val coding: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "编程"
        AppLanguage.ENGLISH -> "Coding"
        AppLanguage.ARABIC -> "البرمجة"
        AppLanguage.PORTUGUESE -> "Programação"
        AppLanguage.SPANISH -> "Programación"
        AppLanguage.FRENCH -> "Programmation"
        AppLanguage.GERMAN -> "Programmierung"
        AppLanguage.RUSSIAN -> "Программирование"
        AppLanguage.JAPANESE -> "プログラミング"
        AppLanguage.KOREAN -> "프로그래밍"
    }

    val agentDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "用 AI 直接构建网页、模块、应用 — 在手机上"
        AppLanguage.ENGLISH -> "Build pages, modules, and apps with AI — directly on your phone"
        AppLanguage.ARABIC -> "بناء الصفحات والوحدات والتطبيقات باستخدام الذكاء الاصطناعي — مباشرة على هاتفك"
        AppLanguage.PORTUGUESE -> "Construa páginas, módulos e apps com IA — direto no seu telefone"
        AppLanguage.SPANISH -> "Crea páginas, módulos y apps con IA — directamente en tu teléfono"
        AppLanguage.FRENCH -> "Créez pages, modules et apps avec l'IA — directement sur votre téléphone"
        AppLanguage.GERMAN -> "Erstelle Seiten, Module und Apps mit KI — direkt auf deinem Handy"
        AppLanguage.RUSSIAN -> "Создавайте страницы, модули и приложения с ИИ — прямо на телефоне"
        AppLanguage.JAPANESE -> "AIでページ、モジュール、アプリを構築 — スマホで直接"
        AppLanguage.KOREAN -> "AI로 페이지, 모듈, 앱을 제작 — 휴대폰에서 직접"
    }

    val agentImageDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "在 Agent 会话中生成、查看、迭代图像"
        AppLanguage.ENGLISH -> "Generate, view, and iterate on images during Agent sessions"
        AppLanguage.ARABIC -> "إنشاء الصور وعرضها وتحسينها في جلسات Agent"
        AppLanguage.PORTUGUESE -> "Gere, visualize e itere imagens durante sessões do Agent"
        AppLanguage.SPANISH -> "Genera, visualiza e itera imágenes durante sesiones de Agent"
        AppLanguage.FRENCH -> "Générez, visualisez et itérez des images pendant les sessions Agent"
        AppLanguage.GERMAN -> "Bilder in Agent-Sitzungen erstellen, ansehen und iterieren"
        AppLanguage.RUSSIAN -> "Создавайте, просматривайте и итерируйте изображения во время сессий Agent"
        AppLanguage.JAPANESE -> "Agentセッション中に画像を生成、表示、反復"
        AppLanguage.KOREAN -> "Agent 세션 중 이미지 생성, 보기, 반복"
    }

    val iconGenerationDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "使用 AI 生成应用图标"
        AppLanguage.ENGLISH -> "Generate app icons using AI"
        AppLanguage.ARABIC -> "إنشاء أيقونات التطبيق باستخدام الذكاء الاصطناعي"
        AppLanguage.PORTUGUESE -> "Gerar ícones de app usando IA"
        AppLanguage.SPANISH -> "Generar iconos de app con IA"
        AppLanguage.FRENCH -> "Générer des icônes d'app avec l'IA"
        AppLanguage.GERMAN -> "App-Icons mit KI generieren"
        AppLanguage.RUSSIAN -> "Генерация иконок приложения с помощью ИИ"
        AppLanguage.JAPANESE -> "AIでアプリアイコンを生成"
        AppLanguage.KOREAN -> "AI로 앱 아이콘 생성"
    }

    val moduleDevelopmentDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "AI Agent 辅助开发扩展模块"
        AppLanguage.ENGLISH -> "AI Agent assisted extension module development"
        AppLanguage.ARABIC -> "تطوير وحدات الإضافة بمساعدة وكيل الذكاء الاصطناعي"
        AppLanguage.PORTUGUESE -> "Desenvolvimento de módulos de extensão assistido por AI Agent"
        AppLanguage.SPANISH -> "Desarrollo de módulos de extensión asistido por AI Agent"
        AppLanguage.FRENCH -> "Développement de modules d'extension assisté par AI Agent"
        AppLanguage.GERMAN -> "AI Agent-gestützte Erweiterungsmodul-Entwicklung"
        AppLanguage.RUSSIAN -> "Разработка модулей расширения с помощью AI Agent"
        AppLanguage.JAPANESE -> "AI Agent支援による拡張モジュール開発"
        AppLanguage.KOREAN -> "AI Agent 지원 확장 모듈 개발"
    }

    val agentTitle: String get() = when (Strings.lang) {
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
    val agentNewSession: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "新建会话"
        AppLanguage.ENGLISH -> "New session"
        AppLanguage.ARABIC -> "جلسة جديدة"
        AppLanguage.PORTUGUESE -> "Nova sessão"
        AppLanguage.SPANISH -> "Nueva sesión"
        AppLanguage.FRENCH -> "Nouvelle session"
        AppLanguage.GERMAN -> "Neue Sitzung"
        AppLanguage.RUSSIAN -> "Новая сессия"
        AppLanguage.JAPANESE -> "新規セッション"
        AppLanguage.KOREAN -> "새 세션"
    }
    val agentTabSessions: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "会话"
        AppLanguage.ENGLISH -> "Sessions"
        AppLanguage.ARABIC -> "الجلسات"
        AppLanguage.PORTUGUESE -> "Sessões"
        AppLanguage.SPANISH -> "Sesiones"
        AppLanguage.FRENCH -> "Sessions"
        AppLanguage.GERMAN -> "Sitzungen"
        AppLanguage.RUSSIAN -> "Сессии"
        AppLanguage.JAPANESE -> "セッション"
        AppLanguage.KOREAN -> "세션"
    }
    val agentTabFiles: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "文件"
        AppLanguage.ENGLISH -> "Files"
        AppLanguage.ARABIC -> "الملفات"
        AppLanguage.PORTUGUESE -> "Arquivos"
        AppLanguage.SPANISH -> "Archivos"
        AppLanguage.FRENCH -> "Fichiers"
        AppLanguage.GERMAN -> "Dateien"
        AppLanguage.RUSSIAN -> "Файлы"
        AppLanguage.JAPANESE -> "ファイル"
        AppLanguage.KOREAN -> "파일"
    }
    val agentDrawerSearchHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "搜索…"
        AppLanguage.ENGLISH -> "Search…"
        AppLanguage.ARABIC -> "بحث…"
        AppLanguage.PORTUGUESE -> "Pesquisar…"
        AppLanguage.SPANISH -> "Buscar…"
        AppLanguage.FRENCH -> "Rechercher…"
        AppLanguage.GERMAN -> "Suchen…"
        AppLanguage.RUSSIAN -> "Поиск…"
        AppLanguage.JAPANESE -> "検索…"
        AppLanguage.KOREAN -> "검색…"
    }
    val agentComposerHintIdle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "描述你想做的，输入 / 触发命令"
        AppLanguage.ENGLISH -> "Describe what to build. Type / for commands."
        AppLanguage.ARABIC -> "صف ما تريد بناءه، أدخل / للأوامر"
        AppLanguage.PORTUGUESE -> "Descreva o que construir. Digite / para comandos."
        AppLanguage.SPANISH -> "Describe qué construir. Escribe / para comandos."
        AppLanguage.FRENCH -> "Décrivez ce que vous voulez créer. Tapez / pour les commandes."
        AppLanguage.GERMAN -> "Beschreibe, was du bauen willst. Tippe / für Befehle."
        AppLanguage.RUSSIAN -> "Опишите, что создать. Введите / для команд."
        AppLanguage.JAPANESE -> "作成したい内容を説明。/ でコマンド呼び出し。"
        AppLanguage.KOREAN -> "제작할 내용을 설명하세요. / 입력 시 명령어 호출."
    }
    val agentComposerHintWorking: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "AI 工作中…"
        AppLanguage.ENGLISH -> "AI is working…"
        AppLanguage.ARABIC -> "الذكاء الاصطناعي يعمل…"
        AppLanguage.PORTUGUESE -> "IA trabalhando…"
        AppLanguage.SPANISH -> "IA trabajando…"
        AppLanguage.FRENCH -> "IA en cours…"
        AppLanguage.GERMAN -> "KI arbeitet…"
        AppLanguage.RUSSIAN -> "ИИ работает…"
        AppLanguage.JAPANESE -> "AI処理中…"
        AppLanguage.KOREAN -> "AI 작동 중…"
    }
    val agentAttachTooltip: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "添加附件"
        AppLanguage.ENGLISH -> "Attach"
        AppLanguage.ARABIC -> "إرفاق"
        AppLanguage.PORTUGUESE -> "Anexar"
        AppLanguage.SPANISH -> "Adjuntar"
        AppLanguage.FRENCH -> "Joindre"
        AppLanguage.GERMAN -> "Anhängen"
        AppLanguage.RUSSIAN -> "Прикрепить"
        AppLanguage.JAPANESE -> "添付"
        AppLanguage.KOREAN -> "첨부"
    }
    val agentAttachImage: String get() = when (Strings.lang) {
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
    val agentAttachFile: String get() = when (Strings.lang) {
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
    val agentAttachFolder: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "文件夹"
        AppLanguage.ENGLISH -> "Folder"
        AppLanguage.ARABIC -> "مجلد"
        AppLanguage.PORTUGUESE -> "Pasta"
        AppLanguage.SPANISH -> "Carpeta"
        AppLanguage.FRENCH -> "Dossier"
        AppLanguage.GERMAN -> "Ordner"
        AppLanguage.RUSSIAN -> "Папка"
        AppLanguage.JAPANESE -> "フォルダー"
        AppLanguage.KOREAN -> "폴더"
    }
    val agentAttachImporting: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "正在导入附件…"
        AppLanguage.ENGLISH -> "Importing attachment…"
        AppLanguage.ARABIC -> "جارٍ استيراد المرفق…"
        AppLanguage.PORTUGUESE -> "Importando anexo…"
        AppLanguage.SPANISH -> "Importando adjunto…"
        AppLanguage.FRENCH -> "Import de la pièce jointe…"
        AppLanguage.GERMAN -> "Anhang wird importiert…"
        AppLanguage.RUSSIAN -> "Импорт вложения…"
        AppLanguage.JAPANESE -> "添付ファイルをインポート中…"
        AppLanguage.KOREAN -> "첨부 파일 가져오는 중…"
    }
    val agentAttachFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "附件导入失败（文件过大或存储空间不足）"
        AppLanguage.ENGLISH -> "Attachment import failed (file too large or storage full)"
        AppLanguage.ARABIC -> "فشل استيراد المرفق (الملف كبير جدًا أو التخزين ممتلئ)"
        AppLanguage.PORTUGUESE -> "Falha ao importar anexo (arquivo muito grande ou armazenamento cheio)"
        AppLanguage.SPANISH -> "Error al importar el adjunto (archivo demasiado grande o almacenamiento lleno)"
        AppLanguage.FRENCH -> "Échec de l'import de la pièce jointe (fichier trop volumineux ou stockage plein)"
        AppLanguage.GERMAN -> "Anhang-Import fehlgeschlagen (Datei zu groß oder Speicher voll)"
        AppLanguage.RUSSIAN -> "Не удалось импортировать вложение (файл слишком большой или память заполнена)"
        AppLanguage.JAPANESE -> "添付ファイルのインポートに失敗しました（ファイルが大きすぎるか、ストレージ不足）"
        AppLanguage.KOREAN -> "첨부 파일 가져오기 실패 (파일이 너무 크거나 저장 공간 부족)"
    }
    val agentContextChipLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "上下文"
        AppLanguage.ENGLISH -> "Context"
        AppLanguage.ARABIC -> "السياق"
        AppLanguage.PORTUGUESE -> "Contexto"
        AppLanguage.SPANISH -> "Contexto"
        AppLanguage.FRENCH -> "Contexte"
        AppLanguage.GERMAN -> "Kontext"
        AppLanguage.RUSSIAN -> "Контекст"
        AppLanguage.JAPANESE -> "コンテキスト"
        AppLanguage.KOREAN -> "컨텍스트"
    }
    val agentContextPickerTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择上下文"
        AppLanguage.ENGLISH -> "Select context"
        AppLanguage.ARABIC -> "اختيار السياق"
        AppLanguage.PORTUGUESE -> "Selecionar contexto"
        AppLanguage.SPANISH -> "Seleccionar contexto"
        AppLanguage.FRENCH -> "Sélectionner le contexte"
        AppLanguage.GERMAN -> "Kontext auswählen"
        AppLanguage.RUSSIAN -> "Выбрать контекст"
        AppLanguage.JAPANESE -> "コンテキストを選択"
        AppLanguage.KOREAN -> "컨텍스트 선택"
    }
    val agentContextAppsHeader: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "应用"
        AppLanguage.ENGLISH -> "Apps"
        AppLanguage.ARABIC -> "التطبيقات"
        AppLanguage.PORTUGUESE -> "Apps"
        AppLanguage.SPANISH -> "Apps"
        AppLanguage.FRENCH -> "Apps"
        AppLanguage.GERMAN -> "Apps"
        AppLanguage.RUSSIAN -> "Приложения"
        AppLanguage.JAPANESE -> "アプリ"
        AppLanguage.KOREAN -> "앱"
    }
    val agentContextModulesHeader: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "扩展模块"
        AppLanguage.ENGLISH -> "Modules"
        AppLanguage.ARABIC -> "الوحدات"
        AppLanguage.PORTUGUESE -> "Módulos"
        AppLanguage.SPANISH -> "Módulos"
        AppLanguage.FRENCH -> "Modules"
        AppLanguage.GERMAN -> "Module"
        AppLanguage.RUSSIAN -> "Модули"
        AppLanguage.JAPANESE -> "モジュール"
        AppLanguage.KOREAN -> "모듈"
    }
    val agentContextEmpty: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "还没有应用或模块。"
        AppLanguage.ENGLISH -> "No apps or modules yet."
        AppLanguage.ARABIC -> "لا توجد تطبيقات أو وحدات بعد."
        AppLanguage.PORTUGUESE -> "Nenhum app ou módulo ainda."
        AppLanguage.SPANISH -> "Aún no hay apps ni módulos."
        AppLanguage.FRENCH -> "Pas encore d'apps ni de modules."
        AppLanguage.GERMAN -> "Noch keine Apps oder Module."
        AppLanguage.RUSSIAN -> "Пока нет приложений или модулей."
        AppLanguage.JAPANESE -> "アプリやモジュールはまだありません。"
        AppLanguage.KOREAN -> "아직 앱이나 모듈이 없습니다."
    }
    val agentContextPickerDone: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "完成"
        AppLanguage.ENGLISH -> "Done"
        AppLanguage.ARABIC -> "تم"
        AppLanguage.PORTUGUESE -> "Concluído"
        AppLanguage.SPANISH -> "Listo"
        AppLanguage.FRENCH -> "Terminé"
        AppLanguage.GERMAN -> "Fertig"
        AppLanguage.RUSSIAN -> "Готово"
        AppLanguage.JAPANESE -> "完了"
        AppLanguage.KOREAN -> "완료"
    }
    val agentContextAllCategories: String get() = when (Strings.lang) {
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
    val agentSaveAsApp: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "保存为应用"
        AppLanguage.ENGLISH -> "Save as app"
        AppLanguage.ARABIC -> "حفظ كتطبيق"
        AppLanguage.PORTUGUESE -> "Salvar como app"
        AppLanguage.SPANISH -> "Guardar como app"
        AppLanguage.FRENCH -> "Enregistrer comme app"
        AppLanguage.GERMAN -> "Als App speichern"
        AppLanguage.RUSSIAN -> "Сохранить как приложение"
        AppLanguage.JAPANESE -> "アプリとして保存"
        AppLanguage.KOREAN -> "앱으로 저장"
    }
    val agentSaveAsAppTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "保存到我的应用"
        AppLanguage.ENGLISH -> "Save to My Apps"
        AppLanguage.ARABIC -> "حفظ في تطبيقاتي"
        AppLanguage.PORTUGUESE -> "Salvar em Meus Apps"
        AppLanguage.SPANISH -> "Guardar en Mis Apps"
        AppLanguage.FRENCH -> "Enregistrer dans Mes Apps"
        AppLanguage.GERMAN -> "In Meine Apps speichern"
        AppLanguage.RUSSIAN -> "Сохранить в Мои приложения"
        AppLanguage.JAPANESE -> "マイアプリに保存"
        AppLanguage.KOREAN -> "내 앱에 저장"
    }
    val agentSaveAsAppDescription: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "把当前会话生成的项目添加到主屏的应用列表"
        AppLanguage.ENGLISH -> "Add this session's project to your home apps list"
        AppLanguage.ARABIC -> "أضف مشروع هذه الجلسة إلى قائمة التطبيقات في الشاشة الرئيسية"
        AppLanguage.PORTUGUESE -> "Adicione o projeto desta sessão à sua lista de apps iniciais"
        AppLanguage.SPANISH -> "Añade el proyecto de esta sesión a tu lista de apps de inicio"
        AppLanguage.FRENCH -> "Ajoutez le projet de cette session à votre liste d'applications"
        AppLanguage.GERMAN -> "Projekt dieser Sitzung zur Home-App-Liste hinzufügen"
        AppLanguage.RUSSIAN -> "Добавить проект сессии в список домашних приложений"
        AppLanguage.JAPANESE -> "このセッションのプロジェクトをホームアプリリストに追加"
        AppLanguage.KOREAN -> "이 세션의 프로젝트를 홈 앱 목록에 추가"
    }
    val agentSaveAsAppNameLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "应用名称"
        AppLanguage.ENGLISH -> "App name"
        AppLanguage.ARABIC -> "اسم التطبيق"
        AppLanguage.PORTUGUESE -> "Nome do app"
        AppLanguage.SPANISH -> "Nombre de la app"
        AppLanguage.FRENCH -> "Nom de l'app"
        AppLanguage.GERMAN -> "App-Name"
        AppLanguage.RUSSIAN -> "Название приложения"
        AppLanguage.JAPANESE -> "アプリ名"
        AppLanguage.KOREAN -> "앱 이름"
    }
    val agentSaveAsAppPickIcon: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择图标（可选）"
        AppLanguage.ENGLISH -> "Choose icon (optional)"
        AppLanguage.ARABIC -> "اختر أيقونة (اختياري)"
        AppLanguage.PORTUGUESE -> "Escolher ícone (opcional)"
        AppLanguage.SPANISH -> "Elegir icono (opcional)"
        AppLanguage.FRENCH -> "Choisir icône (facultatif)"
        AppLanguage.GERMAN -> "Symbol wählen (optional)"
        AppLanguage.RUSSIAN -> "Выбрать иконку (необязательно)"
        AppLanguage.JAPANESE -> "アイコンを選択（任意）"
        AppLanguage.KOREAN -> "아이콘 선택 (선택 사항)"
    }
    val agentSaveAsAppChangeIcon: String get() = when (Strings.lang) {
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
    val agentSaveAsAppConfirm: String get() = when (Strings.lang) {
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
    val agentSaveAsAppSuccess: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已保存到我的应用：%s"
        AppLanguage.ENGLISH -> "Saved to My Apps: %s"
        AppLanguage.ARABIC -> "تم الحفظ في تطبيقاتي: %s"
        AppLanguage.PORTUGUESE -> "Salvo em Meus Apps: %s"
        AppLanguage.SPANISH -> "Guardado en Mis Apps: %s"
        AppLanguage.FRENCH -> "Enregistré dans Mes Apps: %s"
        AppLanguage.GERMAN -> "In Meine Apps gespeichert: %s"
        AppLanguage.RUSSIAN -> "Сохранено в Мои приложения: %s"
        AppLanguage.JAPANESE -> "マイアプリに保存しました: %s"
        AppLanguage.KOREAN -> "내 앱에 저장됨: %s"
    }
    val agentSaveAsAppFailure: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "保存失败：%s"
        AppLanguage.ENGLISH -> "Save failed: %s"
        AppLanguage.ARABIC -> "فشل الحفظ: %s"
        AppLanguage.PORTUGUESE -> "Falha ao salvar: %s"
        AppLanguage.SPANISH -> "Error al guardar: %s"
        AppLanguage.FRENCH -> "Échec de l'enregistrement: %s"
        AppLanguage.GERMAN -> "Speichern fehlgeschlagen: %s"
        AppLanguage.RUSSIAN -> "Ошибка сохранения: %s"
        AppLanguage.JAPANESE -> "保存に失敗: %s"
        AppLanguage.KOREAN -> "저장 실패: %s"
    }
    val agentSaveAsModule: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "保存为模块"
        AppLanguage.ENGLISH -> "Save as module"
        AppLanguage.ARABIC -> "حفظ كوحدة"
        AppLanguage.PORTUGUESE -> "Salvar como módulo"
        AppLanguage.SPANISH -> "Guardar como módulo"
        AppLanguage.FRENCH -> "Enregistrer comme module"
        AppLanguage.GERMAN -> "Als Modul speichern"
        AppLanguage.RUSSIAN -> "Сохранить как модуль"
        AppLanguage.JAPANESE -> "モジュールとして保存"
        AppLanguage.KOREAN -> "모듈로 저장"
    }
    val agentSaveAsModuleSuccess: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已保存到扩展模块：%s"
        AppLanguage.ENGLISH -> "Saved to Extension Modules: %s"
        AppLanguage.ARABIC -> "تم الحفظ في وحدات الإضافة: %s"
        AppLanguage.PORTUGUESE -> "Salvo em Módulos de Extensão: %s"
        AppLanguage.SPANISH -> "Guardado en Módulos de Extensión: %s"
        AppLanguage.FRENCH -> "Enregistré dans Modules d'Extension: %s"
        AppLanguage.GERMAN -> "In Erweiterungsmodule gespeichert: %s"
        AppLanguage.RUSSIAN -> "Сохранено в Модули расширений: %s"
        AppLanguage.JAPANESE -> "拡張モジュールに保存しました: %s"
        AppLanguage.KOREAN -> "확장 모듈에 저장됨: %s"
    }
    val agentSaveAsModuleSuccessMany: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已保存到扩展模块：%s 等 %d+1 个"
        AppLanguage.ENGLISH -> "Saved to Extension Modules: %s + %d more"
        AppLanguage.ARABIC -> "تم الحفظ في وحدات الإضافة: %s + %d أخرى"
        AppLanguage.PORTUGUESE -> "Salvo em Módulos de Extensão: %s + %d mais"
        AppLanguage.SPANISH -> "Guardado en Módulos de Extensión: %s + %d más"
        AppLanguage.FRENCH -> "Enregistré dans Modules d'Extension: %s + %d autres"
        AppLanguage.GERMAN -> "In Erweiterungsmodule gespeichert: %s + %d weitere"
        AppLanguage.RUSSIAN -> "Сохранено в Модули расширений: %s + %d ещё"
        AppLanguage.JAPANESE -> "拡張モジュールに保存: %s + %d 件"
        AppLanguage.KOREAN -> "확장 모듈에 저장됨: %s + %d개 더"
    }
    val agentSaveAsModuleFailure: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "保存模块失败：%s"
        AppLanguage.ENGLISH -> "Save module failed: %s"
        AppLanguage.ARABIC -> "فشل حفظ الوحدة: %s"
        AppLanguage.PORTUGUESE -> "Falha ao salvar módulo: %s"
        AppLanguage.SPANISH -> "Error al guardar módulo: %s"
        AppLanguage.FRENCH -> "Échec de l'enregistrement du module: %s"
        AppLanguage.GERMAN -> "Modul speichern fehlgeschlagen: %s"
        AppLanguage.RUSSIAN -> "Ошибка сохранения модуля: %s"
        AppLanguage.JAPANESE -> "モジュール保存に失敗: %s"
        AppLanguage.KOREAN -> "모듈 저장 실패: %s"
    }
    val agentSaveNoArtifacts: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "会话里没找到可以保存的内容"
        AppLanguage.ENGLISH -> "Nothing in this session is ready to save"
        AppLanguage.ARABIC -> "لا يوجد شيء في هذه الجلسة جاهز للحفظ"
        AppLanguage.PORTUGUESE -> "Nada nesta sessão está pronto para salvar"
        AppLanguage.SPANISH -> "Nada en esta sesión está listo para guardar"
        AppLanguage.FRENCH -> "Rien dans cette session n'est prêt à être enregistré"
        AppLanguage.GERMAN -> "Nichts in dieser Sitzung ist speicherbereit"
        AppLanguage.RUSSIAN -> "В этой сессии нет ничего готового к сохранению"
        AppLanguage.JAPANESE -> "このセッションに保存できるものはありません"
        AppLanguage.KOREAN -> "이 세션에 저장할 내용이 없습니다"
    }
    val agentSaveDialogChooseLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择要保存的项目"
        AppLanguage.ENGLISH -> "Choose what to save"
        AppLanguage.ARABIC -> "اختر ما تريد حفظه"
        AppLanguage.PORTUGUESE -> "Escolha o que salvar"
        AppLanguage.SPANISH -> "Elige qué guardar"
        AppLanguage.FRENCH -> "Choisissez quoi enregistrer"
        AppLanguage.GERMAN -> "Wählen Sie, was gespeichert werden soll"
        AppLanguage.RUSSIAN -> "Выберите, что сохранить"
        AppLanguage.JAPANESE -> "保存する項目を選択"
        AppLanguage.KOREAN -> "저장할 항목 선택"
    }
    val agentSaveDialogTargetApp: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "我的应用"
        AppLanguage.ENGLISH -> "My Apps"
        AppLanguage.ARABIC -> "تطبيقاتي"
        AppLanguage.PORTUGUESE -> "Meus aplicativos"
        AppLanguage.SPANISH -> "Mis aplicaciones"
        AppLanguage.FRENCH -> "Mes applications"
        AppLanguage.GERMAN -> "Meine Apps"
        AppLanguage.RUSSIAN -> "Мои приложения"
        AppLanguage.JAPANESE -> "マイアプリ"
        AppLanguage.KOREAN -> "내 앱"
    }
    val agentSaveDialogTargetModule: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "扩展模块"
        AppLanguage.ENGLISH -> "Extension Modules"
        AppLanguage.ARABIC -> "وحدات الإضافة"
        AppLanguage.PORTUGUESE -> "Módulos de extensão"
        AppLanguage.SPANISH -> "Módulos de extensión"
        AppLanguage.FRENCH -> "Modules d'extension"
        AppLanguage.GERMAN -> "Erweiterungsmodule"
        AppLanguage.RUSSIAN -> "Модули расширений"
        AppLanguage.JAPANESE -> "拡張モジュール"
        AppLanguage.KOREAN -> "확장 모듈"
    }
    val agentSaveArtifactSuffix: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "%d 个文件 · %s"
        AppLanguage.ENGLISH -> "%d files · %s"
        AppLanguage.ARABIC -> "%d ملفات · %s"
        AppLanguage.PORTUGUESE -> "%d arquivos · %s"
        AppLanguage.SPANISH -> "%d archivos · %s"
        AppLanguage.FRENCH -> "%d fichiers · %s"
        AppLanguage.GERMAN -> "%d Dateien · %s"
        AppLanguage.RUSSIAN -> "%d файлов · %s"
        AppLanguage.JAPANESE -> "%d ファイル · %s"
        AppLanguage.KOREAN -> "%d개 파일 · %s"
    }
    val agentKindHtml: String get() = when (Strings.lang) {
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
    val agentKindReact: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "React"
        AppLanguage.ENGLISH -> "React"
        AppLanguage.ARABIC -> "React"
        AppLanguage.PORTUGUESE -> "React"
        AppLanguage.SPANISH -> "React"
        AppLanguage.FRENCH -> "React"
        AppLanguage.GERMAN -> "React"
        AppLanguage.RUSSIAN -> "React"
        AppLanguage.JAPANESE -> "React"
        AppLanguage.KOREAN -> "React"
    }
    val agentKindVue: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Vue"
        AppLanguage.ENGLISH -> "Vue"
        AppLanguage.ARABIC -> "Vue"
        AppLanguage.PORTUGUESE -> "Vue"
        AppLanguage.SPANISH -> "Vue"
        AppLanguage.FRENCH -> "Vue"
        AppLanguage.GERMAN -> "Vue"
        AppLanguage.RUSSIAN -> "Vue"
        AppLanguage.JAPANESE -> "Vue"
        AppLanguage.KOREAN -> "Vue"
    }
    val agentKindNodeJs: String get() = when (Strings.lang) {
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
    val agentKindPhp: String get() = when (Strings.lang) {
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
    val agentKindPython: String get() = when (Strings.lang) {
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
    val agentKindGo: String get() = when (Strings.lang) {
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
    val agentKindMultiWeb: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "多站点"
        AppLanguage.ENGLISH -> "Multi-Web"
        AppLanguage.ARABIC -> "متعدد المواقع"
        AppLanguage.PORTUGUESE -> "Multi-Web"
        AppLanguage.SPANISH -> "Multi-Web"
        AppLanguage.FRENCH -> "Multi-Web"
        AppLanguage.GERMAN -> "Multi-Web"
        AppLanguage.RUSSIAN -> "Мультисайт"
        AppLanguage.JAPANESE -> "マルチウェブ"
        AppLanguage.KOREAN -> "멀티 웹"
    }
    val agentKindGallery: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "图库"
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
    val agentKindJsModule: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "JS 模块"
        AppLanguage.ENGLISH -> "JS Module"
        AppLanguage.ARABIC -> "وحدة JS"
        AppLanguage.PORTUGUESE -> "Módulo JS"
        AppLanguage.SPANISH -> "Módulo JS"
        AppLanguage.FRENCH -> "Module JS"
        AppLanguage.GERMAN -> "JS-Modul"
        AppLanguage.RUSSIAN -> "JS-модуль"
        AppLanguage.JAPANESE -> "JSモジュール"
        AppLanguage.KOREAN -> "JS 모듈"
    }
    val agentKindStyleModule: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "样式模块"
        AppLanguage.ENGLISH -> "Style Module"
        AppLanguage.ARABIC -> "وحدة نمط"
        AppLanguage.PORTUGUESE -> "Módulo de estilo"
        AppLanguage.SPANISH -> "Módulo de estilo"
        AppLanguage.FRENCH -> "Module de style"
        AppLanguage.GERMAN -> "Stil-Modul"
        AppLanguage.RUSSIAN -> "Модуль стиля"
        AppLanguage.JAPANESE -> "スタイルモジュール"
        AppLanguage.KOREAN -> "스타일 모듈"
    }
    val agentKindUserScript: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "用户脚本"
        AppLanguage.ENGLISH -> "Userscript"
        AppLanguage.ARABIC -> "نص مستخدم"
        AppLanguage.PORTUGUESE -> "Userscript"
        AppLanguage.SPANISH -> "Userscript"
        AppLanguage.FRENCH -> "Userscript"
        AppLanguage.GERMAN -> "Userscript"
        AppLanguage.RUSSIAN -> "Юзерскрипт"
        AppLanguage.JAPANESE -> "ユーザースクリプト"
        AppLanguage.KOREAN -> "유저스크립트"
    }
    val agentKindChromeExtension: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Chrome 扩展"
        AppLanguage.ENGLISH -> "Chrome Extension"
        AppLanguage.ARABIC -> "إضافة Chrome"
        AppLanguage.PORTUGUESE -> "Extensão do Chrome"
        AppLanguage.SPANISH -> "Extensión de Chrome"
        AppLanguage.FRENCH -> "Extension Chrome"
        AppLanguage.GERMAN -> "Chrome-Erweiterung"
        AppLanguage.RUSSIAN -> "Расширение Chrome"
        AppLanguage.JAPANESE -> "Chrome拡張機能"
        AppLanguage.KOREAN -> "Chrome 확장 프로그램"
    }

    val agentAutoModeLabel: String get() = when (Strings.lang) {
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
    val agentManualModeLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "手动"
        AppLanguage.ENGLISH -> "Manual"
        AppLanguage.ARABIC -> "يدوي"
        AppLanguage.PORTUGUESE -> "Manual"
        AppLanguage.SPANISH -> "Manual"
        AppLanguage.FRENCH -> "Manuel"
        AppLanguage.GERMAN -> "Manuell"
        AppLanguage.RUSSIAN -> "Вручную"
        AppLanguage.JAPANESE -> "手動"
        AppLanguage.KOREAN -> "수동"
    }

    val agentSlashChipLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "命令"
        AppLanguage.ENGLISH -> "Commands"
        AppLanguage.ARABIC -> "الأوامر"
        AppLanguage.PORTUGUESE -> "Comandos"
        AppLanguage.SPANISH -> "Comandos"
        AppLanguage.FRENCH -> "Commandes"
        AppLanguage.GERMAN -> "Befehle"
        AppLanguage.RUSSIAN -> "Команды"
        AppLanguage.JAPANESE -> "コマンド"
        AppLanguage.KOREAN -> "명령어"
    }
    val agentChangesReviewHeader: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "%d 个改动"
        AppLanguage.ENGLISH -> "%d changes"
        AppLanguage.ARABIC -> "%d تغييرات"
        AppLanguage.PORTUGUESE -> "%d alterações"
        AppLanguage.SPANISH -> "%d cambios"
        AppLanguage.FRENCH -> "%d modifications"
        AppLanguage.GERMAN -> "%d Änderungen"
        AppLanguage.RUSSIAN -> "%d изменений"
        AppLanguage.JAPANESE -> "%d件の変更"
        AppLanguage.KOREAN -> "%d개 변경"
    }
    val agentChangesReviewUndoAll: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "全部撤销"
        AppLanguage.ENGLISH -> "Undo all"
        AppLanguage.ARABIC -> "تراجع عن الكل"
        AppLanguage.PORTUGUESE -> "Desfazer tudo"
        AppLanguage.SPANISH -> "Deshacer todo"
        AppLanguage.FRENCH -> "Tout annuler"
        AppLanguage.GERMAN -> "Alle rückgängig"
        AppLanguage.RUSSIAN -> "Отменить всё"
        AppLanguage.JAPANESE -> "すべて元に戻す"
        AppLanguage.KOREAN -> "모두 실행 취소"
    }
    val agentChangesReviewClear: String get() = when (Strings.lang) {
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
    val agentChangesReviewUndoOne: String get() = when (Strings.lang) {
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
    val agentChangesReviewExpand: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "展开"
        AppLanguage.ENGLISH -> "Expand"
        AppLanguage.ARABIC -> "توسيع"
        AppLanguage.PORTUGUESE -> "Expandir"
        AppLanguage.SPANISH -> "Expandir"
        AppLanguage.FRENCH -> "Développer"
        AppLanguage.GERMAN -> "Ausklappen"
        AppLanguage.RUSSIAN -> "Развернуть"
        AppLanguage.JAPANESE -> "展開"
        AppLanguage.KOREAN -> "펼치기"
    }
    val agentChangesReviewCollapse: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "折叠"
        AppLanguage.ENGLISH -> "Collapse"
        AppLanguage.ARABIC -> "طي"
        AppLanguage.PORTUGUESE -> "Recolher"
        AppLanguage.SPANISH -> "Contraer"
        AppLanguage.FRENCH -> "Réduire"
        AppLanguage.GERMAN -> "Einklappen"
        AppLanguage.RUSSIAN -> "Свернуть"
        AppLanguage.JAPANESE -> "折りたたむ"
        AppLanguage.KOREAN -> "접기"
    }
    val agentChangesUndoneToast: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已撤销 %d 个改动"
        AppLanguage.ENGLISH -> "Undid %d changes"
        AppLanguage.ARABIC -> "تم التراجع عن %d تغييرات"
        AppLanguage.PORTUGUESE -> "%d alterações desfeitas"
        AppLanguage.SPANISH -> "%d cambios deshechos"
        AppLanguage.FRENCH -> "%d modifications annulées"
        AppLanguage.GERMAN -> "%d Änderungen rückgängig gemacht"
        AppLanguage.RUSSIAN -> "Отменено %d изменений"
        AppLanguage.JAPANESE -> "%d件の変更を元に戻しました"
        AppLanguage.KOREAN -> "%d개 변경 실행 취소됨"
    }
    val agentAppChangesHeader: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "%d 个应用变更"
        AppLanguage.ENGLISH -> "%d app changes"
        AppLanguage.ARABIC -> "%d تغييرات على التطبيقات"
        AppLanguage.PORTUGUESE -> "%d alterações em apps"
        AppLanguage.SPANISH -> "%d cambios de apps"
        AppLanguage.FRENCH -> "%d modifications d'apps"
        AppLanguage.GERMAN -> "%d App-Änderungen"
        AppLanguage.RUSSIAN -> "%d изменений приложений"
        AppLanguage.JAPANESE -> "%d件のアプリ変更"
        AppLanguage.KOREAN -> "%d개 앱 변경"
    }
    val agentAppChangesKindCreate: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "新建"
        AppLanguage.ENGLISH -> "Created"
        AppLanguage.ARABIC -> "إنشاء"
        AppLanguage.PORTUGUESE -> "Criado"
        AppLanguage.SPANISH -> "Creado"
        AppLanguage.FRENCH -> "Créé"
        AppLanguage.GERMAN -> "Erstellt"
        AppLanguage.RUSSIAN -> "Создано"
        AppLanguage.JAPANESE -> "作成"
        AppLanguage.KOREAN -> "생성"
    }
    val agentAppChangesKindUpdate: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "更新"
        AppLanguage.ENGLISH -> "Updated"
        AppLanguage.ARABIC -> "تحديث"
        AppLanguage.PORTUGUESE -> "Atualizado"
        AppLanguage.SPANISH -> "Actualizado"
        AppLanguage.FRENCH -> "Mis à jour"
        AppLanguage.GERMAN -> "Aktualisiert"
        AppLanguage.RUSSIAN -> "Обновлено"
        AppLanguage.JAPANESE -> "更新"
        AppLanguage.KOREAN -> "업데이트"
    }
    val agentAppChangesOpen: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "打开应用"
        AppLanguage.ENGLISH -> "Open app"
        AppLanguage.ARABIC -> "فتح التطبيق"
        AppLanguage.PORTUGUESE -> "Abrir app"
        AppLanguage.SPANISH -> "Abrir app"
        AppLanguage.FRENCH -> "Ouvrir l'app"
        AppLanguage.GERMAN -> "App öffnen"
        AppLanguage.RUSSIAN -> "Открыть приложение"
        AppLanguage.JAPANESE -> "アプリを開く"
        AppLanguage.KOREAN -> "앱 열기"
    }
    val agentAppChangesFields: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "变更字段：%s"
        AppLanguage.ENGLISH -> "Fields: %s"
        AppLanguage.ARABIC -> "الحقول: %s"
        AppLanguage.PORTUGUESE -> "Campos: %s"
        AppLanguage.SPANISH -> "Campos: %s"
        AppLanguage.FRENCH -> "Champs : %s"
        AppLanguage.GERMAN -> "Felder: %s"
        AppLanguage.RUSSIAN -> "Поля: %s"
        AppLanguage.JAPANESE -> "フィールド: %s"
        AppLanguage.KOREAN -> "필드: %s"
    }
    val agentPlanModeBadge: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Plan 模式"
        AppLanguage.ENGLISH -> "Plan Mode"
        AppLanguage.ARABIC -> "وضع التخطيط"
        AppLanguage.PORTUGUESE -> "Modo Plan"
        AppLanguage.SPANISH -> "Modo Plan"
        AppLanguage.FRENCH -> "Mode Plan"
        AppLanguage.GERMAN -> "Plan-Modus"
        AppLanguage.RUSSIAN -> "Режим Plan"
        AppLanguage.JAPANESE -> "Planモード"
        AppLanguage.KOREAN -> "Plan 모드"
    }
    val agentPlanModeNoPath: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "未指定"
        AppLanguage.ENGLISH -> "(no path)"
        AppLanguage.ARABIC -> "(لا يوجد مسار)"
        AppLanguage.PORTUGUESE -> "(sem caminho)"
        AppLanguage.SPANISH -> "(sin ruta)"
        AppLanguage.FRENCH -> "(aucun chemin)"
        AppLanguage.GERMAN -> "(kein Pfad)"
        AppLanguage.RUSSIAN -> "(нет пути)"
        AppLanguage.JAPANESE -> "(パスなし)"
        AppLanguage.KOREAN -> "(경로 없음)"
    }
    val agentPlanModeExitTooltip: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "退出 Plan 模式"
        AppLanguage.ENGLISH -> "Exit plan mode"
        AppLanguage.ARABIC -> "الخروج من وضع التخطيط"
        AppLanguage.PORTUGUESE -> "Sair do modo Plan"
        AppLanguage.SPANISH -> "Salir del modo Plan"
        AppLanguage.FRENCH -> "Quitter le mode Plan"
        AppLanguage.GERMAN -> "Plan-Modus verlassen"
        AppLanguage.RUSSIAN -> "Выйти из режима Plan"
        AppLanguage.JAPANESE -> "Planモードを終了"
        AppLanguage.KOREAN -> "Plan 모드 종료"
    }
    val agentOpenDrawer: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "打开侧边栏"
        AppLanguage.ENGLISH -> "Open drawer"
        AppLanguage.ARABIC -> "فتح اللوحة الجانبية"
        AppLanguage.PORTUGUESE -> "Abrir painel"
        AppLanguage.SPANISH -> "Abrir panel"
        AppLanguage.FRENCH -> "Ouvrir le panneau"
        AppLanguage.GERMAN -> "Seitenleiste öffnen"
        AppLanguage.RUSSIAN -> "Открыть панель"
        AppLanguage.JAPANESE -> "ドロワーを開く"
        AppLanguage.KOREAN -> "서랍 열기"
    }
    val agentHomeTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "构建点什么"
        AppLanguage.ENGLISH -> "Build something"
        AppLanguage.ARABIC -> "ابنِ شيئًا"
        AppLanguage.PORTUGUESE -> "Construa algo"
        AppLanguage.SPANISH -> "Construye algo"
        AppLanguage.FRENCH -> "Construisez quelque chose"
        AppLanguage.GERMAN -> "Baue etwas"
        AppLanguage.RUSSIAN -> "Создайте что-нибудь"
        AppLanguage.JAPANESE -> "何かを作る"
        AppLanguage.KOREAN -> "무언가 만들기"
    }
    val agentHomeSubtitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "直接在下方输入即可开始，不必先新建会话"
        AppLanguage.ENGLISH -> "Type below to start right away — no need to create a session first"
        AppLanguage.ARABIC -> "اكتب أدناه للبدء مباشرة — لا حاجة لإنشاء جلسة أولاً"
        AppLanguage.PORTUGUESE -> "Digite abaixo para começar — sem precisar criar sessão antes"
        AppLanguage.SPANISH -> "Escribe abajo para empezar — no hace falta crear sesión primero"
        AppLanguage.FRENCH -> "Tapez ci-dessous pour commencer — pas besoin de créer une session"
        AppLanguage.GERMAN -> "Tippe unten, um sofort zu starten — keine neue Sitzung nötig"
        AppLanguage.RUSSIAN -> "Пишите ниже, чтобы начать сразу — создавать сессию заранее не нужно"
        AppLanguage.JAPANESE -> "下に入力するだけで開始。先にセッションを作る必要はありません"
        AppLanguage.KOREAN -> "아래에서 바로 입력하세요. 먼저 세션을 만들 필요 없습니다"
    }
    val agentHomeRecentTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "最近的会话"
        AppLanguage.ENGLISH -> "Recent sessions"
        AppLanguage.ARABIC -> "الجلسات الأخيرة"
        AppLanguage.PORTUGUESE -> "Sessões recentes"
        AppLanguage.SPANISH -> "Sesiones recientes"
        AppLanguage.FRENCH -> "Sessions récentes"
        AppLanguage.GERMAN -> "Letzte Sitzungen"
        AppLanguage.RUSSIAN -> "Недавние сессии"
        AppLanguage.JAPANESE -> "最近のセッション"
        AppLanguage.KOREAN -> "최근 세션"
    }
    val agentHomeRecentSeeAll: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "查看全部"
        AppLanguage.ENGLISH -> "See all"
        AppLanguage.ARABIC -> "عرض الكل"
        AppLanguage.PORTUGUESE -> "Ver tudo"
        AppLanguage.SPANISH -> "Ver todo"
        AppLanguage.FRENCH -> "Voir tout"
        AppLanguage.GERMAN -> "Alle anzeigen"
        AppLanguage.RUSSIAN -> "Смотреть все"
        AppLanguage.JAPANESE -> "すべて表示"
        AppLanguage.KOREAN -> "모두 보기"
    }
    val agentHomeUntitledSession: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "未命名会话"
        AppLanguage.ENGLISH -> "Untitled session"
        AppLanguage.ARABIC -> "جلسة بدون عنوان"
        AppLanguage.PORTUGUESE -> "Sessão sem título"
        AppLanguage.SPANISH -> "Sesión sin título"
        AppLanguage.FRENCH -> "Session sans titre"
        AppLanguage.GERMAN -> "Unbenannte Sitzung"
        AppLanguage.RUSSIAN -> "Сессия без названия"
        AppLanguage.JAPANESE -> "無題のセッション"
        AppLanguage.KOREAN -> "제목 없는 세션"
    }
    val agentHomeStartersTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "试试这些"
        AppLanguage.ENGLISH -> "Try these"
        AppLanguage.ARABIC -> "جرّب هذه"
        AppLanguage.PORTUGUESE -> "Experimente estes"
        AppLanguage.SPANISH -> "Prueba estos"
        AppLanguage.FRENCH -> "Essayez ceci"
        AppLanguage.GERMAN -> "Probiere diese"
        AppLanguage.RUSSIAN -> "Попробуйте эти"
        AppLanguage.JAPANESE -> "これを試す"
        AppLanguage.KOREAN -> "이런 걸 해보세요"
    }
    val agentStarterPrompt1: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "做一个简洁的待办清单网页应用"
        AppLanguage.ENGLISH -> "Build a clean todo list web app"
        AppLanguage.ARABIC -> "أنشئ تطبيق قائمة مهام ويب أنيق"
        AppLanguage.PORTUGUESE -> "Crie um app web limpo de lista de tarefas"
        AppLanguage.SPANISH -> "Crea una app web limpia de lista de tareas"
        AppLanguage.FRENCH -> "Créez une app web de todo list épurée"
        AppLanguage.GERMAN -> "Baue eine schlichte Todo-Listen-Web-App"
        AppLanguage.RUSSIAN -> "Сделайте чистое веб-приложение списка дел"
        AppLanguage.JAPANESE -> "シンプルなToDoリストWebアプリを作る"
        AppLanguage.KOREAN -> "깔끔한 할 일 목록 웹 앱 만들기"
    }
    val agentStarterPrompt2: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "做一个带暗色模式的个人作品集主页"
        AppLanguage.ENGLISH -> "Make a personal portfolio homepage with dark mode"
        AppLanguage.ARABIC -> "اصنع صفحة محفظة شخصية مع الوضع الداكن"
        AppLanguage.PORTUGUESE -> "Faça uma homepage de portfólio com modo escuro"
        AppLanguage.SPANISH -> "Haz una homepage de portafolio con modo oscuro"
        AppLanguage.FRENCH -> "Faites une page portfolio perso avec mode sombre"
        AppLanguage.GERMAN -> "Erstelle eine Portfolio-Homepage mit Dark Mode"
        AppLanguage.RUSSIAN -> "Сделайте портфолио-главную с тёмной темой"
        AppLanguage.JAPANESE -> "ダークモード付きの個人ポートフォリオホームを作る"
        AppLanguage.KOREAN -> "다크 모드 개인 포트폴리오 홈페이지 만들기"
    }
    val agentStarterPrompt3: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "把这段需求变成可预览的 HTML 页面"
        AppLanguage.ENGLISH -> "Turn this idea into a previewable HTML page"
        AppLanguage.ARABIC -> "حوّل هذه الفكرة إلى صفحة HTML قابلة للمعاينة"
        AppLanguage.PORTUGUESE -> "Transforme esta ideia em uma página HTML pré-visualizável"
        AppLanguage.SPANISH -> "Convierte esta idea en una página HTML previsualizable"
        AppLanguage.FRENCH -> "Transformez cette idée en page HTML prévisualisable"
        AppLanguage.GERMAN -> "Mach aus dieser Idee eine vorschaufähige HTML-Seite"
        AppLanguage.RUSSIAN -> "Превратите идею в HTML-страницу с превью"
        AppLanguage.JAPANESE -> "このアイデアをプレビュー可能なHTMLページにする"
        AppLanguage.KOREAN -> "이 아이디어를 미리보기 가능한 HTML 페이지로 만들기"
    }
    val agentStarterPrompt4: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "帮我搭建一个移动端优先的落地页"
        AppLanguage.ENGLISH -> "Help me scaffold a mobile-first landing page"
        AppLanguage.ARABIC -> "ساعدني في بناء صفحة هبوط مخصصة للجوال"
        AppLanguage.PORTUGUESE -> "Ajude-me a montar uma landing page mobile-first"
        AppLanguage.SPANISH -> "Ayúdame a armar una landing page mobile-first"
        AppLanguage.FRENCH -> "Aidez-moi à monter une landing page mobile-first"
        AppLanguage.GERMAN -> "Hilf mir, eine mobile-first Landingpage aufzubauen"
        AppLanguage.RUSSIAN -> "Помогите собрать mobile-first лендинг"
        AppLanguage.JAPANESE -> "モバイルファーストのランディングページを組み立てて"
        AppLanguage.KOREAN -> "모바일 우선 랜딩 페이지 뼈대 만들기"
    }
    val agentPreviewToolbarPrefix: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "预览 · %s"
        AppLanguage.ENGLISH -> "Preview · %s"
        AppLanguage.ARABIC -> "معاينة · %s"
        AppLanguage.PORTUGUESE -> "Pré-visualização · %s"
        AppLanguage.SPANISH -> "Vista previa · %s"
        AppLanguage.FRENCH -> "Aperçu · %s"
        AppLanguage.GERMAN -> "Vorschau · %s"
        AppLanguage.RUSSIAN -> "Предпросмотр · %s"
        AppLanguage.JAPANESE -> "プレビュー · %s"
        AppLanguage.KOREAN -> "미리보기 · %s"
    }
    val agentPreviewNoFile: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "（未选择文件）"
        AppLanguage.ENGLISH -> "(no file)"
        AppLanguage.ARABIC -> "(لا يوجد ملف)"
        AppLanguage.PORTUGUESE -> "(sem arquivo)"
        AppLanguage.SPANISH -> "(sin archivo)"
        AppLanguage.FRENCH -> "(aucun fichier)"
        AppLanguage.GERMAN -> "(keine Datei)"
        AppLanguage.RUSSIAN -> "(нет файла)"
        AppLanguage.JAPANESE -> "(ファイルなし)"
        AppLanguage.KOREAN -> "(파일 없음)"
    }

    val agentPreviewSwitchHeader: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "切换预览文件"
        AppLanguage.ENGLISH -> "Switch preview file"
        AppLanguage.ARABIC -> "تبديل ملف المعاينة"
        AppLanguage.PORTUGUESE -> "Trocar arquivo de pré-visualização"
        AppLanguage.SPANISH -> "Cambiar archivo de vista previa"
        AppLanguage.FRENCH -> "Changer de fichier d'aperçu"
        AppLanguage.GERMAN -> "Vorschaudatei wechseln"
        AppLanguage.RUSSIAN -> "Сменить файл предпросмотра"
        AppLanguage.JAPANESE -> "プレビューファイルを切り替え"
        AppLanguage.KOREAN -> "미리보기 파일 전환"
    }

    val agentPreviewSwitchEmpty: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "本次会话还没有 HTML / 图片可以预览"
        AppLanguage.ENGLISH -> "No HTML / image files in this session yet"
        AppLanguage.ARABIC -> "لا توجد ملفات HTML / صور في هذه الجلسة بعد"
        AppLanguage.PORTUGUESE -> "Ainda não há arquivos HTML / imagens nesta sessão"
        AppLanguage.SPANISH -> "Aún no hay archivos HTML / imágenes en esta sesión"
        AppLanguage.FRENCH -> "Pas encore de fichiers HTML / images dans cette session"
        AppLanguage.GERMAN -> "Noch keine HTML-/Bilddateien in dieser Sitzung"
        AppLanguage.RUSSIAN -> "В этой сессии ещё нет файлов HTML / изображений"
        AppLanguage.JAPANESE -> "このセッションにはまだプレビューできる HTML / 画像ファイルがありません"
        AppLanguage.KOREAN -> "이 세션에 아직 HTML / 이미지 파일이 없습니다"
    }

    val agentPreviewSwitchTooltip: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "切换文件"
        AppLanguage.ENGLISH -> "Switch file"
        AppLanguage.ARABIC -> "تبديل الملف"
        AppLanguage.PORTUGUESE -> "Trocar arquivo"
        AppLanguage.SPANISH -> "Cambiar archivo"
        AppLanguage.FRENCH -> "Changer de fichier"
        AppLanguage.GERMAN -> "Datei wechseln"
        AppLanguage.RUSSIAN -> "Сменить файл"
        AppLanguage.JAPANESE -> "ファイルを切り替え"
        AppLanguage.KOREAN -> "파일 전환"
    }
    val agentPreviewErrorsCount: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "%d 错误"
        AppLanguage.ENGLISH -> "%d err"
        AppLanguage.ARABIC -> "%d خطأ"
        AppLanguage.PORTUGUESE -> "%d erros"
        AppLanguage.SPANISH -> "%d errores"
        AppLanguage.FRENCH -> "%d err."
        AppLanguage.GERMAN -> "%d Fehler"
        AppLanguage.RUSSIAN -> "%d ошибок"
        AppLanguage.JAPANESE -> "%d件エラー"
        AppLanguage.KOREAN -> "%d 에러"
    }
    val agentPreviewWarningsCount: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "%d 警告"
        AppLanguage.ENGLISH -> "%d warn"
        AppLanguage.ARABIC -> "%d تحذير"
        AppLanguage.PORTUGUESE -> "%d avisos"
        AppLanguage.SPANISH -> "%d aviso"
        AppLanguage.FRENCH -> "%d avert."
        AppLanguage.GERMAN -> "%d Warn."
        AppLanguage.RUSSIAN -> "%d предупрежд."
        AppLanguage.JAPANESE -> "%d件警告"
        AppLanguage.KOREAN -> "%d 경고"
    }
    val agentPreviewRefresh: String get() = when (Strings.lang) {
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
    val agentSessionPin: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "置顶"
        AppLanguage.ENGLISH -> "Pin"
        AppLanguage.ARABIC -> "تثبيت"
        AppLanguage.PORTUGUESE -> "Fixar"
        AppLanguage.SPANISH -> "Fijar"
        AppLanguage.FRENCH -> "Épingler"
        AppLanguage.GERMAN -> "Anheften"
        AppLanguage.RUSSIAN -> "Закрепить"
        AppLanguage.JAPANESE -> "ピン留め"
        AppLanguage.KOREAN -> "고정"
    }
    val agentSessionUnpin: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "取消置顶"
        AppLanguage.ENGLISH -> "Unpin"
        AppLanguage.ARABIC -> "إلغاء التثبيت"
        AppLanguage.PORTUGUESE -> "Desafixar"
        AppLanguage.SPANISH -> "Desfijar"
        AppLanguage.FRENCH -> "Désépingler"
        AppLanguage.GERMAN -> "Loslösen"
        AppLanguage.RUSSIAN -> "Открепить"
        AppLanguage.JAPANESE -> "ピン留めを解除"
        AppLanguage.KOREAN -> "고정 해제"
    }
    val agentDrawerPinned: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "置顶"
        AppLanguage.ENGLISH -> "Pinned"
        AppLanguage.ARABIC -> "مثبتة"
        AppLanguage.PORTUGUESE -> "Fixadas"
        AppLanguage.SPANISH -> "Fijadas"
        AppLanguage.FRENCH -> "Épinglées"
        AppLanguage.GERMAN -> "Angeheftet"
        AppLanguage.RUSSIAN -> "Закреплённые"
        AppLanguage.JAPANESE -> "ピン留め"
        AppLanguage.KOREAN -> "고정됨"
    }
    val agentSessionDelete: String get() = when (Strings.lang) {
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
    val agentSessionMessagesShort: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "%d 条"
        AppLanguage.ENGLISH -> "%d msgs"
        AppLanguage.ARABIC -> "%d رسالة"
        AppLanguage.PORTUGUESE -> "%d msgs"
        AppLanguage.SPANISH -> "%d mens."
        AppLanguage.FRENCH -> "%d msg"
        AppLanguage.GERMAN -> "%d Nachrichten"
        AppLanguage.RUSSIAN -> "%d сообщ."
        AppLanguage.JAPANESE -> "%d件"
        AppLanguage.KOREAN -> "%d개"
    }
    val agentToolDenied: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "工具被拒绝：%s"
        AppLanguage.ENGLISH -> "Tool denied: %s"
        AppLanguage.ARABIC -> "تم رفض الأداة: %s"
        AppLanguage.PORTUGUESE -> "Ferramenta negada: %s"
        AppLanguage.SPANISH -> "Herramienta denegada: %s"
        AppLanguage.FRENCH -> "Outil refusé : %s"
        AppLanguage.GERMAN -> "Werkzeug verweigert: %s"
        AppLanguage.RUSSIAN -> "Инструмент отклонён: %s"
        AppLanguage.JAPANESE -> "ツールが拒否されました: %s"
        AppLanguage.KOREAN -> "도구 거부됨: %s"
    }
    val agentErrorPrefix: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "[错误] %s"
        AppLanguage.ENGLISH -> "[Error] %s"
        AppLanguage.ARABIC -> "[خطأ] %s"
        AppLanguage.PORTUGUESE -> "[Erro] %s"
        AppLanguage.SPANISH -> "[Error] %s"
        AppLanguage.FRENCH -> "[Erreur] %s"
        AppLanguage.GERMAN -> "[Fehler] %s"
        AppLanguage.RUSSIAN -> "[Ошибка] %s"
        AppLanguage.JAPANESE -> "[エラー] %s"
        AppLanguage.KOREAN -> "[오류] %s"
    }

    val agentSlashHeader: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "命令"
        AppLanguage.ENGLISH -> "Commands"
        AppLanguage.ARABIC -> "الأوامر"
        AppLanguage.PORTUGUESE -> "Comandos"
        AppLanguage.SPANISH -> "Comandos"
        AppLanguage.FRENCH -> "Commandes"
        AppLanguage.GERMAN -> "Befehle"
        AppLanguage.RUSSIAN -> "Команды"
        AppLanguage.JAPANESE -> "コマンド"
        AppLanguage.KOREAN -> "명령어"
    }
    val agentMentionHeader: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "项目文件"
        AppLanguage.ENGLISH -> "Project files"
        AppLanguage.ARABIC -> "ملفات المشروع"
        AppLanguage.PORTUGUESE -> "Arquivos do projeto"
        AppLanguage.SPANISH -> "Archivos del proyecto"
        AppLanguage.FRENCH -> "Fichiers du projet"
        AppLanguage.GERMAN -> "Projektdateien"
        AppLanguage.RUSSIAN -> "Файлы проекта"
        AppLanguage.JAPANESE -> "プロジェクトファイル"
        AppLanguage.KOREAN -> "프로젝트 파일"
    }
    val agentMentionEmpty: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "没有匹配的文件"
        AppLanguage.ENGLISH -> "No matching files"
        AppLanguage.ARABIC -> "لا توجد ملفات مطابقة"
        AppLanguage.PORTUGUESE -> "Nenhum arquivo correspondente"
        AppLanguage.SPANISH -> "Sin archivos coincidentes"
        AppLanguage.FRENCH -> "Aucun fichier correspondant"
        AppLanguage.GERMAN -> "Keine passenden Dateien"
        AppLanguage.RUSSIAN -> "Нет совпадающих файлов"
        AppLanguage.JAPANESE -> "一致するファイルがありません"
        AppLanguage.KOREAN -> "일치하는 파일 없음"
    }
    val agentPreviewEmpty: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "项目里还没有可预览的页面"
        AppLanguage.ENGLISH -> "Nothing to preview yet"
        AppLanguage.ARABIC -> "لا يوجد شيء للمعاينة بعد"
        AppLanguage.PORTUGUESE -> "Nada para pré-visualizar ainda"
        AppLanguage.SPANISH -> "Nada que previsualizar aún"
        AppLanguage.FRENCH -> "Rien à prévisualiser pour le moment"
        AppLanguage.GERMAN -> "Noch nichts als Vorschau"
        AppLanguage.RUSSIAN -> "Пока нечего предпросматривать"
        AppLanguage.JAPANESE -> "まだプレビューするものがありません"
        AppLanguage.KOREAN -> "미리볼 항목이 아직 없습니다"
    }
    val agentPreviewTabIdle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "暂无预览"
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
    val agentPhaseToolRunning: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "执行工具…"
        AppLanguage.ENGLISH -> "Running tool…"
        AppLanguage.ARABIC -> "تشغيل الأداة…"
        AppLanguage.PORTUGUESE -> "Executando ferramenta…"
        AppLanguage.SPANISH -> "Ejecutando herramienta…"
        AppLanguage.FRENCH -> "Exécution de l'outil…"
        AppLanguage.GERMAN -> "Werkzeug wird ausgeführt…"
        AppLanguage.RUSSIAN -> "Выполнение инструмента…"
        AppLanguage.JAPANESE -> "ツール実行中…"
        AppLanguage.KOREAN -> "도구 실행 중…"
    }
    val agentPhaseThinking: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "思考中…"
        AppLanguage.ENGLISH -> "Thinking…"
        AppLanguage.ARABIC -> "تفكير…"
        AppLanguage.PORTUGUESE -> "Pensando…"
        AppLanguage.SPANISH -> "Pensando…"
        AppLanguage.FRENCH -> "Réflexion…"
        AppLanguage.GERMAN -> "Denke nach…"
        AppLanguage.RUSSIAN -> "Размышление…"
        AppLanguage.JAPANESE -> "思考中…"
        AppLanguage.KOREAN -> "생각 중…"
    }
    val agentThinkingHeaderLive: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "思考中"
        AppLanguage.ENGLISH -> "Thinking"
        AppLanguage.ARABIC -> "يفكر"
        AppLanguage.PORTUGUESE -> "Pensando"
        AppLanguage.SPANISH -> "Pensando"
        AppLanguage.FRENCH -> "Réflexion"
        AppLanguage.GERMAN -> "Denkt nach"
        AppLanguage.RUSSIAN -> "Размышляет"
        AppLanguage.JAPANESE -> "思考中"
        AppLanguage.KOREAN -> "생각 중"
    }
    val agentThinkingDoneSeconds: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已思考 %.1fs"
        AppLanguage.ENGLISH -> "Thought for %.1fs"
        AppLanguage.ARABIC -> "فكر لمدة %.1f ث"
        AppLanguage.PORTUGUESE -> "Pensou por %.1fs"
        AppLanguage.SPANISH -> "Pensó durante %.1fs"
        AppLanguage.FRENCH -> "A réfléchi pendant %.1fs"
        AppLanguage.GERMAN -> "Dachte für %.1fs"
        AppLanguage.RUSSIAN -> "Размышлял %.1f с"
        AppLanguage.JAPANESE -> "%.1f秒思考しました"
        AppLanguage.KOREAN -> "%.1f초 생각함"
    }
    val agentThinkingTapToExpand: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "点击展开"
        AppLanguage.ENGLISH -> "Tap to expand"
        AppLanguage.ARABIC -> "اضغط للتوسيع"
        AppLanguage.PORTUGUESE -> "Toque para expandir"
        AppLanguage.SPANISH -> "Toca para expandir"
        AppLanguage.FRENCH -> "Toucher pour développer"
        AppLanguage.GERMAN -> "Tippen zum Ausklappen"
        AppLanguage.RUSSIAN -> "Нажмите, чтобы развернуть"
        AppLanguage.JAPANESE -> "タップして展開"
        AppLanguage.KOREAN -> "탭하여 펼치기"
    }
    val agentThinkingTapToCollapse: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "点击收起"
        AppLanguage.ENGLISH -> "Tap to collapse"
        AppLanguage.ARABIC -> "اضغط للطي"
        AppLanguage.PORTUGUESE -> "Toque para recolher"
        AppLanguage.SPANISH -> "Toca para contraer"
        AppLanguage.FRENCH -> "Toucher pour réduire"
        AppLanguage.GERMAN -> "Tippen zum Einklappen"
        AppLanguage.RUSSIAN -> "Нажмите, чтобы свернуть"
        AppLanguage.JAPANESE -> "タップして折りたたむ"
        AppLanguage.KOREAN -> "탭하여 접기"
    }
    val agentMessageActionCopy: String get() = when (Strings.lang) {
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
    val agentMessageActionCopyOutput: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "复制输出"
        AppLanguage.ENGLISH -> "Copy output"
        AppLanguage.ARABIC -> "نسخ المخرجات"
        AppLanguage.PORTUGUESE -> "Copiar saída"
        AppLanguage.SPANISH -> "Copiar salida"
        AppLanguage.FRENCH -> "Copier la sortie"
        AppLanguage.GERMAN -> "Ausgabe kopieren"
        AppLanguage.RUSSIAN -> "Копировать вывод"
        AppLanguage.JAPANESE -> "出力をコピー"
        AppLanguage.KOREAN -> "출력 복사"
    }
    val agentMessageActionCopyAll: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "复制全部(含思考与工具)"
        AppLanguage.ENGLISH -> "Copy all (with thinking & tools)"
        AppLanguage.ARABIC -> "نسخ الكل (مع التفكير والأدوات)"
        AppLanguage.PORTUGUESE -> "Copiar tudo (com raciocínio e ferramentas)"
        AppLanguage.SPANISH -> "Copiar todo (con razonamiento y herramientas)"
        AppLanguage.FRENCH -> "Tout copier (avec réflexion et outils)"
        AppLanguage.GERMAN -> "Alles kopieren (mit Denken & Werkzeugen)"
        AppLanguage.RUSSIAN -> "Копировать всё (с размышлениями и инструментами)"
        AppLanguage.JAPANESE -> "すべてコピー(思考・ツール含む)"
        AppLanguage.KOREAN -> "전체 복사(사고 및 도구 포함)"
    }
    val agentMessageActionEdit: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "编辑并重发"
        AppLanguage.ENGLISH -> "Edit & resend"
        AppLanguage.ARABIC -> "تعديل وإعادة الإرسال"
        AppLanguage.PORTUGUESE -> "Editar e reenviar"
        AppLanguage.SPANISH -> "Editar y reenviar"
        AppLanguage.FRENCH -> "Modifier et renvoyer"
        AppLanguage.GERMAN -> "Bearbeiten und erneut senden"
        AppLanguage.RUSSIAN -> "Изменить и отправить снова"
        AppLanguage.JAPANESE -> "編集して再送信"
        AppLanguage.KOREAN -> "편집 후 재전송"
    }
    val agentMessageActionRegenerate: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "重新生成"
        AppLanguage.ENGLISH -> "Regenerate"
        AppLanguage.ARABIC -> "إعادة إنشاء"
        AppLanguage.PORTUGUESE -> "Regenerar"
        AppLanguage.SPANISH -> "Regenerar"
        AppLanguage.FRENCH -> "Régénérer"
        AppLanguage.GERMAN -> "Neu generieren"
        AppLanguage.RUSSIAN -> "Сгенерировать заново"
        AppLanguage.JAPANESE -> "再生成"
        AppLanguage.KOREAN -> "재생성"
    }
    val agentMessageActionDelete: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "删除以下对话"
        AppLanguage.ENGLISH -> "Delete from here"
        AppLanguage.ARABIC -> "حذف من هنا"
        AppLanguage.PORTUGUESE -> "Excluir a partir daqui"
        AppLanguage.SPANISH -> "Eliminar desde aquí"
        AppLanguage.FRENCH -> "Supprimer à partir d'ici"
        AppLanguage.GERMAN -> "Ab hier löschen"
        AppLanguage.RUSSIAN -> "Удалить отсюда"
        AppLanguage.JAPANESE -> "ここから削除"
        AppLanguage.KOREAN -> "이후부터 삭제"
    }
    val agentMessageCopied: String get() = when (Strings.lang) {
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
    val agentJumpToLatest: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "跳到最新"
        AppLanguage.ENGLISH -> "Jump to latest"
        AppLanguage.ARABIC -> "اقفز إلى الأحدث"
        AppLanguage.PORTUGUESE -> "Ir para o mais recente"
        AppLanguage.SPANISH -> "Ir al más reciente"
        AppLanguage.FRENCH -> "Aller au plus récent"
        AppLanguage.GERMAN -> "Zum neuesten springen"
        AppLanguage.RUSSIAN -> "Перейти к последнему"
        AppLanguage.JAPANESE -> "最新へジャンプ"
        AppLanguage.KOREAN -> "최신으로 이동"
    }
    val agentEditingHeader: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "编辑后将替换之后的对话"
        AppLanguage.ENGLISH -> "Editing — later messages will be replaced"
        AppLanguage.ARABIC -> "التعديل — سيتم استبدال الرسائل اللاحقة"
        AppLanguage.PORTUGUESE -> "Editando — mensagens posteriores serão substituídas"
        AppLanguage.SPANISH -> "Editando — los mensajes posteriores serán reemplazados"
        AppLanguage.FRENCH -> "Modification — les messages suivants seront remplacés"
        AppLanguage.GERMAN -> "Bearbeiten — spätere Nachrichten werden ersetzt"
        AppLanguage.RUSSIAN -> "Редактирование — последующие сообщения будут заменены"
        AppLanguage.JAPANESE -> "編集中 — 以降のメッセージは置き換えられます"
        AppLanguage.KOREAN -> "편집 중 — 이후 메시지가 대체됩니다"
    }
    val agentEditingCancel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "取消编辑"
        AppLanguage.ENGLISH -> "Cancel edit"
        AppLanguage.ARABIC -> "إلغاء التعديل"
        AppLanguage.PORTUGUESE -> "Cancelar edição"
        AppLanguage.SPANISH -> "Cancelar edición"
        AppLanguage.FRENCH -> "Annuler la modification"
        AppLanguage.GERMAN -> "Bearbeitung abbrechen"
        AppLanguage.RUSSIAN -> "Отменить редактирование"
        AppLanguage.JAPANESE -> "編集をキャンセル"
        AppLanguage.KOREAN -> "편집 취소"
    }
    val agentNewSessionAlreadyEmpty: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "当前已是空会话"
        AppLanguage.ENGLISH -> "Already in an empty session"
        AppLanguage.ARABIC -> "أنت بالفعل في جلسة فارغة"
        AppLanguage.PORTUGUESE -> "Já está em uma sessão vazia"
        AppLanguage.SPANISH -> "Ya estás en una sesión vacía"
        AppLanguage.FRENCH -> "Déjà dans une session vide"
        AppLanguage.GERMAN -> "Bereits in einer leeren Sitzung"
        AppLanguage.RUSSIAN -> "Уже в пустой сессии"
        AppLanguage.JAPANESE -> "すでに空のセッションです"
        AppLanguage.KOREAN -> "이미 빈 세션입니다"
    }
    val agentPreviewOpen: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "实时预览"
        AppLanguage.ENGLISH -> "Live preview"
        AppLanguage.ARABIC -> "معاينة مباشرة"
        AppLanguage.PORTUGUESE -> "Pré-visualização ao vivo"
        AppLanguage.SPANISH -> "Vista previa en vivo"
        AppLanguage.FRENCH -> "Aperçu en direct"
        AppLanguage.GERMAN -> "Live-Vorschau"
        AppLanguage.RUSSIAN -> "Живой предпросмотр"
        AppLanguage.JAPANESE -> "ライブプレビュー"
        AppLanguage.KOREAN -> "실시간 미리보기"
    }
    val agentPreviewClose: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "关闭预览"
        AppLanguage.ENGLISH -> "Close preview"
        AppLanguage.ARABIC -> "إغلاق المعاينة"
        AppLanguage.PORTUGUESE -> "Fechar pré-visualização"
        AppLanguage.SPANISH -> "Cerrar vista previa"
        AppLanguage.FRENCH -> "Fermer l'aperçu"
        AppLanguage.GERMAN -> "Vorschau schließen"
        AppLanguage.RUSSIAN -> "Закрыть предпросмотр"
        AppLanguage.JAPANESE -> "プレビューを閉じる"
        AppLanguage.KOREAN -> "미리보기 닫기"
    }
    val agentToolCallCount: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "%d 次工具调用"
        AppLanguage.ENGLISH -> "Tool calls · %d"
        AppLanguage.ARABIC -> "استدعاءات الأدوات · %d"
        AppLanguage.PORTUGUESE -> "Chamadas de ferramentas · %d"
        AppLanguage.SPANISH -> "Llamadas a herramientas · %d"
        AppLanguage.FRENCH -> "Appels d’outils · %d"
        AppLanguage.GERMAN -> "Werkzeugaufrufe · %d"
        AppLanguage.RUSSIAN -> "Вызовы инструментов · %d"
        AppLanguage.JAPANESE -> "ツール呼び出し · %d"
        AppLanguage.KOREAN -> "도구 호출 · %d"
    }
    val agentToolReadFile: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "读取文件"
        AppLanguage.ENGLISH -> "Read file"
        AppLanguage.ARABIC -> "قراءة ملف"
        AppLanguage.PORTUGUESE -> "Ler arquivo"
        AppLanguage.SPANISH -> "Leer archivo"
        AppLanguage.FRENCH -> "Lire un fichier"
        AppLanguage.GERMAN -> "Datei lesen"
        AppLanguage.RUSSIAN -> "Чтение файла"
        AppLanguage.JAPANESE -> "ファイルを読む"
        AppLanguage.KOREAN -> "파일 읽기"
    }
    val agentToolWriteFile: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "写入文件"
        AppLanguage.ENGLISH -> "Write file"
        AppLanguage.ARABIC -> "كتابة ملف"
        AppLanguage.PORTUGUESE -> "Gravar arquivo"
        AppLanguage.SPANISH -> "Escribir archivo"
        AppLanguage.FRENCH -> "Écrire un fichier"
        AppLanguage.GERMAN -> "Datei schreiben"
        AppLanguage.RUSSIAN -> "Запись файла"
        AppLanguage.JAPANESE -> "ファイルを書き込む"
        AppLanguage.KOREAN -> "파일 쓰기"
    }
    val agentToolEditFile: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "编辑文件"
        AppLanguage.ENGLISH -> "Edit file"
        AppLanguage.ARABIC -> "تحرير ملف"
        AppLanguage.PORTUGUESE -> "Editar arquivo"
        AppLanguage.SPANISH -> "Editar archivo"
        AppLanguage.FRENCH -> "Modifier un fichier"
        AppLanguage.GERMAN -> "Datei bearbeiten"
        AppLanguage.RUSSIAN -> "Изменение файла"
        AppLanguage.JAPANESE -> "ファイルを編集"
        AppLanguage.KOREAN -> "파일 편집"
    }
    val agentToolDeleteFile: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "删除文件"
        AppLanguage.ENGLISH -> "Delete file"
        AppLanguage.ARABIC -> "حذف ملف"
        AppLanguage.PORTUGUESE -> "Excluir arquivo"
        AppLanguage.SPANISH -> "Eliminar archivo"
        AppLanguage.FRENCH -> "Supprimer un fichier"
        AppLanguage.GERMAN -> "Datei löschen"
        AppLanguage.RUSSIAN -> "Удаление файла"
        AppLanguage.JAPANESE -> "ファイルを削除"
        AppLanguage.KOREAN -> "파일 삭제"
    }
    val agentToolFindFiles: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "查找文件"
        AppLanguage.ENGLISH -> "Find files"
        AppLanguage.ARABIC -> "العثور على ملفات"
        AppLanguage.PORTUGUESE -> "Encontrar arquivos"
        AppLanguage.SPANISH -> "Buscar archivos"
        AppLanguage.FRENCH -> "Rechercher des fichiers"
        AppLanguage.GERMAN -> "Dateien finden"
        AppLanguage.RUSSIAN -> "Поиск файлов"
        AppLanguage.JAPANESE -> "ファイルを探す"
        AppLanguage.KOREAN -> "파일 찾기"
    }
    val agentToolSearchContent: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "搜索内容"
        AppLanguage.ENGLISH -> "Search contents"
        AppLanguage.ARABIC -> "البحث في المحتوى"
        AppLanguage.PORTUGUESE -> "Pesquisar conteúdo"
        AppLanguage.SPANISH -> "Buscar contenido"
        AppLanguage.FRENCH -> "Rechercher dans le contenu"
        AppLanguage.GERMAN -> "Inhalte durchsuchen"
        AppLanguage.RUSSIAN -> "Поиск по содержимому"
        AppLanguage.JAPANESE -> "内容を検索"
        AppLanguage.KOREAN -> "내용 검색"
    }
    val agentToolListFiles: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "列出文件"
        AppLanguage.ENGLISH -> "List files"
        AppLanguage.ARABIC -> "عرض قائمة الملفات"
        AppLanguage.PORTUGUESE -> "Listar arquivos"
        AppLanguage.SPANISH -> "Listar archivos"
        AppLanguage.FRENCH -> "Lister les fichiers"
        AppLanguage.GERMAN -> "Dateien auflisten"
        AppLanguage.RUSSIAN -> "Список файлов"
        AppLanguage.JAPANESE -> "ファイルを一覧表示"
        AppLanguage.KOREAN -> "파일 목록 보기"
    }
    val agentToolSubmitPlan: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "提交计划审阅"
        AppLanguage.ENGLISH -> "Submit plan for review"
        AppLanguage.ARABIC -> "إرسال الخطة للمراجعة"
        AppLanguage.PORTUGUESE -> "Enviar plano para revisão"
        AppLanguage.SPANISH -> "Enviar plan a revisión"
        AppLanguage.FRENCH -> "Soumettre le plan à la validation"
        AppLanguage.GERMAN -> "Plan zur Prüfung einreichen"
        AppLanguage.RUSSIAN -> "Отправка плана на проверку"
        AppLanguage.JAPANESE -> "計画をレビューに提出"
        AppLanguage.KOREAN -> "계획 검토 요청"
    }
    val agentToolArgsLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "参数"
        AppLanguage.ENGLISH -> "Arguments"
        AppLanguage.ARABIC -> "المعطيات"
        AppLanguage.PORTUGUESE -> "Argumentos"
        AppLanguage.SPANISH -> "Argumentos"
        AppLanguage.FRENCH -> "Arguments"
        AppLanguage.GERMAN -> "Argumente"
        AppLanguage.RUSSIAN -> "Аргументы"
        AppLanguage.JAPANESE -> "引数"
        AppLanguage.KOREAN -> "인수"
    }
    val agentToolResultLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "结果"
        AppLanguage.ENGLISH -> "Result"
        AppLanguage.ARABIC -> "النتيجة"
        AppLanguage.PORTUGUESE -> "Resultado"
        AppLanguage.SPANISH -> "Resultado"
        AppLanguage.FRENCH -> "Résultat"
        AppLanguage.GERMAN -> "Ergebnis"
        AppLanguage.RUSSIAN -> "Результат"
        AppLanguage.JAPANESE -> "結果"
        AppLanguage.KOREAN -> "결과"
    }

    val agentCodeCopy: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "复制代码"
        AppLanguage.ENGLISH -> "Copy code"
        AppLanguage.ARABIC -> "نسخ الكود"
        AppLanguage.PORTUGUESE -> "Copiar código"
        AppLanguage.SPANISH -> "Copiar código"
        AppLanguage.FRENCH -> "Copier le code"
        AppLanguage.GERMAN -> "Code kopieren"
        AppLanguage.RUSSIAN -> "Копировать код"
        AppLanguage.JAPANESE -> "コードをコピー"
        AppLanguage.KOREAN -> "코드 복사"
    }
    val agentTodoListHeader: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "任务清单"
        AppLanguage.ENGLISH -> "Checklist"
        AppLanguage.ARABIC -> "قائمة المهام"
        AppLanguage.PORTUGUESE -> "Lista de verificação"
        AppLanguage.SPANISH -> "Lista de verificación"
        AppLanguage.FRENCH -> "Liste de vérification"
        AppLanguage.GERMAN -> "Checkliste"
        AppLanguage.RUSSIAN -> "Чек-лист"
        AppLanguage.JAPANESE -> "チェックリスト"
        AppLanguage.KOREAN -> "체크리스트"
    }
    val agentNoOutput: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "(无输出)"
        AppLanguage.ENGLISH -> "(no output)"
        AppLanguage.ARABIC -> "(لا يوجد ناتج)"
        AppLanguage.PORTUGUESE -> "(sem saída)"
        AppLanguage.SPANISH -> "(sin salida)"
        AppLanguage.FRENCH -> "(aucune sortie)"
        AppLanguage.GERMAN -> "(keine Ausgabe)"
        AppLanguage.RUSSIAN -> "(нет вывода)"
        AppLanguage.JAPANESE -> "(出力なし)"
        AppLanguage.KOREAN -> "(출력 없음)"
    }
    val agentEmptyResponse: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "模型返回了空响应（无文本、未调用工具）。若持续出现，请检查接口地址与模型名称是否匹配。"
        AppLanguage.ENGLISH -> "The model returned an empty response (no text, no tool calls). If this persists, check that the endpoint and model name match."
        AppLanguage.ARABIC -> "أعاد النموذج استجابة فارغة (لا نص ولا استدعاءات أدوات). إذا تكرر ذلك، تحقق من تطابق نقطة النهاية واسم النموذج."
        AppLanguage.PORTUGUESE -> "O modelo retornou uma resposta vazia (sem texto e sem chamadas de ferramentas). Se persistir, verifique se o endpoint e o nome do modelo correspondem."
        AppLanguage.SPANISH -> "El modelo devolvió una respuesta vacía (sin texto ni llamadas a herramientas). Si persiste, comprueba que el endpoint y el nombre del modelo coincidan."
        AppLanguage.FRENCH -> "Le modèle a renvoyé une réponse vide (aucun texte, aucun appel d'outil). Si cela persiste, vérifiez que l'endpoint et le nom du modèle correspondent."
        AppLanguage.GERMAN -> "Das Modell lieferte eine leere Antwort (kein Text, keine Tool-Aufrufe). Falls dies bestehen bleibt, prüfe, ob Endpunkt und Modellname zusammenpassen."
        AppLanguage.RUSSIAN -> "Модель вернула пустой ответ (ни текста, ни вызовов инструментов). Если это повторяется, проверьте соответствие адреса endpoint и имени модели."
        AppLanguage.JAPANESE -> "モデルが空の応答を返しました（テキストなし、ツール呼び出しなし）。続く場合は、エンドポイントとモデル名が一致しているか確認してください。"
        AppLanguage.KOREAN -> "모델이 빈 응답을 반환했습니다(텍스트 없음, 도구 호출 없음). 계속되면 엔드포인트와 모델 이름이 일치하는지 확인하세요."
    }
    val agentAbortedHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "[已中断]"
        AppLanguage.ENGLISH -> "[Interrupted]"
        AppLanguage.ARABIC -> "[تمت المقاطعة]"
        AppLanguage.PORTUGUESE -> "[Interrompido]"
        AppLanguage.SPANISH -> "[Interrumpido]"
        AppLanguage.FRENCH -> "[Interrompu]"
        AppLanguage.GERMAN -> "[Abgebrochen]"
        AppLanguage.RUSSIAN -> "[Прервано]"
        AppLanguage.JAPANESE -> "[中断されました]"
        AppLanguage.KOREAN -> "[중단됨]"
    }
    val agentMissingTextModel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "请先在 AI 设置中配置一个可编程模型"
        AppLanguage.ENGLISH -> "Configure a coding model in AI settings first"
        AppLanguage.ARABIC -> "قم بإعداد نموذج برمجة في إعدادات الذكاء الاصطناعي أولاً"
        AppLanguage.PORTUGUESE -> "Configure um modelo de codificação nas configurações de IA primeiro"
        AppLanguage.SPANISH -> "Configura un modelo de codificación en la configuración de IA primero"
        AppLanguage.FRENCH -> "Configurez d'abord un modèle de codification dans les paramètres d'IA"
        AppLanguage.GERMAN -> "Konfiguriere zuerst ein Codierungsmodell in den KI-Einstellungen"
        AppLanguage.RUSSIAN -> "Сначала настройте модель кодирования в настройках ИИ"
        AppLanguage.JAPANESE -> "まずAI設定でコーディングモデルを設定してください"
        AppLanguage.KOREAN -> "먼저 AI 설정에서 코딩 모델을 구성하세요"
    }
    val agentMissingApiKey: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "找不到所选模型的 API Key"
        AppLanguage.ENGLISH -> "API key for the selected model is missing"
        AppLanguage.ARABIC -> "مفتاح API للنموذج المحدد مفقود"
        AppLanguage.PORTUGUESE -> "A chave de API para o modelo selecionado está faltando"
        AppLanguage.SPANISH -> "Falta la clave de API para el modelo seleccionado"
        AppLanguage.FRENCH -> "La clé d'API pour le modèle sélectionné est manquante"
        AppLanguage.GERMAN -> "API-Schlüssel für das ausgewählte Modell fehlt"
        AppLanguage.RUSSIAN -> "Отсутствует API-ключ для выбранной модели"
        AppLanguage.JAPANESE -> "選択したモデルのAPIキーが見つかりません"
        AppLanguage.KOREAN -> "선택한 모델의 API 키가 없습니다"
    }
    val agentModelSwitched: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已切换到模型：%s"
        AppLanguage.ENGLISH -> "Switched to model: %s"
        AppLanguage.ARABIC -> "تم التبديل إلى النموذج: %s"
        AppLanguage.PORTUGUESE -> "Modelo trocado para: %s"
        AppLanguage.SPANISH -> "Modelo cambiado a: %s"
        AppLanguage.FRENCH -> "Modèle changé pour : %s"
        AppLanguage.GERMAN -> "Zu Modell gewechselt: %s"
        AppLanguage.RUSSIAN -> "Переключено на модель: %s"
        AppLanguage.JAPANESE -> "モデルを切り替えました: %s"
        AppLanguage.KOREAN -> "모델 전환됨: %s"
    }
    val agentModelPickerTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "选择模型"
        AppLanguage.ENGLISH -> "Select model"
        AppLanguage.ARABIC -> "اختر النموذج"
        AppLanguage.PORTUGUESE -> "Selecionar modelo"
        AppLanguage.SPANISH -> "Seleccionar modelo"
        AppLanguage.FRENCH -> "Sélectionner le modèle"
        AppLanguage.GERMAN -> "Modell auswählen"
        AppLanguage.RUSSIAN -> "Выбрать модель"
        AppLanguage.JAPANESE -> "モデルを選択"
        AppLanguage.KOREAN -> "모델 선택"
    }
    val agentModelChipLabel: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "模型"
        AppLanguage.ENGLISH -> "Model"
        AppLanguage.ARABIC -> "النموذج"
        AppLanguage.PORTUGUESE -> "Modelo"
        AppLanguage.SPANISH -> "Modelo"
        AppLanguage.FRENCH -> "Modèle"
        AppLanguage.GERMAN -> "Modell"
        AppLanguage.RUSSIAN -> "Модель"
        AppLanguage.JAPANESE -> "モデル"
        AppLanguage.KOREAN -> "모델"
    }
    val agentModelSearchHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "搜索模型…"
        AppLanguage.ENGLISH -> "Search models…"
        AppLanguage.ARABIC -> "البحث في النماذج…"
        AppLanguage.PORTUGUESE -> "Pesquisar modelos…"
        AppLanguage.SPANISH -> "Buscar modelos…"
        AppLanguage.FRENCH -> "Rechercher des modèles…"
        AppLanguage.GERMAN -> "Modelle suchen…"
        AppLanguage.RUSSIAN -> "Поиск моделей…"
        AppLanguage.JAPANESE -> "モデルを検索…"
        AppLanguage.KOREAN -> "모델 검색…"
    }
    val agentModelPickerProviders: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "供应商"
        AppLanguage.ENGLISH -> "Providers"
        AppLanguage.ARABIC -> "المزوّدون"
        AppLanguage.PORTUGUESE -> "Provedores"
        AppLanguage.SPANISH -> "Proveedores"
        AppLanguage.FRENCH -> "Fournisseurs"
        AppLanguage.GERMAN -> "Anbieter"
        AppLanguage.RUSSIAN -> "Провайдеры"
        AppLanguage.JAPANESE -> "プロバイダー"
        AppLanguage.KOREAN -> "제공자"
    }
    val agentModelPickerModels: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "模型"
        AppLanguage.ENGLISH -> "Models"
        AppLanguage.ARABIC -> "النماذج"
        AppLanguage.PORTUGUESE -> "Modelos"
        AppLanguage.SPANISH -> "Modelos"
        AppLanguage.FRENCH -> "Modèles"
        AppLanguage.GERMAN -> "Modelle"
        AppLanguage.RUSSIAN -> "Модели"
        AppLanguage.JAPANESE -> "モデル"
        AppLanguage.KOREAN -> "모델"
    }
    val agentServiceNotConnected: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "AI 服务未连接"
        AppLanguage.ENGLISH -> "AI service is not connected"
        AppLanguage.ARABIC -> "خدمة الذكاء الاصطناعي غير متصلة"
        AppLanguage.PORTUGUESE -> "Serviço de IA não está conectado"
        AppLanguage.SPANISH -> "El servicio de IA no está conectado"
        AppLanguage.FRENCH -> "Le service d'IA n'est pas connecté"
        AppLanguage.GERMAN -> "KI-Dienst ist nicht verbunden"
        AppLanguage.RUSSIAN -> "Сервис ИИ не подключён"
        AppLanguage.JAPANESE -> "AIサービスが接続されていません"
        AppLanguage.KOREAN -> "AI 서비스가 연결되지 않았습니다"
    }
    val agentTurnDoneToolCount: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "完成（%d 次工具调用）"
        AppLanguage.ENGLISH -> "Done (%d tool call(s))"
        AppLanguage.ARABIC -> "تم (%d استدعاء أداة)"
        AppLanguage.PORTUGUESE -> "Concluído (%d chamada(s) de ferramenta)"
        AppLanguage.SPANISH -> "Hecho (%d llamada(s) a herramienta)"
        AppLanguage.FRENCH -> "Terminé (%d appel(s) d'outil)"
        AppLanguage.GERMAN -> "Fertig (%d Werkzeugaufruf(e))"
        AppLanguage.RUSSIAN -> "Готово (%d вызов(ов) инструментов)"
        AppLanguage.JAPANESE -> "完了 (%d回のツール呼び出し)"
        AppLanguage.KOREAN -> "완료 (%d회 도구 호출)"
    }
    val agentPlanEntered: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已进入 Plan 模式"
        AppLanguage.ENGLISH -> "Entered plan mode"
        AppLanguage.ARABIC -> "تم الدخول إلى وضع التخطيط"
        AppLanguage.PORTUGUESE -> "Entrou no modo Plan"
        AppLanguage.SPANISH -> "Entró al modo Plan"
        AppLanguage.FRENCH -> "Entré en mode Plan"
        AppLanguage.GERMAN -> "Plan-Modus betreten"
        AppLanguage.RUSSIAN -> "Вошёл в режим Plan"
        AppLanguage.JAPANESE -> "Planモードに入りました"
        AppLanguage.KOREAN -> "Plan 모드에 진입했습니다"
    }
    val agentPlanAlreadyActive: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已经在 Plan 模式"
        AppLanguage.ENGLISH -> "Already in plan mode"
        AppLanguage.ARABIC -> "أنت بالفعل في وضع التخطيط"
        AppLanguage.PORTUGUESE -> "Já está no modo Plan"
        AppLanguage.SPANISH -> "Ya estás en modo Plan"
        AppLanguage.FRENCH -> "Déjà en mode Plan"
        AppLanguage.GERMAN -> "Bereits im Plan-Modus"
        AppLanguage.RUSSIAN -> "Уже в режиме Plan"
        AppLanguage.JAPANESE -> "すでにPlanモードです"
        AppLanguage.KOREAN -> "이미 Plan 모드입니다"
    }

    val agentPlanEmpty: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Plan 文件还是空的，先写完再退出"
        AppLanguage.ENGLISH -> "Plan file is empty — write the plan before exiting"
        AppLanguage.ARABIC -> "ملف الخطة فارغ — اكتب الخطة قبل الخروج"
        AppLanguage.PORTUGUESE -> "O arquivo do plano está vazio — escreva o plano antes de sair"
        AppLanguage.SPANISH -> "El archivo del plan está vacío — escribe el plan antes de salir"
        AppLanguage.FRENCH -> "Le fichier du plan est vide — écrivez le plan avant de quitter"
        AppLanguage.GERMAN -> "Plan-Datei ist leer — schreibe den Plan vor dem Verlassen"
        AppLanguage.RUSSIAN -> "Файл плана пуст — напишите план перед выходом"
        AppLanguage.JAPANESE -> "Planファイルが空です — 終了前にプランを書いてください"
        AppLanguage.KOREAN -> "Plan 파일이 비어 있습니다 — 종료 전에 계획을 작성하세요"
    }
    val agentPlanAwaitingReview: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Plan 已提交，等待审批"
        AppLanguage.ENGLISH -> "Plan submitted for review"
        AppLanguage.ARABIC -> "تم تقديم الخطة للمراجعة"
        AppLanguage.PORTUGUESE -> "Plano enviado para revisão"
        AppLanguage.SPANISH -> "Plan enviado para revisión"
        AppLanguage.FRENCH -> "Plan soumis pour examen"
        AppLanguage.GERMAN -> "Plan zur Prüfung eingereicht"
        AppLanguage.RUSSIAN -> "План отправлен на проверку"
        AppLanguage.JAPANESE -> "Plan がレビュー待ちで提出されました"
        AppLanguage.KOREAN -> "검토를 위해 Plan이 제출되었습니다"
    }
    val agentPlanApprove: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "批准并开始实现"
        AppLanguage.ENGLISH -> "Approve"
        AppLanguage.ARABIC -> "موافقة"
        AppLanguage.PORTUGUESE -> "Aprovar"
        AppLanguage.SPANISH -> "Aprobar"
        AppLanguage.FRENCH -> "Approuver"
        AppLanguage.GERMAN -> "Genehmigen"
        AppLanguage.RUSSIAN -> "Одобрить"
        AppLanguage.JAPANESE -> "承認"
        AppLanguage.KOREAN -> "승인"
    }
    val agentPlanRequestRevisions: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "需要修改"
        AppLanguage.ENGLISH -> "Request revisions"
        AppLanguage.ARABIC -> "طلب تعديلات"
        AppLanguage.PORTUGUESE -> "Solicitar revisões"
        AppLanguage.SPANISH -> "Solicitar revisiones"
        AppLanguage.FRENCH -> "Demander des révisions"
        AppLanguage.GERMAN -> "Änderungen anfordern"
        AppLanguage.RUSSIAN -> "Запросить правки"
        AppLanguage.JAPANESE -> "修正を依頼"
        AppLanguage.KOREAN -> "수정 요청"
    }
    val agentCompacting: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "压缩中…"
        AppLanguage.ENGLISH -> "Compacting…"
        AppLanguage.ARABIC -> "جارٍ الضغط…"
        AppLanguage.PORTUGUESE -> "Compactando…"
        AppLanguage.SPANISH -> "Compactando…"
        AppLanguage.FRENCH -> "Compactage…"
        AppLanguage.GERMAN -> "Komprimieren…"
        AppLanguage.RUSSIAN -> "Сжатие…"
        AppLanguage.JAPANESE -> "圧縮中…"
        AppLanguage.KOREAN -> "압축 중…"
    }
    val agentCompactedManual: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "对话已压缩（%d -> %d 条）"
        AppLanguage.ENGLISH -> "Compacted conversation (%d -> %d messages)"
        AppLanguage.ARABIC -> "تم ضغط المحادثة (%d -> %d رسالة)"
        AppLanguage.PORTUGUESE -> "Conversa compactada (%d -> %d mensagens)"
        AppLanguage.SPANISH -> "Conversación compactada (%d -> %d mensajes)"
        AppLanguage.FRENCH -> "Conversation compactée (%d -> %d messages)"
        AppLanguage.GERMAN -> "Konversation komprimiert (%d -> %d Nachrichten)"
        AppLanguage.RUSSIAN -> "Диалог сжат (%d -> %d сообщений)"
        AppLanguage.JAPANESE -> "会話を圧縮しました (%d -> %dメッセージ)"
        AppLanguage.KOREAN -> "대화 압축됨 (%d -> %d개 메시지)"
    }
    val agentCompactedAuto: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已自动压缩历史对话"
        AppLanguage.ENGLISH -> "Auto-compacted older messages"
        AppLanguage.ARABIC -> "تم ضغط الرسائل القديمة تلقائيًا"
        AppLanguage.PORTUGUESE -> "Mensagens antigas compactadas automaticamente"
        AppLanguage.SPANISH -> "Mensajes antiguos compactados automáticamente"
        AppLanguage.FRENCH -> "Anciens messages compactés automatiquement"
        AppLanguage.GERMAN -> "Ältere Nachrichten automatisch komprimiert"
        AppLanguage.RUSSIAN -> "Старые сообщения сжаты автоматически"
        AppLanguage.JAPANESE -> "古いメッセージを自動圧縮しました"
        AppLanguage.KOREAN -> "이전 메시지 자동 압축됨"
    }
    val agentCompactSkipped: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "未压缩：%s"
        AppLanguage.ENGLISH -> "Compact skipped: %s"
        AppLanguage.ARABIC -> "تم تخطي الضغط: %s"
        AppLanguage.PORTUGUESE -> "Compactação ignorada: %s"
        AppLanguage.SPANISH -> "Compactación omitida: %s"
        AppLanguage.FRENCH -> "Compactage ignoré : %s"
        AppLanguage.GERMAN -> "Komprimierung übersprungen: %s"
        AppLanguage.RUSSIAN -> "Сжатие пропущено: %s"
        AppLanguage.JAPANESE -> "圧縮をスキップ: %s"
        AppLanguage.KOREAN -> "압축 건너뜀: %s"
    }

    val agentContextOverflowCompacting: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "上下文超限，正在压缩历史并重试…"
        AppLanguage.ENGLISH -> "Context limit reached — compacting history and retrying…"
        AppLanguage.ARABIC -> "تم بلوغ حد السياق — جارٍ ضغط السجل وإعادة المحاولة…"
        AppLanguage.PORTUGUESE -> "Limite de contexto atingido — compactando histórico e tentando de novo…"
        AppLanguage.SPANISH -> "Límite de contexto alcanzado — compactando historial y reintentando…"
        AppLanguage.FRENCH -> "Limite de contexte atteinte — compactage de l'historique et nouvelle tentative…"
        AppLanguage.GERMAN -> "Kontextlimit erreicht — Verlauf wird komprimiert und erneut versucht…"
        AppLanguage.RUSSIAN -> "Достигнут лимит контекста — сжимаю историю и повторяю…"
        AppLanguage.JAPANESE -> "コンテキスト上限に達しました — 履歴を圧縮して再試行します…"
        AppLanguage.KOREAN -> "컨텍스트 한도 도달 — 기록을 압축하고 다시 시도합니다…"
    }

    val agentContextCapacity: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "上下文容量"
        AppLanguage.ENGLISH -> "Context Capacity"
        AppLanguage.ARABIC -> "سعة السياق"
        AppLanguage.PORTUGUESE -> "Capacidade de contexto"
        AppLanguage.SPANISH -> "Capacidad de contexto"
        AppLanguage.FRENCH -> "Capacité de contexte"
        AppLanguage.GERMAN -> "Kontextkapazität"
        AppLanguage.RUSSIAN -> "Ёмкость контекста"
        AppLanguage.JAPANESE -> "コンテキスト容量"
        AppLanguage.KOREAN -> "컨텍스트 용량"
    }

    val agentContextCapacityHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "模型支持的最大上下文 token 数（留空使用默认值 %d）"
        AppLanguage.ENGLISH -> "Max context tokens the model supports (leave empty for default %d)"
        AppLanguage.ARABIC -> "الحد الأقصى لرموز السياق التي يدعمها النموذج (اتركه فارغًا للافتراضي %d)"
        AppLanguage.PORTUGUESE -> "Máximo de tokens de contexto que o modelo suporta (deixe vazio para padrão %d)"
        AppLanguage.SPANISH -> "Máximo de tokens de contexto que soporta el modelo (dejar vacío para predeterminado %d)"
        AppLanguage.FRENCH -> "Maximum de tokens de contexte pris en charge par le modèle (laisser vide pour défaut %d)"
        AppLanguage.GERMAN -> "Maximale Kontext-Tokens, die das Modell unterstützt (leer für Standard %d)"
        AppLanguage.RUSSIAN -> "Макс. токенов контекста, поддерживаемых моделью (пусто = по умолчанию %d)"
        AppLanguage.JAPANESE -> "モデルがサポートする最大コンテキストトークン数 (空欄でデフォルト %d)"
        AppLanguage.KOREAN -> "모델이 지원하는 최대 컨텍스트 토큰 수 (빈 값은 기본값 %d)"
    }

    val agentCompactNow: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "压缩上下文"
        AppLanguage.ENGLISH -> "Compact Context"
        AppLanguage.ARABIC -> "ضغط السياق"
        AppLanguage.PORTUGUESE -> "Compactar contexto"
        AppLanguage.SPANISH -> "Compactar contexto"
        AppLanguage.FRENCH -> "Compacter le contexte"
        AppLanguage.GERMAN -> "Kontext komprimieren"
        AppLanguage.RUSSIAN -> "Сжать контекст"
        AppLanguage.JAPANESE -> "コンテキストを圧縮"
        AppLanguage.KOREAN -> "컨텍스트 압축"
    }

    val agentPermissionRequired: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "AI 想运行工具"
        AppLanguage.ENGLISH -> "AI wants to run a tool"
        AppLanguage.ARABIC -> "يريد الذكاء الاصطناعي تشغيل أداة"
        AppLanguage.PORTUGUESE -> "A IA quer executar uma ferramenta"
        AppLanguage.SPANISH -> "La IA quiere ejecutar una herramienta"
        AppLanguage.FRENCH -> "L'IA veut exécuter un outil"
        AppLanguage.GERMAN -> "KI möchte ein Werkzeug ausführen"
        AppLanguage.RUSSIAN -> "ИИ хочет запустить инструмент"
        AppLanguage.JAPANESE -> "AIがツールを実行しようとしています"
        AppLanguage.KOREAN -> "AI가 도구를 실행하려고 합니다"
    }
    val agentPermissionAlways: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "总是允许"
        AppLanguage.ENGLISH -> "Always allow"
        AppLanguage.ARABIC -> "السماح دائمًا"
        AppLanguage.PORTUGUESE -> "Sempre permitir"
        AppLanguage.SPANISH -> "Permitir siempre"
        AppLanguage.FRENCH -> "Toujours autoriser"
        AppLanguage.GERMAN -> "Immer erlauben"
        AppLanguage.RUSSIAN -> "Всегда разрешать"
        AppLanguage.JAPANESE -> "常に許可"
        AppLanguage.KOREAN -> "항상 허용"
    }
    val agentPermissionAllow: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "允许"
        AppLanguage.ENGLISH -> "Allow"
        AppLanguage.ARABIC -> "السماح"
        AppLanguage.PORTUGUESE -> "Permitir"
        AppLanguage.SPANISH -> "Permitir"
        AppLanguage.FRENCH -> "Autoriser"
        AppLanguage.GERMAN -> "Erlauben"
        AppLanguage.RUSSIAN -> "Разрешить"
        AppLanguage.JAPANESE -> "許可"
        AppLanguage.KOREAN -> "허용"
    }
    val agentPermissionDeny: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "拒绝"
        AppLanguage.ENGLISH -> "Deny"
        AppLanguage.ARABIC -> "رفض"
        AppLanguage.PORTUGUESE -> "Negar"
        AppLanguage.SPANISH -> "Denegar"
        AppLanguage.FRENCH -> "Refuser"
        AppLanguage.GERMAN -> "Ablehnen"
        AppLanguage.RUSSIAN -> "Отклонить"
        AppLanguage.JAPANESE -> "拒否"
        AppLanguage.KOREAN -> "거부"
    }
    val agentChoiceTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "AI 需要你来选择"
        AppLanguage.ENGLISH -> "AI needs your input"
        AppLanguage.ARABIC -> "يحتاج الذكاء الاصطناعي إلى مدخلاتك"
        AppLanguage.PORTUGUESE -> "A IA precisa de sua entrada"
        AppLanguage.SPANISH -> "La IA necesita tu entrada"
        AppLanguage.FRENCH -> "L'IA a besoin de votre entrée"
        AppLanguage.GERMAN -> "KI braucht deine Eingabe"
        AppLanguage.RUSSIAN -> "ИИ ждёт вашего ввода"
        AppLanguage.JAPANESE -> "AIがあなたの入力を必要としています"
        AppLanguage.KOREAN -> "AI가 사용자의 입력을 필요로 합니다"
    }
    val agentChoiceOtherHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "其他…（自由输入）"
        AppLanguage.ENGLISH -> "Other (free text)…"
        AppLanguage.ARABIC -> "أخرى (نص حر)…"
        AppLanguage.PORTUGUESE -> "Outro (texto livre)…"
        AppLanguage.SPANISH -> "Otro (texto libre)…"
        AppLanguage.FRENCH -> "Autre (texte libre)…"
        AppLanguage.GERMAN -> "Andere (Freitext)…"
        AppLanguage.RUSSIAN -> "Другое (свободный текст)…"
        AppLanguage.JAPANESE -> "その他 (自由入力)…"
        AppLanguage.KOREAN -> "기타 (자유 텍스트)…"
    }
    val agentActionSubmit: String get() = when (Strings.lang) {
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
    val agentActionCancel: String get() = when (Strings.lang) {
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
    val agentSendTooltip: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "发送"
        AppLanguage.ENGLISH -> "Send"
        AppLanguage.ARABIC -> "إرسال"
        AppLanguage.PORTUGUESE -> "Enviar"
        AppLanguage.SPANISH -> "Enviar"
        AppLanguage.FRENCH -> "Envoyer"
        AppLanguage.GERMAN -> "Senden"
        AppLanguage.RUSSIAN -> "Отправить"
        AppLanguage.JAPANESE -> "送信"
        AppLanguage.KOREAN -> "전송"
    }
    val agentStopTooltip: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "停止"
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
    val agentSlashClose: String get() = when (Strings.lang) {
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
    val agentEmptySessions: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "暂无会话"
        AppLanguage.ENGLISH -> "No sessions yet"
        AppLanguage.ARABIC -> "لا توجد جلسات بعد"
        AppLanguage.PORTUGUESE -> "Nenhuma sessão ainda"
        AppLanguage.SPANISH -> "Sin sesiones aún"
        AppLanguage.FRENCH -> "Pas encore de session"
        AppLanguage.GERMAN -> "Noch keine Sitzungen"
        AppLanguage.RUSSIAN -> "Пока нет сессий"
        AppLanguage.JAPANESE -> "セッションがまだありません"
        AppLanguage.KOREAN -> "세션이 아직 없습니다"
    }
    val agentEmptySessionsHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "新建一个会话开始"
        AppLanguage.ENGLISH -> "Create a new session to begin"
        AppLanguage.ARABIC -> "أنشئ جلسة جديدة للبدء"
        AppLanguage.PORTUGUESE -> "Crie uma nova sessão para começar"
        AppLanguage.SPANISH -> "Crea una nueva sesión para comenzar"
        AppLanguage.FRENCH -> "Créez une nouvelle session pour commencer"
        AppLanguage.GERMAN -> "Erstelle eine neue Sitzung zum Beginnen"
        AppLanguage.RUSSIAN -> "Создайте новую сессию, чтобы начать"
        AppLanguage.JAPANESE -> "新しいセッションを作成して開始"
        AppLanguage.KOREAN -> "새 세션을 만들어 시작하세요"
    }
    val agentEmptyFiles: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "项目还没有文件"
        AppLanguage.ENGLISH -> "No files yet"
        AppLanguage.ARABIC -> "لا توجد ملفات بعد"
        AppLanguage.PORTUGUESE -> "Nenhum arquivo ainda"
        AppLanguage.SPANISH -> "Sin archivos aún"
        AppLanguage.FRENCH -> "Pas encore de fichier"
        AppLanguage.GERMAN -> "Noch keine Dateien"
        AppLanguage.RUSSIAN -> "Пока нет файлов"
        AppLanguage.JAPANESE -> "ファイルがまだありません"
        AppLanguage.KOREAN -> "파일이 아직 없습니다"
    }
    val agentEmptyFilesHint: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "和 AI 聊天后会自动出现"
        AppLanguage.ENGLISH -> "Files appear after talking with the AI"
        AppLanguage.ARABIC -> "تظهر الملفات بعد التحدث مع الذكاء الاصطناعي"
        AppLanguage.PORTUGUESE -> "Os arquivos aparecem após conversar com a IA"
        AppLanguage.SPANISH -> "Los archivos aparecen después de hablar con la IA"
        AppLanguage.FRENCH -> "Les fichiers apparaissent après avoir discuté avec l'IA"
        AppLanguage.GERMAN -> "Dateien erscheinen nach dem Chat mit der KI"
        AppLanguage.RUSSIAN -> "Файлы появятся после общения с ИИ"
        AppLanguage.JAPANESE -> "AIと会話するとファイルが表示されます"
        AppLanguage.KOREAN -> "AI와 대화하면 파일이 나타납니다"
    }
    val agentBuiltApks: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "构建产物"
        AppLanguage.ENGLISH -> "Build Artifacts"
        AppLanguage.ARABIC -> "مخرجات البناء"
        AppLanguage.PORTUGUESE -> "Artefatos de Build"
        AppLanguage.SPANISH -> "Artefactos de compilación"
        AppLanguage.FRENCH -> "Artefacts de build"
        AppLanguage.GERMAN -> "Build-Artefakte"
        AppLanguage.RUSSIAN -> "Артефакты сборки"
        AppLanguage.JAPANESE -> "ビルド成果物"
        AppLanguage.KOREAN -> "빌드 산출물"
    }
    val agentApkInstall: String get() = when (Strings.lang) {
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
    val agentApkShare: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "分享"
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
    val agentApkPackage: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "包名"
        AppLanguage.ENGLISH -> "Package"
        AppLanguage.ARABIC -> "الحزمة"
        AppLanguage.PORTUGUESE -> "Pacote"
        AppLanguage.SPANISH -> "Paquete"
        AppLanguage.FRENCH -> "Package"
        AppLanguage.GERMAN -> "Paket"
        AppLanguage.RUSSIAN -> "Пакет"
        AppLanguage.JAPANESE -> "パッケージ"
        AppLanguage.KOREAN -> "패키지"
    }
    val agentApkVersion: String get() = when (Strings.lang) {
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
    val agentApkBuildMode: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "构建模式"
        AppLanguage.ENGLISH -> "Build mode"
        AppLanguage.ARABIC -> "وضع البناء"
        AppLanguage.PORTUGUESE -> "Modo de build"
        AppLanguage.SPANISH -> "Modo de compilación"
        AppLanguage.FRENCH -> "Mode de build"
        AppLanguage.GERMAN -> "Build-Modus"
        AppLanguage.RUSSIAN -> "Режим сборки"
        AppLanguage.JAPANESE -> "ビルドモード"
        AppLanguage.KOREAN -> "빌드 모드"
    }
    val agentApkSize: String get() = when (Strings.lang) {
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
    val agentNotifIdle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Agent 已就绪"
        AppLanguage.ENGLISH -> "Agent ready"
        AppLanguage.ARABIC -> "Agent جاهز"
        AppLanguage.PORTUGUESE -> "Agent pronto"
        AppLanguage.SPANISH -> "Agent listo"
        AppLanguage.FRENCH -> "Agent prêt"
        AppLanguage.GERMAN -> "Agent bereit"
        AppLanguage.RUSSIAN -> "Agent готов"
        AppLanguage.JAPANESE -> "Agentの準備ができました"
        AppLanguage.KOREAN -> "Agent 준비 완료"
    }
    val agentNotifRunning: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "Agent 进行中…"
        AppLanguage.ENGLISH -> "Agent in progress…"
        AppLanguage.ARABIC -> "Agent قيد التنفيذ…"
        AppLanguage.PORTUGUESE -> "Agent em andamento…"
        AppLanguage.SPANISH -> "Agent en progreso…"
        AppLanguage.FRENCH -> "Agent en cours…"
        AppLanguage.GERMAN -> "Agent läuft…"
        AppLanguage.RUSSIAN -> "Agent выполняется…"
        AppLanguage.JAPANESE -> "Agent進行中…"
        AppLanguage.KOREAN -> "Agent 진행 중…"
    }
    val agentOutputTruncated: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "本轮输出达到模型单次最大长度,内容可能被截断。让 AI 继续以补全剩余部分。"
        AppLanguage.ENGLISH -> "This turn hit the model's max output length, so the content may be truncated. Ask the AI to continue to finish the rest."
        AppLanguage.ARABIC -> "بلغ هذا الرد الحد الأقصى لطول مخرجات النموذج، لذا قد يكون المحتوى مقتطعًا. اطلب من الذكاء الاصطناعي المتابعة لإكمال الباقي."
        AppLanguage.PORTUGUESE -> "Este turno atingiu o comprimento máximo de saída do modelo, então o conteúdo pode estar truncado. Peça à IA para continuar e completar o restante."
        AppLanguage.SPANISH -> "Este turno alcanzó la longitud máxima de salida del modelo, por lo que el contenido puede estar truncado. Pide a la IA que continúe para terminar el resto."
        AppLanguage.FRENCH -> "Ce tour a atteint la longueur maximale de sortie du modèle, le contenu peut donc être tronqué. Demandez à l'IA de continuer pour terminer le reste."
        AppLanguage.GERMAN -> "Diese Runde erreichte die maximale Ausgabelänge des Modells, der Inhalt könnte abgeschnitten sein. Bitte die KI, fortzufahren, um den Rest zu vollenden."
        AppLanguage.RUSSIAN -> "Этот ход достиг максимальной длины вывода модели, содержимое может быть обрезано. Попросите ИИ продолжить, чтобы завершить остаток."
        AppLanguage.JAPANESE -> "このターンはモデルの最大出力長に達したため、コンテンツが切り捨てられている可能性があります。AIに続きを促して残りを完成させてください。"
        AppLanguage.KOREAN -> "이번 응답이 모델의 최대 출력 길이에 도달하여 내용이 잘렸을 수 있습니다. AI에게 계속하라고 요청하여 나머지를 완성하세요."
    }

    val agentContinuing: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "输出达到长度限制，正在自动续写…"
        AppLanguage.ENGLISH -> "Output hit length limit, auto-continuing…"
        AppLanguage.ARABIC -> "بلغ الإخراج حد الطول، يتابع تلقائيًا…"
        AppLanguage.PORTUGUESE -> "Saída atingiu o limite de comprimento, continuando automaticamente…"
        AppLanguage.SPANISH -> "La salida alcanzó el límite de longitud, continuando automáticamente…"
        AppLanguage.FRENCH -> "La sortie a atteint la limite de longueur, continuation automatique…"
        AppLanguage.GERMAN -> "Ausgabe erreichte Längenlimit, fahre automatisch fort…"
        AppLanguage.RUSSIAN -> "Вывод достиг лимита длины, автопродолжение…"
        AppLanguage.JAPANESE -> "出力が長さ制限に達しました、自動継続中…"
        AppLanguage.KOREAN -> "출력이 길이 제한에 도달했습니다, 자동 계속 중…"
    }

    val agentRateLimitRetry: (Int, Int, Long) -> String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> { attempt, max, ms -> "请求被限流，${ms / 1000}秒后重试（第${attempt}/${max}次）…" }
        AppLanguage.ENGLISH -> { attempt, max, ms -> "Rate limited, retrying in ${ms / 1000}s (attempt ${attempt}/${max})…" }
        AppLanguage.ARABIC -> { attempt, max, ms -> "تم تحديد الطلبات، إعادة المحاولة خلال ${ms / 1000}ث (المحاولة ${attempt}/${max})…" }
        AppLanguage.PORTUGUESE -> { attempt, max, ms -> "Limite de taxa, tentando novamente em ${ms / 1000}s (tentativa ${attempt}/${max})…" }
        AppLanguage.SPANISH -> { attempt, max, ms -> "Límite de tasa, reintentando en ${ms / 1000}s (intento ${attempt}/${max})…" }
        AppLanguage.FRENCH -> { attempt, max, ms -> "Limite de débit, nouvelle tentative dans ${ms / 1000}s (essai ${attempt}/${max})…" }
        AppLanguage.GERMAN -> { attempt, max, ms -> "Rate-Limit, Wiederholung in ${ms / 1000}s (Versuch ${attempt}/${max})…" }
        AppLanguage.RUSSIAN -> { attempt, max, ms -> "Ограничение скорости, повтор через ${ms / 1000}с (попытка ${attempt}/${max})…" }
        AppLanguage.JAPANESE -> { attempt, max, ms -> "レート制限、${ms / 1000}秒後にリトライ（${attempt}/${max}回目）…" }
        AppLanguage.KOREAN -> { attempt, max, ms -> "속도 제한, ${ms / 1000}초 후 재시도 (${attempt}/${max}번째)…" }
    }

    val agentKeySwitched: (String) -> String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> { masked -> "这把密钥被限额了，已换到 $masked" }
        AppLanguage.ENGLISH -> { masked -> "That key is rate-limited. Switched to $masked" }
        AppLanguage.ARABIC -> { masked -> "وصل هذا المفتاح إلى الحد. تم التبديل إلى $masked" }
        AppLanguage.PORTUGUESE -> { masked -> "Essa chave atingiu o limite. Trocada para $masked" }
        AppLanguage.SPANISH -> { masked -> "Esa clave llegó al límite. Cambiada a $masked" }
        AppLanguage.FRENCH -> { masked -> "Cette clé a atteint sa limite. Basculement vers $masked" }
        AppLanguage.GERMAN -> { masked -> "Dieser Schlüssel ist am Limit. Wechsel zu $masked" }
        AppLanguage.RUSSIAN -> { masked -> "Этот ключ упёрся в лимит. Переключились на $masked" }
        AppLanguage.JAPANESE -> { masked -> "このキーは制限中です。$masked に切り替えました" }
        AppLanguage.KOREAN -> { masked -> "이 키는 한도에 걸렸습니다. $masked 로 바꿨습니다" }
    }

    val agentKeysCooling: (Long) -> String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> { minutes -> "这组密钥都在冷却，大约 $minutes 分钟后可以再用" }
        AppLanguage.ENGLISH -> { minutes -> "Every key for this model is cooling down. The next one frees up in about $minutes min" }
        AppLanguage.ARABIC -> { minutes -> "كل المفاتيح لهذا النموذج في فترة تبريد. يتاح التالي بعد نحو $minutes د" }
        AppLanguage.PORTUGUESE -> { minutes -> "Todas as chaves deste modelo estão em pausa. A próxima libera em cerca de $minutes min" }
        AppLanguage.SPANISH -> { minutes -> "Todas las claves de este modelo están en pausa. La siguiente queda libre en unos $minutes min" }
        AppLanguage.FRENCH -> { minutes -> "Toutes les clés de ce modèle sont en pause. La prochaine se libère dans environ $minutes min" }
        AppLanguage.GERMAN -> { minutes -> "Alle Schlüssel für dieses Modell pausieren. Der nächste ist in etwa $minutes Min wieder frei" }
        AppLanguage.RUSSIAN -> { minutes -> "Все ключи этой модели на паузе. Следующий освободится примерно через $minutes мин" }
        AppLanguage.JAPANESE -> { minutes -> "このモデルのキーはすべて冷却中です。次に使えるまで約 $minutes 分" }
        AppLanguage.KOREAN -> { minutes -> "이 모델의 키가 모두 냉각 중입니다. 다음은 약 $minutes 분 후에 풀립니다" }
    }

    val agentNoReadyApiKey: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "这组密钥现在都不可用"
        AppLanguage.ENGLISH -> "None of the API keys for this model can be used right now"
        AppLanguage.ARABIC -> "لا يمكن استخدام أي مفتاح API لهذا النموذج الآن"
        AppLanguage.PORTUGUESE -> "Nenhuma chave de API deste modelo pode ser usada agora"
        AppLanguage.SPANISH -> "Ninguna clave de API de este modelo se puede usar ahora"
        AppLanguage.FRENCH -> "Aucune clé API de ce modèle n'est utilisable pour le moment"
        AppLanguage.GERMAN -> "Keiner der API-Schlüssel für dieses Modell ist gerade nutzbar"
        AppLanguage.RUSSIAN -> "Сейчас ни один ключ API этой модели недоступен"
        AppLanguage.JAPANESE -> "このモデルの API キーは今どれも使えません"
        AppLanguage.KOREAN -> "이 모델의 API 키는 지금 하나도 쓸 수 없습니다"
    }

    val agentFileSaved: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "已保存 %s"
        AppLanguage.ENGLISH -> "Saved %s"
        AppLanguage.ARABIC -> "تم حفظ %s"
        AppLanguage.PORTUGUESE -> "%s salvo"
        AppLanguage.SPANISH -> "%s guardado"
        AppLanguage.FRENCH -> "%s enregistré"
        AppLanguage.GERMAN -> "%s gespeichert"
        AppLanguage.RUSSIAN -> "%s сохранён"
        AppLanguage.JAPANESE -> "%sを保存しました"
        AppLanguage.KOREAN -> "%s 저장됨"
    }
    val agentFileOpen: String get() = when (Strings.lang) {
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
    val agentFileCopyPath: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "复制路径"
        AppLanguage.ENGLISH -> "Copy path"
        AppLanguage.ARABIC -> "نسخ المسار"
        AppLanguage.PORTUGUESE -> "Copiar caminho"
        AppLanguage.SPANISH -> "Copiar ruta"
        AppLanguage.FRENCH -> "Copier le chemin"
        AppLanguage.GERMAN -> "Pfad kopieren"
        AppLanguage.RUSSIAN -> "Копировать путь"
        AppLanguage.JAPANESE -> "パスをコピー"
        AppLanguage.KOREAN -> "경로 복사"
    }
    val agentPathCopied: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "路径已复制"
        AppLanguage.ENGLISH -> "Path copied"
        AppLanguage.ARABIC -> "تم نسخ المسار"
        AppLanguage.PORTUGUESE -> "Caminho copiado"
        AppLanguage.SPANISH -> "Ruta copiada"
        AppLanguage.FRENCH -> "Chemin copié"
        AppLanguage.GERMAN -> "Pfad kopiert"
        AppLanguage.RUSSIAN -> "Путь скопирован"
        AppLanguage.JAPANESE -> "パスをコピーしました"
        AppLanguage.KOREAN -> "경로가 복사됨"
    }
    val agentFileOpenWith: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "打开方式…"
        AppLanguage.ENGLISH -> "Open with…"
        AppLanguage.ARABIC -> "فتح باستخدام…"
        AppLanguage.PORTUGUESE -> "Abrir com…"
        AppLanguage.SPANISH -> "Abrir con…"
        AppLanguage.FRENCH -> "Ouvrir avec…"
        AppLanguage.GERMAN -> "Öffnen mit…"
        AppLanguage.RUSSIAN -> "Открыть с помощью…"
        AppLanguage.JAPANESE -> "別のアプリで開く…"
        AppLanguage.KOREAN -> "다른 앱으로 열기…"
    }
    val agentFileNotFound: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "文件不存在"
        AppLanguage.ENGLISH -> "File not found"
        AppLanguage.ARABIC -> "الملف غير موجود"
        AppLanguage.PORTUGUESE -> "Arquivo não encontrado"
        AppLanguage.SPANISH -> "Archivo no encontrado"
        AppLanguage.FRENCH -> "Fichier introuvable"
        AppLanguage.GERMAN -> "Datei nicht gefunden"
        AppLanguage.RUSSIAN -> "Файл не найден"
        AppLanguage.JAPANESE -> "ファイルが見つかりません"
        AppLanguage.KOREAN -> "파일을 찾을 수 없습니다"
    }
    val agentFileOpenFailed: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "无法打开此文件"
        AppLanguage.ENGLISH -> "Cannot open this file"
        AppLanguage.ARABIC -> "تعذر فتح هذا الملف"
        AppLanguage.PORTUGUESE -> "Não é possível abrir este arquivo"
        AppLanguage.SPANISH -> "No se puede abrir este archivo"
        AppLanguage.FRENCH -> "Impossible d'ouvrir ce fichier"
        AppLanguage.GERMAN -> "Diese Datei kann nicht geöffnet werden"
        AppLanguage.RUSSIAN -> "Невозможно открыть этот файл"
        AppLanguage.JAPANESE -> "このファイルを開けません"
        AppLanguage.KOREAN -> "이 파일을 열 수 없습니다"
    }
    val agentFileBinaryPreview: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "二进制文件（%s），无文本预览"
        AppLanguage.ENGLISH -> "Binary file (%s) — no text preview"
        AppLanguage.ARABIC -> "ملف ثنائي (%s) — لا معاينة نصية"
        AppLanguage.PORTUGUESE -> "Arquivo binário (%s) — sem pré-visualização de texto"
        AppLanguage.SPANISH -> "Archivo binario (%s) — sin vista previa de texto"
        AppLanguage.FRENCH -> "Fichier binaire (%s) — pas d'aperçu texte"
        AppLanguage.GERMAN -> "Binärdatei (%s) — keine Textvorschau"
        AppLanguage.RUSSIAN -> "Двоичный файл (%s) — текстовый предпросмотр недоступен"
        AppLanguage.JAPANESE -> "バイナリファイル（%s）— テキストプレビューなし"
        AppLanguage.KOREAN -> "바이너리 파일(%s) — 텍스트 미리보기 없음"
    }

    val lrcGenerationDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "AI 生成 LRC 歌词文件"
        AppLanguage.ENGLISH -> "AI-generated LRC lyrics files"
        AppLanguage.ARABIC -> "ملفات كلمات LRC المُنشأة بالذكاء الاصطناعي"
        AppLanguage.PORTUGUESE -> "Arquivos de letras LRC gerados por IA"
        AppLanguage.SPANISH -> "Archivos de letras LRC generados por IA"
        AppLanguage.FRENCH -> "Fichiers de paroles LRC générés par IA"
        AppLanguage.GERMAN -> "KI-generierte LRC-Textdateien"
        AppLanguage.RUSSIAN -> "AI-сгенерированные файлы текстов LRC"
        AppLanguage.JAPANESE -> "AI生成のLRC歌詞ファイル"
        AppLanguage.KOREAN -> "AI 생성 LRC 가사 파일"
    }

    val translationDesc: String get() = when (Strings.lang) {
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

    val generalChatDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "通用 AI 对话功能"
        AppLanguage.ENGLISH -> "General AI chat functionality"
        AppLanguage.ARABIC -> "وظيفة الدردشة العامة بالذكاء الاصطناعي"
        AppLanguage.PORTUGUESE -> "Funcionalidade geral de chat por IA"
        AppLanguage.SPANISH -> "Funcionalidad general de chat por IA"
        AppLanguage.FRENCH -> "Fonctionnalité générale de chat IA"
        AppLanguage.GERMAN -> "Allgemeine KI-Chat-Funktionalität"
        AppLanguage.RUSSIAN -> "Общий функционал ИИ-чата"
        AppLanguage.JAPANESE -> "汎用AIチャット機能"
        AppLanguage.KOREAN -> "일반 AI 채팅 기능"
    }

    val dataBackupTitle: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "数据备份"
        AppLanguage.ENGLISH -> "Data Backup"
        AppLanguage.ARABIC -> "نسخ البيانات احتياطيًا"
        AppLanguage.PORTUGUESE -> "Backup de dados"
        AppLanguage.SPANISH -> "Copia de seguridad de datos"
        AppLanguage.FRENCH -> "Sauvegarde de données"
        AppLanguage.GERMAN -> "Datensicherung"
        AppLanguage.RUSSIAN -> "Резервное копирование данных"
        AppLanguage.JAPANESE -> "データバックアップ"
        AppLanguage.KOREAN -> "데이터 백업"
    }

    val dataBackupDesc: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "导出或导入所有应用数据，包括配置、图标、启动画面、BGM等资源文件"
        AppLanguage.ENGLISH -> "Export or import all app data including config, icons, splash screens, BGM and other resources"
        AppLanguage.ARABIC -> "تصدير أو استيراد جميع بيانات التطبيق"
        AppLanguage.PORTUGUESE -> "Exportar ou importar todos os dados do app, incluindo config, ícones, telas splash, BGM e outros recursos"
        AppLanguage.SPANISH -> "Exportar o importar todos los datos de la app, incluyendo config, iconos, pantallas de inicio, BGM y otros recursos"
        AppLanguage.FRENCH -> "Exporter ou importer toutes les données de l'app, y compris config, icônes, écrans de démarrage, BGM et autres ressources"
        AppLanguage.GERMAN -> "Alle App-Daten exportieren oder importieren, inkl. Konfiguration, Icons, Startbildschirme, BGM und andere Ressourcen"
        AppLanguage.RUSSIAN -> "Экспорт или импорт всех данных приложения, включая конфигурацию, иконки, заставки, BGM и другие ресурсы"
        AppLanguage.JAPANESE -> "設定、アイコン、スプラッシュ画面、BGMなどのリソースを含むすべてのアプリデータをエクスポート/インポート"
        AppLanguage.KOREAN -> "설정, 아이콘, 스플래시 화면, BGM 등 리소스를 포함한 모든 앱 데이터 내보내기/가져오기"
    }

    val dataBackupUninstallWarning: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "卸载 WebToApp 会永久删除所有项目与源码，本应用未开启系统备份。请定期导出备份并妥善保存。"
        AppLanguage.ENGLISH -> "Uninstalling WebToApp permanently deletes every project and its source files — system backup is disabled. Export a backup regularly and keep it somewhere safe."
        AppLanguage.ARABIC -> "إلغاء تثبيت WebToApp يحذف جميع المشاريع وملفاتها المصدرية نهائياً، والنسخ الاحتياطي للنظام معطّل. صدّر نسخة احتياطية بانتظام واحتفظ بها في مكان آمن."
        AppLanguage.PORTUGUESE -> "Desinstalar o WebToApp exclui permanentemente todos os projetos e seus arquivos de origem, pois o backup do sistema está desativado. Exporte um backup regularmente e guarde-o em local seguro."
        AppLanguage.SPANISH -> "Desinstalar WebToApp borra permanentemente todos los proyectos y sus archivos de código, ya que la copia de seguridad del sistema está desactivada. Exporta una copia con regularidad y guárdala en un lugar seguro."
        AppLanguage.FRENCH -> "Désinstaller WebToApp supprime définitivement tous les projets et leurs fichiers source, la sauvegarde système étant désactivée. Exportez régulièrement une sauvegarde et conservez-la en lieu sûr."
        AppLanguage.GERMAN -> "Beim Deinstallieren von WebToApp werden alle Projekte und ihre Quelldateien dauerhaft gelöscht, da die Systemsicherung deaktiviert ist. Exportiere regelmäßig ein Backup und bewahre es sicher auf."
        AppLanguage.RUSSIAN -> "Удаление WebToApp безвозвратно удаляет все проекты и их исходники, поскольку системное резервирование отключено. Регулярно экспортируйте резервную копию и храните её в надёжном месте."
        AppLanguage.JAPANESE -> "WebToApp をアンインストールすると、すべてのプロジェクトとソースが完全に削除されます。システムバックアップは無効です。定期的にバックアップを書き出して安全な場所に保管してください。"
        AppLanguage.KOREAN -> "WebToApp 을 제거하면 모든 프로젝트와 소스가 영구 삭제됩니다. 시스템 백업이 꺼져 있으므로 정기적으로 백업을 내보내 안전한 곳에 보관하세요."
    }

    val dataBackupNote: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "导入数据会添加新应用，不会覆盖现有数据。建议在更新应用前先导出备份。"
        AppLanguage.ENGLISH -> "Importing data adds new apps without overwriting existing data. It's recommended to export backup before updating."
        AppLanguage.ARABIC -> "استيراد البيانات يضيف تطبيقات جديدة دون الكتابة فوق البيانات الموجودة."
        AppLanguage.PORTUGUESE -> "Importar dados adiciona novos apps sem sobrescrever os existentes. Recomenda-se exportar o backup antes de atualizar."
        AppLanguage.SPANISH -> "Importar datos añade nuevas apps sin sobrescribir las existentes. Se recomienda exportar la copia de seguridad antes de actualizar."
        AppLanguage.FRENCH -> "L'import des données ajoute de nouvelles apps sans écraser les existantes. Il est recommandé d'exporter une sauvegarde avant la mise à jour."
        AppLanguage.GERMAN -> "Datenimport fügt neue Apps hinzu, ohne bestehende zu überschreiben. Vor dem Update wird ein Backup-Export empfohlen."
        AppLanguage.RUSSIAN -> "Импорт данных добавляет новые приложения, не перезаписывая существующие. Перед обновлением рекомендуется экспортировать резервную копию."
        AppLanguage.JAPANESE -> "データのインポートは新しいアプリを追加し、既存のデータを上書きしません。更新前にバックアップをエクスポートすることをお勧めします。"
        AppLanguage.KOREAN -> "데이터 가져오기는 기존 데이터를 덮어쓰지 않고 새 앱을 추가합니다. 업데이트 전 백업을 내보내는 것이 좋습니다."
    }

    val legalDisclaimer: String get() = when (Strings.lang) {
        AppLanguage.CHINESE -> "法律声明与免责条款"
        AppLanguage.ENGLISH -> "Legal Disclaimer"
        AppLanguage.ARABIC -> "إخلاء المسؤولية القانونية"
        AppLanguage.PORTUGUESE -> "Aviso legal"
        AppLanguage.SPANISH -> "Aviso legal"
        AppLanguage.FRENCH -> "Mentions légales"
        AppLanguage.GERMAN -> "Rechtlicher Hinweis"
        AppLanguage.RUSSIAN -> "Юридическое уведомление"
        AppLanguage.JAPANESE -> "法的免責事項"
        AppLanguage.KOREAN -> "법적 고지 사항"
    }

}
