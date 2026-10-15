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
      <a-col :span="16">
        <CopTotalList ref="copTotalListRef"  @custom-event="handleEvent" :formData="formData"></CopTotalList>
      </a-col>
      <a-col :span="8">
        <Pie class="chartPart" :chartData="pieChartData" :option="pieOption"></Pie>
      </a-col>
    </a-row>
    <a-row class="list-part">
      <a-col :span="24">
        <BarMulti class="chartPart" :chartData="barChartData" :option="barOption" :seriesColor="colorArray"></BarMulti>
      </a-col>
    </a-row>

  </div>
</template>

<script lang="ts" name="cop-totalIndex" setup>
import { onMounted, ref } from 'vue';
import { useForm, BasicForm } from '/@/components/Form';
import { searchFormSchema, colorArray, barOption, pieOption } from './CopTotal.data';
import { list } from './CopTotal.api';
import BarMulti from '/@/components/chart/BarMulti.vue';
import Pie from '/@/components/chart/Pie.vue';
import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
import CopTotalList from './components/CopTotalList.vue'
import { useI18n } from '/@/hooks/web/useI18n';
const { t } = useI18n();
import {addFactoryDept} from "@/utils/common/compUtils";

//柱状图数据
const barChartData = ref([] as any);

//饼图数据
const pieChartData = ref([] as any);

//列表数据
const copTotalListRef = ref();

const formData = ref();

/**
 * BasicForm绑定注册;
 */
const [registerForm, { submit, setFieldsValue }] = useForm({
  schemas: searchFormSchema,
  showAdvancedButton: false,
  showResetButton: false,
  showAdvancedButton: true,
  autoAdvancedLine: 3,
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
  formData.value = values;
  //设置柱状图标题
  barOption.value.title.text = '清洗分布图' ;
  //获取后台数据
  console.log(addFactoryDept(values))
  const res = await list(addFactoryDept(values));
  if(!res){
    return;
  }
  const copListData = res.result;
  //设置柱状图数据
  let barArr = [] as any;
  for(const item of copListData){
    const name = item.name;
    barArr.push({
      "name": name,
      "value": item.complete,
      "type": "已完成",
    });
    barArr.push({
      "name": name,
      "value": item.delay,
      "type": "延迟",
    });
    barArr.push({
      "name": name,
      "value": item.delayComplete,
      "type": "延迟完成",
    });
    barArr.push({
      "name": name,
      "value": item.check,
      "type": "待审核",
    });
    barArr.push({
      "name": name,
      "value": item.noExecute,
      "type": "未执行",
    });
    barArr.push({
      "name": name,
      "value": item.unqualified,
      "type": "不合格",
    });
    barArr.push({
      "name": name,
      "value": item.plan,
      "type": "计划",
    });
    barArr.push({
      "name": name,
      "value": item.noExecutePlan,
      "type": "因生产取消",
    });

  }
  barChartData.value = barArr;

  //设置列表数据
  copTotalListRef.value.reloadData(copListData);
}

const handleEvent = (data) => {
  //设置饼图数据
  pieChartData.value = [
    {
      "name": "已完成",
      "value": data.complete,
    },
    {
      "name": "延迟",
      "value": data.delay,
    },
    {
      "name": "延迟完成",
      "value": data.delayComplete,
    },
    {
      "name": "待审核",
      "value": data.check,
    },
    {
      "name": "未执行",
      "value": data.noExecute,
    },
    {
      "name": "不合格",
      "value": data.unqualified,
    },
    {
      "name": "计划",
      "value": data.plan,
    },
    {
      "name": "因生产取消",
      "value": data.noExecutePlan,
    },
  ];

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
