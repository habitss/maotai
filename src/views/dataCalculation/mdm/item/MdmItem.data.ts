import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { CheckItemTypeEnum } from '/@/enums/planEnum';

const switchComponentProps = {
  //非选中时的内容
  unCheckedChildren: '否',
  //非选中时的值
  unCheckedValue: 0,
  //选中时的内容
  checkedChildren: '是',
  //选中时的值
  checkedValue: 1,
};

//列表数据
export const columns: BasicColumn[] = [
  {
    title: '工厂',
    align: "center",
    dataIndex: 'factoryCode_dictText'
  },
  {
    title: '部门',
    align: "center",
    dataIndex: 'deptCode_dictText'
  },
  {
    title: '产线',
    align: "center",
    dataIndex: 'lineCode_dictText'
  },
  {
    title: '设备',
    align: "center",
    dataIndex: 'deviceId_dictText'
  },
  {
    title: '检查项类型',
    align: "center",
    dataIndex: 'checkItemType_dictText'
  },
  {
    title: '检查项目/指标',
    align: "center",
    dataIndex: 'itemName'
  },
  {
    title: '检查地点',
    align: "center",
    dataIndex: 'itemLocation'
  },
  {
    title: '检查频次',
    align: "center",
    dataIndex: 'remark'
  },
  {
    title: '检查标准',
    align: "center",
    dataIndex: 'itemStandard'
  },
  {
    title: '检查数据填写说明',
    align: "center",
    dataIndex: 'detail'
  },
  {
    title: '条目类型',
    align: "center",
    dataIndex: 'itemTypeCode_dictText'
  },
  {
    title: '结果类型',
    align: "center",
    dataIndex: 'resultTypeCode_dictText'
  },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: '部门',
    field: 'deptCode',
    component: 'KtDepartSelect',
    componentProps: ({ formActionType, formModel }) => {
      return {
        selectType: 'depart',
        onChange: async e => {
          formModel.lineCode = null;
          formModel.deviceId = null;
          await formActionType.updateSchema([
            {
              field: 'lineCode',
              componentProps: {
                selectType: 'line',
                parentKey: e,
                onChange: async e => {
                  formModel.deviceId = null;
                }
              },
            },
          ]);
        }
      }
    },
  },
  {
    label: '产线',
    field: 'lineCode',
    component: 'KtDepartSelect',
  },
  {
    label: '设备',
    field: 'deviceId',
    component: 'JDictSelectTag',
    componentProps: ({ formModel }) => {
      return {
        showChooseOption: false,
        dictCode: "mes3_mdm_device where del_flag = 0 and line_code = '" + formModel.lineCode + "' order by sort, device_name, id",
      }
    },
  },
  {
    label: '检查项目/指标',
    field: 'itemName',
    component: 'Input',
  },
  {
    label: '检查地点',
    field: 'itemLocation',
    component: 'Input',
  },
  {
    label: '检查频次',
    field: 'remark',
    component: 'Input',
  },
  {
    label: '检查标准',
    field: 'itemStandard',
    component: 'Input',
  },
  {
    label: '检查数据填写说明',
    field: 'detail',
    component: 'Input',
  },
  {
    label: '结果类型',
    field: 'resultTypeCode',
    component: 'JDictSelectTag',
    componentProps: {
      showChooseOption: false,
      dictCode: "mes3_mdm_dict where group_code = 'MES3_ITEM_RESULT_TYPE' order by sort, item_key, item_val",
    },
  },
  {
    label: '条目类型',
    field: 'itemTypeCode',
    component: 'JDictSelectTag',
    componentProps: {
      showChooseOption: false,
      dictCode: "mes3_mdm_dict where group_code = 'MES3_ITEM_TYPE' order by sort, item_key, item_val",
    },
  },
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '部门',
    field: 'deptCode',
    component: 'KtDepartSelect',
    componentProps: ({ formActionType, formModel }) => {
      return {
        selectType: 'depart',
        onChange: async e => {
          formModel.lineCode = null;
          formModel.deviceId = null;
          await formActionType.updateSchema([
            {
              field: 'lineCode',
              componentProps: {
                selectType: 'line',
                parentKey: e,
                onChange: async e => {
                  formModel.deviceId = null;
                }
              },
            },
          ]);
        }
      }
    },
    required: true,
    dynamicDisabled: ({ values }) => {
      return values.deptDeviceEdit;
    },
  },
  {
    label: '产线',
    field: 'lineCode',
    component: 'KtDepartSelect',
    componentProps: ({ formModel }) => {
      return {
        selectType: 'line',
        parentKey: formModel.deptCode,
        onChange: async () => {
          formModel.deviceId = null;
        }
      }
    },
    required: true,
    dynamicDisabled: ({ values }) => {
      return values.deptDeviceEdit;
    },
  },
  {
    label: '设备',
    field: 'deviceId',
    component: 'JDictSelectTag',
    componentProps: ({ formModel }) => {
      return {
        showChooseOption: false,
        showSearch: true,
        getPopupContainer: () => document.body,
        dictCode: "mes3_mdm_device where del_flag = 0 and line_code = '" + formModel.lineCode + "' order by sort, device_name, id",
      }
    },
    required: true,
    dynamicDisabled: ({ values }) => {
      return values.deptDeviceEdit;
    },
  },
  {
    label: '检查项类型',
    field: 'checkItemType',
    component: 'JDictSelectTag',
    componentProps: {
      showChooseOption: false,
      dictCode: "mes3_mdm_dict where del_flag = 0 and group_code = 'MES3_JCXLX' order by sort, item_key, item_val",
      stringToNumber: true,
      disabled: true,
    },
    required: true,
  },
  {
    label: '交接班类型',
    field: 'isHandover',
    component: 'RadioGroup',
    componentProps: {
      options: [
        { label: '交班', value: 1 },
        { label: '接班', value: 0 },
      ],
    },
    defaultValue: 1,
    colProps: { span: 12 },
    required: true,
    ifShow: ({ values }) => {
      return values.checkItemType == CheckItemTypeEnum.TYPE_RECORD;
    },
  },
  {
    label: '是否生产检查',
    field: 'isProduction',
    component: 'Switch',
    componentProps: switchComponentProps,
    defaultValue: 1,
    colProps: { span: 12 },
    required: true,
    ifShow: ({ values }) => {
      return values.checkItemType == CheckItemTypeEnum.TYPE_RECORD;
    },
  },
  {
    label: '检查项目/指标',
    field: 'itemName',
    component: 'InputTextArea',
    required: true,
  },
  {
    label: '检查地点',
    field: 'itemLocation',
    component: 'InputTextArea',
  },
  {
    label: '检查频次',
    field: 'remark',
    component: 'InputTextArea',
  },
  {
    label: '检查标准',
    field: 'itemStandard',
    component: 'InputTextArea',
    required: true,
  },
  {
    label: '检查数据填写说明',
    field: 'detail',
    component: 'InputTextArea',
    required: true,
  },
  {
    label: '结果类型',
    field: 'resultTypeCode',
    component: 'JDictSelectTag',
    componentProps: {
      showChooseOption: false,
      dictCode: "mes3_mdm_dict where del_flag = 0 and group_code = 'MES3_ITEM_RESULT_TYPE' order by sort, item_key, item_val",
    },
    required: true,
  },
  {
    label: '结果是否必填',
    field: 'isRequire',
    component: 'Switch',
    componentProps: switchComponentProps,
    defaultValue: 1,
  },
  {
    label: '下拉选项',
    field: 'selectOption',
    component: 'Input',
    slot: 'selectOption',
    ifShow: ({ values }) => {
      return values.id && (values.resultTypeCode == 8 || values.resultTypeCode == 9);
    },
  },
  {
    label: '条目类型',
    field: 'itemTypeCode',
    component: 'JDictSelectTag',
    componentProps: {
      showChooseOption: false,
      dictCode: "mes3_mdm_dict where group_code = 'MES3_ITEM_TYPE', item_key, item_val",
    },
  },
  {
    label: '是否需要上传图片',
    field: 'isUploadImg',
    component: 'Switch',
    componentProps: switchComponentProps,
    defaultValue: 0,
    colProps: { span: 12 },
  },
  {
    label: '是否需要检查时间',
    field: 'isCheckTime',
    component: 'Switch',
    componentProps: switchComponentProps,
    defaultValue: 0,
    colProps: { span: 12 },
  },
  {
    label: '是否需要备注',
    field: 'isRemark',
    component: 'Switch',
    componentProps: switchComponentProps,
    defaultValue: 0,
    colProps: { span: 12 },
  },
  {
    label: '是否需要确认人',
    field: 'isConfirmUser',
    component: 'Switch',
    componentProps: switchComponentProps,
    defaultValue: 0,
    colProps: { span: 12 },
  },
  {
    label: '是否需要NA项',
    field: 'isNa',
    component: 'Switch',
    componentProps: switchComponentProps,
    defaultValue: 0,
    colProps: { span: 12 },
  },
  {
    label: '是否对结果进行合格判定',
    field: 'isQualified',
    component: 'Switch',
    componentProps: { ...switchComponentProps, disabled: true },
    defaultValue: 1,
    colProps: { span: 12 },
    show: false,
  },
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false
  },
  {
    label: '部门、设备是否可编辑',
    field: 'deptDeviceEdit',
    component: 'Input',
    show: false
  },
];