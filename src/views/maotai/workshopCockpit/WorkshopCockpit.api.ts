import { defHttp } from '/@/utils/http/axios';

export interface TransferItem {
  name: string;
  shortName?: string;
  planned: number;
  actual: number;
}

export interface TransferPlan {
  completion: number;
  variance: number;
  axisMax: number;
  unit: string;
  items: TransferItem[];
}

export interface InventoryItem {
  name: string;
  percentage: number;
  quantity: number;
  color?: string;
}

export interface InventoryData {
  total: number;
  unit: string;
  items: InventoryItem[];
}

export interface MaterialRow {
  brand: string;
  tank: string;
  batch: string;
  inventory: number;
  status: 'normal' | 'warning' | 'alarm' | string;
  updatedAt?: string;
}

export interface MaterialStatus {
  unit: string;
  rows: MaterialRow[];
}

export interface EnergyItem {
  name: string;
  value: number;
  unit: string;
  plan: number;
  perUnit: string;
  state: 'normal' | 'warning' | 'alarm' | string;
}

export interface TankRisk {
  running: number;
  idle: number;
  cleaning: number;
  alarm: number;
  loadingRate: number;
}

export interface EnvironmentData {
  temperature: number;
  humidity: number;
  temperatureRange: number[];
  humidityRange: number[];
  temperatureScale: number[];
  humidityScale: number[];
  temperatureWarning: number;
  temperatureAlarm: number;
  humidityWarning: number;
  humidityAlarm: number;
}

export interface TransferExecution {
  inbound: number;
  outbound: number;
  inboundPlan: number;
  outboundPlan: number;
  net: number;
  completion: number;
}

export interface TrendPoint {
  liquid: number[];
  temperature: number[];
  labels: string[];
}

export interface TrendData {
  current: string;
  liquidNormalRange: number[];
  temperatureNormalRange: number[];
  periods: Record<string, TrendPoint>;
}

export interface TankRealtimeItem {
  tankId: string;
  /** 位置编码(车间码-序号,对应设备台账,如 ZZG-001) */
  funlocCode?: string;
  brand?: string;
  levelPct?: number;
  temperatureC?: number;
  alcoholPct?: number;
  volumeM3?: number;
  capacityM3?: number;
  status?: 'normal' | 'standby' | 'warning' | 'alarm' | string;
  batchNo?: string;
  inbound?: number;
  outbound?: number;
  updatedAt?: string;
}

export interface TankRealtimeData {
  workshopIndex: number;
  updatedAt?: string;
  list: TankRealtimeItem[];
}

const baseUrl = '/maotai/cockpitCard';
const params = (workshopIndex: number) => ({ workshopIndex });

export const getMonthlyTransfer = (workshopIndex: number, month?: number) =>
  defHttp.get<TransferPlan>({ url: `${baseUrl}/monthlyTransfer`, params: { ...params(workshopIndex), month } });
export const getAnnualTransfer = (workshopIndex: number) =>
  defHttp.get<TransferPlan>({ url: `${baseUrl}/annualTransfer`, params: params(workshopIndex) });
export const getInventory = (workshopIndex: number) => defHttp.get<InventoryData>({ url: `${baseUrl}/inventory`, params: params(workshopIndex) });
export const getMaterialStatus = (workshopIndex: number) =>
  defHttp.get<MaterialStatus>({ url: `${baseUrl}/materialStatus`, params: params(workshopIndex) });
export const getEnergy = (workshopIndex: number) => defHttp.get<EnergyItem[]>({ url: `${baseUrl}/energy`, params: params(workshopIndex) });
export const getTankRisk = (workshopIndex: number) => defHttp.get<TankRisk>({ url: `${baseUrl}/tankRisk`, params: params(workshopIndex) });
export const getEnvironment = (workshopIndex: number) =>
  defHttp.get<EnvironmentData>({ url: `${baseUrl}/environment`, params: params(workshopIndex) });
export const getTransfer = (workshopIndex: number) => defHttp.get<TransferExecution>({ url: `${baseUrl}/transfer`, params: params(workshopIndex) });
export const getTrend = (workshopIndex: number) => defHttp.get<TrendData>({ url: `${baseUrl}/trend`, params: params(workshopIndex) });
/** 品牌库存趋势(日粒度,mes3_maotai_cockpit_inventory_trend) */
export const getInventoryTrend = (workshopIndex: number, startDate?: string, endDate?: string) =>
  defHttp.get<InventoryTrendPoint[]>({ url: `${baseUrl}/inventoryTrend`, params: { workshopIndex, startDate, endDate } });

export interface InventoryTrendPoint {
  date: string;
  items: Array<{ name: string; quantity: number }>;
}

export const getTankRealtime = (workshopIndex: number) =>
  defHttp.get<TankRealtimeData>({ url: '/maotai/tankRealtime/list', params: params(workshopIndex) });

/** 一次并行获取驾驶舱全部九类卡片数据。 */
export const getCockpitCards = (workshopIndex: number) =>
  Promise.allSettled([
    getMonthlyTransfer(workshopIndex),
    getAnnualTransfer(workshopIndex),
    getInventory(workshopIndex),
    getMaterialStatus(workshopIndex),
    getEnergy(workshopIndex),
    getTankRisk(workshopIndex),
    getEnvironment(workshopIndex),
    getTransfer(workshopIndex),
    getTrend(workshopIndex),
  ]);
