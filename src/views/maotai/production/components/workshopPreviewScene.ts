import * as THREE from 'three';
import type { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js';

export interface WorkshopIndicator {
  title: string;
  liveValue?: string;
  status?: string;
  totalCap?: number;
  zone?: number;
  brand?: string;
  [key: string]: any;
}

export interface WorkshopContent {
  tankMeshes: THREE.Group[];
  roots: THREE.Object3D[];
  zoneLabelsNear: THREE.Object3D[];
  zoneLabelsFar: THREE.Object3D[];
}

export interface WorkshopEnvironment {
  envMap: THREE.Texture;
  cornerPoles: THREE.Object3D[];
}

const WS0 = { radius: 1.35, height: 2.5, spacing: 4.6, baseY: 0.06 };
const WS1 = { radius: 1.35, height: 2.5, spacing: 4.6, baseY: 0.06 };
const WS2 = { radius: 1.15, height: 2.3, spacing: 3.6, baseY: 0.06 };
const BRAND_POOL = ['飞天茅台', '茅台王子酒', '茅台迎宾酒', '汉酱酒', '赖茅酒', '仁酒'];

function disposeMaterial(material: THREE.Material, envMap?: THREE.Texture, textures = new Set<THREE.Texture>()) {
  Object.values(material).forEach((value: any) => {
    if (value?.isTexture && value !== envMap) textures.add(value);
  });
  material.dispose();
}

export function disposeObjectTrees(objects: THREE.Object3D[], envMap?: THREE.Texture) {
  const geometries = new Set<THREE.BufferGeometry>();
  const materials = new Set<THREE.Material>();
  const textures = new Set<THREE.Texture>();
  objects.forEach((root) => root.traverse((object: any) => {
    if (object.geometry?.isBufferGeometry) geometries.add(object.geometry);
    if (object.material) {
      const list = Array.isArray(object.material) ? object.material : [object.material];
      list.forEach((material: THREE.Material) => materials.add(material));
    }
  }));
  materials.forEach((material) => disposeMaterial(material, envMap, textures));
  textures.forEach((texture) => texture.dispose());
  geometries.forEach((geometry) => geometry.dispose());
}

function wallTexture() {
  const canvas = document.createElement('canvas'); canvas.width = 128; canvas.height = 128;
  const ctx = canvas.getContext('2d')!;
  ctx.fillStyle = '#4A5466'; ctx.fillRect(0, 0, 128, 128);
  for (let i = 0; i < 128; i += 8) {
    const shade = Math.sin(i / 8 * Math.PI);
    ctx.fillStyle = `rgba(255,255,255,${0.05 + shade * 0.04})`; ctx.fillRect(i, 0, 4, 128);
    ctx.fillStyle = 'rgba(0,0,0,0.06)'; ctx.fillRect(i + 4, 0, 4, 128);
  }
  ctx.fillStyle = 'rgba(0,0,0,0.14)'; ctx.fillRect(0, 63, 128, 2);
  const texture = new THREE.CanvasTexture(canvas);
  texture.wrapS = texture.wrapT = THREE.RepeatWrapping;
  return texture;
}

function cementTexture() {
  const canvas = document.createElement('canvas'); canvas.width = 256; canvas.height = 256;
  const ctx = canvas.getContext('2d')!;
  ctx.fillStyle = '#323A48'; ctx.fillRect(0, 0, 256, 256);
  for (let i = 0; i < 4000; i++) {
    const value = Math.random() > 0.5 ? 255 : 0;
    ctx.fillStyle = `rgba(${value},${value},${value},${Math.random() * 0.06})`;
    ctx.fillRect(Math.random() * 256, Math.random() * 256, 1.5, 1.5);
  }
  const texture = new THREE.CanvasTexture(canvas);
  texture.wrapS = texture.wrapT = THREE.RepeatWrapping;
  return texture;
}

function windowTexture() {
  const canvas = document.createElement('canvas'); canvas.width = 128; canvas.height = 128;
  const ctx = canvas.getContext('2d')!;
  ctx.fillStyle = '#141C28'; ctx.fillRect(0, 0, 128, 128);
  ctx.fillStyle = '#5E7E9C'; ctx.fillRect(10, 10, 108, 108);
  ctx.fillStyle = 'rgba(255,255,255,0.14)'; ctx.fillRect(10, 10, 108, 34);
  ctx.fillStyle = '#141C28'; ctx.fillRect(60, 10, 4, 108); ctx.fillRect(10, 60, 108, 4);
  return new THREE.CanvasTexture(canvas);
}

function createEnvironmentMap(renderer: THREE.WebGLRenderer) {
  const envScene = new THREE.Scene();
  envScene.background = new THREE.Color(0x080810);
  envScene.add(new THREE.Mesh(new THREE.PlaneGeometry(200, 200), new THREE.MeshBasicMaterial({ color: 0x181C22, side: THREE.DoubleSide })).translateY(25).rotateX(Math.PI / 2));
  envScene.add(new THREE.Mesh(new THREE.PlaneGeometry(200, 50), new THREE.MeshBasicMaterial({ color: 0x302820, side: THREE.DoubleSide })).translateX(35).translateY(12).rotateY(-Math.PI / 2));
  envScene.add(new THREE.Mesh(new THREE.PlaneGeometry(200, 50), new THREE.MeshBasicMaterial({ color: 0x080808, side: THREE.DoubleSide })).translateX(-35).translateY(12).rotateY(Math.PI / 2));
  envScene.add(new THREE.Mesh(new THREE.PlaneGeometry(200, 50), new THREE.MeshBasicMaterial({ color: 0x101418, side: THREE.DoubleSide })).translateZ(-35).translateY(12));
  envScene.add(new THREE.Mesh(new THREE.PlaneGeometry(200, 50), new THREE.MeshBasicMaterial({ color: 0x202830, side: THREE.DoubleSide })).translateZ(35).translateY(12));
  envScene.add(new THREE.Mesh(new THREE.PlaneGeometry(200, 200), new THREE.MeshBasicMaterial({ color: 0x000000, side: THREE.DoubleSide })).rotateX(Math.PI / 2).translateY(-5));
  for (let x = -10; x <= 10; x += 20) {
    const strip = new THREE.Mesh(new THREE.PlaneGeometry(30, 0.3), new THREE.MeshBasicMaterial({ color: 0x48505A, side: THREE.DoubleSide }));
    strip.position.set(x, 24, 0); strip.rotation.x = Math.PI / 2; envScene.add(strip);
  }
  const warmPanel = new THREE.Mesh(new THREE.PlaneGeometry(8, 4), new THREE.MeshBasicMaterial({ color: 0x3A3428, side: THREE.DoubleSide }));
  warmPanel.position.set(30, 8, 0); warmPanel.rotation.y = -Math.PI / 2; envScene.add(warmPanel);
  const pmrem = new THREE.PMREMGenerator(renderer);
  const envMap = pmrem.fromScene(envScene, 0.02).texture;
  pmrem.dispose();
  disposeObjectTrees([envScene]);
  return envMap;
}

export function createWorkshopEnvironment(scene: THREE.Scene, renderer: THREE.WebGLRenderer): WorkshopEnvironment {
  const envMap = createEnvironmentMap(renderer);
  scene.environment = envMap;
  scene.background = new THREE.Color(0x1E2532);
  scene.fog = new THREE.FogExp2(0xFFFFFF, 0.003);

  scene.add(new THREE.AmbientLight(0xFFFFFF, 0.75));
  [[0xD8E0E8, 0.8, 18, 18, 12], [0xC8D0D8, 0.7, -16, 16, 10], [0xD0D8E0, 0.7, -14, 14, -12], [0xC0C8D0, 0.6, 16, 12, -14], [0xD0D8E0, 0.5, 0, 25, 0]].forEach(([color, intensity, x, y, z]) => {
    const light = new THREE.DirectionalLight(color, intensity); light.position.set(x, y, z); scene.add(light);
  });
  scene.add(new THREE.HemisphereLight(0xFFFFFF, 0x101018, 0.35));
  const frontLight = new THREE.PointLight(0xE0D8C0, 0.15, 50); frontLight.position.set(0, 5, 18); scene.add(frontLight);

  const groundTexture = cementTexture(); groundTexture.repeat.set(24, 20);
  const ground = new THREE.Mesh(new THREE.PlaneGeometry(60, 50), new THREE.MeshStandardMaterial({ map: groundTexture, color: 0xFFFFFF, roughness: 0.95, metalness: 0.05 }));
  ground.rotation.x = -Math.PI / 2; ground.position.y = -0.02; ground.receiveShadow = true; scene.add(ground);

  const WX = 28, WZ = 24;
  const texture = wallTexture(); texture.repeat.set(20, 4);
  const wallMaterial = new THREE.MeshStandardMaterial({ map: texture, color: 0xFFFFFF, roughness: 0.82, metalness: 0.18 });
  const trimMaterial = new THREE.MeshStandardMaterial({ color: 0xFFFFFF, roughness: 0.6, metalness: 0.5 });
  const windowMaterial = new THREE.MeshStandardMaterial({ map: windowTexture(), emissive: 0x3A5878, emissiveIntensity: 0.4, roughness: 0.3, metalness: 0.1 });
  const addWall = (width: number, x: number, z: number, rotationY: number) => {
    const wall = new THREE.Mesh(new THREE.PlaneGeometry(width, 12), wallMaterial);
    wall.position.set(x, 5.5, z); wall.rotation.y = rotationY; scene.add(wall);
  };
  addWall(WX * 2, 0, -WZ, 0); addWall(WX * 2, 0, WZ, Math.PI);
  addWall(WZ * 2, -WX, 0, Math.PI / 2); addWall(WZ * 2, WX, 0, -Math.PI / 2);

  const cornerPoles: THREE.Object3D[] = [];
  [[-WX + 0.05, -WZ + 0.05], [WX - 0.05, -WZ + 0.05], [-WX + 0.05, WZ - 0.05], [WX - 0.05, WZ - 0.05]].forEach(([x, z]) => {
    const pole = new THREE.Mesh(new THREE.BoxGeometry(0.28, 12, 0.28), trimMaterial);
    pole.position.set(x, 5.5, z); scene.add(pole); cornerPoles.push(pole);
  });
  const addWindows = (fixed: number, axis: 'x' | 'z', rotationY: number) => {
    [-18, -6, 6, 18].forEach((offset) => {
      const win = new THREE.Mesh(new THREE.PlaneGeometry(3.4, 2.4), windowMaterial);
      if (axis === 'x') win.position.set(offset, 6.8, fixed); else win.position.set(fixed, 6.8, offset);
      win.rotation.y = rotationY; scene.add(win);
    });
  };
  addWindows(-WZ + 0.15, 'x', 0); addWindows(WZ - 0.15, 'x', Math.PI);
  addWindows(-WX + 0.15, 'z', Math.PI / 2); addWindows(WX - 0.15, 'z', -Math.PI / 2);
  [[0, -WZ], [0, WZ], [-WX, 0], [WX, 0]].forEach(([x, z]) => {
    const horizontal = Math.abs(z) === WZ;
    const kick = new THREE.Mesh(new THREE.BoxGeometry(horizontal ? WX * 2 : 0.5, 0.6, horizontal ? 0.3 : WZ * 2), trimMaterial);
    kick.position.set(x, 0.3, z); scene.add(kick);
  });
  [-16, 0].forEach((z) => {
    const light = new THREE.PointLight(0xFFF0CC, 0.4, 32, 1.5); light.position.set(0, 11.6, z); scene.add(light);
  });
  return { envMap, cornerPoles };
}

function tankWallMaterial(envMap: THREE.Texture) {
  return new THREE.MeshPhysicalMaterial({ color: 0xAEBCCC, transparent: true, opacity: 0.22, metalness: 0.55, roughness: 0.22, side: THREE.DoubleSide, envMap, envMapIntensity: 1.6, depthWrite: false, clearcoat: 1, clearcoatRoughness: 0.08, reflectivity: 0.75, sheen: 0.6, sheenRoughness: 0.5, sheenColor: 0xC8D8EC });
}

function liquidMaterial() {
  return new THREE.MeshPhysicalMaterial({ color: 0xDDC082, transparent: true, opacity: 0.6, roughness: 0.05, metalness: 0, side: THREE.FrontSide, depthWrite: false, emissive: 0x3A2A14, emissiveIntensity: 0.12 });
}

function liquidTopMaterial() {
  return new THREE.MeshStandardMaterial({ color: 0xE4CC94, transparent: true, opacity: 0.66, roughness: 0, metalness: 0, side: THREE.FrontSide, depthWrite: false });
}

function steelMaterial(envMap: THREE.Texture) {
  return new THREE.MeshStandardMaterial({ color: 0x3C4856, roughness: 0.45, metalness: 0.68, envMap, envMapIntensity: 0.8 });
}

function topCapMaterial(envMap: THREE.Texture) {
  return new THREE.MeshPhysicalMaterial({ color: 0x909AA8, roughness: 0.28, metalness: 0.75, envMap, envMapIntensity: 1.2, clearcoat: 0.4, clearcoatRoughness: 0.15, reflectivity: 0.6 });
}

function statusColor(status?: string) {
  if (status === 'warning' || status === 'alarm') return 0xA03030;
  if (status === 'standby' || status === 'inspection' || status === 'cleaning') return 0xA06E00;
  return 0x157015;
}

function createBrandTag(text: string, radius: number, height: number, baseY: number) {
  const canvas = document.createElement('canvas'); canvas.width = 512; canvas.height = 200;
  const ctx = canvas.getContext('2d')!;
  ctx.shadowColor = 'rgba(40,140,230,0.55)'; ctx.shadowBlur = 18; ctx.shadowOffsetY = 4;
  const corner = 22;
  ctx.fillStyle = 'rgba(40,130,225,0.88)'; ctx.beginPath();
  ctx.moveTo(corner, 2); ctx.lineTo(512 - corner, 2); ctx.arcTo(512, 2, 512, 2 + corner, corner);
  ctx.lineTo(512, 200 - corner); ctx.arcTo(512, 200, 512 - corner, 200, corner);
  ctx.lineTo(corner, 200); ctx.arcTo(2, 200, 2, 200 - corner, corner);
  ctx.lineTo(2, 2 + corner); ctx.arcTo(2, 2, 2 + corner, 2, corner); ctx.closePath(); ctx.fill();
  ctx.shadowColor = 'transparent'; ctx.strokeStyle = '#7FD4FF'; ctx.lineWidth = 2.5; ctx.stroke();
  ctx.fillStyle = '#A8E4FF'; ctx.fillRect(10, 28, 5, 144);
  ctx.font = 'bold 76px sans-serif'; ctx.fillStyle = '#111111'; ctx.textAlign = 'center'; ctx.textBaseline = 'middle'; ctx.fillText(text, 260, 102);
  const texture = new THREE.CanvasTexture(canvas); texture.minFilter = THREE.LinearFilter; texture.magFilter = THREE.LinearFilter;
  const sprite = new THREE.Sprite(new THREE.SpriteMaterial({ map: texture, transparent: true, depthTest: false, depthWrite: false, sizeAttenuation: true }));
  const width = Math.max(0.9, radius * 1.6); sprite.scale.set(width, width * 200 / 512, 1); sprite.position.y = baseY + height + 0.9; sprite.renderOrder = 12; sprite.raycast = () => {};
  return sprite;
}

function createTank(indicator: WorkshopIndicator, radius: number, height: number, baseY: number, level: number, baseScale: number, envMap: THREE.Texture) {
  const group = new THREE.Group();
  const liquidBaseY = baseY + 0.15;
  const offset = 0.25 * (baseScale || 1) + 0.05;
  const base = new THREE.Mesh(new THREE.CylinderGeometry(radius + offset, radius + offset + 0.05, 0.1, 48), new THREE.MeshStandardMaterial({ color: 0x28323E, roughness: 0.55, metalness: 0.5 }));
  base.position.y = baseY + 0.05; group.add(base);
  if (baseScale >= 0.8) {
    const skirtHeight = baseY > 0 ? baseY + 0.06 : 0.06;
    const skirt = new THREE.Mesh(new THREE.CylinderGeometry(radius + 0.01, radius + 0.04, skirtHeight, 48), steelMaterial(envMap));
    skirt.position.y = baseY + 0.12 + skirtHeight / 2; group.add(skirt);
  }
  const liquidHeight = Math.max(0.01, height * Math.min(1, Math.max(0, level)));
  const liquid = new THREE.Mesh(new THREE.CylinderGeometry(radius, radius, liquidHeight, 36, 1, true), liquidMaterial());
  liquid.position.y = liquidBaseY + liquidHeight / 2; liquid.renderOrder = 0; liquid.name = 'liquidBody'; group.add(liquid);
  if (level > 0.01) {
    const top = new THREE.Mesh(new THREE.CircleGeometry(radius, 36), liquidTopMaterial());
    top.rotation.x = -Math.PI / 2; top.position.y = liquidBaseY + liquidHeight; top.renderOrder = 0; group.add(top);
  }
  const wall = new THREE.Mesh(new THREE.CylinderGeometry(radius + 0.015, radius + 0.015, height, 64), tankWallMaterial(envMap));
  wall.position.y = liquidBaseY + height / 2; wall.renderOrder = 1; wall.name = 'tankWall'; group.add(wall);
  const hoop = new THREE.Mesh(new THREE.TorusGeometry(radius + 0.02, 0.03, 8, 48), steelMaterial(envMap));
  hoop.rotation.x = Math.PI / 2; hoop.position.y = liquidBaseY; group.add(hoop);
  const coneHeight = height * 0.18;
  const cone = new THREE.Mesh(new THREE.CylinderGeometry(radius * 0.06, radius, coneHeight, 48), topCapMaterial(envMap));
  cone.position.y = liquidBaseY + height + coneHeight / 2; group.add(cone);
  const ring = new THREE.Mesh(new THREE.TorusGeometry(radius + 0.06, 0.06, 12, 48), new THREE.MeshBasicMaterial({ color: statusColor(indicator.status), toneMapped: false }));
  ring.rotation.x = Math.PI / 2; ring.position.y = liquidBaseY; ring.name = 'statusRing'; group.add(ring);
  if (!indicator.title.includes('收集')) {
    const match = /(\d+)/.exec(indicator.title);
    const brand = indicator.brand || BRAND_POOL[match ? parseInt(match[1]) % BRAND_POOL.length : 0];
    const tag = createBrandTag(brand, radius, height, liquidBaseY); tag.name = 'brandTag'; group.add(tag);
  }
  group.userData.modelHeight = height;
  return group;
}

function createGroundLabel(text: string) {
  const canvas = document.createElement('canvas'); canvas.width = 512; canvas.height = 192;
  const ctx = canvas.getContext('2d')!; ctx.font = 'bold 100px sans-serif'; ctx.fillStyle = '#000000'; ctx.textAlign = 'center'; ctx.textBaseline = 'middle'; ctx.fillText(text, 256, 96);
  const texture = new THREE.CanvasTexture(canvas); texture.minFilter = THREE.LinearFilter; texture.magFilter = THREE.LinearFilter; texture.anisotropy = 8;
  const label = new THREE.Mesh(new THREE.PlaneGeometry(2.2, 0.825), new THREE.MeshBasicMaterial({ map: texture, transparent: true, depthWrite: false, side: THREE.DoubleSide }));
  label.rotation.x = -Math.PI / 2; label.renderOrder = 1; label.raycast = () => {};
  return label;
}

function buildSkid(indicator: WorkshopIndicator | undefined, envMap: THREE.Texture) {
  const group = new THREE.Group();
  const frame = new THREE.MeshStandardMaterial({ color: 0x586473, metalness: 0.78, roughness: 0.4, envMap, envMapIntensity: 0.6 });
  const tank = new THREE.MeshStandardMaterial({ color: 0xC8CDD2, metalness: 0.5, roughness: 0.3 });
  const silver = new THREE.MeshStandardMaterial({ color: 0xA0A8B0, metalness: 0.7, roughness: 0.2 });
  const dark = new THREE.MeshStandardMaterial({ color: 0x222222, metalness: 0.6, roughness: 0.3 });
  const width = 0.16, depth = 0.42, baseHeight = 0.02, columnHeight = 0.32;
  const addBox = (x: number, y: number, z: number, w: number, h: number, d: number, material = frame) => {
    const mesh = new THREE.Mesh(new THREE.BoxGeometry(w, h, d), material); mesh.position.set(x, y, z); group.add(mesh);
  };
  addBox(-width / 2 + 0.01, baseHeight / 2, 0, 0.018, baseHeight, depth);
  addBox(width / 2 - 0.01, baseHeight / 2, 0, 0.018, baseHeight, depth);
  [-depth / 2 + 0.01, 0, depth / 2 - 0.01].forEach((z) => addBox(0, baseHeight / 2, z, width, baseHeight, 0.018));
  [-1, 1].forEach((sx) => [-1, 1].forEach((sz) => addBox(sx * (width / 2 - 0.01), baseHeight + columnHeight / 2, sz * (depth / 2 - 0.015), 0.018, columnHeight, 0.018)));
  const topY = baseHeight + columnHeight;
  [-width / 2 + 0.01, width / 2 - 0.01].forEach((x) => addBox(x, topY, 0, 0.018, 0.012, depth));
  [-depth / 2 + 0.015, depth / 2 - 0.015].forEach((z) => addBox(0, topY, z, width, 0.012, 0.018));
  const tankRadius = 0.04, tankLength = 0.34, tankY = baseHeight + tankRadius + 0.01;
  [-width / 4 + 0.005, width / 4 - 0.005].forEach((x) => {
    const body = new THREE.Mesh(new THREE.CylinderGeometry(tankRadius, tankRadius, tankLength, 18), tank); body.rotation.x = Math.PI / 2; body.position.set(x, tankY, 0); group.add(body);
    [-tankLength / 2, tankLength / 2].forEach((z, index) => {
      const cap = new THREE.Mesh(new THREE.SphereGeometry(tankRadius, 14, 10, 0, Math.PI * 2, 0, Math.PI / 2.5), tank); cap.rotation.x = index ? Math.PI / 2 : -Math.PI / 2; cap.position.set(x, tankY, z); group.add(cap);
    });
  });
  const lightZ = depth / 2 - 0.04, lightBaseY = topY + 0.04;
  const post = new THREE.Mesh(new THREE.CylinderGeometry(0.008, 0.008, 0.1, 6), silver); post.position.set(0, lightBaseY + 0.05, lightZ); group.add(post);
  const holder = new THREE.Mesh(new THREE.CylinderGeometry(0.024, 0.03, 0.02, 10), dark); holder.position.set(0, lightBaseY + 0.11, lightZ); group.add(holder);
  const color = statusColor(indicator?.status);
  const bulb = new THREE.Mesh(new THREE.SphereGeometry(0.028, 14, 14), new THREE.MeshBasicMaterial({ color, toneMapped: false })); bulb.position.set(0, lightBaseY + 0.11, lightZ); group.add(bulb);
  const glow = new THREE.Mesh(new THREE.SphereGeometry(0.055, 14, 14), new THREE.MeshBasicMaterial({ color, transparent: true, opacity: indicator?.status === 'warning' ? 0.5 : 0.24, toneMapped: false })); glow.position.copy(bulb.position); group.add(glow);
  const ring = new THREE.Mesh(new THREE.TorusGeometry(tankRadius + 0.01, 0.008, 8, 24), new THREE.MeshBasicMaterial({ color, toneMapped: false })); ring.rotation.x = Math.PI / 2; ring.position.y = baseHeight + 0.005; ring.name = 'statusRing'; group.add(ring);
  group.userData.modelHeight = columnHeight;
  return group;
}

export function createWorkshopContent(scene: THREE.Scene, indicators: WorkshopIndicator[], wsIndex: number, envMap: THREE.Texture): WorkshopContent {
  const tankMeshes: THREE.Group[] = [];
  const roots: THREE.Object3D[] = [];
  const zoneLabelsNear: THREE.Object3D[] = [];
  const zoneLabelsFar: THREE.Object3D[] = [];
  const addRoot = (object: THREE.Object3D) => { scene.add(object); roots.push(object); };
  const addZone = (text: string, x: number, nearZ: number, farZ: number) => {
    const near = createGroundLabel(text); near.position.set(x, 0.02, nearZ); addRoot(near);
    const far = createGroundLabel(text); far.position.set(x, 0.02, farZ); if (farZ < 0) far.rotation.z = Math.PI; addRoot(far);
    (nearZ >= 0 ? zoneLabelsNear : zoneLabelsFar).push(near);
    (farZ >= 0 ? zoneLabelsNear : zoneLabelsFar).push(far);
  };
  const addTank = (index: number, radius: number, height: number, baseY: number, x: number, z: number, baseScale = 1) => {
    const indicator = indicators[index]; if (!indicator) return;
    const level = Math.min(1, Math.max(0, parseFloat(indicator.liveValue || '0') / 100));
    const tank = createTank(indicator, radius, height, baseY, level, baseScale, envMap);
    tank.position.set(x, 0, z); tank.userData = { ...tank.userData, tankIndex: index, indicator, tankId: indicator.title, totalCap: indicator.totalCap || 0 };
    addRoot(tank); tankMeshes.push(tank);
  };

  if (wsIndex === 0) {
    const { radius: R, height: H, spacing: SP, baseY: BY } = WS0;
    const shiftX = -SP * 1.2;
    for (let i = 0; i < Math.min(20, indicators.length); i++) addTank(i, R, H, BY, (i % 5 - 2) * SP + shiftX, (Math.floor(i / 5) - 1.5) * SP);
    ['一罐区', '二罐区', '三罐区', '四罐区', '五罐区'].forEach((name, column) => addZone(name, (column - 2) * SP + shiftX, 1.5 * SP + SP * 0.62, -1.5 * SP - SP * 0.62));
    const rowLength = 3 * SP, offsetX = 3 * SP + shiftX;
    const mediumR = R * 0.38, mediumH = H * 0.45;
    for (let i = 20; i < Math.min(28, indicators.length); i++) addTank(i, mediumR, mediumH, BY, offsetX, -rowLength / 2 + (i - 20) * rowLength / 7, 0.4);
    const smallR = R * 0.28, smallH = H * 0.35;
    for (let i = 28; i < Math.min(39, indicators.length); i++) {
      const index = i - 28; addTank(i, index < 2 ? mediumR : smallR, index < 2 ? mediumH : smallH, BY, offsetX + SP * 0.55, -rowLength / 2 + index * rowLength / 10, 0.3);
    }
    addZone('六罐区', offsetX, rowLength / 2 + 0.9, -rowLength / 2 - 0.9);
    addZone('七罐区', offsetX + SP * 0.55, rowLength / 2 + 0.9, -rowLength / 2 - 0.9);
    const skidX = offsetX + SP * 0.55 + SP * 0.7;
    for (let i = 39; i < Math.min(54, indicators.length); i++) {
      const skid = buildSkid(indicators[i], envMap); skid.position.set(skidX, 0, -rowLength / 2 + (i - 39) * rowLength / 14);
      skid.userData = { ...skid.userData, tankIndex: i, indicator: indicators[i], tankId: indicators[i].title };
      addRoot(skid); tankMeshes.push(skid);
    }
    addZone('酒撬系统', skidX, rowLength / 2 + 0.9, -rowLength / 2 - 0.9);
  } else if (wsIndex === 1) {
    const { radius: R, height: H, spacing: SP, baseY: BY } = WS1;
    for (let i = 0; i < Math.min(24, indicators.length); i++) addTank(i, R, H, BY, (i % 6 - 2.5) * SP, (Math.floor(i / 6) - 1.5) * SP);
    ['一罐区', '二罐区', '三罐区', '四罐区', '五罐区', '六罐区'].forEach((name, column) => addZone(name, (column - 2.5) * SP, 1.5 * SP + SP * 0.62, -1.5 * SP - SP * 0.62));
    const smallR = R * 0.25, smallH = H * 0.3, frontZ = -1.5 * SP - R - 2.4 - smallR;
    const col2X = (2 - 2.5) * SP, col4X = (4 - 2.5) * SP;
    for (let index = 0; index < 6; index++) addTank(27 + index, smallR, smallH, BY, col2X + index * (col4X - col2X) / 5, frontZ);
    addZone('收集酒罐', (col2X + col4X) / 2, 1.5 * SP + 0.9, frontZ - 0.9);
    addTank(33, R * 0.38, H * 0.4, BY, ((1 - 2.5) * SP + (2 - 2.5) * SP) / 2, frontZ);
  } else if (wsIndex === 2) {
    const { radius: R, height: H, spacing: SP, baseY: BY } = WS2;
    for (let i = 0; i < Math.min(60, indicators.length); i++) addTank(i, R, H, BY, (i % 10 - 4.5) * SP, (Math.floor(i / 10) - 2.5) * SP);
    ['一罐区', '二罐区', '三罐区', '四罐区', '五罐区', '六罐区', '七罐区', '八罐区', '九罐区', '十罐区'].forEach((name, column) => addZone(name, (column - 4.5) * SP, 2.5 * SP + SP * 0.62, -2.5 * SP - SP * 0.62));
    const waterR = R * 0.45, waterH = H * 0.45, waterX = -4.5 * SP - R - 0.8 - waterR, waterZ = 2.5 * SP;
    addTank(60, waterR, waterH, BY, waterX, waterZ);
    if (indicators[60]) addZone('纯水暂存罐', waterX, waterZ + 0.9, -waterZ - 0.9);
  } else {
    const R = 0.8, H = 2, BY = 0.06, SP = 2.4, gap = 4, zone1Columns = 7, zone2Columns = 10;
    const totalWidth = (zone1Columns - 1) * SP + gap + (zone2Columns - 1) * SP;
    const offset1 = -totalWidth / 2, offset2 = offset1 + (zone1Columns - 1) * SP + gap;
    for (let i = 0; i < Math.min(34, indicators.length); i++) {
      const indicator = indicators[i]; let x: number, z: number;
      if (indicator.zone === 0) {
        const column = i % zone1Columns, row = Math.floor(i / zone1Columns); x = offset1 + (row === 0 ? column : zone1Columns - 1 - column) * SP; z = (0.5 - row) * SP;
      } else {
        const index = i - 14, column = index % zone2Columns, row = Math.floor(index / zone2Columns); x = offset2 + column * SP; z = (0.5 - row) * SP;
      }
      addTank(i, R, H, BY, x, z);
    }
    addZone('一罐区', offset1 + (zone1Columns - 1) * SP / 2, 0.5 * SP + SP * 0.62, -0.5 * SP - SP * 0.62);
    addZone('二罐区', offset2 + (zone2Columns - 1) * SP / 2, 0.5 * SP + SP * 0.62, -0.5 * SP - SP * 0.62);
  }
  return { tankMeshes, roots, zoneLabelsNear, zoneLabelsFar };
}

export function applyWorkshopCamera(camera: THREE.PerspectiveCamera, controls: OrbitControls, wsIndex: number) {
  // 平视低机位(用户定稿):机位降到罐顶高度,视野上沿落在墙/窗内不露黑边;60 罐车间稍高稍远
  if (wsIndex === 0) camera.position.set(0, 12, 25.5);
  else if (wsIndex === 1) camera.position.set(0, 12, 25.5);
  else if (wsIndex === 2) camera.position.set(0, 17, 27);
  else camera.position.set(0, 12, 25.5);
  const ty = wsIndex === 3 ? 2.2 : 1.9;
  controls.target.set(0, ty, 0); camera.lookAt(controls.target); controls.update();
}

export function updateWorkshopVisibility(camera: THREE.PerspectiveCamera, content: WorkshopContent, cornerPoles: THREE.Object3D[]) {
  const cameraZ = camera.position.z;
  content.zoneLabelsNear.forEach((label) => { label.visible = Math.sign(label.position.z) === Math.sign(cameraZ); });
  content.zoneLabelsFar.forEach((label) => { label.visible = Math.sign(label.position.z) === Math.sign(cameraZ); });
  const ranked = cornerPoles.map((pole) => {
    const dx = pole.position.x - camera.position.x, dz = pole.position.z - cameraZ;
    return { pole, distance: dx * dx + dz * dz };
  }).sort((a, b) => a.distance - b.distance);
  ranked.forEach(({ pole }, index) => { pole.visible = index >= 2; });
}
