<template>
  <!--引用表格-->
  <BasicTable @register="registerTable" @edit-end="editEnd">
    <template #tableTitle>
      <a-button type="primary" preIcon="ant-design:export-outlined" @click="handleExportXls" v-auth="'line.efficiency:mes3_line_efficiency_state_record_piece_sku:exportXls'"> 导出</a-button>
    </template>
  </BasicTable>
</template>

<script lang="ts" name="line-lineEfficiencySkuDay" setup>
  import { ref, reactive } from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { skuColumns as columns } from './OpiSplitdata';
  import { skuList as list, speedEdit, getSkuExportUrl } from './OpiSplit.api';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  const queryParam = reactive<any>({});
  const batchData = ref({});
  const { createMessage } = useMessage();
  const emit = defineEmits(['ok']);
  //注册table数据
  const { tableContext, onExportXls } = useListPage({
    tableProps: {
      // title: 'sku',
      api: list,
      columns,
      showTableSetting:false,
      canResize:true,
      useSearchForm: false,
      showActionColumn: false,
      maxHeight: 220,
      actionColumn: {
        width: 120,
        fixed: 'right',
      },
      beforeFetch: async (params) => {
        return Object.assign(params, queryParam);
      },
    },
    exportConfig: {
      name: "分SKU批次数据",
      url: getSkuExportUrl,
      params: queryParam,
    },
	  importConfig: {
	    url: "",
	    success: handleSuccess
	  },
  });
  const [registerTable, { reload,getDataSource }, { selectedRowKeys }] = tableContext;

  /**
   * 导出
   */
  function handleExportXls() {
    const data = getDataSource();
    if (!data || data.length === 0) {
      createMessage.warning('列表数据为空，无法导出');
      return;
    }
    onExportXls();
  }

  /**
   * 成功回调
   */
  function handleSuccess() {
    (selectedRowKeys.value = []) && reload();
  }
  /**
   * 查询
   */
  function searchQuery(val) {
    queryParam.lineCode = val.lineCode;
    queryParam.startTime = val.startTime;
    queryParam.endTime = val.endTime;
    queryParam.batchStartTime = val.startTime;
    queryParam.batchEndTime = val.endTime;
    queryParam.batchRunningTime = val.batchRunningTime;
    queryParam.batchId = val.batchId;
    queryParam.queryType = "1"
    reload();
  }

  //编辑完成
  async function editEnd(val){
    let data = val.record;

    // let tableDataArr = getDataSource();
    // if(findMatchingObject(data,tableDataArr)){
    //   createMessage.warning("值为更改");
    //   return;
    // }
    
    data.batchStartTime = queryParam.startTime;
    data.batchEndTime = queryParam.endTime;
    data.batchRunningTime = queryParam.batchRunningTime;
    data.batchId = queryParam.batchId;
    await speedEdit(data)
      .then((res) => {
        if (res.success) {
          createMessage.success(res.message);
          searchQuery(queryParam);
          emit('ok');
        } else {
          createMessage.warning(res.message);
        }
      })
      .finally(() => {
        //confirmLoading.value = false;
      });
    console.log(data.record)
  }

  // 查找id相同且skuSpeed相同的对象
  function findMatchingObject(targetObj, array) {
    return array.some(item => 
      item.id === targetObj.id && 
      item.skuSpeed === targetObj.skuSpeed
    );
  }

  defineExpose({
   searchQuery,
  });
</script>

<style lang="less" scoped>
  .jeecg-basic-table-form-container {
    padding: 0;
    .table-page-search-submitButtons {
      display: block;
      margin-bottom: 24px;
      white-space: nowrap;
    }
    .query-group-cust{
      min-width: 100px !important;
    }
    .query-group-split-cust{
      width: 30px;
      display: inline-block;
      text-align: center
    }
    .ant-form-item:not(.ant-form-item-with-help){
      margin-bottom: 16px;
      height: 32px;
    }
    :deep(.ant-picker),:deep(.ant-input-number){
      width: 100%;
    }
  }
</style>
