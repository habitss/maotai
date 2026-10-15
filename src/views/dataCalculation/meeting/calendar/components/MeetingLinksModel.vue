<template>
  <BasicModal @register="registerModal" v-bind="$attrs" :title="title" :width="width" @ok="handleSubmit"
              :cancelText="t('language.closeText')" :showOkBtn='false' :destroyOnClose="true">

      <a-row :gutter="24" v-for="col in colValue" :key="col.link">
        <a-col :span="24">
          <a-button type="link" @click="showDetail(col.linkAddress)">
            {{col.link}}</a-button>
        </a-col>
      </a-row>
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { useMessage } from '/@/hooks/web/useMessage';
import { linkList } from '../Meeting.api';
import { useI18n } from '/@/hooks/web/useI18n';
import {useGlobSetting} from "@/hooks/setting";
import {useUserStore} from "@/store/modules/user";

const { t } = useI18n();

// 禁用
const disabledRef = ref(false);

//需修改记录的id
const recordId = ref();
const colValue = ref([]);

const title = ref<string>('链接查看');
const width = ref<number>(600);
const globSetting = useGlobSetting();
const userStore = useUserStore();

const emit = defineEmits(['success']);

//注册modal
const [registerModal, { closeModal }] = useModalInner(async (data: any) => {
  console.log('data1',data)
  colValue.value = [];
  let result = await linkList({templateId: data.record.templateId});
  console.log('data2',result)
  colValue.value = result;
  colValue.value.push({link:data.record.link,linkAddress:data.record.linkAddress})
  console.log('data3',colValue.value)
});

//注册Message
const { createMessage: msg } = useMessage();

async function showDetail(record) {
  window.open(record, '_blank');
}

</script>

<style>
/**隐藏样式-modal确定按钮 */
.jee-hidden {
  display: none !important;
}
</style>
