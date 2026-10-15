import { handleDelete } from '@/views/dataCalculation/postCheck/postCheckRecordConfig/components/PostCheckRecordConfigProjectModal.vue'; // 根据实际路径调整

// Mock functions
jest.mock('@/api/config', () => ({
  deleteConfig: jest.fn()
}));

const mockGetDataSource = jest.fn();
const mockDeleteConfig = jest.fn();
const mockDeleteTableDataRecord = jest.fn();
const mockSetModalProps = jest.fn();

// Mock global functions
global.getDataSource = mockGetDataSource;
global.deleteConfig = mockDeleteConfig;
global.deleteTableDataRecord = mockDeleteTableDataRecord;
global.setModalProps = mockSetModalProps;

describe('handleDelete function', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  /**
   * --story=1055312 --user=孙金伟 23-最佳实践优化处理 https://www.tapd.cn/63554897/s/1383697 单元测试
   * 测试删除具有正常ID且rowSpan为1的记录时，是否正确调用删除API
   */
  it('should call deleteConfig when deleting non-temporary data with rowSpan=1', async () => {
    const record = {
      bzid: 'normal_id',
      id: '123',
      rowSpan: 1
    };
    const index = 0;
    const tableData = [record];
    
    mockGetDataSource.mockReturnValue(tableData);
    mockDeleteConfig.mockResolvedValue();

    await handleDelete(record, index);

    // 验证deleteConfig被正确调用
    expect(mockDeleteConfig).toHaveBeenCalledWith({
      id: '123',
      bzid: 'normal_id'
    });
    
    // 验证记录被标记为删除
    expect(record.bzid).toBe('del');
    expect(mockDeleteTableDataRecord).toHaveBeenCalledWith('del');
    
    // 验证加载状态被重置
    expect(mockSetModalProps).toHaveBeenCalledWith({ confirmLoading: false });
  });

  /**
   * Test Case 2: 删除非临时数据且rowSpan>1
   * 测试删除合并行的第一条记录时，是否正确处理下一行的合并属性
   */
  it('should handle rowSpan>1 deletion correctly', async () => {
    const record = {
      bzid: 'normal_id',
      rowSpan: 3,
      step: 'step1',
      checkFrequency: 'daily',
      frequencyIllustrate: 'illustrate1',
      frequencyIllustrateCount: 5,
      checkItem: 'item1'
    };
    const nextRecord = {
      rowSpan: 0
    };
    const index = 0;
    const tableData = [record, nextRecord];
    
    mockGetDataSource.mockReturnValue(tableData);
    mockDeleteConfig.mockResolvedValue();

    await handleDelete(record, index);

    // 验证deleteConfig被调用
    expect(mockDeleteConfig).toHaveBeenCalledWith({
      id: '',
      bzid: 'normal_id'
    });
    
    // 验证下一行继承了合并属性
    expect(nextRecord.rowSpan).toBe(2);
    expect(nextRecord.step).toBe('step1');
    expect(nextRecord.checkFrequency).toBe('daily');
    expect(nextRecord.frequencyIllustrate).toBe('illustrate1');
    expect(nextRecord.frequencyIllustrateCount).toBe(5);
    expect(nextRecord.checkItem).toBe('item1');
  });

  /**
   * Test Case 3: 删除rowSpan=0的数据
   * 测试删除合并行中的非第一条记录时，是否正确更新主行的rowSpan
   */
  it('should decrease rowSpan of main row when deleting rowSpan=0 record', async () => {
    const record = {
      bzid: 'temp_bz_123',
      id: 'main_id',
      rowSpan: 0
    };
    const mainRecord = {
      id: 'main_id',
      rowSpan: 3
    };
    const index = 1;
    const tableData = [mainRecord, record];
    
    mockGetDataSource.mockReturnValue(tableData);

    await handleDelete(record, index);

    // 验证主行的rowSpan减少
    expect(mainRecord.rowSpan).toBe(2);
    
    // 验证临时数据不调用deleteConfig
    expect(mockDeleteConfig).not.toHaveBeenCalled();
  });

  /**
   * Test Case 4: 删除临时数据
   * 测试删除临时创建的数据时，是否跳过API调用
   */
  it('should not call deleteConfig for temporary data', async () => {
    const record = {
      bzid: 'temp_bz_123',
      id: 'temp_id',
      rowSpan: 1
    };
    const index = 0;
    const tableData = [record];
    
    mockGetDataSource.mockReturnValue(tableData);

    await handleDelete(record, index);

    // 验证不调用deleteConfig
    expect(mockDeleteConfig).not.toHaveBeenCalled();
    
    // 验证记录被正确标记和删除
    expect(record.bzid).toBe('del');
    expect(mockDeleteTableDataRecord).toHaveBeenCalledWith('del');
  });

  /**
   * Test Case 5: 删除操作异常处理
   * 测试删除过程中发生异常时，是否正确处理错误并重置加载状态
   */
  it('should handle delete error correctly', async () => {
    const record = {
      bzid: 'normal_id',
      id: '123',
      rowSpan: 1
    };
    const index = 0;
    const tableData = [record];
    const errorMessage = 'Delete failed';
    
    mockGetDataSource.mockReturnValue(tableData);
    mockDeleteConfig.mockRejectedValue(new Error(errorMessage));

    await handleDelete(record, index);

    // 验证异常被处理（这里通过finally块重置加载状态）
    expect(mockSetModalProps).toHaveBeenCalledWith({ confirmLoading: false });
    
    // 验证记录仍被标记为删除（即使API失败）
    expect(record.bzid).toBe('del');
    expect(mockDeleteTableDataRecord).toHaveBeenCalledWith('del');
  });

  /**
   * Test Case 6: 空表数据边界情况
   * 测试在空表格数据情况下的删除行为
   */
  it('should handle empty table data gracefully', async () => {
    const record = {
      bzid: 'normal_id',
      id: '123',
      rowSpan: 1
    };
    const index = 0;
    
    mockGetDataSource.mockReturnValue([]);
    mockDeleteConfig.mockResolvedValue();

    await handleDelete(record, index);

    // 验证基本删除流程仍能执行
    expect(mockDeleteConfig).toHaveBeenCalledWith({
      id: '123',
      bzid: 'normal_id'
    });
    expect(record.bzid).toBe('del');
  });
});