<template>
  <a-spin :spinning="confirmLoading">
    <div class="p-2">
      <!--查询区域-->
      <div class="jeecg-basic-table-form-container">
        <a-form ref="formRef" @keyup.enter.native="searchQuery" :model="queryParam" :label-col="labelCol" :wrapper-col="wrapperCol">
          <a-row :gutter="24">
            <!--部门-->
            <a-col :lg="6">
              <a-form-item label="部门">
                <kt-depart-select
                  v-model:value="queryParam.deptQuery"
                  select-type="depart"
                  placeholder="请选择部门"
                  @change="handleDeptChange"
                />
              </a-form-item>
            </a-col>
            <!--产线-->
            <a-col :lg="6">
              <a-form-item label="产线">
                <kt-depart-select
                  v-model:value="queryParam.lineCode"
                  select-type="line"
                  placeholder="请选择产线"
                  :parentKey="queryParam.deptQuery"
                />
              </a-form-item>
            </a-col>

            <!--SOP类型-->
            <a-col :span="6">
              <a-form-item label="SOP类型">
                <a-select v-model:value="queryParam.sopType"
                  style="width: 100%" 
                  placeholder="请选择SOP类型" 
                  :options="sopTypeOptions"
                  :fieldNames="{ label: 'itemKey', value: 'itemVal' }"
                  allowClear

                />
              </a-form-item>
            </a-col>
            <!--SOP名称-->
            <a-col :span="6">
                <a-form-item label="SOP名称">
                  <a-input placeholder="请输入SOP名称" v-model:value="queryParam.sopDesc" />
                </a-form-item>
            </a-col>
            <!--是否需要诊断-->
            <a-col :span="6">
              <a-form-item label="是否需要诊断">
                <a-select v-model:value="queryParam.diagnosis"
                  style="width: 100%" 
                  placeholder="请选择是否需要诊断" 
                  :options="diagnosisOptions"
                  allowClear
                />
              </a-form-item>
            </a-col>

            <a-col :xl="4" :lg="4" :md="8" :sm="24">
              <span style="float: left; overflow: hidden; margin-left: -12px" class="table-page-search-submitButtons">
                <a-col :lg="6">
                  <!--查询-->
                  <a-button type="primary" preIcon="ant-design:search-outlined" @click="searchQuery">{{ t('language.search') }}</a-button>
                </a-col>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>
      
      <a-card :bordered="false">
        <!-- 操作按钮区域 -->
        <div class="table-operator">
          <a-button type="primary" v-auth="'sop:mes3_sop_home_page:add'"  @click="handleAdd" preIcon="ant-design:plus-outlined">新增</a-button>
          <a-button type="primary" v-auth="'sop:mes3_sop_home_page:add'"  @click="submitForm" preIcon="ant-design:plus-outlined">保存</a-button>
          <a-button  type="primary" v-auth="'sop:mes3_sop_home_page:exportXls'" preIcon="ant-design:export-outlined" @click="onExportXls">导出</a-button>
          <j-upload-button  type="primary" v-auth="'sop:mes3_sop_home_page:importExcel'"  preIcon="ant-design:import-outlined" @click="onImportXls">导入</j-upload-button>
          <a-dropdown v-if="selectedRowKeys.length > 0">
            <template #overlay>
              <a-menu>
                <a-menu-item key="1" @click="batchHandleDelete">
                  <Icon icon="ant-design:delete-outlined"></Icon>
                  删除
                </a-menu-item>
              </a-menu>
            </template>
            <a-button v-auth="'sop:mes3_sop_home_page:deleteBatch'">
              批量操作
              <Icon icon="mdi:chevron-down"></Icon>
            </a-button>
          </a-dropdown>
        </div>
        <!-- table区域-begin -->
        <div>
          <!-- <a-table ref="table" size="middle" :scroll="{ x: false, y: scrollY }" bordered rowKey="id" :columns="columns"
            :customRow="clickThenCheck" :dataSource="dataSource" :pagination="ipagination" :loading="loading"
            :rowSelection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }" class="j-table-force-nowrap"
            @change="handleTableChange"> -->
          <a-table ref="table" size="middle" :scroll="{ x: false, y: scrollY }" bordered rowKey="id" :columns="columns"
            :customRow="clickThenCheck" :dataSource="dataSource" :pagination="ipagination" :loading="loading"
            @dblclick ="rowDbClick"
            :rowSelection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }" @change="handleTableChange">

            <!--插槽-->
            <template #sortSlots="{ record,column }">
              <span v-if="showSlotRow == record.id">
                <a-input-number style="width: 100%"  v-model:value="record[column.dataIndex]" ></a-input-number>
              </span>
              <span v-else>{{record[column.dataIndex] }}</span>
            </template>

            <template #deptSlots="{ record,column }">
              <span v-if="showSlotRow == record.id">
                <kt-depart-select v-model:value="record[column.dataIndex]" select-type="depart" placeholder="请选择部门"/>
              </span>
              <span v-else>{{ getOptionText(record[column.dataIndex], 'dept') }}</span>
            </template>

            <template #lineSlots="{ record,column }">
              <span v-if="showSlotRow == record.id">
                <kt-depart-select v-model:value="record[column.dataIndex]" select-type="line" placeholder="请选择产线" :parentKey="record['deptCode']"/>
              </span>
              <span v-else>{{ getOptionText(record[column.dataIndex], 'dept') }}</span>
            </template>

            <template #sopTypeSlots="{ record,column }">
              <span v-if="showSlotRow == record.id">
                <a-select v-model:value="record[column.dataIndex]"
                  style="width: 100%" 
                  placeholder="请选择SOP类型" 
                  :options="sopTypeOptions"
                  :fieldNames="{ label: 'itemKey', value: 'itemVal' }"
                />
              </span>
              <span v-else>{{ getOptionText(record[column.dataIndex], 'MES3_SOP_TYPE') }}</span>
            </template>

            <template #inputSlots="{ record,column }">
              <span v-if="showSlotRow == record.id">
                <span v-if="column.dataIndex ==='sopDesc'">
                  <a-input v-model:value="record[column.dataIndex]" placeholder="请输入"  allow-clear style="width:90%"></a-input>
                </span>
                <span v-else>
                  <a-input v-model:value="record[column.dataIndex]" placeholder="请输入"  allow-clear style="width:100%"></a-input>
                </span>
              </span>
              <span v-else>
                {{record[column.dataIndex] }}
                <span v-if="column.dataIndex ==='sopDesc'">
                  <a-icon type="file" class="element" style="font-size: 1rem;padding-left:5px;" @click="popUp(record, column.dataIndex,2)" />
                </span>
              </span>
            </template>



            <!-- 编写人 -->
            <template #putIntoNamesSlots="{ record,column }">
              <span>
                <a @click="popUp(record, column.dataIndex,1)">{{ record[column.dataIndex]?record[column.dataIndex]:"无" }}</a>
              </span>
            </template>

            <!--历史版本查询-->
            <template #oldListSlots="{ record,column }">
              <span>
                <a @click="popUp(record, column.dataIndex,2)">{{ record[column.dataIndex] }}</a>
              </span>
            </template>

            <!-- OWD计划 -->
            <template #planUrlSlots="{ record,column }">
              <span>
                <a @click="showPlan(column.dataIndex)">{{ record[column.dataIndex] }}</a>
              </span>
            </template>
            <!-- 回顾计划 -->
            <template #reviewPlanUrlSlots="{ record,column }">
              <span>
                <a @click="showPlan(column.dataIndex)">{{ record[column.dataIndex] }}</a>
              </span>
            </template>

            <template #diagnosisSlots="{ record,column }">
              <span v-if="showSlotRow == record.id">
                <a-select v-model:value="record[column.dataIndex]"
                  style="width: 100%" 
                  placeholder="请选择是否需要诊断" 
                  :options="diagnosisOptions"
                />
              </span>
              <span v-else>{{ getOptionText(record[column.dataIndex], 'diagnosis') }}</span>
            </template>
          </a-table>
        </div>

      </a-card>

      <!-- 表单区域 -->
      <SopHomePageModal ref="registerModal" @success="handleSuccess"></SopHomePageModal>
      <SopUrlReviewModal ref="sopUrlReviewModal" @success="handleSuccess"></SopUrlReviewModal>
      <SopResponsibilityModal ref="responsibilityModal" @success="handleSuccess"></SopResponsibilityModal>
    </div>
  </a-spin>
</template>

<script lang="ts" name="sop-sopHomePage" setup>
  import { ref, reactive,onMounted } from 'vue';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { columns } from './SopHomePage.data';
  import { router } from '/@/router';
  import { list, deleteOne, batchDelete, getImportUrl, getExportUrl,saveOrUpdate } from './SopHomePage.api';
  import SopHomePageModal from './components/SopHomePageModal.vue'
  import { useUserStore } from '/@/store/modules/user';
  import { useI18n } from '/@/hooks/web/useI18n';
  import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import SopUrlReviewModal from '/@/views/dataCalculation/sop/review/components/sopUrlReviewModal.vue';
  import SopResponsibilityModal from './components/SopResponsibilityModal.vue';
  import { dicList } from '/@/views/dataCalculation/mdm/dict/dict.api';
  import { buildUUID } from '/@/utils/uuid';
  import { departListAll} from "/@/api/common/api"
  //可编辑的行
  const showSlotRow = ref('');
  //已编辑的行数据
  const editIds = ref({});
  const { createMessage } = useMessage();
  const { t } = useI18n();
  const selectedRowKeys = ref([])
  const formRef = ref();
  const scrollY = ref(400);
  const queryParam = reactive<any>({});
  const registerModal = ref();
  const sopUrlReviewModal = ref();
  const responsibilityModal = ref();
  const userStore = useUserStore();
  const dataSource = ref([] as any);
  const loading = ref(false);
  const confirmLoading = ref(false);
  const ipagination = ref({
    current: 1,
    pageSize: 10,
    total: dataSource.value.length,
    showSizeChanger: true,
    showQuickJumper: true,
    showTotal: (total) => `共 ${total} 条数据`,
  });
  //sop类型下拉数据
  const sopTypeOptions = ref([]);
  //部门数据
  const deptOptions = ref([]);
  //是否需要诊断
  const diagnosisOptions = [
    { value: 1, label: '是' },
    { value: 0, label: '否' },
  ]

  const labelCol = reactive({
    xs:24,
    sm:4,
    xl:6,
    xxl:5
  });
  const wrapperCol = reactive({
    xs: 24,
    sm: 20,
  });

  const {onExportXls, onImportXls } = useListPage({
    tableProps: {
      title: 'KPI清单',
    },
    exportConfig: {
      name: "SOP清单",
      url: getExportUrl,
      params: queryParam,
    },
	  importConfig: {
	    url: getImportUrl,
	    success: handleSuccess
	  },
  });

  
  /**
   * 新增事件
   */
  function handleAdd() {
    // registerModal.value.disableSubmit = false;
    // registerModal.value.add();
    let id = 'temp_' + buildUUID();
    let columnsCopy = deepCloneByJSON(columns);
    let dataMap = {id} as any;
    columnsCopy.forEach((item) => {
      dataMap[item['dataIndex']] = ""
      if(item['dataIndex'] == 'oldList') dataMap[item['dataIndex']] = "list"
    })
    dataSource.value.unshift(dataMap);
    editIds.value[id] = true;
    showSlotRow.value = id;
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
    (selectedRowKeys.value = []) && loadData(1);
  }
   
  /**
   * 操作栏
   */
  function getTableAction(record) {
    return [
      {
        label: '编辑',
        onClick: handleEdit.bind(null, record),
        auth: 'sop:mes3_sop_home_page:edit'
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
        auth: 'sop:mes3_sop_home_page:delete'
      }
    ]
  }

  /**
   * 查询
   */
  function searchQuery() {
    loadData(1);
  }
  
  /**
   * 重置
   */
  function searchReset() {
    formRef.value.resetFields();
    selectedRowKeys.value = [];
  }

  //部门改变事件
  async function handleDeptChange(value){
    //清空产线
    queryParam.value.lineCode = "";
  }


  function clickThenCheck(record){
    return {
      on: {
        click: () => {
          alert()
          emit('rowClick', record)//触发transfer方法，record 为向父组件传递的数据
        }
      },
      onDblclick: () => rowDbClick(record)
    }
  }

  //深度拷贝
  const deepCloneByJSON = (obj) => {
    return JSON.parse(JSON.stringify(obj));
  }


  
  /**
   * 提交数据
   */
  async function submitForm() {
    let submitData = dataSource.value.filter(item => editIds.value.hasOwnProperty(item.id));
    if(submitData && submitData.length == 0){
      createMessage.warning("保存记录为空");
      return false;
    }
    let isSave = true;
    //验证数据
    submitData.forEach((item) => {
      item.factoryCode = userStore.factoryCode;
      //item.deptCode = userStore.departCode;
      if(!item.sopDesc){
        isSave = false;
      }
    });
    if(!isSave){
      createMessage.warning("SOP名称不能为空");
      return false;
    }
    confirmLoading.value = true;
    await saveOrUpdate(submitData)
      .then((res) => {
        if (res.success) {
          createMessage.success(res.message);
          loadData(1)
        } else {
          createMessage.warning(res.message);
        }
      })
      .finally(() => {
        confirmLoading.value = false;
      });
  }


  //获取数据
  async function loadData(arg){
    if (arg === 1) {
      ipagination.value.current = 1;
    }
    var params = queryParam;//查询条件
    loading.value = true;
    // let deptStr = "";
    // //将查询条件中的部门集合转换成字符串用逗号分隔
    // if(params.deptQuery){
    //   params.deptQuery.forEach(ele => {
    //     if(deptStr != ""){
    //       deptStr = deptStr + "," +ele;
    //     }else{
    //       deptStr = ele;
    //     }
    //   })
    // }

    // params.deptQuery = params.deptCode;
    //设置排序字段和排序顺序
    params.orderField = 'sort';
    params.orderType = 'asc';
    // Object.assign(params,ipagination.value);
    params.pageNo = ipagination.value.current;
    params.pageSize = ipagination.value.pageSize;
    await list(params)
    .then((res) => {
      if (res.success) {
        restEdit();
        
        let data = res.result.records || res.result;
        data.forEach((item) => {
          item.sopTitle = item.sopDescDto && item.sopDescDto.sopTitle ? item.sopDescDto.sopTitle : ""
          item.writeUrl = '链接'
          item.planUrl = '链接'
          item.oldList = 'list'
          item.reviewPlanUrl = '链接'
          //新需求，字段从回顾计划中获取
          //是否为关键SOP 1是0否
          let sopStateName = "";
          sopStateName = item.sopReviewPlan && item.sopReviewPlan.sopState ? item.sopReviewPlan.sopState : ""
          item.sopStateName = sopStateName == "1" ? '是' : sopStateName == "0" ? "否" : ""
          //回顾频次
          item.reviewFrequencyName = item.sopReviewPlan && item.sopReviewPlan.frequencyName ? item.sopReviewPlan.frequencyName : ""
          //最新版版本号
          item.versionNumber = item.sopReviewPlan && item.sopReviewPlan.latestVersionNumber ? item.sopReviewPlan.latestVersionNumber : ""
          //实施时间
          item.putIntoTime = item.sopReviewPlan && item.sopReviewPlan.operateTime ? item.sopReviewPlan.operateTime : ""
          //OWD频次
          item.owdFrequencyName = item.statisticData && item.statisticData.checkIntervalName ? item.statisticData.checkIntervalName : ""
        })

        dataSource.value = data;
        if (res.result.total) {
          ipagination.value.total = res.result.total;
        } else {
          ipagination.value.total = 0;
        }
      } else {
        createMessage.warning(res.message);
      }
    })
    .finally(() => {
      loading.value = false;
    });
  }
  //
  function handleTableChange(pagination, filters, sorter) {
    //TODO 筛选
    ipagination.value.pageSize = pagination.pageSize;
    ipagination.value.current = pagination.current;
    loadData(pagination.current);
  }

  function onSelectChange(rowKeys) {
    console.log('selectedRowKeys changed: ', rowKeys);
    selectedRowKeys.value = rowKeys;
  }

  //名称点击
  function showNewFile(record, name){
    console.log(name)
  }

  //弹出
  function popUp(record, dataIndex, type){
    if(type === 1){
      //编写人
      responsibilityModal.value.disableSubmit = false;
      responsibilityModal.value.edit(record);
    }else if(type === 2){
      //历史版本
      let data = {
        sopId: record.id,
        addType: "show"
      }
      sopUrlReviewModal.value.disableSubmit = false;
      sopUrlReviewModal.value.edit(data);
    }
  }


  //查看计划/回顾
  function showPlan(val){
    if(val === "planUrl"){
      //计划
      router.push({ path: "/dataCalculation/sop/plan/sopStatisticDataList"})
    }else{
      //回顾
      router.push({ path: "/dataCalculation/sop/review/sopReviewPlanList"})
    }
  }

  //表格双击事件
  function rowDbClick(record){
    showSlotRow.value = record.id;
    editIds.value[record.id] = true;
  }

  function restEdit(){
    showSlotRow.value = '';
    editIds.value = {};
  }
  /**
   * 获取下拉选项显示文本
   */
  function getOptionText(value, type){
    let optionArr = [] as any;
    let key = 'value';
    let label = 'label';
    switch (type) {
      case 'MES3_SOP_TYPE':
        optionArr = sopTypeOptions.value;
        key = 'itemVal';
        label = 'itemKey';
        break;
      case 'dept':
        optionArr = deptOptions.value;
        key = 'orgCode';
        label = 'departName';
        break;
      case 'diagnosis':
        optionArr = diagnosisOptions;
        break;
    }
    const findObj = optionArr.find(item => item[key] == value);
    return findObj ? findObj[label] : '';
  }

  onMounted(async () => {
    loading.value = true;
    sopTypeOptions.value = await dicList({code: "MES3_SOP_TYPE"});
    deptOptions.value = await departListAll({});
    scrollY.value = document.body.scrollHeight - 420+"px";
    loadData(1)
    
  })
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

  .table-operator{
    margin-bottom: 5px;
  }
  .table-operator .ant-btn {
      margin: 0 8px 8px 0;
  }
  .element {
    cursor: pointer; /* 鼠标悬停时显示手形 */
  }
</style>
