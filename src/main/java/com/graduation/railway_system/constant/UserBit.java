package com.graduation.railway_system.constant;

/**
 * @author Dedalusin
 * @version 1.0
 * @date 2022/5/9 1:48
 */

/**
 * 用户状态位 int 32位空间，可以保存32种状态，具有可拓展性
 */
public enum UserBit {
    /**
     * 管理员
     */
    MANAGER("manager", 0),
    /**
     * 学生
     */
    STUDENT("student", 1);

    int statusBit;
    String statusName;
    UserBit(String statusName, int statusBit) {
        this.statusBit = statusBit;
        this.statusName = statusName;
    }

    public int getStatusBit() {
        return statusBit;
    }

    public void setStatusBit(int statusBit) {
        this.statusBit = statusBit;
    }
}
