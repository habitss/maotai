<template>
  <BasicModal 
    @register="registerModal" 
    v-bind="$attrs" 
    :destroyOnClose = true
    :showOkBtn = false
    cancelText="关闭"
    title="标准管理">
    <ManageList ref="manageRef" :configData @closeSelect="closeSelect"></ManageList>
  </BasicModal>
</template>
<script lang="ts" setup>
import { BasicModal, useModalInner } from '/@/components/Modal';
import ManageList from '../manage/standardManageList.vue';
import { nextTick, ref } from 'vue';
//emit组件
const emit = defineEmits(['closeSelect']);
//配置数据
const configData = ref();
const manageRef = ref();
//注册modal
const [registerModal, { closeModal, setModalProps }] = useModalInner(async (data: any) => {
  configData.value = data.record;
  if('piChart' === data.record.inputType){
    nextTick(()=>{
      //设置modal
      setModalProps({
        showCancelBtn: false,
        showOkBtn: false,
      });
      //设置表格
      manageRef.value.setProps({
        showActionColumn: false,
        title: '',
        showTableSetting: false,
        rowSelection: {
          type: data.record.inputSubType === 'box' ? 'radio' : 'checkbox',
        }
      })
    });
  }
});

function closeSelect(standRecord){
  emit('closeSelect', standRecord);
  closeModal();
}
</script>
<style scoped>
  /** 时间和数字输入框样式 */
  :deep(.ant-input-number) {
    width: 100%;
  }

  :deep(.ant-picker) {
    width: 100%;
  }

  /** 下拉框默认文字颜色 */
  :deep(.ant-select-selection-placeholder) {
    color: #BEBEBE !important;
  }
</style>