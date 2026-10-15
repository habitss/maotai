package com.keytech.modules.maotai.workorder;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.keytech.modules.maotai.workorder.entity.TransferRoute;
import com.keytech.modules.maotai.workorder.mapper.TransferRouteMapper;
import com.keytech.modules.maotai.workorder.service.impl.TransferRouteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 转酒路径配置服务测试:按工艺的罐/撬互斥规则、同车间同工艺编码唯一、新增/编辑分流。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("转酒路径配置 TransferRouteServiceImpl")
class TransferRouteServiceImplTest {

    @Mock
    private TransferRouteMapper transferRouteMapper;

    @InjectMocks
    private TransferRouteServiceImpl service;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(service, "baseMapper", transferRouteMapper);
        when(transferRouteMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(transferRouteMapper.insert(any(TransferRoute.class))).thenReturn(1);
        when(transferRouteMapper.updateById(any(TransferRoute.class))).thenReturn(1);
    }

    private TransferRoute route(String process, String skid, String src, String tgt) {
        return new TransferRoute()
                .setWorkshopCode("ZZG").setProcessType(process)
                .setSkidCode(skid).setSourceTank(src).setTargetTank(tgt)
                .setRouteCode("Z100CX01").setRouteName("1#线").setSortNo(1);
    }

    @Test
    @DisplayName("卸车工艺:必须配撬与目标罐,源罐强制清空(槽车来酒无源罐)")
    void validate_unloadClearsSourceTank() {
        TransferRoute r = route("卸车工艺", "HP-01", "多余源罐", "ZTK103");
        TransferRoute saved = service.saveOrUpdateRoute(r, "管理员");
        assertNull(saved.getSourceTank(), "卸车工艺源罐应被清空");
        assertNotNull(saved.getSkidCode());
        assertEquals("ZTK103", saved.getTargetTank());
    }

    @Test
    @DisplayName("卸车工艺:缺撬或缺目标罐被拒")
    void validate_unloadRequiresSkidAndTarget() {
        IllegalArgumentException e1 = assertThrows(IllegalArgumentException.class,
                () -> service.saveOrUpdateRoute(route("卸车工艺", null, null, "ZTK103"), "管理员"));
        assertTrue(e1.getMessage().contains("卸车工艺需配置卸车撬和目标罐"));
        IllegalArgumentException e2 = assertThrows(IllegalArgumentException.class,
                () -> service.saveOrUpdateRoute(route("卸车工艺", "HP-01", null, null), "管理员"));
        assertTrue(e2.getMessage().contains("卸车工艺需配置卸车撬和目标罐"));
    }

    @Test
    @DisplayName("装车工艺:必须配撬与源罐,目标罐强制清空")
    void validate_loadClearsTargetTank() {
        TransferRoute r = route("装车工艺", "HP-01", "ZTK101", "多余目标罐");
        TransferRoute saved = service.saveOrUpdateRoute(r, "管理员");
        assertNull(saved.getTargetTank());
        assertEquals("ZTK101", saved.getSourceTank());
    }

    @Test
    @DisplayName("倒罐等其它工艺:必须双罐,撬强制清空")
    void validate_otherRequiresBothTanks() {
        TransferRoute r = route("倒罐工艺", "多余撬", "ZTK101", "ZTK105");
        TransferRoute saved = service.saveOrUpdateRoute(r, "管理员");
        assertNull(saved.getSkidCode());
        assertEquals("ZTK101", saved.getSourceTank());
        assertEquals("ZTK105", saved.getTargetTank());
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.saveOrUpdateRoute(route("倒罐工艺", null, null, "ZTK105"), "管理员"));
        assertTrue(e.getMessage().contains("该工艺需配置源罐和目标罐"));
    }

    @Test
    @DisplayName("基础校验:缺车间/工艺或编码/名称被拒")
    void validate_requiredFields() {
        IllegalArgumentException e1 = assertThrows(IllegalArgumentException.class,
                () -> service.saveOrUpdateRoute(route("倒罐工艺", null, "A", "B").setWorkshopCode(null), "管理员"));
        assertTrue(e1.getMessage().contains("请选择车间和工艺"));
        IllegalArgumentException e2 = assertThrows(IllegalArgumentException.class,
                () -> service.saveOrUpdateRoute(route("倒罐工艺", null, "A", "B").setRouteCode(null), "管理员"));
        assertTrue(e2.getMessage().contains("请填写路径编码和路径名称"));
    }

    @Test
    @DisplayName("唯一性:同车间同工艺同编码重复被拒")
    void validate_duplicateCodeRejected() {
        when(transferRouteMapper.selectCount(any(Wrapper.class))).thenReturn(1L);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.saveOrUpdateRoute(route("倒罐工艺", null, "A", "B"), "管理员"));
        assertTrue(e.getMessage().contains("路径编码已存在"));
    }

    @Test
    @DisplayName("新增:enabled 默认 1,审计字段落库")
    void create_setsDefaultsAndAudit() {
        TransferRoute r = route("倒罐工艺", null, "A", "B"); // enabled 未传
        TransferRoute saved = service.saveOrUpdateRoute(r, "管理员");
        assertEquals(1, saved.getEnabled());
        assertEquals(0, saved.getDelFlag());
        assertEquals("管理员", saved.getCreateBy());
        verify(transferRouteMapper).insert(any(TransferRoute.class));
    }

    @Test
    @DisplayName("编辑:存在且未删时更新,审计字段更新")
    void update_changesExisting() {
        TransferRoute db = route("倒罐工艺", null, "A", "B");
        db.setId("r1");
        db.setDelFlag(0);
        when(transferRouteMapper.selectById("r1")).thenReturn(db);
        TransferRoute patch = route("倒罐工艺", null, "A2", "B2");
        patch.setId("r1");
        patch.setEnabled(0).setRouteName("改名线");
        TransferRoute r = service.saveOrUpdateRoute(patch, "管理员");
        verify(transferRouteMapper).updateById(any(TransferRoute.class));
        verify(transferRouteMapper, never()).insert(any(TransferRoute.class));
        // 返回已保存对象,更新后的库内对象字段已被覆盖
        assertEquals("改名线", db.getRouteName());
        assertEquals(0, db.getEnabled());
        assertEquals("管理员", db.getUpdateBy());
    }

    @Test
    @DisplayName("编辑:配置不存在(或已删)被拒")
    void update_rejectsMissing() {
        when(transferRouteMapper.selectById("gone")).thenReturn(null);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> {
                    TransferRoute gone = route("倒罐工艺", null, "A", "B");
                    gone.setId("gone");
                    service.saveOrUpdateRoute(gone, "管理员");
                });
        assertEquals("路径配置不存在", e.getMessage());
    }

    @Test
    @DisplayName("编辑:唯一性校验排除自身(id 不等)")
    void update_duplicateExcludesSelf() {
        TransferRoute db = route("倒罐工艺", null, "A", "B");
        db.setId("r1");
        db.setDelFlag(0);
        when(transferRouteMapper.selectById("r1")).thenReturn(db);
        // count>0 模拟"库里另一条同编码";带 id 的更新仍应被拒
        when(transferRouteMapper.selectCount(any(Wrapper.class))).thenReturn(1L);
        assertThrows(IllegalArgumentException.class, () -> {
            TransferRoute patch = route("倒罐工艺", null, "A", "B");
            patch.setId("r1");
            service.saveOrUpdateRoute(patch, "管理员");
        });
    }
}
