<template>
  <BasicModal @register="registerModal" v-bind="$attrs" :title="title" :width="width"
              showOkBtn="false" showCancelBtn="true">
    <BasicTable @register="registerTable">

      <!--插槽:表格内容-->
      <template #bodyCell="{ column, record, index }">
        <div v-if="column.dataIndex.length > 15" class="box">
          <div v-if="record[column.dataIndex].type == '4' || record[column.dataIndex].type == '5'">
            <a-button type="link" @click="showDetail(record[column.dataIndex].value)">查看</a-button>
          </div>
          <div v-else>
            {{record[column.dataIndex].value}}

          </div>
        </div>
      </template>
    </BasicTable>
  </BasicModal>
</template>

<script lang="ts" setup>
import {ref} from "vue";
import { BasicTable } from '/@/components/Table';
import {useI18n} from "@/hooks/web/useI18n";
import {BasicModal, useModalInner} from "@/components/Modal";
import {useListPage} from "@/hooks/system/useListPage";
import {
  factoryPath,
  viewList
} from "@/views/dataCalculation/periodCheck/periodPlan/PeriodScheduleList.api";
import {
  columns,
  viewColumns
} from "@/views/dataCalculation/periodCheck/periodPlan/PeriodScheduleList.data";
import {cloneDeep} from "lodash-es";
import {encryptByBase64} from "@/utils/cipher";
import {useGlobSetting} from "@/hooks/setting";
import {useUserStore} from "@/store/modules/user";

const userStore = useUserStore();
const globSetting = useGlobSetting();

const disabledRef = ref(false);
const title = ref<string>('历史记录');
const width = ref<number>(1200);
const tableData = ref([]);

const { t } = useI18n();
const record = ref();
const emit = defineEmits(['success']);
//注册modal
const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data: any) => {
  record.value = data.record
  console.log(record.value)
  setModalProps({ confirmLoading: false, showCancelBtn: true, showOkBtn: false });
});


// 列表页面公共参数、方法
const { tableContext } = useListPage({
  tableProps: {
    api: viewList,
    columns: viewColumns,
    showIndexColumn:false,
    showTableSetting: false,
    showActionColumn: false,
    beforeFetch: (params) => {
      params.column = 'checkDate';
      params.order = 'desc';
      params.pageSize = 60;
      params.id = record.value;
      return params;
    },
    afterFetch: (records) =>{
      setShowPagination(false);
      console.log(records)
      let tableColumn = cloneDeep(viewColumns);
      //构造表格列头
      let weekArray = JSON.parse(records[0].dataJson);
      console.log(records[0].dataJson)
      weekArray.forEach(function (value, index) {
        let weekColumn = {
          title: '',
          dataIndex: '',
          key: '',
          width: '100',
          fixed: 'center',
        };

        weekColumn.title = value.name;
        weekColumn.dataIndex = value.id;
        weekColumn.key = value.id;
        console.log(weekColumn)
        tableColumn.push(weekColumn);
      });
      setColumns(tableColumn);
      //封装表格的值
      let weekData = loadTableData(records);
      console.log(weekData)
      return weekData;
    }
  },

});
//BasicTable绑定注册
const [registerTable ,{ reload,setShowPagination,setColumns, setTableData }] = tableContext;

function loadTableData(records){
  //封装表格的值
  let weekData = [];
  records.forEach(function (values, index) {
    let weekTable = {};
    let weeks = JSON.parse(values.dataJson);
    weeks.forEach(function (value, index) {
      let stat_name = value.id;
      weekTable[stat_name] = value;
    });
    const mergedObj = Object.assign( values, weekTable);
    weekData.push(mergedObj);
  });
  console.log(weekData)
  return weekData;
}

async function showDetail(record) {
  // let url = globSetting.domainUrl
  //console.log(url)

    let ip = await factoryPath({factoryCode: userStore?.userInfo?.orgCode})
  let path = ip + 'keytech-boot/mes3_common/static/' + record;//要预览文件的访问地址
    console.log(path)
  window.open(ip + 'preview/onlinePreview?url=' + encodeURIComponent(encryptByBase64(path)), '_blank');
  // let path = '/keytech-boot/mes3_common/static/' + record;
  // window.open(path, '_blank');
}
</script>

<style scoped>

</style>
