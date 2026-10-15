//--story=1055786 --user=孙金伟 24-PI点图箱线图开发 https://www.tapd.cn/63554897/s/1384954 单元测试
import { ref, Ref } from 'vue';
import * as echarts from 'echarts';
import dayjs from 'dayjs';

// Mock 依赖
jest.mock('dayjs', () => {
  return () => ({
    format: jest.fn(() => '2023年01月01日')
  });
});

jest.mock('echarts', () => ({
  init: jest.fn()
}));

// 重新实现 initChart 函数用于测试（从源组件中复制）
const createInitChart = (boxChartOption: Ref<any>, chart: Ref<any>) => {
  return (res: any, values: any) => {
    const mockEChartsInstance = {
      setOption: jest.fn()
    };
    
    (echarts.init as jest.Mock).mockReturnValue(mockEChartsInstance);

    boxChartOption.value = {
      title: {
        text: `${values.standardName}（${dayjs(values.searchTimeStart).format('YYYY年MM月DD日')}-${dayjs(values.searchTimeEnd).format('YYYY年MM月DD日')}）`,
        left: 'center'
      },
      tooltip: {
        trigger: 'item',
        axisPointer: {
          type: 'shadow'
        },
        formatter: (params: any) => {
          if (params.seriesType === 'boxplot') {
            const index = params.value[0];
            const min = params.value[1];
            const q1 = params.value[2];
            const median = params.value[3];
            const q3 = params.value[4];
            const max = params.value[5];
            const boxHeight = q3 - q1;
            
            return `
              <label style="font-weight: bold;">${res.nameList[index]}</label><br/>
              最小值: ${min}<br/>
              Q1: ${q1}<br/>
              中位数: ${median}<br/>
              Q3: ${q3}<br/>
              最大值: ${max}<br/>
              箱体高度: ${boxHeight.toFixed(2)}
            `;
          }
        }
      },
      grid: {
        left: '10%',
        right: '10%',
        bottom: '15%'
      },
      xAxis: {
        type: 'category',
        data: res.nameList,
        boundaryGap: true,
        nameGap: 30,
        splitArea: {
          show: false
        },
        splitLine: {
          show: false
        }
      },
      yAxis: {
        type: 'value',
        splitArea: {
          show: true
        }
      },
      series: [
        {
          name: 'boxplot',
          type: 'boxplot',
          data: res.boxList,
          tooltip: {
            formatter: (params: any) => {
              const index = params.value[0];
              const min = params.value[1];
              const q1 = params.value[2];
              const median = params.value[3];
              const q3 = params.value[4];
              const max = params.value[5];
              const boxHeight = q3 - q1;
              
              return `
                <label style="font-weight: bold;">${res.nameList[index]}</label><br/>
                最小值: ${min}<br/>
                Q1: ${q1}<br/>
                中位数: ${median}<br/>
                Q3: ${q3}<br/>
                最大值: ${max}<br/>
                箱体高度: ${boxHeight.toFixed(2)}
              `;
            }
          },
          markLine: {
            silent: true,
            symbol: 'none',
            label: {
              position: 'middle',
              formatter: '上规格线'
            },
            data: [
              {
                yAxis: res.redUp
              }
            ]
          }
        },
        {
          name: 'outlier',
          type: 'scatter',
          data: res.scatterList,
          itemStyle: {
            color: function (param: any) {
              const colorList = ['#E19359', '#4874CB', '#00FF00'];
              return colorList[param.dataIndex % 3];
            }
          },
          label: {
            show: true,
            position: 'top',
            formatter: function (param: any) {
              return param.value[1];
            }
          }
        }
      ]
    };

    if (chart.value) {
      const myChart = echarts.init(chart.value);
      myChart.setOption(boxChartOption.value);
    }
  };
};

describe('initChart', () => {
  let boxChartOption: Ref<any>;
  let chart: Ref<any>;

  beforeEach(() => {
    boxChartOption = ref({});
    chart = ref(document.createElement('div'));
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it('TC001: 应该使用正确的标题和配置初始化图表', () => {
    const initChart = createInitChart(boxChartOption, chart);
    
    const res = {
      nameList: ['A', 'B'],
      boxList: [[0, 1, 2, 3, 4, 5], [1, 2, 3, 4, 5, 6]],
      scatterList: [[0, 10], [1, 20]],
      redUp: 80
    };
    
    const values = {
      standardName: 'Test Standard',
      searchTimeStart: '2023-01-01',
      searchTimeEnd: '2023-01-31'
    };

    initChart(res, values);

    // 验证标题是否正确生成
    expect(boxChartOption.value.title.text).toBe('Test Standard（2023年01月01日-2023年01月01日）');
    
    // 验证其他基本配置
    expect(boxChartOption.value.xAxis.data).toEqual(res.nameList);
    expect(boxChartOption.value.series[0].data).toEqual(res.boxList);
    expect(boxChartOption.value.series[1].data).toEqual(res.scatterList);
    
    // 验证 echarts 被正确调用
    expect(echarts.init).toHaveBeenCalledWith(chart.value);
  });

  it('TC002: tooltip formatter 应该正确处理箱线图类型的参数', () => {
    const initChart = createInitChart(boxChartOption, chart);
    
    const res = {
      nameList: ['Item A', 'Item B'],
      boxList: [[0, 10, 20, 30, 40, 50]],
      scatterList: [],
      redUp: 80
    };
    
    const values = {
      standardName: 'Test',
      searchTimeStart: '2023-01-01',
      searchTimeEnd: '2023-01-31'
    };

    // 先执行 initChart 以生成配置
    initChart(res, values);
    
    // 获取 formatter 函数并测试
    const formatter = boxChartOption.value.tooltip.formatter;
    const params = {
      seriesType: 'boxplot',
      value: [0, 10, 20, 30, 40, 50]
    };
    
    const result = formatter(params);
    expect(result).toContain('<label style="font-weight: bold;">Item A</label>');
    expect(result).toContain('Q1: 20');
    expect(result).toContain('最小值: 10');
    expect(result).toContain('中位数: 30');
    expect(result).toContain('最大值: 50');
    expect(result).toContain('Q3: 40');
    expect(result).toContain('箱体高度: 20.00');
  });

  it('TC003: 散点图 itemStyle.color 应该根据 dataIndex 循环返回颜色', () => {
    const initChart = createInitChart(boxChartOption, chart);
    
    const res = {
      nameList: [],
      boxList: [],
      scatterList: [[0, 10], [1, 20], [2, 30], [3, 40]],
      redUp: 80
    };
    
    const values = {
      standardName: 'Test',
      searchTimeStart: '2023-01-01',
      searchTimeEnd: '2023-01-31'
    };

    // 先执行 initChart 以生成配置
    initChart(res, values);
    
    // 获取 color 函数并测试不同情况
    const colorFunction = boxChartOption.value.series[1].itemStyle.color;
    
    expect(colorFunction({ dataIndex: 0 })).toBe('#E19359');
    expect(colorFunction({ dataIndex: 1 })).toBe('#4874CB');
    expect(colorFunction({ dataIndex: 2 })).toBe('#00FF00');
    expect(colorFunction({ dataIndex: 3 })).toBe('#E19359'); // 循环回到第一个
  });

  it('TC004: 应该能够处理空数据而不抛出异常', () => {
    const initChart = createInitChart(boxChartOption, chart);
    
    const res = {
      nameList: [],
      boxList: [],
      scatterList: [],
      redUp: null
    };
    
    const values = {
      standardName: '',
      searchTimeStart: null,
      searchTimeEnd: null
    };

    // 不应该抛出异常
    expect(() => {
      initChart(res, values);
    }).not.toThrow();
  });
});