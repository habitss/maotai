<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection" :expandedRowKeys="expandedRowKeys"
      @expand="handleExpand" @fetch-success="onFetchSuccess">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button type="primary" v-auth="'pi:mes3_pi_abnormal_analysis_point:add'" @click="handleCreate"
          preIcon="ant-design:plus-outlined"> 新增</a-button>
        <a-button type="primary" v-auth="'pi:mes3_pi_abnormal_analysis_point:exportXls'"
          preIcon="ant-design:export-outlined" @click="handleExport"> 导出</a-button>
        <j-upload-button type="primary" v-auth="'pi:mes3_pi_abnormal_analysis_point:importExcel'"
          preIcon="ant-design:import-outlined" @click="onImportXls">导入</j-upload-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                删除
              </a-menu-item>
            </a-menu>
          </template>
          <a-button v-auth="'pi:mes3_pi_abnormal_analysis_point:deletionBatch'">批量操作
            <Icon icon="ant-design:down-outlined"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
      
      <!--  deptCode部门插槽    -->
      <template #form-deptCode="{ model, field }">
        <kt-depart-select
          v-model:value="model[field]"
          select-type="depart"
          :placeholder="t('language.selectDepartment')"
          @change="handleDeptChange"
        />
      </template>
      
      <!--  lineCode产线插槽    -->
      <template #form-lineCode="{ model, field }">
        <kt-depart-select
          v-model:value="model[field]"
          select-type="line"
          :placeholder="t('language.selectProductLine')"
          :parentKey="model.deptCode"
          @change="handleLineChange"
        />
      </template>
      
      <!--实际值插槽-->
      <template #actualValueSlots="{ record, column }">
        <a-input v-model:value="record[column.dataIndex]" placeholder="请输入实际值" />
      </template>

      <!--原因分析插槽-->
      <template #reasonAnalysisSlots="{ record, column }">
        <a-input v-model:value="record[column.dataIndex]" placeholder="请输入原因分析"  />
      </template>
    </BasicTable>
    <!--字典弹窗-->
    <PiAbnormalAnalysisPointModal @register="registerModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" name="pi-piAbnormalAnalysisPoint" setup>
//ts语法
import { ref, reactive, unref } from 'vue';
import { BasicTable, TableAction } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useListPage } from '/@/hooks/system/useListPage'
import { useMethods } from '/@/hooks/system/useMethods';
import { filterObj } from '/@/utils/common/compUtils';
import PiAbnormalAnalysisPointModal from './components/PiAbnormalAnalysisPointModal.vue';
import { getColumnsConfig, searchFormSchema, searchFormSchema2, validSelectValue } from './PiAbnormalAnalysisPoint.data';
import { list, deletePiAbnormalAnalysisPoint, batchDeletePiAbnormalAnalysisPoint, getChildListBatch, getExportUrl, getImportUrl } from './PiAbnormalAnalysisPoint.api';
import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
import { useI18n } from '/@/hooks/web/useI18n';
import { cloneDeep } from 'lodash-es';
const props = defineProps({
  isEmbed: { type: Boolean, default: false },
});

//国际化
const { t } = useI18n();
const queryParam = reactive<any>({});
const expandedRowKeys = ref([]);
//字典model
const [registerModal, { openModal }] = useModal();
//注册table数据
const { tableContext, onImportXls } = useListPage({
  tableProps: {
    api: list,
    title: '异常点位配置',
    columns: getColumnsConfig(props.isEmbed),
    showTableSetting: false,
    isTreeTable: true,
    pagination: !props.isEmbed,
    formConfig: {
      schemas: props.isEmbed ? searchFormSchema2 : searchFormSchema,
      autoSubmitOnEnter: true,
      showAdvancedButton: false,
      showActionButtonGroup: true,
    },
    actionColumn: {
      width: 180,
      fixed: 'right'
    },
    beforeFetch: (params) => {
      params.tableCode = validSelectValue(params.tableCode);
      params.fieldCode = validSelectValue(params.fieldCode);
      params.column = 'tableCode';
      params.order = 'asc';
      params.pointName = params.pointName ? `*${params.pointName}*` : null;
      if(props.isEmbed){
        params.pageSize = 999;
      }
      return Object.assign(params, queryParam);
    },
  },
  exportConfig: {
    name: "异常点位配置",
    url: getExportUrl,
    params: queryParam,
  },
  importConfig: {
    url: getImportUrl,
    success: handleSuccess
  },
})

const [registerTable, { reload, updateTableDataRecord, findTableDataRecord, getDataSource, getForm, getSelectRows }, { rowSelection, selectedRowKeys }] = tableContext

// 导出
const { handleExportXls } = useMethods();
async function handleExport() {
  let paramsForm: any = {};
  try {
    paramsForm = await getForm().validate();
  } catch (e) {
    console.error(e);
  }
  // 处理 labelInValue 对象，转换为纯 key 值，避免序列化 Proxy 对象导致栈溢出
  paramsForm.tableCode = validSelectValue(paramsForm.tableCode);
  paramsForm.fieldCode = validSelectValue(paramsForm.fieldCode);
  paramsForm.column = 'tableCode';
  paramsForm.order = 'asc';
  paramsForm.pointName = paramsForm.pointName ? `*${paramsForm.pointName}*` : null;
  if (!paramsForm?.column) {
    Object.assign(paramsForm, { column: 'createTime', order: 'desc' });
  }
  Object.assign(paramsForm, queryParam);
  if (selectedRowKeys.value && selectedRowKeys.value.length > 0) {
    paramsForm['selections'] = selectedRowKeys.value.join(',');
  }
  return handleExportXls('异常点位配置', getExportUrl, filterObj(paramsForm));
}

/**
 * 新增事件
 */
function handleCreate() {
  openModal(true, {
    isUpdate: false,
  });
}

/**
 * 编辑事件
 */
async function handleEdit(record) {
  openModal(true, {
    record,
    isUpdate: true,
  });
}

/**
 * 详情
 */
async function handleDetail(record) {
  openModal(true, {
    record,
    isUpdate: true,
    hideFooter: true,
  });
}

/**
 * 删除事件
 */
async function handleDelete(record) {
  await deletePiAbnormalAnalysisPoint({ id: record.id }, importSuccess);
}

/**
 * 批量删除事件
 */
async function batchHandleDelete() {
  const ids = selectedRowKeys.value.filter(item => !item.includes('loadChild'))
  await batchDeletePiAbnormalAnalysisPoint({ id: ids }, importSuccess);
}
/**
 * 导入
 */
function importSuccess() {
  (selectedRowKeys.value = []) && reload();
}
/**
 * 添加下级
 */
function handleAddSub(record) {
  const obj = cloneDeep(record);
  obj.parentId = obj.id;
  delete obj.pointName;
  delete obj.standardValue;
  delete obj.serialNumber;
  delete obj.id;
  openModal(true, {
    record: obj,
    isUpdate: false,
    isAddSub: true,
  });
}
/**
 * 成功回调
 */
async function handleSuccess({ isUpdate, values, expandedArr, changeParent }) {
  if (isUpdate) {
      reload();
  } else {
    if (!values['id'] || !values['parentId']) {
      //新增根节点
      reload();
    } else {
      //新增子集
      expandedRowKeys.value = [];
      for (let key of unref(expandedArr)) {
        await expandTreeNode(key)
      }
    }
  }
}

/**
 * 接口请求成功后回调
 */
function onFetchSuccess(result) {
  getDataByResult(result.items) && loadDataByExpandedRows();
}
/**
 * 根据已展开的行查询数据（用于保存后刷新时异步加载子级的数据）
 */
async function loadDataByExpandedRows() {
  if (unref(expandedRowKeys).length > 0) {
    const res = await getChildListBatch({ parentIds: unref(expandedRowKeys).join(',') });
    if (res.success && res.result.records.length > 0) {
      //已展开的数据批量子节点
      let records = res.result.records
      const listMap = new Map();
      for (let item of records) {
        let pid = item['parentId'];
        if (unref(expandedRowKeys).includes(pid)) {
          let mapList = listMap.get(pid);
          if (mapList == null) {
            mapList = [];
          }
          mapList.push(item);
          listMap.set(pid, mapList);
        }
      }
      let childrenMap = listMap;
      let fn = (list) => {
        if (list) {
          list.forEach(data => {
            if (unref(expandedRowKeys).includes(data.id)) {
              data.children = getDataByResult(childrenMap.get(data.id))
              fn(data.children)
            }
          })
        }
      };
      fn(getDataSource())
    }
  }
}
/**
 * 处理数据集
 */
function getDataByResult(result) {
  if (result && result.length > 0) {
    return result.map(item => {
      //判断是否标记了带有子节点
      if (item["hasChild"] == '1') {
        let loadChild = { id: item.id + '_loadChild', name: 'loading...', isLoading: true }
        item.children = [loadChild]
      }
      return item
    })
  }
}
/**
 *树节点展开合并
 * */
async function handleExpand(expanded, record) {
  // 判断是否是展开状态，展开状态(expanded)并且存在子集(children)并且未加载过(isLoading)的就去查询子节点数据
  if (expanded) {
    expandedRowKeys.value.push(record.id)
    if (record.children.length > 0 && !!record.children[0].isLoading) {
      let result = await list({ parentId: record.id, column: 'serialNumber', order: 'asc' });
      result = result.records ? result.records : result;
      if (result && result.length > 0) {
        record.children = getDataByResult(result);
      } else {
        record.children = null
        record.hasChild = '0'
      }
    }
  } else {
    let keyIndex = expandedRowKeys.value.indexOf(record.id)
    if (keyIndex >= 0) {
      expandedRowKeys.value.splice(keyIndex, 1);
    }
  }
}
/**
 *操作表格后处理树节点展开合并
 * */
async function expandTreeNode(key) {
  let record = findTableDataRecord(key)
  expandedRowKeys.value.push(key);
  let result = await list({ parentId: key });
  if (result && result.length > 0) {
    record.children = getDataByResult(result);
  } else {
    record.children = null
    record.hasChild = '0'
  }
  updateTableDataRecord(key, record);
}
/**
 * 操作栏
 */
function getTableAction(record) {
  return [
    {
      label: '编辑',
      onClick: handleEdit.bind(null, record),
      auth: 'pi:mes3_pi_abnormal_analysis_point:edit'
    },
    {
      label: '添加下级',
      onClick: handleAddSub.bind(null, record),
    }
  ]
}
/**
 * 下拉操作栏
 */
function getDropDownAction(record) {
  return [
    {
      label: '详情',
      onClick: handleDetail.bind(null, record),
    }, {
      label: '删除',
      popConfirm: {
        title: '确定删除吗?',
        confirm: handleDelete.bind(null, record),
        placement: 'topLeft'
      },
      auth: 'pi:mes3_pi_abnormal_analysis_point:delete'
    }
  ]
}

//部门改变事件
async function handleDeptChange(value){
  //清空产线、报表、pi
  await getForm().setFieldsValue({ 
    lineCode: undefined,
    tableCode: undefined,
    fieldCode: undefined,
  });
}

//产线改变事件
async function handleLineChange(value) {
  //清空报表
  await getForm().setFieldsValue({ 
    tableCode: undefined,
    fieldCode: undefined,
  });
}

//设置查询参数
function setQueryParams(params){
  Object.assign(queryParam, params);
}

//暴露方法
defineExpose({
  getSelectRows,
  setQueryParams,
})
</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}
</style>
