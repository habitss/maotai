<template>
  <div class="meeting-calender-container">
    <!-- 查询区域 -->
    <a-row class="search-part">
        <a-col :span="24" class="title">
          {{ titleText }}
        </a-col>
    </a-row>
 <CalendarList ref="calendarListRef" ></CalendarList>
  </div>
</template>

<script lang="ts" name="action.effective-meetingCalendar" setup>
import CalendarList from './components/CalendarList.vue';

const titleText = '会议日历';
</script>
<style scoped>
.meeting-calender-container{
  max-width: 100%;
  padding: 10px;
}
.search-part {
  padding: 14px;
  background-color: #fff;
    .title{
      font-weight: bold;
      font-size: 1.4rem;
      text-align: center;
      margin-top: 16px;
    }
}

</style>
