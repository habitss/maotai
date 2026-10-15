import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import { ref } from 'vue';
const { t } = useI18n();
import { mes3UserListAll } from '@/api/common/api';
import {getPopupContainer} from "@/utils";

// 用户下拉框数据
let USER_LIST = ref([]);
(async function f() {
  USER_LIST.value = await mes3UserListAll();
})();

//下拉选择过滤方法
function filterOption(input, option) {
  return option.realname.toLowerCase().indexOf(input.toLowerCase()) >= 0;
};
//列表表头配置
export const columns: BasicColumn[] = [
  {
    title: 'id',
    dataIndex: 'id',
    defaultHidden: true,
  },
  {
    title: '仪器',
    align: 'center',
    dataIndex: 'apparatusName',
  },
  {
    //部门
    title: t('language.department'),
    dataIndex: 'deptCode_dictText',
    align: 'center',
    resizable: true,
  },
  {
    //产线
    title: t('language.productLine'),
    dataIndex: 'lineCode_dictText',
    align: 'center',
    resizable: true,
  },
  {
    title: '仪器型号',
    align:"center",
    dataIndex: 'apparatusType',
    resizable: true,
  },
  {
    title: '允许误差',
    align:"center",
    dataIndex: 'permissibleError',
    resizable: true,
  },
  {
    title: '检查频次',
    align:"center",
    dataIndex: 'frequencyOfCheckVal',
    resizable: true,
  },
  {
    title: '核查方式',
    align:"center",
    dataIndex: 'verificationMethod',
    resizable: true,
  },
  {
    title: '说明',
    align:"center",
    dataIndex: 'explainType',
    resizable: true,
  },
  {
    title: '是否可用',
    align:"center",
    dataIndex: 'active',
    customRender:function ({ record }) {
      return String(record.active) === '1' ? "可用" : "不可用";
    }
  },
  {
    title: '顺序',
    align:"center",
    width: 100,
    dataIndex: 'sort',
  },

];

//列表页面查询表单
export const searchFormSchema: FormSchema[] = [
  {
    label: t('language.department'),
    field: 'deptCode',
    component: 'Input',
    slot:'deptCode',
    colProps: { span: 6 },
  },
  {
    label: t('language.productLine'),
    field: 'lineCode',
    component: 'Input',
    slot:'lineCode',
    colProps: { span: 7 },
  },
  {
    label: '仪器名称',
    field: 'apparatusName',
    component: 'Input',
    colProps: {
      span: 7,
    },
  },
  {
    label: '频次',
    field: 'frequencyOfCheck',
    component: 'JDictSelectTag',
    componentProps: {
      //字典code配置，比如通过性别字典编码：sex，也可以使用demo,name,id 表名,名称,值的方式
      dictCode:'mes3_mdm_dict, item_key, item_val, del_flag = 0 and group_code = \'MES3_COP_CONFIG_FREQUENCY\'',
    },
    colProps: { span: 6 },
  },
];

//新增/修改表单字段
export const addFormSchemas: FormSchema[] = [
    {
      label: '主键',
      field: 'id',
      component: 'Input',
      show: false,
    },
    {
      //部门
      label: t('language.department'),
      field: 'deptCode',
      component: 'Input',
      slot:'deptCode',
      required: true,
      colProps: { span: 11 },
    },
    {
      //产线
      label: t('language.productLine'),
      field: 'lineCode',
      component: 'Input',
      slot:'lineCode',
      required: true,
      colProps: { span: 11 },
    },
    {
      label: '仪器名称',
      field: 'apparatusName',
      component: 'Input',
      required: true,
      colProps: { span: 8 },
    },
    {
      label: '仪器型号',
      field: 'apparatusType',
      component: 'Input',
      colProps: { span: 8 },
    },
    {
      label: '允许误差',
      field: 'permissibleError',
      component: 'Input',
      colProps: { span: 6 },
    },
    {
      label: '检查频次',
      field: 'frequencyOfCheck',
      component: 'JDictSelectTag',
      componentProps: {
        //字典code配置，比如通过性别字典编码：sex，也可以使用demo,name,id 表名,名称,值的方式
        dictCode:'mes3_mdm_dict, item_key, item_val, del_flag = 0 and group_code = \'MES3_COP_CONFIG_FREQUENCY\'',
      },
      colProps: { span: 7 },
      required: true,
    },
  {
    label: '检查人',
    field: 'checkUser',
    colProps: { span: 7 },
    component: 'Select',
    componentProps: {
      options: USER_LIST,
      fieldNames: { label: 'realname', value: 'id' },
      // maxTagCount: 2,
      // mode: 'multiple',
      getPopupContainer:() => getPopupContainer(),
      placement: 'bottomRight',
      showSearch: true,
      allowClear: true,
      filterOption,
    },
  },
  {
    label: '核查人',
    field: 'verifyUser',
    colProps: { span: 7 },
    component: 'Select',
    componentProps: {
      options: USER_LIST,
      fieldNames: { label: 'realname', value: 'id' },
      // maxTagCount: 2,
      //  mode: 'multiple',
      getPopupContainer:() => getPopupContainer(),
      placement: 'bottomRight',
      showSearch: true,
      allowClear: true,
      filterOption,
    },
  },
    {
      label: '核查方式',
      field: 'verificationMethod',
      component: 'InputTextArea',
      colProps: { span: 24 },
    },
    {
      label: '说明',
      field: 'explainType',
      component: 'InputTextArea',
      colProps: { span: 24 },
    },
  {
    label: '是否激活',
    field: 'active',
    component: 'Switch',
    colProps: { span: 8 },
    componentProps: {
      //非选中时的内容
      unCheckedChildren:'否',
      //非选中时的值
      unCheckedValue:0,
      //选中时的内容
      checkedChildren:'是',
      //选中时的值
      checkedValue:1,
    },
    defaultValue: 1,
  },
    {
      label: '顺序',
      field: 'sort',
      colProps: { span: 8 },
      component: 'InputNumber',
    },
  ];

export const columnsConfig: BasicColumn[] = [
  {
    title: 'id',
    dataIndex: 'id',
    defaultHidden: true,
  },
  {
    title: '仪器名称',
    align: 'center',
    dataIndex: 'apparatusIdVal',
    width:200,
  },
  {
    title: '字段名称',
    align: 'center',
    dataIndex: 'fieldName',
    width:200,
  },
  {
    title: '字段类型',
    align: 'center',
    dataIndex: 'fieldTypeVal',
    width:200,
  },
  {
    title: '顺序',
    align:"center",
    width: 100,
    dataIndex: 'sort',
  },
];

//列表页面查询表单
export const searchConfigFormSchema: FormSchema[] = [
  {
    label: '仪器名称',
    field: 'apparatusName',
    component: 'Input',
    colProps: {
      span: 6,
    },
  }
];

export const addConfigFormSchemas: FormSchema[] = [
  {
    label: '主键',
    field: 'id',
    component: 'Input',
    show: false,
    colProps: { span: 24 },
  },
  {
    label: '仪器名称',
    field: 'apparatusName',
    component: 'Input',
    colProps: { span: 24 },
  },
  {
    label: '字段名称',
    field: 'fieldName',
    component: 'Input',
    colProps: { span: 11 },
    required: true,
  },
  {
    label: '字段类型',
    field: 'fieldType',
    component: 'Select',
    componentProps: {
      options: [
        {
          label: '时间',
          value: '1',
        },
        {
          label: '日期',
          value: '2',
        },
        {
          label: '输入框',
          value: '3',
        },
        {
          label: '图片上传',
          value: '4',
        },
        {
          label: '视频/文件上传',
          value: '5',
        },
      ],
    },
    colProps: { span: 10 },
    required: true,
  },
  {
    label: '顺序',
    field: 'sort',
    component: 'InputNumber',
  },
];

