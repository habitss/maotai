<template>
  <a-spin tip="Loading..." :spinning="spinning">
    <div class="p-2">
      <!--查询区域-->
      <div class="jeecg-basic-table-form-container">
        <a-form ref="formRef" @keyup.enter.native="searchQuery" :model="queryParam" :label-col="labelCol" :wrapper-col="wrapperCol">
          <a-row :gutter="24">
            <!--产线-->
            <a-col :lg="6">
              <a-form-item label="产线">
                <!-- <a-input placeholder="请输入产线" v-model:value="queryParam.lineName" allowClear /> -->
                 <kt-dict
                    v-model:value="queryParam.lineName"
                    placeholder="请选择产线"
                    dict="OPI_SPLIT_LINE"
                  />
              </a-form-item>
            </a-col>
            <a-col :xl="6" :lg="7" :md="8" :sm="24">
              <span style="float: left; overflow: hidden; margin-left: -12px" class="table-page-search-submitButtons">
                <a-col :lg="6">
                  <a-button type="primary" preIcon="ant-design:search-outlined" @click="searchQuery">{{ t('language.search') }}</a-button>
                  <a-button type="primary" preIcon="ant-design:reload-outlined" @click="searchReset" style="margin-left: 8px">{{t('language.reset')}}</a-button>
                </a-col>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>
      <!--引用表格-->
      <BasicTable @register="registerTable" :rowSelection="rowSelection" @selectionChange="selectionData">
        <template #tableTitle>
          <a-button type="primary" preIcon="ant-design:export-outlined" @click="handleExportXls" v-auth="'line.efficiency:mes3_line_efficiency_state_record_sku:exportXls'"> 导出</a-button>
        </template>
      </BasicTable>
      <div style="height: 30px;text-align: center;line-height: 30px;background-color: #fff;margin: 5px 0px 5px;">分SKU批次数据</div>
      <SkuList ref="skuListRef" @ok="handleSuccess"></SkuList>
    </div>
  </a-spin>
</template>

<script lang="ts" name="line-lineEfficiencySkuDay" setup>
  import { ref, reactive } from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { columns } from './OpiSplitdata';
  import { list, getExportUrl } from './OpiSplit.api';
  import SkuList from './SkuList.vue'
  import { useUserStore } from '/@/store/modules/user';
  import KtDict from '/@/components/KtDict/KtDict.vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  const formRef = ref();
  const queryParam = reactive<any>({
    lineName:'C1'
  });
  const toggleSearchStatus = ref<boolean>(false);
  const registerModal = ref();
  const skuListRef = ref();
  const spinning = ref(false);
  //注册table数据
  const { tableContext, onExportXls } = useListPage({
    tableProps: {
      // title: '送总部OPI拆分',
      api: list,
      columns,
      showTableSetting:false,
      canResize:true,
      useSearchForm: false,
      showActionColumn: false,
      maxHeight: 220,
      rowSelection:{
        type: 'radio'
      },
      actionColumn: {
        width: 120,
        fixed: 'right',
      },
      beforeFetch: async (params) => {
        return Object.assign(params, queryParam);
      },
    },
    exportConfig: {
      name: "送总部OPI拆分",
      url: getExportUrl,
      params: Object.assign({ type: "0", stateCode: "1" }, queryParam),
    },
	  importConfig: {
	    url: "",
	    success: handleSuccess
	  },
  });
  const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;
  const labelCol = reactive({
    xs:24,
    sm:4,
    xl:6,
    xxl:4
  });
  const wrapperCol = reactive({
    xs: 24,
    sm: 20,
  });

  /**
   * 导出（清空选中数据）
   */
  function handleExportXls() {
    selectedRowKeys.value = [];
    onExportXls();
  }

  /**
   * 成功回调
   */
  function handleSuccess() {
    reload();
  }
   
  /**
   * 查询
   */
  function searchQuery() {
    reload();
  }
  
  /**
   * 重置
   */
  function searchReset() {
    formRef.value.resetFields();
    queryParam.lineName = "";
    queryParam.date = "";
    selectedRowKeys.value = [];

    //刷新数据
    reload();
  }
  //选中批次
  function selectionData(val){
    console.log(val.rows[0]);
    let data = val.rows[0]
    data.batchStartTime = data.startTime;
    data.batchEndTime = data.endTime;
    data.batchRunningTime = data.runningTime;
    data.batchId = data.id;
    skuListRef.value.searchQuery(data);
  }



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
