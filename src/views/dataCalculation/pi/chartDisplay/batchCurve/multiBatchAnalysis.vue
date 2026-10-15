<template>
  <a-spin :spinning="pageLoading">
  <div style="padding: 5px;height: 88vh;">
    <div style="display: flex;background-color: #ffffff;height: 100%;padding: 5px;justify-content: space-between;">
      <div style="width: 20%;padding: 5px;" class="div-border">
         <a-form layout="vertical">
          <!-- 开始时间 -->
          <a-form-item label="开始时间">
            <a-date-picker v-model:value="form.startDate" style="width: 100%" @change="getReccatData" format="YYYY-MM-DD" valueFormat="YYYY-MM-DD"/>
          </a-form-item>
          <!-- 结束时间 -->
          <a-form-item label="结束时间">
            <a-date-picker v-model:value="form.endDate" style="width: 100%" @change="getReccatData" format="YYYY-MM-DD" valueFormat="YYYY-MM-DD"/>
          </a-form-item>
          <!--区域-->
          <a-form-item label="区域">
            <kt-dict
              ref="pcuDictRef"
              v-model:value="form.pcuNo"
              placeholder="请选择区域"
              dict="MES3_PI_CURVE_AREA"
              style="width:250px !important;"
              @change="getReccatData"
            />
          </a-form-item>

          <!--设备Pi-->
          <a-form-item label="设备" v-if="curveDataType == '3' && !isReccat">
            <a-select show-search option-filter-prop="label" v-model:value="form.deviceNo"
            :options="deviceDataArr" @change="getPiRecipes()" placeholder="请选择设备" allowClear></a-select>
          </a-form-item>
  

          <!--配方类型-->
          <a-form-item label="配方类型" v-if="curveDataType != '3' && isReccat">
            <a-select show-search option-filter-prop="label" v-model:value="form.reccatNo" 
              :options="reccatArr" placeholder="请选择配方类型" allowClear @change="getRecipes"></a-select>
          </a-form-item>
          <!-- 麦汁品种 -->
          <!-- <a-form-item label="麦汁品种">
            <a-select v-model:value="form.beerType" style="width: 100%">
              <a-select-option value="">请选择</a-select-option>
            </a-select>
          </a-form-item> -->

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
      <div style="width:79.5%;background-color: #ECECEC;padding: 10px;overflow-y: auto" class="div-border">
        <MultiBatch ref="multiBatchRef" @chart-change="chart" @step-save="stepSave" @step-switch="stepSwitch"/>
        <!-- <a-card v-for="(tag) in deviceArr" :key="tag.id" :bordered="false" style="width: 100%;height: 49%;">
          <template #title>
            <div style="display: flex; align-items: center; gap: 8px;">
              <span>{{ tag.unitName }}</span>
              <a-select show-search option-filter-prop="label" v-model:value="chartForm[tag.unitNo]"
              :options="tag.points" placeholder="请选择" allowClear @change="chart(tag.unitNo, tag.points)" 
              :field-names="{ label: 'pointName', value: 'pointCode'}" style="width: 230px;" mode="multiple" :maxTagCount="1"></a-select>
            </div>
          </template>
          <div :ref="el => chartRefs[tag.unitNo] = el" style="width:100%;height:300px;"></div>
        </a-card> -->

      </div>

    </div>
  </div>
  </a-spin>
</template>

<script lang="ts" name="pi-multiBatchAnalysis" setup>
  import { ref, reactive, onMounted, unref , nextTick} from 'vue';

  import { getRecipe,getRtBatches,unitAndPoint,getPointData,getReccat,getDevice,stepsSave } from './batchCurve.api';
  import { getRecipe as getPiRecipe,getRtBatches as getPiRtBatches,unitAndPoint as unitAndPointPi } from './batchCurvePi.api';
  import { getDepartId } from '/@/utils/ktTool';
  import { useUserStore } from '/@/store/modules/user';
  import MultiBatch from './components/multiBatch.vue';
  import KtDict from '/@/components/KtDict/KtDict.vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import * as echarts from 'echarts';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { createMessage } = useMessage();

  const { t } = useI18n();
  const multiBatchRef = ref();
  const queryParam = ref<any>({});
  const userStore = useUserStore();
  const dataOption = ref([] as any);
  const recipeArr = ref([] as any);
  const reccatArr = ref([] as any);
  const rtBatchArr = ref([] as any);
  const unitArr = ref([]);
  const deviceArr = ref([{unitName:"--",unitNo:'charts',id:'1',points:[]}]);
  const deviceDataArr = ref([] as any);
  const chartRefs = ref<Record<number, HTMLElement>>({})
  //是否发酵
  const isFermentation = ref(false); 
  const devicePointObj = ref({});
  const unitObj = ref({});
  const pcuDictRef = ref();
  const pageLoading = ref(false);
  const isReccat = ref(true);
  //曲线数据类型
  const curveDataType = ref('1');
  //全量设备和步骤
  const allDeviceAndStep = ref({});
  const optionData = ref({
    name: "-",
    type: 'line',
    smooth: true,
    data: [0],
  })
  const xAxisData = ref([1,7,13,19,25,31,37])
  //const unitNames = ref('糖化锅,麦芽粉碎,过滤槽,煮沸锅,沉淀槽,麦汁冷却,立罐,卧罐,发酵罐,大米粉碎,糊化锅,过滤机,高浓稀释');
  const unitNames = ref('糖化锅,粉碎,过滤槽,煮沸,沉淀槽,麦汁冷却,立罐,卧罐,发酵罐,大米粉碎,糊化锅,过滤机,高浓稀释,罐,AFT');
  // const unitNames = ref('糊化锅');
  const form = ref({
    startDate: null,
    endDate: null,
    beerType: null,
    recipe: null,
    batchNos: [],
    pcuNo: undefined,
    reccatNo: null,
    recipeName: null,
    unitId: undefined,
    deviceNo: undefined
  });
  const chartForm = ref({});
  const checkAll = ref(false);
  const invertChecked = ref(false);
  const allBatchNos = ref([]);
  const allBatchNoTime = ref({});
  //特殊处理-记录步骤和时间范围 (设备:[{},{}],设备2:[{},{}])
  const standardStepRanges = ref<Record<string, any[]>>({});

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
    multiBatchRef.value.initEcharts("charts", [optionData.value], xAxisData.value, undefined, undefined);
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
    pageLoading.value = true;
    recipeArr.value = [];
    form.value['recipe'] = null
    rtBatchArr.value = [];
    if(!form.value.startDate || !form.value.endDate || !form.value.pcuNo){
      pageLoading.value = false;
      return false;
    }
    form.value.unitId = undefined;

    let obj = {
      "eventTime": form.value.startDate+" 00:00:00",
      "endTime": form.value.endDate+" 23:59:59",
      "pcuNos": form.value.pcuNo,
      "reccatNo": form.value.reccatNo,
      "unitNames": unitNames.value,
    }
    let res = await getRecipe(obj);
    if(res && res.length > 0){
       res.forEach((item, index, array) => {
        recipeArr.value.push({
          value: item.reccatNo+'-'+item.recipeNo+'-'+item.pcuNo+'-'+item.areaNo,
          //label: item.recipeName+'('+item.areaNo+'-'+item.recipeNo+')',
          label: item.recipeName,
          name: item.recipeName
        })
      });
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

    let unitIds = ""
    if(form.value.unitId){
      unitIds = form.value.unitId.join(",")
    }
    let obj = {
      areaNo: areaNo,
      "eventTime": form.value.startDate+ " 00:00:00",
      "endTime": form.value.endDate+ " 23:59:59",
      "recipeNo": recipeNo,
      "pcuNos": form.value.pcuNo,
      "unitNames": unitNames.value,
      "reccatNo": form.value.reccatNo,
      "recipeName" : form.value.recipeName,
      unitId: unitIds
    }
    let res = await getRtBatches(obj);
    if(res){
      rtBatchArr.value = res;
      rtBatchArr.value.forEach((item, index, array) => {
        const batchNo = item.orderNo + '-' + item.batchNo;
        allBatchNos.value.push(batchNo);
        allBatchNoTime.value[batchNo]= item.startTime+'_'+item.endTime;
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
          //const codeParts = item.deviceCode.split('-');
          //const lastPart = codeParts[1];
          unitArr.value.push({
            value: item.deviceCode,
            label: item.deviceName,
          })
        });
      }
    }
  }

  //获取设备和设备对应的点位
  async function unitAndPoints(){
    
    // let reccatNo = "";
    let recipeNo = "";
    let areaNo = "";
    if(form.value.recipe){
      let array = form.value.recipe.split("-");
      // reccatNo = array[0];
      recipeNo = array[1];
      areaNo = array[3];
    }

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
      areaNo,
      unitNames: unitNames.value,
      orderAndBatch,
      recipeNo,
      "pcuNos": form.value.pcuNo,
      "reccatNo": form.value.reccatNo,
      "recipeName" : form.value.recipeName,
      unitId: unitIds
    }
    let res = await unitAndPoint(obj);
    if(res){
      if(!res.unitList || res.unitList.length === 0){
        deviceArr.value = [{unitName:"--",unitNo:'charts',id:'1',points:[]}]
        multiBatchRef.value.setDevice(deviceArr.value);
      }else{
        res.unitList.forEach((unit,index) => {
          unit.unitNo = `${unit.unitNo}_${index}`
        })
        deviceArr.value = res.unitList;
        allDeviceAndStep.value = res.adapterStepGrouped
        //设置device参数
        multiBatchRef.value.setDevice(deviceArr.value, res.stepGroupMap, res.configStepsMap,res.stepGroupMap, isFermentation.value);
        batchTime.value.startTime = res.startTime
        batchTime.value.endTime = res.endTime
        let partialStandardUnitMap = res.partialStandardMap;
        let configStepsMap = res.configStepsMap;
        // 匹配局部标准
        standardStepRanges.value = {}
        if(res.stepGroupMap && partialStandardUnitMap){
          // res.stepGroupMap 结构 {'1-2-3': [{ropId, eopNo, startTime, endTime}, ...]}
          // partialStandardUnitMap 结构 {'1-2-3': [{ropId, eopNo, initialValue, endValue}, ...]}
          // 遍历 stepGroupMap，按 unitKey 分组匹配 partialStandardUnitMap
          Object.entries(res.stepGroupMap).forEach(([unitKey, stepItems]: [string, any]) => {
            // 获取 partialStandardUnitMap[unitKey] 得到 partialStandards
            // 判断 key 结构：包含 '-' 为 '1-2-3' 格式，否则为单个 id
            let partialStandards;
            if(unitKey.includes('-')){
              partialStandards = partialStandardUnitMap[unitKey];
            } else {
              // 单个 id，遍历 partialStandardUnitMap 找匹配的 key
              const matchKey = Object.keys(partialStandardUnitMap).find(k => k.split('-').includes(unitKey));
              partialStandards = matchKey ? partialStandardUnitMap[matchKey] : null;
            }
            if(!partialStandards || partialStandards.length === 0) return;
            // 构建 ropId-eopNo -> {initialValue, endValue} 的对照索引
            const partialStandardMap: Record<string, {initialValue: string; endValue: string}> = {};
            partialStandards.forEach((item: any) => {
              const key = `${item.ropId}-${item.eopNo}`;
              partialStandardMap[key] = {
                initialValue: item.initialValue,
                endValue: item.endValue,
              };
            });
            const standardStepRangesArray: any[] = [];
            //第一步
            let firstStep: any = null;
            // 遍历 stepItems，通过 ropId-eopNo 匹配标准
            stepItems.forEach((item: any) => {
              //过滤configStepsMap，判断item.id是否在配置的id列表中
              const configStepIds = configStepsMap ? configStepsMap[unitKey] : null;
              if (configStepIds && (configStepIds as any[]).length > 0 && !configStepIds.includes(item.id)) return;

              if(!firstStep){
                firstStep = item;
              }
              const key = `${item.ropId}-${item.eopNo}`;
              const standard = partialStandardMap[key];
              if(standard){
                const firstStepStartTime = new Date(firstStep.startTime).getTime();
                //const divisor = isFermentation.value ? 3600000 : 60000;
                standardStepRangesArray.push({
                  //时长-开始(第一步开始计算到开始时间)
                  startTime: (new Date(item.startTime).getTime() - firstStepStartTime),
                  //时长-结束(第一步开始计算到结束时间)
                  endTime: (new Date(item.endTime).getTime() - firstStepStartTime),

                  initialValue: standard.initialValue != null && standard.initialValue !== '' ? Number(standard.initialValue) : '',
                  endValue: standard.endValue != null && standard.endValue !== '' ? Number(standard.endValue) : '',
                });
              }
            });
            standardStepRanges.value[unitKey] = standardStepRangesArray
          });
        }
        

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
                  }else if(point.standardIds){
                    devicePointObj.value[key] = [{standardids: point.standardIds, pointCode: point.pointCode}];
                  }else{
                     devicePointObj.value[key] =[{}]
                  }
                } catch (error) {
                    devicePointObj.value[key] = point.orderBatchJson;
                }
                unitObj.value[point.pointName] = point.unit?point.unit:"";
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
      multiBatchRef.value.initEcharts(unitNo,[optionData.value], xAxisData.value, unitObj.value,timeUnit,timeUnit)
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

    
    console.log(JSON.stringify(pointObj)+"=======================");
    let obj = {
      points:JSON.stringify(pointObj),
      startTime: batchTime.value.startTime,
      endTime: batchTime.value.endTime,
      isFermentation: isFermentation.value
    }
    //获取点位数据
    let res = await getPointData(obj);
    let datas= [];
    for (const key in res.pointData) {
      let data = res.pointData[key];
      if(!data || data.length == 0){
        data = optionData.value.data
      }
      //处理标准
      //判断 key 结构不是 1-2-2323 为 32423
      //let standardRanges: any[] = [];
      if(!key.includes('-')){
        let standardSteps = standardStepRanges.value[deviceCode]
        let processedData = [...data];
        for(let timeIn = 0; timeIn < standardSteps.length; timeIn++){
          let times = standardSteps[timeIn];
          let startTime = times.startTime;
          let endTime = times.endTime;
          let initialValue = times?.initialValue;
          let endValue = times?.endValue;

          //startTime = getPreviousMinute(startTime)
          // data = processTimeRangeData(data,startTime,endTime)
          processedData = processTimeRangeData(processedData, startTime, endTime,initialValue, endValue);
        }
        data = processedData;
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
    // let timeUnit = "分钟"
    // if(isFermentation.value){
    //   timeUnit ="小时"
    // }
    multiBatchRef.value.initEcharts(unitNo,datas,res.intervals, unitObj.value,timeUnit,deviceCode)
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
      "areaNo": form.value.pcuNo,
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
    deviceDataArr.value = [];
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
      "pcuNos": form.value.pcuNo,
      //"unitId": queryParam.value.unitNo
    }
    let res = await getDevice(obj);
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
      "areaNo": form.value.pcuNo,
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
      "areaNo": form.value.pcuNo,
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
        //设置device参数
        // multiBatchRef.value.setDevice(deviceArr.value);
         //设置device参数
        multiBatchRef.value.setDevice(deviceArr.value, res.stepGroupMap, res.configStepsMap, isFermentation.value);
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
  //设备变更
  function deviceChange(){

  }


  //保存步骤配置
  async function stepSave(stepIds, excludeIds , deviceCode=''){
    pageLoading.value = true;
    let obj = {stepIds: stepIds.join(",")
                , excludeIds: excludeIds.join(",")
                ,curveDataType:curveDataType.value
                //,areaNo:form.value.areaNo
                ,pcuNo:form.value.pcuNo
                //,batchNo: queryParam.value.batch
                //,curveType: curveType.value
              }
    if(deviceCode){
      //更新pcuNo
      obj.pcuNo = deviceCode.split("-")[2];
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
    //1、更新子页面下拉点位的json(开始时间和结束时间)
    let orderBatchSteps = allDeviceAndStep.value[deviceCode]
    //要排除的步骤 stepNr
    let excludeIds = multiBatchRef.value.getExcludeIds(deviceCode, 1)
    const orderBatchTime = Object.entries(orderBatchSteps).reduce(
      (result, [orderBatchKey, steps]) => {
        // 1过滤掉要排除的步骤
        const validSteps = steps
          .filter(step => !excludeIds.includes(step.ropId))
          // 保证按时间或 stepNr 排序（很重要）
          .sort((a, b) => a.ropId - b.ropId);

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





  // 计算斜率方法 ===========================================================================

  /**
 * 处理时间段数据：重新计算指定时间段内的数据，其他数据保持不变
 * @param {Array} data - 原始数据数组 [[时间1, 值1], [时间2, 值2], ...]
 * @param {string} startTime - 第一步开始时间到当前步骤开始时间差
 * @param {string} endTime - 第一步开始时间到当前步骤结束时间差
 * @returns {Array} 处理后的数据
 */
function processTimeRangeData(data, startTime, endTime, initialValue=undefined, endValue=undefined) {
  // 1. 数据验证
  if (!Array.isArray(data) || data.length === 0) {
      console.warn('数据为空或格式不正确');
      return data || [];
  }
  // data结构为 [{cnlNum: "600138", time: "2026-04-03T13:00:00Z", value: 12}]

  // 取第一个元素的时间作为基准
  const baseTime = new Date(data[0].time).getTime();
  const startOffset = Number(startTime);
  const endOffset = Number(endTime);

  // startTime/endTime 为相对于第一步开始时间的时长差（毫秒）
  const startTimestamp = baseTime + startOffset;
  const endTimestamp = baseTime + endOffset;

  if (isNaN(baseTime)) {
      console.warn('数据时间格式不正确');
      return data;
  }

  if (!Number.isFinite(startOffset) || !Number.isFinite(endOffset) || isNaN(startTimestamp) || isNaN(endTimestamp)) {
      console.warn('开始或结束时长格式不正确');
      return data;
  }

  if (startTimestamp >= endTimestamp) {
      return data;
  }

  const isFermen = isFermentation.value;
  const timeUnitMs = isFermen ? 3600000 : 60000;
  // step 边界通常不一定刚好落在采样点上，给半个采样单位容差避免漏匹配
  const tolerance = timeUnitMs / 2;
  const matchStartTimestamp = startTimestamp - tolerance;
  const matchEndTimestamp = endTimestamp + tolerance;

  // 2. 分离数据：时间段内和时间段外
  const dataInRange: any[] = [];
  const dataOutOfRange: any[] = [];

  data.forEach(item => {
      const itemTime = new Date(item.time).getTime();
      if (itemTime >= matchStartTimestamp && itemTime <= matchEndTimestamp) {
          dataInRange.push({ ...item });
      } else {
          dataOutOfRange.push({ ...item });
      }
  });

  // 3. 如果时间段内数据不足2个，直接返回原数据
  if (dataInRange.length < 2) {
      console.log('时间段内数据点不足，无法重新计算');
      return data;
  }

  // 4. 按时间排序
  dataInRange.sort((a, b) => new Date(a.time).getTime() - new Date(b.time).getTime());
  dataOutOfRange.sort((a, b) => new Date(a.time).getTime() - new Date(b.time).getTime());

  // 4.1 追加命中区间首点的前一个采样点（若存在）
  if (dataInRange.length > 0) {
      const firstInRangeTime = new Date(dataInRange[0].time).getTime();
      let previousPoint: any = null;
      for (let i = dataOutOfRange.length - 1; i >= 0; i--) {
          const outTime = new Date(dataOutOfRange[i].time).getTime();
          if (outTime < firstInRangeTime) {
              previousPoint = dataOutOfRange[i];
              dataOutOfRange.splice(i, 1);
              break;
          }
      }
      if (previousPoint) {
          dataInRange.unshift({ ...previousPoint });
      }
  }

  // 5. 获取时间段内的第一个和最后一个数据
  let firstData = dataInRange[0];
  if (initialValue !== undefined && initialValue !== null && initialValue !== '') {
      firstData.value = Number(initialValue);
  }

  let lastData = dataInRange[dataInRange.length - 1];
  if (endValue !== undefined && endValue !== null && endValue !== '') {
      lastData.value = Number(endValue);
  }

  // 6. 计算时间差和值差
  const startDate = new Date(firstData.time);
  const endDate = new Date(lastData.time);
  const timeDiff = (endDate.getTime() - startDate.getTime()) / timeUnitMs;
  const valueDiff = lastData.value - firstData.value;

  if (timeDiff === 0) {
      console.warn('时间段内开始和结束时间相同');
      return data;
  }

  // 7. 计算每单位时间的差值
  const valuePerUnit = valueDiff / timeDiff;
  const timeUnitLabel = isFermen ? '小时' : '分钟';

  console.log(`计算信息：
    时间段: ${startTime} - ${endTime}
    开始值: ${firstData.value}
    结束值: ${lastData.value}
    时间差: ${timeDiff} ${timeUnitLabel}
    值差: ${valueDiff.toFixed(6)}
    每${timeUnitLabel}差值: ${valuePerUnit.toFixed(6)}`);

  // 8. 重新计算时间段内的数据（基于命中的原始时间点重算，避免步长误差）
  const recalculatedData: any[] = dataInRange.map((item, index) => {
      if (index === 0) {
          return { ...item, value: parseFloat(Number(firstData.value).toFixed(6)) };
      }
      if (index === dataInRange.length - 1) {
          return { ...item, value: parseFloat(Number(lastData.value).toFixed(6)) };
      }
      const itemTime = new Date(item.time).getTime();
      const diffUnit = (itemTime - startDate.getTime()) / timeUnitMs;
      const newValue = Number(firstData.value) + (valuePerUnit * diffUnit);
      return {
          ...item,
          value: parseFloat(newValue.toFixed(6))
      };
  });

  // 9. 合并数据：时间段外数据 + 重新计算的时间段内数据
  const finalData = [...dataOutOfRange, ...recalculatedData];

  // 10. 按时间排序返回
  return finalData.sort((a, b) => new Date(a.time).getTime() - new Date(b.time).getTime());
}

  /**
   * 格式化时间为字符串
   */
  function formatDateTime(date) {
      const pad = (n) => n.toString().padStart(2, '0');
      return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ` +
            `${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
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

  // 计算斜率方法 END===========================================================================
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
