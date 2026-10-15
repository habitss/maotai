package com.keytech.modules.cpk.util;

import cn.hutool.core.collection.CollUtil;
import com.keytech.modules.cpk.entity.CpkConfig;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CpkUtil {
    public static List<CpkConfig> getTreeList(List<CpkConfig> list, String pid, int level) {
        Map<String, List<CpkConfig>> groupedByParentId = list.stream()
                .collect(Collectors.groupingBy(
                        CpkConfig::getParentId
                ));
        return buildTree(groupedByParentId, pid, level);
    }

    private static List<CpkConfig> buildTree(Map<String, List<CpkConfig>> groupedByParentId, String pid, int level) {
        List<CpkConfig> result = groupedByParentId.get(pid);
        if(CollUtil.isNotEmpty(result)){
            for(CpkConfig cpkConfig : result){
                cpkConfig.setLevel(level);
                cpkConfig.setChildren(buildTree(groupedByParentId, cpkConfig.getId(), (level+1)));
            }
        }
        return result;
    }
}
