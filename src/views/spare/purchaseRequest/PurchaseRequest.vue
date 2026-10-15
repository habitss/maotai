<template>
  <!--引用表格-->
  <BasicTable @register="registerTable">
    <!--插槽:table标题-->
    <!-- 操作按钮 -->
    <template #tableTitle>
      <a-button type="primary" preIcon="ant-design:plus-outlined" @click="onAdd">新增</a-button>
    </template>
    <!--操作栏-->
    <template #action="{ record }">
      <TableAction :actions="getTableAction(record)" />
    </template>
  </BasicTable>
  <PurchaseRequestAddModal @register="registerAddModal" @success="handleSuccess" />
</template>

<script lang="ts" setup name="spare-purchase-request-list">
import PurchaseRequestAddModal
  from "@/views/spare/purchaseRequest/components/PurchaseRequestAddModal.vue";
import {useI18n} from "@/hooks/web/useI18n";
import {useMessage} from "@/hooks/web/useMessage";
import {useUserStore} from "@/store/modules/user";
import {useListPage} from "@/hooks/system/useListPage";
import {columns, searchFormSchema} from "@/views/dataCalculation/meeting/config/MeetingConfig.data";
import {deleteOne, getList} from "@/views/dataCalculation/meeting/config/MeetingConfig.api";
import {addFactoryDept} from "@/utils/common/compUtils";
import {useModal} from "@/components/Modal";
import {ActionItem} from "@/components/Table";

const { t } = useI18n();
const { createMessage: msg } = useMessage();
const userInfo = useUserStore();
//注册table数据
const { tableContext , onExportXls, onImportXls } = useListPage({
  tableProps: {
    title: '采购申请列表',
    columns,
    api: getList,
    showActionColumn: true,
    actionColumn: {
      width: 180,
    },
    formConfig: {
      schemas: searchFormSchema,
      showAdvancedButton: false,
    },
    beforeFetch: (params) => {
      return addFactoryDept(params);
    },
  },

})
const [registerTable, { reload , setTableData, getForm }] = tableContext;

//新增/编辑modal
const [registerAddModal, { openModal ,setModalProps}] = useModal();


/**
 * 操作栏
 */
function getTableAction(record): ActionItem[] {
  return [
    {
      label: '编辑',
      onClick: handleEdit.bind(null, record),
    },
    {
      label: '删除',
      onClick: handleDelete.bind(null, record),
    },
  ];
}

/**
 * 新增事件
 */
function onAdd() {
  openModal(true, {'isAdd': true, 'isDetail': false})
}

/**
 * 编辑事件
 */
function handleEdit(record) {
  let users = record.participant;
  if(null != users){
    let participant = users.split(",");
    record.participant = participant;
  }
  openModal(true, {'isAdd': false, 'isDetail': false, 'record': record})
}

/**
 * 详情
 */
function handleDetail(record) {
  openModal(true, {'isAdd': false, 'isDetail': true, 'record': record})
}

/**
 * 删除事件
 */
async function handleDelete(record) {
  await deleteOne({ id: record.id }, handleSuccess);
}

/**
 * 成功回调
 */
function handleSuccess() {
  reload();
}

</script>

<style scoped>

</style>
