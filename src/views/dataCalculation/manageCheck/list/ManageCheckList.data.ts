import { ref } from 'vue';
import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { list as postList } from '/@/views/dataCalculation/mdm/postManage/MdmPost.api';
import { pageList as weekList } from '/@//views/dataCalculation/mdm/week/MdmWeek.api';
import { getDictItems } from '/@/api/common/api';
import { queryAllTreeData as categoryTreeList, allList as categoryList } from '/@//views/dataCalculation//manageCheck/category/ManageCheckCategory.api';
import { handleUserIdChange, USER_LIST, filterOption as userFilterOption, getDeptOptions } from '/@/views/dataCalculation/actionPlan/track/ActionPlanTrack.data';
import { cloneDeep } from 'lodash-es';
import { getCurrentDepart } from '/@/utils/common/compUtils';
export const currentDepart = getCurrentDepart();

// 岗位数据
export let POST_LIST = ref([] as any);
export let POST_MANAGER_LIST = ref([] as any);
export let CHECK_TYPE_POST_LIST = ref([] as any);
(async function f() {
  const { records } = await postList({ pageSize: 10000 });
  for (const item of records) {
    let obj = {
      label: item.postName,
      value: item.id.toString()
    };
    if(item.postType == 1){
      POST_MANAGER_LIST.value.push(cloneDeep(obj));
    }
    POST_LIST.value.push(cloneDeep(obj));
    obj.label += '检查';
    CHECK_TYPE_POST_LIST.value.push(obj);
  }
})();

//行动计划完成情况
export let ACTION_PLAN_FINISH = ref([] as any);
(async function f() {
  const records = await getDictItems("mes3_mdm_dict where group_code = 'MES3_ACTION_PLAN_FINISH', item_key, item_val");
  ACTION_PLAN_FINISH.value = records;
})();

//获取问题分类所有数据
export let CATEGORY_ALl_LIST = ref([] as any);
(async function f() {
  const records = await categoryList();
  CATEGORY_ALl_LIST.value = records;
})();

//人员表单配置
const userFormSchemaConfig = {
  component: 'Select',
  componentProps: ({ formActionType, formModel, schema }) => {
    return {
      options: USER_LIST.value,
      fieldNames: { label: 'realname', value: 'id' },
      maxTagCount: 2,
      showSearch: true,
      allowClear: true,
      filterOption: userFilterOption,
      onChange: (e) => handleUserIdChange(formModel, formActionType, schema.field, e, 'Dept')
    }
  },
  required: true,
} as FormSchema;

//人员搜索表单配置
const userSearchFormSchemaConfig = {
  component: 'Select',
  componentProps: {
    options: USER_LIST,
    showSearch: true,
    mode: 'multiple',
    filterOption: userFilterOption,
    fieldNames: {
      label: 'realname',
      value: 'id',
    },
  }
} as FormSchema;

//岗位表单配置
const postFormSchemaConfig = {
  component: 'Select',
  componentProps: {
    options: POST_LIST.value,
    filterOption,
    showSearch: true,
    dropdownMatchSelectWidth: false
  },
  required: true,
} as FormSchema;

//部门表单配置
const deptFormSchemaConfig = {
  component: 'Select',
  required: true,
} as FormSchema;

//图片表单配置
const imgUploadFormSchemaConfig = {
  component: 'JImageUploadV2',
  componentProps: {
    text: '图片上传',
    listType: 'picture-card',
    bizPath: 'temp',
    disabled: false,
    fileMax: 5,
  },
  colProps: {
    span: 24,
  }
} as FormSchema;

const JDictSelectTagFormSchemaConfig = {
  type: 'select',
  labelField: 'item_key',
  valueField: 'item_val',
  showChooseOption: false,
  showSearch: true,
};

//列表数据
export const columns: BasicColumn[] = [
  {
    title: '日期',
    align: "center",
    dataIndex: 'checkDate',
    customCell: (record) => {
      return { rowSpan: record.rowSpan ?? 1 };
    },
    fixed: 'left',
  },
  {
    title: '所属周',
    align: "center",
    dataIndex: 'week',
    width: 60,
    customCell: (record) => {
      return { rowSpan: record.rowSpan ?? 1 };
    },
    fixed: 'left',
  },
  {
    title: '问题描述',
    align: "center",
    dataIndex: 'problemDesc',
    width: 140,
    customCell: (record) => {
      return { rowSpan: record.rowSpan ?? 1 };
    },
    slots: { customRender: 'problemDescSlots' },
    fixed: 'left',
    resizable: true,
  },
  {
    title: '检查类型',
    align: "center",
    dataIndex: 'checkType_dictText',
    customRender: ({ value }) => {
      return value+'检查';
    },
  },
  {
    title: '检查人',
    align: "center",
    dataIndex: 'checkUser_dictText'
  },
  {
    title: '检查人部门',
    align: "center",
    dataIndex: 'checkUserDept_dictText'
  },
  {
    title: '检查人岗位',
    align: "center",
    dataIndex: 'checkUserPost_dictText'
  },
  {
    title: '责任部门',
    align: "center",
    dataIndex: 'checkOwnerDept_dictText'
  },
  {
    title: '责任班组',
    align: "center",
    dataIndex: 'checkOwnerClassGroup_dictText'
  },
  {
    title: '责任岗位',
    align: "center",
    dataIndex: 'checkOwnerPost_dictText'
  },
  {
    title: '责任人',
    align: "center",
    dataIndex: 'checkOwner_dictText'
  },
  {
    title: '责任管理人',
    align: "center",
    dataIndex: 'checkOwnerAdmin_dictText'
  },
  {
    title: '5s检查点',
    align: "center",
    dataIndex: 'fivesCheck_dictText'
  },
  {
    title: '问题图片',
    align: "center",
    dataIndex: 'problemImg',
    width: 80,
    slots: { customRender: 'imgSlots' },
  },
  {
    title: '问题分类',
    align: "center",
    dataIndex: 'problemType_dictText'
  },
  {
    title: '问题子项',
    align: "center",
    dataIndex: 'problemSubclass_dictText'
  },
  {
    title: '行动',
    align: "center",
    dataIndex: 'actionPlanTrack.actionTitle',
    width: 140,
    slots: { customRender: 'actionTitleSlots' },
    resizable: true,
  },
  {
    title: '执行人',
    align: "center",
    dataIndex: 'actionPlanTrack.executeUserName',
    customRender: ({ record }) => {
      return record.actionPlanTrack?.executeUserName;
    },
  },
  {
    title: '整改描述',
    align: "center",
    dataIndex: 'optimizeDesc',
    width: 140,
  },
  {
    title: '整改图片',
    align: "center",
    dataIndex: 'optimizeImg',
    width: 80,
    slots: { customRender: 'imgSlots' },
  },
  {
    title: '计划完成时间',
    align: "center",
    dataIndex: 'actionPlanTrack.planFinishDate',
    customRender: ({ record }) => {
      return record.actionPlanTrack?.planFinishDate;
    },
  },
  {
    title: '实际完成时间',
    align: "center",
    dataIndex: 'actionPlanTrack.practicalFinishDate',
    customRender: ({ record }) => {
      return record.actionPlanTrack?.practicalFinishDate;
    },
  },
  {
    title: '完成情况',
    align: "center",
    dataIndex: 'actionPlanTrack.finishState',
    customRender: ({ record }) => {
      if(!record.actionPlanTrack){
        return '';
      }
      return ACTION_PLAN_FINISH.value.find((item) => item.value == record.actionPlanTrack?.finishState).text;
    },
    customCell: (record) => {
      const state = record.actionPlanTrack?.finishState;
      let colorObj = { bgColor: '' }
      if (state == 1) {
        colorObj.bgColor = '#00FF00'
      } else if (state == 2 || state == 4) {
        colorObj.bgColor = '#FE0000'
      } else if (state == 0) {
        colorObj.bgColor = 'yellow'
      } else if (state == 3) {
        colorObj.bgColor = '#666666'
      } else {
        colorObj.bgColor = ''
      }
      return {
        style: {
          'background-color': colorObj.bgColor
        }
      }
    },
  },
  {
    title: '确认人',
    align: "center",
    dataIndex: 'actionPlanTrack.verifyUserName',
    customRender: ({ record }) => {
      return record.actionPlanTrack?.verifyUserName;
    },
  },
  {
    title: '是否进入车间行动计划',
    align: "center",
    dataIndex: 'actionPlanTrack.status',
    customRender: ({ record }) => {
      return record.actionPlanTrack?.status == 1 ? '是' : '否';
    },
  },
  {
    title: '录入时间',
    align: "center",
    dataIndex: 'createTime',
  },
  {
    title: '创建人',
    align: "center",
    dataIndex: 'createBy_dictText'
  },
  {
    title: '创建部门',
    align: "center",
    dataIndex: 'deptCode_dictText'
  },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: "日期",
    field: 'checkDate',
    component: 'RangeDate',
    componentProps: {
      valueFormat: 'YYYY-MM-DD'
    },
  },
  {
    label: "问题分类",
    field: 'problemType',
    component: 'ApiTreeSelect',
    componentProps: {
      api: categoryTreeList,
      showSearch: true,
      treeNodeFilterProp: "categoryName",
      fieldNames: { children: 'childList', label: 'categoryName', value: 'code' },
      multiple: 'true',
      
    }
  },
  {
    label: "问题子项",
    field: 'problemSubclass',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "mes3_mdm_dict where group_code = 'MES3_SUB_CLASS', item_key, item_val",
      ...JDictSelectTagFormSchemaConfig,
    },
  },
  {
    label: "完成情况",
    field: 'actionPlanTrack.finishState',
    component: 'Select',
    componentProps: {
      options: ACTION_PLAN_FINISH,
    },
  },
  {
    label: "检查部门",
    field: 'checkUserDept',
    component: 'KtDepartTreeSelect',
    componentProps: {
      defaultExpandLevel: 1,
      dropdownMatchSelectWidth: 220,
      multiple: true,
    },
    defaultValue: currentDepart.id,
  },
  {
    label: "责任部门",
    field: 'checkOwnerDept',
    component: 'KtDepartTreeSelect',
    componentProps: {
      defaultExpandLevel: 1,
      dropdownMatchSelectWidth: 220,
      multiple: true,
    },
    defaultValue: currentDepart.id,
  },
  {
    ...{
      label: "检查人",
      field: 'checkUser',
      component: 'Select',
    },
    ...userSearchFormSchemaConfig
  },
  {
    label: '检查人岗位',
    field: 'checkUserPost',
    component: 'Select',
    componentProps: {
      options: POST_MANAGER_LIST,
      filterOption,
      mode: 'multiple',
      showSearch: true,
    },
  },
  {
    ...{
      label: "责任人",
      field: 'checkOwner',
      component: 'Select',
    },
    ...userSearchFormSchemaConfig
  },
  {
    label: '责任岗位',
    field: 'checkOwnerPost',
    component: 'Select',
    componentProps: {
      options: POST_LIST,
      filterOption,
      mode: 'multiple',
      showSearch: true,
    },
  },
  {
    ...{
      label: "执行人",
      field: 'actionPlanTrack.executeUserId',
      component: 'Select',
    },
    ...userSearchFormSchemaConfig
  },
  {
    ...{
      label: "确认人",
      field: 'actionPlanTrack.verifyUserId',
      component: 'Select',
    },
    ...userSearchFormSchemaConfig
  },
  {
    label: "创建部门",
    field: 'deptCode',
    component: 'KtDepartTreeSelect',
    componentProps: {
      defaultExpandLevel: 1,
      fieldNames: { label: 'departName', value: 'orgCode' },
      dropdownMatchSelectWidth: 220,
    },
  },
  {
    label: "录入时间",
    field: 'createTime',
    component: 'RangeDate',
    componentProps: {
      valueFormat: 'YYYY-MM-DD'
    },
  },
  {
    label: "是否进入行动",
    field: 'actionPlanTrack.status',
    component: 'Select',
    componentProps: {
      options:[
        {label: '进入', value: '1'},
        {label: '不进入', value: '0'},
      ]
    },
  },
  {
    label: "是否录入行动",
    field: 'enterAction',
    component: 'Select',
    componentProps: {
      options:[
        {label: '已录入', value: '1'},
        {label: '未录入', value: '0'},
      ]
    },
  },
  {
    label: "检查类型",
    field: 'checkType',
    component: 'Select',
    componentProps: {
      showSearch: true,
      options: CHECK_TYPE_POST_LIST,
      filterOption,
    },
  },
  {
    label: "5S检查点",
    field: 'fivesCheck',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "mes3_mdm_dict where group_code = 'MES3_5S_POINT', item_key, item_val",
      ...JDictSelectTagFormSchemaConfig,
    },
  },
  {
    label: "查询部门子级",
    field: 'queryDeptChild',
    component: 'RadioGroup',
    componentProps: {
      options: [
        {label: '是', value: 1},
        {label: '否', value: 0},
      ]
    },
    defaultValue: 0,
  },
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '日期',
    field: 'checkDate',
    component: 'DatePicker',
    componentProps: ({ formModel }) => {
      return {
        valueFormat: 'YYYY-MM-DD',
        onChange: async (value) => {
          formModel.week = await getWeek(value);
        }
      }
    },
    required: true,
  },
  {
    label: '所属周',
    field: 'week',
    component: 'Input',
    componentProps: {
      disabled: true,
    },
  },
  {
    label: '检查类别',
    field: 'checkType',
    component: 'Select',
    componentProps: {
      options: CHECK_TYPE_POST_LIST.value,
      filterOption,
      showSearch: true,
    },
    required: true,
  },
  {
    ...{
      label: '检查人',
      field: 'checkUser',
    },
    ...userFormSchemaConfig
  },
  {
    label: '检查人岗位',
    field: 'checkUserPost',
    component: 'Select',
    componentProps: {
      options: POST_MANAGER_LIST.value,
      filterOption,
      showSearch: true,
    },
    required: true,
  },
  {
    ...{ 
      label: '检查人部门', 
      field: 'checkUserDept', 
      componentProps: ({ formModel }) => {
        return {
          options: getDeptOptions(formModel.checkUser),
          showSearch: true,
          optionFilterProp: 'label',
        }
      },
    },
    ...deptFormSchemaConfig
  },
  {
    ...{
      label: '责任人',
      field: 'checkOwner',
    },
    ...userFormSchemaConfig
  },
  {
    ...{
      label: '责任岗位',
      field: 'checkOwnerPost',
    },
    ...postFormSchemaConfig
  },
  {
    ...{ 
      label: '责任部门', 
      field: 'checkOwnerDept', 
      componentProps: ({ formModel }) => {
        return {
          options: getDeptOptions(formModel.checkOwner),
          showSearch: true,
          optionFilterProp: 'label',
        }
      },
    },
    ...deptFormSchemaConfig
  },
  {
    label: '责任班组',
    field: 'checkOwnerClassGroup',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "mes3_mdm_shift_group, group_name, id, del_flag = 0",
      ...JDictSelectTagFormSchemaConfig,
    },
  },
  {
    ...{
      label: '责任管理人',
      field: 'checkOwnerAdmin',
    },
    ...userFormSchemaConfig
  },
  {
    label: '问题描述',
    field: 'problemDesc',
    component: 'InputTextArea',
    colProps: {
      span: 24,
    },
    dynamicRules: ({ values }) => {
      //需要return
      return [
        {
          required: true,
          validator: (_, value) => {
            return new Promise((resolve, reject) => {
              if (!value) {
                reject('请输入问题描述');
              }
              if (value.length<8) {
                reject('问题描述长度不能少于8个字符');
              }
              resolve();
            });
          },
        },
      ];
    },
  },
  {
    ...{
      label: '问题图片',
      field: 'problemImg',
    },
    ...imgUploadFormSchemaConfig
  },
  {
    label: '问题分类',
    field: 'problemType',
    component: 'ApiTreeSelect',
    componentProps: ({ formModel }) => {
      return {
        api: categoryTreeList,
        showSearch: true,
        treeNodeFilterProp: "categoryName",
        fieldNames: { children: 'childList', label: 'categoryName', value: 'code' },
        params: {
          disabled: true
        },
        onChange: (e) => {
          if (fivesCheck(formModel)) {
            formModel.fivesCheck = null;
          }
        }
      }
    },
    required: true,
  },
  {
    label: '5s检查点',
    field: 'fivesCheck',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "mes3_mdm_dict where group_code = 'MES3_5S_POINT', item_key, item_val",
      ...JDictSelectTagFormSchemaConfig,
    },
    dynamicDisabled: ({ values }) => !fivesCheck(values),
  },
  {
    label: '分类子项',
    field: 'problemSubclass',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "mes3_mdm_dict where group_code = 'MES3_SUB_CLASS', item_key, item_val",
      ...JDictSelectTagFormSchemaConfig,
    },
    required: true,
  },
  {
    label: '整改描述',
    field: 'optimizeDesc',
    component: 'InputTextArea',
    colProps: {
      span: 24,
    }
  },
  {
    ...{
      label: '整改图片',
      field: 'optimizeImg',
    },
    ...imgUploadFormSchemaConfig
  },
  {
    label: '行动/其他',
    field: 'extJson',
    component: 'Input',
    show: false
  },
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false
  },
  {
    label: '',
    field: 'delFlag',
    component: 'Input',
    show: false
  },
];

//下拉选择过滤方法
function filterOption(input, option) {
  return option.label.toLowerCase().indexOf(input.toLowerCase()) >= 0;
};

//查看问题分类是否为5s管理
function fivesCheck(data) {
  let rootData = CATEGORY_ALl_LIST.value.find(item => item.categoryName === '5S管理')
  let typeData = CATEGORY_ALl_LIST.value.find(item => item.code == data.problemType)
  return typeData?.code?.startsWith(rootData?.code);
}

export async function getWeek(value){
  const { records } = await weekList({
    //自定义查询连接方式
    superQueryMatchType: 'and',
    //自定义查询条件
    superQueryParams: `[{"field":"startDate","rule":"le","val":"${value}","type":"date","dbType":"string"}, {"field":"endDate","rule":"ge","val":"${value}","type":"date","dbType":"string"}]`
  });
  let week = '';
  if (records && records.length > 0) {
    week = records[0].weekNumber;
  }
  return week;
}