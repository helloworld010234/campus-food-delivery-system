<template>
  <view class="order_page">
    <uni-nav-bar
      @clickLeft="goBack"
      left-icon="back"
      leftIcon="arrowleft"
      title="确认订单"
      statusBar="true"
      fixed="true"
      color="#ffffff"
      backgroundColor="#2F5D50"
    ></uni-nav-bar>

    <view class="order_content" @touchstart="touchstart">
      <view class="checkout_banner">
        <text class="banner_eyebrow">校内食堂 · 宿舍直送</text>
        <text class="banner_title">确认一下送达地点和时间</text>
        <text class="banner_desc">
          建议优先选择宿舍楼下或教学楼门口，骑手联系和取餐都会更顺畅。
        </text>
      </view>

      <view class="order_content_box">
        <address-pop
          :address="address"
          :tagLabel="tagLabel"
          :addressLabel="addressLabel"
          :nickName="nickName"
          :phoneNumber="phoneNumber"
          :arrivalTime="arrivalTime"
          :popleft="popleft"
          :weeks="weeks"
          :newDateData="newDateData"
          :tabIndex="tabIndex"
          :selectValue="selectValue"
          @change="change"
          @goAddress="goAddress"
          @dateChange="dateChange"
          @timeClick="timeClick"
        ></address-pop>

        <dish-detail
          :orderDataes="orderDataes"
          :showDisplay="showDisplay"
          :orderDishNumber="orderDishNumber"
          :orderListDataes="orderListDataes"
          :orderDishPrice="orderDishPrice"
        ></dish-detail>

        <dish-info
          ref="dishinfo"
          :remark="remark"
          :tablewareData="tablewareData"
          :radioGroup="radioGroup"
          :activeRadio="activeRadio"
          :baseData="baseData"
          @goRemark="goRemark"
          @openPopuos="openPopuos"
          @change="change"
          @closePopup="closePopup"
          @handlePiker="handlePiker"
          @changeCont="changeCont"
          @handleRadio="handleRadio"
        ></dish-info>
      </view>

      <view class="footer_order_buttom order_form">
        <view class="pay_summary">
          <text class="pay_label">应付金额</text>
          <text class="pay_desc">共 {{ orderDishNumber }} 件商品，支持微信支付</text>
        </view>
        <view class="pay_price">
          <text class="ico">¥</text>{{ orderDishPrice.toFixed(2) }}
        </view>
        <view
          class="order_but"
          :class="{ 'order_but--disabled': isHandlePy }"
          @click="!isHandlePy && payOrderHandle()"
        >
          {{ isHandlePy ? "提交中..." : "微信支付" }}
        </view>
      </view>
    </view>
  </view>
</template>
<script src="./index.js"></script>
<style src="./style.scss" lang="scss" scoped></style>
