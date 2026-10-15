<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="PiExtractTableForm">
          <a-row>
						<a-col :span="12">
							<a-form-item label="来源表名称" v-bind="validateInfos.sourceTable" id="PiExtractTableForm-sourceTable" name="sourceTable">
								<!-- <a-input v-model:value="formData.sourceTable" placeholder="请输入数据来源表名称"  allow-clear ></a-input> -->
                <a-select v-model:value="formData.sourceTable" :options="tableArr" placeholder="请选择来源表名称" :field-names="{ label: 'tableComment', value: 'tableName' }" @change="tableChange" allowClear :disabled="isEd"></a-select>
              </a-form-item>
						</a-col>
						<a-col :span="12">
							<a-form-item label="来源表">
								<a-input v-model:value="formData.sourceTable" placeholder="请输入数据来源表"  allow-clear disabled></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="12">
							<a-form-item label="目标表名称" v-bind="validateInfos.targetTable" id="PiExtractTableForm-targetTable" name="targetTable">
								<!-- <a-input v-model:value="formData.targetTable" placeholder="请输入数据目标表名称"  allow-clear ></a-input> -->
                <a-select v-model:value="formData.targetTable" :options="targetTableArr" placeholder="请输入目标表名称" :field-names="{ label: 'tableComment', value: 'tableName' }" @change="targetTableChange(true)" allowClear :disabled="isEd"></a-select>
              </a-form-item>
						</a-col>
						<a-col :span="12">
							<a-form-item label="目标表">
								<a-input v-model:value="formData.targetTable" placeholder="请输入数据目标表"  allow-clear disabled></a-input>
							</a-form-item>
						</a-col>
          </a-row>
        </a-form>
        
        <!--字段-->
        <div>
           <!--引用表格-->
          <BasicTable @register="registerTable">
            <template #tableTitle>
              <!-- <a-button type="primary" @click="handleAdd" preIcon="ant-design:plus-outlined"> 新增</a-button> -->
            </template>
            <!--操作栏-->
            <template #action="{ record }">
              <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)"/>
            </template>
            
            <!--目标表字段-->
            <template #targetField="{ column, record }">
                <a-select v-model:value="record.targetField" :options="targetTableFieldArr" placeholder="请选择来源表名称" :field-names="{ label: 'columnComment', value: 'columnName' }" allowClear></a-select>
            </template>
           </BasicTable> 
        </div>
      </template>
    </JFormContainer>
  </a-spin>
</template>

<script lang="ts" setup>
  import { ref, reactive, defineExpose, nextTick, defineProps, computed, onMounted } from 'vue';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { defHttp } from '/@/utils/http/axios';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicTable, useTable, TableAction} from '/@/components/Table';
  import { getValueType } from '/@/utils';
  import { saveOrUpdate,getExportUrl,getTableList,fieldList,targetTable,targetTableField } from '../PiExtractTable.api';
  import { fieldColumns } from '../PiExtractTable.data';
  import { Form } from 'ant-design-vue';
  import JFormContainer from '/@/components/Form/src/container/JFormContainer.vue';
  const props = defineProps({
    formDisabled: { type: Boolean, default: false },
    formData: { type: Object, default: () => ({})},
    formBpm: { type: Boolean, default: true }
  });
  const formRef = ref();
  const useForm = Form.useForm;
  const emit = defineEmits(['register', 'ok']);
  const formData = reactive<Record<string, any>>({
    id: '',
    tenantId: undefined,
    factoryCode: '',   
    deptCode: '',   
    lineCode: '',   
    sourceTable: '',   
    sourceTableName: '',   
    targetTable: '',   
    targetTableName: '',   
    tableId: undefined,
    sort: undefined,
    delFlag: undefined,
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  const queryParam = reactive<any>({})
  //表单验证
  const validatorRules = reactive({
    sourceTable: [{ required: true, message: '请选择来源表名称!'},],
    targetTable: [{ required: true, message: '请选择目标表名称!'},]
  });
  const isEd = ref(false);
  //来源表
  const tableArr = ref([]);
  //目标表
  const targetTableArr = ref([]);
  //目标表
  const targetTableFieldArr = ref([]);

  // 目标表字段
  const targetFieldArr = ref([
    {"targetField":"f_001","targetFieldName":"字段1"},
    {"targetField":"f_002","targetFieldName":"字段2"}
  ])

  const { resetFields, validate, validateInfos } = useForm(formData, validatorRules, { immediate: false });

  // 表单禁用
  const disabled = computed(()=>{
    if(props.formBpm === true){
      if(props.formData.disabled === false){
        return false;
      }else{
        return true;
      }
    }
    return props.formDisabled;
  });

  
  //注册table数据
  const { prefixCls, tableContext, onExportXls, onImportXls } = useListPage({
    tableProps: {
      title: '数据抽取配置字段',
      api: fieldList,
      columns: fieldColumns,
      canResize:false,
      useSearchForm: false,
      showActionColumn: true,
      showIndexColumn: true,
      showTableSetting: false,
      showActionColumn:false,
      beforeFetch: async (params) => {
        return Object.assign(params, queryParam);
      },
    },
    exportConfig: {
      name: "数据抽取配置字段",
      url: getExportUrl,
      params: queryParam,
    }
  });

  const [registerTable, {reload, insertTableDataRecord,deleteTableDataRecord,updateTableDataRecord,getDataSource,setTableData }, { rowSelection, selectedRowKeys }] = tableContext;

  
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
        if(record.hasOwnProperty(key)){
          tmpData[key] = record[key]
        }
      })
      //赋值
      Object.assign(formData, tmpData);
    });
    formData.sourceTable = "";
    queryParam.sourceTable = "";
    tableArr.value= []
    targetTableArr.value= []
    targetTableFieldArr.value = []
    reload();
    if(record.id){
      isEd.value = true;
      //编辑-获取目标字段
      targetTableChange(false)
    }else{
      isEd.value = false;
    }
    setTimeout(() => {
      
      //获取表
      getTable();
      //获取目标表
      getTargetTable();
    }, 800);
    
  }

    /**
   * 操作栏
   */
  function getTableAction(record) {
    return [
      //  {
      //   label: '编辑',
      //   onClick: handleEdit.bind(null, record)
      // },{
      //   label: '删除',
      //   popConfirm: {
      //     title: '是否确认删除',
      //     confirm: handleDelete.bind(null, record),
      //     placement: 'topLeft',
      //   },
      //   auth: 'line:tb_pkg_class_sku_detail_op:delete'
      // }
    ];
  }

  function getDropDownAction(record) {
    return []
  }

  /**
   * 提交数据
   */
  async function submitForm() {
    try {
      // 触发表单验证
      await validate();
    } catch ({ errorFields }) {
      return false;
    }
    confirmLoading.value = true;
    // const isUpdate = ref<boolean>(false);
    // //时间格式化
    // let model = formData;
    // if (model.id) {
    //   isUpdate.value = true;
    // }


    //获取表格数据
    let rows = getDataSource();
    let fields = [];
    //循环数据
    rows.forEach(data => {
      if (data.sourceField && data.targetField) {
        fields.push(data);
      }
    });

    let obj = {
      id: formData.id,
      sourceTable: formData.sourceTable,
      targetTable: formData.targetTable,
      sourceTableName: formData.sourceTableName,
      targetTableName: formData.targetTableName,
      fields: fields
    }
    await saveOrUpdate(obj)
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

  /**获取表 */
  async function getTable(){
    await getTableList(formData)
      .then((res) => {
        if (res.success) {
          tableArr.value = res.result
        } else {
          createMessage.warning(res.message);
        }
      })
      .finally(() => {});
  }
  /**
   * 选中来源表后获取字段 
   */
  function tableChange(){
    queryParam.sourceTable = formData.sourceTable
    reload();
    //获取名称
    const column = tableArr.value.find(item => item.tableName === formData.sourceTable);
    formData.sourceTableName = column.tableComment ;
  }


    /**
   * 获取目标表 
   */
  async function getTargetTable(){
    await targetTable(formData)
      .then((res) => {
        if (res.success) {
          targetTableArr.value = res.result
        } else {
          createMessage.warning(res.message);
        }
      })
      .finally(() => {});
  }

  /**
   * 选中目标表后获取字段 
   */
  async function targetTableChange(isRelad){
    if(isRelad){
      reload()
      const column = targetTableArr.value.find(item => item.tableName === formData.targetTable);
      formData.targetTableName = column.tableComment ;
    }
    targetTableFieldArr.value = []
    await targetTableField({"targetTable": formData.targetTable})
      .then((res) => {
        if (res.success) {
          targetTableFieldArr.value = res.result
        } else {
          createMessage.warning(res.message);
        }
      })
      .finally(() => {});
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
  :deep(.jeecg-basic-table .ant-table-wrapper .ant-table-title){
    min-height: 0px;
  }
</style>
