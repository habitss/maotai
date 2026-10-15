<template>
    <BasicModal @register="registerModal" v-bind="$attrs" :title="title" :width="width" @ok="handleSubmit"
        :cancelText="t('language.closeText')" :showOkBtn=!disabledRef>
        <BasicForm @register="registerForm" @submit="handleSubmit">
        <!--  deptCode部门插槽    -->
        <template #deptCode="{ model, field }">
            <kt-depart-select
            v-model:value="model[field]"
            select-type="depart"
            :placeholder="t('language.selectDepartment')"
            @change="handleDeptChange"
            :disabled = disabledRef
            />
        </template>
        <!--  lineCode产线插槽    -->
        <template #lineCode="{ model, field }">
            <kt-depart-select
            v-model:value="model[field]"
            select-type="line"
            :placeholder="t('language.selectProductLine')"
            :parentKey="model.deptCode"
            :disabled = disabledRef
            />
        </template>
        </BasicForm>
    </BasicModal>
</template>
  
<script lang="ts" setup>
import { ref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { useForm, BasicForm } from '/@/components/Form';
import { saveOrUpdate } from '../copList.api';
import { addFormSchemas } from '../copList.data';
import { useMessage } from '/@/hooks/web/useMessage';
import { cloneDeep } from 'lodash-es';
import { useI18n } from '/@/hooks/web/useI18n';
import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
const { t } = useI18n();

// 判断新增或编辑或详情标识
const isAdd = ref();
const isDetail = ref();

// 禁用
const disabledRef = ref(false);

//需修改记录的id
const recordId = ref();

const title = ref<string>('');
const width = ref<number>(800);

const emit = defineEmits(['success']);

//注册modal
const [registerModal, { closeModal }] = useModalInner(async (data: any) => {
    disabledRef.value = false;
    //重置表单数据
    await resetFields();
    console.log('data111', data)
    //设置表单数据
    setFieldsValue(data.record ?? {});
    //赋值操作标识
    isAdd.value = data.isAdd;
    isDetail.value = data.isDetail

    //判断执行新增、编辑、详情操作
    if (data.isDetail) {
        title.value = '详情';
        disabledRef.value = true;
    }
    if (data.isAdd) {
        title.value = '新增';
    } else {
        title.value = '编辑';
        data.record.active = String(data.record.active)
        let newData = cloneDeep(data.record);
        //赋值需修改记录的id
        recordId.value = newData.id;
        //设置表单值
        setFieldsValue(newData);
    }
});

//注册Message
const { createMessage: msg } = useMessage();

/**
* BasicForm绑定注册;
*/
const [registerForm, { resetFields, validate, setFieldsValue, updateSchema }] = useForm({
    //注册表单列
    schemas: addFormSchemas,
    //回车提交
    autoSubmitOnEnter: true,
    showActionButtonGroup: false,
    disabled: disabledRef,
    labelWidth: 140,
    baseColProps: { span: 24 }
    //表格label宽度
    // labelCol: { style: { width: '100px' } },
});


//部门改变事件
async function handleDeptChange(value) {
    //清空产线
    await setFieldsValue({
        lineCode: '',
    });
}

//表单提交事件
async function handleSubmit() {
    try {
        const values = await validate();
        //设置工厂代码
        values.factoryCode = values.deptCode.substr(0, 9);
        //提交表单
        if (isAdd.value) {
            //调用后台新增接口
            await saveOrUpdate(values, isAdd.value);
        } else {
            values.id = recordId.value;
            //调用后台修改接口
            await saveOrUpdate(values, isAdd.value);
        }
        //关闭弹窗
        closeModal();
        //重置表单
        resetFields();
        //刷新外部表格
        emit('success');
    } catch (error) {
        msg.error({ content: '保存失败', key: 'saving' });
    }
}
</script>
  
<style>
/**隐藏样式-modal确定按钮 */
.jee-hidden {
    display: none !important;
}
</style>