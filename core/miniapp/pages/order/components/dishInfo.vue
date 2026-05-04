<template>
  <view class="info_card">
    <view class="info_row" @click="goRemark">
      <view class="row_copy">
        <text class="row_label">备注</text>
        <text class="row_value">{{ remark || "口味偏好、楼栋说明、电话联系等" }}</text>
      </view>
      <view class="row_arrow">›</view>
    </view>

    <view class="info_row" @click="openPopuos('bottom')">
      <view class="row_copy">
        <text class="row_label">餐具数量</text>
        <text class="row_value">{{ tablewareData }}</text>
      </view>
      <view class="row_arrow">›</view>
    </view>

    <view class="info_row info_row--static">
      <view class="row_copy">
        <text class="row_label">发票</text>
        <text class="row_value">如需发票，请联系商家</text>
      </view>
    </view>

    <view class="delivery_tip">
      配送说明：如果宿舍门禁不便进入，骑手会优先电话联系你到楼下取餐。
    </view>

    <uni-popup ref="popup" @change="change" class="popupBox">
      <view class="popup-content">
        <view class="popupTitle">
          根据校园点单习惯，请按需选择本单餐具数量
        </view>
        <view class="popupCon">
          <view class="popupBtn">
            <text @click="closePopup">取消</text>
            <text>选择本单餐具</text>
            <text @click="handlePiker">确定</text>
          </view>
          <pikers :baseData="baseData" ref="piker" @changeCont="changeCont"></pikers>
        </view>
        <view class="popupSet">
          <view class="popupSetTitle">后续订单餐具设置</view>
          <radio-group @change="handleRadio" class="radioGroup">
            <label v-for="item in radioGroup" :key="item" class="radioItem">
              <radio :value="item" color="#2F5D50" :checked="item == activeRadio" />
              {{ item }}
            </label>
          </radio-group>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script>
import Pikers from "@/components/uni-piker/index.vue"

export default {
  props: {
    remark: {
      type: String,
      default: "",
    },
    tablewareData: {
      type: String,
      default: "",
    },
    radioGroup: {
      type: Array,
      default: () => [],
    },
    activeRadio: {
      type: String,
      default: "",
    },
    baseData: {
      type: Array,
      default: () => [],
    },
  },
  components: { Pikers },
  methods: {
    goRemark() {
      this.$emit("goRemark")
    },
    openPopuos(type) {
      this.$refs.popup.open(type)
    },
    change() {
      this.$emit("change")
    },
    closePopup(type) {
      this.$refs.popup.close(type)
    },
    handlePiker() {
      this.$emit("handlePiker")
      this.closePopup()
    },
    changeCont(val) {
      this.$emit("changeCont", val)
    },
    handleRadio(e) {
      this.$emit("handleRadio", e)
    },
  },
}
</script>

<style lang="scss" scoped>
$primary: #2f5d50;
$text: #23312c;
$muted: #73817c;
$accent: #f4a259;

.info_card {
  margin-bottom: 22rpx;
  padding: 8rpx 28rpx 28rpx;
  border-radius: 32rpx;
  background: #fbfaf7;
  box-shadow: 0 20rpx 50rpx rgba(36, 54, 46, 0.08);
}

.info_row {
  display: flex;
  align-items: center;
  gap: 18rpx;
  padding: 26rpx 0;
  border-bottom: 1rpx solid rgba(47, 93, 80, 0.08);
}

.info_row--static {
  border-bottom: 0;
}

.row_copy {
  min-width: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.row_label {
  font-size: 24rpx;
  line-height: 34rpx;
  color: $accent;
  font-weight: 600;
}

.row_value {
  margin-top: 12rpx;
  font-size: 28rpx;
  line-height: 40rpx;
  color: $text;
}

.row_arrow {
  flex-shrink: 0;
  font-size: 40rpx;
  line-height: 48rpx;
  color: rgba(35, 49, 44, 0.28);
}

.delivery_tip {
  margin-top: 20rpx;
  padding: 20rpx 22rpx;
  border-radius: 24rpx;
  background: rgba(47, 93, 80, 0.06);
  font-size: 22rpx;
  line-height: 34rpx;
  color: $muted;
}

.popup-content {
  background: #fbfaf7;
  border-radius: 32rpx 32rpx 0 0;
  padding: 28rpx 24rpx calc(24rpx + env(safe-area-inset-bottom));
}

.popupTitle {
  font-size: 26rpx;
  line-height: 38rpx;
  color: $muted;
  text-align: center;
}

.popupCon {
  margin-top: 22rpx;
  overflow: hidden;
  border-radius: 28rpx;
  background: #ffffff;
}

.popupBtn {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 26rpx 24rpx;
  border-bottom: 1rpx solid rgba(47, 93, 80, 0.08);
  font-size: 26rpx;
  line-height: 36rpx;
  color: $text;
  font-weight: 600;
}

.popupSet {
  margin-top: 20rpx;
  padding: 24rpx;
  border-radius: 28rpx;
  background: #ffffff;
}

.popupSetTitle {
  font-size: 24rpx;
  line-height: 34rpx;
  color: $muted;
}

.radioGroup {
  margin-top: 18rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.radioItem {
  font-size: 26rpx;
  line-height: 36rpx;
  color: $text;
  display: flex;
  align-items: center;
}
</style>
