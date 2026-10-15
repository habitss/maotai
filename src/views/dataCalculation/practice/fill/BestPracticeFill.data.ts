import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { useUserStore } from '/@/store/modules/user';
import { list as kpiList } from '/@/views/dataCalculation/kpi/info/KpiInfo.api';
import dayjs from 'dayjs';
import { getCurrentDepart } from '/@/utils/common/compUtils';

const currentDepart = getCurrentDepart();
// 用户信息
const { userInfo } = useUserStore();
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '知识共享最佳实践提报清单',
    children: [
      {
        title: '名称',
        align: "center",
        dataIndex: 'title',
        width: 120,
        slots: {
          customRender: 'title',
        },
      },
      {
        title: '提报员工',
        align: "center",
        dataIndex: 'userId_dictText',
        width: 90,
      },
      {
        title: '提报班组',
        align: "center",
        dataIndex: 'shiftGroupId_dictText',
        width: 90,
      },
      {
        title: '提报部门',
        align: "center",
        dataIndex: 'reportDeptId_dictText',
        width: 90,
      },
      {
        title: '提报工厂',
        align: "center",
        dataIndex: 'factoryCode_dictText',
        width: 90,
      },
      {
        title: '提报时间',
        align: "center",
        dataIndex: 'reportTime',
        width: 90,
        customRender: ({ text }) => {
          text = !text ? "" : (text.length > 10 ? text.substr(0, 10) : text);
          return text;
        },
      },
      {
        title: '改善类型',
        align: "center",
        dataIndex: 'improvementType_dictText',
        width: 120,
      },
      {
        title: '改善效果',
        align: "center",
        dataIndex: 'improvementEffect_dictText',
        width: 120,
      },
      {
        title: '对应KPI',
        align: "center",
        dataIndex: 'kpiCode_dictText',
        width: 90,
      },
    ]
  },
  {
    title: '班组评审',
    children: [
      {
        title: '班组评审意见',
        align: "center",
        dataIndex: 'shiftGroupReview',
        width: 150
      },
      {
        title: '评审结果',
        align: "center",
        dataIndex: 'shiftGroupResult',
        width: 90,
        slots: {
          customRender: 'shiftGroupResult',
        },
      },
    ]
  },
  {
    title: '部门评审',
    children: [
      {
        title: '部门评审意见',
        align: "center",
        dataIndex: 'deptReview',
        width: 150
      },
      {
        title: '评审结果',
        align: "center",
        dataIndex: 'deptResult',
        width: 90,
        slots: {
          customRender: 'deptResult',
        },
      },
    ]
  },
  {
    title: '工厂评审',
    children: [
      {
        title: '工厂评审意见',
        align: "center",
        dataIndex: 'factoryReview',
        width: 150
      },
      {
        title: '评审结果',
        align: "center",
        dataIndex: 'factoryResult',
        width: 90,
        slots: {
          customRender: 'factoryResult',
        },
      },
    ]
  },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: "年月",
    field: 'yearMon',
    component: 'Input',
    slot: 'yearMon'
  },
  {
    label: "提报部门",
    field: 'reportDeptId',
    component: 'KtDepartTreeSelect',
    componentProps: {
      allowClear: true,
      fieldNames: { label:'title', value: 'orgCode' },
      defaultExpandLevel: 1,
    },
    defaultValue: currentDepart?.cutDeptCode,
  },
  {
    label: "提报时间",
    field: 'reportTime',
    component: 'RangePicker',
    componentProps: {
      valueFormat: 'YYYY-MM-DD',
      format: 'YYYY-MM-DD',
    },
    defaultValue: [
      dayjs().subtract(3, 'month').format("YYYY-MM-DD HH:mm:ss"),
      dayjs().format("YYYY-MM-DD HH:mm:ss"),
    ]
  },
  {
    label: '改善类型',
    field: 'improvementType',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "mes3_best_practice_type where del_flag = 0, type_name, id",
      showChooseOption: false,
      mode: 'multiple',
    },
  },
  {
    label: '名称',
    field: 'title',
    component: 'JInput',
  },
];

//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '名称',
    field: 'title',
    component: 'Input',
    required: true,
  },
  {
    label: '提报员工',
    field: 'userId',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'sys_user, realname, id, del_flag = 0',
      showChooseOption: false,
      showSearch: true,
    },
    defaultValue: userInfo?.id,
    required: true,
  },
  {
    label: '提报班组',
    field: 'shiftGroupId',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'mes3_mdm_shift_group, group_name, id, del_flag = 0',
      showChooseOption: false,
      showSearch: true,
    }
  },
  {
    label: '提报部门',
    field: 'reportDeptId',
    component: 'Input',
    required: true,
    slot: 'deptCode'
  },
  {
    label: '提报部门名称',
    field: 'reportDeptName',
    component: 'Input',
    show: false
  },
  {
    label: '提报时间',
    field: 'reportTime',
    component: 'DatePicker',
    componentProps: {
      valueFormat: 'YYYY-MM-DD'
    },
    required: true,
  },
  {
    label: '改善类型',
    field: 'improvementType',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "mes3_best_practice_type where del_flag = 0, type_name, id",
      showChooseOption: false,
    },
    slot: 'improvementTypeSlot',
    required: true,
  },
  {
    label: '改善效果',
    field: 'improvementEffect',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "mes3_mdm_dict where del_flag = 0 and group_code = 'MES3_BEST_EFFECT', item_key, item_val",
      showChooseOption: false,
    },
  },
  {
    label: '对应KPI',
    field: 'kpiCode',
    component: 'ApiTreeSelect',
    componentProps: {
      api: kpiList,
      fieldNames: {children:'children', label:'kpiName', value: 'id' },
      resultField:'records',
      showSearch: true,
      treeNodeFilterProp: 'kpiName',
      params:{pageSize: 9999},
    },
  },
  {
    //空白占位，用于表单排版
    label: 'none',
    field: 'none',
    component: 'Input',
    colSlot: 'null',
    colProps: { span: 16 },
  },
  {
    label: '班组评审意见',
    field: 'shiftGroupReview',
    component: 'InputTextArea',
    colProps: { span: 16 },
  },
  {
    label: '班组评审结果',
    field: 'shiftGroupResult',
    component: 'Input',
    slot: 'shiftGroupResult'
  },
  {
    label: '部门评审意见',
    field: 'deptReview',
    component: 'InputTextArea',
    colProps: { span: 16 },
  },
  {
    label: '部门评审结果',
    field: 'deptResult',
    component: 'Input',
    slot: 'deptResult'
  },
  {
    label: '工厂评审意见',
    field: 'factoryReview',
    component: 'InputTextArea',
    colProps: { span: 16 },
  },
  {
    label: '工厂评审结果',
    field: 'factoryResult',
    component: 'Input',
    slot: 'factoryResult'
  },
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false
  },
  {
    label: '',
    field: 'shiftGroupReportId',
    component: 'Input',
    show: false
  },
  {
    label: '',
    field: 'shiftGroupAvgScore',
    component: 'Input',
    show: false
  },
  {
    label: '',
    field: 'deptReportId',
    component: 'Input',
    show: false
  },
  {
    label: '',
    field: 'deptAvgScore',
    component: 'Input',
    show: false
  },
  {
    label: '',
    field: 'factoryReportId',
    component: 'Input',
    show: false
  },
  {
    label: '',
    field: 'factoryAvgScore',
    component: 'Input',
    show: false
  },
];

//最佳实践推荐列表数据
export const recommendColumns: BasicColumn[] = [
  {
    title: '改善类型',
    align: "center",
    dataIndex: 'title',
    width: 80,
    customCell: (record, index) => {
      return { rowSpan: index === 0 ? record.rowSpan : 0 };
    },
  },
  {
    title: '改善类型',
    align: "center",
    dataIndex: 'improvementTypeName',
    width: 120,
  },
  {
    title: '改善前（量化数据）',
    align: "center",
    dataIndex: 'beforeImprovement',
  },
  {
    title: '改善后（量化数据）',
    align: "center",
    dataIndex: 'afterImprovement',
  },
  {
    title: '实际成本（量化数据）',
    align: "center",
    dataIndex: 'actualCost',
  },
  {
    title: '推广范围',
    align: "center",
    dataIndex: 'promotionScope',
  },
];