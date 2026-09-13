package com.redtourism.controller;

import com.redtourism.common.Constants;
import com.redtourism.common.Result;
import com.redtourism.entity.User;
import com.redtourism.service.InteractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/like")
public class LikeController {

    @Autowired
    private InteractionService interactionService;

    @GetMapping("/add")
    public Result<String> add(@RequestParam String targetType,
                               @RequestParam Long targetId,
                               HttpSession session) {
        User user = (User) session.getAttribute(Constants.SESSION_USER);
        if (user == null) return Result.error(401, "请先登录");
        interactionService.addLike(user.getId(), targetType, targetId);
        return Result.success("点赞成功", null);
    }

    @GetMapping("/remove")
    public Result<String> remove(@RequestParam String targetType,
                                  @RequestParam Long targetId,
                                  HttpSession session) {
        User user = (User) session.getAttribute(Constants.SESSION_USER);
        if (user == null) return Result.error(401, "请先登录");
        interactionService.removeLike(user.getId(), targetType, targetId);
        return Result.success("取消点赞", null);
    }

    @GetMapping("/check")
    public Result<Boolean> check(@RequestParam String targetType,
                                  @RequestParam Long targetId,
                                  HttpSession session) {
        User user = (User) session.getAttribute(Constants.SESSION_USER);
        if (user == null) return Result.success(false);
        return Result.success(interactionService.isLiked(user.getId(), targetType, targetId));
    }

    @GetMapping("/count")
    public Result<Long> count(@RequestParam String targetType,
                               @RequestParam Long targetId) {
        return Result.success(interactionService.countLikes(targetType, targetId));
    }
}
