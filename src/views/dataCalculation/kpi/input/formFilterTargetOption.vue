<template>
  <!--目标值查询条件-->
  <div class="filter-options">
    <!--查询区域-->
    <div class="jeecg-basic-table-form-container">
      <a-form ref="formRef" @keyup.enter.native="searchQuery" :model="queryParam" :label-col="labelCol" :wrapper-col="wrapperCol">
        <a-row :gutter="24">
          <!--年份-->
          <a-col :lg="6">
            <a-form-item label="年份">
              <a-date-picker v-model:value="queryParam.year" picker="year" valueFormat="YYYY" format="YYYY" style="width: 200px;" @change="getSelectMounth"/>
            </a-form-item>
          </a-col>

          <!--月份-->
          <a-col :lg="6">
            <a-form-item label="月份">
                <a-select v-model:value="queryParam.months" class="select-style" show-search
                  placeholder="请选择月份"
                  style="width: 200px"
                  mode="multiple" :maxTagCount="1" :filter-option="filterOption">
                  <a-select-option :value="item.value" v-for="item in mounthItem" :key="item.value">
                    {{ item.text }}
                  </a-select-option>
                </a-select>
              </a-form-item>
          </a-col>

          <!--KPI分类-->
          <a-col :lg="6">
            <a-form-item label="KPI分类">
              <a-tree-select
                v-model:value="queryParam.perentId"
                show-search
                style="width: 200px"
                :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
                placeholder="请选择KPI分类"
                allow-clear
                :tree-data="treeData"
                tree-node-filter-prop="kpiType"
                :fieldNames= "{children:'children', label:'kpiType', value: 'id' }"
              ></a-tree-select>
            </a-form-item>
          </a-col>
          <!--KPI名称-->
          <a-col :lg="6">
            <a-form-item label="KPI名称">
              <a-input placeholder="请输入KPI名称" v-model:value="queryParam.kpiName" allow-clear/>
            </a-form-item>
          </a-col>
          <!--层级部门-->
          <a-col :span="6">
							<a-form-item label="层级部门">
                <!-- <JSelectDept v-model:value="formData.dept" :multiple="false" :showButton="false" :expandonsearch="true" :search="true" rowKey="orgCode"/> -->
                <a-tree-select
                  v-model:value="queryParam.deptCode"
                  show-search
                  style="width: 200px"
                  :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
                  placeholder="请选择层级部门"
                  :max-tag-count = "2"
                  multiple
                  allow-clear
                  :treeDefaultExpandedKeys="treeDefaultExpandedKeys"
                  :tree-data="kpiRoomList"
                  tree-node-filter-prop="departName"
                  :fieldNames= "{children:'children', label:'departName', key: 'id', value: 'orgCode' }"
                ></a-tree-select>
							</a-form-item>
					</a-col>

          <!--填写部门-->
          <a-col :span="6">
							<a-form-item label="填写部门">
                <a-tree-select
                  v-model:value="queryParam.inputDeptQuery"
                  show-search
                  style="width: 200px"
                  :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
                  placeholder="请选择层级部门"
                  :max-tag-count = "2"
                  multiple
                  allow-clear
                  :treeDefaultExpandedKeys="treeDefaultExpandedKeys"
                  :tree-data="kpiRoomList"
                  tree-node-filter-prop="departName"
                  :fieldNames= "{children:'children', label:'departName', key: 'id', value: 'orgCode' }"
                ></a-tree-select>
							</a-form-item>
					</a-col>

          <!-- <a-col :lg="2"> -->
            <!--周累-->
            <!-- <a-checkbox @change="e => changeWeek(e)" style="margin-top: 5px;">周累</a-checkbox>
          </a-col> -->
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <span style="float: left; overflow: hidden; margin-left: -12px" class="table-page-search-submitButtons">
              <a-col :lg="6">
                <!--周累-->
                <a-checkbox @change="e => changeWeek(e)" style="margin-top: 5px;">周累</a-checkbox>
                <a-checkbox @change="e => changeKpiDuty(e)" style="margin-top: 5px;">KPI职责分工</a-checkbox>
                <a-button type="primary" preIcon="ant-design:search-outlined" @click="searchQuery">{{ t('language.search') }}</a-button>
                <a-button type="primary" preIcon="ant-design:reload-outlined" @click="searchReset" style="margin-left: 8px">{{
                  t('language.reset')
                }}</a-button>
                <a-button type="primary" preIcon="ant-design:save-outlined" @click="saveData()" style="margin-left: 8px">保存</a-button>
                <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls()" style="margin-left: 8px">导出</a-button>
              </a-col>
            </span>
          </a-col>

        </a-row>
      </a-form>
    </div>
  </div>
</template>

<script lang="ts" name="kpi-kpiReportInput" setup>
  import { ref, reactive, onMounted } from 'vue';
  import { list as typeList } from '/@/views/dataCalculation/kpi/type/KpiType.api';
  import { queryDeptTreeList } from '/@/views/dataCalculation/kpi/info/KpiInfo.api';
  const emit = defineEmits(['filterData', 'saveData', 'exportExcel']);
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  const formRef = ref();
  const queryParam = reactive<any>({
    showWeek: false,
    showKpiDuty: false
  });
  const treeData = ref([]);
  const kpiRoomList = ref([]);
  const labelCol = reactive({
    xs:24,
    sm:4,
    xl:6,
    xxl:7
  });
  const wrapperCol = reactive({
    xs: 18,
    sm: 17,
  });
  const treeDefaultExpandedKeys = ref([])
  
  //月份集合
  const mounthItem = ref([]);

  /**
   * 查询
   */
  function searchQuery() {
    if(queryParam.deptCode){
      queryParam.deptCodeStr = queryParam.deptCode.join(",")
    }
    if(queryParam.inputDeptQuery){
      queryParam.inputDeptQueryStr = queryParam.inputDeptQuery.join(",")
    }
    
    emit('filterData',queryParam)
  }
  
  /**
   * 重置
   */
  function searchReset() {
    queryParam.months = undefined ;
    queryParam.perentId = undefined ;
    queryParam.deptCode = undefined ;
    //刷新数据
    searchQuery();
  }
  
  /**
   * 保存
   * @param 
   */
  function saveData(){
    emit('saveData')
  }
  /**
   * 导出
   * @param 
   */
   function onExportXls(){
    emit('exportExcel')
   }

  //KPI分类
  async function classification(){
    treeData.value = [];
    let res = await typeList({});
    treeData.value.push(...res);
  }

  //部门树
  async function deptTreeList(){
    let res = await queryDeptTreeList({factory:"",isRoot:"true"});
    kpiRoomList.value = res
    //获取第一层(展开)
    if(res){
      treeDefaultExpandedKeys.value = [res[0].orgCode];
    }
  }

  onMounted(() => {
    
    //初始化年
    // 创建一个 Date 对象，默认表示当前日期和时间
    const currentDate = new Date();
    // // 使用 getFullYear() 方法获取四位数的年份
    const currentYear = currentDate.getFullYear();
    // const currentMonth = currentDate.getMonth()+1;
    // queryParam.year = currentYear+"-"+(currentMonth>=10?currentMonth:"0"+currentMonth);
    queryParam.year = currentYear+"";

    classification();
    deptTreeList();
    getSelectMounth()
  })

  //获取月份
  function getSelectMounth() {
    const curYear = [];
    for (let i = 1; i < 13; i++) {
      let val = i < 10 ? `0${i}` : i
      curYear.push({ 'value': `${i}`, 'text': `${val}月` })
    }
    mounthItem.value = curYear
  }
    //周累
  function changeWeek(e){
    queryParam.showWeek = e.target.checked
  }

  //KPI职责分工
  function changeKpiDuty(e){
    queryParam.showKpiDuty = e.target.checked
  }

  function filterOption(input, option) {
    console.log('option.componentOptions.children[0].text:', option.componentOptions.children[0].text)
    return (
      option.componentOptions.children[0].text.toLowerCase().indexOf(input.toLowerCase()) >= 0
    );
  }
  defineExpose({
    searchQuery
  });
</script>

<style lang="less" scoped>
  .box{
    background-color: #fff;
  }
  .jeecg-basic-table-form-container {
    padding: 0;
    .table-page-search-submitButtons {
      display: block;
      margin-bottom: 24px;
      white-space: nowrap;
    }
    .query-group-cust{
      min-width: 100px !important;
    }
    .query-group-split-cust{
      width: 30px;
      display: inline-block;
      text-align: center
    }
    .ant-form-item:not(.ant-form-item-with-help){
      margin-bottom: 16px;
      height: 32px;
    }
    :deep(.ant-picker),:deep(.ant-input-number){
      width: 100%;
    }
  }
</style>
