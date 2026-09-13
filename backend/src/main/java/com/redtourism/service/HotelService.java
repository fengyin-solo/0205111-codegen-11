package com.redtourism.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.redtourism.entity.Hotel;

public interface HotelService extends IService<Hotel> {
    IPage<Hotel> listHotels(int page, int size, String keyword, String orderBy);
    Hotel getDetail(Long id);
}
