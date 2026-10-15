import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
import { list } from '/@/views/dataCalculation/kpi/type/KpiType.api';
//列表数据
export const columns: BasicColumn[] = [
  // {
  //   title: '年份',
  //   align: "center",
  //   dataIndex: 'year'
  // },
  {
    title: 'KPI名称',
    align: "left",
    width:260,
    dataIndex: 'kpiName',
    fixed: 'left'
  },
  {
    title: 'KPICode',
    align: "center",
    dataIndex: 'kpiCode'
  },
  {
    title: '所属年份',
    align: "center",
    dataIndex: 'year'
  },
  //填写部门
  {
    title: '填写部门',
    align: "center",
    dataIndex: 'inputDeptName'
  },
  //查看层级部门
  {
    title: '查看层级部门',
    align: "center",
    dataIndex: 'deptName'
  },
  {
    title: 'KPI分类',
    align: "center",
    dataIndex: 'kpiTypeName'
  },
  {
    title: '单位',
    align: "center",
    dataIndex: 'unit'
  },
  {
    title: '第三方接口指标名称',
    align: "center",
    dataIndex: 'thirdName'
  },
  //红区判断条件
  {
    title: '红区判断条件',
    align: "center",
    dataIndex: 'compareValue',
    customRender:function (text) {

      let t = text.value;
      let typeStr = "";
      if(t == '1'){
        typeStr = ">(大于)";
      }else if(t == '0'){
        typeStr = "<(小于)";
      }else if(t == '2'){
        typeStr = "范围";
      }else if(t == '3'){
        typeStr = "比例";
      }else{
        typeStr = "不计算";
      }
      return typeStr;
    },
  },
  {
    title: '是否自动采集',
    align: "center",
    dataIndex: 'active',
    customRender: function (val) {
      if (val.text === 1) {
        return '自动'
      } else {
        return '手动'
      }
    }
  },
  {
    title: '是否进入柏拉图',
    align: "center",
    dataIndex: 'plato',
    customRender: function (val) {
      if (val.text === 1) {
        return '是'
      } else {
        return '否'
      }
    }
  },
  {
    title: '是否显示周/月报',
    align: "center",
    dataIndex: 'visible',
    customRender: function (val) {
      if (val.text === 1) {
        return '是'
      } else {
        return '否'
      }
    }
  },
  {
    title: '是否在填写中展示',
    align: "center",
    dataIndex: 'visibleWrite',
    customRender: function (val) {
      if (val.text === 1) {
        return '是'
      } else {
        return '否'
      }
    }
  },
  {
    title: '是否可用',
    align: "center",
    dataIndex: 'activeLook',
    customRender: function (val) {
      if (val.text === 1) {
        return '是'
      } else {
        return '否'
      }
    }
  },
  {
    title: '是否是重点关注KPI',
    align: "center",
    dataIndex: 'emphasisType',
    customRender: function (val) {
      if (val.text === 1) {
        return '是'
      } else {
        return '否'
      }
    }
  },
  {
    title: '备注',
    align: "center",
    dataIndex: 'text'
  },
  {
    title: '保留小数位数',
    align: "center",
    dataIndex: 'decimalNum'
  },
  {
    title: '填报排序',
    align: "center",
    dataIndex: 'inputSort'
  },
  {
    title: '报表排序',
    align: "center",
    dataIndex: 'reportSort'
  }
  
];


//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: "KPI分类",
    field: "id",
    component: 'ApiTreeSelect',
    componentProps: {
      api: list,
      params: { async: false, parentId: '' },
      fieldNames: { label: 'kpiType', value: 'id' },
      showSearch: true,
      treeNodeFilterProp: 'kpiType',
    },
    //colProps: {span: 6},
  },
];

