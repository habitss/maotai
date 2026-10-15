<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="AlarmApiAppForm">
          <a-row>
            <a-col :span="24">
              <a-form-item label="应用名称" v-bind="validateInfos.appName" id="AlarmApiAppForm-appName" name="appName">
                <a-input v-model:value="formData.appName" placeholder="请输入应用名称" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="应用标识" v-bind="validateInfos.appId" id="AlarmApiAppForm-appId" name="appId">
                <a-input v-model:value="formData.appId" placeholder="请输入应用标识，未填写时系统自动生成" allow-clear :disabled="formData.id" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="应用过期时间" v-bind="validateInfos.expireTime" id="AlarmApiAppForm-expireTime" name="expireTime">
                <a-date-picker
                  placeholder="请选择应用过期时间"
                  v-model:value="formData.expireTime"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="IP白名单" v-bind="validateInfos.ipWhitelist" id="AlarmApiAppForm-ipWhitelist" name="ipWhitelist">
                <a-textarea v-model:value="formData.ipWhitelist" :rows="4" placeholder="请输入IP白名单，多个IP用逗号分隔" />
              </a-form-item>
            </a-col>
            <a-col :span="24" v-if="formData.id">
              <a-form-item label="旧密钥" v-bind="validateInfos.appSecretOld" id="AlarmApiAppForm-appSecretOld" name="appSecretOld">
                <a-input disabled v-model:value="formData.appSecretOld" placeholder="请输入旧密钥" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24" v-if="formData.id">
              <a-form-item label="密钥轮换时间" v-bind="validateInfos.secretRotateTime" id="AlarmApiAppForm-secretRotateTime" name="secretRotateTime">
                <a-date-picker
                  placeholder="请选择密钥轮换时间"
                  v-model:value="formData.secretRotateTime"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                  disabled
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24" v-if="formData.id">
              <a-form-item label="状态" v-bind="validateInfos.status" id="AlarmApiAppForm-status" name="status">
                <j-dict-select-tag
                  v-model:value="formData.status"
                  dictCode="mes3_mdm_dict WHERE group_code = 'EMS_StateYN',item_key,item_val"
                  placeholder="请选择状态"
                  allow-clear
                />
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
      </template>
    </JFormContainer>
    <!-- 应用密钥成功弹窗 -->
    <AppSecretSuccessModal v-model:open="secretVisible" :app-id="currentAppId" :app-secret="currentAppSecret" @ok="emit('ok')" />
  </a-spin>
</template>

<script lang="ts" setup>
  import { ref, reactive, defineExpose, nextTick, defineProps, computed } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { Form } from 'ant-design-vue';
  import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';
  import { getValueType } from '/@/utils';
  import { saveOrUpdate } from '../AlarmApiApp.api';
  import JFormContainer from '/@/components/Form/src/container/JFormContainer.vue';
  import AppSecretSuccessModal from './AppSecretSuccessModal.vue';
  const props = defineProps({
    formDisabled: { type: Boolean, default: false },
    formBpm: { type: Boolean, default: true },
  });
  const formRef = ref();
  const useForm = Form.useForm;
  const emit = defineEmits(['register', 'ok']);
  const formData = reactive<Record<string, any>>({
    id: '',
    appId: '',
    appName: '',
    appSecret: '',
    status: undefined,
    expireTime: '',
    ipWhitelist: '',
    appSecretOld: '',
    secretRotateTime: '',
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);

  const secretVisible = ref(false);
  const currentAppId = ref('');
  const currentAppSecret = ref('');

  //表单验证
  const validatorRules = reactive({
    appName: [{ required: true, message: '请输入应用名称!' }],
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
      if (formData.status || formData.status === 0) {
        formData.status = String(formData.status);
      }
      if (formData.appSecretOld) {
        formData.appSecretOld = '***************************';
      }
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
          // 新增成功时，显示包含密钥的模态框
          if (!isUpdate.value && res.result && res.result.appSecret) {
            currentAppId.value = res.result.appId;
            currentAppSecret.value = res.result.appSecret;
            secretVisible.value = true;
          } else {
            createMessage.success(res.message);
            emit('ok');
          }
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
