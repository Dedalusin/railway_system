package com.graduation.railway_system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.graduation.railway_system.constant.UserBit;
import com.graduation.railway_system.model.LoginRequest;
import com.graduation.railway_system.model.LoginResponse;
import com.graduation.railway_system.model.User;
import com.graduation.railway_system.repository.UserMapper;
import com.graduation.railway_system.service.UserService;
import com.graduation.railway_system.utils.UserBitUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Dedalusin
 * @version 1.0
 * @date 2022/1/19 22:17
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("user_id", loginRequest.getUserId()).eq("password", loginRequest.getPassword()));
        LoginResponse response = new LoginResponse();
        if (user == null) {
            return null;
        }
        BeanUtils.copyProperties(user, response);
        response.setEnableManager(UserBitUtil.checkUser(user, UserBit.MANAGER));
        return response;
    }

    @Override
    public User getUserInfo(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public boolean updateUserInfo(User user) {
        if (userMapper.updateById(user) >= 1) {
            return true;
        }
        return false;
    }

    @Override
    public int registerUser(User user) {
        return userMapper.insert(user);
    }

}
