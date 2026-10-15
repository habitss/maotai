<template>
  <div class="container">
    <!-- 查询区域 -->
      <a-form  :label-col="labelCol" :wrapper-col="wrapperCol" class="search-part">
        <a-row :gutter="24">
          <a-col :span="6">
            <a-form-item :label="t('language.department')">
              <kt-depart-select v-model:value="queryParam.deptCode" :placeholder="t('language.selectDepartment')"
                                select-type="depart" @change="handleDeptChange" />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item :label="t('language.productLine')">
              <kt-depart-select  v-model:value="queryParam.lineCode" select-type="line"
                                :placeholder="t('language.selectProductLine')" :parentKey="queryParam.deptCode" />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="日期范围" >
              <a-range-picker v-model:value="queryParam.timeRange" value-format="YYYY-MM-DD" />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="触发条件">
              <JDictSelectTag v-model:value="queryParam.triggerConditions" placeholder="请选择触发条件"
                :showSearch="true"
                              dictCode="mes3_mdm_dict, item_key, item_val, del_flag = 0 and group_code = 'MES3_EXCEPTION_TRRIGER_CONDITION'"/>
            </a-form-item>
          </a-col>

        </a-row>
        <a-row>
          <a-col :span="12">
            <a-button type="primary" preIcon="ant-design:search-outlined" @click="searchQuery">{{
                t('language.search')
              }}</a-button>
            <a-button type="primary" preIcon="ant-design:reload-outlined" @click="searchReset"
                      style="margin-left: 8px">{{
                t('language.reset')
              }}</a-button>
          </a-col>
        </a-row>
      </a-form>
<!--    <a-row class="list-part">-->
<!--      <a-col :span="24">-->
<!--        <LineRecordModal ref="lineListRef" :record="recordDate"  :reload="reload"/>-->
<!--      </a-col>-->
<!--    </a-row>-->
<!--    <a-row class="list-part">-->
<!--      <a-col :span="24">-->
<!--        <ExceptionRecordModal ref="lineListRef" :record="recordDate" :reload="reload"/>-->
<!--      </a-col>-->
<!--    </a-row>-->
    <a-table
      :dataSource="lineData"
      :columns="columns"
      bordered
      :pagination="false"
      style="margin: 5px 0"
    >
    </a-table>

    <a-table
      :dataSource="exceptionData"
      :columns="exceptionColumns"
      bordered
      :pagination="false"
      style="margin: 5px 0"
    >
    </a-table>

    <a-table
      :dataSource="typeData"
      :columns="typeColumns"
      bordered
      :pagination="false"
      style="margin: 5px 0"
    >
    </a-table>
  </div>
</template>

<script lang="ts" name="dataCalculation-exception-report-review" setup>
import {onMounted, provide, ref} from 'vue';
import {list} from './Review.api';
import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';
import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
import { useI18n } from '/@/hooks/web/useI18n';
import {
  columns, exceptionColumns,typeColumns
} from "@/views/dataCalculation/exception/review/Review.data";
import {addFactoryDept} from "@/utils/common/compUtils";
const { t } = useI18n();

const queryParam = ref<any>({});
const recordDate = ref();
const lineData = ref();
const exceptionData = ref();
const typeData = ref();
const departObject = ref<Object>({});
provide('departObject', departObject);

const labelCol = { style: { width: '70px' } };
const wrapperCol = { span: 18 };

/**
 * 点击提交按钮的value值
 * @param values
 */
async function handleSubmit(values: any) {
  console.log(values)
  recordDate.value = values;
}

//部门改变事件
async function handleDeptChange(value){

}

function reload(){
  reload();
}

/**
 * 查询
 */
function searchQuery() {
  console.log(queryParam.value)
  loadData(queryParam.value);
}

/**
 * 重置
 */
async function searchReset() {
  queryParam.value = {};
  await loadData({})
}

async function loadData(value){
  lineData.value = await list(Object.assign(value, {type:1}));
  exceptionData.value = await list(Object.assign(value, {type:2}));
  typeData.value = await list(Object.assign(value, {type:3}));
}

onMounted(async () => {
  lineData.value = await list(addFactoryDept({type:1}));
  exceptionData.value = await list(addFactoryDept({type:2}));
  typeData.value = await list(addFactoryDept({type:3}));
});
</script>

<style scoped>


/** 时间和数字输入框样式 */
:deep(.ant-input-number) {
  width: 100%;
}

:deep(.ant-picker) {
  width: 100%;
}

.container {
  max-width: 100%;
  padding: 10px;
}

.search-part {
  padding: 20px;
  background-color: #fff;
}

.list-part {
  background-color: #fff;
  margin-top: 10px;
}

.chartPart {
  height: 30vh !important;
  margin-top: 20px;
}
</style>
