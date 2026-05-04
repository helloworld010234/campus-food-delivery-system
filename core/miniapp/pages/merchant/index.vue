<template>
  <view class="merchant-page">
    <uni-nav-bar
      @clickLeft="goBack"
      left-icon="back"
      leftIcon="arrowleft"
      title="切换商家"
      statusBar="true"
      fixed="true"
      color="#ffffff"
      backgroundColor="#2f5d50"
    ></uni-nav-bar>

    <view class="merchant-page__body">
      <view class="merchant-page__headline">
        <text class="merchant-page__title">当前校区商家</text>
        <text class="merchant-page__subtitle">
          保留现有点单页面，只切换当前商家
        </text>
      </view>

      <view v-if="merchantItems.length > 0" class="merchant-list">
        <view
          v-for="item in merchantItems"
          :key="item.id"
          class="merchant-card"
          :class="{ active: isCurrentMerchant(item) }"
          @click="selectMerchant(item)"
        >
          <image
            v-if="item.logo"
            class="merchant-card__logo"
            :src="item.logo"
            mode="aspectFill"
          ></image>
          <view v-else class="merchant-card__logo merchant-card__logo--empty">
            {{ getMerchantInitial(item) }}
          </view>

          <view class="merchant-card__body">
            <view class="merchant-card__top">
              <text class="merchant-card__name">{{ item.name }}</text>
              <text
                class="merchant-card__status"
                :class="{ rest: Number(item.businessStatus) !== 1 }"
              >
                {{ Number(item.businessStatus) === 1 ? "营业中" : "休息中" }}
              </text>
            </view>

            <text class="merchant-card__desc">
              {{ item.announcement || item.description || item.addressDetail || "校园商家" }}
            </text>

            <view class="merchant-card__meta">
              <text>配送费 {{ Number(item.deliveryFee || 0).toFixed(0) }}</text>
              <text v-if="item.estimatedDeliveryMinutes">
                {{ item.estimatedDeliveryMinutes }} 分钟送达
              </text>
            </view>
          </view>

          <view v-if="isCurrentMerchant(item)" class="merchant-card__tag">
            当前
          </view>
        </view>
      </view>

      <view v-else class="merchant-empty">
        暂时没有可用商家
      </view>
    </view>
  </view>
</template>

<script>
import { getMerchantList } from "../api/api.js";
import { mapMutations, mapState } from "vuex";
import {
  normalizeMerchantId,
  resolveMerchantIdFromState,
} from "../../utils/merchant.js";

export default {
  data() {
    return {
      merchantItems: [],
      routeMerchantId: "",
    };
  },
  methods: {
    ...mapMutations(["setMerchantList", "setCurrentMerchantId", "setStoreInfo"]),
    ...mapState(["merchantList", "currentMerchantId", "storeInfo", "shopInfo"]),
    getResolvedMerchantId() {
      return resolveMerchantIdFromState(
        this.routeMerchantId || this.currentMerchantId(),
        this.storeInfo() || {},
        this.shopInfo() || {}
      );
    },
    async loadMerchants() {
      const storeInfo = this.storeInfo() || {};
      const params = {};
      if (storeInfo.campusId) {
        params.campusId = storeInfo.campusId;
      }

      try {
        const res = await getMerchantList(params);
        if (res && res.code === 1) {
          const list = Array.isArray(res.data)
            ? res.data.map((item) => ({
                ...item,
                id: normalizeMerchantId(item.id),
              }))
            : [];
          this.merchantItems = list;
          this.setMerchantList(list);
          return;
        }
      } catch (error) {}

      const fallback = this.merchantList();
      this.merchantItems = Array.isArray(fallback) ? fallback : [];
    },
    isCurrentMerchant(item) {
      return normalizeMerchantId(item.id) === this.getResolvedMerchantId();
    },
    getMerchantInitial(item) {
      const name = item && item.name ? String(item.name) : "鍟?";
      return name.slice(0, 1);
    },
    selectMerchant(item) {
      const merchantId = normalizeMerchantId(item.id);
      const currentStoreInfo = this.storeInfo() || {};
      this.setCurrentMerchantId(merchantId);
      this.setStoreInfo({
        ...currentStoreInfo,
        campusId: item.campusId || currentStoreInfo.campusId,
        merchantId,
        shopId: merchantId,
        estimatedDeliveryMinutes:
          item.estimatedDeliveryMinutes || currentStoreInfo.estimatedDeliveryMinutes,
      });
      uni.redirectTo({
        url: `/pages/index/index?merchantId=${encodeURIComponent(merchantId)}&from=merchant`,
      });
    },
    goBack() {
      uni.navigateBack({
        delta: 1,
      });
    },
  },
  onLoad(options) {
    this.routeMerchantId = normalizeMerchantId(options && options.merchantId);
    this.loadMerchants();
  },
};
</script>

<style lang="scss" scoped>
.merchant-page {
  min-height: 100vh;
  background: #f6f2ea;
}

.merchant-page__body {
  padding: 120rpx 24rpx 40rpx;
}

.merchant-page__headline {
  padding: 20rpx 8rpx 28rpx;
}

.merchant-page__title {
  display: block;
  font-size: 40rpx;
  line-height: 52rpx;
  color: #23312c;
  font-weight: 700;
}

.merchant-page__subtitle {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  line-height: 36rpx;
  color: #73817c;
}

.merchant-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.merchant-card {
  position: relative;
  display: flex;
  gap: 20rpx;
  padding: 24rpx;
  border-radius: 30rpx;
  background: #fbfaf7;
  box-shadow: 0 18rpx 40rpx rgba(36, 54, 46, 0.08);
  border: 2rpx solid transparent;
}

.merchant-card.active {
  border-color: rgba(47, 93, 80, 0.24);
}

.merchant-card__logo {
  width: 112rpx;
  height: 112rpx;
  border-radius: 28rpx;
  flex-shrink: 0;
}

.merchant-card__logo--empty {
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(47, 93, 80, 0.12);
  color: #2f5d50;
  font-size: 36rpx;
  font-weight: 700;
}

.merchant-card__body {
  min-width: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.merchant-card__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.merchant-card__name {
  min-width: 0;
  flex: 1;
  font-size: 30rpx;
  line-height: 42rpx;
  color: #23312c;
  font-weight: 700;
}

.merchant-card__status {
  flex-shrink: 0;
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  background: rgba(47, 93, 80, 0.12);
  color: #2f5d50;
  font-size: 20rpx;
  line-height: 28rpx;
}

.merchant-card__status.rest {
  background: rgba(115, 129, 124, 0.14);
  color: #73817c;
}

.merchant-card__desc {
  display: block;
  margin-top: 10rpx;
  font-size: 24rpx;
  line-height: 36rpx;
  color: #73817c;
}

.merchant-card__meta {
  margin-top: 16rpx;
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;

  text {
    padding: 8rpx 14rpx;
    border-radius: 999rpx;
    background: rgba(47, 93, 80, 0.06);
    color: #2f5d50;
    font-size: 20rpx;
    line-height: 28rpx;
  }
}

.merchant-card__tag {
  position: absolute;
  top: 18rpx;
  right: 18rpx;
  padding: 8rpx 14rpx;
  border-radius: 999rpx;
  background: #2f5d50;
  color: #ffffff;
  font-size: 20rpx;
  line-height: 28rpx;
}

.merchant-empty {
  margin-top: 40rpx;
  padding: 100rpx 32rpx;
  border-radius: 30rpx;
  background: #fbfaf7;
  text-align: center;
  color: #73817c;
  font-size: 26rpx;
  line-height: 38rpx;
}
</style>
