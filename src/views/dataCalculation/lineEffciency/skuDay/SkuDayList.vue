<template>
  <div class="p-2">
    <!--查询区域-->
    <div class="jeecg-basic-table-form-container">
      <a-form ref="formRef" @keyup.enter.native="searchQuery" :model="queryParam" :label-col="labelCol" :wrapper-col="wrapperCol">
        <a-row :gutter="24">
          <!--产线-->
          <a-col :lg="6">
            <a-form-item label="产线">
              <!-- <a-input placeholder="请输入产线" v-model:value="queryParam.lineName" allowClear /> -->
              <kt-dict
                v-model:value="queryParam.lineName"
                placeholder="请选择产线"
                dict="OPI_SPLIT_LINE"
              />
            </a-form-item>
          </a-col>
          <!--日期-->
          <a-col :lg="6">
            <a-form-item label="日期">
              <a-date-picker v-model:value="queryParam.dateDay" picker="date" valueFormat="YYYY-MM-DD" format="YYYY-MM-DD"/>
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <span style="float: left; overflow: hidden; margin-left: -12px" class="table-page-search-submitButtons">
              <a-col :lg="6">
                <a-button type="primary" preIcon="ant-design:search-outlined" @click="searchQuery">{{ t('language.search') }}</a-button>
                <a-button type="primary" preIcon="ant-design:reload-outlined" @click="searchReset" style="margin-left: 8px">{{
                  t('language.reset')
                }}</a-button>
              </a-col>
            </span>
          </a-col>
        </a-row>
      </a-form>
    </div>
    <!--引用表格-->
    <BasicTable @register="registerTable" @edit-end="editEnd">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button type="primary" v-auth="'line:mes3_line_efficiency_sku_day:add'"  @click="handleAdd" preIcon="ant-design:plus-outlined">新增</a-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                删除
              </a-menu-item>
            </a-menu>
          </template>
          <a-button v-auth="'line:mes3_line_efficiency_sku_day:deleteBatch'">批量操作
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)"/>
      </template>
      <template v-slot:bodyCell="{ column, record, index, text }">
      </template>
    </BasicTable>
    <!-- 表单区域 -->
    <!-- <SkuDayModal ref="registerModal" @success="handleSuccess"></SkuDayModal> -->
     <!--数据拆分-->
    <DataSplitModal  ref="dataSplitModal" @success="handleSuccess"></DataSplitModal>
  </div>
</template>

<script lang="ts" name="line-lineEfficiencySkuDay" setup>
  import { ref, reactive } from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { columns } from './SkuDay.data';
  import { list, deleteOne, batchDelete, getImportUrl, getExportUrl,speedEdit,queryById } from './SkuDay.api';
  import SkuDayModal from './components/SkuDayModal.vue'
  import { useUserStore } from '/@/store/modules/user';
  import DataSplitModal from './components/DataSplitModal.vue'
  import KtDict from '/@/components/KtDict/KtDict.vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  const formRef = ref();
  const queryParam = reactive<any>({});
  const toggleSearchStatus = ref<boolean>(false);
  const registerModal = ref();
  const dataSplitModal = ref();
  const userStore = useUserStore();
  const { createMessage } = useMessage();
  //注册table数据
  const { prefixCls, tableContext } = useListPage({
    tableProps: {
      title: '产线-SKU天记录拆分',
      api: list,
      columns,
      canResize:false,
      useSearchForm: false,
      actionColumn: {
        width: 120,
        fixed: 'right',
      },
      beforeFetch: async (params) => {
        return Object.assign(params, queryParam);
      },
    },
    exportConfig: {
      name: "产线-SKU天记录拆分",
      url: getExportUrl,
      params: queryParam,
    },
	  importConfig: {
	    url: getImportUrl,
	    success: handleSuccess
	  },
  });
  const [registerTable, { reload, collapseAll, updateTableDataRecord, findTableDataRecord, getDataSource }, { rowSelection, selectedRowKeys }] = tableContext;
  const labelCol = reactive({
    xs:24,
    sm:4,
    xl:6,
    xxl:4
  });
  const wrapperCol = reactive({
    xs: 24,
    sm: 20,
  });

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
   * 批量删除事件
   */
  async function batchHandleDelete() {
    await batchDelete({ ids: selectedRowKeys.value }, handleSuccess);
  }
   
  /**
   * 成功回调
   */
  function handleSuccess() {
    (selectedRowKeys.value = []) && reload();
  }
   
  /**
   * 操作栏
   */
  function getTableAction(record) {
    return [
      {
        label: '编辑',
        onClick: handleEdit.bind(null, record),
        auth: 'line:mes3_line_efficiency_sku_day:edit'
      },{
        label: '拆分',
        onClick: dataSplit.bind(null, record),
      }
    ];
  }
   
  /**
   * 下拉操作栏
   */
  function getDropDownAction(record) {
    return [
    ]
  }

  /**
   * 数据拆分
   */
  async function dataSplit(record: Recordable) {
    //判断是否被拆分
    await queryById({id: record.id})
      .then((res) => {
        if (res.code === 200) {
          dataSplitModal.value.disableSubmit = false;
          dataSplitModal.value.edit(record);
        } else {
          createMessage.warning("当前数据已完成拆分，无需重复操作");
          reload();
        }
      })
      .finally(() => {
        //confirmLoading.value = false;
      });
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
    queryParam.lineName = "";
    queryParam.date = "";
    selectedRowKeys.value = [];

    //刷新数据
    reload();
  }
  
  //编辑完成
  async function editEnd(val){
    let data = val.record;
    await speedEdit(data)
      .then((res) => {
        if (res.success) {
          createMessage.success(res.message);
          searchQuery();
        } else {
          createMessage.warning(res.message);
        }
      })
      .finally(() => {
        //confirmLoading.value = false;
      });
    console.log(data.record)
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
    .query-group-cust{
      min-width: 100px !important;
    }
    .query-group-split-cust{
      width: 30px;
      display: inline-block;
      text-align: center
    }
    .ant-form-item:not(.ant-form-item-with-help){
      margin-bottom: 16px;
      height: 32px;
    }
    :deep(.ant-picker),:deep(.ant-input-number){
      width: 100%;
    }
  }
</style>
