import dayjs from 'dayjs';
import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { useUserStore } from '/@/store/modules/user';
import { getCurrentDepart } from '/@/utils/common/compUtils';
import { ref } from 'vue';
export const currentDepart = getCurrentDepart();
const userStore = useUserStore();
//当前星期
const currentWeek = ref(dayjs().day() == 0 ? '7' : String(dayjs().day()));
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '部门',
    align: "center",
    dataIndex: 'dynamicDept',
  },
  {
    title: '检查清单名称',
    align: "center",
    dataIndex: 'dynamicName'
  },
  {
    title: '推送周',
    align: "center",
    dataIndex: 'pushWeek_dictText',
    width: 80,
  },
  {
    title: '记录日期',
    align: "center",
    dataIndex: 'recordDate'
  },
  {
    title: '记录周',
    align: "center",
    dataIndex: 'recordWeek_dictText',
    width: 80,
  },
  {
    title: '检查人',
    align: "center",
    dataIndex: 'inspectedBy_dictText'
  },
  {
    title: '确认人',
    align: "center",
    dataIndex: 'confirmBy_dictText'
  },
  {
    title: '结果确认人',
    align: "center",
    dataIndex: 'resultConfirmBy_dictText'
  },
  {
    title: '结果确认时间',
    align: "center",
    dataIndex: 'resultConfirmTime'
  },
  {
    title: '生产状态',
    align: "center",
    dataIndex: 'stopProduction',
    slots: { customRender: 'stopProductionSlot' },
  },
  {
    title:'录入状态',
    align:"center",
    dataIndex: 'isEdit',
    slots: { customRender: 'isEditSlot' },
  },
  {
    title:'审核状态',
    align:"center",
    dataIndex: 'confirmResult',
    slots: { customRender: 'confirmResultSlot' },
  },
  {
    title:'创建时间',
    align:"center",
    dataIndex: 'createTime',
    width: 180
  },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: "部门",
    field: 'deptCode',
    component: 'KtDepartTreeSelect',
    componentProps: {
      allowClear: false,
      fieldNames: { label:'title', value: 'orgCode' },
      defaultExpandLevel: 1,
    },
    defaultValue: userStore?.userInfo?.orgCode,
  },
  {
    label: "记录日期",
    field: "recordDate",
    component: 'RangePicker',
  },
  {
    label: "推送周",
    field: "pushWeek",
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "mes3_mdm_dict where group_code = 'MES3_MANAGE_CHECK_WEEK', item_key, item_val",
      showChooseOption: false,
    },
  },
  {
    label: "记录周",
    field: "recordWeek",
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "mes3_mdm_dict where group_code = 'MES3_MANAGE_CHECK_WEEK', item_key, item_val",
      showChooseOption: false,
    },
  },
  {
    label: "审核状态",
    field: "confirmResult",
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "mes3_mdm_dict where group_code = 'MES3_MANAGE_CHECK_CONFIRM', item_key, item_val",
      showChooseOption: false,
    },
  },
  {
    label: "岗位名称",
    field: "postName",
    component: 'Input',
  },
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: "推送周",
    field: "pushWeek",
    component: 'JDictSelectTag',
    componentProps: ({ formActionType }) => {
      return {
        dictCode: "mes3_mdm_dict where group_code = 'MES3_MANAGE_CHECK_WEEK', item_key, item_val",
        showChooseOption: false,
        onChange: () => {
          formActionType.submit();
        }
      }
    },
    dynamicDisabled: ({ values }) => {
      return values.id ? true : false;
    },
    defaultValue: currentWeek.value
  },
  {
    label: "记录日期",
    field: "recordDate",
    component: 'DatePicker',
    componentProps: ({ schema, tableAction, formActionType, formModel }) => {
      return {
        valueFormat: 'YYYY-MM-DD',
        format: 'YYYY-MM-DD',
        onChange:e=>{
          formModel.recordWeek = dayjs(e).day() == 0 ? '7' : String(dayjs(e).day());
        }
      }
    },
    dynamicDisabled: ({ values }) => {
      return '1,2'.indexOf(values?.confirmResult) >= 0 ? true : false;
    },
    defaultValue: dayjs().format('YYYY-MM-DD')
  },
  {
    label: "记录周",
    field: "recordWeek",
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "mes3_mdm_dict where group_code = 'MES3_MANAGE_CHECK_WEEK', item_key, item_val",
      showChooseOption: false,
      disabled: true,
    },
    defaultValue: currentWeek.value
  },
  {
    label: "检查人",
    field: "inspectedBy",
    component: 'KtUserScrollSelect',
    componentProps: {
      showChooseOption: false,
    },
    dynamicDisabled: ({ values }) => {
      return '1,2'.indexOf(values?.confirmResult) >= 0 ? true : false;
    },
    defaultValue: userStore?.userInfo?.id
  },
  {
    label: "确认人",
    field: "confirmBy",
    component: 'KtUserScrollSelect',
    componentProps: {
      showChooseOption: false,
    },
    dynamicDisabled: ({ values }) => {
      return '1,2'.indexOf(values?.confirmResult) >= 0 ? true : false;
    },
  },
  {
    label: "生产状态",
    field: "stopProduction",
    component: 'Select',
    slot: 'stopProduction',
    componentProps: ({ formActionType }) => {
      return {
        options: [
          { label: '生产', value: '1' },
          { label: '停产', value: '0' },
          { label: '休假', value: '2' }
        ],
        onChange: () => {
          formActionType.submit();
        }
      }
    },
    defaultValue: '1'
  },
  {
    label: "id",
    field: "id",
    component: 'Input',
    show: false,
  },
  {
    label: "confirmResult",
    field: "confirmResult",
    component: 'Input',
    show: false,
  },
];

//列表数据
export const modalColumns: BasicColumn[] = [
  {
    title: '序号',
    align: "center",
    dataIndex: 'number',
    width: 50,
  },
  {
    title: '检查项编号',
    align: "center",
    dataIndex: 'itemNumber',
    width: 100,
  },
  {
    title: '常规活动',
    align: "center",
    dataIndex: 'activities',
    width: 140,
  },
  {
    title: '检查项来源',
    align: "center",
    dataIndex: 'sourceName',
    width: 120,
  },
  {
    title: '频率',
    align: "center",
    dataIndex: 'frequencyName',
    width: 100,
  },
  {
    title: '体系',
    align: "center",
    dataIndex: 'systemName',
    width: 100,
  },
  {
    title: '检查内容',
    align: "center",
    width: '20vw',
    dataIndex: 'content'
  },
  {
    title: '检查结果',
    align: "center",
    colSpan: 2,
    dataIndex: 'rowType',
    width: 100,
    slots: { customRender: 'rowTypeSlot' },
  },
  {
    title: '检查结果',
    align: "center",
    width: '20vw',
    colSpan: 0,
    dataIndex: 'resulSlot',
    slots: { customRender: 'resulSlot' },
  },
];