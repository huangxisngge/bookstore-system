<template>
  <div class="book-cover" :style="coverStyle">
    <el-image v-if="src" :src="src" fit="cover" :style="imgStyle" @error="imgError = true">
      <template #error>
        <div class="cover" :class="'style-' + styleIndex" :style="imgStyle">
          <div class="cover-inner">
            <!-- 样式0: 经典竖排 -->
            <template v-if="styleIndex === 0">
              <div class="s0-top">{{ author }}</div>
              <div class="s0-title">{{ title }}</div>
              <div class="s0-bottom">{{ author }}</div>
            </template>
            <!-- 样式1: 横排极简 -->
            <template v-else-if="styleIndex === 1">
              <div class="s1-border">
                <div class="s1-title">{{ title }}</div>
                <div class="s1-author">{{ author }}</div>
              </div>
            </template>
            <!-- 样式2: 上色块标题 -->
            <template v-else-if="styleIndex === 2">
              <div class="s2-header"></div>
              <div class="s2-title">{{ title }}</div>
              <div class="s2-author">{{ author }}</div>
            </template>
            <!-- 样式3: 下划线文学风 -->
            <template v-else-if="styleIndex === 3">
              <div class="s3-title">{{ title }}</div>
              <div class="s3-line"></div>
              <div class="s3-author">{{ author }}</div>
            </template>
            <!-- 样式4: 横线装饰 -->
            <template v-else>
              <div class="s4-title">{{ title }}</div>
              <div class="s4-dots">· · ·</div>
              <div class="s4-author">{{ author }}</div>
            </template>
          </div>
        </div>
      </template>
    </el-image>
    <div v-else class="cover" :class="'style-' + styleIndex" :style="imgStyle">
      <div class="cover-inner">
        <template v-if="styleIndex === 0">
          <div class="s0-top">{{ author }}</div>
          <div class="s0-title">{{ title }}</div>
          <div class="s0-bottom">{{ author }}</div>
        </template>
        <template v-else-if="styleIndex === 1">
          <div class="s1-border">
            <div class="s1-title">{{ title }}</div>
            <div class="s1-author">{{ author }}</div>
          </div>
        </template>
        <template v-else-if="styleIndex === 2">
          <div class="s2-header"></div>
          <div class="s2-title">{{ title }}</div>
          <div class="s2-author">{{ author }}</div>
        </template>
        <template v-else-if="styleIndex === 3">
          <div class="s3-title">{{ title }}</div>
          <div class="s3-line"></div>
          <div class="s3-author">{{ author }}</div>
        </template>
        <template v-else>
          <div class="s4-title">{{ title }}</div>
          <div class="s4-dots">· · ·</div>
          <div class="s4-author">{{ author }}</div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  src: { type: String, default: '' },
  title: { type: String, default: '' },
  author: { type: String, default: '' },
  width: { type: String, default: '120px' },
  height: { type: String, default: '160px' }
})

const imgError = ref(false)

const palettes = [
  { bg: '#f0ebe4', ink: '#2c1810', accent: '#8B3A4A', spine: '#3D2B1F' },
  { bg: '#f5f0e8', ink: '#3D2B1F', accent: '#8B3A4A', spine: '#5c3a28' },
  { bg: '#ede5d8', ink: '#2c1810', accent: '#C4A35A', spine: '#3D2B1F' },
  { bg: '#e8e2d8', ink: '#3a2a1a', accent: '#4A7C59', spine: '#3D2B1F' },
  { bg: '#f0e8e0', ink: '#2c1810', accent: '#8B3A4A', spine: '#5c3a28' },
  { bg: '#f5efe8', ink: '#3D2B1F', accent: '#C4A35A', spine: '#3D2B1F' },
  { bg: '#ece5dc', ink: '#2c1810', accent: '#4A7C59', spine: '#3D2B1F' },
  { bg: '#f0ebe0', ink: '#3a2a1a', accent: '#8B3A4A', spine: '#3D2B1F' },
  { bg: '#e8e0d5', ink: '#2c1810', accent: '#C4A35A', spine: '#5c3a28' },
  { bg: '#f5f0e5', ink: '#3D2B1F', accent: '#8B3A4A', spine: '#3D2B1F' },
]

function hash(str) {
  let h = 0
  for (let i = 0; i < str.length; i++) {
    h = str.charCodeAt(i) + ((h << 5) - h)
  }
  return Math.abs(h)
}

const palette = computed(() => palettes[hash(props.title || 'x') % palettes.length])
const styleIndex = computed(() => hash(props.title || 'x') % 5)

const coverStyle = computed(() => ({
  width: props.width,
  height: props.height,
  borderRadius: '3px 6px 6px 3px',
  overflow: 'hidden',
  flexShrink: 0,
  boxShadow: '2px 3px 10px rgba(60,40,15,0.22)'
}))

const imgStyle = computed(() => ({
  width: props.width,
  height: props.height
}))
</script>

<style scoped>
.book-cover { position: relative; }

/* 通用封面容器 */
.cover {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  font-family: 'Noto Serif SC', 'Source Han Serif SC', 'SimSun', 'STSong', serif;
  user-select: none;
  overflow: hidden;
}
.cover-inner { width: 100%; height: 100%; position: relative; display: flex; }

/* ============ 样式0: 经典竖排 ============ */
.style-0 {
  background: v-bind('palette.bg');
  padding: 0;
}
.style-0 .cover-inner {
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
  padding: 14% 0;
}
.style-0::before {
  content: '';
  position: absolute;
  left: 0; top: 0; bottom: 0;
  width: 8%;
  background: v-bind('palette.spine');
  box-shadow: inset -2px 0 4px rgba(0,0,0,0.12);
}
.s0-top { font-size: 10px; color: v-bind('palette.accent'); letter-spacing: 2px; writing-mode: horizontal-tb; }
.s0-title {
  font-size: 18px;
  font-weight: 700;
  color: v-bind('palette.ink');
  writing-mode: vertical-rl;
  letter-spacing: 6px;
  line-height: 1.8;
  display: -webkit-box;
  -webkit-line-clamp: 5;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-orientation: mixed;
}
.s0-bottom { font-size: 10px; color: v-bind('palette.accent'); opacity: 0.7; }

/* ============ 样式1: 极简方框 ============ */
.style-1 {
  background: v-bind('palette.bg');
}
.style-1 .cover-inner {
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.s1-border {
  border: 1.5px solid v-bind('palette.accent');
  padding: 18% 14%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 76%;
  height: 76%;
}
.s1-title {
  font-size: 14px;
  font-weight: 700;
  color: v-bind('palette.ink');
  text-align: center;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-all;
}
.s1-author {
  margin-top: auto;
  font-size: 10px;
  color: v-bind('palette.accent');
  letter-spacing: 1px;
}

/* ============ 样式2: 顶部色块 ============ */
.style-2 {
  background: v-bind('palette.bg');
}
.style-2 .cover-inner {
  flex-direction: column;
}
.s2-header {
  height: 28%;
  background: v-bind('palette.spine');
  width: 100%;
  flex-shrink: 0;
}
.s2-title {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12%;
  font-size: 14px;
  font-weight: 700;
  color: v-bind('palette.ink');
  text-align: center;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-all;
}
.s2-author {
  padding: 0 14% 12%;
  font-size: 10px;
  color: v-bind('palette.accent');
  text-align: right;
}

/* ============ 样式3: 文学下划线 ============ */
.style-3 {
  background: v-bind('palette.bg');
}
.style-3 .cover-inner {
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
  padding: 16% 14%;
}
.s3-title {
  font-size: 15px;
  font-weight: 700;
  color: v-bind('palette.ink');
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-all;
}
.s3-line {
  width: 30px;
  height: 2px;
  background: v-bind('palette.accent');
  margin: 10px 0;
  flex-shrink: 0;
}
.s3-author {
  font-size: 10px;
  color: v-bind('palette.accent');
  letter-spacing: 1px;
}

/* ============ 样式4: 三点居中 ============ */
.style-4 {
  background: v-bind('palette.bg');
}
.style-4 .cover-inner {
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 16%;
  gap: 8px;
}
.s4-title {
  font-size: 14px;
  font-weight: 700;
  color: v-bind('palette.ink');
  text-align: center;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-all;
}
.s4-dots {
  font-size: 14px;
  color: v-bind('palette.accent');
  letter-spacing: 4px;
}
.s4-author {
  font-size: 10px;
  color: v-bind('palette.accent');
  letter-spacing: 1px;
  margin-top: auto;
}
</style>
