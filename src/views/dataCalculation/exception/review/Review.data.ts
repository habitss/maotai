import {BasicColumn, FormSchema} from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
const { t } = useI18n();

//列表数据
export const columns: BasicColumn[] = [
  {
    //产线
    title: '产线',
    dataIndex: 'type',
    align: 'center',
    resizable: true,
    width: 100,
  },
  {
    title: '一月',
    dataIndex: 'jan',
    align: 'center',
    width: 50,
  },
  {
    title: '二月',
    dataIndex: 'feb',
    align: 'center',
    width: 50,
  },
  {
    title: '三月',
    dataIndex: 'mar',
    align: 'center',
    width: 50,
  },
  {
    title: '四月',
    dataIndex: 'apr',
    align: 'center',
    width: 50,
  },
  {
    title: '五月',
    dataIndex: 'may',
    align: 'center',
    width: 50,
  },
  {
    title: '六月',
    dataIndex: 'jun',
    align: 'center',
    width: 50,
  },
  {
    title: '七月',
    dataIndex: 'jul',
    align: 'center',
    width: 50,
  },
  {
    title: '八月',
    dataIndex: 'aug',
    align: 'center',
    width: 50,
  },
  {
    title: '九月',
    dataIndex: 'sep',
    align: 'center',
    width: 50,
  },
  {
    title: '十月',
    dataIndex: 'oct',
    align: 'center',
    width: 50,
  },
  {
    title: '十一月',
    dataIndex: 'nov',
    align: 'center',
    width: 50,
  },
  {
    title: '十二月',
    dataIndex: 'dec',
    align: 'center',
    width: 50,
  },
  {
    title: '总数',
    dataIndex: 'total',
    align: 'center',
    width: 50,
  },

];

export const exceptionColumns: BasicColumn[] = [
  {
    //产线
    title: '异常分析',
    dataIndex: 'type',
    align: 'center',
    resizable: true,
    width: 100,
  },
  {
    title: '一月',
    dataIndex: 'jan',
    align: 'center',
    width: 50,
  },
  {
    title: '二月',
    dataIndex: 'feb',
    align: 'center',
    width: 50,
  },
  {
    title: '三月',
    dataIndex: 'mar',
    align: 'center',
    width: 50,
  },
  {
    title: '四月',
    dataIndex: 'apr',
    align: 'center',
    width: 50,
  },
  {
    title: '五月',
    dataIndex: 'may',
    align: 'center',
    width: 50,
  },
  {
    title: '六月',
    dataIndex: 'jun',
    align: 'center',
    width: 50,
  },
  {
    title: '七月',
    dataIndex: 'jul',
    align: 'center',
    width: 50,
  },
  {
    title: '八月',
    dataIndex: 'aug',
    align: 'center',
    width: 50,
  },
  {
    title: '九月',
    dataIndex: 'sep',
    align: 'center',
    width: 50,
  },
  {
    title: '十月',
    dataIndex: 'oct',
    align: 'center',
    width: 50,
  },
  {
    title: '十一月',
    dataIndex: 'nov',
    align: 'center',
    width: 50,
  },
  {
    title: '十二月',
    dataIndex: 'dec',
    align: 'center',
    width: 50,
  },
  {
    title: '总数',
    dataIndex: 'total',
    align: 'center',
    width: 50,
  },

];

export const typeColumns: BasicColumn[] = [
  {
    //产线
    title: '问题类型',
    dataIndex: 'type',
    align: 'center',
    resizable: true,
    width: 100,
  },
  {
    title: '一月',
    dataIndex: 'jan',
    align: 'center',
    width: 50,
  },
  {
    title: '二月',
    dataIndex: 'feb',
    align: 'center',
    width: 50,
  },
  {
    title: '三月',
    dataIndex: 'mar',
    align: 'center',
    width: 50,
  },
  {
    title: '四月',
    dataIndex: 'apr',
    align: 'center',
    width: 50,
  },
  {
    title: '五月',
    dataIndex: 'may',
    align: 'center',
    width: 50,
  },
  {
    title: '六月',
    dataIndex: 'jun',
    align: 'center',
    width: 50,
  },
  {
    title: '七月',
    dataIndex: 'jul',
    align: 'center',
    width: 50,
  },
  {
    title: '八月',
    dataIndex: 'aug',
    align: 'center',
    width: 50,
  },
  {
    title: '九月',
    dataIndex: 'sep',
    align: 'center',
    width: 50,
  },
  {
    title: '十月',
    dataIndex: 'oct',
    align: 'center',
    width: 50,
  },
  {
    title: '十一月',
    dataIndex: 'nov',
    align: 'center',
    width: 50,
  },
  {
    title: '十二月',
    dataIndex: 'dec',
    align: 'center',
    width: 50,
  },
  {
    title: '总数',
    dataIndex: 'total',
    align: 'center',
    width: 50,
  },

];

//列表页面查询表单
export const searchFormSchema: FormSchema[] = [
  {
    label: t('language.department'),
    field: 'deptCode',
    component: 'Input',
    slot:'deptCode',
    colProps: { span: 6 },
  },
  {
    label: t('language.productLine'),
    field: 'lineCode',
    component: 'Input',
    slot:'lineCode',
    colProps: { span: 7 },
  },
  {
    label: '日期范围',
    field: 'times',
    component: 'RangeDate',
    componentProps: {
      //日期格式化
      format:'YYYY/MM/DD',
      //范围文本描述用集合
      placeholder:['请选择开始日期','请选择结束日期']
    },
    colProps: { span: 7 },
  },
  {
    label: '触发条件',
    field: 'triggerConditions',
    component: 'JDictSelectTag',
    componentProps: {
      //字典code配置，比如通过性别字典编码：sex，也可以使用demo,name,id 表名,名称,值的方式
      dictCode:'mes3_mdm_dict, item_key, item_val, del_flag = 0 and group_code = \'MES3_EXCEPTION_TRRIGER_CONDITION\'',
    },
    colProps: {
      span: 6,
    },
  }
];



