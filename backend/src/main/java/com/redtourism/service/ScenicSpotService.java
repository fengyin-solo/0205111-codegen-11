package com.redtourism.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.redtourism.entity.ScenicSpot;
import com.redtourism.entity.ScenicSpotImage;
import java.util.List;

public interface ScenicSpotService extends IService<ScenicSpot> {
    IPage<ScenicSpot> listSpots(int page, int size, String region, String theme,
                                 Integer status, String keyword, String orderBy);
    ScenicSpot getDetail(Long id);
    List<ScenicSpotImage> getImages(Long spotId);
    List<ScenicSpot> getCarousel();
    List<ScenicSpot> getRelated(Long id);
    List<ScenicSpot> getHot(int limit);
    boolean addImage(Long spotId, String imageUrl, Integer sortOrder);
    boolean deleteImage(Long imageId);
}
