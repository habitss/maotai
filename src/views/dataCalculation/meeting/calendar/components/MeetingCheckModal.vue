<template>
  <BasicModal @register="registerModal" v-bind="$attrs" :title="title" :width="width" @ok="handleSubmit"
              :cancelText="t('language.closeText')" :showOkBtn=!disabledRef>
    <div class="content">
      <div class="left"
           style="display: inline-flex;  justify-content: center;flex-direction: column; align-items: center;">
        <p
          style="margin-top:40px;display: inline-flex; height: 30px; width:100%; font-size:26px; font-weight: 540; justify-content: center;">
          请扫码签到</p>
        <br />
        <p
          style="display: inline-flex; height: 20px; width:100%; font-size:18px; font-weight: 520; justify-content: center;">
          {{ title_time }}
        </p>
        <p
          style="display: inline-flex; height: 30px; width:100%; font-size:18px; font-weight: 520; justify-content: center;">
          {{ title }}
        </p>
        <br />
        <a-spin :spinning="imgLoading">
          <img v-if="url" style="display: inline-block; width: 240px; height: 230px;" :src="url" alt="">
          <a-empty v-else style="display: inline-block; width: 240px; height: 230px;" description="无效二维码" />
        </a-spin>
        <br />
        <p
          style="display: inline-flex; height: 30px; width:100%; font-size:18px; font-weight: 540; justify-content: center;">
          请扫码签到</p>
        <br />
      </div>
      <div class="right" style="flex: 1;">
        <a-row style="font-weight: 600; font-size:22px;">
          <a-col :span="12">
            <p>截至: {{ curTime }}</p>
          </a-col>
          <a-col :span="12"> {{ count }} 人已签到</a-col>
        </a-row>
        <br />
        <div style="display: inline-block; width: 100%; height: 300px ">
<!--          <a-table ref="table" size="middle" :scroll="{ y: 300 }" rowKey="id" :columns="checkColumns"-->
<!--                   :dataSource="dataSource" :pagination="false" ></a-table>-->
          <BasicTable @register="registerTable" :pagination="false">
            <!--操作栏-->
            <template #action="{ record }">
              <TableAction :actions="getTableAction(record)" />
            </template>
          </BasicTable>
        </div>
      </div>
    </div>
  </BasicModal>
  <MeetingChangeCheckModal @register="registerCancelModal" @success="reload" />
</template>

<script lang="ts"  setup>
import {TableAction, ActionItem, BasicTable} from '/@/components/Table';
import {checkColumns} from '../Meeting.data';
import {BasicModal, useModal, useModalInner} from "@/components/Modal";
import {useI18n} from "@/hooks/web/useI18n";
import {onBeforeUnmount, onMounted, ref} from "vue";
import dayjs from "dayjs";
import {checkList, checkCode} from "@/views/dataCalculation/meeting/calendar/Meeting.api";
import {useUserStore} from "@/store/modules/user";
import {useListPage} from "@/hooks/system/useListPage";
import MeetingChangeCheckModal
  from "@/views/dataCalculation/meeting/calendar/components/MeetingChangeCheckModal.vue";

const props = defineProps({
  record: {
    type: Object,
    default: {}
  }
})

const { t } = useI18n();
const record = ref();
const emit = defineEmits(['success']);
const curTime = ref();
const title_time = ref();
const imgLoading = false;
// 禁用
const disabledRef = ref(false);
const dataSource = ref([]);
const title = ref<string>('会议签到');
const width = ref<number>(1200);
const url = ref();
const count = ref(0);
const userStore = useUserStore();
const queryParams = ref();
const timer = ref();
//注册modal
const [registerModal, { setModalProps, closeModal} ] = useModalInner(async (data: any) => {
  disabledRef.value = false;
  record.value = data;
  await reload();
  title_time.value = data.record.meetingName;
  curTime.value = dayjs().format('YYYY-MM-DD HH:mm:ss')
  url.value = await checkCode({meetingId:data.record.id,ipAddress:window.location.origin,factoryCode:userStore.factoryCode})
  let result = await checkList({meetingId:data.record.id,pageSize:999});
  result.records.forEach(function (value,index)  {
      if(value.active == 1){
        count.value++;
      }
  })
  // count.value = result.records.length;
  setModalProps({ confirmLoading: false, showCancelBtn: false, showOkBtn: false });
});


// 列表页面公共参数、方法
const { tableContext } = useListPage({
  designScope: 'basic-table-demo-ajax',
  tableProps: {
    api: checkList,
    columns: checkColumns,
    showIndexColumn:true,
    showTableSetting: false,
    showActionColumn: true,
    actionColumn: {
      width: 100,
    },
    beforeFetch: (params) => {
      params.column = 'checkTime';
      params.order = 'asc';
      params.pageSize = 999;
      params.meetingId = record.value.record.id;
      return Object.assign(params, queryParams.value);
    },
  },
});
//BasicTable绑定注册
const [registerTable ,{ reload }] = tableContext;

//配置模版modal
const [registerCancelModal, { openModal}] = useModal();
/**
 * 操作栏
 */
function getTableAction(record): ActionItem[] {
  return [
    {
      label: '修改参会状态',
      onClick: handleEdit.bind(null, record),
    },
  ];
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

// 初始化定时器
const initTimer = () => {
   timer.value = setInterval(async () => {
     curTime.value = dayjs().format('YYYY-MM-DD HH:mm:ss');
     if(null != record.value){
       let result = await checkList({meetingId: record.value.record.id,pageSize:999});
       count.value = 0;
       result.records.forEach(function (value,index)  {
         if(value.active == 1){
           count.value++;
         }
       })
       await reload();
     }

   }, 5000); // 5秒刷新一次
};

// 组件挂载时
onMounted(() => {
  reload(); // 立即加载第一次数据
  initTimer();
});

// 组件卸载前
onBeforeUnmount(() => {
  if (timer.value) clearInterval(timer.value);
});
</script>

<style scoped>

.content {
  display: flex;
  height: 460px;
}

.left {
  width: 300px;
  height: auto;
}
</style>
