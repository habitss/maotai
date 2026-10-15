<template>
   <a-drawer
      v-if="open == true"
      v-model:visible="open"
      class="custom-class"
      root-class-name="root-class-name"
      :root-style="{ color: 'blue' }"
      :body-style="{'background-color': '#f7f8fa', padding:'10px'}"
      :title="title"
      :footer-style="{ textAlign: 'center' }"
      width="100%"
      placement="right"
  >
    <PiAlgorithmForm ref="registerForm" @ok="submitCallback" @close="handleCancel" :formDisabled="disableSubmit" :formBpm="false" :isAdd="title === t('language.add')" />
   
   
    <template #footer>
      <a-button style="margin-right: 8px" @click="handleCancel">取消</a-button>
      <a-button v-auth="'pi.algorithm:mes3_pi_algorithm:edit'" type="primary" @click="onSave">保存</a-button>
    </template>
   </a-drawer>
</template>

<script lang="ts" setup>
    import {ref, nextTick, defineExpose} from 'vue';
    import PiAlgorithmForm from './PiAlgorithmForm.vue';
    import {BasicModal} from '/@/components/Modal';
    import { useI18n } from '/@/hooks/web/useI18n';
    const { t } = useI18n();

    const title = ref<string>('');
    const width = ref<number>(800);
    const visible = ref<boolean>(false);
    const disableSubmit = ref<boolean>(false);
    const registerForm = ref();
    const emit = defineEmits(['register', 'success']);
    const open = ref<boolean>(false);

    /**
     * 新增
     */
    function add(record) {
        //title.value = t('language.add');
        title.value = "算法配置("+record.piName+")";
        //visible.value = true;
        open.value = true;
        nextTick(() => {
            registerForm.value.add(record);
        });
    }

    /**
     * 编辑
     * @param record
     */
    function edit(record) {
        // title.value = disableSubmit.value ? t('language.detail') : t('language.edit');
        title.value = "算法配置("+record.piName+")";
        // visible.value = true;
        open.value = true;
        nextTick(() => {
            registerForm.value.edit(record);
        });
    }

    /**
     * form保存回调事件
     */
    function submitCallback(val) {
        // if(val){

        // }else{
        handleCancel();
        emit('success',val);
        // }
    }
    /**
     * 保存
     */
    function onSave(){
        registerForm.value.submitForm();
    }


    /**
     * 取消按钮回调事件
     */
    function handleCancel() {
        open.value = false;
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
/* :deep(.ant-drawer .ant-drawer-body) {
  flex: 1;
  min-width: 0;
  min-height: 0;
  padding: 10px;
  overflow: auto;
} */

</style>