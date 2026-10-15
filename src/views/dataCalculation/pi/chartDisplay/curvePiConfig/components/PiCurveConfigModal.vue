<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose :title="title" :width="800" @ok="handleSubmit">
      <BasicForm @register="registerForm" name="PiCurveConfigForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
    import {ref, computed, unref} from 'vue';
    import {BasicModal, useModalInner} from '/@/components/Modal';
    import {BasicForm, useForm} from '/@/components/Form/index';
    import {getSearchFormSchema} from '../PiCurveConfig.data';
    import {saveOrUpdate} from '../PiCurveConfig.api';
    import{getDevice} from '/@/views/dataCalculation/pi/chartDisplay/standardConfig/PiCurveStandard.api';
    // Emits声明
    const emit = defineEmits(['register','success']);
    const isUpdate = ref(true);
    const isDetail = ref(false);
    const dataSource = ref('');

    // 加载设备选项
    const fetchUnits = async (areaNo) => {
      if(!areaNo){
        return false;
      }
      // 根据数据源和区域过滤设备
      let res = await getDevice({dataSource: dataSource.value, areaNo})
      let unitArray = []
      if(res){
        res.forEach((item, index, array) => {
          //0标准和1目标
          unitArray.push({
            value: `${item.deviceCode}`,
            label: item.deviceName
          })
        })
        return unitArray;
      }
      return [];
    };

    //pcu变更
    const handleAreaChange = async (value , data,first = false) => {
      //const dataSource = getFieldsValue().dataSource;
      const options = await fetchUnits(value);
      updateSchema({ field: 'deviceNo', componentProps: { options } });
      if(!first){
        setFieldsValue({ deviceNo: undefined});
      }
    };

    //表单配置
    const [registerForm, { setProps,resetFields, setFieldsValue, validate, scrollToField,getFieldsValue,updateSchema }] = useForm({
        labelWidth: 150,
        schemas: getSearchFormSchema(handleAreaChange),
        showActionButtonGroup: false,
        baseColProps: {span: 24}
    });
    //表单赋值
    const [registerModal, {setModalProps, closeModal}] = useModalInner(async (data) => {
        dataSource.value = data.record.dataSource;
        //重置表单
        await resetFields();
        setModalProps({confirmLoading: false,showCancelBtn:!!data?.showFooter,showOkBtn:!!data?.showFooter});
        isUpdate.value = !!data?.isUpdate;
        isDetail.value = !!data?.showFooter;
        if (unref(isUpdate)) {
            //表单赋值
            await setFieldsValue({
                ...data.record,
            });
        }
        // 隐藏底部时禁用整个表单
       setProps({ disabled: !data?.showFooter })

       if (data.record.areaNo) await handleAreaChange(data.record.areaNo,{}, true);
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
</script>

<style lang="less" scoped>
	/** 时间和数字输入框样式 */
  :deep(.ant-input-number) {
    width: 100%;
  }

  :deep(.ant-calendar-picker) {
    width: 100%;
  }
</style>