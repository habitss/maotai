package com.keytech.modules.oauth2.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.keytech.modules.oauth2.entity.OAuth2Client;
import org.apache.ibatis.annotations.Mapper;

/**
 * 第三方OAuth2应用表Mapper
 * @author claude
 * @since 2026.08.19
 */
@Mapper
public interface OAuth2ClientMapper extends BaseMapper<OAuth2Client> {
}
