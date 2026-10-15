<template>
  <a-select
    v-model:value="selectedValue"
    v-bind="attrs"
    showSearch
    :allowClear="allowClear"
    style="width: 100%"
    optionFilterProp="label"
    :placeholder="placeholder"
    :options="options"
    :notFoundContent="loading ? undefined : null"
    :dropdownAlign="{ overflow: { adjustY: adjustY } }"
    @change="handleChange"
  >
    <!-- <a-select-option v-for="d in options" :key="d.value" :value="d.value">{{ d.text }}</a-select-option> -->
  </a-select>
</template>

<script lang="ts">
import { defineComponent, PropType, ref, watchEffect, unref, watch, onMounted } from 'vue';
import { useAttrs } from '/@/hooks/core/useAttrs';
import { propTypes } from '/@/utils/propTypes';

import { defHttp } from '/@/utils/http/axios';
import { changeTableSortParams } from '/@/utils/ktTool';
import { cloneDeep } from 'lodash-es';

export default defineComponent({
  name: 'KtSelect',
  inheritAttrs: false,
  props: {
    value: propTypes.string.def(''),
    // type: propTypes.string.def(JInputTypeEnum.JINPUT_QUERY_LIKE),
    placeholder: propTypes.string.def(''),
    trim: propTypes.bool.def(false),
    //默认开启Y轴溢出位置调整，因此在可视空间不足时下拉框位置会自动上移，导致Select的输入框被遮挡。需要注意的是，默认情况是是可视空间，而不是所拥有的空间
    //update-begin-author:liusq date:2023-04-04 for:[issue/286]下拉搜索框遮挡问题
    adjustY: propTypes.bool.def(true),
    url: propTypes.string.def(''),
    optionKey: propTypes.string.def('name'),
    optionValue: propTypes.string.def('id'),
    isRequest: propTypes.string.def('request'),
    searchInfo: propTypes.object.def({}),
    setDefaultVaule: propTypes.bool.def(false), //是否初始化选中第一条数据
    allowClear: propTypes.bool.def(true),
    requestParams: propTypes.string.def(''), // 接口请求增加参数
  },
  emits: ['change', 'update:value'],
  setup(props, { emit }) {
    const attrs = useAttrs();
    const selectedValue = ref('');
    //表单值
    const showText = ref('');

    const options = ref<any[]>([]);
    const loading = ref(false);

    //绑定属性
    const getBindValue = Object.assign({}, unref(props), unref(attrs));
    const dataSourceObject = ref<any[]>([]);
    //监听value变化
    watch(
      () => props.value,
      () => {
        initVal();
      },
      { immediate: true }
    );
    watch(
      () => props.searchInfo,
      () => {
        initDictData(props.searchInfo);
      },
      { immediate: false }
    );
    watch(
      () => props.requestParams,
      (val) => {
        console.log('监听/workstation/reportColumn/getColumnAndRemarkByTableName', JSON.parse(val).lineCode);
        initDictData(JSON.parse(val));
      },
      { immediate: false }
    );

    onMounted(() => {
      if (props.isRequest === 'request') {
        let paramsObject = {};
        if (props.requestParams) {
          paramsObject = Object.assign(JSON.parse(props.requestParams));
        }

        initDictData({ ...props.searchInfo, ...paramsObject });
      }
    });

    /**
     * 初始化数值
     */
    function initVal() {
      if (!props.value) {
        selectedValue.value = undefined;
      } else {
        selectedValue.value = props.value;
      }
    }

    /**
     * 过滤选中option
     */
    function filterOption(input, option) {
      //update-begin-author:taoyan date:2022-11-8 for: issues/218 所有功能表单的下拉搜索框搜索无效
      let value = '',
        label = '';
      try {
        value = option.value;
        label = option.children()[0].children;
      } catch (e) {
        console.log('获取下拉项失败', e);
      }
      let str = input.toLowerCase();
      return value.toLowerCase().indexOf(str) >= 0 || label.toLowerCase().indexOf(str) >= 0;
      //update-end-author:taoyan date:2022-11-8 for: issues/218 所有功能表单的下拉搜索框搜索无效
    }

    /**
     * 初始化字典下拉数据
     */
    async function initDictData(params) {
      let { async, value, optionValue } = props;
      if (!params.orderField) {
        params.orderField = 'sort';
        params.orderType = 'asc';
      }
      const request = Object.assign({ pageNo: 1, pageSize: 9999, validFlag: true }, params);
      list(request).then((res) => {
        loading.value = false;
        // console.log('res:', res)
        options.value = [];
        if (res && res.records && res.records.length > 0) {
          options.value = res.records.map((item) => {
            return { label: item[props.optionKey], value: item[optionValue] };
          });
          dataSourceObject.value = res.records;
        } else if (res && res.length > 0) {
          options.value = res.map((item) => {
            return { label: item[props.optionKey], value: item[optionValue], disabled: item['disabled'] };
          });
          dataSourceObject.value = res;
        }
        setTimeout(() => {
          props.value ? (selectedValue.value = props.value) : props.setDefaultVaule ? (selectedValue.value = unref(options)[0].value) : '';
          emit('update:value', unref(selectedValue));
        }, 300);

        // console.log(unref(selectedValue), '------------------------selectedValue---------------------')
      });
    }

    /**
     * 同步改变事件
     * */
    function handleChange(value, label) {
      selectedValue.value = value;
      const getResult = options.value.find((item) => item.value === value);

      emit('update:value', unref(selectedValue));
      emit('text', getResult.label);
      emit('change');
    }
    /**
     * 列表接口
     * @param params
     */
    const list = (params) => {
      // changeTableSortParams(params);
      return defHttp.get({ url: props.url, params });
    };

    return { showText, attrs, getBindValue, filterOption, selectedValue, loading, options, handleChange, dataSourceObject };
  },
});
</script>

<style scoped></style>
