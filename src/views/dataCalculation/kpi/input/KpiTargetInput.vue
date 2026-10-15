<template>
  <a-spin :spinning="confirmLoading" class="span-box">
    <div class="box">
      <form-filter-target-option @filterData="searchEvent" @saveData="saveData" ref="filterOption"
        @exportExcel="exportExcel" :saveLoading="loadingButton"></form-filter-target-option>
        <!-- KPI目标值 -->
        <!--Vtable-->
        <div class="table-container" ref="containerRef" style="width:99.9%;height: 100%;">
          <ListTable :options="vtableOption" :height="listTableHeight" ref="listTableRef"/>
        </div>
    </div>
  </a-spin>
  <!--人员选择-->
  <UserSelectModal rowKey="id" @register="registerSelUserModal" @getSelectResult="onSelectOk" :isRadioSelection="true" ref="userSelect"/>
</template>

<script lang="ts" name="kpi-targetInput" setup>
  import { ref, reactive, onMounted } from 'vue';

  import { VTable, ListTable } from '@visactor/vue-vtable';
  import * as VTableEditors from '@visactor/vtable-editors';
  import {downloadExcel,exportVTableToExcel} from "@visactor/vtable-export";
  import FormFilterTargetOption from './formFilterTargetOption.vue';
  import { queryHeader, queryData,save } from './KpiTargetInput.api';
  import { useUserStore } from '/@/store/modules/user';
  import UserSelectModal from '/@/components/Form/src/jeecg/components/modal/UserSelectModal.vue'
  import {useModal} from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useCopyToClipboard } from '/@/hooks/web/useCopyToClipboard';
  const { t } = useI18n();
  // 注册用户选择框 
  const [registerSelUserModal, {openModal}] = useModal()

  // 用户信息
  const userStore = useUserStore();
  const confirmLoading = ref<boolean>(false);
  const queryParamData = ref({
    showWeek:false
  });
  const loadingButton = ref(false);
  //表格初始高度
  const listTableHeight = ref(600);
  const filterOption = ref();
  const { createMessage } = useMessage();
  //编辑数据data集合
  const editDataIndex = ref<any>([]);
  //表头字段集合
  const allColumns = ref<any>([]);
  //当前页数据
  const dataPage = ref<any>([]);
  //修改后右上角标记
  const editMarked = ref({});
  //R A C点击单元格数据临时储存
  const racData = ref({});

  //注册输入框编辑器
  const inputEditor = new VTableEditors.InputEditor();
  VTable.register.editor('input-editor', inputEditor);

  

  //表格实例
  const listTableRef = ref();
  //用户选择
  const userSelect = ref();

  //表格配置(v-table)
  const vtableOption = reactive({
    records: [] as any,
    columns: [] as any,
    enableLineBreak: true, //是否开启换行符解析，开启后，单元格内容中包含换行符时，会自动解析换行
    autoWrapText: true, //文字自动换行
    limitMaxAutoWidth: 1000,
    heightMode: 'autoHeight',
    editCellTrigger: 'doubleclick',
    autoFillWidth: true, //列宽撑满容器
    // widthMode: 'adaptive',
    frozenColCount: 1,
    keyboardOptions: {
      copySelected: true,
      pasteValueToCell: true,
      selectAllOnCtrlA: true,
      moveEditCellOnArrowKeys: true, //开启 tab 键交互 默认为 true。开启 tab 键移动选中单元格，如果当前是在编辑单元格 则移动到下一个单元格也是编辑状态
    },
    // emptyTip: {
    //   text: '暂无数据'
    // },
    theme: VTable.themes.ARCO.extends({
      scrollStyle: {
        visible: 'always',
        hoverOn: false,
        // barToSide: true
      }
    })
    //editor: '' // 配置一个空的编辑器，以遍能粘贴到单元格中
  });

  //查询
  function searchEvent(queryParam) {
    queryParamData.value = queryParam;
    getHaed(queryParam);
    getData(queryParam);
  }
  //保存
  async function saveData(){
    let postDatas = []
    //通过修改id获取数据
    postDatas = dataPage.value.filter(item => {
      if (editDataIndex.value.includes(item.kpiCode)) {
        return true
      }
      return false
    })
    if (postDatas.length < 1) {
      createMessage.warning("暂无被修改的数据");
      return;
    }
    //设置用户信息
    postDatas.forEach((val) => {
      val.factoryCode = userStore.factoryCode;
      val.deptCode = userStore.departCode;
    });
    confirmLoading.value = true;

    // let obj = {
    //   data:postDatas
    // }
    let obj ={
      "targetDatas": postDatas,
      "year": queryParamData.value.year,
      "monthList": queryParamData.value.months
    }

    await save(obj)
    .then((res) => {
      if (res.success) {
          const successMessage = res.message ? res.message : t('language.save');
          createMessage.success(successMessage);
          //searchQuery();
          //查询
          filterOption.value.searchQuery();
          //清空修改记录id
          editDataIndex.value=[];
      } else {
          createMessage.warning(res.message);
          //保存失败刷新数据
          filterOption.value.searchQuery();
      }
    })
    .finally(() => {
      confirmLoading.value = false;
    });


  }
  //导出
  async function exportExcel(){
    console.log("导出------------------------");
    const listTableInstance = listTableRef.value;
    if (!listTableInstance) {
      createMessage.warning("ListTable 实例未创建或未挂载");
    }
    //获取 VTable 实例的数据
    const tableInstance = listTableInstance.vTableInstance;
    downloadExcel(await exportVTableToExcel(tableInstance), "KPI目标值");
  }

  onMounted(() => {
    //获取页面高度
    listTableHeight.value = (document.documentElement.scrollHeight || document.body.scrollHeight)-290;
    const currentDate = new Date();
    // // 使用 getFullYear() 方法获取四位数的年份
    const currentYear = currentDate.getFullYear();
    // const currentMonth = currentDate.getMonth()+1;
    let queryParam = {
      // year: currentYear+"-"+(currentMonth>=10?currentMonth:"0"+currentMonth)
      year: currentYear,
      showWeek: false
    }
    queryData.value = queryParam;
    getHaed(queryParam);
    //filterOption.value.searchQuery();
    //获取数据
    getData(queryParam);

    // 初始化VTable事件(修改事件)
    listTableRef.value.vTableInstance.on('change_cell_value', arg => {
      // if(!arg.changedValue || arg.changedValue == arg.rawValue){
      if(arg.changedValue == arg.rawValue){
        return false;
      }
      handleChangeVtable(arg.changedValue,arg.row,arg.col);
    });
    // 初始化VTable事件(点击事件)
    listTableRef.value.vTableInstance.on('click_cell', arg => {
      if(arg.field=="provideByText" || arg.field=="possessByText" || arg.field=="consultedByText" ||
        arg.field=="dutyProvideByText" || arg.field=="dutyPossessByText" || arg.field=="dutyConsultedByText"){
        //let field = allColumns.value[arg.col+1]; //加上序号行
        // let index = arg.row-1; //减去表头层数
        let index = arg.row-2; //减去表头层数
        // if(queryParamData.value.showWeek){
        //   //显示周,减去2层
        //   index = arg.row-2;
        // }
       
        let record = dataPage.value[index];
        //changeUserValue(record, arg.field, index);
        racData.value ={
          //当前行数据
          record: record,
          //单元格列名称
          field: arg.field,
          //数据行号
          index: index,
          //vTable行
          row: arg.row,
          //vTable列
          col: arg.col
        }
        openModal()
      }
    });
    //checkbox选中事件
    listTableRef.value.vTableInstance.on('checkbox_state_change', arg => {
      handleChangeVtable(arg.checked,arg.row,arg.col);
    });
    //复制事件
    listTableRef.value.vTableInstance.on('copy_data', arg => {
      console.log("复制:"+arg.copyData)
      copy(arg.copyData);
    })
  })

  //获取表头
  function getHaed(queryParam){
    allColumns.value = [];
    editMarked.value = {};
    let obj = {
      months: queryParam.months,
      year: queryParam.year,
      showWeek: queryParam.showWeek,
      showKpiDuty: queryParam.showKpiDuty,
    }
    queryHeader(obj).then((res) => {
      let dataArr = res.result;
      allColumns.value.push('');
      dataArr[0].field = dataArr[0].key;
      setWidth(dataArr[0])
      //设置编辑属性 从1开始排查KPI列
      for (let i = 1; i < dataArr.length; i++) {
          const item = dataArr[i];
          item.field = item.key;
          setWidth(item)
          // 检查 columns 属性是否存在且不为空
          if (item.children && item.children.length > 0) {
              item.columns = item.children;
              // 遍历 columns 数组中的每个对象
              for (let j = 0; j < item.columns.length; j++) {
                  const columnItem = item.columns[j];
                  columnItem.field = columnItem.key;
                  //设置宽度
                  setWidth(columnItem)

                  //第二层
                  if(columnItem.field == 'provideBy' || columnItem.field == 'possessBy' || columnItem.field == 'consultedBy' ||
                    columnItem.field=="dutyProvideBy" || columnItem.field=="dutyPossessBy" || columnItem.field=="dutyConsultedBy"){
                    //不做录入
                    //名称转换现在人员姓名
                    if(columnItem.field == "provideBy"){
                      columnItem.field = "provideByText"
                    }
                    if(columnItem.field == "possessBy"){
                      columnItem.field = "possessByText"
                    }
                    if(columnItem.field == "consultedBy"){
                      columnItem.field = "consultedByText"
                    }


                    if(columnItem.field == "dutyProvideBy"){
                      columnItem.field = "dutyProvideByText"
                    }
                    if(columnItem.field == "dutyPossessBy"){
                      columnItem.field = "dutyPossessByText"
                    }
                    if(columnItem.field == "dutyConsultedBy"){
                      columnItem.field = "dutyConsultedByText"
                    }

                    columnItem.style = (args)=>{
                      let styleObj = {};
                      //设置是否显示单元格标记 marked: true
                      //判断是否修改-添加修改后标注
                      if(editMarked.value[args.row+"_"+args.col]){
                        styleObj.marked = true;
                      }
                      return styleObj;
                    }
                    allColumns.value.push(columnItem.field);
                  }else{
                    // 为每个对象添加 editor 属性
                    columnItem.editor = 'input-editor';
                    allColumns.value.push(columnItem.field);

                    //添加编辑后右上角角标
                    columnItem.style = (args)=>{
                      let styleObj = {};
                      //设置是否显示单元格标记 marked: true
                      //判断是否修改-添加修改后标注
                      // let colKey = allColumns.value[args.col+1]; //加上序号行
                      // let data = listTableRef.value.vTableInstance.getRecordByCell(args.col,args.row);
                      if(editMarked.value[args.row+"_"+args.col]){
                        styleObj.marked = true;
                      }
                      return styleObj;
                    }
                  }
              }
          }else{
            //第一层
            // if(item.field == 'provideBy' || item.field == 'possessBy' || item.field == 'consultedBy'){
            //   //不做录入
            //   //名称转换现在人员姓名
            //   if(item.field == "provideBy"){
            //     item.field = "provideByText"
            //   }
            //   if(item.field == "possessBy"){
            //     item.field = "possessByText"
            //   }
            //   if(item.field == "consultedBy"){
            //     item.field = "consultedByText"
            //   }

            //   item.style = (args)=>{
            //     let styleObj = {};
            //     //设置是否显示单元格标记 marked: true
            //     //判断是否修改-添加修改后标注
            //     if(editMarked.value[args.row+"_"+args.col]){
            //       styleObj.marked = true;
            //     }
            //     return styleObj;
            //   }
            // }else 
            if(item.field ==='copyYearTarget' || item.field ==='copyMonthTarget'){
                //复选框
                item.cellType= 'checkbox'
                item.style= {
                  textAlign: 'center',
                }
            }else{
              //编辑框
              item.editor = 'input-editor';
              //添加编辑后右上角角标
              item.style = (args)=>{
                let styleObj = {};
                //设置是否显示单元格标记 marked: true
                //判断是否修改-添加修改后标注
                if(editMarked.value[args.row+"_"+args.col]){
                  styleObj.marked = true;
                }
                return styleObj;
              }
            }
            allColumns.value.push(item.field);
          }
      }
      vtableOption.columns = dataArr;
    })
  }
  //设置宽度
  function setWidth(item){
    if(item.key == 'kpiName'){
      item.width = 260
    }else{
      item.width = 110
    }
  }
  //获取数据
  function getData(queryParam){
    confirmLoading.value = true;
    let query = deepCloneByJSON(queryParam);
    

    query.factory = userStore.factoryCode;
    // queryParam.dept = userStore.departCode;
    query.dept = userStore?.userInfo?.orgCode;
    query.reportId = userStore?.userInfo?.orgCode;
    query.typeId = queryParam.perentId;
    query.deptQuery = queryParam.deptCodeStr;
    query.inputDeptQuery = queryParam.inputDeptQueryStr
    //userStore.departCode
    //queryData({ month: queryParam.year,factory:factory,dept: dept,reportId:dept,typeId:queryParam.perentId,deptQuery:queryParam.deptCode}).then((res) => {
    queryData(query).then((res) => {
        if(res.result && res.result.length > 0){
          vtableOption.records = res.result;
          dataPage.value = res.result;
          confirmLoading.value = false;
        }else{
          vtableOption.records =[];
          dataPage.value = [];
          confirmLoading.value = false;
          vtableOption.emptyTip = {
            text: '暂无数据'
          }
        }
    })
  }

  //深度拷贝
  function deepCloneByJSON(obj) {
    return JSON.parse(JSON.stringify(obj));
  }

  //编辑后出发事件 值  行(固定列+数据)  列
  function handleChangeVtable(value, row, col) {
    let colKey = allColumns.value[col];
    let data = listTableRef.value.vTableInstance.getRecordByCell(col,row);
    //右上角标注
    //data[colKey+"_marked"] = true
    editMarked.value[row+"_"+col] = true;
    // editDataIndex.value.push(data.id);
    if(isMoon(colKey)){
          // 批量行修改
          //判断是否需要批量修改
          // let colKeyMom = this.allColumns[col-4];//同步月目标key
          // if(data['copyMonthTarget'] === 1){
            // let colKeyOne = allColumns.value[col];
            // let colKeyTwo = allColumns.value[col+1];
            // let colKeyThree = allColumns.value[col+2];
            // let colKeyFour = allColumns.value[col+3];
            // let colKeyFive = allColumns.value[col+4];
            // if(colKeyFive != 'provideBy'){
            //   data[colKeyFive] = value;
            // }
            // data[colKeyOne] = value;
            // data[colKeyTwo] = value;
            // data[colKeyThree] = value;
            // data[colKeyFour] = value;
            // data['copyMonthTarget'] = null
            // let editData = [data];//数据
            // let editIndex = [row-2];//列
            // if(editData.length > 0 && editData.length > 0){
            //   listTableRef.value.vTableInstance.updateRecords(editData,editIndex);
            // }
            // data['copyMonthTarget'] = 1
          // }
          
          data[colKey] = value;
          editDataIndex.value.push(data.kpiCode);
        }else{
          if (data) {
            if(colKey == 'copyMonthTarget'){
              //同步月目标
              if(value){
                data[colKey] =1
              }else{
                data[colKey] = null
              }
            }else if(colKey == 'copyYearTarget'){
              //同步年目标
              if(value){
                data[colKey] = 1
              }else{
                data[colKey] = null
              }
            }else{
              data[colKey] = value;
            }
            editDataIndex.value.push(data.kpiCode);
          }
        }
  }
  //用户选择后更新
  function onSelectOk(options, values){
    // console.log(userSelect.value.selectRows);
    // console.log(options)
    if(userSelect.value.selectRows && userSelect.value.selectRows.length > 0){
      let data = userSelect.value.selectRows[0]
      let field = racData.value["field"]
      let index = racData.value["index"]
      //人员信息变更
      racData.value["record"][field.replace("Text","")] = data.id;
      racData.value["record"][field] = data.realname;
      //修改记录
      // editDataIndex.value.push(racData.value["record"].id);
      editDataIndex.value.push(racData.value["record"].kpiCode);

      //右上角标注
      editMarked.value[racData.value["row"]+"_"+racData.value["col"]] = true;
      //原始数据变更
      dataPage.value[index] = racData.value["record"]

      //vTable数据变更
      listTableRef.value.vTableInstance.updateRecords([racData.value["record"]],[index]);
    }
  }

  //判断是否是月累计
  function isMoon(key){
    let months = ["janValue", "febValue", "marValue", "aprValue", "mayValue", "junValue", "julValue", "augValue", "sepValue", "octValue", "novValue", "decValue"];
    for (let month of months) {
      if (key.includes(month)) {
        return true;
      }
    }
    return false;
  }

  //复制内容存到剪贴板
  async function copy(value){
    useCopyToClipboard(value);
  }
</script>

<style lang="less" scoped>
  .box{
    background-color: #fff;
    :deep(.jeecg-basic-table-form-container .ant-form) {
      padding: 0px 10px 0px 10px;
      margin-bottom: 0px;
    }
  }
</style>
