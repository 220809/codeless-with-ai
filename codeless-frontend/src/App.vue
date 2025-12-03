<script setup lang="ts">
import BasicLayout from './layouts/BasicLayout.vue'
import { healthCheck } from '@/api/healthController.ts'

healthCheck().then(
  ({data}) => console.log(data),
)
</script>

<template>
  <BasicLayout />
</template>

<style>
/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  margin: 0;
  padding: 0;
}
</style>
