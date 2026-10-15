import { defHttp } from '/@/utils/http/axios';

enum Api {
  getAllTagOption = '/ems/tagUsage/getAllTagOption',
}

/**
 * 查询点位表达式所需的所有点位数据
 * @param tagType
 */
export const getAllTagOptionList = (tagType: string) => defHttp.get({ url: Api.getAllTagOption, data: { tagType: tagType } });
