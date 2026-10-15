<template>
    <BasicModal @register="registerModal" v-bind="$attrs" :title="title" :width="width" @ok="handleSubmit"
        :cancelText="t('language.closeText')" :showOkBtn=!disabledRef>
        <BasicForm @register="registerForm" @submit="handleSubmit">
        </BasicForm>
        <template #footer>
            <a-button @click="closeModal">关闭</a-button>
            <a-button type="primary" @click="handleSubmit(0)">保存</a-button>
            <a-button type="primary" @click="handleSubmit(1)">保存并同步到其他清洗点</a-button>
        </template>
    </BasicModal>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { useForm, BasicForm } from '/@/components/Form';
import { saveOrUpdate } from './copPointConfig.api';
import {addConfigFormSchemas} from './copPonin.data';
import { useMessage } from '/@/hooks/web/useMessage';
import { cloneDeep } from 'lodash-es';
import { useI18n } from '/@/hooks/web/useI18n';
const { t } = useI18n();

// 判断新增或编辑或详情标识
const isAdd = ref();
const isDetail = ref();

// 禁用
const disabledRef = ref(false);

//需修改记录的id
const recordId = ref();

const title = ref<string>('');
const width = ref<number>(400);

const baseData = ref({
    copDeviceId: '',
    copPointId: '',
    copPointIdVal: '',
    copPointName: '',
    copDeviceName: '',
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
    baseData.value.copPointId = data.deviceData.id
    baseData.value.copDeviceId = data.deviceData.copDeviceId
    baseData.value.copDeviceName = data.deviceData.copDeviceIdVal
    baseData.value.factoryCode = data.deviceData.factoryCode
    baseData.value.deptCode = data.deviceData.deptCode
    baseData.value.lineCode = data.deviceData.lineCode
    if (data.isAdd) {
        title.value = '新增';
        await setFieldsValue(baseData.value)
    } else {
        title.value = '编辑';
        let newData = cloneDeep(data.record);
        //赋值需修改记录的id
        recordId.value = newData.id;
        //设置表单值
        await setFieldsValue(newData);
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
async function handleSubmit(isSync) {
    try {
        const values = await validate();
        values.isSync = isSync;
        //提交表单
        if (!isAdd.value) {
            values.id = recordId.value;
        } 
        values.copPointId = baseData.value.copPointId
        values.copDeviceId = baseData.value.copDeviceId;
        values.factoryCode = baseData.value.factoryCode;
        values.deptCode = baseData.value.deptCode;
        values.lineCode = baseData.value.lineCode;
        //调用后台修改接口
        await saveOrUpdate(values, isAdd.value);
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
<style scoped>
/**隐藏样式-modal确定按钮 */
.jee-hidden {
    display: none !important;
}
:deep(.ant-input-number) {
    width: 100%;
}
:deep(.ant-picker) {
    width: 100%;
}

/** 下拉框默认文字颜色 */
:deep(.ant-select-selection-placeholder) {
    color: #BEBEBE !important;
}
</style>
