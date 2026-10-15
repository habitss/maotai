<template>
  <div>
    <!--查询区域-->
    <div class="jeecg-basic-table-form-container">
      <a-form @keyup.enter.native="searchQuery" :model="queryParam" :label-col="labelCol" :wrapper-col="wrapperCol" labelAlign="left">
        <a-row :gutter="24">
          <!--日期-->
          <a-col :lg="4">
            <a-form-item label="日期">
              <a-date-picker format="YYYY-MM-DD" valueFormat="YYYY-MM-DD" v-model:value="queryParam.date" @change="dateChange"/>
            </a-form-item>
          </a-col>
           <!--产线-->
          <a-col :lg="3">
            <a-form-item :label="t('language.productLine')">
              <kt-depart-select 
              ref="departSelectRef"
              v-model:value="queryParam.lineCode"
              select-type="line"
              :placeholder="t('language.selectProductLine')"
              @change="getDeviceData"
               />
            </a-form-item>
          </a-col>
          <!--设备-->
          <a-col :lg="4">
            <a-form-item label="设备">
              <a-select show-search option-filter-prop="label" v-model:value="queryParam.deviceId" 
              :options="deviceArr" @change="(val) => deviceChange(val, false)" placeholder="请选择设备" allowClear></a-select>
            </a-form-item>
          </a-col>
           <!--配方-->
          <a-col :lg="4">
            <a-form-item label="配方">
              <a-select show-search option-filter-prop="label" v-model:value="queryParam.recipe" 
              :options="recipeArr" @change="recipeChange" placeholder="请选择配方" allowClear></a-select>
            </a-form-item>
          </a-col>
           <!--订单号-->
          <a-col :lg="4">
            <a-form-item label="订单号">
              <a-select show-search option-filter-prop="label" v-model:value="queryParam.order" 
              :options="orderArr" @change="orderChange" placeholder="请选择订单号" allowClear></a-select>
            </a-form-item>
          </a-col>
           <!--批次-->
          <a-col :lg="4">
            <a-form-item label="批次">
              <a-select show-search option-filter-prop="label" v-model:value="queryParam.batch" 
              :options="batchArr" @change="batchChange" placeholder="请选择批次" allowClear></a-select>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
      <div style="height: 100%;margin: 0px 5px;">
         <SingleBatch ref="singleBatchRef" @handle-change="handleChange"></SingleBatch>
      </div>
    </div>

  </div>
  
</template>

<script lang="ts" name="pi-singleBatchStandardCurve" setup>
  import { ref, reactive, onMounted, unref, nextTick } from 'vue';
  import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
  import { getDepartId } from '/@/utils/ktTool';
  import { useUserStore } from '/@/store/modules/user';
  import { list as basePointList } from '/@/views/dataCalculation/pi/basePoint/PiBasePoint.api';
  import { getRecipe,getOrder,getBatch,getData,pointData,getDevice,getMashingData } from './batchCurvePi.api';
  import SingleBatch from './components/singleBatch.vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import dayjs from 'dayjs';
  const { t } = useI18n();
  const singleBatchRef = ref();
  const queryParam = ref<any>({});
  const userStore = useUserStore();
  const recipeArr = ref([] as any);
  const orderArr = ref([] as any)
  const batchArr = ref([] as any)
  const pointArray = ref([] as any)
  const stepDataArray = ref([] as any)
  const curveDataArray = ref([] as any)
  const markLineData = ref({})
  const markAreaData = ref({})
  const segmentedObj = ref({viewMode:'simple'})
  const chartHeight = ref(600);
  const deviceArr = ref([] as any);
  const curveType = ref('1');
  const optionData = ref({
    name: "-",
    type: 'line',
    symbol: 'none',  // 去掉所有数据点的符号
    smooth: true,
    data: [],
  })
  const exportParams = reactive({
    departId: '1',
    deviceId: queryParam?.value?.deviceId,
  });

  const labelCol = reactive({
    xs: { span: 24 },
    sm: { span: 7 },
  });
  const wrapperCol = reactive({
    xs: { span: 24 },
    sm: { span: 16 },
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


  onMounted(() => {
    exportParams.departId = getDepartId(userStore?.getUserInfo?.orgCode);
    chartHeight.value = window.innerHeight - 330;
    setTimeWithDayjs(new Date(), 0);
    singleBatchRef.value.initEcharts([optionData.value]);
    // 步骤区间
    const formattedTime = dayjs(new Date()).format('YYYY-MM-DD HH:mm:ss')
    singleBatchRef.value.initStepEcharts([[formattedTime,formattedTime,0,"步骤"],]);
    //获取糖化锅最新配置曲线
    //getMashing();
  });

  function handleChange(points) {
    curveDataArray.value =[]
    pointArray.value = [];
    pointArray.value = points;
    //获取点位数据
    devicePointData();
  };

  //初始化数据
  function setTimeWithDayjs(date, value) {
    const formattedTime = dayjs(date).format('YYYY-MM-DD HH:mm:ss')
    optionData.value.data = [[formattedTime, value]]
  }

  //设备变更 通过设备获取点位
  async function deviceChange(val,isFirst){
    if(!isFirst){
      queryParam.value.areaNo = "";
      queryParam.value.unitNo = "";
      queryParam.value.pcuNo = "";
      queryParam.value.recipe = undefined;
      queryParam.value.order = undefined
      queryParam.value.batch = undefined;
      batchArr.value = [];
      orderArr.value = [];
      recipeArr.value = [];

      //获取设备code
      const device = deviceArr.value.find(item => item.value === val);
     
      if(device && device.code){
        let deviceCode = device.code;
        queryParam.value.unitNo = deviceCode
        queryParam.value.unitName = device.name
      }
    }
    if(!val){
      tagsData.value = [];
      singleBatchRef.value.setPoints([],[]);
      return false;
    }

    let res = await basePointList({pageNo: 1,pageSize: 9999, deviceId:val, isNewData: false});
    tagsData.value = [];
    let unitObj = {};
    if(res && res.records){
      res.records.forEach((item, index, array) => {
        //0标准和1目标
        if(item.dataAttribute == '0'){
          tagsData.value.push({
            pointCode: item.pointCode,
            pointName: item.pointName
          })
        }
        unitObj[item.pointName] = item.unit;
      });
      singleBatchRef.value.setPoints(tagsData.value, unitObj);
    }
    //获取订单号
    getOrders(isFirst);
    //配方
    getRecipes();
    if(isFirst){
      //设置第一个点位被选中
      singleBatchRef.value.setSelectPoint();
    }

  }

  // 获取配方
  async function getRecipes(){
    recipeArr.value = [];
    let obj = {
      "startTime": queryParam.value.date+" 00:00:00",
      "endTime": queryParam.value.date+" 23:59:59",
      "unitNo": queryParam.value.unitNo,
      "curveType": curveType.value,
      "unitName": queryParam.value.unitName,
    }
    let res = await getRecipe(obj);
    if(res){
       res.forEach((item, index, array) => {
        recipeArr.value.push({
          value: item.recipe,
          label: item.recipe
        })
      });
    } 
  }

    // 选择日期
  function dateChange(){
    //获取设备
    getDeviceData(false);
    //获取订单号
    getOrders(false);
    //获取批次号
    getBatchs(false);
  }

  // 选择配方
  function recipeChange(){
    getOrders(false);
    setParam();
  }
  // 获取订单
  async function getOrders(isFirst){
     if(!isFirst){
      queryParam.value.order = undefined
      queryParam.value.batch = undefined;
      orderArr.value = [];
      setParam();
    }
    
    if(!queryParam.value.date){
      return false;
    }
    if(!queryParam.value.recipe){
      return false;
    }

    let obj = {
      "startTime": queryParam.value.date+" 00:00:00",
      "endTime": queryParam.value.date+" 23:59:59",
      "recipe": queryParam.value.recipe,
      "unitNo": queryParam.value.unitNo,
      "unitName": queryParam.value.unitName,
      "curveType": curveType.value
    }
    let res = await getOrder(obj);
    if(res){
       res.forEach((item, index, array) => {
        orderArr.value.push({
          value: item.orderNo,
          label: item.orderNo
        })
      });
    }
  }

   // 选择订单
  async function orderChange(){
    getBatchs(false);
  }
  // 获取批次
  async function getBatchs(isFirst){
    if(!isFirst){
      queryParam.value.batch = undefined;
    }
    if(!queryParam.value.order){
      return false;
    }

    batchArr.value = [];
    let obj = {
      "startTime": queryParam.value.date+" 00:00:00",
      "endTime": queryParam.value.date+" 23:59:59",
      "recipe": queryParam.value.recipe,
      "order":  queryParam.value.order,
      "unitNo": queryParam.value.unitNo,
      "unitName": queryParam.value.unitName,
      "curveType": curveType.value
    }
    let res = await getBatch(obj);
    if(res){
       res.forEach((item, index, array) => {
        batchArr.value.push({
          value: item.batchNo,
          label: item.batchNo
        })
      });
    }
  }
  //选择批次
  function batchChange(){
    getAllData();
  }
  //获取数据
  async function getAllData(){
    // let areaNo = "";
    // let reccatNo = "";
    // let recipeNo = "";
    // if(queryParam.value.recipe){
    //   let array = queryParam.value.recipe.split("-");
    //   areaNo = array[0];
    //   reccatNo = array[1];
    //   recipeNo = array[2];
    // }
    const result = getDateRange(queryParam.value.date);
    let obj = {
      "startTime": result.startDate+" 00:00:00",
      "endTime":  result.endDate+" 23:59:59",
      "recipe": queryParam.value.recipe,
      "unitNo": queryParam.value.unitNo,
      "unitName": queryParam.value.unitName,
      "order": queryParam.value.order,
      "batch": queryParam.value.batch,
      "curveType": curveType.value
    }
    let res = await getData(obj);
    if(res){
      information.value.startTime = res.startTime;
      information.value.endTime = res.endTime;
      information.value.formulaName = res.recipeName
      let stepData = res.stepList;
      let markLineDataArray = [];
      let markAreaDataArray = [];
      setParam();

      // 生成 0~100 之间的波动值
      const layerHeight = 20;
      let maxY = 100;
      stepDataArray.value = [];
      stepData.forEach((item, index, array) => {

        // 使用正弦或余弦控制上下浮动
        const amplitude = 40; // 波动幅度（上下各 40）
        const base = 50;      // 中心基线
        const yCenter = base + Math.sin(index * Math.PI / 4) * amplitude;

        const height = 10; // 每个块的高度
        const y1 = yCenter - height / 2;
        const y2 = yCenter + height / 2;
        // let monitoringTime = getMinutesBetween(item.startTime, item.endTime);
         let between = getTimeBetween(item.startTime, item.endTime);
        if(index == 0){
          markLineDataArray.push({xAxis:item.startTime,lineStyle: {color: '#999999',width: 1}});
          markLineDataArray.push({xAxis:item.endTime,lineStyle: {color: '#999999',width: 1}});
          
          let markArray = []
          markArray.push({xAxis: item.startTime, name: `${item.eopName}\n时长：${between.display}`,yAxis: y1 });
          markArray.push({xAxis:item.endTime, yAxis: y2});
          markAreaDataArray.push(markArray);
        }else{
          markLineDataArray.push({xAxis:item.startTime,lineStyle: {color: '#999999',width: 1}});
          markLineDataArray.push({xAxis:item.endTime,lineStyle: {color: '#999999',width: 1}});
          let markArray = []
          markArray.push({xAxis: item.startTime, name: `${item.eopName}\n时长：${between.display}` ,yAxis: y1});
          markArray.push({xAxis:item.endTime ,yAxis: y2});
          markAreaDataArray.push(markArray);
        }
        stepDataArray.value.push([item.startTime, item.endTime, between.display, item.eopName]);
        
      })
     
      //生成图表步骤区间
      // 竖直虚线
      markLineData.value = {
        symbol: 'none',
        label: {
          show: true,
          formatter: (p) => p.name,
          color: '#00BFFF',
          position: 'insideEndTop'
        },
        lineStyle: {
          type: 'dashed',
          color: '#00BFFF',
        },
        data: markLineDataArray
      };
      // 区段
      markAreaData.value = {
        silent: true, // 禁止鼠标交互（避免tooltip乱跳）
        itemStyle: {
          color: 'rgba(0,191,255,0)',  // 淡蓝色背景
        },
        label: {
          show: segmentedObj.value.viewMode == 'details'? true:false,
          color: '#00BFFF',
          fontWeight: 'bold',
          position: 'insideTop'
        },
        data: markAreaDataArray
      }
      devicePointData();
    }
  }
  //获取点位数据
  async function devicePointData(){
    if(!pointArray.value || pointArray.value.length == 0){
      singleBatchRef.value.initEcharts([optionData.value])
      return false;
    }
    let points = pointArray.value.join();
    if(!information.value.startTime || !information.value.endTime){
      return false;
    }
    if(information.value.startTime =='-' || information.value.endTime =='-'){
      return false;
    }

    let obj = {
      "startTime": information.value.startTime,
      "endTime": information.value.endTime,
      points
    }
    let res = await pointData(obj);
    let i = 0;
    curveDataArray.value = [];
    for (const key in res.pointTimeData) {
      let data = res.pointTimeData[key];
      //点位名称
      let name = res.pointName[key];
      //let name = getNameByKey(tagsData.value, key);
      // let standard = name.includes("标准")
      let keywords = ["标准","设定","目标"]
      const standard = keywords.some(keyword => name.includes(keyword));
      let eObj = {}
      if(i === 0){
        eObj = {
          name: name,
          type: 'line',
          symbol: 'none',  // 去掉所有数据点的符号
          smooth: true,
          data: data,
          lineStyle: {
              type: standard?'dashed':'solid',  // 实线
              width: 2
          },
          markLine: {
            ...markLineData.value,
            label: {
              show: false  // 完全隐藏标签
            }
          },
          markArea: markAreaData.value
        }
        i++;
      }else{
        eObj = {
          name: name,
          type: 'line',
          symbol: 'none',  // 去掉所有数据点的符号
          smooth: true,
          data: data,
          lineStyle: {
              type: standard?'dashed':'solid',  // 实线
              width: 2
          }
        }
      }
      curveDataArray.value.push(eObj);
    };
    //折线和步骤区间
    singleBatchRef.value.initEcharts(curveDataArray.value);
    //步骤区间
    singleBatchRef.value.initStepEcharts(stepDataArray.value);
  }

 /**
   * 计算时间差，智能返回分钟或秒
   * @param {string|Date} startTime - 开始时间
   * @param {string|Date} endTime - 结束时间
   * @returns {Object} 包含数值和单位的结果对象
   */
  function getTimeBetween(startTime, endTime) {
      const start = new Date(startTime);
      const end = new Date(endTime);
      
      // 验证日期有效性
      if (isNaN(start.getTime()) || isNaN(end.getTime())) {
          throw new Error('Invalid date provided');
      }
      
      const diffMs = Math.abs(end - start);
      const diffMinutes = Math.floor(diffMs / (1000 * 60));
      const diffSeconds = Math.floor(diffMs / 1000);
      
      // 如果分钟数为0，返回秒数
      if (diffMinutes === 0) {
          return {
              value: diffSeconds,
              unit: 'seconds',
              display: `${diffSeconds}秒`
          };
      }
      
      return {
          value: diffMinutes,
          unit: 'minutes', 
          display: `${diffMinutes}分钟`
      };
  }
  //参数设置
  function setParam(){
    singleBatchRef.value.setParam(queryParam.value,information.value);
  }
  function searchQuery(){}

  /**
   * 获取指定日期前后天数的日期范围
   * @param {string|Date} baseDate - 基准日期
   * @param {number} daysBefore - 往前天数，默认30
   * @param {number} daysAfter - 往后天数，默认30
   * @returns {Object} 包含开始日期和结束日期的对象
   */
  function getDateRange(baseDate, daysBefore = 30, daysAfter = 30) {
    const date = new Date(baseDate);
    
    const startDate = new Date(date);
    startDate.setDate(startDate.getDate() - daysBefore);
    
    const endDate = new Date(date);
    endDate.setDate(endDate.getDate() + daysAfter);
    
    return {
        startDate: startDate.toISOString().split('T')[0],
        endDate: endDate.toISOString().split('T')[0],
        startDateObj: startDate,
        endDateObj: endDate
    };
}


//获取设备
async function getDeviceData(isFirst){
  if(!isFirst){
    queryParam.value.areaNo = "";
    queryParam.value.unitNo = "";
    queryParam.value.pcuNo = "";
    queryParam.value.recipe = undefined;
    queryParam.value.order = undefined
    queryParam.value.batch = undefined;
    batchArr.value = [];
    orderArr.value = [];
    recipeArr.value = [];

    queryParam.value.deviceId = undefined;
    queryParam.value.areaNo = '';
    queryParam.value.unitNo = '';
    deviceArr.value = []
  }
  if(!queryParam.value.date){
    return false;
  }
  if(!queryParam.value.lineCode){
    return false;
  }
  let obj = {
    "startTime": queryParam.value.date+" 00:00:00",
    "endTime": queryParam.value.date+" 23:59:59",
    "lineCode": queryParam.value.lineCode,
    "curveType": curveType.value
  }
  let res = await getDevice(obj);
  if(res){
      res.forEach((item, index, array) => {
      deviceArr.value.push({
        value: item.id,
        label: `${item.deviceName}(${item.deviceCode})`,
        code: item.deviceCode,
        name: item.deviceName
      })
    });
  }
} 
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


</style>