package com.redtourism.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.redtourism.common.Constants;
import com.redtourism.common.Result;
import com.redtourism.entity.User;
import com.redtourism.entity.UserCustomRoute;
import com.redtourism.mapper.UserCustomRouteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/customRoute")
public class CustomRouteController {

    @Autowired
    private UserCustomRouteMapper routeMapper;

    private User getUser(HttpSession session) {
        return (User) session.getAttribute(Constants.SESSION_USER);
    }

    @GetMapping("/list")
    public Result<List<UserCustomRoute>> list(HttpSession session) {
        User user = getUser(session);
        if (user == null) return Result.error(401, "请先登录");
        LambdaQueryWrapper<UserCustomRoute> w = new LambdaQueryWrapper<>();
        w.eq(UserCustomRoute::getUserId, user.getId())
         .orderByDesc(UserCustomRoute::getCreateTime);
        return Result.success(routeMapper.selectList(w));
    }

    @GetMapping("/detail")
    public Result<UserCustomRoute> detail(@RequestParam Long id, HttpSession session) {
        User user = getUser(session);
        if (user == null) return Result.error(401, "请先登录");
        UserCustomRoute r = routeMapper.selectById(id);
        if (r == null || !r.getUserId().equals(user.getId())) return Result.error("线路不存在");
        return Result.success(r);
    }

    @GetMapping("/save")
    public Result<UserCustomRoute> save(
            @RequestParam(required = false) Long id,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "1") Integer days,
            @RequestParam(required = false, defaultValue = "[]") String spotData,
            HttpSession session) {
        User user = getUser(session);
        if (user == null) return Result.error(401, "请先登录");

        UserCustomRoute route;
        if (id != null) {
            route = routeMapper.selectById(id);
            if (route == null || !route.getUserId().equals(user.getId()))
                return Result.error("线路不存在");
            route.setUpdateTime(new Date());
        } else {
            route = new UserCustomRoute();
            route.setUserId(user.getId());
            route.setCreateTime(new Date());
            route.setUpdateTime(new Date());
        }
        route.setName(name);
        route.setDescription(description);
        route.setDays(days);
        route.setSpotData(spotData);
        if (id != null) routeMapper.updateById(route);
        else routeMapper.insert(route);
        return Result.success(route);
    }

    /** 用户提交自定义线路供管理员审核（纳入官方推荐） */
    @GetMapping("/submitForReview")
    public Result<String> submitForReview(@RequestParam Long id, HttpSession session) {
        User user = getUser(session);
        if (user == null) return Result.error(401, "请先登录");
        UserCustomRoute route = routeMapper.selectById(id);
        if (route == null || !route.getUserId().equals(user.getId()))
            return Result.error("线路不存在");
        route.setStatus("SUBMITTED");
        route.setUpdateTime(new Date());
        routeMapper.updateById(route);
        return Result.success("已提交审核，等待管理员处理", null);
    }

    @GetMapping("/delete")
    public Result<String> delete(@RequestParam Long id, HttpSession session) {
        User user = getUser(session);
        if (user == null) return Result.error(401, "请先登录");
        UserCustomRoute route = routeMapper.selectById(id);
        if (route == null || !route.getUserId().equals(user.getId()))
            return Result.error("线路不存在");
        routeMapper.deleteById(id);
        return Result.success("删除成功", null);
    }
}
