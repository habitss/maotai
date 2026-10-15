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
            <a-form-item name="funlocCode">
              <template #label><span title="功能位置编码 FUNLOC_CODE">功能位置编码</span></template>
              <a-input placeholder="请输入功能位置编码 FUNLOC_CODE" v-model:value="queryParam.funlocCode" allow-clear />
            </a-form-item>
          </a-col>
          <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
            <a-form-item name="funlocDesc">
              <template #label><span title="功能位置描述 FUNLOC_DESC">功能位置描述</span></template>
              <a-input placeholder="请输入功能位置描述 FUNLOC_DESC" v-model:value="queryParam.funlocDesc" allow-clear />
            </a-form-item>
          </a-col>
          <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
            <a-form-item name="indName">
              <template #label><span title="指标名称 IND_NAME">指标名称</span></template>
              <a-input placeholder="请输入指标名称 IND_NAME" v-model:value="queryParam.indName" allow-clear />
            </a-form-item>
          </a-col>
          <template v-if="toggleSearchStatus">
            <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
              <a-form-item name="indBusiArea">
                <template #label><span title="业务区域 IND_BUSI_AREA">业务区域</span></template>
                <a-input placeholder="请输入业务区域 IND_BUSI_AREA" v-model:value="queryParam.indBusiArea" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
              <a-form-item name="indStandCode">
                <template #label><span title="指标标准编码 IND_STAND_CODE">指标标准编码</span></template>
                <a-input placeholder="请输入指标标准编码 IND_STAND_CODE" v-model:value="queryParam.indStandCode" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
              <a-form-item name="indStandDesc">
                <template #label><span title="指标标准描述 IND_STAND_DESC">指标标准描述</span></template>
                <a-input placeholder="请输入指标标准描述 IND_STAND_DESC" v-model:value="queryParam.indStandDesc" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
              <a-form-item name="equipDesc">
                <template #label><span title="设备描述 EQUIP_DESC">设备描述</span></template>
                <a-input placeholder="请输入设备描述 EQUIP_DESC" v-model:value="queryParam.equipDesc" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
              <a-form-item name="acceEquipDesc">
                <template #label><span title="辅件设备描述 ACCE_EQUIP_DESC">辅件设备描述</span></template>
                <a-input placeholder="请输入辅件设备描述 ACCE_EQUIP_DESC" v-model:value="queryParam.acceEquipDesc" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
              <a-form-item name="partNumDesc">
                <template #label><span title="部件描述 PART_NUM_DESC">部件描述</span></template>
                <a-input placeholder="请输入部件描述 PART_NUM_DESC" v-model:value="queryParam.partNumDesc" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
              <a-form-item name="indInstCode">
                <template #label><span title="指标实例编码 IND_INST_CODE">指标实例编码</span></template>
                <a-input placeholder="请输入指标实例编码 IND_INST_CODE" v-model:value="queryParam.indInstCode" allow-clear />
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
    <DfMdmIndicatorModal ref="registerModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" name="DfMdmIndicator" setup>
  import { ref, reactive } from 'vue';
  import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';
  import { BasicTable } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { columns, superQuerySchema } from './DfMdmIndicator.data';
  import { enableFlagOptions } from './DfMdmRecipe.data';
  import { list } from './DfMdmIndicator.api';
  import DfMdmIndicatorModal from './components/DfMdmIndicatorModal.vue';

  const formRef = ref();
  const queryParam = reactive<any>({});
  const toggleSearchStatus = ref<boolean>(false);
  //注册table数据
  const { tableContext } = useListPage({
    tableProps: {
      title: 'MDM指标主数据',
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
   * 成功回调
   */
  function handleSuccess() {
    selectedRowKeys.value = [];
    reload();
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
