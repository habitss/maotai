import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
//列表数据
export const columns: BasicColumn[] = [
  //  {
  //   title: '工厂',
  //   align:"center",
  //   dataIndex: 'factory'
  //  },
  //  {
  //   title: '部门',
  //   align:"center",
  //   dataIndex: 'dept'
  //  },
  //  {
  //   title: '产线',
  //   align:"center",
  //   dataIndex: 'lineId'
  //  },
  //  {
  //   title: '自定义表id',
  //   align:"center",
  //   dataIndex: 'tableid'
  //  },
  {
    title: '字段中文名称',
    align:"center",
    dataIndex: 'name',
    width: 300,
    fixed: 'left'
   },
   {
    title: '字段名',
    align:"center",
    dataIndex: 'fieldName',
    width: 100,
   },
   {
    title: '字段长度',
    align:"center",
    dataIndex: 'fieldLength',
    width: 100,
   },
  //  {
  //   title: '小数点',
  //   align:"center",
  //   dataIndex: 'decimalpoint'
  //  },
  //  {
  //   title: '默认值',
  //   align:"center",
  //   dataIndex: 'fielddefault'
  //  },
   {
    title: '字段类型',
    align:"center",
    dataIndex: 'type_dictText',
    width: 100,
   },
  //  {
  //   title: '是否表单显示(0否/1是)',
  //   align:"center",
  //   dataIndex: 'isForm'
  //  },
   {
    title: '是否列表显示',
    align:"center",
    dataIndex: 'isList',
    width: 80,
    customRender: ({ record }) => {
      return record.isList == '1' ? "是" : "否";
    }
   },
   {
    title: '是否排序',
    align:"center",
    dataIndex: 'isSort',
    width: 80,
    customRender:function (t) {
      let str = "否";
      if(t.value == '1'){
        str = "是";
      }
      return str;
    }
   },
   {
    title: '排序方式',
    align:"center",
    dataIndex: 'sortType',
    width: 80,
    customRender:function (t) {
      if(!t.value && 0 != t.value){
        return '';
      }
      let str = "降序";
      if(t.value == '1'){
        str = "升序";
      }
      console.log('str :>> ', str);
      return str;
    }
   },
   {
    title: '是否查询',
    align:"center",
    dataIndex: 'isQuery',
    width: 80,
    customRender:function (t) {
      let str = "否";
      if(t.value == '1'){
        str = "是";
      }
      return str;
    }
   },
   {
    title: '排序',
    align:"center",
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
    field: 'lineId',
    component: 'Input',
  },
  {
    label: '自定义表id',
    field: 'tableid',
    component: 'InputNumber',
  },
  {
    label: '字段名',
    field: 'fieldname',
    component: 'Input',
  },
  {
    label: '字段备注',
    field: 'notes',
    component: 'Input',
  },
  {
    label: '字段长度',
    field: 'fieldlength',
    component: 'InputNumber',
  },
  {
    label: '小数点',
    field: 'decimalpoint',
    component: 'InputNumber',
  },
  {
    label: '默认值',
    field: 'fielddefault',
    component: 'Input',
  },
  {
    label: '字段类型',
    field: 'type',
    component: 'Input',
  },
  {
    label: '是否表单显示(0否/1是)',
    field: 'isform',
    component: 'InputNumber',
  },
  {
    label: '是否列表显示(0否/1是)',
    field: 'islist',
    component: 'InputNumber',
  },
  {
    label: '是否排序(0否/1是)',
    field: 'issort',
    component: 'InputNumber',
  },
  {
    label: '排序方式(0降序/1升序)',
    field: 'sorttype',
    component: 'InputNumber',
  },
  {
    label: '是否查询(0否/1是)',
    field: 'isquery',
    component: 'InputNumber',
  },
  {
    label: '排序',
    field: 'sortNum',
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