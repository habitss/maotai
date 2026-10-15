<template>
  <div class='home-workshope'>
    <a-spin :spinning='loadingMillCount'>
      <!-- <div class='top-title'>
        <top-rolling-news class='rolling-style' :newsData='newsText'></top-rolling-news>
      </div> -->
      <a-row :gutter='[16, 16]' style="padding: 10px;">
        <a-col :span='24'>
          <top-rolling-news class='rolling-style' :newsData='newsText'></top-rolling-news>
        </a-col>
      </a-row>
      <a-row :gutter='[8, 8]' style='margin-top: 4px'>
        <a-col :xs='24' :sm='24' :md='12' :lg='6' :xl='6' style="position: relative;">
          <!--酿造车间关键KPI累计达标率-->
          
          <CardItemComponent class='card-item' :card='dataArr[0]'>
            <template v-slot:content>
              <div id='kpiId' class='content' style='padding-top: 12px;'>
                <a-row class='title-name' style="display: inline-flex;width: 100%">
                  <a-col style="display: inline-block; width: 140px">
                    <div style="width: 140px">
                      <div class='progress-title title'>月累达标率</div>
                      <!-- <div class='title' v-text='`(${dayjs().year()}-1~${dayjs().year()}-${dayjs().month()})`'></div> -->
                      <div class='title'>
                        {{dayjs().year()}}-1~{{dayjs().year()}}-{{ dayjs().month() }}
                      </div>
                    </div>
                  </a-col>
                  <a-col style="flex: 1;">
                    <a-progress :percent='kpiAcceptability.year'
                      :stroke-color='getStrokeColor(kpiAcceptability.year)' />
                  </a-col>
                </a-row>
                <a-row class='title-name' style="display: inline-flex;width: 100%">
                  <a-col style="display: inline-block; width: 140px">
                    <div style="width: 140px">
                      <div class='progress-title title'>上月达标率</div>
                      <div class='title' v-text='`(${dayjs().month()}月)`'></div>
                    </div>
                  </a-col>
                  <a-col style="flex: 1;">
                    <a-progress :percent='kpiAcceptability.month'
                      :stroke-color='getStrokeColor(kpiAcceptability.month)' />
                  </a-col>
                </a-row>
                <a-row class='title-name' style="display: inline-flex;width: 100%">
                  <a-col style="display: inline-block; width: 140px">
                    <div style="width: 140px">
                      <div class='progress-title title'>上周达标率</div>
                      <div class='title' v-text='`(第${kpiAcceptability.weekNum}周)`'></div>
                    </div>
                  </a-col>
                  <a-col style="flex: 1;">
                    <a-progress :percent='kpiAcceptability.week' status='active'
                      :stroke-color='getStrokeColor(kpiAcceptability.week)' />
                  </a-col>
                </a-row>
              </div>
            </template>
          </CardItemComponent>
        </a-col>
        <!-- PI合格率 -->
        <a-col :xs='24' :sm='24' :md='12' :lg='6' :xl='6'>
          <CardItemComponent class='card-item' :card='dataArr[1]'>
            <template v-slot:content>
              <a-row style="height: 100%;overflow: hidden; overflow-y: auto;">
                <a-col :span='24'
                  style="display: inline-flex; flex-direction: column;justify-content: center; height: 100%">
                  <div :key="op.id" id='piProgressId' class='pi-progress' v-for="op in acceptability.acceptabilityList">
                    <a-row>
                      <a-col :span='8'>
                        <div class='progress-title title'>{{ op.name }}</div>
                      </a-col>
                      <a-col :span='15'>
                        <a-progress :percent='Math.ceil(op.lv * 100)'
                          :stroke-color='getStrokeColor(Number(op.lv * 100))'>
                          <template #format='percent'>
                            <span>{{ Number(op.lv * 100).toFixed(2) + '%' }}</span>
                          </template>
                        </a-progress>
                      </a-col>
                    </a-row>
                  </div>
                </a-col>
              </a-row>
            </template>
          </CardItemComponent>
        </a-col>
         
        <!-- 行动计划完成率 -->
        <a-col :xs='24' :sm='24' :md='12' :lg='6' :xl='6'>
          <!--酿造车间行动计划完成率-->
          <CardItemComponent class='card-item' :card='dataArr[3]'>
            <template v-slot:extra>
              <a @click="handleGoAction('dutyDeptId')">前往行动计划</a>
            </template>
            <template v-slot:content>
              <div class='bg-img'>
                <div id='workshowActionId' class='content workshow-action pie-action'></div>
                <div id='workshowActionIdRight' class='content workshow-action2'></div>
              </div>
            </template>
          </CardItemComponent>
        </a-col>

        <!-- 个人行动计划完成率 -->
        <a-col :xs='24' :sm='24' :md='12' :lg='6' :xl='6' style="display: inline-flex; flex-direction: column;">
          <CardItemComponent class='card-item' :card='dataArr[2]'>
            <template v-slot:extra>
              <a @click="handleGoRaAction('dutyUserId')">前往RA单</a>
            </template>
            <template v-slot:content>
              <div class='bg-img'>
                <div id='workshowActionIdt' class='content workshow-action pie-action'></div>
                <div id='workshowActionIdRightt' class='content workshow-action2'></div>
              </div>
            </template>
          </CardItemComponent>
        </a-col>
      </a-row>
      <!-- 重点关注KPI指标 -->
      <a-row :gutter='[8, 8]'>
        <a-col :xs='24' :sm='24' :md='12' :lg='12' :xl='12'>
          <a-card title='重点关注KPI指标' :head-style='{ borderBottom: 0 }' :body-style='{ paddingTop: 0, height: "185px" }'
            :border='false' style='margin-top: 6px;'>
            <a-empty v-if="dataKpi.length < 1" style="height: 140px;"></a-empty>
            <a-table v-if="dataKpi.length > 0" :columns='columnsKpi' :data-source='dataKpi' bordered size='small'
              :pagination='false' style="height: 140px;" :scroll='{ x: true }'>
              <!-- <span v-for='acol in cols' v-bind:key='acol' :slot='acol.colItem'>{{ acol.title }}
              </span> -->
              <span v-for="acol in cols" :key="acol" :slot="acol.colItem">
                {{ acol.title }}
              </span>
            </a-table>
          </a-card>
          <!-- 重点关注PI指标（本周） -->
          <a-card title='重点关注PI指标（本周）' :head-style='{ borderBottom: 0 }'
            :body-style='{ paddingTop: 0, height: "190px" }' :border='false' style='margin-top: 8px'>
            <a-empty v-if="dataPi.length < 1" style="height: 300px;"></a-empty>
            <a-table v-if="dataPi.length > 0" :columns='piColumns' :data-source='dataPi' bordered size='small'
              :pagination='false' :scroll='{ y: 130 }'>
            </a-table>
          </a-card>
        </a-col>
        <!-- 待办事项 -->
        <a-col :xs='24' :sm='24' :md='12' :lg='12' :xl='12'>
          <a-card title='待办事项' :head-style='{ borderBottom: 0 }'
            :body-style='{ paddingTop: 0, paddingBottom: 0, height: "445px" }' :border='false' style="margin-top: 6px;">
            <a-row :gutter='[8, 8]' style="">
              <!--异常分析-->
              <a-col :xs='24' :sm='24' :md='24' :lg='12' :xl='12'>
                <div id='error-analysis' class='echart-item'></div>
              </a-col>
              <a-col :xs='24' :sm='24' :md='24' :lg='12' :xl='12'>
                <div id='check-list' class='echart-item'></div>
              </a-col>
              <a-col :xs='24' :sm='24' :md='24' :lg='12' :xl='12'>
                <div id='manage' class='echart-item'></div>
              </a-col>
              <a-col :xs='24' :sm='24' :md='24' :lg='12' :xl='12'>
                <div id='my-task' class='echart-item'></div>
              </a-col>

            </a-row>
          </a-card>
        </a-col>
      </a-row>
    </a-spin>
  </div>
</template>

<script lang="ts" name="home-workshop" setup>
import { ref, nextTick, defineExpose, defineProps, computed, onMounted } from 'vue';
import topRollingNews from './components/topRollingNews.vue';
import CardItemComponent from './components/cardItemComponent.vue';
import { useMessage } from "/@/hooks/web/useMessage";
import { router } from '/@/router';
import { useI18n } from '/@/hooks/web/useI18n';
import { useUserStore } from '/@/store/modules/user';
import {userWorkShopPageHome,workShopActionStatisticsData,getDeptName,getAnnountCement, queryPayCloseAttentionPi} from './home.api';
import {optionTemp, optionTempRight, optionAction, optionActionRight,
  piColumns,
  columnsAbnor,
  piData,
  errorAnalysisDataOpt,} from './home.data';
import * as echarts from 'echarts';

import dayjs from 'dayjs';
const userStore = useUserStore();
const { createMessage } = useMessage();
const { t } = useI18n();

// name="work-home"
const title = ref<string>('');
const protocol = ref<string>('');
const actionPlan = ref({})
const loadingMillCount = ref(false)
const newsText = ref('')
const dataKpi = ref([])
const columnsKpi = ref([])
const dataPi = ref([])
const cols = ref([])
const dataArr = ref([] as any)
const actionRightPlan = ref()

const errorAnalysisData = ref({})
const layeringChecksData = ref({})
const projectManage = ref({})
const reportTypeMap = ref({})

const departMap = ref({})
const myTaskData = ref({})
const kpiTableHeight = ref(false)

const errorAnalysis = ref()
const workshowActionIdChart = ref();
const workshowActionIdChartt = ref();
const workshowActionIdRightChart = ref();
const workshowActionIdRightChartt = ref();

const optionTempRightPI = ref({});
const optionTempPI = ref({});

const checkList = ref();
const manage = ref();
const myTask = ref();
const dataAbnor = ref([]);
const departName = ref('');
const departId = ref('');
const strokeColor1 = ref({
  '0%': '#DB2A26',
  '100%': '#DB2A26'
})
const strokeColor2 = ref({
  '0%': '#F55855',
  '100%': '#F55855'
})
const strokeColor3 = ref({
  '0%': '#F8CF00',
  '100%': '#F8CF00'
})
const strokeColor4 = ref({
  '0%': '#00ff00',
  '100%': '#00ff00'
})

const kpiAcceptability = ref({
  year: 0,
  month: 0,
  week: 0,
  weekNum: ''
})
const acceptability =  ref({
  number: 0,
  total: 0,
  acceptabilityList: [] as any
})
const personActionPlanComplate = ref({
  delay: 0,
  deadline: 0,
  doing: 0,
  closed: 0
});


function initData(){
  // let departName = "测试"
  dataArr.value = [{
    title: departName.value + '关键KPI累计达标率',
    timeType: '1',
    subTitle: '上月KPI异常数据',
    subTitleValue: '0',
    subColor: 'red'
  }, {
    title: departName.value  + 'PI合格率',
    timeType: '2',
    subTitle: '车间PI合格率',
    subTitleValue: '0',
    subColor: 'green'
  }, {
    title: '个人行动计划完成率',
    timeType: '1',
    subTitle: '本月计划完成任务',
    subTitleValue: '0',
    subColor: 'blue'
  }, {
    title: departName.value + '行动计划完成率',
    timeType: '1',
    subTitle: '本月计划完成任务',
    subTitleValue: '0',
    subColor: 'green'
  }];
}

function getStrokeColor(value) {
  if (value < 80) {
    return "red";
  } else {
    return "#00ff00";
  }
}

async function getMillCount() {
  return new Promise((resolve) => {
    // const userInfo = this.getUserInfo();
    const timeParams = getDateParame('week', 1)
    let params = {
      // 'workShopCode': this.departMap.orgCode,
      'workShopCode': userStore?.userInfo?.orgCode,
      'deptId': departId.value,
      'dutUserId': (userStore?.userInfo?.id),
      'factory': (userStore && userStore.factoryCode),
      "functionName": "workshopStatistics",
      ...timeParams
    };


    loadingMillCount.value = true;
    // millCount(params).then(res => {
    userWorkShopPageHome(params).then(res => {
      if (res.success) {
        let { result } = res;

        dataArr.value[0].subTitleValue = result['关键KPI累计达标率']['异常数量'];
        kpiAcceptability.value.year = parseFloat(parseFloat(result['关键KPI累计达标率']['月累达标率'] * 100).toFixed(0));
        kpiAcceptability.value.month = parseFloat(parseFloat(result['关键KPI累计达标率']['上月达标率'] * 100).toFixed(0));
        kpiAcceptability.value.week = parseFloat(parseFloat(result['关键KPI累计达标率']['周达标率'] * 100).toFixed(0));
        kpiAcceptability.value.weekNum = parseFloat(result['关键KPI累计达标率']['上周']);

        dataArr.value[2].subTitleValue = result['个人行动计划完成率']['本月计划完成任务'];
        personActionPlanComplate.value.delay = result['个人行动计划完成率']['延迟'];
        personActionPlanComplate.value.deadline = result['个人行动计划完成率']['临期'];
        personActionPlanComplate.value.doing = result['个人行动计划完成率']['正在进行'];
        personActionPlanComplate.value.closed = result['个人行动计划完成率']['已完成'];

        dataArr.value[1].subTitleValue = result['pi合格率']['车间PI合格率'];
        //result["pi合格率"]['总数']
        acceptability.value.number = result['pi合格率']['green'];
        acceptability.value.total = result['pi合格率']['总数'];
        acceptability.value.acceptabilityList = result['pi合格率']['lv'] || [];
        //PI合格率， 设置  this.barChart 的数据值
        let passMap = deepCloneByJSON(result['pi合格率'])
        passMap = {
          ...passMap,
          lv: [
            {
              lv: parseFloat(parseFloat(passMap.green) / parseFloat(passMap['总数'])).toFixed(2),
              name: "绿区率",
              size: passMap.green || 0,
              totalSize: 0
            },
            {
              lv: parseFloat(parseFloat(passMap.yellow) / parseFloat(passMap['总数'])).toFixed(2),
              name: "黄区率",
              size: passMap.yellow || 0,
              totalSize: 0
            },
            {
              lv: parseFloat(parseFloat(passMap.red) / parseFloat(passMap['总数'])).toFixed(2),
              name: "红区率",
              size: passMap.red || 0,
              totalSize: 0
            },
          ],
          "已完成": passMap.green || 0,
          "延期": passMap.red || 0,
          "进行中": passMap.yellow || 0,
        }
        acceptability.value.acceptabilityList = passMap.lv;
        console.log('passMap :>> ', passMap);
        setActionEchartData(actionPlan.value, passMap, true);
        actionPlan.value.series[0].name = 'PI统计';
        // this.barChart.setOption(this.actionPlan, true);

        //PI合格率， 设置  this.barChart 的数据值
        setActionEchartRightData(actionRightPlan.value, passMap, true);
        actionRightPlan.value.series[0].name = 'PI统计';
        // this.barChartRightChart.setOption(this.actionRightPlan, true);


        //行动计划完成率
        setActionEchartData(actionPlan.value, result['行动计划完成率']);
        actionPlan.value.series[0].name = '行动计划完成率';
        workshowActionIdChart.value.setOption(actionPlan.value, true);

        //行动计划完成率
        setActionEchartRightData(actionRightPlan.value, result['行动计划完成率']);
        actionRightPlan.value.series[0].name = '行动计划完成率';
        workshowActionIdRightChart.value.setOption(actionRightPlan.value, true);

        // let planMap = _.cloneDeep(result['个人行动计划完成率'])
        let planMap = deepCloneByJSON(result['个人行动计划完成率'])
        planMap = {
          ...planMap,
          lv: [
            {
              lv: parseFloat(parseFloat(planMap['已完成']) / parseFloat(planMap['本月计划完成任务'])).toFixed(2),
              name: "completed",
              size: planMap['已完成'] || 0,
              totalSize: 0
            },
            {
              lv: parseFloat(parseFloat(planMap['正在进行']) / parseFloat(planMap['本月计划完成任务'])).toFixed(2),
              name: "underWay",
              size: planMap['正在进行'] || 0,
              totalSize: 0
            },
            {
              lv: parseFloat(parseFloat(planMap['延迟']) / parseFloat(planMap['本月计划完成任务'])).toFixed(2),
              name: "postpone",
              size: planMap['延迟'] || 0,
              totalSize: 0
            },
          ],
          "已完成": planMap['已完成'] || 0,
          "延期": planMap['延迟'] || 0,
          "进行中": planMap['正在进行'] || 0,
          "总数": planMap['本月计划完成任务'] || 0,
        }

        //行动计划完成率
        setActionEchartData(actionPlan.value, planMap);
        actionPlan.value.series[0].name = '个人行动计划完成率';
        workshowActionIdChartt.value.setOption(actionPlan.value, true);

        //行动计划完成率
        setActionEchartRightData(actionRightPlan.value, planMap);
        actionRightPlan.value.series[0].name = '个人行动计划完成率';
        workshowActionIdRightChartt.value.setOption(actionRightPlan.value, true);


        //代办事项，
        //异常分析功能
        setTodoThingsData(errorAnalysisData.value, result['待办事项']['异常分析'], '异常分析');
        errorAnalysisData.value.series[0].name = '异常分析';
        errorAnalysis.value.setOption(errorAnalysisData.value, true);

        setTodoThingsData(layeringChecksData.value, result['待办事项']['分层检查'], '分层检查');
        layeringChecksData.value.series[0].name = '分层检查';
        checkList.value.setOption(layeringChecksData.value, true);

        setTodoThingsData(projectManage.value, result['待办事项']['COP记录'], 'COP记录');

        projectManage.value.series[0].name = 'COP记录';
        manage.value.setOption(projectManage.value, true);

        dataAbnor.value = result['待办事项']['报警列表'];

        setTodoThingsData(myTaskData.value, result["待办事项"]["期间核查"], '期间核查')
        myTaskData.value.series[0].name = '期间核查'
        myTask.value.setOption(myTaskData.value, true);

        // window.addEventListener('resize', this.barChart.resize);
        // window.addEventListener('resize', this.barChartRightChart.resize);
        window.addEventListener('resize', workshowActionIdChart.value.resize);
        window.addEventListener('resize', workshowActionIdChartt.value.resize);
        window.addEventListener('resize', errorAnalysis.value.resize);

        window.addEventListener('resize', layeringChecksData.value.resize);
        window.addEventListener('resize', projectManage.value.resize);
        window.addEventListener('resize', myTaskData.value.resize);

      } else {
        createMessage.warning(res.message);
      }
    }).finally(() => {
      loadingMillCount.value = false;
    });
  });
}

function getDateParame(keys, isEndCurDay) {
  //isEndCurDay    1: 从当前算起往前推的周或者月
  //               2：上一个月份或者周  从星期一到星期天的区间
  //               3： 当前月或者周     从星期一到星期天的区间
  if (keys == 'week' && isEndCurDay === 1) {
    return {
      opTime: dayjs().subtract(1, 'week').format('YYYY-MM-DD'),
      edTime: dayjs().format('YYYY-MM-DD')
    }
  } else if (keys == 'week' && isEndCurDay === 2) {
    let startOfLastWeek = dayjs().subtract(1, 'week').startOf('week');
    let endOfLastWeek = startOfLastWeek.add(6, 'day').endOf('day');
    return {
      opTime: startOfLastWeek.format('YYYY-MM-DD'),
      edTime: endOfLastWeek.format('YYYY-MM-DD')
    }
  } else if (keys == 'month' && isEndCurDay === 1) {
    return {
      opTime: dayjs().subtract(1, 'month').format('YYYY-MM-DD'),
      edTime: dayjs().format('YYYY-MM-DD')
    }
  } else if (keys == 'month' && isEndCurDay === 2) {
    return {
      opTime: dayjs().subtract(1, 'month').startOf('month').format('YYYY-MM-DD'),
      edTime: dayjs().subtract(1, 'month').endOf('month').format('YYYY-MM-DD')
    }
  } else if (keys == 'month' && isEndCurDay === 3) {
    return {
      opTime: dayjs().startOf('month').format('YYYY-MM-DD'),
      edTime: dayjs().endOf('month').format('YYYY-MM-DD')
    }
  } else if (keys == 'year' && isEndCurDay === 2) {
    return {
      opTime: dayjs().subtract(1, 'year').startOf('year').format('YYYY-MM-DD'),
      edTime: dayjs().subtract(1, 'year').endOf('year').format('YYYY-MM-DD')
    }
  } else if (keys == 'year' && isEndCurDay === 3) {
    return {
      opTime: dayjs().startOf('year').format('YYYY-MM-DD'),
      edTime: dayjs().startOf('year').format('YYYY-MM-DD')
    }
  }

}
//前往行动计划
function handleGoAction(key) {
  // let queryObj = {}
  // if (key === 'dutyUserId') {
  //   queryObj.dutyUserId = this.userInfo().id
  // } else {
  //   queryObj.dutyDeptId = this.reportTypeMap.id
  // }
  // this.$router.push({ path: '/farmactionplan', query: queryObj })
  router.push({ path: "/dataCalculation/actionPlan/track/actionPlanTrackList"})
}
//前往RA单
function handleGoRaAction(key){
  // let queryObj = {}
  // if (key === 'dutyUserId') {
  //   queryObj.dutyUserId = this.userInfo().id
  // } else {
  //   queryObj.dutyDeptId = this.reportTypeMap.id
  // }
  // this.$router.push({ path: '/farmactionplan', query: queryObj })
  router.push({ path: "/dataCalculation/actionPlan/ra/RaIndex"})
}

function setActionEchartData(echartSeriasData, data, isPass) {

  const pieGradualChangeColorTemp = [
    [{ offset: 0, color: '#00ff00' }, { offset: 1, color: '#00ff00' }],
    [{ offset: 0, color: '#FF9100' }, { offset: 1, color: '#FFC400' }],
    [{ offset: 0, color: 'red' }, { offset: 1, color: 'red' }],
    [{ offset: 0, color: '#cccccc' }, { offset: 1, color: '#cdcdcd' }],
    [{ offset: 0, color: 'red' }, { offset: 1, color: 'red' }],
  ];

  const nameEnumValue = {
    'completed': isPass ? '绿区' : '已完成',
    'underWay': isPass ? '黄区' : '进行中',
    'postpone': isPass ? '红区' : '延期',
    'cancel': '取消',
    'delayOk': '延期完成',
  };
  let realData = data['lv'];
  let currealData = [];

  for (let key in nameEnumValue) {
    realData.find((item) => item.name === key) && currealData.push(realData.find((item) => item.name === key));
  }
  //let _self = this
  currealData.forEach((item, index) => {
    if (!echartSeriasData.series[0].data[index]) {
      echartSeriasData.series[0].data[index] = {
        lv: 0,
        name: '无数据',
        size: 0,
        itemStyle: { normal: { color: 'red' } }
      };
    }
    echartSeriasData.series[0].data[index].name = nameEnumValue[item.name];
    echartSeriasData.series[0].data[index].value = item.size;
    echartSeriasData.series[0].data[index].lv = item.lv;

    echartSeriasData.series[0].data[index].itemStyle.normal.color = new echarts.graphic.LinearGradient(0, 0, 0, 1, pieGradualChangeColorTemp[index])
  });

  dataArr.value[3].subTitleValue = data['本月计划完成任务'];
  var val = parseFloat(parseFloat(parseFloat(data['已完成']) / parseFloat(data['总数'])) * 100).toFixed(2)
  echartSeriasData.title = {
    text: isNaN(val) ? '0.00' : val,
    left: 'center',
    top: 'middle'
  };
}


function setActionEchartRightData(echartSeriasData, data, isPass) {
  //圆饼的渐变颜色
  const pieGradualChangeColorTemp = [
    [{ offset: 0, color: '#00ff00' }, { offset: 1, color: '#00ff00' }],
    [{ offset: 0, color: '#FF9100' }, { offset: 1, color: '#FFC400' }],
    [{ offset: 0, color: 'red' }, { offset: 1, color: 'red' }],
    [{ offset: 0, color: '#cccccc' }, { offset: 1, color: '#cdcdcd' }],
    [{ offset: 0, color: 'red' }, { offset: 1, color: 'red' }],
  ];

  const nameEnumValue = {
    'completed': isPass ? '绿区' : '已完成',
    'underWay': isPass ? '黄区' : '进行中',
    'postpone': isPass ? '红区' : '延期',
    'cancel': '取消',
    'delayOk': '延期完成',
  };

  let realData = data['lv'];

  let currealData = [];

  for (let key in nameEnumValue) {
    realData.find((item) => item.name === key) && currealData.push(realData.find((item) => item.name === key));
  }
  // let realData = [{lv: 10,name:"已完成",size: 480},{lv: 30,name:"正在进行",size: 30},{lv: 20,name:"延期",size: 10}]
  echartSeriasData.legend.formatter = function (name) {
    const dataItem = realData.find((item) => nameEnumValue[item.name] === name);
    let numPre = data['总数'] && dataItem.size / data['总数'] ? dataItem.size / data['总数'] : 0
    return `${name}  ${dataItem.size}  ${(Math.floor((numPre) * 100))}%`;
  };

  currealData.forEach((item, index) => {
    if (!echartSeriasData.series[0].data[index]) {
      echartSeriasData.series[0].data[index] = {
        lv: 0,
        name: '无数据',
        size: 0,
        itemStyle: { normal: { color: 'red' } }
      };
    }

    echartSeriasData.series[0].data[index].name = nameEnumValue[item.name];
    echartSeriasData.series[0].data[index].value = item.size;
    echartSeriasData.series[0].data[index].lv = item.lv;

    echartSeriasData.series[0].data[index].itemStyle.normal.color = new echarts.graphic.LinearGradient(0, 0, 0, 1, pieGradualChangeColorTemp[index]);
  });

  dataArr.value[3].subTitleValue = data['本月计划完成任务'];
}

//深度拷贝
function deepCloneByJSON(obj) {
  return JSON.parse(JSON.stringify(obj));
}
const colWidth = computed(() => {

});

async function getFindEmphasisType() {
  return new Promise((resolve) => {
    //KPI指标
    let params = {
      'dept': userStore?.userInfo?.orgCode,
      'factory': userStore.factoryCode,
      'reportId': userStore.orgCode || userStore.factoryCode,
      'showChildrenKpi': false,
      'showWeek': true,
      'year': dayjs().format('YYYY'),
      'months': [dayjs().format('YYYY-MM')]
    };

    loadingMillCount.value = true;
    workShopActionStatisticsData(params).then(res => {
      if (res.success) {
        let { result } = res;
        setKpiAttr(result);
      } else {
        createMessage.warning(res.message);
      }
    }).finally(() => {
      loadingMillCount.value = false;
    });
  });
}

function setKpiAttr(KPiData) {
  const allCol = [{ 'name': 'KPI', 'biaozhun': '标准', 'yue': '上月', 'week': '上周', 'id': '88' }].concat(KPiData);

  columnsKpi.value = setColumnsArr(allCol);
  cols.value = columnsKpi.value.map(item => {
    return { 'colItem': item.slots.title, 'title': item.titleText };
  });

  if (KPiData.length < 1) {
    dataKpi.value = [];
    return;
  }
  dataKpi.value = [{}, {}, {}];

  allCol.forEach((item) => {
    dataKpi.value.forEach((ele, index) => {
      let cutNum = index == 1 ? item.yue : index == 2 ? item.zhou : item.biaoZhun;
      ele[item.id] = toFixedValue(cutNum);
      ele[item.id + '_colory'] = index == 1 ? item.yueColor : index == 2 ? item.zhouColor : '';
      ele['88'] = index == 1 ? '上月' : index == 2 ? '上周' : '标准'
    })
  });
}

function setColumnsArr(columns) {
  const colArr = columns.map(item => {
    return {
      //slots: { title: `${item.id}_title` },
      slots: { customRender: `${item.id}_title` },
      dataIndex: item.id,
      align: 'center',
      width: (item.name.length * 22) + 'px',
      titleText: item.name,
      title: item.name,
      customRender: function (value) { 
        //值
        let record = value.record
        return record[item.id]
      },
      customCell: function (value, index) { 
        //设置颜色
        // let record = value.record
        // let index = value.index
        return setTableCellColor(value[item.id + '_colory'], index)
      }
    };
  });
  return colArr;
}

function toFixedValue(value) {
  if (!value && value !== 0 && value !== '0') return '';
  if (/^[+-]?\d*\.?\d*$/.test(value) && value !== '' && value !== null && value !== undefined) {
    let floor = Math.floor(value);
    if (floor === value) {
      return value;
    }

    return Number(value).toFixed(2);
  } else return value;
}

function setTableCellColor(record, rowInd) {
  let color = 'white';
  let colorObj = { 'red': 'red', 'green': '#00ff00', 'yellow': 'yellow' };
  if (rowInd < 0)
    color = '#EBF1FB';
  if (record) {
    color = colorObj[record];
  }
  return {
    style: {
      'background-color': color,
      'color': "#000000d9"//record ? record == 'yellow' ? '#333' : '#fff' : '#000000d9'
    }
  };
}


async function getPayCloseAttentionPi() {
  const { result } = await queryPayCloseAttentionPi({deptCode: userStore?.userInfo?.orgCode});
  dataPi.value = result;
}

function setTodoThingsData(echartSeriasData, data, type) {
  const nameEnumValue = {
    'postpone': '延期',
    'underWay': '进行中',
    'completed': '已完成',
  };
  const pieColor = ['#DB2A26', '#F8CF00', '#00ff00'];
  const sortArr = ['postpone', 'underWay', 'completed'];
  let realData = [];
  sortArr.forEach(itemName => {
    const dataValue = data.filter(item => item.name === itemName)[0]
    realData.push(dataValue);
  })

  echartSeriasData.legend.formatter = function (name) {

    const dataItem = data.find((item) => nameEnumValue[item.name] === name);
    return `${name}  ${dataItem.size}  ${Math.floor(dataItem.lv * 10000) / 100}%`;

  };
  echartSeriasData.title.text = type;
  const isEmpty = realData.every(item => {
    if(item){
      return item.size == 0;
    }else{
      return false;
    }
    
  });
  realData.forEach((item, index) => {
    if (!echartSeriasData.series[0].data[index]) {
      echartSeriasData.series[0].data[index] = {
        lv: 0,
        name: '无数据',
        size: 0,
        itemStyle: { normal: { color: 'red' } }
      };
    }

    echartSeriasData.series[0].data[index].name = nameEnumValue[item.name];
    echartSeriasData.series[0].data[index].value = item.size;
    echartSeriasData.series[0].data[index].lv = item.lv;
    if (isEmpty) {
      echartSeriasData.series[0].data[index].itemStyle.normal.color = '#ededed';
    } else {
      echartSeriasData.series[0].data[index].itemStyle.normal.color = pieColor[index];
    }

  });
}

function initEcharts(){
  optionTempPI.value = deepCloneByJSON(optionTemp);
  optionTempRightPI.value = deepCloneByJSON(optionTempRight);
  actionPlan.value = deepCloneByJSON(optionAction);
  actionRightPlan.value = deepCloneByJSON(optionActionRight);
  errorAnalysisData.value = deepCloneByJSON(errorAnalysisDataOpt);
  layeringChecksData.value = deepCloneByJSON(errorAnalysisDataOpt);
  projectManage.value = deepCloneByJSON(errorAnalysisDataOpt);
  myTaskData.value = deepCloneByJSON(errorAnalysisDataOpt);

  errorAnalysis.value = echarts.init(document.getElementById('error-analysis'));

  workshowActionIdChart.value = echarts.init(document.getElementById('workshowActionId'));
  workshowActionIdRightChart.value = echarts.init(document.getElementById('workshowActionIdRight'));
  workshowActionIdChartt.value = echarts.init(document.getElementById('workshowActionIdt'));
  workshowActionIdRightChartt.value = echarts.init(document.getElementById('workshowActionIdRightt'));
  checkList.value = echarts.init(document.getElementById('check-list'));
  manage.value = echarts.init(document.getElementById('manage'));
  myTask.value = echarts.init(document.getElementById('my-task'));

}

//获取部门
async function getDept(){
  let params={
    dept:userStore?.userInfo?.orgCode
  }
  getDeptName(params).then(res => {
    if (res.success) {
      departName.value = res.result.name;
      departId.value = res.result.id;
      initData();
      getMillCount();
      getFindEmphasisType();
    }
  })
}
//公告
async function getNotice(){
  getAnnountCement({type:1}).then(res => {
    if (res.success) {
      let { result } = res;
      const allNews = result.map(item => {
        if (item.busId == 1)
          return item.msgContent;
        return '';
      });
      newsText.value =  allNews.filter(item => (item && !!item)).join('   ');
    } else {
      createMessage.warning(res.message);
    }
  })
}
onMounted(() => {
  getDept();
  initEcharts();
  getNotice();
  // Promise.all([findDeptByOrgCode(reportTypeMap.value)]).then(() => {
  // })
  //initData();
  // getMillCount();
  // getFindEmphasisType();
  getPayCloseAttentionPi();
});

</script>

<style lang='less' scoped>
.home-workshope {

  .pi-date {
    position: relative;
    left: 165px;
    top: -20px;
  }

  .kpi-pi {
    display: flex;
    justify-content: space-between
  }

  .card-item {
  }

  .card-item:last-child {
    margin-right: 0px;
  }

  .pi-card-item {
    flex: 2 1 100px;
  }

  .pi-progress {
  }

  .top-title {
    height: 40px;
    width: 100%;
    background-color: white;
    line-height: 40px;
    border-radius: 5px;
    position: relative;
    border-top-left-radius: 5px;
    border-bottom-left-radius: 5px;
  }

  .half-style {
    height: 100%;

    .bg {
      background-color: white;
    }

    .table-title {
      font-weight: bold;
      margin-bottom: 10px;
      display: block;
      font-size: 15px;
    }
  }

  .content {
    //height: 150px;
    //width: 100%;
    //position: relative;
    //font-size: 12px;

    .title-name {
      padding: 6px 0;
      font-size: 12px;
    }

    .title {
      width: 100%;
      float: left;
      line-height: 18px;
      text-align: center;
    }
  }

  .title-name-pi {
    /*margin-bottom: 23px; */
    height: 36px;
  }

  .echart-item {
    //
    width: 330px;
    height: 220px;
    left: calc((100% - 330px) / 2)
  }

  .pie {
    position: absolute;
    width: 128px;
    height: 160px;
    background: url(/@/assets/images/query.jpg);
    background-repeat: no-repeat;
    background-position: center 6px;
    background-size: cover;
  }

  .pie-action {
    // position: absolute;
    width: 128px;
    height: 160px;
    background: url(/@/assets/images/query.jpg);
    background-repeat: no-repeat;
    background-position: center 6px;
    background-size: cover;
  }

  .bg-img {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .table-content {
    height: 600px;
  }

  .rolling-style {
    // margin-right: 200px;
    position: relative;
  }


  .workshow-action2 {
    width: 150px;
    height: 180px;
    /* height: 180px; */
    // position: absolute;
    // left: 180px;
  }

}

.ant-row {
  font-size: 16px;

  .ant-col {
    font-size: 16px;
    color: #181717;
  }
}

:deep(.radioNode .ant-radio-button-wrapper) {
  border-radius: 0px !important;
}

:deep(.ant-progress-text) {
  color: #181717;
}
</style>
<style>
.ant-progress-text {
  margin-left: 0px;
}
</style>
