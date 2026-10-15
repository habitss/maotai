<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button type="primary" @click="handleManage"
          preIcon="ant-design:plus-outlined">管理关联检查项</a-button>
        <a-button type="primary" @click="handleSave"
          preIcon="ant-design:plus-outlined">保存排序</a-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                删除
              </a-menu-item>
            </a-menu>
          </template>
          <a-button v-auth="'plan:mes3_action_plan_problem_type:deleteBatch'">批量操作
            <Icon icon="ant-design:down-outlined"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <template #sortSlots="{ record }">
        <a-input-number v-model:value="record.sort" style="width: 100%" />
      </template>
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
  </div>
  <!-- 检查项管理抽屉 -->
  <MdmItemDrawer @register="registerDrawer" @success="handleSuccess" />
</template>

<script lang="ts" name="mdm3-mdmTaskItem" setup>
import { ref } from 'vue';
import { BasicTable, TableAction } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage'
import { list, editBatch, deleteData } from './MdmTaskItem.api';
import { columns } from './MdmTaskItem.data';
import { useDrawer } from '/@/components/Drawer';
import MdmItemDrawer from './components/MdmItemDrawer.vue';
import { CheckItemTypeEnum } from '/@/enums/planEnum';
//注册检查项管理抽屉
const [registerDrawer, { openDrawer }] = useDrawer();
//任务数据
const taskData = ref();
//检查项类型
const checkItemType = ref();
//任务条目关联数据
const taskItemList = ref();

//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    title: '已关联检查项',
    columns: columns,
    pagination: false,
    immediate: false,
    showTableSetting: false,
    actionColumn: {
      width: 80,
      fixed: 'right'
    },
  },
})

const [registerTable, { getDataSource, setTableData, setLoading, setProps }, { rowSelection, selectedRowKeys }] = tableContext
/**
   * 操作栏
   */
function getTableAction(record) {
  return [
    {
      label: '删除',
      popConfirm: {
        title: '是否确认删除',
        confirm: handleDelete.bind(null, record),
        placement: 'topLeft',
      },
      auth: 'mdm3:mes3_mdm_item:delete'
    }
  ]
}
/**
 * 管理关联检查项
 */
function handleManage() {
  openDrawer(true, {
    taskData: taskData.value,
    oldList: taskItemList,
    checkItemType: checkItemType.value 
  });
}

//保存排序
async function handleSave(){
  try{
    setLoading(true);
    await editBatch(getDataSource());
    handleSuccess();
  } catch(e) {}
  finally{
    setLoading(false);
  }
}

/**
 * 成功回调
 */
async function handleSuccess() {
  setLoading(true);
  //根据任务id查询关联检查项
  const { records } = await list({
    pageSize: 999,
    taskId: taskData.value.id,
    column: 'sort',
    order: 'asc',
  });
  taskItemList.value = records;
  await setTableData(records);
  setLoading(false);
}

/**
 * 删除事件
 */
async function handleDelete(record) {
  await deleteData({ ids: record.id, taskId: record.taskId }, handleSuccess);
}
/**
 * 批量删除事件
 */
async function batchHandleDelete() {
  await deleteData({ ids: selectedRowKeys.value, taskId: taskData.value.id }, handleSuccess);
}

async function reloadData(data, checkTaskType){
  taskData.value = data;
  checkItemType.value = checkTaskType;
  if(checkTaskType == CheckItemTypeEnum.TYPE_RECORD){
    let newColumns = [{
      title: '交接班类型',
      dataIndex: 'isHandover',
      width: 120,
      customRender: ({record}) => {
        return record.mdmItem.isHandover == 1 ? '交班' : '接班';
      }
    },{
      title: '是否生产检查项',
      dataIndex: 'isProduction',
      width: 120,
      customRender: ({record}) => {
        return record.mdmItem.isProduction == 1 ? '是' : '否';
      }
    }];

    setProps({
      columns: [
        ...columns,
        ...newColumns
      ]
    });
  }
  handleSuccess();
}

//暴露方法
defineExpose({
  reloadData,
})
</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}
</style>