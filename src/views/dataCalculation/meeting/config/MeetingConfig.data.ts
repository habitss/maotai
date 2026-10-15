import { BasicColumn, FormSchema } from '/@/components/Table';
import {
  USER_LIST
} from "@/views/dataCalculation/actionPlan/track/ActionPlanTrack.data";
import {getPopupContainer} from "@/utils";

//列表表头配置
export const columns: BasicColumn[] = [
  {
    title: '部门信息',
    children: [
      {
        title: '工厂',
        dataIndex: 'factoryCodeVal',
        align: "center",
        width: 100,
      },
      {
        title: '部门',
        dataIndex: 'deptCodeVal',
        align: "center",
        width: 100,
      },
      {
        title: '车间',
        align: "center",
        dataIndex: 'workshopVal',
        width: 100,
      },
    ],
  },
  {
    title: '会议名称',
    align: "center",
    dataIndex: 'meetingName',
    width: 120,
  },
  {
    title: '会议类型',
    align: "center",
    dataIndex: 'meetingType',
    width: 120,
  },
  {
    title: '会议频次',
    align: "center",
    dataIndex: 'meetingCycle',
    customRender:function ({ record }) {
      if (record.meetingCycle == 1) {
        return "日会"
      } else if (record.meetingCycle == 2) {
        return "周会"
      } else if (record.meetingCycle == 3) {
        return "月会"
      } else if (record.meetingCycle == 4) {
        return "半年会"
      } else if (record.meetingCycle == 5) {
        return "年会"
      }
    },
    width: 100,
  },
  {
    title: '主持人',
    align: "center",
    dataIndex: 'presenterVal',
    ellipsis:false,
    width: 200,
  },
  {
    title: '参会人',
    align: "center",
    dataIndex: 'participantVal',
    ellipsis:false,
    width: 200,
  },
];

//列表页面查询表单
export const searchFormSchema: FormSchema[] = [
  {
    label: "部门",
    field: 'deptId',
    component: 'KtDepartTreeSelect',
    componentProps: ({formActionType}) => {
      return {
        defaultExpandLevel: 1,
        fieldNames: { label: 'title', value: 'id' },
        onChange: e =>{
          changeType(formActionType,e);
        }
      };
    },
  },

  {
    label: '会议类型',
    field: 'meetingType',
    component: 'JDictSelectTag',
    componentProps: {
      showChooseOption:false,
    },
    colProps: { span: 6 },
    defaultValue: '',
  },

  {
    label: '会议名称',
    field: 'meetingName',
    component: 'Input',
    colProps: { span: 6 },
  },
];

//新增/修改表单字段
export const addFormSchemas: FormSchema[] = [
    {
      label: '主键',
      field: 'id',
      component: 'Input',
      show: false,
    },
  {
    label: "部门",
    field: 'deptCode',
    component: 'KtDepartTreeSelect',
    componentProps: {
      defaultExpandLevel: 1,
      fieldNames: { label: 'title', value: 'id' },
      getPopupContainer:() => getPopupContainer(),
    },
    colProps: { span: 11 },
    required: true,
  },
    {
      label: '会议名称',
      field: 'meetingName',
      component: 'Input',
      colProps: { span: 11 },
      required: true,
    },
    {
      label: '会议类型',
      field: 'meetingType',
      component: 'Input',
      colProps: { span: 11 },
      required: true,
    },
  {
    label: '会议频次',
    field: 'meetingCycle',
    component: 'Select',
    required: true,
    componentProps: {
      options: [
        {
          label: '日会',
          value: 1,
        },
        {
          label: '周会',
          value: 2,
        },
        {
          label: '月会',
          value: 3,
        },
        {
          label: '半年会',
          value: 4,
        },
        {
          label: '年会',
          value: 5,
        },
      ],
    },
    colProps: { span: 11 },
    defaultValue: '',
  },
  {
    label: '主持人',
    field: 'presenter',
    component: 'Select',
    colProps: { span: 20 },
    componentProps: () => {
      return {
        options: USER_LIST.value,
        fieldNames: {label: 'realname', value: 'id'},
        maxTagCount: 100,
        getPopupContainer:() => getPopupContainer(),
        mode:'multiple',
        allowClear: true,
        placement: 'bottomRight',
        showSearch: true,
        filterOption: (inputValue, option) =>{
          return option.realname.toLowerCase().indexOf(inputValue.toLowerCase()) >= 0;
        }
      }
    },
  },
    {
      label: '参会人',
      field: 'participant',
      component: 'Select',
      colProps: { span: 20 },
      componentProps: () => {
        return {
          options: USER_LIST.value,
          fieldNames: {label: 'realname', value: 'id'},
          maxTagCount: 100,
          getPopupContainer:() => getPopupContainer(),
          mode:'multiple',
          allowClear: true,
          placement: 'bottomRight',
          showSearch: true,
          filterOption: (inputValue, option) =>{
            return option.realname.toLowerCase().indexOf(inputValue.toLowerCase()) >= 0;
          }
        }
      },
    },

  ];


async function changeType(form,index){
  console.log(index)
  await form.updateSchema([
    {
      field: 'meetingType',
      component: 'JDictSelectTag',
      componentProps: {
        dictCode: 'mes3_meeting_config, meeting_type, id, del_flag = 0 and dept_id=\'' + index + '\'' ,
      },
    },
  ]);
}
