<template>
<BasicTable @register="registerTable">
  <template #action="{ record, column }">
    <!-- 操作列 -->
    <TableAction :actions="createActions(record, column)" />
  </template>
    
    <!-- 操作按钮 -->
    <template #tableTitle>
      <a-button type="primary" preIcon="ant-design:plus-outlined" @click="onAdd">新增</a-button>
    </template>
</BasicTable>

<!-- 填报modal -->
<TemplateModal @register="registerTemplateModal" @success="reload" />
</template>
<script lang="ts" name="dataCalculation-report-query-index" setup>
import { onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { ActionItem, BasicTable, BasicColumn, TableAction, EditRecordRow } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage';
import { useMessage } from '/@/hooks/web/useMessage';
import { useModal } from '/@/components/Modal';
import { getHeaderList, getList, deleteData } from './reportQueryList.api';
import { getDictItemList } from '../customReport/index.api';
import { getById } from '../reportTemplate/index.api';
import TemplateModal from './components/templateModal.vue';

//获取路由
const route = useRoute();
//获取路由中的code参数
const code = route.params.code;
//模板详细数据
const templateRecord = ref();

//消息提示组件
const { createMessage: msg, createConfirm: confirm } = useMessage();


//填报modal
const [registerTemplateModal, { openModal : openTemplateModal }] = useModal();

// 列表页面公共参数、方法
const { tableContext } = useListPage({
  tableProps: {
    title: '统计查询',
    api: getList,
    immediate: false,
    beforeFetch: function(params){
      params.templateId = code;
    },
    showActionColumn: true,
    actionColumn: {
      width: 180,
      fixed: 'right',
    },
    rowKey:"id",
  },
});

/**
 * 创建操作列
 */
function createActions(record: EditRecordRow, column: BasicColumn): ActionItem[] {
  return [
    {
      label: '编辑',
      onClick: handleEdit.bind(null, record),
    },
    {
      label: '详情',
      onClick: handleDetail.bind(null, record),
    },
    {
      label: '删除',
      popConfirm: {
        title: '是否删除？',
        confirm: handleDelete.bind(null, record.id),
      },
    },
  ];
}

// BasicTable绑定注册
const [registerTable, { reload, setSelectedRowKeys, setProps, getForm },  { rowSelection, selectedRowKeys, selectedRows }] = tableContext;

/**
 * 新增事件
 */
function onAdd() {
  openTemplateModal(true, {flag: 'add', templateRecord: templateRecord.value})
}

/**
 * 编辑事件
 */
function handleEdit(record: Recordable) {
  openTemplateModal(true, {flag: 'edit', templateRecord: templateRecord.value, record})
}

/**
 * 详情
 */
function handleDetail(record: Recordable) {
  openTemplateModal(true, {flag: 'detail', templateRecord: templateRecord.value, record})
}

/**
 * 删除
 */
async function handleDelete(id){
  try {
    await deleteData({
      tableName: templateRecord.value.tableName,
      recordId: id
    })
    handleSuccess()
  } catch {}
}

//获取动态配置
async function getDynamicConfig(){
  const cellComponentsList = JSON.parse(templateRecord.value.reportConfig).cellComponentsList;
  //动态表头
  let dynamicColumns = [] as any;
  //动态查询表单
  let dynamicSearchForm = [] as any;
  try {
    //获取动态表头数据
    const res = await getHeaderList({ templateId: code });
    if(res?.length > 0){
      //默认添加id列
      dynamicColumns.push({
        dataIndex: 'id',
        defaultHidden: true,
      })
      //解析表头数据
      for(const item of res){
        //添加动态表头
        let columnsObj = {
          title: item.name,
          dataIndex: item.fieldName,
          resizable: true,
          width: 150
        }
        dynamicColumns.push(columnsObj);

        //添加动态查询
        if(item.isQuery === 1){
          let component = 'Input';
          let componentProps = {};
          if(item.type == '4'){
            //日期类型
            component = 'RangeDate';
          } else if(item.type == '6'){
            //日期时间类型
            component = 'RangePicker';
            componentProps = {
              showTime: true,
              valueFormat:'YYYY-MM-DD HH:mm:ss',
            };
          } else {
            const cellComponent = cellComponentsList.find(cell => cell.mappingColumn == item.fieldName && cell.dictType);
            //判断是否为下拉字典
            if(cellComponent){
              component = 'ApiSelect';
              componentProps = {
                api: getDictItemList,
                numberToString: false,
                labelField: 'itemKey',
                valueField: 'itemVal',
                params:{
                  pageSize: 999,
                  groupCode: cellComponent.dictType
                },
                resultField:'records'
              };
            }
          }
          let searchObj = {
            label: item.name,
            field: item.fieldName,
            component,
            componentProps,
            colProps: {
              span: 8,
            },
          }
          dynamicSearchForm.push(searchObj);
        }
      }
    }
  } catch (error) {
    msg.error({ content: '操作失败', key: 'saving' });
  } finally {
    //设置table参数
    setProps({ 
      columns: dynamicColumns,
      formConfig: {
        schemas: dynamicSearchForm,
      }
    });
    reload();
  }
}

/**
 * 成功回调
 */
function handleSuccess() {
  (selectedRowKeys.value = []) && reload();
}

onMounted(async () => {
  //获取模板详细信息
  templateRecord.value = await getById(code);
  //获取动态配置
  await getDynamicConfig();
});
</script>
<style scoped>
  /** 时间和数字输入框样式 */
  :deep(.ant-input-number) {
    width: 100%;
  }

  :deep(.ant-picker) {
    width: 100%;
  }

  /** 下拉框默认文字颜色 */
  :deep(.ant-select-selection-placeholder) {
    color: #BEBEBE !important;
  }
</style>