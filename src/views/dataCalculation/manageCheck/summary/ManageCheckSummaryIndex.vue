<template>
  <div class="container">
    <!-- 查询区域 -->
    <a-row class="search-part">
      <a-col :span="24">
        <BasicForm @register="registerForm" @submit="getData">
          <!--  部门插槽  -->
          <template #dept="{ model, field, schema }">
            <kt-depart-select
              v-model:value="model[field]"
              select-type="depart"
              :placeholder="`请选择${schema.label}`"
              :valueField = "'orgCode'"
              :disabled = " currentDepart.codeLength > 9 "
            />
          </template>
        </BasicForm>
      </a-col>
    </a-row>
    <a-row class="content-part">
      <a-col>
        <a-radio-group v-model:value="viewType" :style="{ margin: '10px' }" @change="handleRadioGroupChange">
          <a-radio-button value="list">列表视图</a-radio-button>
          <a-radio-button value="chart">图表视图</a-radio-button>
        </a-radio-group>
        <a-button type="link">(该页面只统计分层检查清单中的问题数，如一个问题录入多个行动只算一条)</a-button>
      </a-col>
      <a-col class="part" :span="24">
        <div v-for="i in 4" key="i">
          <SummaryList :ref="el => { if (el) listRef[i] = el }" v-if="viewType === 'list' && !hideType.includes(i)" :type = i :monthRange></SummaryList>
          <SummaryChart :ref="el => { if (el) chartRef[i] = el }" v-if="viewType === 'chart' && !hideType.includes(i)" :type = i :monthRange></SummaryChart>
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script lang="ts" name="manage.check-manageCheckSummary" setup>
import { useForm, BasicForm } from '/@/components/Form';
import { searchFormSchema, hideType, monthRange, currentDepart } from './ManageCheckSummary.data';
import { nextTick, onMounted, ref } from 'vue';
import { list } from './ManageCheckSummary.api';
import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
import SummaryList from './components/SummaryList.vue';
import SummaryChart from './components/SummaryChart.vue';
import { cloneDeep } from 'lodash-es';
// 视图类型选择;
const viewType = ref('list');
// 列表ref对象
const listRef = ref([] as any);
// 图表ref对象
const chartRef = ref([] as any);
//数据
const dataArr = ref([] as any);

/**
 * BasicForm绑定注册;
 */
const [registerForm, { submit, getFieldsValue }] = useForm({
  schemas: searchFormSchema,
  showAdvancedButton: false,
  alwaysShowLines: 2,
  showResetButton: false,
  actionColOptions: { span: 2 },
  fieldMapToTime: [['checkDate', ['checkDate_begin', 'checkDate_end'], 'YYYY-MM-DD']],
  fieldMapToNumber: [],
  baseColProps: { span: 6 },
  labelCol: { style: { width: '70px' } },
  wrapperCol: { style: { marginRight: '20px' } },
});

/**获取数据 */
async function getData(){
  listRef.value.forEach((element) => {
    if(element){
      element.setLoading(true);
    }
  });
  let searchFields = cloneDeep(getFieldsValue());
  
  const res = await list(searchFields);
  dataArr.value = ["", res?.checkUser, res?.checkOwnerClassGroup, res?.checkOwnerPost, res?.checkOwner];
  handleRadioGroupChange()
}

function handleRadioGroupChange(){
  nextTick(() => {
    if(viewType.value === 'list'){
      listRef.value.forEach((element, index) => {
        if(element){
          element.reloadData(dataArr.value[index], monthRange.value);
        }
      });
    } else {
      chartRef.value.forEach((element, index) => {
        if(element){
          element.reloadData(dataArr.value[index]);
        }
      });
    }
  })
}

onMounted(async () => {
  submit();
})
</script>

<style scoped>
/** 时间和数字输入框样式 */
:deep(.ant-input-number) {
  width: 100%;
}

:deep(.ant-picker) {
  width: 100%;
}

.container{
  max-width: 100%;
  padding: 10px;
}

.search-part{
  margin-bottom: 10px;
  padding: 14px;
  background-color: #fff;
}

.content-part{
  background-color: #fff;

  .part{
    height: calc(100vh - 332px); 
    overflow: auto;
  }
}
</style>