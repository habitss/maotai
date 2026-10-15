<template>
  <a-spin :spinning="pageLoading">
    <div class="p-2">
      <div class="jeecg-basic-table-form-container">
        <a-form :label-col="labelCol" :wrapper-col="wrapperCol" style="background-color: #fff; padding: 10px 10px">
          <div style="display: flex; align-items: center; padding: 5px 0px 12px 0px">
            <div style="width: 100px">数据时间筛选 :</div>
            <div v-if="isDayPicker">
              <a-date-picker v-model:value="pickerValue" format="YYYY-MM-DD" @change="handlePickerDayChange" />
            </div>
            <div v-if="isMonthPicker">
              <a-date-picker v-model:value="pickerValue" picker="month" format="YYYY-MM" @change="handlePickerMonthChange" />
            </div>
            <div v-if="isYearPicker">
              <a-date-picker v-model:value="pickerValue" picker="year" format="YYYY" @change="handlePickerYearChange" />
            </div>
            <div style="width: 300px" v-if="isTagOptionPicker">
              <j-select-multiple placeholder="请选择点位" v-model:value="pickerTagIds" :dictCode="tagIdDictCode" :idAndLabel="true" allow-clear />
            </div>
            <div style="margin-left: 10px" v-if="isShowRepSelect">
              <a-select v-model:value="selRepShowFlag" style="width: 150px" :options="repShowFlagOptions" placeHolder="请选择" />
              <a-select v-model:value="selRepShowType" style="width: 130px; margin-left: 5px" :options="repShowTypeOptions" placeHolder="请选择" />
            </div>
            <div style="margin-left: 10px"><a-button type="primary" @click="queryDataByMonth"> 查询 </a-button></div>
          </div>
          <div style="display: flex; align-items: center">
            <a-checkbox-group v-model:value="selBaseDatas">
              <a-checkbox v-for="item in baseDataList" :key="item.id" :disabled="checkDisabled(item.id)" :value="item.id">{{
                item.label
              }}</a-checkbox>
            </a-checkbox-group>
          </div>
        </a-form>
        <div class="spreadContainer">
          <gc-spread-sheets hostClass="spreadHost" @workbook-initialized="workbookInitialized">
            <GcWorksheet />
          </gc-spread-sheets>
        </div>
        <a-row>
          <a-col :span="24" class="footer-btn" style="text-align: center">
            <a-button type="primary" @click="exportXlsx"> 导出数据 </a-button>
          </a-col>
        </a-row>
      </div>
    </div>
  </a-spin>
</template>

<script lang="ts" name="ems-BaseReportExport" setup>
  import { reactive, ref, watch, onMounted, computed } from 'vue';
  import JSZip from 'jszip';
  import '@grapecity/spread-sheets/styles/gc.spread.sheets.excel2016colorful.css';
  import * as GC from '@grapecity/spread-sheets';
  import { GcSpreadSheets, GcWorksheet } from '@grapecity/spread-sheets-vue';
  import '@grapecity/spread-sheets-resources-zh';
  import '@grapecity/spread-sheets-io';
  import FaverSaver from 'file-saver';
  import dayjs from 'dayjs';
  import { repShowFlagOptions, repShowTypeOptions, baseDataList, headProps } from './ReportFunction.data';
  import * as RepFunApi from './ReportFunction.api';
  import { GrainTypeEnum, TagKindEnum } from '@/enums/EmsEnum';
  import JSelectMultiple from '/@/components/Form/src/jeecg/components/JSelectMultiple.vue';
  import { useMessage } from '@/hooks/web/useMessage';
  const { createMessage } = useMessage();
  // 初始化 Spread
  GC.Spread.Common.CultureManager.culture('zh-cn');
  let SpreadJSKey =
    'emsapac.ab-inbev.cn,623376735916589#B1v4r7N0dHWyBDcwkXVMJ7Qr9UWtpWUYlzbzgjUORkUY3GOzEVNHZFNp5kZGdXc4xEbvAVUOh6VGJWWWdTORJjTMRWdyoXS4B5MJNHdwAlVmt6MRNTb4gTTxMzVrUTNhZldQ94Yx2yVrlzT4JFV6U7KQdlMB3SZmVFbzUFTxJVVN9kUNdFOHJXcDN4YxpmTPl5VsBXRz2UQvsGcyAHTqlEa8YmRk5UMhd4dCtkTDpmVCl6aQdETKJmUNNUZyAnSrd6Y68kc8VnV62GN4Elc4QjWFR6Sv2iWy24Yt5UYFR7RhZXaXNjU6hjUY3WcJhmetNUR5ZGTZNkS85EcU3GbwdlI0IyUiwiI8QUNyADNGZjI0ICSiwSO7AzNyQTO8UTM0IicfJye&Qf35VficFVDplI0IyQiwiI6EjL6ByUKBCZhVmcwNlI0IiTis7W0ICZyBlIsISOzMDM8ADI9ETNwMjMwIjI0ICdyNkIsIibj9idlJmbp5iYh9yYhBXYz5WZiojIz5GRiwiI8+Y9sWY9QmZ0Jyp9Li09leb9WyY9oqY9qeI0R+K0+0L0JGr9m6q9iojIh94QiwiI9gTN6ETO5MzN6czMzIjNiojIklkIs4XXbpjInxmZiwSZzxWYmpjIyNHZisnOiwmbBJye0ICRiwiI34TQmBjSNNlZJtUOGxEVxZje75kY8MjWMdER6QmMrg7RmZTbLRUQRp7cM9GZ5dWRv2kTsdUN6onewpnepB5ZhljWxN4dnRUaSdTOGNWWUxkbFhlZrpVb7q3'; // Enter a valid license key.
  GC.Spread.Sheets.LicenseKey = SpreadJSKey;

  // 初始化数据
  const pageLoading = ref(false);
  const grainTypeFlag = ref(GrainTypeEnum.DAY);
  const tagTypeFlag = ref(TagKindEnum.COLLECT);
  const curSpread = ref<GC.Spread.Sheets.Workbook | null>(null);
  const baseSheetNames = ref<string[]>([]);
  const selBaseDatas = ref<string[]>([]);
  const selRepShowFlag = ref<string | undefined>('1'); // 报表表头内容标识
  const selRepShowType = ref<string | undefined>('1'); // 报表表头显示方式
  const pickerValue = ref<dayjs.Dayjs | null>(null); // DatePicker 用 dayjs 对象
  const yearMonth = ref<string>();
  const pickerTagIds = ref<string>('');
  const labelCol = reactive({
    xs: 12,
    sm: 8,
    md: 6,
    lg: 8,
    xl: 6,
    xxl: 6,
  });
  const wrapperCol = reactive({});

  //初始值
  pickerValue.value = dayjs(); // 设置为当前月
  yearMonth.value = pickerValue.value.format('YYYY-MM');

  // 监听 selBaseDatas 变化
  watch(
    () => selBaseDatas.value.slice(), // 用 slice() 保证是快照副本，避免引用一致
    (val, old) => {
      const nSet = new Set(val);
      const oSet = new Set(old);
      const isAdd = val.length > old.length;
      let diffId = '';
      if (isAdd) {
        diffId = [...nSet].find((n) => !oSet.has(n))!;
      } else {
        diffId = [...oSet].find((n) => !nSet.has(n))!;
      }
      const diffItem = baseDataList.find((item) => item.id === diffId);
      if (diffItem) {
        const sheetName = diffItem.label;
        changeDatePickerBySheetName(sheetName);
        loadBasedataToSpread(diffItem.label, isAdd);
      }
    }
  );

  // 组件挂载时初始化 baseSheetNames
  onMounted(() => {
    baseDataList.forEach((element) => {
      baseSheetNames.value.push(element.label);
    });
  });

  //点位下拉分小时和半小时
  const tagIdDictCode = computed(() => {
    if (tagTypeFlag.value === TagKindEnum.COLLECT) {
      const period = grainTypeFlag.value === GrainTypeEnum.HALFHOUR ? 0 : 1;
      return `mes3_pi_base_point,point_name,tag_id,energy_type!='' AND collect_period=${period}`;
    } else if (tagTypeFlag.value === TagKindEnum.ORSDATA) {
      return `ems_orsdata_tag,tag_name,tag_id`;
    } else if (tagTypeFlag.value === TagKindEnum.THIRDPARTY) {
      return 'ems_thirdparty_tag,tag_name,tag_id';
    } else {
      return '';
    }
  });
  //是否选择点位组件
  const isTagOptionPicker = computed(() => grainTypeFlag.value === GrainTypeEnum.HALFHOUR || grainTypeFlag.value === GrainTypeEnum.HOUR);
  //是否日组件
  const isDayPicker = computed(() => (grainTypeFlag.value === GrainTypeEnum.HALFHOUR || grainTypeFlag.value === GrainTypeEnum.HOUR) && 1 > 2);
  //是否月组件
  const isMonthPicker = computed(
    () =>
      grainTypeFlag.value === GrainTypeEnum.HALFHOUR ||
      grainTypeFlag.value === GrainTypeEnum.HOUR ||
      grainTypeFlag.value === GrainTypeEnum.SHIFT ||
      grainTypeFlag.value === GrainTypeEnum.DAY
  );
  //是否年组件
  const isYearPicker = computed(() => grainTypeFlag.value === GrainTypeEnum.WEEK || grainTypeFlag.value === GrainTypeEnum.MONTH);
  //是否显示报表表头选择
  const isShowRepSelect = computed(
    () => (grainTypeFlag.value === GrainTypeEnum.HALFHOUR || grainTypeFlag.value === GrainTypeEnum.HOUR) && tagTypeFlag.value !== TagKindEnum.ORSDATA
  );

  // WorkbookInitialized 回调
  const workbookInitialized = (spread: GC.Spread.Sheets.Workbook) => {
    curSpread.value = spread;
    spread.refresh();
    //绑定 Sheet 页切换事件
    spread.bind(GC.Spread.Sheets.Events.ActiveSheetChanged, () => {
      const sheet = curSpread.value!.getActiveSheet();
      const sheetName = sheet.name();
      changeDatePickerBySheetName(sheetName);
      sheet.resumePaint();
      sheet.repaint();
    });
    console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Init Spread Completed: ${spread}`); // 使用 dayjs 格式化日期
  };

  // 根据sheetName判断日期组件的显示与格式
  const changeDatePickerBySheetName = (sheetName: string) => {
    //数据类型
    if (sheetName.includes('采集')) {
      tagTypeFlag.value = TagKindEnum.COLLECT;
    } else if (sheetName.includes('手输')) {
      tagTypeFlag.value = TagKindEnum.MANUAL;
    } else if (sheetName.includes('第三方')) {
      tagTypeFlag.value = TagKindEnum.THIRDPARTY;
    } else if (sheetName.includes('中间')) {
      tagTypeFlag.value = TagKindEnum.ORSDATA;
    }
    //时间粒度
    if (sheetName.includes('半小时')) {
      grainTypeFlag.value = GrainTypeEnum.HALFHOUR;
    } else if (sheetName.includes('小时')) {
      grainTypeFlag.value = GrainTypeEnum.HOUR;
    } else if (sheetName.includes('班')) {
      grainTypeFlag.value = GrainTypeEnum.SHIFT;
    } else if (sheetName.includes('日')) {
      grainTypeFlag.value = GrainTypeEnum.DAY;
    } else if (sheetName.includes('周')) {
      grainTypeFlag.value = GrainTypeEnum.WEEK;
    } else if (sheetName.includes('MTD')) {
      grainTypeFlag.value = GrainTypeEnum.MONTH;
    }

    if (isDayPicker.value) {
      yearMonth.value = pickerValue.value!.format('YYYY-MM-DD');
    } else if (isMonthPicker.value) {
      yearMonth.value = pickerValue.value!.format('YYYY-MM');
    } else if (isYearPicker.value) {
      yearMonth.value = pickerValue.value!.format('YYYY');
    }
  };

  const handlePickerDayChange = (date: dayjs.Dayjs | null) => {
    yearMonth.value = date ? date.format('YYYY-MM-DD') : '';
  };

  const handlePickerMonthChange = (date: dayjs.Dayjs | null) => {
    yearMonth.value = date ? date.format('YYYY-MM') : '';
  };

  const handlePickerYearChange = (date: dayjs.Dayjs | null) => {
    yearMonth.value = date ? date.format('YYYY') : '';
  };

  // 查询指定月份当前激活 Sheet 的数据
  const dataLoaders: Record<string, (sheet: GC.Spread.Sheets.Worksheet, ym: string) => void> = {
    采集表半小时数据: (s, ym) => loadCollectHalfHourUsageData(s, ym),
    采集表小时数据: (s, ym) => loadCollectHourUsageData(s, ym),
    采集表班数据: (s, ym) => loadCollectClassUsageData(s, ym),
    采集表日数据: (s, ym) => loadCollectDayUsageData(s, ym),
    采集表周数据: (s, ym) => loadCollectWeekUsageData(s, ym.split('-')[0]),
    采集表MTD数据: (s, ym) => loadCollectMtdUsageData(s, ym.split('-')[0]),
    手输表班数据: (s, ym) => loadManualClassUsageData(s, ym),
    手输表日数据: (s, ym) => loadManualDayUsageData(s, ym),
    手输表周数据: (s, ym) => loadManualWeekUsageData(s, ym.split('-')[0]),
    手输表MTD数据: (s, ym) => loadManualMtdUsageData(s, ym.split('-')[0]),
    第三方小时数据: (s, ym) => loadThirdpartyHourUsageData(s, ym),
    第三方班数据: (s, ym) => loadThirdpartyClassUsageData(s, ym),
    第三方日数据: (s, ym) => loadThirdpartyDayUsageData(s, ym),
    第三方周数据: (s, ym) => loadThirdpartyWeekUsageData(s, ym.split('-')[0]),
    第三方MTD数据: (s, ym) => loadThirdpartyMtdUsageData(s, ym.split('-')[0]),
    中间表小时数据: (s, ym) => loadOrsHourUsageData(s, ym),
    中间表班数据: (s, ym) => loadOrsClassUsageData(s, ym),
    中间表日数据: (s, ym) => loadOrsDayUsageData(s, ym),
    中间表周数据: (s, ym) => loadOrsWeekUsageData(s, ym.split('-')[0]),
    中间表MTD数据: (s, ym) => loadOrsMtdUsageData(s, ym.split('-')[0]),
  };
  const queryDataByMonth = () => {
    const sheet = curSpread.value!.getActiveSheet();
    sheet.clear(0, 0, sheet.getRowCount(), sheet.getColumnCount(), GC.Spread.Sheets.SheetArea.viewport, GC.Spread.Sheets.StorageType.data);

    const sheetName = sheet.name();
    const loader = dataLoaders[sheetName];
    if (loader) {
      loader(sheet, yearMonth.value!);
    } else {
      console.warn(`未匹配的数据加载器：${sheetName}`);
    }
  };

  // 检查是否禁用
  const checkDisabled = (id: string) => {
    return (
      (selBaseDatas.value.includes('StepEnergy') && id !== 'StepEnergy') ||
      (selBaseDatas.value.length > 0 && !selBaseDatas.value.includes('StepEnergy') && id === 'StepEnergy')
    );
  };

  // 导出 Excel
  const exportXlsx = () => {
    curSpread.value!.export(
      (blob) => {
        FaverSaver.saveAs(blob, '基础数据表.xlsx');
      },
      () => {}
    );
  };

  const loadBasedataToSpread = async (diffName: string, isAdd: boolean) => {
    const year = yearMonth.value!.split('-')[0];
    const sheetNameMap: Record<string, (sheet: GC.Spread.Sheets.Worksheet) => Promise<void>> = {
      采集表半小时数据: (s) => loadCollectHalfHourUsageData(s, (pickerValue.value ?? dayjs()).format('YYYY-MM-DD')),
      采集表小时数据: (s) => loadCollectHourUsageData(s, (pickerValue.value ?? dayjs()).format('YYYY-MM-DD')),
      采集表班数据: (s) => loadCollectClassUsageData(s, yearMonth.value),
      采集表日数据: (s) => loadCollectDayUsageData(s, yearMonth.value),
      采集表周数据: (s) => loadCollectWeekUsageData(s, year),
      采集表MTD数据: (s) => loadCollectMtdUsageData(s, year),
      手输表班数据: (s) => loadManualClassUsageData(s, yearMonth.value),
      手输表日数据: (s) => loadManualDayUsageData(s, yearMonth.value),
      手输表周数据: (s) => loadManualWeekUsageData(s, year),
      手输表MTD数据: (s) => loadManualMtdUsageData(s, year),
      第三方小时数据: (s) => loadThirdpartyHourUsageData(s, (pickerValue.value ?? dayjs()).format('YYYY-MM-DD')),
      第三方班数据: (s) => loadThirdpartyClassUsageData(s, yearMonth.value),
      第三方日数据: (s) => loadThirdpartyDayUsageData(s, yearMonth.value),
      第三方周数据: (s) => loadThirdpartyWeekUsageData(s, year),
      第三方MTD数据: (s) => loadThirdpartyMtdUsageData(s, year),
      中间表小时数据: (s) => loadOrsHourUsageData(s, (pickerValue.value ?? dayjs()).format('YYYY-MM-DD')),
      中间表班数据: (s) => loadOrsClassUsageData(s, yearMonth.value),
      中间表日数据: (s) => loadOrsDayUsageData(s, yearMonth.value),
      中间表周数据: (s) => loadOrsWeekUsageData(s, year),
      中间表MTD数据: (s) => loadOrsMtdUsageData(s, year),
    };

    if (!isAdd) {
      const sheetInx = curSpread.value!.getSheetIndex(diffName);
      if (sheetInx >= 0) {
        curSpread.value!.removeSheet(sheetInx);
        if (curSpread.value!.getSheetCount() === 0) {
          curSpread.value!.addSheet(0, new GC.Spread.Sheets.Worksheet('Sheet1'));
        }
      }
    } else {
      let sheet = curSpread.value!.getSheetFromName('Sheet1');
      if (sheet) {
        sheet.name(diffName);
      } else {
        sheet = new GC.Spread.Sheets.Worksheet(diffName);
        curSpread.value!.addSheet(0, sheet);
      }

      const loader = sheetNameMap[diffName];
      if (loader) {
        await loader(sheet);
      } else {
        console.warn('未知的 diffName:', diffName);
      }
    }
  };

  // 加载采集表半小时数据
  const loadCollectHalfHourUsageData = async (sheet: GC.Spread.Sheets.Worksheet, endDate: string | undefined) => {
    if (!pickerTagIds.value) {
      createMessage.warning('请选择最多5个采集点位', 5);
      return;
    } else if (pickerTagIds.value.split(',').length > 5) {
      createMessage.warning('最多只能选择5个采集点位', 5);
      return;
    }
    pageLoading.value = true;
    console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Start Download CollectHalfHourUsage`); // 使用 dayjs 格式化日期
    try {
      const response = await RepFunApi.getCollectHalfHourUsageList(
        endDate?.split('-').length === 2 ? endDate + '-01' : endDate,
        pickerTagIds?.value?.split(',') ?? ['NULL'],
        selRepShowFlag.value ?? undefined,
        selRepShowType.value ?? undefined
      );
      const zip = await JSZip.loadAsync(response.data);
      const text = await zip.file('apidata')!.async('text');
      console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]UnZip Completed Start Load apidata`); // 使用 dayjs 格式化日期
      const jsonData = JSON.parse(text);
      await setDataToSheet(sheet, jsonData, 0, 'CollectHalfHourUsage');
    } catch (error) {
      console.error('Error loading CollectHalfHourUsage:', error);
    } finally {
      pageLoading.value = false;
    }
  };

  // 加载采集表小时数据
  const loadCollectHourUsageData = async (sheet: GC.Spread.Sheets.Worksheet, endDate: string | undefined) => {
    if (!pickerTagIds.value) {
      createMessage.warning('请选择最多5个采集点位', 5);
      return;
    } else if (pickerTagIds.value.split(',').length > 5) {
      createMessage.warning('最多只能选择5个采集点位', 5);
      return;
    }

    pageLoading.value = true;
    console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Start Download CollectHourUsage`); // 使用 dayjs 格式化日期
    try {
      const response = await RepFunApi.getCollectHourUsageList(
        endDate?.split('-').length === 2 ? endDate + '-01' : endDate,
        pickerTagIds?.value?.split(',') ?? ['NULL'],
        selRepShowFlag.value ?? undefined,
        selRepShowType.value ?? undefined
      );
      const zip = await JSZip.loadAsync(response.data);
      const text = await zip.file('apidata')!.async('text');
      console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]UnZip Completed Start Load apidata`); // 使用 dayjs 格式化日期
      const jsonData = JSON.parse(text);
      await setDataToSheet(sheet, jsonData, 0, 'CollectHourUsage');
    } catch (error) {
      console.error('Error loading CollectHourUsage:', error);
    } finally {
      pageLoading.value = false;
    }
  };

  // 加载采集表日数据
  const loadCollectDayUsageData = async (sheet: GC.Spread.Sheets.Worksheet, month: string | undefined) => {
    pageLoading.value = true;
    console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Start Download CollectDayUsage`); // 使用 dayjs 格式化日期
    try {
      const response = await RepFunApi.getCollectDayUsageList(month);
      const zip = await JSZip.loadAsync(response.data);
      const text = await zip.file('apidata')!.async('text');
      console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]UnZip Completed Start Load apidata`); // 使用 dayjs 格式化日期
      const jsonData = JSON.parse(text);
      await setDataToSheet(sheet, jsonData, 0, 'CollectDayUsage');
    } catch (error) {
      console.error('Error loading CollectDayUsage:', error);
    } finally {
      pageLoading.value = false;
    }
  };

  // 加载采集表班数据
  const loadCollectClassUsageData = async (sheet: GC.Spread.Sheets.Worksheet, month: string | undefined) => {
    pageLoading.value = true;
    console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Start Download CollectClassUsage`); // 使用 dayjs 格式化日期
    try {
      const response = await RepFunApi.getCollectClassUsageList(month);
      const zip = await JSZip.loadAsync(response.data);
      const text = await zip.file('apidata')!.async('text');
      console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]UnZip Completed Start Load apidata`); // 使用 dayjs 格式化日期
      const jsonData = JSON.parse(text);
      await setDataToSheet(sheet, jsonData, 0, 'CollectClassUsage');
    } catch (error) {
      console.error('Error loading CollectClassUsage:', error);
    } finally {
      pageLoading.value = false;
    }
  };

  // 加载采集表WEEK数据
  const loadCollectWeekUsageData = async (sheet: GC.Spread.Sheets.Worksheet, year: string | undefined) => {
    pageLoading.value = true;
    console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}] Start Download CollectWeekUsage`);
    try {
      const response = await RepFunApi.getCollectWeekUsageList(year);
      if (response.success) {
        setDataToSheet(sheet, response.result, 0, 'CollectWeekUsage');
      } else {
        createMessage.error(response.message, 5);
      }
    } catch (error) {
      console.error('Error loading CollectWeekUsage:', error);
    } finally {
      pageLoading.value = false;
    }
  };

  // 加载采集表MTD数据
  const loadCollectMtdUsageData = async (sheet: GC.Spread.Sheets.Worksheet, year: string | undefined) => {
    pageLoading.value = true;
    console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}] Start Download CollectMtdUsage`);
    try {
      const response = await RepFunApi.getCollectMtdUsageList(year);
      if (response.success) {
        setDataToSheet(sheet, response.result, 0, 'CollectMtdUsage');
      } else {
        createMessage.error(response.message, 5);
      }
    } catch (error) {
      console.error('Error loading CollectMtdUsage:', error);
    } finally {
      pageLoading.value = false;
    }
  };

  // 加载手输表日数据
  const loadManualDayUsageData = async (sheet: GC.Spread.Sheets.Worksheet, month: string | undefined) => {
    pageLoading.value = true;
    console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Start Download ManualDayUsage`); // 使用 dayjs 格式化日期
    try {
      const response = await RepFunApi.getManualDayUsageDataList(month);
      const zip = await JSZip.loadAsync(response.data);
      const text = await zip.file('apidata')!.async('text');
      console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]UnZip Completed Start Load apidata`); // 使用 dayjs 格式化日期
      const jsonData = JSON.parse(text);
      await setDataToSheet(sheet, jsonData, 0, 'ManualDayUsage');
    } catch (error) {
      console.error('Error loading ManualDayUsage:', error);
    } finally {
      pageLoading.value = false;
    }
  };

  // 加载手输表班数据
  const loadManualClassUsageData = async (sheet: GC.Spread.Sheets.Worksheet, month: string | undefined) => {
    pageLoading.value = true;
    console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Start Download ManualClassUsage`); // 使用 dayjs 格式化日期
    try {
      const response = await RepFunApi.getManualClassUsageDataList(month);
      const zip = await JSZip.loadAsync(response.data);
      const text = await zip.file('apidata')!.async('text');
      console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]UnZip Completed Start Load apidata`); // 使用 dayjs 格式化日期
      const jsonData = JSON.parse(text);
      await setDataToSheet(sheet, jsonData, 0, 'ManualClassUsage');
    } catch (error) {
      console.error('Error loading ManualClassUsage:', error);
    } finally {
      pageLoading.value = false;
    }
  };

  // 加载手输表WEEK数据
  const loadManualWeekUsageData = async (sheet: GC.Spread.Sheets.Worksheet, year: string | undefined) => {
    pageLoading.value = true;
    console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}] Start Download ManualWeekUsage`);
    try {
      const response = await RepFunApi.getManualWeekUsageDataList(year);
      if (response.success) {
        setDataToSheet(sheet, response.result, 0, 'ManualWeekUsage');
      } else {
        createMessage.error(response.message, 5);
      }
    } catch (error) {
      console.error('Error loading ManualWeekUsage:', error);
    } finally {
      pageLoading.value = false;
    }
  };

  // 加载手输表MTD数据
  const loadManualMtdUsageData = async (sheet: GC.Spread.Sheets.Worksheet, year: string | undefined) => {
    pageLoading.value = true;
    console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}] Start Download ManualMtdUsage`);
    try {
      const response = await RepFunApi.getManualMtdUsageDataList(year);
      if (response.success) {
        setDataToSheet(sheet, response.result, 0, 'ManualMtdUsage');
      } else {
        createMessage.error(response.message, 5);
      }
    } catch (error) {
      console.error('Error loading ManualMtdUsage:', error);
    } finally {
      pageLoading.value = false;
    }
  };

  // 加载第三方表小时数据
  const loadThirdpartyHourUsageData = async (sheet: GC.Spread.Sheets.Worksheet, endDate: string | undefined) => {
    if (!pickerTagIds.value) {
      createMessage.warning('请选择最多5个点位', 5);
      return;
    } else if (pickerTagIds.value.split(',').length > 5) {
      createMessage.warning('最多只能选择5个点位', 5);
      return;
    }
    pageLoading.value = true;
    console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Start Download ThirdpartyHourUsage`); // 使用 dayjs 格式化日期
    try {
      const response = await RepFunApi.getThirdpartyHourUsageList(
        endDate?.split('-').length === 2 ? endDate + '-01' : endDate,
        pickerTagIds?.value?.split(',') ?? ['NULL'],
        selRepShowFlag.value ?? undefined,
        selRepShowType.value ?? undefined
      );
      const zip = await JSZip.loadAsync(response.data);
      const text = await zip.file('apidata')!.async('text');
      console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]UnZip Completed Start Load apidata`); // 使用 dayjs 格式化日期
      const jsonData = JSON.parse(text);
      await setDataToSheet(sheet, jsonData, 0, 'ThirdpartyHourUsage');
    } catch (error) {
      console.error('Error loading ThirdpartyHourUsage:', error);
    } finally {
      pageLoading.value = false;
    }
  };

  // 加载第三方日数据
  const loadThirdpartyDayUsageData = async (sheet: GC.Spread.Sheets.Worksheet, month: string | undefined) => {
    pageLoading.value = true;
    console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Start Download ThirdpartyDayUsage`); // 使用 dayjs 格式化日期
    try {
      const response = await RepFunApi.getThirdpartyDayUsageDataList(month);
      const zip = await JSZip.loadAsync(response.data);
      const text = await zip.file('apidata')!.async('text');
      console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]UnZip Completed Start Load apidata`); // 使用 dayjs 格式化日期
      const jsonData = JSON.parse(text);
      await setDataToSheet(sheet, jsonData, 0, 'ThirdpartyDayUsage');
    } catch (error) {
      console.error('Error loading ThirdpartyDayUsage:', error);
    } finally {
      pageLoading.value = false;
    }
  };

  // 加载第三方班数据
  const loadThirdpartyClassUsageData = async (sheet: GC.Spread.Sheets.Worksheet, month: string | undefined) => {
    pageLoading.value = true;
    console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Start Download ThirdpartyClassUsage`); // 使用 dayjs 格式化日期
    try {
      const response = await RepFunApi.getThirdpartyClassUsageDataList(month);
      const zip = await JSZip.loadAsync(response.data);
      const text = await zip.file('apidata')!.async('text');
      console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]UnZip Completed Start Load apidata`); // 使用 dayjs 格式化日期
      const jsonData = JSON.parse(text);
      await setDataToSheet(sheet, jsonData, 0, 'ThirdpartyClassUsage');
    } catch (error) {
      console.error('Error loading ThirdpartyClassUsage:', error);
    } finally {
      pageLoading.value = false;
    }
  };

  // 加载第三方WEEK数据
  const loadThirdpartyWeekUsageData = async (sheet: GC.Spread.Sheets.Worksheet, year: string | undefined) => {
    pageLoading.value = true;
    console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}] Start Download ThirdpartyWeekUsage`);
    try {
      const response = await RepFunApi.getThirdpartyWeekUsageDataList(year);
      if (response.success) {
        setDataToSheet(sheet, response.result, 0, 'ThirdpartyWeekUsage');
      } else {
        createMessage.error(response.message, 5);
      }
    } catch (error) {
      console.error('Error loading ThirdpartyWeekUsage:', error);
    } finally {
      pageLoading.value = false;
    }
  };

  // 加载第三方MTD数据
  const loadThirdpartyMtdUsageData = async (sheet: GC.Spread.Sheets.Worksheet, year: string | undefined) => {
    pageLoading.value = true;
    console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}] Start Download ThirdpartyMtdUsage`);
    try {
      const response = await RepFunApi.getThirdpartyMtdUsageDataList(year);
      if (response.success) {
        setDataToSheet(sheet, response.result, 0, 'ThirdpartyMtdUsage');
      } else {
        createMessage.error(response.message, 5);
      }
    } catch (error) {
      console.error('Error loading ThirdpartyMtdUsage:', error);
    } finally {
      pageLoading.value = false;
    }
  };
  // 加载中间表小时数据
  const loadOrsHourUsageData = async (sheet: GC.Spread.Sheets.Worksheet, endDate: string | undefined) => {
    if (!pickerTagIds.value) {
      createMessage.warning('请选择最多5个点位', 5);
      return;
    } else if (pickerTagIds.value.split(',').length > 5) {
      createMessage.warning('最多只能选择5个点位', 5);
      return;
    }
    pageLoading.value = true;
    console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Start Download OrsHourUsage`); // 使用 dayjs 格式化日期
    try {
      const response = await RepFunApi.getOrsdataHourUsageList(
        endDate?.split('-').length === 2 ? endDate + '-01' : endDate,
        pickerTagIds?.value?.split(',') ?? ['NULL'],
        selRepShowFlag.value ?? undefined,
        selRepShowType.value ?? undefined
      );
      const zip = await JSZip.loadAsync(response.data);
      const text = await zip.file('apidata')!.async('text');
      console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]UnZip Completed Start Load apidata`); // 使用 dayjs 格式化日期
      const jsonData = JSON.parse(text);
      await setDataToSheet(sheet, jsonData, 0, 'OrsHourUsage');
    } catch (error) {
      console.error('Error loading OrsHourUsage:', error);
    } finally {
      pageLoading.value = false;
    }
  };

  // 加载中间表日数据
  const loadOrsDayUsageData = async (sheet: GC.Spread.Sheets.Worksheet, month: string | undefined) => {
    pageLoading.value = true;
    console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Start Download OrsDayUsage`); // 使用 dayjs 格式化日期
    try {
      const response = await RepFunApi.getOrsdataDayUsageList(month);
      const zip = await JSZip.loadAsync(response.data);
      const text = await zip.file('apidata')!.async('text');
      console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]UnZip Completed Start Load apidata`); // 使用 dayjs 格式化日期
      const jsonData = JSON.parse(text);
      await setDataToSheet(sheet, jsonData, 0, 'OrsDayUsage');
    } catch (error) {
      console.error('Error loading OrsDayUsage:', error);
    } finally {
      pageLoading.value = false;
    }
  };

  // 加载中间表班数据
  const loadOrsClassUsageData = async (sheet: GC.Spread.Sheets.Worksheet, month: string | undefined) => {
    pageLoading.value = true;
    console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Start Download OrsClassUsage`); // 使用 dayjs 格式化日期
    try {
      const response = await RepFunApi.getOrsdataClassUsageList(month);
      const zip = await JSZip.loadAsync(response.data);
      const text = await zip.file('apidata')!.async('text');
      console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]UnZip Completed Start Load apidata`); // 使用 dayjs 格式化日期
      const jsonData = JSON.parse(text);
      await setDataToSheet(sheet, jsonData, 0, 'OrsClassUsage');
    } catch (error) {
      console.error('Error loading OrsClassUsage:', error);
    } finally {
      pageLoading.value = false;
    }
  };

  // 加载中间表周数据
  const loadOrsWeekUsageData = async (sheet: GC.Spread.Sheets.Worksheet, year: string | undefined) => {
    pageLoading.value = true;
    console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}] Start Download OrsWeekUsage`);
    try {
      const response = await RepFunApi.getOrsdataWeekUsageList(year);
      if (response.success) {
        setDataToSheet(sheet, response.result, 0, 'OrsWeekUsage');
      } else {
        createMessage.error(response.message, 5);
      }
    } catch (error) {
      console.error('Error loading OrsWeekUsage:', error);
    } finally {
      pageLoading.value = false;
    }
  };

  // 加载中间表MTD数据
  const loadOrsMtdUsageData = async (sheet: GC.Spread.Sheets.Worksheet, year: string | undefined) => {
    pageLoading.value = true;
    console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}] Start Download OrsMtdUsage`);
    try {
      const response = await RepFunApi.getOrsdataMtdUsageList(year);
      if (response.success) {
        setDataToSheet(sheet, response.result, 0, 'OrsMtdUsage');
      } else {
        createMessage.error(response.message, 5);
      }
    } catch (error) {
      console.error('Error loading OrsMtdUsage:', error);
    } finally {
      pageLoading.value = false;
    }
  };

  // 其他加载数据方法...

  // 主函数：将数据设置到指定 Sheet 中
  const setDataToSheet = (sheet: GC.Spread.Sheets.Worksheet, jsonData: any[], firstRow: number, apiName: string) => {
    sheet.suspendPaint(); // 暂停渲染，提升性能
    // 确保表格大小足以容纳数据
    ensureSheetSize(sheet, jsonData);
    // 根据 apiName 处理数据（如颜色、批注等），返回用于写入的数据
    const allData = transformData(sheet, jsonData, apiName);
    console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Completed Download ${apiName} Start Set Array Data`);
    // 将处理好的二维数组写入到 sheet
    sheet.setArray(firstRow, 0, apiName === 'OrsAllUsage' ? allData : jsonData);
    // 如果是 StepEnergyUsage 类型，设置行样式和列宽
    if (apiName === 'StepEnergyUsage') {
      applyStepEnergyStyle(sheet);
    }
    // 如果需要，添加配置页 sheet
    ensureConfigSheetExists(apiName);
    // 恢复渲染、清除变更缓存并刷新
    sheet.resumePaint();
    sheet.clearPendingChanges();
    curSpread.value!.refresh();
    console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Completed ${apiName} Set Array Data`);
  };

  // 保证表格的行列数不小于数据大小
  const ensureSheetSize = (sheet: GC.Spread.Sheets.Worksheet, jsonData: any[]) => {
    console.log(jsonData, '================');
    const rowCount = jsonData.length;
    const colCount = jsonData[0].length;
    const currentRows = sheet.getRowCount();
    const currentCols = sheet.getColumnCount();
    if (rowCount > currentRows) {
      sheet.addRows(currentRows, rowCount - currentRows + 1);
    }
    if (colCount > currentCols) {
      sheet.addColumns(currentCols, colCount - currentCols + 1);
    }
  };

  // 根据 apiName 判断是否需要特殊处理颜色、批注等，返回处理后的二维数组
  const transformData = (sheet: GC.Spread.Sheets.Worksheet, jsonData: any[], apiName: string): any[][] => {
    const allData = [...jsonData.map((row) => [...row])]; // 深拷贝一份用于写入
    jsonData.forEach((row, i) => {
      row.forEach((cell, j) => {
        if (apiName === 'OrsAllUsage' && cell && typeof cell === 'object') {
          // 设置背景色等
          handleCellColor(sheet, i, j, cell);
          // 添加批注（如存在）
          handleCellComment(sheet, i, j, cell);
          // 提取最终写入值
          allData[i][j] = cell.cellValue ?? null;
        } else {
          // 普通数值字段转数字
          const num = Number(cell);
          if (!isNaN(num)) jsonData[i][j] = num;
        }
      });
    });
    return allData;
  };

  // 处理单元格背景色、前景色
  const handleCellColor = (sheet: GC.Spread.Sheets.Worksheet, row: number, col: number, cell: any) => {
    if (!cell.cellValue || !cell.cellColor) return;
    const cellRef = sheet.getCell(row, col);
    switch (cell.cellColor) {
      case '#FF33FF': // 紫色高亮
        cellRef.foreColor('#FFFFFF').backColor('#660066');
        break;
      case '#009900': // 绿色背景
        cellRef.backColor('#008000');
        break;
      case '#990033': // 红色背景
        cellRef.backColor('#FF0000');
        break;
      case '#FFFF00': // 黄色背景
        cellRef.backColor('#FFFF00');
        break;
    }
  };

  // 如果存在错误信息，为该单元格添加批注
  const handleCellComment = (sheet: GC.Spread.Sheets.Worksheet, row: number, col: number, cell: any) => {
    if (!cell.cellValue || !cell.errorMsg) return;
    const comment = new GC.Spread.Sheets.Comments.Comment();
    comment
      .text(cell.errorMsg)
      .width(500)
      .height(cell.errorMsg.length + 70); // 高度按内容长度估算
    sheet.getCell(row, col).comment(comment);
  };

  // 为 StepEnergyUsage 类型设置标题样式和列宽
  const applyStepEnergyStyle = (sheet: GC.Spread.Sheets.Worksheet) => {
    const style = new GC.Spread.Sheets.Style();
    style.backColor = '#ed7d31'; // 橙色背景
    sheet.setStyle(0, -1, style); // 设置整行样式
    sheet.setColumnCount(headProps.length, GC.Spread.Sheets.SheetArea.viewport);
    headProps.forEach((elem, index) => {
      sheet.setColumnWidth(index, elem.size);
    });
  };

  // 如果没有“配置页”并且当前为 OrsAllUsage 类型，添加配置页
  const ensureConfigSheetExists = (apiName: string) => {
    if (apiName !== 'OrsAllUsage') return;
    const configSheet = curSpread.value!.getSheetFromName('配置页');
    if (configSheet !== null) return;
    const newSheet = new GC.Spread.Sheets.Worksheet('配置页');
    newSheet.getCell(0, 0).text('名称');
    newSheet.getCell(0, 1).text('日期维度');
    newSheet.getCell(0, 2).text('ID');
    newSheet.getCell(0, 3).text('日期');
    curSpread.value!.addSheet(1, newSheet);
  };
</script>

<style lang="less" scoped>
  .jeecg-basic-table-form-container {
    padding: 0;
    .table-page-search-submitButtons {
      display: block;
      margin-bottom: 24px;
      white-space: nowrap;
    }
    .spreadContainer {
      padding: 10px;
      padding-left: 0;
      height: 60vh;
    }
    .spreadHost {
      width: 95%;
      height: 100%;
    }
    .footer-btn {
      margin-top: 10px;
      text-align: center;
    }
    .footer-btn button {
      width: 169px;
      height: 42px;
      font-size: 15px;
      border-radius: 0;
    }
  }
</style>
