<template>
  <a-modal :open="visible" title="卸车撬管理" :width="960" @cancel="close" :footer="null">
    <div class="form-pad">
      <!-- 工具条 -->
      <div class="skid-toolbar">
        <button class="btn-create" @click="startAdd">＋ 新增撬</button>
        <span class="skid-count">共 {{ records.length }} 项(工单路径配置的撬下拉即此清单)</span>
      </div>
      <!-- 编辑/新增表单 -->
      <div v-if="editing" class="skid-form">
        <input v-model="form.itemKey" class="skid-input" placeholder="编码,如 HP-01" style="flex: 0 0 140px" />
        <input v-model="form.itemVal" class="skid-input" placeholder="名称,如 1号卸车撬" style="flex: 1" />
        <a-input-number v-model:value="form.sort" :min="0" size="small" style="width: 90px" placeholder="排序" />
        <button class="btn btn-save" :disabled="saving" @click="save">{{ form.id ? '保存' : '添加' }}</button>
        <button class="btn btn-cancel" @click="editing = false">取消</button>
      </div>
    <!-- 列表 -->
    <table class="log-table">
      <thead>
        <tr>
          <th>编码</th>
          <th>名称</th>
          <th style="width: 70px">排序</th>
          <th style="width: 130px">操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="records.length === 0">
          <td colspan="4" class="empty">暂无数据</td>
        </tr>
        <tr v-for="r in pagedRecords" :key="r.id">
          <td class="log-time">{{ r.itemKey }}</td>
          <td>{{ r.itemVal }}</td>
          <td>{{ r.sort ?? 0 }}</td>
          <td>
            <button class="btn btn-edit" @click="startEdit(r)">编辑</button>
            <button class="btn btn-delete" @click="handleDelete(r)">删除</button>
          </td>
        </tr>
      </tbody>
    </table>
      <div class="pager">
        <Pagination
          v-model:current="page"
          :total="records.length"
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
  </a-modal>
</template>
<script lang="ts" setup>
import { computed, reactive, ref } from 'vue';
import { Pagination } from 'ant-design-vue';
import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';

// 字典 MES3_SKID(卸车撬)的页内维护:工单路径配置的撬下拉数据源
const GROUP = 'MES3_SKID';
const Api = {
  list: '/mes3_mdm/dict/queryPageList',
  save: '/mes3_mdm/dict',
  edit: '/mes3_mdm/dict',
  delete: '/mes3_mdm/dict',
};

const visible = ref(false);
const records = ref<any[]>([]);
// 前端分页(撬数量小,全量拉取本地分页);分页样式与设备台账同款:默认10条+条数切换+跳页
const pageSize = ref(10);
const page = ref(1);
const pagedRecords = computed(() => records.value.slice((page.value - 1) * pageSize.value, page.value * pageSize.value));
function onPageChange(_p: number, s: number) {
  pageSize.value = s;
}
const editing = ref(false);
const saving = ref(false);
const form = reactive({ id: '', itemKey: '', itemVal: '', sort: 0 });

const { createMessage: msg } = useMessage();
const emit = defineEmits(['change']);

async function load() {
  const res = await defHttp.get({ url: Api.list, params: { groupCode: GROUP, pageNo: 1, pageSize: 100 } });
  records.value = res?.records || res || [];
}
function startAdd() {
  form.id = '';
  form.itemKey = '';
  form.itemVal = '';
  form.sort = records.value.length + 1;
  editing.value = true;
}
function startEdit(r: any) {
  form.id = r.id;
  form.itemKey = r.itemKey;
  form.itemVal = r.itemVal;
  form.sort = r.sort ?? 0;
  editing.value = true;
}
async function save() {
  if (!form.itemKey || !form.itemVal) {
    msg.warning({ content: '请填写编码和名称' });
    return;
  }
  saving.value = true;
  try {
    const payload = { id: form.id || undefined, groupCode: GROUP, itemKey: form.itemKey, itemVal: form.itemVal, sort: form.sort ?? 0, status: 1 };
    if (form.id) {
      await defHttp.put({ url: Api.edit, params: payload });
    } else {
      await defHttp.post({ url: Api.save, params: payload });
    }
    msg.success({ content: '保存成功' });
    editing.value = false;
    await load();
    emit('change');
  } finally {
    saving.value = false;
  }
}
async function handleDelete(r: any) {
  await defHttp.delete({ url: Api.delete + `/${r.id}` }, { joinParamsToUrl: true });
  msg.success({ content: '删除成功' });
  await load();
  emit('change');
}
function open() {
  visible.value = true;
  editing.value = false;
  page.value = 1;
  load();
}
function close() {
  visible.value = false;
}
defineExpose({ open });
</script>

<style scoped lang="less">
@wo-blue: #003366;

/** 与创建工单表单一致:内容左右间距 24px(jeecg a-modal body 默认 padding 被全局清零);底部留出间距防贴边 */
.form-pad {
  padding: 4px 24px 20px;
}

.skid-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
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
.skid-count {
  font-size: 12px;
  color: #888;
}
.skid-form {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 12px;
  padding: 10px;
  background: #f5f8fc;
  border: 1px solid #cce0f5;
  border-radius: 3px;
}
.skid-input {
  padding: 6px 8px;
  border: 1px solid #ccc;
  border-radius: 3px;
  font-size: 12px;
  color: #222;
}
.skid-input:focus {
  outline: none;
  border-color: #66afe9;
}
/* 表格(与转酒路径配置页同款) */
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
  padding: 7px 12px;
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
.btn {
  border: none;
  border-radius: 3px;
  font-size: 12px;
  padding: 4px 10px;
  color: #fff;
  cursor: pointer;
  margin-right: 6px;
}
.btn-save {
  background: #3f9d6b;
}
.btn-cancel {
  background: #6b7280;
}
.btn-edit {
  background: #5b7a99;
}
.btn-delete {
  background: #c0584f;
}
.empty {
  text-align: center;
  padding: 30px 0;
  color: #9ca3af;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
</style>
