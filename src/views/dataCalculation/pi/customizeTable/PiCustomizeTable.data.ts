import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
   {
    title: '工厂',
    align:"center",
    dataIndex: 'factoryCode_dictText'
   },
   {
    title: '部门',
    align:"center",
    dataIndex: 'deptCode_dictText'
   },
  // {
  //   title: '工段',
  //   align:"center",
  //   dataIndex: 'section_dictText'
  //  },
   {
    title: '产线',
    align:"center",
    dataIndex: 'lineCode_dictText'
   },
   {
    title: '报表名称',
    align:"center",
    dataIndex: 'name',
    width: 150,
   },
   {
    title: '表名',
    align:"center",
    dataIndex: 'tableName',
    width: 170,
   },
   {
    title: '配置字段数',
    align:"center",
    dataIndex: 'configureField',
    width: 90,
   },
   {
    title: '配置算法数',
    align:"center",
    dataIndex: 'configureAlgorithm',
    width: 90,
   },
   {
    title: '未配置算法数',
    align:"center",
    dataIndex: 'noConfigureAlgorithm',
    width: 110,
   },
   {
    title: '数据保留天数',
    align:"center",
    dataIndex: 'days',
    width: 120,
   },
   {
    title: '是否自动报表',
    align:"center",
    dataIndex: 'automatic',
    width: 120,
    customRender:function (t,r,index) {
      let str = "手动报表";
      if(t.value == '1'){
        str = "自动报表";
      }
      return str;
    }
   },
   {
    title: '备注说明',
    align:"center",
    dataIndex: 'notes'
   },
   {
    title: '排序',
    align:"center",
    dataIndex: 'sort'
   },
  //  {
  //   title: '删除状态(0未删,1已删)',
  //   align:"center",
  //   dataIndex: 'delFlag'
  //  },
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
    label: '表名',
    field: 'tableName',
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
export function getBpmFormSchema(_formData): FormSchema[]{
  // 默认和原始表单保持一致 如果流程中配置了权限数据，这里需要单独处理formSchema
  return formSchema;
}