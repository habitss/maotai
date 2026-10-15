import { BasicColumn, FormSchema } from '/@/components/Table';
import { h } from 'vue';

// 状态映射(含背景色,与字典 MES3_MAINT_STATUS 对应)
const STATUS_MAP: Record<string, { text: string; color: string; bg: string }> = {
  pending: { text: '待执行', color: '#F59E0B', bg: '#FFFBEB' },
  progress: { text: '进行中', color: '#3B82F6', bg: '#EFF6FF' },
  completed: { text: '已完成', color: '#10B981', bg: '#ECFDF5' },
  overdue: { text: '逾期', color: '#F43F5E', bg: '#FEF2F2' },
};

// 字典 dictCode(显示列在前,值列在后)
const DICT_MAINT_TYPE = "mes3_mdm_dict where group_code='MES3_MAINT_TYPE' and del_flag=0, item_val, item_key";
const DICT_MAINT_PRIORITY = "mes3_mdm_dict where group_code='MES3_MAINT_PRIORITY' and del_flag=0, item_val, item_key";
const DICT_MAINT_STATUS = "mes3_mdm_dict where group_code='MES3_MAINT_STATUS' and del_flag=0, item_val, item_key";
// 设备下拉:关联设备台账
const DICT_EQUIPMENT = "mes3_maotai_equipment_ledger where del_flag=0, equip_name, id";

// 列表数据
export const columns: BasicColumn[] = [
  { title: '工单号', align: 'center', dataIndex: 'orderNo', width: 180 },
  { title: '设备名称', align: 'center', dataIndex: 'equipmentName', width: 150, resizable: true },
  { title: '保养类型', align: 'center', dataIndex: 'maintType_dictText', width: 110 },
  { title: '计划日期', align: 'center', dataIndex: 'planDate', width: 110 },
  { title: '完成日期', align: 'center', dataIndex: 'completeDate', width: 110 },
  { title: '执行人', align: 'center', dataIndex: 'executor', width: 100 },
  {
    title: '状态',
    align: 'center',
    dataIndex: 'status',
    width: 100,
    customRender: ({ record }) => {
      const m = STATUS_MAP[record.status];
      if (!m) return record.status_dictText || record.status;
      return h(
        'span',
        { style: { color: m.color, background: m.bg, fontWeight: 600, padding: '2px 10px', borderRadius: '12px', display: 'inline-block' } },
        m.text,
      );
    },
  },
];

// 查询数据(条件分开,各自独立)
export const searchFormSchema: FormSchema[] = [
  { label: '工单号', field: 'orderNo', component: 'Input', componentProps: { placeholder: '工单号' }, colProps: { span: 6 } },
  { label: '设备名称', field: 'equipmentName', component: 'Input', componentProps: { placeholder: '设备名称' }, colProps: { span: 6 } },
  { label: '执行人', field: 'executor', component: 'Input', componentProps: { placeholder: '执行人' }, colProps: { span: 6 } },
  {
    label: '保养类型',
    field: 'maintType',
    component: 'JDictSelectTag',
    componentProps: { dictCode: DICT_MAINT_TYPE, placeholder: '请选择类型' },
    colProps: { span: 6 },
  },
  {
    label: '状态',
    field: 'status',
    component: 'JDictSelectTag',
    componentProps: { dictCode: DICT_MAINT_STATUS, placeholder: '请选择状态' },
    colProps: { span: 6 },
  },
  {
    label: '计划日期',
    field: 'planDate',
    component: 'RangePicker',
    componentProps: { valueFormat: 'YYYY-MM-DD', style: { width: '100%' } },
    // 与其他条件同为 span6:RangePicker 跟列宽走,框宽和设备名称等输入框一致
    colProps: { span: 6 },
  },
];

// 表单数据(新增/编辑/详情)
export const addFormSchemas: FormSchema[] = [
  // 工单号:新增时后端生成,只读
  {
    label: '工单号',
    field: 'orderNo',
    component: 'Input',
    componentProps: { placeholder: '保存后自动生成', disabled: true },
    ifShow: ({ values }) => !!values.id,
  },
  {
    label: '所属设备',
    field: 'equipmentId',
    component: 'JDictSelectTag',
    componentProps: { dictCode: DICT_EQUIPMENT, placeholder: '请选择设备' },
    required: true,
  },
  {
    label: '保养类型',
    field: 'maintType',
    component: 'JDictSelectTag',
    componentProps: { dictCode: DICT_MAINT_TYPE, placeholder: '请选择类型' },
    required: true,
  },
  {
    label: '计划日期',
    field: 'planDate',
    component: 'DatePicker',
    componentProps: { valueFormat: 'YYYY-MM-DD', style: { width: '100%' } },
    required: true,
  },
  {
    label: '状态',
    field: 'status',
    component: 'JDictSelectTag',
    componentProps: { dictCode: DICT_MAINT_STATUS, placeholder: '请选择状态' },
    defaultValue: 'pending',
  },
  {
    label: '优先级',
    field: 'priority',
    component: 'JDictSelectTag',
    componentProps: { dictCode: DICT_MAINT_PRIORITY, placeholder: '请选择优先级' },
  },
  {
    label: '完成日期',
    field: 'completeDate',
    component: 'DatePicker',
    componentProps: { valueFormat: 'YYYY-MM-DD', style: { width: '100%' } },
  },
  { label: '执行人', field: 'executor', component: 'Input', componentProps: { placeholder: '请输入执行人' } },
  { label: '预估工时', field: 'estWorkhour', component: 'Input', componentProps: { placeholder: '如:2.5 小时' } },
  { label: '保养内容', field: 'maintContent', component: 'InputTextArea', componentProps: { rows: 3, placeholder: '请输入保养内容' } },
  { label: '所需备件', field: 'spareParts', component: 'Input', componentProps: { placeholder: '如:空气滤芯 x1,润滑油 x5L' } },
  { label: '备注', field: 'remark', component: 'InputTextArea', componentProps: { rows: 2, placeholder: '可选' } },
  { label: '', field: 'id', component: 'Input', show: false },
];
