import { h } from 'vue';
import type { Ref } from 'vue';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { WORKSHOP_PROCESSES, TRUCK_PROCESSES, type TransferPath } from './WorkOrder.api';

// 工单状态字典:pending待执行/running执行中/paused已暂停/completed已完成/abandoned已废弃
// 配色对齐设备台账运行状态 pill(深色文字+浅底+圆角)
export const STATUS_MAP: Record<string, { text: string; color: string; bg: string }> = {
  pending: { text: '待执行', color: '#D97706', bg: '#FFFBEB' },
  running: { text: '执行中', color: '#10B981', bg: '#ECFDF5' },
  paused: { text: '已暂停', color: '#7C3AED', bg: '#F5F3FF' },
  completed: { text: '已完成', color: '#1D4ED8', bg: '#EFF6FF' },
  abandoned: { text: '已废弃', color: '#6B7280', bg: '#F5F5F4' },
};
export const STATUS_OPTIONS = Object.keys(STATUS_MAP).map((k) => ({ value: k, label: STATUS_MAP[k].text }));

// 车间编码→名称(静态兜底值;列表页加载车间字典后覆盖为字典口径)
export const workshopNameMap: Record<string, string> = {
  ZZG: '中转酒罐车间',
  XLG: '系列酒勾调厂房',
  CPG: '成品酒罐房',
  GWG: '包装厂房',
};

/** 状态彩色徽章(列表列与日志列共用) */
export function renderStatusPill(status?: string) {
  const m = STATUS_MAP[status || ''];
  if (!m) return status || '—';
  return h(
    'span',
    {
      style: {
        color: m.color,
        background: m.bg,
        fontWeight: 600,
        padding: '2px 10px',
        borderRadius: '12px',
        display: 'inline-block',
      },
    },
    m.text,
  );
}

// 字典 dictCode(与设备台账同款,显示列在前值列在后)

// 弹窗内下拉/日期面板统一挂 body:ModalWrapper 滚动容器的定位上下文会把 absolute 面板带偏
const POPUP_BODY = { getPopupContainer: () => document.body };

const DICT_WORKSHOP = "mes3_mdm_dict where group_code='MES3_WORKSHOP' and del_flag=0, item_val, item_key";

/** 是否槽车工艺(卸车/装车,带车牌驾驶员等车辆字段) */
function isTruck(p?: string) {
  return !!p && TRUCK_PROCESSES.includes(p);
}

// 列表数据(卡片列;转酒路径显示配置名称,旧数据无名称回退编码)
export const columns: BasicColumn[] = [
  {
    title: '工单号',
    align: 'center',
    dataIndex: 'orderNo',
    width: 130,
    sorter: true,
    resizable: true,
  },
  {
    title: '工艺名称',
    align: 'center',
    dataIndex: 'processName',
    width: 90,
    resizable: true,
  },
  {
    title: '所属车间',
    align: 'center',
    dataIndex: 'workshopCode',
    width: 95,
    customRender: ({ record }) => workshopNameMap[record.workshopCode] || record.workshopCode || '—',
  },
  {
    title: '日期',
    align: 'center',
    dataIndex: 'planDate',
    width: 95,
    sorter: true,
  },
  {
    title: '源酒罐',
    align: 'center',
    dataIndex: 'sourceTank',
    width: 100,
    customRender: ({ record }) => record.sourceTank || '—',
  },
  {
    title: '目标酒罐',
    align: 'center',
    dataIndex: 'targetTank',
    width: 100,
    customRender: ({ record }) => record.targetTank || '—',
  },
  {
    title: '转酒路径',
    align: 'center',
    dataIndex: 'transferPathName',
    width: 95,
    resizable: true,
    customRender: ({ record }) => record.transferPathName || record.transferPath || '—',
  },
  {
    title: '输送量(吨)',
    align: 'center',
    dataIndex: 'amount',
    width: 85,
    customRender: ({ record }) => record.amount ?? 0,
  },
  {
    title: '工单状态',
    align: 'center',
    dataIndex: 'status',
    width: 80,
    customRender: ({ record }) => renderStatusPill(record.status),
  },
  {
    title: '操作人',
    align: 'center',
    dataIndex: 'operator',
    width: 70,
    customRender: ({ record }) => record.operator || '—',
  },
];

// 查询数据(工单号为用户点名必备条件)
export const searchFormSchema: FormSchema[] = [
  {
    label: '工单号',
    field: 'orderNo',
    component: 'Input',
    componentProps: { placeholder: '请输入工单号' },
    colProps: { span: 5 },
  },
  {
    label: '所属车间',
    field: 'workshopCode',
    component: 'JDictSelectTag',
    componentProps: { dictCode: DICT_WORKSHOP, placeholder: '请选择车间' },
    colProps: { span: 5 },
  },
  {
    label: '工单状态',
    field: 'status',
    component: 'Select',
    componentProps: { options: STATUS_OPTIONS, placeholder: '请选择状态' },
    colProps: { span: 5 },
  },
];

/**
 * 新增/编辑/详情表单 schema 工厂(字段顺序对齐原型:工单号|工艺 → 车间|日期 → 计划时间 →
 * 源罐|源品牌 → 目标罐|目标品牌 → 转酒路径|输送量 → 打空 → 备注 → 槽车块)。
 * 罐选项/路径候选与联动回调依赖组件内状态,故以工厂注入(不用模块级常量)。
 */
export function buildFormSchemas(
  pathOptions: Ref<TransferPath[]>,
  tankOptions: Ref<{ label: string; value: string }[]>,
  hooks: { onTankChange: () => void; onProcessChange: () => void; onWorkshopChange: () => void },
): FormSchema[] {
  return [
    {
      label: '工单号',
      field: 'orderNo',
      component: 'Input',
      // 系统自动生成,新增态不显示;编辑/详情只读展示
      ifShow: ({ values }) => !!values?.id,
      dynamicDisabled: ({ values }) => !!values?.id,
      colProps: { span: 12 },
    },
    {
      label: '所属车间',
      field: 'workshopCode',
      component: 'JDictSelectTag',
      componentProps: { ...POPUP_BODY, dictCode: DICT_WORKSHOP, placeholder: '请选择车间', showSearch: true, optionFilterProp: 'label', onChange: () => hooks.onWorkshopChange() },
      required: true,
      // 车间变更会引起罐清单/工艺/路径联动,编辑态锁定
      dynamicDisabled: ({ values }) => !!values?.id,
      colProps: { span: 12 },
    },
    {
      label: '工艺名称',
      field: 'processName',
      component: 'Select',
      componentProps: ({ formModel }) => ({
        ...POPUP_BODY,
        options: (WORKSHOP_PROCESSES[formModel.workshopCode] || []).map((p) => ({ label: p, value: p })),
        showSearch: true,
        optionFilterProp: 'label',
        placeholder: '请选择工艺名称',
        onChange: () => hooks.onProcessChange(),
      }),
      required: true,
      dynamicDisabled: ({ values }) => !!values?.id,
      colProps: { span: 12 },
    },
    {
      label: '日期',
      field: 'planDate',
      component: 'DatePicker',
      componentProps: { ...POPUP_BODY, valueFormat: 'YYYY-MM-DD', style: { width: '100%' }, placeholder: '请选择日期' },
      required: true,
      colProps: { span: 12 },
    },
    {
      label: '计划开始时间',
      field: 'planStartTime',
      component: 'Input',
      componentProps: { placeholder: '如 09:00' },
      colProps: { span: 12 },
    },
    {
      label: '计划完成时间',
      field: 'planEndTime',
      component: 'Input',
      componentProps: { placeholder: '如 11:30' },
      colProps: { span: 12 },
    },
    {
      label: '源罐',
      field: 'sourceTank',
      component: 'Select',
      // 罐清单来自设备台账(tank_config_id 非空),随车间联动;支持搜索
      componentProps: () => ({
        options: tankOptions.value,
        showSearch: true,
        optionFilterProp: 'label',
        placeholder: '请选择源罐',
        onChange: () => hooks.onTankChange(),
      }),
      // 卸车工艺无源罐(未选工艺前不显示)
      ifShow: ({ values }) => !!values?.processName && values.processName !== '卸车工艺',
      dynamicRules: ({ values }) =>
        values?.processName && values.processName !== '卸车工艺' ? [{ required: true, message: '请选择源罐' }] : [],
      colProps: { span: 12 },
    },
    {
      label: '源品牌',
      field: 'sourceBrand',
      component: 'Input',
      componentProps: { placeholder: '如：飞天茅台' },
      ifShow: ({ values }) => !!values?.processName && values.processName !== '卸车工艺',
      colProps: { span: 12 },
    },
    {
      label: '目标罐',
      field: 'targetTank',
      component: 'Select',
      componentProps: () => ({
        ...POPUP_BODY,
        options: tankOptions.value,
        showSearch: true,
        optionFilterProp: 'label',
        placeholder: '请选择目标罐',
        onChange: () => hooks.onTankChange(),
      }),
      // 装车工艺无目标罐(未选工艺前不显示)
      ifShow: ({ values }) => !!values?.processName && values.processName !== '装车工艺',
      dynamicRules: ({ values }) =>
        values?.processName && values.processName !== '装车工艺' ? [{ required: true, message: '请选择目标罐' }] : [],
      colProps: { span: 12 },
    },
    {
      label: '目标品牌',
      field: 'targetBrand',
      component: 'Input',
      componentProps: { placeholder: '如：飞天茅台' },
      ifShow: ({ values }) => !!values?.processName && values.processName !== '装车工艺',
      colProps: { span: 12 },
    },
    {
      label: '转酒路径',
      field: 'transferPath',
      component: 'Select',
      // 候选全部来自转酒路径配置表,必填且不自动选中(无代码兜底)
      componentProps: () => ({
        options: pathOptions.value.map((p) => ({ label: p.name, value: p.code })),
        showSearch: true,
        optionFilterProp: 'label',
        placeholder: '请选择转酒路径',
        notFoundContent: '未配置路径',
      }),
      required: true,
      colProps: { span: 12 },
    },
    {
      label: '输送量(吨)',
      field: 'amount',
      component: 'InputNumber',
      componentProps: { min: 0, style: { width: '100%' }, placeholder: '请输入输送量' },
      colProps: { span: 12 },
    },
    {
      label: '是否打空源罐',
      field: 'emptySource',
      component: 'Select',
      componentProps: { ...POPUP_BODY, options: [{ label: '否', value: 0 }, { label: '是', value: 1 }] },
      ifShow: ({ values }) => values?.processName === '倒罐工艺',
      colProps: { span: 12 },
    },
    {
      label: '批次号',
      field: 'batchNo',
      component: 'Input',
      // 一工单一批次:创建时后端按阶段自动生成(XC卸/ZZ中转/GD勾调/CP成品/GZ灌装-{yyMM}-nn),新增态不显示,编辑/详情只读
      ifShow: ({ values }) => !!values?.id,
      dynamicDisabled: ({ values }) => !!values?.id,
      colProps: { span: 12 },
    },
    {
      label: '备注说明',
      field: 'remark',
      component: 'InputTextArea',
      componentProps: { rows: 2, placeholder: '其他需要记录的信息' },
      colProps: { span: 24 },
    },
    // 槽车块(卸车/装车工艺,原型置于表单底部):酒精度|车牌号 → 司机姓名|联系电话
    {
      label: '酒精度(%vol)',
      field: 'alcohol',
      component: 'InputNumber',
      componentProps: { min: 0, max: 100, style: { width: '100%' }, placeholder: '如：53' },
      // 槽车工艺(原型字段)+倒罐工艺(API05 alcoholContent 必填)
      ifShow: ({ values }) => isTruck(values?.processName) || values?.processName === '倒罐工艺',
      dynamicRules: ({ values }) =>
        values?.processName === '倒罐工艺' ? [{ required: true, message: '请输入酒精度' }] : [],
      colProps: { span: 12 },
    },
    {
      label: '车载酒量',
      field: 'currentVehicleQty',
      component: 'Input',
      componentProps: { placeholder: '当前车载酒数量(API05 必填)' },
      // 仅倒罐工艺(API05 currentVehicleQty 必填)
      ifShow: ({ values }) => values?.processName === '倒罐工艺',
      dynamicRules: ({ values }) =>
        values?.processName === '倒罐工艺' ? [{ required: true, message: '请输入当前车载酒数量' }] : [],
      colProps: { span: 12 },
    },
    {
      label: '车牌号',
      field: 'plateNo',
      component: 'Input',
      componentProps: { placeholder: '如：贵A·12345' },
      ifShow: ({ values }) => isTruck(values?.processName),
      dynamicRules: ({ values }) => (isTruck(values?.processName) ? [{ required: true, message: '请输入车牌号' }] : []),
      colProps: { span: 12 },
    },
    {
      label: '司机姓名',
      field: 'driverName',
      component: 'Input',
      componentProps: { placeholder: '请输入司机姓名' },
      ifShow: ({ values }) => isTruck(values?.processName),
      dynamicRules: ({ values }) => (isTruck(values?.processName) ? [{ required: true, message: '请输入司机姓名' }] : []),
      colProps: { span: 12 },
    },
    {
      label: '联系电话',
      field: 'contactPhone',
      component: 'Input',
      componentProps: { placeholder: '请输入联系电话' },
      ifShow: ({ values }) => isTruck(values?.processName),
      colProps: { span: 12 },
    },
    // 详情态只读信息
    {
      label: '工单状态',
      field: 'status',
      component: 'Input',
      ifShow: ({ values }) => !!values?.id,
      render: ({ values }) => renderStatusPill(values?.status),
      colProps: { span: 12 },
    },
    {
      label: '实际开始时间',
      field: 'startTime',
      component: 'Input',
      dynamicDisabled: () => true,
      ifShow: ({ values }) => !!values?.id && !!values?.startTime,
      colProps: { span: 12 },
    },
    {
      label: '完成时间',
      field: 'endTime',
      component: 'Input',
      dynamicDisabled: () => true,
      ifShow: ({ values }) => !!values?.id && !!values?.endTime,
      colProps: { span: 12 },
    },
    {
      label: '操作人',
      field: 'operator',
      component: 'Input',
      dynamicDisabled: () => true,
      ifShow: ({ values }) => !!values?.id && !!values?.operator,
      colProps: { span: 12 },
    },
    {
      label: '卸车撬',
      field: 'skidCode',
      component: 'Input',
      dynamicDisabled: () => true,
      ifShow: ({ values }) => !!values?.id && !!values?.skidCode,
      colProps: { span: 12 },
    },
    // 主键隐藏字段
    {
      label: '',
      field: 'id',
      component: 'Input',
      show: false,
      colProps: { span: 24 },
    },
  ];
}

/** 批量新增公共字段(明细行单独渲染,不含罐/数量/路径) */
export function buildBatchSchemas(hooks: { onProcessChange: () => void }): FormSchema[] {
  return [
    {
      label: '所属车间',
      field: 'workshopCode',
      component: 'JDictSelectTag',
      componentProps: { dictCode: DICT_WORKSHOP, placeholder: '请选择车间' },
      required: true,
      colProps: { span: 12 },
    },
    {
      label: '工艺名称',
      field: 'processName',
      component: 'Select',
      componentProps: ({ formModel }) => ({
        ...POPUP_BODY,
        options: (WORKSHOP_PROCESSES[formModel.workshopCode] || []).map((p) => ({ label: p, value: p })),
        showSearch: true,
        optionFilterProp: 'label',
        placeholder: '请选择工艺名称',
        onChange: () => hooks.onProcessChange(),
      }),
      required: true,
      colProps: { span: 12 },
    },
    {
      label: '日期',
      field: 'planDate',
      component: 'DatePicker',
      componentProps: { ...POPUP_BODY, valueFormat: 'YYYY-MM-DD', style: { width: '100%' }, placeholder: '请选择日期' },
      required: true,
      colProps: { span: 12 },
    },
    {
      label: '计划开始时间',
      field: 'planStartTime',
      component: 'Input',
      componentProps: { placeholder: '如 09:00' },
      colProps: { span: 12 },
    },
    {
      label: '计划完成时间',
      field: 'planEndTime',
      component: 'Input',
      componentProps: { placeholder: '如 11:30' },
      colProps: { span: 12 },
    },
    {
      label: '源品牌',
      field: 'sourceBrand',
      component: 'Input',
      componentProps: { placeholder: '如：飞天茅台' },
      colProps: { span: 12 },
    },
    {
      label: '目标品牌',
      field: 'targetBrand',
      component: 'Input',
      componentProps: { placeholder: '如：飞天茅台' },
      colProps: { span: 12 },
    },
    {
      label: '备注说明',
      field: 'remark',
      component: 'InputTextArea',
      componentProps: { rows: 2, placeholder: '可选填写备注信息' },
      colProps: { span: 24 },
    },
  ];
}

// 操作日志列表数据
export const logColumns: BasicColumn[] = [
  {
    title: '操作时间',
    align: 'center',
    dataIndex: 'operateTime',
    width: 160,
    // 后端返回 ISO 格式(2026-09-15T10:10:56),展示转空格分隔
    customRender: ({ record }) => (record.operateTime ? String(record.operateTime).replace('T', ' ').slice(0, 19) : '—'),
  },
  {
    title: '操作人',
    align: 'center',
    dataIndex: 'operator',
    width: 100,
  },
  {
    title: '操作类型',
    align: 'center',
    dataIndex: 'action',
    width: 100,
  },
  {
    title: '工单号',
    align: 'center',
    dataIndex: 'orderNo',
    width: 150,
  },
  {
    title: '操作内容',
    align: 'left',
    dataIndex: 'content',
  },
  {
    title: '工单状态',
    align: 'center',
    dataIndex: 'orderStatus',
    width: 100,
    customRender: ({ record }) => renderStatusPill(record.orderStatus),
  },
];

// 操作日志查询数据(工单号为用户点名必备条件)
export const logSearchFormSchema: FormSchema[] = [
  {
    label: '工单号',
    field: 'orderNo',
    component: 'Input',
    componentProps: { placeholder: '请输入工单号' },
    colProps: { span: 5 },
  },
  {
    label: '关键字',
    field: 'keyword',
    component: 'Input',
    componentProps: { placeholder: '操作人/操作类型/操作内容' },
    colProps: { span: 5 },
  },
  {
    label: '工单状态',
    field: 'status',
    component: 'Select',
    componentProps: { options: STATUS_OPTIONS, placeholder: '请选择状态' },
    colProps: { span: 5 },
  },
  {
    label: '操作时间',
    field: 'fieldTime',
    component: 'RangePicker',
    componentProps: { valueType: 'Date' },
    colProps: { span: 5 },
  },
];
