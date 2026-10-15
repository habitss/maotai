<template>
  <BasicModal @register="registerModal" v-bind="$attrs" :title="title" :width="width" @ok="handleSubmit"
              destroyOnClose :cancelText="t('language.closeText')" :showOkBtn=!disabledRef>
    <div>
    <BasicTable @register="registerTable">

      <template #action="{ record }">
        <!-- 操作列 -->
        <TableAction :actions="getTableAction(record)" />
      </template>
      <!--插槽:表格内容-->
      <template #bodyCell="{ column, record, index }">
        <div v-if="column.dataIndex == 'link'" class="custom-div">
          <div v-for="(item, index) in record.links" :key="index">
            <a-button type="link" class="wrap-text" @click="showDetail(item.linkAddress)">{{item.link}}</a-button>
          </div>
        </div>
        <div v-if="column.dataIndex == 'retime'" class="custom-div">
          <a-button type="link" class="wrap-text">{{ totalTime }}</a-button>
        </div>
        <div v-if="column.dataIndex == 'remark'" class="custom-div">
          <a-button type="link" class="wrap-text" @click="showFiles(record)">查看</a-button>
        </div>
      </template>
    </BasicTable>
    </div>
  </BasicModal>
<MeetingEditModal @register="registerEditModal" @success="reload" />
<UploadFilesModel @register="registerUploadFileModal" @success="reload" />
  <MeetingLinksModel @register="registerLinksModal" @success="reload" />
</template>

<script lang="ts"  setup>
import { ActionItem, BasicTable, TableAction } from '/@/components/Table';
import { meetingColumns} from '../Meeting.data';
import {viewList} from '../Meeting.api';
import { useListPage } from '/@/hooks/system/useListPage';
import {BasicModal,useModal, useModalInner} from "@/components/Modal";
import {useMessage} from "@/hooks/web/useMessage";
import {useI18n} from "@/hooks/web/useI18n";
import { ref} from "vue";
import MeetingEditModal
  from "@/views/dataCalculation/meeting/calendar/components/MeetingEditModal.vue";
import UploadFilesModel from "@/views/dataCalculation/meeting/calendar/components/UploadFilesModel.vue";
import {mapTableTotalSummary} from "@/utils/common/compUtils";
import MeetingLinksModel
  from "@/views/dataCalculation/meeting/calendar/components/MeetingLinksModel.vue";


const props = defineProps({
  record: {
    type: Object,
    default: {}
  }
})

const { t } = useI18n();
const dataId = ref();
const emit = defineEmits(['success']);

// 禁用
const disabledRef = ref(false);

const title = ref<string>('会议议程');
const width = ref<number>(1500);



// 列表页面公共参数、方法
const { tableContext } = useListPage({
  designScope: 'basic-table-demo-ajax',
  tableProps: {
    api: viewList,
    columns: meetingColumns,
    showIndexColumn:true,
    showTableSetting: false,
    actionColumn: {
      width: 50,
    },
    showSummary: true,
    summaryFunc: onSummary,
    beforeFetch: (params) => {
      params.column = 'sort';
      params.order = 'asc';
      params.pageSize = 30;
      params.dataId = dataId.value
      return params;
    },
    afterFetch: () =>{
      setShowPagination(false);
    }
  },

});
//BasicTable绑定注册
const [registerTable ,{ reload,setShowPagination }] = tableContext;

//注册modal
const [registerModal, { setModalProps, closeModal} ] = useModalInner(async (data: any) => {
  disabledRef.value = false;
  dataId.value = data.record.id;
  console.log(data.record,dataId.value)
  setModalProps({ confirmLoading: false, showCancelBtn: false, showOkBtn: false });
});

const [registerEditModal, { openModal: openEditModal }] = useModal();

const [registerUploadFileModal, { openModal: openUploadModal }] = useModal();

const [registerLinksModal, { openModal: openLinksModal }] = useModal();

/**
 * 操作栏
 */
function getTableAction(record): ActionItem[] {
  return [
    {
      label: '编辑',
      onClick: handleEdit.bind(null, record),
    },
    // {
    //   label: '查看文件',
    //   onClick: handleUpload.bind(null, record),
    // },
  ];
}

//消息提示
const { createMessage: msg } = useMessage();

/**
 * 编辑事件
 */
function handleEdit(record) {
  openEditModal(true, {'isAdd': false, 'isDetail': false, 'record': record})
}

function handleLinks(record) {
  openLinksModal(true, {'isAdd': false, 'isDetail': false, 'record': record})
}



function showDetail(record){
  console.log(record)
  window.open(record, '_blank');
}

function showFiles(record){
  openUploadModal(true, {'isAdd': false, 'isDetail': true, 'record': record})
}

function onSummary(tableData: Recordable[]){
  const totals = mapTableTotalSummary(tableData, ['time','realTime']);
  console.log(totals)
  totals.realTime = (totals.realTime / 60).toFixed(2) + '分钟';
  totals.time = totals.time + '分钟';
  return [totals];
}
/**
 * 成功回调
 */
function handleSuccess() {
  reload();
}
</script>

<style scoped>
.wrap-text {
  white-space: normal;
  word-wrap: break-word;
}
</style>
