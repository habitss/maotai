<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable">
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
      <!--字段回显插槽-->
      <template v-slot:bodyCell="">
      </template>

      <!--查询表单-年月插槽-->
      <template #form-yearMon="{ model, field }">
        <KtMonthsPicker v-model:value="model[field]" :maxTagCount="2" />
      </template>
      
      <!-- 行动计划插槽 -->
      <template #ifTrack="{ record }">
        <a-button type="link" @click="handleActionClick(1, record)">添加</a-button>
        <a-button type="link" @click="handleActionClick(2, record)">查看</a-button>
      </template>
    </BasicTable>
    <!-- 表单区域 -->
    <BestPracticePromotionLogModal @register="registerModal" @success="handleSuccess"></BestPracticePromotionLogModal>
      
    <!-- 行动计划 -->
    <RaModal @register="registerActionModal" @success="onSuccess"></RaModal>
  </div>
</template>

<script lang="ts" name="practice-bestPracticePromotionLog" setup>
import { reactive } from 'vue';
import { BasicTable, TableAction } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useListPage } from '/@/hooks/system/useListPage'
import BestPracticePromotionLogModal from './components/BestPracticePromotionLogModal.vue'
import { columns, searchFormSchema } from './BestPracticePromotionLog.data';
import { list, getImportUrl, getExportUrl } from './BestPracticePromotionLog.api';
import RaModal from '/@/views/dataCalculation/actionPlan/ra/components/RaModal.vue';
import KtMonthsPicker from '/@/components/KtMonthsPicker/KtMonthsPicker.vue';
import { useUserStore } from '/@/store/modules/user';
import dayjs from 'dayjs';
const queryParam = reactive<any>({});
//获取用户信息
const { userInfo } = useUserStore();
//注册model
const [registerModal, { openModal }] = useModal();
//注册行动计划modal
const [registerActionModal, { openModal: openActionModal }] = useModal();
//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    title: '最佳实践推广记录',
    api: list,
    columns,
    formConfig: {
      //labelWidth: 120,
      schemas: searchFormSchema,
      autoSubmitOnEnter: true,
      showAdvancedButton: true,
      fieldMapToNumber: [
      ],
      fieldMapToTime: [
      ],
    },
    actionColumn: {
      width: 60,
      fixed: 'right'
    },
    beforeFetch: (params) => {
      params.pageSize = 9999;
      params.status = 1;
      return Object.assign(params, queryParam);
    },
    afterFetch: (res) => {
      //合并行数据，此处为合并相同proId的记录
      try {
        let proId = '';
        //需要合并的行数
        let rowSpan = 1;
        res.forEach((item, index) => {
          if (proId != item.proId) {
            //如果proId不同，则合并之前的行数，并重置rowSpan和proId
            if (index != 0) {
              res[(index - rowSpan)].rowSpan = rowSpan;
            }
            proId = item.proId
            rowSpan = 1;
          } else {
            //如果proId相同，则rowSpan加1,并判断是否为最后一行，如果是最后一行，则需要合并之前的行数
            rowSpan++;
            res[index].rowSpan = 0;
            if (index == res.length - 1) {
              res[(index - (rowSpan-1))].rowSpan = rowSpan;
            }
          }
        })
        return res;
      } catch (error) {
        console.log('error :>> ', error);
      }
    },
    pagination: false
  },
  exportConfig: {
    name: "最佳实践推广记录",
    url: getExportUrl,
    params: queryParam,
  },
  importConfig: {
    url: getImportUrl,
    success: handleSuccess
  },
})

const [registerTable, { reload }, { selectedRowKeys }] = tableContext
/**
 * 编辑事件
 */
function handleEdit(record: Recordable) {
  openModal(true, {
    record,
    isUpdate: true,
    showFooter: true,
  });
}
/**
 * 成功回调
 */
function handleSuccess() {
  (selectedRowKeys.value = []) && reload();
}
/**
   * 操作栏
   */
function getTableAction(record) {
  return [
    {
      label: '编辑',
      onClick: handleEdit.bind(null, record),
      auth: 'practice:mes3_best_practice_promotion_log:edit'
    }
  ]
}

//行动计划维护点击事件
function handleActionClick(type, record) {
  openActionModal(true, {
    record: {
      dataId: record.id,
      raiseDate: dayjs().format('YYYY-MM-DD'),
      status: 1,
      createDept: userInfo?.orgCode,
      putUserId: userInfo?.id,
      verifyUserId: userInfo?.id,
      planFinishDate: dayjs().add(1, 'day').format('YYYY-MM-DD'),
      actionLevel: '1',
      enterMeeting: '0',
    },
    type,
    dataId: record.id,
  });
}
</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}
</style>