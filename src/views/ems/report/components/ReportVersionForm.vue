<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="ReportVersionForm">
          <div><span class="tit">版本详情</span></div>
          <div class="flex-row">
            <a-row style="width: 66%">
              <a-col :span="24" style="margin-top: 10px">
                <a-form-item label="名称" v-bind="validateInfos.verName" id="ReportVersionForm-verName" name="verName">
                  <a-input v-model:value="formData.verName" placeholder="请输入版本名称" disabled="disabled" />
                </a-form-item>
              </a-col>
              <a-col :span="24">
                <a-form-item label="备注" v-bind="validateInfos.verDesc" id="ReportVersionForm-verDesc" name="verDesc">
                  <a-textarea v-model:value="formData.verDesc" placeholder="请输入版本备注" :rows="3" disabled="disabled" />
                </a-form-item>
              </a-col>
              <a-col :span="24">
                <a-form-item label="生效日期" v-bind="validateInfos.effectSdate" id="ReportVersionForm-effectSdate" name="effectSdate">
                  <a-input v-model:value="formData.effectSdate" disabled="disabled" />
                </a-form-item>
              </a-col>
              <a-col :span="24">
                <a-form-item label="失效日期" v-bind="validateInfos.effectEdate" id="ReportVersionForm-effectEdate">
                  <a-input v-model:value="formData.effectEdate" disabled="disabled" />
                </a-form-item>
              </a-col>
              <a-col :span="24">
                <a-form-item label="最后修改人" v-bind="validateInfos.updateBy" id="ReportVersionForm-updateBy">
                  <a-input v-model:value="formData.updateBy" disabled="disabled" />
                </a-form-item>
              </a-col>
              <a-col :span="24">
                <a-form-item label="最后修改时间" v-bind="validateInfos.updateTime" id="ReportVersionForm-updateTime">
                  <a-input v-model:value="formData.updateTime" disabled="disabled" />
                </a-form-item>
              </a-col>
            </a-row>
            <div style="padding-top: 10px" v-if="formData.id">
              <a-button type="primary" v-auth="'ems:ems_report_version:update'" @click="updateReportVersion" style="margin-left: -10px"
                >更新</a-button
              >
              <a-button type="success" v-auth="'ems:ems_report_version:downBlob'" @click="downloadReportVersionExcel" style="margin-left: 15px"
                >下载</a-button
              >
              <a-button type="default" v-auth="'ems:ems_report_version:delete'" @click="deleteReportVersion" style="margin-left: 15px">删除</a-button>
            </div>
          </div>
        </a-form>
      </template>
    </JFormContainer>
  </a-spin>
</template>

<script lang="ts" setup>
  import { ref, reactive, defineExpose, nextTick, defineProps, computed } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getValueType } from '/@/utils';
  import { saveOrUpdate, getBlob } from '../ReportVersion.api';
  import { Form } from 'ant-design-vue';
  import JFormContainer from '/@/components/Form/src/container/JFormContainer.vue';
  import FaverSaver from 'file-saver';
  const emit = defineEmits(['register', 'ok', 'update', 'delete']);
  const { createMessage } = useMessage();

  const props = defineProps({
    formDisabled: { type: Boolean, default: false },
    formBpm: { type: Boolean, default: true },
  });
  const formRef = ref();
  const useForm = Form.useForm;
  const formData = reactive<Record<string, any>>({
    id: '',
    repid: 0,
    deptCode: '',
    verName: '',
    verDesc: '',
    effectSdate: '',
    effectEdate: '',
    updateBy: '',
    updateTime: '',
  });
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 18 } });
  const confirmLoading = ref<boolean>(false);
  //表单验证
  const validatorRules = reactive({
    verName: [{ required: false, message: '请输入版本名称!' }],
    verDesc: [{ required: false, message: '请输入版本描述!' }],
    effectSdate: [{ required: false, message: '请输入开始日期!' }],
    updateBy: [{ required: false, message: '请输入更新人!' }],
    updateTime: [{ required: false, message: '请输入更新时间!' }],
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

  //下载报表版本原文件
  function downloadReportVersionExcel() {
    confirmLoading.value = true;
    getBlob(formData.id).then((response) => {
      FaverSaver.saveAs(response.data, formData.verName + '.xlsx');
      confirmLoading.value = false;
    });
  }

  //更新指定的报表版本
  function updateReportVersion() {
    emit('update', formData);
  }

  //删除指定的版本
  function deleteReportVersion() {
    emit('delete', formData.id);
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
  .tit {
    font-size: 16px;
    font-weight: bold;
  }
  .ant-input[disabled] {
    color: rgba(51, 51, 51, 0.9);
    background-color: rgba(51, 51, 51, 0.03);
  }
</style>
