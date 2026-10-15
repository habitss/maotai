<template>
  <a-spin tip="Loading..." :spinning="spinning">
    <div class="container-fluid">
      <!-- 查询区域 -->
      <a-row class="search-part">
        <a-col :span="24">
          <BasicForm @register="registerForm" @submit="handleSearch()"></BasicForm>
        </a-col>
      </a-row>
      <a-row style="background-color: #fff; margin-top: 10px;">
        <a-col :span="24">
          <a-button type="primary" preIcon="ant-design:export-outlined" @click="exportData()" style="margin: 8px">导出</a-button>
        </a-col>
      </a-row>
      <a-row class="list-part">
        <a-col :span="24">
          <SummaryTemplateConfig ref="customReportRef"></SummaryTemplateConfig>
        </a-col>
      </a-row>
    </div>
  </a-spin>
</template>
<script lang="ts" name="summary-template-query" setup>
import { onMounted, reactive, ref } from 'vue';
import { useForm, BasicForm } from '/@/components/Form';
import { useRoute } from 'vue-router';
import dayjs from 'dayjs';
import SummaryTemplateConfig from './SummaryTemplateConfig.vue';
import { getById as getTemplateRecord, queryByTemplateConfig } from '/@/views/dataCalculation/customReport/reportTemplate/index.api';
//获取路由
const route = useRoute();
//获取路由中的code参数
const code = route.params.code;
//获取路由中的code参数
const paramData = reactive({
  id: '',
  dataId: '',
});
//pi字段数据
const piColumns = ref([] as any);
//模版组件ref
const customReportRef = ref();
//模版数据
const templateData = ref();
//加载状态
const spinning = ref(false);
//当前展示的数据
const currentRecord = ref() as any;
/**
 * BasicForm绑定注册;
 */
const [registerForm, { setProps, getFieldsValue, validate }] = useForm({
  schemas: [],
  showActionButtonGroup: true,
  submitButtonOptions: { text: '查询', preIcon: '' },
  resetButtonOptions: { text: '重置', preIcon: '' },
  autoSubmitOnEnter: true,
  showAdvancedButton: true,
  autoAdvancedCol: 3,
  alwaysShowLines: 1,
  labelCol: { style: { width: '100px' } },
  baseColProps: { span: 6 },
});

/**
 * 查询表单数据
 */
async function handleSearch() {
  try{
    const values = await validate();
    //查询PI数据
    const records = await queryByTemplateConfig({
      templateId: paramData.id,
      queryFiledData: JSON.stringify(removeNullValues(values)),
    });
    await initCustomReport(records?.data ? JSON.parse(records?.data) : []);
  } catch (error) {
  }
}
//排除对象的空值属性
function removeNullValues(obj) {
  return Object.keys(obj).reduce((acc, key) => {
    if (obj[key] !== null) {
      acc[key] = obj[key];
    }
    return acc;
  }, {});
}

//获取动态查询配置
async function getDynamicConfig() {
  //获取配置数据
  const reportConfigJson = templateData.value.reportConfig;
  const reportConfig = reportConfigJson ? JSON.parse(reportConfigJson) : [];
  const queryFieldArr = reportConfig?.configForm?.queryField;
  //动态查询表单
  let dynamicSearchForm = [] as any;
  if (queryFieldArr?.length > 0) {
      //添加动态查询
    for (const item of queryFieldArr) {
      let defaultValue = item.defaultVal;
      let component = 'Input';
      let componentProps = {};
      if (item.conditionType == '2') {
        //日期类型
        component = 'RangePicker';
        componentProps = {
          valueFormat: 'YYYY-MM-DD',
        };
        defaultValue = getDefaultValue(defaultValue, 'YYYY-MM-DD');
      } else if (item.conditionType == '3') {
        //日期时间类型
        component = 'RangePicker';
        componentProps = {
          showTime: true,
          valueFormat: 'YYYY-MM-DD HH:mm:ss',
        };
        defaultValue = getDefaultValue(defaultValue, 'YYYY-MM-DD HH:mm:ss');
      }
      let searchObj = {
        label: item.conditionText,
        field: item.conditionField,
        component,
        componentProps,
        defaultValue,
      }
      dynamicSearchForm.push(searchObj);
    }
  }
  setProps({
    schemas: dynamicSearchForm,
  });
}
//获取默认值
function getDefaultValue(rangeType, format = 'YYYY-MM-DD'){
  const now = dayjs();
  let start, end;

  switch (rangeType) {
    case '1':
      // 本周（周一到周日）
      start = now.startOf('week');
      end = now.endOf('week');
      break;

    case '2':
      // 本月
      start = now.startOf('month');
      end = now.endOf('month');
      break;

    case '3':
      // 近7天（包含今天，即今天往前推6天）
      start = now.subtract(6, 'day').startOf('day');
      end = now.endOf('day');
      break;

    case '4':
      // 近30天（包含今天，即今天往前推29天）
      start = now.subtract(29, 'day').startOf('day');
      end = now.endOf('day');
      break;

    case '5':
      // 近60天（包含今天，即今天往前推29天）
      start = now.subtract(59, 'day').startOf('day');
      end = now.endOf('day');
      break;

    default: null;
  }
  return !start ? [] :[start.format(format), end.format(format)];
}

//初始化子组件数据
async function initCustomReport(record) {
  let flag = record == null ? 'detail' : 'edit';
  currentRecord.value = record;
  //重置子组件
  await customReportRef.value.initData(templateData.value, flag, record, getFieldsValue());
}
//导出数据
function exportData(){
  customReportRef.value.exportData();
}

onMounted(async () => {
  //重置pi字段列表数据
  piColumns.value = [];
  //拆分code参数
  const paramArr = code.split('_');
  paramData.id = paramArr[0];
  paramData.dataId = paramArr[1];
  //获取模版数据
  templateData.value = await getTemplateRecord(paramData.id);
  //获取查询配置
  await getDynamicConfig();
  handleSearch();
});
</script>
<style scoped>
.container-fluid {
  padding: 10px;
}

.search-part {
  padding: 14px;
  background-color: #fff;
}

.list-part {
  background-color: #fff;
  height: calc(100vh - 278px) !important;
}
</style>