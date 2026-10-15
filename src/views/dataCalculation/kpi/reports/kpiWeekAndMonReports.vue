<template>
  <a-spin :spinning="confirmLoading" class="span-box">
    <div class="p-2">
      <!--查询区域-->
      <div class="jeecg-basic-table-form-container">
        <a-form ref="formRef" @keyup.enter.native="searchQuery" :model="queryParam" :label-col="labelCol" :wrapper-col="wrapperCol">
          <a-row :gutter="24">
            <!--年份-->
            <a-col :lg="6">
              <a-form-item label="年份">
                <a-date-picker v-model:value="queryParam.year" picker="year" valueFormat="YYYY" format="YYYY" 
                @change="getSelectMounth" :allowClear="false"/>
              </a-form-item>
            </a-col>
            <!-- 月份分类筛选 -->
            <a-col :lg="6">
              <a-form-item label="月份">
                <a-select v-model:value="queryParam.months" class="select-style" show-search
                  mode="multiple" :maxTagCount="1" :filter-option="filterOption">
                  <a-select-option :value="item.value" v-for="item in mounthItem" :key="item.value">
                    {{ item.text }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>

            <!--KPI分类-->
            <a-col :lg="5">
              <a-form-item label="KPI分类">
                <a-tree-select
                  v-model:value="queryParam.typeId"
                  show-search
                  style="width: 100%"
                  :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
                  placeholder="请选择KPI分类"
                  allow-clear
                  :tree-data="treeData"
                  tree-node-filter-prop="kpiType"
                  :fieldNames= "{children:'children', label:'kpiType', value: 'id' }"
                ></a-tree-select>
              </a-form-item>
            </a-col>
            
            <!--层级部门-->
            <a-col :span="7">
                <a-form-item label="层级部门">
                  <a-tree-select
                    class="selectQuery"
                    v-model:value="queryParam.deptQuery"
                    show-search
                    style="width: 60%"
                    :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
                    placeholder="请选择层级部门"
                    allow-clear
                    :treeDefaultExpandedKeys="treeDefaultExpandedKeys"
                    :tree-data="kpiRoomList"
                    tree-node-filter-prop="departName"
                    :fieldNames= "{children:'children', label:'departName', key: 'id', value: 'orgCode' }"
                  ></a-tree-select>
                  <a-radio-group v-model:value="queryParam.deptQueryType" button-style="solid">
                    <a-radio-button value="0" title="当前层级">当前</a-radio-button>
                    <a-radio-button value="1" title="当前层级及下级">下级</a-radio-button>
                  </a-radio-group>
                  <!-- <a-checkbox @change="e => changeNode(e)" style="margin-left: 5px;">查看下级KPI</a-checkbox> -->
                </a-form-item>
            </a-col>
            
            <!--填写部门-->
            <a-col :span="6">
                <a-form-item label="填写部门">
                  <a-tree-select
                    v-model:value="queryParam.inputDeptQuery"
                    show-search
                    :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
                    placeholder="请选择层级部门"
                    :max-tag-count = "2"
                    allow-clear
                    :treeDefaultExpandedKeys="treeDefaultExpandedKeys"
                    :tree-data="kpiRoomList"
                    tree-node-filter-prop="departName"
                    :fieldNames= "{children:'children', label:'departName', key: 'id', value: 'orgCode' }"
                  ></a-tree-select>
                </a-form-item>
            </a-col>

            <!--标签-->
            <a-col :span="6">
              <a-form-item label="标签">
                <kt-dict
                  v-model:value="queryParam.label"
                  placeholder="请选择标签"
                  dict="MES3_KPI_INFO_LABEL"
                />
              </a-form-item>
            </a-col>
           
            <!--按钮-->
            <a-col :span="12">
              <span style="float: left; overflow: hidden; margin-left: -12px" class="table-page-search-submitButtons">
                  <!--二级KPI-->
                  <a-checkbox @change="e => changeNode(e)" style="margin-left: 30px;">二级KPI</a-checkbox>
                  <!--周累-->
                  <a-checkbox @change="e => changeWeek(e)">周累</a-checkbox>
                  <!--KPI职责分工-->
                  <a-checkbox @change="e => changeKpiDuty(e)" style="margin-top: 5px;">KPI职责分工</a-checkbox>
                  <!--查询-->
                  <a-button type="primary" preIcon="ant-design:search-outlined" @click="searchQuery">{{ t('language.search') }}</a-button>
                  <!--重算-->
                  <a-button type="primary" preIcon="ant-design:reload-outlined" @click="recalculate" v-auth="'kpi:mes3_kpi_reports:rerun'" style="margin-left: 8px">重算</a-button>
                  <!--导出-->
                  <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls" style="margin-left: 8px"> 导出</a-button>
              </span>
            </a-col>
          </a-row>

        </a-form>
      </div>
        <div class="table-content" ref="container">
          <!--引用表格-->
          <a-table :columns="columns" :data-source="data" bordered size="small" rowKey="key" :pagination="false"
                :loading="loadingIcon" :scroll="{ x: 1100, y: '65vh' }" :expandIconColumnIndex="0" class="tableBox">

                <!--曲线-->
                <!-- <template v-for="col in curveColoumns" #[col]="{ text, record }" :key="col">
                  <div style="display: inline-block;width: 100%; height: 100%;position:relative">
                    <span class="pointer" @click="(e) => showCrue(2, col, record, e)"> -->
                      <!-- {{ text && text.value }} -->
                    <!-- </span>
                  </div>
                </template> -->
                <template #bodyCell="{ column, record }">
                   <template v-if="column.key === 'curve'">
                      <div style="display: inline-block;width: 100%; height: 100%;position:relative">
                        <span class="pointer" @click="(e) => showCrue(2, column.key, record, e)">
                          {{ record && record['curve'].value }}
                        </span>
                      </div>
                   </template>
                </template>
                
                <!--周列-->
                <template v-for="col in allObjectColoumns" #[col]="{ text, record, index }">
                  <div style="display: inline-block;width: 100%; height: 100%;position:relative">
                    <span class="pointer" @click="(e) => showCrue(5, col, record, e)">
                      {{ text && toFixedValue(text.value, record) }}
                    </span>
                    <div style="position:absolute;top:-4px;left:-2px;">
                      <div v-if="index % 2 === 1 && text && text.fiveWhyMark && (text.value || text.value === 0 || text.value === '0')" class="cell-with-triangle-left">
                        <div>
                          <span class="cell-with-triangle-left-text">5W</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </template>

                <!--R A C I-->
                <template v-for="col in personColoumns" #[col]="{ text, record, index }">
                  <span>
                    {{ text && text.value }}
                  </span>
                </template>

                <!-- 月列 -->
                <!-- <template v-for="colItem in monthColoumns" :slot="colItem" slot-scope="text, record, index"> -->
                <template v-for="col in monthColoumns" #[col]="{ text, record, index }">
                  <div style="display: inline-block;width: 100%; height: 100%;position:relative">
                    <span class="pointer" @click="e => showModal(e, 1, col, record)">
                      {{ text && toFixedValue(text.value, record) }}
                    </span>
                    <div style="position:absolute;top:-4px;left:-2px;">
                      <div v-if="index % 2 === 1 && text && text.fiveWhyMark && (text.value || text.value === 0 || text.value === '0')" class="cell-with-triangle-left">
                        <div>
                          <span class="cell-with-triangle-left-text">5W</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </template>

          </a-table>
          <!--操作菜单-->
          <div v-if="visible" :style="{ left: left + 'px', top: top + 'px' }" class="popupModal">
            <a class="item" v-if="!isWeekVisible" @click="openModal($event, 1)" style="">差距分析</a><br>
            <a class="item" v-if="!whyVisible" @click="openModal($event, 2)">5Why</a>
            <a class="item" @click="openModal($event, 3)">数据修改</a>
          </div>
        </div>
      <!-- 差距分析 -->
      <KpiTargetModal ref="kpiTargetModal" @success="handleSuccess"></KpiTargetModal>
      <!-- 数据修改modal -->
      <KpiReportExceptionLogModal @register="registerUpdateDataModal" @success="handleSuccess"></KpiReportExceptionLogModal>
      <!-- 5why分析 modal -->
      <FiveWhy ref="fiveWhyRef" @success="searchQuery"></FiveWhy>
      <!--KPI趋势曲线图-->
      <KpiTrend ref="kpiTrendRef"></KpiTrend>
    </div>
  </a-spin>
</template>

<script lang="ts" name="kpi-kpiWeekAndMonth" setup>
  import { ref, reactive,onMounted,computed } from 'vue';
  import { queryKpiReportHeader,listKpiReportData,retryReport } from './kpiWeekAndMonReports.api';
  import { useUserStore } from '/@/store/modules/user';
  import { list as typeList } from '/@/views/dataCalculation/kpi/type/KpiType.api';
  import ExcelJS from '/@/utils/excelUtils/exportExcel';
  import KpiTargetModal from './components/kpiTargetModal.vue';
  import KpiReportExceptionLogModal from '/@/views/dataCalculation/kpi/dataUpdate/components/KpiReportExceptionLogModal.vue';
  import FiveWhy from '/@/views/dataCalculation/fiveWhy/fiveWhy.vue';
  import KpiTrend from './components/kpiTrend.vue';
  import { queryDeptTreeList } from '/@/views/dataCalculation/kpi/info/KpiInfo.api';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useModal } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import KtDict from '/@/components/KtDict/KtDict.vue';
  //注册model
  const { createMessage } = useMessage();
  const [registerUpdateDataModal, { openModal: openUpdateDataModal }] = useModal();
  const { t } = useI18n();
  const confirmLoading = ref<boolean>(false);
  const formRef = ref();
  const queryParam = reactive<any>({});
  const loadingIcon = ref<boolean>(false);
  const visible = ref<boolean>(false);
  const treeData = ref([]);
  const userStore = useUserStore();
  const data = ref([]);
  //数据提取(目标/实际)
  const keyData = ref({})
  const columns = ref([]);
  const kpiTargetModal = ref();
  const fiveWhyRef = ref();
  const kpiTrendRef = ref();
  const container = ref(null);
  const left = ref(100);
  const top = ref(100);
  const kpiRoomList = ref([]);
  const yearNow = ref("")
  const treeDefaultExpandedKeys = ref([]);
  const year = ref("");
   //获取范围
  const rangeArray = ref({});
  //月份集合
  const mounthItem = ref([]);

  const labelCol = reactive({
    xs:24,
    sm:5,
    xl:6,
    xxl:5
  });
  const wrapperCol = reactive({
    xs: 24,
    sm: 20,
  });

  /**
   * 导出
   */
  function onExportXls(){
    let headersTemp = columns.value.filter(s => s.title !== '操作')
        let headers = []
        headersTemp.forEach((s, index) => {
          let headerArr = [];
          if(index < 7){
          // if(index < 9){
            headerArr = [s.title, s.title]
            headers.push({
              key: s.dataIndex,
              header: headerArr,
              width: String(s.title).length * 2.5 + 3
            })
          }else{
            if(s.children && s.children.length > 0){
              s.children.forEach(item => {
                headerArr = [s.title, item.title]
                headers.push({
                  key: item ? item.dataIndex: s.dataIndex,
                  header: headerArr,
                  width: String(s.title || item.title).length * 3.5 + 3
                })
              })
            }else{
              headers.push({
                key: s.children ? s.children[0].dataIndex: s.dataIndex,
                header: headerArr,
                width: String(s.title).length * 2.5 + 3
              })
            }
          }
        })
        console.log('headers:', headers)
        console.log('this.data--this.data:', data)

        //复制获取新数组
        const newArray = data.value.map(item => ({...item }));
        const dataNew = flattenDeepArray(newArray)

        const exportData = deepCloneByJSON(dataNew)

        const tableData = exportData.map(item => {
          for(let key in item){
            if(item[key] && item[key].colour){
              item[`${key}_Color`] = item[key].colour
            }
            item[key] = item[key].value
          }
          return item
        })
        console.log('tableData :>> ', tableData);
    ExcelJS.commonDownExcel(headers, tableData, 'KPI报表');
  }

  //将多层数组转成扁平化数组
  function flattenDeepArray(arr) {
    let flattened = [];
    for (let item of arr) {
      flattened.push(item);
      if (item.children) {
        let childrenFlattened = flattenDeepArray(item.children);
        flattened = flattened.concat(childrenFlattened);
        delete item.children;
      }
    }
    return flattened;
  }

  /**
   * 成功回调
   */
  function handleSuccess() {
    (selectedRowKeys.value = []) && reload();
  }
   
  /**
   * 操作栏
   */
  function getTableAction(record) {
    return [
      {
        label: '编辑',
        onClick: handleEdit.bind(null, record),
        auth: 'kpi:mes3_kpi_info:edit'
      },
      {
        label: '详情',
        onClick: handleDetail.bind(null, record),
      }, {
        label: '删除',
        popConfirm: {
          title: '是否确认删除',
          confirm: handleDelete.bind(null, record),
          placement: 'topLeft',
        },
        auth: 'kpi:mes3_kpi_info:delete'
      }
    ];
  }
   
  /**
   * 下拉操作栏
   */
  function getDropDownAction(record) {
    return []
  }

  /**
   * 查询
   */
  function searchQuery() {
    if(queryParam.months && queryParam.months.length == 0){
      createMessage.warning("请选择月份");
      return false;
    }
    year.value = queryParam.year;
    // getHaed();
    getData();
  }

  //重算
  async function recalculate(){
    confirmLoading.value = true;
    await retryReport({})
    .then((res) => {
      if (res.success) {
        createMessage.success(res.message);
      } else {
        createMessage.warning(res.message);
      }
    })
    .finally(() => {
      confirmLoading.value = false;
    });

  }

  onMounted(() => {
    queryParam.deptQueryType = "0";
    //初始化年
    // 创建一个 Date 对象，默认表示当前日期和时间
    const currentDate = new Date();
    // // 使用 getFullYear() 方法获取四位数的年份
    const currentYear = currentDate.getFullYear();
    const currentMonth = currentDate.getMonth()+1;
    queryParam.year = currentYear+"";
    let currentMonthStr = currentMonth >= 10 ? currentMonth : '0'+currentMonth;
    queryParam.months = [currentYear+"-"+currentMonthStr];
    yearNow.value = queryParam.year
    year.value= queryParam.year
    getSelectMounth(queryParam.year)
    //层级部门
    deptTreeList();
    //初始化分类
    classification();
    //classification(queryParam.year);

    //获取表头
    // getHaed();
    //获取数据
    getData();
    //隐藏菜单
    document.onclick = (e) => {
      visible.value = false
      isMounthVisible.value = false
      isWeekVisible.value = false
      whyVisible.value = false;
      // console.log('this.visible:', visible.value)
    }
  });
  //分类
  async function classification(){
    treeData.value = []
    let res = await typeList({});
    if(res && res.length > 0){
      treeData.value.push(...res);
    }
  }
  //获取表头
  async function getHaed(dataRes){
    // queryParam.year = "2025"
    //queryKpiReportHeader({year: queryParam.year,months:['2025-03'],}).then((res) => {
    // queryParam.months=['2025-03']
    await queryKpiReportHeader(queryParam).then((res) => {
      // columns.value = res.result;
      data.value = dataRes.result;
      if(data.value){
        keyData.value = flattenTree(data.value);
      }

      let { result } = res
      let cols = result.map((item, index) => {
        if (index < 7) {
          if(item.children){
            item = item.children[0]
          }
        }
        return item;
      })
      //获取范围
      result.forEach(function(item) {
        if(item.title.includes("周") && item.children){
          //周
          let obj = item.children[0];
          let title = obj["title"];
          let year = queryParam.year; 
          const result = title.split('-').map(part => `${year}-${part.replace('.', '-')}`).join('|');
          rangeArray.value[obj.key] = result
        }
      });

      //拷贝
      let colsTemp = deepCloneByJSON(cols)
      //设置表头属性
      loopHeadMap(colsTemp)
      columns.value  = setHeadAndData(colsTemp)
    })
    .finally(() => {
      confirmLoading.value = false;
    });
  }
  //获取数据
  async function getData(){
    confirmLoading.value = true
    await listKpiReportData(queryParam).then((res) => {
      getHaed(res)
    })
  }
  //遍历数据
  function flattenTree(nodes, result = {}) {
    nodes.forEach(node => {
      result[node.key] = node; // 提取当前节点
      if (node.children) {
        flattenTree(node.children, result); // 递归处理子节点
      }
    });
    return result;
  }



  //
  function filterOption(input, option) {
    console.log('option.componentOptions.children[0].text:', option.componentOptions.children[0].text)
    return (
      option.componentOptions.children[0].text.toLowerCase().indexOf(input.toLowerCase()) >= 0
    );
  }
  //获取月份
  function getSelectMounth(curYearVal) {
    //切换分类
    //classification(curYearVal)
    if(yearNow.value != curYearVal){
      queryParam.months=[];
      queryParam.months = [curYearVal+"-01"];
    }
    
   
    const curYear = [];
    for (let i = 1; i < 13; i++) {
      let val = i < 10 ? `0${i}` : i
      if (curYearVal) {
        curYear.push({ 'value': `${curYearVal}-${val}`, 'text': `${i}月` })
      } else {
        curYear.push({ 'value': `${val}`, 'text': `${val}` })
      }
    }
    yearNow.value = curYear
    mounthItem.value = curYear
  }

  //是否显示二级KPI
  function changeNode(e){
    queryParam.showChildrenKpi = e.target.checked
  }
  //是否显示周
  function changeWeek(e){
    console.log(e.target.checked)
    queryParam.showWeek = e.target.checked
  }
  //KPI职责分工
  function changeKpiDuty(e){
    queryParam.showKpiDuty = e.target.checked
  }
  //深度拷贝
  function deepCloneByJSON(obj) {
    return JSON.parse(JSON.stringify(obj));
  }
  //设置表头属性
  function loopHeadMap(columns) {
    debugger
    for (let item of columns) {
      item.dataIndex = item.key ? item.key : ''
      item.align = 'center'
      item.width = '80px'
      if(item.title == '单位'){
        item.fixed = queryParam.showWeek
        item.width = '60px'
      }
      if(item.title == '曲线'){
        item.width = '60px'
      }
      if(item.title == 'R' || item.title == 'A' || item.title == 'C'){
        item.width = '80px'
      }
      if(item.title.includes('累') ){
        item.width = '80px'
      }
      if(item.title.includes('月') ){
        item.width = '80px'
      }
      if(item.title.includes('目标') ){
        item.fixed = queryParam.showWeek
        item.width = '95px'
      }
      if(item.title.includes('实际') ){
        item.fixed = queryParam.showWeek
        item.width = '95px'
      }
      if(item.title.includes('项目') ){
        item.fixed = queryParam.showWeek
        item.width = '120px'
      }
      if(item.title.includes('分类') || item.title.includes('层级') ){
        item.fixed = queryParam.showWeek
        // item.width = '40px'
        item.width = '110px'
      }
      if(item.title == 'I'){
        item.width = '120px'
      }
      if(item.title == '截止时间'){
        item.width = '160px'
      }
      if(item.title == '填写部门'){
        item.width = '150px'
      }
      if(item.title.includes('K') && item.title.includes('P') && item.title.includes('I')){
        item.fixed = queryParam.showWeek
        item.width = '150px'
      }
      if(item.key.includes("w_")){
        item.width = '95px'
      }
      if (item.children && item.children.length > 0) {
        loopHeadMap(item.children)
      }
    }
  }
    
  const allObjectColoumns = ref([]);
  const monthColoumns = ref([]);
  const personColoumns = ref([]);
  const curveColoumns = ref([])

  function setHeadAndData(columnsErrorRecord) {
    // this.data = []
    allObjectColoumns.value = []
    for (let i = 0; i < columnsErrorRecord.length; i++) {


      //定值为object对象的
      //设列合并以及他们的显示值
      columnsErrorRecord[i].customRender = (value, row, index) => {
          if (typeof value === 'undefined')
            return ''
          // let obj = {};
          let objStr = '';

          let yearColoumns = ['yearTarget', 'lastYear', queryParam.year];

          if(columnsErrorRecord[i].key === 'kpiName'){
            //K.P.I
            objStr = value.value.value;
          }else if(yearColoumns.indexOf(columnsErrorRecord[i].key) > -1){

            let valStr = toFixedValue(value.value.value, value.record)
            objStr = valStr;
          } else {
            objStr = value.value.value;
          }
          return objStr;
      }

      //合并单元格
      columnsErrorRecord[i].customCell=(value, index) => {
        let obj = {}
        if (typeof value[`rowSpan_${columnsErrorRecord[i].dataIndex}`] != 'undefined')
            obj.rowSpan = value[`rowSpan_${columnsErrorRecord[i].dataIndex}`]
        return obj;
      }


      //设置行合并的row  data值
      if(columnsErrorRecord[i].dataIndex === 'lastYear' ||columnsErrorRecord[i].dataIndex === 'yearTarget'
          ||columnsErrorRecord[i].dataIndex === 'unit' ||columnsErrorRecord[i].title === '年累' ){
        setRowYearAndMerge(data.value, columnsErrorRecord[i].dataIndex)
      }else if(columnsErrorRecord[i].dataIndex === 'kpiName'){
        //合并kpi名称
        setRowColObject(data.value, columnsErrorRecord[i].dataIndex)
      }else if(columnsErrorRecord[i].dataIndex === 'type' || columnsErrorRecord[i].dataIndex === 'curve'){
        //不做合并
        //  this.typeColoumns.push(columnsErrorRecord[i])
        //this.setRowYearAndMerge(this.data, columnsErrorRecord[i].dataIndex)
      }else{
        setRowYearAndMerge(data.value, columnsErrorRecord[i].dataIndex)
      }

      //设定部分表格的背景色
      if (columnsErrorRecord[i].children && columnsErrorRecord[i].children.length > 0) {
        columnsErrorRecord[i].children.forEach(itemChildren => {
          //设置颜色
          itemChildren.customCell = (record, rowIndex) => {
            return setCellBg(record, itemChildren.dataIndex)
          }
          itemChildren.scopedSlots = { customRender: itemChildren.dataIndex }
          const disableClickCell = ['possessBy','consultedBy','informedBy','provideBy','deadline','deptName','dutyPossessBy','dutyConsultedBy','dutyInformedBy','dutyProvideBy']  //这几列禁止点击单元格

          if (columnsErrorRecord[i].title === '月累') {
            monthColoumns.value.push(itemChildren.dataIndex)
            itemChildren.slots={ customRender: itemChildren.dataIndex }
          } else if(disableClickCell.includes(itemChildren.dataIndex)){
            //R	A	C	I	截止时间	填写部门
            personColoumns.value.push(itemChildren.dataIndex)
            itemChildren.slots={ customRender: itemChildren.dataIndex }
          }else {
            //其他
            allObjectColoumns.value.push(itemChildren.dataIndex)
            itemChildren.slots={ customRender: itemChildren.dataIndex }
          }
        })
      } else {
        //*****
        if(columnsErrorRecord[i].title === '曲线'){
          //曲线
          //curveColoumns.value.push(columnsErrorRecord[i].dataIndex)
          // columnsErrorRecord[i].scopedSlots = { customRender: columnsErrorRecord[i].dataIndex }
          // columnsErrorRecord[i].slots={ customRender: columnsErrorRecord[i].dataIndex }
        }else{
          columnsErrorRecord[i].scopedSlots = { customRender: columnsErrorRecord[i].dataIndex }
          allObjectColoumns.value.push(columnsErrorRecord[i].dataIndex)
          columnsErrorRecord[i].slots={ customRender: columnsErrorRecord[i].dataIndex }
        }
        
      }
    }
    return columnsErrorRecord
  }

  function toFixedValue(value, record) {
    if(!value && value !== 0 && value !== '0')return '';
    let curData = deepCloneByJSON(record)
    if (/^[+-]?\d*\.?\d*$/.test(value) && value !== '' && value !== null && value !== undefined) {
      let floor = Math.floor(value);
      if (floor === value) {
        return value;
      }
      if(JSON.stringify(curData.decimalNum) === '{}'){
        return Number(value).toFixed(2);
      }

      if(curData && curData.decimalNum && (curData.decimalNum.value === 0 || curData.decimalNum.value === '0'))
        return Number(value).toFixed(Number(curData.decimalNum.value));
      else
        return Number(value).toFixed(Number(curData.decimalNum.value ? curData.decimalNum.value : 2) || 2);
    }else return value;
  }

  function setRowYearAndMerge(data, col){
    for (let i = data.length - 1; i > -1; i--) {
      if(i%2==0){
        if (data[i][col] && (data[i][col].value && data[i + 1][col].value && data[i][col].value.value === data[i + 1][col].value.value)){
           data[i][`rowSpan_${col}`] = 2;
        }else {
           data[i][`rowSpan_${col}`] = 1;
        }
      }else{
        if (data[i][col] && (data[i][col].value && data[i - 1][col].value && data[i][col].value.value === data[i - 1][col].value.value)){
          data[i][`rowSpan_${col}`] = 0;
        }else{
          data[i][`rowSpan_${col}`] = 1;
        }
      }
    }
  }

  //设置合并属性
  function setRowColObject(data, col) { 
    //设定值为object对象的单元格的合并
    let mergeNum = 1;
    if (!col)
      return;
    for (let i = data.length - 1; i > 0; i--) {
      if(!data[i][col]){
        break
      }
      if (data[i][col].value === data[i - 1][col].value) {
        mergeNum++;
        data[i][`rowSpan_${col}`] = 0;
      } else {
        data[i][`rowSpan_${col}`] = mergeNum;
        mergeNum = 1;
      }

      if (i == 1) {
        if (data[1][col].value === data[0][col].value) {
          data[0][`rowSpan_${col}`] = mergeNum
        } else {
          data[0][`rowSpan_${col}`] = 1
        }
      }
    }
  }

  //设置颜色
  function setCellBg(cell, col) {
    let colorObj = {}
    let record = cell[col]
    if (typeof record == 'undefined')
      return { style: {} }

    if (record.colour === 'green') {
      colorObj.bgColor = '#00FF00'
    } else if (record.colour === 'red') {
      colorObj.bgColor = '#FE0000'
    } else if (record.colour === 'yellow') {
      colorObj.bgColor = 'yellow'
    } else {
      colorObj.bgColor = ''
    }
    return {
      style: {
        'backgroundColor': colorObj.bgColor,
        //'color': colorObj.fontColor
      }
    }
  }

  const params = ref({});
  const rowData = ref({});
  const KpiTrendShow = ref(false);
  const isWeekVisible = ref(false);
  const isMounthVisible = ref(false);
  const whyVisible = ref(false); //是否显示右键的5why分析
  //菜单(设置特定选项)
  function showCrue(index, col, record, e) {
    record['colKey'] = col   //当前行的数据传给差距分析弹框
    if (index == 1) {
      params.value["page"] = ''
      record ? rowData.value = record : '', //当前行的数据传给差距分析弹框
      params.value["yearMonth"] = col
      params.value["kpiName"] = record['kpiName'].value
      params.value["year"] = queryParam.year;
      params.value = Object.assign(params, { kpiCode: record['kpiName'].id, reportId: record['kpiObj'].id, recordId: record[col].id })
    } else if (index == 2 && col === "curve") {
      //KPI趋势曲线图
      kpiTrendRef.value.disableSubmit = false;
      params.value["kpiName"] = record['kpiName'].value
      params.value["kpiCode"] = record['kpiName'].id
      params.value["year"] = queryParam.year;
      kpiTrendRef.value.showModal(record,params);
    // } else if (index == 5 && record[col]['colour'] == 'red') {
    } else if (index == 5) {
      record['year'] = queryParam.year  //当前行的数据传给差距分析弹框
      e.stopPropagation();

      const rect = container.value.getBoundingClientRect();
      const scrollX = window.scrollX;
      const scrollY = window.scrollY+13;
      // 计算相对于容器的坐标
      left.value = e.clientX - rect.left + scrollX;
      top.value = e.clientY - rect.top + scrollY;

      params.value["page"] = ''
      record ? rowData.value = record : '', //当前行的数据传给差距分析弹框
      params.value["yearMonth"] = col
      params.value["kpiName"] = record['kpiName'].value
      params.value["year"] = queryParam.year;
      //*****
      params.value = Object.assign(params.value, { kpiCode: record['kpiName'].id, reportId: record['kpiObj'].id, recordId: record.kpiId })
      // this.currentVal="目标值调整",
      rowData.value = record   //当前行的数据传给差距分析弹框
      isWeekVisible.value = true;
      isMounthVisible.value = false;
      visible.value = true;
    }
    // else if(index == 5){
    //   this.params.page = ''
    //   record ? this.rowData = record : '', //当前行的数据传给差距分析弹框
    //   this.params.yearMonth = col
    //   this.params.kpiName = record['kpiName'].value
    //   this.params = Object.assign(this.params, { kpiCode: record['kpiName'].id, reportId: record['kpiObj'].id, recordId: record[col].id })
    //   // text当前单元数据，record 当前行
    //   this.$refs.whyCase5.add('', record, "");
    //   this.$refs.whyCase5.title = "KPI 异常 5Why分析";
    // }
    if(col.includes('yearAccumulate')){
      whyVisible.value = true;
    }
  }
  
  //菜单(设置特定选项)
  function showModal(e, num, col, record) {
    console.log('e:', e)
    e.stopPropagation();
    const rect = container.value.getBoundingClientRect();
    const scrollX = window.scrollX;
    const scrollY = window.scrollY+13;
    // 计算相对于容器的坐标
    left.value = e.clientX - rect.left + scrollX;
    top.value = e.clientY - rect.top + scrollY;

    // top.value = e.clientY
    // left.value = e.clientX
    params.value["yearMonth"] = col
    params.value["kpiName"] = record['kpiName'].value
    params.value["year"] = queryParam.year;
    //****
    params.value = Object.assign(params.value, { kpiCode: record['kpiName'].id, reportId: record['kpiObj'].id, recordId: record.kpiId })
    rowData.value = record   //当前行的数据传给差距分析弹框
    rowData.value['colKey'] = col   //当前行的数据传给差距分析弹框
    isMounthVisible.value = true;
    isWeekVisible.value = false;
    whyVisible.value = false
    visible.value = true;
  }

  //打开5w/差距分析 功能页
  function openModal(e, type) {
    e.stopPropagation();
    //KPI分类
    const fileterObject = queryParam.perentId;
    if (type == 2) {
      console.log("5Why分析")
      //KPI名称：工厂重大火灾事故,周期为：2025-04，目标值：0,实际值：,颜色为：
      let str = "";
      let kpiName = rowData.value.kpiName.value
      let colKey = rowData.value['colKey']
      let kpiId = rowData.value['kpiId']
      
      let range = "";
      if(colKey.includes("w_")){
        range = rangeArray.value[colKey]
      }else{
        range = colKey;
      }
      
      //keyData.value[]
      //目标值
      let target = keyData.value[kpiId+"_1"][colKey].value;
      //实际值
      let actual = keyData.value[kpiId+"_2"][colKey].value;
      let color = '';
      if(rowData.value[colKey].colour == 'green'){
        color= '绿区'
      }else if(rowData.value[colKey].colour == 'red'){
        color= '红区'
      }
      str = 'KPI名称：'+kpiName+'，周期为：'+range+'，目标值：'+target+'，实际值：'+actual+'，颜色为：'+color
      console.log(str+"==============================");
      rowData.value.text = str
      rowData.value.year = year.value
      //KPI 异常 5Why分析 rowData.value
      fiveWhyRef.value.openFiveWhy({
        kpiRowData: rowData.value, 
        kpiParams: params.value, 
        type: 2
      });
    } else if(type == 1) {
      console.log("差距分析")
      kpiTargetModal.value.disableSubmit = false;
      kpiTargetModal.value.showModal(type,rowData,params);
    }else{
      //打开数据修改modal
      openUpdateDataModal(true, {
        isUpdate: false,
        showFooter: true,
        type,
        rowData,
        params
      });
      // this.$refs['modifyData'] && this.$refs['modifyData'].init({...this.rowData, reportId: fileterObject.orgCode})
    }
    visible.value = false;
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
</script>

<style lang="less" scoped>
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
    .ant-form {
      padding: 12px 10px 6px 10px;
      margin-bottom: 8px;
      background-color: #ffffff;
      border-radius: 2px;
    }
    :deep(.ant-picker),:deep(.ant-input-number){
      width: 100%;
    }
  }

//手形
.pointer {
  cursor: pointer;
}

//菜单弹出
.popupModal {
  position: absolute;
  background-color: white;
  display: flex;
  flex-wrap: wrap;
  border-radius: 5px;
  z-index: 1;
  width: 100px;
  border: 1px solid #efefef;

  .item {
    width: 100%;
    text-align: center;
    margin: 5px 0;
    display: inline-block;
  }
}


.cell-with-triangle-left {
  position: relative;
  display: inline-block;
  width: 24px;
  height: 24px;
  /*padding-left: 20px; /* 留出空间给三角角标 */
}

.cell-with-triangle-left-text {
  position: absolute;
  top: 5px;
  /* 调整位置 */
  color: #ffffff;
  line-height: 11px;
  left: 1px;
  font-size: 12px;
  /* 调整位置 */
}
.cell-with-triangle-left::before {
  content: "";
  display: block;
  width: 0;
  height: 0;
  font-size: 12px;
  border-bottom: 24px solid transparent;
  /* 设置透明边 */
  border-left: 24px solid #1969e0;
  /* 设置红色边 */
  position: absolute;
  top: 3px;
  /* 调整位置 */
  left: 1px;
  /* 调整位置 */
}
.table-content{
  position: relative;
}

:deep(.ant-table-thead>tr>th){
  padding: 1px !important;
}
:deep(.ant-table-tbody>tr>td){
  padding: 1px !important;
}
:deep(.selectQuery > .ant-select-single .ant-select-selector){
  border-radius: 4px 0px 0px 4px !important;
}
:deep(.ant-radio-button-wrapper){
  padding-inline: 5px !important;
}
</style>
