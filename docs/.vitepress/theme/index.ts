import DefaultTheme from 'vitepress/theme'
import type { Theme } from 'vitepress'
import LatestRelease from './LatestRelease.vue'
import CustomLayout from './CustomLayout.vue'
import './custom.css'

export default {
  extends: DefaultTheme,
  Layout: CustomLayout,
  enhanceApp({ app }) {
    app.component('LatestRelease', LatestRelease)
  }
} satisfies Theme
