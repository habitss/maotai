<template>
    <BasicModal @register="registerModal" v-bind="$attrs" :title="title" :width="width"
                :cancelText="t('language.closeText')" :showOkBtn="false"
                :showCancelBtn="false" :bodyStyle="{ height: '70vh', overflow: 'auto' }" :maskClosable="false" :destroyOnClose="true">
  <div class="page-container">
    <!-- 头部信息区域 -->
    <a-row :gutter="24" class="header-form">
      <a-col :span="2" class="title_name">
        会议名称:
      </a-col>
      <a-col :span="4">
        <a-input v-model:value="formData.meetingName" placeholder="会议名称" disabled/>
      </a-col>
      <a-col :span="2" class="title_name">
        会议时间:
      </a-col>
      <a-col :span="4">
        <a-input
          v-model:value="formData.meetingDate"
          placeholder="会议时间"
          disabled
        />
      </a-col>
      <a-col :span="2" class="title_name">
        记录人:
      </a-col>
      <a-col :span="4">
        <a-select style="width:160px" show-search v-model:value="formData.recorder" placeholder="请选择记录人" :allowClear=true
                  :options="userList" :filter-option="handleFilterOption" :fieldNames="{ label: 'realname', value: 'id' }">
        </a-select>
      </a-col>
      <a-col :span="2" class="title_name">
        评估人:
      </a-col>
      <a-col :span="4">
        <a-select style="width:160px" show-search v-model:value="formData.leader" placeholder="请选择评估人" :allowClear=true
                  :options="userList" :filter-option="handleFilterOption" :fieldNames="{ label: 'realname', value: 'id' }">
        </a-select>
      </a-col>

    </a-row>

    <!-- 会议内容评分表 -->
    <a-table
      :dataSource="contentData"
      :columns="recordColumns"
      bordered
      :pagination="false"
      style="margin: 5px 0"
    >
      <template #title>会议内容评分表</template>
      <template #bodyCell="{ column, record }">
        <!-- <template v-if="column.dataIndex === 'content'">
          <a-textarea
            v-model:value="record.content"
            style="width: 100%"
          />
        </template>
        <template v-if="column.dataIndex === 'standard'">
          <a-textarea
            v-model:value="record.standard"
          />
        </template>
        <template v-if="column.dataIndex === 'weight'">
          <a-input
            v-model:value="record.weight"
            :min="0"
            style="width: 100%"
          />
        </template> -->
        <template v-if="column.dataIndex === 'score'">
          <a-input-number
            v-model:value="record.score"
            :min="0"
            :max="record.weight"
            style="width: 100%"
          />
        </template>
        <template v-if="column.dataIndex === 'scoreReason'">
          <a-textarea
            v-model:value="record.scoreReason"
            placeholder="填写得分原因"
          />
        </template>
      </template>
      <template #summary>
        <a-table-summary-row>
          <a-table-summary-cell>小计</a-table-summary-cell>
          <a-table-summary-cell>
            <a-typography-text></a-typography-text>
          </a-table-summary-cell>
          <a-table-summary-cell>
            <a-typography-text>{{contentWeightTotal}}</a-typography-text>
          </a-table-summary-cell>
          <a-table-summary-cell>
            <a-typography-text>{{contentTotal}}</a-typography-text>
          </a-table-summary-cell>
          <a-table-summary-cell>
            <a-typography-text></a-typography-text>
          </a-table-summary-cell>
        </a-table-summary-row>
      </template>
    </a-table>

    <!-- 会议效果评分表 -->
    <a-table
      :dataSource="effectData"
      :columns="recordColumns"
      bordered
      :pagination="false"
      style="margin: 5px 0"
    >
      <template #title>会议效果评分表</template>
      <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'score'">
            <a-input-number
              v-model:value="record.score"
              :min="0"
              :max="record.weight"
              style="width: 100%"
            />
          </template>
          <template v-if="column.dataIndex === 'scoreReason'">
            <a-textarea
              v-model:value="record.scoreReason"
              placeholder="填写得分原因"
            />
          </template>

      </template>
      <template #summary>
        <a-table-summary-row>
          <a-table-summary-cell>小计</a-table-summary-cell>
          <a-table-summary-cell>
            <a-typography-text></a-typography-text>
          </a-table-summary-cell>
          <a-table-summary-cell>
            <a-typography-text>{{effectWeighTotal}}</a-typography-text>
          </a-table-summary-cell>
          <a-table-summary-cell>
            <a-typography-text>{{effectTotal}}</a-typography-text>
          </a-table-summary-cell>
          <a-table-summary-cell>
            <a-typography-text></a-typography-text>
          </a-table-summary-cell>
        </a-table-summary-row>
        <a-table-summary-row>
          <a-table-summary-cell>合计</a-table-summary-cell>
          <a-table-summary-cell>
            <a-typography-text></a-typography-text>
          </a-table-summary-cell>
          <a-table-summary-cell>
            <a-typography-text> {{ contentWeightTotal + effectWeighTotal }}</a-typography-text>
          </a-table-summary-cell>
          <a-table-summary-cell>
            <a-typography-text type="danger">
              {{ contentTotal + effectTotal }}
            </a-typography-text>
          </a-table-summary-cell>
          <a-table-summary-cell>
            <a-typography-text></a-typography-text>
          </a-table-summary-cell>
        </a-table-summary-row>
      </template>
    </a-table>

    <div class="action-area">
      <a-button type="primary" :loading="isLoading" @click="handleSave">保存</a-button>
    </div>
  </div>
      <ActionPlanTrackForm ref="actionPlanRef" @success="handleSuccess" :initRecord="false" :isShowTopForm="false" :isShowSubmitBtn="false"></ActionPlanTrackForm>
    </BasicModal>
  </template>

<script lang="ts" name="action.meeting-record" setup>
import {BasicModal, useModal, useModalInner} from "@/components/Modal";
import {useI18n} from "@/hooks/web/useI18n";
import {computed, ref} from "vue";
import {recordColumns} from "@/views/dataCalculation/meeting/calendar/Meeting.data";
import {recordList, saveOrUpdateRecord} from "@/views/dataCalculation/meeting/calendar/Meeting.api";
import {mes3UserListAll} from "@/api/common/api";
import ActionPlanTrackForm from '@/views/dataCalculation/actionPlan/track/ActionPlanTrackForm.vue';
import {list as actionList} from "@/views/dataCalculation/actionPlan/track/ActionPlanTrack.api";
import {useUserStore} from "@/store/modules/user";
import {Dayjs} from "dayjs";

const date = ref<Dayjs>();
const { t } = useI18n();
const props = defineProps({
  record: {
    type: Object,
    default: {}
  }
})

const userInfo = useUserStore();
const emit = defineEmits(['success']);
//行动计划
const actionPlanRef = ref();
// 禁用
const disabledRef = ref(false);
const title = ref<string>('会议评估');
const width = ref<number>(1200);
const meetingDate = ref();
const userList = ref([]);
// 会议内容数据
const contentData = ref([]);
// 会议效果数据
const effectData = ref([]);
const isLoading = ref(false);
// 新增表单数据
const formData = ref({
  meetingId: '',
  meetingName: '',
  recorder: '',
  leader: '',
  meetingDate: null,
});

//注册modal
const [registerModal, { setModalProps, closeModal} ] = useModalInner(async (data: any) => {
  disabledRef.value = false;
  meetingDate.value = data.record
  formData.value.meetingId = meetingDate.value.id
  formData.value.meetingName = meetingDate.value.meetingName
  formData.value.meetingDate = meetingDate.value.meetingDate
  contentData.value = await recordList({meetingId:meetingDate.value.id,meetingType:meetingDate.value.meetingType,recordType:1,column:'sort',order:'asc'});
  effectData.value = await recordList({meetingId:meetingDate.value.id,meetingType:meetingDate.value.meetingType,recordType:2,column:'sort',order:'asc'});
  console.log(contentData.value[0])
  if(contentData.value.length > 0){
    formData.value.recorder = contentData.value[0].recorder;
    formData.value.leader = contentData.value[0].leader;
  }
  await getUserList();
  await initAction(meetingDate.value);
  console.log('====',formData.value)
  setModalProps({ confirmLoading: false, showCancelBtn: false, showOkBtn: false });
});

const handleSave = async () => {
  try {
    isLoading.value = true;
    //组装行动计划所需数据
    const actionForm = {
      raiseDate: date.value?.toDate(),
      problemDescription: '',
      createDept: userInfo?.orgCode,
      problemTypeId: '14',
    };
    //获取行动计划数据
    const actionResult = await actionPlanRef.value?.setFormDataAndResultData(actionForm, false);
    console.log(actionResult);
    //行动计划数据校验
    if(false === actionResult) return;

    const submitData = {
      ...formData.value,
      content: contentData.value,
      effect: effectData.value,
      actionPlanTrackList: actionResult
    };

    await saveOrUpdateRecord(submitData);
    console.log('完整提交数据:', submitData);
    emit('success'); // 触发刷新事件
    //关闭弹窗
    closeModal();
  } catch (error) {
    console.error('保存失败:', error);
  } finally {
    isLoading.value = false;
  }
};

function handleFilterOption(input, option) {
  return option['realname'].toLowerCase().indexOf(input.toLowerCase()) >= 0;
}

async function getUserList(){
  userList.value = await mes3UserListAll();
}

// 计算小计
const contentTotal = computed(() => {
  return contentData.value.reduce((sum, item) => {
    return sum + (Number(item.score) || 0);
  }, 0);
});

const contentWeightTotal = computed(() => {
  return contentData.value.reduce((sum, item) => {
    return sum + (Number(item.weight) || 0);
  }, 0);
});

const effectTotal = computed(() => {
  return effectData.value.reduce((sum, item) => {
    return sum + (Number(item.score) || 0);
  }, 0);
});

const effectWeighTotal = computed(() => {
  return effectData.value.reduce((sum, item) => {
    return sum + (Number(item.weight) || 0);
  }, 0);
});


async function initAction(data){
  //获取数据
  let datas = [];
  if(data.id){
    const {records} = await actionList({pageSize: 999, dataId: data?.id });
    console.log(records)
    datas = records;
  }
  //初始化
  await actionPlanRef.value.otherPageInit({}, datas, true);
}

function handleSuccess(){
  closeModal();
  emit('success');
}


</script>

<style scoped>
.page-container {
  padding: 20px;
  background: #fff;
}

.action-area {
  text-align: center;
  margin-top: 30px;
}

:deep(.ant-table-title) {
  font-weight: bold;
  background: #fafafa;
}
.title_name{
  text-align: center;
  padding: 5px;
}

/* 添加小计行样式 */
.ant-table-summary {
  background: #fafafa;
}

.ant-table-summary-row td {
  font-weight: bold;
}
</style>
