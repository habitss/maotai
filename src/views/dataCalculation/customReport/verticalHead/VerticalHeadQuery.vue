<template>
  <a-spin tip="Loading..." :spinning="spinning">
    <div class="container-fluid">
      <!-- 查询区域 -->
      <a-row class="search-part">
        <a-col :span="24">
          <BasicForm @register="registerForm" @submit="handleSearch(false)">
            <template #formFooter>
              <div>
                <a-button type="primary" @click="handleSave" :loading="saveLoading"> 保存 </a-button>
              </div>
            </template>
          </BasicForm>
        </a-col>
      </a-row>
      <a-row style="background-color: #fff; margin-top: 10px;">
        <a-col :span="24">
          <a-button type="primary" preIcon="ant-design:export-outlined" @click="exportData()" style="margin: 8px">导出</a-button>
        </a-col>
      </a-row>
      <a-row class="list-part">
        <a-col :span="24">
          <VerticalHeadConfig ref="customReportRef"></VerticalHeadConfig>
        </a-col>
      </a-row>
    </div>
  </a-spin>
</template>
<script lang="ts" name="vertical-template-query" setup>
import { onMounted, reactive, ref } from 'vue';
import { useForm, BasicForm } from '/@/components/Form';
import { useRoute } from 'vue-router';
import VerticalHeadConfig from './VerticalHeadConfig.vue';
import { list as getpiColumns } from '@/views/dataCalculation/pi/allocationTable/AllocationTableField.api';
import { getDictItemList } from '/@/views/dataCalculation/customReport/customReport/index.api';
import { getById as getTemplateRecord } from '/@/views/dataCalculation/customReport/reportTemplate/index.api';
import { list as getPiList, batchEditVal } from '/@/views/dataCalculation/pi/allocationTable/QueryTable.api';
import { useMessage } from '/@/hooks/web/useMessage';
import dayjs from 'dayjs';
const { createMessage } = useMessage();
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
//pi唯一标识字段
const uniqueMap = ref([] as any);
//模版组件ref
const customReportRef = ref();
//模版数据
const templateData = ref();
//模版数据
const configFormData = ref();
//保存按钮加载状态
const saveLoading = ref(false);
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
async function handleSearch(isInit) {
  try{
    let values = await validate();
    let queryFieldValue = values[configFormData.value.queryField];
    if(queryFieldValue && dayjs(queryFieldValue).isValid() && configFormData.value.queryShift == '1'){
      const nextDay = dayjs(queryFieldValue).add(1, 'day');
      values[configFormData.value.queryField] = queryFieldValue+' 08:00:00,'+nextDay.format('YYYY-MM-DD')+' 07:00:00';
    }
    
    customReportRef.value.openFullLoading();
    //查询PI数据
    const { records } = await getPiList({
      allocationTableId: templateData.value.dataId,
      tableId: templateData.value.tableId,
      pageSize: 999,
      orderField: 'sort',
      orderType: 'asc',
      ...values
    });
    await initCustomReport(records ?? []);
  } catch (error) {
  }
}
const dates = ref({});
//获取动态查询配置
async function getDynamicConfig() {
  dates.value = [];
  //获取配置数据
  const reportConfigJson = templateData.value.reportConfig;
  const reportConfig = reportConfigJson ? JSON.parse(reportConfigJson) : [];
  const requiredQueryField = reportConfig.configForm.queryField;
  const queryShift = reportConfig.configForm.queryShift;
  //获取pi分配字段
  const { records } = await getpiColumns({
    pageSize: 500,
    allocationTableId: paramData.dataId,
  });
  //动态查询表单
  let dynamicSearchForm = [] as any;
  if (records?.length > 0) {
    //解析表头数据
    for (const item of records) {
      //获取唯一约束字段
      if(item.isUnique == 1){
        uniqueMap.value[item.fieldName] = item.name;
      }
      //获取查询字段
      if (item.isQuery == 1) {
        //添加动态查询
        let component = 'Input';
        let componentProps = {};
        let defaultVal;

        if (item.type == '4') {
          //日期类型
          component = 'RangePicker';
          if(requiredQueryField == item.fieldName && queryShift == '1'){
            defaultVal = dayjs();
            component = 'DatePicker';
          }
          componentProps = {
            valueFormat: 'YYYY-MM-DD'
          };
        } else if (item.type == '6') {
          dates.value[item.fieldName] = [];
          //日期时间类型
          let showTime = true;
          let formatStr = 'YYYY-MM-DD HH:mm:ss';
          if(item.fieldName && queryShift == '1'){
            component = 'DatePicker';
            showTime = false;
            formatStr = 'YYYY-MM-DD';
            defaultVal = dayjs();
          }else{
            component = 'RangePicker';
          }
          componentProps = {
            showTime: showTime,
            valueFormat: formatStr,
          };
        } else if (item.type == 1 && item.dropdownData) {
          //判断是否为下拉字典
          component = 'ApiSelect';
          componentProps = {
            api: getDictItemList,
            numberToString: false,
            labelField: 'itemKey',
            valueField: 'itemVal',
            params: {
              pageSize: 999,
              groupCode: item.dropdownData
            },
            resultField: 'records'
          };
        }
        let searchObj = {
          label: item.name,
          field: item.fieldName,
          component,
          componentProps,
          required: requiredQueryField === item.fieldName,
        }
        console.log('searchObj 》》》》》', searchObj);
        if(defaultVal){
          searchObj.defaultValue = defaultVal;
        }
        if(requiredQueryField === item.fieldName){
          dynamicSearchForm.unshift(searchObj);
        }else{
          dynamicSearchForm.push(searchObj);
        }
      }
    }
  }
  setProps({
    schemas: dynamicSearchForm,
  });
}

function range(start: number, end: number){
  const result = [] as any;
  for (let i = start; i < end; i++) {
    result.push(i);
  }
  return result;
};

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

//保存数据
async function handleSave() {
  saveLoading.value = true;
  try {
    //获取报表数据
    const editData = customReportRef.value.getEditFieldSet();
    if (!editData || editData.length == 0) {
      createMessage.warning("暂无被修改的数据");
      return;
    }
    //组装保存数据
    let saveObj = {
      allocationTableId: paramData.dataId,
      tableName: templateData.value.tableName,
      type: '2',
      data: editData,
    } as any;
    await batchEditVal(saveObj);
    createMessage.success("保存成功");
    // handleSearch(false);
  } catch (error) {
    createMessage.error("保存失败");
  } finally {
    saveLoading.value = false;
  }
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
  configFormData.value = JSON.parse(templateData.value.reportConfig).configForm
  //获取查询配置
  await getDynamicConfig();
  // //获取格式配置字段
  // //[{"fieldName":"field_0004","time_format":"2"}]
  // let res = await getTimeFormat({allocationTableId: paramData.dataId});
  // if(res){
  //   templateData.value.fieldFormat = res
  // }
  // initCustomReport(null);
  handleSearch(true);
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
  height: calc(100vh - 260px) !important;
}
</style>