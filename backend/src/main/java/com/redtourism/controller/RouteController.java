package com.redtourism.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.redtourism.common.Result;
import com.redtourism.entity.Route;
import com.redtourism.entity.RouteSpot;
import com.redtourism.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/route")
public class RouteController {

    @Autowired
    private RouteService routeService;

    @GetMapping("/list")
    public Result<IPage<Route>> list(@RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "10") int size,
                                      @RequestParam(required = false) Integer days,
                                      @RequestParam(required = false) String theme,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(required = false, defaultValue = "zh") String lang) {
        IPage<Route> result = routeService.listRoutes(page, size, days, theme, keyword);
        result.getRecords().forEach(r -> applyLang(r, lang));
        return Result.success(result);
    }

    @GetMapping("/detail")
    public Result<Route> detail(@RequestParam Long id,
                                 @RequestParam(required = false, defaultValue = "zh") String lang) {
        Route r = routeService.getDetail(id);
        if (r != null) applyLang(r, lang);
        return Result.success(r);
    }

    @GetMapping("/spots")
    public Result<List<RouteSpot>> spots(@RequestParam Long routeId) {
        return Result.success(routeService.getRouteSpots(routeId));
    }

    @GetMapping("/themes")
    public Result<List<String>> themes() {
        List<String> themes = java.util.Arrays.asList(
                "红色研学", "经典打卡", "小众探秘", "亲子游", "红色+自然");
        return Result.success(themes);
    }

    private void applyLang(Route r, String lang) {
        if ("en".equals(lang)) {
            if (r.getNameEn() != null && !r.getNameEn().isEmpty()) r.setName(r.getNameEn());
            if (r.getDescriptionEn() != null && !r.getDescriptionEn().isEmpty()) r.setDescription(r.getDescriptionEn());
        } else if ("ja".equals(lang)) {
            if (r.getNameJa() != null && !r.getNameJa().isEmpty()) r.setName(r.getNameJa());
            if (r.getDescriptionJa() != null && !r.getDescriptionJa().isEmpty()) r.setDescription(r.getDescriptionJa());
        }
    }
}
