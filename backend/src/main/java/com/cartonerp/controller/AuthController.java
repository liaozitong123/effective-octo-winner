package com.cartonerp.controller;

import com.cartonerp.common.Result;
import com.cartonerp.dto.LoginRequest;
import com.cartonerp.entity.User;
import com.cartonerp.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired private UserRepository userRepo;
    @Autowired private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginRequest req, HttpSession session) {
        Optional<User> opt = userRepo.findByUsername(req.getUsername());
        if (opt.isEmpty() || !passwordEncoder.matches(req.getPassword(), opt.get().getPassword())) {
            return Result.fail(401, "用户名或密码错误");
        }
        User user = opt.get();
        session.setAttribute("userId", user.getId());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("displayName", user.getDisplayName());
        return Result.ok(Map.of(
            "username", user.getUsername(),
            "displayName", user.getDisplayName() != null ? user.getDisplayName() : user.getUsername()
        ));
    }

    @GetMapping("/current-user")
    public Result<?> currentUser(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) return Result.fail(401, "未登录");
        return Result.ok(Map.of(
            "username", username,
            "displayName", session.getAttribute("displayName")
        ));
    }
}
