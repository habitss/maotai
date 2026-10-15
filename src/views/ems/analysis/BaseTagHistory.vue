<template>
  <a-spin :spinning="pageLoading">
    <div class="form-container">
      <a-form layout="inline">
        <a-form-item>
          <j-dict-select-tag
            placeholder="点位类型"
            v-model:value="modelDto.tagType"
            dictCode="mes3_mdm_dict where group_code = 'EMS_TagType',item_key,item_val"
            allow-clear
            @change="dataChange"
          />
        </a-form-item>
        <a-form-item>
          <a-form-item>
            <JDeptCascader v-model:modelValue="modelDto.factoryDeptCode" :show-default="true" @change="deptChangeCascader" />
          </a-form-item>
        </a-form-item>
        <a-form-item>
          <a-select v-model:value="modelDto.selType" :max-tag-count="3" placeholder="选择曲线类型" style="width: 110px" @change="selTypeChange">
            <a-select-option v-for="item in selTypeList" :key="item.id" :value="item.id">
              {{ item.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item v-show="selTypeOldSHow">
          <a-range-picker
            v-model:value="pickDates"
            :show-time="{
              defaultValue: [dayjs('00:00:00', 'HH:mm:ss'), dayjs('00:00:00', 'HH:mm:ss')],
            }"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            :ranges="pickerRanges"
            :placeholder="['开始时间', '结束时间']"
          />
        </a-form-item>
        <a-form-item v-show="selTypeNewSHow">
          <a-select v-model:value="modelDto.selTime" :max-tag-count="3" placeholder="请选择时间类型">
            <a-select-option v-for="item in selTimeList" :key="item.id" :value="item.id">
              {{ item.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item v-if="false">
          <a-select v-model:value="groupId" allow-clear show-search placeholder="请选择分析组" @change="getDataGroup">
            <a-select-option v-for="item in groupList" :key="item.id" :value="item.id">
              {{ item.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="changDataType">搜索</a-button>
          <a-button @click="reset" style="margin-left: 8px">重置</a-button>
          <a-button v-show="yLineData.length > 0" type="primary" style="margin-left: 8px" @click="setEchartYDate">设置EChartY轴</a-button>
        </a-form-item>
        <a-form-item v-if="false">
          <a-button type="primary" @click="updateGroup">维护分析组</a-button>
        </a-form-item>
      </a-form>
    </div>

    <a-modal
      v-model:visible="echartShow"
      title="设置EchartY轴值"
      :footer="null"
      :mask-closable="false"
      width="360px"
      :bodyStyle="{ padding: '20px' }"
      @cancel="closeDialogY"
    >
      <a-form :label-col="{ span: 8 }" :wrapper-col="{ span: 16 }" sytle="padding: 20px;margin-bottom: 20px">
        <a-form-item label="最大值：">
          <a-input-number v-model:value="echartY.max" placeholder="输入最大值" style="width: 100%" />
        </a-form-item>
        <a-form-item label="最小值：">
          <a-input-number v-model:value="echartY.min" placeholder="输入最小值" style="width: 100%" />
        </a-form-item>
        <a-form-item :wrapper-col="{ offset: 8, span: 16 }">
          <a-button type="primary" @click="saveEchart">保存</a-button>
          <a-button @click="closeDialogY" style="margin-left: 10px">取消</a-button>
        </a-form-item>
      </a-form>
    </a-modal>

    <a-row :gutter="10" class="chart-container">
      <a-col :span="5" :class="{ 'folded-left': siderOpened }">
        <div class="groupCont clearfix" :class="{ 'is-active': siderOpened }">
          <div class="flex-row flex-start">
            <a-input v-show="!siderOpened" v-model:value="tagKey" allow-clear placeholder="请输入点位关键字筛选" />
            <div v-if="false" :class="{ 'is-active': siderOpened }" class="hamburger-container" @click="toggleSiderTag">
              <left-outlined />
            </div>
          </div>

          <div v-show="!siderOpened" class="group-tag-cond">
            <a-spin :spinning="tagLoading">
              <a-list :data-source="tagList" size="small" :bordered="false">
                <template #renderItem="{ item }">
                  <a-list-item
                    :class="{ selClass: modelDto.tagids.includes(String(item.tagId)) }"
                    :title="item.tagName"
                    @dblclick="dbclickHandler(item.tagId)"
                    draggable
                    @dragstart="liRowDrag(item.tagId)"
                  >
                    <div class="div-tagid">[{{ item.tagId }}] {{ item.tagName }}</div>
                  </a-list-item>
                </template>
              </a-list>
            </a-spin>
          </div>
        </div>
      </a-col>
      <a-col :span="19" :class="{ 'folded-right': siderOpened }">
        <div id="chart_2" @dragover.prevent @drop="dropHandler">
          <a-button v-if="modelDto.selType === 1 && leftBtn" class="left-btn" shape="circle" @click="leftClick">
            <left-outlined />
          </a-button>
          <a-button v-if="modelDto.selType === 1 && xLineData.length > 0" class="right-btn" shape="circle" @click="rightClick">
            <right-outlined />
          </a-button>
          <line-area-chart
            id="tagHistoryCurve"
            ref="historyChart"
            class="line-chart"
            :y-data="yLineData"
            :x-data="xLineData"
            :limit-obj="limitObj"
            :type="xType"
            :x-show="xshow"
            :y-min="yMin"
            :y-max="yMax"
            :legend-seled="legendSelect"
            @handler-line-datazoom="handlerLineDatazoom"
            @handler-line-restore="handlerLineRestore"
            @handler-legend-chg="handlerLegendChg"
            @change-yechats="changeYechats"
          />
        </div>
      </a-col>
    </a-row>

    <a-modal v-model:visible="udigGroupShow" :title="udigGroupTitle" :mask-closable="false" width="500px" @cancel="closeDialog">
      <a-form ref="editFormRef" :model="modelGroup" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item
          label="分析组名："
          name="name"
          :rules="[
            { required: true, message: '分析组名称不能为空' },
            { max: 20, message: '不得超过20字符' },
          ]"
        >
          <a-input v-model:value="modelGroup.name" allow-clear placeholder="输入分析组名称" />
        </a-form-item>
        <a-form-item label="点位集合：" name="utagids" :rules="[{ required: true, message: '点位集合不能为空' }]">
          <a-select
            v-model:value="modelDto.utagids"
            mode="multiple"
            allow-clear
            show-search
            :max-tag-count="3"
            placeholder="选择点位"
            :loading="loading"
          >
            <a-select-option v-for="item in tagListX" :key="item.tagId" :value="item.tagId">
              {{ item.tagName }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item :wrapper-col="{ offset: 6, span: 18 }">
          <a-button danger @click="delGroup">
            <template #icon><delete-outlined /></template>
            删除
          </a-button>
          <a-button type="primary" @click="modifyGroup" style="margin-left: 10px">保存</a-button>
          <a-button @click="onClkCancel" style="margin-left: 10px">取消</a-button>
        </a-form-item>
      </a-form>
    </a-modal>
  </a-spin>
</template>

<script lang="ts" name="ems-BaseTagHistory" setup>
  import { ref, reactive, onMounted, onBeforeUnmount, watch, nextTick } from 'vue';
  import { LeftOutlined, RightOutlined, DeleteOutlined } from '@ant-design/icons-vue';
  import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';
  import JDeptCascader from '../components/JDeptCascader.vue';
  import LineAreaChart from '../components/LineAreaChart.vue';
  import { saveEnergyGroup, listEnergyGroup, delEnergyGroupList, getBaseTagList, getHistoryCurveData } from './BaseTagHistory.api';
  import dayjs from 'dayjs';
  import { useRoute } from 'vue-router';
  import { useMessage } from '/@/hooks/web/useMessage';
  const { createMessage } = useMessage();
  const route = useRoute();

  // 类型定义
  interface TagItem {
    tagId: string | number;
    tagName: string;
    unit?: string;
  }

  interface EnergyGroup {
    id: string;
    name: string;
    energyDataList: TagItem[];
  }

  interface ChartData {
    data: number[];
    name: string;
    tagId: string | number;
    lineStyle?: { width: number };
  }

  // 组件状态
  const pageLoading = ref(false);
  const tagLoading = ref(false);
  const loading = ref(false);
  const echartShow = ref(false);
  const udigGroupShow = ref(false);
  const siderOpened = ref(false);
  const leftBtn = ref(false);
  const xshow = ref(true);
  const tagKey = ref('');
  const groupId = ref('');
  const rowTagid = ref<number | string>(0);
  const selTypeOldSHow = ref(false);
  const selTypeNewSHow = ref(true);
  const historyChart = ref<InstanceType<typeof LineAreaChart>>();
  const editFormRef = ref();

  // 数据列表
  const tagListX = ref<TagItem[]>([]);
  const tagList = ref<TagItem[]>([]);
  const tagMapX = ref<Record<string, string>>({});
  const groupList = ref<EnergyGroup[]>([]);
  const xLineData = ref<string[]>([]);
  const yLineData = ref<ChartData[]>([]);
  const pickDates = ref<string[]>([]);

  // 模型数据
  const modelDto = reactive<Record<string, any>>({
    factoryDeptCode: '',
    factoryCode: '',
    deptCode: '',
    lineCode: '',
    tagType: '',
    tagids: [] as string[],
    selType: 2,
    selTime: 4,
    utagids: [] as (string | number)[],
  });

  const modelGroup = reactive({
    name: '',
  });

  const echartY = reactive({
    min: 0,
    max: 0,
  });

  const limitObj = ref<{ tagId: string; limitval: number } | undefined>(undefined);
  const legendSelect = ref<Record<string, boolean> | undefined>(undefined);
  type ChartType = 'Day' | 'Hour' | 'Second' | 'Other' | '';
  const xType = ref<ChartType>('Day');
  const yMin = ref('0');
  const yMax = ref('0');
  const udigGroupTitle = ref('新增分组');
  const timer = ref<NodeJS.Timeout | null>(null);

  // 选择器选项
  const selTypeList = ref([
    { id: 1, name: '历史' },
    { id: 2, name: '实时' },
  ]);

  const selTimeList = ref([
    { id: 1, name: '前1个小时' },
    { id: 4, name: '前4个小时' },
    { id: 8, name: '前8个小时' },
  ]);

  // 时间范围快捷选项
  const pickerRanges = {
    最近一周: [dayjs().subtract(7, 'day'), dayjs()],
    最近一个月: [dayjs().subtract(1, 'month'), dayjs()],
    最近三个月: [dayjs().subtract(3, 'month'), dayjs()],
  };

  // 监听器
  watch(tagKey, (newVal) => {
    tagKeyChange(newVal);
  });

  // 生命周期
  onMounted(() => {
    /**获取分组列表
     * getEnergyGroupList();
     */
    getBaseDataList();
  });

  onBeforeUnmount(() => {
    clearInterval(timer.value as NodeJS.Timeout);
  });

  //分级部门选择事件
  function deptChangeCascader(selectDepts) {
    console.log(selectDepts);
    if (selectDepts.length > 0) {
      modelDto.factoryCode = selectDepts[0];
      if (selectDepts.length > 1) {
        modelDto.deptCode = selectDepts[1];
        if (selectDepts.length > 2) {
          modelDto.lineCode = selectDepts[2];
        } else {
          delete modelDto.lineCode;
        }
      } else {
        delete modelDto.deptCode;
        delete modelDto.lineCode;
      }
    } else {
      delete modelDto.factoryCode;
      delete modelDto.deptCode;
      delete modelDto.lineCode;
    }
    dataChange();
  }

  const dataChange = () => {
    if (!modelDto.tagType && !modelDto.factoryDeptCode && tagList.value.length === 0) {
      createMessage.warning('请先选择能源类型！', 5);
      return;
    }
    if (!modelDto.tagType && !modelDto.factoryDeptCode && tagList.value.length > 0) {
      tagListX.value = [];
      tagList.value = [];
      return;
    }
    getBaseDataList();
  };

  const getBaseDataList = async () => {
    tagLoading.value = true;
    tagListX.value = [];
    tagList.value = [];
    xLineData.value = [];
    yLineData.value = [];
    modelDto.tagids = [];

    const paraObj = {
      factoryCode: modelDto.factoryCode,
      deptCode: modelDto.deptCode,
      lineCode: modelDto.lineCode,
      tagType: modelDto.tagType,
      pageSize: 9999999,
      selectFields: 'tag_id,point_name,energy_type',
    };

    try {
      const response = await getBaseTagList(paraObj);
      tagListX.value = response.records;
      tagList.value = [...tagListX.value];
      tagListX.value.forEach((elem) => {
        tagMapX.value[elem.tagId] = elem.tagName + '(' + elem.unit + ')';
      });

      nextTick(() => {
        getBaseHistory();
        tagKey.value = '';
        tagKeyChange('');
      });
    } catch (e) {
      createMessage.error('获取基础数据失败', 5);
      console.error('获取基础数据失败:', e);
    } finally {
      tagLoading.value = false;
    }
  };

  const tagKeyChange = (tagVal: string) => {
    if (tagVal) {
      tagList.value = tagListX.value.filter((item) => (item.tagId + item.tagName).toLowerCase().includes(tagVal.toLowerCase()));
    } else {
      tagList.value = [...tagListX.value];
    }
  };

  const liRowDrag = (tagid: number | string) => {
    rowTagid.value = tagid;
  };

  const dropHandler = (event: DragEvent) => {
    event.preventDefault();
    modelDto.tagids.push(String(rowTagid.value));
    changDataType();
  };

  const dbclickHandler = (tagid: number | string) => {
    const tagidStr = String(tagid);
    const index = modelDto.tagids.indexOf(tagidStr);

    if (index > -1) {
      if (modelDto.tagids.length > 1) {
        modelDto.tagids.splice(index, 1);
      } else {
        createMessage.warning('至少需要保留一个点位', 5);
      }
    } else {
      modelDto.tagids.push(tagidStr);
    }
    changDataType();
  };

  const changDataType = () => {
    if (modelDto.selType === 1) {
      if (timer.value) {
        clearInterval(timer.value);
        timer.value = null;
      }
      showHistoryChart();
      xshow.value = true;
    } else {
      if (timer.value === null) {
        timer.value = setInterval(() => {
          console.log(dayjs().format('YYYY-MM-DD HH:mm:ss') + ' Reload Data.....');
          getNewDataList();
        }, 10000);
      }
      xshow.value = false;
      showNewChart();
    }
  };

  const getEchartData = async (stime: string, etime: string) => {
    pageLoading.value = false;
    const paraObj = {
      frequency: historyChart.value?.getChart?.()?.getWidth?.() ?? 1000,
      stime: stime,
      etime: etime,
      tagIds: unique(modelDto.tagids).join(','),
    };
    try {
      if (modelDto.selType === 1) {
        historyChart.value?.getChart()?.clear();
        historyChart.value?.getChart()?.showLoading();
      }

      const response = await getHistoryCurveData(paraObj);
      leftBtn.value = true;
      xLineData.value = [];
      yLineData.value = [];
      xType.value = '';
      console.log('response:', response);
      const dataArr = response.result;
      if (dataArr) {
        dataArr.forEach((o: any) => {
          const xDatas: string[] = [];
          const yDatas: number[] = [];
          const dataXyDatas = o.lineDatas;

          if (dataXyDatas.length > 0) {
            dataXyDatas.forEach((v: any) => {
              xDatas.push(v.time);
              yDatas.push(Number(v.value));
            });
          }

          xLineData.value = xDatas;
          yLineData.value.push({
            data: yDatas,
            name: tagMapX.value[o.tagId],
            tagId: o.tagId,
            lineStyle: { width: 1 },
          });
          xType.value = o.dataType;
        });
        if (modelDto.selType === 1) {
          historyChart.value?.getChart()?.hideLoading();
        }
      }
    } catch (e) {
      createMessage.error('获取图表数据失败', 5);
      console.error('获取图表数据失败:', e);
    } finally {
      pageLoading.value = false;
    }
  };

  const leftClick = () => {
    if (pickDates.value.length < 2) return;

    const days = dayjs(pickDates.value[1]).diff(pickDates.value[0], 'day');
    const startDate = dayjs(pickDates.value[0]).subtract(days, 'day').format('YYYY-MM-DD HH:mm:ss');
    const endDate = dayjs(pickDates.value[1]).subtract(days, 'day').format('YYYY-MM-DD HH:mm:ss');

    pickDates.value = [startDate, endDate];
    getEchartData(startDate, endDate);
  };

  const rightClick = () => {
    if (pickDates.value.length < 2) return;

    const days = dayjs(pickDates.value[1]).diff(pickDates.value[0], 'day');
    const startDate = dayjs(pickDates.value[0]).add(days, 'day').format('YYYY-MM-DD HH:mm:ss');
    const endDate = dayjs(pickDates.value[1]).add(days, 'day').format('YYYY-MM-DD HH:mm:ss');

    pickDates.value = [startDate, endDate];
    getEchartData(startDate, endDate);
  };

  const showHistoryChart = () => {
    if (groupId.value !== '') {
      modelDto.tagids = [];
      const ntagids = tagList.value.map((item) => String(item.tagId));
      modelDto.tagids = [...ntagids];
    }

    if (modelDto.tagids.length === 0) {
      createMessage.warning('请选择点位', 3);
      return;
    }

    if (pickDates.value.length === 0) {
      createMessage.warning('请选择查询时间段', 3);
      return;
    }

    getEchartData(pickDates.value[0], pickDates.value[1]);
  };

  const handlerLineDatazoom = (dateRanges: string[]) => {
    getEchartData(dateRanges[0], dateRanges[1]);
  };

  const handlerLineRestore = () => {
    if (modelDto.tagids.length === 0) {
      createMessage.warning('请选择点位', 5);
      return;
    }

    if (pickDates.value.length === 0) {
      createMessage.warning('请选择查询时间段', 5);
      return;
    }

    getEchartData(pickDates.value[0], pickDates.value[1]);
  };

  const handlerLegendChg = (param: any) => {
    legendSelect.value = param;
  };

  const unique = (arr: string[]) => {
    const res = new Map();
    return arr.filter((tagid) => !res.has(tagid) && res.set(tagid, 1));
  };

  const reset = () => {
    xLineData.value = [];
    yLineData.value = [];
    pickDates.value = [];

    Object.assign(modelDto, {
      factoryDeptCode: '',
      factoryCode: '',
      deptCode: '',
      lineCode: '',
      tagType: '',
      tagids: [],
      selTime: 4,
      utagids: [],
    });

    if (timer.value) {
      clearInterval(timer.value);
      timer.value = null;
    }

    yMin.value = '0';
    yMax.value = '0';
    echartY.min = 0;
    echartY.max = 0;
  };

  const selTypeChange = () => {
    if (modelDto.selType === 1) {
      selTypeOldSHow.value = true;
      selTypeNewSHow.value = false;
      changDataType();
    } else {
      selTypeOldSHow.value = false;
      selTypeNewSHow.value = true;
    }
  };

  const showNewChart = () => {
    if (modelDto.tagids.length === 0) {
      createMessage.warning('请选择点位', 5);
      return;
    }

    if (modelDto.selTime == undefined || modelDto.selTime == null) {
      createMessage.warning('请选择查询时间类型', 5);
      return;
    }

    getNewDataList();
  };

  const getNewDataList = () => {
    if (modelDto.tagids.length > 0) {
      const stime = new Date();
      const etime = new Date();
      stime.setTime(etime.getTime() - Number(modelDto.selTime) * 3600 * 1000);
      getEchartData(dayjs(stime).format('YYYY-MM-DD HH:mm:ss'), dayjs(etime).format('YYYY-MM-DD HH:mm:ss'));
    }
  };

  const toggleSiderTag = () => {
    siderOpened.value = !siderOpened.value;
    nextTick(() => {
      historyChart.value?.handleResize();
    });
  };

  const getDataGroup = async () => {
    if (groupId.value !== '') {
      modelDto.tagids = [];
      const groups = groupList.value.filter((o) => groupId.value === o.id);

      if (groups.length > 0) {
        const tagidList = groups[0].energyDataList.map((item) => ({
          tagId: item.tagId,
          tagName: item.tagName || '',
        }));

        tagList.value = [...tagidList];
        modelDto.tagids = tagList.value.map((item) => String(item.tagId));
        changDataType();
      }
    } else {
      getBaseDataList();
    }
  };

  const updateGroup = () => {
    if (groupId.value === '') {
      udigGroupShow.value = true;
      udigGroupTitle.value = '新增分组';
      modelDto.utagids = [];
    } else {
      udigGroupTitle.value = '修改分组';
      udigGroupShow.value = true;
      modelDto.utagids = [];

      const groups = groupList.value.filter((o) => groupId.value === o.id);
      if (groups.length > 0) {
        modelGroup.name = groups[0].name;
        modelDto.utagids = groups[0].energyDataList.map((item) => item.tagId);
      }
    }
  };

  const handResetForm = () => {
    editFormRef.value?.resetFields();
    modelGroup.name = '';
  };

  const modifyGroup = async () => {
    try {
      await editFormRef.value?.validate();

      const energyDataList = modelDto.utagids.map((tagid) => ({
        tagId: tagid,
        groupId: groupId.value,
      }));

      const paraObj = {
        id: groupId.value,
        name: modelGroup.name,
        type: 2,
        energyDataList: energyDataList,
      };

      const response = await saveEnergyGroup(paraObj, false);
      createMessage.success(response.msg || '保存成功');
      udigGroupShow.value = false;
      getEnergyGroupList();
    } catch (error) {
      console.error('保存失败:', error);
    }
  };

  const delGroup = async () => {
    if (groupId.value === '') {
      createMessage.error('请选择分组', 4);
      return;
    }

    await delEnergyGroupList(groupId.value, (response: any) => {
      createMessage.success(response.msg || '删除成功', 5);
      udigGroupShow.value = false;
      groupId.value = '';
      modelDto.tagids = [];
      getEnergyGroupList();
    });
  };

  const getEnergyGroupList = async () => {
    const paraObj = {
      energyGroup: {
        type: 2,
        userId: 0,
      },
    };

    try {
      const response = await listEnergyGroup(paraObj);
      response.data.sort((a: EnergyGroup, b: EnergyGroup) => a.name.localeCompare(b.name, 'zh'));
      groupList.value = response.data;
    } catch (e) {
      createMessage.error('获取分析组列表失败', 5);
      console.error(e);
    }
  };

  const onClkCancel = () => {
    udigGroupShow.value = false;
  };

  const closeDialog = () => {
    handResetForm();
  };

  const closeDialogY = () => {
    echartShow.value = false;
  };

  const setEchartYDate = () => {
    echartShow.value = true;
  };

  const saveEchart = () => {
    if (Number(echartY.min) >= Number(echartY.max)) {
      createMessage.warning('最大值必须大于最小值');
      return;
    }
    yMax.value = String(echartY.max);
    yMin.value = String(echartY.min);
    echartShow.value = false;
  };

  const changeYechats = (max: number, min: number) => {
    yMax.value = String(max);
    yMin.value = String(min);
  };

  const getBaseHistory = () => {
    Object.assign(limitObj, {});
    console.log(route.query, 'route.query');
    if (route.query.searchType) {
      modelDto.tagids = [];
      modelDto.selType = Number(route.query.selType);
      modelDto.selTime = Number(route.query.selTime);
      modelDto.tagids.push(String(route.query.tagId));
      tagKey.value = String(route.query.tagId);

      if (route.query.islimit === 'true') {
        Object.assign(limitObj, {
          tagId: String(route.query.tagId),
          limitval: route.query.thresholdValue,
        });
      }

      if (route.query.searchType === '2') {
        selTypeOldSHow.value = true;
        selTypeNewSHow.value = false;
        pickDates.value = [String(route.query.stime), String(route.query.etime)];
      }

      nextTick(() => {
        changDataType();
      });
    }
  };
</script>

<style scoped lang="less">
  .form-container {
    padding: 15px;
    margin: 10px;
    background-color: #fff;
  }
  .chart-container {
    width: 100%;
    background-color: #fff;
    margin: 10px !important;
  }
  .groupCont {
    padding: 5px 10px;
    margin: 10px 0px;

    background-color: #fff;
    border: 1px solid #f0f0f0;
    &.is-active {
      width: 32px;
      padding: 0;
    }
    .hamburger-container {
      color: #a0a0a0;
      font-weight: bold;
      font-size: 28px;
      float: left;
      padding: 0 0 0 10px !important;
      line-height: 30px;
      cursor: pointer;

      &.is-active {
        transform: rotate(180deg);
      }
    }

    .group-tag-cond {
      display: block;
      clear: both;
      overflow-y: auto;
      height: calc(100vh - 300px);

      :deep(.ant-list-item) {
        padding: 4px 8px;
        margin: 7px 0;
        font-size: 14px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        cursor: pointer;

        &:hover {
          background-color: rgba(255, 255, 255, 0.1);
        }
      }
    }
  }

  #chart_2 {
    position: relative;
  }

  .left-btn,
  .right-btn {
    font-size: 20px;
    border: none;
    position: absolute;
    top: 5px;
    z-index: 1;
    color: #d92622;
    line-height: 1;
    background-color: transparent;

    &:hover {
      background-color: rgba(185, 145, 145, 0.1) !important;
    }
  }

  .left-btn {
    left: 33%;
  }

  .right-btn {
    right: 44%;
  }

  .line-chart {
    height: calc(100vh - 258px);
    border: 1px solid #f0f0f0;
    overflow: hidden;
    margin-top: 10px;
    background-color: rgba(255, 255, 255, 0.1);
  }

  .div-tagid {
    display: inline-block;
    width: 70px;
  }

  .selClass {
    color: #d92622;
    font-size: 15px !important;
    font-weight: bold;
    cursor: default !important;
  }

  .folded-left {
    width: auto;
  }

  .folded-right {
    width: 95%;
  }

  .clearfix:after {
    content: '';
    display: block;
    clear: both;
  }
</style>
