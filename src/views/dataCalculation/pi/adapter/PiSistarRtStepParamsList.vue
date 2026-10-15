<template>
  <div class="p-2">
     <!--查询区域-->
    <div class="jeecg-basic-table-form-container">
      <a-form ref="formRef" @keyup.enter.native="searchQuery" :model="queryParam" :label-col="labelCol" :wrapper-col="wrapperCol">
        <a-row :gutter="24">
          <!--时间-->
          <!-- <a-col :lg="6">
            <a-form-item label="写入时间">
              <a-range-picker class="date-pick" :ranges="{}" 
                format="YYYY-MM-DD HH:mm:ss" valueFormat="YYYY-MM-DD HH:mm:ss" 
                v-model:value="queryParam.timeRange" 
                @change="onChangeDate" 
                :show-time="{
                  hideDisabledOptions: true,
                  defaultValue: [dayjs('00:00:00', 'HH:mm:ss'), dayjs('23:59:59', 'HH:mm:ss')],
                }"
                style="width: 215px;"/>
            </a-form-item>
          </a-col> -->
          <!--订单号-->
          <a-col :lg="6">
            <a-form-item label="订单号">
              <a-input placeholder="请输入订单号" v-model:value="queryParam.orderNo" allowClear />
            </a-form-item>
          </a-col>

          <!--批次号-->
          <a-col :lg="6">
            <a-form-item label="批次号">
              <a-input placeholder="请输入批次号" v-model:value="queryParam.batchNo" allowClear />
            </a-form-item>
          </a-col>
          <!--PCU-->
          <!-- <a-col :lg="6">
            <a-form-item label="PCU">
              <a-input placeholder="请输入PCU" v-model:value="queryParam.pcuNo" allowClear />
            </a-form-item>
          </a-col> -->
          <!--EOP-->
          <!-- <a-col :lg="6">
            <a-form-item label="EOP">
              <a-input placeholder="请输入EOP" v-model:value="queryParam.eopNo" allowClear />
            </a-form-item>
          </a-col> -->

          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <span style="float: left; overflow: hidden; margin-left: -12px" class="table-page-search-submitButtons">
              <a-col :lg="6">
                <a-button type="primary" preIcon="ant-design:search-outlined" @click="searchQuery">{{ t('language.search') }}</a-button>
                <a-button type="primary" preIcon="ant-design:reload-outlined" @click="searchReset" style="margin-left: 8px">{{
                  t('language.reset')
                }}</a-button>
              </a-col>
            </span>
          </a-col>
        </a-row>
      </a-form>
    </div>
    <!--引用表格-->
    <BasicTable @register="registerTable">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button type="primary" v-auth="'pi:mes3_pi_sistar_rt_step_params:add'"  @click="handleAdd" preIcon="ant-design:plus-outlined"> 新增</a-button>
        <a-button  type="primary" v-auth="'pi:mes3_pi_sistar_rt_step_params:exportXls'" preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
        <j-upload-button  type="primary" v-auth="'pi:mes3_pi_sistar_rt_step_params:importExcel'"  preIcon="ant-design:import-outlined" @click="onImportXls">导入</j-upload-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                删除
              </a-menu-item>
            </a-menu>
          </template>
          <a-button v-auth="'pi:mes3_pi_sistar_rt_step_params:deleteBatch'">批量操作
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)"/>
      </template>
      <template v-slot:bodyCell="{ column, record, index, text }">
      </template>
    </BasicTable>
    <!-- 表单区域 -->
    <PiSistarRtStepParamsModal ref="registerModal" @success="handleSuccess"></PiSistarRtStepParamsModal>
  </div>
</template>

<script lang="ts" name="pi-piSistarRtStepParams" setup>
  import { ref, reactive } from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { columns } from './PiSistarRtStepParams.data';
  import { list, deleteOne, batchDelete, getImportUrl, getExportUrl } from './PiSistarRtStepParams.api';
  import { downloadFile } from '/@/utils/common/renderUtils';
  import PiSistarRtStepParamsModal from './components/PiSistarRtStepParamsModal.vue'
  import { useUserStore } from '/@/store/modules/user';
  import dayjs from 'dayjs';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  const formRef = ref();
  const queryParam = reactive<any>({});
  const toggleSearchStatus = ref<boolean>(false);
  const registerModal = ref();
  const userStore = useUserStore();
  //注册table数据
  const { prefixCls, tableContext, onExportXls, onImportXls } = useListPage({
    tableProps: {
      title: 'Adapter数据-步骤明细',
      api: list,
      columns,
      canResize:false,
      useSearchForm: false,
      actionColumn: {
        width: 120,
        fixed: 'right',
      },
      beforeFetch: async (params) => {
        return Object.assign(params, queryParam);
      },
    },
    exportConfig: {
      name: "Adapter数据-步骤明细",
      url: getExportUrl,
      params: queryParam,
    },
	  importConfig: {
	    url: getImportUrl,
	    success: handleSuccess
	  },
  });
  const [registerTable, { reload, collapseAll, updateTableDataRecord, findTableDataRecord, getDataSource }, { rowSelection, selectedRowKeys }] = tableContext;
  const labelCol = reactive({
    xs:24,
    sm:4,
    xl:6,
    xxl:5
  });
  const wrapperCol = reactive({
    xs: 24,
    sm: 20,
  });

  /**
   * 新增事件
   */
  function handleAdd() {
    registerModal.value.disableSubmit = false;
    registerModal.value.add();
  }
  
  /**
   * 编辑事件
   */
  function handleEdit(record: Recordable) {
    registerModal.value.disableSubmit = false;
    registerModal.value.edit(record);
  }
   
  /**
   * 详情
   */
  function handleDetail(record: Recordable) {
    registerModal.value.disableSubmit = true;
    registerModal.value.edit(record);
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
        label: '详情',
        onClick: handleDetail.bind(null, record),
      },
    ];
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
    selectedRowKeys.value = [];
    //刷新数据
    reload();
  }
  
  function onChangeDate(value,dates){
    queryParam.insertTimestampBegin = dates[0]
    queryParam.insertTimestampEnd = dates[1]
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
