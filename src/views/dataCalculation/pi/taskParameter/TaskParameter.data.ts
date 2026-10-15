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
    title: '任务名称',
    align:"center",
    dataIndex: 'name'
   },
   {
    title: '定时任务',
    align:"center",
    dataIndex: 'taskName'
   },
   {
    title: '报表名称',
    align:"center",
    dataIndex: 'tableIdVal'
   },
   {
    title: '间隔(秒)',
    align:"center",
    dataIndex: 'difference'
   },
   {
    title: '排序',
    align:"center",
    dataIndex: 'sort'
   },
   {
    title: '备注',
    align:"center",
    dataIndex: 'remark'
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