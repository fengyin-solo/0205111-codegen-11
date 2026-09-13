package com.redtourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.redtourism.common.Constants;
import com.redtourism.entity.User;
import com.redtourism.mapper.UserMapper;
import com.redtourism.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User findByUsername(String username) {
        return lambdaQuery().eq(User::getUsername, username).one();
    }

    @Override
    public User findByPhone(String phone) {
        return lambdaQuery().eq(User::getPhone, phone).one();
    }

    @Override
    public User login(String username, String password) {
        User user = findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("密码错误");
        }
        if (user.getStatus() == Constants.STATUS_DISABLED) {
            throw new RuntimeException("账号已被禁用");
        }
        return user;
    }

    @Override
    public User register(String username, String password, String phone) {
        if (findByUsername(username) != null) {
            throw new RuntimeException("用户名已存在");
        }
        if (findByPhone(phone) != null) {
            throw new RuntimeException("手机号已注册");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setPhone(phone);
        user.setNickname(username);
        user.setRole(Constants.ROLE_USER);
        user.setStatus(Constants.STATUS_ENABLED);
        save(user);
        return user;
    }

    @Override
    public boolean resetPassword(String phone, String oldPassword, String newPassword) {
        User user = findByPhone(phone);
        if (user == null) {
            throw new RuntimeException("手机号未注册");
        }
        if (!user.getPassword().equals(oldPassword)) {
            throw new RuntimeException("旧密码错误");
        }
        user.setPassword(newPassword);
        return updateById(user);
    }

    @Override
    public IPage<User> listUsers(int page, int size, String role, Integer status, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(role)) {
            wrapper.eq(User::getRole, role);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getPhone, keyword));
        }
        wrapper.orderByDesc(User::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public boolean toggleStatus(Long id) {
        User user = getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setStatus(user.getStatus() == Constants.STATUS_ENABLED ?
                Constants.STATUS_DISABLED : Constants.STATUS_ENABLED);
        return updateById(user);
    }
}
