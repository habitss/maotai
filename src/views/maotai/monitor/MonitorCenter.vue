<template>
  <div class="monitor-center">
    <!-- 左:监控点树(区域→相机) -->
    <aside class="mc-sidebar">
      <div class="mc-side-head">
        <h3>监控点</h3>
        <a-input-search v-model:value="keyword" placeholder="搜索名称/编码" allow-clear size="small" @search="() => {}" />
      </div>
      <a-spin :spinning="loading">
        <div class="mc-cam-list">
          <a-tree
            v-if="treeData.length"
            v-model:expanded-keys="expandedKeys"
            v-model:selected-keys="selectedKeys"
            block-node
            show-line
            :tree-data="treeData"
            @select="onTreeSelect"
          >
            <template #title="{ title, key }">
              <template v-if="String(key).startsWith('region:')">
                <span class="mc-region">
                  {{ title }}
                  <span class="mc-region-count">{{ regionCount(String(key)) }}</span>
                </span>
              </template>
              <template v-else>
                <span class="mc-cam-title">
                  <span class="mc-online-dot" :class="onlineClassOf(String(key))"></span>
                  {{ title }}
                </span>
              </template>
            </template>
          </a-tree>
          <div v-else class="mc-empty"> 暂无监控点{{ hostReady ? '' : '(后端 host/AK/SK 未配置时不返回)' }} </div>
        </div>
      </a-spin>
    </aside>

    <!-- 右:视频区 -->
    <section class="mc-main">
      <div class="mc-toolbar">
        <a-radio-group v-model:value="mode" button-style="solid" size="small">
          <a-radio-button value="real">实时预览</a-radio-button>
          <a-radio-button value="playback">录像回放</a-radio-button>
        </a-radio-group>
        <div class="mc-tool-right">
          <template v-if="mode === 'playback'">
            <a-range-picker
              v-model:value="playbackRange"
              :show-time="{ format: 'HH:mm' }"
              format="YYYY-MM-DD HH:mm"
              size="small"
              style="width: 330px"
            />
          </template>
          <a-divider type="vertical" />
          <span class="mc-split-label">分屏</span>
          <a-radio-group v-model:value="splitNum" size="small">
            <a-radio-button :value="1">1路</a-radio-button>
            <a-radio-button :value="2">4路</a-radio-button>
            <a-radio-button :value="3">9路</a-radio-button>
            <a-radio-button :value="4">16路</a-radio-button>
          </a-radio-group>
          <a-button size="small" @click="onCapture">📷 截图</a-button>
          <a-button size="small" @click="simActive ? simPlayerRef?.fullScreen() : playerRef?.fullScreen()">⛶ 全屏</a-button>
        </div>
      </div>
      <div class="mc-player">
        <!-- 海康流走 h5player;后端未配置 host 时返回 MP4 模拟流,走原生 video 网格(按 URL 类型自动分流) -->
        <HikPlayer
          v-show="!simActive"
          ref="playerRef"
          :current-split="splitNum"
          @wnd-select="(i: number) => (currentWnd = i)"
          @wnd-dbl-click="onWndDblClick"
          @error="onPlayError"
        />
        <SimVideoPlayer
          v-show="simActive"
          ref="simPlayerRef"
          :split="splitNum"
          @wnd-select="(i: number) => (currentWnd = i)"
          @wnd-dbl-click="onWndDblClick"
        />
      </div>
    </section>
  </div>
</template>

<script lang="ts" name="maotai-monitor-center" setup>
  import { ref, computed, onMounted, watch, nextTick } from 'vue';
  import { useRoute } from 'vue-router';
  import { message } from 'ant-design-vue';
  import dayjs, { Dayjs } from 'dayjs';
  import HikPlayer from './components/HikPlayer.vue';
  import SimVideoPlayer from './components/SimVideoPlayer.vue';
  import { listCameras, getPreviewUrl, getPlaybackUrl, CameraVO } from './MonitorCenter.api';

  const cameras = ref<CameraVO[]>([]);
  const total = ref(0);
  const loading = ref(false);
  const keyword = ref('');
  const activeCode = ref('');
  const mode = ref<'real' | 'playback'>('real');
  const splitNum = ref(1);
  const currentWnd = ref(0);
  const playbackRange = ref<[Dayjs, Dayjs]>();
  const playerRef = ref<InstanceType<typeof HikPlayer>>();
  const simPlayerRef = ref<InstanceType<typeof SimVideoPlayer>>();
  // 模拟视频模式:取流 URL 是 MP4(后端未配置海康 host 的 mock)时自动切原生 video 网格
  const simActive = ref(false);
  function isSimUrl(url?: string) {
    return !!url && /\.mp4($|\?)/i.test(url);
  }
  const route = useRoute();
  // 各窗口正在播什么(双击放大/恢复时重播用):wndIndex → {code, mode, begin?, end?}
  const playSlots = new Map<number, { code: string; mode: 0 | 1; begin?: string; end?: string }>();
  // 双击放大前的分屏数,再双击恢复用
  const beforeSplit = ref(1);

  // host/AK/SK 没配时后端会返回失败 → 列表空,这里仅用于提示文案
  const hostReady = ref(false);

  const filteredCameras = computed(() => {
    const kw = keyword.value.trim().toLowerCase();
    if (!kw) return cameras.value;
    return cameras.value.filter((c) => (c.cameraName || '').includes(kw) || c.cameraIndexCode.includes(kw));
  });

  // ===== 监控点树(区域→相机):regionIndexCode 分组;mock 放区域中文名,真海康时为区域编码(树逻辑同源) =====
  interface CamTreeNode {
    title: string;
    key: string;
    isLeaf?: boolean;
    selectable?: boolean;
    children?: CamTreeNode[];
  }
  const treeData = computed<CamTreeNode[]>(() => {
    const groups = new Map<string, CameraVO[]>();
    filteredCameras.value.forEach((c) => {
      const r = c.regionIndexCode || '未分区';
      if (!groups.has(r)) groups.set(r, []);
      groups.get(r)!.push(c);
    });
    return Array.from(groups.entries()).map(([region, cams]) => ({
      title: region,
      key: 'region:' + region,
      selectable: false,
      children: cams.map((c) => ({ title: c.cameraName || c.cameraIndexCode, key: c.cameraIndexCode, isLeaf: true })),
    }));
  });
  const expandedKeys = ref<string[]>([]);
  const selectedKeys = ref<string[]>([]);
  function regionCount(regionKey: string) {
    const region = regionKey.replace('region:', '');
    return cameras.value.filter((c) => (c.regionIndexCode || '未分区') === region).length;
  }
  function onlineClassOf(code: string) {
    const c = cameras.value.find((x) => x.cameraIndexCode === code);
    return c ? onlineClass(c) : 'unknown';
  }
  function onTreeSelect(keys: (string | number)[], info: any) {
    const key = String(keys[0] ?? '');
    if (!key || key.startsWith('region:')) return;
    const cam = cameras.value.find((x) => x.cameraIndexCode === key);
    if (cam) onCameraClick(cam);
  }

  async function loadCameras() {
    loading.value = true;
    try {
      // 树形+搜索过滤模式下一次拉全量(树本身承担分组浏览,不再分页)
      const res = await listCameras({ pageNo: 1, pageSize: 500 });
      cameras.value = res?.list || [];
      total.value = res?.total || 0;
      hostReady.value = cameras.value.length > 0;
      expandedKeys.value = treeData.value.map((n) => n.key);
    } catch (e) {
      console.error('[Monitor] 加载监控点失败', e);
    } finally {
      loading.value = false;
    }
  }

  /** 取流并播到指定窗口(按 URL 类型分流:MP4→模拟播放器,其余→海康 h5player);slot 供双击放大重播 */
  async function playBySlot(wnd: number, slot: { code: string; mode: 0 | 1; begin?: string; end?: string; name?: string }) {
    let url: string | undefined;
    if (slot.mode === 0) {
      const r = await getPreviewUrl(slot.code);
      url = r?.url;
    } else {
      const r = await getPlaybackUrl({ cameraIndexCode: slot.code, beginTime: slot.begin || '', endTime: slot.end || '' });
      url = r?.url;
    }
    if (!url) return false;
    if (isSimUrl(url)) {
      simActive.value = true;
      simPlayerRef.value?.play({ url, wndIndex: wnd, name: slot.name });
    } else {
      await playerRef.value?.play({
        url,
        wndIndex: wnd,
        mode: slot.mode,
        startTime: slot.begin,
        endTime: slot.end,
      });
    }
    playSlots.set(wnd, slot);
    return true;
  }

  async function onCameraClick(c: CameraVO) {
    activeCode.value = c.cameraIndexCode;
    try {
      const wnd = currentWnd.value;
      if (mode.value === 'real') {
        const ok = await playBySlot(wnd, { code: c.cameraIndexCode, mode: 0, name: c.cameraName });
        if (!ok) message.warning('未取到取流 URL(后端 hikvision.host/AK/SK 是否已配置?)');
      } else {
        // 回放:未选时间段默认当天 00:00~现在,点相机直接播
        const args = playbackArgs();
        const ok = await playBySlot(currentWnd.value, {
          code: c.cameraIndexCode,
          mode: 1,
          begin: args.begin,
          end: args.end,
          name: c.cameraName,
        });
        if (!ok) message.warning('未取到回放 URL');
      }
      // 播放成功后自动指向下一个未占用窗口(连点相机列表依次填充分屏,像海康客户端)
      currentWnd.value = nextFreeWnd(wnd);
    } catch (e: any) {
      message.error('播放失败: ' + (e?.message || e));
    }
  }

  /** 下一个未占用窗口:优先找空槽,全占则顺序轮转 */
  function nextFreeWnd(from: number) {
    const total = splitNum.value ** 2;
    for (let i = 0; i < total; i++) {
      const w = (from + 1 + i) % total;
      if (!playSlots.has(w)) return w;
    }
    return (from + 1) % total;
  }

  /** 当前播放用的回放时间参数(未选时间段时默认当天 00:00~现在,回放点相机即可播不必先选) */
  function playbackArgs() {
    if (mode.value !== 'playback') return {};
    const range = playbackRange.value;
    const begin = range?.[0] ?? dayjs().startOf('day');
    const end = range?.[1] ?? dayjs();
    const beginTime = begin.format('YYYY-MM-DDTHH:mm:ss.SSSZ');
    const endTime = end.format('YYYY-MM-DDTHH:mm:ss.SSSZ');
    return { mode: 1 as 0 | 1, begin: beginTime, end: endTime };
  }

  /** 切分屏后自动给各窗口分配不同相机:4路=4种画面,9/16路=8台相机各占一格(超出留空) */
  async function fillWindows() {
    const total = splitNum.value ** 2;
    const list = cameras.value.slice(0, total);
    for (let i = 0; i < list.length; i++) {
      playBySlot(i, { code: list[i].cameraIndexCode, name: list[i].cameraName, mode: 0, ...playbackArgs() }).catch(() => {});
    }
    await nextTick();
    // 兜底:重新渲染的 video 可能不自动起播,统一拉起
    setTimeout(() => simPlayerRef.value?.resumeAll(), 300);
  }

  // 分屏切换自动铺满;回放模式切换给默认时间段
  watch(splitNum, () => fillWindows());
  watch(mode, (m) => {
    if (m === 'playback' && !playbackRange.value) {
      playbackRange.value = [dayjs().startOf('day'), dayjs()];
    }
  });
  // 树选中态跟随 activeCode(报警联动 autoPlayByCode 时同步高亮)
  watch(activeCode, (code) => {
    selectedKeys.value = code ? [code] : [];
  });

  function onPlayError(info: { idx: number; code: number; err: any }) {
    message.error(`窗口${info.idx} 播放异常 code=${info.code}`);
  }

  /** 在线状态圆点 class:online 优先,回退 status;1在线(绿) 0离线(红) 其他未知(灰) */
  function onlineClass(c: CameraVO) {
    const s = c.online ?? c.status;
    if (s === '1') return 'on';
    if (s === '0') return 'off';
    return 'unknown';
  }

  /** 截图当前选中窗口(先检查该窗口是否在播,无流时直接提示,避免对空窗口截图无反应) */
  async function onCapture() {
    if (!playSlots.has(currentWnd.value)) {
      message.warning('当前窗口未播放视频,无法截图(先点监控点播放;host/AK/SK 未配时取不到流)');
      return;
    }
    try {
      if (simActive.value) {
        await simPlayerRef.value?.capture(currentWnd.value, dayjs().format('YYYYMMDDHHmmss'));
      } else {
        await playerRef.value?.capture(currentWnd.value, dayjs().format('YYYYMMDDHHmmss'));
      }
      message.success('已截图窗口' + currentWnd.value);
    } catch (e: any) {
      message.warning('截图失败: ' + (e?.message || e));
    }
  }

  /** 重播某窗口的流(双击放大/恢复;海康 ws URL 会过期需重取,模拟 MP4 直接重取同一地址) */
  async function replay(wnd: number, slot: { code: string; mode: 0 | 1; begin?: string; end?: string; name?: string }) {
    try {
      await playBySlot(wnd, slot);
    } catch (e) {
      console.error('[Monitor] 重播失败 wnd=' + wnd, e);
    }
  }

  /**
   * 双击窗口:多画面 → 放大成单画面(该路重播到窗口0);单画面 → 恢复原分屏(各槽位重播)。
   * 权衡:arrange 切分屏 SDK 只保留窗口0内容,被双击窗口的流不会自动迁移,故放大时重播该路到窗口0。
   */
  async function onWndDblClick(idx: number) {
    if (splitNum.value > 1) {
      beforeSplit.value = splitNum.value;
      const slot = playSlots.get(idx);
      splitNum.value = 1;
      if (slot) {
        await playerRef.value?.selectWnd(0);
        await replay(0, slot);
        playSlots.set(0, slot);
      }
    } else {
      splitNum.value = beforeSplit.value || 2;
      for (const [wnd, s] of playSlots) {
        if (wnd !== 0) await replay(wnd, s);
      }
    }
  }

  /** 报警联动跳入:按 cameraIndexCode 自动取流播放(播放器未就绪则重试) */
  async function autoPlayByCode(code: string, retry = 0) {
    try {
      const r = await getPreviewUrl(code);
      const url = r?.url;
      if (!url) {
        message.warning('未取到取流 URL(host/AK/SK 可能未配置)');
        return;
      }
      activeCode.value = code;
      currentWnd.value = 0;
      if (isSimUrl(url)) {
        simActive.value = true;
        simPlayerRef.value?.play({ url, wndIndex: 0, name: cameras.value.find((c) => c.cameraIndexCode === code)?.cameraName });
      } else {
        await playerRef.value?.play({ url, wndIndex: 0, mode: 0 });
      }
      playSlots.set(0, { code, mode: 0 });
    } catch (e: any) {
      if (e?.message === '播放器未就绪' && retry < 15) {
        setTimeout(() => autoPlayByCode(code, retry + 1), 300);
      } else {
        console.error('[Monitor] 联动自动播放失败', e);
      }
    }
  }

  onMounted(() => {
    loadCameras();
    const code = route.query.cameraIndexCode as string;
    if (code) autoPlayByCode(code);
  });
</script>

<style lang="less" scoped>
  .monitor-center {
    display: flex;
    gap: 12px;
    padding: 12px;
    height: calc(100vh - 100px);
    min-height: 480px;
  }

  .mc-sidebar {
    width: 280px;
    background: #fff;
    border-radius: 4px;
    border: 1px solid #f0f0f0;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }

  .mc-side-head {
    padding: 12px;
    border-bottom: 1px solid #f0f0f0;

    h3 {
      margin: 0 0 10px;
      font-size: 15px;
    }
  }

  .mc-cam-list {
    flex: 1;
    overflow-y: auto;
    padding: 6px;
  }

  /* 区域节点:加粗深蓝+数量徽标 */
  .mc-region {
    font-weight: 600;
    color: #003366;
    font-size: 13px;
  }

  .mc-region-count {
    margin-left: 6px;
    padding: 0 7px;
    border-radius: 8px;
    background: #e6f0fa;
    color: #3d6da5;
    font-size: 11px;
    font-weight: 500;
  }

  .mc-cam-title {
    font-size: 13px;
    color: #333;
  }

  .mc-online-dot {
    display: inline-block;
    width: 8px;
    height: 8px;
    border-radius: 50%;
    margin-right: 6px;
    vertical-align: middle;
    background: #bfbfbf; // unknown 默认灰

    &.on {
      background: #52c41a;
    } // 在线绿

    &.off {
      background: #ff4d4f;
    } // 离线红

    &.unknown {
      background: #bfbfbf;
    } // 未知灰
  }

  .mc-empty {
    text-align: center;
    color: #bbb;
    font-size: 12px;
    padding: 30px 8px;
    line-height: 1.6;
  }

  .mc-main {
    flex: 1;
    display: flex;
    flex-direction: column;
    background: #fff;
    border-radius: 4px;
    border: 1px solid #f0f0f0;
    overflow: hidden;
  }

  .mc-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 14px;
    border-bottom: 1px solid #f0f0f0;
  }

  .mc-tool-right {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .mc-split-label {
    font-size: 12px;
    color: #666;
  }

  .mc-player {
    flex: 1;
    background: #000;
  }
</style>
