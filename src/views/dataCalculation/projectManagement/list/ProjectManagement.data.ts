import dayjs from 'dayjs';
import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import {getYearArr, getMonthArr} from '/@/utils/common/YMUtil';
import { getCurrentDepart } from '/@/utils/common/compUtils';
export const currentDepart = getCurrentDepart();

let dictComponentProps = {
  type: 'select',
  labelField: 'item_key',
  valueField: 'item_val',
  showChooseOption: false,
}

//列表数据
export const columns: BasicColumn[] = [
  {
    title: '项目名称',
    align: "center",
    dataIndex: 'projectName',
    fixed: 'left',
    width: 200,
    slots: { customRender: 'projectNameSlot' },
  },
  {
    title: '立项编号',
    align: "center",
    dataIndex: 'projectCode',
    width: 90,
  },
  {
    title: '项目等级',
    align: "center",
    dataIndex: 'projectLevel_dictText',
    width: 90,
  },
  {
    title: '项目类型',
    align: "center",
    dataIndex: 'projectType_dictText',
    width: 90,
  },
  {
    title: '项目来源',
    align: "center",
    dataIndex: 'projectSource_dictText',
    width: 90,
  },
  {
    title: '主控部门',
    align: "center",
    dataIndex: 'sysOrgId_dictText',
    width: 90,
  },
  {
    title: '项目总监',
    align: "center",
    dataIndex: 'projectBossId_dictText',
    width: 90,
  },
  {
    title: '项目经理',
    align: "center",
    dataIndex: 'projectManageId_dictText',
    width: 90,
  },
];
export const columnsNode: BasicColumn[] = [
  {
    title: '总节点数',
    align: "center",
    dataIndex: 'nodeTotal',
    customRender: ({ record }) => {
      return record.summary.nodeTotal;
    },
    width: 90,
  },
  {
    title: '当前节点完成数',
    align: "center",
    dataIndex: 'nodeFinish',
    customRender: ({ record }) => {
      return record.summary.nodeFinishTotal;
    },
    width: 90,
  },
  {
    title: '当前节点延期数',
    align: "center",
    dataIndex: 'nodeDelay',
    customRender: ({ record }) => {
      return record.summary.nodeDelayTotal;
    },
    width: 90,
  },
  {
    title: '当前行动完成数',
    align: "center",
    dataIndex: 'actionFinish',
    customRender: ({ record }) => {
      return record.summary.actionFinishTotal;
    },
    width: 90,
  },
  {
    title: '当前行动延期数',
    align: "center",
    dataIndex: 'actionDelay',
    customRender: ({ record }) => {
      return record.summary.actionDelayTotal;
    },
    width: 90,
  },
  {
    title: '总行动数',
    align: "center",
    dataIndex: 'actionTotal',
    customRender: ({ record }) => {
      return record.summary.actionTotal;
    },
    width: 90,
  },
  {
    title: '当前节点完成率',
    align: "center",
    dataIndex: 'nodeFinishRate',
    customRender: ({ record }) => {
      return record.summary.nodeFinishRate+'%';
    },
    width: 90,
  },
  {
    title: '当前节点延期率',
    align: "center",
    dataIndex: 'nodeDelayRate',
    customRender: ({ record }) => {
      return record.summary.nodeDelayRate+'%';
    },
    width: 90,
  },
  {
    title: '当前行动完成率',
    align: "center",
    dataIndex: 'actionFinishRate',
    customRender: ({ record }) => {
      return record.summary.actionFinishRate+'%';
    },
    width: 90,
  },
  {
    title: '当前行动延期率',
    align: "center",
    dataIndex: 'actionDelayRate',
    customRender: ({ record }) => {
      return record.summary.actionDelayRate+'%';
    },
    width: 90,
  },
  {
    title: '立项报告',
    align: "center",
    dataIndex: 'projectDocument',
    width: 150,
    slots: { customRender: 'projectDocumentSlot' }
  },
  {
    title: '计划/实际',
    align: "center",
    dataIndex: 'palnOrActual',
    customRender: ({ record }) => {
      return record.weekSummary.weekType;
    },
    width: 90,
  },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: '项目名称',
    field: 'projectName',
    component: 'Input',
  },
  {
    label: '项目等级',
    field: 'projectLevel',
    component: 'JDictSelectTag',
    componentProps: {
      ...dictComponentProps, 
      dictCode: "mes3_mdm_dict where group_code = 'MES3_PROJECT_LEVEL', item_key, item_val",
      mode: "multiple",
    }
  },
  {
    label: '项目类型',
    field: 'projectType',
    component: 'JDictSelectTag',
    componentProps: {
      ...dictComponentProps, 
      dictCode: "mes3_mdm_dict where group_code = 'MES3_PROJECT_TYPE', item_key, item_val",
      mode: "multiple",
    }
  },
  {
    label: '项目来源',
    field: 'projectSource',
    component: 'JDictSelectTag',
    componentProps: {
      ...dictComponentProps, 
      dictCode: "mes3_mdm_dict where group_code = 'MES3_PROJECT_SOURCE', item_key, item_val",
      mode: "multiple",
    }
  },
  {
    label: '主控部门',
    field: 'sysOrgId',
    component: 'KtDepartTreeSelect',
    componentProps: {
      isTreeSelect : true,
      valueField: 'id',
      defaultExpandLevel: 1,
      dropdownMatchSelectWidth: 220,
    },
    defaultValue: currentDepart.id,
  },
  {
    label: '责任部门',
    field: 'leaderDeptIds',
    component: 'KtDepartTreeSelect',
    componentProps: {
      isTreeSelect : true,
      valueField: 'id',
      defaultExpandLevel: 1,
      dropdownMatchSelectWidth: 220,
    },
  },
  {
    label: '年',
    field: 'year',
    component: 'Select',
    componentProps: {
      options: getYearArr(10, 5),
      allowClear: false
    },
    defaultValue: new Date().getFullYear(),
  },
  {
    label: '月',
    field: 'months',
    component: 'Select',
    componentProps: {
      options: getMonthArr(false),
      mode: "multiple",
      maxTagCount: 1,
    },
    defaultValue: dayjs().month() + 1,
  },
  {
    label: '',
    field: 'weekAndMon',
    component: 'CheckboxGroup',
    componentProps: {
      name:'weekAndMon',
      options: [
        { label: '月累', value: 'mon' },
        { label: '周累', value: 'week' },
      ],
    },
    defaultValue:['mon', 'week']
  },
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '项目名称',
    field: 'projectName',
    component: 'Input',
    required: true,
  },
  {
    label: '立项编号',
    field: 'projectCode',
    component: 'Input',
    required: true,
  },
  {
    label: '项目等级',
    field: 'projectLevel',
    component: 'JDictSelectTag',
    componentProps: {...dictComponentProps, dictCode: "mes3_mdm_dict where group_code = 'MES3_PROJECT_LEVEL', item_key, item_val"}
  },
  {
    label: '项目类型',
    field: 'projectType',
    component: 'JDictSelectTag',
    componentProps: {...dictComponentProps, dictCode: "mes3_mdm_dict where group_code = 'MES3_PROJECT_TYPE', item_key, item_val"}
  },
  {
    label: '项目来源',
    field: 'projectSource',
    component: 'JDictSelectTag',
    componentProps: {...dictComponentProps, dictCode: "mes3_mdm_dict where group_code = 'MES3_PROJECT_SOURCE', item_key, item_val"}
  },
  {
    label: '主控部门',
    field: 'sysOrgId',
    component: 'KtDepartTreeSelect',
  },
  {
    label: '项目总监',
    field: 'projectBossId',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'sys_user, realname, id, del_flag = 0',
      type: 'select',
      labelField: 'realname',
      valueField: 'id',
      showChooseOption: false,
      showSearch: true
    },
  },
  {
    label: '项目经理',
    field: 'projectManageId',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'sys_user, realname, id, del_flag = 0',
      type: 'select',
      labelField: 'realname',
      valueField: 'id',
      showChooseOption: false,
      showSearch: true
    },
  },
  {
    label: '立项报告',
    field: 'projectDocument',
    component: 'JUpload',
    componentProps: {
      text: '文件上传',
      maxCount: 5,
      download: true,
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

//项目行动计划列表数据
export const projectActionColumns: BasicColumn[] = [
  {
    title: '项目关键节点排序',
    align: "center",
    dataIndex: 'nodeSort',
    width: '80px',
    slots: { customRender: 'sortSlot' },
    customCell: (record) => {
      return { rowSpan: record.rowSpan };
    },
  },
  {
    title: '项目关键节点',
    align: "center",
    dataIndex: 'nodeName',
    width: '200px',
    slots: { customRender: 'nodeSlot' },
    customCell: (record) => {
      return { rowSpan: record.rowSpan };
    },
  },
  {
    title: '行动计划排序',
    align: "center",
    dataIndex: 'sort',
    width: '80px',
    slots: { customRender: 'sortSlot' },
  },
  {
    title: '行动计划',
    align: "center",
    dataIndex: 'actionName',
    width: '200px',
    slots: { customRender: 'textareaSlot' },
  },
  {
    title: '负责人',
    align: "center",
    width: '100px',
    dataIndex: 'leaderUserId',
    slots: { customRender: 'userSlot' },
  },
  {
    title: '负责部门',
    align: "center",
    width: '130px',
    dataIndex: 'leaderDeptId',
    slots: { customRender: 'deptSlot' },
  },
  {
    title: '提交成果',
    align: "center",
    dataIndex: 'submitResult',
    width: '200px',
    slots: { customRender: 'textareaSlot' },
  },
  {
    title: '计划完成时间',
    align: "center",
    width: '130px',
    dataIndex: 'planCompletionTime',
    slots: { customRender: 'planCompletionTimeSlot' },
  },
  {
    title: '实际完成时间',
    align: "center",
    width: '130px',
    dataIndex: 'realCompletionTime',
    slots: { customRender: 'realCompletionTimeSlot' },
  },
  {
    title: '完成状态',
    align: "center",
    dataIndex: 'actionState',
    width: '120px',
    slots: { customRender: 'actionStateSlot' },
  },
  {
    title: '激励',
    align: "center",
    width: '130px',
    dataIndex: 'reward',
    slots: { customRender: 'rewardSlot' },
  },
  {
    title: '延迟说明',
    align: "center",
    width: '200px',
    dataIndex: 'remark',
    slots: { customRender: 'textareaSlot' },
  },
];