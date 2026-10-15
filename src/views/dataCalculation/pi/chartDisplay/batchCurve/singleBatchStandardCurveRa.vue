<template>
  <a-spin :spinning="pageLoading">
  <div>
    <!--查询区域-->
    <div class="jeecg-basic-table-form-container">
      <a-form @keyup.enter.native="searchQuery" :model="queryParam" :label-col="labelCol" :wrapper-col="wrapperCol" labelAlign="left">
        <a-row :gutter="24">
          <!--日期-->
          <a-col :lg="5">
            <!--{{ stepDataArray }} === -->
            <!-- <a-form-item label="日期"
            :label-col="{ style: { width: '50px' } }"
            :wrapper-col="{ style: { width: 'calc(100% - 50px)' } }"
            >
              <a-date-picker format="YYYY-MM-DD" valueFormat="YYYY-MM-DD" v-model:value="queryParam.date" @change="dateChange"/>
            </a-form-item> -->
            <a-form-item label="日期">
              <a-range-picker
                format="YYYY-MM-DD"
                valueFormat="YYYY-MM-DD"
                v-model:value="queryParam.dateRange"
                @change="areaChange(false)"
              />
            </a-form-item>
          </a-col>
           <!--产线-->
          <a-col :lg="4">
            <a-form-item :label="t('language.productLine')"
            :label-col="{ style: { width: '50px' } }"
            :wrapper-col="{ style: { width: 'calc(100% - 50px)' } }"
            >
              <!-- <kt-depart-select 
              v-model:value="queryParam.lineCode"
              select-type="line"
              :placeholder="t('language.selectProductLine')"
              @change="getDeviceData"
               /> -->
              <kt-dict
                v-model:value="queryParam.areaId"
                placeholder="请选择区域"
                dict="MES3_PI_CURVE_AREA"
                class="width-250 margin-left-10"
                style="width:250px !important;"
                @change="areaChange(false)"
              />
            </a-form-item>
          </a-col>
          <!--设备-->
          <a-col :lg="5">
            <a-form-item label="设备"
              :label-col="{ style: { width: '50px' } }"
              :wrapper-col="{ style: { width: 'calc(100% - 50px)' } }"
            >
              <a-select show-search option-filter-prop="label" v-model:value="queryParam.deviceId" 
              :options="deviceArr" @change="(val) => deviceChange(val, false)" placeholder="请选择设备" allowClear></a-select>
            </a-form-item>
          </a-col>
           <!--配方-->
          <!-- <a-col :lg="5">
            <a-form-item label="配方" 
              :label-col="{ style: { width: '50px' } }"
              :wrapper-col="{ style: { width: 'calc(100% - 50px)' } }">
              <a-select show-search option-filter-prop="label" v-model:value="queryParam.recipe" 
              :options="recipeArr" @change="recipeChange" placeholder="请选择配方" allowClear></a-select>
            </a-form-item>
          </a-col> -->
           <!--订单号-->
          <!-- <a-col :lg="3">
            <a-form-item label="订单号">
              <a-select show-search option-filter-prop="label" v-model:value="queryParam.order" 
              :options="orderArr" @change="orderChange" placeholder="请选择订单号" allowClear></a-select>
            </a-form-item>
          </a-col> -->
           <!--批次-->
          <a-col :lg="3">
            <a-form-item label="批次">
              <a-select show-search option-filter-prop="label" v-model:value="queryParam.batch" 
              :options="batchArr" @change="batchChange" placeholder="请选择批次" allowClear></a-select>
            </a-form-item>
          </a-col>
          <!--上一批/下一批-->
          <a-col :lg="4">
            <!--下一批-->
            <a-button type="primary" @click="getBatchSearch(0)" style="margin-left: 8px" :loading="batchDisabled">下一批</a-button>
            <!--上一批-->
            <a-button type="primary" @click="getBatchSearch(1)" style="margin-left: 8px" :loading="batchDisabled">上一批</a-button>
          </a-col>
        </a-row>
      </a-form>
      <div style="height: 100%;margin: 0px 5px;">
         <SingleBatch ref="singleBatchRef" @handle-change="handleChange" @step-save="stepSave" @step-switch="stepSwitch"></SingleBatch>
      </div>
    </div>

  </div>
  </a-spin>
</template>

<script lang="ts" name="pi-singleBatchStandardCurveRa" setup>
  import { ref, reactive, onMounted, unref, nextTick } from 'vue';

  import KtDevice from '/@/components/KtDeviceType/KtDeviceType.vue';
  import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
  import { getDepartId } from '/@/utils/ktTool';
  import { useUserStore } from '/@/store/modules/user';
  import { list as basePointList } from '/@/views/dataCalculation/pi/basePoint/PiBasePoint.api';
  import { getRecipe,getOrder,getBatch,getData,pointData,getDevice,getBatchByDirection,stepsSave,getOtherData,getMashingData } from './batchCurveRa.api';
  import SingleBatch from './components/singleBatch.vue';
  import { getRecipe as getRecipePi,getOrder as getOrderPi,getBatch as getBatchPi,getData as getDataPi,pointData as pointDataPi,
  getDevice as getDevicePi } from './batchCurvePi.api';
  import * as echarts from 'echarts';
  import type { EChartsType } from 'echarts';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import KtDict from '/@/components/KtDict/KtDict.vue';
  const { createMessage } = useMessage();
  import dayjs from 'dayjs';
  const { t } = useI18n();
  const singleBatchRef = ref();
  const queryParam = ref<any>({});
  const userStore = useUserStore();
  const recipeArr = ref([] as any);
  const deviceArr = ref([] as any);
  const orderArr = ref([] as any)
  const batchArr = ref([] as any)
  const pointArray = ref([] as any)
  const stepDataArray = ref([] as any)
  const curveDataArray = ref([] as any)
  const deviceName = ref("")
  const markLineData = ref({})
  const markAreaData = ref({})
  const segmentedObj = ref({viewMode:'simple'})
  const chartHeight = ref(600);
  const batchDisabled = ref(false);
  const pageLoading = ref(false);
  //曲线数据类型
  const curveDataType = ref('1');
  //曲线类型1单批次
  const curveType = ref('1');
  //被选中的步骤区间
  const stepInterval = ref(['','']);
  const optionData = ref({
    name: "-",
    type: 'line',
    symbol: 'none',  // 去掉所有数据点的符号
    smooth: true,
    data: [],
  })
  const pointChart = ref<EChartsType | null>(null);
  const exportParams = reactive({
    departId: '1',
    deviceId: queryParam?.value?.deviceId,
  });

  const labelCol = reactive({
    xs: { span: 7 },
    sm: { span: 7 },
  });
  const wrapperCol = reactive({
    xs: { span: 24 },
    sm: { span: 24 },
  });

  const information = ref({
    order: '-',
    batch: '-',
    variety: '-',
    formulaName: '-',
    concentration: '',
    output: '',
    startTime: '-',
    endTime: '-',
    stepList:[],
    curveStepMap: {},
  });

  const tagsData = ref([]);
  const selectTags = reactive([]);

  //黑龙江特殊处理-记录步骤和时间范围
  const stepTimeNj = ref([]);

  onMounted(() => {
    exportParams.departId = getDepartId(userStore?.getUserInfo?.orgCode);
    chartHeight.value = window.innerHeight - 330;
    setTimeWithDayjs(new Date(), 0);
    singleBatchRef.value.initEcharts([optionData.value]);
    //获取配方
    //getRecipes();
    // 步骤区间
    const formattedTime = dayjs(new Date()).format('YYYY-MM-DD HH:mm:ss')
    singleBatchRef.value.initStepEcharts([[formattedTime,formattedTime,0,"步骤"],]);
    
    //获取糖化锅最新配置曲线
    getMashing();
  });
  //点位切换
  function handleChange(points) {
    curveDataArray.value =[]
    //stepDataArray.value =[]
    pointArray.value = [];
    pointArray.value = points;
    //判断批次是否为空
    if(!queryParam?.value?.batch){
      return false;
    }
    //获取点位数据
    //devicePointData();
    batchChange();
  };


  //区域/日期变更
  function areaChange(isFirst){
    curveDataType.value = '1'
    information.value.stepList = [];

    //queryParam.value.areaId = "";
    queryParam.value.unitId = "";
    queryParam.value.pcuNo = "";
    queryParam.value.recipe = undefined;
    queryParam.value.order = undefined;

    information.value.startTime = '';
    information.value.endTime = '';
    information.value.formulaName = '';

    setParam();
    //曲线
    singleBatchRef.value.initEcharts([optionData.value]);
    // 步骤区间
    const formattedTime = dayjs(new Date()).format('YYYY-MM-DD HH:mm:ss')
    singleBatchRef.value.initStepEcharts([[formattedTime,formattedTime,0,"步骤"],]);

    if (!queryParam.value.dateRange || queryParam.value.dateRange.length !== 2) {
      return false;
    }
    if (!queryParam.value.areaId) {
      return false;
    }
    //获取批次
    //getBatchs(false);
    //获取设备
    getDeviceData(false)
  }



  //步骤切换
  function stepSwitch(points){
    curveDataArray.value =[]
    // stepDataArray.value =[]
    pointArray.value = [];
    pointArray.value = points;
    //获取点位数据
    devicePointData();
  }
  //初始化数据
  function setTimeWithDayjs(date, value) {
    const formattedTime = dayjs(date).format('YYYY-MM-DD HH:mm:ss')
    optionData.value.data = [[formattedTime, value]]
  }

  //设备变更 通过设备获取点位
  async function deviceChange(val,isFirst){
    console.log("设备编码: "+val)
    if(!isFirst){
      queryParam.value.areaId = "";
      queryParam.value.unitId = "";
      queryParam.value.pcuNo = "";
      queryParam.value.deviceNo =  "";
      queryParam.value.recipe = undefined;
      queryParam.value.order = undefined
      queryParam.value.batch = undefined;

      information.value.startTime = '';
      information.value.endTime = '';
      information.value.formulaName = '';
      information.value.stepList = [];

      batchArr.value = [];
      orderArr.value = [];
      recipeArr.value = [];
      
      //获取设备code
      const device = deviceArr.value.find(item => item.value === val);
      curveDataType.value = device.curveDataType
      let deviceCode = device.code;
      if(deviceCode && deviceCode.includes("-")){
        //区域-设备
        let units = deviceCode.split("-");
        queryParam.value.deviceNo = deviceCode;
        queryParam.value.areaId = units[0]
        queryParam.value.unitId = units[1]
      }else{
        queryParam.value.areaId = "-1"
      }
    }
    // if(!val){
    //   setParam();
    //   return false;
    // }
    setParam();
    tagsData.value = [];
    singleBatchRef.value.setPoints([],[]);
    //曲线
    singleBatchRef.value.initEcharts([optionData.value]);
    // 步骤区间
    const formattedTime = dayjs(new Date()).format('YYYY-MM-DD HH:mm:ss')
    singleBatchRef.value.initStepEcharts([[formattedTime,formattedTime,0,"步骤"],]);
    if(!val){
      return false;
    }
    //获取批次
    await getBatchDispatch(isFirst);

    // let res = await basePointList({pageNo: 1,pageSize: 9999, deviceId:val, dataAttribute:0});
    let res = await basePointList({pageNo: 1,pageSize: 9999, deviceId:val, isNewData: false,orderField: 'sort',orderType:'asc'});

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
    //getOrders();
    //配方
    //getRecipes();
    //获取批次
    //getBatchs(false);
  }
  //设备名称
  function currentData(deviceObj){
    queryParam.value.areaId = "";
    queryParam.value.unitId = "";
    queryParam.value.pcuNo = "";
    queryParam.value.recipe = undefined;
    queryParam.value.order = undefined
    queryParam.value.batch = undefined;
    batchArr.value = [];
    orderArr.value = [];
    if(deviceObj){
      deviceName.value = deviceObj.deviceName;
      //获取设备code
      let deviceCode = deviceObj.deviceCode
      if(deviceCode && deviceCode.includes("-")){
        //区域-设备
        let units = deviceCode.split("-");
        queryParam.value.areaId = units[0]
        queryParam.value.unitId = units[1]
      }else{
        queryParam.value.areaId = "-1"
      }
    }else{
      deviceName.value = "";
    }
    getRecipes();
  }

  // 获取配方
  async function getRecipes(){
    queryParam.value.recipe = undefined;
    recipeArr.value = [];
    let obj = {
      // "eventTime": queryParam.value.date+" 00:00:00",
      // "endTime": queryParam.value.date+" 23:59:59",
      "eventTime": queryParam.value.dateRange[0]+" 00:00:00",
      "endTime": queryParam.value.dateRange[1]+" 23:59:59",
      "areaId": queryParam.value.areaId,
      "unitId": queryParam.value.unitId
    }
    let res = await getRecipe(obj);
    if(res){
       res.forEach((item, index, array) => {
        recipeArr.value.push({
          // value: `${item.areaId}-${item.uid}`,
          // label: `${item.nameCn}(${item.uid})`
          value: `${item.areaId}-${item.recipeId}`,
          label: `${item.recipeName}(${item.recipeId})`
        })
      });
    } 
  }
  // 选择配方
  function recipeChange(){
    getOrders();
    setParam();
  }
  // 选择日期
  function dateChange(){
    //获取设备
    getDeviceData(false);
    //获取订单号
    //getOrders();
    //获取批次号
    //getBatchs();
  }

  // 获取订单
  async function getOrders(){
    setParam();
    if(!queryParam.value.date){
      return false;
    }
    orderArr.value = [];
    let areaId = "";
    let recipeId = "";
    if(queryParam.value.recipe){
      let array = queryParam.value.recipe.split("-");
      areaId = array[0];
      recipeId = array[1];
    }

    // let unitId = '';
    // if(queryParam.value.deviceId){
    //   let array = queryParam.value.deviceId.split("-");
    //   if(array && array.length ===2){
    //     unitId = array[1];
    //   }else if(array && array.length ===1){
    //     unitId = array[0];
    //   }
    // }

    let obj = {
      // "eventTime": queryParam.value.date+" 00:00:00",
      // "endTime": queryParam.value.date+" 23:59:59",
      "eventTime": queryParam.value.dateRange[0]+" 00:00:00",
      "endTime": queryParam.value.dateRange[1]+" 23:59:59",
      "areaId": queryParam.value.areaId,
      "recipeId": recipeId,
      "unitId": queryParam.value.unitId
    }
    let res = await getOrder(obj);
    if(res){
       res.forEach((item, index, array) => {
        orderArr.value.push({
          value: item.orderId,
          label: item.orderId
        })
      });
    }
  }

   // 选择订单
  async function orderChange(){
    queryParam.value.batch = undefined
    getBatchs(false);
  }
  // 获取批次
  async function getBatchs(isFirst){
    batchArr.value = [];
    //let areaId = "";
    // let recipeId = "";
    // if(queryParam.value.recipe){
    //   let array = queryParam.value.recipe.split("-");
    //   //areaId = array[0];
    //   recipeId = array[1];
    // }
    // let unitId = '';
    // if(queryParam.value.deviceId){
    //   let array = queryParam.value.deviceId.split("-");
    //   if(array && array.length ===2){
    //     unitId = array[1];
    //   }else if(array && array.length ===1){
    //     unitId = array[0];
    //   }
    // }
    let obj = {
      //areaNo: areaId,
      // "eventTime": queryParam.value.date+" 00:00:00",
      // "endTime": queryParam.value.date+" 23:59:59",
      "eventTime": queryParam.value.dateRange[0]+" 00:00:00",
      "endTime": queryParam.value.dateRange[1]+" 23:59:59",
      "areaId": queryParam.value.areaId,
      //"recipeId": recipeId,
      //"orderId":  queryParam.value.order,
      "unitId": queryParam.value.unitId
      
    }
    let res = await getBatch(obj);
    if(res){
       res.forEach((item, index, array) => {
        batchArr.value.push({
          value: item.batchNr,
          label: item.batchNr,
          orderId: item.orderId
        })
      });
    }
  }
  //选择批次
  function batchChange(){
    //queryParam.value.order = undefined;
    // getAllData();
    if(curveDataType.value == '1'){
      getAllData();
    }else{
      getAllDataPi();
    }
  }
  //获取数据
  async function getAllData(){
    //let areaId = "";
    // let recipeId = "";
    // if(queryParam.value.recipe){
    //   let array = queryParam.value.recipe.split("-");
    //   //areaId = array[0];
    //   recipeId = array[1];
    // }
    // let unitId = '';
    // if(queryParam.value.deviceId){
    //   let array = queryParam.value.deviceId.split("-");
    //   if(array && array.length ===2){
    //     unitId = array[1];
    //   }else if(array && array.length ===1){
    //     unitId = array[0];
    //   }
    // }
    pageLoading.value = true;
    // 获取订单
    if(queryParam.value.batch){
      const item = batchArr.value.find(item => item.value === queryParam.value.batch);
      if(item){
        queryParam.value.order = item.orderId;
      }
    }
    //开始时间前后30天
    const result = getDateRange(queryParam.value.dateRange[0]);
    let obj = {
      "eventTime": result.startDate+" 00:00:00",
      "endTime": result.endDate+" 23:59:59",
      //"eventTime": queryParam.value.date+" 00:00:00",
      //"endTime": queryParam.value.date+" 23:59:59",
      areaId: queryParam.value.areaId,
      // recipeId: recipeId,
      //"unitId": unitId,
      unitId: queryParam.value.deviceId,
      orderId: queryParam.value.order,
      batchNr: queryParam.value.batch,
    }
    //获取冷麦汁等参数(糖化)
    //if(queryParam.value.areaId == '2'){
    getOtherParam(queryParam.value);
    //}
    
    let res = await getData(obj);
    if(res && res.raStepList && res.raStepList.length > 0){
      const normalizeTimeText = (val) => {
        if (!val) return '';
        const text = String(val).trim().replace('T', ' ').replace('Z', '');
        const parsed = dayjs(text);
        if (parsed.isValid()) {
          return parsed.format('YYYY-MM-DD HH:mm:ss');
        }
        return text.length >= 19 ? text.slice(0, 19) : text;
      };
      const normalizeStepList = (list = []) => {
        return (list || []).map((item) => ({
          ...item,
          startTime: normalizeTimeText(item.startTime || item.eventTime),
          endTime: normalizeTimeText(item.endTime || item.finishTime),
          eopName: item.eopName || item.bopName
        }));
      };
      const getBoundaryTime = (list = [], field = 'startTime', type = 'min') => {
        const values: string[] = list
          .map((item) => item?.[field])
          .filter((time) => !!time);
        if (!values.length) return '';
        return type === 'max' ? values.reduce((a, b) => (a > b ? a : b)) : values.reduce((a, b) => (a < b ? a : b));
      };

      const raStepList = normalizeStepList(res.raStepList);
      const standardStepList = normalizeStepList(res.standardStepList);
      const stepData = standardStepList && standardStepList.length > 0 ? standardStepList : raStepList;

      const stepStartTime = getBoundaryTime(stepData, 'startTime', 'min');
      const stepEndTime = getBoundaryTime(stepData, 'endTime', 'max');

      information.value.startTime = stepStartTime || res.startTime;
      information.value.endTime = stepEndTime || res.endTime;
      information.value.formulaName = res.recipeName
      information.value.stepList = res.raStepList
      information.value.curveStepMap = res.curveStepMap
      let markLineDataArray = [];
      let markAreaDataArray = [];
      setParam();

      // 生成 0~100 之间的波动值
      // const layerHeight = 20;
      // let maxY = 100;
      stepDataArray.value = []

      //黑龙江特殊处理
      stepTimeNj.value = []
      stepData.forEach((item, index, array) => {

        //获取需要特殊处理的步骤
        // if(item.eopName == '自然升温'){
        //   stepTimeNj.value.push({startTime: item.startTime,endTime: item.endTime,name:""});
        // }
        // if(item.eopName == '麦汁进罐'){
        //   stepTimeNj.value.push({startTime: item.startTime,endTime: item.endTime,name:"麦汁进罐"});
        // }
        // if(item.eopName == '快速降温'){
        //   stepTimeNj.value.push({startTime: item.startTime,endTime: item.endTime});
        // }


        // 使用正弦或余弦控制上下浮动
        const amplitude = 40; // 波动幅度（上下各 40）
        const base = 50;      // 中心基线
        const yCenter = base + Math.sin(index * Math.PI / 4) * amplitude;

        const height = 10; // 每个块的高度
        const y1 = yCenter - height / 2;
        const y2 = yCenter + height / 2;
        let startTime = item.startTime || item.eventTime
        let endTime = item.endTime
        let id = item.id
        if (!startTime || !endTime) {
          return; 
        }

        //let eopName = item.bopName
        //替换/
        let eopName = item?.bopName?.split('/')[0];

        // let monitoringTime = getMinutesBetween(startTime, endTime);
        let between = getTimeBetween(startTime, endTime);
        if(index == 0){
          markLineDataArray.push({xAxis:startTime,lineStyle: {color: '#999999',width: 1},id: id});
          markLineDataArray.push({xAxis:endTime,lineStyle: {color: '#999999',width: 1},id: id});
          
          let markArray = []
          markArray.push({xAxis: startTime, name: `${eopName}\n时长：${between.display}`,yAxis: y1 });
          markArray.push({xAxis:endTime, yAxis: y2});
          markAreaDataArray.push(markArray);
        }else{
          markLineDataArray.push({xAxis:startTime,lineStyle: {color: '#999999',width: 1},id: id});
          markLineDataArray.push({xAxis:endTime,lineStyle: {color: '#999999',width: 1},id: id});
          let markArray = []
          markArray.push({xAxis: startTime, name: `${eopName}\n时长：${between.display}` ,yAxis: y1});
          markArray.push({xAxis:endTime ,yAxis: y2});
          markAreaDataArray.push(markArray);
        }
        stepDataArray.value.push([startTime, endTime, between.display, eopName, item.id]);
        
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
    }else{
      information.value.startTime = '';
      information.value.endTime = '';
      information.value.formulaName = ''
      information.value.stepList = []
      queryParam.value.order = ''

      //曲线
      singleBatchRef.value.initEcharts([optionData.value]);
      // 步骤区间
      const formattedTime = dayjs(new Date()).format('YYYY-MM-DD HH:mm:ss')
      singleBatchRef.value.initStepEcharts([[formattedTime,formattedTime,0,"步骤"],]);
      setParam();
    }
    pageLoading.value = false;
  }
  //获取点位数据
  async function devicePointData(){
    if(!pointArray.value || pointArray.value.length == 0){
      singleBatchRef.value.initEcharts([optionData.value])
      return false;
    }
    let points = pointArray.value.join();
    // if(!information.value.startTime || !information.value.endTime){
    //   return false;
    // }
    // if(information.value.startTime =='-' || information.value.endTime =='-'){
    //   return false;
    // }

    let startTime = information.value.startTime;
    let endTime = information.value.endTime;
    // 获取勾选步骤集合
    let stepList = []
    //获取选择的步骤
    const timeRange = singleBatchRef.value.getTimeRange();
    let stepDatas = stepDataArray.value
    if(timeRange && timeRange.startTime && timeRange.endTime){
      stepDatas = [];
      startTime = timeRange.startTime;
      endTime = timeRange.endTime;
      //排除步骤(勾选项)
      stepDatas = stepDataArray.value.filter(item => 
        timeRange.stepState.has(item[4])
      );
       //勾选的完整对象
      stepList = information.value.stepList.filter(item => 
        timeRange.stepState.has(item.id)
      );
    }
    if(!startTime || !endTime || startTime =='-' || endTime =='-'){
      //清空曲线
      // information.value.startTime = '';
      // information.value.endTime = '';
      // information.value.formulaName = ''
      // information.value.stepList = []
      // queryParam.value.order = ''

      //曲线
      singleBatchRef.value.initEcharts([optionData.value]);
      // 步骤区间
      const formattedTime = dayjs(new Date()).format('YYYY-MM-DD HH:mm:ss')
      singleBatchRef.value.initStepEcharts([[formattedTime,formattedTime,0,"步骤"],]);
      singleBatchRef.value.setAxisTimeRange(undefined, undefined);
      setParam();
      return false;
    }
    singleBatchRef.value.setAxisTimeRange(startTime, endTime);

    let obj = {
      startTime,
      endTime,
      points,
      stepList
    }
    let res = await pointData(obj);
    let i = 0;
    curveDataArray.value = [];

    //黑龙江特殊处理-找到温度设定值
    let findSettingKeys = []
    if(res && res?.pointName){
      findSettingKeys = Object.entries(res.pointName)
        .filter(([key, value]) => value.includes('温度设定'))
        .map(([key, value]) => key);
    }



    for (const key in res.pointTimeData) {
      let data = res.pointTimeData[key];

       //黑龙江特殊处理-计算斜率
      // 先复制一份原始数据
      if(stepTimeNj.value && stepTimeNj.value.length > 0 && findSettingKeys && findSettingKeys.length > 0 && findSettingKeys[0] === key){
        let processedData = [...data];
        for(let timeIn = 0;timeIn<stepTimeNj.value.length; timeIn++){
          let times = stepTimeNj.value[timeIn];
          let startTime = times.startTime;
          let endTime = times.endTime;
          let name = times.name
          startTime = getPreviousMinute(startTime)
          // data = processTimeRangeData(data,startTime,endTime)
          processedData = processTimeRangeData(processedData, startTime, endTime, name);
        }
        data = processedData;
        console.log(data)
      }


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
          z: standard? 99:1, //标准显示在上层
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
          z: standard? 99:1,
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
    // singleBatchRef.value.initStepEcharts(stepDataArray.value);
    singleBatchRef.value.initStepEcharts(stepDatas);
  }

  // 获取点位名称
  function getNameByKey(poointts, pointCode) {
    const item = poointts.find(item => item.pointCode === pointCode);
    return item ? item.pointName : null;
  }
  //计算时间差
  function getMinutesBetween(startTime, endTime) {
    const start = new Date(startTime);
    const end = new Date(endTime);
    const diffMs = end - start;
    const diffMinutes = Math.floor(diffMs / (1000 * 60));
    return Math.abs(diffMinutes); // 返回绝对值
  }

  /**
   * 计算时间差，智能返回合适的时间单位
   * @param {string|Date} startTime - 开始时间
   * @param {string|Date} endTime - 结束时间
   * @returns {Object} 包含数值、单位和显示文本的结果对象
   */
  function getTimeBetween(startTime, endTime) {
    const start = startTime instanceof Date ? startTime : new Date(startTime);
    const end = endTime instanceof Date ? endTime : new Date(endTime);

    // 验证日期有效性，返回默认值而非抛出异常
    if (isNaN(start.getTime()) || isNaN(end.getTime())) {
      return { value: 0, unit: 'unknown', display: '--' };
    }

    const diffMs = Math.abs(end - start);
    const diffSeconds = Math.floor(diffMs / 1000);
    const diffMinutes = Math.floor(diffSeconds / 60);
    const diffHours = Math.floor(diffMinutes / 60);
    const diffDays = Math.floor(diffHours / 24);

    // 智能选择最合适的单位显示
    if (diffDays > 0) {
      const remainHours = diffHours % 24;
      const display = remainHours > 0 ? `${diffDays}天${remainHours}小时` : `${diffDays}天`;
      return { value: diffDays, unit: 'days', display };
    }
    if (diffHours > 0) {
      const remainMinutes = diffMinutes % 60;
      const display = remainMinutes > 0 ? `${diffHours}小时${remainMinutes}分钟` : `${diffHours}小时`;
      return { value: diffHours, unit: 'hours', display };
    }
    if (diffMinutes > 0) {
      return { value: diffMinutes, unit: 'minutes', display: `${diffMinutes}分钟` };
    }
    return { value: diffSeconds, unit: 'seconds', display: `${diffSeconds}秒` };
  }

  //参数设置
  function setParam(){
    //配方去除多余/
    if(information.value && information.value.formulaName){
      information.value.formulaName = information.value.formulaName.split('/')[0];
    }
    singleBatchRef.value.setParam(queryParam.value,information.value);
  }
  function searchQuery(){}


  //获取设备
  async function getDeviceData(isFirst){
    if(!isFirst){
      pageLoading.value = true;
      //queryParam.value.areaId = "";
      queryParam.value.unitId = "";
      queryParam.value.pcuNo = "";
      queryParam.value.recipe = undefined;
      queryParam.value.order = undefined
      queryParam.value.batch = undefined;
      batchArr.value = [];
      orderArr.value = [];
      recipeArr.value = [];

      queryParam.value.deviceId = undefined;
      //queryParam.value.areaNo = '';
      //queryParam.value.unitNo = '';
      deviceArr.value = []
    }
    if (!queryParam.value.dateRange || queryParam.value.dateRange.length !== 2) {
      return false;
    }
    if(!queryParam.value.areaId){
      return false;
    }
    let obj = {
      "eventTime": queryParam.value.dateRange[0]+" 00:00:00",
      "endTime": queryParam.value.dateRange[1]+" 23:59:59",
      "areaId": queryParam.value.areaId,
      "unitId": queryParam.value.unitNo
    }
    let res = await getDevice(obj);
    if(res){
       res.forEach((item, index, array) => {
        deviceArr.value.push({
          value: item.id,
          label: `${item.deviceName}`,
          code: item.deviceCode,
          curveDataType: item.curveDataType
        })
      });
    }
    pageLoading.value = false;
  }

  // 下一批0/上一批1
  async function getBatchSearch(pageType){
    if(!information.value.startTime || !queryParam.value.batch){
      return false;
    }
    batchDisabled.value = true
    let obj = {
      //单元开始时间
      startTime: information.value.startTime,
      endTime: information.value.endTime,
      areaId: queryParam.value.areaId,
      unitId: queryParam.value.unitId,
      orderId: queryParam.value.order,
      batchNr: queryParam.value.batch,
      pageType,
    }
    let res = await getBatchByDirection(obj);

    if(res && res.length > 0){
      //显示数据
      queryParam.value.dateRange[0] = dayjs(res[0].eventTime).format('YYYY-MM-DD')
      queryParam.value.dateRange[1] = dayjs(res[0].eventTime).format('YYYY-MM-DD')
      queryParam.value.order = res[0].orderId
      queryParam.value.batch = res[0].batchNr
      //获取批次
      getBatchs();
      //获取数据
      getAllData();
      batchDisabled.value = false
    }else{
      batchDisabled.value = false
      createMessage.warning("当前批次已是最新");
    }
  }

  //保存步骤配置
  async function stepSave(stepIds, excludeIds){
    pageLoading.value = true;
    // await stepsSave({stepIds: stepIds.join(",")});
    await stepsSave({stepIds: stepIds.join(","),excludeIds: excludeIds.join(",")
                    ,curveDataType:curveDataType.value

                    ,unitNo:queryParam.value.deviceNo
                    ,areaNo:queryParam.value.areaNo
                    ,batchNo: queryParam.value.batch

                    ,curveType: curveType.value
                    })
    .then((res) => {
      if (res.success) {
        createMessage.success(res.message);
      } else {
        createMessage.warning(res.message);
      }
    })
    .finally(() => {
      pageLoading.value = false;
    });
    pageLoading.value = false;
  }

  //获取其他参数
  async function getOtherParam(queryParam){
    if(!queryParam.dateRange[0]){
      return false;
    }
    let obj = {
      startTime: queryParam.dateRange[0]+ " 00:00:00",
      orderId: queryParam.order,
      batchNr: queryParam.batch,
      areaId: queryParam.areaId,
    }
    let res = await getOtherData(obj)
    if(res){
      //工艺浓度
      information.value.concentration = res[1];
      //产量
      information.value.output = res[2];
    }
  }

  //获取糖化锅最新批次数据
async function getMashing(){
  //获取下拉数据
  //const options = departSelectRef.value.allOpts;

  let obj = {unitName: '糖化锅'};
  let res = await getMashingData(obj);
  if(res){
    //判断时间是否7天之前,7天之前,开始结束取res.eventTime, 7天内取7天前和当前日期
    queryParam.value.dateRange = getDateRangeByEventTime(res.eventTime);
    //queryParam.value.pcuNos = areaComparison.value[res.pcuNo]
    queryParam.value.deviceId = parseInt(res.unitNames)
    queryParam.value.order = res.orderId
    queryParam.value.batch = res.batchNr

    queryParam.value.areaId = `${res.areaId}`
    queryParam.value.unitId = res.unitId
    // queryParam.value.pcuNo = res.pcuNo
    //获取点位
    deviceChange(queryParam.value.deviceId,true);
    //获取设备
    getDeviceData(true);
    //获取数据
    batchChange();
  }
}
  
/**
 * 根据事件时间设置查询日期范围
 * @param {string} eventTime - 事件时间字符串或 Date 对象
 * @returns {Array} [startDate, endDate]
 */
function getDateRangeByEventTime (eventTime){
  if (!eventTime) {
    // 如果事件时间为空，默认返回最近7天
    const today = dayjs();
    const sevenDaysAgo = today.subtract(7, 'day');
    return [
      sevenDaysAgo.format('YYYY-MM-DD'),
      today.format('YYYY-MM-DD')
    ];
  }
  
  const targetDate = dayjs(eventTime);
  const today = dayjs().endOf('day'); // 今天23:59:59
  const sevenDaysAgo = today.subtract(30, 'day').startOf('day'); // 30天前00:00:00
  
  // 判断目标日期是否在N天之前（不含当天）
  if (targetDate.isBefore(sevenDaysAgo, 'day')) {
    // N天之前：使用事件日期当天
    return [
      targetDate.format('YYYY-MM-DD'),
      targetDate.format('YYYY-MM-DD')
    ];
  } else {
    // N天之内（包含当天）：使用最近7天范围
    return [
      sevenDaysAgo.format('YYYY-MM-DD'),
      today.format('YYYY-MM-DD')
    ];
  }
};

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

function getBatchDispatch(isFirst){
  if(curveDataType.value == '1'){
    getBatchs(isFirst);
  }else{
    getBatchsPi(isFirst);
  }
}

// =========================================== PI报表曲线 相关方法 ===========================================
// 获取批次
async function getBatchsPi(isFirst){

  if(!isFirst){
    queryParam.value.batch = undefined;
  }
  // if(!queryParam.value.order){
  //   return false;
  // }

  batchArr.value = [];
  let obj = {
    "startTime": queryParam.value.dateRange[0]+" 00:00:00",
    "endTime": queryParam.value.dateRange[1]+" 23:59:59",
    //"recipe": queryParam.value.recipe,
    //"order":  queryParam.value.order,
    "areaNo": queryParam.value.areaId,
    "deviceNo": queryParam.value.deviceNo,
    "unitName": queryParam.value.unitName,
    //"curveType": curveType.value
  }
  let res = await getBatchPi(obj);
  if(res){
      res.forEach((item, index, array) => {
      batchArr.value.push({
        value: item.batchNo,
        label: item.batchNo,
        orderNo: item.orderNo
      })
    });
  }
}

//获取数据
  async function getAllDataPi(){
    // let areaNo = "";
    // let reccatNo = "";
    // let recipeNo = "";
    // if(queryParam.value.recipe){
    //   let array = queryParam.value.recipe.split("-");
    //   areaNo = array[0];
    //   reccatNo = array[1];
    //   recipeNo = array[2];
    // }

    if(queryParam.value.batch){
      const item = batchArr.value.find(item => item.value === queryParam.value.batch);
      if(item){
        queryParam.value.order = item.orderNo
      }
    }
    const result = getDateRange(queryParam.value.dateRange[0]);
    let obj = {
      "startTime": result.startDate+" 00:00:00",
      "endTime":  result.endDate+" 23:59:59",
      //"recipe": queryParam.value.recipe,
      "areaNo": queryParam.value.areaId,
      //"unitNo": queryParam.value.unitNo,
      "deviceNo": queryParam.value.deviceNo,
      "unitName": queryParam.value.unitName,
      //"order": queryParam.value.order,
      "batch": queryParam.value.batch,
      "curveType": 1,
      "dataType": "ra"
    }
    if(!queryParam.value.batch){
      obj.batch = "-1"
    }
    let res = await getDataPi(obj);
    if(res){
      information.value.startTime = res.startTime;
      information.value.endTime = res.endTime;
      information.value.formulaName = res.recipeName
      let stepData = res.stepList;

      information.value.stepList = res.stepList
      information.value.curveStepMap = res.curveStepMap

      let markLineDataArray = [];
      let markAreaDataArray = [];
      setParam();
      
      // 生成 0~100 之间的波动值
      //const layerHeight = 20;
      //let maxY = 100;
      stepDataArray.value = [];
      if(!stepData || (stepData && stepData.length === 0)){return false}
      stepData.forEach((item, index, array) => {
        let id = item.id
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
          markLineDataArray.push({xAxis:item.startTime,lineStyle: {color: '#999999',width: 1},id: id});
          markLineDataArray.push({xAxis:item.endTime,lineStyle: {color: '#999999',width: 1},id: id});
          
          let markArray = []
          markArray.push({xAxis: item.startTime, name: `${item.eopName}\n时长：${between.display}`,yAxis: y1 ,id: id});
          markArray.push({xAxis:item.endTime, yAxis: y2});
          markAreaDataArray.push(markArray);
        }else{
          markLineDataArray.push({xAxis:item.startTime,lineStyle: {color: '#999999',width: 1},id: id});
          markLineDataArray.push({xAxis:item.endTime,lineStyle: {color: '#999999',width: 1},id: id});
          let markArray = []
          markArray.push({xAxis: item.startTime, name: `${item.eopName}\n时长：${between.display}` ,yAxis: y1});
          markArray.push({xAxis:item.endTime ,yAxis: y2});
          markAreaDataArray.push(markArray);
        }
        stepDataArray.value.push([item.startTime, item.endTime, between.display, item.eopName, item.id]);
        
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


// =========================================== PI报表曲线 相关方法 End ===========================================








// 黑龙江特殊处理-计算斜率方法 ===========================================================================
/**
 * 处理时间段数据：重新计算指定时间段内的数据，其他数据保持不变
 * @param {Array} data - 原始数据数组 [[时间1, 值1], [时间2, 值2], ...]
 * @param {string} startTime - 开始时间 "YYYY-MM-DD HH:mm:ss"
 * @param {string} endTime - 结束时间 "YYYY-MM-DD HH:mm:ss"
 * @returns {Array} 处理后的数据
 */
function processTimeRangeData(data, startTime, endTime, name) {
  // 1. 数据验证
  if (!Array.isArray(data) || data.length === 0) {
      console.warn('数据为空或格式不正确');
      return data || [];
  }

  //取前一分钟
  let startTimestamp = new Date(startTime).getTime()-(1 * 60 * 1000);
  let endTimestamp = new Date(endTime).getTime() + (1 * 60 * 1000);
  if(name === '麦汁进罐'){
    startTimestamp = new Date(startTime).getTime();
    endTimestamp = new Date(endTime).getTime();
  }

  if (isNaN(startTimestamp) || isNaN(endTimestamp)) {
      console.warn('开始或结束时间格式不正确');
      return data;
  }
  
  if (startTimestamp >= endTimestamp) {
      console.warn('开始时间必须早于结束时间');
      return data;
  }
  
  // 2. 分离数据：时间段内和时间段外
  const dataInRange = [];  // 时间段内的数据
  const dataOutOfRange = []; // 时间段外的数据
  
  data.forEach(item => {
      const itemTime = new Date(item[0]).getTime();
      if (itemTime >= startTimestamp && itemTime <= endTimestamp) {
          //修复：使用数组解构或 Array.from 复制数组
          dataInRange.push([item[0], item[1]]); // 正确复制数组
      } else {
          dataOutOfRange.push([item[0], item[1]]); // 正确复制数组
      }
  });
  
  // 3. 如果时间段内数据不足2个，直接返回原数据
  if (dataInRange.length < 2) {
      console.log('时间段内数据点不足，无法重新计算');
      return data;
  }
  
  // 4. 按时间排序
  dataInRange.sort((a, b) => new Date(a[0]) - new Date(b[0]));
  dataOutOfRange.sort((a, b) => new Date(a[0]) - new Date(b[0]));
  
  // 5. 获取时间段内的第一个和最后一个数据
  let firstData = []
  let lastData = []
  if(name === '麦汁进罐'){
    firstData = dataInRange[0];
    lastData = dataInRange[dataInRange.length - 1];
    firstData[1] = 9.8
    lastData[1] = 9.8
  }else{
    firstData = dataInRange[0];
    lastData = dataInRange[dataInRange.length - 1];
  }
  
  
  // 6. 计算时间差（分钟）和值差
  const startDate = new Date(firstData[0]);
  const endDate = new Date(lastData[0]);
  const timeDiffMinutes = (endDate - startDate) / (1000 * 60);
  const valueDiff = lastData[1] - firstData[1];
  
  if (timeDiffMinutes === 0) {
      console.warn('时间段内开始和结束时间相同');
      return data;
  }
  
  // 7. 计算每分钟的差值
  const valuePerMinute = valueDiff / timeDiffMinutes;
  
  console.log(`计算信息：
    时间段: ${startTime} - ${endTime}
    开始值: ${firstData[1]}
    结束值: ${lastData[1]}
    时间差: ${timeDiffMinutes} 分钟
    值差: ${valueDiff.toFixed(6)}
    每分钟差值: ${valuePerMinute.toFixed(6)}`);
  
  // 8. 重新计算时间段内的数据
  const recalculatedData = [];
  
  // 保留第一个点（作为基准）
  recalculatedData.push([firstData[0], parseFloat(firstData[1].toFixed(6))]);
  
  // 计算后续时间点
  for (let i = 1; i <= timeDiffMinutes; i++) {
      const newTime = new Date(startDate.getTime() + i * 60 * 1000);
      const newValue = firstData[1] + (valuePerMinute * i);
      
      recalculatedData.push([
          formatDateTime(newTime),
          parseFloat(newValue.toFixed(6))
      ]);
  }
  
  // 9. 合并数据：时间段外数据 + 重新计算的时间段内数据
  const finalData = [...dataOutOfRange, ...recalculatedData];
  
  // 10. 按时间排序返回
  return finalData.sort((a, b) => new Date(a[0]) - new Date(b[0]));
}

  /**
   * 格式化时间为字符串
   */
  function formatDateTime(date) {
      const pad = (n) => n.toString().padStart(2, '0');
      return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ` +
            // `${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
             `${pad(date.getHours())}:${pad(date.getMinutes())}:00`;
  }

  // 方法1：使用 Date 对象
  function getPreviousMinute(timeStr) {
    const date = new Date(timeStr);
    date.setMinutes(date.getMinutes() - 1);
    
    // 格式化回字符串
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');
    
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
  }


// 黑龙江特殊处理-计算斜率方法 END===========================================================================
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

// .my-descriptions .ant-descriptions-item-label,
// .my-descriptions .ant-descriptions-item-content {
//   padding: 16px 10px !important;  /* 重写 padding */
// }

// :deep(.compact-descriptions .ant-descriptions-item-label) {
//   padding: 6px 8px;
//   white-space: nowrap;
// }
:deep(.ant-form-item){
  margin-bottom: 7px;
}

</style>
