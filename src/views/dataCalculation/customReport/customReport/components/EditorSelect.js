import { createApp, h, ref } from 'vue';
import { Select } from 'ant-design-vue';
import { getDictItemList } from '../index.api'

export default class SelectEditor {
  //根节点元素
  root = null;
  //挂载组件元素
  element = null;
  //VTable实例所处的容器元素
  container = null;
  //下拉选中值
  currentValue = null;
  //下拉选中文本
  currentText = null;

  //构造函数
  constructor(cellComponentsList, isVerticalHeade) {
    this.cellComponentsList = cellComponentsList;
    this.isVerticalHeade = isVerticalHeade;
  }
  // 单元格进入编辑状态时调用,vtable自带方法
  async onStart(editorContext) {
    await this.removeSelectedElement();
    const that = this;
    const { container, referencePosition, value, endEdit } = editorContext;
    //重置数据
    this.onRest();
    //初始化当前值为编辑器传入值
    this.currentText = value;

    //获取字典缓存数据
    let dictData = []
    if(this.cellComponentsList){
      //查找对应组件项
      let foundObject = {};
      
      if(this.isVerticalHeade){
        foundObject = this.cellComponentsList.find(obj => obj.row == editorContext.row);
      }else{
        foundObject = this.cellComponentsList.find(obj => obj.col == editorContext.col && obj.row == editorContext.row);
      }

      if(!foundObject || !foundObject.dictType){
        return;
      }
      //获取字典数据
      const res = await getDictItemList({pageSize: 999, groupCode: foundObject.dictType});
      dictData = res.records;
      //根据中文获取key
      const foundDict = dictData.find(obj => obj.itemKey == value)??'';
      this.currentValue = foundDict.itemVal;
    }

    const elementArr = document.querySelectorAll('.vtable-editor-select');
    if(elementArr && elementArr.length>0){
      return;
    }
    //创建外部容器用于定位
    const divContainer = document.createElement('div');
    divContainer.style.position = 'absolute';
    divContainer.style.width = '100%';
    divContainer.style.boxSizing = 'border-box';
    divContainer.style.padding = '4px';
    divContainer.style.top = `${referencePosition.rect.top}px`;
    divContainer.style.left = `${referencePosition.rect.left}px`;
    divContainer.style.width = `${referencePosition.rect.width}px`;
    divContainer.style.height = `${referencePosition.rect.height}px`;
    divContainer.className = 'vtable-editor-select';
    container.appendChild(divContainer);
    
    //直接挂载会把外部容器设置的样式全都取消掉，因此创建内部容器用于挂载组件
    const divInner = document.createElement('div');
    //将内部容器添加到外部容器中
    divContainer.appendChild(divInner);
    //创建组件
    const newVue = createApp({
      data() {
        return {
          selectValue: value,
        };
      },
      render() {
        return h(
          //挂载组件
          Select, 
          {
            getPopupContainer: () => divContainer,
            value: this.selectValue,
            defaultValue: this.selectValue,
            fieldNames: {label: 'itemKey', value: 'itemVal'},
            options: dictData,
            allowClear: true,
            filterOption: function(input, option) {
              return option.itemKey.toLowerCase().indexOf(input.toLowerCase()) >= 0;
            },
            showSearch: true,
            onChange: (value, option) =>{
              this.selectValue = value;
              that.currentValue = value;
              that.currentText = option.itemKey;
            },
            style: {
              width: '100%'
            },
          },
        );
      }
    });
    //创建、挂载组件
    const app = newVue;
    await app.mount(divInner);
    this.root = app;
    this.element = divContainer;
    this.container = container;
  }

  // 单元格退出编辑状态时调用,vtable自带方法
  onEnd() {
    if (this.root) {
      this.root.unmount();
      this.root = null;
    }
    if (this.element && this.container) {
      this.container.removeChild(this.element);
      this.element = null;
    }
  }

  removeSelectedElement() {
    const elementsToRemove = document.querySelectorAll('.vtable-editor-select');
    // 遍历找到的元素，并逐个从DOM中移除
    elementsToRemove.forEach(el => {
      if (el && el.parentNode) {
        el.parentNode.removeChild(el);
      }
    });
  }

  // 获取编辑器当前值,将在 `onEnd` 调用后调用,vtable自带方法
  getValue() {
    if(!this.currentValue){
      return '';
    }
    return {value: this.currentValue, text: this.currentText};
  }

  /**
   * 如果提供了此函数，VTable 将会在用户点击其他地方时调用此函数。
   * 如果此函数返回了一个假值，VTable 将会调用 `onEnd` 并退出编辑状态。
   * 如果未定义此函数或此函数返回了一个真值， VTable 将不会做任何事。
   * 这意味着，你需要手动调用 `onStart` 中提供的 `endEdit` 来结束编辑模式。
   */
  isEditorElement(target) {
    if(!this.element){
      return false
    }
    //判断是否在元素内
    return this.element.contains(target);
  }

  setCellComponentsList(data, isVerticalHeade){
    this.cellComponentsList = data;
    this.isVerticalHeade = isVerticalHeade;
  }

  //重置所有参数
  onRest(){
    this.root = null;
    this.element = null;
    this.container = null;
    this.currentValue = null;
    this.currentText = null;
  }
}