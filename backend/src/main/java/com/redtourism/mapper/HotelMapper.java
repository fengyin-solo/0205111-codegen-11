package com.redtourism.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.redtourism.entity.Hotel;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HotelMapper extends BaseMapper<Hotel> {
}
