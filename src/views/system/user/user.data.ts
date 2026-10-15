import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { getAllRolesListNoByTenant, getAllTenantList } from './user.api';
import { rules } from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { useI18n } from '/@/hooks/web/useI18n';
import { validateRules } from '@/utils/validate/index';
const { t } = useI18n();
let isKor = import.meta.env.VITE_GROUP === 'kro'
export const columns: BasicColumn[] = [
  {
    title: t('language.user_account'),
    dataIndex: 'username',
    width: 120,
  },
  {
    title: t('language.user_name'),
    dataIndex: 'realname',
    width: 100,
  },
  {
    title: t('language.user_name'),
    dataIndex: 'avatar',
    width: 120,
    customRender: render.renderAvatar,
  },
  // {
  //   title: '性别',
  //   dataIndex: 'sex',
  //   width: 80,
  //   sorter: true,
  //   customRender: ({ text }) => {
  //     return render.renderDict(text, 'sex');
  //   },
  // },
  // {
  //   title: '生日',
  //   dataIndex: 'birthday',
  //   width: 100,
  // },
  {
    title: t('language.phone_number'),
    dataIndex: 'phone',
    width: 100,
  },
  {
    title: t('language.department'),
    width: 150,
    dataIndex: 'orgCodeTxt',
  },
  {
    title: t('language.department_in_charge'),
    width: 150,
    dataIndex: 'departIds_dictText',
  },
  {
    title: t('language.fileStatue'),
    dataIndex: 'status_dictText',
    width: 80,
  },
];

export const recycleColumns: BasicColumn[] = [
  {
    title: t('language.user_account'),
    dataIndex: 'username',
    width: 100,
  },
  {
    title: t('language.user_name'),
    dataIndex: 'realname',
    width: 100,
  },
  {
    title: t('language.avatar'),
    dataIndex: 'avatar',
    width: 80,
    customRender: render.renderAvatar,
  },
  {
    title: t('language.sex'),
    dataIndex: 'sex',
    width: 80,
    sorter: true,
    customRender: ({ text }) => {
      return render.renderDict(text, 'sex');
    },
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    label: t('language.userName'),
    field: 'username',
    component: 'JInput',
    //colProps: { span: 6 },
  },
  {
    label: t('language.name'),
    field: 'realname',
    component: 'JInput',
    //colProps: { span: 6 },
  },
  {
    label: t('language.sex'),
    field: 'sex',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'sex',
      placeholder: t('language.pleaseSelectGender'),
      stringToNumber: true,
    },
    //colProps: { span: 6 },
  },
  {
    label: t('language.phone_number'),
    field: 'phone',
    component: 'Input',
    //colProps: { span: 6 },
  },
  {
    label: t('language.user_state'),
    field: 'status',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'user_status',
      placeholder: t('language.PleaseSelectStatus'),
      stringToNumber: true,
    },
    //colProps: { span: 6 },
  },
];

export const formSchema: FormSchema[] = [
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    label: t('language.user_account'),
    field: 'username',
    component: 'Input',
    dynamicDisabled: ({ values }) => {
      return !!values.id;
    },
    dynamicRules: ({ model, schema }) => {
      return [
        { ...rules.duplicateCheckRule('sys_user', 'username', model, schema, true), required: true }
      ]
    },
    componentProps: {
      placeholder: t('language.enterUserCount'),
    },
  },
  {
    label: t('language.login_password'),
    field: 'password',
    component: 'StrengthMeter',
    rules: [
      {
        required: true,
        message: t('language.enterPassword'),
      },
    ],
  },
  {
    label: t('language.confirm_password'),
    field: 'confirmPassword',
    component: 'InputPassword',
    dynamicRules: ({ values }) => rules.confirmPassword(values, true),
    componentProps: {
      placeholder: t('language.enterConfirmPassword'),
    },
  },
  {
    label: t('language.user_name'),
    field: 'realname',
    required: true,
    component: 'Input',
    componentProps: {
      placeholder: t('language.enterUserFullName'),
    },
  },
  // {
  //   label: '工号',
  //   field: 'workNo',
  //   required: true,
  //   component: 'Input',
  //   dynamicRules: ({ model, schema }) => rules.duplicateCheckRule('sys_user', 'work_no', model, schema, true),
  // },
  // {
  //   label: '职务',
  //   field: 'post',
  //   required: false,
  //   component: 'JSelectPosition',
  //   componentProps: {
  //     rowKey: 'code',
  //     labelKey: 'name',
  //   },
  // },
  {
    label: t('language.role'),
    field: 'selectedroles',
    component: 'ApiSelect',
    required: true,
    componentProps: {
      placeholder: t('language.selectRole'),
      mode: 'multiple',
      api: getAllRolesListNoByTenant,
      labelField: 'roleName',
      valueField: 'id',
    },
  },
  {
    label: t('language.tenement'),
    field: 'relTenantIds',
    component: 'ApiSelect',
    required: true,
    componentProps: {
      placeholder: t('language.selectTen'),
      mode: 'multiple',
      api: getAllTenantList,
      numberToString: true,
      labelField: 'name',
      valueField: 'id',
    },
  },
  {
    label: t('language.department'), //部门
    field: 'selecteddeparts',
    component: 'JSelectDept',
    required: true,
    componentProps: ({ formActionType, formModel }) => {
      return {
        sync: false,
        checkStrictly: true,
        defaultExpandLevel: 2,

        onSelect: (options, values) => {
          const { updateSchema } = formActionType;
          //所属部门修改后更新负责部门下拉框数据
          updateSchema([
            {
              field: 'departIds',
              componentProps: { options },
            },
          ]);
          //所属部门修改后更新负责部门数据
          formModel.departIds && (formModel.departIds = formModel.departIds.filter((item) => values.value.indexOf(item) > -1));
        },
      };
    },
  },
  {
    label: t('language.identity'),
    field: 'userIdentity',
    component: 'RadioGroup',
    defaultValue: 1,
    componentProps: ({ formModel }) => {
      return {
        options: [
          { label: t('language.normal_user'), value: 1, key: '1' },
          { label: t('language.superior'), value: 2, key: '2' },
        ],
        onChange: () => {
          formModel.userIdentity == 1 && (formModel.departIds = []);
        },
      };
    },
  },
  {
    label: t('language.department_in_charge'), //负责部门
    field: 'departIds',
    component: 'Select',
    componentProps: {
      mode: 'multiple',
    },
    ifShow: ({ values }) => values.userIdentity == 2,
  },
  {
    label: t('language.avatar'),
    field: 'avatar',
    component: 'JImageUpload',
    componentProps: {
      fileMax: 1,
    },
  },
  // {
  //   label: '生日',
  //   field: 'birthday',
  //   component: 'DatePicker',
  // },
  // {
  //   label: '性别',
  //   field: 'sex',
  //   component: 'JDictSelectTag',
  //   componentProps: {
  //     dictCode: 'sex',
  //     placeholder: '请选择性别',
  //     stringToNumber: true,
  //   },
  // },
  {
    label: t('language.email'),
    field: 'email',
    component: 'Input',
    dynamicRules: ({ model, schema }) => {
      return [
        // { ...rules.duplicateCheckRule('sys_user', 'email', model, schema, false)[0], trigger: 'blur' },
        { ...rules.rule('email', false)[0], trigger: 'blur' },
      ];
    },
    componentProps: {
      placeholder: t('language.enterEmail'),
    },
  },
  {
    label: t('language.phone_number'),
    field: 'phone',
    component: 'Input',
    dynamicRules: ({ model, schema }) => {
      console.log('校验', model, schema)
      // 去除-及空格后再校验手机号
      if (model.phone) {
        model.phone = JSON.parse(JSON.stringify(model.phone)).replace(/[- ]/g, '')
      }

      return [
        // { ...rules.duplicateCheckRule('sys_user', 'phone', model, schema, false)[0], trigger: 'blur' },
        { pattern: validateRules.mobile, message: t('language.mobileNumberError'), trigger: 'blur' },
      ];
    },
    componentProps: {
      placeholder: t('language.enterUserMobile'),
    },
  },
  // {
  //   label: '座机',
  //   field: 'telephone',
  //   component: 'Input',
  //   rules: [{ pattern: /^0\d{2,3}-[1-9]\d{6,7}$/, message: '请输入正确的座机号码' }],
  // },
  {
    label: t('language.workflow_engine'),
    field: 'activitiSync',
    defaultValue: 1,
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'activiti_sync',
      type: 'radio',
      stringToNumber: true,
    },
  },
];

export const formPasswordSchema: FormSchema[] = [
  {
    label: t('language.user_account'),
    field: 'username',
    component: 'Input',
    componentProps: { readOnly: true },
  },
  {
    label: t('language.login_password'),
    field: 'password',
    component: 'StrengthMeter',
    componentProps: {
      placeholder: t('language.inputText') + t('language.login_password'),
    },
    rules: [
      {
        required: true,
        message: t('language.inputText') + t('language.login_password'),
      },
    ],
  },
  {
    label: t('language.confirm_password'),
    field: 'confirmPassword',
    component: 'InputPassword',
    dynamicRules: ({ values }) => rules.confirmPassword(values, true),
  },
];

export const formAgentSchema: FormSchema[] = [
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    field: 'userName',
    label: t('language.user_account'),
    component: 'Input',
    componentProps: {
      readOnly: true,
      allowClear: false,
    },
  },
  {
    field: 'agentUserName',
    label: t('language.agentUsername'),
    required: true,
    component: 'JSelectUser',
    componentProps: {
      rowKey: 'username',
      labelKey: 'realname',
      maxSelectCount: 10,
    },
  },
  {
    field: 'startTime',
    label: t('language.agentStart'),
    component: 'DatePicker',
    required: true,
    componentProps: {
      showTime: true,
      valueFormat: 'YYYY-MM-DD HH:mm:ss',
      placeholder: t('language.selectAgentStart'),
      getPopupContainer: () => document.body,
    },
  },
  {
    field: 'endTime',
    label: t('language.agentEnd'),
    component: 'DatePicker',
    required: true,
    componentProps: {
      showTime: true,
      valueFormat: 'YYYY-MM-DD HH:mm:ss',
      placeholder: t('language.selectAgentEnd'),
      getPopupContainer: () => document.body,
    },
  },
  {
    field: 'status',
    label: t('language.fileStatue'),
    component: 'JDictSelectTag',
    defaultValue: '1',
    componentProps: {
      dictCode: 'valid_status',
      type: 'radioButton',
    },
  },
];

export const formQuitAgentSchema: FormSchema[] = [
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    field: 'userName',
    label: t('language.usernameText'),
    component: 'Input',
    componentProps: {
      readOnly: true,
      allowClear: false,
    },
  },
  {
    field: 'agentUserName',
    label: t('language.handover'),
    required: true,
    component: 'JSelectUser',
    componentProps: {
      rowKey: 'username',
      labelKey: 'realname',
      maxSelectCount: 1,
    },
  },
  {
    field: 'startTime',
    label: t('language.handoverStart'),
    component: 'DatePicker',
    required: true,
    componentProps: {
      showTime: true,
      valueFormat: 'YYYY-MM-DD HH:mm:ss',
      placeholder: t('language.selectHandoverStart'),
      getPopupContainer: () => document.body,
    },
  },
  {
    field: 'endTime',
    label: t('language.handoverEnd'),
    component: 'DatePicker',
    required: true,
    componentProps: {
      showTime: true,
      valueFormat: 'YYYY-MM-DD HH:mm:ss',
      placeholder: t('language.selectHandoverEnd'),
      getPopupContainer: () => document.body,
    },
  },
  {
    field: 'status',
    label: t('language.fileStatue'),
    component: 'JDictSelectTag',
    defaultValue: '1',
    componentProps: {
      dictCode: 'valid_status',
      type: 'radioButton',
    },
  },
];

//租户用户列表
export const userTenantColumns: BasicColumn[] = [
  {
    title: t('language.user_account'),
    dataIndex: 'username',
    width: 120,
  },
  {
    title: t('language.user_name'),
    dataIndex: 'realname',
    width: 100,
  },
  {
    title: t('language.avatar'),
    dataIndex: 'avatar',
    width: 120,
    customRender: render.renderAvatar,
  },
  {
    title: t('language.phone_number'),
    dataIndex: 'phone',
    width: 100,
  },
  {
    title: t('language.department'),
    width: 150,
    dataIndex: 'orgCodeTxt',
  },
  {
    title: t('language.fileStatue'),
    dataIndex: 'status',
    width: 80,
    customRender: ({ text }) => {
      if (text === '1') {
        return t('language.normalStatus');
      } else if (text === '3') {
        return t('language.underReview');
      } else {
        return t('language.rejected');
      }
    },
  },
];

//用户租户搜索表单
export const userTenantFormSchema: FormSchema[] = [
  {
    label: t('language.userName'),
    field: 'username',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    label: t('language.nameText'),
    field: 'realname',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    label: t('language.gender'),
    field: 'sex',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'sex',
      placeholder: t('language.pleaseSelectGender'),
      stringToNumber: true,
    },
    colProps: { span: 6 },
  },
];
