<template>
  <div class="p-2">
    <!--查询区域-->
    <div class="jeecg-basic-table-form-container">
      <a-form ref="formRef" @keyup.enter="searchQuery" :model="queryParam" :label-col="labelCol" :wrapper-col="wrapperCol">
        <a-row :gutter="24">
          <a-col :xxl="6" :xl="8" :lg="8" :md="12" :sm="12" :xs="24">
            <a-form-item name="entryDesc">
              <template #label><span title="词条">搜索词条</span></template>
              <a-input placeholder="请输入词条Key或词条描述查询" v-model:value="queryParam.entryDesc" allow-clear />
            </a-form-item>
          </a-col>
          <a-col :xxl="12" :xl="12" :lg="12" :md="12" :sm="12" :xs="24">
            <span style="float: left; overflow: hidden" class="table-page-search-submitButtons">
              <a-button type="primary" preIcon="ant-design:search-outlined" @click="searchQuery">查询</a-button>
              <a-button type="primary" preIcon="ant-design:reload-outlined" @click="searchReset" style="margin-left: 8px">重置</a-button>
              <a-button
                type="primary"
                v-auth="'i18n:mes3_mdm_i18n_entry_lang:exportXls'"
                preIcon="ant-design:export-outlined"
                style="margin-left: 8px"
                @click="onExportXls"
              >
                导出</a-button
              >
              <span style="margin-left: 8px">
                <j-upload-button
                  type="primary"
                  v-auth="'i18n:mes3_mdm_i18n_entry_lang:importExcel'"
                  preIcon="ant-design:import-outlined"
                  @click="onImportXls"
                  >{{ t('common.import') }}</j-upload-button
                >
              </span>
            </span>
            <a-button
              type="primary"
              style="margin-left: 8px"
              v-auth="'i18n:mes3_mdm_i18n_entry:add'"
              @click="handleAddEntry"
              preIcon="ant-design:plus-outlined"
            >
              添加新词条</a-button
            >
          </a-col>
        </a-row>
      </a-form>
    </div>

    <!-- 表格 -->
    <j-vxe-table
      ref="vxeTable"
      rowKey="entryId"
      @value-change="handleValueChange"
      @keydown="handleKeyDown"
      @page-change="handlePageChange"
      :columns="columns"
      :dataSource="dataSource"
      :pagination="pagination"
      :row-style="getRowStyle"
      keyboard-edit
      keep-source
      bordered
      style="width: 100%"
    >
      <template #actionSlot="{ row }">
        <a-button v-if="row._dirty" type="primary" size="small" @click="handleSaveRow(row)" tabindex="0"> 保存 </a-button>
      </template>
    </j-vxe-table>

    <!-- 表单区域 -->
    <MdmI18nEntryModal ref="editEntryModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" name="i18n-mdmI18nEntryLang" setup>
  import { ref, reactive, onMounted } from 'vue';
  import { listPageWithLang, saveEntryLangRow } from './MdmI18nEntryLang.api';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { getImportUrl, getExportUrl } from './MdmI18nEntryLang.api';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import MdmI18nEntryModal from './components/MdmI18nEntryModal.vue';

  const { t } = useI18n();
  const { createMessage } = useMessage();
  const formRef = ref();
  const queryParam = reactive<any>({ entryDesc: '' });
  const dataSource = ref([]);
  const columns = ref<ColumnConfig[]>([]);
  const pagination = ref({
    current: 1,
    pageSize: 20,
    total: 0,
    pageSizeOptions: ['10', '20', '50', '100', '200', '500'],
    size: 'small',
  });
  const currentRow = ref<any>({});
  const vxeTable = ref();
  const langCodes = ref<string[]>([]);
  const editEntryModal = ref();

  // 注册导出和导入方法
  const { onExportXls, onImportXls } = useListPage({
    tableProps: { immediate: false },
    exportConfig: {
      name: '国际化词条翻译',
      url: getExportUrl,
      params: queryParam,
    },
    importConfig: {
      url: getImportUrl,
      success: handleSuccess,
    },
  });

  // 扩展接口，使类型更灵活
  interface ColumnConfig {
    title: string;
    key: string;
    type?: string;
    disabled?: boolean;
    width?: number;
    fixed?: string;
    slots?: {
      default: string;
    };
  }
  const labelCol = reactive({
    xs: 12,
    sm: 8,
    md: 6,
    lg: 8,
    xl: 6,
    xxl: 6,
  });
  const wrapperCol = reactive({});

  /**
   * 键盘事件
   */
  function handleKeyDown(event) {
    if (event.$event.key !== 'Enter') {
      return;
    }
    if (currentRow.value.entryId) {
      handleSaveRow(currentRow.value);
    }
  }

  /**
   * 当前正在编辑的行样式
   */
  function getRowStyle({ row }) {
    return currentRow.value.entryId === row.entryId ? { backgroundColor: '#ffe6e6' } : {};
  }

  /**
   * 分页事件
   * @param page 分页信息
   */
  function handlePageChange({ current, pageSize }) {
    console.log('分页事件:', current, pageSize);
    pagination.value.current = current;
    pagination.value.pageSize = pageSize;
    loadData(current);
  }

  /**
   * 构建表格列
   * @param langCols 语言列
   * @returns
   */
  function buildColumns(langCols) {
    const fixed = [
      { title: '模块', key: 'module', width: 120, align: 'center', fixed: 'left' },
      { title: '词条Key', key: 'entryKey', width: 200, fixed: 'left' },
      { title: '词条描述', key: 'description', align: 'center', fixed: 'left' },
    ];

    const langs = langCols.map((l) => ({
      title: `${l.langName} (${l.langCode})`,
      key: `langMap.${l.langCode}`,
      type: 'input',
      align: 'center',
    }));

    const actionCol = {
      title: '操作',
      key: 'action',
      width: 120,
      fixed: 'right',
      align: 'center',
      slots: { default: 'actionSlot' },
      editRender: null,
    };

    return [...fixed, ...langs, actionCol];
  }

  /**
   * 加载数据
   */
  async function loadData(page = 1) {
    try {
      pagination.value.current = page;
      const res = await listPageWithLang({
        pageNo: pagination.value.current,
        pageSize: pagination.value.pageSize,
        entryDesc: queryParam.entryDesc,
      });

      if (res.success && res.result) {
        columns.value = buildColumns(res.result.columns || []);

        // 从 columns 中解析语言 code
        langCodes.value = res.result.columns.map((col) => col.langCode);

        // 处理数据，将嵌套结构展开为扁平结构
        dataSource.value = processData(res.result.page?.records || []);
        pagination.value.total = res.result.page?.total || 0;

        console.log('处理后的数据:', dataSource.value);
      }
    } catch (error) {
      console.error('加载数据失败:', error);
      createMessage.error('数据加载失败');
    }
  }

  /**
   * 处理数据，将嵌套的 langMap 展开为扁平结构
   */
  function processData(records) {
    return records.map((record) => {
      const processedRecord = {
        ...record,
        _dirty: false,
        _saving: false,
      };

      // 将嵌套的 langMap 对象展开为扁平属性
      if (record.langMap && typeof record.langMap === 'object') {
        Object.keys(record.langMap).forEach((langCode) => {
          const flatKey = `langMap.${langCode}`;
          processedRecord[flatKey] = record.langMap[langCode] || '';
        });
      }

      return processedRecord;
    });
  }

  /**
   * 保存行
   * @param row 行数据
   */
  async function handleSaveRow(row) {
    if (!row._dirty) {
      createMessage.info('该行未修改，无需保存');
      return;
    }
    try {
      // 动态构建 langMap
      const langMap = {};
      langCodes.value.forEach((lang) => {
        const flatKey = `langMap.${lang}`;
        langMap[lang] = row[flatKey] ?? row.langMap?.[lang] ?? '';
      });
      // 重新组织数据，确保 langMap 对象正确
      const saveData = {
        entryId: row.entryId,
        entryKey: row.entryKey,
        module: row.module,
        description: row.description,
        langMap,
      };

      console.log('保存数据:', saveData);
      await saveEntryLangRow(saveData);
      row._dirty = false;
      currentRow.value = {};
      createMessage.success('保存成功');
    } catch (error) {
      console.error('保存失败:', error);
      createMessage.error('保存失败');
    }
  }

  /**
   * 查询
   */
  function searchQuery() {
    loadData();
  }

  /**
   * 重置
   */
  function searchReset() {
    formRef.value.resetFields();
    loadData();
  }

  /**
   * 值变化
   * @param event
   */
  function handleValueChange(event) {
    const { row } = event;
    if (event.value !== event.oldValue) {
      row._dirty = true;
      currentRow.value = row;
      console.log('handleValueChange', currentRow);
    }
  }

  /**
   * 成功回调
   */
  function handleSuccess() {
    loadData();
  }

  /**
   * 新增新词条
   */
  function handleAddEntry() {
    editEntryModal.value.disableSubmit = false;
    editEntryModal.value.add();
  }

  onMounted(() => {
    loadData();
  });
</script>

<style lang="less" scoped>
  ::v-deep(.vxe-table .active-row) {
    background-color: #ffe6e6 !important;
    color: #ff0000 !important;
  }
</style>
