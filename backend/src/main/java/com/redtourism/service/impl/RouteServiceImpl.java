package com.redtourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.redtourism.entity.Route;
import com.redtourism.entity.RouteSpot;
import com.redtourism.entity.ScenicSpot;
import com.redtourism.mapper.RouteMapper;
import com.redtourism.mapper.RouteSpotMapper;
import com.redtourism.mapper.ScenicSpotMapper;
import com.redtourism.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class RouteServiceImpl extends ServiceImpl<RouteMapper, Route> implements RouteService {

    @Autowired
    private RouteSpotMapper routeSpotMapper;

    @Autowired
    private ScenicSpotMapper scenicSpotMapper;

    @Override
    public IPage<Route> listRoutes(int page, int size, Integer days, String theme, String keyword) {
        LambdaQueryWrapper<Route> wrapper = new LambdaQueryWrapper<>();
        if (days != null) {
            wrapper.eq(Route::getDays, days);
        }
        if (StringUtils.hasText(theme)) {
            wrapper.eq(Route::getTheme, theme);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Route::getName, keyword)
                    .or().like(Route::getDescription, keyword));
        }
        wrapper.orderByDesc(Route::getViewCount);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public Route getDetail(Long id) {
        Route route = getById(id);
        if (route != null) {
            route.setViewCount(route.getViewCount() == null ? 1L : route.getViewCount() + 1);
            updateById(route);
        }
        return route;
    }

    @Override
    public List<RouteSpot> getRouteSpots(Long routeId) {
        LambdaQueryWrapper<RouteSpot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RouteSpot::getRouteId, routeId)
                .orderByAsc(RouteSpot::getDayNumber)
                .orderByAsc(RouteSpot::getSortOrder);
        List<RouteSpot> list = routeSpotMapper.selectList(wrapper);
        // 填充景点名称与坐标，供地图和多语言使用
        list.forEach(rs -> {
            if (rs.getSpotId() != null) {
                ScenicSpot spot = scenicSpotMapper.selectById(rs.getSpotId());
                if (spot != null) {
                    rs.setSpotName(spot.getName());
                    rs.setSpotNameEn(spot.getNameEn());
                    rs.setSpotNameJa(spot.getNameJa());
                    rs.setLatitude(spot.getLatitude());
                    rs.setLongitude(spot.getLongitude());
                }
            }
        });
        return list;
    }

    @Override
    @Transactional
    public boolean saveRouteSpots(Long routeId, List<RouteSpot> spots) {
        LambdaQueryWrapper<RouteSpot> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(RouteSpot::getRouteId, routeId);
        routeSpotMapper.delete(deleteWrapper);

        if (spots != null && !spots.isEmpty()) {
            for (RouteSpot spot : spots) {
                spot.setRouteId(routeId);
                routeSpotMapper.insert(spot);
            }
        }
        return true;
    }
}
