import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
export const options = [
  { label: '是', value: 1 },
  { label: '否', value: 0 },
];

//列表数据
export const columns: BasicColumn[] = [
  {
   title: '建议推广类型',
   align:"center",
   children: [
     {
      title: '名称',
      align:"center",
      dataIndex: 'title',
      customCell: (record) => {
        return { rowSpan: record.rowSpan ?? 1 };
      },
      ellipsis: false,
     },
     {
      title: '编号',
      align:"center",
      dataIndex: 'serialNumber',
      customCell: (record) => {
        return { rowSpan: record.rowSpan ?? 1 };
      },
     },
     {
      title: '提报工厂',
      align:"center",
      dataIndex: 'reportFactory',
      customCell: (record) => {
        return { rowSpan: record.rowSpan ?? 1 };
      },
     },
     {
      title: '提报部门',
      align:"center",
      dataIndex: 'reportWorkshop_dictText',
      customCell: (record) => {
        return { rowSpan: record.rowSpan ?? 1 };
      },
     },
     {
      title: '适用部门',
      align:"center",
      dataIndex: 'applicableWorkshopId_dictText'
     },
     {
      title: '改善类型',
      align:"center",
      dataIndex: 'reportType_dictText'
     },
     {
      title: '对应KPI',
      align:"center",
      dataIndex: 'kpiCode_dictText'
     },
     {
      title: '最佳实践评审得分',
      align:"center",
      dataIndex: 'reviewScore',
      ellipsis: false,
     },
   ]
  },
  {
   title: '识别过程记录',
   align:"center",
   children: [
    {
      title: '对应KPI是否好于提报部门',
      align:"center",
      dataIndex: 'deptType',
      ellipsis: false,
      customRender: ({ text }) => {
        return getOptionsValue(text);
      }
     },
     {
      title: '对应设备、工艺是否一致',
      align:"center",
      dataIndex: 'type',
      ellipsis: false,
      customRender: ({ text }) => {
        return getOptionsValue(text);
      }
     },
     {
      title: '识别结果(是否推广)',
      align:"center",
      dataIndex: 'identifyType',
      ellipsis: false,
      customRender: ({ text }) => {
        return getOptionsValue(text);
      }
     },
     {
      title: '推广原因',
      align:"center",
      dataIndex: 'promotedReason',
      ellipsis: false,
     },
     {
      title: '未推广原因',
      align:"center",
      dataIndex: 'unpromotedReason',
      ellipsis: false,
     },
     {
      title: '行动计划',
      align:"center",
      dataIndex: 'ifTrack',
      ellipsis: false,
      slots: {
        customRender: 'ifTrack',
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
    label: '改善类型',
    field: 'reportType',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "mes3_best_practice_type where del_flag = 0, type_name, id",
      showChooseOption: false,
      mode: 'multiple',
    },
  },
  {
    label: '提报部门',
    field: 'reportWorkshop',
    component: 'KtDepartSelect',
    componentProps: {
      showChooseOption: false,
      getPopupContainer: () => document.body,
      selectType: 'depart',
      mode: 'multiple',
    }
  },
  {
    label: '适用部门',
    field: 'applicableWorkshopId',
    component: 'KtDepartSelect',
    componentProps: {
      showChooseOption: false,
      getPopupContainer: () => document.body,
      selectType: 'depart',
      mode: 'multiple',
    }
  },
  {
    label: '名称',
    field: 'title',
    component: 'JInput',
  },
  {
    label: '提报工厂',
    field: 'reportFactory',
    component: 'JInput',
  },
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '建议推广类型',
    field: 'divisionLine',
    component: 'Divider',
    colProps: { span: 24 },
    componentProps: {
      dashed: true,
      orientation: 'center',
      plain: true,
      type:'horizontal',
    },
  },
  {
    label: '名称',
    field: 'title',
    component: 'Input',
    componentProps: {
      disabled: true
    }
  },
  {
    label: '编号',
    field: 'serialNumber',
    component: 'Input',
    componentProps: {
      disabled: true
    }
  },
  {
    label: '提报工厂',
    field: 'reportFactory_dictText',
    component: 'Input',
    componentProps: {
      disabled: true
    }
  },
  {
    label: '提报部门',
    field: 'reportWorkshop_dictText',
    component: 'Input',
    componentProps: {
      disabled: true
    }
  },
  {
    label: '适用部门',
    field: 'applicableWorkshopId_dictText',
    component: 'Input',
    componentProps: {
      disabled: true
    }
  },
  {
    label: '改善类型',
    field: 'reportType_dictText',
    component: 'Input',
    componentProps: {
      disabled: true
    }
  },
  {
    label: '对应KPI',
    field: 'kpiCode_dictText',
    component: 'Input',
    componentProps: {
      disabled: true
    }
  },
  {
    label: '最佳实践评审得分',
    field: 'reviewScore',
    component: 'Input',
    componentProps: {
      disabled: true
    }
  },
  {
    label: '识别过程记录',
    field: 'divisionLine',
    component: 'Divider',
    colProps: { span: 24 },
    componentProps: {
      dashed: true,
      orientation: 'center',
      plain: true,
      type:'horizontal',
    },
  },
  {
    label: '对应KPI是否好于提报部门',
    field: 'deptType',
    component: 'Select',
    componentProps: {
      options,
    },
  },
  {
    label: '对应设备、工艺是否一致',
    field: 'type',
    component: 'Select',
    componentProps: {
      options,
    },
  },
  {
    label: '识别结果(是否推广)',
    field: 'identifyType',
    component: 'Select',
    componentProps: {
      options,
    },
  },
  {
    label: '推广原因',
    field: 'promotedReason',
    component: 'InputTextArea',
    colProps: { span: 24 },
  },
  {
    label: '未推广原因',
    field: 'unpromotedReason',
    component: 'InputTextArea',
    colProps: { span: 24 },
  },
  {
    label: '行动计划',
    field: 'ifTrack',
    component: 'Input',
    slot: 'ifTrack'
  },
	// TODO 主键隐藏字段，目前写死为ID
	{
	  label: '',
	  field: 'id',
	  component: 'Input',
	  show: false
	},
];

// 高级查询数据
export const superQuerySchema = {
  tenantId: {title: '租户ID',order: 0,view: 'number', type: 'number',},
  factoryCode: {title: '工厂Code',order: 1,view: 'text', type: 'string',},
  deptCode: {title: '部门Code',order: 2,view: 'text', type: 'string',},
  lineCode: {title: '产线Code',order: 3,view: 'text', type: 'string',},
  section: {title: '工段',order: 4,view: 'text', type: 'string',},
  proId: {title: '评估表ID(mes3_best_practice_promotion)',order: 5,view: 'number', type: 'number',},
  status: {title: '是否展示 1: 展示  0:不展示',order: 6,view: 'number', type: 'number',},
  title: {title: '名称',order: 7,view: 'text', type: 'string',},
  serialNumber: {title: '编号',order: 8,view: 'text', type: 'string',},
  yearMon: {title: '年',order: 9,view: 'text', type: 'string',},
  reportFactoryId: {title: '提报工厂编码',order: 10,view: 'number', type: 'number',},
  reportWorkshopId: {title: '提报部门编码',order: 11,view: 'number', type: 'number',},
  reportType: {title: '改善类型',order: 12,view: 'text', type: 'string',},
  kpiCode: {title: '对应KPI',order: 13,view: 'text', type: 'string',},
  reviewScore: {title: '最佳实践评审得分',order: 14,view: 'text', type: 'string',},
  applicableWorkshopId: {title: '适用部门编码',order: 15,view: 'number', type: 'number',},
  deptType: {title: '对应KPI是否好于提报部门',order: 16,view: 'number', type: 'number',},
  type: {title: '对应设备,工艺是否一致',order: 17,view: 'number', type: 'number',},
  identifyType: {title: '识别结果(是否推广)',order: 18,view: 'number', type: 'number',},
  unpromotedReason: {title: '推广原因',order: 19,view: 'text', type: 'string',},
  ifTrack: {title: '行动计划',order: 20,view: 'text', type: 'string',},
  trackId: {title: '行动计划id',order: 21,view: 'text', type: 'string',},
  remark: {title: '备注',order: 23,view: 'text', type: 'string',},
  sort: {title: '排序',order: 24,view: 'number', type: 'number',},
};

/**
* 流程表单调用这个方法获取formSchema
* @param param
*/
export function getBpmFormSchema(_formData): FormSchema[]{
  // 默认和原始表单保持一致 如果流程中配置了权限数据，这里需要单独处理formSchema
  return formSchema;
}

function getOptionsValue(value){
  return value == 1 ? '是' : value == 0 ? '否' : '';
}