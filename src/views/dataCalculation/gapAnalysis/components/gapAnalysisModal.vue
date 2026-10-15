<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose title="差距分析" width="90vw" :height="600"
    :maskClosable="false">
    <UpList ref="upRef" @close="handleClose"></UpList>
    <DownList ref="downRef" @close="handleClose"></DownList>
    <div style="padding: 0 20px; margin: auto;">
      <a-divider />
      <a-button type="primary" @click="handleAddDetail" style="margin-right: 10px;">新增行</a-button>
      <a-button type="primary" @click="handleSaveDetail" :loading="saveDetailLoading">保存</a-button>
    </div>
    <FiveWhyDetailList ref="detailListRef" @close="handleClose"></FiveWhyDetailList>
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { queryData, saveOrUpdate } from '../gapAnalysis.api';
import { useMessage } from '/@/hooks/web/useMessage';
import UpList from './upList.vue';
import DownList from './downList.vue';
import FiveWhyDetailList from '/@/views/dataCalculation/fiveWhy/components/measureList.vue';
//注册Message
const { createMessage: msg } = useMessage();
const emit = defineEmits(['success']);
const upRef = ref();
const downRef = ref();
const detailListRef = ref();
const saveDetailLoading = ref(false);
//后台详情数据
const detailData = ref({} as any);
//MODAL注册
const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
  setModalProps({ confirmLoading: false, showCancelBtn: false, showOkBtn: false });
  const res = await queryData({ kpiCode: 'test' });
  detailData.value = res;
  detailListRef.value.setMeasureData(res.detailList ?? []);
});
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
        kpiCode: 'test',
        detailList: detailList
      }
      await saveOrUpdate(obj);
      //关闭弹窗
      closeModal();
      emit('success');
    }
  } catch (error) {
    msg.error({ content: '保存失败' });
    console.log('error :>> ', error);
  } finally {
    saveDetailLoading.value = false;
  }
}

function handleClose() {
  closeModal();
  emit('success');
}
</script>