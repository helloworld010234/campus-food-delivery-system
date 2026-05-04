<template>
  <view class="dish_detail_pop">
    <view class="detail_panel" v-if="dishDetailes.type == 1">
      <image mode="aspectFill" class="div_big_image" :src="dishDetailes.image"></image>
      <view class="title">{{ dishDetailes.name }}</view>
      <view class="desc">
        {{ dishDetailes.description || "现点现做，优先安排校内配送。" }}
      </view>
      <view class="scene_tip">适合宿舍点餐，也适合下课后顺手带走</view>
      <view class="but_item">
        <view class="price">
          <text class="ico">¥</text>
          {{ Number(dishDetailes.price || 0).toFixed(2) }}
        </view>
        <view
          class="active"
          v-if="dishDetailes.flavors && dishDetailes.flavors.length === 0 && dishDetailes.dishNumber > 0"
        >
          <view class="dish_step dish_step--light" @click="redDishAction(dishDetailes, 'menu')">
            -
          </view>
          <text class="dish_number">{{ dishDetailes.dishNumber }}</text>
          <view class="dish_step" @click="addDishAction(dishDetailes, 'menu')">
            +
          </view>
        </view>

        <view class="active" v-if="dishDetailes.flavors && dishDetailes.flavors.length > 0">
          <view class="dish_card_add" @click="moreNormDataesHandle(dishDetailes)">
            选择规格
          </view>
        </view>

        <view
          class="active"
          v-if="(!dishDetailes.flavors || dishDetailes.flavors.length === 0) && dishDetailes.dishNumber === 0"
        >
          <view class="dish_card_add" @click="addDishAction(dishDetailes, 'menu')">
            加入购物车
          </view>
        </view>
      </view>
    </view>

    <view class="detail_panel" v-else>
      <view class="title">{{ dishDetailes.name }}</view>
      <view class="desc">套餐内容如下，支持直接加入购物车</view>
      <scroll-view class="dish_items" scroll-y="true" scroll-top="0">
        <view class="dish_item" v-for="(item, index) in dishMealData" :key="index">
          <image class="div_big_image small" :src="item.image" mode="aspectFill"></image>
          <view class="dish_item_body">
            <view class="dish_item_title">
              {{ item.name }}
              <text>X{{ item.copies }}</text>
            </view>
            <view class="dish_item_desc">{{ item.description || "套餐内搭配" }}</view>
          </view>
        </view>
      </scroll-view>
      <view class="but_item">
        <view class="price">
          <text class="ico">¥</text>
          {{ Number(dishDetailes.price || 0).toFixed(2) }}
        </view>
        <view class="active" v-if="dishDetailes.dishNumber && dishDetailes.dishNumber > 0">
          <view class="dish_step dish_step--light" @click="redDishAction(dishDetailes, 'menu')">
            -
          </view>
          <text class="dish_number">{{ dishDetailes.dishNumber }}</text>
          <view class="dish_step" @click="addDishAction(dishDetailes, 'menu')">
            +
          </view>
        </view>
        <view class="active" v-else>
          <view class="dish_card_add" @click="addDishAction(dishDetailes, 'menu')">
            加入购物车
          </view>
        </view>
      </view>
    </view>

    <view class="close" @click="dishClose">×</view>
  </view>
</template>

<script>
export default {
  props: {
    dishDetailes: {
      type: Object,
      default: () => ({}),
    },
    openDetailPop: {
      type: Boolean,
      default: false,
    },
    dishMealData: {
      type: Array,
      default: () => [],
    },
  },
  methods: {
    addDishAction(obj, item) {
      this.$emit("addDishAction", obj, item)
    },
    redDishAction(obj, item) {
      this.$emit("redDishAction", obj, item)
    },
    moreNormDataesHandle(obj) {
      this.$emit("moreNormDataesHandle", obj)
    },
    dishClose() {
      this.$emit("dishClose")
    },
  },
}
</script>

<style lang="scss" scoped>
$primary: #2f5d50;
$text: #23312c;
$muted: #73817c;
$accent: #f4a259;

.dish_detail_pop {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.detail_panel {
  width: calc(100vw - 96rpx);
  max-height: 72vh;
  padding: 28rpx;
  box-sizing: border-box;
  border-radius: 36rpx;
  background: #fbfaf7;
  position: relative;
}

.div_big_image {
  width: 100%;
  height: 320rpx;
  border-radius: 28rpx;
}

.div_big_image.small {
  width: 108rpx;
  height: 108rpx;
  flex-shrink: 0;
}

.title {
  margin-top: 24rpx;
  font-size: 38rpx;
  line-height: 50rpx;
  color: $text;
  font-weight: 700;
}

.desc,
.scene_tip,
.dish_item_desc {
  font-size: 24rpx;
  line-height: 36rpx;
  color: $muted;
}

.desc {
  margin-top: 12rpx;
}

.scene_tip {
  margin-top: 12rpx;
  padding: 14rpx 18rpx;
  border-radius: 22rpx;
  background: rgba(47, 93, 80, 0.08);
}

.dish_items {
  margin-top: 24rpx;
  max-height: 44vh;
}

.dish_item {
  display: flex;
  gap: 18rpx;
  padding: 18rpx 0;
  border-bottom: 1rpx solid rgba(47, 93, 80, 0.08);
}

.dish_item_body {
  min-width: 0;
  flex: 1;
}

.dish_item_title {
  font-size: 28rpx;
  line-height: 40rpx;
  color: $text;
  font-weight: 700;

  text {
    margin-left: 10rpx;
    color: $accent;
    font-size: 22rpx;
  }
}

.dish_item_desc {
  margin-top: 8rpx;
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

.active {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.dish_step {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: $primary;
  color: #ffffff;
  text-align: center;
  line-height: 56rpx;
  font-size: 34rpx;
  font-weight: 600;
}

.dish_step--light {
  background: rgba(47, 93, 80, 0.1);
  color: $primary;
}

.dish_number {
  min-width: 24rpx;
  text-align: center;
  font-size: 28rpx;
  line-height: 38rpx;
  color: $text;
  font-weight: 600;
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
  bottom: 88rpx;
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
