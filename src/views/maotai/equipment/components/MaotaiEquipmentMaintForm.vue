<template>
  <BasicModal @register="register" :title="title" :width="880" :showOkBtn="!isReadonly" @ok="handleSubmit">
    <div class="form-pad">
    <a-form :label-col="{ style: { width: '110px' } }" :wrapper-col="{ style: { width: 'calc(100% - 110px)' } }" class="maint-form">
      <a-row :gutter="[12, 0]">
        <a-col :span="12">
          <a-form-item label="工单号">
            <a-input v-model:value="form.orderNo" placeholder="保存后自动生成" disabled />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="所属设备" required>
            <JDictSelectTag v-model:value="form.equipmentId" :disabled="isReadonly" dictCode="mes3_maotai_equipment_ledger where del_flag=0, equip_name, id" placeholder="请选择设备" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="保养类型" required>
            <JDictSelectTag v-model:value="form.maintType" :disabled="isReadonly" dictCode="mes3_mdm_dict where group_code='MES3_MAINT_TYPE' and del_flag=0, item_val, item_key" placeholder="请选择类型" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="优先级">
            <JDictSelectTag v-model:value="form.priority" :disabled="isReadonly" dictCode="mes3_mdm_dict where group_code='MES3_MAINT_PRIORITY' and del_flag=0, item_val, item_key" placeholder="请选择优先级" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="计划日期" required>
            <a-date-picker v-model:value="form.planDate" :disabled="isReadonly" valueFormat="YYYY-MM-DD" style="width:100%" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="状态">
            <JDictSelectTag v-model:value="form.status" :disabled="isReadonly" dictCode="mes3_mdm_dict where group_code='MES3_MAINT_STATUS' and del_flag=0, item_val, item_key" placeholder="请选择状态" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="完成日期">
            <a-date-picker v-model:value="form.completeDate" :disabled="isReadonly" valueFormat="YYYY-MM-DD" style="width:100%" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="执行人">
            <a-input v-model:value="form.executor" :disabled="isReadonly" placeholder="请输入执行人" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="预估工时">
            <a-input v-model:value="form.estWorkhour" :disabled="isReadonly" placeholder="如:2.5 小时" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="实际工时">
            <a-input v-model:value="form.actualWorkhour" :disabled="isReadonly" placeholder="如:3.5 小时" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="费用">
            <a-input v-model:value="form.cost" :disabled="isReadonly" placeholder="如:¥2,380" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="所需备件">
            <a-input v-model:value="form.spareParts" :disabled="isReadonly" placeholder="如:空气滤芯 x1,润滑油 x5L" />
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item label="保养内容">
            <a-textarea v-model:value="form.maintContent" :disabled="isReadonly" :rows="2" placeholder="请输入保养内容" />
          </a-form-item>
        </a-col>
      </a-row>

      <!-- 保养内容清单(动态,仅记录内容项) -->
      <a-form-item label="保养内容清单">
        <div class="checklist">
          <div class="check-row" v-for="(_, idx) in checklistItems" :key="idx">
            <span class="check-no">{{ idx + 1 }}.</span>
            <a-input v-model:value="checklistItems[idx]" :disabled="isReadonly" placeholder="保养内容项" />
            <a-button v-if="!isReadonly" type="link" danger size="small" @click="removeCheck(idx)">删除</a-button>
          </div>
          <a-button v-if="!isReadonly" type="dashed" size="small" block @click="addCheck">
            <Icon icon="ant-design:plus-outlined" /> 添加内容项
          </a-button>
          <div v-if="isReadonly && checklistItems.length === 0" class="empty-tip">暂无保养内容项</div>
        </div>
      </a-form-item>

      <a-form-item label="备注">
        <a-textarea v-model:value="form.remark" :disabled="isReadonly" :rows="2" placeholder="可选" />
      </a-form-item>
    </a-form>
    </div>
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref, reactive, computed } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';
import { Icon } from '/@/components/Icon';
import { useMessage } from '/@/hooks/web/useMessage';
import { saveOrUpdate } from '../MaotaiEquipmentMaint.api';

const emit = defineEmits(['success']);
const { createMessage: msg } = useMessage();

const submitting = ref(false);
const isUpdate = ref(false);
const isReadonly = ref(false);
const title = computed(() => (isReadonly.value ? '保养工单详情' : isUpdate.value ? '编辑保养工单' : '新建保养工单'));

const form = reactive<any>({});
const checklistItems = ref<string[]>([]);

const [register, { closeModal, setModalProps }] = useModalInner(async (data: any) => {
  resetForm();
  isUpdate.value = data.action === 'edit';
  isReadonly.value = data.action === 'detail';
  setModalProps({ showOkBtn: !isReadonly.value });
  const r = data.record || {};
  Object.assign(form, {
    id: r.id || undefined,
    orderNo: r.orderNo || '',
    equipmentId: r.equipmentId || undefined,
    maintType: r.maintType || undefined,
    priority: r.priority || undefined,
    status: r.status || 'pending',
    planDate: r.planDate || undefined,
    completeDate: r.completeDate || undefined,
    executor: r.executor || '',
    estWorkhour: r.estWorkhour || '',
    actualWorkhour: r.actualWorkhour || '',
    cost: r.cost || '',
    maintContent: r.maintContent || '',
    spareParts: r.spareParts || '',
    remark: r.remark || '',
  });
  checklistItems.value = parseChecklist(r.checklist);
});

function resetForm() {
  Object.keys(form).forEach((k) => delete form[k]);
  checklistItems.value = [];
}
function addCheck() {
  checklistItems.value.push('');
}
function removeCheck(idx: number) {
  checklistItems.value.splice(idx, 1);
}
/**
 * 解析保养内容清单:兼容三种历史格式
 *  - "| 分隔文本"(当前,如 "检查油位|更换滤芯")
 *  - JSON 字符串数组(如 ["a","b"])
 *  - JSON 对象数组(旧 [{item,done}])
 */
function parseChecklist(raw: any): string[] {
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
  return s.split('|').map((x) => x.trim()).filter(Boolean);
}

async function handleSubmit() {
  if (!isUpdate.value) {
    delete form.id;
  }
  if (!form.equipmentId) {
    msg.warning({ content: '请选择所属设备' });
    return;
  }
  if (!form.maintType) {
    msg.warning({ content: '请选择保养类型' });
    return;
  }
  if (!form.planDate) {
    msg.warning({ content: '请选择计划日期' });
    return;
  }
  submitting.value = true;
  try {
    const payload = {
      ...form,
      checklist: checklistItems.value.map((s) => (s || '').trim()).filter(Boolean).join('|'),
    };
    await saveOrUpdate(payload, isUpdate.value);
    msg.success({ content: '操作成功' });
    closeModal();
    emit('success');
  } catch (e) {
    // defHttp 统一提示
  } finally {
    submitting.value = false;
  }
}
</script>

<style lang="less" scoped>
/** 表单内容与弹窗左右边缘留出间距(jeecg modal body 自身 padding 为 0) */
.form-pad {
  padding: 4px 24px 0;
}
.checklist .check-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.checklist .check-no {
  flex-shrink: 0;
  width: 24px;
  text-align: right;
  color: #a8a29e;
  font-size: 13px;
}
.empty-tip {
  color: #a8a29e;
  font-size: 12px;
  text-align: center;
  padding: 8px;
}
:deep(.ant-select) {
  width: 100%;
}
</style>
