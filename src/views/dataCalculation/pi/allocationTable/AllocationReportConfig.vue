<template>
  <div>
    <!--主表信息显示-->
    <div class="jeecg-basic-table-form-container">
      <!--引用表格-->
      <BasicTable @register="registerTable">
        <template #tableTitle>
          <a-button type="primary" preIcon="ep:plus" @click="onSave">保存</a-button>
        </template>

        <template #headerCell="{ column }">
          {{ column.customTitle }}
          <span v-if="column?.key?.startsWith('show')">
            <a-checkbox 
              v-model:checked="checkAllMap[column.key]" 
              @change="handleChangeAll(column.key)"
            ></a-checkbox>
          </span>
        </template>

        <template #bodyCell="{ column, record }">
          <a-checkbox 
            v-if="column?.key?.startsWith('show')"
            v-model:checked="record[column.key]" 
            @change="checkIsAll(column.key)"
          >
          </a-checkbox>
        </template>
      </BasicTable>
    </div>
  </div>
</template>

<script lang="ts" name="pi-PiCustomizeReportConfig" setup>
  import { ref, onMounted, reactive, } from 'vue';
  import { BasicTable } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage'
  import { columns, searchFormSchema } from './AllocationReportConfig.data';
  import { list, editBatch } from './AllocationReportConfig.api';
  import { useMessage } from '/@/hooks/web/useMessage';

  //消息提示
  const { createMessage: msg } = useMessage();
  //查询参数
  const queryParam = ref<any>({});
  //接收父组件传过来的参数
  const props = defineProps({
    departObject: { type: Object,default:()=>({}) },
  });

  //复选框全选对象
  const checkAllMap = reactive({
    //标准值
    showStandard: false,
    //偏差值
    showDeviation: false,
    //实际值
    showActual: false,
  });

  //注册table数据
  const { tableContext } = useListPage({
    tableProps:{
      title: '报表配置',
      api: list,
      columns,
      pagination: false,
      showActionColumn: false,
      canResize: true,
      beforeFetch: (params) => {
        params.column ="sort";
        params.order ="asc";
        return Object.assign(params, queryParam.value);
      },
      afterFetch: (records) => {
        for(const key in checkAllMap){
          checkIsAll(key, records);
        }
      },
      formConfig: {
        schemas: searchFormSchema,
        fieldMapToTime: [['createTime', ['createTime_begin', 'createTime_end'], 'YYYY-MM-DD']],
      },
    },
  })
  
  //注册table
  const [registerTable, { getDataSource }, { selectedRowKeys }] = tableContext;

  //保存
  async function onSave(){
    try {
      //调用后台修改接口, 删除
      await editBatch(getDataSource());
    } catch (error) {
      msg.error({ content: '操作失败', key: 'saving' });
    }
  }

  //全选事件
  function handleChangeAll(columnKey) {
    getDataSource().forEach(item => {
      item[columnKey] = checkAllMap[columnKey];
    });
  }
  //判断是否全选
  function checkIsAll(columnKey, records){
    if(null == records){
      records = getDataSource();
    }
    let count = 0;
    records.forEach(item => {
      if(item[columnKey]) count++;
    });
    checkAllMap[columnKey] = count === records.length;
  }

  //初始化
  onMounted(() => {
    const { departObject } = props;
    queryParam.value.allocationTableId = departObject.id;
    queryParam.value.pageSize = 999;
  });
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
</style>