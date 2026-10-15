<template>
  <a-row :class="['p-4', `${prefixCls}--box`]" :gutter="10">
    <a-col span="8">
      <a-card :bordered="false">
        <div class="tree-scroll-wrapper">
          <a-tree
            :tree-data="treeData"
            @select="handleSelect" 
            :fieldNames="{children:'children', key:'id' }"
            style="height: calc(100vh - 190px);"
            v-model:expandedKeys="expandedKeys"
          >
            <!-- 内容插槽 -->
            <template #title="node">
              <div style="width: 100%">
                <FolderOutlined v-if="lastLevel != node.level" />
                <FileTextOutlined v-else/>
                {{ node.nodeName }}
                <span class="ml">
                  <!-- 新增按钮 -->
                  <PlusSquareTwoTone v-if="lastLevel != node.level" two-tone-color="#52c41a" @click.stop="handleAdd(node)" />
                  <!-- 编辑按钮 -->
                  <EditTwoTone v-if="0 != node.id" style="margin-left: 5px;" @click.stop="handleEdit(node)" />
                  <!-- 删除按钮 -->
                  <DeleteTwoTone v-if="0 != node.id" two-tone-color="#D92622" style="margin-left: 5px;" @click.stop="handleDelete(node)" />
                </span>
              </div>
            </template>
          </a-tree>
        </div>
      </a-card>
    </a-col>
    <a-col span="16">
      <a-card :bordered="false" style="height: calc(100vh - 150px);">
        <CpkConfigDetailList v-show="showConfigDetail" ref="configDetailtRef"/>
      </a-card>
    </a-col>
  </a-row>
  <!--表单弹窗-->
  <CpkConfigModal @register="registerModal" @success="handleSuccess" />
</template>

<script lang="ts" setup name="cpk-cpkConfigIndex">
import { onMounted, provide, ref } from 'vue';
import { useModal } from '/@/components/Modal';
import { useDesign } from '/@/hooks/web/useDesign';
import { useMessage } from '/@/hooks/web/useMessage';
import { PlusSquareTwoTone, DeleteTwoTone, EditTwoTone, FolderOutlined, FileTextOutlined } from '@ant-design/icons-vue';
import { loadTreeData, deleteCpkConfig } from './CpkConfig.api';
import CpkConfigModal from './components/CpkConfigModal.vue';
import CpkConfigDetailList from './components/CpkConfigDetailList.vue';
const { createConfirm } = useMessage();
const { prefixCls } = useDesign('depart-user');
provide('prefixCls', prefixCls);
//最后一级
const lastLevel = ref(5);
//需要展开的节点
const expandedKeys = ref(['0']);
//树数据
const treeData = ref([
  {
    id: '0',
    nodeLevel: 0,
    nodeName: '指标配置',
    children: [],
  }]);
//字典model
const [registerModal, { openModal }] = useModal();
//显示子组件
const showConfigDetail = ref(false);
//子组件的引用
const configDetailtRef = ref();
//加载数据
async function initData() {
  treeData.value[0].children = await loadTreeData({ pcode: '', async: false });
}
//树点击
async function handleSelect(selectedKeys, e) {
  const node = e.selectedNodes[0];
  showConfigDetail.value = node?.nodeLevel == lastLevel.value ? true : false;
  if(node.level == lastLevel.value){
    configDetailtRef.value.initData(node);
  }
}
//新增
function handleAdd(node){
  let nodeLevel = Number(node.nodeLevel) + 1;
  if(node.id == 0){
    openModal(true, {
      record: {
        nodeLevel
      },
      isUpdate: false,
    });
  }else{
    openModal(true, {
      record: { 
        parentId: node.id,
        parentCode: node.nodeCode,
        deptCode: node.deptCode,
        nodeLevel
      },
      isUpdate: false,
    });
  }
}

/**
 * 编辑事件
 */
async function handleEdit(record) {
  openModal(true, {
    record,
    isUpdate: true,
  });
}

/**
 * 删除事件
 */
async function handleDelete(record) {
  createConfirm({
    iconType: 'warning',
    title: '确认删除',
    content: '是否删除?',
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      await deleteCpkConfig({ id: record.id }, initData);
    }
  });
}

async function handleSuccess({isUpdate, parentId}){
  await initData();
  //如果是新增，将节点展开
  if(!isUpdate && parentId){
    // 去重后添加
    const uniqueKeys = new Set([...expandedKeys.value, parentId]);
    expandedKeys.value = [...uniqueKeys];
  }
}

onMounted(async () => {
  await initData();
});
</script>
<style lang="css" scoped>
  :deep(.ant-card-body){
    padding: 10px;
  }

  
.tree-scroll-wrapper {
  width: 100%; 
  height: calc(100vh - 170px);
  overflow: auto;
}

/* 关键：穿透样式，强制 Tree 内所有文本不换行 */
.tree-scroll-wrapper :deep(.ant-tree-node-content-wrapper) {
  white-space: nowrap;
  display: inline-block; /* 确保整体作为行内块 */
}

/* 可选：防止图标和文字之间因空间不足分离 */
.tree-scroll-wrapper :deep(.ant-tree-title) {
  white-space: nowrap;
  display: inline-block;
}
</style>