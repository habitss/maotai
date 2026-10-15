<template>
  <div>
    <!--查询区域-->
    <div class="jeecg-basic-table-form-container">
      <a-form @keyup.enter.native="searchQuery" :model="queryParam" :label-col="labelCol" :wrapper-col="wrapperCol" labelAlign="left">
        <a-row :gutter="24">
          <!-- <a-col :lg="8">
            <a-form-item :label="t('language.workSection')">
              <kt-dict v-model:value="queryParam.orgTypeCode" :placeholder="t('language.selectWorkSection')" dict="ORG_TYPE" />
            </a-form-item>
          </a-col> -->

          <a-col :lg="8">
            <a-form-item :label="t('language.department')">
              <kt-depart-select
                v-model:value="queryParam.deptCode"
                select-type="depart"
                :placeholder="t('language.selectDepartment')"
              />
            </a-form-item>
          </a-col>

          <a-col :lg="8">
            <a-form-item :label="t('language.productLine')">
              <kt-depart-select v-model:value="queryParam.lineCode" :placeholder="t('language.selectProductLine')" select-type="line" />
            </a-form-item>
          </a-col>
          <template v-if="toggleSearchStatus">
            <a-col :lg="8">
              <a-form-item label="设备">
                <kt-device
                    ref="ktDeviceRef"
                    v-model:value="queryParam.deviceId"
                    :lineCode="queryParam.lineCode"
                    dict=""
                    :placeholder="请选择设备"
                    type="device"
                />
              </a-form-item>
            </a-col>
            <a-col :lg="8">
              <a-form-item label="步骤号">
                <a-input placeholder="请输入步骤号" v-model:value="queryParam.stepNum" />
              </a-form-item>
            </a-col>
            <a-col :lg="8">
              <a-form-item label="步骤名称">
                <a-input placeholder="请输入步骤名称" v-model:value="queryParam.stepName" />
              </a-form-item>
            </a-col>
          </template>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <span style="float: left; overflow: hidden; margin-left: -12px" class="table-page-search-submitButtons">
              <a-col :lg="6">
                <a-button type="primary" preIcon="ant-design:search-outlined" @click="searchQuery">{{ t('language.search') }}</a-button>
                <a-button type="primary" preIcon="ant-design:reload-outlined" @click="searchReset" style="margin-left: 8px">{{
                  t('language.reset')
                }}</a-button>
                <a @click="toggleSearchStatus = !toggleSearchStatus" style="margin-left: 8px">
                  {{ toggleSearchStatus ? t('language.collapse') : t('language.open') }}
                  <Icon :icon="toggleSearchStatus ? 'ant-design:up-outlined' : 'ant-design:down-outlined'" />
                </a>
              </a-col>
            </span>
          </a-col>
        </a-row>
      </a-form>
    <!--引用表格-->
   <BasicTable @register="registerTable" :rowSelection="rowSelection">
     <!--插槽:table标题-->
      <template #tableTitle>
          <a-button type="primary" @click="handleAdd" preIcon="ant-design:plus-outlined"> 新增</a-button>
          <a-button  type="primary" preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
          <j-upload-button  type="primary" preIcon="ant-design:import-outlined" @click="onImportXls">导入</j-upload-button>
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
      <!--字段回显插槽-->
      <template v-slot:bodyCell="{ column, record, index, text }">
      </template>
    </BasicTable>
    </div>
    <!-- 表单区域 -->
    <PiStepModal ref="registerModal" @success="handleSuccess"></PiStepModal>
  </div>
</template>

<script lang="ts" name="pi-piStep" setup>
  import {ref, reactive, onMounted, unref} from 'vue';
  import {BasicTable, useTable, TableAction} from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage'
  import {columns} from './PiStep.data';
  import {list, deleteOne, batchDelete, getImportUrl,getExportUrl} from './PiStep.api';
  import { downloadFile } from '/@/utils/common/renderUtils';
  import PiStepModal from './components/PiStepModal.vue'

  import JSelectDept from '/@/components/Form/src/jeecg/components/JSelectDept.vue';
  import KtDict from '/@/components/KtDict/KtDict.vue';
  import KtDevice from '/@/components/KtDeviceType/KtDeviceType.vue';
  import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
  import { getDepartId } from '/@/utils/ktTool';
  import { useUserStore } from '/@/store/modules/user';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  const queryParam = ref<any>({});
  const toggleSearchStatus = ref<boolean>(false);
  const registerModal = ref();
  const userStore = useUserStore();
  const exportParams = reactive({
    departId: '1',
    deviceId: queryParam?.value?.deviceId,
  });

  //注册table数据
  const { prefixCls,tableContext,onExportXls,onImportXls } = useListPage({
      tableProps:{
           title: '步骤管理',
           api: list,
           columns,
           canResize:false,
           useSearchForm: false,
           actionColumn: {
              width: 120,
              fixed: 'right',
           },
         
        beforeFetch: (params) => {
          return Object.assign(params, queryParam.value);
        },
      },
      exportConfig: {
        name:"步骤管理",
        url: getExportUrl,
      },
      importConfig: {
        url: getImportUrl,
        success: handleSuccess
      },
  })

  
 const [registerTable, { reload, collapseAll, updateTableDataRecord, findTableDataRecord, getDataSource }, { rowSelection, selectedRowKeys }] =
  tableContext;
  const labelCol = reactive({
    xs: { span: 24 },
    sm: { span: 5 },
  });
  const wrapperCol = reactive({
    xs: { span: 24 },
    sm: { span: 16 },
  });

  onMounted(() => {
    exportParams.departId = getDepartId(userStore?.getUserInfo?.orgCode);
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
      label: t('language.edit'),
      onClick: handleEdit.bind(null, record),
      auth: '',
    },
  ];
}

/**
 * 下拉操作栏
 */
function getDropDownAction(record) {
  return [
    {
      label: t('language.detail'),
      onClick: handleDetail.bind(null, record),
    },
    {
      label: t('language.delText'),
      popConfirm: {
        title: t('language.confirmDelete'),
        confirm: handleDelete.bind(null, record),
        placement: 'left',
      },
      auth: '',
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
  queryParam.value = {};
  selectedRowKeys.value = [];
  //刷新数据
  reload();
}


</script>

<style lang="less" scoped>
.jeecg-basic-table-form-container {
  .table-page-search-submitButtons {
    display: block;
    margin-bottom: 24px;
    white-space: nowrap;
  }
  .query-group-cust {
    width: calc(50% - 15px);
    min-width: 100px !important;
  }
  .query-group-split-cust {
    width: 30px;
    display: inline-block;
    text-align: center;
  }
}
//@import '../common.less';
</style>