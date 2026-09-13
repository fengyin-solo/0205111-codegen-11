package com.redtourism.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.redtourism.common.Constants;
import com.redtourism.common.Result;
import com.redtourism.entity.ScenicSpot;
import com.redtourism.entity.SpotSuggestion;
import com.redtourism.entity.User;
import com.redtourism.mapper.SpotSuggestionMapper;
import com.redtourism.mapper.UserMapper;
import com.redtourism.service.MessageService;
import com.redtourism.service.ScenicSpotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/spotSuggestion")
public class SpotSuggestionController {

    @Autowired private SpotSuggestionMapper mapper;
    @Autowired private UserMapper userMapper;
    @Autowired private ScenicSpotService spotService;
    @Autowired private MessageService messageService;

    @GetMapping("/submit")
    public Result<String> submit(@RequestParam Long spotId,
                                  @RequestParam String fieldName,
                                  @RequestParam String newValue,
                                  @RequestParam(required = false) String reason,
                                  HttpSession session) {
        User user = (User) session.getAttribute(Constants.SESSION_USER);
        if (user == null) return Result.error(401, "请先登录");
        ScenicSpot spot = spotService.getById(spotId);
        SpotSuggestion s = new SpotSuggestion();
        s.setUserId(user.getId());
        s.setSpotId(spotId);
        s.setSpotName(spot != null ? spot.getName() : "");
        s.setFieldName(fieldName);
        s.setNewValue(newValue);
        s.setReason(reason);
        s.setStatus("PENDING");
        s.setCreateTime(new Date());
        String oldVal = "";
        if (spot != null) {
            switch (fieldName) {
                case "name": oldVal = spot.getName(); break;
                case "description": oldVal = spot.getDescription(); break;
                case "location": oldVal = spot.getLocation(); break;
                case "openTime": oldVal = spot.getOpenTime(); break;
                case "ticketPrice": oldVal = spot.getTicketPrice() != null ? spot.getTicketPrice().toString() : ""; break;
                case "trafficInfo": oldVal = spot.getTrafficInfo(); break;
                default: oldVal = "";
            }
        }
        s.setOldValue(oldVal);
        mapper.insert(s);
        return Result.success("更正建议已提交，等待管理员审核", null);
    }

    @GetMapping("/my")
    public Result<List<SpotSuggestion>> myList(HttpSession session) {
        User user = (User) session.getAttribute(Constants.SESSION_USER);
        if (user == null) return Result.error(401, "请先登录");
        return Result.success(mapper.selectList(
                new LambdaQueryWrapper<SpotSuggestion>()
                        .eq(SpotSuggestion::getUserId, user.getId())
                        .orderByDesc(SpotSuggestion::getCreateTime)));
    }
}
