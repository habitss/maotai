<template>
  <BasicModal @register="registerModal" @ok="handleSubmit" v-bind="$attrs" wrapClassName="wo-form-modal" title="批量新增工单" :width="1060">
    <div class="form-pad">
      <BasicForm @register="registerForm" @submit="handleSubmit" />
      <!-- 明细行:源罐+目标罐+转酒路径+数量,逐行生成工单;罐选项随车间,路径随罐联动(配置表候选,必选) -->
      <div class="batch-rows">
        <div class="batch-rows-title">工单明细</div>
        <a-table :data-source="rows" :columns="rowColumns" :pagination="false" size="small" rowKey="_key" bordered>
          <template #bodyCell="{ column, index }">
            <template v-if="column.key === 'seq'">{{ index + 1 }}</template>
            <template v-else-if="column.key === 'sourceTank'">
              <a-select
                v-model:value="rows[index].sourceTank"
                :options="tankOptions"
                :get-popup-container="popupBody"
                show-search
                option-filter-prop="label"
                placeholder="请选择源罐"
                size="small"
                style="width: 100%"
                @change="refreshRowPaths(index)"
              />
            </template>
            <template v-else-if="column.key === 'targetTank'">
              <a-select
                v-model:value="rows[index].targetTank"
                :options="tankOptions"
                :get-popup-container="popupBody"
                show-search
                option-filter-prop="label"
                placeholder="请选择目标罐"
                size="small"
                style="width: 100%"
                @change="refreshRowPaths(index)"
              />
            </template>
            <template v-else-if="column.key === 'transferPath'">
              <a-select
                v-model:value="rows[index].transferPath"
                :options="rows[index].pathOptions"
                :get-popup-container="popupBody"
                placeholder="请选择路径"
                size="small"
                style="width: 100%"
              />
            </template>
            <template v-else-if="column.key === 'amount'">
              <a-input-number v-model:value="rows[index].amount" :min="0" size="small" style="width: 100%" placeholder="数量" />
            </template>
            <template v-else-if="column.key === 'action'">
              <a-button type="link" danger size="small" :disabled="rows.length <= 1" @click="rows.splice(index, 1)">删除</a-button>
            </template>
          </template>
        </a-table>
        <a-button dashed block class="add-row-btn" @click="addRow">+ 添加明细</a-button>
      </div>
    </div>
  </BasicModal>
</template>
<script lang="ts" setup>
import { computed, ref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { useForm, BasicForm } from '/@/components/Form';
import { useMessage } from '/@/hooks/web/useMessage';
import { batchAddOrder, getPaths, getTanks, type TransferPath } from '../WorkOrder.api';
import { buildBatchSchemas } from '../WorkOrder.data';

// 声明Emits
const emit = defineEmits(['success']);

const { createMessage: msg } = useMessage();

// 弹窗内下拉挂 body:滚动容器定位上下文会带偏面板
const popupBody = () => document.body;

interface BatchRow {
  _key: number;
  sourceTank?: string;
  targetTank?: string;
  transferPath?: string;
  amount?: number;
  pathOptions: { label: string; value: string }[];
}
let keySeed = 1;
// 初始默认一行(useModalInner 回调在 openModal 未传 data 时不触发,初始值兜底)
const rows = ref<BatchRow[]>([]);

// 车间罐清单(设备台账,车间联动)
const tankOptions = ref<{ label: string; value: string }[]>([]);

// 按工艺显隐明细列:卸车无源罐、装车无目标罐
const isUnload = computed(() => (getFieldsValue() as Recordable)?.processName === '卸车工艺');
const isLoad = computed(() => (getFieldsValue() as Recordable)?.processName === '装车工艺');

const rowColumns = computed(() => {
  const cols: any[] = [{ title: '序号', key: 'seq', width: 55, align: 'center' }];
  if (isUnload.value) {
    // 卸车:目标罐+路径(源端=卸车撬,含在路径里)
    cols.push({ title: '目标罐', key: 'targetTank' }, { title: '转酒路径', key: 'transferPath', width: 190 });
  } else if (isLoad.value) {
    // 装车:源罐+路径(目标端=卸车撬)
    cols.push({ title: '源罐', key: 'sourceTank' }, { title: '转酒路径', key: 'transferPath', width: 190 });
  } else {
    // 倒罐/灌装/未选:双罐+路径
    cols.push(
      { title: '源罐', key: 'sourceTank' },
      { title: '目标罐', key: 'targetTank' },
      { title: '转酒路径', key: 'transferPath', width: 190 },
    );
  }
  cols.push({ title: '数量(吨)', key: 'amount', width: 110 }, { title: '操作', key: 'action', width: 70, align: 'center' });
  return cols;
});

function addRow() {
  rows.value.push({ _key: keySeed++, sourceTank: '', targetTank: '', transferPath: '', amount: 0, pathOptions: [] });
}

// 行内罐变化→拉该行路径候选(清空已选;不自动选中,必选)
async function refreshRowPaths(index: number) {
  const row = rows.value[index];
  row.pathOptions = [];
  row.transferPath = '';
  const values = getFieldsValue() as Recordable;
  if (!values?.workshopCode || !values?.processName) return;
  const p = values.processName as string;
  if (p !== '卸车工艺' && p !== '装车工艺' && (!row.sourceTank || !row.targetTank)) return;
  if (p === '卸车工艺' && !row.targetTank) return;
  if (p === '装车工艺' && !row.sourceTank) return;
  try {
    const list: TransferPath[] = (await getPaths({
      workshopCode: values.workshopCode,
      processName: p,
      sourceTank: row.sourceTank,
      targetTank: row.targetTank,
    })) || [];
    row.pathOptions = list.map((x) => ({ label: x.name, value: x.code }));
  } catch (e) {
    row.pathOptions = [];
  }
}

async function loadTanks(workshopCode?: string) {
  tankOptions.value = [];
  if (!workshopCode) return;
  try {
    tankOptions.value = (await getTanks(workshopCode)) || [];
  } catch (e) {
    console.error('罐清单加载失败', e);
  }
}

const schemas = buildBatchSchemas({
  onProcessChange: () => {
    // 工艺切换:明细行的罐/路径失效,清空重选
    rows.value.forEach((r) => {
      r.pathOptions = [];
      r.transferPath = '';
    });
  },
});

// 注册modal
const [registerModal, { closeModal }] = useModalInner(async (data: any) => {
  rows.value = [];
  addRow();
  resetFields();
  // 车间字段联动罐清单(覆盖 schema 静态 componentProps 注入 onChange)
  updateSchema({
    field: 'workshopCode',
    componentProps: {
      onChange: async (val: string) => {
        rows.value.forEach((r) => {
          r.sourceTank = '';
          r.targetTank = '';
          r.pathOptions = [];
          r.transferPath = '';
        });
        await loadTanks(val);
      },
    },
  });
});

/**
 * BasicForm绑定注册(公共字段)
 */
const [registerForm, { resetFields, validate, getFieldsValue, updateSchema }] = useForm({
  schemas,
  autoSubmitOnEnter: true,
  showActionButtonGroup: false,
  baseColProps: { span: 12 },
  labelWidth: 110,
  wrapperCol: { xs: 24, sm: 24, md: 24, lg: 24, xl: 24, xxl: 24 },
  rowProps: { gutter: [12, 0] },
});

// 表单提交事件
async function handleSubmit() {
  try {
    const values = await validate();
    const p = values.processName as string;
    for (let i = 0; i < rows.value.length; i++) {
      const r = rows.value[i];
      if (p !== '卸车工艺' && !r.sourceTank) {
        msg.warning({ content: `第 ${i + 1} 行明细请选择源罐` });
        return;
      }
      if (p !== '装车工艺' && !r.targetTank) {
        msg.warning({ content: `第 ${i + 1} 行明细请选择目标罐` });
        return;
      }
      if (!r.transferPath) {
        msg.warning({ content: `第 ${i + 1} 行明细请选择转酒路径` });
        return;
      }
    }
    const created = await batchAddOrder({ common: values, rows: rows.value });
    msg.success({ content: `批量创建 ${created.length} 条工单` });
    closeModal();
    resetFields();
    emit('success');
  } catch (error) {
    // 校验失败或接口报错,defHttp 已统一提示
  }
}
</script>

<style scoped lang="less">
/** 表单内容与弹窗左右边缘留出间距 */
.form-pad {
  padding: 4px 24px 0;
}
/** 行距压缩:公共字段+明细表格在屏内放完 */
:deep(.ant-form-item) {
  margin-bottom: 10px;
}
/** 时间和数字输入框样式 */
:deep(.ant-input-number) {
  width: 100%;
}
:deep(.ant-picker) {
  width: 100%;
}
:deep(.ant-select-selection-placeholder) {
  color: #bebebe !important;
}
.batch-rows {
  margin-top: 4px;
}
.batch-rows-title {
  font-weight: 600;
  margin-bottom: 8px;
}
.add-row-btn {
  margin-top: 8px;
}
</style>
<style lang="less">
/** 同 WorkOrderForm:弹窗上提+解除 ModalWrapper 内联 max-height 限制 */
.wo-form-modal .ant-modal {
  top: 30px;
}
.wo-form-modal .ant-modal-body .scrollbar__view > div[style*="max-height"] {
  max-height: none !important;
}
</style>
