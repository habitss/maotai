<template>
  <!-- <div class="home-style"> -->
    <!-- <div class="home-content">
      <a-card v-for="item in shortCutInfo" class="cart-item" :key="item.id" @click="(e) => openPageUrl(item)" hoverable>
        <template #cover>
          <img style="padding: 10px; max-height: 153px" :alt="VITE_GLOB_APP_TITLE" :src="getImageUrl(item)" />
        </template>
        <a-card-meta :title="item.name">
          <template #description><span class="cart-text" v-text="item.subTitle"></span></template>
        </a-card-meta>
      </a-card>
    </div> -->
  <!-- </div> -->

  <Factory v-if="pageType == 2"></Factory>
  <Workshop v-else></Workshop>
</template>

<script lang="ts" setup>
import { ref, nextTick, defineExpose, defineProps, computed, onMounted } from 'vue';
import { router } from '/@/router';
import { useI18n } from '/@/hooks/web/useI18n';

import Factory from '/@/views/dataCalculation/home/factory.vue';
import Workshop from '/@/views/dataCalculation/home/workshop.vue';
import { useUserStore } from '/@/store/modules/user';
const userStore = useUserStore();
const { t } = useI18n();
// name="work-home"
const title = ref<string>('');
const protocol = ref<string>('');
const VITE_GLOB_APP_TITLE = import.meta.env.VITE_GLOB_APP_TITLE
const shortCutInfo = ref<array>([]);
const pageType = ref<string>("1")


const props = defineProps({
  sectionList: { type: Array, default: () => [] },
});

const colWidth = computed(() => {
  if (props.sectionList.length === 2) {
    return '12';
  }
  return '8';
});

onMounted(() => {
  console.log(userStore)
  let dept = userStore?.userInfo?.orgCode
  pageType.value = dept?.length <= 9? "2":"1"

  // 定制化http协议
  const isSecureConnection = document.location.protocol === 'https:';
  protocol.value = isSecureConnection ? 'https://' : 'http://';
  if(import.meta.env.VITE_GROUP === 'ts' || import.meta.env.VITE_GROUP === 'mes3') {
    shortCutInfo.value = [
      {
        name: '包装数字化智能大屏',
        subTitle: '包装数字化智能大屏',
        imgUrl: '6.png',
        target: '/dashboard/highRiseBoard',
        id: 6,
      },
      {
        name: t('language.EfficiencySummary'),
        subTitle: t('language.EfficiencySummary'),
        imgUrl: '3.png',
        target: '/frame/1 xlzb-1',
        id: 3,
      },
      {
        name: t('language.EnergySummary'),
        subTitle: t('language.EnergySummary'),
        imgUrl: '1.png',
        target: '/frame/1 nyzongbiao 1',
        id: 1,
      },
      {
        name: '质量总表',
        subTitle: '质量总表',
        imgUrl: 'zhiliang2.png',
        target: '/frame/1 zhiliangzongbiao-1',
        id: 8,
      },
      {
        name: '4万1#线实时监控',
        subTitle: '4万1#线实时监控',
        imgUrl: '5.png',
        target: '/frame/4w1',
        id: 5,
      },
      {
        name: t('language.EfficiencyDaily'),
        subTitle: t('language.EfficiencyDaily'),
        imgUrl: '4.png',
        target: '/frame/1 xlrib-1',
        id: 4,
      },
      {
        name: t('language.EnergyDaily'),
        subTitle: t('language.EnergyDaily'),
        imgUrl: '2.png',
        target: '/frame/1 nyribao-1',
        id: 2,
      },
      {
        name: '质量日报',
        subTitle: '质量日报',
        imgUrl: 'zhiliang1.png',
        target: '/frame-1-zhiliangribao-1',
        id: 9,
      }
    ]
  } else {
    shortCutInfo.value = [
      {
        name: t('language.piIndicatorMonitoring'),
        subTitle: t('language.productionManagement'),
        imgUrl: '1.png',
        target: '/productionManagement/productionExecutiveManagement/lineWorkstation',
        id: 1,
      },
      {
        name: t('language.productionWorkOrderExecution'),
        subTitle: t('language.productionManagement'),
        imgUrl: '2.png',
        target: '/productionManagement/planManagement/workOrderManagement',
        id: 2,
      },
      {
        name: t('language.equipmentMonitoringDashboard'),
        subTitle: t('language.dataVisualization'),
        imgUrl: '3.png',
        target: '/frame/4w1',
        id: 3,
      },
      {
        name: t('language.jobRecordReport'),
        subTitle: t('language.piIndicatorMonitoring'),
        imgUrl: '4.png',
        target: '/report/post-record/report-list',
        id: 4,
      },
      {
        name: t('language.preservationRecordReport'),
        subTitle: t('language.piIndicatorMonitoring'),
        imgUrl: '5.png',
        target: '/report/post-record/run-report-list',
        id: 5,
      },
      {
        name: t('language.dataConfiguration'),
        subTitle: t('language.masterDataManagement'),
        imgUrl: '6.png',
        target: '/mainDataManage/deviceType/MdmDeviceTypeList',
        id: 6,
      },
    ]
  }
});

/**
 * 新增
 */
function add() {
  title.value = '新增';
  // visible.value = true;
  // nextTick(() => {
  //   registerForm.value.add();
  // });
}

function openPageUrl(item) {
  // console.log('item:', item);
  router.push({ path: item.target });
}

function getImageUrl(item) {
  let locationUrl = protocol.value + window.location.host + '/';
  let vitePublicPath = import.meta.env.VITE_PUBLIC_PATH;
  let vitePublicPathStr = "";
  if(vitePublicPath && vitePublicPath != '/'){
    vitePublicPathStr = vitePublicPath.replace(/\//g, '')
  }
  return `${locationUrl}`+vitePublicPathStr+`/resource/home/${item.imgUrl}`;
}

// defineExpose({
//   add,
// });
</script>

<style lang="less" scoped>
.home-style {
  width: 101%;
  height: 100%;
  background: url('@/assets/images/login-back.png') no-repeat center / 100% 100%;
  margin-left: -1px;
  .home-content {
    margin: 0 auto;
    width: 1200px;
    display: flex;
    flex-wrap: wrap;
    position: relative;
    top: calc((100% - 540px) / 4);
  }

  .cart-item {
    flex: 0 0 259px;
    justify-content: center;
    margin: 10px 10px;
    border-radius: 5%;
  }
  .cart-text {
    display: block;
    text-align: center;
  }
  /deep/.ant-card-meta-title {
    font-weight: bold;
    text-align: center;
    white-space: pre-wrap;
  }

  .cart-item:hover {
    transform: scale(1.05);
  }
}
</style>
