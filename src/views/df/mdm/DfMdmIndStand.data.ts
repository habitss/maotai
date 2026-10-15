import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '指标编码',
    align: "center",
    dataIndex: 'indStandCode'
  },
  {
    title: '指标名称',
    align: "center",
    dataIndex: 'indName'
  },
  {
    title: '指标定义',
    align: "center",
    dataIndex: 'indDef'
  },
  {
    title: '度量对象',
    align: "center",
    dataIndex: 'meaObj'
  },
  {
    title: '计量单位',
    align: "center",
    dataIndex: 'unit'
  },
  {
    title: '对应SAP功能位置-工厂',
    align: "center",
    dataIndex: 'funlocFac'
  },
  {
    title: '对应SAP功能位置-部门',
    align: "center",
    dataIndex: 'funlocDeparDesc'
  },
  {
    title: '对应SAP功能位置-工序',
    align: "center",
    dataIndex: 'funlocProcessDesc'
  },
  {
    title: '对应SAP功能位置-主设备',
    align: "center",
    dataIndex: 'funlocEquipDesc'
  },
  {
    title: '对应SAP功能位置-附属设备',
    align: "center",
    dataIndex: 'funlocAcceEquipDesc'
  },
  {
    title: '对应SAP功能位置-部件',
    align: "center",
    dataIndex: 'funlocPart'
  },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: '工厂',
    field: 'funlocFac',
    component: 'JInput',
  },
  {
    label: '部门',
    field: 'funlocDeparDesc',
    component: 'JInput',
  },
  {
    label: '工序',
    field: 'funlocProcessDesc',
    component: 'JInput',
  },
  {
    label: '指标名称',
    field: 'indName',
    component: 'JInput',
  },
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '流程架构',
    field: 'bizCatalogPath',
    component: 'Input',
  },
  {
    label: '指标框架（T0）',
    field: 'indFraT0',
    component: 'Input',
  },
  {
    label: '指标框架（T1）',
    field: 'indFraT1',
    component: 'Input',
  },
  {
    label: '指标框架（T2）',
    field: 'indFraT2',
    component: 'Input',
  },
  {
    label: '指标框架（T3）',
    field: 'indFraT3',
    component: 'Input',
  },
  {
    label: '指标框架（T4）',
    field: 'indFraT4',
    component: 'Input',
  },
  {
    label: '指标框架（T5）【酿造/包装/动力-运营管理指标填写】',
    field: 'indFraT5',
    component: 'Input',
  },
  {
    label: '管理对象【酿造/包装/动力-运营管理指标填写】',
    field: 'manaObj',
    component: 'Input',
  },
  {
    label: '活动【酿造/包装/动力-运营管理指标填写】',
    field: 'activity',
    component: 'Input',
  },
  {
    label: '一级任务【酿造/包装/动力-生产监测指标填写】',
    field: 'firTask',
    component: 'Input',
  },
  {
    label: '二级任务【酿造/包装/动力-生产监测指标填写】',
    field: 'secTask',
    component: 'Input',
  },
  {
    label: '指标编码',
    field: 'indStandCode',
    component: 'Input',
  },
  {
    label: '对应ORS指标代码',
    field: 'corOrsCode',
    component: 'Input',
  },
  {
    label: '指标名称(原始名称)',
    field: 'indNameOld',
    component: 'Input',
  },
  {
    label: '指标名称（英文名)【质量-管理类指标填写】',
    field: 'indNameEn',
    component: 'Input',
  },
  {
    label: '指标名称',
    field: 'indName',
    component: 'Input',
  },
  {
    label: '指标定义',
    field: 'indDef',
    component: 'Input',
  },
  {
    label: '监测对象分类【酿造/动力/质量(生产)监测类指标填写】',
    field: 'monObjType',
    component: 'Input',
  },
  {
    label: '监测对象【酿造/动力/质量(生产)监测类指标填写】',
    field: 'monObj',
    component: 'Input',
  },
  {
    label: '检查项【包装-生产监测指标填写】',
    field: 'inspItem',
    component: 'Input',
  },
  {
    label: '检查对象分类【包装-生产监测指标填写】',
    field: 'inspObjType',
    component: 'Input',
  },
  {
    label: '检查对象【包装-生产监测指标填写】',
    field: 'inspObj',
    component: 'Input',
  },
  {
    label: '工序-所属部门【质量指标填写】',
    field: 'opStepType',
    component: 'Input',
  },
  {
    label: '工序-工艺一级分类【质量指标填写】',
    field: 'firProcName',
    component: 'Input',
  },
  {
    label: '工序-工艺二级分类【质量指标填写】',
    field: 'secProcName',
    component: 'Input',
  },
  {
    label: '工序-工序步骤1【质量指标填写】',
    field: 'op1StepName',
    component: 'Input',
  },
  {
    label: '度量对象',
    field: 'meaObj',
    component: 'Input',
  },
  {
    label: '计量单位',
    field: 'unit',
    component: 'Input',
  },
  {
    label: '对应SAP功能位置-工厂',
    field: 'funlocFac',
    component: 'Input',
  },
  {
    label: '对应SAP功能位置-部门',
    field: 'funlocDeparDesc',
    component: 'Input',
  },
  {
    label: '对应SAP功能位置-工序',
    field: 'funlocProcessDesc',
    component: 'Input',
  },
  {
    label: '对应SAP功能位置-主设备',
    field: 'funlocEquipDesc',
    component: 'Input',
  },
  {
    label: '对应SAP功能位置-附属设备',
    field: 'funlocAcceEquipDesc',
    component: 'Input',
  },
  {
    label: '对应SAP功能位置-部件',
    field: 'funlocPart',
    component: 'Input',
  },
  {
    label: '取样点',
    field: 'samPoint',
    component: 'Input',
  },
  {
    label: '监测方式',
    field: 'monMethod',
    component: 'Input',
  },
  {
    label: '监测设备',
    field: 'monEquip',
    component: 'Input',
  },
  {
    label: '数据来源',
    field: 'dataSource',
    component: 'Input',
  },
  {
    label: '指标口径',
    field: 'indCal',
    component: 'Input',
  },
  {
    label: '指标设置目的',
    field: 'indSetPurp',
    component: 'Input',
  },
  {
    label: '计算方式',
    field: 'calMeth',
    component: 'Input',
  },
  {
    label: '计算公式',
    field: 'calFormula',
    component: 'Input',
  },
  {
    label: '指标统计周期',
    field: 'indStaCycle',
    component: 'Input',
  },
  {
    label: '指标统计维度',
    field: 'indStaDim',
    component: 'Input',
  },
  {
    label: '刷新频率',
    field: 'refFreq',
    component: 'Input',
  },
  {
    label: '指标归口管理部门',
    field: 'indCenManDept',
    component: 'Input',
  },
  {
    label: '指标责任人',
    field: 'indOwner',
    component: 'Input',
  },
  {
    label: '指标分类【动力-运营管理指标填写】',
    field: 'piType',
    component: 'Input',
  },
  {
    label: '原燃辅料类型【动力-运营管理指标填写】',
    field: 'rawcamType',
    component: 'Input',
  },
  {
    label: '业务类型【动力-运营管理指标填写】',
    field: 'busiType',
    component: 'Input',
  },
  {
    label: '采购来源【动力-运营管理指标填写】',
    field: 'procurementSource',
    component: 'Input',
  },
  {
    label: '能源介质类型【动力-能源消耗指标填写】',
    field: 'energyType',
    component: 'Input',
  },
  {
    label: '能源介质子类【动力-能源消耗指标填写】',
    field: 'energySubtype',
    component: 'Input',
  },
  {
    label: '指标别名',
    field: 'indNameAlias',
    component: 'Input',
  },
  {
    label: '指标应用场景',
    field: 'applyScenario',
    component: 'Input',
  },
  {
    label: '取数报表',
    field: 'accReport',
    component: 'Input',
  },
  {
    label: '取值字段',
    field: 'reportField',
    component: 'Input',
  },
  {
    label: '备注',
    field: 'remarks',
    component: 'Input',
  },
  {
    label: '同步状态',
    field: 'synchronousStatus',
    component: 'Input',
  },
  {
    label: '计量单位_中文',
    field: 'unitCn',
    component: 'Input',
  },
  {
    label: '工序-工序步骤2',
    field: 'op2StepName',
    component: 'Input',
  },
  {
    label: '系统ID',
    field: 'sysUniqueId',
    component: 'InputNumber',
  },
  {
    label: '数据来源系统ID',
    field: 'dataSourceSysId',
    component: 'Input',
  },
  {
    label: '系统编码',
    field: 'sysUniqueCode',
    component: 'Input',
  },
  {
    label: '有效标识',
    field: 'enableFlag',
    component: 'Input',
  },
  {
    label: '校验错误消息',
    field: 'checkErrMsg',
    component: 'Input',
  },
  {
    label: '数据版本号',
    field: 'rtMdmDataVersion',
    component: 'Input',
  },
  // TODO 主键隐藏字段，目前写死为ID
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false
  },
];

// 高级查询数据
export const superQuerySchema = {
  tenantId: { title: '租户ID', order: 0, view: 'number', type: 'number', },
  bizCatalogPath: { title: '流程架构', order: 1, view: 'text', type: 'string', },
  indFraT0: { title: '指标框架（T0）', order: 2, view: 'text', type: 'string', },
  indFraT1: { title: '指标框架（T1）', order: 3, view: 'text', type: 'string', },
  indFraT2: { title: '指标框架（T2）', order: 4, view: 'text', type: 'string', },
  indFraT3: { title: '指标框架（T3）', order: 5, view: 'text', type: 'string', },
  indFraT4: { title: '指标框架（T4）', order: 6, view: 'text', type: 'string', },
  indFraT5: { title: '指标框架（T5）【酿造/包装/动力-运营管理指标填写】', order: 7, view: 'text', type: 'string', },
  manaObj: { title: '管理对象【酿造/包装/动力-运营管理指标填写】', order: 8, view: 'text', type: 'string', },
  activity: { title: '活动【酿造/包装/动力-运营管理指标填写】', order: 9, view: 'text', type: 'string', },
  firTask: { title: '一级任务【酿造/包装/动力-生产监测指标填写】', order: 10, view: 'text', type: 'string', },
  secTask: { title: '二级任务【酿造/包装/动力-生产监测指标填写】', order: 11, view: 'text', type: 'string', },
  indStandCode: { title: '指标编码', order: 12, view: 'text', type: 'string', },
  corOrsCode: { title: '对应ORS指标代码', order: 13, view: 'text', type: 'string', },
  indNameOld: { title: '指标名称(原始名称)', order: 14, view: 'text', type: 'string', },
  indNameEn: { title: '指标名称（英文名)【质量-管理类指标填写】', order: 15, view: 'text', type: 'string', },
  indName: { title: '指标名称', order: 16, view: 'text', type: 'string', },
  indDef: { title: '指标定义', order: 17, view: 'text', type: 'string', },
  monObjType: { title: '监测对象分类【酿造/动力/质量(生产)监测类指标填写】', order: 18, view: 'text', type: 'string', },
  monObj: { title: '监测对象【酿造/动力/质量(生产)监测类指标填写】', order: 19, view: 'text', type: 'string', },
  inspItem: { title: '检查项【包装-生产监测指标填写】', order: 20, view: 'text', type: 'string', },
  inspObjType: { title: '检查对象分类【包装-生产监测指标填写】', order: 21, view: 'text', type: 'string', },
  inspObj: { title: '检查对象【包装-生产监测指标填写】', order: 22, view: 'text', type: 'string', },
  opStepType: { title: '工序-所属部门【质量指标填写】', order: 23, view: 'text', type: 'string', },
  firProcName: { title: '工序-工艺一级分类【质量指标填写】', order: 24, view: 'text', type: 'string', },
  secProcName: { title: '工序-工艺二级分类【质量指标填写】', order: 25, view: 'text', type: 'string', },
  op1StepName: { title: '工序-工序步骤1【质量指标填写】', order: 26, view: 'text', type: 'string', },
  meaObj: { title: '度量对象', order: 27, view: 'text', type: 'string', },
  unit: { title: '计量单位', order: 28, view: 'text', type: 'string', },
  funlocFac: { title: '对应SAP功能位置-工厂', order: 29, view: 'text', type: 'string', },
  funlocDeparDesc: { title: '对应SAP功能位置-部门', order: 30, view: 'text', type: 'string', },
  funlocProcessDesc: { title: '对应SAP功能位置-工序', order: 31, view: 'text', type: 'string', },
  funlocEquipDesc: { title: '对应SAP功能位置-主设备', order: 32, view: 'text', type: 'string', },
  funlocAcceEquipDesc: { title: '对应SAP功能位置-附属设备', order: 33, view: 'text', type: 'string', },
  funlocPart: { title: '对应SAP功能位置-部件', order: 34, view: 'text', type: 'string', },
  samPoint: { title: '取样点', order: 35, view: 'text', type: 'string', },
  monMethod: { title: '监测方式', order: 36, view: 'text', type: 'string', },
  monEquip: { title: '监测设备', order: 37, view: 'text', type: 'string', },
  dataSource: { title: '数据来源', order: 38, view: 'text', type: 'string', },
  indCal: { title: '指标口径', order: 39, view: 'text', type: 'string', },
  indSetPurp: { title: '指标设置目的', order: 40, view: 'text', type: 'string', },
  calMeth: { title: '计算方式', order: 41, view: 'text', type: 'string', },
  calFormula: { title: '计算公式', order: 42, view: 'text', type: 'string', },
  indStaCycle: { title: '指标统计周期', order: 43, view: 'text', type: 'string', },
  indStaDim: { title: '指标统计维度', order: 44, view: 'text', type: 'string', },
  refFreq: { title: '刷新频率', order: 45, view: 'text', type: 'string', },
  indCenManDept: { title: '指标归口管理部门', order: 46, view: 'text', type: 'string', },
  indOwner: { title: '指标责任人', order: 47, view: 'text', type: 'string', },
  piType: { title: '指标分类【动力-运营管理指标填写】', order: 48, view: 'text', type: 'string', },
  rawcamType: { title: '原燃辅料类型【动力-运营管理指标填写】', order: 49, view: 'text', type: 'string', },
  busiType: { title: '业务类型【动力-运营管理指标填写】', order: 50, view: 'text', type: 'string', },
  procurementSource: { title: '采购来源【动力-运营管理指标填写】', order: 51, view: 'text', type: 'string', },
  energyType: { title: '能源介质类型【动力-能源消耗指标填写】', order: 52, view: 'text', type: 'string', },
  energySubtype: { title: '能源介质子类【动力-能源消耗指标填写】', order: 53, view: 'text', type: 'string', },
  indNameAlias: { title: '指标别名', order: 54, view: 'text', type: 'string', },
  applyScenario: { title: '指标应用场景', order: 55, view: 'text', type: 'string', },
  accReport: { title: '取数报表', order: 56, view: 'text', type: 'string', },
  reportField: { title: '取值字段', order: 57, view: 'text', type: 'string', },
  remarks: { title: '备注', order: 58, view: 'text', type: 'string', },
  synchronousStatus: { title: '同步状态', order: 59, view: 'text', type: 'string', },
  unitCn: { title: '计量单位_中文', order: 60, view: 'text', type: 'string', },
  op2StepName: { title: '工序-工序步骤2', order: 61, view: 'text', type: 'string', },
  sysUniqueId: { title: '系统ID', order: 62, view: 'number', type: 'number', },
  dataSourceSysId: { title: '数据来源系统ID', order: 63, view: 'text', type: 'string', },
  sysUniqueCode: { title: '系统编码', order: 64, view: 'text', type: 'string', },
  enableFlag: { title: '有效标识', order: 65, view: 'text', type: 'string', },
  checkErrMsg: { title: '校验错误消息', order: 66, view: 'text', type: 'string', },
  rtMdmDataVersion: { title: '数据版本号', order: 67, view: 'text', type: 'string', },
};

/**
* 流程表单调用这个方法获取formSchema
* @param param
*/
export function getBpmFormSchema(_formData): FormSchema[] {
  // 默认和原始表单保持一致 如果流程中配置了权限数据，这里需要单独处理formSchema
  return formSchema;
}