<template>
  <a-spin :spinning="pageLoading">
    <div class="p-2">
      <div class="jeecg-basic-table-form-container">
        <div class="spreadContainer">
          <SpreadJsModule ref="spreadJsRef" ></SpreadJsModule>
        </div>
      </div>
    </div>
  </a-spin>
</template>

<script lang="ts" setup>
  import {ref,onMounted} from 'vue';
  import { useRoute } from 'vue-router';
  import { queryById } from '../config/PiSpreadjsMain.api';
  import SpreadJsModule from '../spreadJsModule/index.vue';
  const route = useRoute();
  //获取路由中的code参数
  const code = route.params.code;
  const queryParam = ref<any>({});
  //const queryParams = ref(route.query);
  //const tableId = ref(queryParams.value.tableId);

  //子组件实例
  const spreadJsRef = ref();
  const pageLoading = ref(false);
  
  async function init(){
    if(code){
      //let data = route;
      queryParam.value.tableId =code;
      //获取报表配置
      const res = await queryById({id: code});
      if(res?.code === 200){
        // let obj = {id:code}
        spreadJsRef.value.initData(res.result);
      }
      //设置标题
      //title.value = data.record.templateName;
      // //设置数据记录
      //record.value = data.record;
      //
      //获取表头
      //tableChange(tableId.value,{});
    }
    //getTableArr();
  }


  onMounted(() => {
    init()
  });
</script>

<style lang="less" scoped>
  .jeecg-basic-table-form-container {
    padding: 0;
    .spreadContainer {
      padding: 0px;
      padding-left: 0;
      height: 90vh;
    }
    .spreadHost {
      width: 99%;
      height: 100%;
    }
    .custom-upload {
      display: flex;
      /deep/ .ant-upload-list {
        margin-left: 10px;
      }
    }
  }

  .div-border {
    border: 1px solid #C1C0B6;   /* 1px 实线 灰色边框 */
  }

</style>
