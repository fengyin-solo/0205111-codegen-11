package com.redtourism.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.redtourism.common.Constants;
import com.redtourism.common.Result;
import com.redtourism.entity.Feedback;
import com.redtourism.entity.User;
import com.redtourism.mapper.FeedbackMapper;
import com.redtourism.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    @Autowired private FeedbackMapper feedbackMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private com.redtourism.service.MessageService messageService;

    /** 提交反馈（可匿名） */
    @GetMapping("/submit")
    public Result<String> submit(@RequestParam String title,
                                  @RequestParam String content,
                                  @RequestParam(defaultValue = "GENERAL") String category,
                                  @RequestParam(required = false) String contact,
                                  HttpSession session) {
        Feedback fb = new Feedback();
        User user = (User) session.getAttribute(Constants.SESSION_USER);
        if (user != null) fb.setUserId(user.getId());
        fb.setTitle(title);
        fb.setContent(content);
        fb.setCategory(category);
        fb.setContact(contact);
        fb.setStatus("PENDING");
        fb.setCreateTime(new Date());
        feedbackMapper.insert(fb);
        return Result.success("反馈已提交，我们会尽快处理", null);
    }

    /** 当前登录用户的历史反馈 */
    @GetMapping("/my")
    public Result<List<Feedback>> myFeedbacks(HttpSession session) {
        User user = (User) session.getAttribute(Constants.SESSION_USER);
        if (user == null) return Result.error(401, "请先登录");
        LambdaQueryWrapper<Feedback> w = new LambdaQueryWrapper<>();
        w.eq(Feedback::getUserId, user.getId()).orderByDesc(Feedback::getCreateTime);
        return Result.success(feedbackMapper.selectList(w));
    }

    /** 管理员：分页列表 */
    @GetMapping("/admin/list")
    public Result<IPage<Feedback>> adminList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Feedback> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) w.eq(Feedback::getStatus, status);
        if (StringUtils.hasText(category)) w.eq(Feedback::getCategory, category);
        if (StringUtils.hasText(keyword))
            w.and(q -> q.like(Feedback::getTitle, keyword).or().like(Feedback::getContent, keyword));
        w.orderByDesc(Feedback::getCreateTime);
        IPage<Feedback> result = feedbackMapper.selectPage(new Page<>(page, size), w);
        result.getRecords().forEach(fb -> {
            if (fb.getUserId() != null) {
                User u = userMapper.selectById(fb.getUserId());
                if (u != null) fb.setUsername(u.getNickname() != null ? u.getNickname() : u.getUsername());
            }
        });
        return Result.success(result);
    }

    /** 管理员：回复反馈 */
    @GetMapping("/admin/reply")
    public Result<String> reply(@RequestParam Long id,
                                 @RequestParam String reply,
                                 @RequestParam(defaultValue = "RESOLVED") String status) {
        Feedback fb = feedbackMapper.selectById(id);
        if (fb == null) return Result.error("反馈不存在");
        fb.setReply(reply);
        fb.setStatus(status);
        fb.setReplyTime(new Date());
        feedbackMapper.updateById(fb);
        if (fb.getUserId() != null) {
            messageService.sendMessage(fb.getUserId(), "您的反馈已收到回复",
                    "您提交的【" + fb.getTitle() + "】已收到回复：" + reply);
        }
        return Result.success("回复成功", null);
    }

    /** 管理员：删除 */
    @GetMapping("/admin/delete")
    public Result<String> delete(@RequestParam Long id) {
        feedbackMapper.deleteById(id);
        return Result.success("删除成功", null);
    }

    /** 待处理数量 */
    @GetMapping("/admin/pendingCount")
    public Result<Long> pendingCount() {
        LambdaQueryWrapper<Feedback> w = new LambdaQueryWrapper<>();
        w.eq(Feedback::getStatus, "PENDING");
        return Result.success(feedbackMapper.selectCount(w));
    }
}
