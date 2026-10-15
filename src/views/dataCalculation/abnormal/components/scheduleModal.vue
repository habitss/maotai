<template>
  <BasicModal title="过程记录" v-bind="$attrs" @register="registerModal" destroyOnClose width="80vw" :minHeight="300"
    @ok="handleOk">
    <div class="p-4 pt-0">
      <div class="flex justify-between mt-2 mb-2" >
        <div>
          <!-- {{ baseData.fieldData.calculateSignalId }} ==== -->
          <div class="title">考核标准</div>
          <span>PI计算时间：{{ baseData.piTime }} </span>
          <span style="padding-left: 20px;">实际值：{{ baseData.actualValue }}</span>
          <div>报警描述：{{ baseData.maintenanceDescribe }}</div>
        </div>
        <a-button type="primary" @click="viewAlgorithm" :disabled="algorithmBtnDisabled">查看计算公式</a-button>
      </div>

      <div class="top-table">
        <BasicTable size="small" bordered rowKey="id"
        :dataSource="baseData.messageStatisticsVo" :columns="topColumns" :pagination="false" :showIndexColumn="false">
        </BasicTable>
      </div>
      <div class="mt-2">
        <div class="title mt-5 mb-2">考核标准散点图</div>
        <div class="flex border p-3" style="background-color: #F4F5F5;">
          <div class="w-3/4 mr-10 border" style="background-color: #fff;">
            <CheckStandardChart :chartData="baseData.chartData"/>
          </div>
          <div class="w-1/4">
            <div>
              <a-checkbox-group>
                <a-checkbox v-model:checked="introduceStandard" @change="e => checkboxChange(e)">是否引入红黄区进行变色</a-checkbox>
              </a-checkbox-group>
            </div>
            <div class="title mt-1 mb-2" style="text-align: center;">
                <a-select v-model:value="selectedKey" class="w-100%" style="border: 1px solid transparent; "  @change="handleChange">
                  <a-select-option
                    v-for="(value, key) in baseData.processAnalysisList"
                    :key="value"
                    :value="key"
                  >
                    {{ value }}
                  </a-select-option>
                </a-select>
            </div>
            <BasicTable style="padding: 0;" :scroll="{ y: 400 }" size="small" :showHeader="false" rowKey="id" bordered :loading="baseData.loading" :dataSource="baseData.chartData" :columns="chartColumns"
              :showIndexColumn="false" :pagination="false">
            </BasicTable>
            <div style="margin-top: 20px;">
              <a-button color="success" :disabled="baseData.dataPage <= 1" @click="handlePrevPage()">上一页</a-button>
              <a-button color="success" :disabled="baseData.chartData.length < baseData.dataOffSet" style="margin-left: 10px;" @click="handleNextPage()">下一页</a-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </BasicModal>
  <!--算法配置-列表页面-->
  <PiAlgorithmDrawer @register="register" v-if="drawerVisible"  @close="drawerVisible = false"/>
</template>
  
<script lang="ts" setup>
import { BasicTable } from '/@/components/Table';
import { ref, reactive, onMounted,provide, unref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { defHttp } from '/@/utils/http/axios';
import CheckStandardChart from './checkStandardChart.vue'
import { queryTriggerTime, queryProcessAnalysis } from '../abnormal.api'
import { useMessage } from '@/hooks/web/useMessage';
import { topColumns, chartColumns } from '../abnormal.data'
import PiAlgorithmDrawer from '/@/views/dataCalculation/pi/customizeTable/components/PiAlgorithmDrawer.vue'
import { useDrawer } from '/@/components/Drawer';
import { cloneDeep } from 'lodash-es';
const { createMessage } = useMessage();
const drawerVisible = ref(false);
const algorithmBtnDisabled = ref(true);
const baseData = ref({
  piTime: '',
  actualValue: '',
  maintenanceDescribe: '',
  messageStatisticsVo: [] as any,
  processAnalysisList: [] as any,
  processAnalysisData: {} as any,
  chartData: [] as any,
  fieldData: [] as any,
  dataPage:1,
  dataOffSet:1000,
  dataMap : {} as any,
  loading:true,
  algorithmTypeCode: '',
  algorithmType: '',
  calculateSignalId: '',
})
const selectedKey = ref(); 

const [registerModal ,{ setModalProps, closeModal }] = useModalInner(async (data) => {
  reset();
  drawerVisible.value = true;
  baseData.value.dataMap = data;
  getfieldData(data);
  setModalProps({ confirmLoading: false, showCancelBtn: false, showOkBtn: false });
  getPiStandardData(data);
  getTriggerTime(data);
  getProcessAnalysis(data);
});
//注册算法配置汇总抽屉
const [register, { openDrawer }] = useDrawer();
// 查看计算公式
const props = defineProps({
  departObject: { type: Object,default:()=>({}) },
});
const departObject = ref<Object>({});
provide('departObject', departObject);
//const hasReopen = ref<boolean>(false);

//是否引入红黄区进行变色
const introduceStandard = ref<boolean>(false);


async function handleOk(v) {
  reset();
  closeModal();
}

// 获取考核标准表格内数据
async function getPiStandardData(data) {
  try {
    const res = await defHttp.get({ url: '/pi/piAbnormalAnalysis/queryPiStandardData', params: { tableCode: data.params.tableCode, dataId: data.params.dataId, fieldCode: data.params.fieldCode, dataValue: data.params.actualValue ? data.params.actualValue : 0 } });
    baseData.value.actualValue = data.params.actualValue
    
    if(res && res.standardMaintenance){
      res.standardMaintenance.actualValue = data.params.actualValue
      baseData.value.maintenanceDescribe = res.maintenanceDescribe
      baseData.value.messageStatisticsVo = [res.standardMaintenance]
    }
  } catch (error) {
    createMessage.error('网络错误');
  }
}

// 获取PI计算时间
async function getTriggerTime(data) {
  const res = await queryTriggerTime({ tableCode: data.params.tableCode, dataId: data.params.dataId, fieldCode: data.params.fieldCode });
  baseData.value.piTime = res.time
  //算法类型
  baseData.value.fieldData.algorithmType = res.type
  baseData.value.fieldData.calculateSignalId = res.calculateSignalId
  algorithmBtnDisabled.value = false;
}

// 获取标准散点图内数据
async function getProcessAnalysis(data) {
  baseData.value.loading = true;
  const res = await queryProcessAnalysis({ tableCode: data.params.tableCode, dataId: data.params.dataId, fieldCode: data.params.fieldCode,dataPage:baseData.value.dataPage,dataOffSet:baseData.value.dataOffSet });
  selectedKey.value = Object.keys(res)[0]
  // 获取下拉框数据
  baseData.value.processAnalysisList = Object.keys(res)
  baseData.value.processAnalysisData = res
  if(null != res[selectedKey.value]){
    baseData.value.chartData = res[selectedKey.value].map((item) => ({
      time: formatTime(item.time),
      value: changeValue(item.value),
      standar: baseData.value.messageStatisticsVo,
      introduceStandard: introduceStandard.value,
    }));
  }
  baseData.value.loading = false;
}

//获取字段的基本信息
async function getfieldData(data) {
  const res = await defHttp.get({ url: '/pi/piAbnormalAnalysis/getCustomizeTableField', params: { tableCode: data.params.tableCode, fieldCode: data.params.fieldCode } });
  baseData.value.fieldData = res;
}


// 格式化时间
const formatTime = (time) => {
  const date = new Date(time);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`;
};

// 更新表格和图表
const handleChange = (key, value) => {
  baseData.value.chartData=[]
  baseData.value.chartData = baseData.value.processAnalysisData[value.key].map((item) => ({
    time: formatTime(item.time),
    value: changeValue(item.value),
    standar:baseData.value.messageStatisticsVo,
    introduceStandard: introduceStandard.value,
  }));
  selectedKey.value = value.key;
};

//重置
function reset() {
  baseData.value.piTime = '';
  baseData.value.maintenanceDescribe = '';
  baseData.value.messageStatisticsVo = [];
  baseData.value.chartData = [];
  introduceStandard.value = false;
}

//查看计算公式
async function viewAlgorithm(){
  drawerVisible.value = true;
  //传递参数
  departObject.value = baseData.value.fieldData;
  let tableData = {
    id : departObject.value.tableId,
    lineCode: departObject.value.lineCode,
    deptCode: departObject.value.deptCode,
    factoryCode: departObject.value.factoryCode,
    section: '',
    name: departObject.value.name,
    tableName: departObject.value.tableName,
  }
  let record = cloneDeep(departObject.value)
  //延迟执行
  setTimeout(() => {
    openDrawer(true, {
      record,
      tableData: tableData
    });
  }, 100);
}

//根据字段配置的取值方式，对数据进行处理
function changeValue(value){
  if(introduceStandard.value){
    //保留几位小数
    const keepDecimals = baseData.value.fieldData.keepDecimals;
    //取值方式 1向上取整 2向下取整 3四舍五入 4截取
    const takingValue = baseData.value.fieldData.takingValue;
    switch (takingValue) {
      case "1":
        value = Math.ceil(value);
        break;
      case "2":
        value = Math.floor(value);
        break;
      case "3":
        value = Number(value.toFixed(keepDecimals));
        break;
      case "4":
        const multiplier = Math.pow(10, keepDecimals);
        const truncatedNumber = Math.trunc(value * multiplier) / multiplier;
        // 这里 toFixed 只是用来格式化输出，不会改变数值
        value = Number(truncatedNumber.toFixed(keepDecimals));
        break;
    }
    value = Number(value.toFixed(keepDecimals));
  }
  return value;
}

function checkboxChange(e){
  if(e.target.checked){
    introduceStandard.value = true;
  }else{
    introduceStandard.value = false;
  }
  const selectObj = {"key":selectedKey.value};
  handleChange(null,selectObj);
}

//上一页触发事件
function handlePrevPage(){
  baseData.value.dataPage = baseData.value.dataPage - 1;
  if(baseData.value.dataPage < 1){
    baseData.value.dataPage = 1;
  }
  getProcessAnalysis(baseData.value.dataMap);
}

//下一页触发事件
function handleNextPage(){
  baseData.value.dataPage = baseData.value.dataPage + 1;
  getProcessAnalysis(baseData.value.dataMap);
}

</script>
<style lang="less" scoped>
.title {
  font-weight: bold;
}
</style>