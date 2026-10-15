import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { useUserStore } from '/@/store/modules/user';
import dayjs from 'dayjs';
const userStore = useUserStore();

//列表数据
export const columns: BasicColumn[] = [
  {
    title: '部门',
    align: "center",
    dataIndex: 'deptName',
	width:100,
    customCell: () => {
      return {
        style: { textAlign: 'left' } // 内容居左
      }
    }
  },
  {
    title: '岗位',
    align: "center",
    dataIndex: 'postName',
	width:100,
  },
  {
    title: '检查人姓名',
    align: "center",
    dataIndex: 'realName',
    width:80,
  },
  // {
    // title: '时间范围',
    // align: "center",
    // children:[
      {
        title: "应执行次数",
        align: "center",
        dataIndex: 'plannedCount',
        width:80,
      },
      {
        title: "实际执行数",
        align: "center",
        dataIndex: 'actualCount',
        width:80,
      },
      {
        title: "执行率",
        align: "center",
        dataIndex: 'executionRate',
        width:80,
      }
  //   ]
  // }
];

// 搜索表单配置
export const searchFormSchema: FormSchema[] = [
  {
    label: "部门",
    field: 'deptCode',
    component: 'KtDepartTreeSelect',
    componentProps: ({formModel}) => {
      return {
        allowClear: false,
        fieldNames: { label:'title', value: 'orgCode' },
        defaultExpandLevel: 1,
        componentProps: ({formModel}) => {
          return {
            allowClear: false,
            fieldNames: { label:'title', value: 'orgCode' },
            defaultExpandLevel: 1,
            onChange: (e) => {
              formModel.postId = '';
            }
		  }
		},
      }
    },
    defaultValue: userStore?.userInfo?.orgCode,
  },
  {
    label: '岗位名称',
    field: 'postId',
    component: 'JDictSelectTag',
    // componentProps: {
    //   dictCode: "mes3_mdm_post where del_flag = 0 and post_type = 1, post_name, id",
    //   showChooseOption: false,
    //   showSearch: true
    // },
	componentProps: ({ formModel }) => {
	  return {
		dictCode: "mes3_mdm_post where del_flag = 0 and dept_code = '"+formModel.deptCode+"' and post_type = 1, post_name, id",
		showChooseOption: false,
		mode: 'multiple',
		showSearch: true
	  }
	},
  },
  {
    field: 'timeRange',
    label: '日期',
    component: 'RangePicker',
    componentProps: {
      format: 'YYYY-MM-DD',
      valueFormat: 'YYYY-MM-DD',
    },
    defaultValue: [dayjs().startOf('month').format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')],
  }
];
