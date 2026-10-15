<template>
  <div class="item-option">
    <template v-for="(tag, index) in tags" :key="tag">
      <a-tooltip v-if="tag.length > 20" :title="tag">
        <a-tag class="tag-space" :closable="true" @close="handleClose(tag)" color="blue">
          {{ `${tag.slice(0, 20)}...` }}
        </a-tag>
      </a-tooltip>
      <a-tag class="tag-space" v-else :closable="true" @close="handleClose(tag)" color="blue">
        {{ tag }}
      </a-tag>
    </template>
    <a-input v-if="inputVisible" class="tag-space" ref="inputRef" v-model:value="inputValue" type="text" size="small"
      :style="{ width: '78px' }" @blur="handleInputConfirm" @keyup.enter="handleInputConfirm" />
    <a-tag class="tag-space" v-else style="background: #fff; border-style: dashed" @click="showInput">
      <plus-outlined />
      增加
    </a-tag>
  </div>
</template>

<script lang="ts" name="mdm-item-option">
import { defineComponent, nextTick, onMounted, reactive, ref, toRefs } from 'vue';
import { PlusOutlined } from '@ant-design/icons-vue';
import { useMessage } from '/@/hooks/web/useMessage';
import { deleteOneItemOption, getItemOptionArr, saveOrUpdateItemOption } from '../MdmItem.api';
import { cloneDeep } from 'lodash-es';


export default defineComponent({
  components: {
    PlusOutlined,
  },
  props: {
    //排除用户id的集合
    baseInfo: {
      type: Object,
      default: {},
    },
  },
  setup(props) {
    const { createMessage } = useMessage();
    let allOptionArray = [];
    const inputRef = ref();
    const state = reactive({
      tags: [],
      inputVisible: false,
      inputValue: '',
    });

    onMounted(() => {
      getItemOptions({ itemId: props.baseInfo.itemId });
    });

    const handleClose = async (removedTag: string) => {
      const findObject = allOptionArray.find(item => item.optionName === removedTag)
      await batchHandleDelete(findObject.id)
      const tags = state.tags.filter(tag => tag !== removedTag);
      state.tags = tags;
    };

    const showInput = () => {
      state.inputVisible = true;
      nextTick(() => {
        inputRef.value.focus();
      });
    };

    const handleInputConfirm = () => {
      const inputValue = state.inputValue;
      let tags = state.tags;

      if (tags.includes(inputValue)) {
        Object.assign(state, {
          tags,
          inputVisible: false,
          inputValue: '',
        });
        return;
      }

      if (inputValue && tags.indexOf(inputValue) === -1) {
        tags = [...tags, inputValue];
      }
      Object.assign(state, {
        tags,
        inputVisible: false,
        inputValue: '',
      });
      submitForm(inputValue);

    };

    /**
     * 提交数据
     */
    async function submitForm(optionStr) {
      if(!optionStr){
        return;
      }
      const submit = cloneDeep(props.baseInfo);
      submit.optionName = optionStr;
      submit.factoryCode = submit.deptCode.substr(0,9);
      await saveOrUpdateItemOption(submit, false);
    }

    /**
 * 获取设备类型PI可选项
 */
    async function getItemOptions(params) {
      params.pageSize = 9999
      params.pageNo = 1
      getItemOptionArr(params).then((res) => {
        if (res.records && res.records.length > 0) {
          state.tags = res.records.map(item => item.optionName)
        }
        allOptionArray = res.records;
      })
    }

    /**
     * 批量删除事件
     */
    async function batchHandleDelete(id) {
      await deleteOneItemOption({ id: id }, () => {
      });
    }


    return {
      ...toRefs(state),
      handleClose,
      showInput,
      handleInputConfirm,
      inputRef,
    };
  },
});
</script>
<style lang="less" scoped>
.item-option {
  .tag-space {
    margin: 5px;
  }
}
</style>
