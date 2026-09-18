package com.redtourism.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.redtourism.common.Constants;
import com.redtourism.common.Result;
import com.redtourism.entity.User;
import com.redtourism.entity.UserCustomRoute;
import com.redtourism.mapper.UserCustomRouteMapper;
import com.redtourism.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.*;

@RestController
@RequestMapping("/api/customRoute")
public class CustomRouteController {

    /** 分享链接有效期：30 天 */
    private static final long SHARE_VALID_MILLIS = 30L * 24 * 60 * 60 * 1000;

    @Autowired
    private UserCustomRouteMapper routeMapper;

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
         .orderByDesc(UserCustomRoute::getUpdateTime);
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

    /** 保存行程（支持 POST JSON，行程条目较长时不再受 URL 长度限制） */
    @PostMapping("/save")
    public Result<UserCustomRoute> savePost(@RequestBody Map<String, Object> body, HttpSession session) {
        User user = getUser(session);
        if (user == null) return Result.error(401, "请先登录");
        Long id = body.get("id") != null ? Long.valueOf(String.valueOf(body.get("id"))) : null;
        Integer days = body.get("days") != null ? Integer.valueOf(String.valueOf(body.get("days"))) : 1;
        return doSave(user, id, str(body.get("name")), str(body.get("description")), days, str(body.get("spotData")));
    }

    /** 保存行程（GET 兼容旧调用方式） */
    @GetMapping("/save")
    public Result<UserCustomRoute> saveGet(
            @RequestParam(required = false) Long id,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "1") Integer days,
            @RequestParam(required = false, defaultValue = "[]") String spotData,
            HttpSession session) {
        User user = getUser(session);
        if (user == null) return Result.error(401, "请先登录");
        return doSave(user, id, name, description, days, spotData);
    }

    private Result<UserCustomRoute> doSave(User user, Long id, String name, String description,
                                           Integer days, String spotData) {
        if (name == null || name.trim().isEmpty()) return Result.error("请填写线路名称");
        if (days == null || days < 1) days = 1;
        if (spotData == null || spotData.isEmpty()) spotData = "[]";

        UserCustomRoute route;
        Date now = new Date();
        if (id != null) {
            route = routeMapper.selectById(id);
            if (route == null || !route.getUserId().equals(user.getId()))
                return Result.error("线路不存在或您没有编辑权限");
            route.setUpdateTime(now);
        } else {
            route = new UserCustomRoute();
            route.setUserId(user.getId());
            route.setStatus("DRAFT");
            route.setCreateTime(now);
            route.setUpdateTime(now);
        }
        route.setName(name.trim());
        route.setDescription(description);
        route.setDays(days);
        route.setSpotData(spotData);
        if (id != null) routeMapper.updateById(route);
        else routeMapper.insert(route);
        return Result.success(route);
    }

    /**
     * 登录后把游客本机（localStorage）保存的行程草稿合并到账号。
     * 请求体：{routes: [{name, description, days, spotData}, ...]}
     * 按 名称+行程内容 去重，已存在完全相同的草稿时跳过。
     */
    @PostMapping("/merge")
    public Result<Map<String, Object>> merge(@RequestBody Map<String, Object> body, HttpSession session) {
        User user = getUser(session);
        if (user == null) return Result.error(401, "请先登录");
        List<?> routes = body != null && body.get("routes") instanceof List ? (List<?>) body.get("routes") : Collections.emptyList();

        LambdaQueryWrapper<UserCustomRoute> w = new LambdaQueryWrapper<>();
        w.eq(UserCustomRoute::getUserId, user.getId());
        List<UserCustomRoute> existing = routeMapper.selectList(w);

        int merged = 0, skipped = 0;
        Date now = new Date();
        for (Object item : routes) {
            if (!(item instanceof Map)) continue;
            Map<?, ?> rt = (Map<?, ?>) item;
            String name = str(rt.get("name"));
            String spotData = str(rt.get("spotData"));
            if (name == null || name.trim().isEmpty()) { skipped++; continue; }
            if (spotData == null || spotData.isEmpty()) spotData = "[]";
            Integer days = rt.get("days") != null ? Integer.valueOf(String.valueOf(rt.get("days"))) : 1;

            final String fName = name.trim();
            final String fData = spotData;
            boolean dup = existing.stream().anyMatch(e ->
                    fName.equals(e.getName()) && fData.equals(e.getSpotData()));
            if (dup) { skipped++; continue; }

            UserCustomRoute route = new UserCustomRoute();
            route.setUserId(user.getId());
            route.setName(fName);
            route.setDescription(str(rt.get("description")));
            route.setDays(days);
            route.setSpotData(fData);
            route.setStatus("DRAFT");
            route.setCreateTime(now);
            route.setUpdateTime(now);
            routeMapper.insert(route);
            merged++;
        }
        Map<String, Object> res = new HashMap<>();
        res.put("merged", merged);
        res.put("skipped", skipped);
        return Result.success(res);
    }

    /** 用户提交自定义线路供管理员审核（纳入官方推荐） */
    @GetMapping("/submitForReview")
    public Result<String> submitForReview(@RequestParam Long id, HttpSession session) {
        User user = getUser(session);
        if (user == null) return Result.error(401, "请先登录");
        UserCustomRoute route = routeMapper.selectById(id);
        if (route == null || !route.getUserId().equals(user.getId()))
            return Result.error("线路不存在或您没有编辑权限");
        route.setStatus("SUBMITTED");
        route.setUpdateTime(new Date());
        routeMapper.updateById(route);
        return Result.success("已提交审核，等待管理员处理", null);
    }

    /** 创建者删除行程：软删除，分享详情页据此向同伴注明情况 */
    @GetMapping("/delete")
    public Result<String> delete(@RequestParam Long id, HttpSession session) {
        User user = getUser(session);
        if (user == null) return Result.error(401, "请先登录");
        UserCustomRoute route = routeMapper.selectById(id);
        if (route == null || !route.getUserId().equals(user.getId()))
            return Result.error("线路不存在或您没有编辑权限");
        routeMapper.deleteById(id);
        return Result.success("删除成功", null);
    }

    // ==================== 只读分享 ====================

    /** 生成只读分享链接（幂等：已有未失效令牌则直接返回，失效则重新生成） */
    @GetMapping("/share/create")
    public Result<Map<String, Object>> createShare(@RequestParam Long id, HttpSession session) {
        User user = getUser(session);
        if (user == null) return Result.error(401, "请先登录");
        UserCustomRoute route = routeMapper.selectById(id);
        if (route == null || !route.getUserId().equals(user.getId()))
            return Result.error("线路不存在或您没有编辑权限");
        if (route.getShareToken() == null
                || route.getShareExpireTime() == null
                || route.getShareExpireTime().before(new Date())) {
            route.setShareToken(UUID.randomUUID().toString().replace("-", ""));
            route.setShareExpireTime(new Date(System.currentTimeMillis() + SHARE_VALID_MILLIS));
            route.setUpdateTime(new Date());
            routeMapper.updateById(route);
        }
        return Result.success(buildShareInfo(route));
    }

    /** 作废旧链接并重新生成（旧链接立即失效） */
    @GetMapping("/share/refresh")
    public Result<Map<String, Object>> refreshShare(@RequestParam Long id, HttpSession session) {
        User user = getUser(session);
        if (user == null) return Result.error(401, "请先登录");
        UserCustomRoute route = routeMapper.selectById(id);
        if (route == null || !route.getUserId().equals(user.getId()))
            return Result.error("线路不存在或您没有编辑权限");
        route.setShareToken(UUID.randomUUID().toString().replace("-", ""));
        route.setShareExpireTime(new Date(System.currentTimeMillis() + SHARE_VALID_MILLIS));
        route.setUpdateTime(new Date());
        routeMapper.updateById(route);
        return Result.success(buildShareInfo(route));
    }

    /** 关闭分享（令链接失效） */
    @GetMapping("/share/revoke")
    public Result<String> revokeShare(@RequestParam Long id, HttpSession session) {
        User user = getUser(session);
        if (user == null) return Result.error(401, "请先登录");
        UserCustomRoute route = routeMapper.selectById(id);
        if (route == null || !route.getUserId().equals(user.getId()))
            return Result.error("线路不存在或您没有编辑权限");
        route.setShareToken(null);
        route.setShareExpireTime(null);
        route.setUpdateTime(new Date());
        routeMapper.updateById(route);
        return Result.success("已关闭分享", null);
    }

    /** 同伴凭令牌查看只读行程（无需登录） */
    @GetMapping("/shared")
    public Result<Map<String, Object>> shared(@RequestParam String token) {
        if (token == null || token.trim().length() < 8) {
            return Result.error(404, "分享链接无效，请向行程创建者重新获取");
        }
        UserCustomRoute route = routeMapper.selectByShareToken(token.trim());
        if (route == null) {
            return Result.error(404, "分享链接无效，请向行程创建者重新获取");
        }
        if (route.getDeleted() != null && route.getDeleted() == 1) {
            Map<String, Object> deletedInfo = new HashMap<>();
            deletedInfo.put("deleted", true);
            deletedInfo.put("name", route.getName());
            Result<Map<String, Object>> deleted = Result.error(410, "行程创建者已删除该行程，无法继续查看");
            deleted.setData(deletedInfo);
            return deleted;
        }
        if (route.getShareExpireTime() == null || route.getShareExpireTime().before(new Date())) {
            return Result.error(403, "分享链接已失效，请向行程创建者重新获取");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id", route.getId());
        data.put("name", route.getName());
        data.put("description", route.getDescription());
        data.put("days", route.getDays());
        data.put("spotData", route.getSpotData());
        data.put("status", route.getStatus());
        data.put("createTime", route.getCreateTime());
        data.put("expireTime", route.getShareExpireTime());
        data.put("readOnly", true);
        User owner = userMapper.selectById(route.getUserId());
        data.put("ownerName", owner != null
                ? (owner.getNickname() != null && !owner.getNickname().isEmpty() ? owner.getNickname() : owner.getUsername())
                : "行程创建者");
        return Result.success(data);
    }

    private Map<String, Object> buildShareInfo(UserCustomRoute route) {
        Map<String, Object> info = new HashMap<>();
        info.put("token", route.getShareToken());
        info.put("expireTime", route.getShareExpireTime());
        info.put("url", "shared-route.html?token=" + route.getShareToken());
        return info;
    }

    private static String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }
}
