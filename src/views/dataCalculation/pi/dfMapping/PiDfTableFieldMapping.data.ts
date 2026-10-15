import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { list as dfTableListApi } from '/@/views/df/mdm/DfTableInfo.api';
import { list as dfTableFieldListApi } from '/@/views/df/mdm/DfTableFieldInfo.api';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '租户ID',
    align: "center",
    dataIndex: 'tenantId'
  },
  {
    title: '工厂编码',
    align: "center",
    dataIndex: 'factoryCode'
  },
  {
    title: '部门编码',
    align: "center",
    dataIndex: 'deptCode'
  },
  {
    title: '产线编码',
    align: "center",
    dataIndex: 'lineCode'
  },
  {
    title: '数据库表id',
    align: "center",
    dataIndex: 'tableId'
  },
  {
    title: '数据库表名',
    align: "center",
    dataIndex: 'tableName'
  },
  {
    title: '表字段id',
    align: "center",
    dataIndex: 'fieldId'
  },
  {
    title: '表字段名',
    align: "center",
    dataIndex: 'fieldName'
  },
  {
    title: 'df数据库表id',
    align: "center",
    dataIndex: 'dfTableId'
  },
  {
    title: 'df数据库表名',
    align: "center",
    dataIndex: 'dfTableName'
  },
  {
    title: 'df表字段id',
    align: "center",
    dataIndex: 'dfFieldId'
  },
  {
    title: 'df表字段名',
    align: "center",
    dataIndex: 'dfFieldName'
  },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
];
//映射表单数据
export const mappingFormSchema: FormSchema[] = [
  {
    label: 'DF数据库表名',
    field: 'dfTableNameSelect',
    component: 'ApiSelect',
    componentProps: ({ formModel }) => {
      return {
        api: dfTableListApi,
        params: { 
          tableId: formModel.dfTableId,
          pageNo: 1,
          pageSize: 500
        },
        resultField: 'records',
        labelField: 'dfTableDesc',
        valueField: 'id',
        labelInValue: true,
        showSearch: true,
        filterOption: (input, option) => {
          return option.value.toLowerCase().indexOf(input.toLowerCase()) >= 0 || option?.label?.indexOf(input) >= 0;
        },
        getPopupContainer: () => document.body,
        onChange: (e) => {
          formModel.dfTableId = e.option.value;
          formModel.dfTableName = e.option.dfTableName;
          formModel.dfFieldNameSelect = null;
          formModel.dfFieldId = null;
          formModel.dfFieldName = null;
        }
      }
    },
    required: true,
  },
  {
    label: 'DF表字段名',
    field: 'dfFieldNameSelect',
    component: 'ApiSelect',
    componentProps: ({ formModel }) => {
      return {
        api: dfTableFieldListApi,
        params: { 
          tableId: formModel.dfTableId,
          pageNo: 1,
          pageSize: 500
        },
        resultField: 'records',
        labelField: 'dfFieldDesc',
        valueField: 'dfFieldName',
        labelInValue: true,
        showSearch: true,
        filterOption: (input, option) => {
          return option.value.toLowerCase().indexOf(input.toLowerCase()) >= 0 || option?.label?.indexOf(input) >= 0;
        },
        getPopupContainer: () => document.body,
        onChange: (e) => {
          formModel.dfFieldId = e.option.id;
          formModel.dfFieldName = e.option.value;
        }
      }
    },
    required: true,
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
    label: '部门编码',
    field: 'deptCode',
    component: 'Input',
    show: false
  },
  {
    label: '产线编码',
    field: 'lineCode',
    component: 'Input',
    show: false
  },
  {
    label: '数据库表id',
    field: 'tableId',
    component: 'Input',
    show: false
  },
  {
    label: '数据库表名',
    field: 'tableName',
    component: 'Input',
    show: false
  },
  {
    label: '表字段id',
    field: 'fieldId',
    component: 'Input',
    show: false
  },
  {
    label: '表字段名',
    field: 'fieldName',
    component: 'Input',
    show: false
  },
  {
    label: 'df数据库表id',
    field: 'dfTableId',
    component: 'Input',
    show: false
  },
  {
    label: 'DF数据库表名',
    field: 'dfTableName',
    component: 'Input',
    show: false
  },
  {
    label: 'df表字段id',
    field: 'dfFieldId',
    component: 'Input',
    show: false
  },
  {
    label: 'DF表字段名',
    field: 'dfFieldName',
    component: 'Input',
    show: false
  },
];
