<template>
  <a-spin :spinning="pageLoading">
    <div class="kpiStruct-pg">
      <splitpanes class="default-theme" @splitter-dblclick="handleSplitterDblclick">
        <!-- 左边固定宽度区域 -->
        <pane :size="leftPanelSize" :resizable="false">
          <div class="kpiTree-hd">
            <j-dict-select-tag
              type="radioButton"
              :showChooseOption="false"
              v-model:value="queryParam.energyType"
              dictCode="mes3_mdm_dict where group_code = 'EMS_TagType',item_key,item_val"
              @change="handleEnergyTypeChange"
            />
            <a-date-picker
              v-if="treeData.length > 0"
              v-model:value="pickerDayValue"
              format="YYYY-MM-DD"
              style="width: 130px; margin-left: 95px"
              @change="handlePickerDayChange"
              :allowClear="false"
            />
          </div>
          <div class="kpi-tree">
            <div class="tree-head" v-if="treeData.length > 0">
              <div class="tree-head-title">KPI/PI</div>
              <div class="tree-head-list">
                <span class="span-lbl">YTD</span>
                <span class="span-lbl">MTD</span>
                <span class="span-lbl">Daily</span>
              </div>
            </div>
            <a-tree
              ref="treeRef"
              show-line
              :selected-keys="selectedKeys"
              :tree-data="treeData"
              :expanded-keys="expandedKeys"
              :field-names="{ key: 'id', title: 'title', children: 'children' }"
              @expand="setExpandedKeys"
            >
              <template #title="{ title, data }">
                <div class="custom-tree-node">
                  <span class="tree-node-list">
                    <span @dblclick="onNodeDblClick(data)">{{ title || data.name }}</span>
                  </span>
                  <span class="tree-node-line"></span>
                  <span class="tree-data-list">
                    <template v-if="!!data.kpiDataList">
                      <a-tooltip placement="right" color="#fff" v-for="item in data.kpiDataList" :key="item.kpiTagId">
                        <template #title>
                          <div style="color: #000; font-size: 13px">
                            <div><em style="color: #ff4d4f; text-shadow: 0 1px 2px rgba(0, 0, 0, 0.25)">●</em> 红上限: {{ item.rlover }}</div>
                            <div><em style="color: #f4e21a; text-shadow: 0 1px 2px rgba(0, 0, 0, 0.25)">●</em> 黄上限: {{ item.ylover }}</div>
                            <div><em style="color: #52c41a; text-shadow: 0 1px 2px rgba(0, 0, 0, 0.25)">●</em> 标准值: {{ item.standard }}</div>
                            <div><em style="color: #f4e21a; text-shadow: 0 1px 2px rgba(0, 0, 0, 0.25)">●</em> 黄下限: {{ item.ylbelow }}</div>
                            <div><em style="color: #ff4d4f; text-shadow: 0 1px 2px rgba(0, 0, 0, 0.25)">●</em> 红下限: {{ item.rlbelow }}</div>
                          </div>
                        </template>
                        <span :class="acUsageClass(item)" @click="openDayChartModal({ tagId: data.tagId, kpiTagName: data.kpiTagName, ...item })">
                          {{ item.acUsage || 0 }}
                        </span>
                      </a-tooltip>
                    </template>
                    <template v-else>
                      <span class="link-gray em-lbl" style="width: 350px">未配置点位</span>
                    </template>
                  </span>
                </div>
              </template>
              <!-- 突出显示展示折叠的图标 -->
              <template #switcherIcon="{ dataRef, defaultIcon }">
                <component
                  :is="defaultIcon"
                  v-if="!dataRef.isLeaf"
                  style="color: red; font-size: 18px; filter: drop-shadow(0 2px 2px rgba(0, 0, 0, 0.3))"
                />
              </template>
            </a-tree>
          </div>
        </pane>

        <!-- 拖动分隔线区域（右边区域可变）-->
        <pane :size="100 - leftPanelSize">
          <splitpanes horizontal @splitter-dblclick="handleRightSplitterDblclick">
            <pane :size="100 - rightBottomPanelSize">
              <div class="kpiTree-hd flex-row flex-vcenter" style="font-weight: bold; color: rgba(51, 51, 51, 0.88)">
                <div>{{ rightPanleTitle }}</div>
              </div>
              <div class="right-pane">
                <div class="grid">
                  <div v-for="(item, inx) in kpiDataCardSource" :key="inx" class="square-box">
                    <KpiDataCard :header="item.headerData" :data-items="item.dataItems" @open-chart="openDayChartModal" />
                  </div>
                </div>
              </div>
            </pane>
            <pane v-if="collectTagList && collectTagList.length>0" :size="rightBottomPanelSize">
              <div class="right-pane-bottmo">
                <div v-for="(item, inx) in collectTagList" :key="inx" class="line-chart-box">
                  <CollectTagChart :hourData="dayjs().startOf('hour').format('YYYY-MM-DD HH:mm')" :tagId="item.tagId" :tagName="item.kpiTagName" />
                </div>                
              </div>
            </pane>
          </splitpanes>
        </pane>
      </splitpanes>

      <!-- KPI结构弹窗 -->
      <a-modal v-model:open="digKpiStructShow" :title="digTitle" :footer="null" :body-style="{ padding: '20px' }" @cancel="closeDialog">
        <a-form ref="editFormRef" :model="modelKpiStruct" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
          <a-form-item
            label="名称"
            name="name"
            :rules="[
              { required: true, message: '名称不能为空！' },
              { max: 20, message: '不得超过20字符' },
            ]"
          >
            <a-input v-model:value="modelKpiStruct.name" placeholder="请输入名称" />
          </a-form-item>
        </a-form>
        <div class="flex-row flex-hcenter" style="margin-top: 30px">
          <a-button type="primary">保存</a-button>
        </div>
      </a-modal>

      <!-- 天用量柱状图弹窗 -->
      <date-bar-chart-modal
        v-model="tagBarChartVisible"
        :tag-line-title="barChartTitle"
        :tag-line-sub-title="barChartSubTitle"
        :picker-type="barChartPickerType"
        :initial-date-range="[barChartStartDate, barChartEndDate]"
        :y-line-data="barChartYData"
        :x-line-data="barChartXData"
        @close="handleChartClose"
        @search="fetchChartData"
        @handler-line-click="handleChartClick"
      />
    </div>
  </a-spin>
</template>

<script lang="ts" setup name="ems-KpiData">
  import { ref, reactive, onMounted, nextTick } from 'vue';
  import type { FormInstance } from 'ant-design-vue';
  import { Tree } from 'ant-design-vue';
  import { Splitpanes, Pane } from 'splitpanes';
  import 'splitpanes/dist/splitpanes.css';
  import dayjs from 'dayjs';
  import { getKpiTreeDataList, getKpiFilterDataList, getOrsDayUsageList, getOrsMtdUsageList, getOrsYtdUsageList, getCollectTagByStructId } from './KpiData.api';
  import CollectTagChart from './components/CollectTagChart.vue';
  import { TreeDataNode } from './KpiStructSet.data';
  import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';
  import KpiDataCard from './components/KpiDataCard.vue';
  import DateBarChartModal from '../components/DateBarChartModal.vue';
  import { judgeValueColor } from '../EmsUtils';
  import { useMessage } from '/@/hooks/web/useMessage';
  const { createMessage } = useMessage();

  // 状态管理
  const pageLoading = ref(false);
  const treeRef = ref<InstanceType<typeof Tree>>();
  const digKpiStructShow = ref(false);
  const digTitle = ref('新增KPI结构');
  const expandedKeys = ref<number[]>([]);
  const editFormRef = ref<FormInstance>();

  const queryParam = reactive({
    energyType: 'Electric',
    queryDate: '',
  });

  const modelKpiStruct = reactive<any>({});
  const leftPanelSize = ref(60);
  const rightBottomPanelSize = ref(40);
  const pickerDayValue = ref<dayjs.Dayjs | null>(dayjs());

  // 下拉数据
  const treeData = ref<TreeDataNode[]>([]);
  const selectedKeys = ref<number[]>([]);

  // KPI卡片数据源
  const kpiDataCardSource = reactive([
    {
      headerData: { dailyText: '', title: '', date: '' },
      dataItems: [],
    },
  ]);

  const rightPanleTitle = ref('工厂级【各能源介质】 KPI/PI 用量');

  //KPI结构绑定的采集点位
  const collectTagList = ref<any>([]);

  // 初始化
  onMounted(() => {
    //查询左侧树结构及主KPI点位数据
    queryKpiTreeDataList();
    //默认进入显示所有能源一级KPI节点所有点位数据
    queryKpiDataListByFilter(1, [], true, 999, dayjs());
  });

  const handlePickerDayChange = (date: dayjs.Dayjs | null) => {
    queryParam.queryDate = date ? date.format('YYYY-MM-DD') : '';
    queryKpiTreeDataList();
    queryKpiDataListByFilter(1, [], true, 999, date || dayjs());
  };

  // 工具函数
  const list2Tree = (list: TreeDataNode[], parentId: number): TreeDataNode[] => {
    return list
      .filter((item) => item.parentId === parentId)
      .map((item) => ({
        ...item,
        children: list2Tree(list, item.id),
      }));
  };

  const handleEnergyTypeChange = (value: string) => {
    queryParam.energyType = value;
    queryKpiTreeDataList();
  };

  //双击拖动图标
  const handleSplitterDblclick = () => {
    if (leftPanelSize.value === 1) {
      leftPanelSize.value = 60;
    } else {
      leftPanelSize.value = 1;
    }
  };

  const handleRightSplitterDblclick = () =>{
    if (rightBottomPanelSize.value === 99) {
      rightBottomPanelSize.value = 40;
    } else {
      rightBottomPanelSize.value = 99;
    }
  }

  //查询左侧树结构及数据
  const queryKpiTreeDataList = async () => {
    pageLoading.value = true;

    try {
      const data = await getKpiTreeDataList(queryParam);
      if (data.length === 0) {
        treeData.value = [];
        createMessage.warning('此能源类型下还未配置KPI结构');
        return;
      }

      // 处理点位排序
      data.forEach((elem) => {
        elem.title = elem.kpiTagName;
        if (elem.kpiTagList && elem.kpiTagList.length > 1) {
          elem.kpiTagList.sort((a, b) => b.primaryTag - a.primaryTag);
        }
      });

      // 设置展开节点
      if (data.length > 0) {
        expandedKeys.value = data.filter((item) => item.level <= 4).map((item) => item.id);
      }

      treeData.value = list2Tree(data, 0);
    } catch (e) {
      console.error('获取树结构失败:', e);
      createMessage.error('获取树结构失败');
    } finally {
      pageLoading.value = false;
    }
  };

  //查询指定条件右侧数据
  const queryKpiDataListByFilter = async (level: number, idList: number[], allEnergyType: boolean, primaryTag: number, queryDate: dayjs.Dayjs) => {
    pageLoading.value = true;
    const paramObj: any = { queryDate: queryParam.queryDate };
    if (!allEnergyType) {
      paramObj.energyType = queryParam.energyType;
    }
    if (idList && idList.length > 0) {
      paramObj.idList = idList;
    }
    if (level > 0) {
      paramObj.level = level;
    }
    if (primaryTag < 2) {
      paramObj.primaryTag = primaryTag;
    }
    paramObj.queryDate = queryDate.format('YYYY-MM-DD');

    try {
      const data = await getKpiFilterDataList(paramObj);
      if (data.length === 0) {
        return;
      }
      //循环构建显示组件
      const kpiDataCardList: any[] = [];
      const vmoldList: any[] = ['YTD', 'MTD', 'Daily'];
      data.forEach((elem) => {
        vmoldList.forEach((vmold) => {
          let formatDate;
          if (vmold === 'YTD') {
            formatDate = queryDate.format('YYYY');
          } else if (vmold === 'MTD') {
            formatDate = queryDate.format('YYYY-MM');
          } else {
            formatDate = paramObj.queryDate;
          }
          const headerData = {
            dailyText: vmold,
            title: elem.kpiTagName,
            date: formatDate,
          };
          const dataItems: any[] = [];
          elem.kpiDataList
            .filter((item) => item.vmold === vmold)
            .forEach((item) => {
              const threshold = { vmold: item.vmold, rlover: item.rlover, rlbelow: item.rlbelow, ylover: item.ylover, ylbelow: item.ylbelow };
              if (item.buUsage) {
                dataItems.push({
                  ...threshold,
                  label: item.primaryTag == 1 ? 'BU' : '用量',
                  value: item.buUsage,
                  kpiTagName: item.kpiTagName,
                  tagId: item.tagId,
                });
              } else {
                dataItems.push({
                  ...threshold,
                  label: item.primaryTag == 1 ? 'BU' : '用量',
                  value: 'N/A',
                  kpiTagName: item.kpiTagName,
                  tagId: item.tagId,
                });
              }
              if (item.acUsage) {
                dataItems.push({
                  ...threshold,
                  label: item.primaryTag == 1 ? 'AC' : '用量',
                  value: item.acUsage,
                  kpiTagName: item.kpiTagName,
                  tagId: item.tagId,
                });
              } else {
                dataItems.push({
                  ...threshold,
                  label: item.primaryTag == 1 ? 'AC' : '用量',
                  value: 'N/A',
                  kpiTagName: item.kpiTagName,
                  tagId: item.tagId,
                });
              }
            });
          kpiDataCardList.push({
            headerData: headerData,
            dataItems: dataItems,
          });
        });
      });
      kpiDataCardSource.splice(0, kpiDataCardSource.length);
      kpiDataCardSource.push(...kpiDataCardList);
    } catch (e) {
      console.error('查询指定条件右侧数据失败:', e);
    } finally {
      pageLoading.value = false;
    }
  };

  //计算 AC 值 变色
  const acUsageClass = (item: any) => {
    if (!item.acUsage) {
      return 'link-null em-lbl';
    }
    if ((item.rlover && item.acUsage >= item.rlover) || (item.rlbelow && item.acUsage <= item.rlbelow)) {
      return 'link-red em-lbl';
    }
    if ((item.ylover && item.acUsage >= item.ylover) || (item.ylbelow && item.acUsage <= item.ylbelow)) {
      return 'link-yellow em-lbl';
    }
    if (item.rlover || item.rlbelow || item.ylover || item.ylbelow) {
      return 'link-green em-lbl';
    }
    return 'em-lbl';
  };

  const setExpandedKeys = (keys: number[]) => {
    expandedKeys.value = keys;
  };

  const closeDialog = () => {};

  //双击左侧树节点，在中侧区域内展示本级与子级数据
  const onNodeDblClick = (nodeData: TreeDataNode) => {
    rightPanleTitle.value = nodeData.name + '和子级【' + queryParam.energyType + '】KPI/PI 用量';
    selectedKeys.value = [nodeData.id];
    //查询本级与下级数据
    queryKpiDataListByFilter(
      0,
      [nodeData.id, ...(Array.isArray(nodeData.children) ? nodeData.children.map((item) => item.id) : [])],
      false,
      999,
      pickerDayValue.value || dayjs()
    );

    //获取节点采集点位折线图
    getCollectTagLineChart(nodeData.id);
  };

  // ------------------- 柱图弹窗 --------------------Start
  const nowTagData = reactive({
    tagId: '',
    kpiTagName: '',
    vmold: '',
    redUpperLimit: 0,
    yellowUpperLimit: 0,
    yellowLowerLimit: 0,
    redLowerLimit: 0,
  });
  const tagBarChartVisible = ref(false);
  // 图表的标题和副标题
  const barChartTitle = ref('历史用量趋势');
  const barChartSubTitle = ref('单位');
  // 日期范围 默认初始日期范围：最近7天
  const barChartPickerType = ref('date');
  const barChartStartDate = ref(dayjs().subtract(6, 'days').format('YYYY-MM-DD'));
  const barChartEndDate = ref(dayjs().format('YYYY-MM-DD'));
  // 图表数据
  const barChartYData = ref<any[]>([]);
  const barChartXData = ref<string[]>([]);

  // 打开柱图
  const openDayChartModal = (dataItem: any) => {
    console.log('openDayChartModal', dataItem);
    nowTagData.tagId = dataItem.tagId;
    nowTagData.kpiTagName = dataItem.kpiTagName;
    nowTagData.vmold = dataItem.vmold;
    nowTagData.redUpperLimit = dataItem.rlover;
    nowTagData.yellowUpperLimit = dataItem.ylover;
    nowTagData.yellowLowerLimit = dataItem.ylbelow;
    nowTagData.redLowerLimit = dataItem.rlbelow;

    if (dataItem.vmold === 'Daily') {
      barChartPickerType.value = 'date';
      barChartEndDate.value = pickerDayValue.value ? pickerDayValue.value.format('YYYY-MM-DD') : dayjs().format('YYYY-MM-DD');
      barChartStartDate.value = pickerDayValue.value
        ? pickerDayValue.value.subtract(6, 'days').format('YYYY-MM-DD')
        : dayjs().subtract(6, 'days').format('YYYY-MM-DD');
    } else if (dataItem.vmold === 'MTD') {
      barChartPickerType.value = 'month';
      barChartEndDate.value = pickerDayValue.value ? pickerDayValue.value.format('YYYY-MM') : dayjs().format('YYYY-MM');
      barChartStartDate.value = pickerDayValue.value
        ? pickerDayValue.value.subtract(6, 'months').format('YYYY-MM')
        : dayjs().subtract(6, 'months').format('YYYY-MM');
    } else if (dataItem.vmold === 'YTD') {
      barChartPickerType.value = 'year';
      barChartEndDate.value = pickerDayValue.value ? pickerDayValue.value.format('YYYY') : dayjs().format('YYYY');
      barChartStartDate.value = pickerDayValue.value
        ? pickerDayValue.value.subtract(3, 'years').format('YYYY')
        : dayjs().subtract(3, 'years').format('YYYY');
    }
    getDayChartData(nowTagData);
  };
  // 关闭柱图
  const handleChartClose = () => {};

  // 当点击搜索按钮时触发
  const fetchChartData = (dateRange: [string, string]) => {
    barChartStartDate.value = dateRange[0];
    barChartEndDate.value = dateRange[1];
    getDayChartData(nowTagData);
  };

  // 获取图表数据
  const getDayChartData = async (tagData: any) => {
    let data: any[] = [];
    if (tagData.vmold === 'Daily') {
      const paramObj = {
        sTime: barChartStartDate.value + ' 00:00:00',
        eTime: barChartEndDate.value + ' 00:00:00',
        orsdataDayUsage: {
          tagId: tagData.tagId,
        },
      };
      barChartSubTitle.value = '天';
      data = await getOrsDayUsageList(paramObj);
    } else if (tagData.vmold === 'MTD') {
      const paramObj = {
        sTime: barChartStartDate.value + '-01 00:00:00',
        eTime: barChartEndDate.value + '-01 00:00:00',
        orsdataMtdUsage: {
          tagId: tagData.tagId,
        },
      };
      barChartSubTitle.value = '月';
      data = await getOrsMtdUsageList(paramObj);
    } else if (tagData.vmold === 'YTD') {
      const paramObj = {
        sTime: barChartStartDate.value + '-01-01 00:00:00',
        eTime: barChartEndDate.value + '-01-01 00:00:00',
        orsdataYtdUsage: {
          tagId: tagData.tagId,
        },
      };
      barChartSubTitle.value = '年';
      data = await getOrsYtdUsageList(paramObj);
    } else {
      barChartSubTitle.value = '';
    }
    barChartSubTitle.value = barChartSubTitle.value + '用量历史趋势';
    barChartTitle.value = tagData.kpiTagName;
    if (data.length > 0) {
      tagBarChartVisible.value = true;
      nextTick(() => {
        const xDatas: any[] = [];
        const yDatas: any[] = [];
        const timeValList = data[0].timeValList;
        timeValList.sort((a, b) => (a.time >= b.time ? 1 : -1));
        for (const i in timeValList) {
          xDatas.push(timeValList[i].time);
          yDatas.push({
            value: timeValList[i].value,
            valueDate: timeValList[i].time,
            redUpperLimit: tagData.redUpperLimit,
            yellowUpperLimit: tagData.yellowUpperLimit,
            yellowLowerLimit: tagData.yellowLowerLimit,
            redLowerLimit: tagData.redLowerLimit,
            itemStyle: {
              color: judgeValueColor(
                timeValList[i].value,
                tagData.redUpperLimit,
                tagData.redLowerLimit,
                tagData.yellowUpperLimit,
                tagData.yellowLowerLimit
              ),
            },
          });
        }
        barChartXData.value = xDatas;
        barChartYData.value = yDatas;
      });
    } else {
      createMessage.warning('暂无数据', 2);
    }
  };

  // 图表柱状图点击事件处理
  const handleChartClick = (params: any) => {
    console.log('柱状图点击:', params);
  };
  // ------------------- 柱图弹窗 --------------------End

  //采集点位折线图
  async function getCollectTagLineChart(structId){
    collectTagList.value = [];
    const res = await getCollectTagByStructId({structId: structId});
    if(res && res.length > 0){
      collectTagList.value = [...res]
    }
  }

</script>

<style lang="less" scoped>
  @import url('/@/styles/disabled.less');
  .kpiTree-hd {
    background-color: #fff;
    padding: 0px 0px 10px 0px;
    border-bottom: 2px solid #eee;
    min-width: 1000px;
  }
  .kpiStruct-pg {
    background-color: #fff;
    margin: 10px;
    height: calc(100vh - 140px);
    .kpi-tree {
      min-width: 1000px;
      margin-top: 5px;
    }
    .custom-tree-node {
      display: flex;
      align-items: center;
      position: relative;
      padding-right: 350px; /* 预留页码位置 */
      min-height: 24px;

      .tree-node-list {
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        z-index: 1;
        padding: 0 5px;
      }
      .tree-node-line {
        flex: 1;
        height: 1px;
        margin: 0 8px;
        position: relative;
        top: 0; /* 可调整对齐 */
        border-bottom: 1px dashed #ccc;
        z-index: 0;
      }
      .tree-data-list {
        position: absolute;
        right: 0;
        white-space: nowrap;
        color: #888;
        font-variant-numeric: tabular-nums;
        background-color: white; /* 防止被线遮住 */
        padding-left: 4px;
        z-index: 1;
        top: -2px;
      }

      .em-lbl {
        font-style: normal;
        font-size: 13px;
        color: #000;
        margin-left: 10px;
        border: 1px dashed #e1e1e1;
        width: 110px;
        display: inline-block;
        border-radius: 3px;
        padding: 0 2px;
        overflow: hidden;
        text-overflow: ellipsis;
        text-align: center;
        &:hover {
          border: 1px dashed #d92622 !important;
        }
      }

      .link-green {
        color: #000;
        background-color: #52c41a;
        border-color: #52c41a;
      }

      .link-red {
        color: #000;
        background-color: #ff4d4f;
        border-color: #ff4d4f;
      }

      .link-yellow {
        color: #000;
        background-color: #f4e21a;
        border: 1px solid #f4e21a;
      }
      .link-null {
        color: transparent;
      }
      .link-gray {
        color: #d5d5d5;
      }
    }

    .ant-tree-node-content-wrapper {
      width: 100%;
    }
  }
  .tree-head {
    display: flex;
    align-items: center;
    padding-right: 80px;
    font-weight: bold;
    margin-bottom: 10px;
    position: relative;

    .tree-head-title {
      display: inline-block;
      font-weight: bold;
    }
    .tree-head-list {
      position: absolute;
      right: 0;
      white-space: nowrap;
      color: #888;
      font-variant-numeric: tabular-nums;
      background-color: white;
      padding-left: 4px;
      z-index: 1;
      .span-lbl {
        font-style: normal;
        font-size: 13px;
        color: #000;
        margin-left: 10px;
        border: 1px solid #d9d9d9;
        width: 110px;
        display: inline-block;
        border-radius: 3px;
        padding: 2px 2px;
        text-align: center;
      }
    }
  }

  ::v-deep(.splitpanes) {
    .splitpanes__splitter {
      background-color: #f2f2f2;
      box-sizing: border-box;
      position: relative;
      flex-shrink: 0;
      width: 7px;
      border-left: 1px solid #eee;
      margin-left: -1px;
    }
    .splitpanes__pane {
      background-color: #fff;
      padding: 10px;
    }
  }

  .left-pane {
    height: 100%;
    overflow-y: auto;
    padding: 16px;
    background: #f0f2f5;
    overflow-x: hidden;
  }

  .right-pane {
    height: 100%;
    padding-right: 5px;
    box-sizing: border-box;
    overflow-y: auto;
    overflow-x: hidden;
    margin-top: 5px;
    padding-bottom: 50px;
  }

  .grid {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    gap: 10px;
  }
  .square-box {
    width: 300px;
    background-color: #f5f5f5;
    display: flex;
    justify-content: center;
    align-items: flex-start;
    border-radius: 8px;
    padding: 5px;
    font-size: 18px;
  }

  .grid-chart {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(clamp(300px, 25%, 500px), 1fr));
    gap: 12px;
  }
  .square-box-chart {
    width: 100%;
    padding: 5px;
    aspect-ratio: 2;
    background-color: #f5f5f5;
    display: flex;
    justify-content: center;
    align-items: center;
    border-radius: 4px;
    font-size: 18px;
  }

  .right-pane-bottmo {
    height: 100%;
    padding-right: 5px;
    box-sizing: border-box;
    overflow-y: auto;
    overflow-x: hidden;
    margin-top: 5px;
  }
  .line-chart-box{
    width: 100%;
    height: 200px;
    padding: 5px;
    aspect-ratio: 2;
    background-color: #f5f5f5;
    display: flex;
    justify-content: center;
    align-items: center;
    border-radius: 4px;
    font-size: 18px;
    margin-top: 5px;
  }

  .tooltip-content {
    color: #000;
    font-size: 13px;
    .red-tooltip {
      background-color: #f00;
      color: #fff;
      padding: 3px 5px;
      margin-right: 5px;
    }
    .yellow-tooltip {
      background-color: #ff0;
      color: #000;
      padding: 3px 5px;
      margin-right: 5px;
    }
  }
  ::v-deep(.ant-tree .ant-tree-treenode) {
    width: 100% !important;
  }

  ::v-deep(.ant-tree .ant-tree-node-content-wrapper) {
    width: 100% !important;
    padding-right: 0px !important;
  }
  ::v-deep(.ant-tree-node-content-wrapper:hover .tree-node-list) {
    background-color: #ff000075 !important;
  }
  ::v-deep(.ant-tree-node-selected .tree-node-list) {
    background-color: #ff000075 !important;
  }
</style>
