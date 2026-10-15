import axios from 'axios';
import { useGlobSetting } from '/@/hooks/setting';

const globSetting = useGlobSetting();

export const i18nHttp = axios.create({
  baseURL: globSetting.apiUrl,
  timeout: 30000,
});
