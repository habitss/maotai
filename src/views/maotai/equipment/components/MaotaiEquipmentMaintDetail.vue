<template>
  <BasicModal @register="register" title="保养工单详情" :width="880" :showOkBtn="false" cancelText="关闭">
    <a-form v-if="record" :label-col="{ style: { width: '110px' } }" :wrapper-col="{ style: { width: 'calc(100% - 110px)' } }" class="maint-detail">
      <a-row :gutter="[12, 0]">
        <a-col :span="12"><a-form-item label="工单号"><span class="dv mono">{{ record.orderNo || '—' }}</span></a-form-item></a-col>
        <a-col :span="12"><a-form-item label="所属设备"><span class="dv">{{ record.equipmentName || '—' }}</span></a-form-item></a-col>
        <a-col :span="12"><a-form-item label="保养类型"><span class="dv">{{ record.maintType_dictText || record.maintType || '—' }}</span></a-form-item></a-col>
        <a-col :span="12"><a-form-item label="优先级"><span class="dv">{{ record.priority_dictText || record.priority || '—' }}</span></a-form-item></a-col>
        <a-col :span="12"><a-form-item label="计划日期"><span class="dv">{{ record.planDate || '—' }}</span></a-form-item></a-col>
        <a-col :span="12"><a-form-item label="状态">
          <span class="status-pill" :style="statusStyle">{{ statusText }}</span>
        </a-form-item></a-col>
        <a-col :span="12"><a-form-item label="完成日期"><span class="dv" :class="{ warn: !record.completeDate }">{{ record.completeDate || '待完成' }}</span></a-form-item></a-col>
        <a-col :span="12"><a-form-item label="执行人"><span class="dv">{{ record.executor || '—' }}</span></a-form-item></a-col>
        <a-col :span="12"><a-form-item label="预估工时"><span class="dv">{{ record.estWorkhour || '—' }}</span></a-form-item></a-col>
        <a-col :span="12"><a-form-item label="实际工时"><span class="dv">{{ record.actualWorkhour || '—' }}</span></a-form-item></a-col>
        <a-col :span="12"><a-form-item label="费用"><span class="dv">{{ record.cost || '—' }}</span></a-form-item></a-col>
        <a-col :span="12"><a-form-item label="所需备件">
          <span class="material-tag" v-for="(m, i) in spareParts" :key="i">{{ m }}</span>
          <span v-if="!spareParts.length" class="dv">—</span>
        </a-form-item></a-col>
        <a-col :span="24"><a-form-item label="保养内容"><span class="dv block">{{ record.maintContent || '—' }}</span></a-form-item></a-col>
        <a-col :span="24">
          <a-form-item label="保养内容清单">
            <ul class="check-list" v-if="checklist.length">
              <li v-for="(it, idx) in checklist" :key="idx"><span class="ci">{{ idx + 1 }}</span><span>{{ it }}</span></li>
            </ul>
            <span v-else class="dv">—</span>
          </a-form-item>
        </a-col>
        <a-col :span="24"><a-form-item label="备注"><span class="dv block">{{ record.remark || '—' }}</span></a-form-item></a-col>
      </a-row>
    </a-form>
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref, computed } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';

const record = ref<any>({});

const [register] = useModalInner((data: any) => {
  record.value = data.record || {};
});

// 状态(贴原型 status 颜色)
const STATUS_META: Record<string, { text: string; color: string; bg: string }> = {
  pending: { text: '待执行', color: '#F59E0B', bg: '#FFFBEB' },
  progress: { text: '进行中', color: '#3B82F6', bg: '#EFF6FF' },
  completed: { text: '已完成', color: '#10B981', bg: '#ECFDF5' },
  overdue: { text: '逾期', color: '#F43F5E', bg: '#FEF2F2' },
};
const statusMeta = computed(() => STATUS_META[record.value.status] || { text: record.value.status || '—', color: '#999', bg: '#F5F5F4' });
const statusText = computed(() => statusMeta.value.text);
const statusStyle = computed(() => ({ color: statusMeta.value.color, background: statusMeta.value.bg }));

// 保养内容清单:兼容 "| 分隔文本"(当前)/ JSON 数组(旧)/ JSON [{item,done}](旧)
const checklist = computed<string[]>(() => {
  const raw = record.value.checklist;
  if (!raw) return [];
  const s = String(raw).trim();
  if (s.startsWith('[')) {
    try {
      const arr = JSON.parse(s);
      return (Array.isArray(arr) ? arr : [])
        .map((it: any) => (typeof it === 'string' ? it : it && it.item ? it.item : ''))
        .filter(Boolean);
    } catch (e) {
      // fallthrough
    }
  }
  return s.split('|').map((x: string) => x.trim()).filter(Boolean);
});

// 所需备件:逗号分隔 → 数组
const spareParts = computed<string[]>(() => {
  const s = record.value.spareParts || '';
  return s.split(/[,，]/).map((x: string) => x.trim()).filter(Boolean);
});
</script>

<style lang="less" scoped>
/* 值文本:与编辑表单输入框同占位,贴输入框内边距,视觉与表单字段等宽 */
.dv {
  display: inline-block;
  min-height: 32px;
  line-height: 30px;
  padding: 0 11px;
  color: rgba(0, 0, 0, 0.88);
  background: #fafafa;
  border: 1px solid #f0f0f0;
  border-radius: 2px;
  width: 100%;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.dv.warn { color: #f59e0b; }
.dv.block { white-space: pre-wrap; height: auto; min-height: 54px; line-height: 22px; padding-top: 5px; }
.mono { font-family: Consolas, Menlo, monospace; }

.status-pill { display: inline-block; padding: 2px 10px; border-radius: 12px; font-weight: 600; font-size: 13px; }

.check-list { list-style: none; padding: 0; margin: 0; width: 100%; }
.check-list li { display: flex; align-items: center; gap: 10px; padding: 6px 0; border-bottom: 1px solid #f5f5f5; font-size: 13px; color: #555; }
.check-list li:last-child { border-bottom: none; }
.ci { width: 20px; height: 20px; border-radius: 4px; background: #D1FAE5; color: #059669; font-size: 11px; font-weight: 700; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }

.material-tag { display: inline-block; padding: 2px 8px; margin: 2px 4px 2px 0; border-radius: 4px; font-size: 12px; font-weight: 600; background: #fafafa; border: 1px solid #f0f0f0; color: #555; }
</style>
