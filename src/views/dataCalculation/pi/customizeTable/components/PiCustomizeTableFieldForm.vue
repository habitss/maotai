<template>
    <a-spin :spinning="confirmLoading">
    <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol">
      <a-row>
        <!-- 字段中文名称 -->
        <a-col :span="24">
          <a-form-item label="字段中文名称" v-bind="validateInfos.name">
            <a-input v-model:value="formData.name" placeholder="请输入字段中文名称" :disabled="disabled" />
          </a-form-item>
        </a-col>
        <!-- 字段类型 -->
        <a-col :span="24">
          <a-form-item label="字段类型" v-bind="validateInfos.type">
            <a-select v-model:value="formData.type" allowClear placeholder="请选择字段类型" :disabled="formData.id?true:false" @change="handleMappingChange">
                <a-select-option :value="item.value" v-for="(item, index) in fieldTypeArr" :key="item.value">
                  {{ item.label }}
                </a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <!--字段长度-->
        <a-col :span="24" v-if="formData.type == '1'">
          <a-form-item label="字段长度" v-bind="validateInfos.fieldLength">
            <a-input-number v-model:value="formData.fieldLength" placeholder="请输入字段长度" :disabled="formData.id?true:false" :min="0" :max="800" style="width: 100%;"/>
          </a-form-item>
        </a-col>
        <!--是否表单显示-->
        <!-- <a-col :span="24">
          <a-form-item label="报表名称" v-bind="validateInfos.name">
            <a-input v-model:value="formData.name" :placeholder="请输入报表名称" :disabled="disabled" />
          </a-form-item>
        </a-col> -->
        <!-- 是否列表显示 -->
        <a-col :span="24">
          <a-form-item label="是否列表显示" v-bind="validateInfos.isList">
            <a-switch checkedChildren="是" unCheckedChildren="否" v-model:checked="modelData.isList" />
          </a-form-item>
        </a-col>

        <!-- 是否排序 -->
        <a-col :span="24">
          <a-form-item label="是否排序" v-bind="validateInfos.isSort">
            <a-switch checkedChildren="是" unCheckedChildren="否" v-model:checked="modelData.isSort" />
          </a-form-item>
        </a-col>
        <!-- 排序方式 -->
        <a-col :span="24" v-if="modelData.isSort">
          <a-form-item label="排序方式" v-bind="validateInfos.sortType">
            <a-switch checkedChildren="升序" unCheckedChildren="降序" v-model:checked="modelData.sortType" />
          </a-form-item>
        </a-col>
        <!-- 是否查询条件 -->
        <a-col :span="24">
          <a-form-item label="是否查询条件" v-bind="validateInfos.isQuery">
            <a-switch checkedChildren="是" unCheckedChildren="否" v-model:checked="modelData.isQuery" />
          </a-form-item>
        </a-col>
        <!--是否模糊查询-->
        <a-col :span="24" v-if="modelData.isQuery === true">
          <a-form-item label="是否模糊查询" v-bind="validateInfos.isLike">
            <a-switch checkedChildren="是" unCheckedChildren="否" v-model:checked="modelData.isLike" />
          </a-form-item>
        </a-col>

        <!--是否唯一约束-->
        <a-col :span="24">
          <a-form-item label="是否唯一约束" v-bind="validateInfos.isUnique">
            <a-switch checkedChildren="是" unCheckedChildren="否" v-model:checked="modelData.isUnique" />
          </a-form-item>
        </a-col>

        <!--是否可编辑-->
        <a-col :span="24">
          <a-form-item label="是否可编辑" v-bind="validateInfos.isEdit">
            <a-switch checkedChildren="是" unCheckedChildren="否" v-model:checked="modelData.isEdit" />
          </a-form-item>
        </a-col>

        <!--是否固定列-->
        <a-col :span="24">
          <a-form-item label="是否固定列" v-bind="validateInfos.isFixed">
            <a-switch checkedChildren="是" unCheckedChildren="否" v-model:checked="modelData.isFixed" />
          </a-form-item>
        </a-col>
        
        <!--是否重点关注-->
        <a-col :span="24">
          <a-form-item label="是否重点关注PI" v-bind="validateInfos.isFocus">
            <a-switch checkedChildren="是" unCheckedChildren="否" v-model:checked="modelData.isFocus" />
          </a-form-item>
        </a-col>
        
        <!--是否工厂关注PI-->
        <a-col :span="24">
          <a-form-item label="是否工厂关注PI" v-bind="validateInfos.isFactoryFocus">
            <a-switch checkedChildren="是" unCheckedChildren="否" v-model:checked="modelData.isFactoryFocus" />
          </a-form-item>
        </a-col>

        <!--是否参与绿区率统计-->
        <a-col :span="24">
          <a-form-item label="是否参与绿区率统计" v-bind="validateInfos.isStatistics">
            <a-switch checkedChildren="是" unCheckedChildren="否" v-model:checked="modelData.isStatistics" />
          </a-form-item>
        </a-col>
        <!--绿区率目标值-->
        <a-col :span="24">
          <a-form-item label="绿区率目标值(%)" v-bind="validateInfos.greenTarget">
            <a-input-number v-model:value="formData.greenTarget" style="width: 100%" placeholder="绿区率目标值"/>
          </a-form-item>
        </a-col>

        <!--填报项组件-->
        <a-col :span="24">
          <a-form-item label="填报项组件" v-bind="validateInfos.editWidgetType">
            <a-select v-model:value="formData.editWidgetType" allowClear placeholder="填报项组件" :disabled="editWidgetTypeDisabled">
                <a-select-option :value="item.value" v-for="(item, index) in componentOptions" :key="item.value" :disabled="item.disabled">
                  {{ item.label }}
                </a-select-option>
            </a-select>
          </a-form-item>
        </a-col>

        <!--组件字典配置-->
        <a-col :span="24" v-if="formData.editWidgetType == 'select'">
          <a-form-item label="组件字典配置" v-bind="validateInfos.dropdownData">
            <a-select v-model:value="formData.dropdownData" allowClear placeholder="组件字典配置">
                <a-select-option :value="item.dictGroupCode" v-for="(item, index) in dictOption" :key="item.dictGroupCode">
                  {{ item.dictGroupName }}
                </a-select-option>
            </a-select>
          </a-form-item>
        </a-col>

        <!--保留几位小数-->
        <a-col :span="24" v-if="formData.type !='4' && formData.type!='6'">
          <a-form-item label="保留几位小数" v-bind="validateInfos.keepDecimals">
            <a-input-number v-model:value="formData.keepDecimals" style="width: 100%" :disabled="disabled" :min="0" :max="10"/>
          </a-form-item>
        </a-col>

        <!--取值方式-->
        <a-col :span="24" v-if="formData.type !='4' && formData.type!='6'">
          <a-form-item label="取值方式" v-bind="validateInfos.takingValue">
            <a-select v-model:value="formData.takingValue" allowClear placeholder="请选择取值方式">
                <a-select-option :value="item.value" v-for="(item, index) in takingValueArr" :key="item.value">
                  {{ item.label }}
                </a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <!--是否自动PI-->
        <a-col :span="24">
          <a-form-item label="是否自动PI" v-bind="validateInfos.automaticPi">
            <a-select v-model:value="formData.automaticPi" :options="automaticArr" placeholder="请选择" allowClear></a-select> 
          </a-form-item>
        </a-col>

        <!-- 排序 -->
        <a-col :span="24">
          <a-form-item label="排序" v-bind="validateInfos.sort">
            <a-input-number v-model:value="formData.sort" :placeholder="t('language.enterSort')" style="width: 100%" :disabled="disabled" />
          </a-form-item>
        </a-col>
        <!--单位-->
        <a-col :span="24">
          <a-form-item label="单位" v-bind="validateInfos.piUnit">
            <a-input v-model:value="formData.piUnit" placeholder="单位" style="width: 100%"/>
          </a-form-item>
        </a-col>
        <!--Adapter唯一约束-->
        <a-col :span="24">
          <a-form-item label="唯一约束" v-bind="validateInfos.adapterOnly">
          <kt-dict
                v-model:value="formData.adapterOnly"
                placeholder="请选择Adapter/RA唯一约束"
                dict="MES3_PI_ADAPTER_ONLY"
                :disabled="disabled"
              />
          </a-form-item>
        </a-col>
        <!--是否启用设定值-->
        <a-col :span="24">
          <a-form-item label="是否启用设定值" v-bind="validateInfos.isValueEnabled">
            <a-switch checkedChildren="是" unCheckedChildren="否" v-model:checked="modelData.isValueEnabled" />
          </a-form-item>
        </a-col>
        <!--设定值-->
        <a-col :span="24" v-if="modelData.isValueEnabled =='1'">
          <a-form-item label="设定值" v-bind="validateInfos.setValue">
            <a-input v-model:value="formData.setValue" placeholder="请输入设定值" :disabled="disabled" />
          </a-form-item>
        </a-col>

        <!--是否显示历史数据-->
        <a-col :span="24" v-if="(formData.type == '1' || formData.type == '2' || formData.type == '2') && modelData.isQuery == '1' && (!formData.editWidgetType || formData.editWidgetType == 'input')">
          <a-form-item label="查询是否显示历史" v-bind="validateInfos.historyData">
            <a-popover placement="left">
              <template #title>
                <span>功能说明</span>
              </template>
              <template #content>
                <p>开启后，查询输入框会展示最新数据</p>
              </template>
              <a-switch checkedChildren="是" unCheckedChildren="否" v-model:checked="modelData.historyData" />
            </a-popover>
          </a-form-item>
        </a-col>
        <!--时间展示格式-->
         <a-col :span="24" v-if="formData.type =='6'">
          <a-form-item label="时间展示格式" v-bind="validateInfos.timeFormat">
            <a-select v-model:value="formData.timeFormat" allowClear placeholder="请选择时间展示格式">
                <a-select-option :value="item.value" v-for="(item, index) in dateTimeFormatArr" :key="item.value">
                  {{ item.label }}
                </a-select-option>
            </a-select>
          </a-form-item>
        </a-col>

        <!-- 备注 -->
        <a-col :span="24">
          <a-form-item :label="t('language.remark')" v-bind="validateInfos.notes">
            <a-textarea v-model:value="formData.notes" rows="3" :placeholder="t('language.enterRemark')" :disabled="disabled" />
          </a-form-item>
        </a-col>
      </a-row>
       <div style="text-align: center;margin-top: 20px;">
        <a-button style="margin-right: 8px" @click="onClose">取消</a-button>
        <a-button type="primary" @click="submitForm">保存</a-button>
      </div>
    </a-form>
  </a-spin>
</template>

<script lang="ts" setup>
  import { ref, reactive, nextTick, computed, onMounted, watch, inject } from 'vue';
  import { defHttp } from '/@/utils/http/axios';
  import { useMessage } from '/@/hooks/web/useMessage';
  import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';
  import JSelectDept from '/@/components/Form/src/jeecg/components/JSelectDept.vue';
  import { getValueType } from '/@/utils';
  import { Form } from 'ant-design-vue';
  import KtDict from '/@/components/KtDict/KtDict.vue';
  import KtDevice from '/@/components/KtDeviceType/KtDeviceType.vue';
  import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
  import {saveOrUpdate,sort} from '../PiCustomizeTableField.api';
  import { getDictList } from '/@/views/dataCalculation/customReport/customReport/index.api';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();
  import { useUserStore } from '/@/store/modules/user';
  const departObject = inject('departObject');
  // 用户信息
  const userStore = useUserStore();
  //是否自动报表
  const automaticArr = reactive<Record<string, any>>([
      { value: '0', label: '手动PI' },
      { value: '1', label: '自动PI' }
  ]);

  const props = defineProps({
      formDisabled: { type: Boolean, default: false },
      formData: { type: Object, default: () => {} },
      formBpm: { type: Boolean, default: true },
      isAdd: { type: Boolean, default: true },
  });

  const formRef = ref();
  const useForm = Form.useForm;
  const emit = defineEmits(['register', 'ok']);
  //录入组件是否可编辑
  const editWidgetTypeDisabled = ref<boolean>(false);

  const formData = reactive<Record<string, any>>({
      id: '',
      name:'',
      type:'',
      fieldLength:'',
      deptCode: '',
      lineCode: '',
      isList: 1,
      isSort: 0,
      sortType: 0,
      isQuery: 0,
      isLike: 0,
      isUnique:0,
      isEdit:0,
      keepDecimals:2,
      takingValue:''

  });
  const modelData = reactive<Record<string, any>>({
      // isForm: true,
      isList: true,
      isSort: false,
      sortType: true,
      isQuery: false,
      isLike: false,
      isUnique:false,
      isEdit:false,
      isFixed:false,
      isFocus: false,
      isFactoryFocus: false,
      isStatistics: false,
      isValueEnabled: false,
      historyData: false
  });
  //字段类型
  const fieldTypeArr = reactive<Record<string, any>>([
      { value: '1', label: '字符串' },
      { value: '2', label: '整数' },
      // { value: '3', label: 'Double' },
      { value: '4', label: '日期' },
      { value: '5', label: '大文本' },
      { value: '6', label: '时间' }
  ]);
   
  //填报项组件
  const componentOptions = [
    { value: 'input', label: '输入框' },
    { value: 'select', label: '选择框' },
    { value: 'selectUser', label: '用户选择框' },
    { value: 'datepicker', label: '日期选择框', disabled: true },
    { value: 'dateTimePicker', label: '时间选择框', disabled: true },
    { value: 'textarea', label: '超长文本框', disabled: true },
  ];
  //取值方式
  const takingValueArr = reactive<Record<string, any>>([
    { value: '1', label: '向上取整' },
    { value: '2', label: '向下取整' },
    { value: '3', label: '四舍五入' },
    { value: '4', label: '截取' }
  ]);
  //时间展示格式
  const dateTimeFormatArr = reactive<Record<string, any>>([
    { value: '1', label: '年月日时分秒' },
    { value: '2', label: '年月日' },
    { value: '3', label: '时分秒' },
    { value: '4', label: '时分' },
    { value: '5', label: '时' }
  ]);

  //字典
  const dictOption = ref([] as any);

  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 6 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  //表单验证
  const validatorRules = {
      // factoryCode: [{ required: true, message: `${t('language.enterFactory')}!` }],
      name: [{ required: true, message: `字段中文名称不为空!` }],
      type: [{ required: true, message: `字段类型不为空!` }],
      // fieldLength: [{ required: true, message: `字段长度不为空!` }],
      // stepName: [{ required: true, message: `步骤名称不为空!` }],
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
  const setEditStatus = computed(() => {
  if (!props.isAdd) {
      return true;
  }
  return false;
  });

  onMounted(() => {
    getDictData();
  });
  //获取字典数据
  async function getDictData(){
    let row = await getDictList({pageSize: 999});
    dictOption.value = row.records
  }

  function init(){
    modelData.isList = formData.isList == '1'?true:false;
    modelData.isSort = formData.isSort == '1'?true:false;
    modelData.sortType = formData.sortType == '1'?true:false;
    modelData.isQuery = formData.isQuery == '1'?true:false;
    modelData.isLike = formData.isLike == '1'?true:false;
    modelData.isUnique = formData.isUnique == '1'?true:false;
    modelData.isEdit = formData.isEdit == '1'?true:false;
    modelData.isFixed = formData.isFixed == '1'?true:false;
    modelData.isFocus = formData.isFocus == '1'?true:false;
    modelData.isFactoryFocus = formData.isFactoryFocus == '1'?true:false;
    modelData.isStatistics = formData.isStatistics == '1'?true:false;
    modelData.isValueEnabled = formData.isValueEnabled == '1'?true:false;
    modelData.historyData = formData.historyData == '1'?true:false;
  }

/**
 * 新增
 */
function add(tableId) {
  formData.tableId = tableId
  edit({});
  //获取排序
  getSort();
}

/**
 * 编辑
 */
function edit(record) {
  nextTick(() => {
    resetFields();
    //赋值
    Object.assign(formData, record);
    console.log('获取表单数据', formData);
    init();
  });
}

  /**
   * 提交数据
   */
  async function submitForm() {
      // 触发表单验证
      await validate();
      //字符串判断长度是否为空
      if(formData.type =='1' && !formData.fieldLength){
        createMessage.warning("字段长度不为空");
        return false;
      }
      confirmLoading.value = true;
      const isUpdate = ref<boolean>(false);
      
      //是否启用转换
      // formData.value.isForm = modelData.value.isForm ? 1 : 0
      formData.isList = modelData.isList ? 1 : 0
      formData.isSort = modelData.isSort ? 1 : 0
      formData.sortType = modelData.sortType ? 1 : 0
      if(formData.isSort === 0){
        formData.sortType="";
      }
      formData.isQuery = modelData.isQuery ? 1 : 0
      formData.isLike = modelData.isLike ? 1 : 0
      formData.isUnique = modelData.isUnique ? 1 : 0
      formData.isEdit = modelData.isEdit ? 1 : 0
      formData.isFixed = modelData.isFixed ? 1 : 0
      formData.isFocus = modelData.isFocus ? 1 : 0
      formData.isFactoryFocus = modelData.isFactoryFocus ? 1 : 0
      formData.isStatistics = modelData.isStatistics ? 1 : 0
      formData.isValueEnabled = modelData.isValueEnabled ? 1 : 0
      formData.historyData = modelData.historyData ? 1 : 0
      //时间格式化
      let model = formData;
      if (model.id) {
          isUpdate.value = true;
      }else{
        model.deptCode = userStore.departCode;
      }
      if(!model.takingValue){
        model.takingValue="";
      }
      //循环数据
      for (let data in model) {
          //如果该数据是数组并且是字符串类型
          if (model[data] instanceof Array) {
            let valueType = getValueType(formRef.value.getProps, data);
            //如果是字符串类型的需要变成以逗号分割的字符串
            if (valueType === 'string') {
                model[data] = model[data].join(',');
            }
          }
      }
      model.factoryCode = departObject.value.factoryCode;
      model.deptCode = departObject.value.deptCode;
      model.lineCode = departObject.value.lineCode;
      
      await saveOrUpdate(model, isUpdate.value)
          .then((res) => {
          if (res.success) {
              // const successMessage = res.message ? res.message : t('language.save');
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

  /**
   * 初始化设备类型下拉数据
   */
  async function getDeviceTypeArr() {
      deviceTypeList({ pageNo: 1, pageSize: 999 }).then((res) => {
          if (res && res.records && res.records.length > 0) {
          res.records.map((item) => {
              return { text: item.itemKey, value: item.itemVal };
          });
          }
      });
  }

  function getChange() {
      formData.deptCode = '';
      formData.lineCode = '';
  }

  function getDepartObject(ret) {
      if (ret.memo) {
          formData.orgTypeCode = undefined;
          formData.orgTypeCode = ret.memo;
          //根据结构类型值获取设备类型
          // findDeviceTypeByOrgCode({ orgTypeCode: ret.memo });
      }
  }
  //更换类型
  function handleMappingChange(value, obj){
    editWidgetTypeDisabled.value = false;
    //录入组件类型
    formData.editWidgetType = "";
    //字段长度
    formData.fieldLength = "";
    if(value === '1' || value === '5'){
      componentOptions.forEach((option) => {
        option.disabled = false;
        if (option.value === 'datepicker' || option.value === 'dateTimePicker') {
            option.disabled = true;
        }
      });
      if(value === '1'){
        formData.fieldLength = 50;
      }
    }else{
      editWidgetTypeDisabled.value = true;
      if(value === '2'){
        formData.editWidgetType = "input";
      }else if(value === '4'){
        formData.editWidgetType = "datepicker";
      }else if(value === '6'){
        formData.editWidgetType = "dateTimePicker";
      }      
    }
  }

  //获取排序
  async function getSort(){
    let row = await sort({tableId: formData.tableId});
    if(row && row.length>0){
      formData.sort = row[0].sort;
    }else{
      formData.sort = 1;
    }
  }


  defineExpose({
      add,
      edit,
      submitForm,
  });


  function onClose(){
    emit('close');
  }
</script>

<style lang="less" scoped>
// @import '../../common.less';
</style>