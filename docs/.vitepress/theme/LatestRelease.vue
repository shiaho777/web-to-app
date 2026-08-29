<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useData } from 'vitepress'

const REPO = 'shiaho777/web-to-app'
const RELEASES_PAGE = `https://github.com/${REPO}/releases`

const { lang } = useData()
const isZh = computed(() => lang.value.startsWith('zh'))

// Fallback target until the API resolves (or if it fails): the releases page.
const href = ref(RELEASES_PAGE)
const tag = ref('')
const detected = ref(false)

onMounted(async () => {
  try {
    const res = await fetch(`https://api.github.com/repos/${REPO}/releases/latest`, {
      headers: { Accept: 'application/vnd.github+json' }
    })
    if (!res.ok) return
    const release: any = await res.json()
    const asset = (release.assets ?? []).find((a: any) =>
      typeof a.name === 'string' && a.name.toLowerCase().endsWith('.apk')
    )
    href.value = asset?.browser_download_url || release.html_url || RELEASES_PAGE
    tag.value = release.tag_name ?? ''
    detected.value = true
  } catch {
    // Rate-limited or offline: keep the releases-page fallback.
  }
})

const buttonLabel = computed(() =>
  tag.value
    ? `${isZh.value ? '下载' : 'Download'} ${tag.value}`
    : isZh.value
      ? '下载最新版 APK'
      : 'Download latest APK'
)

const metaLabel = computed(() =>
  detected.value
    ? isZh.value
      ? '版本号自动检测自 GitHub Releases'
      : 'Version auto-detected from GitHub Releases'
    : ''
)
</script>

<template>
  <div class="wta-release">
    <a class="wta-release-btn" :href="href" target="_blank" rel="noreferrer">
      <svg
        xmlns="http://www.w3.org/2000/svg"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2"
        stroke-linecap="round"
        stroke-linejoin="round"
      >
        <path d="M12 3v12" />
        <path d="m7 10 5 5 5-5" />
        <path d="M4 21h16" />
      </svg>
      <span>{{ buttonLabel }}</span>
    </a>
    <span v-if="metaLabel" class="wta-release-meta">{{ metaLabel }}</span>
  </div>
</template>
