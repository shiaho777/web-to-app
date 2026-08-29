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
      <LatestRelease v-if="frontmatter.layout === 'home' && frontmatter.hero" />
    </template>
  </Layout>
</template>
