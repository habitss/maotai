<template>
  <div class='gutter-example'>
    <!--滚动栏-->
    <a-row :gutter='[16, 16]' style="padding: 10px;">
      <a-col :span='24'>
        <top-rolling-news class='rolling-style' :newsData='newsText'></top-rolling-news>
      </a-col>
    </a-row>

    <!--工厂KPI达标率-->
    <a-row :gutter='[8, 8]'>
      <a-col :xs='24' :sm='12' :md='12' :lg='12' :xl='6' style="position: relative;">
        <a-card title='工厂KPI达标率' :headStyle='{ borderBottom: 0 }' :body-style='{ paddingTop: 0 }'>
          <div style="position: absolute; right: 6px; top: 16px" class="radioNode">
            <a-radio-group size="small" @change="kpiCompleteChange" v-model:value="kpiCompleteStatus" button-style="outline">
              <a-radio-button value="a">
                上周
              </a-radio-button>
              <a-radio-button value="b">
                上月
              </a-radio-button>
            </a-radio-group>
          </div>
          <a-spin :spinning='!showKpiPie' class='spinning'>
            <section class='box'>
              <MyPie id='2023A01' v-if='showKpiPie' :percent='KPI.rate' :num1='KPI.complate' :num2='KPI.noComplate'/>
            </section>
          </a-spin>
        </a-card>
      </a-col>
      <!--KPI达标率排名-->
      <a-col :xs='24' :sm='12' :md='12' :lg='12' :xl='6' style="position: relative;">
        <a-card title='KPI达标率排名' :headStyle='{ borderBottom: 0 }' :body-style='{ paddingTop: 0 }'>
          <div style="position: absolute; right: 6px; top: 16px" class="radioNode">
            <a-radio-group size="small" @change="kpiRanldChange" v-model:value="kpiRanldStatus" button-style="outline">
              <a-radio-button value="a">
                上周
              </a-radio-button>
              <a-radio-button value="b">
                上月
              </a-radio-button>
            </a-radio-group>
          </div>
          <a-spin :spinning='showKpiRanld' class='spinning'>
            <section class='box'>
              <div id='bar1' class='bar-home'></div>
            </section>
          </a-spin>
        </a-card>
      </a-col>
      <!--工厂PI达标率-->
      <a-col :xs='24' :sm='12' :md='12' :lg='12' :xl='6' style="position: relative;">
        <a-card title='工厂PI达标率' :headStyle='{ borderBottom: 0 }' :body-style='{ paddingTop: 0 }'>
          <div style="position: absolute; right: 6px; top: 16px" class="radioNode">
            <a-radio-group size="small" @change="piCompleteChange" v-model:value="piCompleteStatus" button-style="outline">
              <a-radio-button value="a">
                上周
              </a-radio-button>
              <a-radio-button value="b">
                上月
              </a-radio-button>
              <a-radio-button value="c">
                本月
              </a-radio-button>
            </a-radio-group>
          </div>
          <a-spin :spinning='!showPiPie' class='spinning'>
            <section class='box'>
              <MyPie v-if='showPiPie' ref="piPie" :percent='PI.rate' :id="'piEcharts'" :num1='PI.complate'
                :num2='PI.noComplate' />
            </section>
          </a-spin>
        </a-card>
      </a-col>
      <!--PI达标率排名-->
      <a-col :xs='24' :sm='12' :md='12' :lg='12' :xl='6' style="position: relative;">
        <a-card title='PI达标率排名' class='pi-rank-card' :headStyle='{ borderBottom: 0 }' :body-style='{ paddingTop: 0 }'>
          <div style="position: absolute; right: 6px; top: 16px" class="radioNode">
            <a-radio-group size="small" @change="piRanldChange" v-model:value="piRanldStatus" button-style="outline">
              <a-radio-button value="a">
                上周
              </a-radio-button>
              <a-radio-button value="b">
                上月
              </a-radio-button>
              <a-radio-button value="c">
                本月
              </a-radio-button>
            </a-radio-group>
          </div>
          <a-spin :spinning='showPiRanld' class='spinning'>
            <section class='box'>
              <div id='bar2' class='bar-home'></div>
            </section>
          </a-spin>
        </a-card>
      </a-col>
    </a-row>
    <!--重点关注KPI指标-->
    <a-row :gutter='[8, 8]'>
      <a-col :xs='24' :sm='12' :md='12' :lg='12' :xl='12'>
        <a-card title='重点关注KPI指标' :headStyle='{ borderBottom: 0 }' :body-style='{ paddingTop: 0 }' style='margin-top: 6px;'>
          <section class='' style='align-items: start;'>
            <div class='' style='width: 100%'>
              <a-table :columns='columnsKpi' :data-source='dataKpi' bordered size='small' :pagination='false'
                style="min-height: 140px;" class='table-bg' :scroll='{ x: true }'>
                <!-- <span v-for='acol in cols' v-bind:key='acol' :slot='acol.colItem'>{{ acol.title }}</span> -->
                <span v-for='acol in cols' v-bind:key='acol'>{{ acol.title }}</span>
              </a-table>
            </div>
          </section>
        </a-card>
        <a-card title='重点关注PI指标（本周）' :headStyle='{ borderBottom: 0 }' :body-style='{ paddingTop: 0 }'
          style='margin-top: 8px; height:280px'>
          <section class='' style='min-height: 230px;'>
            <div class='' style=''>
              <a-table :columns='columnsPi' :data-source='dataPi' bordered size='small' :pagination='false'
                class='table-bg' :scroll='{ y: 170 }'>
              </a-table>
            </div>
          </section>
        </a-card>
      </a-col>
      <!--车间龙虎榜-->
      <a-col :xs='24' :sm='12' :md='12' :lg='12' :xl='12'>
        <a-card title='车间龙虎榜' :headStyle='{ borderBottom: 0 }'
          :body-style='{ paddingTop: 0, boder: 24, paddingBottom: 0 }' style='margin-top: 6px;'>
          <div>
            <!-- 行动计划 -->
            <a-row :gutter='[12, 12]'>
              <a-col :xs='24' :sm='24' :md='24' :lg='12' :xl='12' style="position: relative;">
                <a-spin :spinning='showActionList' class='spinning'>
                  <factory-progress-item :initData='actionPlanData[0]'>
                  </factory-progress-item>
                </a-spin>
                <div style="position: absolute; right: 6px; top: 6px" class="radioNode">
                  <a-radio-group size="small" @change="actionChange" v-model:value="actionStatus" button-style="outline">
                    <a-radio-button value="a">
                      上周
                    </a-radio-button>
                    <a-radio-button value="b">
                      上月
                    </a-radio-button>
                    <a-radio-button value="c">
                      本月
                    </a-radio-button>
                  </a-radio-group>
                </div>
              </a-col>
              <!-- 分层检查 -->
              <a-col :xs='24' :sm='24' :md='24' :lg='12' :xl='12' style="position: relative;">
                <a-spin :spinning='showlayeringList' class='spinning'>
                  <factory-progress-item :initData='actionPlanData[1]'>
                  </factory-progress-item>
                </a-spin>
                <div style="position: absolute; right: 6px; top: 6px" class="radioNode">
                  <a-radio-group size="small" @change="layeringChange" v-model:value="layeringStatus" button-style="outline">
                    <a-radio-button value="a">
                      上周
                    </a-radio-button>
                    <a-radio-button value="b">
                      上月
                    </a-radio-button>
                    <a-radio-button value="c">
                      本月
                    </a-radio-button>
                  </a-radio-group>
                </div>
              </a-col>
            </a-row>
            <!-- COP完成率排名 -->
            <a-row :gutter='[12, 12]'>
              <a-col :xs='24' :sm='24' :md='24' :lg='12' :xl='12' style="position: relative;">
                <a-spin :spinning='showCopList' class='spinning'>
                  <factory-progress-item :initData='actionPlanData[2]'>
                  </factory-progress-item>
                </a-spin>
                <div style="position: absolute; right: 6px; top: 6px" class="radioNode">
                  <a-radio-group size="small" @change="copChange" v-model:value="copStatus" button-style="outline">
                    <a-radio-button value="a">
                      上周
                    </a-radio-button>
                    <a-radio-button value="b">
                      上月
                    </a-radio-button>
                    <a-radio-button value="c">
                      本月
                    </a-radio-button>
                  </a-radio-group>
                </div>
              </a-col>
              <!-- 期间核查完成率排名 -->
              <a-col :xs='24' :sm='24' :md='24' :lg='12' :xl='12' style="position: relative;">
                <a-spin :spinning='showCheckList' class='spinning'>
                  <factory-progress-item :initData='actionPlanData[3]'>
                  </factory-progress-item>
                </a-spin>
                <div style="position: absolute; right: 6px; top: 6px" class="radioNode">
                  <a-radio-group size="small" @change="checkChange" v-model:value="checkStatus" button-style="outline">
                    <a-radio-button value="a">
                      上周
                    </a-radio-button>
                    <a-radio-button value="b">
                      上月
                    </a-radio-button>
                    <a-radio-button value="c">
                      本月
                    </a-radio-button>
                  </a-radio-group>
                </div>
              </a-col>
            </a-row>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- <DepartAlert ref="DepartAlert" /> -->
  </div>
</template>

<script lang="ts" name="home-factory" setup>
import { ref, nextTick, defineExpose, defineProps, computed, onMounted } from 'vue';
import topRollingNews from './components/topRollingNews.vue';
import { router } from '/@/router';
import { useI18n } from '/@/hooks/web/useI18n';
import { useMessage } from "/@/hooks/web/useMessage";
import * as echarts from 'echarts';
import { useUserStore } from '/@/store/modules/user';
import MyPie from './components/pie.vue';
import FactoryProgressItem from './components/factoryProgressItem.vue';
import {factoryUsePageHome,factoryActionStatisticsData,factoryActionStatisticsPi,getAnnountCement, queryPayCloseAttentionPi} from './home.api';
import {piColumns, piData} from './home.data';
import dayjs from 'dayjs';

const { createMessage } = useMessage();
const userStore = useUserStore();

const newsText = ref("");
const actionPlanData = ref([{},{},{}] as any);
const showKpiPie = ref(true);
const showKpiRanld = ref(false);
const showPiPie = ref(true);


const kpiCompleteStatus = ref('a');
const kpiRanldStatus = ref('a');
const piCompleteStatus = ref('a');
const piRanldStatus = ref('a');
const actionStatus = ref('a');
const layeringStatus = ref('a');
const copStatus = ref('a');
const checkStatus = ref('a');

const showPiRanld =ref(false);
const columnsKpi =ref([] as any)
const dataKpi =ref([] as any)
const cols =ref([] as any)
const columnsPi = piColumns //?
const dataPi = ref([] as any) //?
const showActionList=ref(false)
const showlayeringList=ref(false)
const showCopList=ref(false)
const showCheckList=ref(false)
const rangeDateValue = ref([] as any)

const loadingMillCount = ref(false)




var colorList = ref(['red', '#e6cf4e', '#00ff00', '#0093ff']);
var datas = ref([
  { 'value': 14, 'name': '糖化车间' },
  { 'value': 54, 'name': '发酵车间' },
  { 'value': 29, 'name': '包装车间' },
  { 'value': 25, 'name': '系列四' }]);

let maxArr = (new Array(datas.value.length)).fill(100);

const KPI= ref({
  rate: '0',
  complate: '0',
  noComplate: '0'
}) as any;
const PI= ref({
  rate: '0',
  complate: '0',
  noComplate: '0'
}) as any;


const { t } = useI18n();
const props = defineProps({

});
const colWidth = computed(() => {

});


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
      'color': '#000000d9'
    }
  };
}

//工厂KPI达标率
function kpiCompleteChange(val){
  let params;
  if (val === 'a' || val.target.value === 'a') {
    params = getDateParame('week', 2)
    params = { ...params, functionName: 'kpiComplianceRate' }
  } else {
    params = getDateParame('month', 2)
    params = { ...params, functionName: 'kpiComplianceRate' }
  }
  showKpiPie.value = false;
  getKpiByCommon(params)
}
//KPI达标率排名
function kpiRanldChange(val){
  let params;
  if (val === 'a' || val.target.value === 'a') {
    params = getDateParame('week', 2)
    params = { ...params, functionName: 'kpiComplianceRank' }
  } else {
    params = getDateParame('month', 2)
    params = { ...params, functionName: 'kpiComplianceRank' }
  }
  showKpiRanld.value = true
  getKpiByCommon(params)

}
//工厂PI达标率
function piCompleteChange(val){
  let params;
  if (val === 'a' || val.target.value === 'a') {
    params = getDateParame('week', 2)
    params = { ...params, functionName: 'piComplianceRate' }
  } else if (val === 'b' || val.target.value === 'b') {
    params = getDateParame('month', 2)
    params = { ...params, functionName: 'piComplianceRate' }
  } else {
    params = getDateParame('month', 3)
    params = { ...params, functionName: 'piComplianceRate' }
  }
  showPiPie.value = false
  getKpiByCommon(params)
}


//PI达标率排名
function piRanldChange(val){
  let params;
  if (val === 'a' || val.target.value === 'a') {
    params = getDateParame('week', 2)
    params = { ...params, functionName: 'piComplianceRank' }
  } else if (val === 'b' || val.target.value === 'b') {
    params = getDateParame('month', 2)
    params = { ...params, functionName: 'piComplianceRank' }
  } else {
    params = getDateParame('month', 3)
    params = { ...params, functionName: 'piComplianceRank' }
  }
  showPiRanld.value = true
  getKpiByCommon(params)
}
//行动计划
function actionChange(val){
  let params;
  if (val === 'a' || val.target.value === 'a') {
    params = getDateParame('week', 2)
    params = { ...params, functionName: 'trackPlan' }
  } else if (val === 'b' || val.target.value === 'b') {
    params = getDateParame('month', 2)
    params = { ...params, functionName: 'trackPlan' }
  } else {
    params = getDateParame('month', 3)
    params = { ...params, functionName: 'trackPlan' }
  }
  showActionList.value = true
  getKpiByCommon(params)
}
//分层检查
function layeringChange(val){
  let params;
  if (val === 'a' || val.target.value === 'a') {
    params = getDateParame('week', 2)
    params = { ...params, functionName: 'manageCheck' }
  } else if (val === 'b' || val.target.value === 'b') {
    params = getDateParame('month', 2)
    params = { ...params, functionName: 'manageCheck' }
  } else {
    params = getDateParame('month', 3)
    params = { ...params, functionName: 'manageCheck' }
  }
  showlayeringList.value = true
  getKpiByCommon(params)
}
//COP完成率排名
function copChange(val){
  let params;
  if (val === 'a' || val.target.value === 'a') {
    params = getDateParame('week', 2)
    params = { ...params, functionName: 'cop' ,dateType: '1'}
  } else if (val === 'b' || val.target.value === 'b') {
    params = getDateParame('month', 2)
    params = { ...params, functionName: 'cop' ,dateType: '2'}
  } else {
    params = getDateParame('month', 3)
    params = { ...params, functionName: 'cop' ,dateType: '3'}
  }
  showCopList.value = true
  getKpiByCommon(params)
}
//期间核查完成率排名
function checkChange(val){
  let params;
  if (val === 'a' || val.target.value === 'a') {
    params = getDateParame('week', 2)
    params = { ...params, functionName: 'periodPlan',dateType: '1' }
  } else if (val === 'b' || val.target.value === 'b') {
    params = getDateParame('month', 2)
    params = { ...params, functionName: 'periodPlan',dateType: '2' }
  } else {
    params = getDateParame('month', 3)
    params = { ...params, functionName: 'periodPlan',dateType: '3' }
  }
  showCheckList.value = true
  getKpiByCommon(params)
}


//KPI达标率排名
async function getKpiByCommon(params) {
  return new Promise((resolve) => {
    factoryUsePageHome(params).then(res => {
      if (res.success) {
        let { result } = res;
        if (params['functionName'] == 'kpiComplianceRate') {
          KPI.value.rate = result['工厂KPI达标率']['lv'].toFixed(2);
          KPI.value.rate = Math.floor(KPI.value.rate * 10000) / 100;
          KPI.value.complate = result['工厂KPI达标率']['size'];
          if (result['工厂KPI达标率']['lv'] == 0) {
            KPI.value.noComplate = 1;
          } else {
            KPI.value.noComplate = Math.floor(result['工厂KPI达标率']['size'] / result['工厂KPI达标率']['lv']) - result['工厂KPI达标率']['size'];
          }
        } else if (params['functionName'] == 'piComplianceRate') {
          PI.value.rate = Math.floor(result['工厂PI达标率']['lv'].toFixed(2) * 10000) / 100;
          PI.value.complate = result['工厂PI达标率']['size'];
          PI.value.noComplate =result['工厂PI达标率']['totalSize'] - result['工厂PI达标率']['size'];

          nextTick(() => {
            //this.$refs['piPie'] && this.$refs['piPie'].refushEcharts('piEcharts', PI.value.rate, PI.value.complate, PI.value.noComplate)
          })
        } else if (params['functionName'] == 'kpiComplianceRank') {
          let KPIRateRank = [] as any;
          if (Array.isArray(result['KPI达标率排名'])) {
            KPIRateRank = result['KPI达标率排名'].map((item, index) => {
              if (!item.name) return '';
              let cDataMap = {}
              item.data && item.data.length && item.data[0].forEach(op => {
                if (cDataMap[op['kpiName']]) cDataMap[op['kpiName']]++;
                else cDataMap[op['kpiName']] = 1
              });
              return { 'name': item.name, 'value': Math.ceil(item.lv * 10000) / 100, 'size': item.size, cDataMap };
            });
          }
          KPIRateRank = KPIRateRank.filter(item => item.name && !!item.name);
          setTimeout(() => {
            initChart('bar1', KPIRateRank, [], 'KPI达标率排名');
          }, 300);
        } else if (params['functionName'] == 'piComplianceRank') {
          let PIRateRank = [] as any;
          if (Array.isArray(result['PI达标率排名'])) {
            PIRateRank = result['PI达标率排名'].map(item => {
              // return {'name':item.name, 'value':Math.floor(item.lv.toFixed(2)*100), 'size': item.size}
              let cDataMap = {}
              item.data && item.data.length && item.data.forEach(op => {
                if (cDataMap[op['piName']]) cDataMap[op['piName']]++;
                else cDataMap[op['piName']] = 1
              });
              return { 'name': item.name, 'value': Math.floor(item.lv * 10000) / 100, 'size': item.size, cDataMap };
            });
          }
          setTimeout(() => {
            initChart('bar2', PIRateRank, [], 'PI达标率排名');
          }, 300);
        } else if (params['functionName'] == 'trackPlan') {
          actionPlanData.value[0].titleRate = result['行动计划']['工厂行动计划完成率'];
          actionPlanData.value[0].list = result['行动计划']['行动计划完成率排名'];
          // actionPlanData.value[0].showTitleRate = true;
          actionPlanData.value[0].showTitle = true;
        } else if (params['functionName'] == 'manageCheck') {
          actionPlanData.value[1].titleRate = result['分层检查']['工厂整改行动计划完成率'] || '0';
          actionPlanData.value[1].list = result['分层检查']['分层检查完成率排名'] || [];
          actionPlanData.value[1].showTitleRate = true;
        } else if (params['functionName'] == 'cop') {
          actionPlanData.value[2].titleRate = '0';
          let copData = result['COP完成率排名'] ? result['COP完成率排名'] : []
          // 按完成率降序排序
          copData.sort((a, b) => (b.lv || 0) - (a.lv || 0));
          actionPlanData.value[2].list = copData;
          actionPlanData.value[2].showTitleRate = false;
        } else if (params['functionName'] == 'periodPlan') {
          actionPlanData.value[3].titleRate = '0';
          let periodData = result['期间核查完成率排名'] ? result['期间核查完成率排名'] : []
          // 按完成率降序排序
          periodData.sort((a, b) => (b.lv || 0) - (a.lv || 0));
          actionPlanData.value[3].list = periodData;
          actionPlanData.value[3].showTitleRate = false;
        }
        showKpiPie.value = true;
        showPiPie.value = true;
        showKpiRanld.value = false;
        showPiRanld.value = false;
        showActionList.value = false;
        showlayeringList.value = false;
        showCopList.value = false;
        showCheckList.value = false;
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


// 柱图
function initChart(id, xData, yData, title) {
  const newData = []
  // 只对 PI 达标率排名显示所有数据，其他的保持只显示前 8 个
	  if (title === 'PI达标率排名') {
		  // 1. 数据校验
		  if (!Array.isArray(xData) || xData.length === 0) {
			  console.warn('PI达标率排名数据为空');
			  return;
		  }
		  // 2. 按达标率降序排序（高在前）
		  let sortedData = [...xData];
		  sortedData.sort((a, b) => (b.value || 0) - (a.value || 0));
	  
		  const newData = sortedData.map(item => ({
			  name: item.name || '未命名',
			  value: Number(item.value) || 0,
			  cDataMap: item.cDataMap || {}
		  }));
	  
		  // 3. 获取或创建滚动容器
		  const container = document.getElementById(id);
		  if (!container) return;
	  
		  let innerId = id + '_inner';
		  let innerDiv = document.getElementById(innerId);
		  if (!innerDiv) {
			  innerDiv = document.createElement('div');
			  innerDiv.id = innerId;
			  innerDiv.style.width = '100%';
			  // 每行高度设为 40px（与 KPI 排名自然高度一致），最小 300px
			  const innerHeight = Math.max(300, newData.length * 25);
			  innerDiv.style.height = innerHeight + 'px';
			  container.innerHTML = '';
			  container.appendChild(innerDiv);
		  } else {
			  const innerHeight = Math.max(300, newData.length * 25);
			  innerDiv.style.height = innerHeight + 'px';
			  const oldChart = echarts.getInstanceByDom(innerDiv);
			  if (oldChart) oldChart.dispose();
		  }
	  
		  // 4. 延迟渲染，确保容器尺寸正确
		  setTimeout(() => {
			  if (innerDiv.clientWidth === 0 || innerDiv.clientHeight === 0) {
				  console.warn('innerDiv 尺寸为0');
				  return;
			  }
			  const chart = echarts.init(innerDiv);
	  
			  // 5. 完全复用 KPI 达标率排名的 option 配置（与 KPI 一模一样）
			  const colorList = ['red', '#e6cf4e', '#00ff00', '#0093ff'];
			  const richHeight = 16;
			  const richWidth = 16;
	  
			  const option = {
				  tooltip: {
					  trigger: 'axis',
					  position: 'bottom',
					  axisPointer: { type: 'shadow' },
					  formatter: '<div>{b0}</div>{a0}: <label style="font-weight: bold;">{c0}%</label>'
				  },
				  legend: { show: false },
				  grid: {
					  top: 0,
					  bottom: 0,
					  left: -22,
					  right: 0,
					  containLabel: true
				  },
				  xAxis: { show: false, type: 'value' },
				  yAxis: [
					  {
						  type: 'category',
						  inverse: true,
						  axisLine: { show: false },
						  axisTick: { show: false },
						  data: newData.map(item => item.name),
						  axisLabel: {
							  interval: 0,
							  fontSize: 14,
							  align: 'left',
							  margin: 96,
							  color: '#333',
							  rich: {
								  a1: { color: '#fff', backgroundColor: colorList[0], width: richWidth, height: richHeight, align: 'center', borderRadius: [15, 0, 0, 15] },
								  a2: { color: '#fff', backgroundColor: colorList[0], width: richWidth, height: richHeight, align: 'center', borderRadius: [15, 0, 0, 15] },
								  a3: { color: '#fff', backgroundColor: colorList[0], width: richWidth, height: richHeight, align: 'center', borderRadius: [15, 0, 0, 15] },
								  b: { color: '#fff', backgroundColor: colorList[0], width: richWidth, height: richHeight, align: 'center', borderRadius: [15, 0, 0, 15] }
							  },
							  formatter: function (params) {
								  const index = newData.map(item => item.name).indexOf(params) + 1;
								  return `{b|${index}}  `;
							  }
						  }
					  },
					  {
						  type: 'category',
						  inverse: true,
						  axisTick: 'none',
						  axisLine: 'none',
						  show: true,
						  data: newData.map(item => item.value),
						  axisLabel: {
							  interval: 0,
							  show: true,
							  fontSize: 14,
							  color: '#333',
							  formatter: '{value}%'
						  }
					  }
				  ],
				  series: [
					  {
						  z: 2,
						  name: '达标率',
						  type: 'bar',
						  barWidth: 11,
						  barBorderRadius: 24,
						  zlevel: 1,
						  data: newData.map(item => ({
							  value: item.value,
							  itemStyle: { color: item.value > 80 ? '#00ff00' : 'red', barBorderRadius: 24 }
						  })),
						  label: {
							  show: true,
							  position: 'left',
							  color: '#333333',
							  formatter: '{b}',
							  fontSize: 14,
							  offset: [-2, 0]
						  }
					  },
					  {
						  name: '背景',
						  type: 'bar',
						  barWidth: 11,
						  barGap: '-100%',
						  itemStyle: { normal: { color: 'rgba(118, 111, 111, 0.1)', barBorderRadius: 24 } },
						  data: Array(newData.length).fill(100)
					  }
				  ],
				  dataZoom: []
			  };
	  
			  chart.setOption(option);
			  chart.on('click', (params) => {
				  if (params.componentType === 'series' && params.dataIndex !== undefined) {
					  const item = newData[params.dataIndex];
					  if (item && item.cDataMap) showDepartList(item.cDataMap);
				  }
			  });
			  window.addEventListener('resize', () => chart.resize());
		  }, 50);
		  return;
	  }else{
		// 其他图表（KPI 达标率排名等）保持原有逻辑，只取前 8 个
		xData.forEach((op, index) => { if (index < 8) newData.push(op); });
	}
	
    
  var getchart = echarts.init(document.getElementById(id));
  const richHeight = 16;
  const richWidth = 16;
  var option = {
    title: [
      {
        show: true,
        textStyle: {
          color: '#333',
          fontSize: 14
        },
        left: 30,
        top: 0
      }
    ],
    tooltip: {
      trigger: 'axis',
      position: 'bottom',
      axisPointer: {
        type: 'shadow'
      },
      formatter: '<div>{b0}</div>{a0}: <label style="font-weight: bold;">{c0}</label>'
    },
    legend: {
      show: false
    },
    grid: {
      top: 0,
      bottom: 0,
      left: -22,
      right: 0,
      containLabel: true
    },

    xAxis: {
      show: false,
      type: 'value'
    },
    yAxis: [{
      type: 'category',
      inverse: true,
      axisLine: { show: false },
      axisTick: { show: false },
      data: newData.map(item => item.name || '暂无'),
      axisLabel: {
        interval: 0,
        fontSize: 14,
        align: 'left',
        margin: 96,
        color: '#333',
        rich: {
          a1: {
            color: '#fff',
            backgroundColor: colorList.value[0],
            width: richWidth,
            height: richHeight,
            align: 'center',
            borderRadius: [15, 0, 0, 15]
          },
          a2: {
            color: '#fff',
            backgroundColor: colorList.value[0],
            width: richWidth,
            height: richHeight,
            align: 'center',
            borderRadius: [15, 0, 0, 15]
          },
          a3: {
            color: '#fff',
            backgroundColor: colorList.value[0],
            width: richWidth,
            height: richHeight,
            align: 'center',
            borderRadius: [15, 0, 0, 15]
          },
          b: {
            color: '#fff',
            backgroundColor: colorList.value[0],
            width: richWidth,
            height: richHeight,
            align: 'center',
            borderRadius: [15, 0, 0, 15]
          }
        },
        formatter: function (params) {
          var index = newData.map(item => item.name).indexOf(params);
          index = index + 1;
          return ['{b|' + index + '}' + '  '].join('\n');
        }
      }
    },
    {
      type: 'category',
      inverse: true,
      axisTick: 'none',
      axisLine: 'none',
      show: true,
      data: newData.map((item) => item.value),
      axisLabel: {
        interval: 0,
        show: true,
        fontSize: 14,
        color: '#333',
        formatter: '{value}%'
      }
    }],
    series: [{
      z: 2,
      name: '达标率',
      type: 'bar',
      barWidth: 11,
      barBorderRadius: 24,
      zlevel: 1,
      data: newData.map((item, i) => {
        // let color = new this.$echarts.graphic.LinearGradient(0, 0, 0, 1, ['#F55855','#FF9391'])
        let color = 'red';
        if (item.value > 80) {
          color = color = '#00ff00';
        }

        let itemStyle = {
          color: color,
          barBorderRadius: 24
        };

        let label = {
          show: true,
          width: 5
        }
        // return item.value/100
        return {
          value: item.value,
          itemStyle: itemStyle,
        };
      }),
      label: {
        show: true,
        position: 'left',
        color: '#333333',
        formatter: '{b}',
        fontSize: 14,
        // offset: [-20, 0]
        offset: [-2, 0]
      }
    },
    {
      name: '背景',
      type: 'bar',
      barWidth: 11,
      barGap: '-100%',
      itemStyle: {
        normal: {
          // #EFEFEF
          color: 'rgba(118, 111, 111, 0.1)',
          barBorderRadius: 24
        }
      },
      data: Array(xData.length).fill(100)
    }
    ],
    dataZoom: []
  } as any;

  if (newData.length > 8) {
    option.dataZoom = [];
  }

  // 4.3 更新配置
  getchart.setOption(option);
  // let _self = this;
  getchart.on('click', function (params) {
    showDepartList(newData[params['dataIndex']].cDataMap);
    // if (params.componentType === "bar") {
    // }
  });
  //随着屏幕大小调节图表
  window.addEventListener('resize', () => {
    getchart.resize();
  });
}

async function getPayAttentionKPi() {
  return new Promise((resolve) => {
    //PI指标
    //const userInfo = getUserInfo();
    let params = {
      'factory': userStore.factoryCode,
      // "opTime": dayjs().subtract(30, 'day').format('YYYY-MM-DD HH:MM:ss'),
      'year': dayjs().format('YYYY'),
      'reportId': userStore.orgCode || userStore.factoryCode,   //初次登录没有分配工段时，给默认值
      'showChildrenKpi': false,
      'showWeek': true,
      'dept':  userStore?.userInfo?.orgCode
    };
    loadingMillCount.value = true;
    factoryActionStatisticsData(params).then(res => {
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

//详情
function showDepartList(data) {
  let initData = []
  for (const keys in data) {
    initData.push({ name: keys, value: data[keys] })
  }
  //this.$refs['DepartAlert'] && this.$refs['DepartAlert'].init(initData, '不达标指标详情')
}
function initGet() {
  kpiCompleteChange('a')
  kpiRanldChange('a')
  piCompleteChange('a')
  piRanldChange('a')
  actionChange('a')
  layeringChange('a')
  copChange('a')
  checkChange('a')
  getPayCloseAttentionPi();
  getPayAttentionKPi();
}

function setKpiAttr(KPiData) {
  const allCol = [{ 'name': 'KPI', 'biaozhun': '标准', 'yue': '上月', 'week': '上周', 'id': '88' }].concat(KPiData);
  columnsKpi.value = setColumnsArr(allCol);
  cols.value = columnsKpi.value.map(item => {
    return { 'colItem': item.slots.title, 'title': item.titleText };
  });

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
      // slots: { title: `${item.id}_title` },
      slots: { customRender: `${item.id}_title` },
      dataIndex: item.id,
      align: 'center',
      // width: (item.name.length * 22) + 'px',
      titleText: item.name,
      title: item.name,
      // customRender: function (value) {
      //   let record = value.record;
      //   let index = value.index;
      //   return setTableCellColor(record[item.id + '_colory'], index, value) 
      // },
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

async function getPayCloseAttentionPi() {
  const { result } = await queryPayCloseAttentionPi({});
  dataPi.value = result;
  // return new Promise((resolve) => {
  //   //PI指标
  //   let params = {
  //     'factory': userStore.factoryCode,
  //     'opTime': rangeDateValue[0] + ' 00:00:00',
  //     'edTime': rangeDateValue[1] + ' 23:59:59'
  //   };
  //   loadingMillCount.value = true;
  //   factoryActionStatisticsPi(params).then(res => {
  //     if (res.success) {
  //       let { result } = res;
  //       dataPi.value = result;

  //       dataPi.value.forEach(item => {
  //         item.lvDlv = `${Math.floor(item.lvDlv * 100)}%`;
  //       });
  //     } else {
  //       createMessage.warning(res.message);
  //     }
  //   }).finally(() => {
  //     loadingMillCount.value = false;
  //   });
  // });
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
  rangeDateValue.value = [dayjs().subtract(30, 'day').format("YYYY-MM-DD"), dayjs().subtract(0, 'day').format("YYYY-MM-DD")];
  actionPlanData.value = [
      {
        'title': '行动计划',
        'titleRateText': '工厂行动计划完成率：',
        'titleRate': '0.5',
        'subTitle': '行动计划完成率排名：',
        'list': [{ 'rate': '0.3', 'name': '无数据' }]
      },
      {
        'title': '分层检查',
        'titleRateText': '工厂整改行动计划完成率：',
        'titleRate': '0.8',
        'subTitle': '分层检查问题完成率排名：',
        'list': []
      },
      {
        'title': 'COP完成率排名',
        'titleRateText': '',
        'titleRate': '',
        'subTitle': '',
        'list': []
      },
      {
        'title': '期间核查完成率排名',
        'titleRateText': '',
        'titleRate': '',
        'subTitle': '',
        'list': []
      }
    ]


  initGet();
  //获取公告
  getNotice();
});


</script>

<style lang="less" scoped>
.select-top-right {
  float: right;
  margin-bottom: 8px;
}

.box {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 145px;
  position: relative;
}

.bar-home {
  flex: 1;
  height: 145px;
}

.pi-rank-card {
  .box {
    height: 145px !important;      // 与左边卡片一致
    display: flex !important;
    flex-direction: column;
    justify-content: flex-start;   // 内容从顶部开始
    align-items: stretch;          // 宽度占满
  }
  #bar2 {
    flex: 1;
    overflow-y: auto;
	overflow-x: hidden;
    min-height: 0;                 // 关键：允许flex子项滚动
  }
}

.gutter-box {
  background: #00a0e9;
  padding: 5px 0;
}

.gutter-box-tips {
  background: #fff;
  padding: 18px;
}

.select {
  position: absolute;
  right: 10px;
  top: 10px;
  z-index: 999;
}

.table-title {
  font-weight: 500;
  margin-bottom: 10px;
  display: block;
  font-size: 15px;
}

.factory-title {
  font-weight: 500;
  font-size: 15px;
  color: #444444;
}

.rolling-style {

  position: relative;
}

.date-pick {
  width: 200px;
  display: block;
  position: absolute;
  right: 9px;
  top: 8px;
}

:deep(.ant-calendar-picker-input) {
  height: 40px;
  border: 0px solid gray;
}

.spinning {
  margin: auto;
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


</style>
<style>
.gutter-example>>>.ant-row>div {
  background: transparent;
  border: 0;
}
</style>
