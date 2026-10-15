<template>
  <div class="wo-page">
    <div class="section">
      <div class="section-title">转酒路径配置</div>
      <!-- 工具条:车间/工艺/编码 + 新增 -->
      <div class="log-toolbar">
        <select v-model="query.workshopCode" class="log-select" @change="search(1)">
          <option value="">全部车间</option>
          <option v-for="ws in workshopList" :key="ws.code" :value="ws.code">{{ ws.name }}</option>
        </select>
        <select v-model="query.processType" class="log-select" @change="search(1)">
          <option value="">全部工艺</option>
          <option v-for="p in ALL_PROCESSES" :key="p" :value="p">{{ p }}</option>
        </select>
        <div class="log-search">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="7" />
            <line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
          <input v-model="query.routeCode" type="text" placeholder="搜索 路径编码" @keyup.enter="search(1)" />
        </div>
        <button class="btn-create" @click="search(1)">查询</button>
        <button class="btn-sort" @click="reset">重置</button>
        <button class="btn-sort" @click="openSkid">卸车撬管理</button>
        <button class="btn-create add-right" @click="handleAdd">＋ 新增配置</button>
        <span class="log-count">共 {{ total }} 条</span>
      </div>
      <!-- 表格(原型 log-table 同款) -->
      <table class="log-table">
        <thead>
          <tr>
            <th>所属车间</th>
            <th>工艺</th>
            <th>源罐</th>
            <th>目标罐</th>
            <th>卸车撬</th>
            <th>路径编码</th>
            <th>路径名称</th>
            <th>排序</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="records.length === 0">
            <td colspan="10" class="empty">暂无数据</td>
          </tr>
          <tr v-for="r in records" :key="r.id">
            <td>{{ workshopName(r.workshopCode) }}</td>
            <td>{{ r.processType }}</td>
            <td>{{ r.sourceTank || '—' }}</td>
            <td>{{ r.targetTank || '—' }}</td>
            <td>{{ r.skidCode || '—' }}</td>
            <td class="log-time">{{ r.routeCode }}</td>
            <td>{{ r.routeName }}</td>
            <td>{{ r.sortNo ?? 0 }}</td>
            <td>
              <span class="log-status">
                <i :style="{ background: r.enabled === 1 ? '#10B981' : '#9CA3AF' }" />{{ r.enabled === 1 ? '启用' : '停用' }}
              </span>
            </td>
            <td>
              <button class="btn btn-edit" @click="handleEdit(r)">编辑</button>
              <button class="btn btn-delete" @click="handleDelete(r)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div class="pager">
        <Pagination
          v-model:current="page"
          :total="total"
          :page-size="pageSize"
          :page-size-options="['10', '50', '80', '100']"
          size="small"
          show-size-changer
          show-quick-jumper
          :show-total="(t) => `共 ${t} 条`"
          @change="onPageChange"
        />
      </div>
    </div>

    <!-- 新增/编辑modal(样式与工单弹窗统一) -->
    <RouteFormModal @register="registerModal" :width="880" @success="search(page)" />
    <!-- 卸车撬管理(字典 MES3_SKID 页内维护) -->
    <SkidManageModal ref="skidModalRef" @change="search(page)" />
  </div>
</template>
<script lang="ts" setup name="maotai-transfer-route">
import { createVNode, nextTick, onMounted, reactive, ref } from 'vue';
import { Pagination } from 'ant-design-vue';
import { Modal } from 'ant-design-vue';
import { ExclamationCircleOutlined } from '@ant-design/icons-vue';
import { useModal } from '/@/components/Modal';
import { useMessage } from '/@/hooks/web/useMessage';
import { getDictItems } from '/@/utils/dict/JDictSelectUtil';
import { deleteRoute, getRoutePage, type TransferRouteItem } from './WorkOrder.api';
import { workshopNameMap } from './WorkOrder.data';
import RouteFormModal from './components/TransferRouteForm.vue';
import SkidManageModal from './components/SkidManageModal.vue';

// 卸车撬管理弹窗
const skidModalRef = ref();
function openSkid() {
  skidModalRef.value?.open();
}

// 分页与设备台账(BasicTable)同款:默认每页10条+条数切换+跳页输入
const pageSize = ref(10);
const ALL_PROCESSES = ['卸车工艺', '装车工艺', '倒罐工艺', '灌装工艺'];

const query = reactive({ workshopCode: '', processType: '', routeCode: '' });
const records = ref<TransferRouteItem[]>([]);
const total = ref(0);
const page = ref(1);
const workshopList = ref<{ code: string; name: string }[]>([
  { code: 'ZZG', name: '中转酒罐车间' }, { code: 'XLG', name: '系列酒勾调厂房' },
  { code: 'CPG', name: '成品酒罐房' }, { code: 'GWG', name: '包装厂房' },
]);

// 新增/编辑modal
const [registerModal, { openModal }] = useModal();
const { createMessage: msg } = useMessage();

function workshopName(code?: string) {
  return workshopList.value.find((w) => w.code === code)?.name || code || '—';
}

async function search(p = 1) {
  page.value = p;
  const res = await getRoutePage({
    pageNo: p,
    pageSize: pageSize.value,
    workshopCode: query.workshopCode || undefined,
    processType: query.processType || undefined,
    routeCode: query.routeCode || undefined,
  });
  records.value = res?.records || [];
  total.value = res?.total || 0;
}
// 换页/换每页条数统一走这里(条数变化时回到第 p 页,antd change 回调带新 pageSize)
function onPageChange(p: number, s: number) {
  pageSize.value = s;
  search(p);
}
function reset() {
  query.workshopCode = '';
  query.processType = '';
  query.routeCode = '';
  search(1);
}

function handleAdd() {
  nextTick(() => openModal(true, { action: 'add' }));
}
function handleEdit(r: TransferRouteItem) {
  openModal(true, { action: 'edit', record: r });
}
function handleDelete(r: TransferRouteItem) {
  Modal.confirm({
    title: `确认删除路径配置 ${r.routeCode}（${r.routeName}）？`,
    icon: createVNode(ExclamationCircleOutlined, { style: { color: '#ef4444' } }),
    okText: '确认',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      await deleteRoute(r.id);
      msg.success({ content: '删除成功' });
      search(page.value);
    },
  });
}

// 车间字典口径
onMounted(async () => {
  try {
    const items: any = await getDictItems("mes3_mdm_dict where group_code='MES3_WORKSHOP' and del_flag=0", 'item_val,item_key');
    const byKey: Record<string, string> = {};
    (items || []).forEach((it: any) => {
      if (it.value) {
        byKey[it.value] = it.text;
        workshopNameMap[it.value] = it.text;
      }
    });
    const order = ['ZZG', 'XLG', 'CPG', 'GWG'];
    const names = order.map((k) => byKey[k]).filter(Boolean);
    if (names.length === 4) workshopList.value = order.map((k, i) => ({ code: k, name: names[i] }));
  } catch (e) {
    console.error('车间字典加载失败', e);
  }
  search(1);
});
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

/* 工具条(与日志页同款) */
.log-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.log-search {
  position: relative;
  flex: 0 0 240px;
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
.add-right {
  margin-left: auto;
}
.log-count {
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
}
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

/* 操作按钮(原型配色) */
.btn {
  border: none;
  border-radius: 3px;
  font-size: 12px;
  padding: 4px 10px;
  color: #fff;
  cursor: pointer;
  margin-right: 6px;
}
.btn-edit {
  background: #5b7a99;
}
.btn-edit:hover {
  background: #4a6580;
}
.btn-delete {
  background: #c0584f;
}
.btn-delete:hover {
  background: #a0463f;
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
