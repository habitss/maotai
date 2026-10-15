<template>
  <a-spin :spinning="pageLoading">
  <div class="p-2">
    <!--查询区域-->
    <div class="jeecg-basic-table-form-container">
      <a-form ref="formRef" @keyup.enter="searchQuery" :model="queryParam" :label-col="labelCol" :wrapper-col="wrapperCol">
        <a-row :gutter="24">
          <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
            <a-form-item name="factoryCode">
              <template #label><span title="部门">部门</span></template>
              <j-dict-select-tag
                placeholder="请选择部门"
                v-model:value="queryParam.deptCode"
                dictCode="sys_depart where org_category=4,depart_name,org_code"
                allow-clear
              />
            </a-form-item>
          </a-col>
          <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
            <a-form-item name="tagKind">
              <template #label><span title="点位大类">点位大类</span></template>
              <j-dict-select-tag
                  :showChooseOption="false"
                  placeholder="请选择点位大类"
                  v-model:value="queryParam.tagKind"
                  :options="tagKindList"
                  @change="changeTagKind"
                  allow-clear
                />
            </a-form-item>
          </a-col>
          <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
            <a-form-item name="tagId">
              <template #label><span title="点位">点位</span></template>
                <a-select
                  v-model:value="queryParam.tagId"
                  placeholder="请选择点位"
                  show-search
                  :filterOption="filterOption"
                  allow-clear
                >
                  <a-select-option v-for="d in tagOptions" :key="d.tagId" :value="d.tagId">[{{ d.tagId }}] {{ d.tagName }}</a-select-option>
                </a-select>
            </a-form-item>
          </a-col>
          <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
            <a-form-item name="alarmTime">
              <template #label><span title="报警时间">报警时间</span></template>
              <a-range-picker showTime value-format="YYYY-MM-DD HH:mm:ss" v-model:value="queryParam.alarmTime" class="query-group-cust" />
            </a-form-item>
          </a-col>
          <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
            <a-form-item name="alarmContent">
              <template #label><span title="报警内容">报警内容</span></template>
              <j-input placeholder="请输入报警内容" v-model:value="queryParam.alarmContent" allow-clear />
            </a-form-item>
          </a-col>
          <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
            <span style="float: left; overflow: hidden" class="table-page-search-submitButtons">
              <a-button type="primary" preIcon="ant-design:search-outlined" @click="searchQuery">查询</a-button>
              <a-button type="primary" preIcon="ant-design:reload-outlined" @click="searchReset" style="margin-left: 8px">重置</a-button>
            </span>
          </a-col>
        </a-row>
      </a-form>
    </div>
    <!--引用表格-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button type="primary" v-auth="'ems:ems_kpi_alarm_log:add'" @click="handleAdd" preIcon="ant-design:plus-outlined"> 新增</a-button>
        <a-button type="primary" v-auth="'ems:ems_kpi_alarm_log:exportXls'" preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
        <j-upload-button
          type="primary"
          v-auth="'ems:ems_kpi_alarm_log:importExcel'"
          preIcon="ant-design:import-outlined"
          v-if="false"
          @click="onImportXls"
          >导入</j-upload-button
        >
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined" />
                删除
              </a-menu-item>
            </a-menu>
          </template>
          <a-button v-auth="'ems:ems_kpi_alarm_log:deleteBatch'"
            >批量操作
            <Icon icon="mdi:chevron-down" />
          </a-button>
        </a-dropdown>
        <!-- 高级查询 -->
        <super-query :config="superQueryConfig" @search="handleSuperQuery" v-if="false" />
      </template>
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>

      <template #tagId="{ record }">
        <div>
          <span v-if="record.tagKind === TagKindEnum.COLLECT">{{'[' + record.tagId + '] ' + collectTagNameMap[record.tagId] }}</span>  
          <span v-if="record.tagKind === TagKindEnum.ORSDATA">{{'[' + record.tagId + '] ' +  tagNameMap[record.tagId] }}</span>
        </div>
      </template>
    </BasicTable>
    <!-- 表单区域 -->
    <KpiAlarmLogModal ref="registerModal" @success="handleSuccess" />
  </div>
  </a-spin>
</template>

<script lang="ts" name="ems-kpiAlarmLog" setup>
  import { ref, reactive, onMounted, provide } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { columns, superQuerySchema } from './KpiAlarmLog.data';
  import { list, deleteOne, batchDelete, getImportUrl, getExportUrl } from './KpiAlarmLog.api';
  import KpiAlarmLogModal from './components/KpiAlarmLogModal.vue';
  import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';
  import { JInput } from '/@/components/Form';
  import { cloneDeep } from 'lodash-es';
  import { listAllOrgTag } from '../tagdata/OrsdataTag.api';
  import { listAllCollectTag } from './KpiStruct.api';
  import { TagItem, tagKindList } from './KpiStructSet.data';
  import { TagKindEnum } from '@/enums/EmsEnum';

  const formRef = ref();
  const queryParam = reactive<any>({});
  const registerModal = ref();
  //注册table数据
  const { tableContext, onExportXls, onImportXls } = useListPage({
    tableProps: {
      title: 'KPI点位报警日志',
      api: list,
      columns,
      canResize: false,
      useSearchForm: false,
      actionColumn: {
        title: '操作',
        width: 120,
        fixed: 'right',
      },
      beforeFetch: async (params) => {
        let rangerQuery = await setRangeQuery();
        return Object.assign(params, rangerQuery);
      },
    },
    exportConfig: {
      name: 'KPI点位报警日志',
      url: getExportUrl,
      params: queryParam,
    },
    importConfig: {
      url: getImportUrl,
      success: handleSuccess,
    },
  });
  const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;
  const labelCol = reactive({
    xs: 12,
    sm: 8,
    md: 6,
    lg: 8,
    xl: 6,
    xxl: 6,
  });
  const wrapperCol = reactive({});

  const pageLoading = ref(false);

  // 点位相关
  const orstagList = ref<TagItem[]>([]);
  const collectTagList = ref<TagItem[]>([]);
  const tagNameMap = ref<any>({});
  const collectTagNameMap = ref<any>({});
  const tagOptions = ref<TagItem[]>([]);

  // 提供数据给表单
  provide('orstagList', orstagList); 
  provide('collectTagList', collectTagList);

  // 初始化
  onMounted(() => {
    getOrstagList();
    getCollectTagList();
  });

  //获取中间点位列表
  const getOrstagList = async () => {
    pageLoading.value = true;
    const paramObj = {};
    try {
      const response = await listAllOrgTag(paramObj);
      orstagList.value = response;
      orstagList.value.forEach((item)=>{
        tagNameMap.value[item.tagId] = item.tagName
      });
    } catch (e) {
      console.error('获取中间点列表失败:', e);
    } finally{
      pageLoading.value = false;
    }
  };

  //获取采集点位列表
  const getCollectTagList = async () => {
    pageLoading.value = true;
    collectTagList.value = [];
    try {
      const paramObj = {};
      const response = await listAllCollectTag(paramObj);
      response.forEach((item) => {
        const tagItem: TagItem = {
          tagId: item.tagId,
          tagName: item.tagName,
          unit: item.unit,
          tagType: item.tagType,
          valType: item.valType,
        };
        collectTagList.value.push(tagItem);
        collectTagNameMap.value[item.tagId] = item.tagName
      });
    } catch (e) {
      console.error('获取采集点位失败:', e);
    } finally {
      pageLoading.value = false;
    }
  };

  function changeTagKind(value){
    queryParam.tagId = null;
    tagOptions.value = [];
    if(TagKindEnum.ORSDATA === value){
      tagOptions.value = [...orstagList.value];
    } else if(TagKindEnum.COLLECT === value){
      tagOptions.value = [...collectTagList.value];
    }  
  }

  function filterOption(input, option) {
    let value = '',
      label = '';
    try {
      value = option.value;
      label = option.children()[0].children;
    } catch (e) {
      console.log('获取下拉项失败', e);
    }
    let str = input.toLowerCase();
    return value.toLowerCase().indexOf(str) >= 0 || label.toLowerCase().indexOf(str) >= 0;
  }

  // 高级查询配置
  const superQueryConfig = reactive(superQuerySchema);

  /**
   * 高级查询事件
   */
  function handleSuperQuery(params) {
    Object.keys(params).map((k) => {
      queryParam[k] = params[k];
    });
    searchQuery();
  }

  /**
   * 新增事件
   */
  function handleAdd() {
    registerModal.value.disableSubmit = false;
    registerModal.value.add();
  }

  /**
   * 编辑事件
   */
  function handleEdit(record: Recordable) {
    registerModal.value.disableSubmit = false;
    registerModal.value.edit(record);
  }

  /**
   * 详情
   */
  function handleDetail(record: Recordable) {
    registerModal.value.disableSubmit = true;
    registerModal.value.edit(record);
  }

  /**
   * 删除事件
   */
  async function handleDelete(record) {
    await deleteOne({ id: record.id }, handleSuccess);
  }

  /**
   * 批量删除事件
   */
  async function batchHandleDelete() {
    await batchDelete({ ids: selectedRowKeys.value }, handleSuccess);
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
        label: '编辑',
        onClick: handleEdit.bind(null, record),
        auth: 'ems:ems_kpi_alarm_log:edit',
      },
    ];
  }

  /**
   * 下拉操作栏
   */
  function getDropDownAction(record) {
    return [
      {
        label: '详情',
        onClick: handleDetail.bind(null, record),
      },
      {
        label: '删除',
        popConfirm: {
          title: '是否确认删除',
          confirm: handleDelete.bind(null, record),
          placement: 'topLeft',
          getPopupContainer: () => document.body,
        },
        auth: 'ems:ems_kpi_alarm_log:delete',
      },
    ];
  }

  /**
   * 查询
   */
  function searchQuery() {
    reload();
  }

  /**
   * 重置
   */
  function searchReset() {
    formRef.value.resetFields();
    selectedRowKeys.value = [];
    //刷新数据
    reload();
  }

  let rangeField = 'alarmTime,';

  /**
   * 设置范围查询条件
   */
  async function setRangeQuery() {
    let queryParamClone = cloneDeep(queryParam);
    if (rangeField) {
      let fieldsValue = rangeField.split(',');
      fieldsValue.forEach((item) => {
        if (queryParamClone[item]) {
          let range = queryParamClone[item];
          queryParamClone[item + '_begin'] = range[0];
          queryParamClone[item + '_end'] = range[1];
          delete queryParamClone[item];
        } else {
          queryParamClone[item + '_begin'] = '';
          queryParamClone[item + '_end'] = '';
        }
      });
    }
    return queryParamClone;
  }
</script>

<style lang="less" scoped>
  .jeecg-basic-table-form-container {
    padding: 0;
    .table-page-search-submitButtons {
      display: block;
      margin-bottom: 24px;
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
      margin-bottom: 16px;
      height: 32px;
    }
    :deep(.ant-picker),
    :deep(.ant-input-number) {
      width: 100%;
    }
  }
</style>
