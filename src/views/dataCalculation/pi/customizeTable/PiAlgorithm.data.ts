import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
//列表数据
export const columns: BasicColumn[] = [
    // {
    //   title: 'PI编码',
    //   align:"center",
    //   dataIndex: 'piCode'
    //  },
    //  {
    //   title: 'PI名称',
    //   align:"center",
    //   dataIndex: 'piName'
    //  },
    {
      title: '点位类型',
      align:"center",
      dataIndex: 'pointType',
      customRender:function ({ record }) {
        if(record.pointType === 0){
            return "自动化点位";
        }else if(record.pointType === 1){
            return "时间";
        }else{
            return "";
        }
      }
     },
     {
      title: '基础点位代码',
      align:"center",
      dataIndex: 'pointCode'
     },
     {
        title: '基础点位名称',
        align:"center",
        dataIndex: 'pointCode_dictText'
    },
    // {
    //     title: '计算类型',
    //     align:"center",
    //     dataIndex: 'calculateType'
    // },
    // {
    //     title: '计算值',
    //     align:"center",
    //     dataIndex: 'calculateValue'
    // },
    {
        title: '说明',
        align:"center",
        dataIndex: 'explanation'
    },
    {
      title: '是否启用',
      align:"center",
      dataIndex: 'isEnabled',
      customRender:function ({ record }) {
        if(record.isEnabled === 0){
            return "禁用";
        }else if(record.isEnabled === 1){
            return "启用";
        }else{
            return "";
        }
      },
      customCell:(record)=>{
        if(record.isEnabled === 1){
            return {style:{color:"green"}}
        }else{
            return {style:{color:"red"}}
        }
      }
     },
    {
        title: '时间类型',
        align:"center",
        dataIndex: 'timeType',
        customRender:function ({ record }) {
            let timeTypeStr = "";
            if(record.timeType === 0){
                timeTypeStr = '整点小时'
            }else if(record.timeType === 1){
                timeTypeStr = '偶数小时'
            }else if(record.timeType === 2){
                timeTypeStr = '指定时间'
            }
            return timeTypeStr;
        }
    },
    {
        title: '指定时间',
        align:"center",
        dataIndex: 'timeComment'
    },
    {
        title: '取值类型',
        align:"center",
        dataIndex: 'stepType',
        customRender:function (t,r,index) {
            let str = "瞬时取值";
            if(t.value == '1'){
                str = "范围取值";
            }else if(t.value == '2'){
                str = "监控取值";
            }
            return str;
        }
    },
    {
    title: '取数信号',
    align:"center",
    dataIndex: 'retrievalExplanation'
    },
    {
    title: '计算模型',
    align:"center",
    dataIndex: 'formulaDescribe'
    },
  ];




