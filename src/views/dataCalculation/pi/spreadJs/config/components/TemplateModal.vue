<template>
   <BasicModal 
    @register="registerModal" 
    v-bind="$attrs" 
    :title="`${title}`"
    :cancelText="t('language.closeText')"
    :defaultFullscreen = true
    :canFullscreen = false
    @ok="handleSubmit">
    <SpreadJsModule ref="spreadJsRef" ></SpreadJsModule>
  </BasicModal>
</template>

<script lang="ts" setup>
  import {ref, reactive, computed, unref} from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import SpreadJsModule from '../../spreadJsModule/index.vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  // 声明Emits
  const emit = defineEmits(['success']);

  //标题
  const title = ref();

  //子组件实例
  const spreadJsRef = ref();
  //数据记录
  const record = ref();

  //注册modal
  const [registerModal, { closeModal }] = useModalInner(async (data: any) => {
    //先清理再打开
    spreadJsRef.value?.resetSpread();
    //设置标题
    title.value = data.record.templateName;
    // //设置数据记录
    record.value = data.record;
    data.record.config = true
    //
    // spreadJsRef.value.initData(data.record);
    // 等待 Modal 渲染 + 动画完成
    await new Promise(resolve => setTimeout(resolve, 80));
    spreadJsRef.value.initData(data.record);
    spreadJsRef.value.fixLayout();
  });

  function handleSubmit(){
    spreadJsRef.value.submitForm();
  }
</script>

<style lang="less" scoped>
  .jeecg-basic-table-form-container {
    padding: 0;
    .table-page-search-submitButtons {
      display: block;
      margin-bottom: 24px;
      white-space: nowrap;
    }
    .spreadContainer {
      padding: 10px;
      padding-left: 0;
      height: 60vh;
    }
    .spreadHost {
      width: 95%;
      height: 100%;
    }
    .footer-btn {
      margin-top: 10px;
      text-align: center;
    }
    .footer-btn button {
      width: 169px;
      height: 42px;
      font-size: 15px;
      border-radius: 0;
    }
  }
</style>
