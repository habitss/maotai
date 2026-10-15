import {BasicColumn} from '/@/components/Table';
 import {ref } from 'vue';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
import { initDictOptions } from '/@/utils/dict';

export const piFieldOptions = ref<any[]>([]);
export const piTableId = ref<any[]>([]);
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: 'Sheet',
    field: 'sheetName',
    component: 'Input',
  },{
    label: '报表',
    field: 'tableId',
    component: 'JDictSelectTag',
    componentProps: ({formModel}) => {
      return {
        dictCode: "mes3_pi_customize_table where del_flag = '0', name, id",
        showChooseOption: false,
        showSearch: true,
        onChange: async (val) => {
          // formModel.fieldId=null
          formModel.fieldIds=null
          if (!val) {
            piFieldOptions.value = [];
            return;
          }
          piTableId.value = val;
          const dictData = await initDictOptions("mes3_pi_customize_table_field where table_id = '"+val+"', name, field_name");
          if (dictData) {
            piFieldOptions.value = dictData;
            //console.log('列表数据:', piFieldOptions.value);
          }
        },
      }
    }
  },{
    label: 'PI',
    field: 'fieldIds',
    component: 'JDictSelectTag',
    componentProps: ({formModel}) => {
      return {
        dictCode: "mes3_pi_customize_table_field where table_id = '"+formModel.tableId+"', name, field_name",
        showChooseOption: false,
        mode: 'multiple',
        showSearch: true,
        //labelInValue: true,//获取文本
      }
    }
  },
	// TODO 主键隐藏字段，目前写死为ID
	{
	  label: '',
	  field: 'id',
	  component: 'Input',
	  show: false
	},
];

