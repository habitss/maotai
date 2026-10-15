<template>
  <div>
    <!--查询区域-->
    <div class="jeecg-basic-table-form-container">
      <a-form @keyup.enter.native="searchQuery" :model="queryParam" :label-col="labelCol" :wrapper-col="wrapperCol" labelAlign="left">
        <a-row :gutter="24">
          <a-col :lg="8">
            <a-form-item label="报表">
              <a-select v-model:value="queryParam.tableId" :options="tableArr" option-filter-prop="label" show-search placeholder="请选择" @change="tableChange" :disabled="disabled"  allowClear="true"></a-select>
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <span style="float: left; overflow: hidden; margin-left: -12px" class="table-page-search-submitButtons">
              <a-col :lg="6">
                <a-button type="primary" preIcon="ant-design:search-outlined" @click="searchQuery">{{ t('language.search') }}</a-button>
                <a-button type="primary" preIcon="ant-design:reload-outlined" @click="searchReset" style="margin-left: 8px">{{
                  t('language.reset')
                }}</a-button>
                <!-- <a @click="toggleSearchStatus = !toggleSearchStatus" style="margin-left: 8px">
                  {{ toggleSearchStatus ? t('language.collapse') : t('language.open') }}
                  <Icon :icon="toggleSearchStatus ? 'ant-design:up-outlined' : 'ant-design:down-outlined'" />
                </a> -->
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
          <a-dropdown v-if="selectedRowKeys.length > 0">
              <template #overlay>
                <a-menu>
                  <a-menu-item key="1" @click="batchHandleDelete">
                    <Icon icon="ant-design:delete-outlined"></Icon>
                    删除
                  </a-menu-item>
                </a-menu>
              </template>
              <a-button>批量操作
                <Icon icon="mdi:chevron-down"></Icon>
              </a-button>
        </a-dropdown>
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
    <!--配置字段-->
    <a-drawer
      v-if="hasReopen == true"
      v-model:visible="hasReopen"
      class="custom-class"
      root-class-name="root-class-name"
      :root-style="{ color: 'blue' }"
      title="分配字段"
      width="90%"
      placement="right">
      <AllocationTableFieldList @ok="updateList" :departObject="departObject"></AllocationTableFieldList>
    </a-drawer>
    <!-- 表单区域 -->
    <AllocationTableModal ref="registerModal" @success="handleSuccess"></AllocationTableModal>
  </div>
  
  <a-modal 
    width="800"
    :title="title" 
    :visible="visible" 
    @ok="handleOk" 
    okText="复制URL地址"
    @cancel="handleCloseMenuUrlModal" 
    cancelText="关闭"
  >
    <div style="height: 100px; padding: 20px; color: #909399;">
      {{ menuUrlToCopy }}
    </div>
  </a-modal>

  <!--报表配置-->
  <a-drawer
    v-if="reportConfigVisible == true"
    v-model:visible="reportConfigVisible"
    class="custom-class"
    root-class-name="root-class-name"
    :root-style="{ color: 'blue' }"
    title="报表配置"
    width="60%"
    placement="right"
  >
    <AllocationReportConfig :departObject="departObject"></AllocationReportConfig>
  </a-drawer>

  <!--模板配置抽屉-->
  <TemplateConfigDrawer @register="registerDrawer" @success="handleSuccess" />
  <!--表头配置页面-->
  <TableHeadConfigDrawer @register="registerTableHeadDrawer" @success="handleSuccess" />
  <!-- <TableHeadModal @register="registerTableHeadModal" @success="handleSuccess" /> -->
  <!--垂直表头配置抽屉-->
  <VerticalHeadConfigDrawer @register="registerVerticalHeadConfigDrawer" @success="handleSuccess" />
  <!--数据赛选条件-->
  <FilterCriteriaMode @register="filterCriteria" @success="handleSuccess" />
</template>

<script lang="ts" name="pi-piBasePoint" setup>
  import { ref, reactive, onMounted, unref, nextTick } from 'vue';
  import {BasicTable, useTable, TableAction} from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage'
  import {columns} from './AllocationTable.data';
  import {list, deleteOne, batchDelete, getImportUrl,getExportUrl} from './AllocationTable.api';
  import AllocationTableModal from './components/AllocationTableModal.vue'
  import AllocationTableFieldList from './AllocationTableFieldList.vue'
  import AllocationReportConfig from './AllocationReportConfig.vue'
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useUserStore } from '/@/store/modules/user';
  import { useCopyToClipboard } from '/@/hooks/web/useCopyToClipboard';
  import {list as tableList} from '/@/views/dataCalculation/pi/customizeTable/PiCustomizeTable.api';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useDrawer } from '/@/components/Drawer';
  import { useModal } from '/@/components/Modal';
  import TemplateConfigDrawer from './components/TemplateConfigDrawer.vue';
  import VerticalHeadConfigDrawer from './components/VerticalHeadConfigDrawer.vue';
  // import TableHeadModal from './components/TableHeadModal.vue';
  import TableHeadConfigDrawer from './components/TableHeadConfigDrawer.vue';
  import FilterCriteriaMode from './components/FilterCriteria.vue';
import { cloneDeep } from 'lodash-es';
  const { t } = useI18n();

  const queryParam = ref<any>({});
  const toggleSearchStatus = ref<boolean>(false);
  const registerModal = ref();
  const userStore = useUserStore();
  const { createMessage } = useMessage();
  const exportParams = reactive({
    departId: '1',
    deviceId: queryParam?.value?.deviceId,
  });
  //模板配置抽屉
  const [registerDrawer, { openDrawer }] = useDrawer();
  //模板配置抽屉
  const [registerVerticalHeadConfigDrawer, { openDrawer : openVerticalHeadConfigDrawer }] = useDrawer();
  //表头设置modal
  const [registerTableHeadDrawer, { openDrawer : openTableHeadDrawer }] = useDrawer();
  //数据筛选条件
  const [filterCriteria, { openModal : openFilterCriteria }] = useModal();
  //报表集合
  const tableArr = reactive<Record<string, any>>([]);

  //分配字段
  const hasReopen = ref<boolean>(false);
  const departObject = ref<Object>({});

  //配置菜单地址modal
  const visible = ref<boolean>(false);
  //配置菜单地址modal标题
  const title = ref<string>('');
  //配置菜单复制文本
  const menuUrlToCopy = ref('');
  //消息提示
  const { createMessage: msg } = useMessage();
  //报表配置显示
  const reportConfigVisible = ref<boolean>(false);

  //注册table数据
  const { prefixCls,tableContext,onExportXls,onImportXls } = useListPage({
      tableProps:{
           title: 'PI日报分配',
           api: list,
           columns,
           canResize:true,
           useSearchForm: false,
           actionColumn: {
              width: 250,
              fixed: 'right',
           },

        beforeFetch: (params) => {
          return Object.assign(params, queryParam.value);
        },
      },
      exportConfig: {
        name:"PI日报分配",
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
    //exportParams.departId = getDepartId(userStore?.getUserInfo?.orgCode);
    getTableArr();
  });

  /**
   * 初始化设备类型下拉数据
   */
  async function getTableArr() {
      tableList({ pageNo: 1, pageSize: 999 }).then((res) => {
        if (res && res.records && res.records.length > 0) {
          res.records.map((item) => {
            tableArr.push({label: item.name, value: item.id});
          });
        }
      });
  }

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
      label: t('language.edit'),
      onClick: handleEdit.bind(null, record),
    },
    {
      label: "分配字段",
      onClick: configField.bind(null, record),
      auth: '',
    },
    {
      label: "配置菜单",
      onClick: handleShowMenuUrlModal.bind(null, record),
      auth: '',
    },
    // {
    //   label: '获取参数',
    //   onClick: handleParameter.bind(null, record),
    //   auth: 'mdm:deviceManage:parameter',
    // },
  ];
}
//配置字段
const configField = (record) => {
  hasReopen.value = true;
  departObject.value = record;
}
//获取任务参数
function handleParameter(record: Recordable){
  //生成数据
  copyTextToClipboard("123456")
}

// 封装的复制方法
const copyTextToClipboard = async (textToCopy) => {
  if (typeof textToCopy ==='string') {
    try {
      await navigator.clipboard.writeText(textToCopy);
      createMessage.success('复制成功');
    } catch (err) {
      createMessage.warning('复制失败:'+err);
    }
  }
};


/**
 * 下拉操作栏
 */
function getDropDownAction(record) {
  return getActionData(record);
}

//菜单动态显示
function getActionData(record) {
  const actions = [
    {
      label: "报表配置",
      onClick: handleReportConfig.bind(null, record),
      auth: '',
      visible: '1'
    },
    {
      label: "模板配置",
      onClick: handleTemplateConfig.bind(null, record, 2),
      auth: '',
      visible: '1'
    },
    {
      label: "表头配置",
      onClick: handleHeadConfig.bind(null, record),
      auth: '',
      visible: '1,2'
    },
    {
      label: "垂直表头配置",
      onClick: handleOpenVerticalHeadConfigDrawer.bind(null, record),
      auth: '',
      visible: '1'
    },
    {
      label: "统计表头配置",
      onClick: handleTemplateConfig.bind(null, record, 4),
      auth: '',
      visible: '1,2'
    },
    {
      label: "筛选条件",
      onClick: filterCriteriaFun.bind(null, record),
      auth: '',
      visible: '1'
    },
    {
      label: t('language.detail'),
      onClick: handleDetail.bind(null, record),
      visible: '1,2'
    },
    {
      label: t('language.delText'),
      popConfirm: {
        title: t('language.confirmDelete'),
        confirm: handleDelete.bind(null, record),
        placement: 'left',
      },
      visible: '1,2'
    },
  ];
  
  let visible = '1';
  if(record.multipleTables === '1'){
    visible = '2';
  }

  // 动态过滤：权限检查 + 可见性检查
  return actions.filter(action => {
    const isVisible = action.visible.includes(visible);
    return isVisible;
  });
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
 * 复制配置菜单链接
 */
 async function handleOk(){
    const { isSuccessRef } = useCopyToClipboard(menuUrlToCopy.value);
    if(isSuccessRef.value){
      msg.success('复制URL成功');
    } else {
      msg.error('复制URL失败，请手动复制链接');
    }
}

/**
 * 关闭配置菜单链接
 */
function handleCloseMenuUrlModal(){
  visible.value = false;
}

/**
 * 打开配置菜单链接
 */
function handleShowMenuUrlModal(record){
  title.value = '【'+record.reportName+'】菜单地址';
  menuUrlToCopy.value = '/dataCalculation/pi/allocationTable/QueryTableList/'+record.id+'/2';
  visible.value = true;
}

//报表配置
const handleReportConfig = (record) => {
  nextTick(() => {
    reportConfigVisible.value = true;
    departObject.value = record;
  });
}
/**
 * 模版配置
 */
function handleTemplateConfig(record, type){
  let r = cloneDeep(record);
  r.reportType = type;
  openDrawer(true, {
    record: r,
  });
}
/**
 * 垂直表头配置
 */
function handleOpenVerticalHeadConfigDrawer(record){
  openVerticalHeadConfigDrawer(true, {
    record,
  });
}
/**
 * 表头配置
 */
function handleHeadConfig(record){
   openTableHeadDrawer(true, {record})
}
/**
 * 数据筛选条件
 */
function filterCriteriaFun(record){
  openFilterCriteria(true, {record})
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