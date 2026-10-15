<template>
  <div class="kpidata-card">
    <!-- 头部区域 - 两行布局 -->
    <div class="card-header">
      <div class="header-line">
        <span class="daily-label">{{ header.dailyText }}</span>
        <span class="date">{{ header.date }}</span>
      </div>
      <div class="header-title">{{ header.title }}</div>
    </div>

    <!-- 数据区域 - 左右结构 -->
    <div class="data-grid">
      <div
        v-for="(item, index) in computedDataItems"
        :key="index"
        class="data-item"
        @click="handleItemClick(item)"
        :class="{ 'blue-bg': index % 2 === 0 }"
        :style="{ backgroundColor: item.backgroundColor }"
      >
        <div class="item-label">{{ item.label }}</div>
        <a-tooltip placement="right" color="#fff" v-if="index % 2 === 1">
          <template #title>
            <div style="color: #000; font-size: 13px">
              <div><em style="color: #ff4d4f; text-shadow: 0 1px 2px rgba(0, 0, 0, 0.25)">●</em> 红上限: {{ item.rlover }}</div>
              <div><em style="color: #f4e21a; text-shadow: 0 1px 2px rgba(0, 0, 0, 0.25)">●</em> 黄上限: {{ item.ylover }}</div>
              <div><em style="color: #52c41a; text-shadow: 0 1px 2px rgba(0, 0, 0, 0.25)">●</em> 标准值: {{ item.standard }}</div>
              <div><em style="color: #f4e21a; text-shadow: 0 1px 2px rgba(0, 0, 0, 0.25)">●</em> 黄下限: {{ item.ylbelow }}</div>
              <div><em style="color: #ff4d4f; text-shadow: 0 1px 2px rgba(0, 0, 0, 0.25)">●</em> 红下限: {{ item.rlbelow }}</div>
            </div>
          </template>
          <div class="item-value" :style="{ color: item.textColor || '#fff' }">
            {{ item.value }}
          </div>
        </a-tooltip>
        <div v-else class="item-value" :style="{ color: item.textColor || '#fff' }">
          {{ item.value }}
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import { judgeValueColor } from '../../EmsUtils';

  // 定义组件props类型
  interface Header {
    dailyText: string;
    title: string;
    date: string;
  }

  interface DataItem {
    label: string;
    value: string | number;
    rlover?: number;
    ylover?: number;
    ylbelow?: number;
    rlbelow?: number;
    standard?: number;
  }

  const props = defineProps<{
    header: Header;
    dataItems: DataItem[];
  }>();

  // 左侧固定蓝色，右侧根据值变化
  const computedDataItems = computed(() => {
    return props.dataItems.map((item, index) => {
      // 左侧固定为蓝色（索引0和2）
      if (index % 2 === 0) {
        return {
          ...item,
          backgroundColor: '#1890ff',
          textColor: '#fff',
        };
      }

      // 右侧（索引1和3）保持传入的背景色，否则根据默认规则计算
      const backgroundColor = getColorByValue(item);

      return {
        ...item,
        backgroundColor,
        textColor: getTextColor(backgroundColor),
      };
    });
  });

  // 根据数值获取背景色
  function getColorByValue(item: DataItem): string {
    return judgeValueColor(item.value, item.rlover, item.rlbelow, item.ylover, item.ylbelow);
  }

  // 根据背景色获取文字颜色
  function getTextColor(bgColor: string): string {
    if (bgColor === '#f4e21a') {
      return '#fffffe';
    } else {
      return '#fff';
    }
  }

  // 点击事件处理
  const handleItemClick = (item: DataItem) => {
    emit('openChart', item);
  };

  const emit = defineEmits(['openChart']);
</script>

<style lang="less" scoped>
  .kpidata-card {
    font-family: 'Inter', 'Helvetica Neue', Arial, sans-serif;
    background-color: #ffffff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    padding: 5px 8px;
    width: 300px;
    display: flex;
    flex-direction: column;

    .card-header {
      padding-bottom: 2px;
      border-bottom: 1px solid #f0f0f0;
      margin-bottom: 8px; /* 减少与数据区的间距 */

      .header-line {
        display: flex;
        justify-content: space-between;
        position: relative;
        height: 20px;

        .daily-label {
          font-size: 12px;
          color: #8c8c8c;
          font-weight: 500;
        }

        .date {
          position: absolute;
          left: 50%;
          transform: translateX(-50%);
          font-size: 12px;
          color: #8c8c8c;
          font-weight: 500;
        }
      }

      .header-title {
        font-size: 15px;
        font-weight: 600;
        color: #262626;
        text-align: center;
        letter-spacing: 0.5px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }

    .data-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 3px;
      height: 100%;

      .data-item {
        cursor: pointer;
        border-radius: 5px;
        padding: 4px 6px;
        display: flex;
        justify-content: space-between;
        align-items: center;

        &.blue-bg {
          background-color: #1890ff;
        }

        .item-label {
          font-size: 13px;
          color: #ffffff;
          text-shadow: 0 1px 1px rgba(0, 0, 0, 0.5);
          margin-right: 8px;
        }

        .item-value {
          font-size: 13px;
          color: #ffffff;
          letter-spacing: 0.5px;
          text-shadow: 0 1px 2px rgba(0, 0, 0, 0.7);
        }
      }
    }
  }
</style>
