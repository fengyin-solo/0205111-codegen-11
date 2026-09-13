package com.redtourism.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.redtourism.entity.Message;

public interface MessageService extends IService<Message> {
    IPage<Message> listMessages(int page, int size, Long userId, Integer isRead);
    boolean markRead(Long id);
    boolean markAllRead(Long userId);
    long countUnread(Long userId);
    boolean sendMessage(Long userId, String title, String content);
}
