<template>
  <BasicModal @register="registerModal" v-bind="$attrs" :title="title" :width="width" @ok="handleSubmit"
              :cancelText="t('language.closeText')" :showOkBtn='false'>

      <a-row :gutter="24" v-for="col in colValue" :key="col.id">
        <a-col :span="24">
          <a-button type="link" @click="showDetail(col)">
            {{col}}</a-button>
        </a-col>
      </a-row>
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { useMessage } from '/@/hooks/web/useMessage';
import { useI18n } from '/@/hooks/web/useI18n';
import {useGlobSetting} from "@/hooks/setting";
import {useUserStore} from "@/store/modules/user";
import {factoryPath} from "@/views/dataCalculation/periodCheck/periodPlan/PeriodScheduleList.api";
import {encryptByBase64} from "@/utils/cipher";

const { t } = useI18n();

// 禁用
const disabledRef = ref(false);

//需修改记录的id
const recordId = ref();
const colValue = ref([]);
//5w组件ref
const title = ref<string>('异常文件查看');
const width = ref<number>(600);
const globSetting = useGlobSetting();
const userStore = useUserStore();

const emit = defineEmits(['success']);

//注册modal
const [registerModal, { closeModal }] = useModalInner(async (data: any) => {
  console.log('data',data)
  colValue.value = [];
  let file = data.record;
  console.log('data',file.exceptionFile)
  if(file.exceptionFile.includes(',')){
    colValue.value = file.exceptionFile.replaceAll('temp/','').split(',')
  } else {
    colValue.value.push(file.exceptionFile.replace('temp/',''));
  }
  console.log('data',colValue.value)
});

//注册Message
const { createMessage: msg } = useMessage();

// function showDetail(record){
//   console.log(record)
//   let path = '/keytech-boot/mes3_common/static/temp/' + record;
//   window.open(path, '_blank');
// }
async function showDetail(record) {
  let ip = await factoryPath({factoryCode: userStore?.userInfo?.orgCode})
  let path = ip + 'keytech-boot/mes3_common/static/temp/' + record;//要预览文件的访问地址
  console.log(path)
  window.open(ip + 'preview/onlinePreview?url=' + encodeURIComponent(encryptByBase64(path)), '_blank');
}

</script>

<style>
/**隐藏样式-modal确定按钮 */
.jee-hidden {
  display: none !important;
}
</style>
