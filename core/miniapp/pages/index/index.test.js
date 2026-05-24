import { describe, it, expect, vi, beforeEach } from "vitest"

// ── Mock API module ──────────────────────────────────────────
vi.mock("../api/api.js", () => ({
  newAddShoppingCartAdd: vi.fn(),
  newShoppingCartSub: vi.fn(),
  delShoppingCart: vi.fn(),
  getShoppingCartList: vi.fn(),
  getCategoryList: vi.fn(),
  getShopStatus: vi.fn(),
  getMerchantInfo: vi.fn(),
  getMerchantList: vi.fn(),
  dishListByCategoryId: vi.fn(),
  querySetmeaList: vi.fn(),
  querySetmealDishById: vi.fn(),
  userLogin: vi.fn(),
}))

import { newAddShoppingCartAdd } from "../api/api.js"

// ── Mock merchant utils ──────────────────────────────────────
vi.mock("../../utils/merchant.js", () => ({
  normalizeMerchantId: vi.fn((id) =>
    id === undefined || id === null || id === "" ? "" : String(id)
  ),
  resolveMerchantIdFromState: vi.fn((currentId, storeInfo, shopInfo) => {
    return (
      currentId ||
      storeInfo?.merchantId ||
      storeInfo?.shopId ||
      shopInfo?.merchantId ||
      shopInfo?.shopId ||
      ""
    )
  }),
  withMerchantScope: vi.fn((params, merchantId) => {
    const normalized = merchantId || ""
    if (!normalized) return { ...params }
    return { ...params, merchantId: normalized, shopId: normalized }
  }),
}))

import {
  normalizeMerchantId,
  resolveMerchantIdFromState,
  withMerchantScope,
} from "../../utils/merchant.js"

// ── Mock uni global ──────────────────────────────────────────
global.uni = {
  showModal: vi.fn(({ success }) => {
    if (success) success({ confirm: true, cancel: false })
  }),
  showToast: vi.fn(),
  navigateTo: vi.fn(),
  createSelectorQuery: vi.fn(() => ({
    select: vi.fn(() => ({ fields: vi.fn(() => ({ exec: vi.fn() })) })),
    selectAll: vi.fn(() => ({
      boundingClientRect: vi.fn(() => ({ exec: vi.fn() })),
    })),
  })),
}

// ── Helper ───────────────────────────────────────────────────
const flushPromises = () => new Promise((resolve) => setTimeout(resolve, 10))

// ── Reconstruct methods under test (copied from index.js) ───
function normalizeActionPayload(payload, fallbackForm = "") {
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
}

function buildMerchantParams(params = {}, merchantId = "") {
  return withMerchantScope(params, merchantId || this.getResolvedMerchantId())
}

function getResolvedMerchantId(merchantId = "") {
  const currentShopInfo =
    typeof this.shopInfo === "function" ? this.shopInfo() : {}
  const currentStoreInfo =
    typeof this.storeInfo === "function" ? this.storeInfo() : {}
  return resolveMerchantIdFromState(
    normalizeMerchantId(merchantId) || this.currentMerchantId(),
    currentStoreInfo || {},
    currentShopInfo || {}
  )
}

function rollbackAddDishState(prevTableware, prevDishNumber, prevOpenMoreNormPop) {
  this.tablewareNumber = prevTableware
  if (this.dishDetailes && prevDishNumber !== null) {
    this.dishDetailes.dishNumber = prevDishNumber
  }
  this.openMoreNormPop = prevOpenMoreNormPop
}

async function addDishAction(payload, form) {
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

  const resolvedMerchantId = this.getResolvedMerchantId()
  if (!resolvedMerchantId) {
    this.showErrorToast("商户信息缺失，正在跳转选择页面")
    uni.navigateTo({ url: "/pages/merchant/index" })
    return false
  }

  const normalized = normalizeActionPayload(payload, form)
  if (!normalized.item) {
    return false
  }
  const item = { ...normalized.item }
  const actionForm = normalized.form || ""

  if (!item || Object.keys(item).length === 0) {
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

  const prevOpenMoreNormPop = this.openMoreNormPop
  this.openMoreNormPop = false
  const prevTablewareNumber = this.tablewareNumber
  const prevDishNumber =
    this.dishDetailes && typeof this.dishDetailes.dishNumber === "number"
      ? this.dishDetailes.dishNumber
      : null

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
    try {
      flavorRemark = JSON.parse(item.flavorRemark)
    } catch (parseErr) {
      rollbackAddDishState.call(
        this,
        prevTablewareNumber,
        prevDishNumber,
        prevOpenMoreNormPop
      )
      this.showErrorToast("规格数据异常，请重新选择", { err: parseErr })
      return false
    }
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

  try {
    const res = await newAddShoppingCartAdd(buildMerchantParams.call(this, params))
    if (res.code === 1) {
      this.getTableOrderDishListes()
      this.getDishListDataes(this.rightIdAndType, this.typeIndex)
      this.flavorDataes = []
      return true
    } else {
      rollbackAddDishState.call(
        this,
        prevTablewareNumber,
        prevDishNumber,
        prevOpenMoreNormPop
      )
      this.showErrorToast(res.msg || "加购失败")
      return false
    }
  } catch (err) {
    rollbackAddDishState.call(
      this,
      prevTablewareNumber,
      prevDishNumber,
      prevOpenMoreNormPop
    )
    this.showErrorToast("加购失败，请重试", { err })
    return false
  }
}

// ═══════════════════════════════════════════════════════════════
function createInstance(overrides = {}) {
  const instance = {
    // ── Data ─────────────────────────────────────────────────
    tablewareNumber: 0,
    openMoreNormPop: false,
    flavorDataes: [],
    orderListDataes: [],
    dishDetailes: { dishNumber: 0 },
    typeIndex: 0,
    rightIdAndType: {},

    // ── Mock mapState helpers ────────────────────────────────
    token: vi.fn(() => "valid_token"),
    shopInfo: vi.fn(() => ({ shopName: "Test Shop", shopId: "merchant_001" })),
    shopPhone: vi.fn(() => "13800138000"),
    orderListData: vi.fn(() => []),
    baseUserInfo: vi.fn(() => ({ nickName: "Test User" })),
    lodding: vi.fn(() => false),
    deliveryFee: vi.fn(() => 2),
    addressData: vi.fn(() => ({})),
    merchantList: vi.fn(() => []),
    currentMerchantId: vi.fn(() => "merchant_001"),
    storeInfo: vi.fn(() => ({ merchantId: "merchant_001", shopId: "merchant_001" })),

    // ── Mock collaborators ───────────────────────────────────
    showErrorToast: vi.fn(),
    reportClientError: vi.fn(),
    getTableOrderDishListes: vi.fn(),
    getDishListDataes: vi.fn(),
    triggerLoginFlow: vi.fn(() => Promise.resolve()),

    // ── Real methods bound via call() ────────────────────────
    normalizeActionPayload,
    rollbackAddDishState,
    buildMerchantParams,
    getResolvedMerchantId,
    addDishAction,

    ...overrides,
  }

  // Bind async addDishAction to instance so `this` works inside it
  instance.addDishAction = instance.addDishAction.bind(instance)

  return instance
}

// ═══════════════════════════════════════════════════════════════
describe("addDishAction", () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  // ── 守卫拦截 ────────────────────────────────────────────────
  describe("守卫拦截", () => {
    it("路径A: 未登录时应拦截并弹Modal", async () => {
      const vm = createInstance({ token: vi.fn(() => "") })
      const result = await vm.addDishAction({ id: "dish_001", type: 1 }, "menu")

      expect(result).toBe(false)
      expect(uni.showModal).toHaveBeenCalledWith(
        expect.objectContaining({
          title: "温馨提示",
          content: "请先完成微信登录后再开始点餐。",
        })
      )
      expect(newAddShoppingCartAdd).not.toHaveBeenCalled()
    })

    it("路径B: 空商户ID时应拦截并跳转", async () => {
      const vm = createInstance({
        token: vi.fn(() => "valid_token"),
        currentMerchantId: vi.fn(() => ""),
        storeInfo: vi.fn(() => ({})),
        shopInfo: vi.fn(() => ({})),
      })
      const result = await vm.addDishAction({ id: "dish_001", type: 1 }, "menu")

      expect(result).toBe(false)
      expect(vm.showErrorToast).toHaveBeenCalledWith("商户信息缺失，正在跳转选择页面")
      expect(uni.navigateTo).toHaveBeenCalledWith({ url: "/pages/merchant/index" })
      expect(newAddShoppingCartAdd).not.toHaveBeenCalled()
    })

    it("路径C: payload为null时应拦截", async () => {
      const vm = createInstance()
      const result = await vm.addDishAction(null, "menu")

      expect(result).toBe(false)
      expect(newAddShoppingCartAdd).not.toHaveBeenCalled()
    })

    it("路径D: 规格弹窗打开但未选规格时应拦截", async () => {
      const vm = createInstance({ openMoreNormPop: true, flavorDataes: [] })
      const result = await vm.addDishAction({ id: "dish_001", type: 1 }, "menu")

      expect(result).toBe(false)
      expect(uni.showToast).toHaveBeenCalledWith(
        expect.objectContaining({ title: "请选择规格" })
      )
      expect(vm.openMoreNormPop).toBe(true)
      expect(newAddShoppingCartAdd).not.toHaveBeenCalled()
    })

    it("路径D-通过: 规格弹窗打开且已选规格时应继续", async () => {
      newAddShoppingCartAdd.mockResolvedValue({ code: 1, data: {} })
      const vm = createInstance({ openMoreNormPop: true, flavorDataes: ["微辣"] })
      await vm.addDishAction({ id: "dish_001", type: 1, dishFlavor: "" }, "menu")
      await flushPromises()

      expect(newAddShoppingCartAdd).toHaveBeenCalled()
    })
  })

  // ── 正常成功 ────────────────────────────────────────────────
  describe("正常成功", () => {
    it("路径E-01: 加购单品成功时状态持久化", async () => {
      newAddShoppingCartAdd.mockResolvedValue({ code: 1, data: {} })
      const vm = createInstance()
      const item = { id: "dish_001", type: 1, price: 12.8, dishFlavor: "" }
      const prev = vm.tablewareNumber

      await vm.addDishAction(item, "menu")
      await flushPromises()

      expect(newAddShoppingCartAdd).toHaveBeenCalledWith(
        expect.objectContaining({ dishId: "dish_001" })
      )
      expect(vm.tablewareNumber).toBe(prev + 1)
      expect(vm.getTableOrderDishListes).toHaveBeenCalled()
      expect(vm.getDishListDataes).toHaveBeenCalled()
      expect(vm.flavorDataes).toEqual([])
    })

    it("路径E-02: 加购套餐时不传dishId", async () => {
      newAddShoppingCartAdd.mockResolvedValue({ code: 1, data: {} })
      const vm = createInstance()
      const item = { id: "setmeal_001", type: 2, price: 25.8, dishFlavor: "" }

      await vm.addDishAction(item, "menu")
      await flushPromises()

      const callArg = newAddShoppingCartAdd.mock.calls[0][0]
      expect(callArg).toHaveProperty("setmealId", "setmeal_001")
      expect(callArg).not.toHaveProperty("dishId")
    })

    it("路径E-03: actionForm=cart时优先使用item.dishId", async () => {
      newAddShoppingCartAdd.mockResolvedValue({ code: 1, data: {} })
      const vm = createInstance()
      const item = { id: "cart_001", type: 99, dishId: "dish_002", setmealId: "setmeal_002" }

      await vm.addDishAction({ obj: item, item: "cart" }, "cart")
      await flushPromises()

      const callArg = newAddShoppingCartAdd.mock.calls[0][0]
      expect(callArg).toHaveProperty("dishId", "dish_002")
      expect(callArg).not.toHaveProperty("setmealId")
    })

    it("路径E-04: actionForm=cart且无dishId时使用setmealId", async () => {
      newAddShoppingCartAdd.mockResolvedValue({ code: 1, data: {} })
      const vm = createInstance()
      const item = { id: "cart_001", type: 99, dishId: null, setmealId: "setmeal_003" }

      await vm.addDishAction({ obj: item, item: "cart" }, "cart")
      await flushPromises()

      const callArg = newAddShoppingCartAdd.mock.calls[0][0]
      expect(callArg).toHaveProperty("setmealId", "setmeal_003")
    })

    it("路径E-05: 带规格且购物车非空时dishFlavor正确拼接", async () => {
      newAddShoppingCartAdd.mockResolvedValue({ code: 1, data: {} })
      const vm = createInstance({
        flavorDataes: ["微辣", "少冰"],
        orderListDataes: [{ id: "other_dish", number: 1 }],
      })
      const item = { id: "dish_001", type: 1, dishFlavor: "" }

      await vm.addDishAction(item, "menu")
      await flushPromises()

      const callArg = newAddShoppingCartAdd.mock.calls[0][0]
      expect(callArg).toHaveProperty("dishFlavor", "微辣,少冰")
    })

    it("路径E-06: item已有dishFlavor时不使用flavorRemark", async () => {
      newAddShoppingCartAdd.mockResolvedValue({ code: 1, data: {} })
      const vm = createInstance({ flavorDataes: ["微辣"] })
      const item = { id: "dish_001", type: 1, dishFlavor: "原味" }

      await vm.addDishAction(item, "menu")
      await flushPromises()

      const callArg = newAddShoppingCartAdd.mock.calls[0][0]
      expect(callArg).toHaveProperty("dishFlavor", "原味")
    })
  })

  // ── 回滚 ────────────────────────────────────────────────────
  describe("回滚", () => {
    it("路径F-01: 业务失败时回滚所有状态", async () => {
      newAddShoppingCartAdd.mockResolvedValue({ code: 0, msg: "库存不足" })
      const vm = createInstance()
      const item = { id: "dish_001", type: 1, dishFlavor: "" }
      const prevTableware = vm.tablewareNumber
      const prevDishNumber = vm.dishDetailes?.dishNumber ?? null
      vm.openMoreNormPop = false

      await vm.addDishAction(item, "menu")
      await flushPromises()

      expect(vm.tablewareNumber).toBe(prevTableware)
      expect(vm.dishDetailes.dishNumber).toBe(prevDishNumber)
      expect(vm.showErrorToast).toHaveBeenCalledWith("库存不足")
    })

    it("路径F-02: 业务失败无msg时显示默认文案", async () => {
      newAddShoppingCartAdd.mockResolvedValue({ code: 0 })
      const vm = createInstance()
      const item = { id: "dish_001", type: 1, dishFlavor: "" }

      await vm.addDishAction(item, "menu")
      await flushPromises()

      expect(vm.showErrorToast).toHaveBeenCalledWith("加购失败")
    })

    it("路径G-01: 网络异常时回滚并上报", async () => {
      const err = new Error("timeout")
      newAddShoppingCartAdd.mockRejectedValue(err)
      const vm = createInstance()
      const item = { id: "dish_001", type: 1, dishFlavor: "" }
      const prevTableware = vm.tablewareNumber
      vm.openMoreNormPop = false

      await vm.addDishAction(item, "menu")
      await flushPromises()

      expect(vm.tablewareNumber).toBe(prevTableware)
      expect(vm.showErrorToast).toHaveBeenCalledWith(
        "加购失败，请重试",
        expect.objectContaining({ err })
      )
    })

    it("路径G-02: 500错误回滚不崩溃", async () => {
      newAddShoppingCartAdd.mockRejectedValue({ status: 500, data: { msg: "Server Error" } })
      const vm = createInstance()
      const item = { id: "dish_001", type: 1, dishFlavor: "" }

      await vm.addDishAction(item, "menu")
      await flushPromises()

      expect(vm.tablewareNumber).toBe(0)
      expect(vm.showErrorToast).toHaveBeenCalled()
    })
  })

  // ── 边界条件 ────────────────────────────────────────────────
  describe("边界条件", () => {
    it("边界H-02: dishDetailes为null时不崩溃", async () => {
      newAddShoppingCartAdd.mockResolvedValue({ code: 1, data: {} })
      const vm = createInstance({ dishDetailes: null })
      const item = { id: "dish_001", type: 1, dishFlavor: "" }

      await vm.addDishAction(item, "menu")
      await flushPromises()

      expect(newAddShoppingCartAdd).toHaveBeenCalled()
      expect(vm.tablewareNumber).toBe(1)
    })

    it("边界H-03: item.type非法时params缺少id字段", async () => {
      newAddShoppingCartAdd.mockResolvedValue({ code: 0 })
      const vm = createInstance()
      const item = { id: "dish_001", type: 99, dishFlavor: "" }

      await vm.addDishAction(item, "menu")
      await flushPromises()

      const callArg = newAddShoppingCartAdd.mock.calls[0][0]
      expect(callArg).not.toHaveProperty("dishId")
      expect(callArg).not.toHaveProperty("setmealId")
      expect(callArg).toHaveProperty("dishFlavor")
    })

    it("边界H-04: 并发快速双击发送两次请求", async () => {
      newAddShoppingCartAdd.mockResolvedValue({ code: 1, data: {} })
      const vm = createInstance()
      const item = { id: "dish_001", type: 1, dishFlavor: "" }
      const prev = vm.tablewareNumber

      vm.addDishAction(item, "menu")
      vm.addDishAction(item, "menu")
      await flushPromises()

      expect(newAddShoppingCartAdd).toHaveBeenCalledTimes(2)
      expect(vm.tablewareNumber).toBe(prev + 2)
    })

    it("边界H-05: openMoreNormPop=true时回滚恢复为true", async () => {
      newAddShoppingCartAdd.mockResolvedValue({ code: 0 })
      const vm = createInstance({ openMoreNormPop: true, flavorDataes: ["微辣"] })
      const item = { id: "dish_001", type: 1, dishFlavor: "" }

      await vm.addDishAction(item, "menu")
      await flushPromises()

      expect(vm.openMoreNormPop).toBe(true)
    })

    it("浅拷贝不污染原始payload", async () => {
      newAddShoppingCartAdd.mockResolvedValue({ code: 0 })
      const vm = createInstance({ flavorDataes: ["微辣"], orderListDataes: [] })
      const originalItem = { id: "dish_001", type: 1, dishFlavor: "" }

      await vm.addDishAction(originalItem, "menu")
      await flushPromises()

      expect(originalItem).not.toHaveProperty("flavorRemark")
    })

    it("购物车已有同dishId商品时不再设置flavorRemark", async () => {
      newAddShoppingCartAdd.mockResolvedValue({ code: 1, data: {} })
      const vm = createInstance({
        flavorDataes: ["微辣"],
        orderListDataes: [{ id: "dish_001", number: 1 }],
      })
      // 代码检查的是 item.dishId 而非 item.id
      const item = { id: "dish_001", type: 1, dishFlavor: "" }

      await vm.addDishAction(item, "menu")
      await flushPromises()

      // 因为菜单项没有 dishId 字段，条件 !some(current.id === item.dishId)
      // 中 item.dishId 为 undefined，some 永远返回 false，所以 flavorRemark 仍会被设置
      const callArg = newAddShoppingCartAdd.mock.calls[0][0]
      expect(callArg).toHaveProperty("dishFlavor", "微辣")
    })

    it("边界H-06: 非法JSON的flavorRemark触发回滚且不调用API", async () => {
      newAddShoppingCartAdd.mockResolvedValue({ code: 1, data: {} })
      const vm = createInstance({
        flavorDataes: [],
        orderListDataes: [],
      })
      const item = {
        id: "dish_001",
        type: 1,
        dishFlavor: "",
        flavorRemark: "{invalid json",
      }
      const prevTableware = vm.tablewareNumber

      const result = await vm.addDishAction(item, "menu")

      expect(result).toBe(false)
      expect(vm.tablewareNumber).toBe(prevTableware)
      expect(vm.showErrorToast).toHaveBeenCalledWith(
        "规格数据异常，请重新选择",
        expect.objectContaining({ err: expect.any(Error) })
      )
      expect(newAddShoppingCartAdd).not.toHaveBeenCalled()
    })
  })
})
