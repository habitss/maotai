<template>
  <a-spin :spinning="confirmLoading" class="span-box">
    <div>
      <div class="jeecg-basic-table-form-container">
        <!--查询区域-->
        <a-form @keyup.enter.native="searchQuery" :model="queryParam" :label-col="labelCol" :wrapper-col="wrapperCol" labelAlign="left">
          <a-row :gutter="24">
            <a-col :lg="8">
              <a-form-item label="报表">
                <a-select v-model:value="queryParam.allocationId" :options="tableArr" @change="changeTable" placeholder="请选择" allowClear="true"></a-select>
              </a-form-item>
            </a-col>

			<!--PI名称-->
			<a-col :lg="8">
			  <a-form-item label="PI名称">
				  <a-select v-model:value="queryParam.fields" :options="fieldsArr"  placeholder="请选择" allowClear="true"></a-select>
			  </a-form-item>
			</a-col>

            <a-col :lg="8">
              <a-form-item label="审批状态">
                <kt-dict
                  v-model:value="queryParam.state"
                  placeholder="请选择点位类型"
                  dict="MES3_PI_APPROVE_STATE"
                />
              </a-form-item>
            </a-col>
			
            <a-col :xl="6" :lg="7" :md="8" :sm="24">
              <span style="float: left; overflow: hidden; margin-left: -12px" class="table-page-search-submitButtons">
                <a-col :lg="6">
                  <a-button type="primary" preIcon="ant-design:search-outlined" @click="searchQuery">{{ t('language.search') }}</a-button>
                </a-col>
              </span>
            </a-col>
          </a-row>
        </a-form>

        <!--引用表格-->
        <BasicTable @register="registerTable" :rowSelection="rowSelection">
          <!--插槽:table标题-->
          <template #tableTitle>
            <a-button type="primary" v-auth="'pi:mes3_pi_approve:add'"  @click="handleAdd" preIcon="ant-design:plus-outlined"> 新增</a-button>
            <a-button  type="primary" v-auth="'pi:mes3_pi_approve:exportXls'" preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
            <j-upload-button  type="primary" v-auth="'pi:mes3_pi_approve:importExcel'"  preIcon="ant-design:import-outlined" @click="onImportXls">导入</j-upload-button>
            <a-dropdown v-if="selectedRowKeys.length > 0">
              <template #overlay>
                <a-menu>
                  <a-menu-item key="1" @click="batchHandleDelete">
                    <Icon icon="ant-design:delete-outlined"></Icon>
                    删除
                  </a-menu-item>
                </a-menu>
              </template>
              <a-button v-auth="'pi:mes3_pi_approve:deleteBatch'">批量操作
                <Icon icon="mdi:chevron-down"></Icon>
              </a-button>
            </a-dropdown>
          </template>
          <!--操作栏-->
          <template #action="{ record }">
            <!-- <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)"/> -->
            <TableAction :actions="getTableAction(record)"/>
          </template>
          <template v-slot:bodyCell="{ column, record, index, text }">
          </template>
        </BasicTable>
      </div>
    </div>
  </a-spin>
  <!-- 表单区域 -->
  <PiApproveModal ref="registerModal" @success="handleSuccess"></PiApproveModal>
</template>

<script lang="ts" name="pi-piApprove" setup>
  import { ref, reactive,onMounted } from 'vue';
  import {allocationTableList,tableFieldsList} from '../allocationTable/QueryTable.api';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { columns,modifyColumns, removeColumns } from './Approve.data';
  import { list, deleteOne, batchDelete, getImportUrl, getExportUrl,approve } from './Approve.api';
  import KtDict from '/@/components/KtDict/KtDict.vue';
  import PiApproveModal from './components/ApproveModal.vue'
  import { useUserStore } from '/@/store/modules/user';
  import { useRoute } from 'vue-router';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  const formRef = ref();
  const queryParam = reactive<any>({});
  const registerModal = ref();
  const { createMessage } = useMessage();
  const confirmLoading = ref<boolean>(false);
  //获取路由
  const route = useRoute();
   //报表集合
  const tableArr = reactive<Record<string, any>>([]);
  //字段集合
  const fieldsArr = reactive<Record<string, any>>([]);
  //获取路由中的type参数
  const type = route.params.appType;
  //注册table数据
  const { tableContext, onExportXls, onImportXls } = useListPage({
    tableProps: {
      title: 'pi报表-报表审批',
      api: list,
      columns: columns,
      canResize:false,
      useSearchForm: false,
      beforeFetch: async (params) => {
        return Object.assign(params, queryParam);
      },
    },
    exportConfig: {
      name: "pi报表-报表审批",
      url: getExportUrl,
      params: queryParam,
    },
	  importConfig: {
	    url: getImportUrl,
	    success: handleSuccess
	  },
  });
  const [registerTable, { reload,setProps }, { rowSelection, selectedRowKeys }] = tableContext;
  const labelCol = reactive({
    xs:24,
    sm:4,
    xl:6,
    xxl:4
  });
  const wrapperCol = reactive({
    xs: 24,
    sm: 20,
  });

  /**
   * 新增事件
   */
  function handleAdd() {
    registerModal.value.disableSubmit = false;
    registerModal.value.add();
  }
   
  /**
   * 详情
   */
  function handleDetail(record: Recordable) {
    registerModal.value.disableSubmit = true;
    registerModal.value.edit(record);
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
    let tableDataArr = [];
    let tableData = {};
    if(record.state == '0'){
      tableData = {
        label: '审批',
        popConfirm: {
          title: '是否同意',
          okText: "同意",
          cancelText: "不同意",
          confirm: agree.bind(null, record),
          cancel: disagree.bind(null, record),
          placement: 'topLeft',
        },
        // auth: 'pi:mes3_pi_approve:approve'
      }
    }
    tableDataArr.push(tableData);
    return tableDataArr;
  }
  //同意
  function agree(record){
    approveFun(1,record)
  }
  //不同意
  function disagree(record){
    approveFun(2,record)
  }

  //审批
  async function approveFun(state,record){
    confirmLoading.value = true;
    let obj = {
      "id": record.id,
      "state": state,
      "type": type,
      "dataId": record.dataId,
      "fields": record.fields,
      "tableName":record.tableName,
      "modifyValue": record.modifyValue
    }
    await approve(obj)
      .then((res) => {
        if (res.success) {
          const successMessage = res.message ? res.message : t('language.save');
          createMessage.success(successMessage);
          handleSuccess();
        } else {
            createMessage.warning(res.message);
        }
      })
      .finally(() => {
        confirmLoading.value = false;
    });

  }

  /**
   * 下拉操作栏
   */
  function getDropDownAction(record) {
    return [
      {
        label: '详情',
        onClick: handleDetail.bind(null, record),
      },
      // {
      //   label: '删除',
      //   popConfirm: {
      //     title: '是否确认删除',
      //     confirm: handleDelete.bind(null, record),
      //     placement: 'topLeft',
      //   },
      //   auth: 'pi:mes3_pi_approve:delete'
      // }
    ]
  }

  /**
   * 查询
   */
  function searchQuery() {
    reload();
  }
  
  /**
   * 重置
   */
  function searchReset() {
    formRef.value.resetFields();
    selectedRowKeys.value = [];
    //刷新数据
    reload();
  }
  
  
   /**
   * 初始化报表下拉数据
   */
  async function getTableArr() {
    //pi报表
    allocationTableList({ pageNo: 1, pageSize: 999 }).then((res) => {
      if (res && res.records && res.records.length > 0) {
        res.records.map((item) => {
          tableArr.push({label: item.reportName, value: item.id, tableId: item.tableId});
        });
      }
    });
  }

  onMounted(() => {
    queryParam.type = type;
    getTableArr();
    if(type == '0'){
      setProps({columns: modifyColumns});
    }else{
      setProps({columns: removeColumns});
    }
  });

  //报表改变
  async function changeTable(){
	  //console.log(e)
    // 清空fieldsArr数组
    fieldsArr.length = 0;
	queryParam.fields = "";
    // 重新获取字段下拉数据
    getFieldsArr(queryParam.allocationId);
  }
  
  /**
   * 初始化字段下拉数据
   */
  async function getFieldsArr(allocationTableId) {
    //pi报表
    tableFieldsList({ pageNo: 1, pageSize: 999, allocationTableId: allocationTableId }).then((res) => {
      if (res && res.records && res.records.length > 0) {
        res.records.map((item) => {
          fieldsArr.push({label: item.name, value: item.fieldName, fieldName: item.fieldName});
        });
      }
    });
  }

</script>

<style lang="less" scoped>
  .jeecg-basic-table-form-container {
    padding: 0;
    .table-page-search-submitButtons {
      display: block;
      margin-bottom: 24px;
      white-space: nowrap;
    }
    .query-group-cust{
      min-width: 100px !important;
    }
    .query-group-split-cust{
      width: 30px;
      display: inline-block;
      text-align: center
    }
    .ant-form-item:not(.ant-form-item-with-help){
      margin-bottom: 16px;
      height: 32px;
    }
    :deep(.ant-picker),:deep(.ant-input-number){
      width: 100%;
    }
  }
</style>
