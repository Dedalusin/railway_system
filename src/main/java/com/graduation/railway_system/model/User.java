package com.graduation.railway_system.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author Dedalusin
 * @version 1.0
 * @date 2022/1/19 22:33
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Component
public class User implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long userId;

    private String userName;
    @NotNull(message = "密码不能为空")
    private String password;
    @NotNull(message = "身份证类型不能为空")
    private String identityCardType;
    @NotNull(message = "用户名不能为空")
    private String identityCardName;
    @NotNull(message = "身份证密码不能为空")
    private String identityCardId;
    @NotNull(message = "用户类型不能为空")
    private String accountType;

    private String email;
    @NotNull(message = "手机号不能为空")
    private String phone;
}

