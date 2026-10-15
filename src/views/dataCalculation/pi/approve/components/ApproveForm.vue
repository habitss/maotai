<template>
    <a-spin :spinning="confirmLoading">
    <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol">
      <div v-if="formData.type === 0">
        <a-row>
          <a-col :span="24">
            <a-form-item label="原值" v-bind="validateInfos.originalValue">
              <a-input v-model:value="formData.originalValue" :disabled="true"/>
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item label="新值" v-bind="validateInfos.modifyValue">
              <a-input placeholder="请输入" v-model:value="formData.modifyValue"/>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row>
          <a-col :span="24">
            <a-form-item label="修改原因" v-bind="validateInfos.reason">
              <a-textarea v-model:value="formData.reason" />
            </a-form-item>
          </a-col>
        </a-row>
      </div>

      <div v-if="formData.type === 1">
        <a-row>
          <a-col :span="24">
            <a-form-item label="剔除分类" v-bind="validateInfos.removeCategory">
              <kt-dict
                v-model:value="formData.removeCategory"
                placeholder="请选择点位类型"
                dict="MES3_PI_REMOVE_CATEGORY"
              />
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item label="剔除范围" v-bind="validateInfos.removeRange">
              <a-radio-group v-model:value="formData.removeRange">
                <a-radio value="1">当前PI</a-radio>
                <a-radio value="0">整条PI</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row>
          <a-col :span="24">
            <a-form-item label="剔除原因" v-bind="validateInfos.reason">
              <a-textarea v-model:value="formData.reason" />
            </a-form-item>
          </a-col>
        </a-row>
      </div>
    </a-form>
   
  </a-spin>

</template>

<script lang="ts" setup>
    import { ref, reactive, defineExpose, nextTick,defineProps, computed, onMounted } from 'vue';
    import { useMessage } from '/@/hooks/web/useMessage';
    import { Form } from 'ant-design-vue';
    import KtDict from '/@/components/KtDict/KtDict.vue';
    import dayjs from 'dayjs';
    import { useI18n } from '/@/hooks/web/useI18n';
    const { t } = useI18n();
    import { useUserStore } from '/@/store/modules/user';
    // 用户信息
    const userStore = useUserStore();
    import {saveOrUpdate} from '../Approve.api';
    // import {tableHead,batchEditVal} from '/@/views/dataCalculation/pi/approve/QueryTable.api';

    //参数集合
    const params = ref<Object>({});

    //报表字段集合
    const fieldArr = ref([]) as any;

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
        tableId: '',
        type:'',
        removeRange: '1',
        removeCategory:"",
        reason:"",
        modifyValue:""
    });
    const { createMessage } = useMessage();
    const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 6 } });
    const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
    const confirmLoading = ref<boolean>(false);
    //表单验证
    const validatorRules = {
        removeCategory: [{ required: true, message: `剔除分类不为空!` }],
        reason: [{ required: true, message: `原因不为空!` }],
        modifyValue: [{ required: true, message: `新值不为空!` }]
    };
    
    const {  resetFields,validate, validateInfos } = useForm(formData, validatorRules, { immediate: true });

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
      //getTableArr();
    });

    /**
     * 新增
     */
    function add(record) {
      // formData = tableParams;
      nextTick(() => {
        resetFields();
        //赋值
        Object.assign(formData, record);
        console.log('获取表单数据', formData);
      });
      // cleanData(formData);
      //获取字段
      //getFields(tableParams);
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
     * 提交数据
     */
    async function submitForm() {

      // 触发表单验证
      //await validate();
      if("1" == formData.type){
        if(!formData.removeCategory){
          createMessage.warning("剔除分类不为空!");
          return false;
        }
        
      }
      if("0" == formData.type){
        if(!formData.modifyValue){
          createMessage.warning("新值不为空!");
          return false;
        }
      }

      if(!formData.reason){
        createMessage.warning("原因不为空!");
        return false;
      }
      confirmLoading.value = true;
      const isUpdate = ref<boolean>(false);
      //时间格式化
      if (formData.id) {
          isUpdate.value = true;
      }
      // formData.deptCode = userStore.departCode;
      // formData.factoryCode = userStore.factoryCode;
      await saveOrUpdate(formData,isUpdate.value)
        .then((res) => {
          if (res.success) {
              const successMessage = res.message ? res.message : t('language.save');
              createMessage.success(successMessage);
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
      submitForm,
    });
</script>

<style lang="less" scoped>
// @import '../../common.less';
</style>