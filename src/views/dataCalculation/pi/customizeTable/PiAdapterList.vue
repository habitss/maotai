<template>
  <div>
    <!--主表信息显示-->
    <div class="jeecg-basic-table-form-container">
      
    <!-- <a-form :label-col="labelCol" :wrapper-col="wrapperCol" labelAlign="left">
      <a-row :gutter="24">
          <a-col :lg="12">
          <a-form-item label="字段中文名称">
            <a-input v-model:value="tableData.name" disabled/>
          </a-form-item>
        </a-col>
        <a-col :lg="12">
          <a-form-item label="字段名">
            <a-input v-model:value="tableData.fieldName" disabled/>
          </a-form-item>
        </a-col>
      </a-row>
    </a-form> -->

    <!--引用表格-->
   <BasicTable @register="registerTable" :rowSelection="rowSelection">
     <!--插槽:table标题-->
      <template #tableTitle>
          <a-button type="primary" @click="addAlgorithm" preIcon="ant-design:plus-outlined"> 新增{{departObject.algorithmType == 1 ? 'Adapter' : 'RA'}}算法</a-button>
          <a-button type="primary" @click="batchHandleDelete" preIcon="ant-design:delete-outlined" v-if="selectedRows.length > 0"> 删除</a-button>
      </template>
       <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)"/>
      </template>
      <!--字段回显插槽-->
      <template v-slot:bodyCell="{ column, record, index, text }">
      </template>
    </BasicTable>
    </div>
    <!-- 算法配置 -->
    <PiAdapterModal ref="algorithmModal" @success="algorithmSuccess"></PiAdapterModal>
  </div>
</template>

<script lang="ts" name="pi-piCustomizeTableField" setup>
  import { ref, reactive, watch } from 'vue';
  import {BasicTable, useTable, TableAction} from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage'
  import {columns} from './PiAdapter.data';
  import {list,deleteOne,batchDelete} from './PiAdapter.api';
  import PiAdapterModal from './components/PiAdapterModal.vue'

  import { useUserStore } from '/@/store/modules/user';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  const queryParam = ref<any>({});
  const tableData = ref<any>({});
  const toggleSearchStatus = ref<boolean>(false);
  const registerModal = ref();
  const algorithmModal = ref();
  const userStore = useUserStore();
  const props = defineProps({
    departObject: { type: Object,default:()=>({}) },
  });
  const fieldObject = ref<Object>({});
  // const exportParams = reactive({
  //   departId: '1',
  //   deviceId: queryParam?.value?.deviceId,
  // });

  //注册table数据
  const { prefixCls,tableContext,onExportXls,onImportXls } = useListPage({
      tableProps:{
           title: 'PI算法配置',
           api: list,
           columns,
           canResize:false,
           useSearchForm: false,
           pagination: true,
           showIndexColumn: true,
           actionColumn: {
              width: 200,
              fixed: 'right',
           },
        beforeFetch: (params) => {
          return Object.assign(params, queryParam.value);
        },
      },
      // exportConfig: {
      //   name:"pi算法配置",
      //   url: getExportUrl,
      // },
      // importConfig: {
      //   url: getImportUrl,
      //   success: handleSuccess
      // },
  })

  
 const [registerTable, { reload,setSelectedRowKeys}, { rowSelection,selectedRows ,selectedRowKeys }] =
  tableContext;
  const labelCol = reactive({
    xs: { span: 24 },
    sm: { span: 5 },
  });
  const wrapperCol = reactive({
    xs: { span: 24 },
    sm: { span: 16 },
  });

  // 监听 departObject 变化，解决抽屉再次打开时显示上次内容的问题
  watch(
    () => props.departObject,
    (newVal) => {
      if (newVal && Object.keys(newVal).length > 0) {
        queryParam.value.piCode = newVal.fieldName;
        queryParam.value.piTable = newVal.tableName;
        queryParam.value.type = newVal.algorithmType;
        queryParam.value.id = newVal.calculateSignalId;

        tableData.value.id = newVal.id;
        tableData.value.tableId = newVal.tableId;
        tableData.value.tableName = newVal.tableName;
        tableData.value.lineCode = newVal.lineCode;
        tableData.value.section = newVal.section;
        tableData.value.name = newVal.name;
        tableData.value.fieldName = newVal.fieldName;
        tableData.value.type = newVal.algorithmType;

        // 重新加载表格数据
        reload();
      }
    },
    { immediate: true, deep: true }
  );

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
 * 算法配置成功
 */
function algorithmSuccess(id) {
  handleSuccess()
  if(id){
    setTimeout(() => {
      setSelectedRowKeys([id])
    }, 1000);
  }
}

  /**
 * 操作栏
 */
function getTableAction(record) {
  return [
    // {
    //   label: t('language.edit'),
    //   onClick: handleEdit.bind(null, record),
    //   auth: 'mdm:deviceManage:edit',
    // },
    {
      label: "配置算法",
      onClick: algorithm.bind(null, record),
      auth: '',
    },
  ];
}

//配置算法-新增
const addAlgorithm = () => {
  let record = {
    piTable:tableData.value.tableName,
    piCode:tableData.value.fieldName,
    piName:tableData.value.name,
    piTableId:tableData.value.tableId,
    type:tableData.value.type
  }
  algorithmModal.value.add(record);
}

//配置算法-修改
const algorithm = (record) => {
  // record.piTable = tableData.value.tableName
  // record.piTable = tableData.value.tableName
  record.piTableId = tableData.value.tableId
  record.piName = tableData.value.name,
  algorithmModal.value.edit(record);
}

/**
 * 下拉操作栏
 */
function getDropDownAction(record) {
  return [
    // {
    //   label: t('language.detail'),
    //   onClick: handleDetail.bind(null, record),
    // },
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
  queryParam.value = {};
  selectedRowKeys.value = [];
  //刷新数据
  reload();
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