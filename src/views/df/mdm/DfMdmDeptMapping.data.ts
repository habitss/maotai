import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '部门类型',
    align: "center",
    dataIndex: 'deptType',
    width: 100,
    customRender: ({ record }) => {
      switch (record.deptType) {
        case '10':
          return '工厂';
        case '20':
          return '部门';
        case '30':
          return '产线';
      }
    }
  },
  {
    title: 'MDM部门名称',
    align: "left",
    dataIndex: 'mdmFullName'
  },
  {
    title: 'MDM部门编码',
    align: "left",
    dataIndex: 'mdmFullCode'
  },
  {
    title: '系统部门名称',
    align: "left",
    dataIndex: 'sysDeptName'
  },
  {
    title: '系统部门编码',
    align: "left",
    dataIndex: 'sysDeptCode'
  },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: '部门类型',
    field: 'deptType',
    component: 'Select',
    componentProps: {
      options: [
        {
          label: '工厂',
          value: '10',
        },
        {
          label: '部门',
          value: '20',
        },
        {
          label: '产线',
          value: '30',
        },
      ],
      allowClear: true,
    },
  },
  {
    label: 'MDM部门名称',
    field: 'mdmFullName',
    component: 'JInput',
  },
  {
    label: '系统部门名称',
    field: 'sysDeptName',
    component: 'JInput',
  },
  {
    label: '是否映射',
    field: 'isMapping',
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
      ],
      allowClear: true,
    },
  },
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: 'MDM部门名称',
    field: 'mdmFullName',
    component: 'Input',
    componentProps: {
      disabled: true,
    }
  },
  {
    label: 'MDM部门编码',
    field: 'mdmFullCode',
    component: 'Input',
    componentProps: {
      disabled: true,
    }
  },
  {
    label: '系统部门名称',
    field: 'sysDeptNameSelect',
    component: 'KtDepartTreeSelect',
    componentProps: ({ formModel }) => {
      return {
        defaultExpandLevel: 2,
        showAll: true,
        fieldNames: { label: 'title', value: 'orgCode' },
        getPopupContainer: () => document.body,
        labelInValue: true,
        onChange: (value) => {
          formModel.sysDeptName = value.label;
          formModel.sysDeptCode = value.value;
        }
      }
    },
    required: true,
  },
  {
    label: '系统部门名称',
    field: 'sysDeptName',
    component: 'Input',
    show: false
  },
  {
    label: '系统部门编码',
    field: 'sysDeptCode',
    component: 'Input',
    componentProps: {
      disabled: true,
    }
  },
  // TODO 主键隐藏字段，目前写死为ID
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false
  },
];