<template>
  <div>
    <!--主表信息显示-->
    <div class="jeecg-basic-table-form-container">
      <a-form :label-col="labelCol" :wrapper-col="wrapperCol" labelAlign="right">
        <a-row :gutter="24">
          <a-col :lg="8">
            <a-form-item label="部门">
              <!-- <a-input v-model:value="tableData.section" disabled/> -->
              <kt-depart-select v-model:value="tableData.deptCode" select-type="depart" disabled/>
            </a-form-item>
          </a-col>
          <a-col :lg="8">
            <a-form-item label="产线">
              <!-- <a-input v-model:value="tableData.lineCode" disabled/> -->
              <kt-depart-select v-model:value="tableData.lineCode" select-type="line" disabled/>
            </a-form-item>
          </a-col>
            <a-col :lg="8">
            <a-form-item label="报表名称">
              <a-input v-model:value="tableData.name" disabled/>
            </a-form-item>
          </a-col>
          <a-col :lg="8">
            <a-form-item label="表名称">
              <a-input v-model:value="tableData.tableName" disabled/>
            </a-form-item>
          </a-col>
          <a-col :lg="8">
            <a-form-item label="字段中文名">
              <a-input v-model:value="queryParam.searchName"/>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
      <!--引用表格-->
      <BasicTable @register="registerTable" :rowSelection="rowSelection">
        <!--插槽:table标题-->
        <template #tableTitle>
          <a-button type="primary" @click="reload" preIcon="ant-design:plus-outlined">查询</a-button>
          <a-button type="primary" @click="handleAdd" preIcon="ant-design:plus-outlined"> 新增</a-button>
          <a-button type="primary"
            preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
          <j-upload-button type="primary"
            preIcon="ant-design:import-outlined" @click="onImportClick">导入</j-upload-button>
        </template>
        <!--操作栏-->
        <template #action="{ record }">
          <TableAction :actions="getTableAction(record)" />
        </template>
      </BasicTable>
    </div>
    <!-- 表单区域 -->
    <ReportConfigFieldModal ref="registerModal" @success="handleSuccess"></ReportConfigFieldModal>
  </div>
</template>

<script lang="ts" name="pi-piCustomizeTableField" setup>
  import { ref, reactive, onMounted,provide } from 'vue';
  import {BasicTable, TableAction} from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage'
  import {columns} from './reportConfigField.data';
  import {list, deleteOne, batchDelete, getImportUrl,getExportUrl} from './reportConfigField.api';
  import ReportConfigFieldModal from './components/reportConfigFieldModal.vue'
  import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  const { createMessage } = useMessage();
  const { t } = useI18n();

  const queryParam = ref<any>({});
  const tableData = ref<any>({});
  const registerModal = ref();
  const props = defineProps({
    departObject: { type: Object,default:()=>({}) },
  });
  const departObject = ref<Object>({});
  provide('departObject', departObject);

  function onImportClick(data){
    createMessage.success('正在导入中，如数据量过大，请耐心等待几分钟！');

    onImportXls(data);
  }

  //注册table数据
  const { tableContext, onExportXls, onImportXls } = useListPage({
      tableProps:{
        title: '自定义报表字段配置',
        api: list,
        columns,
        canResize:true,
        useSearchForm: false,
        pagination: true,
        actionColumn: {
          width: 160,
          fixed: 'right',
        },
        beforeFetch: (params) => {
          params.column ="sort";
          params.order ="asc";
          queryParam.value.name = queryParam.value.searchName ? `*${queryParam.value.searchName }*`: null;
          return Object.assign(params, queryParam.value);
        },
      },
      exportConfig: {
        name:"自定义报表字段配置",
        url: getExportUrl,
        params: queryParam.value,
      },
      importConfig: {
        url: getImportUrl,
        success: handleSuccess
      },
  })

  
 const [registerTable, { reload }, { rowSelection, selectedRowKeys }] =
  tableContext;
  const labelCol = reactive({
    xs: { span: 24 },
    sm: { span: 5 },
  });
  const wrapperCol = reactive({
    xs: { span: 24 },
    sm: { span: 16 },
  });

  onMounted(() => {
    const { departObject } = props;
    queryParam.value.tableId = departObject.id;
    tableData.value.id = departObject.id;
    tableData.value.lineCode = departObject.lineCode;
    tableData.value.section = departObject.section;
    tableData.value.name = departObject.name;
    tableData.value.tableName = departObject.tableName;
  });


   /**
    * 新增事件
    */
  function handleAdd() {
    registerModal.value.disableSubmit = false;
    registerModal.value.add(tableData.value.id);
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
      label: t('language.edit'),
      onClick: handleEdit.bind(null, record),
    },
    {
      label: t('language.detail'),
      onClick: handleDetail.bind(null, record),
    },
    {
      label: t('language.delText'),
      popConfirm: {
        title: t('language.confirmDelete'),
        confirm: handleDelete.bind(null, record),
        placement: 'left',
      },
    },
  ];
}
</script>

<style lang="less" scoped>
.jeecg-basic-table-form-container {
  .table-page-search-submitButtons {
    display: block;
    margin-bottom: 24px;
    white-space: nowrap;
  }
  .query-group-cust {
    width: calc(50% - 15px);
    min-width: 100px !important;
  }
  .query-group-split-cust {
    width: 30px;
    display: inline-block;
    text-align: center;
  }
}
//@import '../common.less';
</style>