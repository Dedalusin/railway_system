package com.graduation.railway_system.controller;

import com.graduation.railway_system.annotation.NeedSession;
import com.graduation.railway_system.model.*;
import com.graduation.railway_system.service.UserService;
import com.graduation.railway_system.utils.UUIDUtil;
import com.sun.deploy.net.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.invoke.MethodType;

/**
 * @author Dedalusin
 * @version 1.0
 * @date 2022/1/19 0:19
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseVo login(@RequestBody LoginRequest loginRequest, HttpSession session) {
        LoginResponse loginResponse = userService.login(loginRequest);
        if (loginResponse == null) {
            return ResponseVo.failed("账号或密码错误");
        }
        session.setAttribute("token", UUIDUtil.generateToken());
        session.setAttribute("userId", loginResponse.getUserId());
        return ResponseVo.success(loginResponse);
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseVo register(@RequestBody User user) {
        int result = userService.registerUser(user);
        if (result >= 1) {
            return ResponseVo.success("注册成功");
        }
        return ResponseVo.failed("账号信息有误");
    }

    @NeedSession
    @RequestMapping(value = "/userInfo", method = RequestMethod.GET)
    public ResponseVo getUserInfo(HttpSession session) {
        Long userId = Long.parseLong(session.getAttribute("userId").toString());
        User user = userService.getUserInfo(userId);
        if (user == null) {
            return ResponseVo.failed("账号信息错误");
        } else {
            return ResponseVo.success(user);
        }
    }

    @NeedSession
    @RequestMapping(value = "/changePassword", method = RequestMethod.POST)
    public ResponseVo changePassword(@RequestBody ChangePasswordRequest request, HttpSession session) {
        Long userId = Long.parseLong(session.getAttribute("userId").toString());
        User user = userService.getUserInfo(userId);
        if (!request.getOldPassword().equals(user.getPassword())) {
            return ResponseVo.failed("旧密码输入错误");
        }
        user.setPassword(request.getNewPassword());
        userService.updateUserInfo(user);
        return ResponseVo.success("修改成功");
    }

    @NeedSession
    @RequestMapping(value = "/changeUserInfo", method = RequestMethod.POST)
    public ResponseVo changeUserInfo(@RequestBody User user, HttpSession session) {
        user.setUserId(Long.parseLong(session.getAttribute("userId").toString()));
        if (userService.updateUserInfo(user)) {
            return ResponseVo.success("更新成功");
        }
        return ResponseVo.failed("更新失败");
    }

    @NeedSession
    @RequestMapping(value = "/signout", method = RequestMethod.GET)
    public ResponseVo signout(HttpSession session) {
        session.removeAttribute("token");
        session.removeAttribute("userId");
        int i = 1;
        return ResponseVo.success("已退出");
    }

}
