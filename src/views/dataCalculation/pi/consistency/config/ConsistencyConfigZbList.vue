<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button type="primary" v-auth="'pi:mes3_consistency_config_zb:add'" @click="handleAdd"
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
          <a-button v-auth="'pi:mes3_consistency_config_zb:deletionBatch'">批量操作
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
    </BasicTable>
    <!-- 表单区域 -->
    <ConsistencyConfigZbModal @register="registerModal" @success="handleSuccess"></ConsistencyConfigZbModal>
    <!-- pi配置 -->
    <ConsistencyConfigMxListModal @register="registerMxModal" @success="handleSuccess"></ConsistencyConfigMxListModal>
  </div>
</template>

<script lang="ts" name="pi-consistencyConfigZb" setup>
import { onMounted, reactive } from 'vue';
import { BasicTable, TableAction } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useListPage } from '/@/hooks/system/useListPage'
import ConsistencyConfigZbModal from './components/ConsistencyConfigZbModal.vue'
import ConsistencyConfigMxListModal from './components/ConsistencyConfigMxListModal.vue'
import { columns, searchFormSchema } from './ConsistencyConfigZb.data';
import { list, deleteOne, batchDelete } from './ConsistencyConfigZb.api';
const queryParam = reactive<any>({});
//注册model
const [registerModal, { openModal }] = useModal();
//注册model
const [registerMxModal, { openModal: openMxModal }] = useModal();
//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    title: '一致性指数',
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
      width: 200,
      fixed: 'right'
    },
    beforeFetch: (params) => {
      params.column = "sort";
      params.order = "asc";
      params.consistencyName = params.consistencyName && `*${params.consistencyName}*`;
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
 * 指数配置
 */
function handleConfig(record: Recordable) {
  openMxModal(true, {
    record
  });
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
      auth: 'pi:mes3_consistency_config_zb:edit'
    },
    {
      label: 'pi配置',
      onClick: handleConfig.bind(null, record),
      auth: 'pi:mes3_consistency_config_zb:config'
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
      auth: 'pi:mes3_consistency_config_zb:delete'
    }
  ]
}
</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}
</style>