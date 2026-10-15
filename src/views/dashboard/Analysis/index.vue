<template>
  <IndexChart v-if="indexStyle === 0"></IndexChart>
  <IndexDef v-if="indexStyle === 1"></IndexDef>
  <IndexBdc v-if="indexStyle == 2"></IndexBdc>
  <IndexTask v-if="indexStyle == 3"></IndexTask>
  <div style="width: 100%; text-align: right; margin-top: 20px">
    首页主题：
    <a-radio-group v-model:value="indexStyle">
      <a-radio :value="0">{{ t('language.home') }}</a-radio>
      <a-radio :value="1">{{ t('language.salesStatistics') }}</a-radio>
      <a-radio :value="2">{{ t('language.businessStatistics') }}</a-radio>
      <a-radio :value="3">{{ t('language.myAssignment') }}</a-radio>
    </a-radio-group>
  </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue';
import IndexDef from './homePage/IndexDef.vue';
import IndexChart from './homePage/IndexChart.vue';
import IndexBdc from './homePage/IndexBdc.vue';
import IndexTask from './homePage/IndexTask.vue';
import Cookies from 'js-cookie';
import { getToken } from '/@/utils/auth';
import { setStorageDepart } from '/@/utils/ktTool';
import { useUserStore } from '/@/store/modules/user';

const indexStyle = ref(0);

//设定缓存中工厂和部门值;
const userStore = useUserStore();
const userInfo = userStore.getUserInfo;
setStorageDepart(userInfo.orgCode);
import { useI18n } from '/@/hooks/web/useI18n';
const { t } = useI18n();

// console.log('userInfo::::', userInfo);

Cookies.set('X-Access-Token', getToken(), { expires: 5 }); //创建有效期为27天的cookie
</script>
