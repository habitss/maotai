<template>
  <a-spin :spinning="confirmLoading">
    <div class="p-2">
      <!--查询区域-->
      <div class="jeecg-basic-table-form-container">
        <a-form ref="formRef" @keyup.enter.native="searchQuery" :model="queryParam" :label-col="labelCol" :wrapper-col="wrapperCol">
          <a-row :gutter="24">
            <!--年份-->
            <a-col :lg="6">
              <a-form-item label="所属年份">
                <a-date-picker v-model:value="queryParam.year" picker="year" valueFormat="YYYY" format="YYYY" @change="getSelectMounth"/>
              </a-form-item>
            </a-col>
            
            <!--月份-->
            <a-col :lg="6">
              <a-form-item label="月份">
                <a-select v-model:value="queryParam.months" class="select-style" show-search
                  mode="multiple" :maxTagCount="1" :filter-option="filterOption">
                  <a-select-option :value="item.value" v-for="item in monthItem" :key="item.value">
                    {{ item.text }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            
            <!--部门-->
            <a-col :span="6">
              <a-form-item label="部门">
                <kt-depart-select
                  v-model:value="queryParam.deptQuery"
                  select-type="depart"
                  placeholder="请选择部门"
                />
              </a-form-item>
            </a-col>
            
            <!--产线-->
            <a-col :span="6">
              <a-form-item label="产线">
                <kt-depart-select
                    v-model:value="queryParam.lineCode"
                    select-type="line"
                    placeholder="请选择产线"
                    :parentKey="queryParam.deptQuery"
                  />
              </a-form-item>
            </a-col>

             <!--SOP名称-->
            <a-col :span="6">
              <a-form-item label="SOP名称">
                <a-input v-model:value="queryParam.sopDesc" placeholder="请输入SOP名称" style="width:100%"></a-input>
              </a-form-item>
            </a-col>

             <!--SOP编号-->
            <a-col :span="6">
              <a-form-item label="SOP编号">
                <a-input v-model:value="queryParam.sopNumber" placeholder="SOP编号" style="width:100%"></a-input>
              </a-form-item>
            </a-col>

             <!--SOP类型-->
            <a-col :span="6">
              <a-form-item label="SOP类型">
                <kt-dict
                  v-model:value="queryParam.sopType"
                  placeholder="请选择SOP类型"
                  dict="MES3_SOP_TYPE"
                />
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
      <BasicTable @register="registerTable" @row-dbClick="rowDbClick">
        <!--插槽:table标题-->
        <template #tableTitle>
          <a-button type="primary" v-auth="'sop:mes3_sop_statistic_data:add'"  @click="submitForm(null)" preIcon="ant-design:plus-outlined">保存</a-button>
          <a-button  type="primary" v-auth="'sop:mes3_sop_statistic_data:exportXls'" preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
          <j-upload-button  type="primary" v-auth="'sop:mes3_sop_statistic_data:importExcel'"  preIcon="ant-design:import-outlined" @click="onImportXls">导入</j-upload-button>
        </template>

        <!--人员姓名显示-->
        <template v-slot:userNameSlots="{ column, record }">
          <span>{{ getOptionText(record[column.dataIndex], 'user') }}</span>
        </template>

        <!--诊断负责人-->
        <template v-slot:checkUserIdSlots="{ column, record }">
          <span v-if="showSlotRow == record.id">
            <!-- <KtUserScrollSelect v-model:value="record[column.dataIndex]" placeholder="请选择诊断负责人"></KtUserScrollSelect> -->
            <a-select v-model:value="record[column.dataIndex]" :options="userArray" placeholder="请选择诊断负责人" allowClear style="width:90%" show-search option-filter-prop="label"></a-select>
          </span>
          <span v-else>{{ getOptionText(record[column.dataIndex], 'user') }}</span>
        </template>

        <!--上一次诊断年份-->
        <template v-slot:lastCheckYearSlots="{ column, record }">
          <span v-if="showSlotRow == record.id">
            <a-date-picker v-model:value="record[column.dataIndex]" picker="year" placeholder="请选择" valueFormat="YYYY" format="YYYY"/>
          </span>
          <span v-else>{{ record[column.dataIndex] }}</span>
        </template>

        <!-- 诊断频率 -->
        <template v-slot:checkIntervalSlots="{ column, record }">
          <span v-if="showSlotRow == record.id">
            <a-select v-model:value="record[column.dataIndex]" :options="frequencyArray" placeholder="请选择诊断频率" allowClear style="width:90%"></a-select>
          </span>
          <span v-else>{{ getOptionText(record[column.dataIndex], 'MES3_SOP_FREQUENCY') }}</span>
        </template>

        <!-- 被诊断人***** -->
        <template v-slot:byCheckUserIdSlots="{ column, record, index }">
          <span>
            <div style="" class="check-user">
              <div style="display: flex; flex-direction: column;" >
                <!--新增-->
                <p><a-icon @click="addSourceData(record, 1)" type="plus-square" /></p>
                <!--批量新增-->
                <p><a-icon @click="addBatchSourceData(record)" type="form-outlined" /></p>
                <!--删除-->
                <p><a-popconfirm title="确定删除吗?" @confirm="reduceRecord(record, index)"><a-icon type="minus-square"/> </a-popconfirm></p>
              </div>
              <a-select v-if="showSlotRow == record.id" v-model:value="record[column.dataIndex]" :options="userArray" placeholder="请选择被诊断人" allowClear 
                style="width:90%" show-search option-filter-prop="label">
              </a-select>
              <span v-else>{{ getOptionText(record[column.dataIndex], 'user') }}</span>
            </div>
          </span>
        </template>

        <!-- 员工状态 -->
        <template v-slot:employeeStateSlots="{ column, record }">
          <span v-if="showSlotRow == record.id">
            <a-select v-model:value="record[column.dataIndex]" :options="statusArray" placeholder="请选择" allowClear style="width:90%"></a-select>
          </span>
          <span v-else>{{ getOptionText(record[column.dataIndex], 'MES3_SOP_OP_STATUS') }}</span>
        </template>

        <!-- 办公室诊断 -->
        <template v-slot:officeFileStateSlots="{ column, record }">
          <span v-if="showSlotRow == record.id">
            <a-select v-model:value="record[column.dataIndex]" :options="checkArray" placeholder="请选择" allowClear style="width:90%"></a-select>
          </span>
          <span v-else>{{ getOptionText(record[column.dataIndex], 'MES3_SOP_CHECK_ALL') }}</span>
        </template>
        <!-- 现场诊断 -->
        <template v-slot:sceneCheckSlots="{ column, record }">
          <span v-if="showSlotRow == record.id">
            <a-select v-model:value="record[column.dataIndex]" :options="checkArray" placeholder="请选择" allowClear style="width:90%"></a-select>
          </span>
          <span v-else>{{ getOptionText(record[column.dataIndex], 'MES3_SOP_CHECK_ALL') }}</span>
        </template>
        <!--计划状态(周)-->
        <!--计划状态(周)-->
        <template v-slot:weekSlots="{ column, record }">
          <!--输入框显示-->
          <span v-if="showSlotRow === record.id && record[column.dataIndex] != '3'">
            <a-select v-model:value="record[column.dataIndex]" :options="sopPlanstateArray" placeholder="请选择" style="width: 95px;" @change="function(){record.isEdit = true}"></a-select>
          </span>
          <!--输入框显示-->
          <span v-else-if="!record[column.dataIndex] || 0 == record[column.dataIndex]"></span>
          <!--文字显示-->
          <div v-else>
            <span>
              <a @click="writeDiagnostics(record,column.dataIndex)" style="color:#000000a6">{{ planName(record[column.dataIndex]) }}</a>
            </span>
          </div>
        </template>
      </BasicTable>
      <!-- 表单区域 -->
      <DiagnosticPostModal ref="registerModal" @success="handleSuccess"></DiagnosticPostModal>
      <JModal title="批量添加(最大添加20行)" :width="400" :maxHeight="300" v-model:open="visible" @ok="handleOk">
        <div style="height: 100px; padding: 30px 10px 0">
          <a-input-number v-model:value="batchNum" style="width: 100%" max="20" min="1" placeholder="请输入添加数量" />
        </div>
      </JModal>
    </div>
  </a-spin>
</template>

<script lang="ts" name="sop-sopStatisticData" setup>
  import { ref, reactive, onMounted } from 'vue';
  import { BasicTable } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { columns } from './SopStatisticData.data';
  import { list, getWeek, sopSubmit, deleteOne, getExportUrl, getImportUrl } from './SopStatisticData.api';
  import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import KtDict from '/@/components/KtDict/KtDict.vue';
  import dayjs from 'dayjs';
  import { useMessage } from '/@/hooks/web/useMessage';
  import DiagnosticPostModal from './components/diagnosticPostModal.vue';
  import { dicList } from '/@/views/dataCalculation/mdm/dict/dict.api';
  import { getDictItems } from '/@/api/common/api';
  import { cloneDeep } from 'lodash-es';
  import { getRowSpan } from '/@/utils/common/tableUtils';
  import { useUserStore } from '/@/store/modules/user';
  import JModal from '/@/components/Modal/src/JModal/JModal.vue';

  // 用户信息
  const userStore = useUserStore();
  
  const { createMessage } = useMessage();
  const confirmLoading = ref(false);
  const { t } = useI18n();
  //可编辑的行
  const showSlotRow = ref('');
  //已编辑的行数据
  const editIds = ref({});
  const formRef = ref();
  const queryParam = reactive<any>({deptCode : userStore?.userInfo?.orgCode});
  const registerModal = ref();
  const tableColumns = ref([] as any);
  //诊断频率
  const frequencyArray = ref([])
  const statusArray = ref([])
  const checkArray = ref([])
  //用户集合
  const userArray = ref([]);
  //月份集合
  const monthItem = ref([]);
  //年份
  const yearNow = ref("");
  const year = ref("");
  //批量添加弹出
  const visible = ref<boolean>(false);
  //需要批量添加的行数
  const batchNum = ref(1);
  const baseData = ref();
  //注册table数据
  const { tableContext, onExportXls, onImportXls } = useListPage({
    tableProps: {
      title: 'SOP-诊断计划',
      api: list,
      canResize:true,
      useSearchForm: false,
      showActionColumn: false,
      scroll: { x: 'max-content' },
      actionColumn: {
        width: 120,
        fixed: 'right',
      },
      afterFetch: (records) => {
        //获取合并行数
        let newRecords = getRowSpan(records, 'sopId');
        newRecords.forEach(function (value) {
          //解析周的结果数据
          let obj = value.resultData ? JSON.parse(value.resultData) : null;
          Object.assign(value, obj);
        })
        return newRecords;
      },
      beforeFetch: async (params) => {
        params.column = 'sopId';
        params.order = 'asc';
        return Object.assign(params, queryParam);
      },
    },
    exportConfig: {
      name: "SOP-诊断计划",
      url: getExportUrl,
      params: queryParam,
    },
    importConfig: {
      url: getImportUrl,
      success: handleSuccess
    }
  });
  const [registerTable, { reload, setColumns,getDataSource }, { selectedRowKeys }] = tableContext;
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
  //计划状态
  let sopPlanstateArray = ref([
    { value: 0, label: '请选择' },
    { value: 1, label: '计划' },
    { value: 4, label: '延迟' },
    { value: 5, label: '取消' },
    { value: 7, label: '未通过' },
  ]);

  /**
   * 成功回调
   */
  function handleSuccess() {
    (selectedRowKeys.value = []) && reload();
  }
   
  /**
   * 查询
   */
  function searchQuery() {
    if(queryParam.months && queryParam.months.length == 0){
      createMessage.warning("请选择月份");
      return false;
    }
    restEdit();
    getWeekData();
    reload();
  }
  //获取对象名称
  function getValueByKey(array,key){
    let result;
    array.forEach((obj) =>{
      if(obj["value"] === key){
        result = obj["label"]
      }
    });
    return result;
  }

  //获取计划名称
  function planName(value){
    if(value == '3'){
      return "已完成"
    }
    return getValueByKey(sopPlanstateArray.value,value)
  }
  /**
   * 设置参数
   */
  function setParameter(){
    let date = new Date();
    queryParam.year = dayjs(date).format('YYYY');
    const currentMonth = date.getMonth()+1;
    let currentMonthStr = currentMonth >= 10 ? currentMonth : '0'+currentMonth;
    queryParam.months = [queryParam.year+"-"+currentMonthStr];
    yearNow.value = queryParam.year;
    year.value= queryParam.year;
    getSelectMounth(queryParam.year);
  }
  
  /**
   * 获取月份选项
   */
  function getSelectMounth(curYearVal) {
    if(yearNow.value != curYearVal){
      queryParam.months=[];
      queryParam.months = [curYearVal+"-01"];
    }
    
    const curYear = [];
    for (let i = 1; i < 13; i++) {
      let val = i < 10 ? `0${i}` : i
      if (curYearVal) {
        curYear.push({ 'value': `${curYearVal}-${val}`, 'text': `${i}月` })
      } else {
        curYear.push({ 'value': `${val}`, 'text': `${val}` })
      }
    }
    yearNow.value = curYear
    monthItem.value = curYear
  }
  
  /**
   * 筛选选项
   */
  function filterOption(input: string, option: any) {
    return option.componentOptions.children[0].text.toLowerCase().indexOf(input.toLowerCase()) >= 0;
  }
  //获取周
  async function getWeekData(){
    console.log('获取周数据，参数:', {year: queryParam.year, months: queryParam.months});
    await getWeek({year: queryParam.year, months: queryParam.months}).then((res) => {
      console.log('周数据返回:', res);
      if (res) {
        tableColumns.value = cloneDeep(columns);
        // 修改父列标题为当前年份
        tableColumns.value[0].title = queryParam.year+'年度诊断计划';
        
        // 清空现有子列，保留固定列和非周列
        let reservedColumns = tableColumns.value[0].children.filter(col => col.fixed || !col.dataIndex || !col.dataIndex.includes('-'));
        tableColumns.value[0].children = reservedColumns;

        
        // 添加周列作为子列
        res.forEach((item) => {
          let obj = {
            title: '第'+item.weekNumber+'周',
            align: "center",
            width: 120,
            dataIndex: item.weekRangeStr,
            ellipsis: false,
            slots: { customRender: 'weekSlots'},
            customCell: function(record){
              let style = {}
              if (record[item.weekRangeStr] === 1) {
                   style["backgroundColor"] = 'yellow'
              } else if (record[item.weekRangeStr] === 2) {
                   style["backgroundColor"] = 'yellow'
              } else if (record[item.weekRangeStr] === 6 || record[item.weekRangeStr] == 4) {
                   style["backgroundColor"] = '#FE0000'
              } else if (record[item.weekRangeStr] === 5) {
                   style["backgroundColor"] = '#9fa09f'
              } else if (record[item.weekRangeStr] === 3) {
                   style["backgroundColor"] = '#00FF00'
              } else {
                   style["backgroundColor"] = ''
              }
              return {style};
            }
          }
          tableColumns.value[0].children.push(obj);
        });
        //设置表头
        setColumns(tableColumns.value);
      }
    })
    .finally(() => {
      confirmLoading.value = false;
    });
  }
  //深度拷贝
  const deepCloneByJSON = (obj) => {
    return JSON.parse(JSON.stringify(obj));
  }

  /**
   * 获取下拉选项显示文本
   */
  function getOptionText(value, type){
    let optionArr = [] as any;
    switch (type) {
      case 'user':
        optionArr = userArray.value;
        break;
      case 'MES3_SOP_FREQUENCY':
        optionArr = frequencyArray.value;
        break;
      case 'MES3_SOP_OP_STATUS':
        optionArr = statusArray.value;
        break;
      case 'MES3_SOP_CHECK_ALL':
        optionArr = checkArray.value;
        break;
    }
    const findObj = optionArr.find(item => item.value == value);
    return findObj ? findObj.label : '';
  }

  /**
   * 提交数据
   */
   async function submitForm(newRecords) {
    let dataSource = newRecords ?? getDataSource();
    if(null == newRecords){
      dataSource = getDataSource().filter(item => editIds.value.hasOwnProperty(item.id));
    } else {
      dataSource = newRecords;
    }
    if(dataSource && dataSource.length == 0){
      createMessage.warning("保存记录为空");
      return false;
    }
    confirmLoading.value = true;
    await sopSubmit(dataSource)
      .then((res) => {
        if (res.success) {
          createMessage.success(res.message);
          restEdit()
          reload()
        } else {
          createMessage.warning(res.message);
        }
      })
      .finally(() => {
        confirmLoading.value = false;
      });
  }

  //周点击出现诊断表
  function writeDiagnostics(record, week){
    record.week = week
    record.year = queryParam.year
    registerModal.value.disableSubmit = false;
    registerModal.value.edit(record);

  }

  /**
   * 批量添加
   */
  function addBatchSourceData(record){
    visible.value = true;
    batchNum.value = 1;
    baseData.value = record;
  }
  //批量添加确认
  function handleOk(){
    visible.value = false;
    addSourceData(baseData.value, batchNum.value)
  }

  //添加
  async function addSourceData(record, num){
    try{
      // let dataSource = cloneDeep(getDataSource());
      // if(record.rowSpan != 0){
      //   record.rowSpan++;
      // } else {
      //   dataSource.find(item => item.sopId == record.sopId).rowSpan++;
      // }

      let record_new = deepCloneByJSON(record);
      //新增一行，周数上所有记录全部为空
      for (let key in record_new) {
          if(key.includes("-")){
            record_new[key] = 0;
          }
      }
      let addArr = [] as any;
      for(let i = 0; i < num; i++){
        addArr.push({
          ...record_new, 
          id: null, 
          byCheckUserId: '', 
          employeeState: '1',
          officeFileState: '0',
          sceneCheck: '1',
          isDetail: '',
          createTime: '',
          rowSpan: 0,
        });
      }
      submitForm(addArr)
    } catch(e) {
    }
  }

  //删除
  async function reduceRecord(record, index){
    if(record && record['id']){
      await deleteOne({id: record.id}, handleSuccess);
    }else{
      let dataSource = getDataSource();  
      dataSource.splice(index, 1)
    }
  }

  //表格双击事件
  function rowDbClick(record){
    showSlotRow.value = record.id;
    editIds.value[record.id] = true;
  }

  function restEdit(){
    showSlotRow.value = '';
    editIds.value = {};
  }

  //获取字典
  async function getDict(code,obj){
    if(code === "userAll"){
      obj.value = await getDictItems("sys_user,realname,id");
    }else{
      let res = await dicList({code});
      if(res){
        let newArr = res.map(item => ({
            value: item.itemVal,
            label: item.itemKey,
            disabled: item.delFlag
        }));
        obj.value = newArr;
      }
    }
  }
  onMounted(async () => {
    //获取字典
    getDict("MES3_SOP_FREQUENCY", frequencyArray);
    getDict("MES3_SOP_OP_STATUS", statusArray);
    getDict("MES3_SOP_CHECK_ALL", checkArray);
    getDict("userAll",userArray);
    setParameter();
    getWeekData();

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

  .check-user{
    display: flex; 
    align-items: center;

    p {
      margin-top: 0;
      margin-bottom: 0em;
    }
  }
</style>
