import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { rules } from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '工厂',
    align: "center",
    dataIndex: 'factoryCode_dictText',
    width: 100,
  },
  {
    title: '部门',
    align: "center",
    dataIndex: 'deptCode_dictText',
    width: 100,
  },
  {
    title: '产线',
    align: "center",
    dataIndex: 'lineCode_dictText',
    width: 100,
  },
  {
    title: '报表名称',
    align: "center",
    dataIndex: 'name'
  },
  {
    title: '表名',
    align: "center",
    dataIndex: 'tableName'
  },
  {
    title: '备注说明',
    align: "center",
    dataIndex: 'notes'
  },
  {
    title: '排序',
    align: "center",
    dataIndex: 'sort',
    width: 80,
  },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '工厂',
    field: 'factory',
    component: 'Input',
  },
  {
    label: '部门',
    field: 'dept',
    component: 'Input',
  },
  {
    label: '产线',
    field: 'line',
    component: 'Input',
  },
  {
    label: '工段',
    field: 'section',
    component: 'Input',
  },
  {
    label: '报表名称',
    field: 'name',
    component: 'Input',
  },
  {
    label: '备注说明',
    field: 'notes',
    component: 'Input',
  },
  {
    label: '排序',
    field: 'sort',
    component: 'InputNumber',
  },
  {
    label: '删除状态(0未删,1已删)',
    field: 'delFlag',
    component: 'InputNumber',
  },
  // TODO 主键隐藏字段，目前写死为ID
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false
  },
];



/**
* 流程表单调用这个方法获取formSchema
* @param param
*/
export function getBpmFormSchema(_formData): FormSchema[] {
  // 默认和原始表单保持一致 如果流程中配置了权限数据，这里需要单独处理formSchema
  return formSchema;
}