<template>
  <div class="app-container">
    <a-spin :spinning="pageLoading">
      <a-space>
        <a-date-picker
          v-model:value="yearMonth"
          picker="month"
          format="YYYY-MM"
          value-format="YYYY-MM"
          placeholder="选择月份"
          :allow-clear="false"
          :input-read-only="true"
          style="width: 128px"
        />
        <a-date-picker
          v-if="isOrsExport"
          v-model:value="selDay"
          format="DD号"
          value-format="YYYY-MM-DD"
          placeholder="选择日期"
          :allow-clear="false"
          :input-read-only="true"
          :disabled-date="disabledDate"
          style="width: 96px"
          @change="selDayChangeHandler"
        />
        <a-button type="primary" @click="showReportById">查询</a-button>
        <a-select v-show="showFilterDept" v-model:value="selDept" placeholder="请选择部门" allow-clear style="width: 150px" @change="filterDeptRow">
          <a-select-option v-for="item in deptList" :key="item" :value="item">
            {{ item }}
          </a-select-option>
        </a-select>
        <a-button v-show="showBack" @click="goBack">返回</a-button>
        <a-button v-show="showUpdateBtn" type="success" @click="saveUpdateData"> 保存所有修改 </a-button>
        <a-switch v-model:checked="showTbHeader" checked-children="显示行列表头" un-checked-children="隐藏行列表头" />
        <a-switch
          v-model:checked="onlyExportValue"
          checked-children="仅导出数值"
          un-checked-children="导完整表格"
          active-color="#1ec63b"
          inactive-color="#dd001b"
          @change="handlerExportValue"
        />
        <a-button :loading="exportBtnLoading" type="primary" @click="exportXlsx"> 导出Excel </a-button>
        <a-button v-show="showBackfillBtn" type="success" @click="setBackfillTagDateRange"> 重算所选区域数据 </a-button>
        <a-button v-show="showAnalysisBtn" type="warning" @click="showAnalysisModal">分析点位数据</a-button>
      </a-space>

      <div class="spread-container">
        <gc-spread-sheets hostClass="spreadHost" @workbook-initialized="workbookInitialized">
          <GcWorksheet />
        </gc-spread-sheets>

        <combined-chart
          v-if="showSpreadChart"
          class-name="spread-chart"
          :x-title="xTitle"
          :yaxis-datas="yaxisDatas"
          :x-datas="xDatas"
          :serie-datas="serieDatas"
          :legend-datas="legendDatas"
        />

        <div v-if="totalCharge.show" class="total-charge">
          <div>
            <span>预算总电费：</span>
            <span>
              <em>￥</em><em v-for="item in totalCharge.budgetHtml" :key="item">{{ item }}</em>
            </span>
          </div>
          <div>
            <span>预测总电费：</span>
            <span :style="'color:' + totalCharge.fontColor">
              <em>￥</em><em v-for="item in totalCharge.forecastHtml" :key="item">{{ item }}</em>
            </span>
          </div>
          <div>
            <span>实际总电费：</span>
            <span>
              <em>￥</em><em v-for="item in totalCharge.practicalHtml" :key="item">{{ item }}</em>
            </span>
          </div>
        </div>
      </div>
    </a-spin>

    <DataAnalysisModal ref="analysisModal" />
  </div>
</template>

<script lang="ts" name="ems-ReportShow" setup>
  import { ref, unref, onUnmounted, watch, onMounted, nextTick, computed } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { useMultipleTabStore } from '/@/store/modules/multipleTab';
  import CombinedChart from './components/CombinedChart.vue';
  import '@grapecity/spread-sheets/styles/gc.spread.sheets.excel2016colorful.css';
  import * as GC from '@grapecity/spread-sheets';
  import { GcSpreadSheets, GcWorksheet } from '@grapecity/spread-sheets-vue';
  import '@grapecity/spread-sheets-resources-zh';
  import '@grapecity/spread-sheets-io';
  import dayjs from 'dayjs';
  import minMax from 'dayjs/plugin/minMax';
  import { useTabs } from '/@/hooks/web/useTabs';
  import FaverSaver from 'file-saver';
  import { useMessage } from '@/hooks/web/useMessage';
  import * as MySpreadUtils from '@/utils/spreadUtils';
  import * as CombinedChartApi from './components/CombinedChart.api';
  import * as RepDataLoader from './ReportShow.api';
  import * as RepFunApi from './ReportFunction.api';
  import * as RepSetApi from './ReportSetting.api';
  import { getAnalysisTagData } from './ReportData.api';
  import { baseDataList } from './ReportFunction.data';
  import { GrainTypeEnum, TagKindEnum } from '@/enums/EmsEnum';
  import DataAnalysisModal from '../components/DataAnalysisModal.vue';

  dayjs.extend(minMax);
  const { setTitle } = useTabs();
  const { createMessage, createConfirm } = useMessage();
  const route = useRoute();
  const router = useRouter();
  const tabStore = useMultipleTabStore();

  // 初始化 Spread
  GC.Spread.Common.CultureManager.culture('zh-cn');
  let SpreadJSKey =
    'emsapac.ab-inbev.cn,623376735916589#B1v4r7N0dHWyBDcwkXVMJ7Qr9UWtpWUYlzbzgjUORkUY3GOzEVNHZFNp5kZGdXc4xEbvAVUOh6VGJWWWdTORJjTMRWdyoXS4B5MJNHdwAlVmt6MRNTb4gTTxMzVrUTNhZldQ94Yx2yVrlzT4JFV6U7KQdlMB3SZmVFbzUFTxJVVN9kUNdFOHJXcDN4YxpmTPl5VsBXRz2UQvsGcyAHTqlEa8YmRk5UMhd4dCtkTDpmVCl6aQdETKJmUNNUZyAnSrd6Y68kc8VnV62GN4Elc4QjWFR6Sv2iWy24Yt5UYFR7RhZXaXNjU6hjUY3WcJhmetNUR5ZGTZNkS85EcU3GbwdlI0IyUiwiI8QUNyADNGZjI0ICSiwSO7AzNyQTO8UTM0IicfJye&Qf35VficFVDplI0IyQiwiI6EjL6ByUKBCZhVmcwNlI0IiTis7W0ICZyBlIsISOzMDM8ADI9ETNwMjMwIjI0ICdyNkIsIibj9idlJmbp5iYh9yYhBXYz5WZiojIz5GRiwiI8+Y9sWY9QmZ0Jyp9Li09leb9WyY9oqY9qeI0R+K0+0L0JGr9m6q9iojIh94QiwiI9gTN6ETO5MzN6czMzIjNiojIklkIs4XXbpjInxmZiwSZzxWYmpjIyNHZisnOiwmbBJye0ICRiwiI34TQmBjSNNlZJtUOGxEVxZje75kY8MjWMdER6QmMrg7RmZTbLRUQRp7cM9GZ5dWRv2kTsdUN6onewpnepB5ZhljWxN4dnRUaSdTOGNWWUxkbFhlZrpVb7q3'; // Enter a valid license key.
  GC.Spread.Sheets.LicenseKey = SpreadJSKey;

  // 定义加载状态枚举
  enum LoadingState {
    Loading = 1,
    Success = 2,
    Error = 3,
  }
  // 响应式变量
  const loadingState = ref<LoadingState>(LoadingState.Loading); // 加载状态（1-加载中，2-成功）
  const loadingSecond = ref(0); // 加载计时秒数
  let loadingTimer: ReturnType<typeof setInterval> | null = null; // 定时器引用

  // 类型定义
  interface TotalCharge {
    budgetUsage: number; // 预算总电费
    budgetHtml: string[]; // 预算总电费特效
    forecastUsage: number; // 预测总电费
    forecastHtml: string[]; // 预测总电费特效
    practicalUsage: number; // 实际总电费
    practicalHtml: string[]; // 实际总电费
    fontColor: string; // 预测电费变色
    show: boolean;
  }

  interface SheetData {
    name: string;
    value: any[][];
    row: number;
  }

  // 响应式变量
  const pageLoading = ref(false);
  const curSpread = ref<GC.Spread.Sheets.Workbook | null>(null);
  const curSheetName = ref('');
  const reportTitle = ref('');
  const repId = ref(route.query.id as string);
  const repName = ref(route.query.name as string);
  const energy = ref(route.query.energy as string);
  const yearMonth = ref(dayjs().format('YYYY-MM'));
  const selDay = ref(dayjs().subtract(1, 'day').format('YYYY-MM-DD'));
  const showBack = ref(!window.location.href.includes('/report/show'));
  const showUpdateBtn = ref(false);
  const showFilterDept = ref(false);
  const showTbHeader = ref(true);
  const retryBtn = ref(false);
  const useLucky = ref(false);
  const deptList = ref<string[]>([]);
  const baseDataNames = ref<string[]>([]);
  const selDept = ref('');
  const origindataObj = ref<Record<string, any[]>>({});
  const showSpreadChart = ref(false);
  const xTitle = ref('');
  const yaxisDatas = ref<any[]>([]);
  const xDatas = ref<any[]>([]);
  const serieDatas = ref<any[]>([]);
  const legendDatas = ref<string[]>([]);
  const totalCharge = ref<TotalCharge>({
    budgetUsage: 0, // 预算总电费
    budgetHtml: [], // 预算总电费特效
    forecastUsage: 0, // 预测总电费
    forecastHtml: [], // 预测总电费特效
    practicalUsage: 0, // 实际总电费
    practicalHtml: [], // 实际总电费
    fontColor: '', // 预测电费变色
    show: false,
  });
  const exportBtnLoading = ref(false);
  const onlyExportValue = ref(false);
  const sheetDataList = ref<SheetData[]>([]);
  const isOrsExport = ref(false);
  const activeSheetData = ref<any[][]>([]);

  const analysisModal = ref<InstanceType<typeof DataAnalysisModal>>();
  const showAnalysisModal = () => {
    const filterResult = getHandSelectedValue();
    if (
      filterResult.length === 0 ||
      filterResult.length > 1 ||
      filterResult[0].tagIds.split(',').length > 1 ||
      filterResult[0].dayDates.split(',').length > 1
    ) {
      createMessage.warning('请在表格中选取一个有效的用量单元格，且只能选择一个！', 5);
    } else {
      pageLoading.value = true;
      //请求接口获取所选单元格的点位及数据
      const selTagDate = filterResult[0];
      getAnalysisTagData({
        tagKind: getCurrentTagKind(),
        grainType: getCurrentGrainType(),
        yearMonth: yearMonth.value,
        tagId: selTagDate.tagIds,
        dayDate: selTagDate.dayDates,
        classFlag: selTagDate.classFlags,
      })
        .then((res) => {
          res.usage = selTagDate.validValues;
          res.grainType = getCurrentGrainType();
          analysisModal.value?.openModal(res);
        })
        .finally(() => {
          pageLoading.value = false;
        });
    }
  };

  //暂时不用：计算属性
  //const fullPath = computed(() => route.fullPath);

  //设置当前Tab页标题
  setReportTabTitle();

  // Watch
  watch(yearMonth, (newVal) => {
    if (dayjs(newVal).month() === dayjs().month()) {
      selDay.value = dayjs().subtract(1, 'day').format('YYYY-MM-DD');
    } else {
      selDay.value = dayjs(newVal).endOf('month').format('YYYY-MM-DD');
    }
  });

  watch(
    showTbHeader,
    (newVal) => {
      const sheet = curSpread.value?.getActiveSheet();
      if (sheet) {
        sheet.options.rowHeaderVisible = newVal;
        sheet.options.colHeaderVisible = newVal;
        if (sheet.name().includes('图表') && sheet.getValue(0, 0) === '图表') {
          showSpreadChart.value = !newVal;
        }
      }
    },
    { immediate: true }
  );

  // 生命周期
  onMounted(() => {
    if (dayjs().date() === 1) {
      yearMonth.value = dayjs().subtract(1, 'day').format('YYYY-MM');
    }
    loadingTimer = setInterval(checkLoadingState, 1500);
    showReportById();
  });

  // 生命周期钩子：组件卸载时清除定时器
  onUnmounted(() => {
    clearLoadingTimer();
  });

  //是否显示重算按钮
  const showBackfillBtn = computed(
    () =>
      baseDataList.map((item) => item.label).includes(curSheetName.value) &&
      !curSheetName.value.includes('手输') &&
      !curSheetName.value.includes('第三方') &&
      !curSheetName.value.includes('小时') &&
      !curSheetName.value.includes('周') &&
      !curSheetName.value.includes('MTD')
  );
  const showAnalysisBtn = computed(
    () =>
      baseDataList.map((item) => item.label).includes(curSheetName.value) &&
      (curSheetName.value.includes('中间表日') || curSheetName.value.includes('中间表班'))
  );

  //根据当前报表名称返回点位类型
  const getCurrentTagKind = () => {
    if (curSheetName.value.includes('采集')) {
      return TagKindEnum.COLLECT;
    } else if (curSheetName.value.includes('手输')) {
      return TagKindEnum.MANUAL;
    } else if (curSheetName.value.includes('第三方')) {
      return TagKindEnum.THIRDPARTY;
    } else if (curSheetName.value.includes('中间')) {
      return TagKindEnum.ORSDATA;
    } else {
      return 0;
    }
  };
  //根据当前报表名称返回时间粒度
  const getCurrentGrainType = () => {
    if (curSheetName.value.includes('半小时')) {
      return GrainTypeEnum.HALFHOUR;
    } else if (curSheetName.value.includes('小时')) {
      return GrainTypeEnum.HOUR;
    } else if (curSheetName.value.includes('班')) {
      return GrainTypeEnum.SHIFT;
    } else if (curSheetName.value.includes('日')) {
      return GrainTypeEnum.DAY;
    } else if (curSheetName.value.includes('周')) {
      return GrainTypeEnum.WEEK;
    } else if (curSheetName.value.includes('MTD')) {
      return GrainTypeEnum.MONTH;
    } else {
      return 0;
    }
  };

  //更新报表的名称
  function setReportTabTitle() {
    const { currentRoute } = router;
    const route = unref(currentRoute);
    const tab = tabStore.getTabList.find((item) => item.fullPath === route.fullPath)!;
    setTitle(repName.value, tab);
  }

  //SpreadJs Workbook 初始化
  const workbookInitialized = (spread: GC.Spread.Sheets.Workbook) => {
    curSpread.value = spread;
    spread.refresh();
    spread.bind(GC.Spread.Sheets.Events.ActiveSheetChanged, handleActiveSheetChange);
    console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Init Spread Completed: ${spread}`);
  };
  //Workbook Sheet 切换事件
  const handleActiveSheetChange = (e, args) => {
    console.log(e);
    const name = args.newSheet.name();
    //记录一下当前打开的 SheetName，后面重新加载基础数据时默认切换到此 Sheet
    curSheetName.value = name;
    const sheet = curSpread.value!.getActiveSheet();
    sheet.resumePaint();
    sheet.repaint();
    sheet.options.rowHeaderVisible = showTbHeader.value;
    sheet.options.colHeaderVisible = showTbHeader.value;
    //如果是图表Sheet则显示图表层
    if (name.includes('图表') && sheet.getValue(0, 0) === '图表') {
      showSpreadSheetChart(sheet);
    } else {
      showSpreadChart.value = false;
    }
    //更新显示或隐藏
    updateSheetState(sheet);
  };
  //根据 Sheet 名称显示或隐藏 保存按钮及部门下拉框
  const updateSheetState = (sheet: GC.Spread.Sheets.Worksheet) => {
    const sheetName = sheet.name();
    showUpdateBtn.value = [
      '手输表日数据录入',
      '手输表班数据录入',
      '第三方表日数据修改',
      '第三方表班数据修改',
      '采集表日数据修改',
      '采集表班数据修改',
    ].includes(sheetName);
    showFilterDept.value = ['手输表日数据录入', '手输表班数据录入'].includes(sheetName);
    retryBtn.value = ['手输表日数据录入'].includes(sheetName);
    //批次报表才会用到这个
    if (showFilterDept.value) {
      updateDeptList(sheet);
    }
  };
  const updateDeptList = (sheet: GC.Spread.Sheets.Worksheet) => {
    const data = sheet.getArray(0, 0, sheet.getRowCount(), sheet.getColumnCount());
    const departments = [...new Set(data.map((row) => row[3] || '（空）'))];
    deptList.value = departments;
  };

  //时间组件禁用日期
  function disabledDate(current: dayjs.Dayjs) {
    const minTime = dayjs(yearMonth.value + '-01');
    const maxTime = minTime.endOf('month');
    return current.isAfter(maxTime) || current.isBefore(minTime);
  }
  //回退
  function goBack() {
    router.go(-1);
  }

  // 过滤部门数据
  function filterDeptRow() {
    // 过滤部门数据
    pageLoading.value = true;
    const sheet = curSpread.value!.getActiveSheet();
    if (sheet.name() === '手输表日数据录入') {
      const tabdata = origindataObj.value.EditManualDayUsage.filter((item) => {
        if (selDept.value) {
          if (selDept.value === '（空）') {
            return !item[3];
          } else {
            return item[3] === selDept.value;
          }
        } else {
          return true;
        }
      });
      const addRowNum = origindataObj.value.EditManualDayUsage.length - tabdata.length;
      const nullRow = Array.from({ length: tabdata[0].length }, () => '');
      for (let i = 0; i < addRowNum; i++) {
        tabdata.push([...nullRow]);
      }
      setTimeout(() => {
        setDataToSheet(sheet, tabdata, 2, 'EditManualDayUsage');
        pageLoading.value = false;
      }, 200);
    } else if (sheet.name() === '手输表班数据录入') {
      const tabdata = origindataObj.value.EditManualClassUsage.filter((item) => {
        if (selDept.value) {
          if (selDept.value === '（空）') {
            return !item[3];
          } else {
            return item[3] === selDept.value;
          }
        } else {
          return true;
        }
      });
      const addRowNum = origindataObj.value.EditManualClassUsage.length - tabdata.length;
      const nullRow = Array.from({ length: tabdata[0].length }, () => '');
      for (let i = 0; i < addRowNum; i++) {
        tabdata.push([...nullRow]);
      }
      setTimeout(() => {
        setDataToSheet(sheet, tabdata, 2, 'EditManualClassUsage');
        pageLoading.value = false;
      }, 200);
    }
  }

  // 显示指定ID报表并加载数据
  function showReportById() {
    pageLoading.value = true;
    RepSetApi.getOne(repId.value).then((data) => {
      reportTitle.value = data.repName;
      baseDataNames.value = data.basedataName.split(',');
      useLucky.value = data.basedataName.split(',').includes('中间计算表全量数据');
      loadBaseDataAndExcel(yearMonth.value);
    });
  }

  // 加载基础数据及模板文件
  async function loadBaseDataAndExcel(midJson) {
    pageLoading.value = true;
    curSpread.value!.suspendCalcService(false);
    curSpread.value!.suspendPaint();

    console.log('[' + dayjs().format('YYYY-MM-DD HH:mm:ss') + ']Start Download Excel');

    const handleLoad = (response) => {
      const excelFile: File = response.data;
      console.time('LoadExcel');
      curSpread.value!.import(
        excelFile,
        () => {
          console.timeEnd('LoadExcel');
          queryBasedata();
        },
        (e) => {
          console.log(e);
          queryBasedata();
        }
      );
    };
    if (useLucky.value) {
      console.log('midJson=', midJson);
      //后端接口很长
      RepSetApi.getblobByModel(repId.value, midJson).then(handleLoad);
    } else {
      RepSetApi.getXlsxBlob(repId.value).then(handleLoad);
    }
  }

  //查询基础表数据
  async function queryBasedata() {
    pageLoading.value = true;
    await loadAllData();
    curSpread.value!.resumeCalcService(true);

    if (energy.value === 'water') {
      curSpread.value!.setActiveSheet('图表-水');
    } else if (energy.value === 'co2') {
      curSpread.value!.setActiveSheet('图表-天然气');
    } else if (energy.value === 'electricity') {
      curSpread.value!.setActiveSheet('图表-电');
    } else if (energy.value === 'steam') {
      curSpread.value!.setActiveSheet('图表-蒸气');
    }

    const sheet = curSpread.value!.getActiveSheet();
    sheet.options.rowHeaderVisible = showTbHeader.value;
    sheet.options.colHeaderVisible = showTbHeader.value;

    if (sheet.name() === '采集表日数据修改' || sheet.name() === '采集表班数据修改' || sheet.name() === '手输表日数据录入') {
      const rowColNums = [sheet.getRowCount(), sheet.getColumnCount()];
      const filter = new GC.Spread.Sheets.Filter.HideRowFilter(new GC.Spread.Sheets.Range(2, 0, rowColNums[0] - 2, rowColNums[1] - 1));
      sheet.rowFilter(filter);
      filter.filterDialogVisibleInfo({
        sortByValue: true,
        filterByValue: true,
        listFilterArea: true,
      });
      filter.filterButtonVisible(0, false);
    }
    curSpread.value!.resumePaint();
    if (
      sheet.name() === '手输表日数据录入' ||
      sheet.name() === '手输表班数据录入' ||
      sheet.name() === '第三方表日数据修改' ||
      sheet.name() === '第三方表班数据修改' ||
      sheet.name() === '采集表日数据修改' ||
      sheet.name() === '采集表班数据修改'
    ) {
      const rowCnt = sheet.getRowCount();
      const colCnt = sheet.getColumnCount();
      activeSheetData.value = sheet.getArray(0, 0, rowCnt, colCnt);
      showUpdateBtn.value = true;

      if (sheet.name() === '手输表日数据录入' || sheet.name() === '手输表班数据录入') {
        showFilterDept.value = true;
      }

      if (sheet.name() === '手输表日数据录入') {
        retryBtn.value = true;
      }
    } else {
      showUpdateBtn.value = false;
      showFilterDept.value = false;
      retryBtn.value = false;
    }

    if (sheet.name().includes('图表') && sheet.getValue(0, 0) === '图表') {
      showSpreadSheetChart(sheet);
    }

    const ORSSheet = curSpread.value!.getSheetFromName('ORS');
    if (ORSSheet) {
      isOrsExport.value = true;
      const dayNums = (dayjs().hour(0).minute(0).second(0).millisecond(0).valueOf() - dayjs(selDay.value).valueOf()) / (1000 * 24 * 60 * 60);
      ORSSheet.getCell(0, 0).formula('=TODAY()-' + dayNums);
      curSpread.value!.getActiveSheet().resumePaint();
    }

    pageLoading.value = false;
  }

  //显示或隐藏 Loading
  // const pageLoadingSetter = (show: boolean) => {
  //   pageLoading.value = show;
  // };

  //加载所有数据
  async function loadAllData() {
    console.log('===> Start loadAllData');
    // 封装清除工作表指定类型数据的函数
    const clearSheetData = (sheet, storageTypes) => {
      const rowCnt = sheet.getRowCount();
      const colCnt = sheet.getColumnCount();
      storageTypes.forEach((type) => {
        sheet.clear(0, 0, rowCnt, colCnt, GC.Spread.Sheets.SheetArea.viewport, type);
      });
    };

    // 封装获取或创建工作表的函数
    const getOrCreateSheet = (spread, sheetName) => {
      let sheet = spread.getSheetFromName(sheetName);
      if (!sheet) {
        sheet = new GC.Spread.Sheets.Worksheet(sheetName);
        spread.addSheet(0, sheet);
      }
      return sheet;
    };

    // 临时保留：封装处理包含标签数组的工作表逻辑
    // const handleSheetWithTagArr = (sheet): any[] => {
    //   const tagArr: any[] = [];
    //   for (let i = 2; i <= sheet.getRowCount() - 1; i++) {
    //     const cellValue = sheet.getCell(i, 0).value();
    //     if (cellValue && !tagArr.includes(cellValue)) {
    //       tagArr.push(cellValue);
    //     }
    //   }
    //   return tagArr;
    // };

    // 处理中间数据表
    const orsDataSheets = [
      {
        name: '中间表MTD数据',
        condition: () => curSpread.value!.getSheetFromName('中间表MTD数据') && repName.value !== '中间表MTD数据',
        clearTypes: [GC.Spread.Sheets.StorageType.data, GC.Spread.Sheets.StorageType.style, GC.Spread.Sheets.StorageType.comment],
        loadFunction: () => RepDataLoader.loadOrsMtdUsageData(yearMonth.value.split('-')[0]),
      },
      {
        name: '中间表周数据',
        condition: () => curSpread.value!.getSheetFromName('中间表周数据'),
        clearTypes: [GC.Spread.Sheets.StorageType.data],
        loadFunction: () => RepDataLoader.loadOrsWeekUsageData(yearMonth.value.split('-')[0]),
      },
      {
        name: '中间表日数据',
        condition: () => curSpread.value!.getSheetFromName('中间表日数据'),
        clearTypes: [GC.Spread.Sheets.StorageType.data],
        loadFunction: () => RepDataLoader.loadOrsDayUsageData(yearMonth.value),
      },
      {
        name: '中间表班数据',
        condition: () => curSpread.value!.getSheetFromName('中间表班数据') && repName.value !== '中间表班数据',
        clearTypes: [GC.Spread.Sheets.StorageType.data],
        loadFunction: () => RepDataLoader.loadOrsClassUsageData(yearMonth.value),
      },
      {
        name: '中间表小时数据',
        condition: () => curSpread.value!.getSheetFromName('中间表小时数据'),
        clearTypes: [GC.Spread.Sheets.StorageType.data],
        loadFunction: () =>
          RepDataLoader.loadOrsHourUsageData(
            yearMonth.value.split('-').length === 2
              ? dayjs.min(dayjs(`${yearMonth.value}-01`).endOf('month'), dayjs()).format('YYYY-MM-DD')
              : yearMonth.value
          ),
      },
    ];

    // 处理基础数据表
    const baseDataSheets = [
      {
        name: '采集表班数据',
        loadFunction: () => RepDataLoader.loadCollectClassUsageData(yearMonth.value),
      },
      {
        name: '采集表日数据',
        loadFunction: () => RepDataLoader.loadCollectDayUsageData(yearMonth.value),
      },
      {
        name: '采集表半小时数据',
        loadFunction: () => {
          return RepDataLoader.loadCollectHalfHourUsageData(
            yearMonth.value.split('-').length === 2
              ? dayjs.min(dayjs(`${yearMonth.value}-01`).endOf('month'), dayjs()).format('YYYY-MM-DD')
              : yearMonth.value
          );
        },
      },
      {
        name: '采集表小时数据',
        loadFunction: () => {
          return RepDataLoader.loadCollectHourUsageData(
            yearMonth.value.split('-').length === 2
              ? dayjs.min(dayjs(`${yearMonth.value}-01`).endOf('month'), dayjs()).format('YYYY-MM-DD')
              : yearMonth.value
          );
        },
      },
      {
        name: '采集表周数据',
        loadFunction: () => RepDataLoader.loadCollectWeekUsageData(yearMonth.value.split('-')[0]),
      },
      {
        name: '采集表MTD数据',
        loadFunction: () => RepDataLoader.loadCollectMtdUsageData(yearMonth.value.split('-')[0]),
      },
      {
        name: '手输表班数据',
        loadFunction: () => RepDataLoader.loadManualClassUsageData(yearMonth.value),
      },
      {
        name: '手输表日数据',
        loadFunction: () => RepDataLoader.loadManualDayUsageData(yearMonth.value),
      },
      {
        name: '手输表周数据',
        loadFunction: () => RepDataLoader.loadManualWeekUsageData(yearMonth.value.split('-')[0]),
      },
      {
        name: '手输表MTD数据',
        loadFunction: () => RepDataLoader.loadManualMtdUsageData(yearMonth.value.split('-')[0]),
      },
      {
        name: '第三方小时数据',
        loadFunction: () =>
          RepDataLoader.loadThirdpartyHourUsageData(
            yearMonth.value.split('-').length === 2
              ? dayjs.min(dayjs(`${yearMonth.value}-01`).endOf('month'), dayjs()).format('YYYY-MM-DD')
              : yearMonth.value
          ),
      },
      {
        name: '第三方班数据',
        loadFunction: () => RepDataLoader.loadThirdpartyClassUsageData(yearMonth.value),
      },
      {
        name: '第三方日数据',
        loadFunction: () => RepDataLoader.loadThirdpartyDayUsageData(yearMonth.value),
      },
      {
        name: '第三方周数据',
        loadFunction: () => RepDataLoader.loadThirdpartyWeekUsageData(yearMonth.value.split('-')[0]),
      },
      {
        name: '第三方MTD数据',
        loadFunction: () => RepDataLoader.loadThirdpartyMtdUsageData(yearMonth.value.split('-')[0]),
      },
    ];

    // 处理特定的编辑数据工作表
    const editDataSheets = [
      {
        name: '手输表日数据录入',
        loadFunction: (sheet: any) => RepDataLoader.loadEditManualDayUsage(sheet, yearMonth.value, origindataObj, deptList, selDept),
      },
      {
        name: '手输表班数据录入',
        loadFunction: (sheet: any) => RepDataLoader.loadEditManualClassUsage(sheet, yearMonth.value, origindataObj, deptList, selDept),
      },
      {
        name: '第三方表日数据修改',
        loadFunction: (sheet: any) => RepDataLoader.loadEditThirdpartyDayUsage(sheet, yearMonth.value, origindataObj, deptList, selDept),
      },
      {
        name: '第三方表班数据修改',
        loadFunction: (sheet: any) => RepDataLoader.loadEditThirdpartyClassUsage(sheet, yearMonth.value, origindataObj, deptList, selDept),
      },
      {
        name: '采集表日数据修改',
        loadFunction: (sheet: any) => RepDataLoader.loadEditCollectDayUsage(sheet, yearMonth.value),
      },
      {
        name: '采集表班数据修改',
        loadFunction: (sheet: any) => RepDataLoader.loadEditCollectClassUsage(sheet, yearMonth.value),
      },
    ];

    const arrList: Promise<any>[] = [];

    // 处理ORS数据表
    orsDataSheets.forEach(({ name, condition, clearTypes, loadFunction }) => {
      if (condition()) {
        const sheet = curSpread.value!.getSheetFromName(name);
        clearSheetData(sheet, clearTypes);
        arrList.push(loadFunction());
      }
    });

    // 处理基础数据表
    baseDataSheets.forEach(({ name, loadFunction }) => {
      if (baseDataNames.value.includes(name)) {
        getOrCreateSheet(curSpread.value, name);
        arrList.push(loadFunction());
      }
    });

    // 处理特定的编辑数据工作表
    editDataSheets.forEach(({ name, loadFunction }) => {
      const sheet = curSpread.value!.getSheetFromName(name);
      if (sheet) {
        arrList.push(loadFunction(sheet));
      }
    });

    loadingState.value = LoadingState.Success;
    sheetDataList.value = await Promise.all(arrList);

    sheetDataList.value.forEach((item) => {
      const sheet = getOrCreateSheet(curSpread.value, item.name);
      setDataToSheet(sheet, item.value, item.row, item.name);
    });

    //加载完数据后获取当前激活的Sheet名称
    curSheetName.value = curSpread.value?.getActiveSheet().name();
  }

  // 设置数据到指定Sheet(拆分出三个子方法)
  const setDataToSheet = (sheet, jsonData, firstRow, apiName) => {
    sheet.suspendPaint();

    adjustSheetSize(sheet, jsonData, firstRow);
    const data = preprocessJsonData(jsonData, apiName, sheet);

    sheet.setArray(firstRow, 0, data);

    sheet.resumePaint();
    sheet.clearPendingChanges();

    loadingSecond.value = 0;
    loadingState.value = LoadingState.Success;

    //切换到之前打开的 SheetName
    curSpread.value!.setActiveSheet(curSheetName.value);
    console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}] Completed ${apiName} Set Array Data`);
  };
  // 1.统一封装 sheet 的结构调整逻辑
  const adjustSheetSize = (sheet, jsonData, firstRow) => {
    const addRowNum = 3;
    const [dataRows, dataCols] = [jsonData.length, jsonData[0].length];
    const [sheetRows, sheetCols] = [sheet.getRowCount(), sheet.getColumnCount()];

    if (dataRows >= sheetRows) {
      sheet.addRows(sheetRows, dataRows - sheetRows + addRowNum);
    } else {
      sheet.deleteRows(dataRows + firstRow, sheetRows - dataRows - firstRow);
      sheet.addRows(dataRows + firstRow, addRowNum);
    }

    if (dataCols + 2 > sheetCols) {
      sheet.addColumns(sheetCols, dataCols + 2 - sheetCols);
    }
  };
  // 2.单元格颜色 & 批注提取为独立函数
  const applyCellStylesAndComments = (sheet, cell, row, col) => {
    if (cell.cellColor) {
      const colorMap = {
        '#FF33FF': { fore: '#FFFFFF', back: '#660066' },
        '#009900': { back: '#008000' },
        '#990033': { back: '#FF0000' },
        '#FFFF00': { back: '#FFFF00' },
      };
      const style = colorMap[cell.cellColor];
      if (style) {
        if (style.fore) sheet.getCell(row, col).foreColor(style.fore);
        sheet.getCell(row, col).backColor(style.back);
      }
    }

    if (cell.errorMsg) {
      const comment = new GC.Spread.Sheets.Comments.Comment();
      comment.text(cell.errorMsg);
      comment.width(500);
      comment.height(cell.errorMsg.length + 70);
      sheet.comments.add(row, col, comment);
    }
  };
  // 3.将 jsonData 转换为最终展示数据
  const preprocessJsonData = (jsonData, apiName, sheet) => {
    const processed = jsonData.map((row, i) =>
      row.map((cell, j) => {
        if (cell === null || cell === '' || cell === 'null') return '';

        if (apiName === 'OrsAllUsage' && typeof cell === 'object' && JSON.stringify(cell) !== '{}') {
          applyCellStylesAndComments(sheet, cell, i, j);
          return cell.cellValue;
        }

        const num = Number(cell);
        return isNaN(num) ? cell : num;
      })
    );
    return processed;
  };
  //导出 Excel
  const exportXlsx = () => {
    // 判断是导出值还是全部导出
    if (onlyExportValue.value) {
      const sheetCount = curSpread.value!.getSheetCount();
      const hiddenNames: string[] = [];
      for (let i = 0; i < sheetCount; i++) {
        const nowSheet = curSpread.value!.getSheet(i);
        if (nowSheet.visible()) {
          const row = nowSheet.getRowCount();
          const col = nowSheet.getColumnCount();
          nowSheet.copyTo(0, 0, 0, 0, row - 1, col - 1, GC.Spread.Sheets.CopyToOptions.value);
        } else {
          hiddenNames.push(nowSheet.name());
        }
      }
      hiddenNames.forEach((item) => {
        const index = curSpread.value!.getSheetIndex(item);
        curSpread.value!.removeSheet(index);
      });
    }
    // 导出前打开行列表头
    const sheetNum = curSpread.value!.getSheetCount();
    for (let i = 0; i < sheetNum; i++) {
      const sheet = curSpread.value!.getSheet(i);
      sheet.options.rowHeaderVisible = true;
      sheet.options.colHeaderVisible = true;
    }
    exportBtnLoading.value = true;
    setTimeout(() => {
      curSpread.value!.export(
        (blob) => {
          FaverSaver.saveAs(blob, reportTitle.value + '.xlsx');
          // 导出后隐藏当前表行列表头
          const activeSheet = curSpread.value!.getActiveSheet();
          activeSheet.options.rowHeaderVisible = false;
          activeSheet.options.colHeaderVisible = false;
          exportBtnLoading.value = false;
        },
        (e) => {
          console.log(e);
        }
      );
    }, 500);
  };
  // 封装处理响应的函数
  const handleUpdateUsageResponse = (response: any, shouldQueryData: boolean) => {
    pageLoading.value = false;
    if (response.success) {
      curSpread.value!.getActiveSheet().clearPendingChanges();
      createMessage.success('保存成功', 2);
      if (shouldQueryData) {
        queryBasedata();
      }
    } else {
      createMessage.error(response.message || 'Error', 4);
    }
  };

  // 保存采集表日数据的修改
  const updateCollectDayUsage = (data: any) => {
    pageLoading.value = true;
    RepFunApi.saveCollectDayUsage(data).then((response) => {
      handleUpdateUsageResponse(response, false);
    });
  };

  // 保存采集表班数据的修改
  const updateCollectClassUsage = (data: any) => {
    pageLoading.value = true;
    RepFunApi.saveCollectClassUsage(data).then((response) => {
      handleUpdateUsageResponse(response, false);
    });
  };

  // 保存手输表日数据的修改
  const updateManualDayUsage = (data: any) => {
    pageLoading.value = true;
    RepFunApi.saveManualDayUsage(data).then((response) => {
      handleUpdateUsageResponse(response, true);
    });
  };

  // 保存手输表班数据的修改
  const updateManualClassUsage = (data: any) => {
    pageLoading.value = true;
    RepFunApi.saveManualClassUsage(data).then((response) => {
      handleUpdateUsageResponse(response, true);
    });
  };

  // 保存第三方表日数据的修改
  const updateThirdpartyDayUsage = (data: any) => {
    pageLoading.value = true;
    RepFunApi.saveThirdpartyDayUsage(data).then((response) => {
      handleUpdateUsageResponse(response, true);
    });
  };

  // 保存第三方表班数据的修改
  const updateThirdpartyClassUsage = (data: any) => {
    pageLoading.value = true;
    RepFunApi.saveThirdpartyClassUsage(data).then((response) => {
      handleUpdateUsageResponse(response, true);
    });
  };
  // 保存所有修改(拆分出 4 个子方法)
  const saveUpdateData = () => {
    const sheet = curSpread.value!.getActiveSheet();
    const rowCount = sheet.getRowCount();
    const colCount = sheet.getColumnCount();
    const sheetName = sheet.name();

    // 新增
    let rows = sheet.getInsertRows();
    console.log('AddRow: ', rows);
    // 删除
    const delRows = sheet.getDeletedRows();
    console.log('DeleteRow: ', delRows);
    // 修改
    rows = sheet.getDirtyRows();
    console.log('UpdateRow: ', rows);

    const cells = sheet.getDirtyCells(0, 0, rowCount, colCount);
    console.log(cells);
    const updateMap: { [key: number]: any[] } = {};

    for (const item of cells) {
      if (!processCellData(sheet, item, sheetName, updateMap)) {
        return;
      }
    }

    const dataList = processUpdateMap(sheet, updateMap);

    if (dataList.length > 0) {
      if (sheetName === '采集表日数据修改') {
        updateCollectDayUsage(dataList);
      } else if (sheetName === '采集表班数据修改') {
        updateCollectClassUsage(dataList);
      } else if (sheetName === '手输表日数据录入') {
        updateManualDayUsage(dataList);
      } else if (sheetName === '手输表班数据录入') {
        updateManualClassUsage(dataList);
      } else if (sheetName === '第三方表日数据修改') {
        updateThirdpartyDayUsage(dataList);
      } else if (sheetName === '第三方表班数据修改') {
        updateThirdpartyClassUsage(dataList);
      }
    }
  };
  //1.处理日用量数据
  const handleDailyUsageData = (tagId: any, newval: string, monthday: string) => {
    const dataObj = { tagId: tagId };
    const dayStr = monthday.split('日')[0];
    dataObj['utime'] = `${yearMonth.value}-${dayStr.padStart(2, '0')}`;
    dataObj['fusage'] = newval === null ? '' : newval;
    //因为这里的oldVal是指上一次修改的值，改很多次只记录上一次，所以没有意义
    //dataObj['oldVal'] = oldval;
    return dataObj;
  };

  //2.处理班用量数据
  const handleClassUsageData = (tagId: any, newval: string, monthday: string) => {
    const dataObj = { tagId: tagId };
    const dayClass = monthday.split('班')[0].split('日');
    const dayStr = dayClass[0];
    const clasStr = dayClass[1] === '白' ? '早' : dayClass[1];
    dataObj['utime'] = `${yearMonth.value}-${dayStr.padStart(2, '0')}`;
    dataObj['shift'] = clasStr;
    dataObj['fusage'] = newval === null ? '' : newval;
    return dataObj;
  };

  //3.处理单元格数据
  const processCellData = (sheet: any, item: any, sheetName: string, updateMap: { [key: number]: any[] }) => {
    //单元格无论有无旧值，清除或双击删除后新值均为null，遇到这种newValue=null直接过滤掉，不予处理
    if (item.newValue === null) {
      sheet.getCell(item.row, item.col).backColor(null);
      return true;
    }

    const monthday = String(sheet.getValue(1, item.col)).replace(/[\n\r]/g, '');
    if (item.col <= 0 || monthday.includes('用量MTD')) return true;

    const tagId = sheet.getValue(item.row, 0);
    //因为这里的oldVal是指上一次修改的值，改很多次只记录上一次，所以没有意义
    //const oldval = String(item.oldValue).trim();
    const newval = String(item.newValue).trim();
    const isInvalid = !newval || isNaN(Number(newval));

    if (isInvalid) {
      sheet.getCell(item.row, item.col).backColor('#fababa');
      createMessage.error('检测到输入空字符串或非数字字符串，请修改后再次保存！', 5);
      return false;
    }

    // 清除之前的错误背景色
    const backColor = sheet.getCell(item.row, item.col).backColor();
    if (backColor === '#fababa') {
      sheet.getCell(item.row, item.col).backColor(null);
    }

    let dataObj: any = null;
    const isDaily =
      (sheetName === '手输表日数据录入' || sheetName === '第三方表日数据修改' || sheetName === '采集表日数据修改') && monthday.includes('日用量');
    const isClass =
      (sheetName === '手输表班数据录入' || sheetName === '第三方表班数据修改' || sheetName === '采集表班数据修改') && monthday.includes('班用量');

    if (isDaily) {
      dataObj = handleDailyUsageData(tagId, newval, monthday);
    } else if (isClass) {
      dataObj = handleClassUsageData(tagId, newval, monthday);
    }

    if (dataObj != null) {
      if (typeof updateMap[item.row] === 'undefined') {
        updateMap[item.row] = [];
      }
      updateMap[item.row].push(dataObj);
    }

    return true;
  };

  //4.处理 updateMap 生成 dataList
  const processUpdateMap = (sheet: any, updateMap: { [key: number]: any[] }) => {
    const dataList: any[] = [];
    for (const row in updateMap) {
      const dataO: any = { delFlag: 0 };
      dataO['fusageList'] = updateMap[row];
      dataO['tagId'] = sheet.getValue(Number(row), 0);
      dataList.push(dataO);
    }
    return dataList;
  };
  //显示图标盖住 Spread 表格
  const showSpreadSheetChart = (sheet: any) => {
    const chartTitle = MySpreadUtils.getSheetVal(sheet, 'B-1');
    const startRow = MySpreadUtils.getSheetVal(sheet, 'F-1') - 1;
    const endRow = MySpreadUtils.getSheetVal(sheet, 'H-1') - 1;

    const localXDatas = CombinedChartApi.getXDatas(sheet);
    const { series, legend, yAxis } = CombinedChartApi.getSeriesData(sheet, startRow, endRow, localXDatas);

    showSpreadChart.value = true;
    nextTick(() => {
      xTitle.value = chartTitle;
      xDatas.value = [...localXDatas];
      yaxisDatas.value = [...yAxis];
      serieDatas.value = [...series];
      legendDatas.value = [...legend];
    });
    // 显示右上角总费三值
    if (sheet.name() === '图表-电') {
      totalCharge.value.practicalUsage = Number(MySpreadUtils.formatNumber(MySpreadUtils.getSheetVal(sheet, 'B-15'), 2));
      totalCharge.value.practicalHtml = [...String(totalCharge.value.practicalUsage)];

      getBudgetUsageByMonth('90001');
      if (dayjs().month() === dayjs(yearMonth.value + '-01').month()) {
        const date = dayjs().date();
        const dayNum = dayjs().daysInMonth();
        totalCharge.value.forecastUsage = Number(MySpreadUtils.formatNumber((totalCharge.value.practicalUsage / date) * dayNum, 2));
        totalCharge.value.forecastHtml = [...String(totalCharge.value.forecastUsage)];
      } else {
        totalCharge.value.forecastHtml = [...String(totalCharge.value.practicalUsage)];
      }

      getTagLimitByPI('90001');
      totalCharge.value.show = true;
    } else {
      totalCharge.value.show = false;
    }
  };

  // 日期切换处理函数
  const selDayChangeHandler = () => {
    if (!curSpread.value) return; // 防御性编程
    const sheet = curSpread.value!.getSheetFromName('ORS');
    if (sheet) {
      // 计算天数差（使用 moment 处理日期）
      const currentDate = dayjs().startOf('day');
      const selectedDate = dayjs(selDay.value).startOf('day');
      const dayNums = currentDate.diff(selectedDate, 'days');

      // 设置单元格公式
      sheet.getCell(0, 0).formula(`=TODAY()-${dayNums}`);
      curSpread.value!.getActiveSheet().resumePaint();
    }
  };

  // 获取当月预算值
  const getBudgetUsageByMonth = async (tagId: string) => {
    if (!yearMonth.value) return; // 校验必填参数

    const paramObj = {
      manualOtherUsage: {
        date: `${yearMonth.value}-01`, // 拼接月初日期
        tagId: tagId,
      },
    };

    try {
      const response = await RepFunApi.listManualOtherUsage(paramObj);
      const dataList = response.data || [];

      // 处理预算值
      const budgetValue = dataList.length ? MySpreadUtils.formatNumber(dataList[0].value, 2) : '0.00';
      totalCharge.value.budgetUsage = Number(budgetValue);

      // 生成带格式的 HTML
      totalCharge.value.budgetHtml = budgetValue.split(''); // 拆分成字符数组
    } catch (error) {
      console.error('获取预算值失败:', error);
    }
  };

  // 获取点位上下限值
  const getTagLimitByPI = async (tagId: string) => {
    if (!yearMonth.value) return; // 校验必填参数

    const paramObj = {
      tagLimitValue: {
        effectiveMonth: yearMonth.value,
        piId: tagId,
      },
    };

    try {
      const response = await RepFunApi.getTagLimitByPI(paramObj);
      const tagLimitValue = response.data || {};

      // 颜色判断逻辑
      const { forecastUsage } = totalCharge.value;
      const { redUpperLimit, redLowerLimit, yellowUpperLimit, yellowLowerLimit } = tagLimitValue;

      if (forecastUsage > redUpperLimit || forecastUsage < redLowerLimit) {
        totalCharge.value.fontColor = 'red';
      } else if (
        (forecastUsage <= redUpperLimit && forecastUsage > yellowUpperLimit) ||
        (forecastUsage >= redLowerLimit && forecastUsage < yellowLowerLimit)
      ) {
        totalCharge.value.fontColor = '#e6a700';
      } else {
        totalCharge.value.fontColor = '#15ca65';
      }
    } catch (error) {
      console.error('获取限值失败:', error);
    }
  };

  // 导出处理函数
  const handlerExportValue = () => {
    const onlyExportValue = ref(false); // 假设是组件内响应式变量
    if (!onlyExportValue.value) {
      const showReportById = () => {
        console.log('执行报表展示逻辑');
      };
      showReportById();
    }
  };

  // 检查加载状态发生错误自动再次加载
  const checkLoadingState = () => {
    if (loadingState.value === LoadingState.Loading) {
      loadingSecond.value += 1;
      if (loadingSecond.value === 10) {
        queryBasedata(); // 自动重试
        loadingSecond.value = 0; // 重置计时
      }
    } else if (loadingState.value === LoadingState.Success) {
      // 假设 spread 是响应式对象
      const spread = ref<any>(null);
      spread.value?.refresh(); // 安全调用
      clearLoadingTimer(); // 清除定时器
    }
  };

  // 清除定时器的辅助函数
  const clearLoadingTimer = () => {
    if (loadingTimer) {
      clearInterval(loadingTimer);
      loadingTimer = null;
    }
  };

  // 保存手动设置重算区域
  const saveSetBackfillPointStatus = (data: any) => {
    pageLoading.value = true;
    RepFunApi.updateBackfillPointStatus(data).then((response) => {
      pageLoading.value = false;
      if (response.success) {
        curSpread.value!.getActiveSheet().clearSelection();
        createMessage.success('设置成功', 2);
      } else {
        createMessage.error(response.message || 'Error', 4);
      }
    });
  };

  //得到所选区域有效值
  const getHandSelectedValue = () => {
    const sheet = curSpread.value!.getActiveSheet();
    const selections = sheet.getSelections();
    console.log('selections:', selections, selections.length);

    const result = selections.map(
      (selection: {
        row: number; // 选区起始行（0-based）
        col: number; // 选区起始列（0-based）
        rowCount: number;
        colCount: number;
      }) => {
        const { row, col, rowCount, colCount } = selection;

        // 1. 获取第一列（A列）的所有值
        const firstColumnValues: string[] = [];
        const firstRowInx: number[] = [];
        for (let r = row; r < row + rowCount; r++) {
          const cellValue = sheet.getValue(r, 0); // A列是第0列
          const cellValueStr = cellValue?.toString() || '';
          // 过滤掉年份和tag_id这两个值
          if (!cellValueStr.includes('年') && !cellValueStr.includes('tag_id')) {
            firstColumnValues.push(cellValue);
            firstRowInx.push(r);
          }
        }

        // 2. 获取第一行（行1）和第二行（行2）的所有值
        const firstRowValues: string[] = [];
        const secondRowValues: string[] = [];
        const firstColumnInx: number[] = [];
        for (let c = col; c < col + colCount; c++) {
          const firstVal = sheet.getValue(0, c)?.toString() || '';
          // 过滤掉非日期格式的值，只保留类似 05/06 的日期格式
          if (/^\d{2}\/\d{2}$/.test(firstVal)) {
            firstRowValues.push(firstVal); // 行1是第0行
            secondRowValues.push(sheet.getValue(1, c)?.toString() || ''); // 行2是第1行
            firstColumnInx.push(c);
          }
        }

        // 3. 根据上面的firstColumnInx和firstRowInx获对应有效的单元格值
        const validValues: string[] = [];
        for (const r of firstRowInx) {
          for (let c of firstColumnInx) {
            validValues.push(sheet.getValue(r, c)?.toString() || '');
          }
        }

        return {
          tagIds: firstColumnValues.join(','), // A列值，逗号分隔
          dayDates: firstRowValues.join(','), // 行1值，逗号分隔
          classFlags: secondRowValues.join(','), // 行2值，逗号分隔
          validValues: validValues.join(','), // 行2值，逗号分隔
        };
      }
    );
    //过滤掉空值
    const filterResult = result.filter((item) => item.tagIds && item.dayDates && item.classFlags);
    console.log('重算所选区域结果:', filterResult);
    return filterResult;
  };

  //重算所选区域
  const setBackfillTagDateRange = () => {
    const filterResult = getHandSelectedValue();
    if (filterResult.length === 0) {
      createMessage.warning('请先在表格中选取需要重算的区域，按住Ctrl可以选择不连续区域！', 5);
    } else {
      const msg = filterResult
        .map(
          (item, index) =>
            `${index + 1}、时间区间:[${item.dayDates.split(',')[0]}~${item.dayDates.split(',')[item.dayDates.split(',').length - 1]}]<br>点位:[${item.tagIds}] `
        )
        .join('<br>');
      createConfirm({
        iconType: 'warning',
        title: '确认要重算以下数据吗？',
        content: msg,
        okText: '确认',
        cancelText: '取消',
        onOk: () => {
          const data = {
            tagKind: getCurrentTagKind(),
            grainType: getCurrentGrainType(),
            yearMonth: yearMonth.value,
            tagDateRangeList: filterResult,
          };
          console.log('重算所选区域数据:', data);
          saveSetBackfillPointStatus(data);
        },
      });
    }
  };
</script>

<style scoped>
  /* 保持和之前相似的样式 */
  .app-container {
    padding: 20px;
  }

  .spread-container {
    position: relative;
    height: calc(100vh - 200px);
    margin-top: 16px;
  }
  .spreadHost {
    width: 95%;
    height: 100%;
  }
  .total-charge {
    position: absolute;
    top: 20px;
    right: 20px;
    background: rgba(255, 255, 255, 0.9);
    padding: 16px;
    border-radius: 4px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  }

  /* Ant Design 组件样式覆盖 */
  :deep(.ant-picker) {
    width: 128px;
  }

  :deep(.ant-select) {
    width: 150px;
  }

  /* 关闭状态背景色 */
  :deep(.ant-switch) {
    background-color: #d92622;
  }
  /* 开启状态背景色 */
  :deep(.ant-switch-checked) {
    background-color: #d92622;
  }
</style>
