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
import { searchFormSchema } from './boxDiagram.data';
import { useModal } from '/@/components/Modal';
import { list, getPiFieldList } from './boxDiagram.api';
import ManageStandardModal from '/@/views/dataCalculation/pi/standardValue/components/manageModal.vue';
import dayjs from 'dayjs';
import * as echarts from 'echarts';
import { cloneDeep } from 'lodash-es';
//传入参数
const inputParams = ref();
//折线图配置
const boxChartOption = ref();
//折线图实例
const chart = ref();
//数据
const data = ref();
//显示空
const showEmpty = ref(true);
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
  labelCol: { style: { width: '80px' } },
  wrapperCol: { style: { marginRight: '20px' } },
});
//初始化数据
async function initData(params) {
  inputParams.value = params;
  const res = await getPiFieldList({ tableName: params.tableName });
  //设置时间范围限制
  await updateSchema([
    {
      //字段必填
      field: 'xFieldName',
      component: 'Select',
      componentProps: {
        options: res,
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
  record.inputSubType = 'box';
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
  values.searchTimeEnd = dayjs(values.searchTimeEnd).endOf('day').format('YYYY-MM-DD HH:mm:ss');
  //获取后台数据
  const res = await list(values);
  if (!res || !res.boxList || res.boxList.length === 0) {
    return;
  }
  data.value = res;
  showEmpty.value = false;
  await nextTick(() => {
    initChart(res, values)
  });
}

function initChart(res, values) {
  let title = `${values.standardName}（${dayjs(values.searchTimeStart).format('YYYY年MM月DD日')}-${dayjs(values.searchTimeEnd).format('YYYY年MM月DD日')}）`;
  //箱图配置
  boxChartOption.value = {
    title: {
      text: title,
      textStyle: {
        fontSize: '18px',
        fontWeight: 'bold',
        color: '#000'
      },
      itemGap: 30,
      left: 'center',
    },
    tooltip: {
      trigger: 'item',
      axisPointer: {
        type: 'shadow'
      },
      formatter: function (params) {
        if (params.seriesType === 'boxplot') {
          return [
            `<label style="font-weight: bold;">${res.nameList[params.value[0]]}</label>`,
            'Q1: ' + params.value[2],
            '最小值: ' + params.value[1],
            '中位数: ' + params.value[3],
            '最大值: ' + params.value[5],
            'Q3: ' + params.value[4],
            '箱体高度: ' + (params.value[4] - params.value[2]).toFixed(2),
          ].join('<br />');
        }
      }
    },
    grid: {
      top: 60,
      bottom: '4%',
      left: '4%',
      right: '4%'
    },
    xAxis: {
      type: 'category',
      data: res.nameList
    },
    yAxis: {
      type: 'value',
      splitNumber: 10,
        min: function(value) {
          let minNum = Math.min(value.min, res.redUp);
          return Number((minNum - (minNum / 5)).toFixed(2));
        },
        max: function(value) {
          let maxNum = Math.max(value.max, res.redUp);
          return Number((maxNum + (maxNum / 5)).toFixed(2));
        }
    },
    series: [
      {
        name: 'boxplot',
        type: 'boxplot',
        data: res.boxList,
        itemStyle: {
          borderColor: 'black',
          borderWidth: 1
        },
        markLine: {
          silent: true,
          symbol: 'none',
          lineStyle: {
            width: 2,
            type: 'solid',
          },
          data: [
            { yAxis: res.redUp, name: '红上', lineStyle: { color: '#FE0000' } },
          ]
        }
      },
      {
        name: 'scatter',
        type: 'scatter',
        data: res.scatterList,
        symbolSize: 6,
        itemStyle: {
          color: (params) => {
            switch (params.dataIndex % 3) {
              case 0:
                return '#E19359';
              case 1:
                return '#4874CB';
              case 2:
                return '#00FF00';
              default:
                return 'black';
            }
          }
        },
        label: {
          show: true,
          position: 'top',
          formatter: function (params) {
            return params.value[1];
          }
        }
      }
    ]
  };
  //初始化图例
  var myChart = echarts.init(chart.value);
  myChart.setOption(boxChartOption.value);
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