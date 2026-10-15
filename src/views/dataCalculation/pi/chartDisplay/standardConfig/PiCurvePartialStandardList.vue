<template>
  <div>
    <!--引用表格-->
   <BasicTable @register="registerTable" :rowSelection="rowSelection">
     <!--插槽:table标题-->
      <template #tableTitle>
          <a-button type="primary" v-auth="'pi:mes3_pi_curve_partial_standard:add'" @click="handleAdd" preIcon="ant-design:plus-outlined"> 新增</a-button>
          <a-button  type="primary" v-auth="'pi:mes3_pi_curve_partial_standard:exportXls'" preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
          <j-upload-button type="primary" v-auth="'pi:mes3_pi_curve_partial_standard:importExcel'" preIcon="ant-design:import-outlined" @click="onImportXlsByParams">导入</j-upload-button>
          <a-dropdown v-if="selectedRowKeys.length > 0">
              <template #overlay>
                <a-menu>
                  <a-menu-item key="1" @click="batchHandleDelete">
                    <Icon icon="ant-design:delete-outlined"></Icon>
                    删除
                  </a-menu-item>
                </a-menu>
              </template>
              <a-button v-auth="'pi:mes3_pi_curve_partial_standard:deletionBatch'">批量操作
                <Icon icon="mdi:chevron-down"></Icon>
              </a-button>
        </a-dropdown>
      </template>
       <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)"/>
      </template>
      <!--字段回显插槽-->
      <template v-slot:bodyCell="{ column, record, index, text }">
      </template>
    </BasicTable>
    <!-- 表单区域 -->
    <PiCurvePartialStandardModal @register="registerModal" @success="handleSuccess"></PiCurvePartialStandardModal>
  </div>
</template>

<script lang="ts" name="pi-piCurvePartialStandard" setup>
  import {ref, reactive, computed, unref} from 'vue';
  import { useRoute } from 'vue-router';
  import {BasicTable, useTable, TableAction} from '/@/components/Table';
  import {useModal} from '/@/components/Modal';
  import { useListPage } from '/@/hooks/system/useListPage'
  import PiCurvePartialStandardModal from './components/PiCurvePartialStandardModal.vue'
  import {columns, getSearchFormSchema} from './PiCurvePartialStandard.data';
  import {list, deleteOne, batchDelete, getImportUrl, getExportUrl, getDevice, getRecipe, getStep} from './PiCurvePartialStandard.api';
  import { downloadFile } from '/@/utils/common/renderUtils';
  import { useUserStore } from '/@/store/modules/user';
  import {initDictOptions} from '/@/utils/dict/index';
  const queryParam = reactive<any>({});
  const checkedKeys = ref<Array<string | number>>([]);
  const userStore = useUserStore();
  const searchRecipeOptions = ref<any[]>([]);
  const route = useRoute();
  const dataSource = computed(() => {
    const fromParams = route.params?.dataSource;
    const fromQuery = route.query?.dataSource;
    let raw = Array.isArray(fromParams)
      ? fromParams[0]
      : (fromParams ?? (Array.isArray(fromQuery) ? fromQuery[0] : fromQuery));
    // fallback: 从 URL 路径直接解析
    if (!raw) {
      const pathMatch = route.path.match(/piCurvePartialStandardList\/(\d+)/);
      raw = pathMatch?.[1] ?? undefined;
    }
    const normalized = Number(raw);
    return Number.isFinite(normalized) && normalized > 0 ? normalized : 1;
  });

  // 搜索表单联动：区域变更 → 加载配方、设备
  const handleSearchAreaChange = async (value) => {
    const form = getForm();
    if (!value) {
      form.updateSchema({ field: 'recipeNo', componentProps: { options: [] } });
      form.updateSchema({ field: 'unitNo', componentProps: { options: [] } });
      form.updateSchema({ field: 'eopNo', componentProps: { options: [] } });
      form.setFieldsValue({ recipeNo: undefined, unitNo: undefined, eopNo: undefined });
      return;
    }
    // 加载配方
    const recipeRes = await getRecipe({areaNo: value, dataSource: dataSource.value});
    const recipeOptions = (recipeRes || []).map((item: any) => ({
      value: `${item.reccatNo}-${item.recipeNo}`,
      label: `${item.recipeName}(${item.reccatName || ''})`,
      recipeNo: item.recipeNo,
      reccatNo: item.reccatNo,
    }));
    searchRecipeOptions.value = recipeOptions;
    form.updateSchema({ field: 'recipeNo', componentProps: { options: recipeOptions } });

    form.updateSchema({ field: 'unitNo', componentProps: { options: [] } });
    form.updateSchema({ field: 'eopNo', componentProps: { options: [] } });
    form.setFieldsValue({ recipeNo: undefined, unitNo: undefined, eopNo: undefined });
  };

  // 搜索表单联动：配方变更 → 加载设备
  const handleSearchRecipeChange = async (value) => {
    const form = getForm();
    if (!value) {
      form.updateSchema({ field: 'unitNo', componentProps: { options: [] } });
      form.updateSchema({ field: 'eopNo', componentProps: { options: [] } });
      form.setFieldsValue({ unitNo: undefined, eopNo: undefined });
      return;
    }
    const item = searchRecipeOptions.value.find(r => r.value === value);
    const recipeNo = item?.recipeNo || '';
    const reccatNo = item?.reccatNo || '';
    const { pcuNo } = form.getFieldsValue();
    const deviceRes = await getDevice({areaNo: pcuNo, pcuNo, recipeNo, reccatNo, dataSource: dataSource.value});
    const unitOptions = (deviceRes || []).map((d: any) => {
      const device = d.deviceCode.split('-')[1];
      return { value: device, label: d.deviceName };
    });
    form.updateSchema({ field: 'unitNo', componentProps: { options: unitOptions } });
    form.updateSchema({ field: 'eopNo', componentProps: { options: [] } });
    form.setFieldsValue({ unitNo: undefined, eopNo: undefined });
  };

  // 搜索表单联动：设备变更 → 加载步骤
  const handleSearchUnitChange = async (value) => {
    const form = getForm();
    const { pcuNo } = form.getFieldsValue();
    if (!value || !pcuNo) {
      form.updateSchema({ field: 'eopNo', componentProps: { options: [] } });
      form.setFieldsValue({ eopNo: undefined });
      return;
    }
    const res = await getStep({areaNo: pcuNo, unitNo: value});
    const options = (res || []).map(item => ({ value: item.eopNo, label: item.eopName }));
    form.updateSchema({ field: 'eopNo', componentProps: { options } });
    form.setFieldsValue({ eopNo: undefined });
  };

  //注册model
  const [registerModal, {openModal}] = useModal();
  //注册table数据
  // dataSource=1 时才显示 ropId 列
  const visibleColumns = computed(() => {
    if (dataSource.value === 1) return columns;
    return columns.filter(col => col.dataIndex !== 'ropId');
  });

  const { prefixCls,tableContext,onExportXls,onImportXlsByParams } = useListPage({
      tableProps:{
           title: '曲线局部标准配置',
           api: list,
           columns: visibleColumns,
           canResize:false,
           formConfig: {
              //labelWidth: 120,
              schemas: getSearchFormSchema(handleSearchAreaChange, handleSearchRecipeChange, handleSearchUnitChange),
              autoSubmitOnEnter:true,
              showAdvancedButton:true,
              fieldMapToNumber: [
              ],
              fieldMapToTime: [
              ],
            },
           actionColumn: {
               width: 120,
               fixed:'right'
            },
            beforeFetch: (params) => {
              queryParam.dataSource = dataSource.value;
              return Object.assign(params, queryParam);
            },
      },
       exportConfig: {
          name:"曲线局部标准配置",
          url: getExportUrl,
          params: queryParam,
        },
        importConfigByParams: {
          url: getImportUrl,
          success: handleSuccess,
          params: { dataSource },
        },
  })

  const [registerTable, {reload, getForm},{ rowSelection, selectedRowKeys }] = tableContext

  // 加载区域字典（拆分逗号分隔的value）
  const loadAreaDictForSearch = async () => {
    const dictCode = "mes3_mdm_dict where del_flag = '0' and group_code = 'MES3_PI_CURVE_AREA', item_key, item_val";
    const res = await initDictOptions(dictCode);
    const options: {value: string; label: string}[] = [];
    (res || []).forEach((item: any) => {
      const val = (item.value || '').toString();
      const label = item.text || item.label || '';
      if (val.includes(',')) {
        val.split(',').forEach((subVal: string) => {
          const trimmed = subVal.trim();
          if (trimmed) {
            options.push({ value: trimmed, label: `${label}(${trimmed})` });
          }
        });
      } else {
        options.push({ value: val, label });
      }
    });
    getForm().updateSchema({ field: 'pcuNo', componentProps: { options } });
  };
  loadAreaDictForSearch();

  /**
    * 新增事件
    */
  function handleAdd() {
     openModal(true, {
       record: { dataSource: dataSource.value },
       isUpdate: false,
       showFooter: true,
     });
  }
   /**
    * 编辑事件
    */
  function handleEdit(record: Recordable) {
     openModal(true, {
       record: { ...record, dataSource: record?.dataSource ?? dataSource.value },
       isUpdate: true,
       showFooter: true,
     });
   }
   /**
    * 详情
   */
  function handleDetail(record: Recordable) {
     openModal(true, {
       record: { ...record, dataSource: record?.dataSource ?? dataSource.value },
       isUpdate: true,
       showFooter: false,
     });
   }
   /**
    * 删除事件
    */
  async function handleDelete(record) {
     await deleteOne({id: record.id}, handleSuccess);
   }
   /**
    * 批量删除事件
    */
  async function batchHandleDelete() {
     await batchDelete({ids: selectedRowKeys.value}, handleSuccess);
   }
   /**
    * 成功回调
    */
  function handleSuccess() {
      (selectedRowKeys.value = []) && reload();
   }
   /**
      * 操作栏
      */
  function getTableAction(record){
       return [
         {
           label: '编辑',
           onClick: handleEdit.bind(null, record),
           auth: 'pi:mes3_pi_curve_partial_standard:edit'
         }
       ]
   }
     /**
        * 下拉操作栏
        */
  function getDropDownAction(record){
       return [
         {
           label: '详情',
           onClick: handleDetail.bind(null, record),
         }, {
           label: '删除',
           popConfirm: {
             title: '是否确认删除',
             confirm: handleDelete.bind(null, record),
             placement: 'topLeft',
           },
           auth: 'pi:mes3_pi_curve_partial_standard:delete'
         }
       ]
   }


</script>

<style lang="less" scoped>
  :deep(.ant-picker),:deep(.ant-input-number){
    width: 100%;
  }
</style>
