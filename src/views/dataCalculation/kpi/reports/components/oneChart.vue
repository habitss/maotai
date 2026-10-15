<template>
   <a-card :bordered="false">
        <a-row>
            <a-col :span="12">
                <br>
                <div style="min-height: 320px;">
                    <div class="t2"> {{ reportObj.kpiName }}(一级柏拉图)</div>
                    <a-table ref="table" size="middle" bordered rowKey="id" :columns="columns" :scroll="{ x: true }"
                        :dataSource="dataSource" :pagination="false" :loading="loading">
                    </a-table>
                </div>

            </a-col>
            <a-col :span="12">
              <!-- <div id="aa" style="height:400px;width:100%; "></div> -->
              <Bar style="height:400px;width:100%;" :option="oneOption" v-if="isOneChart"></Bar>
            </a-col>
        </a-row>

        <a-row>
            <a-col :span="12">
                <template v-for="(val, key, i) in objArr" :key="i">
                    <div style="min-height: 300px;">
                        <div class="title"> {{ key }}(二级柏拉图)</div>
                        <a-table ref="table" size="middle" bordered rowKey="id" :columns="columns" :scroll="{ x: true, height: 300 }" 
                            :dataSource="val" :pagination="false" :loading="loading">
                        </a-table>
                    </div>
                </template>
            </a-col>
            <a-col :span="12">
                <template v-for="(arr, key, index) in objArr" :key="index">
                    <!-- <div :id="['aa' + index]" style="height:300px;width:100%; "> </div> -->
                    <Bar style="height:400px;width:100%;" :option="twoCharOption[index]" ></Bar>
                </template>
            </a-col>
        </a-row>

        <a-row>
            <a-col :span="12">
                <template v-for="(val, key, i) in threeObjArr" :key="i">
                    <div style="min-height: 300px;">
                        <!-- <div class="t2"> {{ reportObj.kpiName }}(二级柏拉图)</div> -->
                        <div class="title"> {{ key }}(三级柏拉图)</div>
                        <a-table ref="table" size="middle" bordered rowKey="id" :columns="columns" :scroll="{ x: true, height: 300 }" 
                          :dataSource="val" :pagination="false" :loading="loading">
                        </a-table>
                    </div>
                </template>
            </a-col>
            <a-col :span="12">
                <template v-for="(arr, key, index) in threeObjArr" :key="index">
                    <!-- <div :id="['mm' + index]" style="height:300px;width:100%; "> </div> -->
                    <Bar style="height:400px;width:100%;" :option="threeCharOption[index]" ></Bar>
                </template>
            </a-col>
        </a-row>
    </a-card>
</template>

<script lang="ts" setup>
  import { ref, nextTick, reactive,defineExpose, unref } from 'vue';
  // import { queryKpiReportHeader,listKpiReportData } from '../kpiWeekAndMonReports.api';
  import { listPlatoTree } from '/@/views/dataCalculation/kpi/plato/KpiPlatoOne.api';
  import Bar from '/@/components/chart/Bar.vue';
  const columns = ref(
    [
      { dataIndex: 'a1', title: " 影响原因", align: 'center' },
      { dataIndex: 'a2', title: "损耗差值", align: 'center' },
      { dataIndex: 'a3', title: "损耗占比", align: 'center' },
      { dataIndex: 'a4', title: "累计百分比", align: 'center' }
    ]
  )
  const reportObj = ref({})
  const loading = ref(false)
  //一级柏拉图
  const dataSource = ref([])
  //二级柏拉图
  const objArr = ref({})
  //三级柏拉图
  const threeObjArr = ref({})
  //一级柏拉图-图表结构
  const oneOption = ref([] as any);
  //一级柏拉图-图表是否显示
  const isOneChart = ref(false)
  //二级柏拉图-图表结构
  const twoCharOption = ref([] as any);
  //三级柏拉图-图表结构
  const threeCharOption = ref([] as any);

  async function init(params){
    reportObj.value['kpiName'] = params.kpiParams.kpiName;
    //获取数据
    loadData(params)
  }

  //一级柏拉图
  async function loadData(params){
    let month = ""
    if(params.queryParam.month){
        month = params.queryParam.month
    }
    debugger
    let kpiCode = params.kpiParams.kpiCode;
    let query = { "month": month, "kpiCode": kpiCode }
    await listPlatoTree(query).then((res) => {
      if(res){
        let arr = []
        let xArr = [], yArr = [], percent = []
        let temp = 0
        if (res.length == 0) {
            dataSource.value = []
            chart([], [], [])
            return
        }

        let numAll = getAllNum(res)
        res && res.length > 0 && res.forEach(element => {
            temp += parseFloat(element.value)

            arr.push(
                { a1: element.name, a2: element.value, a3: parseFloat((element.value / numAll) * 100).toFixed(2) + '%', a4: parseFloat((temp / numAll) * 100).toFixed(2) + '%' },
            )
            xArr.push(element.name)
            yArr.push(element.value)
            percent.push(((temp / numAll) * 100).toFixed(2))
        });
        //表格数据
        dataSource.value = arr
        //图表数据
        chart(xArr, yArr, percent)
        //获取二级柏拉图
        setTwoLevel(res)

      }
    })

  }
  //一级柏拉图(图表)
  function chart(x, y, percent) {
      let option = {
          //标题
          title: {
              text: '柏拉图',
              left: 'center' //居中显示
          },
          tooltip: {
              trigger: 'axis',
              axisPointer: {
                  type: 'cross',
                  crossStyle: {
                      color: '#999'
                  }
              }
          },
          grid: {
              borderWidth: '10',
              borderColor: '#eeeeee'
          },
          legend: {
              //selectedMode: false,
              //data:['水量','占比']
              data: ['', '']
          },
          xAxis: [
              {
                  type: 'category',
                  //显示横坐标中对应值的坐标线（竖线）：默认不显示
                  //splitLine:{show:true},
                  // data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
                  // axisTick: { alignWithLabel: true },
                  data: x
              }
              , {
                  type: 'category',
                  show: false,
                  boundaryGap: false,
                  // data: ['', '1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
                  data: [0, ...x]
              }
          ],
          yAxis: [
              {
                  type: 'value',
                  //纵坐标轴上的各个值对应的横线是否显示：默认显示
                  splitLine: false,
                  name: '',
                  min: 0,
                  // max: 250,
                  interval: 50,
                  axisLabel: {
                      formatter: '{value} ml'
                  }
              },
              {
                  type: 'value',
                  name: '占比',
                  min: 0,
                  max: 100,
                  interval: 15,
                  //纵坐标轴上的各个值对应的横线是否显示：默认显示
                  splitLine: false,
                  //splitArea : {show : true}, //保留网格区域
                  axisLabel: {
                      formatter: '{value} %'
                  }
              }
          ],
          series: [
              {
                  name: ' ',
                  type: 'bar',
                  //柱状图之间的空隙为0
                  barCategoryGap: '0',

                  //设置柱状图的颜色
                  itemStyle: {
                      normal: {
                          color: function (params) {
                              var colorList = [
                                  '#5e7e54', '#e44f2f', '#81b6b2', '#eba422', '#5e7e54',
                                  '#e44f2f', '#81b6b2', '#eba422', '#5e7e54', '#e44f2f', '#2e7e04', '#e00f2f'
                              ];
                              return colorList[params.dataIndex]
                          },
                          //color: '#5a9bd5',
                          //设置柱状图边框线颜色
                          //barBorderColor: 'black',
                          //设置柱状图边框线的显示宽度
                          //barBorderWidth:0.2
                      }
                  },
                  // data: [15, 10, 9, 8, 8, 8, 8, 8, 8, 7, 6, 5]
                  data: y
              },
              {
                  name: '占比',
                  type: 'line',
                  symbolSize: 10,
                  //拐点显示设置（新版本EChart需要设置）
                  symbol: function (value) {
                      if (value > 0) {//值大于0才显示
                          return 'circle'; //拐点类型
                      } else {
                          return 'none'; //拐点不显示
                      }
                  },
                  //显示设置
                  itemStyle:
                  {
                      normal: {
                          //color:'#f37b1d',
                          color: function (params) {
                              var colorList = [
                                  '', '#f37b1d', '#f37b1d', '#f37b1d', '#f37b1d',
                                  '#f37b1d', '#f37b1d', '#f37b1d', '#f37b1d',
                                  'red', '#f37b1d', '#f37b1d', ''];
                              return colorList[params.dataIndex]
                          },
                          label:
                          {
                              show: true,
                              //格式化
                              formatter: '{c}%'
                          }
                      }
                  },
                  xAxisIndex: 1,
                  yAxisIndex: 1,
                  data: [0, ...percent]
              }
              ,

          ]
      };
      nextTick(() => {
          //let chart = this.$echarts.init(document.getElementById('aa'));
          // chart.setOption(option, true);
          // window.addEventListener("resize", chart.resize);
          oneOption.value = option
          isOneChart.value = true
      })
  }

  //二级柏拉图
  function setTwoLevel(result) {
      let numAll = getAllNum(result)
      let objArrTemp = {}
      let threeObjArrTemp = {}
      let twoResult = [];
      let ptemp = 0;
      for (let i = 0; i < result.length; i++) {
          let child = result[i];
          ptemp += parseFloat(child.value)
          twoResult.push(child);
          if (parseFloat((ptemp / numAll) * 100) >= 80) break;
      }
      twoResult.forEach((element) => {
          if (element.children && element.children.length) {
              let dataSource = []
              let temp = 0

              element.children.forEach(item => {
                  temp += parseFloat(item.value);
                  dataSource.push({
                      a1: item.name,
                      a2: item.value,
                      a3: ((parseFloat(item.value) / element.value) * 100).toFixed(2) + '%',
                      a4: (temp / parseFloat(element.value) * 100).toFixed(2) + '%',
                  })
              });
              objArrTemp[element.name] = dataSource


              let pptemp = 0
              let threeResult = []
              let numAlls = getAllNum(element.children)
              for (let i = 0; i < element.children.length; i++) {
                  let sChild = element.children[i];
                  pptemp += parseFloat(sChild.value)
                  threeResult.push(sChild);
                  if (parseFloat((pptemp / numAlls) * 100) >= 80) break;
              }

              // 去除3极柏拉图的PI名称跳到5why用
              let piCaseList = []
              threeResult.forEach((element1) => {
                  let dataSources = []
                  let templ = 0
                  if (element1.children && element1.children.length) {
                      element1.children.forEach((element2, index) => {
                          // 循环第三级添加到2极数据
                          templ += parseFloat(element2.value);
                          dataSources.push({
                              a1: element2.name,
                              a2: element2.value,
                              a3: ((parseFloat(element2.value) / element1.value) * 100).toFixed(2) + '%',
                              a4: (templ / parseFloat(element2.value) * 100).toFixed(2) + '%',
                          })
                          piCaseList.push({ name: element2.name, piCode: element2.id, problem: element2.name })
                      })
                      threeObjArrTemp[element1.name] = dataSources
                  }
              });

              nextTick(() => {
                  let str = JSON.stringify(piCaseList);
                  sessionStorage.setItem("piCaseList", str)
              })

              threeObjArr.value = threeObjArrTemp
              Object.keys(threeObjArrTemp).forEach((items, indexs) => {
                  let xArrs = [], yArrs = [], percents = []
                  threeObjArrTemp[items].forEach((element) => {
                      xArrs.push(element.a1)
                      yArrs.push(element.a2)
                      percents.push((1) * 100);
                  });
                  //三级柏拉图 图表
                  threeChart(xArrs, yArrs, percents, indexs, items)
              })

          }
      });
      objArr.value = objArrTemp
      Object.keys(objArrTemp).forEach((item, index) => {
          let xArr = [], yArr = [], percent = []
          objArrTemp[item].forEach((element) => {
              xArr.push(element.a1)
              yArr.push(element.a2)
              percent.push(element.a4);
          });
          //二级排列图(图表)
          twoChart(xArr, yArr, percent, index, item)
      })
  }

  //二级柏拉图(图标)
  function twoChart(x, y, percent, index, name) {
    let option = {
        //标题
        title: {
            text: name,
            left: 'center' //居中显示
        },

        tooltip: {
            trigger: 'axis',
            axisPointer: {
                type: 'cross',
                crossStyle: {
                    color: '#999'
                }
            }
        },
        grid: {
            borderWidth: '10',
            borderColor: '#eeeeee'
        },
        legend: {
            //selectedMode: false,
            //data:['水量','占比']
            data: ['', '']
        },
        xAxis: [
            {
                type: 'category',
                //显示横坐标中对应值的坐标线（竖线）：默认不显示
                //splitLine:{show:true},
                // data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
                data: x
            }
            , {
                type: 'category',
                show: false,
                boundaryGap: false,
                // data: ['', '1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
                data: [0, ...x]
            }
        ],
        yAxis: [
            {
                type: 'value',
                //纵坐标轴上的各个值对应的横线是否显示：默认显示
                splitLine: false,
                name: '损耗差值',
                min: 0,
                // max: 250,
                interval: 50,
                axisLabel: {
                    formatter: '{value} ml'
                }
            },
            {
                type: 'value',
                name: '占比',
                min: 0,
                max: 100,
                interval: 15,
                //纵坐标轴上的各个值对应的横线是否显示：默认显示
                splitLine: false,
                //splitArea : {show : true}, //保留网格区域
                axisLabel: {
                    formatter: '{value} %'
                }
            }
        ],
        series: [
            {
                name: '损耗差值',
                type: 'bar',
                //柱状图之间的空隙为0
                barCategoryGap: '0',

                //设置柱状图的颜色
                itemStyle: {
                    normal: {
                        color: function (params) {
                            var colorList = [
                                '#5e7e54', '#e44f2f', '#81b6b2', '#eba422', '#5e7e54',
                                '#e44f2f', '#81b6b2', '#eba422', '#5e7e54', '#e44f2f', '#2e7e04', '#e00f2f'
                            ];
                            return colorList[params.dataIndex]
                        },
                    }
                },
                data: y
            },
            {
                name: '占比',
                type: 'line',
                symbolSize: 10,
                //拐点显示设置（新版本EChart需要设置）
                symbol: function (value) {
                    if (value > 0) {//值大于0才显示
                        return 'circle'; //拐点类型
                    } else {
                        return 'none'; //拐点不显示
                    }
                },
                //显示设置
                itemStyle:
                {
                    normal: {
                        //color:'#f37b1d',
                        color: function (params) {
                            var colorList = [
                                '', '#f37b1d', '#f37b1d', '#f37b1d', '#f37b1d',
                                '#f37b1d', '#f37b1d', '#f37b1d', '#f37b1d',
                                'red', '#f37b1d', '#f37b1d', ''];
                            return colorList[params.dataIndex]
                        },
                        label:
                        {
                            show: true,
                            //格式化
                            formatter: '{c}%'
                        }
                    }
                },
                xAxisIndex: 1,
                yAxisIndex: 1,
                data: [0, ...percent]
            }
            ,

        ]
    };
    nextTick(() => {
        // let chart = this.$echarts.init(document.getElementById('aa' + index));
        // chart.setOption(option, true);
        // window.addEventListener("resize", chart.resize);
        twoCharOption.value[index] = option
    })
  }


  //三级柏拉图
  //三级柏拉图(图表)
  function threeChart(x, y, percent, index, name) {
      let option = {
          //标题
          title: {
              text: name,
              left: 'center' //居中显示
          },

          tooltip: {
              trigger: 'axis',
              axisPointer: {
                  type: 'cross',
                  crossStyle: {
                      color: '#999'
                  }
              }
          },
          grid: {
              borderWidth: '10',
              borderColor: '#eeeeee'
          },
          legend: {
              //selectedMode: false,
              //data:['水量','占比']
              data: ['', '']
          },
          xAxis: [
              {
                  type: 'category',
                  data: x
              }
              , {
                  type: 'category',
                  show: false,
                  boundaryGap: false,
                  data: [0, ...x]
              }
          ],
          yAxis: [
              {
                  type: 'value',
                  //纵坐标轴上的各个值对应的横线是否显示：默认显示
                  splitLine: false,
                  name: '损耗差值',
                  min: 0,
                  // max: 250,
                  interval: 50,
                  axisLabel: {
                      formatter: '{value} ml'
                  }
              },
              {
                  type: 'value',
                  name: '占比',
                  min: 0,
                  max: 100,
                  interval: 15,
                  //纵坐标轴上的各个值对应的横线是否显示：默认显示
                  splitLine: false,
                  //splitArea : {show : true}, //保留网格区域
                  axisLabel: {
                      formatter: '{value} %'
                  }
              }
          ],
          series: [
              {
                  name: '损耗差值',
                  type: 'bar',
                  //柱状图之间的空隙为0
                  barCategoryGap: '0',

                  //设置柱状图的颜色
                  itemStyle: {
                      normal: {
                          color: function (params) {
                              var colorList = [
                                  '#5e7e54', '#e44f2f', '#81b6b2', '#eba422', '#5e7e54',
                                  '#e44f2f', '#81b6b2', '#eba422', '#5e7e54', '#e44f2f', '#2e7e04', '#e00f2f'
                              ];
                              return colorList[params.dataIndex]
                          },
                          //color: '#5a9bd5',
                          //设置柱状图边框线颜色
                          //barBorderColor: 'black',
                          //设置柱状图边框线的显示宽度
                          //barBorderWidth:0.2
                      }
                  },
                  // data: [15, 10, 9, 8, 8, 8, 8, 8, 8, 7, 6, 5]
                  data: y
              },
              {
                  name: '占比',
                  type: 'line',
                  symbolSize: 10,
                  //拐点显示设置（新版本EChart需要设置）
                  symbol: function (value) {
                      if (value > 0) {//值大于0才显示
                          return 'circle'; //拐点类型
                      } else {
                          return 'none'; //拐点不显示
                      }
                  },
                  //显示设置
                  itemStyle:
                  {
                      normal: {
                          //color:'#f37b1d',
                          color: function (params) {
                              var colorList = [
                                  '', '#f37b1d', '#f37b1d', '#f37b1d', '#f37b1d',
                                  '#f37b1d', '#f37b1d', '#f37b1d', '#f37b1d',
                                  'red', '#f37b1d', '#f37b1d', ''];
                              return colorList[params.dataIndex]
                          },
                          label:
                          {
                              show: true,
                              //格式化
                              formatter: '{c}%'
                          }
                      }
                  },
                  xAxisIndex: 1,
                  yAxisIndex: 1,
                  //  data: [0, 13, 26, 32, 42, 50, 58, 66, 74, 82, 89, 95, 100]
                  data: [0, ...percent]
              }
              ,

          ]
      };
      nextTick(() => {
          // let chart = this.$echarts.init(document.getElementById('mm' + index));
          // chart.setOption(option, true);
          // window.addEventListener("resize", chart.resize);
          threeCharOption.value[index] = option
      })
  }

  function getAllNum(result) {
      let data = JSON.parse(JSON.stringify(result))
      let numAll = 0
      if (data.length > 1) {
          numAll = data.reduce((a, b) => {
              let type = Object.prototype.toString.call(a)
              if (type === "[object Object]") {
                  return parseFloat(a.value) + parseFloat(b.value) || 0;
              } else {
                  return parseFloat(a) + parseFloat(b.value) || 0;
              }
          })
          return numAll;
      } else if (data.length) {
          return data[0].value
      } else {
          return 0
      }
  }


  defineExpose({
    init,
  });
</script>

<style lang="less">
  /**隐藏样式-modal确定按钮 */
  .jee-hidden {
    display: none !important;
  }

  .t1 {
    width: 100%;
    height: 40px;
    background: #fbfbfb;
    font-size: 16px;
    font-weight: bold;
    text-align: center;
    line-height: 40px;
    //border: 1px solid #e8e8e8
}

.t2 {
    .t1();
    font-weight: 400;
}

.title {
  width: 100%;
  background: #fbfbfb;
  line-height: 30px;
  text-align: center;
  font-weight: bold;
}
</style>
<style lang="less" scoped></style>
