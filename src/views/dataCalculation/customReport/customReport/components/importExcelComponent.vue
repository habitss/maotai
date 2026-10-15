<template>
  <div class="import-excel">
    <a-upload name="file" :multiple="true" @change="fileHandleChange" :before-upload="beforeUpload">
      <a-button type="primary" class="btn-style" :disabled = "btnLoading"> <a-icon type="upload"  /> 导入 </a-button>
      <a-spin :spinning="btnLoading"></a-spin>
    </a-upload>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import * as XLSX from 'xlsx'
import { useMessage } from '/@/hooks/web/useMessage';

//消息提示
const { createMessage: msg } = useMessage();

//按钮loding
const btnLoading = ref(false);

//父组件传值
const props = defineProps({
  excelCol: { type: Array, default: [] },
});

//声明emit
const emit = defineEmits([
  'getTableDate',
  'getOriginalData',
])

const xlsxJson = ref({});
const importExcelModalData = ref([]);

function fileHandleChange(fileObject) {
  let file = fileObject.file
  if (file.size >= 30 * 1024 * 1024) {
    msg.warning('文件过大，超过30M，请重新选择！')
    return
  }

  const types = file.name.slice(file.name.lastIndexOf('.') + 1)
  const fileType = ['xlsx', 'xlc', 'xlm', 'xls', 'xlt', 'xlw', 'csv', 'xlsm'].some(
    item => item === types
  )
  if (!fileType) {
    msg.warning('文件格式错误，请重新选择！')
    return
  }

  btnLoading.value = true
  file2Xce(file).then(tabJson => {
    if (tabJson && tabJson.length > 0) {
      xlsxJson.value = tabJson
      btnLoading.value = false
      importExcelModalData.value = []
      tabJson.forEach((item, index) => {
        importExcelModalData.value.push({ "sheetName": item.sheetName, "sheetPosition": `sheet${index}` })
        // sheetList.push({ "value": `sheet${index}`, "title": `sheet${index}` })
      })
      let res = {
        returnExcel: tabJson,
      }
      emit('getTableDate', res)
      emit('getOriginalData', xlsxJson.value)
    } else {
      msg.error('导入文件有误，请重新选择！')
    }
  })
}

function file2Xce(file) {
  return new Promise(function (resolve, reject) {
    const reader = new FileReader()
    reader.onload = function (e) {
      const data = e.target?.result

      const wb = XLSX.read(data, {
        type: 'binary',
        cellDates: true,
      })
      const result = [] as any;

      wb.SheetNames.forEach((sheetName) => {
        const worksheet = wb.Sheets[sheetName];
        // 获取合并单元格信息  
        let merges = worksheet['!merges'] || []; 
        if(worksheet['!ref']){
          // 找到工作表中使用的最大行号和列号（基于1的索引）  
          const range = XLSX.utils.decode_range(worksheet['!ref']);  
          const maxRow = range.e.r; // 最大行号（基于0的索引，但通常我们基于1来考虑Excel行号）  
          const maxCol = range.e.c; // 最大列号（转换为基于0的索引）
        
          result.push({
            sheetName: sheetName,
            sheet: XLSX.utils.sheet_to_json(wb.Sheets[sheetName],{ header: 1 }),
            merges:merges,
            maxRow:maxRow,
            maxCol:maxCol
          })
        }
      })

      resolve(result)
    }
    reader.readAsBinaryString(file)
  })
}

function beforeUpload(file) {
  return false;
}
</script>
<style lang="less" scoped>

.import-excel{
  
  ::v-deep .ant-upload-list-text {
    display: none;
  }
  
}

</style>