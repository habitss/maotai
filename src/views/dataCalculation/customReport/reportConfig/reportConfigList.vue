<template>
  <div>
    <!--查询区域-->
    <div class="jeecg-basic-table-form-container">
      <a-form @keyup.enter.native="searchQuery" :model="queryParam" :label-col="labelCol" :wrapper-col="wrapperCol"
        labelAlign="right">
        <a-row :gutter="24">
          <a-col :lg="8">
            <a-form-item :label="t('language.department')">
              <kt-depart-select v-model:value="queryParam.deptCode" :placeholder="t('language.selectDepartment')"
                select-type="depart" @change="handleDeptChange" />
            </a-form-item>
          </a-col>
          <a-col :lg="8">
            <a-form-item :label="t('language.productLine')">
              <kt-depart-select v-model:value="queryParam.lineCode" select-type="line"
                :placeholder="t('language.selectProductLine')" :parentKey="queryParam.deptCode" />
            </a-form-item>
          </a-col>
          <template v-if="toggleSearchStatus">
            <a-col :lg="8">
              <a-form-item label="报表名称">
                <a-input placeholder="请输入报表名称" v-model:value="queryParam.name" />
              </a-form-item>
            </a-col>
            <a-col :lg="8">
              <a-form-item label="表名">
                <a-input placeholder="请输入表名" v-model:value="queryParam.tableName" />
              </a-form-item>
            </a-col>
            <!-- <a-col :lg="8">
              <a-form-item :label="t('language.deviceCode')">
                <a-input :placeholder="t('language.enterDeviceCode')" v-model:value="queryParam.deviceCode" />
              </a-form-item>
            </a-col> -->
          </template>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <span style="float: left; overflow: hidden; margin-left: -12px" class="table-page-search-submitButtons">
              <a-col :lg="6">
                <a-button type="primary" preIcon="ant-design:search-outlined" @click="searchQuery">{{
                  t('language.search')
                  }}</a-button>
                <a-button type="primary" preIcon="ant-design:reload-outlined" @click="searchReset"
                  style="margin-left: 8px">{{
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
          <!-- <a-button  type="primary" preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
          <j-upload-button  type="primary" preIcon="ant-design:import-outlined" @click="onImportXls">导入</j-upload-button> -->
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
          <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
        </template>
        <!--字段回显插槽-->
        <template v-slot:bodyCell="{ column, record, index, text }">
        </template>
      </BasicTable>
    </div>

    <!--配置字段-->
    <a-drawer v-if="hasReopen == true" v-model:visible="hasReopen" class="custom-class"
      root-class-name="root-class-name" :root-style="{ color: 'blue' }" title="配置字段" width="70%" placement="right">
      <ReportConfigFieldList @ok="updateList" :departObject="departObject"></ReportConfigFieldList>
    </a-drawer>
    <!-- 表单区域 -->
    <ReportConfigModal ref="registerModal" @success="handleSuccess"></ReportConfigModal>
  </div>
</template>

<script lang="ts" name="pi-piCustomizeTable" setup>
import { ref, reactive, provide, onMounted, unref } from 'vue';
import { BasicTable, TableAction } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage'
import { columns } from './reportConfig.data';
import { list, deleteOne, batchDelete, getImportUrl, getExportUrl } from './reportConfig.api';
import ReportConfigModal from './components/reportConfigModal.vue'
import ReportConfigFieldList from './reportConfigFieldList.vue'
import KtDict from '/@/components/KtDict/KtDict.vue';
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
const hasReopen = ref<boolean>(false);
const departObject = ref<Object>({});
provide('departObject', departObject);

//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    title: '自定义报表配置',
    api: list,
    columns,
    canResize: false,
    useSearchForm: false,
    actionColumn: {
      width: 200,
      fixed: 'right',
    },

    beforeFetch: (params) => {
      return Object.assign(params, queryParam.value);
    },
  },
  exportConfig: {
    name: "自定义报表配置",
    url: getExportUrl,
  },
  importConfig: {
    url: getImportUrl,
    success: handleSuccess
  },
})


const [registerTable, { reload, getForm }, { rowSelection, selectedRowKeys }] =
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
    },
    {
      label: "配置字段",
      onClick: configField.bind(null, record),
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

//配置字段
const configField = (record) => {
  hasReopen.value = true;
  departObject.value = record;
}
function updateList() {
  //hasReopen.value = false;
}


//部门改变事件
async function handleDeptChange(value){
  //清空产线
  queryParam.value.lineCode = '';
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

/** 时间和数字输入框样式 */
:deep(.ant-input-number) {
  width: 100%;
}

:deep(.ant-picker) {
  width: 100%;
}

//下拉框默认文字颜色
:deep(.ant-select-selection-placeholder) {
  color: #BEBEBE !important;
}

//@import '../common.less';</style>