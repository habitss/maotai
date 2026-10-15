import { cloneDeep } from "lodash-es";

/**
 * 获取表格每行需要合并的行数
 * @param records 集合数据
 * @param key 用于判断合并与否的字段
 * @returns 
 */
export const getRowSpan = (records, key) => {
    let newRecords = cloneDeep(records);
    //记录旧id
    let oldKey = '';
    //需要合并的行数
    let rowSpan = 1;
    newRecords.forEach((item, index) => {
        //如果取值不同
        if (oldKey != item[key]) {
            if (index != 0) {
                newRecords[(index - rowSpan)].rowSpan = rowSpan;
            }
            //如果是最后一行rowSpan设置为1
            if (index == newRecords.length - 1) {
                item.rowSpan = 1;
            } 
            oldKey = item[key]
            rowSpan = 1;
        } else {
            //如果id相同，则rowSpan加1,并判断是否为最后一行，如果是最后一行，则需要合并之前的行数
            rowSpan++;
            newRecords[index].rowSpan = 0;
            if (index == newRecords.length - 1) {
                newRecords[(index - (rowSpan - 1))].rowSpan = rowSpan;
            }
        }
    })
    return newRecords;
}