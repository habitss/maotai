<template>
  <a-modal :open="visible" width="700px" :body-style="{ padding: '30px' }" :title="title" ok-text="我已保存" @ok="handleOk" @cancel="handleOk">
    <div class="space-y-3">
      <p class="text-base">{{ message }}</p>
      <p v-if="showAppId">
        <strong>App ID：</strong>
        <span class="font-mono">{{ nowAppId }}</span>
      </p>

      <div class="flex items-center gap-2">
        <p class="m-0">
          <strong>{{ secretLabel }}：</strong>
          <span class="font-mono">{{ nowAppSecret }}</span>
        </p>
        <a-button size="small" @click="copySecret">复制</a-button>
      </div>

      <p class="text-red-500"> 请妥善保存 {{ secretLabel }}，它只会显示一次！ </p>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref, watch, computed } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  const { createMessage } = useMessage();

  interface Props {
    open: boolean;
    appId?: string;
    appSecret?: string;
    title?: string;
    message?: string;
    secretLabel?: string;
    showAppId?: boolean;
  }

  const props = withDefaults(defineProps<Props>(), {
    title: '新增成功',
    message: '应用创建成功！',
    secretLabel: 'App Secret',
    showAppId: true,
  });
  const emit = defineEmits(['update:open', 'ok']);

  const visible = ref(false);
  const nowAppId = ref('');
  const nowAppSecret = ref('');

  watch(
    () => props.open,
    (val) => {
      visible.value = val;
      if (val) {
        nowAppId.value = props.appId || '';
        nowAppSecret.value = props.appSecret || '';
      }
    },
    { immediate: true }
  );

  const title = computed(() => props.title);
  const message = computed(() => props.message);
  const secretLabel = computed(() => props.secretLabel);
  const showAppId = computed(() => props.showAppId);

  const handleOk = () => {
    visible.value = false;
    emit('update:open', false);
    emit('ok');
  };

  const copySecret = async () => {
    if (!nowAppSecret.value) return;

    try {
      await navigator.clipboard.writeText(nowAppSecret.value);
      createMessage.success('App Secret 已复制');
    } catch (error) {
      if (error instanceof DOMException) {
        console.warn('浏览器不支持剪贴板或无权限:', error);
      } else {
        console.error('未知复制异常:', error);
      }
      createMessage.error('复制失败，请手动复制');
    }
  };
</script>

<style scoped lang="less">
  .font-mono {
    font-family: Menlo, Monaco, Consolas, 'Courier New', monospace;
  }
</style>
