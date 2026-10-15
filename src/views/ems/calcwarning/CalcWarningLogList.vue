<template>
  <div class="p-2">
    <!--查询区域-->
    <div class="jeecg-basic-table-form-container">
      <a-form ref="formRef" @keyup.enter="searchQuery" :model="queryParam" :label-col="labelCol" :wrapper-col="wrapperCol">
        <a-row :gutter="24">
          <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
            <a-form-item name="tagKind">
              <template #label><span title="点位大类">点位大类</span></template>
              <j-dict-select-tag
                placeholder="请选择点位大类"
                v-model:value="queryParam.tagKind"
                dictCode="mes3_mdm_dict where group_code = 'EMS_TagKind',item_key,item_val"
                allow-clear
                @change="handleTagKindChange"
              />
            </a-form-item>
          </a-col>
          <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
            <a-form-item name="tagId">
              <template #label><span title="关联点位">关联点位</span></template>
              <j-search-select placeholder="请选择关联点位" v-model:value="queryParam.tagId" :dict="tagIdDictCode" :idAndLabel="true" allow-clear />
            </a-form-item>
          </a-col>
          <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
            <a-form-item name="grainType">
              <template #label><span title="时间周期">时间周期</span></template>
              <j-dict-select-tag
                placeholder="请选择时间周期"
                v-model:value="queryParam.grainType"
                dictCode="mes3_mdm_dict where group_code = 'EMS_GrainType',item_key,item_val"
                allow-clear
              />
            </a-form-item>
          </a-col>
          <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
            <a-form-item name="timeRange">
              <template #label><span title="时间区间">时间区间</span></template>
              <a-range-picker
                valueFormat="YYYY-MM-DD"
                :placeholder="['开始日期', '结束日期']"
                v-model:value="queryParam.timeRange"
                allow-clear
                @change="handleTimeRangeChange"
              />
            </a-form-item>
          </a-col>
          <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
            <a-form-item name="warnLevel">
              <template #label><span title="预警等级">预警等级</span></template>
              <j-dict-select-tag
                placeholder="请选择预警等级"
                v-model:value="queryParam.warnLevel"
                dictCode="mes3_mdm_dict where group_code = 'EMS_WarnLevel',item_key,item_val"
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
        <a-button type="primary" v-auth="'ems:ems_calc_warning_log:exportXls'" preIcon="ant-design:export-outlined" @click="onExportXls">
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
          <a-button v-auth="'ems:ems_calc_warning_log:deleteBatch'"
            >批量操作
            <Icon icon="mdi:chevron-down" />
          </a-button>
        </a-dropdown>
        <!-- 高级查询 -->
        <super-query :config="superQueryConfig" @search="handleSuperQuery" v-if="false" />
      </template>
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
    <!-- 表单区域 -->
    <CalcWarningLogModal ref="registerModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" name="ems-calcWarningLog" setup>
  import { ref, reactive } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { columns, superQuerySchema } from './CalcWarningLog.data';
  import { list, deleteOne, batchDelete, getImportUrl, getExportUrl } from './CalcWarningLog.api';
  import CalcWarningLogModal from './components/CalcWarningLogModal.vue';
  import JSearchSelect from '/@/components/Form/src/jeecg/components/JSearchSelect.vue';
  import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';

  const formRef = ref();
  const queryParam = reactive<any>({});
  const registerModal = ref();
  //注册table数据
  const { tableContext, onExportXls } = useListPage({
    tableProps: {
      title: '采集计算预警日志表',
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
        column: 'timePoint',
        order: 'desc',
      },
    },
    exportConfig: {
      name: '采集计算预警日志表',
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
  //动态获取字典
  const tagIdDictCode = ref<string>("mes3_pi_base_point,point_name,tag_id,energy_type!=''");

  /**
   * 高级查询事件
   */
  function handleSuperQuery(params) {
    Object.keys(params).map((k) => {
      queryParam[k] = params[k];
    });
    searchQuery();
  }

  // 处理点位大类变化
  const handleTagKindChange = (value) => {
    if (value === '1') {
      tagIdDictCode.value = "mes3_pi_base_point,point_name,tag_id,energy_type!=''";
    } else if (value === '2') {
      tagIdDictCode.value = 'ems_manual_tag,tag_name,tag_id';
    } else if (value === '3') {
      tagIdDictCode.value = 'ems_thirdparty_tag,tag_name,tag_id';
    } else {
      tagIdDictCode.value = 'ems_orsdata_tag,tag_name,tag_id';
    }
  };

  // 处理时间范围变化
  const handleTimeRangeChange = (dates: [string, string] | null) => {
    if (dates && dates.length === 2) {
      queryParam.startTime = dates[0];
      queryParam.endTime = dates[1];
    } else {
      delete queryParam.startTime;
      delete queryParam.endTime;
    }
  };

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
      },
      {
        label: '删除',
        popConfirm: {
          title: '是否确认删除',
          confirm: handleDelete.bind(null, record),
          placement: 'topLeft',
          getPopupContainer: () => document.body,
        },
        auth: 'ems:ems_calc_warning_log:delete',
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
    handleTimeRangeChange(null);
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
