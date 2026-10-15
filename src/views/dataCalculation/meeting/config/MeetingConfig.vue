<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable">
      <!--插槽:table标题-->
      <!-- 操作按钮 -->
      <template #tableTitle>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="onAdd">新增</a-button>
      </template>
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)"/>
      </template>
    </BasicTable>
    <ConfigAddModal @register="registerAddModal" @success="handleSuccess" />
    <MeetingTemplate @register="registerTemplateModal" :width="1500" />
  </div>
</template>

<script lang="ts" setup name="dataCalculation-meeting-config-list">
import { BasicTable, TableAction, ActionItem } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage'
import { useModal } from '/@/components/Modal';
import { useMessage } from '/@/hooks/web/useMessage';
import {columns, searchFormSchema} from "./MeetingConfig.data";
import { useI18n } from '/@/hooks/web/useI18n';
import ConfigAddModal from './components/ConfigAddModal.vue'
import {getList,deleteOne} from "@/views/dataCalculation/meeting/config/MeetingConfig.api";
import MeetingTemplate from "@/views/dataCalculation/meeting/template/MeetingTemplate.vue";
import {addFactoryDept} from "@/utils/common/compUtils";
import {useUserStore} from "@/store/modules/user";

const { t } = useI18n();
const { createMessage: msg } = useMessage();
const userInfo = useUserStore();
//注册table数据
const { tableContext , onExportXls, onImportXls } = useListPage({
  tableProps: {
    title: '会议类型配置',
    columns,
    api: getList,
    showActionColumn: true,
    actionColumn: {
      width: 130,
    },
    formConfig: {
      schemas: searchFormSchema,
      showAdvancedButton: false,
    },
    beforeFetch: (params) => {
      return addFactoryDept(params);
    },
  },

})
const [registerTable, { reload , setTableData, getForm }] = tableContext;

//新增/编辑modal
const [registerAddModal, { openModal ,setModalProps}] = useModal();

//配置模版modal
const [registerTemplateModal, { openModal: openTemplateModal }] = useModal();

/**
 * 操作栏
 */
function getTableAction(record): ActionItem[] {
  return [
    {
      label: '会议模版',
      onClick: handleTemplate.bind(null, record),
    },
  ];
}

/**
 * 下拉操作栏
 */
function getDropDownAction(record) {
  return [
    {
      label: '编辑',
      onClick: handleEdit.bind(null, record),
    },
    {
      label: '删除',
      popConfirm: {
        title: '是否删除？',
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

function handleTemplate(record){
  openTemplateModal(true, {'record': record})
}

/**
 * 编辑事件
 */
function handleEdit(record) {
  let users = record.participant;
  let presenters = record.presenter;
  if(null != users && users.includes(',')){
    let participant = users.split(',');
    record.participant = participant;
  }
  if(null != presenters && presenters.includes(',')){
    let presenter = presenters.split(',');
    record.presenter = presenter;
  }
  openModal(true, {'isAdd': false, 'isDetail': false, 'record': record})
}

/**
 * 详情
 */
function handleDetail(record) {
  openModal(true, {'isAdd': false, 'isDetail': true, 'record': record})
}

/**
 * 删除事件
 */
async function handleDelete(record) {
  await deleteOne({ id: record.id }, handleSuccess);
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
    workshop: '',
  });
}

async function handleLineChange(value) {
  await getForm().updateSchema([
    {
      field: 'lineCode',
      component: 'JDictSelectTag',
      componentProps: {
        dictCode: "mes3_period_verification_device, apparatus_name, id, del_flag = 0 and line_code='" + value + "'",
      },
    },
  ]);
}

</script>

<style scoped>

</style>
