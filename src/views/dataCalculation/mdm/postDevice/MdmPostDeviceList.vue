<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection" @selection-change="handleSelectChange">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button type="primary" @click="handleAdd" preIcon="ant-design:plus-outlined">提交关联</a-button>
      </template>

      <template #isMain="{ record, text }">
        <a-checkbox :checked="text" @change="handleMainChange(record)"></a-checkbox>
      </template>
    </BasicTable>
  </div>
</template>

<script lang="ts" name="mdm3-mdmPostDevice" setup>
import { reactive, ref } from 'vue';
import { BasicTable } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage'
import { columns, searchFormSchema } from './MdmPostDevice.data';
import { list as deviceList } from '/@/views/dataCalculation/mdm/deviceManage/MdmDevice.api';
import { add } from './MdmPostDevice.api';
import { useMessage } from '/@/hooks/web/useMessage';
const queryParam = reactive<any>({});
const emit = defineEmits(['success']);
//父级传入数据
const parentData = ref();
//所选的主设备id
const lastIndexId = ref();
//注册Message
const { createMessage: msg } = useMessage();
//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    title: '岗位设备关联',
    api: deviceList,
    immediate: false,
    columns,
    canResize: true,
    showActionColumn: false,
    showTableSetting: false,
    pagination: false,
    defSort: {
      column: 'sort',
      order: 'asc',
    },
    formConfig: {
      schemas: searchFormSchema,
      autoSubmitOnEnter: true,
      showAdvancedButton: true,
      fieldMapToNumber: [
      ],
      fieldMapToTime: [
      ],
    },
    beforeFetch: (params) => {
      return Object.assign(params, queryParam);
    },
  },
})

const [registerTable, { reload, getDataSource, updateTableDataRecord, getSelectRows, getSelectRowKeys, setSelectedRowKeys, deleteSelectRowByKey, setLoading }, { rowSelection, selectedRowKeys }] = tableContext;

//设置主设备
async function handleMainChange(record: Recordable) {
  //获取列表数据
  const list = getDataSource();
  //获取上一个选中值
  const findValue = list.find((item) => {
    return item.id === lastIndexId.value;
  });
  //上一个选中值不为空，则取消上一个选中值的勾选状态并更新数据源
  if (findValue) {
    findValue.isMain = 0;
    updateTableDataRecord(lastIndexId.value, findValue);
    await deleteSelectRowByKey(lastIndexId.value);
  }

  //更新当前选中值勾选状态并更新数据源
  record.isMain = record.isMain ? 0 : 1;
  updateTableDataRecord(record.id, record);
  lastIndexId.value = record.id;

  //主设备变更时添加当前选中值到选中列表中
  await deleteSelectRowByKey(record.id);
  let selectedRow = getSelectRowKeys();
  selectedRow.push(record.id);
  await setSelectedRowKeys(selectedRow);
}

/**
 * 新增事件
 */
async function handleAdd() {
  const selectedRowArr = getSelectRowKeys();
  //校验数据
  if (!selectedRowArr || selectedRowArr.length === 0) {
    msg.warning('请选择设备');
    return;
  }
  if (!lastIndexId.value) {
    msg.warning('请选择主设备');
    return;
  }
  //组装数据
  let saveData = [] as any;
  for(const item of getSelectRows()){
    saveData.push({
      factoryCode: parentData.value.factoryCode,
      deptCode: parentData.value.deptCode,
      lineCode: parentData.value.lineCode,
      postId: parentData.value.id,
      deviceId: item.id,
      deviceName: item.deviceName,
      isMain: item.id === lastIndexId.value ? 1 : 0,
      mainDeviceId: lastIndexId.value,
    })
  }
  try {
    setLoading(true);
    await add(saveData);
    emit('success');
  } catch (error) {
    return Promise.reject(error);
  } finally {
    setLoading(false);
  }
}

function handleSelectChange({keys}){
  const findkey = keys.find(item => item === lastIndexId.value);
  if(lastIndexId.value && !findkey){
    let selectedRow = getSelectRowKeys();
    selectedRow.push(lastIndexId.value);
    setSelectedRowKeys(selectedRow);
  }
}

function restSelect(){
  lastIndexId.value = null;
  setSelectedRowKeys([]);
}

async function initData(value){
  //清空所有选择
  await restSelect();
  //参数赋值
  parentData.value = value;
  queryParam.lineCode = value.lineCode;
  queryParam.pageSize = 999;
  //查询数据
  await reload();
  //选中岗位已关联的设备
  let deviceArr = [] as any;
  for(const item of value.deviceList || []){
    if(item.isMain == 1) {
      lastIndexId.value = item.deviceId;
      handleMainChange({id: item.deviceId});
    } else {
      deviceArr.push(item.deviceId);
    }
  }
  setSelectedRowKeys(deviceArr);
}

//暴露方法
defineExpose({
  initData,
})
</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}
</style>