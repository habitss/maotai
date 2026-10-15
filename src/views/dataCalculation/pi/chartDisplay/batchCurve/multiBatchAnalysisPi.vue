<template>
  <div style="padding: 5px;height: 100%;">
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
          <!--区域-->
          <!-- <a-form-item label="区域">
            <kt-dict
              v-model:value="form.pcuNo"
              placeholder="请选择区域"
              dict="MES3_PI_CURVE_AREA"
              style="width:250px !important;"
              @change="getReccatData"
            />
          </a-form-item> -->
          <!--配方类型-->
          <!-- <a-form-item label="配方类型">
            <a-select show-search option-filter-prop="label" v-model:value="form.reccatNo" 
              :options="reccatArr" placeholder="请选择配方类型" allowClear @change="getRecipes"></a-select>
          </a-form-item> -->

          <!-- 品种配方 -->
          <a-form-item label="品种配方">
            <a-select show-search option-filter-prop="label" v-model:value="form.recipe" 
              :options="recipeArr" placeholder="请选择配方" allowClear @change="getRtBatch"></a-select>
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
                  <a-checkbox v-for="(tag) in rtBatchArr" :key="tag.id" :value="tag.order+'-'+tag.batch">{{ tag.order+'-'+tag.batch }}</a-checkbox>
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
      <div style="width:79.5%;background-color: #ECECEC;padding: 10px;" class="div-border">
        <MultiBatch ref="multiBatchRef" @chart-change="chart"/>
      </div>

    </div>
  </div>
</template>

<script lang="ts" name="pi-multiBatchAnalysis" setup>
  import { ref, reactive, onMounted, unref , nextTick} from 'vue';

  import { getRecipe,getRtBatches,unitAndPoint,getPointData } from './batchCurvePi.api';
  import { getDepartId } from '/@/utils/ktTool';
  import { useUserStore } from '/@/store/modules/user';
  import MultiBatch from './components/multiBatch.vue';
  import KtDict from '/@/components/KtDict/KtDict.vue';
  import * as echarts from 'echarts';
  import { useI18n } from '/@/hooks/web/useI18n';

  const { t } = useI18n();
  const multiBatchRef = ref();
  const queryParam = ref<any>({});
  const userStore = useUserStore();
  const dataOption = ref([] as any);
  const recipeArr = ref([] as any);
  const reccatArr = ref([] as any);
  const rtBatchArr = ref([] as any);
  const deviceArr = ref([{unitName:"--",unitNo:'charts',id:'1',points:[]}]);
  const chartRefs = ref<Record<number, HTMLElement>>({})
  const devicePointObj = ref({});
  const unitObj = ref({});
  const optionData = ref({
    name: "-",
    type: 'line',
    smooth: true,
    data: [0],
  })
  const curveType = ref('2');
  const xAxisData = ref([1,7,13,19,25,31,37])
  const unitNames = ref('糖化锅,麦芽粉碎,过滤槽,煮沸锅,沉淀槽,麦汁冷却,立罐,卧罐,发酵罐');
  const form = ref({
    startDate: null,
    endDate: null,
    beerType: null,
    recipe: null,
    batchNos: [],
    pcuNo: undefined,
    reccatNo: null,
    recipeName: null
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


  const batchTime = ref({
    startTime: '',
    endTime: '',
  });


  onMounted(() => {
    exportParams.departId = getDepartId(userStore?.getUserInfo?.orgCode);
    multiBatchRef.value.initEcharts("charts", [optionData.value], xAxisData.value, undefined);
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
    unitAndPoints();
  };
 
  // 获取配方
  async function getRecipes(){
    recipeArr.value = [];
    form.value['recipe'] = null
    rtBatchArr.value = [];
    if(!form.value.startDate || !form.value.endDate){
      return false;
    }
    let obj = {
      "startTime": form.value.startDate+" 00:00:00",
      "endTime": form.value.endDate+" 23:59:59",
      "unitNames": unitNames.value,
      "curveType": curveType.value
    }
    let res = await getRecipe(obj);
    if(res){
       res.forEach((item, index, array) => {
        recipeArr.value.push({
          value: item.recipe,
          label: item.recipe,
          name: item.recipe
        })
      });
    } 
  }

  // 获取批次号
  async function getRtBatch(){
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
      "unitNames": unitNames.value,
      "recipeName" : form.value.recipeName,
      "curveType": curveType.value
    }
    let res = await getRtBatches(obj);
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
  async function unitAndPoints(){
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
      "curveType": curveType.value
    }
    let res = await unitAndPoint(obj);
    if(res){
      if(!res.unitList || res.unitList.length === 0){
        deviceArr.value = [{unitName:"--",unitNo:'charts',id:'1',points:[]}]
        multiBatchRef.value.setDevice(deviceArr.value);
      }else{
        deviceArr.value = res.unitList;
        //设置device参数
        multiBatchRef.value.setDevice(deviceArr.value);
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
        debugger
        deviceArr.value.forEach((item, index, array) => {
          multiBatchRef.value.initEcharts(item.unitNo, [optionData.value], xAxisData.value, unitObj.value)
        })
      });

    }
  }
  //选择点位
  async function chart(unitNo, pointArray,chartForm){
    let points = chartForm[unitNo].join();
    if(!points){
      multiBatchRef.value.initEcharts(unitNo,[optionData.value], xAxisData.value, unitObj.value)
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
    }
    //获取点位数据
    let res = await getPointData(obj);
    let datas= [];
    debugger
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
    multiBatchRef.value.initEcharts(unitNo,datas,res.intervals, unitObj.value)
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