import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import dayjs from "dayjs";
import { mes3UserListAll } from '@/api/common/api';
import {ref} from "vue";
const { t } = useI18n();

// 用户下拉框数据
export let USER_LIST = ref([]);
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
    title: '仪器名称',
    align: 'center',
    fixed:'left',
    dataIndex: 'apparatusName',
    ellipsis:false,
    width: 100,
  },
  {
    title: '仪器型号',
    align: 'center',
    fixed:'left',
    dataIndex: 'apparatusType',
    width: 100,
  },
  {
    title: '检查频次',
    dataIndex: 'frequency',
    fixed:'left',
    width: 70,
  },
  // {
  //   title: '允许误差',
  //   fixed:'left',
  //   dataIndex: 'permissibleError',
  //   width: 50,
  // },
  {
    title: '核查人',
    fixed:'left',
    dataIndex: 'checkUser',
    width: 70,
  },
  {
    title: '确认人',
    fixed:'left',
    dataIndex: 'verifyUser',
    width: 70,
  },
  // {
  //   title: '核查人',
  //   dataIndex: 'checkUser',
  //   editRow: true,
  //   editComponent: 'ApiSelect',
  //   editComponentProps: {
  //     api: mes3UserListAll,
  //     resultField: 'records',
  //     labelField: 'realname',
  //     valueField: 'id',
  //   },
  //   editValueMap:(value) => processingEditValue(value, 'checkUser'),
  //   width: 100,
  // },
  // {
  //   title: '确认人',
  //   dataIndex: 'verifyUser',
  //   editRow: true,
  //   editComponent: 'ApiSelect',
  //   editComponentProps: {
  //     api: mes3UserListAll,
  //     resultField: 'records',
  //     labelField: 'realname',
  //     valueField: 'id',
  //   },
  //   editValueMap:(value) => processingEditValue(value, 'verifyUser'),
  //   width: 100,
  // },

];

export const viewColumns: BasicColumn[] = [
  {
    title: '序号',
    dataIndex: 'id',
    fixed:'left',
    defaultHidden: true,
  },
  {
    title: '核查人',
    fixed:'left',
    dataIndex: 'checkUser',
    width: 80,
  },
  {
    title: '核查日期',
    fixed:'left',
    dataIndex: 'checkDate',
    width: 150,
  },
  {
    title: '核查结果',
    fixed:'left',
    dataIndex: 'checkResult',
    width: 80,
    customRender:function ({ record }) {
      return String(record.checkResult) === '1' ? "OK" : "NOK";
    }
  },
  {
    title: '确认人',
    fixed:'left',
    dataIndex: 'verifyUser',
    width: 80,
  },
  {
    title: '确认结果',
    fixed:'left',
    dataIndex: 'verifyResult',
    width: 80,
    customRender:function ({ record }) {
      return String(record.verifyResult) === '1' ? "OK" : "NOK";
    }
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
    label: '仪器名称',
    field: 'apparatusName',
    component: 'Input',
    colProps: { span: 6 },
  },

  {
    label: '检查频次',
    field: 'frequency',
    component: 'JDictSelectTag',
    componentProps: {
      //字典code配置，比如通过性别字典编码：sex，也可以使用demo,name,id 表名,名称,值的方式
      dictCode:'mes3_mdm_dict, item_key, item_val, del_flag = 0 and group_code = \'MES3_COP_CONFIG_FREQUENCY\'',
    },
    colProps: { span: 6 },
  },
  {
    label: '核查人',
    field: 'checkUser',
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

export const submitFormSchemas: FormSchema[] = [
  {
    label: '清洗标准',
    field: 'cleaningStandard',
    component: 'InputTextArea',
    colProps: { span: 24 },
    componentProps: {
      autoSize: {
        //最小显示行数
        minRows: 2,
        //最大显示行数
        maxRows: 5
      },
    },
  },
  {
    label: '清洗步骤',
    field: 'cleaningStep',
    component: 'InputTextArea',
    colProps: { span: 24 },
    componentProps: {
      autoSize: {
        //最小显示行数
        minRows: 2,
        //最大显示行数
        maxRows: 5
      },
    },
  },
  {
    label: '机台',
    field: 'copPointName',
    component: 'Input',
    colProps: { span: 8 },
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
    colProps: { span: 8 },
    required: true,
  },
  {
    label: '清洗人',
    field: 'clearBy',
    component: 'Select',
    colProps: { span: 8 },
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
    colProps: { span: 12 },
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
    colProps: { span: 12 },
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
    colProps: { span: 12 },
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



