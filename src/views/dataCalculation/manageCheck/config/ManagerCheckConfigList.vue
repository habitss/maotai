<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button type="primary" v-auth="'manage.check:mes3_manager_check_config:add'" @click="handleAdd"
          preIcon="ant-design:plus-outlined"> 新增</a-button>
        <a-button type="primary" v-auth="'manage.check:mes3_manager_check_config:exportXls'"
          preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
        <j-upload-button type="primary" v-auth="'manage.check:mes3_manager_check_config:importExcel'"
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
          <a-button v-auth="'manage.check:mes3_manager_check_config:deletionBatch'">批量操作
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>

      <!-- 配置插槽 -->
      <template #configSlot="{ record, column, text }">
        <div 
          :class="{text, 'auth-text': hasPermission('manage.check:mes3_manager_check_config:edit')}"
          @click="hasPermission('manage.check:mes3_manager_check_config:edit') ? handleConfigClick(record, column) : null;"
        >
          <div v-if="text">
            <a-row>
              <a-col :span="10">频率：</a-col>
              <a-col :span="14">{{ text[0] }}</a-col>
            </a-row>
            <a-row v-for="(item, index) in getCycle(record, column.dataIndex, text[0], text[1])">
              <a-col :span="10">{{ index === 0 ? '周期：' : ''}}</a-col>
              <a-col :span="14">{{ item }}</a-col>
            </a-row>
          </div>
          <div v-else>
            /
          </div>
        </div>

        <!-- <div v-else>{{ text || '/' }}</div> -->
      </template>
    </BasicTable>
    <!-- 表单区域 -->
    <ManagerCheckConfigModal @register="registerModal" @success="handleSuccess"></ManagerCheckConfigModal>
    <!-- 岗位配置表单区域 -->
    <ManagerCheckGwpzModal @register="registerGwpzModal" @success="handleSuccess"></ManagerCheckGwpzModal>
  </div>
</template>

<script lang="ts" name="manage.check-managerCheckConfig" setup>
import { onMounted, reactive, ref } from 'vue';
import { BasicTable, TableAction } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useListPage } from '/@/hooks/system/useListPage'
import { columns, searchFormSchema } from './ManagerCheckConfig.data';
import { list, deleteOne, batchDelete, getImportUrl, getExportUrl } from './ManagerCheckConfig.api';
import { list as postList } from '/@/views/dataCalculation/mdm/postManage/MdmPost.api';
import ManagerCheckConfigModal from './components/ManagerCheckConfigModal.vue'
import ManagerCheckGwpzModal from './components/ManagerCheckGwpzModal.vue'
import { cloneDeep } from 'lodash-es';
import { usePermission } from '/@/hooks/web/usePermission'
const { hasPermission } = usePermission();
const queryParam = reactive<any>({});
//记录是否切换部门
const oldDept = ref(null);
//注册model
const [registerModal, { openModal }] = useModal();
//注册岗位配置model
const [registerGwpzModal, { openModal: openGwpzModel }] = useModal();
//注册table数据
const { tableContext, onExportXls, onImportXls } = useListPage({
  tableProps: {
    title: '分层检查-检查识别矩阵',
    api: list,
    maxColumnWidth: 80,
    ellipsis: false,
    immediate: false,
    pagination: {
      pageSizeOptions: ['10', '20', '50'],
    },
    formConfig: {
      schemas: searchFormSchema,
      autoSubmitOnEnter: true,
      showAdvancedButton: true,
      submitFunc: onSubmit
    },
    actionColumn: {
      width: 120,
      title: '操作',
      fixed: 'right'
    },
    defSort: { column: 'sort', order: 'asc' },
    beforeFetch: (params) => {
      return Object.assign(params, queryParam);
    },
    afterFetch: (records) => {
      //处理数据，将岗位配置信息拼接成字符串显示在表格中
      records.forEach(item => {
        let gwpzArr = item.gwpzJson ? JSON.parse(item.gwpzJson) : [];
        gwpzArr.forEach(gwpz => {
          item[gwpz.id] = [gwpz.plName, gwpz.weekName];
        });
      })
      return records;
    }
  },
  exportConfig: {
    name: "分层检查-检查识别矩阵",
    url: getExportUrl,
    params: {
      ...queryParam, ...{column: 'sort', order: 'asc'}
    },
  },
  importConfig: {
    url: getImportUrl,
    success: handleSuccess
  },
})

const [registerTable, { reload, getForm, setColumns, setLoading }, { rowSelection, selectedRowKeys }] = tableContext;

//重写表单查询事件
async function onSubmit() {
  setLoading(true);
  await setHeader();
  reload();
}

/**
 * 设置动态表头
 */
async function setHeader() {
  //部门改变时重新设置表头信息
  if (getForm().getFieldsValue().deptCode === oldDept.value) {
    return;
  }
  //获取静态表头数据
  let newColumns = cloneDeep(columns);
  //获取岗位数据
  const { records } = await postList({
    pageSize: 1000,
    postType: 1,
    deptCode: getForm().getFieldsValue().deptCode,
    lineCode: getForm().getFieldsValue().lineCode,
    id: getForm().getFieldsValue().postId,
    column: 'sort',
    order: 'asc'
  });
  //动态添加岗位列头信息
  for (const item of records) {
    newColumns.push({
      title: item.postName,
      align: "center",
      dataIndex: item.id,
      width: 124,
      slots: { customRender: 'configSlot' }
    })
  }
  setColumns(newColumns);
}

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
 * 岗位配置事件
 */
function handleConfigClick(record, column) {
  openGwpzModel(true, {
    record,
    column,
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
      auth: 'manage.check:mes3_manager_check_config:edit'
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
      auth: 'manage.check:mes3_manager_check_config:delete'
    }
  ]
}

//解析频率
function getCycle(record, columnField, text, cycle){
  let resltData = [] as any;
  //处理月年配置
  if(text.indexOf('月') > -1 || text.indexOf('年') > -1){
    //转换配置为数组
    const configArr = JSON.parse(record.gwpzJson);
    //获取对应的配置项
    const findObj = configArr.find(item => item.id == columnField);
    //每月推送
    if(findObj.pl == 89 || findObj.pl == 91){
      resltData.push(findObj.day ? findObj.day+'号推送' : cycle);
    } else {
      //季度、半年、年推送
      resltData.push(findObj.monDay1);
      resltData.push(findObj.monDay2);
      resltData.push(findObj.monDay3);
      resltData.push(findObj.monDay4);
    }
  } else {
    resltData.push(cycle);
  }
  return resltData;
}

onMounted(() => {
  onSubmit();
});

</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}

.text{
  max-width: 100%; 
  padding: 0;
  white-space: normal;
  word-break: break-all;
  text-align: left;
}

.auth-text{
  color: @primary-color;
  cursor: pointer;
}
</style>