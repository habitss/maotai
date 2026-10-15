import {BasicColumn, FormSchema} from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import {USER_LIST} from "@/views/dataCalculation/actionPlan/track/ActionPlanTrack.data";
import {getPopupContainer} from "@/utils";
const { t } = useI18n();

//列表数据
export const columns: BasicColumn[] = [
  {
    title: '工厂',
    dataIndex: 'factoryCodeVal',
    align: "center",
    width: 100,
  },
  {
    //部门
    title: t('language.department'),
    dataIndex: 'deptCode_dictText',
    align: 'center',
    resizable: true,
    width: 100,
  },
  {
    title: '车间',
    dataIndex: 'workshopVal',
    align: "center",
    width: 100,
  },
  {
    title: '会议类型',
    dataIndex: 'meetingTypeVal',
    width: 100,
  },
  {
    title: '会议名称',
    dataIndex: 'meetingName',
    width: 130,
  },
  {
    title: '会议日期',
    dataIndex: 'meetingDate',
    editRow: true,
    editComponent: 'Input',
    width: 150,
  },
  {
    title: '会议时间',
    dataIndex: 'meetingTime',
    editRow: true,
    editComponent: 'Input',
    width: 120,
  },
  {
    title: '评分',
    dataIndex: 'score',
    width: 50,
  },
  {
    title: '状态',
    dataIndex: 'active',
    customRender:function ({ record }) {
      return String(record.active) === '1' ? "未取消" : "取消";
    },
    width: 70,
  },
  {
    title: '理由',
    dataIndex: 'notes',
    ellipsis:false,
    width: 70,
  }
];

//列表数据
export const meetingColumns: BasicColumn[] = [
  {
    title: '会议议程',
    dataIndex: 'contentTitle',
    ellipsis:false,
    width: 80,
  },
  {
    title: '会议内容',
    dataIndex: 'content',
    ellipsis:false,
    width: 240,
  },
  {
    title: '链接',
    dataIndex: 'link',
    ellipsis:false,
    width: 100,
  },
  {
    title: '汇报人',
    align: "center",
    dataIndex: 'reporterVal',
    width: 50,
  },
  {
    title: '代理人',
    align: "center",
    dataIndex: 'agentVal',
    width: 50,
  },
  {
    title: '计划时长(分)',
    dataIndex: 'time',
    width: 60,
  },
  {
    title: '实际用时(秒)',
    dataIndex: 'realTime',
    width: 60,
  },
  {
    title: '附件',
    dataIndex: 'remark',
    ellipsis:false,
    width: 50,
  },
];

export const checkColumns: BasicColumn[] = [

  {
    title: '参会人员',
    dataIndex: 'checkPersonVal',
    width: 80,
  },
  {
    title: '人员类型',
    dataIndex: 'checkPersonType',
    customRender:function ({ record }) {
      if(record.checkPersonType === 0){
        return "固定参会人";
      } else if(record.checkPersonType === 1){
        return "非固定参会人";
      }
    },
    width: 80,
  },
  {
    title: '参会信息',
    dataIndex: 'active',
    customRender:function ({ record }) {
      if(record.active === 0){
        return "缺席";
      } else if(record.active === 1){
        return "出席";
      } else if(record.active === 2){
        return "请假";
      }

    },
    width: 50,
  },
  {
    title: '签到时间',
    dataIndex: 'checkTime',
    width: 90,
  },
  {
    title: '备注',
    dataIndex: 'remark',
    ellipsis:false,
    width: 100,
  },

];

export const checkFormSchemas: FormSchema[] = [
  {
    label: '参会状态',
    field: 'active',
    component: 'Select',
    colProps: { span: 11 },
    componentProps: {
      options: [
        {
          label: '缺席',
          value: 0,
        },
        {
          label: '出席',
          value: 1,
        },
        {
          label: '请假',
          value: 2,
        },
      ],
    },
    required: true,
  },
  {
    label: '理由',
    field: 'remark',
    component: 'InputTextArea',
    colProps: { span: 24 },
    required: true,
    //属性
    componentProps:{
      //可以点击清除图标删除内容
      allowClear: true,
      //自适应内容高度，可设置为 true | false 或对象：{ minRows: 2, maxRows: 6 }
      autoSize:{
        //最小显示行数
        minRows: 5,
        //最大显示行数
        maxRows: 10
      },
    }
  },

];

//列表数据
export const recordColumns: BasicColumn[] = [

  {
    title: '会议内容',
    dataIndex: 'content',
    width: 200,
  },
  {
    title: '评分标准',
    dataIndex: 'standard',
    width: 200,
  },
  {
    title: '权重',
    align: "center",
    dataIndex: 'weight',
    width: 80,
  },
  {
    title: '得分',
    align: "center",
    dataIndex: 'score',
    editRow: true,
    width: 80,
  },
  {
    title: '现有分数原因',
    dataIndex: 'scoreReason',
    editRow: true,
    width: 150,
  },

];
export const addConfigFormSchemas: FormSchema[] = [
  {
    label: '主键',
    field: 'id',
    component: 'Input',
    show: false,
    colProps: { span: 20},
  },
  {
    label: "部门",
    field: 'deptCode',
    component: 'KtDepartTreeSelect',
    componentProps: ({formActionType,formModel}) => {
      return {
        defaultExpandLevel: 1,
        fieldNames: {label: 'title', value: 'id'},
        getPopupContainer: () => getPopupContainer(),
        onChange: e => {
          changeType(formActionType, formModel, e);
        }
      }
    },
    colProps: { span: 20 },
    required: true,
  },
  {
    label: '会议类型',
    field: 'meetingType',
    component: 'JDictSelectTag',
    componentProps: {
      showChooseOption:false,
    },
    colProps: { span: 20 },
    defaultValue: '',
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
    colProps: { span: 20 },
    defaultValue: '',
  },
  {
    label: '会议名称',
    field: 'meetingName',
    component: 'Input',
    colProps: { span: 20 },
    defaultValue: '',
    required: true,
  },
  {
    label: '会议日期',
    field: 'meetingDate',
    component: 'DatePicker',
    componentProps: {
      size:'default',
      showToday:true,
      format: 'YYYY-MM-DD',
      valueFormat: 'YYYY-MM-DD',
    },
    // defaultValue: dayjs().format('YYYY-MM-DD'),
    colProps: { span: 20 },
    required: true,
  },
  {
    label: '会议时间',
    field: 'meetingTime',
    component: 'TimePicker',
    componentProps: {
      size:'default',
      showNow: true,
      format: 'HH:mm',
      valueFormat: 'HH:mm',
    },
    // defaultValue: dayjs().format('HH:mm'),
    colProps: { span: 20 },
    required: true,
  },

];

export const cancelFormSchemas: FormSchema[] = [
  {
    label: '取消原因',
    field: 'notes',
    component: 'InputTextArea',
    required: true,
    colProps: { span: 24 },
    //属性
    componentProps:{
      //可以点击清除图标删除内容
      allowClear: true,
      //自适应内容高度，可设置为 true | false 或对象：{ minRows: 2, maxRows: 6 }
      autoSize:{
        //最小显示行数
        minRows: 5,
        //最大显示行数
        maxRows: 10
      },
    }
  },

];

export const editFormSchemas: FormSchema[] = [
  {
    label: '主键',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    label: '会议日程',
    field: 'contentTitle',
    component: 'InputTextArea',
    colProps: { span: 24 },
    dynamicDisabled: () => {
      return true;
    },
    required: true,
    //属性
    componentProps:{
      //可以点击清除图标删除内容
      allowClear: true,
      //自适应内容高度，可设置为 true | false 或对象：{ minRows: 2, maxRows: 6 }
      autoSize:{
        //最小显示行数
        minRows: 2,
        //最大显示行数
        maxRows: 3
      },
    }
  },
  {
    label: '会议内容',
    field: 'content',
    component: 'InputTextArea',
    colProps: { span: 24 },
    dynamicDisabled: () => {
      return true;
    },
    required: true,
    //属性
    componentProps:{
      //可以点击清除图标删除内容
      allowClear: true,
      //自适应内容高度，可设置为 true | false 或对象：{ minRows: 2, maxRows: 6 }
      autoSize:{
        //最小显示行数
        minRows: 2,
        //最大显示行数
        maxRows: 3
      },
    }
  },
  {
    label: '汇报人',
    field: 'reporter',
    component: 'Select',
    colProps: { span: 8 },
    componentProps: () => {
      return {
        options: USER_LIST.value,
        fieldNames: {label: 'realname', value: 'id'},
        showSearch: true,
        allowClear: true,
        filterOption: (inputValue, option) =>{
          return option.realname.toLowerCase().indexOf(inputValue.toLowerCase()) >= 0;
        }
      }
    },
  },
  {
    label: '代理人',
    field: 'agent',
    component: 'Select',
    colProps: { span: 8 },
    componentProps: () => {
      return {
        options: USER_LIST.value,
        fieldNames: {label: 'realname', value: 'id'},
        showSearch: true,
        allowClear: true,
        filterOption: (inputValue, option) =>{
          return option.realname.toLowerCase().indexOf(inputValue.toLowerCase()) >= 0;
        }
      }
    },
  },
  {
    label: '计划时长',
    field: 'time',
    component: 'Input',
    dynamicDisabled: () => {
      return true;
    },
    colProps: { span: 5 },
  },
  {
    label: '计时',
    field: 'optime',
    slot: 'optime',
    component: 'Input',
    colProps: { span: 10 },
  },
  {
    label: '实际时长',
    field: 'realTime',
    slot: 'realTime',
    component: 'Input',
    colProps: { span: 10 },
  },
  {
    label: '备注',
    field: 'remark',
    component: 'InputTextArea',
    colProps: { span: 24 },
    //属性
    componentProps:{
      //可以点击清除图标删除内容
      allowClear: true,
      //自适应内容高度，可设置为 true | false 或对象：{ minRows: 2, maxRows: 6 }
      autoSize:{
        //最小显示行数
        minRows: 2,
        //最大显示行数
        maxRows: 3
      },
    }
  },
  {
    label: '顺序',
    field: 'sort',
    component: 'InputNumber',
    // labelWidth: 140,
    // labelLength: 8,
    // colProps: { span: 24 },
  },
  {
    field: 'uploadFiles',
    component: 'JUpload',
    componentProps: {
      fileType: 'all',
    },
    helpMessage: '无限制上传',
    label: '上传文件',
  },
  {
    label: '链接',
    field: 'link',
    component: 'Input',
    colProps: { span: 16 },
  },
  {
    label: '链接地址',
    field: 'linkAddress',
    component: 'InputTextArea',
    colProps: { span: 24 },
  },
];

async function changeType(form,value,index){
  console.log(form,value,index)
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



