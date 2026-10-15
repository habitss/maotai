<template>
  <div v-if="actionFlag === 'config'">
    <!-- <span style="color:red">*</span> -->
    <a-button color="error" v-on:click="allocationField" preIcon="ep:plus">设置字段</a-button>
    <!-- <a-button color="error" v-on:click="resetTable" preIcon="ant-design:delete-outlined" title="清空表头配置"
    v-if="parentData.headingId">重置表头</a-button> -->
  </div>
  <div class="table-container" ref="containerRef" :style="actionFlag === 'config' ? 'height: calc(100% - 42px);' : 'height: 100%;'">
    <ListTable class="vtableList" ref="listTableRef" 
      :options="vtableOption" 
      :height="listTableHeight"
      @onDropdownMenuClick = "handleDropdownMenuClick"
      @onContextMenuCell = "handleContextMenuCell"
      @onResizeColumnEnd = "handleResizeColumnEnd"
    >
      <Menu menuType="html" v-if="actionFlag === 'config'" :contextMenuItems="contextMenuItems" />
    </ListTable>
  </div>
  
  <!--颜色选择配置抽屉-->
  <ColorDrawer @register="colorDrawer" @success="colorDrawerOk" :showFooter="true" />
  <!--字体样式-->
  <FontDrawer @register="fontDrawer" @success="fontDrawerOk" :showFooter="true" />
  <!--字段分配-->
  <a-drawer
        v-if="hasReopen == true"
        v-model:visible="hasReopen"
        class="custom-class"
        root-class-name="root-class-name"
        :root-style="{ color: 'blue' }"
        title="字段分配"
        width="70%"
        placement="right">
        <AllocationFieldForm @ok="updateList" @close="handleCancel" :departObject="departObject"></AllocationFieldForm>
  </a-drawer>
</template>

<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue';
import { Modal } from 'ant-design-vue';
import { VTable, ListTable, Menu } from '@visactor/vue-vtable';
import * as VTableEditors from '@visactor/vtable-editors';
import dayjs from 'dayjs';
import ColorDrawer from '@/views/dataCalculation/pi/allocationTable/components/colorDrawer.vue';
import FontDrawer from '@/views/dataCalculation/pi/allocationTable/components/fontDrawer.vue';
import { list as getFieldList } from '@/views/dataCalculation/customReport/reportConfig/reportConfigField.api';
import { list as getPiFieldList } from '@/views/dataCalculation/pi/allocationTable/AllocationTableField.api';
import { useMessage } from '/@/hooks/web/useMessage';
import { useDrawer } from '/@/components/Drawer';
import {downloadExcel,exportVTableToExcel} from "@visactor/vtable-export";
import AllocationFieldForm from '@/views/dataCalculation/pi/allocationTable/components/AllocationFieldForm.vue'
const hasReopen = ref<boolean>(false);
import { uploadUrl } from '/@/api/common/api';
const departObject = ref<Object>({});
const emit = defineEmits(['resetTable']);
const color = ref('red');

//消息提示
const { createMessage: msg } = useMessage();

//填报项配置抽屉
const [registerDrawer, { openDrawer }] = useDrawer();
//颜色设置
const [colorDrawer, { openDrawer:openColorDrawer }] = useDrawer();
//字体设置
const [fontDrawer, { openDrawer:openFontDrawer }] = useDrawer();
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
//已配置的单元格图片数据
const cellImgConfigList = ref([] as any);
//已配置过的绑定表字段数据
const tableFieldData = ref([] as any);
//模版绑定表的所有字段数据，用于和已配置过的字段比较，避免字段重复绑定
const originalTableFieldData = ref([] as any);
//注册输入框编辑器
const inputEditor = new VTableEditors.InputEditor();
VTable.register.editor('input-editor', inputEditor);



//-测试
const preloadedImages = new Map();
mergeCellList.value.forEach(item => {
  const img = new Image();
  img.src = item.dataValue;
  preloadedImages.set(item.dataValue, img);
});



//表格配置
const vtableOption = reactive({
  showHeader: false, //是否隐藏表头
  columns: [] as any,
  records: [] as any,
  theme: VTable.themes.DEFAULT.extends({
    scrollStyle: {
      visible: 'always'
    }
  }),
  editCellTrigger: 'click',
  columnResizeMode : 'all',
  heightMode : 'autoHeight', //内容换行
  autoWrapText: true,
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

  // customRender: (args) => {
  //   const { row, col, rect, table } = args;
  //   for (let item of mergeCellList.value) {
  //     if (row === item.startRow && col === item.startCol) {
  //       const ctx = table.getContext();
  //       const { x, y, width, height } = rect;

  //       // 计算整个合并区域的宽高
  //       const range = {
  //         width: table.getColsWidth(item.startCol, item.endCol),
  //         height: table.getRowsHeight(item.startRow, item.endRow)
  //       };
  //       debugger
  //       const img = new Image();
  //       // img.src = item.dataValue;
  //       debugger
  //       img.src = "https://fastly.picsum.photos/id/281/500/100.jpg?hmac=XqhjkXsmMDAq5TfpCi8xXzvzlQOZAFmjRYOEfBcsjWw";
  //       img.onload = () => {
  //         const ratio = img.width / img.height;
  //         let drawW = range.width;
  //         let drawH = range.width / ratio;
  //         if (drawH > range.height) {
  //           drawH = range.height;
  //           drawW = range.height * ratio;
  //         }
  //         const offsetX = x + (range.width - drawW) / 2;
  //         const offsetY = y + (range.height - drawH) / 2;

  //         ctx.drawImage(img, offsetX, offsetY, drawW, drawH);
  //       };

  //       return true;
  //     }
  //   }
  //   return false;
  // }

});

//右键菜单配置
const contextMenuItems = [
  {
    text: '设置单元样式',
    children: [
      // {
      //   text: '加粗',
      //   menuKey: 'setStyle_fontBold'
      // },
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
        text: '背景设置',
        menuKey: 'setStyle_bgColor'
      },
      {
        text: '字体样式',
        menuKey: 'setStyle_font'
      },
    ]
  },
  {type: 'split'},
//   {text: '设置填报项', menuKey: 'reportConfig'}, 
//   {type: 'split'},
  {text: '合并单元格', menuKey: 'mergeCell'},
  {text: '取消合并单元格', menuKey: 'unMergeCell'},
  {type: 'split'},
  {text: '新增行', menuKey: 'addRow'},
  {text: '删除行', menuKey: 'deleteRow'},
  {type: 'split'},
  {text: '新增列', menuKey: 'addCol'},
  {text: '删除列', menuKey: 'deleteCol'},
];

/**
 * 初始化数据
 * @param record 模板数据
 * @param flag 是否为配置
 * @param fillInData 填报数据
 */
async function initData(record, flag) {
  parentData.value = record;
  actionFlag.value = flag;
  const configData = record.reportConfig ? JSON.parse(record.reportConfig) : '';
  const fieldFormat = record.fieldFormat ? record.fieldFormat:{};
  vtableOption.columnResizeMode = flag === 'config' ? 'all' : 'none';
  //重置数据
  await restData(configData,fieldFormat);
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
function restData(reportConfigJson, fieldFormat){
  //绑定字段数据
  let cellList = reportConfigJson ? reportConfigJson.cellComponentsList : [];
  let timeFormatArr = {};
  for(const cell of cellList){
    //判断 时间 是否需要进行格式 
    let timeFormat = fieldFormat[cell.mappingColumn];
    if(cell.componentType == 'dateTimePicker' && timeFormat){
      timeFormatArr[cell.row+"_"+cell.col] = timeFormat;
    }
  }

  let columnsArr = reportConfigJson ? reportConfigJson.columns : [];
  for(let i=0; i<columnsArr.length; i++){
    let obj = columnsArr[i];
    obj.fieldFormat = function(value,col,row) {
      let formatType = timeFormatArr[row+"_"+col]
      let val = value[obj.field]
      if (val && formatType && val !='/') {
        return formatDateTime(val, parseInt(formatType));
      }
      return val;
    }
    obj.cellType = function(args) {

      if(cellImgConfigList.value.some(item => item.col === args.col && item.row === args.row)){
        return 'image'
      }else{
        return 'text'
      }
      // debugger
      // if (args.col === 0) return 'image';
      // else if (args.col > 1) return 'text';
    }
    //obj.keepAspectRatio = true;
  }
  vtableOption.columns = columnsArr;
  vtableOption.records = reportConfigJson ? reportConfigJson.records : [];
  mergeCellList.value = reportConfigJson ? reportConfigJson.mergeCellList : [];
  cellComponentsList.value = reportConfigJson ? reportConfigJson.cellComponentsList : [];
  cellStyleConfigList.value = reportConfigJson ? reportConfigJson.cellStyleConfigList : [];
  fillInList.value = [];
  console.log('cellComponentsList.value :>> ', cellComponentsList.value);
}

/**
 * 时间格式化公共方法
 * @param {Date|string} date - 日期对象或日期字符串
 * @param {number} formatType - 格式类型：1-年月日时分秒，2-年月日，3-时分秒，4-时分，5-时
 * @returns {string} 格式化后的时间字符串
 */
function formatDateTime(date, formatType = 1) {
  // 确保 date 是 Date 对象
  const d = new Date(date);
  if (isNaN(d.getTime())) {
    return "";
  }

  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  const hours = String(d.getHours()).padStart(2, '0');
  const minutes = String(d.getMinutes()).padStart(2, '0');
  const seconds = String(d.getSeconds()).padStart(2, '0');

  switch (formatType) {
    case 1: // 年月日时分秒
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    case 2: // 年月日
      return `${year}-${month}-${day}`;
    case 3: // 时分秒
      return `${hours}:${minutes}:${seconds}`;
    case 4: // 时分
      return `${hours}:${minutes}`;
    case 5: // 时
      return `${hours}`;
    default:
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
  }
}


//事件绑定
function bindEvent(){
  //单元格编辑事件绑定，无法直接在组件上用on绑定，需单独声明绑定
  listTableRef.value.vTableInstance.on(VTable.ListTable.EVENT_TYPE.CHANGE_CELL_VALUE, (...args) => {
    let value = args[0].changedValue;
    let dictKey = '';
    //如果为对象代表下拉编辑器返回的数据，显示文本值
    if(typeof value === 'object'){
      dictKey = value.value;
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

    //添加填报数据
    pushFillInData(args[0].row, args[0].col, value, dictKey);
  });
}

//添加填报数据
function pushFillInData(row, col, value, dictKey){
  //如果是填报，添加填报数据
  if(actionFlag.value === 'add' || actionFlag.value === 'edit'){
    //查找填报项下标
    const index = fillInList.value.findIndex(item => item.row == row && item.col == col);
    //如果填报项中已存在，修改数据，否则新增数据
    if(index !== -1){
      fillInList.value[index] = { ...fillInList.value[index], ...{value, dictKey} };
    } else {
      //找出对应的填报项组件单元格
      const cellComponent = cellComponentsList.value.find(item => item.row == row && item.col == col);
      cellComponent.value = value;
      cellComponent.dictKey = dictKey;
      fillInList.value.push(cellComponent);
    }
  }
}

//加载填报数据
function loadFillInData(fillInRecords){
  for(const cell of cellComponentsList.value){
    //解析填报项组件设置数组，修改对应单元格的值
    if(fillInRecords && fillInRecords[cell.mappingColumn]){
      //判断是否在合并单元格，修改合并单元格显示值
      const mergeCell = validMergeCell(cell.row, cell.col, 'object');
      if(null != mergeCell){
        mergeCell.dataValue = fillInRecords[cell.mappingColumn];
      }
      vtableOption.records[cell.row][vtableOption.columns[cell.col].field] = fillInRecords[cell.mappingColumn];
    }else{
      const mergeCell = validMergeCell(cell.row, cell.col, 'object');
      if(null != mergeCell){
        mergeCell.dataValue = "/";
      }
      vtableOption.records[cell.row][vtableOption.columns[cell.col].field] = "/";
    }
  }
}

//初始化表格行列
function handleInitTableData(fieldArr){
  // if(vtableOption.columns.length > 0){
  //   msg.warning('表格已有数据，不能初始化')
  //   return;
  // }
  let record = parentData.value;
  const configData = record.reportConfig ? JSON.parse(record.reportConfig) : '';

  let columnList = [] as any;
  let recordList = [] as any;
  let dataObj = {};
  for(let i = 0; i < fieldArr.length; i++){
    let fieldObj = fieldArr[i];
    columnList.push({ field: fieldObj.fieldName, title: fieldObj.description, width: 100, style: { borderColor: '#f0f0f0', borderLineWidth: '1' }});
    dataObj[fieldObj.fieldName] = fieldObj.description;
  }
  if(!configData || !configData.records){
    recordList.push(dataObj);
    vtableOption.records = recordList;
  }
  vtableOption.columns = columnList;
  
}

//右键下拉菜单点击事件监听
async function handleDropdownMenuClick(...args){
  if(args[0].menuKey == 'mergeCell'){
    //合并单元格
    await handleMergeCells();
  }else if(args[0].menuKey == 'unMergeCell'){
    //取消合并单元格
    await handleUnmergeCells();
  }else if(args[0].menuKey == 'addRow'){
    //新增行
    await handleAddRow(args[0]);
  }else if(args[0].menuKey == 'addCol'){
    //新增列
    await handleAddCol(args[0]);
  }else if(args[0].menuKey == 'deleteRow'){
    //删除行
    await handleDeleteRow(args[0]);
  }else if(args[0].menuKey == 'deleteCol'){
    //删除列
    await handleDeleteCol(args[0]);
  }else if(args[0].menuKey == 'reportConfig'){
    //设置填报项
    await handleSetConfig(args[0]);
  }else if(args[0].menuKey.includes('setStyle')){
    //设置样式
    await handleSetStyle(args[0]);
  }
}

//鼠标右键点击事件监听
function handleContextMenuCell(...args){
  if(args[0].cells.length == 1 && args[0].cells[0].length == 1){
    //选中右键所在单元格
    listTableRef.value.vTableInstance.selectCell(args[0].col, args[0].row);
  }
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
  let isFont = false;
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
      }else if(args.menuKey == 'setStyle_font'){
        //设置字体样式
        isFont = true;
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
  if(isFont){
    //设置字体
    setFont(selectCells)
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

//列宽拖拽结束监听
function handleResizeColumnEnd(...args){
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

//导出
async function exportExcel(){
  console.log("导出------------------------");
  const listTableInstance = listTableRef.value;
  if (!listTableInstance) {
    msg.warning("ListTable 实例未创建或未挂载");
  }
  //获取 VTable 实例的数据
  const tableInstance = listTableInstance.vTableInstance;
  downloadExcel(await exportVTableToExcel(tableInstance), "自定义报表");
}
//设置单元格颜色
function setColor(selectCells){
  //cellStyle.style.bgColor = "#52c41a";
  openColorDrawer(true, {selectCells, styleConfigList: cellStyleConfigList.value});
}
//颜色选择完成
function colorDrawerOk(color,img,selectCells){
  console.log(color+"============================");
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
      cellStyle.style.bgColor = color;
      // img = "https://picsum.photos/500/100?bg"
      if(img){
        img = uploadUrl+img;
        //判断编辑数据是否在合并单元格，修改合并单元格显示值
        const mergeCell = validMergeCell(col.row,col.col, 'object');
        if(null != mergeCell){
          mergeCell.dataValue = img;
        }else{
          const record = vtableOption.records[col.row];
          const field = vtableOption.columns[col.col].field;
          record[field] = img;
        }
        //添加填报数据
        pushFillInData(col.row, col.col, img, '');

        cellImgConfigList.value.push({"row": col.row, "col": col.col})
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
//分配字段
const allocationField = () => {
  hasReopen.value = true;
  parentData
  let record = {
    tableName: parentData.value.tableName,
    tableId: parentData.value.tableId,  //自定义表id
    allocationTableId: parentData.value.dataId, //分配表主表id
    tableHeadConfig : true
  }
  departObject.value = record;
}
/**配置完成 */
function updateList(val) {
  hasReopen.value = false;
  handleInitTableData(val);
  //(selectedRowKeys.value = []) && reload();
}

/**取消按钮回调事件*/
function handleCancel() {
  hasReopen.value = false;
}
/**重置表头 */
// function resetTable(){
//   Modal.confirm({
//     title: '重置表头',
//     content: '确认重置表头，配置将被清空',
//     okText: '确认',
//     cancelText: '取消',
//     onOk: () => {
//        emit('resetTable',parentData.value.headingId);
//     },
//   });
// }
// 暴露方法
defineExpose({
  initData,
  getConfigData,
  exportExcel,
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