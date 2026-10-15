import { BasicColumn, FormSchema } from '/@/components/Table';
import { getColor} from '/@/utils/common/piColorUtils';

//标准维护列表表头配置
export const columns: BasicColumn[] = [
  {
    title: 'id',
    dataIndex: 'id',
    defaultHidden: true,
  },
  {
    title: 'PI名称',
    dataIndex: 'fieldName',
    align:"center",
    editComponent: 'Select',
    editRow: true,
  },
  {
    title: '指标类型',
    dataIndex: 'indicatorType',
    align:"center",
    width: 140,
    editComponent: 'Select',
    editComponentProps: {
      getPopupContainer: (node) => document.body,
      options:[
        { value: '1', label: '实际值' },
        { value: '2', label: '偏差值' },
      ]
    },
    editValueMap: (value) => {
      return value ? value==1 ? '实际值' : '偏差值' : '';
    },
    editRow: true,
  },
  {
    title: '红下',
    dataIndex: 'redDown',
    align:"center",
    width: 140,
    editRow: true,
    customHeaderCell: function(column){
      return {
        style: {
          backgroundColor: getColor('red'),
        }
      };
    },
    editValueMap: (value) => {
      return value+'';
    },
  },
  {
    title: '黄下',
    dataIndex: 'yellowDown',
    align:"center",
    width: 140,
    editRow: true,
    customHeaderCell: function(column){
      return {
        style: {
          backgroundColor: getColor('yellow'),
        }
      };
    },
    editValueMap: (value) => {
      return value+'';
    },
  },
  {
    title: '目标值',
    dataIndex: 'standardValue',
    align:"center",
    width: 140,
    editRow: true,
    customHeaderCell: function(column){
      return {
        style: {
          backgroundColor: getColor('green'),
        }
      };
    },
    editValueMap: (value) => {
      return value+'';
    },
  },
  {
    title: '黄上',
    dataIndex: 'yellowUp',
    align:"center",
    width: 140,
    editRow: true,
    customHeaderCell: function(column){
      return {
        style: {
          backgroundColor: getColor('yellow'),
        }
      };
    },
    editValueMap: (value) => {
      return value+'';
    },
  },
  {
    title: '红上',
    dataIndex: 'redUp',
    align:"center",
    width: 140,
    editRow: true,
    customHeaderCell: function(column){
      return {
        style: {
          backgroundColor: getColor('red'),
        }
      };
    },
    editValueMap: (value) => {
      return value+'';
    },
  },
];



//PI日报-标准维护列表表头配置
export const piColumns: BasicColumn[] = [
  {
    title: 'id',
    dataIndex: 'id',
    defaultHidden: true,
  },
  // {
  //   title: 'PI名称',
  //   dataIndex: 'fieldText',
  //   align:"center",
  //   editComponent: 'Select',
  //   editRow: true,
  // },
  {
    title: '标准名称',
    align:"center",
    dataIndex: 'standardName',
    width: 200,
  },
 
  // {
  //   title: '版本',
  //   align:"center",
  //   dataIndex: 'standardVersion',
  //   width: 60,
  // },
  // {
  //   title: '排序',
  //   align:"center",
  //   dataIndex: 'sort',
  //   width: 60,
  // },
  {
    title: '重算时间对照字段',
    align:"center",
    dataIndex: 'calcTimeFieldName',
    width: 140,
  },
  {
    title: '条件字段',
    align:"center",
    dataIndex: 'conditionText',
    width: 260,
  },

  {
    title: '指标类型',
    dataIndex: 'indicatorType',
    align:"center",
    width: 140,
    editComponent: 'Select',
    editComponentProps: {
      getPopupContainer: (node) => document.body,
      options:[
        { value: '1', label: '实际值' },
        { value: '2', label: '偏差值' },
      ]
    },
    editValueMap: (value) => {
      return value ? value==1 ? '实际值' : '偏差值' : '';
    },
    editRow: true,
  },
  {
    title: '红下',
    dataIndex: 'redDown',
    align:"center",
    width: 100,
    editRow: true,
    customHeaderCell: function(column){
      return {
        style: {
          backgroundColor: getColor('red'),
        }
      };
    },
    editValueMap: (value) => {
      return value+'';
    },
  },
  {
    title: '黄下',
    dataIndex: 'yellowDown',
    align:"center",
    width: 100,
    editRow: true,
    customHeaderCell: function(column){
      return {
        style: {
          backgroundColor: getColor('yellow'),
        }
      };
    },
    editValueMap: (value) => {
      return value+'';
    },
  },
  {
    title: '目标值',
    dataIndex: 'standardValue',
    align:"center",
    width: 100,
    editRow: true,
    customHeaderCell: function(column){
      return {
        style: {
          backgroundColor: getColor('green'),
        }
      };
    },
    editValueMap: (value) => {
      return value+'';
    },
  },
  {
    title: '黄上',
    dataIndex: 'yellowUp',
    align:"center",
    width: 100,
    editRow: true,
    customHeaderCell: function(column){
      return {
        style: {
          backgroundColor: getColor('yellow'),
        }
      };
    },
    editValueMap: (value) => {
      return value+'';
    },
  },
  {
    title: '红上',
    dataIndex: 'redUp',
    align:"center",
    width: 100,
    editRow: true,
    customHeaderCell: function(column){
      return {
        style: {
          backgroundColor: getColor('red'),
        }
      };
    },
    editValueMap: (value) => {
      return value+'';
    },
  },
  {
    title: '启用时间',
    align:"center",
    dataIndex: 'enableTime',
    width: 100,
  },
  {
    title: '停用时间',
    align:"center",
    dataIndex: 'stopTime',
    width: 100,
  },
];