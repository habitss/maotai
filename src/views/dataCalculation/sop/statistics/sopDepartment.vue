<template>
    <div class="p-2">
      <!--查询区域-->
      <div class="jeecg-basic-table-form-container">
        <a-form ref="formRef" @keyup.enter.native="searchQuery" :model="queryParam" :label-col="labelCol" :wrapper-col="wrapperCol">
          <a-row :gutter="24">
            <!--年份-->
            <a-col :lg="6">
              <a-form-item label="年份">
                <a-date-picker v-model:value="queryParam.year" picker="year" valueFormat="YYYY" format="YYYY" @change="classification"/>
              </a-form-item>
            </a-col>
            
            <!--部门-->
            <a-col :span="6">
              <a-form-item label="部门">
                <kt-depart-select
                  v-model:value="queryParam.depts"
                  select-type="depart"
                  placeholder="请选择部门"
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
      </div>
  
  
      <!--引用表格-->
      <BasicTable @register="registerTable">
        <!--插槽:table标题-->
        <template #tableTitle>
        </template>
        <template v-slot:bodyCell="">
        </template>
      </BasicTable>
    </div>
  </template>
  
  <script lang="ts" name="sop-sopDepartment" setup>
    import { ref, reactive, onMounted } from 'vue';
    import { BasicTable } from '/@/components/Table';
    import { useListPage } from '/@/hooks/system/useListPage';
    import { columns } from './sopDepartment.data';
    import { list } from './sopDepartment.api';
    import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
    import { useI18n } from '/@/hooks/web/useI18n';
    import dayjs from 'dayjs';
    const { t } = useI18n();
    const formRef = ref();
    const queryParam = reactive<any>({
      year: '',
      depts:'',
      dept:''
    });
    //注册table数据
    const { tableContext } = useListPage({
      tableProps: {
        title: '诊断统计',
        api: list,
        columns,
        rowKey:"orgCode",
        isTreeTable: true,
        canResize:false,
        useSearchForm: false,
        showActionColumn: false,
        pagination: false,
        actionColumn: {
          width: 120,
          fixed: 'right',
        },
        beforeFetch: async (params) => {
          return Object.assign(params, queryParam);
        },
      }
    });
    const [registerTable, { reload, setTableData }] = tableContext;
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
     * 查询
     */
    function searchQuery() {
      setTableData([]);
      reload();
    }
    
    onMounted(() => {
      let date = new Date();
      queryParam.year = dayjs(date).format('YYYY');
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
  </style>
  