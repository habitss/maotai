<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose :title="title" :width="'70%'" :minHeight="level === 1 ? 250 : 500" @ok="handleSubmit">
    <!--KPI level = 1 显示 表单  -->
    <BasicForm v-if="level === 1 || level === 0" @register="registerForm">
      <template #kpiCodeSlot="{ model, field }">
        <a-tree-select
          v-model:value="model[field]"
          show-search
          style="width: 100%"
          :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
          placeholder="请选择KPI"
          allow-clear
          tree-default-expand-all
          :tree-data="kpiTreeData"
          tree-node-filter-prop="kpiName"
          :fieldNames="{ children: 'children', label: 'kpiName', value: 'kpiCode' }"
        ></a-tree-select>
      </template>
    </BasicForm>

    <!--PI level > 1 显示 列表 字段有 报表名称  PI名称 时间PI名称-->
    <div v-else>
      <a-button type="primary" @click="handleAddRow" style="margin-bottom: 16px;">添加行</a-button>
      <a-table
        :columns="piColumns"
        :dataSource="piDataSource"
        :pagination="false"
        rowKey="key"
        bordered
      >
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.dataIndex === 'tableId'">
            <j-dict-select-tag
              v-model:value="record.tableId"
              placeholder="请选择报表"
              :dictCode="reportDictCode"
              showSearch
              style="width: 100%"
              @change="(val) => handleReportChange(val, record)"
            />
          </template>
          <template v-else-if="column.dataIndex === 'fieldId'">
            <j-dict-select-tag
              v-model:value="record.fieldId"
              placeholder="请选择PI"
              :dictCode="getFieldDictCode(record.tableId)"
              showSearch
              style="width: 100%"
            />
          </template>
          <template v-else-if="column.dataIndex === 'timeFieldId'">
            <j-dict-select-tag
              v-model:value="record.timeFieldId"
              placeholder="请选择时间PI"
              :dictCode="getFieldDictCode(record.tableId)"
              showSearch
              style="width: 100%"
            />
          </template>
          <template v-else-if="column.dataIndex === 'action'">
            <a @click="handleDeleteRow(index)" style="color: #f5222d">删除</a>
          </template>
        </template>
      </a-table>
    </div>
  </BasicModal>
</template>

<script lang="ts" setup>
    import {ref, computed, unref} from 'vue';
    import {BasicModal, useModalInner} from '/@/components/Modal';
    import {BasicForm, useForm} from '/@/components/Form/index';
    import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';
    import {initDictOptions} from '/@/components/Dict/src/JDictSelectUtil';
    import {formSchema} from '../KpiConsistencyConfig.data';
    import {saveOrUpdate} from '../KpiConsistencyConfig.api';
    import { list as kpiListApi } from '/@/views/dataCalculation/kpi/info/KpiInfo.api';

    // Emits声明
    const emit = defineEmits(['register','success']);
    const isUpdate = ref(true);
    const isDetail = ref(false);
    const level = ref(0);
    const currentRecord = ref<any>({});
    const configTitle = ref('');
    const kpiTreeData = ref<any[]>([]);

    // 报表字典编码
    const reportDictCode = "mes3_pi_customize_table where del_flag = '0', name, table_name";

    // 获取PI字段字典编码
    function getFieldDictCode(tableId: string) {
      if (!tableId) return '';
      return `mes3_pi_customize_table_field where del_flag = '0' and table_name = '${tableId}', name, field_name`;
    }

    // 加载KPI树形数据
    async function loadKpiTreeData() {
      let res = await kpiListApi({ pageNo: 1, pageSize: 9999 });
      if (res && res.records) {
        kpiTreeData.value = res.records;
      }
    }

    // PI列表列配置
    const piColumns = [
      {
        title: '报表名称',
        align: 'center',
        dataIndex: 'tableId',
        width: 200,
      },
      {
        title: 'PI',
        align: 'center',
        dataIndex: 'fieldId',
        width: 200,
      },
      {
        title: '时间PI',
        align: 'center',
        dataIndex: 'timeFieldId',
        width: 200,
      },
      {
        title: '操作',
        align: 'center',
        dataIndex: 'action',
        width: 80,
      },
    ];

    // PI列表数据
    const piDataSource = ref<any[]>([]);
    let rowKey = 0;

    //表单配置
    const [registerForm, { setProps,resetFields, setFieldsValue, validate, scrollToField }] = useForm({
        labelWidth: 150,
        schemas: formSchema,
        showActionButtonGroup: false,
        baseColProps: {span: 24}
    });

    /**
     * 添加行
     */
    function handleAddRow() {
      piDataSource.value.push({
        key: ++rowKey,
        tableId: '',
        fieldId: '',
        timeFieldId: '',
      });
    }

    /**
     * 删除行
     */
    function handleDeleteRow(index: number) {
      piDataSource.value.splice(index, 1);
    }

    /**
     * 报表选择变化
     */
    function handleReportChange(val: string, record: any) {
      // 清空PI和时间PI的值
      record.fieldId = '';
      record.timeFieldId = '';
    }

    //表单赋值
    const [registerModal, {setModalProps, closeModal}] = useModalInner(async (data) => {
        //重置表单
        await resetFields();
        setModalProps({confirmLoading: false,showCancelBtn:!!data?.showFooter,showOkBtn:!!data?.showFooter});
        isUpdate.value = !!data?.isUpdate;
        isDetail.value = !!data?.showFooter;

        // 获取level
        level.value = data?.record?.level ?? 0;
        currentRecord.value = data?.record || {};
        const piName = data?.record?.piName || '';
        const fieldTitle = data?.record?.configTitle || '';
        configTitle.value = piName ? `${piName}-${fieldTitle}` : fieldTitle;

        // 加载KPI树形数据
        await loadKpiTreeData();

        // 重置列表数据
        rowKey = 0;
        piDataSource.value = [];

        if (level.value > 1) {
          // PI level > 1，从 configField 字段中解析 JSON 获取 datas
          const configField = currentRecord.value.configField;
          const configValue = currentRecord.value[configField];
          if (configValue) {
            try {
              const configData = JSON.parse(configValue);
              if (configData.datas && configData.datas.length > 0) {
                piDataSource.value = configData.datas.map((item: any) => ({
                  key: ++rowKey,
                  ...item,
                }));
              } else {
                handleAddRow();
              }
            } catch (e) {
              console.error('解析配置数据失败:', e);
              handleAddRow();
            }
          } else {
            // 默认添加一行空数据
            handleAddRow();
          }
        } else if (level.value === 1 || level.value === 0) {
          // level === 1，从 configField 字段中解析 JSON 获取 kpiCode
          const configField = currentRecord.value.configField;
          const configValue = currentRecord.value[configField];
          if (configValue) {
            try {
              const configData = JSON.parse(configValue);
              await setFieldsValue({
                kpiCode: configData.kpiCode,
              });
            } catch (e) {
              console.error('解析配置数据失败:', e);
            }
          }
        }
        // 隐藏底部时禁用整个表单
       setProps({ disabled: !data?.showFooter })
    });

    //设置标题
    const title = computed(() => {
      const prefix = configTitle.value ? configTitle.value + ' - ' : '';
      return prefix+"配置";
    });

    //表单提交事件
    async function handleSubmit(v) {
        try {
            if (level.value === 1 || level.value === 0) {
              let values = await validate();
              setModalProps({confirmLoading: true});
              // 将数据打包成 JSON 格式
              const configData = {
                type: 1,
                kpiCode: values.kpiCode,
              };
              // 存入传过来的对象中，字段对应点击的字段
              const configField = currentRecord.value.configField;
              const submitRecord = {
                ...currentRecord.value,
                [configField]: JSON.stringify(configData),
              };
              // 提交保存
              await saveOrUpdate(submitRecord, true);
              //关闭弹窗
              closeModal();
              //刷新列表
              emit('success');
            } else {
              // PI level > 1，提交列表数据
              setModalProps({confirmLoading: true});
              const configField = currentRecord.value.configField;
              const datas = piDataSource.value.map(item => ({
                tableId: item.tableId,
                fieldId: item.fieldId,
                timeFieldId: item.timeFieldId,
              }));
              // 将数据打包成 JSON 格式
              const configData = {
                type: 2,
                datas: datas,
              };
              // 存入传过来的对象中
              const submitRecord = {
                ...currentRecord.value,
                [configField]: JSON.stringify(configData),
              };
              // 提交保存
              await saveOrUpdate(submitRecord, true);
              //关闭弹窗
              closeModal();
              //刷新列表
              emit('success');
            }
        } catch ({ errorFields }) {
           if (errorFields) {
             const firstField = errorFields[0];
             if (firstField) {
               scrollToField(firstField.name, { behavior: 'smooth', block: 'center' });
             }
           }
           return Promise.reject(errorFields);
        } finally {
            setModalProps({confirmLoading: false});
        }
    }
</script>

<style lang="less" scoped>
	/** 表格表头高度 */
  :deep(.ant-table-thead > tr > th) {
    padding: 8px 16px;
  }

	/** 时间和数字输入框样式 */
  :deep(.ant-input-number) {
    width: 100%;
  }

  :deep(.ant-calendar-picker) {
    width: 100%;
  }
</style>
