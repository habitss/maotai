<template>
  <a-spin :spinning="confirmLoading" class="span-box">
    <div>
      <!--查询区域-->
      <div class="jeecg-basic-table-form-container">
        <a-form @keyup.enter.native="searchQuery" :model="queryParam" :label-col="labelCol" :wrapper-col="wrapperCol" labelAlign="left">
          <a-row :gutter="24">
            <a-col :lg="8">
              <a-form-item label="报表">
                <a-select v-model:value="queryParam.allocationTableId" :options="tableArr" placeholder="请选择" @change="tableChange"  allowClear="true" :disabled="selectTable"></a-select>
              </a-form-item>
            </a-col>
            
            <template v-if="toggleSearchStatus">
              <a-col :lg="8" v-for="(val, index) in queryArr" :key="index">
                <a-form-item :label="val.name">
                  <!--近10条数据-->
                  <a-select v-model:value="queryParam[val.fieldName+'_tag']" v-if="val.historyData=='1'"
                  mode="tags" :max-count="1" style="width: 100%" placeholder="请选择" allow-clear :options="historyOptions(val.fieldName)" 
                  @change="handleSingleTagChange(queryParam[val.fieldName+'_tag'], val.fieldName)"></a-select>
                  
                  <!--时间-->
                  <a-range-picker class="date-pick" v-else-if="val.type=='6'" :ranges="{}" format="YYYY-MM-DD HH:mm:ss" v-model:value="rangePickerTime[val.fieldName]" @change="onChangeTime(val.fieldName)" show-time/>              
                  <!--日期-->
                  <a-range-picker class="date-pick" v-else-if="val.type=='4'" :ranges="{}" format="YYYY-MM-DD" v-model:value="rangePickerDate[val.fieldName]" @change="onChangeDate(val.fieldName)" />
                  
                  <!--下拉框-->
                  <kt-dict
                    v-else-if="(val.type=='1' || val.type=='2' || val.type=='5') &&val.editWidgetType=='select' && val.editWidgetType!='input' && val.editWidgetType!='selectUser'" 
                    v-model:value="queryParam[val.fieldName]"
                    placeholder="请选择"
                    :dict="val.dropdownData"
                    :labelInValueProp="true"
                  />
                  <!--输入框-->
                  <a-input placeholder="请输入" v-else-if="val.type=='1' || val.type=='2' || val.type=='5'" v-model:value="queryParam[val.fieldName]" />
                  <!--人员-->
                  <JDictSelectTag  v-else v-model:value="queryParam[val.fieldName]" placeholder="请选择用户名称" dictCode="sys_user,realname,realname" />
                </a-form-item>
              </a-col>
            </template>

            <a-col :xl="12" :lg="12" :md="12" :sm="12">
              <span style="float: left; overflow: hidden; margin-left: 5px" class="table-page-search-submitButtons">
                <!-- <a-col :lg="6"> -->
                  <a-button type="primary" preIcon="ant-design:search-outlined" @click="searchQuery">{{ t('language.search') }}</a-button>
                  <a-button type="primary" preIcon="ant-design:sync-outlined" v-auth="'pi:mes3_pi:recalculation'" @click="rerun(1)" style="margin-left: 8px">PI重算</a-button>
                  <a-button type="primary" preIcon="ant-design:sync-outlined" v-auth="'pi:mes3_pi:red_recalculation'" @click="rerun(2)" style="margin-left: 8px">红黄区重算</a-button>
                  <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleAdd()" style="margin-left: 8px">新增</a-button>
                  <a-button type="primary" preIcon="ant-design:save-outlined" @click="saveData()" style="margin-left: 8px">保存</a-button>
                  <a-button type="primary" preIcon="ant-design:export-outlined" @click="exportData()" style="margin-left: 8px">导出</a-button>
                  <a-dropdown v-if="delDataIndex.length > 0">
                    <template #overlay>
                      <a-menu>
                        <a-menu-item key="1" @click="batchHandleDelete">
                          <Icon icon="ant-design:delete-outlined"></Icon>
                          删除
                        </a-menu-item>
                      </a-menu>
                    </template>
                    <a-button>批量操作
                      <Icon icon="mdi:chevron-down"></Icon>
                    </a-button>
                  </a-dropdown>
                  <a @click="toggleSearchStatus = !toggleSearchStatus" style="margin-left: 8px">
                    {{ toggleSearchStatus ? t('language.collapse') : t('language.open') }}
                    <Icon :icon="toggleSearchStatus ? 'ant-design:up-outlined' : 'ant-design:down-outlined'" />
                  </a>
                <!-- </a-col> -->
              </span>
            </a-col>

          </a-row>
        </a-form>
        <!--Vtable-->
        <div class="table-container" ref="containerRef" style="width:100%;height: 100%;">
          <!-- <ListTable :options="vtableOption" :height="listTableHeight" ref="listTableRef" v-if="isList">
            <Menu menuType="html" :contextMenuItems="getMenuConfig" />
          </ListTable> -->

          <ListTableHead ref="tableHeadRef" @rightClick="rightClickEvent" @checkboxChange="handleCheckboxChange"/>
          <!--分页-->
          <div style="text-align: right;padding: 10px 5px;">
            <a-pagination
              v-model:current="pageNo"
              size="small"
              :total="totalRecord"
              :pageSize="pageSize"
              show-size-changer
              show-quick-jumper
              @change="onPageChange"
              :page-size-options="pageSizeOptions"
              @showSizeChange="onShowSizeChange"
              :show-total="total => `共 ${total} 条数据`" 
            />
          </div>
          
        </div>

      </div>
    </div>
  </a-spin> 

  <!--重算/红黄区-->
  <a-modal
    width="1000px"
    :title="title" 
    :visible="visible"
    @ok="rerunOk"
    okText="确认重算"
    @cancel="rerunClose"
    cancelText="关闭"
  >
  <div style="margin: 8px;">
    <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol">
        <a-row>
          <a-col :span="24" v-if="rerunType === 1">
            <a-form-item label="字段" v-bind="validateInfos.fields">
              <div style="display: flex;align-items: center;">
                <a-select
                 show-search 
                 option-filter-prop="label"
                 v-model:value="formData.fieldIds"
                 mode="multiple"
                 style="width: 100%"
                 placeholder="请选择字段"
                 :options="fieldArr"
                 allowClear
                ></a-select>
                <CheckSquareOutlined title="全选" style="margin-left: 5px;font-size: 20px;" @click="selectAll"/>
              </div>
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item label="范围" v-bind="validateInfos.times">
              <a-range-picker v-model:value="formData.times" class="date-pick" :ranges="{}" format="YYYY-MM-DD HH:mm:ss"  style="width: 100%" show-time/>
            </a-form-item>
          </a-col>
        </a-row>
    </a-form>
  </div>
  </a-modal>
  
  <!--PI新增-->
  <PiDataForm ref="registerModal" @success="handleSuccess"></PiDataForm>
  <!--PI审批-->
  <ApproveModal ref="approveModal" @success="handleSuccess"></ApproveModal>
  <!--标准值-->
  <ManageStandardModal ref="manageStandardModal" :width="1200" @success="handleSuccess" :key="dayjs().valueOf()" />
  <!--5why分析-->
  <FiveWhyList ref="fiveWhyRef" @success="handleSuccess"/>
  <!--异常分析-->
  <AbnormalList ref="abnormalRef" @success="handleSuccess"></AbnormalList>
  <!--图例modal -->
  <OpenChartModel ref="chartModalRef" @success="handleSuccess"></OpenChartModel>
</template>

<script lang="ts" name="pi-tableHead" setup>
  import { VTable, ListTable, Menu } from '@visactor/vue-vtable';
  import {downloadExcel,exportVTableToExcel} from "@visactor/vtable-export";
  import * as VTableEditors from '@visactor/vtable-editors';
  import { ref, reactive, onMounted,nextTick  } from 'vue';
  import { buildUUID } from '/@/utils/uuid';
  import {list as dataList,head,allocationTableList,batchEditVal,saveOrUpdate,rerunFun,batchDelete,getAlgorithmField,getTableFieldData} from '@/views/dataCalculation/pi/allocationTable/QueryTable.api';
  import {customizeTableList,head as baseHead,list as baseDataList} from '/@/views/dataCalculation/pi/customizeTable/QueryTable.api';
  import { getById as getTemplateRecord } from '/@/views/dataCalculation/customReport/reportTemplate/index.api';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useRoute } from 'vue-router';
  import { Form } from 'ant-design-vue';
  import { CheckSquareOutlined } from '@ant-design/icons-vue';
  import EditorDatePicker from '@/views/dataCalculation/pi/allocationTable/components/EditorDatePicker';
  import SelectEditor from '@/views/dataCalculation/pi/allocationTable/components/EditorSelect';
  import SelectUserEditor from '@/views/dataCalculation/pi/allocationTable/components/EditorSelectUser';
  import dayjs from 'dayjs';
  import KtDict from '/@/components/KtDict/KtDict.vue';
  import { Pagination } from 'ant-design-vue';
  import PiDataForm from '@/views/dataCalculation/pi/allocationTable/components/PiDataModal.vue';
  import ApproveModal from '@/views/dataCalculation/pi/approve/components/ApproveModal.vue';
  import ManageStandardModal from '/@/views/dataCalculation/pi/standardValue/components/manageStandardModal.vue';
  import { getColor} from '/@/utils/common/piColorUtils';
  //5w分析
  import FiveWhyList from '/@/views/dataCalculation/fiveWhy/fiveWhy.vue';
  // 异常分析
  import AbnormalList from '/@/views/dataCalculation/abnormal/abnormal.vue'
  //图例modal
  import OpenChartModel from '@/views/dataCalculation/pi/allocationTable/components/OpenChartModel.vue'
  import { useCopyToClipboard } from '/@/hooks/web/useCopyToClipboard';
  import { getTableHead } from '@/views/dataCalculation/pi/allocationTable/AllocationTable.api';
  import ListTableHead from '@/views/dataCalculation/pi/allocationTable/components/ListTableHead.vue';
  import { isLiteralExpression } from 'typescript';
  import { right } from 'inquirer/lib/utils/readline';
  const APagination = Pagination;
  const { t } = useI18n();

  //报表类型
  const tableParams = reactive({
    type: "2",   //1.基础报表 2.PI报表
    tableId: "", //报表id
    allocationId: "",//PI日报id
    templateId: "",//模板id
  });
  //表头字段-名称集合
  const headField = ref<any>({});
  //是否显示原始表,true 原始表 ,false 自定义表头 
  const isList = ref(true);
  //自定义表头
  const tableHeadRef = ref();
  //查询条件
  const queryParam = ref<any>({});
  const toggleSearchStatus = ref<boolean>(false);
  //PI新增组件
  const registerModal = ref();
  //修改/剔除
  const approveModal = ref();
  //标准
  const manageStandardModal =ref();
  //5w
  const fiveWhyRef =ref();
  // 异常
  const abnormalRef = ref()
  // 图例ref对象
  const chartModalRef = ref()
  // const userStore = useUserStore();
  const { createMessage } = useMessage();
  const confirmLoading = ref<boolean>(false);
  //最新前几条数据
  const historyData = ref({});
  //复选
  const selectedRowKeys = ref([]) as any;
  //需要显示的编辑组件
  const showEditArr = ref([]) as any;
  //报表是否可择
  const selectTable = ref<boolean>(false);
  //获取路由
  const route = useRoute();
  //获取路由中的code参数
  const code = route.params.code;
  //获取路由中的type参数 默认2
  const type = '2';
  //表格初始高度
  const listTableHeight = ref(600);

  //需要获取最新前几条数据的字段集合
  const historyDataArr = ref([]);
  //是否表
  const multipleTables = ref("0")
  //************VTable************/
  //表格实例
  const listTableRef = ref();
  //分页
  const totalRecord = ref(0);
  const pageNo = ref(1);
  const pageSize = ref(20);
  const pageSizeOptions = ref<string[]>(['20', '50', '100', '200', '300', '500']);
  const onShowSizeChange = (current: number, pageSizeNew: number) => {
    pageSize.value = pageSizeNew;
  };
  //分页事件
  function onPageChange(){
    searchQuery();
  }
  //表格组件数据(通过表头获取)
  const cellComponentsList = ref([] as any);
  //注册输入框编辑器
  const inputEditor = new VTableEditors.InputEditor();
  VTable.register.editor('input-editor', inputEditor);
  //注册日期选择编辑器
  const EditorDatePickerObject = new EditorDatePicker('date');
  VTable.register.editor('date-editor', EditorDatePickerObject);
  //注册日期时间选择编辑器
  const EditorDateTimePickerObject = new EditorDatePicker('dateTime');
  VTable.register.editor('date-time-editor', EditorDateTimePickerObject);
  //注册下拉框编辑器
  const SelectEditorObject = new SelectEditor(cellComponentsList.value);
  VTable.register.editor('select-editor', SelectEditorObject);
  //注册用户下拉框编辑器
  const SelectUserEditorObject = new SelectUserEditor();
  VTable.register.editor('select-user-editor', SelectUserEditorObject);

  //字段
  const columns = ref([]) as any;
  //表格配置
  const vtableOption = reactive({
    records: [] as any,
    columns: [] as any,
    enableLineBreak: true,
    autoWrapText: true,
    limitMaxAutoWidth: 600,
    heightMode: 'autoHeight',
    editCellTrigger: 'doubleclick',
    // widthMode: 'adaptive',
    frozenColCount: 1,
    keyboardOptions: {
      copySelected: true,
      pasteValueToCell: true,
      selectAllOnCtrlA: true
    },
    theme: VTable.themes.ARCO.extends({
      scrollStyle: {
        visible: 'always',
        hoverOn: false,
        // barToSide: true
      }
    }),
    customRender:customRenderFun,
    //editor: '' // 配置一个空的编辑器，以遍能粘贴到单元格中
  });
  //表头字段集合
  const allColumns = ref<any>([]);
  //当前页数据
  const dataPage = ref<any>([]);
  //编辑数据data集合
  const editDataIndex = ref<any>([]);
  //编辑字段集合
  const editFieldSet = ref<Set<any>>(new Set());
  //删除数据集合
  const delDataIndex = ref<any>([]);

  //定义图标(编辑)
  const headerIcon = ref<any>({
    type: 'svg', //指定svg格式图标，其他还支持path，image
    svg: `<svg t="1736418214797" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="4272" width="200" height="200"><path d="M858.048 735.104a167.872 167.872 0 0 1-167.232 159.68H295.424a167.808 167.808 0 0 1-167.232-159.68H128V287.808h0.192A167.872 167.872 0 0 1 295.424 128H628.48l-72.192 60.928H295.424a107.136 107.136 0 0 0-106.496 107.008v430.976c0 58.88 47.872 106.88 106.496 106.88h395.392c58.752 0 106.496-48 106.496-106.88V425.344l60.8-59.968v369.728h-0.064z" fill="#888888" p-id="4273"></path><path d="M359.744 643.648a40.32 40.32 0 0 0 57.088 0L858.24 202.24a40.448 40.448 0 0 0 0-57.216l-0.128-0.064a40.32 40.32 0 0 0-56.96 0.064L359.68 586.432a40.448 40.448 0 0 0 0 57.216z" fill="#888888" p-id="4274"></path></svg>`,
    width: 20,
    height: 20,
    name: 'frozenCurrent', //定义图标的名称，在内部会作为缓存的key值
    positionType: VTable.TYPES.IconPosition.right,
    marginRight: 0,
    hover: {
      // 热区大小
      width: 22,
      height: 22,
      bgColor: 'rgba(101, 117, 168, 0.1)'
    },
    cursor: 'pointer'
  });

  //报表集合
  const tableArr = reactive<Record<string, any>>([]);
  //查询条件集合
  const queryArr = reactive<Record<string, any>>([]);

  //分配字段
  const hasReopen = ref<boolean>(false);
  const departObject = ref<Object>({});

  //查询条件-日期
  const rangePickerDate = ref<Object>({});
  //查询条件-时间
  const rangePickerTime = ref<Object>({});


  //#####################重算#####################
  //重算类型
  const rerunType = ref<Number>(1);
  //重算地址modal
  const visible = ref<boolean>(false);
  //重算modal标题
  const title = ref<string>('');
  //表单
  const formRef = ref();
  const useForm = Form.useForm;
  const formData = reactive<Record<string, any>>({
    id: '',
    deptCode: '',
    lineCode: '',
    times:[],
  });
  //表单验证
  const validatorRules = {
    fields: [{ required: true, message: `字段不为空!` }],
    times: [{ required: true, message: `范围不为空!` }],
      // stepName: [{ required: true, message: `步骤名称不为空!` }],
  };
  const formDataNew = reactive<Record<string, any>>({});
  const { resetFields, validate, validateInfos } = useForm(formDataNew, validatorRules, { immediate: true });
  //字段集合
  const fieldArr = reactive<Record<string, any>>([]);
  //自定义报表id
  const customReportTableId = ref<string>('');
  //自定义报表名称(表名)
  const customReportTableName = ref<string>('');
  //自定义报表中文名称()
  const customReportName = ref<string>('');
  //自定义报表部门名称
  const customReportDeptName = ref<string>('');
  //自定义报表产线名称
  const customReportLineName = ref<string>('');
  //字段标准值集合数据
  const piStandardMaintenanceList = ref<any>([]);
  
  const labelCol = reactive({
    xs: { span: 24 },
    sm: { span: 5 },
  });
  const wrapperCol = reactive({
    xs: { span: 24 },
    sm: { span: 16 },
  });

  async function editVal(column, record){
    const parameter = {
      id: record.id,
      tableId: queryParam.value.allocationTableId,
      value: record[column.key],
      key: column.key
    }
    confirmLoading.value = true;
    await saveOrUpdate(parameter, true)
        .then((res) => {
        if (res.success) {
          createMessage.success(res.message);
        } else {
          //保存失败需要刷新
          handleSuccess()
          createMessage.warning(res.message);
        }
    }).finally(() => {
      confirmLoading.value = false;
      handleEditCancle(column, record);
    });
  }

  //点击编辑
  function handleEditClick(column, record){
    showEditArr.value.push(record.id+'_'+column.key+'_'+column.editType);
    showEditArr.value.push(record.id+'_'+column.key+'_icon');
  }

  //是否显示编辑
  function handleShowEdit(column, record, type){
    const key = record.id+'_'+column.key+'_'+type;
    return showEditArr.value.includes(key);
  }

  //保存编辑
  function handleSave(column, record){
    editVal(column, record);
  }

  //取消编辑
  function handleEditCancle(column, record){
    const key = record.id+'_'+column.key+'_'+column.editType;
    const icon = record.id+'_'+column.key+'_icon';
    showEditArr.value = showEditArr.value.filter(item => item != key && item !== icon);
  }

  onMounted(() => {
    //获取页面高度
    listTableHeight.value = (document.documentElement.scrollHeight || document.body.scrollHeight)-260;
    //获取当前日期
    let currentDate = getFormattedDate();
    formData.times[0] = dayjs(currentDate+" 00:00:00");
    formData.times[1] = dayjs(currentDate+" 23:59:59");
   
    //获取地址栏报表code
    if(code){
      selectTable.value = true;
      const paramArr = code.split('_');
      tableParams.templateId = paramArr[0];
      tableParams.allocationId = paramArr[1];
      queryParam.value.allocationTableId = tableParams.allocationId;
      // tableParams.type = type;
      // tableParams.tableId = code;
      // if(type == '2'){
      //   tableParams.allocationId = code;
      // }
    }
     //报表集合
    getTableArr();
  });

  //右键
  function rightClickEvent(args, rowData){
    //获取数据(整行)
    // let rowData = listTableRef.value.vTableInstance.getRecordByCell(args.col, args.row);
    let data = {
      tableId: customReportTableId.value,
      allocationId: tableParams.allocationId,
      dataId: rowData.id,
      tableName:customReportTableName.value,
      originalValue:rowData[args.field],
      fields: args.field,
      field:headField.value[args.field],
      customReportLineName: customReportLineName.value,
      customReportDeptName: customReportDeptName.value
    }

    if (args.menuKey === 'modify') {
      //修改数据 
      data.type = 0;
      approveAdd(data);
    }else if(args.menuKey === 'invalid') {
      //无效剔除
      data.type = 1;
      approveAdd(data);
    }else if(args.menuKey === 'standard'){
      //查看标准
      let fieldName = args.field;
      let regex = /^field_(\d+)_(\d+)$/;
      let match = fieldName.match(regex);
      if(match){
        fieldName = fieldName.replace("_2","").replace("_3","");
      }
      data.fieldName = fieldName;
      data.name = headField.value[fieldName];
      data.id = rowData.id;
      manageStandardModal.value.disableSubmit = false;
      manageStandardModal.value.init(data);
    }else if(args.menuKey === 'fiveAnalysis'){
      //5Why分析
      let regex = /^field_(\d+)_(\d+)$/;
      let fieldName = args.field;
      let match = fieldName.match(regex);
      if(match){
        return
      }
      //获取颜色
      let bgColor = colorArr[args.row-2][fieldName];
      let obj ={
        //表名
        tableCode: customReportTableName.value,
        //表中文名
        tableName: customReportName.value,
        //字段
        fieldCode: fieldName,
        //字段中文名称
        fieldName: headField.value[fieldName],
        //数据id
        dataId: rowData.id,
        //标准值
        standardValue: rowData[fieldName+"_2"],
        //实际值
        actualValue: rowData[fieldName],
        //颜色
        color: bgColor,
        //类型(默认1)
        type: '1',
      }
      fiveWhyRef.value.openFiveWhy(obj);
      console.log(obj);
    }else if(args.menuKey === 'abnormalAnalysis'){
      let regex = /^field_(\d+)_(\d+)$/;
      let fieldName = args.field;
      let match = fieldName.match(regex);
      if(match){
        return
      }
      //异常分析
      let obj ={
        //分配表id
        allocationTableId: queryParam.value.allocationTableId,
        //表id
        tableId: queryParam.value.tableId,
        //表名
        tableCode: customReportTableName.value,
        //表中文名
        tableName: customReportName.value,
        //字段
        fieldCode: fieldName,
        //字段中文名称
        fieldName: headField.value[fieldName],
        //数据id
        dataId: rowData.id,
        //标准值
        standardValue: rowData[fieldName+"_2"],
        //实际值
        actualValue: rowData[fieldName],
        //颜色
        color: colorArr[args.row-2][fieldName],
        //类型(默认1)
        type: '1',
      }
      console.log(obj);
      abnormalRef.value.openAbnormal(obj);
    } else if(args.menuKey === 'pointDiagram'){ 
      chartModalRef.value.openPointDiagram(data);
    } else if(args.menuKey === 'boxDiagram'){ 
      chartModalRef.value.openBoxDiagram(data);
    }
  }
  //点击(单击)
  function clickEvent(args){
    //判断是否表头第一行
    if(args.row === 0 && args.col !== 0){
      let obj ={
        tableName: customReportTableName.value,
        fieldName: args.field,
        name: args.dataValue
      }
      manageStandardModal.value.disableSubmit = false;
      manageStandardModal.value.init(obj);
    }
  }

  //编辑后出发事件 值  行(固定列+数据)  列
  function handleChangeVtable(value, row, col) {
    // let colKey = allColumns.value[col+1]; //加上序号行
    let colKey = allColumns.value[col];
    const target = dataPage.value[row-2]; //减去表头层数
    // if (target) {
    //   target[colKey] = value;
    // }
    let data = listTableRef.value.vTableInstance.getRecordByCell(col,row);
    //右上角标注
    data[colKey+"_marked"] = true
    editDataIndex.value.push(target.id);
    editFieldSet.value.add(colKey);
  }

  //去除空格
  function strTrim(str){
    if(str){
      //去除所有空格
      //return str.replace(/\s/g, '');
      //去除前后空格
      return str.trim();
    }
    return "";
  }

  //记录选中事件(删除)
  function checkboxChangeVtable(value, row, col) {
    if((row === 0 ||row === 1) && col === 0){
      //全选
      if(value){
        //全选
        dataPage.value.forEach(function(obj) {
          console.log(obj+"-----------------------")
          delDataIndex.value.push(obj.id);
        });
      }else{
        //全取消
        delDataIndex.value=[]
      }
    }else{
      const target = dataPage.value[row-2]; //减去表头层数
      const index = delDataIndex.value.findIndex(item => item === target.id);
      if (index!== -1) {
        // 如果存在，移除该id
        delDataIndex.value.splice(index, 1);
      } else {
        // 如果不存在，添加该id
        if(target.id){
          delDataIndex.value.push(target.id);
        }
      }
      //delDataIndex.value.push(target.id);
    }
  }

  /**
   * 复选框变更事件（来自ListTableHead组件）
   */
  function handleCheckboxChange({ checked, isAll, id }) {
    if(isAll) {
      if(checked) {
        dataPage.value.forEach(obj => {
          if(!delDataIndex.value.includes(obj.id)) {
            delDataIndex.value.push(obj.id);
          }
        });
      } else {
        delDataIndex.value = [];
      }
    } else {
      const index = delDataIndex.value.findIndex(item => item === id);
      if(checked) {
        if(index === -1 && id) delDataIndex.value.push(id);
      } else {
        if(index !== -1) delDataIndex.value.splice(index, 1);
      }
    }
  }

  //获取当前日期
  function getFormattedDate() {
    let currentDate = new Date();
    let year = currentDate.getFullYear();
    let month = ('0' + (currentDate.getMonth() + 1)).slice(-2);
    let day = ('0' + currentDate.getDate()).slice(-2);
    return year+"-"+month+"-"+day;
  }

  /**
   * 初始化报表下拉数据
   */
  async function getTableArr() {
    //pi报表
    allocationTableList({ pageNo: 1, pageSize: 9999 }).then((res) => {
      if (res && res.records && res.records.length > 0) {
        let tableId="";
        res.records.map((item) => {
          tableArr.push({label: item.reportName, value: item.id, tableId: item.tableId});
          //获取自定义表id
          // if(code && item.id == code){
          if(tableParams.allocationId && item.id == tableParams.allocationId){
            tableId =  item.tableId;
            customReportTableId.value = tableId;
            customReportTableName.value = item.tableName;
            customReportName.value = item.reportName;
            customReportDeptName.value = item.deptCode_dictText;
            customReportLineName.value = item.lineCode_dictText;
            multipleTables.value = item.multipleTables;
          }
        });
        if(code){
          //初始化表
          tableChange(queryParam.value.allocationTableId,{tableId})
        }
      }
    });
  }

   /**
    * 新增事件
    */
  function handleAdd() {
    tableParams.tableName = customReportTableName.value
    registerModal.value.disableSubmit = false;
    registerModal.value.add(tableParams);
  }

  /**
  * 修改/剔除
  */
  function approveAdd(data) {
    let regex = /^field_(\d+)_(\d+)$/;
    let match = data.fields.match(regex);
    if(match){
      return
    }
    approveModal.value.disableSubmit = false;
    approveModal.value.add(data);
  }


  /**
   * 成功回调
   */
  function handleSuccess() {
    searchQuery();
  }

  //颜色集合
  const colorArr = reactive<Record<string, any>>([]);
  /**
   * 查询
   */
  function searchQuery() {
    queryParam.value.pageNo = pageNo;
    queryParam.value.pageSize = pageSize;
    colorArr.length = 0;
    confirmLoading.value = true;
    //清空删除标识
    delDataIndex.value = [];
    getData();
    
  }

  //获取pi报表数据
  function getData(){
    dataList(queryParam.value).then((res) => {
      let dataArr = res.records;
      if (dataArr && dataArr.length > 0) {
        totalRecord.value = res.total;
        //标准值
        //let standardArr = res.piStandardMaintenanceList
        dataPage.value = dataArr;
        //数据
        for (const data of dataArr) {
          if(multipleTables.value === '1'){
            data.id = 'temp_' + buildUUID();
          }
          
          //获取颜色
          //let piStandardData = data.piStandardData;
          const obj = data?.piStandardData && JSON.parse(data?.piStandardData);
          if(obj){
            let colorObj ={}
            for(const element  of obj){
              if(element.color){
                colorObj[element.field] = element.color;
              }
            }
            colorArr.push(colorObj);
          }else{
            colorArr.push({});
          }
        }
        //自定义表头
        tableHeadRef.value.setData(dataArr);
        // vtableOption.records = vtableOption.records;
      }else{
        totalRecord.value = 0
        dataPage.value = []
        if(isList.value){
           vtableOption.records = [];
        }else{
          tableHeadRef.value.setData([]);
        }
      }
    }).finally(() => {
      confirmLoading.value = false;
    });
  }

  /**
   * 获取查询条件/表头/数据
   */
  async function tableChange(val,obj){
    //获取表头(自定义)
    //let res = await getTableHead({allocationTableId: val});
    //获取模板
    let res = await getTemplateRecord(tableParams.templateId);

    if(res && res.reportConfig){
      isList.value = false;
      nextTick(() => {
        restData(res,val,obj);
      })
    }
  }

  //查询条件-时间
  function onChangeTime(fieldName){
    if(rangePickerTime.value[fieldName] && rangePickerTime.value[fieldName].length>0){
      let timeBegin = rangePickerTime.value[fieldName][0];
      let timeEnd = rangePickerTime.value[fieldName][1];
      //
      queryParam.value[fieldName] = dayjs(timeBegin).format('YYYY-MM-DD HH:mm:ss')+","+dayjs(timeEnd).format('YYYY-MM-DD HH:mm:ss')
    }else{
      queryParam.value[fieldName] ="";
    }
  }
  //查询条件-日期
  function onChangeDate(fieldName){
    if(rangePickerDate.value[fieldName] && rangePickerDate.value[fieldName].length>0){
      let dateBegin = rangePickerDate.value[fieldName][0];
      let dateEnd = rangePickerDate.value[fieldName][1];
      //
      queryParam.value[fieldName] = dayjs(dateBegin).format('YYYY-MM-DD')+","+dayjs(dateEnd).format('YYYY-MM-DD');
    }else{
      queryParam.value[fieldName] ="";
    }
  }
  //*******************PI重算/红黄区重算*********************** */
  //重算
  function rerun(type){
    if(type == '1'){
      title.value = 'PI重算';
    }else{
      title.value = '红黄区重算';
    }
    
    visible.value = true;
    rerunType.value = type
    tableField();
  }
  /**
   * 关闭重算配置
   */
  function rerunClose(){
    visible.value = false;
  }
  //验证方法
  async function validateDate(){
    await validate();
  }

  /**
   * 执行重算
   */
  async function rerunOk(){
    // 触发表单验证
    validateDate();

    //时间处理 并判断时间段是否在10内
    let times = formData.times
    //字段集合处理
    let fieldIds = formData.fieldIds
    if((!fieldIds || fieldIds.length === 0) && rerunType.value === 1){
      createMessage.warning("字段不为空");
      return false;
    }

    if(!times || times.length === 0){
      createMessage.warning("范围不为空");
      return false;
    }
    
    confirmLoading.value = true;
    
    
    let dateBegin = times[0];
    let dateEnd = times[1];

    let rerunData ={
      fieldList:fieldIds,
      tableId: customReportTableId.value,
      tableName: customReportTableName.value,
      timeStart: dayjs(dateBegin).format('YYYY-MM-DD HH:mm:ss'),
      timeEnd: dayjs(dateEnd).format('YYYY-MM-DD HH:mm:ss')
    }
    rerunFun(rerunData,rerunType.value)
        .then((res) => {
        if (res.success) {
          //执行成功
          }
    }).finally(() => {
      confirmLoading.value = false;
    });
    //无需等待,计算时间比较漫长
    visible.value = false;
    confirmLoading.value = false;
    createMessage.success("重算任务下发成功!");
  }

  //判断时间是否大于10天
  function isGreaterThanTenDays(dateBegin, dateEnd ,day) {
      const diffInMs = Math.abs(new Date(dateBegin).getTime() - new Date(dateEnd).getTime());
      const diffInDays = diffInMs / (24 * 60 * 60 * 1000);
      return diffInDays > day;
  }
  /**
   * 报表切换获取字段数据 
   */
  function tableField(){
    fieldArr.length = 0
    formData.fieldIds = [];
    let obj ={ tableId:customReportTableId.value,tableName:customReportTableName.value,reportType:type }
    // if(type == '2'){
    //   obj.allocationTableId = code;
    // }
    obj.allocationTableId = tableParams.allocationId;
    getAlgorithmField(obj).then((res) => {
        if (res && res.length > 0) {
          res.forEach((item) => {
            fieldArr.push({label: item.name, value: item.fieldName});
          });
        }
    });
  }
  /**
   * 重算-字段全选
   */
  function selectAll(){
    formData.fieldIds = []
    fieldArr.map((item) => {
      formData.fieldIds.push(item.value);
    });
  
  }

  /**
   * 保存前验证
   */
  function saveData(){
    let postDatas = []
    let dataPageData = dataPage.value;
    let editDataIndexData = editDataIndex.value;
    if(!isList.value){
      dataPageData = tableHeadRef.value.dataPage
      editDataIndexData = tableHeadRef.value.editDataIndex
    }

    postDatas = dataPageData.filter(item => {
      if (editDataIndexData.includes(item.id)) {
        return true
      }
      return false
    })
    if (postDatas.length < 1) {
      createMessage.warning("暂无被修改的数据");
      return;
    }
    const regex = /^field_[^_]+$/;
    const regexMultiple = /_f_/;
    postDatas.forEach(item => {
      for (let key in item) {
        if (key.includes('piStandardData') || ((!regex.test(key)) && key!="id" && (!regexMultiple.test(key)))||
        key.includes('_marked')) {
          delete item[key]
        }
      }
    })
    postData(postDatas)
  }

  //保存数据
  async function postData(listData) {
    confirmLoading.value = true;
    let editFieldSet = tableHeadRef.value.getEditFieldSet();
    let obj = {
      data:listData,
      tableName: customReportTableName.value,
      type:type,
      editFields: [...editFieldSet].join(',') //被修改的字段集合
    }
    obj.allocationTableId = tableParams.allocationId;
    await batchEditVal(obj)
    .then((res) => {
      if (res.success) {
          const successMessage = res.message ? res.message : t('language.save');
          createMessage.success(successMessage);
          //setSaveDataId(listData, dataPage, result)
          //this.$refs['filterOption'] && this.$refs['filterOption'].updateList()
          searchQuery();
          editDataIndex.value=[];
          // editFieldSet.value.clear();
          tableHeadRef.value.clearEditFieldSet();
      } else {
          createMessage.warning(res.message);
          //保存失败刷新数据
          searchQuery();
      }
    })
    .finally(() => {
      confirmLoading.value = false;
    });
  }

  //批量删除
  async function batchHandleDelete(){
    await batchDelete({data:delDataIndex.value.join(), tableName: customReportTableName.value}, handleSuccess);
    delDataIndex.value = [];
  }

  //标记5W
  function customRenderFun(args) {
    if (args.row === 0 || args.col === 0) return null;
    // let colKey = allColumns.value[args.col+1]; //加上序号行
    let colKey = allColumns.value[args.col]; //加上序号行
    if(!(listTableRef && listTableRef.value && listTableRef.value.vTableInstance)){
      return {renderDefault: true};
    }
    let data = listTableRef.value.vTableInstance.getRecordByCell(args.col,args.row);
    if(data[colKey+"_f"]){
      console.log(args);
      const { width, height } = args.rect;
      const { dataValue, table, row, col } = args;
      const elements = [] as any;
      let top = 30;
      const left = 15;
      let maxWidth = 0;
      elements.push({
        type: 'icon',
        svg: '<svg width="200" height="200" viewBox="0 0 200 200" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M37.5 162.5V37.5H162.5" fill="#1969E0"/><path d="M50.3914 79.88V74.16C51.5381 74.4267 52.4714 74.6133 53.1914 74.72C53.9381 74.8 54.8447 74.84 55.9114 74.84C57.7247 74.84 59.1381 74.5733 60.1514 74.04C61.1914 73.48 61.7114 72.52 61.7114 71.16C61.7114 69.8 61.2714 68.8667 60.3914 68.36C59.5381 67.8267 58.1381 67.56 56.1914 67.56C54.4314 67.56 52.4314 67.76 50.1914 68.16L52.1914 52.24H67.0714V57.92H57.5914L56.8314 62.84C57.7381 62.6533 58.6847 62.56 59.6714 62.56C62.7381 62.56 65.0181 63.28 66.5114 64.72C68.0314 66.1333 68.7914 68.28 68.7914 71.16C68.7914 74.2533 67.7381 76.5733 65.6314 78.12C63.5247 79.64 60.5114 80.4 56.5914 80.4C54.3781 80.4 52.3114 80.2267 50.3914 79.88ZM79.0195 52.24L82.5395 71.52H82.7795L86.4595 54.68H94.5795L97.9395 71.52H98.1795L101.82 52.24H109.18L102.9 80H94.0595L90.3395 63.6H90.0995L86.2995 80H77.4595L71.6195 52.24H79.0195Z" fill="white"/></svg>',
        x: -6,
        y: -6,
        width: 35,
        height: 35
      });
      return {
        elements,
        renderDefault: true,
      };
    }else{
      return {renderDefault: true,};
    }
  }

  //导出
  async function exportData(){
    if(isList.value){
      const listTableInstance = listTableRef.value;
      if (!listTableInstance) {
        createMessage.warning("ListTable 实例未创建或未挂载");
      }
      //获取 VTable 实例的数据
      const tableInstance = listTableInstance.vTableInstance;
      //5w分析
      vtableOption.customRender = "";
      //固定列行数
      let frozenColCount = vtableOption.frozenColCount;
      vtableOption.frozenColCount = 0
      downloadExcel(await exportVTableToExcel(tableInstance, {ignoreIcon: true,formatExportOutput: ({ cellType, cellValue, table, col, row }) => {
        if (cellType === 'checkbox') {
          if(row == 0){
            return "序号"
          }else{
            return row-1;
          }
        }
      }}), customReportName.value);
      vtableOption.frozenColCount = frozenColCount;
      vtableOption.customRender = customRenderFun;
    }else{
      tableHeadRef.value.exportExcel(customReportName.value)
    }
  }

  //复制内容存到剪贴板
  async function copy(value){
    useCopyToClipboard(value);
  }



  //=======================自定义表头==================================
  //已配置的合并单元格数据
  const mergeCellList = ref([] as any);
  //已配置的单元格样式数据
  const cellStyleConfigList = ref([] as any);
  //填报数据
  const fillInList = ref([] as any);

  //自定义表头
  async function restData(data,val,obj){
    let config= {
      listTableHeight:listTableHeight.value
    }
    //获取表头行数
    let rowCount = 0
    if(data && data?.reportConfig){
      const reportConfigJson = data.reportConfig ? JSON.parse(data.reportConfig) : '';
      const records = reportConfigJson ? reportConfigJson.records : [];
      rowCount = records.length;
    }

    //获取表头
    // await getHaed(val,obj);
     //固定列数
    let frozenColCount = 1; 
    //子页面的 listTableRef
    let listTableRef =  tableHeadRef.value.listTableRef;
    await head({ allocationTableId: val }).then((res) => {
     if (res && res.length > 0) {
        //复选框
        columns.value.push({
          field: 'isCheck',
          title: '',
          width: 60,
          headerType: 'checkbox',
          cellType: 'checkbox'
        },)
        //字典所属列
        let index = 1; //有复选框，数据列从第1列开始
        let columnObj = {};
        for (const item of res) {
          headField.value[item.fieldName] = item.name;
          let childrenArr = [] as any;
          //判断是否显示实际值
          if(item.showActual === true){
            //下拉框管理字典类型
            cellComponentsList.value.push({
              col:index++,
              dictType: item.dropdownData
            });
            let fileType = 'input-editor'
            if(item.type === '4'){
              //日期
              fileType = 'date-editor' 
            }else if(item.type === '6'){
              //时间
              fileType = 'date-time-editor' 
            }
            else if(item.type === '1' || item.type === '2' || item.type === '5'){
              if(item.editWidgetType=='select'){
                fileType = 'select-editor' 
              }
              else if(item.editWidgetType=='selectUser'){
                fileType = 'select-user-editor' 
              }
            }

            columnObj = {
              title: item.name,
              field: item.fieldName,
              width: 'auto',
              headerStyle: {
                textAlign: 'center',
                color: '#000000',
                fontWeight: 500,
                fontSize: 14,
                fontFamily: 'Microsoft YaHei'
              },
   
              style: (args)=>{
                let styleObj = {};
                //背景色
                if(rowCount > 0 && args.row >= rowCount && colorArr && colorArr[args.row-rowCount][args?.cellHeaderPaths?.colHeaderPaths[0]?.field]){
                  //颜色转换
                  let bgColor = colorArr[args.row-rowCount][args.cellHeaderPaths.colHeaderPaths[0].field];
                  let fontColor = "#000000a6";
                  if(bgColor){
                    if(bgColor === 'grey'){
                      fontColor = "#33333340"
                    }
                    bgColor = getColor(bgColor);
                    styleObj = {bgColor: bgColor,color: fontColor,fontFamily: 'Microsoft YaHei'};
                  }
                }
                //设置是否显示单元格标记 marked: true
                //判断是否修改-添加修改后标注
                let colKey = allColumns.value[args.col]; //加上序号行
                let data = listTableRef.vTableInstance.getRecordByCell(args.col,args.row);
                if(data[colKey+"_marked"]){
                  styleObj.marked = true;
                }
                return styleObj;
              },
              
              fieldFormat: function (value) {
                let val = value[item.fieldName];
                if (val === null || val === "" || val === undefined) {
                      return "/";
                }
                return val;
              }
            }
            //是否可以编辑
            if(item.isEdit == '1'){
              columnObj.editor = fileType;
              columnObj.headerIcon = headerIcon.value;
            }
          }
      
          //重点关注(PI)
          // if(item.isFocus == '1'){
          //   columnObj.headerIcon = headerStressIcon.value
          // }
          //固定
          if(item.isFixed == '1'){
            let count = childrenArr.length===0?1:childrenArr.length
            frozenColCount = frozenColCount + count;
          }
          //表头集合allColumns
          allColumns.value.push(item.fieldName);
         
          //vtable 表头
          columns.value.push(columnObj)
          //获取查询条件
          if(item.isQuery === 1){
            queryArr.push(item);
          }
        }
        //设置固定列
        vtableOption.frozenColCount = frozenColCount
        vtableOption.columns = columns;
      }
    })

    data.columns = vtableOption.columns
    data.frozenColCount = vtableOption.frozenColCount
    data.allColumns = allColumns.value;
    //初始化组件
    tableHeadRef.value.initData(data,config);
    queryParam.value.tableId = data.tableId;
    //获取数据
    searchQuery();
    //绑定字段数据
    // const reportConfigJson = data.reportConfig ? JSON.parse(data.reportConfig) : '';
    // let columnsArr = reportConfigJson ? reportConfigJson.columns : [];
    // vtableOption.columns = columnsArr;
    // vtableOption.records = reportConfigJson ? reportConfigJson.records : [];
    // mergeCellList.value = reportConfigJson ? reportConfigJson.mergeCellList : [];
    // cellComponentsList.value = reportConfigJson ? reportConfigJson.cellComponentsList : [];
    // cellStyleConfigList.value = reportConfigJson ? reportConfigJson.cellStyleConfigList : [];
    // fillInList.value = [];
    // // vtableOption.showHeader = false,
    // vtableOption.customMergeCell = function(col, row, table) {
    //   // 根据配置的合并数据合并单元格
    //   for (let item of mergeCellList.value) {
    //     if (col >= item.startCol && col <= item.endCol && row >= item.startRow && row <= item.endRow) {
    //       return {
    //         text: item.dataValue,
    //         range: {
    //           start: {
    //             col: item.startCol,
    //             row: item.startRow
    //           },
    //           end: {
    //             col: item.endCol,
    //             row: item.endRow
    //           }
    //         }
    //       };
    //     }
    //   }
    //   // 如果没有匹配的合并项，返回 null 或 undefined
    //   return null;
    // };
    // console.log('cellComponentsList.value :>> ', cellComponentsList.value);

    // //加载样式
    // await loadCellStyle();
    // //事件绑定
    // // bindEvent();
    // await listTableRef.value.vTableInstance.renderWithRecreateCells();
  }


  //注册处理样式
  async function loadCellStyle() {
    for(const item of cellStyleConfigList.value){
      //注册样式配置
      await listTableRef.value.vTableInstance.registerCustomCellStyle(item.id, item.style);
      //更新单元格样式
      listTableRef.value.vTableInstance.arrangeCustomCellStyle({ col: item.col, row: item.row }, item.id)
    }
  }
  //获取字段历史数据
  function getHistoryData(val){
    getTableFieldData({reportType: type, tableId:val , fields: historyDataArr.value.join()}).then((res) => {
        if (res) {
          historyData.value = res;
        }
    });
  }
  //获取近对应字段 10条数据
  function historyOptions(val){
    let optionsArray = [];
    if(historyData.value[val] && historyData.value[val].length>0){
      optionsArray = historyData.value[val].map(value => ({
        value: value,
        label: value
      }));
    }
    return optionsArray;
  }
  //对应字段 10条数据选择
  function handleSingleTagChange(val,fieldName){
    const tagFieldName = fieldName + '_tag';
    if (!val || val.length === 0) {
      // 清空情况
      queryParam.value[tagFieldName] = [];
      queryParam.value[fieldName] = undefined;
      return;
    }
    if (val.length > 1) {
      // 如果超过一个，保留最后一个
      queryParam.value[tagFieldName] = [val[val.length - 1]];
    } else {
      // 正常选择一个的情况
      queryParam.value[tagFieldName] = val;
    }
    
    // 将数组值赋给普通字段（取第一个元素）
    queryParam.value[fieldName] = queryParam.value[tagFieldName][0];
  }

  /**
   * 时间格式化公共方法
   * @param {Date|string} date - 日期对象或日期字符串
   * @param {number} formatType - 格式类型：1-年月日时分秒，2-年月日，3-时分秒，4-时分，5-时
   * @returns {string} 格式化后的时间字符串
   */
  function formatDateTime(date, formatType = 1) {
    // 确保 date 是 Date 对象
    const d = new Date(date);
    if (isNaN(d.getTime())) {
      return "";
    }

    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    const hours = String(d.getHours()).padStart(2, '0');
    const minutes = String(d.getMinutes()).padStart(2, '0');
    const seconds = String(d.getSeconds()).padStart(2, '0');

    switch (formatType) {
      case 1: // 年月日时分秒
        return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
      case 2: // 年月日
        return `${year}-${month}-${day}`;
      case 3: // 时分秒
        return `${hours}:${minutes}:${seconds}`;
      case 4: // 时分
        return `${hours}:${minutes}`;
      case 5: // 时
        return `${hours}`;
      default:
        return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    }
  }

  function getMenuConfig(field, row, col){
    //右键菜单配置
    let menus = [];
    let menuArray = [
      { text: '查看标准', menuKey: 'standard' },
      { text: '修改数据', menuKey: 'modify' },
      { text: '无效剔除', menuKey: 'invalid' },
      // { text: '5Why', menuKey: 'fiveAnalysis' },
      { text: '异常分析', menuKey: 'abnormalAnalysis' },
      { text: '点图', menuKey: 'pointDiagram' },
    ];
    if(customReportDeptName.value && customReportDeptName.value.includes('包装')){
      menuArray.push({ text: '箱图', menuKey: 'boxDiagram' })
    }
    if(row > 1){
      let regex = /^field_(\d+)$/;
      let match = field.match(regex);
      if(match){
        //实际值
        menus = menuArray;
      }else{
        //标注值
        menus.push(menuArray[0])
      }
    }
    return menus;
  }
  /**
   * 获取重点关注图标
   * @param isFocus 是否重点关注PI
   * @param isFactoryFocus 是否工厂关注PI
   */
  function getHeaderStressIcon(isFocus, isFactoryFocus){
    //红色
    let leftColor = '#FF3355';
    //蓝色
    let rightColor = '#007BFF';
    if(isFocus == 1 && isFactoryFocus != 1){
      //如果只是重点关注pi则图标为红色
      rightColor = leftColor;
    }else if(isFocus != 1 && isFactoryFocus == 1){
      //如果只是工厂关注pi则图标为蓝色
      leftColor = rightColor;
    }
    return {
      type: 'svg', //指定svg格式图标，其他还支持path，image
      svg: `<svg t="1736819720498" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="4264" width="200" height="200">
          <defs>
            <mask id="half-mask" maskUnits="userSpaceOnUse">
              <rect x="0" y="0" width="512" height="1024" fill="white" />
              <rect x="512" y="0" width="512" height="1024" fill="black" />
            </mask>
          </defs>
          <!-- 底层：完整图标为右侧颜色（比如蓝色） -->
          <path d="M529.066667 162.133333l83.626666 169.386667a42.666667 42.666667 
            0 0 0 32.213334 23.466667L832 382.08a42.666667 42.666667 0 0 1 23.68 72.96l-135.253333 
            132.053333a42.666667 42.666667 0 0 0-12.373334 37.76L739.84 810.666667a42.666667 
            42.666667 0 0 1-62.08 45.013333L510.506667 768a42.666667 42.666667 0 0 0-39.68 
            0l-167.253334 87.893333A42.666667 42.666667 0 0 1 241.493333 810.666667l32-186.24a42.666667 
            42.666667 0 0 0-12.373333-37.76L125.866667 455.04A42.666667 42.666667 0 0 1 149.333333 
            382.08l186.88-27.093333a42.666667 42.666667 0 0 0 32.213334-23.466667L452.266667 
            162.133333a42.666667 42.666667 0 0 1 76.8 0z"
            fill="${rightColor}" />

          <!-- 上层：只显示左半部分，颜色为红色 -->
          <path d="M529.066667 162.133333l83.626666 169.386667a42.666667 42.666667 
            0 0 0 32.213334 23.466667L832 382.08a42.666667 42.666667 0 0 1 23.68 72.96l-135.253333 
            132.053333a42.666667 42.666667 0 0 0-12.373334 37.76L739.84 810.666667a42.666667 
            42.666667 0 0 1-62.08 45.013333L510.506667 768a42.666667 42.666667 0 0 0-39.68 
            0l-167.253334 87.893333A42.666667 42.666667 0 0 1 241.493333 810.666667l32-186.24a42.666667 
            42.666667 0 0 0-12.373333-37.76L125.866667 455.04A42.666667 42.666667 0 0 1 149.333333 
            382.08l186.88-27.093333a42.666667 42.666667 0 0 0 32.213334-23.466667L452.266667 
            162.133333a42.666667 42.666667 0 0 1 76.8 0z"
            fill="${leftColor}"
            mask="url(#half-mask)" />
        </svg>`,
      width: 20,
      height: 20,
      name: 'frozenCurrent', //定义图标的名称，在内部会作为缓存的key值
      positionType: VTable.TYPES.IconPosition.right,
      marginRight: 0,
      hover: {
        // 热区大小
        width: 22,
        height: 22,
        bgColor: 'rgba(101, 117, 168, 0.1)'
      },
      cursor: 'pointer'
    }
  }
</script>

<style lang="less" scoped>
.jeecg-basic-table-form-container {
  .table-page-search-submitButtons {
    display: block;
    margin-bottom: 24px;
    white-space: nowrap;
  }
  .query-group-cust {
    width: calc(50% - 15px);
    min-width: 100px !important;
  }
  .query-group-split-cust {
    width: 30px;
    display: inline-block;
    text-align: center;
  }
}
.edit-action {
  width: calc(100% - 40px);
}
.edit-icon {
  cursor: pointer;
  margin-left: 6px;
}
.edit-icon:hover {
  color: @primary-color;
}
</style>