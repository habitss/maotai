import { h, VNode } from 'vue';
import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
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
    dataIndex: 'deviceIds_dictText'
  },
  {
    title: '订单号',
    align: "center",
    dataIndex: 'orderName'
  },
  {
    title: '计划名',
    align: "center",
    dataIndex: 'planName',
    resizable: true
  },
  {
    title: '计划日期',
    align: "center",
    dataIndex: 'ymd',
  },
  {
    title: '预计开始时间',
    align: "center",
    dataIndex: 'planStartTime'
  },
  {
    title: '预计结束时间',
    align: "center",
    dataIndex: 'planEndTime'
  },
  {
    title: '计划大类',
    align: "center",
    dataIndex: 'planType_dictText'
  },
  {
    title: '计划细类',
    align: "center",
    dataIndex: 'planTypeSub_dictText'
  },
  {
    title: '实际开始时间',
    align: "center",
    dataIndex: 'realStartTime'
  },
  {
    title: '实际结束时间',
    align: "center",
    dataIndex: 'realEndTime'
  },
  {
    title: '状态',
    align: "center",
    dataIndex: 'planStatus',
    width: 100,
    fixed: 'right',
    customRender: ({ text }): VNode => {
      switch (text) {
        case 1:
          return h('span', {
            style: {
              color: 'orange',
            },
          }, '未开始')
        case 2:
          return h('span', {
            style: {
              color: 'green',
            },
          }, '进行中')
        case 3:
          return h('span', {
            style: {
              color: 'red',
            },
          }, '已完成')
        case 5:
          return h('span', {
            style: {
              color: 'orange',
            },
          }, '暂停中')
        case 7:
          return h('span', {
            style: {
              color: 'red',
            },
          }, '已完成')
        case 4:
          return h('span', {
            style: {
              color: 'gray',
            },
          }, '已取消')
        default:
          return h('span', {
            style: {
              color: 'gray',
            },
          }, '/')
      }
    },
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
    componentProps: ({ formModel }) => {
      return {
        selectType: 'line',
        parentKey: formModel.deptCode,
      }
    },
  },
  {
    label: '订单号',
    field: 'orderName',
    component: 'Input',
  },
  {
    label: '计划名',
    field: 'planName',
    component: 'Input',
  },
  {
    label: '计划日期',
    field: 'ymd',
    component: 'RangeDate',
    componentProps: {
      valueFormat: 'YYYY-MM-DD',
      format: 'YYYY-MM-DD',
    },
  },
  {
    label: '计划大类',
    field: 'planType',
    component: 'JDictSelectTag',
    componentProps: {
      showChooseOption: false,
      dictCode: "mes3_mdm_dict where group_code = 'MES3_PLAN_TYPE' and del_flag = 0 and item_val != '4', item_key, item_val",
    },
  },
  {
    label: '计划细类',
    field: 'planTypeSub',
    component: 'JDictSelectTag',
    componentProps: {
      showChooseOption: false,
      dictCode: "mes3_mdm_dict where group_code = 'MES3_PLAN_TYPE_SUB', item_key, item_val",
      mode: 'multiple',
    },
  },
  {
    label: '状态',
    field: 'planStatus',
    component: 'JDictSelectTag',
    componentProps: {
      showChooseOption: false,
      dictCode: "mes3_mdm_dict where group_code = 'MES3_PLAN_STATUS', item_key, item_val",
    },
  },
  {
    label: '检查项',
    field: 'taskItemName',
    component: 'Input',
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
        getPopupContainer: () => document.body,
        onChange: async e => {
          formModel.lineCode = null;
          formModel.deviceIds = null;
          formModel.triggerPostIds = null;
          await formActionType.updateSchema([
            {
              field: 'lineCode',
              componentProps: {
                selectType: 'line',
                parentKey: e,
                onChange: async e => {
                  formModel.deviceIds = null;
                }
              },
            },
          ]);
        }
      }
    },
    required: true,
  },
  {
    label: '产线',
    field: 'lineCode',
    component: 'KtDepartSelect',
    componentProps: ({ formModel }) => {
      return {
        selectType: 'line',
        getPopupContainer: () => document.body,
        parentKey: formModel.deptCode,
      }
    },
    required: true,
  },
  {
    label: '设备',
    field: 'deviceIds',
    component: 'JDictSelectTag',
    componentProps: ({ formModel }) => {
      return {
        showChooseOption: false,
        getPopupContainer: () => document.body,
        dictCode: "mes3_mdm_device where del_flag = 0 and line_code = '" + formModel.lineCode + "' order by sort, device_name, id",
        mode: 'multiple',
      }
    },
    required: true,
  },
  {
    label: '计划操作岗位',
    field: 'triggerPostId',
    component: 'JDictSelectTag',
    componentProps: ({ formModel }) => {
      return {
        showChooseOption: false,
        getPopupContainer: () => document.body,
        dictCode: "mes3_mdm_post where del_flag = 0 and dept_code = '" + formModel.deptCode + "' and post_type = 1 order by sort, post_name, id",
      }
    },
    required: true,
  },
  {
    label: '订单号',
    field: 'orderName',
    component: 'Input',
    required: true,
  },
  {
    label: '计划名',
    field: 'planName',
    component: 'Input',
    required: true,
  },
  {
    label: '预计开始时间',
    field: 'planStartTime',
    component: 'DatePicker',
    componentProps: {
      showTime: true,
      valueFormat: 'YYYY-MM-DD HH:mm',
      format: 'YYYY-MM-DD HH:mm',
    },
    required: true,
  },
  {
    label: '预计结束时间',
    field: 'planEndTime',
    component: 'DatePicker',
    componentProps: {
      showTime: true,
      valueFormat: 'YYYY-MM-DD HH:mm',
      format: 'YYYY-MM-DD HH:mm',
    },
    required: true,
  },
  {
    label: '计划大类',
    field: 'planType',
    component: 'JDictSelectTag',
    componentProps: ({ formModel }) => {
      return {
        showChooseOption: false,
        getPopupContainer: () => document.body,
        dictCode: "mes3_mdm_dict where group_code = 'MES3_PLAN_TYPE' and del_flag = 0 and (item_val < 3 or item_val > 6), item_key, item_val",
        stringToNumber: true,
      }
    },
    required: true,
  },
  {
    label: '计划细类',
    field: 'planTypeSub',
    component: 'JDictSelectTag',
    componentProps: ({ formModel }) => {
      return {
        showChooseOption: false,
        getPopupContainer: () => document.body,
        showSearch: true,
        dictCode: "mes3_mdm_dict where group_code = 'MES3_PLAN_TYPE_SUB', item_key, item_val",
      }
    },
    required: true,
    ifShow: ({ values }) => {
      return values.planType == 2;
    },
  },
  // TODO 主键隐藏字段，目前写死为ID
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false
  },
];

//暂停记录列表数据
export const pauseColumns: BasicColumn[] = [
  {
    title: '操作类型',
    align: "center",
    dataIndex: 'type',
    customRender: ({ text }): VNode => {
      switch (text) {
        case 2:
          return h('span', {
            style: {
              color: 'green',
            },
          }, '恢复')
        case 5:
          return h('span', {
            style: {
              color: 'red',
            },
          }, '暂停')
        default:
          return h('span', {}, '')
      }
    }
  },
  {
    title: '操作人',
    align: "center",
    dataIndex: 'createBy_dictText'
  },
  {
    title: '操作时间',
    align: "center",
    dataIndex: 'createTime'
  },
];