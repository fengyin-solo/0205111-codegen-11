package com.redtourism.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.redtourism.common.Constants;
import com.redtourism.common.Result;
import com.redtourism.entity.ServiceChat;
import com.redtourism.entity.User;
import com.redtourism.mapper.ServiceChatMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ServiceChatController {

    @Autowired private ServiceChatMapper mapper;

    @GetMapping("/send")
    public Result<String> send(@RequestParam String content, HttpSession session) {
        User user = (User) session.getAttribute(Constants.SESSION_USER);
        if (user == null) return Result.error(401, "请先登录");
        ServiceChat c = new ServiceChat();
        c.setUserId(user.getId());
        c.setSender("USER");
        c.setContent(content);
        c.setCreateTime(new Date());
        mapper.insert(c);
        return Result.success("已发送", null);
    }

    @GetMapping("/history")
    public Result<List<ServiceChat>> history(HttpSession session) {
        User user = (User) session.getAttribute(Constants.SESSION_USER);
        if (user == null) return Result.error(401, "请先登录");
        return Result.success(mapper.selectList(
                new LambdaQueryWrapper<ServiceChat>()
                        .eq(ServiceChat::getUserId, user.getId())
                        .orderByAsc(ServiceChat::getCreateTime)));
    }
}
