<template>
    <a-spin :spinning="confirmLoading">
    <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol">
      <a-row>
        <!-- 部门 -->
        <!-- <a-col :span="24">
          <a-form-item label="部门" v-bind="validateInfos.queryDept">
            <kt-depart-select
              v-model:value="formData.deptCode"
              select-type="depart"
              placeholder="请选择部门"
              :disabled="disabled"
            />
          </a-form-item>
        </a-col> -->
        <!-- 产线 -->
        <!-- <a-col :span="24">
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
        </a-col> -->
        <!--任务名称-->
        <a-col :span="24">
          <a-form-item label="任务名称" v-bind="validateInfos.name">
            <a-input v-model:value="formData.name" placeholder="请输入任务名称" :disabled="disabled" />
          </a-form-item>
        </a-col>
        <!--定时任务-->
        <a-col :span="24">
          <a-form-item label="定时任务" v-bind="validateInfos.taskId">
            <a-select v-model:value="formData.taskId" :options="taskArr" placeholder="请选择" @change="tableChange" :disabled="disabled"></a-select>
          </a-form-item>
        </a-col>
        <!--报表名称-->
        <a-col :span="24">
          <a-form-item label="报表名称" v-bind="validateInfos.tableId">
            <a-select v-model:value="formData.tableId" :options="tableArr" placeholder="请选择" @change="tableChange" :disabled="disabled || (formData.id != '')"></a-select>
          </a-form-item>
        </a-col>

        <!--字段-->
        <a-col :span="24">
          <a-form-item label="字段" v-bind="validateInfos.fieldIds">
            <!-- <a-textarea v-model:value="formData.remark" rows="3" :placeholder="t('language.enterRemark')" :disabled="disabled" /> -->
            <div style="display: flex;align-items: center;">
              <a-select
                v-model:value="formData.fieldIds"
                mode="multiple"
                style="width: 100%"
                placeholder="请选择字段"
                :max-tag-text-length="10"
                :options="fieldArr"
                allowClear
              ></a-select>
              <CheckSquareOutlined title="全选" style="margin-left: 5px;font-size: 20px;" @click="selectAll"/>
            </div>
          </a-form-item>
          
        </a-col>

        <!-- 间隔 -->
         <a-col :span="24">
          <a-form-item label="间隔" v-bind="validateInfos.difference">
            <a-input-number v-model:value="formData.difference" placeholder="请输入间隔" :disabled="disabled" :min="0" addon-after="秒" style="width:100%"/>
          </a-form-item>
        </a-col>
        
        <!-- 排序 -->
        <a-col :span="24">
          <a-form-item :label="t('language.sort')" v-bind="validateInfos.sort">
            <a-input-number v-model:value="formData.sort" :placeholder="t('language.enterSort')" style="width: 100%" :disabled="disabled" />
          </a-form-item>
        </a-col>

      
        <!-- 备注 -->
        <a-col :span="24">
          <a-form-item :label="t('language.remark')" v-bind="validateInfos.remark">
            <a-textarea v-model:value="formData.remark" rows="3" :placeholder="t('language.enterRemark')" :disabled="disabled" />
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
    import { useI18n } from '/@/hooks/web/useI18n';
    const { t } = useI18n();
    import { CheckSquareOutlined } from '@ant-design/icons-vue';
    import {saveOrUpdate,task} from '../TaskParameter.api';
    import {list as tableList} from '/@/views/dataCalculation/pi/customizeTable/PiCustomizeTable.api';
    import {fieldList} from '/@/views/dataCalculation/pi/customizeTable/PiCustomizeTableField.api';
    import { useUserStore } from '/@/store/modules/user';
    // 用户信息
    const userStore = useUserStore();

    //报表集合
    const tableArr = reactive<Record<string, any>>([]);
    //字段集合
    const fieldArr = reactive<Record<string, any>>([]);
    //定时任务
    const taskArr = reactive<Record<string, any>>([]);

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
        name:'',
        difference: 0,
        fieldIds:undefined,
        taskId:'',
        sort:'',
    });
    const { createMessage } = useMessage();
    const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
    const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
    const confirmLoading = ref<boolean>(false);
    //表单验证
    const validatorRules = {
        // factoryCode: [{ required: true, message: `${t('language.enterFactory')}!` }],
        name: [{ required: true, message: `任务名称不为空!` }],
        tableId: [{ required: true, message: `报表不为空!` }],
        difference: [{ required: true, message: `间隔不为空!` }],
        fieldIds: [{ required: true, message: `字段不为空!` }],
        taskId: [{ required: true, message: `定时任务不为空!` }]
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
      getTableArr();
      getTaks();
    });

    /**
     * 新增
     */
    function add() {
      cleanData(formData);
      formData.value = {
            id: '',
            deptCode: '',
            lineCode: '',
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
        //赋值
        Object.assign(formData, record);
        if(record.fields){
          tableChange(record.tableId);
          formData.fieldIds = record.fields.split(",")
        }
        
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
        }else{
          //部门
          model.deptCode = userStore.departCode;
          //工厂
          model.factoryCode = userStore.factoryCode;
        }
        if(model.fieldIds){
          model["fields"] = model["fieldIds"].join(',');
        }
        await saveOrUpdate(model, isUpdate.value)
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

    /**
     * 初始化报表数据
     */
    async function getTableArr() {
        tableList({ pageNo: 1, pageSize: 999 }).then((res) => {
          if (res && res.records && res.records.length > 0) {
            res.records.map((item) => {
              tableArr.push({label: item.name, value: item.id, tableName: item.tableName});
            });
          }
        });
    }

    /**
     * 报表切换获取字段数据 
     */
    function tableChange(text){
      if(formData.tableId && formData.taskId){
        fieldArr.length = 0
        formData.fieldIds = [];
        //获取自定义报表表名(排除修改)
        if(!(formData.id && formData.tableName)){
          const tableObj = tableArr.find(item => item.value === formData.tableId);
          formData.tableName = tableObj.tableName
        }

        //获取字段(排除唯一标识字段)-同时配置过算法
        fieldList({ tableId:formData.tableId,tableName:formData.tableName, isUnique:'0',id:formData.id,taskId:formData.taskId}).then((res) => {
          if (res && res.length > 0) {
            res.map((item) => {
              fieldArr.push({label: item.name, value: item.fieldName});
            });
          }
        });
      }else{
        fieldArr.length = 0
        formData.fieldIds = [];
      }
    }

    /**
     * 初始化定时任务下拉数据
     */
    async function getTaks() {
        task({}).then((res) => {
          if (res && res.length > 0) {
            res.map((item) => {
              taskArr.push(item);
            });
          }
        });
    }
    /**
     * 重算-字段全选
     */
    function selectAll(){
      formData.fieldIds = []
      fieldArr.map((item) => {
        formData.fieldIds.push(item.value);
      });
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