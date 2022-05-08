package com.graduation.railway_system;

import com.graduation.railway_system.constant.UserBit;
import com.graduation.railway_system.utils.UserBitUtil;
import org.junit.jupiter.api.Test;

/**
 * @author Dedalusin
 * @version 1.0
 * @date 2022/5/9 2:02
 */
public class UserBitTests {
    @Test
    void test() {
        System.out.println(UserBitUtil.checkUserStatus(1, UserBit.STUDENT));
    }
}
