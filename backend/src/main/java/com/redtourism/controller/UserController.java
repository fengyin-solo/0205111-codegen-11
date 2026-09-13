package com.redtourism.controller;

import com.redtourism.common.Constants;
import com.redtourism.common.Result;
import com.redtourism.entity.User;
import com.redtourism.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public Result<User> getProfile(HttpSession session) {
        User current = (User) session.getAttribute(Constants.SESSION_USER);
        if (current == null) return Result.error(401, "未登录");
        User user = userService.getById(current.getId());
        user.setPassword(null);
        return Result.success(user);
    }

    @GetMapping("/updateProfile")
    public Result<User> updateProfile(@RequestParam(required = false) String nickname,
                                       @RequestParam(required = false) String phone,
                                       @RequestParam(required = false) String avatar,
                                       HttpSession session) {
        User current = (User) session.getAttribute(Constants.SESSION_USER);
        if (current == null) return Result.error(401, "未登录");
        User user = userService.getById(current.getId());
        if (nickname != null) user.setNickname(nickname);
        if (phone != null) user.setPhone(phone);
        if (avatar != null) user.setAvatar(avatar);
        userService.updateById(user);
        session.setAttribute(Constants.SESSION_USER, user);
        user.setPassword(null);
        return Result.success("更新成功", user);
    }

    @GetMapping("/updatePassword")
    public Result<String> updatePassword(@RequestParam String oldPassword,
                                          @RequestParam String newPassword,
                                          HttpSession session) {
        User current = (User) session.getAttribute(Constants.SESSION_USER);
        if (current == null) return Result.error(401, "未登录");
        User user = userService.getById(current.getId());
        if (!user.getPassword().equals(oldPassword)) {
            return Result.error("旧密码错误");
        }
        user.setPassword(newPassword);
        userService.updateById(user);
        return Result.success("密码修改成功", null);
    }
}
