<template>
  <div style="margin-top: 10px">
    <BarMulti :chartData="barMultiData" :option="multiBarOption" height="33vh"></BarMulti>
  </div>
</template>

<script setup lang="ts">
import BarMulti from "@/components/chart/BarMulti.vue";
import {getData} from "@/views/report/chartdemo/chartdemo.data";
import {onMounted, reactive, ref, unref, onBeforeUnmount, h} from "vue";
import {REPORT_HIGHRISE_BOTTLEINSPECTIONPERCENT} from "@/views/dashboard/highRiseBoard/index.api";

const {barDataSource, barMultiData, pieData, barLineData, radarData} = getData;
let chartData = ref([
  {
    "id": null,
    "tenantId": null,
    "factoryCode": null,
    "deptCode": null,
    "lineCode": "A04A01A01A01A01",
    "createTime": null,
    "year": null,
    "month": null,
    "totalNum": null,
    "okNum": null,
    "bottleCode": 24,
    "percent": "79",
    "lineName": "1#",
    "bottleName": "450烤花"
  },
  {
    "id": null,
    "tenantId": null,
    "factoryCode": null,
    "deptCode": null,
    "lineCode": "A04A01A01A01A01",
    "createTime": null,
    "year": null,
    "month": null,
    "totalNum": null,
    "okNum": null,
    "bottleCode": 24,
    "percent": "86",
    "lineName": "1#",
    "bottleName": "720红"
  },
  {
    "id": null,
    "tenantId": null,
    "factoryCode": null,
    "deptCode": null,
    "lineCode": "A04A01A01A01A01",
    "createTime": null,
    "year": null,
    "month": null,
    "totalNum": null,
    "okNum": null,
    "bottleCode": 24,
    "percent": "53",
    "lineName": "1#",
    "bottleName": "720黄"
  },
  {
    "id": null,
    "tenantId": null,
    "factoryCode": null,
    "deptCode": null,
    "lineCode": "A04A01A01A01A01",
    "createTime": null,
    "year": null,
    "month": null,
    "totalNum": null,
    "okNum": null,
    "bottleCode": 24,
    "percent": "87",
    "lineName": "1#",
    "bottleName": "720雪啤"
  },
  {
    "id": null,
    "tenantId": null,
    "factoryCode": null,
    "deptCode": null,
    "lineCode": "A04A01A01A01A01",
    "createTime": null,
    "year": null,
    "month": null,
    "totalNum": null,
    "okNum": null,
    "bottleCode": 24,
    "percent": "77.74",
    "lineName": "1#",
    "bottleName": "620"
  },

  {
    "id": null,
    "tenantId": null,
    "factoryCode": null,
    "deptCode": null,
    "lineCode": "A04A01A01A01A01",
    "createTime": null,
    "year": null,
    "month": null,
    "totalNum": null,
    "okNum": null,
    "bottleCode": 24,
    "percent": "86",
    "lineName": "2#",
    "bottleName": "450烤花"
  },
  {
    "id": null,
    "tenantId": null,
    "factoryCode": null,
    "deptCode": null,
    "lineCode": "A04A01A01A01A01",
    "createTime": null,
    "year": null,
    "month": null,
    "totalNum": null,
    "okNum": null,
    "bottleCode": 24,
    "percent": "45",
    "lineName": "2#",
    "bottleName": "720红"
  },
  {
    "id": null,
    "tenantId": null,
    "factoryCode": null,
    "deptCode": null,
    "lineCode": "A04A01A01A01A01",
    "createTime": null,
    "year": null,
    "month": null,
    "totalNum": null,
    "okNum": null,
    "bottleCode": 24,
    "percent": "65",
    "lineName": "2#",
    "bottleName": "720黄"
  },
  {
    "id": null,
    "tenantId": null,
    "factoryCode": null,
    "deptCode": null,
    "lineCode": "A04A01A01A01A01",
    "createTime": null,
    "year": null,
    "month": null,
    "totalNum": null,
    "okNum": null,
    "bottleCode": 24,
    "percent": "23",
    "lineName": "2#",
    "bottleName": "720雪啤"
  },
  {
    "id": null,
    "tenantId": null,
    "factoryCode": null,
    "deptCode": null,
    "lineCode": "A04A01A01A01A01",
    "createTime": null,
    "year": null,
    "month": null,
    "totalNum": null,
    "okNum": null,
    "bottleCode": 24,
    "percent": "34",
    "lineName": "2#",
    "bottleName": "620"
  },
])
// 设置定时器，每5分钟执行一次 getData 方法
const intervalId = setInterval(getChartData, 5 * 60 * 1000);
// 在组件销毁之前清除定时器，以防止内存泄漏
onBeforeUnmount(() => {
  clearInterval(intervalId);
});
onMounted(() => {
  getChartData()
})

const multiBarOption: any = ref({
  title: {text: '今日验瓶机合格率', left: 'left', textStyle: {color: '#fff'}},
  grid: {
    bottom: 30,
    left: '15%'
  },
  xAxis: {
    axisLine: {
      lineStyle: {
        color: '#fff',
      }
    },
    axisLabel: {
      textStyle: {
        fontSize: 18
      },
    },
    data: []
  },
  yAxis: {
    axisLine: {
      lineStyle: {
        color: '#fff'
      }
    },
    axisLabel: {
      textStyle: {
        fontSize: 18
      },
      formatter: function (value) {
        return value + "%";
      },
    },
    splitLine: {
      show: true,
      lineStyle: {
        opacity: 0.2
      }
    },
  },
  legend: {
    top: '10%',
    textStyle: {
      color: '#fff',
    }
  },
  series: []
});

function getItemStyleColor(type) {
  console.log(type, 'type')
  switch (type) {
    case '450ML烤花（小7）':
      return '#5d86e5';
    case '720ML烤花(红7)':
      return '#7cb9c2';
    case '720ML烤花(黄7)':
      return '#6ba364';
    case '720ML烤花(雪啤)':
      return '#eabf5a';
    case '620':
      return '#df7c41';
  }
}

function fetchData(url, body) {
  return fetch(url, body)
    .then((response) => response.json()) // 将Response对象转换为JSON格式
    .then((data) => {
      // 在这里可以对获取到的数据进行处理
      return data; // 返回处理后的数据
    }).catch(() => {});
}

async function getChartData() {
  // const res = await REPORT_HIGHRISE_BOTTLEINSPECTIONPERCENT()
  // if(res.code === 200) {
  //   console.log(res, '验瓶机效率')
  //   chartData.value = res.result
  // }
  let values = {
    tagIds: [
      '25304', //进瓶
      '25305', //出瓶
      '25303', //sku
    ]
  }
  let res = await fetchData('http://192.168.17.123:8000/api/v1/calc-pi-clean/InfluxDb/GetLastTagInfoByTagIds', {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify(values),
  });
  // let res = {
  //   "code": 200,
  //   "message": "success",
  //   "data": [
  //     {
  //       "measurement": "basedata_second",
  //       "timeUtc": "2024-07-31 15:43:59",
  //       "tagId": "25303",
  //       "value": 21
  //     },
  //     {
  //       "measurement": "basedata_second",
  //       "timeUtc": "2024-07-31 15:43:59",
  //       "tagId": "25304",
  //       "value": 21770
  //     },
  //     {
  //       "measurement": "basedata_second",
  //       "timeUtc": "2024-07-31 15:43:59",
  //       "tagId": "25305",
  //       "value": 21256
  //     }
  //   ]
  // }
  let bottlecode = res.data.find(i => i.tagId === '25303').value
  let jin = res.data.find(i => i.tagId === '25304').value
  let chu = res.data.find(i => i.tagId === '25305').value

  // const allType = Array.from(new Set(unref(chartData).map((item) => item.bottleName)))
  // multiBarOption.value.xAxis['data'] = Array.from(new Set(unref(chartData).map((item) => item.lineName)))
  // multiBarOption.value.series = allType.map(type => {
  //   let obj = {
  //     name: type,
  //     type: 'bar',
  //     barGap: 0.1, //柱子之间间距
  //     itemStyle: {
  //       color: getItemStyleColor(type),
  //     },
  //     label: {
  //       show: true,
  //       position: 'top',
  //       textStyle: {
  //         color: '#fff',
  //         fontSize: 18
  //       },
  //       formatter: function (value) {
  //         return value.data + "%";
  //       },
  //     },
  //     // lineStyle: {
  //     //   color: '#cc7a5f'
  //     // },
  //     data: unref(chartData).filter(i => i.bottleName === type).map(item => Number(item.percent).toFixed(0))
  //   }
  //   return obj
  // })

  const allType = [
    { code: 1, bottleName: '450ML异型(棕瓶)' },
    { code: 6, bottleName: '720ml' },
    { code: 8, bottleName: '600ml' },
    { code: 9, bottleName: '720ml' },
    { code: 10, bottleName: '720ml' },
    { code: 14, bottleName: '620ml' },
    { code: 15, bottleName: '450ml' },
    { code: 22, bottleName: '720ml' },
    { code: 23, bottleName: '450ml' },
    { code: 26, bottleName: '720ML烤花(雪啤)' },
    { code: 17, bottleName: '720ML异型(棕瓶)' },
    { code: 4, bottleName: '500ML异型(棕瓶)' },
    { code: 5, bottleName: '500ML异型(白瓶)' },
    { code: 11, bottleName: '600ML异型(绿瓶)' },
    { code: 13, bottleName: '450ML异型(蓝瓶)' },
    { code: 17, bottleName: '720ML异型(棕瓶)' },
    { code: 21, bottleName: '720ML烤花(红7)' },
    { code: 24, bottleName: '720ML烤花(黄7)' },
    { code: 25, bottleName: '450ML烤花（小7）' },
    { code: 27, bottleName: '720ML烤花(雪啤)' },
  ]
  multiBarOption.value.xAxis['data'] = Array.from(new Set(unref(chartData).map((item) => item.lineName)))
  multiBarOption.value.series = [
    {
      name: allType.find(i => bottlecode === i.code).bottleName,
      type: 'bar',
      barGap: 0.1, //柱子之间间距
      itemStyle: {
        color: getItemStyleColor(allType.find(i => bottlecode === i.code).bottleName),
      },
      label: {
        show: true,
        position: 'top',
        offset: [0, 30],
        textStyle: {
          color: '#fff',
          fontSize: 18
        },
        formatter: function (value) {
          return value.data + "%";
        },
      },
      // lineStyle: {
      //   color: '#cc7a5f'
      // },
      data: [Number((chu / jin) * 100).toFixed(2)]
    }
  ]
  console.log(multiBarOption, 'multiBarOptionmultiBarOptionmultiBarOption')
}


</script>

<style scoped>
.total-title {
  text-align: center;
  font-size: 18px;
  font-weight: bold;
  color: #fff;
}
</style>
