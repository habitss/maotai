<template>
  <UpList ref="upRef" :tableTitle></UpList>
  <DownList ref="downRef"></DownList>
  <div style="padding: 0 20px; margin: auto;">
    <a-divider />
    <a-button type="primary" @click="handleAddDetail" style="margin-right: 10px;">新增行</a-button>
    <a-button type="primary" @click="handleSaveDetail" :loading="saveDetailLoading">保存</a-button>
  </div>
  <FiveWhyDetailList ref="detailListRef"></FiveWhyDetailList>
</template>

<script lang="ts" name="dataCalculation-gapAnalysis" setup>
import { ref } from 'vue';
import { saveOrUpdate } from './gapAnalysis.api';
import { useMessage } from '/@/hooks/web/useMessage';
import { queryData } from './gapAnalysis.api';
import UpList from './components/upList.vue';
import DownList from './components/downList.vue';
import FiveWhyDetailList from '/@/views/dataCalculation/fiveWhy/components/measureList.vue';
//注册Message
const { createMessage: msg } = useMessage();
const emit = defineEmits(['success']);
//子组件引用
const upRef = ref();
//子组件引用
const downRef = ref();
//详情组件引用
const detailListRef = ref();
//保存按钮loading状态
const saveDetailLoading = ref(false);
//表格标题
const tableTitle = ref('差距分析');
//后台详情数据
const detailData = ref({} as any);
//kpi行数据
const kpiRowData = ref({} as any);
//新增
function handleAddDetail() {
  //调用子组件添加方法
  detailListRef.value.addMeasure(null, null, 3);
}
//保存
async function handleSaveDetail() {
  try {
    saveDetailLoading.value = true;
    //调用措施组件添加方法
    const detailList = detailListRef.value.getMeasureData();
    if (detailList && detailList.length > 0) {
      const obj = {
        id: detailData.value.id,
        kpiCode: kpiRowData.value.kpiName.id,
        kpiColKey: kpiRowData.value.colKey,
        kpiYear: kpiRowData.value.year,
        detailList: detailList
      }
      await saveOrUpdate(obj);
      reloadDetailData();
      //关闭弹窗
      emit('success');
    }
  } catch (error) {
    msg.error({ content: '保存失败' });
    console.log('error :>> ', error);
  } finally {
    saveDetailLoading.value = false;
  }
}

async function reloadDetailData(){
  detailData.value = await queryData({
    kpiCode: kpiRowData.value.kpiName.id,
    kpiColKey: kpiRowData.value.colKey,
    kpiYear: kpiRowData.value.year,
  });
  detailListRef.value.setMeasureData(detailData.value?.detailList ?? []);
}

async function init(obj){
  kpiRowData.value = obj.kpiRowData;
  upRef.value.initUpList(obj);
  downRef.value.initDownList(obj);
  reloadDetailData();
}

//暴露方法
defineExpose({
  init
})
</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}
</style>