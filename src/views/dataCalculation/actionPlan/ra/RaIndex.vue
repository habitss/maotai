<template>
  <div class="container">
    <!-- 查询区域 -->
    <a-row class="search-part">
      <a-col :span="24">
        <BasicForm @register="registerForm" @submit="handleSubmit"/>
      </a-col>
    </a-row>
    <a-row class="list-part">
      <a-col :span="24" class="title">
        {{ titleText }}
      </a-col>
    </a-row>
    <a-row class="list-part">
      <a-col v-show="checkType.includes(1)" :span="12">
        <!-- 提出单 -->
        <RaList :key="1" ref="putListRef" :listType=1 @success="submit" />
      </a-col>
      <a-col v-show="checkType.includes(2)" :span="12">
        <!-- 责任单 -->
        <RaList :key="2" ref="dutyListRef" :listType=2 @success="submit" />
      </a-col>
      <a-col v-show="checkType.includes(3)" :span="12">
        <!-- 执行单 -->
        <RaList :key="3" ref="executeListRef" :listType=3 @success="submit" />
      </a-col>
      <a-col v-show="checkType.includes(4)" :span="12">
        <!-- 确认单 -->
        <RaList :key="4" ref="verifyListRef" :listType=4 @success="submit" />
      </a-col>
    </a-row>
  </div>
</template>

<script lang="ts" name="action.plan-raIndex" setup>
import { useForm, BasicForm } from '/@/components/Form';
import { searchFormSchema, checkType } from './Ra.data';
import { list } from './Ra.api';
import { useUserStore } from '/@/store/modules/user';
import RaList from './components/RaList.vue'
import { onMounted, ref } from 'vue';
import { cloneDeep } from 'lodash-es';
// 用户信息
const { userInfo } = useUserStore();
//标题
const titleText = userInfo?.realname + '行动/责任单';
//子组件ref对象
const putListRef = ref();
const dutyListRef = ref();
const executeListRef = ref();
const verifyListRef = ref();

/**
 * BasicForm绑定注册;
 */
const [registerForm, { submit }] = useForm({
  schemas: searchFormSchema,
  showAdvancedButton: true,
  autoAdvancedCol: 4,
  alwaysShowLines: 2,
  showResetButton: false,
  actionColOptions: { span: 2 },
  fieldMapToTime: [['planFinishDate', ['planFinishDate_begin', 'planFinishDate_end'], 'YYYY-MM-DD']],
  fieldMapToNumber: [],
  baseColProps: { span: 5 },
  labelCol: { style: { width: '90px' } },
  wrapperCol: { style: { marginRight: '20px' } },
});

/**
 * 点击提交按钮的value值
 * @param values
 */
async function handleSubmit(values: any) {
  //设置子组件加载状态为true
  await putListRef.value?.setLoading(true);
  await dutyListRef.value?.setLoading(true);
  await executeListRef.value?.setLoading(true);
  await verifyListRef.value?.setLoading(true);
  //清除不必要的查询字段
  delete values.checkType;
  values.pageSize = 999;
  values.userIds = userInfo?.id;
  values.status = 1;
  const { records } = await list(values);
  //刷新子组件列表数据
  putListRef.value?.reloadData(cloneDeep(records.filter(item => item.putUserId == userInfo?.id)));
  dutyListRef.value?.reloadData(cloneDeep(records.filter(item => item.dutyUserId == userInfo?.id)));
  executeListRef.value?.reloadData(cloneDeep(records.filter(item => item.executeUserId == userInfo?.id)));
  verifyListRef.value?.reloadData(cloneDeep(records.filter(item => item.verifyUserId == userInfo?.id)));
}

onMounted(() => {
  checkType.value = [1, 2, 3, 4];
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

.list-part{
  background-color: #fff;

  .title{
    font-weight: bold;
    font-size: 1.4rem;
    text-align: center;
    margin-top: 16px;
  }
}
</style>