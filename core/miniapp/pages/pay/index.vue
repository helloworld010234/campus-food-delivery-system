<template>
  <view class="pay-page">
    <uni-nav-bar
      @clickLeft="goBack"
      left-icon="back"
      leftIcon="arrowleft"
      title="微信支付"
      statusBar="true"
      fixed="true"
      color="#ffffff"
      backgroundColor="#2F5D50"
    ></uni-nav-bar>

    <view class="pay-content">
      <view class="pay-hero">
        <text class="pay-eyebrow">订单已提交</text>
        <text class="pay-title">{{ timeout ? "订单支付已超时" : "请尽快完成支付" }}</text>
        <text class="pay-desc" v-if="!timeout">
          支付剩余时间
          <text class="pay-countdown">{{ rocallTime }}</text>
        </text>
        <text class="pay-desc" v-else>超时后可返回订单详情重新处理</text>
      </view>

      <view class="pay-card">
        <view class="amount-label">应付金额</view>
        <view class="amount-value">
          <text class="currency">¥</text>{{ orderDataInfo.orderAmount }}
        </view>
        <view class="order-meta">
          {{ shopInfo().shopName }} · {{ orderDataInfo.orderNumber }}
        </view>
      </view>

      <view class="method-card">
        <view class="method-title">支付方式</view>
        <radio-group class="method-list" @change="styleChange">
          <label
            class="method-item"
            v-for="(item, index) in payMethodList"
            :key="item"
          >
            <view class="method-info">
              <view class="wechat-icon">微</view>
              <text class="method-name">{{ item }}</text>
            </view>
            <radio
              :value="item"
              color="#2F5D50"
              :checked="index === activeRadio"
              class="radioIcon"
            />
          </label>
        </radio-group>
      </view>
    </view>

    <view class="pay-footer">
      <button class="pay-btn" type="primary" plain="true" @click="handleSave">
        {{ timeout ? "返回订单详情" : "确认支付" }}
      </button>
    </view>
  </view>
</template>

<script>
import { mapState } from "vuex";
import { paymentOrder, cancelOrder } from "@/pages/api/api.js";

export default {
  data() {
    return {
      timeout: false,
      rocallTime: "",
      orderId: null,
      orderDataInfo: {},
      activeRadio: 0,
      payMethodList: ["微信支付"],
      times: null,
    };
  },
  created() {
    this.orderDataInfo = this.orderData();
  },
  mounted() {
    this.runTimeBack();
  },
  onLoad(options) {
    this.orderId = options.orderId;
  },
  methods: {
    ...mapState(["orderData", "shopInfo"]),
    goBack() {
      uni.navigateBack({ delta: 1 });
    },
    styleChange(e) {
      const index = this.payMethodList.findIndex((item) => item === e.detail.value);
      this.activeRadio = index === -1 ? 0 : index;
    },
    handleSave() {
      if (this.timeout) {
        cancelOrder(this.orderId).then(() => {});
        uni.redirectTo({
          url: "/pages/details/index?orderId=" + this.orderId,
        });
        return;
      }

      clearTimeout(this.times);
      const params = {
        orderNumber: this.orderDataInfo.orderNumber,
        payMethod: this.activeRadio === 0 ? 1 : 2,
      };

      paymentOrder(params).then(async (res) => {
        if (res.code !== 1) {
          uni.showToast({
            title: res.msg,
            duration: 1000,
            icon: "none",
          });
          return;
        }

        if (res.data && res.data.mockPay) {
          await uni.showToast({ title: "Paid", icon: "success" });
          setTimeout(() => {
            uni.redirectTo({
              url: "/pages/success/index?orderId=" + this.orderId,
            });
          }, 1500);
          return;
        }

        const [err] = await uni.requestPayment({
          ...res.data,
          package: res.data.packageStr,
        });

        if (err) {
          await uni.showToast({ title: "Failed", icon: "error" });
          setTimeout(() => {
            uni.redirectTo({
              url: "/pages/details/index?orderId=" + this.orderId,
            });
          }, 1500);
          return;
        }

        await uni.showToast({ title: "Paid", icon: "success" });
        setTimeout(() => {
          uni.redirectTo({
            url: "/pages/success/index?orderId=" + this.orderId,
          });
        }, 1500);
      });
    },
    runTimeBack() {
      const end = Date.parse(this.orderDataInfo.orderTime.replace(/-/g, "/"));
      const now = Date.parse(new Date());
      const m15 = 15 * 60 * 1000;
      const msec = m15 - (now - end);
      if (msec < 0) {
        this.timeout = true;
        clearTimeout(this.times);
      } else {
        let min = parseInt((msec / 1000 / 60) % 60);
        let sec = parseInt((msec / 1000) % 60);
        if (min < 10) {
          min = "0" + min;
        }
        if (sec < 10) {
          sec = "0" + sec;
        }
        this.rocallTime = min + ":" + sec;
        if (min >= 0 && sec >= 0) {
          if (min === 0 && sec === 0) {
            this.timeout = true;
            clearTimeout(this.times);
            return;
          }
          this.times = setTimeout(() => {
            this.runTimeBack();
          }, 1000);
        }
      }
    },
  },
};
</script>

<style scoped lang="scss">
.pay-page {
  min-height: 100vh;
  background: #f6f2ea;
}

.pay-content {
  padding: 176rpx 24rpx 200rpx;
}

.pay-hero {
  padding: 30rpx;
  border-radius: 32rpx;
  background: linear-gradient(145deg, #2f5d50 0%, #203d34 100%);
  color: #ffffff;
  box-shadow: 0 24rpx 60rpx rgba(24, 49, 41, 0.18);
}

.pay-eyebrow {
  font-size: 22rpx;
  line-height: 30rpx;
  color: rgba(244, 162, 89, 0.96);
  font-weight: 600;
}

.pay-title {
  display: block;
  margin-top: 14rpx;
  font-size: 40rpx;
  line-height: 52rpx;
  font-weight: 700;
}

.pay-desc {
  display: block;
  margin-top: 14rpx;
  font-size: 24rpx;
  line-height: 38rpx;
  color: rgba(255, 255, 255, 0.76);
}

.pay-countdown {
  color: #f4a259;
  font-weight: 700;
}

.pay-card,
.method-card {
  margin-top: 22rpx;
  padding: 30rpx 28rpx;
  border-radius: 32rpx;
  background: #fbfaf7;
  box-shadow: 0 20rpx 50rpx rgba(36, 54, 46, 0.08);
}

.amount-label,
.method-title {
  font-size: 24rpx;
  line-height: 34rpx;
  color: #f4a259;
  font-weight: 600;
}

.amount-value {
  margin-top: 14rpx;
  font-size: 54rpx;
  line-height: 66rpx;
  color: #23312c;
  font-weight: 700;
}

.currency {
  font-size: 28rpx;
}

.order-meta {
  margin-top: 10rpx;
  font-size: 24rpx;
  line-height: 36rpx;
  color: #73817c;
}

.method-list {
  margin-top: 18rpx;
}

.method-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
  padding: 20rpx 0;
}

.method-info {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.wechat-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 20rpx;
  background: rgba(47, 93, 80, 0.08);
  color: #2f5d50;
  font-size: 28rpx;
  line-height: 64rpx;
  text-align: center;
  font-weight: 700;
}

.method-name {
  font-size: 28rpx;
  line-height: 38rpx;
  color: #23312c;
  font-weight: 600;
}

.pay-footer {
  position: fixed;
  left: 24rpx;
  right: 24rpx;
  bottom: calc(28rpx + env(safe-area-inset-bottom));
}

.pay-btn {
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 999rpx;
  background: #f4a259;
  border: 0;
  color: #ffffff;
  font-size: 28rpx;
  font-weight: 700;
}

.pay-btn::after {
  border: 0;
}
</style>
