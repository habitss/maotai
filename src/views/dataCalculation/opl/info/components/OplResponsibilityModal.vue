<template>
  <j-modal :title="title" width="70%" :visible="visible" :showOkBtn="false"
    @cancel="handleCancel" cancelText="关闭" :destroyOnClose="true">
    <OplResponsibility ref="registerForm" @ok="submitCallback" :formDisabled="disableSubmit" :formBpm="false">
    </OplResponsibility>
  </j-modal>
</template>

<script lang="ts" setup>
import { ref, nextTick, defineExpose } from 'vue';
import OplResponsibility from './OplResponsibility.vue'
import JModal from '/@/components/Modal/src/JModal/JModal.vue';

const title = ref<string>('');
const visible = ref<boolean>(false);
const disableSubmit = ref<boolean>(false);
const registerForm = ref();
const emit = defineEmits(['register', 'success']);

/**
 * 编辑
 * @param record
 */
function edit(record) {
  title.value = '编写职责';
  visible.value = true;
  nextTick(() => {
    registerForm.value.edit(record);
  });
}

/**
 * 确定按钮点击事件
 */
function handleOk() {
  registerForm.value.submitForm();
}

/**
 * form保存回调事件
 */
function submitCallback() {
  handleCancel();
  emit('success');
}

/**
 * 取消按钮回调事件
 */
function handleCancel() {
  visible.value = false;
}

defineExpose({
  edit,
  disableSubmit,
});
</script>

<style lang="less">
/**隐藏样式-modal确定按钮 */
.jee-hidden {
  display: none !important;
}
</style>
<style lang="less" scoped></style>
