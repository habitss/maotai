<template>
  <div style="display: flex;justify-content: space-between;width: 97%;margin: auto;">
    <div>
      <a-button type="primary" preIcon="ep:plus" @click="handleAdd" :disabled="'' != currentEditKeyRef" style="margin-right: 5px;">新增</a-button>
      <!-- <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls" style="margin-right: 5px;"> 导出</a-button>
      <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="onImportXlsByParams" style="margin-right: 5px;">导入</j-upload-button> -->
      <a-button type="primary" preIcon="ant-design:save-outlined" @click="saveData" :disabled="'' != currentEditKeyRef" style="margin-left: 5px; margin-right: 10px;">保存</a-button>
      <!--批量删除-->
      <a-dropdown v-if="delDataIndex.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                删除
              </a-menu-item>
            </a-menu>
          </template>
          <a-button>批量操作
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
      </a-dropdown>
      <span style="color:red;margin-left: 5px;">“PI 名称” 及 “指标类型” 无法执行复制粘贴操作</span>
    </div>
    <div>
      <span>共{{ total }}条数据</span>
    </div>
  </div>

  <!--Vtable-->
  <div class="table-container" ref="containerRef" style="width:100%;height: 100%;">
    <ListTable :options="vtableOption" :height="listTableHeight" ref="listTableRef"/>
  </div>



</template>
<script lang="ts" name="dataCalculation-standard-maintenance-list" setup>
import { ActionItem, BasicTable, BasicColumn, TableAction, EditRecordRow } from '/@/components/Table';
import { getList, addData, getExportUrl, deleteData,batchEditVal,batchDelete } from './standardMaintenance.api';
import { getFieldList } from '../manage/standardManage.api';
import { useListPage } from '/@/hooks/system/useListPage';
import { useMessage } from '/@/hooks/web/useMessage';
import cloneDeep from 'lodash-es/cloneDeep';
import { ref, reactive } from 'vue';
import { buildUUID } from '/@/utils/uuid';
import { VTable, ListTable } from '@visactor/vue-vtable';
import * as VTableEditors from '@visactor/vtable-editors';
import { getColor} from '/@/utils/common/piColorUtils';
import SelectEditor from './EditorSelect';
import { useCopyToClipboard } from '/@/hooks/web/useCopyToClipboard';
import { useI18n } from '/@/hooks/web/useI18n';
const { t } = useI18n();
//消息提示
const { createMessage: msg } = useMessage();

//父组件传值
const manageData = ref({} as any);
/**导入导出使用参数 */
const manageId = ref();
const tableName = ref();

//pi名称下拉原始数据
const fieldArray = ref([] as any);
const fieldMap = ref({});
//表格初始高度
const listTableHeight = ref(600);
//p数据量
const total = ref(0);
//指标类型
const indicatorTypeArray = ref([
  { itemVal: '实际值', itemKey: '实际值' }, //1
  { itemVal: '偏差值', itemKey: '偏差值' }]); //2

//************VTable************/
const inputEditor = new VTableEditors.InputEditor();
VTable.register.editor('input-editor', inputEditor);
//表格实例
const listTableRef = ref();
//表格组件数据(通过表头获取)
const cellComponentsList = ref([] as any);
//注册下拉框编辑器
const SelectEditorObject = new SelectEditor(cellComponentsList.value, indicatorTypeArray.value, []);
VTable.register.editor('select-editor', SelectEditorObject);


//编辑数据data集合
const editDataIndex = ref<any>([]);
//表头字段集合
const allColumns = ref<any>([]);
//当前页数据
const dataPage = ref<any>([]);
//删除数据集合
const delDataIndex = ref<any>([]);
//表格配置
const vtableOption = reactive({
  records: [] as any,
  columns: [] as any,
  enableLineBreak: true,
  autoWrapText: true,
  limitMaxAutoWidth: 600,
  heightMode: 'autoHeight',
  editCellTrigger: 'doubleclick',
  // widthMode: 'adaptive',
  autoFillWidth: true, //列宽撑满容器
  frozenColCount: 1,
  keyboardOptions: {
    copySelected: true,
    pasteValueToCell: true,
    selectAllOnCtrlA: true
  },
  theme: VTable.themes.ARCO.extends({
    scrollStyle: {
      visible: 'always',
      hoverOn: false,
      // barToSide: true
    }
  }),
  // customRender:customRenderFun,
  //editor: '' // 配置一个空的编辑器，以遍能粘贴到单元格中
});
const columns = ref([
  {
    field: 'isCheck',
    title: '',
    width: 20,
    headerType: 'checkbox',
    cellType: 'checkbox'
  },
  {
    title:'PI名称',
    field: 'fieldText',
    width: 'auto',
    editor: 'select-editor-piName',
    headerStyle: {
      textAlign: 'center',
      color: '#000000',
      fontWeight: 500,
      fontSize: 14,
      fontFamily: 'Microsoft YaHei'
    },
    style: (args)=>{
      let styleObj = {};
      //判断是否修改-添加修改后标注
      let colKey = allColumns.value[args.col]; //加上序号行
      let data = listTableRef.value.vTableInstance.getRecordByCell(args.col,args.row);
      if(data[colKey+"_marked"]){
        styleObj.marked = true;
      }
      return styleObj;
    }
  },
  {
    title:'指标类型',
    field: 'indicatorTypeText',
    width: 'auto',
    editor: 'select-editor',
    headerStyle: {
      textAlign: 'center',
      color: '#000000',
      fontWeight: 500,
      fontSize: 14,
      fontFamily: 'Microsoft YaHei'
    },
    style: (args)=>{
      let styleObj = {};
      //判断是否修改-添加修改后标注
      let colKey = allColumns.value[args.col]; //加上序号行
      let data = listTableRef.value.vTableInstance.getRecordByCell(args.col,args.row);
      if(data[colKey+"_marked"]){
        styleObj.marked = true;
      }
      return styleObj;
    }
  },
  {
    title:'红下',
    field: 'redDown',
    width: 'auto',
    editor: 'input-editor',
    fieldFormat: (record) => {
      const value = record.redDown;
      if (value == null || value === '') return value;
      const num = Number(value);
      if (isNaN(num)) return value;
      return Number.isInteger(num) ? num.toFixed(2) : String(value);
    },
    headerStyle: {
      textAlign: 'center',
      color: '#000000',
      fontWeight: 500,
      fontSize: 14,
      fontFamily: 'Microsoft YaHei',
      bgColor: getColor('red')
    },
    style: (args)=>{
      let styleObj = {};
      //判断是否修改-添加修改后标注
      let colKey = allColumns.value[args.col]; //加上序号行
      let data = listTableRef.value.vTableInstance.getRecordByCell(args.col,args.row);
      if(data[colKey+"_marked"]){
        styleObj.marked = true;
      }
      return styleObj;
    }
  },
  {
    title:'黄下',
    field: 'yellowDown',
    width: 'auto',
    editor: 'input-editor',
    fieldFormat: (record) => {
      const value = record.yellowDown;
      if (value == null || value === '') return value;
      const num = Number(value);
      if (isNaN(num)) return value;
      return Number.isInteger(num) ? num.toFixed(2) : String(value);
    },
    headerStyle: {
      textAlign: 'center',
      color: '#000000',
      fontWeight: 500,
      fontSize: 14,
      fontFamily: 'Microsoft YaHei',
      bgColor: getColor('yellow')
    },
    style: (args)=>{
      let styleObj = {};
      //判断是否修改-添加修改后标注
      let colKey = allColumns.value[args.col]; //加上序号行
      let data = listTableRef.value.vTableInstance.getRecordByCell(args.col,args.row);
      if(data[colKey+"_marked"]){
        styleObj.marked = true;
      }
      return styleObj;
    }
  },
   {
    title:'目标值',
    field: 'standardValue',
    width: 'auto',
    editor: 'input-editor',
    fieldFormat: (record) => {
      const value = record.standardValue;
      if (value == null || value === '') return value;
      const num = Number(value);
      if (isNaN(num)) return value;
      return Number.isInteger(num) ? num.toFixed(2) : String(value);
    },
    headerStyle: {
      textAlign: 'center',
      color: '#000000',
      fontWeight: 500,
      fontSize: 14,
      fontFamily: 'Microsoft YaHei',
      bgColor: getColor('green')
    },
    style: (args)=>{
      let styleObj = {};
      //判断是否修改-添加修改后标注
      let colKey = allColumns.value[args.col]; //加上序号行
      let data = listTableRef.value.vTableInstance.getRecordByCell(args.col,args.row);
      if(data[colKey+"_marked"]){
        styleObj.marked = true;
      }
      return styleObj;
    }
  },
  {
    title:'黄上',
    field: 'yellowUp',
    width: 'auto',
    editor: 'input-editor',
    fieldFormat: (record) => {
      const value = record.yellowUp;
      if (value == null || value === '') return value;
      const num = Number(value);
      if (isNaN(num)) return value;
      return Number.isInteger(num) ? num.toFixed(2) : String(value);
    },
    headerStyle: {
      textAlign: 'center',
      color: '#000000',
      fontWeight: 500,
      fontSize: 14,
      fontFamily: 'Microsoft YaHei',
      bgColor: getColor('yellow')
    },
    style: (args)=>{
      let styleObj = {};
      //判断是否修改-添加修改后标注
      let colKey = allColumns.value[args.col]; //加上序号行
      let data = listTableRef.value.vTableInstance.getRecordByCell(args.col,args.row);
      if(data[colKey+"_marked"]){
        styleObj.marked = true;
      }
      return styleObj;
    }
  },
  {
    title:'红上',
    field: 'redUp',
    width: 'auto',
    editor: 'input-editor',
    fieldFormat: (record) => {
      const value = record.redUp;
      if (value == null || value === '') return value;
      const num = Number(value);
      if (isNaN(num)) return value;
      return Number.isInteger(num) ? num.toFixed(2) : String(value);
    },
    headerStyle: {
      textAlign: 'center',
      color: '#000000',
      fontWeight: 500,
      fontSize: 14,
      fontFamily: 'Microsoft YaHei',
      bgColor: getColor('red')
    },
    style: (args)=>{
      let styleObj = {};
      //判断是否修改-添加修改后标注
      let colKey = allColumns.value[args.col]; //加上序号行
      let data = listTableRef.value.vTableInstance.getRecordByCell(args.col,args.row);
      if(data[colKey+"_marked"]){
        styleObj.marked = true;
      }
      return styleObj;
    }
  },
]);

const currentEditKeyRef = ref('');

/**
 * 创建操作列
 */
function createActions(record: EditRecordRow, column: BasicColumn): ActionItem[] {
  if (!record.editable) {
    return [
      {
        label: '编辑',
        disabled: currentEditKeyRef.value ? currentEditKeyRef.value !== record.id : false,
        onClick: handleEdit.bind(null, record),
      },
      {
        label: '删除',
        popConfirm: {
          title: '是否删除',
          confirm: handleDelete.bind(null, record, column),
        },
      },
    ];
  }
  return [
    {
      label: '保存',
      onClick: handleSave.bind(null, record, column),
    },
    {
      label: '取消',
      popConfirm: {
        title: '是否取消编辑',
        confirm: handleCancel.bind(null, record, column),
      },
    },
  ];
}

//开启行编辑
async function handleEdit(record: EditRecordRow) {
  await setPinameConfig(record.fieldName);
  currentEditKeyRef.value = record.id;
  record.onEdit?.(true);
}

//取消行编辑
function handleCancel(record: EditRecordRow) {
  currentEditKeyRef.value = '';
  record.onEdit?.(false, false);
}

//添加事件
async function handleAdd(){
  //在第一行插入一条新数据
  let obj = {
    id: 'temp_'+buildUUID(), 
    manageId: manageData.value.id,
    manageName: manageData.value.standardName,
    tableName: manageData.value.tableName,
    tableText: manageData.value.tableText,
    fieldText:'',
    fieldName:''
  }
  // dataPage.value.unshift(obj);
  editDataIndex.value.push(obj.id);
  //vTable数据变更
  listTableRef.value.vTableInstance.addRecords([obj], 0)
}

/**
 * 删除操作
 */
async function handleDelete(record){
  try {
    await deleteData(record.id);
    deleteTableDataRecord(record.id);
    msg.success({ content: '删除成功'});
  } catch (error) {
    msg.error({ content: '删除失败'});
  } 
}

//保存数据
async function handleSave(record: EditRecordRow) {
  // 校验
  msg.loading({ content: '正在保存...', duration: 0, key: 'saving' });
  const valid = await record.onValid?.();
  if (valid) {
    try {
      //处理数据
      const submitData = cloneDeep(record.editValueRefs) ?? {};
      submitData.manageId = record.manageId;
      submitData.manageName = record.manageName;
      submitData.tableName = record.tableName;
      submitData.tableText = record.tableText;
      //判断是否为新增数据
      if(record.id && !record.id.startsWith('temp_')){
        submitData.id = record.id;
      }
      const findField = fieldArray.value.find((item) => item.fieldName == submitData.fieldName);
      submitData.fieldText = findField.name;
      submitData.sort = findField.sort;

      //调用接口提交数据
      await addData(submitData);
      //修改表格编辑状态
      const pass = await record.onEdit?.(false, true);
      if (pass) {
        currentEditKeyRef.value = '';
      }
      msg.success({ content: '数据已保存', key: 'saving' });
      setPinameConfig('');
    } catch (error) {
      msg.error({ content: '保存失败', key: 'saving' });
    }
  } else {
    msg.error({ content: '请填写正确的数据', key: 'saving' });
  }
}

//保存数据(批量)
async function saveData() {
  //判断是否有数据更改
  let postDatas = dataPage.value.filter(item => {
    if (editDataIndex.value.includes(item.id)) {
      return true
    }
    return false
  })
  if (postDatas.length < 1) {
    msg.warning("暂无被修改的数据");
    return;
  }
  // 校验
  msg.loading({ content: '正在保存...', duration: 0, key: 'saving' });

  postDatas.forEach(item => {
    if(item.indicatorTypeText == '实际值'){
      item.indicatorType = '1'
    }else if(item.indicatorTypeText == '偏差值'){
      item.indicatorType = '2'
    }
    item.fieldName = fieldMap.value[item.fieldText]
  });


  try {
   await batchEditVal(postDatas)
    .then((res) => {

      if (res.success) {
          // const successMessage = res.message ? res.message : t('language.save');
          // msg.success(successMessage);
          msg.success({ content: '数据已保存', key: 'saving' });
          // searchQuery();
          editDataIndex.value=[];
          getData();
      } else {
          msg.warning(res.message);
      }
    })
    .finally(() => {
    });
  } catch (error) {
    msg.error({ content: '保存失败', key: 'saving' });
  }
}

const SelectEditorPiNameObject = ref();
/**
 * 初始化PI名称下拉数据
 */
async function initData(data){
  manageData.value = data.record;
  manageId.value = data.record.id;
  tableName.value = data.record.tableName;
  //获取数据
  await getData();
  //获取字段数据
  const res = await getFieldList({pageSize: 9999, tableName: data.record.tableName});
  fieldArray.value  = res.records.map(item => ({
    // itemVal: item.fieldName,
    itemVal: item.name,
    itemKey: item.name
  }));
  fieldMap.value = res.records.reduce((acc, item) => {
    acc[item.name] = item.fieldName; // 以 item.name 作为 key，item.fieldName 作为 value
    return acc;
  }, {}); // 初始值设为空对象 {}

  //注册下拉框编辑器
  SelectEditorPiNameObject.value = new SelectEditor(cellComponentsList.value, fieldArray.value, vtableOption.records);
  VTable.register.editor('select-editor-piName', SelectEditorPiNameObject.value);


  // setPinameConfig('');
  //储存表头
  allColumns.value = columns.value.map(item => item.field);


  vtableOption.columns = columns;

  //获取页面高度
  listTableHeight.value = (document.documentElement.scrollHeight || document.body.scrollHeight)-300;
  //初始化Vtable
  // 初始化VTable事件(修改事件)
  listTableRef.value.vTableInstance.on(VTable.ListTable.EVENT_TYPE.CHANGE_CELL_VALUE, arg => {
    if(arg.changedValue == arg.rawValue){
      return false;
    }
    handleChangeVtable(arg.changedValue,arg.row,arg.col);
  });

  //checkbox选中事件
  listTableRef.value.vTableInstance.on(VTable.ListTable.EVENT_TYPE.CHECKBOX_STATE_CHANGE, arg => {
    checkboxChangeVtable(arg.checked,arg.row,arg.col);
  });


  //复制事件copy_data
  listTableRef.value.vTableInstance.on(VTable.ListTable.EVENT_TYPE.COPY_DATA, arg => {
    console.log("复制:"+arg.copyData)
    copy(arg.copyData);
  })
}


 function handleChangeVtable(value, row, col) {
  let colKey = allColumns.value[col]; //加上序号行
  const target = dataPage.value[row-1]; //减去表头层数
  let data = listTableRef.value.vTableInstance.getRecordByCell(col,row);
  //右上角标注
  data[colKey+"_marked"] = true
  editDataIndex.value.push(target.id);
}


//获取数据
async function getData(){
  const res = await getList({pageSize: 9999,tableName:tableName.value,manageId: manageId.value});
  total.value = res.records.length;
  res.records.forEach(item => {
    if(item.indicatorType == '1'){
      item.indicatorTypeText = '实际值'; 
    }else if(item.indicatorType == '2'){
       item.indicatorTypeText = '偏差值'; 
    }
  });

  vtableOption.records = res.records;
  dataPage.value = res.records;
}


//设置pi下拉组件
function setPinameConfig(field){
  //去除重复对象
  const newArr = fieldArray.value.filter(item1 => 
    !getRawDataSource()?.records?.some(item2 => item2.fieldName === item1.fieldName && item2.fieldName !== field)
  );
  //设置组件参数
  columns[1].editComponentProps= {
    options: newArr,
    fieldNames: {label: 'name', value: 'fieldName'},
    filterOption: (input, option) => handleFilterOption(input, option, 'name'),
    showSearch: true,
    getPopupContainer: (node) => document.body,
  };
  //设置对照值
  columns[1].editValueMap= (value) => {
    const obj = fieldArray.value.find((item) => item.fieldName == value)
    return obj ? obj.name : '';
  };
  setColumns(columns);
}

/**
 *下拉框搜索 
 */
function handleFilterOption(input, option, searchField) {
  return option[searchField].toLowerCase().indexOf(input.toLowerCase()) >= 0;
}

//复制内容存到剪贴板
async function copy(value){
  useCopyToClipboard(value);
}

//记录选中事件(删除)
function checkboxChangeVtable(value, row, col) {
    if((row === 0) && col === 0){
      //全选
      if(value){
        //全选
        dataPage.value.forEach(function(obj) {
          console.log(obj+"-----------------------")
          delDataIndex.value.push(obj.id);
        });
      }else{
        //全取消
        delDataIndex.value=[]
      }
    }else{
      const target = dataPage.value[row-1]; //减去表头层数
      const index = delDataIndex.value.findIndex(item => item === target.id);
      if (index!== -1) {
        // 如果存在，移除该id
        delDataIndex.value.splice(index, 1);
      } else {
        // 如果不存在，添加该id
        if(target.id){
          delDataIndex.value.push(target.id);
        }
      }
      //delDataIndex.value.push(target.id);
    }
    
}
//批量删除
async function batchHandleDelete(){
  await batchDelete({data:delDataIndex.value.filter(id => !id.startsWith('temp_')).join()}, handleSuccess);
}

/**
 * 成功回调
 */
async function handleSuccess() {
  
  console.log('delDataIndex.value :>> ', delDataIndex.value);
  console.log('vtableOption.records>>>>', vtableOption.records)
  vtableOption.records = await vtableOption.records.filter(item => !delDataIndex.value.includes(item.id))
  SelectEditorPiNameObject.value.setCellComponentsList(cellComponentsList.value, vtableOption.records);
  // vtableOption.records = [];
  // await getData();
}

// 公开方法，以便父组件可以访问
defineExpose({
  initData,
});
</script>
<style scoped>
/** 时间和数字输入框样式 */
:deep(.ant-input-number) {
  width: 100%;
}

:deep(.ant-picker) {
  width: 100%;
}

/** 下拉框默认文字颜色 */
:deep(.ant-select-selection-placeholder) {
  color: #BEBEBE !important;
}

/** 操作列提示窗口样式 */
:deep(.ant-popover-buttons){
  display: flex;
}
</style>