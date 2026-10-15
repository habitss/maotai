/**
 * 浏览器语音合成(TTS)封装 —— 工艺报警按级别语音播报用。
 * <p>项目原本零音频/语音基建(speechSynthesis/Audio/mp3/wav 全无),这里从零搭。
 *
 * <p><b>串行单通道 + Chrome 卡死坑:</b>speechSynthesis 同一时刻只播一条,且 Chrome 在
 * 长队列(连续 speak 多条未消费)时会卡死不发声(已知 bug)。故 speakAlarm 每次 speak 前
 * 先 {@link SpeechSynthesis#cancel} 取消上一条,保证"每轮只播最高级别一条"+ "持续重复时
 * 每轮覆盖上一条不堆队列"。调用方只需每轮调一次 speakAlarm(最高级别那条),不用管队列。
 *
 * <p><b>静音:</b>speechSynthesis 无音量调节(只有 rate/pitch/volume,但 volume 对部分
 * 中文语音无效),故静音 = 模块 muted 标志 + cancel 当前播报 + 跳过新播报。值班人接电话时
 * 点静音,灯仍闪(见 AlarmCenter toggleMute)。
 */
import { LEVEL_TEXT, type AlarmLevel } from './alarm.mock';

/** 静音标志(模块级,AlarmCenter 静音按钮调 setter 同步) */
let muted = false;

export function setSpeechMuted(m: boolean) {
  muted = m;
}

export function isSpeechMuted() {
  return muted;
}

/**
 * 选一个中文语音。Chrome/Edge 首次 speechSynthesis.getVoices() 返回空,需等 voiceschanged;
 * 这里每次都重新查(异步加载完后自然命中),选不到就不设(用 lang='zh-CN' 让浏览器自选)。
 */
function pickZhVoice(): SpeechSynthesisVoice | null {
  if (typeof window === 'undefined' || !('speechSynthesis' in window)) return null;
  const voices = window.speechSynthesis.getVoices();
  return voices.find((v) => /^zh(-|_)/i.test(v.lang)) || null;
}

// 触发浏览器加载 voices(有些浏览器需主动调一次 getVoices + 监听 voiceschanged 才会填充)
if (typeof window !== 'undefined' && 'speechSynthesis' in window) {
  window.speechSynthesis.getVoices();
}

/**
 * 播报一条报警。文案 = "{级别}报警,{车间},{内容}"。
 * @param level    报警级别(决定文案前缀:紧急/警告/提示)
 * @param content  报警内容(alarmDesc,作为正文)
 * @param workshop 车间名(可选,拼在级别和内容之间便于定位)
 */
export function speakAlarm(level: AlarmLevel, content: string, workshop?: string) {
  if (muted) {
    console.debug('[Alarm][speech] 跳过(静音)');
    return;
  }
  if (typeof window === 'undefined' || !('speechSynthesis' in window)) {
    console.warn('[Alarm][speech] 浏览器不支持 speechSynthesis');
    return;
  }
  const synth = window.speechSynthesis;
  const text = `${LEVEL_TEXT[level]}报警,${workshop ? workshop + ',' : ''}${content}`;
  console.debug('[Alarm][speech] 播报:', text);
  // 串行:新播报前先取消未播完的,防堆队列卡死(Chrome speechSynthesis 已知 bug)。
  // ⚠ Chrome 竞态坑:cancel() 后立即 speak() 会把刚提交的也吞掉 → 必须 setTimeout 延迟一拍
  synth.cancel();
  setTimeout(() => {
    const u = new SpeechSynthesisUtterance(text);
    u.lang = 'zh-CN';
    u.rate = 1.0;
    const zhVoice = pickZhVoice();
    if (zhVoice) u.voice = zhVoice; // 选中文语音(没选到就靠 lang 让浏览器自选)
    synth.speak(u);
    // 自动播放策略排查:页面无用户交互时 speak 会被浏览器静默拦,pending 队列会非空
    setTimeout(() => {
      const st = synth.speaking || synth.pending;
      if (!st) console.warn('[Alarm][speech] 未发声(speaking & pending 都 false)—— 多半浏览器自动播放策略拦截,点一下页面再试');
    }, 200);
  }, 60);
}

/** 停止播报(切页面/静音时调) */
export function stopSpeech() {
  if (typeof window !== 'undefined' && 'speechSynthesis' in window) {
    window.speechSynthesis.cancel();
  }
}

