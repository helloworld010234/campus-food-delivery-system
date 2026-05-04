<template>
  <view>
    <uni-nav-bar
      @clickLeft="goBack"
      left-icon="back"
      leftIcon="arrowleft"
      title="个人中心"
      statusBar="true"
      fixed="true"
      color="#ffffff"
      backgroundColor="#ffc200"
    ></uni-nav-bar>

    <view class="my-center">
      <head
        :psersonUrl="psersonUrl"
        :nickName="nickName"
        :gender="gender"
        :phoneNumber="phoneNumber"
        :getPhoneNum="getPhoneNum"
      ></head>

      <view class="container">
        <order-info @goAddress="goAddress" @goOrder="goOrder"></order-info>
        <view
          class="recent"
          v-if="recentOrdersList && recentOrdersList.length > 0"
        >
          <text class="order_line">最近订单</text>
        </view>
        <order-list
          :scrollH="scrollH"
          @lower="lower"
          @goDetail="goDetail"
          @oneOrderFun="oneOrderFun"
          @getOvertime="getOvertime"
          @statusWord="statusWord"
          @numes="numes"
          :loading="loading"
          :loadingText="loadingText"
          :recentOrdersList="recentOrdersList"
        ></order-list>
      </view>
    </view>
  </view>
</template>

<script>
import { getOrderPage, repetitionOrder, delShoppingCart } from "../api/api.js";
import { mapMutations } from "vuex";
import { statusWord, getOvertime } from "@/utils/index.js";
import {
  resolveMerchantIdFromOrder,
  withMerchantScope,
} from "../../utils/merchant.js";

import HeadInfo from "./components/headInfo.vue";
import OrderInfo from "./components/orderInfo.vue";
import OrderList from "./components/orderList.vue";

export default {
  data() {
    return {
      psersonUrl: "/static/imgDefault.png",
      nickName: "微信用户",
      gender: "0",
      phoneNumber: "",
      recentOrdersList: [],
      sumOrder: {
        amount: 0,
        number: 0,
      },
      status: "",
      scrollH: 0,
      pageInfo: {
        page: 1,
        pageSize: 10,
        total: 0,
      },
      loadingText: "",
      loading: false,
    };
  },
  components: {
    HeadInfo,
    OrderInfo,
    OrderList,
  },
  filters: {
    getPhoneNum(str = "") {
      return String(str).replace(/\-/g, "");
    },
  },
  onLoad() {
    if (!this.$store.state.token) {
      uni.redirectTo({
        url: "/pages/index/index",
      });
      return;
    }
    const baseUserInfo = this.$store.state.baseUserInfo || {};
    this.psersonUrl = baseUserInfo.avatarUrl || "/static/imgDefault.png";
    this.nickName = baseUserInfo.nickName || "微信用户";
    this.gender = String(baseUserInfo.gender || 0);
    this.phoneNumber = this.$store.state.shopPhone || "";
    this.getList();
  },
  created() {},
  onReady() {
    uni.getSystemInfo({
      success: (res) => {
        this.scrollH = res.windowHeight - uni.upx2px(100);
      },
    });
  },
  methods: {
    ...mapMutations([
      "setAddressBackUrl",
      "setCurrentMerchantId",
      "setStoreInfo",
    ]),
    statusWord(obj) {
      return statusWord(obj.status, obj.time);
    },
    getOvertime(time) {
      return getOvertime(time);
    },
    getList() {
      const params = {
        pageSize: 10,
        page: this.pageInfo.page,
      };
      getOrderPage(params)
        .then((res) => {
          if (res.code === 1) {
            this.recentOrdersList = this.recentOrdersList.concat(
              res.data.records
            );
            this.pageInfo.total = res.data.total;
            this.loadingText = "";
            this.loading = false;
          }
        })
        .catch(() => {
          this.loadingText = "";
          this.loading = false;
        });
    },
    goAddress() {
      this.setAddressBackUrl("/pages/my/my");
      uni.redirectTo({
        url: "/pages/address/address?form=" + "my",
      });
    },
    goOrder() {
      uni.navigateTo({
        url: "/pages/historyOrder/historyOrder",
      });
    },
    async oneOrderFun(order) {
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
    quitClick() {},
    goDetail(id) {
      this.setAddressBackUrl("/pages/my/my");
      uni.redirectTo({
        url: "/pages/details/index?orderId=" + id,
      });
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
    goBack() {
      uni.redirectTo({
        url: "/pages/index/index",
      });
    },
  },
};
</script>
<style lang="scss" scoped>
.my-center {
  background: #f6f6f6;
  height: 100%;

  .container {
    margin-top: 20rpx;
    height: calc(100% - 194rpx);
  }
}
::v-deep .uni-navbar--border {
  border-width: 0 !important;
}
</style>
