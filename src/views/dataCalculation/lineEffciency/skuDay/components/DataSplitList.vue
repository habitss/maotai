<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <div style="width: 99%;margin: 5px auto 2px auto;">
          <a-descriptions title="" bordered size="small">
            <a-descriptions-item label="产线">{{ formData.lineName }}</a-descriptions-item>
            <a-descriptions-item label="日期">{{ formData.dateDay }}</a-descriptions-item>
            <!-- <a-descriptions-item label="班次">{{ formData.classtime }}</a-descriptions-item> -->
            <a-descriptions-item label="SKU">{{ formData.skuName }}</a-descriptions-item>
            
            <a-descriptions-item label="开始时间"><span style="color:red">{{ formData.startTime }}</span></a-descriptions-item>
            <a-descriptions-item label="结束时间"><span style="color:red">{{ formData.endTime }}</span></a-descriptions-item>
            <a-descriptions-item label="产量">{{ formData.outputValue }}</a-descriptions-item>
          </a-descriptions>
        </div>
      
        <div>
           <!--引用表格-->
          <BasicTable @register="registerTable">
           <template #tableTitle>
              <a-button type="primary" @click="handleAdd" preIcon="ant-design:plus-outlined"> 新增</a-button>
            </template>
              <!--操作栏-->
              <template #action="{ record }">
                <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)"/>
              </template>
           </BasicTable> 
        </div>
      </template>
    </JFormContainer>
  </a-spin>
  <!--拆分表单-->
  <j-modal :title="title" :width="width" :max-height="320" style="margin-top: 50px;" :visible="visible" @ok="handleOk" :okButtonProps="{ class: { 'jee-hidden': disableSubmit } }" @cancel="handleCancel" cancelText="关闭">
    <DataSplitForm ref="registerForm" @ok="submitCallback" :formDisabled="disableSubmit" :formBpm="false" style="height: 400px;"></DataSplitForm>
  </j-modal>
</template>

<script lang="ts" setup>
  import { ref, reactive, defineExpose, nextTick, defineProps, computed, onMounted } from 'vue';
  import { useListPage } from '/@/hooks/system/useListPage';
  import JModal from '/@/components/Modal/src/JModal/JModal.vue';
  import { BasicTable, useTable, TableAction} from '/@/components/Table';
  import { defHttp } from '/@/utils/http/axios';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getValueType } from '/@/utils';
  import { splitColumns } from '../SkuDay.data';
  import { getExportUrl,dataSplit } from '../SkuDay.api';
  import { Form } from 'ant-design-vue';
  import JFormContainer from '/@/components/Form/src/container/JFormContainer.vue';
  import DataSplitForm from './DataSplitForm.vue'
  const props = defineProps({
    formDisabled: { type: Boolean, default: false },
    formData: { type: Object, default: () => ({})},
    formBpm: { type: Boolean, default: true }
  });
  const width = ref<number>(800);
  const disableSubmit = ref<boolean>(false);
  const visible = ref<boolean>(false);
  const formRef = ref();
  const title = ref('');
  const registerForm = ref();
  const useForm = Form.useForm;
  const emit = defineEmits(['register', 'ok']);
  import dayjs from 'dayjs';
  const bodyStyle = ref({
    height:"50px"
  });
  const formData = reactive<Record<string, any>>({
    id: '',
    lineCode: '',
    lineName: '',   
    dateDay: '',
    classDes: '',
    skuCode: '',
    skuName: '',
    skuSpeed: '',
    type: '',
    speedOp: '',
    startTime: '',   
    endTime: '',
    outputValue: '',
    outputCode: '',
  });

  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  const isDateTime = ref<boolean>(false);
  const queryParam = reactive<any>({});
  //表单验证
  const validatorRules = reactive({});
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
      title: 'OPI拆分',
      // api: null,
      columns: splitColumns,
      canResize:false,
      useSearchForm: false,
      showActionColumn: true,
      showIndexColumn: true,
      beforeFetch: async (params) => {
        return Object.assign(params, queryParam);
      },
    },
    exportConfig: {
      name: "OPI拆分",
      url: getExportUrl,
      params: queryParam,
    }
  });

  const [registerTable, { insertTableDataRecord,deleteTableDataRecord,updateTableDataRecord,getDataSource,setTableData }, { rowSelection, selectedRowKeys }] = tableContext;


  /**
   * 新增
   */
  // function add() {
  //   edit({});
  // }

  /**
   * 编辑
   */
  function edit(record) {
    setTableData([]);
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
      if(tmpData.id){
        isDateTime.value = true;
      }
    });
  }

  /**
   * 提交数据
   */
  async function submitForm() {
    //判断是否拆分完成
    //获取表格数据
    let rows = getDataSource();
    //设置限制时间段
    if(rows && rows.length>0){
      //上一个拆分的结束时间
      let row = rows[rows.length-1];
      if(!dayjs(formData.endTime).isSame(dayjs(row.endTime))){
        //分配完成
        createMessage.warning("时间还未全部拆分, 请先完成拆分");
        return false;
      }
    }else{
      createMessage.warning("时间还未拆分, 请先完成拆分");
      return false;
    }

    confirmLoading.value = true;
    
    for (const item of rows) {
      item.id = null;
      item.lineCode = formData.lineCode;
      item.lineName = formData.lineName;
      item.dateDay = formData.dateDay;
      item.classDes = formData.classDes;
      item.type = formData.type;
      item.speedOp = formData.speedOp;
      item.mainId = formData.id;
    }
  
    await dataSplit(rows)
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

  //添拆分数据
  function handleAdd(){
    
    //获取表格数据
    let rows = getDataSource();
    let obj = {};
    //设置限制时间段
    if(rows && rows.length>0){
      //上一个拆分的结束时间
      let row = rows[rows.length-1];
      if(dayjs(formData.endTime).isSame(dayjs(row.endTime))){
        //分配完成
        createMessage.warning("时间已经拆分完成,确认无误后,点击[确认]完成拆分");
        return false;
      }
      obj["startTime"] = row.endTime;
    }else{
      //新增的首条数据开始时间取原始数据的开始时间
      obj["startTime"] = formData.startTime;
    }
    obj["lineCode"] = formData.lineCode;
    obj["lineName"] = formData.lineName;
    obj["skuCode"] = formData.skuCode;
    obj["skuName"] = formData.skuName;
    obj["skuSpeed"] = formData.skuSpeed;
    obj["outputCode"] = formData.outputCode;
    //原始数据结束时间
    obj["dateMax"] = formData.endTime
    title.value = "拆分("+obj["startTime"]+" | "+obj["dateMax"]+")"
    visible.value = true;
    nextTick(() => {
      registerForm.value.add(obj);
    })
    
  }
  //编辑
  function handleEdit(record){
    visible.value = true;
    title.value = "拆分-编辑"
    registerForm.value.edit(record);
  }

  /**
   * 确定按钮点击事件
   */
  function handleOk() {
    registerForm.value.submitForm();
  }

  /**
   * 取消按钮回调事件
   */
  function handleCancel() {
    visible.value = false;
  }

  /**
   * form保存回调事件
   */
  function submitCallback(data, mode) {
    
    let tableData = JSON.parse(JSON.stringify(data))
    if(mode === 'add'){
      addTableData(tableData)
    }else{
      updateTableData(tableData)
    }
    visible.value = false;
  }
  /**
   * 新增数据
   */
  async function addTableData(record){
    await insertTableDataRecord(record)
  }
  /**
   * 删除指定行数据
   * @param record 
   */
  function handleDelete(record){
    deleteTableDataRecord(record.id)
  }
  /**
   * 更新数据
   */
  function updateTableData(record){
    updateTableDataRecord(record.id, record)
  }


  /**
   * 操作栏
   */
  function getTableAction(record) {
    return [
       {
        label: '编辑',
        onClick: handleEdit.bind(null, record)
      },{
        label: '删除',
        popConfirm: {
          title: '是否确认删除',
          confirm: handleDelete.bind(null, record),
          placement: 'topLeft',
        },
        auth: 'line:tb_pkg_class_sku_detail_op:delete'
      }
    ];
  }

  function getDropDownAction(record) {
    return []
  }

  defineExpose({
    // add,
    edit,
    submitForm,
  });
</script>

<style lang="less" scoped>
  .antd-modal-form {
    padding: 14px;
  }
</style>
