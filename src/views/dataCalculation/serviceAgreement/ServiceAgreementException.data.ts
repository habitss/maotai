import {BasicColumn, FormSchema} from '/@/components/Table';
import {ref} from "vue";
import {list as kpiTypeList} from "@/views/dataCalculation/kpi/info/KpiInfo.api";
export let kpiTreeData = ref<any>([]);
(async function f() {
  console.log('111')
  let result = await kpiTypeList({pageNo: 1,pageSize: 9999})
  kpiTreeData.value = result.records;
  console.log('111',kpiTreeData.value)
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
    dataIndex: 'provideDeptVal',
    width: 100,
  },
  {
    title: '产品/服务',
    align: "center",
    ellipsis:false,
    dataIndex: 'product',
    width: 100,
  },
  {
    title: '活动',
    align: "center",
    width: 80,
    ellipsis:false,
    dataIndex: 'activity'
  },
  {
    title: '接收方部门',
    align: "center",
    dataIndex: 'receiveDeptVal',
    width: 100,
  },
  {
    title: '发生日期',
    align: "center",
    dataIndex: 'exceptionDate',
    width: 100,
    customRender:({text}) =>{
      text = !text ? "" : (text.length > 10 ? text.substr(0,10) : text);
      return text;
    },
  },
  {
    title: '异常记录',
    align: "center",
    ellipsis: false,
    dataIndex: 'exceptionDesc'
  },
  {
    title: '异常材料',
    align: "center",
    ellipsis:false,
    width: 100,
    dataIndex: 'exceptionFile'
  },
  {
    title: '影响的KPI',
    align: "center",
    ellipsis: false,
    dataIndex: 'affectKpiCodeVal'
  },
  {
    title: '影响的PI',
    align: "center",
    ellipsis: false,
    dataIndex: 'affectPiTable'
  },
  {
    title: '确认状态',
    align: "center",
    fixed: "right",
    dataIndex: 'confirmStatus',
    width: 100,
  },
  {
    title: '是否5why分析',
    align: "center",
    fixed: "right",
    dataIndex: 'fiveWhyStatus',
    width: 100,
    customRender:function ({ record }) {
      if (record.fiveWhyStatus == 0) {
        return "未分析"
      } else if (record.fiveWhyStatus == 1) {
        return "已分析"
      }
    }
  },
  {
    title: '是否进入行动',
    align: "center",
    fixed: "right",
    dataIndex: 'actionStatus',
    width: 100,
    customRender:function ({ record }) {
      if (record.actionStatus == 0) {
        return "未记录"
      } else if (record.actionStatus == 1) {
        return "已记录"
      }
    }
  },
  {
    title: '审批状态',
    align: "center",
    fixed: "right",
    dataIndex: 'examineStatus',
    width: 100,
    customRender:function ({ record }) {
      if (record.examineStatus == 0) {
        return "未审批"
      } else if (record.examineStatus == 1) {
        return "已审批"
      }
    }
  },
];

export const columnsRec: BasicColumn[] = [
  {
    title: 'id',
    align: "center",
    dataIndex: 'id',
    defaultHidden: true,
  },
  {
    title: '提供方部门',
    align: "center",
    dataIndex: 'provideDeptVal'
  },
  {
    title: '产品/服务',
    align: "center",
    ellipsis:false,
    dataIndex: 'product'
  },
  {
    title: '活动',
    align: "center",
    ellipsis:false,
    dataIndex: 'activity'
  },
  {
    title: '接收方部门',
    align: "center",
    dataIndex: 'receiveDeptVal'
  },
  {
    title: '发生日期',
    align: "center",
    dataIndex: 'exceptionDate',
    customRender:({text}) =>{
      text = !text ? "" : (text.length > 10 ? text.substr(0,10) : text);
      return text;
    },
  },
  {
    title: '异常记录',
    align: "center",
    ellipsis:false,
    dataIndex: 'exceptionDesc'
  },
  {
    title: '异常材料',
    align: "center",
    ellipsis:false,
    dataIndex: 'exceptionFile'
  },
  {
    title: '影响的KPI',
    align: "center",
    dataIndex: 'affectKpiCodeVal'
  },
  {
    title: '影响的PI',
    align: "center",
    dataIndex: 'affectPiTable'
  },
  {
    title: '是否5why分析',
    align: "center",
    fixed: "right",
    dataIndex: 'fiveWhyStatus',
    customRender:function ({ record }) {
      if (record.fiveWhyStatus == 0) {
        return "未分析"
      } else if (record.fiveWhyStatus == 1) {
        return "已分析"
      }
    }
  },
  {
    title: '是否进入行动',
    align: "center",
    fixed: "right",
    dataIndex: 'actionStatus',
    customRender:function ({ record }) {
      if (record.actionStatus == 0) {
        return "未记录"
      } else if (record.actionStatus == 1) {
        return "已记录"
      }
    }
  },
  {
    title: '创建人',
    align: "center",
    fixed: "right",
    dataIndex: 'createBy_dictText',
  },
];

export const searchFormProvidSchema: FormSchema[] = [
  {
    label: '服务提供方',
    field: 'provideDept',
    component: 'Input',
    slot:'provideDeptPro',
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
    slot:'receiveDeptPro',
    colProps: { span: 6 },
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    label: '服务提供方',
    field: 'provideDept',
    component: 'Input',
    slot:'provideDeptRec',
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
    slot:'receiveDeptRec',
    colProps: { span: 6 },
  },
  {
    label: '是否5why分析',
    field: 'fiveWhyStatus',
    component: 'Select',
    componentProps: {
      options: [
        {
          label: '已分析',
          value: '1',
        },
        {
          label: '未分析',
          value: '0',
        },
      ],
    },
    colProps: { span: 6 },
  },
  {
    label: '是否进入行动',
    field: 'actionStatus',
    component: 'Select',
    componentProps: {
      options: [
        {
          label: '已记录',
          value: '1',
        },
        {
          label: '未记录',
          value: '0',
        },
      ],
    },
    colProps: { span: 6 },
  },
  {
    label: '审批状态',
    field: 'examineStatus',
    component: 'Select',
    componentProps: {
      options: [
        {
          label: '已审批',
          value: '1',
        },
        {
          label: '未审批',
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
    label: '服务协议登记信息',
    field: 'serviceAgreement',
    slot:'product',
    component: 'Input',
    colProps: {
      span: 20,
    },
  },
  {
    label: '异常日期',
    field: 'exceptionDate',
    component: 'DatePicker',
    componentProps: {
      //日期格式化，页面上显示的值
      format: 'YYYY-MM-DD',
      //返回值格式化（绑定值的格式）
      valueFormat: 'YYYY-MM-DD',
      //是否显示今天按钮
      showToday: true,
    },
    colProps: { span: 20 },
  },
  {
    label: '异常描述',
    field: 'exceptionDesc',
    component: 'InputTextArea',
    colProps: { span: 20 },
  },
  {
    field: 'exceptionFile',
    component: 'JUpload',
    componentProps: {
      fileType: 'all',
    },
    helpMessage: '无限制上传',
    label: '异常材料',
  },
  {
    label: '影响的KPI',
    field: 'affectKpiCode',
    component: 'TreeSelect',
    componentProps: {
      treeData: kpiTreeData,
      fieldNames: { children: 'children', label: 'kpiName', value: 'kpiCode' },
      treeNodeFilterProp: "kpiType",
    },
    // dynamicDisabled: ({ values }) => {
    //   return !values.kpiYear;
    // },
    colProps: {
      span: 20,
    },
  },
  {
    label: '影响的PI',
    field: 'affectPiCode',
    slot:'affectPiCode',
    component: 'Input',
    colProps: {
      span: 20,
    },
  },
  {
    label: '是否5why分析',
    field: 'fiveWhyStatus',
    component: 'Switch',
    dynamicDisabled: true,
    colProps: { span: 10 },
    componentProps: {
      //非选中时的内容
      unCheckedChildren:'未分析',
      //非选中时的值
      unCheckedValue:0,
      //选中时的内容
      checkedChildren:'已分析',
      //选中时的值
      checkedValue:1,
    },
    defaultValue: 0,
  },
  {
    label: '是否进入行动',
    field: 'actionStatus',
    component: 'Switch',
    dynamicDisabled: true,
    colProps: { span: 10 },
    componentProps: {
      //非选中时的内容
      unCheckedChildren:'未记录',
      //非选中时的值
      unCheckedValue:0,
      //选中时的内容
      checkedChildren:'已记录',
      //选中时的值
      checkedValue:1,
    },
    defaultValue: 0,
  },
  {
    label: '审批状态',
    field: 'examineStatus',
    component: 'Switch',
    dynamicDisabled: true,
    colProps: { span: 10 },
    componentProps: {
      //非选中时的内容
      unCheckedChildren:'待审批',
      //非选中时的值
      unCheckedValue:0,
      //选中时的内容
      checkedChildren:'已审批',
      //选中时的值
      checkedValue:1,
    },
    defaultValue: 0,
  },
];


