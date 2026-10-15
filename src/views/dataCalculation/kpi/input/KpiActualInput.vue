<template>
  <a-spin :spinning="confirmLoading" class="span-box">
    <div class="box">
      <form-filter-option @filterData="searchEvent" @saveData="saveData" ref="filterOption"
        @exportExcel="exportExcel" :saveLoading="loadingButton"></form-filter-option>
      <!-- KPI实际值 -->
      <!--Vtable-->
      <div class="table-container" ref="containerRef" style="width:99.9%;height: 100%;">
        <ListTable :options="vtableOption" :height="listTableHeight" ref="listTableRef"/>
      </div>
    </div>
  </a-spin>
</template>

<script lang="ts" name="kpi-actualInput" setup>
  import { ref, reactive,onMounted } from 'vue';
  
  // import { BasicTable, useTable, TableAction } from '/@/components/Table';
  // import { useListPage } from '/@/hooks/system/useListPage';
  // import { downloadFile } from '/@/utils/common/renderUtils';
  // import KpiReportActualDayValueModal from './components/KpiReportActualDayValueModal.vue'
  import { VTable, ListTable } from '@visactor/vue-vtable';
  import * as VTableEditors from '@visactor/vtable-editors';
  import {downloadExcel,exportVTableToExcel} from "@visactor/vtable-export";
  import FormFilterOption from './formFilterOption.vue';
  import { queryHeader, queryData ,save} from './KpiActualInput.api';
  import { useUserStore } from '/@/store/modules/user';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useCopyToClipboard } from '/@/hooks/web/useCopyToClipboard';

  const { t } = useI18n();
  // 用户信息
  const userStore = useUserStore();
  const confirmLoading = ref<boolean>(false);
  
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

  //注册输入框编辑器
  const inputEditor = new VTableEditors.InputEditor();
  VTable.register.editor('input-editor', inputEditor);

  //表格实例
  const listTableRef = ref();
  //表格配置(v-table)
  const vtableOption = reactive({
    records: [] as any,
    columns: [] as any,
    enableLineBreak: true,
    autoWrapText: true,
    limitMaxAutoWidth: 1000,
    heightMode: 'autoHeight',
    editCellTrigger: 'doubleclick',
    autoFillWidth: true, //列宽撑满容器
    // widthMode: 'adaptive',
    frozenColCount: 1,
    keyboardOptions: {
      copySelected: true,
      pasteValueToCell: true,
      selectAllOnCtrlA: true
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
    getHaed(queryParam);
    getData(queryParam);
  }
  //保存
  async function saveData(){
    let postDatas = []
    //通过修改id获取数据
    postDatas = dataPage.value.filter(item => {
      item.factoryCode = userStore.factoryCode;
      if (editDataIndex.value.includes(item.kpiCode)) {
        return true
      }
      return false
    })
    if (postDatas.length < 1) {
      createMessage.warning("暂无被修改的数据");
      return;
    }

    confirmLoading.value = true;

    // let obj = {
    //   data:postDatas
    // }
    await save(postDatas)
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
           //清空修改记录id
           editDataIndex.value=[];
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
    downloadExcel(await exportVTableToExcel(tableInstance), "KPI实际值");
  }


  onMounted(() => {
    //获取页面高度
    listTableHeight.value = (document.documentElement.scrollHeight || document.body.scrollHeight)-290;
    const currentDate = new Date();
    // // 使用 getFullYear() 方法获取四位数的年份
    const currentYear = currentDate.getFullYear();
    const currentMonth = currentDate.getMonth()+1;
    let queryParam = {
      year: currentYear+"-"+(currentMonth>=10?currentMonth:"0"+currentMonth)
    }
    getHaed(queryParam);
    //filterOption.value.searchQuery();
    getData(queryParam);

    // 初始化VTable事件(修改事件)
    listTableRef.value.vTableInstance.on('change_cell_value', arg => {
      // if(!arg.changedValue || arg.changedValue == arg.rawValue){
      if(arg.changedValue == arg.rawValue){
        return false;
      }
      handleChangeVtable(arg.changedValue,arg.row,arg.col);
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
    queryHeader({ month: queryParam.year }).then((res) => {
      let dataArr = res.result;
      allColumns.value.push('');
      for (let i = 0; i < dataArr.length; i++) {
          const item = dataArr[i];
          item.field = item.key;
          setWidth(item);
          // item.width = "auto";
          // 检查 columns 属性是否存在且不为空
          if (item.children && item.children.length > 0) {
              item.columns = item.children;
              // 遍历 columns 数组中的每个对象
              for (let j = 0; j < item.columns.length; j++) {
                  const columnItem = item.columns[j];
                  columnItem.field = columnItem.key;
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
      }
      vtableOption.columns = dataArr;
    })
  }
  //设置宽度
  function setWidth(item){
    if(item.key == 'kpiName'){
      item.width = 100
    }
  }
  //获取数据
  function getData(queryParam){
    confirmLoading.value = true;
    let factory = userStore.factoryCode;
    // let dept = userStore.departCode;
    let dept = userStore?.userInfo?.orgCode;

    //userStore.departCode
    queryData({ month: queryParam.year,dept: dept,reportId:dept,factory,typeId:queryParam.perentId,
      deptQuery:queryParam.deptCodeStr,kpiName:queryParam.kpiName,inputDeptQuery:queryParam.inputDeptQueryStr}).then((res) => {
    // queryData(queryParam).then((res) => {
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

  //编辑后出发事件 值  行(固定列+数据)  列
  function handleChangeVtable(value, row, col) {
    // let colKey = allColumns.value[col+1]; //加上序号行
    let data = listTableRef.value.vTableInstance.getRecordByCell(col,row);
    //右上角标注
    // data[colKey+"_marked"] = true
    // editDataIndex.value.push(data.id);
    editMarked.value[row+"_"+col] = true;
    editDataIndex.value.push(data.kpiCode);
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
