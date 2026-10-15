<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable">
      <!--插槽:table标题-->
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
      <!--字段回显插槽-->
      <template v-slot:bodyCell="{ column, record, index, text }"> </template>
    </BasicTable>
    <!-- 表单区域 -->
    <LineEfficiencyEhsRecordsModal @register="registerModal" @success="handleSuccess"></LineEfficiencyEhsRecordsModal>
  </div>
</template>

<script lang="ts" name="line.efficiency-lineEfficiencyEhsRecords" setup>
  import { reactive } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useListPage } from '/@/hooks/system/useListPage';
  import LineEfficiencyEhsRecordsModal from './components/LineEfficiencyEhsRecordsModal.vue';
  import { columns, searchFormSchema } from './LineEfficiencyEhsRecords.data';
  import { list, deleteOne, batchDelete, getImportUrl, getExportUrl } from './LineEfficiencyEhsRecords.api';
  const queryParam = reactive<any>({});
  //注册model
  const [registerModal, { openModal }] = useModal();
  //注册table数据
  const { tableContext } = useListPage({
    tableProps: {
      title: 'ehs推送查询',
      api: list,
      columns,
      canResize: true,
      formConfig: {
        schemas: searchFormSchema,
        autoSubmitOnEnter: true,
        showAdvancedButton: true,
        fieldMapToNumber: [],
        fieldMapToTime: [['monitorDateTime', ['monitorDateTime_begin', 'monitorDateTime_end'], 'YYYY-MM-DD HH:mm:ss']],
      },
      beforeFetch: (params) => {
        return Object.assign(params, queryParam);
      },
      pagination: {
        pageSizeOptions: ['50', '100', '300'],
        pageSize: 50,
      },
    },
    exportConfig: {
      name: 'ehs查询',
      url: getExportUrl,
      params: queryParam,
    },
    importConfig: {
      url: getImportUrl,
      success: handleSuccess,
    },
  });

  const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;

  /**
   * 编辑事件
   */
  function handleEdit(record: Recordable) {
    openModal(true, {
      record,
      isUpdate: true,
      showFooter: true,
    });
  }
  /**
   * 详情
   */
  function handleDetail(record: Recordable) {
    openModal(true, {
      record,
      isUpdate: true,
      showFooter: false,
    });
  }

  /**
   * 删除事件
   */
  async function handleDelete(record) {
    await deleteOne({ id: record.id }, handleSuccess);
  }

  /**
   * 批量删除事件
   */
  async function batchHandleDelete() {
    await batchDelete({ ids: selectedRowKeys.value }, handleSuccess);
  }
  /**
   * 成功回调
   */
  function handleSuccess() {
    (selectedRowKeys.value = []) && reload();
  }
  /**
   * 操作栏
   */
  function getTableAction(record) {
    return [
      {
        label: '编辑',
        onClick: handleEdit.bind(null, record),
        auth: 'line.efficiency:mes3_line_efficiency_ehs_records:edit',
      },
    ];
  }
  /**
   * 下拉操作栏
   */
  function getDropDownAction(record) {
    return [
      {
        label: '详情',
        onClick: handleDetail.bind(null, record),
      },
      {
        label: '删除',
        popConfirm: {
          title: '是否确认删除',
          confirm: handleDelete.bind(null, record),
          placement: 'topLeft',
        },
        auth: 'line.efficiency:mes3_line_efficiency_ehs_records:delete',
      },
    ];
  }
</script>

<style lang="less" scoped>
  :deep(.ant-picker),
  :deep(.ant-input-number) {
    width: 100%;
  }
</style>
