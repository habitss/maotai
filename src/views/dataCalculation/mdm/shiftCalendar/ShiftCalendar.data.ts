import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import {useUserStore} from "@/store/modules/user";
import {USER_LIST} from "@/views/dataCalculation/actionPlan/track/ActionPlanTrack.data";
import {getPopupContainer} from "@/utils";

const userInfo = useUserStore();
const { t } = useI18n();
console.log('0000',userInfo?.userInfo?.orgCode)

//列表数据
export const columns: BasicColumn[] = [
  {
    title: '班次名称',
    align: "center",
    dataIndex: 'shiftName'
  },
  {
    title: '班组名称',
    align: "center",
    dataIndex: 'groupName'
  },
  {
    title: '人员分配',
    align: "center",
    dataIndex: 'groupUser'
  },
  {
    title: '上班时间',
    align: "center",
    dataIndex: 'startTime'
  },
  {
    title: '下班时间',
    align: "center",
    dataIndex: 'endTime'
  },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: t('language.department'),
    field: 'deptCode',
    component: 'Input',
    slot:'deptCode',
    colProps: { span: 6 },
  },
  {
    label: t('language.productLine'),
    field: 'lineCode',
    component: 'Input',
    slot:'lineCode',
    colProps: { span: 7 },
  },
];
//表单数据
export const addConfigFormSchemas: FormSchema[] = [
  {
    label: '班次名称',
    field: 'manageId',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'mes3_mdm_shift_manage, shift_name, id, del_flag = 0 and dept_code=\'' + userInfo?.userInfo?.orgCode +'\'',
    },
    required: true,
  },
  {
    label: '班组名称',
    field: 'groupId',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'mes3_mdm_shift_group, group_name, id, del_flag = 0 and dept_code=\'' + userInfo?.userInfo?.orgCode +'\'' ,
    },
    required: true,
  },
  // TODO 主键隐藏字段，目前写死为ID
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false
  },
];

export const editFormSchemas: FormSchema[] = [
  {
    label: '班次名称',
    field: 'manageId',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'mes3_mdm_shift_manage, shift_name, id, del_flag = 0 and dept_code=\'' + userInfo?.userInfo?.orgCode +'\'',
    },
    required: true,
  },
  {
    label: '班组名称',
    field: 'groupId',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'mes3_mdm_shift_group, group_name, id, del_flag = 0 and dept_code=\'' + userInfo?.userInfo?.orgCode +'\'' ,
    },
    required: true,
  },
  {
    label: '人员分配',
    field: 'groupUserIds',
    component: 'Select',
    colProps: { span: 20 },
    componentProps: () => {
      return {
        options: USER_LIST.value,
        fieldNames: {label: 'realname', value: 'id'},
        maxTagCount: 20,
        getPopupContainer:() => getPopupContainer(),
        mode:'multiple',
        allowClear: true,
        placement: 'bottomRight',
        showSearch: true,
        filterOption: (inputValue, option) =>{
          return option.realname.toLowerCase().indexOf(inputValue.toLowerCase()) >= 0;
        }
      }
    },
  },
  // TODO 主键隐藏字段，目前写死为ID
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false
  },
];
