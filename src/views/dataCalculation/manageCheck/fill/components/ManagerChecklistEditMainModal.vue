<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose :title="title" :width="1200" @ok="handleSubmit"
    :defaultFullscreen="true" :maskClosable=false :closable="false" :ok-text="'保存'">
    <div class="postText">{{ formTitle }}检查清单</div>
    <!--引用表格-->
    <BasicTable @register="registerTable" style="padding: 0;">
      <template #form-stopProduction="{ model, field }">
        <a-select v-model:value="model[field]" style="width: 100%" @change="handleStopProductionClick">
          <a-select-option value="1">生产</a-select-option>
          <a-select-option value="0">停产</a-select-option>
          <a-select-option value="2">休假</a-select-option>
        </a-select>
      </template>

      <template #rowTypeSlot="{ text }">
        <label v-if="text == 1">检查记录</label>
        <label v-else-if="text == 2">检查图片</label>
        <label v-else-if="text == 3">检查判定</label>
        <label v-else-if="text == 4">问题描述</label>
        <label v-else-if="text == 5">问题提交</label>
      </template>
      <template #resulSlot="{ record, index }">
        <!-- //检查记录 -->
        <a-textarea v-if="record.rowType == '1'" v-model:value="record.record" placeholder="请输入检查记录"
          :disabled="isEditData(record, '')" />
        <!-- //检查图片 -->
        <div v-if="record.rowType == '2'">
          <JImageUploadV2 :fileMax="5" v-model:value="record.picture" :disabled="isEditData(record, '')"></JImageUploadV2>
        </div>
        <!-- //检查判定 -->
        <div v-if="record.rowType == '3'" class="cell-content">
          <a-switch class="custom-switch" checkedValue="true" checkedChildren="OK" unCheckedValue="false" unCheckedChildren="NOK"
            v-model:checked="record.sure" :disabled="isEditData(record, '')" />
        </div>
        <!-- //异常描述 -->
        <a-textarea v-if="record.rowType == '4'" v-model:value="record.description" placeholder="请输入异常描述"
          :disabled="isEditData(record, '')" />
        <!-- //异常提交 -->
        <div v-if="record.rowType == '5'" @click="addRow(record, index, isEditData(record, 'edit'))"
          :disabled="isEditData(record, '')" style="cursor:pointer">
          <a-button type="link" :style="{color: record.layeringId ? 'green' :'#D92622'}" >问题录入({{ record.layeringId ? '已录入' : '未录入' }})</a-button>
        </div>
      </template>
    </BasicTable>
    <!-- 分层检查 -->
    <ManageCheckListModal @register="registerAddModal" @success="handleSuccess" @updateDesc="handleUpdateDesc"></ManageCheckListModal>
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref, computed, unref, nextTick } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicTable } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage'
import { formSchema, modalColumns } from '../ManagerChecklistEditMain.data';
import { getWeek } from '/@/views/dataCalculation/manageCheck/list/ManageCheckList.data';
import { saveOrUpdate, queryInspectData } from '../ManagerChecklistEditMain.api';
import { queryById as queryCheckListById } from '/@/views/dataCalculation/manageCheck/list/ManageCheckList.api';
import { useModal } from '/@/components/Modal';
import ManageCheckListModal from '/@/views/dataCalculation/manageCheck/list/components/ManageCheckListModal.vue'
import JImageUploadV2 from '/@/components/Form/src/jeecg/components/JImageUploadV2.vue';
// Emits声明
const emit = defineEmits(['register', 'success']);
const isUpdate = ref(true);
const isDetail = ref(false);
//设置MODAL标题
const title = computed(() => (!unref(isUpdate) ? '新增' : !unref(isDetail) ? '详情' : '编辑'));
//表单标题
const formTitle = ref('');
//上级传入数据
const parentData = ref({} as any);
//记录数据
const mainRecord = ref({} as any);
//注册分层检查model
const [registerAddModal, { openModal : openAddModal }] = useModal();
//表单赋值
const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
  //重置表单数据
  getForm().resetFields();
  //设置行合并
  modalColumns.forEach((item, i) => {
    item.customCell = (record) => {
      let rowSpan = 1;
      if (i < 7) {
        if (record.rowType == 1) {
          rowSpan = 5;
        } else {
          rowSpan = 0;
        }
      }
      let style = {} as any;
      if (record.disabledFlag == true) {
        style = { color: "#bfbfbf" };
      }
      return { 
        rowSpan, 
        style 
      };
    };
  });
  setColumns(modalColumns);

  parentData.value = data;
  formTitle.value = data.postName;
  setModalProps({ confirmLoading: false, showOkBtn: !!data?.showFooter });
  isUpdate.value = !!data?.isUpdate;
  isDetail.value = !!data?.showFooter;
  if (unref(isUpdate)) {
    getForm().setFieldsValue(data.record);
  }
  setProps({scroll: {y: window.innerHeight - 300}});
});

//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    immediate: false,
    showTableSetting: false,
    showActionColumn: false,
    ellipsis: false,
    pagination: false,
    formConfig: {
      schemas: formSchema,
      autoSubmitOnEnter: true,
      showActionButtonGroup: false,
      autoAdvancedCol: 30,
      baseColProps: { span: 4 },
      submitFunc: reloadInspectData
    },
  },
})

const [registerTable, { getForm, getDataSource, setTableData, setColumns, updateTableData, setProps }] = tableContext;

async function reloadInspectData() {
  nextTick(async () => {
    //查询参数
    let searchParams = {
      deptCode: parentData.value.deptCode,
      postId: parentData.value.postId,
    } as any;
    //如果是编辑或者详情，则传入record数据作为查询参数
    if(isUpdate.value){
      searchParams.id= parentData.value.record.id;
    }else{
      searchParams.pushWeek = getForm().getFieldsValue().pushWeek;
      searchParams.stopProduction = getForm().getFieldsValue().stopProduction;
    }
    const { itemList, mainData } = await queryInspectData(searchParams);
    mainRecord.value = mainData;
    setTableData(itemList);
  })
}

function isEditData(record, type) {
  //确认结果[不可编辑]
  if (mainRecord.value.confirmResult && mainRecord.value.confirmResult != '0' && type != 'edit') {
    record.disabledFlag = true;
    return true;
  }
  //停产或休假且是否生产推送(是) 无法编辑
  const stopProduction = getForm().getFieldsValue().stopProduction;
  if ((stopProduction == 0 || stopProduction == 2) && record.stopProduction == 1) {
    //不可编辑
    record.disabledFlag = true;
    return true;
  } else {
    return false;
  }
}

//新增分层检查
async function addRow(record, index, isEdit) {
  //是否可以修改
  if(isEdit){return false};
  const formData = await getForm().getFieldsValue();
  let addData = {} as any;
  //是否有分层检查数据，有的话先查询
  if(record.layeringId){
    const res = await queryCheckListById({id: record?.layeringId});
    addData = res;
  }else{
    //组装新增数据
    addData = {
      checkDate: formData.recordDate,
      week: await getWeek(formData.recordDate),
      checkType: String(parentData.value.postId),
      checkUser: formData.inspectedBy,
      checkUserPost: String(parentData.value.postId),
      problemDesc: getDataSource()[index-1].description,
      problemImg: getDataSource()[index-3].picture,
    };
  }
  addData.rIndex = index;
  openAddModal(true, {
    record: addData,
    isUpdate: record?.layeringId ? true : false,
    showFooter: !'1,2'.includes(formData?.confirmResult),
  });
};

/**
 * 顶部生产状态切换事件
 * @param value 生产状态 0:停产  1:生产  2:休假
 */
function handleStopProductionClick(value){
  //获取表格数据
  let tableData = getDataSource();
  for(const item of tableData){
    //如果选择停产或休假，当前记录为必须生产下执行则禁止填写
    if((value == 0 || value == 2) && item.stopProduction == 1){
      item.disabledFlag = true;
    } else {
      item.disabledFlag = false;
    }
  }
  //如果为停产或休假，则按照停产排在最上，否则按序号排序
  if(value == 0 || value == 2){
    tableData.sort((a, b) => a.stopProduction - b.stopProduction);
  } else {
    tableData.sort((a, b) => a.number - b.number);
  }
  setTableData(tableData);
}

/**
 * 子页面修改当前页面的问题描述
 * @param record 
 */
function handleUpdateDesc(index, content){
  updateTableData(index-1, "description", content);
}

//分层检查保存成功
function handleSuccess(data, checkId){
  updateTableData(data.rIndex, "layeringId", checkId);
}

//表单提交事件
async function handleSubmit() {
  try {
    setModalProps({ confirmLoading: true });
    //获取顶部表单数据
    const formData = getForm().getFieldsValue();
    const formStopProduction = formData.stopProduction != 1 ? 0 : 1;
    //判断是否所有检查记录都填写了
    let isEdit = 1;
    for(const item of getDataSource()){
      const stopProduction = item.stopProduction ? item.stopProduction : 0;
      if(item.rowType == 1 && stopProduction <= formStopProduction && !item.record){
        isEdit = 0;
        break;
      }

    }
    //组装表单数据
    formData.managerChecklistEditRecordList = getDataSource();
    formData.postId = parentData.value.postId;
    formData.isEdit = isEdit;
    //提交表单
    await saveOrUpdate(formData, isUpdate.value);
    //关闭弹窗
    closeModal();
    //刷新列表
    emit('success');
  } catch (error) {
  } finally {
    setModalProps({ confirmLoading: false });
  }
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

:deep(.ant-form) {
  background-color: #EBF1FB !important;
  margin-bottom: 0 !important;
}

:deep(.ant-table-wrapper) {
  padding: 0;
  border-radius: 0;
}

:deep(.ant-table-thead .ant-table-cell) {
  background-color: #EBF1FB;
  border: 1px solid #e5e7eb;
}

.postText {
  height: 40px;
  line-height: 40px;
  text-align: center;
  font-size: 20px;
  font-weight: bold;
  background-color: #EBF1FB;
  border: 1px solid #e5e7eb;
}

/* 覆盖 a-switch 的背景色和滑块颜色 */
.ant-switch {
  background-color: #ff5722 !important; /* 修改背景色 */
}
.ant-switch-checked {
  background-color: #4CAF50 !important; /* 修改选中时的背景色 */
}
.ant-switch-handle {
  background-color: #fff !important; /* 修改滑块颜色 */
}
</style>