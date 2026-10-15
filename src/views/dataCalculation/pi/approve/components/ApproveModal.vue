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
    <ApproveForm ref="registerForm" @ok="submitCallback" :formDisabled="disableSubmit" :formBpm="false" :isAdd="title === t('language.add')" />
  </BasicModal>
</template>

<script lang="ts" setup>
    import {ref, nextTick, defineExpose} from 'vue';
    import ApproveForm from './ApproveForm.vue';
    import {BasicModal} from '/@/components/Modal';
    import { useI18n } from '/@/hooks/web/useI18n';
    const { t } = useI18n();

    const title = ref<string>('');
    const width = ref<number>(1200);
    const visible = ref<boolean>(false);
    const disableSubmit = ref<boolean>(false);
    const registerForm = ref();
    const emit = defineEmits(['register', 'success']);

    /**
     * 新增
     */
    function add(tableParams) {
        if(tableParams.type === 0){
            title.value = '修改-'+tableParams.field;
        }else{
            title.value = '剔除-'+tableParams.field;
        }
        visible.value = true;
        nextTick(() => {
            registerForm.value.add(tableParams);
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