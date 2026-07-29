import { defineConfig } from 'vitepress'

// GitHub Pages serves this under /web-to-app/. Override with DOCS_BASE for a
// custom domain (e.g. DOCS_BASE=/ npm run build).
const base = process.env.DOCS_BASE || '/web-to-app/'

type Lang = 'en' | 'zh'

const t = {
  en: {
    nav: { guide: 'Guide', developer: 'Developer', extensions: 'Extensions' },
    cc: {
      title: 'Common Config',
      basicInfo: 'Basic Info',
      browserUi: 'Browser & Interface',
      mediaInteraction: 'Media & Interaction',
      extensionsNetwork: 'Extensions & Network',
      disguise: 'Disguise',
      launchRuntime: 'Launch & Runtime',
      advancedExport: 'Advanced & Export',
      appIcon: 'App Icon',
      appName: 'App Name',
      urlWebpage: 'URL / Webpage',
      activation: 'Activation Code',
      hideToolbar: 'Hide Browser Toolbar',
      fullscreen: 'Fullscreen Mode',
      orientation: 'Screen Orientation',
      keepScreenOn: 'Keep Screen On',
      floatingWindow: 'Floating Window',
      longPressMenu: 'Long-press Menu',
      splash: 'Splash Animation',
      bgm: 'Background Music',
      announcement: 'Popup Announcement',
      translate: 'Auto Translation',
      extensionModules: 'Extension Modules',
      adBlocking: 'Ad Blocking',
      customDns: 'Custom DNS',
      iconDisguise: 'Icon & App',
      deviceDisguise: 'Device Disguise',
      autoStart: 'Auto-start',
      forcedRun: 'Forced Run',
      blacktech: 'BlackTech',
      advancedSettings: 'Advanced Settings',
      specialSettings: 'Special Settings',
      apkExport: 'APK Export Config'
    },
    guide: {
      start: 'Start',
      intro: 'Introduction',
      gettingStarted: 'Getting Started',
      mainScreen: 'Main Screen',
      myApps: 'My Apps',
      darkLight: 'Dark / Light',
      language: 'Language',
      search: 'Search',
      more: 'More Menu',
      categories: 'Categories',
      appList: 'App List',
      createButton: 'Create Button',
      create: 'Create App',
      typesOverview: 'Overview',
      web: 'Web',
      multiWeb: 'Multi-Web',
      html: 'HTML',
      offlinePack: 'Offline Pack',
      frontend: 'Frontend',
      php: 'PHP',
      wordpress: 'WordPress',
      nodejs: 'Node.js',
      python: 'Python',
      go: 'Go',
      media: 'Media',
      gallery: 'Gallery',
      appActions: 'App Actions',
      editCore: 'Edit Core Config',
      editCommon: 'Edit Common Config',
      createShortcut: 'Create Shortcut',
      buildApk: 'Build APK',
      shareApk: 'Share APK',
      exportApk: 'Export',
      moveToCategory: 'Move to Category',
      delete: 'Delete',
      moreFeatures: 'More Features',
      aiCoding: 'AI Coding',
      aiSettings: 'AI Settings',
      extensionModules: 'Extension Modules',
      appModifier: 'App Modifier',
      linuxEnvironment: 'Linux Environment',
      runtimeManagement: 'Runtime Management',
      portManager: 'Port Manager',
      browserKernel: 'Browser Kernel',
      hostsAdblock: 'Hosts Ad Blocking',
      usageStats: 'Usage Stats',
      googlePlay: 'Google Play',
      fileManager: 'File Manager',
      batchImport: 'Batch Import',
      about: 'About',
      config: 'App Configuration',
      configOverview: 'Overview',
      network: 'Network',
      privacy: 'Privacy',
      appearance: 'Appearance',
      runtimes: 'Runtimes',
      faq: 'FAQ'
    },
    dev: {
      title: 'Developer Docs',
      overview: 'Overview',
      architecture: 'Architecture',
      exportPipeline: 'Export Pipeline',
      shellSync: 'Shell Sync & Template',
      configDrift: 'Config Field Drift',
      i18n: 'Internationalization',
      recipes: 'Change Recipes',
      contributing: 'Contributing'
    },
    ext: {
      title: 'Extension Authoring',
      overview: 'Overview',
      jsModule: 'JS Modules',
      cssModule: 'CSS Modules',
      userscript: 'Userscripts',
      chromeMv3: 'Chrome MV3 Extensions',
      apiRef: 'API Reference',
      publish: 'Publish to the Market'
    }
  },
  zh: {
    nav: { guide: '使用手册', developer: '开发者', extensions: '扩展开发' },
    cc: {
      title: '通用配置',
      basicInfo: '基本信息',
      browserUi: '浏览器与界面',
      mediaInteraction: '媒体与互动',
      extensionsNetwork: '扩展与网络',
      disguise: '伪装',
      launchRuntime: '启动与运行',
      advancedExport: '高级与导出',
      appIcon: '应用图标',
      appName: '应用名称',
      urlWebpage: '网址/网页',
      activation: '激活码验证',
      hideToolbar: '隐藏浏览器工具栏',
      fullscreen: '全屏模式',
      orientation: '屏幕方向',
      keepScreenOn: '保持屏幕常亮',
      floatingWindow: '悬浮小窗',
      longPressMenu: '长按菜单',
      splash: '启动动画',
      bgm: '背景音乐',
      announcement: '弹窗公告',
      translate: '网页自动翻译',
      extensionModules: '拓展模块',
      adBlocking: '广告拦截',
      customDns: '自定义DNS',
      iconDisguise: '图标与应用',
      deviceDisguise: '设备伪装',
      autoStart: '自启动设置',
      forcedRun: '强制运行设置',
      blacktech: '黑科技功能',
      advancedSettings: '高级设置',
      specialSettings: '特殊设置',
      apkExport: 'APK导出配置'
    },
    guide: {
      start: '开始',
      intro: '简介',
      gettingStarted: '快速开始',
      mainScreen: '主界面',
      myApps: '我的应用',
      darkLight: '暗亮色',
      language: '语言',
      search: '搜索',
      more: '更多菜单',
      categories: '应用分类',
      appList: '应用列表',
      createButton: '创建按钮',
      create: '创建应用',
      typesOverview: '总览',
      web: '网页',
      multiWeb: '多站点',
      html: 'HTML',
      offlinePack: '离线包',
      frontend: '前端',
      php: 'PHP',
      wordpress: 'WordPress',
      nodejs: 'Node.js',
      python: 'Python',
      go: 'Go',
      media: '媒体',
      gallery: '画廊',
      appActions: '应用功能',
      editCore: '编辑核心配置',
      editCommon: '编辑通用配置',
      createShortcut: '创建快捷方式',
      buildApk: '构建 APK',
      shareApk: '分享 APK',
      exportApk: '导出',
      moveToCategory: '移动到分类',
      delete: '删除',
      moreFeatures: '更多功能',
      aiCoding: 'AI 编程',
      aiSettings: 'AI 设置',
      extensionModules: '扩展模块',
      appModifier: '应用修改器',
      linuxEnvironment: 'Linux 环境',
      runtimeManagement: '运行时管理',
      portManager: '端口管理',
      browserKernel: '浏览器内核',
      hostsAdblock: 'Hosts 拦截',
      usageStats: '使用统计',
      googlePlay: 'Google Play',
      fileManager: '文件管理',
      batchImport: '批量导入',
      about: '关于',
      config: '应用配置',
      configOverview: '总览',
      network: '网络',
      privacy: '隐私',
      appearance: '外观',
      runtimes: '运行时',
      faq: '常见问题'
    },
    dev: {
      title: '开发者文档',
      overview: '总览',
      architecture: '架构',
      exportPipeline: '导出管线',
      shellSync: 'Shell 同步与模板',
      configDrift: '配置字段漂移',
      i18n: '国际化',
      recipes: '常见改动配方',
      contributing: '贡献指南'
    },
    ext: {
      title: '扩展开发',
      overview: '总览',
      jsModule: 'JS 模块',
      cssModule: 'CSS 模块',
      userscript: '油猴脚本',
      chromeMv3: 'Chrome MV3 扩展',
      apiRef: 'API 参考',
      publish: '发布到市场'
    }
  }
} as const

function nav(lang: Lang, prefix: string) {
  const s = t[lang]
  return [
    { text: s.nav.guide, link: `${prefix}/guide/introduction` },
    { text: s.nav.developer, link: `${prefix}/developer/` },
    { text: s.nav.extensions, link: `${prefix}/extensions/` }
  ]
}

function sidebar(lang: Lang, prefix: string) {
  const s = t[lang]
  return {
    [`${prefix}/guide/`]: [
      {
        text: s.guide.start,
        items: [
          { text: s.guide.intro, link: `${prefix}/guide/introduction` },
          { text: s.guide.gettingStarted, link: `${prefix}/guide/getting-started` }
        ]
      },
      {
        text: s.guide.mainScreen,
        items: [
          { text: s.guide.myApps, link: `${prefix}/guide/main-screen/my-apps` },
          { text: s.guide.darkLight, link: `${prefix}/guide/main-screen/dark-light` },
          { text: s.guide.language, link: `${prefix}/guide/main-screen/language` },
          { text: s.guide.search, link: `${prefix}/guide/main-screen/search` },
          { text: s.guide.more, link: `${prefix}/guide/main-screen/more` },
          { text: s.guide.categories, link: `${prefix}/guide/main-screen/categories` },
          { text: s.guide.appList, link: `${prefix}/guide/main-screen/app-list` },
          { text: s.guide.createButton, link: `${prefix}/guide/main-screen/create-app` }
        ]
      },
      {
        text: s.guide.create,
        items: [
          { text: s.guide.typesOverview, link: `${prefix}/guide/app-types/` },
          { text: s.guide.web, link: `${prefix}/guide/app-types/web` },
          { text: s.guide.multiWeb, link: `${prefix}/guide/app-types/multi-web` },
          { text: s.guide.html, link: `${prefix}/guide/app-types/html` },
          { text: s.guide.offlinePack, link: `${prefix}/guide/app-types/offline-pack` },
          { text: s.guide.frontend, link: `${prefix}/guide/app-types/frontend` },
          { text: s.guide.php, link: `${prefix}/guide/app-types/php` },
          { text: s.guide.wordpress, link: `${prefix}/guide/app-types/wordpress` },
          { text: s.guide.nodejs, link: `${prefix}/guide/app-types/nodejs` },
          { text: s.guide.python, link: `${prefix}/guide/app-types/python` },
          { text: s.guide.go, link: `${prefix}/guide/app-types/go` },
          { text: s.guide.media, link: `${prefix}/guide/app-types/media` },
          { text: s.guide.gallery, link: `${prefix}/guide/app-types/gallery` }
        ]
      },
      {
        text: s.guide.appActions,
        items: [
          { text: s.guide.editCore, link: `${prefix}/guide/app-actions/edit-core-config` },
          { text: s.guide.editCommon, link: `${prefix}/guide/app-actions/edit-common-config` },
          { text: s.guide.createShortcut, link: `${prefix}/guide/app-actions/create-shortcut` },
          { text: s.guide.buildApk, link: `${prefix}/guide/app-actions/build-apk` },
          { text: s.guide.shareApk, link: `${prefix}/guide/app-actions/share-apk` },
          { text: s.guide.exportApk, link: `${prefix}/guide/app-actions/export-apk` },
          { text: s.guide.moveToCategory, link: `${prefix}/guide/app-actions/move-to-category` },
          { text: s.guide.delete, link: `${prefix}/guide/app-actions/delete` }
        ]
      },
      {
        text: s.cc.title,
        items: [
          {
            text: s.cc.basicInfo,
            collapsed: false,
            items: [
              { text: s.cc.appIcon, link: `${prefix}/guide/app-actions/edit-common-config/app-icon` },
              { text: s.cc.appName, link: `${prefix}/guide/app-actions/edit-common-config/app-name` },
              { text: s.cc.urlWebpage, link: `${prefix}/guide/app-actions/edit-common-config/url-webpage` }
            ]
          },
          {
            text: s.cc.browserUi,
            collapsed: false,
            items: [
              { text: s.cc.hideToolbar, link: `${prefix}/guide/app-actions/edit-common-config/hide-toolbar` },
              { text: s.cc.fullscreen, link: `${prefix}/guide/app-actions/edit-common-config/fullscreen` },
              { text: s.cc.orientation, link: `${prefix}/guide/app-actions/edit-common-config/orientation` },
              { text: s.cc.keepScreenOn, link: `${prefix}/guide/app-actions/edit-common-config/keep-screen-on` },
              { text: s.cc.floatingWindow, link: `${prefix}/guide/app-actions/edit-common-config/floating-window` },
              { text: s.cc.longPressMenu, link: `${prefix}/guide/app-actions/edit-common-config/long-press-menu` }
            ]
          },
          {
            text: s.cc.mediaInteraction,
            collapsed: false,
            items: [
              { text: s.cc.splash, link: `${prefix}/guide/app-actions/edit-common-config/splash` },
              { text: s.cc.bgm, link: `${prefix}/guide/app-actions/edit-common-config/bgm` },
              { text: s.cc.announcement, link: `${prefix}/guide/app-actions/edit-common-config/announcement` },
              { text: s.cc.translate, link: `${prefix}/guide/app-actions/edit-common-config/translate` }
            ]
          },
          {
            text: s.cc.extensionsNetwork,
            collapsed: false,
            items: [
              { text: s.cc.extensionModules, link: `${prefix}/guide/app-actions/edit-common-config/extension-modules` },
              { text: s.cc.adBlocking, link: `${prefix}/guide/app-actions/edit-common-config/ad-blocking` },
              { text: s.cc.customDns, link: `${prefix}/guide/app-actions/edit-common-config/custom-dns` }
            ]
          },
          {
            text: s.cc.disguise,
            collapsed: false,
            items: [
              { text: s.cc.iconDisguise, link: `${prefix}/guide/app-actions/edit-common-config/icon-disguise` },
              { text: s.cc.deviceDisguise, link: `${prefix}/guide/app-actions/edit-common-config/device-disguise` }
            ]
          },
          {
            text: s.cc.launchRuntime,
            collapsed: false,
            items: [
              { text: s.cc.autoStart, link: `${prefix}/guide/app-actions/edit-common-config/auto-start` },
              { text: s.cc.forcedRun, link: `${prefix}/guide/app-actions/edit-common-config/forced-run` },
              { text: s.cc.blacktech, link: `${prefix}/guide/app-actions/edit-common-config/blacktech` }
            ]
          },
          {
            text: s.cc.advancedExport,
            collapsed: false,
            items: [
              { text: s.cc.advancedSettings, link: `${prefix}/guide/app-actions/edit-common-config/advanced-settings` },
              { text: s.cc.specialSettings, link: `${prefix}/guide/app-actions/edit-common-config/special-settings` },
              { text: s.cc.apkExport, link: `${prefix}/guide/app-actions/edit-common-config/apk-export` }
            ]
          }
        ]
      },
      {
        text: s.guide.moreFeatures,
        items: [
          { text: s.guide.aiCoding, link: `${prefix}/guide/more-features/ai-coding` },
          { text: s.guide.aiSettings, link: `${prefix}/guide/more-features/ai-settings` },
          { text: s.guide.extensionModules, link: `${prefix}/guide/more-features/extension-modules` },
          { text: s.guide.appModifier, link: `${prefix}/guide/more-features/app-modifier` },
          { text: s.guide.linuxEnvironment, link: `${prefix}/guide/more-features/linux-environment` },
          { text: s.guide.runtimeManagement, link: `${prefix}/guide/more-features/runtime-management` },
          { text: s.guide.portManager, link: `${prefix}/guide/more-features/port-manager` },
          { text: s.guide.browserKernel, link: `${prefix}/guide/more-features/browser-kernel` },
          { text: s.guide.hostsAdblock, link: `${prefix}/guide/more-features/hosts-adblock` },
          { text: s.guide.usageStats, link: `${prefix}/guide/more-features/usage-stats` },
          { text: s.guide.googlePlay, link: `${prefix}/guide/more-features/google-play` },
          { text: s.guide.fileManager, link: `${prefix}/guide/more-features/file-manager` },
          { text: s.guide.batchImport, link: `${prefix}/guide/more-features/batch-import` },
          { text: s.guide.about, link: `${prefix}/guide/more-features/about` }
        ]
      },
      {
        text: s.guide.config,
        items: [
          { text: s.guide.configOverview, link: `${prefix}/guide/config/` },
          { text: s.guide.network, link: `${prefix}/guide/config/network` },
          { text: s.guide.privacy, link: `${prefix}/guide/config/privacy` },
          { text: s.guide.appearance, link: `${prefix}/guide/config/appearance` },
          { text: s.guide.runtimes, link: `${prefix}/guide/config/runtimes` }
        ]
      },
      {
        text: s.guide.faq,
        items: [{ text: s.guide.faq, link: `${prefix}/guide/faq` }]
      }
    ],
    [`${prefix}/developer/`]: [
      {
        text: s.dev.title,
        items: [
          { text: s.dev.overview, link: `${prefix}/developer/` },
          { text: s.dev.architecture, link: `${prefix}/developer/architecture` },
          { text: s.dev.exportPipeline, link: `${prefix}/developer/export-pipeline` },
          { text: s.dev.shellSync, link: `${prefix}/developer/shell-sync` },
          { text: s.dev.configDrift, link: `${prefix}/developer/config-drift` },
          { text: s.dev.i18n, link: `${prefix}/developer/i18n` },
          { text: s.dev.recipes, link: `${prefix}/developer/recipes` },
          { text: s.dev.contributing, link: `${prefix}/developer/contributing` }
        ]
      }
    ],
    [`${prefix}/extensions/`]: [
      {
        text: s.ext.title,
        items: [
          { text: s.ext.overview, link: `${prefix}/extensions/` },
          { text: s.ext.jsModule, link: `${prefix}/extensions/js-module` },
          { text: s.ext.cssModule, link: `${prefix}/extensions/css-module` },
          { text: s.ext.userscript, link: `${prefix}/extensions/userscript` },
          { text: s.ext.chromeMv3, link: `${prefix}/extensions/chrome-mv3` },
          { text: s.ext.apiRef, link: `${prefix}/extensions/api-reference` },
          { text: s.ext.publish, link: `${prefix}/extensions/publish` }
        ]
      }
    ]
  }
}

function themeConfig(lang: Lang, prefix: string) {
  const s = t[lang]
  const isZh = lang === 'zh'
  return {
    nav: nav(lang, prefix),
    sidebar: sidebar(lang, prefix),
    outline: { label: isZh ? '本页目录' : 'On this page' },
    docFooter: {
      prev: isZh ? '上一页' : 'Prev',
      next: isZh ? '下一页' : 'Next'
    },
    darkModeSwitchLabel: isZh ? '外观' : 'Appearance',
    lightModeSwitchText: isZh ? '浅色' : 'Light',
    darkModeSwitchText: isZh ? '深色' : 'Dark',
    sidebarMenuLabel: isZh ? '菜单' : 'Menu',
    returnToTopLabel: isZh ? '返回顶部' : 'Return to top',
    lastUpdated: {
      text: isZh ? '最后更新' : 'Last updated',
      formatOptions: { dateStyle: 'medium' }
    },
    editLink: {
      pattern: 'https://github.com/shiaho777/web-to-app/edit/main/docs/:path',
      text: isZh ? '在 GitHub 上编辑此页' : 'Edit this page on GitHub'
    }
  }
}

export default defineConfig({
  title: 'WebToApp',
  description:
    'Build Android APKs from web projects, directly on your phone — on-device runtimes, hardened networking, and Play-ready export.',
  lang: 'en',
  base,
  cleanUrls: true,
  lastUpdated: true,
  head: [
    ['link', { rel: 'icon', type: 'image/png', href: `${base}logo.png` }],
    ['meta', { name: 'theme-color', content: '#3b82f6' }],
    ['meta', { property: 'og:type', content: 'website' }],
    ['meta', { property: 'og:title', content: 'WebToApp' }],
    [
      'meta',
      {
        property: 'og:description',
        content: 'An on-device APK workshop that goes far beyond URL wrapping.'
      }
    ],
    ['meta', { property: 'og:image', content: `${base}social-preview.png` }]
  ],
  locales: {
    root: {
      label: 'English',
      lang: 'en',
      themeConfig: themeConfig('en', '')
    },
    zh: {
      label: '简体中文',
      lang: 'zh-CN',
      title: 'WebToApp',
      description: '在手机上直接把网页项目构建成 Android APK —— 设备端运行时、加固网络栈、Play 级导出。',
      themeConfig: themeConfig('zh', '/zh')
    }
  },
  themeConfig: {
    logo: '/logo.png',
    siteTitle: 'WebToApp',
    search: {
      provider: 'local',
      options: {
        locales: {
          zh: {
            translations: {
              button: { buttonText: '搜索文档', buttonAriaLabel: '搜索文档' },
              modal: {
                noResultsText: '无法找到相关结果',
                resetButtonTitle: '清除查询条件',
                footer: { selectText: '选择', navigateText: '切换', closeText: '关闭' }
              }
            }
          }
        }
      }
    },
    socialLinks: [
      { icon: 'github', link: 'https://github.com/shiaho777/web-to-app' },
      { icon: 'x', link: 'https://x.com/shiaho777' }
    ],
    footer: {
      message: 'Released under the Unlicense.',
      copyright: 'Built by shiaho · WebToApp'
    }
  }
})
