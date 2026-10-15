import * as MySpreadUtils from '@/utils/spreadUtils';

//获取 X 轴数据
export function getXDatas(sheet: any): string[] {
  const start = MySpreadUtils.position2Rowcol(MySpreadUtils.getSheetVal(sheet, 'B-2'));
  const result: string[] = [];
  for (let j = 0; j < 31; j++) {
    result.push(sheet.getValue(start[0], j + start[1]));
  }
  return result;
}

//获取 Y 轴数据与系列配置
export function getSeriesData(sheet: any, startRow: number, endRow: number, xDatas: string[]) {
  const yAxisArr: any[] = [];
  const series: any[] = [];
  const legends: string[] = [];
  const yAxisSet: Set<string> = new Set();

  const stackedBars: any[] = [];

  for (let i = startRow; i <= endRow; i++) {
    const meta = extractChartMeta(sheet, i);
    if (!yAxisSet.has(meta.yAxisKey)) {
      yAxisSet.add(meta.yAxisKey);
      yAxisArr.push(createYAxis(meta));
    }

    const serie = createSerie(sheet, meta, xDatas);
    series.push(serie);
    legends.push(meta.name);
    if (meta.stack) stackedBars.push(serie);
  }

  if (stackedBars.length) {
    const totals = getStackTotals(stackedBars);
    stackedBars.forEach((serie) => {
      serie.data = serie.data.map((val: any, idx: number) => [xDatas[idx], val, totals[idx]]);
    });
  }

  return { series, legend: legends, yAxis: yAxisArr };
}

//提取每一行的图表元数据
export function extractChartMeta(sheet: any, row: number) {
  return {
    name: sheet.getValue(row, 0),
    dataIndex: MySpreadUtils.position2Rowcol(sheet.getValue(row, 1)),
    type: sheet.getValue(row, 2),
    stack: sheet.getValue(row, 3),
    color: sheet.getCell(row, 4).backColor(),
    yAxisPos: sheet.getValue(row, 5),
    yAxisIndex: sheet.getValue(row, 6),
    yMin: sheet.getValue(row, 7),
    yMax: sheet.getValue(row, 8),
    symbol: sheet.getValue(row, 9),
    labelType: sheet.getValue(row, 10),
    showLabel: sheet.getValue(row, 11) === '是',
    yAxisKey: sheet.getValue(row, 5) + '-' + sheet.getValue(row, 6),
  };
}

//创建 Y 轴对象
export function createYAxis(meta: any) {
  const y: any = { type: 'value', position: meta.yAxisPos };
  if (meta.yMin != null) y.min = meta.yMin;
  if (meta.yMax != null) y.max = meta.yMax;
  if (meta.labelType === '百分数') {
    y.axisLabel = {
      formatter: (val: number) => Math.round(val * 1000.0) / 10.0 + '%',
    };
  }
  return y;
}

//创建每一个图表序列（series）
export function createSerie(sheet: any, meta: any, xDatas: string[]) {
  const xLen = xDatas.length;
  console.log(xLen);
  const yDatas: any[] = [];
  for (let j = 0; j < 31; j++) {
    const val = sheet.getValue(meta.dataIndex[0], j + meta.dataIndex[1]);
    if (meta.labelType === '百分数') {
      yDatas.push(Math.round(val * 1000.0) / 1000.0);
    } else {
      yDatas.push(Math.round(val * 100.0) / 100.0);
    }
  }

  const serie: any = {
    name: meta.name,
    type: meta.type,
    yAxisIndex: meta.yAxisIndex,
    itemStyle: { color: meta.color },
    data: yDatas,
  };

  if (meta.stack) serie.stack = meta.stack;

  if (meta.type === 'bar') {
    serie.label = getBarLabel(meta.labelType, meta.showLabel);
  } else if (meta.type === 'line') {
    serie.symbol = meta.symbol;
    serie.symbolSize = 6;
    serie.label = getLineLabel(meta.labelType, meta.showLabel);
  }

  return serie;
}

//标签格式处理函数
export function getBarLabel(type: string, show: boolean) {
  return {
    show,
    position: 'inside',
    color: '#000000',
    formatter: (params: any) => {
      const [_, val, total] = params.value;
      if (type === '占比') {
        return total ? Math.round((val * 1000.0) / total) / 10.0 + '%' : '0.0%';
      } else if (type === '值') {
        return val;
      } else if (type === '百分数') {
        return Math.round(val * 1000.0) / 10.0 + '%';
      }
    },
  };
}
//标签格式处理函数
function getLineLabel(type: string, show: boolean) {
  return {
    show,
    position: 'top',
    color: 'rgba(255, 0, 0, 0.9)',
    textShadowColor: 'rgba(0, 0, 0, 1)',
    textShadowBlur: 0.5,
    textShadowOffsetX: 0.8,
    textShadowOffsetY: 0.8,
    formatter: (params: any) => {
      const val = params.value;
      if (type === '值') return val;
      if (type === '百分数') return Math.round(val * 1000.0) / 10.0 + '%';
    },
  };
}

//获取柱状图堆叠总和
export function getStackTotals(stackedBars: any[]) {
  const totals = [...stackedBars[0].data];
  for (let i = 1; i < stackedBars.length; i++) {
    for (let j = 0; j < stackedBars[i].data.length; j++) {
      totals[j] += stackedBars[i].data[j];
    }
  }
  return totals;
}
