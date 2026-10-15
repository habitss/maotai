import { h } from 'vue';
import type { Ref } from 'vue';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { WORKSHOP_PROCESSES } from './WorkOrder.api';
import { workshopNameMap } from './WorkOrder.data';


// 弹窗内下拉/日期面板统一挂 body:ModalWrapper 滚动容器的定位上下文会把 absolute 面板带偏
const POPUP_BODY = { getPopupContainer: () => document.body };

const DICT_WORKSHOP = "mes3_mdm_dict where group_code='MES3_WORKSHOP' and del_flag=0, item_val, item_key";

/** 是否槽车工艺(卸车/装车:路径一端为卸车撬) */
function isTruck(p?: string) {
  return p === '卸车工艺' || p === '装车工艺';
}

// 列表数据
export const routeColumns: BasicColumn[] = [
  {
    title: '所属车间',
    align: 'center',
    dataIndex: 'workshopCode',
    width: 130,
    customRender: ({ record }) => workshopNameMap[record.workshopCode] || record.workshopCode || '—',
  },
  {
    title: '工艺',
    align: 'center',
    dataIndex: 'processType',
    width: 90,
  },
  {
    title: '源罐',
    align: 'center',
    dataIndex: 'sourceTank',
    width: 110,
    customRender: ({ record }) => record.sourceTank || '—',
  },
  {
    title: '目标罐',
    align: 'center',
    dataIndex: 'targetTank',
    width: 110,
    customRender: ({ record }) => record.targetTank || '—',
  },
  {
    title: '卸车撬',
    align: 'center',
    dataIndex: 'skidCode',
    width: 90,
    customRender: ({ record }) => record.skidCode || '—',
  },
  {
    title: '路径编码',
    align: 'center',
    dataIndex: 'routeCode',
    width: 110,
    resizable: true,
  },
  {
    title: '路径名称',
    align: 'center',
    dataIndex: 'routeName',
    resizable: true,
  },
  {
    title: '排序',
    align: 'center',
    dataIndex: 'sortNo',
    width: 70,
  },
  {
    title: '状态',
    align: 'center',
    dataIndex: 'enabled',
    width: 80,
    customRender: ({ record }) =>
      h(
        'span',
        {
          style: {
            color: record.enabled === 1 ? '#10B981' : '#6B7280',
            background: record.enabled === 1 ? '#ECFDF5' : '#F5F5F4',
            fontWeight: 600,
            padding: '2px 10px',
            borderRadius: '12px',
            display: 'inline-block',
          },
        },
        record.enabled === 1 ? '启用' : '停用',
      ),
  },
];

// 查询数据
export const routeSearchFormSchema: FormSchema[] = [
  {
    label: '所属车间',
    field: 'workshopCode',
    component: 'JDictSelectTag',
    componentProps: { dictCode: DICT_WORKSHOP, placeholder: '请选择车间' },
    colProps: { span: 5 },
  },
  {
    label: '工艺',
    field: 'processType',
    component: 'Select',
    componentProps: {
      options: ['卸车工艺', '装车工艺', '倒罐工艺', '灌装工艺'].map((p) => ({ label: p, value: p })),
      placeholder: '请选择工艺',
    },
    colProps: { span: 5 },
  },
  {
    label: '路径编码',
    field: 'routeCode',
    component: 'Input',
    componentProps: { placeholder: '请输入路径编码' },
    colProps: { span: 5 },
  },
];

/**
 * 路径配置表单 schema 工厂:车间→工艺联动,罐下拉(设备台账)注入,
 * 卸车=撬+目标罐 / 装车=撬+源罐 / 倒罐·灌装=双罐,按工艺动态显隐。
 */
export function buildRouteSchemas(
  tankOptions: Ref<{ label: string; value: string }[]>,
  skidOptions: Ref<{ label: string; value: string }[]>,
  hooks: { onWorkshopChange: () => void; onProcessChange: () => void },
): FormSchema[] {
  return [
    {
      label: '所属车间',
      field: 'workshopCode',
      component: 'JDictSelectTag',
      componentProps: { ...POPUP_BODY, dictCode: DICT_WORKSHOP, placeholder: '请选择车间', onChange: () => hooks.onWorkshopChange() },
      required: true,
      dynamicDisabled: ({ values }) => !!values?.id,
      colProps: { span: 12 },
    },
    {
      label: '工艺',
      field: 'processType',
      component: 'Select',
      componentProps: ({ formModel }) => ({
        ...POPUP_BODY,
        options: (WORKSHOP_PROCESSES[formModel.workshopCode] || []).map((p) => ({ label: p, value: p })),
        showSearch: true,
        optionFilterProp: 'label',
        placeholder: '请选择工艺',
        onChange: () => hooks.onProcessChange(),
      }),
      required: true,
      dynamicDisabled: ({ values }) => !!values?.id,
      colProps: { span: 12 },
    },
    {
      label: '源罐',
      field: 'sourceTank',
      component: 'Select',
      componentProps: () => ({
        ...POPUP_BODY,
        options: tankOptions.value,
        showSearch: true,
        optionFilterProp: 'label',
        placeholder: '请选择源罐',
      }),
      // 卸车工艺无源罐(源端=卸车撬)
      ifShow: ({ values }) => values?.processType !== '卸车工艺',
      dynamicRules: ({ values }) => (values?.processType && values.processType !== '卸车工艺' ? [{ required: true, message: '请选择源罐' }] : []),
      colProps: { span: 12 },
    },
    {
      label: '卸车撬',
      field: 'skidCode',
      component: 'Select',
      componentProps: () => ({
        ...POPUP_BODY,
        options: skidOptions.value,
        placeholder: '请选择卸车撬',
      }),
      ifShow: ({ values }) => isTruck(values?.processType),
      dynamicRules: ({ values }) => (isTruck(values?.processType) ? [{ required: true, message: '请选择卸车撬' }] : []),
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
      }),
      // 装车工艺无目标罐(目标端=卸车撬)
      ifShow: ({ values }) => values?.processType !== '装车工艺',
      dynamicRules: ({ values }) => (values?.processType && values.processType !== '装车工艺' ? [{ required: true, message: '请选择目标罐' }] : []),
      colProps: { span: 12 },
    },
    {
      label: '路径编码',
      field: 'routeCode',
      component: 'Input',
      componentProps: { placeholder: '如：Z100FX01' },
      required: true,
      colProps: { span: 12 },
    },
    {
      label: '路径名称',
      field: 'routeName',
      component: 'Input',
      componentProps: { placeholder: '如：1#卸车线' },
      required: true,
      colProps: { span: 12 },
    },
    {
      label: '排序',
      field: 'sortNo',
      component: 'InputNumber',
      componentProps: { min: 0, style: { width: '100%' }, placeholder: '候选下拉展示顺序' },
      colProps: { span: 12 },
    },
    {
      label: '状态',
      field: 'enabled',
      component: 'Select',
      componentProps: { ...POPUP_BODY, options: [{ label: '启用', value: 1 }, { label: '停用', value: 0 }] },
      defaultValue: 1,
      colProps: { span: 12 },
    },
    {
      label: '备注',
      field: 'remark',
      component: 'InputTextArea',
      componentProps: { rows: 2, placeholder: '可选填写备注' },
      colProps: { span: 24 },
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
