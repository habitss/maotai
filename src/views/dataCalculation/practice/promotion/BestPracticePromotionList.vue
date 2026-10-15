<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button type="primary" v-auth="'practice:mes3_best_practice_promotion:add'" @click="handleAdd"
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
          <a-button v-auth="'practice:mes3_best_practice_promotion:deletionBatch'">批量操作
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
      
      <!--年月插槽-->
      <template #form-yearMon="{ model, field }">
        <KtMonthsPicker v-model:value="model[field]" :maxTagCount="2" />
      </template>

      <!-- 结果插槽 -->
      <template #reviewResult="{ record }">
        <a-tag v-if="record.reviewResult == 2" color="#FFFF00" style="padding: 4px 20px; color: #000">建议推广</a-tag>
        <a-tag v-else-if="record.reviewResult == 3" color="#008000" style="padding: 4px 20px;">强制推广</a-tag>
        <a-tag v-else color="#FE0000" style="padding: 4px 20px;">不推广</a-tag>
      </template>
    </BasicTable>
    <!-- 表单区域 -->
    <BestPracticePromotionModal @register="registerModal" @success="handleSuccess"></BestPracticePromotionModal>
  </div>
</template>

<script lang="ts" name="practice-bestPracticePromotion" setup>
import { onMounted, reactive } from 'vue';
import { BasicTable, TableAction } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useListPage } from '/@/hooks/system/useListPage'
import BestPracticePromotionModal from './components/BestPracticePromotionModal.vue'
import KtMonthsPicker from '/@/components/KtMonthsPicker/KtMonthsPicker.vue';
import { columns, searchFormSchema, getDepart } from './BestPracticePromotion.data';
import { list, deleteOne, batchDelete, getImportUrl, getExportUrl } from './BestPracticePromotion.api';
const queryParam = reactive<any>({});
//注册model
const [registerModal, { openModal }] = useModal();
//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    title: '最佳实践评估',
    api: list,
    ellipsis: false,
    formConfig: {
      schemas: searchFormSchema,
      autoSubmitOnEnter: true,
      showAdvancedButton: true,
      fieldMapToNumber: [
      ],
      fieldMapToTime: [
      ],
    },
    actionColumn: {
      width: 120,
      fixed: 'right'
    },
    beforeFetch: (params) => {
      return Object.assign(params, queryParam);
    },
    afterFetch: (res) => {
      for(const item of res) {
        // 解析识别部门数据
        const arr = item.identificationShop ? JSON.parse(item.identificationShop) : [];
        for(const obj of arr) {
          item[obj.field] = obj.value;
        }
      }
      return res;
    }
  },
  exportConfig: {
    name: "最佳实践评估",
    url: getExportUrl,
    params: queryParam,
  },
  importConfig: {
    url: getImportUrl,
    success: handleSuccess
  },
})

const [registerTable, { reload, setColumns }, { rowSelection, selectedRowKeys }] = tableContext

/**
 * 新增事件
 */
function handleAdd() {
  openModal(true, {
    isUpdate: false,
    showFooter: true,
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
      auth: 'practice:mes3_best_practice_promotion:edit'
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
      auth: 'practice:mes3_best_practice_promotion:delete'
    }
  ]
}

onMounted(async () => {
  //获取部门数据
  const deptArr = await getDepart() ?? [];
  //组装表头数据
  let headers = [] as any;
  for(const item of deptArr){
    headers.push(
    {
      title: item.departName,
      align: 'center',
      dataIndex: 'sbcj_'+item.orgCode+'_'+item.departName,
      width: 90,
      customRender: ({ text }) => {
        return text === '1' ? '适用' : text === '0' ? '不适用' : '不选择';
      },
      customCell : (record, rowIndex, column) => {
        const fontColor = record[column.dataIndex] === '1' ? '#008000' : record[column.dataIndex] === '0' ? '#FE0000' : '';
        return {
          style: {
            color: fontColor // 修改为你想要的颜色
          }
        }
      }
    })
  }
  columns[2].children = headers;
  setColumns(columns);
})
</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}
</style>