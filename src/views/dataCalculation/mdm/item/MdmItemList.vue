<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--插槽:table标题-->
      <template #tableTitle v-if="props.isTask != true">
        <a-button type="primary" v-auth="'mdm3:mes3_mdm_item:add'" @click="handleAdd"
          preIcon="ant-design:plus-outlined"> 新增</a-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                删除
              </a-menu-item>
            </a-menu>
          </template>
          <a-button v-auth="'mdm3:mes3_mdm_item:deletionBatch'">批量操作
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <template #tableTitle v-else>
        <a-button type="primary" @click="handleAddTaskItem"
          preIcon="ant-design:plus-outlined"> 保存</a-button>
      </template>
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
      <!--字段回显插槽-->
      <template v-slot:bodyCell="">
      </template>
    </BasicTable>
    <!-- 表单区域 -->
    <MdmItemModal @register="registerModal" @success="handleSuccess"></MdmItemModal>
  </div>
</template>

<script lang="ts" name="mdm3-mdmItem" setup>
import { computed, reactive, ref } from 'vue';
import { BasicTable, TableAction } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useListPage } from '/@/hooks/system/useListPage'
import MdmItemModal from './components/MdmItemModal.vue'
import { columns, searchFormSchema } from './MdmItem.data';
import { list, deleteOne, batchDelete, getImportUrl, getExportUrl } from './MdmItem.api';
import { cloneDeep } from 'lodash-es';
import { useMessage } from '/@/hooks/web/useMessage';
import { useRoute } from 'vue-router';
import { CheckItemTypeEnum } from '/@/enums/planEnum';
const { createConfirm } = useMessage();
const emit = defineEmits(['success']);
//获取路由
const route = useRoute();
//获取路由中的code参数
const checkItemType = ref(route.params.code);
const queryParam = reactive<any>({});
const props = defineProps({
  //是否关联任务
  isTask: { type: Boolean, default: false },
  //设备id
  deviceId: { type: String },
});

//根据传参获取表格配置信息
const tableConfigObj = computed(() => {
  let _columns = cloneDeep(columns);
  let _searchFormSchema = cloneDeep(searchFormSchema);
  //如果是任务关联页面跳转过来的，去掉工厂部门等信息
  if(props.isTask == true){
    _columns.splice(0, 3);
    _searchFormSchema.splice(0, 3);
  }
  getCol(checkItemType.value, _columns);
  return {
    columns: _columns,
    searchFormSchema: _searchFormSchema,
  };
});

//注册model
const [registerModal, { openModal }] = useModal();
//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    title: '检查项管理',
    api: list,
    columns: tableConfigObj.value.columns,
    showTableSetting: false,
    canResize: false,
    showActionColumn: !props.isTask,
    formConfig: {
      schemas: tableConfigObj.value.searchFormSchema,
      autoSubmitOnEnter: true,
      showAdvancedButton: true,
    },
    actionColumn: {
      width: 180,
      fixed: 'right'
    },
    beforeFetch: (params) => {
      if(props.isTask == true){
        params.deviceId = props.deviceId ?? '-1';
      }
      params.checkItemType = checkItemType.value;
      processingLikeParam(params);
      return Object.assign(params, queryParam);
    },
  },
  exportConfig: {
    name: "检查项管理",
    url: getExportUrl,
    params: queryParam,
  },
  importConfig: {
    url: getImportUrl,
    success: handleSuccess
  },
})

const [registerTable, { reload, setLoading, getSelectRows, setProps }, { rowSelection, selectedRowKeys }] = tableContext

/**
 * 新增事件
 */
function handleAdd() {
  openModal(true, {
    isUpdate: false,
    showFooter: true,
    checkItemType: Number(checkItemType.value),
  });
}

/**
 * 编辑事件
 */
function handleEdit(record: Recordable) {
  openModal(true, {
    record,
    isUpdate: true,
    showFooter: true,
  });
}

/**
 * 复制事件
 */
function handleCopy(record: Recordable) {
  let copyData = cloneDeep(record);
  delete copyData.id;
  openModal(true, {
    record: copyData,
    isUpdate: true,
    showFooter: true,
    isCopy: true,
  });
}
/**
 * 详情
*/
function handleDetail(record: Recordable) {
  openModal(true, {
    record,
    isUpdate: true,
    showFooter: false,
  });
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
  (selectedRowKeys.value = []) && reload();
}
/**
   * 操作栏
   */
function getTableAction(record) {
  return [
    {
      label: '编辑',
      onClick: handleEdit.bind(null, record),
      auth: 'mdm3:mes3_mdm_item:edit'
    },
    {
      label: '复制',
      onClick: handleCopy.bind(null, record),
      auth: 'mdm3:mes3_mdm_item:edit'
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
        title: '是否确认删除',
        confirm: handleDelete.bind(null, record),
        placement: 'topLeft',
      },
      auth: 'mdm3:mes3_mdm_item:delete'
    }
  ]
}

//保存任务条目关联
async function handleAddTaskItem(){
  if(!getSelectRows() || getSelectRows().length == 0){
    createConfirm({
      title: '保存前确认',
      content: `当前未选择任何检查项，继续保存会清除当前任务已经关联的检查项，是否继续？`,
      iconType: 'warning',
      onOk: async () => {await emit('success', getSelectRows())},
    });
  } else {
    await emit('success', getSelectRows());
  }
}

/**
 * 处理模糊查询参数
 */
function processingLikeParam(params){
  const fields = ['itemName', 'itemLocation', 'remark', 'itemStandard', 'detail'];
  for(const key in params){
    if(fields.includes(key) && params[key]){
      params[key] = `*${params[key]}*`;
    }
  }
}

function getCol(checkItemTypeParam, columnArr){
  if(CheckItemTypeEnum.TYPE_RECORD == checkItemTypeParam){
    let newItem = [{
      title: '交接班类型',
      dataIndex: 'isHandover',
      width: 120,
      customRender: ({text}) => {
        return text == 1 ? '交班' : '接班';
      }
    },{
      title: '是否生产检查项',
      dataIndex: 'isProduction',
      width: 120,
      customRender: ({text}) => {
        return text == 1 ? '是' : '否';
      }
    }];
    columnArr.splice(5, 0, ...newItem);
  }
}

async function reloadData(oldList, checkItemTypeParam){
  queryParam.pageSize = 200;
  queryParam.excludeIds = oldList;
  checkItemType.value = checkItemTypeParam;
  await setProps({
    columns: tableConfigObj.value.columns
  });
  reload();
}

//暴露方法
defineExpose({
  setLoading,
  setProps,
  reloadData
})
</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}
</style>