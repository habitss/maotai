<template>
    <!--定义表格-->
    <BasicTable @register="registerTable" @edit-change="onEditChange">
      <template #action="{ record, column }">
        <TableAction :actions="createActions(record, column)" />
      </template>
    </BasicTable>

</template>

<script lang="ts" name="action.meeting-record" setup>
import { BasicTable, useTable, TableAction, BasicColumn, ActionItem, EditRecordRow } from '/@/components/Table';
import { recordColumns } from '../Meeting.data';
import {recordList, saveOrUpdateRecord} from '../Meeting.api';
import { useListPage } from '/@/hooks/system/useListPage';
import {BasicModal, useModal, useModalInner} from "@/components/Modal";
import {useMessage} from "@/hooks/web/useMessage";
import {useI18n} from "@/hooks/web/useI18n";
import { cloneDeep } from 'lodash-es';
import {ref} from "vue";

const { t } = useI18n();
const props = defineProps({
  record: {
    type: Object,
    default: {}
  }
})

// 禁用
const disabledRef = ref(false);
const currentEditKeyRef = ref('');
const queryParams = ref();
const meetingDate = ref();


//注册modal
// const [registerModal, { setModalProps, closeModal} ] = useModalInner(async (data: any) => {
//   disabledRef.value = false;
//   queryParams.value = {lineCode:data.record.lineCode,meetingType:data.record.meetingType,recordType:2}
//   meetingDate.value = data.record
//   setModalProps({ confirmLoading: false, showCancelBtn: false, showOkBtn: false });
// });

// 列表页面公共参数、方法
const { tableContext ,reload} = useListPage({
  tableProps: {
    api: recordList,
    columns: recordColumns,
    showIndexColumn: true,
    showTableSetting: false,
    tableSetting: {fullScreen: false},
    pagination:false,
    actionColumn: {
      title: ' 操作',
      dataIndex: 'action',
      slots: {customRender: 'action'},
      width: 120,
    },
    beforeFetch: (params) => {
      params.column = 'sort';
      params.order = 'asc';
      params.meetingId = props.record.id;
      params.meetingType = props.record.meetingType
      params.recordType = 2
      return Object.assign(params, queryParams.value);
    },
    afterFetch:(record) => {
      let score = 0;
      record.forEach(function (value,index){
        if(null !=  value.score){
          score += value.score
        }
      })
      let mergedObj = {content:'小计',score:score};
      record.push(mergedObj);
    }
  }
});

//BasicTable绑定注册
const [registerTable] = tableContext;


//消息提示
const { createMessage: msg } = useMessage();

function handleEdit(record: EditRecordRow) {
  currentEditKeyRef.value = record.key;
  record.onEdit?.(true);
}

function handleCancel(record: EditRecordRow) {
  currentEditKeyRef.value = '';
  record.onEdit?.(false, false);
}

async function handleSave(record: EditRecordRow) {
  // 校验
  msg.loading({ content: '正在保存...', duration: 0, key: 'saving' });
  const valid = await record.onValid?.();
  if (valid) {
    try {
      const data = cloneDeep(record.editValueRefs);
      let recordData = {score:data.score,scoreReason:data.scoreReason,
        meetingId:props.record.id,deptCode:props.record.deptCode,lineCode:props.record.lineCode,
        recordTemplateId:props.record.recordTemplateId,recordType:props.record.recordType
      };
      //TODO 此处将数据提交给服务器保存
      await saveOrUpdateRecord(recordData);
      // 保存之后提交编辑状态
      const pass = await record.onEdit?.(false, true);
      if (pass) {
        currentEditKeyRef.value = '';
      }
      msg.success({ content: '数据已保存', key: 'saving' });
      reload();
    } catch (error) {
      msg.error({ content: '保存失败', key: 'saving' });
    }
  } else {
    msg.error({ content: '请填写正确的数据', key: 'saving' });
  }
}

function createActions(record: EditRecordRow, column: BasicColumn): ActionItem[] {
  if (!record.editable) {
    return [
      {
        label: '编辑',
        disabled: currentEditKeyRef.value ? currentEditKeyRef.value !== record.key : false,
        onClick: handleEdit.bind(null, record),
      },
    ];
  }
  return [
    {
      label: '保存',
      onClick: handleSave.bind(null, record, column),
    },
    {
      label: '取消',
      popConfirm: {
        title: '是否取消编辑',
        confirm: handleCancel.bind(null, record, column),
      },
    },
  ];
}

function onEditChange({ column, value, record }) {
  // 本例
  if (column.dataIndex === 'id') {
    record.editValueRefs.name4.value = `${value}`;
  }
  props.record.recordTemplateId = record.recordTemplateId
  props.record.recordType = record.recordType
}


</script>

<style scoped>

</style>
