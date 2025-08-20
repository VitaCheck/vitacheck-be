package com.vitacheck.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import java.lang.reflect.Method;
import java.util.Arrays;

@Slf4j
public class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        log.error("--- @Async Uncaught Exception ---");
        log.error("Exception Message: {}", ex.getMessage());
        log.error("Method Name: {}", method.getName());
        log.error("Parameters: {}", Arrays.toString(params));
        log.error("---------------------------------", ex);
    }
}
