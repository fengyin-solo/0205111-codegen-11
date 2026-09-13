package com.redtourism.controller;

import com.redtourism.common.Constants;
import com.redtourism.common.Result;
import com.redtourism.entity.User;
import com.redtourism.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Collections;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public Result<User> login(@RequestParam String username,
                               @RequestParam String password,
                               HttpSession session) {
        User user = userService.login(username, password);
        session.setAttribute(Constants.SESSION_USER, user);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(user, null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole())));
        SecurityContextHolder.getContext().setAuthentication(authToken);
        user.setPassword(null);
        return Result.success("登录成功", user);
    }

    @GetMapping("/register")
    public Result<User> register(@RequestParam String username,
                                  @RequestParam String password,
                                  @RequestParam String phone) {
        User user = userService.register(username, password, phone);
        user.setPassword(null);
        return Result.success("注册成功", user);
    }

    @GetMapping("/resetPassword")
    public Result<String> resetPassword(@RequestParam String phone,
                                         @RequestParam String oldPassword,
                                         @RequestParam String newPassword) {
        userService.resetPassword(phone, oldPassword, newPassword);
        return Result.success("密码重置成功", null);
    }

    @GetMapping("/logout")
    public Result<String> logout(HttpSession session) {
        session.invalidate();
        SecurityContextHolder.clearContext();
        return Result.success("退出成功", null);
    }

    @GetMapping("/currentUser")
    public Result<User> currentUser(HttpSession session) {
        User user = (User) session.getAttribute(Constants.SESSION_USER);
        if (user == null) {
            return Result.error(401, "未登录");
        }
        User fresh = userService.getById(user.getId());
        if (fresh != null) {
            fresh.setPassword(null);
        }
        return Result.success(fresh);
    }
}
