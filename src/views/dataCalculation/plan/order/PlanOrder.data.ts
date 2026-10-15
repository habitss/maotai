import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
//列表数据-通用开头
export const dynamicColumnsStart: BasicColumn[] = [
  {
    title: '岗位',
    align: "center",
    dataIndex: 'postId_dictText'
  },
  {
    title: '设备',
    align: "center",
    dataIndex: 'deviceId_dictText'
  },
  {
    title: '任务名称',
    align: "center",
    dataIndex: 'taskId_dictText',
    width: 300,
  },
  {
    title: '状态',
    align: "center",
    dataIndex: 'executeStatus_dictText',
    customRender: ({record, text}) =>{
      if(record.activeStatus == 1) {
        return '待审核';
      }else if(record.activeStatus == 2){
        return '已取消';
      }
      return text;
    }
  },
];
//列表数据-通用结束
export const dynamicColumnsEnd: BasicColumn[] = [
  {
    title: '任务下发时间',
    align: "center",
    dataIndex: 'realStartTime'
  },
  {
    title: '任务填报时间',
    align: "center",
    dataIndex: 'realEndTime'
  },
  {
    title: '填报人',
    align: "center",
    dataIndex: 'executorId_dictText'
  },
];
//列表数据-过程质量
export const dynamicColumnsRecipeType: BasicColumn[] = [
  {
    title: '触发大类',
    align: "center",
    dataIndex: 'recipeTypeCode_dictText'
  },
  {
    title: '触发细类',
    align: "center",
    dataIndex: 'recipeSubTypeCode_dictText'
  },
];
//列表数据-非计划任务
export const dynamicColumnsCycleType: BasicColumn[] = [
  {
    title: '任务种类',
    align: "center",
    dataIndex: 'taskKindCode_dictText'
  },
  {
    title: '推送频率',
    align: "center",
    dataIndex: 'loopTriggerName'
  },
];
//列表数据-交接班记录
export const dynamicColumnsRecord: BasicColumn[] = [
  {
    title: '交接班类型',
    align: "center",
    dataIndex: 'specialType_dictText'
  },
];

//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: '设备',
    field: 'deviceId',
    component: 'Select',
  },
  {
    label: '任务名称',
    field: 'taskName',
    component: 'Input',
  },
  {
    label: '下发日期',
    field: 'realStartTime',
    component: 'RangeDate',
  },
  {
    label: '填报日期',
    field: 'realEndTime',
    component: 'RangeDate',
  },
  {
    label: '执行状态',
    field: 'executeStatus',
    component: 'JDictSelectTag',
    componentProps: {
      showChooseOption: false,
      getPopupContainer: () => document.body,
      dictCode: "mes3_mdm_dict where group_code = 'MES3_TASK_EXECUTE_STATUS' and del_flag = 0, item_key, item_val",
    }
  },
  {
    label: '触发大类',
    field: 'recipeTypeCode',
    component: 'JDictSelectTag',
    componentProps: {
      showChooseOption: false,
      dictCode: "mes3_mdm_dict where group_code = 'MES3_PLAN_TYPE' and del_flag = 0 and item_val != '4', item_key, item_val",
    },
  },
  {
    label: '触发细类',
    field: 'recipeSubTypeCode',
    component: 'JDictSelectTag',
    componentProps: {
      showChooseOption: false,
      dictCode: "mes3_mdm_dict where group_code = 'MES3_PLAN_TYPE_SUB', item_key, item_val",
      mode: 'multiple',
    },
  },
  {
    label: '是否取消',
    field: 'activeStatus',
    component: 'Select',
    componentProps: {
      options: [
        { label: '未取消', value: 0 },
        { label: '待审核', value: 1 },
        { label: '已取消', value: 2 },
      ],
    },
    defaultValue: 0
  },
  {
    label: '检查项',
    field: 'taskItemName',
    component: 'Input',
  },
];

//填报结果列表数据
export const resultColumns: BasicColumn[] = [
  {
    title: '填报项目/指标',
    align: "center",
    dataIndex: 'itemName',
    fixed: 'left'
  },
  {
    title: '检查地点',
    align: "center",
    dataIndex: 'itemLocation'
  },
  {
    title: '检查频次',
    align: "center",
    dataIndex: 'configRemark'
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
    title: '填报结果',
    align: "center",
    dataIndex: 'result',
    slots: { customRender: 'resultSlots' },
  },
  {
    title: '现场照片',
    align: "center",
    dataIndex: 'resultImg',
    slots: { customRender: 'imgSlots' },
  },
  {
    title: '异常原因',
    align: "center",
    dataIndex: 'remark',
    width: 300,
  },
];