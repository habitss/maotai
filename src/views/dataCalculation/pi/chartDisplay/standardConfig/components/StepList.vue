<template>
  <!--引用表格-->
  <!-- {{ groupData }} ==== -->
  <BasicTable @register="registerTable" :rowSelection="rowSelection">
    <!--插槽:table标题-->
    <template #tableTitle>
      <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleCreate"> {{ t('language.add') }}</a-button>
      <a-button v-if="selectedRowKeys.length > 0" type="primary" preIcon="ant-design:copy-outlined" @click="batchHandleCopy">复制</a-button>

      <a-dropdown v-if="selectedRowKeys.length > 0">
        <template #overlay>
          <a-menu>
            <a-menu-item key="1" @click="batchHandleDelete">
              <Icon icon="ant-design:delete-outlined"></Icon>
              删除
            </a-menu-item>
          </a-menu>
        </template>
        <a-button
          >{{ t('language.batchOperation') }}
          <Icon icon="ant-design:down-outlined"></Icon>
        </a-button>
      </a-dropdown>
    </template>
    <!--操作栏-->
    <template #action="{ record }">
      <TableAction :actions="getTableAction(record)" />
    </template>
  </BasicTable>
  <!--弹窗-->
  <PiCurveStandardModal @register="registerModal" @success="handleSuccess" />
  <!--配置抽屉-->
  <!-- <DictItemList @register="registerDrawer" /> -->
</template>

<script lang="ts" setup>
  //ts语法
  import { ref, computed, unref, reactive, h } from 'vue';
  import { Modal, InputNumber } from 'ant-design-vue';
  import { BasicTable, TableAction } from '@/components/Table';
  import { useDrawer } from '@/components/Drawer';
  import { useModal } from '@/components/Modal';
  import PiCurveStandardModal from './PiCurveStandardModal.vue';
  import { useMessage } from '@/hooks/web/useMessage';
  import { removeAuthCache, setAuthCache } from '@/utils/auth';
  import { columns, searchFormSchema } from '../PiCurveStandard.data';
  // import { configQueryPageList, deleteDict, batchDeleteDict, getExportUrl, getImportUrl, refreshCache, queryAllDictItems } from '/@/views/dataCalculation/mdm/dict/dict.api';
  import { configQueryPageList, getExportUrl, getImportUrl,configDeleteOne,configBatchDelete,configBatchCopyApi } from '../PiCurveStandard.api';
  import { DB_DICT_DATA_KEY } from '@/enums/cacheEnum';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();
  const queryParam = reactive<any>({});
  const { createMessage } = useMessage();
  const groupData = ref({
    recipeId: null,
    dataSource: null,
    areaNo: null,
    recipeNo: null,
    recipeName: null,
    groupId: null
  });


  //分类id
  const categorizeId = ref();
  //model
  const [registerModal, { openModal }] = useModal();
  //配置drawer
  // const [registerDrawer, { openDrawer }] = useDrawer();
  import { useListPage } from '/@/hooks/system/useListPage';

  // 列表页面公共参数、方法
  const { prefixCls, tableContext, onExportXls, onImportXls } = useListPage({
    designScope: 'dict-template',
    tableProps: {
      title: '数据',
      api: configQueryPageList,
      beforeFetch: (params) => {
        // params.dictGroupName = params.dictGroupName ? '*'+params.dictGroupName+'*' : '';
        return Object.assign(params, queryParam);
      },
      columns: columns,
      immediate: false,
      canResize:true,
      resizeHeightOffset: 90,    // 设置高度偏移量
      formConfig: {
        schemas: searchFormSchema,
      },
      pagination: {
        pageSize: 50,     // 默认每页显示20条
        pageSizeOptions: ['50','100'],
        showQuickJumper: false, // 隐藏快速跳转
      },
      actionColumn: {
        width: 90,
        fixed:'right'
      },
    },
    exportConfig: {
      name: '列表',
      url: getExportUrl,
    },
    importConfig: {
      url: getImportUrl,
    },
  });

  //注册table数据
  const [registerTable, { reload, updateTableDataRecord, getDataSource }, { rowSelection, selectedRowKeys }] = tableContext;

  /**
   * 新增事件
   */
  function handleCreate() {
    if(!groupData.value.groupId){
      createMessage.warning("请先选择设备分类");
      return false;
    }
    // 获取列表中最大序号+1
    const dataSource = getDataSource() || [];
    let maxSort = 0;
    dataSource.forEach(item => {
      if (item.sort && Number(item.sort) > maxSort) {
        maxSort = Number(item.sort);
      }
    });
    const nextSort = maxSort + 1;
    // 获取列表第一条数据的设备名称（列表不为空时）
    let unitNo = '';
    let unitName = '';
    if (dataSource.length > 0) {
      const firstRecord = dataSource[0];
      unitNo = firstRecord?.unitNo || '';
      unitName = firstRecord?.unitName || '';
    }

    openModal(true, {
      record: { ...groupData.value, sort: nextSort, unitNo, unitName },
      isUpdate: false,
      showFooter: true
    });
  }
  /**
   * 编辑事件
   */
  async function handleEdit(record: Recordable) {
    openModal(true, {
      record,
      isUpdate: true,
      showFooter: true
    });
  }
  /**
   * 详情
   */
  async function handleDetail(record) {
    openModal(true, {
      record,
      isUpdate: true,
    });
  }
  /**
   * 删除事件
   */
  async function handleDelete(record) {
    await configDeleteOne({ id: record.id }, reload);
  }
  /**
   * 批量删除事件
   */
  async function batchHandleDelete() {
    await configBatchDelete({ ids: selectedRowKeys.value.join(",") }, reload);
  }
  /**
   * 批量复制事件
   */
  function batchHandleCopy() {
    let copyNum = 1;
    Modal.confirm({
      title: '批量复制',
      content: h('div', { style: 'margin-top: 12px' }, [
        h('span', '复制份数：'),
        h(InputNumber, {
          min: 1,
          max: 99,
          defaultValue: 1,
          style: 'width: 120px',
          onChange: (val: number) => { copyNum = val; },
        }),
      ]),
      okText: '确认',
      cancelText: '取消',
      onOk: async () => {
        await configBatchCopyApi({ ids: selectedRowKeys.value.join(","), copyNum });
        reload();
      },
    });
  }
  /**
   * 成功回调 { isUpdate, values }
   */
  function handleSuccess() {
    // if (isUpdate) {
    //   updateTableDataRecord(values.id, values);
    // } else {
      reload();
    // }
  }
  /**
   * 配置
   */
  // function handleItem(record) {
  //   openDrawer(true, {
  //     id: record.id,
  //     record: record,
  //   });
  // }
  /**
   * 操作栏
   */
  function getTableAction(record) {
    return [
      {
        label: t('language.edit'),
        onClick: handleEdit.bind(null, record),
      },
      {
        label: t('language.delText'),
        popConfirm: {
          title: t('language.confirmDelete'),
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }

  /**
   * 重新加载列表
   * @param dataSource  数据源
   * @param areaNo  区域编号
   * @param recipeNo  配方编号
   * @param recipeId  配方ID
   * @param groupId  组ID
   * @param recipeName 配方名称
   */
  function reloadList(data) {
    if(!data.groupId){return;}
    Object.assign(queryParam, data)
    groupData.value = data;
    reload();
  }

  defineExpose({
    reloadList
  });
</script>

<style scoped></style>
