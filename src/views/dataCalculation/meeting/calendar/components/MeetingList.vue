<template>
  <BasicModal
    @register="registerModal"
    title="会议列表"
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
  </BasicTable>
    <!-- 填报modal -->
    <Modal @register="registerAddModal" @success="handleSuccess" />
    <MeetingCheckModal @register="registerCheckModal" @success="handleSuccess" />
    <MeetingModal @register="registerViewModal" @success="handleSuccess" />
    <MeetingRecordModal @register="registerRecordModal"  @success="handleSuccess" />
    <MeetingCancelModal @register="registerCancelModal" @success="handleSuccess" />
  </BasicModal>
</template>

<script lang="ts" name="action.meeting-list" setup>
import { ActionItem, BasicTable, TableAction } from '/@/components/Table';
import { columns } from '../Meeting.data';
import { deleteOne, list} from '../Meeting.api';
import { useListPage } from '/@/hooks/system/useListPage';
import Modal from './MeetingAddModal.vue';
import {useModal, BasicModal,useModalInner} from "@/components/Modal";
import {useMessage} from "@/hooks/web/useMessage";
import MeetingCancelModal
  from "@/views/dataCalculation/meeting/calendar/components/MeetingCancelModal.vue";
import MeetingCheckModal
  from "@/views/dataCalculation/meeting/calendar/components/MeetingCheckModal.vue";
import MeetingRecordModal
  from "@/views/dataCalculation/meeting/calendar/components/MeetingRecordModal.vue";
import MeetingModal from "@/views/dataCalculation/meeting/calendar/components/MeetingModal.vue";
import {reactive} from "vue";

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
    api: list,
    columns: columns,
    showIndexColumn:true,
    showTableSetting: false,
    actionColumn: {
      width: 220,
    },
    beforeFetch: (params) => {
      params.factoryCode = props.record.factoryCode;
      params.deptCode = props.record.deptCode;
      params.currentDate = props.record.currentDate;
      return Object.assign(params, queryParam);
    },
  },
});
//BasicTable绑定注册
const [registerTable ,{ reload }] = tableContext;

//新增/编辑modal
const [registerAddModal, { openModal }] = useModal();

//配置模版modal
const [registerRecordModal, { openModal: openRecordModal }] = useModal();

//配置模版modal
const [registerCheckModal, { openModal: openCheckModal }] = useModal();

//配置模版modal
const [registerViewModal, { openModal: openViewModal }] = useModal();

//配置模版modal
const [registerCancelModal, { openModal: openCancelModal }] = useModal();

/**
 * 操作栏
 */
function getTableAction(record): ActionItem[] {
  return [
    {
      label: '签到',
      onClick: handleCheck.bind(null, record),
    },
    {
      label: '会议',
      onClick: handleView.bind(null, record),
    },
    {
      label: '编辑',
      onClick: handleEdit.bind(null, record),
    },
    {
      label: '评估',
      onClick: handleRecord.bind(null, record),
    },

  ];
}

//消息提示
const { createMessage: msg } = useMessage();

/**
 * 下拉操作栏
 */
function getDropDownAction(record) {
  if(record.active == 1){
    return [
      {
        label: '取消',
        popConfirm: {
          title: '是否取消？',
          confirm: handleCancel.bind(null, record.id),
        },
      },
      {
        label: '详情',
        onClick: handleDetail.bind(null, record),
      },
      {
        label: '删除',
        popConfirm: {
          title: '是否删除？',
          confirm: handleDelete.bind(null, record.id),
        },
      },
    ]
  } else {
    return [
      {
        label: '详情',
        onClick: handleDetail.bind(null, record),
      },
      {
        label: '删除',
        popConfirm: {
          title: '是否删除？',
          confirm: handleDelete.bind(null, record.id),
        },
      },
    ]
  }

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
  openModal(true, {'isAdd': false, 'isDetail': false, 'record': record})
}


/**
 * 编辑事件
 */
function handleCheck(record) {
  openCheckModal(true, {'isAdd': false, 'isDetail': false, 'record': record})
}

/**
 * 编辑事件
 */
function handleView(record) {
  openViewModal(true, {'isAdd': false, 'isDetail': false, 'record': record})
}

/**
 * 编辑事件
 */
function handleRecord(record) {
  openRecordModal(true, {'isAdd': false, 'isDetail': false, 'record': record})
}

/**
 * 编辑事件
 */
async function handleCancel(record) {
  openCancelModal(true, {'isAdd': false, 'isDetail': false, 'record': record})
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

function fetchData(){
  reload();

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
