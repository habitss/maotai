import { BasicColumn } from '/@/components/Table';

//列表数据
export const columns: BasicColumn[] = [
  {
    title: '类型',
    align: 'center',
    dataIndex: 'type_dictText',
    width: 100,
  },
  {
    title: '请求参数',
    align: 'center',
    dataIndex: 'requestParams',
    customRender: ({ text }) => {
      return text ? text.split('?')[1] : '-';
    },
  },
  {
    title: '返回结果',
    align: 'center',
    dataIndex: 'responseResult',
  },
  {
    title: '请求时间',
    align: 'center',
    dataIndex: 'requestTime',
    width: 180,
  },
  {
    title: '响应时间',
    align: 'center',
    dataIndex: 'responseTime',
    width: 180,
  },
  {
    title: '请求状态',
    align: 'center',
    dataIndex: 'status_dictText',
    width: 100,
  },
];

// 高级查询数据
export const superQuerySchema = {
  type: {
    title: '类型',
    order: 0,
    view: 'number',
    type: 'number',
    dictTable: "mes3_mdm_dict WHERE group_code='TSM_LogType'",
    dictCode: 'item_val',
    dictText: 'item_key',
  },
  requestParams: { title: '请求参数', order: 1, view: 'textarea', type: 'string' },
  responseResult: { title: '返回结果', order: 2, view: 'textarea', type: 'string' },
  requestTime: { title: '请求时间', order: 3, view: 'datetime', type: 'string' },
  responseTime: { title: '响应时间', order: 4, view: 'datetime', type: 'string' },
  status: {
    title: '请求状态',
    order: 5,
    view: 'number',
    type: 'number',
    dictTable: "mes3_mdm_dict WHERE group_code='TSM_ReqStatus'",
    dictCode: 'item_val',
    dictText: 'item_key',
  },
};
