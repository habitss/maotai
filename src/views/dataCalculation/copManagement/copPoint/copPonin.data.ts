import { ref } from 'vue';
import { BasicColumn, FormSchema } from '/@/components/Table';
import { mes3UserListAll } from '@/api/common/api';
import {getPopupContainer} from "@/utils";

// 用户下拉框数据
let USER_LIST = ref([]);
(async function f() {
  USER_LIST.value = await mes3UserListAll();
})();

//下拉选择过滤方法
function filterOption(input, option) {
  return option.realname.toLowerCase().indexOf(input.toLowerCase()) >= 0;
};


//列表表头配置
export const columns: BasicColumn[] = [
  {
    title: 'id',
    dataIndex: 'id',
    defaultHidden: true,
  },
  {
    title: '所属设备',
    align: 'center',
    dataIndex: 'copDeviceIdVal',
    width:100,
  },
  {
    title: '清理点名称',
    align: 'center',
    dataIndex: 'pointName',
    ellipsis:false,
    width:150,
  },
  {
    title: '清洗标准',
    align: 'center',
    dataIndex: 'cleaningStandard',
    ellipsis:false,
  },
  {
    title: '清洗步骤',
    align: 'center',
    dataIndex: 'cleaningStep',
    ellipsis:false,
  },
  {
    title: '频次',
    align: 'center',
    dataIndex: 'frequencyVal',
    width:80,
  },
  {
    title: '是否可用',
    align:"center",
    dataIndex: 'active',
    width:80,
    customRender:function ({ record }) {
      return String(record.active) === '1' ? "可用" : "不可用";
    }
  },
  {
    title: '顺序',
    align:"center",
    width: 60,
    dataIndex: 'sort',
  },

];

//列表页面查询表单
export const searchFormSchema: FormSchema[] = [
  {
    label: '清洗点',
    field: 'pointName',
    component: 'Input',
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
      colProps: { span: 24 },
    },
    {
      label: '设备名称',
      field: 'copDeviceName',
      component: 'Input',
      dynamicDisabled: () => {
        return true;
      },
      colProps: { span: 11 },
      required: true,
    },
    {
        label: '清洗点名称',
        field: 'pointName',
        component: 'Input',
        colProps: { span: 11 },
        required: true,
    },
    {
        label: '温度',
        field: 'temperature',
        component: 'JDictSelectTag',
        componentProps: {
          //字典code配置，比如通过性别字典编码：sex，也可以使用demo,name,id 表名,名称,值的方式
          dictCode:'mes3_mdm_dict, item_key, item_val, del_flag = 0 and group_code = \'MES3_COP_CONFIG_TEMPERATURE\'',
        },
        colProps: { span: 11 },
    },
    {
        label: '时间',
        field: 'timeValue',
        component: 'Input',
        colProps: { span: 11 },
    },
    {
        label: '推荐配方',
        field: 'formula',
        component: 'JDictSelectTag',
        componentProps: {
          getPopupContainer:() => getPopupContainer(),
          showSearch: true,
          placement: 'bottomRight',
        //字典code配置，比如通过性别字典编码：sex，也可以使用demo,name,id 表名,名称,值的方式
          dictCode:'mes3_mdm_dict, item_key, item_val, del_flag = 0 and group_code = \'MES3_COP_RECOMMENDED_FORMULA\'',
        },
        colProps: { span: 11 },
    },
    {
        label: '频次',
        field: 'frequency',
        component: 'JDictSelectTag',
        componentProps: {
        //字典code配置，比如通过性别字典编码：sex，也可以使用demo,name,id 表名,名称,值的方式
        dictCode:'mes3_mdm_dict, item_key, item_val, del_flag = 0 and group_code = \'MES3_COP_CONFIG_FREQUENCY\'',
      },
        colProps: { span: 11 },
      required: true,
    },
    {
        label: '顺序',
        field: 'sort',
        component: 'InputNumber',
        // labelWidth: 140,
        // labelLength: 8,
         colProps: { span: 6 },
    },
    {
        label: '清洗人',
        field: 'clearBy',
        colProps: { span: 7 },
        component: 'Select',
        componentProps: {
          options: USER_LIST,
          fieldNames: { label: 'realname', value: 'id' },
          // maxTagCount: 2,
          // mode: 'multiple',
          getPopupContainer:() => getPopupContainer(),
          mode:'multiple',
          allowClear: true,
          placement: 'bottomRight',
          showSearch: true,
          filterOption,
        },
    },
    {
        label: '检查人',
        field: 'checkBy',
        colProps: { span: 7 },
        component: 'Select',
        componentProps: {
          options: USER_LIST,
          fieldNames: { label: 'realname', value: 'id' },
          // maxTagCount: 2,
          //  mode: 'multiple',
          getPopupContainer:() => getPopupContainer(),
          mode:'multiple',
          allowClear: true,
          placement: 'bottomRight',
          showSearch: true,
          filterOption,
        },
    },
    {
        label: '清洗标准',
        field: 'cleaningStandard',
        component: 'InputTextArea',
        colProps: { span: 24 },
        required: true,
    },
    {
        label: '清洗步骤',
        field: 'cleaningStep',
        component: 'InputTextArea',
        colProps: { span: 24 },
        required: true,
    },
  {
    label: '是否可用',
    field: 'active',
    component: 'Switch',
    componentProps: {
      //非选中时的内容
      unCheckedChildren:'否',
      //非选中时的值
      unCheckedValue: 0,
      //选中时的内容
      checkedChildren:'是',
      //选中时的值
      checkedValue: 1,
    },
    defaultValue: 1,
  },
  ];

export const columnsConfig: BasicColumn[] = [
  {
    title: 'id',
    dataIndex: 'id',
    defaultHidden: true,
  },
  // {
  //   title: '清理点名称',
  //   align: 'center',
  //   dataIndex: 'copPointIdVal',
  //   width:200,
  // },
  {
    title: '字段名称',
    align: 'center',
    dataIndex: 'fieldName',
    width:200,
  },
  {
    title: '字段类型',
    align: 'center',
    dataIndex: 'fieldTypeVal',
    width:200,
  },
  {
    title: '顺序',
    align:"center",
    width: 100,
    dataIndex: 'sort',
  },
];

//列表页面查询表单
export const searchConfigFormSchema: FormSchema[] = [
  {
    label: '清洗点',
    field: 'pointName',
    component: 'Input',
    colProps: {
      span: 6,
    },
  }
];

export const addConfigFormSchemas: FormSchema[] = [
  {
    label: '主键',
    field: 'id',
    component: 'Input',
    show: false,
    colProps: { span: 24 },
  },
  // {
  //   label: '清洗点名称',
  //   field: 'copPointName',
  //   component: 'Input',
  //   colProps: { span: 24 },
  //   required: true,
  // },
  {
    label: '字段名称',
    field: 'fieldName',
    component: 'Input',
    colProps: { span: 20 },
    required: true,
  },
  {
    label: '字段类型',
    field: 'fieldType',
    component: 'Select',
    componentProps: {
      options: [
        {
          label: '时间',
          value: '1',
        },
        {
          label: '日期',
          value: '2',
        },
        {
          label: '输入框',
          value: '3',
        },
        {
          label: '附件上传',
          value: '4',
        },
      ],
    },
    colProps: { span: 20 },
    required: true,
  },
  {
    label: '顺序',
    field: 'sort',
    component: 'InputNumber',
    colProps: { span: 20 },
  },
];
