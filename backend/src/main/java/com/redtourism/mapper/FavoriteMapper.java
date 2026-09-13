package com.redtourism.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.redtourism.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
}
