<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="AlarmTypesForm">
          <a-row>
						<a-col :span="24">
							<a-form-item label="父类型" v-bind="validateInfos.parentId" id="AlarmTypesForm-parentId" name="parentId">
                <a-select v-model:value="formData.parentId" allowClear disabled
                   placeholder="请选择" style="width:100%;">
                  <a-select-option v-for="d in alarmCategoryList" :key="d.id" :value="d.id">
                    {{ d.typeName }}
                  </a-select-option>
                </a-select>
							</a-form-item>
						</a-col>
            <a-col :span="24">
							<a-form-item label="类型名称" v-bind="validateInfos.typeName" id="AlarmTypesForm-typeName" name="typeName">
								<a-input v-model:value="formData.typeName" placeholder="请输入类型名称"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
          </a-row>
        </a-form>
      </template>
    </JFormContainer>
  </a-spin>
</template>

<script lang="ts" setup>
  import { ref, reactive, defineExpose, nextTick, unref, defineProps, computed, onMounted } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getValueType } from '/@/utils';
  import {listOptions, loadTreeData, saveOrUpdateDict} from '../AlarmTypes.api';
  import { Form } from 'ant-design-vue';
  import JFormContainer from '/@/components/Form/src/container/JFormContainer.vue';
  const useForm = Form.useForm;
  const formRef = ref();
  const isUpdate = ref(false);
  const expandedRowKeys = ref([]);
  const treeData = ref([]);
  const alarmCategoryList = ref([]);
  const pidField = ref<string>('pid');
  const emit = defineEmits(['register', 'ok']);
  let model: Nullable<Recordable> = null;
  const formData = reactive<Record<string, any>>({
    id: '',
    typeName: '',   
    parentId: undefined,
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  //表单验证
  const validatorRules = reactive({
    typeName: [{ required: true, message: '请输入类型名称!'},],
  });
  const { resetFields, validate, validateInfos } = useForm(formData, validatorRules, { immediate: false });
  const props = defineProps({
    formDisabled: { type: Boolean, default: false },
    formData: { type: Object, default: () => ({}) },
    formBpm: { type: Boolean, default: true }
  });

  // 表单禁用
  const disabled = computed(()=>{
    if(props.formBpm === true){
      if(props.formData.disabled === false){
        return false;
      }else{
        return true;
      }
    }
    return props.formDisabled;
  });
  
  /**
   * 新增
   */
  function add(obj = {}) {
    edit(obj);
  }

  /**
   * 编辑
   */
  function edit(record) {
    console.log('record', record)
    nextTick(async () => {
      resetFields();
      expandedRowKeys.value = [];
      treeData.value = await loadTreeData({ async: false, pid: 0 });
      alarmCategoryList.value = await listOptions({ parentId: 0});
      const tmpData = {};
      Object.keys(formData).forEach((key) => {
        if(record.hasOwnProperty(key)){
          tmpData[key] = record[key]
        }
      })
      //赋值
      Object.assign(formData,tmpData);
      model = tmpData
    });
  }

  /**
   * 根据pid获取展开的节点
   * @param pid
   * @param arr
   */
  function getExpandKeysByPid(pid, arr) {
    if (pid && arr && arr.length > 0) {
      for (let i = 0; i < arr.length; i++) {
        if (arr[i].key == pid && unref(expandedRowKeys).indexOf(pid) < 0) {
          expandedRowKeys.value.push(arr[i].key);
          getExpandKeysByPid(arr[i]['parentId'], unref(treeData));
        } else {
          getExpandKeysByPid(pid, arr[i].children);
        }
      }
    }
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
            formRef.value.scrollToField(firstField.name, {
              behavior: 'smooth',
              block: 'center',
            });
          }
        }
        return Promise.reject(error.errorFields);
    }
    confirmLoading.value = true;
    isUpdate.value = false;
    if (formData.id) {
      isUpdate.value = true;
    }
    //循环数据
    for (let data in formData) {
      //如果该数据是数组并且是字符串类型
      if (formData[data] instanceof Array) {
        let valueType = getValueType(formRef.value.getProps, data);
        //如果是字符串类型的需要变成以逗号分割的字符串
        if (valueType === 'string') {
          formData[data] = formData[data].join(',');
        }
      }
    }
    await saveOrUpdateDict(formData, isUpdate.value)
      .then(async (res) => {
        if (res.success) {
          await getExpandKeysByPid(formData['parentId'], unref(treeData));
          delete formData['children'];
          emit('ok', {
            isUpdate: unref(isUpdate),
            values: { ...formData },
            expandedArr: unref(expandedRowKeys).reverse(),
            // 是否更改了父级节点
            changeParent: model != null && model['parentId'] != formData['parentId'],
          });
          createMessage.success(res.message);
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
