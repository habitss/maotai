package com.keytech.modules.pi.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.keytech.modules.pi.entity.PiPost;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * &#064;Description: PI岗位绑定
 * &#064;Author: jeecg-boot
 * &#064;Date:   2025-07-14
 * &#064;Version: V1.0
 */
@Mapper
public interface PiPostMapper extends BaseMapper<PiPost> {

}
