<template>
  <a-layout class="basic-layout">
    <GlobalHeader />
    <a-layout-content class="layout-content">
      <div class="content-wrapper">
        <RouterView />
      </div>
    </a-layout-content>
    <GlobalFooter />
  </a-layout>
</template>

<script setup lang="ts">
import { RouterView } from 'vue-router'
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'
</script>

<style scoped>
.basic-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #f0f9ff 0%, #e0f2fe 30%, #dbeafe 60%, #e0e7ff 100%);
  position: relative;
}

.basic-layout::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    radial-gradient(circle at 20% 50%, rgba(147, 197, 253, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 80%, rgba(167, 243, 208, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 40% 20%, rgba(196, 181, 253, 0.08) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
}

.layout-content {
  flex: 1;
  background: transparent;
  min-height: calc(100vh - 64px - 50px);
  position: relative;
  z-index: 1;
}

.content-wrapper {
  max-width: 1600px;
  margin: 0 auto;
  padding: 24px;
  min-height: 100%;
  position: relative;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .content-wrapper {
    padding: 16px;
  }
}
</style>

