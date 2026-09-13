package com.redtourism.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.redtourism.common.Result;
import com.redtourism.entity.CultureCategory;
import com.redtourism.entity.CultureContent;
import com.redtourism.service.CultureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/culture")
public class CultureController {

    @Autowired
    private CultureService cultureService;

    @GetMapping("/list")
    public Result<IPage<CultureContent>> list(@RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int size,
                                               @RequestParam(required = false) Long categoryId,
                                               @RequestParam(required = false) String keyword,
                                               @RequestParam(required = false, defaultValue = "zh") String lang) {
        IPage<CultureContent> result = cultureService.listContents(page, size, categoryId, keyword);
        result.getRecords().forEach(c -> applyLang(c, lang));
        return Result.success(result);
    }

    @GetMapping("/detail")
    public Result<CultureContent> detail(@RequestParam Long id,
                                          @RequestParam(required = false, defaultValue = "zh") String lang) {
        CultureContent c = cultureService.getDetail(id);
        if (c != null) applyLang(c, lang);
        return Result.success(c);
    }

    @GetMapping("/categories")
    public Result<List<CultureCategory>> categories() {
        return Result.success(cultureService.listCategories());
    }

    @GetMapping("/categoriesByParent")
    public Result<List<CultureCategory>> categoriesByParent(@RequestParam Long parentId) {
        return Result.success(cultureService.listCategoriesByParent(parentId));
    }

    private void applyLang(CultureContent c, String lang) {
        if ("en".equals(lang)) {
            if (c.getTitleEn() != null && !c.getTitleEn().isEmpty()) c.setTitle(c.getTitleEn());
            if (c.getContentEn() != null && !c.getContentEn().isEmpty()) c.setContent(c.getContentEn());
        } else if ("ja".equals(lang)) {
            if (c.getTitleJa() != null && !c.getTitleJa().isEmpty()) c.setTitle(c.getTitleJa());
            if (c.getContentJa() != null && !c.getContentJa().isEmpty()) c.setContent(c.getContentJa());
        }
    }
}
