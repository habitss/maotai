import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { useI18n } from '/@/hooks/web/useI18n';
//列表数据
export const columns: BasicColumn[] = [
  //  {
  //   title: '部门',
  //   align:"center",
  //   dataIndex: 'deptCodeVal'
  //  },
  //  {
  //   title: '产线',
  //   align:"center",
  //   dataIndex: 'lineCodeVal'
  //  },
  {
    title: '报表名称',
    align:"center",
    dataIndex: 'reportName'
  },
  {
    title: '绑定表',
    align:"center",
    dataIndex: 'tableId_dictText'
  },
  {
    title: '表名',
    align:"center",
    dataIndex: 'tableName'
  }
];
//查询数据
export const searchFormSchema: FormSchema[] = [
];
//表单数据
export const formSchema: FormSchema[] = [
];



/**
* 流程表单调用这个方法获取formSchema
* @param param
*/
export function getBpmFormSchema(_formData): FormSchema[]{
  // 默认和原始表单保持一致 如果流程中配置了权限数据，这里需要单独处理formSchema
  return formSchema;
}



//数据筛选条件
export const filterCriteriaData: FormSchema[] = [
  {
    label: '主键',
    field: 'id',
    component: 'Input',
    show: false,
    colProps: { span: 24 },
  },
  {
    label: '报表名称',
    field: 'reportName',
    componentProps:{disabled: true},
    component: 'Input',
    colProps: { span: 24 },
    required: true,
  }
]
