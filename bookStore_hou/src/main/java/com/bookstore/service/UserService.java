package com.bookstore.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bookstore.dto.LoginRequest;
import com.bookstore.dto.RegisterRequest;
import com.bookstore.entity.User;
import com.bookstore.vo.LoginVO;

public interface UserService extends IService<User> {

    LoginVO register(RegisterRequest request);

    LoginVO login(LoginRequest request);

    User getUserById(Long id);

    void updateUserInfo(Long userId, User user);

    void updatePassword(Long userId, String oldPassword, String newPassword);

    void updateMemberLevel(Long userId);
}
