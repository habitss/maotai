<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button type="primary" v-auth="'pi:mes3_consistency_config_mx:add'" @click="handleAdd"
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
          <a-button v-auth="'pi:mes3_consistency_config_mx:deletionBatch'">批量操作
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
    <ConsistencyConfigMxModal @register="registerModal" @success="handleSuccess"></ConsistencyConfigMxModal>
    <!-- pi基础数据 -->
    <ConsistencyConfigPiModal @register="registerPiModal" @success="handleSuccess"></ConsistencyConfigPiModal>
  </div>
</template>

<script lang="ts" name="pi-consistencyConfigMx" setup>
import { reactive, ref } from 'vue';
import { BasicTable, TableAction } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useListPage } from '/@/hooks/system/useListPage'
import ConsistencyConfigMxModal from './components/ConsistencyConfigMxModal.vue'
import ConsistencyConfigPiModal from './components/ConsistencyConfigPiModal.vue'
import { columns, searchFormSchema } from './ConsistencyConfigMx.data';
import { list, deleteOne, batchDelete } from './ConsistencyConfigMx.api';
const queryParam = reactive<any>({});
//注册model
const [registerModal, { openModal }] = useModal();
//注册model
const [registerPiModal, { openModal: openPiModal }] = useModal();
const zbRecord = ref({} as any);
//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    title: 'pi配置',
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
    immediate: false,
    actionColumn: {
      title: '操作',
      width: 200,
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
    zbRecord,
    isUpdate: false,
    showFooter: true,
  });
}
/**
 * 编辑事件
 */
function handleEdit(record: Recordable) {
  openModal(true, {
    zbRecord,
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
    zbRecord,
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
 * pi基础数据
 */
function handleConfig(record) {
  openPiModal(true, {
    record,
    isUpdate: false,
    showFooter: true,
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
      auth: 'pi:mes3_consistency_config_mx:edit'
    },
    {
      label: 'pi基础数据',
      ifShow: !record.piCode,
      onClick: handleConfig.bind(null, record),
      auth: 'pi:mes3_consistency_config_mx:config'
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
      auth: 'pi:mes3_consistency_config_mx:delete'
    }
  ]
}

// 刷新列表数据
function reloadList(data) {
  zbRecord.value = data;
  queryParam.zbId = data.id;
  queryParam.column = "sort";
  queryParam.order = "asc";
  reload();
}

defineExpose({
  reloadList
});
</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}
</style>