<template>
  <BasicDrawer v-bind="$attrs" @register="registerDrawer" title="设置背景" width="520px" @ok="handleSubmit">
    <!--颜色-->
    <ant-color-picker-with-title
      v-model:modelValue="fontObj.fontColor"
      title="选择颜色："
      description=""
      required
    />
    <!--字体大小-->
    <div class="ant-picker-header">
      <span class="ant-picker-title">字体大小：</span>
    </div>
    <div class="ant-picker-body">
      <a-select v-model:value="fontObj.fontSize"  class="width-200 margin-left-10" :options="fontSizeArr" placeholder="请选择"></a-select>
    </div>
    <!--加粗-->
    <div class="ant-picker-header">
      <span class="ant-picker-title">字体粗细：</span>
    </div>
    <div class="ant-picker-body">
      <!-- <a-switch checkedChildren="加粗" unCheckedChildren="不加粗" v-model:checked="fontWeight" /> -->
      <div style="width: 20%;">
        <a-switch checkedChildren="加粗" unCheckedChildren="不加粗" v-model:checked="fontObj.fontWeight"/>
      </div>
    </div>

  </BasicDrawer>
</template>
<script lang="ts" setup>
import { ref } from 'vue';
import { useMessage } from '/@/hooks/web/useMessage';
import { BasicDrawer, useDrawerInner } from '@/components/Drawer';
import AntColorPickerWithTitle from '@/views/dataCalculation/customReport/customReport/components/AntColorPickerWithTitle.vue';
//消息提示
const { createConfirm } = useMessage();
// 声明Emits
const emit = defineEmits(['success', 'clearComponent']);
//字体对象
const fontObj = ref({
  //粗细
  fontWeight: false,
  //颜色
  fontColor: ref('#1890ff'),
  //字体大小
  fontSize: 15,
});
//父级数据
const parentData = ref({} as any);
//字体大小下拉框
const fontSizeArr = ref([]);

//注册抽屉
const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
  parentData.value = data.selectCells;
  let styleData = parentData.value[0][0]
  let styleConfigList = data.styleConfigList;

  let styleObj = styleConfigList.find(item => item.id === "style-id-"+styleData.row+"-"+styleData.col);
  let style = styleObj.style;

  let color = style.color?style.color:"";
  if(color){
    fontObj.value.fontColor = color;
  }

  let fontSize = style.fontSize?style.fontSize:"";
  if(fontSize){
    fontObj.value.fontSize = fontSize;
  }
  let fontWeight = style.fontWeight?style.fontWeight:"";
  if(fontWeight && fontWeight === 'bold'){
    fontObj.value.fontWeight = true;
  }else{
    fontObj.value.fontWeight = false;
  }

  fontSizeArr.value = generateFontSizeArray(12,32);
  debugger
});


//回调
async function handleSubmit() {
  //emit('success', values);
  emit('success', fontObj.value, parentData.value);
  // emit('success', primaryColor.value, 70, parentData.value);
  closeDrawer();
}

// 生成从start到end的字体大小数组
function generateFontSizeArray(start, end) {
  const arr = [];
  // 循环从start到end（包含end）
  for (let i = start; i <= end; i++) {
    arr.push({
      value: i, // 值以字符串形式存储
      label: i.toString()  // 标签以字符串形式存储
    });
  }
  return arr;
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