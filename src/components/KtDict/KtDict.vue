<template>
  <!-- <a-input v-bind="getBindValue" v-model:value="showText" @input="backValue"></a-input> -->
  <a-select
    v-model:value="selectedValue"
    v-bind="attrs"
    showSearch
    :placeholder="placeholder"
    :filterOption="filterOption"
    :notFoundContent="loading ? undefined : null"
    :dropdownAlign="{ overflow: { adjustY: adjustY } }"
    @change="handleChange"
    :size="size"
    allow-clear
  >
    <!-- <template #notFoundContent>
      <a-spin v-if="loading" size="small" />
    </template> -->
    <a-select-option v-for="d in options" :key="d.value" :value="d.value">{{ d.text }}</a-select-option>
  </a-select>
</template>

<script lang="ts">
import { defineComponent, PropType, ref, watchEffect, unref, watch, onMounted } from 'vue';
import { useAttrs } from '/@/hooks/core/useAttrs';
import { propTypes } from '/@/utils/propTypes';
// import { JInputTypeEnum } from '/@/enums/jeecgEnum.ts';
import { defHttp } from '/@/utils/http/axios';
import { itemList } from './ktDict.api';

export default defineComponent({
  name: 'KtDict',
  inheritAttrs: false,
  props: {
    value: propTypes.string.def(''),
    // type: propTypes.string.def(JInputTypeEnum.JINPUT_QUERY_LIKE),
    placeholder: propTypes.string.def(''),
    trim: propTypes.bool.def(false),
    //默认开启Y轴溢出位置调整，因此在可视空间不足时下拉框位置会自动上移，导致Select的输入框被遮挡。需要注意的是，默认情况是是可视空间，而不是所拥有的空间
    //update-begin-author:liusq date:2023-04-04 for:[issue/286]下拉搜索框遮挡问题
    adjustY: propTypes.bool.def(true),
    dict: propTypes.string.def(''),
    size: propTypes.string.def('middle'),
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
    //监听value变化
    watch(
      () => props.value,
      () => {
        initVal();
      },
      { immediate: true }
    );

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
    async function initDictData() {
      let { dict, async, dictOptions, pageSize, value } = props;
      if (value) {
        selectedValue.value = value.toString();
      }

      // console.log('selectedValue.value:', selectedValue.value)

      if (!dict) {
        console.error('搜索组件未配置字典项');
      } else {
        //异步一开始也加载一点数据
        loading.value = true;
        // let keywordInfo = getKeywordParam('');
        // defHttp
        //   .get({
        //     url: `/sys/dict/loadDict/${dict}`,
        //     params: { pageSize: pageSize, keyword: keywordInfo },
        //   })

        itemList({ groupCode: dict, pageNo: 1, pageSize: 999, orderField: 'sort', orderType: 'asc' }).then((res) => {
          loading.value = false;
          if (res && res.records && res.records.length > 0) {
            options.value = res.records.map((item) => {
              return { text: item.itemKey, value: item.itemVal };
            });
            // console.log('options.value:', options.value)

            initVal();
          }
        });
      }
    }

    //获取关键词参数 支持设置排序信息
    function getKeywordParam(text) {
      // 如果设定了排序信息，需要写入排序信息，在关键词后加 [orderby:create_time,desc]
      if (props.params && props.params.column && props.params.order) {
        let temp = text || '';

        //update-begin-author:taoyan date:2023-5-22 for: /issues/4905 表单生成器字段配置时，选择关联字段，在进行高级配置时，无法加载数据库列表，提示 Sgin签名校验错误！ #4905
        temp = temp + '[orderby:' + props.params.column + ',' + props.params.order + ']';
        return encodeURI(temp);
        //update-end-author:taoyan date:2023-5-22 for: /issues/4905 表单生成器字段配置时，选择关联字段，在进行高级配置时，无法加载数据库列表，提示 Sgin签名校验错误！ #4905
      } else {
        return text;
      }
    }

    /**
     * 同步改变事件
     * */
    function handleChange(value) {
      selectedValue.value = value;
      emit('update:value', unref(selectedValue));
      emit('change', unref(selectedValue));
      // callback();
    }

    return { showText, attrs, getBindValue, backValue, filterOption, selectedValue, loading, options, handleChange };
  },
});
</script>

<style scoped></style>
