import { BasicColumn } from '/@/components/Table';

//列表数据
export const columns: BasicColumn[] = [
  {
    title: '部门Code',
    align: 'center',
    dataIndex: 'deptCode_dictText',
  },
  {
    title: '版本名称',
    align: 'center',
    dataIndex: 'verName',
  },
  {
    title: '版本描述',
    align: 'center',
    dataIndex: 'verDesc',
  },
  {
    title: '开始日期',
    align: 'center',
    sorter: true,
    dataIndex: 'effectSdate',
  },
];

// 高级查询数据
export const superQuerySchema = {
  deptCode: { title: '部门Code', order: 0, view: 'list', type: 'string', dictTable: 'sys_depart', dictCode: 'org_code', dictText: 'depart_name' },
  verName: { title: '版本名称', order: 1, view: 'text', type: 'string' },
  verDesc: { title: '版本描述', order: 2, view: 'text', type: 'string' },
  effectSdate: { title: '开始日期', order: 3, view: 'datetime', type: 'string' },
};

//报表版本类型
export interface ReportVersion {
  id: number;
  repid: number;
  verName: string;
  verDesc: string;
  effectSdate: string;
  effectEdate: string;
  createBy: string;
  updateBy: string;
  updateTime: string;
}
