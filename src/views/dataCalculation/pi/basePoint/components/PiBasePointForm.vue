<template>
    <a-spin :spinning="confirmLoading">
    <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol">
      <a-row>
        <!-- 部门 -->
        <a-col :span="24">
          <a-form-item label="部门" v-bind="validateInfos.queryDept">
            <kt-depart-select
              v-model:value="formData.deptCode"
              select-type="depart"
              placeholder="请选择部门"
              :disabled="disabled"
              @change="handleDeptChange"
            />
          </a-form-item>
        </a-col>
        <!-- 产线 -->
        <a-col :span="24">
          <a-form-item label="产线" v-bind="validateInfos.lineCode">
            <kt-depart-select
              v-model:value="formData.lineCode"
              select-type="line"
              @currentData="getDepartObject"
              placeholder="请选择产线"
              :parentKey="formData.deptCode"
              :disabled="disabled"
            />
          </a-form-item>
        </a-col>

        <!--设备名称-->
         <a-col :span="24">
          <a-form-item label="设备" v-bind="validateInfos.deviceId">
            <kt-device
                v-model:value="formData.deviceId"
                :disabled="disabled"
                :deptCode="formData.deptCode"
                dict=""
                placeholder="请选择设备"
                type="device"
            />
          </a-form-item>
        </a-col>

        <!-- 点位编码 -->
         <a-col :span="24">
          <a-form-item label="点位编码" v-bind="validateInfos.pointCode">
            <a-input v-model:value="formData.pointCode" placeholder="请输入点位编码" :disabled="disabled" />
          </a-form-item>
        </a-col>

        <!-- 点位类型 -->
        <a-col :span="24">
          <a-form-item label="点位类型" v-bind="validateInfos.pointType">
            <kt-dict
              v-model:value="formData.pointType"
              placeholder="请选择点位类型"
              dict="MES3_PI_POINT_TYPE"
              :disabled="disabled"
            />
          </a-form-item>
        </a-col>

        <!-- 点位中文描述 -->
        <a-col :span="24">
          <a-form-item label="点位中文描述" v-bind="validateInfos.pointName">
            <a-input v-model:value="formData.pointName" placeholder="请输入点位中文描述" :disabled="disabled" />
          </a-form-item>
        </a-col>
        <!-- IP地址 -->
        <a-col :span="24">
          <a-form-item label="IP地址" v-bind="validateInfos.ip">
            <a-input v-model:value="formData.ip" placeholder="请输入IP地址" :disabled="disabled" />
          </a-form-item>
        </a-col>
        <!-- DB地址 -->
        <a-col :span="24">
          <a-form-item label="DB地址" v-bind="validateInfos.dbAddress">
            <a-input v-model:value="formData.dbAddress" placeholder="请输入DB地址" :disabled="disabled" />
          </a-form-item>
        </a-col>
        <!-- 数据类型 -->
        <a-col :span="24">
          <a-form-item label="数据类型" v-bind="validateInfos.dataType">
            <a-input v-model:value="formData.dataType" placeholder="请输入数据类型" :disabled="disabled" />
          </a-form-item>
        </a-col>

        <!-- 汉化方式 -->
        <a-col :span="24">
          <a-form-item label="汉化方式" v-bind="validateInfos.cnWay">
            <kt-dict
              v-model:value="formData.cnWay"
              placeholder="请选择汉化方式"
              dict="MES3_PI_SINICIZED"
              :disabled="disabled"
            />
          </a-form-item>
        </a-col>
        <!-- 字典汉化CODE -->
        
        <a-col :span="24" v-if="formData.cnWay == '2'">
          <a-form-item label="字典汉化编码" v-bind="validateInfos.dictCode">
            <a-select show-search option-filter-prop="label" v-model:value="formData.dictCode" :options="dictOptions" placeholder="请选择"></a-select>
          </a-form-item>
        </a-col>
        
        <!-- 点位功能位置编码 -->
        <a-col :span="24">
          <a-form-item label="点位功能位置编码" v-bind="validateInfos.tagFunlocCode">
            <a-input v-model:value="formData.tagFunlocCode" placeholder="请输入点位功能位置编码" :disabled="disabled" />
          </a-form-item>
        </a-col>
        <!-- IOT属性编码 -->
        <a-col :span="24">
          <a-form-item label="IOT属性编码" v-bind="validateInfos.iotAttrName">
            <a-input v-model:value="formData.iotAttrName" placeholder="请输入IOT属性编码" :disabled="disabled" />
          </a-form-item>
        </a-col>

        <a-col :span="24">
          <a-form-item label="数据属性" v-bind="validateInfos.dataAttribute">
             <kt-dict
              v-model:value="formData.dataAttribute"
              placeholder="请选择数据属性"
              dict="MES3_PI_DATAATTRIBUTE"
              :disabled="disabled"
            />
          </a-form-item>
        </a-col>

        <a-col :span="24" v-if="formData.dataAttribute == '0'">
          <a-form-item label="绑定标准值" v-bind="validateInfos.bindStandard">
            <JDictSelectTag v-model:value="formData.bindStandard" placeholder="请选择绑定标准值"
                :showChooseOption=false allow-clear mode="multiple"
                dictCode="mes3_pi_base_point where del_flag = 0 and data_attribute = 1, point_name, id"
                />
          </a-form-item>
        </a-col>

        <!-- 单位 -->
        <a-col :span="24">
          <a-form-item label="单位" v-bind="validateInfos.unit">
            <a-input v-model:value="formData.unit" placeholder="请输入单位" :disabled="disabled" />
          </a-form-item>
        </a-col>
        <!-- 排序 -->
        <a-col :span="24">
          <a-form-item :label="t('language.sort')" v-bind="validateInfos.sort">
            <a-input-number v-model:value="formData.sort" :placeholder="t('language.enterSort')" style="width: 100%" :disabled="disabled" />
          </a-form-item>
        </a-col>
        <!-- 备注 -->
        <!-- <a-col :span="24">
          <a-form-item :label="t('language.remark')" v-bind="validateInfos.remark">
            <a-textarea v-model:value="formData.remark" rows="3" :placeholder="t('language.enterRemark')" :disabled="disabled" />
          </a-form-item>
        </a-col> -->
      </a-row>
    </a-form>
  </a-spin>


</template>

<script lang="ts" setup>
    import { ref, reactive, defineExpose, nextTick, defineProps, computed, onMounted, watch } from 'vue';
    import { defHttp } from '/@/utils/http/axios';
    import { useMessage } from '/@/hooks/web/useMessage';
    import JSelectDept from '/@/components/Form/src/jeecg/components/JSelectDept.vue';
    import { getValueType } from '/@/utils';
    import { Form } from 'ant-design-vue';
    import KtDict from '/@/components/KtDict/KtDict.vue';
    import KtDevice from '/@/components/KtDeviceType/KtDeviceType.vue';
    import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
    import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';
    import { useI18n } from '/@/hooks/web/useI18n';
    const { t } = useI18n();
    import {saveOrUpdate} from '../PiBasePoint.api';
    import {getDictionary} from '../../customizeTable/PiAlgorithm.api';
    import { useUserStore } from '/@/store/modules/user';
    // 用户信息
    const userStore = useUserStore();

    //计算模型-汉化代码
    const dictOptions = reactive<Record<string, any>>([]);
    
    const props = defineProps({
        formDisabled: { type: Boolean, default: false },
        formData: { type: Object, default: () => {} },
        formBpm: { type: Boolean, default: true },
        isAdd: { type: Boolean, default: true },
    });

    const formRef = ref();
    const useForm = Form.useForm;
    const emit = defineEmits(['register', 'ok']);
    const formData = reactive<Record<string, any>>({
        id: '',
        deptCode: '',
        lineCode: '',
        deviceId: '',
        pointCode:'',
        pointType:'',
        pointName:'',
        ip:'',
        dbAddress:'',
        dataType:'',
        cnWay:'3',
        dictCode:'',
        tagFunlocCode:'',
        iotAttrName:'',
        dataAttribute: '',
        bindStandard:'',
        sort:'',
    });
    const { createMessage } = useMessage();
    const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
    const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
    const confirmLoading = ref<boolean>(false);
    //表单验证
    const validatorRules = {
        // factoryCode: [{ required: true, message: `${t('language.enterFactory')}!` }],
        queryDept: [{ required: true, message: `部门不为空!` }],
        lineCode: [{ required: true, message: `产线不为空!` }],
        deviceId: [{ required: true, message: `设备不为空!` }],
        pointCode: [{ required: true, message: `点位编码不为空!` }],
        pointType: [{ required: true, message: `点位类型不为空!` }],
        pointName: [{ required: true, message: `点位中文描述不为空!` }],
        cnWay: [{ required: true, message: `汉化方式不为空!` }]
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
    watch(
        () => formData.orgTypeCode,
        (factoryCode: string, oldVal: string) => {
            // formData.deviceTypeId = undefined;
        }
    );

  onMounted(() => {
    // getDeviceTypeArr();
    //获取汉化代码
    getDictionaryData();
  });

    /**
     * 新增
     */
    function add() {
      cleanData(formData);
      formData.value = {
        id: '',
        deptCode: '',
        lineCode: ''
      }
      edit({});
    }
    function cleanData(valObj){
      // 清空formData的值
      for (const key in valObj) {
          if (valObj.hasOwnProperty(key)) {
              valObj[key] = '';
          }
      }
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
        if(record.bindStandard){
          formData.bindStandard = record.bindStandard.split(",");
        }
      });
    }

    /**
     * 提交数据
     */
    async function submitForm() {
        // 触发表单验证
        await validate();
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
        model.factoryCode = userStore.factoryCode;
        await saveOrUpdate(model, isUpdate.value)
            .then((res) => {
            if (res.success) {
                const successMessage = res.message ? res.message : t('language.save');
                createMessage.success(successMessage);
                emit('ok');
            } else {
                createMessage.warning(res.message);
            }
            })
            .finally(() => {
            confirmLoading.value = false;
        });
    }

    // /**
    //  * 初始化设备类型下拉数据
    //  */
    // async function getDeviceTypeArr() {
    //     deviceTypeList({ pageNo: 1, pageSize: 999 }).then((res) => {
    //         if (res && res.records && res.records.length > 0) {
    //         res.records.map((item) => {
    //             return { text: item.itemKey, value: item.itemVal };
    //         });
    //         }
    //     });
    // }

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

    //计算模型-汉化模型
    async function getDictionaryData(){
      let res = await getDictionary({});
      let newArr = res.map(item => ({
          label:item.label,
          value:item.value
      }));

      dictOptions.push(...newArr);
    }

    //部门改变事件
    async function handleDeptChange(value){
      //清空产线
      formData.lineCode = "";
      //清空设备
      formData.deviceId = "";
    }
    defineExpose({
        add,
        edit,
        submitForm,
    });

</script>

<style lang="less" scoped>
// @import '../../common.less';
</style>