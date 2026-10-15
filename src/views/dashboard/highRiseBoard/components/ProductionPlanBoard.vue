<template>
  <div style="height: 40vh">
    <div class="title">生产计划看板</div>
    <a-table
      id="dataTable"
      :columns="tableColumns"
      :customHeaderRow="setCustomHeaderRow"
      :data-source="tableData"
      :loading="tableLoading"
      :pagination="false"
      bordered
      :rowClassName="setRowClassName"
      :scroll="{ y: '30vh' }"
      size="small"
    >
      <template #bodyCell="{ text, column, record }">
        <template v-if="showTooltipArr.includes(column.key)">
          <a-tooltip placement="bottom">
            <template #title>{{ text }}</template>
            {{ text }}
          </a-tooltip>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script lang="ts" setup>
import {onMounted, reactive, ref} from "vue";
import {cloneObject} from "@/utils";
import {PLAN_PLAN_QUERYPAGELIST} from "@/views/dashboard/highRiseBoard/index.api";
import {message} from "ant-design-vue";
import dayjs from "dayjs";
import {h} from 'vue';
import {useI18n} from '/@/hooks/web/useI18n';

const {t} = useI18n();
const showTooltipArr = ['skuDetail', 'realStartTime']
const tableColumns = reactive([
  {
    title: '产线',
    dataIndex: 'lineCodeVal',
    width: 50,
    customHeaderCell: () => {
      return {style: {backgroundColor: '#171d48', color: '#fff'}}
    }
  },
  {
    title: '成品',
    dataIndex: ['sku', 'skuDetail'],
    key: 'skuDetail',
    width: 300,
    ellipsis: true,
    customHeaderCell: () => {
      return {style: {backgroundColor: '#171d48', color: '#fff'}}
    }
  },
  {
    title: '开始时间',
    dataIndex: 'realStartTime',
    key: 'realStartTime',
    width: 180,
    ellipsis: true,
    customHeaderCell: () => {
      return {style: {backgroundColor: '#171d48', color: '#fff'}}
    }
  },
  {
    title: t('component.upload.fileStatue'),
    dataIndex: 'planStatus',
    key: 'planStatus',
    width: 70,
    customHeaderCell: () => {
      return {style: {backgroundColor: '#171d48', color: '#fff'}}
    },
    customCell: (record, rowIndex, column) => {
      switch (record.planStatus) {
        case 1:
          return {
            style: {
              'background-color': '#9e9e9e',
            }
          }
        case 2:
          return {
            style: {
              'background-color': '#e7ac4a',
            }
          }
        case 3:
          return {
            style: {
              'background-color': '#479b2f',
            }
          }
        case 4:
          return {
            style: {
              'background-color': '#181d45',
            }
          }
        default:
          return '/'
      }
    },
    customRender: ({text, record, index, column}): VNode => {
      switch (text) {
        case 1:
          return h('span', {
            style: {
              color: '#000',
            },
          }, '未开始')
        case 2:
          return h('span', {
            style: {
              color: '#000',
            },
          }, '进行中')
        case 3:
          return h('span', {
            style: {
              color: '#000',
            },
          }, '已完成')
        case 4:
          return h('span', {
            style: {
              color: '#000',
            },
          }, '已关闭')
        default:
          return '/'
      }
    },
  },
  {
    title: '计划产量(瓶)',
    dataIndex: 'bottleNum',
    width: 100,
    customHeaderCell: () => {
      return {style: {backgroundColor: '#171d48', color: '#fff'}}
    }
  },
  {
    title: '实际产量(瓶)',
    dataIndex: 'currentYield',
    width: 100,
    customHeaderCell: () => {
      return {style: {backgroundColor: '#171d48', color: '#fff'}}
    }
  },
])
const tableData = ref([])
const tableLoading = ref<boolean>(false)

function setCustomHeaderRow(columns, index) {
  // columns.forEach(i => {
  //   i.customHeaderCell = () => {
  //     return { style: { backgroundColor: '#171d48', color: '#fff' } }
  //   }
  // })
}

function setRowClassName(record, index) {
  // console.log(record, 'record')
  return 'ant-table-row-bgc'
}

onMounted(() => {
  getListData()
})

const getListData = async () => {
  try {
    tableLoading.value = true;
    let dic = {
      pageNo: 1,
      pageSize: 999,
      ymd: dayjs().format('YYYY-MM-DD') + ' 00:00:00',
      // ymd: '2024-06-22 00:00:00',
      orderField: 'realStartTime',
      orderType: 'desc'
    }
    const res = await PLAN_PLAN_QUERYPAGELIST(dic);
    if (res.code === 200) {
      let result = res.result;
      tableData.value = result.records;
    } else {
      message.warning(res.message);
    }
  } catch (e) {
    console.log(e);
    message.error('网络错误，请联系管理员');
  } finally {
    tableLoading.value = false;
  }
};
</script>

<style scoped>
.title {
  text-align: center;
  color: #fff;
  font-size: 34px;
  font-weight: bold;
  margin: 2% 0;
}

:deep(.ant-table-row-bgc) {
  background-color: #181d45;
  color: #fff;
}

:deep(table tbody tr:hover > td) {
  background-color: #fff !important;
  color: #000 !important;
}
</style>
