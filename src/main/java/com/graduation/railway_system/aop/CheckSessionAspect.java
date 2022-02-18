package com.graduation.railway_system.aop;

import com.graduation.railway_system.model.ResponseVo;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Dedalusin
 * @version 1.0
 * @date 2022/1/21 19:28
 */
@Aspect
@Component
public class CheckSessionAspect {

    @Pointcut("@annotation(com.graduation.railway_system.annotation.NeedSession)")
    public void checkSessionPointCut(){}

    @Autowired
    HttpServletRequest request;

    @Around("checkSessionPointCut()")
    public Object before(ProceedingJoinPoint joinPoint) throws Throwable {
        if (request.getSession().getAttribute("userId") == null) {
            return ResponseVo.failed("需要重新登录");
        }
        return joinPoint.proceed();
    }


}
