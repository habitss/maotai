<template>
  <div style="padding: 0px 10px">
    <div class="bar-name">工具栏：</div>
    <div class="tool-box">
      <draggable
        v-model="toolList"
        tag="div"
        item-key="id"
        :sort="false"
        :disabled="toolsDisabled"
        class="tool-content"
        v-bind="dragOptions"
        @start="onDragStart1"
        @end="onDragEnd1"
      >
        <template #item="{ element }">
          <div :key="element.order" class="tool-card" :title="element.type">
            <div class="tool-txt" v-if="element.type === 1">
              <a-input v-model:value="element.tagname" size="small" placeholder="常量" style="width: 120px" allowClear @change="handleInputChange" />
            </div>
            <div class="tool-txt" v-else>{{ element.tagname }}</div>
          </div>
        </template>
      </draggable>
    </div>
  </div>
  <div style="padding: 0px 10px">
    <div class="bar-name">公式表达式：</div>
    <div class="logic-main">
      <draggable
        v-model="expressionTagList"
        item-key="id"
        tag="div"
        v-bind="dragOptions"
        :disabled="expressDisabled"
        class="logic-content"
        @start="onDragStart2"
        @end="onDragEnd2"
        @add="onAdd"
        @remove="onRemove"
      >
        <template #item="{ element, index }">
          <div
            :id="element.id"
            :key="index"
            class="cond-card"
            @dblclick="editTag(index)"
            @mouseover="moveOverCondCard(index)"
            @mouseleave="moveLeaveCondCard"
          >
            <template v-if="element.type === 0">
              <div class="tool-txt">{{ element.tagname }}</div>
            </template>
            <template v-else-if="element.type === 1">
              <div>{{ element.tagname }}</div>
            </template>
            <template v-else>
              <div>[{{ element.type + ':' + element.tagid }}]</div>
              <div>{{ element.tagname }}</div>
            </template>
            <div class="close-btn" v-if="moveInx == index" @click="removeExpObj(index)">X</div>
          </div>
        </template>
      </draggable>
    </div>
  </div>

  <!-- 点位选择框 -->
  <a-modal v-model:open="selTagVisible" title="请选择点位" @ok="handleOk" @cancel="handleCancel">
    <div style="padding: 20px">
      <span style="margin-right: 10px; display: inline-block; width: 70px">{{ selTagTitle }}</span>
      <a-select
        :placeholder="'请选择' + selTagTitle"
        v-model:value="selectedValue"
        style="width: 80%"
        :options="currentOptions"
        show-search
        :filterOption="(input, option) => (option?.label ?? '').toLowerCase().includes(input.toLowerCase())"
      />
      <div style="color: red; padding-left: 80px" v-if="requireFlag">请选择点位</div>
    </div>
  </a-modal>
</template>

<script lang="ts" setup>
  import { ref, computed, watch, defineProps } from 'vue';
  import draggable from 'vuedraggable';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { SelectProps } from 'ant-design-vue';

  // 当前类型
  const currentType = ref<string | null>(null);
  const selTagVisible = ref(false);
  const selectedValue = ref<string | undefined>();
  // 各点位数据源
  const tagDataLists = ref<Record<string, any[]>>({});
  const selTagTitle = ref<string>('选择点位');
  const requireFlag = ref<boolean>(false);

  const props = defineProps({
    tagTypeList: { type: Array, default: () => [] },
    expressValue: { type: String, default: '' },
  });

  const logicNewIndex = ref(0);
  //工具栏禁用状态
  const toolsDisabled = ref(false);
  //表达式区禁用状态
  const expressDisabled = ref(false);
  //expressionTagList 的备份列表，用于监听，其实可以不用，直接用表达式列表监听
  const tagListX = ref<any[]>([]);
  const id2TagMap: Record<string, any> = {};

  //表达式所有元素数组
  const expressionTagList = ref<any[]>([]);
  //鼠标移动到表达式元素的下标
  const moveInx = ref<number>(-1);

  // 计算属性
  const dragOptions = computed(() => {
    return {
      animation: 0,
      group: 'description',
      ghostClass: 'ghost',
    };
  });

  const { createConfirm } = useMessage();

  // 定义 emits
  const emits = defineEmits(['onChange']);

  //工具栏工具对象列表
  const toolList = ref([
    { tagname: '+', type: 0, order: 12, desc: '加' },
    { tagname: '-', type: 0, order: 13, desc: '减' },
    { tagname: 'x', type: 0, order: 14, desc: '乘' },
    { tagname: '/', type: 0, order: 15, desc: '除' },
    { tagname: '(', type: 0, order: 16, desc: '左括号' },
    { tagname: ')', type: 0, order: 17, desc: '右括号' },
    { tagname: '', type: 1, order: 18, desc: '常量' },
  ]);
  //初始化传入的各类点位列表
  const initPropTagTypeList = (tagTypeList) => {
    if (tagTypeList.length > 0) {
      console.log(tagTypeList, '==============');
      const newTagList = tagTypeList.map((item, inx) => {
        return { tagname: item.tagKindName, type: item.tagKindFlag, tagid: 0, order: inx };
      });
      newTagList.forEach((newItem) => {
        if (!toolList.value.some((item) => item.tagname === newItem.tagname)) {
          toolList.value.unshift(newItem);
        }
      });
      //构建点位下拉框的数据源同时构建一个MAP给反编译公式用
      tagTypeList.forEach((item) => {
        tagDataLists.value[item.tagKindFlag] = item.tagList;
        item.tagList.forEach((elem: any) => {
          id2TagMap['[' + item.tagKindFlag + ':' + elem.tagId + ']'] = elem;
        });
      });
    }
  };

  watch(
    () => props.tagTypeList,
    (newVal) => {
      console.log(newVal); // 监听变化
      initPropTagTypeList(newVal);
    },
    { immediate: true }
  );

  //反编译公式
  const parseExpress = (logicExpress) => {
    // 反编译
    const tagList: any[] = [];
    while (logicExpress.length > 0) {
      if (logicExpress.startsWith('(')) {
        tagList.push({ tagname: '(', type: 0 });
        logicExpress = logicExpress.substring(1);
      } else if (logicExpress.startsWith(')')) {
        tagList.push({ tagname: ')', type: 0 });
        logicExpress = logicExpress.substring(1);
      } else if (logicExpress.startsWith('+')) {
        tagList.push({ tagname: '+', type: 0 });
        logicExpress = logicExpress.substring(1);
      } else if (logicExpress.startsWith('-')) {
        tagList.push({ tagname: '-', type: 0 });
        logicExpress = logicExpress.substring(1);
      } else if (logicExpress.startsWith('x')) {
        tagList.push({ tagname: 'x', type: 0 });
        logicExpress = logicExpress.substring(1);
      } else if (logicExpress.startsWith('/')) {
        tagList.push({ tagname: '/', type: 0 });
        logicExpress = logicExpress.substring(1);
      } else if (/^\d/.test(logicExpress)) {
        //截取数字
        const numStr = logicExpress.match(/^\d+\.?\d*/)?.[0] || '';
        tagList.push({ tagname: numStr, type: 1 });
        logicExpress = logicExpress.slice(numStr.length);
      } else if (logicExpress.startsWith('[')) {
        const idKey = logicExpress.substring(0, logicExpress.indexOf(']') + 1);
        const idKeyArr = idKey.split(':');
        tagList.push({
          tagname: id2TagMap[idKey]?.tagName ?? idKey,
          tagid: id2TagMap[idKey]?.tagId ?? idKey + '_id',
          type: idKeyArr[0].substring(1),
        });
        logicExpress = logicExpress.substring(idKey.length);
      } else {
        console.warn('发现运算符号之外的内容...');
        break;
      }
    }
    expressionTagList.value = [...tagList];
  };

  watch(
    () => props.expressValue,
    (newVal) => {
      console.log(newVal, '=-=-=-='); // 监听变化
      if (newVal && id2TagMap) {
        parseExpress(newVal);
      } else {
        expressionTagList.value = [];
        tagListX.value = [];
      }
    },
    { immediate: true }
  );

  // 监听 tagListX,当公式变化时向上触发 onChange 事件并传出公式表达式
  watch(
    tagListX,
    (newVal) => {
      if (newVal && newVal.length > 0) {
        //公式原型
        const expressVal = expressionTagList.value
          .map((item) => {
            if (item.type === 0 || item.type === 1) {
              return item.tagname;
            } else {
              return '[' + item.type + ':' + item.tagid + ']';
            }
          })
          .join('');
        //公式描述
        const expressDesc = expressionTagList.value
          .map((item) => {
            if (item.type === 0 || item.type === 1) {
              return item.tagname;
            } else {
              return '[' + item.type + ':' + item.tagid + item.tagname + ']';
            }
          })
          .join('');
        emits('onChange', expressVal, expressDesc);
      }
    },
    { deep: true }
  );

  //限制Input框工具只能输数字
  const handleInputChange = (e) => {
    const num = parseFloat(e.target.value);
    if (isNaN(num)) {
      const numTagObj = toolList.value.find((item) => item.type === 1);
      if (numTagObj) {
        e.target.value = '';
        numTagObj.tagname = '';
      }
    }
  };

  // 当前 options 列表
  const currentOptions = computed<SelectProps['options']>(() => {
    if (currentType.value && tagDataLists.value[currentType.value]) {
      return tagDataLists.value[currentType.value].map((item) => ({
        label: '[' + item.tagId + ']' + item.tagName,
        value: currentType.value + ':' + item.tagId, //M:123
      }));
    }
    return [];
  });

  // 打开点位选择框
  function openTagSelector(type: string, kindName: string) {
    console.log(' select tagFlag = ', type, ' optionData = ', tagDataLists.value[type]);
    selTagTitle.value = kindName;
    currentType.value = type;
    selectedValue.value = undefined;
    selTagVisible.value = true;
  }

  // 确认选择
  function handleOk() {
    if (currentType.value && selectedValue.value) {
      console.log(selectedValue.value, '================');
      const selTagVals = selectedValue.value.split(':');
      const selTagObj = tagDataLists.value[selTagVals[0]].find((item) => item.tagId === selTagVals[1]);
      const newExpTag = { type: selTagVals[0], tagid: selTagObj.tagId, tagname: selTagObj.tagName };
      expressionTagList.value.splice(logicNewIndex.value, 1, { ...newExpTag });
      tagListX.value = [...expressionTagList.value];
      selTagVisible.value = false;
      requireFlag.value = false;
    } else {
      requireFlag.value = true;
    }
  }

  // 取消
  function handleCancel() {
    selTagVisible.value = false;
    requireFlag.value = false;
    //没选择点位则删除
    if (!expressionTagList.value[logicNewIndex.value].tagid) {
      expressionTagList.value.splice(logicNewIndex.value, 1);
      tagListX.value = [...expressionTagList.value];
    }
  }

  const onDragStart1 = (e: any) => {
    const toolTag = toolList.value[e.oldIndex];
    //零或空常量不允许拖入表达式
    if (toolTag.type === 1 && (!toolTag.tagname || Number(toolTag.tagname) == 0)) {
      expressDisabled.value = true;
    } else {
      expressDisabled.value = false;
    }
  };

  const onDragEnd1 = (e: any) => {
    console.log(e.oldIndex);
  };

  const onDragStart2 = (e: any) => {
    console.log(e.oldIndex);
    toolsDisabled.value = true;
  };

  const onDragEnd2 = (e: any) => {
    console.log(e.oldIndex);
    tagListX.value = [...expressionTagList.value];
    toolsDisabled.value = false;
  };

  const onAdd = (e: any) => {
    logicNewIndex.value = e.newIndex;
    const addTag = expressionTagList.value[logicNewIndex.value];
    toolList.value.splice(e.oldIndex, 0, { ...addTag });
    const ttype = addTag.type;
    const tname = addTag.tagname;
    tagListX.value = [...expressionTagList.value];
    if (ttype !== 0 && ttype !== 1) {
      //添加的是点位，弹框选择点位
      openTagSelector(ttype, tname);
    } else if (ttype === 1) {
      const numTagObj = toolList.value.find((item) => item.type === 1);
      if (numTagObj) {
        e.target.value = '';
        numTagObj.tagname = '';
      }
    }
  };

  const onRemove = (e: any) => {
    console.log(e);
    tagListX.value = [...expressionTagList.value];
  };

  const editTag = (e: any) => {
    console.log(e);
  };
  //鼠标移入表达式元素时显示移除图标
  const moveOverCondCard = (inx: number) => {
    moveInx.value = inx;
  };
  //鼠标移出表达式元素时隐藏移除图标
  const moveLeaveCondCard = () => {
    moveInx.value = -1;
  };
  //删除指定的表达式元素
  const removeExpObj = (inx: number) => {
    createConfirm({
      title: '系统提示',
      content: `确定要删除吗？`,
      iconType: 'warning',
      onOk: () => {
        expressionTagList.value = expressionTagList.value.filter((_, index) => index !== inx);
        tagListX.value = [...expressionTagList.value];
      },
    });
  };
</script>

<style lang="less" scoped>
  .main-content {
    padding: 0px 10px;
  }
  .bar-name {
    font-weight: bold;
    margin-bottom: 8px;
    font-size: 16px;
    color: #333;
  }

  .tool-box,
  .logic-main {
    margin-bottom: 16px;
  }

  .tool-content,
  .logic-content {
    display: flex;
    flex-wrap: wrap;
    min-height: 50px;
    background: #fafafa;
    border: 1px dashed #d9d9d9;
    padding: 5px;
    border-radius: 4px;
  }

  .tool-card,
  .cond-card {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    min-width: 60px;
    min-height: 60px;
    margin: 6px;
    padding: 8px;
    background-color: #ffffff;
    border: 1px solid #d9d9d9;
    border-radius: 8px;
    cursor: grab;
    transition: all 0.3s;
  }

  .tool-card:hover,
  .cond-card:hover {
    border-color: #409eff;
    background-color: #ecf5ff;
  }

  .tool-content {
    .tool-txt {
      font-size: 14px;
      color: #666;
      font-weight: bold;
    }
    .tool-card {
      min-width: 40px;
      min-height: 40px;
      margin: 4px;
      padding: 1px 8px;
      border-radius: 4px;
    }
  }

  .logic-content {
    .cond-card {
      position: relative;
      font-size: 14px;
      text-align: left;
      align-items: flex-start;
      color: #666;
      min-width: 40px;
      min-height: 40px;
      margin: 4px;
      padding: 6px;
      .tool-txt {
        font-weight: bold;
        text-align: center;
        font-size: 18px;
        margin: 3px;
        padding: 6px;
      }
      .close-btn {
        position: absolute;
        right: -5px;
        top: -5px;
        color: #d92622;
        border-radius: 100%;
        font-weight: bold;
        border: 1px solid #d92622;
        font-size: 12px;
        line-height: 14px;
        padding: 1px 3px;
        cursor: pointer;
        background-color: #fff;
      }
    }
  }

  .ghost {
    opacity: 0.4;
  }
</style>
