<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--插槽:table标题-->
      <template #tableTitle>
        <div style="width: 100%;">
          <a-tabs v-model:activeKey="activeKey" @change="handleTabsChange">
            <a-tab-pane v-for="i in tabsData" :key="i.id" :tab="i.postName"></a-tab-pane>
          </a-tabs>
        </div>
        <a-button type="primary" v-auth="'manage.check:mes3_manager_checklist_edit_main:add'" @click="handleAdd"
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
          <a-button v-auth="'manage.check:mes3_manager_checklist_edit_main:deletionBatch'">批量操作
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>

      <template #stopProductionSlot="{ text }">
        <a-tag v-if="text == 0" color="red">停产</a-tag>
        <a-tag v-else-if="text == 1" color="green">生产</a-tag>
        <a-tag v-else-if="text == 2" color="red">休假</a-tag>
      </template>

      <template #isEditSlot="{ text }">
        <a-tag v-if="text != 1" color="red">未录入</a-tag>
        <a-tag v-else color="green">已录入</a-tag>
      </template>

      <template #confirmResultSlot="{ record, text }">
        <div v-if="text == '1'"><a-tag color="green">通过</a-tag></div>
        <div v-else-if="text == '2'"><a-tag color="red">不通过</a-tag></div>
        <div v-else>
          <a-tag v-if="record.isEdit == 1">
            <a-popconfirm title="请审核" ok-text="通过" cancel-text="不通过" 
              @confirm="() => handleConfirm(record, 1)"
              @cancel="() => handleConfirm(record, 2)"
            >未审核</a-popconfirm>
          </a-tag>
          
          <a-tag v-else="record.isEdit == 1">
            <a-popconfirm title="请先录入数据" ok-text="关闭" :showCancel="false"
            >未审核</a-popconfirm>
          </a-tag>
        </div>
      </template>
    </BasicTable>
    <!-- 表单区域 -->
    <ManagerChecklistEditMainModal @register="registerModal" @success="handleSuccess"></ManagerChecklistEditMainModal>
  </div>
</template>

<script lang="ts" name="manage.check-managerChecklistEditMain" setup>
import { onMounted, reactive, ref } from 'vue';
import { BasicTable, TableAction } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useListPage } from '/@/hooks/system/useListPage'
import ManagerChecklistEditMainModal from './components/ManagerChecklistEditMainModal.vue'
import { columns, searchFormSchema } from './ManagerChecklistEditMain.data';
import { list, deleteOne, batchDelete, confirmData } from './ManagerChecklistEditMain.api';
import { list as postList } from '/@/views/dataCalculation/mdm/postManage/MdmPost.api';
import { departListAll } from '/@/api/common/api';
import { useMessage } from '/@/hooks/web/useMessage';
import { useUserStore } from '/@/store/modules/user';
const userStore = useUserStore();

//注册Message
const { createConfirm } = useMessage();
//查询对象
const queryParam = reactive<any>({});
//tabs页签选中值
const activeKey = ref(null);
//tabs页签选中文字
const activeText = ref('');
//tabs页签选中所属部门
const activeDeptText = ref('');
//tabs页签数据
const tabsData = ref([] as any);
//部门数据
const departArr = ref([] as any);
const deptName = ref('');
//注册model
const [registerModal, { openModal }] = useModal();
//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    title: '分层检查-检查清单录入主表',
    api: list,
    columns,
    immediate: false,
    maxColumnWidth: 90,
    formConfig: {
      schemas: searchFormSchema,
      autoSubmitOnEnter: true,
      showAdvancedButton: false,
      fieldMapToTime: [
        ['recordDate', ['recordDate_begin', 'recordDate_end'], 'YYYY-MM-DD']
      ],
      submitFunc: searchSubmit,
    },
    actionColumn: {
      title: '操作',
      width: 120,
      fixed: 'right'
    },
    beforeFetch: (params) => {
      delete params.deptCode;
      return Object.assign(params, queryParam);
    },
    afterFetch: (res) => {
      for (const item of res) {
        item.dynamicName = activeText.value + '检查';
        item.dynamicDept = activeDeptText.value;
      }
      return res;
    }
  },
})

const [registerTable, { reload, getForm, setTableData }, { rowSelection, selectedRowKeys }] = tableContext;

/**
 * 查询按钮事件 
 */
async function searchSubmit() {
  //获取查询参数
  const params = getForm()?.getFieldsValue();
  let deptCode = params?.deptCode;
  let lineCode = null;
  if(deptCode.length <= 9){
    deptCode = null;
  }else if(deptCode.length > 12){
    lineCode = deptCode;
    deptCode = null;
  }

  //获取tabs数据
  const { records } = await postList({
    deptCode: deptCode,
    lineCode: lineCode,
    postType: 1,
    postName: params?.postName,
    column: 'sort',
    order: 'asc',
    pageSize: 10000
  });
  tabsData.value = records;
  deptName.value = departArr.value?.find(item => item.orgCode === params?.deptCode)?.departName;;

  //处理tabs默认选中数据
  if (records.length !== 0) {
    //如果查询后还有之前选中的tab，则选中之前的tabs;如果没有查询到之前选中的tabs，则默认选中第一个tab;
    const activeObject = records.find(item => item.id === activeKey.value);
    activeKey.value = activeObject ? activeKey.value : records[0].id;
  } else {
    activeKey.value = null;
  }
  handleTabsChange(activeKey.value);
}

//tabs页签切换事件
function handleTabsChange(key) {
  const findObj = tabsData.value?.find(item => item.id === key);
  queryParam.recordDate_begin = getForm()?.getFieldsValue().recordDate_begin;
  queryParam.recordDate_end = getForm()?.getFieldsValue().recordDate_end;
  queryParam.postId = key;
  activeKey.value = key;
  activeText.value = findObj?.postName;
  activeDeptText.value = findObj?.deptCode_dictText;

  if (activeText.value) {
    reload()
  } else {
    setTableData([])
  }
}

/**
 * 新增事件
 */
function handleAdd() {
  openModal(true, {
    deptCode: getForm()?.getFieldsValue().deptCode,
    postId: activeKey.value,
    postName: activeText.value,
    record: {},
    isUpdate: false,
    showFooter: true,
  });
}
/**
 * 编辑事件
 */
function handleEdit(record: Recordable) {
  openModal(true, {
    deptCode: getForm()?.getFieldsValue().deptCode,
    postId: activeKey.value,
    postName: activeText.value,
    record,
    isUpdate: true,
    showFooter: record.resultConfirmBy ? false : true,
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
      auth: 'manage.check:mes3_manager_checklist_edit_main:edit'
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
      auth: 'manage.check:mes3_manager_checklist_edit_main:delete'
    }
  ]
}

/**
 * 审核事件
 * @param record 
 * @param type 
 */
async function handleConfirm(record, type) {
  let str = '确定审批通过?';
  if (type == '2') {
    str = '确定审批不通过?';
  }
  createConfirm({
    title: '提示',
    content: str,
    iconType: 'warning',
    onOk: async () => {
      record.resultConfirmBy = userStore?.userInfo?.id;
      record.confirmResult = type;
      await confirmData(record);
      reload();
    },
  });
}

onMounted(async () => {
  departArr.value = await departListAll({});
  searchSubmit();
})
</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}

:deep(.jeecg-basic-table-header__toolbar) {
  display: none;
}

:deep(.jeecg-basic-table-header__tableTitle) {
  width: 100%;
}
</style>