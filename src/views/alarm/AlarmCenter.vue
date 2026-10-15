<template>
  <div class="alarm-center">
    <!-- 头部标题 -->
    <div class="header">
      <h1>{{ currentFactory.departName }}报警中心</h1>
      <div class="header-right">
        <div class="time">{{ currentTime }}</div>
        <div v-if="showFactorySelector" class="factory-selector">
          <select v-model="currentFactory" @change="changeFactory(currentFactory)">
            <option v-for="factory in factoryList" :key="factory.id" :value="factory">{{ factory.departName }}</option>
          </select>
        </div>
      </div>
    </div>

    <!-- 主体内容 -->
    <div class="main-content">
      <!-- 左侧列：占1/3宽度 -->
      <div class="left-column">
        <!-- 报警监控 -->
        <div class="panel alarm-monitor">
          <h3 class="panel-title">报警监控</h3>
          <div class="monitor-content">
            <div class="monitor-image">
              <img src="/resource/img/alarm/video.png" alt="监控画面" />
            </div>
            <div class="monitor-info">
              <div class="info-item">
                <span class="label">产线：</span>
                <span class="value">{{ data.latestAlarm.lineCodeVal || data.latestAlarm.lineCode }}</span>
              </div>
              <div class="info-item">
                <span class="label">设备：</span>
                <span class="value">{{ data.latestAlarm.deviceCodeVal || data.latestAlarm.deviceCode }}</span>
              </div>
              <div class="info-item">
                <span class="label">报警类型：</span>
                <span class="value">{{ data.latestAlarm.alarmCategory }}</span>
              </div>
              <div class="info-item">
                <span class="label">报警等级：</span>
                <span class="value level-1">{{ data.latestAlarm.alarmLevel }}</span>
              </div>
              <div class="info-item">
                <span class="label">在岗人员：</span>
                <span class="value">{{ data.latestAlarm.onDutyPerson }}</span>
              </div>
              <div class="info-item">
                <span class="label">时间：</span>
                <span class="value">{{ data.latestAlarm.alarmTime }}</span>
              </div>
              <div class="info-item alarm-desc">
                <span class="label">报警信息：</span>
                <span class="value">{{ data.latestAlarm.alarmName }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 待办事项 -->
        <div class="panel todo-list">
          <h3 class="panel-title">待办事项</h3>
          <div class="todo-content">
            <div class="todo-table">
              <table>
                <thead>
                  <tr>
                    <th>产线</th>
                    <th>设备</th>
                    <th>报警描述</th>
                    <th style="width: 70px">在岗人员</th>
                    <th>时间</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(item, index) in data.todoData" :key="index">
                    <td>{{ item.lineCodeVal || item.lineCode }}</td>
                    <td>{{ item.deviceCodeVal || item.deviceCode }}</td>
                    <td>{{ item.alarmName }}</td>
                    <td>{{ item.onDutyPerson }}</td>
                    <td>{{ item.alarmTime }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧列：占2/3宽度 -->
      <div class="right-column">
        <!-- 报警分析 -->
        <div class="panel alarm-analysis">
          <h3 class="panel-title" style="padding-left: 35px">报警分析</h3>
          <div class="analysis-content">
            <!-- 左侧：四个环图 -->
            <div style="padding: 0px 10px 45px 15px; width: 20%">
              <div class="analysis-left">
                <div v-for="(item, index) in data.statusList" :key="index" class="pie-item">
                  <div class="pie-chart">
                    <Progress
                      type="circle"
                      :percent="item.percent"
                      :size="60"
                      :strokeColor="item.color"
                      trailColor="#000000"
                      :format="(percent) => `${percent}%`"
                    />
                  </div>
                  <div class="pie-info">
                    <div class="pie-label">{{ item.label }}</div>
                    <div class="pie-value">{{ item.value }} <span class="unit">条</span></div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 分隔线 -->
            <div class="split-line-container">
              <img src="/resource/img/alarm/split-line.png" alt="" />
            </div>

            <!-- 中间：同比环比和报警类型 -->
            <div class="analysis-middle">
              <!-- 同比环比 -->
              <div class="compare-section">
                <h4 class="section-title">同比环比</h4>
                <div class="compare-items">
                  <div class="compare-item">
                    <div class="item-content">
                      <div class="item-icon">
                        <img src="/resource/img/alarm/exp-icon.png" alt="" />
                      </div>
                      <div class="item-text">
                        <div class="text-row">
                          <span class="text-label">今日异常</span>
                          <span class="text-value">{{ data.comparisonData.todayCnt }}<span class="unit">个</span></span>
                        </div>
                        <div class="text-row">
                          <span class="text-label">昨日异常</span>
                          <span class="text-value">{{ data.comparisonData.yesterdayCnt }}<span class="unit">个</span></span>
                        </div>
                        <div class="text-row">
                          <span class="text-label">环比{{ data.comparisonData.dayCompareRate > 0 ? '增长' : '下降' }}</span>
                          <span class="text-value" :class="data.comparisonData.dayCompareRate > 0 ? 'up' : 'down'"
                            >{{ Math.abs(data.comparisonData.dayCompareRate) }} <span class="unit">%</span>
                            <img v-if="data.comparisonData.dayCompareRate > 0" src="/resource/img/alarm/up_red.png" alt="上升" />
                            <img v-if="data.comparisonData.dayCompareRate < 0" src="/resource/img/alarm/down_green.png" alt="下降" />
                          </span>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div class="compare-item">
                    <div class="item-content">
                      <div class="item-icon">
                        <img src="/resource/img/alarm/exp-icon.png" alt="" />
                      </div>
                      <div class="item-text">
                        <div class="text-row">
                          <span class="text-label">本月异常</span>
                          <span class="text-value">{{ data.comparisonData.monthCnt }}<span class="unit">个</span></span>
                        </div>
                        <div class="text-row">
                          <span class="text-label">上月异常</span>
                          <span class="text-value">{{ data.comparisonData.lastMonthCnt }} <span class="unit">个</span></span>
                        </div>
                        <div class="text-row">
                          <span class="text-label">环比{{ data.comparisonData.monthCompareRate > 0 ? '增长' : '下降' }}</span>
                          <span class="text-value" :class="data.comparisonData.monthCompareRate > 0 ? 'up' : 'down'"
                            >{{ Math.abs(data.comparisonData.monthCompareRate) }} <span class="unit">%</span>
                            <img v-if="data.comparisonData.monthCompareRate > 0" src="/resource/img/alarm/up_red.png" alt="上升" />
                            <img v-if="data.comparisonData.monthCompareRate < 0" src="/resource/img/alarm/down_green.png" alt="下降" />
                          </span>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div class="compare-item">
                    <div class="item-content">
                      <div class="item-icon">
                        <img src="/resource/img/alarm/exp-icon.png" alt="" />
                      </div>
                      <div class="item-text">
                        <div class="text-row">
                          <span class="text-label">当年异常</span>
                          <span class="text-value">{{ data.comparisonData.yearCnt }} <span class="unit">个</span></span>
                        </div>
                        <div class="text-row">
                          <span class="text-label">去年异常</span>
                          <span class="text-value">{{ data.comparisonData.lastYearCnt }} <span class="unit">个</span></span>
                        </div>
                        <div class="text-row">
                          <span class="text-label">同比{{ data.comparisonData.yearCompareRate > 0 ? '增长' : '下降' }}</span>
                          <span class="text-value" :class="data.comparisonData.yearCompareRate > 0 ? 'up' : 'down'"
                            >{{ Math.abs(data.comparisonData.yearCompareRate) }} <span class="unit">%</span>
                            <img v-if="data.comparisonData.yearCompareRate > 0" src="/resource/img/alarm/up_red.png" alt="上升" />
                            <img v-if="data.comparisonData.yearCompareRate < 0" src="/resource/img/alarm/down_green.png" alt="下降" />
                          </span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 报警类型 -->
              <div class="alarm-type-section">
                <h4 class="section-title">报警类型</h4>
                <div class="alarm-type-content">
                  <div class="pie-chart-large">
                    <Pie ref="pieChartRef" :chart-data="data.alarmTypeData" :option="alarmTypeOption" :height="'150px'" :width="'150px'" />
                  </div>
                  <div class="alarm-type-list">
                    <div class="type-item" v-for="(item, index) in data.alarmTypeList" :key="index" :class="'type-' + index">
                      <div class="type-dot" :class="'type-' + index"></div>
                      <div class="type-name">{{ item.name }}</div>
                      <div class="type-count">{{ item.count }}<span class="unit">条</span></div>
                      <div class="type-percent">{{ item.percent }}%</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 分隔线 -->
            <div class="split-line-container">
              <img src="/resource/img/alarm/split-line.png" alt="" />
            </div>

            <!-- 右侧：设备异常排名 -->
            <div class="analysis-right">
              <h3 class="panel-title">设备异常排名Top5</h3>
              <div class="rank-content">
                <div class="rank-item" v-for="(item, index) in data.deviceRank.slice(0, 5)" :key="index" :class="'rank-item-' + index">
                  <div class="rank-name">{{ item.name }}</div>
                  <div class="rank-value">{{ item.value }}<span class="unit">次</span></div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 报警信息 -->
        <div class="panel alarm-info">
          <h3 class="panel-title">报警信息</h3>
          <div class="info-content">
            <table class="info-table">
              <thead>
                <tr>
                  <th>生产线</th>
                  <th>设备</th>
                  <th>报警等级</th>
                  <th>报警类型</th>
                  <th>在岗人员</th>
                  <th>报警时间</th>
                  <th>报警描述</th>
                  <th>响应状态</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(item, index) in data.alarmInfoData" :key="index">
                  <td>{{ item.lineCodeVal || item.lineCode }}</td>
                  <td>{{ item.deviceCodeVal || item.deviceCode }}</td>
                  <td
                    ><span :class="'level-' + item.alarmLevel">{{ item.alarmLevel }}</span></td
                  >
                  <td>{{ item.alarmCategory }}</td>
                  <td>{{ item.onDutyPerson }}</td>
                  <td>{{ item.alarmTime }}</td>
                  <td>{{ item.alarmName }}</td>
                  <td :class="item.status === '已处理' ? 'status-handled' : 'status-processing'">
                    {{ item.statusName }}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" name="AlarmCenter" setup>
  import { reactive, ref, onMounted, onUnmounted, watch, nextTick, computed } from 'vue';
  import { Progress } from 'ant-design-vue';
  import Pie from '/@/components/chart/Pie.vue';
  import { comparison, typeCount, topFive, statusCount, list, todoList } from './AlarmCenter.api';
  import { getUserDeparts } from '/@/views/system/depart/depart.api';

  // 动态时间
  const currentTime = ref('');
  let timer: number | null = null;

  // 自动滚动相关
  let autoScrollTimer: number | null = null;
  const currentPage = ref(1);
  const hasMoreData = ref(true);
  const maxPage = ref(10);

  // 工厂相关
  const factoryList = ref([]);
  const currentFactory = ref({});
  const showFactorySelector = ref(false);

  // 获取图表组件的引用
  const pieChartRef = ref();

  // 格式化时间
  const formatTime = () => {
    const date = new Date();
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const weekDays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'];
    const weekDay = weekDays[date.getDay()];
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');

    currentTime.value = `${year}.${month}.${day}　${weekDay}　${hours}:${minutes}`;
  };

  /**
   * 数据
   */
  const data = reactive({
    //1.同比环比
    comparisonData: {
      lastMonthCnt: 0,
      monthCnt: 0,
      lastYearCnt: 0,
      yearCompareRate: 0,
      yesterdayCnt: 0,
      todayCnt: 0,
      yearCnt: 0,
      monthCompareRate: 0,
      dayCompareRate: 0,
    },

    //2.报警类型
    typeCountData: [],
    // 报警类型数据
    alarmTypeData: [
      { value: 30, name: '超温报警', itemStyle: { color: '#52c41a' } },
      { value: 15, name: '安装报警', itemStyle: { color: '#1890ff' } },
      { value: 15, name: '安全报警', itemStyle: { color: '#f7ba2a' } },
      { value: 20, name: '效能报警', itemStyle: { color: '#9254de' } },
      { value: 20, name: '低成报警', itemStyle: { color: '#ff4d4f' } },
    ],
    // 报警类型列表
    alarmTypeList: [
      { name: '超温报警', count: 6, percent: '30.00%' },
      { name: '安装报警', count: 3, percent: '15.00%' },
      { name: '安全报警', count: 3, percent: '15.00%' },
      { name: '效能报警', count: 4, percent: '20.00%' },
      { name: '低成报警', count: 4, percent: '20.00%' },
    ],
    // 总报警数量
    totalAlarmCount: 0,

    //3.设备异常排名Top5
    topFiveData: [],
    // 设备异常排名数据
    deviceRank: [
      { name: '6#1换热器', value: 10, percent: '100%' },
      { name: '2#7PPVP', value: 8, percent: '80%' },
      { name: '5#8发酵罐', value: 6, percent: '60%' },
      { name: '精滤罐', value: 5, percent: '50%' },
      { name: '1#6罐', value: 5, percent: '50%' },
      { name: '酸化锅', value: 5, percent: '50%' },
    ],

    //4.报警分析
    statusCountData: [],
    statusList: [],
    statusNameMap: {
      '0': '未响应',
      '1': '已响应',
      '2': '处理中',
      '3': '已处理',
    },
    statusColorMap: {
      '0': '#36cfc9',
      '1': '#f7ba2a',
      '2': '#9254de',
      '3': '#52c41a',
    },

    //5.报警信息
    alarmInfoData: [],

    //6.待办事项
    todoData: [],

    //7.报警监控
    latestAlarm: {},
  });

  // 自动滚动逻辑
  function autoScrollToNextPage() {
    if (hasMoreData.value && currentPage.value < maxPage.value) {
      currentPage.value++;
    } else {
      currentPage.value = 1;
    }
    getAlarmInfoList();
  }

  // 组件挂载时启动定时器
  onMounted(async () => {
    formatTime();
    timer = window.setInterval(formatTime, 10000);

    // 加载部门信息
    await loadDepartList();

    // 启动自动滚动定时器，每分钟请求一次
    autoScrollTimer = window.setInterval(autoScrollToNextPage, 30000);
  });

  //1.同比环比
  async function getComparison() {
    const params = { factoryCode: currentFactory.value.orgCode };
    const res = await comparison(params);
    data.comparisonData = res || {
      lastMonthCnt: 0,
      monthCnt: 0,
      lastYearCnt: 0,
      yearCompareRate: 0,
      yesterdayCnt: 0,
      todayCnt: 0,
      yearCnt: 0,
      monthCompareRate: 0,
      dayCompareRate: 0,
    };
  }

  //2.报警类型
  async function getTypeCount() {
    const params = { factoryCode: currentFactory.value.orgCode };
    const res = (await typeCount(params)) || [];
    data.typeCountData = res;

    //饼图数据
    const alarmTypeData = [] as any;
    res.forEach((vo) => {
      const obj = {
        name: vo.item ?? '其它',
        value: vo.cnt,
      };
      alarmTypeData.push(obj);
    });
    data.alarmTypeData = alarmTypeData;

    //列表数据
    const alarmTypeList = [] as any;
    const totalCnt = res.reduce((sum, item) => sum + (item.cnt || 0), 0);
    res.forEach((vo) => {
      const name = vo.item ?? '其它';
      const count = vo.cnt;
      const percent = totalCnt > 0 ? ((vo.cnt * 100) / totalCnt).toFixed(2) : 0;
      const obj = {
        name: name,
        count: count,
        percent: percent,
      };
      alarmTypeList.push(obj);
    });
    data.alarmTypeList = alarmTypeList;

    // 计算总条数
    data.totalAlarmCount = data.alarmTypeList.reduce((total, item) => total + item.count, 0);
  }

  //3.设备异常排名Top5
  async function getTopFive() {
    const params = { factoryCode: currentFactory.value.orgCode };
    const res = (await topFive(params)) || [];
    data.topFiveData = res;

    const deviceRank = [] as any;
    const totalCnt = res.reduce((sum, item) => sum + (item.cnt || 0), 0);
    res.forEach((vo) => {
      const name = vo.item ?? '其它';
      const count = vo.cnt;
      const percent = totalCnt > 0 ? ((vo.cnt * 100) / totalCnt).toFixed(2) : 0;
      const obj = {
        name: name,
        value: count,
        percent: percent + '%',
      };
      deviceRank.push(obj);
    });
    data.deviceRank = deviceRank;
  }

  //4.报警分析
  async function getStatusCount() {
    const params = { factoryCode: currentFactory.value.orgCode };
    const res = (await statusCount(params)) || [];
    data.statusCountData = res;

    const statusMap = {};
    res.forEach((vo) => {
      statusMap[vo.item] = vo.cnt;
    });
    const totalCnt = res.reduce((sum, item) => sum + (item.cnt || 0), 0);
    const statusList = [] as any;
    for (let i = 0; i < 4; i++) {
      const name = i + '';
      const count = statusMap[name] || 0;
      const percent = totalCnt > 0 ? ((count * 100) / totalCnt).toFixed(2) : 0;
      const label = data.statusNameMap[name];
      const color = data.statusColorMap[name];
      const obj = {
        name: name,
        value: count,
        percent: percent,
        label: label,
        color: color,
      };
      statusList.push(obj);
    }
    data.statusList = statusList;
  }

  //5.报警信息
  async function getAlarmInfoList() {
    const params = { pageNum: currentPage.value, factoryCode: currentFactory.value.orgCode };
    const res = await list(params);

    // 检查是否有数据
    if (res && res.records && res.records.length > 0) {
      data.alarmInfoData = res.records;
      hasMoreData.value = true;

      //左上角报警监控，取最新的一条报警数据
      let latestAlarm = {} as any;
      latestAlarm = res.records[0];
      data.latestAlarm = { ...latestAlarm };
    } else {
      // 没有数据，回到第一页
      currentPage.value = 1;
      hasMoreData.value = false;
      // 重新获取第一页数据
      const firstPageRes = await list({ pageNum: 1, factoryCode: currentFactory.value.orgCode });
      if (firstPageRes && firstPageRes.records && firstPageRes.records.length > 0) {
        data.alarmInfoData = firstPageRes.records;
        // 更新最新报警
        data.latestAlarm = { ...firstPageRes.records[0] };
      } else {
        // 清空数据
        data.alarmInfoData = [];
        data.latestAlarm = {};
      }
    }
  }

  //6.待办事项
  async function getTodoList() {
    const params = { factoryCode: currentFactory.value.orgCode };
    const res = await todoList(params);
    data.todoData = res || [];
  }

  /**
   *加载部门信息
   */
  async function loadDepartList() {
    const result = await getUserDeparts();
    if (!result.list || result.list.length == 0) {
      return;
    }

    // 筛选出 orgType = 3 的工厂
    const factories = result.list.filter((item) => item.orgType === '3');
    factoryList.value = factories;

    // 检查是否有多个工厂
    if (factories.length > 1) {
      showFactorySelector.value = true;
    }

    // 设置当前工厂，优先使用 result.orgCode 匹配的
    let currentDepart = result.list.find((item) => item.orgCode === result.orgCode);
    if (currentDepart) {
      currentFactory.value = currentDepart;
    } else if (factories.length > 0) {
      currentFactory.value = factories[0];
    }

    // 重新加载数据
    await refreshData();
  }

  /**
   * 切换工厂
   */
  async function changeFactory(factory) {
    currentFactory.value = factory;
    await refreshData();
  }

  /**
   * 刷新所有数据
   */
  async function refreshData() {
    await getComparison();
    await getTypeCount();
    await getTopFive();
    await getStatusCount();
    await getAlarmInfoList();
    await getTodoList();
  }

  // 组件卸载时清除定时器
  onUnmounted(() => {
    if (timer) {
      clearInterval(timer);
    }
    if (autoScrollTimer) {
      clearInterval(autoScrollTimer);
    }
  });

  // 报警类型饼图配置
  // 使用计算属性生成完整的配置
  const alarmTypeOption = computed(() => ({
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 0,
          borderColor: '#1f2937',
          borderWidth: 2,
        },
        label: {
          show: true,
          position: 'center',
          formatter: `${data.totalAlarmCount}`,
          fontSize: '16',
          color: '#fff',
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '14',
            fontWeight: 'bold',
            formatter: '{d}%',
          },
        },
        labelLine: {
          show: false,
        },
        data: data.alarmTypeData,
      },
    ],
  }));

  // 监听数据变化，手动更新图表
  watch(
    [() => data.totalAlarmCount, () => data.alarmTypeData],
    () => {
      nextTick(() => {
        if (pieChartRef.value && pieChartRef.value.chart) {
          // 方法1：设置新选项
          //pieChartRef.value.chart.setOption(alarmTypeOption.value, true);
          // 方法2：或者只更新数据部分
          pieChartRef.value.chart.setOption({
            series: [
              {
                data: data.alarmTypeData,
                label: {
                  formatter: `${data.totalAlarmCount}条`,
                },
              },
            ],
          });
        }
      });
    },
    { deep: true }
  );
</script>

<style scoped lang="less">
  .alarm-center {
    width: 100%;
    max-width: 100vw;
    aspect-ratio: 16/9;
    background-color: #050918;
    color: #fff;
    font-family: 'Microsoft YaHei', sans-serif;
    padding: 0;
    margin: 0;
    box-sizing: border-box;
    overflow: hidden;

    .header {
      display: flex;
      justify-content: center;
      align-items: center;
      margin-bottom: 10px;
      background-image: url('/resource/img/alarm/top.png');
      background-size: cover;
      background-position: center;
      padding: 10px 0;
      height: 80px;
      border-radius: 0;
      position: relative;

      h1 {
        font-size: 40px;
        font-weight: bold;
        margin: -5px 0 0 0;
        background: linear-gradient(180deg, #edf6ff 0%, #e5f2fe 28%, #164088 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
        text-fill-color: transparent;
      }

      .header-right {
        display: flex;
        align-items: center;
        gap: 20px;
        position: absolute;
        right: 10px;
        top: 50%;
        transform: translateY(-50%);
      }

      .time {
        font-size: 20px;
        color: #acc9e6;
        font-family: Arial, sans-serif;
        font-weight: bold;
      }

      .factory-selector {
        select {
          background-color: rgba(26, 31, 46, 0.8);
          color: #fff;
          border: 1px solid #2d3748;
          border-radius: 4px;
          padding: 5px 10px;
          font-size: 16px;
          outline: none;
          cursor: pointer;

          &:hover {
            border-color: #1890ff;
          }

          option {
            background-color: #1a1f2e;
            color: #fff;
          }
        }
      }
    }

    .main-content {
      display: flex;
      gap: 15px;
      height: calc(100% - 90px);
      padding: 0 15px 15px 15px;
    }

    /* 左侧列：占1/3宽度 */
    .left-column {
      width: 33.33%;
      display: flex;
      flex-direction: column;
      gap: 20px;
    }

    /* 右侧列：占2/3宽度 */
    .right-column {
      width: 66.67%;
      display: flex;
      flex-direction: column;
      gap: 20px;
    }

    .panel {
      background-color: #1a1f2e;
      border-radius: 8px;
      padding: 16px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);

      .panel-title {
        font-size: 16px;
        font-weight: bold;
        color: #fff;
        margin: 0 0 16px 0;
        padding-bottom: 8px;
        border-bottom: 1px solid #2d3748;
        display: flex;
        align-items: center;
        gap: 8px;

        &::before {
          content: '';
          display: inline-block;
          width: 24px;
          height: 24px;
          background-image: url('/resource/img/alarm/tit-prefix_1.png');
          background-size: contain;
          background-repeat: no-repeat;
          background-position: center;
        }
      }
    }

    /* 报警监控样式 */
    .alarm-monitor {
      flex: 1;
      min-height: 0;
      background-color: transparent;
      background-image: url('/resource/img/alarm/con-grid_0.png');
      background-size: 100% 100%;
      background-position: center;
      background-repeat: no-repeat;
      border-radius: 0;
      padding: 0;
      margin: 0;
      box-shadow: none;

      .panel-title {
        background: linear-gradient(180deg, rgba(82, 143, 204, 0.25) 0%, rgba(96, 142, 191, 0) 100%);
        padding: 8px 16px;
        border-radius: 0;
        margin: 0;
        border-bottom: none;
      }

      .monitor-content {
        display: flex;
        flex-direction: column;
        height: calc(100% - 32px);
        padding: 10px;
      }

      .monitor-image {
        width: 100%;
        height: calc(66.67% - 20px);
        overflow: hidden;
        margin-bottom: 20px;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
          clip-path: polygon(10px 0, calc(100% - 10px) 0, 100% 10px, 100% 100%, 0 100%, 0 10px);
        }
      }

      .monitor-info {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 12px;

        .info-item {
          display: flex;
          flex-direction: row;
          gap: 8px;

          .label {
            font-size: 14px;
            color: #fff;
            white-space: nowrap;
          }

          .value {
            font-size: 14px;
            color: #fff;

            &.level-1 {
              color: #fff;
              background-image: url('/resource/img/alarm/level_bg.png');
              background-size: cover;
              background-position: center;
              background-repeat: no-repeat;
              padding: 0px 7px;
              border-radius: 4px;
              width: 20px;
              height: 20px;
            }
          }
        }

        .alarm-desc {
          grid-column: 1 / -1;
        }
      }
    }

    /* 待办事项样式 */
    .todo-list {
      flex: 1;
      min-height: 0;
      background-color: transparent;
      background-image: url('/resource/img/alarm/con-grid_0.png');
      background-size: 100% 100%;
      background-position: center;
      background-repeat: no-repeat;
      border-radius: 0;
      padding: 0;
      margin: 0;
      box-shadow: none;

      .panel-title {
        background: linear-gradient(180deg, rgba(82, 143, 204, 0.25) 0%, rgba(96, 142, 191, 0) 100%);
        padding: 8px 16px;
        border-radius: 0;
        margin: 0;
        border-bottom: none;
      }

      .todo-content {
        height: calc(100% - 32px);
        display: flex;
        flex-direction: column;
        padding: 10px;
      }

      .todo-tabs {
        display: flex;
        gap: 16px;
        margin-bottom: 16px;
      }

      .tab-item {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 8px 16px;
        background-color: #1f2937;
        border-radius: 4px;
        cursor: pointer;
        transition: all 0.3s ease;

        &.active {
          background-color: #2d3748;
        }
      }

      .tab-icon {
        width: 20px;
        height: 20px;
        border-radius: 50%;

        &.cold {
          background-color: #1890ff;
        }

        &.hot {
          background-color: #ff4d4f;
        }
      }

      .tab-title {
        font-size: 14px;
        color: #fff;
      }

      .tab-count {
        font-size: 12px;
        color: #86909c;
      }

      .todo-table {
        flex: 1;
        overflow: hidden;

        table {
          width: 100%;
          border-collapse: separate;
          border-spacing: 0 4px;
        }

        th,
        td {
          padding: 6px 8px;
          text-align: center;
          font-size: 12px;
        }

        th:nth-child(3),
        td:nth-child(3) {
          text-align: left;
        }

        th {
          color: #86909c;
          border-bottom: 1px solid #2d3748;
          position: sticky;
          top: 0;
        }

        td {
          color: #fff;
          border: none;
        }

        tbody tr {
          height: 20px;
          line-height: 20px;
        }

        tbody tr:nth-child(odd) {
          background-color: #1d2844;
        }

        tbody tr:nth-child(even) {
          background-color: #12192d;
        }
      }
    }

    /* 报警分析样式 */
    .alarm-analysis {
      flex: 1;
      min-height: 0;
      background-color: transparent;
      background-image: url('/resource/img/alarm/con-grid_2.png');
      background-size: 100% 100%;
      background-position: center;
      background-repeat: no-repeat;
      border-radius: 0;
      padding: 0;
      margin: 0;
      box-shadow: none;

      .panel-title {
        background: linear-gradient(180deg, rgba(82, 143, 204, 0.25) 0%, rgba(96, 142, 191, 0) 100%);
        padding: 8px 16px;
        border-radius: 0;
        margin: 0;
        border-bottom: none;

        &::before {
          display: none;
        }
      }

      .analysis-content {
        display: flex;
        gap: 2px;
        height: calc(100% - 32px);

        .split-line-container {
          display: flex;
          flex-direction: column;
          justify-content: flex-start;
          height: 100%;
          margin-bottom: 20px;

          img {
            height: calc(100% - 45px);
            object-fit: cover;
            object-position: top;
          }
        }
      }

      /* 左侧：四个环图 */
      .analysis-left {
        width: 100%;
        height: 100%;
        display: flex;
        flex-direction: column;
        gap: 10%;
        align-items: center;
        justify-content: center;
        padding: 5% 0 10% 0;
        background-image: url('/resource/img/alarm/dot-bg.png');
        background-size: auto;
        background-position: center;
        background-repeat: repeat;
        background-color: #2c43622b;
        position: relative;

        .pie-item {
          display: flex;
          flex-direction: row;
          align-items: center;
          gap: 20px;
          width: 100%;
          justify-content: center;
          flex: 1;
          min-height: 0;

          .pie-chart {
            ::v-deep(.ant-progress-text) {
              font-size: 12px;
              color: #fff;
            }
          }

          .pie-info {
            display: flex;
            flex-direction: column;
            gap: 2px;
            justify-content: center;
            min-width: 60px;
          }
          .pie-label {
            font-size: 12px;
            color: #fff;
          }

          .pie-value {
            font-size: 16px;
            font-weight: bold;
            color: #fff;
            .unit {
              font-size: 10px;
              color: #5eff5a;
              font-weight: normal;
            }
          }
        }
      }

      /* 中间：同比环比和报警类型 */
      .analysis-middle {
        flex: 1;
        display: flex;
        flex-direction: column;
        gap: 0px;
      }

      /* 同比环比 */
      .compare-section {
        flex: 0 0 44%;
        height: 44%;

        .section-title {
          font-size: 14px;
          font-weight: bold;
          color: #fff;
          margin-bottom: 0px;
          display: flex;
          align-items: center;
          gap: 8px;
          margin-left: 10px;

          &::before {
            content: '';
            display: inline-block;
            width: 12px;
            height: 12px;
            background-image: url('/resource/img/alarm/tit-prefix_2.png');
            background-size: contain;
            background-repeat: no-repeat;
            background-position: center;
          }
        }

        .compare-items {
          display: flex;
          justify-content: space-between;
          gap: 0px;
          height: calc(100% - 22px);

          .compare-item {
            flex: 1;
            background-color: transparent;
            padding: 10px;
            border-radius: 0;
            display: flex;
            flex-direction: column;
            justify-content: center;
            position: relative;
            overflow: hidden;

            .item-content {
              display: flex;
              flex-direction: column;
              align-items: center;
              position: relative;
              z-index: 1;
              padding: 10px 5px;
              background-color: #3b4a6352;
              background-image: url('/resource/img/alarm/border_1.png');
              background-size: 100% 100%;
              background-position: center;
              background-repeat: no-repeat;

              .item-icon {
                width: 60px;
                height: 60px;
                display: flex;
                align-items: center;
                justify-content: center;
                margin-bottom: 10px;

                img {
                  width: 60px;
                  height: 60px;
                  object-fit: contain;
                }
              }

              .item-text {
                width: 100%;
                display: flex;
                flex-direction: column;
                gap: 5px;

                .text-row {
                  display: flex;
                  align-items: baseline;
                  width: 100%;

                  .text-label {
                    font-size: 14px;
                    color: #809aab;
                    text-align: right;
                    flex: 1;
                  }

                  .text-value {
                    font-size: 16px;
                    margin-left: 15px;
                    color: #54beff;
                    text-align: left;
                    flex: 1;

                    &.up {
                      color: #ff4d4f;
                    }

                    &.down {
                      color: #52c41a;
                    }

                    .unit {
                      font-size: 11px;
                      font-weight: normal;
                    }

                    img {
                      width: 8px;
                      height: 12px;
                      margin-left: 5px;
                      vertical-align: middle;
                    }
                  }
                }
              }
            }
          }
        }
      }

      /* 报警类型 */
      .alarm-type-section {
        flex: 1;

        .section-title {
          font-size: 14px;
          font-weight: bold;
          color: #ffffff;
          margin-bottom: 0px;
          display: flex;
          align-items: center;
          gap: 8px;
          margin-left: 10px;

          &::before {
            content: '';
            display: inline-block;
            width: 12px;
            height: 12px;
            background-image: url('/resource/img/alarm/tit-prefix_2.png');
            background-size: contain;
            background-repeat: no-repeat;
            background-position: center;
          }
        }

        .alarm-type-content {
          display: flex;
          gap: 20px;
          height: calc(100% - 76px);
          align-items: center;
          margin: 10px 10px 0px 10px;
          background-color: #3b4a6352;
          background-image: url('/resource/img/alarm/border_2.png');
          background-size: 100% 100%;
          background-position: center;
          background-repeat: no-repeat;

          .pie-chart-large {
            width: 40%;
            text-align: center;
            justify-content: center;
            align-items: center;
            display: flex;
            background-image: url('/resource/img/alarm/pie_grid.png');
            background-size: 144px 144px;
            background-position: center;
            background-repeat: no-repeat;
          }

          .alarm-type-list {
            flex: 1;
            display: flex;
            flex-direction: column;
            gap: 5px;
            padding: 0 10px;

            .type-item {
              display: flex;
              align-items: center;
              gap: 10px;

              .type-dot {
                width: 12px;
                height: 12px;
                border-radius: 50%;

                &.type-0 {
                  background-color: #52c41a;
                }

                &.type-1 {
                  background-color: #1890ff;
                }

                &.type-2 {
                  background-color: #f7ba2a;
                }

                &.type-3 {
                  background-color: #9254de;
                }

                &.type-4 {
                  background-color: #ff4d4f;
                }
              }

              .type-name {
                width: 100px;
                font-size: 14px;
                color: #fff;
              }

              .type-count {
                width: 100px;
                font-size: 16px;
                margin-right: 15px;
                font-weight: normal;
              }

              .type-percent {
                font-size: 16px;
                font-weight: normal;
              }

              .type-count .unit {
                font-size: 12px;
                font-weight: normal;
              }

              &.type-0 .type-count,
              &.type-0 .type-percent {
                color: #52c41a;
              }

              &.type-1 .type-count,
              &.type-1 .type-percent {
                color: #1890ff;
              }

              &.type-2 .type-count,
              &.type-2 .type-percent {
                color: #f7ba2a;
              }

              &.type-3 .type-count,
              &.type-3 .type-percent {
                color: #9254de;
              }

              &.type-4 .type-count,
              &.type-4 .type-percent {
                color: #ff4d4f;
              }
            }
          }
        }
      }

      /* 右侧：设备异常排名 */
      .analysis-right {
        width: 25%;
        display: flex;
        flex-direction: column;
        background-image: url('/resource/img/alarm/top5_bg.png');
        background-size: contain;
        background-position: left center;
        background-repeat: no-repeat;
        padding-left: 20px;
        margin-bottom: 30px;
        margin-left: 20px;
        margin-top: -6px;
        height: 418px;
      }

      .analysis-right .panel-title {
        font-size: 14px;
        margin-bottom: 12px;
        padding: 0px 10px 0px 5px;
        background: transparent;
      }

      .rank-content {
        flex: 1;
        display: flex;
        flex-direction: column;
        gap: 35px;
        margin-top: 28px;
        padding: 0px 20px 0px 0px;

        .rank-item {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 6px 12px;
          background-color: #0d1a39;
          border: 1px solid #2e5a7d;
          border-radius: 3px;

          .rank-name {
            font-size: 14px;
            color: #fff;
          }

          .rank-value {
            font-size: 18px;
            font-weight: bold;

            .unit {
              font-size: 12px;
              font-weight: normal;
              margin-left: 3px;
            }
          }

          &.rank-item-0 .rank-value {
            color: #ff386b;
          }

          &.rank-item-1 .rank-value {
            color: #ed7230;
          }

          &.rank-item-2 .rank-value,
          &.rank-item-3 .rank-value,
          &.rank-item-4 .rank-value {
            color: #54beff;
          }
        }
      }
    }

    /* 报警信息样式 */
    .alarm-info {
      flex: 1;
      min-height: 0;
      overflow: hidden;
      background-color: transparent;
      background-image: url('/resource/img/alarm/con-grid_1.png');
      background-size: 100% 100%;
      background-position: center;
      background-repeat: no-repeat;
      border-radius: 0;
      padding: 0;
      margin: 0;
      box-shadow: none;

      .panel-title {
        background: linear-gradient(180deg, rgba(82, 143, 204, 0.25) 0%, rgba(96, 142, 191, 0) 100%);
        padding: 8px 16px;
        border-radius: 0;
        margin: 0;
        border-bottom: none;
      }

      .info-content {
        height: calc(100% - 32px);
        overflow-y: auto;
        padding: 10px;

        .info-table {
          width: 100%;
          border-collapse: separate;
          border-spacing: 0 4px;

          th,
          td {
            padding: 6px 8px;
            text-align: center;
            font-size: 12px;
          }

          th:nth-child(7),
          td:nth-child(7) {
            text-align: left;
          }

          th {
            color: #86909c;
            border-bottom: 1px solid #2d3748;
            position: sticky;
            top: 0;
            background-color: transparent;
          }

          td {
            color: #fff;
            border: none;
          }

          tbody tr {
            height: 20px;
            line-height: 20px;
          }

          tbody tr:nth-child(odd) {
            background-color: #1d2844;
          }

          tbody tr:nth-child(even) {
            background-color: #12192d;
          }

          .status-handled {
            color: #52c41a;
          }

          .status-processing {
            color: #f7ba2a;
          }

          [class^='level-'] {
            color: #fff;
            background-image: url('/resource/img/alarm/level_bg.png');
            background-size: cover;
            background-position: center;
            background-repeat: no-repeat;
            padding: 0px 7px;
            border-radius: 4px;
            width: 20px;
            height: 20px;
            display: inline-block;
            text-align: center;
            line-height: 20px;
          }
        }
      }
    }
  }
</style>
