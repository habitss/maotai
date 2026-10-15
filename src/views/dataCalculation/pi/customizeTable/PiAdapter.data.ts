import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
//列表数据
export const columns: BasicColumn[] = [
     {
      title: '区域',
      align:"center",
      dataIndex: 'areaNoVal'
     },
     {
        title: '设备',
        align:"center",
        dataIndex: 'unitName'
    },
    {
        title: '步骤',
        align:"center",
        dataIndex: 'eopName',
        customRender:function (t) {
            let str = "";
            if(t.value){
                str = t.value + "("+t.record.eopNo+")"
            }
            return str;
        }
    },
    {
        title: '取值',
        align:"center",
        dataIndex: 'val',
        customRender:function (t) {
            let str = "";
            if(t.value == 'startTime'){
                str = "开始时间";
            }else if(t.value == 'startDate'){
                str = "开始时间(日期)";
            }else if(t.value == 'endTime'){
                str = "结束时间";
            }else if(t.value == 'endDate'){
                str = "结束时间(日期)";
            }else if(t.value == 'runtime'){
                str = "运行时长";
            }else if(t.value == 'formula'){
                str = "配方";
            }else if(t.value == 'batchStartTime'){
                str = "批次开始时间";
            }else if(t.value == 'deviceName'){
                str = "设备";
            }else{
                str = t.value;
            }
            return str;
        }
    },
    {
        title: '取值类型',
        align:"center",
        dataIndex: 'valType',
        customRender:function (t) {
            let str = "";
            if(t.value == '0'){
                str = "设定值";
            }else if(t.value == '1'){
                str = "实际值";
            }
            return str;
        }
    },
    {
        title: '取第几次',
        align:"center",
        dataIndex: 'several',
        customRender:function (t) {
            let str = "";
            if(t.value == '-1'){
              str = '最后一条'
            }else{
              str = t.value
            }
            return str;
        }
    },
    {
        title: '系统步骤',
        align:"center",
        dataIndex: 'ropId'
    },
    {
        title: '限制条件',
        align:"center",
        dataIndex: 'conditionExplanation'
    }
  ];




