<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable">
      <!--插槽:table标题-->
      <template #tableTitle v-if="showSaveBtn">
        <a-button type="primary" @click="handleSave(true)" :loading="saveLoading">保存</a-button>
        <a-button type="primary" @click="handleAddRow(null, null, null)">新增分析</a-button>
        <a-button type="primary" @click="handleAddWhy">扩展5W</a-button>
        <a-button type="primary" @click="handleAddMeasure">新增措施</a-button>
        <a-button type="primary" @click="handleExportXls">导出5W</a-button>
        <a-button type="primary" @click="handleExportXlsMeasure">导出5W措施</a-button>
        <a-button type="primary" @click="handleExportAll">导出全部</a-button>
        <a-button type="primary" @click="handleCancel" v-if="showCancel">取消分析</a-button>
        <a-button type="primary" @click="handleCite" v-if="showCite > 0">重复引用</a-button>
      </template>

      <!--插槽:表格内容-->
	  
      <template #bodyCell="{ column, record, index }">
        <div v-if="column.key.endsWith('Reason')" class="box">
          <a-textarea v-model:value="record[column.key]" :placeholder="`请输入${column.customTitle}`"
            style="height: 100%;" @change="(e) => handleChange(e, column, index, record)" />
        </div>
        <div v-else-if="column.key !== 'problem'" class="box">
          <a-textarea v-model:value="record[column.key]" :placeholder="`请输入${column.customTitle}`"
            style="height: 100%;" />
          <span v-if="column.key.endsWith('Why')">
            <a-icon @click="handleAddRow(record, index, column)" type="plus-square" />
          </span>
        </div>
      </template>

      <!--操作栏-->
      <template #action="{ record, index }">
        <TableAction :actions="getTableAction(record, index)" />
      </template>
    </BasicTable>
  </div>
  <div>
    <!-- 措施 -->
    <MeasureList ref="measureListRef"></MeasureList>
  </div>
  <!-- pi重复引用 -->
  <CitePiModal @register="registerCitePiModal" @success="handleOk"></CitePiModal>
  <!-- kpi重复引用 -->
  <CiteKpiModal @register="registerCiteKpiModal" @success="handleOk"></CiteKpiModal>
</template>

<script lang="ts" name="dataCalculation-fiveWhy-fiveWhyList" setup>
import { ref } from 'vue';
import { BasicColumn, BasicTable, TableAction } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage'
import { columns, columnWidth } from '../fiveWhy.data';
import { saveOrUpdate, queryData, deleteOne } from '../fiveWhy.api';
import { cloneDeep } from 'lodash-es';
import { useMessage } from '/@/hooks/web/useMessage';
import MeasureList from './measureList.vue';
import { buildUUID } from '/@/utils/uuid';
import { exportExcel, exportExcelWithHeader } from '/@/utils/common/exportToExcelUtils';
import { useModal } from '/@/components/Modal';
import CitePiModal from './CitePiModal.vue';
import CiteKpiModal from './CiteKpiModal.vue';
//注册pi重复引用modal
const [registerCitePiModal, { openModal : openCitePiModal }] = useModal();
//注册kpi重复引用modal
const [registerCiteKpiModal, { openModal : openCiteKpiModal }] = useModal();
//是否显示重复引用
const showCite = ref(0);
//重复引用查询参数
const citeSearchParams = ref({});
const showCancel = ref(false);
const saveLoading = ref(false);
const emit = defineEmits(['close']);
const fiveWhyData = ref([]);
const baseData = ref({
	tableName: '',
	tableCode: '',
	fieldName: '',
	fieldCode: '',
	dataId: '',
	standardValue: '',
	actualValue: '',
	color: '',
	type: '',
  problem: '',
  problemDesc: '',
  mergeRule: '',
  resonDtoList: [] as any,
  fiveWhyDetailList: [] as any,
  whyNum: 6,
  id: undefined
});
const showSaveBtn = ref(true);
//措施组件ref对象
const measureListRef = ref();
//消息提示
const { createMessage: msg, createConfirm } = useMessage();
//下一个添加why的下标
const whyNum = ref(6);
//新增why枚举
const NUM_ENUM = {
  6: { why: 'sixWhy', reason: 'sixReason' },
  7: { why: 'sevenWhy', reason: 'sevenReason' },
  8: { why: 'eightWhy', reason: 'eightReason' },
  9: { why: 'nineWhy', reason: 'nineReason' },
  10: { why: 'tenWhy', reason: 'tenReason' },
};
//色区
const COLOR_ENUM = {
  green: '绿区',
  yellow: '黄区',
  red: '红区',
  grey: '灰区',
};
//合并行规则列表
const mergerRowRuleList = ref([] as any);
//存储传给措施的原因列下标
const reasonIndex = ref({} as any);
//是否更新
const isUpdate = ref(false);
//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    title: '5Why',
    columns,
    canResize: false,
    showTableSetting: false,
    pagination: false,
    actionColumn: {
      title: '操作',
      width: 80,
      fixed: 'right'
    },
  },
})

const [registerTable, { getDataSource, setTableData, getColumns, setColumns, insertTableDataRecord, deleteTableDataRecord }] = tableContext;

/**
 * 操作栏
 */
function getTableAction(record, index) {
  return [
    {
      label: '删除',
      popConfirm: {
        title: '是否确认删除',
        confirm: handleDelete.bind(null, record, index),
        placement: 'topLeft',
      },
    },
  ]
}

//扩展5why
function handleAddWhy() {
  if (whyNum.value > 10) {
    msg.warning('填写5Why已达上限！');
    return;
  }
  //获取表格中的列
  const newWhyArr: BasicColumn[] = getColumns();
  //需要新添加的对象
  const newObject = [
    {
      title: `${whyNum.value}.为什么`,
      dataIndex: NUM_ENUM[whyNum.value].why,
      align: "center",
      width: columnWidth,
    },
    {
      title: `${whyNum.value}.原因`,
      dataIndex: NUM_ENUM[whyNum.value].reason,
      align: "center",
      width: columnWidth,
    }
  ];
  //getColumns()获取出来的列会多一列操作列，所以在最后一列前添加新列
  newWhyArr.splice(newWhyArr.length - 1, 0, ...newObject);
  setCustomCell(newWhyArr);
  whyNum.value++;
}

//新增措施
function handleAddMeasure(){
  //调用措施组件添加方法
  measureListRef.value.addMeasure(null, baseData.value.problem, baseData.value.type);
}

//删除事件
async function handleDelete(record, rowIndex) {
  const dataResource = getDataSource();
  const columnArr = getColumns();
  //如果不是最后一行数据，则处理规则，否则直接删除
  if (dataResource.length > 1) {
    //获取当前行的合并规则数据
    const currentRowRule = mergerRowRuleList.value[rowIndex];
    for (const key in currentRowRule) {
      if (currentRowRule[key] > 1) {
        //选中行的当前列合并规则大于1，则将下一行同一列合并规则等于当前值-1
        const nextRowRule = mergerRowRuleList.value[rowIndex + 1];
        nextRowRule[key] = currentRowRule[key] - 1;

        //将当前行合并的数据赋值给下一行，避免删除后合并数据消失
        let field = columnArr[key].dataIndex;
        dataResource[rowIndex + 1][field] = record[field];
      } else if (currentRowRule[key] == 0) {
        //如果当前值为0，则往上查找合并规则不为0的行
        await porcessingDeleteMergeRow(rowIndex - 1, key);
      }
    }
  }
  //删除当前行数据
  await deleteTableDataRecord(record.id);
  //删除当前行合并规则
  await mergerRowRuleList.value.splice(rowIndex, 1);
  //删除原因下标
  delete reasonIndex.value[record.id];
}

/**
 * 添加事件
 * @param record 行数据
 * @param index 行下标
 * @param column 列数据
 */
async function handleAddRow(record, index, column) {
  //如果没有传入参数，则模拟第一行数据1.为什么的点击行为
  if (null == record) {
    if (mergerRowRuleList.value.length <= 0) {
      getDefaultRowSpan(0, -1);
    }
    record = getDataSource()[0];
    index = 0;
    column = { customTitle: '1.' };
  }
  //计算当前点击列的列数
  const colIndex = column.customTitle.split('.')[0] * 2;
  porcessingAddMergeRow(record, index, colIndex);
}

//保存事件
async function handleSave(isTransformResponse) {
  try{
    saveLoading.value = true;
    //列配置数据
    const columnArr = getColumns();
    //5w表格数据
    const resonDtoList = cloneDeep(getDataSource());
    //详细表数据
    const fiveWhyDetailList = measureListRef.value.getMeasureData();
    //详细表校验
    if(!fiveWhyDetailList){ return false;}
    //处理合并数据，将合并行的数据赋值给下一行，避免保存后合并数据消失
    resonDtoList.forEach((row, rowIndex) => {
      //循环列数据
      columnArr.forEach((col, colIndex) => {
        const key = col.dataIndex as string;
        //非操作列、值不为空、合并规则大于1时，根据合并行数将当前列数据往下赋值
        if(mergerRowRuleList.value[rowIndex][colIndex] > 1 && '' != row[key] && 'action' != col?.dataIndex){
          for(let j = 1; j < mergerRowRuleList.value[rowIndex][colIndex]; j++){
            resonDtoList[rowIndex+j][key] = row[key];
          }
        }
      })
    })
    baseData.value.resonDtoList = resonDtoList;
    baseData.value.fiveWhyDetailList = fiveWhyDetailList;
    baseData.value.mergeRule = JSON.stringify(mergerRowRuleList.value);
    baseData.value.whyNum = whyNum.value;
    await saveOrUpdate(baseData.value, isUpdate.value, isTransformResponse);
    measureListRef.value.reloadTable();
    emit('close');
  } catch (error) {
    msg.error('保存失败！' + error)
  } finally{
    saveLoading.value = false;
  }
}

/**
 * 处理新增时的合并规则
 * @param record 当前行数据
 * @param rowIndex 行
 * @param colIndex 列
 */
async function porcessingAddMergeRow(record, rowIndex, colIndex) {
  //获取指定行的数据合并规则
  const currentRowRule = mergerRowRuleList.value[rowIndex];
  //点击列的合并行数
  const currentRowSpan = mergerRowRuleList.value[rowIndex][colIndex];
  //数据大于0时才添加新的合并规则
  if (getDataSource().length > 0) {
    //指定行后添加默认行数据合并规则
    await getDefaultRowSpan(rowIndex + currentRowSpan, colIndex);
  }
  for (const key in currentRowRule) {
    //当前值大于0，且列小于点击列，则当前合并值+1
    if (currentRowRule[key] > 0 && getDataSource().length > 0) {
      if (key <= colIndex) {
        currentRowRule[key]++;
      }
    } else if (currentRowRule[key] == 0) {
      //如果当前列为0，递归往上查找
      porcessingColRowSpn(rowIndex, key)
    }
  }

  //插入新行数据到点击行+合并行后的位置
  let newRecord = {} as any;
  newRecord.id = 'temp_' + buildUUID();
  newRecord.problem = baseData.value.problem;
  newRecord.problemDesc = baseData.value.problemDesc;
  await insertTableDataRecord(newRecord, rowIndex + currentRowSpan);
  //调用措施组件添加方法
  measureListRef.value.addMeasure(newRecord.id, newRecord.problem, baseData.value.type);
  //添加原因下标
  reasonIndex.value[newRecord.id] = 1;
}

//递归处理合并规则
function porcessingColRowSpn(rowIndex, colIndex) {
  //每次往上获取一行数据，如果当前列为0，则继续往上递归，否则当前列+1并返回结束循环
  const currentRowRule = mergerRowRuleList.value[rowIndex - 1];
  if (currentRowRule[colIndex] == 0) {
    porcessingColRowSpn(rowIndex - 1, colIndex)
  } else {
    currentRowRule[colIndex]++;
    return;
  }
}

/**
 * 获取一个默认的合并行数据规则，默认设置22列
 * @param rowIndex 初始为当前行，添加操作为点击行+点击行需要合并的行数
 * @param colIndex 当前列
 */
function getDefaultRowSpan(rowIndex, colIndex) {
  let obj = {};
  for (let i = 0; i < 22; i++) {
    //默认为1不合并其他行，如果小于当前列，则设置为0不进行渲染让其他行合并
    let num = 1;
    if (colIndex != -1 && i <= colIndex) {
      num = 0;
    }
    obj[i] = num;
  }
  //插入到指定行后的位置
  mergerRowRuleList.value.splice(rowIndex, 0, obj);
}

/**
 * 删除时递归往上查找列合并规则
 * @param rowIndex 
 * @param key 
 */
function porcessingDeleteMergeRow(rowIndex, key) {
  //超过下标直接retuen掉
  if (rowIndex < 0) {
    return;
  }
  //按下标获取合并规则
  const currentRowRule = mergerRowRuleList.value[rowIndex];
  //当前列的合并值大于1则当前值-1，等于0则往上递归，等于1不处理
  if (currentRowRule[key] > 1) {
    currentRowRule[key]--;
  } else if (currentRowRule[key] == 0) {
    porcessingDeleteMergeRow(rowIndex - 1, key)
  }
}

//设置合并
function setCustomCell(newWhyArr) {
  newWhyArr.forEach((item, colIndex) => {
    item.customCell = (record, index, column) => {
      //用当前行列坐标获取合并规则，如果当前列为0则不渲染，否则渲染合并行数
      return {
        rowSpan: mergerRowRuleList.value[index][colIndex],
        style: {
          'height': '86px'
        }
      };
    }
  });
  //重新设置表格columns数据
  setColumns(newWhyArr);
}

//原因列的change事件
function handleChange(e, column, index, record){
  setTimeout(() => {
    const num = parseInt(column.customTitle.split('.')[0]);
    if(reasonIndex.value[record.id] <= num){
      reasonIndex.value[record.id] = num;
      measureListRef.value.changeReason(record.id, record[column.dataIndex]);
    }
  }, 100);
}

function handleExportXls(){
  const columnMap = {} as any;
  for(const item of columns){
    columnMap[item.dataIndex] = item.title;
  }
  let newArr = [] as any;
  const tableData = cloneDeep(fiveWhyData.value);
  for(const item of tableData){
    let obj = {} as any;
    for(const key in columnMap){
      if(key != 'id'){
        obj[columnMap[key]] = item[key];
      }
    }
    newArr.push(obj);
  }
  //5why导出
  exportExcel('5WHY', newArr, null);
}

function handleExportXlsMeasure(){
  //调用措施导出功能
  measureListRef.value.onExportXlsss();
}

// 导出全部（5W + 措施）
function handleExportAll() {
  // 1. 获取5W数据
  const fiveWhyColumnMap = {} as any;
  for (const item of columns) {
    fiveWhyColumnMap[item.dataIndex] = item.title;
  }
  const fiveWhyHeaders = Object.values(fiveWhyColumnMap).filter((_, index) => Object.keys(fiveWhyColumnMap)[index] !== 'id');
  const fiveWhyExportData = [] as any;
  const tableData = cloneDeep(fiveWhyData.value);
  for (const item of tableData) {
    const row = [];
    for (const key in fiveWhyColumnMap) {
      if (key != 'id') {
        row.push(item[key]);
      }
    }
    fiveWhyExportData.push(row);
  }
  // 2. 获取措施数据
  const measureExportData = measureListRef.value.getExportData();
  const measureData = measureExportData.data.map(item => {
    const row = [];
    measureExportData.header.forEach(header => {
      row.push(item[header]);
    });
    return row;
  });
  // 3. 构建完整的导出数据：5W表头 + 5W数据 + 空行 + 措施表头 + 措施数据
  const exportData = [];
  // 添加5W数据
  exportData.push(...fiveWhyExportData);
  // 添加空行
  exportData.push([]);
  // 添加措施表头
  exportData.push(measureExportData.header);
  // 添加措施数据
  exportData.push(...measureData);
  // 导出
  exportExcelWithHeader('5WHY分析及措施', exportData, fiveWhyHeaders);
}

async function init(params){
  if(null == params || Object.keys(params).length === 0){
    handleAddRow(null, null, null);
    setCustomCell(getColumns());
    return;
  }
  //是否显示保存按钮
  showSaveBtn.value = params.showSaveBtn === 'hide' ? false : true;
  //复制重复引用参数
  citeSearchParams.value = params;
  let searchParams = {} as any;
  let baseObj = {} as any;
  if(params.type == 1){
    params.problemDesc = `pi名称：${params.fieldName}，目标值：${params.standardValue??''}，实际值：${params.actualValue??''}。颜色为：${COLOR_ENUM[params.color] || ''}`;
    params.problem = params.fieldName;
    baseObj = cloneDeep(params);
    //拼接pi的查询参数
    searchParams = { 
      tableCode: params.tableCode, 
      dataId: params.dataId, 
      fieldCode: params.fieldCode, 
      type: params.type 
    };
  } else if(params.type == 2 || params.type == 'kpi'){
    const kpiRowData = cloneDeep(params.kpiRowData);
    //拼接kpi分析的查询参数
    searchParams = {
      kpiCode: kpiRowData.kpiName.id,
      kpiColKey: kpiRowData.colKey,
      kpiYear: kpiRowData.year,
      type: 2,
    }

    //拼接kpi分析所需要的数据
    baseObj = {
      ...searchParams,
      problem: kpiRowData.kpiName.value,
      problemDesc: kpiRowData.text
    }
  }else{
    searchParams = { 
      dataId: params.dataId, 
      type: params.type,
    };
    baseObj = cloneDeep(params);
  }
  //5w数据
  let res = {} as any;
  //如果传进来的参数有数据则直接使用，否则从后台获取数据
  if(params.fiveWhyData){
    res = params.fiveWhyData;
  } else {
    //获取后台数据
    res = await queryData(searchParams);
  }
  if(res && res.id){
    showCancel.value = true;
    //根据why数量拓展why列
    if(res.whyNum && res.whyNum > whyNum.value){
      for(let i = 6; i < res.whyNum; i++){
        handleAddWhy();
      }
    }
    //设置合并行规则
    mergerRowRuleList.value = JSON.parse(res?.mergeRule || '');
    //设置表格数据
    setTableData(res.resonDtoList);
    //设置措施组件数据
    measureListRef.value.setMeasureData(res.fiveWhyDetailList);
    baseData.value = res;
    fiveWhyData.value = res.resonDtoList;
  } else {
    showCite.value = baseObj.type;
    //如果是kpi分析，且没有id说明是新增获取后台拼接的数据
    if(baseObj.type == 2){
      baseObj.problemDesc = res.problemDesc ? res.problemDesc : baseObj.problemDesc;
    }
    baseData.value = baseObj;
    handleAddRow(null, null, null);
  }
  setCustomCell(getColumns());
}
//取消分析
function handleCancel(){
  createConfirm({
    title: '取消分析',
    content: `确定要取消该5W分析吗？`,
    iconType: 'warning',
    onOk: async function(){
      try{
        //清空表格数据
        setTableData([]);
        //清空措施组件数据
        measureListRef.value.setMeasureData([]);
        //调用删除接口
        await deleteOne({id: baseData.value.id});
        showCancel.value = false;
        emit('close');
      } catch (error) {
        msg.error('保存失败！' + error)
      }
    },
  });
}
//重复引用
function handleCite(){
  if(showCite.value == 1){
    openCitePiModal(true, {
      params: citeSearchParams.value,
    });
  } else if(showCite.value == 2){
    openCiteKpiModal(true, {
      params: citeSearchParams.value,
    });
  }
}
//重复引用确认事件
function handleOk(params, fiveWhyData){
  console.log('params :>> ', params);
  // 处理引用数据
  fiveWhyData.id = 'temp';
  fiveWhyData.resonDtoList = JSON.parse(fiveWhyData.reasonJson);
  fiveWhyData.fiveWhyDetailList = [];
  fiveWhyData.dataId = params.dataId;
  for(const item of fiveWhyData.resonDtoList){
    item.id = 'temp_' + buildUUID();
    item.problemDesc = params.problemDesc;
  }
  params.fiveWhyData = fiveWhyData;
  init(params);
}

//暴露方法
defineExpose({
  init,
  fiveSave: handleSave,
})
</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}

:deep(.ant-table-cell .ant-table-cell-ellipsis) {
  min-height: 100px !important;
}

.box {
  height: 100%;
  display: flex;
  align-items: center;

  span {
    cursor: pointer;
    margin-left: 4px;
  }
}
</style>