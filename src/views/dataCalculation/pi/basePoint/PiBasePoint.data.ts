import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { useI18n } from '/@/hooks/web/useI18n';
//列表数据
export const columns: BasicColumn[] = [
   {
    title: '部门',
    align:"center",
    sorter: true,
    dataIndex: 'deptCode',
    customRender:function (t,r,index) {
      return t.record.deptCodeVal;
    }
   },
   {
    title: '产线',
    align:"center",
    sorter: true,
    dataIndex: 'lineCode',
    customRender:function (t,r,index) {
      return t.record.lineCodeVal;
    }
   },
   {
    title: '设备名称',
    align:"center",
    sorter: true,
    dataIndex: 'deviceId',
    customRender:function (t,r,index) {
      return t.record.deviceIdVal;
    }
   },
   {
    title: '点位编码',
    align:"center",
    sorter: true,
    dataIndex: 'pointCode',
   },
   {
    title: '点位类型',
    align:"center",
    sorter: true,
    dataIndex: 'pointType',
    customRender:function (t,r,index) {
        return t.record.pointTypeVal;
    }
   },
   {
    title: '点位中文描述',
    align:"center",
    resizable: true,
    sorter: true,
    dataIndex: 'pointName'
   },
   {
    title: 'IP地址',
    align:"center",
    dataIndex: 'ip'
   },
   {
    title: 'DB地址',
    align:"center",
    dataIndex: 'dbAddress'
   },
   {
    title: '数据类型',
    align:"center",
    dataIndex: 'dataType'
   },
   {
    title: '汉化方式',
    align:"center",
    dataIndex: 'cnWayVal',
   },
   {
    title: '字典表汉化编码',
    align:"center",
    dataIndex: 'dictCode_dictText'
   },
   {
    title: '点位功能位置编码',
    align:"center",
    dataIndex: 'tagFunlocCode'
   },
   {
    title: 'IOT属性编码',
    align:"center",
    dataIndex: 'iotAttrName'
   },
   {
    title: '数据属性',
    align:"center",
    dataIndex: 'dataAttributeVal'
   },
   {
    title: '绑定标准值',
    align:"center",
    dataIndex: 'bindStandardVal'
   },
   {
    title: '最新值',
    align:"center",
    dataIndex: 'latestValue'
   },
   {
    title: '最后更新时间',
    align:"center",
    width:"160px",
    dataIndex: 'lastUpdateTime'
   },
   {
    title: '排序',
    align:"center",
    dataIndex: 'sort'
   },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '部门',
    field: 'dept',
    component: 'JSelectDept',
    componentProps: {
      //是否开启异步加载
      sync: false,
      //是否显示复选框
      checkable: true,
      //是否显示选择按钮
      showButton: false,
      //父子节点选中状态不再关联
      checkStrictly: true,
      //选择框标题
      modalTitle: '部门选择',
    }
  },
  {
    label: '产线',
    field:"center",
    component: 'Input'
  },
  {
    label: '设备',
    field: 'deviceId',
    component: 'InputNumber',
  },
  {
    label: '点位编码',
    field: 'pointCode',
    component: 'Input',
  },
  {
    label: '点位类型',
    field: 'pointType',
    component: 'Input',
  },
  {
    label: '点位中文描述',
    field: 'pointName',
    component: 'Input',
  },
  {
    label: 'IP地址',
    field: 'ip',
    component: 'Input',
  },
  {
    label: 'DB地址',
    field: 'dbAddress',
    component: 'Input',
  },
  {
    label: '数据类型',
    field: 'dataType',
    component: 'Input',
  },
  {
    label: '汉化方式',
    field: 'cnWay',
    component: 'Input',
  },
  {
    label: '字典表汉化Code',
    field: 'dictCode',
    component: 'Input',
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