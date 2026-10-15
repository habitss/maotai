<template>
  <div class="table-container" ref="containerRef">
    <ListTable class="vtableList" ref="listTableRef" 
      :options="vtableOption"
      :height="listTableHeight"
    >
    <Menu menuType="html" :contextMenuItems="getMenuConfig" />
    </ListTable>
  </div>
  
</template>

<script lang="ts" setup>
import { onMounted, reactive, ref, nextTick } from 'vue';
import { VTable, ListTable, Menu } from '@visactor/vue-vtable';
import * as VTableEditors from '@visactor/vtable-editors';
import dayjs from 'dayjs';
import { list as getFieldList } from '@/views/dataCalculation/customReport/reportConfig/reportConfigField.api';
import { list as getPiFieldList } from '@/views/dataCalculation/pi/allocationTable/AllocationTableField.api';
import { useMessage } from '/@/hooks/web/useMessage';
import { useDrawer } from '/@/components/Drawer';
import { useCopyToClipboard } from '/@/hooks/web/useCopyToClipboard';
import {downloadExcel,exportVTableToExcel} from "@visactor/vtable-export";
const hasReopen = ref<boolean>(false);
import { red } from '@ant-design/colors';
import { isHexColor } from '/@/utils/color';
import { table } from 'console';
const departObject = ref<Object>({});
const color = ref('red');
// Emits声明
const emit = defineEmits(['rightClick', 'checkboxChange']);

//消息提示
const { createMessage: msg } = useMessage();

//填报项配置抽屉
const [registerDrawer, { openDrawer }] = useDrawer();
//颜色设置
const [colorDrawer, { openDrawer:openColorDrawer }] = useDrawer();
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
//已配置的合并单元格数据
const mergeCellList = ref([] as any);
//已配置的单元格编辑组件数据
const cellComponentsList = ref([] as any);
//已配置的单元格样式数据
const cellStyleConfigList = ref([] as any);
//已配置过的绑定表字段数据
const tableFieldData = ref([] as any);
//模版绑定表的所有字段数据，用于和已配置过的字段比较，避免字段重复绑定
const originalTableFieldData = ref([] as any);
//自定义报表部门名称
const customReportDeptName = ref<string>('');
//注册输入框编辑器
const inputEditor = new VTableEditors.InputEditor();
VTable.register.editor('input-editor', inputEditor);
//记录表头行数
const tableRow = ref(0);
//编辑数据data集合
const editDataIndex = ref<any>([]);
//编辑字段集合
const editFieldSet = ref<Set<any>>(new Set());
//表头字段集合
const allColumns = ref<any>([]);
//当前页数据
const dataPage = ref<any>([]);

//表格配置
const vtableOption = reactive({
  showHeader: false, //是否隐藏表头
  columns: [] as any,
  records: [] as any,
  theme: VTable.themes.DEFAULT.extends({
    scrollStyle: {
      visible: 'always',
      hoverOn: false,
      width: 10,
    }
  }),
  // editCellTrigger: 'click',
  editCellTrigger: 'doubleclick',
  columnResizeMode : 'all',
  keyboardOptions: {
    copySelected: true,
    pasteValueToCell: true,
    selectAllOnCtrlA: true
  },
  editor: (args)=>{
    //详情页面不开启任何编辑功能
    if(actionFlag.value != 'detail'){
      //将设置过组件的单元格绑定编辑器
      for(let item of cellComponentsList.value){
        if(item.col == args.col && item.row == args.row && item.isEdit != 0){
          if(item.componentType == 'input'){
            return 'input-editor';
          }else if(item.componentType == 'select'){
            return 'select-editor';
          }else if(item.componentType == 'datepicker'){
            return 'date-time-editor';
          }else if(item.componentType == 'dateTimePicker'){
            return 'date-time-editor';
          }else if(item.componentType == 'textarea'){
            return 'textarea-editor';
          }else if(item.componentType == 'selectUser'){
            return 'select-user-editor';
          }
        }
      }
      //如果是配置页面则未绑定组件的单元格也开启编辑功能
      if(actionFlag.value === 'config'){
        return 'input-editor';
      }
    }
  },
  customMergeCell: (col, row, table)=>{
    //根据配置的合并数据合并单元格
    for(let item of mergeCellList.value){
      if(col >= item.startCol && col <= item.endCol && row >= item.startRow && row <= item.endRow){
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


/**
 * 初始化数据
 * @param record 模板数据
 * @param flag 是否为配置
 */
async function initData(record,config) {
  allColumns.value = record.allColumns;
  listTableHeight.value = config.listTableHeight;
  let flag ='none'
  parentData.value = record;
  actionFlag.value = "none";
  const configData = record.reportConfig ? JSON.parse(record.reportConfig) : '';
  const columns = record.columns ? record.columns:{};
  const frozenColCount = record.frozenColCount ? record.frozenColCount:1;
  vtableOption.columnResizeMode = flag === 'config' ? 'all' : 'none';
  //重置数据
  await restData(configData,columns,frozenColCount);
  //获取映射表字段数据
  //await getData(record);
  //加载样式
  await loadCellStyle();
  //事件绑定
  bindEvent();
  //设置日期编辑器所需要的组件对象
  //EditorDatePickerObject.setCellComponentsList(cellComponentsList.value);
  //设置下拉编辑器所需要的组件对象
  //SelectEditorObject.setCellComponentsList(cellComponentsList.value);
  //加载填报数据
  //await loadFillInData(fillInRecords);
  //渲染表格
  await listTableRef.value.vTableInstance.renderWithRecreateCells();
}

//重置表格数据
function restData(reportConfigJson, columns, frozenColCount){
  let columnsArr = reportConfigJson ? reportConfigJson.columns : [];
  const mergedColumns = mergeObjectsByFieldOptimized(columnsArr,columns);
  // 在前面添加复选框列
  vtableOption.columns = [
    { field: 'isCheck', title: '', width: 40, headerType: 'checkbox', cellType: 'checkbox', style: { textAlign: 'center' } },
    ...mergedColumns
  ];
  vtableOption.records = reportConfigJson ? reportConfigJson.records : [];
  tableRow.value = vtableOption.records.length;

  let configFormData = reportConfigJson.configForm;
  vtableOption.frozenRowCount = tableRow.value;
  vtableOption.heightMode = 'autoHeight',
  vtableOption.autoWrapText = true,
  // 复选框列偏移+1
  mergeCellList.value = reportConfigJson ? reportConfigJson.mergeCellList.map(item => ({...item, startCol: item.startCol + 1, endCol: item.endCol + 1})) : [];
  
  // 合并表头行的复选框列
  if(tableRow.value > 1){
    mergeCellList.value.unshift({ startCol: 0, endCol: 0, startRow: 0, endRow: tableRow.value - 1, dataValue: '' });
  }
  cellComponentsList.value = reportConfigJson ? reportConfigJson.cellComponentsList.map(item => ({...item, col: item.col + 1})) : [];
  cellStyleConfigList.value = reportConfigJson ? reportConfigJson.cellStyleConfigList.map(item => ({...item, col: item.col + 1, id: 'style-id-'+item.row+'-'+(item.col+1)})) : [];
  // 合并后的复选框表头单元格取表头颜色
  let headerBgColor = '#EEF1F5';
  for(const item of cellStyleConfigList.value){
    if(item.row < tableRow.value && item.style?.bgColor){
      headerBgColor = item.style.bgColor;
      break;
    }
  }
  if(tableRow.value > 0){
    cellStyleConfigList.value.push({ id: 'checkbox-header-style', row: 0, col: 0, style: { bgColor: headerBgColor } });
  }
  fillInList.value = [];
  console.log('cellComponentsList.value :>> ', cellComponentsList.value);
}
//比较两个对象数组并根据 field 字段合并属性
function mergeObjectsByFieldOptimized(a, b, field = 'field') {
  // 创建 b 数组的映射，以 field 为键
  const bMap = new Map();
  b.forEach(item => bMap.set(item[field], item));
  
  return a.map(itemA => {
    const matchingItem = bMap.get(itemA[field]);
    if (matchingItem) {
      matchingItem.width = itemA.width;
      return {
        ...itemA,
        ...matchingItem
      };
    }
    return itemA;
  });
}

//事件绑定
function bindEvent(){
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

    // //右键事件
    listTableRef.value.vTableInstance.on(VTable.ListTable.EVENT_TYPE.DROPDOWN_MENU_CLICK, args => {
      console.log('dropdown_menu_click', args);
      rightClickEvent(args);
    });

    // //点击事件
    // listTableRef.value.vTableInstance.on(VTable.ListTable.EVENT_TYPE.CLICK_CELL, args => {
    //   console.log('click_cell', args);
    //   clickEvent(args);
    // });

    //复制事件copy_data
    listTableRef.value.vTableInstance.on(VTable.ListTable.EVENT_TYPE.COPY_DATA, arg => {
      console.log("复制:"+arg.copyData)
      copy(arg.copyData);
    })

}

//编辑后出发事件 值  行(固定列+数据)  列
function handleChangeVtable(value, row, col) {
  let colKey = allColumns.value[col];
  const target = dataPage.value[row-tableRow.value]; //减去表头层数
  let data = listTableRef.value.vTableInstance.getRecordByCell(col,row);
  //右上角标注
  data[colKey+"_marked"] = true
  editDataIndex.value.push(target.id);
  editFieldSet.value.add(colKey);
}

//记录选中事件(删除)
function checkboxChangeVtable(checked, row, col) {
  if(col !== 0) return;
  if(row < tableRow.value) {
    // 表头行 - 全选/全不选
    emit('checkboxChange', { checked, isAll: true });
  } else {
    const target = dataPage.value[row - tableRow.value];
    if(target) {
      emit('checkboxChange', { checked, isAll: false, id: target.id });
    }
  }
}

//右键
function rightClickEvent(args){
  //获取数据(整行)
  let rowData = listTableRef.value.vTableInstance.getRecordByCell(args.col, args.row);
  // let data = {
  //   tableId: customReportTableId.value,
  //   allocationId: tableParams.allocationId,
  //   dataId: rowData.id,
  //   tableName:customReportTableName.value,
  //   originalValue:rowData[args.field],
  //   fields: args.field,
  //   field:headField.value[args.field]
  // }
  emit('rightClick', args, rowData);
}

//点击(单击)
// function clickEvent(args){
//   //判断是否表头第一行
//   if(args.row === 0 && args.col !== 0){
//     let obj ={
//       tableName: customReportTableName.value,
//       fieldName: args.field,
//       name: args.dataValue
//     }
//     manageStandardModal.value.disableSubmit = false;
//     manageStandardModal.value.init(obj);
//   }
// }
//复制内容存到剪贴板
async function copy(value){
  useCopyToClipboard(value);
}


//右键菜单合并单元格事件
function handleMergeCells() {
  //合并前取消编辑状态，避免合并后还在编辑状态，造成取值错误
  listTableRef.value.vTableInstance.completeEditCell();

  //获取选中单元格
  const selectCells = listTableRef.value.vTableInstance.getSelectedCellInfos();
  //判断是否选中或者是否一行选择两列
  if(selectCells.length == 0 || (selectCells.length == 1 && selectCells[0].length < 2)){
    msg.warning('请至少选中两个单元格');
    return;
  }

  //判断是否只选择了一行数据
  if(selectCells.length == 1 && selectCells[0].length > 1){
    for(let item of selectCells){
      //判断是否隔列选择
      for(let i = 0; i < item.length; i++){
        if(i > 0 && (item[i].col - item[i-1].col) != 1){
          msg.warning('请选择正确的单元格合并');
          return;
        }
      }
    }
  } else {
    let tempLength = 0;
    let tempRow = -1;
    for(let item of selectCells){
      if(tempRow >= 0 && (item[0].row - tempRow) != 1){  //判断隔行选择
        msg.warning('请选择正确的单元格合并');
        return;
      }else if(tempLength != 0 && item.length != tempLength){ //判断正确选择后，单独选择其他单元格
        msg.warning('请选择正确的单元格合并');
        return;
      }
      tempLength = item.length;
      tempRow = item[0].row;
    }
  }

  //判断是否对已经合并单元格再次合并
  for(let mergeItem of mergeCellList.value){
    for(let rowItem of selectCells){
      for(let colItem of rowItem){
        if(colItem.col >= mergeItem.startCol && colItem.col <= mergeItem.endCol
          && colItem.row >= mergeItem.startRow && colItem.row <= mergeItem.endRow
        ){
          msg.warning('不能重复合并单元格');
          return;
        }
      }
    }
  }

  //添加需要合并单元格
  mergeCellList.value.push({
    startCol: selectCells[0][0].col, 
    endCol: selectCells[0][selectCells[0].length-1].col, 
    startRow: selectCells[0][0].row, 
    endRow: selectCells[selectCells.length-1][0].row,
    dataValue: selectCells[0][0].dataValue ? selectCells[0][0].dataValue : ' '
  })
  
  //合并单元格将所有单元格数据变为第一个的数据，避免编辑时显示异常
  for(const item of selectCells){
    for(const childItem of item){
      vtableOption.records[childItem.row][childItem.field] = selectCells[0][0].dataValue;
    }
  }
  //清除填报项
  removeCellComponent(selectCells[0][0].row, selectCells[0][0].col);
}

//右键菜单取消合并单元格事件
function handleUnmergeCells() {
  //获取选中单元格
  const selectCells = listTableRef.value.vTableInstance.getSelectedCellInfos();
  if(selectCells.length < 1){
    msg.warning('请单击选中已合并的单元格');
    return;
  }
  
  const tempItem = selectCells[0][0];
  const newMergeCellList = mergeCellList.value.filter(mergeItem => {
    //排除需要取消的合并单元格
    if(tempItem.col >= mergeItem.startCol && tempItem.col <= mergeItem.endCol 
      && tempItem.row >= mergeItem.startRow && tempItem.row <= mergeItem.endRow){
      return false;
    }
    return true;
  })

  //清除设置的组件
  removeCellComponent(tempItem.row, tempItem.col);

  if(newMergeCellList.length < mergeCellList.value.length){
    mergeCellList.value = newMergeCellList;
    //重新渲染表格
    listTableRef.value.vTableInstance.renderWithRecreateCells();
  }
}

//新增行事件
async function handleAddRow(obj){
  //判断新增行是否存在合并单元格，且已设置组件
  const validMerge = validMergeCell(obj.row, obj.col, 'addRow');
  if(validMerge){
    msg.warning('当前行存在已设置组件的合并单元格，不能新增');
    return;
  }

  //处理已合并单元格配置的row值
  for(let item of mergeCellList.value){
    if(obj.row <= item.startRow){
      //插入行小于等于合并单元格开始行，则开始行结束行都需要+1
      item.startRow = item.startRow+1;
      item.endRow = item.endRow+1;
    } else if (obj.row > item.startRow && obj.row <= item.endRow){
      //插入行大于开始行，小于等于结束行，则结束行+1
      item.endRow = item.endRow+1;
    }
  }

  //处理单元格编辑组件设置的row值
  for(let item of cellComponentsList.value){
    if(item.row >= obj.row){
      item.row = item.row+1;
    }
  }
  listTableRef.value.vTableInstance.addRecord({}, obj.row);
  
  // 调整现有样式数据
  let tempStyleList = [];
  for(const item of cellStyleConfigList.value){
    if(item.row >= obj.row) {
      //卸载样式
      listTableRef.value.vTableInstance.registerCustomCellStyle(item.id, null)
      //调整已存在数据
      item.id = 'style-id-'+(item.row+1)+'-'+item.col;
      item.row = item.row+1;
    }
    tempStyleList.push(item);
  }
  cellStyleConfigList.value = tempStyleList;
  //重新渲染样式
  await loadCellStyle();
}

//新增列事件
async function handleAddCol(obj){
  //判断新增列是否存在合并单元格，且已设置组件
  const validMerge = validMergeCell(obj.row, obj.col, 'addCol');
  if(validMerge){
    msg.warning('当前列存在已设置组件的合并单元格，不能新增');
    return;
  }
  //获取时间戳转换成编码
  const id = dayjs().valueOf().toString(36);
  //处理已合并单元格配置的col值
  for(let item of mergeCellList.value){
    if(obj.col <= item.startCol){
      //插入列小于等于合并单元格开始列，则开始列结束列都需要+1
      item.startCol = item.startCol+1;
      item.endCol = item.endCol+1;
    } else if (obj.col > item.startCol && obj.col <= item.endCol){
      //插入列大于开始列，小于等于结束列，则结束列+1
      item.endCol = item.endCol+1;
    }
  }

  //处理单元格编辑组件设置的col值
  for(let item of cellComponentsList.value){
    if(item.col >= obj.col){
      item.col = item.col+1;
    }
  }
  
  //添加列操作
  let newColumns = [
    ...vtableOption.columns.slice(0, obj.col), 
    ...[{ field: id, title: id, width: 100 }], 
    ...vtableOption.columns.slice(obj.col)
  ];
  vtableOption.columns.splice(obj.col, 0, { field: id, title: id, width: 100 });

  // 调整现有样式数据
  let tempStyleList = [];
  for(const item of cellStyleConfigList.value){
    if(item.col >= obj.col) {
      //卸载样式
      listTableRef.value.vTableInstance.registerCustomCellStyle(item.id, null)
      //调整已存在样式
      item.id = 'style-id-'+item.row+'-'+(item.col+1);
      item.col = item.col+1;
    }
    tempStyleList.push(item);
  }
  cellStyleConfigList.value = tempStyleList;
  //重新渲染样式
  await loadCellStyle();
  listTableRef.value.vTableInstance.updateColumns(newColumns);
}

//删除行事件
async function handleDeleteRow(obj){
  // 处理已合并单元格配置的row值
  for(let item of mergeCellList.value){
    //删除行小于等于结束行才处理合并单元格
    if(obj.row <= item.endRow){
      //删除行小于合并单元格开始行，则开始行需要-1
      if(obj.row < item.startRow){
        item.startRow = item.startRow-1;
      }
      item.endRow = item.endRow-1;
    }
  }

  //处理单元格编辑组件设置的row值
  let newCellComponentsList = [];
  for(let item of cellComponentsList.value){
    if(item.row != obj.row){
      newCellComponentsList.push(item);
      if(item.row >= obj.row){
        item.row = item.row-1;
      }
    }
  }
  cellComponentsList.value = newCellComponentsList;

  // 调整现有样式数据
  let tempStyleList = [];
  for(const item of cellStyleConfigList.value){
    //卸载样式
    listTableRef.value.vTableInstance.registerCustomCellStyle(item.id, null)
    if(item.row != obj.row){
      if(item.row > obj.row) {
        //调整已存在数据
        item.id = 'style-id-'+(item.row-1)+'-'+item.col;
        item.row = item.row-1;
      }
      tempStyleList.push(item);
    }
  }
  cellStyleConfigList.value = tempStyleList;
  //重新渲染样式
  await loadCellStyle();

  listTableRef.value.vTableInstance.deleteRecords([obj.row]);
  removeDuplicates();
}

//删除列事件
async function handleDeleteCol(obj){
  //处理已合并单元格配置的col值
  for(let item of mergeCellList.value){
    //删除列小于等于结束列才处理合并单元格
    if(obj.col <= item.endCol){
      //删除列小于合并单元格开始列，则开始列需要-1
      if(obj.col < item.startCol){
        item.startCol = item.startCol-1;
      }
      item.endCol = item.endCol-1;
    }
  }

  //处理单元格编辑组件设置的col值
  let newCellComponentsList = [];
  for(let item of cellComponentsList.value){
    if(item.col != obj.col){
      newCellComponentsList.push(item);
      if(item.col >= obj.col){
        item.col = item.col-1;
      }
    }
  }
  cellComponentsList.value = newCellComponentsList;
  let newColumns = vtableOption.columns;
  // 使用splice删除指定下标的元素
  newColumns.splice(obj.col, 1);

  // 调整现有样式数据
  let tempStyleList = [];
  for(const item of cellStyleConfigList.value){
    //卸载样式
    listTableRef.value.vTableInstance.registerCustomCellStyle(item.id, null)
    if(item.col != obj.col){
      if(item.col > obj.col) {
        //调整已存在样式
        item.id = 'style-id-'+item.row+'-'+(item.col-1);
        item.col = item.col-1;
      }
      tempStyleList.push(item);
    }
  }
  cellStyleConfigList.value = tempStyleList;
  //重新渲染样式
  await loadCellStyle();
  
  listTableRef.value.vTableInstance.updateColumns(newColumns);
  removeDuplicates();
}

//设置样式
function handleSetStyle(args){
  const selectCells = listTableRef.value.vTableInstance.getSelectedCellInfos();
  let isColor = false;
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
      if(args.menuKey == 'setStyle_fontBold'){
        //文字加粗
        cellStyle.style.fontWeight = 'bold';
      }else if(args.menuKey == 'setStyle_alignLeft'){
        //文字左对齐
        cellStyle.style.textAlign = 'left';
      }else if(args.menuKey == 'setStyle_alignCenter'){
        //文字居中
        cellStyle.style.textAlign = 'center';
      }else if(args.menuKey == 'setStyle_alignRight'){
        //文字右对齐
        cellStyle.style.textAlign = 'right';
      }else if(args.menuKey == 'setStyle_marked'){
        //设置组件标记
        cellStyle.style.marked = args.marked;
      }else if(args.menuKey == 'setStyle_bgColor'){
        //需要设置背景色
        isColor = true;
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
  if(isColor){
    //设置背景色
    setColor(selectCells)
  }
}

//设置填报项
function handleSetConfig(args){
  let tempTableFieldData = [];
  const componentObject = cellComponentsList.value.find(obj => obj.col == args.col && obj.row == args.row);
  //如果当前设置过填报项，则把填报项也加入tempTableFieldData中，用于抽屉窗口显示
  if(componentObject){
    tempTableFieldData.push(originalTableFieldData.value.find(obj => obj.fieldName == componentObject.mappingColumn));
  }
  
  openDrawer(true, {
    args, 
    tableFieldData: [...tempTableFieldData, ...tableFieldData.value], 
    componentObject,
    parentData: parentData.value,
  });
}

//回填填报项设置
async function handleConfigDrawerOk(data){
  const mergeCellObject = validMergeCell(data.row, data.col, 'object');
  //如果是合并单元格，则需给所有单元格添加，否则添加当前单元格
  if(mergeCellObject){
    for(let row = mergeCellObject.startRow; row <= mergeCellObject.endRow; row++){
      for(let col = mergeCellObject.startCol; col <= mergeCellObject.endCol; col++){
        addCellComponentsList(row, col, data.mappingColumn, data.componentType, data.dictType, data.mappingId)
      }
    }
  }else{
    addCellComponentsList(data.row, data.col, data.mappingColumn, data.componentType, data.dictType, data.mappingId)
  }

  //设置下拉编辑器数据，用于查询缓存字典
  SelectEditorObject.setCellComponentsList(cellComponentsList.value);
  EditorDatePickerObject.setCellComponentsList(cellComponentsList.value);
  await removeDuplicates();
  await loadCellStyle();
  //渲染表格
  listTableRef.value.vTableInstance.renderWithRecreateCells();
}

//添加配置
function addCellComponentsList(row, col, mappingColumn, componentType, dictType, mappingId){
  //删除已存在配置，添加新配置
  cellComponentsList.value = cellComponentsList.value.filter(obj => !(obj.col == col && obj.row == row));
  cellComponentsList.value.push({
    col: parseInt(col),
    row: parseInt(row),
    mappingColumn: mappingColumn,
    componentType: componentType,
    dictType: dictType,
    mappingId: mappingId
  });
  //设置组件标记样式
  handleSetStyle({row: row, col: col, menuKey: 'setStyle_marked', marked: true});
}

//删除配置
function removeCellComponent(row, col){
  //删除已存在配置
  cellComponentsList.value = cellComponentsList.value.filter(obj => !(obj.col == col && obj.row == row));
  //设置组件标记样式
  handleSetStyle({row: row, col: col, menuKey: 'setStyle_marked', marked: false});
  
  //如果是合并单元格，删除所有单元格的配置
  const mergeCellObject = validMergeCell(row, col, 'object');
  if(mergeCellObject){
    for(let row = mergeCellObject.startRow; row <= mergeCellObject.endRow; row++){
      for(let col = mergeCellObject.startCol; col <= mergeCellObject.endCol; col++){
        cellComponentsList.value = cellComponentsList.value.filter(obj => !(obj.col == col && obj.row == row));
      }
    }
  }
  removeDuplicates();
  //重新渲染表格
  listTableRef.value.vTableInstance.renderWithRecreateCells();
}

//验证单元格是否在合并单元格内，在内返回合并单元格对象
function validMergeCell(row, col, type){
  if(type == 'object'){
    //返回object对象
    let mergeCellObject = null;
    for(const item of mergeCellList.value){
      if(col >= item.startCol && col <= item.endCol
        && row >= item.startRow && row <= item.endRow
      ){
        mergeCellObject = item;
        break;
      }
    }
    return mergeCellObject;
  } else if (type == 'addRow'){
    //新增行判断是否合并单元格且设置过组件
    for(const item of mergeCellList.value){
      if(row > item.startRow && row <= item.endRow){
        return cellComponentsList.value.find(obj => obj.col == item.startCol && obj.row == row) ? true : false;
      }
    }
  } else if (type == 'addCol'){
    //新增列判断是否合并单元格且设置过组件
    for(const item of mergeCellList.value){
      if(col > item.startCol && col <= item.endCol){
        return cellComponentsList.value.find(obj => obj.col == col && obj.row == item.startRow) ? true : false;
      }
    }
  }
  return null;
}

//获取数据
async function getData(record){
  let res = {} as any;
  //类型等于2则获取pi的字段映射
  if(record.reportType == 2){
    res = await getPiFieldList({allocationTableId: record.dataId, isList: 1, pageSize: 1000});
    //标记字段是否可编辑
    for(const cellItem of cellComponentsList.value){
      for(const item of res.records){
        if(cellItem.mappingColumn == item.fieldName){
          cellItem.isEdit = item.isEdit;
          break;
        }
      }
    }
  } else {
    //自定义报表的字段映射
    res = await getFieldList({tableName: record.tableName, pageSize: 1000})
  }
  originalTableFieldData.value = res.records;
  removeDuplicates();
}

//排除已经绑定的列字段
function removeDuplicates(){
  tableFieldData.value = originalTableFieldData.value.filter(fieldItem => 
    !cellComponentsList.value.some(componentItem => componentItem.mappingColumn == fieldItem.fieldName)
  );
}

//注册处理样式
async function loadCellStyle() {
  for(const item of cellStyleConfigList.value){
    //注册样式配置
    await listTableRef.value.vTableInstance.registerCustomCellStyle(item.id, item.style);
    //更新单元格样式
    listTableRef.value.vTableInstance.arrangeCustomCellStyle({ col: item.col, row: item.row }, item.id)
  }
}

function getTableData(data) {
  restData();
  data.returnExcel.map((item, index) =>{
    //初始化表格大小
    //最大列
    let maxCol = item['maxCol'] || 30;
    //最大行
    let maxRow = item['maxRow'] || 30;
    //设置列
    let colList = [];
    for(let colNum = 0;colNum <= maxCol;colNum ++){
      let colOption = {};
      colOption.field = colNum;
      colOption.title = '';
      colOption.width = 'auto';
      colList.push(colOption);
    }
    vtableOption.columns = colList;

    //数据信息
    let rowData = item['sheet'];
    //设置行
    let newArray = [];
    for(let rowNum = 0; rowNum <= maxRow; rowNum++){
      let colTextList = {};
      for(let colNum = 0;colNum <= maxCol;colNum ++){
        let colText = rowData[rowNum][colNum] == undefined ? "" : rowData[rowNum][colNum];
        colTextList[colNum+''] = colText;
      }
      newArray.push(colTextList);
    }
    vtableOption.records = newArray;

    //合并单元格信息
    let merges = item['merges'] || [];
    let mergesList = [];
    merges.forEach((item,index) => {
      let mergesHr = {};
      mergesHr.startCol = item['s']['c'];
      mergesHr.endCol = item['e']['c'];
      mergesHr.startRow = item['s']['r'];
      mergesHr.endRow = item['e']['r'];
      //合并单元格之后，将第一个单元格的值赋予合并后的值
      let newData = vtableOption.records[item['s']['r']][item['s']['c']];
      mergesHr.dataValue = newData;
      mergesList.push(mergesHr);
    });
    mergeCellList.value = mergesList;
  });
}

//提供外部获取所有配置数据
function getConfigData(){
  //点击确定是关闭表格所有的编辑状态，以便保存数据
  listTableRef.value.vTableInstance.completeEditCell();
  return {
    columns: vtableOption.columns,
    records: vtableOption.records,
    mergeCellList: mergeCellList.value,
    cellComponentsList: cellComponentsList.value,
    cellStyleConfigList: cellStyleConfigList.value,
    fillInList: fillInList.value
  };
}


onMounted(() => {
  // 监听父容器高度改变
  // const resizeObserver = new ResizeObserver(entries => {
  //   for (let entry of entries) {
  //     listTableHeight.value = entry.contentRect.height;
  //   }
  // });
  // resizeObserver.observe(containerRef.value);
  
})

//导出
async function exportExcel(tableName){
  console.log("导出------------------------");
  const listTableInstance = listTableRef.value;
  if (!listTableInstance) {
    msg.warning("ListTable 实例未创建或未挂载");
  }
  //获取 VTable 实例的数据
  const tableInstance = listTableInstance.vTableInstance;
  downloadExcel(await exportVTableToExcel(tableInstance), tableName);
}
//设置单元格颜色
function setColor(selectCells){
  openColorDrawer(true, {selectCells});
}


//查询数据
async function setData(data){
  
  let delArr = []
  for(let i=0; i<dataPage.value.length; i++){
    delArr.push(tableRow.value+i);
  }
  dataPage.value = data;
  await listTableRef.value.vTableInstance.deleteRecords(delArr);
  nextTick(() => {
    //添加数据
    for(let i=0; i<data.length; i++){
      listTableRef.value.vTableInstance.addRecord(data[i], tableRow.value+1+i);
    }
  })
  
}
//获取可编辑字段
function getEditFieldSet(){
  return editFieldSet.value
}
function clearEditFieldSet(){
  editFieldSet.value.clear();
}
//菜单配置
function getMenuConfig(field, row, col){
    //右键菜单配置
    let menus = [];
    let menuArray = [
      { text: '查看标准', menuKey: 'standard' },
      { text: '修改数据', menuKey: 'modify' },
      { text: '无效剔除', menuKey: 'invalid' },
      // { text: '5Why', menuKey: 'fiveAnalysis' },
      { text: '异常分析', menuKey: 'abnormalAnalysis' },
      { text: '点图', menuKey: 'pointDiagram' },
    ];
    if(customReportDeptName.value && customReportDeptName.value.includes('包装')){
      menuArray.push({ text: '箱图', menuKey: 'boxDiagram' })
    }
    if(row > 1){
      let regex = /^field_(\d+)$/;
      let match = field.match(regex);
      if(match){
        //实际值
        menus = menuArray;
      }else{
        //标注值
        menus.push(menuArray[0])
      }
    }
    return menus;
  }

  // 暴露方法
  defineExpose({
    initData,
    getConfigData,
    setData,
    exportExcel,
    dataPage,
    editDataIndex,
    listTableRef,
    getEditFieldSet,
    clearEditFieldSet,
  });
</script>

<style lang="less" scoped>
.table-container{
  width: 100%;
  min-height: 400px;
}

.vtableList{
  width: 100%;
}

.ant-btn{
  margin-left: 10px;
  margin-bottom: 10px;
}
</style>