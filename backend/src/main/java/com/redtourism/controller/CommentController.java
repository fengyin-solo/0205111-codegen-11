package com.redtourism.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.redtourism.common.Constants;
import com.redtourism.common.Result;
import com.redtourism.entity.Comment;
import com.redtourism.entity.User;
import com.redtourism.service.InteractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Autowired
    private InteractionService interactionService;

    @GetMapping("/add")
    public Result<String> add(@RequestParam String targetType,
                               @RequestParam Long targetId,
                               @RequestParam String content,
                               @RequestParam(required = false) String images,
                               @RequestParam(required = false, defaultValue = "5") Integer rating,
                               HttpSession session) {
        User user = (User) session.getAttribute(Constants.SESSION_USER);
        if (user == null) return Result.error(401, "请先登录");
        Comment comment = new Comment();
        comment.setUserId(user.getId());
        comment.setTargetType(targetType);
        comment.setTargetId(targetId);
        comment.setContent(content);
        comment.setImages(images);
        comment.setRating(rating);
        interactionService.addComment(comment);
        return Result.success("评论成功", null);
    }

    @GetMapping("/list")
    public Result<IPage<Comment>> list(@RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "10") int size,
                                        @RequestParam(required = false) String targetType,
                                        @RequestParam(required = false) Long targetId) {
        return Result.success(interactionService.listComments(page, size, targetType, targetId));
    }

    @GetMapping("/delete")
    public Result<String> delete(@RequestParam Long id, HttpSession session) {
        User user = (User) session.getAttribute(Constants.SESSION_USER);
        if (user == null) return Result.error(401, "请先登录");
        interactionService.deleteComment(id);
        return Result.success("删除成功", null);
    }
}
