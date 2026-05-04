<template>
  <view class="more_norm_pop">
    <view class="title">{{ currentDish.name || "选择规格" }}</view>
    <view class="desc">选择喜欢的口味和规格后再加入购物车</view>

    <scroll-view class="items_cont" scroll-y scroll-top="0">
      <view v-for="(obj, index) in moreNormdata" :key="index" class="item_row">
        <view class="flavor_name">{{ obj.name }}</view>
        <view class="flavor_item">
          <view
            v-for="(item, ind) in obj.value"
            :key="ind"
            class="item"
            :class="{ act: isFlavorSelected(item) }"
            @click="checkMoreNormPop(obj.value, item)"
          >
            {{ item }}
          </view>
        </view>
      </view>
    </scroll-view>

    <view class="but_item">
      <view class="price">
        <text class="ico">¥</text>
        {{ currentDishPrice }}
      </view>
      <view class="active">
        <view class="dish_card_add" @click="addShop(currentDish)">
          加入购物车
        </view>
      </view>
    </view>

    <view class="close" @click="closeMoreNorm(currentDish)">×</view>
  </view>
</template>

<script>
export default {
  props: {
    moreNormDishdata: {
      type: Object,
      default: () => ({}),
    },
    moreNormdata: {
      type: Array,
      default: () => [],
    },
    flavorDataes: {
      type: Array,
      default: () => [],
    },
  },
  computed: {
    currentDish() {
      return this.moreNormDishdata || {}
    },
    currentDishPrice() {
      return Number(this.currentDish.price || 0).toFixed(2)
    },
  },
  methods: {
    isFlavorSelected(item) {
      return this.flavorDataes.findIndex((current) => current === item) !== -1
    },
    checkMoreNormPop(obj, item) {
      this.$emit("checkMoreNormPop", { obj, item })
    },
    addShop(obj) {
      this.$emit("addShop", obj)
    },
    closeMoreNorm(obj) {
      this.$emit("closeMoreNorm", obj)
    },
  },
}
</script>

<style lang="scss" scoped>
$primary: #2f5d50;
$text: #23312c;
$muted: #73817c;
$accent: #f4a259;

.more_norm_pop {
  width: calc(100vw - 96rpx);
  max-height: 72vh;
  padding: 32rpx 28rpx 28rpx;
  box-sizing: border-box;
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  background: #fbfaf7;
  border-radius: 36rpx;
}

.title {
  font-size: 38rpx;
  line-height: 48rpx;
  color: $text;
  font-weight: 700;
}

.desc {
  margin-top: 12rpx;
  font-size: 24rpx;
  line-height: 36rpx;
  color: $muted;
}

.items_cont {
  margin-top: 24rpx;
  max-height: 46vh;
}

.item_row + .item_row {
  margin-top: 20rpx;
}

.flavor_name {
  font-size: 26rpx;
  line-height: 36rpx;
  color: $text;
  font-weight: 600;
}

.flavor_item {
  margin-top: 14rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 14rpx;
}

.item {
  padding: 16rpx 22rpx;
  border-radius: 22rpx;
  border: 1rpx solid rgba(47, 93, 80, 0.12);
  background: #ffffff;
  color: $text;
  font-size: 24rpx;
  line-height: 32rpx;
}

.item.act {
  border-color: rgba(244, 162, 89, 0.24);
  background: rgba(244, 162, 89, 0.16);
  color: #d36e32;
  font-weight: 600;
}

.but_item {
  margin-top: 28rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
}

.price {
  font-size: 42rpx;
  line-height: 52rpx;
  color: #d36e32;
  font-weight: 700;

  .ico {
    font-size: 24rpx;
  }
}

.dish_card_add {
  min-width: 214rpx;
  height: 68rpx;
  padding: 0 28rpx;
  border-radius: 999rpx;
  background: $accent;
  color: #ffffff;
  font-size: 26rpx;
  line-height: 68rpx;
  text-align: center;
  font-weight: 600;
}

.close {
  position: absolute;
  left: 50%;
  bottom: -94rpx;
  transform: translateX(-50%);
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.18);
  color: #ffffff;
  text-align: center;
  line-height: 72rpx;
  font-size: 42rpx;
}
</style>
