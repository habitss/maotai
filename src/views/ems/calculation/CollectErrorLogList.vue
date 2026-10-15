<template>
  <div class="p-2">
    <!--查询区域-->
    <div class="jeecg-basic-table-form-container">
      <a-form ref="formRef" @keyup.enter="searchQuery" :model="queryParam" :label-col="labelCol" :wrapper-col="wrapperCol">
        <a-row :gutter="24">
          <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
            <a-form-item name="factoryDeptCode">
              <template #label><span title="部门区域">部门区域</span></template>
              <JDeptCascader
                ref="deptCascaderRef"
                v-model:modelValue="queryParam.factoryDeptCode"
                :show-default="false"
                @change="deptChangeCascader"
              />
            </a-form-item>
          </a-col>
          <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
            <a-form-item name="tagId">
              <template #label><span title="关联点位">关联点位</span></template>
              <j-search-select
                :idAndLabel="true"
                v-model:value="queryParam.tagId"
                dict="mes3_pi_base_point WHERE energy_type != '',point_name,tag_id"
                placeholder="请选择关联点位"
                allow-clear
              />
            </a-form-item>
          </a-col>
          <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
            <a-form-item name="errorType">
              <template #label><span title="错误类型">错误类型</span></template>
              <j-dict-select-tag
                placeholder="请选择错误类型"
                v-model:value="queryParam.errorType"
                dictCode="mes3_mdm_dict where group_code = 'EMS_CollectErrorType',item_key,item_val"
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
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button type="primary" v-auth="'ems:ems_collect_error_log:exportXls'" preIcon="ant-design:export-outlined" @click="onExportXls">
          导出</a-button
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
          <a-button v-auth="'ems:ems_collect_error_log:deleteBatch'"
            >批量操作
            <Icon icon="mdi:chevron-down" />
          </a-button>
        </a-dropdown>
        <!-- 高级查询 -->
        <super-query v-if="false" :config="superQueryConfig" @search="handleSuperQuery" />
      </template>
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
    <!-- 表单区域 -->
    <CollectErrorLogModal ref="registerModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" name="ems-collectErrorLog" setup>
  import { ref, reactive } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { columns, superQuerySchema } from './CollectErrorLog.data';
  import { list, deleteOne, batchDelete, getImportUrl, getExportUrl } from './CollectErrorLog.api';
  import CollectErrorLogModal from './components/CollectErrorLogModal.vue';
  import JSearchSelect from '/@/components/Form/src/jeecg/components/JSearchSelect.vue';
  import JDeptCascader from '../components/JDeptCascader.vue';
  import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';

  const formRef = ref();
  const deptCascaderRef = ref();
  const queryParam = reactive<any>({});
  const registerModal = ref();
  //注册table数据
  const { tableContext, onExportXls } = useListPage({
    tableProps: {
      title: '采集异常日志表',
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
        return Object.assign(params, queryParam);
      },
      //自定义默认排序
      defSort: {
        column: 'collectTime',
        order: 'desc',
      },
    },
    exportConfig: {
      name: '采集异常日志表',
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

  /**
   * 高级查询事件
   */
  function handleSuperQuery(params) {
    Object.keys(params).map((k) => {
      queryParam[k] = params[k];
    });
    searchQuery();
  }

  //分级部门选择事件
  function deptChangeCascader(selectDepts) {
    console.log(selectDepts);
    if (selectDepts.length > 0) {
      queryParam.factoryCode = selectDepts[0];
      if (selectDepts.length > 1) {
        queryParam.deptCode = selectDepts[1];
        if (selectDepts.length > 2) {
          queryParam.lineCode = selectDepts[2];
        } else {
          delete queryParam.lineCode;
        }
      } else {
        delete queryParam.deptCode;
        delete queryParam.lineCode;
      }
    } else {
      delete queryParam.factoryCode;
      delete queryParam.deptCode;
      delete queryParam.lineCode;
    }
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
    deptChangeCascader([]);
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
      },
      {
        label: '删除',
        popConfirm: {
          title: '是否确认删除',
          confirm: handleDelete.bind(null, record),
          placement: 'topLeft',
          getPopupContainer: () => document.body,
        },
        auth: 'ems:ems_collect_error_log:delete',
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
