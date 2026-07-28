import { defineConfig } from 'vitepress'

// GitHub Pages serves this under /web-to-app/. Override with DOCS_BASE for a
// custom domain (e.g. DOCS_BASE=/ npm run build).
const base = process.env.DOCS_BASE || '/web-to-app/'

type Lang = 'en' | 'zh'

const t = {
  en: {
    nav: { guide: 'Guide', developer: 'Developer', extensions: 'Extensions' },
    guide: {
      start: 'Getting Started',
      intro: 'Introduction',
      gettingStarted: 'Getting Started',
      mainScreen: 'Main Screen',
      create: 'Create App',
      createOverview: 'Overview & Types',
      webContent: 'Web Content',
      serverRuntimes: 'Server Runtimes',
      media: 'Media',
      appActions: 'App Actions',
      more: 'More Features',
      aiTools: 'AI Tools',
      devTools: 'Developer Tools',
      browser: 'Browser',
      system: 'System',
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
    guide: {
      start: '入门',
      intro: '简介',
      gettingStarted: '快速开始',
      mainScreen: '主界面',
      create: '创建应用',
      createOverview: '总览与类型',
      webContent: '网页内容',
      serverRuntimes: '服务运行时',
      media: '媒体',
      appActions: '应用功能',
      more: '更多功能',
      aiTools: 'AI 工具',
      devTools: '开发工具',
      browser: '浏览器',
      system: '系统',
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
        items: [{ text: s.guide.mainScreen, link: `${prefix}/guide/main-screen` }]
      },
      {
        text: s.guide.create,
        items: [
          { text: s.guide.createOverview, link: `${prefix}/guide/create-app` },
          { text: s.guide.webContent, link: `${prefix}/guide/app-types/web-content` },
          { text: s.guide.serverRuntimes, link: `${prefix}/guide/app-types/server-runtimes` },
          { text: s.guide.media, link: `${prefix}/guide/app-types/media` }
        ]
      },
      {
        text: s.guide.appActions,
        items: [{ text: s.guide.appActions, link: `${prefix}/guide/app-actions` }]
      },
      {
        text: s.guide.more,
        items: [
          { text: s.guide.aiTools, link: `${prefix}/guide/more-features/ai-tools` },
          { text: s.guide.devTools, link: `${prefix}/guide/more-features/dev-tools` },
          { text: s.guide.browser, link: `${prefix}/guide/more-features/browser` },
          { text: s.guide.system, link: `${prefix}/guide/more-features/system` }
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
