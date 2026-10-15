import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
const { t } = useI18n();

//列表表头配置
export const columns: BasicColumn[] = [
  {
    title: 'id',
    dataIndex: 'id',
    defaultHidden: true,
  },
  {
    title: '异常报告编号',
    align: 'center',
    dataIndex: 'reportCode',
  },
  {
    //部门
    title: t('language.department'),
    dataIndex: 'deptCode_dictText',
    align: 'center',
    resizable: true,
  },
  {
    //产线
    title: t('language.productLine'),
    dataIndex: 'lineCode_dictText',
    align: 'center',
    resizable: true,
  },

  {
    title: '异常触发条件',
    align:"center",
    ellipsis:false,
    dataIndex: 'triggerConditionsVal',
  },
  {
    title: '时间',
    align:"center",
    width: 100,
    ellipsis:false,
    dataIndex: 'exceptionTime',
  },
  {
    title: '报告',
    align:"center",
    width: 100,
    ellipsis:false,
    dataIndex: 'reportFile',
  },
  {
    title: '异常报告名称',
    align:"center",
    width: 100,
    ellipsis:false,
    dataIndex: 'reportName',
  },
  {
    title: '问题类型',
    align:"center",
    width: 60,
    dataIndex: 'exceptionType',
    customRender:function ({ record }) {
      if (record.exceptionType == 1) {
        return "操作问题"
      } else if (record.exceptionType == 2) {
        return "质量问题"
      } else if (record.exceptionType == 3) {
        return "维护问题"
      }
    }
  },
  {
    title: '异常描述',
    align:"center",
    ellipsis:false,
    dataIndex: 'exception',
  },
  {
    title: '影响到什么产品',
    align:"center",
    width: 70,
    ellipsis:false,
    dataIndex: 'effectProduct',
  },
  {
    title: '损失',
    align:"center",
    width: 70,
    ellipsis:false,
    dataIndex: 'loss',
  },
  {
    title: '停机时间',
    align:"center",
    width: 50,
    dataIndex: 'stopTime',
  },
  {
    title: '报告查看',
    align:"center",
    fixed: "right",
    width: 120,
    dataIndex: 'reportPath',
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
      showSearch: true,
      //字典code配置，比如通过性别字典编码：sex，也可以使用demo,name,id 表名,名称,值的方式
      dictCode:'mes3_mdm_dict, item_key, item_val, del_flag = 0 and group_code = \'MES3_EXCEPTION_TRRIGER_CONDITION\'',
    },
    colProps: {
      span: 6,
    },
  }
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
      //部门
      label: t('language.department'),
      field: 'deptCode',
      component: 'Input',
      slot:'deptCode',
      required: true,
      colProps: { span: 11 },
    },
    {
      //产线
      label: t('language.productLine'),
      field: 'lineCode',
      component: 'Input',
      slot:'lineCode',
      colProps: { span: 11 },
    },
    {
      label: '报告编号',
      field: 'reportCode',
      component: 'Input',
      required: true,
      colProps: { span: 11 },
    },
    {
      label: '报告名称',
      field: 'reportName',
      component: 'Input',
      required: true,
      colProps: { span: 11 },
    },
    {
      label: '触发条件',
      field: 'triggerConditions',
      component: 'JDictSelectTag',
      componentProps: {
        showSearch: true,
        //字典code配置，比如通过性别字典编码：sex，也可以使用demo,name,id 表名,名称,值的方式
        dictCode:'mes3_mdm_dict, item_key, item_val, del_flag = 0 and group_code = \'MES3_EXCEPTION_TRRIGER_CONDITION\'',
      },
      required: true,
      colProps: { span: 11 },
    },
    {
      label: '日期选择',
      field: 'exceptionTime',
      component: 'DatePicker',
      componentProps: {
        showTime:true,
        //日期格式化，页面上显示的值
        format: 'YYYY-MM-DD HH:mm:ss',
        //返回值格式化（绑定值的格式）
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        //是否显示今天按钮
        showToday: true,
      },
      required: true,
      colProps: { span: 11 },
    },
    {
      label: '报告上传',
      field: 'reportFile',
      component: 'JUpload',
      componentProps: {
        //是否显示选择按钮
        text: '文件上传',
        //最大上传数
        maxCount: 2,
        //是否显示下载按钮
        download: true,
      },
    },
  {
    label: '问题类型',
    field: 'exceptionType',
    component: 'Select',
    colProps: { span: 11 },
    componentProps: {
      options: [
        {
          label: '操作问题',
          value: 1,
        },
        {
          label: '质量问题',
          value: 2,
        },
        {
          label: '维护问题',
          value: 3,
        },
      ],
    },
    required: true,
  },
    {
      label: '异常描述',
      field: 'exception',
      component: 'Input',
      required: true,
    },
  {
    label: '影响到什么产品',
    field: 'effectProduct',
    component: 'Input',
    colProps: { span: 11 },
  },
  {
    label: '损失',
    field: 'loss',
    component: 'Input',
    colProps: { span: 11 },
  },
  {
    label: '停机时间',
    field: 'stopTime',
    component: 'InputNumber',
    colProps: { span: 11 },
  },
  {
    label: '被替代的设备',
    field: 'replaceDevice',
    component: 'Input',
    colProps: { span: 11 },
  },
  ];
