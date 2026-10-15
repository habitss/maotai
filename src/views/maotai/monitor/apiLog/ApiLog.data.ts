import { h } from 'vue';
import { BasicColumn, FormSchema } from '/@/components/Table';

/** 方向 IN=入站(外部→平台) OUT=出站(平台→外部) */
const DIRECTION_MAP: Record<string, { text: string; color: string }> = {
  IN: { text: '入站', color: '#1890ff' },
  OUT: { text: '出站', color: '#52c41a' },
};

/** 结果 1=成功 0=失败 */
const RESULT_MAP: Record<number, { text: string; color: string }> = {
  1: { text: '成功', color: '#52c41a' },
  0: { text: '失败', color: '#f5222d' },
};

export const columns: BasicColumn[] = [
  {
    title: '方向',
    dataIndex: 'direction',
    width: 70,
    customRender: ({ record }) => {
      const m = DIRECTION_MAP[record.direction];
      return m ? h('span', { style: { color: m.color, fontWeight: 600 } }, m.text) : record.direction;
    },
  },
  {
    title: '接口',
    dataIndex: 'apiCode',
    width: 90,
  },
  {
    title: '工单号',
    dataIndex: 'orderNo',
    width: 140,
    align: 'center',
  },
  {
    title: '操作方',
    dataIndex: 'operator',
    width: 100,
    align: 'center',
  },
  {
    title: '结果',
    dataIndex: 'result',
    width: 70,
    customRender: ({ record }) => {
      const m = RESULT_MAP[record.result];
      return m ? h('span', { style: { color: m.color, fontWeight: 600 } }, m.text) : record.result;
    },
  },
  {
    title: '处理信息',
    dataIndex: 'processMsg',
    width: 200,
    align: 'center',
    ellipsis: true,
  },
  {
    title: '重试',
    dataIndex: 'retryCount',
    width: 60,
    customRender: ({ record }) => (record.retryCount ? `${record.retryCount}次` : '-'),
  },
  {
    title: '操作时间',
    dataIndex: 'operateTime',
    sorter: true,
    width: 150,
    align: 'center',
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'direction',
    label: '方向',
    component: 'Select',
    labelWidth: 70,
    colProps: { span: 5 },
    componentProps: {
      placeholder: '请选择方向',
      options: [
        { label: '入站(外部→平台)', value: 'IN' },
        { label: '出站(平台→外部)', value: 'OUT' },
      ],
    },
  },
  {
    field: 'apiCode',
    label: '接口编码',
    component: 'Input',
    labelWidth: 70,
    colProps: { span: 5 },
    componentProps: { placeholder: '请输入接口编码' },
  },
  {
    field: 'orderNo',
    label: '工单号',
    component: 'Input',
    labelWidth: 70,
    colProps: { span: 5 },
    componentProps: { placeholder: '请输入工单号' },
  },
  {
    field: 'operator',
    label: '操作方',
    component: 'Input',
    labelWidth: 70,
    colProps: { span: 5 },
    componentProps: { placeholder: '请输入操作方' },
  },
  {
    field: 'result',
    label: '结果',
    component: 'Select',
    labelWidth: 70,
    colProps: { span: 5 },
    componentProps: {
      placeholder: '请选择结果',
      options: [
        { label: '成功', value: 1 },
        { label: '失败', value: 0 },
      ],
    },
  },
  {
    field: 'fieldTime',
    component: 'RangePicker',
    label: '操作时间',
    labelWidth: 70,
    componentProps: {
      valueType: 'Date',
    },
    colProps: { span: 7 },
  },
];
