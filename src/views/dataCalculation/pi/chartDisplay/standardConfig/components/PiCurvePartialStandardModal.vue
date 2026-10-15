<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose :title="title" :width="1200"
  :minHeight="350"
  @ok="handleSubmit">
      <BasicForm @register="registerForm" name="PiCurvePartialStandardForm"/>
  </BasicModal>
</template>

<script lang="ts" setup>
  import {ref, computed, unref, nextTick} from 'vue';
  import {BasicModal, useModalInner} from '/@/components/Modal';
  import {BasicForm, useForm} from '/@/components/Form/index';
  import {getFormSchema} from '../PiCurvePartialStandard.data';
  import {getDevice, getRecipe, getStep, saveOrUpdate} from '../PiCurvePartialStandard.api';
  import {initDictOptions} from '/@/utils/dict/index';
  // Emits声明
  const emit = defineEmits(['register','success']);
  const isUpdate = ref(true);
  const isDetail = ref(false);
  const unitArray = ref([]);
  const recipeArray = ref<{value: string; label: string; recipeNo?: string; reccatNo?: string}[]>([]);
  const stepArray = ref<{value: string; label: string; ropId?: string; eopNo?: string}[]>([]);
  const dataSource = ref(1);
  const areaDictOptions = ref<{value: string; label: string}[]>([]);

  // 加载设备选项
  const fetchUnits = async (areaNo, recipeNo, reccatNo) => {
    if(!areaNo){
      return false;
    }
    const pcuNo = getFieldsValue().pcuNo;
    let res = await getDevice({areaNo, pcuNo, recipeNo, reccatNo, dataSource:dataSource.value })
    unitArray.value = []
    if(res){
      res.forEach((item) => {
        let device = item.deviceCode.split('-')[1];
        unitArray.value.push({
          value: device,
          label: item.deviceName
        })
      })
      return unitArray.value;
    }
    return [];
  };

  // 加载配方
  const fetchRecipes = async (areaNo) => {
    if(!areaNo){
      return [];
    }
    let res = await getRecipe({areaNo, dataSource: dataSource.value})
    recipeArray.value = []
    if(res){
      res.forEach((item) => {
        recipeArray.value.push({
          value: `${item.reccatNo}-${item.recipeNo}`,
          label: `${item.recipeName}${(item.reccatName || '')}`,
          recipeNo: item.recipeNo,
          reccatNo: item.reccatNo,
        })
      })
      return recipeArray.value;
    }
    return [];
  };

  // 加载步骤
  const fetchSteps = async (areaNo, unitNo, recipeNo, reccatNo) => {
    const pcuNo = getFieldsValue().pcuNo;
    let res = await getStep({areaNo, pcuNo, unitNo, recipeNo, reccatNo, dataSource:dataSource.value})
    stepArray.value = []
    if(res){
      res.forEach((item) => {
        stepArray.value.push({
          value: `${item.ropId}-${item.eopNo}`,
          label: item.eopName,
          ropId: item.ropId,
          eopNo: item.eopNo,
        })
      })
      return stepArray.value;
    }
    return [];
  };

  // ------------------------
  // 联动方法
  // ------------------------
  // 加载区域字典（拆分逗号分隔的value，如 value="1,2" label="糖化" → 糖化(1), 糖化(2)）
  const loadAreaDict = async () => {
    const dictCode = "mes3_mdm_dict where del_flag = '0' and group_code = 'MES3_PI_CURVE_AREA', item_key, item_val";
    const res = await initDictOptions(dictCode);
    const options: {value: string; label: string}[] = [];
    (res || []).forEach(item => {
      const val = (item.value || '').toString();
      const label = item.text || item.label || '';
      if (val.includes(',')) {
        val.split(',').forEach(subVal => {
          const trimmed = subVal.trim();
          if (trimmed) {
            options.push({ value: trimmed, label: `${label}(${trimmed})` });
          }
        });
      } else {
        options.push({ value: val, label });
      }
    });
    areaDictOptions.value = options;
    updateSchema({ field: 'pcuNo', componentProps: { options } });
  };

  // 区域变更 → 加载配方，启用配方选择
  const handleAreaChange = async (value, data, first = false) => {
    const found = areaDictOptions.value.find(item => item.value === value);
    if(found && found.label){
      setFieldsValue({ pcuName: found.label });
    }
    const recipeOptions = await fetchRecipes(value);
    updateSchema({ field: 'recipeNo', componentProps: { options: recipeOptions, disabled: !value || recipeOptions.length === 0 } });
    updateSchema({ field: 'unitNo', componentProps: { options: [], disabled: true } });
    updateSchema({ field: 'eopNo', componentProps: { options: [], disabled: true } });
    if(!first){
      setFieldsValue({ recipeNo: undefined, recipeName: '', unitNo: undefined, unitName: '', eopNo: undefined, eopName: '', ropId: '' });
    }
  };

  // 配方变更 → 加载设备，启用设备选择
  const handleRecipeChange = async (value, data, first = false) => {
    const item = recipeArray.value.find(item => item.value === value);
    let recipeCn = ""
    if(item && item.label){
      recipeCn = item.label
    }
    const realRecipeNo = item?.recipeNo || '';
    const reccatNo = item?.reccatNo || '';
    const unitOptions = await fetchUnits(getFieldsValue().pcuNo, realRecipeNo, reccatNo);
    updateSchema({ field: 'unitNo', componentProps: { options: unitOptions, disabled: !value || unitOptions.length === 0 } });
    updateSchema({ field: 'eopNo', componentProps: { options: [], disabled: true } });
    if(!first){
      setFieldsValue({ recipeName: recipeCn, unitNo: undefined, unitName: '', eopNo: undefined, eopName: '', ropId: '' });
    }
  };

  // 设备变更 → 加载步骤，启用步骤选择
  const handleUnitChange = async (value, data, first = false) => {
    let unitName = ""
    if(data && data.label){
      unitName = data.label
    }
    const { pcuNo, recipeNo: recipeVal } = getFieldsValue();
    // recipeNo 存的是 reccatNo-recipeNo，提取原始 recipeNo 用于 API 调用
    const recipeItem = recipeArray.value.find(item => item.value === recipeVal);
    const realRecipeNo = recipeItem?.recipeNo || recipeVal;
    const reccatNo = recipeItem?.reccatNo || '';
    const options = await fetchSteps(pcuNo, value, realRecipeNo, reccatNo);
    const isDisabled = !value || (options && options.length === 0);
    await nextTick();
    updateSchema({
      field: 'eopNo',
      componentProps: {
        options: options || [],
        disabled: isDisabled
      }
    });
    if(!first){
      setFieldsValue({ eopNo: undefined, eopName: '', ropId: '', unitName: unitName });
    }
  };

  // 步骤变更 → 自动填充ropId和步骤名称
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
    setFieldsValue({ ropId: ropNo, eopName: stepName });
  };

  // form schema 注入事件
  const [registerForm, { setProps, updateSchema, setFieldsValue, resetFields, validate, scrollToField, getFieldsValue }] = useForm({
    schemas: getFormSchema(handleAreaChange, handleRecipeChange, handleUnitChange, handleStepChange),
    showActionButtonGroup: false,
    baseColProps: { span: 24 },
  });

  // 在 useForm 之后加载区域字典，确保 updateSchema 可用
  loadAreaDict();

  // Modal 注册
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    await resetFields();
    dataSource.value = Number(data?.record?.dataSource) || 1;
    // dataSource=1 时才需要录入 ropId
    updateSchema({
      field: 'ropId',
      show: dataSource.value === 1,
      rules: dataSource.value === 1 ? [{ required: true, message: 'ropId不能为空', trigger: 'blur' }] : [],
    });
    // resetFields 会清空 schema 中的 options，需要重新加载区域下拉
    await loadAreaDict();
    setModalProps({
      confirmLoading: false,
      showCancelBtn: !!data?.showFooter,
      showOkBtn: !!data?.showFooter,
    });

    isUpdate.value = !!data?.isUpdate;
    isDetail.value = !!data?.showFooter;

    if (unref(isUpdate) && data.record) {
      // 1. 加载区域选项 → 加载配方选项
      if (data.record.pcuNo) await handleAreaChange(data.record.pcuNo, {}, true);
      await nextTick();
      // 先写入 pcuNo，后续联动依赖 getFieldsValue().pcuNo
      await setFieldsValue({ pcuNo: data.record.pcuNo, pcuName: data.record.pcuName });
      await nextTick();

      // 2. 将原始 recipeNo 转换为组合 value，触发配方联动 → 加载设备
      const recipeItem = recipeArray.value.find(item => String(item.recipeNo) === String(data.record.recipeNo));
      const recipeNoValue = recipeItem?.value || data.record.recipeNo;
      if (data.record.recipeNo) await handleRecipeChange(recipeNoValue, {}, true);
      await nextTick();
      // 写入组合格式的 recipeNo，使 Select 能匹配并显示 label
      await setFieldsValue({ recipeNo: recipeNoValue, recipeName: data.record.recipeName });
      await nextTick();

      // 3. 触发设备联动 → 加载步骤
      if (data.record.unitNo) await handleUnitChange(data.record.unitNo, {}, true);
      await nextTick();
      await setFieldsValue({ unitNo: data.record.unitNo, unitName: data.record.unitName });
      await nextTick();

      // 4. 将原始 eopNo 转换为组合 value，设置步骤值
      const stepItem = stepArray.value.find(item => String(item.eopNo) === String(data.record.eopNo));
      const eopNoValue = stepItem?.value || data.record.eopNo;
      await setFieldsValue({ eopNo: eopNoValue, eopName: data.record.eopName, ropId: data.record.ropId });
      await nextTick();

      // 5. 设置其余字段
      const { pcuNo: _, recipeNo: __, unitNo: ___, eopNo: ____, pcuName: _____, recipeName: ______, unitName: _______, eopName: ________, ropId: _________, ...rest } = data.record;
      if (Object.keys(rest).length > 0) {
        await setFieldsValue(rest);
      }
    } else {
      // 新增时设置默认值
      await setFieldsValue({ ...data.record });
    }
    // 禁用整个表单
    setProps({ disabled: !data?.showFooter });
  });

  // 表单提交
  async function handleSubmit() {
    try {
      const values = await validate();
      // recipeNo 存的是 reccatNo-recipeNo，提取原始 recipeNo 和 reccatNo
      const recipeItem = recipeArray.value.find(item => item.value === values.recipeNo);
      if (recipeItem) {
        values.recipeNo = recipeItem.recipeNo;
        values.reccatNo = recipeItem.reccatNo;
      }
      // eopNo 存的是 ropId-eopNo，提取原始 eopNo
      const stepItem = stepArray.value.find(item => item.value === values.eopNo);
      if (stepItem) {
        values.eopNo = stepItem.eopNo;
      }
      values.dataSource = dataSource.value;
      setModalProps({ confirmLoading: true });
      await saveOrUpdate(values, isUpdate.value);
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
