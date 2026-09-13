package com.redtourism.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.redtourism.common.Constants;
import com.redtourism.common.Result;
import com.redtourism.entity.Message;
import com.redtourism.entity.User;
import com.redtourism.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping("/list")
    public Result<IPage<Message>> list(@RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "10") int size,
                                        @RequestParam(required = false) Integer isRead,
                                        HttpSession session) {
        User user = (User) session.getAttribute(Constants.SESSION_USER);
        if (user == null) return Result.error(401, "请先登录");
        return Result.success(messageService.listMessages(page, size, user.getId(), isRead));
    }

    @GetMapping("/read")
    public Result<String> markRead(@RequestParam Long id) {
        messageService.markRead(id);
        return Result.success("已读", null);
    }

    @GetMapping("/readAll")
    public Result<String> markAllRead(HttpSession session) {
        User user = (User) session.getAttribute(Constants.SESSION_USER);
        if (user == null) return Result.error(401, "请先登录");
        messageService.markAllRead(user.getId());
        return Result.success("全部已读", null);
    }

    @GetMapping("/unreadCount")
    public Result<Long> unreadCount(HttpSession session) {
        User user = (User) session.getAttribute(Constants.SESSION_USER);
        if (user == null) return Result.success(0L);
        return Result.success(messageService.countUnread(user.getId()));
    }
}
