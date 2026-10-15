<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose :title="title" :width="1000" @ok="handleSubmit">
    <a-form-model :model="form">
      <table class="table">
        <tbody>
          <tr>
            <th colspan="5" style="text-align: center;"> 知识共享最佳实践推荐表</th>
          </tr>
          <tr>
            <td style="width: 120px;">建议名称</td>
            <td colspan="4">{{ dataRecord.title }} </td>
          </tr>
          <tr>
            <td>建议类型</td>
            <td colspan="5">
              {{ dataRecord.improvementType_dictText }}
            </td>
          </tr>
          <tr>
            <td></td>
            <td>提报员工</td>
            <td>{{ dataRecord.userId_dictText }}</td>
            <td>提报班组</td>
            <td>{{ dataRecord.shiftGroupId_dictText }}</td>
          </tr>
          <tr>
            <td></td>
            <td style="width: 120px;">提报部门</td>
            <td>{{ dataRecord.reportDeptId_dictText }}</td>
            <td style="width: 120px;">提报工厂</td>
            <td>{{ dataRecord.factoryCode_dictText }}</td>
          </tr>
          <tr>
            <td rowspan="2"> 内容描述</td>
            <td colspan="4"><a-textarea v-model:value="form.improveDescription" placeholder="改善前描述" /></td>
          </tr>
          <tr>
            <td colspan="4"> <a-textarea v-model:value="form.improveMeasure" placeholder="改善措施" /></td>
          </tr>
          <tr>
            <td>附件上传</td>
            <td colspan="4">
              <JUpload ref="uploadRef" v-model:value="form.improveFile" :maxCount="5" />
            </td>
          </tr>
          <tr>
            <td colspan="5" style="padding: 0;">
              <BasicTable @register="registerTable">
                <template #bodyCell="{ column, record }">
                  <a-textarea v-if="column.dataIndex != 'title' && column.dataIndex != 'improvementTypeName'"
                    v-model:value="record[column.dataIndex]" :placeholder="`请输入${column.customTitle}`" />
                </template>
              </BasicTable>
            </td>
          </tr>
          <tr>
            <td>
              推荐人意见
            </td>
            <td colspan="4"> <a-textarea v-model:value="form.opinion" placeholder="推荐人意见" /></td>
          </tr>
        </tbody>
      </table>
    </a-form-model>
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicTable } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage'
import { recommendColumns } from '../BestPracticeFill.data';
import { queryeRcommendByFillId, saveOrUpdateRecommend } from '../BestPracticeFill.api';
import { itemList } from '/@/views/dataCalculation/mdm/dict/dict.api';
import JUpload from '/@/components/Form/src/jeecg/components/JUpload/JUpload.vue';
const isUpdate = ref(true);
// Emits声明
const emit = defineEmits(['register', 'success']);
//设置标题
const title = '知识共享最佳实践推荐表';
//表单数据
const dataRecord = ref({} as any)
//表单数据
const form = ref({} as any)
//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    columns: recommendColumns,
    canResize: false,
    showActionColumn: false,
    showTableSetting: false,
    pagination: false,
  },
})

const [registerTable, { setTableData, getDataSource }] = tableContext

//表单赋值
const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
  dataRecord.value = data?.record || {};
  //获取推荐表数据
  const rcommend = await queryeRcommendByFillId({fillId: data.record?.id});
  isUpdate.value = rcommend ? true : false;
  form.value = rcommend || {};
  form.value.fillId = data.record?.id;

  //推荐表详细数据
  let detailList = rcommend?.detailList || [];

  if(detailList.length > 0){
    //如果有详细数据直接复用
    detailList[0].title = "改善效果";
    detailList[0].rowSpan = detailList.length;
  } else {
    //获取改善效果
    const { records, total } = await itemList({ pageSize: 100, groupCode: 'MES3_BEST_EFFECT' });
    //根据改善效果设置行数据
    for (const item of records) {
      detailList.push({
        "title": "改善效果",
        "improvementTypeId": item.itemVal,
        "improvementTypeName": item.itemKey,
        "rowSpan": total,
        "recommendId": form.value.id,
      });
    }
  }
  setTableData(detailList);
});

//表单提交事件
async function handleSubmit() {
  try {
    form.value.detailList = getDataSource();
    //提交表单
    await saveOrUpdateRecommend(form.value, isUpdate.value);
    //关闭弹窗
    closeModal();
    emit('success');
  } catch ({ errorFields }) {
    if (errorFields) {
      const firstField = errorFields[0];
      if (firstField) {
        // scrollToField(firstField.name, { behavior: 'smooth', block: 'center' });
      }
    }
    return Promise.reject(errorFields);
  } finally {
    setModalProps({ confirmLoading: false });
  }
}
</script>

<style lang="less" scoped>
/** 时间和数字输入框样式 */
:deep(.ant-input-number) {
  width: 100%;
}

:deep(.ant-calendar-picker) {
  width: 100%;
}

.table {
  width: 100%;
}

.table th,
.table td {
  border: 1px solid #eee;
  padding: 10px;
}

.jeecg-basic-table-form-container {
  padding: 0;
}
</style>