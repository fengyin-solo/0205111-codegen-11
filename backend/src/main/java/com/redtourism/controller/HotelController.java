package com.redtourism.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.redtourism.common.Result;
import com.redtourism.entity.Hotel;
import com.redtourism.service.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hotel")
public class HotelController {

    @Autowired
    private HotelService hotelService;

    @GetMapping("/list")
    public Result<IPage<Hotel>> list(@RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "10") int size,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) String orderBy) {
        return Result.success(hotelService.listHotels(page, size, keyword, orderBy));
    }

    @GetMapping("/detail")
    public Result<Hotel> detail(@RequestParam Long id) {
        return Result.success(hotelService.getDetail(id));
    }
}
