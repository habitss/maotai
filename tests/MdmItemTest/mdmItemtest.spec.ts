import { shallowMount, mount } from '@vue/test-utils';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import MdmItemModal from '@/views/dataCalculation/mdm/item/components/MdmItemModal.vue';
import { formSchema } from '@/views/dataCalculation/mdm/item/MdmItem.data';

// --story=1055313 --user=孙金伟 23-行动计划优化处理 https://www.tapd.cn/63554897/s/1382848 单元测试
vi.mock('/@/components/Modal', () => ({
  BasicModal: {
    name: 'BasicModal',
    props: ['title', 'width', 'min-height', 'bodyStyle', 'destroyOnClose'],
    emits: ['register', 'ok'],
    template: '<div><slot /></div>'
  },
  useModalInner: () => [
    vi.fn(),
    {
      setModalProps: vi.fn(),
      closeModal: vi.fn()
    }
  ]
}));
//--story=1055311 --user=孙金伟 23-岗位记录、检查清单优化处理 https://www.tapd.cn/63554897/s/1383453 单元测试
vi.mock('/@/components/Form/index', () => ({
  BasicForm: {
    name: 'BasicForm',
    props: ['labelWidth', 'schemas', 'showActionButtonGroup', 'baseColProps'],
    emits: ['register'],
    template: '<div><slot name="selectOption" :model="{id: 1}" /><slot /></div>'
  },
  useForm: () => [
    vi.fn(),
    {
      setProps: vi.fn(),
      resetFields: vi.fn(),
      setFieldsValue: vi.fn(),
      validate: vi.fn().mockResolvedValue(true),
      scrollToField: vi.fn()
    }
  ]
}));

vi.mock('../MdmItem.data', () => ({
  formSchema: [...formSchema]
}));

vi.mock('../MdmItem.api', () => ({
  saveOrUpdate: vi.fn().mockResolvedValue({})
}));
//--story=1055317 --user=孙金伟 22-交接班任务配置 https://www.tapd.cn/63554897/s/1385617 单元测试
describe('MdmItemModal.vue', () => {
  let wrapper;
  const mockData = {
    isUpdate: true,
    showFooter: true,
    record: {
      id: 1,
      factoryCode: 'F1',
      deptCode: 'D1',
      lineCode: 'L1',
      deviceId: 'D1',
      itemName: 'Test Item',
      itemLocation: 'Test Location',
      remark: 'Test Remark'
    }
  };

  beforeEach(() => {
    wrapper = mount(MdmItemModal, {
      props: {
        data: mockData
      }
    });
  });

  it('should render properly', () => {
    expect(wrapper.exists()).toBe(true);
  });

  it('should have correct form schema', () => {
    const form = wrapper.findComponent({ name: 'BasicForm' });
    expect(form.props('schemas')).toEqual(formSchema);
  });

  it('should handle modal registration and data loading', async () => {
    const setFieldsValue = vi.fn();
    const registerModal = vi.fn();
    
    wrapper = mount(MdmItemModal, {
      props: {
        data: mockData
      },
      listeners: {
        register: registerModal
      }
    });
    
    await wrapper.vm.$options.setup();
    
    expect(registerModal).toHaveBeenCalled();
    expect(setFieldsValue).toHaveBeenCalledWith({
      ...mockData.record,
    });
  });

  it('should handle submit', async () => {
    const validate = vi.fn().mockResolvedValue(true);
    const saveOrUpdateMock = vi.fn().mockResolvedValue({});
    
    wrapper = mount(MdmItemModal, {
      props: {
        data: mockData
      }
    });
    
    // Replace the actual saveOrUpdate with our mock
    wrapper.vm.$options.setup().then(() => {
      wrapper.vm.saveOrUpdate = saveOrUpdateMock;
      return wrapper.vm.handleSubmit();
    });
    
    await wrapper.vm.handleSubmit();
    
    expect(validate).toHaveBeenCalled();
    expect(saveOrUpdateMock).toHaveBeenCalledWith({
      ...mockData.record,
    });
  });

  it('should emit success event after submit', async () => {
    const emitSuccess = vi.fn();
    
    wrapper = mount(MdmItemModal, {
      props: {
        data: mockData
      },
      listeners: {
        success: emitSuccess
      }
    });
    
    await wrapper.vm.handleSubmit();
    
    expect(emitSuccess).toHaveBeenCalled();
  });
});