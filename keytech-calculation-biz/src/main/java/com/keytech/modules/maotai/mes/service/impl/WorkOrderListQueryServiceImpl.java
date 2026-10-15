package com.keytech.modules.maotai.mes.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keytech.base.service.impl.BaseServiceImpl;
import com.keytech.modules.maotai.mes.client.MesClient;
import com.keytech.modules.maotai.mes.config.MesProperties;
import com.keytech.modules.maotai.mes.dto.MesResponse;
import com.keytech.modules.maotai.mes.dto.WorkOrderListQueryRequest;
import com.keytech.modules.maotai.mes.dto.WorkOrderListResponse;
import com.keytech.modules.maotai.mes.dto.WorkOrderListResponse.WorkOrderVO;
import com.keytech.modules.maotai.mes.entity.MesWorkOrderInconsistency;
import com.keytech.modules.maotai.mes.entity.MesWorkOrderPush;
import com.keytech.modules.maotai.mes.entity.MesWorkOrderPushItem;
import com.keytech.modules.maotai.mes.mapper.MesWorkOrderInconsistencyMapper;
import com.keytech.modules.maotai.mes.mapper.MesWorkOrderPushItemMapper;
import com.keytech.modules.maotai.mes.mapper.MesWorkOrderPushMapper;
import com.keytech.modules.maotai.mes.service.WorkOrderListQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 工单查询实现(API02):按员工工号(empNo 动态传参)调勾储MES,响应工单解析落库 + 重复校验。
 * <p>同工单号(workOrderId)重复校验:不存在→INSERT;存在且核心字段一致→幂等跳过;
 * 存在但有字段不一致→落异常表 mes3_maotai_work_order_inconsistency(diff_fields 记 [{field,old,new}])
 * + 不更新不落该工单 + 返回异常汇总。鉴权 empNo+password(AES-ECB) 密文取 gzmes 配置,密钥/padding 待联调补。
 * <p>empNo 动态后不再纯轮询(@Scheduled 已去),改平台内部端点收 empNo 转发。
 *
 * @author maotai
 * @since 2026-08-04
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WorkOrderListQueryServiceImpl extends BaseServiceImpl<MesWorkOrderPushMapper, MesWorkOrderPush>
        implements WorkOrderListQueryService {

    /** 来源标识:API02 查询 */
    private static final String SOURCE_QUERY = "QUERY";

    private final MesClient mesClient;
    private final MesProperties properties;
    private final MesWorkOrderPushItemMapper itemMapper;
    private final MesWorkOrderInconsistencyMapper inconsistencyMapper;
    private final ObjectMapper objectMapper;

    @Override
    public MesResponse queryWorkOrderList(String rqi, String empNo) {
        String usedRqi = (rqi != null && !rqi.isEmpty()) ? rqi : generateRqi();
        if (empNo == null || empNo.isEmpty()) {
            return MesResponse.fail(usedRqi, "员工工号 empNo 不能为空");
        }

        // 1. 组装嵌套请求(empNo 动态;password/serialNo 取 gzmes 配置;timestamp 即时生成)
        WorkOrderListQueryRequest req = new WorkOrderListQueryRequest();
        req.setRqi(usedRqi);
        req.setSerialNo(properties.getSerialNo());
        WorkOrderListQueryRequest.Params params = new WorkOrderListQueryRequest.Params();
        params.setEmpNo(empNo);
        params.setPassword(properties.getPassword());
        params.setTimestamp(String.valueOf(System.currentTimeMillis() / 1000L));
        req.setParams(params);

        // 2. 调勾储MES
        WorkOrderListResponse resp = mesClient.workOrderListQuery(req);
        if (!resp.isSuccess()) {
            log.warn("[API02] 勾储MES 查询失败 rqi={} empNo={} desp={}", usedRqi, empNo, resp.getResultDesp());
            return MesResponse.fail(usedRqi, "勾储MES 查询失败: " + resp.getResultDesp());
        }

        // 3. 收集所有 services[].properties.workOrders[](MES 一般只返一个 service,遍历兜底)
        List<WorkOrderVO> workOrders = new ArrayList<>();
        if (resp.getServices() != null) {
            for (WorkOrderListResponse.Service svc : resp.getServices()) {
                if (svc != null && svc.getProperties() != null && svc.getProperties().getWorkOrders() != null) {
                    workOrders.addAll(svc.getProperties().getWorkOrders());
                }
            }
        }

        // 4. 逐条重复校验 + 落库
        int inserted = 0;
        int skipped = 0;
        List<String> inconsistentIds = new ArrayList<>();
        for (WorkOrderVO vo : workOrders) {
            String woId = vo.getId();
            if (woId == null || woId.isEmpty()) {
                log.warn("[API02] 工单缺 id,跳过 rqi={}", usedRqi);
                continue;
            }
            MesWorkOrderPushItem existing = findLatestByWorkOrderId(woId);
            if (existing == null) {
                // 新工单 → INSERT
                itemMapper.insert(toItem(vo, null));
                inserted++;
            } else {
                List<Map<String, Object>> diffs = diff(existing, vo);
                if (diffs.isEmpty()) {
                    skipped++; // 一致 → 幂等跳过
                } else {
                    // 不一致 → 落异常表 + 不更新该工单
                    inconsistencyMapper.insert(toInconsistency(woId, usedRqi, empNo, diffs));
                    inconsistentIds.add(woId + diffs.toString());
                    log.warn("[API02] 工单内容不一致 不落库 workOrderId={} rqi={} diffs={}", woId, usedRqi, diffs);
                }
            }
        }

        // 5. 主表落一条本次查询记录(rqi 幂等——同 rqi 重试不重复落)
        MesWorkOrderPush rec = new MesWorkOrderPush();
        rec.setRqi(usedRqi);
        rec.setEmpNo(empNo);
        rec.setSource(SOURCE_QUERY);
        rec.setProcessMsg(String.format("查询返回%d条: 新增%d, 幂等跳过%d, 不一致%d(未落库)",
                workOrders.size(), inserted, skipped, inconsistentIds.size()));
        try {
            save(rec);
        } catch (Exception e) {
            // 主表落库失败不阻断明细已落的结果(可能是 rqi 重试),记日志即可
            log.warn("[API02] 主表落库失败(可能 rqi 重试) rqi={} : {}", usedRqi, e.getMessage());
        }

        // 6. 返回汇总
        String desp = String.format("查询返回%d条: 新增%d, 幂等跳过%d, 不一致%d",
                workOrders.size(), inserted, skipped, inconsistentIds.size());
        if (!inconsistentIds.isEmpty()) {
            desp += "; 不一致工单未落库: " + String.join("; ", inconsistentIds);
            return MesResponse.fail(usedRqi, "存在工单内容不一致: " + desp);
        }
        return MesResponse.ok(usedRqi, desp);
    }

    /** 按 work_order_id 查最新一条(同工单号多次落库取最近一次) */
    private MesWorkOrderPushItem findLatestByWorkOrderId(String workOrderId) {
        List<MesWorkOrderPushItem> list = itemMapper.selectList(
                new LambdaQueryWrapper<MesWorkOrderPushItem>()
                        .eq(MesWorkOrderPushItem::getWorkOrderId, workOrderId)
                        .orderByDesc(MesWorkOrderPushItem::getCreateTime)
                        .last("LIMIT 1"));
        return list.isEmpty() ? null : list.get(0);
    }

    /** 显式比较核心业务字段(扩展 extend 不参与比较),返回 [{field,old,new}] 列表,空表示一致 */
    private List<Map<String, Object>> diff(MesWorkOrderPushItem old, WorkOrderVO vo) {
        List<Map<String, Object>> diffs = new ArrayList<>();
        // 字符串字段(null 归一化为空串比较)
        putIfDiff(diffs, "name", old.getName(), vo.getName());
        putIfDiff(diffs, "taskType", old.getTaskType(), vo.getTaskType());
        putIfDiff(diffs, "taskTypeName", old.getTaskTypeName(), vo.getTaskTypeName());
        putIfDiff(diffs, "location", old.getLocation(), vo.getLocation());
        putIfDiff(diffs, "inStoreTotal", old.getInStoreTotal(), toBd(vo.getInStoreTotal()));
        putIfDiff(diffs, "inStoreTanks", old.getInStoreTanks(), vo.getInStoreTanks());
        putIfDiff(diffs, "inStoreDetails", old.getInStoreDetails(), vo.getInStoreDetails());
        putIfDiff(diffs, "outStoreTotal", old.getOutStoreTotal(), toBd(vo.getOutStoreTotal()));
        putIfDiff(diffs, "outStoreTanks", old.getOutStoreTanks(), vo.getOutStoreTanks());
        putIfDiff(diffs, "outStoreDetails", old.getOutStoreDetails(), vo.getOutStoreDetails());
        putIfDiff(diffs, "startTime", old.getStartTime(), vo.getStartTime());
        putIfDiff(diffs, "endTime", old.getEndTime(), vo.getEndTime());
        putIfDiff(diffs, "extendBlendBatchNo", old.getExtendBlendBatchNo(), vo.getExtendBlendBatchNo());
        putIfDiff(diffs, "extendInspectionBatchNo", old.getExtendInspectionBatchNo(), vo.getExtendInspectionBatchNo());
        putIfDiff(diffs, "extendPackagePoint", old.getExtendPackagePoint(), vo.getExtendPackagePoint());
        return diffs;
    }

    private void putIfDiff(List<Map<String, Object>> diffs, String field, Object oldVal, Object newVal) {
        String o = oldVal == null ? "" : String.valueOf(oldVal);
        String n = newVal == null ? "" : String.valueOf(newVal);
        if (!o.equals(n)) {
            Map<String, Object> d = new LinkedHashMap<>();
            d.put("field", field);
            d.put("old", o);
            d.put("new", n);
            diffs.add(d);
        }
    }

    private java.math.BigDecimal toBd(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        try {
            return new java.math.BigDecimal(s.trim());
        } catch (NumberFormatException e) {
            return null; // 非数字按空处理(比较时与 null 一致)
        }
    }

    /** WorkOrderVO → 明细 entity(pushId 由调用方填,此处新建场景传 null) */
    private MesWorkOrderPushItem toItem(WorkOrderVO vo, String pushId) {
        MesWorkOrderPushItem item = new MesWorkOrderPushItem();
        item.setPushId(pushId);
        item.setWorkOrderId(vo.getId());
        item.setName(vo.getName());
        item.setTaskType(vo.getTaskType());
        item.setTaskTypeName(vo.getTaskTypeName());
        item.setType(vo.getType());
        item.setTypeName(vo.getTypeName());
        item.setLocation(vo.getLocation());
        item.setInStoreTotal(toBd(vo.getInStoreTotal()));
        item.setInStoreTanks(vo.getInStoreTanks());
        item.setInStoreDetails(vo.getInStoreDetails());
        item.setOutStoreTotal(toBd(vo.getOutStoreTotal()));
        item.setOutStoreTanks(vo.getOutStoreTanks());
        item.setOutStoreDetails(vo.getOutStoreDetails());
        item.setStartTime(vo.getStartTime());
        item.setEndTime(vo.getEndTime());
        item.setExtendBlendBatchNo(vo.getExtendBlendBatchNo());
        item.setExtendInspectionBatchNo(vo.getExtendInspectionBatchNo());
        item.setExtendPackagePoint(vo.getExtendPackagePoint());
        return item;
    }

    private MesWorkOrderInconsistency toInconsistency(String woId, String rqi, String empNo, List<Map<String, Object>> diffs) {
        MesWorkOrderInconsistency rec = new MesWorkOrderInconsistency();
        rec.setWorkOrderId(woId);
        rec.setRqi(rqi);
        rec.setEmpNo(empNo);
        rec.setDiffFields(toJsonSafe(diffs));
        return rec;
    }

    private String toJsonSafe(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("[API02] 序列化 diff 失败: {}", e.getMessage());
            return String.valueOf(obj);
        }
    }

    private String generateRqi() {
        return "RQI-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
