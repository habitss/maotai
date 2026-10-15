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
                <!-- <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls()" style="margin-left: 8px">导出</a-button> -->
                <!--上一条-->
                <a-button type="primary" @click="pageSearch(0)" v-auth="'pi:mes3_reportQuery:up'" style="margin-left: 8px"> 下一批 </a-button>
                <!--下一条-->
                <a-button type="primary" @click="pageSearch(1)" v-auth="'pi:mes3_reportQuery:down'" style="margin-left: 8px;margin-right: 8px"> 上一批 </a-button>
                <a-dropdown>
                  <template #overlay>
                    <a-menu>
                      <a-menu-item key="1" @click="exportExcel">
                        <Icon icon="ant-design:export-outlined"></Icon>
                        导出
                      </a-menu-item>
                      <a-menu-item key="1" @click="batchExportExcel">
                        <Icon icon="ant-design:export-outlined"></Icon>
                        批量导出
                      </a-menu-item>
                    </a-menu>
                  </template>
                  <a-button v-auth="'pi:mes3_reportQuery:export'">导出
                    <Icon icon="mdi:chevron-down"></Icon>
                  </a-button>
                </a-dropdown>
              </div>
            </template>
          </BasicForm>
        </a-col>
      </a-row>
      <a-row class="list-part">
        <a-col :span="24">
          <CustomReport ref="customReportRef"></CustomReport>
        </a-col>
      </a-row>
    </div>
  </a-spin>
  <!-- 填报modal -->
  <DataList @register="registerModal" @success="initCustomReport"  width="80vw"  max-height="80vh"></DataList>
  <!-- 导出数据选择-->
  <ExportDataList @register="exportModal" @success="exportSelect" :width="1200"></ExportDataList>
</template>
<script lang="ts" name="dataCalculation-report-query-pi-index" setup>
import { onMounted, reactive, ref } from 'vue';
import { useForm, BasicForm } from '/@/components/Form';
import { useRoute } from 'vue-router';
import DataList from './components/dataList.vue';
import ExportDataList from './components/exportDataList.vue';
import CustomReport from '/@/views/dataCalculation/customReport/customReport/index.vue';
import { useModal } from '/@/components/Modal';
import { list as getpiColumns,getTimeFormat } from '@/views/dataCalculation/pi/allocationTable/AllocationTableField.api';
import { getDictItemList } from '/@/views/dataCalculation/customReport/customReport/index.api';
import { getById as getTemplateRecord } from '/@/views/dataCalculation/customReport/reportTemplate/index.api';
import { list as getPiList, batchEditVal,getTableFieldData } from '/@/views/dataCalculation/pi/allocationTable/QueryTable.api';
import { useMessage } from '/@/hooks/web/useMessage';
import { Modal } from 'ant-design-vue';
import { options } from '../../practice/promotion/BestPracticePromotion.data';
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
//保存按钮加载状态
const saveLoading = ref(false);
//加载状态
const spinning = ref(false);
//当前展示的数据
const currentRecord = ref() as any;
//PI数据列表弹窗
const [registerModal, { openModal }] = useModal();
//导出PI数据列表弹窗
const [exportModal, { openModal : openExportModal, closeModal : closeExportModal }] = useModal();
//最新前几条数据
const historyData = ref({});
//需要获取最新前几条数据的字段集合
const historyDataArr = ref([]);
/**
 * BasicForm绑定注册;
 */
const [registerForm, { setProps, getFieldsValue, setFieldsValue }] = useForm({
  schemas: [],
  showActionButtonGroup: true,
  submitButtonOptions: { text: '查询', preIcon: '' },
  resetButtonOptions: { text: '重置', preIcon: '' },
  autoSubmitOnEnter: true,
  showAdvancedButton: true,
  autoAdvancedCol: 3,
  alwaysShowLines: 2,
  labelCol: { style: { width: '100px' } },
  baseColProps: { span: 6 },
});

/**
 * 查询表单数据
 */
async function handleSearch(isInit) {
  //查询PI数据
  const { records, total } = await getPiList({
    allocationTableId: templateData.value.dataId,
    tableId: templateData.value.tableId,
    pageSize: 10,
    orderField: 'sort',
    orderType: 'asc',
    ...getFieldsValue()
  });
  if(total == 0){
    createMessage.warning("未查询到数据");
  }
  //如果数据大于1条，则让用户选择，否则直接显示模板
  if (total > 1 && !isInit) {
    openModal(true, {
      records,
      piColumns: piColumns.value,
      queryParam: {
        allocationTableId: templateData.value.dataId,
        tableId: templateData.value.tableId,
        orderField: 'sort',
        orderType: 'asc',
        ...getFieldsValue()
      }
    })
  } else {
    await initCustomReport(records[0] ?? []);
  }
}

/**
 * 查询(上一页/下一页)
 */
async function pageSearch(type) {
  // 只保留field_*字段，过滤掉其他字段（如modifiedFields、piStandardData等）
  const sortValues = {};
  for (const key in currentRecord.value) {
    if (key.startsWith('field_') || key === 'id') {
      sortValues[key] = currentRecord.value[key];
    }
  }
  const { records, total } = await getPiList({
    id:currentRecord.value.id,
    allocationTableId: templateData.value.dataId,
    tableId: templateData.value.tableId,
    pageSize: 1,
    orderField: 'sort',
    orderType: 'asc',
    upOrDown: type, //0下一批/1上一批
    //当前数据，转为JSON字符串传递，避免后端解析Map类型错误
    currentSortValues: JSON.stringify(sortValues),
    ...getFieldsValue()
  });
  if(total == 0){
    createMessage.warning("未查询到数据");
    return;
  }
  console.log("当前页ID: "+ records[0]?.id)
  await initCustomReport(records[0] ?? []);
}


//获取动态查询配置
async function getDynamicConfig() {
  //获取pi分配字段
  const { records } = await getpiColumns({
    pageSize: 1000,
    allocationTableId: paramData.dataId,
    column: 'sort',
    order: 'asc',
  });
  //动态查询表单
  let dynamicSearchForm = [] as any;
  if (records?.length > 0) {
    //获取需要查询需要显示历史的集合
    for (const item of records) {
      if (item.isQuery == 1 && item.historyData=='1') {
        historyDataArr.value.push(item.fieldName);
      }
    }
    //获取历史数据
    if(historyDataArr.value && historyDataArr.value.length > 0){
      await getHistoryData(paramData.dataId);
    }
    //解析表头数据
    for (const item of records) {
      //获取显示字段，只展示前10个
      if (item.isList == 1 && piColumns.value.length < 10) {
        let dataIndex = item.fieldName;
        // if (item.isQuery == 1 && item.historyData=='1') {
        //   dataIndex = item.fieldName+'_tag'
        // }
        piColumns.value.push({
          title: item.name,
          align: 'center',
          dataIndex: dataIndex,
          ellipsis: true,
          //width: 100,
        },);
      }
      //获取唯一约束字段
      if(item.isUnique == 1){
        uniqueMap.value[item.fieldName] = item.name;
      }
      //获取查询字段
      if (item.isQuery == 1) {
        //添加动态查询
        let component = 'Input';
        let componentProps = {};

        if(item.historyData=='1'){
          //查询显示前几条数据
          component = 'Select';
          componentProps = {
            options: historyOptions(item.fieldName),
            mode: "tags",
            "max-count": 1,
            onChange: (val) => handleSingleTagChange(val, item.fieldName)
          }
        }else if (item.type == '4') {
          //日期类型
          component = 'RangeDate';
        } else if (item.type == '6') {
          //日期时间类型
          component = 'RangePicker';
          componentProps = {
            showTime: true,
            valueFormat: 'YYYY-MM-DD HH:mm:ss',
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
        }
        dynamicSearchForm.push(searchObj);
      }
    }
  }
  setProps({
    schemas: dynamicSearchForm,
  });
}

//初始化子组件数据
async function initCustomReport(record) {
  let flag = record == null ? 'detail' : 'edit';
  currentRecord.value = record;
  //重置子组件
  await customReportRef.value.initData(templateData.value, flag, record);
}

//保存数据
async function handleSave() {
  saveLoading.value = true;
  try {
    //获取报表数据
    const reportConfig = customReportRef.value.getConfigData();
    const editFields = customReportRef.value.getEditFieldSet();
    if (!reportConfig.fillInList || reportConfig.fillInList.length == 0) {
      createMessage.warning("暂无被修改的数据");
      return;
    }

    //组装保存数据
    let saveObj = {
      allocationTableId: paramData.dataId,
      tableName: templateData.value.tableName,
      type: '2',
      data: [],
      editFields //被修改的字段集合
    } as any;
    //组装填充数据
    let fillObj = {
      id: currentRecord.value.id
    } as any;
    //解析报表中的填充数据
    for (const item of reportConfig.fillInList) {
      fillObj[item.mappingColumn] = item.value;
      if(uniqueMap.value[item.mappingColumn] && !item.value){
        createMessage.warning(`${uniqueMap.value[item.mappingColumn]}(第${item.row+1}行，第${item.col+1}列)为唯一约束，值不能为空！`);
        return;
      }
    }
    saveObj.data.push(fillObj);
    await batchEditVal(saveObj);
    createMessage.success("保存成功");
    await refreshCurrentRecord();
  } catch (error) {
    createMessage.error("保存失败");
  } finally {
    saveLoading.value = false;
  }
}

// 保存后仅刷新当前数据，避免触发多条数据选择弹窗
async function refreshCurrentRecord() {
  if (!currentRecord.value?.id) {
    return;
  }
  const { records, total } = await getPiList({
    id: currentRecord.value.id,
    allocationTableId: templateData.value.dataId,
    tableId: templateData.value.tableId,
    pageSize: 1,
    orderField: 'sort',
    orderType: 'asc',
    ...getFieldsValue()
  });
  if (total == 0 || !records?.length) {
    createMessage.warning("未查询到当前数据");
    return;
  }
  await initCustomReport(records[0]);
}

onMounted(async () => {
  //重置pi字段列表数据
  piColumns.value = [];
  //拆分code参数
  const paramArr = code.split('_');
  paramData.id = paramArr[0];
  paramData.dataId = paramArr[1];
  //获取查询配置
  getDynamicConfig();
  //获取模版数据
  templateData.value = await getTemplateRecord(paramData.id);
  //获取格式配置字段
  //[{"fieldName":"field_0004","time_format":"2"}]
  let res = await getTimeFormat({allocationTableId: paramData.dataId});
  if(res){
    templateData.value.fieldFormat = res
  }
  // initCustomReport(null);
  handleSearch(true);
});


//导出当前页
function exportExcel(){
  const reportConfig = customReportRef.value.getConfigData();
  if (!reportConfig.fillInList || reportConfig.fillInList.length > 0) {
    createMessage.warning("有数据尚未保存，为确保导出内容完整，请先保存后再导出");
    return;
  }
  customReportRef.value.exportExcel();
}
//批量导出
function batchExportExcel(){
  const reportConfig = customReportRef.value.getConfigData();
  if (!reportConfig.fillInList || reportConfig.fillInList.length > 0) {
    createMessage.warning("有数据尚未保存，为确保导出内容完整，请先保存后再导出");
    return;
  }
  selectData();
}

//批量导出数据选择数据
async function selectData(){
  openExportModal(true, {
    piColumns: piColumns.value,
    queryParam: {
      allocationTableId: templateData.value.dataId,
      tableId: templateData.value.tableId,
      orderField: 'sort',
      orderType: 'asc',
      ...getFieldsValue()
    }
  })
}
// 完成选择选择,进行导出
async function exportSelect(records){
  if(records && records.length > 0){
    spinning.value = true;
    //导出
    await customReportRef.value.batchExportExcel(records);
    spinning.value = false;
    //关闭
    closeExportModal();
  }else{
    createMessage.warning("未选择待导出数据");
  }
}

//获取字段历史数据
async function getHistoryData(val){
  let res = await getTableFieldData({reportType: '2', tableId:val , fields: historyDataArr.value.join()})
  if (res) {
    historyData.value = res;
  }
  
}
//获取近对应字段 10条数据
function historyOptions(val){
    const optionsArray = historyData.value[val].map(value => ({
        value: value,
        label: value
    }));
    return optionsArray;
}
//对应字段 10条数据选择
function handleSingleTagChange(val,fieldName){
  const tagFieldName = fieldName + '_tag';
  let tagVal = [];

  if (!val || val.length === 0) {
    tagVal = [];
  } else if (val.length > 1) {
    tagVal = [val[val.length - 1]];
  } else {
    tagVal = val;
  }

  //用 useForm 返回的 setFieldsValue 更新表单内部值
  setFieldsValue({
    [tagFieldName]: tagVal,
    [fieldName]: tagVal[0],
  });
}
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
  margin-top: 10px;
  height: calc(100vh - 260px) !important;
}
</style>
