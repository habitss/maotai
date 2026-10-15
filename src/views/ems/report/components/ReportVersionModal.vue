<template>
  <j-modal :width="width" :visible="visible" :footer="null" cancelText="关闭" @cancel="handleCancel">
    <template #title>
      <span> {{ title }} </span>
      <a-button type="primary" v-auth="'ems:ems_report_version:add'" @click="addReportVersion" style="margin-left: 20px"> 新增版本</a-button>
    </template>
    <a-spin :spinning="modalLoading">
      <div class="modal-main">
        <div class="flex-row flex-vcenter rep-ver-list">
          <div
            class="flex-col flex-hcenter flex-vcenter"
            :class="{ selected: nowRepVerInx === inx }"
            v-for="(item, inx) in repVerList"
            :key="item.id"
            style="margin: 0px 10px; padding: 5px; cursor: pointer"
            @click="showVerInfo(inx)"
          >
            <div><img :src="getPngIcon('rep-ver-period')" alt="" style="width: 30px; height: 30px" /></div>
            <div style="margin-top: 5px">{{ item.effectSdate }}</div>
          </div>
        </div>
        <div class="rep-ver-form">
          <ReportVersionForm
            ref="registerForm"
            @ok="submitCallback"
            :formDisabled="disableSubmit"
            @update="updateReportVersion"
            @delete="deleteReportVersion"
            :formBpm="false"
          />
        </div>
      </div>
    </a-spin>
  </j-modal>
</template>

<script lang="ts" setup>
  import { ref, nextTick, defineExpose } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import ReportVersionForm from './ReportVersionForm.vue';
  import JModal from '/@/components/Modal/src/JModal/JModal.vue';
  import { getPngIcon } from '../ReportSetting.api';
  import { list, deleteOne } from '../ReportVersion.api';
  import { ReportVersion } from '../ReportVersion.data';
  const emit = defineEmits(['editRepVer', 'success']);
  const { createMessage, createConfirm } = useMessage();

  const modalLoading = ref<boolean>(false);
  const title = ref<string>('');
  const width = ref<number>(1200);
  const visible = ref<boolean>(false);
  const disableSubmit = ref<boolean>(false);
  const registerForm = ref();
  //记录一下当前的报表的 ID
  const nowRepid = ref<number>();
  const repVerList = ref<ReportVersion[]>([]);
  const nowRepVerInx = ref<number>();

  /**
   * 新增
   */
  function add() {
    title.value = '新增';
    visible.value = true;
    nextTick(() => {
      registerForm.value.add();
    });
  }

  /**
   * 显示报表版本列表
   * @param repid
   */
  function showVerList(repid) {
    modalLoading.value = true;
    nowRepid.value = repid;
    title.value = '历史版本管理';
    visible.value = true;
    nextTick(() => {
      const params = {
        repid: repid,
        pageSize: 9999,
        selectFields: 'id,repid,ver_name,ver_desc,effect_sdate,create_by,update_by,update_time',
      };
      list(params)
        .then((res) => {
          repVerList.value = [...res.records];
          if (repVerList.value.length > 0) {
            nowRepVerInx.value = repVerList.value.length - 1;
            showVerInfo(nowRepVerInx.value);
          } else {
            registerForm.value.add();
          }
        })
        .finally(() => {
          modalLoading.value = false;
        });
    });
  }

  /**
   * 显示指定版本详情
   * @param record
   */
  function showVerInfo(inx) {
    nowRepVerInx.value = inx;
    const record = repVerList.value[inx];
    if (!record.updateBy) {
      record.updateBy = record.createBy;
    }
    if (inx >= repVerList.value.length - 1) {
      record.effectEdate = '2088-01-01';
    } else {
      record.effectEdate = repVerList.value[inx + 1].effectSdate;
    }
    registerForm.value.edit(record);
  }

  /**
   * 编辑
   * @param record
   */
  function edit(record) {
    title.value = disableSubmit.value ? '详情' : '编辑';
    visible.value = true;
    nextTick(() => {
      registerForm.value.edit(record);
    });
  }

  /**
   * form保存回调事件
   */
  function submitCallback() {
    handleCancel();
    emit('success');
  }

  /**
   * 取消按钮回调事件
   */
  function handleCancel() {
    repVerList.value = [];
    registerForm.value.add();
    visible.value = false;
  }

  /**
   * 打开报表版本新增窗口
   */
  function addReportVersion() {
    //获取最新一个版本名，取前缀作为新版本前缀
    const lastVerName = repVerList.value.length > 0 ? repVerList.value[repVerList.value.length - 1].verName : 'NULL';
    emit('editRepVer', { verName: lastVerName, repid: nowRepid.value });
  }

  /**
   * 打开报表版本更新窗口
   */
  function updateReportVersion(record) {
    emit('editRepVer', { ...record });
  }

  //删除指定的版本
  function deleteReportVersion(id) {
    if (repVerList.value.length <= 1) {
      createMessage.warning('最后一个版本不能删除');
      return;
    }
    createConfirm({
      iconType: 'warning',
      title: '确认删除',
      content: '是否删除当前选中的报表版本',
      okText: '确认',
      cancelText: '取消',
      onOk: () => {
        deleteOne({ id: id }, () => {
          showVerList(nowRepid.value);
        });
      },
    });
  }

  defineExpose({
    add,
    edit,
    showVerList,
    disableSubmit,
  });
</script>
<style lang="less" scoped>
  .modal-main {
    padding: 10px 20px;
    .rep-ver-list {
      border: 1px solid #f0f0f0;
      height: 80px;
      border-radius: 3px;
    }
    .rep-ver-form {
      border: 1px solid #f0f0f0;
      margin-top: 10px;
    }
    .selected {
      border: 2px solid #d92622;
      background-color: #d9262211 !important;
    }
  }
</style>
<style lang="less">
  /**隐藏样式-modal确定按钮 */
  .jee-hidden {
    display: none !important;
  }
</style>
