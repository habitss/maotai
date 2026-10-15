import { BasicColumn, FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
const { t } = useI18n();

//列表表头配置
export const columns: BasicColumn[] = [
        {
          title: '评估类型',
          align: "center",
          dataIndex: 'recordType',
          width: 80,
        },
        {
          title: '排序',
          align: "center",
          dataIndex: 'sort',
          width: 60,
        },
        {
          title: '内容',
          align: "center",
          dataIndex: 'content',
          ellipsis:false,
          width: 250,
        },
        {
          title: '评分标准',
          align: "center",
          dataIndex: 'standard',
          ellipsis:false,
          width: 300,
        },
        {
          title: '权重',
          align: "center",
          dataIndex: 'weight',
          width: 100,
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
    label: '会议类型',
    field: 'meetingType',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "mes3_meeting_config where del_flag = '0', meeting_type, id",
      type: 'select',
      labelField: 'item_key',
      valueField: 'item_val',
    },
    colProps: { span: 6 },
    defaultValue: ''
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
    label: '评估类型',
    field: 'recordType',
    component: 'Select',
    componentProps: {
      options: [
        {
          label: '会议内容',
          value: '1',
        },
        {
          label: '会议效果',
          value: '2',
        },
      ],
    },
    colProps: { span: 12 },
    required: true,
  },
    {
      label: '内容',
      field: 'content',
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
          minRows: 2,
          //最大显示行数
          maxRows: 3
        },
      }
    },
    {
      label: '评分标准',
      field: 'standard',
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
          minRows: 2,
          //最大显示行数
          maxRows: 3
        },
      }
    },
    {
      label: '权重',
      field: 'weight',
      component: 'InputNumber',
      required: true,
      // labelWidth: 140,
      // labelLength: 8,
      // colProps: { span: 24 },
    }, {
    label: '排序',
    field: 'sort',
    component: 'InputNumber',
    required: true,
    // labelWidth: 140,
    // labelLength: 8,
    // colProps: { span: 24 },
  },
  ];
