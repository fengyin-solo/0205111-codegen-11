package com.redtourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.redtourism.entity.ScenicSpot;
import com.redtourism.entity.ScenicSpotImage;
import com.redtourism.mapper.ScenicSpotImageMapper;
import com.redtourism.mapper.ScenicSpotMapper;
import com.redtourism.service.ScenicSpotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Service
public class ScenicSpotServiceImpl extends ServiceImpl<ScenicSpotMapper, ScenicSpot> implements ScenicSpotService {

    @Autowired
    private ScenicSpotImageMapper spotImageMapper;

    @Override
    public IPage<ScenicSpot> listSpots(int page, int size, String region, String theme,
                                        Integer status, String keyword, String orderBy) {
        LambdaQueryWrapper<ScenicSpot> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(region)) {
            wrapper.eq(ScenicSpot::getRegion, region);
        }
        if (StringUtils.hasText(theme)) {
            wrapper.eq(ScenicSpot::getTheme, theme);
        }
        if (status != null) {
            wrapper.eq(ScenicSpot::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(ScenicSpot::getName, keyword)
                    .or().like(ScenicSpot::getRegion, keyword)
                    .or().like(ScenicSpot::getTheme, keyword));
        }
        if ("viewCount".equals(orderBy) || "hot".equals(orderBy)) {
            wrapper.orderByDesc(ScenicSpot::getViewCount);
        } else if ("rating".equals(orderBy)) {
            wrapper.orderByDesc(ScenicSpot::getAvgRating);
        } else if ("favoriteCount".equals(orderBy)) {
            wrapper.orderByDesc(ScenicSpot::getFavoriteCount);
        } else if ("commentCount".equals(orderBy)) {
            wrapper.orderByDesc(ScenicSpot::getCommentCount);
        } else {
            wrapper.orderByDesc(ScenicSpot::getCreateTime);
        }
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public ScenicSpot getDetail(Long id) {
        ScenicSpot spot = getById(id);
        if (spot != null) {
            spot.setViewCount(spot.getViewCount() == null ? 1L : spot.getViewCount() + 1);
            updateById(spot);
        }
        return spot;
    }

    @Override
    public List<ScenicSpotImage> getImages(Long spotId) {
        LambdaQueryWrapper<ScenicSpotImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScenicSpotImage::getSpotId, spotId).orderByAsc(ScenicSpotImage::getSortOrder);
        return spotImageMapper.selectList(wrapper);
    }

    @Override
    public List<ScenicSpot> getCarousel() {
        return lambdaQuery()
                .eq(ScenicSpot::getStatus, 1)
                .orderByDesc(ScenicSpot::getViewCount)
                .last("LIMIT 6")
                .list();
    }

    @Override
    public List<ScenicSpot> getRelated(Long id) {
        ScenicSpot spot = getById(id);
        if (spot == null) return Collections.emptyList();
        return lambdaQuery()
                .ne(ScenicSpot::getId, id)
                .and(w -> w.eq(ScenicSpot::getTheme, spot.getTheme())
                        .or().eq(ScenicSpot::getRegion, spot.getRegion()))
                .eq(ScenicSpot::getStatus, 1)
                .last("LIMIT 6")
                .list();
    }

    @Override
    public List<ScenicSpot> getHot(int limit) {
        return lambdaQuery()
                .eq(ScenicSpot::getStatus, 1)
                .orderByDesc(ScenicSpot::getViewCount)
                .last("LIMIT " + limit)
                .list();
    }

    @Override
    public boolean addImage(Long spotId, String imageUrl, Integer sortOrder) {
        ScenicSpotImage image = new ScenicSpotImage();
        image.setSpotId(spotId);
        image.setImageUrl(imageUrl);
        image.setSortOrder(sortOrder == null ? 0 : sortOrder);
        return spotImageMapper.insert(image) > 0;
    }

    @Override
    public boolean deleteImage(Long imageId) {
        return spotImageMapper.deleteById(imageId) > 0;
    }
}
