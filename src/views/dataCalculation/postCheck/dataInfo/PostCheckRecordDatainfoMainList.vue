<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--插槽:table标题-->
      <template #tableTitle>
        <div style="width: 100%;">
          <a-tabs v-model:activeKey="activeKey" @change="handeTabsChange">
            <a-tab-pane v-for="i in tabsData" :key="i.id" :tab="i.reportName"></a-tab-pane>
          </a-tabs>
        </div>
        <a-button v-if="activeKey && activeKey != 'all'" type="primary" v-auth="'post.check:mes3_post_check_record_datainfo_main:add'"
          @click="handleAdd" preIcon="ant-design:plus-outlined">新增</a-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                删除
              </a-menu-item>
            </a-menu>
          </template>
          <a-button v-auth="'post.check:mes3_post_check_record_datainfo_main:deletionBatch'">批量操作
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>

      <!--  deptCode部门查询插槽    -->
      <template #form-deptCode="{ model, field }">
        <kt-depart-select v-model:value="model[field]" select-type="depart" placeholder="请选择部门"
          @change="handleDeptChange" />
      </template>

      <!--  lineCode产线查询插槽    -->
      <template #form-lineCode="{ model, field }">
        <kt-depart-select v-model:value="model[field]" select-type="line" placeholder="请选择产线"
          :parentKey="model.deptCode" />
      </template>
    </BasicTable>
    <!-- 表单区域 -->
    <PostCheckRecordDatainfoMainModal @register="registerModal" @success="handleSuccess">
    </PostCheckRecordDatainfoMainModal>
  </div>
</template>

<script lang="ts" name="post.check-postCheckRecordDatainfoMain" setup>
import { onMounted, ref } from 'vue';
import { BasicTable, TableAction } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useListPage } from '/@/hooks/system/useListPage'
import { columns, searchFormSchema } from './PostCheckRecordDatainfoMain.data';
import { list, deleteOne, batchDelete } from './PostCheckRecordDatainfoMain.api';
import { list as configMainList } from '/@/views/dataCalculation/postCheck/postCheckRecordConfig/PostCheckRecordConfigMain.api';
import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
import PostCheckRecordDatainfoMainModal from './components/PostCheckRecordDatainfoMainModal.vue'
import { useRoute } from 'vue-router';
//获取路由
const route = useRoute();
//获取路由中的code参数
const reportType = route.params.code;
//tabs页签选中值
const activeKey = ref('');
//tabs页签数据
const tabsData = ref([] as any);
//注册model
const [registerModal, { openModal }] = useModal();
//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    title: '岗位记录填报主表',
    api: list,
    columns,
    showTableSetting: false,
    immediate: false,
    defSort:{
      column: 'fillingDate',
      order: 'desc'
    },
    formConfig: {
      schemas: searchFormSchema,
      autoSubmitOnEnter: true,
      showAdvancedButton: true,
      autoAdvancedCol: 8,
      fieldMapToNumber: [
      ],
      fieldMapToTime: [
      ],
      submitFunc: searchSubmit
    },
    actionColumn: {
      width: 120,
      fixed: 'right'
    },
    beforeFetch: async (params) => {
      params.configMainId = activeKey.value === 'all' ? tabsData.value.map(item => item.id).join(',') : activeKey.value;
      params.orderNo = params.orderNo ? `*${params.orderNo}*` : null;
    },
  },
})

const [registerTable, { reload, getForm, setTableData, setColumns }, { rowSelection, selectedRowKeys }] = tableContext;

/**
 * 新增事件
 */
function handleAdd() {
  //选中的tabs页签数据
  const findObj = tabsData.value.find(item => item.id === activeKey.value);
  openModal(true, {
    isUpdate: false,
    reportType,
    showFooter: true,
    configObj: findObj,
  });
}
/**
 * 编辑事件
 */
function handleEdit(record: Recordable) {
  openModal(true, {
    record,
    reportType,
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
      onClick: handleEdit.bind(null, record),
      auth: 'post.check:mes3_post_check_record_datainfo_main:edit'
    }
  ]
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
      },
      auth: 'post.check:mes3_post_check_record_datainfo_main:delete'
    }
  ]
}

//部门改变事件
async function handleDeptChange(value) {
  //清空产线
  await getForm().setFieldsValue({
    lineCode: '',
  });
}

//重写查询按钮提交事件
async function searchSubmit() {
  const searchParam = getForm().getFieldsValue();
  searchParam.configType = reportType;
  await getTabsData(searchParam);
  if (activeKey.value) {
    reload();
  } else {
    setTableData([]);
  }
}

//获取tabs页签数据
async function getTabsData(params) {
  //获取tabs数据
  let { records } = await configMainList({
    pageSize: 999,
    reportType: params.configType,
    reportName: params.configName ? `*${params.configName}*` : null,
    deptCode: params.deptCode,
    lineCode: params.lineCode,
    ifEffective: 1,
    column: 'sort',
    order: 'asc',
  });
  tabsData.value = [];
  //处理默认选中数据
  if (records.length !== 0) {
    //如果有多个，则添加一个全部的选项
    if(records.length > 1){
      records = [{id: 'all', reportName: '全部'}, ...records];
    }
    tabsData.value = records;
    const activeObject = records.find(item => item.id === activeKey.value);
    if (!activeObject) {
      activeKey.value = records[0].id;
    }
  } else {
    activeKey.value = '';
  }
}

//tabs页签切换事件
function handeTabsChange(values) {
  reload();
}

onMounted(async () => {
  //根据reportType动态修改列名和查询表单名称
  let configName = '检查清单名称';
  if(reportType == '1'){
    configName = '岗位记录名称';
  }
  //修改列名称
  for(const item of columns){
    if(item.dataIndex === 'configMainId_dictText'){
      item.title = configName;
    }
  }
  setColumns(columns);
  //修改查询表单名称
  getForm().updateSchema([
    {
      label: configName,
      field: 'configName',
      component: 'Input',
    },
  ]);
  searchSubmit();
})
</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}

:deep(.jeecg-basic-table-header__toolbar) {
  display: none;
}

:deep(.jeecg-basic-table-header__tableTitle) {
  width: 100%;
}
</style>