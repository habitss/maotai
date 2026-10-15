<template>
  <div>
    <!--引用表格-->
   <BasicTable @register="registerTable" :rowSelection="rowSelection">
     <!--插槽:table标题-->
      <template #tableTitle>
          <a-button type="primary" v-auth="'pi:mes3_pi_spreadjs_main:add'" @click="handleAdd" preIcon="ant-design:plus-outlined"> 新增</a-button>
          <a-dropdown v-if="selectedRowKeys.length > 0">
              <template #overlay>
                <a-menu>
                  <a-menu-item key="1" @click="batchHandleDelete">
                    <Icon icon="ant-design:delete-outlined"></Icon>
                    删除
                  </a-menu-item>
                </a-menu>
              </template>
              <a-button v-auth="'pi:mes3_pi_spreadjs_main:deletionBatch'">批量操作
                <Icon icon="mdi:chevron-down"></Icon>
              </a-button>
        </a-dropdown>
      </template>
       <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)"/>
      </template>
      <!--字段回显插槽-->
      <template v-slot:bodyCell="{ column, record, index, text }">
      </template>
    </BasicTable>
    <!-- 表单区域 -->
    <PiSpreadjsMainModal @register="registerModal" @success="handleSuccess"></PiSpreadjsMainModal>
    <!--报表配置-->
    <TemplateModal @register="registerTemplateModal" @success="reload"></TemplateModal>
  </div>
  
  <a-modal 
    width="800"
    :title="title" 
    :visible="visible" 
    @ok="handleOk" 
    okText="复制URL地址"
    @cancel="handleCloseMenuUrlModal" 
    cancelText="关闭"
  >
    <div style="height: 100px; padding: 20px; color: #909399;">
      {{ menuUrlToCopy }}
    </div>
  </a-modal>
</template>

<script lang="ts" name="pi-piSpreadjsMain" setup>
  import {ref, reactive, computed, unref} from 'vue';
  import {BasicTable, useTable, TableAction} from '/@/components/Table';
  import {useModal} from '/@/components/Modal';
  import { useListPage } from '/@/hooks/system/useListPage'
  import { useCopyToClipboard } from '/@/hooks/web/useCopyToClipboard';
  import PiSpreadjsMainModal from './components/PiSpreadjsMainModal.vue'
  import {columns, searchFormSchema} from './PiSpreadjsMain.data';
  import {list, deleteOne, batchDelete, getImportUrl,getExportUrl} from './PiSpreadjsMain.api';
  import { useMessage } from '/@/hooks/web/useMessage';
  import TemplateModal from './components/TemplateModal.vue';

  //配置菜单地址modal
  const visible = ref<boolean>(false);
  //配置菜单地址modal标题
  const title = ref<string>('');
  //配置菜单复制文本
  const menuUrlToCopy = ref('');
  
  //消息提示
  const { createMessage: msg } = useMessage();
  
  //报表模板modal
  const [registerTemplateModal, { openModal : openTemplateModal }] = useModal();
  
  const queryParam = reactive<any>({});
  //注册model
  const [registerModal, {openModal}] = useModal();
  //注册table数据
  const { prefixCls,tableContext,onExportXls,onImportXls } = useListPage({
      tableProps:{
        title: 'SpreadJS模板配置',
        api: list,
        columns,
        canResize:false,
        formConfig: {
          //labelWidth: 120,
          schemas: searchFormSchema,
          autoSubmitOnEnter:true,
          showAdvancedButton:true,
          fieldMapToNumber: [
          ],
          fieldMapToTime: [
          ],
        },
        actionColumn: {
          width: 270,
          fixed:'right'
        },
        beforeFetch: (params) => {
          params.column = 'sort'
          params.order = 'asc'
          return Object.assign(params, queryParam);
        },
      },
      exportConfig: {
        name:"SpreadJS模板配置",
        url: getExportUrl,
        params: queryParam,
      },
      importConfig: {
        url: getImportUrl,
        success: handleSuccess
      },
  })

  const [registerTable, {reload},{ rowSelection, selectedRowKeys }] = tableContext

  /**
   * 高级查询事件
   */
  // function handleSuperQuery(params) {
  //   Object.keys(params).map((k) => {
  //     queryParam[k] = params[k];
  //   });
  //   reload();
  // }
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
     await deleteOne({id: record.id}, handleSuccess);
   }
   /**
    * 批量删除事件
    */
  async function batchHandleDelete() {
     await batchDelete({ids: selectedRowKeys.value}, handleSuccess);
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
  function getTableAction(record){
    return [
      {
        label: '编辑',
        onClick: handleEdit.bind(null, record),
        auth: 'pi:mes3_pi_spreadjs_main:edit'
      },
      {
        label: '模板配置',
        onClick: handleConfig.bind(null, record),
        auth: 'pi:mes3_pi_spreadjs_main:config'
      },
      {
        label: '配置菜单',
        onClick: handleShowMenuUrlModal.bind(null, record),
      },
    ]
   }
  /**
    * 下拉操作栏
    */
  function getDropDownAction(record){
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
        auth: 'pi:mes3_pi_spreadjs_main:delete'
      }
    ]
   }
  /**
   * 模板配置
   */
  function handleConfig(record){
    openTemplateModal(true, {record})
  }
  /**
   * 打开配置菜单链接
   */
  function handleShowMenuUrlModal(record){
    // if(!record.reportConfig){
    //   msg.error({ content: '请先配置模板' });
    //   return;
    // }
    title.value = '【'+record.templateName+'】菜单地址';
    menuUrlToCopy.value = '/dataCalculation/pi/spreadJs/show/'+record.id;
    visible.value = true;
  }
  /**
   * 关闭配置菜单链接
   */
  function handleCloseMenuUrlModal(){
    visible.value = false;
  }
  /**
   * 复制配置菜单链接
   */
  async function handleOk(){
    const { isSuccessRef } = useCopyToClipboard(menuUrlToCopy.value);
    if(isSuccessRef.value){
      msg.success('复制URL成功');
    } else {
      msg.error('复制URL失败，请手动复制链接');
    }
  }
</script>

<style lang="less" scoped>
  :deep(.ant-picker),:deep(.ant-input-number){
    width: 100%;
  }
</style>