<template>
  <div>
    <!--查询区域-->
    <div class="jeecg-basic-table-form-container">
      <a-form @keyup.enter.native="searchQuery" :model="queryParam" :label-col="labelCol" :wrapper-col="wrapperCol" labelAlign="right">
        <a-row :gutter="24">
          <!--部门-->
          <a-col :lg="6">
            <a-form-item :label="t('language.department')">
              <KtDepartTreeSelect 
                v-model:value="queryParam.orgCode"
                :allowClear="true" 
                :defaultExpandLevel="1" 
                :byUserOrg="true"
                :fieldNames=" { label:'title', value: 'orgCode' }">
              </KtDepartTreeSelect>
            </a-form-item>
          </a-col>
            <a-col :lg="6">
              <a-form-item label="报表名称">
                <a-input placeholder="请输入报表名称" v-model:value="queryParam.name" />
              </a-form-item>
            </a-col>
            <a-col :lg="6">
              <a-form-item label="表名">
                <a-input placeholder="请输入表名" v-model:value="queryParam.tableName" />
              </a-form-item>
            </a-col>
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
    </div>

    <!--配置字段-->
    <a-drawer
      v-if="hasReopen == true"
      v-model:visible="hasReopen"
      class="custom-class"
      root-class-name="root-class-name"
      :root-style="{ color: 'blue' }"
      title="配置字段"
      width="90%"
      placement="right"
    >
      <PiCustomizeTableFieldList @ok="updateList" :departObject="departObject"></PiCustomizeTableFieldList>
    </a-drawer>
    <!-- 表单区域 -->
    <PiCustomizeTableModal ref="registerModal" @success="handleSuccess"></PiCustomizeTableModal>
    <!-- pi岗位绑定抽屉组件 -->
    <PiPostBindDrawer @register="register" />
  </div>
</template>

<script lang="ts" name="pi-piCustomizeTable" setup>
  import { ref, reactive, provide,onMounted } from 'vue';
  import {BasicTable, TableAction} from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage'
  import {columns} from './PiCustomizeTable.data';
  import {list, deleteOne, batchDelete, getImportUrl,getExportUrl} from './PiCustomizeTable.api';
  import PiCustomizeTableModal from './components/PiCustomizeTableModal.vue'
  import PiCustomizeTableFieldList from './PiCustomizeTableFieldList.vue'
  import { getDepartId } from '/@/utils/ktTool';
  import { useUserStore } from '/@/store/modules/user';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useDrawer } from '/@/components/Drawer';
  import PiPostBindDrawer from './components/PiPostBindDrawer.vue';
  import { useRouter } from 'vue-router'
  import KtDepartTreeSelect from '/@/components/KtDepartSelect/KtDepartTree.vue';
  
  const router = useRouter()
  const { t } = useI18n();

  //注册pi岗位绑定抽屉
  const [register, { openDrawer }] = useDrawer();
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
      tableProps:{
           title: 'PI基础数据',
           api: list,
           columns,
           canResize:true,
           useSearchForm: false,
           actionColumn: {
              width: 250,
              fixed: 'right',
           },
         
        beforeFetch: (params) => {
          params.deptScope = userStore.getUserInfo.orgCode;
          const queryParams = { ...queryParam.value };
          if (queryParams.isDataHandle === undefined || queryParams.isDataHandle === null) {
            queryParams.isDataHandle = '1';
          }
          return Object.assign(params, queryParams);
        },
      },
      exportConfig: {
        name:"PI基础数据",
        url: getExportUrl,
      },
      importConfig: {
        url: getImportUrl,
        success: handleSuccess
      },
  })

  
 const [registerTable, { reload }, { rowSelection, selectedRowKeys }] =
  tableContext;
  const labelCol = reactive({
    xs: { span: 24 },
    sm: { span: 5 },
  });
  const wrapperCol = reactive({
    xs: { span: 24 },
    sm: { span: 24 },
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
   * pi岗位绑定
   */
  function handlePiPostBind(record){
    openDrawer(true,{
      record
    });
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
      auth: '',
    },
    {
      label: "查看报表",
      onClick: showTable.bind(null, record),
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
      label: 'PI岗位绑定',
      onClick: handlePiPostBind.bind(null, record),
    },
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
//查看报表
const showTable = (record) =>{
  router.push({ path: '/dataCalculation/pi/allocationTable/QueryTableList/'+record.id+"/1"})
}

function updateList() {
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