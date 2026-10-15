<template>
  <div ref="containerRef" class="workshop-3d-container">
    <div ref="panelRef" class="tank-info-panel" v-if="selectedTank" :style="panelStyle">
      <div class="tip-header">
        <span class="tip-title">{{ selectedTank.title }}</span>
        <span class="tip-status" :class="selectedTank.status">{{ statusText(selectedTank.status) }}</span>
      </div>
      <div class="tip-row" v-for="(p, i) in selectedTankRows" :key="`${p.label}-${i}`">
        <span class="label">{{ p.label }}</span>
        <span class="val" :class="`value-${p.s || 'normal'}`">
          {{ p.value }}<span v-if="p.sub" class="val-sub">{{ p.sub }}</span>
        </span>
      </div>
      <div v-if="selectedTankBrand" class="tip-brand">品牌: <span>{{ selectedTankBrand }}</span></div>
      <div v-if="selectedTankFunloc" class="tip-link" title="在新窗口打开设备台账详情">
        <a @click.stop="openLedgerDetail">查看设备 {{ selectedTankFunloc }} &#187;</a>
      </div>
    </div>
    <!-- 报警罐点击弹窗:该罐未消除报警明细(后端 alarmList,与工艺报警页同源) -->
    <a-modal
      :open="alarmModalVisible"
      :title="`${selectedTank?.title || ''} 报警信息`"
      :footer="null"
      width="520px"
      wrapClassName="cockpit-alarm-modal"
      @cancel="alarmModalVisible = false"
    >
      <div v-for="(a, i) in selectedTankAlarms" :key="i" class="alarm-row">
        <span class="alarm-lv" :class="`lv-${a.level || 1}`">{{ levelText(a.level) }}</span>
        <span class="alarm-desc">{{ a.desc }}</span>
        <span class="alarm-time">{{ a.time }}</span>
      </div>
      <div v-if="alarmLoading" class="alarm-empty">查询中…</div>
      <div v-else-if="!selectedTankAlarms.length" class="alarm-empty">该罐当前无未处理报警</div>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { computed, ref, onMounted, onBeforeUnmount, watch } from 'vue';
import * as THREE from 'three';
THREE.ColorManagement.enabled = false;
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js';

const props = defineProps<{ indicators: any[]; wsIndex?: number; wsColor?: string }>();
const containerRef = ref<HTMLDivElement>();
const panelRef = ref<HTMLDivElement>();
const selectedTank = ref<any>(null);
const panelStyle = ref({});
let selectedAnchorLocal: THREE.Vector3 | null = null;
 
const selectedTankRows = computed(() => {
  const indicator = selectedTank.value;
  if (!indicator) return [];
  const params = indicator.params || [];
  const actual = params.find((item: any) => item.label === '实际液位');
  const rows: Array<{ label: string; value: string; sub?: string; s?: string }> = [];
  params.forEach((item: any) => {
    if (item.label === '液位') {
      rows.push({ ...item, label: '液位', value: indicator.liveValue || item.value });
      rows.push({
        // 实际液位=合并层写入的当前酒量(volumeM3),副标题显示总容量;勿取容量行的值——容量行已是总容量,取它会与容量一样(09-17 误伤修复)
        label: '实际液位',
        value: actual?.value || '-',
        sub: indicator.totalCap ? ` / ${formatPanelNumber(indicator.totalCap)}m³` : '',
        s: actual?.s || item.s,
      });
      return;
    }
    // 容量行正常显示;实际液位行已在上面合并输出,跳过防重复
    if (item.label !== '容量' && item.label !== '实际液位') rows.push(item);
  });
  return rows;
});
const selectedTankBrand = computed(() => selectedTank.value ? indicatorBrand(selectedTank.value) : '');

// 报警弹窗(点报警罐触发):打开时实时查罐体接口取 alarmList(与工艺报警页同源,查表)
const alarmModalVisible = ref(false);
const selectedTankAlarms = ref<any[]>([]);
const alarmLoading = ref(false);
async function openAlarmModal() {
  alarmModalVisible.value = true;
  alarmLoading.value = true;
  selectedTankAlarms.value = [];
  try {
    const res = await fetch(`/keytech-boot/maotai/tankRealtime/list?workshopIndex=${props.wsIndex ?? 0}`, {
      headers: { 'X-Access-Token': JSON.parse(localStorage.getItem(Object.keys(localStorage).find((k) => k.includes('COMMON__LOCAL__KEY')) || '') || '{}')?.value?.TOKEN__?.value || '' },
    }).then((r) => r.json());
    const rec = ((res?.result?.list) || []).find((x: any) => x.tankId === selectedTank.value?.realtime?.tankId || x.tankId === selectedTank.value?.title);
    selectedTankAlarms.value = (rec?.alarmList as any[]) || [];
  } catch (e) {
    console.error('报警明细查询失败', e);
  } finally {
    alarmLoading.value = false;
  }
}
function levelText(level?: number) {
  return level === 3 ? '紧急' : level === 2 ? '警告' : '提示';
}

  const selectedTankFunloc = computed(() => selectedTank.value?.realtime?.funlocCode || '');
  function openLedgerDetail() {
    const code = selectedTankFunloc.value;
    if (code) window.open(`/mes3/#/maotai/equipmentLedger/detail?funlocCode=${encodeURIComponent(code)}`, '_blank');
  }

function formatPanelNumber(value: unknown) {
  const number = Number(value);
  return Number.isFinite(number) ? number.toFixed(1).replace(/\.0$/, '') : '-';
}

// 与 workshop.html 嵌入驾驶舱模式保持一致的三组建模常量。
const WS0_R = 1.35, WS0_H = 2.5, WS0_SP = 4.6, BY = 0.06;
const WS1_R = 1.35, WS1_H = 2.5, WS1_SP = 4.6;
const WS2_R = 1.15, WS2_H = 2.3, WS2_SP = 3.6;
const BRAND_POOL = ['飞天茅台', '茅台王子酒', '茅台迎宾酒', '汉酱酒', '赖茅酒', '仁酒'];

let renderer: THREE.WebGLRenderer;
let scene: THREE.Scene;
let camera: THREE.PerspectiveCamera;
let controls: OrbitControls;
let raycaster: THREE.Raycaster;
let mouse = new THREE.Vector2();
let tankMeshes: any[] = [];
let groundLabels: any[] = [];
let animId = 0;
let pmrem: THREE.PMREMGenerator;
let envTex: THREE.Texture;
let hoveredTank: any = null;
let selectedTankGroup: any = null;
let brushedMetalTex: THREE.CanvasTexture | undefined;
let liquidSurfaceFlowTex: THREE.CanvasTexture | undefined;
let statusPulseMeshes: THREE.Object3D[] = [];

const STATUS_TEXT: Record<string, string> = { normal: '正常', warning: '预警', standby: '待机', alarm: '报警' };
function statusText(s: string) { return STATUS_TEXT[s] || s; }

function brushedMetalTexture() {
  if (brushedMetalTex) return brushedMetalTex;
  const canvas = document.createElement('canvas'); canvas.width = 256; canvas.height = 64;
  const ctx = canvas.getContext('2d')!; const image = ctx.createImageData(canvas.width, canvas.height);
  for (let y = 0; y < canvas.height; y++) for (let x = 0; x < canvas.width; x++) {
    let grain = 166 + 9 * Math.sin(x * 0.43) + 5 * Math.sin(x * 1.37) + 2 * Math.sin((x + y * 0.18) * 2.63);
    grain = Math.max(142, Math.min(190, Math.round(grain)));
    const p = (y * canvas.width + x) * 4;
    image.data[p] = image.data[p + 1] = image.data[p + 2] = grain; image.data[p + 3] = 255;
  }
  ctx.putImageData(image, 0, 0);
  brushedMetalTex = new THREE.CanvasTexture(canvas);
  brushedMetalTex.wrapS = brushedMetalTex.wrapT = THREE.RepeatWrapping; brushedMetalTex.repeat.set(12, 1);
  return brushedMetalTex;
}
function liquidSurfaceFlowTexture() {
  if (liquidSurfaceFlowTex) return liquidSurfaceFlowTex;
  const canvas = document.createElement('canvas'); canvas.width = canvas.height = 256;
  const ctx = canvas.getContext('2d')!; ctx.fillStyle = '#B8B8B8'; ctx.fillRect(0, 0, 256, 256);
  const flow = ctx.createLinearGradient(0, 256, 256, 0);
  flow.addColorStop(0, '#AEAEAE'); flow.addColorStop(0.34, '#BCBCBC'); flow.addColorStop(0.46, '#F6F6F6');
  flow.addColorStop(0.53, '#FFFFFF'); flow.addColorStop(0.62, '#CACACA'); flow.addColorStop(1, '#AEAEAE');
  ctx.fillStyle = flow; ctx.fillRect(0, 0, 256, 256);
  liquidSurfaceFlowTex = new THREE.CanvasTexture(canvas);
  liquidSurfaceFlowTex.wrapS = liquidSurfaceFlowTex.wrapT = THREE.RepeatWrapping;
  liquidSurfaceFlowTex.repeat.set(1.35, 1.35); liquidSurfaceFlowTex.center.set(0.5, 0.5);
  liquidSurfaceFlowTex.minFilter = liquidSurfaceFlowTex.magFilter = THREE.LinearFilter;
  return liquidSurfaceFlowTex;
}

// ===== workshop.html 驾驶舱嵌入模式材质 =====
function tankWallMat() {
  const brushed = brushedMetalTexture();
  return new THREE.MeshPhysicalMaterial({
    color: 0xB8CBD4, transparent: true, opacity: 0.31, metalness: 0.62, roughness: 0.36,
    roughnessMap: brushed, bumpMap: brushed, bumpScale: 0.004, side: THREE.DoubleSide,
    envMap: envTex, envMapIntensity: 1.18, depthWrite: false,
    clearcoat: 0.42, clearcoatRoughness: 0.24, reflectivity: 0.68,
    sheen: 0.24, sheenRoughness: 0.62, sheenColor: 0xD9F2FA,
  });
}
function liquidMat(_warning: boolean) {
  return new THREE.MeshPhysicalMaterial({
    color: 0xD79A2B, transparent: true, opacity: 0.88, roughness: 0.14, metalness: 0,
    side: THREE.FrontSide, depthWrite: false, emissive: 0x7A4500, emissiveIntensity: 0.16,
    envMap: envTex, envMapIntensity: 0.55, clearcoat: 0.88, clearcoatRoughness: 0.1,
  });
}
function liquidTopMat(_warning: boolean) {
  return new THREE.MeshPhysicalMaterial({
    color: 0xE4B95F, transparent: true, opacity: 0.96, roughness: 0.08, metalness: 0,
    side: THREE.FrontSide, depthWrite: false, emissive: 0xB47719, emissiveIntensity: 0.34,
    emissiveMap: liquidSurfaceFlowTexture(), envMap: envTex, envMapIntensity: 0.84,
    clearcoat: 1, clearcoatRoughness: 0.04,
  });
}
function steelMatDark() {
  return new THREE.MeshStandardMaterial({ color: 0x6B818B, roughness: 0.46, metalness: 0.68, roughnessMap: brushedMetalTexture(), envMap: envTex, envMapIntensity: 0.78 });
}
function basePlateMat() {
  return new THREE.MeshStandardMaterial({ color: 0x132531, roughness: 0.72, metalness: 0.38, roughnessMap: brushedMetalTexture(), envMap: envTex, envMapIntensity: 0.24 });
}
function topCapMat() {
  return new THREE.MeshPhysicalMaterial({
    color: 0xB9C9CF, roughness: 0.48, metalness: 0.62, envMap: envTex, envMapIntensity: 0.72,
    clearcoat: 0.12, clearcoatRoughness: 0.42, reflectivity: 0.55,
  });
}

// ===== 标签(对齐原型) =====
function createBrandTag(text: string, r: number, h: number, baseY: number) {
  const c = document.createElement('canvas'); c.width = 512; c.height = 200;
  const ctx = c.getContext('2d')!; ctx.clearRect(0, 0, 512, 200);
  ctx.font = '900 78px "Microsoft YaHei UI","Microsoft YaHei",sans-serif';
  ctx.textAlign = 'center'; ctx.textBaseline = 'middle';
  ctx.lineJoin = 'round'; ctx.shadowColor = 'rgba(255,200,40,0.7)'; ctx.shadowBlur = 22;
  ctx.lineWidth = 12; ctx.strokeStyle = 'rgba(255,200,40,0.5)'; ctx.strokeText(text, 256, 98);
  ctx.shadowColor = 'rgba(255,200,40,0.85)'; ctx.shadowBlur = 8;
  ctx.lineWidth = 7; ctx.strokeStyle = '#FFD700'; ctx.strokeText(text, 256, 98);
  ctx.shadowColor = 'transparent'; ctx.shadowBlur = 0;
  ctx.lineWidth = 2.5; ctx.strokeStyle = '#B8860B'; ctx.strokeText(text, 256, 98);
  ctx.fillStyle = '#000000'; ctx.fillText(text, 256, 98);
  const tex = new THREE.CanvasTexture(c);
  tex.minFilter = THREE.LinearFilter; tex.magFilter = THREE.LinearFilter;
  const sp = new THREE.Sprite(new THREE.SpriteMaterial({ map: tex, transparent: true, depthTest: false, depthWrite: false, sizeAttenuation: true }));
  const w = Math.max(1.02, r * 1.76);
  sp.scale.set(w, w * (200 / 512), 1);
  sp.position.y = baseY + h + 0.9;
  sp.renderOrder = 12;
  return sp;
}
function createZoneGroundLabel(text: string) {
  const c = document.createElement('canvas'); c.width = 512; c.height = 192;
  const ctx = c.getContext('2d')!;
  ctx.font = 'bold 100px sans-serif'; ctx.fillStyle = '#F2FBFF';
  ctx.textAlign = 'center'; ctx.textBaseline = 'middle';
  ctx.shadowColor = 'rgba(20,198,255,0.82)'; ctx.shadowBlur = 14;
  ctx.fillText(text, 256, 96);
  const tex = new THREE.CanvasTexture(c);
  tex.minFilter = THREE.LinearFilter; tex.magFilter = THREE.LinearFilter;
  tex.anisotropy = 8;
  const plane = new THREE.Mesh(new THREE.PlaneGeometry(3, 1.1), new THREE.MeshBasicMaterial({ map: tex, transparent: true, side: THREE.DoubleSide, depthWrite: false }));
  plane.rotation.x = -Math.PI / 2;
  return plane;
}

// ===== 罐体建模(对齐原型 buildTank) =====
function buildTank(tankR: number, tankH: number, baseY: number, lv: number, status: string, ind: any, baseScale: number) {
  const g = new THREE.Group();
  const by = baseY + 0.15;
  const liqRatio = Math.min(1, Math.max(0, lv));
  const bS = baseScale || 1;
  const bOff = 0.25 * bS + 0.05;
  const w = status === 'warning' || status === 'alarm';

  // 底座
  const base = new THREE.Mesh(new THREE.CylinderGeometry(tankR + bOff, tankR + bOff + 0.05, 0.1, 48), basePlateMat());
  base.position.y = baseY + 0.05; g.add(base);
  // 支撑裙
  if (bS >= 0.8) {
    const skirtH = baseY > 0 ? baseY + 0.06 : 0.06;
    const skirt = new THREE.Mesh(new THREE.CylinderGeometry(tankR + 0.01, tankR + 0.04, skirtH, 48), steelMatDark());
    skirt.position.y = baseY + 0.12 + skirtH / 2; g.add(skirt);
  }
  // 液体
  const liqH = Math.max(0.01, tankH * liqRatio);
  const liq = new THREE.Mesh(new THREE.CylinderGeometry(tankR, tankR, liqH, 36, 1, true), liquidMat(w));
  liq.position.y = by + liqH / 2; liq.renderOrder = 0; liq.name = 'liquidBody'; g.add(liq);
  if (liqRatio > 0.01) {
    const top = new THREE.Mesh(new THREE.CircleGeometry(tankR, 36), liquidTopMat(w));
    top.rotation.x = -Math.PI / 2; top.position.y = by + liqH; top.renderOrder = 2; top.name = 'liquidTop';
    top.userData.liquidSurfaceBaseOpacity = (top.material as THREE.Material & { opacity: number }).opacity;
    g.add(top);
  }
  // 罐壁(透明玻璃)
  const wallR = tankR + 0.015;
  const wall = new THREE.Mesh(new THREE.CylinderGeometry(wallR, wallR, tankH, 64), tankWallMat());
  wall.position.y = by + tankH / 2; wall.renderOrder = 1; g.add(wall);
  // 底部环箍
  const hoop = new THREE.Mesh(new THREE.TorusGeometry(tankR + 0.02, 0.03, 8, 48), steelMatDark());
  hoop.rotation.x = Math.PI / 2; hoop.position.y = by; g.add(hoop);
  // 锥形封顶
  const coneH = tankH * 0.18;
  const cone = new THREE.Mesh(new THREE.CylinderGeometry(tankR * 0.06, tankR, coneH, 48), topCapMat());
  cone.position.y = by + tankH + coneH / 2; g.add(cone);
  // 状态环(底部)
  const sc = tankStatusColor(ind.status);
  const sr = new THREE.Mesh(new THREE.TorusGeometry(tankR + 0.06, 0.06, 12, 48), new THREE.MeshBasicMaterial({ color: sc, toneMapped: false }));
  sr.rotation.x = Math.PI / 2; sr.position.y = by; sr.name = 'statusRing'; registerStatusPulse(sr, ind.status, 'ring'); g.add(sr);
  // 品牌铭牌(悬浮罐顶)
  if (ind.title && ind.title.indexOf('收集') === -1) {
    const brand = indicatorBrand(ind);
    const tag = createBrandTag(brand, tankR, tankH, by); tag.name = 'brandTag'; g.add(tag);
  }
  return g;
}

// ===== 布局(对齐原型 createWorkshop0) =====
function createLayout() {
  const wsIdx = props.wsIndex || 0;
  if (wsIdx === 0) createWorkshop0();
  else if (wsIdx === 1) createWorkshop1();
  else if (wsIdx === 2) createWorkshop2();
  else if (wsIdx === 3) createWorkshop3();
}

// ===== 车间0:中转酒罐(大罐5×4 + 中罐8 + 小罐11 + 酒撬15) =====
function createWorkshop0() {
  const inds = props.indicators;
  if (!inds?.length) return;
  const R = WS0_R, H = WS0_H, SP = WS0_SP;
  const shiftX = -SP * 1.2;
  // 大罐 5×4
  for (let i = 0; i < Math.min(20, inds.length); i++) {
    const col = i % 5, row = Math.floor(i / 5);
    addTank(inds[i], R, H, (col - 2) * SP + shiftX, (row - 1.5) * SP, 1, i);
  }
  // 地面罐区标签
  const zn = ['一罐区', '二罐区', '三罐区', '四罐区', '五罐区'];
  for (let c = 0; c < 5; c++) {
    addGroundZone(zn[c], (c - 2) * SP + shiftX, 1.5 * SP + SP * 0.62, -1.5 * SP - SP * 0.62);
  }
  // 中罐(六罐区)
  const rowLen = 3 * SP, offX = 3 * SP + shiftX;
  const mR = R * 0.38, mH = H * 0.45, mSP = rowLen / 7;
  for (let i = 20; i < Math.min(28, inds.length); i++) {
    addTank(inds[i], mR, mH, offX, -rowLen / 2 + (i - 20) * mSP, 0.4, i);
  }
  // 小罐(七罐区)
  const sR = R * 0.28, sH = H * 0.35, r2SP = rowLen / 10;
  for (let i = 28; i < Math.min(39, inds.length); i++) {
    const idx = i - 28;
    addTank(inds[i], idx < 2 ? mR : sR, idx < 2 ? mH : sH, offX + SP * 0.55, -rowLen / 2 + idx * r2SP, 0.3, i);
  }
  for (const [txt, x] of [['六罐区', offX], ['七罐区', offX + SP * 0.55]] as [string, number][]) {
    addGroundZone(txt, x, rowLen / 2 + 0.9, -rowLen / 2 - 0.9);
  }
  // 酒撬系统：与 workshop.html 一样固定生成 15 套，不依赖实时接口是否返回酒撬记录。
  const skX = offX + SP * 0.55 + SP * 0.7, skSP = rowLen / 14;
  const skBlue = new THREE.MeshStandardMaterial({ color: 0x586473, metalness: 0.78, roughness: 0.4, envMap: envTex, envMapIntensity: 0.6 });
  const skTank = new THREE.MeshStandardMaterial({ color: 0xC8CDD2, metalness: 0.5, roughness: 0.3 });
  const skSilver = new THREE.MeshStandardMaterial({ color: 0xA0A8B0, metalness: 0.7, roughness: 0.2 });
  const skYellow = new THREE.MeshStandardMaterial({ color: 0xE8B800, metalness: 0.3, roughness: 0.4 });
  for (let i = 39; i < 54; i++) {
    const idx = i - 39, sz = -rowLen / 2 + idx * skSP;
    const sk = new THREE.Group();
    const skW = 0.16, skD = 0.42, baseH = 0.02, colH = 0.32, tankR = 0.04, tankLen = 0.34;
    for (const sx of [-skW / 2 + 0.01, skW / 2 - 0.01]) {
      const beam = new THREE.Mesh(new THREE.BoxGeometry(0.018, baseH, skD), skBlue); beam.position.set(sx, baseH / 2, 0); sk.add(beam);
      const topBeam = new THREE.Mesh(new THREE.BoxGeometry(0.018, 0.012, skD), skBlue); topBeam.position.set(sx, baseH + colH, 0); sk.add(topBeam);
    }
    for (const z of [-skD / 2 + 0.01, skD / 2 - 0.01, 0]) {
      const beam = new THREE.Mesh(new THREE.BoxGeometry(skW, baseH, 0.018), skBlue); beam.position.set(0, baseH / 2, z); sk.add(beam);
    }
    for (const [sx, z] of [[-skW / 2 + 0.01, -skD / 2 + 0.015], [skW / 2 - 0.01, -skD / 2 + 0.015], [-skW / 2 + 0.01, skD / 2 - 0.015], [skW / 2 - 0.01, skD / 2 - 0.015]]) {
      const column = new THREE.Mesh(new THREE.BoxGeometry(0.018, colH, 0.018), skBlue); column.position.set(sx, baseH + colH / 2, z); sk.add(column);
    }
    for (const sx of [-skW / 4 + 0.005, skW / 4 - 0.005]) {
      const tank = new THREE.Mesh(new THREE.CylinderGeometry(tankR, tankR, tankLen, 18), skTank);
      tank.rotation.x = Math.PI / 2; tank.position.set(sx, baseH + tankR + 0.01, 0); sk.add(tank);
      const gaugeStem = new THREE.Mesh(new THREE.CylinderGeometry(0.006, 0.006, 0.02, 6), skSilver);
      gaugeStem.position.set(sx, baseH + tankR * 2 + 0.02, -tankLen * 0.15); sk.add(gaugeStem);
    }
    const pipe = new THREE.Mesh(new THREE.CylinderGeometry(0.007, 0.007, skD * 0.8, 8), skYellow);
    pipe.rotation.x = Math.PI / 2; pipe.position.set(0, baseH + colH + 0.015, 0); sk.add(pipe);
    const status = inds[i]?.status || 'normal'; const statusValue = tankStatusColor(status);
    const pole = new THREE.Mesh(new THREE.CylinderGeometry(0.008, 0.008, 0.1, 6), skSilver); pole.position.set(0, baseH + colH + 0.09, skD / 2 - 0.04); sk.add(pole);
    const bulb = new THREE.Mesh(new THREE.SphereGeometry(0.028, 14, 14), new THREE.MeshBasicMaterial({ color: statusValue, toneMapped: false }));
    bulb.position.set(0, baseH + colH + 0.15, skD / 2 - 0.04); registerStatusPulse(bulb, status, 'bulb'); sk.add(bulb);
    const glow = new THREE.Mesh(new THREE.SphereGeometry(0.055, 14, 14), new THREE.MeshBasicMaterial({ color: statusValue, transparent: true, opacity: status === 'warning' || status === 'alarm' ? 0.5 : status === 'standby' ? 0.36 : 0.24, toneMapped: false }));
    glow.position.copy(bulb.position); registerStatusPulse(glow, status, 'glow'); sk.add(glow);
    if (inds[i]) sk.userData = { tankIndex: i, indicator: inds[i], tankId: inds[i].title };
    sk.position.set(skX, 0, sz); scene.add(sk); tankMeshes.push(sk);
  }
  addGroundZone('酒撬系统', skX, rowLen / 2 + 0.9, -rowLen / 2 - 0.9);
}

function indicatorBrand(ind: any) {
  const match = String(ind?.title || '').match(/(\d+)/);
  return ind?.realtime?.brand || ind?.brand || BRAND_POOL[match ? (parseInt(match[1]) % BRAND_POOL.length) : 0];
}

// ===== 车间1:系列酒勾调(主罐6×4 + 收集酒罐6 + 纯水罐1) =====
function createWorkshop1() {
  const inds = props.indicators;
  if (!inds?.length) return;
  const R = WS1_R, H = WS1_H, SP = WS1_SP;
  for (let i = 0; i < Math.min(24, inds.length); i++) {
    const col = i % 6, row = Math.floor(i / 6);
    addTank(inds[i], R, H, (col - 2.5) * SP, (row - 1.5) * SP, 1, i);
  }
  const zn1 = ['一罐区', '二罐区', '三罐区', '四罐区', '五罐区', '六罐区'];
  for (let c = 0; c < 6; c++) addGroundZone(zn1[c], (c - 2.5) * SP, 1.5 * SP + SP * 0.62, -1.5 * SP - SP * 0.62);
  const sR = R * 0.25, sH = H * 0.3, frontZ = -1.5 * SP - R - 2.4 - sR;
  const col2x = (2 - 2.5) * SP, col4x = (4 - 2.5) * SP;
  for (let ci = 0; ci < 6; ci++) {
    const si = 27 + ci;
    if (si >= inds.length) break;
    addTank(inds[si], sR, sH, col2x + ci * (col4x - col2x) / 5, frontZ, 0.3, si);
  }
  addGroundZone('收集酒罐', (col2x + col4x) / 2, 1.5 * SP + 0.9, frontZ - 0.9);
  if (33 < inds.length) {
    addTank(inds[33], R * 0.38, H * 0.4, ((1 - 2.5) * SP + (2 - 2.5) * SP) / 2, frontZ, 0.4, 33);
  }
}

// ===== 车间2:成品酒罐房(主罐10×6 + V1003纯水罐) =====
function createWorkshop2() {
  const inds = props.indicators;
  if (!inds?.length) return;
  const R2 = WS2_R, H2 = WS2_H, SP2 = WS2_SP, COLS = 10;
  for (let i = 0; i < Math.min(60, inds.length); i++) {
    const col = i % COLS, row = Math.floor(i / COLS);
    addTank(inds[i], R2, H2, (col - 4.5) * SP2, (row - 2.5) * SP2, 1, i);
  }
  const zn2 = ['一罐区', '二罐区', '三罐区', '四罐区', '五罐区', '六罐区', '七罐区', '八罐区', '九罐区', '十罐区'];
  for (let c = 0; c < COLS; c++) addGroundZone(zn2[c], (c - 4.5) * SP2, 2.5 * SP2 + SP2 * 0.62, -2.5 * SP2 - SP2 * 0.62);
  if (60 < inds.length) {
    const vR = R2 * 0.45, vH = H2 * 0.45, vX = (0 - 4.5) * SP2 - R2 - 0.8 - vR, vZ = (5 - 2.5) * SP2;
    addTank(inds[60], vR, vH, vX, vZ, 0.45, 60);
    addGroundZone('纯水暂存罐', vX, vZ + 0.9, -vZ - 0.9);
  }
}

// ===== 车间3:高位酒罐区(一区2×7蛇形 + 二区2×10) =====
function createWorkshop3() {
  const inds = props.indicators;
  if (!inds?.length) return;
  const R3 = 0.8, H3 = 2.0, SP3 = 2.4, GAP = 4, Z1 = 7, Z2 = 10;
  const totalW = (Z1 - 1) * SP3 + GAP + (Z2 - 1) * SP3;
  const offX1 = -totalW / 2, offX2 = offX1 + (Z1 - 1) * SP3 + GAP;
  for (let i = 0; i < Math.min(34, inds.length); i++) {
    const ind = inds[i];
    let x: number, z: number;
    if (ind.zone === 0) {
      const col = i % Z1, row = Math.floor(i / Z1);
      x = offX1 + (row === 0 ? col : (Z1 - 1 - col)) * SP3;
      z = (0.5 - row) * SP3;
    } else {
      const idx2 = i - 14, col = idx2 % Z2, row = Math.floor(idx2 / Z2);
      x = offX2 + col * SP3;
      z = (0.5 - row) * SP3;
    }
    addTank(ind, R3, H3, x, z, 1, i);
  }
  addGroundZone('一罐区', offX1 + (Z1 - 1) * SP3 / 2, 0.5 * SP3 + SP3 * 0.62, -0.5 * SP3 - SP3 * 0.62);
  addGroundZone('二罐区', offX2 + (Z2 - 1) * SP3 / 2, 0.5 * SP3 + SP3 * 0.62, -0.5 * SP3 - SP3 * 0.62);
}

function addGroundZone(text: string, x: number, zNear: number, zFar: number) {
  const near = createZoneGroundLabel(text); near.position.set(x, 0.02, zNear); near.userData.zside = zNear >= 0 ? 1 : -1;
  const far = createZoneGroundLabel(text); far.position.set(x, 0.02, zFar); far.userData.zside = zFar >= 0 ? 1 : -1;
  if (zFar < 0) far.rotation.z = Math.PI;
  groundLabels.push(near, far); scene.add(near, far);
}

function addTank(ind: any, tankR: number, tankH: number, x: number, z: number, bs: number, idx: number) {
  const lv = parseLevel(ind.liveValue);
  const g = buildTank(tankR, tankH, BY, lv, ind.status || 'normal', ind, bs);
  g.position.set(x, 0, z);
  g.userData = { tankIndex: idx, indicator: ind, tankId: ind.title, totalCap: ind.totalCap || 0 };
  scene.add(g); tankMeshes.push(g);
}

// ===== 场景初始化 =====
function buildScene() {
  scene = new THREE.Scene();
  scene.background = null;
  try {
    pmrem = new THREE.PMREMGenerator(renderer);
    const envS = new THREE.Scene();
    envS.background = new THREE.Color(0x03162F);
    envS.add(new THREE.Mesh(new THREE.PlaneGeometry(200, 200), new THREE.MeshBasicMaterial({ color: 0x181c22 })).translateY(25).rotateX(Math.PI / 2));
    envS.add(new THREE.Mesh(new THREE.PlaneGeometry(200, 50), new THREE.MeshBasicMaterial({ color: 0x302820 })).translateX(35).translateY(12).rotateY(-Math.PI / 2));
    envS.add(new THREE.Mesh(new THREE.PlaneGeometry(200, 50), new THREE.MeshBasicMaterial({ color: 0x080808 })).translateX(-35).translateY(12).rotateY(Math.PI / 2));
    envS.add(new THREE.Mesh(new THREE.PlaneGeometry(200, 50), new THREE.MeshBasicMaterial({ color: 0x101418 })).translateZ(-35).translateY(12));
    envS.add(new THREE.Mesh(new THREE.PlaneGeometry(200, 50), new THREE.MeshBasicMaterial({ color: 0x202830 })).translateZ(35).translateY(12));
    // 地板(深黑)
    envS.add(new THREE.Mesh(new THREE.PlaneGeometry(200, 200), new THREE.MeshBasicMaterial({ color: 0x000000 })).rotateX(Math.PI / 2).translateY(-5));
    // 窄光带(天花板,产生窄高光反射)
    for (let ei = -10; ei <= 10; ei += 20) {
      const strip = new THREE.Mesh(new THREE.PlaneGeometry(30, 0.3), new THREE.MeshBasicMaterial({ color: 0x48505A }));
      strip.position.set(ei, 24, 0); strip.rotation.x = Math.PI / 2; envS.add(strip);
    }
    // 右侧暖面板
    const warmPanel = new THREE.Mesh(new THREE.PlaneGeometry(8, 4), new THREE.MeshBasicMaterial({ color: 0x3A3428 }));
    warmPanel.position.set(30, 8, 0); warmPanel.rotation.y = -Math.PI / 2; envS.add(warmPanel);
    envTex = pmrem.fromScene(envS, 0.02).texture;
    scene.environment = envTex;
  } catch (e) {}
  const el = containerRef.value!;
  camera = new THREE.PerspectiveCamera(40, el.clientWidth / el.clientHeight, 0.1, 500);
  const wsIdx = props.wsIndex || 0;
  if (wsIdx === 0) camera.position.set(0, 12, 25.5);
  else if (wsIdx === 1) camera.position.set(0, 12, 25.5);
  else if (wsIdx === 2) camera.position.set(0, 17, 32.9);
  else camera.position.set(0, 12, 25.5);
  controls = new OrbitControls(camera, renderer.domElement);
  controls.enableDamping = true; controls.dampingFactor = 0.08;
  controls.minPolarAngle = THREE.MathUtils.degToRad(15); controls.maxPolarAngle = THREE.MathUtils.degToRad(75);
  controls.minDistance = 8; controls.maxDistance = 70; controls.target.set(0, 1.2, 0);
  scene.add(new THREE.AmbientLight(0x31536A, 0.24));
  scene.add(new THREE.HemisphereLight(0x79B6D2, 0x07111D, 0.34));
  const keyLight = new THREE.DirectionalLight(0xD8F3FF, 0.92); keyLight.position.set(16, 22, 18); scene.add(keyLight);
  const fillLight = new THREE.DirectionalLight(0x6FA8C4, 0.3); fillLight.position.set(-18, 10, 14); scene.add(fillLight);
  const rimLight = new THREE.DirectionalLight(0x45C9FF, 0.52); rimLight.position.set(-16, 14, -20); scene.add(rimLight);
  const capLight = new THREE.DirectionalLight(0xEAFBFF, 0.2); capLight.position.set(2, 26, -2); scene.add(capLight);
  const frontFill = new THREE.PointLight(0x7AD8FF, 0.12, 55, 1.6); frontFill.position.set(0, 7, 20); scene.add(frontFill);
  tankMeshes = [];
  createLayout();
  applyEmbedCameraComposition();
}

function applyEmbedCameraComposition() {
  const el = containerRef.value;
  if (!camera || !el || !el.clientWidth || !el.clientHeight) return;
  camera.setViewOffset(el.clientWidth, el.clientHeight, 0, Math.round(el.clientHeight * 0.1), el.clientWidth, el.clientHeight);
}

function init() {
  const el = containerRef.value!;
  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
  renderer.setPixelRatio(Math.min(Math.max(window.devicePixelRatio || 1, 2), 2.5));
  renderer.setSize(el.clientWidth, el.clientHeight);
  renderer.outputColorSpace = THREE.SRGBColorSpace;
  renderer.setClearColor(0x000000, 0);
  renderer.toneMapping = THREE.ACESFilmicToneMapping;
  renderer.toneMappingExposure = 0.86;
  renderer.domElement.style.background = 'transparent';
  el.appendChild(renderer.domElement);
  raycaster = new THREE.Raycaster();
  renderer.domElement.addEventListener('mousemove', onPointerMove);
  renderer.domElement.addEventListener('click', onPointerClick);
  buildScene();
  animate();
}

// ===== 交互(对齐原型 onPointerMove/Click) =====
function getTankGroup(obj: any): any {
  let o = obj;
  while (o && o.userData && o.userData.tankIndex === undefined) o = o.parent;
  return o && o.userData && o.userData.tankIndex !== undefined ? o : null;
}
function onPointerMove(e: MouseEvent) {
  if (!renderer || !camera) return;
  const rect = renderer.domElement.getBoundingClientRect();
  mouse.x = ((e.clientX - rect.left) / rect.width) * 2 - 1;
  mouse.y = -((e.clientY - rect.top) / rect.height) * 2 + 1;
  raycaster.setFromCamera(mouse, camera);
  const shells: any[] = [];
  tankMeshes.forEach((t) => t.traverse((ch: any) => { if (ch.isMesh) shells.push(ch); }));
  const its = raycaster.intersectObjects(shells, false);
  if (hoveredTank) { if (hoveredTank.children[1]) hoveredTank.children[1].material.emissiveIntensity = 0; hoveredTank.scale.setScalar(1); }
  if (its.length > 0) {
    const g = getTankGroup(its[0].object);
    if (g) {
      hoveredTank = g;
      if (g.children[1]) { g.children[1].material.emissive = new THREE.Color(0x888888); g.children[1].material.emissiveIntensity = 0.3; }
      g.scale.setScalar(1.03);
      renderer.domElement.style.cursor = 'pointer';
      return;
    }
  }
  hoveredTank = null;
  renderer.domElement.style.cursor = 'grab';
}
function onPointerClick(e: MouseEvent) {
  if (!renderer || !camera) return;
  const rect = renderer.domElement.getBoundingClientRect();
  mouse.x = ((e.clientX - rect.left) / rect.width) * 2 - 1;
  mouse.y = -((e.clientY - rect.top) / rect.height) * 2 + 1;
  raycaster.setFromCamera(mouse, camera);
  const shells: any[] = [];
  tankMeshes.forEach((t) => t.traverse((ch: any) => { if (ch.isMesh) shells.push(ch); }));
  const its = raycaster.intersectObjects(shells, false);
  if (selectedTankGroup) { if (selectedTankGroup.children[1]) selectedTankGroup.children[1].material.emissiveIntensity = 0; const pr = selectedTankGroup.getObjectByName('statusRing'); if (pr) pr.material.emissiveIntensity = 0; }
  if (its.length > 0) {
    const g = getTankGroup(its[0].object);
    if (g) {
      selectedTankGroup = g;
      // 以本次真正点中的罐体表面作为浮层锚点，避免预设罐顶坐标在
      // 透视相机与 viewOffset 共同作用下产生偏移。
      selectedAnchorLocal = g.worldToLocal(its[0].point.clone());
      if (g.children[1]) { g.children[1].material.emissive = new THREE.Color(0xaaaaaa); g.children[1].material.emissiveIntensity = 0.4; }
      const ring = g.getObjectByName('statusRing');
      if (ring) ring.material.emissiveIntensity = 1.0;
      // 从 props 取最新 indicator(userData 是首建快照,实时覆盖后 alarmList/status 会有旧值)
      const ti = g.userData?.tankIndex;
      selectedTank.value = (props.indicators as any[])?.[ti] ?? g.userData.indicator;
      if (String(selectedTank.value?.status) === 'alarm') { void openAlarmModal(); }
      updatePanelPos(g);
      return;
    }
  }
  selectedTankGroup = null;
  selectedAnchorLocal = null;
  selectedTank.value = null;
}
function updatePanelPos(g: any) {
  const pos = selectedAnchorLocal?.clone() || new THREE.Vector3(0, BY + 0.03 + H, 0);
  g.localToWorld(pos);
  pos.project(camera);
  const el = containerRef.value!;
  const sx = (pos.x * 0.5 + 0.5) * el.clientWidth;
  const sy = (-pos.y * 0.5 + 0.5) * el.clientHeight;
  const panelWidth = panelRef.value?.offsetWidth || 240;
  const panelHeight = panelRef.value?.offsetHeight || 190;
  const anchorGap = 18;
  let x = sx + anchorGap;
  // 详情框顶部略高于点击点，使它紧邻罐体但不会遮挡罐体主体。
  let y = sy - 30;
  if (x + panelWidth > el.clientWidth - 10) x = sx - panelWidth - anchorGap;
  x = Math.max(10, Math.min(el.clientWidth - panelWidth - 10, x));
  y = Math.max(10, Math.min(el.clientHeight - panelHeight - 10, y));
  panelStyle.value = { transform: `translate(${x}px, ${y}px)` };
}

function parseLevel(v: string): number {
  if (!v) return 0;
  const n = parseFloat(v.replace('%', ''));
  return isNaN(n) ? 0 : n / 100;
}
function animate(time = 0) {
  animId = requestAnimationFrame(animate);
  controls?.update();
  const cameraSide = Math.sign(camera.position.z) || 1;
  groundLabels.forEach((label) => { label.visible = Math.sign(label.position.z) === cameraSide; });
  statusPulseMeshes.forEach((mesh: any) => {
    const cfg = mesh.userData.statusPulse; if (!cfg || !mesh.material) return;
    const duration = cfg.type === 'alarm' ? 5200 : 6200;
    const pulse = (Math.sin(time * Math.PI * 2 / duration + cfg.phase) + 1) * 0.5;
    const minOpacity = cfg.type === 'alarm' ? 0.58 : 0.74;
    const maxOpacity = cfg.type === 'pending' ? 0.95 : cfg.baseOpacity;
    mesh.material.opacity = minOpacity + (maxOpacity - minOpacity) * pulse;
    mesh.scale.setScalar(1 + (cfg.type === 'alarm' ? 0.035 : 0.018) * pulse);
  });
  if (selectedTankGroup) updatePanelPos(selectedTankGroup);
  renderer.render(scene, camera);
}
function onResize() {
  if (!containerRef.value || !renderer) return;
  const w = containerRef.value.clientWidth, h = containerRef.value.clientHeight;
  camera.aspect = w / h; camera.updateProjectionMatrix();
  renderer.setPixelRatio(Math.min(Math.max(window.devicePixelRatio || 1, 2), 2.5));
  renderer.setSize(w, h);
  applyEmbedCameraComposition();
}
let resizeObserver: ResizeObserver;
onMounted(() => {
  init();
  resizeObserver = new ResizeObserver(onResize);
  resizeObserver.observe(containerRef.value!);
});
onBeforeUnmount(() => {
  cancelAnimationFrame(animId);
  resizeObserver?.disconnect();
  renderer.domElement.removeEventListener('mousemove', onPointerMove);
  renderer.domElement.removeEventListener('click', onPointerClick);
  scene?.traverse((o: any) => { if (o.geometry) o.geometry.dispose(); if (o.material) { Array.isArray(o.material) ? o.material.forEach((m: any) => m.dispose()) : o.material.dispose(); } });
  renderer?.dispose(); pmrem?.dispose();
});
function disposeObject(object: THREE.Object3D) {
  object.traverse((child: any) => {
    child.geometry?.dispose?.();
    const materials = Array.isArray(child.material) ? child.material : child.material ? [child.material] : [];
    materials.forEach((material: any) => {
      if (material.map) material.map.dispose?.();
      material.dispose?.();
    });
  });
}

function tankStatusColor(status: string) {
  if (status === 'warning' || status === 'alarm') return 0xFF3B4E;
  if (status === 'standby' || status === 'inspection' || status === 'cleaning') return 0xFFC13A;
  return 0x00D000;
}
function registerStatusPulse(mesh: THREE.Object3D, status: string, kind = 'ring') {
  const material = (mesh as any).material;
  const type = status === 'warning' || status === 'alarm' ? 'alarm' :
    status === 'standby' || status === 'inspection' || status === 'cleaning' ? 'pending' : '';
  if (!type || !material) return;
  material.transparent = true; material.depthWrite = false;
  mesh.userData.statusPulse = { type, kind, baseOpacity: material.opacity, phase: statusPulseMeshes.length * 0.47 };
  statusPulseMeshes.push(mesh);
}
watch([() => props.indicators, () => props.wsIndex], () => {
  if (!renderer) return;
  tankMeshes.forEach((tank) => { scene.remove(tank); disposeObject(tank); });
  groundLabels.forEach((label) => { scene.remove(label); disposeObject(label); });
  tankMeshes = []; groundLabels = []; statusPulseMeshes = []; hoveredTank = null; selectedTankGroup = null; selectedTank.value = null;
  createLayout();
}, { deep: true });
</script>

<style scoped>
.workshop-3d-container { width: 100%; height: 100%; position: relative; cursor: grab; }
.tank-info-panel { position:absolute; top:0; left:0; z-index:230; min-width:240px; padding:16px 20px; color:#ddf6ff; pointer-events:none; background:rgba(4,22,44,.94); border:1px solid rgba(56,217,255,.45); border-radius:12px; box-shadow:0 18px 48px rgba(0,0,0,.45),0 0 24px rgba(28,181,245,.14); backdrop-filter:blur(8px) saturate(1.2); }
.tip-header { display:flex; align-items:center; justify-content:space-between; gap:10px; padding-bottom:10px; margin-bottom:10px; border-bottom:1px solid rgba(255,255,255,.12); }
.tip-title { color:#ddf6ff; font-size:15px; font-weight:700; }
.tip-status { padding:2px 8px; border-radius:3px; font-size:11px; font-weight:600; }
.tip-status.normal { color:#45d795; background:rgba(16,185,129,.16); }
.tip-status.standby { color:#ffc13a; background:rgba(242,181,42,.16); }
.tip-status.warning,.tip-status.alarm { color:#ff5b66; background:rgba(239,68,68,.17); }
.tip-row { display:flex; align-items:center; justify-content:space-between; gap:24px; padding:5px 0; font-size:13px; }
.tip-row .label { color:#9fc2d8; }
.tip-row .val { color:#ddf6ff; font-weight:600; font-family:'JetBrains Mono',Consolas,monospace; }
.tip-row .val-sub { margin-left:3px; color:#82b8d8; font-size:12px; font-weight:400; }
.tip-row .val.value-warning,.tip-row .val.value-standby { color:#ffc13a; }
.tip-row .val.value-alarm { color:#ff5b66; }
.cockpit-alarm-modal .alarm-row { display:flex; align-items:center; gap:10px; padding:10px 4px; border-bottom:1px solid #f0f0f0; }
.cockpit-alarm-modal .alarm-row:last-child { border-bottom:none; }
.cockpit-alarm-modal .alarm-lv { flex-shrink:0; padding:2px 10px; border-radius:10px; font-size:12px; font-weight:600; }
.cockpit-alarm-modal .alarm-lv.lv-3 { color:#dc2626; background:#fef2f2; }
.cockpit-alarm-modal .alarm-lv.lv-2 { color:#d97706; background:#fffbeb; }
.cockpit-alarm-modal .alarm-lv.lv-1 { color:#2563eb; background:#eff6ff; }
.cockpit-alarm-modal .alarm-desc { flex:1; font-size:13px; color:#222; }
.cockpit-alarm-modal .alarm-time { flex-shrink:0; font-size:12px; color:#999; font-family:monospace; }
.cockpit-alarm-modal .alarm-empty { text-align:center; color:#999; padding:24px 0; }
.tip-link { margin-top: 8px; padding-top: 8px; border-top: 1px dashed rgba(255,255,255,.15); pointer-events: auto; }
.tip-link a { color: #38d9ff; font-size: 12px; cursor: pointer; text-decoration: none; }
.tip-link a:hover { text-decoration: underline; }
.tip-brand { margin-top:8px; padding-top:8px; border-top:1px solid rgba(255,255,255,.1); color:#82b8d8; font-size:12px; }
.tip-brand span { color:#43dcff; font-weight:600; }
</style>
