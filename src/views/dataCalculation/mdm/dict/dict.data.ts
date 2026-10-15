import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { rules } from '/@/utils/helper/validator';
import { useI18n } from '/@/hooks/web/useI18n';
import { categorizeList } from './dict.api';
const { t } = useI18n();
export const columns: BasicColumn[] = [
  {
    title: t('language.DictionaryName'),
    dataIndex: 'dictGroupName',
    width: 240,
  },
  {
    title: t('language.DictionaryCode'),
    dataIndex: 'dictGroupCode',
    width: 240,
  },
  {
    title: t('language.describe'),
    dataIndex: 'remark',
    // width: 120
  },
];

export const recycleBincolumns: BasicColumn[] = [
  {
    title: '字典名称',
    dataIndex: 'dictName',
    width: 120,
  },
  {
    title: '字典编码',
    dataIndex: 'dictCode',
    width: 120,
  },
  {
    title: '描述',
    dataIndex: 'description',
    width: 120,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    label: t('language.DictionaryName'),
    field: 'dictGroupName',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    label: t('language.DictionaryCode'),
    field: 'dictGroupCode',
    component: 'Input',
    colProps: { span: 6 },
  },
];

export const formSchema: FormSchema[] = [
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    label: '',
    field: 'categorizeId',
    component: 'Input',
    show: false,
  },
  {
    label: t('language.DictionaryName'),
    field: 'dictGroupName',
    required: true,
    component: 'Input',
  },
  {
    label: t('language.DictionaryCode'),
    field: 'dictGroupCode',
    component: 'Input',
    dynamicDisabled: ({ values }) => {
      return !!values.id;
    },
    dynamicRules: ({ model, schema }) => {
      const arr = rules.duplicateCheckRule('sys_dict', 'dict_code', model, schema, true);
      arr[0].required = true;
      return arr;
    },
  },
  {
    label: t('language.describe'),
    field: 'remark',
    component: 'Input',
  },
];

export const dictItemColumns: BasicColumn[] = [
  {
    title: t('language.name'),
    dataIndex: 'itemKey',
    width: 80,
  },
  {
    title: t('language.dataValue'),
    dataIndex: 'itemVal',
    width: 80,
  },
  {
    title: t('language.sort'),
    dataIndex: 'sort',
    width: 80,
  },
];

export const dictItemSearchFormSchema: FormSchema[] = [
  {
    label: t('language.name'),
    field: 'itemKey',
    component: 'Input',
  },
  {
    label: t('language.fileStatue'),
    field: 'status',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'dict_item_status',
      stringToNumber: true,
    },
  },
];

export const itemFormSchema: FormSchema[] = [
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    label: t('language.name'),
    field: 'itemKey',
    required: true,
    component: 'Input',
  },
  {
    label: t('language.dataValue'),
    field: 'itemVal',
    component: 'Input',
    dynamicDisabled: ({ values }) => {
      console.log(values);
      return !!values.id;
    },
    dynamicRules: ({ values, model }) => {
      return [
        {
          required: true,
          validator: (_, value) => {
            if (!value) {
              return Promise.reject('请输入数据值');
            }
            // if (new RegExp("[`~!@#$^&*()=|{}'.<>《》/?！￥（）—【】‘；：”“。，、？\u4e00-\u9fa5]").test(value)) {
            //   return Promise.reject('数据值不能包含特殊字符和中文！');
            // }
            return Promise.resolve();
            // return new Promise<void>((resolve, reject) => {
            //   let params = {
            //     dictId: values.dictId,
            //     id: model.id,
            //     itemVal: value,
            //   };
            //   dictItemCheck(params)
            //     .then((res) => {
            //       res.success ? resolve() : reject(res.message || '校验失败');
            //     })
            //     .catch((err) => {
            //       reject(err.message || '验证失败');
            //     });
            // });
          },
        },
      ];
    },
  },
  {
    label: t('language.describe'),
    field: 'remark',
    component: 'Input',
  },
  {
    field: 'sort',
    label: t('language.sort'),
    component: 'InputNumber',
    defaultValue: 1,
  },
  // {
  //   field: 'status',
  //   label: '是否启用',
  //   defaultValue: 1,
  //   component: 'JDictSelectTag',
  //   componentProps: {
  //     type: 'radioButton',
  //     dictCode: 'dict_item_status',
  //     stringToNumber: true,
  //   },
  // },
];

//字典分类form配置
export const categorizeFormSchema: FormSchema[] = [
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    label: '分类名称',
    field: 'categorizeName',
    required: true,
    component: 'Input',
  },
  {
    label: '父类',
    field: 'parentId',
    component: 'ApiSelect',
    componentProps: {
      api: categorizeList,
      params: {
        parentId: 0
      },
      labelField: 'categorizeName',
      valueField: 'id',
      getPopupContainer: () => document.body,
    },
  },
];