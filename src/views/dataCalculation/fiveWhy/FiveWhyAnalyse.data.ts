import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';

//处理设置过labelInValue的值
export function validSelectValue(value) {
  return Object.prototype.toString.call(value) === '[object Object]' ? value.key : value;
}
//列表数据
export const columns: BasicColumn[] = [
   // {
   //  title: '租户ID',
   //  align:"center",
   //  dataIndex: 'tenantId'
   // },
   // {
   //  title: '工厂',
   //  align:"center",
   //  dataIndex: 'factoryCode'
   // },
   // {
   //  title: '部门',
   //  align:"center",
   //  dataIndex: 'deptCode'
   // },
   // {
   //  title: '产线',
   //  align:"center",
   //  dataIndex: 'lineCode'
   // },
   // {
   //  title: '工段',
   //  align:"center",
   //  dataIndex: 'section'
   // },
   {
    title: '问题',
    align:"center",
    dataIndex: 'problem'
   },
   {
    title: '问题描述',
    align:"center",
    dataIndex: 'problemDesc'
   },
   {
    title: '报表',
    align:"center",
    dataIndex: 'tableName'
   },
   {
    title: 'PI名称',
    align:"center",
    dataIndex: 'fieldName'
   },
   {
    title: '创建时间',
    align:"center",
    dataIndex: 'createTime'
   },
   // {
   //  title: 'pi编码',
   //  align:"center",
   //  dataIndex: 'piCode'
   // },
   // {
   //  title: 'kpi名称',
   //  align:"center",
   //  dataIndex: 'kpiName'
   // },
   // {
   //  title: 'kpi编码',
   //  align:"center",
   //  dataIndex: 'kpiCode'
   // },
   // {
   //  title: '原因',
   //  align:"center",
   //  dataIndex: 'reason'
   // },
   // {
   //  title: '标题',
   //  align:"center",
   //  dataIndex: 'title'
   // },
   // {
   //  title: '5Wjson数据',
   //  align:"center",
   //  dataIndex: 'reasonJson'
   // },
   // {
   //  title: '措施json数据',
   //  align:"center",
   //  dataIndex: 'measuresJson'
   // },
   // {
   //  title: '排序',
   //  align:"center",
   //  dataIndex: 'sort'
   // },
   // {
   //  title: '备注说明',
   //  align:"center",
   //  dataIndex: 'remark'
   // },
   // {
   //  title: '操作',
   //  align: "center",
   //  dataIndex: 'action',
   //  width: 100,
   //  fixed: 'right',
   //  slots: {
   //    default: 'action'
   //  }
   // }
];
//查询数据
export const searchFormSchema: FormSchema[] = [
	{
    label: '报表',
    field: 'tableCode',
    component: 'JDictSelectTag',
    componentProps: ({ formModel }) => {
    return {
      dictCode: 'mes3_pi_customize_table, name, table_name, del_flag = 0',
      showChooseOption: false,
      showSearch: true,
      labelInValue: true,
      onChange: e => {
        formModel.tableName = e ? e.label[0].children : '';
        formModel.fieldCode = null;
      },
    }
  },
  },
	{
    label: 'PI名称',
    field: 'fieldCode',
    component: 'JDictSelectTag',
    componentProps: ({ formModel }) => {
    return {
      dictCode: 'mes3_pi_customize_table_field, name, field_name, table_name = \'' + (validSelectValue(formModel.tableCode) || '') + '\' and del_flag = 0',
      showChooseOption: false,
      showSearch: true,
      labelInValue: true,
      onChange: e => {
        formModel.fieldName = e ? e.label[0].children : '';
      },
    }
  },
  },
	{
      label: "异常点",
      field: 'problem',
      component: 'Input',
      //colProps: {span: 6},
 	},
	
     {
      label: "创建时间",
      field: "createTime",
      component: 'RangePicker',
      componentProps: {
          valueType: 'Date',
          showTime:true
      },
      //colProps: {span: 6},
	},
];

