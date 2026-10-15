<template>
  <div class="flex-row">
    <div class="left-container">
      <GrafanaInstanceSideList @selected-instance="handleSelectInstance" />
    </div>
    <div class="p-2 right-container">
      <!--查询区域-->
      <div class="jeecg-basic-table-form-container">
        <a-form ref="formRef" @keyup.enter="searchQuery" :model="queryParam" :label-col="labelCol" :wrapper-col="wrapperCol">
          <a-row :gutter="24">
            <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
              <a-form-item name="uid">
                <template #label><span title="报表ID">报表ID</span></template>
                <a-input placeholder="请输入报表ID" v-model:value="queryParam.uid" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
              <a-form-item name="title">
                <template #label><span title="报表标题">报表标题</span></template>
                <a-input placeholder="请输入报表标题" v-model:value="queryParam.title" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
              <span style="float: left; overflow: hidden" class="table-page-search-submitButtons">
                <a-button type="primary" preIcon="ant-design:search-outlined" @click="searchQuery">查询</a-button>
                <a-button type="primary" preIcon="ant-design:reload-outlined" @click="searchReset" style="margin-left: 8px">重置</a-button>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>
      <!--引用表格-->
      <BasicTable @register="registerTable" :rowSelection="rowSelection">
        <!--插槽:table标题-->
        <template #tableTitle>
          <a-button type="primary" v-auth="'grafana:grafana_dashboard_backup:add'" @click="handleAdd" preIcon="ant-design:plus-outlined">
            新增</a-button
          >
          <a-button
            v-if="false"
            type="primary"
            v-auth="'grafana:grafana_dashboard_backup:exportXls'"
            preIcon="ant-design:export-outlined"
            @click="onExportXls"
          >
            导出</a-button
          >
          <j-upload-button
            v-if="false"
            type="primary"
            v-auth="'grafana:grafana_dashboard_backup:importExcel'"
            preIcon="ant-design:import-outlined"
            @click="onImportXls"
            >导入</j-upload-button
          >
          <a-dropdown v-if="selectedRowKeys.length > 0">
            <template #overlay>
              <a-menu>
                <a-menu-item key="1" @click="batchHandleDelete">
                  <Icon icon="ant-design:delete-outlined" />
                  删除
                </a-menu-item>
              </a-menu>
            </template>
            <a-button v-auth="'grafana:grafana_dashboard_backup:deleteBatch'"
              >批量操作
              <Icon icon="mdi:chevron-down" />
            </a-button>
          </a-dropdown>
        </template>
        <!--操作栏-->
        <template #action="{ record }">
          <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
        </template>
      </BasicTable>
      <!-- 表单区域 -->
      <GrafanaDashboardBackupModal ref="registerModal" @success="handleSuccess" />
    </div>
  </div>
</template>

<script lang="ts" name="grafana-grafanaDashboardBackup" setup>
  import { ref, reactive } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { columns } from './GrafanaDashboardBackup.data';
  import { list, deleteOne, batchDelete, getImportUrl, getExportUrl } from './GrafanaDashboardBackup.api';
  import GrafanaDashboardBackupModal from './components/GrafanaDashboardBackupModal.vue';
  import GrafanaInstanceSideList from './GrafanaInstanceSideList.vue';
  import { useRouter } from 'vue-router';

  const router = useRouter();
  const formRef = ref();
  const queryParam = reactive<any>({});
  const registerModal = ref();
  //注册table数据
  const { tableContext, onExportXls, onImportXls } = useListPage({
    tableProps: {
      title: 'Grafana报表JSON备份表',
      api: list,
      columns,
      canResize: false,
      useSearchForm: false,
      immediate: false, //不立即加载，手动reload
      actionColumn: {
        title: '操作',
        width: 120,
        fixed: 'right',
      },
      beforeFetch: async (params) => {
        const paramsObj = Object.assign(params, queryParam);
        if (paramsObj.title) {
          paramsObj.title = '*' + paramsObj.title + '*';
        }
        return paramsObj;
      },
    },
    exportConfig: {
      name: 'Grafana报表JSON备份表',
      url: getExportUrl,
      params: queryParam,
    },
    importConfig: {
      url: getImportUrl,
      success: handleSuccess,
    },
  });
  const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;
  const labelCol = reactive({
    xs: 12,
    sm: 8,
    md: 6,
    lg: 8,
    xl: 6,
    xxl: 6,
  });
  const wrapperCol = reactive({});

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
   * 批量删除事件
   */
  async function batchHandleDelete() {
    await batchDelete({ ids: selectedRowKeys.value }, handleSuccess);
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
        label: '迁移',
        onClick: handleMigration.bind(null, record),
      },
    ];
  }

  /**
   * 下拉操作栏
   */
  function getDropDownAction(record) {
    return [
      {
        label: '编辑',
        onClick: handleEdit.bind(null, record),
        auth: 'grafana:grafana_dashboard_backup:edit',
      },
      {
        label: '删除',
        popConfirm: {
          title: '是否确认删除',
          confirm: handleDelete.bind(null, record),
          placement: 'topLeft',
          getPopupContainer: () => document.body,
        },
        auth: 'grafana:grafana_dashboard_backup:delete',
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

  /**
   * 左侧GF实例列表选中行事件
   * @author Rocye
   */
  function handleSelectInstance(record) {
    queryParam.instanceId = record.id;
    queryParam.pageNo = 1;
    reload();
  }

  /**
   * 迁移事件
   * @author Rocye
   */
  function handleMigration(record: Recordable) {
    goDashboardExtractPath(record.id);
  }

  /**
   * 跳转到报表解析迁移及点位配置页面
   * @author Rocye
   */
  function goDashboardExtractPath(id: number) {
    const path = '/grafana/dashboardExtractMapping';
    router.push({ path, query: { id } });
  }
</script>

<style lang="less" scoped>
  .left-container {
    width: 400px;
    flex-shrink: 0; /* 不被压缩 */
    border-right: 1px solid var(--color-border-weak);
  }
  .right-container {
    overflow-x: auto;
    padding-left: 0px;
    width: 100%;
  }
  .jeecg-basic-table-form-container {
    padding: 0;
    .table-page-search-submitButtons {
      display: block;
      margin-bottom: 24px;
      white-space: nowrap;
    }
    .query-group-cust {
      min-width: 100px !important;
    }
    .query-group-split-cust {
      width: 30px;
      display: inline-block;
      text-align: center;
    }
    .ant-form-item:not(.ant-form-item-with-help) {
      margin-bottom: 16px;
      height: 32px;
    }
    :deep(.ant-picker),
    :deep(.ant-input-number) {
      width: 100%;
    }
  }
</style>
