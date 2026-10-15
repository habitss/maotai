<template>
  <div style="width: 100%;height:100%;">
    <!-- <a-card v-for="(tag,index) in deviceArr" :key="index" :bordered="false" style="width: 100%;height: 49%;margin-bottom: 7px;height:385px;"> -->
    <a-card v-for="(tag,index) in deviceArr" :key="index" :bordered="false" style="width: 100%;height: 49%;margin-bottom: 7px;height:450px;">
      <template #title>
        <div style="display: flex;justify-content: space-between;">
          <div style="display: flex; align-items: center; gap: 8px;">
            <span>{{ tag.unitName }}</span>
            <a-select class="point-multi-select" show-search option-filter-prop="label" v-model:value="chartForm[tag.unitNo]"
            :options="filteredPoints(tag.points)" placeholder="请选择" allowClear @change="chart(tag.unitNo, tag.points, tag.deviceCode)" 
            :field-names="{ label: 'pointName', value: 'pointCode'}" style="width: 260px;" mode="multiple" :maxTagCount="1"></a-select>
          </div>
          <div>
            <!-- <AlignLeftOutlined /> -->
            <a-popover title="工艺步骤" trigger="click"  placement="leftTop" v-if="stepOptions">
              <template #content>
                <div class="step-scroll" style="width: 500px;">
                  <a-checkbox-group v-model:value="stepState[tag.deviceCode]" @change="e=>onChange(e,tag.deviceCode,tag.unitNo,tag.points)">
                    <a-row :gutter="[12, 8]">
                      <a-col
                        :span="stepOptions[tag.deviceCode].length>1?12:24"
                        v-for="(item, index) in stepOptions[tag.deviceCode]"
                        :key="item.id"
                      >
                      <label>
                        <div class="step-card" :title="stepTitle(item)">
                          <a-checkbox :value="item.id" :disabled="isDisabled(index, tag.deviceCode)">
                            {{ item.eopName }}
                          </a-checkbox>
                        </div>
                      </label>
                      </a-col>
                    </a-row>
                  </a-checkbox-group>
                  <div style="text-align:center;margin-top: 10px;" v-if="stepOptions">
                    <a-button type="primary" @click="onSave(tag.deviceCode)" size="small" v-auth="'mes:pi_chart_display:save'">保存</a-button>
                  </div>
                </div>
              </template>
              <!-- <a-button type="primary">Click me</a-button> -->
              <AlignLeftOutlined title="工艺步骤"/>
            </a-popover>
          </div>
        </div>
      </template>
      <div :ref="el => chartRefs[tag.unitNo] = el" style="width:100%;height:300px;"></div>
      <div :ref="el => chartStepRefs[tag.deviceCode] = el" style="width:100%;height:60px;"></div>
    </a-card>
  </div>
</template>

<script lang="ts" name="pi-piBasePoint" setup>
  import { ref, reactive, onMounted, unref , nextTick} from 'vue';
  import { getDepartId } from '/@/utils/ktTool';
  import { useUserStore } from '/@/store/modules/user';
  import * as echarts from 'echarts';
  import { useI18n } from '/@/hooks/web/useI18n';
   import { AlignLeftOutlined } from '@ant-design/icons-vue';
  const emit = defineEmits(['chart-change','step-save','step-switch'])
  const queryParam = ref<any>({});
  const userStore = useUserStore();
  const dataOption = ref([] as any);
  const recipeArr = ref([] as any);
  const rtBatchArr = ref([] as any);
  const deviceArr = ref([{unitName:"--",unitNo:'charts',id:'1',points:[]}]);
  const chartRefs = ref<Record<number, HTMLElement>>({})
  const chartStepRefs = ref<Record<number, HTMLElement>>({})
  const devicePointObj = ref({});
  const stepState = ref({});
  const disableArray = ref({})
  const stepOptions = ref({})
  const stepBlockOptions = ref({})
  const pointArray = ref([])
  //是否发酵
  const isFermentation = ref(false); 
  //步骤配置
  const configSteps = ref({})
  const optionData = ref({
    name: "-",
    type: 'line',
    smooth: true,
    data: [0],
  })
  const xAxisData = ref([1,7,13,19,25,31,37])

  const form = ref({
    startDate: null,
    endDate: null,
    beerType: null,
    recipe: null,
    batchNos: [],
  });
  const chartForm = ref({});
  const checkAll = ref(false);
  const invertChecked = ref(false);
  const allBatchNos = ref([]);
  const allBatchNoTime = ref({});

  const exportParams = reactive({
    departId: '1',
    deviceId: queryParam?.value?.deviceId,
  });

  const handleChange = (tag: string, checked: boolean) => {
    console.log(tag, checked);
  };

  const batchTime = ref({
    startTime: '',
    endTime: '',
  });


  onMounted(() => {
    exportParams.departId = getDepartId(userStore?.getUserInfo?.orgCode);
  });

  //选择点位
  async function chart(unitNo, pointArray, deviceCode){
    emit('chart-change',  unitNo, pointArray, chartForm.value, deviceCode)
  }
  // 获取点位名称
  // function getNameByKey(poointts, pointStr) {
  //   let pointArr = pointStr.split("-")
  //   let pointCode = pointArr[2]
  //   const item = poointts.find(item => item.pointCode === pointCode);
  //   return item ? item.pointName+"-"+pointArr[0]+"-"+pointArr[1] : null;
  // }

  //图表初始化
  function initEcharts(ref, data, xData, unitObj, timeUnit = '', deviceCode='') {
    //设置默认
    if(!data || data.length == 0){
      data = [optionData.value]
    }
    if(!xData || xData.length == 0){
      xData = xAxisData.value
    }
    if(timeUnit){
      timeUnit = `（${timeUnit}）`
    }
    //数据过滤
    // 配置图表
    var option = {
      tooltip: { 
        trigger: 'axis',
        axisPointer: {
          type: "line"
        },
        backgroundColor: 'rgba(255, 255, 255, 0.9)', // 白底
        borderColor: '#ccc',
        borderWidth: 1,
        textStyle: { color: '#333' },
        formatter: function (params) {
          // params 是一个数组，包含同一 x 轴上所有 series 的数据点
          if (!params || params.length === 0) return '';

          const time = params[0].axisValueLabel; // X轴时间
          let html = `<div style="font-size:13px; padding:4px 8px;">
            <div>${time}${timeUnit}</div>
            <div style="margin-top:4px;"></div>
          `;

          params.forEach(item => {
            if (item.value == null) return; // value 为空时跳过
            const color = item.color; // 当前 series 的颜色
            const name = item.seriesName;
            const value = Array.isArray(item.value) ? (item.value[1] ?? '') : item.value; // 兼容 [x, y] 结构和直接数值
            
            // 保留两位小数
            let displayValue = value;
            if (typeof value === 'number') {
              if (Number.isInteger(value)) {
                displayValue = value; // 整数直接显示
              } else {
                displayValue = value.toFixed(2); // 小数保留两位
              }
            }
            //获取单位
            let unit = "";
            if(name && unitObj){
              // 查找包含关键字的单位
              const matchedKey = Object.keys(unitObj).find(key => name.includes(key));
              if(matchedKey){
                unit = unitObj[matchedKey];
              }
            }
            if(!unit || unit == undefined){
              unit = '';
            }
            html += `
              <div style="display:flex; align-items:center; margin-top:2px;">
                <span style="
                  display:inline-block;
                  width:8px;
                  height:8px;
                  border-radius:50%;
                  background:${color};
                  margin-right:6px;
                "></span>
                <span style="flex:1;">${name}</span>
                <span style="margin-left:10px;">${displayValue} ${unit}</span>
              </div>
            `;
          });

          html += `</div>`;
          return html;
        }
      },
      grid: {
        "top": "2%",
        "left": 10,
        "right": 10,
        "bottom": "3%",
        "containLabel": true
      },
      xAxis: {
        type: 'category',
        splitLine: { show: false },
        data: xData,
        boundaryGap: false,  // 让坐标轴从第一个刻度开始
        //name: '时',
      },
      // yAxis: { type: 'value' },
      yAxis: [
        {
          type: 'value',
          // name: '左侧Y轴',
          scale: true,
          min: function (val) {
            // 向下取整
            return Math.floor(Number(val.min));
          },
          max: function (val) {
            // 向上取整
            return Math.ceil(Number(val.max));
          },
          position: 'left',
          axisLine: {
            show: true,
            lineStyle: {
              color: '#000'
            }
          },
          axisLabel: {
            formatter: '{value}'
          },
          axisTick: {
            show: true, // 显示主刻度线
            lineStyle: {
              color: '#333', // 主刻度线颜色（深）
              width: 1.5    // 主刻度线宽度（粗）
            },
          },
          // 显示小刻度线
          minorTick: {
            show: true,
            splitNumber: 5,   // 每两个主刻度之间分5个小刻度
            length: 3         // 小刻度线长度
          },

        },
        {
          type: 'value',
          // name: '右侧Y轴',
          min: function (val) {
            // 向下取整
            return Math.floor(Number(val.min));
          },
          max: function (val) {
            // 向上取整
            return Math.ceil(Number(val.max));
          },
          position: 'right',
          axisLine: {
            show: true,
            lineStyle: {
              color: '#000'
            }
          },
          axisLabel: {
            formatter: '{value}'
          },
          splitLine: { show: false },  // 右侧Y轴不显示网格线

          axisTick: {
            show: true, // 显示主刻度线
            lineStyle: {
              color: '#333', // 主刻度线颜色（深）
              width: 1.5    // 主刻度线宽度（粗）
            }
          },
          // 显示小刻度线
          minorTick: {
            show: true,
            splitNumber: 5,   // 每两个主刻度之间分5个小刻度
            length: 3         // 小刻度线长度
          },
        }
      ],
      series: data
    };
    dataOption.value = option;
    var myChart = echarts.init(chartRefs.value[ref]);
    myChart.setOption(option,true);

    //步骤
    stepEcharts(deviceCode, ref)
  }
  function stepEcharts(deviceCode, unitNo?) {
    //遍历 stepOptions.value 得到
    let result = []
    //var stepOptions = stepOptions.value;
    // if(stepOptions.value?.[deviceCode]?.length > 0){
    //   result = stepOptions.value[deviceCode].map((item, index) => [
    //     item.startTime,
    //     item.endTime,
    //     index,
    //     item.eopName?.trim() || ''
    //   ])
    // }
    if (stepOptions.value?.[deviceCode]?.length > 0) {
      //const stepIds = configSteps.value[deviceCode]; // 需要匹配的步骤字符串数组
      const stepIds = stepState.value[deviceCode]
      // 先过滤出符合 step 条件的步骤
      const filteredSteps = stepBlockOptions.value[deviceCode].filter(item => stepIds.includes(item.id));
      // if (filteredSteps.length > 0) {
      //   // 找到最早的开始时间作为基准
      //   const baseTime = Math.min(...filteredSteps.map(item => new Date(item.startTime).getTime()));
      //   result = filteredSteps.map((item, index) => {
      //     const start = new Date(item.startTime).getTime();
      //     const end = new Date(item.endTime).getTime();
      //     const startDiff = Math.round((start - baseTime) / 60000); // 转为分钟
      //     const endDiff = Math.round((end - baseTime) / 60000);     // 转为分钟
      //     const durationObj = getTimeBetween(start,end);                      // 时长分钟
      //     return [startDiff, endDiff, durationObj.display, item.eopName?.trim() || ''];
      //   });
      // }

      if (filteredSteps.length > 0) {
        // 1 时间单位切换
        const unitDivisor = isFermentation.value ? 3600000 : 60000; // 小时 / 分钟

        // 2 找到最早开始时间
        const baseTime = Math.min(
          ...filteredSteps.map(item => new Date(item.startTime).getTime())
        );

        result = filteredSteps.map(item => {
          const start = new Date(item.startTime).getTime();
          const end   = new Date(item.endTime).getTime();

          // 3 时间差（分钟 or 小时），x轴从1开始
          const startDiff = Math.round((start - baseTime) / unitDivisor) + 1;
          const endDiff   = Math.round((end - baseTime) / unitDivisor) + 1;

          // 4 显示用时长
          const durationObj = getTimeBetween(start, end,isFermentation.value);

          return [
            startDiff,
            endDiff,
            durationObj.display,
            item.eopName?.trim() || ''
          ];
        });
      }
    }
    

    // if (stepChart.value) {
    //   stepChart.value.dispose();
    // }
    // 配置图表
    var option = {
      grid: {
        top: 0,
        bottom: 10,
        left: 56,
        right: 50,
        containLabel: false
      },
      tooltip: {
        trigger: 'item',
        backgroundColor: 'rgba(0,0,0,0.75)',
        borderWidth: 0,
        textStyle: { color: '#fff', fontSize: 12 },
        formatter: function (params) {
          const [start, end, value , stepName] = params.value;
          return `
            <div>
              <b>${stepName}</b><br/>
              开始：${start}<br/>
              结束：${end}<br/>
              时长：${value}
            </div>
          `;
        }
      },
      xAxis: {
        type: 'value', //改成数值轴
        axisLine: { show: false },
        axisTick: { show: false },
        splitLine: { show: false },
        axisLabel: {
          color: '#ccc',
          fontSize: 11,
          // formatter: function (val) {
          //   const d = new Date(val);
          //   return `${d.getHours()}:${d.getMinutes().toString().padStart(2, '0')}`;
          // }
        }
      },
      // dataZoom: [
      //   {
      //     type: 'inside',
      //     xAxisIndex: 0
      //   }
      // ],
      yAxis: {
        show: false,
        type: 'category',
        data: ['步骤'],
        axisLine: { show: false },
        axisTick: { show: false },
        axisLabel: {
          color: '#ccc',
          fontSize: 12,
          margin: 10
        }
      },
      series: [
        {
          type: 'custom',
          renderItem: function (params, api) {
            const categoryIndex = api.value(2);
            const start = api.coord([api.value(0), categoryIndex]);
            const end = api.coord([api.value(1), categoryIndex]);
            const barHeight = api.size([0, 1])[1] * 0.6; // 压缩高度，让上下更紧凑
            const rectShape = echarts.graphic.clipRectByRect(
              {
                x: start[0],
                y: start[1] - barHeight / 2,
                width: end[0] - start[0],
                height: barHeight
              },
              {
                x: params.coordSys.x,
                y: params.coordSys.y,
                width: params.coordSys.width,
                height: params.coordSys.height
              }
            );

            return (
              rectShape && {
                type: 'rect',
                shape: rectShape,
                style: api.style({
                  fill: api.visual('color')
                }),
                textContent: {
                  style: {
                    text: api.value(3),
                    fill: '#fff',
                    fontSize: 11,
                    align: 'center',
                    verticalAlign: 'middle'
                  }
                },
                textConfig: {
                  position: 'inside'
                }
              }
            );
          },
          encode: { x: [0, 1], y: 2 },
          data: result,
          itemStyle: {
            color: function (params) {
              const colors = [
                '#5B8FF9',  // 蓝色
                '#F4664A',  // 红色/珊瑚色
                '#8C8C8C',  // 中性灰
                '#FAD337',  // 黄色
                '#5AD8A6',  // 青绿色
                '#91CC75',  // 黄绿色
                '#A75CFF',  // 新增：紫色
                '#FF7CDB',  // 新增：粉色
                '#FF9D4D',  // 新增：橙色
                '#4A9BFF',  // 新增：天蓝色
                '#1A8E5F',  // 新增：深绿色
                '#BD6F26'   // 新增：棕色
              ];
              return colors[params.dataIndex % colors.length];
            }
          }
        }
      ]
    };

    if (result.length > 0) {
      const lastEnd = Math.max(...result.map(r => r[1])); // 获取最后一个步骤的结束分钟
      option.xAxis.min = 1;
      option.xAxis.max = lastEnd; // 设置 x 轴最大值，保证最后一个柱子贴右
      option.xAxis.axisLabel.formatter = val => `${val}`; // 可选，显示分钟
    }
    //stepChart.value = echarts.init(stepChartRef.value);
    //stepChart.value.setOption(option, true);

    //dataOption.value = option;
    if(deviceCode){
      // 从折线图实例读取实际 grid 像素边界，实现视觉对齐
      const lineChartDom = unitNo ? chartRefs.value[unitNo] : chartRefs.value[deviceCode];
      const lineChartInstance = lineChartDom ? echarts.getInstanceByDom(lineChartDom) : null;
      if (lineChartInstance) {
        const lineGrid = (lineChartInstance as any).getModel().getComponent('grid', 0);
        if (lineGrid) {
          const gridRect = lineGrid.coordinateSystem.getRect();
          const containerWidth = chartStepRefs.value[deviceCode].clientWidth;
          const rightPx = containerWidth - gridRect.x - gridRect.width;
          option.grid = {
            top: 0,
            bottom: 10,
            left: gridRect.x,
            right: Math.max(rightPx, 0),
            containLabel: false
          };
        }
      }
      var myChart = echarts.init(chartStepRefs.value[deviceCode]);
      myChart.setOption(option,true);
    }
  }

  /**
   * 设置参数
   * @param {Object} datas 设备点位集合
   * @param {Array} unitStep 设备步骤集合
   * @param {Object} configStep 配置的步骤集合
   * @param {Boolean} isFerm 是否是发酵
  */
  function setDevice(datas, unitStep = [], configStep={},standardStepListMap = {}, isFerm = false){
    //设备步骤集合
    // 右侧勾选
    stepOptions.value = unitStep;
    // 步骤色块
    stepBlockOptions.value = standardStepListMap;
    configSteps.value = configStep;
    isFermentation.value = isFerm;
    //配置的步骤集合
    //const configSteps = configSteps
    //设备点位集合
    deviceArr.value = datas;

   disableArray.value = {};
   //步骤判断是否被选中 {设备编码:[点位id1,点位id2]}
   for (const key in unitStep) {
      if (unitStep.hasOwnProperty(key)) {
        //disableArray.value[key] = unitStep[key].map(item => item.id);
        //禁用
        let array = [];
        //选中的步骤
        let stepStateArray = [];
        //步骤第一个配置项目
        let stepRecord = false;
        //标记
        let stepRecordTg = false;
        let stepEndRecord = false;

        for(let i = 0; i < unitStep[key].length; i++){

          let isNotDisable = false;
          if(configStep[key]){
            if(configStep[key].includes(unitStep[key][i].id)){
              stepStateArray.push(unitStep[key][i].id);
              if(!stepRecord){
                stepRecordTg = true;
                stepRecord = true;
              }
            }else{
              isNotDisable = true;
              if(stepRecord){
                stepEndRecord = true;
                stepRecordTg = true;
                //最后一个选中点不禁用
                array[i-1] = false;
              }
            }
          }else{
            stepStateArray.push(unitStep[key][i].id);
            
          }
          if(i === 0 || i === unitStep[key].length-1 || isNotDisable || (stepRecord && stepRecordTg) || stepEndRecord){
            stepRecordTg = false;
            array.push(false);
          }else{
            array.push(true);
          }
        }
        disableArray.value[key] = array;
        stepState.value[key] = stepStateArray;
      }
   }
    
    //设置默认值
    datas.forEach(unit => {
      if (unit.points && unit.points.length > 0 && (!chartForm.value[unit.unitNo] || chartForm.value[unit.unitNo].length > 0)) {
          // 获取第一个点位的 pointCode
          const firstPointCode = unit.points[0].pointCode;
          // 设置到 chartForm 中，key 为 unitNo
          chartForm.value[unit.unitNo] = [firstPointCode];
          setTimeout(() => {
            chart(unit.unitNo, unit.points, unit.deviceCode)
          },400)
      }
    });
  }
  function filteredPoints(points){
    //return points.filter(point => point.dataAttribute !== '1');
    return points;
  }

  //步骤显示时间
  function stepTitle(item = {}) {
    const label = item.eopName ?? '';
    const startTime = item.startTime ?? '--';
    const endTime = item.endTime ?? '--';
    let duration = '--'
    if(item.startTime || item.endTime){
      duration = getTimeBetween(startTime, endTime, isFermentation.value).display
    }
    return `${label} (${duration})`;
  }

   /**
 * 计算时间差，返回秒 / 分钟 / 小时
 * @param {string|Date} startTime
 * @param {string|Date} endTime
 * @param {boolean} isFermentation - 是否发酵阶段（小时）
 * @returns {Object}
 */
function getTimeBetween(startTime, endTime, isFermentation = false) {
  const start = new Date(startTime);
  const end   = new Date(endTime);

  if (isNaN(start.getTime()) || isNaN(end.getTime())) {
    return { value: 0, unit: 'unknown', display: '--' };
    // throw new Error('Invalid date provided');
  }

  const diffMs = Math.abs(end - start);

  // 发酵阶段 → 小时
  if (isFermentation) {
    const hours = diffMs / 3600000;
    return {
      value: Number(hours.toFixed(1)),
      unit: 'hours',
      display: `${hours.toFixed(1)}小时`
    };
  }

  const diffMinutes = Math.floor(diffMs / 60000);

  // 非发酵：小于 1 分钟 → 秒
  if (diffMinutes === 0) {
    const seconds = Math.floor(diffMs / 1000);
    return {
      value: seconds,
      unit: 'seconds',
      display: `${seconds}秒`
    };
  }

  return {
    value: diffMinutes,
    unit: 'minutes',
    display: `${diffMinutes}分钟`
  };
}

  //判断每个 checkbox 是否禁用
  function isDisabled(index, deviceCode) {
    return disableArray.value[deviceCode][index] === true;
  }
  //步骤操作
  function onChange(newValues, deviceCode,unitNo,points){
    //开头
    let options = stepOptions.value[deviceCode]
    for (let i = 0; i < options.length; i++) {
      const id = options[i].id
       // 判断 id 是否在 newValues 中
      if (newValues.includes(id)) {
        disableArray.value[deviceCode][i+1] = true;
        setTopSelected(i,deviceCode);
        break;
      }else{
        disableArray.value[deviceCode][i+1] = false;
      }
    }
    //结尾
    for (let i = (options.length-1); i > 0; i--) {
      const id = options[i].id
       // 判断 id 是否在 newValues 中
      if (newValues.includes(id)) {
        disableArray.value[deviceCode][i-1] = true;
        setTailSelected(i,deviceCode);
        break;
      }else{
        disableArray.value[deviceCode][i-1] = false;
      }
    }
    //调用查询
    //步骤开头与结尾不能禁止
    disableArray.value[deviceCode][0] = false;
    disableArray.value[deviceCode][stepOptions.value[deviceCode].length-1] = false;
    //更新图表
    emit('step-switch', unitNo,points,chartForm.value, deviceCode)
    //更新步骤
    stepEcharts(deviceCode, unitNo);
  }
  
  function setTopSelected(val,deviceCode){
    for (let i = val+1; i < stepOptions.value[deviceCode].length-1; i++) {
      const optionId = stepOptions.value[deviceCode][i].id;
       // 判断id是否已存在于stepState.value中
      if (!stepState.value[deviceCode].includes(optionId)) {
        disableArray.value[deviceCode][i+1] = true;
        stepState.value[deviceCode].push(optionId);
      }else{
        disableArray.value[deviceCode][i+1] = true;
        break;
      }
    }
  }

  function setTailSelected(val,deviceCode){
    for (let i = val-1; i > 0 ; i--) {
      const optionId = stepOptions.value[deviceCode][i].id;
       // 判断id是否已存在于stepState.value中
      if (!stepState.value[deviceCode].includes(optionId)) {
        disableArray.value[deviceCode][i-1] = true;
        stepState.value[deviceCode].push(optionId);
      }else{
        disableArray.value[deviceCode][i-1] = true;
        break;
      }
    }
  }
  //获取排除的步骤
  function getExcludeIds(deviceCode, type) {
    if(type === 2){
      //RA
      return stepOptions.value[deviceCode].filter(option => !stepState.value[deviceCode].includes(option.id))
     .map(option => option.stepNr);
    }else{
      //Adapter
      return stepOptions.value[deviceCode].filter(option => !stepState.value[deviceCode].includes(option.id))
     .map(option => Number(option.ropId));
    }
    
  }

  //保存工艺步骤配置
  function onSave(deviceCode){
    // 选中步骤集合
    const excludeIds = stepOptions.value[deviceCode].filter(option => !stepState.value[deviceCode].includes(option.id))
    .map(option => option.id);
    emit('step-save', stepState.value[deviceCode], excludeIds, deviceCode)
  }
   // 暴露方法
  defineExpose({
    initEcharts,
    setDevice,
    getExcludeIds
  });
</script>

<style lang="less" scoped>
.jeecg-basic-table-form-container {
  .table-page-search-submitButtons {
    display: block;
    margin-bottom: 24px;
    white-space: nowrap;
  }
  .query-group-cust {
    width: calc(50% - 15px);
    min-width: 100px !important;
  }
  .query-group-split-cust {
    width: 30px;
    display: inline-block;
    text-align: center;
  }
  .ant-form {
    padding: 12px 10px 6px 10px;
    margin-bottom: 8px;
    background-color: #ffffff;
    border-radius: 2px;
    margin: 5px 5px;
  }
}

.my-descriptions .ant-descriptions-item-label,
.my-descriptions .ant-descriptions-item-content {
  padding: 16px 10px !important;  /* 重写 padding */
}

:deep(.compact-descriptions .ant-descriptions-item-label) {
  padding: 6px 8px;
  white-space: nowrap;
}
.div-border {
  border: 1px solid #C1C0B6;   /* 1px 实线 灰色边框 */
}

:deep(.point-multi-select .ant-select-selector) {
  overflow: hidden;
}

:deep(.point-multi-select .ant-select-selection-overflow) {
  flex-wrap: nowrap !important;
}

:deep(.point-multi-select .ant-select-selection-overflow-item),
:deep(.point-multi-select .ant-select-selection-item) {
  max-width: 100%;
}
</style>
