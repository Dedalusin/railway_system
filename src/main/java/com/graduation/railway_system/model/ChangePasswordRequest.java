package com.graduation.railway_system.model;

import lombok.Data;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

/**
 * @author Dedalusin
 * @version 1.0
 * @date 2022/1/28 14:48
 */
@Component
@Data
public class ChangePasswordRequest {

    @NotNull(message = "旧密码不能为空")
    String oldPassword;

    @NotNull(message = "新密码不能为空")
    String newPassword;


}
