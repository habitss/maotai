<template>
  <div>
    <!--主表信息显示-->
    <div class="jeecg-basic-table-form-container">
      <!--引用表格-->
      <BasicTable @register="registerTable" :rowSelection="rowSelection">
        <!--插槽:table标题-->
        <template #tableTitle>
          <a-button type="primary" @click="handleAddDfAlgorithm" preIcon="ant-design:plus-outlined"> 新增DF算法</a-button>
          <a-button type="primary" @click="batchHandleDelete" preIcon="ant-design:delete-outlined"
            v-if="selectedRows.length > 0"> 删除</a-button>
        </template>
        <!--操作栏-->
        <template #action="{ record }">
          <TableAction :actions="getTableAction(record)" />
        </template>
        <!--字段回显插槽-->
        <template v-slot:bodyCell="">
        </template>
      </BasicTable>
    </div>
    <!-- 算法配置 -->
    <PiDfAlgorithmDrawer @register="register" @success="algorithmSuccess"></PiDfAlgorithmDrawer>
  </div>
</template>

<script lang="ts" name="pi-piCustomizeTableField" setup>
import { ref, onMounted } from 'vue';
import { BasicTable, TableAction } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage'
import { columns } from './PiDfAlgorithm.data';
import { list, batchDelete } from './PiDfAlgorithm.api';
import { useDrawer } from '/@/components/Drawer';
import PiDfAlgorithmDrawer from './components/PiDfAlgorithmDrawer.vue'

//注册DF算法配置抽屉
const [register, { openDrawer }] = useDrawer();
const queryParam = ref<any>({});
const baseInfo = ref<any>({});
const props = defineProps({
  parentData: { type: Object, default: () => ({}) },
});

//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    title: 'pi算法配置',
    api: list,
    columns,
    canResize: false,
    useSearchForm: false,
    pagination: true,
    showIndexColumn: true,
    actionColumn: {
      width: 200,
      fixed: 'right',
    },
    beforeFetch: (params) => {
      return Object.assign(params, queryParam.value);
    },
  },
})

const [registerTable, { reload, setSelectedRowKeys }, { rowSelection, selectedRows, selectedRowKeys }] =
  tableContext;

onMounted(() => {
  const { parentData } = props;
  //设置查询参数
  queryParam.value.piFieldId = parentData.id;
  queryParam.value.piTableId = parentData.tableId;
  queryParam.value.id = parentData.calculateSignalId;
  //设置基础信息
  baseInfo.value.piFieldId = parentData.id;
  baseInfo.value.piFieldName = parentData.fieldName;
  baseInfo.value.piTableId = parentData.tableId;
  baseInfo.value.piTableName = parentData.tableName;
});

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
 * 算法配置成功
 */
function algorithmSuccess(id) {
  handleSuccess()
  if (id) {
    setTimeout(() => {
      setSelectedRowKeys([id])
    }, 1000);
  }
}

/**
* 操作栏
*/
function getTableAction(record) {
  return [
    {
      label: "编辑算法",
      onClick: handleEditDfAlgorithm.bind(null, record),
      auth: '',
    },
  ];
}

/**
 * 新增算法
 */
function handleAddDfAlgorithm(){
  openDrawer(true, {
    isUpdate: false,
    baseInfo: baseInfo.value,
  });
}

/**
 * 编辑算法
 */
function handleEditDfAlgorithm(record){
  openDrawer(true, {
    isUpdate: true,
    baseInfo: record,
  });
}
</script>

<style lang="less" scoped>
.jeecg-basic-table-form-container {
  .table-page-search-submitButtons {
    display: block;
    margin-bottom: 24px;
    white-space: nowrap;
  }

  .query-group-cust {
    width: calc(50% - 15px);
    min-width: 100px !important;
  }

  .query-group-split-cust {
    width: 30px;
    display: inline-block;
    text-align: center;
  }
}
</style>