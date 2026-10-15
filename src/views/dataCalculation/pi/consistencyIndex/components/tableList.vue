<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable" @row-click="handleRowClick">
      <template #recalculateTimePiSlot="{ record,  text }">
        <a-button v-if="currentRowId != record.id" @click.stop="handleFieldClick(record)" type="link" >{{ text ?? '未设置' }}</a-button>
        <a-select v-else v-model:value="record.recalculateTimePi" 
          :ref="(el) => setSelectRef(record.id, el)"
          :options="getOptions(record)" 
          showSearch
          :labelInValue="true"
          optionFilterProp="label"
          :onChange="(val) => handleChange(val, record)"
          @blur="handleBlur"
        >
        </a-select>
      </template>
    </BasicTable>
  </div>
</template>

<script lang="ts" name="consistencyTable" setup>
import { nextTick, reactive, ref } from 'vue';
import { BasicTable } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage';
import { allocationTableList, queryPiList } from '../index.api';
//查询参数
const queryParam = reactive<any>({});
//pi报表字段
const piFieldList = ref<any[]>([]);
//当前操作行
const currentRowId = ref<any>('');
// 用于存储每个 select 的 ref
const selectRefs = new Map();
//接口数据
const allListData = ref<any[]>([]);
//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    api: allocationTableList,
    immediate: false,
    canResize: true,
    pagination: false,
    showActionColumn: false,
    ellipsis: false,
    showTableSetting: false,
    columns: [
      {
        title: '报表名称',
        align: "center",
        dataIndex: 'name',
        width:80,
      },
      {
        title: '查询字段',
        align: "center",
        dataIndex: 'recalculateTimePi_dictText',
        width:80,
        slots: { customRender: 'recalculateTimePiSlot' },
      },
    ],
    beforeFetch: (params) => {
      return Object.assign(params, queryParam);
    },
  },
})

const [registerTable, { setTableData, getDataSource, setLoading, getRawDataSource }, {  }] = tableContext;
// 设置 ref 的函
const setSelectRef = (id, el) => {
  if (el) {
    selectRefs.set(id, el);
  } else {
    selectRefs.delete(id); // 组件销毁时清理
  }
};
//行点击事件
function handleRowClick(record){
  if(currentRowId.value != record.id){
    handleBlur();
  }
}
//字段击事件
async function handleFieldClick(record){
  currentRowId.value = record.id;
  // 等待 DOM 更新，确保 select 已渲染
  await nextTick(); 
  const selectInstance = selectRefs.get(record.id);
  if (selectInstance && typeof selectInstance.focus === 'function') {
    selectInstance.focus();
  }
}

//获取下拉框数据
function getOptions(record){
  let options = [] as any;
  piFieldList.value.forEach((item) => { 
    if(record.id == item.tableId){
      options.push({
        label: item.name,
        value: item.fieldName,
      })
    }
  })
  return options;
}
//下拉改变事件
function handleChange(val, record){
  record.recalculateTimePi = val.key;
  record.recalculateTimePi_dictText = val.label;
}
//失去焦点事件
function handleBlur(){
  currentRowId.value = '';
}
async function reloadData(formModel){
  setLoading(true);
  await nextTick();
  let tableDta = [] as any; 
  //如果部门不同，重新接口查询数据
  if(queryParam.deptCode != formModel.deptCode){
    queryParam.deptCode = formModel.deptCode;
    queryParam.pageSize = 200;
    // 查询所有PI报表
    const { records, total } = await allocationTableList(queryParam);
    if(total && total > 0){
      // 转为逗号拼接的报表d
      const tableIds = records.map(record => record.id).join(',');
      // 查询PI时间类型的字段
      const fieldData = await queryPiList({tableId: tableIds, type: 6, pageSize: 999}); 
      piFieldList.value = fieldData.records;

      records.forEach(record => {
        if(record.recalculateTimePi){
          // 获取字段
          const field = piFieldList.value.find(item => (item.tableId == record.id && item.fieldName == record.recalculateTimePi));
          if(field){
            record.recalculateTimePi_dictText = field.name;
          }
        }
      });
      tableDta = records;
      allListData.value = records;
    }
  } else {
    console.log('formModel :>> ', formModel);
    if((!formModel.lineCode || formModel.lineCode.length == 0) && (!formModel.tableIds || formModel.tableIds.length == 0)){
        tableDta = allListData.value;
    }else{
      allListData.value.forEach(record => {
        if(!formModel.tableIds || formModel.tableIds.length == 0){
          if(formModel.lineCode && formModel.lineCode.includes(record.lineCode)){
            tableDta.push(record);
          }
        } else {
          if(formModel.tableIds && formModel.tableIds.includes(record.id)){
            tableDta.push(record);
          }
        }
      });
    }
  }
  setTableData(tableDta);
  setLoading(false);
  return true;
}

function getQueryFieldList(){
  let queryField = {};
  getDataSource().forEach(item => {
    if(item.recalculateTimePi){
      queryField[item.id] = item.recalculateTimePi;
    }
  });
  return JSON.stringify(queryField);
}

//暴露方法
defineExpose({
  reloadData,
  getQueryFieldList,
})
</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}
:deep(.ant-table-wrapper .ant-table.ant-table-small) {
  .ant-table-footer
  {
    padding: 8px 0px !important;
  }
}
</style>