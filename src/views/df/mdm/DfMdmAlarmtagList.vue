<template>
  <div class="p-2">
    <!--查询区域-->
    <div class="jeecg-basic-table-form-container">
      <a-form ref="formRef" @keyup.enter="searchQuery" :model="queryParam" :label-col="labelCol" :wrapper-col="wrapperCol">
        <a-row :gutter="24">
          <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
            <a-form-item name="uniqId">
              <template #label><span title="业务唯一标识 UNIQ_ID">业务唯一标识</span></template>
              <a-input placeholder="请输入业务唯一标识 UNIQ_ID" v-model:value="queryParam.uniqId" allow-clear />
            </a-form-item>
          </a-col>
          <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
            <a-form-item name="tagFunlocCode">
              <template #label><span title="TAG功能位置编码 TAG_FUNLOC_CODE">功能位置编码</span></template>
              <a-input placeholder="请输入TAG功能位置编码 TAG_FUNLOC_CODE" v-model:value="queryParam.tagFunlocCode" allow-clear />
            </a-form-item>
          </a-col>

          <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
            <a-form-item name="tagFunlocCode2">
              <template #label><span title="TAG功能位置层级描述 TAG_FUNLOC_CODE2">功能位置层级描述</span></template>
              <a-input placeholder="请输入TAG功能位置层级描述 TAG_FUNLOC_CODE2" v-model:value="queryParam.tagFunlocCode2" allow-clear />
            </a-form-item>
          </a-col>
          <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
            <a-form-item name="vfunlocDesc">
              <template #label><span title="功能位置描述 V_FUNLOC_DESC">功能位置描述</span></template>
              <a-input placeholder="请输入功能位置描述 V_FUNLOC_DESC" v-model:value="queryParam.vfunlocDesc" allow-clear />
            </a-form-item>
          </a-col>
          <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
            <a-form-item name="vfunlocDesc2">
              <template #label><span title="标准点位功能位置 V_FUNLOC_DESC2">标准点位功能位置</span></template>
              <a-input placeholder="请输入标准点位功能位置 V_FUNLOC_DESC2" v-model:value="queryParam.vfunlocDesc2" allow-clear />
            </a-form-item>
          </a-col>
          <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
            <a-form-item name="enableFlag">
              <template #label><span title="有效状态">有效状态</span></template>
              <j-dict-select-tag allowClear v-model:value="queryParam.enableFlag" :options="enableFlagOptions" placeholder="请选择" />
            </a-form-item>
          </a-col>
          <template v-if="toggleSearchStatus"></template>
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
        <a-button v-if="false" type="primary" v-auth="'df.mdm:mes3_mdm_df_alarmtag:add'" @click="handleAdd" preIcon="ant-design:plus-outlined">
          新增</a-button
        >
        <a-button type="primary" v-auth="'df.mdm:mes3_mdm_df_alarmtag:exportXls'" preIcon="ant-design:export-outlined" @click="onExportXls">
          导出</a-button
        >
        <j-upload-button
          v-if="false"
          type="primary"
          v-auth="'df.mdm:mes3_mdm_df_alarmtag:importExcel'"
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
          <a-button v-auth="'df.mdm:mes3_mdm_df_alarmtag:deleteBatch'"
            >批量操作
            <Icon icon="mdi:chevron-down" />
          </a-button>
        </a-dropdown>
        <!-- 高级查询 -->
        <super-query :config="superQueryConfig" @search="handleSuperQuery" />
      </template>
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
    <!-- 表单区域 -->
    <DfMdmAlarmtagModal ref="registerModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" name="df.mdm-dfMdmAlarmtag" setup>
  import { ref, reactive } from 'vue';
  import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { columns, superQuerySchema } from './DfMdmAlarmtag.data';
  import { list, deleteOne, batchDelete, getImportUrl, getExportUrl } from './DfMdmAlarmtag.api';
  import { enableFlagOptions } from './DfMdmRecipe.data';
  import DfMdmAlarmtagModal from './components/DfMdmAlarmtagModal.vue';

  const formRef = ref();
  const queryParam = reactive<any>({});
  const toggleSearchStatus = ref<boolean>(false);
  const registerModal = ref();
  //注册table数据
  const { tableContext, onExportXls, onImportXls } = useListPage({
    tableProps: {
      title: 'MDM-报警点位主数据表',
      api: list,
      columns,
      canResize: false,
      useSearchForm: false,
      actionColumn: {
        title: '操作',
        width: 120,
        fixed: 'right',
      },
      beforeFetch: async (params) => {
        const newParams = { ...queryParam };
        Object.keys(queryParam).forEach((key) => {
          const val = queryParam[key];
          if (typeof val === 'string' && val.trim() !== '') {
            newParams[key] = '*' + val + '*'; // 加 * 触发模糊搜索
          } else {
            newParams[key] = val;
          }
        });
        return Object.assign(params, newParams);
      },
    },
    exportConfig: {
      name: 'MDM-报警点位主数据表',
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
    md: 8,
    lg: 8,
    xl: 8,
    xxl: 8,
  });
  const wrapperCol = reactive({});

  // 高级查询配置
  const superQueryConfig = reactive(superQuerySchema);

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
        label: '详情',
        onClick: handleDetail.bind(null, record),
        auth: 'df.mdm:mes3_mdm_df_alarmtag:edit',
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
        auth: 'df.mdm:mes3_mdm_df_alarmtag:delete',
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
  /deep/ .ant-table-thead > tr > th {
    white-space: normal !important;
  }
  /deep/ .ant-table-tbody > tr > td {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
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
