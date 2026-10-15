
<template>
<!--  <a-button type="primary" preIcon="ant-design:plus-outlined" @click="openHandle">选择部门</a-button>-->
<!--  <DeptSelectModal @register="registerSelModal" @getSelectResult="onSelectOk"/>-->
  <KtDepartTreeSelect style="width:300px;" :allowClear="false" v-model:value="dept"
                      :defaultExpandLevel="1" :dropdownAlign="{ overflow: { adjustY } }"
                      :fieldNames=" { label:'title', value: 'orgCode' }" @change="showMeeting"></KtDepartTreeSelect>
  <a-calendar v-model:value="value" @select="onSelect" @panelChange="onPanelChange">
    <template #dateCellRender="{ current }">
      <ul class="events">
        <li v-for="item in getListData(current)" style="line-height: 20px;text-align: center" :key="item.content">
<!--          <a-space v-if="null != item.content && " direction="vertical" style="width: 100%;background-color: #00bb00">-->
<!--              <a-badge :numberStyle="{fontSize: '26px'}" :status="item.type"  size="large" class="wrap-text" :text="item.content" @click="showModal"/>-->
<!--          </a-space>-->
          <a-space v-if="item.color === 1" direction="vertical" style="width: 100%;background-color: gold">
            <a-badge :status="item.type"  size="large" class="wrap-text" :text="item.content" @click="showModal"/>
          </a-space>
          <a-space v-else-if="item.color === 2" direction="vertical" style="width: 100%;background-color: grey">
            <a-badge :status="item.type"  size="large" class="wrap-text" :text="item.content" @click="showModal"/>
          </a-space>
          <a-space v-else-if="item.color === 3" direction="vertical" style="width: 100%;background-color: red">
            <a-badge :status="item.type"  size="large" class="wrap-text" :text="item.content" @click="showModal"/>
          </a-space>
          <a-space v-else-if="item.color === 4" direction="vertical" style="width: 100%;background-color: springgreen">
            <a-badge :status="item.type"  size="large" class="wrap-text" :text="item.content" @click="showModal"/>
          </a-space>
          <a-space v-else-if="item.color === 0" direction="vertical">
            <a-badge :status="item.type"  size="large" class="wrap-text" :text="item.content" @click="showModal"/>
          </a-space>
        </li>
      </ul>
    </template>
  </a-calendar>
  <MeetingList @register="registerListModal"  @success="reload" :record="recordDate" :width="1500"></MeetingList>
</template>

<script lang="ts" name="action.meeting-masterplate" setup>
import {onMounted, ref} from 'vue';
import { Dayjs } from 'dayjs';
import MeetingList from './MeetingList.vue';
import {listForMonth} from "@/views/dataCalculation/meeting/calendar/Meeting.api";
import {useModal} from "@/components/Modal";
import {addFactoryDept} from "@/utils/common/compUtils";
import KtDepartTreeSelect from '/@/components/KtDepartSelect/KtDepartTree.vue';
import { Loading, useLoading } from '/@/components/Loading';
const [openFullLoading, closeFullLoading] = useLoading({
  tip: '加载中...',
});
const value = ref<Dayjs>();
const meetingList = ref([]);

const open = ref<boolean>(false);
const currentMonth = ref(new Date());
const panelMonth = ref();
const panelYear = ref();
const dataResult = ref();
const isShow =ref<boolean>(false);
const dept = ref("");
const recordDate = ref({
  currentDate:undefined,
});


const onPanelChange = async (value: Dayjs, mode: string) => {
  isShow.value = false
  if(dept.value != null && dept.value != "") {
    console.log(dept.value)
    dataResult.value = await listForMonth({deptCode:dept.value,month: value.month() + 1, year: value.year()})
  } else {
    dataResult.value = await listForMonth(addFactoryDept({month: value.month() + 1, year: value.year()}))
  }
    meetingList.value = dataResult.value
    panelMonth.value = value.month() + 1
    panelYear.value = value.year();
};

//新增/编辑modal
const [registerListModal, { openModal}] = useModal();


function getListData(value: Dayjs){
  return getDateList(value.date(),value.month()+1);
}

const onSelect = (value: Dayjs,info:string) => {
  if(info.source == 'date'){
    isShow.value = true
  }
  recordDate.value = { currentDate:value.format("YYYY-MM-DD")};
  if(dept.value != null && dept.value != ""){
    recordDate.value.deptCode = dept.value
    openModal(isShow.value, {'isAdd': false, 'isDetail': false, 'record': recordDate.value})
  }
  openModal(isShow.value, {'isAdd': false, 'isDetail': false, 'record': addFactoryDept(recordDate.value)})
};
const showModal = () => {
  open.value = true;
};
const handleOk = (e: MouseEvent) => {
  open.value = false;
};

async function showMeeting(code){
  openFullLoading();
  dept.value = code;
  if(null != panelMonth.value && panelMonth.value != currentMonth.value.getMonth() + 1){
    console.log(panelMonth.value,panelYear.value)
    dataResult.value = await listForMonth({deptCode:code,month: panelMonth.value, year: panelYear.value})
  } else {
    console.log(currentMonth.value.getMonth())
    dataResult.value = await listForMonth({deptCode:code,month: currentMonth.value.getMonth() + 1, year: currentMonth.value.getFullYear()})
  }

  meetingList.value = dataResult.value;
  closeFullLoading();
}

function getDateList(date,month){
  let list = [{}];
  meetingList.value.forEach(function (value,index){
    if(date == value.day && month == value.month){
      let score = '';
      if(null != value.score && value.score != 0){
        score = '(评分:' + value.score+')'
      }
      list.push({type: 'success', color: value.color, content: value.meetingName  + score + '-'  + value.meetingTime})
    }
  })
  return list;
}

async function reload(){
  openFullLoading();
  if(dept.value != null && dept.value != ""){
    if(null != panelMonth.value && panelMonth.value != currentMonth.value.getMonth() + 1){
      dataResult.value = await listForMonth({deptCode:dept.value,month: panelMonth.value, year: panelYear.value})
    } else {
      dataResult.value = await listForMonth({deptCode:dept.value,month: currentMonth.value.getMonth() + 1, year: currentMonth.value.getFullYear()})
    }
    meetingList.value = dataResult.value
  } else {
    if(null != panelMonth.value && panelMonth.value != currentMonth.value.getMonth() + 1){
      dataResult.value = await listForMonth({deptCode:dept.value,month: panelMonth.value, year: panelYear.value})
    } else {
      dataResult.value = await listForMonth({deptCode:dept.value,month: currentMonth.value.getMonth() + 1, year: currentMonth.value.getFullYear()})
    }
    meetingList.value = dataResult.value
  }
  closeFullLoading();
}

 onMounted(async () => {
  const params = addFactoryDept({month:currentMonth.value.getMonth() + 1,year:currentMonth.value.getFullYear()});
   dept.value = params.deptCode;
   dataResult.value = await listForMonth(params);
   meetingList.value = dataResult.value;
});
</script>

<style scoped>
.events {
  list-style: none;
  margin: 0;
  padding: 0;
}
.events .ant-badge-status {
  overflow: hidden;
  white-space: nowrap;
  width: 100%;
  text-overflow: ellipsis;
  font-size: 12px;
}
.notes-month {
  text-align: center;
  font-size: 28px;
}
.notes-month section {
  font-size: 28px;
}
.events .wrap-text {
  white-space: normal;
  word-wrap: break-word;
  padding: 0;
  color: mintcream;
}

.wrap-text :deep(.ant-badge-dot) {
  transform: scale(5.5); /* 按比例缩放 */
}
/* 单元格整体布局 */
.custom-calendar :deep(.ant-picker-panel) {
  /* 可选：确保面板本身有足够空间 */
  width: 100%;
}

/* 单元格内容容器 */
.custom-calendar :deep(.ant-picker-cell-inner) {
  display: flex !important;
  justify-content: center !important;
  align-items: center !important;
  text-align: center !important;
  height: 100%; /* 确保高度继承 */
}

/* 日期数字的单独控制 */
.custom-calendar :deep(.ant-picker-calendar-date) {
  width: 100%;
  display: block;
  margin: 0 auto;
}

/* 其他相关元素调整 */
.custom-calendar :deep(.ant-picker-cell) {
  padding: 4px 0; /* 调整单元格内边距 */
}

/* 如果是周模式 */
.custom-calendar :deep(.ant-picker-week-panel-row) {
  justify-content: center;
}
</style>
