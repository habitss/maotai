import { mount } from '@vue/test-utils';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { nextTick } from 'vue';
import ActionPlanTrackForm from './ActionPlanTrackForm.vue';

// --story=1055311 --user=孙金伟 23-岗位记录、检查清单优化处理 https://www.tapd.cn/63554897/s/1383214 单元测试
vi.mock('dayjs', () => {
  return {
    default: () => ({
      format: (format: string) => '2023-01-01',
      add: () => ({
        format: (format: string) => '2023-01-02'
      }),
      endOf: () => ({
        diff: () => 1
      })
    })
  };
});

// Mock uuid
vi.mock('/@/utils/uuid', () => {
  return {
    buildUUID: () => 'test-uuid'
  };
});

// --story=1055311 --user=孙金伟 23-岗位记录、检查清单优化处理 https://www.tapd.cn/63554897/s/1383469 单元测试
const mockUserInfo = {
  id: 'user1',
  orgCode: 'org1',
  realname: 'Test User'
};

vi.mock('/@/store/modules/user', () => {
  return {
    useUserStore: () => ({
      userInfo: mockUserInfo
    })
  };
});

// Mock useMessage
vi.mock('/@/hooks/web/useMessage', () => {
  return {
    useMessage: () => ({
      createMessage: {
        error: vi.fn()
      }
    })
  };
});

// Mock API functions
vi.mock('./ActionPlanTrack.api', () => {
  return {
    addBatch: vi.fn().mockResolvedValue({}),
    departListAll: vi.fn().mockResolvedValue([]),
    deleteOne: vi.fn().mockImplementation((params, callback) => callback())
  };
});

vi.mock('/@/views/dataCalculation/kpi/info/KpiInfo.api', () => {
  return {
    list: vi.fn().mockResolvedValue({ records: [] })
  };
});

vi.mock('@/api/common/api', () => {
  return {
    getDictItems: vi.fn().mockResolvedValue([]),
    queryTreeList: vi.fn().mockResolvedValue([])
  };
});

vi.mock('/@/views/dataCalculation/actionPlan/problemType/ActionPlanProblemType.api', () => {
  return {
    userList: vi.fn().mockResolvedValue([]),
    queryAllTreeData: vi.fn().mockResolvedValue([])
  };
});

describe('ActionPlanTrackForm.vue', () => {
  let wrapper: any;

  beforeEach(() => {
    wrapper = mount(ActionPlanTrackForm, {
      props: {
        initRecord: true,
        isShowTopForm: true,
        isShowSubmitBtn: true
      },
      global: {
        stubs: {
          BasicTable: true,
          TableAction: true,
          JDictSelectTag: true,
          ApiTreeSelect: true,
          'a-button': true,
          'a-row': true,
          'a-col': true,
          'a-date-picker': true,
          'a-tree-select': true,
          'a-textarea': true,
          'a-select': true,
          'a-switch': true
        }
      }
    });
  });

  it('renders without errors', () => {
    expect(wrapper.exists()).toBe(true);
  });

  it('initializes with default form data', async () => {
    await nextTick();
    const vm = wrapper.vm as any;
    expect(vm.formData.raiseDate).toBe('2023-01-01');
    expect(vm.formData.createDept).toBe('org1');
  });

  it('adds new record when handleAdd is called', async () => {
    await nextTick();
    const vm = wrapper.vm as any;
    
    const initialLength = vm.getDataSource().length;
    await vm.handleAdd();
    const newLength = vm.getDataSource().length;
    
    expect(newLength).toBe(initialLength + 1);
  });

  it('validates required fields', async () => {
    await nextTick();
    const vm = wrapper.vm as any;
    
    // Test with empty data
    const result = vm.validateData();
    expect(result).toBe(false);
  });

  it('validates minimum length for description fields', async () => {
    await nextTick();
    const vm = wrapper.vm as any;
    
    // Test with short description
    const testData = {
      problemDescription: 'short',
      actionTitle: 'short'
    };
    
    const result = vm.validRule(testData);
    expect(result).toBe(false);
  });

  it('calculates remaining days correctly', () => {
    const vm = wrapper.vm as any;
    const result = vm.calcDay('2023-01-05', '2023-01-01');
    expect(result).toBe(1);
  });

  it('processes form data correctly', () => {
    const vm = wrapper.vm as any;
    const testData = {
      raiseDate: '2023-01-01',
      problemTypeId: 'type1',
      problemDescription: 'Test problem description with enough characters',
      pkiInfo: 'kpi1',
      importanceEvaluation: 'high'
    };
    
    vm.processinFormgData(testData);
    expect(vm.formData.raiseDate).toBe('2023-01-01');
    expect(vm.formData.problemTypeId).toBe('type1');
  });

  it('emits success event on successful submission', async () => {
    await nextTick();
    const vm = wrapper.vm as any;
    
    // Add a record first
    await vm.handleAdd();
    
    // Mock validation to pass
    vm.validateData = vi.fn().mockReturnValue(true);
    vm.getDataSource = vi.fn().mockReturnValue([{ id: 'temp_1' }]);
    
    await vm.handleSubmit();
    expect(wrapper.emitted()).toHaveProperty('success');
  });

  it('shows submit button by default', () => {
    const submitButton = wrapper.find('a-button[type="primary"]');
    expect(submitButton.exists()).toBe(true);
  });

  it('hides submit button when isShowSubmitBtn is false', async () => {
    const newWrapper = mount(ActionPlanTrackForm, {
      props: {
        initRecord: true,
        isShowTopForm: true,
        isShowSubmitBtn: false
      },
      global: {
        stubs: {
          BasicTable: true,
          TableAction: true,
          JDictSelectTag: true,
          ApiTreeSelect: true,
          'a-button': true,
          'a-row': true,
          'a-col': true,
          'a-date-picker': true,
          'a-tree-select': true,
          'a-textarea': true,
          'a-select': true,
          'a-switch': true
        }
      }
    });
    
    const submitButton = newWrapper.find('a-button[type="primary"]');
    expect(submitButton.exists()).toBe(false);
  });
});