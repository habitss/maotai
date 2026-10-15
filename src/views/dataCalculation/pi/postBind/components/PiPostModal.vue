<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose :title="title" :width="800" @ok="handleSubmit" :maskClosable="false">
    <BasicForm @register="registerForm" name="PiPostForm">
      <template #fieldListSlot="{ model }">
        <div>
          <a-transfer
            v-model:target-keys="model.relatedField"
            :data-source="fieldData"
            :titles="['未分配', '已分配']"
            show-search
            :render="item => item.name"
            :rowKey="item => item.fieldName"
            :list-style="{
              width: '100%',
              height: '50vh',
            }"
          />
        </div>
      </template>
    </BasicForm>
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref, computed, unref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicForm, useForm } from '/@/components/Form/index';
import { formSchema } from '../PiPost.data';
import { saveOrUpdate } from '../PiPost.api';
import { listAll as piFieldListAll } from '/@/views/dataCalculation/pi/customizeTable/PiCustomizeTableField.api';
import { list as getPostList } from '/@/views/dataCalculation/mdm/postManage/MdmPost.api';
import { cloneDeep } from 'lodash-es';
// Emits声明
const emit = defineEmits(['register', 'success']);
const isUpdate = ref(true);
const isDetail = ref(false);
//字段信息
const fieldData = ref([] as any);
//表单配置
const [registerForm, { setProps, resetFields, setFieldsValue, validate, scrollToField, updateSchema }] = useForm({
  labelWidth: 100,
  schemas: formSchema,
  showActionButtonGroup: false,
  baseColProps: { span: 24 }
});
//表单赋值
const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
  //重置表单
  await resetFields();
  setModalProps({ confirmLoading: false, showCancelBtn: !!data?.showFooter, showOkBtn: !!data?.showFooter });
  isUpdate.value = !!data?.isUpdate;
  isDetail.value = !!data?.showFooter;
  //获取对应的pi字段
  fieldData.value = await piFieldListAll({tableName: data.record.relatedTable});
  //获取唯一标识字段并禁用
  let uniqueArr = [] as any;
  fieldData.value.forEach(item => {
    if(item.isUnique == 1){
      //设置禁用
      item.disabled = item.isUnique;
      //获取唯一标识字段
      uniqueArr.push(item.fieldName);
    }
  })
  //克隆表单数据
  let formData = cloneDeep(data.record);
  //将选择字段拆分为数组用于控件显示
  if(isUpdate.value){
    //岗位ID转换成数字类型
    formData.postId = parseInt(formData.postId);
    //合并唯一标识字段和选择的字段
    uniqueArr = [...new Set([...uniqueArr, ...formData.relatedField.split(',')])];
  }
  formData.relatedField = uniqueArr;
  //表单赋值
  await setFieldsValue({
    ...formData,
  });
  //获取岗位数据
  let postRes = await getPostList({lineCode: data.record.lineCode, pageSize: 100});
  //排除已经绑定过的岗位
  let filteredPosts = postRes?.records?.filter(post => 
    !data.bindPost.some(bp => bp.postId == post.id) || post.id == data.record.postId
  );

  updateSchema([
    {
      field: 'postId',
      componentProps: {
        options: filteredPosts,
        fieldNames: {
          label: 'postName',
          value: 'id',
          optionFilterProp: 'postName',
          numberToString: true,
        },
      },
    }
  ]);
  // 隐藏底部时禁用整个表单
  setProps({ disabled: !data?.showFooter })
});
//设置标题
const title = computed(() => (!unref(isUpdate) ? '新增' : !unref(isDetail) ? '详情' : '编辑'));
//表单提交事件
async function handleSubmit() {
  try {
    let values = await validate();
    setModalProps({ confirmLoading: true });
    //提交表单
    await saveOrUpdate(values, isUpdate.value);
    //关闭弹窗
    closeModal();
    //刷新列表
    emit('success');
  } catch ({ errorFields }) {
    if (errorFields) {
      const firstField = errorFields[0];
      if (firstField) {
        scrollToField(firstField.name, { behavior: 'smooth', block: 'center' });
      }
    }
    return Promise.reject(errorFields);
  } finally {
    setModalProps({ confirmLoading: false });
  }
}
</script>

<style lang="less" scoped>
/** 时间和数字输入框样式 */
:deep(.ant-input-number) {
  width: 100%;
}

:deep(.ant-calendar-picker) {
  width: 100%;
}
</style>