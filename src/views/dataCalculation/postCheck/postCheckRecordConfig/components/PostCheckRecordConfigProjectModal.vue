<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose title="岗位检查记录配置" width="80vw" :minHeight="600"
    @ok="handleSubmit" ok-text="保存">
    <!--引用表格-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button type="primary" v-auth="'post_check:mes3_post_check_record_config:add'" @click="handleAdd('', -1)"
          preIcon="ant-design:plus-outlined"> 新增</a-button>
        <a-button type="primary" v-auth="'post_check:mes3_post_check_record_config:exportXls'"
          preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
        <j-upload-button type="primary" v-auth="'post_check:mes3_post_check_record_config:importExcel'"
          preIcon="ant-design:import-outlined" @click="onImportXlsByParams">导入</j-upload-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                删除
              </a-menu-item>
            </a-menu>
          </template>
          <a-button v-auth="'post_check:mes3_post_check_record_config_main:deletionBatch'">批量操作
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
        <div class="title">{{ title }}</div>
      </template>

      <!--操作栏-->
      <template #action="{ record, index }">
        <TableAction :actions="getTableAction(record, index)" />
      </template>

      <!--插槽:单元格开始-->
      <template #step="{ record }">
        <a-textarea v-model:value="record.step" placeholder="步骤" />
      </template>

      <template #checkFrequency="{ record }">
        <a-input-number v-model:value="record.checkFrequency" :min="1" :precision="0" placeholder="项目检查次数" />
      </template>

      <template #frequencyIllustrate="{ record }">
        <a-textarea v-model:value="record.frequencyIllustrate" placeholder="标准检查频次说明" />
      </template>

      <template #frequencyIllustrateCount="{ record }">
        <a-input-number v-model:value="record.frequencyIllustrateCount" :min="1" :precision="0" placeholder="标准检查频次" />
      </template>

      <template #checkItem="{ record, index }">
        <div style="display: flex; align-items: center;">
          <a-textarea v-model:value="record.checkItem" placeholder="检查项目"
            style="width: calc(100% - 20px); margin-right: 4px;" />
          <span v-if="queryParam.type == 1"><a-icon @click="handleAdd(record, index)" type="plus-square" style="cursor: pointer;" /></span>
        </div>
      </template>

      <template #checkStandardDesc="{ record, index }">
        <div style="display: flex; align-items: center;">
          <a-textarea v-model:value="record.checkStandardDesc" placeholder="检查标准"
            style="width: calc(100% - 20px); margin-right: 4px;" />
          <span><a-icon @click="handleAdd(record, index)" type="plus-square" style="cursor: pointer;" /></span>
        </div>
      </template>

      <template #checkname="{ record }">
        <a-textarea v-model:value="record.checkname" :placeholder="queryParam.type == 2 ? '检查数据记录描述' : '检查标准'" />
      </template>

      <template #checknameFormat="{ record }">
        <a-select v-model:value="record.checknameFormat" placeholder="检查数据记录"
          :options="checknameFormatOptions" allowClear
          :getPopupContainer="(node) => getPopupContainer()"/>
      </template>

      <template #checknameIfmust="{ record }">
        <a-switch checked-children="是" :checkedValue=1 un-checked-children="否" :unCheckedValue=0 v-model:checked="record.checknameIfmust" />
      </template>

      <template #sort="{ record }">
        <a-input-number v-model:value="record.sort" :min="0" placeholder="排序" />
      </template>
      <!--插槽:单元格结束-->
    </BasicTable>
  </BasicModal>
</template>

<script lang="ts" setup>
import { reactive, ref } from 'vue';
import { getDictItems } from '@/api/common/api';
import { saveConfig, deleteConfig, batchDeleteConfig, queryConfigList, getExportConfigUrl, getImportConfigUrl } from '../PostCheckRecordConfigMain.api';
import { configColumns, setConfigReportType } from '../PostCheckRecordConfigMain.data';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicTable, TableAction } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage'
import { buildUUID } from '/@/utils/uuid';
import { getPopupContainer } from '/@/utils';
//查询参数对象
const queryParam = reactive<any>({});
const props = defineProps({
  propsReportType: String,
});

// Emits声明
const emit = defineEmits(['register', 'success']);

//横幅标题
const title = ref('');

//主表对象
const zbData = ref();

//导入使用参数
const zbId = ref();
const reportType = ref();

//检查数据记录下拉选项
const checknameFormatOptions = ref([]);

//表单赋值
const [registerModal, { setModalProps }] = useModalInner(async (data) => {
  selectedRowKeys.value = [];
  //获取下拉选项数据
  getDictItems("mes3_mdm_dict, item_key, item_val, del_flag = 0 and group_code = 'MES3_POST_CHECK_FIELD_TYPE'").then((res) => {
    checknameFormatOptions.value = res;
  });
  //设置报表类型下拉选项值
  setConfigReportType(data.record.reportType);
  //设置标题信息
  title.value = data.record.reportName || '';
  //赋值主表数据信息
  zbData.value = data.record;
  zbId.value = data.record.id;
  reportType.value = data.record.reportType;
  //设置查询参数对象信息
  queryParam.zbId = data.record.id;
  queryParam.type = data.record.reportType;
  queryParam.column = 'step';
  queryParam.order = 'asc';
});

//注册table数据
const { tableContext, onExportXls, onImportXlsByParams } = useListPage({
  tableProps: {
    title: '',
    api: queryConfigList,
    columns: configColumns,
    canResize: false,
    rowKey: 'bzid',
    actionColumn: {
      width: 80,
      fixed: 'right'
    },
    showTableSetting: false,
    pagination: false,
    scroll: { y: 432 },
    beforeFetch: (params) => {
      return Object.assign(params, queryParam);
    },
  },
  exportConfig: {
    name: props.propsReportType == '1' ? "通用岗位记录配置导出" : "岗位检查清单配置导出",
    url: getExportConfigUrl,
    params: queryParam,
  },
  importConfigByParams: {
    url: getImportConfigUrl,
    params: {
      zbId: zbId,
      reportType: reportType
    }
    // success: handleSuccess
  },
})

const [registerTable, { reload, getDataSource, insertTableDataRecord, deleteTableDataRecord, getSelectRows }, { rowSelection, selectedRowKeys }] = tableContext;

/**
   * 操作栏
   */
function getTableAction(record, index) {
  return [
    {
      label: '删除',
      popConfirm: {
        title: '是否确认删除',
        confirm: handleDelete.bind(null, record, index),
        placement: 'topLeft',
      },
      auth: 'post_check:mes3_post_check_record_config:delete'
    },
  ]
}
/**
 * 成功回调
 */
function handleSuccess() {
  (selectedRowKeys.value = []) && reload();
}

/**
 * 批量删除事件
 */
async function batchHandleDelete() {
  const selectData = getSelectRows().filter(item => !item.bzid.startsWith('temp'));
  if (!selectData.length) {
    handleSuccess();
    return;
  }
  // 提取所有的id
  const ids = selectData.map(item => item.id);
  // 使用Set去重
  const uniqueIds = [...new Set(ids)];
  // 提取所有的bzid
  const bzIds = selectData.map(item => item.bzid);
  await batchDeleteConfig({ ids: uniqueIds , bzIds: bzIds }, handleSuccess);
}

/**
 * 删除事件
 */
async function handleDelete(record, index) {
  //获取表格数据
  const tableData = getDataSource();
  try {
    if(!record.bzid.startsWith('temp_bz_')){
      let params = {
        id: record.rowSpan == 1 ? record.id : '',
        bzid: record.bzid
      }
      await deleteConfig(params);
    }
    //如果删除行的rowSpan大于1，说明是合并行的第一条，则将下一条数据变成合并行第一条
    if (record.rowSpan > 1) {
      tableData[index + 1].rowSpan = record.rowSpan - 1;
      tableData[index + 1].step = record.step;
      tableData[index + 1].checkFrequency = record.checkFrequency;
      tableData[index + 1].frequencyIllustrate = record.frequencyIllustrate;
      tableData[index + 1].frequencyIllustrateCount = record.frequencyIllustrateCount;
      tableData[index + 1].checkItem = record.checkItem;
    } else if (record.rowSpan === 0) {
      //如果删除行的rowSpan等于0，则找出合并行的第一条，修改需要合并的行数
      for (const item of tableData) {
        if (item.id === record.id && item.rowSpan > 1) {
          item.rowSpan--;
        }
      }
    }
    //改变当前数据id为临时id，便于触发删除事件
    record.bzid = 'del';
    deleteTableDataRecord('del');
  } catch (error) {
    console.log('error :>> ', error);
  } finally {
    setModalProps({ confirmLoading: false });
  }
}

//添加事件
async function handleAdd(record, index) {
  //record为空则为点击新增按钮，重新添加一行，不为空则为点击表格内图标，处理行合并
  if (record) {
    //获取表格所有数据
    const tableData = getDataSource();
    //添加行数据
    await insertTableDataRecord({
      id: record.id,
      bzid: 'temp_bz_' + buildUUID(),
      //点表格内图标添加设置为0，不渲染指定单元格，用于合并行
      rowSpan: 0,
    }, index + tableData[index].rowSpan);
    //行合并数+1
    tableData[index].rowSpan++;
  } else {
    await insertTableDataRecord({
      id: 'temp_' + buildUUID(),
      bzid: 'temp_bz_' + buildUUID(),
      rowSpan: 1,
    })
  }
}

//保存事件
async function handleSubmit() {
  try {
    //获取表格所有数据
    let values = initSaveData();
    setModalProps({ confirmLoading: true });
    //提交数据
    await saveConfig(values);
    //刷新表格
    reload();
    //刷新列表
    emit('success');
  } catch (error) {
    console.log('error :>> ', error);
  } finally {
    setModalProps({ confirmLoading: false });
  }
}

function initSaveData(){
  //获取表格所有数据
  const tableData = getDataSource();
  //初始化保存数据数组
  let saveConfigArr = [] as any;
  //遍历表格数据，组装保存数据格式
  for(const item of tableData){
    //组装检查项数据对象
    let jcxm = {
      id: item.id,
      zbId: zbData.value.id,
      factoryCode: zbData.value.factoryCode,
      deptCode: zbData.value.deptCode,
      lineCode: zbData.value.lineCode,
      step: item.step,
      checkFrequency: item.checkFrequency,
      frequencyIllustrate: item.frequencyIllustrate,
      frequencyIllustrateCount:item.frequencyIllustrateCount,
      checkItem: item.checkItem,
      checkStandardDesc: item.checkStandardDesc,
      listJcbz: [] as any
    };
    //组装检查标准数据对象
    let jcbz = {
      id: item.bzid.startsWith("temp_") ? undefined : item.bzid,
      checkname: item.checkname,
      checknameFormat: item.checknameFormat,
      checknameIfmust: item.checknameIfmust,
      sort: item.sort,
    };
    //判断检查项id是否已经存在，不存在则添加到保存数据数组中，存在则在对应检查项下添加检查标准
    let isExist = false;
    for(const saveConfig of saveConfigArr){
      if(saveConfig.id === item.id){
        saveConfig.listJcbz.push(jcbz);
        isExist = true;
        break;
      }
    }
    if(!isExist){
      jcxm.listJcbz.push(jcbz);
      saveConfigArr.push(jcxm);
    }
  }
  //删除临时id数据，提交保存
  saveConfigArr.map(item => {
    if(item.id.startsWith("temp_")) delete item['id']
  });
  return saveConfigArr;
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

:deep(.jeecg-basic-table-header__toolbar) {
  display: none;

}

.title {
  background: rgb(235, 241, 251);
  width: 100%;
  height: 36px;
  line-height: 36px;
  color: rgb(51, 51, 51);
  font-size: 20px;
  font-weight: bold;
  justify-content: center;
  margin-top: 12px;
  text-align: center;
}
</style>