<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" style="height: 80vh" :labelCol="labelCol" :wrapperCol="wrapperCol" name="DfMdmIndicatorForm">
          <a-row>
            <a-col :span="12">
              <a-form-item label="系统唯一ID sys_unique_id" v-bind="validateInfos.sysUniqueId" id="DfMdmIndicatorForm-sysUniqueId" name="sysUniqueId">
                <a-input v-model:value="formData.sysUniqueId" placeholder="请输入系统唯一ID sys_unique_id" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item
                label="唯一编码 sys_unique_code"
                v-bind="validateInfos.sysUniqueCode"
                id="DfMdmIndicatorForm-sysUniqueCode"
                name="sysUniqueCode"
              >
                <a-input v-model:value="formData.sysUniqueCode" placeholder="请输入唯一编码 sys_unique_code" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="工厂名称 FAC_DESC" v-bind="validateInfos.facDesc" id="DfMdmIndicatorForm-facDesc" name="facDesc">
                <a-input v-model:value="formData.facDesc" placeholder="请输入工厂名称 FAC_DESC" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="工厂编码 FAC_CODE" v-bind="validateInfos.facCode" id="DfMdmIndicatorForm-facCode" name="facCode">
                <a-input v-model:value="formData.facCode" placeholder="请输入工厂编码 FAC_CODE" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="部门名称 DEPAR_DESC" v-bind="validateInfos.deparDesc" id="DfMdmIndicatorForm-deparDesc" name="deparDesc">
                <a-input v-model:value="formData.deparDesc" placeholder="请输入部门名称 DEPAR_DESC" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="部门编码 DEPAR_CODE" v-bind="validateInfos.deparCode" id="DfMdmIndicatorForm-deparCode" name="deparCode">
                <a-input v-model:value="formData.deparCode" placeholder="请输入部门编码 DEPAR_CODE" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="工序描述 PROCESS_DESC" v-bind="validateInfos.processDesc" id="DfMdmIndicatorForm-processDesc" name="processDesc">
                <a-input v-model:value="formData.processDesc" placeholder="请输入工序描述 PROCESS_DESC" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="工序编码 PROCESS_CODE" v-bind="validateInfos.processCode" id="DfMdmIndicatorForm-processCode" name="processCode">
                <a-input v-model:value="formData.processCode" placeholder="请输入工序编码 PROCESS_CODE" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="产线描述 LINE_DESC" v-bind="validateInfos.lineDesc" id="DfMdmIndicatorForm-lineDesc" name="lineDesc">
                <a-input v-model:value="formData.lineDesc" placeholder="请输入产线描述 LINE_DESC" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="产线编码 LINE_CODE" v-bind="validateInfos.lineCode" id="DfMdmIndicatorForm-lineCode" name="lineCode">
                <a-input v-model:value="formData.lineCode" placeholder="请输入产线编码 LINE_CODE" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="功能位置描述 FUNLOC_DESC" v-bind="validateInfos.funlocDesc" id="DfMdmIndicatorForm-funlocDesc" name="funlocDesc">
                <a-input v-model:value="formData.funlocDesc" placeholder="请输入功能位置描述 FUNLOC_DESC" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="功能位置编码 FUNLOC_CODE" v-bind="validateInfos.funlocCode" id="DfMdmIndicatorForm-funlocCode" name="funlocCode">
                <a-input v-model:value="formData.funlocCode" placeholder="请输入功能位置编码 FUNLOC_CODE" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="业务区域 IND_BUSI_AREA" v-bind="validateInfos.indBusiArea" id="DfMdmIndicatorForm-indBusiArea" name="indBusiArea">
                <a-input v-model:value="formData.indBusiArea" placeholder="请输入业务区域 IND_BUSI_AREA" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item
                label="中心管理部门 IND_CEN_MAN_DEPT"
                v-bind="validateInfos.indCenManDept"
                id="DfMdmIndicatorForm-indCenManDept"
                name="indCenManDept"
              >
                <a-input v-model:value="formData.indCenManDept" placeholder="请输入中心管理部门 IND_CEN_MAN_DEPT" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="指标名称 IND_NAME" v-bind="validateInfos.indName" id="DfMdmIndicatorForm-indName" name="indName">
                <a-input v-model:value="formData.indName" placeholder="请输入指标名称 IND_NAME" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item
                label="指标标准编码 IND_STAND_CODE"
                v-bind="validateInfos.indStandCode"
                id="DfMdmIndicatorForm-indStandCode"
                name="indStandCode"
              >
                <a-input v-model:value="formData.indStandCode" placeholder="请输入指标标准编码 IND_STAND_CODE" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item
                label="指标标准描述 IND_STAND_DESC"
                v-bind="validateInfos.indStandDesc"
                id="DfMdmIndicatorForm-indStandDesc"
                name="indStandDesc"
              >
                <a-input v-model:value="formData.indStandDesc" placeholder="请输入指标标准描述 IND_STAND_DESC" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="指标分类 T0 IND_FRA_T0" v-bind="validateInfos.indFraT0" id="DfMdmIndicatorForm-indFraT0" name="indFraT0">
                <a-input v-model:value="formData.indFraT0" placeholder="请输入指标分类 T0 IND_FRA_T0" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="指标分类 T1 IND_FRA_T1" v-bind="validateInfos.indFraT1" id="DfMdmIndicatorForm-indFraT1" name="indFraT1">
                <a-input v-model:value="formData.indFraT1" placeholder="请输入指标分类 T1 IND_FRA_T1" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="指标分类 T2 IND_FRA_T2" v-bind="validateInfos.indFraT2" id="DfMdmIndicatorForm-indFraT2" name="indFraT2">
                <a-input v-model:value="formData.indFraT2" placeholder="请输入指标分类 T2 IND_FRA_T2" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="指标分类 T3 IND_FRA_T3" v-bind="validateInfos.indFraT3" id="DfMdmIndicatorForm-indFraT3" name="indFraT3">
                <a-input v-model:value="formData.indFraT3" placeholder="请输入指标分类 T3 IND_FRA_T3" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="指标分类 T4 IND_FRA_T4" v-bind="validateInfos.indFraT4" id="DfMdmIndicatorForm-indFraT4" name="indFraT4">
                <a-input v-model:value="formData.indFraT4" placeholder="请输入指标分类 T4 IND_FRA_T4" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="指标分类 T5 IND_FRA_T5" v-bind="validateInfos.indFraT5" id="DfMdmIndicatorForm-indFraT5" name="indFraT5">
                <a-input v-model:value="formData.indFraT5" placeholder="请输入指标分类 T5 IND_FRA_T5" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="单位 UNIT" v-bind="validateInfos.unit" id="DfMdmIndicatorForm-unit" name="unit">
                <a-input v-model:value="formData.unit" placeholder="请输入单位 UNIT" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="单位中文 UNIT_CN" v-bind="validateInfos.unitCn" id="DfMdmIndicatorForm-unitCn" name="unitCn">
                <a-input v-model:value="formData.unitCn" placeholder="请输入单位中文 UNIT_CN" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="主设备编码 EQUIP_CODE" v-bind="validateInfos.equipCode" id="DfMdmIndicatorForm-equipCode" name="equipCode">
                <a-input v-model:value="formData.equipCode" placeholder="请输入主设备编码 EQUIP_CODE" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="主设备描述 EQUIP_DESC" v-bind="validateInfos.equipDesc" id="DfMdmIndicatorForm-equipDesc" name="equipDesc">
                <a-input v-model:value="formData.equipDesc" placeholder="请输入主设备描述 EQUIP_DESC" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="主设备序号 EQUIP_NUM" v-bind="validateInfos.equipNum" id="DfMdmIndicatorForm-equipNum" name="equipNum">
                <a-input v-model:value="formData.equipNum" placeholder="请输入主设备序号 EQUIP_NUM" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item
                label="主设备序号描述 EQUIP_NUM_DESC"
                v-bind="validateInfos.equipNumDesc"
                id="DfMdmIndicatorForm-equipNumDesc"
                name="equipNumDesc"
              >
                <a-input v-model:value="formData.equipNumDesc" placeholder="请输入主设备序号描述 EQUIP_NUM_DESC" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item
                label="辅件设备编码 ACCE_EQUIP_CODE"
                v-bind="validateInfos.acceEquipCode"
                id="DfMdmIndicatorForm-acceEquipCode"
                name="acceEquipCode"
              >
                <a-input v-model:value="formData.acceEquipCode" placeholder="请输入辅件设备编码 ACCE_EQUIP_CODE" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item
                label="辅件设备描述 ACCE_EQUIP_DESC"
                v-bind="validateInfos.acceEquipDesc"
                id="DfMdmIndicatorForm-acceEquipDesc"
                name="acceEquipDesc"
              >
                <a-input v-model:value="formData.acceEquipDesc" placeholder="请输入辅件设备描述 ACCE_EQUIP_DESC" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="部件编码 PART_NUM" v-bind="validateInfos.partNum" id="DfMdmIndicatorForm-partNum" name="partNum">
                <a-input v-model:value="formData.partNum" placeholder="请输入部件编码 PART_NUM" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="部件描述 PART_NUM_DESC" v-bind="validateInfos.partNumDesc" id="DfMdmIndicatorForm-partNumDesc" name="partNumDesc">
                <a-input v-model:value="formData.partNumDesc" placeholder="请输入部件描述 PART_NUM_DESC" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="零件 PART" v-bind="validateInfos.part" id="DfMdmIndicatorForm-part" name="part">
                <a-input v-model:value="formData.part" placeholder="请输入零件 PART" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="零件描述 PART_DESC" v-bind="validateInfos.partDesc" id="DfMdmIndicatorForm-partDesc" name="partDesc">
                <a-input v-model:value="formData.partDesc" placeholder="请输入零件描述 PART_DESC" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="PI类型 PI_TYPE" v-bind="validateInfos.piType" id="DfMdmIndicatorForm-piType" name="piType">
                <a-input v-model:value="formData.piType" placeholder="请输入PI类型 PI_TYPE" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="业务类型 BUSI_TYPE" v-bind="validateInfos.busiType" id="DfMdmIndicatorForm-busiType" name="busiType">
                <a-input v-model:value="formData.busiType" placeholder="请输入业务类型 BUSI_TYPE" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item
                label="指标实例编码 IND_INST_CODE"
                v-bind="validateInfos.indInstCode"
                id="DfMdmIndicatorForm-indInstCode"
                name="indInstCode"
              >
                <a-input v-model:value="formData.indInstCode" placeholder="请输入指标实例编码 IND_INST_CODE" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item
                label="实例使用编码 INST_USE_CODE"
                v-bind="validateInfos.instUseCode"
                id="DfMdmIndicatorForm-instUseCode"
                name="instUseCode"
              >
                <a-input v-model:value="formData.instUseCode" placeholder="请输入实例使用编码 INST_USE_CODE" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item
                label="业务状态 BUSINESS_STATUS"
                v-bind="validateInfos.businessStatus"
                id="DfMdmIndicatorForm-businessStatus"
                name="businessStatus"
              >
                <a-input v-model:value="formData.businessStatus" placeholder="请输入业务状态 BUSINESS_STATUS" />
              </a-form-item>
            </a-col>
            <!-- <a-col :span="12">
              <a-form-item
                label="实例业务状态 INST_BUSINESS_STATUS"
                v-bind="validateInfos.instBusinessStatus"
                id="DfMdmIndicatorForm-instBusinessStatus"
                name="instBusinessStatus"
              >
                <a-input v-model:value="formData.instBusinessStatus" placeholder="请输入实例业务状态 INST_BUSINESS_STATUS" />
              </a-form-item>
            </a-col> -->
            <a-col :span="12">
              <a-form-item
                label="锁产线状态 LOCK_LINE_STATUS"
                v-bind="validateInfos.lockLineStatus"
                id="DfMdmIndicatorForm-lockLineStatus"
                name="lockLineStatus"
              >
                <a-input v-model:value="formData.lockLineStatus" placeholder="请输入锁产线状态 LOCK_LINE_STATUS" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="是否启用" v-bind="validateInfos.enableFlag" id="DfMdmIndicatorForm-enableFlag" name="enableFlag">
                <a-input v-model:value="formData.enableFlag" placeholder="请输入是否启用" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="创建人" v-bind="validateInfos.createBy" id="DfMdmIndicatorForm-createBy" name="createBy">
                <a-input v-model:value="formData.createBy" placeholder="请输入创建人" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="创建时间" v-bind="validateInfos.createTime" id="DfMdmIndicatorForm-createTime" name="createTime">
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
              <a-form-item label="更新人" v-bind="validateInfos.updateBy" id="DfMdmIndicatorForm-updateBy" name="updateBy">
                <a-input v-model:value="formData.updateBy" placeholder="请输入更新人" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="更新时间" v-bind="validateInfos.updateTime" id="DfMdmIndicatorForm-updateTime" name="updateTime">
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
  import { saveOrUpdate } from '../DfMdmIndicator.api';
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
    funlocCode: '',
    funlocDesc: '',
    indBusiArea: '',
    indCenManDept: '',
    indName: '',
    indFraT0: '',
    indFraT1: '',
    indFraT2: '',
    indFraT3: '',
    indFraT4: '',
    indFraT5: '',
    indStandCode: '',
    indStandDesc: '',
    unit: '',
    unitCn: '',
    facCode: '',
    facDesc: '',
    deparCode: '',
    deparDesc: '',
    processCode: '',
    processDesc: '',
    lineCode: '',
    lineDesc: '',
    equipCode: '',
    equipDesc: '',
    equipNum: '',
    equipNumDesc: '',
    acceEquipCode: '',
    acceEquipDesc: '',
    partNum: '',
    partNumDesc: '',
    part: '',
    partDesc: '',
    piType: '',
    busiType: '',
    indInstCode: '',
    instUseCode: '',
    businessStatus: '',
    instBusinessStatus: '',
    lockLineStatus: '',
    enableFlag: '',
    createBy: '',
    createTime: '',
    updateBy: '',
    updateTime: '',
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 9 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 14 } });
  const confirmLoading = ref<boolean>(false);
  //表单验证
  const validatorRules = reactive({});
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
