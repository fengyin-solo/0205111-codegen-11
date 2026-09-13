package com.redtourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.redtourism.entity.Faq;
import com.redtourism.mapper.FaqMapper;
import com.redtourism.service.FaqService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FaqServiceImpl extends ServiceImpl<FaqMapper, Faq> implements FaqService {

    @Override
    public List<Faq> listAll() {
        LambdaQueryWrapper<Faq> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Faq::getSortOrder);
        return list(wrapper);
    }

    @Override
    public String autoReply(String question) {
        if (question == null || question.trim().isEmpty()) {
            return "请输入您的问题";
        }
        String q = question.toLowerCase();
        List<Faq> faqs = listAll();
        for (Faq faq : faqs) {
            if (q.contains(faq.getQuestion().toLowerCase()) ||
                faq.getQuestion().toLowerCase().contains(q)) {
                return faq.getAnswer();
            }
        }
        String[] keywords = q.split("\\s+");
        for (Faq faq : faqs) {
            for (String kw : keywords) {
                if (kw.length() >= 2 && faq.getQuestion().toLowerCase().contains(kw)) {
                    return faq.getAnswer();
                }
            }
        }
        return "抱歉，暂时无法回答您的问题。建议您联系人工客服获取帮助，或拨打服务热线：0851-12345。";
    }
}
