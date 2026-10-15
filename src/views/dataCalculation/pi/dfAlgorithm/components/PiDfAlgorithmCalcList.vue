<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button type="primary" @click="handleAdd" preIcon="ant-design:plus-outlined"> 新增</a-button>
      </template>
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)"/>
      </template>
      
      <template v-slot:headerCell="{ column }">
        <div>{{ column.customTitle }}</div>
        <template v-if="column.dataIndex == 'resultType'">
          <span class="margin-left-10" style="color:red">
            <ExclamationCircleOutlined />&ensp;仅适用时间类型PI计算
          </span>
        </template>
        <template v-if="column.dataIndex == 'sinicizedKey' || column.dataIndex == 'sinicizedValue'">
          <span class="margin-left-10" style="color:red">
            <ExclamationCircleOutlined />&ensp;汉化方式为主数据时填写
          </span>
        </template>
      </template>
      <!--字段回显插槽-->
      <template v-slot:bodyCell="{ column, record }">
        <a-select
          v-if="column.dataIndex == 'piFieldName'"
          v-model:value="record.piFieldName"
          placeholder="请选择PI字段"
          style="margin-top: 4px; text-align: left;"
          allow-clear
          :options="piFieldList"
          :fieldNames= "{label:'name', value: 'fieldName' }"
          :filterOption = "(input, option) => filterOption(input, option, {label:'name', value: 'fieldName' })"
          showSearch
        ></a-select>

        <a-select
          v-if="column.dataIndex == 'dfFieldName'"
          v-model:value="record.dfFieldName"
          placeholder="请选择DF字段"
          style="margin-top: 4px; text-align: left;"
          allow-clear
          :options="dfFieldList"
          :fieldNames= "{label:'dfFieldDesc', value: 'dfFieldName' }"
          :filterOption = "(input, option) => filterOption(input, option, {label:'dfFieldDesc', value: 'dfFieldName' })"
          showSearch
        ></a-select>

        <a-switch v-if="column.dataIndex == 'ifCover'" v-model:checked="record.ifCover" defaultChecked="1"
          checked-children="是" checkedValue="1" un-checked-children="否" unCheckedValue="0" />

        <a-select
          v-if="column.dataIndex == 'ifSinicized'"
          v-model:value="record.ifSinicized"
          placeholder="请选择汉化方式"
          style="margin-top: 4px; text-align: left;"
          :options="sinicizedOptions"
          showSearch
          @change="(selectValue) => handleSinicizedChange(selectValue, record)"
        ></a-select>

        <a-select 
          v-if="column.dataIndex == 'sinicizedDic' && record.ifSinicized != '0'"
          show-search 
          v-model:value="record.sinicizedDic" 
          :options="record.ifSinicized == '1' ? dictOptions : mdmTableNameList" 
          placeholder="请选择汉化模型"
          @change="(selectValue) => handleSinicizedTableChange(selectValue, record)"
        ></a-select>

        
        <ApiSelect v-if="(column.dataIndex == 'sinicizedKey' || column.dataIndex == 'sinicizedValue') && record.ifSinicized == '2' && record.sinicizedDic"
          v-model:value="record[column.dataIndex]"
          :api="queryMdmTableFieldName"
          :params="{
            tableName: record.sinicizedDic,
          }"
          show-search
          placeholder="请选择"
        ></ApiSelect>
        <!-- <a-select 
          v-if="column.dataIndex == 'sinicizedKey' && record.ifSinicized == '2'"
          show-search 
          option-filter-prop="label" 
          v-model:value="record.sinicizedKey" 
          :options="mdmTableFieldNameList" 
          placeholder="请选择汉化主键字段"
        ></a-select> -->
        
        <!-- <a-select 
          v-if="column.dataIndex == 'sinicizedValue' && record.ifSinicized == '2'"
          show-search 
          option-filter-prop="label" 
          v-model:value="record.sinicizedValue" 
          :options="mdmTableFieldNameList" 
          placeholder="请选择汉化取值字段"
        ></a-select> -->

        <a-select 
          v-if="column.dataIndex == 'resultType'"
          show-search 
          option-filter-prop="label" 
          v-model:value="record.resultType" 
          :options="resultTypeArr" 
          placeholder="请选择"
        ></a-select>
      </template>
    </BasicTable>
  </div>
</template>

<script lang="ts" name="pi-piDfTableFieldMapping" setup>
import { ref, reactive } from 'vue';
import { BasicTable, TableAction } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage'
import { calcModelColumns } from '../PiDfAlgorithm.data';
import { listAllCalcModel, deleteCalcModel, queryMdmTableFieldName, queryMdmTableName } from '../PiDfAlgorithm.api';
import { buildUUID } from '/@/utils/uuid';
import { useMessage } from "/@/hooks/web/useMessage";
import { getDictionary } from '../../customizeTable/PiAlgorithm.api';
import { filterOption } from '/@/utils';
import { ExclamationCircleOutlined } from '@ant-design/icons-vue';
import ApiSelect from '/@/components/Form/src/components/ApiSelect.vue';
//消息提示组件
const { createMessage } = useMessage();
const queryParam = reactive<any>({});
//汉化方式options
const sinicizedOptions = [
  {
    label: '不汉化',
    value: '0'
  },
  {
    label: '字典表',
    value: '1'
  },
  {
    label: '主数据表',
    value: '2'
  },
]
//结果类型
const resultTypeArr = reactive<Record<string, any>>([
  { value: '', label: '请选择' },
  { value: '1', label: '天' },
  { value: '2', label: '小时' },
  { value: '3', label: '分钟' },
  { value: '4', label: '秒' }
]);
//获取字典表数据
const dictOptions = ref([] as any);
//主数据表名列表
const mdmTableNameList = ref([] as any);
//算法基本配置
const baseInfo = ref({} as any);
//pi表字段
const piFieldList = ref([] as any);
//取数表字段
const dfFieldList = ref([] as any);
//取数表公共字段
const dfPublicFieldList = ref([] as any);
//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    title: '计算模型',
    api: listAllCalcModel,
    immediate: false,
    columns: calcModelColumns,
    pagination: false,
    ellipsis: false,
    showTableSetting: false,
    scroll: { y: 300 },
    actionColumn: {
      width: 120,
      fixed: 'right'
    },
    defSort: {
      column: 'id',
      order: 'desc',
    },
    beforeFetch: (params) => {
      params.dataId = baseInfo.value.id ?? '-1';
      return Object.assign(params, queryParam);
    },
    afterFetch: (res) => {
      const resFieldMap = {};
      res.forEach((item, i) => {
        if (item.dfFieldName) {
          item.itemIndex = i;
          resFieldMap[item.dfFieldName] = item;
        }
      });
      dfPublicFieldList.value.forEach(item => {
        if (!resFieldMap[item.dfFieldName]) {
          res.push({
            id: 'temp_' + buildUUID(),
            piTableName: baseInfo.value.piTableName,
            dfTableName: baseInfo.value.dataTableName,
            ifCover: '1',
            ifSinicized: '0',
            dfFieldName: item.dfFieldName,
            isPublic: 1,
          });
        }else{
          res[resFieldMap[item.dfFieldName].itemIndex].isPublic = 1;
        }
      });
    },
  },
})

const [registerTable, { reload, setTableData, getDataSource, insertTableDataRecord, deleteTableDataRecord  }] = tableContext

//计算模型-汉化模型
async function getDictionaryData(){
  let res = await getDictionary({});
  let newArr = res.map(item => ({
    label:item.label,
    value:item.value
  }));

  dictOptions.value.push(...newArr);
}

async function handleAdd() {
  if(!baseInfo.value.dataTableName){
    createMessage.warning('请选择取数表！');
    return;
  }
  await insertTableDataRecord({
    id: 'temp_' + buildUUID(),
    piTableName: baseInfo.value.piTableName,
    dfTableName: baseInfo.value.dataTableName,
    ifCover: '1',
    ifSinicized: '0',
  })
}

/**
 * 删除事件
 */
async function handleDelete(record) {
  if(!record.id.includes('temp_')){
    await deleteCalcModel({ id: record.id }, handleSuccess);
  }
  await deleteTableDataRecord(record.id);
}

/**
 * 成功回调
 */
function handleSuccess() {
  reload();
}
/**
 * 操作栏
 */
function getTableAction(record) {
  return [
    {
      label: '删除',
      ifShow: record.isPublic !== 1,
      popConfirm: {
        title: '是否确认删除',
        confirm: handleDelete.bind(null, record),
        placement: 'topLeft',
      },
    }
  ]
}
/**
 * 汉化方式改变事件
 */
async function handleSinicizedChange(selectValue, record){
  record.sinicizedDic = null;
  record.sinicizedKey = null;
  record.sinicizedValue = null;
}
/**
 * 汉化模型改变事件
 */
async function handleSinicizedTableChange(selectValue, record){
  record.sinicizedKey = null;
  record.sinicizedValue = null;
}
/**
 * 初始化数据
 * @param baseInfoData 基础配置
 * @param piFieldListData pi字段数据
 * @param dfFieldListData df字段数据
 */
async function initData(baseInfoData, piFieldListData, dfFieldListData) {
  baseInfo.value = baseInfoData;
  piFieldList.value = piFieldListData;
  dfFieldList.value = dfFieldListData;
  dfPublicFieldList.value = dfFieldListData.filter(item => item.isPublic === 1);
  await getDictionaryData();
  mdmTableNameList.value = await queryMdmTableName({});
}
/**
 * 暴露方法
 */
defineExpose({
  initData,
  getDataSource,
  setTableData,
  reload
});
</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}
</style>