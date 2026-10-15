<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button type="primary" v-auth="'mdm:mes3_mdm_shift_group:add'" @click="handleAdd"
          preIcon="ant-design:plus-outlined"> 新增</a-button>
        <a-button type="primary" v-auth="'mdm:mes3_mdm_shift_group:exportXls'" preIcon="ant-design:export-outlined"
          @click="onExportXls"> 导出</a-button>
        <j-upload-button type="primary" v-auth="'mdm:mes3_mdm_shift_group:importExcel'"
          preIcon="ant-design:import-outlined" @click="onImportXls">导入</j-upload-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                删除
              </a-menu-item>
            </a-menu>
          </template>
          <a-button v-auth="'mdm:mes3_mdm_shift_group:deletionBatch'">批量操作
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>

      <!--  deptCode部门查询插槽    -->
      <template #form-deptCode="{ model, field }">
        <kt-depart-select
          v-model:value="model[field]"
          select-type="depart"
          :placeholder="t('language.selectDepartment')"
          @change="handleDeptChange"
        />
      </template>

      <!--  lineCode产线查询插槽    -->
      <template #form-lineCode="{ model, field }">
        <kt-depart-select
          v-model:value="model[field]"
          select-type="line"
          :placeholder="t('language.selectProductLine')"
          :parentKey="model.deptCode"
        />
      </template>
    </BasicTable>
    <!-- 表单区域 -->
    <MdmShiftGroupModal @register="registerModal" @success="handleSuccess"></MdmShiftGroupModal>
    <!--人员分配抽屉-->
    <MdmShiftGroupDrawer @register="registerDrawer" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" name="mdm-mdmShiftGroup" setup>
import { reactive } from 'vue';
import { BasicTable, TableAction } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useListPage } from '/@/hooks/system/useListPage'
import MdmShiftGroupModal from './components/MdmShiftGroupModal.vue'
import MdmShiftGroupDrawer from './components/MdmShiftGroupDrawer.vue'
import { columns, searchFormSchema } from './MdmShiftGroup.data';
import { list, deleteOne, batchDelete, getImportUrl, getExportUrl } from './MdmShiftGroup.api';
import { useDrawer } from '/@/components/Drawer';
import { useI18n } from '/@/hooks/web/useI18n';
import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
const { t } = useI18n();
const queryParam = reactive<any>({});
//注册model
const [registerModal, { openModal }] = useModal();
//人员分配抽屉
const [registerDrawer, { openDrawer }] = useDrawer();
//注册table数据
const { tableContext, onExportXls, onImportXls } = useListPage({
  tableProps: {
    title: '班组管理',
    api: list,
    columns,
    canResize: false,
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
      width: 180,
      fixed: 'right'
    },
    beforeFetch: (params) => {
      return Object.assign(params, queryParam);
    },
  },
  exportConfig: {
    name: "班组管理",
    url: getExportUrl,
    params: queryParam,
  },
  importConfig: {
    url: getImportUrl,
    success: handleSuccess
  },
})

const [registerTable, { reload, getForm }, { rowSelection, selectedRowKeys }] = tableContext

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
 * 人员分配事件
 */
function handleManage(record: Recordable) {
  openDrawer(true, {
    record,
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
      auth: 'mdm:mes3_mdm_shift_group:edit'
    },
    {
      label: '人员分配',
      onClick: handleManage.bind(null, record),
      auth: 'mdm:mes3_mdm_shift_type:people'
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
      auth: 'mdm:mes3_mdm_shift_group:delete'
    }
  ]
}

//部门改变事件
async function handleDeptChange(value){
  //清空产线
  await getForm().setFieldsValue({ 
    lineCode: '',  
  });
}
</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}
</style>