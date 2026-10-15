<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose :title="title" :width="800" @ok="handleSubmit">
      <BasicForm @register="registerForm" name="KpiPlatoOneForm">
        <!--年份-->
        <!-- <template #platoOneYear="{ model, field }">
          <a-date-picker v-model:value="model[field]" picker="year" placeholder="请选择所属年份" valueFormat="YYYY" format="YYYY" style="width: 100%" @change="kpiList"/>
        </template> -->
        <!--KPI-->
        <template #kpiSelect="{ model, field}">
          <a-tree-select
            v-model:value="model[field]"
            show-search
            style="width: 100%"
            :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
            placeholder="请选择KPI"
            allow-clear
            tree-default-expand-all
            :tree-data="infoTreeData"
            tree-node-filter-prop="kpiType"
            :fieldNames= "{children:'children', label:'kpiName', value: 'kpiCode' }"
          ></a-tree-select>
        </template>
      </BasicForm>
  </BasicModal>
</template>

<script lang="ts" setup>
    import {ref, computed, unref} from 'vue';
    import {BasicModal, useModalInner} from '/@/components/Modal';
    import {BasicForm, useForm} from '/@/components/Form/index';
    import {formSchema} from '../KpiPlatoOne.data';
    import {saveOrUpdate} from '../KpiPlatoOne.api';
    import { list } from '/@/views/dataCalculation/kpi/info/KpiInfo.api';
    import { useUserStore } from '/@/store/modules/user';
    // 用户信息
    const userStore = useUserStore();
    // Emits声明
    const emit = defineEmits(['register','success']);
    const isUpdate = ref(true);
    const isDetail = ref(false);
    //表单配置
    const [registerForm, {getFieldsValue, setProps,resetFields, setFieldsValue, validate, scrollToField }] = useForm({
        labelWidth: 150,
        schemas: formSchema,
        showActionButtonGroup: false,
        baseColProps: {span: 24}
    });
    const infoTreeData = ref([]);
    //表单赋值
    const [registerModal, {setModalProps, closeModal}] = useModalInner(async (data) => {
        //重置表单
        await resetFields();
        setModalProps({confirmLoading: false,showCancelBtn:!!data?.showFooter,showOkBtn:!!data?.showFooter});
        isUpdate.value = !!data?.isUpdate;
        isDetail.value = !!data?.showFooter;
        if (unref(isUpdate)) {
            // if(data.record.year){
            //   kpiList(data.record.year);
            // }
            //表单赋值
            await setFieldsValue({
                ...data.record,
            });
        }
        // 隐藏底部时禁用整个表单
       setProps({ disabled: !data?.showFooter })
       kpiList();
    });
    //设置标题
    const title = computed(() => (!unref(isUpdate) ? '新增' : !unref(isDetail) ? '详情' : '编辑'));
    //表单提交事件
    async function handleSubmit(v) {
        try {
            let values = await validate();
            if(!values.id){
              values.deptCode = userStore.departCode;
              values.factoryCode = userStore.factoryCode;
            }
            
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

    //KPI清单
    async function kpiList(){
      // await setFieldsValue({ parentCode: '' });
      infoTreeData.value = [];
      let res = await list({pageNo: 1,pageSize: 9999});
      if(res && res.records){
        // infoTreeData.value.push(...res.records);
        infoTreeData.value = res.records;
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