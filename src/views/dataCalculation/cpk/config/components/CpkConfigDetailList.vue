<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable">
      <template #form-formHeader>
        <div class="w-full mb-1">
          <a-button type="primary" v-auth="'cpk:mes3_cpk_config:add'" @click="handleSaveData"
            preIcon="ant-design:plus-outlined">保存</a-button>
        </div>
      </template>
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button class="ml-1" type="primary" v-auth="'cpk:mes3_cpk_config:add'"  @click="handleAddCondition"
          preIcon="ant-design:plus-outlined">新增条件</a-button>
      </template>
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
      <!--字段回显插槽-->
      <template v-slot:bodyCell="{ column, record }">
        <a-select
          v-if="column.dataIndex === 'conditionField'" 
          v-model:value="record[column.dataIndex]"
          placeholder="请选择条件字段"
          :showSearch="true"
          :options="piFieldList"
          optionFilterProp="name"
          :fieldNames="{
            label: 'name',
            value: 'fieldName',
          }"
        >
        </a-select>
        <a-input v-else v-model:value="record[column.dataIndex]" placeholder="请输入条件值" />
      </template>
    </BasicTable>
  </div>
</template>

<script lang="ts" name="mdm3-mdmItem" setup>
import { ref, watch } from 'vue';
import { BasicTable, TableAction } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage'
import { getPiFieldList, piFieldList, detailColumns, detailFormSchema } from '../CpkConfig.data';
import { saveDetail, queryById } from '../CpkConfig.api';
import { useMessage } from '/@/hooks/web/useMessage';
import dayjs from 'dayjs';

//注册Message
const { createMessage: msg } = useMessage();
//父级数据
const parentData = ref();
//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    title: '',
    columns: detailColumns,
    showTableSetting: false,
    canResize: true,
    showActionColumn: true,
    actionColumn: {
      width: 80,
      fixed: 'right'
    },
    pagination: false,
    immediate: false,
    formConfig: {
      schemas: detailFormSchema,
      baseColProps: { span: 12 },
      showActionButtonGroup: false,
      showAdvancedButton: false,
    },
  },
})
const [registerTable, { getDataSource, setTableData, setLoading, setProps, getForm, insertTableDataRecord, deleteTableDataRecord }] = tableContext
//初始化数据
async function initData(parentRecord){
  try{
    setLoading(true);
    parentData.value = parentRecord;
    let res = await queryById(parentRecord);
    if(res.tableCode){
      await getPiFieldList(res.tableCode);
    }else{
      getForm().resetFields();
      getForm().resetSchema(detailFormSchema);
    }
    getForm().setFieldsValue(res);
    setTableData(res?.detailList ?? []);
  } catch (error) {
  } finally {
    setLoading(false);
  }
}
/**
 * 新增条件
 */
async function handleAddCondition() {
  try{
    await getForm().validate();
    await insertTableDataRecord({ 
      id: 'temp_'+dayjs().valueOf().toString(36),
      configId: parentData.value.id, 
    });
  } catch ({ errorFields }) {
    if (errorFields) {
      const firstField = errorFields[0];
      if (firstField) {
        getForm().scrollToField(firstField.name, { behavior: 'smooth', block: 'center' });
      }
    }
    return Promise.reject(errorFields);
  }
}
/**
 * 删除事件
 */
async function handleDelete(record) {
  await deleteTableDataRecord(record.id);
}
/**
   * 操作栏
   */
function getTableAction(record) {
  return [
    {
      label: '删除',
      popConfirm: {
        title: '确定删除吗?',
        confirm: handleDelete.bind(null, record),
        placement: 'topLeft'
      },
      auth: 'cpk:mes3_cpk_config:delete'
    },
  ]
}
//保存数据
async function handleSaveData(){
  try{
    let values = await getForm().validate();
    let detailList = getDataSource();
    if(!detailList || detailList.length == 0){
      msg.error({ content: '请设置条件', key: 'saving' });
      return;
    }
    values.detailList = detailList;
    values.id = parentData.value.id;
    setLoading(true);
    await saveDetail(values);
    await initData({...parentData.value, ...values});
  } catch ({ errorFields }) {
    if (errorFields) {
      const firstField = errorFields[0];
      if (firstField) {
        getForm().scrollToField(firstField.name, { behavior: 'smooth', block: 'center' });
      }
    }
    return Promise.reject(errorFields);
  } finally {
    setLoading(false);
  }
}

watch(
  () => {
    const formInstance = getForm();
    let res = undefined;
    if(formInstance && formInstance.getFieldsValue() && formInstance.getFieldsValue().tableCode){
      res = formInstance.getFieldsValue().tableCode;
    }
    return res;
  },
  async (newTableCode, oldTableCode) => {
    if (newTableCode && newTableCode !== oldTableCode) {
      setTableData([]);
    }
  },
);

//暴露方法
defineExpose({
  setLoading,
  setProps,
  initData
})
</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}
</style>