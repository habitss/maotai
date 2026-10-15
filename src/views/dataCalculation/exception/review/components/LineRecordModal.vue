<template>
    <!--定义表格-->
    <BasicTable @register="registerTable">

    </BasicTable>

</template>

<script lang="ts" name="action.meeting-record" setup>
import { BasicTable } from '/@/components/Table';
import {list} from '../Review.api';
import { useListPage } from '/@/hooks/system/useListPage';
import {useMessage} from "@/hooks/web/useMessage";
import {useI18n} from "@/hooks/web/useI18n";
import {ref} from "vue";
import {columns} from "../Review.data";

const { t } = useI18n();
const props = defineProps({
  record: {
    type: Object,
    default: {}
  }
})

// 禁用
const disabledRef = ref(false);
const queryParams = ref();

// 列表页面公共参数、方法
const { tableContext } = useListPage({
  tableProps: {
    api: list,
    columns: columns,
    showTableSetting: false,
    showActionColumn: false,
    tableSetting: {fullScreen: false},
    pagination:false,
    beforeFetch: (params) => {
      params.type = 1;
      params.deptCode = props.record.deptCode;
      params.lineCode = props.record.lineCode;
      params.times = props.record.times;
      params.triggerConditions = props.record.triggerConditions;
      console.log('params',params)
      return Object.assign(params, queryParams.value);
    },
  }
});

//BasicTable绑定注册
const [registerTable] = tableContext;


//消息提示
const { createMessage: msg } = useMessage();


</script>

<style scoped>

</style>
