import { Tag } from 'ant-design-vue';
import { h } from 'vue';
import { BasicColumn, FormSchema } from '/@/components/Table';

// 运行状态字典:1在线 2停机 3维修 4报警(runStatus 字段为 Integer,直接用 options 与类型保持一致,避免 JDictSelectTag 字典字符串 key 匹配不上数字 v-model)
const RUN_STATUS_MAP: Record<number, { text: string; color: string; bg: string }> = {
  1: { text: '在线', color: '#10B981', bg: '#ECFDF5' },
  2: { text: '停机', color: '#A8A29E', bg: '#F5F5F4' },
  3: { text: '维修', color: '#D97706', bg: '#FFFBEB' },
  4: { text: '报警', color: '#B91C1C', bg: '#FEF2F2' },
};
// 运行状态下拉选项(value 为 number,与实体 Integer 一致)
const RUN_STATUS_OPTIONS = Object.keys(RUN_STATUS_MAP).map((k) => ({ value: Number(k), label: RUN_STATUS_MAP[Number(k)].text }));

// 字典 dictCode(显示列在前,值列在后)
const DICT_WORKSHOP = "mes3_mdm_dict where group_code='MES3_WORKSHOP' and del_flag=0, item_val, item_key";
const DICT_MAINT_CYCLE = "mes3_mdm_dict where group_code='MES3_MAINT_CYCLE' and del_flag=0, item_val, item_key";

// 列表数据
export const columns: BasicColumn[] = [
  {
    title: '功能位置编码',
    align: 'center',
    dataIndex: 'funlocCode',
    width: 140,
    resizable: true,
  },
  {
    title: '设备名称',
    align: 'center',
    dataIndex: 'equipName',
    width: 150,
    resizable: true,
  },
  {
    title: '所属车间',
    align: 'center',
    dataIndex: 'workshopCode_dictText',
    width: 160,
    resizable: true,
  },
  {
    title: '设备型号',
    align: 'center',
    dataIndex: 'equipModel',
    width: 110,
  },
  {
    title: '制造商',
    align: 'center',
    dataIndex: 'manufacturer',
    width: 150,
    resizable: true,
  },
  {
    title: '安装日期',
    align: 'center',
    dataIndex: 'installDate',
    width: 110,
  },
  {
    title: '罐容量(m³)',
    align: 'center',
    dataIndex: 'tankCapacity',
    width: 100,
    customRender: ({ record }) => record.tankCapacity ?? '—',
  },
  {
    title: '运行状态',
    align: 'center',
    dataIndex: 'runStatus',
    width: 90,
    customRender: ({ record }) => {
      const m = RUN_STATUS_MAP[record.runStatus];
      if (!m) return record.runStatus;
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
    },
  },
  {
    title: '最近保养',
    align: 'center',
    dataIndex: 'lastMaintDate',
    width: 110,
  },
];

// 查询数据(四条件+按钮同一行:span 压缩 + label 窄化由列表页 labelWidth 控制)
export const searchFormSchema: FormSchema[] = [
  {
    label: '功能位置编码',
    field: 'funlocCode',
    component: 'Input',
    componentProps: { placeholder: '请输入功能位置编码' },
    colProps: { span: 5 },
  },
  {
    label: '设备名称',
    field: 'equipName',
    component: 'Input',
    componentProps: { placeholder: '请输入设备名称' },
    colProps: { span: 5 },
  },
  {
    label: '所属车间',
    field: 'workshopCode',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: DICT_WORKSHOP,
      placeholder: '请选择车间',
    },
    colProps: { span: 5 },
  },
  {
    label: '运行状态',
    field: 'runStatus',
    component: 'JDictSelectTag',
    componentProps: {
      options: RUN_STATUS_OPTIONS,
      placeholder: '请选择状态',
    },
    colProps: { span: 5 },
  },
];

// 表单数据(新增/编辑/详情;两列布局——baseColProps span:12 由 Form.vue 设置,备注单独跨整行)
export const addFormSchemas: FormSchema[] = [
  {
    label: '功能位置编码',
    field: 'funlocCode',
    component: 'Input',
    componentProps: { placeholder: '请输入功能位置编码' },
    required: true,
    // 可编辑(2026-09-16 放开:罐体关联稳定键已是 tank_config_id,罐移动/换位可直接改编码,驾驶舱实时跟随台账)
    // 唯一性由后端租户内唯一校验保证,
    colProps: { span: 12 },
  },
  {
    label: '设备名称',
    field: 'equipName',
    component: 'Input',
    componentProps: { placeholder: '请输入设备名称' },
    required: true,
    colProps: { span: 12 },
  },
  {
    label: '所属车间',
    field: 'workshopCode',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: DICT_WORKSHOP,
      placeholder: '请选择车间',
    },
    colProps: { span: 12 },
  },
  {
    label: '设备型号',
    field: 'equipModel',
    component: 'Input',
    componentProps: { placeholder: '请输入设备型号' },
    colProps: { span: 12 },
  },
  {
    label: '制造商',
    field: 'manufacturer',
    component: 'Input',
    componentProps: { placeholder: '请输入制造商' },
    colProps: { span: 12 },
  },
  {
    label: '安装日期',
    field: 'installDate',
    component: 'DatePicker',
    componentProps: { valueFormat: 'YYYY-MM-DD', style: { width: '100%' } },
    colProps: { span: 12 },
  },
  {
    label: '投运日期',
    field: 'commissionDate',
    component: 'DatePicker',
    componentProps: { valueFormat: 'YYYY-MM-DD', style: { width: '100%' } },
    colProps: { span: 12 },
  },
  {
    label: '额定功率',
    field: 'ratedPower',
    component: 'Input',
    componentProps: { placeholder: '如:450 kW' },
    colProps: { span: 12 },
  },
  {
    label: '安装位置',
    field: 'installLocation',
    component: 'Input',
    componentProps: { placeholder: '如:A区-01号机位' },
    colProps: { span: 12 },
  },
  {
    label: '罐体容量(m³)',
    field: 'tankCapacity',
    component: 'InputNumber',
    componentProps: { min: 0, style: { width: '100%' }, placeholder: '罐体容量,如:240' },
    // 仅罐体设备需要;非罐设备留空。驾驶舱/车间预览的容量显示以此为准(修改后 10s 内跟随)
    colProps: { span: 12 },
  },
  {
    label: '保养周期',
    field: 'maintCycle',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: DICT_MAINT_CYCLE,
      placeholder: '请选择保养周期',
    },
    colProps: { span: 12 },
  },
  {
    label: '设计寿命',
    field: 'designLife',
    component: 'Input',
    componentProps: { placeholder: '如:15年' },
    colProps: { span: 12 },
  },
  {
    label: '运行状态',
    field: 'runStatus',
    component: 'JDictSelectTag',
    // 编辑时状态由驾驶舱罐体实时同步,人工不可改——渲染为只读彩色标签
    ifShow: ({ values }) => !values?.id,
    componentProps: {
      options: RUN_STATUS_OPTIONS,
      placeholder: '请选择状态',
    },
    colProps: { span: 12 },
  },
  {
    label: '运行状态',
    field: 'runStatus',
    component: 'Input',
    // 编辑态只读标签:值来自驾驶舱实时同步,非人工可编辑字段
    ifShow: ({ values }) => !!values?.id,
    render: ({ values }) => {
      const m = RUN_STATUS_MAP[values?.runStatus];
      if (!m) return h(Tag, { color: 'default' }, () => String(values?.runStatus ?? '—'));
      return h(Tag, { color: m.bg, style: `color:${m.color};border:1px solid ${m.color}33` }, () => m.text);
    },
    colProps: { span: 12 },
  },
  {
    label: '联系电话',
    field: 'contactPhone',
    component: 'Input',
    componentProps: { placeholder: '请输入联系电话' },
    colProps: { span: 12 },
  },
  {
    label: '最近保养',
    field: 'lastMaintDate',
    component: 'DatePicker',
    componentProps: { valueFormat: 'YYYY-MM-DD', style: { width: '100%' } },
    colProps: { span: 12 },
  },
  {
    label: '备注',
    field: 'remark',
    component: 'InputTextArea',
    componentProps: { rows: 2, placeholder: '可选填写设备备注信息' },
    // 跨剩余宽度:label 起点与第一列对齐,输入框右缘与第二列末尾对齐(span 24 而非 12)
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
