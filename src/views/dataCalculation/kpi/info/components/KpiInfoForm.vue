<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="KpiInfoForm">
          <a-row>
            <a-col :span="12">
							<a-form-item label="所属年份" v-bind="validateInfos.year" id="KpiInfoForm-year" name="year">
								<!-- <a-date-picker v-model:value="formData.year" picker="year" placeholder="请选择年份" valueFormat="YYYY" format="YYYY" style="width: 100%" @change="yearFun"/> -->
                <a-select
                  v-model:value="formData.year"
                  mode="multiple"
                  style="width: 100%"
                  placeholder="请选择年份"
                  :options="yearArr"
                ></a-select>
              </a-form-item>
						</a-col>
						<!--选择层级-->
            <a-col :span="12">
							<a-form-item label="选择层级部门" v-bind="validateInfos.deptCode" id="KpiInfoForm-dept" name="deptCode">
                <!-- <JSelectDept v-model:value="formData.dept" :multiple="false" :showButton="false" :expandonsearch="true" :search="true" rowKey="orgCode"/> -->
                <a-tree-select
                  v-model:value="formData.deptCode"
                  show-search
                  style="width: 100%"
                  :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
                  placeholder="请选择层级"
                  allow-clear
                  :treeDefaultExpandedKeys="treeDefaultExpandedKeys"
                  :tree-data="kpiRoomList"
                  tree-node-filter-prop="departName"
                  :fieldNames= "{children:'children', label:'departName', key: 'id', value: 'orgCode' }"
                ></a-tree-select>
							</a-form-item>
						</a-col>

            <a-col :span="12">
							<a-form-item label="KPI分类" v-bind="validateInfos.perentId" id="KpiInfoForm-perentId" name="perentId">
                <a-tree-select
                  v-model:value="formData.perentId"
                  show-search
                  style="width: 100%"
                  :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
                  placeholder="请选择KPI分类"
                  allow-clear
                  :tree-data="treeData"
                  tree-node-filter-prop="kpiType"
                  :fieldNames= "{children:'children', label:'kpiType', value: 'id' }"
                ></a-tree-select>
							</a-form-item>
						</a-col>

            <a-col :span="12">
							<a-form-item label="红区判断条件" v-bind="validateInfos.compareValue" id="KpiInfoForm-compareValue" name="compareValue">
                <a-radio-group v-model:value="formData.compareValue">
                  <a-radio-button value="-1">不计算</a-radio-button>
                  <a-radio-button value="1">>(大于)</a-radio-button>
                  <a-radio-button value="0"><(小于)</a-radio-button>
                  <a-radio-button value="2">范围</a-radio-button>
                  <a-radio-button value="3">比例</a-radio-button>
                </a-radio-group>
							</a-form-item>
						</a-col>

            <a-col :span="12">
							<a-form-item label="上级KPI" v-bind="validateInfos.parentId" id="KpiInfoForm-parentId" name="parentId">
                <a-tree-select
                  v-model:value="formData.parentId"
                  show-search
                  style="width: 100%"
                  :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
                  placeholder="请选择上级KPI"
                  allow-clear
                  tree-default-expand-all
                  :tree-data="infoTreeData"
                  tree-node-filter-prop="kpiName"
                  :fieldNames= "{children:'children', label:'kpiName', value: 'kpiCode' }"
                ></a-tree-select>
							</a-form-item>
						</a-col>
            
            <a-col :span="12">
							<a-form-item label="是否是重点关注KPI" v-bind="validateInfos.emphasisType" id="KpiInfoForm-emphasisType" name="emphasisType">
                <a-switch checkedChildren="是" unCheckedChildren="否" v-model:checked="isData.emphasisType" />
							</a-form-item>
						</a-col>

            <a-col :span="12">
							<a-form-item label="KPI名称" v-bind="validateInfos.kpiName" id="KpiInfoForm-kpiName" name="kpiName">
								<a-input v-model:value="formData.kpiName" placeholder="请输入KPI名称"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
            
            <a-col :span="12">
							<a-form-item label="是否在周/月报中展示" v-bind="validateInfos.visible" id="KpiInfoForm-visible" name="visible">
                <a-switch checkedChildren="是" unCheckedChildren="否" v-model:checked="isData.visible" />
							</a-form-item>
						</a-col>
						
            <a-col :span="12">
							<a-form-item label="是否自动采集" v-bind="validateInfos.active" id="KpiInfoForm-active" name="active">
								<!-- <a-input-number v-model:value="formData.active" placeholder="请选择"/> -->
                <a-select v-model:value="formData.active" optionFilterProp="children" allowClear
                   placeholder="请选择" style="width:100%;height:100%">
                  <a-select-option v-for="d in questionList" :key="d.id" :value="d.id">
                    {{ d.questionText }}
                  </a-select-option>
                </a-select>
							</a-form-item>
						</a-col>
					
            <a-col :span="12">
							<a-form-item label="是否在录入中展示" v-bind="validateInfos.visibleWrite" id="KpiInfoForm-visibleWrite" name="visibleWrite">
                <a-switch checkedChildren="是" unCheckedChildren="否" v-model:checked="isData.visibleWrite" />
							</a-form-item>
						</a-col>

             <!--选择填写部门-->
             <a-col :span="12">
							<a-form-item label="填写部门" v-bind="validateInfos.inputPermission" id="KpiInfoForm-inputPermission" name="inputPermission">
                <!-- <JSelectDept v-model:value="formData.inputPermission" :multiple="false" placeholder="请选择填写部门" :showButton="false" :expandonsearch="true" :search="true"/> -->
                <a-tree-select
                  v-model:value="formData.inputPermission"
                  show-search
                  style="width: 100%"
                  :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
                  placeholder="请选择填写部门"
                  allow-clear
                  :treeDefaultExpandedKeys="treeDefaultExpandedKeys"
                  :tree-data="kpiRoomList"
                  tree-node-filter-prop="departName"
                  :fieldNames= "{children:'children', label:'departName', key: 'id', value: 'orgCode' }"
                ></a-tree-select>
							</a-form-item>
						</a-col>

            <a-col :span="12">
							<a-form-item label="是否可用" v-bind="validateInfos.activeLook" id="KpiInfoForm-activeLook" name="activeLook">
                <a-switch checkedChildren="是" unCheckedChildren="否" v-model:checked="isData.activeLook" />
							</a-form-item>
						</a-col>

            <a-col :span="12">
							<a-form-item label="是否进入柏拉图" v-bind="validateInfos.plato" id="KpiInfoForm-plato" name="plato">
                <a-switch checkedChildren="是" unCheckedChildren="否" v-model:checked="isData.plato" />
							</a-form-item>
						</a-col>
           
            <a-col :span="12">
							<a-form-item label="填报排序" v-bind="validateInfos.inputSort" id="KpiInfoForm-inputSort" name="inputSort">
								<a-input-number v-model:value="formData.inputSort" placeholder="请输入填报排序"/>
							</a-form-item>
						</a-col>

            <a-col :span="12">
							<a-form-item label="第三方指标名称" v-bind="validateInfos.thirdName" id="KpiInfoForm-thirdName" name="thirdName">
								<a-input v-model:value="formData.thirdName" placeholder="请输入第三方接口指标名称"  allow-clear ></a-input>
							</a-form-item>
						</a-col>


            <a-col :span="12">
							<a-form-item label="保留小数位数" v-bind="validateInfos.decimalNum" id="KpiInfoForm-decimalNum" name="decimalNum">
								<a-input-number v-model:value="formData.decimalNum" placeholder="请输入保留小数位数"/>
							</a-form-item>
						</a-col>

            <a-col :span="12">
							<a-form-item label="第三方指标id" v-bind="validateInfos.thirdKey" id="KpiInfoForm-thirdKey" name="thirdKey">
								<a-input v-model:value="formData.thirdKey" placeholder="请输入第三方接口指标key"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
          
            <a-col :span="12">
							<a-form-item label="报表排序" v-bind="validateInfos.reportSort" id="KpiInfoForm-reportSort" name="reportSort">
								<a-input-number v-model:value="formData.reportSort" placeholder="请输入报表排序"/>
							</a-form-item>
						</a-col>

            <a-col :span="12">
							<a-form-item label="计算公式说明" v-bind="validateInfos.text" id="KpiInfoForm-text" name="text">
								<a-input v-model:value="formData.text" placeholder="请输入计算公式说明"  allow-clear ></a-input>
							</a-form-item>
						</a-col>

						<a-col :span="12">
							<a-form-item label="单位" v-bind="validateInfos.unit" id="KpiInfoForm-unit" name="unit">
								<a-input v-model:value="formData.unit" placeholder="请输入单位"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
           <a-col :span="12">
							<a-form-item label="截止时间" v-bind="validateInfos.deadline" id="KpiInfoForm-deadline" name="deadline">
								<a-input v-model:value="formData.deadline" placeholder="请输入截止时间"  allow-clear ></a-input>
							</a-form-item>
						</a-col>

            <a-col :span="12">
							<a-form-item label="标签" v-bind="validateInfos.label" id="KpiInfoForm-label" name="label">
                <kt-dict
                  v-model:value="formData.label"
                  placeholder="请选择标签"
                  dict="MES3_KPI_INFO_LABEL"
                  mode="multiple"
                />
							</a-form-item>
						</a-col>


            <a-divider>数据填报职责分工</a-divider>
            <!--数据填报职责分工(车间职责分工)-->
            <a-col :span="12">
							<a-form-item label="R" v-bind="validateInfos.provideBy" id="KpiInfoForm-provideBy" name="provideBy">
                <a-select v-model:value="formData.provideBy" placeholder="请选择" show-search :allowClear=true
                  :getPopupContainer="(node) => getPopupContainer()"
                  :maxTagCount="1" :filter-option="filterOption" :options="userOptions"
                  :fieldNames="{ label: 'realname', value: 'id' }">
                </a-select>
							</a-form-item>
						</a-col>
            <a-col :span="12">
							<a-form-item label="A" v-bind="validateInfos.possessBy" id="KpiInfoForm-possessBy" name="possessBy">
                <a-select v-model:value="formData.possessBy" placeholder="请选择" show-search :allowClear=true
                  :getPopupContainer="(node) => getPopupContainer()"
                  :maxTagCount="1" :filter-option="filterOption" :options="userOptions"
                  :fieldNames="{ label: 'realname', value: 'id' }">
                </a-select>
							</a-form-item>
						</a-col>
            <a-col :span="12">
							<a-form-item label="C" v-bind="validateInfos.consultedBy" id="KpiInfoForm-consultedBy" name="consultedBy">
                <a-select v-model:value="formData.consultedBy" placeholder="请选择" show-search :allowClear=true
                  :getPopupContainer="(node) => getPopupContainer()"
                  :maxTagCount="1" :filter-option="filterOption" :options="userOptions"
                  :fieldNames="{ label: 'realname', value: 'id' }">
                </a-select>
							</a-form-item>
						</a-col>
            <a-col :span="12">
							<a-form-item label="I" v-bind="validateInfos.informedBy" id="KpiInfoForm-informedBy" name="informedBy">
								<a-input v-model:value="formData.informedBy" placeholder="请输入I"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
            
            <a-divider>KPI职责分工</a-divider>
            <!--KPI职责分工-->
            <a-col :span="12">
							<a-form-item label="R" v-bind="validateInfos.provideBy" id="KpiInfoForm-provideBy" name="dutyProvideBy">
                <a-select v-model:value="formData.dutyProvideBy" placeholder="请选择" show-search :allowClear=true
                  :getPopupContainer="(node) => getPopupContainer()"
                  :maxTagCount="1" :filter-option="filterOption" :options="userOptions"
                  :fieldNames="{ label: 'realname', value: 'id' }">
                </a-select>
							</a-form-item>
						</a-col>
            <a-col :span="12">
							<a-form-item label="A" v-bind="validateInfos.possessBy" id="KpiInfoForm-possessBy" name="dutyPossessBy">
                <a-select v-model:value="formData.dutyPossessBy" placeholder="请选择" show-search :allowClear=true
                  :getPopupContainer="(node) => getPopupContainer()"
                  :maxTagCount="1" :filter-option="filterOption" :options="userOptions"
                  :fieldNames="{ label: 'realname', value: 'id' }">
                </a-select>
							</a-form-item>
						</a-col>
            <a-col :span="12">
							<a-form-item label="C" v-bind="validateInfos.consultedBy" id="KpiInfoForm-consultedBy" name="dutyConsultedBy">
                <a-select v-model:value="formData.dutyConsultedBy" placeholder="请选择" show-search :allowClear=true
                  :getPopupContainer="(node) => getPopupContainer()"
                  :maxTagCount="1" :filter-option="filterOption" :options="userOptions"
                  :fieldNames="{ label: 'realname', value: 'id' }">
                </a-select>
							</a-form-item>
						</a-col>
            <a-col :span="12">
							<a-form-item label="I" v-bind="validateInfos.informedBy" id="KpiInfoForm-informedBy" name="dutyInformedBy">
								<a-input v-model:value="formData.dutyInformedBy" placeholder="请输入I"  allow-clear ></a-input>
							</a-form-item>
						</a-col>

          </a-row>
        </a-form>
      </template>
    </JFormContainer>
  </a-spin>
</template>

<script lang="ts" setup>
  import { ref, reactive, defineExpose, nextTick, defineProps, computed, onMounted } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getValueType } from '/@/utils';
  import { saveOrUpdate,list,queryDeptTreeList } from '../KpiInfo.api';
  import { Form } from 'ant-design-vue';
  import JFormContainer from '/@/components/Form/src/container/JFormContainer.vue';
  import { list as typeList } from '/@/views/dataCalculation/kpi/type/KpiType.api';
  import { getPopupContainer } from '/@/utils';
  import { userList } from '@/views/dataCalculation/actionPlan/problemType/ActionPlanProblemType.api';
  import KtDict from '/@/components/KtDict/KtDict.vue';
  import { useUserStore } from '/@/store/modules/user';
  // 用户信息
  const userStore = useUserStore();
  const props = defineProps({
    formDisabled: { type: Boolean, default: false },
    formData: { type: Object, default: () => ({})},
    formBpm: { type: Boolean, default: true }
  });
  const formRef = ref();
  const useForm = Form.useForm;
  const emit = defineEmits(['register', 'ok']);
  const treeData = ref([]);
  const infoTreeData = ref([]);
  const kpiRoomList = ref([]);
  const yearArr = ref([]);

  const treeDefaultExpandedKeys = ref([]);

  const isData = reactive<Record<string, any>>({
    emphasisType:false,
    visible:true,
    visibleWrite:true,
    activeLook:true,
    plato:false
  })
  const formData = reactive<Record<string, any>>({
    id: undefined,
    year: [],
    factory: '',   
    deptCode: undefined,   
    line: '',   
    kpiCode: '',   
    kpiName: '',   
    perentId: undefined,
    typeChildName: '',   
    plato: false,
    active: undefined,
    text: '',   
    sort: undefined,
    unit: '',   
    emphasisType: undefined,
    compareValue: '1',
    isomerism: undefined,
    parentId: undefined,
    thirdKey: '',   
    thirdName: '',   
    decimalNum: undefined,
    visible: undefined,
    visibleWrite: true,
    reportPermission: '',   
    inputPermission: '',   
    activeLook: false,
    reportSort: undefined,
    inputSort: undefined,
    delFlag: undefined,
    provideBy: '',
    possessBy: '',
    consultedBy: '',
    informedBy: '',
    deadline: '',
    label: '',
    dutyProvideBy:'',
    dutyPossessBy: '',
    dutyConsultedBy: '',
    dutyInformedBy: '',
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 7 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  //用户下拉列表数据
  const userOptions = ref([] as any);
  //是否自动采集
  const questionList = ref([
    { questionText: '手动', id: 0 },
    { questionText: '自动', id: 1 }
  ])

  //表单验证
  const validatorRules = reactive({
    year: [{ required: true, message: '请选择年份!'},],
    deptCode: [{ required: true, message: '请选择层级部门'},],
    perentId: [{ required: true, message: '请选择KPI分类'},],
    kpiName: [{ required: true, message: '请输入KPI名称'},],
    active: [{ required: true, message: '请选择是否自动采集'},],
    //plato: [{ required: true, message: '请选择是否进入柏拉图'},],
  });
  const { resetFields, validate, validateInfos } = useForm(formData, validatorRules, { immediate: false });

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
  function add() {
    edit({dataAdd:true});
  }

  /**
   * 编辑
   */
  function edit(record) {
    //kpi分类
    classification();
    //kpi清单
    kpiList();
    //单位
    deptTreeList();
    //获取年份集合
    getYear();
    //初始化
    initData();

    nextTick(() => {
      resetFields();
      const tmpData = {};
      Object.keys(formData).forEach((key) => {
        if(record.hasOwnProperty(key)){
          tmpData[key] = record[key]
        }
      })
      //赋值
      if(!record.dataAdd){
        isData.activeLook = tmpData.activeLook == '1'? true:false;
        isData.plato = tmpData.plato == '1'? true:false;
        isData.visibleWrite = tmpData.visibleWrite == '1'? true:false;
        isData.emphasisType = tmpData.emphasisType == '1'? true:false;
        isData.visible = tmpData.visible == '1'? true:false;
      }
      Object.assign(formData, tmpData);
     
      let years = tmpData.year?tmpData.year.split(","):undefined
      //
      if(years){
        formData.year = []
        years.forEach((element, index) => {
          formData.year.push(element);
        });
        yearFun(record.year, true);
      }
      // if(formData.year){
      //   formData.year = ['2025']
      // }
      if(record.label){
        formData.label = record.label.split(",");
      }
     
    });
  }

  /**
   * 提交数据
   */
  async function submitForm() {
    try {
      // 触发表单验证
      await validate();
    } catch ({ errorFields }) {
      if (errorFields) {
        const firstField = errorFields[0];
        if (firstField) {
          formRef.value.scrollToField(firstField.name, { behavior: 'smooth', block: 'center' });
        }
      }
      return Promise.reject(errorFields);
    }
    //上级KPI不能是当前KPI
    if(formData.parentId === formData.kpiCode){
      createMessage.warning("上级 KPI 不可选择当前指标，请重新选择");
      return false;
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
    model.activeLook = isData.activeLook?1:0;
    model.plato = isData.plato?1:0;
    model.visibleWrite = isData.visibleWrite?1:0;
    model.emphasisType = isData.emphasisType?1:0;
    model.visible = isData.visible?1:0;
 
    //model.deptCode = userStore.departCode;
    model.factoryCode = userStore.factoryCode;

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

  //KPI分类
  async function classification(){
    treeData.value = [];
    let res = await typeList({});
    for (const element of res) {
      if(element){
        element.disabled = true
      }
    }
    treeData.value.push(...res);
  }
  //获取年份(前后5年)
  function getYear(){
    // 获取当前年份
    yearArr.value = Array.from(
        { length: 11 }, 
        (_, i) => new Date().getFullYear() - 5 + i
      )
      .reverse()
      .map(year => ({ key: year, value: year+"" })); // 
  }
  //KPI清单
  async function kpiList(){
    let res = await list({pageNo: 1,pageSize: 9999});
    if(res && res.records){
      infoTreeData.value = [];
      // infoTreeData.value.push(...res.records);
      infoTreeData.value = res.records;
    }
  }
  //部门树
  async function deptTreeList(){
    let res = await queryDeptTreeList({factory:"",isRoot:"true"});
    kpiRoomList.value = res
    //获取第一层(展开)
    if(res){
      treeDefaultExpandedKeys.value = [res[0].orgCode];
    }
  }

  function yearFun(year,isEdit){
    if(isEdit!=true){
      formData.perentId = ''
      formData.parentId = ''
    }
    //classification(year);
    //kpiList(year);
  }

  async function initData(){
    //获取用户下拉选项数据
    userOptions.value = await userList({ pageSize: 9999 });

  }
  //下拉选择过滤方法
  function filterOption(input, option) {
    return option.realname.toLowerCase().indexOf(input.toLowerCase()) >= 0;
  };

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
