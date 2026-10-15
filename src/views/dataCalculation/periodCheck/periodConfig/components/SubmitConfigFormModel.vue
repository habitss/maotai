<template>
    <BasicModal @register="registerModal" v-bind="$attrs" :title="title" :width="width" @ok="handleSubmit"
        :cancelText="t('language.closeText')" :showOkBtn=!disabledRef>
        <BasicForm @register="registerForm" @submit="handleSubmit">
        </BasicForm>
    </BasicModal>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { useForm, BasicForm } from '/@/components/Form';
import { saveOrUpdateConfig } from '../ConfigList.api';

import { useMessage } from '/@/hooks/web/useMessage';
import { cloneDeep } from 'lodash-es';
import { useI18n } from '/@/hooks/web/useI18n';
import {
  addConfigFormSchemas
} from "@/views/dataCalculation/periodCheck/periodConfig/ConfigList.data";
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

const baseData = ref({
  apparatusId: '',
  apparatusName: '',
    factoryCode:'',
    deptCode:'',
    lineCode:''
})
const emit = defineEmits(['success']);

//注册modal
const [registerModal, { closeModal }] = useModalInner(async (data: any) => {
    disabledRef.value = false;
    //重置表单数据
    await resetFields();
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
        baseData.value.apparatusId = data.deviceData.id
        baseData.value.apparatusName = data.deviceData.apparatusName
        baseData.value.factoryCode = data.deviceData.factoryCode
        baseData.value.deptCode = data.deviceData.deptCode
        baseData.value.lineCode = data.deviceData.lineCode
        await setFieldsValue(baseData.value)
    } else {
        title.value = '编辑';
        let newData = cloneDeep(data.record);
        let apparatusName = {
          apparatusName:''
        };
        apparatusName.apparatusName = newData.apparatusIdVal;
        //赋值需修改记录的id
        recordId.value = newData.id;
        const mergedObj = Object.assign({}, apparatusName, newData);
        //设置表单值
        await setFieldsValue(mergedObj);
    }
});

//注册Message
const { createMessage: msg } = useMessage();

/**
* BasicForm绑定注册;
*/
const [registerForm, { resetFields, validate, setFieldsValue, updateSchema }] = useForm({
    //注册表单列
    schemas: addConfigFormSchemas,
    //回车提交
    autoSubmitOnEnter: true,
    showActionButtonGroup: false,
    disabled: disabledRef,
    //表格label宽度
    labelCol: { style: { width: '100px' } },
});


//表单提交事件
async function handleSubmit() {
    try {
        const values = await validate();
        //提交表单
        if (isAdd.value) {
            //调用后台新增接口
            values.apparatusId = baseData.value.apparatusId;
            values.factoryCode = baseData.value.factoryCode;
            values.deptCode = baseData.value.deptCode;
            values.lineCode = baseData.value.lineCode;
            await saveOrUpdateConfig(values, isAdd.value);
        } else {
            values.id = recordId.value;
            //调用后台修改接口
            await saveOrUpdateConfig(values, isAdd.value);
        }
        //关闭弹窗
        closeModal();
        //重置表单
        await resetFields();
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
