<template>
  <BasicTable @register="registerTable"  :rowSelection="rowSelection">
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

    <template #action="{ record }">
      <!-- 操作列 -->
      <TableAction :actions="createActions(record)" :dropDownActions="getDropDownAction(record)"/>
    </template>
  </BasicTable>

  <!-- 填报modal -->
  <Modal @register="registerAddModal" @success="reload" />
  <!-- 配置modal -->
  <CopPointConfigModal @register="registerCopPointConfigModal" @success="reload" />
</template>
<script lang="ts" name="dataCalculation-cop-point-index" setup>
  import { ActionItem, BasicTable, TableAction, EditRecordRow } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useModal } from '/@/components/Modal';
  import {getList, deleteData, getConfigList} from './copPonin.api';
  import Modal from './copPointForm.vue';
  import { columns, searchFormSchema } from './copPonin.data';
  import CopPointConfigModal
    from "@/views/dataCalculation/copManagement/copPoint/copPointConfigModal.vue";
  import {
    getExportUrl,
    getImportUrl
  } from "@/views/dataCalculation/copManagement/copPoint/copPonin.api";
  import {reactive} from "vue";
  import {batchDelete, batchPointDelete} from "@/views/dataCalculation/copManagement/copList.api";

  const props = defineProps({
    record: {
      type: Object,
      default: {}
    }
  })
    //消息提示
    const { createMessage: msg } = useMessage();

    //新增/编辑modal
    const [registerAddModal, { openModal }] = useModal();

    const [registerCopPointConfigModal, { openModal:openCopPointConfigModal }] = useModal();

    const queryParam = reactive<any>({});

    // 列表页面公共参数、方法
    const { prefixCls,tableContext, onExportXls, onImportXls } = useListPage({
      tableProps: {
        title: '清洗点列表',
        api: getList,
        columns: columns,
        expandRowByClick: true,
        showActionColumn: true,
        actionColumn: {
          width: 230,
          fixed: 'right',
        },
        beforeFetch: (params) => {
          params.column = 'sort';
          params.order = 'asc';
        return Object.assign(params, {dataId: props.record.id});
      },
        formConfig: {
          schemas: searchFormSchema,
        },
        rowSelection: {
          columnWidth: 20,
        },
        rowKey: "id",
      },
      exportConfig: {
        name: "清洗点信息",
        url: getExportUrl,
        params: queryParam,
      },
      importConfig: {
        url: getImportUrl,
        success: handleSuccess
      },
    });

    /**
     * 创建操作列
     */
    function createActions(record: EditRecordRow): ActionItem[] {
      return [
        {
          label: '编辑',
          onClick: handleEdit.bind(null, record),
        },
        {
          label: '清洗信息配置',
          onClick: handleOpenCopPointConfig.bind(null, record),
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
        },
        {
          label: '删除',
          popConfirm: {
            title: '是否删除？',
            confirm: handleDelete.bind(null, record.id),
          },
        },
      ]
    }

    // BasicTable绑定注册
    const [registerTable, { reload },{ rowSelection, selectedRows, selectedRowKeys }] = tableContext;

    /**
     * 新增事件
     */
    function onAdd() {
      openModal(true, {'isAdd': true, 'isDetail': false, 'deviceData': props.record})
    }

    /**
     * 编辑事件
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
     * 删除
     */
    async function handleDelete(record) {
      console.log(record)
        //调用后台修改接口, 删除
        await deleteData(record, handleSuccess);
        msg.success({ content: '删除成功', key: 'saving' });
    }

  /**
   * 配置清洗点
   */
  function handleOpenCopPointConfig(record) {
    openCopPointConfigModal(true, {'record': record})
  }

  /**
   * 批量删除事件
   */
  async function batchHandleDelete() {
    const ids = selectedRowKeys.value
    await batchPointDelete({ ids: ids }, importSuccess);
  }

  function importSuccess() {
    (selectedRowKeys.value = []) && reload();
  }

    /**
     * 成功回调
     */
    function handleSuccess() {
      reload();
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
