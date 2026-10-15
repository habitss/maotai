<template>
  <div class="p-1">
    <!-- 顶部统计卡片 -->
<!--    <div class="stat-cards">-->
<!--      <div class="stat-card sc-green">-->
<!--        <div class="sc-body">-->
<!--          <div class="sc-label">本月已完成</div>-->
<!--          <div class="sc-value">{{ stat.monthCompleted ?? 0 }}<span class="sc-unit">次</span></div>-->
<!--          <div class="sc-sub">保养任务</div>-->
<!--        </div>-->
<!--      </div>-->
<!--      <div class="stat-card sc-blue">-->
<!--        <div class="sc-body">-->
<!--          <div class="sc-label">计划待执行</div>-->
<!--          <div class="sc-value">{{ stat.pending ?? 0 }}<span class="sc-unit">次</span></div>-->
<!--          <div class="sc-sub">需完成</div>-->
<!--        </div>-->
<!--      </div>-->
<!--      <div class="stat-card sc-rose">-->
<!--        <div class="sc-body">-->
<!--          <div class="sc-label">逾期未完成</div>-->
<!--          <div class="sc-value">{{ stat.overdue ?? 0 }}<span class="sc-unit">次</span></div>-->
<!--          <div class="sc-sub">需立即跟进</div>-->
<!--        </div>-->
<!--      </div>-->
<!--      <div class="stat-card sc-amber">-->
<!--        <div class="sc-body">-->
<!--          <div class="sc-label">保养覆盖率</div>-->
<!--          <div class="sc-value">{{ stat.coverageRate ?? 0 }}<span class="sc-unit">%</span></div>-->
<!--          <div class="sc-sub">设备覆盖</div>-->
<!--        </div>-->
<!--      </div>-->
<!--    </div>-->

    <BasicTable @register="registerTable">
      <template #tableTitle>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleAdd">新建工单</a-button>
        <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls">导出</a-button>
        <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="onImportXls">导入</j-upload-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined" />
                批量删除
              </a-menu-item>
            </a-menu>
          </template>
          <a-button>批量操作 <Icon icon="mdi:chevron-down" /></a-button>
        </a-dropdown>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>

    <FormModal @register="registerActionModal" :width="880" @success="onModalSuccess" />
    <DetailModal @register="registerDetailModal" :width="880" />
  </div>
</template>
<script lang="ts" name="maotai-equipment-maint" setup>
import { reactive, onMounted } from 'vue';
import { BasicTable, TableAction, EditRecordRow } from '/@/components/Table';
import { Icon } from '/@/components/Icon';
import { list, deleteOne, batchDelete, getImportUrl, getExportUrl, getStatistics, saveOrUpdate } from './MaotaiEquipmentMaint.api';
import { columns, searchFormSchema } from './MaotaiEquipmentMaint.data';
import { useListPage } from '/@/hooks/system/useListPage';
import { useMessage } from '/@/hooks/web/useMessage';
import { useModal } from '/@/components/Modal';
import { nextTick } from 'vue';
import FormModal from './components/MaotaiEquipmentMaintForm.vue';
import DetailModal from './components/MaotaiEquipmentMaintDetail.vue';

const { createConfirm } = useMessage();

const [registerActionModal, { openModal }] = useModal();
const [registerDetailModal, { openModal: openDetailModal }] = useModal();

const { tableContext, onExportXls, onImportXls } = useListPage({
  tableProps: {
    title: '设备保养记录',
    api: list,
    columns,
    showActionColumn: true,
    actionColumn: { width: 150 },
    rowSelection: { columnWidth: 20 },
    formConfig: {
      schemas: searchFormSchema,
      // labelWidth 而非 labelCol.style.width(后者会让 wrapper 拿到无效宽度,
      // 普通输入框收缩、RangePicker 按默认宽,控件各宽不一)
      labelWidth: 65,
      showAdvancedButton: false,
      fieldMapToTime: [['planDate', ['planDate_begin', 'planDate_end'], 'YYYY-MM-DD']],
    },
  },
  exportConfig: { name: '设备保养记录', url: getExportUrl },
  importConfig: { url: getImportUrl, success: handleSuccess },
});

const [registerTable, { reload }, { selectedRowKeys }] = tableContext;
const { createMessage: msg } = useMessage();

// 顶部统计
const stat = reactive({ monthCompleted: 0, pending: 0, overdue: 0, coverageRate: 0 });
function loadStat() {
  getStatistics()
    .then((res: any) => Object.assign(stat, res || {}))
    .catch(() => {});
}

function getTableAction(record) {
  const actions = [];
  // 确认放第一位(已完成的工单不可再确认,不显示)
  if (record.status !== 'completed') {
    actions.push({ label: '确认', onClick: handleConfirm.bind(null, record) });
  }
  actions.push(
    { label: '查看', onClick: handleDetail.bind(null, record) },
    { label: '编辑', onClick: handleEdit.bind(null, record) },
    // 删除(从原"更多"下拉拿出,直接平铺)
    {
      label: '删除',
      popConfirm: { title: '确认删除该保养工单?', confirm: handleDelete.bind(null, record), placement: 'left' },
    },
  );
  return actions;
}
/** 确认完成:二次确认后把工单状态改为 completed;已完成的工单不可再触发 */
function handleConfirm(record: EditRecordRow) {
  if (record.status === 'completed') {
    msg.warning({ content: '该工单已完成' });
    return;
  }
  createConfirm({
    iconType: 'warning',
    title: '确认完成',
    content: '确认将该保养工单标记为已完成?',
    onOk: async () => {
      try {
        await saveOrUpdate({ id: record.id, status: 'completed' }, true);
        msg.success({ content: '工单已完成' });
        reload();
        loadStat();
      } catch (error) {
        msg.error({ content: '操作失败' });
      }
    },
  });
}
function handleAdd() {
  nextTick(() => openModal(true, { action: 'add' }));
}
function handleEdit(record: EditRecordRow) {
  openModal(true, { action: 'edit', record });
}
function handleDetail(record: EditRecordRow) {
  openDetailModal(true, { record });
}
async function handleDelete(record: EditRecordRow) {
  try {
    await deleteOne({ id: record.id }, handleSuccess);
    reload();
  } catch (error) {
    msg.error({ content: '操作失败' });
  }
}
async function batchHandleDelete() {
  await batchDelete({ ids: selectedRowKeys.value }, handleSuccess);
}
function handleSuccess() {
  (selectedRowKeys.value = []) && reload();
}
// 弹窗成功(新增/编辑/详情)后刷新列表 + 统计
function onModalSuccess() {
  reload();
  loadStat();
}

onMounted(() => {
  loadStat();
});
</script>

<style lang="less" scoped>
.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 12px;
}
.stat-card {
  position: relative;
  padding: 14px 18px;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #f0f0f0;
  overflow: hidden;
}
.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
}
.sc-green::before { background: #10b981; }
.sc-blue::before { background: #3b82f6; }
.sc-rose::before { background: #f43f5e; }
.sc-amber::before { background: #f59e0b; }
.sc-body { padding-left: 8px; }
.sc-label { font-size: 12px; color: #a8a29e; font-weight: 600; margin-bottom: 6px; }
.sc-value { font-size: 26px; font-weight: 800; line-height: 1; }
.sc-unit { font-size: 12px; font-weight: 400; color: #a8a29e; margin-left: 2px; }
.sc-sub { font-size: 11px; color: #a8a29e; margin-top: 6px; }

:deep(.ant-input-number) { width: 100%; }
/* 范围日期选择器:width100% 外还要 min-width:0,否则 antd 默认最小宽撑出列宽(比设备名称框宽 40px) */
:deep(.ant-picker) { width: 100%; min-width: 0; }
:deep(.ant-select-selection-placeholder) { color: #bebebe !important; }
:deep(.ant-form) {
  padding-left: 0;        /* 搜表格搜索区容器左内边距,默认有 8~12px */
}
</style>
