<template>
  <a-spin :spinning="confirmLoading" class="span-box">
    <a-row :gutter="[0,16]">
      <!--**************取数信号********************-->
      <a-col class="gutter-row" :span="24">
        <div class="gutter-box">
          <div class="box-piece-title">取数信号</div>
          <a-divider class="divider-style"/>
          <div class="box-piece-content">
            <div>
              <span class="margin-left-10 margin-right-10">区域</span>
              <kt-dict
                  v-model:value="formData.areaNo"
                  placeholder="请选择区域"
                  dict="MES3_PI_ADAPTER_AREA"
                  :disabled="disabled"
                  class="width-250 margin-left-10"
                  style="width:250px !important;"
                  @change="areaChange"
                />
              <span class="margin-left-10">PCU</span>
              <a-select show-search option-filter-prop="label" v-model:value="formData.pcuNo" :size="size" class="width-200 margin-left-10" :options="pcuArr" @change="pcuChange" placeholder="请选择" allowClear></a-select>
              <span class="margin-left-10">设备</span>
              <a-select show-search option-filter-prop="label" v-model:value="formData.unitNo" :size="size" class="width-200 margin-left-10" :options="unitArr" @change="unitChange" placeholder="请选择" allowClear></a-select>
              <span class="margin-left-10">EOP</span>
              <a-select show-search option-filter-prop="label" v-model:value="formData.eopNo" :size="size" class="width-200 margin-left-10" :options="eopArr" @change="eopChange" placeholder="请选择" allowClear></a-select>
              
            </div>
            <div style="margin-top: 10px;">
              <span class="margin-left-10">取第几次</span>
              <!-- <a-input v-model:value="formData.several" placeholder="请输入" :size="size" class="width-90 margin-left-10" allowClear/> -->
              <!-- <a-input-group compact> -->
              <a-select v-model:value="formData.severalType" class="width-90 margin-left-10" @change="severalTypeChange">
                <a-select-option value="1">第几次</a-select-option>
                <a-select-option value="-1" title="最后一条">最后</a-select-option>
              </a-select>
              <a-input v-model:value="formData.several" placeholder="请输入" :size="size" class="width-80" allowClear :disabled="formData.severalType == '-1'"/>
              <!-- </a-input-group> -->

              <span class="margin-left-10" title="RopId">系统步骤</span>
              <a-input v-model:value="formData.ropId" placeholder="请输入" :size="size" class="margin-left-10" allowClear style="width: 110px;"/>
            </div>
          </div>
        </div>
      </a-col>

      <!-- **************取值设置************** -->
       <a-col class="gutter-row" :span="24">
        <div class="gutter-box">
          <div class="box-piece-title">取值设置</div>
          <a-divider class="divider-style"/>
          <div class="box-piece-content">
            <div>
                <span class="margin-right-10">取值</span>
                <!--固定值+参数-->
                <a-select show-search option-filter-prop="label" v-model:value="formData.val" :size="size" class="width-200 margin-left-10" :options="paramAllArr" @change="valChange" placeholder="请选择" allowClear></a-select>
                <span class="margin-left-10" v-if="isParam">取值类型</span>
                <a-select show-search option-filter-prop="label" v-model:value="formData.valType" :size="size" class="width-200 margin-left-10" :options="targetActualArr" placeholder="请选择" allowClear v-if="isParam"></a-select>
                <span class="margin-left-10" v-if="isValue(formData.val)">格式转换</span>
                <a-select show-search option-filter-prop="label" v-model:value="formData.timeFormat" :size="size" class="width-200 margin-left-10" :options="timeFormatArr" placeholder="请选择" allowClear v-if="isValue(formData.val)"></a-select>
              </div>
          </div>
        </div>
      </a-col>
      <!-- **************限制条件************** -->
      <a-col class="gutter-row" :span="24">
          <div class="gutter-box" style="height: 160px;">
            <div class="box-piece-title">限制条件（步骤）</div>
            <a-divider class="divider-style"/>
            <div class="box-piece-content">
              <div style="height: 105px;overflow-y: auto;margin-top: 5px;">
                <div class="height-30" v-for="(val, index) in stepConditions" :key="index">
                  <PlusCircleOutlined @click="addStepCondition" class="width-50 icon-style" title="添加" :class="{icon_add_visibility: index != 0}"/>
                  <a-select show-search option-filter-prop="label" v-model:value="val.paramName" :size="size" class="width-100 margin-left-10 margin-right-10" :options="stepArr" placeholder="请选择" allowClear></a-select>
                  <a-select v-model:value="val.calculateType" :size="size" class="width-100 margin-left-10" :options="stepFetchConditionArr" placeholder="请选择" allowClear></a-select>
                  <a-input v-model:value="val.calculateValue" placeholder="请输入" :size="size" class="width-400 margin-left-10" allowClear title="使用逗号( , )分隔,可配置多个包含或不包含"/>
                  <CloseCircleOutlined class="del-signal" title="删除" @click="delStepCondition(index)" style="cursor: pointer;color:red;margin-left: 5px;"/>
                </div>
              </div>
            </div>
          </div>
      </a-col>
      <a-col class="gutter-row" :span="24">
        <div class="gutter-box" style="height: 320px;">
          <div class="box-piece-title">限制条件（参数）</div>
          <a-divider class="divider-style"/>
          <div class="box-piece-content">
            <div style="height: 100px;overflow-y: auto;margin-top: 5px;">
                <div class="height-30" v-for="(val, index) in conditions" :key="index">
                  <PlusCircleOutlined @click="addCondition" class="width-50 icon-style" title="添加" :class="{icon_add_visibility: index != 0}"/>
                  <span>参数</span>
                  <a-select show-search option-filter-prop="label" v-model:value="val.paramName" :size="size" class="width-200 margin-left-10 margin-right-10" :options="paramArr" placeholder="请选择" allowClear></a-select>
                  <span>的</span>
                  <a-select show-search option-filter-prop="label" v-model:value="val.valType" :size="size" class="width-100 margin-left-10" :options="targetActualArr" placeholder="请选择" allowClear></a-select>
                  <a-select v-model:value="val.calculateType" :size="size" class="width-100 margin-left-10" :options="fetchConditionArr" placeholder="请选择" allowClear></a-select>
                  <a-input v-model:value="val.calculateValue" placeholder="请输入" :size="size" class="width-90 margin-left-10" allowClear/>
                  <DownCircleOutlined class="width-50 margin-left-10 icon-style" title="添加" :class="{icon_add_visibility: index != 0}" @click="addConditionData"/>
                </div>
            </div>
            <div class="text-content" style="height: 120px;">
                <div v-for="(item, index) in conditionHistory" :key="index">{{index+1}}.{{item}} <span class="del-signal" @click="delCondition(index)" style="cursor: pointer;color:red" title="删除"><CloseCircleOutlined/></span></div>
            </div>
          </div>
        </div>
      </a-col>
    </a-row>
  </a-spin>
</template>

<script lang="ts" setup>
  import { ref, reactive, defineExpose, nextTick, defineProps, computed, onMounted } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { Form } from 'ant-design-vue';
  import {getUnitList,getEopList,getEopParamList, adapterData, saveOrUpdate, getPcuList} from '../PiAdapter.api';
  import { DownCircleOutlined,PlusCircleOutlined,CloseCircleOutlined } from '@ant-design/icons-vue';
  import KtDict from '/@/components/KtDict/KtDict.vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();
  const { createMessage } = useMessage();

  const props = defineProps({
      formDisabled: { type: Boolean, default: false },
      formData: { type: Object, default: () => {} },
      formBpm: { type: Boolean, default: true },
      isAdd: { type: Boolean, default: true },
  });

  const size = ref('middle');
  const useForm = Form.useForm;
  const emit = defineEmits(['register', 'ok','close']);
  const confirmLoading = ref<boolean>(false);
  const isParam = ref(false);
  const formData = reactive<Record<string, any>>({
      id: '',
      pcuNo: '',
      severalType: '1',
  });
  
  //步骤条件筛选
  const stepConditions = reactive<Record<string, any>>([]);
  //步骤-条件
  const stepFetchConditionArr = reactive<Record<string, any>>([
    { value: '3', label: '包含' },
    { value: '4', label: '不包含' },
  ]);


  //条件结果集合-待写入
  const conditions = reactive<Record<string, any>>([]);
  //条件-历史记录(展示)
  const conditionHistory = reactive<Record<string, any>>([]);
  //条件-历史记录(存储)
  const conditionData = reactive<Record<string, any>>([]);

  //条件
  const fetchConditionArr = reactive<Record<string, any>>([
    { value: '1', label: '>' },
    { value: '2', label: '≥' },
    { value: '3', label: '=' },
    { value: '4', label: '≠' },
    { value: '5', label: '<' },
    { value: '6', label: '≤' },
  ]);
  //格式
  const timeFormatArr = reactive<Record<string, any>>([
    { value: 1, label: '分' },
    { value: 2, label: '时' },
    { value: 3, label: '天' }
  ]);
  //取值
  //  const yearHourArr = reactive<Record<string, any>>([]); 
  //pcu集合
  const pcuArr = reactive<Record<string, any>>([]);
  //设备集合
  const unitArr = reactive<Record<string, any>>([]); 
  //EOP集合
  const eopArr = reactive<Record<string, any>>([]); 
  //步骤-过滤
  const stepArr = reactive<Record<string, any>>([
    { value: 'recipeName', label: '配方' },
  ]);
  
  //参数-动态值
  const paramArr = reactive<Record<string, any>>([]);
  //参数-固定+动态
  const paramAllArr = reactive<Record<string, any>>([]);
  //参数-固定
  const paramFixedArr = reactive<Record<string, any>>([
    { value: 'startTime', label: '开始时间' },
    { value: 'startDate', label: '开始时间(日期)' },
    { value: 'endTime', label: '结束时间' },
    { value: 'endDate', label: '结束时间(日期)' },
    { value: 'runtime', label: '运行时长' },
    { value: 'formula', label: '配方' },
    { value: 'batchStartTime', label: '批次开始时间' },
    { value: 'deviceName', label: '设备' },
    { value: 'ordercatName', label: '生产配方' },
  ]);
  const targetActualArr = reactive<Record<string, any>>([
    { value: '0', label: '设定值' },
    { value: '1', label: '实际值' },
  ]);

  //追加步骤条件
  function addStepCondition(){
    let obj = {"paramName":"recipeName","calculateType":undefined,"calculateValue":""};
    stepConditions.push(obj);
  }
  //删除步骤条件
  function delStepCondition(index){
    stepConditions.splice(index, 1);
    if(stepConditions.length === 0){
      addStepCondition();
    }
  }
  //追加条件(参数)
  function addCondition(){
    let obj = {"id":"","paramName":undefined,"calculateType":undefined,"calculateValue":""};
    conditions.push(obj);
  }
  //删除历史
  function delCondition(index){
    conditionHistory.splice(index, 1);
    conditionData.splice(index, 1);
  }
  //写入记录
  function addConditionData(){
    //为空判断
    let str = "";
    let dataArr = [];
    conditions.forEach((e, index, array) => {
      if(e.paramName && e.valType && e.calculateType && e.calculateValue){
        //条件-字段
        let valType = getValueByKey(targetActualArr,e.valType)
        let pointName = e.paramName + "的" + valType 
        let explanation = "";
        //条件(大小小于..)
        let calculateType = getValueByKey(fetchConditionArr,e.calculateType)
        if(str){
          str += " 且 "+ pointName + calculateType +e.calculateValue
          explanation = " 且 "+ pointName + calculateType +e.calculateValue;
        }else{
          str += pointName + calculateType +e.calculateValue
          explanation = pointName + calculateType +e.calculateValue
        }
        let obj={
          "paramName": e.paramName,
          "valType": e.valType,
          "calculateType": e.calculateType,
          "calculateValue": e.calculateValue,
          "explanation": explanation
        }
        dataArr.push(obj);
      }
    });

    if(str){
      //适用于
      conditionData.push(dataArr);
      conditionHistory.push(str);
      //初始化写入
      conditions.length = 0;
      initCondition();
    }else{
      createMessage.warning("请配置条件!");
    }
  }

  //传入数组对象 获取指定key对应的值
  function getValueByKey(array,key) {
      let result;
      array.forEach((obj) => {
          if (obj["value"] === key) {
              result = obj["label"];
          }
      });
      return result;
  }

  //初始化条件
  function initCondition(){
    conditions.value=[];
    let obj = {"id":"","paramName":undefined,"calculateType":undefined,"calculateValue":""};
    conditions.push(obj);
  }

  //表单验证
  const validatorRules = {
      name: [{ required: true, message: `字段中文名称不为空!` }],
      type: [{ required: true, message: `字段类型不为空!` }],
  };
  const { resetFields, validate, validateInfos } = useForm(formData, validatorRules, { immediate: true });

  // 表单禁用
  const disabled = computed(() => {
    if (props.formBpm === true) {
      if (props.formData.disabled === false) {
        return false;
      } else {
        return true;
      }
    }
    return props.formDisabled;
  });


  onMounted(() => {
    //初始约束条件
    initCondition();
  });


  /**
   * 新增
   */
  function add(record) {
    edit(record);
  }

  /**
   * 编辑
   */
  function edit(record) {
    nextTick(() => {
      resetFields();
      //赋值
      formData.piTableId = record.piTableId;
      formData.piTable = record.piTable;
      formData.piCode = record.piCode;
      formData.piName = record.piName;
      console.log('获取表单数据', formData);
      //值回显
      if(record.id){
        Object.assign(formData, record);
        getPcu(formData.areaNo);
        getUnit(formData.pcuNo)
        getEop(formData.unitNo)
        getEopParam(formData.eopNo)
        //获取其他回显数据
        getAdapterData(record.id);
        valChange(record.val)
        //取第几次
        if(formData.several == '-1'){
          formData.severalType = '-1'
          formData.several = ''
        }else{
          formData.severalType = '1'
        }
        //判断步骤限制条件是否为空
        if(record.stepCondition && record.stepCondition != '[]'){
          const parsedData = JSON.parse(record.stepCondition);
          // 方法1：清空后推入新数据
          stepConditions.length = 0;
          stepConditions.push(...parsedData);
        }else{
          addStepCondition(); 
        }
      }else{
        addStepCondition();
      }
    });
  }

  //数据回显
  async function getAdapterData(id){
      let res = await adapterData({id});
      //条件
      if(res.adapterAlgorithmCondition){
        let adapterAlgorithmCondition = JSON.parse(res.adapterAlgorithmCondition)
        conditionData.push(...adapterAlgorithmCondition);
        adapterAlgorithmCondition.forEach((arr, index, array) => {
          let str = "";
          arr.forEach((e, index, array) => {
            str+=e.explanation;
          })
          if(str){
            conditionHistory.push(str);
          }
        })
        
      }
  };


  /**
   * 提交数据
   */
  async function submitForm() {
      // 触发表单验证
      await validate();
      confirmLoading.value = true;
      // const isUpdate = ref<boolean>(false);
      let formAllData = reactive<Record<string, any>>({});
      if(formData.severalType == '-1'){
        formData.several = '-1'
      }
      if(stepConditions && stepConditions.length>0){
        const filteredConditions = stepConditions.filter(item => {
          return item?.paramName && item?.calculateType && item?.calculateValue !== undefined && item?.calculateValue !== null && item?.calculateValue !== '';
        });
        formData.stepCondition = JSON.stringify(filteredConditions);
      }

      //计算信号calculateSignal
      formAllData.piAdapterAlgorithm = formData;
      //取数信号(多个值)二维数组
      formAllData.adapterAlgorithmCondition = JSON.stringify(conditionData);

     

      await saveOrUpdate(formAllData)
      .then((res) => {
      if (res.success) {
          createMessage.success(res.message);
          emit('ok', formData.id);
      } else {
          createMessage.warning(res.message);
      }
      })
      .finally(() => {
        if(formData.severalType == '-1'){
          formData.several = ''
        }
        confirmLoading.value = false;
      });
  }



  //获取PCU
  async function areaChange(val){
    unitArr.length = 0;
    formData.pcuNo = '';
    formData.unitNo = '';
    formData.eopNo = '';
    formData.val = '';
    formData.valType = '';
    conditions.length = 0;
    conditionHistory.length = 0;
    conditionData.length = 0;
    paramAllArr.length = 0;
    paramArr.length = 0;
    pcuArr.length = 0;
    initCondition();

    if(!val){
      return false;
    }
    getPcu(val);
  }

  async function getPcu(val){
    if(!val){return false;}
    let res = await getPcuList({"areaNo":val});
    let newArr = res.map(item => ({
        value: item.pcuNo,
        label: item.pcuName
    }));
    pcuArr.push(...newArr);
  }

  
  //获取设备
  async function pcuChange(val){
    unitArr.length = 0;
    formData.unitNo = '';
    formData.eopNo = '';
    formData.val = '';
    formData.valType = '';
    conditions.length = 0;
    conditionHistory.length = 0;
    conditionData.length = 0;
    paramAllArr.length = 0;
    paramArr.length = 0;
    initCondition();

    if(!val){
      return false;
    }
    getUnit(val);
  }
  async function getUnit(val){
    if(!val && !formData.pcuNo){return false;}
    let res = await getUnitList({"areaNo":formData.areaNo,"pcuNo":val});
    let newArr = res.map(item => ({
        value: item.unitNo,
        label: `${item.unitName}(${item.unitNo})`
    }));
    unitArr.push(...newArr);
  }



  //获取EOP
  async function unitChange(val){
    formData.eopNo = '';
    formData.val = '';
    formData.valType = '';
    conditions.length = 0;
    conditionHistory.length = 0;
    conditionData.length = 0;
    paramAllArr.length = 0;
    paramArr.length = 0;
    eopArr.length = 0;
    initCondition();
    if(!val){
      return false;
    }
    getEop(val)
  }
  async function getEop(val){
    if(!val && !formData.pcuNo){return false;}
    let res = await getEopList({"areaNo":formData.areaNo,"pcuNo":formData.pcuNo,"unitNo":val});
    let newArr = res.map(item => ({
        value: item.eopNo,
        label: item.eopName+"("+item.eopNo+")"
    }));
    eopArr.push(...newArr);
  }

  //获取EOP参数
  async function eopChange(val){
    paramArr.length = 0;
    paramAllArr.length = 0;
    if(!val){
      return false;
    }
    getEopParam(val);
  }
  //获取
  async function getEopParam(val){
    if(!val){
      return false;
    }
    let res = await getEopParamList({"areaNo":formData.areaNo,"unitNo":formData.unitNo,"eopNo":val,"pcuNo":formData.pcuNo});
    let newArr = res.map(item => ({
        value: item.paramName,
        label: item.paramName
    }));
    paramArr.push(...newArr);

    paramAllArr.push(...paramFixedArr)
    paramAllArr.push(...paramArr)
  }

  //
  function valChange(val){
    if(paramFixedArr.some(item => item.value === val)){
      formData.valType = ''
      isParam.value = false
    }else{
      isParam.value = true
    }
  }
  function isValue(val){
    if(val){
      return true;
    }else{
      return false;
    }
  }
  //取第几次设置
  function severalTypeChange(){
    if(formData.severalType == '-1'){
      formData.several = '';
    }
  }
  defineExpose({
      add,
      edit,
      submitForm,
  });
</script>

<style lang="less" scoped>
.gutter-box{
  width: 98%;
  height: 200px;
  background-color: #ffffff;
  margin: 0 auto;
  border-radius: 8px;
  color:#000000d9;
}
.gutter-box-bottom{
  width: 99%;
  height: 470px;
  background-color: #ffffff;
  margin: 0 auto;
  border-radius: 8px;
  color:#000000d9;
}
.box-piece-title{
  font-size: 14px;
  font-weight: 600;
  padding: 5px;
  color:#000000d9;
}
.box-piece-content{
  padding: 8px;
}
.margin-right-5{
  margin-right: 5px;
}
.margin-right-10{
  margin-right: 10px;
}
.margin-left-10{
  margin-left: 10px;
}
.height-30{
  height: 35px;
}
.height-35{
  height: 35px;
}

.width-50{
  width: 50px;
}
.width-70{
  width: 70px;
}
.width-80{
  width: 80px;
}
.width-90{
  width: 90px;
}
.width-100{
  width: 100px;
}
.width-120{
  width: 120px;
}
.width-150{
  width: 150px;
}
.width-170{
  width: 170px;
}
.width-200{
  width: 200px;
}
.width-250{
  width: 250px;
}
.width-400{
  width: 400px;
}
.text-content{
  background-color: #f7f8fa;
  width: 100%;
  margin-top: 5px;
  overflow-y: auto;
}
.text-count-content{
  background-color: #f7f8fa;
  width: 100%;
  margin-top: 5px;
}

.icon-style{
  font-size: 18px;
  color: #1677ff;
}

.title-icon{
  font-size: 14px;
  color: #1677ff;
}
.ant-divider-horizontal {
  display: flex;
  clear: both;
  width: 100%;
  min-width: 100%;
  margin: 0px 0;
}
.divider-style{
  height: 1px; 
  background-color: #f7f8fa;
}
.icon_add_visibility{
  visibility: hidden;
}
</style>