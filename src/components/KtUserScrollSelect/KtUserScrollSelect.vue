<!-- 用户选择下拉框根据滚动条翻页组件 -->
<template>
  <a-select
    v-model:value="selectedValue"
    show-search
    :filter-option="false"
    @search="debouncedOnSearch"
    @popupScroll="handlePopupScroll"
    @dropdownVisibleChange="handleDropdown"
    :showSearch=showSearch
    :allowClear="allowClear"
    :placeholder= "placeholder"
    :bordered="bordered"
    style="width: 100%;"
  >
    <a-select-option v-for="item in options" :key="item.realname" :value="item.id">
      {{ item.realname }}
    </a-select-option>
  </a-select>
</template>

<script setup name="KtUserScrollSelect">
import { ref, defineProps, defineEmits, watch, onMounted, toRefs, nextTick } from 'vue';
import { getUserListAll } from '/@/api/common/api';
import { propTypes } from '/@/utils/propTypes';
import { cloneDeep } from 'lodash-es';

// 定义组件的 props
const props = defineProps({
  value: propTypes.string.def(''),
  pageSize: {
    type: Number,
    default: 50,
    // 自定义验证函数，确保 pageSize 是一个正整数
    validator: (value) => Number.isInteger(value) && value > 0,
  },
  allowClear: propTypes.bool.def(true),
  showSearch: propTypes.bool.def(true),
  placeholder: propTypes.string.def('请选择用户'),
  bordered: propTypes.bool.def(true),
});
// 定义组件的 emits
const emit = defineEmits(['update:value']);

// 定义响应式变量
const selectedValue = ref(null); // 当前选中的值
const options = ref([]); // 下拉选项列表
let page = ref(1); // 当前页码
const loading = ref(false); // 加载状态
//记录原始选项，避免第一也不存在所选择的用户，无法显示
const isNeedSearch = ref(true);
//记录搜索
const keyWord = ref('');

const oldObject = ref({
  oldPage: 1,
  oldOptions: []
});

// 获取选项数据的函数
const fetchOptions = async (searchQuery) => {
  if (loading.value) return; // 如果正在加载，则返回
  loading.value = true; // 设置加载状态为 true
  try {
    searchQuery = searchQuery ? `*${searchQuery}*` : null;
    const response = await getUserListAll({pageNo: page.value, pageSize: props.pageSize, realname: searchQuery});
    let filteredItems = response.records;
    if(!searchQuery){
      //非搜索状态排除掉已选择的数据，避免出现bug
      filteredItems = filteredItems.filter(item => item.id !== selectedValue.value);
    }
    await options.value.push(...filteredItems); // 将新获取的数据添加到选项列表中
    if(!searchQuery){
      oldObject.value.oldPage = cloneDeep(page.value);
      oldObject.value.oldOptions = cloneDeep(options.value);
    }
  } catch (error) {
    console.error('获取选项时出错:', error); // 捕获并打印错误信息
  } finally {
    loading.value = false; // 设置加载状态为 false
  }
};

// 处理搜索事件的函数
const onSearch = async (searchQuery) => {
  if (!searchQuery) return; // 如果搜索词为空，则返回
  page.value = 1; // 重置页码
  options.value = []; // 清空选项列表
  await fetchOptions(searchQuery); // 调用 fetchOptions 获取新的选项数据
  keyWord.value = searchQuery;
};

// 手动实现防抖函数
let debounceTimeout;
const debouncedOnSearch = (searchQuery) => {
  clearTimeout(debounceTimeout);
  debounceTimeout = setTimeout(() => {
    onSearch(searchQuery);
  }, 300); // 防抖时间为 300 毫秒
};

// 处理下拉菜单滚动事件的函数
const handlePopupScroll = async (event) => {
  const target = event.target;
  // 判断是否滚动到底部
  if (
    target.scrollTop + target.offsetHeight >= target.scrollHeight - 100 &&
    !loading.value
  ) {
    page.value += 1; // 增加页码
    await fetchOptions(keyWord.value); // 调用 fetchOptions 获取更多选项数据
  }
};

//下拉时关闭时重置数据
function handleDropdown(open){
  if(!open){
    //将选中值加入到oldObject中，避免下次下拉时丢失选中值
    const findData = options.value.find(item => item.id === selectedValue.value);
    if(findData){
      const isDuplicate = oldObject.value.oldOptions.some(item => item.id === findData?.id);
      if (!isDuplicate) {
        oldObject.value.oldOptions.push(findData);
      }
    }

    //关闭下拉时还原数据
    keyWord.value = '';
    page.value = cloneDeep(oldObject.value.oldPage);
    options.value = cloneDeep(oldObject.value.oldOptions);
  }
}

async function ensureOptionExists(){
  isNeedSearch.value = false;
  const response = await getUserListAll({pageNo: page.value, pageSize: props.pageSize, id: props.value});
  options.value.push(...response.records); // 将新获取的数据添加到选项列表中
}
let ensureOptionTimeout;
onMounted(() => {
  // 组件挂载后，调用 onSearch 获取初始选项数据
  fetchOptions();
  oldObject.value.oldPage = 1;
  oldObject.value.oldOptions = [];
  if(props.value){
    selectedValue.value = props.value;
    ensureOptionExists();
  }
});

// 监听 selectedValue 的变化，并通过 emit 通知父组件
watch(
  () => selectedValue.value,
  (newValue) => {
    emit('update:value', newValue);
  }
);

watch(
  () => props.value,
  async (newValue,value) => {
    selectedValue.value = props.value;
    if(true === isNeedSearch.value){
      // 延迟执行，避免设置了defaultValue时，下拉框不显示选中值的问题
      clearTimeout(ensureOptionTimeout);
      ensureOptionTimeout = setTimeout(() => {
        ensureOptionExists();
      }, 500);
    }
  }
);

</script>

<style scoped>
/* 添加任何特定于组件的样式 */
</style>