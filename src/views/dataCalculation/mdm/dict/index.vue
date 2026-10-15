<template>
  <a-row :class="['p-4', `${prefixCls}--box`]" :gutter="10">
    <a-col :xl="6" :lg="8" :md="10" :sm="24" style="flex: 1">
      <a-card :bordered="false">
        <div style="height: calc(100vh - 230px); overflow: auto;">
          <BasicTree checkable :treeData="treeData" @select="handleSelect" @check="handleCheck" :selectedKeys />
        </div>
        <a-row :gutter="10">
          <a-col :span="12" :align="'right'">
            <a-button type="primary" @click="handleCreate">添加</a-button>
          </a-col>
          <a-col :span="12" :align="'left'">
            <a-button danger @click="handleDelete">删除</a-button>
          </a-col>
        </a-row>
      </a-card>
    </a-col>
    <a-col :xl="18" :lg="16" :md="14" :sm="24" style="flex: 1">
      <a-card :bordered="false" style="height: calc(100vh - 150px);">
        <DictList v-if="treeData && treeData.length > 0" ref="dictListRef"/>
      </a-card>
    </a-col>
  </a-row>
  <DictCategorizeModal @register="registerModal" @success="initData" />
</template>

<script lang="ts" setup name="mes3-mdm-dict-categorize">
import { loadTreeData, deletionBatchDictCategorize } from './dict.api';
import { onMounted, provide, ref } from 'vue';
import { useModal } from '/@/components/Modal';
import { useDesign } from '/@/hooks/web/useDesign';
import { BasicTree } from '/@/components/Tree';
import DictCategorizeModal from './components/DictCategorizeModal.vue';
import DictList from './components/DictList.vue';
import { useMessage } from '/@/hooks/web/useMessage';
const { createConfirm } = useMessage();
const { prefixCls } = useDesign('depart-user');
provide('prefixCls', prefixCls);
//选中值
const selectedKeys = ref([]);
//勾选值
const checkedIds = ref();
//树数据
const treeData = ref([]);
//字典model
const [registerModal, { openModal }] = useModal();
// 创建一个对子组件的引用
const dictListRef = ref();
//加载数据
async function initData(){
  treeData.value = await loadTreeData({ pcode: 0, async: false });
}
//树点击
function handleSelect(selectedKeys){
  selectedKeys.value = selectedKeys;
  dictListRef?.value?.reloadList(selectedKeys[0]);
}
//树勾选
function handleCheck(checkedKeys, e){
  checkedIds.value = checkedKeys;
}
/**
 * 新增事件
 */
function handleCreate() {
  openModal(true, {
    isUpdate: false,
    showFooter: true,
  });
}

/**
 * 删除事件
 */
async function handleDelete(){
  if(!checkedIds.value || checkedIds.value.length === 0){
    return;
  }
  console.log('checkedIds.value :>> ', checkedIds.value);
  await deletionBatchDictCategorize({ids: checkedIds.value.join(',')}, initData);
}

onMounted(async () => {
  await initData();
  dictListRef?.value?.reloadList(treeData.value[0].key);
  selectedKeys.value = [treeData.value[0].key];
});
</script>
