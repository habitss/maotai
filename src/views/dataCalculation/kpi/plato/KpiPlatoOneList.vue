<template>
  <div>
    <!--引用表格-->
   <BasicTable @register="registerTable" :rowSelection="rowSelection">
     <!--插槽:table标题-->
      <template #tableTitle>
          <a-button type="primary" v-auth="'kpi:mes3_kpi_plato_one:add'" @click="handleAdd" preIcon="ant-design:plus-outlined"> 新增</a-button>
          <a-button  type="primary" v-auth="'kpi:mes3_kpi_plato_one:exportXls'" preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
          <j-upload-button type="primary" v-auth="'kpi:mes3_kpi_plato_one:importExcel'" preIcon="ant-design:import-outlined" @click="onImportXls">导入</j-upload-button>
          <a-dropdown v-if="selectedRowKeys.length > 0">
              <template #overlay>
                <a-menu>
                  <a-menu-item key="1" @click="batchHandleDelete">
                    <Icon icon="ant-design:delete-outlined"></Icon>
                    删除
                  </a-menu-item>
                </a-menu>
              </template>
              <a-button v-auth="'kpi:mes3_kpi_plato_one:deletionBatch'">批量操作
                <Icon icon="mdi:chevron-down"></Icon>
              </a-button>
        </a-dropdown>
      </template>
       <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)"/>
      </template>
      <!--字段回显插槽-->
      <template v-slot:bodyCell="{ column, record, index, text }">
      </template>
    </BasicTable>
    <!-- 表单区域 -->
    <KpiPlatoOneModal @register="registerModal" @success="handleSuccess"></KpiPlatoOneModal>
  </div>
</template>

<script lang="ts" name="kpi-kpiPlatoOne" setup>
  import {ref, reactive, computed, unref} from 'vue';
  import {BasicTable, useTable, TableAction} from '/@/components/Table';
  import {useModal} from '/@/components/Modal';
  import { useListPage } from '/@/hooks/system/useListPage'
  import KpiPlatoOneModal from './components/KpiPlatoOneModal.vue'
  import {columns, searchFormSchema} from './KpiPlatoOne.data';
  import {list, deleteOne, batchDelete, getImportUrl,getExportUrl} from './KpiPlatoOne.api';
  import { downloadFile } from '/@/utils/common/renderUtils';
  import { useUserStore } from '/@/store/modules/user';
  const queryParam = reactive<any>({});
  const userStore = useUserStore();
  //注册model
  const [registerModal, {openModal}] = useModal();
  //注册table数据
  const { prefixCls,tableContext,onExportXls,onImportXls } = useListPage({
    tableProps:{
      title: '一级柏拉图（生产流程）',
      api: list,
      columns,
      canResize:false,
      formConfig: {
        //labelWidth: 120,
        schemas: searchFormSchema,
        autoSubmitOnEnter:true,
        showAdvancedButton:true,
        fieldMapToNumber: [
        ],
        fieldMapToTime: [
        ],
      },
      actionColumn: {
          width: 120,
          fixed:'right'
      },
      beforeFetch: (params) => {
        params.processName = params.processName && `*${params.processName}*`;
        return Object.assign(params, queryParam);
      }
    },
    exportConfig: {
      name:"一级柏拉图（生产流程）",
      url: getExportUrl,
      params: queryParam,
    },
    importConfig: {
      url: getImportUrl,
      success: handleSuccess
    },
  })

  const [registerTable, {reload},{ rowSelection, selectedRowKeys }] = tableContext

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
     await deleteOne({id: record.id}, handleSuccess);
   }
  /**
  * 批量删除事件
  */
  async function batchHandleDelete() {
     await batchDelete({ids: selectedRowKeys.value}, handleSuccess);
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
  function getTableAction(record){
    return [
      {
        label: '编辑',
        onClick: handleEdit.bind(null, record),
        auth: 'kpi:mes3_kpi_plato_one:edit'
      }
    ]
   }
  /**
  * 下拉操作栏
  */
  function getDropDownAction(record){
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
        auth: 'kpi:mes3_kpi_plato_one:delete'
      }
    ]
   }

</script>

<style lang="less" scoped>
  :deep(.ant-picker),:deep(.ant-input-number){
    width: 100%;
  }
</style>