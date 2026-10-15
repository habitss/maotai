<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose :title="title" :width="800" @ok="handleSubmit">
      <BasicForm @register="registerForm" name="KpiPlatoTwoForm">
        <!--年份-->
        <!-- <template #platoOneYear="{ model, field }">
          <a-date-picker v-model:value="model[field]" picker="year" placeholder="请选择所属年份" valueFormat="YYYY" format="YYYY" style="width: 100%" @change="platoOneList"/>
        </template> -->
        <!--一级柏拉图生产流程名称-->
        <template #platoOneSelect="{ model, field }">
          <a-select v-model:value="model[field]" show-search :options="platoOneData" :fieldNames= "{children:'children', label:'processName', value: 'code' }" placeholder="请选择" allowClear @change="platoOne"></a-select>
        </template>
      </BasicForm>
  </BasicModal>
</template>

<script lang="ts" setup>
    import {ref, computed, unref} from 'vue';
    import {BasicModal, useModalInner} from '/@/components/Modal';
    import {BasicForm, useForm} from '/@/components/Form/index';
    import {formSchema} from '../KpiPlatoTwo.data';
    import {saveOrUpdate} from '../KpiPlatoTwo.api';
    import {list} from '../KpiPlatoOne.api';
    import { useUserStore } from '/@/store/modules/user';
    // 用户信息
    const userStore = useUserStore();
    // Emits声明
    const emit = defineEmits(['register','success']);
    const isUpdate = ref(true);
    const isDetail = ref(false);
    const platoOneData = ref([]);
    //表单配置
    const [registerForm, { setProps,resetFields, setFieldsValue, validate, scrollToField }] = useForm({
        labelWidth: 150,
        schemas: formSchema,
        showActionButtonGroup: false,
        baseColProps: {span: 24}
    });
    //表单赋值
    const [registerModal, {setModalProps, closeModal}] = useModalInner(async (data) => {
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
       platoOneList();
    });
    //设置标题
    const title = computed(() => (!unref(isUpdate) ? '新增' : !unref(isDetail) ? '详情' : '编辑'));
    //表单提交事件
    async function handleSubmit(v) {
        try {
            let values = await validate();
            setModalProps({confirmLoading: true});

            values.deptCode = userStore.departCode;
            values.factoryCode = userStore.factoryCode;
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

    //一级柏拉图(生产流程名称)
    async function platoOneList(){
      // if(isupdate != false){
      //   await setFieldsValue({ parentCode: '' });
      // }
      platoOneData.value = [];
      let res = await list({pageNo: 1,pageSize: 9999});
      if(res && res.records){
        // infoTreeData.value.push(...res.records);
        platoOneData.value = res.records;
      }
    }
    //获取KPIcode
    async function platoOne(platoOneCode){
      const result = platoOneData.value.find(item => item.code === platoOneCode);
      await setFieldsValue({ superCode: result.superCode });
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