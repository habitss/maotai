import { ref } from 'vue';
import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { useUserStore } from '/@/store/modules/user';
import { getPopupContainer } from '/@/utils';
import { getDictItems } from '@/api/common/api';
const userStore = useUserStore();

export let MANAGE_CHECK_WEEK = ref([] as any);
export let MANAGE_CHECK_CYCLE = ref([] as any);
(async function f() {
  const records = await getDictItems("mes3_mdm_dict where group_code = 'MES3_MANAGE_CHECK_WEEK', item_key, item_val");
  MANAGE_CHECK_WEEK.value = records.filter(item => item.value < 8);
  MANAGE_CHECK_CYCLE.value = records.filter(item => item.value >= 8);
})();
//1-28天
let monDayOptions = ref(
  Array.from({ length: 28 }, (_, i) => ({
    value: i + 1+'',
    label: `${i + 1}号推送`,
    disabled: false
  }))
)

//列表数据
export const columns: BasicColumn[] = [
  {
    title: '部门',
    align: "center",
    dataIndex: 'deptCode_dictText',
    fixed: 'left',
  },
  {
    title: '产线',
    align: "center",
    dataIndex: 'lineCode_dictText',
    fixed: 'left',
  },
  {
    title: '检查项编号',
    align: "center",
    dataIndex: 'checkBh',
    fixed: 'left',
  },
  {
    title: '常规活动',
    align: "center",
    dataIndex: 'cghd',
    width: 140,
    fixed: 'left',
  },
  {
    title: '检查项来源',
    align: "center",
    dataIndex: 'jcxlyDm_dictText',
    fixed: 'left',
  },
  {
    title: '检查体系',
    align: "center",
    dataIndex: 'jctxDm_dictText',
    fixed: 'left',
  },
  {
    title: '检查内容',
    align: "left",
    dataIndex: 'jcnr',
    width: 300,
    fixed: 'left',
    resizable: true
  },
  {
    title: '是否必须生产下执行',
    align: "center",
    dataIndex: 'ifZx',
    customRender: ({ text }) => {
      return text == 1 ? "是" : "否";
    }
  },
  {
    title: '排序',
    align: "center",
    dataIndex: 'sort',
    width: 60,
  },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: "部门",
    field: 'deptCode',
    component: 'KtDepartTreeSelect',
    componentProps: {
      defaultExpandLevel: 1,
      fieldNames: { label: 'title', value: 'orgCode' },
    },
    defaultValue: userStore?.userInfo?.orgCode
  },
  {
    label: "常规活动",
    field: "cghd",
    component: 'Input',
  },
  {
    label: '检查项来源',
    field: 'jcxlyDm',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "mes3_mdm_dict where group_code = 'MES3_MANAGE_CHECK_SOURCE', item_key, item_val",
      type: 'select',
      labelField: 'item_key',
      valueField: 'item_val',
      showChooseOption: false,
    },
  },
  {
    label: '检查体系',
    field: 'jctxDm',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "mes3_mdm_dict where group_code = 'MES3_MANAGE_CHECK_TX', item_key, item_val",
      type: 'select',
      labelField: 'item_key',
      valueField: 'item_val',
      showChooseOption: false,
    },
  },
  {
    label: '检查体系',
    field: 'jctxDm',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "mes3_mdm_dict where group_code = 'MES3_MANAGE_CHECK_TX', item_key, item_val",
      type: 'select',
      labelField: 'item_key',
      valueField: 'item_val',
      showChooseOption: false,
    },
  },
  {
    label: '岗位名称',
    field: 'postId',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "mes3_mdm_post where del_flag = 0 and post_type = 1, post_name, id",
      showChooseOption: false,
      showSearch: true
    },
  },
  {
    label: '是否配置频率',
    field: 'isConfig',
    component: 'Switch',
    componentProps: {
      size:'default',
      unCheckedChildren:'否',
      unCheckedValue:'0',
      checkedChildren:'是',
      checkedValue:'1',
    },
    defaultValue: '0',
    ifShow: ({ model }) => {
      return model.postId;
    },
  },
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '部门',
    field: 'deptCode',
    component: 'KtDepartTreeSelect',
    componentProps: ({ formModel }) => {
      return {
        defaultExpandLevel: 1,
        fieldNames: { label: 'title', value: 'orgCode' },
        onChange: async e => {
          formModel.lineCode = null;
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
        parentKey: formModel.deptCode,
      }
    },
  },
  {
    label: '检查项编号',
    field: 'checkBh',
    component: 'Input',
    required: true,
  },
  {
    label: '常规活动',
    field: 'cghd',
    component: 'Input',
    required: true,
  },
  {
    label: '检查项来源',
    field: 'jcxlyDm',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "mes3_mdm_dict where group_code = 'MES3_MANAGE_CHECK_SOURCE', item_key, item_val",
      type: 'select',
      labelField: 'item_key',
      valueField: 'item_val',
      showChooseOption: false,
    },
    required: true,
  },
  {
    label: '检查体系',
    field: 'jctxDm',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "mes3_mdm_dict where group_code = 'MES3_MANAGE_CHECK_TX', item_key, item_val",
      type: 'select',
      labelField: 'item_key',
      valueField: 'item_val',
      showChooseOption: false,
    },
    required: true,
  },
  {
    label: '检查内容',
    field: 'jcnr',
    component: 'InputTextArea',
    required: true,
  },
  {
    label: '是否必须生产下执行',
    field: 'ifZx',
    component: 'Select',
    componentProps: {
      options: [
        {
          label: '是',
          value: 1,
        },
        {
          label: '否',
          value: 0,
        },
      ]
    },
    defaultValue: 0,
    required: true,
  },
  {
    label: '排序',
    field: 'sort',
    component: 'InputNumber',
  },
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false
  },
];

//岗位配置表单数据
export const gwpzFormSchema: FormSchema[] = [
  {
    label: '检查频率',
    field: 'plSelect',
    component: 'JDictSelectTag',
    componentProps: ({ formModel }) => {
      return {
        dictCode: "mes3_mdm_dict where group_code = 'MES3_MANAGE_CHECK_PL' order by sort, item_key, item_val",
        getPopupContainer: () => getPopupContainer(),
        showChooseOption: false,
        labelInValue: true,
        onChange: (e) =>{
          formModel.pl = e?.value;
          formModel.plName = e?.label[0].children;
          formModel.week = undefined;
          formModel.weekName = undefined;
          formModel.weekSelect = undefined;
          formModel.day = undefined;
          formModel.monDay1 = undefined;
          formModel.monDay2 = undefined;
          formModel.monDay3 = undefined;
          formModel.monDay4 = undefined;
          if(e?.value > 2){
            formModel.week = e?.value;
            formModel.weekName = MANAGE_CHECK_CYCLE.value.find(item => item.value == e.value).text;
          } else {
            disabledWeekOption(formModel.pl, formModel?.week);
          }
          disabledMonDayOption(formModel.pl, formModel?.day);
        }
      }
    },
    required: true,
  },
  {
    //频率为周时显示使用
    label: '检查周期',
    field: 'weekSelect',
    component: 'Select',
    componentProps: ({ formModel }) => {
      return {
        options: MANAGE_CHECK_WEEK.value,
        getPopupContainer: () => getPopupContainer(),
        labelInValue: true,
        mode: 'multiple',
        onChange: (e) => {
          e.sort((a, b) => a.value - b.value);
          formModel.week = e.map(item => item.value).join(',');
          formModel.weekName = e.map(item => item.label).join(',');
          disabledWeekOption(formModel.pl, formModel.week)
        }
      }
    },
    dynamicRules: ({ values }) => {
      //需要return
      return [
        {
          required: true,
          validator: (_, value) => {
            return new Promise((resolve, reject) => {
              if (!value) {
                reject('请选择检查周期');
              }
              if (value.length < values.pl) {
                reject('检查周期次数不足');
              }
              resolve();
            });
          },
        },
      ];
    },
    show: ({ values }) => {
      return values?.pl < 3 ? true : false;
    }
  },
  {
    //频率为每月一次时显示使用
    label: '检查日期',
    field: 'day',
    component: 'Select',
    componentProps: ({ formModel }) => {
      return {
        options: monDayOptions,
        getPopupContainer: () => getPopupContainer(),
        mode: 'multiple',
        onChange: (e) => {
          disabledMonDayOption(formModel.pl, e)
        }
      }
    },
    dynamicRules: ({ values }) => {
      //需要return
      return [
        {
          required: true,
          validator: (_, value) => {
            return new Promise((resolve, reject) => {
              if (!value) {
                reject('请选择检查周期');
              }
              let num = values.pl == 89 ? 2 : 1;
              if (value.length < num) {
                reject('检查周期次数不足');
              }
              resolve();
            });
          },
        },
      ];
    },
    show: ({ values }) => {
      return values?.pl == 91 || values?.pl == 89 ? true : false;
    },
    required: true,
  },
  {
    //频率为每3月、每6月、一年一次时显示使用
    label: '检查日期',
    field: 'monDay1',
    component: 'DatePicker',
    componentProps:{
      format: 'MM-DD',
      valueFormat: 'MM-DD',
      getPopupContainer: () => getPopupContainer(),
    },
    show: ({ values }) => {
      return values?.pl >= 92 && values?.pl <= 94 ? true : false;
    },
    required: true,
  },
  {
    //频率为每3月、每6月一次时显示使用
    label: '检查日期',
    field: 'monDay2',
    component: 'DatePicker',
    componentProps:{
      format: 'MM-DD',
      valueFormat: 'MM-DD',
      getPopupContainer: () => getPopupContainer(),
    },
    show: ({ values }) => {
      return values?.pl >= 92 && values?.pl <= 93 ? true : false;
    },
    required: true,
  },
  {
    //频率为每3月一次时显示使用
    label: '检查日期',
    field: 'monDay3',
    component: 'DatePicker',
    componentProps:{
      format: 'MM-DD',
      valueFormat: 'MM-DD',
      getPopupContainer: () => getPopupContainer(),
    },
    show: ({ values }) => {
      return values?.pl == 92 ? true : false;
    },
    required: true,
  },
  {
    //频率为每3月一次时显示使用
    label: '检查日期',
    field: 'monDay4',
    component: 'DatePicker',
    componentProps:{
      format: 'MM-DD',
      valueFormat: 'MM-DD',
      getPopupContainer: () => getPopupContainer(),
    },
    show: ({ values }) => {
      return values?.pl == 92 ? true : false;
    },
    required: true,
  },
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false
  },
  {
    label: '频率',
    field: 'pl',
    component: 'Input',
    show: false
  },
  {
    label: '频率名称',
    field: 'plName',
    component: 'Input',
    show: false
  },
  {
    label: '周期',
    field: 'week',
    component: 'Input',
    show: false
  },
  {
    label: '检查周期',
    field: 'weekName',
    component: 'Input',
    componentProps:{
      disabled: true
    },
    show: ({ values }) => {
      return values?.pl == 90 ? true : false;
    }
  },
];

/**
 * 判断哪些检查周期需要禁用
 * @param pl 频率
 * @param week 周
 */
export function disabledWeekOption(pl, week){
  let weekArr = week ? week.split(',') : [];
  //循环周期数据
  for(const item of MANAGE_CHECK_WEEK.value){
    //如果选择的周期小于频率，则不限制选择
    if(weekArr.length < pl){
      item.disabled = false;
    }else if(!weekArr.includes(item.value)){
      item.disabled = true;
    }
  }
}

/**
 * 判断哪些检查日期需要禁用
 * @param pl 频率
 * @param day 天
 */
export function disabledMonDayOption(pl, day){
  let num = pl == 89 ? 2 : 1;
  let dayArr = day ?? [];
  for(const item of monDayOptions.value){
    //禁用非选择项以及选择数量大于可选择数量
    item.disabled = !dayArr.includes(item.value) && dayArr.length >= num;
  }
}