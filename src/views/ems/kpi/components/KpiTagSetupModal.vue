<template>
  <a-modal
    v-model:open="computedOpen"
    title="KPI设置"
    :footer="null"
    :mask-closable="false"
    wrap-class-name="kpitag-modal-wrapper"
    style="top: 140px; width: 1000px"
    :body-style="{ padding: '10px' }"
    @cancel="handleCancel"
  >
    <a-spin :spinning="pageLoading">
      <a-steps v-model:current="stepCurrent" type="navigation" :style="stepStyle" :items="stepItems" />
      <!-- ---------------------------------- KPI点位和KPI点位阈值 --------------------------- -->
      <div class="flex-row flex-hcenter" style="margin: 30px 0px; height: 500px">
        <!-- 设置KPI点位 -->
        <div v-show="stepItems[stepCurrent].inx === 0" class="form-wrapper">
          <div class="sub-title">{{ '设置KPI' + (internalKpiTag.primaryTag === 1 ? '主' : '其他') + '点位' }}</div>
          <a-form ref="editTagFormRef" :model="internalKpiTag" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
            <a-form-item label="能源类型：" name="energyType">
              <a-input v-model:value="internalKpiTag.energyType" disabled />
            </a-form-item>

            <a-form-item label="点位大类：" name="tagKind" :rules="[{ required: true, message: '点位大类不能为空' }]">
              <j-dict-select-tag
                :disabled="internalKpiTag.primaryTag === 1"
                :showChooseOption="false"
                placeholder="请选择点位大类"
                v-model:value="internalKpiTag.tagKind"
                :options="tagKindList"
                @change="tagKindChange"
              />
            </a-form-item>

            <a-form-item label="关联点位：" name="tagId" :rules="[{ required: true, message: '关联点位不能为空' }]">
              <a-select
                v-model:value="internalKpiTag.tagId"
                placeholder="请选择点位"
                show-search
                :filter-option="false"
                @search="handleTagSearch"
                @popup-scroll="handleTagScroll"
                @change="handleTagChange"
              >
                <a-select-option v-for="item in tagList.slice(0, tagDisplayCount)" :key="item.tagId" :value="item.tagId">
                  [{{ item.tagId }}] {{ item.tagName }}{{ item.unit ? ` (${item.unit})` : '' }}
                </a-select-option>
              </a-select>
            </a-form-item>

            <a-form-item
              label="点位名称："
              name="kpiTagName"
              :rules="[
                { required: true, message: '名称不能为空！' },
                { max: 255, message: '不得超过255字符' },
              ]"
            >
              <a-input v-model:value="internalKpiTag.kpiTagName" placeholder="输入点位名称" />
            </a-form-item>

            <a-form-item label="单位：">
              <a-input v-model:value="internalKpiTag.unit" disabled />
            </a-form-item>
            <a-form-item label="值类型：">
              <j-dict-select-tag
                type="radio"
                v-if="internalKpiTag.tagKind === TagKindEnum.ORSDATA"
                disabled
                v-model:value="internalKpiTag.valType"
                :options="orsValTypeList"
              />
              <j-dict-select-tag
                type="radio"
                v-if="internalKpiTag.tagKind === TagKindEnum.COLLECT"
                disabled
                v-model:value="internalKpiTag.valType"
                :options="collectValTypeList"
              />
            </a-form-item>
            <div style="position: absolute; bottom: 10px; text-align: center; width: 100%">
              <a-button type="primary" @click="saveKpiTag" :disabled="pageLoading">{{ !!internalKpiTag.id ? '保存' : '下一步' }}</a-button>
              <a-button v-if="!!internalKpiTag.id" danger style="margin-left: 10px" @click="deleteKpiTag(internalKpiTag.id)"> 删除 </a-button>
              <a-button v-if="!!internalKpiTag.id" @click="stepCurrent += 1" style="margin-left: 10px">下一步</a-button>
            </div>
          </a-form>
        </div>

        <!-- KPI点位阈值设置 -->
        <div v-show="stepItems[stepCurrent].inx === 1" class="form-wrapper" @paste="handleJvxTabPaste">
          <div class="sub-title">设置KPI点位阈值</div>
          <div class="flex-row flex-vcenter" style="margin-bottom: 10px">
            <div class="flex-row flex-vcenter" style="margin-right: 30px" v-if="false">
              <InsertRowBelowOutlined style="font-size: 20px; margin-right: 5px; cursor: pointer" />
              <InsertRowRightOutlined style="font-size: 20px; cursor: pointer" />
            </div>
            <div class="flex-row" style="gap: 10px">
              <left-outlined @click="handlePrevMonth" />
              <a-date-picker
                v-model:value="pickerMonthValue"
                picker="month"
                format="YYYY-MM"
                style="width: 110px"
                @change="handlePickerMonthChange"
                :allowClear="false"
              />
              <right-outlined @click="handleNextMonth" />
            </div>
          </div>
          <j-vxe-table ref="jvxeTable" keep-source :loading="false" :columns="thresholdColumns" :dataSource="thresholdTbleData" :maxHeight="400">
            <template #valFlag="scope">
              <span v-if="scope.row.valFlag.includes('红')" class="red-txt">{{ scope.row.valFlag }}</span>
              <span v-else-if="scope.row.valFlag.includes('黄')" class="yellow-txt">{{ scope.row.valFlag }}</span>
              <span v-else class="normal-txt">{{ scope.row.valFlag }}</span>
            </template>
          </j-vxe-table>
          <div style="position: absolute; bottom: 10px; text-align: center; width: 100%">
            <a-button type="primary" @click="saveBatchKpiThreshold" :disabled="!internalKpiTag.id || pageLoading">保存</a-button>
            <a-button v-if="!!internalKpiTag.id" @click="stepCurrent -= 1" style="margin-left: 10px">上一步</a-button>
            <a-button v-if="!!internalKpiTag.id" @click="stepCurrent += 1" style="margin-left: 10px">下一步</a-button>
          </div>
        </div>
        <!-- ---------------------------------- 预警限值和预警接收人 --------------------------- -->
        <!-- 设置KPI点位 -->
        <div v-show="stepItems[stepCurrent].inx === 2" class="form-wrapper">
          <div class="sub-title">设置KPI点位预警限值</div>
          <a-form ref="editKpiAlarmFormRef" :model="modelKpiAlarmLimit" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
            <template v-if="internalKpiTag.tagKind === TagKindEnum.COLLECT">
              <a-form-item label="预警值类型：">
                <j-dict-select-tag type="radio" v-model:value="modelKpiAlarmLimit.collectAlarmLimit!.valType" :options="alarmCValTypeList" />
              </a-form-item>
              <a-form-item label="预警方式：">
                <j-dict-select-tag type="radio" v-model:value="modelKpiAlarmLimit.collectAlarmLimit!.limitType" :options="alarmLimitTypeList" />
              </a-form-item>
              <a-form-item label="限值：" :name="['collectAlarmLimit', 'limitValue']" :rules="[{ required: true, message: '限值不能为空' }]">
                <a-input-number v-model:value="modelKpiAlarmLimit.collectAlarmLimit!.limitValue" />
              </a-form-item>
              <a-form-item label="持续分钟：" :name="['collectAlarmLimit', 'duration']" :rules="[{ required: true, message: '持续分钟不能为空' }]">
                <a-input-number v-model:value="modelKpiAlarmLimit.collectAlarmLimit!.duration" />
              </a-form-item>
            </template>
            <template v-if="internalKpiTag.tagKind === TagKindEnum.ORSDATA">
              <a-form-item label="预警值类型：">
                <j-dict-select-tag type="radio" v-model:value="modelKpiAlarmLimit.orsAlarmLimit!.valType" :options="alarmOValTypeList" />
              </a-form-item>
              <a-form-item
                label="上限动态百分比："
                :name="['orsAlarmLimit', 'dynPerUppLimit']"
                :rules="[{ required: true, message: '上限动态百分比不能为空' }]"
                v-if="modelKpiAlarmLimit.orsAlarmLimit!.valType === 1"
              >
                <a-input-number v-model:value="modelKpiAlarmLimit.orsAlarmLimit!.dynPerUppLimit" :max="100" :min="0" />
              </a-form-item>
              <a-form-item
                label="下限动态百分比："
                :name="['orsAlarmLimit', 'dynPerLowLimit']"
                :rules="[{ required: true, message: '下限动态百分比不能为空' }]"
                v-if="modelKpiAlarmLimit.orsAlarmLimit!.valType === 1"
              >
                <a-input-number v-model:value="modelKpiAlarmLimit.orsAlarmLimit!.dynPerLowLimit" :max="100" :min="0" />
              </a-form-item>
              <a-form-item
                label="红上限："
                :name="['orsAlarmLimit', 'redUppLimit']"
                :rules="[{ required: true, message: '红上限不能为空' }]"
                v-if="modelKpiAlarmLimit.orsAlarmLimit!.valType === 2"
              >
                <a-input-number v-model:value="modelKpiAlarmLimit.orsAlarmLimit!.redUppLimit" />
              </a-form-item>
              <a-form-item
                label="黄下限："
                :name="['orsAlarmLimit', 'yellowLowLimit']"
                :rules="[{ required: true, message: '黄下限不能为空' }]"
                v-if="modelKpiAlarmLimit.orsAlarmLimit!.valType === 2"
              >
                <a-input-number v-model:value="modelKpiAlarmLimit.orsAlarmLimit!.yellowLowLimit" />
              </a-form-item>
            </template>
          </a-form>
          <div style="position: absolute; bottom: 10px; text-align: center; width: 100%">
            <a-button type="primary" @click="saveKpiAlarmLimit" :disabled="!internalKpiTag.id || pageLoading">保存</a-button>
            <a-button v-if="!!internalKpiTag.id" @click="stepCurrent -= 1" style="margin-left: 10px">上一步</a-button>
            <a-button v-if="!!internalKpiTag.id" @click="stepCurrent += 1" style="margin-left: 10px">下一步</a-button>
          </div>
        </div>

        <!-- KPI点位报警接收人设置 -->
        <div v-show="stepItems[stepCurrent].inx === 3" class="form-wrapper" @paste="handleJvxTabPaste">
          <div class="sub-title">设置KPI点位报警接收人</div>
          <div class="flex-row" style="margin-bottom: 10px; gap: 20px">
            <div style="width: 50%">
              <a-tabs v-model:activeKey="userActiveKey" class="user-tabs">
                <a-tab-pane key="1" tab="用户列表">
                  <a-input v-model:value="userSearchKey" placeholder="输入姓名关键字筛选" allow-clear />
                  <div class="user-container">
                    <a-checkbox-group v-model:value="selectedUserIds">
                      <div class="checkbox-grid">
                        <a-checkbox
                          v-for="item in allDictUserList"
                          :class="{ 'non-searched': !item.userName.includes(userSearchKey) }"
                          :key="item.userId"
                          :value="item.userId"
                          @change="(e) => handleCheckUser(e.target.checked, item.userId)"
                        >
                          {{ item.userName }}
                        </a-checkbox>
                      </div>
                    </a-checkbox-group>
                  </div>
                </a-tab-pane>
                <a-tab-pane key="2" tab="已存在自定义分组">
                  <div v-if="allGroupNameList.length === 0" style="text-align: center"> 暂无自定义分组 </div>
                  <div v-else class="flex-row" style="gap: 10px; border: 1px solid #ddd; padding: 5px">
                    <div class="group-container" style="width: 60%">
                      <a-radio-group v-model:value="selectedGroup" size="small" @change="handleGroupChange">
                        <a-radio :style="radioStyle" v-for="item in allGroupNameList" :key="item" :value="item">{{ item }}</a-radio>
                      </a-radio-group>
                    </div>
                    <div class="group-user-list" style="width: 40%">
                      <a-checkbox v-model:checked="allCheckGroupUser" @change="handleAllCheckGroupUser" v-if="selectedGroup"
                        ><strong>全选</strong></a-checkbox
                      >
                      <a-spin :spinning="groupUserLoading">
                        <a-checkbox-group v-model:value="selectedGroupUserIds">
                          <div class="checkbox-grid">
                            <a-checkbox
                              v-for="item in groupUserList"
                              :key="item.userId"
                              :value="item.userId"
                              @change="(e) => handleCheckGroupUser(e, item.userId)"
                            >
                              {{ item.userName }}
                            </a-checkbox>
                          </div>
                        </a-checkbox-group>
                      </a-spin>
                    </div>
                  </div>
                </a-tab-pane>
              </a-tabs>
            </div>
            <div style="width: 50%">
              <div>
                <div class="flex-row flex-vcenter">
                  <a-input style="margin: 7px 5px 0px 0px" v-model:value="modelKpiAlarmUser.groupName" placeholder="自定义分组名称" allow-clear />
                  <a-checkbox
                    v-if="false && selectedGroup && selectedGroupUserIds.length > 0"
                    @change="handleUpdateOldGroupUser"
                    style="margin-top: 5px"
                    v-model:checked="upOldGroupUser"
                    >更新旧分组用户</a-checkbox
                  >
                </div>
                <div style="font-size: 13px; color: #d92622">注：自定义分组后下次可以快速选择此分组用户</div>
                <div class="selected-container">
                  <div class="list-grid">
                    <div class="list-item" v-for="item in selectedDictUsers" :key="item?.userId">
                      <span class="item-text">{{ item?.userName }}</span>
                      <CloseOutlined class="delete-icon" @click="removeItem(item?.userId)" />
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div style="position: absolute; bottom: 10px; text-align: center; width: 100%">
            <a-button type="primary" @click="saveBatchKpiReceiver" :disabled="!internalKpiTag.id || pageLoading">保存</a-button>
            <a-button v-if="!!internalKpiTag.id" @click="stepCurrent -= 1" style="margin-left: 10px">上一步</a-button>
          </div>
        </div>
      </div>
    </a-spin>
  </a-modal>
</template>

<script lang="ts" setup>
  import { ref, reactive, computed, watch, nextTick, h } from 'vue';
  import type { FormInstance } from 'ant-design-vue';
  import { LeftOutlined, RightOutlined, InsertRowBelowOutlined, InsertRowRightOutlined, CloseOutlined } from '@ant-design/icons-vue';
  import dayjs from 'dayjs';
  import { TagKindEnum } from '@/enums/EmsEnum';
  import { saveOrUpdate as saveOrUpKpiTag, deleteOne as deleteOneKpiTag } from '../KpiTag.api';
  import { saveBatch as saveBatchThreshold, listByKpiTagId as listThresholdByKpiTagId } from '../KpiThreshold.api';
  import { getByKpiTagId as getKpiAlarmLimitByKpiTagId, saveOrUpdateByDto as saveOrUpdateKpiAlarmLimitByDto } from '../KpiAlarmLimit.api';
  import {
    getGroupNameList,
    getGroupUserList,
    getByKpiTagId as getKpiAlarmReciverByKpiTagId,
    saveOrUpdateByDto as saveOrUpdateKpiAlarmReciverByDto,
  } from '../KpiAlarmReciver.api';
  import { getSysDictUserList } from '../../masterdata/SystemData.api';
  import { DictItem, TagItem, KpiTag, DictUser, KpiAlarmLimit, KpiAlarmUser } from '../KpiStructSet.data';
  import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';
  import { JVxeColumn, JVxeTypes, JVxeTableInstance } from '/@/components/jeecg/JVxeTable/types';
  import JVxeTable from '@/components/jeecg/JVxeTable/src/JVxeTable';
  import { cloneDeep } from 'lodash-es';
  import { useMessage } from '/@/hooks/web/useMessage';
  const { createMessage, createConfirm } = useMessage();

  const props = defineProps({
    open: {
      type: Boolean,
      required: true,
    },
    energyType: {
      type: String,
      required: true,
    },
    modelKpiTag: {
      type: Object as () => KpiTag,
      required: true,
    },
    tagKindList: {
      type: Array as () => DictItem[],
      required: true,
    },
    collectTagList: {
      type: Array as () => TagItem[],
      required: true,
    },
    orstagList: {
      type: Array as () => TagItem[],
      required: true,
    },
  });

  const emit = defineEmits(['update:open', 'refresh', 'close']);

  // 使用计算属性作为代理
  const computedOpen = computed({
    get() {
      return props.open;
    },
    set(newValue) {
      // 发出事件让父组件处理状态更新
      emit('update:open', newValue);

      // 如果模态框关闭，触发关闭回调
      if (!newValue) {
        emit('close');
      }
    },
  });

  // 创建内部响应式变量
  const internalKpiTag = ref<KpiTag>({} as KpiTag);
  // 初始化和响应 props.modelKpiTag 变化
  watch(
    () => props.modelKpiTag,
    (newVal) => {
      internalKpiTag.value = { ...newVal };
    },
    { immediate: true, deep: true }
  );

  // 状态管理
  const visible = ref(false);
  const pageLoading = ref(false);
  const groupUserLoading = ref(false);

  const jvxeTable = ref<JVxeTableInstance>();
  const editTagFormRef = ref<FormInstance>();
  const editKpiAlarmFormRef = ref<FormInstance>();

  //Setp相关
  const stepItems = ref([
    {
      inx: 0,
      status: 'process',
      title: 'KPI点位',
      description: '未设置',
    },
    {
      inx: 1,
      status: 'wait',
      title: 'KPI点位阈值',
      description: '未设置',
      disabled: !internalKpiTag.value.id,
    },
    {
      inx: 2,
      status: 'wait',
      title: 'KPI点位预警限值',
      description: '未设置',
      disabled: !internalKpiTag.value.id,
    },
    {
      inx: 3,
      status: 'wait',
      title: 'KPI点位报警接收人',
      description: '未设置',
      disabled: !internalKpiTag.value.id,
    },
  ]);
  const stepCurrent = ref<number>(0);
  const stepStyle = {
    marginBottom: '5px',
    boxShadow: '0px -1px 0 0 #e8e8e8 inset',
  };

  // KPI点位阈值相关
  const thresholdTbleData = ref<Recordable[]>([]);
  const thresholdNullData = ref<Recordable[]>([
    { field: 'rlover', valFlag: '红上限', ytd: '', mtd: '', daily: '', hour: '' },
    { field: 'rlbelow', valFlag: '红下限', ytd: '', mtd: '', daily: '', hour: '' },
    { field: 'ylover', valFlag: '黄上限', ytd: '', mtd: '', daily: '', hour: '' },
    { field: 'ylbelow', valFlag: '黄下限', ytd: '', mtd: '', daily: '', hour: '' },
    { field: 'standard', valFlag: '标准值', ytd: '', mtd: '', daily: '', hour: '' },
    { field: 'yields', valFlag: '产量', ytd: '', mtd: '', daily: '', hour: '' },
  ]);

  const thresholdColumns = ref<JVxeColumn[]>([
    {
      title: 'field',
      key: 'field',
      type: JVxeTypes.hidden,
    },
    {
      title: '阈值类型',
      key: 'valFlag',
      width: 80,
      type: JVxeTypes.slot,
      slotName: 'valFlag',
    },
    {
      title: 'YTD',
      key: 'ytd',
      type: JVxeTypes.inputNumber,
      width: 130,
    },
    {
      title: 'MTD',
      key: 'mtd',
      type: JVxeTypes.inputNumber,
      width: 130,
    },
    {
      title: 'Daily',
      key: 'daily',
      type: JVxeTypes.inputNumber,
      width: 130,
    },
    {
      title: 'Hour',
      key: 'hour',
      type: JVxeTypes.inputNumber,
      width: 130,
    },
  ]);

  const thresholdColumnKeys = thresholdColumns.value.map((col) => col.key);
  const pickerMonthValue = ref<dayjs.Dayjs | null>(dayjs());
  const yearMonth = ref<string>(dayjs().format('YYYY-MM'));

  // 点位相关
  const tagListAll = ref<TagItem[]>([]);
  const tagList = ref<TagItem[]>([]);
  const tagDisplayCount = ref(50);

  // 下拉列表
  const collectValTypeList = ref<DictItem[]>([
    { value: 0, label: '瞬时值' },
    { value: 1, label: '累积值' },
  ]);

  const orsValTypeList = ref<DictItem[]>([
    { value: 0, label: '消耗' },
    { value: 1, label: '用量' },
  ]);

  const alarmCValTypeList = ref<DictItem[]>([
    { value: 1, label: '底数' },
    { value: 2, label: '用量' },
  ]);

  const alarmOValTypeList = ref<DictItem[]>([
    { value: 1, label: '动态限值' },
    { value: 2, label: '固定限值' },
  ]);

  const alarmLimitTypeList = ref<DictItem[]>([
    { value: 1, label: '高于' },
    { value: 2, label: '低于' },
  ]);

  // 预警相关
  const nullKpiAlarmLimit = reactive<KpiAlarmLimit>({
    id: undefined,
    kpiTagId: 0,
    tagKind: TagKindEnum.ORSDATA,
    tagId: '',
    orsAlarmLimit: {
      valType: 1,
      dynPerUppLimit: undefined,
      dynPerLowLimit: undefined,
      redUppLimit: undefined,
      yellowLowLimit: undefined,
    },
    collectAlarmLimit: {
      valType: 1,
      limitType: 1,
      limitValue: undefined,
      duration: undefined,
    },
  });
  const modelKpiAlarmLimit = reactive<KpiAlarmLimit>(cloneDeep(nullKpiAlarmLimit));

  // 接收人相关
  const allGroupNameList = ref<string[]>([]);
  const allDictUserList = ref<DictUser[]>([]);
  const groupUserList = ref<DictUser[]>([]);
  const selectedUserIds = ref<string[]>([]);
  const selectedGroupUserIds = ref<string[]>([]);
  const selectedGroup = ref<string>('');
  const selectedDictUsers = computed(
    () => selectedUserIds.value.map((id) => allDictUserList.value.find((item) => item.userId === id)).filter((item) => item) || []
  );

  const userActiveKey = ref('1');
  const userSearchKey = ref('');
  const upOldGroupUser = ref<boolean>(false);
  const allCheckGroupUser = ref<boolean>(false);
  const modelKpiAlarmUser = reactive<KpiAlarmUser>({
    groupName: '',
    kpiTagId: 0,
    userIds: [],
    userNames: [],
  });

  const radioStyle = reactive({
    display: 'flex',
    height: '30px',
    lineHeight: '30px',
  });

  // 监听props变化
  watch(
    () => props.open,
    (val) => {
      visible.value = val;
    }
  );

  watch(visible, (val) => {
    if (!val) {
      resetData();
    } else {
      initData();
    }
    emit('update:open', val);
  });

  // 初始化数据
  function initData() {
    thresholdTbleData.value = cloneDeep(thresholdNullData.value);
    modelKpiAlarmUser.groupName = '';
    selectedUserIds.value = [];
    //初始化点位下拉列表
    tagKindChange(null);
    // 初始化点位列表
    handleTagSearch('');

    // 如果有ID，获取相关数据
    if (internalKpiTag.value.id) {
      //编辑时全部都能点击
      stepItems.value[0].description = '已设置';
      stepItems.value.forEach((item) => {
        item.disabled = false;
      });

      if (internalKpiTag.value.tagKind === TagKindEnum.ORSDATA) {
        //根据kpiTagId查询阈值
        queryThresholdListByKpiTagId(internalKpiTag.value.id || 0, yearMonth.value);
        //根据kpiTagId查询点位预警限值
        queryKpiAlarmLimitByKpiTagId(internalKpiTag.value.id || 0);
        queryKpiAlarmReciverByKpiTagId(internalKpiTag.value.id || 0);
      } else {
        //根据kpiTagId查询点位预警限值
        queryKpiAlarmLimitByKpiTagId(internalKpiTag.value.id || 0);
        queryKpiAlarmReciverByKpiTagId(internalKpiTag.value.id || 0);
      }
    } else {
      stepCurrent.value = 0;
      //添加时仅第一项能点击
      stepItems.value.forEach((item) => {
        item.disabled = true;
        item.description = '未设置';
      });
      stepItems.value[0].disabled = false;
    }
    queryAllDictUser();
    queryAllGroupName();
  }

  // 重置数据
  function resetData() {
    thresholdTbleData.value = cloneDeep(thresholdNullData.value);
    editTagFormRef.value?.resetFields(); // 重置表单
    pickerMonthValue.value = dayjs();
    yearMonth.value = pickerMonthValue.value.format('YYYY-MM');
    stepCurrent.value = 0;

    // 清空预警限值
    Object.assign(modelKpiAlarmLimit, cloneDeep(nullKpiAlarmLimit));

    // 清空预警接收人
    selectedUserIds.value = [];
    selectedGroupUserIds.value = [];
    userActiveKey.value = '1';
    groupUserList.value = [];
    selectedGroup.value = '';
    allCheckGroupUser.value = false;

    Object.assign(modelKpiAlarmUser, {
      groupName: '',
      kpiTagId: 0,
      userIds: [],
      userNames: [],
    });

    // 重新查询分组列表
    queryAllGroupName();

    emit('close');
  }

  // 操作方法
  function tagKindChange(e: any) {
    if (internalKpiTag.value.tagKind === TagKindEnum.ORSDATA) {
      tagListAll.value = [...props.orstagList.filter((item) => item.tagType === internalKpiTag.value.energyType)];
      //调整 步骤条
      if (stepItems.value.length == 3) {
        stepItems.value.splice(1, 0, {
          inx: 1,
          status: 'wait',
          title: '设置KPI点位阈值',
          description: '未设置',
          disabled: !internalKpiTag.value.id,
        });
      }
    } else if (internalKpiTag.value.tagKind === TagKindEnum.COLLECT) {
      tagListAll.value = [...props.collectTagList.filter((item) => item.tagType === internalKpiTag.value.energyType)];
      //调整 步骤条
      if (stepItems.value.length == 4) {
        stepItems.value.splice(1, 1);
      }
    }
    tagList.value = [...tagListAll.value];
    if (e) {
      // 清空 tagId 时使用内部变量
      internalKpiTag.value = {
        ...internalKpiTag.value,
        tagId: '',
      };
    }
  }

  function handleTagSearch(value: string) {
    if (value) {
      tagList.value = tagListAll.value.filter((item) => `[${item.tagId}]${item.tagName}`.toLowerCase().includes(value.toLowerCase()));
    } else {
      tagList.value = [...tagListAll.value];
    }
  }

  function handleTagScroll(e: Event) {
    const target = e.target as HTMLElement;
    const { scrollHeight, scrollTop, clientHeight } = target;
    if (scrollHeight - scrollTop <= clientHeight + 10) {
      tagDisplayCount.value += 10;
    }
  }

  function handleTagChange(value: string) {
    const elem = tagList.value.find((item) => item.tagId === value);
    if (elem) {
      console.log('tag change', elem);
      internalKpiTag.value = {
        ...internalKpiTag.value,
        unit: elem.unit || '',
        valType: elem.valType || '',
        kpiTagName: elem.tagName || '',
        energyType: elem.tagType || '',
      };
    }
    console.log('内部变量:', internalKpiTag.value);
  }

  async function saveKpiTag() {
    try {
      await editTagFormRef.value?.validate();
      pageLoading.value = true;
      const response = await saveOrUpKpiTag(internalKpiTag.value, !!internalKpiTag.value.id);
      if (response.success) {
        createMessage.success('保存成功');
        if (!internalKpiTag.value.id) {
          stepItems.value[0].description = '已设置';
          stepCurrent.value += 1;
        }
        // 更新内部变量
        internalKpiTag.value = {
          ...internalKpiTag.value,
          id: response.result,
        };
        emit('refresh');
      } else {
        createMessage.error('保存失败');
      }
    } catch (e) {
      console.error('保存失败:', e);
    } finally {
      pageLoading.value = false;
    }
  }

  function deleteKpiTag(id: number) {
    createConfirm({
      iconType: 'warning',
      title: '确认删除',
      content: '您确定要删除此点位吗？',
      okText: '确认',
      cancelText: '取消',
      onOk: async () => {
        pageLoading.value = true;
        try {
          await deleteOneKpiTag({ id: id }, () => {
            createMessage.success('删除点位成功');
            emit('refresh');
            visible.value = false;
          });
        } catch (e) {
          console.error('删除点位失败:', e);
          createMessage.error('删除点位失败');
        } finally {
          pageLoading.value = false;
        }
      },
    });
  }

  // 阈值操作
  function handlePrevMonth() {
    if (pickerMonthValue.value) {
      pickerMonthValue.value = pickerMonthValue.value.subtract(1, 'month');
      yearMonth.value = pickerMonthValue.value.format('YYYY-MM');
      queryThresholdListByKpiTagId(internalKpiTag.value.id || 0, yearMonth.value);
    }
  }

  function handleNextMonth() {
    if (pickerMonthValue.value) {
      pickerMonthValue.value = pickerMonthValue.value.add(1, 'month');
      yearMonth.value = pickerMonthValue.value.format('YYYY-MM');
      queryThresholdListByKpiTagId(internalKpiTag.value.id || 0, yearMonth.value);
    }
  }

  function handlePickerMonthChange(date: dayjs.Dayjs | null) {
    yearMonth.value = date ? date.format('YYYY-MM') : '';
    queryThresholdListByKpiTagId(internalKpiTag.value.id || 0, yearMonth.value);
  }

  async function saveBatchKpiThreshold() {
    const tableData = jvxeTable.value?.getTableData();
    let thresholdData = transformThresholdData(tableData || thresholdTbleData.value);
    if (thresholdData.filter((item) => Object.keys(item).length > 1).length == 0) {
      createMessage.warning('还未填写阈值数据');
      return;
    }

    thresholdData = thresholdData.map((item) => {
      return {
        ...item,
        kpiTagId: internalKpiTag.value.id,
        tagId: internalKpiTag.value.tagId,
        tagKind: internalKpiTag.value.tagKind,
        yearmonth: yearMonth.value,
      };
    });

    try {
      pageLoading.value = true;
      const response = await saveBatchThreshold(thresholdData);
      if (response.success) {
        createMessage.success('保存成功');
        stepItems.value[1].status = 'process';
        stepItems.value[1].description = '已设置';
      } else {
        createMessage.error('保存失败');
      }
    } catch (e) {
      console.error('保存失败:', e);
      createMessage.error('保存失败');
    } finally {
      pageLoading.value = false;
    }
  }

  // 转换阈值数据
  function transformThresholdData(arr: any[]): Record<string, any>[] {
    const fields = ['YTD', 'MTD', 'Daily', 'Hour'];
    return fields.map((fieldKey) => {
      const obj: Record<string, any> = { vmold: fieldKey };
      arr.forEach((item) => {
        const val = item[fieldKey.toLowerCase()];
        if (val !== '' && val !== null && val !== undefined && String(val).trim() !== '') {
          obj[item.field] = typeof val === 'string' ? Number(val) || val : val;
        }
      });
      return obj;
    });
  }
  // 反转阈值数据
  function reverseThresholdData(thresholdArr: Record<string, any>[]): Record<string, any>[] {
    const fieldMeta = {
      rlover: { valFlag: '红上限' },
      rlbelow: { valFlag: '红下限' },
      ylover: { valFlag: '黄上限' },
      ylbelow: { valFlag: '黄下限' },
      standard: { valFlag: '标准值' },
      yields: { valFlag: '产量' },
    };

    const result: Record<string, any>[] = [];
    Object.keys(fieldMeta).forEach((field) => {
      const item: Record<string, any> = {
        field,
        valFlag: fieldMeta[field].valFlag,
        ytd: '',
        mtd: '',
        daily: '',
        hour: '',
      };

      thresholdArr.forEach((row) => {
        const key = row.vmold.toLowerCase();
        if (row[field] !== undefined) {
          item[key] = row[field];
        }
      });
      result.push(item);
    });

    return result;
  }

  async function queryThresholdListByKpiTagId(kpiTagId: number, yearMonth: string) {
    try {
      pageLoading.value = true;
      const params = { kpiTagId, yearmonth: yearMonth };
      const data = await listThresholdByKpiTagId(params);
      if (data.length === 0) {
        stepItems.value[1].status = 'wait';
        stepItems.value[1].description = '未设置';
      } else {
        stepItems.value[1].status = 'process';
        stepItems.value[1].description = '已设置';
      }
      thresholdTbleData.value = reverseThresholdData(data);
    } catch (e) {
      console.error('查询阈值失败:', e);
      createMessage.error('查询阈值失败');
    } finally {
      pageLoading.value = false;
    }
  }

  // 预警限值操作
  async function queryKpiAlarmLimitByKpiTagId(kpiTagId: number) {
    try {
      pageLoading.value = true;
      const params = { kpiTagId };
      const response = await getKpiAlarmLimitByKpiTagId(params);
      if (response.success) {
        Object.assign(modelKpiAlarmLimit, response.result);
        stepItems.value[2].status = 'process';
        stepItems.value[2].description = '已设置';
      } else {
        stepItems.value[2].status = 'wait';
        stepItems.value[2].description = '未设置';
      }
    } catch (e) {
      console.error('查询预警限值失败:', e);
    } finally {
      pageLoading.value = false;
    }
  }

  async function saveKpiAlarmLimit() {
    try {
      await editKpiAlarmFormRef.value?.validate();
      pageLoading.value = true;
      modelKpiAlarmLimit.kpiTagId = internalKpiTag.value.id || 0;
      modelKpiAlarmLimit.tagId = internalKpiTag.value.tagId;
      modelKpiAlarmLimit.tagKind = internalKpiTag.value.tagKind;

      const response = await saveOrUpdateKpiAlarmLimitByDto(modelKpiAlarmLimit);
      if (response.success) {
        createMessage.success('保存成功');
        modelKpiAlarmLimit.id = response.result;
        stepItems.value[2].status = 'process';
        stepItems.value[2].description = '已设置';
      } else {
        createMessage.error('保存失败');
      }
    } catch (e) {
      console.error('保存失败:', e);
    } finally {
      pageLoading.value = false;
    }
  }

  // 接收人操作
  async function queryAllDictUser() {
    const data = await getSysDictUserList({});
    allDictUserList.value = [...data];
  }

  async function queryAllGroupName() {
    const data = await getGroupNameList();
    allGroupNameList.value = [...data];
  }

  async function queryDictUserByGroupName(groupName: string) {
    groupUserLoading.value = true;
    const params = { groupName };
    const data = await getGroupUserList(params);
    groupUserList.value = [...data];
    groupUserLoading.value = false;
  }

  function removeItem(userId: any) {
    handleCheckUser(false, userId);
    const index = selectedUserIds.value.indexOf(userId);
    if (index !== -1) {
      selectedUserIds.value.splice(index, 1);
    }
  }

  async function saveBatchKpiReceiver() {
    try {
      pageLoading.value = true;
      modelKpiAlarmUser.kpiTagId = internalKpiTag.value.id || 0;
      modelKpiAlarmUser.userIds = selectedUserIds.value;
      modelKpiAlarmUser.userNames = selectedUserIds.value.map((id) => allDictUserList.value.find((item) => item.userId === id)?.userName || '');

      if (selectedUserIds.value.length === 0) {
        createMessage.warning('还未添加接收人');
        return;
      }

      const response = await saveOrUpdateKpiAlarmReciverByDto(modelKpiAlarmUser);
      if (response.success) {
        createMessage.success('保存成功');
        stepItems.value[3].status = 'process';
        stepItems.value[3].description = '已设置';
      } else {
        createMessage.error(response.message || '保存失败');
      }
    } catch (e) {
      console.error('保存失败:', e);
    } finally {
      pageLoading.value = false;
    }
  }

  function handleGroupChange() {
    queryDictUserByGroupName(selectedGroup.value);
  }

  function handleAllCheckGroupUser(e: any) {
    const groupUserIds = groupUserList.value.map((item) => item.userId);
    if (e.target.checked) {
      selectedGroupUserIds.value = groupUserIds;
      selectedUserIds.value = [...new Set([...selectedUserIds.value, ...groupUserIds])];
      modelKpiAlarmUser.groupName = selectedGroup.value;
    } else {
      selectedGroupUserIds.value = [];
      selectedUserIds.value = selectedUserIds.value.filter((id) => !groupUserIds.includes(id));
    }
  }

  function handleCheckUser(echecked: boolean, userId: any) {
    if (echecked) {
      selectedGroupUserIds.value.push(userId);
    } else {
      const index = selectedGroupUserIds.value.indexOf(userId);
      if (index !== -1) {
        selectedGroupUserIds.value.splice(index, 1);
      }
      if (selectedGroupUserIds.value.length === 0) {
        modelKpiAlarmUser.groupName = '';
        allCheckGroupUser.value = false;
      }
    }
  }

  function handleCheckGroupUser(e: any, userId: any) {
    if (e.target.checked) {
      selectedUserIds.value.push(userId);
    } else {
      const index = selectedUserIds.value.indexOf(userId);
      if (index !== -1) {
        selectedUserIds.value.splice(index, 1);
      }
    }
  }

  async function queryKpiAlarmReciverByKpiTagId(kpiTagId: number) {
    try {
      pageLoading.value = true;
      const params = { kpiTagId };
      const response = await getKpiAlarmReciverByKpiTagId(params);
      if (response.success) {
        Object.assign(modelKpiAlarmUser, {
          groupName: response.result.groupName,
          kpiTagId: response.result.kpiTagId,
          userIds: response.result.userIds,
          userNames: [],
        });
        selectedUserIds.value = response.result.userIds;
        selectedGroupUserIds.value = response.result.userIds;
        stepItems.value[3].status = 'process';
        stepItems.value[3].description = '已设置';
      } else {
        stepItems.value[3].status = 'wait';
        stepItems.value[3].description = '未设置';
      }
    } catch (e) {
      console.error('查询接收人失败:', e);
    } finally {
      pageLoading.value = false;
    }
  }

  function handleCancel() {
    visible.value = false;
  }

  //表格粘贴
  const handleJvxTabPaste = async (event: ClipboardEvent) => {
    const clipboardData = event.clipboardData;
    if (!clipboardData) return;

    const text = clipboardData.getData('text/plain');
    const rows = text
      .trim()
      .split('\n')
      .map((row) => row.split('\t'));

    for (let i = 0; i < rows.length; i++) {
      const rowData = rows[i];
      for (let j = 0; j < rowData.length && j < thresholdColumnKeys.length; j++) {
        const field = thresholdColumnKeys[j + 2];
        thresholdTbleData.value[i][field] = rowData[j];
      }
    }
    nextTick(() => {
      jvxeTable.value?.getXTable().reloadData(thresholdTbleData.value);
    });
  };

  //更新旧分组用户，带上当前选中的旧分组
  const handleUpdateOldGroupUser = (e: any) => {
    if (e.target.checked) {
      modelKpiAlarmUser.groupName = selectedGroup.value;
    } else {
      modelKpiAlarmUser.groupName = '';
    }
  };

  defineExpose({});
</script>

<style scoped>
  .sub-title {
    font-weight: bold;
    margin-bottom: 10px;
    font-size: 15px;
    text-align: center;
  }
  ::v-deep(.red-txt) {
    background-color: red;
    color: #fff;
    font-weight: bold;
    padding: 2px 5px;
    user-select: none;
  }
  ::v-deep(.yellow-txt) {
    background-color: yellow;
    color: #000;
    font-weight: bold;
    padding: 2px 5px;
    user-select: none;
  }
  ::v-deep(.normal-txt) {
    font-weight: bold;
    padding: 2px 5px;
    user-select: none;
  }

  .form-wrapper {
    width: 80%;
    position: relative;
    height: 100%;
    border: 0px solid #e8e8e8;
    padding: 10px;
    border-radius: 5px;
  }

  .user-container {
    margin-top: 2px;
    height: 280px;
    border: 1px solid #ddd;
    overflow-y: auto;
    padding: 6px;
    .checkbox-grid {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 5px;
      .non-searched {
        display: none;
      }
    }
  }
  .selected-container {
    height: 316px;
    overflow-y: auto;
    border: 1px solid #eee;
    margin-top: 2px;
    padding: 8px;
    ::v-deep(.selected-item) {
      padding-top: 4px !important;
      padding-bottom: 4px !important;
      line-height: 1.2;
      margin-block-end: 9px !important;
    }
    .list-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 2px;
      .list-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 3px 10px;
        background: #f9f9f9;
        border: 1px solid #ccc;
        border-radius: 4px;
      }

      .delete-icon {
        color: red;
        font-weight: bold;
        cursor: pointer;
        font-size: 16px;
        line-height: 1;
      }
    }
  }
  .user-tabs {
    ::v-deep(.ant-tabs-nav) {
      margin-bottom: 5px !important;
    }
  }
  .group-container {
    ::v-deep(.ant-radio-wrapper) {
      border-radius: 0px !important;
      border: 0px;
      margin-top: 3px;
      width: 170px;
      span {
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
      .ant-radio {
        display: none;
      }
      border-bottom: 1px solid #ddd;
    }
  }
  .group-user-list {
    margin-top: 2px;
    height: 300px;
    overflow-y: auto;
    padding: 6px;
    border-left: 1px solid #ddd;
    .checkbox-grid {
      display: grid;
      grid-template-columns: repeat(1, minmax(0, 1fr));
      gap: 5px;
    }
  }
</style>
