<template>
  <a-layout-footer class="global-footer">
    <div class="footer-content">
      <span>
        CodeLess by
        <a href="https://github.com/220809/codeless-with-ai" target="_blank" rel="noopener noreferrer">
          dingzk
        </a>
      </span>
    </div>
  </a-layout-footer>
</template>

<script setup lang="ts">
// Footer 组件无需额外逻辑
</script>

<style scoped>
.global-footer {
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  text-align: center;
  padding: 20px 0;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  position: relative;
  bottom: 0;
  width: 100%;
  margin-top: auto;
}

.footer-content {
  color: #64748b;
  font-size: 14px;
  font-weight: 400;
}

.footer-content a {
  color: #1890ff;
  text-decoration: none;
  margin-left: 4px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.footer-content a:hover {
  color: #40a9ff;
  text-decoration: underline;
}
</style>

