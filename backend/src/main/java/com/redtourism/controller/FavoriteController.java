package com.redtourism.controller;

import com.redtourism.common.Constants;
import com.redtourism.common.Result;
import com.redtourism.entity.Favorite;
import com.redtourism.entity.User;
import com.redtourism.service.InteractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    @Autowired
    private InteractionService interactionService;

    @GetMapping("/add")
    public Result<String> add(@RequestParam String targetType,
                               @RequestParam Long targetId,
                               HttpSession session) {
        User user = (User) session.getAttribute(Constants.SESSION_USER);
        if (user == null) return Result.error(401, "请先登录");
        interactionService.addFavorite(user.getId(), targetType, targetId);
        return Result.success("收藏成功", null);
    }

    @GetMapping("/remove")
    public Result<String> remove(@RequestParam String targetType,
                                  @RequestParam Long targetId,
                                  HttpSession session) {
        User user = (User) session.getAttribute(Constants.SESSION_USER);
        if (user == null) return Result.error(401, "请先登录");
        interactionService.removeFavorite(user.getId(), targetType, targetId);
        return Result.success("取消收藏", null);
    }

    @GetMapping("/check")
    public Result<Boolean> check(@RequestParam String targetType,
                                  @RequestParam Long targetId,
                                  HttpSession session) {
        User user = (User) session.getAttribute(Constants.SESSION_USER);
        if (user == null) return Result.success(false);
        return Result.success(interactionService.isFavorited(user.getId(), targetType, targetId));
    }

    @GetMapping("/myList")
    public Result<List<Favorite>> myList(@RequestParam(required = false) String targetType,
                                          HttpSession session) {
        User user = (User) session.getAttribute(Constants.SESSION_USER);
        if (user == null) return Result.error(401, "请先登录");
        return Result.success(interactionService.listUserFavorites(user.getId(), targetType));
    }
}
