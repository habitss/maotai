import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
import { getColor} from '/@/utils/common/piColorUtils';
const { t } = useI18n();

//设备类型下拉树设置
const deviceTypeComponentProps = {
  dict: 'mes3_mdm_device_type, device_type_name, id',
  pidField: 'pid',
  pidValue: '0',
  hasChildField: 'has_child',
  params: {
    delFlag: 0
  },
  condition: JSON.stringify({del_flag: '0'})
};

//列表数据
export const columns: BasicColumn[] = [
  {
    //工厂
    title: t('language.factory'),
    align: 'center',
    dataIndex: 'factoryCode',
    width: 140,
    resizable: true,
    customRender: ({ text, record }) => {
      return record.factoryCodeVal;
    },
  },
  {
    //部门
    title: t('language.department'),
    align: 'center',
    dataIndex: 'deptCode',
    width: 120,
    resizable: true,
    customRender: ({ text, record }) => {
      return record.deptCodeVal;
    },
  },
  {
    //产线
    title: t('language.productLine'),
    align: 'center',
    dataIndex: 'lineCode',
    width: 120,
    resizable: true,
    customRender: ({ text, record }) => {
      return record.lineCodeVal;
    },
  },
  {
    //设备类型
    title: t('language.deviceType'),
    align: 'center',
    dataIndex: 'deviceTypeId',
    width: 140,
    customRender: ({ text, record }) => {
      return record.deviceTypeIdVal;
    },
  },
  {
    //设备名称
    title: t('language.deviceName'),
    align: 'center',
    dataIndex: 'deviceName',
    width: 200,
  },
  {
    //设备编号
    title: t('language.deviceCode'),
    align: 'center',
    dataIndex: 'deviceCode',
    width: 140,
  },
  {
    //设备简称
    title: t('language.shortName'),
    align: 'center',
    dataIndex: 'aliasName',
    width: 140,
    resizable: true,
  },
  {
    title: '是否后段设备',
    align: 'center',
    dataIndex: 'isAfter',
    customCell(record) {
      return {
        style: {
          'color': record.isAfter == 1 ? 'green' : '#FE0000'
        }
      }
    },
    customRender: ({ text }) => {
      return text == 1 ? '是' : '否';
    },
    width: 70,
  },
  {
    title: '是否关联岗位',
    align: 'center',
    dataIndex: 'havePost',
    width: 70,
    customCell(record) {
      return {
        style: {
          'color': record.havePost == 1 ? 'green' : '#FE0000'
        }
      }
    },
    customRender: ({ text }) => {
      return text ? '是' : '否';
    },
  },
  {
    title: '是否添加任务',
    align: 'center',
    dataIndex: 'haveTask',
    width: 70,
    customCell(record) {
      return {
        style: {
          'color': record.haveTask == 1 ? 'green' : '#FE0000'
        }
      }
    },
    customRender: ({ text }) => {
      return text ? '是' : '否';
    },
  },
  {
    //排序
    title: t('language.sort'),
    align: 'center',
    dataIndex: 'sort',
    width: 70,
  },
  {
    //备注
    title: t('language.remark'),
    align: 'left',
    dataIndex: 'remark',
    width: 400,
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
    colProps: { span: 6 },
  },
  {
    label: t('language.deviceType'),
    field: 'deviceTypeId',
    component: 'JTreeSelect',
    componentProps: deviceTypeComponentProps,
    colProps: { span: 6 },
  },
  {
    label: t('language.deviceName'),
    field: 'deviceName',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    label: t('language.deviceCode'),
    field: 'deviceCode',
    component: 'Input',
    colProps: { span: 6 },
  },
];

//表单数据
export const addFormSchemas: FormSchema[] = [
  {
    //部门
    label: t('language.department'),
    field: 'deptCode',
    component: 'Input',
    slot:'deptCode',
    required: true,
  },
  {
    //产线
    label: t('language.productLine'),
    field: 'lineCode',
    component: 'Input',
    slot:'lineCode',
    required: true,
  },
  {
    //设备类型
    label: t('language.deviceType'),
    field: 'deviceTypeId',
    component: 'JTreeSelect',
    componentProps: deviceTypeComponentProps,
    required: true,
  },
  {
    //设备编号
    label: t('language.deviceCode'),
    field: 'deviceCode',
    component: 'Input',
    required: true,
    helpMessage: ({ values }) => {
      return '提示：Adapter格式 区域-设备编码-PCU，RA格式 区域-设备编码';
    },
  },
  {
    //设备名称
    label: t('language.deviceName'),
    field: 'deviceName',
    component: 'Input',
    required: true,
  },
  {
    //设备简称
    label: t('language.shortName'),
    field: 'aliasName',
    component: 'Input',
  },
  {
    label: '是否为后段设备',
    field: 'isAfter',
    component: 'Switch',
    componentProps: {
      unCheckedChildren:'否',
      unCheckedValue:0,
      checkedChildren:'是',
      checkedValue:1,
    },
    defaultValue: 0,
  },
  {
    //排序
    label: t('language.sort'),
    field: 'sort',
    component: 'InputNumber',
  },
  {
    //备注
    label: t('language.remark'),
    field: 'remark',
    component: 'InputTextArea',
    //属性
    componentProps:{
      //可以点击清除图标删除内容
      allowClear: true,
      //自适应内容高度，可设置为 true | false 或对象：{ minRows: 2, maxRows: 6 }
      autoSize:{
        //最小显示行数
        minRows: 2,
        //最大显示行数
        maxRows: 3
      },
    }
  },
  //主键隐藏字段
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
  //工厂隐藏字段
  {
    label: '',
    field: 'factoryCode',
    component: 'Input',
    show: false,
  },
];
