import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import dayjs from "dayjs";
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '设备名称',
    align: "center",
    ellipsis:false,
    dataIndex: 'deviceId_dictText'
  },
  {
    title: '任务名称',
    align: "center",
    ellipsis:false,
    dataIndex: 'taskName'
  },
  {
    title: '计划任务数',
    align: "center",
    ellipsis:false,
    width: 100,
    dataIndex: 'taskCount'
  },
  {
    title: '实际完成数',
    align: "center",
    ellipsis:false,
    width: 100,
    dataIndex: 'taskCompleteCount'
  },
  {
    title: '未完成数',
    align: "center",
    ellipsis:false,
    width: 100,
    dataIndex: 'taskNoCompleteCount'
  },
  {
    title: '完成率',
    align: "center",
    ellipsis:false,
    width: 100,
    dataIndex: 'taskCompleteRate',
    customRender: ({ record }) => {
      return calculatePercentage(record.taskCompleteCount, record.taskCount);
    }
  },
  {
    title: '检查项数量',
    align: "center",
    ellipsis:false,
    width: 100,
    dataIndex: 'orderResultCount'
  },
  {
    title: '合格数量',
    align: "center",
    ellipsis:false,
    width: 100,
    dataIndex: 'orderResultCompleteCount'
  },
  {
    title: '不合格数量',
    align: "center",
    ellipsis:false,
    width: 100,
    dataIndex: 'orderResultNoCompleteCount',
  },
  {
    title: '合格率',
    align: "center",
    ellipsis:false,
    width: 100,
    dataIndex: 'orderResultRate',
    customRender: ({ record }) => {
      return calculatePercentage(record.orderResultCompleteCount, record.orderResultCount);
    }
  },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: '年',
    field: 'year',
    component: 'DatePicker',
    componentProps: {
      picker: 'year',
      format: 'YYYY',
      valueFormat: 'YYYY',
      allowClear: false,
    },
    defaultValue: dayjs().format('YYYY'),
    colProps: { span: 6 },
  },
  {
    label: '月',
    field: 'month',
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
    },
    defaultValue: (new Date().getMonth() + 1).toString(),
    colProps: { span: 6 },
  },
  {
    label: '日期区间',
    field: 'realStartTime',
    component: 'RangeDate',
    componentProps: {
      format: 'YYYY-MM-DD',
    },
    colProps: { span: 6 },
  },
  {
    label: '部门',
    field: 'deptCode',
    component: 'KtDepartSelect',
    colProps: { span: 6 },
    componentProps: ({ formActionType, formModel }) => {
      return {
        selectType: 'depart',
        getPopupContainer: () => document.body,
        onChange: async e => {
          formModel.lineCode = null;
          formModel.deviceIds = null;
          formModel.team = null;
          await formActionType.updateSchema([
            {
              field: 'lineCode',
              componentProps: {
                selectType: 'line',
                parentKey: e,
                onChange: () => {
                  formModel.deviceIds = null;
                  formModel.team = null;
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
    colProps: { span: 6 },
    componentProps: ({ formModel }) => {
      return {
        selectType: 'line',
        getPopupContainer: () => document.body,
        parentKey: formModel.deptCode,
        onChange: async e => {
          formModel.deviceIds = null;
          formModel.team = null;
        }
      }
    },
  },
  {
    label: '设备',
    field: 'deviceIds',
    component: 'JDictSelectTag',
    colProps: { span: 6 },
    componentProps: ({ formModel }) => {
      return {
        showChooseOption: false,
        getPopupContainer: () => document.body,
        dictCode: "mes3_mdm_device where del_flag = 0 and line_code = '" + formModel.lineCode + "' order by sort, device_name, id",
        mode: 'multiple',
      }
    },
  },
  {
    label: '班组',
    field: 'team',
    component: 'JDictSelectTag',
    colProps: { span: 6 },
    componentProps: ({ formModel }) => {
      return {
        showChooseOption: false,
        getPopupContainer: () => document.body,
        dictCode: "mes3_mdm_shift_group where del_flag = 0 and line_code = '" + formModel.lineCode + "' order by sort, group_name, id",
        mode: 'multiple',
      }
    },
  },
  {
    label: '任务名称',
    field: 'taskId',
    component: 'JDictSelectTag',
    colProps: { span: 6 },
    componentProps: ({ formModel }) => {
      return {
        showSearch: true,
        showChooseOption: false,
        getPopupContainer: () => document.body,
        dictCode: "mes3_mdm_task ,task_name, id , line_code = '" + formModel.lineCode + "'",
      }
    },
  },
  {
    label: '计划类型',
    field: 'planType',
    component: 'JDictSelectTag',
    colProps: { span: 6 },
    componentProps: () => {
      return {
        showChooseOption: false,
        getPopupContainer: () => document.body,
        dictCode: "mes3_mdm_dict where group_code = 'MES3_PLAN_TYPE' and del_flag = 0 and (item_val < 3 or item_val > 6), item_key, item_val",
        stringToNumber: true,
      }
    },
  },
  {
    label: '任务种类',
    field: 'taskKindCode',
    component: 'JDictSelectTag',
    colProps: { span: 6 },
    componentProps: () => {
      return {
        showChooseOption: false,
        getPopupContainer: () => document.body,
        dictCode: "mes3_mdm_dict where group_code = 'MES3_TASK_KIND' and del_flag = 0, item_key, item_val",
        stringToNumber: true,
      }
    },
  },
  {
    label: '岗位名称',
    field: 'postId',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "mes3_mdm_post where del_flag = 0 , post_name, id",
      showChooseOption: false,
      showSearch: true
    },
  	colProps: { span: 6 },
  },
];

export const searchResultFormSchema: FormSchema[] = [
  {
    label: '年',
    field: 'year',
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
    field: 'month',
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
    },
    defaultValue: (new Date().getMonth() + 1).toString(),
    colProps: { span: 6 },
  },
  {
    label: '日期区间',
    field: 'realStartTime',
    component: 'RangeDate',
    componentProps: {
      format: 'YYYY-MM-DD',
    },
    colProps: { span: 6 },
  },
  {
    label: '部门',
    field: 'deptCode',
    component: 'KtDepartSelect',
    colProps: { span: 6 },
    componentProps: ({ formActionType, formModel }) => {
      return {
        selectType: 'depart',
        getPopupContainer: () => document.body,
        onChange: async e => {
          formModel.lineCode = null;
          formModel.deviceIds = null;
          formModel.team = null;
          await formActionType.updateSchema([
            {
              field: 'lineCode',
              componentProps: {
                selectType: 'line',
                parentKey: e,
                onChange: () => {
                  formModel.deviceIds = null;
                  formModel.team = null;
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
    colProps: { span: 6 },
    componentProps: ({ formModel }) => {
      return {
        selectType: 'line',
        getPopupContainer: () => document.body,
        parentKey: formModel.deptCode,
        onChange: async e => {
          formModel.deviceIds = null;
          formModel.team = null;
        }
      }
    },
  },
  {
    label: '设备',
    field: 'deviceIds',
    component: 'JDictSelectTag',
    colProps: { span: 6 },
    componentProps: ({ formModel }) => {
      return {
        showChooseOption: false,
        getPopupContainer: () => document.body,
        dictCode: "mes3_mdm_device where del_flag = 0 and line_code = '" + formModel.lineCode + "' order by sort, device_name, id",
        mode: 'multiple',
      }
    },
  },
  {
    label: '班组',
    field: 'team',
    component: 'JDictSelectTag',
    colProps: { span: 6 },
    componentProps: ({ formModel }) => {
      return {
        showChooseOption: false,
        getPopupContainer: () => document.body,
        dictCode: "mes3_mdm_shift_group where del_flag = 0 and line_code = '" + formModel.lineCode + "' order by sort, group_name, id",
        mode: 'multiple',
      }
    },
  },
  {
    label: '任务名称',
    field: 'taskId',
    component: 'JDictSelectTag',
    colProps: { span: 6 },
    componentProps: ({ formModel }) => {
      return {
        showChooseOption: false,
        showSearch: true,
        getPopupContainer: () => document.body,
        dictCode: "mes3_mdm_task ,task_name, id , del_flag = 0 and line_code = '" + formModel.lineCode + "'",
        mode: 'multiple',
      }
    },
  },
  {
    label: '计划类型',
    field: 'planType',
    component: 'JDictSelectTag',
    colProps: { span: 6 },
    componentProps: () => {
      return {
        showChooseOption: false,
        getPopupContainer: () => document.body,
        dictCode: "mes3_mdm_dict where group_code = 'MES3_PLAN_TYPE' and del_flag = 0 and (item_val < 3 or item_val > 6), item_key, item_val",
        stringToNumber: true,
      }
    },
  },
  {
    label: '任务种类',
    field: 'taskKindCode',
    component: 'JDictSelectTag',
    colProps: { span: 6 },
    componentProps: () => {
      return {
        showChooseOption: false,
        getPopupContainer: () => document.body,
        dictCode: "mes3_mdm_dict where group_code = 'MES3_TASK_KIND' and del_flag = 0, item_key, item_val",
        stringToNumber: true,
      }
    },
  },
  {
    label: '检查项',
    field: 'itemName',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    label: '岗位名称',
    field: 'postId',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "mes3_mdm_post where del_flag = 0 , post_name, id",
      showChooseOption: false,
      showSearch: true
    },
	colProps: { span: 6 },
  },
];

//填报结果列表数据
export const resultColumns: BasicColumn[] = [
  {
    title: '设备名称',
    align: "center",
    ellipsis:false,
    dataIndex: 'deviceId_dictText',
    fixed: 'left'
  },
  {
    title: '任务名称',
    align: "center",
    ellipsis:false,
    dataIndex: 'taskName'
  },
  {
    title: '检查项',
    align: "center",
    ellipsis:false,
    dataIndex: 'itemName'
  },
  {
    title: '计划检查项数',
    align: "center",
    ellipsis:false,
    width: 100,
    dataIndex: 'itemCount'
  },
  {
    title: '实际完成检查项数',
    align: "center",
    ellipsis:false,
    width: 100,
    dataIndex: 'completeCount'
  },
  {
    title: '未完成检查项数',
    align: "center",
    ellipsis:false,
    width: 100,
    dataIndex: 'noCompleteCount',
    customRender: ({ record }) => {
      return record.itemCount - record.completeCount;
    }
  },
  {
    title: '完成率',
    align: "center",
    ellipsis:false,
    width: 100,
    dataIndex: 'completeRate',
    customRender: ({ record }) => {
      return calculatePercentage(record.completeCount, record.itemCount);
    }
  },
  {
    title: '合格数量',
    align: "center",
    ellipsis:false,
    width: 100,
    dataIndex: 'orderResultCompleteCount',
  },
  {
    title: '不合格数量',
    align: "center",
    ellipsis:false,
    width: 100,
    dataIndex: 'orderResultNoCompleteCount',
  },
  {
    title: '合格率',
    align: "center",
    ellipsis:false,
    width: 100,
    dataIndex: 'orderResultRate',
    customRender: ({ record }) => {
      return calculatePercentage(record.orderResultCompleteCount, record.itemCount);
    }
  },
];

//计算百分比
export function calculatePercentage(value, total) {
    if (total == 0 || value == 0 || !value) return '0.00%'; // 避免除以0
    let percentage = (value / total) * 100;
    return percentage.toFixed(2)+'%'; // 保留两位小数
}