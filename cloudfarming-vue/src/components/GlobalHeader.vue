<template>
  <div id="global-header">
    <a-row>
      <a-col :span="12">
        <a-flex justify="space-evenly" align="center" class="header-flex">
          <a href="/">请登录</a>
          <a href="/">免费注册</a>
        </a-flex>
      </a-col>
      <a-col :span="12">
        <a-flex justify="center" align="center">
          <a>帮助中心</a>
        </a-flex>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">

</script>

<style scoped>
#global-header {
  height: 36px;
  background-color: #f2f2f2;
  margin: 0 240px;
  text-align: center;
}

#global-header :deep(.ant-row) {
  height: 100%;
}

#global-header :deep(.ant-col) {
  display: flex;
}

/* 给flex中的链接添加间距 */
.header-flex a {
  margin: 0 8px;
}
</style>