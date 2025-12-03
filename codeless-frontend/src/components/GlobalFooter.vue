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
  background: #f0f2f5;
  text-align: center;
  padding: 16px 0;
  border-top: 1px solid #e8e8e8;
  position: relative;
  bottom: 0;
  width: 100%;
}

.footer-content {
  color: rgba(0, 0, 0, 0.65);
  font-size: 14px;
}

.footer-content a {
  color: #1890ff;
  text-decoration: none;
  margin-left: 4px;
}

.footer-content a:hover {
  color: #40a9ff;
  text-decoration: underline;
}
</style>

