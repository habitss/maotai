<template>
  <div class="p-2">
    <!--查询区域-->
    <div class="jeecg-basic-table-form-container">
      <a-form ref="formRef" @keyup.enter.native="searchQuery" :model="queryParam" :label-col="labelCol"
        :wrapper-col="wrapperCol">
        <a-row :gutter="24">
          <!--年份-->
          <a-col :lg="6">
            <a-form-item label="年份">
              <a-date-picker v-model:value="queryParam.yearMonth" picker="month" valueFormat="YYYY-MM" format="YYYY-MM"/>
            </a-form-item>
          </a-col>

          <!--部门-->
          <a-col :span="6">
            <a-form-item label="部门">
              <kt-depart-select v-model:value="queryParam.deptArr" select-type="depart" placeholder="请选择部门"
                mode='multiple' @change="handleDeptChange" />
            </a-form-item>
          </a-col>

          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <span style="float: left; overflow: hidden; margin-left: -12px" class="table-page-search-submitButtons">
              <a-col :lg="6">
                <a-button type="primary" preIcon="ant-design:search-outlined" @click="searchQuery">{{
                  t('language.search') }}</a-button>
              </a-col>
            </span>
          </a-col>
        </a-row>
      </a-form>
    </div>
    <!--引用表格-->
    <BasicTable @register="registerTable">
    </BasicTable>
  </div>
</template>

<script lang="ts" name="opl-oplManage" setup>
import { ref, reactive, onMounted } from 'vue';
import { BasicTable } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage';
import { columns } from './OplManage.data';
import { list } from './OplManage.api';
import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
import { useI18n } from '/@/hooks/web/useI18n';
import dayjs from 'dayjs';
import { cloneDeep } from 'lodash-es';
const { t } = useI18n();
const formRef = ref();
const queryParam = reactive<any>({
  year: '',
  depts: '',
  dept: ''
});
//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    title: 'OPL管理回顾统计',
    api: list,
    canResize: false,
    useSearchForm: false,
    showActionColumn: false,
    pagination: false,
    actionColumn: {
      width: 120,
      fixed: 'right',
    },
    beforeFetch: async (params) => {
      return Object.assign(params, queryParam);
    },
  }
});
const [registerTable, { reload, setColumns }] = tableContext;
const labelCol = reactive({
  xs: 24,
  sm: 4,
  xl: 6,
  xxl: 4
});
const wrapperCol = reactive({
  xs: 24,
  sm: 20,
});

/**
 * 查询
 */
function searchQuery() {
  setParameter()
  reload();
}

/**
 * 设置参数
 */
function setParameter() {
  if (!queryParam.yearMonth) {
    let date = new Date();
    queryParam.yearMonth = dayjs(date).format('YYYY-MM');
  }
  let str = queryParam.yearMonth.split('-');
  queryParam.year = str[0];
  queryParam.month = str[1].replace(/^0/, "")
  let tableColumns = cloneDeep(columns);
  //替换年份
  tableColumns[2].title = queryParam.year + "年OPL管理进度";
  //替换月份
  tableColumns[3].title = queryParam.month + "月";
  setColumns(tableColumns);
}

function handleDeptChange(value) {
  queryParam.depts = value.join(',');
}

onMounted(() => {
  setParameter();
})
</script>

<style lang="less" scoped>
.jeecg-basic-table-form-container {
  padding: 0;

  .table-page-search-submitButtons {
    display: block;
    margin-bottom: 24px;
    white-space: nowrap;
  }

  .query-group-cust {
    min-width: 100px !important;
  }

  .query-group-split-cust {
    width: 30px;
    display: inline-block;
    text-align: center
  }

  .ant-form-item:not(.ant-form-item-with-help) {
    margin-bottom: 16px;
    height: 32px;
  }

  :deep(.ant-picker),
  :deep(.ant-input-number) {
    width: 100%;
  }
}
</style>