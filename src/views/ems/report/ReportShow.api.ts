import dayjs from 'dayjs';
import JSZip from 'jszip';
import * as RepFunApi from './ReportFunction.api';
import { useMessage } from '@/hooks/web/useMessage';
const { createMessage } = useMessage();
const errorArr: string[] = ['ERROR', 'ERROR', 'ERROR', 'ERROR', 'ERROR', 'ERROR', 'ERROR', 'ERROR', 'ERROR', 'ERROR', 'ERROR', 'ERROR'];

// 加载采集表班数据
export const loadCollectClassUsageData = async (month) => {
  console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Start Download CollectClassUsage`);
  const response = await RepFunApi.getCollectClassUsageList(month);
  if (response.data.type === 'application/zip') {
    const text = await JSZip.loadAsync(response.data).then((zipdata) => {
      return zipdata.file('apidata')!.async('text');
    });
    const jsonData = JSON.parse(text);
    console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]End Download CollectClassUsage`);
    return { name: '采集表班数据', value: jsonData, row: 0 };
  } else {
    const text = (await response.data.text) ? await response.data.text() : JSON.stringify(response.data);
    const jsonData = JSON.parse(text);
    createMessage.error(jsonData.message, 5);
    return { name: '采集表班数据', value: [[jsonData.message, ...errorArr]], row: 0 };
  }
};

// 加载采集表日数据
export const loadCollectDayUsageData = async (month) => {
  console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Start Download CollectDayUsage`);
  const response = await RepFunApi.getCollectDayUsageList(month);
  if (response.data.type === 'application/zip') {
    const text = await JSZip.loadAsync(response.data).then((zipdata) => {
      return zipdata.file('apidata')!.async('text');
    });
    const jsonData = JSON.parse(text);
    console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]End Download CollectDayUsage`);
    return { name: '采集表日数据', value: jsonData, row: 0 };
  } else {
    const text = (await response.data.text) ? await response.data.text() : JSON.stringify(response.data);
    const jsonData = JSON.parse(text);
    createMessage.error(jsonData.message, 5);
    return { name: '采集表日数据', value: [[jsonData.message, ...errorArr]], row: 0 };
  }
};

// 加载采集表半小时数据
export const loadCollectHalfHourUsageData = async (
  endDate: string | undefined = undefined,
  tagids: string[] | undefined = undefined,
  headFlag: string | undefined = undefined,
  headType: string | undefined = undefined
) => {
  console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Start Download CollectHalfHourUsage`);
  const response = await RepFunApi.getCollectHalfHourUsageList(endDate, tagids, headFlag, headType);
  if (response.data.type === 'application/zip') {
    const text = await JSZip.loadAsync(response.data).then((zipdata) => {
      return zipdata.file('apidata')!.async('text');
    });
    const jsonData = JSON.parse(text);
    console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]End Download CollectHalfHourUsage`);
    return { name: '采集表半小时数据', value: jsonData, row: 0 };
  } else {
    const text = (await response.data.text) ? await response.data.text() : JSON.stringify(response.data);
    const jsonData = JSON.parse(text);
    createMessage.error(jsonData.message, 5);
    return { name: '采集表半小时数据', value: [[jsonData.message, ...errorArr]], row: 0 };
  }
};

// 加载采集表小时数据
export const loadCollectHourUsageData = async (
  endDate: string | undefined = undefined,
  tagids: string[] | undefined = undefined,
  headFlag: string | undefined = undefined,
  headType: string | undefined = undefined
) => {
  console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Start Download CollectHourUsage`);
  const response = await RepFunApi.getCollectHourUsageList(endDate, tagids, headFlag, headType);
  if (response.data.type === 'application/zip') {
    const text = await JSZip.loadAsync(response.data).then((zipdata) => {
      return zipdata.file('apidata')!.async('text');
    });
    const jsonData = JSON.parse(text);
    console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]End Download CollectHourUsage`);
    return { name: '采集表小时数据', value: jsonData, row: 0 };
  } else {
    const text = (await response.data.text) ? await response.data.text() : JSON.stringify(response.data);
    const jsonData = JSON.parse(text);
    createMessage.error(jsonData.message, 5);
    return { name: '采集表小时数据', value: [[jsonData.message, ...errorArr]], row: 0 };
  }
};

// 加载采集表WEEK数据
export const loadCollectWeekUsageData = async (year) => {
  console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Start Download CollectWeekUsage`);
  const response = await RepFunApi.getCollectWeekUsageList(year);
  console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]End Download CollectWeekUsage`, response);
  if (response.success) {
    return { name: '采集表周数据', value: response.result, row: 0 };
  } else {
    createMessage.error(response.message, 5);
    return { name: '采集表周数据', value: [[response.message, ...errorArr]], row: 0 };
  }
};

// 加载采集表MTD数据
export const loadCollectMtdUsageData = async (year) => {
  console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Start Download CollectMtdUsage`);
  const response = await RepFunApi.getCollectMtdUsageList(year);
  console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]End Download CollectMtdUsage`);
  if (response.success) {
    return { name: '采集表MTD数据', value: response.result, row: 0 };
  } else {
    createMessage.error(response.message, 5);
    return { name: '采集表MTD数据', value: [[response.message, ...errorArr]], row: 0 };
  }
};

// 加载手输表班数据
export const loadManualClassUsageData = async (month) => {
  console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Start Download ManualClassUsage`);
  const response = await RepFunApi.getManualClassUsageDataList(month);
  if (response.data.type === 'application/zip') {
    const text = await JSZip.loadAsync(response.data).then((zipdata) => {
      return zipdata.file('apidata')!.async('text');
    });
    const jsonData = JSON.parse(text);
    console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]End Download ManualClassUsage`);
    return { name: '手输表班数据', value: jsonData, row: 0 };
  } else {
    const text = (await response.data.text) ? await response.data.text() : JSON.stringify(response.data);
    const jsonData = JSON.parse(text);
    createMessage.error(jsonData.message, 5);
    return { name: '手输表班数据', value: [[jsonData.message, ...errorArr]], row: 0 };
  }
};

// 加载采集表编辑日数据
export const loadEditCollectDayUsage = async (sheet, month) => {
  console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Start Download EditCollectDayUsage`);
  const response = await RepFunApi.getEditCollectDayUsageList(month);
  const text = await JSZip.loadAsync(response.data).then((zipdata) => {
    return zipdata.file('apidata')!.async('text');
  });
  const jsonData = JSON.parse(text);
  console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]End Download EditCollectDayUsage`);
  if (jsonData.length === 0) {
    const rowColNums = [sheet.getRowCount(), sheet.getColumnCount()];
    sheet.deleteRows(2, rowColNums[0] - 2);
    createMessage.error('本厂采集表点位数据有误，请修改后再重新打开', 5);
  } else {
    return { name: '采集表日数据修改', value: jsonData, row: 2 };
  }
};

// 加载采集表编辑班数据
export const loadEditCollectClassUsage = async (sheet, month) => {
  console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Start Download EditCollectClassUsage`);
  const response = await RepFunApi.getEditCollectClassUsageList(month);
  const text = await JSZip.loadAsync(response.data).then((zipdata) => {
    return zipdata.file('apidata')!.async('text');
  });
  const jsonData = JSON.parse(text);
  console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]End Download EditCollectClassUsage`);
  if (typeof jsonData === 'undefined' || jsonData.length === 0) {
    const rowColNums = [sheet.getRowCount(), sheet.getColumnCount()];
    sheet.deleteRows(2, rowColNums[0] - 2);
    createMessage.error('本厂采集表点位数据有误，请修改后再重新打开', 5);
  } else {
    return { name: '采集表班数据修改', value: jsonData, row: 2 };
  }
};

// 加载手输表编辑日数据
export const loadEditManualDayUsage = async (sheet, month, origindataObj, deptList, selDept) => {
  console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Start Download EditManualDayUsage`);
  const response = await RepFunApi.getEditManualDayUsageDataList(month);
  const text = await JSZip.loadAsync(response.data).then((zipdata) => {
    return zipdata.file('apidata')!.async('text');
  });
  const jsonData = JSON.parse(text);
  console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]End Download EditManualDayUsage`);
  if (typeof jsonData === 'undefined' || jsonData.length === 0) {
    const rowColNums = [sheet.getRowCount(), sheet.getColumnCount()];
    sheet.deleteRows(2, rowColNums[0] - 2);
    createMessage.error('本厂手输表点位数据有误，请修改后再重新打开', 5);
  } else {
    // 存储临时数据做逻辑部门筛选所用
    origindataObj.value.EditManualDayUsage = jsonData;
    // 过滤出所有的部门名称
    jsonData.forEach((elem) => {
      if (!elem[3]) {
        deptList.value.push('（空）');
      } else {
        deptList.value.push(elem[3]);
      }
    });
    deptList.value = [...new Set(deptList.value)];
    selDept.value = '';
    return { name: '手输表日数据录入', value: jsonData, row: 2 };
  }
};

// 加载手输表编辑班数据
export const loadEditManualClassUsage = async (sheet, month, origindataObj, deptList, selDept) => {
  console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Start Download EditManualClassUsage`);
  const response = await RepFunApi.getEditManualClassUsageDataList(month);
  const text = await JSZip.loadAsync(response.data).then((zipdata) => {
    return zipdata.file('apidata')!.async('text');
  });
  const jsonData = JSON.parse(text);
  console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]End Download EditManualClassUsage`);
  if (typeof jsonData === 'undefined' || jsonData.length === 0) {
    const rowColNums = [sheet.getRowCount(), sheet.getColumnCount()];
    sheet.deleteRows(2, rowColNums[0] - 2);
    createMessage.error('本厂手输表点位数据有误，请修改后再重新打开', 5);
  } else {
    // 存储临时数据做逻辑部门筛选所用
    origindataObj.value.EditManualClassUsage = jsonData;
    // 过滤出所有的部门名称
    jsonData.forEach((elem) => {
      if (!elem[3]) {
        deptList.value.push('（空）');
      } else {
        deptList.value.push(elem[3]);
      }
    });
    deptList.value = [...new Set(deptList.value)];
    selDept.value = '';
    return { name: '手输表班数据录入', value: jsonData, row: 2 };
  }
};

// 加载第三方表编辑日数据
export const loadEditThirdpartyDayUsage = async (sheet, month, origindataObj, deptList, selDept) => {
  console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Start Download EditThirdpartyDayUsage`);
  const response = await RepFunApi.getEditThirdpartyDayUsageDataList(month);
  const text = await JSZip.loadAsync(response.data).then((zipdata) => {
    return zipdata.file('apidata')!.async('text');
  });
  const jsonData = JSON.parse(text);
  console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]End Download EditThirdpartyDayUsage`);
  if (typeof jsonData === 'undefined' || jsonData.length === 0) {
    const rowColNums = [sheet.getRowCount(), sheet.getColumnCount()];
    sheet.deleteRows(2, rowColNums[0] - 2);
    createMessage.error('本厂第三方表点位数据有误，请修改后再重新打开', 5);
  } else {
    // 存储临时数据做逻辑部门筛选所用
    origindataObj.value.EditThirdpartyDayUsage = jsonData;
    // 过滤出所有的部门名称
    jsonData.forEach((elem) => {
      if (!elem[3]) {
        deptList.value.push('（空）');
      } else {
        deptList.value.push(elem[3]);
      }
    });
    deptList.value = [...new Set(deptList.value)];
    selDept.value = '';
    return { name: '第三方表日数据修改', value: jsonData, row: 2 };
  }
};

// 加载第三方表编辑班数据
export const loadEditThirdpartyClassUsage = async (sheet, month, origindataObj, deptList, selDept) => {
  console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Start Download EditThirdpartyClassUsage`);
  const response = await RepFunApi.getEditThirdpartyClassUsageDataList(month);
  const text = await JSZip.loadAsync(response.data).then((zipdata) => {
    return zipdata.file('apidata')!.async('text');
  });
  const jsonData = JSON.parse(text);
  console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]End Download EditThirdpartyClassUsage`);
  if (typeof jsonData === 'undefined' || jsonData.length === 0) {
    const rowColNums = [sheet.getRowCount(), sheet.getColumnCount()];
    sheet.deleteRows(2, rowColNums[0] - 2);
    createMessage.error('本厂第三方表点位数据有误，请修改后再重新打开', 5);
  } else {
    // 存储临时数据做逻辑部门筛选所用
    origindataObj.value.EditThirdpartyClassUsage = jsonData;
    // 过滤出所有的部门名称
    jsonData.forEach((elem) => {
      if (!elem[3]) {
        deptList.value.push('（空）');
      } else {
        deptList.value.push(elem[3]);
      }
    });
    deptList.value = [...new Set(deptList.value)];
    selDept.value = '';
    return { name: '第三方表班数据修改', value: jsonData, row: 2 };
  }
};

// 加载手输表日数据
export const loadManualDayUsageData = async (month) => {
  console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Start Download ManualDayUsage`);
  const response = await RepFunApi.getManualDayUsageDataList(month);
  if (response.data.type === 'application/zip') {
    const text = await JSZip.loadAsync(response.data).then((zipdata) => {
      return zipdata.file('apidata')!.async('text');
    });
    const jsonData = JSON.parse(text);
    console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]End Download ManualDayUsage`);
    return { name: '手输表日数据', value: jsonData, row: 0 };
  } else {
    const text = (await response.data.text) ? await response.data.text() : JSON.stringify(response.data);
    const jsonData = JSON.parse(text);
    createMessage.error(jsonData.message, 5);
    return { name: '采集表日数据修改', value: [[jsonData.message, ...errorArr]], row: 0 };
  }
};

// 加载手输表WEEK数据
export const loadManualWeekUsageData = async (year) => {
  console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Start Download ManualWeekUsage`);
  const response = await RepFunApi.getManualWeekUsageDataList(year);
  console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]End Download ManualWeekUsage`);
  if (response.success) {
    return { name: '手输表周数据', value: response.result, row: 0 };
  } else {
    createMessage.error(response.message, 5);
    return { name: '手输表周数据', value: [[response.message, ...errorArr]], row: 0 };
  }
};

// 加载手输表MTD数据
export const loadManualMtdUsageData = async (year) => {
  console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Start Download ManualMtdUsage`);
  const response = await RepFunApi.getManualMtdUsageDataList(year);
  console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]End Download ManualMtdUsage`);
  if (response.success) {
    return { name: '手输表MTD数据', value: response.result, row: 0 };
  } else {
    createMessage.error(response.message, 5);
    return { name: '手输表MTD数据', value: [[response.message, ...errorArr]], row: 0 };
  }
};

// 加载第三方表小时数据
export const loadThirdpartyHourUsageData = async (
  endDate: string | undefined = undefined,
  tagids: string | undefined = undefined,
  headFlag: string | undefined = undefined,
  headType: string | undefined = undefined
) => {
  console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Start Download ThirdpartyHourUsage`);
  const response = await RepFunApi.getThirdpartyHourUsageList(endDate, tagids, headFlag, headType);
  if (response.data.type === 'application/zip') {
    const text = await JSZip.loadAsync(response.data).then((zipdata) => {
      return zipdata.file('apidata')!.async('text');
    });
    const jsonData = JSON.parse(text);
    console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]End Download ThirdpartyHourUsage`);
    return { name: '第三方小时数据', value: jsonData, row: 0 };
  } else {
    const text = (await response.data.text) ? await response.data.text() : JSON.stringify(response.data);
    const jsonData = JSON.parse(text);
    createMessage.error(jsonData.message, 5);
    return { name: '第三方小时数据', value: [[jsonData.message, ...errorArr]], row: 0 };
  }
};

// 加载第三方班数据
export const loadThirdpartyClassUsageData = async (month) => {
  console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Start Download ThirdpartyClassUsage`);
  const response = await RepFunApi.getThirdpartyClassUsageDataList(month);
  if (response.data.type === 'application/zip') {
    const text = await JSZip.loadAsync(response.data).then((zipdata) => {
      return zipdata.file('apidata')!.async('text');
    });
    const jsonData = JSON.parse(text);
    console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]End Download ThirdpartyClassUsage`);
    return { name: '第三方班数据', value: jsonData, row: 0 };
  } else {
    const text = (await response.data.text) ? await response.data.text() : JSON.stringify(response.data);
    const jsonData = JSON.parse(text);
    createMessage.error(jsonData.message, 5);
    return { name: '第三方班数据', value: [[jsonData.message, ...errorArr]], row: 0 };
  }
};

// 加载第三方日数据
export const loadThirdpartyDayUsageData = async (month) => {
  console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Start Download ThirdpartyDayUsage`);
  const response = await RepFunApi.getThirdpartyDayUsageDataList(month);
  if (response.data.type === 'application/zip') {
    const text = await JSZip.loadAsync(response.data).then((zipdata) => {
      return zipdata.file('apidata')!.async('text');
    });
    const jsonData = JSON.parse(text);
    console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]End Download ThirdpartyDayUsage`);
    return { name: '第三方日数据', value: jsonData, row: 0 };
  } else {
    const text = (await response.data.text) ? await response.data.text() : JSON.stringify(response.data);
    const jsonData = JSON.parse(text);
    createMessage.error(jsonData.message, 5);
    return { name: '第三方日数据', value: [[jsonData.message, ...errorArr]], row: 0 };
  }
};

// 加载第三方WEEK数据
export const loadThirdpartyWeekUsageData = async (year) => {
  console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Start Download ThirdpartyWeekUsage`);
  const response = await RepFunApi.getThirdpartyWeekUsageDataList(year);
  console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]End Download ThirdpartyWeekUsage`);
  if (response.success) {
    return { name: '第三方周数据', value: response.result, row: 0 };
  } else {
    createMessage.error(response.message, 5);
    return { name: '第三方周数据', value: [[response.message, ...errorArr]], row: 0 };
  }
};

// 加载第三方MTD数据
export const loadThirdpartyMtdUsageData = async (year) => {
  console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Start Download ThirdpartyMtdUsage`);
  const response = await RepFunApi.getThirdpartyMtdUsageDataList(year);
  console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]End Download ThirdpartyMtdUsage`);
  if (response.success) {
    return { name: '第三方MTD数据', value: response.result, row: 0 };
  } else {
    createMessage.error(response.message, 5);
    return { name: '第三方MTD数据', value: [[response.message, ...errorArr]], row: 0 };
  }
};

// 加载中间表小时数据
export const loadOrsHourUsageData = async (
  endDate: string | undefined = undefined,
  tagids: string | undefined = undefined,
  headFlag: string | undefined = undefined,
  headType: string | undefined = undefined
) => {
  console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Start Download OrsHourUsage`);
  const response = await RepFunApi.getOrsdataHourUsageList(endDate, tagids, headFlag, headType);
  if (response.data.type === 'application/zip') {
    const text = await JSZip.loadAsync(response.data).then((zipdata) => {
      return zipdata.file('apidata')!.async('text');
    });
    const jsonData = JSON.parse(text);
    console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]End Download OrsHourUsage`);
    return { name: '中间表小时数据', value: jsonData, row: 0 };
  } else {
    const text = (await response.data.text) ? await response.data.text() : JSON.stringify(response.data);
    const jsonData = JSON.parse(text);
    createMessage.error(jsonData.message, 5);
    return { name: '中间表小时数据', value: [[jsonData.message, ...errorArr]], row: 0 };
  }
};

// 加载中间表日数据
export const loadOrsDayUsageData = async (month) => {
  console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Start Download OrsDayUsage`);
  const response = await RepFunApi.getOrsdataDayUsageList(month);
  if (response.data.type === 'application/zip') {
    const text = await JSZip.loadAsync(response.data).then((zipdata) => {
      return zipdata.file('apidata')!.async('text');
    });
    const jsonData = JSON.parse(text);
    console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]End Download OrsDayUsage`);
    return { name: '中间表日数据', value: jsonData, row: 0 };
  } else {
    const text = (await response.data.text) ? await response.data.text() : JSON.stringify(response.data);
    const jsonData = JSON.parse(text);
    createMessage.error(jsonData.message, 5);
    return { name: '中间表日数据', value: [[jsonData.message, ...errorArr]], row: 0 };
  }
};

// 加载中间表班数据
export const loadOrsClassUsageData = async (month) => {
  console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Start Download OrsClassUsage`);
  const response = await RepFunApi.getOrsdataClassUsageList(month);
  if (response.data.type === 'application/zip') {
    const text = await JSZip.loadAsync(response.data).then((zipdata) => {
      return zipdata.file('apidata')!.async('text');
    });
    const jsonData = JSON.parse(text);
    console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]End Download OrsClassUsage`);
    return { name: '中间表班数据', value: jsonData, row: 0 };
  } else {
    const text = (await response.data.text) ? await response.data.text() : JSON.stringify(response.data);
    const jsonData = JSON.parse(text);
    createMessage.error(jsonData.message, 5);
    return { name: '中间表班数据', value: [[jsonData.message, ...errorArr]], row: 0 };
  }
};

// 加载中间表周数据
export const loadOrsWeekUsageData = async (year) => {
  console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Start Download OrsWeekUsage`);
  const response = await RepFunApi.getOrsdataWeekUsageList(year);
  console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]End Download OrsWeekUsage`);
  if (response.success) {
    return { name: '中间表周数据', value: response.result, row: 0 };
  } else {
    createMessage.error(response.message, 5);
    return { name: '中间表周数据', value: [[response.message, ...errorArr]], row: 0 };
  }
};

// 加载中间表MTD数据
export const loadOrsMtdUsageData = async (year) => {
  console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Start Download OrsMtdUsage`);
  const response = await RepFunApi.getOrsdataMtdUsageList(year);
  console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]End Download OrsMtdUsage`);
  if (response.success) {
    return { name: '中间表MTD数据', value: response.result, row: 0 };
  } else {
    createMessage.error(response.message, 5);
    return { name: '中间表MTD数据', value: [[response.message, ...errorArr]], row: 0 };
  }
};
