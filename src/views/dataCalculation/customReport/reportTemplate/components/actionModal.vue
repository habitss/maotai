<template>
  <BasicModal 
    @register="registerModal" 
    v-bind="$attrs" 
    :title="`${title}`"
    :cancelText="t('language.closeText')"
    @ok="handleSubmit">
    <BasicForm @register="registerForm" @submit="handleSubmit" />
  </BasicModal>
</template>
<script lang="ts" setup>
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useForm, BasicForm } from '/@/components/Form';
  import { addData, updateData } from '../index.api';
  import { addFormSchemas } from '../index.data';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { ref } from 'vue';
  import { cloneDeep } from 'lodash-es';
    import { useI18n } from '/@/hooks/web/useI18n';
    const { t } = useI18n();
  
  // 声明Emits
  const emit = defineEmits(['success']);

  //标题
  const title = ref();
  
  //操作标识
  const isAdd = ref();
  
  //需修改记录的id
  const recordId = ref();
  
  //注册modal
  const [registerModal, { closeModal }] = useModalInner(async (data: any) => {
    //重置表单数据
    resetFields();
    //更新schemas配置
    await updateSchema([
      {
        //字段必填
        label: '绑定表',
        field: 'tableId',
        componentProps: {
          onChange: function(value, obj){
            setFieldsValue({tableName: obj.tableName})
          }
        },
      },
    ]);
    //赋值操作标识
    isAdd.value = data.isAdd;
    //判断执行新增还是编辑操作
    if(data.isAdd){
      title.value = '新增';
    }else{
      title.value = '编辑';
      let newData = cloneDeep(data.record);
      //赋值需修改记录的id
      recordId.value = newData.id;

      //设置表单值
      setFieldsValue(newData);
    }
  });

  //注册Message
  const { createMessage: msg } = useMessage();

  /**
   * BasicForm绑定注册;
   */
  const [registerForm, { resetFields, validate, setFieldsValue, updateSchema }] = useForm({
    //注册表单列
    schemas: addFormSchemas,
    //回车提交
    autoSubmitOnEnter: true,
    showActionButtonGroup: false,
    //表格label宽度
    labelCol: { style: { width: '100px' } },
  });

  //表单提交事件
  async function handleSubmit() {
    try {
      const values = await validate();
      //提交表单
      if(isAdd.value){
        //调用后台新增接口
       await addData(values);
      }else{
        values.id = recordId.value;
        //调用后台修改接口
        await updateData(values);  
      }
      //关闭弹窗
      closeModal();
      //重置表单
      resetFields();
      //刷新外部表格
      emit('success');
    } catch (error) {
      msg.error({ content: '保存失败', key: 'saving' });
    }
  }
</script>
