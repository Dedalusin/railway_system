package com.graduation.railway_system.service;

import com.graduation.railway_system.model.LoginRequest;
import com.graduation.railway_system.model.LoginResponse;
import com.graduation.railway_system.model.User;
import org.apache.kafka.common.security.auth.Login;

/**
 * @author Dedalusin
 * @version 1.0
 * @date 2022/1/19 22:17
 */
public interface UserService {

    public LoginResponse login(LoginRequest loginRequest);

    public User getUserInfo(Long userId);

    public boolean updateUserInfo(User user);

    public int registerUser(User user);
}
