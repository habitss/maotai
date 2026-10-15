<template>
  <j-modal :title="title" width="calc(100% - 20em)" :visible="visible" @ok="handleOk" :okButtonProps="{ style: { display: 'none' } }" @cancel="handleCancel" cancelText="关闭">
    <!--查询条件-->
    <a-card title="" :bordered="false" :style="{ padding: '10px' }">
      <!-- {{ queryParam.kpiName }} ---
      {{ queryParam.kpiCode }} --- -->
      <!-- {{ infoTreeData }} --- -->
        <div class="jeecg-basic-table-form-container">
          <a-form ref="formRef" layout="inline" :model="queryParam" :label-col="labelCol" :wrapper-col="wrapperCol">
            <a-row :gutter="24" style="width: 100%;">
              <!-- 年份 -->
              <a-col :lg="5">
                <a-form-item label="选择年份">
                    <a-select
                      v-model:value="queryParam.years"
                      mode="multiple"
                      style="width: 250px"
                      placeholder="请选择年份"
                      :options="yearItem"
                      @change="yearChange"
                    ></a-select>
                </a-form-item>
              </a-col>
              <!-- KPI -->
              <a-col :lg="6">
                <a-form-item label="KPI">
                    <a-tree-select
                      v-model:value="queryParam.kpiCode"
                      show-search
                      style="width: 250px"
                      :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
                      placeholder="请选择KPI"
                      allow-clear
                      tree-default-expand-all
                      :tree-data="infoTreeData"
                      tree-node-filter-prop="kpiName"
                      :fieldNames= "{children:'children', label:'kpiName', value: 'kpiCode' }"
                      @change="kpiChange"
                    ></a-tree-select>
                </a-form-item>
              </a-col>
              <!--显示周数据-->
              <a-col :lg="7">
                <a-form-item label="显示周数据">
                  <a-checkbox name="type" @change="showWeek" v-model:checked="queryParam.showWeek"></a-checkbox>
                  <a-button type="primary" html-type="submit" @click="searchQuery" style="margin-left: 10px;"> 查询 </a-button>
                </a-form-item>

              </a-col>
            </a-row>
          </a-form>
        </div>
        
    </a-card>

    <a-card title="" :bordered="false" class="myCard">
        <!--列表-->
        <a-form-model ref="formRef" class="myForm">
            <a-table  bordered rowKey="id" :columns="columns" :data-source="dataSource" size="small"
            :scroll="{ x: true }" :loading="mounthLoading" :pagination="false">
            </a-table>
        </a-form-model>
         <!-- 图表区域 -->
        <div class="title">{{ queryParam.years.length>0?queryParam.years.join("、"):'' }}年{{ queryParam.kpiName||'' }}趋势曲线</div>
        <div ref="chartRef" style="width:100%;height:300px;"></div>
    </a-card>
  </j-modal>
</template>

<script lang="ts" setup>
  import { ref, nextTick, reactive,defineExpose, unref } from 'vue';
  import JModal from '/@/components/Modal/src/JModal/JModal.vue';
  import {trendCurveHeader,trendCurveData} from '../kpiWeekAndMonReports.api'
  import { list } from '/@/views/dataCalculation/kpi/info/KpiInfo.api';
  import * as echarts from 'echarts';
  import dayjs from "dayjs";
  import { join } from 'path';
  
  //日期
  const daysList = ref([]);
  const columns = ref([]);
  //数据
  const dataSource = ref([]);
  const kpiObjectList = ref([]);
  const chartRef = ref();
  const dataOption = ref([] as any);
  const xArr = ref([] as any);
  const weekCompare = ref({});

  const yearItem = ref([] as any);
  const infoTreeData = ref([]);
  const kpiInfoTreeData = ref([]);
  //查询条件
  // const queryParam = ref({})
  const queryParam = reactive<any>({});
  const mounthLoading = ref(false)
  const title = ref<string>('');
  const visible = ref<boolean>(false);
  const disableSubmit = ref<boolean>(false);
  const registerForm = ref();
  const tabPosition = ref('1');
  const emit = defineEmits(['register', 'success']);
  const kpiRowData = ref({});
  const kpiParams = ref({});
  const labelCol = reactive({
    span: 8
  });
  const wrapperCol = reactive({
    span: 14
  });

  /**
   * 显示趋势
   */
  function showModal(rowData,params) {
    title.value = "KPI趋势曲线图"
    kpiRowData.value = rowData.value;
    tabPosition.value = '1'
    visible.value = true;
    //初始化年份
    getSelectYears();
    const currentYear = new Date().getFullYear();
    queryParam.showWeek = false;
    queryParam.years = [currentYear];
    queryParam.kpiName = params.value.kpiName;
    queryParam.kpiCode = params.value.kpiCode;
    //获取KPI清单
    kpiList();
    //获取表头
    getHead();
  }
  
  //查询
  function searchQuery(){
    getHead();
  }
  //获取表头
  async function getHead(){
    let params = { 
      "years": queryParam.years, 
      "showWeek": queryParam.showWeek ? true : false, 
      "kpiName": queryParam.kpiName, 
      "kpiCode": queryParam.kpiCode
    }
    
    await trendCurveHeader(params).then(res => {
      if (!res.success) return
      xArr.value = [];
      weekCompare.value = {};
      //columns.value = processColumns(res.result)
      let department = iteration(res.result);
      columns.value = department;
      //获取数据
      getData(params);
    }).finally(() => {
        // mounthLoading.value = false;
    })
  }

  //获取数据
  async function getData(params){
    let seriesArr = [], legendArr = { data: [] }
     await trendCurveData(params).then(res => {
      if (!res.success) return
      dataSource.value = res.result;
      let newData = JSON.parse(JSON.stringify(res.result)) || null
      // let dataMock = JSON.parse(JSON.stringify(res.result)) || null
      newData && newData.forEach((item, index) => {
          legendArr.data.push(item.name)
          delete item.name
          let keysList = [];
          for (let keys in item) { keysList.push(keys) }
          let newKeys = keysList.sort((a,b)=> parseFloat(a.split('-')[1]) - parseFloat(b.split('-')[1]))

          let tempArr = []
          for (let j = 0; j < newKeys.length; j++) {
              var element = item[newKeys[j]];
              if (element == '') {
                  tempArr.push(0)
              } else {
                  tempArr.push(parseFloat(element))
              }
          }
          seriesArr.push({
              name: legendArr.data[index],
              type: 'line',
              data: tempArr
          })

      });
      barEcharts(xArr.value, seriesArr, legendArr)

    }).finally(() => {
       mounthLoading.value = false;
    })
  }

  function iteration(arr) {
    let newArr = [];
    if (arr != undefined && arr.length > 0) {
        newArr = arr.map(item => {
            item.dataIndex = item.key
            item.width = 100
            if (item.key) {
              if(queryParam.showWeek && item.key.includes("w_")){
                //周
                // xArr.value.push(item.key)
                // let key = weekCompare.value[item.key]= item.key.substring(2, 6)+"("+item.title+")";
                let key = generateDateRange(item.key.substring(2, 6),item.title)
                xArr.value.push(key)
              }else if(!queryParam.showWeek && item.key.includes("-")){
                //月
                xArr.value.push(item.key)
              }
            }
            if (item.children != undefined && item.children.length > 0) {
                iteration(item.children);
            } else {
            }
            return item;
        });
    }
    return newArr;
  }

  //atable 表头
  function processColumns(data) {
    return data.map(item => {
      const newItem = {
        ...item,           // 保留原有属性（如 title、width 等）
        dataIndex: item.key // 新增 dataIndex，值等于 key
      };
      // 如果存在 children，递归处理
      if (item.children && item.children.length > 0) {
        newItem.children = processColumns(item.children);
      }
      return newItem;
    });
  }

  /**
   * 确定按钮点击事件
   */
  function handleOk() {
    registerForm.value.submitForm();
  }

  /**
   * 取消按钮回调事件
   */
  function handleCancel() {
    visible.value = false;
  }

  
  //图表初始化
  function barEcharts(xArr, seriesArr, legendArr) {
    //数据过滤
    debugger
    // 配置图表
    var option = {
        tooltip: {
            trigger: 'axis'
        },
        grid: {
            left: "5%",
            top: "20%",
            right: "5%",
            bottom: "5%",
            containLabel: true
        },
        legend: legendArr,
        //x轴显示种类
        xAxis: {
            type: 'category',
            boundaryGap: false,
            // data: xArr.splice(2, xArr.length)
            data: xArr
        },
        //y轴可填数值等
        yAxis: { type: 'value' },
        series: seriesArr
    }
    console.log(option,"=========================");
    dataOption.value = option;
    var myChart = echarts.init(chartRef.value);
    myChart.setOption(option);

  }
  //KPI清单
  async function kpiList(){
    let res = await list({pageNo: 1,pageSize: 9999});
    if(res && res.records){
      infoTreeData.value = [];
      // infoTreeData.value.push(...res.records);
      infoTreeData.value = res.records;
      kpiInfoTreeData.value = flattenKpiArray(infoTreeData.value);
    }
  }
  //周
  function showWeek() {
    getHead()
  }

  //KPI选择
  function kpiChange(row){
    //获取KPI名称
    kpiInfoTreeData.value.forEach((obj) => {
      if (obj["kpiCode"] === row) {
          queryParam.kpiName = obj["kpiName"];
      }
    });
  }

  //数组对象扁平化
  function flattenKpiArray(arr) {
    const result = [];
    function traverse(node) {
      // 如果节点有 kpiCode 和 kpiName，则添加到结果中
      if (node.kpiCode !== undefined && node.kpiName !== undefined) {
        result.push({
          kpiCode: node.kpiCode,
          kpiName: node.kpiName
        });
      }
      // 如果有子节点，递归遍历
      if (node.children && Array.isArray(node.children)) {
        node.children.forEach(child => traverse(child));
      }
    }
    // 遍历原始数组中的每个元素
    arr.forEach(item => traverse(item));
    return result;
  }

  function getSelectYears() {
      const curYear = [];
      const time = (new Date()).getFullYear()
      for (let i = time+5; i > time - 11; i--) {
        curYear.push({ 'value': i, 'text': i })
      }
      yearItem.value = curYear
  }
  function yearChange(years){
    //年份排序
    years.sort((a, b) => a - b);
  }


  /**
 * 根据年份和标题生成日期范围字符串
 * @param {number} year - 基础年份
 * @param {string} title - 日期范围标题，格式如 "12.30-01.05"
 * @returns {string} 格式化后的日期范围字符串，如 "2024-12-30|2025-01-05"
 */
  function generateDateRange(year, title) {
      // 分割标题获取开始和结束部分
      const [startPart, endPart] = title.split('-');
      
      // 分割开始和结束的月份和日期
      const [startMonth, startDay] = startPart.split('.').map(Number);
      const [endMonth, endDay] = endPart.split('.').map(Number);
      
      // 处理跨年逻辑
      let startYear = year;
      let endYear = year;
      
      // 如果开始月份是12月而结束月份是1月，则表示跨年
      if (startMonth === 12 && endMonth === 1) {
          startYear = year - 1;
      }
      
      // 格式化日期部分，确保月份和日期是两位数
      const formatNumber = num => num.toString().padStart(2, '0');
      
      // 构建结果字符串
      const startDate = `${startYear}-${formatNumber(startMonth)}-${formatNumber(startDay)}`;
      const endDate = `${endYear}-${formatNumber(endMonth)}-${formatNumber(endDay)}`;
      
      return `${startDate}|${endDate}`;
  }
  defineExpose({
    showModal,
    disableSubmit,
  });
</script>

<style lang="less" scoped>
  /**隐藏样式-modal确定按钮 */
  .jee-hidden {
    display: none !important;
  }

  :deep(.ant-card-body .ant-card ) {
    padding: 10px !important;
    border-radius: 0 0 4px 4px;
  }
  :deep(.ant-card-body) {
    padding: 10px;
  }
  
</style>
<style lang="less" scoped></style>
