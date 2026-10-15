<template>
    <div style="display: flex;background-color: #ffffff;height: 80vh;">
      <!-- {{ disableArray }}==========
      {{ stepState }}========== -->
        <div class="batch-panel">
          <div class="step-title">批次信息</div>
          <a-descriptions :column="1" bordered class="my-descriptions">
              <a-descriptions-item label="订单号:">{{ queryParam.order }}</a-descriptions-item>
              <a-descriptions-item label="批次号:">{{ queryParam.batch }}</a-descriptions-item>
              <!-- <a-descriptions-item label="发酵液品种:">{{ information.variety }}</a-descriptions-item> -->
              <a-descriptions-item label="配方名称:">{{ information.formulaName }}</a-descriptions-item>
              <!-- <a-descriptions-item label="工艺浓度:">
              <a-tag v-if="information.concentration" color="orange">
                  {{ information.concentration }}
              </a-tag>
              <span v-else>-</span>
              </a-descriptions-item>
              <a-descriptions-item label="产量(KL):">
              <a-tag v-if="information.output" color="#108ee9">
                  {{ information.output }}
              </a-tag>
              <span v-else>-</span>
              </a-descriptions-item> -->

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
                          {{ item.label }}
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

        <div style="height: 27px;">
            <a-space :size="[0, 8]" wrap>
            <a-checkable-tag v-for="(tag, index) in tagsData" :key="tag" v-model:checked="selectTags[index]" @change="checked => handleChange(tag, checked)" style="height: 25px;line-height: 25px;">
                {{ tag.pointName }}
            </a-checkable-tag>
            </a-space>
        </div>
        <div style="width: 100%;">
            <div ref="chartRef" :style="{ height: chartHeight + 'px', width: '100%' }"></div>
        </div>
        <div style="width: 100%;">
            <div ref="stepChartRef" :style="{height :'70px', width: '100%' }"></div>
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
  const dataOption = ref([] as any);
  const pointArray = ref([] as any)
  const curveDataArray = ref([] as any)
  const deviceName = ref("")
  const segmentedObj = ref({viewMode:'simple'})
  const chartHeight = ref(598);
  const unitObj = ref({});
  const steps = ref([]);
  // 测试
  // const stepState = ref({value1:[]})
  const stepState = ref([])
  const stepOptions = ref([]);


  // 经用标记
  const disableArray = ref([])  

  //被选中的步骤区间
//   const stepInterval = ref(['','']);
//   const optionData = ref({
//     name: "-",
//     type: 'line',
//     symbol: 'none',  // 去掉所有数据点的符号
//     smooth: true,
//     data: [],
//   })
//   const pointChart = ref<EChartsType | null>(null);
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
    chartHeight.value = window.innerHeight - 331;
  });


  const lineEcharts = ref({})
  function initEcharts(datas) {
    chartHeight.value = window.innerHeight - 331;
    setTimeout(() => {
      initLineEcharts(datas);
    },200)
  }

  //曲线
  function initLineEcharts(datas) {
    curveDataArray.value = datas;

    // 在函数内部保存 unitObj.value 的引用
    const currentUnitObj = unitObj.value || {};

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
            <div><strong>时间：</strong>${time}</div>
            <div style="margin-top:4px;"><strong>参数数据：</strong></div>
          `;

          params.forEach(item => {
            const color = item.color; // 当前 series 的颜色
            const name = item.seriesName;
            const value = item.value[1] ?? item.value; // 兼容 [x, y] 结构和直接数值
            //获取单位
            let unit = currentUnitObj[name] || "";

            // 处理数值显示：小数保留两位，整数不处理
            let displayValue = value;
            if (typeof value === 'number') {
              if (Number.isInteger(value)) {
                displayValue = value; // 整数直接显示
              } else {
                displayValue = value.toFixed(2); // 小数保留两位
              }
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
        "top": "20px",
        "left": "3%",
        "right": "3%",
        "bottom": "20px",
        "containLabel": true
      },
      //鼠标滚动
      dataZoom: [
        {
          type: 'inside',  // 鼠标滚轮内缩放
          xAxisIndex: 0,   // 针对第一个 x 轴
          zoomOnMouseWheel: true,  // 滚轮缩放
          moveOnMouseMove: true,   // 鼠标拖动平移
          moveOnMouseWheel: false, // 不使用滚轮平移（防止误触）
        },
        {
          type: 'slider',  // 底部滑块
          xAxisIndex: 0,
          height: 10,
          bottom: 10
        }
      ],
      xAxis: {
        type: 'time',
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

  }
  //步骤
  //const datas_ = ref({})
  function initStepEcharts(datas) {
    //datas_.value = datas;
    // 配置图表
    var option = {
      grid: {
        top: 0,
        bottom: 10,
        left: "2%",
        right: "3%",
        containLabel: true
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
            const barHeight = api.size([0, 1])[1] * 0.8; // 压缩高度，让上下更紧凑
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
    //dataOption.value = option;
    var myChartParam = echarts.init(stepChartRef.value);
    myChartParam.setOption(option,true);
    myChartParam.off('click');
    myChartParam.on('click', function(params) {
      // 过滤只处理 custom 系列的点击（避免其他元素触发）
      if (params.seriesType === 'custom') {
        // params.data 对应 series.data 中的单个数据项
        const [startTime, endTime, value, stepName] = params.data;

        let option = dataOption.value
        let markLineDatas = option.series[0].markLine.data
        markLineDatas.forEach(item => {
          item.lineStyle.width = 1;
          item.lineStyle.color = '#999999';
        });

        updateMarkLineWidth(markLineDatas, startTime, endTime, params.color)
        option.series[0].markLine.data = markLineDatas;
        lineEcharts.value.setOption(option);
      } 
    });
    
  }

  //区间设置成5
  function updateMarkLineWidth(markLineDatas, startTime, endTime, color) {
    for (let i = 0; i < markLineDatas.length; i++) {
      if (markLineDatas[i].xAxis === startTime) {
        markLineDatas[i].lineStyle.width = 4;
        markLineDatas[i].lineStyle.color = color;
        continue; 
      }
      if (markLineDatas[i].xAxis === endTime) {
        markLineDatas[i].lineStyle.width = 4;
        markLineDatas[i].lineStyle.color = color;
        continue;
      }
    }
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
      if (stateSet.has(item.id)) {
        // 初始化第一个找到的记录
        if (minStartTimeRecord === null) {
          minStartTimeRecord = item
          maxEndTimeRecord = item
          continue
        }
        
        // 比较开始时间
        if (new Date(item.startTime) < new Date(minStartTimeRecord.startTime)) {
          minStartTimeRecord = item
        }
        
        // 比较结束时间
        if (new Date(item.endTime) > new Date(maxEndTimeRecord.endTime)) {
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
  function stepTitle(item) {
    return `${item.label} (${item.startTime} - ${item.endTime})`;
  }

  // 暴露方法
  defineExpose({
    initEcharts,
    initStepEcharts,
    setPoints,
    setParam,
    setSelectPoint,
    getTimeRange,
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
</style>