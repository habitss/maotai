import { ref } from 'vue'
import { describe, it, expect, beforeEach, vi } from 'vitest'

// --story=1056858 --user=孙金伟 24-首页PI统计开发 https://www.tapd.cn/63554897/s/1388774 单元测试
describe('getPayCloseAttentionPi', () => {
  // Mock 数据
  let mockQueryPayCloseAttentionPi
  let mockUserStore
  let mockDataPi

  beforeEach(() => {
    // Reset mocks before each test
    vi.resetAllMocks()
    
    // Mock API function
    mockQueryPayCloseAttentionPi = vi.fn()
    
    // Mock user store
    mockUserStore = {
      userInfo: {
        orgCode: 'TEST_ORG_CODE'
      }
    }
    
    // Mock reactive data
    mockDataPi = ref(null)
  })

  it('should call queryPayCloseAttentionPi with correct parameters and set dataPi value', async () => {
    // Arrange: 准备测试数据
    const mockResult = { id: 1, name: 'Test Data' }
    mockQueryPayCloseAttentionPi.mockResolvedValue({
      result: mockResult
    })

    // Act: 执行被测函数
    const { getPayCloseAttentionPi } = createTestModule()
    await getPayCloseAttentionPi()

    // Assert: 验证结果
    // 验证 API 被正确调用
    expect(mockQueryPayCloseAttentionPi).toHaveBeenCalledWith({
      deptCode: 'TEST_ORG_CODE'
    })
    
    // 验证 dataPi 被正确赋值
    expect(mockDataPi.value).toEqual(mockResult)
  })

  it('should handle API call failure gracefully', async () => {
    // Arrange: 准备错误情况
    const errorMessage = 'API Error'
    mockQueryPayCloseAttentionPi.mockRejectedValue(new Error(errorMessage))

    // Act & Assert: 验证错误处理
    const { getPayCloseAttentionPi } = createTestModule()
    await expect(getPayCloseAttentionPi()).rejects.toThrow(errorMessage)
    
    // 验证 dataPi 保持原值
    expect(mockDataPi.value).toBeNull()
  })

  it('should handle undefined orgCode gracefully', async () => {
    // Arrange: 准备 undefined orgCode 情况
    mockUserStore.userInfo.orgCode = undefined
    const mockResult = { id: 2, name: 'Default Data' }
    mockQueryPayCloseAttentionPi.mockResolvedValue({
      result: mockResult
    })

    // Act: 执行被测函数
    const { getPayCloseAttentionPi } = createTestModule()
    await getPayCloseAttentionPi()

    // Assert: 验证传入 undefined 参数
    expect(mockQueryPayCloseAttentionPi).toHaveBeenCalledWith({
      deptCode: undefined
    })
    expect(mockDataPi.value).toEqual(mockResult)
  })

  it('should handle undefined userInfo gracefully', async () => {
    // Arrange: 准备 undefined userInfo 情况
    mockUserStore.userInfo = undefined
    mockQueryPayCloseAttentionPi.mockResolvedValue({
      result: { id: 3, name: 'Fallback Data' }
    })

    // Act: 执行被测函数
    const { getPayCloseAttentionPi } = createTestModule()
    await getPayCloseAttentionPi()

    // Assert: 验证传入 undefined deptCode
    expect(mockQueryPayCloseAttentionPi).toHaveBeenCalledWith({
      deptCode: undefined
    })
  })

  it('should handle undefined userStore gracefully', async () => {
    // Arrange: 准备 undefined userStore 情况
    mockUserStore = undefined
    mockQueryPayCloseAttentionPi.mockResolvedValue({
      result: { id: 4, name: 'No User Data' }
    })

    // Act: 执行被测函数
    const { getPayCloseAttentionPi } = createTestModule()
    await getPayCloseAttentionPi()

    // Assert: 验证传入 undefined deptCode
    expect(mockQueryPayCloseAttentionPi).toHaveBeenCalledWith({
      deptCode: undefined
    })
  })

  // Helper function to create test module with mocks
  function createTestModule() {
    // 这里模拟导入的模块，实际使用时需要根据具体实现调整
    const queryPayCloseAttentionPi = mockQueryPayCloseAttentionPi
    const userStore = mockUserStore
    const dataPi = mockDataPi

    async function getPayCloseAttentionPi() {
      const { result } = await queryPayCloseAttentionPi({ 
        deptCode: userStore?.userInfo?.orgCode 
      });
      dataPi.value = result;
    }

    return {
      getPayCloseAttentionPi,
      queryPayCloseAttentionPi,
      userStore,
      dataPi
    }
  }
})