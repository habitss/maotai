import {BasicColumn} from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '部门',
    dataIndex: 'title',
    width: 150,
    align: 'center',
    key: 'title',
  },
  {
    title: '诊断计划完成进度',
    align: 'center',
    children:[
      {
        title: 'SOP数',
        align: 'center',
        dataIndex: 'sopNumber',
        key: 'sopNumber',
        width: 90
      },
      {
        title: '诊断数',
        align: 'center',
        dataIndex: 'planNumber',
        key: 'planNumber',
        width: 90
      },
      {
        title: '已完成',
        align: 'center',
        dataIndex: 'planSuccess',
        key: 'planSuccess',
        width: 90
      },
      {
        title: '计划',
        align: 'center',
        dataIndex: 'plan',
        key: 'plan',
        width: 90
      },
      {
        title: '延迟',
        align: 'center',
        dataIndex: 'planDelay',
        key: 'planDelay',
        width: 90
      },
      {
        title: '完成率',
        align: 'center',
        dataIndex: 'planRate',
        key: 'planRate',
        width: 90,
        customRender: ({ record }) => {
          let total = record.plan+ record.planDelay + record.planSuccess;
          if(total == 0 || record.planSuccess == 0){
            return '0.00%';
          }
          return (record.planSuccess / (record.plan+ record.planDelay + record.planSuccess)*100).toFixed(2) + '%';
        }
      },
    ]
  }
];