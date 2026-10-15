<template>
  <a-spin :spinning="pageLoading">
    <div class="p-2">
      <!--查询区域-->
      <div class="jeecg-basic-table-form-container">
        <a-form ref="formRef" @keyup.enter="searchQuery" :model="queryParam" :label-col="labelCol" :wrapper-col="wrapperCol">
          <a-row :gutter="24">
            <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
              <a-form-item name="instanceName">
                <template #label><span title="GF实例名称">GF实例名称</span></template>
                <a-input placeholder="请输入GF实例名称" v-model:value="queryParam.instanceName" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
              <a-form-item name="instanceUrl">
                <template #label><span title="GF实例URL">GF实例URL</span></template>
                <a-input placeholder="请输入GF实例URL" v-model:value="queryParam.instanceUrl" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
              <a-form-item name="state">
                <template #label><span title="状态">状态</span></template>
                <j-dict-select-tag
                  placeholder="请选择状态"
                  v-model:value="queryParam.state"
                  dictCode="mes3_mdm_dict where group_code = 'GF_InstanceBackupState',item_key,item_val"
                  allow-clear
                />
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
        <template #nameSolt="{ record }">
          <div>{{ record.instanceName }}</div>
          <div style="font-size: 10px; color: #d92622">{{ record.instanceUrl }}</div>
        </template>
        <!--插槽:table标题-->
        <template #tableTitle>
          <a-button type="primary" v-auth="'grafana:grafana_instance:add'" @click="handleAdd" preIcon="ant-design:plus-outlined"> 新增</a-button>
          <a-button type="primary" v-auth="'grafana:grafana_instance:exportXls'" preIcon="ant-design:export-outlined" @click="onExportXls">
            导出</a-button
          >
          <j-upload-button type="primary" v-auth="'grafana:grafana_instance:importExcel'" preIcon="ant-design:import-outlined" @click="onImportXls"
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
            <a-button v-auth="'grafana:grafana_instance:deleteBatch'"
              >批量操作
              <Icon icon="mdi:chevron-down" />
            </a-button>
          </a-dropdown>
          <!-- 高级查询 -->
          <super-query v-if="false" :config="superQueryConfig" @search="handleSuperQuery" />
        </template>
        <!--操作栏-->
        <template #action="{ record }">
          <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
        </template>
      </BasicTable>
      <!-- 表单区域 -->
      <GrafanaInstanceModal ref="registerModal" @success="handleSuccess" />
    </div>

    <!-- 数据源列表弹窗 -->
    <j-modal
      title="GF实例数据源列表"
      width="80%"
      :visible="dlgGrafanaDsListVisible"
      :footer="false"
      :maxHeight="300"
      @cancel="handleCancel"
      cancelText="关闭"
    >
      <GrafanaDatasourceList ref="grafanaDsList" />
    </j-modal>
  </a-spin>
</template>

<script lang="ts" name="grafana-InstanceList" setup>
  import { ref, reactive, onMounted, nextTick } from 'vue';
  import { BasicTable, TableAction, BasicColumn } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useMessage } from '/@/hooks/web/useMessage';
  const { createMessage, createConfirm } = useMessage();
  import { getColumns, superQuerySchema } from './GrafanaInstance.data';
  import { list, deleteOne, batchDelete, getImportUrl, getExportUrl } from './GrafanaInstance.api';
  import { doCrawlDashboardData } from './GrafanaDashboardBackup.api';
  import { doCrawlDatasourceData, getDsCntMap } from './GrafanaDatasource.api';
  import GrafanaInstanceModal from './components/GrafanaInstanceModal.vue';
  import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';
  import JModal from '/@/components/Modal/src/JModal/JModal.vue';
  import GrafanaDatasourceList from './GrafanaDatasourceList.vue';

  const pageLoading = ref(false);
  const formRef = ref();
  const queryParam = reactive<any>({});
  const registerModal = ref();
  const columns: BasicColumn[] = getColumns(handleShowDsList);
  const dlgGrafanaDsListVisible = ref(false);
  const grafanaDsList = ref();

  //注册table数据
  const { tableContext, onExportXls, onImportXls } = useListPage({
    tableProps: {
      title: 'Grafana实例',
      api: list,
      columns: columns,
      canResize: false,
      useSearchForm: false,
      actionColumn: {
        title: '操作',
        width: 320,
        fixed: 'right',
      },
      beforeFetch: async (params) => {
        return Object.assign(params, queryParam);
      },
      afterFetch: (records) => {
        // 处理 dsCntMap 增加字段
        const newRecords = records.map((item) => ({
          ...item,
          dsCnt: dsCntMap.value[item.id],
        }));

        return newRecords;
      },
    },
    exportConfig: {
      name: 'Grafana实例',
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

  // 高级查询配置
  const superQueryConfig = reactive(superQuerySchema);

  const dsCntMap = ref<any>({});

  /**
   * 显示数据源列表
   */
  async function handleShowDsList(record) {
    dlgGrafanaDsListVisible.value = true;
    await nextTick();
    grafanaDsList.value?.showList(record.id);
  }

  /**
   * 高级查询事件
   */
  function handleSuperQuery(params) {
    Object.keys(params).map((k) => {
      queryParam[k] = params[k];
    });
    searchQuery();
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
        label: '备份报表JSON',
        onClick: handleBackupJsonData.bind(null, record),
        auth: 'grafana:grafana_instance:backupJson',
      },
      {
        label: '备份数据源',
        onClick: handleBackupDataSource.bind(null, record),
        auth: 'grafana:grafana_instance:backupDs',
      },
      {
        label: '编辑',
        onClick: handleEdit.bind(null, record),
        auth: 'grafana:grafana_instance:edit',
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
          title: '是否确认删除',
          confirm: handleDelete.bind(null, record),
          placement: 'topLeft',
          getPopupContainer: () => document.body,
        },
        auth: 'grafana:grafana_instance:delete',
      },
    ];
  }

  /**
   * 备份GF实例JSON按钮点击事件
   */
  function handleBackupJsonData(record: Recordable) {
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
   * 获取报表JSON备份数据
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
   * 备份GF实例数据源配置点击事件
   */
  function handleBackupDataSource(record: Recordable) {
    doCrawlGfDatasourceData(record.id);
  }

  /**
   * 获取GF实例数据源配置数据
   * @param {string} instanceId 实例ID
   * @author Rocye
   */
  async function doCrawlGfDatasourceData(instanceId: string) {
    pageLoading.value = true;
    const res = await doCrawlDatasourceData({ instanceId: instanceId });
    if (res.success) {
      createMessage.success(res.message);
      reload();
    } else {
      createMessage.error(res.message);
    }
    pageLoading.value = false;
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
   * 确定按钮回调事件
   */
  function handleOk() {}

  /**
   * 取消按钮回调事件
   */
  function handleCancel() {
    dlgGrafanaDsListVisible.value = false;
  }

  /**
   * 页面初始化
   */
  onMounted(() => {
    //加载GF实例数据源配置数据
    getDsCntMap().then((res) => {
      dsCntMap.value = res || {};
    });
  });
</script>

<style lang="less" scoped>
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
