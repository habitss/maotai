<template>
    <a-spin :spinning="confirmLoading">
    <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol">
      <a-row>
        <!-- 部门 -->
        <a-col :span="24">
          <a-form-item label="部门" v-bind="validateInfos.deptCode">
            <kt-depart-select
              v-model:value="formData.deptCode"
              select-type="depart"
              placeholder="请选择部门"
              :disabled="disabled"
            />
          </a-form-item>
        </a-col>
        <!-- 产线 -->
        <a-col :span="24">
          <a-form-item label="产线" v-bind="validateInfos.lineCode">
            <kt-depart-select
              v-model:value="formData.lineCode"
              select-type="line"
              @currentData="getDepartObject"
              placeholder="请选择产线"
              :parentKey="formData.deptCode"
              :disabled="disabled"
            />
          </a-form-item>
        </a-col>
        <!--设备名称-->
         <a-col :span="24">
          <a-form-item label="设备" v-bind="validateInfos.deviceId">
            <kt-device
                ref="ktDeviceRef"
                v-model:value="formData.deviceId"
                :disabled="disabled"
                :lineCode="formData.lineCode"
                dict=""
                placeholder="请选择设备"
                type="device"
            />
          </a-form-item>
        </a-col>
        <!-- 步骤号 -->
         <a-col :span="24">
          <a-form-item label="步骤号" v-bind="validateInfos.stepNum">
            <a-input v-model:value="formData.stepNum" placeholder="请输入步骤号" :disabled="disabled" />
          </a-form-item>
        </a-col>
        <!-- 步骤名称 -->
        <a-col :span="24">
          <a-form-item label="步骤名称" v-bind="validateInfos.stepName">
            <a-input v-model:value="formData.stepName" placeholder="请输入步骤名称" :disabled="disabled" />
          </a-form-item>
        </a-col>
        <!-- 排序 -->
        <a-col :span="24">
          <a-form-item label="排序" v-bind="validateInfos.sort">
            <a-input-number v-model:value="formData.sort" :placeholder="t('language.enterSort')" style="width: 100%" :disabled="disabled" />
          </a-form-item>
        </a-col>
        <!-- 备注 -->
        <!-- <a-col :span="24">
          <a-form-item :label="t('language.remark')" v-bind="validateInfos.remark">
            <a-textarea v-model:value="formData.remark" rows="3" :placeholder="t('language.enterRemark')" :disabled="disabled" />
          </a-form-item>
        </a-col> -->
      </a-row>
    </a-form>
  </a-spin>


</template>

<script lang="ts" setup>
    import { ref, reactive, defineExpose, nextTick, defineProps, computed, onMounted, watch } from 'vue';
    import { defHttp } from '/@/utils/http/axios';
    import { useMessage } from '/@/hooks/web/useMessage';
    import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';
    import JSelectDept from '/@/components/Form/src/jeecg/components/JSelectDept.vue';
    import { getValueType } from '/@/utils';
    import { Form } from 'ant-design-vue';
    import KtDict from '/@/components/KtDict/KtDict.vue';
    import KtDevice from '/@/components/KtDeviceType/KtDeviceType.vue';
    import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
    import {saveOrUpdate} from '../PiStep.api';
    import { useI18n } from '/@/hooks/web/useI18n';
    const { t } = useI18n();
    import { useUserStore } from '/@/store/modules/user';
    // 用户信息
    const userStore = useUserStore();


    const props = defineProps({
        formDisabled: { type: Boolean, default: false },
        formData: { type: Object, default: () => {} },
        formBpm: { type: Boolean, default: true },
        isAdd: { type: Boolean, default: true },
    });

    const formRef = ref();
    const useForm = Form.useForm;
    const emit = defineEmits(['register', 'ok']);
    const formData = reactive<Record<string, any>>({
        id: '',
        deptCode: '',
        lineCode: '',
        deviceId:'',
        stepNum:'',
        stepName:'',
    });
    const { createMessage } = useMessage();
    const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
    const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
    const confirmLoading = ref<boolean>(false);
    //表单验证
    const validatorRules = {
        // factoryCode: [{ required: true, message: `${t('language.enterFactory')}!` }],
        queryDept: [{ required: true, message: `部门不为空!` }],
        lineCode: [{ required: true, message: `产线不为空!` }],
        deviceId: [{ required: true, message: `设备不为空!` }],
        stepNum: [{ required: true, message: `步骤号不为空!` }],
        stepName: [{ required: true, message: `步骤名称不为空!` }],
    };
    const { resetFields, validate, validateInfos } = useForm(formData, validatorRules, { immediate: true });

    // 表单禁用
    const disabled = computed(() => {
    if (props.formBpm === true) {
        if (props.formData.disabled === false) {
        return false;
        } else {
        return true;
        }
    }
    return props.formDisabled;
    });
    const setEditStatus = computed(() => {
    if (!props.isAdd) {
        return true;
    }
    return false;
    });
    watch(
        () => formData.orgTypeCode,
        (factoryCode: string, oldVal: string) => {
            // formData.deviceTypeId = undefined;
        }
    );

    onMounted(() => {
  // getDeviceTypeArr();
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
    //赋值
    Object.assign(formData, record);
    console.log('获取表单数据', formData);
  });
}

  /**
   * 提交数据
   */
  async function submitForm() {
      // 触发表单验证
      await validate();
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
      model.factoryCode = userStore.factoryCode;
      await saveOrUpdate(model, isUpdate.value)
          .then((res) => {
          if (res.success) {
              // const successMessage = res.message ? res.message : t('language.save');
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

    /**
     * 初始化设备类型下拉数据
     */
    // async function getDeviceTypeArr() {
    //     deviceTypeList({ pageNo: 1, pageSize: 999 }).then((res) => {
    //         if (res && res.records && res.records.length > 0) {
    //         res.records.map((item) => {
    //             return { text: item.itemKey, value: item.itemVal };
    //         });
    //         }
    //     });
    // }

    function getChange() {
        formData.deptCode = '';
        formData.lineCode = '';
    }

    function getDepartObject(ret) {
        if (ret.memo) {
            formData.orgTypeCode = undefined;
            formData.orgTypeCode = ret.memo;
            //根据结构类型值获取设备类型
            // findDeviceTypeByOrgCode({ orgTypeCode: ret.memo });
        }
    }

    defineExpose({
        add,
        edit,
        submitForm,
    });

</script>

<style lang="less" scoped>
// @import '../../common.less';
</style>