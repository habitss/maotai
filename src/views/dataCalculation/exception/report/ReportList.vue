<template>
  <BasicTable @register="registerTable" :rowSelection="rowSelection">
    <!-- 操作按钮 -->
    <template #tableTitle>
      <a-button type="primary" preIcon="ant-design:plus-outlined" @click="onAdd">新增</a-button>
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
          <Icon icon="ant-design:down-outlined"></Icon>
        </a-button>
      </a-dropdown>
    </template>
    <!-- 查询区域插槽 -->
    <!--  deptCode部门查询插槽    -->
    <template #form-deptCode="{ model, field }">
      <kt-depart-select
        v-model:value="model[field]"
        select-type="depart"
        :placeholder="t('language.selectDepartment')"
        @change="handleDeptChange"
      />
    </template>
    <!--  lineCode产线查询插槽    -->
    <template #form-lineCode="{ model, field }">
      <kt-depart-select
        v-model:value="model[field]"
        select-type="line"
        :placeholder="t('language.selectProductLine')"
        :parentKey="model.deptCode"
      />
    </template>
    <!--插槽:表格内容-->
    <template #bodyCell="{ column, record, index }">
      <div v-if="column.dataIndex == 'reportPath'" class="custom-div">
        <a-button type="link" @click="showDetail( column, record, index)">查看</a-button>
      </div>
    </template>
    <!-- 操作列插槽 -->
    <template #action="{ record }">
      <TableAction :actions="createActions(record)" :dropDownActions="getDropDownAction(record)" />
    </template>
  </BasicTable>
  <Modal @register="registerAddModal" @success="handleSuccess" />
</template>

<script lang="ts" name="dataCalculation-exception-report-list" setup>
import { reactive } from 'vue';
import { ActionItem, BasicTable, BasicColumn, TableAction, EditRecordRow } from '/@/components/Table';
import { getList, deleteData , batchDelete} from './Report.api';
import { columns, searchFormSchema } from './Report.data';
import { useListPage } from '/@/hooks/system/useListPage';
import { useMessage } from '/@/hooks/web/useMessage';
import { useModal } from '/@/components/Modal';
import Modal from './components/ReportAddModal.vue';
import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
import { useI18n } from '/@/hooks/web/useI18n';
import {factoryPath} from "@/views/dataCalculation/periodCheck/periodPlan/PeriodScheduleList.api";
import {encryptByBase64} from "@/utils/cipher";
import {useGlobSetting} from "@/hooks/setting";
import {useUserStore} from "@/store/modules/user";


const { t } = useI18n();
//查询参数对象
const queryParam = reactive<any>({});
const globSetting = useGlobSetting();
const userStore = useUserStore();

//消息提示
const { createMessage: msg } = useMessage();

//新增/编辑modal
const [registerAddModal, { openModal }] = useModal();

// 列表页面公共参数、方法
const { tableContext , onExportXls, onImportXls } = useListPage({
  tableProps: {
    title: '异常处理报告管理',
    api: getList,
    beforeFetch: (params) => {
      params.column = 'sort';
      params.order = 'asc';
      return Object.assign(params, queryParam);
    },
    columns: columns,
    expandRowByClick: true,
    showActionColumn: true,
    actionColumn: {
      width: 120,
      fixed: 'right',
    },
    rowSelection: {
      columnWidth: 20,
    },
    formConfig: {
      schemas: searchFormSchema,
    },
  },
});

// BasicTable绑定注册
const [registerTable, { reload, getForm },{ rowSelection, selectedRows, selectedRowKeys }] = tableContext;


/**
 * 创建操作列
 */
function createActions(record): ActionItem[] {
  return [
    {
      label: '编辑',
      onClick: handleEdit.bind(null, record),
    },
    // {
    //   label: '报告下载',
    //   onClick: handleOpenConfigModal.bind(null, record),
    // },
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
        title: '是否删除',
        confirm: handleDelete.bind(null, record),
      },
    },
  ]
}

/**
 * 新增事件
 */
function onAdd() {
  openModal(true, {'isAdd': true, 'isDetail': false})
}


/**
 * 编辑事件
 //  * templateRecord: templateRecord.value
 */
function handleEdit(record) {
  openModal(true, {'isAdd': false, 'isDetail': false, 'record': record})
}

/**
 * 详情
 */
function handleDetail(record) {
  openModal(true, {'isAdd': false, 'isDetail': true, 'record': record})
}

async function showDetail( column, record, index){
  // console.log(record)
  // let path = '/keytech-boot/mes3_common/static/' + record.reportFile;
  // window.open(path, '_blank');
  if(null != record.reportFile){
    let ip = await factoryPath({factoryCode: userStore?.userInfo?.orgCode})
    let path = ip + 'keytech-boot/mes3_common/static/' + record.reportFile;//要预览文件的访问地址
    console.log(path)
    if(null != record.reportFile && record.reportFile != ''){
      window.open(ip + 'preview/onlinePreview?url=' + encodeURIComponent(encryptByBase64(path)), '_blank');
    }
  } else {
    msg.warning("无异常报告！")
  }

}
// async function showDetail(column, record, index) {
//   let url = globSetting.domainUrl
//   console.log(url)
//   let path = url + '/mes3_common/static/' + record.reportFile;//要预览文件的访问地址
//   let ip = await factoryPath({factoryCode: userStore?.userInfo?.orgCode})
//   console.log(ip)
//   window.open(ip + 'onlinePreview?url=' + encodeURIComponent(encryptByBase64(path)));
//   // let path = '/keytech-boot/mes3_common/static/' + record;
//   // window.open(path, '_blank');
// }

/**
 * 删除
 */
async function handleDelete(record){
    //调用后台修改接口, 删除
  await deleteData(record.id, handleSuccess);
  msg.success({ content: '删除成功', key: 'saving' });
}

/**
 * 批量删除事件
 */
async function batchHandleDelete() {
  const ids = selectedRowKeys.value
  await batchDelete({ ids: ids }, handleSuccess);
}

/**
 * 成功回调
 */
function handleSuccess() {
  reload();
}

//部门改变事件
async function handleDeptChange(value){
  //清空产线
  await getForm().setFieldsValue({
    lineCode: '',
  });
}
</script>

<style scoped>

</style>
