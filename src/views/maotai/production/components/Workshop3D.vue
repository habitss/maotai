<template>
  <div ref="containerRef" class="workshop-3d-container">
    <div class="tank-info-panel" v-if="selectedTank" :style="panelStyle">
      <div class="tip-header">
        <span class="tip-title">{{ selectedTank.title }}</span>
        <span class="tip-status" :style="{ color: statusColor(selectedTank.status) }">{{ statusText(selectedTank.status) }}</span>
      </div>
      <div class="tip-row" v-for="(p, i) in selectedTank.params" :key="i">
        <span class="label">{{ p.label }}</span><span class="val">{{ p.value }}</span>
      </div>
    </div>
    <!-- 报警罐点击弹窗:显示该罐未消除报警明细(与工艺报警页同源) -->
    <a-modal
      :open="alarmModalVisible"
      :title="`${selectedTank?.title || ''} 报警信息`"
      :footer="null"
      width="520px"
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
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js';
import {
  applyWorkshopCamera,
  createWorkshopContent,
  createWorkshopEnvironment,
  disposeObjectTrees,
  updateWorkshopVisibility,
  type WorkshopContent,
  type WorkshopEnvironment,
  type WorkshopIndicator,
} from './workshopPreviewScene';

THREE.ColorManagement.enabled = false;

const props = defineProps<{ indicators: WorkshopIndicator[]; wsIndex?: number; wsColor?: string }>();
const containerRef = ref<HTMLDivElement>();
const selectedTank = ref<any>(null);
const panelStyle = ref<Record<string, string>>({});

let renderer: THREE.WebGLRenderer;
let scene: THREE.Scene;
let camera: THREE.PerspectiveCamera;
let controls: OrbitControls;
let raycaster: THREE.Raycaster;
let mouse = new THREE.Vector2();
let content: WorkshopContent;
let environment: WorkshopEnvironment;
let hoveredTank: THREE.Group | null = null;
let selectedTankGroup: THREE.Group | null = null;
let animationId = 0;
let resizeObserver: ResizeObserver;

const STATUS_TEXT: Record<string, string> = { normal: '正常', warning: '预警', standby: '待机', alarm: '报警', inspection: '待检', cleaning: '清洗' };

function statusColor(status: string) {
  if (status === 'warning' || status === 'alarm') return '#DC2626';
  if (status === 'standby' || status === 'inspection' || status === 'cleaning') return '#F59E0B';
  return '#10B981';
}

function statusText(status: string) {
  return STATUS_TEXT[status] || status;
}

// 报警弹窗(点报警罐触发):打开时实时查罐体接口取 alarmList(与工艺报警页同源,查表)
const alarmModalVisible = ref(false);
const selectedTankAlarms = ref<any[]>([]);
const alarmLoading = ref(false);
async function openAlarmModal() {
  alarmModalVisible.value = true;
  alarmLoading.value = true;
  selectedTankAlarms.value = [];
  try {
    const wsIdx = currentWorkshopIndex();
    const res = await fetch(`/keytech-boot/maotai/tankRealtime/list?workshopIndex=${wsIdx}`, {
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

function currentWorkshopIndex() {
  return props.wsIndex ?? 0;
}

function findTankGroup(object: THREE.Object3D | null) {
  let current: THREE.Object3D | null = object;
  while (current && current.userData?.tankIndex === undefined) current = current.parent;
  return current?.userData?.tankIndex !== undefined ? current as THREE.Group : null;
}

function tankWall(group: THREE.Group | null) {
  return group?.getObjectByName('tankWall') as THREE.Mesh | undefined;
}

function setTankHighlight(group: THREE.Group | null, intensity: number) {
  const wall = tankWall(group);
  const material = wall?.material as THREE.MeshPhysicalMaterial | undefined;
  if (!material) return;
  material.emissive.setHex(0x888888);
  material.emissiveIntensity = intensity;
}

function raycastTank(event: MouseEvent) {
  const bounds = renderer.domElement.getBoundingClientRect();
  mouse.x = ((event.clientX - bounds.left) / bounds.width) * 2 - 1;
  mouse.y = -((event.clientY - bounds.top) / bounds.height) * 2 + 1;
  raycaster.setFromCamera(mouse, camera);
  const meshes: THREE.Object3D[] = [];
  content.tankMeshes.forEach((tank) => tank.traverse((child: any) => {
    if (child.isMesh && child.name !== 'pctLabel') meshes.push(child);
  }));
  const intersections = raycaster.intersectObjects(meshes, false);
  return intersections.length ? findTankGroup(intersections[0].object) : null;
}

function onPointerMove(event: MouseEvent) {
  if (!renderer || !camera || !content) return;
  const next = raycastTank(event);
  if (hoveredTank && hoveredTank !== selectedTankGroup) setTankHighlight(hoveredTank, 0);
  if (hoveredTank) hoveredTank.scale.setScalar(1);
  hoveredTank = next;
  if (hoveredTank) {
    if (hoveredTank !== selectedTankGroup) setTankHighlight(hoveredTank, 0.3);
    hoveredTank.scale.setScalar(1.03);
    renderer.domElement.style.cursor = 'pointer';
  } else {
    renderer.domElement.style.cursor = 'grab';
  }
}

function onPointerClick(event: MouseEvent) {
  if (!renderer || !camera || !content) return;
  const next = raycastTank(event);
  if (selectedTankGroup && selectedTankGroup !== hoveredTank) setTankHighlight(selectedTankGroup, 0);
  selectedTankGroup = next;
  if (!selectedTankGroup) {
    selectedTank.value = null;
    return;
  }
  setTankHighlight(selectedTankGroup, 0.4);
  // 从 props 取最新 indicator(userData 是首建快照,实时覆盖后 alarmList/status 会有旧值)
  const idx = selectedTankGroup.userData?.tankIndex;
  selectedTank.value = (props.indicators as any[])?.[idx] ?? selectedTankGroup.userData.indicator;
  updatePanelPosition(selectedTankGroup);
  // 报警罐:点击同时弹报警明细窗(查表数据已在 realtime.alarmList)
  if (String(selectedTank.value?.status) === 'alarm') { void openAlarmModal(); }
}

function updatePanelPosition(group: THREE.Group) {
  const modelHeight = Number(group.userData.modelHeight || 2.5);
  const position = new THREE.Vector3(0, modelHeight + 1.3, 0);
  group.localToWorld(position);
  position.project(camera);
  const element = containerRef.value!;
  const x = (position.x * 0.5 + 0.5) * element.clientWidth;
  const y = (-position.y * 0.5 + 0.5) * element.clientHeight;
  panelStyle.value = { transform: `translate(${x + 20}px, ${y - 100}px)` };
}

function clearSelection() {
  if (hoveredTank) hoveredTank.scale.setScalar(1);
  setTankHighlight(hoveredTank, 0);
  setTankHighlight(selectedTankGroup, 0);
  hoveredTank = null;
  selectedTankGroup = null;
  selectedTank.value = null;
}

/** 实时数据轻量刷新:原地更新状态环颜色/液位/浮层引用,不重建网格(避免选中面板被清、性能抖动) */
function applyRealtimeUpdate(next: any[]) {
  if (!content) return;
  content.tankMeshes.forEach((group: any) => {
    const index = group.userData?.tankIndex;
    if (index === undefined) return;
    const source = next[index];
    if (!source) return;
    group.userData.indicator = source;
    // 状态环颜色
    const ring = group.getObjectByName('statusRing') as THREE.Mesh | undefined;
    const ringMat = ring?.material as THREE.MeshBasicMaterial | undefined;
    if (ringMat) ringMat.color.setHex(statusColorHex(source.status));
    // 液位柱高度与液位顶面
    const wall = group.getObjectByName('tankWall') as THREE.Mesh | undefined;
    const liquid = group.getObjectByName('liquidBody') as THREE.Mesh | undefined;
    if (wall && liquid) {
      const wallH = (wall.geometry as THREE.CylinderGeometry).parameters.height;
      const liquidBaseY = wall.position.y - (wall.geometry as THREE.CylinderGeometry).parameters.height / 2;
      const pct = Math.min(1, Math.max(0, parseFloat(source.liveValue || '0') / 100 || 0));
      const h = Math.max(0.01, wallH * pct);
      const liquidH = (liquid.geometry as THREE.CylinderGeometry).parameters.height;
      liquid.scale.y = h / Math.max(0.01, liquidH);
      liquid.position.y = liquidBaseY + h / 2;
      const top = group.children.find((c: any) => c.geometry?.type === 'CircleGeometry');
      if (top) top.position.y = liquidBaseY + h;
    }
    // 该罐正被选中:浮层引用同步
    if (selectedTankGroup === group) selectedTank.value = source;
  });
}
defineExpose({ applyRealtimeUpdate });

function statusColorHex(status?: string) {
  if (status === 'warning' || status === 'alarm') return 0xA03030;
  if (status === 'standby' || status === 'inspection' || status === 'cleaning') return 0xA06E00;
  return 0x157015;
}

function rebuildWorkshopContent() {
  clearSelection();
  if (content) {
    content.roots.forEach((root) => scene.remove(root));
    disposeObjectTrees(content.roots, environment.envMap);
  }
  content = createWorkshopContent(scene, props.indicators || [], currentWorkshopIndex(), environment.envMap);
}

function buildScene() {
  const element = containerRef.value!;
  scene = new THREE.Scene();
  environment = createWorkshopEnvironment(scene, renderer);
  camera = new THREE.PerspectiveCamera(40, element.clientWidth / element.clientHeight, 0.1, 500);
  controls = new OrbitControls(camera, renderer.domElement);
  controls.enableDamping = true;
  controls.dampingFactor = 0.08;
  controls.minPolarAngle = THREE.MathUtils.degToRad(15);
  controls.maxPolarAngle = THREE.MathUtils.degToRad(75);
  controls.minDistance = 8;
  controls.maxDistance = 70;
  applyWorkshopCamera(camera, controls, currentWorkshopIndex());
  content = createWorkshopContent(scene, props.indicators || [], currentWorkshopIndex(), environment.envMap);
}

function animate() {
  animationId = requestAnimationFrame(animate);
  controls?.update();
  if (content && environment) updateWorkshopVisibility(camera, content, environment.cornerPoles);
  if (selectedTankGroup) updatePanelPosition(selectedTankGroup);
  renderer.render(scene, camera);
}

function onResize() {
  const element = containerRef.value;
  if (!element || !renderer || !camera) return;
  camera.aspect = element.clientWidth / element.clientHeight;
  camera.updateProjectionMatrix();
  renderer.setSize(element.clientWidth, element.clientHeight);
}

function init() {
  const element = containerRef.value!;
  renderer = new THREE.WebGLRenderer({ antialias: true });
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
  renderer.setSize(element.clientWidth, element.clientHeight);
  renderer.outputColorSpace = THREE.SRGBColorSpace;
  renderer.setClearColor(0x1E2532, 1);
  renderer.toneMapping = THREE.ACESFilmicToneMapping;
  renderer.toneMappingExposure = 1.2;
  element.appendChild(renderer.domElement);
  raycaster = new THREE.Raycaster();
  renderer.domElement.addEventListener('mousemove', onPointerMove);
  renderer.domElement.addEventListener('click', onPointerClick);
  buildScene();
  animate();
}

onMounted(() => {
  init();
  resizeObserver = new ResizeObserver(onResize);
  resizeObserver.observe(containerRef.value!);
});

onBeforeUnmount(() => {
  cancelAnimationFrame(animationId);
  resizeObserver?.disconnect();
  renderer?.domElement.removeEventListener('mousemove', onPointerMove);
  renderer?.domElement.removeEventListener('click', onPointerClick);
  controls?.dispose();
  if (scene) disposeObjectTrees([scene], environment?.envMap);
  environment?.envMap.dispose();
  renderer?.dispose();
});

watch([() => props.indicators, () => props.wsIndex], ([, wsIndex], [, oldWsIndex]) => {
  if (!renderer || !environment) return;
  rebuildWorkshopContent();
  if (wsIndex !== oldWsIndex) applyWorkshopCamera(camera, controls, currentWorkshopIndex());
}, { deep: true });
</script>

<style scoped>
.workshop-3d-container { width: 100%; height: 100%; position: relative; cursor: grab; }
.tank-info-panel { position: absolute; top: 0; left: 0; z-index: 10; background: rgba(255,255,255,.92); border-radius: 8px; padding: 14px 18px; min-width: 220px; box-shadow: 0 4px 20px rgba(0,0,0,.15); }
.tip-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; padding-bottom: 6px; border-bottom: 1px solid #eee; }
.tip-title { font-size: 14px; font-weight: 700; }
.tip-status { font-size: 11px; font-weight: 600; }
.tip-row { display: flex; justify-content: space-between; padding: 3px 0; font-size: 12px; }
.tip-row .label { color: #78716c; }
.tip-row .val { font-weight: 600; font-family: monospace; }
.alarm-row { display: flex; align-items: center; gap: 10px; padding: 10px 4px; border-bottom: 1px solid #f0f0f0; }
.alarm-row:last-child { border-bottom: none; }
.alarm-lv { flex-shrink: 0; padding: 2px 10px; border-radius: 10px; font-size: 12px; font-weight: 600; }
.alarm-lv.lv-3 { color: #dc2626; background: #fef2f2; }
.alarm-lv.lv-2 { color: #d97706; background: #fffbeb; }
.alarm-lv.lv-1 { color: #2563eb; background: #eff6ff; }
.alarm-desc { flex: 1; font-size: 13px; color: #222; }
.alarm-time { flex-shrink: 0; font-size: 12px; color: #999; font-family: monospace; }
.alarm-empty { text-align: center; color: #999; padding: 24px 0; }
</style>
