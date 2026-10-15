// ... existing code ...

import { shallowMount } from '@vue/test-utils';
import SopReviewPlanList from '@/views/dataCalculation/sop/review/SopReviewPlanList.vue';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { useListPage } from '@/hooks/system/useListPage';
import { list, saveOrUpdate, getExportUrl, getImportUrl } from '@/views/dataCalculation/sop/review/SopReviewPlan.api';
import { getDictItems, dicList } from '@/api/common/api';
import { getWeek } from '@/views/dataCalculation/sop/plan/SopStatisticData.api';

//--story=1055308 --user=孙金伟 23-标准制定与执行优化处理 https://www.tapd.cn/63554897/s/1382581 --单元测试
//--story=1055308 --user=孙金伟 23-标准制定与执行优化处理 https://www.tapd.cn/63554897/s/1382581 --单元测试
describe('SopReviewPlanList.vue', () => {
  let wrapper: any;

  beforeEach(() => {
    wrapper = shallowMount(SopReviewPlanList);
  });

  it('submitForm should show warning if dataSource is empty', async () => {
    // Mock dependencies
    wrapper.setData({ editIds: {} });
    wrapper.vm.getDataSource = () => [];

    // Spy on createMessage.warning
    const warningSpy = jest.spyOn(wrapper.vm, 'createMessage');

    await wrapper.vm.submitForm();

    expect(warningSpy).toHaveBeenCalledWith('保存记录为空');
  });

  it('submitForm should call saveOrUpdate with dataSource', async () => {
    // Mock data
    const mockDataSource = [{ id: 1, name: 'Test' }];
    wrapper.setData({ editIds: { 1: true } });
    wrapper.vm.getDataSource = () => mockDataSource;

    // Mock saveOrUpdate to resolve with success
    wrapper.vm.saveOrUpdate = jest.fn().mockResolvedValue({ success: true, message: 'Success' });

    // Spy on createMessage.success and reload
    const successSpy = jest.spyOn(wrapper.vm, 'createMessage');
    const reloadSpy = jest.spyOn(wrapper.vm, 'reload');

    await wrapper.vm.submitForm();

    expect(wrapper.vm.saveOrUpdate).toHaveBeenCalledWith(mockDataSource);
    expect(successSpy).toHaveBeenCalledWith('Success');
    expect(reloadSpy).toHaveBeenCalled();
  });

  it('submitForm should show warning if saveOrUpdate fails', async () => {
    // Mock data
    const mockDataSource = [{ id: 1, name: 'Test' }];
    wrapper.setData({ editIds: { 1: true } });
    wrapper.vm.getDataSource = () => mockDataSource;

    // Mock saveOrUpdate to resolve with failure
    wrapper.vm.saveOrUpdate = jest.fn().mockResolvedValue({ success: false, message: 'Failure' });

    // Spy on createMessage.warning
    const warningSpy = jest.spyOn(wrapper.vm, 'createMessage');

    await wrapper.vm.submitForm();

    expect(warningSpy).toHaveBeenCalledWith('Failure');
  });
});

//--story=1055315 --user=孙金伟 23-5WHY优化处理 https://www.tapd.cn/63554897/s/1383013 单元测试
// 模拟依赖的 API 和钩子
vi.mock('@/hooks/system/useListPage', () => ({
  useListPage: vi.fn().mockReturnValue([
    {
      reload: vi.fn(),
      setColumns: vi.fn(),
      getDataSource: vi.fn().mockReturnValue([])
    },
    {
      selectedRowKeys: { value: [] }
    }
  ])
}));

vi.mock('@/views/dataCalculation/sop/review/SopReviewPlan.api', () => ({
  list: vi.fn(),
  saveOrUpdate: vi.fn(),
  getExportUrl: vi.fn(),
  getImportUrl: vi.fn()
}));

vi.mock('@/api/common/api', () => ({
  getDictItems: vi.fn(),
  dicList: vi.fn()
}));

vi.mock('@/views/dataCalculation/sop/plan/SopStatisticData.api', () => ({
  getWeek: vi.fn()
}));

describe('SopReviewPlanList.vue', () => {
  let wrapper;

  beforeEach(() => {
    wrapper = mount(SopReviewPlanList, {
      props: {}
    });
  });

  it('should call searchQuery when search button is clicked', async () => {
    const searchQuery = wrapper.vm.searchQuery;
    const reload = vi.spyOn(wrapper.vm, 'reload');
    searchQuery();
    expect(reload).toHaveBeenCalled();
  });

  it('should call submitForm when save button is clicked', async () => {
    const submitForm = wrapper.vm.submitForm;
    const saveOrUpdateMock = vi.mocked(saveOrUpdate);
    saveOrUpdateMock.mockResolvedValue({ success: true, message: '保存成功' });
    wrapper.vm.getDataSource = vi.fn().mockReturnValue([{ id: '1' }]);
    wrapper.vm.editIds.value = { '1': true };
    await submitForm();
    expect(saveOrUpdateMock).toHaveBeenCalled();
  });

  it('should call onExportXls when export button is clicked', () => {
    const onExportXls = wrapper.vm.onExportXls;
    const exportMock = vi.fn();
    wrapper.vm.onExportXls = exportMock;
    onExportXls();
    expect(exportMock).toHaveBeenCalled();
  });

  it('should call onImportXls when import button is clicked', () => {
    const onImportXls = wrapper.vm.onImportXls;
    const importMock = vi.fn();
    wrapper.vm.onImportXls = importMock;
    onImportXls();
    expect(importMock).toHaveBeenCalled();
  });

  it('should call rowDbClick when row is double clicked', () => {
    const rowDbClick = wrapper.vm.rowDbClick;
    const record = { id: '1' };
    rowDbClick(record);
    expect(wrapper.vm.showSlotRow.value).toBe('1');
    expect(wrapper.vm.editIds.value).toEqual({ '1': true });
  });

  it('should fetch dict data when getDict is called', async () => {
    const getDict = wrapper.vm.getDict;
    const code = 'MES3_SOP_CRUX';
    const obj = { value: [] };
    dicList.mockResolvedValue([{ itemVal: '1', itemKey: '是' }]);
    await getDict(code, obj);
    expect(obj.value).toEqual([{ value: '1', label: '是', disabled: undefined }]);
  });

  it('should fetch week data when getWeekData is called', async () => {
    const getWeekData = wrapper.vm.getWeekData;
    getWeek.mockResolvedValue([
      { weekRangeStr: '2023-01', weekNumber: 1 }
    ]);
    await getWeekData();
    expect(wrapper.vm.tableColumns.value).toContainEqual(
      expect.objectContaining({
        title: '2023-01',
        children: expect.arrayContaining([
          expect.objectContaining({
            title: '第1周',
            dataIndex: '2023-01'
          })
        ])
      })
    );
  });

  it('should return correct option text', () => {
    const getOptionText = wrapper.vm.getOptionText;
    wrapper.vm.userArray.value = [{ value: '1', label: '用户1' }];
    expect(getOptionText('1', 'user')).toBe('用户1');
  });
});