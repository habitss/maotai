import { mount } from '@vue/test-utils';
import MyComponent from '@/components/MyComponent.vue';

// --story=1055318 --user=孙金伟 22-交接班任务推送 https://www.tapd.cn/63554897/s/1385848 单元测试
//--story=1057422 --user=孙金伟 27-检查项目维护调整，新增糖化、发酵类别 https://www.tapd.cn/63554897/s/1393843 单元测试
//--story=1058614 --user=孙金伟 27-自动触发配置页面开发 https://www.tapd.cn/63554897/s/1394565 单元测试
const mockValidate = jest.fn();
const mockSetModalProps = jest.fn();
const mockSaveOrUpdate = jest.fn();
const mockCloseModal = jest.fn();
const mockScrollToField = jest.fn();
const mockEmit = jest.fn();

// Mock 组件中的函数
jest.mock('@/components/MyComponent.vue', () => {
  return {
    ...jest.requireActual('@/components/MyComponent.vue'),
    validate: mockValidate,
    setModalProps: mockSetModalProps,
    saveOrUpdate: mockSaveOrUpdate,
    closeModal: mockCloseModal,
    scrollToField: mockScrollToField,
    emit: mockEmit
  };
});

describe('handleSubmit function', () => {
  let wrapper;
  let componentInstance;

  beforeEach(() => {
    // 重置所有 mock
    jest.clearAllMocks();
    
    // 创建组件实例
    wrapper = mount(MyComponent);
    componentInstance = wrapper.vm;
  });

  afterEach(() => {
    wrapper.unmount();
  });

  /**
   * 测试用例1: 基本成功提交（无 loopTriggerId 字段）
   * 验证正常流程中各函数的调用顺序和参数
   */
  it('should handle successful form submission without loopTriggerId', async () => {
    // Arrange: 准备测试数据和 mock 返回值
    const formData = { name: 'test', description: 'test description' };
    mockValidate.mockResolvedValue(formData);
    mockSaveOrUpdate.mockResolvedValue({ success: true });

    // Act: 调用被测函数
    await componentInstance.handleSubmit();

    // Assert: 验证各函数调用情况
    // 验证 validate 被调用
    expect(mockValidate).toHaveBeenCalled();
    
    // 验证加载状态设置为 true
    expect(mockSetModalProps).toHaveBeenCalledWith({ confirmLoading: true });
    
    // 验证 saveOrUpdate 被调用且参数正确
    expect(mockSaveOrUpdate).toHaveBeenCalledWith(formData, false);
    
    // 验证弹窗关闭
    expect(mockCloseModal).toHaveBeenCalled();
    
    // 验证 success 事件被触发
    expect(mockEmit).toHaveBeenCalledWith('success');
    
    // 验证最终加载状态设置为 false
    expect(mockSetModalProps).toHaveBeenLastCalledWith({ confirmLoading: false });
  });

  /**
   * 测试用例2: 成功提交且处理 loopTriggerId（有 label）
   * 验证 loopTriggerId 字段的特殊处理逻辑
   */
  it('should handle successful form submission with loopTriggerId having label', async () => {
    // Arrange: 准备包含 loopTriggerId 的测试数据
    const formData = { 
      name: 'test', 
      loopTriggerId: { 
        value: '123', 
        label: 'Trigger Name' 
      } 
    };
    const expectedData = {
      name: 'test',
      loopTriggerId: '123',
      loopTriggerName: 'Trigger Name'
    };
    
    mockValidate.mockResolvedValue(formData);
    mockSaveOrUpdate.mockResolvedValue({ success: true });

    // Act: 调用被测函数
    await componentInstance.handleSubmit();

    // Assert: 验证数据处理和函数调用
    expect(mockValidate).toHaveBeenCalled();
    expect(mockSetModalProps).toHaveBeenCalledWith({ confirmLoading: true });
    
    // 验证 loopTriggerId 被正确处理
    expect(mockSaveOrUpdate).toHaveBeenCalledWith(expectedData, false);
    
    expect(mockCloseModal).toHaveBeenCalled();
    expect(mockEmit).toHaveBeenCalledWith('success');
    expect(mockSetModalProps).toHaveBeenLastCalledWith({ confirmLoading: false });
  });

  /**
   * 测试用例3: 成功提交且处理 loopTriggerId（无 label）
   * 验证 loopTriggerId 字段无 label 时的处理逻辑
   */
  it('should handle successful form submission with loopTriggerId without label', async () => {
    // Arrange: 准备包含 loopTriggerId 但无 label 的测试数据
    const formData = { 
      name: 'test', 
      loopTriggerId: { 
        value: '123' 
        // no label property
      } 
    };
    
    mockValidate.mockResolvedValue(formData);
    mockSaveOrUpdate.mockResolvedValue({ success: true });

    // Act: 调用被测函数
    await componentInstance.handleSubmit();

    // Assert: 验证数据未被修改
    expect(mockValidate).toHaveBeenCalled();
    expect(mockSetModalProps).toHaveBeenCalledWith({ confirmLoading: true });
    
    // 验证 loopTriggerId 保持原样
    expect(mockSaveOrUpdate).toHaveBeenCalledWith(formData, false);
    
    expect(mockCloseModal).toHaveBeenCalled();
    expect(mockEmit).toHaveBeenCalledWith('success');
    expect(mockSetModalProps).toHaveBeenLastCalledWith({ confirmLoading: false });
  });

  /**
   * 测试用例4: 表单验证失败，有 errorFields 并滚动到第一个错误字段
   * 验证错误处理和滚动逻辑
   */
  it('should handle validation error and scroll to first error field', async () => {
    // Arrange: 准备验证错误数据
    const errorFields = [
      { name: 'name', errors: ['Name is required'] },
      { name: 'description', errors: ['Description is required'] }
    ];
    
    mockValidate.mockRejectedValue({ errorFields });

    // Act: 调用被测函数并捕获错误
    try {
      await componentInstance.handleSubmit();
      // 如果没有抛出错误，测试失败
      fail('Expected handleSubmit to throw an error');
    } catch (error) {
      // Assert: 验证错误处理逻辑
      expect(mockValidate).toHaveBeenCalled();
      expect(mockSetModalProps).toHaveBeenCalledWith({ confirmLoading: true });
      
      // 验证滚动到第一个错误字段
      expect(mockScrollToField).toHaveBeenCalledWith('name', { 
        behavior: 'smooth', 
        block: 'center' 
      });
      
      // 验证错误被重新抛出
      expect(error).toEqual(errorFields);
      
      // 验证最终加载状态设置为 false
      expect(mockSetModalProps).toHaveBeenLastCalledWith({ confirmLoading: false });
      
      // 验证成功流程的函数未被调用
      expect(mockSaveOrUpdate).not.toHaveBeenCalled();
      expect(mockCloseModal).not.toHaveBeenCalled();
      expect(mockEmit).not.toHaveBeenCalled();
    }
  });

  /**
   * 测试用例5: 表单验证失败，errorFields 为空数组
   * 验证空错误数组的处理逻辑
   */
  it('should handle validation error with empty errorFields array', async () => {
    // Arrange: 准备空的 errorFields
    const errorFields = [];
    mockValidate.mockRejectedValue({ errorFields });

    // Act: 调用被测函数并捕获错误
    try {
      await componentInstance.handleSubmit();
      fail('Expected handleSubmit to throw an error');
    } catch (error) {
      // Assert: 验证处理逻辑
      expect(mockValidate).toHaveBeenCalled();
      expect(mockSetModalProps).toHaveBeenCalledWith({ confirmLoading: true });
      
      // 验证未调用滚动函数
      expect(mockScrollToField).not.toHaveBeenCalled();
      
      // 验证错误被重新抛出
      expect(error).toEqual(errorFields);
      
      expect(mockSetModalProps).toHaveBeenLastCalledWith({ confirmLoading: false });
      expect(mockSaveOrUpdate).not.toHaveBeenCalled();
      expect(mockCloseModal).not.toHaveBeenCalled();
      expect(mockEmit).not.toHaveBeenCalled();
    }
  });

  /**
   * 测试用例6: 表单验证失败，errorFields[0] 不存在
   * 验证第一个错误字段不存在时的处理逻辑
   */
  it('should handle validation error when first error field does not exist', async () => {
    // Arrange: 准备 null 的第一个错误字段
    const errorFields = [null];
    mockValidate.mockRejectedValue({ errorFields });

    // Act: 调用被测函数并捕获错误
    try {
      await componentInstance.handleSubmit();
      fail('Expected handleSubmit to throw an error');
    } catch (error) {
      // Assert: 验证处理逻辑
      expect(mockValidate).toHaveBeenCalled();
      expect(mockSetModalProps).toHaveBeenCalledWith({ confirmLoading: true });
      
      // 验证未调用滚动函数
      expect(mockScrollToField).not.toHaveBeenCalled();
      
      // 验证错误被重新抛出
      expect(error).toEqual(errorFields);
      
      expect(mockSetModalProps).toHaveBeenLastCalledWith({ confirmLoading: false });
      expect(mockSaveOrUpdate).not.toHaveBeenCalled();
      expect(mockCloseModal).not.toHaveBeenCalled();
      expect(mockEmit).not.toHaveBeenCalled();
    }
  });

  /**
   * 测试用例7: 更新模式下的提交
   * 验证 isUpdate 为 true 时的处理逻辑
   */
  it('should handle successful form submission in update mode', async () => {
    // Arrange: 设置更新模式
    // 这里需要根据实际组件结构来设置 isUpdate 的值
    // 假设可以通过 props 或 data 来设置
    const formData = { id: '1', name: 'updated test' };
    mockValidate.mockResolvedValue(formData);
    mockSaveOrUpdate.mockResolvedValue({ success: true });

    // 如果 isUpdate 是一个 ref，需要在组件中正确设置
    // wrapper.setData({ isUpdate: true }); // 根据实际实现调整
    
    // 或者如果 isUpdate 是通过 props 传递
    // wrapper.setProps({ isUpdate: true });

    // Act: 调用被测函数
    await componentInstance.handleSubmit();

    // Assert: 验证 saveOrUpdate 被调用时第二个参数为 true
    expect(mockValidate).toHaveBeenCalled();
    expect(mockSetModalProps).toHaveBeenCalledWith({ confirmLoading: true });
    expect(mockSaveOrUpdate).toHaveBeenCalledWith(formData, true); // 注意这里应该是 true
    expect(mockCloseModal).toHaveBeenCalled();
    expect(mockEmit).toHaveBeenCalledWith('success');
    expect(mockSetModalProps).toHaveBeenLastCalledWith({ confirmLoading: false });
  });
});