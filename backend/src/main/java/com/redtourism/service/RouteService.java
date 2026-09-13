package com.redtourism.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.redtourism.entity.Route;
import com.redtourism.entity.RouteSpot;
import java.util.List;

public interface RouteService extends IService<Route> {
    IPage<Route> listRoutes(int page, int size, Integer days, String theme, String keyword);
    Route getDetail(Long id);
    List<RouteSpot> getRouteSpots(Long routeId);
    boolean saveRouteSpots(Long routeId, List<RouteSpot> spots);
}
