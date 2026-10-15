<template>
  <div>
    <!--主表信息显示-->
    <div class="jeecg-basic-table-form-container">
      
    <a-form :label-col="labelCol" :wrapper-col="wrapperCol" labelAlign="left">
        <a-row :gutter="24">
          <a-col :lg="12">
            <!-- <a-form-item label="部门">
               <kt-dict v-model:value="tableData.section" dict="ORG_TYPE" disabled/>
            </a-form-item> -->
            <!--部门-->
            <a-form-item :label="t('language.department')">
              <!-- <kt-depart-select
                v-model:value="tableData.deptCode"
                select-type="factory"
                disabled
              /> -->
              <KtDepartTreeSelect 
                v-model:value="tableData.deptCode"
                disabled
                :allowClear="true" 
                :defaultExpandLevel="1" 
                :byUserOrg="true"
                :placeholder="'请选择部门/产线'"
                :fieldNames=" { label:'title', value: 'orgCode' }">
              </KtDepartTreeSelect>
            </a-form-item>
          </a-col>
          <a-col :lg="12">
            <a-form-item label="产线">
              <!-- <a-input v-model:value="tableData.lineCode" disabled/> -->
              <KtDepartTreeSelect 
                v-model:value="tableData.lineCode"
                disabled
                :allowClear="true" 
                :defaultExpandLevel="1" 
                :byUserOrg="true"
                :placeholder="'请选择部门/产线'"
                :fieldNames=" { label:'title', value: 'orgCode' }">
              </KtDepartTreeSelect>
            </a-form-item>
          </a-col>
           <a-col :lg="12">
            <a-form-item label="报表名称">
              <a-input v-model:value="tableData.name" disabled/>
            </a-form-item>
          </a-col>
          <a-col :lg="12">
            <a-form-item label="表名称">
              <a-input v-model:value="tableData.tableName" disabled/>
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
                <a-button type="primary" preIcon="ant-design:search-outlined" @click="searchQuery">{{ t('language.search') }}</a-button>
              </a-col>
            </span>
          </a-col>
        </a-row>


      </a-form>


    <!--引用表格-->
   <BasicTable @register="registerTable" :rowSelection="rowSelection">
     <!--插槽:table标题-->
      <template #tableTitle>
          <a-button type="primary" @click="handleAdd" preIcon="ant-design:plus-outlined"> 新增</a-button>

          <a-button type="primary" @click="closeEdit" v-auth="'pi:mes3_pi_customize:close'" title="只更新已配置算法的字段">关闭自动PI编辑</a-button>
          <a-button type="primary" @click="openEdit" v-auth="'pi:mes3_pi_customize:open'" title="只更新已配置算法的字段">开启自动PI编辑</a-button>
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

    <!-- 算法配置汇总页面 -->
    <PiAlgorithmDrawer @register="register" @success="handleSuccess" />

    <!--算法配置-列表页面-->
    <a-drawer
      v-if="hasReopen == true"
      v-model:visible="hasReopen"
      class="custom-class"
      root-class-name="root-class-name"
      :root-style="{ color: 'blue' }"
      title="算法配置"
      width="85%"
      placement="right"
    >
    <PiAlgorithmList :departObject="departObject"></PiAlgorithmList>
    </a-drawer>


     <!--算法配置-列表页面-->
    <a-drawer
      v-if="adapterHasReopen == true"
      v-model:visible="adapterHasReopen"
      class="custom-class"
      root-class-name="root-class-name"
      :root-style="{ color: 'blue' }"
      :title="algorithmTitle"
      width="85%"
      placement="right"
    >
    <PiAdapterList :departObject="departObject"></PiAdapterList>
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

    <!--重复异常配置-->
    <a-drawer
      v-model:open="openRepeatException"
      class="custom-class"
      root-class-name="root-class-name"
      :root-style="{ color: 'blue' }"
      :title="`${recordData.name}-重复异常配置`"
      width="50%"
      placement="right"
      :destroyOnClose = true
    >
      <PiRepeatExceptionList @ok="handleCopySuccess" @close="handleCancel" :recordData="recordData" :openType="'query'"></PiRepeatExceptionList>
    </a-drawer>
  </div>
</template>

<script lang="ts" name="pi-piCustomizeTableField" setup>
  import { ref, reactive, onMounted,provide, unref } from 'vue';
  import {BasicTable, useTable, TableAction} from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage'
  import {columns} from './PiCustomizeTableField.data';
  import {list, deleteOne, batchDelete, getImportUrl,getExportUrl,openOrCloseEdit} from './PiCustomizeTableField.api';
  import PiCustomizeTableFieldModal from './components/PiCustomizeTableFieldModal.vue'
  import PiAlgorithmList from './PiAlgorithmList.vue'
  import PiAdapterList from './PiAdapterList.vue'
  import PiAlgorithmCopy from './components/PiAlgorithmCopy.vue'
  import PiRepeatExceptionList from '/@/views/dataCalculation/pi/repeatException/PiRepeatExceptionTypeList.vue';
  import PiAlgorithmDrawer from './components/PiAlgorithmDrawer.vue'
  import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
  import { useUserStore } from '/@/store/modules/user';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useDrawer } from '/@/components/Drawer';
import { cloneDeep } from 'lodash-es';
  import KtDepartTreeSelect from '/@/components/KtDepartSelect/KtDepartTree.vue';
  const { t } = useI18n();
  const { createConfirm } = useMessage();
  //注册算法配置汇总抽屉
  const [register, { openDrawer }] = useDrawer();

  const queryParam = ref<any>({});
  const tableData = ref<any>({});
  const hasReopen = ref<boolean>(false);
  const adapterHasReopen = ref<boolean>(false);
  const hasCopyReopen = ref<boolean>(false);
  const recordData = ref<any>({});

  const toggleSearchStatus = ref<boolean>(false);
  const registerModal = ref();
  const algorithmModal = ref();
  //显示重复异常配置
  const openRepeatException = ref(false);
  const userStore = useUserStore();
  const props = defineProps({
    departObject: { type: Object,default:()=>({}) },
  });
  const fieldObject = ref<Object>({});
  const departObject = ref<Object>({});
  const algorithmTitle = ref('');
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
          //  pagination: true,
           showIndexColumn: true,
          //  minHeight: 700,
           pagination:{
            pageSize: 20,
            pageSizeOptions:['20', '50', '100', '200', '300', '500']
           },
           actionColumn: {
              width: 355,
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
    const { departObject } = props;
    queryParam.value.tableId = departObject.id;
    tableData.value.id = departObject.id;
    tableData.value.lineCode = departObject.lineCode;
    tableData.value.deptCode = departObject.deptCode;
    tableData.value.factoryCode = departObject.factoryCode;
    tableData.value.section = departObject.section;
    tableData.value.name = departObject.name;
    tableData.value.tableName = departObject.tableName;
    provide('departObject', tableData.value);
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

  let re = [
    {
      label: t('language.edit'),
      onClick: handleEdit.bind(null, record),
      auth: '',
    },
  ];

  if(record.isUnique != '1'){
    re.push({
      label: '算法配置',
      onClick: handleOpenAlgorithm.bind(null, record),
      auth: '',
    })
    //算法复制
    re.push({
      label: "算法复制",
      onClick: copyAlgorithm.bind(null, record),
      auth: '',
    })
  }

  re.push({
    label: '重复异常配置',
    onClick: handleOpenRepeatException.bind(null, record),
    auth: '',
  })
  return re
}

/**
 * 打开算法配置抽屉
 */
function handleOpenAlgorithm(record){
  let a = cloneDeep(tableData.value);
  openDrawer(true, {
    record,
    tableData: a,
  });
}

/**
* 重复异常配置事件
*/
function handleOpenRepeatException(record){
  recordData.value = record;
  openRepeatException.value = true;
}

//配置算法
const algorithm = (record) => {
  hasReopen.value = true;
  record.tableName = tableData.value.tableName;
  departObject.value = record;
  // record.piTable = tableData.value.tableName
  // algorithmModal.value.edit(record);
}
//Adepter算法配置
const adapterAlgorithm = (record, type)=>{
  if(type == '1'){
    algorithmTitle.value = 'Adapter算法配置';
  }else{
    algorithmTitle.value = 'RA算法配置';
  }
  adapterHasReopen.value = true;
  record.tableName = tableData.value.tableName;
  record.algorithmType = type; 
  departObject.value = record;
}


//算法复制
const copyAlgorithm = (record) => {
  hasCopyReopen.value = true;
  record.tableName = tableData.value.tableName;
  departObject.value = record;
}
function handleCopySuccess(){
  hasCopyReopen.value = false;
  reload();
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
    // {
    //   label: 'Adepter算法',
    //   onClick: adapterAlgorithm.bind(null, record, '1'),
    // },
    // {
    //   label: 'RA算法',
    //   onClick: adapterAlgorithm.bind(null, record, '2'),
    // },
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
        await openOrCloseEdit({ tableId: tableData.value.id,isEdit:1 }, handleSuccess);
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
        await openOrCloseEdit({ tableId: tableData.value.id,isEdit:0}, handleSuccess);
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