<template>
  <BasicDrawer v-bind="$attrs" @register="registerDrawer" title="设置背景" width="500px" @ok="handleSubmit">
    <!--颜色-->
    <ant-color-picker-with-title
      v-model:modelValue="primaryColor"
      title="选择颜色"
      description=""
      required
    />
    <!--背景图-->
    <!-- <div class="ant-picker-header">
      <span class="ant-picker-title">背景图</span>
    </div>
    <div class="ant-picker-body">
      <JImageUpload :fileMax="1" listType="picture" v-model:value="img"></JImageUpload>
    </div> -->

  </BasicDrawer>
</template>
<script lang="ts" setup>
import { ref } from 'vue';
import { useMessage } from '/@/hooks/web/useMessage';
import { BasicDrawer, useDrawerInner } from '@/components/Drawer';
import JImageUpload from '/@/components/Form/src/jeecg/components/JImageUploadV2.vue';
import AntColorPickerWithTitle from '@/views/dataCalculation/customReport/customReport/components/AntColorPickerWithTitle.vue';
//消息提示
const { createConfirm } = useMessage();
// 声明Emits
const emit = defineEmits(['success', 'clearComponent']);
//颜色
const primaryColor = ref('#faf9fb');
//图片
const img = ref('');
//父级数据
const parentData = ref({} as any);

//注册抽屉
const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
  parentData.value = data.selectCells
  let styleData = parentData.value[0][0]
  let styleConfigList = data.styleConfigList;

  let styleObj = styleConfigList.find(item => item.id === "style-id-"+styleData.row+"-"+styleData.col);
  let style = styleObj?.style;
  let color = style?.bgColor?style?.bgColor:"";
  if(color){
    primaryColor.value = color;
  }
});


//回调
async function handleSubmit() {
  //emit('success', values);
  emit('success', primaryColor.value,img.value,parentData.value);
  closeDrawer();
}

</script>


<style lang="less" scoped>
  /** 时间和数字输入框样式 */
  :deep(.ant-input-number){
		width: 100%
	}

	:deep(.ant-calendar-picker){
		width: 100%
	}

  :deep(.ant-select-selection-placeholder) {
    color: #BEBEBE !important;
  }

.ant-picker-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
  max-width: 330px;
}

.ant-picker-header {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: rgba(0, 0, 0, 0.85);
  line-height: 1.5715;
}

.ant-picker-title {
  font-weight: 500;
}

.ant-picker-required {
  color: #ff4d4f;
  margin-left: 4px;
}

.ant-picker-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
</style>