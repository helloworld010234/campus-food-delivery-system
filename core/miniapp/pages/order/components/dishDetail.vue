<template>
  <view class="detail_card">
    <view class="card_header">
      <view class="header_copy">
        <text class="card_label">商品明细</text>
        <text class="shop_name">{{ shopInfo.shopName || "校园窗口" }}</text>
      </view>
      <view class="status_tag">宿舍直送</view>
    </view>

    <view class="dish_list">
      <view class="type_item" v-for="(obj, index) in displayList" :key="index">
        <view class="dish_img">
          <image mode="aspectFill" :src="obj.image" class="dish_img_url"></image>
        </view>
        <view class="dish_info">
          <view class="dish_name">{{ obj.name }}</view>
          <view class="dish_dishFlavor" v-if="obj.dishFlavor">{{ obj.dishFlavor }}</view>
          <view class="dish_meta">适合校内配送，建议电话保持畅通</view>
        </view>
        <view class="dish_side">
          <view class="dish_price">
            <text class="ico">¥</text>{{ Number(obj.amount || 0).toFixed(2) }}
          </view>
          <view class="dish_number">x{{ obj.number }}</view>
        </view>
      </view>
    </view>

    <view class="toggle_line" v-if="orderListDataes.length > 3" @click="expanded = !expanded">
      {{ expanded ? "收起部分商品" : "展开更多商品" }}
    </view>

    <view class="fee_block">
      <view class="fee_row">
        <text>打包费</text>
        <text>¥{{ orderDishNumber }}</text>
      </view>
      <view class="fee_row">
        <text>配送费</text>
        <text>¥{{ Number(deliveryFee || 0).toFixed(2) }}</text>
      </view>
      <view class="total_row">
        <text>合计</text>
        <view class="total_price">
          <text class="ico">¥</text>{{ orderDishPrice.toFixed(2) }}
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { mapState } from "vuex"

export default {
  props: {
    orderDataes: {
      type: Array,
      default: () => [],
    },
    showDisplay: {
      type: Boolean,
      default: false,
    },
    orderListDataes: {
      type: Array,
      default: () => [],
    },
    orderDishNumber: {
      type: Number,
      default: 0,
    },
    orderDishPrice: {
      type: Number,
      default: 0,
    },
  },
  data() {
    return {
      expanded: this.showDisplay,
    }
  },
  computed: {
    ...mapState(["deliveryFee", "shopInfo"]),
    displayList() {
      if (this.expanded || this.orderListDataes.length <= 3) {
        return this.orderListDataes
      }
      return this.orderListDataes.slice(0, 3)
    },
  },
}
</script>

<style lang="scss" scoped>
$primary: #2f5d50;
$text: #23312c;
$muted: #73817c;
$accent: #f4a259;

.detail_card {
  margin-bottom: 22rpx;
  padding: 28rpx;
  border-radius: 32rpx;
  background: #fbfaf7;
  box-shadow: 0 20rpx 50rpx rgba(36, 54, 46, 0.08);
}

.card_header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20rpx;
}

.header_copy {
  min-width: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.card_label {
  font-size: 24rpx;
  line-height: 34rpx;
  color: $accent;
  font-weight: 600;
}

.shop_name {
  margin-top: 12rpx;
  font-size: 34rpx;
  line-height: 46rpx;
  color: $text;
  font-weight: 700;
}

.status_tag {
  flex-shrink: 0;
  padding: 12rpx 18rpx;
  border-radius: 999rpx;
  background: rgba(47, 93, 80, 0.08);
  color: $primary;
  font-size: 22rpx;
  line-height: 30rpx;
}

.dish_list {
  margin-top: 20rpx;
}

.type_item {
  display: flex;
  gap: 18rpx;
  padding: 18rpx 0;
  border-bottom: 1rpx solid rgba(47, 93, 80, 0.08);
}

.type_item:last-child {
  border-bottom: 0;
}

.dish_img {
  width: 104rpx;
  flex-shrink: 0;
}

.dish_img_url {
  width: 104rpx;
  height: 104rpx;
  border-radius: 22rpx;
}

.dish_info {
  min-width: 0;
  flex: 1;
}

.dish_name {
  font-size: 28rpx;
  line-height: 38rpx;
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

.dish_side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: space-between;
  gap: 8rpx;
}

.dish_price,
.total_price {
  font-size: 28rpx;
  line-height: 38rpx;
  color: #d36e32;
  font-weight: 700;

  .ico {
    font-size: 20rpx;
  }
}

.dish_number {
  font-size: 22rpx;
  line-height: 32rpx;
  color: $muted;
}

.toggle_line {
  margin-top: 8rpx;
  text-align: center;
  font-size: 24rpx;
  line-height: 34rpx;
  color: $primary;
  font-weight: 600;
}

.fee_block {
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid rgba(47, 93, 80, 0.08);
}

.fee_row,
.total_row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
}

.fee_row {
  padding-bottom: 14rpx;
  font-size: 24rpx;
  line-height: 34rpx;
  color: $muted;
}

.total_row {
  margin-top: 10rpx;
  font-size: 28rpx;
  line-height: 38rpx;
  color: $text;
  font-weight: 700;
}
</style>
