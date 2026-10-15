<template>
  <a-tree-select
    v-bind="attrs"
    v-model:value="selectedValue"
    :tree-data="treeData"
    :treeDefaultExpandedKeys="expandedKeys"
    :placeholder="placeholder"
    :filterOption="filterOption"
    :dropdownAlign="{ overflow: { adjustY: adjustY } }"
    :check-strictly="true"
    :allowClear="allowClear"
    tree-node-filter-prop="departName"
    show-search
  >
  </a-tree-select>
</template>

<script lang="ts" setup name="KtDepartTreeSelect">
import { useAttrs } from '/@/hooks/core/useAttrs';
import { propTypes } from '/@/utils/propTypes';
import { queryTreeList } from '/@/api/common/api';
import { onMounted, ref, unref, watch } from 'vue';
import { useUserStore } from '/@/store/modules/user';
const attrs = useAttrs();
const userStore = useUserStore();
// 定义组件的 emits
const emit = defineEmits(['update:value']);
// 定义组件的 props
const props = defineProps({
  value: propTypes.string.def(undefined),
  allowClear: propTypes.bool.def(true),
  showSearch: propTypes.bool.def(true),
  placeholder: propTypes.string.def('请选择'),
  bordered: propTypes.bool.def(true),
  adjustY: propTypes.bool.def(true),
  defaultExpandLevel: propTypes.number.def(1),
  showAll: propTypes.bool.def(false),
  byUserOrg: propTypes.bool.def(false),
});
//选中值
const selectedValue = ref(props.value);
//数据
const treeData = ref([] as any);
//展开的节点
const expandedKeys = ref([]) as any;
//绑定属性
const getBindValue = Object.assign({}, unref(props), unref(attrs));

/**
 * 过滤选中option
 */
function filterOption(input, option) {
  let value = '', label = '';
  try {
    value = option.value;
    label = option.children()[0].children;
  } catch (e) {
    console.log('获取下拉项失败', e);
  }
  let str = input.toLowerCase();
  return value.toLowerCase().indexOf(str) >= 0 || label.toLowerCase().indexOf(str) >= 0;
}

/**
 * 获取默认需要展开的节点
 */
function calculateExpandedKeys(data, level){
  if (!data || level >= props.defaultExpandLevel) return;
  let key = getBindValue?.fieldNames?.value || 'key' ;
  for (let i = 0; i < data.length; i++) {
    if (level < props.defaultExpandLevel) {
      expandedKeys.value.push(data[i][key]);
    }
    if (data[i].children && data[i].children.length > 0) {
      calculateExpandedKeys(data[i].children, level + 1);
    }
  }
}

/**
 * 
 * @param data 处理数据
 * @param fieldNames 取值对象 
 */
function processData(data, fieldNames){
  let result = [] as any;
  for(const item of data){
    let obj = {} as any;
    if(item.children && item.children.length > 0){
      obj.children = processData(item.children, fieldNames);
    }
    obj[fieldNames.label] = item[fieldNames.label];
    obj[fieldNames.value] = item[fieldNames.value];
    result.push(obj);
  }
  return result;
}

watch(
  () => props.value,
  (newValue) => {
    selectedValue.value = newValue;
  }
);

watch(selectedValue, (newValue) => {
  emit('update:value', newValue);
});

onMounted(async () => {
  queryTreeList().then((res) => {
    let data = [];
    if(props.showAll){
      // 显示所有
      data = res;
    }else if(props.byUserOrg){
      //根据右上角所选部门及其子级
      let orgCode = userStore.getUserInfo.orgCode;
      const findNodeByCode = (nodes, code) => {
        for (const node of nodes) {
          if (node.orgCode === code) {
            return node;
          }
          if (node.children && node.children.length > 0) {
            const found = findNodeByCode(node.children, code);
            if (found) {
              return found;
            }
          }
        }
        return null;
      };
      const targetNode = findNodeByCode(res, orgCode);
      data = targetNode ? [targetNode] : [];
    }else{
      data = res[0].children[0].children;
    }
    if(props.defaultExpandLevel > 0 && expandedKeys.value.length === 0){
      calculateExpandedKeys(data, 0);
    }
    if(getBindValue?.fieldNames){
      data = processData(data, getBindValue?.fieldNames);
    }
    treeData.value = data;
  });
});
</script>

<style scoped></style>
