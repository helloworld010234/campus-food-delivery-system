import { request } from "../../utils/request.js";
import { withMerchantScope } from "../../utils/merchant.js";

export const openTable = (params) =>
  request({
    url: `/user/table/open/${params.tableId}/${params.seatNumber}`,
    method: "GET",
    params,
  });

export const getTableState = (params) =>
  request({
    url: `/user/table/tableStatus/${params.shopId}/${params.storeId}/${params.tableId}`,
    method: "GET",
    params,
  });

export const getTableOrderDishList = (params) =>
  request({
    url: `/user/order/shopCart/${params.tableId}`,
    method: "GET",
    params,
  });

export const getMoreNorm = (params) =>
  request({
    url: `/user/dish/flavor/${params.dishId}`,
    method: "GET",
    params,
  });

export const getList = (params) =>
  request({
    url: `/user/dish/category`,
    method: "GET",
    params,
  });

export const getDishDetail = (params) =>
  request({
    url: `/user/dish/setmealDishList/${params.setmealId}`,
    method: "GET",
    params,
  });

export const getDishList = (params) =>
  request({
    url: `/user/dish/dishPageList/${params.categoryId}/${params.type}/${params.page}/${params.pageSize}`,
    method: "GET",
    params,
  });

export const addDish = (params) =>
  request({
    url: `/user/order/addDish`,
    method: "POST",
    data: params,
  });

export const delDish = (params) =>
  request({
    url: `/user/order/decreaseDish/${params.tableId}/${params.dishId}`,
    method: "GET",
    params,
  });

export const clearOrder = (params) =>
  request({
    url: `/user/order/cleanShopCart/${params.tableId}`,
    method: "GET",
    params,
  });

export const payOrder = (params) =>
  request({
    url: `/user/order/pay/${params.tableId}/${params.jsCode}`,
    method: "GET",
    params,
  });

export const userLogin = (params) => {
  return request({
    url: "/user/user/login",
    method: "POST",
    data: params,
  });
};

export const getCategoryList = (params) => {
  return request({
    url: "/user/category/list",
    method: "GET",
    params: withMerchantScope(params),
  });
};

export const dishListByCategoryId = (params) => {
  return request({
    url: "/user/dish/list",
    method: "GET",
    params: withMerchantScope(params),
  });
};

export const commonDownload = (params) => {
  return request({
    url: "/user/common/download",
    method: "GET",
    params,
  });
};

export const addShoppingCart = (params) => {
  return request({
    url: "/user/shoppingCart",
    method: "POST",
    data: withMerchantScope(params),
  });
};

export const querySetmeaList = (params) => {
  return request({
    url: "/user/setmeal/list",
    method: "GET",
    params: withMerchantScope(params),
  });
};

export const getShoppingCartList = (params) => {
  return request({
    url: "/user/shoppingCart/list",
    method: "GET",
    params: withMerchantScope(params),
  });
};

export const editShoppingCart = (params) => {
  return request({
    url: "/user/shoppingCart",
    method: "PUT",
    data: withMerchantScope(params),
  });
};

export const newAddShoppingCartAdd = (params) => {
  return request({
    url: "/user/shoppingCart/add",
    method: "POST",
    data: withMerchantScope(params),
  });
};

export const newShoppingCartSub = (params) => {
  return request({
    url: "/user/shoppingCart/sub",
    method: "POST",
    data: withMerchantScope(params),
  });
};

export const delShoppingCart = (params) => {
  return request({
    url: "/user/shoppingCart/clean",
    method: "DELETE",
    params: withMerchantScope(params),
  });
};

export const queryOrderUserPage = (params) => {
  return request({
    url: "/user/order/userPage",
    method: "GET",
    params,
  });
};

export const submitOrderSubmit = (params) => {
  return request({
    url: "/user/order/submit",
    method: "POST",
    data: withMerchantScope(params),
  });
};

export const queryAddressBookList = (params) => {
  return request({
    url: "/user/addressBook/list",
    method: "GET",
    params,
  });
};

export const putAddressBookDefault = (params) => {
  return request({
    url: "/user/addressBook/default",
    method: "PUT",
    data: params,
  });
};

export const addAddressBook = (params) => {
  return request({
    url: "/user/addressBook",
    method: "POST",
    data: params,
  });
};

export const editAddressBook = (params) => {
  return request({
    url: "/user/addressBook",
    method: "PUT",
    data: params,
  });
};

export const delAddressBook = (id) => {
  return request({
    url: `/user/addressBook/?id=${id}`,
    method: "DELETE",
    params: { id },
  });
};

export const queryAddressBookById = (params) => {
  return request({
    url: `/user/addressBook/${params.id}`,
    method: "GET",
    params,
  });
};

export const oneOrderAgain = (params) => {
  return request({
    url: "/user/order/again",
    method: "POST",
    data: params,
  });
};

export const getAddressBookDefault = () => {
  return request({
    url: "/user/addressBook/default",
    method: "GET",
  });
};

export const querySetmealDishById = (params) => {
  return request({
    url: `/user/setmeal/dish/${params.id}`,
    method: "GET",
  });
};

export const getShopStatus = (params) => {
  return request({
    url: `/user/shop/status`,
    method: "GET",
    params: withMerchantScope(params),
  });
};

export const getMerchantInfo = (params) => {
  return request({
    url: `/user/shop/getMerchantInfo`,
    method: "GET",
    params: withMerchantScope(params),
  });
};

export const getMerchantList = (params) => {
  return request({
    url: `/user/shop/list`,
    method: "GET",
    params,
  });
};

export const getOrderPage = (params) => {
  return request({
    url: "/user/order/historyOrders",
    method: "GET",
    params,
  });
};

export const getOrderDetail = (params) =>
  request({
    url: `/user/order/orderDetail/${params}`,
    method: "GET",
  });

export const cancelOrder = (params) =>
  request({
    url: `/user/order/cancel/${params}`,
    method: "PUT",
  });

export const reminderOrder = (params) =>
  request({
    url: `/user/order/reminder/${params}`,
    method: "GET",
  });

export const paymentOrder = (params) =>
  request({
    url: `/user/order/payment`,
    method: "PUT",
    data: params,
  });

export const repetitionOrder = (params) =>
  request({
    url: `/user/order/repetition/${params}`,
    method: "POST",
    data: params,
  });

export const getEstimatedDeliveryTime = (params) =>
  request({
    url: `/user/order/getEstimatedDeliveryTime`,
    method: "GET",
    params: withMerchantScope(params),
  });

export const queryOrdersCheckStatus = (params) =>
  request({
    url: `/user/order/queryOrdersCheckStatus`,
    method: "GET",
    params,
  });
