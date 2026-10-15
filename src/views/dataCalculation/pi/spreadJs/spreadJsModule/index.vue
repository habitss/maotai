<template>
  <a-spin :spinning="pageLoading">
     <div class="p-2">
      <div class="jeecg-basic-table-form-container">
        <!--导入-->
        <a-form style="background-color: #fff; padding: 0px 0px" v-if="config">
          <div style="display: flex; align-items: center; padding: 0px 0px 0px 0px">
            <div style="margin-left: 10px">
              <a-upload class="custom-upload" :before-upload="() => false" :max-count="1" accept=".xlsx,.xls" @change="uploadOnChange">
                <a-button type="primary">选择文件</a-button>
              </a-upload>
            </div>
          </div>
        </a-form>
        
        <div class="spreadContainer" :style="{height: config ? '76vh' : '85vh'}">
          <div :style="{ width: config ? '70%' : '100%' }">
            <gc-spread-sheets hostClass="spreadHost" @workbook-initialized="workbookInitialized">
              <GcWorksheet />
            </gc-spread-sheets>
          </div>
          <div style="width: 30%;" class="div-border" v-if="config">
          <!-- <div style="width: 502px" class="div-border" v-if="config"> -->
            <div style="padding: 5px;background-color: rgb(232 232 232);text-align: center;">报表设置</div>
            <BasicForm @register="registerForm" @submit="handleSubmit">

              <template #conditionSlot="{ model, field}">
                <a-form-item-rest>
                <a-row justify="space-between">
                  <a-col :span="4">
                    <a-select v-model:value="model['relationship_'+field]" placeholder="请选择关系" 
                      allowClear
                      :options="relationshipArr"
                      :disabled="model['index_'+field] == '1'"
                    />
                  </a-col>
                  <a-col :span="8">
                    <a-select v-model:value="model[field]" placeholder="请选择字段" 
                      allowClear
                      show-search
                      :filterOption = "(input, option) => handleFilterOption(input, option, 'name')"
                      :options="piFieldOptions"
                      :fieldNames="{label: 'label', value: 'value'}" 
                      @change="handleChange"
                    />
                  </a-col>
                  <a-col :span="4">
                    <a-select v-model:value="model['symbol_'+field]" placeholder="请选择条件" 
                      allowClear
                      :options="symbolOption"
                      @change="handleChange"
                    />
                  </a-col>
                  <a-col :span="5">
                    <a-input v-model:value="model['value_'+field]" placeholder="请输入值" allowClear :disabled="isDetail" />
                  </a-col>
                  <a-col :span="2">
                    <a-button @click="removeCondition(field)" type="link" danger style="padding: 0">删除</a-button>
                  </a-col>
                </a-row>
              </a-form-item-rest>
              </template>

                <!-- 底部插槽 -->
              <template #formFooter>
                <div style="margin: 0 auto">
                  <a-button type="link" @click="addCondition()">添加条件</a-button>
                </div>
              </template>
            </BasicForm>
            <div style="text-align: center;">
              <a-button type="primary" @click="addDataTable()">添加数据到表格</a-button>
            </div>

          </div>
        </div>
      </div>

      <!--颜色选择配置抽屉-->
    <ColorDrawer @register="colorDrawer" @success="colorDrawerOk" :showFooter="true" />
    </div>
  </a-spin>
</template>

<script lang="ts" setup>
  import {ref,nextTick,watch,toRaw,isVNode,reactive  } from 'vue';
  // import { BasicModal, useModalInner } from '/@/components/Modal';
  import '@grapecity/spread-sheets/styles/gc.spread.sheets.excel2016colorful.css';
  import * as GC from '@grapecity/spread-sheets';
  import { GcSpreadSheets, GcWorksheet } from '@grapecity/spread-sheets-vue';
   import {BasicForm, useForm} from '/@/components/Form/index';
  import * as MySpreadUtils from '@/utils/spreadUtils';
  import '@grapecity/spread-sheets-resources-zh';
  import '@grapecity/spread-sheets-io';
  import dayjs from 'dayjs';
  import { useMessage } from '@/hooks/web/useMessage';
  import {formSchema,piFieldOptions,piTableId} from './spreadJsModule.data';
  import { useDrawer } from '/@/components/Drawer';
  const { createMessage,createConfirm } = useMessage();
  import { cloneDeep } from 'lodash-es';
  import {saveOrUpdate, savePiConfig, getPiConfig} from '../config/PiSpreadjsMain.api';
  import {list as baseDataList} from '/@/views/dataCalculation/pi/customizeTable/QueryTable.api';
  import { initDictOptions } from '/@/utils/dict';
  import ColorDrawer from '@/views/dataCalculation/pi/allocationTable/components/colorDrawer.vue';

  // 初始化 Spread
  GC.Spread.Common.CultureManager.culture('zh-cn');
  let SpreadJSKey =
    'emsapac.ab-inbev.cn,623376735916589#B1v4r7N0dHWyBDcwkXVMJ7Qr9UWtpWUYlzbzgjUORkUY3GOzEVNHZFNp5kZGdXc4xEbvAVUOh6VGJWWWdTORJjTMRWdyoXS4B5MJNHdwAlVmt6MRNTb4gTTxMzVrUTNhZldQ94Yx2yVrlzT4JFV6U7KQdlMB3SZmVFbzUFTxJVVN9kUNdFOHJXcDN4YxpmTPl5VsBXRz2UQvsGcyAHTqlEa8YmRk5UMhd4dCtkTDpmVCl6aQdETKJmUNNUZyAnSrd6Y68kc8VnV62GN4Elc4QjWFR6Sv2iWy24Yt5UYFR7RhZXaXNjU6hjUY3WcJhmetNUR5ZGTZNkS85EcU3GbwdlI0IyUiwiI8QUNyADNGZjI0ICSiwSO7AzNyQTO8UTM0IicfJye&Qf35VficFVDplI0IyQiwiI6EjL6ByUKBCZhVmcwNlI0IiTis7W0ICZyBlIsISOzMDM8ADI9ETNwMjMwIjI0ICdyNkIsIibj9idlJmbp5iYh9yYhBXYz5WZiojIz5GRiwiI8+Y9sWY9QmZ0Jyp9Li09leb9WyY9oqY9qeI0R+K0+0L0JGr9m6q9iojIh94QiwiI9gTN6ETO5MzN6czMzIjNiojIklkIs4XXbpjInxmZiwSZzxWYmpjIyNHZisnOiwmbBJye0ICRiwiI34TQmBjSNNlZJtUOGxEVxZje75kY8MjWMdER6QmMrg7RmZTbLRUQRp7cM9GZ5dWRv2kTsdUN6onewpnepB5ZhljWxN4dnRUaSdTOGNWWUxkbFhlZrpVb7q3'; // Enter a valid license key.
  GC.Spread.Sheets.LicenseKey = SpreadJSKey;
   // 初始化数据
  const curSpread = ref<GC.Spread.Sheets.Workbook | null>(null);
  const reportNameX = ref('');
  const uploadFile = ref<File | null>(null);
  const pageLoading = ref(false);
  const sheetGrouped = ref({});
  // 声明Emits
  const emit = defineEmits(['success']);
  const dataForm = ref({});  
  const formRef = ref();
  const config = ref(false);
  const isFalse = ref(false);

  //颜色
  const colorPickerRef = ref<HTMLInputElement | null>(null);
  const pendingSpread = ref<GC.Spread.Sheets.Workbook | null>(null);
  //颜色设置
  const [colorDrawer, { openDrawer:openColorDrawer }] = useDrawer();
  // 定义自定义右键菜单类
  function CustomContextMenu(spread) {
      // 调用父类构造函数
      GC.Spread.Sheets.ContextMenu.ContextMenu.call(this, spread);
  }
  // 定义自定义筛选 
  function CustomFilter(){
    GC.Spread.Sheets.ConditionalFormatting.Condition.apply(this, arguments);
  };
  // 添加这两行 - 设置原型链继承
  CustomContextMenu.prototype = new GC.Spread.Sheets.ContextMenu.ContextMenu();
  CustomContextMenu.prototype.constructor = CustomContextMenu;

  CustomContextMenu.prototype.onOpenMenu = function (
    menuData,
    itemsDataForShown,
    hitInfo,
    spread
  ) {

    GC.Spread.Sheets.ContextMenu.ContextMenu.prototype.onOpenMenu.call(
      this,
      menuData,
      itemsDataForShown,
      hitInfo,
      spread
    );

    if (!spread) return;

    // 适配新版 hitInfo 结构
    const info = hitInfo?.worksheetHitInfo;
    if (!info) return;

    // 3 = viewport 单元格区域
    if (info.hitTestType !== 3) return;

    const sheet = spread.getActiveSheet();
    if (!sheet) return;

    const selections = sheet.getSelections();
    if (!selections?.length) return;

    const sel = selections[0];

    itemsDataForShown.push({ type: 'separator' });

    // 多选才显示合并
    if (sel.rowCount > 1 || sel.colCount > 1) {
      itemsDataForShown.push({
        name: 'mergeCells',
        text: '合并单元格',
        command: 'mergeCells'
      });
    }
    // 判断是否已合并
    const span = sheet.getSpan(sel.row, sel.col);
    if (span) {
      itemsDataForShown.push({
        name: 'unmergeCells',
        text: '取消合并单元格',
        command: 'unmergeCells'
      });
    }

    // 背景色
    itemsDataForShown.push({
      name: 'setBackColor',
      text: '设置背景色',
      command: 'openColorPicker'
    });

    // 判断是否点击的是表头行（假设表头在第0行） 如果选中的是第一行，显示筛选菜单
    if (sel.row === 0) {
      itemsDataForShown.push({
        name: 'autoFilter',
        text: '筛选',
        command: 'applyAutoFilter'
      });
    }
  };
  //表单配置
  const [registerForm, { setProps,resetFields, setFieldsValue, validate, getFieldsValue,scrollToField,appendSchemaByField,removeSchemaByFiled }] = useForm({
      labelWidth: 50,
      schemas: formSchema,
      showActionButtonGroup: false,
      baseColProps: {span: 24}
  });
  
  //条件符号下拉选项数据
  const symbolOption = ref([
    { value: '1', label: '>' },
    { value: '2', label: '≥' },
    { value: '3', label: '=' },
    { value: '4', label: '≠' },
    { value: '5', label: '<' },
    { value: '6', label: '≤' },
    { value: '7', label: '包含' }
  ]);

  //两个条件关系
  const relationshipArr = ref([
    {value: '1', label: '与(and)'},
    {value: '2', label: '或(or)'}
  ]) 
  
  //条件下拉选项数据
  const conditionArray = ref([]);
  //条件下拉选项数据
  const conditionOption = ref([]);
  //条件总数
  const conditionTotal = ref(0);
  //初始加载loading
  const modalLoading = ref(true);
  //保存loading
  const confirmLoading = ref(false);
  //PI基础数据
  const configData = ref(false);
  const conditionFieldIds = ref<string[]>([]);

  // WorkbookInitialized 回调
  const workbookInitialized = (spread: GC.Spread.Sheets.Workbook) => {
    curSpread.value = spread;
    spread.options.calcOnDemand = false;
    spread.options.allowUserZoom = true;
    //绑定 Sheet 页切换事件
    spread.bind(GC.Spread.Sheets.Events.ActiveSheetChanged, (e, args) => {
      console.log(e);
      const name = args.newSheet.name();
      setPidata(name);
      setFieldsValue({
        sheetName: name
      });
      const sheet = curSpread.value!.getActiveSheet();
      sheet.resumePaint();
      sheet.repaint();
    });
    // curSpread.value.refresh();
    // **新增：监听编辑结束事件**
    spread.bind(GC.Spread.Sheets.Events.EditEnded, (e, args) => {
        console.log('编辑结束', args);
        // 强制重绘当前 sheet
        const sheet = args.sheet;
        if (sheet) {
            // 暂停然后恢复绘制，强制刷新
            sheet.suspendPaint();
            sheet.resumePaint();
            sheet.repaint();
            
            // 同时刷新整个 spread
            // spread.refresh();
            setTimeout(() => {
              spread.refresh();
            }, 10);
        }
    });

    // 监听筛选变化事件
    spread.bind(GC.Spread.Sheets.Events.RangeFiltered, (e, args) => {
      console.log("筛选发生变化:", args);
      setTimeout(() => {
        spread.refresh();
      }, 10);
    });
    // 注册命令
    registerMergeCommands(spread);
    // 替换右键菜单
    spread.contextMenu = new CustomContextMenu(spread);
  };

  // 注册命令的函数
  const registerMergeCommands = (spread: GC.Spread.Sheets.Workbook) => {
    if (!spread) return;
    
    // 注册合并单元格命令
    spread.commandManager().register('mergeCells', {
        canUndo: true,
        execute: function (context, options, isUndo) {
            var Commands = GC.Spread.Sheets.Commands;
            options.cmd = 'mergeCells';
            
            if (isUndo) {
                Commands.undoTransaction(context, options);
                return true;
            } else {
                Commands.startTransaction(context, options);
                
                var sheet = spread.getActiveSheet();
                var selections = sheet.getSelections();
                
                if (selections && selections.length > 0) {
                    var sel = selections[0];
                    sheet.addSpan(sel.row, sel.col, sel.rowCount, sel.colCount);
                }
                
                Commands.endTransaction(context, options);
                return true;
            }
        }
    });

    // 注册取消合并单元格命令
    spread.commandManager().register('unmergeCells', {
        canUndo: true,
        execute: function (context, options, isUndo) {
            var Commands = GC.Spread.Sheets.Commands;
            options.cmd = 'unmergeCells';
            
            if (isUndo) {
                Commands.undoTransaction(context, options);
                return true;
            } else {
                Commands.startTransaction(context, options);
                
                var sheet = spread.getActiveSheet();
                var selections = sheet.getSelections();
                
                if (selections && selections.length > 0) {
                    var sel = selections[0];
                    sheet.removeSpan(sel.row, sel.col);
                }
                
                Commands.endTransaction(context, options);
                return true;
            }
        }
    });

    // 注册设置背景色命令
    spread.commandManager().register('openColorPicker', {
      canUndo: true,
      execute: () => {
        pendingSpread.value = spread;
        openColorDrawer(true, {selectCells:[[{row:'0',col:'0'}]],styleConfigList:[]});
        return true;
      }
    });

    // 筛选
    spread.commandManager().register('applyAutoFilter', {
      canUndo: true,
      execute: function(context, options, isUndo) {
        const sheet = spread.getActiveSheet();
        const selections = sheet.getSelections();
        if (!selections?.length) return true;

        const sel = selections[0];
        // 1. 创建范围，从表头开始到最后一行
        const rowCount = sheet.getRowCount() - sel.row; // 从表头到最后一行
        const colCount = sel.colCount;

        // 2. 创建 HideRowFilter
        const rowFilter = new GC.Spread.Sheets.Filter.HideRowFilter(
          new GC.Spread.Sheets.Range(sel.row, sel.col, rowCount, colCount)
        );
        sheet.rowFilter(rowFilter);

        // 3. 给选中列每一列都添加自定义筛选
        for (let i = 0; i < colCount; i++) {
          rowFilter.addFilterItem(i, new CustomFilter());
          rowFilter.filter(i);
        }
        return true;
      }
    });
  };


  //导入文件
  const uploadOnChange = ({ file }) => {
    if (file.status === 'removed') {
      MySpreadUtils.cleanSpread(curSpread.value);
      return;
    }
    uploadFile.value = file;
    pageLoading.value = true;
    curSpread.value!.import(
      file,
      () => {
        pageLoading.value = false;
      },
      (error) => {
        console.error(error);
        pageLoading.value = false;
        createConfirm({
          iconType: 'warning',
          title: '系统提示',
          content: '加载Excel时发现存在格式问题！确定要继续吗?',
          okText: '确认',
          cancelText: '取消',
          onOk: () => {},
          onCancel() {
            MySpreadUtils.cleanSpread(curSpread.value);
          },
        });
      }
    );
  };

  //初始化
  // async function initData(record) {
  //   dataForm.value = record
  //   // const jsonStr = {"version":"16.1.0","docProps":{"docPropsCore":{"creator":"Geek Q","lastModifiedBy":"Geek Q","created":"2015-06-05T18:19:34Z","modified":"2025-10-30T02:06:06Z","Application":"Microsoft Excel","DocSecurity":"0","ScaleCrop":"false","Company":"","LinksUpToDate":"false","SharedDoc":"false","HyperlinksChanged":"false","AppVersion":"16.0300"},"docPropsApp":{"HeadingPairs":{"size":2,"baseType":"variant","workbookInfoArray":[{"elementType":"vt:lpstr","value":"工作表"},{"elementType":"vt:i4","value":"2"}]},"TitlesOfParts":{"size":2,"baseType":"lpstr","sheetsNameArray":["步骤","产线"]}}},"sheetCount":2,"tabStripRatio":0.6,"iterativeCalculation":false,"iterativeCalculationMaximumIterations":100,"iterativeCalculationMaximumChange":0.001,"customList":[],"activeSheetIndex":1,"sheets":{"步骤":{"name":"步骤","isSelected":false,"rowCount":29,"columnCount":5,"activeRow":28,"activeCol":3,"visible":1,"frozenTrailingRowStickToEdge":true,"frozenTrailingColumnStickToEdge":true,"theme":{"name":"Office","themeColor":{"name":"Office","background1":{"r":255,"g":255,"b":255,"a":255},"background2":{"r":231,"g":230,"b":230,"a":255},"text1":{"r":0,"g":0,"b":0,"a":255},"text2":{"r":68,"g":84,"b":106,"a":255},"accent1":{"r":91,"g":155,"b":213,"a":255},"accent2":{"r":237,"g":125,"b":49,"a":255},"accent3":{"r":165,"g":165,"b":165,"a":255},"accent4":{"r":255,"g":192,"b":0,"a":255},"accent5":{"r":68,"g":114,"b":196,"a":255},"accent6":{"r":112,"g":173,"b":71,"a":255},"hyperlink":{"r":5,"g":99,"b":193,"a":255},"followedHyperlink":{"r":149,"g":79,"b":114,"a":255}},"headingFont":"'Calibri Light'","bodyFont":"Calibri"},"data":{"dataTable":{"0":{"0":{"value":"区域(area_no)"},"1":{"value":"设备(unit_no)"},"2":{"value":"步骤号(eop_no)"},"3":{"value":"PCU号(pcu_no)"},"4":{"value":"步骤名称(eop_name)"}}},"defaultDataNode":{"style":{"backColor":null,"foreColor":"Text 1 0","vAlign":2,"font":"normal normal 14.6667px Calibri","themeFont":"Body","formatter":"General","borderLeft":null,"borderTop":null,"borderRight":null,"borderBottom":null,"locked":true,"textIndent":0,"wordWrap":false,"textDecoration":0,"isVerticalText":false,"textOrientation":0}}},"rowHeaderData":{},"colHeaderData":{},"columns":[{"size":107},{"size":113},{"size":118},{"size":118},{"size":187}],"leftCellIndex":0,"topCellIndex":0,"selections":{"0":{"row":28,"col":3,"rowCount":1,"colCount":1},"length":1},"defaults":{"colHeaderRowHeight":20,"colWidth":64,"rowHeaderColWidth":40,"rowHeight":18.67,"_isExcelDefaultColumnWidth":true},"rowOutlines":{"items":[]},"columnOutlines":{"items":[]},"cellStates":{},"states":{},"outlineColumnOptions":{},"autoMergeRangeInfos":[],"shapeCollectionOption":{"snapMode":0},"index":0,"order":0},"产线":{"name":"产线","isSelected":true,"rowCount":16,"columnCount":6,"activeRow":15,"activeCol":5,"visible":1,"frozenTrailingRowStickToEdge":true,"frozenTrailingColumnStickToEdge":true,"theme":{"name":"Office","themeColor":{"name":"Office","background1":{"r":255,"g":255,"b":255,"a":255},"background2":{"r":231,"g":230,"b":230,"a":255},"text1":{"r":0,"g":0,"b":0,"a":255},"text2":{"r":68,"g":84,"b":106,"a":255},"accent1":{"r":91,"g":155,"b":213,"a":255},"accent2":{"r":237,"g":125,"b":49,"a":255},"accent3":{"r":165,"g":165,"b":165,"a":255},"accent4":{"r":255,"g":192,"b":0,"a":255},"accent5":{"r":68,"g":114,"b":196,"a":255},"accent6":{"r":112,"g":173,"b":71,"a":255},"hyperlink":{"r":5,"g":99,"b":193,"a":255},"followedHyperlink":{"r":149,"g":79,"b":114,"a":255}},"headingFont":"'Calibri Light'","bodyFont":"Calibri"},"data":{"dataTable":{"0":{"0":{"value":"区域"},"1":{"value":"产线"}},"1":{"0":{"value":1},"1":{"value":"一线"}},"2":{"0":{"value":1},"1":{"value":"二线"}},"3":{"0":{"value":2},"1":{"value":"一线"}},"4":{"0":{"value":2},"1":{"value":"二线"}},"5":{"0":{"value":1},"1":{"value":"一线/二线"}},"6":{"0":{"value":2},"1":{"value":"一线/二线"}}},"defaultDataNode":{"style":{"backColor":null,"foreColor":"Text 1 0","vAlign":2,"font":"normal normal 14.6667px Calibri","themeFont":"Body","formatter":"General","borderLeft":null,"borderTop":null,"borderRight":null,"borderBottom":null,"locked":true,"textIndent":0,"wordWrap":false,"textDecoration":0,"isVerticalText":false,"textOrientation":0}}},"rowHeaderData":{"defaultDataNode":{"style":{"themeFont":"Body"}}},"colHeaderData":{"defaultDataNode":{"style":{"themeFont":"Body"}}},"columns":[{"size":87},{"size":78}],"leftCellIndex":0,"topCellIndex":0,"selections":{"0":{"row":15,"col":5,"rowCount":1,"colCount":1},"length":1},"defaults":{"colHeaderRowHeight":20,"colWidth":64,"rowHeaderColWidth":40,"rowHeight":18.67,"_isExcelDefaultColumnWidth":true},"rowOutlines":{"items":[]},"columnOutlines":{"items":[]},"cellStates":{},"states":{},"outlineColumnOptions":{},"autoMergeRangeInfos":[],"shapeCollectionOption":{"snapMode":0},"index":1,"order":1}},"sheetTabCount":0,"namedStyles":[{"backColor":null,"foreColor":"Text 1 0","hAlign":3,"vAlign":2,"font":"normal normal 14.6667px Calibri","themeFont":"Body","formatter":"General","borderLeft":null,"borderTop":null,"borderRight":null,"borderBottom":null,"locked":true,"textIndent":0,"wordWrap":false,"textDecoration":0,"imeMode":1,"name":"Normal","isVerticalText":false,"textOrientation":0},{"backColor":null,"foreColor":"Text 1 0","hAlign":3,"vAlign":2,"font":"normal normal 14.6667px 等线","themeFont":"Body","formatter":"General","borderLeft":null,"borderTop":null,"borderRight":null,"borderBottom":null,"locked":true,"textIndent":0,"wordWrap":false,"textDecoration":0,"name":"__builtInStyle0","isVerticalText":false,"textOrientation":0}],"namedPatterns":{}}
  //   // curSpread.value.fromJSON(jsonStr);
  //   await nextTick(() => { 
  //     if(dataForm.value.templateConfig){
  //       console.log(JSON.parse(dataForm.value.templateConfig));
  //       setTimeout(() => {
  //         curSpread.value.fromJSON(JSON.parse(dataForm.value.templateConfig));
  //       }, 500);
  //     }
  //   });
  // }

  const safeReset = async () => {
    await nextTick();
    await new Promise(r => requestAnimationFrame(r));

    try {
      await resetFields();
    } catch (e) {
      console.warn("form 尚未完成渲染，跳过 reset");
    }
  };
  //初始化
  async function initData(record) {
    // if(record?.config){
    //   config.value = record.config;
    //   // 先重置表单
    //   await resetFields(); // 清空所有字段
    // }
    
    if(record?.config){
      config.value = record.config;
    }
    await safeReset();

    pageLoading.value = true;
    try {
      dataForm.value = record;
      //获取PI配置
      getPiConfigData();
     
      // 1. 更新表单
      if(record?.config){
        await nextTick();
        setFieldsValue({
          reportName: record.reportName,
          sheetName: record.sheetName || 'Sheet1'
        });
      }
      //isFalse.value = true;
      // 2. 等待 Spread 实例就绪
      // if (!curSpread.value) {
      //   console.warn('等待 Spread 实例初始化...');
      //   await new Promise(resolve => {
      //     const checkInterval = setInterval(() => {
      //       if (curSpread.value) {
      //         clearInterval(checkInterval);
      //         resolve(true);
      //       }
      //     }, 50);
      //   });
      // }
      //resetSpread();
      // 3. 如果没有模板配置，重置为空白
      if (!dataForm.value.templateConfig) {
        pageLoading.value = false;
        return;
      }
      
      // 4. 加载模板配置
      const spread = curSpread.value!;
      const config = JSON.parse(dataForm.value.templateConfig);
      
      // // 暂停所有操作
      // spread.suspendPaint();
      // spread.suspendEvent();
      // // 清空当前内容
      // spread.fromJSON({});
      // // 等待一个非常短的延迟
      // await new Promise(resolve => setTimeout(resolve, 1));
      // // 加载新配置
      // spread.fromJSON(config);
      // // 恢复操作
      // spread.resumeEvent();
      // spread.resumePaint();
      
      // 等待 DOM 完全稳定
      await nextTick();
      await new Promise(resolve => requestAnimationFrame(resolve));
      //const spread = curSpread.value!;

      // 彻底清空
      spread.suspendPaint();
      spread.suspendEvent();

      //spread.fromJSON({});

      // 等待一帧
      await new Promise(resolve => requestAnimationFrame(resolve));

      // 重新加载
      if(config){
        spread.fromJSON(config);
      }
      

      // 恢复
      spread.resumeEvent();
      spread.resumePaint();

      // 关键：强制尺寸重算
      spread.invalidateLayout();
      spread.refresh();

      // 再延迟一次 repaint（防止 Vue 异步干扰）
      setTimeout(() => {
        const sheet = spread.getActiveSheet();
        sheet?.repaint();
        spread.refresh();
      }, 0);


      // 5. 强制刷新所有sheet
      spread.sheets.forEach((sheet, index) => {
        sheet.resumePaint();
        sheet.options.isProtected = false;
        sheet.repaint();
      });
      
      // 6. 整体刷新
      spread.refresh();
      
      // 7. 确保编辑功能正常
      const activeSheet = spread.getActiveSheet();
      if (activeSheet) {
        // 启用编辑
        activeSheet.options.protectionOptions = {
          allowSelectLockedCells: true,
          allowSelectUnlockedCells: true,
          allowSort: true,
          allowFilter: true,
          allowEditObjects: true,
          allowResizeRows: true,
          allowResizeColumns: true
        };
        activeSheet.options.isProtected = false;
        
        // 重新计算公式
        activeSheet.recalcAll();

        // 获取当前默认显示的 Sheet
        const sheet = spread.getActiveSheet();
        const sheetName = sheet.name();
        console.log('默认显示的 Sheet 名称:', sheetName);
        if(sheetName){
          setTimeout(() => {
            setPidata(sheetName);
            setFieldsValue({
              sheetName: sheetName
            });
          },1000);
        }

      }
      //查看禁止编辑
      if(!record?.config){
        prohibitEdit(spread);
         //查看模式 隐藏 新建标签 按钮
        spread.options.newTabVisible = false;
      }
      // 禁用修改Sheet名称
      spread.options.tabEditable = false;
      console.log('SpreadJS 模板加载成功');
    } catch (error) {
      console.error('加载失败:', error);
      createMessage.error('加载表格模板失败');
    } finally {
      // 延迟关闭loading，确保渲染完成
      setTimeout(() => {
        pageLoading.value = false;
      }, 100);
    }
  }
  //禁止编辑
  function prohibitEdit(spread){
    // 或只隐藏编辑按钮
    spread.options.tabEditable = false; // 禁止双击标签编辑名称
    spread.options.tabNavigationVisible = true; // 保留导航按钮
    spread.options.showTabStripAddButton = false;  // 隐藏添加按钮
    // spread.options.allowContextMenu = false; //禁止所有右键

    let sheetCount = spread.getSheetCount();
    for (let i = 0; i < sheetCount; i++) {
      let sheet = spread.getSheet(i);
      // 设置工作表保护
      sheet.options.isProtected = true;
      
      // 设置保护选项（根据需要调整）
      sheet.options.protectionOptions = {
          allowSelectLockedCells: true,      // 禁止选择锁定单元格
          allowSelectUnlockedCells: false,    // 禁止选择解锁单元格
          allowEditObjects: false,            // 禁止编辑对象
          allowSort: false,                   // 禁止排序
          allowFilter: true,                 // 禁止筛选
          allowResizeRows: false,             // 禁止调整行高
          allowResizeColumns: false,          // 禁止调整列宽
          allowDragInsertRows: false,         // 禁止拖拽插入行
          allowDragInsertColumns: false,      // 禁止拖拽插入列
          allowInsertRows: false,             // 禁止插入行
          allowInsertColumns: false,          // 禁止插入列
          allowDeleteRows: false,             // 禁止删除行
          allowDeleteColumns: false,          // 禁止删除列
          allowDragFill: false,               // 禁止拖拽填充
          allowDragDrop: false,               // 禁止拖拽
          allowClipboardPaste: false          // 禁止粘贴
      };
    }
  }

  //获取PI配置(通过mainId查询)
  async function getPiConfigData(){
    const res = await getPiConfig({spreadjsMainId: dataForm.value.id});
    if(res){
      sheetGrouped.value = res.reduce((acc, item) => {
        if (!acc[item.sheetName]) {
          if(item.fieldNames){
            item.fieldNameArr = item.fieldNames.split(',')
          }
          acc[item.sheetName] = item;
        }
        return acc;
      }, {});
    }
  }
  //pi设置参数
 async function setPidata(sheetName){
    const piObj = sheetGrouped.value[sheetName];
    removeAllConditions();
    if(piObj){
      setFieldsValue({
        tableId: piObj.tableId,
        fieldIds: piObj.fieldNameArr,
        id: piObj.id
      });
      const newData = cloneDeep(piObj);
      const conditions = newData.conditionJson ? JSON.parse(newData.conditionJson): '';
      setTimeout(async () => {
        for(const item of conditions){
          await addCondition(item.field, item.value, item.symbol, item.relationship);
        }
      }, 100);
      //字段集合为空,重新获取
      //if(!piFieldOptions.value || piFieldOptions.value.length === 0){
      getFiledData(piObj.tableId);
      //}
    }else{
      setFieldsValue({
        tableId: undefined,
        fieldIds: undefined,
        id: ''
      });
    }
  }

  //保存
  async function submitForm(){
    // 导出 无公式 无样式
    curSpread.value.setActiveSheetIndex(0);
    const jsonStr = JSON.stringify(curSpread.value.toJSON());
    console.log(jsonStr);

    dataForm.value.templateConfig = jsonStr
    //提交表单
    await saveOrUpdate(dataForm.value, true);
  }

  
  // 清理方法 
  // function resetSpread() {
  //   if (curSpread.value) {
  //     // 重置为空的 Workbook
  //     curSpread.value.fromJSON({});
  //   }
  // };

  function resetSpread() {
    if (!curSpread.value) return;
    const spread = curSpread.value;

    spread.suspendPaint();
    spread.suspendEvent();

    // 清空
    spread.clearSheets();

    // 新建一个干净 sheet
    const sheet = new GC.Spread.Sheets.Worksheet("Sheet1");
    spread.addSheet(0, sheet);

    spread.resumeEvent();
    spread.resumePaint();

    // 关键恢复步骤
    spread.options.calcOnDemand = false;

    sheet.options.isProtected = false;

    sheet.recalcAll();
    spread.invalidateLayout();
    spread.refresh();
    sheet.repaint();
  }

  //报表添加字段(数据表头)
  async function addDataTable(){
    if (!curSpread.value) return;
    pageLoading.value = true;

    try {
      const formData = getFieldsValue();
      //清空
      clearSheetContent(formData.sheetName);

      //const dataArray = toRaw(formData.fieldIds).map(item => item.label?.[0]?.props?.title);
      const keyArray = formData.fieldIds
      ? formData.fieldIds.split(',')
      : [];
      const dataArray = keyArray.map(val => {
        const item = piFieldOptions.value.find(opt => opt.value === val)
        return item?.label || ''
      })
      //const keyArray = fieldIds.map(item => item.key);

      console.log(dataArray, keyArray);

      const conditionJson = [] as any;
      //处理数据
      const values = await validate();
      for (let key in values) {
        //判断条件表单项前缀，处理数据
        if (key.startsWith('c_field_') && values[key]) {
          conditionJson.push({field: values[key], value: values[`value_${key}`], symbol: values[`symbol_${key}`], relationship: values[`relationship_${key}`]});
        }
      }
      //dataForm.value.conditionJson = JSON.stringify(conditionJson);
      const conditionJsonStr = JSON.stringify(conditionJson)
      // 保存PI配置
      await savePiConfig({
        sheetName: formData.sheetName,
        spreadjsMainId: dataForm.value.id,
        tableId: formData.tableId,
        fieldNames: keyArray.join(','),
        conditionJson: conditionJsonStr,
        id: formData.id
      });

      // 获取数据
      const obj = {
        tableId: formData.tableId,
        pageNo: 1,
        pageSize: 100000,
        orderField: 'sort',
        orderType: 'asc',
        otherConditionJson: conditionJsonStr
      };
      const res = await baseDataList(obj);
      const dataArr = res.records || [];

      // 构造最终写入的数据（表头 + 数据）
      const resultArray = dataArr.map(item => keyArray.map(key => item[key] ?? ''));
      const finalData = [dataArray, ...resultArray];

      // 一次性写入，清空旧数据
      addData(finalData, 0, 0, true);

    } catch (err) {
      console.error(err);
    } finally {
      pageLoading.value = false;
    }
  }

  /**
   * 表格填充数据 
   * 数据  行  列
   **/
  function addData(dataArray, rowNum, colNum , clearBefore = true) {
    if (!curSpread.value || !Array.isArray(dataArray) || dataArray.length === 0) {
        console.warn('Spread实例不存在或数据为空');
        return;
    }
    const spread = curSpread.value;
    const activeSheet = spread.getActiveSheet();
    // 同时暂停事件 + 重绘
    spread.suspendPaint();
    spread.suspendEvent();
    try {
      if (!Array.isArray(dataArray[0])) {
          console.error('dataArray 必须是二维数组');
          return;
      }
      const rowCount = dataArray.length;
      const colCount = dataArray[0].length;
      // 扩展列
      const needColCount = colNum + colCount;
      if (activeSheet.getColumnCount() < needColCount) {
          activeSheet.setColumnCount(needColCount);
      }
      // 扩展行
      const needRowCount = rowNum + rowCount;
      if (activeSheet.getRowCount() < needRowCount) {
          activeSheet.setRowCount(needRowCount);
      }
      // 使用 setArray
      activeSheet.setArray(rowNum, colNum, dataArray);
      // 自动列宽
      for (let c = 0; c < colCount; c++) {
          const columnIndex = colNum + c;
          let maxWidth = 100;

          for (let r = 0; r < rowCount; r++) {
              const cellValue = dataArray[r][c];
              if (cellValue != null) {
                  maxWidth = Math.max(maxWidth, String(cellValue).length * 8 + 20);
              }
          }

          activeSheet.setColumnWidth(columnIndex, Math.min(Math.max(maxWidth, 80), 300));
      }
    } catch (e) {
        console.error('写入数据失败:', e);
    } finally {
        // 恢复
        spread.resumeEvent();
        spread.resumePaint();
        // 强制重算布局
        spread.invalidateLayout();
        spread.refresh();

        setTimeout(() => {
          activeSheet.repaint();
          spread.refresh();
        }, 0);
    }
  }

  /**
   * 下拉选项筛选
   * @param input 输入的值 
   * @param option 选项
   * @param searchField 需要查找的字段 
   */
  function handleFilterOption(input, option, searchField) {
    return option[searchField].toLowerCase().indexOf(input.toLowerCase()) >= 0;
  }

  /**
   * 下拉改变监听
   */
  function handleChange(value, option){
    // processingConditionArray();
  }

  //表单提交事件
  async function handleSubmit() {
    
  }
  /**
   * 删除所有条件（彻底）
   */
  async function removeAllConditions() {
    if (!conditionFieldIds.value.length) return;

    // 1批量删除 schema
    const fieldsToRemove: string[] = [];

    conditionFieldIds.value.forEach((field) => {
      fieldsToRemove.push(
        field,
        `relationship_${field}`,
        `symbol_${field}`,
        `value_${field}`,
        `index_${field}`,
      );
    });

    await removeSchemaByFiled(fieldsToRemove);

    // 2清空所有值（防止残留）
    const resetObj: Record<string, any> = {};
    fieldsToRemove.forEach((key) => {
      resetObj[key] = null;
    });
    await setFieldsValue(resetObj);

    // 3重置状态
    conditionFieldIds.value = [];
    conditionTotal.value = 0;
  }


  /**
 * 删除条件
 */
  async function removeCondition(field){
    conditionTotal.value--;
    removeSchemaByFiled([field, 'value_'+field]);
    // processingConditionArray();
  }

  /**
 * 添加条件
 */
  async function addCondition(conditionField = '', conditionValue= '', conditionSymbol= '', conditionRelationship= ''){
    debugger
    conditionTotal.value++;
    //生成唯一值
    const id = 'c_field_'+dayjs().valueOf().toString(36)+conditionTotal.value;
    await appendSchemaByField(
      {
        field: id,
        component: 'Select',
        label: '条件',
        colProps: { span: 24 },
        slot: 'conditionSlot',
        dynamicRules: function(obj){
          return [
            {
              //默认开启表单检验
              required: false,
              validator: (_, value) => {
                //需要return 一个Promise对象
                return new Promise((resolve, reject) => {
                  const model = obj.model;
                  const field = obj.field;
                  if(value || model['symbol_'+field] || model['value_'+field]){
                    if (!value) {
                      reject('请选择字段');
                    }
                    if (!model['symbol_'+field]) {
                      reject('请选择条件');
                    }
                    if (!model['value_'+field]) {
                      reject('请输入值');
                    }
                  }
                  resolve();
                });
              },
            },
          ];
        },
      },
      ''
    );
    await appendSchemaByField(
      {
        field: `relationship_${id}`,
        component: 'Input',
        label: '关系',
        show: false,
      },
      ''
    );
    await appendSchemaByField(
      {
        field: `symbol_${id}`,
        component: 'Input',
        label: '条件符号',
        show: false,
      },
      ''
    );
    await appendSchemaByField(
      {
        field: `value_${id}`,
        component: 'Input',
        label: '条件值',
        show: false,
      },
      ''
    );
    await appendSchemaByField(
      {
        field: `index_${id}`,
        component: 'Input',
        label: '序号',
        show: false,
      },
      ''
    );
    if(conditionField){
      //分开赋值，避免编辑时校验异常
      await setFieldsValue({['relationship_'+id]: conditionRelationship});
      await setFieldsValue({['symbol_'+id]: conditionSymbol});
      await setFieldsValue({['value_'+id]: conditionValue});
      await setFieldsValue({[id]: conditionField});
      await setFieldsValue({['index_'+id]: conditionTotal.value});
    }else{
      await setFieldsValue({['index_'+id]: conditionTotal.value});
    }
    conditionFieldIds.value.push(id);
  }

  const fixLayout = async () => {
    await nextTick();
    await new Promise(r => requestAnimationFrame(r));

    const spread = curSpread.value;
    if (!spread) return;

    spread.invalidateLayout();
    spread.refresh();
    spread.getActiveSheet()?.repaint();
  };

  /**
 * 清空指定Sheet的所有内容（数据和格式）
 * @param {string} sheetName - 要清空的Sheet名称
 */
  const clearSheetContent = (sheetName) => {
      if (!curSpread.value) return;
      
      const spread = curSpread.value;
      const sheet = spread.getActiveSheet();
      
      if (!sheet) {
          console.warn(`Sheet "${sheetName}" 不存在`);
          return;
      }
      
      console.log(`清空Sheet: ${sheetName}`);
      
      // 暂停绘制，提高性能
      spread.suspendPaint();
      
      // 获取当前行列数
      const rowCount = sheet.getRowCount();
      const colCount = sheet.getColumnCount();
      
      // 清空所有单元格的数据（保留行列结构）
      if (rowCount > 0 && colCount > 0) {
          sheet.clear(0, 0, rowCount, colCount, GC.Spread.Sheets.SheetArea.viewport, 
                    GC.Spread.Sheets.StorageType.data);
      }
      
      // 移除所有合并单元格
      const spans = sheet.getSpans();
      spans.forEach(span => {
          sheet.removeSpan(span.row, span.col, span.rowCount, span.colCount);
      });
      
      // 恢复绘制
      spread.resumePaint();
      
      // 强制刷新
      sheet.repaint();
      spread.refresh();
      
      console.log(`Sheet "${sheetName}" 清空完成`);
  };
  //获取PI集合
  async function getFiledData(tableId) {
    const dictData = await initDictOptions(
      "mes3_pi_customize_table_field where table_id = '"+tableId+"', name, field_name"
    );
    console.log('加载完成，数据:', dictData);
    piFieldOptions.value = dictData || [];
  }

  // 监听 tableId 变化
  watch(
    piTableId,
    async () => {
      //清空条件
      removeAllConditions()
    },
    { 
      immediate: true // 立即执行一次，处理初始值
    }
  );


  // 颜色选择
  const colorDrawerOk = (color) => {
    if (!curSpread.value) return;
    const spread = curSpread.value;
    const sheet = spread.getActiveSheet();
    const selections = sheet.getSelections();

    if (!selections?.length) return;

    const Commands = GC.Spread.Sheets.Commands;
    const options: any = { cmd: 'setBackColor' };

    Commands.startTransaction(spread, options);

    // 给选区设置背景色
    selections.forEach(sel => {
      sheet.getRange(sel.row, sel.col, sel.rowCount, sel.colCount).backColor(color);
    });

    Commands.endTransaction(spread, options);

    pendingSpread.value = null;
  };
  






  // 暴露方法
  defineExpose({
    initData,
    submitForm,
    resetSpread,
    fixLayout,
  });
</script>

<style lang="less" scoped>
  .jeecg-basic-table-form-container {
    padding: 0;
    .spreadContainer {
      display: flex;
      padding: 0px;
      padding-left: 0;
      height: 76vh;
    }
    .spreadHost {
      width: 99%;
      height: 100%;
    }
    .custom-upload {
      display: flex;
      /deep/ .ant-upload-list {
        margin-left: 10px;
      }
    }
  }

  .div-border {
    border: 1px solid #C1C0B6;   /* 1px 实线 灰色边框 */
    height: 100%;
  }

</style>
