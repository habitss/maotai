<template>
  <div class="wo-page">
    <div class="section">
      <div class="section-title">工单操作日志</div>
      <!-- 工具条(原型:搜索框+状态下拉+计数) -->
      <div class="log-toolbar">
        <div class="log-search">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="7" />
            <line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
          <input
            v-model="query.keyword"
            type="text"
            placeholder="搜索 操作人 / 工单号 / 操作内容"
            @keyup.enter="search(1)"
          />
        </div>
        <select v-model="query.status" class="log-select" @change="search(1)">
          <option value="">全部状态</option>
          <option v-for="(m, k) in STATUS_MAP" :key="k" :value="k">{{ m.text }}</option>
        </select>
        <button class="btn-create" @click="search(1)">查询</button>
        <button class="btn-sort" @click="reset">重置</button>
        <span class="log-count">共 {{ total }} 条</span>
      </div>
      <!-- 表格(原型:浅灰表头+交替行+悬停浅蓝) -->
      <table class="log-table">
        <thead>
          <tr>
            <th>时间</th>
            <th>操作人</th>
            <th>操作类型</th>
            <th>操作对象</th>
            <th>操作内容</th>
            <th>工单状态</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="records.length === 0">
            <td colspan="6" class="empty">暂无数据</td>
          </tr>
          <tr v-for="r in records" :key="r.id">
            <td class="log-time">{{ fmtTime(r.operateTime) }}</td>
            <td class="log-user">{{ r.operator || '—' }}</td>
            <td class="log-action">{{ r.action || '—' }}</td>
            <td><span class="log-target">{{ r.orderNo }}</span></td>
            <td class="log-content">{{ r.content }}</td>
            <td>
              <span class="log-status">
                <i :style="{ background: dotColor(r.orderStatus) }" />{{ statusText(r.orderStatus) }}
              </span>
            </td>
          </tr>
        </tbody>
      </table>
      <div class="pager">
        <Pagination
          v-model:current="page"
          :total="total"
          :page-size="PAGE_SIZE"
          size="small"
          :show-total="(t) => `共 ${t} 条`"
          @change="search"
        />
      </div>
    </div>
  </div>
</template>
<script lang="ts" setup name="maotai-work-order-log">
import { onMounted, reactive, ref } from 'vue';
import { Pagination } from 'ant-design-vue';
import { getOrderLogPage, type WorkOrderLogItem } from './WorkOrder.api';
import { STATUS_MAP } from './WorkOrder.data';

const PAGE_SIZE = 15;

const query = reactive({ keyword: '', status: '' });
const records = ref<WorkOrderLogItem[]>([]);
const total = ref(0);
const page = ref(1);

function fmtTime(t?: string) {
  return t ? String(t).replace('T', ' ').slice(0, 19) : '—';
}
function statusText(s?: string) {
  return STATUS_MAP[s || '']?.text || s || '—';
}
function dotColor(s?: string) {
  return STATUS_MAP[s || '']?.color || '#999';
}

async function search(p = 1) {
  page.value = p;
  const res = await getOrderLogPage({
    pageNo: p,
    pageSize: PAGE_SIZE,
    keyword: query.keyword || undefined,
    status: query.status || undefined,
  });
  records.value = res?.records || [];
  total.value = res?.total || 0;
}
function reset() {
  query.keyword = '';
  query.status = '';
  search(1);
}

onMounted(() => search(1));
</script>

<style lang="less" scoped>
@wo-blue: #003366;

.wo-page {
  padding: 12px;
  background: #fff;
  border: 1px solid #ccc;
  border-radius: 4px;
}
.section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.section-title {
  font-size: 15px;
  font-weight: 700;
  color: @wo-blue;
}

/* 工具条(原型 log-toolbar) */
.log-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
}
.log-search {
  position: relative;
  flex: 0 0 340px;
}
.log-search svg {
  position: absolute;
  left: 9px;
  top: 50%;
  transform: translateY(-50%);
  width: 14px;
  height: 14px;
  color: #888;
  pointer-events: none;
}
.log-search input {
  width: 100%;
  padding: 7px 10px 7px 30px;
  border: 1px solid #ccc;
  border-radius: 3px;
  font-size: 12px;
  font-family: Arial, sans-serif;
  color: #222;
}
.log-search input:focus {
  outline: none;
  border-color: #66afe9;
  box-shadow: 0 0 0 2px rgba(102, 175, 233, 0.25);
}
.log-select {
  padding: 7px 10px;
  border: 1px solid #ccc;
  border-radius: 3px;
  font-size: 12px;
  font-family: Arial, sans-serif;
  background: #fff;
  color: #222;
}
.btn-create {
  background: @wo-blue;
  color: #fff;
  border: none;
  border-radius: 3px;
  padding: 7px 14px;
  font-size: 12px;
  cursor: pointer;
}
.btn-create:hover {
  background: #00264d;
}
.btn-sort {
  background: #fff;
  border: 1px solid #ccc;
  border-radius: 3px;
  padding: 7px 12px;
  font-size: 12px;
  color: #333;
  cursor: pointer;
}
.btn-sort:hover {
  border-color: #66afe9;
  color: @wo-blue;
}
.log-count {
  margin-left: auto;
  font-size: 12px;
  color: #888;
}

/* 表格(原型 log-table Brewmaxx 层) */
.log-table {
  width: 100%;
  border-collapse: collapse;
}
.log-table th {
  text-align: left;
  padding: 7px 12px;
  background: #f0f0f0;
  color: #333;
  border-bottom: 1px solid #ccc;
  font-size: 12px;
  font-weight: 600;
}
.log-table td {
  padding: 8px 12px;
  border-bottom: 1px solid #eee;
  font-size: 12px;
  color: #111827;
}
.log-table tr:nth-child(even) td {
  background: #f9f9f9;
}
.log-table tr:hover td {
  background: #e6f2ff;
}
.log-time {
  font-family: 'Consolas', 'Courier New', monospace;
  font-size: 12px;
  color: #6b7280;
  white-space: nowrap;
}
.log-user {
  font-weight: 500;
}
.log-action {
  color: #4b5563;
}
.log-target {
  font-family: 'Consolas', 'Courier New', monospace;
  font-size: 12px;
  background: #f3f4f6;
  padding: 2px 6px;
  border-radius: 4px;
}
.log-content {
  color: #111827;
}
/* 状态:色点+文字 */
.log-status {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #333;
}
.log-status i {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
}
.empty {
  text-align: center;
  padding: 40px 0;
  color: #9ca3af;
}
.pager {
  display: flex;
  justify-content: flex-end;
}
</style>
