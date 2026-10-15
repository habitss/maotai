<template>
  <a-spin :spinning="pageLoading">
    <div class="p-2">
      <div class="jeecg-basic-table-form-container">
        <div class="flex-row flex-vcenter" style="padding: 10px; background-color: #fff; font-weight: bold">
          <a-switch v-model:checked="isDrag" un-checked-children="" :checkedValue="true" :unCheckedValue="false" />
          <span style="margin-left: 5px; cursor: pointer" @click="isDrag = !isDrag">报表顺序调整</span>
        </div>
        <a-collapse v-model:activeKey="activeTypes" ghost>
          <a-collapse-panel v-for="typeItem in typeListWithData" :key="typeItem.typeId" :header="typeItem.typeName">
            <Draggable :list="typeItem.reportList" item-key="id" :disabled="!isDrag" @start="onDragStart" @end="(e) => onDragEnd(e, typeItem.typeId)">
              <template #item="{ element: jtem }">
                <div class="btnDiv">
                  <a-button :class="isDrag ? 'drag-btn' : ''" type="default" size="middle" @click="goPath(jtem.id, jtem.repName, jtem.repType)">
                    <img :src="getPngIcon(jtem.iconName)" alt="" style="width: 40px; height: 40px" />
                    <div class="btn-text">{{ jtem.repName }}</div>
                  </a-button>
                </div>
              </template>
            </Draggable>
          </a-collapse-panel>
        </a-collapse>
      </div>
    </div>
  </a-spin>
</template>

<script lang="ts" name="ems-ReportMap" setup>
  import { ref, onMounted } from 'vue';
  import { useRouter } from 'vue-router';
  import Draggable from 'vuedraggable';
  import { getReportsetMap, saveReportsetOrder, getPngIcon } from './ReportSetting.api';

  const router = useRouter();

  const isDrag = ref(false);
  const pageLoading = ref(true);

  const typeListWithData = ref<{ typeId: number; typeName: string; reportList: any[] }[]>([]);
  const activeTypes = ref<number[]>([]);

  function goPath(id: number, name: string, types: number) {
    const path = types === 9 ? '/breportShow' : '/ems/report/show';
    router.push({ path, query: { id, name } });
  }

  //加载数据
  async function listData() {
    pageLoading.value = true;
    const response = await getReportsetMap();
    typeListWithData.value = response || [];
    activeTypes.value = typeListWithData.value.filter((b) => b.reportList.length > 0).map((i) => i.typeId);
    pageLoading.value = false;
  }

  function onDragStart(e: any) {
    console.log('Drag Start:', e);
  }

  async function onDragEnd(e: any, typeId: number) {
    console.log(e);
    const typeItem = typeListWithData.value.find((t) => t.typeId === typeId);
    if (!typeItem) return;
    const upList = typeItem.reportList.map((elem, index) => ({
      id: elem.id,
      sort: index + 1,
    }));
    pageLoading.value = true;
    await saveReportsetOrder(upList);
    pageLoading.value = false;
  }

  onMounted(() => {
    listData();
  });
</script>

<style lang="less" scoped>
  .jeecg-basic-table-form-container {
    padding: 0;
    .table-page-search-submitButtons {
      display: block;
      margin-bottom: 24px;
      white-space: nowrap;
    }
    .btnDiv {
      display: inline-block;
      padding: 8px;
      .btn-text {
        display: block;
        margin-top: 12px;
        font-size: 12px;
      }
      .ant-btn {
        min-width: 100px;
        height: 110px;
        background: transparent;
        border: 2px solid transparent;
        font-weight: bold;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        &:hover {
          border: 2px solid #d92622;
        }
      }
      .drag-btn {
        border: 2px dashed #d92622;
        cursor: move;
      }
    }

    .ant-collapse {
      background-color: #ffffff;
      .ant-collapse-item {
        border-top: 1px solid #ed9c9a;
        /deep/ .ant-collapse-header {
          background-color: #ffffff;
          font-weight: bold;
          .anticon {
            display: none;
          }
        }
      }
      /deep/ .ant-collapse-content {
        background-color: #ffffff;
        .ant-collapse-content-box {
          padding: 10px;
          padding-block: 0px;
        }
      }
    }
  }
</style>
