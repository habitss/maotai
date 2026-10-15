<template>
  <Description @register="register" class="mt-4" />
</template>
<script lang="ts">
import { defineComponent } from 'vue';
import { Description, DescItem, useDescription } from '/@/components/Description/index';
import { useI18n } from '/@/hooks/web/useI18n';
const { t } = useI18n();
const mockData = {
  username: 'test',
  nickName: 'VB',
  age: '123',
  phone: '15695909xxx',
  email: '190848757@qq.com',
  addr: '厦门市思明区',
  sex: '男',
  certy: '3504256199xxxxxxxxx',
  tag: 'orange',
};
const schema: DescItem[] = [
  {
    field: 'username',
    label: t('language.usernameText'),
  },
  {
    field: 'nickName',
    label: t('language.nickname'),
    render: (curVal, data) => {
      return `${data.username}-${curVal}`;
    },
  },
  {
    field: 'phone',
    label: t('language.contactMobile'),
  },
  {
    field: 'email',
    label: t('language.email'),
  },
  {
    field: 'addr',
    label: t('language.address'),
  },
];
export default defineComponent({
  components: { Description },
  setup() {
    const [register] = useDescription({
      title: 'useDescription',
      data: mockData,
      schema: schema,
    });
    return { mockData, schema, register };
  },
});
</script>
