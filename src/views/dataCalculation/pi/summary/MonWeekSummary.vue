<template>
  <div class="bg-white m-2 p-4">
    <BasicForm @register="registerForm" />
  </div>
  
  <a-spin :spinning="searchLoading" class="span-box">
    <div class="bg-white m-2 p-2">
      <a-button type="primary" preIcon="ant-design:export-outlined" @click="exportData()"
        style="margin-left: 8px">导出</a-button>
      <ListTable :options="vtableOption" ref="listTableRef" :height="listTableHeight"></ListTable>
    </div>
  </a-spin>
</template>

<script lang="ts" name="pi-monWeekSummary" setup>
import { BasicForm, useForm } from '/@/components/Form/index';
import { list } from './MonWeekSummary.api';
import { searchFormSchema } from './MonWeekSummary.data';
import { nextTick, onMounted, reactive, ref } from 'vue';
import { getColor } from '/@/utils/common/piColorUtils';
import { VTable, ListTable } from '@visactor/vue-vtable';
import {downloadExcel,exportVTableToExcel} from "@visactor/vtable-export";
import { useMessage } from '/@/hooks/web/useMessage';
//消息组件
const { createMessage } = useMessage();
//加载中
const searchLoading = ref(false);
//表格实例
const listTableRef = ref();
//表格初始高度
const listTableHeight = ref(20);
//表单配置
const [registerForm, { getFieldsValue }] = useForm({
  labelWidth: 100,
  schemas: searchFormSchema,
  showAdvancedButton: true,
  autoAdvancedCol: 3,
  alwaysShowLines: 1,
  baseColProps: { span: 6 },
  submitFunc: getData
});

//表格配置
const vtableOption = reactive({
  records: [] as any,
  columns: [] as any,
  enableLineBreak: true,
  autoWrapText: true,
  limitMaxAutoWidth: 600,
  heightMode: 'autoHeight',
  theme: VTable.themes.ARCO.extends({
    scrollStyle: {
      visible: 'always',
      hoverOn: false,
    }
  }),
});

async function getData() {
  try {
    await nextTick();
    searchLoading.value = true;
    const res = await list(getFieldsValue());
    let columnList = [
      {
        title: '报表名称',
        field: 'tableCode',
        headerStyle: {
          textAlign: 'center',
        },
        style: {
          textStick: true,
        },
        width: 200,
        mergeCell: true,
      },
      {
        title: 'PI名称',
        field: 'fieldCode',
        headerStyle: {
          textAlign: 'center',
        },
        width: 200,
        mergeCell: true,
      },
      {
        title: '类型',
        field: 'text',
        headerStyle: {
          textAlign: 'center',
        },
        width: 200,
      }
    ] as any;
    for (let key in res?.weekList) {
      columnList.push({
        title: '月累',
        headerStyle: {
          textAlign: 'center',
        },
        width: 100,
        columns: [
          {
            title: key + '月',
            field: key,
            headerStyle: {
              textAlign: 'center',
            },
            style: {
              textAlign: 'center',
              bgColor: (args) => {
                return calcColor(args.row, args.col, args.value, key);
              }
            },
            width: 120,
          },
        ]
      })
      for (let item of res?.weekList[key]) {
        columnList.push({
          title: `第${item.weekNumber}周`,
          headerStyle: {
            textAlign: 'center',
          },
          width: 120,
          columns: [
            {
              title: item.weekRangeStr,
              field: item.weekRangeStr,
              style: {
                textAlign: 'center',
                bgColor: (args) => {
                  return calcColor(args.row, args.col, args.value, key);
                }
              },
              width: 100,
            }
          ]
        })
      }
    }
    vtableOption.columns = columnList;
    vtableOption.records = res?.resultList;
    //渲染表格
    await listTableRef.value.vTableInstance.renderWithRecreateCells();
  } catch (e) {
    console.log('error :>> ', e);
  } finally {
    searchLoading.value = false;
  }
}

function calcColor(row, col, value, field) {
  let color = 'white';
  try {
    if (vtableOption.records[row - 2].text === '实际值(%)') {
      let targetValue = vtableOption.records[row - 5][field];
      if (!targetValue) {
        color = getColor('grey')
      } else if (!value) {
        color = getColor('red')
      } else if (value >= targetValue) {
        color = getColor('green');
      } else if (value < targetValue) {
        color = getColor('red');
      }
    }
  } catch (e) {
    console.log('error 2:>> ', e);
  } finally {
  }
  return color;
}

//导出
async function exportData() {
  const listTableInstance = listTableRef.value;
  if (!listTableInstance) {
    createMessage.warning("ListTable 实例未创建或未挂载");
  }
  //获取 VTable 实例的数据
  const tableInstance = listTableInstance.vTableInstance;
  downloadExcel(await exportVTableToExcel(tableInstance), "自定义报表");
}
onMounted(async () => {
  getData();
  listTableHeight.value = (document.documentElement.scrollHeight || document.body.scrollHeight)-260;
});
</script>