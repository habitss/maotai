<template>
  <BasicModal
    @register="registerModal"
    title="排班列表"
    v-bind="$attrs"
    destroyOnClose
    :maskClosable="false"
  >
  <!--定义表格-->
  <BasicTable @register="registerTable">
    <!-- 操作按钮 -->
    <template #tableTitle>
      <a-button type="primary" preIcon="ant-design:plus-outlined" @click="onAdd">新增</a-button>
    </template>

    <template #action="{ record }">
      <!-- 操作列 -->
      <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)"/>
    </template>

<!--    &lt;!&ndash; 查询区域插槽 &ndash;&gt;-->
<!--    &lt;!&ndash;  deptCode部门查询插槽    &ndash;&gt;-->
<!--    <template #form-deptCode="{ model, field }">-->
<!--      <kt-depart-select-->
<!--        v-model:value="model[field]"-->
<!--        select-type="depart"-->
<!--        :placeholder="t('language.selectDepartment')"-->
<!--        @change="handleDeptChange"-->
<!--      />-->
<!--    </template>-->
<!--    &lt;!&ndash;  lineCode产线查询插槽    &ndash;&gt;-->
<!--    <template #form-lineCode="{ model, field }">-->
<!--      <kt-depart-select-->
<!--        v-model:value="model[field]"-->
<!--        select-type="line"-->
<!--        :placeholder="t('language.selectProductLine')"-->
<!--        :parentKey="model.deptCode"-->
<!--      />-->
<!--    </template>-->
  </BasicTable>
  <!-- 填报modal -->
  <Modal @register="registerAddModal" @success="handleSuccess" />
    <RelationEditModal @register="registerEditModal" @success="handleSuccess" />
  </BasicModal>
</template>

<script lang="ts" name="action.shit-list" setup>
import { ActionItem, BasicTable, TableAction } from '/@/components/Table';
import {deleteOne, shiftList} from '../ShiftCalendar.api';
import { useListPage } from '/@/hooks/system/useListPage';
import Modal from './RelationAddModal.vue';
import {useModal, BasicModal,useModalInner} from "@/components/Modal";
import {useMessage} from "@/hooks/web/useMessage";
import {reactive} from "vue";
import {columns} from "../ShiftCalendar.data";
import { useI18n } from '/@/hooks/web/useI18n';
import RelationEditModal
  from "@/views/dataCalculation/mdm/shiftCalendar/components/RelationEditModal.vue";


const { t } = useI18n();
const emit = defineEmits(['success']);

//查询参数对象
const queryParam = reactive<any>({});
const props = defineProps({
  record: {
    type: Object,
    default: {}
  }
})

//注册modal
const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data: any) => {

  setModalProps({ confirmLoading: false, showCancelBtn: false, showOkBtn: false });
});

// 列表页面公共参数、方法
const { tableContext } = useListPage({
  designScope: 'basic-table-demo-ajax',
  tableProps: {
    api: shiftList,
    columns: columns,
    showIndexColumn:true,
    showTableSetting: false,
    actionColumn: {
      width: 220,
    },
    beforeFetch: (params) => {
      params.deptCode = props.record.deptCode;
      params.currentDate = props.record.currentDate;
      return Object.assign(params, queryParam);
    },
    // formConfig: {
    //   schemas: searchFormSchema,
    // },
  },
});
//BasicTable绑定注册
const [registerTable ,{ reload ,getForm}] = tableContext;

//新增/编辑modal
const [registerAddModal, { openModal }] = useModal();

//新增/编辑modal
const [registerEditModal, { openModal:openEditModal }] = useModal();


/**
 * 操作栏
 */
function getTableAction(record): ActionItem[] {
  return [
    {
      label: '编辑',
      onClick: handleEdit.bind(null, record),
    },
  ];
}

//消息提示
const { createMessage: msg } = useMessage();

/**
 * 下拉操作栏
 */
function getDropDownAction(record) {
  return [
    {
      label: '删除',
      popConfirm: {
        title: '是否删除？',
        confirm: handleDelete.bind(null, record.id),
      },
    },
  ]
}

/**
 * 新增事件
 */
function onAdd() {
  openModal(true, {'isAdd': true, 'isDetail': false, 'record': props.record})
}

/**
 * 编辑事件
 */
function handleEdit(record) {
  let users = record.groupUserIds;
  if(null != users && users.includes(",")){
    let participant = users.split(",");
    record.groupUserIds = participant;
  }
  openEditModal(true, {'isAdd': false, 'isDetail': false, 'record': record})
}



/**
 * 详情
 */
function handleDetail(record) {
  openModal(true, {'isAdd': false, 'isDetail': true, 'record': record})
}

/**
 * 删除
 */
async function handleDelete(record) {
  try {
    await deleteOne({id:record}, handleSuccess);
    await reload();
  } catch (error) {
    msg.error({ content: '操作失败', key: 'saving' });
  }
}

//部门改变事件
async function handleDeptChange(value){
  //清空产线
  await getForm().setFieldsValue({
    lineCode: '',
  });
}

/**
 * 成功回调
 */
function handleSuccess() {
  reload();
  emit('success');
}
</script>

<style scoped>

</style>
