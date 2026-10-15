import { ref, nextTick } from 'vue'
import { describe, it, expect, vi, beforeEach } from 'vitest'

// --story=1055781 --user=孙金伟 24-PI点图曲线图开发 https://www.tapd.cn/63554897/s/1384075 单元测试
//--story=1057867 --user=孙金伟 24-PI点图多标准开发 https://www.tapd.cn/63554897/s/1393029 单元测试
describe('handleSubmit', () => {
  let showEmpty
  let validate
  let list
  let initChart
  let handleSubmit

  beforeEach(() => {
    // Reset mocks and setup test environment
    showEmpty = ref(true)
    validate = vi.fn()
    list = vi.fn()
    initChart = vi.fn()
    
    // Mock nextTick
    vi.mocked(nextTick).mockImplementation((callback) => {
      if (typeof callback === 'function') {
        return Promise.resolve(callback())
      }
      return Promise.resolve()
    })

    // Recreate handleSubmit function with mocked dependencies
    handleSubmit = async function() {
      const values = await validate();
      //获取后台数据
      const res = await list(values);
      if (!res || res.yAxisArr.length <= 0) {
        return;
      }
      showEmpty.value = false;
      await nextTick(() => { 
        initChart(res, values)
      });
    }
  })

  it('should handle successful submission flow', async () => {
    // Arrange: Setup mock data
    const mockValues = { name: 'test' }
    const mockResponse = { yAxisArr: [1, 2, 3], data: 'some data' }
    
    validate.mockResolvedValue(mockValues)
    list.mockResolvedValue(mockResponse)

    // Act: Execute the function
    await handleSubmit()

    // Assert: Verify all expected behaviors
    expect(validate).toHaveBeenCalled()
    expect(list).toHaveBeenCalledWith(mockValues)
    expect(showEmpty.value).toBe(false)
    expect(initChart).toHaveBeenCalledWith(mockResponse, mockValues)
  })
})