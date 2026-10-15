<template>
  <a-tabs v-model:activeKey="activeKey" style="padding: 10px">
    <a-tab-pane key="1" tab="我提供的">
      <div class="p-2" style="padding: 0">
        <!--引用表格-->
        <BasicTable @register="registerTable" :rowSelection="rowSelection">
          <!--插槽:table标题-->
          <template #tableTitle>
            <a-button  type="primary" v-auth="'serviceAgreement:mes3_service_agreement_exception:exportXls'" preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
            <a-dropdown v-if="selectedRowKeys.length > 0">
              <template #overlay>
                <a-menu>
                  <a-menu-item key="1" @click="batchHandleDelete">
                    <Icon icon="ant-design:delete-outlined"></Icon>
                    删除
                  </a-menu-item>
                </a-menu>
              </template>
              <a-button v-auth="'serviceAgreement:mes3_service_agreement_exception:deleteBatch'">批量操作
                <Icon icon="mdi:chevron-down"></Icon>
              </a-button>
            </a-dropdown>
          </template>
          <!--操作栏-->
          <!--插槽:表格内容-->
          <template #bodyCell="{ column, record, index }">
            <div v-if="column.dataIndex == 'exceptionFile'" class="custom-div">
              <a-button type="link" @click="showDetail(record)">查看</a-button>
            </div>
            <div v-if="column.dataIndex == 'fiveWhyStatus' && record.fiveWhyStatus == 0" class="custom-div">
                <a-button type="link" @click="fiveWhy(record)">未分析</a-button>
            </div>
            <div v-else-if="column.dataIndex == 'fiveWhyStatus' && record.fiveWhyStatus == 1" class="custom-div">
              <a-button type="link"  @click="fiveWhy(record)">已分析</a-button>
            </div>
            <div v-if="column.dataIndex == 'actionStatus' && record.actionStatus == 0" class="custom-div">
                <a-button type="link" @click="fiveWhy(record)">未记录</a-button>
            </div>
            <div v-else-if="column.dataIndex == 'actionStatus' && record.actionStatus == 1" class="custom-div">
              <a-button type="link" @click="fiveWhy(record)">已记录</a-button>
            </div>
            <div v-if="column.dataIndex == 'examineStatus' && record.examineStatus == 0" class="custom-div">
              <a-popconfirm
                title="确定要执行此操作吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleConfirm(record)"
                @cancel="handleCancel"
              >
              <a-button type="link">待审批</a-button>
              </a-popconfirm>
            </div>
            <div v-else-if="column.dataIndex == 'examineStatus' && record.examineStatus == 1" class="custom-div">
              <a-button type="link">已审批</a-button>
            </div>
            <div v-if="column.dataIndex == 'confirmStatus'" class="custom-div">
              <a-button type="link" @click="handleConfirmStatus(record)">
                {{ record.confirmStatus == 1 ? '已确认' : '未确认' }}
              </a-button>
            </div>
          </template>
          <!--  deptCode部门查询插槽    -->
          <template #form-provideDeptPro="{ model, field }">
            <kt-depart-select
              v-model:value="model[field]"
              select-type="depart"
              :placeholder="t('language.selectDepartment')"
            />
          </template>

          <!--  deptCode部门查询插槽    -->
          <template #form-receiveDeptPro="{ model, field }">
            <kt-depart-select
              v-model:value="model[field]"
              select-type="depart"
              :placeholder="t('language.selectDepartment')"
            />
          </template>
        </BasicTable>
        <FiveWhyModel @register="registerFiveWhyModal" @success="handleSuccess"></FiveWhyModel>
        <ExceptionFilesModel @register="registerFilesModal" @success="handleSuccess"></ExceptionFilesModel>
      </div>
    </a-tab-pane>
    <a-tab-pane key="2" tab="我接收的" force-render>
      <div class="p-2" style="padding: 0">
        <!--引用表格-->
        <BasicTable @register="registerRecevieTable" :rowSelection="rowSelectionRec">
          <!--插槽:table标题-->
          <template #tableTitle>
            <a-button type="primary" v-auth="'serviceAgreement:mes3_service_agreement_exception:add'"  @click="handleAddRec" preIcon="ant-design:plus-outlined"> 新增</a-button>
            <a-button  type="primary" v-auth="'serviceAgreement:mes3_service_agreement_exception:exportXls'" preIcon="ant-design:export-outlined" @click="onExportRecXls"> 导出</a-button>
            <a-dropdown v-if="selectedRowKeysRec.length > 0">
              <template #overlay>
                <a-menu>
                  <a-menu-item key="1" @click="batchHandleDeleteRec">
                    <Icon icon="ant-design:delete-outlined"></Icon>
                    删除
                  </a-menu-item>
                </a-menu>
              </template>
              <a-button v-auth="'serviceAgreement:mes3_service_agreement_exception:deleteBatch'">批量操作
                <Icon icon="mdi:chevron-down"></Icon>
              </a-button>
            </a-dropdown>
          </template>

          <!--插槽:表格内容-->
          <template #bodyCell="{ column, record, index }">
            <div v-if="column.dataIndex == 'exceptionFile'" class="custom-div">
              <a-button type="link" @click="showDetail(record)">查看</a-button>
            </div>
            <div v-if="column.dataIndex == 'fiveWhyStatus' && record.fiveWhyStatus == 0" class="custom-div">
              <a-button type="link" @click="fiveWhy(record)">未分析</a-button>
            </div>
            <div v-else-if="column.dataIndex == 'fiveWhyStatus' && record.fiveWhyStatus == 1" class="custom-div">
              <a-button type="link"  @click="fiveWhy(record)">已分析</a-button>
            </div>
            <div v-if="column.dataIndex == 'actionStatus' && record.actionStatus == 0" class="custom-div">
              <a-button type="link" @click="fiveWhy(record)">未记录</a-button>
            </div>
            <div v-else-if="column.dataIndex == 'actionStatus' && record.actionStatus == 1" class="custom-div">
              <a-button type="link" @click="fiveWhy(record)">已记录</a-button>
            </div>
          </template>
          <!--  deptCode部门查询插槽    -->
          <template #form-provideDeptRec="{ model, field }">
            <kt-depart-select
              v-model:value="model[field]"
              select-type="depart"
              :placeholder="t('language.selectDepartment')"
            />
          </template>

          <!--  deptCode部门查询插槽    -->
          <template #form-receiveDeptRec="{ model, field }">
            <kt-depart-select
              v-model:value="model[field]"
              select-type="depart"
              :placeholder="t('language.selectDepartment')"
            />
          </template>
          <!--操作栏-->
          <template #action="{ record }">
            <TableAction :actions="getTableActionRec(record)"/>
          </template>
        </BasicTable>
        <!-- 表单区域 -->
        <ServiceAgreementExceptionForm @register="registerAddModal" @success="handleSuccess"></ServiceAgreementExceptionForm>
        <ConfirmStatusModal @register="registerConfirmStatusModal" @success="handleSuccess"></ConfirmStatusModal>
      </div>
    </a-tab-pane>
  </a-tabs>

</template>

<script lang="ts" name="serviceAgreement-serviceAgreementException" setup>
import {ref, reactive, nextTick} from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import {columns, columnsRec,searchFormProvidSchema } from './ServiceAgreementException.data';
  import {
    list,
    deleteOne,
    batchDelete,
    getImportUrl,
    getExportUrl,
    check
  } from './ServiceAgreementException.api';
  import { downloadFile } from '/@/utils/common/renderUtils';
  import ServiceAgreementExceptionForm from './components/ServiceAgreementExceptionForm.vue';
  import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
  import { useUserStore } from '/@/store/modules/user';
  import {useModal} from "@/components/Modal";
  import FiveWhyModel from './components/FiveWhyModel.vue';
  import {
    searchFormSchema
  } from "@/views/dataCalculation/serviceAgreement/ServiceAgreementException.data";
  import { useI18n } from '/@/hooks/web/useI18n';
import ExceptionFilesModel
  from "@/views/dataCalculation/serviceAgreement/components/ExceptionFilesModel.vue";
import ConfirmStatusModal from './components/ConfirmStatusModal.vue';

  const { t } = useI18n();
  const activeKey = ref('1');
  const formRef = ref();
  const queryParam = reactive<any>({});
  const toggleSearchStatus = ref<boolean>(false);
  const registerModal = ref();
  const userStore = useUserStore();
  //注册table数据
  const { prefixCls, tableContext, onExportXls } = useListPage({
    tableProps: {
      title: '服务协议异常记录表',
      api: list,
      columns,
      canResize:false,
      showActionColumn: false,
      scroll: { y: 420 },
      beforeFetch: async (params) => {
        params.deptCode = userStore?.userInfo?.orgCode;
        params.type = 1;
        params.column = 'exceptionDate';
        params.order = 'desc';
        return Object.assign(params, queryParam);
      },
      // afterFetch: () =>{
      //   queryParam.provideDept = userStore?.userInfo?.orgCode;
      // },
      formConfig: {
        schemas: searchFormProvidSchema,
        showAdvancedButton: false,
      },
    },
    exportConfig: {
      name: "服务协议异常记录表",
      url: getExportUrl,
      params: Object.assign({deptCode:userStore?.userInfo?.orgCode,type:1}, queryParam),
    },
	  importConfig: {
	    url: getImportUrl,
	    success: handleSuccess
	  },
  });
  const [registerTable, { reload, collapseAll, updateTableDataRecord, findTableDataRecord, getDataSource },
    { rowSelection, selectedRowKeys }] = tableContext;

  const { prefixCls:prefixRecCls, tableContext:tableRecContext, onExportXls: onExportRecXls} = useListPage({
    tableProps: {
      title: '服务协议异常记录表',
      api: list,
      columns:columnsRec,
      canResize:false,
      scroll: { y: 410 },
      actionColumn: {
        width: 120,
        fixed: 'right',
      },
      formConfig: {
        schemas: searchFormSchema,
        showAdvancedButton: false,
        labelWidth: '100px'
      },
      beforeFetch: async (params) => {
        params.deptCode = userStore?.userInfo?.orgCode;
        params.type = 2;
        params.column = 'exceptionDate';
        params.order = 'desc';
        return Object.assign(params, queryParam);
      },
    },
    exportConfig: {
      name: "服务协议异常记录表",
      url: getExportUrl,
      params: Object.assign({deptCode:userStore?.userInfo?.orgCode,type:2}, queryParam),
    },
  });
  const [registerRecevieTable, { reload:reloadRec, collapseAll:collapseAllRec, updateTableDataRecord:updateTableDataRecordRec, findTableDataRecord:findTableDataRecordRec, getDataSource :getDataSourceRec},
    { rowSelection:rowSelectionRec, selectedRowKeys:selectedRowKeysRec }] = tableRecContext;


  //新增/编辑modal
  const [registerAddModal, { openModal }] = useModal();


  //新增/编辑modal
  const [registerFiveWhyModal, { openModal:openFiveWhyModel }] = useModal();

  //查看文件
  const [registerFilesModal, { openModal:openFilesModel }] = useModal();

  //确认状态弹窗
  const [registerConfirmStatusModal, { openModal:openConfirmStatusModal }] = useModal();

  function showDetail(record){
    openFilesModel(true, {'isAdd': false, 'isDetail': true, 'record': record})
  }

  function handleConfirmStatus(record){
    openConfirmStatusModal(true, {'isAdd': false, 'isDetail': false, 'record': record})
  }

  /**
   * 新增事件
   */
  function handleAddRec() {
    openModal(true, {'isAdd': true, 'isDetail': false})
  }
  
  /**
   * 编辑事件
   */
  function handleEdit(record: Recordable) {
    openModal(true, {'isAdd': false, 'isDetail': false, 'record': record})
  }
   
  /**
   * 详情
   */
  function handleDetail(record: Recordable) {
    openModal(true, {'isAdd': false, 'isDetail': true, 'record': record})
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

  function fiveWhy(record){
    //fiveWhyView(true, {'record': record})
    console.log(record)
     // nextTick(() => {
        openFiveWhyModel(true, {'isAdd': false, 'isDetail': true, 'record': record})
     // });
    }


  /**
   * 成功回调
   */
  function handleSuccess() {
    selectedRowKeys.value = [];
    selectedRowKeysRec.value = [];
    reload();
    reloadRec();
  }

  /**
   * 操作栏
   */
  function getTableActionRec(record) {
    return [
      {
        label: '编辑',
        onClick: handleEdit.bind(null, record),
        auth: 'serviceAgreement:mes3_service_agreement_exception:edit'
      },
      {
        label: '删除',
        disabled: record.createBy !== userStore?.userInfo?.username,
        popConfirm: {
          title: '是否确认删除',
          confirm: handleDelete.bind(null, record),
          placement: 'topLeft',
        },
        auth: 'serviceAgreement:mes3_service_agreement_exception:delete'
      }
    ];
  }

  /**
   * 删除事件
   */
  async function handleCheck(record) {
    console.log(record)
    await check({id: record.id}, handleSuccess);
  }

  const handleConfirm = (record) => {
    console.log('用户确认操作');
    // 执行实际业务逻辑
    handleCheck(record);
  };

  const handleCancel = () => {
    console.log('用户取消操作');
    // 可选的取消处理
  };

  /**
   * 重置
   */
  function searchReset() {
    formRef.value.resetFields();
    selectedRowKeys.value = [];
    //刷新数据
    reload();
  }

</script>

<style lang="less" scoped>

</style>
