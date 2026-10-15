<template>
  <BasicDrawer v-bind="$attrs" @register="registerDrawer" title="算法配置" width="100vw" 
    :destroy-on-close="true" 
    :showFooter="true"
    :maskClosable="false"
    :keyboard="false"
    :ok-text="'保存'"
    @ok=handleOk
  >
    <a-spin :spinning="confirmLoading" class="span-box">
        <a-row :gutter="[16, 16]">
          <!--**************取数来源********************-->
          <a-col :span="24">
            <a-card title="取数来源" :headStyle="headStyle" :bodyStyle="bodyStyle">
              <a-form
                :model="baseInfo"
                name="basic"
                :label-col="{ style: { width: '100px' } }"
                :wrapper-col="{ span: 22 }"
                autocomplete="off"
              >
                <a-row>
                  
                  <a-col :span="baseFormSpan">
                    <a-form-item
                      label="工厂"
                      name="factoryCode"
                    >
                      <!-- <KtDepartSelect 
                        v-model:value="baseInfo.factoryCode"
                        select-type="factory"
                        placeholder="请选择工厂"
                        mode="multiple"
                        :getPopupContainer="() => getPopupContainer()"
                      >
                      </KtDepartSelect> -->
                      <KtDepartTreeSelect 
                        v-model:value="baseInfo.factoryCode"
                        multiple
                        :allowClear="true" 
                        :defaultExpandLevel="1" 
                        :byUserOrg="true"
                        :placeholder="'请选择工厂'"
                        :fieldNames=" { label:'title', value: 'orgCode' }">
                      </KtDepartTreeSelect>
                    </a-form-item>
                  </a-col>
                  
                  <a-col :span="baseFormSpan">
                    <a-form-item
                      label="取数表"
                      name="dataTableName"
                    >
                      <JDictSelectTag v-model:value="baseInfo.dataTableName" placeholder="请选择取数表"
                        :getPopupContainer="() => getPopupContainer()"
                        :showChooseOption=false allow-clear
                        dictCode="mes3_mdm_df_table_info, df_table_desc, df_table_name, del_flag = 0"
                        style="margin-top: 4px; text-align: left;" 
                        @change="handleDataTableNameChange"/>
                    </a-form-item>
                  </a-col>
                  
                  <a-col :span="baseFormSpan">
                    <a-form-item
                      label="取数时间字段"
                      name="dataFieldName"
                    >
                      <a-select
                        v-model:value="baseInfo.dataFieldName"
                        placeholder="请选择取数时间字段"
                        style="margin-top: 4px; text-align: left;"
                        allow-clear
                        :options="dfTimeFielList"
                        :filterOption = "(input, option) => filterOption(input, option, {label:'dfFieldDesc', value: 'dfFieldName' })"
                        :fieldNames= "{label:'dfFieldDesc', value: 'dfFieldName' }"
                        optionFilterProp="dfFieldDesc"
                        showSearch
                      ></a-select>
                    </a-form-item>
                  </a-col>
                  
                  <a-col :span="baseFormSpan">
                    <a-form-item
                      label="指标标准"
                      name="indStand"
                    >
                      <JDictSelectTag v-model:value="baseInfo.indStand" placeholder="请选择指标标准"
                        :getPopupContainer="() => getPopupContainer()"
                        :showChooseOption=false allow-clear
                        dictCode="mes3_mdm_df_ind_stand, ind_name, ind_stand_code, enable_flag = 'Y' order by ind_stand_code"
                        style="margin-top: 4px; text-align: left;"
                        showSearch
                        @change="handlIndStandChange"/>
                    </a-form-item>
                  </a-col>
                  
                  <a-col :span="baseFormSpan">
                    <a-form-item
                      label="实例化指标"
                      name="indicator"
                    >
                      <a-select
                        v-model:value="baseInfo.indicator"
                        placeholder="请选择实例化指标字段"
                        style="margin-top: 4px; text-align: left;"
                        allow-clear
                        :options="indicatorList"
                        :filterOption = "(input, option) => filterOption(input, option, {label:'funlocDesc', value: 'instUseCode' })"
                        :fieldNames= "{label:'funlocDesc', value: 'instUseCode' }"
                        showSearch
                        mode="multiple"
                      ></a-select>
                    </a-form-item>
                  </a-col>
                  
                  <a-col :span="baseFormSpan">
                    <a-form-item
                      label="指标字段"
                      name="indicatorFieldName"
                    >
                      <a-select
                        v-model:value="baseInfo.indicatorFieldName"
                        placeholder="请选择指标字段"
                        style="margin-top: 4px; text-align: left;"
                        allow-clear
                        :options="dfFielList"
                        :filterOption = "(input, option) => filterOption(input, option, {label:'dfFieldDesc', value: 'dfFieldName' })"
                        :fieldNames= "{label:'dfFieldDesc', value: 'dfFieldName' }"
                        optionFilterProp="dfFieldDesc"
                        showSearch
                      ></a-select>
                    </a-form-item>
                  </a-col>
                </a-row>
              </a-form>
            </a-card>
          </a-col>
          
          <!--**************唯一约束********************-->
          <a-col :span="24">
            <a-card title="唯一约束" :headStyle="headStyle" :bodyStyle="bodyStyle">
              <BasicTable @register="registerTable">
                <template #tableTitle>
                  <a-button type="primary" @click="handleDfUniqueAdd" preIcon="ant-design:plus-outlined">新增</a-button>
                </template>
                <!--操作栏-->
                <template #action="{ record }">
                  <TableAction :actions="getTableAction(record)" />
                </template>
                
                <!--字段回显插槽-->
                <template v-slot:bodyCell="{ column, record }">
                  <a-select
                    v-if="column.dataIndex == 'piFieldName'"
                    v-model:value="record.piFieldName"
                    placeholder="请选择PI字段字段"
                    style="margin-top: 4px; text-align: left;"
                    allow-clear
                    :options="piFieldList"
                    :filterOption = "(input, option) => filterOption(input, option, {label:'name', value: 'fieldName' })"
                    :fieldNames= "{label:'name', value: 'fieldName' }"
                    showSearch
                  ></a-select>
                  <a-select
                    v-if="column.dataIndex == 'dfFieldName'"
                    v-model:value="record.dfFieldName"
                    placeholder="请选择DF字段字段"
                    style="margin-top: 4px; text-align: left;"
                    allow-clear
                    :options="dfFielList"
                    :filterOption = "(input, option) => filterOption(input, option, {label:'dfFieldDesc', value: 'dfFieldName' })"
                    :fieldNames= "{label:'dfFieldDesc', value: 'dfFieldName' }"
                    optionFilterProp="dfFieldDesc"
                    showSearch
                  ></a-select>
                </template>
              </BasicTable>
            </a-card>
          </a-col>
          
          <!--**************计算模型********************-->
          <a-col :span="24">
            <a-card title="计算模型" :headStyle="headStyle" :bodyStyle="bodyStyle">
              <PiDfAlgorithmCalcList ref="piDfAlgorithmCalcListRef"></PiDfAlgorithmCalcList>
            </a-card>
          </a-col>
        </a-row>
    </a-spin>
  </BasicDrawer>
</template>
<script lang="ts" setup>
import { ref } from 'vue';
import { BasicDrawer, useDrawerInner } from '@/components/Drawer';
import KtDepartTreeSelect from '/@/components/KtDepartSelect/KtDepartTree.vue';
import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';
import { BasicTable, TableAction } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage';
import { listUnique, deleteOneUnique, saveOrUpdate } from '../PiDfAlgorithm.api';
import { list as queryDfFieldList } from '/@/views/df/mdm/DfTableFieldInfo.api';
import { list as queryPiFieldList } from '/@/views/dataCalculation/pi/customizeTable/PiCustomizeTableField.api';
import { queryIndicatorDic } from '/@/views/df/mdm/DfMdmIndicator.api';
import { getPopupContainer, filterOption } from '/@/utils';
import { buildUUID } from '/@/utils/uuid';
import { useMessage } from "/@/hooks/web/useMessage";
import { cloneDeep } from 'lodash-es';
import PiDfAlgorithmCalcList from './PiDfAlgorithmCalcList.vue';
const baseFormSpan = 8;
//消息提示组件
const { createMessage } = useMessage();
// Emits声明
const emit = defineEmits(['register', 'success']);
//窗体加载状态
const confirmLoading = ref(true);
//卡片组件标题区域样式
const headStyle = ref({
  minHeight: '32px',
  padding: '5px',
  fontSize: '14px',
});
//卡片组件内容区域样式
const bodyStyle = ref({
  padding: '5px',
  overflow: 'auto',
});
//计算模型ref对象
const piDfAlgorithmCalcListRef = ref();
//算法基本配置
const baseInfo = ref({
  factoryCode: '',
} as any);
//历史取数表，用于判断是否更换表
const hisDataTableName = ref('');
//实例化指标数据
const indicatorList = ref([]);
//pi表字段
const piFieldList = ref([]);
//pi表唯一约束字段
const piUniqueFieldList = ref([]);
//取数表字段
const dfFielList = ref([]);
//取数表时间字段
const dfTimeFielList = ref([]);
//注册唯一约束table数据
const { tableContext } = useListPage({
  tableProps: {
    api: listUnique,
    showTableSetting: false,
    immediate: false,
    pagination: false,
    scroll: { y: 300 },
    columns: [
      { title: 'PI字段', dataIndex: 'piFieldName' },
      { title: 'DF字段', dataIndex: 'dfFieldName' },
    ],
    actionColumn: {
      width: 120,
      fixed: 'right'
    },
    beforeFetch: (params) => {
      params.dataId = baseInfo.value.id;
      return params;
    },
  },
})
const [registerTable, { reload, setTableData, getDataSource, insertTableDataRecord, deleteTableDataRecord }] = tableContext

//注册抽屉
const [registerDrawer, { closeDrawer, changeOkLoading }] = useDrawerInner(async (data) => {
  confirmLoading.value = true;
  try{
    //先清空再赋值避免缓存
    baseInfo.value = {};
    baseInfo.value = cloneDeep(data.baseInfo) ?? {};
    hisDataTableName.value = '';
    hisDataTableName.value = data.baseInfo.dataTableName;
    if(baseInfo.value.piTableName){
      //获取pi表字段数据
      let piRes = await queryPiFieldList({pageSize: 1000, tableName: baseInfo.value.piTableName});
      piFieldList.value = piRes.records;
      //获取pi表唯一约束字段数据
      piUniqueFieldList.value = piRes.records.filter(item => item.isUnique == 1);
      //刷新唯一约束配置表数据
      if(baseInfo.value.id && baseInfo.value.dataTableName){
        await handleDataTableNameChange(baseInfo.value.dataTableName);
      }
    }

    if(data.baseInfo.indStand){
      await handlIndStandChange(data.baseInfo.indStand);
      baseInfo.value.indicator = data?.baseInfo?.indicator?.split(',');
    }
    confirmLoading.value = false;
  } catch (error) {
    console.log('error :>> ', error);
  }
});

/**
 * df取数表改变事件
 * @param e 
 */
async function handleDataTableNameChange(e){
  //清空唯一约束列表
  setTableData([]);
  //清空计算模型列表
  piDfAlgorithmCalcListRef.value.setTableData([]);

  //获取df表字段数据
  const { records } = await queryDfFieldList({pageSize: 1000, dfTableName: e});
  dfFielList.value = records;
  dfTimeFielList.value = records.filter(item => item.dfFieldType === 'timestamp');
  
  //计算模型初始化数据
  piDfAlgorithmCalcListRef.value.initData(baseInfo.value, piFieldList.value, dfFielList.value);

  //如果取数表没有改变，则刷新唯一约束配置表数据
  if(e == hisDataTableName.value){
    await reload();
    await piDfAlgorithmCalcListRef.value.reload();
  } else {
    baseInfo.value.dataFieldName = null;
    //PI表有唯一约束字段数据，将PI的唯一约束字段设置到唯一约束配置中
    if(piUniqueFieldList.value && piUniqueFieldList.value.length > 0){
      for(let item of piUniqueFieldList.value){
        await insertTableDataRecord({
          id: 'temp_' + buildUUID(),
          dfTableName: baseInfo.value.dataTableName,
          dfFieldName: null,
          piTableName: item.tableName,
          piFieldName: item.fieldName,
        })
      }
    }
  }
  
}
/**
 * 指标标准改变事件
 * @param e 
 */
async function handlIndStandChange(e){
  delete baseInfo.value.indicator;
  //获取实例化指标数据
  indicatorList.value = await queryIndicatorDic({indStandCode: e ?? '-1'});
}
/**
   * 操作栏
   */
function getTableAction(record) {
  return [
    {
      label: '删除',
      popConfirm: {
        title: '是否确认删除',
        confirm: handleDfUniqueDelete.bind(null, record),
        placement: 'topLeft',
      },
    }
  ]
}

//新增唯一约束
async function handleDfUniqueAdd(){
  if(baseInfo.value.dataTableName == null || baseInfo.value.dataTableName == ''){
    createMessage.warning('请选择取数表！');
    return;
  }
  await insertTableDataRecord({
    id: 'temp_' + buildUUID(),
    piTableName: baseInfo.value.piTableName,
    dfTableName: baseInfo.value.dataTableName,
    dfFieldName: null,
    dataId: baseInfo.value.id,
  })
}

//删除唯一约束
async function handleDfUniqueDelete(record){
  if(!record.id.includes('temp_')){
    await deleteOneUnique({id: record.id}, reload);
  }
  await deleteTableDataRecord(record.id);
}
//保存算法
async function handleOk(){
  try {
    changeOkLoading(true);
    //算法-基础信息
    let saveData = cloneDeep(baseInfo.value);
    if(Array.isArray(saveData?.factoryCode)){
      saveData.factoryCode =  saveData?.factoryCode?.join(',');
    }
    if(Array.isArray(saveData?.indicator)){
      saveData.indicator =  saveData?.indicator?.join(',');
    }
    //算法-唯一约束
    saveData.piDfAlgorithmUniqueList = getDataSource();
    saveData.isChangeDfTable = saveData.dataTableName != hisDataTableName.value ? true : false;
    //算法-模型
    saveData.piDfAlgorithmModelList = piDfAlgorithmCalcListRef.value.getDataSource();
    await saveOrUpdate(saveData, saveData.id);
    //关闭弹窗
    closeDrawer();
    //刷新列表
    emit('success');
  } catch (error) {
    console.log('error :>> ', error);
  } finally {
    changeOkLoading(false);
  }
}
</script>
<style lang="less" scoped>
/** 时间和数字输入框样式 */
:deep(.ant-input-number) {
  width: 100%
}

:deep(.ant-calendar-picker) {
  width: 100%
}

:deep(.ant-select-selection-placeholder) {
  color: #BEBEBE !important;
}

:deep(.jeecg-basic-table-form-container) {
  padding: 0;
  margin: 0;
}

.add-style {
  font-size: 18px;
  color: #1677ff;
  margin: 8px;
}

.del-style {
  font-size: 18px;
  color: red;
}
</style>