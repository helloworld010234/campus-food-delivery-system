<template>
  <view class="navBar" :style="{ paddingTop: safeTop + 'px' }">
    <view class="nav-shell">
      <view class="brand-badge" @click="goHome">
        <view class="brand-dot"></view>
        <text class="brand-text">杏林校园外卖</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  computed: {
    safeTop() {
      const rect = uni.getMenuButtonBoundingClientRect()
      return rect.top > 8 ? rect.top - 4 : rect.top
    },
  },
  methods: {
    goHome() {
      uni.navigateTo({
        url: "/pages/index/index",
      })
    },
  },
}
</script>

<style src="./navbar.scss" lang="scss"></style>
