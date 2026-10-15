<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="KpiTagForm">
          <a-row>
            <a-col :span="24">
              <a-form-item label="能源类型" v-bind="validateInfos.energyType" id="KpiTagForm-energyType" name="energyType">
                <j-dict-select-tag
                  :showChooseOption="false"
                  v-model:value="formData.energyType"
                  dictCode="mes3_mdm_dict where group_code = 'EMS_TagType',item_key,item_val"
                  @change="changeEnergyType"
                />
              </a-form-item>
            </a-col>
            <!-- <a-col :span="24">
              <a-form-item label="工厂" v-bind="validateInfos.factoryCode" id="KpiTagForm-factoryCode" name="factoryCode">
                <j-dict-select-tag
                  v-model:value="formData.factoryCode"
                  dictCode="sys_depart,depart_name,org_code"
                  placeholder="请选择工厂"
                  allow-clear
                  disabled
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="部门" v-bind="validateInfos.deptCode" id="KpiTagForm-deptCode" name="deptCode">
                <j-dict-select-tag
                  v-model:value="formData.deptCode"
                  dictCode="sys_depart,depart_name,org_code"
                  placeholder="请选择部门"
                  allow-clear
                  disabled
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="产线" v-bind="validateInfos.lineCode" id="KpiTagForm-lineCode" name="lineCode">
                <j-dict-select-tag
                  v-model:value="formData.lineCode"
                  dictCode="sys_depart,depart_name,org_code"
                  placeholder="请选择产线"
                  allow-clear
                  disabled
                />
              </a-form-item>
            </a-col> -->
            <a-col :span="24">
              <a-form-item label="KPI结构" v-bind="validateInfos.structId" id="KpiTagForm-structId" name="structId">
                <a-select
                  v-model:value="formData.structId"
                  placeholder="请选择KPI结构"
                  @change="changeStruct"
                  allow-clear
                >
                  <a-select-option v-for="d in structOptions" :key="d.id" :value="d.id">{{ d.name }}</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="是否主点位" v-bind="validateInfos.primaryTag" id="KpiTagForm-primary_tag" name="primaryTag">
                <j-dict-select-tag
                  type="radio"
                  disabled
                  v-model:value="formData.primaryTag"
                  :options="primaryTagOptions"
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="点位大类" v-bind="validateInfos.tagKind" id="KpiTagForm-tagKind" name="tagKind">
                <j-dict-select-tag
                  :disabled="formData.primaryTag === 1"
                  :showChooseOption="false"
                  placeholder="请选择点位大类"
                  v-model:value="formData.tagKind"
                  :options="tagKindList"
                  @change="changeTagKind"
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="点位" v-bind="validateInfos.tagId" id="KpiTagForm-tagId" name="tagId">
                <a-select
                  v-model:value="formData.tagId"
                  placeholder="请选择点位"
                  show-search
                  :filter-option="false"
                  @search="handleTagSearch"
                  @change="changeTag"
                  allow-clear
                >
                <a-select-option v-for="d in tagOptions" :key="d.tagId" :value="d.tagId">[{{ d.tagId }}] {{ d.tagName }}</a-select-option>
              </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="KPI点别名" v-bind="validateInfos.kpiTagName" id="KpiTagForm-kpiTagName" name="kpiTagName">
                <a-input v-model:value="formData.kpiTagName" placeholder="请输入KPI点别名" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="排序号" v-bind="validateInfos.sort" id="KpiTagForm-sort" name="sort">
                <a-input-number v-model:value="formData.sort" placeholder="请输入排序号" style="width: 100%" />
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
      </template>
    </JFormContainer>
  </a-spin>
</template>

<script lang="ts" setup>
  import { ref, reactive, defineExpose, nextTick, defineProps, computed, onMounted, inject } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';
  import { getValueType } from '/@/utils';
  import { saveOrUpdate } from '../KpiTag.api';
  import { Form } from 'ant-design-vue';
  import JFormContainer from '/@/components/Form/src/container/JFormContainer.vue';
  import { getKpiStrutTree} from '../KpiStruct.api';
  import { TagKindEnum } from '@/enums/EmsEnum';
  import { DictItem, KpiStruct, TagItem, tagKindList } from '../KpiStructSet.data'
  const props = defineProps({
    formDisabled: { type: Boolean, default: false },
    formBpm: { type: Boolean, default: true },
  });
  const formRef = ref();
  const useForm = Form.useForm;
  const emit = defineEmits(['register', 'ok']);
  const formData = reactive<Record<string, any>>({
    id: '',
    energyType: 'Electric',
    factoryCode: '',
    deptCode: '',
    lineCode: '',
    kpiTagName: '',
    structId: null,
    primaryTag: null,
    tagKind: null,
    tagId: '',
    sort: null,
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  //表单验证
  const validatorRules = reactive({
    energyType: [{ required: true, message: '请选择能源类型!' }],
    // factoryCode: [{ required: true, message: '请输入工厂!' }],
    // deptCode: [{ required: true, message: '请输入部门!' }],
    // lineCode: [{ required: true, message: '请输入产线!' }],
    kpiTagName: [{ required: true, message: '请输入KPI点别名!' }],
    structId: [{ required: true, message: '请选择KPI结构!' }],
    tagKind: [{ required: true, message: '请选择点位大类!' }],
    tagId: [{ required: true, message: '请选择点位!' }],
    sort: [{ required: true, message: '请输入排序号!' }],
  });
  const { resetFields, validate, validateInfos } = useForm(formData, validatorRules, { immediate: false });

  // 表单禁用
  const disabled = computed(() => {
    if (props.formBpm === true) {
      return false;
    }
    return props.formDisabled;
  });

  // 注入响应式数据
  const orstagList = inject<TagItem[]>('orstagList') || ref<TagItem[]>([]);
  const collectTagList = inject<TagItem[]>('collectTagList') || ref<TagItem[]>([]);

  //选项
  const structOptions = ref<KpiStruct[]>([]);
  const tagListAll = ref<TagItem[]>([]);
  const tagOptions = ref<TagItem[]>([]);
  const primaryTagOptions = ref<DictItem[]>([
    { value: 1, label: '主点位' },
    { value: 0, label: '其它点位' },
  ]);

  // 初始化
  onMounted(async() => {
    // await listStructData();
    // setTagOption();
  });

  //获取结构列表
  const listStructData = async () => {
    structOptions.value = [];
    confirmLoading.value = true;
    try {
      const params = {energyType: formData.energyType}
      const data = await getKpiStrutTree(params);
      if (data.length === 0) {
        createMessage.warning('此能源类型下还未配置KPI结构');
        return;
      }
      structOptions.value = data
    } catch (e) {
      console.error('获取结构列表失败:', e);
      createMessage.error('获取结构列表失败');
    } finally {
      confirmLoading.value = false;
    }
  };

  function changeEnergyType(){
    formData.structId = null;
    formData.primaryTag = null;
    formData.tagKind = null;
    formData.tagId = '';
    listStructData();
    setTagOption();
  }

  function changeStruct(value){
    formData.primaryTag = null;
    formData.tagKind = null;
    formData.tagId = '';
    const elem = structOptions.value.find((item) => item.id === value);
    if(elem){
      const kpiTagList = elem.kpiTagList || []; 
      if(kpiTagList.length > 0){
        formData.primaryTag = 0;
      }else{
        formData.primaryTag = 1;
        formData.tagKind = TagKindEnum.ORSDATA;
        setTagOption();
      }
    }
  }

  function changeTagKind(){
    formData.tagId = '';
    setTagOption();
  }

  function changeTag(value: string){
    const elem = tagOptions.value.find((item) => item.tagId === value);
    if (elem) {
      formData.kpiTagName = elem.tagName || ''; 
    }
  }

  function handleTagSearch(value: string) {
    if (value) {
      tagOptions.value = tagListAll.value.filter((item) => `[${item.tagId}]${item.tagName}`.toLowerCase().includes(value.toLowerCase()));
    } else {
      tagOptions.value = [...tagListAll.value];
    }
  }

  //设置选项
  function setTagOption(){
    tagListAll.value = []
    if(formData.energyType && formData.tagKind){
      if(TagKindEnum.ORSDATA === formData.tagKind){
        tagListAll.value = [...orstagList.value.filter((item) => item.tagType === formData.energyType)];
      } else if(TagKindEnum.COLLECT === formData.tagKind){
        tagListAll.value = [...collectTagList.value.filter((item) => item.tagType === formData.energyType)];
      }   
    } 
    tagOptions.value = [...tagListAll.value];
  }

  /**
   * 新增
   */
  function add() {
    edit({});
  }

  /**
   * 编辑
   */
  function edit(record) {
    nextTick(() => {
      resetFields();
      const tmpData = {};
      Object.keys(formData).forEach((key) => {
        if (record.hasOwnProperty(key)) {
          tmpData[key] = record[key];
        }
      });
      //赋值
      Object.assign(formData, tmpData);
      listStructData();
      setTagOption();
    });
  }

  /**
   * 提交数据
   */
  async function submitForm() {
    try {
      // 触发表单验证
      await validate();
    } catch (err) {
      const error = err as { errorFields?: Array<{ name: string }> };
      if (error.errorFields) {
        const firstField = error.errorFields[0];
        if (firstField) {
          formRef.value.scrollToField(firstField.name, { behavior: 'smooth', block: 'center' });
        }
      }
      return Promise.reject(error.errorFields);
    }
    confirmLoading.value = true;
    const isUpdate = ref<boolean>(false);
    //时间格式化
    let model = formData;
    if (model.id) {
      isUpdate.value = true;
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
    await saveOrUpdate(model, isUpdate.value)
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

  defineExpose({
    add,
    edit,
    submitForm,
  });
</script>

<style lang="less" scoped>
  .antd-modal-form {
    padding: 14px;
  }
</style>
