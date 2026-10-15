<template>
  <!--引用表格-->
  <BasicTable @register="registerTable" :dataSource="tableData" @change="handleTableChange">
    <!-- 查询区域插槽 -->
    <!--  deptCode部门查询插槽    -->
    <template #form-deptCode="{ model, field }">
      <kt-depart-select v-model:value="model[field]" select-type="depart"
                        :placeholder="t('language.selectDepartment')" @change="handleDeptChange" />
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
       <div v-if="column.dataIndex == 'frequency'" class="box">
        <a-select v-model:value="record.frequency" :options="frequencyOptions" @change="handleEdit(record.id)">
            
        </a-select>
      </div>
       <div v-if="column.dataIndex == 'checkUser'" class="box">
        <a-select v-model:value="record.checkUser" :options="USER_LIST"
          :fieldNames="{label: 'realname', value: 'id'}" showSearch
          optionFilterProp = "realname" @change="handleEdit(record.id)"
        >
        </a-select>
      </div>
       <div v-if="column.dataIndex == 'verifyUser'" class="box">
        <a-select v-model:value="record.verifyUser" :options="USER_LIST"
          :fieldNames="{label: 'realname', value: 'id'}" showSearch
          optionFilterProp = "realname" @change="handleEdit(record.id)"
        >
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
          >计划</a-button
          >
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
        <div v-else-if="record[column.dataIndex].state == '6'">
        <a-button
          style="background-color: #7c8087; color: white;"
          v-model:value="record[column.dataIndex]"
          @click="handleChange(column, index, record)"
        >未生产取消</a-button>
        <a-button
          v-model:value="record[column.dataIndex]"
          @click="handleCancelAllChange(column, index, record,0)"
          type="text" danger>取消</a-button>

        </div>
        <div v-else-if="record[column.dataIndex].state == '7'">
        <a-button
          style="background-color: #7c8087; color: white;"
          v-model:value="record[column.dataIndex]"
          @click="handleChange(column, index, record)"
        >设备故障取消</a-button>
        <a-button
          v-model:value="record[column.dataIndex]"
          @click="handleCancelAllChange(column, index, record,0)"
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
          <a-select-option value="2">未生产取消</a-select-option>
          <a-select-option value="7">设备故障取消</a-select-option>
        </a-select>
      </div>
    </template>
  </BasicTable>

  <PeriodSubmitModal @register="registerPeriodSubmitModalForm" @close="handleClose" :width="1200" />
</template>

<script lang="ts" name="action.period-periodSchedule-add" setup>
  import { onMounted, ref } from 'vue';
  import { BasicTable} from '@/components/Table';
  import KtDepartSelect from '@/components/KtDepartSelect/KtDepart.vue';
  import { useModal } from '@/components/Modal';
  import { cloneDeep } from 'lodash-es';
  import { useListPage } from '@/hooks/system/useListPage';
  import {
    saveOrUpdate,
    getList,
    updatePeriod,
    cancel
  } from '@/views/dataCalculation/periodCheck/periodPlan/PeriodScheduleList.api';
  import { columns, searchFormSchema, dataMaps, USER_LIST } from '@/views/dataCalculation/periodCheck/periodPlan/PeriodScheduleList.data';
  import PeriodSubmitModal from './components/PeriodSubmitModal.vue'
  import { useI18n } from '@/hooks/web/useI18n';
  import {addFactoryDept} from "@/utils/common/compUtils";
  import { getDictItems } from '@/api/common/api';
  import { message } from 'ant-design-vue';
  
  const { t } = useI18n();

  const tableData = ref([]);
    // 加载状态
  const saveLoading = ref(false);
    //频率字典项
  const frequencyOptions = ref();

  const open = ref<boolean>(false);

  const [registerPeriodSubmitModalForm, { openModal: openScheduleModalForm }] = useModal();

  const showModal = () => {
    open.value = true;
  };

  const schedule = ref({
    id: '',
    apparatusId: '',
    apparatusType: '',
    permissibleError: '',
    frequency: '',
    checkUser: '',
    verifyUser: '',
    result: '',
    date: '',
    planYear: '',
    scheduleJson: [],
    type: 0,
  });
  //记录修改过数据的记录id
  const editMap = ref({});

  const { tableContext } = useListPage({
    designScope: '期间核查计划',
    tableProps: {
      showActionColumn: false,
      // actionColumn: {
      //   width: 100,
      //   title: '操作',
      //   dataIndex: 'action',
      //   fixed: 'right',
      //   slots: { customRender: 'action' },
      // },
      api: getList,
      formConfig: {
        schemas: searchFormSchema,
        showAdvancedButton: false,
      },
      showIndexColumn: false,
      showTableSetting: true,
      tableSetting: { fullScreen: true },
      title: '期间核查计划',
      beforeFetch: (params) => {
        editMap.value = {};
        addFactoryDept(params)
        return addFactoryDept(params);
      },
      afterFetch: (records) => {
        dataMaps.value = records;
        let tableColumn = cloneDeep(columns);
        //构造表格列头
        let weekArray = JSON.parse(records[0].weekJson);
        weekArray.forEach(function ({ data: dataValue ,num: numValue}, index) {
          let weekColumn = {
            title: '',
            dataIndex: '',
            key: '',
            width: '100',
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
        tableData.value = weekData;
        listOfFrequency();
        return weekData;
      },
    },
  });

  // BasicTable绑定注册
  const [registerTable, { reload, getForm, updateTableDataRecord, setColumns, setTableData }] = tableContext;


  function handleChange(column, index, record) {
    let values = {date:column.dataIndex}
    const mergedObj = Object.assign({}, record, values);
    openScheduleModalForm(true, { record: mergedObj });
  }

  async function handleCancelChange(column, index, record){
    let result = await saveSchedule(record, column, 3);
    await updateTableRow(result,record,index);
  }

  async function handleCancelAllChange(column, index, record,type){
    try{
      // schedule.value.id = record.id;
      schedule.value.apparatusId = record.apparatusId;
      schedule.value.frequency = record.frequency;
      schedule.value.planYear = record.planYear;
      schedule.value.date = column.dataIndex;
      schedule.value.type = type;
      //TODO 此处将数据提交给服务器保存
      let result = await cancel(schedule.value);
      await updateTableRow(result,record,index);
    } catch (e) {
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
    updateTableDataRecord(index,record) ; // 更新数据
  }

  async function listOfFrequency(){
    frequencyOptions.value = await getDictItems("mes3_mdm_dict, item_key, item_val, del_flag = 0 and group_code = 'MES3_COP_CONFIG_FREQUENCY'");
  }

    // 监听表格数据变化
const handleTableChange = (newData) => {
  loadTableData(newData);
};

function loadTableData(records){
  //封装表格的值
  let weekData = [];
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


  // 保存操作
  const handleSave = async () => {
    saveLoading.value = true;
    try {
      const simplifiedData = [] as any;
      tableData.value.forEach((row) => {
        if(editMap.value[row.id] == 1){
          simplifiedData.push({
            id: row.id,
            apparatusId:row.apparatusId,
            frequency: row.frequency,
            checkUser: row.checkUser,
            verifyUser: row.verifyUser
          });
        }
      })
      await updatePeriod(simplifiedData);
      message.success("保存成功！");
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

  // //设备下拉列表联动
  // async function handleDeviceChange(value) {
  //   await getForm().updateSchema([
  //     {
  //       field: 'apparatusName',
  //       component: 'JDictSelectTag',
  //       componentProps: {
  //         dictCode: "mes3_period_verification_device, apparatus_name, id, del_flag = 0 and line_code='" + value + "'",
  //       },
  //     },
  //   ]);
  // }

  async function saveSchedule(record, datas, state) {
    // schedule.value.id = record.id;
    schedule.value.apparatusId = record.apparatusId;
    schedule.value.frequency = record.frequency;
    schedule.value.checkUser = record.checkUser;
    schedule.value.verifyUser = record.verifyUser;
    schedule.value.result = state;
    schedule.value.date = datas.dataIndex;
    schedule.value.planYear = record.planYear;

    //TODO 此处将数据提交给服务器保存
    let result = await saveOrUpdate(schedule.value);
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
    reload();
  }

  function handleEdit(recordId){
    editMap.value[recordId] = '1';
  }

  onMounted(() => {

  })
</script>

<style scoped></style>
