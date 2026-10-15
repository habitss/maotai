<template>
  <a-spin :spinning="confirmLoading">
    <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol">
      <div style="width: 800px;height: 700px;margin: auto;">
        <a-transfer
            v-model:target-keys="formData.fieldIds"
            :data-source="mockData"
            :titles="['未分配', '已分配']"
            show-search
            :filter-option="filterOption"
            :render="item => item.title"
            @change="handleChange"
            @search="handleSearch"
            :list-style="{
              width: '100%',
              height: '700px',
            }"
          />
      </div>

      <div v-if="formData.multipleTables === '1'">
        <a-divider>关联条件</a-divider>
        <!--关联方式-->
        <div style="width: 80%;">
          <a-form-item label="关联方式" v-bind="validateInfos.correlation" name="correlation">
            <a-select show-search option-filter-prop="label" v-model:value="formData.correlation" :options="correlationArr" placeholder="请选择"></a-select>
          </a-form-item>
        </div>
        
        <div style="width: 80%;margin: 0 auto;">
          <template v-for="item in conditionAllArray" :key="item.id">
            <a-row justify="space-between" style="margin-top: 5px;">
              <a-col :span="5">
                <a-select v-model:value="item['relationship']" placeholder="请选择条件"
                  allowClear
                  show-search
                  :filterOption = "(input, option) => handleFilterOption(input, option, 'table')"
                  :options="relationshipArr"
                  :disabled="isDetail || formData.correlation === '2'"
                  @change="() => { item['groupCode'] = undefined; }"
                  style="width: 50%;"
                />
                <kt-dict
                  v-model:value="item['groupCode']"
                  placeholder="分组"
                  dict="MES3_PI_GROUP"
                  style="width: 45% !important;"
                  :disabled="item['relationship'] == '1' || !item['relationship']"
                />
              </a-col>

              <a-col :span="4">
                <!--表-->
                <a-select v-model:value="item['tableName']" placeholder="请选择表"
                  allowClear
                  show-search
                  :filterOption = "(input, option) => handleFilterOption(input, option, 'table')"
                  :options="tableArray"
                  :fieldNames="{label: 'name', value: 'tableName'}"
                  @change="handleChange(item,'1')"
                  :disabled="isDetail"
                  style="width: 90%;"
                />
              </a-col>
              <a-col :span="4">
                <!--表对应字段-->
                <a-select v-model:value="item['tableField']" placeholder="请选择字段" 
                  allowClear
                  show-search
                  :filterOption = "(input, option) => handleFilterOption(input, option, 'field')"
                  :options="item['fieldArray']"
                  :fieldNames="{label: 'name', value: 'fieldName'}" 
                  @change="handleChange"
                  :disabled="isDetail"
                  style="width: 100%;"
                />
              </a-col>
              <a-col :span="1" style="text-align: center;">
                <span> = </span>
              </a-col>
              <a-col :span="4">
                <!--关联表-->
                <a-select v-model:value="item['correlationTableName']" placeholder="请选择关联表" 
                  allowClear
                  show-search
                  :filterOption = "(input, option) => handleFilterOption(input, option, 'table')"
                  :options="tableArray"
                  :fieldNames="{label: 'name', value: 'tableName'}" 
                  @change="handleChange(item,'2')"
                  :disabled="isDetail"
                  style="width: 90%;"
                />
              </a-col>
              <a-col :span="4">
                <!--关联表对应字段-->
                <a-select v-model:value="item['correlationTableField']" placeholder="请选择关联字段" 
                  allowClear
                  show-search
                  :filterOption = "(input, option) => handleFilterOption(input, option, 'field')"
                  :options="item['coorFieldArray']"
                  :fieldNames="{label: 'name', value: 'fieldName'}" 
                  :disabled="isDetail"
                  style="width: 90%;"
                />
              </a-col>

              <a-col :span="2" v-if="!isDetail">
                <a-button @click="removeCondition(item.id)" type="link" danger style="padding: 0">删除</a-button>
              </a-col>
            </a-row>

          </template>
          <div style="margin: 15px auto;text-align: center;" v-if="!isDetail">
            <a-button type="link" @click="addCondition()">添加条件</a-button>
          </div>
          <div style="margin: 15px auto;">
            <span style="color:red" v-if="formData.correlation === '1'">
              备注：或(or) 需要设置分组,将相同分组的归类成同类条件 and (条件1 or 条件2 or 条件3)
            </span>
            <span style="color:red" v-if="formData.correlation === '2'">
              备注：左侧配置关联的主表
            </span>
          </div>
        </div>
      </div>
      
      <div style="text-align: center;margin-top: 20px;">
        <a-button style="margin-right: 8px" @click="onClose">取消</a-button>
        <a-button type="primary" @click="submitForm" v-if="!formData.tableHeadConfig">保存</a-button>
        <a-button type="primary" @click="allocate" v-if="formData.tableHeadConfig">分配</a-button>
      </div>
    </a-form>
  </a-spin>

</template>

<script lang="ts" setup>
  import { ref, reactive, defineExpose, defineProps, computed, onMounted, provide } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { Form } from 'ant-design-vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();
  import {saveOrUpdate,fieldData,list as getPiFieldList,getTableCondition} from '../AllocationTableField.api';
  //import { list as getPiFieldList } from '@/views/dataCalculation/pi/allocationTable/AllocationTableField.api';
  import { buildUUID } from '/@/utils/uuid';
  import KtDict from '/@/components/KtDict/KtDict.vue';
  const props = defineProps({
    departObject: { type: Object,default:()=>({}) },
  });
  const departObject = ref<Object>({});
  provide('departObject', departObject);
  //详情标识
  const isDetail = ref(false);
  //条件下拉选项数据
  const conditionArray = ref([]);
  //条件下拉选项数据
  const conditionOption = ref([]);
  //条件总数
  const conditionTotal = ref(0);
  //初始加载loading
  const modalLoading = ref(true);
  //保存loading
  // const confirmLoading = ref(false);
  //PI基础数据
  const configData = ref(false);
  //
  const modeData = ref({});
  //
  const tableArray = ref([]);
  //
  const fieldArray = ref([]);
  //表字段对照
  const tableFieldArray = ref({});
  //条件集合
  const conditionAllArray = ref([]);

  interface MockData {
    key: string;
    title: string;
    description: string;
    chosen: boolean;
  }

  const mockData = ref<MockData[]>([]);
  //const targetKeys = ref<string[]>([]);
  //条件关系
  const relationshipArr = ref([
    {value: '1', label: '与(and)'},
    {value: '2', label: '或(or)'}
  ]) 
  //关联条件
  const correlationArr = ref([
    {value: '1', label: '内连接(INNER JOIN)'},
    {value: '2', label: '左外连接(LEFT JOIN)'}
  ])

  //获取字段数据
  async function getFieldData(){
    const keys = [];
    const mData = [];
    let res = [];
    if(formData.tableHeadConfig){
      //自定义表头
      let datas = await getPiFieldList({allocationTableId: formData.allocationTableId, isList: 1, pageSize: 1000, orderField:'sort', orderType:'asc'});
      res = datas.records;
      res.forEach(item => {
        item.description = item.name;
        item.title = item.name+"("+item.fieldName+")";
        item.key = item.id;
      });
    }else{
      //PI分配
      res = await fieldData({tableId:formData.tableId,id:formData.allocationTableId,multipleTables:formData.multipleTables});
    } 
    if(res){
      for (let i = 0; i < res.length; i++) {
        let data = res[i];
        if (data.chosen && !formData.tableHeadConfig) {
          keys.push(data.key);
        }
        mData.push(data);
      }
      mockData.value = mData;
      formData.fieldIds = keys;
      // targetKeys.value = keys;
      //多表字段对照
      if(formData.multipleTables === '1'){
        getTableFieldData(res);
        //获取对照数据
        getConditionData();
      }
    }else{
      mockData.value = mData;
      // targetKeys.value = keys;
      formData.fieldIds = keys;
    }
  }


  const formRef = ref();
  const useForm = Form.useForm;
  const emit = defineEmits(['register', 'ok' ,'close']);
  const formData = reactive<Record<string, any>>({
      id: '',
      fieldIds:[],
      correlation: '1'
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  //表单验证
  const validatorRules = {};
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
    const { departObject } = props;
    formData.tableId = departObject.tableId; //自定义报表id
    formData.allocationTableId = departObject.allocationTableId; //分配报表主表id
    formData.tableHeadConfig = departObject.tableHeadConfig?departObject.tableHeadConfig:false;
    formData.multipleTables = departObject.multipleTables;
    formData.tableNameStrs = departObject.tableNameStrs;
    formData.tableNames = departObject.tableName;
    //获取字段
    getFieldData();

    if(formData.multipleTables === '1'){
      //设置多表条件选项
      setTableCondition();
      //查询条件
      addCondition();
    }
    
  });

  /**
   * 提交数据
   */
  async function submitForm() {
    // 触发表单验证
    confirmLoading.value = true;
    if(formData.fieldIds && formData.fieldIds.length > 0){
      formData["fields"] = formData["fieldIds"].join(',');
    }else{
      confirmLoading.value = false;
      createMessage.warning("请先分配字段");
      return false;
    }
    if(formData.multipleTables === '1'){
      //检验条件是否为空
      if(conditionAllArray.value && conditionAllArray.value.length > 0 && isAllPropertiesNotEmpty(conditionAllArray.value)){
        //多表
        //判断或添加是否都添加了分组
        const exists = conditionAllArray.value.some(item => 
          item.relationship === "2" && 
          (item.groupCode === undefined || item.groupCode === null || item.groupCode === '')
        );
        if(exists){
          createMessage.warning("存在或(or)条件未分配分组");
          confirmLoading.value = false;
          return false;
        }

        const newArray = conditionAllArray.value.map(item => {
          if (item.id && item.id.includes('temp_')) {
            return {
              ...item,
              id: '' // 将包含 'temp_' 的 id 设为空
            };
          }
          return { ...item }; // 返回新对象，保持原对象不变
        });
        formData.conditionJson = JSON.stringify(newArray)
      }else{
        createMessage.warning("请先配置关联条件");
        confirmLoading.value = false;
        return false;
      }
    }


    await saveOrUpdate(formData, false)
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
  //检验条件是否为空
  function isAllPropertiesNotEmpty(arr) {
    const specifiedProps = ['tableName', 'tableField', 'correlationTableName', 'correlationTableField'];
    return arr.every(obj => {
      return !specifiedProps.some(prop => {
        const value = obj[prop];
        return value === null || value === undefined || value === '';
      });
    });
  }


  //表头设置-分配
  function allocate(){
    confirmLoading.value = true;
    let fieldArr = [];
    if(formData.fieldIds && formData.fieldIds.length > 0){
      // 得到具体字段名称和字段Code
      fieldArr = getMatchingObjects(mockData.value, formData["fieldIds"])
    }else{
      confirmLoading.value = false;
      createMessage.warning("请先分配字段");
      return false;
    }
    emit('ok',fieldArr);
  }
  //获取选中的字段集合
  function getMatchingObjects(objects, ids) {
    // 先将id数组转为Set，提高查找效率
    const idSet = new Set(ids);
    return objects.reduce((result, obj) => {
      if (idSet.has(obj.key)) {
        result.push(obj);
      }
      return result;
    }, []);
  }
  //关闭
  function onClose(){
    emit('close');
  }
  //移除
  function removeCondition(id){
    conditionAllArray.value = conditionAllArray.value.filter(
      item => item.id !== id
    );
  }
  //选择报表后触发
  function handleChange(item, type){
    if(type === '1'){
      item.tableField = undefined;
      item.fieldArray = tableFieldArray.value[item.tableName];
    }else{
      item.correlationTableField = undefined;
      item.coorFieldArray = tableFieldArray.value[item.correlationTableName];
    }
    
  }
  function handleFilterOption(){

  }
  //添加条件
  function addCondition(){
    conditionAllArray.value.push({
      id: 'temp_' + buildUUID(),
      tableName: undefined,
      tableField: undefined,
      correlationTableName: undefined,
      correlationTableField: undefined,
      fieldArray: [],
      coorFieldArray: [],
      relationship: '1'
    });
  }
  //设置多表条件选项
  function setTableCondition(){
    
    if(formData.tableNameStrs && formData.tableNames){
      //
      let tables = formData.tableNameStrs.split(',');
      let tableNames = formData.tableNames.split(',');

      for(let i = 0; i < tables.length ;i++){
        tableArray.value.push({
          name: tables[i],
          tableName: tableNames[i]
        });
      }

    }
  }
  //获取表字段对照
  function getTableFieldData(res){
    tableFieldArray.value = res.reduce((acc, item) => {
      const tableName = item.tableName;
      
      if (!acc[tableName]) {
          acc[tableName] = [];
      }
      
      acc[tableName].push({
        name: item.description,
        fieldName: item.field
      });
      
      return acc;
    }, {});
  }
  //获取多表对照(关联条件)
  async function getConditionData(){
    let res = await getTableCondition({id:formData.allocationTableId});
    if(res){
      conditionAllArray.value = res;
      conditionAllArray.value.forEach(item => {
        if(item.correlation === '2'){
          formData.correlation = item.correlation;
        }
        item.fieldArray = tableFieldArray.value[item.tableName];
        item.coorFieldArray = tableFieldArray.value[item.correlationTableName];
        if(!item.relationship){
          item.relationship = '1';
        }
      });
    }
  }
  defineExpose({
      submitForm,
  });
</script>

<style lang="less" scoped>
.tree-transfer .ant-transfer-list:first-child {
  width: 50%;
  flex: none;
}
</style>