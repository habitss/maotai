<template>
  <div class="p-2">
    <!--查询区域-->
    <div class="jeecg-basic-table-form-container">
      <a-form ref="formRef" @keyup.enter.native="searchQuery" :model="queryParam" :label-col="labelCol" :wrapper-col="wrapperCol">
        <a-row :gutter="24">
          <a-col :lg="5">
            <a-form-item label="日期">
              <a-range-picker class="date-pick" :ranges="{}" 
              format="YYYY-MM-DD" valueFormat="YYYY-MM-DD" 
              v-model:value="queryParam.timeRange" 
              @change="onChangeDate"
              style="width: 215px;"
              @select="customOnSelect" />
            </a-form-item>
          </a-col>
          <a-col :lg="6">
            <a-checkbox @change="e => changeWeek(e)">KPI统计周累</a-checkbox>
            <a-button type="primary" preIcon="ant-design:search-outlined" @click="searchQuery">{{ t('language.search') }}</a-button>
          </a-col>
        </a-row>
      </a-form>
    </div>
    <!--引用表格-->
    <BasicTable @register="registerTable" :scroll="{ x: 1500 }">
      <!--插槽:table标题-->
      <template #tableTitle></template>
    </BasicTable>
  </div>
</template>

<script lang="ts" name="sop-sopStatisticData" setup>
  import { ref, reactive, onMounted } from 'vue';
  import { BasicTable } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { columns } from './UseStatistics.data';
  import { list, getHead } from './UseStatistics.api';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();
  import { downloadFile } from '/@/utils/common/renderUtils';
  import dayjs from 'dayjs';
  import { useUserStore } from '/@/store/modules/user';

  const formRef = ref();
  const queryParam = reactive<any>({});
  const toggleSearchStatus = ref<boolean>(false);
  const registerModal = ref();
  const userStore = useUserStore();
  const tableColumns = ref([]);
  //注册table数据
  const { prefixCls, tableContext, onExportXls, onImportXls } = useListPage({
    tableProps: {
      title: '系统使用统计',
      api: list,
      canResize:true,
      useSearchForm: false,
      pagination:false,
      showActionColumn: false,
      actionColumn: {
        width: 120,
        fixed: 'right',
      },
      beforeFetch: async (params) => {
        return Object.assign(params, queryParam);
      },
    }
  });
  const [registerTable, { reload,setColumns , collapseAll, updateTableDataRecord, findTableDataRecord, getDataSource }, { rowSelection, selectedRowKeys }] = tableContext;
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
        auth: 'sop:mes3_sop_statistic_data:edit'
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
        auth: 'sop:mes3_sop_statistic_data:delete'
      }
    ]
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
  
  //推送时间
  function onChangeDate(value,dates){
    queryParam.checkDateBegin = dates[0]
    queryParam.checkDateEnd = dates[1]
  }
  //获取表头
  async function getTableHead(){
    await getHead().then((res) => {
      if (res) {
        tableColumns.value = deepCloneByJSON(columns);
        let children = tableColumns.value[2].children
        children.push(...res);
      }
      //设置表头
      setColumns(tableColumns.value);
    })
    
  }
  
  //深度拷贝
  const deepCloneByJSON = (obj) => {
    return JSON.parse(JSON.stringify(obj));
  }

  // KPI统计周累
  function changeWeek(e){
    queryParam.week = e.target.checked
  }
  //日期限制
  function customOnSelect(selectedRange){
    const startYear = selectedRange[0].year();
      const endYear = selectedRange[1].year();
      if (startYear !== endYear) {
        console.error('不允许跨年选择');
        return false;
      }
      queryParam.timeRange = selectedRange;
  }
  /**
   * 获取当前日期（格式：YYYY-MM-DD）
   * @returns {string} 当前日期的字符串
   */
  function getCurrentDate() {
      const date = new Date();
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      return `${year}-${month}-${day}`;
  }
  /**
   * 获取当月第一天（格式：YYYY-MM-DD）
   * @returns {string} 当月第一天的字符串
   */
  function getFirstDayOfMonth() {
      const date = new Date();
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      return `${year}-${month}-01`;
  }
  onMounted(() => {
    // 周
    queryParam.week = false;
    let currentDate = getCurrentDate();
    let firstDayOfMonth = getFirstDayOfMonth();
    //设置日期
    queryParam.timeRange=[firstDayOfMonth,currentDate]
    queryParam.checkDateBegin = queryParam.timeRange[0]
    queryParam.checkDateEnd = queryParam.timeRange[1]
    getTableHead();
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
