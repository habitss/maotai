<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose :title="title" :width="1000" @ok="handleSubmit">
      <BasicForm @register="registerForm" name="BestPracticePromotionLogForm" style="padding-right: 20px;">
        <template #ifTrack="{}">
          <a-button type="link" @click="handleActionClick(1)">添加</a-button>
          <a-button type="link" @click="handleActionClick(2)">查看</a-button>
        </template>
      </BasicForm>
      
      <!-- 行动计划 -->
      <RaModal @register="registerActionModal" @success="onActionnSuccess"></RaModal>
  </BasicModal>
</template>

<script lang="ts" setup>
    import {ref, computed, unref} from 'vue';
    import {BasicModal, useModalInner} from '/@/components/Modal';
    import {BasicForm, useForm} from '/@/components/Form/index';
    import {formSchema} from '../BestPracticePromotionLog.data';
    import {saveOrUpdate} from '../BestPracticePromotionLog.api';
    import { useModal } from '/@/components/Modal';
    import RaModal from '/@/views/dataCalculation/actionPlan/ra/components/RaModal.vue';
    import dayjs from 'dayjs';
    import { useUserStore } from '/@/store/modules/user';
import { cloneDeep } from 'lodash-es';
    //获取用户信息
    const { userInfo } = useUserStore();
    // Emits声明
    const emit = defineEmits(['register','success']);
    const isUpdate = ref(true);
    const isDetail = ref(false);
    //行动计划相关数据
    const actionBaseData = ref({
      dataId: 'temp_orimotion_'+dayjs.valueOf(),
      raiseDate: dayjs().format('YYYY-MM-DD'),
      status: 1,
      createDept: userInfo?.orgCode,
      putUserId: userInfo?.id,
      verifyUserId: userInfo?.id,
      planFinishDate: dayjs().add(1, 'day').format('YYYY-MM-DD'),
      actionLevel: '1',
      enterMeeting: '0',
    });

    //注册model
    const [registerActionModal, { openModal: openActionModal }] = useModal();
    //表单配置
    const [registerForm, { setProps,resetFields, setFieldsValue, validate, scrollToField }] = useForm({
        labelCol: { style: { width: '140px' } },
        schemas: formSchema,
        showActionButtonGroup: false,
        wrapperCol: {
          span: 24,
        },
        baseColProps: {span: 8}
    });
    //表单赋值
    const [registerModal, {setModalProps, closeModal}] = useModalInner(async (data) => {
        //重置表单
        await resetFields();
        setModalProps({confirmLoading: false,showCancelBtn:!!data?.showFooter,showOkBtn:!!data?.showFooter});
        isUpdate.value = !!data?.isUpdate;
        isDetail.value = !!data?.showFooter;
        if (unref(isUpdate)) {
            actionBaseData.value.dataId = data.record?.id;
            data.record.id = data.record?.id;
            //表单赋值
            await setFieldsValue({
                ...data.record,
            });
        }
        // 隐藏底部时禁用整个表单
       setProps({ disabled: !data?.showFooter })
    });
    //设置标题
    const title = computed(() => (!unref(isUpdate) ? '新增' : !unref(isDetail) ? '详情' : '编辑'));
    //表单提交事件
    async function handleSubmit(v) {
        try {
            let values = await validate();
            setModalProps({confirmLoading: true});
            //提交表单
            await saveOrUpdate(values, isUpdate.value);
            //关闭弹窗
            closeModal();
            //刷新列表
            emit('success');
        } catch ({ errorFields }) {
           if (errorFields) {
             const firstField = errorFields[0];
             if (firstField) {
               scrollToField(firstField.name, { behavior: 'smooth', block: 'center' });
             }
           }
           return Promise.reject(errorFields);
        } finally {
            setModalProps({confirmLoading: false});
        }
    }

    //行动计划维护点击事件
    function handleActionClick(type) {
      const record = cloneDeep(actionBaseData.value)
      openActionModal(true, {
        record,
        type,
        dataId: record.dataId,
      });
    }

    //行动计划填报成功回调事件
    async function onActionnSuccess(){
      await setFieldsValue({

      })
    }
</script>

<style lang="less" scoped>
	/** 时间和数字输入框样式 */
  :deep(.ant-input-number) {
    width: 100%;
  }

  :deep(.ant-calendar-picker) {
    width: 100%;
  }

  :deep(.ant-form-item .ant-form-item-label >label){
    min-height: 36px !important;
    word-break: break-all !important; 
    white-space: normal !important;
  }
</style>