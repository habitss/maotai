<template>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">

    <!-- 操作按钮 -->
    <template #tableTitle>
      <a-button type="primary" preIcon="ant-design:plus-outlined" @click="onAdd">新增</a-button>
      <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
      <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="onImportXls">导入</j-upload-button>
      <a-dropdown v-if="selectedRowKeys.length > 0">
        <template #overlay>
          <a-menu>
            <a-menu-item key="1" @click="batchHandleDelete">
              <Icon icon="ant-design:delete-outlined"></Icon>
              删除
            </a-menu-item>
          </a-menu>
        </template>
        <a-button>批量操作
          <Icon icon="ant-design:down-outlined"></Icon>
        </a-button>
      </a-dropdown>
    </template>
      <!-- 查询区域插槽 -->
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

      <!-- 操作列插槽 -->
      <template #action="{ record }">
        <TableAction :actions="createActions(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
    </BasicTable>
    <Modal @register="registerAddModal" @success="handleSuccess" />
    <ConfigModel @register="registerSubmitConfigModal" :width="1200" />
  </template>
  <script lang="ts" name="dataCalculation-period-config-list" setup>
    import { reactive } from 'vue';
    import { ActionItem, BasicTable, BasicColumn, TableAction, EditRecordRow } from '/@/components/Table';
    import { getList, deleteData } from './ConfigList.api';
    import { columns, searchFormSchema } from './ConfigList.data';
    import { useListPage } from '/@/hooks/system/useListPage';
    import { useMessage } from '/@/hooks/web/useMessage';
    import { useModal } from '/@/components/Modal';
    import Modal from './components/ConfigForm.vue';
    import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
    import { useI18n } from '/@/hooks/web/useI18n';
    import ConfigModel
      from "./components/ConfigModal.vue";
    import {
      getConfigList,
      getExportUrl,
      getImportUrl
    } from "@/views/dataCalculation/periodCheck/periodConfig/ConfigList.api";
    import {batchDelete} from "@/views/dataCalculation/periodCheck/periodConfig/ConfigList.api";
    const { t } = useI18n();
    //查询参数对象
    const queryParam = reactive<any>({});


    //消息提示
    const { createMessage: msg } = useMessage();

      //新增/编辑modal
    const [registerAddModal, { openModal }] = useModal();

    //配置modal
    const [registerSubmitConfigModal, { openModal: openConfigModal }] = useModal();


    // 列表页面公共参数、方法
    const { tableContext , onExportXls, onImportXls } = useListPage({
      tableProps: {
        title: '期间核查配置管理',
        api: getList,
        beforeFetch: (params) => {
          params.column = 'sort';
          params.order = 'asc';
          return Object.assign(params, queryParam);
        },
        columns: columns,
        expandRowByClick: true,
        showActionColumn: true,
        actionColumn: {
          width: 240,
          fixed: 'right',
        },
        rowSelection: {
          columnWidth: 20,
        },
        formConfig: {
          schemas: searchFormSchema,
        },
      },
      exportConfig: {
        name: "仪器信息",
        url: getExportUrl,
        params: queryParam,
      },
      importConfig: {
        url: getImportUrl,
        success: handleSuccess
      },
    });

    // BasicTable绑定注册
    const [registerTable, { reload, getForm },{ rowSelection, selectedRows, selectedRowKeys }] = tableContext;


    /**
     * 创建操作列
     */
     function createActions(record): ActionItem[] {
    return [
      {
        label: '编辑',
        onClick: handleEdit.bind(null, record),
      },
      {
        label: '配置检查项',
        onClick: handleOpenConfigModal.bind(null, record),
      }
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
          title: '是否删除',
          confirm: handleDelete.bind(null, record),
        },
      },
  ]
}

    /**
     * 新增事件
     */
    function onAdd() {
        openModal(true, {'isAdd': true, 'isDetail': false})
    }

    /**
     * 编辑事件
    //  * templateRecord: templateRecord.value
     */
     function handleEdit(record) {
        openModal(true, {'isAdd': false, 'isDetail': false, 'record': record})
    }

    /**
     * 详情
     */
    function handleDetail(record) {
        openModal(true, {'isAdd': false, 'isDetail': true, 'record': record})
    }

    /**
     * 配置清洗点
     */
     function handleOpenConfigModal(record) {
        openConfigModal(true, {'record': record})
    }

    /**
     * 删除
     */
    async function handleDelete(record){
        //调用后台修改接口, 删除
        await deleteData(record.id, handleSuccess);
        msg.success({ content: '删除成功', key: 'saving' });
    }

    /**
     * 批量删除事件
     */
    async function batchHandleDelete() {
      const ids = selectedRowKeys.value
      await batchDelete({ ids: ids }, handleSuccess);
    }

    /**
     * 成功回调
     */
    function handleSuccess() {
        reload();
    }

    //部门改变事件
    async function handleDeptChange(value){
      //清空产线
      await getForm().setFieldsValue({
        lineCode: '',
      });
    }
  </script>
  <style scoped>
    /** 时间和数字输入框样式 */
    :deep(.ant-input-number) {
      width: 100%;
    }

    :deep(.ant-picker) {
      width: 100%;
    }

    /** 下拉框默认文字颜色 */
    :deep(.ant-select-selection-placeholder) {
      color: #BEBEBE !important;
    }
  </style>
