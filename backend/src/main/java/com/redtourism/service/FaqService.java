package com.redtourism.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.redtourism.entity.Faq;
import java.util.List;

public interface FaqService extends IService<Faq> {
    List<Faq> listAll();
    String autoReply(String question);
}
