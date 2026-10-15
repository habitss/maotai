<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable">
      <template #tableTitle>
        <a-button type="primary" 
          preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
      </template>
      <template #headerCell="{ column }">
        <template v-if="column.dataIndex && column.dataIndex.startsWith('dict')">
            <a-popover title="" placement="topLeft">
              <template #content>
                <p style="width: 200px">{{ column.customTitle }}</p>
              </template>
              <Icon icon="ant-design:exclamation-circle-outlined" color="#1677FF"></Icon>
              {{ column.customTitle }}
            </a-popover>
        </template>
        <template v-else>{{ column.customTitle }}</template>
      </template>
      <!--  报表列表插槽 -->
      <template #form-tableCode="{ model, field }">
        <a-select v-model:value="model[field]" :options="tableArr" placeholder="请选择" @change="tableChange" showSearch="true"
          :allowClear="true" optionFilterProp="label">
        </a-select>
      </template>

      <template #form-queryField="{ model, field }">
        <a-select v-model:value="model[field]" :options="piArr" placeholder="请选择" :allowClear="true" showSearch="true" optionFilterProp="label">
        </a-select>
      </template>

      <template #form-queryTime>
        <!--开始时间-->
        <a-range-picker v-model:value="rangePickerTime" format="YYYY-MM-DD HH:mm:ss" valueFormat="YYYY-MM-DD HH:mm:ss"
          show-time />
      </template>

      <template #form-queryFieldSlot>
        <!--开始时间-->
        <tableList ref="tableListRef"/>
      </template>

      <template #anomalyRepeatTotalSlots="{ record, text }">
        <a-button type="link" @click="handleAnomalyRepeat(record)">{{text}}</a-button>
      </template>
    </BasicTable>
    <!-- 重复异常统计 -->
    <PiRepeatExceptionStatisticsModal @register="registerModal"></PiRepeatExceptionStatisticsModal>
    <div style="padding: 0 10px 10px;">
      <Bar style="background-color: #fff; height: 500px" class="chartPart" :chartData="anomalyRepeatRateArr" :option="barOption"></Bar>
    </div>
  </div>
</template>

<script lang="ts" name="consistencyIndexMain" setup>
import { ref, reactive, onMounted } from 'vue';
import { BasicTable } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage';
import { columns, searchFormSchema, barOption, setTableListRef } from './index.data';
import { list, allocationTableList, queryPiList } from './index.api';
import { dicList } from '/@/views/dataCalculation/mdm/dict/dict.api';
import { mapTableTotalSummary2, getPercentage } from '/@/utils/common/compUtils';
import { useModal } from '/@/components/Modal';
import Bar from '/@/components/chart/Bar.vue';
import tableList from './components/tableList.vue';
import { Icon } from '/@/components/Icon';
import ExcelJS from '/@/utils/excelUtils/exportExcel';
import PiRepeatExceptionStatisticsModal from '/@/views/dataCalculation/pi/repeatException/components/PiRepeatExceptionStatisticsModal.vue'
//查询参数
const queryParam = reactive<any>({});
const [registerModal, { openModal }] = useModal();
//PI日报集合
const tableArr = reactive<Record<string, any>>([]);
//PI集合(仅限时间类型和日期类型的PI集合)
const piArr = reactive<Record<string, any>>([]);
//查询条件-时间
const rangePickerTime = ref<string[]>([]);
//动态列字段
const dynamicDolumns = ref<string[]>([]);
//异常重复率柱状图数据
const anomalyRepeatRateArr = ref([]);
const tableListRef = ref();
//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    title: 'PI绿区率统计表',
    api: list,
    immediate: false,
    canResize: true,
    pagination: false,
    showActionColumn: false,
    ellipsis: false,
    formConfig: {
      schemas: searchFormSchema,
      autoSubmitOnEnter: true,
      showAdvancedButton: true,
      fieldMapToNumber: [
      ],
      fieldMapToTime: [
      ],
      labelWidth: 100,
      submitFunc: handleQuery
    },
    // 显示底部合计
    showSummary: true,
    // 底部合计计算方法
    summaryFunc: onSummary,
  },
})

const [registerTable, { setProps, getColumns, getDataSource, getForm, setTableData, setLoading }, {  }] = tableContext;
//重写查询事件
async function handleQuery(){
  try {
    let values = await getForm().validate();
    values.queryField = await tableListRef.value.getQueryFieldList();
    values.timeBegin = rangePickerTime.value[0];
    values.timeEnd = rangePickerTime.value[1];
    setLoading(true);
    const result = await list(values);
    
    // 计算5W分析率(实际)并设置其他列值
    result?.forEach(item => {
      // 计算5W分析率(实际) = 5why分析数/(红区数+黄区数)
      let fiveWhyActualRate = 0;
      let redNum = Number(item.red) || 0;
      let yellowNum = Number(item.yellow) || 0;
      let fiveWhyTotalNum = Number(item.fiveWhyTotal) || 0;
      if (redNum + yellowNum > 0) {
        fiveWhyActualRate = (fiveWhyTotalNum / (redNum + yellowNum)) * 100;
      }
      item.fiveWhyActualRate = fiveWhyActualRate.toFixed(2);
      
      // 设置其他4个列的值为"/"
      item.fiveWhyAmendRate = "/";
      item.fiveWhyWorkshopEvaluationTotal = "/";
      item.fiveWhyWorkshopEvaluationOkTotal = "/";
      item.fiveWhySelfCheckOkTotal = "/";
    });
    
    let arr = [] as any;
    result?.forEach(item => {
      arr.push({
        name: item.fieldName,
        value: item.anomalyRepeatRate
      });
    })
    anomalyRepeatRateArr.value = arr.slice().sort((a, b) => b.value - a.value);

    setTableData(result);
  } catch (error) {
    console.log('error :>> ', error);
  } finally {
    setLoading(false);
  }
}

/**
 * 初始化报表下拉数据
 */
async function getTableArr() {
  //pi报表
  allocationTableList({ pageNo: 1, pageSize: 999 }).then((res) => {
    if (res && res.records && res.records.length > 0) {
      res.records.map((item) => {
        tableArr.push({ label: item.name, value: item.id, tableCode: item.tableName });
      });
    }
  });
}

/**
 * 报表下拉选择事件,联动报表内PI的信息
 */
function tableChange(val, obj) {
  piArr.length = 0;
  queryPiList({ tableId: val, pageSize: 999 }).then((res) => {
    if (res.records && res.records.length > 0) {
      queryParam.tableCode = obj.tableCode;
      queryParam.tableId = val;
      queryParam.tableName = obj.label;
      for (const item of res.records) {
        //只显示时间类型的PI
        if (item.type == '6') {
          piArr.push({ label: item.name, value: item.fieldName });
        }
      }
    }
  })
}
function initDate(){
  const now = new Date();

  // 计算上周的起始日期（周一）和结束日期（周日）
  const lastWeekMonday = new Date(now);
  lastWeekMonday.setDate(now.getDate() - now.getDay() - 6); // 调整为上周一
  lastWeekMonday.setHours(0, 0, 0, 0); // 00:00:00

  const lastWeekSunday = new Date(lastWeekMonday);
  lastWeekSunday.setDate(lastWeekMonday.getDate() + 6); // 上周日
  lastWeekSunday.setHours(23, 59, 59, 0); // 23:59:59
  rangePickerTime.value = [formatDate(lastWeekMonday), formatDate(lastWeekSunday)];
}
// 格式化日期为 YYYY-MM-DD HH:mm:ss
function formatDate(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}

/**
 * 计算合计
 * @param tableData
 */
function onSummary(tableData: Recordable[]) {
  // 可用工具方法自动计算合计
  const totals = mapTableTotalSummary2(
    tableData,
    [
      ...[
        'green', 'yellow','red','all','anomalyTotal', 'anomalyRepeatTotal', 'teamEvaluationTotal',
        'fiveWhyTotal', 'fiveWhyWorkshopEvaluationTotal', 'fiveWhyWorkshopEvaluationOkTotal', 
        'fiveWhySelfCheckOkTotal', 'fiveWhySelfCheckOkTotal'
      ], 
      ...dynamicDolumns.value
    ],
    [
      'anomalyTypeMap'
    ]
  );
  // 计算5W分析率(实际)的合计值
  let fiveWhyActualRateTotal = 0;
  let redTotal = Number(totals.red) || 0;
  let yellowTotal = Number(totals.yellow) || 0;
  let fiveWhyTotalNum = Number(totals.fiveWhyTotal) || 0;
  if (redTotal + yellowTotal > 0) {
    fiveWhyActualRateTotal = (fiveWhyTotalNum / (redTotal + yellowTotal)) * 100;
  }
  
  let resultObj = {
    _row: '合计',
    _index: '合计',

    tableName: '合计',
    //合计结果
    green:totals.green,
    yellow:totals.yellow,
    red:totals.red,
    all:totals.all,
    // 计算平均值
    greenProportion: getPercentage(totals.green, totals.all, 2, false),
    yellowProportion: getPercentage(totals.yellow, totals.all, 2, false),
    redProportion: getPercentage(totals.red, totals.all, 2, false),
    anomalyTotal: totals.anomalyTotal,
    anomalyRepeatTotal: totals.anomalyRepeatTotal,
    anomalyRepeatRate: getPercentage(totals.anomalyRepeatTotal, totals.anomalyTotal, 2, false),
    teamEvaluationTotal: totals.teamEvaluationTotal,
    fiveWhyTotal: totals.fiveWhyTotal,
    fiveWhyAmendRate: '/',
    fiveWhyActualRate: fiveWhyActualRateTotal.toFixed(2),
    fiveWhyWorkshopEvaluationTotal: '/',
    fiveWhyWorkshopEvaluationOkTotal: '/',
    fiveWhySelfCheckOkTotal: '/',
  };
  dynamicDolumns.value?.forEach((item) => { 
    resultObj[item] = totals[item];
  })
  return [
    resultObj
  ];
}

//导出excel表格数据
function onExportXls() {
  //根据类型设置合并列
  let mergeArr = [];
  let fileName = 'PI绿区率统计表';
  //获取导出表头
  const headers = ExcelJS.setHeader(getColumns(), mergeArr);
  //调用导出方法
  ExcelJS.commonDownColorExcel(headers, getDataSource(), fileName)
}
/**
 * 重复异常点击事件
 * @param record 行数据
 */
function handleAnomalyRepeat(record){
  openModal(true, {
    record,
  });
}

onMounted(async () => {
  const res = await dicList({code: 'MES3_ABNORMAL_TYPE'});
  // let anomalyTypeColumn = [] as any;
  // res?.forEach((item) => {
  //   let key = 'dict_'+item.itemVal;
  //   anomalyTypeColumn.push({
  //     title: item.itemKey,
  //     align: "center",
  //     dataIndex: key,
  //     width: '200px',
  //     ellipsis:true,
  //     customRender: ({text, record}) => {
  //       return text ?? (record.anomalyTypeMap && record.anomalyTypeMap[key] ? record.anomalyTypeMap[key] : '0');
  //     }
  //   })
  //   dynamicDolumns.value.push(key);
  // });
  // columns[11].children = anomalyTypeColumn;
  setProps({
    columns: columns,
  });
  //时间初始化
  initDate();
  setTableListRef(tableListRef.value);
});

</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}
:deep(.ant-table-wrapper .ant-table.ant-table-small) {
  .ant-table-footer
  {
    padding: 8px 0px !important;
  }
}
</style>