package com.graduation.railway_system.annotation;

import java.lang.annotation.*;

/**
 * @author Dedalusin
 * @version 1.0
 * @date 2022/1/21 19:26
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NeedSession {
}
