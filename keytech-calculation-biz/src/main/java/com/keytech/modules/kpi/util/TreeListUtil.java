package com.keytech.modules.kpi.util;

import com.keytech.modules.action.plan.entity.ActionPlanProblemType;
import com.keytech.modules.kpi.dto.DepartDto;
import com.keytech.modules.kpi.entity.KpiInfo;
import com.keytech.modules.kpi.entity.KpiType;
import com.keytech.modules.manage.check.entity.ManageCheckCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 树形化数据
 */
public class TreeListUtil {
    /**
     *
     * @param nodes 要树形化的数据
     * @param getId 获取节点id的方法
     * @param getPid 获取节点父id的方法
     * @param getChildren 获取子集合的方法
     * @param <Node> 节点数据
     * @param <Id> id的数据类型
     * @param <Pid> Pid的数据类型
     * @return 树形化后的数据
     */
    public static <Node, Id, Pid> List<Node> getTreeList(List<Node> nodes, Function<Node, Id> getId, Function<Node, Pid> getPid, Function<Node, List<Node>> getChildren) {
        Map<Pid, List<Node>> nodeMap = nodes.stream().filter(node -> getPid.apply(node) != null).collect(Collectors.groupingBy(getPid, Collectors.toList()));

        for (Node node : nodes) {
            List<Node> nodeList = nodeMap.get(getId.apply(node));
            if (nodeList != null) getChildren.apply(node).addAll(nodeList);
        }
        // 移除 children 为空的节点的 children 属性
        removeEmptyChildren(nodes, getChildren);
        return nodes;
    }

    private static <Node> void removeEmptyChildren(List<Node> nodes, Function<Node, List<Node>> getChildren) {
        for (Node node : nodes) {
            List<Node> children = getChildren.apply(node);
            if (null != children && children.isEmpty()) {
                // 这里假设 Node 类有一个方法可以移除 children 属性
                // 由于 Node 是泛型，我们无法直接操作，所以这里只是一个示例逻辑
                // 实际使用中需要根据具体的 Node 类实现来处理
                if (node instanceof KpiType) {
                    ((KpiType) node).setChildren(null);
                }
                if (node instanceof DepartDto) {
                    ((DepartDto) node).setChildren(null);
                }
                if (node instanceof KpiInfo) {
                    ((KpiInfo) node).setChildren(null);
                }
                if (node instanceof ActionPlanProblemType) {
                    ((ActionPlanProblemType) node).setChildList(null);
                }
                if (node instanceof ManageCheckCategory) {
                    ((ManageCheckCategory) node).setChildList(null);
                }
            } else {
                // 递归处理子节点
                if(null != children){
                    removeEmptyChildren(children, getChildren);
                }
            }
        }
    }

}