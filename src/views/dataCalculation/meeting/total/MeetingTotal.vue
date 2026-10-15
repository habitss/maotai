<template>
  <!--引用表格-->
  <BasicTable @register="registerTable">

  </BasicTable>
</template>

<script lang="ts" setup name="dataCalculation-meeting-total-list">
import { BasicTable } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage'
import { useMessage } from '/@/hooks/web/useMessage';
import {columns, searchFormSchema} from "./MeetingTotal.data";
import { useI18n } from '/@/hooks/web/useI18n';
import {getList} from "@/views/dataCalculation/meeting/total/MeetingTotal.api";
import {addFactoryDept} from "@/utils/common/compUtils";
import {useUserStore} from "@/store/modules/user";


const { t } = useI18n();
const { createMessage: msg } = useMessage();
const userInfo = useUserStore();
//注册table数据
const { tableContext} = useListPage({
  tableProps: {
    title: '会议开展情况统计',
    columns,
    api: getList,
    showActionColumn: false,
    formConfig: {
      schemas: searchFormSchema,
      showAdvancedButton: false,
    },
    beforeFetch: (params) => {
      params.type = 0;
      params.pageSize = 50;
      return addFactoryDept(params);
    },
  },

})

const [registerTable, { reload , setTableData, getForm }] = tableContext;

/**
 * 成功回调
 */
function handleSuccess() {
  reload();
}
</script>

<style scoped>

</style>
