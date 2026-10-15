<template>
  <BasicModal
    @register="registerModal"
    v-bind="$attrs"
    title="请选择数据"
    cancelText="取消"
    :defaultFullscreen="false"
    :canFullscreen="false"
    :showOkBtn="false"
    :showCancelBtn="false"
    :destroyOnClose="true"
  >
    <BasicTable @register="registerTable" :rowClassName="getRowClassName" @row-click="handleRowClick">
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { list } from '/@/views/dataCalculation/pi/allocationTable/QueryTable.api';
  const emit = defineEmits(['success']);
  const queryParam = ref<any>({});
  // 记录当前选中的行
  const recipeRecord = ref({});
  // 列表页面公共参数、方法
  const { tableContext } = useListPage({
    tableProps: {
      title: '',
      api: list,
      columns: [],
      showActionColumn: true,
      canResize: true,
      useSearchForm: false,
      showTableSetting: false,
      maxHeight: 600,
      scroll: { y: 410 },
      pagination: {
        pageSizeOptions: ['10', '20'],
      },
      beforeFetch: (params) => {
        return Object.assign(params, queryParam.value);
      },

      actionColumn: {
        width: 60,
        fixed: 'right', // 固定在最右侧
      },
    },
  });

  // BasicTable绑定注册
  const [registerTable, { setProps }] = tableContext;

  //注册modal
  const [registerModal, { closeModal }] = useModalInner(async (data: any) => {
    //设置表格列和数据
    //设置查询条件
    queryParam.value = data.queryParam;
    setProps({
      columns: data.piColumns,
      // dataSource: data.records
    });
  });

  /**
   * 操作栏
   */
  function getTableAction(record) {
    return [
      {
        //编辑
        label: '选择',
        onClick: handleChoose.bind(null, record),
      },
    ];
  }

  function handleChoose(record) {
    emit('success', record);
    closeModal();
  }

  function handleRowClick(record, index, event) {
    console.log('点击的行数据：', record); // 当前行数据
    recipeRecord.value = record;
  }

  // 动态行样式
  function getRowClassName(record, index) {
    if (recipeRecord.value && recipeRecord.value.id === record.id) {
      return 'selected-row'; // 选中行的样式类
    }
    return '';
  }
</script>

<style lang="less" scoped>
  // 选中行样式 - 颜色加深
  :deep(.selected-row) {
    background-color: #e6f7ff !important; /* 浅蓝色背景 */
  }
  // 选中行内的操作按钮样式
  :deep(.selected-row) .ant-table-cell {
    background-color: #e6f7ff !important;
    border-radius: 4px;
    padding: 4px 12px;
  }
</style>
