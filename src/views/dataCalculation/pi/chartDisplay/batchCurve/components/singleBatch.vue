<template>
    <div style="display: flex;background-color: #ffffff;height: 80vh;">
        <div class="batch-panel">
          <div class="step-title">批次信息</div>
          <a-descriptions :column="1" bordered class="my-descriptions">
              <a-descriptions-item label="订单号:">{{ queryParam.order }}</a-descriptions-item>
              <a-descriptions-item label="批次号:">{{ batchHandle(queryParam.batch) }}</a-descriptions-item>
              <a-descriptions-item label="配方名称:">{{ information.formulaName && information.formulaName !== 'null' ? information.formulaName : '--' }}</a-descriptions-item>

              <a-descriptions-item label="工艺浓度:" v-if="information.concentration">
              <a-tag v-if="information.concentration" color="orange">
                  {{ information.concentration }}
              </a-tag>
              <span v-else>-</span>
              </a-descriptions-item>
              <a-descriptions-item label="冷麦汁产量:" v-if="information.output">
              <a-tag v-if="information.output" color="#108ee9">
                  {{ information.output }}
              </a-tag>
              <span v-else>-</span>
              </a-descriptions-item>


              <a-descriptions-item label="单元开始时间:">{{ information.startTime }}</a-descriptions-item>
              <a-descriptions-item label="单元结束时间:">{{ information.endTime }}</a-descriptions-item>
          </a-descriptions>
           <!-- 工艺步骤 -->
          <div class="step-section" v-if="stepOptions.length > 0">
          <!-- <div class="step-section" v-if="false"> -->
            <div class="step-title">
              <span>工艺步骤</span>
              <div><a-button type="primary" @click="onSave" size="small" v-auth="'mes:pi_chart_display:save'">保存</a-button></div>
            </div>
            <div class="step-scroll">
              <a-checkbox-group v-model:value="stepState" @change="onChange">
                <a-row :gutter="[12, 8]">
                  <a-col
                    :span="stepOptions.length>1?12:24"
                    v-for="(item, index) in stepOptions"
                    :key="item.value"
                  >
                    <label>
                      <div class="step-card" :title="stepTitle(item)">
                        <a-checkbox :value="item.value" :disabled="isDisabled(index)">
                          {{ item.label && item.label !== 'null' ? item.label : '--' }}
                        </a-checkbox>
                      </div>
                    </label>
                  </a-col>
                </a-row>
              </a-checkbox-group>
            </div>
          </div>

        </div>
        <div style="width: 79%;padding: 3px;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;width: 100%;">
            <div style="font-size: 18px;color:#0798CA">{{ deviceName?deviceName+"-":"" }}参数趋势</div>
            <div style="display: flex; align-items: center;">
            <a-button-group>
                <a-button
                :type="segmentedObj.viewMode === 'simple' ? 'primary' : 'default'"
                @click="handleViewChange('simple')"
                >
                简易版
                </a-button>
                <a-button
                :type="segmentedObj.viewMode === 'details' ? 'primary' : 'default'"
                @click="handleViewChange('details')"
                >
                详情版
                </a-button>
            </a-button-group>
            </div>
        </div>
        <div style="width: 100%; padding: 3px; display: flex; flex-direction: column; min-height: 0;height: 96%;">
          <div ref="topAreaRef" class="tag-point">
              <a-space :size="[0, 8]" wrap>
              <a-checkable-tag v-for="(tag, index) in tagsData" :key="tag" v-model:checked="selectTags[index]" @change="checked => handleChange(tag, checked)" style="height: 25px;line-height: 25px;">
                  {{ tag.pointName }}
              </a-checkable-tag>
              </a-space>
          </div>
          <div style="flex: 1; min-height: 0;">
              <!-- <div ref="chartRef" :style="{ height: chartHeight + 'px', width: '100%' }"></div> -->
              <div ref="chartRef" style="height: 100%; width: 100%;"></div>
          </div>
          <div style="width: 100%;">
              <div ref="stepChartRef" :style="{height :'52px', width: '100%' }"></div>
          </div>
        </div>
      </div>
    </div>

</template>

<script lang="ts" name="pi-piBasePoint" setup>
  import { ref, reactive, onMounted, unref, nextTick } from 'vue';

  import { getDepartId } from '/@/utils/ktTool';
  import { useUserStore } from '/@/store/modules/user';
  import * as echarts from 'echarts';
  import type { EChartsType } from 'echarts';
  const emit = defineEmits(['handle-change','step-save','step-switch'])
  const queryParam = ref<any>({});
  const userStore = useUserStore();
  const chartRef = ref();
  const stepChartRef = ref();
  const dataOption = ref({});
  const pointArray = ref([] as any)
  const curveDataArray = ref([] as any)
  const deviceName = ref("")
  const segmentedObj = ref({viewMode:'simple'})
  const chartHeight = ref(598);
  const unitObj = ref({});
  const steps = ref([]);
  const stepState = ref([])
  const stepOptions = ref([]);
  const lineEcharts = ref(null)
  const axisTimeRange = ref({ startTime: undefined as string | number | undefined, endTime: undefined as string | number | undefined })
  const sharedGrid = {
    left: 56,
    right: 24
  };
  
  // 图表实例引用
  const lineChart = ref<EChartsType | null>(null);
  const stepChart = ref<EChartsType | null>(null);

  // 经用标记
  const disableArray = ref([])  

  const exportParams = reactive({
    departId: '1',
    deviceId: queryParam?.value?.deviceId,
  });

  const information = ref({
    order: '-',
    batch: '-',
    variety: '-',
    formulaName: '-',
    concentration: '',
    output: '',
    startTime: '-',
    endTime: '-'
  });
  const tooltipFormatter = function (params) {
    if (!params || params.length === 0) return '';
    const time = params[0].axisValueLabel;
    let html = `<div style="font-size:13px; padding:4px 8px;">
      <div><strong>时间：</strong>${time}</div>
      <div style="margin-top:4px;"><strong>参数数据：</strong></div>
    `;
    params.forEach(item => {
      const color = item.color;
      const name = item.seriesName;
      const value = item.value[1] ?? item.value;
      const unit = unitObj.value?.[name] || '';

      let displayValue = value;
      if (typeof value === 'number') {
        displayValue = Number.isInteger(value) ? value : value.toFixed(2);
      }

      html += `
        <div style="display:flex; align-items:center; margin-top:2px;">
          <span style="
            width:8px;height:8px;border-radius:50%;
            background:${color};margin-right:6px;
          "></span>
          <span style="flex:1;">${name}</span>
          <span style="margin-left:10px;">${displayValue} ${unit}</span>
        </div>
      `;
    });

    return html + `</div>`;
  };
  const tagsData = ref([]);
  const selectTags = reactive([]);
  //选择点位
  const handleChange = (tag: string, checked: boolean) => {
    pointArray.value = [];
    for(let i = 0; i < selectTags.length; i++){
      if(selectTags[i]){
        pointArray.value.push(tagsData.value[i].pointCode);
      }
    }
    emit('handle-change',  pointArray.value)
  };

  onMounted(() => {
    exportParams.departId = getDepartId(userStore?.getUserInfo?.orgCode);
    //chartHeight.value = window.innerHeight - 331;
  });



  async function initEcharts(datas) {
    chartHeight.value = window.innerHeight - 331;
    setTimeout(() => {
      initLineEcharts(datas);
    },200)
  }
  //曲线
  function initLineEcharts(datas) {
    if(!datas){
      console.log(datas)
      return false;
    }
    curveDataArray.value = datas;
    // 在函数内部保存 unitObj.value 的引用
    //const currentUnitObj = unitObj.value || {};

    if (lineEcharts.value) {
      try {
        if (!lineEcharts.value.isDisposed?.()){
          lineEcharts.value?.dispose();
        }
      } catch (error) {
         console.warn('Dispose error:', error);
      }
      lineEcharts.value = null;
    }
    
    //数据过滤
    // 配置图表
    const axisBoundary = getAxisBoundary();
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
        formatter: tooltipFormatter
      },
      grid: {
        "top": "20px",
        "left": sharedGrid.left,
        "right": sharedGrid.right,
        "bottom": "50px",
        "containLabel": false
      },
      //鼠标滚动
      dataZoom: [
        {
          type: 'inside',
          xAxisIndex: 0,
          zoomOnMouseWheel: true,
          moveOnMouseMove: true,
        },
        {
          type: 'slider',
          xAxisIndex: 0,
          height: 20,
          bottom: 6,
          showDetail: false
        }
      ],
      xAxis: {
        type: 'time',
        min: axisBoundary.min,
        max: axisBoundary.max,
        splitLine: { show: false },
        boundaryGap: false,  // 让坐标轴从第一个刻度开始
        axisLabel: {
          showMinLabel: true, // 尝试强制显示最小标签
          formatter: function(value) {
            // 使用Date对象格式化
            const date = new Date(value);
            const month = date.getMonth() + 1;
            const day = date.getDate();
            const hours = date.getHours().toString().padStart(2, '0');
            const minutes = date.getMinutes().toString().padStart(2, '0');
            return `${month}/${day} ${hours}:${minutes}`;
          }
        }
      },
      yAxis: { 
        type: 'value',
        axisTick: { show: true }, // 刻度线
        axisLabel: { show: true }, // 刻度文字
        axisLine: { show: true },  // 显示轴线
        // 显示小刻度线
        minorTick: {
          show: true,
          splitNumber: 5,   // 每两个主刻度之间分5个小刻度
          length: 3         // 小刻度线长度
        },
      },
      series: datas
    };
    dataOption.value = option;
    var myChart = echarts.init(chartRef.value);
    myChart.setOption(option,true);
    lineEcharts.value = myChart;

    // lineChart.value = echarts.init(chartRef.value);
    // lineChart.value.setOption(option, true);
  }

  function initStepEcharts(datas){
     setTimeout(() => {
      stepEcharts(datas);
    },300)
  }
  //步骤
  function stepEcharts(datas) {
    if (stepChart.value) {
      stepChart.value.dispose();
    }
    // 配置图表
    const axisBoundary = getAxisBoundary();
    var option = {
      grid: {
        top: 0,
        bottom: 2,
        left: sharedGrid.left,
        right: sharedGrid.right,
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
        type: 'time',
        min: axisBoundary.min,
        max: axisBoundary.max,
        boundaryGap: false,
        axisLine: { show: false },
        axisTick: { show: false },
        splitLine: { show: false },
        axisLabel: {
          color: '#ccc',
          fontSize: 11,
          formatter: function (val) {
            const d = new Date(val);
            return `${d.getHours()}:${d.getMinutes().toString().padStart(2, '0')}`;
          }
        }
      },
      dataZoom: [
        {
          type: 'inside',
          xAxisIndex: 0
        }
      ],
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
            const barHeight = api.size([0, 1])[1] * 0.55; // 进一步压低步骤色块高度，减少遮挡感
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
          data: datas,
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
    stepChart.value = echarts.init(stepChartRef.value);
    stepChart.value.setOption(option, true);

    stepChart.value.off('click');
    stepChart.value.on('click', function(params) {
      handleClick(params);
    });
    
    // var myChartParam = echarts.init(stepChartRef.value);
    // myChartParam.setOption(option,true);
    // myChartParam.off('click');
    // myChartParam.on('click', function(params) {
    //   // 过滤只处理 custom 系列的点击（避免其他元素触发）
    //   if (params.seriesType === 'custom') {
    //     // params.data 对应 series.data 中的单个数据项
    //     const [startTime, endTime, value, stepName] = params.data;

    //     let option = dataOption.value
    //     let markLineDatas = option.series[0].markLine.data
    //     markLineDatas.forEach(item => {
    //       item.lineStyle.width = 1;
    //       item.lineStyle.color = '#999999';
    //     });

    //     updateMarkLineWidth(markLineDatas, startTime, endTime, params.color)
    //     option.series[0].markLine.data = markLineDatas;
    //     lineEcharts.value.setOption(option);
    //   } 
    // });
    bindLineStepZoom();
  }
  //点击后线加粗/取消加粗
  function handleClick(params) {
    if (params.seriesType !== 'custom') return;

    const [startTime, endTime, value, stepName, id] = params.data;
    const option = dataOption.value;
    // 先清空 markLine，避免重复
    lineEcharts.value.setOption({
      series: [{
        markLine: {
          data: []
        }
      }]
    }, false);

    // 重新构建 markLine，同时添加 isActive 标记
    const markLineDatas = option.series[0].markLine.data.map(item => {
      // 默认样式
      let lineStyle = { width: 1, color: '#999999' };
      
      // 如果是被点击的 markLine，则 toggle
      // if (item.xAxis === startTime || item.xAxis === endTime) {
      if (item.id === id) {
        item.isActive = !item.isActive; // 切换状态
        if (item.isActive) {
          lineStyle.width = 4;
          lineStyle.color = params.color;
        }
      } else {
        // 其他 markLine 默认样式
        item.isActive = false;
      }

      return {
        ...item,
        lineStyle
      };
    });

    // 更新回图表
    // lineEcharts.value.setOption({
    //   series: [{
    //     markLine: {
    //       data: markLineDatas
    //     }
    //   }]
    // }, false);

    // 获取上次缩放状态
    let startValue: number | undefined;
    let endValue: number | undefined;

    if (lineEcharts.value) {
      const prevOption = lineEcharts.value.getOption();
      const dz = prevOption.dataZoom?.[0];
      if (dz) {
        startValue = dz.startValue;
        endValue = dz.endValue;
      }
      // 先销毁旧实例，避免重复绑定
      lineEcharts.value.dispose();
      lineEcharts.value = null;
    }
    // 如果之前有缩放记录，设置回去
    if (startValue !== undefined && endValue !== undefined) {
      option.dataZoom = option.dataZoom.map(dz => ({
        ...dz,
        startValue,
        endValue
      }));
    }

    // 保存状态
    //dataOption.value.series[0].markLine.data = markLineDatas;
    option.series[0].markLine.data = markLineDatas;
    // 更新回图表
    option.tooltip = {
      ...option.tooltip,   // 保留 trigger / axisPointer / style
      formatter: tooltipFormatter
    };
    console.log(typeof dataOption.value.tooltip?.formatter);
    // lineEcharts.value.setOption(option, false);

    var myChart = echarts.init(chartRef.value);
    myChart.setOption(option,true);
    lineEcharts.value = myChart;
    
    bindLineStepZoom();

    // lineEcharts.value.setOption({
    //   tooltip: {
    //     ...dataOption.value.tooltip,
    //     formatter: tooltipFormatter
    //   }
    // }, false);
}
  

  //简易版/详情版
  function handleViewChange(type){
    if(type == 'simple'){
      //简易版
      segmentedObj.value.viewMode = 'simple'
    }else{
      //详情版
      segmentedObj.value.viewMode = 'details'
    }
    //折线和步骤区间
    if(curveDataArray.value && curveDataArray.value.length > 0){
      if (curveDataArray.value?.[0]?.markArea) {
        curveDataArray.value[0].markArea.label = {
          show: segmentedObj.value.viewMode == 'details'? true:false,
          color: '#00BFFF',
          fontWeight: 'bold',
          position: 'insideTop'
        };
      }
      initEcharts(curveDataArray.value);
    }
    
  }
  //历史选中
  const selectSteps = ref([]);
  //步骤操作
  function onChange(newValues){
    //开头
    for (let i = 0; i < stepOptions.value.length; i++) {
      const id = stepOptions.value[i].value
       // 判断 id 是否在 newValues 中
      if (newValues.includes(id)) {
        disableArray.value[i+1] = true;
        //stepState.value.push(disableArray.value[i+1].id)
        setTopSelected(i);
        break;
      }else{
        disableArray.value[i+1] = false;
      }
    }
    //结尾
    for (let i = (stepOptions.value.length-1); i > 0; i--) {
      const id = stepOptions.value[i].value
       // 判断 id 是否在 newValues 中
      if (newValues.includes(id)) {
        disableArray.value[i-1] = true;
        setTailSelected(i);
        //stepState.value.push(disableArray.value[i-1].id)
        break;
      }else{
        disableArray.value[i-1] = false;
      }
    }
    // if(datas_.value){
    //   initStepEcharts(datas_.value.shift())
    // }
    //调用查询
    //emit('handle-change',  pointArray.value)
    //步骤开头与结尾不能禁止
    disableArray.value[0] = false;
    disableArray.value[stepOptions.value.length-1] = false;
    emit('step-switch',  pointArray.value)
  }
  
  function setTopSelected(val){
    for (let i = val+1; i < stepOptions.value.length-1; i++) {
      const optionId = stepOptions.value[i].value;
       // 判断id是否已存在于stepState.value中
      if (!stepState.value.includes(optionId)) {
        disableArray.value[i+1] = true;
        stepState.value.push(optionId);
      }else{
        disableArray.value[i+1] = true;
        break;
      }
    }
  }

  function setTailSelected(val){
    for (let i = val-1; i > 0 ; i--) {
      const optionId = stepOptions.value[i].value;
       // 判断id是否已存在于stepState.value中
      if (!stepState.value.includes(optionId)) {
        disableArray.value[i-1] = true;
        stepState.value.push(optionId);
      }else{
        disableArray.value[i-1] = true;
        break;
      }
    }
  }



  // 获取开始时间最小和结束时间最大
  function getTimeRange() {
    if (!steps.value || steps.value.length === 0 || !stepState.value || stepState.value.length === 0) {
      return null
    }

    // 将 stepState 转换为 Set 提高查找效率
    const stateSet = new Set(stepState.value)
    
    let minStartTimeRecord = null
    let maxEndTimeRecord = null
    
    // 遍历 steps，只考虑在 stepState 中的记录
    for (const item of steps.value) {
      if(!item.startTime){
        continue
      }
      if(!item.endTime){
        continue
      }
      if (stateSet.has(item.id)) {
        // 初始化第一个找到的记录
        if (minStartTimeRecord === null) {
          minStartTimeRecord = item
          maxEndTimeRecord = item
          continue
        }
        
        // 比较开始时间
        if (item.startTime < minStartTimeRecord.startTime) {
          minStartTimeRecord = item
        }
        
        // 比较结束时间
        if (item.endTime > maxEndTimeRecord.endTime) {
          maxEndTimeRecord = item
        }
      }
    }
    
    // 如果没有找到任何匹配的记录
    if (minStartTimeRecord === null) {
      return null
    }
    
    return {
      startTime: minStartTimeRecord.startTime,
      minStartTimeRecord,
      endTime: maxEndTimeRecord.endTime,
      maxEndTimeRecord,
      matchedCount: stepState.value.length,
      stepState: stateSet
    } 
  }

  // 判断每个 checkbox 是否禁用
  function isDisabled(index) {
    return disableArray.value[index] === true;
  }

  //设置点位
  function setPoints(data, unit){
    for(let i = 0; i < selectTags.length; i++){
      selectTags[i] = false;
    }
    //选择的点位清空
    pointArray.value = []
    tagsData.value = data;
    unitObj.value = unit;
    if(data && data.length > 0){
      selectTags[0] = true
      pointArray.value.push(tagsData.value[0].pointCode);
      // initEcharts([]);
      // initStepEcharts([['','',0,"步骤"],]);
      setTimeout(() => {
        emit('handle-change', pointArray.value)
      }, 50)
    }
  }
  //设置参数
  function setParam(data,informationData){
    queryParam.value = data;
    information.value = informationData;
    if(!queryParam.value.batch && !queryParam.value.order){
      information.value.endTime = '';
      information.value.startTime = '';
      information.value.formulaName = '';
    }
    disableArray.value = [];
    stepState.value = [];
    if(informationData.stepList){
      let stepList = informationData.stepList;
      let curveStepMap = informationData.curveStepMap;
      steps.value = stepList;
      let first = true;
      for (let i = 0; i < stepList.length; i++) {
        let item = stepList[i]
        // stepOptions.value.push({
        //   label: item.eopName,
        //   value: item.id
        // })
        if(curveStepMap){
          //剔除步骤
          let isDisable = true;
          if(!curveStepMap[item.ropId]){
            //没有被剔除的步骤
            if(i === 0 || i === stepList.length-1 || first){
              isDisable = false;
              //排除后第一个勾选
              first = false;
            }
            stepState.value.push(item.id)
          }else{
            isDisable = false;
            if(!first){
              //排除后最后一个勾选
              disableArray.value[i-1] = false;
              first = true;
            }
          }
          disableArray.value.push(isDisable);
        }else{
          //默认显示全量步骤
          stepState.value.push(item.id)
          let isDisable = true;
          if(i === 0 || i === stepList.length-1){
            isDisable = false;
          }
          disableArray.value.push(isDisable);
        }
      }
      stepOptions.value = informationData.stepList.map(item => ({
        label: item.eopName,
        value: item.id,
        startTime: item.startTime,
        endTime: item.endTime
      }))
      //stepState.value = informationData.stepList.map(item => item.id)
      
    }else{
      stepOptions.value = [];
    }  
  }
  //设置点位选中
  function setSelectPoint(){
    if(tagsData.value.length > 0){
       selectTags[0] = true;
       handleChange("",false);
    }
   
  }

  //保存步骤配置
  function onSave(){
    // 选中步骤集合
    const excludeIds = stepOptions.value.filter(option => !stepState.value.includes(option.value))
    .map(option => option.value);
    emit('step-save', stepState.value, excludeIds)
  }
  //步骤显示时间
  function stepTitle(item = {}) {
    const label = item.label ?? '';
    const startTime = item.startTime ?? '--';
    const endTime = item.endTime ?? '--';

    return `${label} (${startTime} - ${endTime})`;
  }

  function bindLineStepZoom() {
    if (lineEcharts.value && stepChart.value) {
      lineEcharts.value.off('datazoom'); 
      lineEcharts.value.on('datazoom', function () {
        const option = lineEcharts.value.getOption();
        const dz = option.dataZoom?.[0]; // 取第一个 dataZoom
        if (dz) {
          const startValue = dz.startValue;
          const endValue = dz.endValue;

          // 更新步骤图的显示范围
          stepChart.value.setOption({
            dataZoom: [
              {
                type: 'inside',
                xAxisIndex: 0,
                startValue,
                endValue
              }
            ]
          });
        }
      });
    }


    // if (lineEcharts.value && stepChart.value) {
    //   lineEcharts.value.off('datazoom'); // 防止重复绑定
    //   lineEcharts.value.on('datazoom', function (params) {
    //     // 获取当前 dataZoom 范围
    //     const option = lineEcharts.value.getOption();
    //     const xAxis = option.xAxis[0];
    //     const startValue = xAxis.min; // 当前起始时间
    //     const endValue = xAxis.max;   // 当前结束时间

    //     // 步骤图也设置相同的时间范围
    //     stepChart.value.setOption({
    //       dataZoom: [
    //         {
    //           type: 'inside',
    //           xAxisIndex: 0,
    //           startValue,
    //           endValue
    //         }
    //       ]
    //     });
    //   });
    // }
  }

  function getAxisBoundary() {
    if (!axisTimeRange.value.startTime || !axisTimeRange.value.endTime) {
      return { min: undefined, max: undefined };
    }
    return {
      min: axisTimeRange.value.startTime,
      max: axisTimeRange.value.endTime
    };
  }

  function setAxisTimeRange(startTime?: string | number, endTime?: string | number){
    axisTimeRange.value.startTime = startTime || undefined;
    axisTimeRange.value.endTime = endTime || undefined;
    const boundary = getAxisBoundary();
    if (lineEcharts.value) {
      lineEcharts.value.setOption({
        xAxis: {
          min: boundary.min,
          max: boundary.max
        }
      });
    }
    if (stepChart.value) {
      stepChart.value.setOption({
        xAxis: {
          min: boundary.min,
          max: boundary.max
        }
      });
    }
  }

  //追加点位单位
  function setUnit(unit){
    Object.assign(unitObj.value, unit);
  }

  //批次号处理
  function batchHandle(val){
    // 检查val是否包含'-'
    if (typeof val === 'string' && val.includes('-')) {
      // 包含'-'，返回'-'后面的部分
      return val.split('-')[1];
    }
    // 不包含'-'或不是字符串，返回原val
    return val;
  }
  // 暴露方法
  defineExpose({
    initEcharts,
    initStepEcharts,
    setPoints,
    setParam,
    setSelectPoint,
    getTimeRange,
    setUnit,
    setAxisTimeRange
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

:deep(.ant-form-item){
  margin-bottom: 7px;
}

:deep(.ant-descriptions-bordered .ant-descriptions-item-label),
:deep(.ant-descriptions-bordered .ant-descriptions-item-content) {
    padding: 12px 12px;
}



// .batch-panel {
//   width: 20%;
//   padding: 8px;
// }

// .my-descriptions {
//   font-size: 12px;
// }

// .step-section {
//   margin-top: 12px;
// }

.step-title {
  margin-bottom: 8px;
  font-weight: 600;
  font-size: 13px;
  color: #262626;

  display: flex;
  justify-content: space-between; /* 左右分开 */
  align-items: center; /* 垂直居中 */
}

.step-card {
  width: 100%;
  padding: 6px 10px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background-color: #fafafa;
  transition: all 0.2s;
}

.step-card:hover {
  border-color: #1677ff;
  background-color: #f0f5ff;
}


.batch-panel {
  height: 100%;
  width: 20%;
  padding: 8px;
  display: flex;
  flex-direction: column;
}

.my-descriptions {
  flex-shrink: 0;
}

/* 工艺步骤整体 */
.step-section {
  margin-top: 12px;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0; /* 必须 */
}


/* 真正滚动的容器 */
.step-scroll {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}


/* Webkit 内核滚动条 */
.step-scroll::-webkit-scrollbar {
  width: 0px;            /* 滚动条宽度 */
}
// .step-scroll::-webkit-scrollbar-thumb {
//   background-color: #fafafa;  /* 滑块颜色 */
//   border-radius: 3px;                     /* 圆角 */
// }
// .step-scroll::-webkit-scrollbar-track {
//   background-color: #fafafa; /* 滑道颜色 */
//   border-radius: 3px;
// }
:deep(.ant-checkbox-disabled+span) {
    color: rgba(51, 51, 51, 0.88);
}

.tag-point {
  flex-shrink: 0;
  height: 50px;  
  max-height: 50px;        /* 限制最大高度 */
  overflow-y: auto;
  overflow-x: hidden;
  .ant-space {
    margin-bottom: 1px !important; /* 或 0 */
    gap: 1px 0px !important;
  }
  /* 隐藏滚动条（不影响滚动） */
  scrollbar-width: none;        /* Firefox */
  -ms-overflow-style: none;     /* IE / Edge 旧版 */

  &::-webkit-scrollbar {
    display: none;              /* Chrome / Safari */
  }
}

</style>
