<template>
  <div class="alarm-center">
    <!-- 卡片一:搜索条件 + 报警状态(两行,独立于表格卡片,像台账的搜索卡与表格卡分离) -->
    <div class="card">
      <div class="filter-bar single-row">
        <div class="filter-fields">
          <span class="field-label lead">报警内容:</span>
          <a-input v-model:value="kwContent" placeholder="请输入报警内容关键词" allow-clear style="width: 170px" @pressEnter="onSearch" />
          <span class="field-label">设备名称:</span>
          <a-input v-model:value="kwEquipment" placeholder="请输入设备编码" allow-clear style="width: 130px" @pressEnter="onSearch" />
          <span class="field-label">所属车间:</span>
          <a-input v-model:value="kwWorkshop" placeholder="请输入车间名称" allow-clear style="width: 130px" @pressEnter="onSearch" />
          <span class="field-label">报警级别:</span>
          <a-select v-model:value="levelFilter" placeholder="请选择级别" allow-clear style="width: 115px" @change="onSearch">
            <a-select-option value="all">全部类型</a-select-option>
            <a-select-option value="critical">紧急</a-select-option>
            <a-select-option value="warning">警告</a-select-option>
            <a-select-option value="info">提示</a-select-option>
          </a-select>
          <span class="field-label">处理状态:</span>
          <a-select v-model:value="statusFilter" placeholder="请选择状态" allow-clear style="width: 115px" @change="onSearch">
            <a-select-option value="all">全部状态</a-select-option>
            <a-select-option value="未处理">未处理</a-select-option>
            <a-select-option value="处理中">处理中</a-select-option>
            <a-select-option value="已处理">已处理</a-select-option>
          </a-select>
          <a-button type="primary" @click="onSearch"><Icon icon="ant-design:search-outlined" /> 查询</a-button>
          <a-button @click="onReset"><Icon icon="ant-design:reload-outlined" /> 重置</a-button>
          <a-button size="small" class="mute-btn" @click="toggleMute">
            {{ speechMuted ? '🔇 静音中' : '🔊 语音开' }}
          </a-button>
        </div>
      </div>

      <!-- 第二行:报警状态灯条(暂时注释,语音开关已上移查询行)
      <div class="filter-bar status-row">
        <div v-if="displayedAlert" :class="['alert-led', `led-${displayedAlert.level}`]" :title="displayedAlert.content">
          <span class="led-bar"></span>
          <span class="led-text">{{ LEVEL_TEXT[displayedAlert.level] }}报警 {{ pendingHighCount }} 条</span>
        </div>
        <div v-else class="alert-led led-idle">
          <span class="led-bar"></span>
          <span class="led-text">无未处理报警</span>
        </div>
      </div>
      -->
    </div>

    <!-- 卡片二:表格 -->
    <div class="card">
      <!-- 报警信息只按报警时间倒序(后端 orderByDesc alarmTime 已保证,前端不再按级别重排,filtered 保持原序)。
           样式对齐设备台账:bordered 分割线 + middle 行高(台账同款紧凑)+ 横向滚动兜底 -->
      <a-table
        :columns="columns"
        :data-source="filtered"
        :loading="loading"
        :row-class-name="rowClass"
        :pagination="{ pageSize: 10, showTotal: (t: number) => `共 ${t} 条` }"
        :scroll="{ x: 1200 }"
        bordered
        row-key="id"
        size="middle"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'level'">
            <a-tag :color="levelColor[record.level]">{{ LEVEL_TEXT[record.level as AlarmLevel] }}</a-tag>
          </template>
          <template v-else-if="column.dataIndex === 'status'">
            <a-badge :status="statusBadge[record.status]" :text="record.status" />
          </template>
          <template v-else-if="column.dataIndex === 'handleWay'">
            <span class="handle-cell">{{ record.handleWay || '-' }}</span>
          </template>
          <template v-else-if="column.dataIndex === 'handleDesc'">
            <span class="handle-cell">{{ record.handleDesc || '-' }}</span>
          </template>
          <template v-else-if="column.dataIndex === 'action'">
            <a-button
              v-auth="'maotai:alarm:handle'" type="link" size="small"
              :disabled="record.status === '已处理'"
              @click="openHandle(record)">{{ record.status === '已处理' ? '已处理' : '处理' }}</a-button>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 视频联动弹窗 -->
    <VideoLinkModal v-model:open="videoOpen" :alarm="currentAlarm" />

    <!-- 处理报警弹窗(布局与编辑设备表单统一:800 宽 + 110px label 两列网格) -->
    <a-modal
      v-model:open="handleOpen"
      :width="880"
      :confirm-loading="submitting"
      ok-text="提交"
      cancel-text="取消"
      :ok-button-props="{ danger: currentAlarm?.level === 'critical' }"
      class="handle-modal"
      @ok="submitHandle"
    >
      <template #title>
        <div class="modal-title">
          <span :class="['title-dot', `dot-${currentAlarm?.level || 'info'}`]"></span>
          处理报警
          <a-tag v-if="currentAlarm" :color="levelColor[currentAlarm.level]" class="title-tag">
            {{ LEVEL_TEXT[currentAlarm.level as AlarmLevel] }}
          </a-tag>
        </div>
      </template>

      <div class="form-pad">
      <a-form :label-col="{ style: { width: '110px' } }" :wrapper-col="{ style: { width: 'calc(100% - 110px)' } }" class="handle-form">
        <a-row :gutter="[12, 0]">
          <a-col :span="12"><a-form-item label="报警时间"><span class="dv">{{ currentAlarm?.time || '—' }}</span></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="报警级别"><a-tag :color="levelColor[currentAlarm?.level || 'info']" style="margin: 0">{{ LEVEL_TEXT[currentAlarm?.level || 'info'] }}</a-tag></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="设备"><span class="dv">{{ currentAlarm?.equipment || '—' }}</span></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="车间"><span class="dv">{{ currentAlarm?.workshop || '—' }}</span></a-form-item></a-col>
          <a-col :span="12"><a-form-item label="当前状态"><a-badge :status="statusBadge[currentAlarm?.status || '未处理']" :text="currentAlarm?.status || '—'" /></a-form-item></a-col>
          <a-col :span="24"><a-form-item label="报警内容"><span class="dv block">{{ currentAlarm?.content || '—' }}</span></a-form-item></a-col>
          <a-col :span="12">
            <a-form-item label="处理状态" required>
              <!-- 状态流转:未处理→可选[处理中/已处理];处理中→只能[已处理](唯一选项不预选,用户点一下确认);
                   已处理→终态,直接文字展示不可再改 -->
              <template v-if="currentAlarm?.status === '已处理'">
                <a-badge status="success" text="已处理" />
              </template>
              <a-radio-group v-else v-model:value="handleForm.status" button-style="solid">
                <a-radio-button v-if="currentAlarm?.status === '未处理'" :value="2">处理中</a-radio-button>
                <a-radio-button :value="3">已处理</a-radio-button>
              </a-radio-group>
              <div v-if="currentAlarm?.status === '处理中'" class="status-hint">
                当前已进入处理流程,仅可标记为已处理
              </div>
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item label="处理方式" required>
              <!-- 已处理单:方式只读展示(原值文字),不可改;其余状态可选 -->
              <span v-if="currentAlarm?.status === '已处理'" class="dv">{{ currentAlarm?.handleWay || '—' }}</span>
              <a-radio-group v-else v-model:value="handleForm.way" button-style="solid">
                <a-radio-button value="现场处理">现场处理</a-radio-button>
                <a-radio-button value="远程复位">远程复位</a-radio-button>
                <a-radio-button value="转派工单">转派工单</a-radio-button>
                <a-radio-button value="标记误报">标记误报</a-radio-button>
              </a-radio-group>
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item label="处理说明">
              <a-textarea v-model:value="handleForm.desc" :rows="2" placeholder="请输入处理说明(选填)" allow-clear />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
      </div>
    </a-modal>
  </div>
</template>

<script lang="ts" name="maotai-alarm-center" setup>
  import { ref, computed, reactive, onMounted, onBeforeUnmount, watch } from 'vue';
  import { message } from 'ant-design-vue';
  import dayjs from 'dayjs';
  import VideoLinkModal from './components/VideoLinkModal.vue';
  import { Icon } from '/@/components/Icon';
  import { LEVEL_TEXT, type AlarmRecord, type AlarmLevel, type AlarmStatus } from './alarm.mock';
  import { listAlarms, handleAlarm, type AlarmEventVO } from './AlarmCenter.api';
  import { speakAlarm, stopSpeech, setSpeechMuted } from './speech';
  import { setExternalLight } from './externalLight';

  // 真实报警数据(后端 /maotai/scada/alarm/list,ScadaAlarmQueryController 固定 sourceSystem='中集自控')。
  // 拉最新 1000 条后前端过滤/分页/统计(沿用原 mock 的客户端逻辑,仅换数据源)。
  const alarms = ref<AlarmRecord[]>([]);
  const loading = ref(false);

  /** 后端 AlarmEvent → 前端展示模型 AlarmRecord(alarmLevel 1/2/3→info/warning/critical;status 0/1/2/3→未处理/处理中/已处理) */
  function toRecord(e: AlarmEventVO): AlarmRecord {
    const level: AlarmLevel = e.alarmLevel === 3 ? 'critical' : e.alarmLevel === 2 ? 'warning' : 'info';
    // status:0未响应/1已响应 都归"未处理"(前端无"已响应"桶);2处理中;3已处理
    const status: AlarmStatus = e.status === 3 ? '已处理' : e.status === 2 ? '处理中' : '未处理';
    return {
      id: String(e.id),
      time: e.alarmTime ? dayjs(e.alarmTime).format('YYYY-MM-DD HH:mm') : '',
      level,
      content: e.alarmDesc || e.alarmName || e.alarmPiCode || '(无描述)',
      equipment: e.deviceCode || '',
      // 车间:后端 ScadaAlarmQueryController.fillDeptNames join sys_depart 翻译为 deptCodeDictText(驼峰);
      // 优先显示翻译名,无翻译(查不到部门)兜底显示 org_code 原值
      workshop: e.deptCodeDictText || e.lineCodeDictText || e.factoryCodeDictText || e.deptCode || e.lineCode || e.factoryCode || '',
      status,
      // 处理方式/说明展示到每条报警信息(表格列直接读)
      handleWay: e.handleWay || '',
      handleDesc: e.handleDesc || '',
      // TODO:点位主数据关联海康 cameraIndexCode 后补真码(见 alarm_point_scada_seed.sql.template)
      cameraIndexCode: '',
      cameraName: '',
    };
  }

  async function loadAlarms() {
    loading.value = true;
    console.debug('[Alarm][poll] 轮询触发', new Date().toLocaleTimeString());
    try {
      const res = await listAlarms({ pageNo: 1, pageSize: 1000 });
      // 后端 orderByDesc(alarm_time) 已按时间倒序;再按 time 字符串倒序排一次兜底(YYYY-MM-DD HH:mm 字典序=时间序),
      // 保证"报警信息只按报警时间倒序",不按级别/状态重排
      alarms.value = (res?.records || []).map(toRecord).sort((a, b) => (a.time < b.time ? 1 : a.time > b.time ? -1 : 0));
      const list = pendingAlerts.value;
      console.debug('[Alarm][poll] 数据', alarms.value.length, '条,未处理高级', list.length, '条,静音=', speechMuted.value);
      // 轮转播报:每轮拉到新数据后,只要还有未处理高级报警且非静音,就播 pending 列表"下一条"
      // (critical 优先再 warning),播完一圈循环。灯条仍跟最高级别(activeAlert)。
      if (list.length && !speechMuted.value) {
        speakNextPending();
      }
    } catch (e) {
      console.error('[Alarm] 加载报警列表失败', e);
      message.error('加载报警列表失败');
    } finally {
      loading.value = false;
    }
  }

  // 30s 轮询(值班告警主动感知):onMounted 起轮询,onBeforeUnmount 清 + 停语音 + 灭外接灯(仅报警页内)
  let pollTimer: ReturnType<typeof setInterval> | undefined;
  const POLL_INTERVAL = 10 * 1000; // 与驾驶舱罐体轮询同频(10s),处理/新报警联动即时可见
  onMounted(() => {
    loadAlarms();
    pollTimer = setInterval(loadAlarms, POLL_INTERVAL);
  });
  onBeforeUnmount(() => {
    if (pollTimer) clearInterval(pollTimer);
    stopSpeech();
    setExternalLight(null);
  });

  /** 最高级别未处理报警(critical>warning;info 不计——提示级只进统计卡不闪不播)。无则 null。 */
  const LEVEL_RANK: Record<AlarmLevel, number> = { critical: 3, warning: 2, info: 1 };
  /** 未处理高级报警列表(critical 优先再 warning,按级别排序;info 不计),供灯条/统计/语音共用 */
  const pendingAlerts = computed(() =>
    alarms.value
      .filter((a) => a.status === '未处理' && a.level !== 'info')
      .sort((a, b) => LEVEL_RANK[b.level] - LEVEL_RANK[a.level]),
  );
  const activeAlert = computed<AlarmRecord | null>(() => pendingAlerts.value[0] || null);
  /** 未处理高级(critical+warning)报警条数,提示灯文字用 */
  const pendingHighCount = computed(() => pendingAlerts.value.length);
  /**
   * 灯条显示哪条:非静音时跟"正在播的那条"(currentSpeech,和语音同步变色——念 critical 闪红、念 warning 闪橙);
   * 静音时回退最高级别(activeAlert,最严重的红色),保证闭音期间视觉仍提示最危情况。
   */
  const displayedAlert = computed<AlarmRecord | null>(() => {
    if (!speechMuted.value && currentSpeech.value) return currentSpeech.value;
    return activeAlert.value;
  });

  // 语音轮转游标:每轮播 pendingAlerts 的一条,顺次轮转(critical 优先再 warning),播完一圈循环。
  // currentSpeech 记录"当前正在播的那条"——灯条颜色跟它变(和语音内容同步),播完一轮还在它身上直到下一轮换。
  const speechCursor = ref(0);
  const currentSpeech = ref<AlarmRecord | null>(null);
  function speakNextPending() {
    const list = pendingAlerts.value;
    if (!list.length) {
      currentSpeech.value = null;
      return;
    }
    const idx = speechCursor.value % list.length;
    const cur = list[idx];
    currentSpeech.value = cur; // 灯条读它变色,和语音同条同步
    speakAlarm(cur.level, cur.content, cur.workshop);
    speechCursor.value = idx + 1; // 顺移到下一条;用取模后的 idx+1 防止数字无限增长
  }

  // 静音:关语音不关灯(值班人接电话时闭音,灯仍闪保证视觉感知)
  const speechMuted = ref(false);
  function toggleMute() {
    speechMuted.value = !speechMuted.value;
    setSpeechMuted(speechMuted.value);
    if (speechMuted.value) {
      stopSpeech(); // 静音:立即停当前播报
    } else if (pendingAlerts.value.length) {
      // 取消静音:立即播 pending 列表的下一条,不用干等下一轮 30s 轮询
      speakNextPending();
    }
  }

  // 外接灯随 displayedAlert 变化同步驱动(和页面灯条一样跟"正在播的那条");页面灯靠 v-if/:class 响应 displayedAlert 自动闪。
  // 语音不在 watch 里播——放 loadAlarms 每轮尾部实现持续轮转(watch 在 currentSpeech 引用不变时不重触发)。
  watch(
    [displayedAlert],
    ([alert]) => {
      setExternalLight(alert ? alert.level : null);
    },
    { immediate: true },
  );
  const levelFilter = ref<'all' | AlarmLevel>('all');
  const statusFilter = ref<'all' | string>('all');
  // 三个独立搜索框(报警内容/设备/车间):输入暂存,点查询/回车才应用(applied 三件套,filtered 只认 applied)。
  // 任一框点 X 清空时 watch 即时把对应 applied 也清掉——"清空就退回全部"符合直觉。
  const kwContent = ref('');
  const kwEquipment = ref('');
  const kwWorkshop = ref('');
  const appliedContent = ref('');
  const appliedEquipment = ref('');
  const appliedWorkshop = ref('');
  function onSearch() {
    appliedContent.value = kwContent.value.trim();
    appliedEquipment.value = kwEquipment.value.trim();
    appliedWorkshop.value = kwWorkshop.value.trim();
  }
  /** 重置:级别/状态回"全部",三个关键词框全部清空 */
  function onReset() {
    levelFilter.value = 'all';
    statusFilter.value = 'all';
    kwContent.value = '';
    kwEquipment.value = '';
    kwWorkshop.value = '';
    appliedContent.value = '';
    appliedEquipment.value = '';
    appliedWorkshop.value = '';
  }
  watch([kwContent, kwEquipment, kwWorkshop], ([c, e, w]) => {
    if (!c) appliedContent.value = '';
    if (!e) appliedEquipment.value = '';
    if (!w) appliedWorkshop.value = '';
  });

  const columns = [
    { title: '报警时间', dataIndex: 'time', width: 150, align: 'center' as const },
    { title: '级别', dataIndex: 'level', width: 80, align: 'center' as const },
    { title: '报警内容', dataIndex: 'content', ellipsis: true },
    { title: '设备', dataIndex: 'equipment', width: 120, align: 'center' as const },
    { title: '车间', dataIndex: 'workshop', width: 140, align: 'center' as const },
    { title: '状态', dataIndex: 'status', width: 90, align: 'center' as const },
    // 操作列插在状态与处理方式之间(2026-09-02 需求);fixed 移除,不再固定右侧
    { title: '操作', dataIndex: 'action', width: 80, align: 'center' as const },
    { title: '处理方式', dataIndex: 'handleWay', width: 110, align: 'center' as const },
    { title: '处理说明', dataIndex: 'handleDesc', ellipsis: true },
  ];

  const levelColor: Record<AlarmLevel, string> = { critical: 'red', warning: 'orange', info: 'default' };
  const statusBadge: Record<string, 'error' | 'warning' | 'success'> = {
    未处理: 'error',
    处理中: 'warning',
    已处理: 'success',
  };


  function rowClass(_record: AlarmRecord) {
    // 行底色统一白色(需求:每条信息卡片底色白色,级别差异只靠"级别"列的彩色 tag 表达)
    return '';
  }

  const filtered = computed(() => {
    const c = appliedContent.value;
    const e = appliedEquipment.value;
    const w = appliedWorkshop.value;
    return alarms.value.filter((a) => {
      if (levelFilter.value !== 'all' && a.level !== levelFilter.value) return false;
      if (statusFilter.value !== 'all' && a.status !== statusFilter.value) return false;
      // 三个关键词框各自独立匹配自己字段(AND 组合)
      if (c && !a.content.includes(c)) return false;
      if (e && !a.equipment.includes(e)) return false;
      if (w && !a.workshop.includes(w)) return false;
      return true;
    });
  });

  // 视频联动弹窗状态保留(操作列按钮已按需求移除,VideoLinkModal 组件与 videoOpen 留作后续入口恢复用)
  const videoOpen = ref(false);
  const currentAlarm = ref<AlarmRecord | null>(null);

  // 处理报警
  const handleOpen = ref(false);
  const submitting = ref(false); // 提交中防重复点击(绑 modal confirm-loading)
  // status:按状态流转规则——未处理→2或3任选;处理中/已处理→只能3(已处理);默认取可选集里最轻的一档
  const handleForm = reactive<{ status?: number; way?: string; desc: string }>({ status: undefined, way: undefined, desc: '' });
  function openHandle(r: AlarmRecord) {
    currentAlarm.value = r;
    // 一进去不预选:状态空选(含处理中单的唯一选项"已处理",由用户点选确认);
    // 处理方式:处理中单已有方式的带出原值亮选(那是它上一步的选择,应延续展示),
    // 未处理单空选;已处理是终态:状态展示徽标、方式只读文字,均不进表单值
    handleForm.status = undefined;
    handleForm.way = r.status === '处理中' && r.handleWay ? r.handleWay : undefined;
    handleForm.desc = r.handleDesc || '';
    handleOpen.value = true;
  }
  async function submitHandle() {
    if (!currentAlarm.value?.id) {
      message.warning('未选中报警');
      return;
    }
    // 未处理单:状态不预选,必须人工点选(处理中/已处理)才能提交
    if (currentAlarm.value.status !== '已处理' && !handleForm.status) {
      message.warning('请选择处理状态');
      return;
    }
    // 处理方式同样必选(已处理终态单除外——它只是补充说明用)
    if (currentAlarm.value.status !== '已处理' && !handleForm.way) {
      message.warning('请选择处理方式');
      return;
    }
    submitting.value = true;
    try {
      await handleAlarm({
        id: currentAlarm.value.id,
        handleWay: handleForm.way || currentAlarm.value.handleWay || '', // 处理方式(已处理单可能未改,带出原值)
        handleDesc: handleForm.desc, // 处理说明
        // 已处理是终态:展示层无 radio,提交固定 3(幂等,后端已是 3 再写一次无害)
        status: currentAlarm.value.status === '已处理' ? 3 : handleForm.status,
      });
      message.success((currentAlarm.value.status === '已处理' ? 3 : handleForm.status) === 3 ? '报警已处理' : '报警转为处理中');
      handleOpen.value = false;
      await loadAlarms(); // 刷新全量,computed(stats/灯条/activeAlert)自动联动各分类计数
    } catch (e) {
      // defHttp 统一弹错误提示,这里不重复弹
    } finally {
      submitting.value = false;
    }
  }

  function msg(t: string) {
    message.info(t);
  }
</script>

<style lang="less" scoped>
  .alarm-center {
    padding: 12px;
  }
  // 常驻提示灯条:有未处理高级报警时灯条闪烁(critical 红 急促/warning 橙 较缓,带外发光 box-shadow);无则灰条静态
  .alert-led {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 5px 12px;
    border-radius: 6px;
    font-size: 12px;
    font-weight: 500;
    background: #f5f5f4;
    .led-bar {
      width: 28px; // 明显的横向小灯条,不会太大
      height: 8px;
      border-radius: 4px;
      background: #d1d5db; // idle 灰
    }
    &.led-idle .led-bar {
      background: #d1d5db;
    }
    &.led-critical .led-bar {
      background: #ef4444;
      box-shadow: 0 0 8px 2px rgba(239, 68, 68, 0.7); // 红色外发光
      animation: blink-critical 0.8s infinite;
    }
    &.led-warning .led-bar {
      background: #f59e0b;
      box-shadow: 0 0 8px 2px rgba(245, 158, 11, 0.7); // 橙色外发光
      animation: blink-warning 1.2s infinite;
    }
  }
  @keyframes blink-critical {
    0%,
    100% {
      opacity: 1;
    }
    50% {
      opacity: 0.2;
    }
  }
  @keyframes blink-warning {
    0%,
    100% {
      opacity: 1;
    }
    50% {
      opacity: 0.4;
    }
  }
  .overview {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;
    margin-bottom: 16px;
  }
  // 统计卡片:整卡颜色填充(浅色底 + 同色系深色数值 + 左侧色条),一眼分区
  .ov-card {
    position: relative;
    padding: 16px 18px;
    border-radius: 8px;
    border: 1px solid transparent;
    overflow: hidden;
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      width: 4px;
      height: 100%;
    }
    &.ov-red {
      background: #fef2f2;
      border-color: #fecaca;
      &::before { background: #ef4444; }
      .ov-label, .ov-sub { color: #b91c1c; }
      .ov-value { color: #dc2626; }
    }
    &.ov-amber {
      background: #fffbeb;
      border-color: #fde68a;
      &::before { background: #f59e0b; }
      .ov-label, .ov-sub { color: #b45309; }
      .ov-value { color: #d97706; }
    }
    &.ov-stone {
      background: #f5f5f4;
      border-color: #e7e5e4;
      &::before { background: #78716c; }
      .ov-label, .ov-sub { color: #57534e; }
      .ov-value { color: #44403c; }
    }
    &.ov-green {
      background: #f0fdf4;
      border-color: #bbf7d0;
      &::before { background: #10b981; }
      .ov-label, .ov-sub { color: #15803d; }
      .ov-value { color: #16a34a; }
    }
    .ov-label {
      font-size: 12px;
      font-weight: 500;
    }
    .ov-value {
      font-size: 28px;
      font-weight: 800;
      margin: 6px 0;
      span {
        font-size: 12px;
        font-weight: 400;
        margin-left: 2px;
        opacity: 0.75;
      }
    }
    .ov-sub {
      font-size: 11px;
    }
  }
  .card {
    background: #fff;
    border-radius: 8px;
    border: 1px solid #f0f0f0;
    padding: 8px;
  }
  /* 第二行报警状态行:与卡片下缘的间距收窄(台账搜索区与表格间距 8px 同款) */
  .status-row {
    margin-bottom: 2px;
  }
  /* 两张卡片间距:台账搜索区与表格同容器贴合,这里两卡相邻贴合、外层留白即可 */
  .card + .card {
    margin-top: 8px;
  }
  .filter-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 12px;
    margin-bottom: 8px;
    flex-wrap: wrap;
  }
  .filter-fields {
    display: flex;
    gap: 8px;
    align-items: center;
    flex-wrap: wrap;
  }
  /* 第二行搜索区:label盒→输入框、输入框→下一label 两侧均匀(与台账 label↔输入框 8px 一致) */
  .single-row .filter-fields {
    gap: 8px;
  }
  .bar-right {
    display: flex;
    gap: 8px;
    align-items: center;
    margin-left: auto;
  }
  /* 第二行搜索框的字段标签:右对齐 + 8px 右内边距 —— 字段文字到其输入框统一 8px
     (台账"功能位置编码"6字label 到输入框 ~12px 的观感,短字段名不吃 96 盒的空白);
     前一个输入框 → 下一个字段名 8px(gap,同台账相邻条件) */
  .field-label {
    display: inline-block;
    width: 96px;
    text-align: right;
    /* 文字紧贴输入框(8px 视觉间距,去掉多余空白) */
    padding-right: 0;
    box-sizing: border-box;
    /* 前一输入框→字段名紧凑:gap8 全额吃掉再叠 6,视觉 ~2px */
    margin-left: -12px;
    font-size: 13px;
    color: rgba(0, 0, 0, 0.65);
    white-space: nowrap;
  }
  /* 首个"报警内容"标签:宽度自适应——文字左缘落在行首,与上行"全部"radio 左缘对齐(不吃 margin-left 缩进) */
  .field-label.lead {
    width: auto;
    text-align: left;
    margin-left: 0;
  }
  // 处理报警弹窗(重设计样式:标题带级别圆点+tag,报警摘要卡片化,表单分区)
  .modal-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    .title-dot {
      width: 10px;
      height: 10px;
      border-radius: 50%;
      &.dot-critical {
        background: #ef4444;
        box-shadow: 0 0 6px rgba(239, 68, 68, 0.6);
      }
      &.dot-warning {
        background: #f59e0b;
        box-shadow: 0 0 6px rgba(245, 158, 11, 0.6);
      }
      &.dot-info {
        background: #a8a29e;
      }
    }
    .title-tag {
      margin-left: 4px;
    }
  }
  /* 处理报警弹窗:表单与弹窗左右边缘留出间距(jeecg modal body 自身 padding 为 0) */
  .handle-modal .form-pad,
  .form-pad {
    padding: 4px 24px 0;
  }
  /* 处理报警弹窗:值展示盒(同保养工单详情的 dv 风格,与表单输入框同占位) */
  .handle-form .dv {
    display: inline-block;
    width: 100%;
    min-height: 32px;
    line-height: 30px;
    padding: 0 11px;
    color: rgba(0, 0, 0, 0.88);
    background: #fafafa;
    border: 1px solid #f0f0f0;
    border-radius: 2px;
    font-size: 14px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .handle-form .dv.block {
    white-space: pre-wrap;
    height: auto;
    min-height: 54px;
    line-height: 22px;
    padding-top: 5px;
  }
  .handle-form .status-hint {
    font-size: 12px;
    color: #f59e0b;
    margin-top: 4px;
  }
  /* 处理方式 radio 按钮文字、处理说明输入框文字与报警内容(14px/深灰)同号同色 */
  .handle-form :deep(.ant-radio-button-wrapper) {
    font-size: 14px;
    color: rgba(0, 0, 0, 0.88);
  }
  .handle-form :deep(.ant-radio-button-wrapper) > .ant-radio-button + span {
    font-size: 14px;
  }
  .handle-form :deep(textarea.ant-input),
  .handle-form :deep(.ant-input) {
    font-size: 14px;
    color: rgba(0, 0, 0, 0.88);
  }

  // 处理方式/说明列浅灰小字(辅助信息,不与主内容抢视觉)
  /* 操作列"处理"按钮垂直居中(td 高于按钮时默认偏上)——按 dataIndex 定位,不依赖列位置 */
  :deep(.ant-table-tbody > tr > td .ant-btn-link) {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    height: 100%;
    padding: 4px 8px;
    vertical-align: middle;
  }
  /* 处理方式/说明列:与其他列、设备台账同款 14px 默认字号(原 12px 灰色小字与其他列不一致) */
  .handle-cell {
    color: rgba(0, 0, 0, 0.88);
    font-size: 14px;
  }
</style>
