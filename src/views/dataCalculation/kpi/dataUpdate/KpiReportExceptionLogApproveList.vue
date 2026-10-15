<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable">
      <!-- 操作按钮 -->
      <template #tableTitle>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="onSave">保存</a-button>
      </template>

      <template #approveTypeSlot="{ record }">
        <a-select v-if="!record.approveTime" v-model:value="record.approveType" :options="approveOptions"
          :bordered="false"></a-select>
        <span v-else>{{ record.approveType == 1 ? '审批通过' : '驳回' }}</span>
      </template>
    </BasicTable>
    <!-- 表单区域 -->
    <KpiReportExceptionLogModal @register="registerModal" @success="handleSuccess"></KpiReportExceptionLogModal>
  </div>
</template>

<script lang="ts" name="kpi-kpiReportExceptionLog" setup>
import { reactive } from 'vue';
import { BasicTable, TableAction } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useListPage } from '/@/hooks/system/useListPage'
import KpiReportExceptionLogModal from './components/KpiReportExceptionLogModal.vue'
import { approveOptions, columns, searchFormSchema } from './KpiReportExceptionLog.data';
import { list, deleteOne, batchAddOrEdit } from './KpiReportExceptionLog.api';
import { cloneDeep } from 'lodash-es';
const queryParam = reactive<any>({});
//注册model
const [registerModal, { openModal }] = useModal();
//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    title: 'KPI数据修改审批',
    api: list,
    columns,
    canResize: false,
    showTableSetting: false,
    showActionColumn: false,
    formConfig: {
      //labelWidth: 120,
      schemas: searchFormSchema,
      autoSubmitOnEnter: true,
      showAdvancedButton: false,
      fieldMapToNumber: [
      ],
      fieldMapToTime: [
        ['createTime', ['createTime_begin', 'createTime_end'], 'YYYY-MM-DD']
      ],
    },
    beforeFetch: (params) => {
      return Object.assign(params, queryParam);
    },
    afterFetch: (records) => {
      let newRecords = [] as any;
      for (const item of records) {
        item.dataType = '原值';
        item.dataResult = item.oldValue;
        item.dataColor = item.oldColor;

        let newItem = cloneDeep(item);
        newItem.dataType = '新值';
        newItem.dataResult = item.newValue;
        newItem.dataColor = item.newColor;
        newRecords.push(item, newItem);
      }
      return newRecords;
    }
  },
})

const [registerTable, { reload, getDataSource }, { selectedRowKeys }] = tableContext

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
      auth: 'kpi:mes3_kpi_report_exception_log:edit'
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
      auth: 'kpi:mes3_kpi_report_exception_log:delete'
    }
  ]
}

//保存审批
async function onSave(){
  try {
    const records = cloneDeep(getDataSource()).filter(item => 
      !item.approveTime && item.approveType != 3
    );
    //提交表单
    await batchAddOrEdit(records);
    handleSuccess();
  } catch (error) {
  }
}

</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}
</style>