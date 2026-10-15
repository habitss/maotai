<template>
  <a-spin :spinning="confirmLoading" class="span-box">
   <div class="p-2">
     <!--查询区域-->
     <div class="jeecg-basic-table-form-container">
      <a-form ref="formRef" @keyup.enter.native="searchQuery" :model="queryParam" :label-col="labelCol" :wrapper-col="wrapperCol">
        <a-row :gutter="24">
          <!--年份-->
          <!-- <a-col :lg="6">
            <a-form-item label="年份">
              <a-date-picker v-model:value="queryParam.year" picker="year" valueFormat="YYYY" format="YYYY" @change="classification"/>
            </a-form-item>
          </a-col> -->
          <!--KPI分类-->
          <a-col :lg="6">
            <a-form-item label="KPI分类">
              <a-tree-select
                v-model:value="queryParam.id"
                show-search
                style="width: 100%"
                :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
                placeholder="请选择KPI分类"
                allow-clear
                :tree-data="treeData"
                tree-node-filter-prop="kpiType"
                :fieldNames= "{children:'children', label:'kpiType', value: 'id' }"
              ></a-tree-select>
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
   <BasicTable @register="registerTable" :rowSelection="rowSelection">
     <!--插槽:table标题-->
      <template #tableTitle>
          <a-button type="primary" v-auth="'kpi:mes3_kpi_type:add'" @click="handleAdd" preIcon="ant-design:plus-outlined"> 新增</a-button>
          <a-button  type="primary" v-auth="'kpi:mes3_kpi_type:exportXls'" preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
          <j-upload-button type="primary" v-auth="'kpi:mes3_kpi_type:importExcel'" preIcon="ant-design:import-outlined" @click="onImportXls">导入</j-upload-button>
          <a-dropdown v-if="selectedRowKeys.length > 0">
              <template #overlay>
                <a-menu>
                  <a-menu-item key="1" @click="batchHandleDelete">
                    <Icon icon="ant-design:delete-outlined"></Icon>
                    删除
                  </a-menu-item>
                </a-menu>
              </template>
              <a-button v-auth="'kpi:mes3_kpi_type:deletionBatch'">批量操作
                <Icon icon="mdi:chevron-down"></Icon>
              </a-button>
        </a-dropdown>
      </template>
       <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)"/>
      </template>
      <!--字段回显插槽-->
      <template v-slot:bodyCell="{ column, record, index, text }">
      </template>
    </BasicTable>
    <!-- 表单区域 -->
    <KpiTypeModal @register="registerModal" @success="handleSuccess"></KpiTypeModal>
  </div>
</a-spin>
</template>

<script lang="ts" name="kpi-kpiType" setup>
  import {ref, reactive, onMounted} from 'vue';
  import {BasicTable, useTable, TableAction} from '/@/components/Table';
  import {useModal} from '/@/components/Modal';
  import { useListPage } from '/@/hooks/system/useListPage'
  import KpiTypeModal from './components/KpiTypeModal.vue'
  import {columns} from './KpiType.data';
  import {list, deleteOne, batchDelete, getImportUrl,getExportUrl} from './KpiType.api';
  import { useUserStore } from '/@/store/modules/user';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();
  const queryParam = reactive<any>({});
  const userStore = useUserStore();
  const formRef = ref();
  const treeData = ref([]);
  const confirmLoading = ref<boolean>(false);
  //注册model
  const [registerModal, {openModal}] = useModal();
  //注册table数据
  const { prefixCls,tableContext,onExportXls,onImportXls } = useListPage({
      tableProps:{
           title: 'KPI分类',
           api: list,
           columns,
           canResize:false,
           isTreeTable: true,
           actionColumn: {
               width: 240,
               fixed:'right'
            },
            beforeFetch: (params) => {
              return Object.assign(params, queryParam);
            },
      },
      exportConfig: {
        name:"KPI分类",
        url: getExportUrl,
        params: queryParam,
      },
      importConfig: {
        url: getImportUrl,
        success: handleSuccess
      },
  })

  const [registerTable, {reload},{ rowSelection, selectedRowKeys }] = tableContext
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
     openModal(true, {
       isUpdate: false,
       showFooter: true,
     });
  }
  /**
  * 编辑事件
  */
  function handleEdit(record: Recordable) {
     openModal(true, {
       record,
       isUpdate: true,
       showFooter: true,
     });
   }
   /**
    * 详情
   */
  function handleDetail(record: Recordable) {
     openModal(true, {
       record,
       isUpdate: true,
       showFooter: false,
     });
   }
  /**
  * 删除事件
  */
  async function handleDelete(record) {
     await deleteOne({id: record.id}, handleSuccess);
   }
  /**
  * 批量删除事件
  */
  async function batchHandleDelete() {
     await batchDelete({ids: selectedRowKeys.value}, handleSuccess);
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
  function getTableAction(record){
       return [
         {
           label: '编辑',
           onClick: handleEdit.bind(null, record),
           auth: 'kpi:mes3_kpi_type:edit'
         },
         {
          label: '添加下级',
          onClick: handleAddSub.bind(null, { parentId: record.id }),
         }
       ]
  }

  /**
   * 添加下级
   */
  function handleAddSub(record) {
    openModal(true, {
      "parentId": record.parentId,
      isUpdate: false,
      showFooter: true,
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
    selectedRowKeys.value = [];
    queryParam.id = ""
    //刷新数据
    reload();
  }

  onMounted(() => {
    //初始化年
    // 创建一个 Date 对象，默认表示当前日期和时间
    // const currentDate = new Date();
    // // 使用 getFullYear() 方法获取四位数的年份
    // const currentYear = currentDate.getFullYear();
    // queryParam.year = currentYear+"";
    //初始化分类
    classification();
  });
  //分类
  async function classification(){
    treeData.value = [];
    let res = await list({});
    if(res && res.length > 0){
      treeData.value.push(...res);
    }
  }

  /**
    * 下拉操作栏
    */
  function getDropDownAction(record){
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
           },
           auth: 'kpi:mes3_kpi_type:delete'
         }
       ]
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