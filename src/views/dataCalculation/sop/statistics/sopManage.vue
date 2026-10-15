<template>
    <div class="p-2">
      <!--查询区域-->
      <div class="jeecg-basic-table-form-container">
        <a-form ref="formRef" @keyup.enter.native="searchQuery" :model="queryParam" :label-col="labelCol" :wrapper-col="wrapperCol">
          <a-row :gutter="24">
            <!--年份-->
            <a-col :lg="6">
              <a-form-item label="年份">
                <a-date-picker v-model:value="queryParam.yearMonth" picker="month" valueFormat="YYYY-MM" format="YYYY-MM" @change="classification"/>
              </a-form-item>
            </a-col>
            
            <!--部门-->
            <a-col :span="6">
              <a-form-item label="部门">
                <kt-depart-select
                  v-model:value="queryParam.deptArr"
                  select-type="depart"
                  placeholder="请选择部门"
                  mode='multiple'
                  @change="handleDeptChange"
                />
              </a-form-item>
            </a-col>
            
            <a-col :xl="6" :lg="7" :md="8" :sm="24">
              <span style="float: left; overflow: hidden; margin-left: -12px" class="table-page-search-submitButtons">
                <a-col :lg="6">
                  <a-button type="primary" preIcon="ant-design:search-outlined" @click="searchQuery">{{ t('language.search') }}</a-button>
                  <!-- <a-button type="primary" preIcon="ant-design:reload-outlined" @click="searchReset" style="margin-left: 8px">{{
                    t('language.reset')
                  }}</a-button> -->
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
        </template>
        <template v-slot:bodyCell="{ column, record, index, text }">
        </template>
      </BasicTable>
    </div>
  </template>
  
  <script lang="ts" name="sop-sopManage" setup>
    import { ref, reactive, onMounted } from 'vue';
    import { BasicTable, useTable, TableAction } from '/@/components/Table';
    import { useListPage } from '/@/hooks/system/useListPage';
    import { columns } from './sopManage.data';
    import { list } from './sopManage.api';
    import { downloadFile } from '/@/utils/common/renderUtils';
    // import SopReviewPlanModal from './components/SopReviewPlanModal.vue'
    import { useUserStore } from '/@/store/modules/user';
    import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
    import { useI18n } from '/@/hooks/web/useI18n';
    import dayjs from 'dayjs';
    const { t } = useI18n();
    const formRef = ref();
    const queryParam = reactive<any>({
      year: '',
      depts:'',
      dept:''
    });
    const toggleSearchStatus = ref<boolean>(false);
    const registerModal = ref();
    const userStore = useUserStore();
    const column = ref([])
    //注册table数据
    const { prefixCls, tableContext, onExportXls, onImportXls } = useListPage({
      tableProps: {
        title: 'SOP管理回顾统计',
        api: list,
        canResize:false,
        useSearchForm: false,
        showActionColumn: false,
        pagination: false,
        actionColumn: {
          width: 120,
          fixed: 'right',
        },
        beforeFetch: async (params) => {
          return Object.assign(params, queryParam);
        },
      }
    });
    const [registerTable, { reload,setColumns, collapseAll, updateTableDataRecord, findTableDataRecord, getDataSource }, { rowSelection, selectedRowKeys }] = tableContext;
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
          auth: 'sop:mes3_sop_review_plan:edit'
        },
      ];
    }
     
    /**
     * 下拉操作栏
     */
    function getDropDownAction(record) {
      return [
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
          auth: 'sop:mes3_sop_review_plan:delete'
        }
      ]
    }
  
    /**
     * 查询
     */
    function searchQuery() {
    //   let str = queryParam.yearMonth.split('-');
    //   queryParam.year = str[0]
    //   queryParam.month = str[1].replace(/^0/, "")
      setParameter()
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
    
    /**
     * 设置参数
     */
    function setParameter(){
      if(!queryParam.yearMonth){
        let date = new Date();
        queryParam.yearMonth = dayjs(date).format('YYYY-MM'); 
      }
      let str = queryParam.yearMonth.split('-');
      queryParam.year = str[0];
      queryParam.month = str[1].replace(/^0/, "")
      let tableColumns = deepCloneByJSON(columns);
      //替换年份
      tableColumns[2].title=queryParam.year+"年SOP管理进度";
      //替换月份
      tableColumns[3].title=queryParam.month+"月";
      setColumns(tableColumns);
    }
    //深度拷贝
    const deepCloneByJSON = (obj) => {
        return JSON.parse(JSON.stringify(obj));
    }

    function handleDeptChange(value){
      queryParam.depts = value.join(',');
    }

    onMounted(() => {
       setParameter();
    })
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
  