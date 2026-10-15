<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable">
      <template #tableTitle>
        <a-button type="primary" v-auth="'project.motivation:mes3_project_motivation:exportExcel'" preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
      </template>
    </BasicTable>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { BasicTable } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage'
import { list } from '../ProjectMotivation.api';
import { columns as projectColumns } from '../../list/ProjectManagement.data';
import { cloneDeep } from 'lodash-es';
import ExcelJS from '/@/utils/excelUtils/exportExcel';

const summaryType = ref('');

//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    pagination: false,
    maxColumnWidth: 90,
    ellipsis: false,
    immediate: false,
    showActionColumn: false,
    showIndexColumn: true,
    showTableSetting: false,
    indexColumnProps: {
      width: '30px'
    },
  },
})

const [registerTable, { setColumns, getColumns, setProps, setTableData, setLoading, getDataSource }] = tableContext;

async function reloadData(type, searchParams) {
  setLoading(true);
  summaryType.value = type;

  //清空数据
  await setTableData([]);
  //查询后台数据
  const res = await list(type, searchParams);

  //获取月份数组，动态生成列
  let monthColumns = [];
  for (let i = 1; i <= 12; i++) {
    monthColumns.push({
      title: i + '月',
      align: "center",
      dataIndex: 'month' + i,
      width: 50,
    })
  }
  //获取基础列，去掉最后两列
  let newColumns = cloneDeep(projectColumns.slice(0, 5));
  if (type === 'personal') {
    let id = '';
    //需要合并的行数
    let rowSpan = 1;
    await res.forEach((item, index) => {
      if (id != item.id) {
        //如果id不同，则合并之前的行数，并重置rowSpan和id
        if (index != 0) {
          res[(index - rowSpan)].rowSpan = rowSpan;
        }
        id = item.id
        rowSpan = 1;
      } else {
        //如果id相同，则rowSpan加1,并判断是否为最后一行，如果是最后一行，则需要合并之前的行数
        rowSpan++;
        res[index].rowSpan = 0;
        if (index == res.length - 1) {
          res[(index - (rowSpan - 1))].rowSpan = rowSpan;
        }
      }
    })

    await newColumns.forEach((item, itemIndex) => {
      delete item.fixed;
      if (itemIndex < 5) {
        item.customCell = (record, index) => {
          return {
            rowSpan: record.rowSpan ?? 1,
          };
        };
      }
    })

    //加上人员列
    await newColumns.push({
      title: '人员姓名',
      align: "center",
      dataIndex: 'leaderUserId_dictText',
      width: 120,
    }, {
      title: '项目贡献度',
      align: "center",
      dataIndex: 'contribution',
      width: 80,
      customRender: ({ record }) => {
        return record.contribution ? record.contribution + '%' : '';
      },
    });
  }
  //加上月累列
  newColumns.push({
    title: '月累',
    align: "center",
    children: monthColumns
  }, {
    title: '汇总加分',
    align: "center",
    dataIndex: 'summarys',
    width: 50,
  });
  //设置列
  await setColumns(newColumns);
  //设置数据
  await setTableData(res);
  setLoading(false);
}

//导出excel表格数据
function onExportXls() {
  //根据类型设置合并列
  let mergeArr = [0,1,2,3,4,5,7];
  let fileName = '项目激励汇总表-部门项目加分';
  if(summaryType.value === 'personal'){
    mergeArr = [0,1,2,3,4,5,6,7,9];
    fileName = '项目激励汇总表-部门个人加分';
  }
  //获取导出表头
  const headers = ExcelJS.getNewHeaders(getColumns(), mergeArr);
  //调用导出方法
  ExcelJS.commonDownColorExcel(headers, getDataSource(), fileName)
}

//暴露方法
defineExpose({
  reloadData
})
</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}
</style>