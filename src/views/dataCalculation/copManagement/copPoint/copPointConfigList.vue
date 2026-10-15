<template>
  <BasicTable @register="registerTable">
    <!-- 操作按钮 -->
    <template #tableTitle>
      <a-button type="primary" preIcon="ant-design:plus-outlined" @click="onAdd">新增</a-button>
    </template>

    <template #action="{ record }">
      <!-- 操作列 -->
      <TableAction :actions="createActions(record)" :dropDownActions="getDropDownAction(record)"/>
    </template>
  </BasicTable>

  <!-- 填报modal -->
  <Modal @register="registerAddModal" @success="reload" />
</template>
<script lang="ts" name="dataCalculation-cop-point-config" setup>
  import { ActionItem, BasicTable, TableAction, EditRecordRow } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useModal } from '/@/components/Modal';
  import { getList, deleteData } from './copPointConfig.api';
  import Modal from './copPointConfigForm.vue';
  import {columnsConfig, searchConfigFormSchema} from './copPonin.data';

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

    // 列表页面公共参数、方法
    const { tableContext } = useListPage({
      tableProps: {
        title: '清洗点配置列表',
        api: getList,
        columns: columnsConfig,
        showActionColumn: true,
        actionColumn: {
          width: 180,
          fixed: 'right',
        },
        beforeFetch: (params) => {
          params.column = 'sort';
          params.order = 'asc';
        return Object.assign(params, {dataId: props.record.id});
      },
        // formConfig: {
        //   schemas: searchConfigFormSchema,
        // },
        rowKey: "id",
      },
    });

    /**
     * 创建操作列
     */
    function createActions(record: EditRecordRow): ActionItem[] {
      return [
        {
          label: '编辑',
          onClick: handleEdit.bind(null, Object.assign(record, {dataId: props.record.id})),
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
            confirm: handleDelete.bind(null, record, 0),
          },
        },
        {
          label: '同步删除其他清洗点',
          popConfirm: {
            title: '是否删除？',
            confirm: handleDelete.bind(null, record, 1),
          },
        },
      ]
    }

    // BasicTable绑定注册
    const [registerTable, { reload }] = tableContext;

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
      openModal(true, {'isAdd': false, 'isDetail': false, 'deviceData': props.record, 'record': record})
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
    async function handleDelete(record, isSync) {
    try {
      record.isSync = isSync;
      record.copDeviceId = props.record.copDeviceId;
      await deleteData(record, handleSuccess);
      await reload();
    } catch (error) {
        msg.error({ content: '操作失败', key: 'saving' });
    }
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
