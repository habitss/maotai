import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
//列表数据
export const columns: BasicColumn[] = []
export const modifyColumns: BasicColumn[] = [

  // {
  //   title: '部门Code',
  //   align: "center",
  //   dataIndex: 'deptCode'
  // },
  {
    title: '报表名称',
    align: "center",
    dataIndex: 'allocationIdVal',
    customRender:function (text,r,index) {
      //基础报表名称
      let allocationIdVal = text.record.allocationIdVal;
      let typeStr = "";
      if(!allocationIdVal){
        typeStr = text.record.tableIdVal;
      }else{
        //pi报表名称
        typeStr = text;
      }
      return typeStr;
    }
  },
  {
    title: '唯一约束',
    align: "center",
    dataIndex: 'uniqueStr',
    width: 280,
  },
  {
    title: '原始值',
    align: "center",
    dataIndex: 'originalValue'
  },
  {
    title: '修改值',
    align: "center",
    dataIndex: 'modifyValue'
  },
  {
    title: '修改原因',
    align: "center",
    dataIndex: 'reason'
  },
  {
    title: '修改字段',
    align: "center",
    dataIndex: 'fieldName'
  },
  {
    title: '修改时间',
    align: "center",
    dataIndex: 'modifyTime'
  },
  {
    title: '修改人',
    align: "center",
    dataIndex: 'modifyByVal'
  },
  {
    title: '审批状态',
    align: "center",
    dataIndex: 'stateVal',
    customCell:(record,rowIndex)=>{
      if(record.state =='1'){
        return {style:{color:"green"}}
      }else if(record.state =='2'){
        return {style:{color:"red"}}
      }
      return {style:{color:"#dfc200"}}
    }
  },
  {
    title: '审批人',
    align: "center",
    dataIndex: 'approveByVal'
  },
  {
    title: '审批时间',
    align: "center",
    dataIndex: 'approveTime'
  }
];

export const removeColumns: BasicColumn[] = [

  // {
  //   title: '部门Code',
  //   align: "center",
  //   dataIndex: 'deptCode'
  // },
  {
    title: '报表名称',
    align: "center",
    dataIndex: 'allocationIdVal',
    customRender:function (text,r,index) {
      //基础报表名称
      let tableName = text.tableIdVal;
      let typeStr = "";
      if(!text){
        typeStr = tableName;
      }else{
        //pi报表名称
        typeStr = text;
      }
      return typeStr;
    }
  },
  {
    title: '唯一约束',
    align: "center",
    dataIndex: 'uniqueStr',
    width: 280,
  },
  {
    title: '剔除原因',
    align: "center",
    dataIndex: 'reason'
  },
  {
    title: '剔除字段',
    align: "center",
    dataIndex: 'fieldName'
  },
  {
    title: '剔除时间',
    align: "center",
    dataIndex: 'modifyTime'
  },
  {
    title: '剔除人',
    align: "center",
    dataIndex: 'modifyByVal'
  },
  {
    title: '剔除分类',
    align: "center",
    dataIndex: 'removeCategoryVal'
  },
  {
    title: '剔除范围',
    align: "center",
    dataIndex: 'removeRange',
    customRender:function (text,r,index) {
      let typeStr = "";
      if(text.value =='0'){
        typeStr = "整行";
      }else{
        typeStr = "当前PI";
      }
      return typeStr;
    }
  },
  {
    title: '审批状态',
    align: "center",
    dataIndex: 'stateVal',
    customCell:(record,rowIndex)=>{
      if(record.state =='1'){
        return {style:{color:"green"}}
      }else if(record.state =='2'){
        return {style:{color:"red"}}
      }
      return {style:{color:"#dfc200"}}
    }
  },
  {
    title: '审批人',
    align: "center",
    dataIndex: 'approveByVal'
  },
  {
    title: '审批时间',
    align: "center",
    dataIndex: 'approveTime'
  }
];



// 高级查询数据
export const superQuerySchema = {
  tenantId: {title: '租户ID',order: 0,view: 'number', type: 'number',},
  factoryCode: {title: '工厂Code',order: 1,view: 'text', type: 'string',},
  deptCode: {title: '部门Code',order: 2,view: 'text', type: 'string',},
  lineCode: {title: '产线Code',order: 3,view: 'text', type: 'string',},
  tableId: {title: '基础报表id',order: 4,view: 'number', type: 'number',},
  allocationId: {title: 'pi报表id',order: 5,view: 'number', type: 'number',},
  dataId: {title: 'pi数据id',order: 6,view: 'number', type: 'number',},
  tableName: {title: '报表名称(表名)',order: 7,view: 'text', type: 'string',},
  originalValue: {title: '原始值',order: 8,view: 'text', type: 'string',},
  modifyValue: {title: '修改值',order: 9,view: 'text', type: 'string',},
  reason: {title: '修改/剔除原因',order: 10,view: 'text', type: 'string',},
  fields: {title: '修改字段/剔除字段(多个)',order: 11,view: 'textarea', type: 'string',},
  modifyTime: {title: '修改/剔除时间',order: 12,view: 'datetime', type: 'string',},
  modifyBy: {title: '修改/剔除修改人',order: 13,view: 'text', type: 'string',},
  removeCategory: {title: '剔除分类(0.试验批/1.停产/2.停机/3.设备故障)',order: 14,view: 'text', type: 'string',},
  state: {title: '审批状态 (0.通过/1.通过/2.不通过)',order: 15,view: 'text', type: 'string',},
  approveBy: {title: '审批人',order: 16,view: 'text', type: 'string',},
  approveTime: {title: '审批时间',order: 17,view: 'datetime', type: 'string',},
  type: {title: '类型(0.修改/1.剔除)',order: 18,view: 'text', type: 'string',},
  sort: {title: '排序',order: 19,view: 'number', type: 'number',},
};
