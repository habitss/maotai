<template>
  <a-cascader
    v-model:value="innerValue"
    :options="treeData"
    :show-search="true"
    :field-names="{ label: 'title', value: 'key', children: 'children' }"
    placeholder="请选择部门区域"
    @change="handleChange"
    style="width: 100%"
    change-on-select
  />
</template>

<script lang="ts" setup>
  import { ref, watch, onMounted } from 'vue';
  import type { CascaderProps } from 'ant-design-vue';
  import { defHttp } from '/@/utils/http/axios';

  interface Props {
    showDefault?: boolean;
    modelValue?: string;
  }

  //根据当前登录信息获取组织机构数据
  const getDeptTree = () => {
    return defHttp.get({ url: '/ems/sys/queryDeptTree' });
  };

  const props = withDefaults(defineProps<Props>(), {
    showDefault: false,
  });

  const emits = defineEmits(['update:modelValue', 'change']);
  const innerValue = ref<string[]>([]);
  const treeData = ref<CascaderProps[]>([]);

  //自动选择单节点
  const autoSelectPath = (nodes): string[] => {
    const path: string[] = [];
    let current = nodes;
    while (current && current.length === 1) {
      const node = current[0];
      path.push(String(node.key));
      current = node.children;
    }
    return path;
  };

  const loadTree = async () => {
    const res = await getDeptTree();
    treeData.value = res;
    if (props.showDefault) {
      showDefaultPath();
    }
  };

  // 若 modelValue 为空，则自动选择单一路径
  function showDefaultPath() {
    if (!props.modelValue) {
      const path = autoSelectPath(treeData.value);
      if (path.length > 0) {
        innerValue.value = path;
        emits('update:modelValue', path.join('/'));
        emits('change', path);
      }
    }
  }

  const initValue = async () => {
    console.log(props.modelValue, '11111', props.showDefault);
    if (!props.modelValue) {
      //传空时如果开启默认值强制选自己部门
      if (props.showDefault) {
        const path = autoSelectPath(treeData.value);
        if (path.length > 0) {
          innerValue.value = path;
        }
      } else {
        innerValue.value = [];
      }
    } else if (props.modelValue.includes('/')) {
      innerValue.value = props.modelValue.split('/');
    } else {
      const code = props.modelValue;
      const result: string[] = [];
      const step = 3;
      for (let i = 3; i <= 6; i++) {
        if (code.length >= i * step) {
          result.push(code.substring(0, i * step));
        }
      }
      innerValue.value = result;
    }
    emits('change', innerValue.value);
  };

  const handleChange = (val: string[]) => {
    if (val === undefined) {
      val = [];
    }
    //开启默认值后强制选择自己部门，不能修改
    if (props.showDefault) {
      const path = autoSelectPath(treeData.value);
      if (path.length > 0 && path.length > val.length) {
        innerValue.value = path;
        val = path;
      }
    }
    emits('update:modelValue', val.join('/'));
    emits('change', val);
  };

  watch(() => props.modelValue, initValue, { immediate: true });
  onMounted(() => loadTree());

  defineExpose({
    showDefaultPath,
  });
</script>
