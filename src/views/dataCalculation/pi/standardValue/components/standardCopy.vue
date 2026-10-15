<template>
  <a-spin :spinning="confirmLoading" class="span-box">
    <div>
      <!--主表信息显示-->
      <div class="jeecg-basic-table-form-container">
        <a-form :label-col="labelCol" :wrapper-col="wrapperCol" labelAlign="left">
          <a-row :gutter="24">
            <a-col :lg="24">
              <a-form-item label="报表名称">
                <a-input v-model:value="tableData.name" disabled/>
              </a-form-item>
            </a-col>
            <a-col :lg="24">
              <a-form-item label="源字段">
                <a-select v-model:value="tableData.sourceField" show-search option-filter-prop="label" :options="configFieldList" placeholder="请选择" allowClear @change="getTargetField"></a-select>
              </a-form-item>
            </a-col>
            <a-col :lg="24">
              <a-form-item label="目标字段">
                <a-select v-model:value="tableData.targetField" show-search option-filter-prop="label" :options="targetFieldList" placeholder="请选择" allowClear @change="setTargetFieldName"></a-select>
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
      </div>
    </div>
  </a-spin>
</template>

<script lang="ts" name="pi-piCustomizeTableField" setup>
  import { ref, reactive, onMounted, unref } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getStandardFieldByTableId,copyStandard } from '../standardValue.api';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();
  const confirmLoading = ref<boolean>(false);
  const tableData = ref<any>({});
  const algorithmModal = ref();

  //字段集合(配置了标准)
  const configFieldList = reactive<Record<string, any>>([]);
  //目标字段集合(排查源字段)
  const targetFieldList = reactive<Record<string, any>>([]);
  //字段集合
  const fieldList = ref([]);

  const { createMessage } = useMessage();
  const emit = defineEmits(['register', 'ok','close']);

  const labelCol = reactive({
    xs: { span: 24 },
    sm: { span: 5 },
  });
  const wrapperCol = reactive({
    xs: { span: 24 },
    sm: { span: 16 },
  });

  /**
   * 提交数据
   */
  async function submitForm() {
    confirmLoading.value = true;
    // 触发表单验证
    if(!tableData.value.sourceField){
      createMessage.warning("源字段不为空");
      confirmLoading.value = false;
      return false;
    }
    if(!tableData.value.targetField){
      createMessage.warning("目标字段不为空");
      confirmLoading.value = false;
      return false;
    }

    await copyStandard(tableData.value)
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

  //设置参数
  function setProps(record){
    tableData.value.tableId = record.id;
    tableData.value.name = record.name;
    //获取源字段(配置标准)
    getField();
  }
  //获取
  async function getField(){
    let res = await getStandardFieldByTableId({tableId: tableData.value.tableId});
    fieldList.value = res;
    let standardFieldArr = res
    .filter(item => item.isStandard !== '0')
    .map(item => {
        return { 
          label:item.name,
          value:item.fieldName}
        }
    ).filter(Boolean);
    configFieldList.push(...standardFieldArr);
    
    let fieldArr = res.map(item => {
        return { 
          label:item.name,
          value:item.fieldName}
        }
    ).filter(Boolean);
    targetFieldList.push(...fieldArr);
    
    
  }
  //排除选中的源字段
  function getTargetField(val){
    targetFieldList.length = 0;
    tableData.value.targetField = undefined;
    tableData.value.targetFieldName = "";
    let fieldArr = fieldList.value
    .filter(item => item.fieldName !== val)
    .map(item => {
        return { 
          label:item.name,
          value:item.fieldName}
        }
    ).filter(Boolean);
    targetFieldList.push(...fieldArr);
  }
  //获取目标名称
  function setTargetFieldName(val){
    tableData.value.targetFieldName = getNameByFieldName(val);
  }

  function getNameByFieldName(val) {
    const found = targetFieldList.find(item => item.value === val);
    return found ? found.label : null;
  }
  //暴露方法
  defineExpose({
    setProps,
    submitForm
  })
</script>

<style lang="less" scoped>
.jeecg-basic-table-form-container {
  .table-page-search-submitButtons {
    display: block;
    margin-bottom: 24px;
    white-space: nowrap;
  }
  .query-group-cust {
    width: calc(50% - 15px);
    min-width: 100px !important;
  }
  .query-group-split-cust {
    width: 30px;
    display: inline-block;
    text-align: center;
  }
}
//@import '../common.less';
</style>