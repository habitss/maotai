<template>
  <BasicTable @register="registerTable">
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
    <template #action="{ record, column }">
      <TableAction :actions="createActions(record, column)" />
    </template>
  </BasicTable>
  <Modal @register="registerAddModal" :width="1200" @success="handleSuccess" :key="dayjs().valueOf()" />
  <StandardCopyModal @register="registerCopyModal" :width="900" @success="copySuccess" :key="dayjs().valueOf()" />
</template>
<script lang="ts" name="dataCalculation-standard-value-list" setup>
  import { ActionItem, BasicTable, BasicColumn, TableAction, EditRecordRow } from '/@/components/Table';
  import { getList } from './standardValue.api';
  import { columns, searchFormSchema } from './standardValue.data';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useModal } from '/@/components/Modal';
  import Modal from './components/manageModal.vue';
  import StandardCopyModal from './components/standardCopyModal.vue';
  import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
  import dayjs from 'dayjs';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { reactive, watch } from 'vue';
  const { t } = useI18n();
  //查询参数对象
  const queryParam = reactive<any>({});

  //标准管理modal
  const [registerAddModal, { openModal }] = useModal();
  //标准复制modal
  const [registerCopyModal, { openModal:openCopyModal,closeModal:closeCopyModal }] = useModal();
  
  // 列表页面公共参数、方法
  const { tableContext } = useListPage({
    tableProps: {
      title: 'PI标准值配置',
      api: getList,
      beforeFetch: (params) => {
        params.column = "sort";
        params.order = "asc";
        return Object.assign(params, queryParam);
      },
      columns: columns,
      expandRowByClick: true,
      showActionColumn: true,
      actionColumn: {
        width: 150,
        fixed: 'right',
      },
      rowSelection: {
        columnWidth: 20,
      },
      formConfig: {
        schemas: searchFormSchema,
        fieldMapToTime: [['createTime', ['createTime_begin', 'createTime_end'], 'YYYY-MM-DD']],
      },
    },
  });

  // BasicTable绑定注册
  const [registerTable, { reload, getForm }, { rowSelection, selectedRowKeys }] = tableContext;
  
  //消息提示
  const { createMessage: msg } = useMessage();

  /**
   * 创建操作列
   */
  function createActions(record: EditRecordRow, column: BasicColumn): ActionItem[] {
    return [
      {
        label: '标准管理',
        onClick: handleOpenModal.bind(null, record),
      },
      {
        label: '标准复制',
        onClick: copyOpenModal.bind(null, record),
      },
    ];
  }

  /**
   * 打开标准管理
   */
  function handleOpenModal(record: EditRecordRow) {
    openModal(true, {record})
  }
  /**
   * 打开标准复制
   */
  function copyOpenModal(record: EditRecordRow) {
    openCopyModal(true, {record})
  }
  /**
   * 成功回调
   */
  function handleSuccess() {
    Modal.destroy();
    (selectedRowKeys.value = []) && reload();
  }
  /**
   * 成功回调
   */
  function copySuccess() {
    closeCopyModal();
    (selectedRowKeys.value = []) && reload();
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
