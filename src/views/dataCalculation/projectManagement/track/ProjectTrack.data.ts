import { BasicColumn } from '/@/components/Table';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '项目加分',
    align: "center",
    children: [
        {
          title: '目标类型',
          align: "center",
          dataIndex: 'targetType',
          width: 80,
        },
        {
          title: '计划加分',
          align: "center",
          dataIndex: 'planAddScore',
          width: 80,
          customRender: ({ text }) => {
            return text ? text.toFixed(2) : text;
          },
        },
        {
          title: '累计加分',
          align: "center",
          dataIndex: 'actualAddScore',
          width: 80,
          customRender: ({ text }) => {
            return text ? text.toFixed(2) : text;
          },
        },
        {
          title: '得分率',
          align: "center",
          dataIndex: 'scoreRate',
          width: 80,
          customRender: ({ record }) => {
            let rate = '0';
            if(record.planAddScore > 0 && record.actualAddScore > 0){
              rate = (record.actualAddScore / record.planAddScore * 100).toFixed(2) ;
            }
            return rate+ '%';
          },
        },
    ]
  },
];