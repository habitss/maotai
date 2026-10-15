/**
 * @description: 用于配置编译构建时需要排除的模块
 */

const allModules = ['baseModules', 'dataReport', 'produceManage', 'monitor', 'demo'];
// 是否排除模块
// const BASE_MODULES = true; // 主数据模块
const DATA_REPORT = true; // 数据报表模块
const MONITOR = true; // 监控模块
const DEMO = true; // 演示模块

const DOCUMENT_MANAGE = true; // 文档管理模块
const WORK_ORDER = true; // 工单模块
const PLAN_MODULES = true; // 计划模块
const WORKSTATION = true; // 工作站模块

export function setExcludeFolder() {
  let excludeFolder = [];
  // 排除baseModules模块
  // const excludeBaseModules = [/src\/views\/mainDataManage\/.*/, /src\/views\/system\/.*/, /src\/views\/sys\/.*/];  
  // 排除monitor模块
  const excludeMonitor = [/src\/views\/monitor\/.*/];
  // 排除demo模块
  const excludeDemo = [/src\/views\/demo\/.*/];
  // 排除dataReport模块
  const excludeDataReport = [/src\/views\/report\/.*/];
  // 排除documentManage模块
  const excludeDocumentManage = [/src\/views\/setting\/fileManage\/.*/];

  // 排除workOrder模块
  const excludeWorkOrder = [
    /src\/views\/mainDataManage\/workOrder\/.*/,
    /src\/views\/mainDataManage\/quartzTaskV2\/.*/,
    /src\/views\/mainDataManage\/piMainData\/.*/,
    /src\/views\/mainDataManage\/exceptionScene\/.*/,
    /src\/views\/mainDataManage\/ExceptionPushScheme\/.*/,
    /src\/views\/mainDataManage\/exceptionOrder\/.*/,
    /src\/views\/productionManagement\/productionExecutiveManagement\/.*/,
  ];

  // 排除workstation模块
  const excludeWorkstation = [
    /src\/views\/mainDataManage\/workstationMainData\/.*/,
    /src\/views\/mainDataManage\/ReportMappingCode\/.*/,
    /src\/views\/mainDataManage\/hourDataCheck\/.*/,
  ];
  // 排除plan模块
  const excludePlan = [
    /src\/views\/energyManagement\/.*/,
    /src\/views\/sakeManage\/.*/,
    /src\/views\/mainDataManage\/materialMasterData\/.*/,
    /src\/views\/productionManagement\/planManagement\/.*/,
    /src\/views\/plan\/.*/,
    /src\/views\/plan\/.*/,
  ];

  if (!MONITOR) {
    excludeFolder = excludeFolder.concat(excludeMonitor);
  }
  if (!DEMO) {
    excludeFolder = excludeFolder.concat(excludeDemo);
  }
  // if (!BASE_MODULES) {
  //   excludeFolder = excludeFolder.concat(excludeBaseModules);
  // }
  if (!DATA_REPORT) {
    excludeFolder = excludeFolder.concat(excludeDataReport);
  }

  excludeFolder = splitExcludeFolder(DOCUMENT_MANAGE, excludeDocumentManage, excludeFolder);
  excludeFolder = splitExcludeFolder(WORK_ORDER, excludeWorkOrder, excludeFolder);
  excludeFolder = splitExcludeFolder(WORKSTATION, excludeWorkstation, excludeFolder);
  excludeFolder = splitExcludeFolder(PLAN_MODULES, excludePlan, excludeFolder);

  console.log('构建时排除的模块如下：excludeFolder', excludeFolder);

  return excludeFolder;
}

//设定构建时需要排除的文件夹
function splitExcludeFolder(isBuild: boolean, modulesArr: array, excludeFolder: array) {
  if (!isBuild) {
    excludeFolder = excludeFolder.concat(modulesArr);
  }
  return excludeFolder;
}
/*******具体模块划分方式如下
主应用 - 主数据，（主数据管理，系统配置，租户管理，我的租户，低代码开发）
- A子应用 - 数据报表（数据可视化，数据 ，数据治理，统计报表，）
- B子应用 - 生产管理（生产管理模块）
- 子应用 - 系统监控（系统监控）
- 子应用 - DEMO和组件模块
     - 组件和功能，Mock示例，页面和导航，JEECG系统监控
- 子应用 - 其他（后期扩展）
***/