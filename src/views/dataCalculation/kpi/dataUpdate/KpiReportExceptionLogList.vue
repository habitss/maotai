<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable">
      <template #approveTypeSlot="{ record }">
        <span v-if="record.approveType == 1">审批通过</span>
        <span v-else-if="record.approveType == 2">驳回</span>
        <span v-else>待审核</span>
      </template>
    </BasicTable>
    <!-- 表单区域 -->
    <KpiReportExceptionLogModal @register="registerModal" @success="handleSuccess"></KpiReportExceptionLogModal>
  </div>
</template>

<script lang="ts" name="kpi-kpiReportExceptionLog" setup>
import { reactive } from 'vue';
import { BasicTable } from '/@/components/Table';
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
    title: 'KPI数据修改记录',
    api: list,
    columns,
    canResize: false,
    showTableSetting: false,
    showActionColumn: false,
    formConfig: {
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

const [registerTable] = tableContext

</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}
</style>