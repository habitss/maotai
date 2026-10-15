import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { mes3UserListAll } from '@/api/common/api';
import {getPopupContainer} from "@/utils";
import {ref} from "vue";
import {getColor} from "@/utils/common/piColorUtils";
//下拉选择过滤方法
function filterOption(input, option) {
  return option.realname.toLowerCase().indexOf(input.toLowerCase()) >= 0;
};
// 用户下拉框数据
let USER_LIST = ref([]);
(async function f() {
  USER_LIST.value = await mes3UserListAll();
})();
//列表数据
export const columns: BasicColumn[] = [
  {
    title: 'id',
    align: "center",
    dataIndex: 'id',
    defaultHidden: true,
  },
  {
    title: '提供方部门',
    align: "center",
    width: 100,
    dataIndex: 'provideDeptVal'
  },
  {
    title: '产品/服务',
    align: "center",
    width: 100,
    ellipsis:false,
    dataIndex: 'product'
  },
  {
    title: '活动',
    align: "center",
    width: 120,
    ellipsis:false,
    dataIndex: 'activity'
  },
  {
    title: '接收方部门',
    align: "center",
    width: 120,
    dataIndex: 'receiveDeptVal'
  },
  {
    title: '服务度量指标',
    align: "center",
    ellipsis:false,
    dataIndex: 'serviceIndicator'
  },
  {
    title: '单位',
    align: "center",
    width: 60,
    dataIndex: 'unit'
  },
  {
    title: '目标值',
    align: "center",
    width: 50,
    dataIndex: 'targetValue'
  },
  {
    title: '提供人',
    align: "center",
    width: 60,
    dataIndex: 'provideByVal'
  },
  {
    title: '接收人',
    align: "center",
    width: 60,
    dataIndex: 'receiveByVal'
  },
  {
    title: '提供方责任人',
    align: "center",
    width: 60,
    dataIndex: 'provideDutyByVal'
  },
  {
    title: '启用日期',
    align: "center",
    width: 80,
    dataIndex: 'startDate'
  },
  {
    title: '停用日期',
    align: "center",
    width: 80,
    dataIndex: 'endDate'
  },
  {
    title: '确认状态',
    align: "center",
    width: 60,
    dataIndex: 'confirmStatus',
    customRender:function ({ record }) {
      return record.confirmStatus === 1 ? "已确认" : "未确认";
    },
  },
  {
    title: '使用状态',
    align: "center",
    width: 60,
    dataIndex: 'useStatus',
    customRender:function ({ record }) {
      return record.useStatus === 1 ? "已使用" : "未使用";
    },
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    label: '服务提供方',
    field: 'provideDept',
    component: 'Input',
    slot:'provideDept',
    colProps: { span: 6 },
  },
  {
    label: '产品/服务',
    field: 'product',
    component: 'Input',
    colProps: {
      span: 6,
    },
  },
  {
    label: '活动',
    field: 'activity',
    component: 'Input',
    colProps: {
      span: 6,
    },
  },
  {
    label: '接收方部门',
    field: 'receiveDept',
    component: 'Input',
    slot:'receiveDept',
    colProps: { span: 6 },
  },
  {
    label: '数据提供人',
    field: 'provideBy',
    colProps: {span: 6},
    component: 'Select',
    componentProps: {
      options: USER_LIST,
      fieldNames: {label: 'realname', value: 'id'},
      // maxTagCount: 2,
      // mode: 'multiple',
      getPopupContainer: () => getPopupContainer(),
      placement: 'bottomRight',
      showSearch: true,
      allowClear: true,
      filterOption,
    },
  },
  {
    label: '数据接收人',
    field: 'receiveBy',
    colProps: {span: 6},
    component: 'Select',
    componentProps: {
      options: USER_LIST,
      fieldNames: {label: 'realname', value: 'id'},
      // maxTagCount: 2,
      // mode: 'multiple',
      getPopupContainer: () => getPopupContainer(),
      placement: 'bottomRight',
      showSearch: true,
      allowClear: true,
      filterOption,
    },
  },
  {
    label: '确认状态',
    field: 'confirmStatus',
    component: 'Select',
    componentProps: {
      options: [
        {
          label: '已确认',
          value: '1',
        },
        {
          label: '未确认',
          value: '0',
        },
      ],
    },
    colProps: { span: 6 },
  },
  {
    label: '使用状态',
    field: 'useStatus',
    component: 'Select',
    componentProps: {
      options: [
        {
          label: '已使用',
          value: '1',
        },
        {
          label: '未使用',
          value: '0',
        },
      ],
    },
    colProps: { span: 6 },
  },
  ];

export const addFormSchemas: FormSchema[] = [
  {
    label: '主键',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    label: '提供方部门',
    field: 'provideDept',
    component: 'Input',
    slot:'provideDept',
    colProps: { span: 20 },
    required: true,
  },
  {
    label: '产品/服务',
    field: 'product',
    component: 'Input',
    colProps: {
      span: 20,
    },
    required: true,
  },
  {
    label: '活动',
    field: 'activity',
    component: 'Input',
    colProps: {
      span: 20,
    },
    required: true,
  },
  {
    label: '接收方部门',
    field: 'receiveDept',
    component: 'Input',
    slot:'receiveDept',
    colProps: { span: 20 },
    required: true,
  },
  {
    label: '服务度量指标',
    field: 'serviceIndicator',
    component: 'InputTextArea',
    colProps: {
      span: 20,
    },
  },
  {
    label: '单位',
    field: 'unit',
    component: 'Input',
    colProps: {
      span: 20,
    },
  },
  {
    label: '目标值',
    field: 'targetValue',
    component: 'InputNumber',
    colProps: {
      span: 10,
    },
  },
  {
    label: '数据提供人',
    field: 'provideBy',
    colProps: {span: 15},
    component: 'Select',
    componentProps: {
      options: USER_LIST,
      fieldNames: {label: 'realname', value: 'id'},
      // maxTagCount: 2,
      // mode: 'multiple',
      getPopupContainer: () => getPopupContainer(),
      placement: 'bottomRight',
      showSearch: true,
      allowClear: true,
      filterOption,
    },
    required: true,
  },
  {
    label: '数据接收人',
    field: 'receiveBy',
    colProps: {span: 15},
    component: 'Select',
    componentProps: {
      options: USER_LIST,
      fieldNames: {label: 'realname', value: 'id'},
      // maxTagCount: 2,
      // mode: 'multiple',
      getPopupContainer: () => getPopupContainer(),
      placement: 'bottomRight',
      showSearch: true,
      allowClear: true,
      filterOption,
    },
    required: true,
  },
  {
    label: '提供方责任人',
    field: 'provideDutyBy',
    colProps: {span: 15},
    component: 'Select',
    componentProps: {
      options: USER_LIST,
      fieldNames: {label: 'realname', value: 'id'},
      // maxTagCount: 2,
      // mode: 'multiple',
      getPopupContainer: () => getPopupContainer(),
      placement: 'bottomRight',
      showSearch: true,
      allowClear: true,
      filterOption,
    },
  },
  {
    label: '启用日期',
    field: 'startDate',
    component: 'DatePicker',
    componentProps: {
      //日期格式化，页面上显示的值
      format: 'YYYY-MM-DD',
      //返回值格式化（绑定值的格式）
      valueFormat: 'YYYY-MM-DD',
      //是否显示今天按钮
      showToday: true,
    },
    colProps: { span: 15 },
    required: true,
  },
  {
    label: '停用日期',
    field: 'endDate',
    component: 'DatePicker',
    componentProps: {
      //日期格式化，页面上显示的值
      format: 'YYYY-MM-DD',
      //返回值格式化（绑定值的格式）
      valueFormat: 'YYYY-MM-DD',
      //是否显示今天按钮
      showToday: true,
    },
    colProps: { span: 15 },
  },
  {
    label: '确认状态',
    field: 'confirmStatus',
    component: 'Switch',
    colProps: { span: 10 },
    componentProps: {
      //非选中时的内容
      unCheckedChildren:'未确认',
      //非选中时的值
      unCheckedValue:0,
      //选中时的内容
      checkedChildren:'已确认',
      //选中时的值
      checkedValue:1,
    },
    defaultValue: 0,
  },
  {
    label: '使用状态',
    field: 'useStatus',
    component: 'Switch',
    colProps: { span: 10 },
    componentProps: {
      //非选中时的内容
      unCheckedChildren:'未使用',
      //非选中时的值
      unCheckedValue:0,
      //选中时的内容
      checkedChildren:'已使用',
      //选中时的值
      checkedValue:1,
    },
    defaultValue: 0,
  },
  {
    label: '备注',
    field: 'remark',
    component: 'InputTextArea',
    colProps: { span: 20 },
  },
];

export const totalColumns: BasicColumn[] = [
  {
    title: 'id',
    align: "center",
    dataIndex: 'id',
    defaultHidden: true,
  },
  {
    title: '提供方部门',
    align: "center",
    dataIndex: 'provideDept_text',
    width:80,
    customCell: (record, index, column) => {
      return {
        rowSpan: (index+1) % 2 === 0 ? 0 : 2,
      };
    }
  },
  {
    title: '产品/服务',
    align: "center",
    ellipsis:false,
    width:100,
    dataIndex: 'product',
    customCell: (record, index, column) => {
      return {
        rowSpan: (index+1) % 2 === 0 ? 0 : 2,
      };
    }
  },
  {
    title: '活动',
    align: "center",
    ellipsis:false,
    width:100,
    dataIndex: 'activity',
    customCell: (record, index, column) => {
      return {
        rowSpan: (index+1) % 2 === 0 ? 0 : 2,
      };
    }
  },
  {
    title: '接收方部门',
    align: "center",
    width:80,
    dataIndex: 'receiveDept_text',
    customCell: (record, index, column) => {
      return {
        rowSpan: (index+1) % 2 === 0 ? 0 : 2,
      };
    }
  },
  {
    title: '服务度量指标',
    align: "center",
    ellipsis:false,
    width:200,
    dataIndex: 'serviceIndicator',
    customCell: (record, index, column) => {
      return {
        rowSpan: (index+1) % 2 === 0 ? 0 : 2,
      };
    }
  },
  {
    title: '单位',
    align: "center",
    dataIndex: 'unit',
    width:60,
    customCell: (record, index, column) => {
      return {
        rowSpan: (index+1) % 2 === 0 ? 0 : 2,
      };
    }
  },
  {
    title: '目标/实际',
    align: "center",
    width:60,
    dataIndex: 'title'
  },
]


export const searchFormProvidSchema: FormSchema[] = [
  {
    label: '年份',
    field: 'year',
    component: 'DatePicker',
    componentProps: {
      format: 'YYYY',
      valueFormat: 'YYYY',
      picker: 'year',
    },
    colProps: { span: 6 },
  },
  {
    label: '服务提供方',
    field: 'provideDept',
    component: 'Input',
    slot:'provideDept',
    colProps: { span: 6 },
  },
  {
    label: '产品/服务',
    field: 'product',
    component: 'Input',
    colProps: {
      span: 6,
    },
  },
  {
    label: '活动',
    field: 'activity',
    component: 'Input',
    colProps: {
      span: 6,
    },
  },
  {
    label: '接收方部门',
    field: 'receiveDept',
    component: 'Input',
    slot:'receiveDept',
    colProps: { span: 6 },
  },

];

