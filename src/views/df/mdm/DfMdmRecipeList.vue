<template>
  <div class="p-2">
    <!--查询区域-->
    <div class="jeecg-basic-table-form-container">
      <a-form ref="formRef" @keyup.enter="searchQuery" :model="queryParam" :label-col="labelCol" :wrapper-col="wrapperCol">
        <a-row :gutter="24">
          <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
            <a-form-item name="facDesc">
              <template #label><span title="工厂名称 FAC_DESC">工厂名称</span></template>
              <a-input placeholder="请输入工厂名称 FAC_DESC" v-model:value="queryParam.facDesc" allow-clear />
            </a-form-item>
          </a-col>
          <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
            <a-form-item name="facCode">
              <template #label><span title="工厂编码 FAC_CODE">工厂编码</span></template>
              <a-input placeholder="请输入工厂编码 FAC_CODE" v-model:value="queryParam.facCode" allow-clear />
            </a-form-item>
          </a-col>
          <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
            <a-form-item name="deparDesc">
              <template #label><span title="部门名称 DEPAR_DESC">部门名称</span></template>
              <a-input placeholder="请输入部门名称 DEPAR_DESC" v-model:value="queryParam.deparDesc" allow-clear />
            </a-form-item>
          </a-col>
          <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
            <a-form-item name="processDesc">
              <template #label><span title="工序描述 PROCESS_DESC">工序描述</span></template>
              <a-input placeholder="请输入工序描述 PROCESS_DESC" v-model:value="queryParam.processDesc" allow-clear />
            </a-form-item>
          </a-col>
          <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
            <a-form-item name="lineCode">
              <template #label><span title="产线编码 LINE_CODE">产线编码</span></template>
              <a-input placeholder="请输入产线编码 LINE_CODE" v-model:value="queryParam.lineCode" allow-clear />
            </a-form-item>
          </a-col>
          <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
            <a-form-item name="facRecipeName">
              <template #label><span title="工厂配方名称 FAC_RECIPE_NAME">工厂配方名称</span></template>
              <a-input placeholder="请输入工厂配方名称 FAC_RECIPE_NAME" v-model:value="queryParam.facRecipeName" allow-clear />
            </a-form-item>
          </a-col>
          <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
            <a-form-item name="facRecipeCode">
              <template #label><span title="工厂配方编码 FAC_RECIPE_CODE">工厂配方编码</span></template>
              <a-input placeholder="请输入工厂配方编码 FAC_RECIPE_CODE" v-model:value="queryParam.facRecipeCode" allow-clear />
            </a-form-item>
          </a-col>
          <template v-if="toggleSearchStatus">
            <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
              <a-form-item name="zoneRecipeCode">
                <template #label><span title="区域配方编码 ZONE_RECIPE_CODE">区域配方编码</span></template>
                <a-input placeholder="请输入区域配方编码 ZONE_RECIPE_CODE" v-model:value="queryParam.zoneRecipeCode" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
              <a-form-item name="zoneRecipeCnName">
                <template #label><span title="区域配方中文名 ZONE_RECIPE_CN_NAME">区域配方中文名</span></template>
                <a-input placeholder="请输入区域配方中文名 ZONE_RECIPE_CN_NAME" v-model:value="queryParam.zoneRecipeCnName" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
              <a-form-item name="busineStatus">
                <template #label><span title="业务状态 BUSINE_STATUS">业务状态</span></template>
                <j-dict-select-tag allowClear v-model:value="queryParam.busineStatus" :options="busineStatusOptions" placeholder="请选择" />
              </a-form-item>
            </a-col>
            <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
              <a-form-item name="enableFlag">
                <template #label><span title="有效状态">有效状态</span></template>
                <j-dict-select-tag allowClear v-model:value="queryParam.enableFlag" :options="enableFlagOptions" placeholder="请选择" />
              </a-form-item>
            </a-col>
          </template>
          <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
            <span style="float: left; overflow: hidden" class="table-page-search-submitButtons">
              <a-button type="primary" preIcon="ant-design:search-outlined" @click="searchQuery">查询</a-button>
              <a-button type="primary" preIcon="ant-design:reload-outlined" @click="searchReset" style="margin-left: 8px">重置</a-button>
              <a @click="toggleSearchStatus = !toggleSearchStatus" style="margin-left: 8px">
                {{ toggleSearchStatus ? '收起' : '展开' }}
                <Icon :icon="toggleSearchStatus ? 'ant-design:up-outlined' : 'ant-design:down-outlined'" />
              </a>
            </span>
          </a-col>
        </a-row>
      </a-form>
    </div>
    <!--引用表格-->
    <BasicTable @register="registerTable">
      <!--插槽:table标题-->
      <template #tableTitle>
        <!-- 高级查询 -->
        <super-query :config="superQueryConfig" @search="handleSuperQuery" />
      </template>
    </BasicTable>
    <!-- 表单区域 -->
    <DfMdmRecipeModal ref="registerModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" name="DfMdmRecipe" setup>
  import { ref, reactive } from 'vue';
  import { BasicTable } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';
  import { columns, superQuerySchema, enableFlagOptions, busineStatusOptions } from './DfMdmRecipe.data';
  import { list } from './DfMdmRecipe.api';
  import DfMdmRecipeModal from './components/DfMdmRecipeModal.vue';

  const formRef = ref();
  const queryParam = reactive<any>({});
  const toggleSearchStatus = ref<boolean>(false);
  const registerModal = ref();
  //注册table数据
  const { tableContext } = useListPage({
    tableProps: {
      title: 'MDM配方主数据',
      api: list,
      columns,
      useSearchForm: false,
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
        onClick: handleEdit.bind(null, record),
        auth: 'mes3_mdm_df_recipe:edit',
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
        auth: 'mes3_mdm_df_recipe:delete',
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
