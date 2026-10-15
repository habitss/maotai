import { ref } from 'vue';
import { TagKindEnum } from '@/enums/EmsEnum';

// 下拉常量定义
export interface DictItem {
  value: number;
  label: string;
}

// KPI点位
export interface KpiTag {
  id?: number;
  kpiTagName: string;
  structId: number;
  tagId: string;
  primaryTag: number; // 0: 其他点位 1: 主点位
  tagKind: number;
  energyType?: string;
  unit?: string;
  valType?: string;
}

// KPI结构
export interface KpiStruct {
  id?: number;
  name: string;
  parentId: number;
  level: number;
  energyType: string;
}

// 基础点位
export interface TagItem {
  tagId: string;
  tagName: string;
  tagType?: string;
  unit?: string;
  valType?: string;
}

//树节点
export interface TreeDataNode {
  id: number;
  name: string;
  level: number;
  parentId: number;
  energyType: string;
  sort: number;
  children?: TreeDataNode[];
  kpiTagList: KpiTag[];
}

//预警限值和预警接收人员
export interface CollectAlarmLimit {
  valType: number;
  limitType: number;
  limitValue?: number;
  duration?: number;
}

export interface OrsAlarmLimit {
  valType: number;
  dynPerUppLimit?: number;
  dynPerLowLimit?: number;
  redUppLimit?: number;
  yellowLowLimit?: number;
}

export interface KpiAlarmLimit {
  id?: number;
  kpiTagId: number;
  tagKind: number;
  tagId: string;
  collectAlarmLimit?: CollectAlarmLimit;
  orsAlarmLimit?: OrsAlarmLimit;
}

// KPI点位报警接收人员
export interface DictUser {
  userId: string;
  userName: string;
}

export interface KpiAlarmUser {
  groupName: string;
  kpiTagId: number;
  userIds: string[];
  userNames: string[];
}

export const tagKindList = ref<DictItem[]>([
  { value: TagKindEnum.ORSDATA, label: '中间表点位' },
  { value: TagKindEnum.COLLECT, label: '采集点位' },
]);
