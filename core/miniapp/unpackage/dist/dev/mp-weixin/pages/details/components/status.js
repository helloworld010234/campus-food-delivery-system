(global["webpackJsonp"] = global["webpackJsonp"] || []).push([["pages/details/components/status"],{

/***/ 233:
/*!************************************************************************!*\
  !*** D:/sky-delivery/core/miniapp/pages/details/components/status.vue ***!
  \************************************************************************/
/*! no static exports found */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var _status_vue_vue_type_template_id_ce7f4d56___WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./status.vue?vue&type=template&id=ce7f4d56& */ 234);
/* harmony import */ var _status_vue_vue_type_script_lang_js___WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ./status.vue?vue&type=script&lang=js& */ 236);
/* harmony reexport (unknown) */ for(var __WEBPACK_IMPORT_KEY__ in _status_vue_vue_type_script_lang_js___WEBPACK_IMPORTED_MODULE_1__) if(["default"].indexOf(__WEBPACK_IMPORT_KEY__) < 0) (function(key) { __webpack_require__.d(__webpack_exports__, key, function() { return _status_vue_vue_type_script_lang_js___WEBPACK_IMPORTED_MODULE_1__[key]; }) }(__WEBPACK_IMPORT_KEY__));
/* harmony import */ var _order_style_scss_vue_type_style_index_0_lang_scss___WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ../../order/style.scss?vue&type=style&index=0&lang=scss& */ 238);
/* harmony import */ var _edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_runtime_componentNormalizer_js__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ../../../../../../edgedownload/HBuilderX.5.06.2026033105/HBuilderX/plugins/uniapp-cli/node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/runtime/componentNormalizer.js */ 36);

var renderjs





/* normalize component */

var component = Object(_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_runtime_componentNormalizer_js__WEBPACK_IMPORTED_MODULE_3__["default"])(
  _status_vue_vue_type_script_lang_js___WEBPACK_IMPORTED_MODULE_1__["default"],
  _status_vue_vue_type_template_id_ce7f4d56___WEBPACK_IMPORTED_MODULE_0__["render"],
  _status_vue_vue_type_template_id_ce7f4d56___WEBPACK_IMPORTED_MODULE_0__["staticRenderFns"],
  false,
  null,
  null,
  null,
  false,
  _status_vue_vue_type_template_id_ce7f4d56___WEBPACK_IMPORTED_MODULE_0__["components"],
  renderjs
)

component.options.__file = "pages/details/components/status.vue"
/* harmony default export */ __webpack_exports__["default"] = (component.exports);

/***/ }),

/***/ 234:
/*!*******************************************************************************************************!*\
  !*** D:/sky-delivery/core/miniapp/pages/details/components/status.vue?vue&type=template&id=ce7f4d56& ***!
  \*******************************************************************************************************/
/*! exports provided: render, staticRenderFns, recyclableRender, components */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var _edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_loaders_templateLoader_js_vue_loader_options_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_preprocess_loader_index_js_ref_17_0_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_template_js_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_uni_app_loader_page_meta_js_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_index_js_vue_loader_options_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_style_js_status_vue_vue_type_template_id_ce7f4d56___WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! -!../../../../../../edgedownload/HBuilderX.5.06.2026033105/HBuilderX/plugins/uniapp-cli/node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!../../../../../../edgedownload/HBuilderX.5.06.2026033105/HBuilderX/plugins/uniapp-cli/node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--17-0!../../../../../../edgedownload/HBuilderX.5.06.2026033105/HBuilderX/plugins/uniapp-cli/node_modules/@dcloudio/webpack-uni-mp-loader/lib/template.js!../../../../../../edgedownload/HBuilderX.5.06.2026033105/HBuilderX/plugins/uniapp-cli/node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-uni-app-loader/page-meta.js!../../../../../../edgedownload/HBuilderX.5.06.2026033105/HBuilderX/plugins/uniapp-cli/node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!../../../../../../edgedownload/HBuilderX.5.06.2026033105/HBuilderX/plugins/uniapp-cli/node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./status.vue?vue&type=template&id=ce7f4d56& */ 235);
/* harmony reexport (safe) */ __webpack_require__.d(__webpack_exports__, "render", function() { return _edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_loaders_templateLoader_js_vue_loader_options_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_preprocess_loader_index_js_ref_17_0_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_template_js_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_uni_app_loader_page_meta_js_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_index_js_vue_loader_options_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_style_js_status_vue_vue_type_template_id_ce7f4d56___WEBPACK_IMPORTED_MODULE_0__["render"]; });

/* harmony reexport (safe) */ __webpack_require__.d(__webpack_exports__, "staticRenderFns", function() { return _edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_loaders_templateLoader_js_vue_loader_options_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_preprocess_loader_index_js_ref_17_0_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_template_js_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_uni_app_loader_page_meta_js_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_index_js_vue_loader_options_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_style_js_status_vue_vue_type_template_id_ce7f4d56___WEBPACK_IMPORTED_MODULE_0__["staticRenderFns"]; });

/* harmony reexport (safe) */ __webpack_require__.d(__webpack_exports__, "recyclableRender", function() { return _edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_loaders_templateLoader_js_vue_loader_options_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_preprocess_loader_index_js_ref_17_0_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_template_js_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_uni_app_loader_page_meta_js_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_index_js_vue_loader_options_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_style_js_status_vue_vue_type_template_id_ce7f4d56___WEBPACK_IMPORTED_MODULE_0__["recyclableRender"]; });

/* harmony reexport (safe) */ __webpack_require__.d(__webpack_exports__, "components", function() { return _edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_loaders_templateLoader_js_vue_loader_options_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_preprocess_loader_index_js_ref_17_0_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_template_js_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_uni_app_loader_page_meta_js_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_index_js_vue_loader_options_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_style_js_status_vue_vue_type_template_id_ce7f4d56___WEBPACK_IMPORTED_MODULE_0__["components"]; });



/***/ }),

/***/ 235:
/*!*******************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
  !*** ./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib/loaders/templateLoader.js??vue-loader-options!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--17-0!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/template.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-uni-app-loader/page-meta.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!D:/sky-delivery/core/miniapp/pages/details/components/status.vue?vue&type=template&id=ce7f4d56& ***!
  \*******************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
/*! exports provided: render, staticRenderFns, recyclableRender, components */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "render", function() { return render; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "staticRenderFns", function() { return staticRenderFns; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "recyclableRender", function() { return recyclableRender; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "components", function() { return components; });
var components
var render = function () {
  var _vm = this
  var _h = _vm.$createElement
  var _c = _vm._self._c || _h
  var m0 = _vm.statusWord(_vm.orderDetailsData.status)
  _vm.$mp.data = Object.assign(
    {},
    {
      $root: {
        m0: m0,
      },
    }
  )
}
var recyclableRender = false
var staticRenderFns = []
render._withStripped = true



/***/ }),

/***/ 236:
/*!*************************************************************************************************!*\
  !*** D:/sky-delivery/core/miniapp/pages/details/components/status.vue?vue&type=script&lang=js& ***!
  \*************************************************************************************************/
/*! no static exports found */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var _edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_babel_loader_lib_index_js_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_preprocess_loader_index_js_ref_13_1_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_script_js_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_index_js_vue_loader_options_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_style_js_status_vue_vue_type_script_lang_js___WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! -!../../../../../../edgedownload/HBuilderX.5.06.2026033105/HBuilderX/plugins/uniapp-cli/node_modules/babel-loader/lib!../../../../../../edgedownload/HBuilderX.5.06.2026033105/HBuilderX/plugins/uniapp-cli/node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--13-1!../../../../../../edgedownload/HBuilderX.5.06.2026033105/HBuilderX/plugins/uniapp-cli/node_modules/@dcloudio/webpack-uni-mp-loader/lib/script.js!../../../../../../edgedownload/HBuilderX.5.06.2026033105/HBuilderX/plugins/uniapp-cli/node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!../../../../../../edgedownload/HBuilderX.5.06.2026033105/HBuilderX/plugins/uniapp-cli/node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!./status.vue?vue&type=script&lang=js& */ 237);
/* harmony import */ var _edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_babel_loader_lib_index_js_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_preprocess_loader_index_js_ref_13_1_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_script_js_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_index_js_vue_loader_options_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_style_js_status_vue_vue_type_script_lang_js___WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_babel_loader_lib_index_js_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_preprocess_loader_index_js_ref_13_1_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_script_js_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_index_js_vue_loader_options_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_style_js_status_vue_vue_type_script_lang_js___WEBPACK_IMPORTED_MODULE_0__);
/* harmony reexport (unknown) */ for(var __WEBPACK_IMPORT_KEY__ in _edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_babel_loader_lib_index_js_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_preprocess_loader_index_js_ref_13_1_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_script_js_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_index_js_vue_loader_options_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_style_js_status_vue_vue_type_script_lang_js___WEBPACK_IMPORTED_MODULE_0__) if(["default"].indexOf(__WEBPACK_IMPORT_KEY__) < 0) (function(key) { __webpack_require__.d(__webpack_exports__, key, function() { return _edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_babel_loader_lib_index_js_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_preprocess_loader_index_js_ref_13_1_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_script_js_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_index_js_vue_loader_options_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_style_js_status_vue_vue_type_script_lang_js___WEBPACK_IMPORTED_MODULE_0__[key]; }) }(__WEBPACK_IMPORT_KEY__));
 /* harmony default export */ __webpack_exports__["default"] = (_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_babel_loader_lib_index_js_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_webpack_preprocess_loader_index_js_ref_13_1_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_script_js_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_vue_cli_plugin_uni_packages_vue_loader_lib_index_js_vue_loader_options_edgedownload_HBuilderX_5_06_2026033105_HBuilderX_plugins_uniapp_cli_node_modules_dcloudio_webpack_uni_mp_loader_lib_style_js_status_vue_vue_type_script_lang_js___WEBPACK_IMPORTED_MODULE_0___default.a); 

/***/ }),

/***/ 237:
/*!********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************!*\
  !*** ./node_modules/babel-loader/lib!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/webpack-preprocess-loader??ref--13-1!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/script.js!./node_modules/@dcloudio/vue-cli-plugin-uni/packages/vue-loader/lib??vue-loader-options!./node_modules/@dcloudio/webpack-uni-mp-loader/lib/style.js!D:/sky-delivery/core/miniapp/pages/details/components/status.vue?vue&type=script&lang=js& ***!
  \********************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;
var _index = __webpack_require__(/*! @/utils/index */ 66);
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
var _default2 = {
  // 获取父级传的数据
  props: {
    // 订单详情
    orderDetailsData: {
      type: Object,
      default: function _default() {
        return {};
      }
    },
    // 倒计时间
    timeout: {
      type: Boolean,
      default: false
    },
    // 支付时间
    rocallTime: {
      type: String,
      default: ""
    }
  },
  methods: {
    // 地址选择
    statusWord: function statusWord(status) {
      this.$emit("statusWord", status);
      // return errr;
      return (0, _index.statusWord)(status);
    },
    //取消订单
    handleCancel: function handleCancel(type, obj) {
      this.$emit("handleCancel", {
        type: type,
        obj: obj
      });
    },
    // 立即支付
    handlePay: function handlePay(id) {
      this.$emit("handlePay", id);
    },
    // 催单
    handleReminder: function handleReminder(type, id) {
      this.$emit("handleReminder", {
        type: type,
        id: id
      });
    },
    // 申请退款
    handleRefund: function handleRefund(type) {
      this.$emit("handleRefund", type);
    },
    // 再来一单
    oneMoreOrder: function oneMoreOrder(order) {
      this.$emit("oneMoreOrder", order);
    }
  }
};
exports.default = _default2;

/***/ })

}]);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/pages/details/components/status.js.map
;(global["webpackJsonp"] = global["webpackJsonp"] || []).push([
    'pages/details/components/status-create-component',
    {
        'pages/details/components/status-create-component':(function(module, exports, __webpack_require__){
            __webpack_require__('2')['createComponent'](__webpack_require__(233))
        })
    },
    [['pages/details/components/status-create-component']]
]);
