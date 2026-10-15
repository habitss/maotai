<template>
  <div class="p-2">
    <div class="box">
      <!--KPI填报-->
      <a-tabs v-model:activeKey="activeKey" :tabBarStyle="{padding: '0px 7px'}">
        <a-tab-pane key="2" tab="KPI目标值">
          <!--目标值-->
          <KpiTargetInput/>
        </a-tab-pane>
        <a-tab-pane key="1" tab="KPI实际值">
          <!--实际值-->
          <KpiActualInput/>
        </a-tab-pane>
      </a-tabs>
    </div>
  </div>
</template>

<script lang="ts" name="kpi-kpiReportInput" setup>
  import { ref, reactive } from 'vue';
  
  import KpiActualInput from './KpiActualInput.vue';
  import KpiTargetInput from './KpiTargetInput.vue';

  const activeKey = ref('2');

</script>

<style lang="less" scoped>
  .box{
    background-color: #fff;
  }
  // .jeecg-basic-table-form-container {
  //   padding: 0;
  //   .table-page-search-submitButtons {
  //     display: block;
  //     margin-bottom: 24px;
  //     white-space: nowrap;
  //   }
  //   .query-group-cust{
  //     min-width: 100px !important;
  //   }
  //   .query-group-split-cust{
  //     width: 30px;
  //     display: inline-block;
  //     text-align: center
  //   }
  //   .ant-form-item:not(.ant-form-item-with-help){
  //     margin-bottom: 16px;
  //     height: 32px;
  //   }
  //   :deep(.ant-picker),:deep(.ant-input-number){
  //     width: 100%;
  //   }
  // }
</style>
