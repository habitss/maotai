<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose :title="title" :width="800"
    :maskClosable="false" :showCancelBtn="false" :showOkBtn="false" :bodyStyle="{height: '68vh'}">
    <a-card v-if="parentData.activeStatus == 1 || parentData.activeStatus == 2" class="detail-card">
      <template #title>
        <p>任务已取消{{ parentData.activeStatus == 1 ? '(待审核)' : '' }}</p>
      </template>
      <a-row class="detail-row">
        <a-col :span="6">取消原因</a-col>
        <a-col :span="14">{{ parentData.remark }}</a-col>
      </a-row>
      <a-row class="detail-row">
        <a-col :span="6">申请人</a-col>
        <a-col :span="14">{{ parentData.activeUserVal }}</a-col>
      </a-row>
      <a-row class="detail-row" v-if="parentData.activeStatus == 2">
        <a-col :span="6">审核人</a-col>
        <a-col :span="14">{{ parentData.updateByVal }}</a-col>
      </a-row>
    </a-card>

    <a-card v-if="parentData.activeStatus != 2" v-for="item in recordsList" :key="item.id" class="detail-card">
      <template #title>
        <p>检查项：{{ item.itemName }}</p>
        <p>检查标准：{{ item.itemStandard }}</p>
        <p>检查地点：{{ item.itemLocation }}</p>
      </template>
      <template v-if="item.naFlag != 1">
        <a-row class="detail-row">
          <a-col :span="6">{{ item.detail }}</a-col>
          <a-col :span="14" v-if="item.resultTypeCode == 1"></a-col>
          <a-col :span="14" v-else-if="item.resultTypeCode == 6">
            <JImageUploadV2 :fileMax="1" v-model:value="item.result" :disabled="true"></JImageUploadV2>
          </a-col>
          <a-col :span="14" v-else-if="item.resultTypeCode == 8 || item.resultTypeCode == 9">
            {{ getOptionsText(item) }}
          </a-col>
          <a-col :span="14" v-else>{{ item.result }}</a-col>
        </a-row>
        <a-row class="detail-row" v-if="item.configIsQualified == 1">
          <a-col :span="6">是否合格</a-col>
          <a-col :span="14">
            <a-switch
              v-model:checked="item.isQualified" 
              checked-children="OK" :checkedValue="1" 
              un-checked-children="NOK" :unCheckedValue="0" 
              disabled
            />
          </a-col>
        </a-row>
        <a-row class="detail-row" v-if="item.isQualified == 0">
          <a-col :span="6">异常描述</a-col>
          <a-col :span="14">{{ item.errorDesc }}</a-col>
        </a-row>
        <a-row class="detail-row" v-if="item.isUploadImg == 1">
          <a-col :span="6">现场照片</a-col>
          <a-col :span="14">
            <JImageUploadV2 :fileMax="1" v-model:value="item.resultImg" :disabled="true"></JImageUploadV2>
          </a-col>
        </a-row>
        <a-row class="detail-row" v-if="item.isCheckTime == 1">
          <a-col :span="6">检查时间</a-col>
          <a-col :span="14">{{ item.createTime }}</a-col>
        </a-row>
        <a-row class="detail-row" v-if="item.isConfirmUser == 1">
          <a-col :span="6">确认人</a-col>
          <a-col :span="14">
            <a-select :options="userOptions" v-model:value="item.confirmUserId" 
            :fieldNames="{ label: 'realname', value: 'id' }" :bordered="false" :disabled="true"> </a-select>
          </a-col>
        </a-row>
        <a-row class="detail-row" v-if="item.isRemark == 1">
          <a-col :span="6">备注</a-col>
          <a-col :span="14">{{ item.qualifiedDesc }}</a-col>
        </a-row>
      </template>
      <a-row class="detail-row" v-if="item.isNa == 1">
        <a-col :span="6">是否NA</a-col>
        <a-col :span="14">
          <a-switch
            v-model:checked="item.naFlag" 
            checked-children="是" :checkedValue="'1'" 
            un-checked-children="否" :unCheckedValue="'0'" 
            disabled
          />
        </a-col>
      </a-row>
    </a-card>
  </BasicModal>
</template>

<script lang="ts" setup>
import { reactive, ref } from 'vue';
import {BasicModal, useModalInner} from '/@/components/Modal';
import { resultList } from '../PlanOrder.api';
import { getFileAccessHttpUrl } from '/@/utils/common/compUtils';
import {mes3UserListAll} from "@/api/common/api";
import JImageUploadV2 from '/@/components/Form/src/jeecg/components/JImageUploadV2.vue';
const queryParam = reactive<any>({});
//设置标题
const title = '详情';
//填报数据
const recordsList = ref<any>([]);
//用户数据
const userOptions = ref<any>([]);
//父级数据
const parentData = ref<any>({});

//表单赋值
const [registerModal, { changeLoading }] = useModalInner(async (data) => {
  changeLoading(true);
  recordsList.value = [];
  parentData.value = data.record;
  //设置填报数据查询参数
  queryParam.orderId = data.record.id;
  queryParam.resultConfigId = data.record.resultConfigId;
  userOptions.value = await mes3UserListAll();
  recordsList.value = await resultList(queryParam);
  changeLoading(false);
});

//根据下拉选项获取显示文本
function getOptionsText(record){
  if(record.result){
    //拆分结果
    const arr = record.result.split(',');
    //根据结果获取文本
    const selectedNames = record.selectionList
      .filter(item => arr.includes(String(item.id)))
      .map(item => item.optionName)
      .join(',');
      return selectedNames;
  }
}

function initFileList(paths) {
  if (!paths || paths.length == 0) {
    return '';
  }
  let files = [] as any;
  let arr = paths.split(',');
  arr.forEach((value) => {
    let url = getFileAccessHttpUrl(value);
    files.push(url);
  });
  return files;
}
</script>

<style lang="less" scoped>
	/** 时间和数字输入框样式 */
  :deep(.ant-input-number) {
    width: 100%;
  }

  :deep(.ant-calendar-picker) {
    width: 100%;
  }
  .detail-card{
    margin-bottom: 20px;
  }
  .detail-row{
    margin-bottom: 10px;
  }
  .ant-card-head-title p{
    margin-bottom: 2px !important;
  }
  :deep(.ant-switch-checked) {
    background-color: green !important; /* 选中的颜色 */
  }
</style>