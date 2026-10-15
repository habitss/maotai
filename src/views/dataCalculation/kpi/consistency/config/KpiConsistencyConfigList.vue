<template>
  <div>
    <!--引用表格-->
   <BasicTable @register="registerTable" @fetch-success="onFetchSuccess">
     <!--插槽:table标题-->
      <!--可点击单元格插槽-->
      <template #targetValueSlot="{ record, text }">
        <a @click="handleCellClick(record, 'targetValue', text)">{{ text }}</a>
      </template>
      <!--批次配置-PI-->
      <template #batchConfigSlot="{ record, text }">
        <div v-if="record.needConfig === 0 || record.level === 1 || record.level === 0" :style="{ background:'#f5f5f5'}">
          <span>&nbsp;</span>
        </div>
         <div v-else>
          <a v-if="text" @click="handleCellClick(record, 'batchConfig', text)" style="color: #52c41a">已配置</a>
          <a v-else @click="handleCellClick(record, 'batchConfig', text)" style="color: #f5222d">未配置</a>
        </div>
      </template>
      <!--合格率配置-KPI-->
      <template #passRateConfigSlot="{ record, text }">
        <div v-if="record.level > 1" :style="{ background:'#f5f5f5'}">
          <span>&nbsp;</span>
        </div>
        <div v-else>
          <a v-if="text" @click="handleCellClick(record, 'passRateConfig', text)" style="color: #52c41a">已配置</a>
          <a v-else @click="handleCellClick(record, 'passRateConfig', text)" style="color: #f5222d">未配置</a>
        </div>
      </template>
      <!--重复次数配置-PI-->
      <template #repeatCountConfigSlot="{ record, text }">
        <div v-if="record.needConfig === 0 || record.level === 1 || record.level === 0" :style="{ background: '#f5f5f5'}">
          <span>&nbsp;</span>
        </div>
        <div v-else>
          <a v-if="text" @click="handleCellClick(record, 'repeatCountConfig', text)" style="color: #52c41a">已配置</a>
          <a v-else @click="handleCellClick(record, 'repeatCountConfig', text)" style="color: #f5222d">未配置</a>
        </div>
      </template>
      <!--重复率配置-PI-->
      <template #repeatRateConfigSlot="{ record, text }">
        <div v-if="record.needConfig === 0 || record.level === 1 || record.level === 0" :style="{ background: '#f5f5f5'}">
          <span>&nbsp;</span>
        </div>
        <div v-else>
          <a v-if="text" @click="handleCellClick(record, 'repeatRateConfig', text)" style="color: #52c41a">已配置</a>
          <a v-else @click="handleCellClick(record, 'repeatRateConfig', text)" style="color: #f5222d">未配置</a>
        </div>
      </template>
    </BasicTable>
    <!-- 表单区域 -->
    <KpiConsistencyConfigModal @register="registerModal" @success="handleSuccess"></KpiConsistencyConfigModal>
  </div>
</template>

<script lang="ts" name="kpi-kpiConsistencyConfig" setup>
  import { reactive, ref } from 'vue';
  import {BasicTable} from '/@/components/Table';
  import {useModal} from '/@/components/Modal';
  import { useListPage } from '/@/hooks/system/useListPage'
  import KpiConsistencyConfigModal from './components/ConsistencyConfigModal.vue'
  import {columns, searchFormSchema} from './KpiConsistencyConfig.data';
  import {list, getImportUrl,getExportUrl} from './KpiConsistencyConfig.api';
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
              //labelWidth: 120,
              schemas: searchFormSchema,
              autoSubmitOnEnter:true,
              showAdvancedButton:true,
              fieldMapToNumber: [
              ],
              fieldMapToTime: [
              ],
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
  :deep(.ant-picker),:deep(.ant-input-number){
    width: 100%;
  }
</style>
