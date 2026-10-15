<template>
  <a-spin :spinning="confirmLoading" class="span-box">
  <div>
    <!--查询区域-->
    <div class="jeecg-basic-table-form-container">
      <a-form @keyup.enter.native="searchQuery" :model="queryParam" :label-col="labelCol" :wrapper-col="wrapperCol" labelAlign="left">
        <a-row :gutter="24">
          <a-col :lg="8">
            <a-form-item label="报表">
              <a-select v-model:value="queryParam.tableId" :options="tableArr" placeholder="请选择" @change="tableChange"  allowClear="true"></a-select>
            </a-form-item>
          </a-col>
          
          <template v-if="toggleSearchStatus">
            <a-col :lg="8" v-for="(val, index) in queryArr" :key="index">
              <a-form-item :label="val.name">
                <!--时间-->
                <a-range-picker class="date-pick" v-if="val.type=='6'" :ranges="{}" format="YYYY-MM-DD HH:mm:ss" v-model:value="rangePickerTime[val.fieldName]" @change="onChangeTime(val.fieldName)" show-time/>              
                <!--日期-->
                <a-range-picker class="date-pick" v-if="val.type=='4'" :ranges="{}" format="YYYY-MM-DD" v-model:value="rangePickerDate[val.fieldName]" @change="onChangeDate(val.fieldName)" />
                <!--输入框-->
                <a-input placeholder="请输入" v-if="val.type=='1' || val.type=='2' || val.type=='5'" v-model:value="queryParam[val.fieldName]" />
                <!--下拉框-->
                
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
    <!--引用表格-->
   <BasicTable @register="registerTable" @edit-end="handleEditEnd"  :rowSelection="rowSelection" :columns="columns">
       <!--插槽:table标题-->
      <template #tableTitle>
          <!-- <a-button  type="primary" preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button> -->
          <a-dropdown v-if="selectedRowKeys.length > 0">
              <template #overlay>
                <a-menu>
                  <a-menu-item key="1" @click="batchHandleDelete">
                    <Icon icon="ant-design:delete-outlined"></Icon>
                    删除
                  </a-menu-item>
                </a-menu>
              </template>
              <a-button>批量操作
                <Icon icon="mdi:chevron-down"></Icon>
              </a-button>
        </a-dropdown>
      </template>
    </BasicTable>
    </div>
  </div>
  </a-spin>
</template>

<script lang="ts" name="pi-piBasePoint" setup>
  import { ref, reactive, onMounted, unref } from 'vue';
  import {BasicTable, useTable, TableAction, BasicColumn } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage'
  // import {columns} from './QueryTable.data';
  import {list,head,customizeTableList,getExportUrl,saveOrUpdate} from './QueryTable.api';
  import { downloadFile } from '/@/utils/common/renderUtils';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getDepartId } from '/@/utils/ktTool';
  import { useUserStore } from '/@/store/modules/user';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useRoute } from 'vue-router';
  const route = useRoute();
  const queryParams = ref(route.query);
  const tableId = ref(queryParams.value.tableId);
  const { t } = useI18n();

  const queryParam = ref<any>({});
  const toggleSearchStatus = ref<boolean>(false);
  const registerModal = ref();
  import dayjs from 'dayjs';
  const userStore = useUserStore();
  const { createMessage } = useMessage();
  const confirmLoading = ref<boolean>(false);
  const exportParams = reactive({
    departId: '1',
    deviceId: queryParam?.value?.deviceId,
  });
  const columns: BasicColumn[] = [];
  //报表集合
  const tableArr = reactive<Record<string, any>>([]);
  //查询条件集合
  const queryArr = reactive<Record<string, any>>([]);
  //分配字段
  const hasReopen = ref<boolean>(false);
  const departObject = ref<Object>({});
  //查询条件-日期
  const rangePickerDate = ref<Object>({});
  //查询条件-时间
  const rangePickerTime = ref<Object>({});

  //注册table数据
  const { prefixCls,tableContext,onExportXls,onImportXls } = useListPage({
      tableProps:{
           title: 'PI日报查询',
           api: list,
           columns:columns,
           canResize:true,
           pagination:{
            pageSize: 50,
           },
           useSearchForm: false,
           showTableSetting: false, 
           showActionColumn: false,   //表格操作栏
           actionColumn: {
              width: 200,
              fixed: 'right',
           },
         
        beforeFetch: (params) => {
          return Object.assign(params, queryParam.value);
        }
      },
      exportConfig: {
        name:"PI日报查询",
        url: getExportUrl,
      },
  })
 const [registerTable, { reload,setProps, collapseAll, updateTableDataRecord, findTableDataRecord, getDataSource }, { rowSelection, selectedRowKeys }] =
  tableContext;
  const labelCol = reactive({
    xs: { span: 24 },
    sm: { span: 5 },
  });
  const wrapperCol = reactive({
    xs: { span: 24 },
    sm: { span: 16 },
  });

  //编辑完成后触发(进行保存操作)
  function handleEditEnd(record){
    //获取对应列id
    let parameter = {
      id: record.record.id,
      tableId: queryParam.value.tableId,
      value: record.value,
      key: record.key
    }
    //更新值
    editVal(parameter);
  }
  async function editVal(parameter){
    confirmLoading.value = true;
    await saveOrUpdate(parameter, true)
        .then((res) => {
        if (res.success) {
            createMessage.success(res.message);
        } else {
          //保存失败需要刷新
          handleSuccess()
          createMessage.warning(res.message);
        }
    }).finally(() => {
      confirmLoading.value = false;
    });
  }

  onMounted(() => {
    if(tableId){
      queryParam.value.tableId =tableId.value;
      //获取表头
      tableChange(tableId.value,{});
    }
    getTableArr();
  });

  /**
   * 初始化设备类型下拉数据
   */
  async function getTableArr() {
    customizeTableList({ pageNo: 1, pageSize: 999 }).then((res) => {
      if (res && res.records && res.records.length > 0) {
        res.records.map((item) => {
          tableArr.push({label: item.name, value: item.id, tableId: item.id});
        });
      }
    });
  }

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
 * 查询
 */
function searchQuery() {
  reload();
}

/**
 * 重置
 */
function searchReset() {
  queryParam.value = {};
  selectedRowKeys.value = [];
  //刷新数据
  reload();
}

/**
 * 获取查询条件/表头/数据
 */
function tableChange(val,obj){
  //获取表头
  getHaed(val,obj)
}
//获取表头
function getHaed(val,obj){
  let columnsNew =[];
  queryArr.length = 0;
  //是否隐藏右上角工具栏
  // setProps({showTableSetting: false})
  head({ tableId: val }).then((res) => {
      if (res && res.length > 0) {
        for (const item of res) {
          let columnObj = {
            title: item.name,
            align:"center",
            dataIndex: item.fieldName,
            resizable: true, //配置列可伸缩
          }
          //判断是否可编辑
          if(item.isEdit == '1'){
            columnObj.edit = true;
            // columnObj.width = 200;
            //判断字段类型
            if(item.type == '4'){
              //日期
              columnObj.editComponent= 'DatePicker';
              columnObj.editComponentProps= {
                valueFormat: 'YYYY-MM-DD',
                format: 'YYYY-MM-DD',
              }
            }
            if(item.type == '6'){
              //日期时间
              columnObj.editComponent= 'DatePicker';
              columnObj.editComponentProps= {
                valueFormat: 'YYYY-MM-DD HH:mm:ss',
                format: 'YYYY-MM-DD HH:mm:ss',
                showTime:{ format: 'HH:mm:ss' }
              }
            }
          }
          columnsNew.push(columnObj)
          //获取查询条件
          if(item.isQuery === 1){
            queryArr.push(item);
          }
        }
        setProps({columns: columnsNew});
        // setProps({showTableSetting: true})

        //获取数据
        // queryParam.value.tableId = obj.tableId;
        queryParam.value.tableId = val;
        searchQuery();
      }else{
        setProps({columns: []});
      }
  });
}

//查询条件-时间
function onChangeTime(fieldName){
  if(rangePickerTime.value[fieldName] && rangePickerTime.value[fieldName].length>0){
    let timeBegin = rangePickerTime.value[fieldName][0];
    let timeEnd = rangePickerTime.value[fieldName][1];
    //
    queryParam.value[fieldName] = dayjs(timeBegin).format('YYYY-MM-DD HH:mm:ss')+","+dayjs(timeEnd).format('YYYY-MM-DD HH:mm:ss')
  }else{
    queryParam.value[fieldName] ="";
  }
}
//查询条件-日期
function onChangeDate(fieldName){
  if(rangePickerDate.value[fieldName] && rangePickerDate.value[fieldName].length>0){
    let dateBegin = rangePickerDate.value[fieldName][0];
    let dateEnd = rangePickerDate.value[fieldName][1];
    //
    queryParam.value[fieldName] = dayjs(dateBegin).format('YYYY-MM-DD')+","+dayjs(dateEnd).format('YYYY-MM-DD');
  }else{
    queryParam.value[fieldName] ="";
  }
}
</script>

<style lang="less" scoped>
.jeecg-basic-table-form-container {
  .table-page-search-submitButtons {
    display: block;
    margin-bottom: 24px;
    white-space: nowrap;
  }
  .query-group-cust {
    width: calc(50% - 15px);
    min-width: 100px !important;
  }
  .query-group-split-cust {
    width: 30px;
    display: inline-block;
    text-align: center;
  }
}
//@import '../common.less';
</style>