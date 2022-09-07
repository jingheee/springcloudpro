package com.atguigu.gulimall.seckill.interceptor;

import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;

@Component
public class RedissonInterceptor implements HandlerInterceptor {
    @Autowired
    RedissonClient redissonClient;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        RRateLimiter rateLimiter = redissonClient.getRateLimiter("request.getRemoteHost()");
        rateLimiter.expire(Duration.ofSeconds(5));
        rateLimiter.trySetRate(RateType.OVERALL, 1, 60, RateIntervalUnit.SECONDS);

        if (rateLimiter.tryAcquire()) {
            return true;
        }
        response.setStatus(429);
        return false;
    }
}
