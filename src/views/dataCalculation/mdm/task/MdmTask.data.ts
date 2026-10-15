import { InputNumber } from 'ant-design-vue';
import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { task } from '/@/views/dataCalculation/pi/taskParameter/TaskParameter.api';
import { CheckItemTypeEnum } from '/@/enums/planEnum';
import { TaskTypeEnum } from '/@/enums/taskEnum';
import { ref } from 'vue';
export { CheckItemTypeEnum };
export const taskCycleObj = {
  61: 0,
  62: 4,
  63: 2,
  64: 1,
}
//1-28天
let monDayOptions = ref(
  Array.from({ length: 28 }, (_, i) => ({
    value: i + 1+'',
    label: `${i + 1}号推送`,
    disabled: false
  }))
)
//0-23点
let hourOptions = ref(
  Array.from({ length: 24 }, (_, i) => ({
    value: i + '',
    label: `${i}点`,
    disabled: false
  }))
)
const dynamicColumns = ['pushDate_hour', 'pushDate_day', 'pushDate_week', 'pushDate1', 'pushDate2', 'pushDate3', 'pushDate4'];
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
    title: '是否关联检查项',
    align: 'center',
    dataIndex: 'haveItem',
    width: 70,
    customCell(record) {
      return {
        style: {
          'color': record.haveItem == 1 ? 'green' : '#FE0000'
        }
      }
    },
    customRender: ({ text }) => {
      return text ? '是' : '否';
    },
  },
  {
    title: '任务名',
    align: "center",
    dataIndex: 'taskName',
    width: 300
  },
  {
    title: '任务种类',
    align: "center",
    dataIndex: 'taskKindCode_dictText'
  },
  {
    title: '触发类型',
    align: "center",
    dataIndex: 'recipeTypeCode_dictText'
  },
  {
    title: '触发细类',
    align: "center",
    dataIndex: 'recipeSubTypeCode_dictText'
  },
  {
    title: '推送频率',
    align: "center",
    dataIndex: 'loopTriggerName'
  },
  {
    title: '是否启用',
    align: "center",
    dataIndex: 'state',
    customCell(record) {
      return {
        style: {
          'color': record.state == 1 ? 'green' : '#FE0000'
        }
      }
    },
    customRender: ({ text }) => {
      return text ? '是' : '否';
    },
  },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: '任务名',
    field: 'taskName',
    component: 'Input',
  },
  {
    label: '任务种类',
    field: 'taskKindCode',
    component: 'JDictSelectTag',
    componentProps: () => {
      return {
        showChooseOption: false,
        dictCode: "mes3_mdm_dict where group_code = 'MES3_TASK_KIND' and del_flag = 0 order by sort, item_key, item_val",
      }
    },
  },
  {
    label: '触发类型',
    field: 'recipeTypeCode',
    component: 'JDictSelectTag',
    componentProps: {
      showChooseOption: false,
      dictCode: "mes3_mdm_dict where group_code = 'MES3_PLAN_TYPE' and del_flag = 0 order by sort, item_key, item_val",
    },
  },
  {
    label: '推送频率',
    field: 'loopTriggerId',
    component: 'ApiSelect',
    componentProps: {
      api: task,
      showChooseOption: true,
      params: {
        groupId: 8
      }
    },
  },
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '部门',
    field: 'deptCode_dictText',
    component: 'Input',
    componentProps: {
      disabled: true,
    },
    required: true,
    show: ({values}) => { 
      return !values.isCopy
    },
  },
  {
    label: '产线',
    field: 'lineCode_dictText',
    component: 'Input',
    componentProps: {
      disabled: true,
    },
    required: true,
    show: ({values}) => { 
      return !values.isCopy
    },
  },
  {
    label: '设备',
    field: 'deviceId_dictText',
    component: 'Input',
    componentProps: {
      disabled: true,
    },
    required: true,
    show: ({values}) => { 
      return !values.isCopy
    },
  },
  {
    label: '部门',
    field: 'deptCode',
    component: 'KtDepartSelect',
    componentProps: ({ formModel }) => {
      return {
        selectType: 'depart',
        onChange: async e => {
          formModel.lineCode = null;
          formModel.deviceId = null;
          formModel.startTriggerPoint = null;
          formModel.endTriggerPoint = null;
        }
      }
    },
    required: true,
    show: ({values}) => { 
      return values.isCopy
    },
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
        onChange: async e => {
          formModel.deviceId = null;
          formModel.startTriggerPoint = null;
          formModel.endTriggerPoint = null;
        }
      }
    },
    required: true,
    show: ({values}) => { 
      return values.isCopy
    },
  },
  {
    label: '设备',
    field: 'deviceId',
    component: 'JDictSelectTag',
    componentProps: ({ formModel }) => {
      return {
        showChooseOption: false,
        dictCode: "mes3_mdm_device where del_flag = 0 and line_code = '" + formModel.lineCode + "' order by sort, device_name, id",
        stringToNumber: true,
        onChange: async () => {
          formModel.startTriggerPoint = null;
          formModel.endTriggerPoint = null;
        }
      }
    },
    required: true,
    show: ({values}) => { 
      return values.isCopy
    },
  },
  {
    label: '是否启用',
    field: 'state',
    component: 'Switch',
    componentProps: {
      unCheckedChildren:'否',
      unCheckedValue: 0,
      checkedChildren:'是',
      checkedValue: 1,
    },
    defaultValue: 1
  },
  {
    label: '任务类型',
    field: 'checkTaskType',
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
    label: '任务细类',
    field: 'taskTypeCode',
    component: 'JDictSelectTag',
    componentProps: {
      showChooseOption: false,
      dictCode: "mes3_mdm_dict where del_flag = 0 and group_code = 'MES3_TASK_TYPE' order by sort, item_key, item_val",
    },
    required: true,
  },
  {
    label: '任务名',
    field: 'taskName',
    component: 'Input',
    required: true,
  },
  {
    label: '任务种类',
    field: 'taskKindCode',
    component: 'JDictSelectTag',
    componentProps: ({ formActionType, formModel }) => {
      return {
        showChooseOption: false,
        dictCode: "mes3_mdm_dict where group_code = 'MES3_TASK_KIND' and del_flag = 0 order by sort, item_key, item_val",
        getPopupContainer: () => document.body,
        onChange: async (e) =>{
          await clearData(formActionType, formModel, null, e);
        }
      }
    },
    required: true,
    defaultValue: '3',
    dynamicDisabled: ({ values }) => {
      return values.checkTaskType == CheckItemTypeEnum.TYPE_RECORD;
    },
    show: ({ values }) => {
      return values.checkTaskType != CheckItemTypeEnum.TYPE_RECORD;
    },
  },
  {
    label: '触发类型',
    field: 'recipeTypeCode',
    component: 'JDictSelectTag',
    componentProps: ({ formActionType, formModel }) => {
      return {
        showChooseOption: false,
        dictCode: "mes3_mdm_dict where group_code = 'MES3_PLAN_TYPE' and del_flag = 0 order by sort, item_key, item_val",
        getPopupContainer: () => document.body,
        onChange: async () =>{
          await clearData(formActionType, formModel, {taskKindCode: 'taskKindCode'}, null);
        }
      }
    },
    required: true,
    show: ({ values }) => {
      return values.taskKindCode == 3;
    },
  },
  {
    label: '触发细类',
    field: 'recipeSubTypeCode',
    component: 'JDictSelectTag',
    componentProps: {
      showChooseOption: false,
      dictCode: "mes3_mdm_dict where group_code = 'MES3_PLAN_TYPE_SUB' and del_flag = 0 order by sort, item_key, item_val",
      getPopupContainer: () => document.body,
    },
    required: true,
    show: ({ values }) => {
      return values.recipeTypeCode == 2 && values.taskKindCode == 3;
    },
  },
  {
    label: '推送频率',
    field: 'loopTriggerId',
    component: 'ApiSelect',
    componentProps: ({ formActionType, formModel }) => {
      return {
        api: task,
        labelInValue: true,
        params: {
          groupId: 8,
          exclusionTask: formModel.taskKindCode == 0 && formModel.checkTaskType == 1 ? null : 1,
          exclusionParam: formModel.taskKindCode == 0 && formModel.checkTaskType == 1 ? 'exclusion_task' : null
        },
        onChange: async (obj) => {
          addField(formActionType, formModel, obj.value ?? obj[0]);
        }
      }
    },
    required: true,
    show: ({ values }) => {
      return values.checkTaskType != CheckItemTypeEnum.TYPE_RECORD && (values.recipeTypeCode == 4 || values.taskKindCode == 0);
    },
  },
  {
    label: '推送时间',
    field: 'firstPushTime',
    component: 'Select',
    componentProps: {
      options:  hourOptions
    },
    required: true,
    show: ({ values }) => {
      return values.taskKindCode == 0 && values.checkTaskType != CheckItemTypeEnum.TYPE_RECORD;
    },
  },
  {
    //自动触发任务显示
    label: '自动触发时间段',
    field: 'autoTriggerRangeTime',
    component: 'RangeTime',
    componentProps: {
      format:'HH',
      valueFormat: 'HH',
    },
    required: true,
    show: ({ values }) => {
      return values.taskKindCode == 10;
    },
    defaultValue: '00,23',
    colProps: { 
      span: 17,
    },
  },
  {
    //自动触发任务显示
    label: '',
    field: 'autoTriggerRangeTime_text',
    colProps: { 
      span: 6,
      push: 1
    },
    component: 'Input',
    componentProps: {
      disabled: true,
    },
    show: ({ values }) => {
      return values.taskKindCode == 10;
    },
    render: ({ values }) => {
      if(values.autoTriggerRangeTime){
        let arr = values.autoTriggerRangeTime.split(',');
        return `${arr[0]}:00至${arr[1]}:59`;
      } else {
        return '未选择时间';
      }
    },
  },
  {
    //自动触发任务显示
    label: '开始步骤',
    field: 'startTriggerPoint',
    component: 'Input',
    slot: 'startStep',
    dynamicRules: ({ model }) => {
      return [
        {
          required: true,
          validator: (_, value) => {
            //需要return 一个Promise对象
            return new Promise((resolve, reject) => {
              if (!value || !model.startTriggerConditionType || !model.startTriggerConditionValue) {
                reject('请配置完整开始步骤！');
              }
              resolve();
            });
          },
        },
      ];
    },
    show: ({ values }) => {
      return values.taskKindCode == 10;
    },
  },
  {
    //自动触发任务显示
    label: '结束步骤',
    field: 'endTriggerPoint',
    component: 'Input',
    slot: 'endStep',
    dynamicRules: ({ model }) => {
      return [
        {
          required: false,
          validator: (_, value) => {
            //需要return 一个Promise对象
            return new Promise((resolve, reject) => {
              //其中一个不为空，其余所有都不能为空
              if(value || model.endTriggerConditionType || model.endTriggerConditionValue){
                //如果配置了结束步骤，频率就一定是自定义
                model.autoTriggerFrequencyType = 0;
                if (!value || !model.endTriggerConditionType || !model.endTriggerConditionValue) {
                  reject('请配置完整结束步骤！');
                }
              }
              resolve();
            });
          },
        },
      ];
    },
    show: ({ values }) => {
      return values.taskKindCode == 10;
    },
  },
  {
    //自动触发任务显示
    label: '自动触发频率',
    field: 'autoTriggerFrequencyType',
    component: 'JDictSelectTag',
    componentProps: {
      showChooseOption: false,
      dictCode: "mes3_mdm_dict where group_code = 'MES3_TASK_AUTO_TRIGGER_TYPE' and del_flag = 0 order by sort, item_key, item_val",
      getPopupContainer: () => document.body,
      stringToNumber: true,
    },
    dynamicDisabled: ({ values }) => {
      //如果配置了结束步骤，频率就一定要自定义
      return (values.endTriggerPoint || values.endTriggerConditionType || !!values.endTriggerConditionValue);
    },
    required: true,
    show: ({ values }) => {
      return values.taskKindCode == 10;
    },
  },
  {
    //自动触发任务显示
    label: '频率值',
    field: 'autoTriggerFrequencyValue',
    component: 'InputNumber',
    componentProps: {
      precision: 0
    },
    required: true,
    suffix: '小时',
    show: ({ values }) => {
      return values.taskKindCode == 10 && values.autoTriggerFrequencyType == 0;
    },
    dynamicRules: ({ values }) => {
      return [
        {
          required: true,
          validator: (_, value) => {
            //需要return 一个Promise对象
            return new Promise((resolve, reject) => {
              if (value <= 0) {
                reject('频率值必须大于0！');
              }
              resolve();
            });
          },
        },
      ];
    }
  },
  {
    //手动触发任务显示
    label: '操作岗位',
    field: 'manualTriggerPost',
    component: 'JDictSelectTag',
    componentProps: ({ formModel }) => {
      return {
        showChooseOption: false,
        dictCode: "mes3_mdm_post where dept_code = '"+formModel.deptCode+"' and del_flag = 0 order by sort, post_name, id",
        showSearch: true,
        stringToNumber: true,
      }
    },
    required: true,
    show: ({ values }) => {
      return values.taskKindCode == 20;
    },
  },
  //隐藏字段
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false
  },
  {
    label: '工厂编码',
    field: 'factoryCode',
    component: 'Input',
    show: false
  },
  {
    label: 'isCopy',
    field: 'isCopy',
    component: 'Input',
    componentProps: {
      disabled: true,
    },
    show: false
  },
  {
    label: '推送频率名称',
    field: 'loopTriggerName',
    component: 'Input',
    show: false
  },
  {
    label: '',
    field: 'pushJson',
    component: 'Input',
    show: false
  },
  {
    label: '',
    field: 'startTriggerId',
    component: 'Input',
    show: false
  },
  {
    label: '',
    field: 'endTriggerId',
    component: 'Input',
    show: false
  },
  {
    label: '',
    field: 'startTriggerConditionType',
    component: 'Input',
    show: false
  },
  {
    label: '',
    field: 'startTriggerConditionValue',
    component: 'Input',
    show: false
  },
  {
    label: '',
    field: 'endTriggerConditionType',
    component: 'Input',
    show: false
  },
  {
    label: '',
    field: 'endTriggerConditionValue',
    component: 'Input',
    show: false
  },
  {
    label: '',
    field: 'startTriggerDevice',
    component: 'Input',
    show: false
  },
  {
    label: '',
    field: 'endTriggerDevice',
    component: 'Input',
    show: false
  },
  {
    label: '',
    field: 'autoTriggerLastTime',
    component: 'Input',
    show: false
  },
];

export async function addField(formAction, form, value){
  let pushJson = form.pushJson ? JSON.parse(form.pushJson) : {};
  //清空动态添加项
  await formAction.removeSchemaByFiled(dynamicColumns);
  if(form.checkTaskType == CheckItemTypeEnum.TYPE_PLAN && value >= 55){
    //月推送
    if(value == TaskTypeEnum.TASK61){
      await formAction.appendSchemaByField(
        {
          label: '推送日期',
          field: 'pushDate_day',
          component: 'Select',
          componentProps: () => {
            return {
              options: monDayOptions,
              getPopupContainer: () => document.body,
              mode: 'multiple',
            }
          },
          required: true,
        },
        ''
      );
      formAction.setFieldsValue({
        pushDate_day: pushJson.pushDate_day.split(','),
      });
    }
    //小时推送
    else if(value == TaskTypeEnum.TASK55){
      await formAction.appendSchemaByField(
        {
          label: '间隔小时',
          field: 'pushDate_hour',
          component: 'InputNumber',
          componentProps: () => {
            return {
              min: 1,
              max: 24,
              step: 1,
              precision: 0
            }
          },
          required: true,
        },
        ''
      );
      formAction.setFieldsValue({
        pushDate_hour: pushJson.pushDate_hour,
      });
    }
    //天推送
    else if(value == TaskTypeEnum.TASK56){
      formAction.setFieldsValue({
        pushDate_everyday: pushJson.pushDate_everyday,
      });
    }
    //周推送
    else if(value == TaskTypeEnum.TASK58){
      await formAction.appendSchemaByField(
        {
          label: '推送周',
          field: 'pushDate_week',
          component: 'Select',
          componentProps: () => {
            return {
              options: [
                { value: '1', label: '周一' },
                { value: '2', label: '周二' },
                { value: '3', label: '周三' },
                { value: '4', label: '周四' },
                { value: '5', label: '周五' },
                { value: '6', label: '周六' },
                { value: '7', label: '周日' },
              ],
              getPopupContainer: () => document.body,
              mode: 'multiple',
            }
          },
          required: true,
        },
        ''
      );
      formAction.setFieldsValue({
        pushDate_week: pushJson.pushDate_week.split(','),
      });
    } else {
      //季度、半年、年推送
      let num = taskCycleObj[value];
      for(let i = 0; i < num; i++){
        await formAction.appendSchemaByField(
          {
            field: `pushDate${i+1}`,
            label: '推送日期' + (i+1),
            component: 'DatePicker',
            componentProps:{
              format: 'MM-DD',
              valueFormat: 'MM-DD',
              getPopupContainer: () => document.body,
            },
            colProps: {
              span: 24,
            },
            required: true,
          },
          ''
        );
        
        formAction.setFieldsValue({
          [`pushDate${i+1}`]: pushJson[`pushDate${i+1}`],
        });
      }
    }
  }
}

//清空数据
async function clearData(formActionType, formModel, fields, value) { 
  let noClearFields = {
    ...{
      id: 'id',
      factoryCode: 'factoryCode',
      isCopy: 'isCopy',
      deptCode_dictText: 'deptCode_dictText',
      lineCode_dictText: 'lineCode_dictText',
      deviceId_dictText: 'deviceId_dictText',
      deptCode: 'deptCode',
      lineCode: 'lineCode',
      deviceId: 'deviceId',
      checkTaskType: 'checkTaskType',
      taskTypeCode: 'taskTypeCode',
      taskName: 'taskName',
      state: 'state',
    },
    ...fields
  }
  for(let field in formModel){
    if(!noClearFields[field]){
      formModel[field] = null;
    }
  }
  if(value == 10){
    formModel.autoTriggerRangeTime = '00,23';
  }
  //清空动态添加项
  await formActionType.removeSchemaByFiled(dynamicColumns);
}