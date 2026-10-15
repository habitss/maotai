<template>
    <a-spin :spinning="confirmLoading">
    <a-form ref="form" form="form" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol">
      <a-row>
        <!--动态字段-->
        <a-col :span="12" v-for="(item, index) in fieldArr" :key="index">
          <a-form-item :label="item.name" v-bind="validateInfos[item.fieldName]" :rules="getRulesForItem(item)">

            <!-- <a-input v-model:value="formData[item.fieldName]" placeholder="请输入" :disabled="disabled" /> -->
            <!--时间-->
            <a-date-picker v-if="item.type=='6'" format="YYYY-MM-DD HH:mm:ss" v-model:value="rangePickerTime[item.fieldName]" @change="onChangeTime(item.fieldName)" show-time style="width: 100%;"/>              
            <!--日期-->
            <a-date-picker v-else-if="item.type=='4'" format="YYYY-MM-DD" v-model:value="rangePickerDate[item.fieldName]" @change="onChangeDate(item.fieldName)" style="width: 100%;"/>
            <!--输入框-->
            <a-input placeholder="请输入" v-else-if="(item.type=='1' || item.type=='2' || item.type=='5') && item.editWidgetType !='select' && item.editWidgetType !='selectUser'" v-model:value="formData[item.fieldName]"/>
            <!--下拉-->
            <kt-dict-content
              v-else-if="((item.type=='1' || item.type=='2' || item.type=='5') && item.editWidgetType=='select') && item.editWidgetType!='input' && item.editWidgetType!='selectUser'" 
              v-model:value="formData[item.fieldName]"
              placeholder="请选择"
              :dict="item.dropdownData"
              :labelInValueProp="true"
            />
            <!--人员-->
            <!-- <a-input placeholder="请输入" v-else-if="(item.type=='1' || item.type=='2' || item.type=='5' || item.editWidgetType=='selectUser') && item.editWidgetType !='select' && item.editWidgetType!='input'" v-model:value="formData[item.fieldName]"/> -->
            <JDictSelectTag  v-else v-model:value="formData[item.fieldName]" placeholder="请选择用户名称" dictCode="sys_user,realname,realname" />
          </a-form-item>
        </a-col>

      </a-row>
    </a-form>
  </a-spin>

</template>

<script lang="ts" setup>
    import { ref, reactive, defineExpose, nextTick, defineProps, computed, onMounted, watch } from 'vue';
    import { useMessage } from '/@/hooks/web/useMessage';
    import { Form } from 'ant-design-vue';
    import KtDictContent from '/@/components/KtDictContent/KtDictContent.vue';
    import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';
    import dayjs from 'dayjs';
    import { useI18n } from '/@/hooks/web/useI18n';
    const { t } = useI18n();

    import {saveOrUpdate} from '../AllocationTable.api';
    import {tableHead,batchEditVal} from '/@/views/dataCalculation/pi/allocationTable/QueryTable.api';

    //报表集合
    const tableArr = reactive<Record<string, any>>([]);
    //报表字段集合
    const fieldArr = ref([]) as any;

    //日期
    const rangePickerDate = ref<Object>({});
    //时间
    const rangePickerTime = ref<Object>({});
    //下拉(字典)
    const ktDictData = ref<Object>({});

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
        reportName: '',
        tableId: '',
        type:''
    });
    const { createMessage } = useMessage();
    const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 6 } });
    const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
    const confirmLoading = ref<boolean>(false);
    //表单验证
    const validatorRules = {
        //name: [{ required: true, message: `报表名称不为空!` }],
        // tableId: [{ required: true, message: `绑定表不为空!` }],
        // type: [{ required: true, message: `报表类型不为空!` }]
    };
    // validatorRules["name"] = [{ required: true, message: `报表名称不为空!` }];
    
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
      //getTableArr();
    });

    /**
     * 新增
     */
    function add(tableParams) {
      cleanData(formData);
      formData.tableName = tableParams.tableName;
      //获取字段
      getFields(tableParams);
    }

    //获取字段集合
    function getFields(tableParams){
      confirmLoading.value = true;
      tableHead({ type: tableParams.type,tableId: tableParams.tableId}).then((res) => {
        confirmLoading.value = false;
        if (res && res.length > 0) {
          for(let field of res){
            formData[field.fieldName]="";
          }
          fieldArr.value = res;
          //唯一约束必填
          //validatorRules{}
        }
      });
    }

    function cleanData(valObj){
      // 清空formData的值
      for (const key in valObj) {
          if (valObj.hasOwnProperty(key)) {
              valObj[key] = '';
          }
      }
      rangePickerDate.value = [];
      rangePickerTime.value = [];
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
        if(model.fieldIds){
          model["fields"] = model["fieldIds"].join(',');
        }
        let data = {
          data:[model],
          tableName: model.tableName
        }
        await batchEditVal(data)
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
    function getRulesForItem(item){
      if (item.isUnique == '1') {
        return [{ required: true, message: item.name+'不为空' }];
      }
      return [];
    }
    //时间
    function onChangeTime(fieldName){
      if(rangePickerTime.value[fieldName]){
        let timeBegin = rangePickerTime.value[fieldName];
        //
        formData[fieldName] = dayjs(timeBegin).format('YYYY-MM-DD HH:mm:ss')
      }else{
        formData[fieldName] ="";
      }
    }
    //日期
    function onChangeDate(fieldName){
      if(rangePickerDate.value[fieldName]){
        let dateBegin = rangePickerDate.value[fieldName];
        //
        formData[fieldName] = dayjs(dateBegin).format('YYYY-MM-DD');
      }else{
        formData[fieldName] ="";
      }
    }
    //字段选择
    function dictChange(fieldName){
      formData[fieldName] = ktDictData.value[fieldName]
    }

    defineExpose({
      add,
      submitForm,
    });
</script>

<style lang="less" scoped>
// @import '../../common.less';
</style>