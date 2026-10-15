<template>
  <div>
    <!-- Your template code here -->
    <div class="signBk">
      <a-row style="background-color: red;height: 160px; padding-top: 20px ">
        <div class='logo-title' style="width: 100%; display: flex;
                justify-content: center;"><img src='@/assets/images/logo-title.png' class='mian-title' />
        </div>
      </a-row>
      <br />
      <br />
      <div style="width:100%; padding: 12px 12px">
        <a-row :gutter="24"
               style="display: flex; justify-content: center;padding:26px; flex-direction: column; align-items: center;">
          <p style="font-size: 20px; font-weight: bold;" v-if="meetingName">会议名称：{{
              meetingName }}</p>
          <br />
          <p style="font-size: 18px; font-weight: 500;" v-if="title">会议日期：{{
              title }}</p>
        </a-row>
        <br />
        <a-row :gutter="24" style="margin: 0 0">
          <a-input v-model:value="formData.checkPerson" size="large" placeholder="请输入工号" />
        </a-row>
        <br />
        <a-row :gutter="24" style="margin: 0 0">
          <a-button size='large' style="border-radius: 30px; width: 100%" :loading="canClick"
                    @click="signAction" type='primary'>签到</a-button>
        </a-row>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {onMounted, ref} from 'vue';
import {useUserStore} from "@/store/modules/user";
import {check, info} from "@/views/dataCalculation/meeting/calendar/Meeting.api";
import {useRoute} from "vue-router";
import {useMessage} from "@/hooks/web/useMessage";

//注册Message
const { createMessage: msg } = useMessage();

const userStore = useUserStore();
const meeting = ref();
const meetingName = ref();
const title = ref();
const canClick = ref(false);
const meetingId = ref();

const formData = ref({
  checkPerson: '',
});
async function signAction(){
  canClick.value = true;
  const submitData = {
    ...formData.value,
    meetingId: meetingId.value
  };
  await check(submitData);
  canClick.value = false;
}


onMounted(async () => {
  const route = useRoute();
  // 获取所有查询参数
  const id = route.params.id.split('=')[1];
  title.value = route.params.meetingDate.split('=')[1];
  meetingName.value = route.params.factoryName.split('=')[1];
  meetingId.value = id;

})

</script>

<style scoped>
.logo-title {
  position: absolute;
  top: 60px;
  left: 140px;
}
</style>
