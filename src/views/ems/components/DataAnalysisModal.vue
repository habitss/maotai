<template>
  <a-modal v-model:visible="visible" :title="title" :bodyStyle="{ padding: '10px' }" width="80%" :footer="null" @cancel="handleCancel">
    <!-- 点位信息 -->
    <div class="tag-info">
      <div class="tag-group">
        <span class="label">{{ currentData.tagKind }}:</span>
        <span class="value">[{{ currentData.tagId }}]{{ currentData.tagName }}</span>
        <span class="label">用量:</span>
        <span class="value">{{ currentData.usage }}</span>
      </div>
      <div class="tag-group">
        <span class="label">公式描述:</span>
        <span class="value">{{ currentData.formulaDesc }}</span>
      </div>
      <div class="tag-group">
        <span class="label">原公式:</span>
        <span class="value">{{ currentData.formulaOrigin }}</span>
      </div>
      <div class="tag-group">
        <span class="label">解析后:</span>
        <span class="value">{{ currentData.formulaAnalysis }}</span>
      </div>
      <div>
        <span class="label">替换值后:</span>
        <span class="value">{{ currentData.formulaUsage }}</span>
      </div>
    </div>

    <!-- 基础点位列表 -->
    <div class="basic-points" v-for="point in basicPoints" :key="point.tagId">
      <div class="basic-point-container">
        <!-- 基础点位头部 -->
        <div class="point-header flex-row flex-vcenter">
          <div class="point-type">
            <span>{{ point.tagKind }}:</span>
            <span style="color: #1890ff; padding-left: 5px">[{{ point.tagKindChar }}:{{ point.tagId }}]</span>
            {{ point.tagName }}
          </div>
          <div class="day-value">用量: {{ point.usage }}</div>
          <div>
            <a-button type="link" @click="toggleHourData(point)"> {{ point.showHourData ? '隐藏' : '查看' }}小时数据 </a-button>
          </div>
        </div>

        <!-- 小时数据 -->
        <div class="hour-data" v-if="point.showHourData">
          <HourDataList
            :tagUsageObj="{
              tagId: point.tagId,
              tagName: point.tagName,
              tagKindId: point.tagKindId,
              collectPeriod: point.collectPeriod,
              stime: point.stime + ':00',
              etime: point.etime + ':00',
            }"
            :key="point.tagId + point.stime"
            @show-minute-chart="handleShowMinuteChart"
          />
        </div>
      </div>
    </div>

    <!-- 5分钟曲线弹窗 -->
    <a-modal v-model:visible="minuteChartVisible" :title="`【${selectedTagId}-${selectedTagName}】分钟数据曲线`" width="70%" :footer="null">
      <MinuteChart v-if="minuteChartVisible" :hourData="selectedHourData" :tagId="selectedTagId" :tagName="selectedTagName" />
    </a-modal>
  </a-modal>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import HourDataList from './HourDataList.vue';
  import MinuteChart from './MinuteChart.vue';
  import { GrainTypeEnum } from '@/enums/EmsEnum';

  interface TagData {
    tagId: string;
    tagName: string;
    tagKindId: number;
    collectPeriod: number;
    tagKindChar: string;
    tagKind: string;
    grainType: number;
    usage: string;
    baseVal: string;
    shift: string;
    stime: string;
    etime: string;
    formulaOrigin: string;
    formulaDesc: string;
    formulaAnalysis: string;
    formulaUsage: string;
    formulaTagUsageList: TagData[];
  }

  interface BasicPoint {
    tagId: string;
    tagName: string;
    tagKindId: number;
    tagKindChar: string;
    collectPeriod: number;
    tagKind: string;
    usage: string;
    baseVal: string;
    shift: string;
    stime: string;
    etime: string;
    showHourData: boolean;
  }

  const visible = ref(false);
  const title = ref('点位数据分析');
  const minuteChartVisible = ref(false);
  const currentData = ref<TagData>({
    tagId: '',
    tagName: '',
    tagKindId: 0,
    collectPeriod: 0,
    tagKindChar: '',
    tagKind: '',
    grainType: 0,
    usage: '',
    baseVal: '',
    shift: '',
    stime: '',
    etime: '',
    formulaOrigin: '',
    formulaDesc: '',
    formulaAnalysis: '',
    formulaUsage: '',
    formulaTagUsageList: [],
  });
  const selectedHourData = ref<string | null>(null);
  const selectedTagId = ref<string | null>(null);
  const selectedTagName = ref<string | null>(null);
  const basicPoints = ref<BasicPoint[]>([]);

  const openModal = (data: TagData) => {
    currentData.value = data;
    visible.value = true;
    title.value = '点位' + (data.grainType == GrainTypeEnum.SHIFT ? '班' : '天') + '数据分析 [' + data.stime + ' ~ ' + data.etime + ']';
    minuteChartVisible.value = false;
    basicPoints.value = currentData.value.formulaTagUsageList.map((item) => ({ ...item, showHourData: false }));
  };

  // 切换小时数据显示
  const toggleHourData = (point: BasicPoint) => {
    const index = basicPoints.value.findIndex((p) => p.tagId === point.tagId);
    if (index >= 0) {
      basicPoints.value[index].showHourData = !basicPoints.value[index].showHourData;
    }
  };

  // 显示5分钟图表
  const handleShowMinuteChart = (hourData: string, tagId: string, tagName: string) => {
    selectedHourData.value = hourData;
    selectedTagId.value = tagId;
    selectedTagName.value = tagName;
    minuteChartVisible.value = true;
  };

  const handleCancel = () => {
    visible.value = false;
  };

  defineExpose({
    openModal,
  });
</script>

<style scoped>
  .tag-info {
    margin: 5px 10px;
    border-radius: 4px;
    border: 1px solid #e1e1e1;
    display: flex;
    flex-wrap: wrap;
    align-items: center;
  }
  .tag-group {
    display: block;
    align-items: center;
    white-space: nowrap;
    width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  .label {
    margin-right: 4px;
    font-weight: bold;
    color: #333;
    padding: 2px 5px;
    display: inline-block;
    min-width: 70px;
    text-align: right;
  }
  .value {
    overflow: hidden;
    text-overflow: ellipsis;
    padding: 2px 5px;
  }
  .middle-point {
    margin-bottom: 20px;
  }
  .basic-points {
    padding: 5px 10px;
  }
  .point-header {
    margin-bottom: 0px;
  }
  .point-type {
    font-weight: bold;
    width: 500px;
  }
  .day-value {
    color: #1890ff;
    width: 200px;
  }
  .hour-data {
    margin-top: 1px;
  }
</style>
