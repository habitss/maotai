<template>
  <a-modal
    :open="open"
    title="视频联动监控"
    :width="760"
    :footer="null"
    :destroy-on-close="true"
    wrap-class-name="video-link-modal"
    @cancel="onClose"
  >
    <div class="video-preview">
      <HikPlayer ref="playerRef" :current-split="1" @error="onPlayError" />
      <div v-if="!loading && !playing" class="video-tip">等待取流…(host/AK/SK 未配置时无画面)</div>
      <!-- 顶部覆盖:摄像头编号 + 时间 -->
      <div class="overlay overlay-top">
        <div class="cam-label"
          ><div class="rec-dot"></div><span>{{ alarm?.cameraIndexCode }}</span></div
        >
        <div class="cam-time">{{ alarm?.time }}</div>
      </div>
      <!-- 底部覆盖:位置 + 云台 -->
      <div class="overlay overlay-bottom">
        <div class="cam-info">{{ alarm?.cameraName }} · 实时画面</div>
        <div v-auth="'maotai:alarm:ptz'" class="cam-ptz">
          <a-button shape="circle" size="small" ghost @click="msg('云台左转(待对接 PTZ)')">◀</a-button>
          <a-button shape="circle" size="small" ghost @click="msg('云台上转(待对接 PTZ)')">▲</a-button>
          <a-button shape="circle" size="small" ghost @click="msg('云台下转(待对接 PTZ)')">▼</a-button>
          <a-button shape="circle" size="small" ghost @click="msg('云台右转(待对接 PTZ)')">▶</a-button>
        </div>
      </div>
      <div v-if="loading" class="loading-mask"><a-spin tip="取流中…" /></div>
    </div>
    <div class="video-actions">
      <a-button v-auth="'maotai:alarm:video'" block @click="gotoMonitor">📹 全屏监控</a-button>
      <a-button v-auth="'maotai:alarm:snapshot'" block @click="onCapture">📷 截图</a-button>
      <a-button v-auth="'maotai:alarm:record'" block @click="onRecord">{{ isRecording ? '⏹ 停止录像' : '⏺ 录像' }}</a-button>
    </div>
  </a-modal>
</template>

<script lang="ts" name="video-link-modal" setup>
  import { ref, watch } from 'vue';
  import dayjs from 'dayjs';
  import { useRouter } from 'vue-router';
  import { message } from 'ant-design-vue';
  import HikPlayer from '/@/views/maotai/monitor/components/HikPlayer.vue';
  import { getPreviewUrl } from '/@/views/maotai/monitor/MonitorCenter.api';
  import type { AlarmRecord } from '../alarm.mock';

  const props = defineProps<{ open: boolean; alarm: AlarmRecord | null }>();
  const emit = defineEmits<{ (e: 'update:open', v: boolean): void }>();
  const router = useRouter();

  const playerRef = ref<InstanceType<typeof HikPlayer>>();
  const loading = ref(false);
  const playing = ref(false);
  const isRecording = ref(false);

  function msg(t: string) {
    message.info(t);
  }

  /** 跳监控中心页,带 cameraIndexCode 让目标页自动定位播放该摄像头 */
  function gotoMonitor() {
    const code = props.alarm?.cameraIndexCode;
    router.push({ path: '/video-monitor', query: code ? { cameraIndexCode: code } : {} });
    emit('update:open', false); // 关弹窗
  }

  /** 截图当前播放窗口(弹窗单画面,固定窗口 0;无流时直接提示) */
  async function onCapture() {
    if (!playing.value) {
      message.warning('当前未播放视频,无法截图(host/AK/SK 未配或取流失败时无画面)');
      return;
    }
    try {
      await playerRef.value?.capture(0, dayjs().format('YYYYMMDDHHmmss'));
      message.success('已截图');
    } catch (e: any) {
      message.warning('截图失败: ' + (e?.message || e));
    }
  }

  /** 录像 toggle:未播提示;在播则开始/停止录像(JS_StartSaveEx / JS_StopSave) */
  async function onRecord() {
    if (!playing.value) {
      message.warning('当前未播放视频,无法录像(host/AK/SK 未配或摄像头编码无效)');
      return;
    }
    try {
      if (!isRecording.value) {
        await playerRef.value?.startRecord(0, dayjs().format('YYYYMMDDHHmmss') + '.mp4');
        isRecording.value = true;
        message.success('录像已开始,再次点击停止');
      } else {
        await playerRef.value?.stopRecord(0);
        isRecording.value = false;
        message.success('录像已停止,文件保存中');
      }
    } catch (e: any) {
      isRecording.value = false;
      message.warning('录像失败: ' + (e?.message || e));
    }
  }

  /** 取流并播放;播放器未就绪(刚挂载)时退避重试 */
  async function startPlay(retry = 0) {
    if (!props.alarm) return;
    // 等播放器实例挂载(modal destroyOnClose 下每次重新挂载)
    if (!playerRef.value) {
      if (retry < 15) return setTimeout(() => startPlay(retry + 1), 200);
      return message.error('播放器初始化超时');
    }
    try {
      const r = await getPreviewUrl(props.alarm.cameraIndexCode);
      const url = r?.url;
      if (!url) {
        message.warning('未取到取流 URL(后端 hikvision host/AK/SK 是否配置?)');
        return;
      }
      loading.value = true;
      await playerRef.value.play({ url, mode: 0 });
      playing.value = true;
    } catch (e: any) {
      // HikPlayer.play 在 plugin 未就绪时 reject "播放器未就绪" → 退避重试
      if (e?.message === '播放器未就绪' && retry < 15) {
        return setTimeout(() => startPlay(retry + 1), 300);
      }
      message.error('视频联动失败: ' + (e?.message || e));
    } finally {
      loading.value = false;
    }
  }

  function onClose() {
    try {
      if (isRecording.value) playerRef.value?.stopRecord(0);
      playerRef.value?.stopAll();
    } catch {
      /* ignore */
    }
    playing.value = false;
    isRecording.value = false;
    emit('update:open', false);
  }

  function onPlayError(info: { idx: number; code: number }) {
    message.error(`窗口${info.idx} 播放异常 code=${info.code}`);
  }

  watch(
    () => props.open,
    (v) => {
      if (v) {
        playing.value = false;
        setTimeout(startPlay, 200); // 等 modal body 渲染
      } else {
        if (isRecording.value) playerRef.value?.stopRecord(0);
        isRecording.value = false;
        playerRef.value?.stopAll();
      }
    }
  );
</script>

<style lang="less" scoped>
  .video-preview {
    position: relative;
    width: 100%;
    aspect-ratio: 16 / 9;
    background: #0a0a0a;
    border-radius: 6px;
    overflow: hidden;
  }
  .video-preview :deep(.hik-player-wrap) {
    position: absolute;
    inset: 0;
  }
  .video-tip {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    color: #cfe4ff;
    font-size: 12px;
    z-index: 1;
  }
  .overlay {
    position: absolute;
    left: 0;
    right: 0;
    padding: 10px 14px;
    display: flex;
    justify-content: space-between;
    z-index: 2;
    pointer-events: none;
  }
  .overlay-top {
    top: 0;
    align-items: flex-start;
    background: linear-gradient(to bottom, rgba(0, 0, 0, 0.6), transparent);
  }
  .overlay-bottom {
    bottom: 0;
    align-items: flex-end;
    background: linear-gradient(to top, rgba(0, 0, 0, 0.6), transparent);
  }
  .cam-label {
    display: flex;
    align-items: center;
    gap: 6px;
    color: #fff;
    font-size: 12px;
    font-weight: 600;
  }
  .rec-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #ef4444;
    animation: blink-rec 1s step-end infinite;
  }
  @keyframes blink-rec {
    0%,
    100% {
      opacity: 1;
    }
    50% {
      opacity: 0;
    }
  }
  .cam-time {
    color: rgba(255, 255, 255, 0.7);
    font-size: 11px;
    font-family: 'JetBrains Mono', monospace;
  }
  .cam-info {
    color: rgba(255, 255, 255, 0.6);
    font-size: 11px;
  }
  .cam-ptz {
    display: flex;
    gap: 6px;
    pointer-events: auto;
    :deep(.ant-btn) {
      color: #fff;
      border-color: rgba(255, 255, 255, 0.4);
      background: rgba(255, 255, 255, 0.1);
    }
  }
  .loading-mask {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(0, 0, 0, 0.4);
    z-index: 3;
  }
  .video-actions {
    display: flex;
    gap: 8px;
    margin-top: 12px;
  }
</style>
