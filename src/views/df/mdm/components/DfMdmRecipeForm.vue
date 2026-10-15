<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" style="height: 80vh" :labelCol="labelCol" :wrapperCol="wrapperCol" name="DfMdmRecipeForm">
          <a-row>
            <a-col :span="12">
              <a-form-item label="系统唯一ID sys_unique_id" v-bind="validateInfos.sysUniqueId" id="DfMdmRecipeForm-sysUniqueId" name="sysUniqueId">
                <a-input v-model:value="formData.sysUniqueId" placeholder="请输入系统唯一ID sys_unique_id" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item
                label="唯一编码 sys_unique_code"
                v-bind="validateInfos.sysUniqueCode"
                id="DfMdmRecipeForm-sysUniqueCode"
                name="sysUniqueCode"
              >
                <a-input v-model:value="formData.sysUniqueCode" placeholder="请输入唯一编码 sys_unique_code" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="工厂名称 FAC_DESC" v-bind="validateInfos.facDesc" id="DfMdmRecipeForm-facDesc" name="facDesc">
                <a-input v-model:value="formData.facDesc" placeholder="请输入工厂名称 FAC_DESC" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="工厂编码 FAC_CODE" v-bind="validateInfos.facCode" id="DfMdmRecipeForm-facCode" name="facCode">
                <a-input v-model:value="formData.facCode" placeholder="请输入工厂编码 FAC_CODE" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="部门名称 DEPAR_DESC" v-bind="validateInfos.deparDesc" id="DfMdmRecipeForm-deparDesc" name="deparDesc">
                <a-input v-model:value="formData.deparDesc" placeholder="请输入部门名称 DEPAR_DESC" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="部门编码 DEPAR_CODE" v-bind="validateInfos.deparCode" id="DfMdmRecipeForm-deparCode" name="deparCode">
                <a-input v-model:value="formData.deparCode" placeholder="请输入部门编码 DEPAR_CODE" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="工序描述 PROCESS_DESC" v-bind="validateInfos.processDesc" id="DfMdmRecipeForm-processDesc" name="processDesc">
                <a-input v-model:value="formData.processDesc" placeholder="请输入工序描述 PROCESS_DESC" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="工序编码 PROCESS_COD" v-bind="validateInfos.processCod" id="DfMdmRecipeForm-processCod" name="processCod">
                <a-input v-model:value="formData.processCod" placeholder="请输入工序编码 PROCESS_COD" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="产线编码 LINE_CODE" v-bind="validateInfos.lineCode" id="DfMdmRecipeForm-lineCode" name="lineCode">
                <a-input v-model:value="formData.lineCode" placeholder="请输入产线编码 LINE_CODE" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="产线子编码 LINE_S_CODE" v-bind="validateInfos.lineSCode" id="DfMdmRecipeForm-lineSCode" name="lineSCode">
                <a-input v-model:value="formData.lineSCode" placeholder="请输入产线子编码 LINE_S_CODE" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item
                label="工厂配方名称 FAC_RECIPE_NAME"
                v-bind="validateInfos.facRecipeName"
                id="DfMdmRecipeForm-facRecipeName"
                name="facRecipeName"
              >
                <a-input v-model:value="formData.facRecipeName" placeholder="请输入工厂配方名称 FAC_RECIPE_NAME" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item
                label="工厂配方编码 FAC_RECIPE_CODE"
                v-bind="validateInfos.facRecipeCode"
                id="DfMdmRecipeForm-facRecipeCode"
                name="facRecipeCode"
              >
                <a-input v-model:value="formData.facRecipeCode" placeholder="请输入工厂配方编码 FAC_RECIPE_CODE" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item
                label="区域配方中文名 ZONE_RECIPE_CN_NAME"
                v-bind="validateInfos.zoneRecipeCnName"
                id="DfMdmRecipeForm-zoneRecipeCnName"
                name="zoneRecipeCnName"
              >
                <a-input v-model:value="formData.zoneRecipeCnName" placeholder="请输入区域配方中文名 ZONE_RECIPE_CN_NAME" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item
                label="区域配方编码 ZONE_RECIPE_CODE"
                v-bind="validateInfos.zoneRecipeCode"
                id="DfMdmRecipeForm-zoneRecipeCode"
                name="zoneRecipeCode"
              >
                <a-input v-model:value="formData.zoneRecipeCode" placeholder="请输入区域配方编码 ZONE_RECIPE_CODE" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="配方号 RECIPE_NUM" v-bind="validateInfos.recipeNum" id="DfMdmRecipeForm-recipeNum" name="recipeNum">
                <a-input v-model:value="formData.recipeNum" placeholder="请输入配方号 RECIPE_NUM" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="配方类型代码 RECIPE_TYPE" v-bind="validateInfos.recipeType" id="DfMdmRecipeForm-recipeType" name="recipeType">
                <a-input v-model:value="formData.recipeType" placeholder="请输入配方类型代码 RECIPE_TYPE" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item
                label="子品牌中文名称 SUB_BRAND_OG_CN_NAME"
                v-bind="validateInfos.subBrandOgCnName"
                id="DfMdmRecipeForm-subBrandOgCnName"
                name="subBrandOgCnName"
              >
                <a-input v-model:value="formData.subBrandOgCnName" placeholder="请输入子品牌中文名称 SUB_BRAND_OG_CN_NAME" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item
                label="子品牌原始编码 SUB_BRAND_OG_CODE"
                v-bind="validateInfos.subBrandOgCode"
                id="DfMdmRecipeForm-subBrandOgCode"
                name="subBrandOgCode"
              >
                <a-input v-model:value="formData.subBrandOgCode" placeholder="请输入子品牌原始编码 SUB_BRAND_OG_CODE" />
              </a-form-item>
            </a-col>
            <!-- <a-col :span="12">
              <a-form-item
                label="配方类型描述 RECIPE_TYPE_DESC"
                v-bind="validateInfos.recipeTypeDesc"
                id="DfMdmRecipeForm-recipeTypeDesc"
                name="recipeTypeDesc"
              >
                <a-input v-model:value="formData.recipeTypeDesc" placeholder="请输入配方类型描述 RECIPE_TYPE_DESC" />
              </a-form-item>
            </a-col> -->
            <a-col :span="12">
              <a-form-item label="设备描述 EQUIP_DESC" v-bind="validateInfos.equipDesc" id="DfMdmRecipeForm-equipDesc" name="equipDesc">
                <a-input v-model:value="formData.equipDesc" placeholder="请输入设备描述 EQUIP_DESC" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="设备代码 EQUIP_CODE" v-bind="validateInfos.equipCode" id="DfMdmRecipeForm-equipCode" name="equipCode">
                <a-input v-model:value="formData.equipCode" placeholder="请输入设备代码 EQUIP_CODE" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="生效时间 EFFE_DATE" v-bind="validateInfos.effeDate" id="DfMdmRecipeForm-effeDate" name="effeDate">
                <a-date-picker
                  placeholder="请选择生效时间 EFFE_DATE"
                  v-model:value="formData.effeDate"
                  showTime
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 100%"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="失效时间 EXPI_DATE" v-bind="validateInfos.expiDate" id="DfMdmRecipeForm-expiDate" name="expiDate">
                <a-date-picker
                  placeholder="请选择失效时间 EXPI_DATE"
                  v-model:value="formData.expiDate"
                  showTime
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 100%"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="业务状态 BUSINE_STATUS" v-bind="validateInfos.busineStatus" id="DfMdmRecipeForm-busineStatus" name="busineStatus">
                <a-input v-model:value="formData.busineStatus" placeholder="请输入业务状态 BUSINE_STATUS" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="是否启用" v-bind="validateInfos.enableFlag" id="DfMdmRecipeForm-enableFlag" name="enableFlag">
                <a-input v-model:value="formData.enableFlag" placeholder="请输入是否启用" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="创建人" v-bind="validateInfos.createBy" id="DfMdmRecipeForm-createBy" name="createBy">
                <a-input v-model:value="formData.createBy" placeholder="请输入createBy" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="创建时间" v-bind="validateInfos.createTime" id="DfMdmRecipeForm-createTime" name="createTime">
                <a-date-picker
                  placeholder="请选择"
                  v-model:value="formData.createTime"
                  showTime
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 100%"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="更新人" v-bind="validateInfos.updateBy" id="DfMdmRecipeForm-updateBy" name="updateBy">
                <a-input v-model:value="formData.updateBy" placeholder="请输入updateBy" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="更新时间" v-bind="validateInfos.updateTime" id="DfMdmRecipeForm-updateTime" name="updateTime">
                <a-date-picker
                  placeholder="请选择"
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
  import { saveOrUpdate } from '../DfMdmRecipe.api';
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
    lineCode: '',
    facCode: '',
    facDesc: '',
    deparCode: '',
    deparDesc: '',
    processCod: '',
    processDesc: '',
    lineSCode: '',
    recipeNum: '',
    recipeType: '',
    recipeTypeDesc: '',
    facRecipeName: '',
    facRecipeCode: '',
    zoneRecipeCode: '',
    zoneRecipeCnName: '',
    subBrandOgCode: '',
    subBrandOgCnName: '',
    equipCode: '',
    equipDesc: '',
    effeDate: '',
    expiDate: '',
    busineStatus: '',
    sysUniqueId: '',
    sysUniqueCode: '',
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
    lineCode: [{ required: true, message: '请输入产线编码 LINE_CODE!' }],
    facCode: [{ required: true, message: '请输入工厂编码 FAC_CODE!' }],
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
