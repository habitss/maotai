<template>
  <div>
    <!--主表信息显示-->
    <div class="jeecg-basic-table-form-container">
      
    <a-form :label-col="labelCol" :wrapper-col="wrapperCol" labelAlign="left">
        <a-row :gutter="24">
          <!-- <a-col :lg="12">
            <a-form-item label="工段">
               <kt-dict v-model:value="tableData.section" dict="ORG_TYPE" disabled/>
            </a-form-item>
          </a-col>
          <a-col :lg="12">
            <a-form-item label="产线">
              <kt-depart-select v-model:value="tableData.lineCode" select-type="line" disabled/>
            </a-form-item>
          </a-col> -->
           <a-col :lg="12">
            <a-form-item label="报表名称">
              <a-input v-model:value="tableData.reportName" disabled/>
            </a-form-item>
          </a-col>
          <a-col :lg="12">
            <a-form-item label="绑定表">
              <a-input v-model:value="tableData.tableId_dictText" disabled/>
            </a-form-item>
          </a-col>

          <a-col :lg="12">
            <a-form-item label="字段中文名称">
              <a-input v-model:value="queryParam.name" placeholder="请输入字段中文名称" allowClear/>
            </a-form-item>
          </a-col>

          <a-col :lg="12">
            <a-form-item label="是否参与绿区率统计">
              <a-select v-model:value="queryParam.isStatistics" allowClear placeholder="请选择是否参与绿区率统计">
                <a-select-option :value="item.value" v-for="(item) in statisticsArr" :key="item.value">
                  {{ item.label }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>

          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <span style="float: left; overflow: hidden; margin-left: -12px" class="table-page-search-submitButtons">
              <a-col :lg="6">
                <a-button type="primary" preIcon="ant-design:search-outlined" @click="searchQuery" allowClear>{{ t('language.search') }}</a-button>
              </a-col>
            </span>
          </a-col>

        </a-row>
      </a-form>


    <!--引用表格-->
   <BasicTable @register="registerTable" :rowSelection="rowSelection">
     <!--插槽:table标题-->
      <template #tableTitle>
          <a-button type="primary" @click="allocationField" preIcon="ant-design:plus-outlined"> 分配字段</a-button>

          <a-button type="primary" @click="closeEdit" v-auth="'pi:mes3_pi_allocation:close'" title="只更新已配置算法的字段">关闭自动PI编辑</a-button>
          <a-button type="primary" @click="openEdit" v-auth="'pi:mes3_pi_allocation:open'" title="只更新已配置算法的字段">开启自动PI编辑</a-button>
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
    <!-- 表单区域 -->
    <AllocationTableFieldModal ref="registerModal" @success="handleSuccess"></AllocationTableFieldModal>
    <!--字段分配-->
    <a-drawer
      v-if="hasReopen == true"
      v-model:visible="hasReopen"
      class="custom-class"
      root-class-name="root-class-name"
      :root-style="{ color: 'blue' }"
      title="字段分配"
      width="70%"
      placement="right">
      <AllocationFieldForm @ok="updateList" @close="handleCancel" :departObject="departObject"></AllocationFieldForm>
    </a-drawer>
  </div>
</template>

<script lang="ts" name="pi-piCustomizeTableField" setup>
  import { ref, reactive, onMounted,provide, unref } from 'vue';
  import {BasicTable, useTable, TableAction} from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage'
  import {columns, searchFormSchema} from './AllocationTableField.data';
  import {list, deleteOne, batchDelete, getImportUrl,getExportUrl,openOrCloseEdit} from './AllocationTableField.api';
  import { downloadFile } from '/@/utils/common/renderUtils';
  import AllocationTableFieldModal from './components/AllocationTableFieldModal.vue'
  import AllocationFieldForm from './components/AllocationFieldForm.vue'

  // import JSelectDept from '/@/components/Form/src/jeecg/components/JSelectDept.vue';
  // import KtDict from '/@/components/KtDict/KtDict.vue';
  // import KtDevice from '/@/components/KtDeviceType/KtDeviceType.vue';
  // import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
  // import { getDepartId } from '/@/utils/ktTool';
  import { useUserStore } from '/@/store/modules/user';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();
  const { createConfirm } = useMessage();
  const queryParam = ref<any>({});
  const tableData = ref<any>({});
  const hasReopen = ref<boolean>(false);
  const toggleSearchStatus = ref<boolean>(false);
  const registerModal = ref();
  const algorithmModal = ref();
  const userStore = useUserStore();
  const props = defineProps({
    departObject: { type: Object,default:()=>({}) },
  });
  const fieldObject = ref<Object>({});
  const departObject = ref<Object>({});
  provide('departObject', departObject);
  //是否参与绿区率统计
  const statisticsArr = reactive<Record<string, any>>([
    { value: '1', label: '是' },
    { value: '0', label: '否' }
  ]);

  //注册table数据
  const { prefixCls,tableContext,onExportXls,onImportXls } = useListPage({
      tableProps:{
           title: 'pi自定义报表字段',
           api: list,
           columns,
           canResize:true,
           useSearchForm: false,
           pagination: true,
           actionColumn: {
              width: 200,
              fixed: 'right',
           },
        beforeFetch: (params) => {
          params.column ="sort";
          params.order ="asc";
          return Object.assign(params, queryParam.value);
        },
      },
      exportConfig: {
        name:"pi自定义报表字段",
        url: getExportUrl,
      },
      importConfig: {
        url: getImportUrl,
        success: handleSuccess
      },
  })

  
 const [registerTable, { reload, collapseAll, updateTableDataRecord, findTableDataRecord, getDataSource }, { rowSelection, selectedRowKeys }] =
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
    // exportParams.departId = getDepartId(userStore?.getUserInfo?.orgCode);
    const { departObject } = props;
    queryParam.value.allocationTableId = departObject.id;
    tableData.value.id = departObject.id;
    tableData.value.lineCode = departObject.lineCode;
    tableData.value.section = departObject.section;
    tableData.value.reportName = departObject.reportName;
    tableData.value.tableId_dictText = departObject.tableId_dictText;
    tableData.value.tableId = departObject.tableId;
    tableData.value.multipleTables = departObject.multipleTables;

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
/**配置完成 */
function updateList() {
  hasReopen.value = false;
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
      auth: '',
    }
  ];
}

//分配字段
const allocationField = () => {
  hasReopen.value = true;
  let record = {
    tableName: tableData.value.tableName,
    tableId: tableData.value.tableId,  //自定义表id
    allocationTableId: tableData.value.id, //分配表主表id
    multipleTables: tableData.value.multipleTables, //是否多表
    tableNameStrs: tableData.value.tableId_dictText,
  }
  departObject.value = record;
}

/**
 * 下拉操作栏
 */
function getDropDownAction(record) {
  return [
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
      auth: '',
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

 /**
 * 取消按钮回调事件
 */
  function handleCancel() {
      //visible.value = false;
      hasReopen.value = false;
  }

  /**
   * 开启PI编辑 
   */
  function openEdit(){
    createConfirm({
      iconType: 'warning',
      title: '确认开启',
      content: '是否开启自动PI编辑',
      okText: '确认',
      cancelText: '取消',
      onOk: async () => {
        await openOrCloseEdit({ allocationTableId: tableData.value.id,isEdit:1 }, handleSuccess);
      }
    });
  }

  /**
   * 关闭PI编辑
   */
  function closeEdit(){
    createConfirm({
      iconType: 'warning',
      title: '确认关闭',
      content: '是否关闭自动PI编辑',
      okText: '确认',
      cancelText: '取消',
      onOk: async () => {
        await openOrCloseEdit({ allocationTableId: tableData.value.id,isEdit:0}, handleSuccess);
      }
    });
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