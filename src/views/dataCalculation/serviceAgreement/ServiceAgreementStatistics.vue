<template>
  <div class="p-2">
    <!--引用表格-->
    <BasicTable @register="registerTable">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button  type="primary" v-auth="'serviceAgreement:mes3_service_agreement_input:exportXls'" preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
      </template>
      <!--  deptCode部门查询插槽    -->
      <template #form-provideDept="{ model, field }">
        <kt-depart-select
          v-model:value="model[field]"
          select-type="depart"
          :placeholder="t('language.selectDepartment')"
        />
      </template>

      <!--  deptCode部门查询插槽    -->
      <template #form-receiveDept="{ model, field }">
        <kt-depart-select
          v-model:value="model[field]"
          select-type="depart"
          :placeholder="t('language.selectDepartment')"
        />
      </template>

    </BasicTable>
  </div>
</template>

<script lang="ts" name="serviceAgreement-serviceAgreementInput-total" setup>
import {reactive} from 'vue';
import { BasicTable } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage';
import {searchFormProvidSchema, totalColumns} from './ServiceAgreementInput.data';
import {getExportTotalUrl, total} from './ServiceAgreementInput.api';
import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
import { useUserStore } from '/@/store/modules/user';
import {useI18n} from "@/hooks/web/useI18n";
import {getColor} from "@/utils/common/piColorUtils";

const { t } = useI18n();
const queryParam = reactive<any>({});
const userStore = useUserStore();
//月份数组
const monthObject = {
  1: 'jan',
  2: 'feb',
  3: 'mar',
  4: 'apr',
  5: 'may',
  6: 'jun',
  7: 'jul',
  8: 'aug',
  9: 'sep',
  10: 'oct',
  11: 'nov',
  12: 'dec'
}

//注册table数据
const { tableContext,onExportXls} = useListPage({
  tableProps: {
    title: '服务协议跟踪统计',
    api: total,
    expandRowByClick: true,
    showActionColumn: false,
    formConfig: {
      schemas: searchFormProvidSchema,
      showAdvancedButton: false,
    },
    beforeFetch: async (params) => {
      setTableColumns(params.year);
      params.deptCode = userStore?.userInfo?.orgCode;
      return Object.assign(params, queryParam);
    },
  },
  exportConfig: {
    name: "服务协议跟踪统计表",
    url: getExportTotalUrl,
    params: Object.assign({deptCode:userStore?.userInfo?.orgCode}, queryParam),
  },
});
const [registerTable, { setColumns }] = tableContext;

function setTableColumns(year) {
  //获取当前年份
  const currentYear = new Date().getFullYear();
  //获取当前月份
  const currentMonth = !year || year >= currentYear ? new Date().getMonth() + 1 : 12;
  let arr = [] as any;
  for(const item in monthObject){
    //只显示小于等于当前月份的列
    if(Number(item) <= currentMonth){
      arr.push({
        title: item+'月',
        align: "center",
        width:50,
        dataIndex: monthObject[item],
        customCell(record) {
          let color = '';
          if (record.title == '实际值' && record[monthObject[item]] < 1) {
            color = 'green'
          } else if (record.title == '实际值' && record[monthObject[item]] > 0) {
            color = 'red'
          }
          return {
            style: {
              'background-color': getColor(color),
            }
          }
        },
      })
    }
  }
  let newTotalColumns = [
    ...totalColumns,
    ...arr,
  ]
  setColumns(newTotalColumns);
}
</script>

<style lang="less" scoped>
.jeecg-basic-table-form-container {
  padding: 0;
  .table-page-search-submitButtons {
    display: block;
    margin-bottom: 24px;
    white-space: nowrap;
  }
  .query-group-cust{
    min-width: 100px !important;
  }
  .query-group-split-cust{
    width: 30px;
    display: inline-block;
    text-align: center
  }
  .ant-form-item:not(.ant-form-item-with-help){
    margin-bottom: 16px;
    height: 32px;
  }
  :deep(.ant-picker),:deep(.ant-input-number){
    width: 100%;
  }
}
</style>
