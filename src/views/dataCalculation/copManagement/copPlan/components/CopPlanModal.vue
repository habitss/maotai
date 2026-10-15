<template>
    <BasicModal
        @register="registerModal"
        title="配置COP计划"
        width="800"
        v-bind="$attrs"
        destroyOnClose
        :maskClosable="false"
    >
      <!--引用表格-->
      <BasicTable @register="registerTable">
        <template #action="{ record, column }">
          <TableAction :actions="createActions(record, column)" />
        </template>
      </BasicTable>
    </BasicModal>
  </template>

  <script lang="ts" setup>
  import {onMounted, ref} from 'vue';
    import { BasicModal, useModalInner } from '/@/components/Modal';
    import {useListPage} from "@/hooks/system/useListPage";
    import {
      getWeekList
    } from "@/views/dataCalculation/copManagement/copPlan/copPlanList.api";
    import { columns, dataMaps } from "@/views/dataCalculation/copManagement/copPlan/copPlanList.data";
    import {ActionItem, BasicColumn, EditRecordRow} from "@/components/Table";
    import {cloneDeep} from "lodash-es";
    import {useMessage} from "@/hooks/web/useMessage";
    const emit = defineEmits(['success']);

    //消息提示
    const { createMessage: msg } = useMessage();

    // 设备名称和id
    const record = ref();

    //注册modal
    const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data: any) => {
      record.value = data.record
      console.log(record.value);
      setModalProps({ confirmLoading: false, showCancelBtn: false, showOkBtn: false });
    });


    function handleClose(){
    closeModal();
    emit('success');
    }

    // 列表页面公共参数、方法
    const currentEditKeyRef = ref('');


  //列表数据
  const weekColumns: BasicColumn[] = [];

    const { tableContext } = useListPage({
      designScope: 'cop清洗计划',
      tableProps: {
        title: 'cop清洗计划',
        columns: weekColumns,
        showIndexColumn: false,
        showTableSetting: true,
        tableSetting: { fullScreen: true },
        useSearchForm: false,
        afterFetch:(records)=>{
          dataMaps.value = records;
        },
      },
    });

    // BasicTable绑定注册
    const [registerTable, { reload, getForm ,setColumns}] = tableContext;


    /**
     * 创建操作列
     */
    function createActions(record: EditRecordRow, column: BasicColumn): ActionItem[] {
      if (!record.editable) {
        return [
          {
            label: '编辑',
            disabled: currentEditKeyRef.value ? currentEditKeyRef.value !== record.key : false,
            onClick: handleEdit.bind(null, record),
          },
        ];
      }
      return [
        {
          label: '保存',
          onClick: handleSave.bind(null, record, column),
        },
        {
          label: '取消',
          popConfirm: {
            title: '是否取消编辑',
            confirm: handleCancel.bind(null, record, column),
          },
        },
      ];
    }

    function handleEdit(record: EditRecordRow) {
      currentEditKeyRef.value = record.key;
      record.onEdit?.(true);
    }

    function handleCancel(record: EditRecordRow) {
      currentEditKeyRef.value = '';
      record.onEdit?.(false, false);
    }

    async function handleSave(record: EditRecordRow) {
      // 校验
      msg.loading({ content: '正在保存...', duration: 0, key: 'saving' });
      const valid = await record.onValid?.();
      if (valid) {
        try {
          const data = cloneDeep(record.editValueRefs);
          console.log(data);
          //TODO 此处将数据提交给服务器保存
          // ...
          // 保存之后提交编辑状态
          const pass = await record.onEdit?.(false, true);
          if (pass) {
            currentEditKeyRef.value = '';
          }
          msg.success({ content: '数据已保存', key: 'saving' });
        } catch (error) {
          msg.error({ content: '保存失败', key: 'saving' });
        }
      } else {
        msg.error({ content: '请填写正确的数据', key: 'saving' });
      }
    }
  </script>

  <style>
    /**隐藏样式-modal确定按钮 */
    .jee-hidden {
        display: none !important;
    }
  </style>
