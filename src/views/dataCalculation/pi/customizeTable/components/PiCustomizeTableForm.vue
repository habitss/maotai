<template>
    <a-spin :spinning="confirmLoading">
    <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol">
      <a-row>
        <!-- 工段 -->
        <!-- <a-col :span="24">
          <a-form-item label="工段" v-bind="validateInfos.section">
            <kt-dict v-model:value="formData.section" :placeholder="t('language.selectWorkSection')" dict="ORG_TYPE" :disabled="disabled"/>
          </a-form-item>
        </a-col> -->
        <!-- 部门 -->
        <a-col :span="24">
          <a-form-item label="部门/产线" v-bind="validateInfos.selectDeptCode">
            <KtDepartTreeSelect 
              v-model:value="formData.selectDeptCode"
              :allowClear="true" 
              :defaultExpandLevel="1" 
              :byUserOrg="true"
              :placeholder="'请选择部门/产线'"
              :fieldNames=" { label:'title', value: 'orgCode' }">
            </KtDepartTreeSelect>
          </a-form-item>
        </a-col>
        <!--报表名称-->
         <a-col :span="24">
          <a-form-item label="报表名称" v-bind="validateInfos.name">
            <a-input v-model:value="formData.name" placeholder="请输入报表名称" :disabled="disabled" />
          </a-form-item>
        </a-col>
        <!-- 表名 -->
        <!-- <a-col :span="24">
          <a-form-item label="表名" v-bind="validateInfos.tableName">
            <a-input v-model:value="formData.tableName" placeholder="请输入表名" :disabled="disabled || !props.isAdd" />
          </a-form-item>
        </a-col> -->
        <!--保留几天数据-->
        <a-col :span="24">
          <a-form-item label="数据保留天数" v-bind="validateInfos.days">
            <a-input-number v-model:value="formData.days" :placeholder="t('language.days')" style="width: 100%" :disabled="disabled" :min="0" :precision="0" />
            <span style="color: red;">为空或者0则默认永久保留</span>
          </a-form-item>
        </a-col>
        <!--是否自动报表-->
        <a-col :span="24">
          <a-form-item label="是否自动报表" v-bind="validateInfos.automatic">
            <a-select v-model:value="formData.automatic" :options="automaticArr" placeholder="请选择" allowClear></a-select> 
          </a-form-item>
        </a-col>
        <!--数据重算时间对照PI-->
        <a-col :span="24">
          <a-form-item label="数据重算时间对照PI" v-bind="validateInfos.recalculateTimePi">
            <a-select v-model:value="formData.recalculateTimePi" :options="timeFieldArr" placeholder="请选择" show-search option-filter-prop="label" allowClear></a-select> 
          </a-form-item>
        </a-col>

        <!-- 排序 -->
        <a-col :span="24">
          <a-form-item label="排序" v-bind="validateInfos.sort">
            <a-input-number v-model:value="formData.sort" :placeholder="t('language.enterSort')" style="width: 100%" :disabled="disabled" />
          </a-form-item>
        </a-col>
        <!-- 备注 -->
        <a-col :span="24">
          <a-form-item :label="t('language.remark')" v-bind="validateInfos.notes">
            <a-textarea v-model:value="formData.notes" rows="3" :placeholder="t('language.enterRemark')" :disabled="disabled" />
          </a-form-item>
        </a-col>
      </a-row>
    </a-form>
  </a-spin>


</template>

<script lang="ts" setup>
  import { ref, reactive, nextTick, computed, onMounted } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getValueType } from '/@/utils';
  import { Form } from 'ant-design-vue';
  import {saveOrUpdate} from '../PiCustomizeTable.api';
  import {list as fieldList} from '../PiCustomizeTableField.api';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();
  import { useUserStore } from '/@/store/modules/user';
  import KtDepartTreeSelect from '/@/components/KtDepartSelect/KtDepartTree.vue';
  import { getFactoryDepartLineByOrgCode, setFirstAvailableCode } from '/@/utils/ktTool';
  // 用户信息
  const userStore = useUserStore();
  //是否自动报表
  const automaticArr = reactive<Record<string, any>>([
      { value: '0', label: '手动报表' },
      { value: '1', label: '自动报表' }
  ]);
  const timeFieldArr = ref([]);
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
    id: "",
    deptCode: null,
    lineCode: "",
    name: "",
    days: "",
    automatic: "",
    sort:"",
    recalculateTimePi: undefined,
    selectDeptCode: null,
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  //表单验证
  const validatorRules = {
      // factoryCode: [{ required: true, message: `${t('language.enterFactory')}!` }],
      selectDeptCode: [{ required: true, message: `部门不为空!` }],
      name: [{ required: true, message: `报表名称不为空!` }],
      automatic: [{ required: true, message: `是否自动报表不为空!` }]
      // tableName: [{ required: true, message: `表名不为空!` }],
      // stepName: [{ required: true, message: `步骤名称不为空!` }],
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

  onMounted(() => {
  });

  /**
   * 新增
   */
  function add() {
    cleanData(formData);
    formData.value = {
      id: ''
    }
    edit({});
  }
  function cleanData(valObj){
    // 清空formData的值
    for (const key in valObj) {
        if (valObj.hasOwnProperty(key)) {
            valObj[key] = '';
        }
    }
  }
  /**
   * 编辑
   */
  function edit(record) {
    nextTick(() => {
      resetFields();
      setFirstAvailableCode(record, 'selectDeptCode');
      //赋值
      Object.assign(formData, record);
      if(record && record.id){
        getTimeField(record.id)
      }
      console.log('获取表单数据', formData);
    });
  }

  /**
   * 提交数据
   */
  async function submitForm() {
      try{
        getFactoryDepartLineByOrgCode(formData.selectDeptCode, formData);
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
      } catch (error) {
        console.log('error :>> ', error);
      } finally {
        confirmLoading.value = false;
      }
    }

    /**
     * 获取字段下拉
     */
    async function getTimeField(tableId) {
      timeFieldArr.value = [];
      fieldList({ pageNo: 1, pageSize: 9999,tableId, type: 6 }).then((res) => {
          if (res && res.records && res.records.length > 0) {
            timeFieldArr.value = res.records.map((item) => {
                return { label: item.name, value: item.fieldName };
            });
          }
      });
    }

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