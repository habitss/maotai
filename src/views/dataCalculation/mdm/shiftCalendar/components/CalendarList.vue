
<template>
  <div class="container">
  <!-- 查询区域 -->
  <a-form  :label-col="labelCol" :wrapper-col="wrapperCol" class="search-part">
    <a-row :gutter="24">
      <a-col :span="8">
        <a-form-item :label="t('language.department')">
          <kt-depart-select v-model:value="queryParam.deptCode" :placeholder="t('language.selectDepartment')"
                            select-type="depart" @change="handleDeptChange" />
        </a-form-item>
      </a-col>
      <a-col :span="8">
        <a-form-item :label="t('language.productLine')">
          <kt-depart-select  v-model:value="queryParam.lineCode" select-type="line"
                             :placeholder="t('language.selectProductLine')" :parentKey="queryParam.deptCode" />
        </a-form-item>
      </a-col>
      <a-col :span="8">
        <a-button type="primary" preIcon="ant-design:search-outlined" @click="searchQuery">{{
            t('language.search')
          }}</a-button>
        <a-button type="primary" preIcon="ant-design:reload-outlined" @click="searchReset"
                  style="margin-left: 8px">{{
            t('language.reset')
          }}</a-button>
        <a-button type="primary" preIcon="ant-design:reload-outlined" @click="showSchedule"
                  style="margin-left: 8px">自动排班</a-button>
      </a-col>
    </a-row>

  </a-form>
  </div>
  <a-calendar v-model:value="value" @select="onSelect" @panelChange="onPanelChange">
    <template #dateCellRender="{ current }">
      <ul class="events">
        <li v-if="getListData(current) > 1" >
          <a-button style="margin-top:20px;margin-left:10px;width:100px;background-color: #4CAF50 !important; border-color: #4CAF50 !important" @click="showModal">
            班
          </a-button>
        </li>
      </ul>
    </template>
  </a-calendar>
  <ShiftRelationList @register="registerListModal"  @success="reload" :record="recordDate" :width="1500"></ShiftRelationList>
  <RelationScheduleModal @register="registerScheduleModal"  @success="reload" :record="recordDate" :width="500"></RelationScheduleModal>
</template>

<script lang="ts" name="action.shift-masterplate" setup>
import {onMounted, ref} from 'vue';
import { Dayjs } from 'dayjs';
import ShiftRelationList from './ShiftRelationList.vue';
import {list} from "/@/views/dataCalculation/mdm/shiftCalendar/ShiftCalendar.api";
import {useModal} from "@/components/Modal";
import {addFactoryDept} from "@/utils/common/compUtils";
import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
import { useI18n } from '/@/hooks/web/useI18n';
import RelationScheduleModal
  from "@/views/dataCalculation/mdm/shiftCalendar/components/RelationScheduleModal.vue";

const { t } = useI18n();
const value = ref<Dayjs>();
const shiftList = ref([]);

const queryParam = ref<any>({});
const open = ref<boolean>(false);
const currentMonth = ref(new Date());
const dataResult = ref();
const isShow =ref<boolean>(false);
const dept = ref("");
const recordDate = ref({
  currentDate:undefined,
});
const labelCol = { style: { width: '60px' } };
const wrapperCol = { span: 12 };

const onPanelChange = async (value: Dayjs, mode: string) => {
  isShow.value = false
  dataResult.value = await list(addFactoryDept({month: value.month() + 1, year: value.year()}))
  shiftList.value = dataResult.value
};

//新增/编辑modal
const [registerListModal, { openModal}] = useModal();

//新增/编辑modal
const [registerScheduleModal, { openModal:openScheduleModel}] = useModal();

async function searchQuery(){
  console.log(currentMonth.value)
  isShow.value = false;
  console.log(queryParam.value)
  dataResult.value = await list({month:currentMonth.value.getMonth() + 1,year:currentMonth.value.getFullYear()
    ,deptCode:queryParam.value.deptCode,lineCode:queryParam.value.lineCode})

  shiftList.value = dataResult.value
}

function getListData(value: Dayjs){
  let result =  getDateList(value.date(),value.month()+1);
  return result.length;
}

const onSelect = (value: Dayjs,info:string) => {
  if(info.source == 'date'){
    isShow.value = true
  }
  recordDate.value = { currentDate:value.format("YYYY-MM-DD")};
  if(dept.value != null || dept.value != ""){
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

function showSchedule(){
  console.log(recordDate.value)
  isShow.value = true
  openScheduleModel(isShow.value, {'isAdd': false, 'isDetail': false, 'record': recordDate.value})
}

function getDateList(date,month){
  let list = [{}];
  shiftList.value.forEach(function (value,index){
    if(date == value.day && month == value.month){
      list.push({type: 'success', content: value.meetingName })
    }
  })
  return list;
}

async function reload(){
  dataResult.value = await list(addFactoryDept({month:currentMonth.value.getMonth() + 1,year:currentMonth.value.getFullYear()}))
  shiftList.value = dataResult.value
}


//部门改变事件
async function handleDeptChange(value){
  //清空产线
  queryParam.value.lineCode = []
}

 onMounted(async () => {
   dataResult.value = await list(addFactoryDept({month:currentMonth.value.getMonth() + 1,year:currentMonth.value.getFullYear()}))
   shiftList.value = dataResult.value
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


.container {
  max-width: 100%;
  padding: 10px;
}

.search-part {
  padding: 20px;
  background-color: #fff;
}
</style>
