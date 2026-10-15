<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--插槽:table标题-->
      <template #tableTitle>
        <template v-if = "openType == 'query'">
          <a-button type="primary" v-auth="'pi:mes3_pi_repeat_exception_type:add'" @click="handleAdd"
            preIcon="ant-design:plus-outlined"> 新增</a-button>
          <a-button type="primary" v-auth="'pi:mes3_pi_repeat_exception_type:add'" @click="handleCopy"
            preIcon="ant-design:plus-outlined">复制其他PI配置</a-button>
          <a-dropdown v-if="selectedRowKeys.length > 0">
            <template #overlay>
              <a-menu>
                <a-menu-item key="1" @click="batchHandleDelete">
                  <Icon icon="ant-design:delete-outlined"></Icon>
                  删除
                </a-menu-item>
              </a-menu>
            </template>
            <a-button v-auth="'pi:mes3_pi_repeat_exception_type:deletionBatch'">批量操作
              <Icon icon="mdi:chevron-down"></Icon>
            </a-button>
          </a-dropdown>
        </template>
      </template>
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
    </BasicTable>
    <!-- 表单区域 -->
    <PiRepeatExceptionTypeModal @register="registerModal" @success="handleSuccess"></PiRepeatExceptionTypeModal>
    <!-- 复制其他PI配置 -->
    <PiRepeatExceptionTypeCopyModal @register="registerCopyModal" @success="handleSuccess"></PiRepeatExceptionTypeCopyModal>
  </div>
</template>

<script lang="ts" name="pi-piRepeatExceptionType" setup>
import { onMounted, reactive } from 'vue';
import { BasicTable, TableAction } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useListPage } from '/@/hooks/system/useListPage'
import PiRepeatExceptionTypeModal from './components/PiRepeatExceptionTypeModal.vue'
import PiRepeatExceptionTypeCopyModal from './components/PiRepeatExceptionTypeCopyModal.vue'
import { columns, searchFormSchema } from './PiRepeatExceptionType.data';
import { list, deleteOne, batchDelete } from './PiRepeatExceptionType.api';
const queryParam = reactive<any>({});
const props = defineProps({
  recordData: { 
    type: Object, 
    default: () => ({}) 
  },
  openType: { 
    type: String, 
    default: 'query'
  },
});
//注册model
const [registerModal, { openModal }] = useModal();
const [registerCopyModal, { openModal: openCopyModal }] = useModal();

//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    title: 'pi重复异常类型配置',
    api: list,
    columns,
    canResize: false,
    showTableSetting: false,
    immediate: false,
    pagination: false,
    formConfig: {
      labelWidth: 120,
      schemas: searchFormSchema,
      autoSubmitOnEnter: true,
      showAdvancedButton: true,
      fieldMapToNumber: [
      ],
      fieldMapToTime: [
      ],
    },
    showActionColumn: props.openType == 'query',
    actionColumn: {
      width: 120,
      fixed: 'right'
    },
    beforeFetch: (params) => {
      params.pageSize = 1000;
      return Object.assign(params, queryParam);
    },
    defSort: {
      column: 'sort',
      order: 'asc',
    },
  },
})

const [registerTable, { reload, getForm, getDataSource }, { rowSelection, selectedRowKeys }] = tableContext

/**
 * 新增事件
 */
function handleAdd() {
  let record = {
    factoryCode: '',
    deptCode: '',
    lineCode: '',
    piTableId: props.recordData.tableId,
    piTableCode: props.recordData.tableName,
    piFieldId: props.recordData.id,
    piFieldCode: props.recordData.fieldName,
  }
  openModal(true, {
    record,
    isUpdate: false,
    showFooter: true,
  });
}
/**
 * 复制其他PI配置事件
 */
function handleCopy(){
  openCopyModal(true, {
    record: props.recordData
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
      auth: 'pi:mes3_pi_repeat_exception_type:edit'
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
      auth: 'pi:mes3_pi_repeat_exception_type:delete'
    }
  ]
}

onMounted(async () => {
  if(props.openType == 'query'){
    queryParam.piTableId = props.recordData.tableId;
    queryParam.piFieldId = props.recordData.id;
    reload();
  }else{
    await getForm().setFieldsValue({
      openType: props.openType,
      piTableId: props.recordData.tableId,
    });
  }
})

//暴露方法
defineExpose({
  getDataSource,
})

</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}
</style>