<template>
  <spin :spinning="pageLoading">
    <div class="p-2">
      <!--查询区域-->
      <div class="jeecg-basic-table-form-container">
        <a-form ref="formRef" @keyup.enter="searchQuery" :model="queryParam" class="inline-form-wrap">
          <a-form-item name="instanceName" class="inline-form-item">
            <a-input placeholder="请输入GF实例名称" v-model:value="queryParam.instanceName" allow-clear />
          </a-form-item>
          <a-form-item class="inline-form-item">
            <a-button type="primary" preIcon="ant-design:search-outlined" @click="searchQuery" />
            <a-button
              type="primary"
              v-auth="'grafana:grafana_instance:add'"
              @click="handleAdd"
              style="margin-left: 8px"
              preIcon="ant-design:plus-outlined"
            />
          </a-form-item>
        </a-form>
      </div>
      <!--引用表格-->
      <BasicTable @register="registerTable" :rowSelection="null" @row-click="handleRowClick" :rowClassName="setRowClassName">
        <!--操作栏-->
        <template #action="{ record }">
          <TableAction :actions="getTableAction(record)" />
        </template>
        <!--侧边栏显示标题-->
        <template #sideContent="{ record }">
          <div style="position: relative; text-align: left; padding-right: 12px" class="cell-with-corner">
            <a-tooltip :title="getStatusText(record.state)">
              <span class="corner-flag" :style="{ borderTopColor: getStatusColor(record.state) }" :title="getStatusText(record.state)"></span>
            </a-tooltip>
            <div>{{ record.instanceName }}</div>
            <div style="font-size: 10px; color: #d92622">{{ record.instanceUrl }}</div>
          </div>
        </template>
      </BasicTable>
      <!-- 表单区域 -->
      <GrafanaInstanceModal ref="registerModal" @success="handleSuccess" />
    </div>
  </spin>
</template>

<script lang="ts" name="grafana-InstanceSideList" setup>
  import { ref, reactive } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useMessage } from '/@/hooks/web/useMessage';
  const { createMessage, createConfirm } = useMessage();
  import { sideColumns } from './GrafanaInstance.data';
  import { list, deleteOne } from './GrafanaInstance.api';
  import { doCrawlDashboardData } from './GrafanaDashboardBackup.api';
  import GrafanaInstanceModal from './components/GrafanaInstanceModal.vue';

  const formRef = ref();
  const queryParam = reactive<any>({});
  const registerModal = ref();
  const pageLoading = ref(false);
  //注册table数据
  const { tableContext } = useListPage({
    tableProps: {
      api: list,
      columns: sideColumns,
      canResize: false,
      pagination: false,
      useSearchForm: false,
      showHeader: false,
      showTableSetting: false,
      actionColumn: {
        title: '操作',
        width: 100,
        fixed: 'right',
      },
      beforeFetch: async (params) => {
        const paramsObj = Object.assign(params, queryParam);
        paramsObj.pageSize = 999999;
        return paramsObj;
      },
      afterFetch: (data) => {
        if (data.length > 0) {
          selectedRowKey.value = data[0].id;
          emit('selectedInstance', data[0]);
        }
        return data;
      },
    },
  });
  const [registerTable, { reload }, { selectedRowKeys }] = tableContext;

  //单击选中行
  const selectedRowKey = ref(null);
  const emit = defineEmits(['selectedInstance']);

  /**
   * 获取状态颜色
   */
  function getStatusColor(status) {
    switch (status) {
      case 0: // 未备份
        return '#d9d9d9';
      case 1: // 备份中
        return '#1890ff';
      case 2: // 备份成功
        return '#52c41a';
      case 3: // 备份失败
        return '#ff4d4f';
      default: // 未知
        return '#bfbfbf';
    }
  }
  function getStatusText(status) {
    switch (status) {
      case 0:
        return '未备份';
      case 1:
        return '备份中';
      case 2:
        return '备份成功';
      case 3:
        return '备份失败';
      default:
        return '未知';
    }
  }

  /**
   * 备份按钮点击事件
   */
  function handleBackupData(record: Recordable) {
    if (record.state === 2) {
      createConfirm({
        iconType: 'warning',
        title: '系统提示',
        content: '此GF实例先前已备份成功，确定要重新备份吗？',
        onOk: () => {
          doCrawlDashboardBackupData(record.id);
        },
      });
    } else {
      doCrawlDashboardBackupData(record.id);
    }
  }

  /**
   * 获取报表备份数据
   * @param {string} instanceId 实例ID
   * @author Rocye
   */
  async function doCrawlDashboardBackupData(instanceId: string) {
    pageLoading.value = true;
    const res = await doCrawlDashboardData({ instanceId: instanceId });
    if (res.success) {
      createMessage.success(res.message);
      reload();
    } else {
      createMessage.error(res.message);
    }
    pageLoading.value = false;
  }

  /**
   * 新增事件
   */
  function handleAdd() {
    registerModal.value.disableSubmit = false;
    registerModal.value.add();
  }

  /**
   * 编辑事件
   */
  function handleEdit(record: Recordable) {
    registerModal.value.disableSubmit = false;
    registerModal.value.edit(record);
  }

  /**
   * 行点击事件
   */
  function handleRowClick(record: Recordable) {
    selectedRowKey.value = record.id;
    emit('selectedInstance', record);
  }

  /**
   * 表格行样式
   */
  function setRowClassName(record: Recordable) {
    return record.id === selectedRowKey.value ? 'custom-row-style selected-row' : 'custom-row-style';
  }

  /**
   * 详情
   */
  function handleDetail(record: Recordable) {
    registerModal.value.disableSubmit = true;
    registerModal.value.edit(record);
  }

  /**
   * 删除事件
   */
  async function handleDelete(record) {
    await deleteOne({ id: record.id }, handleSuccess);
  }

  /**
   * 成功回调
   */
  function handleSuccess() {
    selectedRowKeys.value = [];
    reload();
  }

  /**
   * 操作栏
   */
  function getTableAction(record) {
    return [
      {
        icon: 'ant-design:edit-outlined',
        tooltip: '编辑',
        onClick: handleEdit.bind(null, record),
        auth: 'grafana:grafana_instance:edit',
      },
      {
        icon: 'ant-design:save-outlined',
        tooltip: '备份',
        onClick: handleBackupData.bind(null, record),
        auth: 'grafana:grafana_instance:backupJson',
      },
    ];
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
    formRef.value.resetFields();
    selectedRowKeys.value = [];
    //刷新数据
    reload();
  }
</script>

<style lang="less" scoped>
  .cell-with-corner {
    position: relative;
    text-align: left;
    padding-right: 10px;
    line-height: 1.3;
  }
  /* 右上角三角 */
  .corner-flag {
    position: absolute;
    top: -8px;
    right: -8px;
    width: 0;
    height: 0;
    border-top: 15px solid #52c41a; /* 默认颜色 */
    border-left: 15px solid transparent;
    border-radius: 1px;
  }

  ::v-deep(.custom-row-style) {
    cursor: pointer !important;
  }
  ::v-deep(.selected-row) {
    background-color: #fcebeb !important; /* 选中高亮色 */
  }
  .inline-form-wrap {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
  }
  .inline-form-item {
    flex: 0 0 auto;
  }
  .jeecg-basic-table-form-container {
    padding: 0;
    .table-page-search-submitButtons {
      display: block;
      margin-bottom: 0px;
      white-space: nowrap;
    }
    .ant-form-item:not(.ant-form-item-with-help) {
      margin-bottom: 0px;
      height: 32px;
    }
    :deep(.ant-picker),
    :deep(.ant-input-number) {
      width: 100%;
    }
  }
</style>
