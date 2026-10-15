<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable">
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
    <!-- 表单区域 -->
    <PlanOrderModal @register="registerModal"></PlanOrderModal>
  </div>
</template>

<script lang="ts" name="plan-planOrder" setup>
import { onMounted, reactive } from 'vue';
import { BasicTable, TableAction } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useListPage } from '/@/hooks/system/useListPage';
import PlanOrderModal from './components/PlanOrderModal.vue';
import { dynamicColumnsStart, dynamicColumnsRecord, dynamicColumnsRecipeType, dynamicColumnsEnd, dynamicColumnsCycleType, searchFormSchema } from './PlanOrder.data';
import { list } from './PlanOrder.api';
import dayjs from 'dayjs';
import { useRoute } from 'vue-router';
import { CheckItemTypeEnum } from '/@/enums/planEnum';
//获取路由
const route = useRoute();
//获取路由中的code参数
const code = route.params.code;
const queryParam = reactive<any>({});
//注册model
const [registerModal, { openModal }] = useModal();
//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    title: '',
    api: list,
    columns: dynamicColumnsStart,
    canResize: true,
    immediate: false,
    showTableSetting: false,
    formConfig: {
      schemas: searchFormSchema,
      autoSubmitOnEnter: true,
      showAdvancedButton: true,
      fieldMapToTime: [
        ['realStartTime', ['realStartTime_begin', 'crealStartTime_end'], 'YYYY-MM-DD'],
        ['realEndTime', ['realEndTime_begin', 'crealEndTime_end'], 'YYYY-MM-DD']
      ],
    },
    actionColumn: {
      width: 120,
      fixed: 'right'
    },
    beforeFetch: (params) => {
      params.taskName = params.taskName ? `*${params.taskName}*` : null;
      params.planStartTime_end = dayjs().format('YYYY-MM-DD HH:mm:ss');
      params.column = 'planStartTime';
      params.order = 'desc';
      if(code == CheckItemTypeEnum.TYPE_RECORD){
        queryParam.specialType = getForm().getFieldsValue().specialType ?? '0,1';
      }else if(code == CheckItemTypeEnum.TYPE_PLAN){
        // params.appSearchType = params.appSearchType ? params.appSearchType : '0,10,20';
      }
      return Object.assign(params, queryParam);
    },
  },
})

const [registerTable, { reload, getForm, setProps }] = tableContext

/**
 * 填报信息
*/
function handleDetail(record: Recordable) {
  openModal(true, {
    record
  });
}
/**
 * 操作栏
 */
function getTableAction(record) {
  return [
    {
      label: '填报信息',
      onClick: handleDetail.bind(null, record),
    }
  ]
}

function reloadData(parentData, taskItemName) {
  setProps({
    columns: [...dynamicColumnsStart, ...dynamicColumnsRecipeType, ...dynamicColumnsEnd],
  });
  getForm().setFieldsValue({
    taskItemName: taskItemName,
  });
  //解析工单设备
  let deviceIds = parentData.deviceIds.split(',');
  let deviceIdsVal = parentData.deviceIdsVal.split(',');
  let deviceOptions = [] as any;
  deviceIds.forEach((item, index) => {
    deviceOptions.push({
      label: deviceIdsVal[index],
      value: item,
    });
  });
  //设置设备查询条件
  getForm().updateSchema([
    {
      label: '设备',
      field: 'deviceId',
      component: 'Select',
      componentProps: {
        options: deviceOptions,
        mode: 'multiple',
        showSearch: true,
        optionFilterProp: 'label',
      }
    }
  ])
  
  queryParam.planId = parentData.id;
  reload();
}

onMounted(async () => {
  if(code == CheckItemTypeEnum.TYPE_RECORD){
    setProps({
      columns: [...dynamicColumnsStart, ...dynamicColumnsRecord, ...dynamicColumnsEnd],
    });
    await getForm().appendSchemaByField(
      {
        label: '产线',
        field: 'lineCode',
        component: 'KtDepartSelect',
        componentProps: ({ formModel }) => {
          return {
            selectType: 'line',
            getPopupContainer: () => document.body,
            parentKey: formModel.deptCode,
            onChange: async e => {
              formModel.deviceId = null;
            }
          }
        },
      },
      '', 
      true
    );
    await getForm().appendSchemaByField(
      {
        label: '部门',
        field: 'deptCode',
        component: 'KtDepartSelect',
        componentProps: ({ formModel }) => {
          return {
            selectType: 'depart',
            onChange: async e => {
              formModel.lineCode = null;
              formModel.deviceId = null;
            }
          }
        },
      },
      '', 
      true
    );
    await getForm().appendSchemaByField(
      {
        label: '交接班类型',
        field: 'specialType',
        component: 'Select',
        componentProps:  {
          options: [{label: '接班', value: '0'}, {label: '交班', value: '1'}]
        },
      },
      '', 
    );
    await getForm().updateSchema({
      field: 'deviceId',
      component: 'JDictSelectTag',
      componentProps: ({ formModel }) => {
        return {
          showChooseOption: false,
          dictCode: "mes3_mdm_device where del_flag = 0 and line_code = '" + formModel.lineCode + "' order by sort, device_name, id",
          stringToNumber: true,
        }
      },
    });
    await getForm().removeSchemaByFiled(['recipeTypeCode', 'recipeSubTypeCode']);
    reload();
  } else {
    setProps({
      columns: [...dynamicColumnsStart, ...dynamicColumnsCycleType, ...dynamicColumnsEnd],
    });
    await getForm().appendSchemaByField(
      {
        label: '产线',
        field: 'lineCode',
        component: 'KtDepartSelect',
        componentProps: ({ formModel }) => {
          return {
            selectType: 'line',
            getPopupContainer: () => document.body,
            parentKey: formModel.deptCode,
            onChange: async e => {
              formModel.deviceId = null;
            }
          }
        },
      },
      '', 
      true
    );
    await getForm().appendSchemaByField(
      {
        label: '部门',
        field: 'deptCode',
        component: 'KtDepartSelect',
        componentProps: ({ formModel }) => {
          return {
            selectType: 'depart',
            onChange: async e => {
              formModel.lineCode = null;
              formModel.deviceId = null;
            }
          }
        },
      },
      '', 
      true
    );
    await getForm().appendSchemaByField(
      {
        label: '任务种类',
        field: 'appSearchType',
        component: 'Select',
        componentProps: () => {
          return {
            options: [{label: '循环任务', value: '2'}, {label: '自动触发任务', value: '10'}, {label: '手动触发任务', value: '20'}]
          }
        },
      },
      '', 
    );
    await getForm().updateSchema({
      field: 'deviceId',
      component: 'JDictSelectTag',
      componentProps: ({ formModel }) => {
        return {
          showChooseOption: false,
          dictCode: "mes3_mdm_device where del_flag = 0 and line_code = '" + formModel.lineCode + "' order by sort, device_name, id",
          stringToNumber: true,
        }
      },
    });
    await getForm().removeSchemaByFiled(['recipeTypeCode', 'recipeSubTypeCode']);
    reload();
  }
})

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