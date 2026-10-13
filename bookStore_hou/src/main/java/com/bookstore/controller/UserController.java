package com.bookstore.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookstore.common.PageResult;
import com.bookstore.common.Result;
import com.bookstore.dto.LoginRequest;
import com.bookstore.dto.RegisterRequest;
import com.bookstore.entity.User;
import com.bookstore.service.UserService;
import com.bookstore.vo.LoginVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success("注册成功", userService.register(request));
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.success("登录成功", userService.login(request));
    }

    @GetMapping("/info")
    public Result<User> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getUserById(userId);
        user.setPassword(null);
        return Result.success(user);
    }

    @PutMapping("/update")
    public Result<?> updateUserInfo(@RequestBody User user, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        userService.updateUserInfo(userId, user);
        return Result.success("更新成功");
    }

    @PutMapping("/password")
    public Result<?> updatePassword(@RequestBody java.util.Map<String, String> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        userService.updatePassword(userId, params.get("oldPassword"), params.get("newPassword"));
        return Result.success("密码修改成功");
    }

    @GetMapping("/list")
    public Result<PageResult<User>> listUsers(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {
        Page<User> page = userService.page(new Page<>(current, size));
        page.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(PageResult.from(page));
    }

    @PutMapping("/admin/update")
    public Result<?> adminUpdateUser(@RequestBody User user) {
        User existing = userService.getUserById(user.getId());
        if (existing == null) {
            return Result.error("用户不存在");
        }
        if (existing.getRole() != null && existing.getRole() == 1) {
            if (user.getStatus() != null && user.getStatus() == 0) {
                return Result.error("不能禁用管理员账户");
            }
        }
        user.setPassword(null);
        userService.updateById(user);
        return Result.success("更新成功");
    }
}
