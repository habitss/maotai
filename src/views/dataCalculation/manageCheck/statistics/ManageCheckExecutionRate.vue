<template>
  <div class="p-2">
    <!--引用表格-->
    <BasicTable @register="registerTable" >
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button type="primary" preIcon="ant-design:export-outlined" @click="handleExportXls">导出</a-button>
      </template>
    </BasicTable>
  </div>
</template>

<script lang="ts" name="manageCheck-executionRate" setup>
  import { onMounted } from 'vue';
  import { BasicTable } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { columns, searchFormSchema } from './ManageCheckExecutionRate.data';
  import { list } from './ManageCheckExecutionRate.api';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();
  import { downloadFile } from '/@/utils/common/renderUtils';
  import dayjs from 'dayjs';
  import { useUserStore } from '/@/store/modules/user';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { exportExcel } from '/@/utils/common/exportToExcelUtils';
  const userStore = useUserStore();
  const { createMessage } = useMessage();

  //注册table数据
  const { tableContext } = useListPage({
    tableProps: {
      title: '执行率统计',
      api: list,
      canResize:true,
      useSearchForm: true,
      formConfig: {
        schemas: searchFormSchema,
        autoSubmitOnEnter: true,
        showAdvancedButton: false,
        // 设置默认值
        defaultValues: {
          timeRange: [
            dayjs().startOf('month').format('YYYY-MM-DD'),
            dayjs().format('YYYY-MM-DD')
          ]
        }
      },
      pagination:false,
      showActionColumn: false,
      actionColumn: {
        width: 120,
        fixed: 'right',
      },
      columns: columns,
      beforeFetch: async (params) => {
        // 处理时间范围参数
        console.log('Before fetch params:', params);
        if (params.timeRange) {
          // 检查 timeRange 是否为字符串
          if (typeof params.timeRange === 'string') {
            // 分割字符串获取开始和结束日期
            const timeRangeArray = params.timeRange.split(',');
            if (timeRangeArray.length === 2) {
              params.checkDateBegin = timeRangeArray[0];
              params.checkDateEnd = timeRangeArray[1];
              console.log('Time range set from string:', params.checkDateBegin, params.checkDateEnd);
            }
          } else if (Array.isArray(params.timeRange) && params.timeRange.length === 2) {
            // 如果是数组，直接使用
            params.checkDateBegin = params.timeRange[0];
            params.checkDateEnd = params.timeRange[1];
            console.log('Time range set from array:', params.checkDateBegin, params.checkDateEnd);
          }
        }
        // 处理部门和岗位参数
        params.dept = params.deptCode;
        // 处理岗位多选
        if (params.postId && Array.isArray(params.postId) && params.postId.length > 0) {
          params.postId = params.postId.join(',');
        }
        console.log('Final params:', params);
        return params;
      },
    }
  });
  const [registerTable, { reload, getForm }] = tableContext;

  onMounted(() => {
    // 初始加载数据
    reload();
  })

  // 导出方法
  const handleExportXls = async () => {
    try {
      const form = await getForm();
      const values = form.getFieldsValue();
      const params: any = {};
      
      if (values.timeRange) {
        if (Array.isArray(values.timeRange) && values.timeRange.length === 2) {
          params.checkDateBegin = values.timeRange[0];
          params.checkDateEnd = values.timeRange[1];
        } else if (typeof values.timeRange === 'string') {
          const timeRangeArray = values.timeRange.split(',');
          if (timeRangeArray.length === 2) {
            params.checkDateBegin = timeRangeArray[0];
            params.checkDateEnd = timeRangeArray[1];
          }
        }
      }
      
      if (values.deptCode) {
        params.dept = values.deptCode;
      }
      if (values.postId && Array.isArray(values.postId) && values.postId.length > 0) {
        params.postId = values.postId.join(',');
      }
      if (values.checkerName) {
        params.checkerName = values.checkerName;
      }
      
      console.log('导出请求参数:', params);
      
      // 获取数据
      const response = await list(params);
      const tableData = response?.records || response || [];
      console.log('导出数据:', tableData);
      
      // 处理导出数据
      const exportData = tableData.map(item => ({
        '部门': item.deptName || '',
        '岗位': item.postName || '',
        '检查人姓名': item.realName || '',
        '应执行次数': item.plannedCount || 0,
        '实际执行数': item.actualCount || 0,
        '执行率': item.executionRate || '0%'
      }));
      
      // 导出Excel
      exportExcel('执行率统计', exportData, null);
      createMessage.success('导出成功');
    } catch (error) {
      createMessage.error('导出失败');
      console.error('导出失败详细信息:', error);
    }
  };

</script>

<style lang="less" scoped>
</style>
