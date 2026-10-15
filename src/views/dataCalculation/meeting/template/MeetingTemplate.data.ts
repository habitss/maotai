import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import dayjs from "dayjs";
import { mes3UserListAll } from '@/api/common/api';
import {ref} from "vue";

// 用户下拉框数据
let USER_LIST = ref([]);
(async function f() {
  USER_LIST.value = await mes3UserListAll();
})();

//下拉选择过滤方法
function filterOption(input, option) {
  return option.realname.toLowerCase().indexOf(input.toLowerCase()) >= 0;
};
const { t } = useI18n();

//列表表头配置
export function setBaseColumns(value): BasicColumn[] {
  return  [
    {
      title: value,
      width: 120,
      children: [
        {
          title: '排序',
          align: 'center',
          dataIndex: 'sort',
          width: 50,
        },
        {
          title: '会议日程',
          align: "center",
          dataIndex: 'contentTitle',
          ellipsis:false,
        },
        {
          title: '会议内容',
          align: "center",
          dataIndex: 'content',
          ellipsis:false,
          width: 450,
        },
        {
          title: '链接',
          align: "center",
          dataIndex: 'link',
          ellipsis:false,
          width: 100,
        },
        {
          title: '链接地址',
          align: "center",
          dataIndex: 'linkAddress',
          width: 100,
        },
        {
          title: '汇报人',
          align: "center",
          dataIndex: 'reporterVal',
          ellipsis:false,
          width: 100,
        },
        {
          title: '代理人',
          align: "center",
          dataIndex: 'agentVal',
          ellipsis:false,
          width: 100,
        },
        {
          title: '时间',
          align: "center",
          dataIndex: 'time',
          width: 60,
        },
        {
          title: '备注',
          align: "center",
          dataIndex: 'remark',
          ellipsis:false,
          width: 100,
        },
      ],
    }
  ];
}

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
    label: '会议类型',
    field: 'meetingType',
    component: 'JDictSelectTag',
    componentProps: {
      showChooseOption:false,
    },
    colProps: { span: 6 },
    defaultValue: ''
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
    label: '会议日程',
    field: 'contentTitle',
    component: 'InputTextArea',
    colProps: { span: 24 },
    required: true,
    //属性
    componentProps:{
      //可以点击清除图标删除内容
      allowClear: true,
      //自适应内容高度，可设置为 true | false 或对象：{ minRows: 2, maxRows: 6 }
      autoSize:{
        //最小显示行数
        minRows: 2,
        //最大显示行数
        maxRows: 3
      },
    }
  },
  {
    label: '会议内容',
    field: 'content',
    component: 'InputTextArea',
    colProps: { span: 24 },
    required: true,
    //属性
    componentProps:{
      //可以点击清除图标删除内容
      allowClear: true,
      //自适应内容高度，可设置为 true | false 或对象：{ minRows: 2, maxRows: 6 }
      autoSize:{
        //最小显示行数
        minRows: 2,
        //最大显示行数
        maxRows: 3
      },
    }
  },

  {
    label: '汇报人',
    field: 'reporter',
    component: 'Select',
    colProps: { span: 8 },
    componentProps: () => {
      return {
        options: USER_LIST.value,
        fieldNames: {label: 'realname', value: 'id'},
        maxTagCount: 2,
        showSearch: true,
        mode:'multiple',
        allowClear: true,
        filterOption,
      }
    },
  },
  {
    label: '代理人',
    field: 'agent',
    component: 'Select',
    colProps: { span: 8 },
    componentProps: () => {
      return {
        options: USER_LIST.value,
        fieldNames: {label: 'realname', value: 'id'},
        maxTagCount: 2,
        showSearch: true,
        mode:'multiple',
        allowClear: true,
        filterOption,
      }
    },
  },
  {
    label: '计划时长',
    field: 'time',
    component: 'Input',
    colProps: { span: 5 },
  },
  {
    label: '备注',
    field: 'remark',
    component: 'InputTextArea',
    colProps: { span: 24 },
    //属性
    componentProps:{
      //可以点击清除图标删除内容
      allowClear: true,
      //自适应内容高度，可设置为 true | false 或对象：{ minRows: 2, maxRows: 6 }
      autoSize:{
        //最小显示行数
        minRows: 2,
        //最大显示行数
        maxRows: 3
      },
    }
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
    label: '链接',
    field: 'link',
    component: 'Input',
    colProps: { span: 16 },
  },
  {
    field: '0',
    component: 'Input',
    // ifShow:false,
    label: ' ',
    colProps: {
      span: 8,
    },
    slot: 'addForm',
  },
  {
    label: '链接地址',
    field: 'linkAddress',
    component: 'InputTextArea',
    colProps: { span: 24 },
  },
];

//新增/修改表单字段
export const scheduleFormSchemas: FormSchema[] = [
  {
    label: '会议周期',
    field: 'type',
    component: 'Select',
    componentProps: {
      options: [
        {
          label: '日会',
          value: '1',
        },
        {
          label: '周会',
          value: '2',
        },
        {
          label: '月会',
          value: '3',
        },
      ],
    },
    colProps: { span: 13 },
    required: true,
  },
  {
    label: '星期选择',
    field: 'dayWeek',
    component: 'Select',
    componentProps: {
      mode:'multiple',
      options: [
        {
          label: '星期一',
          value: 'MONDAY',
        },
        {
          label: '星期二',
          value: 'TUESDAY',
        },
        {
          label: '星期三',
          value: 'WEDNESDAY',
        },
        {
          label: '星期四',
          value: 'THURSDAY',
        },
        {
          label: '星期五',
          value: 'FRIDAY',
        },
        {
          label: '星期六',
          value: 'SATURDAY',
        },
        {
          label: '星期天',
          value: 'SUNDAY',
        },
      ],
    },
    ifShow: ({ values }) => {
      return values.type == '1' ;
    },
    required: true,
    colProps: { span: 13 },
  },
  {
    label: '周选择',
    field: 'week',
    // component: 'WeekPicker',
    // componentProps: {
    //     valueFormat: 'YYYY-wo',
    // },
    component: 'Select',
    componentProps: {
      options: [
        {
          label: '第一周',
          value: 1,
        },
        {
          label: '第二周',
          value: 2,
        },
        {
          label: '第三周',
          value: 3,
        },
        {
          label: '第四周',
          value: 4,
        },
        {
          label: '第五周',
          value: 5,
        },
        {
          label: '第六周',
          value: 6,
        },
      ],
    },
    ifShow: ({ values }) => {
      return values.type == '3';
    },
    required: true,
    colProps: { span: 13 },
  },
  {
    label: '星期选择',
    field: 'day',
    component: 'Select',
    componentProps: {
      options: [
        {
          label: '星期一',
          value: 'MONDAY',
        },
        {
          label: '星期二',
          value: 'TUESDAY',
        },
        {
          label: '星期三',
          value: 'WEDNESDAY',
        },
        {
          label: '星期四',
          value: 'THURSDAY',
        },
        {
          label: '星期五',
          value: 'FRIDAY',
        },
        {
          label: '星期六',
          value: 'SATURDAY',
        },
        {
          label: '星期天',
          value: 'SUNDAY',
        },
      ],
    },
    ifShow: ({ values }) => {
      return values.type == '2' || values.type == '3';
    },
    colProps: { span: 13 },
  },
  {
    label: '会议时间',
    field: 'time',
    component: 'TimePicker',
    componentProps: {
      size:'default',
      showNow: true,
      format: 'HH:mm',
      valueFormat: 'HH:mm',
    },
    defaultValue: dayjs().format('HH:mm'),
    colProps: { span: 13 },
    required: true,
  },
];
