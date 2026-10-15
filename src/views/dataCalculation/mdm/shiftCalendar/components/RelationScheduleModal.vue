<template>
  <BasicModal @register="registerModal" v-bind="$attrs" :title="title" :width="width" height="280" @ok="handleSubmit"
              :cancelText="t('language.closeText')" :showOkBtn=!disabledRef>
    <a-form @finish="handleSubmit">
      <a-row :gutter="16" align="middle">
        <!-- 日期选择行 -->
        <a-col :span="24">
          <a-form-item
            label="日期范围"
            :label-col="{ style: 'width: 190px' }"
            :wrapper-col="{ style: 'width: 300px' }"
          >
            <a-range-picker v-model:value="formData.timeRange" style="width: 220px
" value-format="YYYY-MM-DD" />
          </a-form-item>
        </a-col>
      </a-row>
    <a-row :gutter="16" align="middle">

      <a-col v-for="item in shift" :key="item.manageId" class="custom-select-item">
        <a-input hidden="hidden" :value="item.manageId" />
        <span class="label">{{ item.shiftName }}</span>
        <a-select
          v-model:value="item.selectedGroupId"
          :placeholder="item.placeholder"
          style="width: 220px"
        >
          <a-select-option
            v-for="opt in selectedValues"
            :key="opt.id"
            :value="opt.id"
          >
            {{ opt.groupName }}
          </a-select-option>
        </a-select>
      </a-col>
    </a-row>
    </a-form>
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { useI18n } from '/@/hooks/web/useI18n';
import {
  groupList,
  shiftList,
  addSchedule,
  addList
} from "@/views/dataCalculation/mdm/shiftCalendar/ShiftCalendar.api";
import {useMessage} from "@/hooks/web/useMessage";
import {cloneDeep} from "lodash-es";
import {useUserStore} from "@/store/modules/user";

const { t } = useI18n();
const record = ref();
const emit = defineEmits(['success']);

const userInfo = useUserStore();
// 禁用
const disabledRef = ref(false);
const selectedValues = ref([]);
const title = ref<string>('');
const width = ref<number>(500);

// 新增表单数据
const formData = ref({
  manageId: '',
  groupId: '',
  shiftDate: null,
  timeRange:null
});

const shift = ref([]);

// 判断新增或编辑或详情标识
const isAdd = ref();
const isDetail = ref();

//需修改记录的id
const recordId = ref();

const current = ref();

//注册modal
//注册modal
const [registerModal, { closeModal }] = useModalInner(async (data: any) => {
  disabledRef.value = false;
  formData.value.timeRange = null;
  shift.value = [];
  console.log(data.record)
  formData.value.shiftDate = data.record.currentDate
  current.value = data.record.currentDate
  await getListData(current.value);
  await getGroupListData(current.value);
  //重置表单数据
  //赋值操作标识
  isAdd.value = data.isAdd;
  isDetail.value = data.isDetail

  if (data.isDetail) {
    title.value = '详情';
    disabledRef.value = true;
  }
  if (data.isAdd) {
    title.value = '新增';
    current.value = data.record.currentDate
  } else {
    title.value = '编辑';
    let newData = cloneDeep(data.record);

    let code = '';
    if(null != newData.deptCode && '' != newData.deptCode){
      code = newData.deptCode
    } else {
      code = newData.factoryCode
    }
    console.log(code)

    //赋值需修改记录的id
    recordId.value = newData.id;
    //设置表单值
  }
});

//
// /**
//  * BasicForm绑定注册;
//  */
// const [registerForm, { resetFields, validate, setFieldsValue, updateSchema }] = useForm({
//   //注册表单列
//   schemas: addConfigFormSchemas,
//   //回车提交
//   autoSubmitOnEnter: true,
//   showActionButtonGroup: false,
//   disabled: disabledRef,
//   //表格label宽度
//   labelCol: { style: { width: '100px' } },
// });

//注册Message
const { createMessage: msg } = useMessage();
//表单提交事件
async function handleSubmit() {
  try {
    //提交表单
    console.log(formData.value.timeRange)
    const submitData = shift.value.map(item => ({
      manageId: item.manageId,
      groupId: item.selectedGroupId,
      currentDate: current.value,
      times:formData.value.timeRange
    }));
    console.log('最终提交数据:', submitData);
    await addSchedule(submitData);
    //关闭弹窗
    closeModal();

    //刷新外部表格
    emit('success');
  } catch (error) {
    msg.error({ content: '保存失败', key: 'saving' });
  }
}

async function getListData(value){
  console.log(value)
  shift.value = await addList({deptCode:userInfo?.userInfo?.orgCode,currentDate:value});
  console.log(shift.value)
}

async function getGroupListData(value){
  console.log(value)
  selectedValues.value = await groupList({deptCode:userInfo?.userInfo?.orgCode});
  console.log(selectedValues.value)
}


function handleClose(){
  closeModal();
  emit('success');
}
</script>

<style scoped>
.custom-select-item {
  display: flex;
  align-items: center;
  margin-right: 20px;
  margin-top: 20px;
}

.label {
  margin-right: 38px;
  width: 152px;
  text-align: right;
}
</style>
