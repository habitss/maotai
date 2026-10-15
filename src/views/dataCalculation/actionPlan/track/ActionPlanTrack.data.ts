import { ref } from 'vue';
import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { queryTreeList } from '@/api/common/api';
import { queryAllTreeData, userList } from '@/views/dataCalculation/actionPlan/problemType/ActionPlanProblemType.api';
import { departListAll} from './ActionPlanTrack.api';
import { useUserStore } from '/@/store/modules/user';
import { list as kpiList } from '/@/views/dataCalculation/kpi/info/KpiInfo.api';
import dayjs from 'dayjs';
import { getCurrentDepart } from '/@/utils/common/compUtils';
export const currentDepart = getCurrentDepart();
// 用户信息
const { userInfo } = useUserStore();
// 完成情况确认下拉框数据
export const finishVerifyStateOptions = [
  { label: '待确认', value: '1', },
  { label: '合格', value: '2', },
  { label: '不合格', value: '3', },
];
// 用户下拉框数据
export let USER_LIST = ref([] as any);
(async function f() {
  USER_LIST.value = await userList({});
})();
//部门树结构数据
let DEPART_TREE = ref([] as any);
(async function f() {
  DEPART_TREE.value = await queryTreeList({ primaryKey: 'key' });
})();
//所有部门数据
let DEPART_LIST= ref([] as any);
(async function f() {
  DEPART_LIST.value = await departListAll({});
})();
//表单原始数据
const rawFormData = ref({} as any);

//列表数据
export const columns: BasicColumn[] = [
  {
    title: '编号',
    align: "center",
    dataIndex: 'id',
    width: 60,
    fixed: 'left',
  },
  {
    title: '提出日期',
    align: "center",
    dataIndex: 'raiseDate',
    width: 90,
    fixed: 'left',
  },
  {
    title: '问题分类',
    align: "center",
    dataIndex: 'problemTypeId_dictText',
    width: 100,
    ellipsis: false,
  },
  {
    title: '提出人',
    align: "center",
    dataIndex: 'putUserName',
    width: 80,
  },
  {
    title: '提出部门',
    align: "center",
    dataIndex: 'putDeptId_dictText',
    width: 80,
  },
  {
    title: '不合格kpi指标',
    align: "center",
    dataIndex: 'pkiInfo_dictText',
    width: 100,
    ellipsis: false,
  },
  {
    title: '任务/问题描述',
    align: "left",
    dataIndex: 'problemDescription',
    width: 300,
    ellipsis: false,
  },
  {
    title: '重要性评价',
    align: "center",
    dataIndex: 'importanceEvaluation_dictText',
    width: 60,
    ellipsis: false,
  },
  {
    title: '行动',
    align: "left",
    dataIndex: 'actionTitle',
    width: 300,
    ellipsis: false,
  },
  {
    title: '责任人',
    align: "center",
    dataIndex: 'dutyUserName',
    width: 80,
  },
  {
    title: '责任部门',
    align: "center",
    dataIndex: 'dutyDeptId_dictText',
    width: 80,
  },
  {
    title: '执行人',
    align: "center",
    dataIndex: 'executeUserName',
    width: 80,
  },
  {
    title: '执行部门',
    align: "center",
    dataIndex: 'executeDeptId_dictText',
    width: 80,
  },
  {
    title: '行动级别',
    align: "center",
    dataIndex: 'actionLevel_dictText',
    width: 80,
  },
  {
    title: '行动性质',
    align: "center",
    dataIndex: 'actionMeasures',
    width: 60,
    customRender: ({ text }) => {
      if (text === '1') {
        text = '预防';
      } else if (text === '2') {
        text = '纠正';
      }
      return text;
    },
  },
  {
    title: '计划完成日期',
    align: "center",
    dataIndex: 'planFinishDate',
    width: 120,
    ellipsis: false,
  },
  {
    title: '实际完成日期',
    align: "center",
    dataIndex: 'practicalFinishDate',
    width: 130,
    ellipsis: false,
    slots: { customRender: 'practicalFinishDate' }
  },
  {
    title: '剩余天数',
    align: "center",
    dataIndex: 'surplusDay',
    width: 60,
    ellipsis: false,
  },
  {
    title: '确认人',
    align: "center",
    dataIndex: 'verifyUserName',
    width: 80,
  },
  {
    title: '确认部门',
    align: "center",
    dataIndex: 'verifyDeptId_dictText',
    width: 80,
  },
  {
    title: '完成情况',
    align: "center",
    dataIndex: 'finishState_dictText',
    width: 100,
    slots: { customRender: 'finishState' },
    customCell(record) {
      let colorObj = { bgColor: '' }
      if (record.finishState == 1) {
        colorObj.bgColor = '#00FF00'
      } else if (record.finishState == 2 || record.finishState == 4) {
        colorObj.bgColor = '#FE0000'
      } else if (record.finishState == 0) {
        colorObj.bgColor = 'yellow'
      } else if (record.finishState == 3) {
        colorObj.bgColor = '#666666'
      } else {
        colorObj.bgColor = ''
      }
      return {
        style: {
          'background-color': colorObj.bgColor
        }
      }
    },
  },
  {
    title: '完成情况确认',
    align: "center",
    dataIndex: 'finishVerifyState',
    width: 120,
    slots: { customRender: 'finishVerifyState' },
    customRender: function({text}){
      return finishVerifyStateOptions.find(item => item.value == text)?.label;
    },
    customCell(record) {
      let colorObj = { bgColor: '' }
      if (record.finishVerifyState === '1') {
        colorObj.bgColor = 'yellow'
      } else if (record.finishVerifyState === '2') {
        colorObj.bgColor = '#00FF00'
      } else if (record.finishVerifyState === '3') {
        colorObj.bgColor = '#FE0000'
      } else {
        colorObj.bgColor = ''
      }
      return {
        style: {
          'background-color': colorObj.bgColor,
        }
      }
    },
  },
  {
    title: '备注',
    align: "center",
    dataIndex: 'remark',
    width: 300,
  },
  {
    title: '文件上传&下载',
    align: "center",
    dataIndex: 'pathText',
    width: 140,
  },
  {
    title: '整改前图片',
    align: "center",
    dataIndex: 'fileObjBefore',
    width: 100,
    slots: { customRender: 'imgSlots' },
  },
  {
    title: '整改后图片',
    align: "center",
    dataIndex: 'fileObj',
    width: 100,
    slots: { customRender: 'imgSlots' },
  },
  {
    title: '延期次数',
    align: "center",
    dataIndex: 'delayCount',
    width: 60,
    ellipsis: false,
  },
  {
    title: '第一次延期备注',
    align: "center",
    dataIndex: 'oneText',
    width: 140,
  },
  {
    title: '第一次延期期限',
    align: "center",
    dataIndex: 'oneTime',
    width: 90,
    ellipsis: false,
  },
  {
    title: '第二次延期备注',
    align: "center",
    dataIndex: 'twoText',
    width: 140,
  },
  {
    title: '第二次延期期限',
    align: "center",
    dataIndex: 'twoTime',
    width: 90,
    ellipsis: false,
  },
  {
    title: '第三次延期备注',
    align: "center",
    dataIndex: 'threeText',
    width: 140,
  },
  {
    title: '第三次延期期限',
    align: "center",
    dataIndex: 'threeTime',
    width: 90,
    ellipsis: false,
  },
  {
    title: '是否进入会议',
    align: "center",
    dataIndex: 'enterMeeting_dictText',
    width: 60,
    ellipsis: false,
  },
  // {
  //   title: '最后更新人',
  //   align: "center",
  //   dataIndex: 'updateBy_dictText',
  //   width: 80,
  //   ellipsis: false,
  // },
  // {
  //   title: '最后更新时间',
  //   align: "center",
  //   dataIndex: 'updateTime',
  //   width: 90,
  //   ellipsis: false,
  // },
  {
    title: '创建人',
    align: "center",
    dataIndex: 'createBy_dictText',
    width: 80,
    ellipsis: false,
  },
  {
    title: '创建时间',
    align: "center",
    dataIndex: 'createTime',
    width: 90,
    ellipsis: false,
  },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: '相关部门',
    field: 'deptIds',
    component: 'JSelectDept',
    componentProps: {
      labelKey: 'title',
      rowKey: 'key',
      checkStrictly: true,
      sync: false,
      defaultExpandLevel: 3,
      showButton: false,
      maxTagCount: 2,
      search: true,
      expandOnSearch: true,
      highlight: true,
    },
    colProps: { span: 6 },
    defaultValue: currentDepart.id,
  },
  {
    label: '提出部门',
    field: 'putDeptId',
    component: 'JSelectDept',
    componentProps: {
      labelKey: 'title',
      rowKey: 'key',
      checkStrictly: true,
      sync: false,
      defaultExpandLevel: 3,
      showButton: false,
      maxTagCount: 2,
      search: true,
      expandOnSearch: true,
      highlight: true,
    },
    colProps: { span: 6 },
  },
  {
    label: '责任部门',
    field: 'dutyDeptId',
    component: 'JSelectDept',
    componentProps: {
      labelKey: 'title',
      rowKey: 'key',
      checkStrictly: true,
      sync: false,
      defaultExpandLevel: 3,
      showButton: false,
      maxTagCount: 2,
      search: true,
      expandOnSearch: true,
      highlight: true,
    },
    colProps: { span: 6 },
  },
  {
    label: '确认部门',
    field: 'verifyDeptId',
    component: 'JSelectDept',
    componentProps: {
      labelKey: 'title',
      rowKey: 'key',
      checkStrictly: true,
      sync: false,
      defaultExpandLevel: 3,
      showButton: false,
      maxTagCount: 2,
      search: true,
      expandOnSearch: true,
      highlight: true,
    },
    colProps: { span: 6 },
  },
  {
    label: '相关人',
    field: 'userIds',
    component: 'Select',
    componentProps: {
      options: USER_LIST,
      fieldNames: { label: 'realname', value: 'id' },
      maxTagCount: 2,
      mode: 'multiple',
      showSearch: true,
      allowClear: true,
      filterOption,
    },
    colProps: { span: 6 },
  },
  {
    label: '提出人',
    field: 'putUserId',
    component: 'Select',
    componentProps: {
      options: USER_LIST,
      fieldNames: { label: 'realname', value: 'id' },
      maxTagCount: 2,
      mode: 'multiple',
      showSearch: true,
      allowClear: true,
      filterOption,
    },
    colProps: { span: 6 },
  },
  {
    label: '责任人',
    field: 'dutyUserId',
    component: 'Select',
    componentProps: {
      options: USER_LIST,
      fieldNames: { label: 'realname', value: 'id' },
      maxTagCount: 2,
      mode: 'multiple',
      showSearch: true,
      allowClear: true,
      filterOption,
    },
    colProps: { span: 6 },
  },
  {
    label: '确认人',
    field: 'verifyUserId',
    component: 'Select',
    componentProps: {
      options: USER_LIST,
      fieldNames: { label: 'realname', value: 'id' },
      maxTagCount: 2,
      mode: 'multiple',
      showSearch: true,
      allowClear: true,
      filterOption,
    },
    colProps: { span: 6 },
  },
  {
    label: '执行人',
    field: 'executeUserId',
    component: 'Select',
    componentProps: {
      options: USER_LIST,
      fieldNames: { label: 'realname', value: 'id' },
      maxTagCount: 2,
      mode: 'multiple',
      showSearch: true,
      allowClear: true,
      filterOption,
    },
    colProps: { span: 6 },
  },
  {
    label: '剩余天数',
    field: 'surplusDay',
    component: 'InputNumber',
    colProps: { span: 6 },
  },
  {
    label: '创建时间',
    field: 'createTime',
    component: 'RangeDate',
    componentProps: {
      format: 'YYYY-MM-DD',
    },
    colProps: { span: 6 },
  },
  {
    label: '提出日期',
    field: 'raiseDate',
    component: 'RangeDate',
    componentProps: {
      format: 'YYYY-MM-DD',
    },
    colProps: { span: 6 },
  },
  {
    label: '计划完成日期',
    field: 'planFinishDate',
    component: 'RangeDate',
    componentProps: {
      format: 'YYYY-MM-DD',
    },
    colProps: { span: 6 },
  },
  {
    label: '实际完成日期',
    field: 'practicalFinishDate',
    component: 'RangeDate',
    componentProps: {
      format: 'YYYY-MM-DD',
    },
    colProps: { span: 6 },
  },
  {
    label: '行动级别',
    field: 'actionLevel',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "mes3_mdm_dict where group_code = 'MES3_ACTION_PLAN_LEVEL', item_key, item_val",
      type: 'select',
      labelField: 'item_key',
      valueField: 'item_val',
    },
    colProps: { span: 6 },
  },
  {
    label: '行动性质',
    field: 'actionMeasures',
    component: 'Select',
    componentProps: {
      options: [
        {
          label: '预防',
          value: 1,
        },
        {
          label: '纠正',
          value: 2,
        },
      ]
    },
    colProps: { span: 6 },
  },
  {
    label: '完成情况',
    field: 'finishState',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "mes3_mdm_dict where group_code = 'MES3_ACTION_PLAN_FINISH', item_key, item_val",
      type: 'select',
      labelField: 'item_key',
      valueField: 'item_val',
      mode: 'multiple',
      maxTagCount: 2
    },
    defaultValue: ['0', '2'],
    colProps: { span: 6 },
  },
  {
    label: '完成情况确认',
    field: 'finishVerifyState',
    component: 'Select',
    componentProps: {
      options: finishVerifyStateOptions,
    },
    colProps: { span: 6 },
  },
  {
    label: '问题来源',
    field: 'problemTypeId',
    component: 'ApiTreeSelect',
    componentProps: {
      api: queryAllTreeData,
      params: { async: false, pcode: '0', excludeDisabled: true },
      fieldNames: {children: 'childList', label: 'problemName', value: 'id',},
      showSearch: true,
      treeNodeFilterProp: 'problemName',
      multiple: true,
    },
    colProps: { span: 6 },
  },
  {
    label: '进入会议',
    field: 'enterMeeting',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "mes3_mdm_dict where group_code = 'MES3_ACTION_PLAN_MEETING', item_key, item_val",
      type: 'select',
      labelField: 'item_key',
      valueField: 'item_val',
    },
    colProps: { span: 6 },
  },
  {
    label: '编号',
    field: 'id',
    component: 'JRangeNumber',
    colProps: { span: 6 },
  },
];


//表单数据
export const fiexdFormSchema: FormSchema[] = [
  {
    label: '提出日期',
    field: 'raiseDate',
    component: 'DatePicker',
    componentProps: {
      placeholder:' ',
      valueFormat: 'YYYY-MM-DD'
    },
    dynamicDisabled: handleDisabled,
    required: true,
  },
  {
    label: '不合格kpi指标',
    field: 'pkiInfo',
    component: 'ApiTreeSelect',
    componentProps: {
      api: kpiList,
      fieldNames: {children:'children', label:'kpiName', value: 'kpiCode' },
      resultField:'records',
      showSearch: true,
      treeNodeFilterProp: 'kpiName',
      params:{pageSize: 9999},
    },
    dynamicDisabled: handleDisabled,
  },
  {
    label: '重要性评价',
    field: 'importanceEvaluation',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "mes3_mdm_dict where group_code = 'MES3_ACTION_PLAN_IMPORTANCE_EVALUATION', item_key, item_val",
      type: 'select',
      labelField: 'item_key',
      valueField: 'item_val',
    },
    dynamicDisabled: handleDisabled,
  },
  {
    label: '问题分类',
    field: 'problemTypeId',
    component: 'ApiTreeSelect',
    componentProps: {
      api: queryAllTreeData,
      params: { async: false, pcode: '0' },
      fieldNames: {children: 'childList', label: 'problemName', value: 'id',},
      showSearch: true,
      treeNodeFilterProp: 'problemName',
    },
    dynamicDisabled: handleDisabled,
    required: true,
  },
  {
    label: '任务/问题描述',
    field: 'problemDescription',
    component: 'InputTextArea',
    colProps: { span: 24 },
    dynamicDisabled: handleDisabled,
    required: true,
  },
  {
    label: '行动',
    field: 'actionTitle',
    component: 'InputTextArea',
    colProps: { span: 24 },
    dynamicDisabled: handleDisabled,
    required: true,
  },
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '提出人',
    field: 'putUserId',
    component: 'Select',
    componentProps: ({ formActionType, formModel }) => {
      return {
        options: USER_LIST.value,
        fieldNames: { label: 'realname', value: 'id' },
        maxTagCount: 2,
        showSearch: true,
        allowClear: true,
        filterOption,
        onChange:(e) => handleUserIdChange(formModel, formActionType, 'put', e, 'DeptId')
      }
    },
    dynamicDisabled: handleDisabled,
    required: true,
  },
  {
    label: '提出部门',
    field: 'putDeptId',
    component: 'Select',
    dynamicDisabled: handleDisabled,
    required: true,
  },
  {
    label: '提出人激励',
    field: 'putKind',
    component: 'Input',
    dynamicDisabled: handleDisabled,
  },
  {
    label: '责任人',
    field: 'dutyUserId',
    component: 'Select',
    componentProps: ({ formActionType, formModel }) => {
      return {
        options: USER_LIST.value,
        fieldNames: { label: 'realname', value: 'id' },
        maxTagCount: 2,
        showSearch: true,
        allowClear: true,
        filterOption,
        onChange:(e) => handleUserIdChange(formModel, formActionType, 'duty', e, 'DeptId')
      }
    },
    dynamicDisabled: handleDisabled,
    required: true,
  },
  {
    label: '责任部门',
    field: 'dutyDeptId',
    component: 'Select',
    componentProps: ({ formModel }) => {
      return {
        onChange:(e) => handleDutyDeptChange(formModel, e)
      }
    },
    dynamicDisabled: handleDisabled,
    required: true,
  },
  {
    label: '责任人激励',
    field: 'dutyKind',
    component: 'Input',
    dynamicDisabled: handleDisabled,
  },
  {
    label: '执行人',
    field: 'executeUserId',
    component: 'Select',
    componentProps: ({ formActionType, formModel }) => {
      return {
        options: USER_LIST.value,
        fieldNames: { label: 'realname', value: 'id' },
        maxTagCount: 2,
        showSearch: true,
        allowClear: true,
        filterOption,
        onChange:(e) => handleUserIdChange(formModel, formActionType, 'execute', e, 'DeptId')
      }
    },
    dynamicDisabled: handleDisabled,
  },
  {
    label: '执行部门',
    field: 'executeDeptId',
    component: 'Select',
    dynamicDisabled: handleDisabled,
  },
  {
    label: '执行人激励',
    field: 'executeKind',
    component: 'Input',
    dynamicDisabled: handleDisabled,
  },
  {
    label: '确认人',
    field: 'verifyUserId',
    component: 'Select',
    componentProps: ({ formActionType, formModel }) => {
      return {
        options: USER_LIST.value,
        fieldNames: { label: 'realname', value: 'id' },
        maxTagCount: 2,
        showSearch: true,
        allowClear: true,
        filterOption,
        onChange:(e) => handleUserIdChange(formModel, formActionType, 'verify', e, 'DeptId')
      }
    },
    dynamicDisabled: handleDisabled,
    required: true,
  },
  {
    label: '确认部门',
    field: 'verifyDeptId',
    component: 'Select',
    dynamicDisabled: handleDisabled,
    required: true,
  },
  {
    label: '确认人激励',
    field: 'verifyKind',
    component: 'Input',
    dynamicDisabled: handleDisabled,
  },
  {
    label: '行动级别',
    field: 'actionLevel',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "mes3_mdm_dict where group_code = 'MES3_ACTION_PLAN_LEVEL', item_key, item_val",
      type: 'select',
      labelField: 'item_key',
      valueField: 'item_val',
    },
    dynamicDisabled: handleDisabled,
    required: true,
  },
  {
    label: '行动性质',
    field: 'actionMeasures',
    component: 'Select',
    componentProps: {
      options: [
        { label: '预防', value: '1', },
        { label: '纠正', value: '2', },
      ]
    },
    dynamicDisabled: handleDisabled,
  },
  {
    label: '进入会议',
    field: 'enterMeeting',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "mes3_mdm_dict where group_code = 'MES3_ACTION_PLAN_MEETING', item_key, item_val",
      type: 'select',
      labelField: 'item_key',
      valueField: 'item_val',
    },
    dynamicDisabled: handleDisabled,
    required: true,
  },
  {
    label: '计划完成日期',
    field: 'planFinishDate',
    component: 'DatePicker',
    componentProps: ({ formModel }) => {
      return {
        placeholder:' ',
        valueFormat: 'YYYY-MM-DD',
        options: finishVerifyStateOptions,
        onChange:e=>handlePlanDateChange(e, formModel, 'planFinishDate')
      };
    },
    dynamicDisabled: handleDisabled,
    required: true,
  },
  {
    label: '实际完成日期',
    field: 'practicalFinishDate',
    component: 'DatePicker',
    componentProps: ({ formModel }) => {
      return {
        placeholder:' ',
        valueFormat: 'YYYY-MM-DD',
        options: finishVerifyStateOptions,
        onChange:e=>handlePlanDateChange(e, formModel, 'practicalFinishDate')
      };
    },
    dynamicDisabled: handleDisabled,
  },
  {
    label: '完成情况',
    field: 'finishState',
    component: 'JDictSelectTag',
    slot: 'finishState',
    dynamicDisabled: handleDisabled,
  },
  {
    label: '完成情况确认',
    field: 'finishVerifyState',
    component: 'Select',
    componentProps: ({ formModel }) => {
      return {
        options: finishVerifyStateOptions,
        onChange:e => handleFinishVerifyStateChange(e, formModel)
      };
    },
    dynamicDisabled: function({ values }){
      //判断是否为确认人或提出人
      if(userInfo?.id == values.verifyUserId || userInfo?.id == values.putUserId){
        //判断是否填写了计划完成日期和实际完成日期，如果没有填写则禁用此字段
        if(values.planFinishDate && values.practicalFinishDate){
          return false;
        }
      }
      return true;
    },
  },
  {
    label: '剩余天数',
    field: 'surplusDay',
    component: 'InputNumber',
    componentProps: {
      disabled: true,
    },
  },
  {
    label: '延期次数',
    field: 'delayCount',
    component: 'InputNumber',
    dynamicDisabled: true,
  },
  {
    label: '第一次延期期限',
    field: 'oneTime',
    component: 'DatePicker',
    componentProps: ({ formModel }) => {
      return {
        placeholder:' ',
        valueFormat: 'YYYY-MM-DD',
        onChange: e => handlePostponeChange(1, e, formModel)
      }
    },
    dynamicDisabled: function({ values }){
      return values.delayCount>0;
    },
  },
  {
    label: '第一次延期备注',
    field: 'oneText',
    component: 'InputTextArea',
    colProps: { span: 16 },
    dynamicDisabled: function({ values }){
      return values.delayCount>0;
    },
  },
  {
    label: '第二次延期期限',
    field: 'twoTime',
    component: 'DatePicker',
    componentProps: ({ formModel }) => {
      return {
        placeholder:' ',
        valueFormat: 'YYYY-MM-DD',
        onChange: e => handlePostponeChange(2, e, formModel)
      }
    },
    dynamicDisabled: function({ values }){
      return values.delayCount>1 || !values.oneTime;
    },
  },
  {
    label: '第二次延期备注',
    field: 'twoText',
    component: 'InputTextArea',
    colProps: { span: 16 },
    dynamicDisabled: function({ values }){
      return values.delayCount>1 || !values.oneTime;
    },
  },
  {
    label: '第三次延期期限',
    field: 'threeTime',
    component: 'DatePicker',
    componentProps: {
      placeholder:' ',
      valueFormat: 'YYYY-MM-DD'
    },
    dynamicDisabled: function({ values }){
      return values.delayCount>2 || !values.twoTime;
    },
  },
  {
    label: '第三次延期备注',
    field: 'threeText',
    component: 'InputTextArea',
    colProps: { span: 16 },
    dynamicDisabled: function({ values }){
      return values.delayCount>2 || !values.twoTime;
    },
  },
  {
    label: '备注',
    field: 'remark',
    component: 'InputTextArea',
    colProps: { span: 24 },
  },
  {
    label: '文件上传&下载',
    field: 'pathText',
    component: 'JUpload',
    componentProps: {
      //是否显示选择按钮
      text: '文件上传',
      //最大上传数
      maxCount: 5,
      //是否显示下载按钮
      download: true,
    },
    colProps: { span: 24 },
  },
  {
    label: '整改前图片',
    field: 'fileObjBefore',
    component: 'JImageUploadV2',
    componentProps: {
      //按钮显示文字
      text:'图片上传',
      //支持两种基本样式picture和picture-card
      listType:'picture-card',
      //用于控制文件上传的业务路径,默认temp
      bizPath:'temp',
      //是否禁用
      disabled:false,
      //最大上传数量
      fileMax: 5,
    },
    colProps: { span: 24 },
  },
  {
    label: '整改后图片',
    field: 'fileObj',
    component: 'JImageUploadV2',
    componentProps: {
      //按钮显示文字
      text:'图片上传',
      //支持两种基本样式picture和picture-card
      listType:'picture-card',
      //用于控制文件上传的业务路径,默认temp
      bizPath:'temp',
      //是否禁用
      disabled:false,
      //最大上传数量
      fileMax: 5,
    },
    colProps: { span: 24 },
  },
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false
  },
  {
    label: '',
    field: 'fiveWhyMeasuresId',
    component: 'Input',
    show: false
  },
  {
    label: '',
    field: 'dataId',
    component: 'Input',
    show: false
  },
  {
    label: '',
    field: 'putUserName',
    component: 'Input',
    show: false
  },
  {
    label: '',
    field: 'dutyUserName',
    component: 'Input',
    show: false
  },
  {
    label: '',
    field: 'executeUserName',
    component: 'Input',
    show: false
  },
  {
    label: '',
    field: 'verifyUserName',
    component: 'Input',
    show: false
  },
];

/**
* 流程表单调用这个方法获取formSchema
* @param param
*/
export function getBpmFormSchema(_formData): FormSchema[] {
  // 默认和原始表单保持一致 如果流程中配置了权限数据，这里需要单独处理formSchema
  return formSchema;
}

//行动计划录入-列表配置
export const entryColumns: BasicColumn[] = [
  {
    title: '行动',
    align: "center",
    dataIndex: 'actionTitle',
    width: 300,
  },
  {
    title: '提出人',
    align: "center",
    dataIndex: 'putUserId',
    width: 120,
  },
  {
    title: '提出部门',
    align: "center",
    dataIndex: 'putDeptId',
    width: 120,
  },
  {
    title: '责任人',
    align: "center",
    dataIndex: 'dutyUserId',
    width: 120,
  },
  {
    title: '责任部门',
    align: "center",
    dataIndex: 'dutyDeptId',
    width: 120,
  },
  {
    title: '执行人',
    align: "center",
    dataIndex: 'executeUserId',
    width: 120,
  },
  {
    title: '执行部门',
    align: "center",
    dataIndex: 'executeDeptId',
    width: 120,
  },
  {
    title: '确认人',
    align: "center",
    dataIndex: 'verifyUserId',
    width: 120,
  },
  {
    title: '确认部门',
    align: "center",
    dataIndex: 'verifyDeptId',
    width: 120,
  },
  {
    title: '计划完成日期',
    align: "center",
    dataIndex: 'planFinishDate',
    width: 130,
  },
  {
    title: '行动级别',
    align: "center",
    dataIndex: 'actionLevel',
    width: 100,
  },
  {
    title: '行动性质',
    align: "center",
    dataIndex: 'actionMeasures',
    width: 90,
  },
  {
    title: '进入会议',
    align: "center",
    dataIndex: 'enterMeeting',
    width: 130,
  },
  {
    title: '是否进入行动计划',
    align: "center",
    dataIndex: 'status',
    width: 140,
    ifShow: false,
  },
];

//行动计划录入-列表配置2
export const entryLeftColumns: BasicColumn[] = [
  {
    title: '提出日期',
    align: "center",
    dataIndex: 'raiseDate',
    width: 140,
    fixed: 'left',
  },
  {
    title: '问题分类',
    align: "center",
    dataIndex: 'problemTypeId',
    width: 140,
    ellipsis: false,
  },
  {
    title: '不合格kpi指标',
    align: "center",
    dataIndex: 'pkiInfo',
    width: 140,
    ellipsis: false,
  },
  {
    title: '重要性评价',
    align: "center",
    dataIndex: 'importanceEvaluation',
    width: 140,
    ellipsis: false,
  },
  {
    title: '任务/问题描述',
    align: "left",
    dataIndex: 'problemDescription',
    width: 300,
    ellipsis: false,
  },
];

//行动计划录入-定义必填验证字段
export const rules = {
  raiseDate: '请选择提出日期',
  problemTypeId: '请选择问题分类',
  // pkiInfo: '请输不合格kpi指标',
  // importanceEvaluation: '请选择重要性评价',
  problemDescription: '请输入任务/问题描述',
  'problemDescription-min-length': {length: 8, message: '任务/问题描述长度不能少于8个字符'},
  actionTitle: '请输入行动',
  'actionTitle-min-length': {length: 8, message: '行动长度不能少于8个字符'},
  putUserId: '请选择提出人',
  putDeptId: '请选择提出部门',
  dutyUserId: '请选择责任人',
  dutyDeptId: '请选择责任部门',
  executeDeptId: '请选择执行部门',
  verifyUserId: '请选择确认人',
  verifyDeptId: '请选择确认部门',
  planFinishDate: '请选择计划完成日期',
  actionLevel: '请选择行动级别',
  enterMeeting: '请选择进入会议',
};

//下拉选择过滤方法
export function filterOption(input, option) {
  return option.realname.toLowerCase().indexOf(input.toLowerCase()) >= 0;
};

export function setRawFormData(value){
  rawFormData.value = value;
}

//表单判断禁用方法
function handleDisabled ({field}){
  const values = rawFormData.value;
  if(values.putUserId == userInfo?.id
    ||values.dutyUserId == userInfo?.id
    ||values.executeUserId == userInfo?.id
    ||values.verifyUserId == userInfo?.id
  ){
    return false;
  }
  //暂时屏蔽权限控制
  // //提出人
  // if(values.putUserId == userInfo?.id){
  //   return false;
  // }
  // //责任人
  // if(values.dutyUserId == userInfo?.id){
  //   if(field == 'dutyDeptId' || field == 'executeDeptId' || field == 'executeUserId' || field == 'practicalFinishDate'){
  //     return false;
  //   }
  // }
  // //执行人
  // if(values.executeUserId == userInfo?.id){
  //   if(field == 'executeDeptId' || field == 'practicalFinishDate'){
  //     return false;
  //   }
  // }
  // //确认人
  // if(values.verifyUserId == userInfo?.id){
  //   values.isVerifyUser = true;
  //   if(field == 'finishState' || field == 'finishVerifyState'){
  //     return false;
  //   }
  // }
  return true;
}

export function handlePlanDateChange(value, formData, field){
  formData[field] = value;
  // 计划完成日期不为空 且 实际完成日期不为空 且 未确认 则 自动设置为 待确认状态
  if(formData.planFinishDate && formData.practicalFinishDate){
    formData.finishVerifyState = '1';
  } else {
    if(formData.planFinishDate && !formData.practicalFinishDate){
      formData.finishVerifyState = '';
    }
  }
  
  //非取消状态
  if(formData.finishState != '3'){
    //计划完成时间
    const planFinishDate = dayjs(formData.planFinishDate).endOf('day');
    //当前时间
    const now = dayjs().endOf('day');
    if(formData.practicalFinishDate){
      //实际完成时间
      const practicalFinishDate = dayjs(formData.practicalFinishDate).endOf('day');
      //计算计划时间，实际完成时间相差天数
      const diffDay = planFinishDate.diff(practicalFinishDate, 'day');
      if(diffDay < 0){
        //计划完成时间早于实际完成时间，完成情况为延迟
        formData.finishState = '2';
      } else {
        formData.finishState = '0';
      }
    } else {
      //判断计划完成时间
      const diffDay = planFinishDate.diff(now, 'day');
      if(diffDay < 0){
        //计划完成时间早于当前日期，完成情况为延迟
        formData.finishState = '2';
      }else{
        //计划完成时间晚于等于当前日期，完成情况为进行中
        formData.finishState = '0';
      }
    }
  }
}

//确认状态改变事件
export async function handleFinishVerifyStateChange(e, formModel){
  if(e == '2'){
    //合格操作
    if(formModel.finishState == '0'){
      //计划完成时间
      const planFinishDate = dayjs(formModel.planFinishDate).endOf('day');
      //实际完成时间
      const practicalFinishDate = dayjs(formModel.practicalFinishDate).endOf('day');
      const diffDay = practicalFinishDate.diff(planFinishDate, 'day');
      //实际完成日期大于计划完成时间，为延迟完成
      if(diffDay > 0){
        formModel.finishState = '4';
      }else {
        // 完成情况确认为合格，完成情况为进行中时，改为已完成
        formModel.finishState = '1';
      }
    }else if(formModel.finishState == '2'){
      // 完成情况确认为合格，完成情况为进行中时，改为延迟完成
      formModel.finishState = '4';
    }
  }else if(e == '3'){
    //不合格清空实际完成日期
    formModel.practicalFinishDate = null;
    //清空时调用计划完成日期改变事件
    await handlePlanDateChange(formModel.planFinishDate, formModel, 'planFinishDate')
    //重新设置一下完成情况确认为3
    formModel.finishVerifyState = '3';
  }else{
    //清空时调用计划完成日期改变事件
    handlePlanDateChange(formModel.planFinishDate, formModel, 'planFinishDate')
  }
}

/**
 * 表单用户改变事件
 * @param formModel 表单数据
 * @param formActionType 表单操作对象
 * @param prefix 前缀
 * @param userId 用户编码
 * @param suffix 后缀
 */
export async function handleUserIdChange(formModel, formActionType, prefix, userId, suffix){
  suffix = suffix || 'DeptId';
  //获取表单操作对象
  const { updateSchema }= formActionType;
  //获取单位选择下拉数据
  const departOptions = getDeptOptions(userId);
  //如果只有一个部门则选中该部门，否则不选中任何单位
  let departId = undefined;
  if(departOptions.length == 1){
    departId = departOptions[0].value;
  }
  formModel[prefix+suffix] = departId;

  //需要更改的数据
  let updateArr = [{
    field: prefix+suffix,
    componentProps: ({ formModel }) => {
      return {
        options: departOptions,
        showSearch: true,
        optionFilterProp: 'label',
      }
    },
  }] as any;

  //判断是否为责任人， 则修改执行人相关数据
  if(prefix === 'duty'){
    formModel['executeUserId'] = userId;
    formModel['executeDeptId'] = departId;

    updateArr[0].componentProps = ({ formModel }) => {
      return {
        options: departOptions,
        showSearch: true,
        optionFilterProp: 'label',
        onChange:(e) => handleDutyDeptChange(formModel, e)
      }
    };

    updateArr.push({
      field: 'executeDeptId',
      componentProps: {
        options: departOptions,
        optionFilterProp: 'label',
        showSearch: true,
      },
    });
  }

  //更新表单架构，动态设置单位选择下拉数据
  await updateSchema(updateArr);
  //根据用户id获取用户名称，并更新表单架构
  formModel[prefix+'UserName'] = USER_LIST.value.find((item) => item.id === userId).realname;
}

//根据用户id获取用户负责部门信息，并处理单位选择下拉数据
export function getDeptOptions(userId){
  if(!userId){
    return [];
  }
  //获取用户信息
  const findUser = USER_LIST.value.find(user => user.id === userId);
  //获取用户负责部门
  const userDepart = findUser && findUser.orgCodeTxt ? findUser.orgCodeTxt.split(',') : [];
  //处理单位选择下拉数据
  let deptOptions = [] as any;
  for(const item of userDepart){
    let findDepart = DEPART_LIST.value.find(depart => depart.departName == item);
    if(findDepart){
      //判断下拉中是否已存在，不存在则新增该部门信息到下拉中
      const deptOption = deptOptions.find(dept => dept.key == findDepart.id);
      if(!deptOption){
        deptOptions.push({
          key: findDepart.id,
          label: findDepart.departName,
          value :findDepart.id,
          orgCode: findDepart.orgCode
        });
      }
    }
  }
  deptOptions.sort((a, b) => {
    if (a.orgCode < b.orgCode) {
      return -1;
    } else if (a.orgCode > b.orgCode) {
      return 1;
    } else {
      return 0;
    }
  });
  return deptOptions;
}

//表单责任部门改变事件
export function handleDutyDeptChange(formModel, value){
  //且责任人和执行人一致，则改变执行部门
  if(formModel['executeUserId'] === formModel['dutyUserId']){
    formModel['executeDeptId'] = value;
  }
}

/**
 * 延期字段改变事件
 * @param type 类型1：一次延期，2：二次延期，3：三次延期
 * @param value 值
 * @param formModel 表单数据 
 */
function handlePostponeChange(type, value, formModel){
  if(!value){
    if(type === 1){
      formModel.twoTime = null;
      formModel.twoText = null;
      handlePostponeChange(2, value, formModel)
    } else if(type === 2){
      formModel.threeTime = null;
      formModel.threeText = null;
    }
  }
}



//列表用户选择事件
export function handleUserChange(value, columnKey, record, isInit){
  //获取前缀
  const prefix = columnKey.substr(0, columnKey.indexOf('UserId'));
  record[prefix+'DeptId'] = isInit ? null : record[prefix+'DeptId'];
  const deptOptions = getDeptOptions(value);
  //设置部门下拉数据
  record[prefix+'DeptId_options'] = deptOptions;
  //如果只有一个部门则直接选中该部门
  let deptId = '';
  if(deptOptions.length == 1){
    record[prefix+'DeptId'] = deptOptions[0].value;
    deptId = deptOptions[0].value;

  }
  //判断是否为责任人， 则修改执行人相关数据
  if(prefix === 'duty' && isInit === true){
    record['executeUserId'] = value;
    record['executeDeptId'] = deptId;
    record['executeDeptId_options'] = deptOptions;
  }
}

//部门选择事件
export function handleDeptChange(columnKey, record){
  //获取前缀
  const prefix = columnKey.substr(0, columnKey.indexOf('DeptId'));
  //如果是责任部门，且责任人和执行人一致，则改变执行部门
  if(prefix === 'duty' && record['executeUserId'] === record['dutyUserId']){
    record['executeDeptId'] = record['dutyDeptId'];
    record['executeDeptId_options'] = record['dutyDeptId_options'];
  }
}