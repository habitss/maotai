<template>
  <a-spin :spinning="pageLoading">
    <div class="p-2">
      <div class="jeecg-basic-table-form-container">
        <a-form style="background-color: #fff; padding: 10px 10px">
          <div style="display: flex; align-items: center; padding: 5px 0px 12px 0px">
            <div><a-input v-model:value="reportNameX" placeholder="请输入要发布报表名称" :maxlength="20" allowClear /></div>
            <div style="margin-left: 10px">
              <a-upload class="custom-upload" :before-upload="() => false" :max-count="1" accept=".xlsx,.xls" @change="uploadOnChange">
                <a-button type="primary">选择文件</a-button>
              </a-upload>
            </div>
          </div>
        </a-form>
        <div class="spreadContainer">
          <gc-spread-sheets hostClass="spreadHost" @workbook-initialized="workbookInitialized">
            <GcWorksheet />
          </gc-spread-sheets>

          <CombinedChart
            v-if="showSpreadChart"
            class-name="spreadChart"
            :x-title="xTitle"
            :yaxis-datas="yaxisDatas"
            :x-datas="xDatas"
            :serie-datas="serieDatas"
            :legend-datas="legendDatas"
          />
        </div>
        <div class="footer-btn">
          <a-button type="primary" @click="uploadAndPubReport">上传并发布报表</a-button>
        </div>
      </div>
    </div>
  </a-spin>
</template>

<script lang="ts" name="ems-CustomReportImport" setup>
  import { nextTick, ref } from 'vue';
  import { useRouter } from 'vue-router';
  import CombinedChart from './components/CombinedChart.vue';
  import '@grapecity/spread-sheets/styles/gc.spread.sheets.excel2016colorful.css';
  import * as GC from '@grapecity/spread-sheets';
  import { GcSpreadSheets, GcWorksheet } from '@grapecity/spread-sheets-vue';
  import '@grapecity/spread-sheets-resources-zh';
  import '@grapecity/spread-sheets-io';
  import '@grapecity/spread-sheets-charts';
  import '@grapecity/spread-sheets-shapes';
  import dayjs from 'dayjs';
  import { useMessage } from '@/hooks/web/useMessage';
  import * as MySpreadUtils from '@/utils/spreadUtils';
  import * as CombinedChartApi from './components/CombinedChart.api';
  import { baseDataList } from './ReportFunction.data';
  import { UploadRepParams, addCustomReportSetting } from './ReportSetting.api';
  const { createMessage, createConfirm } = useMessage();
  const router = useRouter();

  // 初始化 Spread
  GC.Spread.Common.CultureManager.culture('zh-cn');
  let SpreadJSKey =
    'emsapac.ab-inbev.cn,623376735916589#B1v4r7N0dHWyBDcwkXVMJ7Qr9UWtpWUYlzbzgjUORkUY3GOzEVNHZFNp5kZGdXc4xEbvAVUOh6VGJWWWdTORJjTMRWdyoXS4B5MJNHdwAlVmt6MRNTb4gTTxMzVrUTNhZldQ94Yx2yVrlzT4JFV6U7KQdlMB3SZmVFbzUFTxJVVN9kUNdFOHJXcDN4YxpmTPl5VsBXRz2UQvsGcyAHTqlEa8YmRk5UMhd4dCtkTDpmVCl6aQdETKJmUNNUZyAnSrd6Y68kc8VnV62GN4Elc4QjWFR6Sv2iWy24Yt5UYFR7RhZXaXNjU6hjUY3WcJhmetNUR5ZGTZNkS85EcU3GbwdlI0IyUiwiI8QUNyADNGZjI0ICSiwSO7AzNyQTO8UTM0IicfJye&Qf35VficFVDplI0IyQiwiI6EjL6ByUKBCZhVmcwNlI0IiTis7W0ICZyBlIsISOzMDM8ADI9ETNwMjMwIjI0ICdyNkIsIibj9idlJmbp5iYh9yYhBXYz5WZiojIz5GRiwiI8+Y9sWY9QmZ0Jyp9Li09leb9WyY9oqY9qeI0R+K0+0L0JGr9m6q9iojIh94QiwiI9gTN6ETO5MzN6czMzIjNiojIklkIs4XXbpjInxmZiwSZzxWYmpjIyNHZisnOiwmbBJye0ICRiwiI34TQmBjSNNlZJtUOGxEVxZje75kY8MjWMdER6QmMrg7RmZTbLRUQRp7cM9GZ5dWRv2kTsdUN6onewpnepB5ZhljWxN4dnRUaSdTOGNWWUxkbFhlZrpVb7q3'; // Enter a valid license key.
  GC.Spread.Sheets.LicenseKey = SpreadJSKey;

  // 初始化数据
  const pageLoading = ref(false);
  const curSpread = ref<GC.Spread.Sheets.Workbook | null>(null);
  const reportNameX = ref('');
  const uploadFile = ref<File | null>(null);

  const showSpreadChart = ref(false);
  const xTitle = ref('');
  const xDatas = ref<string[]>([]);
  const yaxisDatas = ref<any[]>([]);
  const serieDatas = ref<any[]>([]);
  const legendDatas = ref<string[]>([]);

  const baseSheetNames = baseDataList.map((item) => item.label);

  // WorkbookInitialized 回调
  const workbookInitialized = (spread: GC.Spread.Sheets.Workbook) => {
    curSpread.value = spread;
    //绑定 Sheet 页切换事件
    spread.bind(GC.Spread.Sheets.Events.ActiveSheetChanged, (e, args) => {
      console.log(e);
      const name = args.newSheet.name();
      const sheet = curSpread.value!.getActiveSheet();
      if (name.includes('图表') && sheet.getValue(0, 0) === '图表') {
        showSpreadSheetChart(sheet);
      } else {
        showSpreadChart.value = false;
      }
      sheet.resumePaint();
      sheet.repaint();
    });
    curSpread.value.refresh();
    console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Init Spread Completed: ${spread}`); // 使用 dayjs 格式化日期
  };

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

  const uploadAndPubReport = async () => {
    if (!reportNameX.value.trim()) {
      createMessage.error('请输入当前要发布的报表名称');
      return;
    }
    pageLoading.value = true;
    const sheetCount: number = curSpread.value!.getSheetCount();
    const basedataNames: string[] = [];
    for (let i = 0; i < sheetCount; i++) {
      const sheet = curSpread.value!.getSheet(i);
      const sheetName = sheet.name();
      if (baseSheetNames.includes(sheetName)) {
        basedataNames.push(sheetName);
      }
    }

    if (!uploadFile.value) {
      createMessage.error('请先上传您要发布的报表');
      return;
    }
    //上传参数
    const params: UploadRepParams = {
      file: uploadFile.value,
      name: 'excelFile',
      data: {
        repName: reportNameX.value,
        baseRepNames: basedataNames.join(','),
      },
    };
    await addCustomReportSetting(params, (res) => {
      if (res.success) {
        createMessage.success(res.message || '上传成功');
        //刷新当前路由
        router.replace({
          path: router.currentRoute.value.path,
          query: {
            ...router.currentRoute.value.query,
            t: Date.now(),
          },
        });
      } else {
        createMessage.error(res.message || '上传失败');
        pageLoading.value = false;
      }
    });
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
    .custom-upload {
      display: flex;
      /deep/ .ant-upload-list {
        margin-left: 10px;
      }
    }
    .spreadContainer {
      padding: 10px;
      padding-left: 0;
      height: 70vh;
    }
    .spreadHost {
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
    .spreadChart {
      background-color: rgba(51, 53, 52, 0.9);
      width: calc(100% - 49px);
      height: calc(70vh - 48px);
      position: absolute;
      top: 108px;
      left: 20px;
    }
  }
</style>
