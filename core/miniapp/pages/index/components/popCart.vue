<template>
  <view class="cart_pop" @click.stop>
    <view class="top_title">
      <view class="top_copy">
        <view class="tit">购物车</view>
        <view class="sub">校内配送订单会优先送到宿舍楼下或教学楼门口</view>
      </view>
      <view class="clear" @click.stop="clearCardOrder">清空</view>
    </view>

    <scroll-view class="card_order_list" scroll-y="true" scroll-top="0">
      <view
        class="type_item_cont"
        v-for="(item, ind) in orderAndUserInfo"
        :key="ind"
      >
        <view
          class="type_item"
          v-for="(obj, index) in item.dishList"
          :key="index"
        >
          <view class="dish_img">
            <image mode="aspectFill" :src="obj.image" class="dish_img_url"></image>
          </view>
          <view class="dish_info">
            <view class="dish_name">{{ obj.name }}</view>
            <view class="dish_dishFlavor" v-if="obj.dishFlavor">
              {{ obj.dishFlavor }}
            </view>
            <view class="dish_meta">当前窗口顺路配送中</view>
            <view class="dish_row">
              <view class="dish_price">
                <text class="ico">¥</text>{{ Number(obj.amount || 0).toFixed(2) }}
              </view>
              <view class="dish_active">
                <view
                  v-if="obj.number && obj.number > 0"
                  class="dish_step dish_step--light"
                  @click.stop="redDishAction(obj, 'cart')"
                >
                  -
                </view>
                <text v-if="obj.number && obj.number > 0" class="dish_number">
                  {{ obj.number }}
                </text>
                <view class="dish_step" @click.stop="addDishAction(obj, 'cart')">
                  +
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>
      <view class="seize_seat"></view>
    </scroll-view>

    <view class="cart_tip">支持备注楼栋、单元门口和电话联系取餐</view>
  </view>
</template>

<script>
export default {
  props: {
    orderAndUserInfo: {
      type: Array,
      default: () => [],
    },
    openOrderCartList: {
      type: Boolean,
      default: false,
    },
  },
  methods: {
    clearCardOrder() {
      this.$emit("clearCardOrder")
    },
    addDishAction(obj, item) {
      this.$emit("addDishAction", obj, item)
    },
    redDishAction(obj, item) {
      this.$emit("redDishAction", obj, item)
    },
  },
}
</script>

<style lang="scss" scoped>
$primary: #2f5d50;
$text: #23312c;
$muted: #73817c;
$accent: #f4a259;

.cart_pop {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 64vh;
  padding: 30rpx 28rpx calc(28rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
  background: #fbfaf7;
  border-radius: 36rpx 36rpx 0 0;
}

.top_title {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24rpx;
  padding-bottom: 22rpx;
  border-bottom: 1rpx solid rgba(47, 93, 80, 0.08);
}

.top_copy {
  min-width: 0;
  flex: 1;
}

.tit {
  font-size: 38rpx;
  line-height: 48rpx;
  color: $text;
  font-weight: 700;
}

.sub {
  margin-top: 10rpx;
  font-size: 22rpx;
  line-height: 32rpx;
  color: $muted;
}

.clear {
  flex-shrink: 0;
  padding: 12rpx 20rpx;
  border-radius: 999rpx;
  background: rgba(47, 93, 80, 0.08);
  color: $primary;
  font-size: 24rpx;
  line-height: 32rpx;
  font-weight: 600;
}

.card_order_list {
  height: calc(100% - 156rpx);
  padding-top: 18rpx;
}

.type_item {
  display: flex;
  gap: 18rpx;
  padding: 18rpx 0;
}

.dish_img {
  width: 124rpx;
  flex-shrink: 0;
}

.dish_img_url {
  width: 124rpx;
  height: 124rpx;
  border-radius: 24rpx;
}

.dish_info {
  min-width: 0;
  flex: 1;
  padding-bottom: 18rpx;
  border-bottom: 1rpx solid rgba(47, 93, 80, 0.08);
}

.dish_name {
  font-size: 28rpx;
  line-height: 40rpx;
  color: $text;
  font-weight: 700;
}

.dish_dishFlavor,
.dish_meta {
  margin-top: 8rpx;
  font-size: 22rpx;
  line-height: 32rpx;
  color: $muted;
}

.dish_row {
  margin-top: 18rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
}

.dish_price {
  font-size: 30rpx;
  line-height: 40rpx;
  color: #d36e32;
  font-weight: 700;

  .ico {
    font-size: 22rpx;
  }
}

.dish_active {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.dish_step {
  width: 52rpx;
  height: 52rpx;
  border-radius: 50%;
  background: $primary;
  color: #ffffff;
  text-align: center;
  line-height: 52rpx;
  font-size: 32rpx;
  font-weight: 600;
}

.dish_step--light {
  background: rgba(47, 93, 80, 0.1);
  color: $primary;
}

.dish_number {
  min-width: 24rpx;
  text-align: center;
  font-size: 26rpx;
  line-height: 36rpx;
  color: $text;
  font-weight: 600;
}

.cart_tip {
  position: absolute;
  left: 28rpx;
  right: 28rpx;
  bottom: calc(18rpx + env(safe-area-inset-bottom));
  padding-top: 18rpx;
  border-top: 1rpx solid rgba(47, 93, 80, 0.08);
  font-size: 22rpx;
  line-height: 32rpx;
  color: $muted;
}

.seize_seat {
  height: 100rpx;
}
</style>
