package com.redtourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.redtourism.entity.CultureCategory;
import com.redtourism.entity.CultureContent;
import com.redtourism.mapper.CultureCategoryMapper;
import com.redtourism.mapper.CultureContentMapper;
import com.redtourism.service.CultureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CultureServiceImpl extends ServiceImpl<CultureContentMapper, CultureContent> implements CultureService {

    @Autowired
    private CultureCategoryMapper categoryMapper;

    @Override
    public IPage<CultureContent> listContents(int page, int size, Long categoryId, String keyword) {
        LambdaQueryWrapper<CultureContent> wrapper = new LambdaQueryWrapper<>();
        if (categoryId != null) {
            wrapper.eq(CultureContent::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(CultureContent::getTitle, keyword)
                    .or().like(CultureContent::getContent, keyword));
        }
        wrapper.orderByDesc(CultureContent::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public CultureContent getDetail(Long id) {
        CultureContent content = getById(id);
        if (content != null) {
            content.setViewCount(content.getViewCount() == null ? 1L : content.getViewCount() + 1);
            updateById(content);
        }
        return content;
    }

    @Override
    public List<CultureCategory> listCategories() {
        LambdaQueryWrapper<CultureCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(CultureCategory::getSortOrder);
        return categoryMapper.selectList(wrapper);
    }

    @Override
    public List<CultureCategory> listCategoriesByParent(Long parentId) {
        LambdaQueryWrapper<CultureCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CultureCategory::getParentId, parentId)
                .orderByAsc(CultureCategory::getSortOrder);
        return categoryMapper.selectList(wrapper);
    }

    @Override
    public boolean saveCategory(CultureCategory category) {
        if (category.getId() != null) {
            return categoryMapper.updateById(category) > 0;
        }
        return categoryMapper.insert(category) > 0;
    }

    @Override
    public boolean deleteCategory(Long id) {
        return categoryMapper.deleteById(id) > 0;
    }
}
