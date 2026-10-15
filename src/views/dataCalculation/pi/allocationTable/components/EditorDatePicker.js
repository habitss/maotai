import { createApp, h, ref } from 'vue';
import { DatePicker } from 'ant-design-vue';
import dayjs from 'dayjs';
import locasdale from 'ant-design-vue/es/date-picker/locale/zh_CN';

export default class EditorDatePicker {
  //根节点元素
  root = null;
  //挂载组件元素
  element = null;
  //VTable实例所处的容器元素
  container = null;
  //返回值
  currentValue = null;
  //是否显示时间
  showTimeFlag = false;

  //构造函数
  constructor(cellComponentsList) {
    this.cellComponentsList = cellComponentsList;
  }
  
  // 单元格进入编辑状态时调用,vtable自带方法
  async onStart(editorContext) {
    const that = this;
    const { container, referencePosition, value, endEdit } = editorContext;

    //初始化当前值为编辑器传入值
    this.currentValue = value;
    // if(this.cellComponentsList){
    //   const foundObject = this.cellComponentsList.find(obj => obj.col == editorContext.col && obj.row == editorContext.row);
    //   this.showTimeFlag = foundObject.componentType == 'dateTimePicker' ? true : false;
    // }
    if(this.cellComponentsList && this.cellComponentsList==='date'){
      this.showTimeFlag = false;
    }else{
      this.showTimeFlag = true;
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
    container.appendChild(divContainer);
    
    //直接挂载会把外部容器设置的样式全都取消掉，因此创建内部容器用于挂载组件
    const divInner = document.createElement('div');
    //将内部容器添加到外部容器中
    divContainer.appendChild(divInner);
    //创建组件
    const newVue = createApp({
      data() {
        return {
          selectValue: value ? dayjs(value) : null,
        };
      },
      render() {
        return h(
          //挂载组件
          DatePicker, 
          {
            getPopupContainer: () => divContainer,
            value: this.selectValue,
            locale: locasdale,
            placeholder: '请选择',
            onChange: (dateValue, stringValue) => {
              this.selectValue = dateValue;
              that.currentValue = stringValue;
            },
            showTime: that.showTimeFlag,
            style: {
              width: '100%'
            },
          },
        );
      }
    });
    //创建、挂载组件
    const app = newVue;
    app.mount(divInner);
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

  // 获取编辑器当前值,将在 `onEnd` 调用后调用,vtable自带方法
  getValue() {
    return this.currentValue;
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

  setCellComponentsList(data){
    this.cellComponentsList = data;
  }
}