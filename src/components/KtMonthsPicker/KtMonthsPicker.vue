<template>
  <a-tree-select
    v-model:value="localSelectedValues"
    :show-search="showSearch"
    :style="{ width: '100%' }"
    :dropdown-style="dropdownStyle"
    :placeholder="placeholder"
    :allow-clear="allowClear"
    :multiple="multiple"
    :tree-checkable="treeCheckable"
    :tree-data="treeData"
    :max-tag-count="maxTagCount"
    showCheckedStrategy="SHOW_ALl"
    @check="onCheck"
  >
    <template #title="{ title }">
      {{ title }}
    </template>
  </a-tree-select>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue';
import dayjs from 'dayjs';
import { defineProps, defineEmits } from 'vue';

const props = defineProps({
  value: {
    type: Array,
    default: () => [],
  },
  showSearch: {
    type: Boolean,
    default: true,
  },
  allowClear: {
    type: Boolean,
    default: true,
  },
  multiple: {
    type: Boolean,
    default: true,
  },
  treeCheckable: {
    type: Boolean,
    default: true,
  },
  dropdownStyle: {
    type: Object,
    default: () => ({ maxHeight: '400px', overflow: 'auto' }),
  },
  placeholder: {
    type: String,
    default: "请选择",
  },
  maxTagCount: {
    type: Number,
    default: 1,
  },
});

const emit = defineEmits(['update:value']);

const currentYear = dayjs().year();
const currentMonth = dayjs().month() + 1; // month is zero-based in dayjs
const startYear = currentYear - 5;
const endYear = currentYear + 5;

const generateTreeData = () => {
  const treeData = [];
  for (let year = startYear; year <= endYear; year++) {
    const children = [];
    for (let month = 1; month <= 12; month++) {
      const value = `${year}-${month.toString().padStart(2, '0')}`;
      const title = `${year}-${month.toString().padStart(2, '0')}`;
      children.push({ value, title });
    }
    treeData.push({
      value: `${year}`,
      title: `${year}年`,
      children,
    });
  }
  return treeData;
};

const treeData = generateTreeData();

const localSelectedValues = ref([]);

onMounted(() => {
  if (!props.value.length) {
    localSelectedValues.value = [`${currentYear}-${currentMonth.toString().padStart(2, '0')}`];
    emit('update:value', localSelectedValues.value);
  } else {
    localSelectedValues.value = props.value;
  }
});

watch(
  () => props.value,
  (newVal) => {
    localSelectedValues.value = newVal;
  }
);

watch(
  localSelectedValues,
  (newVal) => {
    emit('update:value', newVal);
  }
);

const onCheck = (checkedKeys) => {
  localSelectedValues.value = checkedKeys.checked;
};
</script>

<style scoped>
/* Add your styles here */
</style>