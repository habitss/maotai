<template>
    <a-spin :spinning="confirmLoading">
    <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol">
      <a-row>
         <!--报表名称-->
        <a-col :span="24">
          <a-form-item label="报表名称" v-bind="validateInfos.reportName">
            <a-input v-model:value="formData.reportName" placeholder="请输入报表名称" :disabled="disabled" />
          </a-form-item>
        </a-col>
        
        <!--报表类型-->
        <a-col :span="24">
          <a-form-item label="报表类型" v-bind="validateInfos.type">
            <!-- <a-input v-model:value="formData.type" placeholder="请选择报表类型" :disabled="disabled" /> -->
            <kt-dict
              v-model:value="formData.type"
              placeholder="请选择报表类型"
              dict="MES3_PI_TABLE_TYPE"
              :disabled="disabled"
            />
          </a-form-item>
        </a-col>
        <!--是否分配多表-->
        <a-col :span="24">
          <a-form-item label="是否分配多表" v-bind="validateInfos.multipleTables">
            <a-switch checkedChildren="是" unCheckedChildren="否" v-model:checked="multipleTableData.multipleTables"/>
          </a-form-item>
        </a-col>


        <!--绑定表-->
        <a-col :span="24">
          <a-form-item label="绑定表" v-bind="validateInfos.tableId">
            <a-select v-if="multipleTableData.multipleTables" v-model:value="multipleTableData.tableId" :options="tableAloneArr" placeholder="请选择" @change="tableChange" :disabled="disabled" mode="multiple"></a-select>
            <a-select v-else v-model:value="formData.tableId" :options="tableArr" placeholder="请选择" @change="tableChange" :disabled="disabled"></a-select>
          </a-form-item>
        </a-col>

        <a-col :span="24">
          <a-form-item label="表名" v-bind="validateInfos.tableName">
             <a-input v-model:value="formData.tableName" :disabled="true"/>
          </a-form-item>
        </a-col>

        <!-- 排序 -->
        <!-- <a-col :span="24">
          <a-form-item :label="t('language.sort')" v-bind="validateInfos.sort">
            <a-input-number v-model:value="formData.sort" :placeholder="t('language.enterSort')" style="width: 100%" :disabled="disabled" />
          </a-form-item>
        </a-col> -->

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
    import { useMessage } from '/@/hooks/web/useMessage';
    import { Form } from 'ant-design-vue';
    import KtDict from '/@/components/KtDict/KtDict.vue';
    import { useI18n } from '/@/hooks/web/useI18n';
    const { t } = useI18n();

    import {saveOrUpdate} from '../AllocationTable.api';
    import {list as tableList} from '/@/views/dataCalculation/pi/customizeTable/PiCustomizeTable.api';
    //报表集合全量
    const tableArr = reactive<Record<string, any>>([]);
    //报表集合排查无限扩展表
    const tableAloneArr = reactive<Record<string, any>>([]);

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
      reportName: '',
      tableId: undefined,
      type:''
    });
    const multipleTableData = reactive<Record<string, any>>({
      tableId: undefined,
      multipleTables: false
    })
    const { createMessage } = useMessage();
    const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
    const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
    const confirmLoading = ref<boolean>(false);
    //表单验证
    const validatorRules = {
        // factoryCode: [{ required: true, message: `${t('language.enterFactory')}!` }],
        reportName: [{ required: true, message: `报表名称不为空!` }],
        tableId: [{ required: true, message: `绑定表不为空!` }],
        type: [{ required: true, message: `报表类型不为空!` }],
        multipleTables: [{ required: true, message: `是否分配多表不为空!`}],
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
        }
        if(record.multipleTables === '1'){
          multipleTableData["tableId"] = record.tableId.split(",")
          multipleTableData["multipleTables"] = record.multipleTables === '1'? true : false;
        }else{
          multipleTableData["multipleTables"] = false;
        }
        console.log('获取表单数据', formData);
      });
    }

    //获取表名
    function tableChange(value){
      if(multipleTableData.multipleTables){
        //多表
        let targetObj = {};
        let tableName = [];
        value.forEach(element => {
          targetObj = tableArr.find(obj => obj.value === element)
          tableName.push(targetObj.tableName);
        });

        if(tableName && tableName.length > 0){
          formData.tableName = tableName.join(",");
          formData.deptCode = targetObj.deptCode;
          formData.lineCode = targetObj.lineCode;
        }
      }else{
        //单表
        let targetObj = tableArr.find(obj => obj.value === value)
        if(targetObj){
          formData.tableName = targetObj.tableName;
          formData.deptCode = targetObj.deptCode;
          formData.lineCode = targetObj.lineCode;
        }
      }
      
    }
    /**
     * 提交数据
     */
    async function submitForm() {
      let model = formData;
      
      if(multipleTableData.multipleTables){
        model["tableId"] = multipleTableData.tableId.join(',')
        model["multipleTables"] = multipleTableData.multipleTables === true? 1 : 0;
      }
      // 触发表单验证
      await validate();
      confirmLoading.value = true;
      const isUpdate = ref<boolean>(false);
      //时间格式化
     
      if (model.id) {
        isUpdate.value = true;
      }else{
        model.deptCode = userStore.departCode;
      }
  
      if(model.fieldIds){
        model["fields"] = model["fieldIds"].join(',');
      }
      
      
      model.factoryCode = userStore.factoryCode;

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
     * 初始化设备类型下拉数据
     */
    async function getTableArr() {
        tableList({ pageNo: 1, pageSize: 999 }).then((res) => {
          if (res && res.records && res.records.length > 0) {
            res.records.forEach((item) => {
              tableArr.push({label: item.name, value: item.id, tableName:item.tableName, deptCode:item.deptCode, lineCode:item.lineCode});
              // 排查无限扩展
              if(!item.viewName){
                tableAloneArr.push({label: item.name, value: item.id, tableName:item.tableName, deptCode:item.deptCode, lineCode:item.lineCode});
              }
            });
          }
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