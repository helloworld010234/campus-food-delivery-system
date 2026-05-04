<!--鍘嗗彶璁㈠崟-->
<template>
  <view class="history_order">
    <uni-nav-bar
      @clickLeft="goBack"
      left-icon="back"
      leftIcon="arrowleft"
      title="历史订单"
      statusBar="true"
      fixed="true"
      color="#ffffff"
      backgroundColor="#333333"
    ></uni-nav-bar>
    <scroll-view
      scroll-x
      class="scroll-row"
      :scroll-into-view="scrollinto"
      :scroll-with-animation="true"
      enable-flex
    >
      <view
        v-for="(item, index) in tabBars"
        :key="index"
        :id="'tab' + index"
        class="scroll-row-item"
        @click="changeTab(index)"
      >
        <view :class="tabIndex == index ? 'scroll-row-item-act' : ''">
          <text class="line"></text>{{ item }}
        </view>
      </view>
    </scroll-view>
    <swiper
      :current="tabIndex"
      @change="onChangeSwiperTab"
      :style="{ height: scrollH + 'px' }"
    >
      <swiper-item v-for="(item, index) in tabBars" :key="index">
        <scroll-view
          scroll-y="true"
          :style="{ height: scrollH + 'px' }"
          @scrolltolower="lower"
        >
          <view
            class="main recent_orders"
            v-if="recentOrdersList && recentOrdersList.length > 0"
          >
            <view
              class="box order_lists"
              v-for="(item, index) in recentOrdersList"
              :key="index"
              :class="{ 'item-last': Number(index) + 1 === recentOrdersList.length }"
            >
              <view class="date_type">
                <text class="time">{{ item.orderTime }}</text>
                <text class="type status" :class="{ status: item.status == 2 }">{{
                  statusWord(item.status)
                }}</text>
              </view>
              <view class="orderBox" @click="goDetail(item.id)">
                <view class="food_num">
                  <scroll-view
                    scroll-x="true"
                    class="pic"
                    style="width: 100%; overflow: hidden; white-space: nowrap"
                  >
                    <view
                      class="food_num_item"
                      v-for="(num, y) in item.orderDetailList"
                      :key="y"
                    >
                      <view class="img">
                        <image :src="num.image"></image>
                      </view>
                      <view class="food">{{ num.name }}</view>
                    </view>
                  </scroll-view>
                </view>
                <view class="numAndAum">
                  <view><text>￥{{ item.amount.toFixed(2) }}</text></view>
                  <view><text>共{{ numes(item.orderDetailList).count }}件</text></view>
                </view>
              </view>
              <view class="againBtn">
                <button class="new_btn" type="default" @click="oneMoreOrder(item)">
                  再来一单
                </button>
                <button
                  class="new_btn btn"
                  type="default"
                  @click="goDetail(item.id)"
                  v-if="item.status === 1 && getOvertime(item.orderTime) > 0"
                >
                  去支付
                </button>
                <button
                  class="new_btn btn"
                  type="default"
                  @click="handleReminder('center', item.id)"
                  v-if="item.status === 2"
                >
                  催单
                </button>
              </view>
            </view>
          </view>
        </scroll-view>
      </swiper-item>
    </swiper>
    <uni-popup ref="commonPopup" class="comPopupBox">
      <view class="popup-content">
        <view class="text">{{ textTip }}</view>
        <view class="btn" v-if="showConfirm">
          <view @click="closePopup">确认</view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script>
import {
  getOrderPage,
  repetitionOrder,
  reminderOrder,
  delShoppingCart,
  queryOrdersCheckStatus,
} from "../api/api.js";
import { mapMutations } from "vuex";
import Empty from "@/components/empty/empty";
import { statusWord, getOvertime } from "@/utils/index.js";
import {
  resolveMerchantIdFromOrder,
  withMerchantScope,
} from "../../utils/merchant.js";

export default {
  components: {
    Empty,
  },
  data() {
    return {
      recentOrdersList: [],
      pageInfo: {
        page: 1,
        pageSize: 10,
        total: 0,
      },
      status: "",
      payStatus: "",
      loadingType: 0,
      showTitle: false,
      scrollinto: "tab0",
      scrollH: 0,
      tabIndex: 0,
      tabBars: ["全部订单", "待付款", "退款"],
      urlMap: {
        0: {
          fn: getOrderPage,
          key: "status",
        },
        1: {
          fn: getOrderPage,
          key: "status",
        },
        2: {
          fn: queryOrdersCheckStatus,
          key: "payStatus",
        },
      },
      textTip: "",
      showConfirm: false,
      isEmpty: false,
    };
  },
  onLoad() {
    if (!this.$store.state.token) {
      uni.redirectTo({
        url: "/pages/index/index",
      });
      return;
    }
    this.getList();
  },
  onUnload() {
    this.showTitle = false;
  },
  onReady() {
    uni.getSystemInfo({
      success: (res) => {
        this.scrollH = res.windowHeight - uni.upx2px(100);
      },
    });
  },
  onPullDownRefresh() {
    this.pageInfo.page = 1;
    this.loadingType = 0;
    this.recentOrdersList = [];
    this.finished = false;
    this.getList();
    uni.stopPullDownRefresh();
    this.showTitle = true;
  },
  onReachBottom() {
    if (this.recentOrdersList.length < Number(this.pageInfo.total)) {
      this.pageInfo.page++;
      this.loadingStatus = "loading";
      this.getList(this.status);
      this.showTitle = true;
    }
  },
  methods: {
    ...mapMutations([
      "setAddressBackUrl",
      "setCurrentMerchantId",
      "setStoreInfo",
    ]),
    numes(list) {
      let count = 0;
      let total = 0;
      list.length > 0 &&
        list.forEach((obj) => {
          count += Number(obj.number);
          total += Number(obj.number) * Number(obj.amount);
        });
      return { count: count, total: total };
    },
    statusWord(status) {
      return statusWord(status);
    },
    getOvertime(time) {
      return getOvertime(time);
    },
    getList() {
      const key = this.urlMap[this.tabIndex].key;
      const fn = this.urlMap[this.tabIndex].fn;
      const params = {
        pageSize: 10,
        page: this.pageInfo.page,
      };
      params[key] = this[key];
      uni.showLoading({ title: "加载中", mask: true });
      fn(params)
        .then((res) => {
          if (res.code === 1) {
            setTimeout(function () {
              uni.hideLoading();
            }, 100);
            this.recentOrdersList = this.recentOrdersList.concat(
              res.data.records
            );
            this.pageInfo.total = res.data.total;
            this.isEmpty = true;
          }
        })
        .catch(() => {
          uni.hideLoading();
        });
    },
    async oneMoreOrder(order) {
      const merchantId = resolveMerchantIdFromOrder(order);
      await delShoppingCart(withMerchantScope({}, merchantId));
      repetitionOrder(order.id).then((res) => {
        if (res.code === 1) {
          const currentStoreInfo = this.$store.state.storeInfo || {};
          this.setCurrentMerchantId(merchantId);
          this.setStoreInfo({
            ...currentStoreInfo,
            merchantId,
            shopId: merchantId,
          });
          uni.redirectTo({
            url:
              "/pages/index/index?merchantId=" +
              encodeURIComponent(merchantId),
          });
        }
      });
    },
    changeTab(index) {
      if (this.tabIndex == index) {
        return;
      }
      this.tabIndex = index;
      if (index === 1) {
        this.status = 1;
        this.payStatus = 0;
      } else if (index === 2) {
        this.status = 6;
        this.payStatus = 2;
      } else {
        this.status = "";
        this.payStatus = "";
      }
      this.pageInfo.page = 1;
      this.recentOrdersList = [];
      this.getList();
      this.scrollinto = "tab" + index;
    },
    onChangeSwiperTab(e) {
      this.changeTab(e.detail.current);
    },
    dataAdd() {
      const pages = Math.ceil(this.pageInfo.total / 10);
      if (this.pageInfo.page === pages) {
        this.loadingText = "没有更多了";
        this.loading = true;
      } else {
        this.pageInfo.page++;
        this.getList();
      }
    },
    lower() {
      this.loadingText = "数据加载中...";
      this.loading = true;
      this.dataAdd();
    },
    goDetail(id) {
      this.setAddressBackUrl("/pages/historyOrder/historyOrder");
      uni.navigateTo({ url: "/pages/details/index?orderId=" + id });
    },
    handleReminder(type, id) {
      reminderOrder(id).then((res) => {
        if (res.code === 1) {
          this.showConfirm = true;
          this.textTip = "您的催单信息已发送！";
          this.$refs.commonPopup.open(type);
          this.getList(this.status);
        }
      });
    },
    closePopup(type) {
      this.$refs.commonPopup.close(type);
    },
    goBack() {
      uni.redirectTo({
        url: "/pages/my/my",
      });
    },
  },
};
</script>

<style lang="scss" scoped>
.history_order {
  height: 100%;

  .recent_orders {
    padding-top: 8rpx;
  }
}

.scroll-row {
  height: 88rpx;
  line-height: 88rpx;
  background-color: #fff;
  padding: 0 30rpx;
  box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.06);
  width: 100vw;
  box-sizing: border-box;
  flex-wrap: nowrap;
  overflow: auto;
  display: flex;
}

.scroll-row-item {
  margin-right: 88rpx;
  color: #666;
  display: inline-block;
  font-size: 28rpx;
  flex-shrink: 0;
}

.scroll-row-item-act {
  color: #333;
  position: relative;
  font-weight: 600;

  .line {
    width: 32rpx;
    height: 8rpx;
    display: block;
    background: #ffc200;
    border-radius: 8rpx;
    transform: translate(-50%, -50%);
    position: absolute;
    bottom: -4rpx;
    left: 50%;
  }
}
</style>
