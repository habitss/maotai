<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button type="primary" v-auth="'action.plan:mes3_action_plan_track:edit'"
          preIcon="ant-design:export-outlined" @click="handleSave"> 保存</a-button>
        <a-button type="primary" v-if="!showSearch ? false : true" v-auth="'action.plan:mes3_action_plan_track:exportXls'"
          preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
        <j-upload-button v-if="!showSearch ? false : true" type="primary" v-auth="'action.plan:mes3_action_plan_track:importExcel'"
          preIcon="ant-design:import-outlined" @click="onImportXls">导入</j-upload-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                删除
              </a-menu-item>
            </a-menu>
          </template>
          <a-button v-auth="'action.plan:mes3_action_plan_track:deletionBatch'">批量操作
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
      <!--图片插槽-->
      <template #imgSlots="{ text }">
        <TableImg :size="28" :simpleShow="true" :imgList="initFileList(text)" />
      </template>
      <!--实际完成时间插槽-->
      <template #practicalFinishDate="{ record, text }">
        <div v-if="2 != record.oldFinishVerifyState && (userInfo?.id == record.dutyUserId || userInfo?.id == record.executeUserId || userInfo?.id == record.putUserId || userInfo?.id == record.verifyUserId)">
          <a-date-picker v-model:value="record.practicalFinishDate" format="YYYY-MM-DD" valueFormat="YYYY-MM-DD"
           @change="e => handleFinishDate(e, record)"></a-date-picker>
        </div>
        <div v-else>{{ text }}</div>
      </template>
      
      <!--完成情况插槽-->
      <template #finishState="{ record }">
        <div class="finishState">
          <a-select v-model:value="record.finishState" :bordered="false" :options="planFinishaOptions" :disabled="true"
            :fieldNames="{ label: 'itemKey', value: 'itemVal' }" placeholder="请选择" allowClear style="width:90%"
          >
          </a-select>
        </div>
      </template>
      
      <!--完成情况确认插槽-->
      <template #finishVerifyState="{ record, text }">
        <div v-if="2 != record.oldFinishVerifyState && record.practicalFinishDate && (userInfo?.id == record.verifyUserId || userInfo?.id == record.putUserId)">
          <a-select v-model:value="record.finishVerifyState" :options="finishVerifyStateOptions" @change="(e) => handleFinishStateChange(e, record)"></a-select>
        </div>
        <div v-else>{{ finishVerifyStateOptions.find(item => item.value == text)?.label }}</div>
      </template>
    </BasicTable>
    <!-- 复制功能modal -->
    <ActionPlanTrackModal @register="registerModal" @success="handleSuccess"></ActionPlanTrackModal>
    <!-- 提交功能modal -->
    <ActionPlanTrackEditModal @register="registerEditModal" @success="handleSuccess"></ActionPlanTrackEditModal>
  </div>
</template>

<script lang="ts" name="action.plan-actionPlanTrack" setup>
import { onMounted, reactive, ref } from 'vue';
import { BasicTable, TableAction, TableImg } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useListPage } from '/@/hooks/system/useListPage'
import ActionPlanTrackModal from './components/ActionPlanTrackModal.vue'
import ActionPlanTrackEditModal from './components/ActionPlanTrackEditModal.vue'
import { columns, searchFormSchema, finishVerifyStateOptions, handleFinishVerifyStateChange, handlePlanDateChange } from './ActionPlanTrack.data';
import { list, addBatch, batchDelete, getImportUrl, getExportUrl } from './ActionPlanTrack.api';
import { getFileAccessHttpUrl } from '/@/utils/common/compUtils';
import { useUserStore } from '/@/store/modules/user';
import { dicList } from '/@/views/dataCalculation/mdm/dict/dict.api';
const { userInfo } = useUserStore();

const queryParam = reactive<any>({status: 1});
const props = defineProps({
  raId: { type: String, default: '' },
  dataId: { type: String, default: '' },
  showSearch: { type: Boolean, default: true },
});
//完成状态字典数据
const planFinishaOptions = ref();
//记录有调整过数据的id，避免数据整页保存
const needSaveData = ref({});
//注册model
const [registerModal, { openModal }] = useModal();
const [registerEditModal, { openModal: openEditModal }] = useModal();
//注册table数据
const { tableContext, onExportXls, onImportXls } = useListPage({
  tableProps: {
    title: '行动计划跟踪',
    api: list,
    columns,
    canResize: false,
    scroll: {
      y: document.body.scrollHeight - 410
    },
    formConfig: {
      schemas: props.showSearch ? searchFormSchema : [],
      autoSubmitOnEnter: true,
      showAdvancedButton: true,
      fieldMapToNumber: [
        ['id', ['id_begin', 'id_end']]
      ],
      fieldMapToTime: [
        ['planFinishDate', ['planFinishDate_begin', 'planFinishDate_end'], 'YYYY-MM-DD'],
        ['practicalFinishDate', ['practicalFinishDate_begin', 'practicalFinishDate_end'], 'YYYY-MM-DD'],
        ['raiseDate', ['raiseDate_begin', 'raiseDate_end'], 'YYYY-MM-DD'],
        ['createTime', ['createTime_begin', 'createTime_end'], 'YYYY-MM-DD'],
      ],
    },
    showIndexColumn: true,
    actionColumn: {
      width: 100,
      fixed: 'right'
    },
    beforeFetch: (params) => {
      if (props.raId) {
        params.id = props.raId;
      }
      if (props.dataId) {
        params.dataId = props.dataId;
      }
      params.status = 1;
      return Object.assign(params, queryParam);
    },
    afterFetch: (res) => {
      //添加一个旧记录数据，避免表格操作完成情况时直接退出编辑
      res?.forEach(item => {
        item.oldFinishVerifyState = item.finishVerifyState
      })
      return res;
    },
    defSort: {
      column: 'raiseDate',
    },
  },
  exportConfig: {
    name: "行动计划跟踪",
    url: getExportUrl,
    params: queryParam,
  },
  importConfig: {
    url: getImportUrl,
    success: handleSuccess
  },
})

const [registerTable, { reload, setLoading }, { rowSelection, selectedRowKeys }] = tableContext

/**
 * 复制事件
 */
function handleCopy(record: Recordable) {
  openModal(true, {
    record,
  });
}
/**
 * 编辑事件
 */
function handleEdit(record: Recordable) {
  openEditModal(true, {
    record,
    isUpdate: true,
    showFooter: true,
  });
}
/**
 * 批量删除事件
 */
async function batchHandleDelete() {
  await batchDelete({ ids: selectedRowKeys.value }, handleSuccess);
}
/**
 * 成功回调
 */
function handleSuccess() {
  (selectedRowKeys.value = []) && reload();
}
/**
   * 操作栏
   */
function getTableAction(record) {
  return [
    {
      label: '编辑',
      onClick: handleEdit.bind(null, record),
      auth: 'action.plan:mes3_action_plan_track:edit'
    },
    {
      label: '复制',
      onClick: handleCopy.bind(null, record),
      auth: 'action.plan:mes3_action_plan_track:edit'
    }
  ]
}
//实际完成时间改变事件
function handleFinishDate(value, record){
  //调用事件改变处理方法
  handlePlanDateChange(value, record, 'practicalFinishDate');
  //保存操作数据的id
  needSaveData.value[record.id] = record;
}
//完成情况确认改变事件
function handleFinishStateChange(value, record) { 
  //调用事件改变处理方法
  handleFinishVerifyStateChange(value, record)
  //保存操作数据的id
  needSaveData.value[record.id] = record;
}
//文件列表初始化
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

/**
 * 保存表单数据
 */
async function handleSave(){
  try{
    console.log('needSaveData.value :>> ', needSaveData.value);
    //未操作数据不调用修改接口
    if(!needSaveData.value || Object.keys(needSaveData.value).length === 0){
      return;
    }
    setLoading(true);
    let saveList = Object.values(needSaveData.value);
    await addBatch(saveList);
    reload();
  } catch(e) {
  } finally {
    setLoading(false);
  }
}

onMounted(async () => {
  planFinishaOptions.value = await dicList({code: 'MES3_ACTION_PLAN_FINISH'});
});
</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}
.finishState {
  :deep .ant-select-disabled .ant-select-selection {
    background-color: rgba(0, 0, 0, 0) !important;
    color: black !important;
  }

  :deep .ant-select-selection-item {
    padding: 0 !important;
  }

  :deep .ant-select-arrow {
    display: none !important;
  }
}
</style>