<template>
  <div class="cat-empty">
    <img :src="catEmpty" alt="" class="cat-empty-img" />
    <div class="cat-empty-text">{{ description }}</div>
    <slot></slot>
  </div>
</template>

<script setup>
import catEmpty from '@/assets/cat-empty-miss.jpg'

defineProps({
  description: { type: String, default: '这里空空如也' }
})
</script>

<style scoped>
.cat-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 28px 16px;
}
.cat-empty-img {
  width: 190px;
  max-width: 70%;
  border-radius: 16px;
  filter: drop-shadow(0 6px 14px rgba(160, 110, 55, 0.15));
  animation: cat-breathe 3.2s ease-in-out infinite;
}
.cat-empty-text {
  margin-top: 12px;
  color: #a97b52;
  font-size: 14px;
}
@keyframes cat-breathe {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}
</style>