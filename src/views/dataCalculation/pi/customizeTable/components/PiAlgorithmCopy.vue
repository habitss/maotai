<template>
  <a-spin :spinning="confirmLoading" class="span-box">
    <div>
      <!--主表信息显示-->
      <div class="jeecg-basic-table-form-container">
        <a-form :label-col="labelCol" :wrapper-col="wrapperCol" labelAlign="left">
          <a-row :gutter="24">
            <a-col :lg="24">
              <a-form-item label="当前使用算法">
                <a-input v-model:value="tableData.currentAlgorithm" disabled/>
              </a-form-item>
            </a-col>
            <a-col :lg="24">
              <a-form-item label="字段中文名称">
                <a-input v-model:value="tableData.name" disabled/>
              </a-form-item>
            </a-col>
            <a-col :lg="24">
              <a-form-item label="字段名">
                <a-input v-model:value="tableData.fieldName" disabled/>
              </a-form-item>
            </a-col>
            <a-col :lg="24">
              <a-form-item label="目标字段">
                <a-select v-model:value="tableData.targetField" show-search option-filter-prop="label" :options="dailyFieldList" placeholder="请选择" allowClear @change="getName()"></a-select>
              </a-form-item>
            </a-col>
          </a-row>

          <div style="text-align: center;margin-top: 20px;">
            <a-button style="margin-right: 8px" @click="onClose">取消</a-button>
            <a-button type="primary" @click="submitForm">复制</a-button>
          </div>
        </a-form>
      </div>
    </div>
  </a-spin>
</template>

<script lang="ts" name="pi-piCustomizeTableField" setup>
  import { ref, reactive, onMounted, unref } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import {getFieldByTableId,algorithmCopy} from '../PiAlgorithm.api';
  import { useUserStore } from '/@/store/modules/user';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();
  const confirmLoading = ref<boolean>(false);
  const queryParam = ref<any>({});
  const tableData = ref<any>({});
  const toggleSearchStatus = ref<boolean>(false);
  const registerModal = ref();
  const algorithmModal = ref();
  const userStore = useUserStore();
  const props = defineProps({
    departObject: { type: Object,default:()=>({}) },
  });
  const fieldObject = ref<Object>({});
  //字段集合
  const dailyFieldList = reactive<Record<string, any>>([]);
  const { createMessage } = useMessage();
  const emit = defineEmits(['register', 'ok','close']);

  const labelCol = reactive({
    xs: { span: 24 },
    sm: { span: 5 },
  });
  const wrapperCol = reactive({
    xs: { span: 24 },
    sm: { span: 16 },
  });

  onMounted(() => {
    const { departObject } = props;
    tableData.value.id = departObject.id;
    tableData.value.tableId = departObject.tableId;
    tableData.value.tableName = departObject.tableName;
    // tableData.value.lineCode = departObject.lineCode;
    // tableData.value.section = departObject.section;
    tableData.value.currentAlgorithm = departObject.currentAlgorithm;
    tableData.value.name = departObject.name;
    tableData.value.fieldName = departObject.fieldName;
    getDailyFieldData();
  });

  /**
 * 操作栏
 */
function getTableAction(record) {
  return [
    {
      label: "配置算法",
      onClick: algorithm.bind(null, record),
      auth: '',
    },
  ];
}

//配置算法-新增
const addAlgorithm = () => {
  // record.piTable = tableData.value.tableName
  let record = {
    piTable:tableData.value.tableName,
    piCode:tableData.value.fieldName,
    piName:tableData.value.name,
    piTableId:tableData.value.tableId
  }
  algorithmModal.value.add(record);
}

//配置算法-修改
const algorithm = (record) => {
  // record.piTable = tableData.value.tableName
  // record.piTable = tableData.value.tableName
  record.piTableId = tableData.value.tableId
  algorithmModal.value.edit(record);
}



//字段集合
async function getDailyFieldData(){
    let res = await getFieldByTableId({tableId: tableData.value.tableId});
    let newArr = res.map(item => {
       //排除日期
      // if(item.type != '4'){
        return { 
          label:item.name,
          value:item.fieldName}
        }
    //}
    ).filter(Boolean);
    dailyFieldList.push(...newArr);
}
 /**
 * 提交数据
 */
  async function submitForm() {
    confirmLoading.value = true;
    // 触发表单验证
    if(!tableData.value.targetField){
      createMessage.warning("目标字段不为空");
      confirmLoading.value = false;
      return false;
    }

    await algorithmCopy(tableData.value)
    .then((res) => {
    if (res.success) {
        createMessage.success(res.message);
        emit('ok');
    } else {
        createMessage.warning(res.message);
    }
    })
    .finally(() => {
      confirmLoading.value = false;
    });
  }
  //取消
  function onClose(){
    emit('close');
  }
  //获取名称
  function getName(){
    //
    let obj = dailyFieldList.find(item => item.value === tableData.value.targetField);
    tableData.value.targetFieldName = obj.label
  }
</script>

<style lang="less" scoped>
.jeecg-basic-table-form-container {
  .table-page-search-submitButtons {
    display: block;
    margin-bottom: 24px;
    white-space: nowrap;
  }
  .query-group-cust {
    width: calc(50% - 15px);
    min-width: 100px !important;
  }
  .query-group-split-cust {
    width: 30px;
    display: inline-block;
    text-align: center;
  }
}
//@import '../common.less';
</style>