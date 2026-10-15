<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable">
      <!--插槽:table标题-->
      <template #tableTitle>
        <!-- 高级查询 -->
        <super-query :config="superQueryConfig" @search="handleSuperQuery" />
      </template>
    </BasicTable>
  </div>
</template>

<script lang="ts" name="df.mdm-dfMdmIndStand" setup>
import { reactive } from 'vue';
import { BasicTable } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage'
import { columns, searchFormSchema, superQuerySchema } from './DfMdmIndStand.data';
import { list } from './DfMdmIndStand.api';
const queryParam = reactive<any>({});
//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    title: 'DF主数据-指标标准',
    api: list,
    columns,
    canResize: false,
    formConfig: {
      schemas: searchFormSchema,
      autoSubmitOnEnter: true,
      showAdvancedButton: true,
      fieldMapToNumber: [
      ],
      fieldMapToTime: [
      ],
    },
    beforeFetch: (params) => {
      return Object.assign(params, queryParam);
    },
  },
})

const [registerTable, { reload }] = tableContext

// 高级查询配置
const superQueryConfig = reactive(superQuerySchema);

/**
 * 高级查询事件
 */
function handleSuperQuery(params) {
  Object.keys(params).map((k) => {
    queryParam[k] = params[k];
  });
  reload();
}
</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}
</style>