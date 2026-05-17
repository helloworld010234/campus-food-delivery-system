import navBar from "../common/Navbar/navbar.vue"
import Phone from "@/components/uni-phone/index.vue"
import popMask from "./components/popMask.vue"
import popCart from "./components/popCart.vue"
import dishDetail from "./components/dishDetail.vue"
import {
  userLogin,
  getCategoryList,
  dishListByCategoryId,
  querySetmeaList,
  getShoppingCartList,
  newAddShoppingCartAdd,
  newShoppingCartSub,
  delShoppingCart,
  querySetmealDishById,
  getShopStatus,
  getMerchantInfo,
  getMerchantList,
} from "../api/api.js"
import { mapState, mapMutations } from "vuex"
import { baseUrl } from "../../utils/env"
import {
  normalizeMerchantId,
  resolveMerchantIdFromState,
  withMerchantScope,
} from "../../utils/merchant.js"

// Fallback location (Beijing) used only when user denies location permission.
const FALLBACK_LOCATION = "116.481488,39.990464"

export default {
  data() {
    return {
      loginSubmitting: false,
      openOrderCartList: false,
      typeListData: [],
      dishListData: [],
      dishListItems: [],
      dishDetailes: {},
      openDetailPop: false,
      openMoreNormPop: false,
      moreNormDataes: null,
      tableInfo: null,
      moreNormDishdata: {},
      moreNormdata: [],
      dishMealData: [],
      openTablePeoPleNumber: 1,
      orderData: 0,
      typeIndex: 0,
      openTablePop: false,
      flavorDataes: [],
      orderDishNumber: 0,
      orderDishPrice: 0,
      params: {
        shopId: "",
        storeId: "",
        tableId: "",
      },
      rightIdAndType: {},
      phoneData: "",
      tablewareNumber: 0,
      shopStatus: 1,
      scrollTop: 0,
      menuHeight: 0,
      menuItemHeight: 0,
      menuLoading: false,
      menuLoadingCount: 0,
      menuLoadedAtLeastOnce: false,
      itemId: "",
      arr: [],
      pageIntoView: "",
      routeMerchantId: "",
      campusChannels: [
        {
          key: "canteen",
          short: "饭",
          title: "食堂快送",
          desc: "热饭热菜优先出餐",
        },
        {
          key: "tea",
          short: "茶",
          title: "奶茶甜品",
          desc: "下课顺手点一杯",
        },
        {
          key: "night",
          short: "夜",
          title: "夜宵补给",
          desc: "晚自习后也能吃上热乎",
        },
        {
          key: "market",
          short: "购",
          title: "超市速达",
          desc: "零食饮料一起带到楼下",
        },
        {
          key: "light",
          short: "轻",
          title: "轻食水果",
          desc: "运动后也能轻松下单",
        },
      ],
    }
  },
  components: {
    navBar,
    Phone,
    popMask,
    popCart,
    dishDetail,
  },
  computed: {
    orderListDataes() {
      const current = this.orderListData()
      return Array.isArray(current) ? current : []
    },
    loaddingSt() {
      return this.lodding()
    },
    orderAndUserInfo() {
      const orderData = []
      this.orderListDataes.forEach((item) => {
        const group = {
          nickName: item.name ?? "",
          avatarUrl: item.image ?? "",
          dishList: [item],
        }
        const index = orderData.findIndex(
          (current) => current.nickName === group.nickName
        )
        if (index !== -1) {
          orderData[index].dishList.push(item)
        } else {
          orderData.push(group)
        }
      })
      return orderData
    },
    ht() {
      const rect =
        typeof uni.getMenuButtonBoundingClientRect === "function"
          ? uni.getMenuButtonBoundingClientRect()
          : null
      if (!rect) {
        return 0
      }
      return rect.top + rect.height + 10
    },
    homeContentStyle() {
      return `padding-top:${this.ht}px;`
    },
    shopProfile() {
      const current = this.shopInfo()
      return current || {}
    },
    shopStatusText() {
      return this.shopStatus === 1 ? "营业中" : "休息中"
    },
    menuTitle() {
      return this.shopProfile.shopName || "一食堂 6号窗口"
    },
    menuDescription() {
      return this.shopProfile.shopAddress || "校内现做现送，宿舍区优先配送"
    },
    campusLocation() {
      const currentAddress =
        typeof this.addressData === "function" ? this.addressData() : null
      if (currentAddress && currentAddress.detail) {
        return currentAddress.detail
      }
      return "宿舍区 12 号楼"
    },
    menuDeliveryFeeText() {
      return `配送费 ${Number(this.deliveryFee() || 0).toFixed(0)} 元`
    },
    hasMenuCategories() {
      return Array.isArray(this.typeListData) && this.typeListData.length > 0
    },
    hasDishListItems() {
      return Array.isArray(this.dishListItems) && this.dishListItems.length > 0
    },
    showInlineMenuLoading() {
      return this.menuLoading && !this.hasDishListItems
    },
    canRenderMenuEmpty() {
      return this.menuLoadedAtLeastOnce && !this.menuLoading
    },
    estimatedDeliveryMinutesText() {
      const storeInfo = this.storeInfo()
      const minutes =
        this.shopProfile.estimatedDeliveryMinutes ||
        (storeInfo && storeInfo.estimatedDeliveryMinutes) ||
        26
      return `平均 ${minutes} 分钟`
    },
    safeMerchantList() {
      const current = this.merchantList()
      return Array.isArray(current) ? current : []
    },
    hasMerchantSwitch() {
      return this.safeMerchantList.length > 1
    },
    menuBlankTitle() {
      return this.hasMerchantSwitch
        ? "\u5f53\u524d\u5546\u5bb6\u6682\u65f6\u6ca1\u6709\u5546\u54c1"
        : "\u5546\u5bb6\u6b63\u5728\u51c6\u5907\u83dc\u5355"
    },
    menuBlankDescription() {
      return this.hasMerchantSwitch
        ? "\u53ef\u4ee5\u5207\u6362\u5230\u5176\u4ed6\u7a97\u53e3\u770b\u770b\uff0c\u6216\u8005\u91cd\u65b0\u52a0\u8f7d\u5f53\u524d\u5546\u54c1\u3002"
        : "\u5f53\u524d\u5546\u5bb6\u6682\u65f6\u6ca1\u6709\u5c55\u793a\u5206\u7c7b\u6216\u83dc\u54c1\uff0c\u8bf7\u5148\u91cd\u65b0\u52a0\u8f7d\u3002"
    },
    isCartIdle() {
      return this.orderListDataes.length === 0 || this.shopStatus !== 1
    },
    orderDishPriceText() {
      return Number(this.orderDishPrice || 0).toFixed(2)
    },
    safeMoreNormDishdata() {
      return this.moreNormDishdata || {}
    },
    safeMoreNormdata() {
      return Array.isArray(this.moreNormdata) ? this.moreNormdata : []
    },
    safeFlavorDataes() {
      return Array.isArray(this.flavorDataes) ? this.flavorDataes : []
    },
    safeDishMealData() {
      return Array.isArray(this.dishMealData) ? this.dishMealData : []
    },
    safeOrderAndUserInfo() {
      return Array.isArray(this.orderAndUserInfo) ? this.orderAndUserInfo : []
    },
    campusHeroTags() {
      return [
        {
          label: this.shopStatusText,
          muted: this.shopStatus !== 1,
        },
        {
          label: this.menuDeliveryFeeText,
        },
        {
          label: this.estimatedDeliveryMinutesText,
        },
      ]
    },
    campusRecommendCards() {
      const fallbackCards = [
        {
          badge: "午饭快送",
          title: "一食堂热销套餐",
          desc: "热饭现炒，楼栋配送更省心",
          price: "12.80",
          image: "",
        },
        {
          badge: "课间补给",
          title: "奶茶甜品轻松点",
          desc: "下课到寝室之间刚好送到",
          price: "8.00",
          image: "",
        },
      ]

      return fallbackCards.map((fallback, index) => {
        const dish = this.dishListData[index] || this.dishListItems[index]
        if (!dish) {
          return fallback
        }

        return {
          badge: index === 0 ? "热销推荐" : "同学常点",
          title: dish.name,
          desc: dish.description || fallback.desc,
          price: Number(dish.price || 0).toFixed(2),
          image: dish.image || "",
        }
      })
    },
  },
  onReady() {
    this.getMenuItemTop().catch(() => { /* DOM measurement failure safe to ignore */ })
  },
  onLoad(options) {
    this.routeMerchantId = normalizeMerchantId(options && options.merchantId)
    uni.onNetworkStatusChange((res) => {
      if (res.isConnected === false) {
        uni.navigateTo({
          url: "/pages/nonet/index",
        })
      }
    })
    if (options && !options.status && !options.formOrder) {
      this.getData()
    }
  },
  onShow() {
    this.init(this.routeMerchantId)
  },
  methods: {
    ...mapMutations([
      "setShopInfo",
      "setMerchantList",
      "setCurrentMerchantId",
      "setShopPhone",
      "setShopStatus",
      "initdishListMut",
      "setStoreInfo",
      "setBaseUserInfo",
      "setLodding",
      "setToken",
      "setDeliveryFee",
    ]),
    ...mapState([
      "shopInfo",
      "shopPhone",
      "orderListData",
      "baseUserInfo",
      "lodding",
      "token",
      "deliveryFee",
      "addressData",
      "merchantList",
      "currentMerchantId",
      "storeInfo",
    ]),
    notifyError(message) {
      uni.showToast({
        title: message,
        icon: "none",
      })
    },
    getResolvedMerchantId(merchantId = "") {
      const currentShopInfo =
        typeof this.shopInfo === "function" ? this.shopInfo() : {}
      const currentStoreInfo =
        typeof this.storeInfo === "function" ? this.storeInfo() : {}
      return resolveMerchantIdFromState(
        normalizeMerchantId(merchantId) || this.currentMerchantId(),
        currentStoreInfo || {},
        currentShopInfo || {}
      )
    },
    buildMerchantParams(params = {}, merchantId = "") {
      return withMerchantScope(params, merchantId || this.getResolvedMerchantId())
    },
    updateMerchantContext(merchantId, extras = {}) {
      const normalizedMerchantId = normalizeMerchantId(merchantId)
      const currentStoreInfo = this.storeInfo() || {}
      const nextStoreInfo = {
        ...currentStoreInfo,
        ...extras,
      }

      if (normalizedMerchantId) {
        nextStoreInfo.merchantId = normalizedMerchantId
        nextStoreInfo.shopId = normalizedMerchantId
        this.setCurrentMerchantId(normalizedMerchantId)
      }

      this.setStoreInfo(nextStoreInfo)
      return normalizedMerchantId
    },
    applyMerchantInfo(data = {}) {
      const merchantId = normalizeMerchantId(data.merchantId || data.shopId)
      this.setShopInfo({
        ...data,
        merchantId,
        shopId: merchantId,
      })
      this.setShopPhone(data.phone || "")
      if (typeof data.deliveryFee !== "undefined") {
        this.setDeliveryFee(Number(data.deliveryFee || 0))
      }
      this.updateMerchantContext(merchantId, {
        campusId: data.campusId,
        estimatedDeliveryMinutes: data.estimatedDeliveryMinutes,
      })
    },
    resetMerchantScopedView() {
      this.openOrderCartList = false
      this.openDetailPop = false
      this.openMoreNormPop = false
      this.moreNormDataes = null
      this.moreNormDishdata = {}
      this.moreNormdata = []
      this.dishMealData = []
      this.flavorDataes = []
      this.dishDetailes = {}
      this.dishListData = []
      this.dishListItems = []
      this.typeListData = []
      this.typeIndex = 0
      this.rightIdAndType = {}
      this.orderDishNumber = 0
      this.orderDishPrice = 0
      this.initdishListMut([])
    },
    async loadMerchantList() {
      const storeInfo = this.storeInfo() || {}
      const params = {}
      if (storeInfo.campusId) {
        params.campusId = storeInfo.campusId
      }

      try {
        const res = await getMerchantList(params)
        if (res && res.code === 1) {
          const list = Array.isArray(res.data)
            ? res.data.map((item) => ({
                ...item,
                id: normalizeMerchantId(item.id),
                merchantId: normalizeMerchantId(item.id),
                shopId: normalizeMerchantId(item.id),
              }))
            : []
          this.setMerchantList(list)
          if (!this.getResolvedMerchantId() && list.length > 0) {
            this.updateMerchantContext(list[0].id, {
              campusId: list[0].campusId,
              estimatedDeliveryMinutes: list[0].estimatedDeliveryMinutes,
            })
          }
          return list
        }
      } catch (error) {
        this.notifyError("加载商户列表失败，请检查网络")
      }

      this.setMerchantList([])
      return []
    },
    goMerchantList() {
      if (!this.hasMerchantSwitch) {
        return
      }

      const merchantId = this.getResolvedMerchantId()
      uni.navigateTo({
        url: `/pages/merchant/index?merchantId=${encodeURIComponent(
          merchantId
        )}`,
      })
    },
    loginSync() {
      return new Promise((resolve, reject) => {
        uni.login({
          provider: "weixin",
          success: (loginRes) => {
            if (loginRes.errMsg === "login:ok") {
              resolve(loginRes.code)
              return
            }
            reject(new Error(loginRes.errMsg || "login failed"))
          },
          fail: reject,
        })
      })
    },
    getUserProfileSync() {
      return new Promise((resolve, reject) => {
        uni.getUserProfile({
          desc: "登录",
          success: resolve,
          fail: reject,
        })
      })
    },
    async resolveLoginLocation() {
      if (process.env.NODE_ENV === "development") {
        return FALLBACK_LOCATION
      }

      try {
        const [err, location] = await uni.getLocation({
          type: "gcj02",
          isHighAccuracy: true,
        })

        if (err || !location) {
          return FALLBACK_LOCATION
        }

        return `${location.longitude},${location.latitude}`
      } catch (error) {
        return FALLBACK_LOCATION
      }
    },
    async handleUserLogin(jsCode) {
      const params = {
        code: jsCode,
        location: await this.resolveLoginLocation(),
      }

      return userLogin(params)
        .then((success) => {
          if (success.code === 1) {
            const defaultMerchantId = normalizeMerchantId(
              success.data.merchantId || success.data.shopId
            )
            this.setToken(success.data.token)
            this.setDeliveryFee(success.data.deliveryFee)
            this.setCurrentMerchantId(defaultMerchantId)
            this.setStoreInfo({
              campusId: success.data.campusId,
              merchantId: defaultMerchantId,
              shopId: defaultMerchantId,
              estimatedDeliveryMinutes: success.data.estimatedDeliveryMinutes,
            })
            this.setShopPhone(success.data.phone || "")
            this.setShopInfo({
              ...success.data,
              merchantId: defaultMerchantId,
              shopId: defaultMerchantId,
            })
            return this.init(defaultMerchantId)
          }
        })
        .catch(() => {
          uni.showToast({
            title: "登录失败，请稍后重试",
            icon: "none",
          })
        })
    },
    beginMenuLoading() {
      this.menuLoadingCount += 1
      this.menuLoading = true
    },
    endMenuLoading() {
      this.menuLoadingCount = Math.max(0, this.menuLoadingCount - 1)
      this.menuLoading = this.menuLoadingCount > 0
    },
    syncAuthorizedUserInfo() {
      return new Promise((resolve) => {
        if (typeof uni === "undefined" || typeof uni.getUserInfo !== "function") {
          resolve(null)
          return
        }

        uni.getUserInfo({
          success: (res) => {
            if (res && res.userInfo) {
              this.setBaseUserInfo(res.userInfo)
            }
            resolve(res)
          },
          fail: () => {
            resolve(null)
          },
        })
      })
    },
    async triggerLoginFlow() {
      if (this.loginSubmitting) {
        return
      }

      this.loginSubmitting = true
      try {
        const jsCode = await this.loginSync()
        await this.handleUserLogin(jsCode)
        await this.syncAuthorizedUserInfo()
      } catch (error) {
        const rawMessage =
          (error && error.errMsg) ||
          (error && error.message) ||
          (error && error.data && error.data.msg) ||
          ""

        uni.showToast({
          title: String(rawMessage).toLowerCase().includes("cancel")
            ? "登录已取消"
            : "登录失败，请稍后重试",
          icon: "none",
        })
      } finally {
        this.loginSubmitting = false
      }
    },
    getData() {
      const res =
        typeof wx.getMenuButtonBoundingClientRect === "function"
          ? wx.getMenuButtonBoundingClientRect()
          : null
      this.shopStatus = 1
      this.setShopStatus(1)
      this.selectHeight = res ? res.height : 0

      if (this.token() !== "") {
        return
      }

      // Anonymous browse mode: public endpoints (shop, category, dish,
      // setmeal) are now allow-listed in request.js.  The login modal is
      // deferred until the user attempts a private action (add to cart,
      // submit order, etc.) which still triggers a 401 redirect.
      //
      // If the user arrived with a merchantId in the URL, preserve it so
      // the anonymous browse context survives across 401 -> login recovery.
      if (this.routeMerchantId) {
        this.setCurrentMerchantId(this.routeMerchantId)
        this.setStoreInfo({
          ...this.storeInfo(),
          merchantId: this.routeMerchantId,
          shopId: this.routeMerchantId,
        })
      }
    },
    async init(merchantId = "") {
      const activeMerchantId = this.updateMerchantContext(
        merchantId || this.routeMerchantId || this.getResolvedMerchantId()
      )

      this.beginMenuLoading()
      this.menuLoadedAtLeastOnce = false
      this.resetMerchantScopedView()
      try {
        await this.loadMerchantList()

        // Public browse: these calls are allow-listed in request.js and
        // will succeed without a token.  Private calls (cart, order) are
        // still gated by the login interceptor.
        await Promise.all([
          this.getShopInfo(activeMerchantId),
          this.getMerchantInfo(activeMerchantId),
          getCategoryList(this.buildMerchantParams({}, activeMerchantId))
            .then(async (res) => {
              if (res && res.code === 1) {
                this.typeListData = Array.isArray(res.data) ? [...res.data] : []
                if (this.typeListData.length > 0) {
                  await this.getDishListDataes(
                    this.typeListData[this.typeIndex || 0],
                    this.typeIndex
                  )
                }
                return
              }
              this.typeListData = []
              this.dishListData = []
              this.dishListItems = []
            })
            .catch((err) => {
              this.notifyError("加载菜单分类失败")
              this.typeListData = []
              this.dishListData = []
              this.dishListItems = []
            }),
        ])

        // Private: cart requires login.  Silently skip when anonymous.
        if (this.token()) {
          await this.getTableOrderDishListes(activeMerchantId)
        }
      } finally {
        this.endMenuLoading()
        this.menuLoadedAtLeastOnce = true
      }
    },
    async swichMenu(params, index) {
      if (!params || index === this.typeIndex) {
        return
      }

      this.typeIndex = index
      this.leftMenuStatus(index).catch(() => { /* DOM measurement failure safe to ignore */ })
      this.getDishListDataes(params, index)
      if (this.arr.length === 0) {
        this.getMenuItemTop().catch(() => { /* DOM measurement failure safe to ignore */ })
      }
    },
    getElRect(elClass, dataVal, retry = 0) {
      return new Promise((resolve) => {
        const query = uni.createSelectorQuery().in(this)
        query
          .select(`.${elClass}`)
          .fields(
            {
              size: true,
            },
            (res) => {
              if (!res) {
                if (retry >= 20) {
                  resolve(null)
                  return
                }
                setTimeout(() => {
                  this.getElRect(elClass, dataVal, retry + 1).then(resolve)
                }, 30)
                return
              }
              this[dataVal] = res.height
              resolve(res)
            }
          )
          .exec()
      })
    },
    async leftMenuStatus(index) {
      this.typeIndex = index
      if (this.menuHeight === 0 || this.menuItemHeight === 0) {
        await this.getElRect("menu-scroll-view", "menuHeight")
        await this.getElRect("type_item", "menuItemHeight")
      }
      if (!this.menuHeight || !this.menuItemHeight) {
        return
      }
      this.scrollTop =
        index * this.menuItemHeight +
        this.menuItemHeight / 2 -
        this.menuHeight / 2
    },
    getMenuItemTop(retry = 0) {
      return new Promise((resolve) => {
        const selectorQuery = uni.createSelectorQuery()
        selectorQuery
          .selectAll(".class-item")
          .boundingClientRect((rects) => {
            if (!rects || !rects.length) {
              if (retry >= 20) {
                resolve([])
                return
              }
              setTimeout(() => {
                this.getMenuItemTop(retry + 1).then(resolve)
              }, 30)
              return
            }
            this.arr = rects
            resolve(rects)
          })
          .exec()
      })
    },
    async getDishListDataes(params, index) {
      if (!params) {
        this.dishListData = []
        this.dishListItems = []
        return
      }

      this.beginMenuLoading()
      this.rightIdAndType = {
        id: params.id,
        type: params.type,
      }

      const param = this.buildMerchantParams({
        categoryId: params.id,
      })

      try {
        if (params.type === 1) {
          await dishListByCategoryId(param)
            .then((res) => {
              if (res && res.code === 1) {
                this.dishListData = Array.isArray(res.data)
                  ? res.data.map((obj) => ({
                      ...obj,
                      type: 1,
                      newCardNumber: 0,
                    }))
                  : []
              }
            })
            .catch((err) => {
              this.notifyError("加载套餐失败")
              this.dishListData = []
            })
        } else {
          await querySetmeaList(param)
            .then((success) => {
              if (success && success.code === 1) {
                this.dishListData = Array.isArray(success.data)
                  ? success.data.map((obj) => ({
                      ...obj,
                      type: 2,
                      newCardNumber: 0,
                    }))
                  : []
              }
            })
            .catch((err) => {
              this.notifyError("加载菜品失败")
              this.dishListData = []
            })
        }

        this.typeIndex = index
        this.setOrderNum()
        this.$nextTick(() => {
          this.getMenuItemTop().catch(() => { /* DOM measurement failure safe to ignore */ })
        })
      } finally {
        this.endMenuLoading()
      }
    },
    async getShopInfo(merchantId = "") {
      await getShopStatus(this.buildMerchantParams({}, merchantId))
        .then((res) => {
          if (res && typeof res.data !== "undefined") {
            this.shopStatus = res.data
            this.setShopStatus(res.data)
          }
        })
        .catch((err) => {
          this.notifyError("获取店铺状态失败")
        })
    },
    async getMerchantInfo(merchantId = "") {
      await getMerchantInfo(this.buildMerchantParams({}, merchantId))
        .then((res) => {
          if (res && res.data) {
            this.phoneData = res.data.phone || ""
            this.applyMerchantInfo(res.data)
          }
        })
        .catch((err) => {
          this.notifyError("获取商家信息失败")
        })
    },
    getNewImage(image) {
      return `${baseUrl}/common/download?name=${image}`
    },
    async getTableOrderDishListes(merchantId = "") {
      await getShoppingCartList(this.buildMerchantParams({}, merchantId))
        .then((res) => {
          if (res.code === 1) {
            const cartData = Array.isArray(res.data) ? res.data : []
            this.initdishListMut(cartData)
            this.computOrderInfo()
          }
        })
        .catch((err) => {
          this.notifyError("获取购物车失败")
        })
    },
    goOrder() {
      uni.navigateTo({
        url: "/pages/order/index",
      })
    },
    toggleOrderCartList() {
      this.openOrderCartList = !this.openOrderCartList
    },
    closeOrderCartList() {
      this.openOrderCartList = false
    },
    normalizeActionPayload(payload, fallbackForm = "") {
      if (payload && payload.obj) {
        return {
          item: payload.obj,
          form: payload.item || fallbackForm,
        }
      }
      return {
        item: payload,
        form: fallbackForm,
      }
    },
    async addDishAction(payload, form) {
      // Private action guard: adding to cart requires login.
      if (!this.token()) {
        uni.showModal({
          title: "温馨提示",
          content: "请先完成微信登录后再开始点餐。",
          showCancel: false,
          success: async (result) => {
            if (result.confirm) {
              await this.triggerLoginFlow()
            }
          },
        })
        return false
      }

      const normalized = this.normalizeActionPayload(payload, form)
      const item = normalized.item
      const actionForm = normalized.form || ""

      if (!item) {
        return false
      }

      if (
        this.openMoreNormPop &&
        (!this.flavorDataes || this.flavorDataes.length <= 0)
      ) {
        uni.showToast({
          title: "请选择规格",
          icon: "none",
        })
        return false
      }

      this.openMoreNormPop = false
      this.tablewareNumber++
      if (this.dishDetailes && typeof this.dishDetailes.dishNumber === "number") {
        this.dishDetailes.dishNumber++
      }

      if (
        this.orderListDataes.length > 0 &&
        !this.orderListDataes.some((current) => current.id === item.dishId) &&
        this.flavorDataes.length > 0
      ) {
        item.flavorRemark = JSON.stringify(this.flavorDataes)
      }

      let dishFlavorDatas = ""
      let flavorRemark = []
      if (item.flavorRemark) {
        flavorRemark = JSON.parse(item.flavorRemark)
      }
      if (item.dishFlavor !== "" && item.dishFlavor) {
        dishFlavorDatas = item.dishFlavor
      } else if (flavorRemark.length > 0) {
        dishFlavorDatas = flavorRemark.join(",")
      } else {
        dishFlavorDatas = null
      }

      let params = {
        dishFlavor: dishFlavorDatas,
      }

      if (item.type === 1) {
        params = {
          ...params,
          dishId: item.id,
        }
      } else if (item.type === 2) {
        params = {
          setmealId: item.id,
        }
      } else if (actionForm === "cart") {
        if (item.dishId) {
          params = {
            ...params,
            dishId: item.dishId,
          }
        } else {
          params = {
            setmealId: item.setmealId,
          }
        }
      }

      newAddShoppingCartAdd(this.buildMerchantParams(params))
        .then((res) => {
          if (res.code === 1) {
            this.getTableOrderDishListes()
            this.getDishListDataes(this.rightIdAndType, this.typeIndex)
            this.flavorDataes = []
          }
        })
        .catch((err) => {
          this.notifyError("加购失败，请重试")
        })
    },
    addShop(item) {
      this.dishDetailes = item
      this.addDishAction(item, "menu")
    },
    async redDishAction(payload, form) {
      const normalized = this.normalizeActionPayload(payload, form)
      const item = normalized.item
      const actionForm = normalized.form || ""

      if (!item) {
        return false
      }

      this.tablewareNumber--
      if (this.dishDetailes && typeof this.dishDetailes.dishNumber === "number") {
        this.dishDetailes.dishNumber--
      }

      let dishFlavorDatas = ""
      let flavorRemark = []
      if (item.flavorRemark) {
        flavorRemark = JSON.parse(item.flavorRemark)
      }
      if (item.dishFlavor !== "" && item.dishFlavor) {
        dishFlavorDatas = item.dishFlavor
      } else if (flavorRemark.length > 0) {
        dishFlavorDatas = flavorRemark[0]
      } else {
        dishFlavorDatas = null
      }

      let params = {
        dishFlavor: dishFlavorDatas,
      }

      if (item.type === 1) {
        params = {
          ...params,
          dishId: item.id,
        }
      } else if (item.type === 2) {
        params = {
          setmealId: item.id,
        }
      } else if (actionForm === "cart") {
        if (item.dishId) {
          params = {
            ...params,
            dishId: item.dishId,
          }
        } else {
          params = {
            setmealId: item.setmealId,
          }
        }
      }

      await newShoppingCartSub(this.buildMerchantParams(params))
        .then((res) => {
          if (res.code === 1) {
            this.getTableOrderDishListes()
            this.getDishListDataes(this.rightIdAndType, this.typeIndex)
          }
        })
        .catch((err) => {
          this.notifyError("减少数量失败")
        })
    },
    clearCardOrder() {
      delShoppingCart(this.buildMerchantParams())
        .then(() => {
          this.openOrderCartList = false
          this.getTableOrderDishListes()
          this.getDishListDataes(this.rightIdAndType, this.typeIndex)
        })
        .catch((err) => {
          this.notifyError("清空购物车失败")
        })
    },
    openDetailHandle(item) {
      this.dishDetailes = item
      if (item.type === 2) {
        querySetmealDishById({
          id: item.id,
        })
          .then((res) => {
            if (res.code === 1) {
              this.openDetailPop = true
              this.dishMealData = Array.isArray(res.data) ? res.data : []
            }
          })
          .catch((err) => {
            this.notifyError("加载套餐详情失败")
          })
      } else {
        this.openDetailPop = true
      }
    },
    dishClose() {
      this.openDetailPop = false
    },
    moreNormDataesHandle(item) {
      if (!item || !Array.isArray(item.flavors) || item.flavors.length === 0) {
        return
      }
      this.flavorDataes.splice(0)
      this.moreNormDishdata = item
      this.openDetailPop = false
      this.openMoreNormPop = true
      this.moreNormdata = item.flavors.map((obj) => ({
        ...obj,
        value: JSON.parse(obj.value),
      }))
      this.moreNormdata.forEach((current) => {
        if (current.value && current.value.length > 0) {
          this.flavorDataes.push(current.value[0])
        }
      })
    },
    checkMoreNormPop(val) {
      const obj = val.obj
      const item = val.item
      let index
      const hasSameGroup = obj.some((current) => {
        index = this.flavorDataes.findIndex((target) => target === current)
        return index !== -1
      })
      const currentIndex = this.flavorDataes.findIndex(
        (target) => target === item
      )
      if (currentIndex === -1 && !hasSameGroup) {
        this.flavorDataes.push(item)
      } else if (hasSameGroup) {
        this.flavorDataes.splice(index, 1)
        this.flavorDataes.push(item)
      } else {
        this.flavorDataes.splice(currentIndex, 1)
      }
    },
    closeMoreNorm() {
      this.flavorDataes.splice(0, this.flavorDataes.length)
      this.openMoreNormPop = false
    },
    computOrderInfo() {
      this.orderDishNumber = 0
      this.orderDishPrice = 0
      this.orderListDataes.forEach((item) => {
        this.orderDishNumber += Number(item.number || 0)
        this.orderDishPrice += Number(item.number || 0) * Number(item.amount || 0)
      })
    },
    setOrderNum() {
      const origin = Array.isArray(this.dishListData) ? this.dishListData : []
      const cartData = this.orderListDataes
      origin.forEach((obj) => {
        obj.dishNumber = 0
        if (Array.isArray(obj.flavors)) {
          obj.flavors = obj.flavors.filter((value) => value.name !== "")
        }

        if (cartData.length > 0) {
          cartData.forEach((target) => {
            if (obj.id === target.dishId || obj.id === target.setmealId) {
              obj.dishNumber = target.number
            }
          })
        }
      })

      if (this.dishListItems.length === 0) {
        this.dishListItems = origin
      } else {
        this.dishListItems.splice(0, this.dishListItems.length, ...origin)
      }
    },
    campusChannelTap(index) {
      if (this.typeListData.length > 0) {
        const targetIndex = Math.min(index, this.typeListData.length - 1)
        this.swichMenu(this.typeListData[targetIndex], targetIndex)
      }
      this.pageIntoView = "menu-anchor"
      setTimeout(() => {
        this.pageIntoView = ""
      }, 300)
    },
    jumpToMenu() {
      this.campusChannelTap(this.typeIndex || 0)
    },
    goMyCenter() {
      uni.navigateTo({
        url: "/pages/my/my",
      })
    },
    goHistoryOrders() {
      uni.navigateTo({
        url: "/pages/historyOrder/historyOrder",
      })
    },
    retryMenuLoad() {
      this.init(this.getResolvedMerchantId())
    },
    handlePhone(type) {
      if (this.$refs.phone && this.$refs.phone.$refs.popup) {
        this.$refs.phone.$refs.popup.open(type)
      }
    },
    closePopup(type) {
      if (this.$refs.phone && this.$refs.phone.$refs.popup) {
        this.$refs.phone.$refs.popup.close(type)
      }
    },
    disabledScroll() {
      return false
    },
  },
}
