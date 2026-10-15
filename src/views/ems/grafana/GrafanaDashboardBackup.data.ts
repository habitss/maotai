import { BasicColumn } from '/@/components/Table';
import { render } from '/@/utils/common/renderUtils';
import { h } from 'vue';

//列表数据
export const columns: BasicColumn[] = [
  // {
  //   title: 'GF实例名称',
  //   align: 'center',
  //   dataIndex: 'instanceIdVal',
  //   width: 180,
  // },
  // {
  //   title: 'GF实例URL',
  //   align: 'center',
  //   dataIndex: 'instanceIdUrlVal',
  //   width: 350,
  // },
  {
    title: '报表ID',
    align: 'center',
    dataIndex: 'uid',
    width: 160,
  },
  {
    title: '报表标题',
    align: 'center',
    dataIndex: 'title',
    customRender: ({ text, record }) => {
      return h(
        'a',
        {
          href: `${record.instanceIdUrlVal}/d/${record.uid}`,
          target: '_blank',
          style: { color: '#d92622', textDecoration: 'none' },
          onClick: (e) => e.stopPropagation(), // 防止触发表格的行点击事件
        },
        render.renderTip(text)
      );
    },
  },
  {
    title: '报表标识',
    align: 'center',
    dataIndex: 'slug',
  },
  {
    title: '状态',
    align: 'center',
    dataIndex: 'state_dictText',
    width: 100,
  },
  {
    title: '备份时间',
    align: 'center',
    dataIndex: 'createTime',
    width: 180,
  },
];
