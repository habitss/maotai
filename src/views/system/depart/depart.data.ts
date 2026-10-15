import { FormSchema } from '/@/components/Form';
import { getAllRolesListNoByTenant } from "/@/views/system/user/user.api";
import { mdmDictQueryPageList } from "/@/views/system/depart/depart.api";
import { useI18n } from '/@/hooks/web/useI18n';
const { t } = useI18n();

// 部门基础表单
export function useBasicFormSchema() {
  const basicFormSchema: FormSchema[] = [
    {
      field: 'departName',
      label: t('language.name_of_organization'),
      component: 'Input',
      componentProps: {
        placeholder: t('language.Please_enter_an_organization_department_name'),
      },
      rules: [{ required: true, message: t('language.can_not_be_blank') }],
    },
    {
      field: 'departNameAbbr',
      label: t('language.Name_abbreviation'),
      component: 'Input',
      componentProps: {
        placeholder: t('language.inputText') + t('language.Name_abbreviation'),
      },
      rules: [{ required: true, pattern: /^[A-Z\d]{2,}$/, message: '名称缩写应为不低于两位的大写英文与数字的组合' }],
    },
    //上级部门
    {
      field: 'parentId',
      label: t('language.superior_department'),
      component: 'TreeSelect',
      componentProps: {
        treeData: [],
        placeholder: '无',
        dropdownStyle: { maxHeight: '200px', overflow: 'auto' },
      },
    },
    //机构编码
    {
      field: 'orgCode',
      label: t('language.Mechanism_code'),
      component: 'Input',
      componentProps: {
        placeholder: t('language.inputText') + t('language.Mechanism_code'),
      },
    },
    {
      field: 'orgCategory',
      label: t('language.Institutional_level'),
      component: 'RadioButtonGroup',
      componentProps: { options: [] },
    },
    //工段
    {
      field: 'memo',
      label: t('language.workSection'),
      component: 'ApiSelect',
      componentProps: {
        mode: 'multiple',
        api: mdmDictQueryPageList,
        params: {
          pageNo: 1,
          pageSize: 10000,
          groupCode: 'ORG_TYPE'
        },
        labelField: 'itemKey',
        valueField: 'itemVal',
        resultField: 'records'
      },
      show: ({ values }) => values.orgCategory == 6,
      // required: ({ values }) => values.orgCategory == 6,
    },
    {
      field: 'departOrder',
      label: t('language.sort'),
      component: 'InputNumber',
      componentProps: {},
    },
    // {
    //   field: 'mobile',
    //   label: '电话',
    //   component: 'Input',
    //   componentProps: {
    //     placeholder: '请输入电话',
    //   },
    // },
    // {
    //   field: 'fax',
    //   label: '传真',
    //   component: 'Input',
    //   componentProps: {
    //     placeholder: '请输入传真',
    //   },
    // },
    // {
    //   field: 'address',
    //   label: '地址',
    //   component: 'Input',
    //   componentProps: {
    //     placeholder: '请输入地址',
    //   },
    // },
    {
      field: 'memo',
      label: t('language.remark'),
      component: 'InputTextArea',
      componentProps: {
        placeholder: t('language.inputText') + t('language.remark'),
      },
    },
  ];
  return { basicFormSchema };
}

// 机构类型选项
export const orgCategoryOptions = {
  // 一级部门
  root: [{ value: '1', label: t('language.bloc') }],
  // 子级部门
  child: [
    { value: '2', label: t('language.region') },
    { value: '3', label: t('language.factory') },
    { value: '4', label: t('language.department') },
    { value: '5', label: t('language.area') },
    { value: '6', label: t('language.productLine') },
  ],
};
