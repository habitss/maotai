<template>
  <BasicTable @register="registerTable" :columns="columns" :expand-column-width="16">
    <template #expandedRowRender="{ record }">
      <div style="margin-bottom: 5px">
        <a-badge status="success" style="vertical-align: middle" />
        <span style="vertical-align: middle">车检流水号: {{ record.checkNo || '-' }}</span>
      </div>
      <div style="margin-bottom: 5px">
        <a-badge status="processing" style="vertical-align: middle" />
        <span style="vertical-align: middle">处理信息: {{ record.processMsg || '-' }}</span>
      </div>
      <div style="margin-bottom: 5px">
        <a-badge status="warning" style="vertical-align: middle" />
        <span style="vertical-align: middle">请求报文: {{ record.reqBody || '-' }}</span>
      </div>
      <div>
        <a-badge status="processing" style="vertical-align: middle" />
        <span style="vertical-align: middle">响应摘要: {{ record.respSummary || '-' }}</span>
      </div>
    </template>
  </BasicTable>
</template>
<script lang="ts" name="maotai-api-log" setup>
  import { BasicTable, useTable } from '/@/components/Table';
  import { getApiOpLogList } from './ApiLog.api';
  import { columns, searchFormSchema } from './ApiLog.data';
  import { useListPage } from '/@/hooks/system/useListPage';

  // 接口日志(审计总表):所有外部接口(E+H/中集自控/勾储MES)调用的入出站记录
  const { tableContext } = useListPage({
    designScope: 'api-op-log',
    tableProps: {
      api: getApiOpLogList,
      expandRowByClick: true,
      showActionColumn: false,
      formConfig: {
        schemas: searchFormSchema,
        fieldMapToTime: [['fieldTime', ['operateTime_begin', 'operateTime_end'], 'YYYY-MM-DD']],
      },
    },
  });

  const [registerTable] = tableContext;
</script>
