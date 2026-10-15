import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { useI18n } from '/@/hooks/web/useI18n';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '字段1',
    align:"center",
    dataIndex: 'reportName'
  },
  {
    title: '字段2',
    align:"center",
    dataIndex: 'tableId_dictText'
  },
  {
    title: '字段3',
    align:"center",
    dataIndex: 'tableName1'
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