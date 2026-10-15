<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button type="primary" v-auth="'mdm3:mes3_mdm_task:add'" @click="handleAdd"
          preIcon="ant-design:plus-outlined"> 新增</a-button>
        <!-- <a-button type="primary" v-auth="'mdm3:mes3_mdm_task:exportXls'"
          preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
        <j-upload-button type="primary" v-auth="'mdm3:mes3_mdm_task:importExcel'"
          preIcon="ant-design:import-outlined" @click="onImportXlsByParams">导入</j-upload-button> -->
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                删除
              </a-menu-item>
            </a-menu>
          </template>
          <a-button v-auth="'mdm3:mes3_mdm_task:deletionBatch'">批量操作
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
      <!--字段回显插槽-->
      <template v-slot:bodyCell="">
      </template>
    </BasicTable>
    <!-- 表单区域 -->
    <MdmTaskModal @register="registerModal" @success="handleSuccess"></MdmTaskModal>
    <!-- 关联检查项抽屉 -->
    <MdmAssociationItemDrawer @register="registerDrawer" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" name="mdm3-mdmTask" setup>
import { reactive, ref } from 'vue';
import { BasicTable, TableAction } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useDrawer } from '/@/components/Drawer';
import { useListPage } from '/@/hooks/system/useListPage'
import MdmTaskModal from './components/MdmTaskModal.vue'
import MdmAssociationItemDrawer from './components/MdmAssociationItemDrawer.vue';
import { columns, searchFormSchema, CheckItemTypeEnum } from './MdmTask.data';
import { list, deleteOne, batchDelete, getImportUrl, getExportUrl } from './MdmTask.api';
const queryParam = reactive<any>({});
//注册model
const [registerModal, { openModal }] = useModal();
//注册关联检查项抽屉抽屉
const [registerDrawer, { openDrawer }] = useDrawer();
//父级数据
const parentData = ref<any>({});
const deviceId = ref<any>({});
const deviceName = ref<any>({});

//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    title: '任务管理',
    api: list,
    immediate: false,
    columns,
    canResize: true,
    formConfig: {
      //labelWidth: 120,
      schemas: searchFormSchema,
      autoSubmitOnEnter: true,
      showAdvancedButton: true,
      fieldMapToNumber: [
      ],
      fieldMapToTime: [
      ],
    },
    actionColumn: {
      width: 260,
      fixed: 'right'
    },
    beforeFetch: (params) => {
      return Object.assign(params, queryParam);
    },
  },
  exportConfig: {
    name: "任务管理",
    url: getExportUrl,
    params: queryParam,
  },
  importConfigByParams: {
    url: getImportUrl,
    success: handleSuccess,
    params: {
      deviceId,
      deviceName
    },
  },
})

const [registerTable, { reload, setProps }, { rowSelection, selectedRowKeys }] = tableContext

/**
 * 新增事件
 */
function handleAdd() {
  openModal(true, {
    isUpdate: false,
    showFooter: true,
    parentData: parentData.value,
  });
}
/**
 * 编辑事件
 */
function handleEdit(record: Recordable) {
  openModal(true, {
    record,
    isUpdate: true,
    showFooter: true,
  });
}
/**
 * 复制事件
 */
function handleCopy(record: Recordable) {
  openModal(true, {
    record,
    isUpdate: false,
    showFooter: true,
    isCopy: true,
  });
}
/**
 * 详情
*/
function handleDetail(record: Recordable) {
  openModal(true, {
    record,
    isUpdate: true,
    showFooter: false,
  });
}
/**
 * 删除事件
 */
async function handleDelete(record) {
  await deleteOne({ id: record.id, deviceId: record.deviceId }, handleSuccess);
}
/**
 * 批量删除事件
 */
async function batchHandleDelete() {
  await batchDelete({ ids: selectedRowKeys.value, deviceId: deviceId.value }, handleSuccess);
}
/**
 * 成功回调
 */
function handleSuccess() {
  (selectedRowKeys.value = []) && reload();
}
/**
   * 操作栏
   */
function getTableAction(record) {
  return [
    {
      label: '编辑',
      onClick: handleEdit.bind(null, record),
      auth: 'mdm3:mes3_mdm_task:edit'
    },
    {
      label: '复制',
      onClick: handleCopy.bind(null, record),
      auth: 'mdm3:mes3_mdm_task:edit'
    },
    {
      label: '关联检查项',
      onClick: handleTaskItem.bind(null, record),
      auth: 'mdm3:mes3_mdm_task:edit'
    }
  ]
}
/**
   * 下拉操作栏
   */
function getDropDownAction(record) {
  return [
    {
      label: '详情',
      onClick: handleDetail.bind(null, record),
    }, {
      label: '删除',
      popConfirm: {
        title: '是否确认删除',
        confirm: handleDelete.bind(null, record),
        placement: 'topLeft',
      },
      auth: 'mdm3:mes3_mdm_task:delete'
    }
  ]
}

function handleTaskItem(record){
  openDrawer(true, {
    record,
    checkTaskType: parentData.value.checkTaskType
  });
}

function reloadData(data, checkTaskType) {
  //交接班任务隐藏部分字段
  if(checkTaskType == CheckItemTypeEnum.TYPE_RECORD){
    //需要排除的列
    const exclusionColumns = [
      'recipeTypeCode_dictText',
      'recipeSubTypeCode_dictText',
      'taskKindCode_dictText',
      'loopTriggerName',
    ];
    exclusionColumns.includes
    let newColumns = columns.filter(item => !exclusionColumns.includes(item.dataIndex));
    setProps({
      columns: newColumns
    });
  }
  parentData.value = data;
  parentData.value.checkTaskType = Number(checkTaskType);
  deviceId.value = data.id;
  deviceName.value = data.deviceName;
  queryParam.lineCode = data.lineCode;
  queryParam.deviceId = data.id;
  queryParam.checkTaskType = checkTaskType;
  reload();
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