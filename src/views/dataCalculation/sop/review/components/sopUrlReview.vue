<template>
    <a-spin :spinning="confirmLoading">
      <JFormContainer :disabled="disabled">
        <template #detail>
          <!-- <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="SopStatisticDataForm">
            <a-row>
                <a-col :span="24">
                    <a-form-item label="租户ID" v-bind="validateInfos.tenantId" id="SopStatisticDataForm-tenantId" name="tenantId">
                        <a-input-number v-model:value="formData.tenantId" placeholder="请输入租户ID" style="width: 100%" />
                    </a-form-item>
                </a-col>
            </a-row>
          </a-form> -->
          
            <a-card :bordered='false'>
                <div v-if="noData" style="text-align: center;">暂无数据</div>
                <section style=" margin: 8px 0">
                    <a-form ref="formRef" class="antd-modal-form" layout="vertical" :labelCol="labelCol" :wrapperCol="wrapperCol" name="SopStatisticDataForm">
                        <a-row>
                            <a-col :span="8" class="coldc" v-for="item in dataSource" :key="item.id" style="border: 1px solid #ebf1fb;">
                                <a-row>
                                    <a-col :span="24" class="itemsLable">
                                        <label :title="item.createTime">{{ item.createTime }}</label>
                                    </a-col>
                                    <a-col :span="24">
                                        <a-form-item label="回顾人">
                                            <KtUserScrollSelect v-model:value="item.review" placeholder="请选择" style="width: 100%;" :disabled="!isShow"></KtUserScrollSelect>
                                        </a-form-item>
                                    </a-col>
                                    <a-col :span="24">
                                        <a-form-item label="最新版本号">
                                            <a-input v-model:value="item.version" placeholder="请输入"  allow-clear style="width:100%" :disabled="!isShow"></a-input>
                                        </a-form-item>
                                    </a-col>
                                    <a-col :span="24">
                                        <a-form-item label="实施时间">
                                            <a-date-picker type="datetime" v-model:value="item.operateTime" placeholder="请选择" valueFormat="YYYY-MM-DD HH:mm:ss" show-time style="width: 100%;" :disabled="!isShow"></a-date-picker>
                                        </a-form-item>
                                    </a-col>
                                    <a-col :span="24">
                                        <a-form-item label="变更内容">
                                            <a-input v-model:value="item.changes" placeholder="请输入"  allow-clear style="width:100%" :disabled="!isShow"></a-input>
                                        </a-form-item>
                                    </a-col>
                                    <a-col :span="24">
                                        <a-form-item label="文件">
                                            <JUpload :uploadFileMax="1" v-model:value="item.fileObj" :returnUrl="false"></JUpload>
                                        </a-form-item>
                                    </a-col>
                                </a-row>
                            </a-col>
                        </a-row>
                    </a-form>
                </section>
            </a-card>
        </template>
      </JFormContainer>
    </a-spin>
  </template>
  
  <script lang="ts" setup>
    import { ref, reactive, defineExpose, nextTick, defineProps, computed, onMounted } from 'vue';
    import { defHttp } from '/@/utils/http/axios';
    import { useMessage } from '/@/hooks/web/useMessage';
    import { getValueType } from '/@/utils';
    import { fileSave,getFil } from '../SopReviewPlan.api';
    import KtUserScrollSelect from '/@/components/KtUserScrollSelect/KtUserScrollSelect.vue';
    import { Form } from 'ant-design-vue';
    import JFormContainer from '/@/components/Form/src/container/JFormContainer.vue';
    import JUpload from '/@/components/Form/src/jeecg/components/JUpload/JUpload.vue';
    import dayjs from 'dayjs';
    const props = defineProps({
      formDisabled: { type: Boolean, default: false },
      formData: { type: Object, default: () => ({})},
      formBpm: { type: Boolean, default: true }
    });
    const dataSource = ref([]);
    const noData = ref(false);
    const formRef = ref();
    const useForm = Form.useForm;
    const emit = defineEmits(['register', 'ok']);

    const week = ref('');
    const id = ref('');
    const sopId = ref('');
    const number = ref(0);
    const isShow = ref(true);
    const formData = reactive<Record<string, any>>({
      id: '',
    });
    const { createMessage } = useMessage();
    const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 6 } });
    const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 24 } });
    const confirmLoading = ref<boolean>(false);
    //表单验证
    // const validatorRules = reactive({
    //   tenantId: [{ required: true, message: '请输入租户ID!'},],
    //   factoryCode: [{ required: true, message: '请输入工厂Code!'},],
    //   deptCode: [{ required: true, message: '请输入部门Code!'},],
    //   lineCode: [{ required: true, message: '请输入产线Code!'},],
    // });
    // const { resetFields, validate, validateInfos } = useForm(formData, validatorRules, { immediate: false });
  
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
  
    function handleChange(a,b){}

    /**
     * 编辑
     */
    function edit(record) {
      nextTick(() => {
        week.value = record.week
        id.value = record.id
        sopId.value = record.sopId
        let addType = record.addType;
        number.value=0;
        if(addType === 'add'){
            isShow.value = true;
            dataSource.value=[{
                url: '',
                sopName: "",
                fileName: '',
                review:'',
                fileObj:[],
                createTime: dayjs().format('YYYY-MM-DD HH:mm:ss'),
            }]
        }else{
            isShow.value = false;
            //获取文件列表
            getFileData();
        }
      });
    }
    
    //获取文件列表
    async function getFileData(){
        await getFil({id:id.value, week:week.value, sopId:sopId.value}).then((res) => {
        if (res && res.length > 0) {
            res[0].fileObj=[{
              filePath: res[0].url,
              fileName: res[0].fileName
            }]

            res.forEach((item) => {
              item.fileObj=[{
                filePath: item.url,
                fileName: item.fileName
              }]
            })

            dataSource.value=res;
            noData.value = false;
        }else{
          noData.value = true
        }
        }).finally(() => {
            confirmLoading.value = false;
        });
    }




    /**
     * 提交数据
     */
    async function submitForm() {
   
      if(!dataSource.value[0].review){
        createMessage.warning("请选择回顾人");
        return false;
      }
      if(!dataSource.value[0].version){
        createMessage.warning("请填写最新版本号");
        return false;
      }
      if(!dataSource.value[0].operateTime){
        createMessage.warning("请选择实施时间");
        return false;
      }
      if(!dataSource.value[0].changes){
        createMessage.warning("请填写变更内容");
        return false;
      }
      let fileJson = dataSource.value[0].fileObj;
      if(fileJson && fileJson.length > 0){
        let fileArray = JSON.parse(fileJson)
        let fileObj = fileArray[0]
        dataSource.value[0].url = fileObj.filePath
        let fileName = fileObj.fileName;
        if(fileName){
          dataSource.value[0].fileName = fileName.split('.').shift()
        }
      }
      // console.log('fileJson :>> ', fileJson);
      // if(!fileJson || fileJson.length === 0){
      //   createMessage.warning("请上传文件");
      //   return false;
      // }
      // if(!fileObj.filePath){
      //   createMessage.warning("请上传文件");
      //   return false;
      // }
      
      let obj = {
        id: id.value,
        week: week.value,
        sopDescList: dataSource.value
      }
      await fileSave(obj)
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
      edit,
      submitForm,
    });
  </script>
  
  <style lang="less" scoped>
    .antd-modal-form {
      padding: 14px;
    }

    .itemsLable {
        padding: 5px;
        overflow: hidden;
        white-space: nowrap;
        text-overflow: ellipsis;
        display: block;
        text-align: center;
        background: #ebf1fb;
        line-height: 35px;
    }
    :deep(.ant-card .ant-card-body){
        padding: 0px 24px;
        border-radius: 0 0 4px 4px;
    }
  </style>
  