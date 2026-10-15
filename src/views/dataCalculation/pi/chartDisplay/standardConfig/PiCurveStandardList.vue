<template>
  <div class="box-main">
    <!-- <div style="margin: 0px 10px;height: 100%;"> -->
      <a-row :gutter="[16, 16]" style="margin-bottom: 5px;">
        <!--@advanced-change="handleAdvancedChange" @submit="handleSearchQuery"-->
        <a-col :span="24">
          <div style="height: 60px;background-color: #fff;display: flex;align-items: center;padding: 0 16px;">
            <div style="height: 35px;flex: 1;">
              <BasicForm @register="registerForm" :show-action-button-group="true" 
              :rowSelection="{ type: 'checkbox', selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
                @submit="handleSearchQuery">
              </BasicForm>
            </div>
          </div>
        </a-col>
      </a-row>
      <a-row :gutter="[16, 16]">
        <a-col :span="8">
          <div class="page-wrapper">
            <!--引用表格-->
            <BasicTable @register="registerTable"  @row-click="handleRowClick" :rowClassName="getRowClassName">
            <!--插槽:table标题-->
              <template #tableTitle>
                <a-button type="primary" v-auth="'pi:mes3_pi_curve_standard:add'" @click="handleAdd" preIcon="ant-design:plus-outlined"> 新增</a-button>
                <a-dropdown v-if="selectedRowKeys.length > 0">
                      <template #overlay>
                        <a-menu>
                          <a-menu-item key="1" @click="batchHandleDelete">
                            <Icon icon="ant-design:delete-outlined"></Icon>
                            删除
                          </a-menu-item>
                        </a-menu>
                      </template>
                      <a-button v-auth="'pi:mes3_pi_curve_standard:deletionBatch'">批量操作
                        <Icon icon="mdi:chevron-down"></Icon>
                      </a-button>
                </a-dropdown>
              </template>
              <!--操作栏-->
              <template #action="{ record }">
                <TableAction :actions="getTableAction(record)"/>
              </template>
              <!--字段回显插槽-->
              <template v-slot:bodyCell="{ column, record, index, text }"></template>
            </BasicTable>
          </div>
        </a-col>
        <a-col :span="16" style="background-color: #fff;">
          <div class="tab-table-wrapper">
            <div style="height: 46px;">
              <a-tabs v-model:activeKey="activeKey" @change="handleTabChange">
                <a-tab-pane v-for="item in groupData" :key="item.key" :tab="item.title"></a-tab-pane>
              </a-tabs>
            </div>
            <BasicTable @register="registerStepTable"></BasicTable>
          </div>
        </a-col>
      </a-row>

    <!-- </div> -->
    <!-- 表单区域 -->
    <PiCurveStandardEditModal @register="registerModal" @success="handleSuccess"></PiCurveStandardEditModal>
    <RecipeModal @register="recipeRegisterModal" @success="handleSuccess"></RecipeModal>
  </div>
</template>

<script lang="ts" name="pi-piCurveStandard" setup>
  import {ref, reactive, computed, unref, nextTick} from 'vue';
  import {BasicTable, useTable, TableAction} from '/@/components/Table';
  import {useModal} from '/@/components/Modal';
  import {BasicForm, useForm} from '/@/components/Form/index';
  import { useRoute } from 'vue-router';
  import { useListPage } from '/@/hooks/system/useListPage'
  import PiCurveStandardEditModal from './components/PiCurveStandardEditModal.vue'
  import RecipeModal from './components/RecipeModal.vue'
  import {columns, recipeColumns, getSearchFormSchema} from './PiCurveStandard.data';
  import {list,recipeList, recipeDelete, batchDelete, getImportUrl,getExportUrl, getDevice,loadDeviceGroupData,configQueryPageList} from './PiCurveStandard.api';
  import { downloadFile } from '/@/utils/common/renderUtils';
  import { useUserStore } from '/@/store/modules/user';
  const queryParam = reactive<any>({});
  const stepQueryParam = reactive<any>({});
  const checkedKeys = ref<Array<string | number>>([]);
  const userStore = useUserStore();
  //获取路由
  const route = useRoute();
  //获取路由中的code参数
  const dataSource = route.params.dataSource;
  const activeKey = ref('1');
  const groupData = ref([]);
  // 记录当前选中的行
  const recipeRecord = ref({});

  //注册model
  const [registerModal, {openModal}] = useModal();
  const [recipeRegisterModal, {openModal:openRecipeModal}] = useModal();
  
  // 加载设备选项
  const fetchUnits = async (dataSource, areaNo) => {
    if(!areaNo){
      return false;
    }
    // 根据数据源和区域过滤设备
    let res = await getDevice({dataSource, areaNo})
    let unitArray = []
    if(res){
      res.forEach((item, index, array) => {
        //0标准和1目标
        let device = item.deviceCode.split('-')[1];
        unitArray.push({
          value: `${device}`,
          label: item.deviceName
        })
      })
      return unitArray;
    }
    return [];
  };

  const handleAreaChange = async (areaNo) => {
    const form = getForm();
    // 1处理多选 / labelInValue 情况
    let realAreaNo = areaNo;
    
    // labelInValue
    if (typeof areaNo === 'object' && areaNo?.value) {
      realAreaNo = areaNo.value;
    }

    // 2清空设备
    form.setFieldsValue({
      unitNo: undefined,
    });

    // 3没选区域直接清空
    if (!realAreaNo) {
      form.updateSchema({
        field: 'unitNo',
        componentProps: { options: [] },
      });
      return;
    }

    // 4拿数据源
    const { dataSource } = form.getFieldsValue();

    // 5查设备
    const options = await fetchUnits(dataSource, realAreaNo);

    // 6更新 schema
    form.updateSchema({
      field: 'unitNo',
      componentProps: {
        options,
        disabled: false,
      },
    });
  }
  //配方表
  //注册table数据
  const { prefixCls,tableContext,onExportXls,onImportXls } = useListPage({
    tableProps:{
      // title: '曲线标准配置',
      api: recipeList,
      columns: recipeColumns,
      canResize:true,
      formConfig: {
        autoSubmitOnEnter:true,
        showAdvancedButton:true,
        fieldMapToNumber: [],
        fieldMapToTime: [],
      },
      showTableSetting: false,
      actionColumn: {
        width: 90,
        fixed:'right'
      },
      pagination: {
        pageSize: 20,     // 默认每页显示20条
        pageSizeOptions: ['20','50','100'],
        showQuickJumper: false, // 隐藏快速跳转
      },
      beforeFetch: (params) => { 
        queryParam.dataSource = dataSource
        return Object.assign(params, queryParam);
      },
    },
    exportConfig: {
      name:"曲线标准配置",
      url: getExportUrl,
      params: queryParam,
    },
    importConfig: {
      url: getImportUrl,
      success: handleSuccess
    },
  })
  const [registerTable, {reload,getForm},{ rowSelection, selectedRowKeys }] = tableContext

  //步骤表
  const { prefixCls: prefixClsStep, tableContext:tableContextStep } = useListPage({
    tableProps:{
      // title: '曲线标准配置',
      api: configQueryPageList,
      columns,
      canResize:true,
      formConfig: {
        autoSubmitOnEnter:true,
        showAdvancedButton:true,
        fieldMapToNumber: [],
        fieldMapToTime: [],
      },
      showTableSetting: false,
      showActionColumn: false,
      pagination: {
        pageSize: 20,     // 默认每页显示20条
        pageSizeOptions: ['20','50','100'],
        showQuickJumper: false, // 隐藏快速跳转
      },
      beforeFetch: (params) => {
        stepQueryParam.dataSource = dataSource
        //stepQueryParam.recipeId = record.id
        if(recipeRecord.value.id){
          stepQueryParam.recipeId = recipeRecord.value.id
        }else{
          stepQueryParam.recipeId = '-1'
        }
        if(activeKey.value){
          stepQueryParam.groupId = activeKey.value
        }else{
          stepQueryParam.groupId = '-1'
        }
        return Object.assign(params, stepQueryParam);
      },
    },
    exportConfig: {
      name:"曲线标准配置",
      url: getExportUrl,
      params: stepQueryParam,
    },
    importConfig: {
      url: getImportUrl,
      success: handleSuccess
    },
  })

  const [registerStepTable,{reload: stepReload}] = tableContextStep

   /**
    * 新增事件
    */
  function handleAdd() {
    let record = { dataSource : Number(dataSource) }
    openRecipeModal(true, {
      record,
      isUpdate: false,
      showFooter: true,
    });
  }
   /**
    * 编辑事件
    */
  function handleEdit(record: Recordable) {
    record.dataSource = Number(dataSource) 
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
    * 删除事件(配方删除)
    */
  async function handleDelete(record) {
     await recipeDelete({id: record.id}, handleSuccess);
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
        label: '配置',
        onClick: handleEdit.bind(null, record),
        auth: 'pi:mes3_pi_curve_standard:edit'
      },{
        label: '删除',
        popConfirm: {
          title: '是否确认删除',
          confirm: handleDelete.bind(null, record),
          placement: 'topLeft',
        },
        auth: 'pi:mes3_pi_curve_standard:delete'
      }
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
           auth: 'pi:mes3_pi_curve_standard:delete'
         }
       ]
   }

    //表单配置
    const [registerForm, { setProps,resetFields, setFieldsValue, validate, scrollToField,getFieldsValue,updateSchema }] = useForm({
      labelWidth: 150,
      schemas: getSearchFormSchema(handleAreaChange),
      showActionButtonGroup: true,
      showResetButton: false,
      baseColProps: {span: 24},
      //操作按钮的位置
      //actionColOptions: { span: 9 },
      //提交按钮的自定义事件
      submitFunc: customSubmitFunc,
      //重置按钮的自定义事件
      //resetFunc: customResetFunc,
    });
   function customSubmitFunc(values) {
      let data = getFieldsValue()
      queryParam.areaNo = data.areaNo
      queryParam.recipeNo = data.recipeNo ? "*"+data.recipeNo+"*" : ""
      queryParam.recipeName = data.recipeName ? "*"+data.recipeName+"*" : ""
      reload()
   }

  function handleSearchQuery(){
      reload()
  }
   //行被点击
  function handleRowClick(record, index, event) {
    console.log('点击的行数据：', record);  // 当前行数据
    recipeRecord.value = record;
    getGroupData(record.id)
  }
  // 动态行样式
  function getRowClassName (record, index) {
    if (recipeRecord.value && recipeRecord.value.id === record.id) {
      return 'selected-row';  // 选中行的样式类
    }
    return '';
  };
  //加载数据(设备分类)
  async function getGroupData(recipeId){
    groupData.value = await loadDeviceGroupData({ recipeId });
    if(groupData.value && groupData.value.length > 0 ){
      //默认选中第一个
      activeKey.value = groupData.value[0].key;
    }else{
      activeKey.value = '';
    }
    await nextTick();
    //查询步骤数据
    stepReload();
  }
  // 分组改变
  function handleTabChange(key) {
    //查询步骤数据
    stepReload();
  }
</script>

<style lang="less" scoped>
  .box-main{
    margin: 0px 10px;
    height: 87vh;
  }

  :deep(.ant-picker),:deep(.ant-input-number){
    width: 100%;
  }

  .page-wrapper {
    background-color: #fff;
    height: 100%;
    display: flex;
    flex-direction: column;
  }
  .page-wrapper :deep(.ant-table-wrapper) {
    flex: 1;
  }
  .tab-table-wrapper {
    height: 100%;
    display: flex;
    flex-direction: column;
    background-color: #fff;
  }

  .tab-table-wrapper {
    :deep(.ant-tabs-nav) {
      margin: 0;
    }
  }
  .box-main :deep(.jeecg-basic-table-form-container) {
    padding: 0px;
  }

  .tab-table-wrapper :deep(.jeecg-basic-table .ant-table-wrapper) {
    padding-top: 3px;
    padding-left: 0px;
    padding-right: 0px;
  }


  // 选中行样式 - 颜色加深
  :deep(.selected-row) {
    background-color: #e6f7ff !important;  /* 浅蓝色背景 */
    /* 或者更深一些的颜色 */
    /* background-color: #bae7ff !important; */
  }
  // 选中行内的操作按钮样式
  :deep(.selected-row) .ant-table-cell{
    background-color: #e6f7ff !important;
    border-radius: 4px;
    padding: 4px 12px;
  }
  // 鼠标悬停样式
  :deep(.ant-table-row:hover) {
    background-color: #f5f5f5 !important;
  }

  // 选中行的悬停样式
  :deep(.selected-row:hover) {
    background-color: #bae7ff !important;
  }
</style>