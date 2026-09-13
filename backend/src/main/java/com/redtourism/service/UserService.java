package com.redtourism.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.redtourism.entity.User;

public interface UserService extends IService<User> {
    User findByUsername(String username);
    User findByPhone(String phone);
    User login(String username, String password);
    User register(String username, String password, String phone);
    boolean resetPassword(String phone, String oldPassword, String newPassword);
    IPage<User> listUsers(int page, int size, String role, Integer status, String keyword);
    boolean toggleStatus(Long id);
}
