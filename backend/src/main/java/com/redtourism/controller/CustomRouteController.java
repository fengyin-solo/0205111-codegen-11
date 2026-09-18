package com.redtourism.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.redtourism.common.Constants;
import com.redtourism.common.Result;
import com.redtourism.entity.CustomRouteShare;
import com.redtourism.entity.User;
import com.redtourism.entity.UserCustomRoute;
import com.redtourism.mapper.CustomRouteShareMapper;
import com.redtourism.mapper.UserCustomRouteMapper;
import com.redtourism.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customRoute")
public class CustomRouteController {

    /** 分享链接有效期：7 天 */
    private static final long SHARE_TTL_MS = 7L * 24 * 3600 * 1000;

    @Autowired
    private UserCustomRouteMapper routeMapper;
    @Autowired
    private CustomRouteShareMapper shareMapper;
    @Autowired
    private UserMapper userMapper;

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
        List<UserCustomRoute> routes = routeMapper.selectList(w);
        // 附带各线路当前有效的分享码，便于前端直接展示分享状态
        if (!routes.isEmpty()) {
            List<Long> ids = routes.stream().map(UserCustomRoute::getId).collect(Collectors.toList());
            LambdaQueryWrapper<CustomRouteShare> sw = new LambdaQueryWrapper<>();
            sw.in(CustomRouteShare::getRouteId, ids).eq(CustomRouteShare::getStatus, "ACTIVE");
            Map<Long, String> codeMap = shareMapper.selectList(sw).stream()
                    .collect(Collectors.toMap(CustomRouteShare::getRouteId, CustomRouteShare::getShareCode, (a, b) -> a));
            routes.forEach(r -> r.setShareCode(codeMap.get(r.getId())));
        }
        return Result.success(routes);
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
        // 线路删除后，分享记录保留为“已删除”状态，同伴打开链接时可看到明确提示
        LambdaQueryWrapper<CustomRouteShare> sw = new LambdaQueryWrapper<>();
        sw.eq(CustomRouteShare::getRouteId, id);
        List<CustomRouteShare> shares = shareMapper.selectList(sw);
        for (CustomRouteShare s : shares) {
            if (!"DELETED".equals(s.getStatus())) {
                s.setStatus("DELETED");
                shareMapper.updateById(s);
            }
        }
        routeMapper.deleteById(id);
        return Result.success("删除成功", null);
    }

    /** 生成（或刷新）只读分享链接，仅创建者可操作 */
    @GetMapping("/share/create")
    public Result<Map<String, Object>> createShare(@RequestParam Long id, HttpSession session) {
        User user = getUser(session);
        if (user == null) return Result.error(401, "请先登录");
        UserCustomRoute route = routeMapper.selectById(id);
        if (route == null || !route.getUserId().equals(user.getId()))
            return Result.error("线路不存在");

        Date expire = new Date(System.currentTimeMillis() + SHARE_TTL_MS);
        LambdaQueryWrapper<CustomRouteShare> w = new LambdaQueryWrapper<>();
        w.eq(CustomRouteShare::getRouteId, id)
         .eq(CustomRouteShare::getStatus, "ACTIVE")
         .orderByDesc(CustomRouteShare::getId)
         .last("limit 1");
        CustomRouteShare share = shareMapper.selectOne(w);
        if (share == null) {
            share = new CustomRouteShare();
            share.setRouteId(id);
            share.setUserId(user.getId());
            share.setShareCode(UUID.randomUUID().toString().replace("-", ""));
            share.setStatus("ACTIVE");
            share.setCreateTime(new Date());
            share.setExpireTime(expire);
            shareMapper.insert(share);
        } else {
            // 重新获取：刷新有效期，分享码保持不变
            share.setExpireTime(expire);
            shareMapper.updateById(share);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("shareCode", share.getShareCode());
        data.put("expireTime", share.getExpireTime());
        return Result.success("分享链接已生成", data);
    }

    /** 关闭分享，原有链接立即失效，仅创建者可操作 */
    @GetMapping("/share/close")
    public Result<String> closeShare(@RequestParam Long id, HttpSession session) {
        User user = getUser(session);
        if (user == null) return Result.error(401, "请先登录");
        UserCustomRoute route = routeMapper.selectById(id);
        if (route == null || !route.getUserId().equals(user.getId()))
            return Result.error("线路不存在");
        LambdaQueryWrapper<CustomRouteShare> w = new LambdaQueryWrapper<>();
        w.eq(CustomRouteShare::getRouteId, id).eq(CustomRouteShare::getStatus, "ACTIVE");
        List<CustomRouteShare> shares = shareMapper.selectList(w);
        for (CustomRouteShare s : shares) {
            s.setStatus("CLOSED");
            shareMapper.updateById(s);
        }
        return Result.success("分享已关闭", null);
    }

    /** 只读分享查看（无需登录），同伴只能查看不能改动 */
    @GetMapping("/shared")
    public Result<Map<String, Object>> shared(@RequestParam String code) {
        LambdaQueryWrapper<CustomRouteShare> w = new LambdaQueryWrapper<>();
        w.eq(CustomRouteShare::getShareCode, code).orderByDesc(CustomRouteShare::getId).last("limit 1");
        CustomRouteShare share = shareMapper.selectOne(w);
        if (share == null) return Result.error(404, "分享链接无效，请向同伴重新获取最新链接");
        if ("DELETED".equals(share.getStatus()))
            return Result.error(411, "该行程已被创建者删除，分享内容不再可用");
        if ("CLOSED".equals(share.getStatus()))
            return Result.error(410, "分享已被创建者关闭，请向同伴重新获取链接");
        if (share.getExpireTime() != null && share.getExpireTime().before(new Date()))
            return Result.error(410, "分享链接已过期，请向同伴重新获取链接");

        UserCustomRoute route = routeMapper.selectById(share.getRouteId());
        if (route == null) return Result.error(411, "该行程已被创建者删除，分享内容不再可用");

        User owner = userMapper.selectById(route.getUserId());
        String ownerName = owner != null
                ? (owner.getNickname() != null && !owner.getNickname().isEmpty() ? owner.getNickname() : owner.getUsername())
                : "同伴";

        // 只返回只读展示所需字段，不暴露创建者账号信息
        Map<String, Object> data = new HashMap<>();
        data.put("name", route.getName());
        data.put("description", route.getDescription());
        data.put("days", route.getDays());
        data.put("spotData", route.getSpotData());
        data.put("createTime", route.getCreateTime());
        data.put("updateTime", route.getUpdateTime());
        data.put("owner", ownerName);
        data.put("expireTime", share.getExpireTime());
        return Result.success(data);
    }
}
