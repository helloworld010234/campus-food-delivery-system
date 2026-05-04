<template>
  <view class="address_card">
    <view class="card_row" @click="goAddress">
      <view class="row_copy">
        <text class="row_label">送达地点</text>
        <view v-if="address" class="address_block">
          <view class="address_main">
            <text class="tag" :class="'tag' + tagLabel">{{ addressLabel || "校园" }}</text>
            <text class="word">{{ address }}</text>
          </view>
          <view class="contact_line">{{ cryptoName }} {{ phoneNumber }}</view>
          <view class="card_tip">
            建议填写宿舍楼下或教学楼门口，方便骑手快速联系你。
          </view>
        </view>
        <view v-else class="row_empty">请选择送达地点</view>
      </view>
      <view class="row_arrow">›</view>
    </view>

    <view class="card_divider"></view>

    <view class="card_row" @click="openTimePopuo('bottom')">
      <view class="row_copy">
        <text class="row_label">期望送达时间</text>
        <view class="time_line">
          <text class="time_value">
            {{ arrivalTime ? `立即送出 · ${arrivalTime}` : "立即送出" }}
          </text>
        </view>
        <view class="card_tip">午高峰时段送达时间可能略有波动</view>
      </view>
      <view class="row_arrow">›</view>
    </view>

    <uni-popup ref="timePopup" @change="change" class="popupBox">
      <view class="popup-content">
        <view class="popup_title">选择送达时间</view>
        <view class="pickerCon">
          <view class="dayBox">
            <scroll-view scroll-x="true" :scroll-into-view="scrollinto" :scroll-with-animation="true">
              <view
                v-for="(item, index) in popleft"
                :key="index"
                :id="'tab' + index"
                class="scroll-row-item"
                @click="dateChange(index)"
              >
                <view v-for="(val, i) in weeks" :key="i">
                  <view :class="tabIndex == index ? 'scroll-row-day' : ''" v-if="index === i">
                    {{ item }}<text class="week">（{{ val }}）</text>
                  </view>
                </view>
              </view>
            </scroll-view>
          </view>
          <view class="timeBox">
            <scroll-view class="card_order_list" scroll-y="true" scroll-top="0">
              <view
                v-for="(val, i) in newDateData"
                :key="i"
                class="item"
                :class="selectValue === i ? 'city-column_select' : ''"
                @click="timeClick(val, i)"
              >
                {{ val }}
              </view>
            </scroll-view>
          </view>
        </view>
        <view class="btns" @click="onsuer">完成</view>
      </view>
    </uni-popup>
  </view>
</template>

<script>
export default {
  props: {
    tagLabel: {
      type: String,
      default: "",
    },
    addressLabel: {
      type: String,
      default: "",
    },
    address: {
      type: String,
      default: "",
    },
    nickName: {
      type: String,
      default: "",
    },
    gender: {
      type: Number,
      default: -1,
    },
    phoneNumber: {
      type: String,
      default: "",
    },
    arrivalTime: {
      type: String,
      default: "",
    },
    tabIndex: {
      type: Number,
      default: 0,
    },
    selectValue: {
      type: Number,
      default: 0,
    },
    popleft: {
      type: Array,
      default: () => [],
    },
    weeks: {
      type: Array,
      default: () => [],
    },
    newDateData: {
      type: Array,
      default: () => [],
    },
  },
  computed: {
    cryptoName() {
      if (!this.nickName) {
        return "同学"
      }
      return `${this.nickName.charAt(0)}${this.gender === 0 ? "先生" : "同学"}`
    },
  },
  methods: {
    goAddress() {
      this.$emit("goAddress")
    },
    openTimePopuo(type) {
      this.$refs.timePopup.open(type)
    },
    change() {
      this.$emit("change")
    },
    dateChange(index) {
      this.$emit("dateChange", index)
    },
    timeClick(val, i) {
      this.$emit("timeClick", { val: val, i: i })
      this.onsuer()
    },
    onsuer(type) {
      this.$refs.timePopup.close(type)
    },
  },
}
</script>

<style lang="scss" scoped>
$primary: #2f5d50;
$text: #23312c;
$muted: #73817c;
$accent: #f4a259;

.address_card {
  margin-bottom: 22rpx;
  padding: 8rpx 28rpx;
  border-radius: 32rpx;
  background: #fbfaf7;
  box-shadow: 0 20rpx 50rpx rgba(36, 54, 46, 0.08);
}

.card_row {
  display: flex;
  align-items: flex-start;
  gap: 18rpx;
  padding: 26rpx 0;
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

.address_block,
.time_line {
  margin-top: 14rpx;
}

.address_main {
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
}

.tag {
  margin-top: 6rpx;
  padding: 6rpx 14rpx;
  border-radius: 999rpx;
  background: rgba(244, 162, 89, 0.16);
  color: $accent;
  font-size: 18rpx;
  line-height: 26rpx;
  flex-shrink: 0;
}

.word,
.time_value {
  font-size: 30rpx;
  line-height: 42rpx;
  color: $text;
  font-weight: 700;
}

.contact_line {
  margin-top: 12rpx;
  font-size: 24rpx;
  line-height: 34rpx;
  color: $text;
}

.card_tip {
  margin-top: 12rpx;
  font-size: 22rpx;
  line-height: 32rpx;
  color: $muted;
}

.row_empty {
  margin-top: 12rpx;
  font-size: 28rpx;
  line-height: 40rpx;
  color: $muted;
}

.row_arrow {
  flex-shrink: 0;
  font-size: 40rpx;
  line-height: 48rpx;
  color: rgba(35, 49, 44, 0.28);
  padding-top: 34rpx;
}

.card_divider {
  height: 1rpx;
  background: rgba(47, 93, 80, 0.08);
}

.popup-content {
  background: #fbfaf7;
  border-radius: 32rpx 32rpx 0 0;
  padding: 28rpx 24rpx calc(24rpx + env(safe-area-inset-bottom));
}

.popup_title {
  text-align: center;
  font-size: 30rpx;
  line-height: 40rpx;
  color: $text;
  font-weight: 700;
}

.pickerCon {
  margin-top: 22rpx;
  display: flex;
  height: 460rpx;
  border-radius: 24rpx;
  overflow: hidden;
  background: #ffffff;
}

.dayBox {
  width: 42%;
  background: rgba(47, 93, 80, 0.05);
}

.scroll-row-item {
  font-size: 24rpx;
  line-height: 34rpx;
  color: $muted;
}

.scroll-row-day {
  padding: 24rpx 20rpx;
  background: #ffffff;
  color: $text;
  font-weight: 700;
}

.week {
  margin-left: 8rpx;
}

.timeBox {
  flex: 1;
}

.card_order_list {
  height: 100%;
}

.item {
  padding: 24rpx 28rpx;
  font-size: 26rpx;
  line-height: 36rpx;
  color: $text;
}

.city-column_select {
  background: rgba(244, 162, 89, 0.12);
  color: #d36e32;
  font-weight: 700;
}

.btns {
  margin-top: 20rpx;
  height: 84rpx;
  border-radius: 24rpx;
  background: $primary;
  color: #ffffff;
  text-align: center;
  line-height: 84rpx;
  font-size: 28rpx;
  font-weight: 600;
}
</style>
