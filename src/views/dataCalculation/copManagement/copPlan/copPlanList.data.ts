import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import dayjs from "dayjs";
import { mes3UserListAll } from '@/api/common/api';
import {ref} from "vue";
const { t } = useI18n();

// 用户下拉框数据
let USER_LIST = ref([]);
(async function f() {
  USER_LIST.value = await mes3UserListAll();
})();

//下拉选择过滤方法
function filterOption(input, option) {
  return option.realname.toLowerCase().indexOf(input.toLowerCase()) >= 0;
};

export const dataMaps = ref();

//列表表头配置
export const columns: BasicColumn[] = [
  {
    title: '序号',
    dataIndex: 'id',
    fixed:'left',
    defaultHidden: true,
  },
  {
    title: '排序',
    align: 'center',
    fixed:'left',
    dataIndex: 'sort',
    width: 40,
  },
  {
    title: '设备',
    align: 'center',
    fixed:'left',
    dataIndex: 'copDeviceIdVal',
    ellipsis:false,
    resizable: true,
    width: 60,
  },
  {
    title: '清洗点',
    align: 'center',
    fixed:'left',
    dataIndex: 'copPointName',
    ellipsis:false,
    resizable: true,
    width: 65,
  },
  {
    title: '频率',
    dataIndex: 'frequency',
    fixed:'left',
    width: 60,
  },
  {
    title: '配方',
    dataIndex: 'formula',
    ellipsis:false,
    resizable: true,
    width: 80,
  },
  {
    title: '温度',
    dataIndex: 'temperature',
    width: 50,
  },
  {
    title: '清洗人',
    dataIndex: 'clearBy',
    width: 60,
  },
  {
    title: '检查人',
    dataIndex: 'checkBy',
    width: 60,
  },
  // {
  //   title: '清洗人',
  //   dataIndex: 'clearBy',
  //   editRow: true,
  //   editComponent: 'ApiSelect',
  //   editComponentProps: {
  //     api: mes3UserListAll,
  //     resultField: 'records',
  //     labelField: 'realname',
  //     valueField: 'id',
  //   },
  //   editValueMap:(value) => processingEditValue(value, 'clearBy'),
  //   width: 100,
  // },
  // {
  //   title: '检查人',
  //   dataIndex: 'checkBy',
  //   editRow: true,
  //   editComponent: 'ApiSelect',
  //   editComponentProps: {
  //     api: mes3UserListAll,
  //     resultField: 'records',
  //     labelField: 'realname',
  //     valueField: 'id',
  //   },
  //   editValueMap:(value) => processingEditValue(value, 'checkBy'),
  //   width: 100,
  // },

];

//列表页面查询表单
export const searchFormSchema: FormSchema[] = [
  {
    label: t('language.department'),
    field: 'deptCode',
    component: 'Input',
    slot:'deptCode',
    colProps: { span: 6 },
    defaultValue: ''
  },
  {
    label: t('language.productLine'),
    field: 'lineCode',
    component: 'Input',
    slot:'lineCode',
    colProps: { span: 6 },
    defaultValue: ''
  },

  {
    label: '年',
    field: 'planYear',
    component: 'DatePicker',
    componentProps: {
      picker: 'year',
      format: 'YYYY',
      valueFormat: 'YYYY',
    },
    defaultValue: dayjs().format('YYYY'),
    colProps: { span: 6 },
  },
  {
    label: '月',
    field: 'months',
    component: 'Select',
    componentProps: {
      options: [
        {value: '1', label: '一月'},
        {value: '2', label: '二月'},
        {value: '3', label: '三月'},
        {value: '4', label: '四月'},
        {value: '5', label: '五月'},
        {value: '6', label: '六月'},
        {value: '7', label: '七月'},
        {value: '8', label: '八月'},
        {value: '9', label: '九月'},
        {value: '10', label: '十月'},
        {value: '11', label: '十一月'},
        {value: '12', label: '十二月'},
      ],
      //下拉多选
      mode: 'multiple',
    },
    defaultValue: (new Date().getMonth() + 1).toString(),
    colProps: { span: 6 },
  },
  {
    label: '设备名称',
    field: 'copDeviceName',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    label: '清洗点',
    field: 'copPointName',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    label: '频次',
    field: 'frequency',
    component: 'JDictSelectTag',
    componentProps: {
      //字典code配置，比如通过性别字典编码：sex，也可以使用demo,name,id 表名,名称,值的方式
      dictCode:'mes3_mdm_dict, item_key, item_val, del_flag = 0 and group_code = \'MES3_COP_CONFIG_FREQUENCY\'',
    },
    colProps: { span: 6 },
  },
  {
    label: '清洗人',
    field: 'clearBy',
    component: 'Select',
    colProps: { span: 6 },
    componentProps: {
      options: USER_LIST,
      fieldNames: { label: 'realname', value: 'id' },
      maxTagCount: 2,
      mode: 'multiple',
      showSearch: true,
      allowClear: true,
      filterOption,
    },
  },
];

export const addFormSchemas: FormSchema[] = [
  {
    label: '主键',
    field: 'id',
    component: 'Input',
    show: false,
    colProps: { span: 24 },
  },
  {
    label: '设备名称',
    field: 'copDeviceName',
    component: 'Input',
    colProps: { span: 24 },
    required: true,
  },
  {
    label: '清洗点名称',
    field: 'pointName',
    component: 'Input',
    colProps: { span: 24 },
    required: true,
  },
  {
    label: '温度',
    field: 'temperature',
    component: 'JDictSelectTag',
    componentProps: {
      //字典code配置，比如通过性别字典编码：sex，也可以使用demo,name,id 表名,名称,值的方式
      dictCode:'mes3_mdm_dict, item_key, item_val, del_flag = 0 and group_code = \'MES3_COP_CONFIG_TEMPERATURE\'',
    },

    colProps: { span: 24 },
    required: true,
  },
  {
    label: '时间',
    field: 'timeValue',
    component: 'Input',
    colProps: { span: 24 },
    required: true,
  },
  {
    label: '推荐配方',
    field: 'formula',
    component: 'JDictSelectTag',
    componentProps: {
      //字典code配置，比如通过性别字典编码：sex，也可以使用demo,name,id 表名,名称,值的方式
      dictCode:'mes3_mdm_dict, item_key, item_val, del_flag = 0 and group_code = \'MES3_COP_RECOMMENDED_FORMULA\'',
    },
    colProps: { span: 24 },
    required: true,
  },
  {
    label: '频次',
    field: 'frequency',
    component: 'JDictSelectTag',
    componentProps: {
      //字典code配置，比如通过性别字典编码：sex，也可以使用demo,name,id 表名,名称,值的方式
      dictCode:'mes3_mdm_dict, item_key, item_val, del_flag = 0 and group_code = \'MES3_COP_CONFIG_FREQUENCY\'',
    },
    colProps: { span: 24 },
    required: true,
  },
  {
    label: '顺序',
    field: 'sort',
    component: 'InputNumber',
    // labelWidth: 140,
    // labelLength: 8,
    // colProps: { span: 24 },
  },
  {
    label: '清洗人',
    field: 'clearBy',
    component: 'Select',
    colProps: { span: 24 },
    required: true,
    componentProps: {
      options: USER_LIST,
      fieldNames: { label: 'realname', value: 'id' },
      maxTagCount: 2,
      mode: 'multiple',
      showSearch: true,
      allowClear: true,
      filterOption,
    },
  },
  {
    label: '检查人',
    field: 'checkBy',
    component: 'Select',
    colProps: { span: 24 },
    required: true,
    componentProps: {
      options: USER_LIST,
      fieldNames: { label: 'realname', value: 'id' },
      maxTagCount: 2,
      mode: 'multiple',
      showSearch: true,
      allowClear: true,
      filterOption,
    },
  },
  {
    label: '清洗标准',
    field: 'cleaningStandard',
    component: 'InputTextArea',
    colProps: { span: 24 },
    required: true,
  },
  {
    label: '清洗步骤',
    field: 'cleaningStep',
    component: 'InputTextArea',
    colProps: { span: 24 },
    required: true,
  },
];

export const submitFormSchemas: FormSchema[] = [
  {
    label: '清洗标准',
    field: 'cleaningStandard',
    component: 'InputTextArea',
    colProps: { span: 24 },
  },
  {
    label: '清洗步骤',
    field: 'cleaningStep',
    component: 'InputTextArea',
    colProps: { span: 24 },
  },
  {
    label: '机台',
    field: 'copPointName',
    component: 'Input',
    colProps: { span: 24 },
  },
  {
    label: '清洗时间',
    field: 'clearDate',
    component: 'DatePicker',
    componentProps: {
      //日期格式化，页面上显示的值
      format:'YYYY-MM-DD',
      //返回值格式化（绑定值的格式）
      valueFormat:'YYYY-MM-DD',
      //是否显示今天按钮
      showToday:true,
    },
    colProps: { span: 24 },
    required: true,
  },
  {
    label: '清洗人',
    field: 'clearBy',
    component: 'Select',
    colProps: { span: 24 },
    componentProps: {
      options: USER_LIST,
      fieldNames: { label: 'realname', value: 'id' },
      maxTagCount: 2,
      mode: 'multiple',
      showSearch: true,
      allowClear: true,
      filterOption,
    },
  },
  {
    label: '检查人',
    field: 'checkBy',
    component: 'Select',
    colProps: { span: 24 },
    componentProps: {
      options: USER_LIST,
      fieldNames: { label: 'realname', value: 'id' },
      maxTagCount: 2,
      mode: 'multiple',
      showSearch: true,
      allowClear: true,
      filterOption,
    },
  },
  {
    label: '审核结果',
    field: 'checkResult',
    component: 'Select',
    componentProps: {
      options: [
        { value: '0', label: '通过' },
        { value: '1', label: '不通过' },
      ],
    },
  },
  {
    label: '备注',
    field: 'checkRemark',
    component: 'Input',
    colProps: { span: 24 },
  },
  {
    label: '修改计划结果',
    field: 'state',
    component: 'Select',
    componentProps: {
      options: [
        { value: '-1', label: '计划' },
        { value: '0', label: '不合格' },
        { value: '1', label: '已完成' },
        { value: '3', label: '延迟完成' },
        { value: '6', label: '因生产取消' },
      ],
    },
  },
];

// function processingEditValue(value, field) {
//   let str = value;
//   for (const item of dataMaps.value) {
//     if (item[field] == value) {
//       str = item[field + '_dictText'];
//       break;
//     }
//   }
//   return str;
// }



