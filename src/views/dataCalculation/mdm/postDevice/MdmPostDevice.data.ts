import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { useI18n } from '/@/hooks/web/useI18n';
const { t } = useI18n();
//列表数据
export const columns: BasicColumn[] = [
  {
    //设备名称
    title: t('language.deviceName'),
    align: 'center',
    dataIndex: 'deviceName',
    resizable: true,
  },
  {
    //设备编号
    title: t('language.deviceCode'),
    align: 'center',
    dataIndex: 'deviceCode',
    width: 100,
  },
  {
    //设备类型
    title: t('language.deviceType'),
    align: 'center',
    dataIndex: 'deviceTypeIdVal',
    width: 100,
  },
  {
    //设备简称
    title: t('language.shortName'),
    align: 'center',
    dataIndex: 'aliasName',
    width: 100,
    resizable: true,
  },
  {
    //设为主设备
    title: '设为主设备',
    align: 'center',
    dataIndex: 'isMain',
    slots: { customRender: 'isMain' },
    width: 100,
  },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: t('language.deviceName'),
    field: 'deviceName',
    component: 'Input',
    colProps: { span: 8 },
  },
];