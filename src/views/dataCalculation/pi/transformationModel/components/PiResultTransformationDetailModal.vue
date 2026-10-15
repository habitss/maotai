<template>
  <BasicModal @register="registerModal" v-bind="$attrs" 
  destroyOnClose :title="title" :width="width" @ok="handleSubmit"
  okText="保存"
  :loading = "modalLoading"
  :confirmLoading>
    <BasicForm @register="registerForm" @submit="handleSubmit">
      <!--  条件插槽    -->
      <template #conditionSlot="{ model, field}">
      <a-form-item-rest>
        <!--时间 -->
        <a-row justify="space-between" v-if="model['resultType_'+field] == '0'">
          <a-col :span="4">
            <a-time-picker v-model:value="model['leftValue_'+field]"
             placeholder="请选择" format="HH:mm" valueFormat="HH:mm" allowClear style="width: 120px;"></a-time-picker>
          </a-col>  
          <a-col :span="4" style="text-align: center;line-height: 32px;">
            ≤ 数据 ≤
          </a-col>
          <a-col :span="4">
            <a-time-picker v-model:value="model['rightValue_'+field]"
             placeholder="请选择" format="HH:mm" valueFormat="HH:mm" allowClear style="width: 120px;"></a-time-picker>
          </a-col>
          <a-col :span="4">
            <a-select v-model:value="model['outputType_'+field]" placeholder="请选择输出方式" 
              allowClear
              :options="outputMethodArr"
              style="width: 120px;"
              @change="handleChange(model, field)"
            />
          </a-col>
          <a-col :span="4">
            <a-select v-model:value="model['outputValue_'+field]" placeholder="请选择" 
              allowClear
              :options="functionArr"
              style="width: 100px;"
              v-if="model['outputType_'+field] == '1'"
            />
            <a-input v-model:value="model['outputValue_'+field]" placeholder="请输入值" allowClear style="width: 100px;" v-if="model['outputType_'+field] == '0'"/>
          </a-col>
          <a-col :span="2">
            <a-button @click="removeCondition(field)" type="link" danger style="padding: 0">删除</a-button>
          </a-col>
        </a-row>
        <!--字符-->
        <a-row justify="space-between" v-if="model['resultType_'+field] == '1' ">
          <a-col :span="2" style="line-height: 32px;">数据&nbsp;&nbsp; =</a-col>
          <a-col :span="4">
            <a-input v-model:value="model['rightValue_'+field]" placeholder="请输入值" allowClear style="width: 120px;"/>
          </a-col>

          <a-col :span="4">
            <a-select v-model:value="model['outputType_'+field]" placeholder="请选择输出方式" 
              allowClear
              :options="outputMethodArr"
              style="width: 120px;"
              @change="handleChange(model, field)"
            />
          </a-col>
          <a-col :span="4">
            <a-select v-model:value="model['outputValue_'+field]" placeholder="请选择" 
              allowClear
              :options="functionArr"
              style="width: 100px;"
              v-if="model['outputType_'+field] == '1'"
            />
            <a-input v-model:value="model['outputValue_'+field]" placeholder="请输入值" allowClear style="width: 100px;" v-if="model['outputType_'+field] == '0'"/>
          </a-col>

          <a-col :span="8">
            <a-button @click="removeCondition(field)" type="link" danger style="padding: 0">删除</a-button>
          </a-col>
        </a-row>
        <!--数字-->
        <a-row justify="space-between"  v-if="model['resultType_'+field] == '2' ">
          <a-col :span="3">
            <a-input v-model:value="model['leftValue_'+field]" placeholder="请输入值" allowClear style="width: 90px;"/>
          </a-col>
          <a-col :span="2">
            <a-select v-model:value="model['leftCondition_'+field]" placeholder="请选择条件" 
              allowClear
              :options="symbolOption"
              style="width: 70px;"
            />
          </a-col>
          <a-col :span="2" style="text-align: center;line-height: 32px;">
            数据
          </a-col>
           <a-col :span="3">
            <a-select v-model:value="model['rightCondition_'+field]" placeholder="请选择条件" 
              allowClear
              :options="symbolOption"
              style="width: 90px;"
            />
          </a-col>
          <a-col :span="3">
            <a-input v-model:value="model['rightValue_'+field]" placeholder="请输入值" allowClear style="width: 90px;"/>
          </a-col>

          <a-col :span="4">
            <a-select v-model:value="model['outputType_'+field]" placeholder="请选择输出方式" 
              allowClear
              :options="outputMethodArr"
              style="width: 110px;"
              @change="handleChange(model, field)"
            />
          </a-col>
          <a-col :span="3">
            <a-select v-model:value="model['outputValue_'+field]" placeholder="请选择" 
              allowClear
              :options="functionArr"
              style="width: 90px;"
              v-if="model['outputType_'+field] == '1'"
            />
            <a-input v-model:value="model['outputValue_'+field]" placeholder="请输入值" allowClear style="width: 90px;" v-if="model['outputType_'+field] == '0'"/>
          </a-col>

          <a-col :span="2">
            <a-button @click="removeCondition(field)" type="link" danger style="padding: 0">删除</a-button>
          </a-col>
        </a-row>
      </a-form-item-rest>
      </template>

      <!-- 底部插槽  v-if="!isDetail"-->
      <template #formFooter>
        <div style="margin: 0 auto">
          <a-button type="link" @click="addCondition()">添加规则</a-button>
        </div>
      </template>
    </BasicForm>
  </BasicModal>
</template>

<script lang="ts" setup>
  import {ref, computed, unref} from 'vue';
  import {BasicModal, useModalInner} from '/@/components/Modal';
  import {BasicForm, useForm} from '/@/components/Form/index';
  import {detailFormSchema} from '../PiResultTransformationModel.data';
  import {detailSaveOrUpdate,getConditionData} from '../PiResultTransformationModel.api';
  import dayjs from 'dayjs';
  import { useMessage } from '/@/hooks/web/useMessage';
  const width = ref<number>(950);
  //注册Message
  const { createMessage: msg } = useMessage();
  // Emits声明
  const emit = defineEmits(['register','success']);
  const isUpdate = ref(true);
  const isDetail = ref(false);
  const resultType = ref(0);
  
  //输出方式
  const outputMethodArr = ref([
    {value: '0', label: '自定义输出'},
    {value: '1', label: '函数输出'}
  ])
  //函数列表
  const functionArr = ref([
    {value: 'currentDateFun', label: '当前日期'},
    {value: 'yesterdayDateFun', label: '昨天日期'}
  ])

  //条件符号下拉选项数据
  const symbolOption = ref([
    { value: '1', label: '>' },
    { value: '2', label: '≥' },
    { value: '3', label: '=' },
    { value: '5', label: '<' },
    { value: '6', label: '≤' },
  ]);
  //条件下拉选项数据
  const conditionArray = ref([]);
  //条件总数
  const conditionTotal = ref(0);
  //初始加载loading
  const modalLoading = ref(true);
  //保存loading
  const confirmLoading = ref(false);
  //PI基础数据
  const configData = ref(false);

  //表单配置
  const [registerForm, { setProps,resetFields, setFieldsValue, validate, scrollToField,appendSchemaByField,removeSchemaByFiled }] = useForm({
      labelWidth: 150,
      schemas: detailFormSchema,
      showActionButtonGroup: false,
      baseColProps: {span: 24}
  });
  //表单赋值
  const [registerModal, {setModalProps, closeModal}] = useModalInner(async (data) => {
      modalLoading.value = true;
      //重置表单
      await resetFields();
      setModalProps({confirmLoading: false,showCancelBtn:!!data?.showFooter,showOkBtn:!!data?.showFooter});
      isUpdate.value = !!data?.isUpdate;
      isDetail.value = !!data?.showFooter;
      if (unref(isUpdate)) {
          //表单赋值
          await setFieldsValue({
              ...data.record,
          });
      }
      resultType.value = data.record.resultType;
      // 隐藏底部时禁用整个表单
      setProps({ disabled: !data?.showFooter })
      //isDetail.value = true;
      modalLoading.value = false;

      //获取取数规则
      let datas = await getConditionData({modelId: data.record.id});
      for(const item of datas){
        await addCondition(item);
      }
  });
  //设置标题
  const title = "取数规则";
  //表单提交事件
  async function handleSubmit(v) {
    const values = await validate();
    try {
      //msg.loading({ content: '正在保存...', duration: 0, key: 'saving' });
      confirmLoading.value = true;
      const conditionJson = [] as any;
       //处理数据
      for (let key in values) {
        //判断条件表单项前缀，处理数据
        if (key.startsWith('c_field_') && values[key]) {
          conditionJson.push({field: values[key], leftValue: values[`leftValue_${key}`], 
            leftCondition: values[`leftCondition_${key}`],rightCondition: values[`rightCondition_${key}`]
            ,rightValue: values[`rightValue_${key}`],outputType: values[`outputType_${key}`],outputValue: values[`outputValue_${key}`]});
        }
      }
      values.conditionJson = JSON.stringify(conditionJson);

      
      //setModalProps({confirmLoading: true});
      //提交表单
      await detailSaveOrUpdate(values, isUpdate.value);
      msg.success({ content: '保存成功', key: 'saving' });
      //关闭弹窗
      closeModal();
      //重置表单
      resetFields();
      //刷新列表
      emit('success');
    } catch (error) {
      msg.error({ content: '保存失败', key: 'saving' });
    } finally {
      confirmLoading.value = false;
    }
  }
  /**
 * 删除条件
 */
  async function removeCondition(field){
    conditionTotal.value--;
    removeSchemaByFiled([field, 'value_'+field]);
    // processingConditionArray();
  }
  /**
   * 下拉选项筛选
   * @param input 输入的值 
   * @param option 选项
   * @param searchField 需要查找的字段 
   */
  // function handleFilterOption(input, option, searchField) {
  //   return option[searchField].toLowerCase().indexOf(input.toLowerCase()) >= 0;
  // }


  /**
   * 添加条件
   */
  async function addCondition(item){
    conditionTotal.value++;
    //生成唯一值
    const id = 'c_field_'+dayjs().valueOf().toString(36)+conditionTotal.value;
    await appendSchemaByField(
      {
        field: id,
        component: 'Select',
        label: '取数规则',
        colProps: { span: 24 },
        slot: 'conditionSlot',
        // dynamicRules: function(obj){
        //   return [
        //     {
        //       //默认开启表单检验
        //       required: false,
        //       validator: (_, value) => {
        //         //需要return 一个Promise对象
        //         return new Promise((resolve, reject) => {
        //           const model = obj.model;
        //           const field = obj.field;
        //           if(value || model['symbol_'+field] || model['value_'+field]){
        //             if (!value) {
        //               reject('请选择字段');
        //             }
        //             if (!model['symbol_'+field]) {
        //               reject('请选择条件');
        //             }
        //             if (!model['value_'+field]) {
        //               reject('请输入值');
        //             }
        //           }
        //           resolve();
        //         });
        //       },
        //     },
        //   ];
        // },
      },
      ''
    );
    await appendSchemaByField(
      {
        field: `resultType_${id}`,
        component: 'Input',
        label: '结果输入类型',
        show: false,
      },
      ''
    );
    await appendSchemaByField(
      {
        field: `leftValue_${id}`,
        component: 'Input',
        label: '左偏差值',
        show: false,
      },
      ''
    );
    await appendSchemaByField(
      {
        field: `leftCondition_${id}`,
        component: 'Input',
        label: '左偏差条件',
        show: false,
      },
      ''
    );
    await appendSchemaByField(
      {
        field: `rightCondition_${id}`,
        component: 'Input',
        label: '右偏差条件',
        show: false,
      },
      ''
    );
    await appendSchemaByField(
      {
        field: `rightValue_${id}`,
        component: 'Input',
        label: '右偏差值',
        show: false,
      },
      ''
    );

    await appendSchemaByField(
      {
        field: `outputType_${id}`,
        component: 'Input',
        label: '输出方式',
        show: false,
      },
      ''
    );

    await appendSchemaByField(
      {
        field: `outputValue_${id}`,
        component: 'Input',
        label: '输出值',
        show: false,
      },
      ''
    );

    await appendSchemaByField(
      {
        field: `index_${id}`,
        component: 'Input',
        label: '序号',
        show: false,
      },
      ''
    );
    if(item){
      //分开赋值，避免编辑时校验异常
      await setFieldsValue({['leftValue_'+id]: item.leftValue});
      await setFieldsValue({['leftCondition_'+id]: item.leftCondition});
      await setFieldsValue({['rightCondition_'+id]: item.rightCondition});
      await setFieldsValue({['rightValue_'+id]: item.rightValue});
      await setFieldsValue({['outputType_'+id]: `${item.outputType}`});
      await setFieldsValue({['outputValue_'+id]: item.outputValue});
      await setFieldsValue({['index_'+id]: conditionTotal.value});
    }else{
      await setFieldsValue({['index_'+id]: conditionTotal.value});
    }
    await setFieldsValue({['resultType_'+id]: resultType.value});
    await setFieldsValue({[id]: conditionTotal.value});
  }

  //
  function handleChange(model, field){
    model['outputValue_'+field] = ''
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