<template>
  <a-spin :spinning="pageLoading">
  <div style="padding: 5px;height: 88vh;">
    <div style="display: flex;background-color: #ffffff;height: 100%;padding: 5px;justify-content: space-between;">
      <div style="width: 20%;padding: 5px;" class="div-border">
         <a-form layout="vertical">
          <!-- 开始时间 -->
          <a-form-item label="开始时间">
            <a-date-picker v-model:value="form.startDate" style="width: 100%" @change="getRecipes" format="YYYY-MM-DD" valueFormat="YYYY-MM-DD"/>
          </a-form-item>

          <!-- 结束时间 -->
          <a-form-item label="结束时间">
            <a-date-picker v-model:value="form.endDate" style="width: 100%" @change="getRecipes" format="YYYY-MM-DD" valueFormat="YYYY-MM-DD"/>
          </a-form-item>

          <!-- 麦汁品种 -->
          <!-- <a-form-item label="麦汁品种">
            <a-select v-model:value="form.beerType" style="width: 100%">
              <a-select-option value="">请选择</a-select-option>
            </a-select>
          </a-form-item> -->
          <a-form-item label="区域">
            <kt-dict
              ref="pcuDictRef"
              v-model:value="form.areaId"
              placeholder="请选择区域"
              dict="MES3_PI_CURVE_AREA"
              class="width-250 margin-left-10"
              style="width:250px !important;"
              @change="getRecipes"
            />
          </a-form-item>

          <!--设备Pi-->
          <a-form-item label="设备" v-if="curveDataType == '3' && !isReccat">
            <a-select show-search option-filter-prop="label" v-model:value="form.deviceNo"
            :options="deviceDataArr" @change="getPiRecipes()" placeholder="请选择设备" allowClear></a-select>
          </a-form-item>

          <!-- 品种配方 -->
          <a-form-item label="品种配方">
            <a-select show-search option-filter-prop="label" v-model:value="form.recipe" 
              :options="recipeArr" placeholder="请选择配方" allowClear @change="batchData(true)"></a-select>
          </a-form-item>
          <!-- 设备 -->
          <a-form-item label="设备" v-if="curveDataType != '3' && isReccat">
            <a-select show-search mode="multiple" option-filter-prop="label" v-model:value="form.unitId" 
              :options="unitArr" placeholder="请选择设备" allowClear @change="batchData(false)"></a-select>
          </a-form-item>

          
          <!-- 批次号 -->
          <a-form-item>
            <div style="display: flex; align-items: center; justify-content: space-between; width: 100%;">
              <span style="font-weight: 500;">批次号：</span>
              <div>
                <a-checkbox v-model:checked="checkAll" @change="onCheckAllChange">全选</a-checkbox>
                <a-checkbox v-model:checked="invertChecked" @change="onInvertChange">反选</a-checkbox>
              </div>
            </div>
            <div style="border-radius: 4px; padding: 8px; max-height: 200px; overflow-y: auto;margin-top: 5px;" class="div-border">
              <a-checkbox-group v-model:value="form.batchNos">
                <div style="display: grid; grid-template-columns: repeat(2, 1fr); gap: 4px 40px;">
                  <a-checkbox v-for="(tag) in rtBatchArr" :key="tag.id" :value="tag.orderNo+'-'+tag.batchNo">{{ tag.orderNo+'-'+tag.batchNo }}</a-checkbox>
                </div>
              </a-checkbox-group>
            </div>
          </a-form-item>
          <!-- 查询按钮始终在最底部 -->
          <a-form-item>
            <a-button type="primary" block @click="onSearch">查询</a-button>
          </a-form-item>
        </a-form>
      </div>
      <div style="width:79.5%;background-color: #ECECEC;padding: 10px;overflow-y: auto"  class="div-border">
        <MultiBatch ref="multiBatchRef" @chart-change="chart"  @step-save="stepSave" @step-switch="stepSwitch"/>
      </div>

    </div>
  </div>
  </a-spin>
</template>

<script lang="ts" name="pi-multiBatchAnalysisRa" setup>
  import { ref, reactive, onMounted, unref , nextTick} from 'vue';

  import { getRecipe,getRtBatches,unitAndPoint,getPointData,getDevice,stepsSave } from './batchCurveRa.api';
  import { getRecipe as getPiRecipe,getRtBatches as getPiRtBatches,unitAndPoint as unitAndPointPi } from './batchCurvePi.api';
  import { getDepartId } from '/@/utils/ktTool';
  import { useUserStore } from '/@/store/modules/user';
  import MultiBatch from './components/multiBatch.vue';
  import * as echarts from 'echarts';
  import { useI18n } from '/@/hooks/web/useI18n';
  import KtDict from '/@/components/KtDict/KtDict.vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  const { createMessage } = useMessage();

  const { t } = useI18n();
  const multiBatchRef = ref();
  const queryParam = ref<any>({});
  const userStore = useUserStore();
  const dataOption = ref([] as any);
  const recipeArr = ref([] as any);
  const reccatArr = ref([] as any);
  const rtBatchArr = ref([] as any);
  const deviceArr = ref([{unitName:"--",unitNo:'charts',id:'1',points:[]}]);
  const deviceDataArr = ref([] as any);
  const chartRefs = ref<Record<number, HTMLElement>>({})
  //是否发酵
  const isFermentation = ref(false); 
  const devicePointObj = ref({});
  const pcuDictRef = ref();
  const pageLoading = ref(false);
  const unitObj = ref({});
  const isReccat = ref(true);
  //曲线数据类型
  const curveDataType = ref('1');
  //全量设备和步骤
  const allDeviceAndStep = ref({});
  //步骤集合(按设备分组)
  const stepGroupMap = ref({});
  const optionData = ref({
    name: "-",
    type: 'line',
    smooth: true,
    data: [0],
  })
  const xAxisData = ref([1,7,13,19,25,31,37])
  let unitNames = ref('糊化锅,糖化锅,麦汁冷却,前酵罐,麦芽出仓,麦芽粉碎,煮沸锅,沉淀槽,过滤槽,大米粉碎,发酵罐,过滤机,高浓稀释,后酵罐,中间罐');
  const form = ref({
    areaId: "",
    startDate: null,
    endDate: null,
    beerType: null,
    recipe: null,
    batchNos: [],
    recipeId:"",
    unitId: undefined,
    deviceNo: undefined
  });
  const chartForm = ref({});
  const checkAll = ref(false);
  const invertChecked = ref(false);
  const allBatchNos = ref([]);
  const allBatchNoTime = ref({});
  const unitArr = ref([]);
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
    multiBatchRef.value.initEcharts("charts", [optionData.value], xAxisData.value, undefined, undefined, undefined);
    //获取配方
    //getRecipes();
  });

  // 全选
  function onCheckAllChange (e){
    if (e.target.checked) {
      form.value.batchNos = [...allBatchNos.value];
    } else {
      form.value.batchNos = [];
    }
    invertChecked.value = false;
  };

  // 反选
  function onInvertChange(e){
    if (e.target.checked) {
      form.value.batchNos = allBatchNos.value.filter((item) => !form.value.batchNos.includes(item));
    }
    checkAll.value = false;
  };

  function onSearch (){
    // unitAndPoints();
    if(curveDataType.value == '1'){
      unitAndPoints();
    }else{
      unitAndPointsPi();
    }
  };

  //配方类型
  async function getReccatData(){
    curveDataType.value = "1"
    reccatArr.value = [];
    form.value['reccatNo'] = null
    rtBatchArr.value = [];
    recipeArr.value = [];
    form.value['recipe'] = null
    if(!form.value.startDate || !form.value.endDate || !form.value.pcuNo){
      return false;
    }
    pageLoading.value = true;

    // 获取选中项的名称[判断是否是发酵]
    if (form.value.pcuNo && pcuDictRef.value.options) {
      const option = pcuDictRef.value.options.find(
        (item: any) => item.value === form.value.pcuNo
      );
      if (option) {
        //form.value.pcuName = option.text;
        if(option.text == '发酵'){
          isFermentation.value = true;
        }else{
          isFermentation.value = false;
        }
      }
    }
    let obj = {
      "eventTime": form.value.startDate+" 00:00:00",
      "endTime": form.value.endDate+" 23:59:59",
      "pcuNos": form.value.pcuNo,
      "unitNames": unitNames.value,
    }
    let res = await getReccat(obj);
    if(res && res.length > 0){
      isReccat.value = true;
      res.forEach((item, index, array) => {
        reccatArr.value.push({
          value: item.reccatNo,
          label: item.reccatName
        })
      });
    }else{
      //获取PI配方
      isReccat.value = false;
      curveDataType.value = '3'
      // getPiRecipes();
      getDeviceData();
    }
    pageLoading.value = false;
  }

  // 获取配方
  async function getRecipes(){
    if(!form.value.startDate || !form.value.endDate || !form.value.areaId){
      return false;
    }
    curveDataType.value = "1"
    form.value.unitId = undefined;
    
    pageLoading.value = true;
    // 获取选中项的名称[判断是否是发酵]
    if (form.value.areaId && pcuDictRef.value.options) {
      const option = pcuDictRef.value.options.find(
        (item: any) => item.value === form.value.areaId
      );
      if (option) {
        if(option.text.includes('发酵')){
          isFermentation.value = true;
        }else{
          isFermentation.value = false;
        }
      }
    }

    recipeArr.value = [];
    rtBatchArr.value = [];
    form.value.recipe = null;
    let obj = {
      eventTime: form.value.startDate+ " 00:00:00",
      endTime: form.value.endDate+ " 23:59:59",
      unitNames: unitNames.value,
      areaId: form.value.areaId
    }

    let res = await getRecipe(obj);
    debugger
    if(res && res.length > 0){
       res.forEach((item, index, array) => {
        recipeArr.value.push({
          value: `${item.areaId}-${item.recipeId}`,
          label: `${item.recipeName}`,
          name: item.recipeName
        })
      });
    }else{
      //获取PI配方
      isReccat.value = false;
      curveDataType.value = '3'
      getDeviceData();
    }
    pageLoading.value = false;
  }
  
  function batchData(isRecipe){
     if(curveDataType.value === '1'){
      getRtBatch(isRecipe);
    }else{
      getPiRtBatch();
    }
  }
  // 获取批次号
  async function getRtBatch(isRecipe = false){
    rtBatchArr.value = [];
    allBatchNos.value = [];
    allBatchNoTime.value = {};
    form.value.batchNos = [];
    if(!form.value.startDate || !form.value.endDate || !form.value.recipe){
      return false;
    }
    let recipeId = "";
    if(form.value.recipe){
      let array = form.value.recipe.split("-");
      recipeId = array[1];
      form.value.recipeId = recipeId;
    }
    let unitIds = ""
    if(form.value.unitId){
      unitIds = form.value.unitId.join(",")
    }
    let obj = {
      areaId: form.value.areaId,
      eventTime: form.value.startDate+ " 00:00:00",
      endTime: form.value.endDate+ " 23:59:59",
      recipeId: recipeId,
      unitNames: unitNames.value,
      unitId: unitIds
    }
    let res = await getRtBatches(obj);
    if(res){
      rtBatchArr.value = res;
      rtBatchArr.value.forEach((item, index, array) => {
        const batchNo = item.orderNo + '-' + item.batchNo;
        allBatchNos.value.push(batchNo);
        allBatchNoTime.value[batchNo]= (item.startTime || '') +'_'+ (item.endTime || '');
      })
    } 
    if(isRecipe){
      //获取设备
      unitArr.value = [];
      form.value.unitId = undefined;
      let unitData = await getDevice(obj);
      if(unitData){
        unitData.forEach((item, index, array) => {
          // 从 item.deviceCode 中提取最后的数字部分（取横杠后面的部分）
          const codeParts = item.deviceCode.split('-');
          const lastPart = codeParts.length > 1 ? codeParts[codeParts.length - 1] : item.deviceCode;
          unitArr.value.push({
            value: lastPart,
            label: item.deviceName,
          })
        });
      }
    }

  }

  //获取设备和设备对应的点位
  async function unitAndPoints(){
    
    // let reccatNo = "";
    // let recipeId = "";
    // if(form.value.recipe){
    //   let array = form.value.recipe.split("-");
    //   // reccatNo = array[0];
    //   recipeId = array[1];
    // }

    if (!form.value.batchNos || form.value.batchNos.length === 0) {
      return false;
    }
    pageLoading.value = true;
    //添加批次时间段
    let orderAndBatch = form.value.batchNos.map(item => 
    item + "&" + allBatchNoTime.value[item]
    ).join(',');
    let unitIds = ""
    if(form.value.unitId){
      unitIds = form.value.unitId.join(",")
    }
    let obj = {
      areaId: form.value.areaId,
      unitNames: unitNames.value,
      orderAndBatch,
      recipeId: form.value.recipeId,
      unitId: unitIds,
    }
    //设备点位
    let res = await unitAndPoint(obj);
    if(res){
      if(!res.unitList || res.unitList.length === 0){
        deviceArr.value = [{unitName:"--",unitNo:'charts',id:'1',points:[]}]
        multiBatchRef.value.setDevice(deviceArr.value);
      }else{
        deviceArr.value = res.unitList;
        allDeviceAndStep.value = res.stepGrouped
        stepGroupMap.value = res.stepGroupMap || {};

        let stepData = res.stepGroupMap;
        let standardStepListMap = res.standardStepListMap;
        //if(standardStepListMap){
          //stepData = standardStepListMap;
        //}


        //设置device参数
        multiBatchRef.value.setDevice(deviceArr.value, stepData, res.configStepsMap,standardStepListMap, isFermentation.value);
        batchTime.value.startTime = res.startTime
        batchTime.value.endTime = res.endTime

        res.unitList.forEach(unit => {
            const unitNo = unit.unitNo;
            unit.points.forEach(point => {
                const key = `${unitNo}_${point.pointCode}`;
                try {
                    if(point.orderBatchJson){
                      devicePointObj.value[key] = JSON.parse(point.orderBatchJson);
                    }else if(point.standardIds){
                      devicePointObj.value[key] = [{standardids: point.standardIds, pointCode: point.pointCode}];
                    }else{
                      devicePointObj.value[key] =[{}]
                    }
                } catch (error) {
                    devicePointObj.value[key] = point.orderBatchJson;
                }
                unitObj.value[point.pointName] = point.unit;
            });
        });

      }
      nextTick(() => {
        pageLoading.value = false;
        //deviceArr.value.forEach((item, index, array) => {
          //multiBatchRef.value.initEcharts(item.unitNo, [optionData.value], xAxisData.value,unitObj.value)
        //})
      });

    }
  }
  //选择点位
  async function chart(unitNo, pointArray,chartForm,deviceCode){
    let points = chartForm[unitNo].join();
    let timeUnit = "分钟"
    if(isFermentation.value){
      timeUnit ="小时"
    }
    if(!points){
      multiBatchRef.value.initEcharts(unitNo,[optionData.value], xAxisData.value,unitObj.value,timeUnit, deviceCode)
      return false;
    }
    let pointObj = [];
    //点位-订单批次对照
    chartForm[unitNo].forEach(point => {
      const key = `${unitNo}_${point}`;
      if(devicePointObj.value[key] && devicePointObj.value[key].length > 0){
        // 遍历数组，为每个元素添加 pointCode
        const pointsWithCode = devicePointObj.value[key].map(item => ({
          ...item,
          pointCode: point
        }));
        
        // 将处理后的数组推送到 pointObj
        pointObj.push(...pointsWithCode);
      }
    })

    //console.log(JSON.stringify(pointObj)+"=======================");
    // 根据步骤勾选状态过滤 stepList
    const allSteps = stepGroupMap.value[deviceCode] || [];
    let excludeIds = multiBatchRef.value.getExcludeIds(deviceCode, 2);
    const filteredSteps = allSteps.filter(step => !excludeIds.includes(step.stepNr));
    let obj = {
      //包含点位 订单-批次 批次开始和结束时间
      points:JSON.stringify(pointObj),
      //startTime: batchTime.value.startTime,
      //endTime: batchTime.value.endTime,
      isFermentation: isFermentation.value,
      //传递当前设备的步骤集合，用于标准曲线开始时间匹配
      stepList: filteredSteps
    }
    //获取点位数据
    let res = await getPointData(obj);
    let datas= [];
    for (const key in res.pointData) {
      let data = res.pointData[key];
      if(!data || data.length == 0){
        data = optionData.value.data
      }
      //点位名称
      let name = getNameByKey(pointArray, key);
      let keywords = ["标准","设定","目标"]
      const solidLine = keywords.some(keyword => name.includes(keyword));
      //判断液位/压力(小刻度) 温度
      let standard = name?.includes("液位") || name?.includes("压力");
      let eObj = {
          name: name,
          type: 'line',
          smooth: true,
          symbol: 'none',  // 去掉所有数据点的符号
          yAxisIndex: standard?1:0,
          data: data,
          lineStyle: {
              type: solidLine?'dashed':'solid',  // 实线
              width: 2
          }
      }
      datas.push(eObj);
    };
    multiBatchRef.value.initEcharts(unitNo,datas,res.intervals,unitObj.value,timeUnit,deviceCode)
  }
  
  // 获取点位名称
  function getNameByKey(poointts, pointStr) {
    let pointArr = pointStr.split("-")
    if(pointArr && pointArr.length == 3){
      let pointCode = pointArr[2]
      const item = poointts.find(item => item.pointCode === pointCode);
      if(item){
        // if(item.dataAttribute === '1'){
        //   return item ? item.pointName : null;
        // }else{
        return item ? item.pointName+"-"+pointArr[0]+"-"+pointArr[1] : "";
        //}
      }
    }else{
      let pointCode = pointArr[0]
      const item = poointts.find(item => item.pointCode === pointCode);
      if(item){
        return item ? item.pointName : ''; 
      }
    }
    
  }

  //****************************PI ********************************** */
    // 获取配方
  async function getPiRecipes(){
    recipeArr.value = [];
    form.value['recipe'] = null
    rtBatchArr.value = [];
    if(!form.value.startDate || !form.value.endDate){
      return false;
    }
    let obj = {
      "startTime": form.value.startDate+" 00:00:00",
      "endTime": form.value.endDate+" 23:59:59",
      "areaNo": form.value.areaId,
      "deviceNo": form.value.deviceNo,
      //"deviceNo": queryParam.value.deviceNo,
      //"unitNames": unitNames.value,
      //"curveType": curveType.value
    }
    let res = await getPiRecipe(obj);
    if(res){
       curveDataType.value = "3"
       res.forEach((item, index, array) => {
        recipeArr.value.push({
          value: item.recipe,
          label: item.recipe,
          name: item.recipe
        })
      });
    } 
  }
  
  async function getDeviceData(){

    pageLoading.value = true;
    // queryParam.value.areaNo = "";
    // queryParam.value.unitNo = "";
    //queryParam.value.pcuNo = "";
    // queryParam.value.recipe = undefined;
    // queryParam.value.order = undefined
    // queryParam.value.batch = undefined;
    // batchArr.value = [];
    // orderArr.value = [];
    // recipeArr.value = [];

    queryParam.value.deviceId = undefined;
    //queryParam.value.areaNo = '';
    //queryParam.value.unitNo = '';
    // deviceArr.value = []
    
    // if (!queryParam.value.dateRange || queryParam.value.dateRange.length !== 2) {
    //   return false;
    // }
    // if(!queryParam.value.pcuNos){
    //  return false;
    // }
    let obj = {
      "startTime": form.value.startDate+" 00:00:00",
      "endTime": form.value.endDate+" 23:59:59",
      // "lineCode": queryParam.value.lineCode,
      "areaId": form.value.areaId,
      //"unitId": queryParam.value.unitNo
    }
    let res = await getDevice(obj);
    debugger
    if(res){
      res.forEach((item, index, array) => {
        deviceDataArr.value.push({
          //value: item.id,
          // label: `${item.deviceName}(${item.deviceCode})`,
          value: item.deviceCode,
          label: `${item.deviceName}`,
          //code: item.deviceCode,
          curveDataType: item.curveDataType
        })
      });
    }
    pageLoading.value = false;
  }
  
  // 获取批次号
  async function getPiRtBatch(){
    rtBatchArr.value = [];
    allBatchNos.value = [];
    allBatchNoTime.value = {};
    form.value.batchNos = [];
    if(!form.value.startDate || !form.value.endDate || !form.value.recipe){
      return false;
    }
    // let reccatNo = "";
    let recipeNo = "";
    let areaNo = "";
    if(form.value.recipe){
      let array = form.value.recipe.split("-");
      // reccatNo = array[0];
      recipeNo = array[1];
      areaNo = array[3];
    }
    //配方名称
    const recipeObj = recipeArr.value.find(item => item.value === form.value.recipe);
    form.value.recipeName = recipeObj.name;
    let obj = {
      "startTime": form.value.startDate+ " 00:00:00",
      "endTime": form.value.endDate+ " 23:59:59",
      "recipe": form.value.recipe,
      //"unitNames": unitNames.value,
      "recipeName" : form.value.recipeName,
      //"curveType": curveType.value,
      "areaNo": form.value.areaId,
      "deviceNo": form.value.deviceNo,
    }
    let res = await getPiRtBatches(obj);
    if(res){
      rtBatchArr.value = res;
      rtBatchArr.value.forEach((item, index, array) => {
        const batchNo = item.order + '-' + item.batch;
        allBatchNos.value.push(batchNo);
        allBatchNoTime.value[batchNo]= item.startTime+'_'+item.endTime;
      })

    } 
  }
  //获取设备和设备对应的点位
  async function unitAndPointsPi(){
    if (!form.value.batchNos || form.value.batchNos.length === 0) {
      return false;
    }
    //添加批次时间段
    let orderAndBatch = form.value.batchNos.map(item => 
      item + "&" + allBatchNoTime.value[item]
    ).join(',');

    let obj = {
      unitNames: unitNames.value,
      orderAndBatch,
      "recipe" : form.value.recipe,
      "recipeName" : form.value.recipeName,
      // "curveType": curveType.value,
      "areaNo": form.value.areaId,
      "deviceNo": form.value.deviceNo,
    }
    let res = await unitAndPointPi(obj);
    if(res){
      if(!res.unitList || res.unitList.length === 0){
        deviceArr.value = [{unitName:"--",unitNo:'charts',id:'1',points:[]}]
        multiBatchRef.value.setDevice(deviceArr.value);
      }else{
        res.unitList.forEach((unit,index) => {
          //unit.unitNo = `${unit.unitNo}_${index}`
          unit.deviceCode = `${unit.unitNo}`
        })
        deviceArr.value = res.unitList;
        allDeviceAndStep.value = res.adapterStepGrouped
        stepGroupMap.value = res.stepGroupMap || {};
        //设置device参数
        multiBatchRef.value.setDevice(deviceArr.value, res.stepGroupMap, res.configStepsMap, res.standardStepListMap, isFermentation.value);
        batchTime.value.startTime = res.startTime
        batchTime.value.endTime = res.endTime


        // res.unitList.forEach(unit => {
        //     const unitNo = unit.unitNo;
        //     unit.points.forEach(point => {
        //         const key = `${unitNo}_${point.pointCode}`;
        //         devicePointObj.value[key] = point.orderBatchJson;
        //     });
        // });
        res.unitList.forEach(unit => {
            const unitNo = unit.unitNo;
            unit.points.forEach(point => {
                const key = `${unitNo}_${point.pointCode}`;
                try {
                  if(point.orderBatchJson){
                    devicePointObj.value[key] = JSON.parse(point.orderBatchJson);
                  }else{
                     devicePointObj.value[key] =[{}]
                  }
                } catch (error) {
                    devicePointObj.value[key] = point.orderBatchJson;
                }
                unitObj.value[point.pointName] = point.unit;
            });
        });

      }
      nextTick(() => {
        pageLoading.value = false;
        deviceArr.value.forEach((item, index, array) => {
          let timeUnit = "分钟"
          if(isFermentation){
            timeUnit ="小时"
          }
          //multiBatchRef.value.initEcharts(item.unitNo, [optionData.value], xAxisData.value, unitObj.value, timeUnit)
        })
       // deviceArr.value.forEach((item, index, array) => {
          //multiBatchRef.value.initEcharts(item.unitNo, [optionData.value], xAxisData.value, unitObj.value)
       // })
      });

    }
  }


  //保存步骤配置
  async function stepSave(stepIds, excludeIds, deviceCode = ''){
    pageLoading.value = true;
    let obj = {stepIds: stepIds.join(","),excludeIds: excludeIds.join(",")
      ,curveDataType:curveDataType.value
    }
    if(deviceCode){
      //更新
      obj.areaNo = deviceCode.split("-")[0];
      // 统一unitNo格式为 areaId-unitId，与singleBatch和加载查询保持一致
      obj.unitNo = deviceCode;
    }
    if(curveDataType.value != '1'){
      obj.unitNo = form.value.deviceNo
    }

    await stepsSave(obj)
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

  //步骤切换
  function stepSwitch(unitNo, pointArray,chartForm,deviceCode){
    // curveDataArray.value =[]
    // // stepDataArray.value =[]
    // pointArray.value = [];
    // pointArray.value = points;
    // //获取点位数据
    // devicePointData();
  
    //1、更新子页面下拉点位的json(开始时间和结束时间)
    let orderBatchSteps = allDeviceAndStep.value[deviceCode]
    //要排除的步骤 stepNr
    let excludeIds = multiBatchRef.value.getExcludeIds(deviceCode, 2)

    const orderBatchTime = Object.entries(orderBatchSteps).reduce(
      (result, [orderBatchKey, steps]) => {
        // 1过滤掉要排除的步骤
        const validSteps = steps
          .filter(step => !excludeIds.includes(step.stepNr))
          // 保证按时间或 stepNr 排序（很重要）
          .sort((a, b) => a.stepNr - b.stepNr);

        // 2过滤后可能为空，防御一下
        if (validSteps.length === 0) {
          return result;
        }

        // 3取首尾
        const firstStep = validSteps[0];
        const lastStep  = validSteps[validSteps.length - 1];
        result[orderBatchKey] = {
          startDate: firstStep.eventTime, // 或 firstStep.startTime
          endDate: lastStep.endTime
        };
        return result;
      },
      {}
    );
    
    
    //更新父页面点位
    chartForm[unitNo].forEach(point => {
      const key = `${unitNo}_${point}`;
      if(devicePointObj.value[key] && devicePointObj.value[key].length > 0){
        // 遍历数组，为每个元素添加 pointCode
        // const pointsWithCode = devicePointObj.value[key].map(item => ({
        //   ...item,
        //   pointCode: point
        // }));
        // 将处理后的数组推送到 pointObj
        //pointObj.push(...pointsWithCode);

        //devicePointObj.value[key]= ['2026-02-01 12:40:45','2026-02-01 14:18:47']
        // let newStart = '2026-02-01 13:40:45';
        // let newEnd = '2026-02-01 14:18:47';
        // devicePointObj.value[key] = devicePointObj.value[key].map(item => ({
        //   ...item,
        //   startDate: newStart,
        //   endDate: newEnd
        // }))
        devicePointObj.value[key] = devicePointObj.value[key].map(item => {
          const time = orderBatchTime[item.orderBatch];
          // 如果找不到对应时间，保持原值（防御）
          if (!time) {
            return item;
          }

          return {
            ...item,
            startDate: time.startDate,
            endDate: time.endDate
          };
        });
      }
    })

    //触发父页面图表更新
    chart(unitNo, pointArray,chartForm,deviceCode)
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
</style>