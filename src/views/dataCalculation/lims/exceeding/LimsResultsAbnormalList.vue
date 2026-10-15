<template>
  <a-spin :spinning="confirmLoading" class="span-box">
    <div class="p-2">
      <!--查询区域-->
      <div class="jeecg-basic-table-form-container">
        <a-form ref="formRef" @keyup.enter.native="searchQuery" :model="queryParam" :label-col="labelCol" :wrapper-col="wrapperCol">
          <a-row :gutter="24">
             <!--报告日期-->
            <a-col :lg="6">
              <a-form-item label="推送时间">
                <a-range-picker class="date-pick" :ranges="{}" 
                format="YYYY-MM-DD HH:mm:ss" valueFormat="YYYY-MM-DD HH:mm:ss" 
                v-model:value="queryParam.timeRange" 
                @change="onChangeDate" 
                :show-time="{
                  hideDisabledOptions: true,
                  defaultValue: [dayjs('00:00:00', 'HH:mm:ss'), dayjs('23:59:59', 'HH:mm:ss')],
                }"
                style="width: 215px;"/>
              </a-form-item>
            </a-col>
            <!--审核状态-->
            <a-col :lg="6">
              <a-form-item label="审核状态">
                <a-select v-model:value="queryParam.examineStatus" style="width: 215px" allowClear placeholder="请选择审核状态">
                  <a-select-option :value="item.id" v-for="(item) in approvalArr" :key="item.id">
                        {{ item.name }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <!--完成状态-->
            <a-col :lg="6">
              <a-form-item label="完成状态">
                <a-select v-model:value="queryParam.completeStatus" style="width: 215px" allowClear placeholder="请选择完成状态">
                  <a-select-option :value="item.id" v-for="(item) in completeArr" :key="item.id">
                      {{ item.name }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <!--5why分析-->
            <a-col :lg="6">
              <a-form-item label="5why分析">
                <a-select v-model:value="queryParam.fiveWhyStatus" style="width: 215px" allowClear placeholder="请选择是否5why分析">
                  <a-select-option :value="item.id" v-for="(item) in isArr" :key="item.id">
                      {{ item.name }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>

            <!--记录行动-->
            <a-col :lg="6">
              <a-form-item label="记录行动">
                <a-select v-model:value="queryParam.actionStatus" style="width: 215px" allowClear placeholder="请选择是否记录行动">
                  <a-select-option :value="item.id" v-for="(item) in isArr" :key="item.id">
                      {{ item.name }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <!--是否作废-->
            <a-col :lg="6">
              <a-form-item label="是否作废">
                <a-select v-model:value="queryParam.cancelFlag" style="width: 215px" allowClear placeholder="请选择是否作废">
                  <a-select-option :value="item.id" v-for="(item) in cancelFlagArr" :key="item.id">
                      {{ item.name }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <!--LIMS流水号-->
            <a-col :lg="6">
              <a-form-item label="流水号">
                <a-input placeholder="请输入流水号" v-model:value="queryParam.limsFlowId" allowClear style="width: 215px"/>
              </a-form-item>
            </a-col>
            <template v-if="toggleSearchStatus">
              <!-- 部门 -->
              <a-col :lg="6">
                <a-form-item label="责任部门">
                  <a-input placeholder="请输入责任部门" v-model:value="queryParam.limsDept" allowClear style="width: 215px"/>
                </a-form-item>
              </a-col>
              <!-- 产线 -->
              <a-col :lg="6">
                <a-form-item label="责任生产线	">
                  <a-input placeholder="请输入责任生产线" v-model:value="queryParam.deviceName" allowClear style="width: 215px"/>
                </a-form-item>
              </a-col>
              <!-- 批号 -->
              <a-col :lg="6">
                <a-form-item label="批号">
                  <a-input placeholder="请输入批号" v-model:value="queryParam.batchNo" allowClear style="width: 215px"/>
                </a-form-item>
              </a-col>
              <!-- 检验类别 -->
              <a-col :lg="6">
                <a-form-item label="检验类别">
                  <a-input placeholder="请输入检验类别" v-model:value="queryParam.checkCategory" allowClear style="width: 215px"/>
                </a-form-item>
              </a-col>

            </template>
           

            <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <span style="float: left; overflow: hidden; margin-left: -12px" class="table-page-search-submitButtons">
              <a-col :lg="6">
                <a-button type="primary" preIcon="ant-design:search-outlined" @click="searchQuery">{{ t('language.search') }}</a-button>
                <a-button type="primary" preIcon="ant-design:reload-outlined" @click="searchReset" style="margin-left: 8px">{{
                  t('language.reset')
                }}</a-button>
                <a @click="toggleSearchStatus = !toggleSearchStatus" style="margin-left: 8px">
                  {{ toggleSearchStatus ? t('language.collapse') : t('language.open') }}
                  <Icon :icon="toggleSearchStatus ? 'ant-design:up-outlined' : 'ant-design:down-outlined'" />
                </a>
              </a-col>
            </span>
          </a-col>

          </a-row>
        </a-form>
      </div>
      <!--引用表格-->
      <BasicTable @register="registerTable">
        <!--插槽:table标题-->
        <template #tableTitle>
          <a-button type="primary" v-auth="'lims:mes3_lims_results_abnormal:add'"  @click="handleAdd" preIcon="ant-design:plus-outlined"> 新增</a-button>
          <a-button  type="primary" v-auth="'lims:mes3_lims_results_abnormal:exportXls'" preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
          <j-upload-button  type="primary" v-auth="'lims:mes3_lims_results_abnormal:importExcel'"  preIcon="ant-design:import-outlined" @click="onImportXls">导入</j-upload-button>
          <a-dropdown v-if="selectedRowKeys.length > 0">
            <template #overlay>
              <a-menu>
                <a-menu-item key="1" @click="batchHandleDelete">
                  <Icon icon="ant-design:delete-outlined"></Icon>
                  删除
                </a-menu-item>
              </a-menu>
            </template>
            <a-button v-auth="'lims:mes3_lims_results_abnormal:deleteBatch'">批量操作
              <Icon icon="mdi:chevron-down"></Icon>
            </a-button>
          </a-dropdown>
        </template>
        <template v-slot:bodyCell="{ column, record, index, text }">
        </template>

        <!--是否作废-->
        <template #cancelFlag="{ record }">
          <span v-if="record.cancelFlag == '1'" style="color:red">作废</span>
          <span v-else style="color:#52c41a">正常</span>
        </template>

        <!--是否填写根本原因-->
        <template #isReason="{ record }">
          <span class="pointer">
            <a-tag v-if="record.reason" color="green" @click="open5WhyModal(record,'reason')">已填写</a-tag>
            <a-tag v-else color="red" @click="open5WhyModal(record,'reason')">未填写</a-tag>
          </span>
        </template>
		
		<!--是否审批根本原因-->
		<template #reasonStatus="{ record }">
		  <span class="pointer">
		    <a-tag v-if="record.reasonStatus == '1'" color="green">通过</a-tag>
			<a-tag v-else-if="record.reasonStatus == '0' && !record.reason">未审核</a-tag>
			<a-tag v-else-if="record.reasonStatus == '0' && record.reason"><a-popconfirm title="请审核" ok-text="通过" cancel-text="不通过" @confirm="rootCauseApproval(record,'1')" @cancel="rootCauseApproval(record,'2')">未审核</a-popconfirm></a-tag>
			<a-tag v-else color="red">不通过</a-tag>
		  </span>
		</template>

        <!--是否5why分析-->
        <template #fiveWhyStatus="{ record }">
          <span class="pointer">
            <a-tag v-if="record.fiveWhyStatus == '1'" color="green" @click="open5WhyModal(record,'')">已分析</a-tag>
            <a-tag v-else-if="record.reasonStatus != '1'" color="red"><a-tooltip title="请先完成根本原因填写及审核">未分析</a-tooltip></a-tag>
            <a-tag v-else color="red" @click="open5WhyModal(record,'')">未分析</a-tag>
          </span>
        </template>
        
        <!--是否记录行动-->
        <template #actionStatus="{ record }">
          <span class="pointer">
            <!-- 前往行动计划 -->
            <a-tag v-if="record.actionStatus == '1'" color="green" @click="openMoveModal(record)">已记录</a-tag>
            <!-- 前往5w和行动计划进行分析 -->
            <a-tag v-else-if="record.reasonStatus != '1'" color="red"><a-tooltip title="请先完成根本原因填写及审核">未记录</a-tooltip></a-tag>
            <a-tag v-else color="red"  @click="open5WhyModal(record,'')">未记录</a-tag>
          </span>
        </template>

        <!--审核状态-->
        <template #examineStatus="{ record }">
          <span class="pointer">
            <a-tag v-if="record.examineStatus == '1'" color="green">通过</a-tag>
            <a-tag v-else-if="record.examineStatus == '0' && record.actionStatus == '1'"><a-popconfirm title="请审核" ok-text="通过" cancel-text="不通过" @confirm="handleApproval(record,'1')" @cancel="handleApproval(record,'2')">未审核</a-popconfirm></a-tag>
            <a-tag v-else-if="record.examineStatus == '0' && record.actionStatus != '1'"><a-tooltip title="请先完成5why分析和记录行动">未审核</a-tooltip></a-tag>
            <a-tag v-else color="red"><a-tooltip title="审批不通过,请修改根本原因,重新提交审批">不通过</a-tooltip></a-tag>
          </span>
        </template>
        
        <!--完成状态-->
        <template #completeStatus="{ record }">
          <span class="pointer">
            <a-tag :color="getCompleteStatusColor(record)">{{ record.completeProgress }}</a-tag>
          </span>
        </template>
     
      </BasicTable>
      <!--5why分析/过程记录(记录行动)-->
      <LimsModal ref="rootCause" @success="handleSuccess"></LimsModal>
    </div>
  </a-spin>
</template>

<script lang="ts" name="lims-limsResultsAbnormal" setup>
  import { ref, reactive } from 'vue';
  import { BasicTable } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { columns } from './LimsResultsAbnormal.data';
  import { list, approval, getImportUrl, getExportUrl,causeApproval } from './LimsResultsAbnormal.api';
  //5why分析
  import LimsModal from './components/limsModal.vue'
  import { useI18n } from '/@/hooks/web/useI18n';
  //消息提示
  import { useMessage } from '/@/hooks/web/useMessage';
  import dayjs from 'dayjs';
  import GapAnalysis from '../../gapAnalysis/gapAnalysis.vue';
  const { createMessage } = useMessage();
  const { t } = useI18n();
  const formRef = ref();
  const queryParam = ref<any>({});
  const toggleSearchStatus = ref<boolean>(false);
  const registerModal = ref();
  const confirmLoading = ref<boolean>(false);
  //5why分析
  const rootCause = ref();

  const isArr =ref([
    {"id":"0","name":"否"},
    {"id":"1","name":"是"}]
  );
  //审批状态
  const approvalArr =ref([
    {"id":"0","name":"未审核"},
    {"id":"1","name":"通过"},
    {"id":"2","name":"不通过"}]
  );
  //完成状态
  const completeArr =ref([
    {"id":"0","name":"未完成"},
    {"id":"1","name":"已完成"}]
  );
  //是否作废
  const cancelFlagArr =ref([
    {"id":"0","name":"正常"},
    {"id":"1","name":"作废"}]
  );

  //注册table数据
  const { prefixCls, tableContext, onExportXls, onImportXls } = useListPage({
    tableProps: {
      title: '超标不合格台账(LIMS)',
      api: list,
      columns,
      canResize:true,
      useSearchForm: false,
      showIndexColumn: true,
      showActionColumn: false,
      actionColumn: {
        width: 120,
        fixed: 'right',
      },
      beforeFetch: async (params) => {
        return Object.assign(params, queryParam.value);
      },
    },
    exportConfig: {
      name: "超标不合格台账(LIMS)",
      url: getExportUrl,
      params: queryParam.value,
    },
	  importConfig: {
	    url: getImportUrl,
	    success: handleSuccess
	  },
  });
  const [registerTable, { reload, collapseAll, updateTableDataRecord, findTableDataRecord, getDataSource }, { selectedRowKeys }] = tableContext;
  const labelCol = reactive({
    xs:24,
    sm:4,
    xl:5,
    xxl:6
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
   * 编辑事件
   */
  function handleEdit(record: Recordable) {
    registerModal.value.disableSubmit = false;
    registerModal.value.edit(record);
  }
   
  /**
   * 详情
   */
  function handleDetail(record: Recordable) {
    registerModal.value.disableSubmit = true;
    registerModal.value.edit(record);
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
        auth: 'lims:mes3_lims_results_abnormal:edit'
      },
    ];
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
        auth: 'lims:mes3_lims_results_abnormal:delete'
      }
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
    selectedRowKeys.value = [];
    queryParam.value = {};
    //刷新数据
    reload();
  }
  /**
   * 获取完成状态颜色
   */
  function getCompleteStatusColor(record) {
    if (record.completeStatus === '1') {
      return 'green';
    } else if (record.completeProgress === '0/0') {
      return 'red';
    }
    return 'orange';
  }

  //审批通过(1)/审批不通过(2)
  async function handleApproval(record, type){
    record.examineStatus = type;
    confirmLoading.value = true;
    await approval(record)
    .then((res) => {
      if (res.success) {
        createMessage.success(res.message);
      } else {
        record.examineStatus = 0;
        createMessage.warning(res.message);
      }
    })
    .finally(() => {
      confirmLoading.value = false;
    });
  }
  //根本原因审批通过(1)/审批不通过(2)
  async function rootCauseApproval(record, type){
    record.reasonStatus = type;
    confirmLoading.value = true;
    await causeApproval(record)
    .then((res) => {
      if (res.success) {
        createMessage.success(res.message);
      } else {
        record.reasonStatus = 0;
        createMessage.warning(res.message);
      }
    })
    .finally(() => {
      confirmLoading.value = false;
    });
  }
  //5why分析
  function open5WhyModal(record, recordType) {
    let rowData = JSON.parse(JSON.stringify(record)); //当前行的数据传给差距分析弹框
    //异常分析
    let obj ={
      //数据id
      dataId: rowData.id,
      //5why描述
      problem: rowData.checkCategory+'异常',
      //根本原因
      reason: rowData.reason,
      //分析时间
      analysisTime: rowData.analysisTime,
      //审批状态
      examineStatus: rowData.examineStatus,
      //5whyid
      analysisId: rowData.analysisId,
      //类型
      type: '5',
      //根本原因
      recordType: recordType,
	  //根本原因状态
	  reasonStatus: rowData.reasonStatus,
    }
    // 审批通过,不能进行5wyh分析
    if(rowData.examineStatus == '1'){
      obj.showSaveBtn = 'hide';
    }
    //console.log(obj);
    rootCause.value.openRootCause(obj);
  }
  //进入行动分析页面
  function openMoveModal(record){
    //行动分析
    rootCause.value.openActionRecord(record);
  }
  //推送时间
  function onChangeDate(value,dates){
    queryParam.value.checkDateBegin = dates[0]
    queryParam.value.checkDateEnd = dates[1]
  }
  //推送时间空间展开
  function onOpen(){
    if(!queryParam.value.checkDateBegin){
      alert(1)
    }
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
  .pointer{
    cursor: pointer;
  }
</style>
