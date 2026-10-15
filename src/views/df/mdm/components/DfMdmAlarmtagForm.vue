<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" style="height: 60vh" :labelCol="labelCol" :wrapperCol="wrapperCol" name="DfMdmAlarmtagForm">
          <a-row>
            <a-col :span="12">
              <a-form-item label="系统唯一ID" v-bind="validateInfos.sysUniqueId" id="DfMdmAlarmtagForm-sysUniqueId" name="sysUniqueId">
                <a-input v-model:value="formData.sysUniqueId" placeholder="请输入系统唯一ID" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="系统唯一编码" v-bind="validateInfos.sysUniqueCode" id="DfMdmAlarmtagForm-sysUniqueCode" name="sysUniqueCode">
                <a-input v-model:value="formData.sysUniqueCode" placeholder="请输入系统唯一编码" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="业务唯一标识 UNIQ_ID" v-bind="validateInfos.uniqId" id="DfMdmAlarmtagForm-uniqId" name="uniqId">
                <a-input v-model:value="formData.uniqId" placeholder="请输入业务唯一标识 UNIQ_ID" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item
                label="TAG功能位置编码 TAG_FUNLOC_CODE"
                v-bind="validateInfos.tagFunlocCode"
                id="DfMdmAlarmtagForm-tagFunlocCode"
                name="tagFunlocCode"
              >
                <a-input v-model:value="formData.tagFunlocCode" placeholder="请输入TAG功能位置编码 TAG_FUNLOC_CODE" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item
                label="TAG功能位置层级描述 TAG_FUNLOC_CODE2"
                v-bind="validateInfos.tagFunlocCode2"
                id="DfMdmAlarmtagForm-tagFunlocCode2"
                name="tagFunlocCode2"
              >
                <a-input v-model:value="formData.tagFunlocCode2" placeholder="请输入TAG功能位置层级描述 TAG_FUNLOC_CODE2" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item
                label="功能位置描述 V_FUNLOC_DESC"
                v-bind="validateInfos.vfunlocDesc"
                id="DfMdmAlarmtagForm-vfunlocDesc"
                name="vfunlocDesc"
              >
                <a-input v-model:value="formData.vfunlocDesc" placeholder="请输入功能位置描述 V_FUNLOC_DESC" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item
                label="功能位置描述2 V_FUNLOC_DESC2"
                v-bind="validateInfos.vfunlocDesc2"
                id="DfMdmAlarmtagForm-vfunlocDesc2"
                name="vfunlocDesc2"
              >
                <a-input v-model:value="formData.vfunlocDesc2" placeholder="请输入功能位置描述2 V_FUNLOC_DESC2" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="IOT属性编码 IOT_ATTR_CODE" v-bind="validateInfos.iotAttrCode" id="DfMdmAlarmtagForm-iotAttrCode" name="iotAttrCode">
                <a-input v-model:value="formData.iotAttrCode" placeholder="请输入IOT属性编码 IOT_ATTR_CODE" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item
                label="IOT属性显示名称 IOT_ATTR_DISP_NAME"
                v-bind="validateInfos.iotAttrDispName"
                id="DfMdmAlarmtagForm-iotAttrDispName"
                name="iotAttrDispName"
              >
                <a-input v-model:value="formData.iotAttrDispName" placeholder="请输入IOT属性显示名称 IOT_ATTR_DISP_NAME" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="报警位 TAG_BIT" v-bind="validateInfos.tagBit" id="DfMdmAlarmtagForm-tagBit" name="tagBit">
                <a-input v-model:value="formData.tagBit" placeholder="请输入报警位 TAG_BIT" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="报警位描述 TAG_BIT_DESC" v-bind="validateInfos.tagBitDesc" id="DfMdmAlarmtagForm-tagBitDesc" name="tagBitDesc">
                <a-input v-model:value="formData.tagBitDesc" placeholder="请输入报警位描述 TAG_BIT_DESC" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="报警位描述2 TAG_BIT_DESC2" v-bind="validateInfos.tagBitDesc2" id="DfMdmAlarmtagForm-tagBitDesc2" name="tagBitDesc2">
                <a-input v-model:value="formData.tagBitDesc2" placeholder="请输入报警位描述2 TAG_BIT_DESC2" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="是否启用 Y/N" v-bind="validateInfos.enableFlag" id="DfMdmAlarmtagForm-enableFlag" name="enableFlag">
                <a-input v-model:value="formData.enableFlag" placeholder="请输入是否启用 Y/N" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="创建人" v-bind="validateInfos.createBy" id="DfMdmAlarmtagForm-createBy" name="createBy">
                <a-input v-model:value="formData.createBy" placeholder="请输入创建人" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="创建时间" v-bind="validateInfos.createTime" id="DfMdmAlarmtagForm-createTime" name="createTime">
                <a-date-picker
                  placeholder="请选择创建时间"
                  v-model:value="formData.createTime"
                  showTime
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 100%"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="更新人" v-bind="validateInfos.updateBy" id="DfMdmAlarmtagForm-updateBy" name="updateBy">
                <a-input v-model:value="formData.updateBy" placeholder="请输入更新人" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="更新时间" v-bind="validateInfos.updateTime" id="DfMdmAlarmtagForm-updateTime" name="updateTime">
                <a-date-picker
                  placeholder="请选择更新时间"
                  v-model:value="formData.updateTime"
                  showTime
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 100%"
                  allow-clear
                />
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
      </template>
    </JFormContainer>
  </a-spin>
</template>

<script lang="ts" setup>
  import { ref, reactive, defineExpose, nextTick, defineProps, computed } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getValueType } from '/@/utils';
  import { saveOrUpdate } from '../DfMdmAlarmtag.api';
  import { Form } from 'ant-design-vue';
  import JFormContainer from '/@/components/Form/src/container/JFormContainer.vue';
  const props = defineProps({
    formDisabled: { type: Boolean, default: false },
    formBpm: { type: Boolean, default: true },
  });
  const formRef = ref();
  const useForm = Form.useForm;
  const emit = defineEmits(['register', 'ok']);
  const formData = reactive<Record<string, any>>({
    id: '',
    sysUniqueId: '',
    sysUniqueCode: '',
    uniqId: '',
    tagFunlocCode: '',
    tagFunlocCode2: '',
    vfunlocDesc: '',
    vfunlocDesc2: '',
    iotAttrCode: '',
    iotAttrDispName: '',
    tagBit: '',
    tagBitDesc: '',
    tagBitDesc2: '',
    enableFlag: '',
    createBy: '',
    createTime: '',
    updateBy: '',
    updateTime: '',
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 11 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 12 } });
  const confirmLoading = ref<boolean>(false);
  //表单验证
  const validatorRules = reactive({
    sysUniqueId: [{ required: true, message: '请输入MDM系统唯一ID!' }],
    sysUniqueCode: [{ required: true, message: '请输入MDM系统唯一编码!' }],
    uniqId: [{ required: true, message: '请输入业务唯一标识 UNIQ_ID!' }],
  });
  const { resetFields, validate, validateInfos } = useForm(formData, validatorRules, { immediate: false });

  // 表单禁用
  const disabled = computed(() => {
    if (props.formBpm === true) {
      return false;
    }
    return props.formDisabled;
  });

  /**
   * 新增
   */
  function add() {
    edit({});
  }

  /**
   * 编辑
   */
  function edit(record) {
    nextTick(() => {
      resetFields();
      const tmpData = {};
      Object.keys(formData).forEach((key) => {
        if (record.hasOwnProperty(key)) {
          tmpData[key] = record[key];
        }
      });
      //赋值
      Object.assign(formData, tmpData);
    });
  }

  /**
   * 提交数据
   */
  async function submitForm() {
    try {
      // 触发表单验证
      await validate();
    } catch (err) {
      const error = err as { errorFields?: Array<{ name: string }> };
      if (error.errorFields) {
        const firstField = error.errorFields[0];
        if (firstField) {
          formRef.value.scrollToField(firstField.name, { behavior: 'smooth', block: 'center' });
        }
      }
      return Promise.reject(error.errorFields);
    }
    confirmLoading.value = true;
    const isUpdate = ref<boolean>(false);
    //时间格式化
    let model = formData;
    if (model.id) {
      isUpdate.value = true;
    }
    //循环数据
    for (let data in model) {
      //如果该数据是数组并且是字符串类型
      if (model[data] instanceof Array) {
        let valueType = getValueType(formRef.value.getProps, data);
        //如果是字符串类型的需要变成以逗号分割的字符串
        if (valueType === 'string') {
          model[data] = model[data].join(',');
        }
      }
    }
    await saveOrUpdate(model, isUpdate.value)
      .then((res) => {
        if (res.success) {
          createMessage.success(res.message);
          emit('ok');
        } else {
          createMessage.warning(res.message);
        }
      })
      .finally(() => {
        confirmLoading.value = false;
      });
  }

  defineExpose({
    add,
    edit,
    submitForm,
  });
</script>

<style lang="less" scoped>
  .antd-modal-form {
    padding: 14px;
  }
</style>
