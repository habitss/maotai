<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable"></BasicTable>
  </div>
</template>

<script lang="ts" name="mes3-plan-plan" setup>
import { reactive } from 'vue';
import { BasicTable } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage';
import { columns, searchFormSchema } from './VisitsSummary.data';
import { list } from './VisitsSummary.api';
const queryParam = reactive<any>({});
//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    title: 'PI报表访问统计',
    api: list,
    columns,
    canResize: true,
    formConfig: {
      schemas: searchFormSchema,
      autoSubmitOnEnter: true,
      showAdvancedButton: true,
    },
    pagination: false,
    showActionColumn: false,
    beforeFetch: (params) => {
      return Object.assign(params, queryParam);
    },
    defSort: { 
      column: 'ymd', 
      order: 'desc' 
    },
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