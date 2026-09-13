package com.redtourism.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.redtourism.entity.CultureCategory;
import com.redtourism.entity.CultureContent;
import java.util.List;

public interface CultureService extends IService<CultureContent> {
    IPage<CultureContent> listContents(int page, int size, Long categoryId, String keyword);
    CultureContent getDetail(Long id);
    List<CultureCategory> listCategories();
    List<CultureCategory> listCategoriesByParent(Long parentId);
    boolean saveCategory(CultureCategory category);
    boolean deleteCategory(Long id);
}
