<template>
  <a-spin :spinning="confirmLoading">
    <div class="p-2">
      <!--引用表格-->
      <BasicTable @register="registerTable">
        <!--插槽:table标题-->
        <template #tableTitle>
          <a-button type="primary" v-if="saveBtnShowProport" @click="handleAdd"
            preIcon="ant-design:plus-outlined">保存</a-button>
        </template>
        <!--人员选择-->
        <template v-slot:userSlots="{ column, record }">
          <span>
            <KtUserScrollSelect v-model:value="record[column.dataIndex]" placeholder="请选择"></KtUserScrollSelect>
          </span>
        </template>
        <!--录入-->
        <template v-slot:inputSlots="{ column, record }">
          <span>
            <a-input v-model:value="record[column.dataIndex]" placeholder="请输入" allow-clear
              style="width:100%"></a-input>
          </span>
        </template>
      </BasicTable>
    </div>
  </a-spin>

</template>

<script lang="ts" setup>
import { ref, reactive, nextTick } from 'vue';
import { BasicTable } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage';
import { responsibilityColumns } from '../OplHomePage.data';
import { updateByWriteTwoRr } from '../OplHomePage.api';
import KtUserScrollSelect from '/@/components/KtUserScrollSelect/KtUserScrollSelect.vue';
import { useMessage } from '/@/hooks/web/useMessage';
const { createMessage } = useMessage();
const saveBtnShowProport = ref(false)
const queryParam = reactive<any>({});
const dataSource = ref([]);
const oplData = ref({})
const confirmLoading = ref(false);
const emit = defineEmits(['register', 'ok']);
//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    title: 'OPL-诊断计划',
    dataSource: dataSource,
    columns: responsibilityColumns,
    canResize: false,
    useSearchForm: false,
    showActionColumn: false,
    actionColumn: {
      width: 120,
      fixed: 'right',
    },
    beforeFetch: async (params) => {
      return Object.assign(params, queryParam);
    },
  }
});
const [registerTable, { getDataSource }] = tableContext;

function edit(record) {
  nextTick(() => {
    oplData.value = record;
    if (record.writeTwoRrList) {
      saveBtnShowProport.value = true;
      dataSource.value = record.writeTwoRrList
    } else {
      saveBtnShowProport.value = false;
    }
  });
}

async function handleAdd() {
  let dataSource = getDataSource();
  if (dataSource && dataSource.length == 0) {
    createMessage.warning("保存记录为空");
    return false;
  }
  await updateByWriteTwoRr({
    id: oplData.value.id,
    writeTwoRrList: dataSource
  })
    .then((res) => {
      if (res.success) {
        createMessage.success(res.message);
        emit('ok');
      } else {
        createMessage.warning(res.message);
      }
    })
    .finally(() => {
      confirmLoading.value = false;
    });
}

defineExpose({
  edit,
});
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
    text-align: center
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
