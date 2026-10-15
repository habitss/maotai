import * as XLSX from 'xlsx';

export function exportExcelWithHeader(fileName, exportData, exportHeader) {
  // 将表头和数据合并为一个完整的工作表数据
  const worksheetData = [exportHeader, ...exportData];

  // 将数据转换为工作表
  const worksheet = XLSX.utils.aoa_to_sheet(worksheetData);

  // 创建一个新的工作簿并将工作表添加到其中
  const workbook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(workbook, worksheet, 'Sheet1');

  // 生成Excel文件并触发下载
  XLSX.writeFile(workbook, fileName + '.xlsx');
}

export function exportExcel(fileName, exportData, columnWidths) {
  // 构建列定义，包括标题和宽度
  const columns = exportData[0] && Object.keys(exportData[0]).map((key, index) => ({
      header: key,
      key: key,
      width: columnWidths ? columnWidths[index] : 20 // 设置默认宽度为20
    }));
    // 将数据转换为工作表
    const ws = XLSX.utils.json_to_sheet(exportData);

    // 创建工作簿并添加工作表
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, "Sheet1");
    // 设置列宽
    if (columns) {
      ws['!cols'] = columns.map(col => ({ wch: col.width }));
    }

    // 生成Excel文件
    const wbout = XLSX.write(wb, { bookType: 'xlsx', type: 'binary' });

    // 创建二进制对象并创建URL
    const blob = new Blob([s2ab(wbout)], { type: 'application/octet-stream' });
    const url = URL.createObjectURL(blob);

    // 创建下载链接
    const a = document.createElement('a');
    a.href = url;
    a.download = fileName + '.xlsx';
    document.body.appendChild(a);
    a.click();

    // 清理并移除下载链接
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
}

// 将字符串转换为ArrayBuffer
function s2ab(s) {
    const buf = new ArrayBuffer(s.length);
    const view = new Uint8Array(buf);
    for (let i = 0; i !== s.length; ++i) view[i] = s.charCodeAt(i) & 0xFF;
    return buf;
}
