<template>
  <div class="p-2">
    <!--查询区域-->

    <!--引用表格-->
    <BasicTable @register="registerTable" @row-click="handleRowClick" :rowSelection="rowSelection">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button type="primary" v-auth="'serviceAgreement:mes3_service_agreement_input:add'"  @click="handleAdd" preIcon="ant-design:plus-outlined"> 新增</a-button>
        <a-button  type="primary" v-auth="'serviceAgreement:mes3_service_agreement_input:exportXls'" preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
        <j-upload-button  type="primary" v-auth="'serviceAgreement:mes3_service_agreement_input:importExcel'"  preIcon="ant-design:import-outlined" @click="onImportXls">导入</j-upload-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                删除
              </a-menu-item>
            </a-menu>
          </template>
          <a-button v-auth="'serviceAgreement:mes3_service_agreement_input:deleteBatch'">批量操作
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
      </template>
        <!--  deptCode部门查询插槽    -->
        <template #form-provideDept="{ model, field }">
          <kt-depart-select
            v-model:value="model[field]"
            select-type="depart"
            :placeholder="t('language.selectDepartment')"
          />
        </template>

      <!--  deptCode部门查询插槽    -->
      <template #form-receiveDept="{ model, field }">
        <kt-depart-select
          v-model:value="model[field]"
          select-type="depart"
          :placeholder="t('language.selectDepartment')"
        />
      </template>

      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)"/>
      </template>

    </BasicTable>
    <!-- 表单区域 -->
    <ServiceAgreementInputForm @register="registerAddModal" @success="handleSuccess"></ServiceAgreementInputForm>
  </div>
</template>

<script lang="ts" name="serviceAgreement-serviceAgreementInput" setup>
import {ref, reactive, defineEmits} from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { columns, searchFormSchema } from './ServiceAgreementInput.data';
  import { list, deleteOne, batchDelete, getImportUrl, getExportUrl } from './ServiceAgreementInput.api';
  import { downloadFile } from '/@/utils/common/renderUtils';
  import ServiceAgreementInputForm from './components/ServiceAgreementInputForm.vue'
  import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
  import { useUserStore } from '/@/store/modules/user';
  import {useI18n} from "@/hooks/web/useI18n";
  import {useModal} from "@/components/Modal";


  const { t } = useI18n();
  const queryParam = reactive<any>({});
  const userStore = useUserStore();

  //新增/编辑modal
  const [registerAddModal, { openModal }] = useModal();

  //注册table数据
  const { prefixCls, tableContext, onExportXls, onImportXls } = useListPage({
    tableProps: {
      title: '服务协议登记表',
      api: list,
      columns,
      expandRowByClick: true,
      showActionColumn: true,
      actionColumn: {
        width: 120,
        fixed: 'right',
      },
      formConfig: {
        schemas: searchFormSchema,
      },
      beforeFetch: async (params) => {
        return Object.assign(params, queryParam);
      },
    },
    exportConfig: {
      name: "服务协议登记表",
      url: getExportUrl,
      params: queryParam,
    },
	  importConfig: {
	    url: getImportUrl,
	    success: handleSuccess
	  },
  });
  const [registerTable, { reload, collapseAll, updateTableDataRecord, findTableDataRecord, getDataSource }, { rowSelection, selectedRowKeys }] = tableContext;


  /**
   * 新增事件
   */
  function handleAdd() {
    openModal(true, {'isAdd': true, 'isDetail': false})
  }
  
  /**
   * 编辑事件
   */
  function handleEdit(record: Recordable) {
    openModal(true, {'isAdd': false, 'isDetail': false, 'record': record})
  }
   
  /**
   * 详情
   */
  function handleDetail(record: Recordable) {
    openModal(true, {'isAdd': false, 'isDetail': true, 'record': record})
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

const emit = defineEmits(['custom-event']);

const handleRowClick = (record, rowIndex) => {
  emit('custom-event', record); // 触发事件并传递数据
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
        auth: 'serviceAgreement:mes3_service_agreement_input:edit'
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
      }, {
        label: '删除',
        popConfirm: {
          title: '是否确认删除',
          confirm: handleDelete.bind(null, record),
          placement: 'topLeft',
        },
        auth: 'serviceAgreement:mes3_service_agreement_input:delete'
      }
    ]
  }

  /**
   * 查询
   */
  function searchQuery() {
    reload();
  }
  
  /**
   * 重置
   */
  function searchReset() {
    selectedRowKeys.value = [];
    //刷新数据
    reload();
  }
  



</script>

<style lang="less" scoped>
  .jeecg-basic-table-form-container {
    padding: 0;
    .table-page-search-submitButtons {
      display: block;
      margin-bottom: 24px;
      white-space: nowrap;
    }
    .query-group-cust{
      min-width: 100px !important;
    }
    .query-group-split-cust{
      width: 30px;
      display: inline-block;
      text-align: center
    }
    .ant-form-item:not(.ant-form-item-with-help){
      margin-bottom: 16px;
      height: 32px;
    }
    :deep(.ant-picker),:deep(.ant-input-number){
      width: 100%;
    }
  }
</style>
