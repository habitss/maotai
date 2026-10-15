<template>
  <a-spin :spinning="pageLoading">
  <div class="p-2">
    <!--查询区域-->
    <div class="jeecg-basic-table-form-container">
      <a-form ref="formRef" @keyup.enter="searchQuery" :model="queryParam" :label-col="labelCol" :wrapper-col="wrapperCol">
        <a-row :gutter="24">
          <a-col :lg="6">
            <a-form-item name="deptCode">
              <template #label><span title="部门">部门</span></template>
              <j-dict-select-tag
                placeholder="请选择部门"
                v-model:value="queryParam.deptCode"
                dictCode="sys_depart where org_category=4,depart_name,org_code"
                allow-clear
              />
            </a-form-item>
          </a-col>
          <a-col :lg="6">
            <a-form-item name="kpiTagName">
              <template #label><span title="点位名称">点位名称</span></template>
              <j-input placeholder="点位名称" v-model:value="queryParam.kpiTagName" allow-clear />
            </a-form-item>
          </a-col>
          <template v-if="toggleSearchStatus">
            <a-col :lg="6">
              <a-form-item name="tagKind">
                <template #label><span title="点位大类">点位大类</span></template>
                <j-dict-select-tag
                  placeholder="请选择点位大类"
                  v-model:value="queryParam.tagKind"
                  dictCode="mes3_mdm_dict where group_code = 'EMS_TagKind',item_key,item_val"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :lg="6">
              <a-form-item name="tagId">
                <template #label><span title="关联点位">关联点位</span></template>
                <j-dict-select-tag
                  placeholder="请选择关联点位"
                  v-model:value="queryParam.tagId"
                  dictCode="ems_orsdata_tag,tag_name,tag_id"
                  allow-clear
                />
              </a-form-item>
            </a-col>
          </template>
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
        <a-button type="primary" v-auth="'ems:ems_kpi_tag:add'" @click="handleAdd" preIcon="ant-design:plus-outlined"> 新增</a-button>
        <a-button type="primary" v-auth="'ems:ems_kpi_tag:exportXls'" preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
        <j-upload-button type="primary" v-auth="'ems:ems_kpi_tag:importExcel'" preIcon="ant-design:import-outlined" @click="onImportXls"
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
          <a-button v-auth="'ems:ems_kpi_tag:deleteBatch'"
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
          <a v-if="record.tagKind === TagKindEnum.COLLECT" @click="bindKpiTag(record)" :class="`link-usage${record.primaryTag} em-lbl`">{{'[' + record.tagId + '] ' + collectTagNameMap[record.tagId] }}</a>  
          <a v-if="record.tagKind === TagKindEnum.ORSDATA" @click="bindKpiTag(record)" :class="`link-usage${record.primaryTag} em-lbl`">{{'[' + record.tagId + '] ' +  tagNameMap[record.tagId] }}</a>
        </div>
      </template>
    </BasicTable>
    <!-- 表单区域 -->
    <KpiTagModal ref="registerModal" @success="handleSuccess" />

    <!-- KPI设置弹窗 -->
    <KpiTagSetupModal
      ref="kpiTagSetupModal"
      v-model:open="digKpiTagShow"
      :energy-type="energyType"
      :model-kpi-tag="modelKpiTag"
      :tag-kind-list="tagKindList"
      :collect-tag-list="collectTagList"
      :orstag-list="orstagList"
      @refresh="reload"
      @close="closeTagDialog"
    />
  </div>
  </a-spin>
</template>

<script lang="ts" name="ems-kpiTag" setup>
  import { ref, reactive, onMounted, provide } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { columns, superQuerySchema } from './KpiTag.data';
  import { list, deleteOne, batchDelete, getImportUrl, getExportUrl } from './KpiTag.api';
  import KpiTagModal from './components/KpiTagModal.vue';
  import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';
  import { JInput } from '/@/components/Form';
  import { KpiTag, TagItem, tagKindList } from './KpiStructSet.data';
  import { TagKindEnum } from '@/enums/EmsEnum';
  import KpiTagSetupModal from './components/KpiTagSetupModal.vue';
  import { listAllOrgTag } from '../tagdata/OrsdataTag.api';
  import { listAllCollectTag } from './KpiStruct.api';
  import { useMessage } from '/@/hooks/web/useMessage';
  const { createMessage } = useMessage();

  const formRef = ref();
  const queryParam = reactive<any>({});
  const toggleSearchStatus = ref<boolean>(false);
  const registerModal = ref();
  //注册table数据
  const { tableContext, onExportXls, onImportXls } = useListPage({
    tableProps: {
      title: 'KPI点位',
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
        return Object.assign(params, queryParam);
      },
    },
    exportConfig: {
      name: 'KPI点位',
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

  const modelKpiTag = reactive<KpiTag>({
    id: undefined,
    kpiTagName: '',
    structId: 0,
    tagId: '',
    unit: '',
    primaryTag: 1,
    tagKind: TagKindEnum.ORSDATA,
  });

  const pageLoading = ref(false);
  const digKpiTagShow = ref(false);
  const energyType = ref('');

  // 点位相关
  const orstagList = ref<TagItem[]>([]);
  const collectTagList = ref<TagItem[]>([]);
  const tagNameMap = ref<any>({});
  const collectTagNameMap = ref<any>({});

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
      createMessage.error('获取中间点列表失败', 5);
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
      createMessage.error('获取采集点位失败');
    } finally {
      pageLoading.value = false;
    }
  };

  const closeTagDialog = () => {
    //清空点位
    Object.assign(modelKpiTag, {
      id: undefined,
      kpiTagName: '',
      structId: 0,
      tagId: '',
      unit: '',
      energyType: energyType.value,
      primaryTag: 1,
      tagKind: TagKindEnum.ORSDATA,
    });
  };

  const bindKpiTag = (item: KpiTag) => {
    Object.assign(modelKpiTag, item);
    energyType.value = item.energyType || '';
    if (modelKpiTag.tagKind === TagKindEnum.ORSDATA) {
      const orsTag = orstagList.value.find((item) => item.tagId === modelKpiTag.tagId);
      modelKpiTag.unit = orsTag?.unit;
      modelKpiTag.valType = orsTag?.valType;
    } else if(modelKpiTag.tagKind === TagKindEnum.COLLECT){
      const collectTag = collectTagList.value.find((item) => item.tagId === modelKpiTag.tagId);
      modelKpiTag.unit = collectTag?.unit;
      modelKpiTag.valType = collectTag?.valType;
    }
    digKpiTagShow.value = true;
  };

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
        auth: 'ems:ems_kpi_tag:edit',
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
        auth: 'ems:ems_kpi_tag:delete',
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

  .link-usage0 {
    color: #eca80d;
    cursor: pointer;

    &:hover {
      text-decoration: underline;
    }
  }

  .link-usage1 {
    color: @primary-color;
    cursor: pointer;

    &:hover {
      text-decoration: underline;
    }
  }
</style>
