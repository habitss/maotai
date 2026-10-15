import { BasicColumn } from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '点位ID',
    align: 'center',
    sorter: true,
    dataIndex: 'tagId_dictText',
  },
  {
    title: '版本名称',
    align: 'center',
    dataIndex: 'verName',
  },
  {
    title: '开始日期',
    align: 'center',
    dataIndex: 'effectSdate',
  },
  {
    title: '公式描述',
    align: 'center',
    dataIndex: 'formulaDesc',
  },
];

// 高级查询数据
export const superQuerySchema = {
  factoryCode: { title: '工厂', order: 0, view: 'list', type: 'string', dictTable: 'sys_depart', dictCode: 'org_code', dictText: 'depart_name' },
  deptCode: { title: '部门', order: 1, view: 'list', type: 'string', dictTable: 'sys_depart', dictCode: 'org_code', dictText: 'depart_name' },
  lineCode: { title: '产线', order: 2, view: 'list', type: 'string', dictTable: 'sys_depart', dictCode: 'org_code', dictText: 'depart_name' },
  tagId: { title: '点位ID', order: 3, view: 'list', type: 'string', dictTable: 'ems_orsdata_tag', dictCode: 'tag_id', dictText: 'tag_name' },
  verName: { title: '版本名称', order: 4, view: 'text', type: 'string' },
  effectSdate: { title: '开始日期', order: 6, view: 'datetime', type: 'string' },
  formulaDesc: { title: '公式描述', order: 8, view: 'text', type: 'string' },
};

//中间数据点位公式
export interface OrsdataFormula {
  id: number;
  tagid: string;
  verName: string;
  verDesc: string;
  effectSdate: string;
  effectEdate: string;
  formulaOrigin: string;
  formulaDesc: string;
  createBy: string;
  updateBy: string;
  updateTime: string;
}
