<template>
  <!-- <a-input v-bind="getBindValue" v-model:value="showText" @input="backValue"></a-input> -->
  <a-select
    v-model:value="selectedValue"
    v-bind="attrs"
    showSearch
    :placeholder="placeholder"
    :notFoundContent="loading ? undefined : null"
    :dropdownAlign="{ overflow: { adjustY: adjustY } }"
    :options="options"
    optionFilterProp="label"
    allow-clear
    :mode="mode"
    @change="handleChange"
  >
    <!-- <template #notFoundContent>   :filter-option="filterOption"
       :notFoundContent="loading ? undefined : null"
    :dropdownAlign="{overflow: {adjustY: adjustY }}"
      <a-spin v-if="loading" size="small" />
    </template> -->
    <!-- <a-select-option v-for="d in options" :key="d.value" :value="d.value">{{ d.text }}</a-select-option> -->
  </a-select>
</template>

<script lang="ts">
import { defineComponent, PropType, ref, watchEffect, unref, watch, onMounted } from 'vue';
import { useAttrs } from '/@/hooks/core/useAttrs';
import { propTypes } from '/@/utils/propTypes';
// import { JInputTypeEnum } from '/@/enums/jeecgEnum.ts';
import { defHttp } from '/@/utils/http/axios';
import { deviceTypeList, deviceList } from './KtDevice.api';

export default defineComponent({
  name: 'KtDevice',
  inheritAttrs: false,
  props: {
    value: propTypes.string.def(''),
    // type: propTypes.string.def(JInputTypeEnum.JINPUT_QUERY_LIKE),
    placeholder: propTypes.string.def(''),
    trim: propTypes.bool.def(false),
    //默认开启Y轴溢出位置调整，因此在可视空间不足时下拉框位置会自动上移，导致Select的输入框被遮挡。需要注意的是，默认情况是是可视空间，而不是所拥有的空间
    //update-begin-author:liusq date:2023-04-04 for:[issue/286]下拉搜索框遮挡问题
    adjustY: propTypes.bool.def(true),
    dict: propTypes.string.def(''), //机构类型字段
    type: propTypes.string.def('device-type'),
    lineCode: propTypes.string.def(''), //产线
    deptCode: propTypes.string.def(''), //部门
    mode: propTypes.string.def(''),
    setDefaultVaule: propTypes.bool.def(false), //是否初始化选中第一条数据
  },
  emits: ['change', 'update:value', 'currentData'],
  setup(props, { emit }) {
    const attrs = useAttrs();
    const selectedValue = ref('');
    //表单值
    const showText = ref('');

    const options = ref<any[]>([]);
    const loading = ref(false);

    //绑定属性
    const getBindValue = Object.assign({}, unref(props), unref(attrs));

    // 初始数组
    const defaultOptions = ref<any>();

    //监听value变化
    watch(
      () => props.value,
      () => {
        initVal();
      },
      { immediate: true }
    );
    watch(
      () => props.dict,
      () => {
        initDictData();
      },
      { immediate: true }
    );
    watch(
      () => props.lineCode,
      () => {
        initDictData();
      },
      { immediate: true }
    );
    watch(
      () => props.deptCode,
      () => {
        initDictData();
      },
      { immediate: true }
    )

    onMounted(() => {
      // console.log(`the component is now mounted.`);
      initDictData();
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
      // console.log('options:', options);
    }

    /**
     * 返回值
     */
    function backValue(e) {
      let text = e?.target?.value ?? '';
      if (text && !!props.trim) {
        text = text.trim();
      }
      emit('change', text);
      emit('update:value', text);
    }

    /**
     * 过滤选中option
     */
    const filterOption = (input: string, option: any) => {
      return option.text.toLowerCase().indexOf(input.toLowerCase()) >= 0;
    };

    /**
     * 初始化字典下拉数据
     */
    async function initDictData() {
      let { dict, type, async, dictOptions, pageSize, value, lineCode, deptCode } = props;

      if (type === 'device-type') {
        deviceTypeList({ orgTypeCode: dict, pageNo: 1, pageSize: 9999, orderField: 'sort', orderType: 'asc' }).then((res) => {
          loading.value = false;
          options.value = [];
          value ? (selectedValue.value = value.toString()) : (selectedValue.value = undefined);

          if (res && res.records && res.records.length > 0) {
            options.value = res.records.map((item) => {
              return { label: item.deviceTypeName, value: item.id };
            });
          }

          initVal();
        });
      } else {
        deviceList({ deviceTypeId: dict, pageNo: 1, pageSize: 9999, lineCode: lineCode,deptCode: deptCode, orderField: 'sort', orderType: 'asc' }).then((res) => {
          loading.value = false;
          options.value = [];
          value ? (selectedValue.value = value.toString()) : (selectedValue.value = undefined);

          if (res && res.records && res.records.length > 0) {
            defaultOptions.value = JSON.parse(JSON.stringify(res.records));
            options.value = res.records.map((item) => {
              return { label: item.deviceName, value: item.id };
            });
            initSetLineDefaultVal();
          }
          initVal();
        });
      }
    }
    function initSetLineDefaultVal() {
      if (props.setDefaultVaule && (!props.value || props.value === '')) {
        selectedValue.value = options.value[0].value;
        emit('update:value', unref(selectedValue));
        const resend = fiterOptions(options.value[0].value);
        emit('currentData', resend);
      }
    }
    /**
     * 同步改变事件
     * */
    function handleChange(value) {
      selectedValue.value = value;
      emit('update:value', unref(selectedValue));
      emit('change', unref(selectedValue));
      const resend = fiterOptions(value);
      emit('currentData', resend);
      // callback();
    }

    function fiterOptions(id) {
      const obj = defaultOptions.value.find((item) => item.id === id);
      return obj;
    }

    return { showText, attrs, getBindValue, backValue, filterOption, selectedValue, loading, options, handleChange, mode: props.mode };
  },
});
</script>

<style scoped></style>
