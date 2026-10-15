<template>
  <div class="p-2">
    <!--查询区域-->
    <div class="jeecg-basic-table-form-container">
      <a-form ref="formRef" @keyup.enter="searchQuery" :model="queryParam" :label-col="labelCol" :wrapper-col="wrapperCol">
        <a-row :gutter="24">
          <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
            <a-form-item name="appId">
              <template #label>应用标识</template>
              <a-input placeholder="请输入应用标识" v-model:value="queryParam.appId" allow-clear />
            </a-form-item>
          </a-col>
          <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
            <a-form-item name="appName">
              <template #label>应用名称</template>
              <a-input placeholder="请输入应用名称" v-model:value="queryParam.appName" allow-clear />
            </a-form-item>
          </a-col>
          <template v-if="toggleSearchStatus">
            <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
              <a-form-item name="status">
                <template #label>状态：</template>
                <j-dict-select-tag
                  placeholder="请选择状态"
                  v-model:value="queryParam.status"
                  dictCode="mes3_mdm_dict WHERE group_code = 'EMS_StateYN',item_key,item_val"
                  allow-clear
                />
              </a-form-item>
            </a-col>
          </template>
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
        <a-button type="primary" v-auth="'alarm:alarm_api_app:add'" @click="handleAdd" preIcon="ant-design:plus-outlined"> 新增</a-button>
        <a-button v-if="false" type="primary" v-auth="'alarm:alarm_api_app:exportXls'" preIcon="ant-design:export-outlined" @click="onExportXls">
          导出</a-button
        >
        <j-upload-button
          v-if="false"
          type="primary"
          v-auth="'alarm:alarm_api_app:importExcel'"
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
          <a-button v-auth="'alarm:alarm_api_app:deleteBatch'"
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
    <AlarmApiAppModal ref="registerModal" @success="handleSuccess" />
    <!-- 密钥成功模态框 -->
    <AppSecretSuccessModal
      ref="appSecretModal"
      v-model:open="showAppSecretModal"
      :appId="resetSecretData.appId"
      :appSecret="resetSecretData.appSecret"
      :title="'重置成功'"
      :message="'密钥重置成功！'"
      :secretLabel="'新的 App Secret'"
      :showAppId="false"
      @ok="handleSecretModalOk"
    />
  </div>
</template>

<script lang="ts" name="alarm-alarmApiApp" setup>
  import { ref, reactive } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { columns } from './AlarmApiApp.data';
  import { list, deleteOne, batchDelete, getImportUrl, getExportUrl, resetSecret, cleanOldSecret } from './AlarmApiApp.api';
  import AlarmApiAppModal from './components/AlarmApiAppModal.vue';
  import AppSecretSuccessModal from './components/AppSecretSuccessModal.vue';
  import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';

  const formRef = ref();
  const queryParam = reactive<any>({});
  const toggleSearchStatus = ref<boolean>(false);
  const registerModal = ref();
  const appSecretModal = ref();
  const showAppSecretModal = ref(false);
  const resetSecretData = ref({
    appId: '',
    appSecret: '',
  });
  //注册table数据
  const { createMessage } = useMessage();

  const { tableContext, onExportXls, onImportXls } = useListPage({
    tableProps: {
      title: '报警接口应用表',
      api: list,
      columns,
      canResize: false,
      useSearchForm: false,
      actionColumn: {
        title: '操作',
        width: 150,
        fixed: 'right',
      },
      beforeFetch: async (params) => {
        return Object.assign(params, queryParam);
      },
    },
    exportConfig: {
      name: '报警接口应用表',
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
        label: '编辑',
        onClick: handleEdit.bind(null, record),
        auth: 'alarm:alarm_api_app:edit',
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
        label: '重置密钥',
        onClick: handleResetSecret.bind(null, record),
        auth: 'alarm:alarm_api_app:edit',
      },
      {
        label: '清理旧密钥',
        onClick: handleCleanOldSecret.bind(null, record),
        auth: 'alarm:alarm_api_app:edit',
      },
      {
        label: '删除',
        popConfirm: {
          title: '是否确认删除',
          confirm: handleDelete.bind(null, record),
          placement: 'topLeft',
          getPopupContainer: () => document.body,
        },
        auth: 'alarm:alarm_api_app:delete',
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
   * 重置应用密钥
   */
  async function handleResetSecret(record) {
    try {
      const res = await resetSecret(record.appId);
      if (res.success) {
        // 设置重置密钥数据并显示模态框
        resetSecretData.value = {
          appId: record.appId,
          appSecret: res.result,
        };
        showAppSecretModal.value = true;
      } else {
        createMessage.warning(res.message);
      }
    } catch (error) {
      console.error('请求异常:', error);
      createMessage.error('请求失败');
    }
  }

  /**
   * 清理过期旧密钥
   */
  async function handleCleanOldSecret(record) {
    try {
      const res = await cleanOldSecret(record.appId);
      if (res.success) {
        createMessage.success('清理成功');
        reload();
      } else {
        createMessage.warning(res.message);
      }
    } catch (error) {
      createMessage.error('请求失败');
    }
  }

  /**
   * 密钥模态框确认回调
   */
  function handleSecretModalOk() {
    reload();
  }
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
