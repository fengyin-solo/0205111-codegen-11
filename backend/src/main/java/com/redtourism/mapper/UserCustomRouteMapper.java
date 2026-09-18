package com.redtourism.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.redtourism.entity.UserCustomRoute;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserCustomRouteMapper extends BaseMapper<UserCustomRoute> {

    /**
     * 按分享令牌查询（包含已软删除的行程，供分享详情页区分“已失效”和“已删除”提示）。
     */
    @Select("SELECT * FROM user_custom_route WHERE share_token = #{token} LIMIT 1")
    UserCustomRoute selectByShareToken(String token);
}
