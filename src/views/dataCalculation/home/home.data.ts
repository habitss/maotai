import { getColor} from '/@/utils/common/piColorUtils';

//查询数据
export const piColumns = [
  {
    title: '报表',
    dataIndex: 'tableName',
    align: 'left',
  },
  {
    title: '考核项',
    dataIndex: 'fieldName',
    align: 'left',
  },
  {
    title: '绿区',
    dataIndex: 'green',
    align: 'center',
    width: '60px',
  },
  {
    title: '黄区',
    dataIndex: 'yellow',
    width: '60px',
    align: 'center',
  },
  {
    title: '红区',
    dataIndex: 'red',
    align: 'center',
    width: '60px',
  },
  {
    title: '合计',
    dataIndex: 'all',
    align: 'center',
    width: '60px',
  },
  {
    title: '绿区率',
    dataIndex: 'greenProportion',
    align: 'center',
    width: '80px',
    customCell(record) {
      const greenProportion = Number(record.greenProportion);
      const greenRateTarget = Number(record.greenRateTarget);
      let colorObj = { bgColor: '' }
      if (record.all == 0) {
        colorObj.bgColor = getColor('grey');
      } else if (greenProportion >= greenRateTarget) {
        colorObj.bgColor = getColor('green');
      } else if (greenProportion < greenRateTarget) {
        colorObj.bgColor = getColor('red');
      }
      return {
        style: {
          'background-color': colorObj.bgColor
        }
      }
    },
    customRender({text}) {
      return text ? text+'%' : '';
    }
  }
];


function setLastTableCellColor(value, record, index) {
  return {
    children: value,
    style: {
      'background-color': value === '0%' ? 'white' : '#00ff00',
      'color': '#000000d9'
    }
  };
}


export const piData = [
  { 'hong': '1', 'huang': '1', 'lv': '11', 'lvDlv': '1', 'piName': '考核项一', 'id': '1', 'sum': 11 },
  { 'hong': '1', 'huang': '1', 'lv': '11', 'lvDlv': '1', 'piName': '考核项一', 'id': '1', 'sum': 11 },
  { 'hong': '1', 'huang': '1', 'lv': '11', 'lvDlv': '1', 'piName': '考核项一', 'id': '1', 'sum': 11 },
  { 'hong': '1', 'huang': '1', 'lv': '11', 'lvDlv': '1', 'piName': '考核项一', 'id': '1', 'sum': 11 },
  { 'hong': '1', 'huang': '1', 'lv': '11', 'lvDlv': '1', 'piName': '考核项一', 'id': '1', 'sum': 11 },
  { 'hong': '1', 'huang': '1', 'lv': '11', 'lvDlv': '1', 'piName': '考核项一', 'id': '1', 'sum': 11 },
  { 'hong': '1', 'huang': '1', 'lv': '11', 'lvDlv': '1', 'piName': '考核项一', 'id': '1', 'sum': 11 },
  { 'hong': '1', 'huang': '1', 'lv': '11', 'lvDlv': '1', 'piName': '考核项一', 'id': '1', 'sum': 11 },
  { 'hong': '1', 'huang': '1', 'lv': '11', 'lvDlv': '1', 'piName': '考核项一', 'id': '1', 'sum': 11 },
  { 'hong': '1', 'huang': '1', 'lv': '11', 'lvDlv': '1', 'piName': '考核项一', 'id': '1', 'sum': 11 },
  { 'hong': '1', 'huang': '1', 'lv': '11', 'lvDlv': '1', 'piName': '考核项一', 'id': '1', 'sum': 11 },
  { 'hong': '1', 'huang': '1', 'lv': '11', 'lvDlv': '1', 'piName': '考核项一', 'id': '1', 'sum': 11 },
];




export const optionTemp = {
  title: {
    text: '1', //主标题文本
    // subtext: '总数', //副标题文本
    x: 'center',
    y: '37%',
    // left: 35,
    // top: 105,
    textStyle: {
      fontSize: 20,
      color: '#333333',
      align: 'center'
    },
    subtextStyle: {
      fontFamily: '微软雅黑',
      fontSize: 18,
      color: '#333333'
    },
    itemGap: 2
  },
  grid: {
    left: '0',
    right: '0',
    top: '0',
    bottom: '0'
  },
  tooltip: {
    trigger: 'item',
    appendToBody: true
  },
  legend: {
    show: false,
    align: 'left',
    orient: 'vertical',
    left: 100,
    // left: 100,
    // top: 'center',
    // left: 'right',
    x: 'right',
    y: 'center',
    itemGap: 30,
    formatter: function (name) {
      return name + '   XXX' + '   XXX.XX%';
    }
  },
  series: [
    {
      name: '',
      type: 'pie',
      left: 'center',
      top: 0,
      radius: ['78%', '90%'],
      width: 130,
      avoidLabelOverlap: false,
      label: {
        show: false,
        position: 'inside'
      },
      emphasis: {
        label: {
          show: false,
          fontSize: 16,
          color: '#000000',
          fontWeight: 'bold'
        }
      },
      labelLine: {
        show: false
      },
      data: [
        {
          value: 500, name: '动力车间',
          itemStyle: {
            normal: { // 渐变色操作
              color: 'red'
            }
          }
        },
        {
          value: 250, name: '包装车间',
          itemStyle: {
            normal: { // 渐变色操作
              color: 'red'
            }
          }
        }
      ]
    }
  ]
};

export const optionTempRight = {
  legend: {
    show: false,
    align: 'left',
    orient: 'vertical',
    left: 100,
    x: 'right',
    y: 'center',
    itemGap: 30,
    formatter: function (name) {
      return name + '   XXX' + '   XXX.XX%';
    }
  },
  series: [
    {
      name: '',
      type: 'pie',
      left: 'center',
      top: 0,
      radius: ['78%', '90%'],
      width: 130,
      avoidLabelOverlap: false,
      label: {
        show: false,
        position: 'inside'
      },
      emphasis: {
        label: {
          show: false,
          fontSize: 16,
          color: '#000000',
          fontWeight: 'bold'
        }
      },
      labelLine: {
        show: false
      },
      data: [
        {
          value: 500, name: '动力车间',
          itemStyle: {
            normal: { // 渐变色操作
              color: 'red'
            }
          }
        },
        {
          value: 250, name: '包装车间',
          itemStyle: {
            normal: { // 渐变色操作
              color: 'red'
            }
          }
        }
      ]
    }
  ]
};

export const optionAction = {
  title: {
    text: '', //主标题文本
    x: 'center',
    y: '37%',

    textStyle: {
      fontSize: 20,
      color: '#333333',
      align: 'center'
    },
    subtextStyle: {
      fontFamily: '微软雅黑',
      fontSize: 18,
      color: '#333333'
    },
    itemGap: 2
  },
  tooltip: {
    trigger: 'item',
    appendToBody: true
  },
  grid: {
    left: '0',
    right: '0',
    top: '0',
    bottom: '0'
  },
  legend: {
    show: false,
  },
  series: [
    {
      name: '',
      type: 'pie',
      left: 'center',
      top: 0,
      radius: ['78%', '90%'],
      width: 130,
      avoidLabelOverlap: false,
      label: {
        show: false,
        position: 'inside'
      },
      emphasis: {
        label: {
          show: false,  //隐藏黑体的NAME的值
          fontSize: 16,
          color: '#000000',
          fontWeight: 'bold'
        }
      },
      labelLine: {
        show: false
      },
      data: [
        {
          value: 500, name: '动力车间',
          itemStyle: {
            normal: { // 渐变色操作
              color: 'red'
            }
          }
        },
        {
          value: 250, name: '包装车间',
          itemStyle: {
            normal: { // 渐变色操作
              color: 'red'
            }
          }
        }
      ]
    }
  ]
};

export const optionActionRight = {

  // tooltip: {
  //   trigger: 'item',
  //   appendToBody: true
  // },
  legend: {
    show: true,
    align: 'right',
    orient: 'vertical',
    left: -6,
    x: 'right',
    y: 'center',
    itemGap: 12,
    formatter: function (name) {
      return name + '   XXX' + '   XXX.XX%';
    }
  },
  series: [
    {
      name: '',
      type: 'pie',
      left: 0,
      top: 0,
      width: 0,
      avoidLabelOverlap: false,
      label: {
        show: false,
        position: 'inside'
      },
      emphasis: {
        label: {
          show: false,
          fontSize: 16,
          color: '#000000',
          fontWeight: 'bold'
        }
      },
      labelLine: {
        show: false
      },
      data: [
        {
          value: 500, name: '动力车间',
          itemStyle: {
            normal: { // 渐变色操作
              color: 'red'
            }
          }
        },
        {
          value: 250, name: '包装车间',
          itemStyle: {
            normal: { // 渐变色操作
              color: 'red'
            }
          }
        }
      ]
    }
  ]
};


export const KPiData = [{ 'name': 'KPI', 'biaoZhun': '标准', 'yue': '本月', 'zhou': '本周', 'id': '88' },
  { 'name': 'aa', 'biaozhun': '11', 'yue': 'dd', 'week': 'yy', 'id': '11' },
  { 'name': 'bb', 'biaozhun': '11', 'yue': 'dd', 'week': 'yy', 'id': '22' },
  { 'name': 'cc', 'biaozhun': '11', 'yue': 'dd', 'week': 'yy', 'id': '33' },
  { 'name': 'dd', 'biaozhun': '11', 'yue': 'dd', 'week': 'yy', 'id': '44' }];
  
  export const coloumnKpi = [
    {
      dataIndex: 'cell1',
      align: 'center',
      width: ('KPI' * 22) + 'px',
      titleText: 'KPI',
      slots: { title: 'cell1' }
    },
    {
      dataIndex: 'cell2',
      key: 'cell2',
      width: ('糖化周期偏高' * 22) + 'px',
      slots: { title: 'customTitle' },
      titleText: '糖化周期偏高'
    },
  
    {
      slots: { title: 'customTitle2' },
      dataIndex: 'cell3',
      align: 'center',
      width: ('粮耗' * 22) + 'px',
      titleText: '粮耗'
    },
  
    {
      slots: { title: 'customTitle3' },
      dataIndex: 'cell4',
      align: 'center',
      width: ('热效应指数' * 22) + 'px',
      titleText: '热效应指数'
    },
    // {
    //   slots: { title: 'customTitle9' },
    //   dataIndex: 'cell7',
    //   width: ('酿造理化指数' * 22) + 'px',
    //   align: 'center',
    //   titleText: '酿造理化指数'
    // },
    // {
    //   slots: { title: 'customTitle4' },
    //   dataIndex: 'cell6',
    //   align: 'center',
    //   width: ('电耗' * 22) + 'px',
    //   titleText: '电耗'
    // },
    // {
    //   slots: { title: 'customTitle5' },
    //   dataIndex: 'cell16',
    //   align: 'center',
    //   width: ('酒耗' * 22) + 'px',
    //   titleText: '酒耗'
    // },
    // {
    //   slots: { title: 'customTitle6' },
    //   dataIndex: 'cell7',
    //   align: 'center',
    //   width: ('水耗' * 22) + 'px',
    //   titleText: '水耗'
    // },
    // {
    //   slots: { title: 'customTitle7' },
    //   dataIndex: 'cell8',
    //   align: 'center',
    //   width: ('CO2消耗' * 22) + 'px',
    //   titleText: 'CO2消耗'
    // },
    // {
    //   slots: { title: 'customTitle8' },
    //   dataIndex: 'cell9',
    //   align: 'center',
    //   width: ('蒸汽耗' * 22) + 'px',
    //   titleText: '蒸汽耗'
    // }
  ];
  
  export const dataKpi = [
    { 'cell1': '标准', 'cell2': '3', 'cell3': '151.8', 'cell4': '99.99', 'id': '1' },
    // { 'cell1': '本月', 'cell2': '', 'cell4': '224.8', 'cell10': '110', 'id': '2' },
    // { 'cell1': '本周', 'cell2': '', 'cell4': '224.8', 'cell10': '110', 'id': '3' }
  ];
  
  
  export const coloumnPi = [
    {
      title: '考核项',
      dataIndex: 'piName',
      align: 'piName'
      // width: '180px',
    },
    {
      title: '产线',
      dataIndex: 'lineName',
      align: 'left',
      width: '90px'
    },
    {
      title: '绿区',
      dataIndex: 'lv',
      width: '60px',
      scopedSlots: { customRender: 'lv' }
    },
    {
      title: '黄区',
      dataIndex: 'huang',
      width: '60px',
      align: 'center',
      scopedSlots: { customRender: 'huang' }
    },
    {
      title: '红区',
      dataIndex: 'hong',
      align: 'center',
      width: '60px',
      scopedSlots: { customRender: 'hong' }
    },
    {
      title: '合计',
      dataIndex: 'sum',
      align: 'center',
      width: '60px',
      scopedSlots: { customRender: 'sum' }
    },
    {
      title: '绿区率',
      dataIndex: 'lvDlv',
      align: 'center',
      width: '60px',
      scopedSlots: { customRender: 'lvDlv' },
      customRender: function (value, record, index) {
        return setLastTableCellColor(value, record, index);
      }
    }
  ];
  
  function setTableCellColor(record, rowInd, key) {
    let color = 'white';
    let colorObj = { 'red': 'red', 'green': '#00ff00', 'yellow': 'yellow' };
    if (rowInd < 0)
      color = '#EBF1FB';
    if (record) {
      color = colorObj[record];
    }
    // color = 'red'
    return {
      children: record == 'red' ? '红区' : record == 'green' ? '绿区' : record == 'yellow' ? '黄区' : '',
      style: {
        'background-color': color,
        'color': record == 'yellow' ? '#333' : '#fff'
      }
    };
  }
  
  
  export const columnsAbnor = [
    {
      title: '产线',
      dataIndex: 'lineName',
      align: 'left',
      ellipsis: true
      // width: '76px',
    },
    {
      title: '设备',
      dataIndex: 'deviceName',
      align: 'left',
      ellipsis: true
      // width: '60px',
    },
    {
      title: '异常项',
      dataIndex: 'piName',
      ellipsis: true,
      align: 'center'
    },
    {
      title: '异常编码',
      dataIndex: 'piCode',
      ellipsis: true,
      align: 'center'
    },
  
    {
      title: '区域',
      dataIndex: 'color',
      // ellipsis: true,
      width: '50px',
      align: 'center',
      customRender: function (record, index) {
        return setTableCellColor(record, index, 'colour');
      }
      // scopedSlots: { customRender: 'color' },
    },
    {
      title: '异常时间',
      dataIndex: 'time',
      align: 'center',
      ellipsis: true
      // width: '70px',
    }
  
  
  ];
  
  // export const piData = [
  //   { 'hong': '1', 'huang': '1', 'lv': '11', 'lvDlv': '1', 'piName': '考核项一', 'id': '1', 'sum': 11 },
  //   { 'hong': '1', 'huang': '1', 'lv': '11', 'lvDlv': '1', 'piName': '考核项一', 'id': '1', 'sum': 11 },
  //   { 'hong': '1', 'huang': '1', 'lv': '11', 'lvDlv': '1', 'piName': '考核项一', 'id': '1', 'sum': 11 },
  //   { 'hong': '1', 'huang': '1', 'lv': '11', 'lvDlv': '1', 'piName': '考核项一', 'id': '1', 'sum': 11 },
  //   { 'hong': '1', 'huang': '1', 'lv': '11', 'lvDlv': '1', 'piName': '考核项一', 'id': '1', 'sum': 11 },
  //   { 'hong': '1', 'huang': '1', 'lv': '11', 'lvDlv': '1', 'piName': '考核项一', 'id': '1', 'sum': 11 },
  //   { 'hong': '1', 'huang': '1', 'lv': '11', 'lvDlv': '1', 'piName': '考核项一', 'id': '1', 'sum': 11 },
  //   { 'hong': '1', 'huang': '1', 'lv': '11', 'lvDlv': '1', 'piName': '考核项一', 'id': '1', 'sum': 11 },
  //   { 'hong': '1', 'huang': '1', 'lv': '11', 'lvDlv': '1', 'piName': '考核项一', 'id': '1', 'sum': 11 },
  //   { 'hong': '1', 'huang': '1', 'lv': '11', 'lvDlv': '1', 'piName': '考核项一', 'id': '1', 'sum': 11 },
  //   { 'hong': '1', 'huang': '1', 'lv': '11', 'lvDlv': '1', 'piName': '考核项一', 'id': '1', 'sum': 11 },
  //   { 'hong': '1', 'huang': '1', 'lv': '11', 'lvDlv': '1', 'piName': '考核项一', 'id': '1', 'sum': 11 },
  // ];
  
  export const errorAnalysisDataOpt = {
    title: {
      text: '异常分析', //主标题文本
      // subtext: ' 总数', //副标题文本
      left: 63,
      top: '45%',
      textStyle: {
        fontSize: 14,
        color: '#333333',
        align: 'center'
      },
      subtextStyle: {
        fontFamily: '微软雅黑',
        fontSize: 25,
        color: '#333333'
      },
      itemGap: 2
    },
    tooltip: {
      trigger: 'item',
      appendToBody: true
    },
    legend: {
      align: 'left',
      // type: 'scroll',
      // orient: 'horizontal',
      orient: 'vertical',
      icon: 'circle',
      left: 165,
      // top: 30,
      // x: 'center',
      // y: 'top',
      x: 'left',
      y: 'center',
      textStyle: {
        // lineHeight: 55,
      },
      itemGap: 20,
      formatter: function (name) {
        return name + '\nXXX' + 'XXX.XX%';
      }
    },
    series: [
      {
        name: 'Access From',
        type: 'pie',
        left: 40,
        top: 0,
        radius: ['60%', '99%'],
        width: 120,
        avoidLabelOverlap: false,
        label: {
          show: false,
          position: 'inside'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            color: '#000000',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          {
            value: 500, name: '无数据', itemStyle: {
              normal: { color: 'red' }
            }
          }
        ]
      }
    ]
  };
  
  //圆饼的渐变颜色
  export const pieGradualChangeColor = [
    [{ offset: 0, color: '#00ff00' }, { offset: 1, color: '#00ff00' }], //绿
    [{ offset: 0, color: '#FFC400' }, { offset: 1, color: '#FFC400' }], //黄
    [{ offset: 0, color: 'red' }, { offset: 1, color: 'red' }], //红
    [{ offset: 0, color: '#00ff00' }, { offset: 1, color: '##00ff00' }],
    [{ offset: 0, color: '#00ff00' }, { offset: 1, color: '#00ff00' }],
    [{ offset: 0, color: '#00ff00' }, { offset: 1, color: '#00ff00' }]
  
  ];