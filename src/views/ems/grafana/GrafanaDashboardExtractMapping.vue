<template>
  <a-spin :spinning="pageLoading">
    <div class="p-2">
      <!--查询区域-->
      <div class="jeecg-basic-table-form-container">
        <a-form ref="formRef" class="inline-form-wrap">
          <div class="flex-col flex-center">
            <div style="font-weight: bold; margin-bottom: 10px">报表名称：</div>
            <div
              class="flex-row flex-vcenter flex-hcenter"
              style="color: #d92622; min-width: 120px; cursor: pointer; font-weight: bold"
              @click="openPreviewPage(gfInstanceData.instanceUrl, dashboardData.uid)"
            >
              {{ dashboardData.title }}
            </div>
            <div style="margin-top: 10px">
              <a-button type="primary" @click="handleReExtract" size="small">重新解析</a-button>
            </div>
          </div>

          <!--源实例相关数据-->
          <table class="tb-instance">
            <tbody>
              <tr>
                <td rowspan="7" class="lbl-txt" style="text-align: center">
                  <div>源实例</div>
                  <div>{{ gfInstanceData.instanceName }}</div>
                </td>
              </tr>
              <tr>
                <td class="lbl-txt">GF实例地址: </td><td>{{ gfInstanceData.instanceUrl }}</td>
              </tr>
              <tr>
                <td class="lbl-txt">点位数据库: </td><td>{{ gfInstanceData.pointDbUrl }}</td>
              </tr>
              <tr>
                <td class="lbl-txt">用户名密码: </td
                ><td>{{ gfInstanceData.pointDbUname }} / {{ gfInstanceData.pointDbPassword ? '密码已配置' : '密码未配置' }}</td>
              </tr>
              <tr>
                <td class="lbl-txt">基础点位表: </td
                ><td>{{ gfInstanceData.basePointTb }} / {{ gfInstanceData.baseTbIdCol }} / {{ gfInstanceData.baseTbNameCol }}</td>
              </tr>
              <tr>
                <td class="lbl-txt">中间点位表: </td
                ><td>{{ gfInstanceData.orsPointTb }} / {{ gfInstanceData.orsTbIdCol }} / {{ gfInstanceData.orsTbNameCol }}</td>
              </tr>
              <tr>
                <td class="lbl-txt">GF数据源: </td>
                <td>
                  <template v-if="oldGfDatasourceList.length > 0">
                    <a-popover placement="top">
                      <template #content>
                        <div v-for="(item, inx) in oldGfDatasourceList" :key="inx"
                          ><strong>[{{ item.type }}]</strong> {{ item.name }}</div
                        >
                      </template>
                      <span style="cursor: pointer; font-weight: bold">{{ oldGfDatasourceList.length + '个' }}</span>
                    </a-popover>
                  </template>
                  <template v-else>
                    <div>确认已配置并同步成功</div>
                  </template>
                </td>
              </tr>
            </tbody>
          </table>

          <div class="flex-col flex-vcenter flex-hcenter" style="width: 120px">
            <div style="font-size: 16px; font-weight: 600; color: #606266bb">迁移</div>
            <!-- 右箭头 SVG -->
            <svg width="80" height="30" viewBox="0 0 80 30" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
              <!-- 横线部分 -->
              <line x1="2" y1="15" x2="58" y2="15" stroke="#606266aa" stroke-width="5" stroke-linecap="round" />
              <!-- 箭头部分 -->
              <polyline points="50,6 74,15 50,24" fill="none" stroke="#606266aa" stroke-width="5" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
          </div>

          <!--目标实例相关数据-->
          <table class="tb-instance">
            <tbody>
              <tr>
                <td rowspan="7" class="lbl-txt" style="text-align: center">
                  <div>目标实例</div>
                  <div>
                    <j-dict-select-tag
                      placeholder="请选择"
                      v-model:value="selGfInstanceId"
                      :options="gfInstanceOptions"
                      @change="handleSelGfInstanceChange"
                      style="width: 150px"
                      allow-clear
                      size="small"
                    />
                  </div>
                </td>
              </tr>
              <tr>
                <td class="lbl-txt">GF实例地址: </td><td>{{ targetInstance.instanceUrl || '未配置' }}</td>
              </tr>
              <tr>
                <td class="lbl-txt">点位数据库: </td><td>{{ targetInstance.pointDbUrl || '未配置' }}</td>
              </tr>
              <tr>
                <td class="lbl-txt">用户名密码: </td>
                <td>{{ targetInstance.pointDbUname || '用户未配置' }} / {{ targetInstance.pointDbPassword ? '密码已配置' : '密码未配置' }}</td>
              </tr>
              <tr>
                <td class="lbl-txt">基础点位表: </td
                ><td
                  >{{ targetInstance.basePointTb || '表名未配置' }} / {{ targetInstance.baseTbIdCol || 'ID字段未配置' }} /
                  {{ targetInstance.baseTbNameCol || '名称字段未配置' }}</td
                >
              </tr>
              <tr>
                <td class="lbl-txt">中间点位表: </td
                ><td
                  >{{ targetInstance.orsPointTb || '表名未配置' }} / {{ targetInstance.orsTbIdCol || 'ID字段未配置' }} /
                  {{ targetInstance.orsTbNameCol || '名称字段未配置' }}</td
                >
              </tr>
              <tr>
                <td class="lbl-txt">GF数据源: </td>
                <td>
                  <template v-if="targetGfDatasourceList.length > 0">
                    <a-popover placement="top">
                      <template #content>
                        <div v-for="(item, inx) in targetGfDatasourceList" :key="inx"
                          ><strong>[{{ item.type }}]</strong> {{ item.name }}</div
                        >
                      </template>
                      <span style="cursor: pointer; font-weight: bold">{{ targetGfDatasourceList.length + '个' }}</span>
                    </a-popover>
                  </template>
                  <template v-else>
                    <div>确认已配置并同步成功</div>
                  </template>
                </td>
              </tr>
            </tbody>
          </table>

          <div class="flex-col flex-vcenter flex-hcenter" style="font-size: 15px">
            <div style="color: #000">匹配度</div>
            <div style="color: #d92622; font-weight: bold; font-size: 18px">{{ (totalMatchScore * 100).toFixed(0) }}%</div>
          </div>
          <div class="flex-col flex-vcenter flex-hcenter">
            <a-button type="primary" v-if="selGfInstanceId" @click="handlePointMatch">匹配点位</a-button>
            <a-button type="primary" v-if="selGfInstanceId" :disabled="totalMatchScore < 0.5" @click="handleDeployDashboard" style="margin-top: 10px"
              >发布报表</a-button
            >
          </div>
        </a-form>
      </div>
      <!--引用表格-->
      <BasicTable @register="registerTable" :rowSelection="null">
        <!--原始查询语句显示-->
        <template #querySqlShow="{ record }">
          <div class="query-item" v-for="(item, inx) in JSON.parse(record.queryText)" :key="inx">
            {{ item }}
          </div>
        </template>

        <!-- 点位列表显示 -->
        <template #pointListShow="{ record }">
          <div class="point-item" v-for="(item, inx) in JSON.parse(record.pointIds)" :key="inx">
            <strong>[{{ item.pointId }}]</strong> {{ item.pointName }}
          </div>
        </template>

        <!-- 自动匹配点位列表显示 -->
        <template #matchListShow="{ record }">
          <template v-if="record.pointMapList">
            <table class="tb-point-mapping" v-for="(item, inx) in record.pointMapList" :key="inx">
              <tbody>
                <tr>
                  <td colspan="6" class="point-old">[{{ item.oldPointId }}]</td>
                </tr>
                <tr>
                  <td colspan="2" class="point-old-name">目标数据源：</td>
                  <td colspan="4" style="padding: 5px">
                    <j-dict-select-tag
                      placeholder="目标数据源"
                      v-model:value="newPointDataSourceMap[item.id]"
                      :options="targetGfDataSourceOptions"
                      style="text-align: left"
                      show-search
                      allow-clear
                      size="small"
                    />
                  </td>
                </tr>
                <template v-if="item.newPointId">
                  <tr>
                    <td>
                      <a-checkbox :checked="true" />
                    </td>
                    <td class="point-new">
                      <div style="min-width: 40px">{{ item.newPointId }}</div>
                    </td>
                    <td style="cursor: pointer">
                      <a-tooltip placement="right">
                        <template #title>
                          <span>{{ item.newPointName }}</span>
                        </template>
                        <div class="point-new-name">{{ item.newPointName }}</div>
                      </a-tooltip>
                    </td>
                    <td>
                      <div style="min-width: 40px">{{ (item.score * 100).toFixed(0) }}%</div>
                    </td>
                    <td style="padding: 5px">
                      {{ item.matchState_dictText }}
                    </td>
                    <td style="padding: 5px">
                      <a-button type="primary" size="small" @click="changeMappingPoint(item.id, record.dbType, item.newPointId)">更改</a-button>
                    </td>
                  </tr>
                </template>
                <template v-else>
                  <tr v-for="(jtem, jnx) in JSON.parse(item.autoMatch)" :key="jnx">
                    <td>
                      <a-checkbox :checked="confirmPointMap[record.panelId + '-' + item.oldPointId] === jnx" />
                    </td>
                    <td class="point-new">
                      <div style="min-width: 40px">{{ jtem.newPointId }}</div>
                    </td>
                    <td style="cursor: pointer" @click="handleSwitchConfirmPoint(record.panelId, item.oldPointId, jnx)">
                      <a-tooltip placement="right">
                        <template #title>
                          <span>{{ jtem.newPointName }}</span>
                        </template>
                        <div class="point-new-name">{{ jtem.newPointName }}</div>
                      </a-tooltip>
                    </td>
                    <td>
                      <div style="min-width: 40px">{{ (jtem.score * 100).toFixed(0) }}%</div>
                    </td>
                    <td v-if="jnx === 0" :rowspan="JSON.parse(item.autoMatch).length" style="padding: 5px">
                      {{ item.matchState_dictText }}
                    </td>
                    <td v-if="jnx === 0" :rowspan="JSON.parse(item.autoMatch).length" style="padding: 5px">
                      <a-button type="primary" size="small" @click="handleConfirmPoint(item, record.panelId)">确定</a-button>
                    </td>
                  </tr>
                </template>
              </tbody>
            </table>
          </template>
        </template>

        <!--操作栏-->
        <template #action="{ record }">
          <TableAction :actions="getTableAction(record)" />
        </template>
      </BasicTable>
      <!-- 表单区域 -->
      <GrafanaDashboardExtractModal ref="registerModal" @success="handleSuccess" />
    </div>

    <!-- 手动分配点位弹窗 -->
    <j-modal
      title="手动分配点位"
      width="300"
      :visible="dlgManualPointVisible"
      :maxHeight="300"
      :canFullscreen="false"
      @ok="handleOk"
      @cancel="handleCancel"
      cancelText="关闭"
    >
      <div class="flex-row flex-hcenter flex-vcenter" style="padding: 20px">
        <div style="width: 100px">请选择点位</div>
        <j-dict-select-tag
          placeholder="请选择"
          show-search
          :getPopupContainer="getPopupContainer"
          v-model:value="selPointId"
          :options="chgPointOptions"
          style="width: 300px"
          allow-clear
        />
      </div>
    </j-modal>
  </a-spin>
</template>

<script lang="ts" name="grafana-dashboardExtractMapping" setup>
  import { ref, reactive, onMounted } from 'vue';
  import { useRoute } from 'vue-router';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { mappingColumns } from './GrafanaDashboardExtract.data';
  import { list, getDashboardData, doDashboardExtract } from './GrafanaDashboardExtract.api';
  import { list as listInstance } from './GrafanaInstance.api';
  import { list as listPointMapping, listPointOptions, doDashboardPointMatch, doConfirmPointMapping } from './GrafanaPointMapping.api';
  import { list as listDataSource } from './GrafanaDatasource.api';
  import { doDeployDashboard } from './GrafanaMigrationLog.api';
  import GrafanaDashboardExtractModal from './components/GrafanaDashboardExtractModal.vue';
  import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';
  import JModal from '/@/components/Modal/src/JModal/JModal.vue';

  const { createMessage } = useMessage();
  const formRef = ref();
  const queryParam = reactive<any>({});
  const registerModal = ref();
  const route = useRoute();
  const dashboardId = ref(route.query.id as string);
  const dashboardData = ref<any>({});
  const gfInstanceData = ref<any>({});
  const pageLoading = ref(false);
  const gfInstanceList = ref<any>([]);
  const selGfInstanceId = ref<string>('');
  const gfInstanceOptions = ref<any>([]);
  const targetInstance = ref<any>({});
  //用户确认迁移点位
  const confirmPointMap = ref<any>({});
  //点位匹配列表
  const gfPointMappingList = ref<any>([]);
  //手动分配点位弹窗
  const dlgManualPointVisible = ref(false);
  //正在更改的点位ID
  const chgPointMapId = ref<number>(0);
  //当前下拉点位选项
  const chgPointOptions = ref<any>([]);
  //中间点位下拉选项
  const orsPointOptions = ref<any>([]);
  //基础点位下拉选项
  const basePointOptions = ref<any>([]);
  //选中的点位ID
  const selPointId = ref<string>('');
  //总匹配度(报表中已确认的点位除以总点位)
  const totalMatchScore = ref<number>(0);
  //源GF数据源列表
  const oldGfDatasourceList = ref<any>([]);
  //目标GF数据源列表
  const targetGfDatasourceList = ref<any>([]);
  //目标GF数据源选项
  const targetGfDataSourceOptions = ref<any>([]);
  //匹配GF解析面板数据源
  const newPointDataSourceMap = ref<any>({});

  const getPopupContainer = () => document.body;

  //注册table数据
  const { tableContext } = useListPage({
    tableProps: {
      api: list,
      columns: mappingColumns,
      canResize: false,
      pagination: false,
      useSearchForm: false,
      showTableSetting: false,
      actionColumn: {
        title: '操作',
        width: 60,
        fixed: 'right',
      },
      showActionColumn: false,
      beforeFetch: async (params) => {
        const paramsObj = Object.assign(params, queryParam);
        paramsObj.dashboardId = dashboardId.value;
        paramsObj.pageSize = 99999;
        return paramsObj;
      },
      afterFetch: (records) => {
        // 处理 confirmPointMap
        records.forEach((item) => {
          JSON.parse(item.pointIds).forEach((elem) => {
            confirmPointMap.value[item.panelId + '-' + elem.pointId] = 0;
          });
        });
        // 增加字段
        const newRecords = records.map((item) => ({
          ...item,
          instanceUrl: gfInstanceData.value.instanceUrl || '',
          uid: dashboardData.value.uid || '',
        }));

        return newRecords;
      },
    },
  });
  const [registerTable, { reload, getDataSource, setTableData }, { selectedRowKeys }] = tableContext;

  /**
   * 编辑事件
   */
  function handleEdit(record: Recordable) {
    registerModal.value.disableSubmit = false;
    registerModal.value.edit(record);
  }

  /**
   * 成功回调
   */
  function handleSuccess() {
    selectedRowKeys.value = [];
    reload();
  }

  /**
   * 操作栏
   */
  function getTableAction(record) {
    return [
      {
        label: '确定',
        onClick: handleEdit.bind(null, record),
        auth: 'grafana:grafana_dashboard_extract:edit',
      },
    ];
  }

  /**
   * 选择目标实例
   * @author Rocye
   */
  function handleSelGfInstanceChange(value) {
    selGfInstanceId.value = value;
    if (value) {
      const target = gfInstanceList.value.find((item) => item.id === value);
      targetInstance.value = target;
    } else {
      targetInstance.value = {};
    }
    //加载所选实例的点位映射列表
    loadPointMappingList();
    //加载基础点位和中间点位
    loadPointData();
    //加载目标GF数据源列表
    loadTargetGfDataSource();
  }

  /**
   * 打开预览页面(报表或仪表盘)
   * @author Rocye
   */
  function openPreviewPage(instanceUrl: string, dashboardUId: string, panelId?: string) {
    let url = `${instanceUrl}/d/${dashboardUId}`;
    if (panelId) {
      url += `?viewPanel=panel-${panelId}`;
    }
    window.open(url, '_blank');
  }

  /**
   * 重新解析报表
   * @author Rocye
   */
  async function handleReExtract() {
    pageLoading.value = true;
    const res = await doDashboardExtract({ instanceId: gfInstanceData.value.id, dashboardId: dashboardId.value });
    if (res.success) {
      createMessage.success(res.message);
      reload();
    } else {
      createMessage.error(res.message);
    }
    pageLoading.value = false;
  }

  /**
   * 报表点位匹配
   * @author Rocye
   */
  async function handlePointMatch() {
    if (
      !targetInstance.value.pointDbUrl ||
      !targetInstance.value.pointDbUname ||
      !targetInstance.value.pointDbPassword ||
      !targetInstance.value.basePointTb ||
      !targetInstance.value.baseTbIdCol ||
      !targetInstance.value.baseTbNameCol ||
      !targetInstance.value.orsPointTb ||
      !targetInstance.value.orsTbIdCol ||
      !targetInstance.value.orsTbNameCol
    ) {
      createMessage.warning('所选目标实例的点位数据库信息不完整，请先进行配置');
      return;
    }
    pageLoading.value = true;
    const res = await doDashboardPointMatch({
      sourceInstanceId: gfInstanceData.value.id,
      dashboardId: dashboardId.value,
      targetInstanceId: selGfInstanceId.value,
    });
    if (res.success) {
      createMessage.success(res.message);
      //加载所选实例的点位映射列表
      loadPointMappingList();
    } else {
      createMessage.error(res.message);
    }
    pageLoading.value = false;
  }

  /**
   * 手动切换确认前三的点位匹配
   * @author Rocye
   */
  function handleSwitchConfirmPoint(panelId: string, oldPointId: string, inx: number) {
    confirmPointMap.value[panelId + '-' + oldPointId] = inx;
  }

  /**
   * 手动切换确认前三的点位匹配
   * @author Rocye
   */
  async function loadPointMappingList() {
    //加载所有GF实例作为目标实例下拉选择
    const res = await listPointMapping({ pageSize: 99999, dashboardId: dashboardId.value, toInstanceId: selGfInstanceId.value || 'NULL' });
    gfPointMappingList.value = res.records || [];

    //计算总的匹配度
    if (gfPointMappingList.value.length > 0) {
      const confirmCount = gfPointMappingList.value.filter((item) => item.matchState == 2 || item.matchState == 3).length;
      totalMatchScore.value = confirmCount / gfPointMappingList.value.length;
    } else {
      totalMatchScore.value = 0;
    }

    //初始化目标点位数据源
    gfPointMappingList.value.forEach((item) => {
      newPointDataSourceMap.value[item.id] = item.datasourceId;
    });

    const tableData = getDataSource();
    const newTableData = tableData.map((item) => {
      return {
        ...item,
        pointMapList: gfPointMappingList.value.filter((jtem) => jtem.extractId === item.id),
      };
    });

    //自动匹配未设置的点位数据源
    newTableData.forEach((item) => {
      item.pointMapList.forEach((jtem) => {
        if (!jtem.datasourceId) {
          //先通过解析记录中的datasource字段从旧数据源列表中获取具体数据源，从而得到数据源类型
          if (item['datasource'].includes('{')) {
            const datasource = JSON.parse(item['datasource']);
            console.log('datasource', datasource);
            newPointDataSourceMap.value[jtem.id] = targetGfDatasourceList.value.find((ktem) => ktem.type === datasource.type).id;
          } else {
            //V12以下的按名称匹配
            const datasource = oldGfDatasourceList.value.find((ktem) => ktem.name === item['datasource']);
            console.log('datasource', datasource);
            newPointDataSourceMap.value[jtem.id] = targetGfDatasourceList.value.find((ktem) => ktem.type === datasource.type).id;
          }
        }
      });
    });

    setTableData(newTableData);
  }

  /**
   * 确认匹配点位(包括自动点位的确认与手动点位的提交)
   * @author Rocye
   */
  function handleConfirmPoint(pointMapItem: any, panelId: string) {
    console.log('handleConfirmPoint', pointMapItem, panelId, newPointDataSourceMap.value);
    if (!newPointDataSourceMap.value[pointMapItem.id]) {
      createMessage.warning('请先选择目标点位数据源');
      return;
    }
    //获取当前所选的点位下标
    const inx = confirmPointMap.value[panelId + '-' + pointMapItem.oldPointId];
    //从得分排名点位中获取当前所选的点位
    const newPointItem = JSON.parse(pointMapItem.autoMatch)[inx];
    manualConfirmPoint(
      pointMapItem.id,
      newPointItem.newPointId,
      newPointItem.newPointName,
      newPointItem.score,
      newPointDataSourceMap.value[pointMapItem.id]
    );
  }

  /**
   * 手动确认点位
   * @author Rocye
   */
  async function manualConfirmPoint(pointMapId: number, newPointId: string, newPointName: string, score: number, datasourceId: string) {
    pageLoading.value = true;
    const res = await doConfirmPointMapping({
      id: pointMapId,
      newPointId: newPointId,
      newPointName: newPointName,
      score: score,
      datasourceId: datasourceId,
    });
    if (res.success) {
      createMessage.success(res.message);
      loadPointMappingList();
    } else {
      createMessage.error(res.message);
    }
    pageLoading.value = false;
  }

  /**
   * 更改手动切换确认前三的点位匹配
   * @author Rocye
   */
  function changeMappingPoint(pointMapId: number, dbType: number, newPointId: string) {
    if (!newPointDataSourceMap.value[pointMapId]) {
      createMessage.warning('请先选择目标点位数据源');
      return;
    }
    dlgManualPointVisible.value = true;
    chgPointMapId.value = pointMapId;
    if (dbType === 1) {
      chgPointOptions.value = orsPointOptions.value;
    } else {
      chgPointOptions.value = basePointOptions.value;
    }
    selPointId.value = newPointId;
  }

  /**
   * 确定按钮回调事件
   */
  function handleOk() {
    dlgManualPointVisible.value = false;
    const selPointOption = chgPointOptions.value.find((item) => item.value === selPointId.value);
    manualConfirmPoint(chgPointMapId.value, selPointOption.value, selPointOption.name, 1.01, newPointDataSourceMap.value[chgPointMapId.value]);
  }

  /**
   * 取消按钮回调事件
   */
  function handleCancel() {
    dlgManualPointVisible.value = false;
  }

  /**
   * 加载点位数据
   * @author Rocye
   */
  function loadPointData() {
    listPointOptions({ instanceId: selGfInstanceId.value, dbType: 1 }).then((res) => {
      if (res.success) {
        basePointOptions.value = res.result.map((item) => {
          return {
            label: '[' + item.id + ']' + item.name,
            value: item.id,
            name: item.name,
          };
        });
      } else {
        console.error('获取基础点位数据失败：', res.message);
      }
    });
    listPointOptions({ instanceId: selGfInstanceId.value, dbType: 2 }).then((res) => {
      if (res.success) {
        orsPointOptions.value = res.result.map((item) => {
          return {
            label: '[' + item.id + ']' + item.name,
            value: item.id,
            name: item.name,
          };
        });
      } else {
        console.error('获取中间点位数据失败：', res.message);
      }
    });
  }

  /**
   * 发布报表
   * @author Rocye
   */
  async function handleDeployDashboard() {
    pageLoading.value = true;
    const res = await doDeployDashboard({
      dashboardId: dashboardId.value,
      targetInstanceId: selGfInstanceId.value,
    });
    if (res.success) {
      createMessage.success(res.message);
    } else {
      createMessage.error(res.message);
    }
    pageLoading.value = false;
  }

  /**
   * 加载目标GF所有数据源
   * @author Rocye
   */
  async function loadTargetGfDataSource() {
    const res = await listDataSource({ instanceId: selGfInstanceId.value });
    targetGfDatasourceList.value = res.records || [];
    targetGfDataSourceOptions.value = targetGfDatasourceList.value.map((item) => {
      return {
        label: item.name + (item.url ? '[' + item.url + ']' : ''),
        value: item.id,
      };
    });
  }

  /**
   * 加载源GF所有数据源
   * @author Rocye
   */
  async function loadOldGfDataSource() {
    const res = await listDataSource({ instanceId: gfInstanceData.value.id });
    oldGfDatasourceList.value = res.records || [];
  }

  /**
   * 页面初始化
   */
  onMounted(() => {
    //加载源实例所有相关数据
    getDashboardData({ dashboardId: dashboardId.value }).then((res) => {
      console.log('获取成功：', res);
      dashboardData.value = res.result.dashboard || {};
      gfInstanceData.value = res.result.gfInstance || {};
    });

    //加载所有GF实例作为目标实例下拉选择
    listInstance({ pageSize: 99999 }).then((res) => {
      gfInstanceList.value = res.records || [];
      gfInstanceOptions.value = gfInstanceList.value
        .filter((item) => item.id !== gfInstanceData.value.id)
        .map((item) => {
          return {
            label: item.instanceName,
            value: item.id,
          };
        });
    });

    //加载源GF实例所有数据源(用于转换面板数据的名称或uid为数据源类型，以便自动匹配目标GF实例的数据源)
    loadOldGfDataSource();
  });
</script>

<style lang="less" scoped>
  .inline-form-wrap {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
    padding-bottom: 10px;
  }
  .inline-form-item {
    flex: 0 0 auto;
  }
  .point-item {
    padding: 2px;
    display: inline-block;
    max-width: 250px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    vertical-align: middle;
  }
  .tb-instance {
    border-collapse: collapse;
    border: 1px solid #f0f0f0;
    td {
      border: 1px solid #f0f0f0;
      padding: 0px 8px;
    }
  }
  .tb-point-mapping {
    width: auto;
    margin-top: 8px;
    border-collapse: collapse;
    border: 1px solid #dcdfe6;
    td {
      border: 1px solid #dcdfe6;
      padding: 0px 8px;
    }
    .point-old {
      font-weight: 500;
      color: #000;
      text-align: center;
      background-color: #fbfbfb;
    }
    .point-new {
      font-weight: 500;
      color: #000;
      margin: 0px 4px;
      text-align: center;
    }
    .point-new-name {
      display: inline-block;
      width: 150px;
      overflow: hidden;
      white-space: nowrap;
      text-overflow: ellipsis;
      vertical-align: middle;
    }
  }
  .tb-point-mapping:first-child {
    margin-top: 0;
  }

  .lbl-txt {
    text-align: right;
    padding-right: 10px;
    font-weight: bold;
  }
  .query-item {
    text-align: left;
    font-family: 'Courier New', Courier, monospace;
    color: black;
    border: 1px dashed #d9d9d9;
    border-radius: 4px;
    padding: 10px;
    margin-bottom: 5px;
    &:last-child {
      margin-bottom: 0;
    }
  }
  .jeecg-basic-table-form-container {
    padding: 0;
    .table-page-search-submitButtons {
      display: block;
      margin-bottom: 3px;
      white-space: nowrap;
    }
    .query-group-cust {
      min-width: 100px !important;
    }
    .query-group-split-cust {
      width: 30px;
      display: inline-block;
      text-align: center;
    }
    .ant-form-item:not(.ant-form-item-with-help) {
      margin-bottom: 3px;
      height: 32px;
    }
    :deep(.ant-picker),
    :deep(.ant-input-number) {
      width: 100%;
    }
  }
</style>
