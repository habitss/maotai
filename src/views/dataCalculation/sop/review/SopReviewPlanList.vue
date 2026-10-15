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
                  @change="handleDeptChange"
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
                <a-input v-model:value="queryParam.sopDesc" placeholder="请输入SOP名称"></a-input>
              </a-form-item>
            </a-col>

             <!--SOP编号-->
            <a-col :span="6">
              <a-form-item label="SOP编号">
                <a-input v-model:value="queryParam.sopNumber" placeholder="SOP编号"></a-input>
              </a-form-item>
            </a-col>

             <!--是否关键SOP-->
            <a-col :span="6">
              <a-form-item label="是否关键SOP">
                <a-select v-model:value="queryParam.sopState" :options="cruxArray" placeholder="请选择是否关键SOP" allowClear></a-select>
              </a-form-item>
            </a-col>

             <!--回顾频次-->
            <a-col :span="6">
              <a-form-item label="回顾频次">
                <a-select v-model:value="queryParam.frequency" :options="frequencyArray" placeholder="请选择回顾频次" allowClear></a-select>
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
          <a-button type="primary" v-auth="'sop:mes3_sop_review_plan:add'"  @click="submitForm" preIcon="ant-design:plus-outlined">保存</a-button>
          <a-button  type="primary" v-auth="'sop:mes3_sop_review_plan:exportXls'" preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
          <j-upload-button  type="primary" v-auth="'sop:mes3_sop_review_plan:importExcel'"  preIcon="ant-design:import-outlined" @click="onImportXls">导入</j-upload-button>
        </template>
        
        <!--关键SOP-->
        <template v-slot:sopStateSlots="{ column, record }">
          <span v-if="showSlotRow == record.id">
            <a-select v-model:value="record[column.dataIndex]" :options="cruxArray" placeholder="请选择是否关键SOP" allowClear></a-select>
          </span>
          <span v-else>{{ getOptionText(record[column.dataIndex], 'MES3_SOP_CRUX') }}</span>
        </template>
         <!--回顾频次-->
         <template v-slot:frequencySlots="{ column, record }">
          <span v-if="showSlotRow == record.id">
            <a-select v-model:value="record[column.dataIndex]" :options="frequencyArray" placeholder="请选择回顾频次" allowClear></a-select>
          </span>
          <span v-else>{{ getOptionText(record[column.dataIndex], 'MES3_SOP_FREQUENCY') }}</span>
        </template>
         <!--回顾间隔-->
         <template v-slot:reviewIntervalSlots="{ column, record }">
          <span v-if="showSlotRow == record.id">
            <a-select v-model:value="record[column.dataIndex]" :options="frequencyArray" placeholder="请选择回顾间隔" allowClear></a-select>
          </span>
          <span v-else>{{ getOptionText(record[column.dataIndex], 'MES3_SOP_FREQUENCY') }}</span>
        </template>
         <!--回顾负责人-->
         <template v-slot:headUserIdSlots="{ column, record }">
          <span v-if="showSlotRow == record.id">
            <a-select v-model:value="record[column.dataIndex]" :options="userArray" placeholder="请选择回顾负责人" allowClear show-search option-filter-prop="label"></a-select>
          </span>
          <span v-else>{{ getOptionText(record[column.dataIndex], 'user') }}</span>
        </template>

         <!--回顾责任人-->
         <template v-slot:responsibleUserIdSlots="{ column, record }">
          <span v-if="showSlotRow == record.id">
            <a-select v-model:value="record[column.dataIndex]" :options="userArray" placeholder="请选择回顾责任人" allowClear show-search option-filter-prop="label"></a-select>
          </span>
          <span v-else>{{ getOptionText(record[column.dataIndex], 'user') }}</span>
        </template>
        <!--回顾咨询人-->
        <template v-slot:seekUserIdSlots="{ column, record }">
          <span v-if="showSlotRow == record.id">
            <!-- <KtUserScrollSelect v-model:value="record[column.dataIndex]" placeholder="请选择回顾咨询人"></KtUserScrollSelect> -->
            <a-select v-model:value="record[column.dataIndex]" :options="userArray" placeholder="请选择回顾咨询人" allowClear show-search option-filter-prop="label"></a-select>
          </span>
          <span v-else>{{ getOptionText(record[column.dataIndex], 'user') }}</span>
        </template>
        <!--回顾通知人-->
        <template v-slot:noticeUserIdSlots="{ column, record }">
          <span v-if="showSlotRow == record.id">
            <a-input v-model:value="record[column.dataIndex]" placeholder="请输入"  allow-clear style="width:100%"></a-input>
          </span>
        </template>
        <!--周-->
        <template v-slot:weekSlots="{ column, record }">
          <label v-if="showSlotRow != record.id && record[column.dataIndex] != 0" style="margin-right: 6px;">{{ getOptionText(record[column.dataIndex], 'SOP_PLAN_STATE') }}</label>
          <a-select v-if="record[column.dataIndex] != '3' && showSlotRow == record.id" v-model:value="record[column.dataIndex]" :options="sopPlanstateArray" placeholder="请选择" style="width: 100px;"></a-select>
          <span v-if="record[column.dataIndex] != '1' && record[column.dataIndex] != '3'">&emsp;&emsp;</span>
          <span v-if="record[column.dataIndex] == '1'">
           <a @click="fileData(column.dataIndex,record, 'add')">上传</a>
          </span>
          <span v-if="record[column.dataIndex] == '3'">
            <a @click="fileData(column.dataIndex,record, 'show')" style="color:#000000a6">已完成</a>
          </span>
        </template>

        <template v-slot:bodyCell=""></template>
      </BasicTable>
      <!-- 表单区域 -->
      <SopUrlReviewModal ref="registerModal" @success="handleSuccess"></SopUrlReviewModal>
    </div>
  </a-spin>
</template>

<script lang="ts" name="sop-sopReviewPlan" setup>
  import { ref, reactive, onMounted } from 'vue';
  import { BasicTable } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { columns } from './SopReviewPlan.data';
  import { list, saveOrUpdate, getExportUrl, getImportUrl } from './SopReviewPlan.api';
  import { getWeek } from '/@/views/dataCalculation/sop/plan/SopStatisticData.api';
  import { getDictItems } from '/@/api/common/api';
  import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import SopUrlReviewModal from './components/sopUrlReviewModal.vue';
  import dayjs from 'dayjs';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { dicList } from '/@/views/dataCalculation/mdm/dict/dict.api';
  import { useUserStore } from '/@/store/modules/user';

  // 用户信息
  const userStore = useUserStore();
  const { createMessage } = useMessage();
  const { t } = useI18n();
  //可编辑的行
  const showSlotRow = ref('');
  //已编辑的行数据
  const editIds = ref({});
  const formRef = ref();
  const queryParam = reactive<any>({deptCode : userStore?.userInfo?.orgCode});
  const registerModal = ref();
  const tableColumns = ref([]);
  const confirmLoading = ref(false);
  //是否关键SOP
  const cruxArray = ref([]);
  //请选择回顾频次
  const frequencyArray = ref([]);
  //用户集合
  const userArray = ref([]);
  //月份集合
  const monthItem = ref([]);
  //年份
  const yearNow = ref("");
  const year = ref("");
  //注册table数据
  const { tableContext, onExportXls, onImportXls } = useListPage({
    tableProps: {
      title: 'SOP管理回顾',
      api: list,
      canResize:true,
      useSearchForm: false,
      showActionColumn: false,
      actionColumn: {
        width: 120,
        fixed: 'right',
      },
      afterFetch: (records) => {
        records.forEach(function (value) {
          let resultData = value.resultData;
          let obj = JSON.parse(resultData);
          Object.assign(value, obj);
        })
      },
      beforeFetch: async (params) => {
        getWeekData();
        return Object.assign(params, queryParam);
      },
    },
    exportConfig: {
      name: "SOP管理回顾",
      url: getExportUrl,
      params: queryParam,
    },
    importConfig: {
      url: getImportUrl,
      success: handleSuccess
    }
  });
  const [registerTable, { reload,setColumns, getDataSource }, { selectedRowKeys }] = tableContext;
  const labelCol = reactive({
    xs:8,
    sm:8,
    xl:8,
    xxl:8
  });
  const wrapperCol = reactive({
    xs: 24,
    sm: 20,
  });

  //计划状态
  let sopPlanstateArray = ref([
    { value: 0, label: '请选择' },
    { value: 1, label: '计划' },
    { value: 5, label: '取消' }
  ]);

  /**
   * 提交数据
   */
   async function submitForm() {
    let dataSource = getDataSource().filter(item => editIds.value.hasOwnProperty(item.id));
    
    if(dataSource && dataSource.length == 0){
      createMessage.warning("保存记录为空");
      return false;
    }
    confirmLoading.value = true;

    await saveOrUpdate(dataSource)
      .then((res) => {
        if (res.success) {
          createMessage.success(res.message);
          restEdit();
          reload()
        } else {
          createMessage.warning(res.message);
        }
      })
      .finally(() => {
        confirmLoading.value = false;
      });
  }
  
  /**
   * 成功回调
   */
  function handleSuccess() {
    (selectedRowKeys.value = []) && reload();
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

  //获取周
  async function getWeekData(){
    console.log('获取周数据，参数:', {year: queryParam.year, months: queryParam.months});
    await getWeek({year: queryParam.year, months: queryParam.months}).then((res) => {
      console.log('周数据返回:', res);
      if (res) {
        tableColumns.value = deepCloneByJSON(columns);
        res.forEach((item) => {
          let obj = {
            title: item.weekRangeStr,
            align: 'center',
            children:[{
              title: '第'+item.weekNumber+'周',
              align: "center",
              dataIndex: item.weekRangeStr,
              slots: { customRender: 'weekSlots'},
              customCell: function(record){
                let style = {};
                if (record[item.weekRangeStr] == '1') {
                     style["backgroundColor"] = 'yellow'
                }  else if (record[item.weekRangeStr] == '5') {
                     style["backgroundColor"] = '#9fa09f'
                } else if (record[item.weekRangeStr] == '3') {
                     style["backgroundColor"] = '#00FF00'
                } else {
                     style["backgroundColor"] = ''
                }
                return {style};
              }
            }]
          }
          tableColumns.value.push(obj)
        })
        //设置表头
        setColumns(tableColumns.value);
      }
    })
    .finally(() => {
      confirmLoading.value = false;
    });
  }
  //SOP名称列表
  function fileData(column,record,type){
    record.week = column;
    record.addType = type;
    registerModal.value.disableSubmit = false;
    registerModal.value.edit(record);
  }

  //深度拷贝
  const deepCloneByJSON = (obj) => {
    return JSON.parse(JSON.stringify(obj));
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

  //表格双击事件
  function rowDbClick(record){
    showSlotRow.value = record.id;
    editIds.value[record.id] = true;
  }

  function restEdit(){
    showSlotRow.value = '';
    editIds.value = {};
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
      case 'MES3_SOP_CRUX':
        optionArr = cruxArray.value;
        break;
      case 'SOP_PLAN_STATE':
        optionArr = sopPlanstateArray.value;
        break;
    }
    const findObj = optionArr.find(item => item.value == value);
    return findObj ? findObj.label : '';
  }

  onMounted(() => {
    //获取字典
    getDict("MES3_SOP_CRUX",cruxArray);
    getDict("MES3_SOP_FREQUENCY", frequencyArray);
    getDict("userAll", userArray);
    setParameter();
    getWeekData();
    queryParam.column = "sort";
    queryParam.order = "asc";
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
