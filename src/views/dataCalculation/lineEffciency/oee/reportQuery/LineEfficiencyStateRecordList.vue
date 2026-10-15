<template>
  <div class="p-2">
    <!--查询区域-->
    <div class="jeecg-basic-table-form-container">
      <a-form ref="formRef" @keyup.enter.native="searchQuery" :model="queryParam" :label-col="labelCol" :wrapper-col="wrapperCol">
        <a-row :gutter="20">
          <a-col :lg="10">
              <a-form-item name="startTime_range">
                <template #label><span title="开始时间">开始时间</span></template>
                <a-range-picker
                  v-model:value="queryParam.startTime_range"
                  :show-time="{ defaultValue: [dayjs('00:00:00', 'HH:mm:ss'), dayjs('23:59:59', 'HH:mm:ss')] }" format="YYYY-MM-DD HH:mm:ss"
                  :placeholder="['开始时间起', '开始时间止']" 
                  allow-clear
                  @change="onChangeStartTime"
                />
              </a-form-item>
          </a-col>
          <a-col :lg="10">
              <a-form-item name="endTimeActual_range">
                <template #label><span title="实际结束时间">实际结束时间</span></template>
                <a-range-picker
                  v-model:value="queryParam.endTimeActual_range"
                  :show-time="{ defaultValue: [dayjs('00:00:00', 'HH:mm:ss'), dayjs('23:59:59', 'HH:mm:ss')] }" format="YYYY-MM-DD HH:mm:ss"
                  :placeholder="['实际结束时间起', '实际结束时间止']" 
                  allow-clear
                  @change="onChangeEndTimeActual"
                />
              </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="24">
          <a-col :lg="6">
              <a-form-item name="stateCode">
                <template #label><span title="运行状态">运行状态</span></template>
                <a-select
                  v-model:value="queryParam.stateCode"
                  allow-clear
                  style="width: 200px;"
                  placeholder="请选择"
                >
                  <a-select-option value="1">运行</a-select-option>
                  <a-select-option value="0">未运行</a-select-option>
                </a-select>
              </a-form-item>
          </a-col>
          <a-col :lg="6">
            <a-form-item name="lineName">
              <template #label><span title="产线名称">产线名称</span></template>
              <a-input placeholder="请输入产线名称" v-model:value="queryParam.lineName" allow-clear style="width: 200px;" ></a-input>
            </a-form-item>
          </a-col>
          <a-col :lg="6">
            <a-form-item name="sbName">
              <template #label><span title="设备名称">设备名称</span></template>
              <a-input placeholder="请输入设备名称" v-model:value="queryParam.sbName" allow-clear style="width: 150px;" ></a-input>
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <span style="float: left; overflow: hidden" class="table-page-search-submitButtons">
              <a-col :lg="6">
                <a-button type="primary" preIcon="ant-design:search-outlined" @click="searchQuery">查询</a-button>
              </a-col>
            </span>
          </a-col>
        </a-row>
      </a-form>
    </div>
    <!--引用表格-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button type="primary"  @click="handleAdd" preIcon="ant-design:plus-outlined"> 新增</a-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                删除
              </a-menu-item>
            </a-menu>
          </template>
          <a-button>批量操作
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)"/>
      </template>
    </BasicTable>
    <!-- 表单区域 -->
    <LineEfficiencyStateRecordModal ref="registerModal" @success="handleSuccess"></LineEfficiencyStateRecordModal>
  </div>
</template>

<script lang="ts" name="line.efficiency-lineEfficiencyStateRecord" setup>
  import { ref, reactive } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { columns } from './LineEfficiencyStateRecord.data';
  import { list, deleteOne, batchDelete, getImportUrl, getExportUrl } from './LineEfficiencyStateRecord.api';
  import LineEfficiencyStateRecordModal from './components/LineEfficiencyStateRecordModal.vue'
  import dayjs from 'dayjs';

  const formRef = ref();
  const queryParam = reactive<any>({
    //默认为运行状态
    stateCode: '1'
  });

  const registerModal = ref();
  //注册table数据
  const {tableContext} = useListPage({
    tableProps: {
      title: '线效数据查询',
      api: list,
      columns,
      canResize:true,
      useSearchForm: false,
      beforeFetch: async (params) => {
        //只查询oee
        params.type = 0;
        params.column = "endTimeActual";
        params.order = "desc";
        return Object.assign(params, queryParam);
      },
      pagination: {
        pageSizeOptions: ['50', '100', '300'],
        pageSize: 50
      },
    },
    exportConfig: {
      name: "oee查询",
      url: getExportUrl,
      params: queryParam,
    },
	  importConfig: {
	    url: getImportUrl,
	    success: handleSuccess
	  },
  });
  const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;
  const labelCol = reactive({
    style: { width: '150px' } 
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
    (selectedRowKeys.value = []) && reload();
  }
   
  /**
   * 操作栏
   */
  function getTableAction(record) {
    return [
      {
        label: '编辑',
        onClick: handleEdit.bind(null, record)
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
      }, {
        label: '删除',
        popConfirm: {
          title: '是否确认删除',
          confirm: handleDelete.bind(null, record),
          placement: 'topLeft',
        }
      }
    ]
  }

  /**
   * 查询
   */
  function searchQuery() {
    reload();
  }

  /**
   * 开始时间查询范围选择事件
   * @param value 
   * @param dateString 
   */
  const onChangeStartTime = (value,dateString) => {
    queryParam.startTime_begin = dateString[0];
    queryParam.startTime_end = dateString[1];
  };

  /**
   * 实际结束时间查询范围选择事件
   * @param value 
   * @param dateString 
   */
  const onChangeEndTimeActual = (value,dateString) => {
    queryParam.endTimeActual_begin = dateString[0];
    queryParam.endTimeActual_end = dateString[1];
  };

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
