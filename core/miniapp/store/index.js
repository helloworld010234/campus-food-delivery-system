import Vue from "vue";
import Vuex from "vuex";
import {
  createDefaultSessionState,
  loadSessionState,
  normalizeBaseUserInfo,
  persistSessionState,
} from "../utils/session.js";

Vue.use(Vuex);

const state = {
  ...createDefaultSessionState(),
  ...loadSessionState(),
  orderListData: [],
  lodding: false,
  sessionId: "",
  dishTypeIndex: 0,
  shopStatus: {},
  orderData: {},
  arrivals: "",
  remarkData: "",
  addressData: {},
  config: {
    defaultNickname: "同学",
  },
};

const store = new Vuex.Store({
  state,
  mutations: {
    setStoreInfo(state, provider) {
      state.storeInfo = provider;
    },
    setShopInfo(state, provider) {
      state.shopInfo = provider;
    },
    setMerchantList(state, provider) {
      state.merchantList = provider;
    },
    setCurrentMerchantId(state, provider) {
      state.currentMerchantId = provider;
    },
    initdishListMut(state, provider) {
      state.orderListData = provider;
    },
    setBaseUserInfo(state, provider) {
      state.baseUserInfo = normalizeBaseUserInfo(provider);
    },
    setLodding(state, provider) {
      state.lodding = provider;
    },
    setSessionId(state, provider) {
      state.sessionId = provider;
    },
    setAddressBackUrl(state, provider) {
      state.addressBackUrl = provider;
    },
    setDishTypeIndex(state, provider) {
      state.dishTypeIndex = provider;
    },
    setShopPhone(state, provider) {
      state.shopPhone = provider;
    },
    setShopStatus(state, provider) {
      state.shopStatus = provider;
    },
    setOrderData(state, provider) {
      state.orderData = provider;
    },
    setToken(state, provider) {
      state.token = provider;
    },
    setArrivalTime(state, provider) {
      state.arrivals = provider;
    },
    setRemark(state, provider) {
      state.remarkData = provider;
    },
    setAddress(state, provider) {
      state.addressData = provider;
    },
    setDeliveryFee(state, deliveryFee) {
      state.deliveryFee = deliveryFee;
    },
    setGender(state, gender) {
      state.gender = gender;
    },
    setConfig(state, provider) {
      state.config = { ...state.config, ...provider };
    },
  },
  getters: {
    defaultNickname: (state) =>
      state.config && state.config.defaultNickname
        ? state.config.defaultNickname
        : "同学",
  },
  actions: {},
});

store.subscribe((mutation, currentState) => {
  persistSessionState(currentState);
});

export default store;
