<template>
  <!-- tree-checkable -->
  <a-tree-select
    v-if="isTreeSelect === true"
    v-bind="attrs"
    v-model:value="selectedValue"
    :tree-data="treeDataTemp"
    :placeholder="placeholder"
    :filterOption="filterOption"
    :dropdownAlign="{ overflow: { adjustY: adjustY } }"
    @change="handleChange"
    :check-strictly="true"
    tree-node-filter-prop="departName"
    show-search
  >
    <!-- <template #suffixIcon v-if="loading">
      <LoadingOutlined spin />
    </template> -->
  </a-tree-select>
  <a-select
    v-if="isTreeSelect === false"
    v-model:value="selectedValue"
    v-bind="attrs"
    show-search
    :placeholder="placeholder"
    :filterOption="filterOption"
    :allowClear="allowClear"
    @change="handleChange"
  >
    <a-select-option v-for="d in allOpts" :key="d.value" :value="d.value">{{ d.label }}</a-select-option>
  </a-select>
</template>

<script lang="ts">
import { defineComponent, PropType, ref, watchEffect, unref, watch, onMounted } from 'vue';
import { useAttrs } from '/@/hooks/core/useAttrs';
import { propTypes } from '/@/utils/propTypes';
import { queryDepartTreeSync, queryTreeList } from '/@/api/common/api';
import { TreeSelect } from 'ant-design-vue';
// import { LoadingOutlined } from '@ant-design/icons-vue';
import type { TreeSelectProps } from 'ant-design-vue';
import { createLocalStorage, createSessionStorage } from '/@/utils/cache';
import { useUserStore } from '/@/store/modules/user';

export default defineComponent({
  name: 'KtDevice',
  inheritAttrs: false,
  props: {
    value: propTypes.string.def(''),
    placeholder: propTypes.string.def(''),
    trim: propTypes.bool.def(false),
    adjustY: propTypes.bool.def(true),
    selectType: propTypes.string.def(''),
    parentKey: propTypes.string.def(''),
    setDefaultVaule: propTypes.bool.def(false), //是否初始化选中第一条数据
    allowClear: propTypes.bool.def(true),
    valueField: propTypes.string.def('orgCode'),
    isTreeSelect: propTypes.bool.def(false),
    defaultExpandLevel: propTypes.number.def(0)
  },
  emits: ['change', 'update:value', 'currentData'],
  setup(props, { emit }) {
    const attrs = useAttrs();
    const selectedValue = ref(undefined);
    //表单值
    const showText = ref('');
    const loading = ref(false);
    const isMultiple = ref(false);

    //绑定属性
    const getBindValue = Object.assign({}, unref(props), unref(attrs));

    const allOpts = ref<any[]>([]);

    //临时存储组件类型和组织结构数据
    const treeDataTemp = ref<any[]>([]);
    const ss = createSessionStorage();
    const departOfStore = createLocalStorage();
    const disabledBool = ref<Boolean>(true);
    //监听value变化
    watch(
      () => props.value,
      () => {
        initVal();
      },
      { immediate: true }
    );
    watch(
      () => props.parentKey,
      () => {
        if (treeDataTemp.value.length > 0) {
          allOpts.value = [];
          filterTreeData(treeDataTemp.value, props.selectType, '', '');
        }
      },
      { immediate: true }
    );

    onMounted(() => {
      initDictData();
    });

    /**
     * 初始化数值
     */
    function initVal() {
      if (!props.value) {
        selectedValue.value = undefined;
        // 根据右上角选中的部门查找他的父级工厂，并且设置当前工厂的默认值
        // setTimeout(() => {
        //   if (props.selectType === 'factory' && !selectedValue.value) {
        //     selectedValue.value = departOfStore.get('KT_CURRENT_FAC');
        //     emit('update:value', unref(selectedValue));
        //   } else if (props.selectType === 'depart' && !selectedValue.value) {
        //     selectedValue.value = departOfStore.get('KT_CURRENT_DEPART');
        //     emit('update:value', unref(selectedValue));
        //   }
        // }, 50);
      } else {
        selectedValue.value = props.value;

        // setTimeout(() => {
        //   selectedValue.value = props.value;
        // }, 50);

        if (props.selectType === 'line' && selectedValue.value) {
          const data = getSelectData(treeDataTemp.value, unref(selectedValue));
          emit('currentData', data);
        }
      }
    }
    /**
     * 过滤选中option
     */
    function filterOption(input, option) {
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

    function initSetLineDefaultVal() {
      if (
        props.setDefaultVaule &&
        (props.selectType === 'line' || props.selectType === 'factory' || props.selectType === 'depart') &&
        (!props.value || props.value === '')
      ) {
        selectedValue.value = allOpts.value[0].value;
        emit('update:value', unref(selectedValue));
        const data = getSelectData(treeDataTemp.value, unref(selectedValue));
        emit('currentData', data);
      }
    }

    /**
     * 初始化字典下拉数据
     */
    async function initDictData() {
      let { selectType, async, parentKey, pageSize, value } = props;
      if (value) {
        selectedValue.value = value.toString();
      }

      loading.value = true;

      //解决组件在同一个页面多次重复调用的问题，保证2s之内只调用一次
      setTimeout(() => {
        departOfStore.remove('KT_ALL_DEPARTS_FLAG');
      }, 2000);

      if (departOfStore.get('KT_ALL_DEPARTS_FLAG') && departOfStore.get('KT_ALL_DEPARTS')) {
        filterTreeData(departOfStore.get('KT_ALL_DEPARTS'), selectType, '', '');
        treeDataTemp.value = departOfStore.get('KT_ALL_DEPARTS');
        initSetLineDefaultVal();
        return;
      }
      departOfStore.set('KT_ALL_DEPARTS_FLAG', 'REQUEST');

      // primaryKey: '',
      queryTreeList({ pageNo: 1, pageSize: 9999 }).then((res) => {
        loading.value = false;
        if (res && res.length > 0) {
          // 过滤 - 当前用户对应工厂下全部数据
          const userStore = useUserStore();
          res.map((itemA) => {
            itemA?.children?.map((itemB) => {
              itemB.children = itemB?.children?.filter((itemC) => itemC.orgCode === userStore.factoryCode);
            });
          });

          treeDataTemp.value = res;
          // 加入工厂字段
          filterTreeData(res, selectType, '', '');
          initSetLineDefaultVal();
          // 存储数据
          departOfStore.set('KT_ALL_DEPARTS', res);
        }
      });
      // }
    }
    
    /**
     * 同步改变事件
     * */
    function handleChange(value) {
      selectedValue.value = value;
      emit('update:value', unref(selectedValue));
      emit('change', unref(selectedValue));

      const data = getSelectData(treeDataTemp.value, unref(selectedValue));
      emit('currentData', data);
      // callback();
    }

    // 数组去重
    function arrDistinctByProp(arr, prop) {
      //只返回第一次出现的，重复出现的过滤掉
      return arr.filter(function (item, index, self) {
        return self.findIndex((el) => el[prop] == item[prop]) === index;
      });
    }
    /**
     * 过滤数形数据，得到对应的显示树，产线，部门，工厂
     * */
    function filterTreeData(treeTypeData, type, parentOrgcode, factoryName) {
      for (let item in treeTypeData) {
        switch (type) {
          case 'line':
            if (treeTypeData[item].orgCategory === '5') {
              if (treeTypeData[item].children && treeTypeData[item].children.length > 0) {
                if (!props.parentKey || (parentOrgcode && parentOrgcode === props.parentKey)) {
                  treeTypeData[item].children.forEach((itemChild) => {
                    allOpts.value.push({ label: itemChild.title, value: itemChild[props.valueField] });
                  });
                }
              }
            } else if (treeTypeData[item].orgCategory === '6') {
              if (!props.parentKey || (parentOrgcode && parentOrgcode === props.parentKey)) {
                allOpts.value.push({ label: treeTypeData[item].title + '-' + factoryName, value: treeTypeData[item][props.valueField] });
              }
            }
            allOpts.value = arrDistinctByProp(allOpts.value, 'value');
            break;
          case 'depart':
            // console.log('走入depart');
            if (treeTypeData[item].orgCategory === '4') {
              if (!props.parentKey || (parentOrgcode && parentOrgcode === props.parentKey)) {
                allOpts.value.push({ label: treeTypeData[item].title, value: treeTypeData[item][props.valueField] });
              }
            }
            break;
          case 'factory':
            // console.log('走入factory');
            if (treeTypeData[item].orgCategory === '3') allOpts.value.push({ label: treeTypeData[item].title, value: treeTypeData[item].orgCode });
            break;
        }
        if (treeTypeData[item].orgCategory === '3') {
          factoryName = treeTypeData[item].title;
        }
        if (treeTypeData[item].children && treeTypeData[item].children.length > 0) {
          filterTreeData(treeTypeData[item].children, type, treeTypeData[item].orgCode, factoryName);
        }
      }
    }

    /**
     * 过滤数形数据，得到对应的显示树，产线，部门，工厂
     * */
    function getSelectData(treeTypeData, orgCode) {
      for (let item in treeTypeData) {
        let retTemp = '';
        if (treeTypeData[item].orgCode === orgCode) {
          return treeTypeData[item];
        }

        if (treeTypeData[item].children && treeTypeData[item].children.length > 0) {
          retTemp = getSelectData(treeTypeData[item].children, orgCode);
        }

        //找到值的时候返回，没有找到继续循环下去
        if (retTemp) {
          return retTemp;
        }
      }

      return '';
    }

    return { showText, attrs, getBindValue, filterOption, selectedValue, loading, handleChange, allOpts, treeDataTemp };
  },
});
</script>

<style scoped></style>
