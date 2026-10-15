<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose :title="title" :bodyStyle="{minHeight: '70vh'}" :width="'90vw'" @ok="handleSubmit"
    ok-text="保存" :maskClosable="false">
    <!--引用表格-->
    <BasicTable @register="registerTable">
      <!--插槽:table标题-->
      <template #tableTitle>
        <div style="width: 100%;">
          <a-row>
            <a-col span="24" class="text" style="font-weight: bold; font-size: 18px;">{{ configMainData.reportName }}</a-col>
            <a-col span="1" class="text">日期:</a-col>
            <a-col span="3" class="components">
              <a-date-picker v-model:value="formData.fillingDate" valueFormat="YYYY-MM-DD" format="YYYY-MM-DD"
                :bordered="false" :disabled="!isDetail"></a-date-picker>
            </a-col>

            <a-col span="2" class="text">班次:</a-col>
            <a-col span="3" class="components">
              <KtShiftSelect v-model:value="formData.classes" placeholder="请选择班次" :bordered="false" :disabled="!isDetail" :lineCode="formData.lineCode"></KtShiftSelect>
            </a-col>

            <a-col span="2" class="text">班组:</a-col>
            <a-col span="3" class="components">
              <JDictSelectTag v-model:value="formData.teamsGroups" placeholder="请选择班组" :bordered="false"
                :dictCode="`mes3_mdm_shift_group where del_flag = 0 and line_code = '${formData.lineCode}',group_name,id`" 
                :showChooseOption="false" :disabled="!isDetail"></JDictSelectTag>
            </a-col>

            <a-col span="2" class="text">操作员:</a-col>
            <a-col span="3" class="components">
              <a-select v-model:value="formData.operator" placeholder="请选择" show-search :allowClear=true
                :bordered="false" :maxTagCount="1" :filter-option="filterOption" :options="userOptions"
                :disabled="!isDetail">
              </a-select>
            </a-col>

            <a-col span="2" class="text">确认人:</a-col>
            <a-col span="3" class="components">
              <a-select v-model:value="formData.confirm" placeholder="请选择" show-search :allowClear=true
                :bordered="false" :maxTagCount="1" :filter-option="filterOption" :options="userOptions"
                :disabled="!isDetail">
              </a-select>
            </a-col>
          </a-row>

          <a-row>
            <a-col span="1" class="text">订单/罐号:</a-col>
            <a-col span="3" class="components">
              <a-input v-model:value="formData.orderNo" placeholder="请输入订单/罐号" :bordered="false" :disabled="!isDetail" />
            </a-col>

            <a-col span="2" class="text">批次:</a-col>
            <a-col span="3" class="components">
              <a-input v-model:value="formData.batch" placeholder="请输入批次" :bordered="false" :disabled="!isDetail" />
            </a-col>

            <a-col span="2" class="text">品种:</a-col>
            <a-col span="3" class="components">
              <a-input v-model:value="formData.variety" placeholder="请输入品种" :bordered="false" :disabled="!isDetail" />
            </a-col>

            <a-col span="2" class="text">设备:</a-col>
            <a-col span="3" class="components">
              <a-input v-model:value="formData.device" placeholder="请输入设备" :bordered="false" :disabled="!isDetail" />
            </a-col>
            <a-col span="5" class="text"></a-col>

            <a-col span="1" class="text">备注:</a-col>
            <a-col span="23" class="components">
              <a-textarea v-model:value="formData.remark" :rows="2" :maxlength="500" placeholder="请输入备注"
                :bordered="false" :disabled="!isDetail" />
            </a-col>
          </a-row>
        </div>
      </template>
      <!--字段回显插槽-->
      <template #checkTime="{ record }">
        <a-date-picker v-model:value="record.checkTime" show-time placeholder="请选择检查时间"
          value-format="YYYY-MM-DD HH:mm:ss" :disabled="!isDetail"></a-date-picker>
      </template>
      <template #checkData="{ record }">
        <a-date-picker v-if="record.checknameFormat == 1" v-model:value="record.checkData" placeholder="请选择日期"
          value-format="YYYY-MM-DD" style="width: 100%;" :disabled="!isDetail"></a-date-picker>
        <a-date-picker v-else-if="record.checknameFormat == 2" v-model:value="record.checkData" placeholder="请选择时间"
          show-time value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%;" :disabled="!isDetail"></a-date-picker>
        <a-input v-else-if="record.checknameFormat == 3" v-model:value="record.checkData" placeholder="请输入"
          style="width: 100%;" :disabled="!isDetail" />
        <a-switch v-else-if="record.checknameFormat == 4" v-model:checked="record.checkData" checked-children="不合格"
          un-checked-children="合格" class="custom-switch" checkedValue="true" :disabled="!isDetail" />
        <a-select v-else-if="record.checknameFormat == 5" v-model:value="record.checkData" :options="userOptions"
          show-search :filter-option="filterOption" placeholder="请选择员工"
          style="text-align: left;" :disabled="!isDetail">
        </a-select>
        <a-switch v-else-if="record.checknameFormat == 6" v-model:checked="record.checkData" checked-children="NOK"
          un-checked-children="OK" class="custom-switch" checkedValue="true" :disabled="!isDetail" />
      </template>
      <template #checkPhoto="{ record }">
        <JImageUpload :fileMax="10" listType="picture" v-model:value="record.checkPhoto" :disabled="!isDetail"></JImageUpload>
      </template>
    </BasicTable>
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref, computed, unref, reactive } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicTable, } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage'
import { generalColumns } from '../PostCheckRecordDatainfoMain.data';
import { queryConfig, saveDataInfo, queryDetailList } from '../PostCheckRecordDatainfoMain.api';
import { buildUUID } from '/@/utils/uuid';
import { useMessage } from '/@/hooks/web/useMessage';
import { cloneDeep } from 'lodash-es';
import dayjs from 'dayjs';
import JImageUpload from '/@/components/Form/src/jeecg/components/JImageUploadV2.vue';
import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';
import KtShiftSelect from '/@/components/KtShiftSelect/KtShiftSelect.vue';
import { getDictItems } from '/@/api/common/api';

//注册消息提示组件
const { createMessage } = useMessage();
// Emits声明
const emit = defineEmits(['register', 'success']);
const isUpdate = ref(true);
const isDetail = ref(false);
//用户下拉列表数据
const userOptions = ref([] as any);
//清单数据
const configMainData = ref({} as any);
const clientHeight = document.documentElement.clientHeight - 560;

//提交表单数据
const formData = reactive<any>({
  id: undefined,
  fillingDate: undefined,
  classes: undefined,
  teamsGroups: undefined,
  operator: undefined,
  confirm: undefined,
  orderNo: undefined,
  batch: undefined,
  variety: undefined,
  device: undefined,
  remark: undefined
});
//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    title: '',
    pagination: false,
    showTableSetting: false,
    showActionColumn: false,
  },
})
const [registerTable, { setTableData, setLoading, getDataSource, setColumns }] = tableContext

//注册modal数据
const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
  await restFormData();
  //设置modal属性值
  setModalProps({ confirmLoading: false, showCancelBtn: !!data?.showFooter, showOkBtn: !!data?.showFooter });
  isUpdate.value = !!data?.isUpdate;
  isDetail.value = !!data?.showFooter;
  const parentData = cloneDeep(data);
  setLoading(true);
  //获取用户下拉选项数据
  userOptions.value = await getDictItems('sys_user where del_flag = 0, realname, id');
  //获取数据及配置
  await getDataAndConfig(parentData.record, parentData.configObj, parentData.reportType);
  setLoading(false);
});

//设置modal标题
const title = computed(() => (!unref(isUpdate) ? '新增' : !unref(isDetail) ? '详情' : '编辑'));

//获取数据及配置
async function getDataAndConfig(record, configMain, reportType) {
  //详细数据
  let detailRecords = [] as any;
  if(!record?.id || !record.configMain){
    //record为空时，获取配置数据
    configMainData.value = configMain;
    const res = await queryConfig({ id: record?.id, configId: configMain?.id || record.configMainId });
    Object.assign(formData, res);
  }

  //设置配置数据
  if (record && record.id) {
    Object.assign(formData, record);
    configMainData.value = record.configMain ? JSON.parse(formData.configMain) : [];
    const { records } = await queryDetailList({ datainfoId: record?.id, pageNo: 1, pageSize: 9999 });
    detailRecords = records;
  } else {
    formData.configMain = JSON.stringify(configMain);
    formData.factoryCode = configMain?.factoryCode;
    formData.deptCode = configMain?.deptCode;
    formData.lineCode = configMain?.lineCode;
    formData.configMainId = configMain.id;
  }
  configMainData.value.reportType = reportType;
  for(const item of generalColumns){
    //隐藏类型为岗位检查记录不需要的列
    if(item.dataIndex === 'frequencyIllustrate' || item.dataIndex === 'frequencyIllustrateCount'){
      item.ifShow = configMainData.value.reportType == 1;
    }
    //影藏通用岗位不需要的列
    if(item.dataIndex === 'checkStandardDesc'){
      item.ifShow = configMainData.value.reportType == 2;
    }
  }
  setColumns(generalColumns);
  //定义表单变量
  let tableData = [] as any;
  //转换配置数据
  const configProject = formData.configProject ? JSON.parse(formData.configProject) : [];
  const configStandard = formData.configStandard ? JSON.parse(formData.configStandard) : [];
  let num = 1;
  for (const item of configProject) {
    let arr = [] as any;
    configStandard.forEach((standard, standardIndex) => {
      //组合检查项目和检查标准数据
      if (standard.jcxmId === item.id) {
        for(let i = 0; i < Math.floor(item.frequencyIllustrateCount); i++){
          let newObject = { ...standard, ...item };
          newObject.id = 'temp_' + buildUUID();
          newObject.rowSpan = 0;
          newObject.configMainId = item.zbId;
          newObject.checkItemId = item.id + '_' + (i+1); // 添加检查项ID和序号作为唯一标识
          if(i == 0){
            newObject.standardRowSpan = item.frequencyIllustrateCount;
          } else {
            newObject.standardRowSpan = 0;
          }
          arr.push(newObject);
        }
      }
    })
    
    const updatedArr = [] as any;
    if(arr.length > 0){
      //设置合并行
      arr[0].rowSpan = arr.length || 1;
      //根据项目检查次数复制数据行
      for (let i = 0; i < item.checkFrequency; i++) {
        for(let j = 0; j < arr.length; j++){
          let newObj = cloneDeep(arr[j]);
          //判断是否为老平台数据，老平台数据无需重置编号
          if(!record?.section || record?.section?.indexOf('old') < 0){
            newObj.checkItemId = item.id + '_' + num;
          }
          newObj.id = 'temp_' + buildUUID(); // 修改id，避免修改时影响其他行数据
          newObj.reportType = reportType;
          updatedArr.push(newObj);
          num++;
        }
      }
    }
    tableData = [...tableData, ...updatedArr];
  }

  if(detailRecords.length > 0){
    tableData.forEach((item, index) => {
      //老平台上数据存在没有配置的情况，直接按下标取数据
      if(record?.id && !record.configMain){
        Object.assign(item, detailRecords[index]);
      } else {
        detailRecords.forEach((detail) => {
          if(item.checkItemId === detail.checkItemId){
            Object.assign(item, detail);
          }
        });
      }
    });
  }
  setTableData(tableData);
}

//下拉选择过滤方法
function filterOption(input, option) {
  return option.text.toLowerCase().indexOf(input.toLowerCase()) >= 0;
};

//重置FormData数据
function restFormData() {
  configMainData.value.reportType = undefined;
  configMainData.value.reportName = undefined;
  for (const key in formData) {
    formData[key] = undefined;
  }
  formData.fillingDate = dayjs().format('YYYY-MM-DD');
}

/**
 * 提交事件
 */
async function handleSubmit() {
  try {
    if (!formData.fillingDate === undefined) {
      createMessage.warning('日期不能为空！');
      return false;
    }
    setModalProps({ confirmLoading: true });
    formData.detailList = cloneDeep(getDataSource());
    await saveDataInfo(formData);
    //关闭弹窗
    closeModal();
    //重置表单
    restFormData();
    //刷新外部表格
    emit('success');
  } catch (error) {
  } finally {
    setModalProps({ confirmLoading: false });
  }
}
</script>

<style lang="less" scoped>
:deep(.jeecg-basic-table-header__toolbar) {
  display: none;
}

:deep(.jeecg-basic-table-header__tableTitle) {
  margin-bottom: -11px !important;
}

:deep(.jeecg-basic-table-header__tableTitle > *) {
  margin-right: 0;
}

:deep(.ant-select) {
  padding-top: 4px !important;
}

.text {
  min-height: 40px;
  line-height: 40px;
  background-color: #FBFBFB;
  border: 1px solid #e5e7eb;
  border-bottom: 0;
  box-sizing: border-box;
  text-align: center;
}

.components {
  width: 100%;
  text-align: left;
  border-top: 1px solid #e5e7eb;
  box-sizing: border-box;

  >* {
    width: 100%;
    height: 100%;
  }
}

.components:last-child {
  border-right: 1px solid #e5e7eb;
}

.custom-switch {
  background-color: #4CAF50;
  /* 更改未选中状态的背景颜色 */
}

.custom-switch.ant-switch-checked {
  background-color: #FE0000;
  /* 更改选中状态的背景颜色 */
}

.ant-switch:hover:not(.ant-switch-disabled) {
  background-color: #4CAF50;
}

.ant-switch.ant-switch-checked:hover:not(.ant-switch-disabled) {
  background-color: #FE0000;
}
</style>