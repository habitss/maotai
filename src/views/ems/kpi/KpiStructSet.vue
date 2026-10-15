<template>
  <a-spin :spinning="pageLoading">
    <div class="kpiStruct-pg">
      <a-form layout="inline">
        <a-form-item>
          <span style="font-weight: bold">能源类型：</span>
          <j-dict-select-tag
            :showChooseOption="false"
            style="width: 150px"
            v-model:value="queryParam.energyType"
            dictCode="mes3_mdm_dict where group_code = 'EMS_TagType',item_key,item_val"
            @change="handleEnergyTypeChange"
          />
        </a-form-item>
        <a-form-item>
          <a-button v-if="treeData.length === 0" type="primary" @click="addKpiStruct">添加根节点</a-button>
        </a-form-item>
      </a-form>

      <div class="kpi-tree">
        <a-tree
          ref="treeRef"
          :tree-data="treeData"
          :expanded-keys="expandedKeys"
          :field-names="{ key: 'id', title: 'title' }"
          @expand="setExpandedKeys"
        >
          <template #title="{ key: treeKey, data: nodeData }">
            <div class="custom-tree-node">
              <span class="tree-node-list">
                <a-dropdown :trigger="['contextmenu']">
                  <span style="color: #555; font-size: 14px">{{ nodeData.name }}</span>
                  <template #overlay>
                    <a-menu @click="({ key: menuKey }) => onContextMenuClick(treeKey, menuKey)">
                      <a-menu-item key="1" @click="() => append(nodeData)">添加子节点</a-menu-item>
                      <a-menu-item key="2" @click="() => showKpiStruct(nodeData.id)">编辑</a-menu-item>
                      <a-menu-item key="3" @click="() => remove(nodeData)"><span style="color: red">删除</span></a-menu-item>
                    </a-menu>
                  </template>
                </a-dropdown>
              </span>
              <span style="margin-top: -2px; margin-left: 10px" class="flex-row flex-vcenter">
                <span
                  v-for="item in nodeData.kpiTagList"
                  :key="item.tagId"
                  :class="`link-usage${item.primaryTag} em-lbl`"
                  @click="() => bindKpiTag(item)"
                >
                  [
                  {{
                    item.kpiTagName.length > 25
                      ? `${item.tagId}-${item.kpiTagName.slice(0, 16)}...${item.kpiTagName.slice(-4)}`
                      : `${item.tagId}-${item.kpiTagName}`
                  }}
                  ]
                </span>
                <span v-if="nodeData.kpiTagList.length === 0" class="em-lbl"> [ 未设置点位 ] </span>
                <a-tooltip placement="top" v-if="nodeData.kpiTagList.length === 0">
                  <template #title> 添加主点位 </template>
                  <Icon class="icon-btn-p" size="22" icon="ant-design:plus-circle-outlined" @click="() => addKpiTag(nodeData, 1)" />
                </a-tooltip>
                <a-tooltip placement="top" v-if="nodeData.kpiTagList.length > 0">
                  <template #title> 添加其他点位 </template>
                  <Icon class="icon-btn-o" size="22" icon="ant-design:plus-circle-outlined" @click="() => addKpiTag(nodeData, 0)" />
                </a-tooltip>
              </span>
            </div>
          </template>
        </a-tree>
      </div>

      <!-- KPI结构弹窗 -->
      <a-modal v-model:open="digKpiStructShow" :title="digTitle" :footer="null" :body-style="{ padding: '20px' }" @cancel="closeDialog">
        <a-form ref="editFormRef" :model="modelKpiStruct" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
          <a-form-item
            label="名称"
            name="name"
            :rules="[
              { required: true, message: '名称不能为空！' },
              { max: 20, message: '不得超过20字符' },
            ]"
          >
            <a-input ref="nameInputRef" v-model:value="modelKpiStruct.name" placeholder="请输入名称" allowClear @press-enter="saveKpiStruct" />
          </a-form-item>
        </a-form>
        <div class="flex-row flex-hcenter" style="margin-top: 30px">
          <a-button type="primary" @click="saveKpiStruct">保存</a-button>
          <a-button @click="onClkCancel" style="margin-left: 10px">取消</a-button>
        </div>
        <div>
          <a-checkbox v-model:checked="continueAddStruct" style="margin-right: 10px">开启继续添加</a-checkbox>
        </div>
        <div style="color: red; margin-top: 10px">注: 按回车可直接保存，开启继续添加不会自动关闭此窗口</div>
      </a-modal>

      <!-- KPI设置弹窗 -->
      <KpiTagSetupModal
        ref="kpiTagSetupModal"
        v-model:open="digKpiTagShow"
        :energy-type="queryParam.energyType"
        :model-kpi-tag="modelKpiTag"
        :tag-kind-list="tagKindList"
        :collect-tag-list="collectTagList"
        :orstag-list="orstagList"
        @refresh="listStructData"
        @close="closeTagDialog"
      />
    </div>
  </a-spin>
</template>

<script lang="ts" setup name="ems-KpiStructSet">
  import { ref, reactive, onMounted, nextTick, watch } from 'vue';
  import type { FormInstance } from 'ant-design-vue';
  import { Tree } from 'ant-design-vue';
  import KpiTagSetupModal from './components/KpiTagSetupModal.vue';
  import { listAllOrgTag } from '../tagdata/OrsdataTag.api';
  import { TagKindEnum } from '@/enums/EmsEnum';
  import {
    listAllCollectTag,
    getKpiStrutTree,
    getOne as getKpiStrutById,
    saveOrUpdate as saveOrUpdateKpiStruct,
    batchDelete as deleteKpiStructBatch,
  } from './KpiStruct.api';
  import { TreeDataNode, KpiStruct, KpiTag, TagItem, tagKindList } from './KpiStructSet.data';
  import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  const { createMessage, createConfirm } = useMessage();

  const kpiTagSetupModal = ref();

  // 状态管理
  const pageLoading = ref(false);
  const treeRef = ref<InstanceType<typeof Tree>>();
  const digKpiStructShow = ref(false);
  const digKpiTagShow = ref(false);
  const digTitle = ref('新增KPI结构');
  const isUpdate = ref(false);
  const expandedKeys = ref<number[]>([]);
  const editFormRef = ref<FormInstance>();
  const nameInputRef = ref();
  const continueAddStruct = ref(false);

  const queryParam = reactive({
    energyType: 'Electric',
  });

  const modelKpiStruct = reactive<KpiStruct>({
    id: undefined,
    name: '',
    energyType: '',
    parentId: 0,
    level: 0,
  });

  const modelKpiTag = reactive<KpiTag>({
    id: undefined,
    kpiTagName: '',
    structId: 0,
    tagId: '',
    unit: '',
    primaryTag: 1,
    tagKind: TagKindEnum.ORSDATA,
  });

  // 下拉数据
  const treeData = ref<TreeDataNode[]>([]);

  // 点位相关
  const orstagList = ref<TagItem[]>([]);
  const collectTagList = ref<TagItem[]>([]);
  const tagListAll = ref<TagItem[]>([]);
  const tagList = ref<TagItem[]>([]);

  // 初始化
  onMounted(() => {
    getOrstagList();
    getCollectTagList();
    listStructData();
  });

  // 工具函数
  const list2Tree = (list: TreeDataNode[], parentId: number): TreeDataNode[] => {
    return list
      .filter((item) => item.parentId === parentId)
      .map((item) => ({
        ...item,
        children: list2Tree(list, item.id),
      }));
  };

  const tree2List = (tree: TreeDataNode[]): TreeDataNode[] => {
    return tree.reduce((acc: TreeDataNode[], node) => {
      acc.push(node);
      if (node.children && node.children.length > 0) {
        acc.push(...tree2List(node.children));
      }
      return acc;
    }, []);
  };

  // 点位弹框显示时让焦点聚焦到名称输入框
  watch(digKpiStructShow, (visible) => {
    if (visible) {
      nextTick(() => {
        nameInputRef.value?.focus?.();
      });
    }
  });

  const handleEnergyTypeChange = (value: string) => {
    queryParam.energyType = value;
    modelKpiTag.energyType = value;
    tagListAll.value = [...orstagList.value.filter((item) => item.tagType === queryParam.energyType)];
    tagList.value = [...orstagList.value.filter((item) => item.tagType === queryParam.energyType)];
    listStructData();
  };

  const getOrstagList = async () => {
    const paramObj = {};
    try {
      const response = await listAllOrgTag(paramObj);
      orstagList.value = response;
      tagListAll.value = [...orstagList.value.filter((item) => item.tagType === queryParam.energyType)];
      tagList.value = [...orstagList.value.filter((item) => item.tagType === queryParam.energyType)];
    } catch (e) {
      console.error('获取中间点列表失败:', e);
      createMessage.error('获取中间点列表失败', 5);
    }
  };

  /**
   * 获取采集点位列表
   */
  const getCollectTagList = async () => {
    pageLoading.value = true;
    collectTagList.value = [];
    try {
      const paramObj = {};
      const response = await listAllCollectTag(paramObj);
      response.forEach((item) => {
        const tagItem: TagItem = {
          tagId: item.tagId,
          tagName: item.tagName,
          unit: item.unit,
          tagType: item.tagType,
          valType: item.valType,
        };
        collectTagList.value.push(tagItem);
      });
    } catch (e) {
      console.error('获取采集点位失败:', e);
      createMessage.error('获取采集点位失败');
    } finally {
      pageLoading.value = false;
    }
  };

  const listStructData = async () => {
    pageLoading.value = true;

    try {
      const data = await getKpiStrutTree(queryParam);
      if (data.length === 0) {
        treeData.value = [];
        createMessage.warning('此能源类型下还未配置KPI结构');
        return;
      }

      // 处理点位排序
      data.forEach((elem) => {
        if (elem.kpiTagList && elem.kpiTagList.length > 1) {
          elem.kpiTagList.sort((a, b) => b.primaryTag - a.primaryTag);
        }
      });

      // 设置展开节点
      if (data.length > 0) {
        expandedKeys.value = data.filter((item) => item.level <= 4).map((item) => item.id);
      }

      treeData.value = list2Tree(data, 0);
    } catch (e) {
      console.error('获取树结构失败:', e);
      createMessage.error('获取树结构失败');
    } finally {
      pageLoading.value = false;
    }
  };

  // 树操作方法
  const addKpiStruct = () => {
    if (!queryParam.energyType) {
      createMessage.error('请选择能源类型');
      return;
    }

    digTitle.value = '添加根节点';
    modelKpiStruct.parentId = 0;
    modelKpiStruct.energyType = queryParam.energyType;
    modelKpiStruct.level = 1;
    isUpdate.value = false;
    digKpiStructShow.value = true;
  };

  const showKpiStruct = async (id: number) => {
    pageLoading.value = true;
    isUpdate.value = true;

    try {
      const data = await getKpiStrutById(id);
      Object.assign(modelKpiStruct, {
        id: data.id,
        name: data.name,
        level: data.level,
        parentId: data.parentId,
        energyType: data.energyType,
      });
      digTitle.value = '编辑节点';
      digKpiStructShow.value = true;
    } catch (e) {
      console.error('获取树结构失败:', e);
      createMessage.error('获取树结构失败');
    } finally {
      pageLoading.value = false;
    }
  };

  const saveKpiStruct = async () => {
    try {
      await editFormRef.value?.validate();

      pageLoading.value = true;
      const response = await saveOrUpdateKpiStruct(modelKpiStruct, isUpdate.value);

      createMessage.success('保存成功');
      digKpiStructShow.value = continueAddStruct.value;
      modelKpiStruct.name = '';
      expandedKeys.value = [response.result];
      listStructData();
    } catch (error) {
      console.error('保存失败:', error);
    } finally {
      pageLoading.value = false;
    }
  };

  const remove = (data: TreeDataNode) => {
    createConfirm({
      iconType: 'warning',
      title: '确认删除',
      content: '确认删除此节点及其子节点吗?',
      okText: '确认',
      cancelText: '取消',
      onOk: async () => {
        pageLoading.value = true;

        try {
          const delArr = tree2List([data]);
          const delIds = delArr.map((item) => item.id);

          await deleteKpiStructBatch({ ids: delIds }, () => {
            listStructData();
          });
        } catch (e) {
          console.error('删除失败:', e);
          createMessage.error('删除失败');
        } finally {
          pageLoading.value = false;
        }
      },
    });
  };

  const append = (data: TreeDataNode) => {
    console.log(data);
    digTitle.value = `【${data.name}】添加子节点`;
    isUpdate.value = false;
    digKpiStructShow.value = true;
    Object.assign(modelKpiStruct, {
      id: undefined,
      level: data.level + 1,
      energyType: queryParam.energyType,
      parentId: data.id,
      name: '',
    });
  };

  const bindKpiTag = (item: KpiTag) => {
    Object.assign(modelKpiTag, item);
    modelKpiTag.energyType = queryParam.energyType;
    if (modelKpiTag.tagKind === TagKindEnum.ORSDATA) {
      const orsTag = orstagList.value.find((item) => item.tagId === modelKpiTag.tagId);
      modelKpiTag.unit = orsTag?.unit;
      modelKpiTag.valType = orsTag?.valType;
    } else {
      const collectTag = collectTagList.value.find((item) => item.tagId === modelKpiTag.tagId);
      modelKpiTag.unit = collectTag?.unit;
      modelKpiTag.valType = collectTag?.valType;
    }
    digKpiTagShow.value = true;
  };

  const addKpiTag = (data: TreeDataNode, pType: number) => {
    // 重置并设置新值
    Object.assign(modelKpiTag, {
      id: undefined,
      kpiTagName: '',
      structId: data.id,
      tagId: '',
      unit: '',
      primaryTag: pType,
      tagKind: TagKindEnum.ORSDATA,
      energyType: queryParam.energyType,
    });
    digKpiTagShow.value = true;
  };

  const setExpandedKeys = (keys: number[]) => {
    expandedKeys.value = keys;
  };

  const onClkCancel = () => {
    digKpiStructShow.value = false;
  };

  //重置树结构表单
  const resetStructForm = () => {
    Object.assign(modelKpiStruct, {
      id: undefined,
      name: '',
      parentId: 0,
      level: 0,
    });
  };

  const closeDialog = () => {
    resetStructForm();
    continueAddStruct.value = false;
  };

  const closeTagDialog = () => {
    //清空点位
    Object.assign(modelKpiTag, {
      id: undefined,
      kpiTagName: '',
      structId: 0,
      tagId: '',
      unit: '',
      energyType: queryParam.energyType,
      primaryTag: 1,
      tagKind: TagKindEnum.ORSDATA,
    });
  };

  const onContextMenuClick = (treeKey: string, menuKey: string | number) => {
    console.log(`treeKey: ${treeKey}, menuKey: ${menuKey}`);
  };
</script>

<style lang="less" scoped>
  @import url('/@/styles/disabled.less');
  .kpiStruct-pg {
    padding: 20px;
    background-color: #fff;
    margin: 10px;
    height: calc(100vh - 130px);
    .kpi-tree {
      margin-top: 10px !important;
    }

    .icon-btn-p {
      margin: 2px 0px 0px 10px;
      color: @primary-color;
      filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.1));
    }
    .icon-btn-o {
      margin: 2px 0px 0px 10px;
      color: #eca80d;
      filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.1));
    }

    .custom-tree-node {
      position: relative;
      display: flex;
      width: 100%;
      justify-content: space-between;
      align-items: center;
      font-size: 14px;

      .tree-node-list {
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .em-lbl {
        font-style: normal;
        font-size: 14px;
        color: #999;
        margin-left: 10px;
        text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
      }

      .link-usage0 {
        color: #eca80d;
        cursor: pointer;

        &:hover {
          text-decoration: underline;
        }
      }

      .link-usage1 {
        color: @primary-color;
        cursor: pointer;

        &:hover {
          text-decoration: underline;
        }
      }

      .link-green {
        color: #f39418;
      }

      .link-red {
        color: #ff0000;
      }
    }

    .ant-tree-node-content-wrapper {
      width: 100%;
    }
  }

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
    height: 303px;
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
    ::v-deep(.ant-radio-wrapper-checked) {
      border-bottom: 1px solid #d92622;
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
  ::v-deep(.ant-tree-node-content-wrapper:hover) {
    background-color: #fff !important;
  }
  ::v-deep(.ant-tree-node-selected) {
    background-color: #fff !important;
  }
</style>
