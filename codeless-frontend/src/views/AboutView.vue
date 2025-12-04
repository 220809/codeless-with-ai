<template>
  <div class="about">
    <h1>This is an about page</h1>
  </div>
</template>

<style scoped>
.about {
  min-height: calc(100vh - 64px - 50px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
}

.about h1 {
  font-size: 48px;
  font-weight: 700;
  color: #1e293b;
  letter-spacing: -1px;
  text-align: center;
  margin: 0;
  background: linear-gradient(135deg, #1e293b 0%, #3b82f6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

@media (max-width: 768px) {
  .about h1 {
    font-size: 32px;
  }
}
</style>
