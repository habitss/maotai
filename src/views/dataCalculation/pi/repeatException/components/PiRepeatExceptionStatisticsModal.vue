<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :destroyOnClose="true" :title="title" :width="'84vw'">
    <!--引用表格-->
    <BasicTable @register="registerTable">
    </BasicTable>
    <div style="padding: 0 10px 10px;">
      <Bar style="background-color: #fff; height: 500px" class="chartPart" :chartData="anomalyRepeatRateArr" :option="barOption"></Bar>
    </div>
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { BasicTable } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { listAll } from '../PiRepeatExceptionType.api';
import Bar from '/@/components/chart/Bar.vue';
const title = ref('');
//柱状图数据
const anomalyRepeatRateArr = ref([]);

//柱状图配置
const barOption = {
  title: {
    text: '重复异常率',
    left: 'center',
    top: '20px'
  },
  grid: {
    show: false,
    top: '12%',
    right: '4%',
    bottom: '12%',
    left: '4%'
  },
  series: [
    {
      name: '重复异常率',
      type: 'bar',
      label: {
        show: true,
        position: 'top',
        color: '#000',
      }
    },
  ],
};

//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    title: '',
    immediate: false,
    canResize: true,
    pagination: false,
    showActionColumn: false,
    showTableSetting: false,
    ellipsis: false,
  },
})

const [registerTable, { setProps, setLoading }, {  }] = tableContext;

const [registerModal, { setModalProps }] = useModalInner(async (data) => {
  setLoading(true);
  setModalProps({ showCancelBtn: false, showOkBtn: false });
  anomalyRepeatRateArr.value = [];
  //设置标题
  title.value = data.record.fieldName+'-重复异常类型统计';
  //获取统计数据
  const anomalyTypeMap = data.record.anomalyTypeMap
  //获取当前PI的所有重复异常配置数据
  const res = await listAll({
    piTableCode: data.record.tableCode,
    piFieldCode: data.record.fieldCode,
  });
  //表格数据
  let tableData = {
    piName: data.record.fieldName,
  } as any;
  //列数据
  let columnArr = [{
    title: 'PI名称',
    align: "center",
    dataIndex: 'piName',
    width:160,
    fixed: 'left'
  }] as any;
  //创建柱状图数据
  let barArr = [] as any;
  //将统计数据添加到重复异常配置数据中
  res.forEach(item => {
    let total = anomalyTypeMap['dict_'+item.id] ?? 0;

    columnArr.push({
      title: item.exceptionName,
      align: "center",
      dataIndex: item.id,
      width:80,
    });

    tableData[item.id] = total;

    barArr.push({
      name: item.exceptionName,
      value: total
    });
  });
  //排序
  anomalyRepeatRateArr.value = barArr.slice().sort((a, b) => b.value - a.value);
  setProps({
    dataSource: [tableData],
    columns: columnArr
  });
  setLoading(false);
});
</script>

<style lang="less" scoped>
/** 时间和数字输入框样式 */
:deep(.ant-input-number) {
  width: 100%;
}

:deep(.ant-calendar-picker) {
  width: 100%;
}
</style>