import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
   {
    title: '工厂',
    align:"center",
    dataIndex: 'factDesc'
   },
   {
    title: '部门',
    align:"center",
    dataIndex: 'deparDesc'
   },
   {
    title: '工序',
    align:"center",
    dataIndex: 'processDesc'
   },
   {
    title: '产线',
    align:"center",
    dataIndex: 'lineDesc'
   },
   {
    title: '功能位置编码',
    align:"center",
    dataIndex: 'funlocCode'
   },
   {
    title: '功能位置描述汇总',
    align:"center",
    dataIndex: 'funlocDesc'
   },
   {
    title: '主设备编码',
    align:"center",
    dataIndex: 'equipCode'
   },
   {
    title: '主设备序号',
    align:"center",
    dataIndex: 'equipNum'
   },
   {
    title: '附属设备',
    align:"center",
    dataIndex: 'acceEquipCode'
   },
   {
    title: '结构标识',
    align:"center",
    dataIndex: 'strId'
   },
   {
    title: '大小/尺寸',
    align:"center",
    dataIndex: 'sizeDime'
   },
   {
    title: '主设备描述',
    align:"center",
    dataIndex: 'equipDesc'
   },
   {
    title: '附属设备描述',
    align:"center",
    dataIndex: 'acceEquipDesc'
   },
   {
    title: '部件描述',
    align:"center",
    dataIndex: 'partDesc'
   },
   {
    title: '主设备序号描述',
    align:"center",
    dataIndex: 'equipNumDesc'
   },
   {
    title: '部件大类',
    align:"center",
    dataIndex: 'partBigType'
   },
   {
    title: '系统ID',
    align:"center",
    dataIndex: 'sysUniqueId'
   },
   {
    title: '有效标识',
    align:"center",
    dataIndex: 'enableFlag'
   },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '功能位置编码',
    field: 'funlocCode',
    component: 'Input',
  },
  {
    label: '功能位置描述汇总',
    field: 'funlocDesc',
    component: 'Input',
  },
  {
    label: '部件编码',
    field: 'part',
    component: 'Input',
  },
  {
    label: '部件补充与描述',
    field: 'attr1',
    component: 'Input',
  },
  {
    label: '工厂',
    field: 'facCode',
    component: 'Input',
  },
  {
    label: '部门',
    field: 'deparCode',
    component: 'Input',
  },
  {
    label: '工序编码',
    field: 'processCode',
    component: 'Input',
  },
  {
    label: '产线编码',
    field: 'lineCode',
    component: 'Input',
  },
  {
    label: '主设备编码',
    field: 'equipCode',
    component: 'Input',
  },
  {
    label: '主设备序号',
    field: 'equipNum',
    component: 'Input',
  },
  {
    label: '附属设备',
    field: 'acceEquipCode',
    component: 'Input',
  },
  {
    label: '部件序号',
    field: 'partNum',
    component: 'Input',
  },
  {
    label: '部件补充',
    field: 'partRepl',
    component: 'Input',
  },
  {
    label: '部件类型',
    field: 'partType',
    component: 'Input',
  },
  {
    label: '部件英文缩写',
    field: 'partShortEn',
    component: 'Input',
  },
  {
    label: '功能位置编码汇总辅助列',
    field: 'funlocCodeAssi',
    component: 'Input',
  },
  {
    label: '结构标识',
    field: 'strId',
    component: 'Input',
  },
  {
    label: '大小/尺寸',
    field: 'sizeDime',
    component: 'Input',
  },
  {
    label: '制造商',
    field: 'manu',
    component: 'Input',
  },
  {
    label: '型号',
    field: 'model',
    component: 'Input',
  },
  {
    label: '制造商零件号',
    field: 'manuPartNum',
    component: 'Input',
  },
  {
    label: '系列号',
    field: 'seriNum',
    component: 'Input',
  },
  {
    label: '技术对象类型',
    field: 'techObjType',
    component: 'Input',
  },
  {
    label: '制造商国家',
    field: 'manuCountry',
    component: 'Input',
  },
  {
    label: '构建年',
    field: 'consYear',
    component: 'Input',
  },
  {
    label: '构建月',
    field: 'consMonth',
    component: 'Input',
  },
  {
    label: '维护工厂',
    field: 'mainFac',
    component: 'Input',
  },
  {
    label: '工作中心',
    field: 'workCenter',
    component: 'Input',
  },
  {
    label: '主工作中心',
    field: 'mainWorkCenter',
    component: 'Input',
  },
  {
    label: 'ABC标识',
    field: 'abcMark',
    component: 'Input',
  },
  {
    label: '成本中心',
    field: 'costCenter',
    component: 'Input',
  },
  {
    label: '计划人员组',
    field: 'planGroup',
    component: 'Input',
  },
  {
    label: '开始日期',
    field: 'startDate',
    component: 'DatePicker',
    componentProps: {
      valueFormat: 'YYYY-MM-DD'
    },
  },
  {
    label: '系统状态',
    field: 'sysStatus',
    component: 'Input',
  },
  {
    label: '工厂描述',
    field: 'factDesc',
    component: 'Input',
  },
  {
    label: '部门描述',
    field: 'deparDesc',
    component: 'Input',
  },
  {
    label: '工序描述',
    field: 'processDesc',
    component: 'Input',
  },
  {
    label: '主设备描述',
    field: 'equipDesc',
    component: 'Input',
  },
  {
    label: '附属设备描述',
    field: 'acceEquipDesc',
    component: 'Input',
  },
  {
    label: '部件描述',
    field: 'partDesc',
    component: 'Input',
  },
  {
    label: '弹性域2',
    field: 'attr2',
    component: 'Input',
  },
  {
    label: '弹性域3',
    field: 'attr3',
    component: 'Input',
  },
  {
    label: '业务状态',
    field: 'businessStatus',
    component: 'Input',
  },
  {
    label: '弹性域4',
    field: 'attr4',
    component: 'Input',
  },
  {
    label: '弹性域5',
    field: 'attr5',
    component: 'Input',
  },
  {
    label: '分发SAP标识',
    field: 'distributeSapFlag',
    component: 'Input',
  },
  {
    label: '弹性域6',
    field: 'attr6',
    component: 'Input',
  },
  {
    label: '弹性域7',
    field: 'attr7',
    component: 'Input',
  },
  {
    label: '弹性域8',
    field: 'attr8',
    component: 'Input',
  },
  {
    label: '弹性域9',
    field: 'attr9',
    component: 'Input',
  },
  {
    label: '弹性域10',
    field: 'attr10',
    component: 'Input',
  },
  {
    label: '产线描述',
    field: 'lineDesc',
    component: 'Input',
  },
  {
    label: '主设备序号描述',
    field: 'equipNumDesc',
    component: 'Input',
  },
  {
    label: '部件大类',
    field: 'partBigType',
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
  funlocCode: {title: '功能位置编码',order: 0,view: 'text', type: 'string',},
  funlocDesc: {title: '功能位置描述汇总',order: 1,view: 'text', type: 'string',},
  part: {title: '部件编码',order: 2,view: 'text', type: 'string',},
  attr1: {title: '部件补充与描述',order: 3,view: 'text', type: 'string',},
  facCode: {title: '工厂',order: 4,view: 'text', type: 'string',},
  deparCode: {title: '部门',order: 5,view: 'text', type: 'string',},
  processCode: {title: '工序编码',order: 6,view: 'text', type: 'string',},
  lineCode: {title: '产线编码',order: 7,view: 'text', type: 'string',},
  equipCode: {title: '主设备编码',order: 8,view: 'text', type: 'string',},
  equipNum: {title: '主设备序号',order: 9,view: 'text', type: 'string',},
  acceEquipCode: {title: '附属设备',order: 10,view: 'text', type: 'string',},
  partNum: {title: '部件序号',order: 11,view: 'text', type: 'string',},
  partRepl: {title: '部件补充',order: 12,view: 'text', type: 'string',},
  partType: {title: '部件类型',order: 13,view: 'text', type: 'string',},
  partShortEn: {title: '部件英文缩写',order: 14,view: 'text', type: 'string',},
  funlocCodeAssi: {title: '功能位置编码汇总辅助列',order: 15,view: 'text', type: 'string',},
  strId: {title: '结构标识',order: 16,view: 'text', type: 'string',},
  sizeDime: {title: '大小/尺寸',order: 17,view: 'text', type: 'string',},
  manu: {title: '制造商',order: 18,view: 'text', type: 'string',},
  model: {title: '型号',order: 19,view: 'text', type: 'string',},
  manuPartNum: {title: '制造商零件号',order: 20,view: 'text', type: 'string',},
  seriNum: {title: '系列号',order: 21,view: 'text', type: 'string',},
  techObjType: {title: '技术对象类型',order: 22,view: 'text', type: 'string',},
  manuCountry: {title: '制造商国家',order: 23,view: 'text', type: 'string',},
  consYear: {title: '构建年',order: 24,view: 'text', type: 'string',},
  consMonth: {title: '构建月',order: 25,view: 'text', type: 'string',},
  mainFac: {title: '维护工厂',order: 26,view: 'text', type: 'string',},
  workCenter: {title: '工作中心',order: 27,view: 'text', type: 'string',},
  mainWorkCenter: {title: '主工作中心',order: 28,view: 'text', type: 'string',},
  abcMark: {title: 'ABC标识',order: 29,view: 'text', type: 'string',},
  costCenter: {title: '成本中心',order: 30,view: 'text', type: 'string',},
  planGroup: {title: '计划人员组',order: 31,view: 'text', type: 'string',},
  startDate: {title: '开始日期',order: 32,view: 'date', type: 'string',},
  sysStatus: {title: '系统状态',order: 33,view: 'text', type: 'string',},
  factDesc: {title: '工厂描述',order: 34,view: 'text', type: 'string',},
  deparDesc: {title: '部门描述',order: 35,view: 'text', type: 'string',},
  processDesc: {title: '工序描述',order: 36,view: 'text', type: 'string',},
  equipDesc: {title: '主设备描述',order: 37,view: 'text', type: 'string',},
  acceEquipDesc: {title: '附属设备描述',order: 38,view: 'text', type: 'string',},
  partDesc: {title: '部件描述',order: 39,view: 'text', type: 'string',},
  attr2: {title: '弹性域2',order: 40,view: 'text', type: 'string',},
  attr3: {title: '弹性域3',order: 41,view: 'text', type: 'string',},
  businessStatus: {title: '业务状态',order: 42,view: 'text', type: 'string',},
  attr4: {title: '弹性域4',order: 43,view: 'text', type: 'string',},
  attr5: {title: '弹性域5',order: 44,view: 'text', type: 'string',},
  distributeSapFlag: {title: '分发SAP标识',order: 45,view: 'text', type: 'string',},
  attr6: {title: '弹性域6',order: 46,view: 'text', type: 'string',},
  attr7: {title: '弹性域7',order: 47,view: 'text', type: 'string',},
  attr8: {title: '弹性域8',order: 48,view: 'text', type: 'string',},
  attr9: {title: '弹性域9',order: 49,view: 'text', type: 'string',},
  attr10: {title: '弹性域10',order: 50,view: 'text', type: 'string',},
  lineDesc: {title: '产线描述',order: 51,view: 'text', type: 'string',},
  equipNumDesc: {title: '主设备序号描述',order: 52,view: 'text', type: 'string',},
  partBigType: {title: '部件大类',order: 53,view: 'text', type: 'string',},
  sysUniqueId: {title: '系统ID',order: 54,view: 'number', type: 'number',},
  dataSourceSysId: {title: '数据来源系统ID',order: 55,view: 'text', type: 'string',},
  sysUniqueCode: {title: '系统编码',order: 56,view: 'text', type: 'string',},
  enableFlag: {title: '有效标识',order: 57,view: 'text', type: 'string',},
  checkErrMsg: {title: '校验错误消息',order: 58,view: 'text', type: 'string',},
  rtMdmDataVersion: {title: '数据版本号',order: 59,view: 'text', type: 'string',},
};

/**
* 流程表单调用这个方法获取formSchema
* @param param
*/
export function getBpmFormSchema(_formData): FormSchema[]{
  // 默认和原始表单保持一致 如果流程中配置了权限数据，这里需要单独处理formSchema
  return formSchema;
}