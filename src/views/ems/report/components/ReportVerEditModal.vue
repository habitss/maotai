<template>
  <j-modal
    :title="title"
    :width="width"
    :maxHeight="800"
    :style="{ top: '60px' }"
    :visible="visible"
    :footer="null"
    @cancel="handleCancel"
    cancelText="关闭"
  >
    <a-spin :spinning="modalLoading">
      <div class="modal-main">
        <a-form
          ref="modifyFormRef"
          :model="modelRepVerEditForm"
          :rules="editReportVerFormRules"
          layout="inline"
          :label-col="{ style: { width: '100px' } }"
          :wrapper-col="{ span: 12 }"
        >
          <a-form-item v-bind="validateInfos.excelFile" name="excelFile" :wrapper-col="{ span: 24 }">
            <a-upload
              :file-list="fileList"
              class="custom-upload"
              :before-upload="() => false"
              :max-count="1"
              accept=".xlsx,.xls"
              @change="uploadOnChange"
            >
              <a-button type="primary">选择文件</a-button>
            </a-upload>
          </a-form-item>

          <a-form-item v-bind="validateInfos.effectSdate" name="startTime" label="生效日期：" class="txt-edate">
            <a-date-picker
              v-model:value="modelRepVerEditForm.effectSdate"
              style="width: 128px"
              format="YYYY-MM-DD"
              valueFormat="YYYY-MM-DD"
              :disabled="!!modelRepVerEditForm.id"
              placeholder="生效日期"
            />
          </a-form-item>

          <a-form-item v-bind="validateInfos.verName" name="versionName" label="版本名称：" :wrapper-col="{ style: { width: '200px' } }">
            <a-input v-model:value="modelRepVerEditForm.verName" allow-clear placeholder="请输入版本名称" />
          </a-form-item>

          <a-form-item
            v-bind="validateInfos.verDesc"
            name="versionDescription"
            label="版本备注："
            class="txt-desc"
            :wrapper-col="{ style: { width: '400px' } }"
          >
            <a-input v-model:value="modelRepVerEditForm.verDesc" allow-clear placeholder="请输入版本备注" />
          </a-form-item>
        </a-form>

        <div class="spreadContainer">
          <gc-spread-sheets hostClass="spreadHost" @workbook-initialized="workbookInitialized">
            <GcWorksheet />
          </gc-spread-sheets>
        </div>
        <div class="footer-btn">
          <a-button type="primary" @click="saveReportVersion">提 交</a-button>
          <a-button type="default" @click="handleCancel" style="margin-left: 20px">取 消</a-button>
        </div>
      </div>
    </a-spin>
  </j-modal>
</template>

<script lang="ts" setup>
  import { ref, reactive, nextTick, defineExpose } from 'vue';
  import { Form } from 'ant-design-vue';
  import JModal from '/@/components/Modal/src/JModal/JModal.vue';
  import '@grapecity/spread-sheets/styles/gc.spread.sheets.excel2016colorful.css';
  import * as GC from '@grapecity/spread-sheets';
  import { GcSpreadSheets, GcWorksheet } from '@grapecity/spread-sheets-vue';
  import '@grapecity/spread-sheets-resources-zh';
  import '@grapecity/spread-sheets-io';
  import '@grapecity/spread-sheets-charts';
  import '@grapecity/spread-sheets-shapes';
  import dayjs from 'dayjs';
  import * as MySpreadUtils from '@/utils/spreadUtils';
  import { useMessage } from '@/hooks/web/useMessage';
  import { UploadRepVersionParams, saveReportVersionX } from '../ReportVersion.api';
  const emit = defineEmits(['success']);

  const { createMessage, createConfirm } = useMessage();

  // 初始化 Spread
  GC.Spread.Common.CultureManager.culture('zh-cn');
  let SpreadJSKey =
    'emsapac.ab-inbev.cn,623376735916589#B1v4r7N0dHWyBDcwkXVMJ7Qr9UWtpWUYlzbzgjUORkUY3GOzEVNHZFNp5kZGdXc4xEbvAVUOh6VGJWWWdTORJjTMRWdyoXS4B5MJNHdwAlVmt6MRNTb4gTTxMzVrUTNhZldQ94Yx2yVrlzT4JFV6U7KQdlMB3SZmVFbzUFTxJVVN9kUNdFOHJXcDN4YxpmTPl5VsBXRz2UQvsGcyAHTqlEa8YmRk5UMhd4dCtkTDpmVCl6aQdETKJmUNNUZyAnSrd6Y68kc8VnV62GN4Elc4QjWFR6Sv2iWy24Yt5UYFR7RhZXaXNjU6hjUY3WcJhmetNUR5ZGTZNkS85EcU3GbwdlI0IyUiwiI8QUNyADNGZjI0ICSiwSO7AzNyQTO8UTM0IicfJye&Qf35VficFVDplI0IyQiwiI6EjL6ByUKBCZhVmcwNlI0IiTis7W0ICZyBlIsISOzMDM8ADI9ETNwMjMwIjI0ICdyNkIsIibj9idlJmbp5iYh9yYhBXYz5WZiojIz5GRiwiI8+Y9sWY9QmZ0Jyp9Li09leb9WyY9oqY9qeI0R+K0+0L0JGr9m6q9iojIh94QiwiI9gTN6ETO5MzN6czMzIjNiojIklkIs4XXbpjInxmZiwSZzxWYmpjIyNHZisnOiwmbBJye0ICRiwiI34TQmBjSNNlZJtUOGxEVxZje75kY8MjWMdER6QmMrg7RmZTbLRUQRp7cM9GZ5dWRv2kTsdUN6onewpnepB5ZhljWxN4dnRUaSdTOGNWWUxkbFhlZrpVb7q3'; // Enter a valid license key.
  GC.Spread.Sheets.LicenseKey = SpreadJSKey;

  const modalLoading = ref<boolean>(false);
  const curSpread = ref<GC.Spread.Sheets.Workbook | null>(null);
  const title = ref<string>('');
  const width = ref<number>(1600);
  const visible = ref<boolean>(false);

  // 表单引用
  const modifyFormRef = ref();
  const fileList = ref([]);
  const useForm = Form.useForm;

  const historyRepVerList = ref<any[]>([]); // 历史版本列表

  // 表单绑定对象
  const modelRepVerEditForm = reactive({
    id: null,
    repid: 0,
    verName: '',
    verDesc: '',
    effectSdate: '',
    excelFile: null,
  } as {
    id: number | null;
    repid: number;
    verName: string;
    verDesc: string;
    effectSdate: string;
    excelFile: File | null;
  });

  //表单验证
  const editReportVerFormRules = reactive({
    verName: [
      { required: true, message: '版本名称不能为空！' },
      { max: 20, message: '不得超过20字符' },
    ],
    verDesc: [{ max: 100, message: '不得超过100字符' }],
    effectSdate: [
      {
        validator: async (_rule, value) => {
          if (!value) {
            return Promise.reject('生效日期不能为空');
          }
          const eSdate = dayjs(value);
          const isAfterRepVer = historyRepVerList.value.some(
            (elem) => dayjs(elem.effectSdate).isSame(eSdate, 'day') || dayjs(elem.effectSdate).isAfter(eSdate, 'day')
          );

          if (!modelRepVerEditForm.id && isAfterRepVer) {
            return Promise.reject('您选择的生效日期与已存在的版本生效日期冲突');
          }

          return Promise.resolve();
        },
      },
    ],
    excelFile: [
      {
        required: true,
        validator: async (_rule, value) => {
          if (!value || !value.name) {
            return Promise.reject('请上传 Excel 文件！');
          }
          const fileName = value.name;
          const validExt = /\.(xlsx|xls)$/i.test(fileName);
          if (!validExt) {
            return Promise.reject('只支持上传 .xlsx 或 .xls 文件');
          }
          return Promise.resolve();
        },
      },
    ],
  });
  const { resetFields, validate, validateInfos } = useForm(modelRepVerEditForm, editReportVerFormRules, { immediate: false });

  // WorkbookInitialized 回调
  const workbookInitialized = (spread: GC.Spread.Sheets.Workbook) => {
    curSpread.value = spread;
    //绑定 Sheet 页切换事件
    spread.bind(GC.Spread.Sheets.Events.ActiveSheetChanged, () => {
      const sheet = curSpread.value!.getActiveSheet();
      sheet.resumePaint();
      sheet.repaint();
    });
    curSpread.value.refresh();
    console.log(`[${dayjs().format('YYYY-MM-DD HH:mm:ss')}]Init Spread Completed: ${spread}`); // 使用 dayjs 格式化日期
  };

  const uploadOnChange = ({ file }) => {
    if (file.status === 'removed') {
      MySpreadUtils.cleanSpread(curSpread.value);
      modelRepVerEditForm.excelFile = null;
      return;
    }
    modelRepVerEditForm.excelFile = file;
    modalLoading.value = true;
    curSpread.value!.import(
      file,
      () => {
        modalLoading.value = false;
      },
      (error) => {
        console.error(error);
        modalLoading.value = false;
        createConfirm({
          iconType: 'warning',
          title: '系统提示',
          content: '加载Excel时发现存在格式问题！确定要继续吗?',
          okText: '确认',
          cancelText: '取消',
          onOk: () => {},
          onCancel() {
            MySpreadUtils.cleanSpread(curSpread.value);
          },
        });
      }
    );
  };

  //保存报表版本数据
  const saveReportVersion = async () => {
    try {
      // 触发表单验证
      await validate();
    } catch (err) {
      const error = err as { errorFields?: Array<{ name: string }> };
      return Promise.reject(error.errorFields);
    }
    modalLoading.value = true;

    if (!modelRepVerEditForm.excelFile) {
      createMessage.error('请先选择报表 Excel 文件');
      return;
    }
    //上传参数
    const data = { ...modelRepVerEditForm };
    if (data.id === null) {
      delete (data as any).id;
    }
    const params: UploadRepVersionParams = {
      file: modelRepVerEditForm.excelFile,
      name: 'excelFile',
      data,
    };
    await saveReportVersionX(params, (res) => {
      if (res.success) {
        createMessage.success(res.message || '上传成功');
        modalLoading.value = false;
        const repid = modelRepVerEditForm.repid;
        clearForm();
        nextTick(() => {
          visible.value = false;
          emit('success', repid);
        });
      } else {
        createMessage.error(res.message || '上传失败');
        modalLoading.value = false;
      }
    });
  };

  /**
   * 新增
   */
  function add() {
    edit({});
  }

  /**
   * 清理表单
   */
  function clearForm() {
    resetFields();
    // 调用组件内置方法
    fileList.value = [];
    MySpreadUtils.cleanSpread(curSpread.value);
  }

  /**
   * 编辑
   */
  function edit(record) {
    if (!record.id) {
      title.value = '新增报表历史版本';
    } else {
      title.value = '编辑报表历史版本';
    }
    visible.value = true;
    nextTick(() => {
      resetFields();
      const tmpData = {};
      Object.keys(modelRepVerEditForm).forEach((key) => {
        if (record.hasOwnProperty(key)) {
          tmpData[key] = record[key];
        }
      });
      //赋值
      Object.assign(modelRepVerEditForm, tmpData);
    });
  }

  /**
   * 取消按钮回调事件
   */
  function handleCancel() {
    visible.value = false;
  }

  defineExpose({
    add,
    edit,
  });
</script>
<style lang="less" scoped>
  .modal-main {
    padding: 10px 20px;
    .custom-upload {
      display: flex;
      :deep(.ant-upload-list) {
        margin-left: 10px;
      }
    }
    .spreadContainer {
      padding: 10px;
      padding-left: 0;
      height: 70vh;
    }
    .spreadHost {
      height: 100%;
    }
    .footer-btn {
      margin-top: 10px;
      text-align: center;
    }
    .footer-btn button {
      width: 90px;
      height: 42px;
      font-size: 15px;
      border-radius: 0;
    }
  }
</style>
<style lang="less">
  /**隐藏样式-modal确定按钮 */
  .jee-hidden {
    display: none !important;
  }
</style>
