<template>
  <div class="kpi-consistency-report">
    <BasicTable @register="registerTable" @fetch-success="onFetchSuccess">
      <!--可点击单元格 - 目标值-->
      <template #targetValueSlot="{ record, text }">
        <a
          class="cell-link"
          @click="handleCellClick(record, 'targetValue', text)"
        >
          <span class="cell-link__text">{{ text }}</span>
        </a>
      </template>
    </BasicTable>
  </div>
</template>

<script lang="ts" name="kpi-kpiConsistencyConfig" setup>
  import { reactive, ref } from 'vue';
  import {BasicTable} from '/@/components/Table';
  import {useModal} from '/@/components/Modal';
  import { useListPage } from '/@/hooks/system/useListPage'
  import {columns, searchFormSchema} from './KpiConsistencyReport.data';
  import {list, getImportUrl,getExportUrl} from './KpiConsistencyReport.api';
  const queryParam = reactive<any>({});
  //注册model
  const [registerModal, {openModal}] = useModal();
  //注册table数据
  const { prefixCls,tableContext,onExportXls,onImportXls } = useListPage({
      tableProps:{
           api: list,
           columns,
           canResize:true,
           showActionColumn: false,
           pagination: { pageSize: 100 },
           formConfig: {
              schemas: searchFormSchema,
              autoSubmitOnEnter:true,
              showAdvancedButton:true,
              fieldMapToNumber: [],
              fieldMapToTime: [],
            },
            beforeFetch: (params) => {
              params.column = 'sort';
              params.order = 'asc';
              return Object.assign(params, queryParam);
            },
      },
       exportConfig: {
            name:"一致KPI数据配置",
            url: getExportUrl,
            params: queryParam,
          },
          importConfig: {
            url: getImportUrl,
            success: handleSuccess
          },
  })

  const [registerTable, {reload, getDataSource, setColumns }] = tableContext


  // 标题映射
  const titleMap: Record<string, string> = {
    targetValue: '目标值',
    batchConfig: '配置配置-PI',
    passRateConfig: '合格率配置-KPI',
    repeatCountConfig: '重复次数配置-PI',
    repeatRateConfig: '重复率配置-PI',
  };

  /**
   * 单元格点击事件
   */
  function handleCellClick(record: any, field: string, value: any) {
    openModal(true, {
      isUpdate: !!value,
      showFooter: true,
      record: {
        ...record,
        configField: field,
        configTitle: titleMap[field] || field,
      },
    });
  }

  /**
   * 合并单元格
   */
  function mergeCells(text: string, data: any[], key: string, index: number) {
    if (index !== 0 && text === data[index - 1][key]) {
      return { rowSpan: 0 };
    }
    let rowSpan = 1;
    for (let i = index + 1; i < data.length; i++) {
      if (text !== data[i][key]) {
        break;
      }
      rowSpan++;
    }
    return { rowSpan };
  }

  /**
   * 数据加载成功后更新列配置
   */
  function onFetchSuccess() {
    const data = getDataSource() || [];
    const newColumns = columns.map(col => {
      if (col.dataIndex === 'kpiCode' || col.dataIndex === 'kpiName') {
        return {
          ...col,
          customCell: (record: any, index: number) => {
            return mergeCells(record[col.dataIndex as string], data, col.dataIndex as string, index);
          }
        };
      }
      return col;
    });
    setColumns(newColumns);
  }
   /**
    * 成功回调
    */
  function handleSuccess() {
      reload();
   }

</script>

<style lang="less" scoped>
  @primary-color: #D92622;
  @primary-hover: lighten(@primary-color, 6%);
  @success-color: #55D187;
  @text-secondary: #909399;
  @bg-muted: #f0f2f5;
  @bg-muted-bar: linear-gradient(90deg, #ebeef3 0%, #f5f7fa 100%);
  @border-radius-sm: 4px;

  .kpi-consistency-report {
    :deep(.ant-picker),
    :deep(.ant-input-number) {
      width: 100%;
    }

    /* ── 表头美化 ── */
    :deep(.ant-table-thead > tr > th) {
      background: linear-gradient(180deg, #fafbfc 0%, #f0f2f5 100%) !important;
      font-weight: 600;
      color: #303133;
      border-bottom: none;
      font-size: 13px;
    }

    /* ── 行交替与悬停 ── */
    :deep(.ant-table-tbody > tr) {
      transition: background 0.2s ease;

      &:hover > td {
        background: rgba(217, 38, 34, 0.04) !important;
      }

      &:nth-child(even) > td {
        background: #fafbfc;
      }
    }

    /* ── 合并单元格（KPI编码/名称）垂直居中 ── */
    :deep(.ant-table-tbody > tr > td) {
      vertical-align: middle;
    }

    /* ── 单元格内容通用 ── */
    .cell-value {
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 28px;
      font-variant-numeric: tabular-nums;
      font-size: 13px;
    }

    /* ── 灰化单元格 - 斜线纹理，占满整个单元格 ── */
    :deep(td.cell-muted) {
      background: repeating-linear-gradient(
        -45deg,
        #f7f7f7,
        #f7f7f7 4px,
        #f0f0f0 4px,
        #f0f0f0 8px
      ) !important;
    }

    /* ── 可点击链接单元格 ── */
    .cell-link {
      position: relative;
      display: inline-flex;
      align-items: center;
      padding: 2px 8px;
      border-radius: @border-radius-sm;
      cursor: pointer;
      text-decoration: none;
      transition: all 0.2s ease;

      &__text {
        color: @primary-color;
        font-weight: 500;
        letter-spacing: 0.02em;
        border-bottom: 1px dashed transparent;
        transition: border-color 0.2s ease;
      }

      &:hover {
        background: rgba(217, 38, 34, 0.06);

        .cell-link__text {
          color: @primary-hover;
          border-bottom-color: @primary-hover;
        }
      }

      &:active {
        background: rgba(217, 38, 34, 0.12);
        transform: scale(0.97);
      }
    }
  }
</style>
