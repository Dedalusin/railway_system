package com.graduation.railway_system.utils;

import com.graduation.railway_system.constant.UserBit;
import com.graduation.railway_system.model.User;

/**
 * @author Dedalusin
 * @version 1.0
 * @date 2022/5/9 1:58
 */
public class UserBitUtil {

    public static boolean checkUserStatus(int userStatus, UserBit userBit) {
        return (userStatus & 1 << userBit.getStatusBit()) != 0;
    }

    public static boolean checkUser(User user, UserBit userBit) {
        return checkUserStatus(user.getUserStatus(), userBit);
    }

    public static void addUserStatus(User user, UserBit userBit) {
        if (checkUserStatus(user.getUserStatus(), userBit)){
            return;
        }
        int userStatus = user.getUserStatus();
        user.setUserStatus(userStatus += 1 << userBit.getStatusBit());
    }

    public static void deleteUserStatus(User user, UserBit userBit) {
        if (!checkUserStatus(user.getUserStatus(), userBit)){
            return;
        }
        int userStatus = user.getUserStatus();
        user.setUserStatus(userStatus -= 1 << userBit.getStatusBit());
    }

}
