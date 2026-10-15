<template>
  <div class="maotai-equipment-detail">
    <div class="detail-toolbar">
      <a-button @click="goBack"><Icon icon="ant-design:arrow-left-outlined" /> 返回列表</a-button>
    </div>

    <a-spin :spinning="loading">
      <!-- 区块1: 设备概览头部 -->
      <a-card class="block" :bordered="false">
        <div class="eq-header">
          <div class="eq-icon"><Icon icon="ant-design:tool-outlined" :size="28" /></div>
          <div class="eq-info">
            <h2>{{ equipment.equipName || '—' }}</h2>
            <div class="eq-meta">
              <span>{{ equipment.funlocCode }}</span>
              <span>{{ workshopName }}</span>
              <span>{{ equipment.equipModel }}</span>
              <span class="status-tag" :style="{ color: statusMap.color, background: statusMap.bg }">{{ statusMap.text }}</span>
            </div>
          </div>
        </div>
      </a-card>

      <!-- 区块2: 基本信息 + 二维码 -->
      <a-card class="block" title="基本信息" :bordered="false">
        <div class="info-qr-row">
          <a-descriptions :column="3" size="small" bordered>
            <a-descriptions-item label="功能位置编码">{{ equipment.funlocCode }}</a-descriptions-item>
            <a-descriptions-item label="设备名称">{{ equipment.equipName }}</a-descriptions-item>
            <a-descriptions-item label="所属车间">{{ workshopName }}</a-descriptions-item>
            <a-descriptions-item label="设备型号">{{ equipment.equipModel }}</a-descriptions-item>
            <a-descriptions-item label="制造商">{{ equipment.manufacturer }}</a-descriptions-item>
            <a-descriptions-item label="安装位置">{{ equipment.installLocation }}</a-descriptions-item>
            <a-descriptions-item label="安装日期">{{ equipment.installDate }}</a-descriptions-item>
            <a-descriptions-item label="投运日期">{{ equipment.commissionDate }}</a-descriptions-item>
            <a-descriptions-item label="额定功率">{{ equipment.ratedPower }}</a-descriptions-item>
            <a-descriptions-item label="设计寿命">{{ equipment.designLife }}</a-descriptions-item>
            <a-descriptions-item label="保养周期">{{ maintCycleName }}</a-descriptions-item>
            <a-descriptions-item label="运行状态">{{ statusMap.text }}</a-descriptions-item>
            <a-descriptions-item label="联系电话">{{ equipment.contactPhone }}</a-descriptions-item>
            <a-descriptions-item label="最近保养">{{ equipment.lastMaintDate }}</a-descriptions-item>
            <a-descriptions-item label="备注" :span="3">{{ equipment.remark }}</a-descriptions-item>
          </a-descriptions>
          <div class="qr-card">
            <canvas ref="qrCanvasRef" class="qr-canvas"></canvas>
            <div class="qr-label">{{ equipment.funlocCode }}</div>
            <div class="qr-hint">扫码查看设备信息</div>
            <div class="qr-actions">
              <a-button size="small" @click="downloadQR">下载二维码</a-button>
            </div>
          </div>
        </div>
      </a-card>

      <!-- 区块3: SOP / SWI 操作手册 -->
      <a-card class="block" :bordered="false">
        <template #title>
          <span>SOP / SWI 操作手册</span>
        </template>
        <template #extra>
          <a-button type="primary" preIcon="ant-design:upload-outlined" @click="openUploadModal">上传手册</a-button>
        </template>
        <a-table
          :columns="docColumns"
          :dataSource="docList"
          :pagination="false"
          rowKey="id"
          size="small"
          :loading="docLoading"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.dataIndex === 'docType'">
              <span class="doc-badge" :style="badgeStyle(record.docType)">{{ record.docType_dictText || record.docType }}</span>
            </template>
            <template v-else-if="column.dataIndex === 'action'">
              <a-button type="link" size="small" @click="editDoc(record)">编辑</a-button>
              <a-button type="link" size="small" @click="handleDownload(record)" :disabled="!record.fileUrl">下载</a-button>
              <a-popconfirm title="确认删除该手册?" @confirm="handleDeleteDoc(record)">
                <a-button type="link" size="small" danger>删除</a-button>
              </a-popconfirm>
            </template>
          </template>
        </a-table>
      </a-card>
    </a-spin>

    <!-- 上传/编辑手册弹窗 -->
    <a-modal
      v-model:open="uploadVisible"
      :title="uploadTitle"
      :confirm-loading="uploading"
      width="640px"
      @ok="submitUpload"
    >
      <a-form :label-col="{ span: 5 }" :wrapper-col="{ span: 17 }">
        <a-form-item label="手册文件" :required="!isEdit">
          <a-upload
            :before-upload="beforeUpload"
            :max-count="1"
            :file-list="fileList"
            @remove="handleRemoveFile"
            accept=".pdf,.doc,.docx,.xls,.xlsx,.jpg,.jpeg,.png"
          >
            <a-button preIcon="ant-design:upload-outlined">{{ isEdit ? '重新选择文件(可选)' : '选择文件(pdf/doc/xls/图片,≤50MB)' }}</a-button>
          </a-upload>
          <div class="upload-tip" v-if="form.fileName">当前文件:{{ form.fileName }}</div>
        </a-form-item>
        <a-form-item label="文档类型" required>
          <JDictSelectTag v-model:value="form.docType" dictCode="mes3_mdm_dict where group_code='MES3_EQUIP_DOC_TYPE' and del_flag=0, item_val, item_key" placeholder="请选择类型" />
        </a-form-item>
        <a-form-item label="文档标题" required>
          <a-input v-model:value="form.docTitle" placeholder="请输入文档标题" />
        </a-form-item>
        <a-form-item label="文档编码">
          <a-input v-model:value="form.docCode" placeholder="如 SOP-YS-4500-001" />
        </a-form-item>
        <a-form-item label="版本">
          <a-input v-model:value="form.version" placeholder="如 V3.2" />
        </a-form-item>
        <a-form-item label="编制人">
          <a-input v-model:value="form.author" placeholder="请输入编制人" />
        </a-form-item>
        <a-form-item label="审批人">
          <a-input v-model:value="form.approver" placeholder="请输入审批人" />
        </a-form-item>
        <a-form-item label="发布日期">
          <a-date-picker v-model:value="form.publishDate" valueFormat="YYYY-MM-DD" style="width:100%" />
        </a-form-item>
        <a-form-item label="复审日期">
          <a-date-picker v-model:value="form.reviewDate" valueFormat="YYYY-MM-DD" style="width:100%" />
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="form.remark" :rows="2" placeholder="可选" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="ts" name="maotai-equipment-detail" setup>
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import QRCode from 'qrcode';
import { Icon } from '/@/components/Icon';
import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';
import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';
import { getById as getEquipment, list as ledgerListApi } from './MaotaiEquipmentLedger.api';
import { list as docListApi, add as docAddApi, edit as docEditApi, deleteOne as docDeleteApi, uploadUrl } from './MaotaiEquipmentDoc.api';
import { getDictItems } from '/@/utils/dict/JDictSelectUtil';
// 注:不要 import '/@/api/manage'(其内部 @/jslib/request.js 已失效,会致 Vite 解析失败);文件 url 拼接改为内联

const route = useRoute();
const router = useRouter();
const { createMessage: msg, createConfirm } = useMessage();

const equipmentId = ref<string>((route.query.id as string) || '');
// 穿透入口:驾驶舱罐体浮层按位置编码跳转(无 id 时先按 funlocCode 换取 id)
const funlocCodeParam = (route.query.funlocCode as string) || '';
const loading = ref(false);
const equipment = ref<any>({});

// 车间字典 code→text 映射(queryById 单条不回填 _dictText,前端自行翻译)
const workshopMap = ref<Record<string, string>>({});
const workshopName = computed(() => workshopMap.value[equipment.value.workshopCode] || equipment.value.workshopCode_dictText || equipment.value.workshopCode || '—');
async function loadWorkshopDict() {
  try {
    const items: any = await getDictItems("mes3_mdm_dict where group_code='MES3_WORKSHOP' and del_flag=0, item_val, item_key");
    const map: Record<string, string> = {};
    (items || []).forEach((it: any) => { map[it.value] = it.text; });
    workshopMap.value = map;
  } catch (e) {
    // 字典加载失败不影响主流程
  }
}

// 运行状态映射(与台账列表一致)
const STATUS_MAP: Record<number, { text: string; color: string; bg: string }> = {
  1: { text: '在线', color: '#10B981', bg: '#ECFDF5' },
  2: { text: '停机', color: '#A8A29E', bg: '#F5F5F4' },
  3: { text: '维修', color: '#D97706', bg: '#FFFBEB' },
  4: { text: '报警', color: '#B91C1C', bg: '#FEF2F2' },
};
const statusMap = computed(() => STATUS_MAP[equipment.value.runStatus] || { text: '—', color: '#999', bg: '#F5F5F4' });

// 保养周期翻译(maintCycle Integer + 字典 MES3_MAINT_CYCLE)
const maintCycleMap = ref<Record<string, string>>({});
const maintCycleName = computed(() => {
  const m = equipment.value.maintCycle;
  if (m === null || m === undefined || m === '') return '—';
  return maintCycleMap.value[String(m)] || (m + ' 天');
});
async function loadMaintCycleDict() {
  try {
    const items: any = await getDictItems("mes3_mdm_dict where group_code='MES3_MAINT_CYCLE' and del_flag=0, item_val, item_key");
    const map: Record<string, string> = {};
    (items || []).forEach((it: any) => { map[it.value] = it.text; });
    maintCycleMap.value = map;
  } catch (e) {
    // 字典加载失败不影响主流程
  }
}

// 二维码
const qrCanvasRef = ref<HTMLCanvasElement>();
function renderQR() {
  nextTick(() => {
    const canvas = qrCanvasRef.value;
    if (!canvas) return;
    const text = [equipment.value.funlocCode, equipment.value.equipName, equipment.value.workshopCode_dictText || equipment.value.workshopCode, equipment.value.equipModel].filter(Boolean).join('|');
    QRCode.toCanvas(canvas, text || equipment.value.funlocCode, { width: 160, margin: 1 }, (err) => {
      if (err) console.warn('二维码生成失败', err);
    });
  });
}
function downloadQR() {
  const canvas = qrCanvasRef.value;
  if (!canvas) return;
  const link = document.createElement('a');
  link.download = `qrcode_${equipment.value.funlocCode || ''}.png`;
  link.href = canvas.toDataURL('image/png');
  link.click();
}

// funlocCode → 台账 id(穿透入口)
async function resolveFunlocToId() {
  if (!funlocCodeParam || equipmentId.value) return;
  try {
    const res: any = await ledgerListApi({ pageNo: 1, pageSize: 1, funlocCode: funlocCodeParam });
    const row = res?.records?.[0];
    if (row?.id) equipmentId.value = row.id;
  } catch (e) {
    console.error('按位置编码查设备失败', e);
  }
}

// 加载设备信息
async function loadEquipment() {
  if (!equipmentId.value) return;
  loading.value = true;
  try {
    const res = await getEquipment({ id: equipmentId.value });
    equipment.value = res || {};
    renderQR();
  } finally {
    loading.value = false;
  }
}

// ===== SOP/SWI 文档列表 =====
const docLoading = ref(false);
const docList = ref<any[]>([]);
const docColumns = [
  { title: '类型', dataIndex: 'docType', width: 180 },
  { title: '文档标题', dataIndex: 'docTitle' },
  { title: '版本', dataIndex: 'version', width: 90 },
  { title: '编制人', dataIndex: 'author', width: 100 },
  { title: '发布日期', dataIndex: 'publishDate', width: 120 },
  { title: '操作', dataIndex: 'action', width: 210 },
];
const BADGE_STYLE: Record<string, { bg: string; color: string }> = {
  SOP: { bg: '#EFF6FF', color: '#2563EB' },
  SWI: { bg: '#FFFBEB', color: '#D97706' },
  MT: { bg: '#ECFDF5', color: '#059669' },
  EM: { bg: '#FEF2F2', color: '#DC2626' },
};
function badgeStyle(type: string) {
  const s = BADGE_STYLE[type] || { bg: '#F5F5F4', color: '#666' };
  return { background: s.bg, color: s.color };
}

async function loadDocs() {
  docLoading.value = true;
  try {
    const res = await docListApi({ equipmentId: equipmentId.value, pageNo: 1, pageSize: 100 });
    docList.value = res?.records || [];
  } finally {
    docLoading.value = false;
  }
}

function handleDownload(record: any) {
  if (!record.fileUrl) {
    msg.warning({ content: '该手册未上传文件' });
    return;
  }
  // fileUrl 为 dbPath(相对路径),拼访问 url = staticDomainURL(优先)/ domianURL + '/' + dbPath
  const dbPath = record.fileUrl;
  const cfg = (window as any)._CONFIG || {};
  const base = cfg.staticDomainURL || cfg.domianURL || '';
  const url = /^https?:\/\//.test(dbPath) ? dbPath : base + '/' + dbPath;
  if (!base && !/^https?:\/\//.test(dbPath)) {
    msg.warning({ content: '文件地址解析失败(未配置 staticDomainURL)' });
    return;
  }
  window.open(url, '_blank');
}

function handleDeleteDoc(record: any) {
  docDeleteApi({ id: record.id }, () => {
    msg.success({ content: '删除成功' });
    loadDocs();
  });
}

// ===== 上传/编辑手册 =====
const uploadVisible = ref(false);
const uploading = ref(false);
const isEdit = ref(false);
const uploadTitle = computed(() => (isEdit.value ? '编辑操作手册' : '上传操作手册'));
const fileList = ref<any[]>([]);
const currentFile = ref<File | null>(null);
const form = reactive({
  id: '',
  docType: undefined,
  docTitle: '',
  docCode: '',
  version: '',
  author: '',
  approver: '',
  publishDate: undefined,
  reviewDate: undefined,
  remark: '',
  fileName: '',
  fileUrl: '',
});

const ALLOW_EXT = ['pdf', 'doc', 'docx', 'xls', 'xlsx', 'jpg', 'jpeg', 'png'];
const MAX_SIZE = 50 * 1024 * 1024;

function openUploadModal() {
  isEdit.value = false;
  Object.assign(form, { id: '', docType: undefined, docTitle: '', docCode: '', version: '', author: '', approver: '', publishDate: undefined, reviewDate: undefined, remark: '', fileName: '', fileUrl: '' });
  currentFile.value = null;
  fileList.value = [];
  uploadVisible.value = true;
}

/** 编辑手册(预填元信息,文件可选替换) */
function editDoc(record: any) {
  isEdit.value = true;
  Object.assign(form, {
    id: record.id,
    docType: record.docType,
    docTitle: record.docTitle,
    docCode: record.docCode,
    version: record.version,
    author: record.author,
    approver: record.approver,
    publishDate: record.publishDate,
    reviewDate: record.reviewDate,
    remark: record.remark,
    fileName: record.fileName,
    fileUrl: record.fileUrl,
  });
  currentFile.value = null;
  fileList.value = [];
  uploadVisible.value = true;
}

function beforeUpload(file: any) {
  const ext = file.name.substring(file.name.lastIndexOf('.') + 1).toLowerCase();
  if (!ALLOW_EXT.includes(ext)) {
    msg.error({ content: '不支持的文件类型,仅允许:pdf/doc/docx/xls/xlsx/jpg/png' });
    return false;
  }
  if (file.size > MAX_SIZE) {
    msg.error({ content: '文件大小不能超过 50MB' });
    return false;
  }
  currentFile.value = file;
  form.fileName = file.name;
  fileList.value = [{ uid: file.uid || file.name, name: file.name, status: 'done' }];
  return false; // 阻止自动上传,提交时手动上传
}
function handleRemoveFile() {
  currentFile.value = null;
  form.fileName = '';
  form.fileUrl = '';
  fileList.value = [];
}

async function submitUpload() {
  // 新增必须选文件;编辑时文件可选(不选则保留原文件)
  if (!isEdit.value && !currentFile.value) {
    msg.warning({ content: '请先选择手册文件' });
    return;
  }
  if (!form.docType || !form.docTitle) {
    msg.warning({ content: '请填写文档类型和标题' });
    return;
  }
  uploading.value = true;
  try {
    let fileUrl = form.fileUrl;
    let fileName = form.fileName;
    // 选了新文件则上传替换
    if (currentFile.value) {
      // 关键:defHttp.uploadFile 第3参须传 {isReturnResponse:true},否则只弹消息、返回 undefined(见 Axios.ts uploadFile)
      // 返回体约定:message = 文件相对路径(dbPath)
      const upRes: any = await defHttp.uploadFile({ url: uploadUrl }, { file: currentFile.value }, { isReturnResponse: true });
      const newUrl = upRes?.message || '';
      if (!newUrl) {
        msg.error({ content: '文件上传失败,未获取到文件地址' });
        return;
      }
      fileUrl = newUrl;
      fileName = form.fileName; // beforeUpload 已把新文件名写入 form.fileName
    }
    const payload = {
      equipmentId: equipmentId.value,
      docType: form.docType,
      docTitle: form.docTitle,
      docCode: form.docCode,
      version: form.version,
      author: form.author,
      approver: form.approver,
      publishDate: form.publishDate,
      reviewDate: form.reviewDate,
      remark: form.remark,
      fileName,
      fileUrl,
    };
    if (isEdit.value) {
      await docEditApi({ ...payload, id: form.id });
      msg.success({ content: '编辑成功' });
    } else {
      await docAddApi(payload);
      msg.success({ content: '上传成功' });
    }
    uploadVisible.value = false;
    loadDocs();
  } catch (e) {
    // defHttp 统一提示
  } finally {
    uploading.value = false;
  }
}

function goBack() {
  // 明确返回设备台账列表(而非浏览器历史上一页)
  router.push('/maotai/equipmentLedger');
}

onMounted(async () => {
  loadWorkshopDict();
  loadMaintCycleDict();
  await resolveFunlocToId();
  loadEquipment();
  loadDocs();
});

// 切换设备(query.id 变化)时重新加载设备信息 + 文档 + 二维码
// 详情页 hideTab,组件实例复用,onMounted 不再触发,靠 watch 刷新
watch(
  () => route.query.id,
  (newId) => {
    const id = (newId as string) || '';
    if (id && id !== equipmentId.value) {
      equipmentId.value = id;
      loadEquipment();
      loadDocs();
    }
  },
);
</script>

<style lang="less" scoped>
.maotai-equipment-detail {
  padding: 12px;
  .detail-toolbar { margin-bottom: 12px; }
  .block { margin-bottom: 16px; border-radius: 8px; }
  .eq-header { display: flex; align-items: center; gap: 16px;
    .eq-icon { width: 56px; height: 56px; border-radius: 8px; background: #F5F5F4; border: 1px solid #E7E5E4; display: flex; align-items: center; justify-content: center; }
    .eq-info h2 { margin: 0 0 6px 0; font-size: 20px; font-weight: 700; }
    .eq-meta { display: flex; gap: 16px; align-items: center; font-size: 12px; color: #A8A29E; flex-wrap: wrap;
      .status-tag { padding: 2px 10px; border-radius: 12px; font-weight: 600; }
    }
  }
  .info-qr-row { display: grid; grid-template-columns: 1fr 200px; gap: 20px; align-items: center; }
  .qr-card { display: flex; flex-direction: column; align-items: center; padding: 16px; border: 1px solid #E7E5E4; border-radius: 8px; background: #FAFAF9;
    .qr-canvas { width: 160px !important; height: 160px !important; }
    .qr-label { margin-top: 8px; font-weight: 700; font-size: 13px; }
    .qr-hint { font-size: 11px; color: #A8A29E; margin: 2px 0 10px; }
  }
  .doc-badge { display: inline-block; padding: 2px 8px; border-radius: 4px; font-size: 12px; font-weight: 700; }
  .upload-tip { color: #10B981; font-size: 12px; margin-top: 4px; }
}
</style>
