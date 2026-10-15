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

<script lang="ts" name="action.effective-shiftCalendar" setup>
import CalendarList from "@/views/dataCalculation/mdm/shiftCalendar/components/CalendarList.vue";

const titleText = '班组排班';
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
