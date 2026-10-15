<template>
  <!--查询区域-->
  <div class="jeecg-basic-table-form-container">
    <a-form @keyup.enter.native="searchQuery" :model="queryParam" :label-col="labelCol" :wrapper-col="wrapperCol" labelAlign="left">
      <a-row :gutter="24">
        <a-col :lg="8">
          <a-form-item :label="t('language.department')">
            <kt-depart-select
              v-model:value="queryParam.queryDept"
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
                  placeholder="请选择设备"
                  type="device"
              />
            </a-form-item>
          </a-col>
          <a-col :lg="8">
            <a-form-item label="品牌名称">
              <a-input placeholder="请输入品牌名称" v-model:value="queryParam.pointCode" />
            </a-form-item>
          </a-col>
          <a-col :lg="8">
            <a-form-item label="点位中文描述">
              <a-input placeholder="请输入点位中文描述" v-model:value="queryParam.pointName" />
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
  </div>

  <!--引用表格-->
  <BasicTable @register="registerTable" :rowSelection="rowSelection">
    <!--插槽:table标题-->
    <template #tableTitle>
      <a-button v-auth="''" type="primary" preIcon="ant-design:plus-outlined" @click="handleCreate"> {{ t('language.add') }}</a-button>
      <a-button v-auth="''" type="primary" preIcon="ant-design:export-outlined" @click="onExportXls"> {{ t('language.export') }}</a-button>
      <j-upload-button v-auth="''" type="primary" preIcon="ant-design:import-outlined" @click="onImportXls"> {{ t('language.import') }}</j-upload-button>

      <!-- <a-dropdown v-if="selectedRowKeys.length > 0">
        <template #overlay>
          <a-menu>
            <a-menu-item key="1" @click="batchHandleDelete">
              <Icon icon="ant-design:delete-outlined"></Icon>
              删除
            </a-menu-item>
          </a-menu>
        </template>
        <a-button
          >{{ t('language.batchOperation') }}
          <Icon icon="ant-design:down-outlined"></Icon>
        </a-button>
      </a-dropdown> -->
    </template>
    <!--操作栏-->
    <template #action="{ record }">
      <TableAction :actions="getTableAction(record)" />
    </template>
  </BasicTable>
  
  <!-- 表单区域 -->
  <brandModal ref="registerModal" @success="handleSuccess"></brandModal>
</template>

<script lang="ts" name="system-dict" setup>
  //ts语法
  import { ref,reactive,computed, unref } from 'vue';
  import { BasicTable, TableAction } from '@/components/Table';
  import { useDrawer } from '@/components/Drawer';
  import { useModal } from '@/components/Modal';
  import DictItemList from './components/DictItemList.vue';
  import DictModal from './components/DictModal.vue';
  import DictRecycleBinModal from './components/DictRecycleBinModal.vue';
  import { useMessage } from '@/hooks/web/useMessage';
  import { removeAuthCache, setAuthCache } from '@/utils/auth';
  import { columns, searchFormSchema } from './brand.data';
  import { list, deleteDict, batchDeleteDict, getExportUrl, getImportUrl, refreshCache, queryAllDictItems } from './brand.api';
  import { DB_DICT_DATA_KEY } from '@/enums/cacheEnum';
  import brandModal from './components/brandModal.vue'
  import KtDevice from '/@/components/KtDeviceType/KtDeviceType.vue';
  import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  const { createMessage } = useMessage();

  const registerModal = ref();
  const queryParam = ref<any>({});
  const toggleSearchStatus = ref<boolean>(false);
  // //字典model
  // const [registerModal, { openModal }] = useModal();
  //字典配置drawer
  // const [registerDrawer, { openDrawer }] = useDrawer();
  import { useListPage } from '/@/hooks/system/useListPage';
  const exportParams = reactive({
    departId: '1',
    deviceId: queryParam?.value?.deviceId,
  });

  // 列表页面公共参数、方法
  const { prefixCls, tableContext, onExportXls, onImportXls } = useListPage({
    designScope: 'dict-template',
    tableProps: {
      title: '品牌管理',
      api: list,
      columns: columns,
      canResize:false,
      useSearchForm: false,
      actionColumn: {
        width: 160,
        fixed: 'right',
      },
      beforeFetch: (params) => {
        return Object.assign(params, queryParam.value);
      },
    },
    //update-begin---author:wangshuai ---date:20220616  for：[issues/I5AMDD]导入/导出功能，操作后提示没有传递 export.url/import.url 参数------------
    exportConfig: {
      name: '品牌管理列表',
      url: getExportUrl,
    },
    importConfig: {
      url: getImportUrl,
    },
    //update-end---author:wangshuai ---date:20220616  for：[issues/I5AMDD]导入/导出功能，操作后提示没有传递 export.url/import.url 参数--------------
  });

  //注册table数据
  const [registerTable, { reload, updateTableDataRecord }, { rowSelection, selectedRowKeys }] = tableContext;
  const labelCol = reactive({
    xs: { span: 24 },
    sm: { span: 5 },
  });
  const wrapperCol = reactive({
    xs: { span: 24 },
    sm: { span: 16 },
  });

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


  /**
   * 新增事件
   */
  function handleCreate() {
    // openModal(true, {
    //   isUpdate: false,
    // });
    registerModal.value.disableSubmit = false;
    registerModal.value.add();
  }
  /**
   * 编辑事件
   */
  async function handleEdit(record: Recordable) {
    registerModal.value.disableSubmit = false;
    registerModal.value.edit(record);
  }
  /**
   * 详情
   */
  async function handleDetail(record) {
    openModal(true, {
      record,
      isUpdate: true,
    });
  }
  /**
   * 删除事件
   */
  async function handleDelete(record) {
    await deleteDict({ id: record.id }, reload);
  }
  /**
   * 批量删除事件
   */
  async function batchHandleDelete() {
    await batchDeleteDict({ ids: selectedRowKeys.value }, reload);
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
    //编辑/删除
    return [
      {
        label: t('language.edit'),
        onClick: handleEdit.bind(null, record),
        auth: '',
      },
      {
        label: t('language.delText'),
        popConfirm: {
          title: t('language.confirmDelete'),
          confirm: handleDelete.bind(null, record),
        },
        auth: '',
      },
    ];
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
</style>
