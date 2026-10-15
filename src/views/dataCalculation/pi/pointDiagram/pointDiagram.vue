<template>
  <div class="container">
    <!-- 查询区域 -->
    <a-row class="search-part">
      <a-col :span="24">
        <BasicForm @register="registerForm" @submit="handleSubmit">
          <template #standardSlot="{ model, field }">
            <a-input-search v-model:value="model[field]" placeholder="请选择标准" enter-button="选择" readonly
              @search="handleStandSearch" />

          </template>
        </BasicForm>
      </a-col>
      <a-col :span="24" v-if="null != data" >
        <div style="display: flex; justify-content: flex-start;">
          <div><JRangeNumber v-model:value="yRangeNumber"></JRangeNumber></div>
          <div><a-button type="primary" @click="handleYAxis" style="margin: 0 6px;">设置竖向范围</a-button>{{ rangText }}</div>
          <div><a-button type="primary" @click="isEmphasis" style="margin: 0 6px;">是否显示数值</a-button></div>
        </div>
      </a-col>
      <a-col :span="24" class="chartPart">
        <a-empty v-if="showEmpty" style="position: relative; top: 50%;" />
        <div v-else ref="chart" style="height: 100%"></div>
      </a-col>
    </a-row>
  </div>
  <ManageStandardModal @register="registerModal" :key="dayjs().valueOf()" width="70vw"
    @close-select="handleCloseSelect" />
</template>

<script lang="ts" name="actionPlan-summaryIndex" setup>
import { nextTick, ref } from 'vue';
import { useForm, BasicForm } from '/@/components/Form';
import { searchFormSchema } from './pointDiagram.data';
import { useModal } from '/@/components/Modal';
import { list, getPiFieldList } from './pointDiagram.api';
import JRangeNumber from '/@/components/Form/src/jeecg/components/JRangeNumber.vue';
import ManageStandardModal from '/@/views/dataCalculation/pi/standardValue/components/manageModal.vue';
import dayjs from 'dayjs';
import * as echarts from 'echarts';
import { cloneDeep } from 'lodash-es';
//传入参数
const inputParams = ref();
//折线图配置
const lineChartOption = ref();
//折线图实例
const chart = ref();
//y轴范围
const yRangeNumber = ref([]);
//数据
const data = ref();
//pi字段列表
const piFieldList = ref([] as any);
//显示空
const showEmpty = ref(true);
//红黄区文字
const rangText = ref();
//颜色
const colorData = {
  red: '#FE0000',
  yellow: '#FFFF00',
  standard: '#00FF00',
  val: '#5B9BD5'
};
//判断是否显示顶部文字
const isEmphasisFlag = ref(true);

//标准管理modal
const [registerModal, { openModal }] = useModal();
/**
 * BasicForm绑定注册;
 */
const [registerForm, { validate, setFieldsValue, updateSchema }] = useForm({
  schemas: searchFormSchema,
  showAdvancedButton: false,
  showResetButton: false,
  actionColOptions: { span: 2 },
  fieldMapToTime: [['searchTime', ['searchTimeStart', 'searchTimeEnd'], 'YYYY-MM-DD HH:mm:ss']],
  fieldMapToNumber: [],
  baseColProps: { span: 7 },
  labelCol: { style: { width: '100px' } },
});


//页面刷新并且切换数值显示
const isEmphasis = function(){
  isEmphasisFlag.value = !isEmphasisFlag.value;
  handleYAxis();
}
//初始化数据
async function initData(params) {
  inputParams.value = params;
  piFieldList.value = await getPiFieldList({tableName: params.tableName});
  //设置时间范围限制
  await updateSchema([
    {
      //字段必填
      field: 'xFieldName',
      component: 'Select',
      componentProps: {
        options: piFieldList.value,
        fieldNames: {
          label: 'name',
          value: 'fieldName',
        },
        showSearch: true,
        optionFilterProp: 'name',
      },
    },
    {
      //字段必填
      field: 'timeField',
      component: 'Select',
      componentProps: {
        options: piFieldList.value.filter(item => item.type == 6),
        fieldNames: {
          label: 'name',
          value: 'fieldName',
        },
        showSearch: true,
        optionFilterProp: 'name',
      },
    },
  ]);
  await setFieldsValue({
    tableName: params.tableName,
    fieldName: params.fields
  });
}

//标准选择
function handleStandSearch() {
  let record = cloneDeep(inputParams.value);
  record.inputType = 'piChart';
  record.inputSubType = 'point';
  openModal(true, {
    record: record,
    type: 'chart'
  })
}
//标准选择关闭
async function handleCloseSelect(standRecord) {
  //设置标准相关值
  await setFieldsValue({
    standardName: standRecord.map(item => item.standardName).join(', '),
    standardId: standRecord.map(item => item.id).join(', '),
    searchTime: null
  });
}

/**
 * 点击提交按钮的value值
 */
async function handleSubmit() {
  //获取表单数据
  const values = await validate();
  //清空范围
  yRangeNumber.value = [];
  //获取后台数据
  const res = await list(values);
  data.value = res;
  showEmpty.value = false;
  await nextTick(() => {
    initChart(res, values)
  });
}

//设置y轴范围
async function handleYAxis() {
  //复制一分数据，避免影响原数据
  const newData = cloneDeep(data.value);
  //新的y轴数据
  let newArr = [] as any;
  //处理y轴数据
  newData.yAxisArr.forEach(item => {
    //将小于设定的最小值，或者大于设定的最大值，设置为最小值或者最大值用于显示
    if (yRangeNumber.value[0] && item < yRangeNumber.value[0]) {
      item = yRangeNumber.value[0];
    } else if (yRangeNumber.value[1] && item > yRangeNumber.value[1]) {
      item = yRangeNumber.value[1];
    }
    newArr.push(item);
  });
  newData.yAxisArr = newArr;
  initChart(newData, await validate());
}
function initChart(res, values) {
  var piField = piFieldList.value.find(item => item.fieldName == values.xFieldName);
  //折线图配置
  lineChartOption.value = {
    title: {
      text: `${values.standardName}（${dayjs(values.searchTimeStart).format('YYYY年MM月DD日')}-${dayjs(values.searchTimeEnd).format('YYYY年MM月DD日')}）`,
      textStyle: {
        fontSize: '18px',
        fontWeight: 'bold',
        color: '#000'
      },
      subtext: inputParams.value.field,
      subtextStyle: {
        fontSize: '16px',
        fontWeight: 'bold',
        color: '#000'
      },
      itemGap: 30,
      left: 'center',
    },
    tooltip: {
      trigger: 'axis',
      formatter: function (params) {
        const styleText = 'display:inline-block; width: 10px; height: 10px; border-radius: 50%; margin-right: 5px;';
        //取原始查询数据的各项值显示tooltip
        //x轴
        let nameTip = `<span style="font-weight: bold;">${piField.name}：${params[0].axisValue}</span>`;
        //所用标准
        let manageNameTip = `<br>标准：${data.value.manageNameArr ? data.value.manageNameArr[params[0].dataIndex] : ''}`;
        //实际值
        let valueTip = `<br><span style="${styleText} background: ${colorData.val};"></span>实际值：${data.value.yAxisArr[params[0].dataIndex]}<br>`;
        //红上
        let redUpTip = `<br><span style="${styleText} background:  ${colorData.red};"></span>红上：${data.value.redUpArr[params[0].dataIndex]}`;
        //黄上
        let yellowUpTip = `<br><span style="${styleText} background:  ${colorData.yellow};"></span>黄上：${data.value.yellowUpArr[params[0].dataIndex]}`;
        //标准值
        let standardValue = `<br><span style="${styleText} background:  ${colorData.standard};"></span>标准值：${data.value.standardValueArr[params[0].dataIndex]}`;
        //红下
        let yellowDownTip = `<br><span style="${styleText} background:  ${colorData.yellow};"></span>黄下：${data.value.yellowDownArr[params[0].dataIndex]}`;
        //黄下
        let redDownTip = `<br><span style="${styleText} background:  ${colorData.red};"></span>红下：${data.value.redDownArr[params[0].dataIndex]}`;
        return nameTip+manageNameTip+valueTip+redUpTip+yellowUpTip+standardValue+yellowDownTip+redDownTip;
      }
    },
    grid: {
      top: 80,
      bottom: '5%',
      left: '5%',
      right: '5%'
    },
    xAxis: {
      type: 'category',
      data: res.xAxisArr
    },
    yAxis: {
      type: 'value',
      splitLine: {
        show: false
      },
      axisTick: {
        show: false
      },
      axisLine: {
        show: false
      },
      splitNumber: 10,
      axisLabel: {
        showMinLabel: false,
        showMaxLabel: false
      },
      min: function () {
        return yRangeNumber.value[0];
      },
      max: function () {
        return yRangeNumber.value[1];
      }
    },
    series: [
      {
        data: res.yellowUpArr,
        symbolSize: 0,
        itemStyle: {
          color: colorData.yellow
        },
        type: 'line',
      },
      {
        data: res.redUpArr,
        symbolSize: 0,
        itemStyle: {
          color: colorData.red
        },
        type: 'line',
      },
      {
        data: res.standardValueArr,
        symbolSize: 0,
        itemStyle: {
          color: colorData.standard
        },
        type: 'line',
      },
      {
        data: res.yellowDownArr,
        symbolSize: 0,
        itemStyle: {
          color: colorData.yellow
        },
        type: 'line',
      },
      {
        data: res.redDownArr,
        symbolSize: 0,
        itemStyle: {
          color: colorData.red
        },
        type: 'line',
      },
      {
        data: res.yAxisArr,
        symbol: 'circle',
        symbolSize: 8,
        itemStyle: {
          color: colorData.val
        },
        type: 'line',
        label: {
          show: isEmphasisFlag.value,
          position: 'top',
          fontSize:12,
          formatter: function (params) {
            return params.value;
          }
        },
        emphasis: {
          label: {
            show: !isEmphasisFlag.value,
            position: 'top',
            fontSize:12,
            formatter: function (params) {
              return params.value;
            }
          }
        }
      }
    ]
  };
  //初始化图例
  var myChart = echarts.init(chart.value);
  myChart.setOption(lineChartOption.value);
}
//暴露方法
defineExpose({
  initData,
})
</script>

<style scoped>
/** 时间和数字输入框样式 */
:deep(.ant-input-number) {
  width: 100%;
}

:deep(.ant-picker) {
  width: 100%;
}

.container {
  max-width: 100%;
  padding: 10px;
}

.search-part {
  padding: 14px;
  background-color: #fff;
}

.list-part {
  background-color: #fff;
  margin-top: 10px;
}

.chartPart {
  height: calc(100vh - 400px) !important;
  margin-top: 20px;
}
</style>
