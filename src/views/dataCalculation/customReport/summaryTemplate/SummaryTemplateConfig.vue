<template>
  <div style="height: 100%; width: 100%;" loading-tip="加载中...">
    <div v-if="actionFlag === 'config'">
      <BasicForm @register="registerForm" name="ActionPlanProblemTypeForm">
        <template #buttonGroup="">
          <ImportExcelComponent style="display: inline-block;" :excelCol="vtableOption.columns"
            @getTableDate="getTableData" @getOriginalData="">
          </ImportExcelComponent>
          <a-button type="primary" v-on:click="handleInitTableData" preIcon="ep:plus">初始化</a-button>
        </template>

        <template #queryFieldSlot="">
          <QueryFieldList ref="queryFieldListRef"></QueryFieldList>
        </template>
      </BasicForm>
    </div>
    <div v-show="isVisible" class="table-container" ref="containerRef" 
      :style="actionFlag === 'config' ? 'height: calc(100% - 92px);' : 'height: 100%;'">
      <ListTable class="vtableList" ref="listTableRef" :options="vtableOption" :height="listTableHeight"
        @onDropdownMenuClick="handleDropdownMenuClick" @onContextMenuCell="handleContextMenuCell"
        @onResizeColumnEnd="handleResizeColumnEnd">
        <Menu menuType="html" v-if="actionFlag === 'config'" :contextMenuItems="getMenuConfig" />
      </ListTable>
    </div>
  </div>

  <!--特殊文本配置抽屉-->
  <SpecialTextDrawer @register="registerSpecialTextDrawer" @success="handleSpecialTextOk"
     :showFooter="true" />
  <!--颜色选择配置抽屉-->
  <ColorDrawer @register="colorDrawer" @success="colorDrawerOk" :showFooter="true" />
  <!--字体样式-->
  <FontDrawer @register="fontDrawer" @success="fontDrawerOk" :showFooter="true" />
</template>

<script lang="ts" setup name="summary-template-config">
import { nextTick, onMounted, reactive, ref } from 'vue';
import { VTable, ListTable, Menu } from '@visactor/vue-vtable';
import { BasicForm, useForm } from '/@/components/Form/index';
import { useMessage } from '/@/hooks/web/useMessage';
import { useDrawer } from '/@/components/Drawer';
import { list as getPiFieldList } from '@/views/dataCalculation/pi/allocationTable/AllocationTableField.api';
import { formSchema } from './SummaryTemplateConfig.data';
import { cloneDeep } from 'lodash-es';
import { useLoading } from '/@/components/Loading';
import { importExcel } from '/@//utils/excelUtils/customImportExcel';
import {downloadExcel, exportVTableToExcel} from "@visactor/vtable-export";
import * as VTableEditors from '@visactor/vtable-editors';
import QueryFieldList from './components/QueryFieldList.vue';
import dayjs from 'dayjs';
import ImportExcelComponent from '/@/views/dataCalculation/customReport/customReport/components/importExcelComponent.vue';
import SpecialTextDrawer from '/@/views/dataCalculation/customReport/customReport/components/SpecialTextDrawer.vue';
import ColorDrawer from '/@/views/dataCalculation/customReport/customReport/components/colorDrawer.vue';
import FontDrawer from '/@/views/dataCalculation/pi/allocationTable/components/fontDrawer.vue';

//消息提示
const { createMessage: msg } = useMessage();
//是否显示表格
const isVisible = ref(false);
//加载中组件
const [openFullLoading, closeFullLoading] = useLoading({
  tip: '加载中...',
});
//查询字段列表ref对象
const queryFieldListRef = ref();
//特殊文本配置抽屉
const [registerSpecialTextDrawer, { openDrawer: openSpecialTextDrawer }] = useDrawer();
//字体设置
const [fontDrawer, { openDrawer:openFontDrawer }] = useDrawer();
//颜色设置
const [colorDrawer, { openDrawer: openColorDrawer }] = useDrawer();
//父级数据
const parentData = ref({} as any);
//操作标识,config：配置，add：新增，edit：编辑，detail：详情
const actionFlag = ref('');
//填报数据
const fillInList = ref([] as any);
//表格父容器实例
const containerRef = ref();
//表格实例
const listTableRef = ref();
//表格初始高度
const listTableHeight = ref(400);
//所有配置合集
const reportConfig = ref([] as any);
//已配置的合并单元格数据
const mergeCellList = ref([] as any);
//已配置的单元格样式数据
const cellStyleConfigList = ref([] as any);
//已配置过的特殊文本
const specialTextConfigData = ref({} as any);
//填报项记录
const fillInRecordsHistory = ref({})
//查询类型的pi字段数据
const piQueryFieldOptions = ref({} as any);

//注册输入框编辑器
const inputEditor = new VTableEditors.InputEditor();
VTable.register.editor('input-editor', inputEditor);

//表单配置
const [registerForm, { getFieldsValue, setFieldsValue, validate }] = useForm({
  labelWidth: 150,
  schemas: formSchema,
  showActionButtonGroup: false,
  baseColProps: { span: 6 },
  wrapperCol: { span: 24 }
});

//表格配置
const vtableOption = reactive({
  showHeader: false,
  columns: [] as any,
  records: [] as any,
  theme: VTable.themes.DEFAULT.extends({
    scrollStyle: {
      visible: 'always'
    }
  }),
  editCellTrigger: 'click',
  columnResizeMode: 'all',
  frozenRowCount: 0,
  frozenColCount: 0,
  editor: () => {
    //如果是配置页面则未绑定组件的单元格也开启编辑功能
    if (actionFlag.value === 'config') {
      return 'input-editor';
    }
  },
  customMergeCell: (col, row, table) => {
    //根据配置的合并数据合并单元格
    for (let item of mergeCellList.value) {
      if (col >= item.startCol && col <= item.endCol && row >= item.startRow && row <= item.endRow) {
        return {
          text: item.dataValue,
          range: {
            start: {
              col: item.startCol,
              row: item.startRow
            },
            end: {
              col: item.endCol,
              row: item.endRow
            }
          },
        };
      }
    }
  },
});

function getMenuConfig(){
  //右键菜单配置
  let contextMenuItems = [] as any;
  if(actionFlag.value !== 'config'){
    return contextMenuItems;
  }
  contextMenuItems = [
    {
      text: '设置单元格样式',
      children: [
        {
          text: '左对齐',
          menuKey: 'setStyle_alignLeft'
        },
        {
          text: '居中',
          menuKey: 'setStyle_alignCenter'
        },
        {
          text: '右对齐',
          menuKey: 'setStyle_alignRight'
        },
        {
          text: '背景色',
          menuKey: 'setStyle_bgColor'
        },
        {
          text: '字体样式',
          menuKey: 'setStyle_font'
        },
      ]
    },
    { type: 'split' },
    { text: '设置特殊文本', menuKey: 'specialText' },
    { type: 'split' },
    { text: '合并单元格', menuKey: 'mergeCell' },
    { text: '取消合并单元格', menuKey: 'unMergeCell' },
    { type: 'split' },
    { text: '新增行', menuKey: 'addRow' },
    { text: '删除行', menuKey: 'deleteRow' },
    { type: 'split' },
    { text: '新增列', menuKey: 'addCol' },
    { text: '删除列', menuKey: 'deleteCol' },
  ];
  return contextMenuItems;
}

/**
 * 初始化数据
 * @param record 模板数据
 * @param flag 是否为配置
 * @param fillInData 填报数据
 */
async function initData(record, flag, fillInRecords, getFieldsValue) {
  openFullLoading();
  isVisible.value = false;
  parentData.value = record;
  actionFlag.value = flag;
  const configData = record.reportConfig ? JSON.parse(record.reportConfig) : '';
  reportConfig.value = configData;
  vtableOption.columnResizeMode = flag === 'config' ? 'all' : 'none';
  fillInRecordsHistory.value = fillInRecords ? fillInRecords : {};
  specialTextConfigData.value = configData?.specialTextConfigData;
  //重置数据
  await restData(configData, fillInRecords);
  //设置特殊文本
  await setSpecialText(getFieldsValue);
  //获取映射表字段数据
  await getData(record);
  //加载样式
  await loadCellStyle();
  //事件绑定
  bindEvent();
  //渲染表格
  await listTableRef.value.vTableInstance.renderWithRecreateCells();
  isVisible.value = true;
  closeFullLoading();
}

//重置表格数据
function restData(reportConfigJson, fillInRecords) {
  if(!reportConfigJson){
    return;
  }
  if(actionFlag.value === 'config' && reportConfigJson?.configForm){
    //设置表单数据
    nextTick(async () => {
      await setFieldsValue(reportConfigJson?.configForm)
      //设置查询字段列表数据
      queryFieldListRef.value.reloadData(getFieldsValue().queryField);
    });
  }
  //将cellStyleConfigList转换为map，key为row_col，value为textAlign
  const textAlignMap = {};
  reportConfigJson.cellStyleConfigList.forEach(item => {
    const key = `${item.row}_${item.col}`;
    const textAlign = item.style?.textAlign;
    textAlignMap[key] = textAlign ?? 'left';
  });

  let configFormData = reportConfigJson.configForm;
  let columnsArr = reportConfigJson ? reportConfigJson.columns : [];
  columnsArr.forEach((item, index) => {
    //设置列的样式
    item.style = { textAlign: textAlignMap[`${configFormData.startRow-2}_${index}`] };
    item.mergeCell = configFormData.mergeCell == 1;
  });
  vtableOption.frozenRowCount = configFormData.startRow-1;
  vtableOption.frozenColCount = configFormData.startCol-1;
  vtableOption.columns = columnsArr;
  mergeCellList.value = reportConfigJson ? reportConfigJson.mergeCellList : [];
  cellStyleConfigList.value = reportConfigJson ? reportConfigJson.cellStyleConfigList : [];
  //将数据转换为列对应的数据
  let newRecords = [] as any;
  if(fillInRecords && fillInRecords.length > 0){
    if(Object.keys(fillInRecords[0]).length > columnsArr.length){
      msg.error('sql查询列与表头列数量不一致');
      return;
    }
    fillInRecords.forEach((item) => {
      let newItem = {} as any;
      let num = -1;
      for(let key in item) {
        newItem[columnsArr[configFormData.startCol+num].field] = item[key];
        num++;
      }
      newRecords.push(newItem);
    });
  }
  if(reportConfigJson?.records){
    vtableOption.records = [
      ...reportConfigJson.records,
      ...newRecords
    ];
  }

  fillInList.value = [];
}

//获取数据
async function getData(record) {
  let res = {} as any;
  //类型等于2则获取pi的字段映射
  if (record.reportType > 1) {
    res = await getPiFieldList({ allocationTableId: record.dataId, isList: 1, pageSize: 1000 });

    if(actionFlag.value === 'config'){
      //查询字段下拉
      nextTick(() => {
        let options = [] as any;
        res.records.forEach(item => {
          if(item.isQuery == 1){
            options.push({
              label: item.name,
              value: item.fieldName,
            });
          }
        });
        piQueryFieldOptions.value = options;
      });
    }
  }
}

//设置特殊文本
function setSpecialText(getFieldsValue){
  if(actionFlag.value === 'config'){
    return;
  }
  let mergeDataObject = {};
  mergeCellList.value.forEach((item, index) => {
    item.index = index;
    mergeDataObject[item.startRow+'_'+item.endRow+'_'+item.startCol+'_'+item.endCol] = item;
  })

  for(const key in specialTextConfigData.value){
    let item = specialTextConfigData.value[key];
    const fieldArr = item.mappingColumn.split(",");
    let newStr = '';
    fieldArr.forEach((field) => {
      if(getFieldsValue[field]){
        if(newStr){
          newStr += '、';
        }
        newStr += getFieldsValue[field];
      }
    });
    item.text = item.text.replace("#{}", newStr);
    if(item.mergeData && item.mergeData.dataValue){
      const mergeCellObject = item.mergeData;
      for (let row = mergeCellObject.startRow; row <= mergeCellObject.endRow; row++) {
        for (let col = mergeCellObject.startCol; col <= mergeCellObject.endCol; col++) {
          vtableOption.records[row][vtableOption.columns[col].field] = item.text;
        }
      }
      let mergeKey = mergeCellObject.startRow+'_'+mergeCellObject.endRow+'_'+mergeCellObject.startCol+'_'+mergeCellObject.endCol;
      let mergeIndex = mergeDataObject[mergeKey].index;
      mergeCellList.value[mergeIndex].dataValue = item.text;
    } else {
      vtableOption.records[item.row][vtableOption.columns[item.col].field] = item.text;
    }
  }
}

//初始化表格行列
function handleInitTableData() {
  if (vtableOption.columns.length > 0) {
    msg.warning('表格已有数据，不能初始化')
    return;
  }
  let columnList = [] as any;
  let recordList = [] as any;
  for (let i = 0; i < 10; i++) {
    columnList.push({ field: 'field_' + i, title: 'title_' + i, width: 140 });
    recordList.push({ });
  }
  vtableOption.columns = columnList;
  vtableOption.records = recordList;
}

//右键下拉菜单点击事件监听
async function handleDropdownMenuClick(...args) {
  if (args[0].menuKey == 'mergeCell') {
    //合并单元格
    await handleMergeCells();
  } else if (args[0].menuKey == 'unMergeCell') {
    //取消合并单元格
    await handleUnmergeCells();
  } else if (args[0].menuKey == 'addRow') {
    //新增行
    await handleAddRow(args[0]);
  } else if (args[0].menuKey == 'addCol') {
    //新增列
    await handleAddCol(args[0]);
  } else if (args[0].menuKey == 'deleteRow') {
    //删除行
    await handleDeleteRow(args[0]);
  } else if (args[0].menuKey == 'deleteCol') {
    //删除列
    await handleDeleteCol(args[0]);
  } else if (args[0].menuKey == 'specialText') {
    //设置特殊文本
    await handleSpecialTextDrawerOpen(args[0]);
  } else if (args[0].menuKey.includes('setStyle')) {
    //设置样式
    await handleSetStyle(args[0]);
  }
}

//鼠标右键点击事件监听
function handleContextMenuCell(...args) {
  if (args[0].cells.length == 1 && args[0].cells[0].length == 1) {
    //选中右键所在单元格
    listTableRef.value.vTableInstance.selectCell(args[0].col, args[0].row);
  }
}

//右键菜单合并单元格事件
function handleMergeCells() {
  try{
    //合并前取消编辑状态，避免合并后还在编辑状态，造成取值错误
    listTableRef.value.vTableInstance.completeEditCell();

    //获取选中单元格
    const selectCells = listTableRef.value.vTableInstance.getSelectedCellInfos();
    //判断是否选中或者是否一行选择两列
    if (selectCells.length == 0 || (selectCells.length == 1 && selectCells[0].length < 2)) {
      msg.warning('请至少选中两个单元格');
      return;
    }

    //判断是否只选择了一行数据
    if (selectCells.length == 1 && selectCells[0].length > 1) {
      for (let item of selectCells) {
        //判断是否隔列选择
        for (let i = 0; i < item.length; i++) {
          if (i > 0 && (item[i].col - item[i - 1].col) != 1) {
            msg.warning('请选择正确的单元格合并');
            return;
          }
        }
      }
    } else {
      let tempLength = 0;
      let tempRow = -1;
      for (let item of selectCells) {
        if (tempRow >= 0 && (item[0].row - tempRow) != 1) {  //判断隔行选择
          msg.warning('请选择正确的单元格合并');
          return;
        } else if (tempLength != 0 && item.length != tempLength) { //判断正确选择后，单独选择其他单元格
          msg.warning('请选择正确的单元格合并');
          return;
        }
        tempLength = item.length;
        tempRow = item[0].row;
      }
    }

    //判断是否对已经合并单元格再次合并
    for (let mergeItem of mergeCellList.value) {
      for (let rowItem of selectCells) {
        for (let colItem of rowItem) {
          if (colItem.col >= mergeItem.startCol && colItem.col <= mergeItem.endCol
            && colItem.row >= mergeItem.startRow && colItem.row <= mergeItem.endRow
          ) {
            msg.warning('不能重复合并单元格');
            return;
          }
        }
      }
    }

    //添加需要合并单元格
    mergeCellList.value.push({
      startCol: selectCells[0][0].col,
      endCol: selectCells[0][selectCells[0].length - 1].col,
      startRow: selectCells[0][0].row,
      endRow: selectCells[selectCells.length - 1][0].row,
      dataValue: selectCells[0][0].dataValue ? selectCells[0][0].dataValue : ' '
    })

    //合并单元格将所有单元格数据变为第一个的数据，避免编辑时显示异常
    for (const item of selectCells) {
      for (const childItem of item) {
        vtableOption.records[childItem.row][childItem.field] = selectCells[0][0].dataValue;
      }
    }
    //重新渲染表格
    listTableRef.value.vTableInstance.renderWithRecreateCells();
  } catch (error) {
    console.log('合并单元格error :>> ', error);
  }
}

//右键菜单取消合并单元格事件
function handleUnmergeCells() {
  //获取选中单元格
  const selectCells = listTableRef.value.vTableInstance.getSelectedCellInfos();
  if (selectCells.length < 1) {
    msg.warning('请单击选中已合并的单元格');
    return;
  }
  const tempItem = selectCells[0][0];
  const newMergeCellList = mergeCellList.value.filter(mergeItem => {
    //排除需要取消的合并单元格
    if (tempItem.col >= mergeItem.startCol && tempItem.col <= mergeItem.endCol
      && tempItem.row >= mergeItem.startRow && tempItem.row <= mergeItem.endRow) {
      return false;
    }
    return true;
  })

  //清除特殊文本
  if(specialTextConfigData.value){
    delete specialTextConfigData.value[tempItem.row+'_'+tempItem.col];
  }

  if (newMergeCellList.length < mergeCellList.value.length) {
    mergeCellList.value = newMergeCellList;
    //重新渲染表格
    listTableRef.value.vTableInstance.renderWithRecreateCells();
  }
}

//新增行事件
async function handleAddRow(obj) {
  if (obj.row == 0) {
    return;
  }
  //判断新增行是否存在合并单元格，且已设置组件
  const validMerge = validMergeCell(obj.row, obj.col, 'addRow');
  if (validMerge) {
    msg.warning('当前行存在已设置组件的合并单元格，不能新增');
    return;
  }

  //处理已合并单元格配置的row值
  for (let item of mergeCellList.value) {
    if (obj.row <= item.startRow) {
      //插入行小于等于合并单元格开始行，则开始行结束行都需要+1
      item.startRow = item.startRow + 1;
      item.endRow = item.endRow + 1;
    } else if (obj.row > item.startRow && obj.row <= item.endRow) {
      //插入行大于开始行，小于等于结束行，则结束行+1
      item.endRow = item.endRow + 1;
    }
  }

  listTableRef.value.vTableInstance.addRecord(vtableOption.records[1], obj.row);

  // 调整现有样式数据
  let tempStyleList = [] as any;
  for (const item of cellStyleConfigList.value) {
    if (item.row >= obj.row) {
      //卸载样式
      listTableRef.value.vTableInstance.registerCustomCellStyle(item.id, null)
      //调整已存在数据
      item.id = 'style-id-' + (item.row + 1) + '-' + item.col;
      item.row = item.row + 1;
    }
    tempStyleList.push(item);
  }
  cellStyleConfigList.value = tempStyleList;

  //重新渲染样式
  await loadCellStyle();
}

//新增列事件
async function handleAddCol(obj) {
  if (obj.col == 0) {
    return;
  }
  //判断新增列是否存在合并单元格，且已设置组件
  const validMerge = validMergeCell(obj.row, obj.col, 'addCol');
  if (validMerge) {
    msg.warning('当前列存在已设置组件的合并单元格，不能新增');
    return;
  }
  //获取时间戳转换成编码
  const id = dayjs().valueOf().toString(36);
  //处理已合并单元格配置的col值
  for (let item of mergeCellList.value) {
    if (obj.col <= item.startCol) {
      //插入列小于等于合并单元格开始列，则开始列结束列都需要+1
      item.startCol = item.startCol + 1;
      item.endCol = item.endCol + 1;
    } else if (obj.col > item.startCol && obj.col <= item.endCol) {
      //插入列大于开始列，小于等于结束列，则结束列+1
      item.endCol = item.endCol + 1;
    }
  }

  //添加列操作
  let newColumns = [
    ...vtableOption.columns.slice(0, obj.col),
    ...[{ field: id, title: id, width: 100 }],
    ...vtableOption.columns.slice(obj.col)
  ];
  vtableOption.columns.splice(obj.col, 0, { field: id, title: id, width: 140 });

  // 调整现有样式数据
  let tempStyleList = [] as any;
  for (const item of cellStyleConfigList.value) {
    if (item.col >= obj.col) {
      //卸载样式
      listTableRef.value.vTableInstance.registerCustomCellStyle(item.id, null)
      //调整已存在样式
      item.id = 'style-id-' + item.row + '-' + (item.col + 1);
      item.col = item.col + 1;
    }
    tempStyleList.push(item);
  }
  cellStyleConfigList.value = tempStyleList;
  //重新渲染样式
  await listTableRef.value.vTableInstance.updateColumns(newColumns);
  await loadCellStyle();
}

//删除行事件
async function handleDeleteRow(obj) {
  // 处理已合并单元格配置的row值
  for (let item of mergeCellList.value) {
    //删除行小于等于结束行才处理合并单元格
    if (obj.row <= item.endRow) {
      //删除行小于合并单元格开始行，则开始行需要-1
      if (obj.row < item.startRow) {
        item.startRow = item.startRow - 1;
      }
      item.endRow = item.endRow - 1;
    }
  }

  // 调整现有样式数据
  let tempStyleList = [] as any;
  for (const item of cellStyleConfigList.value) {
    //卸载样式
    listTableRef.value.vTableInstance.registerCustomCellStyle(item.id, null)
    if (item.row != obj.row) {
      if (item.row > obj.row) {
        //调整已存在数据
        item.id = 'style-id-' + (item.row - 1) + '-' + item.col;
        item.row = item.row - 1;
      }
      tempStyleList.push(item);
    }
  }
  cellStyleConfigList.value = tempStyleList;
  //重新渲染样式
  await loadCellStyle();

  listTableRef.value.vTableInstance.deleteRecords([obj.row]);
}

//删除列事件
async function handleDeleteCol(obj) {
  //处理已合并单元格配置的col值
  for (let item of mergeCellList.value) {
    //删除列小于等于结束列才处理合并单元格
    if (obj.col <= item.endCol) {
      //删除列小于合并单元格开始列，则开始列需要-1
      if (obj.col < item.startCol) {
        item.startCol = item.startCol - 1;
      }
      item.endCol = item.endCol - 1;
    }
  }
  let newColumns = vtableOption.columns;
  // 使用splice删除指定下标的元素
  newColumns.splice(obj.col, 1);

  // 调整现有样式数据
  let tempStyleList = [] as any;
  for (const item of cellStyleConfigList.value) {
    //卸载样式
    listTableRef.value.vTableInstance.registerCustomCellStyle(item.id, null)
    if (item.col != obj.col) {
      if (item.col > obj.col) {
        //调整已存在样式
        item.id = 'style-id-' + item.row + '-' + (item.col - 1);
        item.col = item.col - 1;
      }
      tempStyleList.push(item);
    }
  }
  cellStyleConfigList.value = tempStyleList;
  //重新渲染样式
  await loadCellStyle();
  listTableRef.value.vTableInstance.updateColumns(newColumns);
}

//设置样式
function handleSetStyle(args) {
  const selectCells = listTableRef.value.vTableInstance.getSelectedCellInfos();
  let isColor = false;
  let isFont = false;
  for (const row of selectCells) {
    for (const col of row) {
      //判断是否为新样式配置
      let isAdd = true;
      //样式id
      const id = 'style-id-' + col.row + '-' + col.col;
      //初始化样式
      let cellStyle = {
        id,
        row: col.row,
        col: col.col,
        style: {}
      } as any;
      //循环判断当前单元格是否存在样式
      for (const styleObj of cellStyleConfigList.value) {
        if (styleObj.id == id) {
          cellStyle = styleObj;
          isAdd = false;
          break;
        }
      }
      if (args.menuKey == 'setStyle_fontBold') {
        //文字加粗
        cellStyle.style.fontWeight = 'bold';
      } else if (args.menuKey == 'setStyle_alignLeft') {
        //文字左对齐
        cellStyle.style.textAlign = 'left';
      } else if (args.menuKey == 'setStyle_alignCenter') {
        //文字居中
        cellStyle.style.textAlign = 'center';
      } else if (args.menuKey == 'setStyle_alignRight') {
        //文字右对齐
        cellStyle.style.textAlign = 'right';
      } else if (args.menuKey == 'setStyle_marked') {
        //设置组件标记
        cellStyle.style.marked = args.marked;
      } else if (args.menuKey == 'setStyle_bgColor') {
        //需要设置背景色
        isColor = true;
      } else if (args.menuKey == 'setStyle_font'){
        //设置字体样式
        isFont = true;
      }
      //新配置添加至样式配置数据中
      if (isAdd) {
        cellStyleConfigList.value.push(cellStyle);
      }
      //注册样式配置
      listTableRef.value.vTableInstance.registerCustomCellStyle(id, cellStyle.style);
      //更新单元格样式
      listTableRef.value.vTableInstance.arrangeCustomCellStyle({ col: col.col, row: col.row }, id)
    }
  }
  if (isColor) {
    //设置背景色
    setColor(selectCells)
  }
  if(isFont){
    //设置字体
    setFont(selectCells)
  }
}
//设置特殊文本
function handleSpecialTextDrawerOpen(args) {
  openSpecialTextDrawer(true, {
    args,
    tableFieldData: piQueryFieldOptions.value,
    componentObject: specialTextConfigData.value ? specialTextConfigData.value[args.row+"_"+args.col] : {row: args.row, col: args.col},
    parentData: parentData.value,
  });
}

//回填特殊文本设置
async function handleSpecialTextOk(data) {
  let row = data.row;
  let col = data.col;
  // 修改数据匹配项的文字内容
  let tempRecord = cloneDeep(vtableOption.records);
  const mergeCellObject = validMergeCell(data.row, data.col, 'object');
  //如果是合并单元格，则需给所有单元格添加，否则添加当前单元格
  if (mergeCellObject) {
    for (let row = mergeCellObject.startRow; row <= mergeCellObject.endRow; row++) {
      for (let col = mergeCellObject.startCol; col <= mergeCellObject.endCol; col++) {
        tempRecord[row][vtableOption.columns[col].field] = data.text;
      }
    }
    mergeCellObject.dataValue = data.text;
    row = mergeCellObject.startRow;
    col = mergeCellObject.startCol;
  } else {
    tempRecord[data.row][vtableOption.columns[data.col].field] = data.text;
  }
  vtableOption.records = tempRecord;
  await loadCellStyle();
  //渲染表格
  listTableRef.value.vTableInstance.renderWithRecreateCells();
  
  // 确保 specialTextConfigData.value 存在并且是一个对象
  if (!specialTextConfigData.value) {
    specialTextConfigData.value = {};
  }
  specialTextConfigData.value[row + '_' + col] = {
    col: parseInt(col),
    row: parseInt(row),
    mappingColumn: data.mappingColumn,
    text: data.text,
    mergeData: mergeCellObject,
  };
}

//验证单元格是否在合并单元格内，在内返回合并单元格对象
function validMergeCell(row, col, type) {
  //返回object对象
  let mergeCellObject = null;
  for (const item of mergeCellList.value) {
    if (col >= item.startCol && col <= item.endCol
      && row >= item.startRow && row <= item.endRow
    ) {
      mergeCellObject = item;
      break;
    }
  }
  return mergeCellObject;
}

//注册处理样式
async function loadCellStyle() {
  for (const item of cellStyleConfigList.value) {
    //注册样式配置
    await listTableRef.value.vTableInstance.registerCustomCellStyle(item.id, item.style);
    //更新单元格样式
    listTableRef.value.vTableInstance.arrangeCustomCellStyle({ col: item.col, row: item.row }, item.id)
  }
}

async function getTableData(data) {
  //初始第一行数据，用于设置匹配项
  restData(null, null);
  const res = await importExcel(data);
  vtableOption.columns = res.colList;
  vtableOption.records = res.newArray;
  mergeCellList.value = res.mergesList;
}

//提供外部获取所有配置数据
async function getConfigData() {
  try {
    let values = await validate();
    values.queryField = queryFieldListRef.value.getDataSource();
    //点击确定是关闭表格所有的编辑状态，以便保存数据
    listTableRef.value.vTableInstance.completeEditCell();
    return {
      columns: vtableOption.columns,
      records: vtableOption.records,
      mergeCellList: mergeCellList.value,
      cellStyleConfigList: cellStyleConfigList.value,
      fillInList: fillInList.value,
      specialTextConfigData: specialTextConfigData.value,
      configForm: values
    };
  } catch (error) {
    return false;
  }
}

//列宽拖拽结束监听
function handleResizeColumnEnd(...args) {
  //操作的列
  let col = args[0].col;
  //操作后的宽度
  let colWidth = args[0].colWidths[col];
  //调整配置中的列宽度
  vtableOption.columns[col].width = colWidth;
}

onMounted(() => {
  // 监听父容器高度改变
  const resizeObserver = new ResizeObserver(entries => {
    for (let entry of entries) {
      listTableHeight.value = entry.contentRect.height;
    }
  });
  resizeObserver.observe(containerRef.value);
})

//设置单元格颜色
function setColor(selectCells) {
  openColorDrawer(true, { selectCells });
}
//颜色选择完成
function colorDrawerOk(color, selectCells) {
  for (const row of selectCells) {
    for (const col of row) {
      //判断是否为新样式配置
      let isAdd = true;
      //样式id
      const id = 'style-id-' + col.row + '-' + col.col;
      //初始化样式
      let cellStyle = {
        id,
        row: col.row,
        col: col.col,
        style: {} as any
      };
      //循环判断当前单元格是否存在样式
      for (const styleObj of cellStyleConfigList.value) {
        if (styleObj.id == id) {
          cellStyle = styleObj;
          isAdd = false;
          break;
        }
      }
      cellStyle.style.bgColor = color;
      //新配置添加至样式配置数据中
      if (isAdd) {
        cellStyleConfigList.value.push(cellStyle);
      }
      //注册样式配置
      listTableRef.value.vTableInstance.registerCustomCellStyle(id, cellStyle.style);
      //更新单元格样式
      listTableRef.value.vTableInstance.arrangeCustomCellStyle({ col: col.col, row: col.row }, id)
    }
  }
}
//设置字体样式
function setFont(selectCells){
  openFontDrawer(true, {selectCells,styleConfigList: cellStyleConfigList.value})
}
//字体选择完成
function fontDrawerOk(fontObj, selectCells){
  for(const row of selectCells){
    for(const col of row){
      //判断是否为新样式配置
      let isAdd = true;
      //样式id
      const id = 'style-id-'+col.row+'-'+col.col;
      //初始化样式
      let cellStyle = {
        id,
        row: col.row,
        col: col.col,
        style: {}
      };
      //循环判断当前单元格是否存在样式
      for(const styleObj of cellStyleConfigList.value){
        if(styleObj.id == id){
          cellStyle = styleObj;
          isAdd = false;
          break;
        }
      }
      cellStyle.style.color = fontObj.fontColor;
      cellStyle.style.fontSize = fontObj.fontSize;
      //字体粗细
      if(fontObj.fontWeight){
        cellStyle.style.fontWeight = 'bold'
      }else{
        cellStyle.style.fontWeight = 100
      }
      //新配置添加至样式配置数据中
      if(isAdd){
        cellStyleConfigList.value.push(cellStyle);
      }
      //注册样式配置
      listTableRef.value.vTableInstance.registerCustomCellStyle(id, cellStyle.style);
      //更新单元格样式
      listTableRef.value.vTableInstance.arrangeCustomCellStyle({ col: col.col, row: col.row }, id)
    }
  }
}

//事件绑定
function bindEvent(){
  //单元格编辑事件绑定，无法直接在组件上用on绑定，需单独声明绑定
  listTableRef.value.vTableInstance.on(VTable.ListTable.EVENT_TYPE.CHANGE_CELL_VALUE, (...args) => {
    let value = args[0].changedValue;
    //如果为对象代表下拉编辑器返回的数据，显示文本值
    if(typeof value === 'object'){
      value = value.text;
    }
    //判断编辑数据是否在合并单元格，修改合并单元格显示值
    const mergeCell = validMergeCell(args[0].row, args[0].col, 'object');
    if(null != mergeCell){
      mergeCell.dataValue = value;
    }else{
      const record = vtableOption.records[args[0].row];
      const field = vtableOption.columns[args[0].col].field;
      record[field] = value;
    }
  });
}


//导出
async function exportData(){
  const listTableInstance = listTableRef.value;
  if (!listTableInstance) {
    msg.warning("ListTable 实例未创建或未挂载");
  }
  //获取 VTable 实例的数据
  const tableInstance = listTableInstance.vTableInstance;
  //固定列行数
  let frozenColCount = vtableOption.frozenColCount;
  vtableOption.frozenColCount = 0
  downloadExcel(await exportVTableToExcel(tableInstance, {ignoreIcon: true, formatExportOutput: ({ cellType, cellValue, table, col, row }) => {
    if (cellType === 'checkbox') {
      if(row == 0){
        return "序号"
      }else{
        return row-1;
      }
    }
  }}), parentData.value.reportName);
  vtableOption.frozenColCount = frozenColCount;
  
}

// 暴露方法
defineExpose({
  initData,
  getConfigData,
  exportData,
});
</script>

<style lang="less" scoped>
/** 时间和数字输入框样式 */
:deep(.ant-input-number) {
  width: 100%;
}

.table-container {
  width: 100%;
  min-height: 400px;
}

.vtableList {
  width: 100%;
}

.ant-btn {
  margin-left: 10px;
  margin-bottom: 10px;
}
</style>