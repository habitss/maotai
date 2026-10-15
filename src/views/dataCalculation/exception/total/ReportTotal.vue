<template>
  <div class="container">
    <!-- 查询区域 -->
    <a-row class="search-part">
      <a-col :span="24">
        <BasicForm @register="registerForm" @submit="handleSubmit" >
          <!--  deptCode部门插槽    -->
          <template #deptCode="{ model, field }">
            <kt-depart-select
              v-model:value="model[field]"
              select-type="depart"
              :placeholder="t('language.selectDepartment')"
              @change="handleDeptChange"
            />
          </template>

          <!--  lineCode产线插槽    -->
          <template #lineCode="{ model, field }">
            <kt-depart-select
              v-model:value="model[field]"
              select-type="line"
              :placeholder="t('language.selectProductLine')"
              :parentKey="model.deptCode"
            />
          </template>
        </BasicForm>
      </a-col>
    </a-row>
    <a-row class="list-part">
      <a-col :span="12">
        <Bar class="chartPart" :chartData="barChartData" :option="barOption" ></Bar>
      </a-col>
      <a-col :span="12">
        <Pie class="chartPart" :chartData="pieChartData" :option="pieOption"></Pie>
      </a-col>
    </a-row>

  </div>
</template>

<script lang="ts" name="exception-totalIndex" setup>
import { onMounted, ref } from 'vue';
import { useForm, BasicForm } from '/@/components/Form';
import {colorArray, barOption, pieOption, searchFormSchema} from './Total.data';
import { list } from './Total.api';
import Bar from '/@/components/chart/Bar.vue';
import Pie from '/@/components/chart/Pie.vue';
import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
import { useI18n } from '/@/hooks/web/useI18n';
const { t } = useI18n();
//柱状图数据
const barChartData = ref([] as any);

//饼图数据
const pieChartData = ref([] as any);

//列表数据

/**
 * BasicForm绑定注册;
 */
const [registerForm, { submit, setFieldsValue }] = useForm({
  schemas: searchFormSchema,
  showAdvancedButton: false,
  showResetButton: false,
  actionColOptions: { span: 2 },
  fieldMapToNumber: [],
  baseColProps: { span: 6 },
  labelCol: { style: { width: '80px' } },
  wrapperCol: { style: { marginRight: '20px' } },
});

//配置modal

/**
 * 点击提交按钮的value值
 * @param values
 */
async function handleSubmit(values: any) {
  //设置柱状图标题
  barOption.value.title.text = '异常触发条件分析' ;
  //获取后台数据
  const res = await list(values);
  console.log(res)
  if(!res){
    return;
  }
  barChartData.value = res;
  pieChartData.value = res;

  //设置列表数据
}


//部门改变事件
async function handleDeptChange(value){
  //清空产线
  await setFieldsValue({
    lineCode: '',
  });
}

onMounted(() => {
  submit();
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
  padding: 14px;
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
