<template>
  <a-spin :spinning="confirmLoading" class="span-box">
  <div class="p-2">
    <!--查询区域-->
    <div class="jeecg-basic-table-form-container">
      <a-form ref="formRef" @keyup.enter.native="searchQuery" :model="queryParam" :label-col="labelCol" :wrapper-col="wrapperCol">
        <a-row :gutter="24">
          <!--年份-->
          <a-col :lg="6">
            <a-form-item label="所属年份">
              <a-date-picker v-model:value="queryParam.year" picker="year" valueFormat="YYYY" format="YYYY" @change="classification"/>
            </a-form-item>
          </a-col>
          <!--KPI分类-->
          <a-col :lg="6">
            <a-form-item label="KPI分类">
              <a-tree-select
                v-model:value="queryParam.perentId"
                show-search
                style="width: 100%"
                :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
                placeholder="请选择KPI分类"
                allow-clear
                :tree-data="treeData"
                tree-node-filter-prop="kpiType"
                :fieldNames= "{children:'children', label:'kpiType', value: 'id' }"
              ></a-tree-select>
            </a-form-item>
          </a-col>
          <!--KPI名称-->
          <a-col :lg="6">
            <a-form-item label="KPI名称">
              <a-input placeholder="请输入KPI名称" v-model:value="queryParam.kpiName" allowClear />
            </a-form-item>
          </a-col>

          <!--层级部门-->
          <a-col :span="6">
            <a-form-item label="层级部门">
              <a-tree-select
                v-model:value="queryParam.deptCode"
                show-search
                style="width: 100%"
                :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
                placeholder="请选择层级部门"
                allow-clear
                :treeDefaultExpandedKeys="treeDefaultExpandedKeys"
                :tree-data="kpiRoomList"
                tree-node-filter-prop="departName"
                :fieldNames= "{children:'children', label:'departName', key: 'id', value: 'orgCode' }"
              ></a-tree-select>
            </a-form-item>
					</a-col>
          <!--填写部门-->
          <a-col :span="6">
							<a-form-item label="填写部门">
                <a-tree-select
                  v-model:value="queryParam.inputDeptQuery"
                  show-search
                  style="width: 100%"
                  :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
                  placeholder="请选择层级部门"
                  allow-clear
                  :treeDefaultExpandedKeys="treeDefaultExpandedKeys"
                  :tree-data="kpiRoomList"
                  tree-node-filter-prop="departName"
                  :fieldNames= "{children:'children', label:'departName', key: 'id', value: 'orgCode' }"
                ></a-tree-select>
							</a-form-item>
					</a-col>


          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <span style="float: left; overflow: hidden; margin-left: -12px" class="table-page-search-submitButtons">
              <a-col :lg="6">
                <a-button type="primary" preIcon="ant-design:search-outlined" @click="searchQuery">{{ t('language.search') }}</a-button>
                <a-button type="primary" preIcon="ant-design:reload-outlined" @click="searchReset" style="margin-left: 8px">{{
                  t('language.reset')
                }}</a-button>
                <!-- <a-button type="primary" preIcon="ant-design:copy-outlined" @click="copy" style="margin-left: 8px">复制</a-button> -->
              </a-col>
            </span>
          </a-col>
        </a-row>
      </a-form>
    </div>
    <!--引用表格-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button type="primary" v-auth="'kpi:mes3_kpi_info:add'"  @click="handleAdd" preIcon="ant-design:plus-outlined"> 新增</a-button>
        <a-button  type="primary" v-auth="'kpi:mes3_kpi_info:exportXls'" preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
        <j-upload-button  type="primary" v-auth="'kpi:mes3_kpi_info:importExcel'"  preIcon="ant-design:import-outlined" @click="onImportXls">导入</j-upload-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                删除
              </a-menu-item>
            </a-menu>
          </template>
          <a-button v-auth="'kpi:mes3_kpi_info:deletionBatch'">批量操作
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)"/>
      </template>
      <template v-slot:bodyCell="{ column, record, index, text }">
      </template>
    </BasicTable>

  <!--复制-->
  <!-- <a-modal
    width="600px"
    title="复制" 
    :visible="visible"
    @ok="rerunOk"
    okText="确认复制"
    @cancel="rerunClose"
    cancelText="关闭"
  >
  <div style="margin: 8px;">
    <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol">
        <a-row>
          <a-col :span="24">
            <a-form-item label="年份" v-bind="validateInfos.year">
              <a-date-picker v-model:value="formData.year" picker="year" valueFormat="YYYY" format="YYYY" style="width: 90%;"/>
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item label="目标年份" v-bind="validateInfos.targetYear">
              <a-date-picker v-model:value="formData.targetYear" picker="year" valueFormat="YYYY" format="YYYY" style="width: 90%;"/>
            </a-form-item>
          </a-col>
        </a-row>
    </a-form>
  </div>
  </a-modal> -->

    <!-- 表单区域 -->
    <KpiInfoModal ref="registerModal" @success="handleSuccess"></KpiInfoModal>
  </div>
</a-spin>
</template>

<script lang="ts" name="kpi-kpiInfo" setup>
  import { ref, reactive,onMounted } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { columns ,searchFormSchema} from './KpiInfo.data';
  import { list, deleteOne, batchDelete, getImportUrl, getExportUrl,copyData } from './KpiInfo.api';
  import { downloadFile } from '/@/utils/common/renderUtils';
  import KpiInfoModal from './components/KpiInfoModal.vue'
  import { useUserStore } from '/@/store/modules/user';
  import { list as typeList } from '/@/views/dataCalculation/kpi/type/KpiType.api';
  // import JTreeSelect from '/@/components/Form/src/jeecg/components/JTreeSelect.vue';
  import { Form } from 'ant-design-vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { queryDeptTreeList } from '/@/views/dataCalculation/kpi/info/KpiInfo.api';
  const { t } = useI18n();
  const formData = reactive<Record<string, any>>({
    // year:'',
    // targetYear:''
  });
  const { createMessage } = useMessage();
   //表单验证
  const validatorRules = {
    year: [{ required: true, message: `年份不为空!` }],
    targetYear: [{ required: true, message: `目标年份不为空!` }]
  };
  const confirmLoading = ref<boolean>(false);
  const useForm = Form.useForm;
  const { validate, validateInfos } = useForm(formData, validatorRules, { immediate: true });
  //复制modal
  const visible = ref<boolean>(false);
  const formRef = ref();
  const queryParam = reactive<any>({});
  const toggleSearchStatus = ref<boolean>(false);
  const treeData = ref([]);
  const registerModal = ref();
  const userStore = useUserStore();
  const kpiRoomList = ref([]);
  const treeDefaultExpandedKeys = ref([]);
  //注册table数据
  const { prefixCls, tableContext, onExportXls, onImportXls } = useListPage({
    tableProps: {
      title: 'KPI清单',
      api: list,
      columns,
      canResize:true,
      isTreeTable: true,
      useSearchForm: false,
      ellipsis:false,
      formConfig: {
        //labelWidth: 120,
        schemas: searchFormSchema,
        autoSubmitOnEnter:true,
        showAdvancedButton:true,
        fieldMapToNumber: [
        ],
        fieldMapToTime: [
        ],
      },
      actionColumn: {
        width: 190,
        fixed: 'right',
      },
      beforeFetch: async (params) => {
        return Object.assign(params, queryParam);
      },
    },
    exportConfig: {
      name: "KPI清单",
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
        label: '编辑',
        onClick: handleEdit.bind(null, record),
        auth: 'kpi:mes3_kpi_info:edit'
      },
      {
        label: '详情',
        onClick: handleDetail.bind(null, record),
      }, {
        label: '删除',
        popConfirm: {
          title: '是否确认删除',
          confirm: handleDelete.bind(null, record),
          placement: 'topLeft',
        },
        auth: 'kpi:mes3_kpi_info:delete'
      }
    ];
  }
   
  /**
   * 下拉操作栏
   */
  function getDropDownAction(record) {
    return []
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
    queryParam.perentId = undefined
    queryParam.kpiName = undefined
    //刷新数据
    reload();
  }
  

  onMounted(() => {
    
    //初始化年
    // 创建一个 Date 对象，默认表示当前日期和时间
    const currentDate = new Date();
    // // 使用 getFullYear() 方法获取四位数的年份
    const currentYear = currentDate.getFullYear();
    queryParam.year = currentYear+"";
    //初始化分类
    classification(currentYear);
    //部门
    deptTreeList();
  });
  //分类
  async function classification(){
    treeData.value = [];
    let res = await typeList({});
    if(res && res.length > 0){
      treeData.value.push(...res);
    }
  }

  //复制(打开选择框)
  function copy(){
    // formData.year = "";
    // formData.targetYear = "";
    visible.value = true;
  }
  //验证方法
  async function validateDate(){
    await validate();
  }
  //复制
  async function rerunOk(){
    // 触发表单验证
    validateDate();
    if(!formData.year){
      createMessage.warning("年份不为空");
      return false;
    }
    if(!formData.targetYear){
      createMessage.warning("目标年份不为空");
      return false;
    }
    if(formData.year == formData.targetYear){
      createMessage.warning("年份和目标年份不能相同");
      return false;
    }
    confirmLoading.value = true;
    copyData(formData)
      .then((res) => {
      if (res.success) {
        //执行成功
        createMessage.success(res.message);
        handleSuccess();
        visible.value = false;
      }else{
        createMessage.warning(res.message);
      }
    }).finally(() => {
      confirmLoading.value = false;
    });
  }
  //复制弹框关闭
  function rerunClose(){
    visible.value = false;
  }

  //部门树
  async function deptTreeList(){
    let res = await queryDeptTreeList({factory:"",isRoot:"true"});
    kpiRoomList.value = res
    //获取第一层(展开)
    if(res){
      treeDefaultExpandedKeys.value = [res[0].orgCode];
    }
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
