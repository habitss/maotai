import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { rules } from '/@/utils/helper/validator';
import { useI18n } from '/@/hooks/web/useI18n';
const { t } = useI18n();
export const columns: BasicColumn[] = [
  {
    title: '部门',
    dataIndex: 'deptCodeVal',
    sorter: true,
    width: 120,
  },
  {
    title: '产线',
    dataIndex: 'lineCodeVal',
    sorter: true,
    width: 120,
  },
  {
    title: '品牌名称',
    dataIndex: 'name',
    sorter: true,
    width: 120,
  },
  {
    title: '名牌简称',
    dataIndex: 'abbreviation',
    sorter: true,
    width: 120,
  },
  {
    title: '品牌编码',
    dataIndex: 'code',
    sorter: true,
    width: 120,
  },
  {
    title: '描述',
    dataIndex: 'notes',
    sorter: true,
    width: 120,
  },
  {
    title: '排序',
    dataIndex: 'sort',
    sorter: true,
    width: 120,
  },
];

export const recycleBincolumns: BasicColumn[] = [
  {
    title: '部门',
    dataIndex: 'deptCode',
    width: 120,
  },
  {
    title: '产线',
    dataIndex: 'lineCode',
    width: 120,
  },
  {
    title: '品牌名称',
    dataIndex: 'name',
    width: 120,
  },
  {
    title: '名牌简称',
    dataIndex: 'abbreviation',
    width: 120,
  },
  {
    title: '品牌编码',
    dataIndex: 'code',
    width: 120,
  },
  {
    title: '描述',
    dataIndex: 'notes',
    width: 120,
  },
  {
    title: '排序',
    dataIndex: 'sort',
    width: 120,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    label: "部门",
    field: 'abbreviation',
    component: 'Input',
    slot:'dept',
  },
  {
    label: "产线",
    field: 'abbreviation',
    component: 'Input',
    slot:'line',
  },
  {
    label: "品牌名称",
    field: 'name',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    label: "品牌简称",
    field: 'abbreviation',
    component: 'Input',
    colProps: { span: 6 },
  }
];

export const formSchema: FormSchema[] = [
  {
    label: '',
    field: 'id',
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
            if (new RegExp("[`~!@#$^&*()=|{}'.<>《》/?！￥（）—【】‘；：”“。，、？\u4e00-\u9fa5]").test(value)) {
              return Promise.reject('数据值不能包含特殊字符和中文！');
            }
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
