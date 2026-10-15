<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose :title="title" 
  :defaultFullscreen="true" :showOkBtn="false">
    <div style="width: 100%;background-color: #f5f5f5;">
      <a-row :class="['p-4', `${prefixCls}--box`]" :gutter="10" style="width: 100%;">
        <a-col :xl="6" :lg="8" :md="10" :sm="24" style="flex: 1">
          <!--设备分组-->
          <a-card :bordered="false" title="设备分类">
            <div style="height: calc(100vh - 315px); overflow: auto;">
              <BasicTree checkable :treeData="treeData" @select="handleSelect" @check="handleCheck" :selectedKeys />
            </div>
            <a-row :gutter="10">
              <a-col :span="12" :align="'right'">
                <!--新增/修改-->
                <a-button type="primary" @click="handleCreate">{{ buttonText }}</a-button>
              </a-col>
              <a-col :span="12" :align="'left'">
                <a-button danger @click="handleDelete">删除</a-button>
              </a-col>
            </a-row>
          </a-card>
        </a-col>
        <a-col :xl="18" :lg="16" :md="14" :sm="24" style="flex: 1">
          <!--步骤参数配置-->
          <a-card :bordered="false" style="height: calc(100vh - 182px);" title="步骤参数配置">
            <!--v-if="treeData && treeData.length > 0"-->
            <StepList ref="stepListRef"/>
          </a-card>
        </a-col>
      </a-row>
    </div>
    <GroupModal @register="registerGroupModal" @success="initData" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { deletionBatchDictCategorize } from '/@/views/dataCalculation/mdm/dict/dict.api';
  import { onMounted, provide, ref,computed } from 'vue';
  import { useModal,useModalInner,BasicModal } from '/@/components/Modal';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { BasicTree } from '/@/components/Tree';
  import GroupModal from './GroupModal.vue';
  import StepList from './StepList.vue';
  import { loadDeviceGroupData,groupDelete } from '../PiCurveStandard.api';
  import { useMessage } from '/@/hooks/web/useMessage';
  const recipeId = ref(null);
  const dataSource = ref(null);
  const areaNo = ref(null);
  const recipeNo = ref(null);
  const recipeName = ref(null);
  const recipeData = ref({});

  // Emits声明
  const emit = defineEmits(['register','success']);

  const { createConfirm } = useMessage();
  const { prefixCls } = useDesign('depart-user');
  provide('prefixCls', prefixCls);
  const title = ref('配置');

  //选中值
  const selectedKeys = ref([]);
  //勾选值
  const checkedIds = ref([]);
  //树数据
  const treeData = ref([]);
  //model
  const [registerGroupModal, { openModal }] = useModal();
  // 创建一个对子组件的引用
  const stepListRef = ref();
  //加载数据(设备分类)
  async function initData(recipeId){
    treeData.value = await loadDeviceGroupData({ recipeId });
  }
  //树点击
  function handleSelect(selectedKeys){
    selectedKeys.value = selectedKeys;
    // dataSource,areaNo,recipeNo,recipeId,groupId
    // let data = {
    //   dataSource:dataSource.value,
    //   areaNo: areaNo.value,
    //   recipeNo: recipeNo.value,
    //   recipeId: recipeId.value,
    //   groupId: selectedKeys[0],
    //   recipeName: recipeName.value,
    // };
    recipeData.value.groupId= selectedKeys[0];
    stepListRef?.value?.reloadList(recipeData.value);
  }
  //树勾选
  function handleCheck(checkedKeys, e){
    checkedIds.value = checkedKeys;
  }
  /**
   * 新增事件
   */
  function handleCreate() {
    let record ={recipeId:recipeData.value.recipeId};
    if(checkedIds.value && checkedIds.value.length === 1){
      const item = treeData.value.find(item => item.key === checkedIds.value[0]);
      record.id = item.key;
      record.groupName = item.title;
    }
    openModal(true, {
      record,
      isUpdate: false,
      showFooter: true,
    });
  }

  /**
   * 删除事件
   */
  async function handleDelete(){
    if(!checkedIds.value || checkedIds.value.length === 0){
      return;
    }
    console.log('checkedIds.value :>> ', checkedIds.value);
    await groupDelete({ids: checkedIds.value.join(','),recipeId:recipeData.value.recipeId}, initData);
  }
 
  // Modal 注册
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    // recipeId.value = data.record.id;
    // dataSource.value = data.record.dataSource;
    // areaNo.value = data.record.areaNo;
    // recipeNo.value = data.record.recipeNo;
    // recipeName.value = data.record.recipeName;

    checkedIds.value = []
    await initData(data.record.id);

    recipeData.value = {
      dataSource: data.record.dataSource,
      areaNo: data.record.areaNo,
      recipeNo: data.record.recipeNo,
      recipeId: data.record.id,
      groupId: treeData?.value[0]?.key,
      recipeName: data.record.recipeName,
    };
    // 设置标题：配置-配方名称
    title.value = data.record.recipeName ? `配置-${data.record.recipeName}` : '配置';
    stepListRef?.value?.reloadList(recipeData.value);
    selectedKeys.value = [treeData?.value[0]?.key];
  });

  // 计算属性：根据 selectedKeys 长度返回按钮文字
  const buttonText = computed(() => {
    return checkedIds?.value?.length === 1 ? '修改' : '添加';
  });
</script>

<style lang="less" scoped>
	/** 时间和数字输入框样式 */
  :deep(.ant-input-number) {
    width: 100%;
  }

  :deep(.ant-calendar-picker) {
    width: 100%;
  }
</style>