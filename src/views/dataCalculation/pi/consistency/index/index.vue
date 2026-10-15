<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable">
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
    <!-- 详情区域 -->
    <IndexFirstModal @register="registerModal"></IndexFirstModal>
  </div>
</template>

<script lang="ts" name="pi-consistencyConfigIndex" setup>
import { onMounted, reactive } from 'vue';
import { BasicTable, TableAction } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useListPage } from '/@/hooks/system/useListPage'
import { columns, searchFormSchema } from './index.data';
import { list } from './index.api';
import IndexFirstModal from './components/IndexFirstModal.vue'
import dayjs from 'dayjs';
const queryParam = reactive<any>({});
//注册model
const [registerModal, { openModal }] = useModal();
//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    title: '一致性指数统计',
    api: list,
    columns,
    canResize: false,
    formConfig: {
      //labelWidth: 120,
      schemas: searchFormSchema,
      autoSubmitOnEnter: true,
      showAdvancedButton: true,
      fieldMapToNumber: [
      ],
      fieldMapToTime: [['startTime', ['timeBegin', 'timeEnd'], 'YYYY-MM-DD HH:mm:ss']],
    },
    actionColumn: {
      width: 80,
      fixed: 'right'
    },
    beforeFetch: (params) => {
      params.column = "sort";
      params.order = "asc";
      return Object.assign(params, queryParam);
    },
  },
})

const [registerTable, { getForm }] = tableContext;
/**
 * 详情
*/
function handleDetail(record: Recordable) {
  const param = getForm().getFieldsValue();
  openModal(true, {
    zbId: record.id,
    consistencyType: param.consistencyType,
    timeBegin: param.timeBegin,
    timeEnd: param.timeEnd,
  });
}
/**
   * 操作栏
   */
function getTableAction(record) {
  return [
    {
      label: '详情',
      onClick: handleDetail.bind(null, record),
    },
  ]
}

onMounted(async () => {
  //设置查询条件默认值,不使用defaultValue是因为表格查询无法在初始的时候获取
  let { setFieldsValue } = getForm();
  setFieldsValue({startTime: [dayjs().startOf('day').subtract(6, 'day'), dayjs().endOf('day')], consistencyType: '1'})
});
</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}
</style>