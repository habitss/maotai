<template>
  <j-modal :width="width" :visible="visible" :footer="null" :maxHeight="800" cancelText="关闭" @cancel="handleCancel">
    <template #title>
      <span> {{ title }} </span>
      <a-button type="primary" v-auth="'ems:ems_report_version:add'" @click="add" style="margin-left: 20px"> 添加新版本</a-button>
    </template>
    <a-spin :spinning="modalLoading">
      <div class="modal-main">
        <div class="flex-row flex-vcenter ors-formula-list">
          <div
            class="flex-col flex-hcenter flex-vcenter"
            :class="{ selected: nowTagFormulaInx === inx }"
            v-for="(item, inx) in tagFormulaList"
            :key="item.id"
            style="margin: 0px 10px; padding: 5px; cursor: pointer"
            @click="showTagFormulaInfo(inx)"
          >
            <div><img :src="getPngIcon('rep-ver-period')" alt="" style="width: 30px; height: 30px" /></div>
            <div style="margin-top: 5px">{{ item.effectSdate }}</div>
          </div>
          <div v-if="tagFormulaList.length == 0" class="not-set-msg">暂未配置公式</div>
        </div>
        <div class="ors-formula-form">
          <div v-if="nowTagFormulaInx >= 0"><span class="tit">公式详情</span></div>
          <OrsdataFormulaForm
            ref="registerForm"
            @ok="submitCallback"
            :formDisabled="disableSubmit"
            @update="updateOrsdataFormula"
            @delete="deleteOrsdataFormula"
            :formBpm="false"
          />
        </div>
        <div style="padding-top: 10px; text-align: center">
          <a-button type="primary" v-auth="'ems:ems_report_version:update'" @click="updateOrsdataFormula" style="margin-left: -10px">保存</a-button>
          <a-button
            v-if="nowTagFormulaInx >= 0"
            type="primary"
            v-auth="'ems:ems_report_version:delete'"
            @click="deleteOrsdataFormula"
            style="margin-left: 15px"
            >删除</a-button
          >
          <a-button type="default" v-auth="'ems:ems_report_version:delete'" @click="handleCancel" style="margin-left: 15px">返回</a-button>
        </div>
      </div>
    </a-spin>
  </j-modal>
</template>

<script lang="ts" setup>
  import { ref, nextTick, defineExpose } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import OrsdataFormulaForm from './OrsdataFormulaForm.vue';
  import JModal from '/@/components/Modal/src/JModal/JModal.vue';
  import { getPngIcon } from '../../report/ReportSetting.api';
  import { list, deleteOne } from '.././OrsdataFormula.api';
  import { OrsdataFormula } from '../OrsdataFormula.data';
  import dayjs from 'dayjs';
  const { createMessage, createConfirm } = useMessage();

  const modalLoading = ref<boolean>(false);
  const title = ref<string>('');
  const width = ref<number>(1200);
  const visible = ref<boolean>(false);
  const disableSubmit = ref<boolean>(false);
  const registerForm = ref();
  const tagTypeList = ref<[]>([]);
  //记录一下当前的中间点位ID
  const nowTagid = ref<string>();
  const nowTagName = ref<string>();
  const tagFormulaList = ref<OrsdataFormula[]>([]);
  const nowTagFormulaInx = ref<number>(-1);

  /**
   * 新增
   */
  function add() {
    visible.value = true;
    nowTagFormulaInx.value = -1;
    nextTick(() => {
      const record: any = { tagId: nowTagid.value };
      //第一个公式有效时间要设置早一点
      if (tagFormulaList.value.length == 0) {
        record.effectSdate = '2020-01-01';
      } else {
        record.effectSdate = dayjs().format('YYYY-MM-DD');
      }
      record.verName = nowTagName.value + '-' + dayjs().format('YYYYMMDD');
      edit(record);
    });
  }

  /**
   * 编辑
   * @param record
   */
  function edit(record) {
    visible.value = true;
    nextTick(() => {
      registerForm.value.edit(record, tagTypeList.value);
    });
  }

  /**
   * form保存回调事件
   */
  function submitCallback() {
    showTagFormulaList(nowTagid.value ?? '0', nowTagName.value ?? '', tagTypeList.value ?? []);
  }

  /**
   * 取消按钮回调事件
   */
  function handleCancel() {
    visible.value = false;
  }

  /**
   * 显示报表版本列表
   * @param repid
   */
  function showTagFormulaList(tagid: string, tagName: string, tagTList: []) {
    tagTypeList.value = tagTList;
    modalLoading.value = true;
    nowTagid.value = tagid;
    nowTagName.value = tagName;
    title.value = '中间点位公式配置';
    visible.value = true;
    nextTick(() => {
      const params = {
        tagId: tagid,
        pageSize: 9999,
        orderField: 'effect_sdate',
        orderType: 'asc',
        selectFields: 'id,tag_id,ver_name,ver_desc,effect_sdate,formula_origin,formula_desc,create_by,update_by,update_time',
      };
      list(params)
        .then((res) => {
          tagFormulaList.value = [...res.records];
          if (tagFormulaList.value.length > 0) {
            nowTagFormulaInx.value = tagFormulaList.value.length - 1;
            showTagFormulaInfo(nowTagFormulaInx.value);
          } else {
            add();
          }
        })
        .finally(() => {
          modalLoading.value = false;
        });
    });
  }

  /**
   * 显示指定公式版本详情
   * @param record
   */
  function showTagFormulaInfo(inx) {
    nowTagFormulaInx.value = inx;
    const record = tagFormulaList.value[inx];
    if (!record.updateBy) {
      record.updateBy = record.createBy;
    }
    if (inx >= tagFormulaList.value.length - 1) {
      record.effectEdate = '2088-01-01';
    } else {
      record.effectEdate = tagFormulaList.value[inx + 1].effectSdate;
    }
    edit(record);
  }

  /**
   * 更新指定版本的公式
   */
  function updateOrsdataFormula() {
    registerForm.value.submitForm();
  }

  //删除指定版本的公式
  function deleteOrsdataFormula() {
    if (tagFormulaList.value.length <= 1) {
      createMessage.warning('最后一个公式版本无法删除');
      return;
    }
    const record = tagFormulaList.value[nowTagFormulaInx.value];
    createConfirm({
      iconType: 'warning',
      title: '确认删除',
      content: '是否删除当前选中的公式版本',
      okText: '确认',
      cancelText: '取消',
      onOk: () => {
        deleteOne({ id: record.id }, () => {
          showTagFormulaList(nowTagid.value ?? '0', nowTagName.value ?? '', tagTypeList.value ?? []);
        });
      },
    });
  }

  defineExpose({
    add,
    edit,
    showTagFormulaList,
    disableSubmit,
  });
</script>
<style lang="less" scoped>
  .modal-main {
    padding: 10px 20px;
    .ors-formula-list {
      border: 1px solid #f0f0f0;
      height: 80px;
      border-radius: 3px;
    }
    .ors-formula-form {
      border: 1px solid #f0f0f0;
      margin-top: 10px;
    }
    .selected {
      border: 2px solid #d92622;
      background-color: #d9262211 !important;
      border-radius: 5px;
    }
    .tit {
      font-size: 16px;
      font-weight: bold;
      padding: 10px;
      line-height: 40px;
    }
    .not-set-msg {
      padding-left: 10px;
      font-size: 18px;
      font-weight: bold;
      color: #d92622;
      width: 100%;
      text-align: center;
    }
  }
</style>
<style lang="less">
  /**隐藏样式-modal确定按钮 */
  .jee-hidden {
    display: none !important;
  }
</style>
