<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable">
      <!--插槽:table标题-->
      <template #tableTitle>
        <div style="width:100%; text-align: center; font-size: 1.1rem; font-weight: bold;">
          <span>进行中：{{ statisticalData.actionIng }}</span> 
          <span style="margin-left:30px">延期：{{ statisticalData.deferCount}}</span>
          <span style="margin-left:30px">已完成：{{ statisticalData.actionOk }}</span>
        </div>
      </template>
      
      <!--插槽:actionTitle-->
      <template #actionTitle="{ record }">
        <span>
          <a-button type="link" class="actionTitle" @click="handleOpenModal(record, 2)"><span>{{ record.actionTitle }}</span></a-button>
        </span>
      </template>
    </BasicTable>

    <!-- 维护modal -->
    <RaModal @register="registerModal" @success="onSuccess"></RaModal>
  </div>
</template>

<script lang="ts" name="action.plan-raList" setup>
import { onMounted, reactive } from 'vue';
import { BasicTable } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useListPage } from '/@/hooks/system/useListPage'
import { columns } from '../Ra.data';
import RaModal from './RaModal.vue'
// Emits声明
const emit = defineEmits(['success']);
//完成情况统计数据信息
const statisticalData = reactive<any>({
  actionIng: 0,
  deferCount: 0,
  actionOk: 0
})
//父组件传值
const props = defineProps({
  listType: { type: Number, default: 1 },
});

//注册model
const [registerModal, { openModal }] = useModal();

//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    columns,
    showIndexColumn: true,
    showActionColumn: false,
    showTableSetting: false,
    pagination: false,
    maxHeight: 400
  },
})
const [registerTable, { getColumns, setColumns, setTableData, setLoading }] = tableContext;

/**
 * 维护事件
 */
function handleOpenModal(record, type) {
  record.visible = false;
  openModal(true, {
    record,
    type
  });
}

//设置表格数据
function reloadData(tableData){
  statisticalData.actionIng = tableData.filter(item => item.finishState == 0).length;
  statisticalData.deferCount = tableData.filter(item => item.finishState == 2 || item.finishState == 4).length;
  statisticalData.actionOk = tableData.filter(item => item.finishState == 1).length;
  setTableData(tableData);
  setLoading(false);
}

onMounted(() => {
  //根据类型设置表格列名
  const newColumns = getColumns();
  let title = '';
  if(1 == props.listType){ 
    //提出
    title = '提出R';
  } else if(2 == props.listType){
    //责任
    title = '责任A';
  } else if(3 == props.listType){
    //执行
    title = '执行N';
  } else if(4 == props.listType){
    //确认
    title = '确认M';
  }
  newColumns.find(item => item.dataIndex === 'actionTitle').title = title;
  setColumns(newColumns);
})

function onSuccess(){
  emit('success');
}

//暴露方法
defineExpose({
  reloadData,
  setLoading
})
</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}

:deep(.jeecg-basic-table-header__toolbar) {
  display: none;
}

.actionTitle{
  max-width: 100%; 
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  padding: 0;
  span{
    width: 100%;
    text-decoration: underline;
    color: #D92622;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}
</style>