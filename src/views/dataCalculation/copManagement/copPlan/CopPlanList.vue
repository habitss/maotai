<template>
  <!--引用表格-->
  <BasicTable @register="registerTable" :dataSource="tableData" @change="handleTableChange">
    <!-- 查询区域插槽 -->
    <!--  deptCode部门查询插槽    -->
    <template #form-deptCode="{ model, field }">
      <kt-depart-select v-model:value="model[field]" select-type="depart" :placeholder="t('language.selectDepartment')" @change="handleDeptChange" />
    </template>
    <!--  lineCode产线查询插槽    -->
    <template #form-lineCode="{ model, field }">
      <kt-depart-select
        v-model:value="model[field]"
        select-type="line"
        :placeholder="t('language.selectProductLine')"
        :parentKey="model.deptCode"
      />
    </template>
    <template #toolbar>
      <a-button 
        type="primary" 
        @click="handleSave"
        :loading="saveLoading"
      >
        保存
      </a-button>
    </template>
    <!--插槽:表格内容-->
    <template #bodyCell="{ column, record, index }">
      <div v-if="column.dataIndex == 'sort'" class="box">
        <a-input v-model:value="record.sort" @change="handleEdit(record.id)" />
      </div>
      <div v-if="column.dataIndex == 'copPointName'" class="custom-div">
        <a-button class="wrap-text"
                  type="link" @click="showDetail(column, index, record)">{{record.copPointName}}</a-button>
      </div>
      <div v-if="column.dataIndex == 'formula'" class="box">
        <a-select v-model:value="record.formula" :options="formulaOptions" allow-clear
                  show-search :filter-option="filterOption" @change="handleEdit(record.id)">

        </a-select>
      </div>
      <div v-if="column.dataIndex == 'temperature'" class="box">
        <a-select v-model:value="record.temperature" :options="temperatureOptions" @change="handleEdit(record.id)">
            
          </a-select>
      </div>
       <div v-if="column.dataIndex == 'frequency'" class="box">
        <a-select v-model:value="record.frequency" :options="frequencyOptions" @change="handleEdit(record.id)">
            
          </a-select>
      </div>
       <div v-if="column.dataIndex == 'clearBy'" class="box">
        <a-select v-model:value="record.clearBy" show-search
                  :filter-option="filterOption" allow-clear @change="handleEdit(record.id)">
          <a-select-option v-for="item in userOptions" :key="item.realname" :value="item.id">
            {{ item.realname }}
          </a-select-option>
        </a-select>
       
      </div>
       <div v-if="column.dataIndex == 'checkBy'" class="box">
        <a-select v-model:value="record.checkBy" show-search
                  :filter-option="filterOption" allow-clear @change="handleEdit(record.id)">
          <a-select-option v-for="item in userOptions" :key="item.realname" :value="item.id">
            {{ item.realname }}
          </a-select-option>
        </a-select>
      </div>
      <div v-if="column.dataIndex.indexOf('-') > 0" class="box">
        <div v-if="record[column.dataIndex] == null">

        </div>
        <div v-else-if="record[column.dataIndex].state == '-1'">
        <a-button
          type="primary"
          color="warning"
          v-model:value="record[column.dataIndex]"
          @click="handleChange(column, index, record)"
        >计划</a-button>
          <a-popconfirm title="确认取消排期？" ok-text="取消当前周后所有排期" cancel-text="取消当前排期"
                        @confirm="handleCancelAllChange(column, index, record,1)"
                        @cancel="handleCancelAllChange(column, index, record,0)">
            <a-button
              v-model:value="record[column.dataIndex]"
              @click="showModal"
              type="text" danger>取消</a-button>
          </a-popconfirm>
        </div>
        <a-button
          v-else-if="record[column.dataIndex].state == '1'"
          style="background-color: green; color: white;"
          v-model:value="record[column.dataIndex]"
          @click="handleChange(column, index, record)"
        >已完成</a-button>
        <a-button
          v-else-if="record[column.dataIndex].state == '2'"
          style="background-color: rosybrown; color: white;"
          v-model:value="record[column.dataIndex]"
          @click="handleChange(column, index, record)"
        >延迟</a-button>
        <a-button
          v-else-if="record[column.dataIndex].state == '3'"
          style="background-color: darkolivegreen; color: white;"
          v-model:value="record[column.dataIndex]"
          @click="handleChange(column, index, record)"
        >延迟完成</a-button>
        <a-button
          v-else-if="record[column.dataIndex].state == '4'"
          style="background-color: indianred; color: white;"
          v-model:value="record[column.dataIndex]"
          @click="handleChange(column, index, record)"
        >待审核</a-button>
        <a-button
          v-else-if="record[column.dataIndex].state == '5'"
          style="background-color: darkslategrey; color: white;"
          v-model:value="record[column.dataIndex]"
          @click="handleChange(column, index, record)"
        >未执行</a-button>
        <div  v-else-if="record[column.dataIndex].state == '6'">
        <a-button
          style="background-color: #7c8087; color: white;"
          v-model:value="record[column.dataIndex]"
          @click="handleChange(column, index, record)"
        >因生产取消</a-button>
        <a-button
          v-model:value="record[column.dataIndex]"
          @click="handleCancelChange(column, index, record)"
          type="text" danger>取消</a-button>
      </div>
        <a-button
          v-else-if="record[column.dataIndex].state == '0'"
          style="background-color: red; color: white;"
          v-model:value="record[column.dataIndex]"
          @click="handleChange(column, index, record)"
        >不合格</a-button>

          <a-select v-else-if="record[column.dataIndex].state == '' && record.frequency != null" @change="(value) => handleStateChange(value, column, index, record)">
            <a-select-option value="1">计划</a-select-option>
            <a-select-option value="2">因生产取消</a-select-option>
          </a-select>
      </div>
    </template>

    <template #action="{ record }">
      <TableAction :actions="createActions(record)" />
    </template>
  </BasicTable>
  <!-- 填报modal -->
  <Modal @register="registerAddModal" @success="reload" />
  <CopSubmitModal @register="registerCopSubmitModalForm" @close="handleClose" :width="1200" />
</template>

<script lang="ts" name="action.cop-copPlanTrack-add" setup>
import {onMounted, ref} from 'vue';
  import {ActionItem, BasicTable, TableAction, EditRecordRow } from '/@/components/Table';
  import KtDepartSelect from '@/components/KtDepartSelect/KtDepart.vue';
  import { useMessage } from '@/hooks/web/useMessage';
  import { useModal } from '@/components/Modal';
  import { cloneDeep } from 'lodash-es';
  import { useListPage } from '@/hooks/system/useListPage';
  import { saveOrUpdate, getList,updateCop,deleteData,cancel } from '@/views/dataCalculation/copManagement/copPlan/copPlanList.api';
  import { columns, searchFormSchema, dataMaps } from '@/views/dataCalculation/copManagement/copPlan/copPlanList.data';
  import { useI18n } from '@/hooks/web/useI18n';
  import  CopSubmitModal  from '@/views/dataCalculation/copManagement/copPlan/components/CopSubmitModal.vue';
  import { addFactoryDept } from "@/utils/common/compUtils";
import {getDictItems, mes3UserListAll} from '@/api/common/api';
  import { message } from 'ant-design-vue';
  import Modal from '@/views/dataCalculation/copManagement/copPoint/copPointForm.vue';
  import {getById} from "@/views/dataCalculation/copManagement/copPoint/copPonin.api";
  const { t } = useI18n();
  //查询参数对象

  const tableData = ref([]);
  //消息提示

  const emit = defineEmits(['close']);

  //新增/编辑modal
  const [registerAddModal, { openModal }] = useModal();

  //配置清洗点modal
  const [registerCopSubmitModalForm, { openModal: openCopPlanModalForm }] = useModal();

  //消息提示
  const { createMessage: msg } = useMessage();
  //配方字典项
  const formulaOptions = ref();
  //文档字典项
  const temperatureOptions = ref();
  //频率字典项
  const frequencyOptions = ref();

const userOptions = ref();

  // 加载状态
const saveLoading = ref(false);

  const copSchedule = ref({
    id: '',
    copPointId: '',
    temperature: '',
    formula: '',
    timeValue: '',
    frequency: '',
    clearBy: '',
    checkBy: '',
    state: '',
    result: '',
    date: '',
    planYear: '',
    scheduleJson: [],
    type: 0
  });
  //记录修改过数据的记录id
  const editMap = ref({});

  const { tableContext } = useListPage({
    designScope: 'cop清洗计划',
    tableProps: {
      showActionColumn: true,
      actionColumn: {
        width: 50,
        fixed: 'right',
      },
      api: getList,
      formConfig: {
        schemas: searchFormSchema,
        showAdvancedButton: false,
      },
      showIndexColumn: false,
      showTableSetting: true,
      tableSetting: { fullScreen: true },
      title: 'cop清洗计划',
      beforeFetch: (params) => {
        params.column = 'sort';
        params.order = 'asc';
        addFactoryDept(params)
        editMap.value = {};
        return addFactoryDept(params);
      },
      afterFetch: (records) => {
        dataMaps.value = records;
        let tableColumn = cloneDeep(columns);
        //构造表格列头
        let weekArray = JSON.parse(records[0].weekJson);
        weekArray.forEach(function ({ data: dataValue,num: numValue }, index) {
          let weekColumn = {
            title: '',
            dataIndex: '',
            key: '',
            width: '230',
            fixed: 'center',
            children: [
              {
                title: '',
                dataIndex: '',
                key: '',
                width: 100,
              },
            ],
          };
          weekColumn.title = '第' + numValue + '周';
          weekColumn.dataIndex = numValue;
          weekColumn.key = numValue;
          weekColumn.children.forEach(function (value,index){
            weekColumn.children[index].title = dataValue;
            weekColumn.children[index].dataIndex = dataValue;
            weekColumn.children[index].key = dataValue;
          })
          tableColumn.push(weekColumn);
        });
        setColumns(tableColumn);
        //封装表格的值
        let weekData = loadTableData(records);
        tableData.value = weekData
        console.log(weekData)
        return weekData;
      },
    },
  });


  /**
   * 创建操作列
   */
  function createActions(record: EditRecordRow): ActionItem[] {
    return [
      {
        label: '删除',
        popConfirm: {
          title: '是否删除？',
          confirm: handleDelete.bind(null, record.id),
        },
      },
    ];
  }

  // BasicTable绑定注册
  const [registerTable, { reload, getForm, updateTableDataRecord, setColumns, setTableData }] = tableContext;



  /**
   * 删除
   */
  async function handleDelete(record) {
    console.log(record)
    //调用后台修改接口, 删除
    await deleteData(record, handleSuccess);
    msg.success({ content: '删除成功', key: 'saving' });
  }

  async function handleChange(column, index, record) {
    let values = {date:column.dataIndex}
    const mergedObj = Object.assign({}, record, values);
    openCopPlanModalForm(true, { record: mergedObj });
  }

  async function handleCancelChange(column, index, record){
    let result = await saveSchedule(record, column, 3);
    await updateTableRow(result,record,index);
  }

  async function handleCancelAllChange(column, index, record,type){
    // schedule.value.id = record.id;
    try{
      copSchedule.value.copPointId = record.copPointId;
      copSchedule.value.frequency = record.frequency;
      copSchedule.value.date = column.dataIndex;
      copSchedule.value.planYear = record.planYear;
      copSchedule.value.type = type;
      //TODO 此处将数据提交给服务器保存
      let result = await cancel(copSchedule.value);
      await updateTableRow(result,record,index);
    } catch (e) {
      console.log(e)
    }
  }

  async function handleStateChange(value, column, index, record) {
    let result = await saveSchedule(record, column, value);
    // await updateTableRow(result,record,index);
    reload();
  }

  async function updateTableRow(result,record,index){
    let weekData = record.weekJson;
    let weeks = JSON.parse(weekData);
    weeks.forEach(function (values, index) {
      let data = values.data;
      let jsons =  JSON.parse(result.weekJson);
      jsons.forEach(function (value, index) {
        if(data == value.data){
          record[data].state = value.state;
        }
      });
    });
    console.log('----',record)
    updateTableDataRecord(index,record) ; // 更新数据
  }

  async function listOfFormula(){
    formulaOptions.value = await getDictItems("mes3_mdm_dict, item_key, item_val, del_flag = 0 and group_code = 'MES3_COP_RECOMMENDED_FORMULA'");
  }

   async function listOfTemperature(){
    temperatureOptions.value = await getDictItems("mes3_mdm_dict, item_key, item_val, del_flag = 0 and group_code = 'MES3_COP_CONFIG_TEMPERATURE'");
  }

   async function listOfFrequency(){
    frequencyOptions.value = await getDictItems("mes3_mdm_dict, item_key, item_val, del_flag = 0 and group_code = 'MES3_COP_CONFIG_FREQUENCY'");
  }

  async function listOfUser(){
    userOptions.value = await mes3UserListAll();
  }

  // 监听表格数据变化
const handleTableChange = (newData) => {
  loadTableData(newData);
};

function loadTableData(records){
  //封装表格的值
  let weekData = [];
  console.log(records)
  if(!Array.isArray(records)){
    return ;
  }

  records.forEach(function (values, index) {
    let weekTable = {};
    let weeks = JSON.parse(values.weekJson);
    weeks.forEach(function (value, index) {
      let stat_name = value.data;
      weekTable[stat_name] = value;
    });
    const mergedObj = Object.assign({}, values, weekTable);
    weekData.push(mergedObj);
  });
  return weekData;
}

  async function showDetail (result,record,index){
  console.log(index.copPointId)
  let cop = await getById({id:index.copPointId});
  cop.copDeviceIdVal = cop.copDeviceName;
    console.log(cop)
  openModal(true, {'isAdd': false, 'isDetail': false, 'record': cop})
}

  // 保存操作
const handleSave = async () => {
  saveLoading.value = true;
  try {
    console.log(tableData.value)
    const simplifiedData = [] as any;
    tableData.value.forEach((row) => {
      if(editMap.value[row.id] == 1){
        simplifiedData.push({
          id: row.id,
          copPointId:row.copPointId,
          sort:row.sort,
          formula: row.formula,
          temperature: row.temperature,
          frequency: row.frequency,
          clearBy: row.clearBy,
          checkBy: row.checkBy
        });
      }
    })
    console.log(simplifiedData)
    await updateCop(simplifiedData);
    message.success("保存成功！");
    handleSuccess();
  } catch {
    message.error("保存失败");
  } finally {
    saveLoading.value = false;
  }
};


  //部门改变事件
  async function handleDeptChange(value) {
    //清空产线
    await getForm().setFieldsValue({
      lineCode: '',
    });
  }

  //设备下拉列表联动
  // async function handleDeviceChange(value) {
  //   await getForm().updateSchema([
  //     {
  //       field: 'copDeviceName',
  //       component: 'JDictSelectTag',
  //       componentProps: {
  //         dictCode: "mes3_cop_device, cop_device_name, id, del_flag = 0 and line_code='" + value + "'",
  //         //触发点位下拉列表
  //         onchange: (value) => handlePointChange(value),
  //       },
  //     },
  //   ]);
  // }
  //
  // //清洗点位下拉列表联动
  // async function handlePointChange(value) {
  //   await getForm().updateSchema([
  //     {
  //       field: 'pointName',
  //       component: 'JDictSelectTag',
  //       componentProps: {
  //         dictCode: "mes3_cop_point, point_name, id, del_flag = 0 and cop_device_id='" + value + "'",
  //       },
  //     },
  //   ]);
  // }


  async function saveSchedule(record, datas, state) {
    //copSchedule.value.id = record.id;
    copSchedule.value.copPointId = record.copPointId;
    copSchedule.value.frequency = record.frequency;
    copSchedule.value.clearBy = record.clearBy;
    copSchedule.value.checkBy = record.checkBy;
    copSchedule.value.result = state;
    copSchedule.value.date = datas.dataIndex;
    copSchedule.value.planYear = record.planYear;
    //TODO 此处将数据提交给服务器保存
    let result = await saveOrUpdate(copSchedule.value);
    let weeks =  JSON.parse(result.result.weekJson);
    weeks.forEach(function (value,index){
      if(value.data == datas.dataIndex){
        record[datas.dataIndex].state = value.state;
      }
    });
    return result;
  }

  function handleClose(){
    //刷新外部表格
    handleSuccess();
  }
  const filterOption = (input: string, option: any) => {
    return option.key.toLowerCase().indexOf(input.toLowerCase()) >= 0;
  };

  function handleEdit(recordId){
    editMap.value[recordId] = '1';
  }

  /**
   * 成功回调
   */
  function handleSuccess() {
    reload();
  }

onMounted(async () => {
  await listOfFormula();
  await listOfTemperature();
  await listOfFrequency();
  await listOfUser();
});

</script>

<style scoped>
.custom-div {
  word-wrap: break-word;  /* 允许长单词换行 */
  white-space: normal;    /* 默认换行行为（可省略） */
  overflow-wrap: break-word; /* 更现代的属性，效果类似 word-wrap */
  word-break: break-all;  /* 强制所有内容换行（包括中文） */
  /* 如果容器宽度固定，可以显式设置宽度 */
  width: 100px;
}
/* 使用深度选择器穿透组件样式 */
:deep(.ant-btn .ant-btn-link) {
  word-break: break-word;
}

.wrap-text {
  white-space: normal;
  word-wrap: break-word;
  padding: 0;
}
</style>
