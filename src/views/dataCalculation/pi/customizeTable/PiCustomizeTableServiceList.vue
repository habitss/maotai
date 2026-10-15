<template>
  <div>
    <!--主表信息显示-->
    <div class="jeecg-basic-table-form-container">
      
    <a-form :label-col="labelCol" :wrapper-col="wrapperCol" labelAlign="left">
        <a-row :gutter="24">
<!--          <a-col :lg="12">-->
<!--            &lt;!&ndash; <a-form-item label="部门">-->
<!--               <kt-dict v-model:value="tableData.section" dict="ORG_TYPE" disabled/>-->
<!--            </a-form-item> &ndash;&gt;-->
<!--            &lt;!&ndash;部门&ndash;&gt;-->
<!--            <a-form-item :label="t('language.department')">-->
<!--              <kt-depart-select-->
<!--                v-model:value="tableData.deptCode"-->
<!--                select-type="depart"-->
<!--              />-->
<!--            </a-form-item>-->
<!--          </a-col>-->
<!--          <a-col :lg="12">-->
<!--            <a-form-item label="产线">-->
<!--              &lt;!&ndash; <a-input v-model:value="tableData.lineCode" disabled/> &ndash;&gt;-->
<!--              <kt-depart-select v-model:value="tableData.lineCode" select-type="line" />-->
<!--            </a-form-item>-->
<!--          </a-col>-->
<!--           <a-col :lg="12">-->
<!--            <a-form-item label="报表名称">-->
<!--              <a-input v-model:value="tableData.name" />-->
<!--            </a-form-item>-->
<!--          </a-col>-->
<!--          <a-col :lg="12">-->
<!--            <a-form-item label="表名称">-->
<!--              <a-input v-model:value="tableData.tableName" />-->
<!--            </a-form-item>-->
<!--          </a-col>-->
          <a-col :lg="12">
            <a-form-item label="字段中文名称">
              <a-input v-model:value="queryParam.name" placeholder="请输入字段中文名称" allowClear/>
            </a-form-item>
          </a-col>

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


    <!--引用表格-->
   <BasicTable @register="registerTable"    @row-click="handleRowClick" :rowSelection="rowSelection">
     <!--插槽:table标题-->
      <template #tableTitle>
          <a-button type="primary" @click="handleAdd" preIcon="ant-design:plus-outlined"> 新增</a-button>
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
    <PiCustomizeTableFieldModal ref="registerModal" @success="handleSuccess"></PiCustomizeTableFieldModal>
    <!-- 算法配置 -->
    <!-- <PiAlgorithmModal ref="algorithmModal" @success="handleSuccess"></PiAlgorithmModal> -->
    <!--算法配置-列表页面-->
    <a-drawer
      v-if="hasReopen == true"
      v-model:visible="hasReopen"
      class="custom-class"
      root-class-name="root-class-name"
      :root-style="{ color: 'blue' }"
      title="算法配置"
      width="70%"
      placement="right"
    >
    <PiAlgorithmList :departObject="departObject"></PiAlgorithmList>
    </a-drawer>

    <!--算法复制-->
    <a-drawer
      v-if="hasCopyReopen == true"
      v-model:visible="hasCopyReopen"
      class="custom-class"
      root-class-name="root-class-name"
      :root-style="{ color: 'blue' }"
      title="算法复制"
      width="50%"
      placement="right"
    >
    <PiAlgorithmCopy @ok="handleCopySuccess" @close="handleCancel"  :departObject="departObject"></PiAlgorithmCopy>
    </a-drawer>
  </div>
</template>

<script lang="ts" name="pi-piCustomizeTableField" setup>
import {ref, reactive, onMounted, provide, unref, defineEmits} from 'vue';
  import {BasicTable, useTable, TableAction} from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage'
  import {columns, searchFormSchema} from './PiCustomizeTableField.data';
  import {list, deleteOne, batchDelete, getImportUrl,getExportUrl} from './PiCustomizeTableField.api';
  import { downloadFile } from '/@/utils/common/renderUtils';
  import PiCustomizeTableFieldModal from './components/PiCustomizeTableFieldModal.vue'
  import PiAlgorithmList from './PiAlgorithmList.vue'
  import PiAlgorithmCopy from './components/PiAlgorithmCopy.vue'

  import JSelectDept from '/@/components/Form/src/jeecg/components/JSelectDept.vue';
  import KtDict from '/@/components/KtDict/KtDict.vue';
  import KtDevice from '/@/components/KtDeviceType/KtDeviceType.vue';
  import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
  import { getDepartId } from '/@/utils/ktTool';
  import { useUserStore } from '/@/store/modules/user';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  const queryParam = ref<any>({});
  const tableData = ref<any>({});
  const hasReopen = ref<boolean>(false);
  const hasCopyReopen = ref<boolean>(false);

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

  //注册table数据
  const { prefixCls,tableContext,onExportXls,onImportXls } = useListPage({
      tableProps:{
           title: 'pi自定义报表字段',
           api: list,
           columns,
           canResize:true,
           useSearchForm: false,
          //  pagination: true,
           showIndexColumn: true,
        showActionColumn: false,
          //  minHeight: 700,
           pagination:{
            pageSize: 20,
            pageSizeOptions:['20', '50', '100', '200', '300', '500']
           },
           actionColumn: {
              width: 250,
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

  // onMounted(() => {
  //   // exportParams.departId = getDepartId(userStore?.getUserInfo?.orgCode);
  //   const { departObject } = props;
  //   queryParam.value.tableId = departObject.id;
  //   tableData.value.id = departObject.id;
  //   tableData.value.lineCode = departObject.lineCode;
  //   tableData.value.deptCode = departObject.deptCode;
  //   tableData.value.section = departObject.section;
  //   tableData.value.name = departObject.name;
  //   tableData.value.tableName = departObject.tableName;
  // });


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

const emit = defineEmits(['custom-event']);

const handleRowClick = (record, rowIndex) => {
  console.log('===',record)
  emit('custom-event', record); // 触发事件并传递数据
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

  let re = [
    {
      label: t('language.edit'),
      onClick: handleEdit.bind(null, record),
      auth: '',
    }
  ];

  if(record.isUnique != '1'){
    re.push({
      label: "配置算法",
      onClick: algorithm.bind(null, record),
      auth: '',
    })

    //算法复制
    re.push({
      label: "算法复制",
      onClick: copyAlgorithm.bind(null, record),
      auth: '',
    })
  }
  

  return re
}

//配置算法
const algorithm = (record) => {
  hasReopen.value = true;
  record.tableName = tableData.value.tableName;
  departObject.value = record;
  // record.piTable = tableData.value.tableName
  // algorithmModal.value.edit(record);
}
//算法复制
const copyAlgorithm = (record) => {
  hasCopyReopen.value = true;
  record.tableName = tableData.value.tableName;
  departObject.value = record;
}
function handleCopySuccess(){
  hasCopyReopen.value = false;
}
function handleCancel(){
  hasCopyReopen.value = false;
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
