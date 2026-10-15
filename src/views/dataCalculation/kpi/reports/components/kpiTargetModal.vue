<template>
  <j-modal :title="title" :width="'90%'" :visible="visible" @ok="handleOk" :okButtonProps="{ style: { display: 'none' } }" @cancel="handleCancel" cancelText="关闭">
    <!--查询条件-->
      <a-form ref="formRef" layout="inline" :model="queryParam" :label-col="labelCol" :wrapper-col="wrapperCol" style="margin-top: 10px;justify-content: center;">
        <a-row :gutter="24" style="width: 100%;">
          <a-col :span="3">
             <!-- kpi -->
            <a-form-item label="KPI" v-if="tabPosition == '1' || tabPosition == '2'|| tabPosition == '5' || tabPosition == '6'">
              {{ kpiParams.kpiName }}
            </a-form-item>
          </a-col>
          <a-col :span="4">
            <!--年份-->
            <a-form-item label="年月" v-if="tabPosition == '1' || tabPosition == '3'  || tabPosition == '4' ">
              <a-date-picker v-model:value="queryParam.month" picker="month" valueFormat="YYYY-MM" format="YYYY-MM" style="width:125px;max-width: 200px;"/>
            </a-form-item>
            <!-- 日期 -->
            <a-form-item label="日期" v-if="tabPosition == '6'">
              <a-select v-model:value="queryParam.yearMonthArr" style="width:125px;max-width: 200px;" mode="multiple"
                @change="dayChose">
                <a-select-option :value="item" v-for="(item, index) in daysList" :key="index">
                    {{ item }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>

          <a-col :lg="17" style="text-align: right;">
              <a-button type="primary" @click="searchQuery" v-if="tabPosition != '2' && tabPosition != '5'">
                  查询
              </a-button>
              <!-- <a-button :style="{ marginLeft: '8px' }" @click="handleReset" v-if="tabPosition != '2' && tabPosition != '5'">
                  重置
              </a-button> -->
          </a-col>
        </a-row>
       
      </a-form>
    <!--Tab-->
    <a-card title="" :bordered="false" class="mycard_tab" style="display: flex;justify-content: center;">
      <a-radio-group v-model:value="tabPosition" style="margin: 8px" :centered="true" button-style="solid" @change="(e) => radioChange(e.target.value)">
        <a-radio-button value="1">差距分析</a-radio-button>
        <a-radio-button value="2">KPI树</a-radio-button>
        <a-radio-button value="3">柏拉图</a-radio-button>
        <a-radio-button value="4">5Why</a-radio-button>
        <a-radio-button value="5">目标值调整</a-radio-button>
        <a-radio-button value="6">基础数据</a-radio-button>
      </a-radio-group>
    </a-card>
    <!--内容-->
    <template v-if="tabPosition === '1'">
      <GapAnalysis ref="gapAnalysisRef" />
    </template>
    <template v-if="tabPosition === '2'">
      <!-- KPI树 -->
      <KpiTree ref="kpiTreeRef" />
    </template>
    <template v-if="tabPosition === '3'">
      <!--柏拉图-->
      <OneChart ref="oneChartRef" />
    </template>
    <template v-if="tabPosition === '4'">
      <FiveWhyList ref="fiveWhyRef" />
    </template>
    <template v-if="tabPosition === '5'">
      <!--目标值调整-->
      <TargetAdjust ref="targetAdjustRef" />
    </template>
    <template v-if="tabPosition === '6'">
      <!--基础数据-->
      <BaseData ref="baseDataRef"/>
    </template>
   
  </j-modal>
</template>

<script lang="ts" setup>
  import { ref, nextTick, reactive,defineExpose, unref } from 'vue';
  import JModal from '/@/components/Modal/src/JModal/JModal.vue';
  import GapAnalysis from '/@/views/dataCalculation/gapAnalysis/gapAnalysis.vue';
  import FiveWhyList from '/@/views/dataCalculation/fiveWhy/components/fiveWhyList.vue';
  import KpiTree from './kpiTree.vue';
  import OneChart from './oneChart.vue';
  import TargetAdjust from './targetAdjust.vue';
  import BaseData from './baseData.vue';
  import dayjs from "dayjs";
  
  //差距分析组件ref
  const gapAnalysisRef = ref();
  //5w组件ref
  const fiveWhyRef = ref();
  //KPI树组件
  const kpiTreeRef = ref();
  //柏拉图
  const oneChartRef = ref();
  //目标值调整
  const targetAdjustRef = ref();
  //基础数据
  const baseDataRef = ref();

  //日期
  const daysList = ref([]);
  //查询条件
  // const queryParam = ref({})
  const queryParam = reactive<any>({});

  const title = ref<string>('');
  const width = ref<number>(1200);
  const visible = ref<boolean>(false);
  const disableSubmit = ref<boolean>(false);
  const registerForm = ref();
  const tabPosition = ref('1');
  const emit = defineEmits(['register', 'success']);
  const kpiRowData = ref({});
  const kpiParams = ref({});
  const labelCol = reactive({
    span: 10
  });
  const wrapperCol = reactive({
    span: 14
  });

  /**
   * 显示分析
   */
  function showModal(type,rowData,params) {
    kpiRowData.value = rowData.value;
    kpiParams.value = params.value;
    tabPosition.value = '1'
    radioChange(tabPosition.value);
    visible.value = true;
    queryParam.month = kpiParams.value.yearMonth;

    var schedule = getDaysArrayByMonth();
    daysList.value = schedule.map(function (item) {
      return item.format("YYYY-MM-DD")
    });
  }
  
  /**
   * 修改标题/打开指定页面
   */
  function radioChange(value){
    let obj = {
      type: 'kpi',
      kpiRowData: unref(kpiRowData.value),
      kpiParams: unref(kpiParams.value),
    }
    if(value === '1'){
      title.value = '差距分析';
      nextTick(() => {
        gapAnalysisRef.value.init(obj);
      });
    }else if(value === '2'){
      title.value = 'KPI树';
      nextTick(() => {
        kpiTreeRef.value.init(obj);
      });
    }else if(value === '3'){
      title.value = '柏拉图';
      nextTick(() => {
        oneChartRef.value.init(obj);
      });
    }else if(value === '4'){
      title.value = '5Why';
      nextTick(() => {
        fiveWhyRef.value.init(obj);
      });
    }else if(value === '5'){
      title.value = '目标值调整';
      nextTick(() => {
        targetAdjustRef.value.init(obj);
      });
    }else if(value === '6'){
      title.value = '基础数据';
      nextTick(() => {
        obj.queryParam = unref(queryParam)
        baseDataRef.value.init(obj);
      });
    }    
  }


  //查询
  function searchQuery(){
    let obj = {
      type: 'kpi',
      kpiRowData: unref(kpiRowData.value),
      kpiParams: unref(kpiParams.value),
      queryParam: unref(queryParam),
    }
    switch (tabPosition.value) {
      case '1':
        //差距分析
        nextTick(() => {
          gapAnalysisRef.value.init(obj);
        });
        break;
      case '2':
        //KPI树
        break;
      case '3':
        //柏拉图
        nextTick(() => {
          oneChartRef.value.init(obj);
        });
        break;
      case '4':
        //5Why
        break;
      case '5':
        //目标值调整 
        break;
      case '6':
        debugger
        //基础数据
        nextTick(() => {
          baseDataRef.value.init(obj);
        });
        break;
      default:
        break;
    }
  }


  /**
   * 确定按钮点击事件
   */
  function handleOk() {
    registerForm.value.submitForm();
  }

  /**
   * form保存回调事件
   */
  function submitCallback() {
    handleCancel();
    emit('success');
  }

  /**
   * 取消按钮回调事件
   */
  function handleCancel() {
    visible.value = false;
  }

  /**
   * 获取当前月份包含的天数
   */
  function getDaysArrayByMonth() {
    //获取当前月份包含的天数
    let currentMonth = dayjs().month() + 1
    var daysInMonth = dayjs().daysInMonth();
    let monthObj = {}
    var arrDays = [];
    //循环获取月份里的日期
    while (daysInMonth) {
        var current = dayjs().date(daysInMonth);
        arrDays.push(current);
        daysInMonth--;
    }
    for (let index = 1; index < currentMonth; index++) {
        monthObj[currentMonth - index] = dayjs().subtract(index, 'month').daysInMonth();
    }
    Object.keys(monthObj).reverse().forEach(key => {
        while (monthObj[key]) {
            var current = dayjs().month(key - 1).date(monthObj[key]);
            arrDays.push(current);
            monthObj[key]--;
        }
    })
    return arrDays;
  }
  defineExpose({
    showModal,
    disableSubmit,
  });
</script>

<style lang="less">
  /**隐藏样式-modal确定按钮 */
  .jee-hidden {
    display: none !important;
  }
  :deep(.ant-card .ant-card-body) {
      padding: 10px;
      border-radius: 0 0 4px 4px;
  }
  // :deep(.mycard_tab .ant-card-body) {
  //     padding: 0;
  // }

  // :deep(.myCard .ant-form-item-control) {
  //     line-height: 1;
  // }

  // :deep(.myCard .ant-table-thead th) {
  //     /* display: none; */
  //     text-align: center;
  // }

  // :deep(.myCard .ant-form-item) {
  //     margin-bottom: 0;
  // }

  // :deep(.myCard .ant-table-tbody) {
  //     /* tr:first-child {
  //         background: #EBF1FB;
  //     } */
  //     text-align: center;
  // }
</style>
<style lang="less" scoped></style>
