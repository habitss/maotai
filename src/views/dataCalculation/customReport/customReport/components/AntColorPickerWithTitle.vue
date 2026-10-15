<template>
  <div class="ant-color-picker-container">
    <!-- 标题区域 -->
    <div class="ant-color-picker-header">
      <span class="ant-color-picker-title">{{ title }}</span>
      <span v-if="required" class="ant-color-picker-required">*</span>
    </div>
    
    <!-- 颜色选择器主体 -->
    <div class="ant-color-picker-body">
      <a-input-group compact>
        <!-- 颜色选择按钮 -->
        <input
          type="color"
          v-model="internalColorValue"
          @change="handleColorInputChange"
          class="ant-color-picker-trigger"
          :disabled="disabled"
        />
        
        <!-- 手动输入框 -->
        <a-input
          v-model:value="colorText"
          @change="handleTextInputChange"
          placeholder="#RRGGBB"
          :maxLength="7"
          style="width: 120px"
          :disabled="disabled"
        />
        
        <!-- 颜色预览 -->
        <div 
          class="ant-color-preview" 
          :style="{ backgroundColor: internalColorValue }"
          :title="internalColorValue"
        ></div>
      </a-input-group>
      
      <!-- 错误提示 -->
      <div v-if="errorMessage" class="ant-color-error">
        <a-alert :message="errorMessage" type="error" show-icon banner />
      </div>
      
      <!-- 预设颜色 -->
      <div class="ant-color-presets" v-if="presetColors.length > 0 && showPresets">
        <div
          v-for="color in presetColors"
          :key="color"
          class="ant-color-preset"
          :style="{ backgroundColor: color }"
          @click="handlePresetSelect(color)"
          :title="color"
        >
        </div>
      </div>
    </div>
    
    <!-- 描述信息 -->
    <div class="ant-color-picker-description" v-if="description">
      {{ description }}
    </div>
  </div>
</template>

<script>
import { ref, watch, computed } from 'vue';

export default {
  name: 'AntColorPickerWithTitle',
  props: {
    modelValue: {
      type: String,
      default: '#1890ff'
    },
    title: {
      type: String,
      default: '颜色选择'
    },
    description: {
      type: String,
      default: ''
    },
    required: {
      type: Boolean,
      default: false
    },
    disabled: {
      type: Boolean,
      default: false
    },
    showPresets: {
      type: Boolean,
      default: true
    },
    presets: {
      type: Array,
      default: () => [
        '#1890ff', '#096dd9', '#52c41a', '#13c2c2',
        '#722ed1', '#eb2f96', '#f5222d', '#fa8c16',
        '#fadb14', '#000000', '#008080', '#ffffff'
      ]
    }
  },
  emits: ['update:modelValue', 'change'],
  setup(props, { emit }) {
    const internalColorValue = ref(props.modelValue);
    const colorText = ref(props.modelValue);
    const errorMessage = ref('');
    
    // 监听外部值变化
    watch(() => props.modelValue, (newVal) => {
      if (isValidColor(newVal)) {
        internalColorValue.value = normalizeColor(newVal);
        colorText.value = normalizeColor(newVal);
        errorMessage.value = '';
      }
    });
    
    // 颜色输入变化处理
    const handleColorInputChange = (e) => {
      const newColor = e.target.value;
      internalColorValue.value = newColor;
      colorText.value = newColor;
      emitColorChange(newColor);
    };
    
    // 文本输入变化处理
    const handleTextInputChange = (e) => {
      const inputValue = e.target.value;
      if (isValidColor(inputValue)) {
        const normalizedColor = normalizeColor(inputValue);
        internalColorValue.value = normalizedColor;
        colorText.value = normalizedColor;
        errorMessage.value = '';
        emitColorChange(normalizedColor);
      } else {
        errorMessage.value = '请输入有效的十六进制颜色值(如 #RRGGBB)';
      }
    };
    
    // 预设颜色选择
    const handlePresetSelect = (color) => {
      internalColorValue.value = color;
      colorText.value = color;
      emitColorChange(color);
    };
    
    // 统一触发变化事件
    const emitColorChange = (color) => {
      emit('update:modelValue', color);
      emit('change', color);
    };
    
    // 验证颜色格式
    const isValidColor = (color) => {
      return /^#([0-9A-F]{3}){1,2}$/i.test(color);
    };
    
    // 标准化颜色格式(如 #RGB => #RRGGBB)
    const normalizeColor = (color) => {
      if (!color.startsWith('#')) return `#${color}`;
      if (color.length === 4) { // #RGB => #RRGGBB
        return `#${color[1]}${color[1]}${color[2]}${color[2]}${color[3]}${color[3]}`;
      }
      return color;
    };
    
    return {
      internalColorValue,
      colorText,
      errorMessage,
      presetColors: props.presets,
      handleColorInputChange,
      handleTextInputChange,
      handlePresetSelect
    };
  }
};
</script>

<style>
.ant-color-picker-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
  max-width: 330px;
}

.ant-color-picker-header {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: rgba(0, 0, 0, 0.85);
  line-height: 1.5715;
}

.ant-color-picker-title {
  font-weight: 500;
}

.ant-color-picker-required {
  color: #ff4d4f;
  margin-left: 4px;
}

.ant-color-picker-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.ant-color-picker-trigger {
  width: 32px;
  height: 32px;
  padding: 0;
  border: 1px solid #d9d9d9;
  border-radius: 2px;
  cursor: pointer;
  vertical-align: middle;
}

.ant-color-picker-trigger:hover {
  border-color: #40a9ff;
}

.ant-color-picker-trigger:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.ant-color-preview {
  width: 32px;
  height: 32px;
  border: 1px solid #d9d9d9;
  border-radius: 2px;
}

.ant-color-presets {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.ant-color-preset {
  width: 24px;
  height: 24px;
  border-radius: 2px;
  cursor: pointer;
  border: 1px solid #f0f0f0;
  transition: transform 0.2s;
}

.ant-color-preset:hover {
  transform: scale(1.1);
}

.ant-color-error {
  margin-top: 4px;
}

.ant-color-picker-description {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
  line-height: 1.5715;
}
</style>