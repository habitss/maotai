<template>
   <BasicModal
    :title="title"
    :width="width"
    :visible="visible"
    @ok="handleOk"
    :okButtonProps="{ class: { 'jee-hidden': disableSubmit } }"
    @cancel="handleCancel"
    :cancelText="t('language.closeText')"
  >
    <PiBasePointForm ref="registerForm" @ok="submitCallback" :formDisabled="disableSubmit" :formBpm="false" :isAdd="title === t('language.add')" />
  </BasicModal>
</template>

<script lang="ts" setup>
    import {ref, nextTick, defineExpose} from 'vue';
    import PiBasePointForm from './brandForm.vue';
    import {BasicModal} from '/@/components/Modal';
    import { useI18n } from '/@/hooks/web/useI18n';
    const { t } = useI18n();

    const title = ref<string>('');
    const width = ref<number>(800);
    const visible = ref<boolean>(false);
    const disableSubmit = ref<boolean>(false);
    const registerForm = ref();
    const emit = defineEmits(['register', 'success']);

    /**
     * 新增
     */
    function add() {
        title.value = t('language.add');
        visible.value = true;
        nextTick(() => {
            registerForm.value.add();
        });
    }

    /**
     * 编辑
     * @param record
     */
    function edit(record) {
        title.value = disableSubmit.value ? t('language.detail') : t('language.edit');
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
        add,
        edit,
        disableSubmit,
    });
</script>

<style>
/**隐藏样式-modal确定按钮 */
.jee-hidden {
  display: none !important;
}
</style>