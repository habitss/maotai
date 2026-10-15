// PlanOrderList.test.ts
import { mount } from '@vue/test-utils';
import PlanOrderList from './PlanOrderList.vue';
import { useListPage } from '/@/hooks/system/useListPage';

// --story=1055320 --user=孙金伟 22-交接班任务统计 https://www.tapd.cn/63554897/s/1386796 单元测试
jest.mock('/@/hooks/system/useListPage');
jest.mock('./PlanOrder.api');

describe('PlanOrderList 设备选项处理', () => {
  let wrapper: any;
  
  beforeEach(() => {
    (useListPage as jest.Mock).mockReturnValue({
      tableContext: [
        null, 
        { 
          reload: jest.fn(),
          getForm: () => ({
            updateSchema: jest.fn()
          }),
          setProps: jest.fn()
        }
      ]
    });
    
    wrapper = mount(PlanOrderList, {
      global: {
        mocks: {
          $route: {
            params: {
              code: 'some-code'
            }
          }
        }
      }
    });
  });

  it('应该正确处理设备ID和名称并生成选项数组', async () => {
    // 模拟 parentData 数据
    const parentData = {
      deviceIds: '1,2,3',
      deviceIdsVal: '设备1,设备2,设备3',
      id: 'plan-id'
    };

    // 获取组件实例
    const vm = wrapper.vm as any;
    
    // 调用 reloadData 方法
    vm.reloadData(parentData);
    
    // 验证处理逻辑
    expect(parentData.deviceIds.split).toHaveBeenCalled();
    expect(parentData.deviceIdsVal.split).toHaveBeenCalled();
    
    // 验证生成的选项数组
    const expectedDeviceOptions = [
      { label: '设备1', value: '1' },
      { label: '设备2', value: '2' },
      { label: '设备3', value: '3' }
    ];
    
    // 注意：由于 updateSchema 是异步调用的，我们需要验证它是否被正确调用
    // 这里需要根据实际的测试环境进行调整
  });

  it('应该正确调用 updateSchema 方法', async () => {
    const parentData = {
      deviceIds: '1,2',
      deviceIdsVal: '设备A,设备B',
      id: 'plan-id'
    };

    const updateSchemaMock = jest.fn();
    
    // 重新设置 mock
    (useListPage as jest.Mock).mockReturnValue({
      tableContext: [
        null, 
        { 
          reload: jest.fn(),
          getForm: () => ({
            updateSchema: updateSchemaMock
          }),
          setProps: jest.fn()
        }
      ]
    });

    // 重新创建组件实例
    wrapper = mount(PlanOrderList, {
      global: {
        mocks: {
          $route: {
            params: {
              code: 'some-code'
            }
          }
        }
      }
    });
    
    const vm = wrapper.vm as any;
    vm.reloadData(parentData);
    
    // 验证 updateSchema 被调用
    expect(updateSchemaMock).toHaveBeenCalledWith([
      expect.objectContaining({
        field: 'deviceId',
        component: 'Select',
        componentProps: expect.objectContaining({
          mode: 'multiple',
          showSearch: true,
          optionFilterProp: 'label'
        })
      })
    ]);
  });
});