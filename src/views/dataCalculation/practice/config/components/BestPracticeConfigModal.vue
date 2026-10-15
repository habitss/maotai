<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose :title="title" :width="1200" @ok="handleOk"
    :bodyStyle="{ height: '70vh' }" :maskClosable="false">
    <BasicTable @register="registerTable" :show-header="false">
      <!--插槽:table标题-->
      <template #tableTitle>
        <template v-if="isFill">
          <JDictSelectTag style="width: 600px;"
            v-model:value="fillReportId"
            :dictCode="`mes3_best_practice_config, config_name, id, type_id = ${typeId} and del_flag = 0`"
            placeholder="请选择配置表" :showChooseOption=false @change="handleConfigChange"
            :disabled="Object.keys(extItemIds).length > 0 || !isAuth">
          </JDictSelectTag>
          <a-button v-if="isAuth" type="primary" @click="addScoreItem(null)">扩展打分表</a-button>
          <a-button v-if="isAuth && Object.keys(extItemIds).length > 0" type="primary" @click="deleteScore()">删除打分表</a-button>
        </template>
        <a-button v-else type="primary" :disabled="isUpdate" @click="showNewItem()">新增评审项</a-button>
      </template>
      <!--字段回显插槽-->
      <template v-slot:bodyCell="{ column, record, index }">
        <!-- 配置页面进入的插槽选项 -->
        <template v-if="isFill === false">
          <!-- 分数、行的添加删除操作插槽 -->
          <template
            v-if="column.dataIndex == 'text' && (record.id == 'influence' || record.id == 'requirement' || record.id == 'improve' || record.id.startsWith('dynamic'))">
            <a-row>
              <a-col span="16">
                {{ record.text }}<br>
                <span v-if="record.id !== 'influence' && record.id !== 'requirement'">(<a-input-number
                    style="width: 30px !important" v-model:value="record['itemScore']"
                    placeholder="请输入分值"></a-input-number>分)</span>
              </a-col>
              <a-col span="3" v-if="!isUpdate">
                <a-row style="height: 100%" type="flex" justify="center" align="middle">
                  <!-- 添加行图标 -->
                  <a-col span="24">
                    <a-icon class="icon" @click="handleAddRow(column, record, index)" type="plus-square" />
                  </a-col>
                  <!-- 删除行图标 -->
                  <a-col span="24" v-if="showDelete(column, record, index)">
                    <a-popconfirm title="是否确认删除" ok-text="确认" cancel-text="取消"
                      @confirm="handleDeleteRow(column, record, index)">
                      <a-icon class="icon" type="minus-square" />
                    </a-popconfirm>
                  </a-col>
                </a-row>
              </a-col>
            </a-row>
          </template>

          <!-- 影响因素求插槽 -->
          <template v-else-if="record.id.startsWith('influence_')">
            <a-input v-model:value="record[column.dataIndex]" placeholder="请输入"></a-input>
          </template>

          <!-- 提报要求插槽 -->
          <template
            v-else-if="record.id.startsWith('requirement') && (column.dataIndex == 'field_1' || column.dataIndex == 'field_2')">
            <a-textarea v-model:value="record[column.dataIndex]" placeholder="请输入"></a-textarea>
          </template>

          <!-- 改善效果插槽 -->
          <template v-else-if="record.id.startsWith('improve')">
            <a-textarea @change="handleImproveChange(record, column)" v-model:value="record[column.dataIndex]"
              placeholder="请输入"></a-textarea>
          </template>

          <!-- 动态行插槽 -->
          <template
            v-else-if="record.id.startsWith('dynamic') && (column.dataIndex == 'field_2' || column.dataIndex == 'field_3' || column.dataIndex == 'field_4')">
            <a-textarea v-model:value="record[column.dataIndex]" placeholder="请输入"></a-textarea>
          </template>

          <!-- 表头插槽 -->
          <template v-else-if="record.id == 'title' && column.dataIndex === 'text'">
            <a-input v-model:value="record[column.dataIndex]" placeholder="请输入评审表名"></a-input>
          </template>
        </template>

        <!-- 填报模式插槽选项 -->
        <template v-else>
          <template v-if="column.dataIndex.startsWith('score_value')">
            <JDictSelectTag v-if="record.id === 'influence'" v-model:value="record[column.dataIndex]"
              :dictCode="`sys_user, realname, id, del_flag = 0`" placeholder="请选择评审人" :showChooseOption=false
              :disabled="!isAuth" show-search allowClear>
            </JDictSelectTag>

            <JDictSelectTag v-else-if="record.id === 'influenceText'" v-model:value="record[column.dataIndex]"
              :dictCode="`mes3_mdm_post, post_name, id, del_flag = 0`" placeholder="请选择评审人岗位" :showChooseOption=false
              :disabled="!isAuth" show-search allowClear>
            </JDictSelectTag>

            <a-select v-else-if="record.id === 'requirement'" placeholder="请选择" :disabled="!isAuth" 
              v-model:value="record[column.dataIndex]" @change="handleCalcScore(column)">
              <a-select-option value="0">不违反</a-select-option>
              <a-select-option value="1">违反</a-select-option>
            </a-select>

            <a-input-number v-else-if="record.id === 'remark'" v-model:value="record[column.dataIndex]" disabled="true"></a-input-number>

            <a-input-number v-else placeholder="请输入分值" :disabled="!isAuth" :precision="0" v-model:value="record[column.dataIndex]" :max="record.itemScore > 0 ? record.itemScore : 0" @blur="handleCalcScore(column)"></a-input-number>
          </template>
          <span v-else-if="column.dataIndex === 'text'" >
            {{ record.text }}<labbel v-if="record.itemScore">（{{ record.itemScore }}）分</labbel>
          </span>
        </template>
      </template>
    </BasicTable>

    <a-modal v-model:open="open" title="新增评审项" @ok="handleAddItem">
      <a-form ref="formRef" :model="formState" name="form_in_modal" :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }">
        <a-form-item label="评审项名称" name="name" :rules="[{ required: true, message: '请输入评审项名称!' }]">
          <a-input v-model:value="formState.name" />
        </a-form-item>
      </a-form>
    </a-modal>
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref, computed, unref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicTable } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage';
import { saveOrUpdate, queryById } from '../BestPracticeConfig.api';
import { saveOrUpdate as reviewSaveOrUpdate, list as reviewList, deleteByType } from '../../review/BestPracticeReview.api';
import { configColumns, defaultMergeRuleList, defaultDataSource } from '../BestPracticeConfig.data';
import { JDictSelectTag } from '/@/components/Form';
import { cloneDeep } from 'lodash-es';
import dayjs from 'dayjs';
import { buildShortUUID } from '/@/utils/uuid';
import { usePermission } from '/@/hooks/web/usePermission';
import {useMessage} from "@/hooks/web/useMessage";
const { hasPermission } = usePermission();
//消息提示组件注册
const { createMessage: msg, createConfirm } = useMessage();
// Emits声明
const emit = defineEmits(['register', 'success']);
const isUpdate = ref(true);
const isAuth = ref(false);
//评审表类型id
const typeId = ref('');
//评审配置id
const configId = ref(null);
//上级传入数据
const configJson = ref('');
//是否为填报模式
const isFill = ref(false);
//评审填报报表id
const fillReportId = ref('');
//填报ID
const fillId = ref();
//填报类型
const fillType = ref();
//扩展的打分表数量
const extNum = ref(0);
//扩展的打分表id
const extItemIds = ref({});
//拓展字段集合
const fieldNameArr = ref([] as any);

//新增评审项弹窗变量和数据
const open = ref<boolean>(false);
const formRef = ref();
const formState = ref({
  name: '',
});

//合并规则
const mergeRuleList = ref({} as any);

//表单赋值
const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
  isFill.value = data?.isFill;
  isUpdate.value = !!data?.isUpdate;
  typeId.value = data?.typeId;
  configJson.value = data?.record?.configJson ?? '';
  configId.value = data?.record?.id ?? null;
  extItemIds.value = {};
  if (false === data?.isFill) {
    renderingTable();
  } else {
    fillType.value = data?.fillType;
    fillId.value = data?.fillId;
    fillReportId.value = data?.fillReportId;

    //根据权限判断是否可以保存数据
    switch (data?.fillType) {
      case 1:
        data.showFooter = hasPermission('practice:mes3_best_practice_fill:shift_group_score');
        break;
      case 2:
        data.showFooter = hasPermission('practice:mes3_best_practice_fill:dept_score');
        break;
      case 3:
        data.showFooter = hasPermission('practice:mes3_best_practice_fill:factory_score');
        break;
      default:
        data.showFooter = false;
        break;
    }
    isAuth.value = data?.showFooter;
    //根据报表id设置配置信息
    if(data?.fillReportId){
      handleConfigChange(data?.fillReportId);
      isUpdate.value = true;
    }
    //查询评审表数据
    const { records } = await reviewList({pageSize: 100, fillId: data?.fillId, fillType: data?.fillType });
    if(records && records.length > 0) {
      //循环数据，添加评分项并赋值
      for(const item of records) {
        const tempObj = JSON.parse(item.processData);
        //添加对应id
        extItemIds.value[tempObj[0].flag] = item.id;
        //添加列
        await addScoreItem(JSON.parse(item.processData)[0].flag);
        //循环表格数据，将对应数据设置赋值
        for(const tableItem of getDataSource()){
          const findobj = tempObj.find(item => item.id === tableItem.id);
          if(findobj) {
            tableItem[findobj.flag] = findobj.score;
          }
        }
      }
      extNum.value = JSON.parse(records.pop().processData)[0].flag.split('_')[2];
    }
  }
  setModalProps({ confirmLoading: false, showCancelBtn: !!data?.showFooter, showOkBtn: !!data?.showFooter });
});
//设置标题
const title = computed(() => (!unref(isUpdate) ? '新增' : '编辑'));

//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    scroll: { y: window.innerHeight * 0.6 },
    pagination: false,
    showTableSetting: false,
    showActionColumn: false,
    actionColumn: {
      width: 120,
      fixed: 'right'
    },
  },
})

const [registerTable, { setTableData, setColumns, insertTableDataRecord, deleteTableDataRecord, getDataSource, getColumns }] = tableContext

//设置合并
function setCustomCell(newWhyArr) {
  //循环新的列数据
  newWhyArr.forEach((item, colIndex) => {
    //设置列样式
    item.customCell = (record, index, column) => {
      let rowSpan = 1;
      let colSpan = 1;
      let styleObj = { borderColor: '#ccc' };
      //获取当前列的合并规则，如果没有则默认为1行1列
      if (mergeRuleList.value[record.id]) {
        rowSpan = mergeRuleList.value[record.id][colIndex - 1]['rowSpan'];
        colSpan = mergeRuleList.value[record.id][colIndex - 1]['colSpan'];
      }
      //判断是否需要改变背景色
      if (column.dataIndex === 'scoreText') {
        styleObj.backgroundColor = '#F0F3FC';
      } else if (record.id != 'remark' && !record.id.includes('dynamic_item') && !column.dataIndex.includes('score_value')) {
        if (column.dataIndex === 'text' || record.id.includes('influence')) {
          styleObj.backgroundColor = '#F0F3FC';
        }
      }
      return {
        rowSpan,
        colSpan,
        style: styleObj,
      };
    }
  });
  //重新设置表格columns数据
  setColumns(newWhyArr);
}
//新增行
async function handleAddRow(column, record, index) {
  //获取点击数据的合并行列数，用于处理新增行数据
  const recordMegerRule = mergeRuleList.value[record.id];

  //处理新增行id
  let recordId = record.id;
  let newRowId = recordId + '_child_' + recordMegerRule[0].rowSpan;

  // //处理新增行的合并规则
  let newRowMegerRule = {};
  if (record.id == 'requirement') {
    //单独处理提报要求的合并规则
    newRowMegerRule = {
      0: { rowSpan: 0, colSpan: 1 },
      1: { rowSpan: 1, colSpan: 1 },
      2: { rowSpan: 0, colSpan: 0 },
      3: { rowSpan: 0, colSpan: 0 },
      4: { rowSpan: 0, colSpan: 0 },
      5: { rowSpan: 0, colSpan: 0 },
    }
  } else {
    for (const key in recordMegerRule) {
      if (record.id.startsWith('dynamic') && key < 2) {
        newRowMegerRule[key] = {
          rowSpan: recordMegerRule[key].rowSpan > 0 ? 0 : 1,
          colSpan: recordMegerRule[key].colSpan > 0 ? 0 : 1
        }
      } else if (key < 1) {
        newRowMegerRule[key] = {
          rowSpan: recordMegerRule[key].rowSpan > 0 ? 0 : 1,
          colSpan: recordMegerRule[key].colSpan > 0 ? 0 : 1
        }
      } else if (key > 4 && record.id != 'improve') {
        newRowMegerRule[key] = {
          rowSpan: 0,
          colSpan: 0
        }
      } else {
        newRowMegerRule[key] = {
          rowSpan: recordMegerRule[key].rowSpan > 1 ? 0 : 1,
          colSpan: recordMegerRule[key].colSpan > 1 ? 0 : 1
        }
      }
    }
  }
  mergeRuleList.value[newRowId] = newRowMegerRule;
  await insertTableDataRecord({
    id: newRowId,
    text: '',
    itemScore: '',
    field_1: '',
    field_2: '',
    field_3: '',
    field_4: '',
    scoreText: '',
  }, index + recordMegerRule[0].rowSpan);
  //新增之后，当前行的合并行数+1
  mergeRuleList.value[record.id][0]['rowSpan'] = recordMegerRule[0].rowSpan + 1;
  if (record.id === 'influence') {  //处理影响因素
    mergeRuleList.value['influenceText'][5]['rowSpan']++;
  } else if (record.id !== 'improve') { //非改善效果最后一列加1
    mergeRuleList.value[record.id][5]['rowSpan'] = recordMegerRule[5].rowSpan + 1;
  }

  if (record.id == 'requirement') {
    //单独处理提报要求的合并规则
    mergeRuleList.value[record.id][2]['rowSpan'] = recordMegerRule[2].rowSpan + 1;
  }
}
//删除行
async function handleDeleteRow(column, record, index) {
  const filterData = getDataSource().filter((item) => item.id.startsWith(record.id));
  const filterDataLength = filterData.length ?? 0;
  //删除动态项最后一行数据id
  const id = filterData[filterData.length - 1].id;
  //删除对应合并规则数据
  await delete mergeRuleList.value[id];
  if (filterDataLength > 1) {
    mergeRuleList.value[filterData[0].id][0].rowSpan = mergeRuleList.value[filterData[0].id][0].rowSpan - 1;
    if (record.id !== 'improve') { //非改善效果最后一列减1
      mergeRuleList.value[filterData[0].id][5].rowSpan = mergeRuleList.value[filterData[0].id][5].rowSpan - 1;
    }
  }
  //提报要求合并规则
  if (record.id == 'requirement') {
    mergeRuleList.value['requirement'][2].rowSpan = mergeRuleList.value['requirement'][2].rowSpan - 1;
  }
  //删除行数据
  await deleteTableDataRecord(id);

}
//是否显示删除行按钮
function showDelete(column, record, index) {
  let isShow = true;
  //如果不是动态项,且记录只有一条时不显示删除按钮
  if (!record.id.startsWith('dynamic_item')) {
    const newArr = getDataSource().filter((item) => item.id.startsWith(record.id));
    if (record.id == 'influence') {
      isShow = newArr.length > 3;
    } else {
      isShow = newArr.length > 1;
    }
  }
  return isShow;
}
//显示新增评审项弹窗
function showNewItem() {
  open.value = true;
}
//新增评审项
async function handleAddItem() {
  formRef.value
    .validateFields()
    .then(async values => {
      //关闭弹窗
      open.value = false;
      //新增评审项数据
      const newRecord = cloneDeep(defaultDataSource.find((item) => item.id === 'dynamic_item_1')) ?? {} as any;
      newRecord.id = 'dynamic_item_' + dayjs().valueOf().toString(36);
      newRecord.text = values.name;
      newRecord.scoreText = values.name;
      //新增评审项合并规则
      const mergeRule = cloneDeep(defaultMergeRuleList['dynamic_item_1']);
      mergeRuleList.value[newRecord.id] = mergeRule;
      //插入数据
      await insertTableDataRecord(newRecord, getDataSource().length - 1);
      //重置表单
      formRef.value.resetFields();
    })
    .catch(info => {
    });
}

//改善效果输入事件
function handleImproveChange(record, column) {
  if(column.dataIndex === 'field_1'){
    record.scoreText = record[column.dataIndex]
  }
}

function handleOk() {
  if (isFill.value === true) {
    handleFillSubmit();
  } else {
    handleSubmit();
  }
}

//配置表单提交事件
async function handleSubmit() {
  try {
    setModalProps({ confirmLoading: true });
    const dataSource = cloneDeep(getDataSource());
    //动态项最大分数
    let itemScore = ''; 
    //动态性顶层标识
    let itemId = ''; 
    for(const item of dataSource){
      //循环数据将动态项子层分数都设置上最大分数
      if(itemId && item.id.startsWith(itemId)){
        item.itemScore = itemScore;
      } else {
        itemScore = item.itemScore;
        itemId = item.id;
      }
    }
    const values = {
      id: configId.value,
      typeId: typeId.value,
      configName: dataSource[0].text,
      configJson: JSON.stringify({
        config: dataSource,
        mergeRuleList: mergeRuleList.value,
      })
    };
    //提交表单
    await saveOrUpdate(values, isUpdate.value);
    //关闭弹窗
    closeModal();
    //刷新列表
    emit('success');
  } catch (error) {
    console.log('error :>> ', error);
  } finally {
    setModalProps({ confirmLoading: false });
  }
}

//填报表单提交事件
async function handleFillSubmit() {
  try {
    setModalProps({ confirmLoading: true });
    let newArr = [] as any;
    let totleScore = 0;
    //循环表格数据
    for(const item of getDataSource()){
      if((item.itemScore || item.scoreText) && "最佳实践评审打分表" !== item.scoreText){
        //将评审的列数据转换成行数据
        for(let i = 0; i < fieldNameArr.value.length; i++){
          const key = fieldNameArr.value[i];
          //如果新数据为空则新建一个对象，否则在原有基础上添加数据
          if(null == newArr[i]){
            newArr[i] = {
              id: extItemIds.value[key], //设置id
              fillType: fillType.value,
              fillId: fillId.value,
              extNum: extNum.value,
              assessor: item[key], //设置评审人
              assessorItem: key, //设置评审项标识
              processData: []//配置数据
            };
          }
          if(item.id === "influenceText"){  //单独提取出评审人岗位
            newArr[i].assessorPosition = item[key];
          }
          if(item.id === "requirement"){ //单独提取出是否违反
            newArr[i].redType = item[key];
          }
          if(item.id === "remark"){ //单独提取出总分
            newArr[i].collectValue = item[key];
            totleScore += item[key];
          }
          //配置数据
          newArr[i].processData.push({
            id: item.id,
            text: item.scoreText,
            score: item[key],
            flag: key
          });
        }
      }
    }

    const oneObj = newArr[0];
    newArr = newArr.filter(item => {
      if (!item.assessor) {
        // 如果 评审人 为空，则过滤掉这个元素
        return false;
      } else {
        // 如果 评审人 不为空，将processData转换为json字符串
        item.processData = JSON.stringify(item.processData);
        return true;
      }
    });
    if(newArr.length === 0){
      oneObj.processData = JSON.stringify(oneObj.processData);
      newArr.push(oneObj);
    }
    // 提交表单
    await reviewSaveOrUpdate({
      fillId: fillId.value,
      fillType: fillType.value,
      fillReportId: fillReportId.value,
      list: newArr
    }, isUpdate.value);
    //关闭弹窗
    closeModal();
    //刷新列表
    emit('success', fillType.value, (totleScore/newArr.length).toFixed(2), fillReportId.value);
  } catch (error) {
    console.log('error :>> ', error);
  } finally {
    setModalProps({ confirmLoading: false });
  }
}

//配置表改变事件
async function handleConfigChange(value) {
  const res = await queryById({ id: value });
  configJson.value = res?.configJson ?? '';
  configId.value = res.id;
  extNum.value = 0;
  await renderingTable();
  if(!isUpdate.value){
    await addScoreItem(null);
  }
}


//渲染表格
async function renderingTable() {
  mergeRuleList.value = cloneDeep(defaultMergeRuleList);
  let dataSource = cloneDeep(defaultDataSource);
  if (isFill.value|| unref(isUpdate.value)) {
    //处理配置项json数据
    const jsonObj = configJson.value ? JSON.parse(configJson.value) : '';
    if (jsonObj) {
      mergeRuleList.value = jsonObj.mergeRuleList;
      dataSource = jsonObj.config;
    }
  }
  await setCustomCell(configColumns);
  await setTableData(dataSource);
}

//扩展打分表
async function addScoreItem(colName) {
  extNum.value++;
  let scoreValueId = 'score_value' + buildShortUUID();
  //获取当前列信息
  let colInfo = getColumns();
  //获取列长，添加新列
  const colLen = getColumns().length;
  const fieldName = colName ?? scoreValueId;
  fieldNameArr.value.push(fieldName);
  colInfo[colLen] = {
    title: fieldName,
    align: "center",
    dataIndex: fieldName,
    ellipsis: false,
  }
  for (const key in mergeRuleList.value) {
    if (key === 'title') {
      mergeRuleList.value.title[5].colSpan++;
      mergeRuleList.value[key][colLen - 1] = { rowSpan: 0, colSpan: 0 };
    } else if (!key.startsWith('requirement') && !key.startsWith('influence')) {
      mergeRuleList.value[key][colLen - 1] = { rowSpan: 1, colSpan: 1 };
    } else {
      mergeRuleList.value[key][colLen - 1] = mergeRuleList.value[key][5];
    }
  }
  //获取当前表格信息
  let dataSource = getDataSource();
  for (const item of dataSource) {
    item[fieldName] = item.id === 'requirement' || item.id === 'remark' ? '0' : '';
  }

  await setTableData(dataSource);
  await setCustomCell(colInfo);
}

//计算分数
function handleCalcScore(column){
  //总分数
  let total = 0;
  //获取所有需要打分的主项
  const mainArr = getDataSource().filter(item => item.itemScore && !item.id.includes('_child_'));
  //获取是否违反，不违反的话计算总分数
  const findObj = getDataSource().find(item => item.id === 'requirement') ?? {};
  if(findObj[column.dataIndex] == 0){
    //循环主项，获取子项分数
    for(const obj of mainArr){
      let score = 0;
      //循环表格获取子项分数
      getDataSource().forEach(item => {
        if(item.id.startsWith(obj.id)){
          score += item[column.dataIndex] ? Number(item[column.dataIndex]) : 0;
        }
      })
      //如果分数超过当前项设置的最大分数，则分数为当前项设置的最大分数
      if(score > obj.itemScore){
        score = obj.itemScore;
      }
      total += score;
    }
  }
  getDataSource()[getDataSource().length-1][column.dataIndex] = total;
}
//删除打分表
function deleteScore(){
  createConfirm({
    title: '提示',
    content: `是否删除当前评审分数表数据(如果删除部门评分，工厂评分也会同步删除)？`,
    iconType: 'warning',
    okText: '删除',
    onOk: async () => {
      await deleteByType({fillId: fillId.value, fillType: fillType.value}, async () => {
        emit('success', fillType.value, null, fillReportId.value);
        //关闭弹窗
        closeModal();
      });
    },
  });
}
</script>

<style lang="less" scoped>
/** 时间和数字输入框样式 */
:deep(.ant-input-number) {
  width: 100%;
}

:deep(.ant-calendar-picker) {
  width: 100%;
}

textarea {
  word-break: normal;
  white-space: normal;
  overflow-wrap: break-word;
}

.icon {
  cursor: pointer;
}
</style>