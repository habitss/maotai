<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose :title="title" :width="1200" 
  :minHeight="350"
  @ok="handleSubmit">
      <BasicForm @register="registerForm" name="PiCurveStandardForm"/>
  </BasicModal>
</template>

<script lang="ts" setup>
  import {ref, computed, unref, onMounted} from 'vue';
  import {BasicModal, useModalInner} from '/@/components/Modal';
  import {BasicForm, useForm} from '/@/components/Form/index';
  import {getFormSchema } from '../PiCurveStandard.data';
  import { getDevice, getRecipe, getStep,configSaveOrUpdate} from '../PiCurveStandard.api';
  // Emits声明
  const emit = defineEmits(['register','success']);
  const isUpdate = ref(true);
  const isDetail = ref(false);
  const unitArray = ref([]);
  const recipeArray = ref([]);
  const stepArray = ref([]);

  // 加载设备选项
  const fetchUnits = async (dataSource, areaNo, recipeNo, recipeName) => {
    if(!areaNo){
      return false;
    }
    // 根据数据源和区域过滤设备
    let res = await getDevice({dataSource, areaNo, recipeNo, recipeName})
    unitArray.value = []
    if(res){
      res.forEach((item, index, array) => {
        //0标准和1目标
        let device = item.deviceCode.split('-')[1];
        unitArray.value.push({
          value: `${device}`,
          label: item.deviceName
        })
      })
      return unitArray.value;
    }
    return [];
  };
  // 加载配方
  const fetchRecipes = async (dataSource, areaNo, unitNo, recipeNo, recipeName) => {
    //let device = unitNo.split('-')[1]; // 结果为 '3'
    let res = await getRecipe({dataSource, areaNo , unitNo, recipeNo, recipeName})
    recipeArray.value = []
    if(res){
      res.forEach((item, index, array) => {
        //0标准和1目标
        recipeArray.value.push({
          value: `${item.recipeNo}`,
          label: item.recipeName
        })
      })
      return recipeArray.value;
    }
    return [];
  };
  // 加载步骤
  const fetchSteps = async (dataSource, areaNo, unitNo, recipeNo, recipeName) => {
    let res = await getStep({dataSource, areaNo, unitNo, recipeNo,recipeName})
    stepArray.value = []
    if(res){
      res.forEach((item, index, array) => {
        //0标准和1目标
        stepArray.value.push({
          value: `${item.eopNo}`,
          label: item.eopName,
          ropId: item.ropId,
        })
      })
      return stepArray.value;
    }
    return [];
  };

  // ------------------------
  // 联动方法
  // ------------------------
  //数据源变更  `
  const handleDataSourceChange = async (value ,data ,first = false) => {
    const areaNo = getFieldsValue().areaNo;
    const options = await fetchUnits(value, areaNo);
    updateSchema({ field: 'unitNo', componentProps: { options } });
    if(!first){
      setFieldsValue({ unitNo: undefined,unitName:'', recipeNo: undefined,recipeName:'', stepNo: undefined, stepName: ''});
    }
  };

  //pcu变更
  const handleAreaChange = async (value , data,first = false) => {
    const dataSource = getFieldsValue().dataSource;
    const recipeNo = getFieldsValue().recipeNo
    const recipeName = getFieldsValue().recipeName
    const options = await fetchUnits(dataSource, value, recipeNo, recipeName);
    updateSchema({ field: 'unitNo', componentProps: { options } });
    if(!first){
      setFieldsValue({ unitNo: undefined,unitName:'', recipeNo: undefined,recipeName:'', stepNo: undefined, stepName: ''});
    }
  };

  //设备变更
  const handleUnitChange = async (value , data, first = false) => {
    const dataSource = getFieldsValue().dataSource;
    const areaNo = getFieldsValue().areaNo;
    const recipeNo = getFieldsValue().recipeNo
    const recipeName = getFieldsValue().recipeName
    //设备名称
    // const item = unitArray.value.find(item => item.value === value);
    // let unitCn = ""
    // if(item && item.label){
    //   unitCn = item.label
    // }
    let units = ""
    if(value && Array.isArray(value)){
       units = value.join(',')
    }
    let unitNames = ""
    if(data && data.length > 0 ){
      unitNames = data.map(item => item.label).join(',')
    }

    //const options = await fetchRecipes(dataSource, areaNo, units, recipeNo, recipeName);
    // updateSchema({ field: 'recipeNo', componentProps: { options } });
    const options = await fetchSteps(dataSource,areaNo,units,recipeNo,recipeName);
    updateSchema({ field: 'eopNo', componentProps: { options } });
    if(!first){
      setFieldsValue({ stepNo: undefined , setpName:'', unitName: unitNames});
    }
  };

  //配方变更
  const handleRecipeChange = async (value , data,first = false) => {
    const dataSource = getFieldsValue().dataSource;
    const areaNo = getFieldsValue().areaNo;
    const unitNo = getFieldsValue().unitNo;
    const item = recipeArray.value.find(item => item.value === value);
    let recipeCn = ""
    if(item && item.label){
      recipeCn = item.label
    }
    const options = await fetchSteps(dataSource,areaNo,unitNo,value);
    updateSchema({ field: 'eopNo', componentProps: { options } });
    if(!first){
      setFieldsValue({ stepNo: undefined, stepName: '', recipeName:recipeCn });
    }
  };

  //步骤变更
  const handleStepChange = async (value) => {
    const item = stepArray.value.find(item => item.value === value);
    let ropNo = "";
    let stepName = "";
    if(item && item.ropId){
      ropNo = item.ropId
    }
    if(item && item.label){
      stepName = item.label
    }
    setFieldsValue({ ropId:  ropNo, eopName: stepName});
  };

  //速率变更 - 计算标准时长
  const handleRateChange = async (e) => {
    // Input组件onChange返回事件对象，需要从e.target.value获取值
    const value = e?.target?.value ?? e;
    if (!value) {
      return;
    }
    const fields = getFieldsValue();
    const initialValue = parseFloat(fields.initialValue);
    const endValue = parseFloat(fields.endValue);
    const durationType = fields.durationType;
    const rate = parseFloat(value);

    // 判断是否都有值且为有效数字
    if (!isNaN(initialValue) && !isNaN(endValue) && !isNaN(rate) && rate !== 0 && durationType) {
      // 标准时长 = (标准结束值 - 标准起始值) / 速率
      const duration = Math.abs((endValue - initialValue) / rate);
      // 保留2位小数
      setFieldsValue({ duration: duration.toFixed(2) });
    }
  };

  //标准时长变更 - 计算速率
  const handleDurationChange = async (e) => {
    const value = e?.target?.value ?? e;
    if (!value) {
      return;
    }
    const fields = getFieldsValue();
    const initialValue = parseFloat(fields.initialValue);
    const endValue = parseFloat(fields.endValue);
    const durationType = fields.durationType;
    const duration = parseFloat(value);

    if (!isNaN(initialValue) && !isNaN(endValue) && !isNaN(duration) && duration !== 0 && durationType) {
      // 速率 = (标准结束值 - 标准起始值) / 标准时长
      const rate = Math.abs((endValue - initialValue) / duration);
      setFieldsValue({ rate: rate.toFixed(2) });
    }
  };


  // form schema 注入事件
  const [registerForm, { setProps,updateSchema, setFieldsValue, resetFields, validate, scrollToField,getFieldsValue }] = useForm({
    schemas: getFormSchema(handleDataSourceChange, handleAreaChange, handleUnitChange, handleRecipeChange, handleStepChange, handleRateChange, handleDurationChange),
    showActionButtonGroup: false,
    baseColProps: { span: 24 },
  });
  // Modal 注册
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    await resetFields();

    setModalProps({
      confirmLoading: false,
      showCancelBtn: !!data?.showFooter,
      showOkBtn: !!data?.showFooter,
    });

    isUpdate.value = !!data?.isUpdate;
    isDetail.value = !!data?.showFooter;

    if (unref(isUpdate) && data.record) {
      await setFieldsValue({ ...data.record });

      // 编辑态回显时手动触发联动
      //if (data.record.dataSource) await handleDataSourceChange(data.record.dataSource,{}, true);
      if (data.record.unitNo) {
        await setFieldsValue({ unitNo: data.record.unitNo.split(',') });
        await handleUnitChange(data.record.unitNo,{}, true);
      }

    }else{
      // 新增时设置默认值
      await setFieldsValue({ ...data.record });

      // 如果有设备名称则设置
      if (data.record.unitNo) {
        await setFieldsValue({ unitNo: data.record.unitNo.split(',') });
      }else{
        // 列表为空时，清空设备名称
        await setFieldsValue({ unitNo: [], unitName: '' });
      }
    }
    if (data.record.areaNo) await handleAreaChange(data.record.areaNo,{}, true);
    if (data.record.recipeNo) await handleRecipeChange(data.record.recipeNo,{}, true);
    // 禁用整个表单
    setProps({ disabled: !data?.showFooter });
  });

  // 表单提交
  async function handleSubmit() {
    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });

      await configSaveOrUpdate(values, isUpdate.value);
      closeModal();
      emit('success');
    } catch ({ errorFields }) {
      if (errorFields?.length) {
        scrollToField(errorFields[0].name, { behavior: 'smooth', block: 'center' });
      }
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }

  // Modal 标题
  const title = computed(() => (!unref(isUpdate) ? '新增' : !unref(isDetail) ? '详情' : '编辑'));
</script>

<style lang="less" scoped>
	/** 时间和数字输入框样式 */
  :deep(.ant-input-number) {
    width: 100%;
  }

  :deep(.ant-calendar-picker) {
    width: 100%;
  }

  // 设备名称多选标签不换行，超出隐藏
  :deep(.ant-select-selector) {
    flex-wrap: nowrap !important;
    overflow: hidden !important;
  }
  :deep(.ant-select-selection-overflow) {
    flex-wrap: nowrap !important;
  }

  // 下拉菜单在控件下方显示
  :deep(.ant-select) {
    .ant-select-dropdown {
      top: 100% !important;
      left: 0 !important;
    }
  }
</style>