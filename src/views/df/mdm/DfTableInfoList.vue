<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button type="primary" v-auth="'df.mdm:mes3_mdm_df_table_info:add'" @click="handleAdd"
          preIcon="ant-design:plus-outlined"> 新增</a-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                删除
              </a-menu-item>
            </a-menu>
          </template>
          <a-button v-auth="'df.mdm:mes3_mdm_df_table_info:deletionBatch'">批量操作
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
      <!--字段回显插槽-->
      <template v-slot:bodyCell="">
      </template>
    </BasicTable>
    <!-- 表单区域 -->
    <DfTableInfoModal @register="registerModal" @success="handleSuccess"></DfTableInfoModal>
    <!--字段配置抽屉-->
    <DfTableFieldInfoListDrawer @register="registerDrawer" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" name="df.mdm-dfTableInfo" setup>
import { reactive } from 'vue';
import { BasicTable, TableAction } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useListPage } from '/@/hooks/system/useListPage'
import DfTableInfoModal from './components/DfTableInfoModal.vue'
import DfTableFieldInfoListDrawer from './components/DfTableFieldInfoListDrawer.vue'
import { columns, searchFormSchema } from './DfTableInfo.data';
import { list, deleteOne, batchDelete } from './DfTableInfo.api';
import { useDrawer } from '/@/components/Drawer';
const queryParam = reactive<any>({});
//字段配置抽屉
const [registerDrawer, { openDrawer }] = useDrawer();
//注册model
const [registerModal, { openModal }] = useModal();
//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    title: 'DF数据库表基础信息',
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
      fieldMapToTime: [
      ],
    },
    actionColumn: {
      width: 220,
      fixed: 'right'
    },
    beforeFetch: (params) => {
      return Object.assign(params, queryParam);
    },
  },
})

const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext

/**
 * 新增事件
 */
function handleAdd() {
  openModal(true, {
    isUpdate: false,
    showFooter: true,
  });
}
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
      auth: 'df.mdm:mes3_mdm_df_table_info:edit'
    },
    {
      label: '字段配置',
      onClick: handleSyncField.bind(null, record),
      auth: 'df.mdm:mes3_mdm_df_table_field_info:edit'
    }
  ]
}
/**
   * 下拉操作栏
   */
function getDropDownAction(record) {
  return [
    {
      label: '详情',
      onClick: handleDetail.bind(null, record),
    }, {
      label: '删除',
      popConfirm: {
        title: '是否确认删除',
        confirm: handleDelete.bind(null, record),
        placement: 'topLeft',
      },
      auth: 'df.mdm:mes3_mdm_df_table_info:delete'
    }
  ]
}

function handleSyncField(record) {
  openDrawer(true, {
    record,
  });
}

</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}
</style>