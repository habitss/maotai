<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button type="primary" v-auth="'practice:mes3_best_practice_fill:add'" @click="handleAdd"
          preIcon="ant-design:plus-outlined"> 新增</a-button>
        <a-button type="primary" v-auth="'practice:mes3_best_practice_fill:exportXls'"
          preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
        <j-upload-button type="primary" v-auth="'practice:mes3_best_practice_fill:importExcel'"
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
          <a-button v-auth="'practice:mes3_best_practice_fill:deletionBatch'">批量操作
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>

      <!--  部门插槽    -->
      <template #form-deptCode="{ model, field }">
        <kt-depart-select v-model:value="model[field]" select-type="depart" :placeholder="'请选择提报部门'" />
      </template>
      <!--查询表单-年月插槽-->
      <template #form-yearMon="{ model, field }">
        <KtMonthsPicker v-model:value="model[field]" :maxTagCount="2" />
      </template>

      <!-- 名称插槽 -->
      <template #title="{ record }">
        <a-button type="link" class="projectName" @click="handleTitleClick(record)">{{ record.title }}</a-button>
      </template>
      <!-- 评审结果插槽 -->
      <template #shiftGroupResult="{ record }">
        <a-button :disabled = "!hasPermission('practice:mes3_best_practice_fill:shift_group_score')"
          type="link" @click="handleResultClick(record, 1, record.shiftGroupReportId)">{{ record.shiftGroupAvgScore ?? '点击' }}</a-button>
      </template>
      <template #deptResult="{ record }">
        <a-button :disabled = "!hasPermission('practice:mes3_best_practice_fill:dept_score')"
          type="link" @click="handleResultClick(record, 2, record.deptReportId)">{{ record.deptAvgScore ?? '点击' }}</a-button>
      </template>
      <template #factoryResult="{ record }">
        <a-button :disabled="record.deptResult == 0 || record.deptAvgScore < 18 || !hasPermission('practice:mes3_best_practice_fill:factory_score')"
          type="link" @click="handleResultClick(record, 3, record.factoryReportId)">{{ record.factoryAvgScore ?? '点击' }}</a-button>
      </template>
    </BasicTable>
    <!-- 表单区域 -->
    <BestPracticeFillModal @register="registerModal" @success="handleSuccess"></BestPracticeFillModal>
    <!-- 表单区域 -->
    <BestPracticeRecommendModal @register="registerRecommendModal" @success="handleSuccess"></BestPracticeRecommendModal>
    <!-- 评审 -->
    <BestPracticeConfigModal @register="registerConfigModal" @success="handleSuccess"></BestPracticeConfigModal>
  </div>
</template>

<script lang="ts" name="practice-bestPracticeFill" setup>
import { reactive } from 'vue';
import { BasicTable, TableAction } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useListPage } from '/@/hooks/system/useListPage'
import BestPracticeFillModal from './components/BestPracticeFillModal.vue'
import BestPracticeRecommendModal from './components/BestPracticeRecommendModal.vue'
import { columns, searchFormSchema } from './BestPracticeFill.data';
import { list, deleteOne, batchDelete, getExportUrl, getImportUrl } from './BestPracticeFill.api';
import BestPracticeConfigModal from '/@/views/dataCalculation/practice/config/components/BestPracticeConfigModal.vue'
import KtMonthsPicker from '/@/components/KtMonthsPicker/KtMonthsPicker.vue';
import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
import { usePermission } from '/@/hooks/web/usePermission'
const { hasPermission } = usePermission();
const queryParam = reactive<any>({});
//注册model
const [registerModal, { openModal }] = useModal();
//注册推荐model
const [registerRecommendModal, { openModal: opeRecommendModalModal }] = useModal();
//注册评审model
const [registerConfigModal, { openModal: openConfigModal }] = useModal();
//注册table数据
const { tableContext, onExportXls, onImportXls } = useListPage({
  tableProps: {
    title: '最佳实践-填报',
    api: list,
    columns,
    canResize: false,
    formConfig: {
      //labelWidth: 120,
      schemas: searchFormSchema,
      autoSubmitOnEnter: true,
      showAdvancedButton: true,
      fieldMapToNumber: [
      ],
      fieldMapToTime: [['reportTime', ['reportTime_begin', 'reportTime_end'], 'YYYY-MM-DD']],
    },
    actionColumn: {
      width: 120,
      fixed: 'right'
    },
    beforeFetch: (params) => {
      return Object.assign(params, queryParam);
    },
  },
  exportConfig: {
    name: "最佳实践填报",
    url: getExportUrl,
    params: queryParam,
  },
  importConfig: {
    url: getImportUrl,
    success: handleSuccess
  },
})

const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext

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
 * 详情
*/
function handleDetail(record: Recordable) {
  openModal(true, {
    record,
    isUpdate: true,
    showFooter: false,
  });
}
/**
 * 删除事件
 */
async function handleDelete(record) {
  await deleteOne({ id: record.id }, handleSuccess);
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
      auth: 'practice:mes3_best_practice_fill:edit'
    }
  ]
}
/**
   * 下拉操作栏
   */
function getDropDownAction(record) {
  return [
    {
      label: '详情',
      onClick: handleDetail.bind(null, record),
    }, {
      label: '删除',
      popConfirm: {
        title: '是否确认删除',
        confirm: handleDelete.bind(null, record),
        placement: 'topLeft',
      },
      auth: 'practice:mes3_best_practice_fill:delete'
    }
  ]
}

//评审填报点击事件
function handleResultClick(record, fillType, fillReportId){
  openConfigModal(true, {
    typeId: record.improvementType,
    fillType,
    fillReportId,
    fillId: record.id,
    isFill: true,
    isUpdate: false,
    showFooter: true,
  });
}

function handleTitleClick(record){
  opeRecommendModalModal(true, {
    record,
  });
}
</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}

.projectName{
  max-width: 100%; 
  white-space: pre-wrap;
  word-break: break-all;
  padding: 0;
  text-align: left;
  span{
    width: 100%;
    text-decoration: underline;
    color: #D92622;
    white-space: pre-wrap;
    word-break: break-all;
  }
}
</style>