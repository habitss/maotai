<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button type="primary" v-auth="'project.management:mes3_project_management_document:edit'" @click="handleSave"
          preIcon="ant-design:plus-outlined" :loading = saveBtnLoading >保存</a-button>
        <a-button type="primary" v-auth="'project.management:mes3_project_management_document:export'"
          preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
      </template>
      <template #nodeSelectSlot="{ record, column }">
        <JSelectMultiple v-model:value="record[column.dataIndex]" 
        :show-choose-option="false"
        :dictCode="`mes3_project_management_node, node_name, id, del_flag = 0 and node_name is not null and project_id = '${record.id}' order by sort`" />
      </template>
      <template #textareaSlot="{ record, column }">
        <a-textarea v-model:value="record[column.dataIndex]" style="width: 100%" />
      </template>
      <template #scoreSlot="{ record, column, index }">
        <a-input-number @change="() => handleScoreChange(index, column.dataIndex)" v-model:value="record[column.dataIndex]" :precision="2" style="width: 100%" :disabled="(index+1) % 5 === 0" />
      </template>
    </BasicTable>
  </div>
</template>

<script lang="ts" name="project.management-track" setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { BasicTable } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage'
import { columns as projectColumns, searchFormSchema } from '../list/ProjectManagement.data';
import { columns } from './ProjectTrack.data';
import { list, saveOrUpdate, getExportUrl } from './ProjectTrack.api';
import JSelectMultiple from '/@/components/Form/src/jeecg/components/JSelectMultiple.vue';
import {getMonthArr} from '/@/utils/common/YMUtil';
import { cloneDeep } from 'lodash-es';
const queryParam = reactive<any>({});
const saveBtnLoading = ref(false);
const schemas = computed(() => {
  let newArr = cloneDeep(searchFormSchema);
  newArr.forEach((item) => {
    if(item.field == 'months'){
      item.label = '行动月份'
    }
  })
  return newArr.slice(0, -1);
});
//注册table数据
const { tableContext, onExportXls } = useListPage({
  tableProps: {
    title: '项目跟踪与激励',
    pagination: false,
    maxColumnWidth: 90,
    ellipsis: false,
    immediate: false,
    showActionColumn: false,
    showTableSetting: false,
    formConfig: {
      labelWidth: '70px',
      schemas: schemas,
      autoSubmitOnEnter: true,
      showAdvancedButton: true,
      baseColProps: { span: 6 },
      autoAdvancedCol: 10,
      submitFunc: reloadData
    },
    beforeFetch: (params) => {
      return Object.assign(params, queryParam);
    },
  },
  exportConfig: {
    name: "项目跟踪与激励",
    url: getExportUrl,
    params: queryParam,
  },
})

const [registerTable, { getForm, setColumns, getDataSource, setLoading, setTableData }] = tableContext;

/**
 * 保存事件
 */
async function handleSave() {
  try{
    saveBtnLoading.value = true;
    const searchForm = getForm().getFieldsValue();
    if(!searchForm.months){
      saveBtnLoading.value = false;
      return;
    }
    const saveData = {
      year: searchForm.year,
      months: String(searchForm.months).split(',').sort(),
      dataList: getDataSource()
    }
    await saveOrUpdate(saveData);
    setTimeout(() => {
        // 这里的代码将在1秒(1000毫秒)后执行
      reloadData();
    }, 2000);
  } catch (error) {
    console.error(error);
  } 
}

/**
 * 加载数据
 */
async function reloadData(){
  setLoading(true);
  //清空数据
  await setTableData([]);
  //获取查询表单数据
  const searchForm = getForm().getFieldsValue();
  const res = await list({
    ...searchForm,
      months : searchForm.months ? searchForm.months : getMonthArr(false).map(item => item.value).join(',')
  });
  //月累列配置
  let monthColumn = [] as any;
  //月、周累复选框不为空时，获取月、周累数据并设置表格列头信息
  if (searchForm.months) {
    //拆分选择的月份数据，并排序处理成数组形式
    let monthArr = String(searchForm.months).split(',').map(Number);
    monthArr = monthArr.sort((a, b) => a - b);
    //用于判断是否添加新的月累表头
    for (const item of monthArr) {
      const key = searchForm.year+'-'+item;
      //勾选月累，且当前数据月不是上月添加一次月累表头
      monthColumn.push({
        title: item + '月',
        align: "center",
        children: [
          {
            title: '本月计划完成节点',
            dataIndex: key + '-planNodeIds',
            width: 200,
            slots: { customRender: 'nodeSelectSlot' },
            customCell: (record, index) => setCustomCell(index),
          },
          {
            title: '本月实际完成节点',
            dataIndex: key + '-realNodeIds',
            width: 200,
            slots: { customRender: 'nodeSelectSlot' },
            customCell: (record, index) => setCustomCell(index),
          },
          {
            title: '效果',
            dataIndex: key + '-effect',
            width: 300,
            slots: { customRender: 'textareaSlot' },
            customCell: (record, index) => setCustomCell(index),
          },
          {
            title: '延期说明及改进',
            dataIndex: key + '-improveRemark',
            width: 300,
            slots: { customRender: 'textareaSlot' },
            customCell: (record, index) => setCustomCell(index),
          },
          {
            title: '计划分',
            dataIndex: key + '-planScore',
            width: 140,
            slots: { customRender: 'scoreSlot' },
          },
          {
            title: '加分',
            dataIndex: key + '-addScore',
            width: 140,
            slots: { customRender: 'scoreSlot' },
          },
        ],
      })
    }
  }
  let newColumns = cloneDeep(projectColumns);
  newColumns.forEach((item) => {
    item.customCell = (record, index) => setCustomCell(index);
  })
  setColumns([...newColumns, ...columns, ...monthColumn]);
  await setTableData(res);
  setLoading(false);
  saveBtnLoading.value = false;
}

/**
 * 设置合并行数
 * @param index 数据行下标
 */
function setCustomCell(index){
  return {
    rowSpan: index % 5 == 0 ? 5 : 0
  }
}
/**
 * 分数改变事件
 * @param index 数据行下标
 * @param field 字段
 */
function handleScoreChange(index, field) {
  //获取开始行下标
  const startIndex = index - (index %5);
  //获取结束行下标
  const endIndex = startIndex + 4;
  //获取表格数据
  const dataSource = getDataSource().slice(startIndex, endIndex);
  //计算合计值
  let total = 0;
  for(const item of dataSource){
    total += item[field] ?? 0;
  }
  //设置合计行数据
  getDataSource()[endIndex][field] = total;
}

onMounted(() => {
  reloadData();
})
</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}
</style>