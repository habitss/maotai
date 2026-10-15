<template>
  <div class="container">
    <!-- 查询区域 -->
    <a-row class="search-part">
      <a-col :span="24">
        <BasicForm @register="registerForm" @submit="reloadData()">
        </BasicForm>
      </a-col>
    </a-row>
    <a-row class="content-part">
      <a-col class="part" :span="24">
        <div class="title">部门项目加分</div>
      </a-col>
      <a-col class="part" :span="24">
        <ProjectMotivationList ref="departSummaryRef"></ProjectMotivationList>
      </a-col>
      <a-col class="part" :span="24">
        <div class="title">部门个人加分</div>
      </a-col>
      <a-col class="part" :span="24">
        <ProjectMotivationList ref="personalSummaryRef"></ProjectMotivationList>
      </a-col>
    </a-row>
  </div>
</template>

<script lang="ts" name="project.management-motivation" setup>
import { BasicForm, useForm } from '/@/components/Form/index';
import { searchFormSchema as commonSearchFormSchema, currentDepart } from '../list/ProjectManagement.data';
import ProjectMotivationList from './components/ProjectMotivationList.vue';
import {searchFormSchema} from './ProjectMotivation.data';
import { onMounted, ref } from 'vue';
const departSummaryRef = ref();
const personalSummaryRef = ref();
//表单配置
const [registerForm, { getFieldsValue, setFieldsValue }] = useForm({
  labelWidth: '70px',
  schemas: [...commonSearchFormSchema.slice(0, -2), ...searchFormSchema],
  autoSubmitOnEnter: true,
  showAdvancedButton: true,
  baseColProps: { span: 4 },
  wrapperCol: { style: {'padding-right': '10px'} },
  autoAdvancedCol: 10,
});

//刷新统计表数据
function reloadData(){
  //获取查询条件参数
  let searchParams = getFieldsValue();
  searchParams.year = searchParams.year ?? new Date().getFullYear();
  //刷新部门和个人的统计表数据
  departSummaryRef.value.reloadData('depart', searchParams);
  personalSummaryRef.value.reloadData('personal', searchParams);
}

onMounted(async () => {
  await setFieldsValue({
    sysOrgId:  currentDepart.id
  })
  reloadData();
})
</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
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

.content-part {
  background-color: #fff;
  margin-top: 10px;
}

.title{
  width: 100%;
  text-align: center;
  margin-top: 20px;
  font-weight: bold;
  font-size: 20px;
}
</style>