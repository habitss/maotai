<template>
  <div class="p-2">
    <!--查询区域-->
    <div class="jeecg-basic-table-form-container">
      <a-form ref="formRef" @keyup.enter.native="searchQuery" :model="queryParam" :label-col="labelCol" :wrapper-col="wrapperCol">
        <a-row :gutter="24">
          <!--部门-->
          <a-col :lg="6">
            <a-form-item :label="t('language.department')">
              <kt-depart-select
                v-model:value="queryParam.queryDept"
                select-type="depart"
                :placeholder="t('language.selectDepartment')"
              />
            </a-form-item>
          </a-col>
          <!--KPI分类-->
          <a-col :lg="6">
            <a-form-item label="KPI分类">
              <a-tree-select
                v-model:value="queryParam.kpiType"
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
          <a-col :lg="6">
            <a-form-item label="选择日期">
              <a-range-picker format="YYYY-MM-DD" v-model:value="queryParam.time" @change="timeChange"/>
            </a-form-item>
          </a-col>
          <a-col :lg="3">
            <a-form-item label="5why/差距分析" :wrapper-col="{ span: 3 }" :label-col = "{ span: 21 }">
              <a-checkbox v-model:checked="queryParam.parmasType"></a-checkbox>
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
    </div>
    
    <!--引用表格-->
    <BasicTable @register="registerTable" @fetch-success="onFetchSuccess">
      <!--插槽:table标题-->
      <template #tableTitle>
        <div class="numNode">
          <span>总数:</span><i>{{ numOptionsMap.excSum || 0 }}</i>
          <span>已分析:</span><i>{{ numOptionsMap.excCount || 0 }}</i>
          <span>统计:</span><i>{{ isNaN(numOptionsMap.percent) ? '0%' : (numOptionsMap.percent * 100).toFixed(2) + '%' }}</i>
        </div>
      </template>
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :dropDownActions="getDropDownAction(record)"/>
      </template>

      <!--内容-->
      <template #fiveType="{ text, record }">
        <a-button v-if="record.fiveType" type="link" class="projectName" @click="openFiveWhy(record)"><span>已分析</span></a-button>
        <span v-else>未分析</span>
      </template>
      <template #diffType="{ text, record }">
        <a-button v-if="record.diffType" type="link" class="projectName" @click="openGapAnalysis(record)"><span>已分析</span></a-button>
        <span v-else>未分析</span>
      </template>
      <template #colorNode="{ text, record }">
        {{ record.color === 1 ? '绿区' : record.color === 2 ? '红区' : '黄区' }}
      </template>
      <template #actionNode="{ text, record }">
        <div v-for="item in record.actionList"><span>{{ item }}</span></div>
      </template>
      <!--OK/NOK-->
      <template #switchCell="{ column, record }">
        <a-switch class="custom-switch" :checkedValue="1" checkedChildren="OK" :unCheckedValue="0" unCheckedChildren="NOK"
            v-model:checked="record[column.key]" @change="switchChange(record, column.key)" style="width: 55px;"/>
      </template>
      

    </BasicTable>
    <FiveWhyModal @register="registerFiveWhyModal" @success="handleSuccess"></FiveWhyModal>
    <!-- 差距分析 -->
    <KpiTargetModal ref="kpiTargetModal" @success="handleSuccess"></KpiTargetModal>
  </div>
</template>

<script lang="ts" name="kpi-kpiInfo" setup>
  import { ref, reactive,onMounted } from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { columns ,searchFormSchema} from './KpiExceptionList.data';
  import { list,kpiExceptionSummaryprecent,confirm } from './KpiExceptionList.api';
  import { downloadFile } from '/@/utils/common/renderUtils';
  import { useUserStore } from '/@/store/modules/user';
  import { list as typeList } from '/@/views/dataCalculation/kpi/type/KpiType.api';
  import JTreeSelect from '/@/components/Form/src/jeecg/components/JTreeSelect.vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import dayjs from 'dayjs';
  import { useModal } from '/@/components/Modal';
  import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
  import FiveWhyModal from '/@/views/dataCalculation/fiveWhy/components/FiveWhyModal.vue';
  import KpiTargetModal from '/@/views/dataCalculation/kpi/reports/components/kpiTargetModal.vue';
  const { t } = useI18n();

  const formRef = ref();
  const queryParam = reactive<any>({
    parmasType: true,
    kpiType:undefined
  });
  const numOptionsMap = ref({});
  const toggleSearchStatus = ref<boolean>(false);
  const treeData = ref([]);
  const registerModal = ref();
   // 用户信息
  const userStore = useUserStore();
  const kpiTargetModal = ref();
  const { createMessage } = useMessage();
  const parmasType = ref(true);
  //注册5why model
  const [registerFiveWhyModal, { openModal }] = useModal();

  //注册table数据
  const { prefixCls, tableContext, onExportXls, onImportXls } = useListPage({
    tableProps: {
      title: 'KPI清单',
      api: list,
      columns,
      canResize:true,
      isTreeTable: false,
      useSearchForm: false,
      showActionColumn: false,
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
      }
    }
  });
  const [registerTable, { reload, collapseAll, updateTableDataRecord, findTableDataRecord, getDataSource }, { rowSelection, selectedRowKeys }] = tableContext;
  const labelCol = reactive({
    span: 8
  });
  const wrapperCol = reactive({
    span: 16
  });
  //数据请求完成
  function onFetchSuccess(items,total){
    //数据处理
    // items.items.forEach((ele, index) => {
    //   ele.visible = false;
    //   ele.evaluationResult = ele.evaluationResult / 1 == 1 ? '绿区' : ele.evaluationResult / 1 == 2 ? '红区' : '黄区'
    //   ele.createTime = ele.createTime ? dayjs(ele.createTime).format('YYYY-MM-DD') : null;
    //   ele.curIndex = index + 1;
    // })

    //处理合并
    //mergeCells
    // columns.forEach(function(element, index, array) {
    //   console.log(element,index,array)
    //   debugger
    //   if(element.dataIndex ==='dateIdentify' || element.dataIndex ==='kpiName'){
    //     element.title = '123'
    //   }
    // });

    // for (let i = 0; i < columns.length; i++) {
    //   let element = columns[i];
    //   if(element.dataIndex === 'dateIdentify' || element.dataIndex === 'kpiName'){
    //     element.title = '123'
    //   }
    // }
    
    
    //获取统计数据
    getExceptionPrecent();
  }

  function getExceptionPrecent() {
    let factoryCode = userStore.factoryCode
    let dept = userStore?.userInfo?.orgCode;
    let obj = { ...queryParam, type: queryParam.parmasType ? 1 : 0, kpiType:queryParam.kpiType,factoryCode:factoryCode,hierarchy: dept}
    kpiExceptionSummaryprecent(obj).then((res) => {
      numOptionsMap.value = res;
    }).finally()
  }
  //时间选择
  function timeChange(){
    queryParam.startTime = dayjs(queryParam.time[0]).format('YYYY-MM-DD');
    queryParam.endTime = dayjs(queryParam.time[1]).format('YYYY-MM-DD');
  }
  /**
   * 成功回调
   */
  function handleSuccess() {
    (selectedRowKeys.value = []) && reload();
  }
  
  /**
   * 合并单元格
   */
  function mergeCells(text, data, key, index) {
    // 上一行该列数据是否一样
    if (index !== 0 && text === data[index - 1][key]) {
      return 0;
    }
    let rowSpan = 1;
    // 判断下一行是否相等
    for (let i = index + 1; i < data.length; i++) {
      if (text !== data[i][key]) {
        break;
      }
      rowSpan++;
    }
    return rowSpan;
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
    //刷新数据
    reload();
  }
  

  onMounted(() => {
    //设置时间
    setDefaultTimeRange();
    //初始化年
    // 创建一个 Date 对象，默认表示当前日期和时间
    const currentDate = new Date();
    // // 使用 getFullYear() 方法获取四位数的年份
    const currentYear = currentDate.getFullYear();
    queryParam.year = currentYear+"";
    //初始化分类
    classification();
    
  });
  //默认时间为最近7天
  function setDefaultTimeRange() {
    const end = dayjs();
    const start = dayjs().subtract(6, 'day'); // 减去6天，共7天
    queryParam.time = [start, end];
    queryParam.startTime = dayjs(queryParam.time[0]).format('YYYY-MM-DD');
    queryParam.endTime = dayjs(queryParam.time[1]).format('YYYY-MM-DD');
  };


  //分类
  async function classification(){
    let res = await typeList({});
    treeData.value.push(...res);
  }
  /**
   * 打开5w弹窗
   */
  function openFiveWhy(params) {
    let kpiRowData = {
      kpiName: {id: params.kpiCode},
      colKey: params.kpiColKey,
      year: params.year,
    }
    openModal(true, {
      params: {
        kpiRowData,
        type: 2
      },
    });
  }

  const rowData = ref({});
  const paramData = ref({});
  /**
   * 打开差距分析弹窗
   */
  function openGapAnalysis(params) {
    paramData.value = {
      yearMonth: params.whyTitle,
      kpiName: params.kpiName,
      year: params.year,
      kpiCode: params.kpiCode,
      reportId: "",
      recordId: params.kpiCode
    }
    rowData.value = {
      kpiName: {id: params.kpiCode, value: params.kpiName},
      colKey: params.kpiColKey,
      year: params.year,
    };
    kpiTargetModal.value.disableSubmit = false;
    kpiTargetModal.value.showModal(1,rowData,paramData);
  }
  //OK/NOK
  function switchChange(record, dataIndex){
    confirm({id:record["id"], [dataIndex] :record[dataIndex]}).then((res) => {
      if(res.code === 200){
        createMessage.success(res.message);
        //searchQuery();
      }
    }).finally()


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


  .numNode {
    display: flex;
    font-size: 16px;

    span {
      font-weight: bold;
    }

    i {
      margin-right: 3.6rem;
    }
  }
  /* 覆盖 a-switch 的背景色和滑块颜色 */
  .ant-switch {
    //background-color: #ff5722 !important; /* 修改背景色 */
  }
  .ant-switch-checked {
    background-color: #4CAF50 !important; /* 修改选中时的背景色 */
  }
  .ant-switch-handle {
    background-color: #fff !important; /* 修改滑块颜色 */
  }
</style>
