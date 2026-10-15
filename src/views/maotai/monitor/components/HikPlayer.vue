<template>
  <div class="hik-player-wrap">
    <div :id="containerId" ref="containerRef" class="hik-player-container" @dblclick="onDblClick"></div>
    <div v-if="!ready" class="hik-player-tip">播放器加载中…(若长时间无画面,检查 codebase 是否完整 / host 是否配置) </div>
  </div>
</template>

<script lang="ts" name="hik-player" setup>
  import { ref, onMounted, onBeforeUnmount, watch } from 'vue';

  /**
   * 海康 h5player(V2.5.1) 播放器封装。
   * 按 V2.5.1 开发指南真实 API:new JSPlugin({szId, szBasePath, ...}) 构造 + 实例方法
   * JS_Play / JS_Stop / JS_ArrangeWindow / JS_StopRealPlayAll / JS_SetWindowControlCallback。
   * codebase(h5player.min.js + 解码依赖)放 public/h5player/dist/,组件按需动态注入主 js。
   */
  const props = withDefaults(
    defineProps<{
      /** h5player codebase 根路径(以 / 开头、/ 结尾),指向含 h5player.min.js 的目录 */
      szBasePath?: string;
      /** 当前分屏 1=1x1 2=2x2 3=3x3 4=4x4 */
      currentSplit?: number;
    }>(),
    { szBasePath: '/h5player/dist/', currentSplit: 1 }
  );

  const emit = defineEmits<{
    (e: 'wndSelect', idx: number): void;
    (e: 'wndDblClick', idx: number): void;
    (e: 'firstFrame', info: { idx: number; w: number; h: number }): void;
    (e: 'streamEnd', idx: number): void;
    (e: 'error', info: { idx: number; code: number; err: any }): void;
  }>();

  // calculation 模式 vite base=/mes3,默认 dev/prod base=/;拼上保证 public 资源带 base 前缀
  // (之前硬编码 /h5player/dist/ 绝对根路径,calculation 模式 h5player.min.js 加载 404 → JSPlugin undefined)
  const resolvedBasePath = (() => {
    const base = (import.meta.env.VITE_PUBLIC_PATH || '/').replace(/\/$/, '');
    const rel = props.szBasePath.replace(/^\/+/, '');
    return (base ? base + '/' : '/') + rel;
  })();

  let uidSeq = 0;
  const containerId = `hik_player_container_${++uidSeq}`;
  const containerRef = ref<HTMLDivElement>();
  const ready = ref(false);
  let plugin: any = null;
  // 当前选中窗口索引(windowEventSelect 回调维护;双击时用它作目标窗口)
  let selectedIdx = 0;

  // h5player.min.js 全应用只加载一次
  let scriptPromise: Promise<void> | null = null;

  function loadScript(): Promise<void> {
    if ((window as any).JSPlugin) return Promise.resolve();
    if (scriptPromise) return scriptPromise;
    scriptPromise = new Promise((resolve, reject) => {
      const s = document.createElement('script');
      s.id = 'h5player-script';
      s.src = resolvedBasePath + 'h5player.min.js';
      s.async = true;
      s.onload = () => resolve();
      s.onerror = () => {
        scriptPromise = null;
        reject(new Error('加载 h5player.min.js 失败: ' + s.src + ' (检查 codebase 路径)'));
      };
      document.head.appendChild(s);
    });
    return scriptPromise;
  }

  function initPlugin() {
    const JSPlugin = (window as any).JSPlugin;
    if (!JSPlugin || !containerRef.value) return;
    plugin = new JSPlugin({
      szId: containerId,
      szBasePath: resolvedBasePath,
      iMaxSplit: 4, // 最大 4x4
      iCurrentSplit: props.currentSplit,
      bSupporDoubleClickFull: false, // 关 SDK 双击全屏,让双击给单路放大用(全屏改独立按钮)
      mseWorkerEnable: false,
    });
    plugin.JS_SetWindowControlCallback({
      windowEventSelect: (idx: number) => {
        selectedIdx = idx;
        emit('wndSelect', idx);
      },
      pluginErrorHandler: (idx: number, code: number, err: any) => {
        console.error('[HIK] 播放异常 窗口' + idx + ' code=' + code, err);
        emit('error', { idx, code, err });
      },
      firstFrameDisplay: (idx: number, w: number, h: number) => emit('firstFrame', { idx, w, h }),
      StreamEnd: (idx: number) => emit('streamEnd', idx),
      // SDK 全屏状态钩子(demo.html:385 windowFullCcreenChange):退出全屏(bFull=false)时容器自身高度
      // 还停在全屏值,SDK 读它 → 按全屏高度画格 → 缩回后溢出。正常尺寸在 fullScreen() 入口已记录,
      // 这里退出时直接强制还原(见 onResizeToStored)。不在 bFull=true 这里记——回调触发时 SDK 可能已拉伸容器。
      windowFullCcreenChange: (bFull: boolean) => {
        console.log('[HIK] windowFullCcreenChange bFull=', bFull, 'stored=', storedW, 'x', storedH);
        if (!bFull) onResizeToStored();
      },
    });
    ready.value = true;
  }

  /** 进入全屏前记录的容器正常尺寸(windowFullCcreenChange bFull=true 时记下)。 */
  let storedW = 0;
  let storedH = 0;

  /** 退出全屏时容器自身高度还停在全屏值,JS_Resize 读它 → 按全屏高度画格 → 缩回后溢出。
   * 解法:把进入前记录的正常尺寸(storedW/H)写进容器 inline style,强制 SDK 按正确尺寸重排。
   * 退出瞬间 SDK 正忙于拆解,立即同步调 JS_Resize 会被忽略,故延迟 100ms 调一次(给 SDK 拆解留时间;
   * 慢机器若偶发溢出可把 MAX 调到 2-3 重试)。全程 opacity:0 藏住,用户只看到"退出全屏 → 直接均匀网格"。 */
  let resizeRetryTimer: ReturnType<typeof setInterval> | null = null;
  function onResizeToStored() {
    const el = containerRef.value;
    if (!el || !plugin) return;
    if (!storedW || !storedH) return; // 没记到正常尺寸(没经过进入全屏),不处理
    if (resizeRetryTimer) {
      clearInterval(resizeRetryTimer);
      resizeRetryTimer = null;
    }
    el.style.opacity = '0';
    el.style.width = storedW + 'px';
    el.style.height = storedH + 'px';
    let attempt = 0;
    const MAX = 1;
    resizeRetryTimer = setInterval(() => {
      const target = containerRef.value;
      if (!target) {
        clearInterval(resizeRetryTimer!);
        resizeRetryTimer = null;
        return;
      }
      target.style.width = storedW + 'px';
      target.style.height = storedH + 'px';
      try {
        plugin?.JS_Resize?.();
      } catch (e) {
        console.error('[HIK] JS_Resize 失败', e);
      }
      const r = target.getBoundingClientRect();
      console.log('[HIK] JS_Resize 重试', attempt + 1, '/', MAX, ' 实测=', Math.round(r.width), 'x', Math.round(r.height));
      attempt++;
      if (attempt >= MAX) {
        clearInterval(resizeRetryTimer!);
        resizeRetryTimer = null;
        requestAnimationFrame(() => {
          const t = containerRef.value;
          if (!t) return;
          t.style.width = '';
          t.style.height = '';
          t.style.opacity = '';
        });
      }
    }, 100);
  }

  /** 原生全屏事件兜底(JS_FullScreenDisplay 内部走 requestFullscreen,退出时 document.fullscreenElement 变 null)。 */
  function onDocFullscreen() {
    if (!document.fullscreenElement) onResizeToStored();
  }

  /** 播放(实时 mode=0 / 回放 mode=1) */
  function play(payload: {
    url: string;
    wndIndex?: number;
    mode?: 0 | 1;
    startTime?: string; // ISO8601 含时区,回放必填
    endTime?: string;
    token?: string;
  }) {
    if (!plugin) return Promise.reject(new Error('播放器未就绪'));
    const { url, wndIndex = 0, mode = 0, startTime, endTime, token } = payload;
    // config 对齐 demo playbackStart(L476):{playURL, mode, keepDecoder, token?};
    // 回放(mode=1)靠第 4/5 参 startTime/endTime 定范围,不加 PlayBackMode——
    // 之前误加 PlayBackMode:1,demo 标准回放不带它(只有倒放 reversekStart 带 PlayBackMode:3)。
    const config: any = { playURL: url, mode, keepDecoder: 0 };
    if (token) config.token = token;
    return plugin.JS_Play(url, config, wndIndex, startTime, endTime);
  }

  function stop(wndIndex: number) {
    return plugin?.JS_Stop(wndIndex);
  }

  function stopAll() {
    return plugin?.JS_StopRealPlayAll();
  }

  function arrange(splitNum: number) {
    return plugin?.JS_ArrangeWindow(splitNum);
  }

  /** 双击窗口 → 通知父组件(用最近选中窗口作目标);父组件决定放大/恢复 */
  function onDblClick() {
    emit('wndDblClick', selectedIdx);
  }

  /**
   * 截图:h5player V2.5.1 JS_CapturePicture(index, name, type) 返回 Promise,
   * 截图由 SDK 内部保存(浏览器环境触发下载),resolve 不返回 base64(对照 demo.html:598)。
   * 之前误用 4 参 callback 版 → callback 不触发 → Promise 永远 pending → 点截图无反应、无提示。
   */
  function capture(wndIndex: number, fileName = 'capture') {
    if (!plugin) return Promise.reject(new Error('播放器未就绪'));
    return plugin.JS_CapturePicture(wndIndex, fileName, 'JPEG');
  }

  /** 选中指定窗口 */
  function selectWnd(idx: number) {
    return plugin?.JS_SelectWnd(idx);
  }

  /** 整体全屏(h5player V2.5.1 方法是 JS_FullScreenDisplay,非 JS_FullScreen;不存在时 fallback 容器原生全屏) */
  function fullScreen() {
    // 进全屏前容器绝对是正常 flex 尺寸(用户点按钮这一刻),记下供退出时强制还原。
    // 不在 windowFullCcreenChange(bFull=true)里记——那个回调触发时 SDK 可能已把容器拉成全屏高度,会记错。
    const r = containerRef.value?.getBoundingClientRect();
    if (r) {
      storedW = Math.round(r.width);
      storedH = Math.round(r.height);
      console.log('[HIK] 进全屏前记录 stored=', storedW, 'x', storedH);
    }
    if (plugin?.JS_FullScreenDisplay) return plugin.JS_FullScreenDisplay(true);
    containerRef.value?.requestFullscreen?.();
  }

  /** 开始录像(MP4,typeCode=5;对照 demo.html:621 JS_StartSaveEx) */
  function startRecord(wndIndex: number, fileName: string) {
    if (!plugin) return Promise.reject(new Error('播放器未就绪'));
    return plugin.JS_StartSaveEx(wndIndex, fileName, 5, {
      irecordType: 1,
      cbStreamCB: (data: any) => console.log('[HIK] record stream cb', data),
    });
  }

  /** 停止录像(对照 demo.html:631 JS_StopSave) */
  function stopRecord(wndIndex: number) {
    if (!plugin) return Promise.reject(new Error('播放器未就绪'));
    return plugin.JS_StopSave(wndIndex);
  }

  onMounted(async () => {
    try {
      await loadScript();
      initPlugin();
      // 退出全屏重排走 onResizeToStored(强制应用进入前记录的尺寸);window resize/fullscreenchange 兜底。
      window.addEventListener('resize', onResizeToStored);
      document.addEventListener('fullscreenchange', onDocFullscreen);
    } catch (e) {
      console.error('[HIK] 初始化失败', e);
    }
  });

  onBeforeUnmount(() => {
    window.removeEventListener('resize', onResizeToStored);
    document.removeEventListener('fullscreenchange', onDocFullscreen);
    if (resizeRetryTimer) {
      clearInterval(resizeRetryTimer);
      resizeRetryTimer = null;
    }
    try {
      plugin?.JS_StopRealPlayAll();
    } catch {
      /* ignore */
    }
    plugin = null;
  });

  // 分屏切换(父组件改 currentSplit → 自动 arrange)
  watch(
    () => props.currentSplit,
    (n) => {
      if (plugin && n) plugin.JS_ArrangeWindow(n);
    }
  );

  defineExpose({ play, stop, stopAll, arrange, capture, selectWnd, fullScreen, startRecord, stopRecord });
</script>

<style lang="less" scoped>
  .hik-player-wrap {
    position: relative;
    width: 100%;
    height: 100%;
    background: #000;
  }

  .hik-player-container {
    width: 100%;
    height: 100%;
  }

  .hik-player-tip {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    color: #cfe4ff;
    font-size: 12px;
    text-align: center;
    width: 80%;
  }
  /* vite watcher 触发重编译 */
</style>
