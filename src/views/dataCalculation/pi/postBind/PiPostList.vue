<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button type="primary" v-auth="'pi:mes3_pi_post:add'" @click="handleAdd" preIcon="ant-design:plus-outlined">新增</a-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                删除
              </a-menu-item>
            </a-menu>
          </template>
          <a-button v-auth="'pi:mes3_pi_post:deletionBatch'">批量操作
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
    <PiPostModal @register="registerModal" @success="handleSuccess"></PiPostModal>
  </div>
</template>

<script lang="ts" name="pi-piPost" setup>
import { reactive, ref } from 'vue';
import { BasicTable, TableAction } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useListPage } from '/@/hooks/system/useListPage'
import PiPostModal from './components/PiPostModal.vue'
import { columns, searchFormSchema } from './PiPost.data';
import { list, deleteOne, batchDelete } from './PiPost.api';
const queryParam = reactive<any>({});
//父级数据
const parentRecord = ref();
//注册model
const [registerModal, { openModal }] = useModal();
//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    title: 'PI岗位绑定',
    api: list,
    columns,
    immediate: false,
    canResize: false,
    showTableSetting: false,
    pagination: false,
    formConfig: {
      schemas: searchFormSchema,
      autoSubmitOnEnter: true,
      showAdvancedButton: true,
      fieldMapToNumber: [
      ],
      fieldMapToTime: [
      ],
    },
    actionColumn: {
      width: 120,
      fixed: 'right'
    },
    beforeFetch: (params) => {
      params.relatedTable = parentRecord.value.tableName;
      return Object.assign(params, queryParam);
    },
  },
})

const [registerTable, { reload, getForm, getDataSource }, { rowSelection, selectedRowKeys }] = tableContext;
/**
 * 新增事件
 */
function handleAdd() {
  openModal(true, {
    record: {
      factoryCode: parentRecord.value.factoryCode,
      lineCode: parentRecord.value.lineCode,
      deptCode: parentRecord.value.deptCode,
      relatedTableName: parentRecord.value.name,
      relatedTable: parentRecord.value.tableName,
    },
    bindPost: getDataSource(),
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
    bindPost: getDataSource(),
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
      auth: 'pi:mes3_pi_post:edit'
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
      auth: 'pi:mes3_pi_post:delete'
    }
  ]
}
//设置表格数据
async function reloadData(data){
  parentRecord.value = data;
  await getForm().resetFields();
  await getForm().updateSchema([
    {
      field: 'postId',
      componentProps: {
        showChooseOption: false,
        getPopupContainer: () => document.body,
        dictCode: "mes3_mdm_post where del_flag = 0 and line_code = '" + data.lineCode + "' order by sort, post_name, id",
      },
    },
  ]);
  reload();
}
//暴露方法
defineExpose({
  reloadData,
})
</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}
</style>