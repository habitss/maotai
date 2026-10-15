import * as GC from '@grapecity/spread-sheets';

/**
 * 将 Excel 格式的位置（如 "A-1", "AB-3"）转换为行列数组 [rowIndex, colIndex]
 * @param position 位置字符串，格式如 "A-1" 或 "AB-5"
 * @returns [行号, 列号]，如 [4, 0]
 */
export function position2Rowcol(position: string): [number, number] {
  const posArr = position.split('-');
  const row = Number(posArr[1]) - 1;
  let col = 0;
  const colStr = posArr[0].toUpperCase();

  if (colStr.length === 1) {
    col = colStr.charCodeAt(0) - 'A'.charCodeAt(0);
  } else if (colStr.length === 2) {
    col = (colStr.charCodeAt(0) - 'A'.charCodeAt(0) + 1) * 26 + (colStr.charCodeAt(1) - 'A'.charCodeAt(0));
  }
  return [row, col];
}

/**
 * 从 SpreadJS 的 Sheet 中读取指定单元格的值
 * @param sheet SpreadJS 的 Worksheet 实例
 * @param position 单元格位置字符串（例如 "AB-5"）
 * @returns 对应单元格的值
 */
export function getSheetVal(sheet: GC.Spread.Sheets.Worksheet, position: string): any {
  const [row, col] = position2Rowcol(position);
  return sheet.getValue(row, col);
}

/**
 * 清空当前Spread并新建一个空 Sheet1
 * @param spread
 */
export function cleanSpread(spread: GC.Spread.Sheets.Workbook | null) {
  spread!.clearSheets();
  spread!.addSheet(0, new GC.Spread.Sheets.Worksheet('Sheet1'));
}

/**
 * 直接用 Sheet1 替换掉当前 WorkBook
 * @param spread
 */
export function resetSpread(spread: GC.Spread.Sheets.Workbook | null) {
  const emptyTemplate = {
    sheets: [{ name: 'Sheet1', rowCount: 100, columnCount: 20 }],
  };
  spread!.suspendPaint();
  spread!.fromJSON(emptyTemplate); // 覆盖所有数据
  spread!.resumePaint();
}

/**
 * 格式化数字，默认两位小数
 */
export function formatNumber(rawValue: unknown, decimalPlaces: number = 2): string {
  if (typeof rawValue === 'number' || (!isNaN(Number(rawValue)) && typeof rawValue !== 'boolean')) {
    const num = Number(rawValue);
    return num.toFixed(decimalPlaces);
  }
  return '0'.padEnd(decimalPlaces + 2, '0');
}
