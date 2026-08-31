<script setup lang="ts">
import DefaultTheme from 'vitepress/theme'
import { useData } from 'vitepress'
import LatestRelease from './LatestRelease.vue'

const { Layout } = DefaultTheme
const { frontmatter } = useData()
</script>

<template>
  <Layout>
    <template #home-hero-actions-after>
      <!-- leading space: soft wrap point between .actions and the download pill -->
      {{ ' ' }}<LatestRelease v-if="frontmatter.layout === 'home' && frontmatter.hero" />
    </template>
  </Layout>
</template>
