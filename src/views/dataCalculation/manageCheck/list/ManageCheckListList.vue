<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button type="primary" v-auth="'manage.check:mes3_manage_check_list:add'" @click="handleAdd"
          preIcon="ant-design:plus-outlined"> 新增</a-button>
        <a-button type="primary" v-auth="'manage.check:mes3_manage_check_list:exportXls'"
          preIcon="ant-design:export-outlined" @click="handleExportXls"> 导出</a-button>
        <j-upload-button type="primary" v-auth="'manage.check:mes3_manage_check_list:importExcel'"
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
          <a-button v-auth="'manage.check:mes3_manage_check_list:deletionBatch'">批量操作
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <!--问题描述插槽-->
      <template #problemDescSlots="{ record, text }">
        <a-button type="link" class="problemDesc" @click="handleEdit(record)"><span>{{ text }}</span></a-button>
      </template>
      <!--行动插槽-->
      <template #actionTitleSlots="{ record, text }">
        <a-button type="link" class="problemDesc" @click="handleOpenActionModal(record)"><span>{{ record?.actionPlanTrack?.actionTitle }}</span></a-button>
      </template>
      <!--图片插槽-->
      <template #imgSlots="{ text }">
        <TableImg :size="28" :simpleShow="true" :imgList="initFileList(text)" />
      </template>
      <!--  部门插槽  -->
      <template #form-dept="{ model, field, schema }">
        <kt-depart-select
          v-model:value="model[field]"
          select-type="depart"
          :placeholder="`请选择${schema.label}`"
          :valueField = "'id'"
          :disabled = 'currentDepart?.codeLength > 9'
        />
      </template>
    </BasicTable>
    <!-- 表单区域 -->
    <ManageCheckListModal @register="registerModal" @success="handleSuccess"></ManageCheckListModal>

    <!-- 行动计划modal -->
    <ActionPlanTrackEditModal @register="registerActionModal" @success="searchSubmit"></ActionPlanTrackEditModal>
  </div>
</template>

<script lang="ts" name="manage.check-manageCheckList" setup>
import { reactive } from 'vue';
import { BasicTable, TableImg } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useListPage } from '/@/hooks/system/useListPage'
import ManageCheckListModal from './components/ManageCheckListModal.vue'
import { columns, searchFormSchema, ACTION_PLAN_FINISH, currentDepart } from './ManageCheckList.data';
import { list, batchDelete, getImportUrl, getExportUrl } from './ManageCheckList.api';
import { getFileAccessHttpUrl } from '/@/utils/common/compUtils';
import { exportExcel } from '/@/utils/common/exportToExcelUtils';
import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
import ActionPlanTrackEditModal from '/@/views/dataCalculation/actionPlan/track/components/ActionPlanTrackEditModal.vue'
import { cloneDeep } from 'lodash-es';
const queryParam = reactive<any>({});
//注册model
const [registerModal, { openModal }] = useModal();
//注册model
const [registerActionModal, { openModal: openActionModal }] = useModal();
//注册table数据
const { tableContext, onExportXls, onImportXls } = useListPage({
  tableProps: {
    title: '管理者检查清单-分层检查清单',
    api: list,
    columns,
    maxColumnWidth: 90,
    ellipsis: false,
    showActionColumn: false,
    rowKey: 'actionId',
    formConfig: {
      schemas: searchFormSchema,
      autoSubmitOnEnter: true,
      showAdvancedButton: true,
      autoAdvancedCol: 16,
      alwaysShowLines: 2,
      submitFunc: searchSubmit,
      fieldMapToNumber: [
      ],
      fieldMapToTime: [
        ['checkDate', ['checkDate_begin', 'checkDate_end'], 'YYYY-MM-DD'],
        ['createTime', ['createTime_begin', 'createTime_end'], 'YYYY-MM-DD'],
      ],
    },
    beforeFetch: (params) => {
      params.column = 'checkDate';
      params.order = 'desc';
      return Object.assign(params, queryParam);
    },
    afterFetch: (res) => {
      let idFlag = '';
      //需要合并的行数
      let rowSpan = 1;
      res.forEach((item, index) => {
        //处理后台拼接的id
        const ids = item.id.split('_');
        item.id = ids[0];
        item.actionId = ids[1] || item.id;

        if (idFlag != item.id) {
          //如果id不同，则合并之前的行数，并重置rowSpan和id
          if (index != 0) {
            res[(index - rowSpan)].rowSpan = rowSpan;
          }
          idFlag = item.id
          rowSpan = 1;
        } else {
          //如果id相同，则rowSpan加1,并判断是否为最后一行，如果是最后一行，则需要合并之前的行数
          rowSpan++;
          res[index].rowSpan = 0;
          if (index == res.length - 1) {
            res[(index - (rowSpan-1))].rowSpan = rowSpan;
          }
        }
      })
      return res;
    }
  },
  exportConfig: {
    name: "管理者检查清单-分层检查清单",
    url: getExportUrl,
    params: queryParam,
  },
  importConfig: {
    url: getImportUrl,
    success: handleSuccess
  },
})

const [registerTable, { reload, getSelectRows, getForm }, { rowSelection, selectedRowKeys }] = tableContext

//重写查询按钮提交事件
async function searchSubmit() {
  reload();
}
/**
 * 新增事件
 */
function handleAdd() {
  openModal(true, {
    isUpdate: false,
    showFooter: true,
  });
}
/**
 * 编辑事件
 */
function handleEdit(record: Recordable) {
  openModal(true, {
    record,
    isUpdate: true,
    showFooter: true,
  });
}
/**
 * 批量删除事件
 */
async function batchHandleDelete() {
  await batchDelete(getSelectRows(), handleSuccess);
}
/**
 * 成功回调
 */
function handleSuccess() {
  (selectedRowKeys.value = []) && reload();
}
/**
 * 打开行动计划modal事件
 */
function handleOpenActionModal(record){
  openActionModal(true, {
    record: {id:  record?.actionPlanTrack?.id},
    isUpdate: true,
    showFooter: true,
  });
}
//图标列表解析
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

async function handleExportXls(){
  const columnMap = {} as any;
  for(const item of columns){
    columnMap[item.dataIndex as string] = item.title;
  }
  let newArr = [] as any;
  //获取查询表单数据
  const formValue = getForm().getFieldsValue();
  formValue.pageSize = 10000;
  //处理传参形式为actionPlanTrack.xxx而不是处理传参形式为actionPlanTrack[xxx]
  for(const key in formValue?.actionPlanTrack){
    formValue['actionPlanTrack.'+key] = formValue?.actionPlanTrack[key];
  }
  delete formValue?.actionPlanTrack;
  
  //导出选中数据,如果未选中则导出全部
  let tableData = cloneDeep(getSelectRows());
  if(!tableData || tableData.length == 0){
    //导出全部数据
    const {records} = await list(formValue);
    tableData = records;
  }
  for(const item of tableData){
    let obj = {
      '主键': item.id.split('_')[0],
      '行动计划主键': item.actionPlanTrack?.id,
    } as any;
    for(const key in columnMap){
      //处理嵌套对象
      if(key.startsWith('actionPlanTrack.')){
        if(item.actionPlanTrack){
          const tempKey = key.split('.')[1];
          let tempValue = item.actionPlanTrack[tempKey];
          if(tempKey === 'status'){
            tempValue = tempValue == 1 ? '进入' : '不进入';
          } else if (tempKey === 'finishState'){
            tempValue = ACTION_PLAN_FINISH.value.find(item => item.value === tempValue)?.label || '';
          }

          obj[columnMap[key]] = tempValue;
        }
      }else{
        obj[columnMap[key]] = item[key];
      }
    }
    newArr.push(obj);
  }
  //分层检查清单导出
  exportExcel('分层检查清单', newArr, null);
}
</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}


.problemDesc{
  max-width: 100%; 
  word-break: normal;
  white-space: normal;
  overflow-wrap: break-word;
  padding: 0;
  span{
    width: 100%;
    word-break: normal;
    white-space: normal;
    overflow-wrap: break-word;
  }
}
</style>