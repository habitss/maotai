<template>
  <BasicTable @register="registerTable">
    <template #tableTitle>
      <a-button type="primary" preIcon="ep:plus" @click="onAddRow">新增</a-button>
    </template>
    <template #action="{ record, column }">
      <TableAction :actions="createActions(record, column)" />
    </template>
  </BasicTable>
  <Modal @register="registerAddModal" :width="600" @success="reload" />
  <TemplateModal @register="registerTemplateModal" @success="reload" />
  <a-modal 
    width="800"
    :title="title" 
    :visible="visible" 
    @ok="handleOk" 
    okText="复制URL地址"
    @cancel="handleCloseMenuUrlModal" 
    cancelText="关闭"
  >
    <div style="height: 100px; padding: 20px; color: #909399;">
      {{ menuUrlToCopy }}
    </div>
  </a-modal>
</template>
<script lang="ts" name="dataCalculation-report-template-index" setup>
  import { ref } from 'vue';
  import { ActionItem, BasicTable, BasicColumn, TableAction, EditRecordRow } from '/@/components/Table';
  import { getList, deleteData } from './index.api';
  import { columns, searchFormSchema } from './index.data';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useCopyToClipboard } from '/@/hooks/web/useCopyToClipboard';
  import { useModal } from '/@/components/Modal';
  import Modal from './components/actionModal.vue';
  import TemplateModal from './components/templateModal.vue';

  //配置菜单地址modal
  const visible = ref<boolean>(false);
  //配置菜单地址modal标题
  const title = ref<string>('');
  //配置菜单复制文本
  const menuUrlToCopy = ref('');

  //新增/编辑modal
  const [registerAddModal, { openModal }] = useModal();

  //报表模板modal
  const [registerTemplateModal, { openModal : openTemplateModal }] = useModal();
  
  // 列表页面公共参数、方法
  const { tableContext } = useListPage({
    tableProps: {
      title: '模板配置',
      api: getList,
      columns: columns,
      expandRowByClick: true,
      showActionColumn: true,
      actionColumn: {
        width: 260,
        fixed: 'right',
      },
      rowSelection: {
        columnWidth: 20,
      },
      formConfig: {
        schemas: searchFormSchema,
        fieldMapToTime: [['createTime', ['createTime_begin', 'createTime_end'], 'YYYY-MM-DD']],
      },
      beforeFetch: (params) => {
        params.reportType = 1;
        return params;
      }
    },
  });

  // BasicTable绑定注册
  const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;
  
  //消息提示
  const { createMessage: msg } = useMessage();

  /**
   * 创建操作列
   */
  function createActions(record: EditRecordRow, column: BasicColumn): ActionItem[] {
    return [
      {
        label: '编辑',
        onClick: handleEdit.bind(null, record),
      },
      {
        label: '配置模板',
        onClick: handleOpenTemplateModal.bind(null, record),
      },
      {
        label: '配置菜单',
        onClick: handleShowMenuUrlModal.bind(null, record),
      },
      {
        label: '删除',
        popConfirm: {
          title: '是否删除',
          confirm: handleDelete.bind(null, record, column),
        },
      },
    ];
  }

  /**
   * 打开新增modal
   */
  function onAddRow(){
    openModal(true, {'isAdd': true})
  }

  /**
   * 开启编辑
   */
  function handleEdit(record: EditRecordRow) {
    openModal(true, {'isAdd': false, 'record': record})
  }

  /**
   * 删除
   */
   async function handleDelete(record: EditRecordRow) {
    try {
      //调用后台修改接口, 删除
      await deleteData({ id: record.id }, handleSuccess);
      reload();
    } catch (error) {
      msg.error({ content: '操作失败', key: 'saving' });
    }
  }

  /**
   * 打开报表模板modal
   */
  function handleOpenTemplateModal(record){
    openTemplateModal(true, {record})
  }

  /**
   * 打开配置菜单链接
   */
  function handleShowMenuUrlModal(record){
    if(!record.reportConfig){
      msg.error({ content: '请先配置模板' });
      return;
    }
    title.value = '【'+record.reportName+'】菜单地址';
    menuUrlToCopy.value = '/dataCalculation/customReport/reportQuery/'+record.id;
    visible.value = true;
  }

  /**
   * 关闭配置菜单链接
   */
  function handleCloseMenuUrlModal(){
    visible.value = false;
  }

  /**
   * 复制配置菜单链接
   */
  async function handleOk(){
    const { isSuccessRef } = useCopyToClipboard(menuUrlToCopy.value);
    if(isSuccessRef.value){
      msg.success('复制URL成功');
    } else {
      msg.error('复制URL失败，请手动复制链接');
    }
  }

  /**
   * 成功回调
   */
  function handleSuccess() {
    (selectedRowKeys.value = []) && reload();
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
