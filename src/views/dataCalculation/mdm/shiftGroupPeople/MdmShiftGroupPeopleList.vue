<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button type="primary" v-auth="'mdm:mes3_mdm_shift_group_people:add'" @click="handleAdd"
          preIcon="ant-design:plus-outlined"> 新增</a-button>
        <a-button type="primary" v-auth="'mdm:mes3_mdm_shift_group_people:exportXls'"
          preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
        <j-upload-button type="primary" v-auth="'mdm:mes3_mdm_shift_group_people:importExcel'"
          preIcon="ant-design:import-outlined" @click="onImportXls">导入</j-upload-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                删除
              </a-menu-item>
            </a-menu>
          </template>
          <a-button v-auth="'mdm:mes3_mdm_shift_group_people:deletionBatch'">批量操作
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
      <!--字段回显插槽-->
      <template v-slot:bodyCell="">
      </template>
    </BasicTable>
    <!-- 表单区域 -->
    <MdmShiftGroupPeopleModal @register="registerModal" @success="handleSuccess"></MdmShiftGroupPeopleModal>
  </div>
</template>

<script lang="ts" name="mdm-mdmShiftGroupPeople" setup>
import { reactive, watch } from 'vue';
import { BasicTable, TableAction } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useListPage } from '/@/hooks/system/useListPage'
import MdmShiftGroupPeopleModal from './components/MdmShiftGroupPeopleModal.vue'
import { columns, searchFormSchema } from './MdmShiftGroupPeople.data';
import { list, deleteOne, batchDelete, getImportUrl, getExportUrl } from './MdmShiftGroupPeople.api';
//查询参数对象
const queryParam = reactive<any>({});
//父组件传递参数对象
const props = defineProps({
  shiftGroupRecord: { type: Object, default: () => ({}) },
});
//注册model
const [registerModal, { openModal }] = useModal();
//注册table数据
const { tableContext, onExportXls, onImportXls } = useListPage({
  tableProps: {
    title: '班组人员分配',
    api: list,
    columns,
    canResize: false,
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
      width: 120,
      fixed: 'right'
    },
    beforeFetch: (params) => {
      queryParam.shiftGroupId = props.shiftGroupRecord.id;
      queryParam.column = "sort";
      queryParam.order = "asc";
      return Object.assign(params, queryParam);
    },
  },
  exportConfig: {
    name: "班组人员分配",
    url: getExportUrl,
    params: queryParam,
  },
  importConfig: {
    url: getImportUrl,
    success: handleSuccess
  },
})

const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext

/**
 * 新增事件
 */
function handleAdd() {
  openModal(true, {
    record: {
      shiftGroupId: props.shiftGroupRecord.id,
      factoryCode: props.shiftGroupRecord.factoryCode,
      deptCode: props.shiftGroupRecord.deptCode,
      lineCode: props.shiftGroupRecord.lineCode,
    },
    isUpdate: false,
    showFooter: true,
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
  await deleteOne({ id: record.id }, handleSuccess);
}
/**
 * 批量删除事件
 */
async function batchHandleDelete() {
  await batchDelete({ ids: selectedRowKeys.value }, handleSuccess);
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
      label: '删除',
      popConfirm: {
        title: '是否确认删除',
        confirm: handleDelete.bind(null, record),
        placement: 'topLeft',
      },
      auth: 'mdm:mes3_mdm_shift_group_people:delete'
    }
  ]
}

watch(props, (newValue, OldValue): void => {
  handleSuccess();
}, { immediate: true, deep: true });
</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}
</style>