<template>
  <view class="campus-page">
    <nav-bar></nav-bar>
    <scroll-view
      class="page_scroll"
      scroll-y
      :scroll-into-view="pageIntoView"
      scroll-with-animation
    >
      <view class="home_content" :style="homeContentStyle">
        <view class="campus_hero">
          <view class="hero_top">
            <view class="location_chip">
              <text class="location_label">当前送达</text>
              <text class="location_value">{{ campusLocation }}</text>
            </view>
            <view class="status_chip" :class="{ close: shopStatus !== 1 }">
              {{ shopStatusText }}
            </view>
          </view>

          <view class="hero_copy">
            <text class="hero_title">杏林校园外卖</text>
            <text class="hero_subtitle">校内食堂 · 宿舍直送</text>
            <text class="hero_desc">
              下课前点好，热饭和饮品会更从容地送到楼下。
            </text>
          </view>

          <view class="hero_search" @click="jumpToMenu">
            <text class="search_icon">搜</text>
            <text class="search_text">搜盖饭、奶茶、夜宵、轻食</text>
          </view>

          <view class="hero_tag_list">
            <view
              v-for="(tag, index) in campusHeroTags"
              :key="index"
              class="hero_tag"
              :class="{ muted: tag.muted }"
            >
              {{ tag.label }}
            </view>
          </view>
          <view class="hero_entry_row">
            <view class="hero_entry hero_entry--primary" @click="goMyCenter">
              <view class="hero_entry_avatar">我</view>
              <view class="hero_entry_text">
                <text class="hero_entry_title">我的</text>
                <text class="hero_entry_desc">个人中心</text>
              </view>
            </view>
            <view class="hero_entry" @click="goHistoryOrders">
              <view class="hero_entry_avatar hero_entry_avatar--soft">单</view>
              <view class="hero_entry_text">
                <text class="hero_entry_title">订单</text>
                <text class="hero_entry_desc">查看历史订单</text>
              </view>
            </view>
          </view>
        </view>

        <view class="campus_banner">
          <view class="banner_copy">
            <text class="banner_eyebrow">午高峰稳送宿舍区</text>
            <text class="banner_title">下课前点好，平均 26 分钟送到楼下</text>
            <text class="banner_desc">
              默认优先派送宿舍楼下和教学楼门口，楼栋取餐更轻松。
            </text>
          </view>
          <view class="banner_action" @click="jumpToMenu">进入点单</view>
        </view>

        <view class="campus_section">
          <view class="section_heading">
            <view>
              <text class="section_eyebrow">校园频道</text>
              <text class="section_title">下课、回宿舍、晚自习都能顺手点</text>
            </view>
          </view>
          <view class="channel_grid">
            <view
              v-for="(item, index) in campusChannels"
              :key="item.key"
              class="channel_card"
              @click="campusChannelTap(index)"
            >
              <view class="channel_icon">{{ item.short }}</view>
              <text class="channel_title">{{ item.title }}</text>
              <text class="channel_desc">{{ item.desc }}</text>
            </view>
          </view>
        </view>

        <view class="campus_section">
          <view class="section_heading">
            <view>
              <text class="section_eyebrow">为你推荐</text>
              <text class="section_title">校园常点搭配，先把热销菜看一眼</text>
            </view>
          </view>
          <view class="recommend_list">
            <view
              v-for="(card, index) in campusRecommendCards"
              :key="index"
              class="recommend_card"
              @click="campusChannelTap(index)"
            >
              <image
                v-if="card.image"
                class="recommend_cover"
                mode="aspectFill"
                :src="card.image"
              ></image>
              <view v-else class="recommend_cover recommend_cover--empty">
                {{ card.badge }}
              </view>
              <view class="recommend_body">
                <text class="recommend_badge">{{ card.badge }}</text>
                <text class="recommend_title">{{ card.title }}</text>
                <text class="recommend_desc">{{ card.desc }}</text>
                <view class="recommend_price">
                  <text class="ico">¥</text>
                  <text>{{ card.price }}</text>
                  <text> 起</text>
                </view>
              </view>
            </view>
          </view>
        </view>

        <view id="menu-anchor" class="menu_shell">
          <view
            v-if="hasMerchantSwitch"
            class="merchant_switcher"
            @click="goMerchantList"
          >
            <view class="merchant_switcher_info">
              <text class="merchant_switcher_label">当前商家</text>
              <text class="merchant_switcher_name">{{ menuTitle }}</text>
            </view>
            <view class="merchant_switcher_action">切换商家</view>
          </view>
          <view class="menu_header">
            <view class="menu_intro">
              <text class="section_eyebrow">今日营业窗口</text>
              <text class="menu_title">{{ menuTitle }}</text>
              <text class="menu_desc">{{ menuDescription }}</text>
              <view class="menu_meta">
                <text class="menu_meta_tag">宿舍直送</text>
                <text class="menu_meta_tag">{{ estimatedDeliveryMinutesText }}</text>
                <text class="menu_meta_tag">{{ menuDeliveryFeeText }}</text>
              </view>
            </view>
            <view class="menu_call" @click="handlePhone('bottom')">联系商家</view>
          </view>

          <view v-if="shopStatus === 1">
            <view v-if="showInlineMenuLoading" class="menu_loading">
              <image
                class="menu_loading_icon"
                src="../../static/lodding.gif"
              ></image>
              <text class="menu_loading_text">正在同步最新菜单...</text>
            </view>

            <view v-else-if="hasMenuCategories" class="restaurant_menu_list">
              <view class="type_list">
              <scroll-view
                class="menu-scroll-view"
                scroll-y
                scroll-with-animation
                :scroll-top="scrollTop + 100"
                :scroll-into-view="itemId"
              >
                <view
                  v-for="(item, index) in typeListData"
                  :key="index"
                  class="type_item"
                  :class="{ active: typeIndex === index }"
                  @tap.stop="swichMenu(item, index)"
                >
                  <view
                    class="item"
                    :class="{ allLine: item.name && item.name.length > 5 }"
                  >
                    {{ item.name }}
                  </view>
                </view>
                <view class="seize_seat"></view>
              </scroll-view>
            </view>

            <scroll-view
              v-if="hasDishListItems"
              class="vegetable_order_list"
              scroll-y
              scroll-top="0"
            >
              <view
                v-for="(item, index) in dishListItems"
                :key="index"
                class="dish_card class-item"
              >
                <view class="dish_img" @click="openDetailHandle(item)">
                  <image class="dish_img_url" mode="aspectFill" :src="item.image"></image>
                </view>

                <view class="dish_info">
                  <view class="dish_headline">
                    <text class="dish_name" @click="openDetailHandle(item)">
                      {{ item.name }}
                    </text>
                    <text v-if="item.flavors && item.flavors.length" class="dish_tag">
                      可选规格
                    </text>
                  </view>

                  <view class="dish_label" @click="openDetailHandle(item)">
                    {{ item.description || "学生常点，宿舍区配送更高频" }}
                  </view>

                  <view class="dish_meta_row" @click="openDetailHandle(item)">
                    <text>{{ item.type === 2 ? "套餐搭配" : "热销推荐" }}</text>
                    <text v-if="item.flavors && item.flavors.length">
                      {{ item.flavors.length }} 种口味可选
                    </text>
                    <text v-else>校内现做现送</text>
                  </view>

                  <view class="dish_bottom">
                    <view class="dish_price">
                      <text class="ico">¥</text>
                      <text>{{ Number(item.price || 0).toFixed(2) }}</text>
                    </view>

                    <view
                      v-if="!item.flavors || item.flavors.length === 0"
                      class="dish_active"
                    >
                      <view
                        v-if="item.dishNumber >= 1"
                        class="dish_step dish_step--light"
                        @click="redDishAction(item, 'menu')"
                      >
                        -
                      </view>
                      <text v-if="item.dishNumber > 0" class="dish_number">
                        {{ item.dishNumber }}
                      </text>
                      <view class="dish_step" @click="addDishAction(item, 'menu')">
                        +
                      </view>
                    </view>

                    <view v-else class="dish_active_btn">
                      <view class="check_but" @click="moreNormDataesHandle(item)">
                        选择规格
                      </view>
                    </view>
                  </view>
                </view>
              </view>
              <view class="seize_seat"></view>
            </scroll-view>

            <view v-else class="no_dish">
              <view v-if="typeListData.length > 0">当前分类暂时没有在售商品</view>
            </view>
            </view>

            <view v-else-if="canRenderMenuEmpty" class="menu_empty menu_empty--inline">
              <text class="menu_empty_title">{{ menuBlankTitle }}</text>
              <text class="menu_empty_desc">{{ menuBlankDescription }}</text>
              <view class="menu_empty_actions">
                <view class="menu_empty_action" @click="retryMenuLoad">閲嶆柊鍔犺浇</view>
                <view
                  v-if="hasMerchantSwitch"
                  class="menu_empty_action menu_empty_action--ghost"
                  @click="goMerchantList"
                >
                  鍒囨崲鍟嗗
                </view>
              </view>
            </view>
          </view>

          <view v-else class="menu_empty">
            <text class="menu_empty_title">当前窗口休息中</text>
            <text class="menu_empty_desc">
              可以先看看推荐菜品，营业后再继续下单。
            </text>
          </view>
        </view>

        <view class="page_spacer"></view>
      </view>
    </scroll-view>

    <view v-if="isCartIdle" class="footer_order_buttom">
      <view class="cart_summary cart_summary--idle">
        <view class="cart_icon">袋</view>
        <view class="cart_texts">
          <text class="cart_title">购物车还是空的</text>
          <text class="cart_desc">选好菜品后就能去结算</text>
        </view>
      </view>
      <view class="order_but order_but--disabled">去结算</view>
    </view>

    <view v-else class="footer_order_buttom order_form">
      <view class="orderCar" @click="toggleOrderCartList">
        <view class="cart_icon cart_icon--active">{{ orderDishNumber }}</view>
        <view class="cart_texts">
          <text class="cart_title">已选 {{ orderDishNumber }} 份</text>
          <view class="order_price">
            <text class="ico">¥</text>
            <text>{{ orderDishPriceText }}</text>
          </view>
        </view>
      </view>
      <view class="order_but" @click="goOrder">去结算</view>
    </view>

    <view v-if="openMoreNormPop" class="pop_mask">
      <pop-mask
        :more-norm-dishdata="safeMoreNormDishdata"
        :more-normdata="safeMoreNormdata"
        :flavor-dataes="safeFlavorDataes"
        @checkMoreNormPop="checkMoreNormPop"
        @addShop="addShop"
        @closeMoreNorm="closeMoreNorm"
      ></pop-mask>
    </view>

    <view v-if="openDetailPop" class="pop_mask" style="z-index: 9999">
      <dish-detail
        :dish-detailes="dishDetailes"
        :open-detail-pop="openDetailPop"
        :dish-meal-data="safeDishMealData"
        @redDishAction="redDishAction"
        @addDishAction="addDishAction"
        @moreNormDataesHandle="moreNormDataesHandle"
        @dishClose="dishClose"
      ></dish-detail>
    </view>

    <view v-if="openOrderCartList" class="pop_mask" @click="closeOrderCartList">
      <pop-cart
        :open-order-cart-list="openOrderCartList"
        :order-and-user-info="safeOrderAndUserInfo"
        @clearCardOrder="clearCardOrder"
        @addDishAction="addDishAction"
        @redDishAction="redDishAction"
      ></pop-cart>
    </view>

    <view v-show="loaddingSt" class="pop_mask">
      <view class="lodding">
        <image class="lodding_ico" src="../../static/lodding.gif"></image>
      </view>
    </view>

    <phone ref="phone" :phone-data="phoneData" @closePopup="closePopup"></phone>
  </view>
</template>
<script src="./index.js"></script>
<style src="./style.scss" lang="scss" scoped></style>
<style scoped>
/* #ifdef MP-WEIXIN || APP-PLUS */
::v-deep ::-webkit-scrollbar {
  display: none !important;
  width: 0 !important;
  height: 0 !important;
  -webkit-appearance: none;
  background: transparent;
  color: transparent;
}
/* #endif */
</style>
